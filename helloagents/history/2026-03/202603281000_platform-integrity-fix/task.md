# 任务清单: 平台功能完整性修复（10项高/中优先级问题）

> 状态: 已执行完成，已归档至 history/2026-03/

目录: `helloagents/history/2026-03/202603281000_platform-integrity-fix/`

---

## 1. 补全 schema.sql 缺失表定义（问题 #1）
- [√] 1.1 在 `backend/src/main/resources/sql/schema.sql` 末尾追加 13 张缺失表的 `CREATE TABLE IF NOT EXISTS` 语句
- [√] 1.2 在 `backend/src/main/resources/sql/schema.sql` 中追加 purchase_orders 和 sales_orders 的审核/取消字段 ALTER 语句
- [√] 1.3 在 `backend/src/main/resources/sql/schema.sql` 中追加 bulletins 表定义

## 2. 补全权限初始数据（问题 #10）
- [√] 2.1 在 `backend/src/main/resources/sql/seed.sql` 中追加 permissions 表初始数据（30条权限定义）
- [√] 2.2 在 `backend/src/main/resources/sql/seed.sql` 中追加 role_permissions 表初始数据

## 3. 采购订单审核与取消（问题 #2 + #3）
- [√] 3.1 在 `PurchaseOrderItem.java` 中新增字段: auditedBy, auditedAt, auditRemark, cancelReason
- [√] 3.2 新增 `AuditRequest.java` 请求DTO
- [√] 3.3 新增 `CancelRequest.java` 请求DTO
- [√] 3.4 在 `PurchaseService.java` 中新增 `audit()` 方法
- [√] 3.5 在 `PurchaseService.java` 中新增 `cancel()` 方法
- [√] 3.6 在 `PurchaseService.java` 中修改 `receive()` 前置条件为 APPROVED
- [√] 3.7 在 `PurchaseController.java` 中新增 audit 和 cancel 端点
- [√] 3.8 在 `PurchaseService.java` 的 SQL 中追加审核/取消字段映射

## 4. 销售订单审核与取消（问题 #2 + #3）
- [√] 4.1 在 `SalesOrderItem.java` 中新增字段: auditedBy, auditedAt, auditRemark, cancelReason
- [√] 4.2 在 `SalesService.java` 中新增 `audit()` 方法
- [√] 4.3 在 `SalesService.java` 中新增 `cancel()` 方法
- [√] 4.4 在 `SalesService.java` 中修改 `outbound()` 前置条件为 APPROVED
- [√] 4.5 在 `SalesController.java` 中新增 audit 和 cancel 端点
- [√] 4.6 在 `SalesService.java` 的 SQL 中追加审核/取消字段映射

## 5. Excel 导入业务入口（问题 #4）
- [√] 5.1 在 `PurchaseService.java` 中新增 `importFromExcel()` 方法
- [√] 5.2 在 `PurchaseController.java` 中新增 `POST /api/purchases/import` 端点
- [√] 5.3 在 `SalesService.java` 中新增 `importFromExcel()` 方法
- [√] 5.4 在 `SalesController.java` 中新增 `POST /api/sales/import` 端点
- [√] 5.5 在 `InventoryService.java` 中新增 `importFromExcel()` 方法
- [√] 5.6 在 `InventoryController.java` 中新增 `POST /api/inventories/import` 端点

## 6. 消息自动推送机制（问题 #5）
- [√] 6.1 在 `PurchaseService.java` 中注入 MessageService，inbound() 末尾添加库存预警通知
- [√] 6.2 在 `SalesService.java` 中注入 MessageService，outbound() 末尾添加库存预警通知
- [√] 6.3 在 `PurchaseService.java` 的 audit() 中添加审核结果消息推送
- [√] 6.4 在 `SalesService.java` 的 audit() 中添加审核结果消息推送

## 7. 库存调拨逻辑修复（问题 #6）
- [√] 7.1 在 `InventoryService.java` 中修改 transfer() 方法，实际扣减库存

## 8. 追溯记录自动写入（问题 #7）
- [√] 8.1 在 `PurchaseService.java` 中注入 AuditService，4个方法末尾调用 trace()
- [√] 8.2 在 `SalesService.java` 中注入 AuditService，4个方法末尾调用 trace()

## 9. 销售信息发布模块（问题 #8）
- [√] 9.1 新建 `BulletinService.java`
- [√] 9.2 新建 `BulletinController.java`
- [√] 9.3 新建 `frontend/src/api/bulletin.js`
- [√] 9.4 新建 `frontend/src/views/sale/SaleBulletinView.vue`
- [√] 9.5 在 route-map.ts 中新增 `/sale/bulletin` 路由

## 10. 异常单据审核接口（问题 #9）
- [√] 10.1 在 `ReportService.java` 中新增 `auditAbnormalDoc()` 方法
- [√] 10.2 在 `ReportController.java` 中新增 `POST /api/reports/abnormal-docs/{id}/audit` 端点
- [√] 10.3 在 `ExceptionAuditView.vue` 中接入真实审核 API

## 11. 前端适配（采购/销售审核与取消按钮）
- [√] 11.1 在 `purchase.js` 中新增 auditPurchase 和 cancelPurchase 接口
- [√] 11.2 在 `PurchaseOrderListView.vue` 中添加审核/取消按钮和对话框
- [√] 11.3 在 `sales.js` 中新增 auditSales 和 cancelSales 接口
- [√] 11.4 在 `SaleOrderListView.vue` 中添加审核/取消按钮和对话框

## 12. 前端适配（Excel 导入上传）
- [√] 12.1 在 `purchase.js` 中新增 importPurchases 接口
- [√] 12.2 在 `PurchaseOrderListView.vue` 中添加导入按钮和 el-upload
- [√] 12.3 在 `sales.js` 中新增 importSales 接口
- [√] 12.4 在 `SaleOrderListView.vue` 中添加导入按钮和 el-upload
- [√] 12.5 在 `inventory.js` 中新增 importInventories 接口
- [√] 12.6 在 `InventoryListView.vue` 中添加导入按钮和 el-upload

## 13. 安全检查
- [√] 13.1 Excel 导入已添加 5MB 文件大小限制和 1000 行限制；审核接口已校验状态前置条件；SQL 均使用参数化查询

## 14. 文档更新
- [√] 14.1 更新 wiki/api.md
- [√] 14.2 更新 wiki/data.md
- [√] 14.3 更新 wiki/modules/procurement.md
- [√] 14.4 更新 wiki/modules/sales.md
- [√] 14.5 更新 wiki/modules/inventory.md
- [√] 14.6 更新 CHANGELOG.md

## 15. 测试与验证
- [√] 15.1 执行代码与知识库一致性审计，确认采购/销售/库存/公告/异常审核接口已同步到知识库
- [X] 15.2 执行 `mvn "-Dmaven.repo.local=D:\workspace-idea\java-test\.m2-repo" "-Dmaven.wagon.http.ssl.insecure=true" "-Dmaven.wagon.http.ssl.allowall=true" test`
> 备注: 依赖下载已恢复，但 `PlatformIntegrationTest` 启动时连接本地 MySQL 失败，错误为 `Connection refused: connect`，当前环境缺少可用数据库实例。
- [√] 15.3 执行 `npm ci`
- [√] 15.4 执行 `npm run build`（含 `vue-tsc --noEmit`），前端构建产物已刷新到 `backend/src/main/resources/static/`
- [?] 15.5 采购/销售审核与取消、Excel 导入、公告发布、异常单据审核、库存调拨等业务链路待接入可用 MySQL 后做人工联调
> 备注: 当前会话已完成静态一致性检查和前端构建验证，但未连接可用业务数据库，无法完成端到端联调验收。
