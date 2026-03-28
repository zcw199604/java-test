# backend

## 目的
提供任务书对齐的后端 API、认证权限、采销存流程与审计追溯能力。

## 模块概述
- **职责:** REST API、Shiro 认证与密码哈希、Spring JDBC 数据访问、Excel 导入导出、日志消息与业务服务编排
- **状态:** 🚧开发中
- **最后更新:** 2026-03-28

## 规范
- 所有 `/api/*` 业务接口默认要求登录。
- 敏感操作需要写入操作日志与追溯记录。

## 最新变更
- 商品接口 `GET /api/products` 支持 `keyword`、`status`、`category` 过滤参数。
- 供应商接口 `GET /api/suppliers` 支持 `keyword`、`status` 过滤参数。
- `SpaForwardController` 已补齐 `/catalog/products` 与 `/supplier/list` 的 SPA 回退路由。
