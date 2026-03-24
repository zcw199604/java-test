# 烟草采销存协同管理平台

> 本文件包含项目级别的核心信息。详细的模块文档见 `modules/` 目录。

## 1. 项目概述

### 目标与背景
基于毕业设计任务书，项目已实现一个采用 Vue + Spring Boot + MySQL 的前后端分离采销存协同平台。当前前端已升级为企业后台工作台风格，覆盖登录、驾驶舱、个人中心、管理员、采购、销售、库存、统计追溯等页面。

### 范围
- **范围内:** 登录认证、用户角色、动态菜单、暗黑模式、商品/供应商/客户基础资料、采购到货与入库、库存预警与盘点、销售回款、首页概览、CSV 报表导出、经营趋势与合规追溯
- **范围外:** 复杂审批流、生产级细粒度按钮权限、正式部署编排、完整移动端原生体验

## 2. 模块索引

| 模块名称 | 职责 | 状态 | 文档 |
|---------|------|------|------|
| backend | REST API 与业务实现 | ✅稳定 | [backend](modules/backend.md) |
| frontend | Vue 页面、布局与权限交互 | ✅稳定 | [frontend](modules/frontend.md) |
| procurement | 采购订单、入库与分析 | ✅稳定 | [procurement](modules/procurement.md) |
| inventory | 库存总览、流水、盘点与台账 | ✅稳定 | [inventory](modules/inventory.md) |
| sales | 销售订单、出库、回款与绩效 | ✅稳定 | [sales](modules/sales.md) |
| admin | 超级管理员与统计追溯入口 | ✅稳定 | [admin](modules/admin.md) |

## 3. 快速链接
- [技术约定](../project.md)
- [架构设计](arch.md)
- [API 手册](api.md)
- [数据模型](data.md)
- [变更历史](../history/index.md)
