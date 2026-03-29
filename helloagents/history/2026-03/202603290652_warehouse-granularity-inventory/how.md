# 方案设计

## 总体方案
采用“仓库维度库存重构 + 动作接口扩展 + 前端筛选增强”的分层改造方案。

### 1. 数据模型改造
**核心原则：库存主键从“商品”升级为“商品 + 仓库”。**

#### 1.1 inventories 表
- 新增 `warehouse_id` 字段，关联 `warehouses.id`。
- 保留 `warehouse_name` 作为兼容展示字段，或在迁移后改为查询联表生成。
- 移除 `product_id` 唯一约束。
- 改为 `UNIQUE(product_id, warehouse_id)`。

#### 1.2 inventory_records 表
- 新增以下字段：
  - `warehouse_id`
  - `warehouse_name`
  - `from_warehouse_id`（调拨来源，可选）
  - `to_warehouse_id`（调拨目标，可选）
- 使流水可表达：入库发生在哪个仓、出库发生在哪个仓、调拨从哪到哪。

#### 1.3 订单表扩展
- `purchase_orders` 新增 `warehouse_id` / `warehouse_name`，表示最终入库仓。
- `sales_orders` 新增 `warehouse_id` / `warehouse_name`，表示最终出库仓。
- 对历史订单采用可空迁移，已执行完出入库的历史单据可回填为默认仓库。
- 这样台账、订单详情、后续追溯都能直接关联仓库，而不仅依赖库存流水反推。

#### 1.3 迁移策略
- 为现有库存数据统一映射到默认仓库（建议使用“中心仓”，按 `warehouses` 中现有记录或初始化补齐）。
- 兼容历史数据：旧的 `inventory_records` 仓库字段可置为默认仓库/空，并在展示层容错。

### 2. 后端接口与服务改造

#### 2.1 采购入库
- `POST /api/purchases/{id}/inbound`
- 请求体新增：`warehouseId`
- 服务逻辑：
  - 校验订单状态 `RECEIVED`
  - 校验仓库存在且启用
  - 将 `purchase_orders.warehouse_id / warehouse_name` 更新为本次入库仓
  - 将数量入到指定仓库的库存记录
  - 写入带仓库信息的 `PURCHASE_INBOUND` 流水

#### 2.2 销售出库
- `POST /api/sales/{id}/outbound`
- 请求体新增：`warehouseId`
- 服务逻辑：
  - 校验订单状态 `APPROVED`
  - 校验指定仓库库存充足
  - 将 `sales_orders.warehouse_id / warehouse_name` 更新为本次出库仓
  - 从指定仓库扣减库存
  - 写入带仓库信息的 `SALES_OUTBOUND` 流水

#### 2.3 库存调拨
- `POST /api/inventory-transfers`
- 请求体明确要求：
  - `productId`
  - `quantity`
  - `fromWarehouseId`
  - `toWarehouseId`
  - `remark`
- 服务逻辑：
  - 禁止 A 仓与 B 仓相同
  - 校验来源仓库存充足
  - 来源仓扣减、目标仓增加
  - 记录两种可选实现之一：
    1. 单条流水，包含 from/to 仓字段
    2. 双条流水，一出一入，并通过 biz_id/remark 串联
- 建议采用 **单次事务 + 双条流水**，便于台账从单仓视角查看增减。

#### 2.4 库存查询接口
- `/api/inventories`
  - 支持 `warehouseId` / `keyword` / `status` 筛选
- `/api/inventory-records`
  - 支持 `warehouseId` / `bizType` / `productId` 筛选
- `/api/inventory-warnings`
  - 支持 `warehouseId` 筛选
- 返回值新增仓库维度字段，供前端直接展示。

### 3. 前端页面改造

#### 3.1 采购入库页 `PurchaseInboundView.vue`
- 新增“入库仓库”下拉。
- 调用入库接口时提交 `warehouseId`。
- 表格显示目标仓库列（已选仓库或入库后仓库）。

#### 3.2 销售出库页 `SaleOutboundView.vue`
- 新增“出库仓库”下拉。
- 调用出库接口时提交 `warehouseId`。
- 允许库管按仓库执行出库。

#### 3.3 库存总览 `InventoryListView.vue`
- 新增仓库筛选。
- KPI 支持“全部仓库”视角与筛选后视角。
- 同一商品可出现多条仓库记录。

#### 3.4 库存流水 `InventoryFlowView.vue`
- 新增仓库筛选。
- 调拨表单新增来源仓、目标仓下拉。
- 调拨流水表显示来源仓/目标仓或当前作用仓库。

#### 3.5 库存台账 `InventoryLedgerView.vue`
- 支持查看全部仓库台账。
- 支持按仓库筛选商品台账与时间轴流水。

### 4. 兼容与验证策略
- 后端优先兼容空仓库参数时返回明确错误，不静默回落。
- 前端在仓库未选择时禁止提交。
- 测试覆盖：
  - 采购入库到不同仓库
  - 销售从指定仓库出库
  - A 仓 → B 仓调拨
  - 多仓库同商品库存查询与筛选
  - 台账/流水按仓库过滤正确

## 决策说明
- **为什么不只改前端？** 因为根因在数据库唯一约束和后端库存扣减逻辑，必须后端先支持仓库维度。
- **为什么建议双条调拨流水？** 因为单仓视角更易理解：来源仓看到“调出”，目标仓看到“调入”。
- **为什么本次要同步扩展订单表？** 因为需求已明确要求订单级仓库归属，若只记录在流水中，订单详情与追溯场景仍会缺失关键仓库信息。
