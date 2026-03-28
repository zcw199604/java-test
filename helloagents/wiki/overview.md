# 烟草采销存协同管理平台

> 本文件包含项目级别的核心信息。详细的模块文档见 `modules/` 目录。

## 1. 项目概述

### 目标与背景
基于毕业设计任务书，项目已从“最小演示闭环”逐步升级为“任务书对齐”的采销存协同平台：前端已升级为企业后台工作台风格，后端重点补齐认证权限、日志审计、系统配置、Excel 导入导出、采购/销售/库存完整流程、合规追溯与异常监控能力。

### 范围
- **范围内:** 登录认证、验证码、忘记/重置密码、RBAC、动态菜单、暗黑模式、用户/角色/权限/配置/仓库、采购需求与采购单全流程、销售信息发布与销售单全流程、库存维护与预警、消息通知、日志审计、采销存统计、追溯与异常单据、Excel 批处理
- **范围外:** 短信/邮件真实发送、生产级分布式部署、多实例会话同步、完整移动端原生体验

## 2. 模块索引

| 模块名称 | 职责 | 状态 | 文档 |
|---------|------|------|------|
| backend | REST API 与业务实现 | 🚧开发中 | [backend](modules/backend.md) |
| frontend | Vue 页面、布局与权限交互 | ✅稳定 | [frontend](modules/frontend.md) |
| procurement | 采购业务 | 🚧开发中 | [procurement](modules/procurement.md) |
| inventory | 库存业务 | 🚧开发中 | [inventory](modules/inventory.md) |
| sales | 销售业务 | 🚧开发中 | [sales](modules/sales.md) |
| admin | 系统管理、权限、日志与报表 | 🚧开发中 | [admin](modules/admin.md) |


## 3. 标准启动说明
- 先在 `frontend/` 执行 `npm install && npm run build`，生成静态资源。
- 再在 `backend/` 执行 `mvn spring-boot:run`，由 Spring Boot 统一提供页面与 `/api/*`。
- 常规联调、验收、冒烟检查统一访问后端端口，不额外依赖 `frontend` dev server。

## 4. 快速链接
- [技术约定](../project.md)
- [架构设计](arch.md)
- [API 手册](api.md)
- [数据模型](data.md)
- [变更历史](../history/index.md)
