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
