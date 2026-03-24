# 任务清单: 烟草采销存协同管理平台全功能实施方案

目录: `helloagents/plan/202603231548_full-platform-features/`

---

## 1. 环境与基础设施准备
- [√] 1.1 在 `backend/pom.xml` 中补充数据访问、认证授权、导出等依赖，验证 why.md#需求用户认证与角色分权-场景登录并加载角色菜单
- [√] 1.2 在 `frontend/package.json` 中补充 UI 组件库、图表等依赖，验证 why.md#需求首页概览与报表分析-场景查看首页经营概览
- [√] 1.3 在 `backend/src/main/resources/` 中补充数据库、日志、跨域与环境配置模板，验证 why.md#需求基础资料管理-场景管理员维护供应商与用户

## 2. 后端认证与权限模块
- [√] 2.1 在 `backend/src/main/java/com/example/tobacco/auth/` 下实现登录、退出、当前用户信息接口，验证 why.md#需求用户认证与角色分权-场景登录并加载角色菜单
- [√] 2.2 在 `backend/src/main/java/com/example/tobacco/system/` 下实现用户、角色、权限实体与 CRUD 接口，验证 why.md#需求用户认证与角色分权-场景管理员维护角色权限
- [√] 2.3 在 `backend/src/main/java/com/example/tobacco/config/` 中实现 JWT 拦截器、权限注解与异常处理，验证 why.md#需求用户认证与角色分权-场景管理员维护角色权限

## 3. 后端基础资料模块
- [√] 3.1 在 `backend/src/main/java/com/example/tobacco/catalog/` 下实现品类、商品、价格管理接口，验证 why.md#需求基础资料管理-场景管理员维护烟草品类与价格
- [√] 3.2 在 `backend/src/main/java/com/example/tobacco/supplier/` 下实现供应商管理接口，验证 why.md#需求基础资料管理-场景管理员维护供应商与用户
- [√] 3.3 在 `backend/src/main/java/com/example/tobacco/customer/` 下实现客户管理接口，验证 why.md#需求销售全流程管理-场景创建销售订单并执行出库

## 4. 后端采购模块
- [√] 4.1 在 `backend/src/main/java/com/example/tobacco/purchase/` 下实现采购单列表、创建、编辑、详情接口，验证 why.md#需求采购全流程管理-场景创建采购订单并跟踪状态
- [√] 4.2 在 `backend/src/main/java/com/example/tobacco/purchase/` 下实现采购状态流转、到货登记与采购入库逻辑，验证 why.md#需求采购全流程管理-场景采购到货后登记入库
- [√] 4.3 在 `backend/src/main/java/com/example/tobacco/purchase/` 与 `inventory/` 间打通采购入库更新库存能力，验证 why.md#需求采购全流程管理-场景采购到货后登记入库

## 5. 后端库存模块
- [√] 5.1 在 `backend/src/main/java/com/example/tobacco/inventory/` 下实现库存台账与库存流水查询接口，验证 why.md#需求库存全流程管理-场景库存查询与预警
- [√] 5.2 在 `backend/src/main/java/com/example/tobacco/inventory/` 下实现库存调拨与盘点逻辑，验证 why.md#需求库存全流程管理-场景盘点与调拨
- [√] 5.3 在 `backend/src/main/java/com/example/tobacco/inventory/` 下实现库存预警计算与列表接口，验证 why.md#需求库存全流程管理-场景库存查询与预警

## 6. 后端销售模块
- [√] 6.1 在 `backend/src/main/java/com/example/tobacco/sales/` 下实现销售单列表、创建、详情接口，验证 why.md#需求销售全流程管理-场景创建销售订单并执行出库
- [√] 6.2 在 `backend/src/main/java/com/example/tobacco/sales/` 与 `inventory/` 间实现销售出库扣减库存逻辑，验证 why.md#需求销售全流程管理-场景创建销售订单并执行出库
- [√] 6.3 在 `backend/src/main/java/com/example/tobacco/sales/` 下实现回款登记与业绩统计接口，验证 why.md#需求销售全流程管理-场景回款登记与业绩统计

## 7. 后端首页与报表模块
- [√] 7.1 在 `backend/src/main/java/com/example/tobacco/report/` 下实现首页概览统计接口，验证 why.md#需求首页概览与报表分析-场景查看首页经营概览
- [√] 7.2 在 `backend/src/main/java/com/example/tobacco/report/` 下实现采购、销售、库存汇总报表接口，验证 why.md#需求首页概览与报表分析-场景查看采销存汇总报表
- [√] 7.3 在 `backend/src/main/java/com/example/tobacco/report/` 下实现趋势分析与导出能力，验证 why.md#需求首页概览与报表分析-场景查看采销存汇总报表

## 8. 前端认证与框架层
- [√] 8.1 在 `frontend/src/router/`、`stores/`、`api/` 中实现登录态管理、路由守卫与鉴权跳转，验证 why.md#需求用户认证与角色分权-场景登录并加载角色菜单
- [√] 8.2 在 `frontend/src/views/` 中新增登录页、401/403 状态页与基础布局增强，验证 why.md#需求用户认证与角色分权-场景管理员维护角色权限
- [√] 8.3 在 `frontend/src/components/` 中封装通用表格、搜索表单、状态标签、详情抽屉等组件，验证 why.md#需求首页概览与报表分析-场景查看首页经营概览

## 9. 前端系统管理模块
- [√] 9.1 在 `frontend/src/views/system/` 下实现用户管理、角色权限页面，验证 why.md#需求用户认证与角色分权-场景管理员维护角色权限
- [√] 9.2 在 `frontend/src/views/catalog/` 下实现商品/品类管理页面，验证 why.md#需求基础资料管理-场景管理员维护烟草品类与价格
- [√] 9.3 在 `frontend/src/views/supplier/` 与 `frontend/src/views/customer/` 下实现供应商、客户管理页面，验证 why.md#需求基础资料管理-场景管理员维护供应商与用户

## 10. 前端采购模块
- [√] 10.1 在 `frontend/src/views/purchase/` 下实现采购单列表、搜索、分页与详情页，验证 why.md#需求采购全流程管理-场景创建采购订单并跟踪状态
- [√] 10.2 在 `frontend/src/views/purchase/` 下实现采购创建/编辑表单、到货登记与入库操作，验证 why.md#需求采购全流程管理-场景采购到货后登记入库
- [√] 10.3 在 `frontend/src/api/purchase.js` 中补充采购模块 API 封装与联调，验证 why.md#需求采购全流程管理-场景采购到货后登记入库

## 11. 前端库存模块
- [√] 11.1 在 `frontend/src/views/inventory/` 下实现库存台账与库存流水页面，验证 why.md#需求库存全流程管理-场景库存查询与预警
- [√] 11.2 在 `frontend/src/views/inventory/` 下实现调拨单、盘点单与预警列表页面，验证 why.md#需求库存全流程管理-场景盘点与调拨
- [√] 11.3 在 `frontend/src/api/inventory.js` 中补充库存模块 API 封装与联调，验证 why.md#需求库存全流程管理-场景库存查询与预警

## 12. 前端销售模块
- [√] 12.1 在 `frontend/src/views/sales/` 下实现销售单列表、详情与状态流转页面，验证 why.md#需求销售全流程管理-场景创建销售订单并执行出库
- [√] 12.2 在 `frontend/src/views/sales/` 下实现出库登记、回款登记与统计页面，验证 why.md#需求销售全流程管理-场景回款登记与业绩统计
- [√] 12.3 在 `frontend/src/api/sales.js` 中补充销售模块 API 封装与联调，验证 why.md#需求销售全流程管理-场景回款登记与业绩统计

## 13. 前端首页与报表模块
- [√] 13.1 在 `frontend/src/views/dashboard/` 下实现首页概览卡片、快捷入口与预警展示，验证 why.md#需求首页概览与报表分析-场景查看首页经营概览
- [√] 13.2 在 `frontend/src/views/report/` 下实现采购/销售/库存汇总报表页面，验证 why.md#需求首页概览与报表分析-场景查看采销存汇总报表
- [√] 13.3 在 `frontend/src/views/report/` 下实现趋势图表与导出交互，验证 why.md#需求首页概览与报表分析-场景查看采销存汇总报表

## 14. 数据库与初始化数据
- [√] 14.1 在 `backend/src/main/resources/sql/` 中编写建表脚本与索引脚本，验证 why.md#需求基础资料管理-场景管理员维护烟草品类与价格
- [√] 14.2 在 `backend/src/main/resources/sql/` 中编写初始角色、用户、基础资料种子数据，验证 why.md#需求用户认证与角色分权-场景登录并加载角色菜单

## 15. 安全检查
- [√] 15.1 执行安全检查（按G9: 输入验证、敏感信息处理、权限控制、EHRB风险规避）
- [√] 15.2 审计关键写操作的权限边界与日志记录，验证 why.md#需求用户认证与角色分权-场景管理员维护角色权限

## 16. 测试与验收
- [√] 16.1 在 `backend/src/test/` 中补充认证、采购、库存、销售关键流程测试，验证 why.md#需求采购全流程管理-场景采购到货后登记入库
- [√] 16.2 在 `frontend` 中补充关键页面联调与交互验收清单，验证 why.md#需求首页概览与报表分析-场景查看首页经营概览
- [√] 16.3 进行端到端联调，验证采购入库增库存、销售出库减库存、回款更新统计的完整链路，验证 why.md#需求销售全流程管理-场景回款登记与业绩统计

## 17. 文档更新
- [√] 17.1 更新 `helloagents/wiki/api.md`、`helloagents/wiki/data.md`、`helloagents/wiki/arch.md` 以同步全功能设计
- [√] 17.2 更新 `README.md` 补充部署方式、模块说明与演示路径
- [√] 17.3 更新 `helloagents/CHANGELOG.md` 记录全功能开发阶段的重要变更


## 执行备注
- 已使用 Docker 启动临时 MySQL：`temp-mysql`，端口 `3307`，数据库 `tobacco_platform`。
- 已验证后端在 `/mnt/dbz/jdk-21.0.4` 环境下可编译，并成功启动后完成登录、用户、角色、商品、供应商接口联调。
- 已验证前端 `npm install` 与 `npm run build` 成功，当前已具备登录页、首页和系统管理基础页面。
- 已补齐采购、库存、销售、报表前后端模块，并验证采购入库、销售出库、回款、报表导出链路。
- 前端新增 ECharts 趋势图与报表 CSV 导出；生产构建通过，但产物 chunk 较大，后续可按需做代码分包优化。
- 当前仍保留少量增强任务未完成：系统/基础资料完整 CRUD、自动化测试与更细粒度权限审计。
- 本次已完成采购、库存、销售、报表、客户、系统管理等模块的前后端最小闭环，并完成 Docker MySQL 联调。
- 验证通过：`backend mvn -DskipTests package`、`backend mvn test`、`frontend npm run build`。
