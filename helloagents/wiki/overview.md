# 烟草采销存协同管理平台

> 本文件包含项目级别的核心信息。详细的模块文档见 `modules/` 目录。

## 1. 项目概述

### 目标与背景
项目已从“最小演示闭环”升级为“任务书对齐”的采销存协同平台后端，重点补齐认证权限、日志审计、系统配置、Excel 导入导出、采购/销售/库存完整流程、合规追溯与异常监控能力。

### 范围
- **范围内:** 登录认证、验证码、忘记/重置密码、RBAC、用户/角色/权限/配置/仓库、采购需求与采购单全流程、销售信息发布与销售单全流程、库存维护与预警、消息通知、日志审计、采销存统计、追溯与异常单据、Excel 批处理
- **范围外:** 前端响应式细节、短信/邮件真实发送、分布式部署与多实例会话同步

## 2. 模块索引

| 模块名称 | 职责 | 状态 | 文档 |
|---------|------|------|------|
| backend | REST API 与业务实现 | 🚧开发中 | [backend](modules/backend.md) |
| procurement | 采购业务 | 🚧开发中 | [procurement](modules/procurement.md) |
| inventory | 库存业务 | 🚧开发中 | [inventory](modules/inventory.md) |
| sales | 销售业务 | 🚧开发中 | [sales](modules/sales.md) |
| admin | 系统管理、权限、日志与报表 | 🚧开发中 | [admin](modules/admin.md) |
| frontend | Vue 页面与交互 | ✅稳定 | [frontend](modules/frontend.md) |

## 3. 快速链接
- [技术约定](../project.md)
- [架构设计](arch.md)
- [API 手册](api.md)
- [数据模型](data.md)
- [变更历史](../history/index.md)
