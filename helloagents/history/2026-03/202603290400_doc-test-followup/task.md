# 任务清单: MyBatis 迁移后的文档与测试补强收口

> 状态: 已完成
> 范围限制: 仅补测试与文档；不改业务逻辑

## 1. 认证兼容测试补强
- [√] 1.1 设计兼容字段测试策略，确认默认 schema 外的补测方式
> 备注: 默认 schema 中旧列仍为非空主路径，本轮采用“集成测试覆盖默认运行链路 + Provider 单元测试覆盖主兼容列 SQL 选择”的组合策略。
- [√] 1.2 补充登录/登出/忘记密码/重置密码在历史列名路径下的测试
> 备注: `AuthIntegrationTest` 持续覆盖默认历史列名链路；`AuthSqlProviderTest` 补充 `captcha_key` / `session_token` / `reset_token` 的 SQL 生成断言。
- [√] 1.3 补充会话鉴权在附加兼容列存在场景下的测试
> 备注: 通过 `AuthSqlProviderTest` 验证 `username` / `role_code` / `expire_at` 等兼容列场景下的查询与插入 SQL 结构。

## 2. Bulletin 测试补强
- [√] 2.1 补充 `expiredAt` 为空分支测试
- [√] 2.2 补充 `createdBy` 持久化与列表字段断言
- [√] 2.3 补充多条公告排序断言
> 备注: `BulletinIntegrationTest` 已新增空 `expiredAt`、`createdBy`、`createdAt` 与 `id desc` 顺序断言。

## 3. 文档与方案包修正
- [√] 3.1 修正 `helloagents/history/index.md` 的月度归档列表
- [√] 3.2 修正 `helloagents/CHANGELOG.md` 的迁移范围记录
- [√] 3.3 修正 `helloagents/wiki/arch.md` 的 ADR/鉴权描述
- [√] 3.4 为 `202603290208_mybatis-annotation-migration/how.md` 补充最终落地说明

## 4. 验证与收口
- [√] 4.1 运行相关测试并确认无回归
> 备注: 已通过 `mvn test`（11 tests, 0 failure）与 `mvn -q -DskipTests compile`。
- [√] 4.2 更新任务状态并迁移方案包至历史记录

## 执行备注
- 已新增 `AuthSqlProviderTest`，用于在不改业务逻辑的前提下补足主兼容列路径的 SQL 断言。
- 已新增 `BulletinIntegrationTest` 的公告边界覆盖，保证空过期时间与倒序列表行为可回归。
- 已同步修正文档与迁移方案包描述，使其与当前代码状态一致。
