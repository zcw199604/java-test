# 烟草采销存协同管理平台

> 本文件记录当前项目级 SSOT。详细模块说明见 `modules/` 目录，接口和数据结构以代码与 SQL 初始化脚本为准。

## 1. 项目概述

### 目标与背景
项目面向毕业设计与业务演示场景，提供一个可运行的烟草采销存协同平台。当前代码已覆盖认证权限、系统管理、基础资料、采购、销售、库存、日志消息、合规追溯、异常审核和报表导出等核心能力。

### 当前交付口径
- 前端采用 Vue 单页应用，构建后由 Spring Boot 统一托管
- 鉴权采用 `Authorization: Bearer <token>` + `user_sessions` 会话表，不再依赖 JWT 作为真实运行时方案
- 后端启动会自动执行 `schema.sql` 与 `data.sql`，加载并重置演示数据

### 范围
- **范围内:** 登录验证码、忘记/重置密码、RBAC、动态菜单、暗黑模式、账号/角色/权限/配置/仓库、商品/品类/供应商/客户、采购全流程、销售全流程、多仓库存、消息通知、日志审计、合规追溯、异常单据审核、Excel 导入导出
- **范围外:** 短信/邮件真实发送、生产级分布式部署、多实例会话同步、移动端原生应用

## 2. 模块索引

| 模块名称 | 职责 | 状态 | 文档 |
|---------|------|------|------|
| backend | 后端 API、认证、数据访问、静态托管与导入导出 | ✅可用 | [backend](modules/backend.md) |
| frontend | Vue 工作台、动态路由、权限交互、图表与页面编排 | ✅可用 | [frontend](modules/frontend.md) |
| procurement | 采购单创建、编辑、审核、到货、入库、导入与追溯 | ✅可用 | [procurement](modules/procurement.md) |
| inventory | 多仓库存、调拨、盘点、预警、流水与台账 | ✅可用 | [inventory](modules/inventory.md) |
| sales | 销售信息发布、销售单、出库、回款、统计与导入 | ✅可用 | [sales](modules/sales.md) |
| admin | 账号权限、基础资料、日志、配置、异常审核与报表入口 | ✅可用 | [admin](modules/admin.md) |

## 3. 标准启动说明
- 先在 `frontend/` 执行 `npm install && npm run build`
- 再在 `backend/` 执行 `mvn spring-boot:run`
- 默认统一访问 `http://localhost:8080`
- `http://localhost:5173` 仅作为前端专项调试入口

## 4. 文档口径
- 当前接口清单以 `backend/src/main/java/**/Controller.java` 为准
- 当前数据模型以 `backend/src/main/resources/sql/schema.sql` 与 `data.sql` 为准
- 当前前端菜单与页面入口以 `frontend/src/router/route-map.ts` 为准

## 5. 快速链接
- [技术约定](../project.md)
- [架构设计](arch.md)
- [API 手册](api.md)
- [数据模型](data.md)
- [变更历史](../history/index.md)
