# 技术方案: MyBatis 迁移后的文档与测试补强收口

## 方案概述
采用“**只补测试与文档，不动业务逻辑**”的方式进行收口。

### 原则
1. **测试优先**：先基于现有实现补断言，不先改业务代码；
2. **文档追平代码**：知识库和方案包以当前真实代码状态为准；
3. **最小改动**：不引入新的 Service/Mapper 重构；
4. **低风险执行**：所有改动应仅影响测试代码与 Markdown 文档。

## 任务拆分

### A. 认证兼容测试补强
重点围绕以下兼容路径追加验证：
- `captcha_records`: `captcha_key/captcha_code/expire_at` 与 `uuid/code/expired_at`
- `user_sessions`: `session_token/expire_at` 与 `token/expired_at`
- `password_reset_records`: `reset_token/expire_at` 与 `token/expired_at`
- 可选附带列：`username`、`role_code`、`used_at`、`last_access_at`

建议方式：
- 若现有 schema 默认不包含新列，可在测试中按需建临时表结构或增加独立兼容测试脚本；
- 断言重点为：登录、登出、忘记密码、重置密码、会话鉴权成功/失败。

### B. Bulletin 测试补强
新增以下测试点：
- `expiredAt` 为空时仍可发布；
- 列表查询中 `createdBy` 存储正确；
- 列表字段格式稳定；
- 多条公告时结果顺序符合 `id desc` 预期。

### C. 文档与方案包修正
需同步以下文档：
- `helloagents/history/index.md`
  - 补齐 `202603290208_mybatis-annotation-migration` 在“按月归档”中的月度列表条目；
- `helloagents/CHANGELOG.md`
  - 补记 `system/report/dashboard/purchase/sales/inventory` 等模块迁移完成状态；
- `helloagents/wiki/arch.md`
  - 修正 ADR 影响模块缺少 `dashboard`；
  - 补充“API Bearer 会话校验主要由 `AuthInterceptor` 承担”的现状描述；
- `helloagents/history/2026-03/202603290208_mybatis-annotation-migration/how.md`
  - 增加“最终落地结果”说明，明确当前已从过渡并存阶段收口到业务代码无 `JdbcTemplate` 直接调用。

## 风险与规避

### 风险1：兼容列测试难以在现有 schema 下复现
- **规避**：单独设计兼容测试上下文，避免污染默认初始化脚本；必要时独立测试类使用显式 SQL 准备数据结构。

### 风险2：文档修正与代码再次偏离
- **规避**：文档修改前先以当前代码与测试报告为唯一依据；修改后再做一次交叉核对。

### 风险3：补测试时误伤现有初始化数据
- **规避**：测试数据使用唯一前缀/时间戳；必要时局部增删，不改公共基线脚本。

## 验证策略
- 运行新增/更新后的目标测试类；
- 执行 `mvn test` 与 `mvn -q -DskipTests compile`；
- 人工复核文档与代码状态是否一致。
