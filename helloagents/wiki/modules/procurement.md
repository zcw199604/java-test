# procurement

## 目的
管理采购需求提报、采购订单创建、审核、到货登记、采购入库与采购跟踪分析。

## 模块概述
- **职责:** 采购需求、采购单、审核、取消、到货、入库、分析、Excel 批处理
- **状态:** ✅已落地，待数据库联调验证
- **最后更新:** 2026-03-28

## 规范
- 采购单状态流转为 `CREATED -> APPROVED|REJECTED -> RECEIVED -> INBOUND`，其中 `CREATED/REJECTED` 可转为 `CANCELLED`。
- `POST /api/purchases/{id}/audit` 会写入 `audited_by`、`audited_at`、`audit_remark`；`POST /api/purchases/{id}/cancel` 会写入 `cancel_reason`。
- `POST /api/purchases/import` 走 Excel 批量导入，限制单文件 `<=5MB`、单次 `<=1000` 行。
- 创建、审核、取消、到货、入库均会写入 `purchase_order_tracks` 与 `trace_records`；入库后会触发库存预警检查与站内消息推送。


## 本次更新（2026-03-28）
- 采购订单支持通过详情接口加载原始数据后进行编辑。
- 编辑后的采购单会回到待审核状态，以保持审批链路一致。
