# backend

## 目的
提供平台后端 API、认证授权、采销存业务编排、审计追溯与 Excel 处理能力。

## 模块概述
- **职责:** REST API、Shiro 认证、Bearer 会话校验、MyBatis 数据访问、静态资源托管、Excel 导入导出、日志消息与业务服务编排
- **状态:** ✅可用，持续迭代
- **最后更新:** 2026-03-29

## 规范
- 所有 `/api/*` 业务接口默认要求登录，开放接口仅限认证辅助与健康检查
- 敏感业务动作必须写入 `operation_logs`，关键链路必须写入 `trace_records`
- 控制器默认返回 `ApiResponse`，仅文件导出接口直接返回 `ResponseEntity<byte[]>`

## 当前实现
- 认证链路由 `AuthService` + `AuthInterceptor` + `user_sessions` 落地，真实运行时不依赖 JWT
- 数据访问已收口到 `mapper/` 包，使用 MyBatis 注解与 `SqlProvider`
- `SpaForwardController` 负责常用业务页的 SPA 回退
- 后端启动时会执行 `schema.sql` 与 `data.sql`，重置演示数据

## 最新变更
- 新增 `GET /api/dashboard/sales-history`，支持驾驶舱查看近 7 天 / 30 天重点烟品历史销售对比
- 采购入库、销售出库、库存调拨、库存盘点均已切换到仓库维度库存模型
- `POST /api/purchases/{id}/inbound` 与 `POST /api/sales/{id}/outbound` 现强制要求 `warehouseId`
- 用户删除、仓库详情、仓库启停用等接口已补齐
- 管理员角色已拆分为 `SUPER_ADMIN` 与 `ADMIN`，并补齐管理员账号保护
- `InventoryService` 在调拨与盘点后同样触发库存预警消息

## 注意事项
- `JwtTokenUtil` 目前仅保留兼容壳，调用会抛出不支持异常
- 采购需求兼容接口存在，但当前没有独立 `purchase_requisitions` 表
- 运行期真实接口清单必须以 Controller 映射为准，不能沿用早期 CSV / JDBC 版本描述
