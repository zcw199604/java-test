# API 手册

## 概述
当前接口以 `backend/src/main/java/**/Controller.java` 为准，覆盖认证、系统管理、基础资料、采购、销售、库存、驾驶舱、报表、日志、消息与追溯能力。

## 认证与响应约定
- 登录接口：`POST /api/auth/login`
- 鉴权方式：`Authorization: Bearer <token>`
- 开放接口：`/api/auth/login`、`/api/auth/captcha`、`/api/auth/forgot-password`、`/api/auth/reset-password`、`/api/health`
- 响应格式：除下载接口外，统一返回 `ApiResponse { code, message, data }`
- 文件下载：`GET /api/reports/export` 直接返回 Excel 文件流

---

## 接口清单

### 健康检查
- `GET /api/health`

### 认证
- `GET /api/auth/captcha`
- `POST /api/auth/login`
- `GET /api/auth/profile`
- `POST /api/auth/logout`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`

### 系统管理
- `GET /api/users`
- `GET /api/users/{id}`
- `POST /api/users`
- `PUT /api/users/{id}`
- `PUT /api/users/{id}/status`
- `DELETE /api/users/{id}`
- `GET /api/roles`
- `POST /api/roles`
- `PUT /api/roles/{code}`
- `GET /api/permissions`
- `POST /api/permissions`
- `GET /api/configs`
- `PUT /api/configs/{key}`
- `GET /api/profile`
- `PUT /api/profile`
- `POST /api/profile/password`
- `GET /api/warehouses`
  - 支持参数：`keyword`、`status`
- `GET /api/warehouses/{id}`
- `POST /api/warehouses`
- `PUT /api/warehouses/{id}`
- `PUT /api/warehouses/{id}/status`

### 日志与消息
- `GET /api/logs/login`
- `GET /api/logs/operation`
- `GET /api/messages`
- `POST /api/messages/{id}/read`

### 基础资料
- `GET /api/categories`
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `GET /api/products`
  - 支持参数：`keyword`、`status`、`category`
- `GET /api/products/{id}`
- `POST /api/products`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`
- `GET /api/suppliers`
  - 支持参数：`keyword`、`status`
- `POST /api/suppliers`
- `PUT /api/suppliers/{id}`
- `DELETE /api/suppliers/{id}`
- `GET /api/customers`
  - 支持参数：`keyword`、`status`
- `GET /api/customers/{id}`
- `POST /api/customers`
- `PUT /api/customers/{id}`
- `DELETE /api/customers/{id}`

### 采购
- `GET /api/purchase-requisitions`
  - 兼容接口，当前直接复用采购单列表，不对应独立数据表
- `GET /api/purchases`
- `GET /api/purchases/requisitions`
  - 与 `GET /api/purchase-requisitions` 一致，当前均返回采购单列表
- `GET /api/purchases/{id}`
  - 供采购单详情页 / 编辑页复用，拥有查看权限的角色可访问
- `GET /api/purchases/{id}/trace`
- `POST /api/purchases`
- `PUT /api/purchases/{id}`
- `POST /api/purchases/{id}/audit`
- `POST /api/purchases/{id}/cancel`
- `POST /api/purchases/{id}/receive`
- `POST /api/purchases/{id}/inbound`
  - 请求体需包含 `warehouseId`，可选 `remark`
- `POST /api/purchases/import`

### 销售
- `GET /api/bulletins`
- `POST /api/bulletins`
- `GET /api/sales`
- `GET /api/sales/{id}`
  - 供销售单详情页 / 编辑页复用，拥有查看权限的角色可访问
- `GET /api/sales/{id}/trace`
  - 返回创建、审核、出库、回款等流程节点时间线，供销售单详情页展示
- `POST /api/sales`
- `PUT /api/sales/{id}`
- `POST /api/sales/{id}/audit`
- `POST /api/sales/{id}/cancel`
- `POST /api/sales/{id}/outbound`
  - 请求体需包含 `warehouseId`，可选 `remark`
- `POST /api/sales/{id}/payment`
- `GET /api/sales/statistics`
- `GET /api/sales/receivables`
- `POST /api/sales/import`

### 库存
- `GET /api/inventories`
  - 支持参数：`warehouseId`、`keyword`、`status`
- `GET /api/inventory-records`
  - 支持参数：`warehouseId`、`bizType`、`keyword`
- `GET /api/inventory-warnings`
  - 支持参数：`warehouseId`
- `POST /api/inventory-transfers`
  - 请求体字段：`productId`、`fromWarehouseId`、`toWarehouseId`、`quantity`、`remark`
- `POST /api/inventory-checks`
  - 请求体字段：`productId`、`warehouseId`、`quantity`、`remark`
- `POST /api/inventories/import`

### 驾驶舱与报表
- `GET /api/dashboard/summary`
- `GET /api/dashboard/sales-history`
  - 支持参数：`metric=quantity|amount`、`days=7|30`、`limit`
- `GET /api/reports/purchase-summary`
- `GET /api/reports/sales-summary`
- `GET /api/reports/inventory-summary`
- `GET /api/reports/trend`
- `GET /api/reports/psi-summary`
- `GET /api/reports/compliance-trace`
  - 支持参数：`keyword`、`warehouseId`、`bizType`、`nodeCode`
- `GET /api/reports/abnormal-docs`
- `POST /api/reports/abnormal-docs/{id}/audit`
- `GET /api/reports/linkage`
- `GET /api/reports/export`
  - 返回 `report-summary.xlsx`

---

## 当前实现说明
- 采购、销售、库存导入接口均限制单文件 `<= 5MB`、单次 `<= 1000` 行
- 审核、到货、入库、出库、调拨、盘点等关键动作会写入操作日志与追溯记录
- `GET /api/reports/compliance-trace` 当前会统一聚合 `trace_records` 与 `inventory_records`，同时提供业务节点与库存变化视角
- 当前不存在 `GET /api/purchases/export`、`GET /api/sales/export`、`GET /api/inventories/export`、`GET /api/inventory-warnings/history`、`GET /api/purchases/analysis` 等运行时接口
