# 任务清单: JdbcTemplate 向 MyBatis 注解 SQL 渐进迁移

> 状态: 已完成
> 执行原则: 先测试、后替换；小步提交；每阶段可回归

## 1. 基线梳理与迁移准备
- [√] 1.1 盘点全部 `JdbcTemplate` 使用点，形成模块迁移清单
- [√] 1.2 引入 MyBatis 依赖、Mapper 扫描配置与基础约定
- [√] 1.3 定义 `mapper` 包结构、命名规范、Provider 使用规范
- [√] 1.4 明确“注解 SQL 优先、XML 例外”的准入标准并写入知识库

## 2. 测试先行补齐
- [√] 2.1 扩展 `PlatformIntegrationTest` 与 `BackendCompatibilityIntegrationTest`，覆盖登录/登出日志、兼容字段、金额精度、仓库/客户/采购/销售关键链路
- [√] 2.2 新增认证模块集成测试，覆盖登录失败、禁用账号、验证码、会话、忘记密码、重置密码
> 备注: 已新增 `AuthIntegrationTest`，覆盖验证码失败、禁用账号、忘记/重置密码、会话失效与个人信息查询。
- [√] 2.3 新增系统与基础资料测试，覆盖用户、角色、权限、仓库、客户、供应商、商品关键 CRUD 与筛选
> 备注: `PlatformIntegrationTest` 已覆盖用户、角色、权限、仓库、客户、供应商、商品关键 CRUD 与筛选链路。
- [√] 2.4 新增采购、销售、库存模块测试，覆盖状态流转、库存联动、消息通知、追溯与操作日志
> 备注: 平台级集成测试已覆盖采购/销售编辑与审核、库存联动、消息已读，以及登录/操作日志关键链路。
- [√] 2.5 新增报表与消息测试，覆盖 PSI 汇总、联动分析、消息已读与筛选
> 备注: `PlatformIntegrationTest` 已覆盖 PSI 汇总与联动分析，`BackendCompatibilityIntegrationTest` 已覆盖消息已读。
- [-] 2.6 对复杂动态 SQL 预备 Provider 测试基类或断言工具
> 备注: 未单独建立 Provider 测试基类，改以认证/平台级集成测试直接覆盖动态 SQL 与兼容字段分支。
- [√] 2.7 在“未引入业务替换”前先执行全量测试，确认基线稳定

## 3. 低风险模块迁移（试点）
- [√] 3.1 迁移 `message` 模块到 MyBatis 注解 Mapper
- [√] 3.2 迁移 `audit` 模块到 MyBatis 注解 Mapper
- [√] 3.3 迁移 `supplier` / `customer` / `catalog` 模块到 MyBatis 注解 Mapper
- [√] 3.4 完成阶段回归，确认接口行为与日志结果一致

## 4. 中风险模块迁移
- [√] 4.1 迁移 `system` 模块的用户、角色、权限、仓库与配置查询/写入
- [√] 4.2 迁移 `report` / `dashboard` 查询逻辑，验证聚合 SQL 与性能
- [√] 4.3 复核仓库 `code` 兼容逻辑与动态筛选 SQL 的注解/Provider 可维护性
- [√] 4.4 完成阶段回归，确认权限与配置行为不变

## 5. 高风险业务模块迁移
- [√] 5.1 迁移 `purchase` 模块，保持事务、日志、追溯、库存联动行为不变
- [√] 5.2 迁移 `sales` 模块，保持金额精度、回款、日志与出库联动行为不变
- [√] 5.3 迁移 `inventory` 模块，保持调拨、盘点、预警与消息通知行为不变
> 备注: 已迁移为 `InventoryMapper` 注解 SQL，并通过库存调拨、盘点、预警相关回归。
- [√] 5.4 对高风险业务阶段执行全量回归与重点人工冒烟
> 备注: 已完成 `purchase`/`sales`/`inventory` 迁移后的自动化回归与页面级人工冒烟。

## 6. 认证与兼容层迁移
- [√] 6.1 迁移 `AuthService` 到 Mapper + 注解 SQL / Provider
> 备注: 已新增 `AuthMapper` / `AuthSqlProvider`，认证、验证码、会话、密码重置改为注解 SQL + Provider。
- [√] 6.2 迁移 `ShiroRealm` 的用户认证与权限读取逻辑
> 备注: 已改为通过 `AuthMapper` 读取用户与权限，保持禁用账号与密码校验行为不变。
- [√] 6.3 保留验证码、会话表、密码重置表的兼容字段行为
> 备注: 兼容字段解析现由 `AuthMapper` + Provider 保持，覆盖 `uuid/code`、`token/expired_at` 等历史字段命名。
- [√] 6.4 回归登录、登出、忘记密码、重置密码、权限校验、日志留痕
> 备注: 已通过 `AuthIntegrationTest`、`BackendCompatibilityIntegrationTest` 与 `PlatformIntegrationTest` 完成回归。

## 7. 清理与收口
- [√] 7.1 删除已完成迁移模块中的 `JdbcTemplate` 依赖与冗余 SQL
> 备注: 已完成认证拦截器与公告服务迁移，后端业务代码中不再保留 `JdbcTemplate` 直接调用。
- [√] 7.2 清理临时适配代码与过渡分支逻辑
> 备注: 已清理认证层、拦截器与公告服务中的过渡 JDBC 逻辑，统一收口到 Mapper/Provider。
- [√] 7.3 更新 `project.md`、`wiki/arch.md` 与 `CHANGELOG.md`，说明数据访问层进入 MyBatis 渐进迁移阶段
- [√] 7.4 更新 `CHANGELOG.md`
- [√] 7.5 执行 `mvn test`、`mvn -DskipTests compile` 与必要人工联调，确认未引入新问题

## 8. 交付与归档
- [√] 8.1 汇总迁移例外清单（若有 XML Mapper，需说明原因）
> 备注: 本轮未引入 XML Mapper，全部采用注解 SQL / Provider 完成迁移。
- [√] 8.2 迁移方案包至 `helloagents/history/YYYY-MM/`
- [√] 8.3 更新 `helloagents/history/index.md`

## 执行总结
- 本轮已完成 MyBatis 基础设施、低/中/高风险业务模块以及认证兼容层的注解迁移，新增 `AuthMapper` / `AuthSqlProvider` 与 `BulletinMapper`，后端业务数据访问已统一收口到 Mapper。
- 当前项目已移除业务代码中的 `JdbcTemplate` 直接调用，认证兼容字段（验证码、会话、密码重置）仍保持历史命名兼容。
- 回归验证已通过：`mvn test`；编译验证已通过：`mvn -DskipTests compile`。
