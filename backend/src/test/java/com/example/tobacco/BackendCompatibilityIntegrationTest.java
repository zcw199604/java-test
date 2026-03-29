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
class BackendCompatibilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSupportCaptchaCompatibilityFieldsOnLogin() throws Exception {
        MvcResult captchaResult = mockMvc.perform(get("/api/auth/captcha"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode captchaJson = objectMapper.readTree(captchaResult.getResponse().getContentAsString()).path("data");
        String captchaKey = captchaJson.path("captchaKey").asText();
        String captchaCode = captchaJson.path("captchaCode").asText();
        assertThat(captchaKey).isEqualTo(captchaJson.path("uuid").asText());
        assertThat(captchaCode).isEqualTo(captchaJson.path("code").asText());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123456\",\"uuid\":\"" + captchaKey + "\",\"code\":\"" + captchaCode + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        assertThat(loginJson.path("code").asInt()).isZero();
        assertThat(loginJson.path("data").path("token").asText()).isNotBlank();
    }

    @Test
    void shouldRecordLoginLogoutOperationLogsAndKeepMessageReadFlow() throws Exception {
        String token = loginAsAdmin();
        mockMvc.perform(post("/api/auth/logout").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        String verifyToken = loginAsAdmin();
        MvcResult loginLogsResult = mockMvc.perform(get("/api/logs/login")
                        .header("Authorization", bearer(verifyToken)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode loginLogs = objectMapper.readTree(loginLogsResult.getResponse().getContentAsString()).path("data");
        assertThat(loginLogs.isArray()).isTrue();
        assertThat(loginLogs.findValuesAsText("status")).contains("SUCCESS");

        MvcResult operationLogsResult = mockMvc.perform(get("/api/logs/operation")
                        .header("Authorization", bearer(verifyToken)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode operationLogs = objectMapper.readTree(operationLogsResult.getResponse().getContentAsString()).path("data");
        assertThat(operationLogs.isArray()).isTrue();
        assertThat(operationLogs.findValuesAsText("action")).contains("LOGIN", "LOGOUT");

        MvcResult messagesResult = mockMvc.perform(get("/api/messages")
                        .header("Authorization", bearer(verifyToken)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode messages = objectMapper.readTree(messagesResult.getResponse().getContentAsString()).path("data");
        assertThat(messages.isArray()).isTrue();
        if (messages.size() > 0) {
            Long messageId = messages.get(0).path("id").asLong();
            mockMvc.perform(post("/api/messages/" + messageId + "/read")
                            .header("Authorization", bearer(verifyToken)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void shouldSupportLowRiskModulesAfterMyBatisAnnotationMigration() throws Exception {
        String token = loginAsAdmin();
        String suffix = String.valueOf(System.currentTimeMillis());

        mockMvc.perform(post("/api/suppliers")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"注解供应商" + suffix + "\",\"contactName\":\"供应商联系人\",\"contactPhone\":\"13800001111\",\"address\":\"供应商地址\"}"))
                .andExpect(status().isOk());
        MvcResult supplierListResult = mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", bearer(token))
                        .param("keyword", "注解供应商" + suffix)
                        .param("status", "ENABLED"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode suppliers = objectMapper.readTree(supplierListResult.getResponse().getContentAsString()).path("data");
        assertThat(suppliers.isArray()).isTrue();
        Long supplierId = suppliers.get(0).path("id").asLong();
        mockMvc.perform(put("/api/suppliers/" + supplierId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"注解供应商更新" + suffix + "\",\"contactName\":\"新联系人\",\"contactPhone\":\"13800002222\",\"address\":\"新地址\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/suppliers/" + supplierId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        MvcResult disabledSupplierListResult = mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", bearer(token))
                        .param("keyword", "注解供应商更新" + suffix)
                        .param("status", "DISABLED"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(disabledSupplierListResult.getResponse().getContentAsString()).path("data").isArray()).isTrue();

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"注解品类" + suffix + "\",\"remark\":\"测试品类\"}"))
                .andExpect(status().isOk());
        MvcResult categoryListResult = mockMvc.perform(get("/api/categories")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode categories = objectMapper.readTree(categoryListResult.getResponse().getContentAsString()).path("data");
        assertThat(categories.findValuesAsText("name")).contains("注解品类" + suffix);

        String productCode = "MB" + suffix;
        mockMvc.perform(post("/api/products")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + productCode + "\",\"name\":\"注解商品" + suffix + "\",\"category\":\"注解品类" + suffix + "\",\"unit\":\"条\",\"unitPrice\":88.5,\"warningThreshold\":6}"))
                .andExpect(status().isOk());
        MvcResult productListResult = mockMvc.perform(get("/api/products")
                        .header("Authorization", bearer(token))
                        .param("keyword", productCode)
                        .param("status", "ENABLED")
                        .param("category", "注解品类" + suffix))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode products = objectMapper.readTree(productListResult.getResponse().getContentAsString()).path("data");
        assertThat(products.isArray()).isTrue();
        Long productId = products.get(0).path("id").asLong();
        MvcResult productDetailResult = mockMvc.perform(get("/api/products/" + productId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(productDetailResult.getResponse().getContentAsString()).path("data").path("code").asText()).isEqualTo(productCode);
        mockMvc.perform(delete("/api/products/" + productId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/customers")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"迁移客户" + suffix + "\",\"contactName\":\"客户联系人\",\"contactPhone\":\"13900009999\",\"address\":\"测试地址\",\"status\":\"ENABLED\"}"))
                .andExpect(status().isOk());
        MvcResult customerListResult = mockMvc.perform(get("/api/customers")
                        .header("Authorization", bearer(token))
                        .param("keyword", "迁移客户" + suffix)
                        .param("status", "ENABLED"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode customerList = objectMapper.readTree(customerListResult.getResponse().getContentAsString()).path("data");
        assertThat(customerList.isArray()).isTrue();
        Long customerId = customerList.get(0).path("id").asLong();
        MvcResult customerDetailResult = mockMvc.perform(get("/api/customers/" + customerId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(customerDetailResult.getResponse().getContentAsString()).path("data").path("name").asText()).isEqualTo("迁移客户" + suffix);
    }

    @Test
    void shouldSupportWarehouseCustomerPurchaseAndSalesDetailUpdateFlow() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(post("/api/warehouses")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"集成测试仓\",\"address\":\"一号库位\",\"status\":\"ENABLED\"}"))
                .andExpect(status().isOk());

        MvcResult warehouseListResult = mockMvc.perform(get("/api/warehouses")
                        .header("Authorization", bearer(token))
                        .param("keyword", "集成测试仓"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode warehouseList = objectMapper.readTree(warehouseListResult.getResponse().getContentAsString()).path("data");
        assertThat(warehouseList.isArray()).isTrue();
        Long warehouseId = warehouseList.get(0).path("id").asLong();

        mockMvc.perform(put("/api/warehouses/" + warehouseId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\":\"二号库位\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/warehouses/" + warehouseId + "/status")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk());
        MvcResult warehouseDetailResult = mockMvc.perform(get("/api/warehouses/" + warehouseId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode warehouseDetail = objectMapper.readTree(warehouseDetailResult.getResponse().getContentAsString()).path("data");
        assertThat(warehouseDetail.path("address").asText()).isEqualTo("二号库位");
        assertThat(warehouseDetail.path("status").asText()).isEqualTo("DISABLED");

        mockMvc.perform(post("/api/customers")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"集成客户\",\"contactName\":\"测试联系人\",\"contactPhone\":\"13900009999\",\"address\":\"测试地址\",\"status\":\"ENABLED\"}"))
                .andExpect(status().isOk());
        MvcResult customerListResult = mockMvc.perform(get("/api/customers")
                        .header("Authorization", bearer(token))
                        .param("keyword", "集成客户")
                        .param("status", "ENABLED"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode customerList = objectMapper.readTree(customerListResult.getResponse().getContentAsString()).path("data");
        assertThat(customerList.isArray()).isTrue();
        Long customerId = customerList.get(0).path("id").asLong();
        MvcResult customerDetailResult = mockMvc.perform(get("/api/customers/" + customerId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(customerDetailResult.getResponse().getContentAsString()).path("data").path("name").asText()).isEqualTo("集成客户");

        mockMvc.perform(post("/api/purchases/3/audit")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"REJECTED\",\"remark\":\"退回修改\"}"))
                .andExpect(status().isOk());
        MvcResult purchaseUpdateResult = mockMvc.perform(put("/api/purchases/3")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"supplierId\":1,\"productId\":2,\"quantity\":18,\"unitPrice\":205}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode purchaseUpdateJson = objectMapper.readTree(purchaseUpdateResult.getResponse().getContentAsString()).path("data");
        assertThat(purchaseUpdateJson.path("status").asText()).isEqualTo("CREATED");
        assertThat(purchaseUpdateJson.path("totalAmount").decimalValue().toPlainString()).isEqualTo("3690.00");

        mockMvc.perform(post("/api/purchases/3/audit")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVED\",\"remark\":\"重新审核通过\"}"))
                .andExpect(status().isOk());
        MvcResult purchaseBlockedResult = mockMvc.perform(put("/api/purchases/3")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"supplierId\":1,\"productId\":2,\"quantity\":20,\"unitPrice\":205}"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(purchaseBlockedResult.getResponse().getContentAsString()).path("code").asInt()).isEqualTo(400);

        mockMvc.perform(post("/api/sales/2/audit")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"REJECTED\",\"remark\":\"退回修改\"}"))
                .andExpect(status().isOk());
        MvcResult salesUpdateResult = mockMvc.perform(put("/api/sales/2")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":2,\"productId\":1,\"quantity\":8,\"unitPrice\":465}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode salesUpdateJson = objectMapper.readTree(salesUpdateResult.getResponse().getContentAsString()).path("data");
        assertThat(salesUpdateJson.path("status").asText()).isEqualTo("CREATED");
        assertThat(salesUpdateJson.path("totalAmount").decimalValue().toPlainString()).isEqualTo("3720.00");

        MvcResult salesDetailResult = mockMvc.perform(get("/api/sales/2")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(salesDetailResult.getResponse().getContentAsString()).path("data").path("quantity").asInt()).isEqualTo(8);

        mockMvc.perform(post("/api/sales/2/audit")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVED\",\"remark\":\"审核通过\"}"))
                .andExpect(status().isOk());
        MvcResult salesBlockedResult = mockMvc.perform(put("/api/sales/2")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":2,\"productId\":1,\"quantity\":9,\"unitPrice\":465}"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(salesBlockedResult.getResponse().getContentAsString()).path("code").asInt()).isEqualTo(400);
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
