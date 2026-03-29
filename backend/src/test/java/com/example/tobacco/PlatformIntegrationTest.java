package com.example.tobacco;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlatformIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRunTaskbookAlignedBusinessFlow() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.path("data").path("token").asText();
        assertThat(token).isNotBlank();
        assertThat(loginJson.path("data").path("permissions").isArray()).isTrue();

        mockMvc.perform(get("/api/auth/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        MvcResult dashboardResult = mockMvc.perform(get("/api/dashboard/summary").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode dashboardJson = objectMapper.readTree(dashboardResult.getResponse().getContentAsString()).path("data");
        assertThat(dashboardJson.path("purchaseCount").asInt()).isGreaterThanOrEqualTo(0);
        assertThat(dashboardJson.path("modules").isArray()).isTrue();
        mockMvc.perform(get("/api/permissions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        MvcResult configsResult = mockMvc.perform(get("/api/configs").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(configsResult.getResponse().getContentAsString()).path("data").isArray()).isTrue();
        String suffix = String.valueOf(System.currentTimeMillis());
        String permissionCode = "tmpperm:" + suffix;
        String roleCode = "TMP_ROLE_" + suffix;
        String username = "tmpuser" + suffix;
        mockMvc.perform(post("/api/permissions").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + permissionCode + "\",\"name\":\"临时权限\",\"module\":\"TEST\",\"remark\":\"迁移测试\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/roles").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + roleCode + "\",\"name\":\"临时角色\",\"remark\":\"迁移测试\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/roles/" + roleCode).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"临时角色更新\",\"remark\":\"迁移测试更新\",\"permissions\":[\"dashboard:view\",\"" + permissionCode + "\"]}"))
                .andExpect(status().isOk());
        MvcResult rolesResult = mockMvc.perform(get("/api/roles").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(rolesResult.getResponse().getContentAsString()).contains(roleCode).contains(permissionCode);
        mockMvc.perform(post("/api/users").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\",\"realName\":\"迁移用户\",\"roleCode\":\"" + roleCode + "\",\"status\":\"ENABLED\",\"scopeType\":\"WAREHOUSE\",\"scopeValue\":\"ALL\"}"))
                .andExpect(status().isOk());
        MvcResult usersResult = mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode usersJson = objectMapper.readTree(usersResult.getResponse().getContentAsString()).path("data");
        Long createdUserId = null;
        for (JsonNode node : usersJson) {
            if (username.equals(node.path("username").asText())) {
                createdUserId = node.path("id").asLong();
                break;
            }
        }
        assertThat(createdUserId).isNotNull();
        MvcResult userDetailResult = mockMvc.perform(get("/api/users/" + createdUserId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode userDetailJson = objectMapper.readTree(userDetailResult.getResponse().getContentAsString()).path("data");
        assertThat(userDetailJson.path("roleCode").asText()).isEqualTo(roleCode);
        mockMvc.perform(put("/api/users/" + createdUserId).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"迁移用户更新\",\"status\":\"DISABLED\",\"roleCode\":\"SELLER\",\"scopeType\":\"WAREHOUSE\",\"scopeValue\":\"ALL\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/users/" + createdUserId + "/status").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ENABLED\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/users/" + createdUserId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/logs/login").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/messages").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/purchase-requisitions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/purchases/3/audit").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVED\",\"remark\":\"测试审核\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/purchases/3/receive").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/purchases/3/inbound").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/purchases/3/trace").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/sales/2/audit").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVED\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/sales/2/outbound").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/sales/2/payment").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":1000,\"payerName\":\"测试客户\",\"remark\":\"测试回款\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/sales/receivables").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/inventories").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/inventory-records").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/inventory-warnings").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/inventory-checks").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":30,\"remark\":\"测试盘点\"}"))
                .andExpect(status().isOk());

        MvcResult exportResult = mockMvc.perform(get("/api/reports/export").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(exportResult.getResponse().getContentType()).contains("spreadsheetml.sheet");
        assertThat(exportResult.getResponse().getContentAsByteArray().length).isGreaterThan(0);

        MvcResult psiResult = mockMvc.perform(get("/api/reports/psi-summary").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode psiJson = objectMapper.readTree(psiResult.getResponse().getContentAsString()).path("data");
        assertThat(psiJson.path("purchase").path("label").asText()).isEqualTo("采购汇总");
        mockMvc.perform(get("/api/reports/compliance-trace").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/reports/abnormal-docs").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        MvcResult linkageResult = mockMvc.perform(get("/api/reports/linkage").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode linkageJson = objectMapper.readTree(linkageResult.getResponse().getContentAsString()).path("data");
        assertThat(linkageJson.path("categoryPurchaseSales").isArray()).isTrue();

        mockMvc.perform(put("/api/configs/inventory.warning.threshold.default").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"configValue\":\"18\",\"remark\":\"测试更新\"}"))
                .andExpect(status().isOk());
    }
}
