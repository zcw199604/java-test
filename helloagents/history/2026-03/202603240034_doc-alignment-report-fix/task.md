# 任务清单: 文档与实现对齐及报表/采购流程修复

## 1. 文档与知识库核对
- [√] 1.1 更新 `README.md` 的项目现状、功能覆盖与报表导出描述
- [√] 1.2 更新 `helloagents/project.md` 的数据层与测试说明
- [√] 1.3 更新知识库中的 API / 架构 / 概述 / 模块文档

## 2. 后端报表与首页修复
- [√] 2.1 修改 `backend/src/main/java/com/example/tobacco/report/ReportService.java`，生成真实 CSV 导出内容
- [√] 2.2 修改 `backend/src/main/java/com/example/tobacco/report/ReportController.java`，返回 CSV 响应头与文本内容
- [√] 2.3 修改 `backend/src/main/java/com/example/tobacco/service/DashboardService.java`，使用数据库真实统计值

## 3. 采购流程修复
- [√] 3.1 修改 `backend/src/main/resources/sql/schema.sql` 与种子数据，支持到货时间与独立状态
- [√] 3.2 修改 `backend/src/main/java/com/example/tobacco/model/PurchaseOrderItem.java`、`PurchaseService.java`、`PurchaseController.java` 支持到货/入库分离
- [√] 3.3 修改前端采购 API 与页面，支持到货和入库两个动作

## 4. 前端接口修复
- [√] 4.1 修改 `frontend/src/api/report.js` 与报表页面，按真实 CSV / 汇总 / 库存台账语义联调
- [√] 4.2 修改 `frontend/src/views/catalog/ProductListView.vue`，使用后端真实字段
- [√] 4.3 校验首页与相关前端页面的统计展示

## 5. 验证与收尾
- [√] 5.1 补充/更新后端测试，覆盖报表导出与采购状态流转
- [√] 5.2 执行 `mvn test` 与 `npm run build`
- [√] 5.3 更新 `helloagents/CHANGELOG.md`、迁移方案包到 `history/`、更新索引
