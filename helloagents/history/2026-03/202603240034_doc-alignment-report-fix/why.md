# 变更提案: 文档与实现对齐及报表/采购流程修复

## 需求背景
当前项目已具备采销存最小闭环，但代码实现、README、项目技术约定及部分前端页面之间存在偏差，导致“文档描述”“接口语义”“页面展示”不完全一致。用户已明确要求以当前后端返回和实际业务流程为准，对文档、接口、前端联调和首页概览进行一次对齐修复。

## 变更目标
1. 修正 `README.md` 与 `helloagents/project.md` 中过时描述，使其与当前实现一致。
2. 将报表导出接口改为真正输出 CSV，消除“导出 JSON 伪装成 CSV”的不一致。
3. 修复前端报表与商品页面对接口字段和返回结构的错误假设。
4. 拆分采购“到货”和“入库”为两个独立动作，保证状态流转清晰。
5. 将首页概览统计改为后端实时数据，移除硬编码数量。
6. 同步更新知识库，确保文档与代码再次保持一致。

## 影响范围
- **模块:** backend, frontend, procurement, reporting, dashboard, helloagents/wiki
- **文件:** 后端控制器/服务/模型/SQL，前端 API 与页面，README，知识库文档
- **API:** `/api/reports/export`、`/api/purchases/{id}/receive`、`/api/purchases/{id}/inbound`、`/api/dashboard/summary`
- **数据:** 采购单状态流转与时间字段、报表导出内容

## 成功标准
- `/api/reports/export` 返回真实 CSV 内容，前端预览与下载均可用。
- 报表中心按接口实际语义展示汇总与库存明细。
- 商品列表、报表页面使用后端真实字段，不再依赖错误字段名。
- 采购流程支持 `CREATED -> RECEIVED -> INBOUND`。
- 首页概览统计来自数据库实时查询。
- `README.md`、`project.md`、知识库文档与当前实现一致。
