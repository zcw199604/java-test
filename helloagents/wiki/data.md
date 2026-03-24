# 数据模型

## 概述
当前系统通过 MySQL 运行，已具备系统管理、基础资料、采购、库存、销售、报表所需的核心表结构。

## 数据表
- `roles`：角色定义
- `users`：系统用户
- `categories`：商品品类
- `products`：商品资料
- `suppliers`：供应商资料
- `customers`：客户资料
- `purchase_orders`：采购单（含创建、到货、入库状态与时间）
- `inventories`：库存台账
- `inventory_records`：库存流水
- `sales_orders`：销售单
- `payment_records`：回款记录

## 关键关系
- `users.role_code -> roles.code`
- `purchase_orders.supplier_id -> suppliers.id`
- `purchase_orders.product_id -> products.id`
- `inventories.product_id -> products.id`
- `inventory_records.product_id -> products.id`
- `sales_orders.customer_id -> customers.id`
- `sales_orders.product_id -> products.id`
- `payment_records.sales_order_id -> sales_orders.id`

## 业务链路
1. 创建采购单 → 登记到货 → 采购入库后更新 `inventories` 并写入 `inventory_records`
2. 创建销售单 → 出库后扣减 `inventories` 并写入 `inventory_records`
3. 回款登记 → 更新 `sales_orders.paid_amount` 与订单状态
4. 报表汇总 → 聚合采购、销售、库存数据并导出 CSV
