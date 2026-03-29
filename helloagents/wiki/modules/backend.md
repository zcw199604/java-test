# backend

## 目的
提供任务书对齐的后端 API、认证权限、采销存流程与审计追溯能力。

## 模块概述
- **职责:** REST API、Shiro 认证与密码哈希、Spring JDBC / MyBatis 注解数据访问、Excel 导入导出、日志消息与业务服务编排
- **状态:** 🚧开发中
- **最后更新:** 2026-03-29

## 规范
- 所有 `/api/*` 业务接口默认要求登录。
- 敏感操作需要写入操作日志与追溯记录。

## 最新变更
- 商品接口 `GET /api/products` 支持 `keyword`、`status`、`category` 过滤参数。
- 供应商接口 `GET /api/suppliers` 支持 `keyword`、`status` 过滤参数。
- `SpaForwardController` 已补齐 `/catalog/products` 与 `/supplier/list` 的 SPA 回退路由。

- 账号管理相关接口已按当前 schema 修正：`/api/users`、`/api/users/{id}`、`POST /api/users`、`PUT /api/users/{id}`、`PUT /api/users/{id}/status` 可正常使用。
- 角色列表接口 `GET /api/roles` 现返回每个角色已有的权限数组，便于前端回显权限树。
- 初始化数据 `data.sql` 已补充 `permissions` 与 `role_permissions` 的基础种子。

- 采购/销售/库存关键业务动作现已同时写入 `operation_logs`，覆盖审批、到货、入库、出库、回款、调拨与盘点。
- 用户删除接口 `DELETE /api/users/{id}` 已提供，并在删除前清理会话与数据范围。

- 采购与销售服务层已增加审批角色校验，采购审核/到货/入库与销售审核/出库仅允许超级管理员、普通管理员或库管执行。
- 库存盘点接口继续以 `/api/inventory-checks` 更新 `inventories.quantity`，并作为前端盘点提交的唯一真实入口。

- 数据库初始化角色现为五种：SUPER_ADMIN、ADMIN、PURCHASER、SELLER、KEEPER；默认 `admin/123456` 为超级管理员，新增 `manager/123456` 作为普通管理员示例账号。
- 用户创建与编辑接口现校验操作者角色：仅 `SUPER_ADMIN` 可分配 `SUPER_ADMIN` / `ADMIN`，避免普通管理员扩散管理员权限。
- 管理员类账号保护已扩展到更新、状态变更与删除接口：非 `SUPER_ADMIN` 无法修改、停用或删除 `SUPER_ADMIN` / `ADMIN` 用户。
- InventoryService 现已在调拨与盘点后复用库存预警通知逻辑，库存降至或低于阈值时同样向库管发送 ALERT 消息。


## 本次更新（2026-03-29）
- 新增 `GET /api/dashboard/sales-history`，支持按 `metric=quantity|amount`、`days=7|30`、`limit` 聚合重点烟品历史销售对比数据。
- `DashboardService` 已接入真实销售订单聚合逻辑，只统计已出库/回款链路单据，并自动补齐日期序列返回给驾驶舱图表。
- 后端集成测试已补充仓库维度库存回归场景，覆盖选仓入库/出库、双仓调拨、按仓盘点和缺少仓库参数的失败分支。
- 库存、采购、销售服务已统一切换为仓库维度库存模型：`inventories` 以 `product_id + warehouse_id` 作为唯一库存单元。
- `POST /api/purchases/{id}/inbound` 与 `POST /api/sales/{id}/outbound` 现要求请求体携带 `warehouseId`，并回写订单仓库字段与库存流水仓库字段。
- `POST /api/inventory-transfers` 现支持 A 仓到 B 仓双仓调拨；`GET /api/inventories`、`GET /api/inventory-records`、`GET /api/inventory-warnings` 已支持按仓库筛选。
