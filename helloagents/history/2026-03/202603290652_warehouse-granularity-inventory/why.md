# 为什么要做

## 背景
当前系统的库存模型以 `product_id` 为唯一维度：
- `inventories` 表对 `product_id` 做唯一约束，无法同时维护同一商品在多个仓库的库存。
- 采购入库、销售出库、库存调拨均未真正绑定具体仓库。
- 前端入库/出库流程也没有仓库选择，库存总览、库存流水、库存台账缺少“按仓库查看/筛选”能力。

这导致“仓库管理”虽然存在，但尚未进入采销存主链路，无法满足真实业务中的多仓库管理场景。

## 现状证据
- 数据库：`backend/src/main/resources/sql/schema.sql`
  - `inventories(product_id UNIQUE, warehouse_name)`，只允许单仓库存。
  - `inventory_records` 不记录仓库维度。
- 后端：
  - `PurchaseService.inbound()` 与 `SalesService.outbound()` 直接按 `product_id` 扣减/增加库存，不接收仓库参数。
  - `InventoryService.transfer()` 仅做库存扣减，不支持从 A 仓转入 B 仓。
- 前端：
  - `PurchaseInboundView.vue` 无仓库选择。
  - `SaleOutboundView.vue` 无仓库选择。
  - `InventoryListView.vue`、`InventoryFlowView.vue`、`InventoryLedgerView.vue` 缺少仓库筛选与总览能力。

## 目标
建立“商品 + 仓库”双维度库存模型，使仓库真正进入以下链路：
1. 采购入库时可选择入库仓库。
2. 销售出库时可选择出库仓库。
3. 库存总览、库存流水、库存台账支持查看全部及按仓库筛选。
4. 库存调拨支持从 A 仓调拨至 B 仓，并形成完整双边流水。

## 成功标准
- 同一商品可在多个仓库同时存在库存记录。
- 采购入库、销售出库、库存调拨均要求或显式携带仓库信息。
- 库存相关页面支持“全部仓库 + 指定仓库”筛选。
- 数据库迁移后旧数据可映射到默认仓库，不破坏现有演示数据可用性。

## 风险与约束
- 本次已明确要求订单级仓库归属，需要同时扩展采购/销售订单表字段，用于持久化入库仓与出库仓。
- 最大风险在数据库迁移：不仅要移除 `inventories.product_id` 唯一约束，还需要为历史订单与历史库存补齐默认仓库映射。
