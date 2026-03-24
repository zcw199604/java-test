# 烟草采销存协同管理平台

> 本文件包含项目级别的核心信息。详细的模块文档见 `modules/` 目录。

## 1. 项目概述

### 目标与背景
基于毕业设计任务书，项目已实现一个采用 Vue + Spring Boot + MySQL 的前后端分离采销存协同平台，可用于登录演示、基础管理、采购、库存、销售和报表联调。

### 范围
- **范围内:** 登录认证、用户角色、品类商品、供应商客户、采购到货与入库、库存预警、销售回款、首页概览、CSV 报表导出与趋势展示
- **范围外:** 复杂审批流、生产级权限点细粒度控制、正式部署编排

## 2. 模块索引

| 模块名称 | 职责 | 状态 | 文档 |
|---------|------|------|------|
| backend | REST API 与业务实现 | ✅稳定 | [backend](modules/backend.md) |
| frontend | Vue 页面与交互 | ✅稳定 | [frontend](modules/frontend.md) |
| procurement | 采购业务 | ✅稳定 | [procurement](modules/procurement.md) |
| inventory | 库存业务 | ✅稳定 | [inventory](modules/inventory.md) |
| sales | 销售业务 | ✅稳定 | [sales](modules/sales.md) |
| admin | 系统管理与报表 | ✅稳定 | [admin](modules/admin.md) |

## 3. 快速链接
- [技术约定](../project.md)
- [架构设计](arch.md)
- [API 手册](api.md)
- [数据模型](data.md)
- [变更历史](../history/index.md)
