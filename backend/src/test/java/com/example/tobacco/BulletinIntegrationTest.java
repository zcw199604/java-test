package com.example.tobacco;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BulletinIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateAndQueryBulletinsAfterMapperMigration() throws Exception {
        String token = login("seller", "123456");
        String suffix = String.valueOf(System.currentTimeMillis());
        String title = "公告迁移测试" + suffix;

        mockMvc.perform(post("/api/bulletins")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"content\":\"BulletinMapper回归验证\",\"category\":\"NOTICE\",\"expiredAt\":\"2026-12-31 23:59:59\"}"))
                .andExpect(status().isOk());

        MvcResult listResult = mockMvc.perform(get("/api/bulletins")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode listJson = objectMapper.readTree(listResult.getResponse().getContentAsString());
        assertThat(listJson.path("code").asInt()).isZero();
        assertThat(listJson.path("data").findValuesAsText("title")).contains(title);
    }

    @Test
    void shouldSupportEmptyExpiredAtAndKeepDescendingOrder() throws Exception {
        String token = login("seller", "123456");
        String suffix = String.valueOf(System.currentTimeMillis());
        String firstTitle = "公告空时间测试A" + suffix;
        String secondTitle = "公告空时间测试B" + suffix;

        mockMvc.perform(post("/api/bulletins")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + firstTitle + "\",\"content\":\"第一条公告\",\"category\":\"NOTICE\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/bulletins")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + secondTitle + "\",\"content\":\"第二条公告\",\"category\":\"NOTICE\"}"))
                .andExpect(status().isOk());

        MvcResult listResult = mockMvc.perform(get("/api/bulletins")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode bulletins = objectMapper.readTree(listResult.getResponse().getContentAsString()).path("data");
        assertThat(bulletins.isArray()).isTrue();

        int firstIndex = indexOfTitle(bulletins, firstTitle);
        int secondIndex = indexOfTitle(bulletins, secondTitle);
        assertThat(firstIndex).isNotNegative();
        assertThat(secondIndex).isNotNegative();
        assertThat(secondIndex).isLessThan(firstIndex);

        JsonNode latest = bulletins.get(secondIndex);
        assertThat(latest.path("createdBy").asText()).isEqualTo("seller");
        assertThat(latest.path("expiredAt").asText()).isEmpty();
        assertThat(latest.path("createdAt").asText()).isNotBlank();
        assertThat(latest.path("category").asText()).isEqualTo("NOTICE");
    }

    private int indexOfTitle(JsonNode bulletins, String title) {
        for (int i = 0; i < bulletins.size(); i++) {
            if (title.equals(bulletins.get(i).path("title").asText())) {
                return i;
            }
        }
        return -1;
    }

    private String login(String username, String password) throws Exception {
        normalizeCompatibilitySchema();
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        assertThat(loginJson.path("code").asInt()).isZero();
        return loginJson.path("data").path("token").asText();
    }


    private void normalizeCompatibilitySchema() {
        dropColumnIfExists("user_sessions", "session_token");
        dropColumnIfExists("user_sessions", "expire_at");
        dropColumnIfExists("user_sessions", "username");
        dropColumnIfExists("user_sessions", "role_code");
        dropColumnIfExists("user_sessions", "last_access_at");
    }

    private void dropColumnIfExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_schema = database() and table_name = ? and column_name = ?",
                Integer.class,
                tableName,
                columnName);
        if (count != null && count > 0) {
            jdbcTemplate.execute("alter table " + tableName + " drop column " + columnName);
        }
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
