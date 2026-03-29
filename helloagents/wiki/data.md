# 数据模型

## 概述
当前数据模型以 `backend/src/main/resources/sql/schema.sql` 与 `data.sql` 为准。后端启动时会自动建表、执行兼容性 `ALTER TABLE`，并重新加载演示数据。

## 核心表

### 认证与权限
- `roles`：角色定义
- `users`：用户账号
- `permissions` / `role_permissions`：权限点与角色权限关系
- `user_data_scopes`：用户数据范围
- `user_sessions`：登录会话令牌
- `captcha_records`：登录验证码
- `password_reset_records`：重置密码令牌

### 系统与基础资料
- `system_configs`：系统配置
- `categories`：品类
- `products`：商品
- `suppliers`：供应商
- `customers`：客户
- `warehouses`：仓库

### 采购与销售
- `purchase_orders`：采购单，包含审核、取消、入库仓信息
- `sales_orders`：销售单，包含审核、取消、出库仓与回款累计
- `payment_records`：销售回款记录
- `bulletins`：销售信息发布 / 公告

### 库存与追溯
- `inventories`：库存台账，当前以 `product_id + warehouse_id` 为唯一库存单元
- `inventory_records`：库存流水，记录入库、出库、调拨、盘点与来源/目标仓
- `trace_records`：业务追溯节点
- `abnormal_documents`：异常单据
- `messages`：站内消息
- `login_logs` / `operation_logs`：登录日志与操作日志

## 关键关系
- `users.role_code -> roles.code`
- `role_permissions.role_code -> roles.code`
- `role_permissions.permission_code -> permissions.code`
- `purchase_orders.supplier_id -> suppliers.id`
- `purchase_orders.product_id -> products.id`
- `sales_orders.customer_id -> customers.id`
- `sales_orders.product_id -> products.id`
- `payment_records.sales_order_id -> sales_orders.id`
- `inventories.product_id -> products.id`
- `inventories.warehouse_id -> warehouses.id`
- `inventory_records.product_id -> products.id`
- `inventory_records.warehouse_id / from_warehouse_id / to_warehouse_id -> warehouses.id`
- `messages.biz_id`、`trace_records.biz_id`、`abnormal_documents.biz_id` 指向对应业务主键

## 当前实现说明
- 当前**不存在**独立的 `purchase_requisitions`、`purchase_order_tracks`、`inventory_check_reports`、`warning_records`、`receivable_records` 表
- 采购需求兼容接口当前直接复用 `purchase_orders`
- 采购 / 销售业务轨迹由 `trace_records` 与 `operation_logs` 承担
- 库存盘点、调拨、预警轨迹统一沉淀到 `inventory_records` 与 `messages`

## 业务链路
1. 用户登录 → 写入 `user_sessions`、`login_logs`
2. 采购建单 / 编辑 / 审核 / 到货 / 入库 → 写 `purchase_orders`、`operation_logs`、`trace_records`、`inventory_records`
3. 销售建单 / 编辑 / 审核 / 出库 / 回款 → 写 `sales_orders`、`payment_records`、`operation_logs`、`trace_records`、`inventory_records`
4. 库存调拨 / 盘点 / 预警 → 写 `inventories`、`inventory_records`、`messages`
5. 异常审核 / 合规追溯 → 聚合 `trace_records`、`abnormal_documents`、`operation_logs`

## 本次确认（2026-03-29）
- `inventories` 已升级为多仓模型，并建立组合唯一键 `product_id + warehouse_id`
- `inventory_records` 已支持 `warehouse_id`、`from_warehouse_id`、`to_warehouse_id` 等仓库轨迹字段
- `purchase_orders` 与 `sales_orders` 已扩展 `warehouse_id / warehouse_name`
- `data.sql` 会在启动时先清空核心业务表，再重新插入演示账号、仓库、商品与业务单据
