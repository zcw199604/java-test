# sales

## 目的
管理销售信息发布、销售订单创建、审核、销售出库、回款与应收状态。

## 模块概述
- **职责:** 信息发布、销售单、审核、取消、出库、回款、应收、统计、Excel 批处理
- **状态:** ✅已落地，待数据库联调验证
- **最后更新:** 2026-03-28

## 规范
- 销售单状态流转为 `CREATED -> APPROVED|REJECTED -> OUTBOUND -> PARTIAL_PAID|PAID`，其中 `CREATED/REJECTED` 可转为 `CANCELLED`。
- `GET/POST /api/bulletins` 对应销售信息发布能力，公告数据写入 `bulletins` 表。
- `POST /api/sales/{id}/audit` 会写入 `audited_by`、`audited_at`、`audit_remark`；`POST /api/sales/{id}/cancel` 会写入 `cancel_reason`。
- `POST /api/sales/import` 走 Excel 批量导入，限制单文件 `<=5MB`、单次 `<=1000` 行。
- 审核、出库、回款均会写入 `trace_records`；出库后会触发库存预警检查并向库管角色推送站内消息。


## 本次更新（2026-03-28）
- 销售订单支持详情加载与重新编辑，更新后自动回到待审核状态。
- 前端销售编辑页不再误调用创建接口。
