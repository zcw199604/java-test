# sales

## 目的
管理销售信息发布、销售单创建、审核、取消、出库、回款、统计与应收。

## 模块概述
- **职责:** 公告发布、销售单、审核、取消、出库、回款、统计、应收、Excel 导入
- **状态:** ✅可用
- **最后更新:** 2026-03-29

## 规范
- 状态流转为 `CREATED -> APPROVED|REJECTED -> OUTBOUND -> PARTIAL_PAID|PAID`，其中 `CREATED/REJECTED` 可转为 `CANCELLED`
- `POST /api/sales/{id}/outbound` 必须携带 `warehouseId`
- `POST /api/sales/import` 限制单文件 `<= 5MB`、单次 `<= 1000` 行
- 审核、出库仅允许超级管理员、普通管理员或库管执行

## 当前实现
- `GET /api/sales/{id}` 支持编辑页回填
- `GET /api/sales/{id}/trace` 提供创建、审核、出库、回款等流程节点时间线
- 编辑后的销售单会回到待审核状态
- `GET /api/sales/receivables` 与 `GET /api/sales/statistics` 提供应收与汇总能力
- `GET/POST /api/bulletins` 承载销售信息发布

## 最新变更
- 销售出库已切换为按仓扣减库存
- 已补齐独立“销售出库页”，支持从订单列表进入后选择仓库执行出库
- 日志、追溯、库存预警消息已覆盖审核、出库与回款链路
