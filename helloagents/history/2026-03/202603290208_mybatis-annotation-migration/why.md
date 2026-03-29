# 变更提案: JdbcTemplate 向 MyBatis 注解 SQL 渐进迁移

## 需求背景

当前烟草采销存协同管理平台后端以 **Spring JDBC + JdbcTemplate + 原生 SQL** 作为数据库访问主方式。虽然该方案已经支撑现有认证、系统管理、采销存、日志、消息、报表等模块运行，但 SQL 分散在多个 Service / Realm 中，随着兼容逻辑、动态字段适配、日志留痕和业务规则增加，后续维护成本持续上升。

用户本次明确要求：

1. 先整理一套完整的修改方案包，而不是直接改造；
2. 数据库访问改造目标为 **MyBatis**；
3. **优先使用注解方式书写 SQL**，避免优先引入 XML Mapper；
4. **先完善所有相关测试用例**，再启动替换；
5. 替换完成后要保证 **不引入新的问题**，即迁移必须可回归、可验证、可分阶段推进。

## 变更目标

1. 将当前分散的 JdbcTemplate 数据访问逐步迁移为 MyBatis 注解 Mapper；
2. 在迁移前补齐核心数据库交互链路的自动化测试，形成“行为基线”；
3. 对迁移采取分模块、可回归、可回滚的渐进策略，而非一次性替换；
4. 确保认证兼容逻辑、历史字段兼容、业务状态机、日志记录等关键行为在迁移后保持一致；
5. 为后续继续扩展 DAO / Mapper 层结构提供统一规范。

## 当前代码现状

### 技术与规模
- 后端核心栈：Java / Spring Boot 2.7 / Spring JDBC / MySQL 8
- 后端源码文件约：73 个
- 前端源码文件约：76 个
- 当前知识库明确记录的数据访问方式仍为 **Spring JDBC**

### 当前数据库访问分布
基于当前代码扫描，`JdbcTemplate` 已分布在以下关键模块：

- `auth/AuthService.java`
- `auth/ShiroRealm.java`
- `audit/AuditService.java`
- `catalog/CatalogService.java`
- `customer/CustomerService.java`
- `inventory/InventoryService.java`
- `message/MessageService.java`
- `purchase/PurchaseService.java`
- `report/ReportService.java`
- `sales/BulletinService.java`
- `sales/SalesService.java`
- `supplier/SupplierService.java`
- `system/SystemService.java`
- `service/DashboardService.java`

这说明迁移并非局部微调，而是跨认证、基础资料、业务单据、审计日志和报表查询的 **中大型技术重构**。

### 当前测试现状
目前后端测试仅有：

- `PlatformIntegrationTest`
- `BackendCompatibilityIntegrationTest`

测试总量偏少，尚不足以覆盖全部 Mapper 迁移风险，尤其缺少：

- 认证失败/成功/登出日志校验；
- 系统管理 CRUD 与仓库兼容逻辑测试；
- 采购 / 销售状态机细粒度测试；
- 库存变动、消息通知、报表聚合边界测试；
- 针对“兼容字段存在/不存在”的回归测试。

## 影响范围

- **后端依赖层:** `pom.xml`、MyBatis 配置、Mapper 扫描配置
- **数据访问层:** 认证、系统管理、基础资料、日志消息、采购、销售、库存、报表
- **测试层:** 集成测试扩充，必要时补充分模块测试基类与数据断言工具
- **知识库:** `project.md`、`wiki/arch.md`、`wiki/api.md`、模块文档、CHANGELOG

## 成功标准

1. 在迁移开始前，新增并通过一批覆盖关键链路的自动化测试；
2. MyBatis 引入后，业务接口对外行为保持不变；
3. 所有原有测试 + 新增测试在迁移各阶段持续通过；
4. 最终主要数据库访问路径由 `Mapper + 注解 SQL` 承载；
5. 对于不适合注解 SQL 的复杂场景，必须在方案中明确例外标准，而不是无边界扩展。

## 范围边界

### 本次方案范围内
- 定义测试先行策略
- 设计 MyBatis 注解化迁移路径
- 明确模块拆分顺序与风险控制
- 明确代码组织规范、验证策略、回滚策略

### 本次方案范围外
- 本轮不直接实施大规模代码替换
- 本轮不引入新的数据库中间件或 ORM 框架组合
- 本轮不调整前端接口协议
- 本轮不默认采用 XML Mapper 全量替代注解方式

## 核心场景

### 需求: 认证与会话兼容迁移
**模块:** auth / audit
需要保证登录、验证码、会话校验、忘记密码、重置密码、登录/操作日志在迁移到 Mapper 后行为完全一致。

### 需求: 采销存核心业务迁移
**模块:** purchase / sales / inventory
需要保证采购、销售、库存的状态流转、数量计算、日志追溯与消息通知不被迁移破坏。

### 需求: 测试基线先行
**模块:** backend test
需要先把关键路径测试补齐，之后才能逐模块替换 JdbcTemplate，以避免“改完才发现行为漂移”。

### 需求: 注解 SQL 规范化
**模块:** mapper 层整体
需要统一 MyBatis 注解写法、参数命名、结果映射、动态 SQL 使用边界，降低后续维护成本。
