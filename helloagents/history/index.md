# 变更历史索引

本文件记录所有已完成变更的索引，便于追溯和查询。

---

## 索引

| 时间戳 | 功能名称 | 类型 | 状态 | 方案包路径 |
|--------|----------|------|------|------------|
| 202603230000 | tobacco-platform-init | 功能 | ✅已完成 | [helloagents/history/2026-03/202603230000_tobacco-platform-init](2026-03/202603230000_tobacco-platform-init/) |
| 202603231548 | full-platform-features | 功能 | ✅已完成 | [helloagents/history/2026-03/202603231548_full-platform-features](2026-03/202603231548_full-platform-features/) |
| 202603240034 | doc-alignment-report-fix | 修复 | ✅已完成 | [helloagents/history/2026-03/202603240034_doc-alignment-report-fix](2026-03/202603240034_doc-alignment-report-fix/) |
| 202603240230 | frontend-page-refactor | 功能 | ✅已完成 | [helloagents/history/2026-03/202603240230_frontend-page-refactor](2026-03/202603240230_frontend-page-refactor/) |
| 202603240318 | taskbook-backend-alignment | 重构 | ✅已完成 | [helloagents/history/2026-03/202603240318_taskbook-backend-alignment](2026-03/202603240318_taskbook-backend-alignment/) |
| 202603240320 | frontend-second-optimization | 优化 | ✅已完成 | [helloagents/history/2026-03/202603240320_frontend-second-optimization](2026-03/202603240320_frontend-second-optimization/) |
| 202603281000 | platform-integrity-fix | 修复 | ✅已完成 | [helloagents/history/2026-03/202603281000_platform-integrity-fix](2026-03/202603281000_platform-integrity-fix/) |
| 202603280644 | catalog-supplier-maintenance | 修复 | ✅已完成 | [helloagents/history/2026-03/202603280644_catalog-supplier-maintenance](2026-03/202603280644_catalog-supplier-maintenance/) |
| 202603280803 | account-role-management-fix | 修复 | ✅已完成 | [helloagents/history/2026-03/202603280803_account-role-management-fix](2026-03/202603280803_account-role-management-fix/) |
| 202603280851 | audit-log-and-user-delete | 修复 | ✅已完成 | [helloagents/history/2026-03/202603280851_audit-log-and-user-delete](2026-03/202603280851_audit-log-and-user-delete/) |
| 202603281130 | approval-and-inventory-check-fix | 修复 | ✅已完成 | [helloagents/history/2026-03/202603281130_approval-and-inventory-check-fix](2026-03/202603281130_approval-and-inventory-check-fix/) |
| 202603281130a | inventory-warning-notify-fix | 修复 | ✅已完成 | [helloagents/history/2026-03/202603281130_inventory-warning-notify-fix](2026-03/202603281130_inventory-warning-notify-fix/) |
| 202603281045 | role-hierarchy-fix | 修复 | ✅已完成 | [helloagents/history/2026-03/202603281045_role-hierarchy-fix](2026-03/202603281045_role-hierarchy-fix/) |
| 202603281120 | admin-account-protection | 修复 | ✅已完成 | [helloagents/history/2026-03/202603281120_admin-account-protection](2026-03/202603281120_admin-account-protection/) |
| 202603281415 | missing-feature-completion | 功能 | ✅已完成 | [helloagents/history/2026-03/202603281415_missing-feature-completion](2026-03/202603281415_missing-feature-completion/) |

---

## 按月归档

### 2026-03

- [202603230000_tobacco-platform-init](2026-03/202603230000_tobacco-platform-init/) - 初始化烟草采销存协同管理平台前后端分离工程
- [202603231548_full-platform-features](2026-03/202603231548_full-platform-features/) - 完成前后端全模块最小闭环与 Docker MySQL 联调
- [202603240034_doc-alignment-report-fix](2026-03/202603240034_doc-alignment-report-fix/) - 修正文档与代码差异，补齐真实 CSV 导出、采购到货/入库拆分与首页实时统计
- [202603240230_frontend-page-refactor](2026-03/202603240230_frontend-page-refactor/) - 完成前端页面重构、动态权限菜单与后台工作台升级
- [202603240318_taskbook-backend-alignment](2026-03/202603240318_taskbook-backend-alignment/) - 按任务书方向补齐后端认证、权限、采销存、报表、日志与 Excel 能力
- [202603240320_frontend-second-optimization](2026-03/202603240320_frontend-second-optimization/) - 完成前端第二轮优化：性能拆包、按钮权限、无权限页与 TypeScript 改造
- [202603281000_platform-integrity-fix](2026-03/202603281000_platform-integrity-fix/) - 补齐平台完整性缺口：订单审核取消、Excel 导入、消息推送、库存调拨、公告发布与异常审核
- [202603280644_catalog-supplier-maintenance](2026-03/202603280644_catalog-supplier-maintenance/) - 补齐商品/供应商维护、列表过滤与管理页路由回退
- [202603280803_account-role-management-fix](2026-03/202603280803_account-role-management-fix/) - 修复账号管理与角色权限能力，支持角色切换与权限回显保存
- [202603280851_audit-log-and-user-delete](2026-03/202603280851_audit-log-and-user-delete/) - 补齐业务操作日志并新增删除用户能力
- [202603281130_approval-and-inventory-check-fix](2026-03/202603281130_approval-and-inventory-check-fix/) - 补齐采购/销售审批确认角色限制、库管页面权限与库存盘点实际提交
- [202603281045_role-hierarchy-fix](2026-03/202603281045_role-hierarchy-fix/) - 调整角色体系为五种并将默认 admin 切换为超级管理员
- [202603281120_admin-account-protection](2026-03/202603281120_admin-account-protection/) - 限制普通管理员修改、停用和删除管理员类账号
- [202603281130_inventory-warning-notify-fix](2026-03/202603281130_inventory-warning-notify-fix/) - 为库存调拨与盘点接入低库存预警消息通知

- [202603281415_missing-feature-completion](2026-03/202603281415_missing-feature-completion/) - 补齐认证前端闭环、客户/仓库维护、订单编辑、库存/报表增强与独立消息中心
