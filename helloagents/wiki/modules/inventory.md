# inventory

## 目的
管理多仓库存总览、库存流水、调拨、盘点、预警与追溯台账。

## 模块概述
- **职责:** 库存台账、流水、调拨、盘点、预警、Excel 导入
- **状态:** ✅可用
- **最后更新:** 2026-03-29

## 规范
- 库存主表按 `product_id + warehouse_id` 维护唯一库存单元
- 调拨必须同时校验来源仓与目标仓，且不能只写流水不改库存
- 盘点必须指定 `warehouseId`
- `POST /api/inventories/import` 限制单文件 `<= 5MB`、单次 `<= 1000` 行

## 当前实现
- `GET /api/inventories` 支持 `warehouseId`、`keyword`、`status`
- `GET /api/inventory-records` 支持 `warehouseId`、`bizType`
- `GET /api/inventory-warnings` 支持按仓筛选
- 调拨会拆分写入 `TRANSFER_OUT` / `TRANSFER_IN` 两条流水
- 盘点会按指定仓库直接更新当前库存数量

## 最新变更
- 库存模型已从单仓升级为多仓
- 采购入库、销售出库、调拨、盘点均复用统一库存预警通知逻辑
- 页面侧已支持仓库筛选、仓库选择和多仓流水中文展示
