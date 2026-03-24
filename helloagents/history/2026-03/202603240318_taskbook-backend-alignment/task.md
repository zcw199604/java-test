# 任务清单: 任务书对齐的后端功能补齐与架构重构

目录: `helloagents/plan/202603240318_taskbook-backend-alignment/`

---

## 1. 认证与权限重构
- [√] 1.1 在 `backend/pom.xml` 中引入 Apache Shiro 与 Apache POI 依赖，并移除/降级与新方案冲突的认证实现，验证 why.md#需求-apache-shiro-认证与-rbac-权限体系-场景-多角色安全登录
- [√] 1.2 在 `backend/src/main/java/com/example/tobacco/config/` 中实现 `ShiroConfig`、Realm、凭证匹配器与权限过滤链，验证 why.md#需求-apache-shiro-认证与-rbac-权限体系-场景-多角色安全登录，依赖任务1.1
- [√] 1.3 在 `backend/src/main/java/com/example/tobacco/auth/` 中重构登录、登出、个人信息、验证码、忘记密码、重置密码接口与服务，验证 why.md#需求-apache-shiro-认证与-rbac-权限体系-场景-忘记密码与重置密码，依赖任务1.2
- [√] 1.4 在 `backend/src/main/resources/sql/` 中扩展用户、角色、权限、验证码、重置密码、数据范围相关表结构与种子数据，验证 why.md#需求-apache-shiro-认证与-rbac-权限体系-场景-多角色安全登录，依赖任务1.2

## 2. 超级管理员与个人中心
- [√] 2.1 在 `backend/src/main/java/com/example/tobacco/system/` 中补齐用户、角色、权限、数据范围、账号修改/禁用/详情接口，验证 why.md#需求-超级管理员全量后台管理-场景-管理员维护账号与权限，依赖任务1.4
- [√] 2.2 在 `backend/src/main/java/com/example/tobacco/system/` 中新增系统配置、个人中心、个人密码修改接口与服务，验证 why.md#需求-超级管理员全量后台管理-场景-管理员查看系统日志与配置，依赖任务1.3
- [√] 2.3 在 `backend/src/main/java/com/example/tobacco/audit/` 与 `backend/src/main/java/com/example/tobacco/message/` 中实现登录日志、操作日志、站内消息查询与已读接口，验证 why.md#需求-辅助功能与通知-场景-预警消息推送，依赖任务2.1

## 3. 基础资料与主数据扩展
- [√] 3.1 在 `backend/src/main/resources/sql/` 中新增仓库、权限、系统配置等主数据表，并扩展商品、供应商、客户字段，验证 why.md#需求-超级管理员全量后台管理-场景-管理员维护账号与权限
- [√] 3.2 在 `backend/src/main/java/com/example/tobacco/catalog/`、`supplier/`、`customer/` 中补齐商品、品类、供应商、客户的完整 CRUD 与仓库管理接口，验证 why.md#需求-超级管理员全量后台管理-场景-管理员维护账号与权限，依赖任务3.1

## 4. 采购模块完整化
- [√] 4.1 在 `backend/src/main/resources/sql/` 中新增采购需求、采购跟踪、采购审核相关表与字段，验证 why.md#需求-采购管理完整化-场景-采购需求提报到采购入库
- [√] 4.2 在 `backend/src/main/java/com/example/tobacco/purchase/` 中实现采购需求提报、采购单审核、修改、取消、跟踪、分析接口，验证 why.md#需求-采购管理完整化-场景-采购需求提报到采购入库，依赖任务4.1
- [√] 4.3 在 `backend/src/main/java/com/example/tobacco/purchase/` 中实现采购 Excel 模板导出、导入校验与批量导入接口，验证 why.md#需求-采购管理完整化-场景-采购需求提报到采购入库，依赖任务1.1

## 5. 销售模块完整化
- [√] 5.1 在 `backend/src/main/resources/sql/` 中新增销售信息发布、销售审核、应收状态相关表与字段，验证 why.md#需求-销售管理完整化-场景-销售发布到回款跟踪
- [√] 5.2 在 `backend/src/main/java/com/example/tobacco/sales/` 中实现销售信息发布、销售单审核、修改、取消、回款状态查询、业绩统计接口，验证 why.md#需求-销售管理完整化-场景-销售发布到回款跟踪，依赖任务5.1
- [√] 5.3 在 `backend/src/main/java/com/example/tobacco/sales/` 中实现销售 Excel 模板导出、导入校验与批量导入接口，验证 why.md#需求-销售管理完整化-场景-销售发布到回款跟踪，依赖任务1.1

## 6. 库存模块增强
- [√] 6.1 在 `backend/src/main/resources/sql/` 中扩展库存、盘点、预警、异常单据与追溯相关表结构，验证 why.md#需求-库存管理增强-场景-库存预警与盘点追踪
- [√] 6.2 在 `backend/src/main/java/com/example/tobacco/inventory/` 中实现库存 Excel 批量导入导出、出入库/调拨明细、盘点盈亏上报、台账筛选接口，验证 why.md#需求-库存管理增强-场景-库存预警与盘点追踪，依赖任务6.1
- [√] 6.3 在 `backend/src/main/java/com/example/tobacco/inventory/` 与 `message/` 中实现自动预警记录生成与消息通知联动，验证 why.md#需求-辅助功能与通知-场景-预警消息推送，依赖任务6.1

## 7. 统计、追溯与异常审核
- [√] 7.1 在 `backend/src/main/java/com/example/tobacco/report/` 与 `controller/` 中实现采销存汇总、合规追溯、异常单据审核、多维联动图表接口，验证 why.md#需求-统计分析与合规追溯-场景-单据追溯与异常审核，依赖任务4.2
- [√] 7.2 在 `backend/src/main/java/com/example/tobacco/audit/`、`purchase/`、`sales/`、`inventory/` 中打通业务状态写日志、写追溯、写异常记录的统一组件，验证 why.md#需求-统计分析与合规追溯-场景-单据追溯与异常审核，依赖任务2.3

## 8. 兼容层、错误处理与安全检查
- [√] 8.1 在 `backend/src/main/java/com/example/tobacco/common/` 中统一补齐权限拒绝、业务状态错误、导入校验错误的异常处理，验证 why.md#需求-apache-shiro-认证与-rbac-权限体系-场景-多角色安全登录，依赖任务1.3
- [√] 8.2 执行安全检查（按G9: 输入验证、敏感信息处理、权限控制、EHRB风险规避）

## 9. 测试与知识库同步
- [X] 9.1 在 `backend/src/test/java/com/example/tobacco/` 中补充 Shiro 登录、RBAC 权限、采购/销售/库存完整流、日志、消息、Excel、追溯、异常审核集成测试，验证 why.md#需求-统计分析与合规追溯-场景-单据追溯与异常审核，依赖任务7.2
- [√] 9.2 更新 `helloagents/wiki/api.md`、`helloagents/wiki/data.md`、`helloagents/wiki/arch.md`、`helloagents/wiki/modules/*.md` 与 `helloagents/project.md`，同步任务书对齐后的接口、架构和数据模型，依赖任务9.1
- [√] 9.3 更新 `helloagents/CHANGELOG.md` 并在开发实施完成后迁移方案包至 `helloagents/history/`，依赖任务9.2


> 说明: 9.1 已补充集成测试代码，但当前执行环境仅提供 JRE、缺少 JDK 编译器，`mvn test` 无法在本地完成执行验证。
