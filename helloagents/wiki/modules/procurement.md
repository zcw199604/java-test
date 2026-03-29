# procurement

## 目的
管理采购单创建、编辑、审核、取消、到货、入库、导入与追溯。

## 模块概述
- **职责:** 采购列表、详情、编辑、审核、取消、到货、入库、Excel 导入、链路追溯
- **状态:** ✅可用
- **最后更新:** 2026-03-29

## 规范
- 状态流转为 `CREATED -> APPROVED|REJECTED -> RECEIVED -> INBOUND`，其中 `CREATED/REJECTED` 可转为 `CANCELLED`
- `POST /api/purchases/{id}/inbound` 必须携带 `warehouseId`，入库后回写订单仓库并新增库存流水
- `POST /api/purchases/import` 限制单文件 `<= 5MB`、单次 `<= 1000` 行
- 审核、到货、入库仅允许超级管理员、普通管理员或库管执行

## 当前实现
- `GET /api/purchases/{id}` 支持前端编辑页回填
- 编辑后的采购单会回到待审核状态，保持审批链路一致
- `GET /api/purchases/{id}/trace` 返回采购链路追溯节点
- `GET /api/purchase-requisitions` 与 `GET /api/purchases/requisitions` 当前均复用采购单列表，不对应独立数据表

## 最新变更
- 入库动作已切换为仓库维度库存模型
- 入库、审核、取消、到货动作统一写入操作日志与追溯记录
- 入库完成后会复用库存预警逻辑，并向相关角色推送站内消息
