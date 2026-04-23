# 任务清单

## 1. 销售出库前端闭环
- [√] 1.1 为销售模块新增独立销售出库页面与路由，支持从订单列表跳转并读取订单上下文
- [√] 1.2 在销售出库页面接入仓库列表、订单详情、出库备注与提交逻辑
- [√] 1.3 调整销售模块文档与页面说明，确保不再引用不存在的页面或旧口径

## 2. 库存统一追溯后端能力
- [√] 2.1 为库存调拨动作补充统一 trace 写入，定义清晰的 nodeCode / nodeName 规范
- [√] 2.2 为库存盘点动作补充统一 trace 写入，纳入统一追溯链路
- [√] 2.3 设计并实现统一追溯查询接口，支持订单号、商品、仓库、业务类型、节点类型筛选

## 3. 前端追溯展示增强
- [√] 3.1 增强库存追溯台账页面，支持统一库存链路查询与筛选展示
- [√] 3.2 增强合规追溯页面，支持展示采购、销售、调拨、盘点的统一节点链路
- [√] 3.3 统一前端追溯文案与展示分层，区分业务节点与库存变化

## 4. 测试与验证
- [√] 4.1 补充后端测试：销售出库、库存调拨、库存盘点进入统一追溯链路
- [√] 4.2 补充后端测试：统一追溯接口筛选结果正确
- [√] 4.3 进行前端构建与后端编译验证，确认页面、接口、路由可用

## 5. 知识库同步
- [√] 5.1 更新 `helloagents/wiki/modules/frontend.md`、`helloagents/wiki/modules/inventory.md`、`helloagents/wiki/modules/sales.md`
- [√] 5.2 更新 `helloagents/wiki/api.md`、`helloagents/wiki/data.md`、`helloagents/wiki/arch.md`
- [√] 5.3 更新 `helloagents/CHANGELOG.md` 并在执行完成后迁移方案包至 `history/`


## 执行备注
- 已新增 `frontend/src/views/sale/SaleOutboundView.vue` 与 `/sale/outbound` 路由，补齐销售出库选仓前端闭环。
- 已为库存调拨、库存盘点补充统一 `trace_records` 写入，并增强 `/api/reports/compliance-trace` 聚合查询。
- 已增强库存追溯台账与合规追溯页面筛选能力，形成“库存变化 + 业务节点”双视角。
- 已验证 `mvn -q -Dtest=PlatformIntegrationTest test` 与 `npm run build` 通过。
