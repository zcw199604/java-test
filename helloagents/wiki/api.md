# API 手册

## 概述
当前系统已形成面向任务书的后端接口集合，并配套企业后台工作台风格前端页面，覆盖认证、系统管理、基础资料、采购、销售、库存、报表、日志、消息与追溯等模块。

## 认证方式
- 登录接口：`POST /api/auth/login`
- 鉴权方式：`Authorization: Bearer <token>`
- 当前登录态：`GET /api/auth/profile`
- 辅助接口：`GET /api/auth/captcha`、`POST /api/auth/forgot-password`、`POST /api/auth/reset-password`

---

## 核心接口

### 认证与系统
- `GET /api/auth/captcha`
- `POST /api/auth/login`
- `GET /api/auth/profile`
- `POST /api/auth/logout`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `GET /api/users`
- `GET /api/users/{id}`
- `POST /api/users`
- `PUT /api/users/{id}`
- `PUT /api/users/{id}/status`
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
- `POST /api/warehouses`

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
- `GET /api/products/{id}`
- `POST /api/products`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`
- `GET /api/suppliers`
- `POST /api/suppliers`
- `PUT /api/suppliers/{id}`
- `DELETE /api/suppliers/{id}`
- `GET /api/customers`
- `POST /api/customers`
- `PUT /api/customers/{id}`
- `DELETE /api/customers/{id}`

### 采购管理
- `GET /api/purchase-requisitions`
- `POST /api/purchase-requisitions`
- `GET /api/purchases`
- `POST /api/purchases`
- `PUT /api/purchases/{id}`
- `POST /api/purchases/{id}/audit`
- `POST /api/purchases/{id}/cancel`
- `POST /api/purchases/{id}/receive`
- `POST /api/purchases/{id}/inbound`
- `GET /api/purchases/{id}/trace`
- `GET /api/purchases/analysis`
- `GET /api/purchases/export`
- `POST /api/purchases/import`

### 销售管理
- `GET /api/bulletins`
- `POST /api/bulletins`
- `GET /api/sales`
- `POST /api/sales`
- `PUT /api/sales/{id}`
- `POST /api/sales/{id}/audit`
- `POST /api/sales/{id}/cancel`
- `POST /api/sales/{id}/outbound`
- `POST /api/sales/{id}/payment`
- `GET /api/sales/receivables`
- `GET /api/sales/statistics`
- `GET /api/sales/export`
- `POST /api/sales/import`

### 库存管理
- `GET /api/inventories`
- `GET /api/inventories/export`
- `POST /api/inventories/import`
- `GET /api/inventory-records`
- `GET /api/inventory-warnings`
- `GET /api/inventory-warnings/history`
- `POST /api/inventory-transfers`
- `POST /api/inventory-checks`

### 报表与追溯
- `GET /api/dashboard/summary`
- `GET /api/dashboard/sales-history` - 驾驶舱历史烟品销售对比，参数：`metric=quantity|amount`、`days=7|30`、`limit`
- `GET /api/reports/purchase-summary`
- `GET /api/reports/sales-summary`
- `GET /api/reports/inventory-summary`
- `GET /api/reports/trend`
- `GET /api/reports/psi-summary`
- `GET /api/reports/compliance-trace`
- `GET /api/reports/abnormal-docs`
- `POST /api/reports/abnormal-docs/{id}/audit`
- `GET /api/reports/linkage`
- `GET /api/reports/export`


## 2026-03-28 缺失功能补齐补充
- 认证：前端已接入 `GET /api/auth/captcha`、`POST /api/auth/forgot-password`、`POST /api/auth/reset-password`。
- 采购：新增 `GET /api/purchases/{id}`、`PUT /api/purchases/{id}` 用于订单编辑。
- 销售：新增 `GET /api/sales/{id}`、`PUT /api/sales/{id}` 用于订单编辑。
- 客户：`GET /api/customers` 支持 `keyword/status` 过滤，并新增 `GET /api/customers/{id}`。
- 仓库：新增 `GET /api/warehouses/{id}`、`PUT /api/warehouses/{id}`、`PUT /api/warehouses/{id}/status`。
- 消息：前端新增独立消息中心，复用 `GET /api/messages` 与 `POST /api/messages/{id}/read`。


## 本次更新（2026-03-29）
- `POST /api/purchases/{id}/inbound`：请求体需包含 `warehouseId`，可选 `remark`；成功后返回回写仓库信息的采购单。
- `POST /api/sales/{id}/outbound`：请求体需包含 `warehouseId`，可选 `remark`；按指定仓库扣减库存。
- `POST /api/inventory-transfers`：请求体使用 `productId`、`fromWarehouseId`、`toWarehouseId`、`quantity`、`remark`。
- `POST /api/inventory-checks`：请求体需包含 `productId`、`warehouseId`、`quantity`、`remark`。
- `GET /api/inventories` / `GET /api/inventory-records` / `GET /api/inventory-warnings`：支持 `warehouseId` 过滤；`GET /api/inventories` 额外支持 `keyword`、`status`。

## 2026-03-29 驾驶舱历史销售对比补充
- 驾驶舱新增 `GET /api/dashboard/sales-history` 用于首页重点烟品历史销售对比图表。
- 返回结构包含 `metric`、`periods`、`series[]`，其中 `series[].values` 与 `periods` 一一对应。
- 当前统计口径为销售订单状态 `OUTBOUND`、`PARTIAL_PAID`、`PAID`，用于反映已进入履约链路的真实销售数据。
