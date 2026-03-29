# 数据模型

## 概述
当前系统围绕“认证权限、系统管理、采销存业务、日志消息、追溯异常、Excel 批处理”扩展了完整 MySQL 数据表集合。

## 核心表
- `roles`：角色定义
- `permissions` / `role_permissions`：权限点与角色权限关系
- `users` / `user_data_scopes` / `user_sessions`：用户、数据范围、登录会话
- `captcha_records` / `password_reset_records`：验证码与重置密码流程
- `login_logs` / `operation_logs`：登录日志与操作日志
- `system_configs`：系统配置
- `messages`：站内消息
- `categories` / `products` / `suppliers` / `customers` / `warehouses`：基础主数据
- `purchase_requisitions` / `purchase_orders` / `purchase_order_tracks`：采购需求、采购单、采购跟踪，采购单包含 `audited_by`、`audited_at`、`audit_remark`、`cancel_reason`
- `bulletins` / `sales_orders` / `payment_records` / `receivable_records`：销售公告、销售单、回款、应收，销售单包含 `audited_by`、`audited_at`、`audit_remark`、`cancel_reason`
- `inventories` / `inventory_records` / `inventory_check_reports` / `warning_records`：库存台账、流水、盘点、预警
- `trace_records` / `abnormal_documents`：全链路追溯与异常单据

## 关键关系
- `users.role_code -> roles.code`
- `role_permissions.role_code -> roles.code`
- `role_permissions.permission_code -> permissions.code`
- `purchase_orders.requisition_id -> purchase_requisitions.id`
- `purchase_orders.supplier_id -> suppliers.id`
- `purchase_orders.product_id -> products.id`
- `sales_orders.customer_id -> customers.id`
- `sales_orders.product_id -> products.id`
- `inventories.product_id -> products.id`
- `inventories.warehouse_id -> warehouses.id`
- `inventory_records.product_id -> products.id`
- `payment_records.sales_order_id -> sales_orders.id`
- `receivable_records.sales_order_id -> sales_orders.id`
- `messages.biz_id / trace_records.biz_id / abnormal_documents.biz_id` 指向对应业务主键

## 业务链路
1. 用户登录 → 写入 `user_sessions`、`login_logs`
2. 采购提报 → 建采购单 → 审核 → 到货 → 入库 → 写 `purchase_order_tracks`、`inventory_records`、`trace_records`
3. 销售公告发布 → 建销售单 → 审核 → 出库 → 回款 → 写 `payment_records`、`receivable_records`、`trace_records`
4. 库存调拨 / 盘点 → 写 `inventory_records`、`inventory_check_reports`、`warning_records`
5. 异常审核 / 合规追溯 → 聚合 `trace_records` 与 `abnormal_documents`


## 本次更新（2026-03-29）
- `inventories` 已从单仓模型升级为多仓模型，新增 `warehouse_id` 并使用组合唯一键 `product_id + warehouse_id`。
- `inventory_records` 新增 `warehouse_id / warehouse_name / from_warehouse_id / to_warehouse_id` 等字段，用于记录入库、出库、调拨、盘点的仓库轨迹。
- `purchase_orders` 与 `sales_orders` 均新增 `warehouse_id / warehouse_name`，用于保留订单实际入库仓/出库仓。
