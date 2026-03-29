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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldSupportForgotResetPasswordAndSessionProfileFlow() throws Exception {
        String adminToken = login("admin", "123456");
        String suffix = String.valueOf(System.currentTimeMillis());
        String username = "auth_user_" + suffix;
        String password = "Init@123";
        String newPassword = "Reset@456";

        mockMvc.perform(post("/api/users")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"realName\":\"认证测试用户\",\"roleCode\":\"KEEPER\",\"status\":\"ENABLED\"}"))
                .andExpect(status().isOk());

        MvcResult captchaResult = mockMvc.perform(get("/api/auth/captcha"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode captcha = objectMapper.readTree(captchaResult.getResponse().getContentAsString()).path("data");

        MvcResult forgotResult = mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"uuid\":\"" + captcha.path("uuid").asText() + "\",\"code\":\"" + captcha.path("code").asText() + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode forgotJson = objectMapper.readTree(forgotResult.getResponse().getContentAsString());
        assertThat(forgotJson.path("code").asInt()).isZero();
        String resetToken = forgotJson.path("data").path("token").asText();
        assertThat(resetToken).isNotBlank();
        assertThat(resetToken).isEqualTo(forgotJson.path("data").path("resetToken").asText());

        MvcResult resetResult = mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"token\":\"" + resetToken + "\",\"newPassword\":\"" + newPassword + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(resetResult.getResponse().getContentAsString()).path("code").asInt()).isZero();

        String userToken = login(username, newPassword);
        MvcResult profileResult = mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode profileJson = objectMapper.readTree(profileResult.getResponse().getContentAsString());
        assertThat(profileJson.path("code").asInt()).isZero();
        assertThat(profileJson.path("data").path("username").asText()).isEqualTo(username);
        assertThat(profileJson.path("data").path("roleCode").asText()).isEqualTo("KEEPER");
        assertThat(profileJson.path("data").path("permissions").isArray()).isTrue();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk());

        MvcResult expiredProfileResult = mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isUnauthorized())
                .andReturn();
        JsonNode expiredProfileJson = objectMapper.readTree(expiredProfileResult.getResponse().getContentAsString());
        assertThat(expiredProfileJson.path("code").asInt()).isEqualTo(401);
    }

    @Test
    void shouldRejectInvalidCaptchaAndDisabledAccountLogin() throws Exception {
        MvcResult captchaResult = mockMvc.perform(get("/api/auth/captcha"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode captcha = objectMapper.readTree(captchaResult.getResponse().getContentAsString()).path("data");

        MvcResult invalidCaptchaLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123456\",\"captchaKey\":\"" + captcha.path("captchaKey").asText() + "\",\"captchaCode\":\"0000\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode invalidCaptchaLoginJson = objectMapper.readTree(invalidCaptchaLoginResult.getResponse().getContentAsString());
        assertThat(invalidCaptchaLoginJson.path("code").asInt()).isEqualTo(400);
        assertThat(invalidCaptchaLoginJson.path("message").asText()).isNotBlank();

        String adminToken = login("admin", "123456");
        MvcResult userListResult = mockMvc.perform(get("/api/users")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode users = objectMapper.readTree(userListResult.getResponse().getContentAsString()).path("data");
        long managerId = 0L;
        for (JsonNode user : users) {
            if ("manager".equals(user.path("username").asText())) {
                managerId = user.path("id").asLong();
                break;
            }
        }
        assertThat(managerId).isPositive();

        mockMvc.perform(put("/api/users/" + managerId + "/status")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk());

        MvcResult disabledLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"manager\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode disabledLoginJson = objectMapper.readTree(disabledLoginResult.getResponse().getContentAsString());
        assertThat(disabledLoginJson.path("code").asInt()).isEqualTo(400);
        assertThat(disabledLoginJson.path("message").asText()).isEqualTo("当前账号已禁用");
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
        dropColumnIfExists("captcha_records", "captcha_key");
        dropColumnIfExists("captcha_records", "captcha_code");
        dropColumnIfExists("captcha_records", "expire_at");
        dropColumnIfExists("user_sessions", "session_token");
        dropColumnIfExists("user_sessions", "expire_at");
        dropColumnIfExists("user_sessions", "username");
        dropColumnIfExists("user_sessions", "role_code");
        dropColumnIfExists("user_sessions", "last_access_at");
        dropColumnIfExists("password_reset_records", "reset_token");
        dropColumnIfExists("password_reset_records", "expire_at");
        dropColumnIfExists("password_reset_records", "username");
        dropColumnIfExists("password_reset_records", "used_at");
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
