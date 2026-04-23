package com.example.tobacco;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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

        mockMvc.perform(get("/api/auth/profile").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        MvcResult dashboardResult = mockMvc.perform(get("/api/dashboard/summary").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode dashboardJson = objectMapper.readTree(dashboardResult.getResponse().getContentAsString()).path("data");
        assertThat(dashboardJson.path("purchaseCount").asInt()).isGreaterThanOrEqualTo(0);
        assertThat(dashboardJson.path("modules").isArray()).isTrue();
        mockMvc.perform(get("/api/permissions").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        MvcResult configsResult = mockMvc.perform(get("/api/configs").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(configsResult.getResponse().getContentAsString()).path("data").isArray()).isTrue();

        String suffix = String.valueOf(System.currentTimeMillis());
        String permissionCode = "tmpperm:" + suffix;
        String roleCode = "TMP_ROLE_" + suffix;
        String username = "tmpuser" + suffix;
        mockMvc.perform(post("/api/permissions").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + permissionCode + "\",\"name\":\"临时权限\",\"module\":\"TEST\",\"remark\":\"迁移测试\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/roles").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + roleCode + "\",\"name\":\"临时角色\",\"remark\":\"迁移测试\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/roles/" + roleCode).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"临时角色更新\",\"remark\":\"迁移测试更新\",\"permissions\":[\"dashboard:view\",\"" + permissionCode + "\"]}"))
                .andExpect(status().isOk());
        MvcResult rolesResult = mockMvc.perform(get("/api/roles").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(rolesResult.getResponse().getContentAsString()).contains(roleCode).contains(permissionCode);

        mockMvc.perform(post("/api/users").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\",\"realName\":\"迁移用户\",\"roleCode\":\"" + roleCode + "\",\"status\":\"ENABLED\",\"scopeType\":\"WAREHOUSE\",\"scopeValue\":\"ALL\"}"))
                .andExpect(status().isOk());
        MvcResult usersResult = mockMvc.perform(get("/api/users").header("Authorization", bearer(token)))
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
        MvcResult userDetailResult = mockMvc.perform(get("/api/users/" + createdUserId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode userDetailJson = objectMapper.readTree(userDetailResult.getResponse().getContentAsString()).path("data");
        assertThat(userDetailJson.path("roleCode").asText()).isEqualTo(roleCode);
        mockMvc.perform(put("/api/users/" + createdUserId).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"迁移用户更新\",\"status\":\"DISABLED\",\"roleCode\":\"SELLER\",\"scopeType\":\"WAREHOUSE\",\"scopeValue\":\"ALL\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/users/" + createdUserId + "/status").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ENABLED\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/users/" + createdUserId).header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/logs/login").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/messages").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/purchase-requisitions").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/purchases/3/audit").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVED\",\"remark\":\"测试审核\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/purchases/3/receive").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        MvcResult purchaseInboundResult = mockMvc.perform(post("/api/purchases/3/inbound").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseId\":2,\"remark\":\"测试采购入库\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode purchaseInboundJson = objectMapper.readTree(purchaseInboundResult.getResponse().getContentAsString()).path("data");
        assertThat(purchaseInboundJson.path("status").asText()).isEqualTo("INBOUND");
        assertThat(purchaseInboundJson.path("warehouseId").asLong()).isEqualTo(2L);
        mockMvc.perform(get("/api/purchases/3/trace").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/sales/2/audit").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVED\"}"))
                .andExpect(status().isOk());
        MvcResult salesOutboundResult = mockMvc.perform(post("/api/sales/2/outbound").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseId\":2,\"remark\":\"测试销售出库\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode salesOutboundJson = objectMapper.readTree(salesOutboundResult.getResponse().getContentAsString()).path("data");
        assertThat(salesOutboundJson.path("status").asText()).isEqualTo("OUTBOUND");
        assertThat(salesOutboundJson.path("warehouseId").asLong()).isEqualTo(2L);
        mockMvc.perform(post("/api/sales/2/payment").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":1000,\"payerName\":\"测试客户\",\"remark\":\"测试回款\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/sales/receivables").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        MvcResult transferResult = mockMvc.perform(post("/api/inventory-transfers").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"fromWarehouseId\":2,\"toWarehouseId\":3,\"quantity\":2,\"remark\":\"测试仓库调拨\"}"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(transferResult.getResponse().getContentAsString()).path("data").asText()).contains("调拨完成");

        MvcResult inventoriesResult = mockMvc.perform(get("/api/inventories").header("Authorization", bearer(token))
                        .param("warehouseId", "2"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(inventoriesResult.getResponse().getContentAsString()).contains("东区仓");
        MvcResult recordsResult = mockMvc.perform(get("/api/inventory-records").header("Authorization", bearer(token))
                        .param("warehouseId", "2"))
                .andExpect(status().isOk())
                .andReturn();
        String recordsBody = recordsResult.getResponse().getContentAsString();
        assertThat(recordsBody).contains("PURCHASE_INBOUND", "SALES_OUTBOUND", "TRANSFER_OUT");
        mockMvc.perform(get("/api/inventory-warnings").header("Authorization", bearer(token))
                        .param("warehouseId", "2"))
                .andExpect(status().isOk());
        MvcResult inventoryCheckResult = mockMvc.perform(post("/api/inventory-checks").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"warehouseId\":3,\"quantity\":3,\"remark\":\"测试盘点\"}"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(inventoryCheckResult.getResponse().getContentAsString()).path("data").asText()).isEqualTo("盘点结果已更新");

        MvcResult filteredTraceResult = mockMvc.perform(get("/api/reports/compliance-trace").header("Authorization", bearer(token))
                        .param("warehouseId", "2")
                        .param("bizType", "INVENTORY")
                        .param("keyword", "东区仓"))
                .andExpect(status().isOk())
                .andReturn();
        String filteredTraceBody = filteredTraceResult.getResponse().getContentAsString();
        assertThat(filteredTraceBody).contains("TRANSFER_OUT", "PURCHASE_INBOUND", "SALES_OUTBOUND");

        MvcResult checkTraceResult = mockMvc.perform(get("/api/reports/compliance-trace").header("Authorization", bearer(token))
                        .param("bizType", "INVENTORY")
                        .param("nodeCode", "CHECK"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(checkTraceResult.getResponse().getContentAsString()).contains("库存盘点", "测试盘点");

        MvcResult exportResult = mockMvc.perform(get("/api/reports/export").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(exportResult.getResponse().getContentType()).contains("spreadsheetml.sheet");
        assertThat(exportResult.getResponse().getContentAsByteArray().length).isGreaterThan(0);

        MvcResult psiResult = mockMvc.perform(get("/api/reports/psi-summary").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode psiJson = objectMapper.readTree(psiResult.getResponse().getContentAsString()).path("data");
        assertThat(psiJson.path("purchase").path("label").asText()).isEqualTo("采购汇总");
        mockMvc.perform(get("/api/reports/compliance-trace").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/reports/abnormal-docs").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        MvcResult linkageResult = mockMvc.perform(get("/api/reports/linkage").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode linkageJson = objectMapper.readTree(linkageResult.getResponse().getContentAsString()).path("data");
        assertThat(linkageJson.path("categoryPurchaseSales").isArray()).isTrue();

        mockMvc.perform(put("/api/configs/inventory.warning.threshold.default").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"configValue\":\"18\",\"remark\":\"测试更新\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectWarehouseRequiredActionsWhenWarehouseIsMissing() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(post("/api/purchases/2/inbound")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(objectMapper.readTree(result.getResponse().getContentAsString()).path("code").asInt()).isEqualTo(400));

        mockMvc.perform(post("/api/sales/2/outbound")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(objectMapper.readTree(result.getResponse().getContentAsString()).path("code").asInt()).isEqualTo(400));

        mockMvc.perform(post("/api/inventory-checks")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":10,\"remark\":\"缺少仓库参数\"}"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(objectMapper.readTree(result.getResponse().getContentAsString()).path("code").asInt()).isEqualTo(400));
    }

    @Test
    void shouldReturnDashboardSalesHistoryWithMetricSwitching() throws Exception {
        String token = loginAsAdmin();

        MvcResult quantityResult = mockMvc.perform(get("/api/dashboard/sales-history")
                        .header("Authorization", bearer(token))
                        .param("metric", "quantity")
                        .param("days", "7")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode quantityJson = objectMapper.readTree(quantityResult.getResponse().getContentAsString()).path("data");
        assertThat(quantityJson.path("metric").asText()).isEqualTo("quantity");
        assertThat(quantityJson.path("periods").isArray()).isTrue();
        assertThat(quantityJson.path("periods").size()).isEqualTo(7);
        assertThat(quantityJson.path("series").isArray()).isTrue();
        assertThat(quantityJson.path("series").size()).isGreaterThanOrEqualTo(1);
        assertThat(quantityJson.path("series").get(0).path("productName").asText()).isNotBlank();
        assertThat(quantityJson.path("series").get(0).path("values").size()).isEqualTo(7);

        MvcResult amountResult = mockMvc.perform(get("/api/dashboard/sales-history")
                        .header("Authorization", bearer(token))
                        .param("metric", "amount")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode amountJson = objectMapper.readTree(amountResult.getResponse().getContentAsString()).path("data");
        assertThat(amountJson.path("metric").asText()).isEqualTo("amount");
        assertThat(amountJson.path("periods").size()).isEqualTo(30);
        assertThat(amountJson.path("series").isArray()).isTrue();
    }

    private String loginAsAdmin() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        assertThat(loginJson.path("code").asInt()).isZero();
        return loginJson.path("data").path("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
