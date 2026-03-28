# 变更提案: 平台功能完整性修复（10项高/中优先级问题）

## 需求背景

烟草采销存协同管理平台功能审计发现 10 项高/中优先级缺陷，涵盖数据库表缺失、订单审核/取消流程缺失、Excel 导入无入口、消息推送机制未接通、库存调拨逻辑不完整、追溯记录无自动写入、销售信息发布功能缺失、异常单据审核接口不完整、权限初始数据未配置等问题。这些缺陷导致系统无法完整覆盖功能清单要求，部分核心流程存在断链风险。

## 变更内容

1. 补全 schema.sql 中缺失的 13 张数据库表定义
2. 为采购/销售订单新增审核（APPROVED/REJECTED）和取消（CANCELLED）状态流转
3. 为采购单、销售单、库存新增 Excel 导入接口
4. 在业务关键节点（入库、出库、预警、审核等）自动调用 MessageService.createMessage()
5. 修复库存调拨逻辑使其实际转移库存数量
6. 在订单创建、入库、出库、回款等节点自动写入 trace_records
7. 新增销售信息发布模块（后端 + 前端）
8. 补全异常单据审核通过/驳回接口
9. 补全 seed.sql 中 permissions 和 role_permissions 初始数据
10. 同步更新前端页面，添加审核/取消按钮、导入上传组件等

## 影响范围

- **模块:** 认证、采购、销售、库存、报表、消息、审计、系统管理
- **文件（后端）:**
  - `backend/src/main/resources/sql/schema.sql`
  - `backend/src/main/resources/sql/seed.sql`
  - `backend/.../purchase/PurchaseController.java`
  - `backend/.../purchase/PurchaseService.java`
  - `backend/.../sales/SalesController.java`
  - `backend/.../sales/SalesService.java`
  - `backend/.../inventory/InventoryController.java`
  - `backend/.../inventory/InventoryService.java`
  - `backend/.../message/MessageService.java`
  - `backend/.../audit/AuditService.java`
  - `backend/.../report/ReportController.java`
  - `backend/.../report/ReportService.java`
  - `backend/.../model/PurchaseOrderItem.java`
  - `backend/.../model/SalesOrderItem.java`
  - 新增: `backend/.../sales/BulletinController.java`
  - 新增: `backend/.../sales/BulletinService.java`
- **文件（前端）:**
  - `frontend/src/views/purchase/PurchaseOrderListView.vue`
  - `frontend/src/views/sale/SaleOrderListView.vue`
  - `frontend/src/views/inventory/InventoryFlowView.vue`
  - `frontend/src/views/audit/ExceptionAuditView.vue`
  - `frontend/src/api/purchase.js`
  - `frontend/src/api/sales.js`
  - `frontend/src/api/inventory.js`
  - 新增: `frontend/src/views/sale/SaleBulletinView.vue`
  - 新增: `frontend/src/api/bulletin.js`
- **API:** 新增约 10 个端点，修改 2 个现有端点
- **数据:** 新增 13 张表 + 1 张 bulletins 表，扩展 purchase_orders/sales_orders 字段

## 核心场景

### 需求: 数据库表定义补全
**模块:** 系统基础
应用启动时自动创建所有业务依赖的数据库表，确保无运行时表缺失错误。

#### 场景: 全新部署启动
执行 schema.sql 后，代码引用的所有 24 张表全部存在
- 应用正常启动无 SQL 异常
- 所有功能模块可正常访问

### 需求: 采购/销售订单审核流程
**模块:** 采购管理、销售管理
订单创建后须经管理员审核，审核通过后才能进行后续操作（到货/出库），支持驳回和取消。

#### 场景: 采购订单审核通过
管理员对 CREATED 状态的采购单执行审核通过
- 状态变为 APPROVED
- 记录审核人、审核时间、审核意见
- 写入操作日志和追溯记录

#### 场景: 采购订单审核驳回
管理员对 CREATED 状态的采购单执行驳回
- 状态变为 REJECTED
- 记录驳回原因
- 创建者收到驳回通知消息

#### 场景: 订单取消
创建者对 CREATED/REJECTED 状态的订单执行取消
- 状态变为 CANCELLED
- 记录取消原因

### 需求: Excel 批量导入
**模块:** 采购管理、销售管理、库存管理
支持通过 Excel 文件批量导入采购单、销售单和库存数据。

#### 场景: 批量导入采购单
上传 Excel 文件，系统逐行校验并批量创建采购订单
- 校验供应商、商品存在性
- 校验数量和单价合法性
- 返回导入成功/失败汇总

### 需求: 消息自动推送
**模块:** 消息通知
业务关键节点自动生成消息通知，推送给相关用户。

#### 场景: 库存预警自动通知
采购入库或销售出库后检查库存预警，低于阈值时自动向库管人员发送预警消息
- 消息类型为 ALERT
- 消息标题包含商品名称和当前库存数量

#### 场景: 审核结果通知
订单审核通过或驳回后，自动向订单创建者发送结果消息
- 消息包含订单号和审核意见

### 需求: 库存调拨实际转移
**模块:** 库存管理
调拨操作需实际扣减库存数量并记录变更流水。

#### 场景: 执行库存调拨
对指定商品执行调拨，系统扣减指定数量
- 校验库存充足
- 更新 inventories 表数量
- change_qty 记录实际变更值
- before_qty/after_qty 正确反映变更前后

### 需求: 追溯记录自动写入
**模块:** 数据统计与合规追溯
业务关键节点自动写入追溯记录，支持全流程合规追溯查询。

#### 场景: 采购入库追溯
采购单入库时自动写入追溯节点
- biz_type=PURCHASE, node_code=INBOUND
- 包含订单号、操作人、时间

### 需求: 销售信息发布
**模块:** 销售管理
销售专员可发布销售公告/促销信息，其他用户可查看。

#### 场景: 发布销售公告
销售专员创建销售公告，设置标题、内容、有效期
- 公告列表按发布时间倒序
- 过期公告自动标记

### 需求: 异常单据审核
**模块:** 数据统计与合规追溯
管理员可对异常单据执行审核通过或驳回操作。

#### 场景: 审核通过异常单据
管理员对 PENDING 状态的异常单执行审核通过
- 状态变为 APPROVED
- 记录审核人

### 需求: 权限初始数据配置
**模块:** 系统管理
系统首次部署时自动初始化角色权限映射数据。

#### 场景: 首次部署权限加载
执行 seed.sql 后，四个角色（ADMIN/PURCHASER/SELLER/KEEPER）各自拥有对应功能权限
- 前端菜单按角色正确显示
- 权限树编辑页可见完整权限列表

## 风险评估

- **风险:** schema.sql 新增表可能与已有数据冲突
- **缓解:** 全部使用 `CREATE TABLE IF NOT EXISTS`，不影响已有数据

- **风险:** 订单新增审核状态可能影响已有订单的状态流转
- **缓解:** 仅对 CREATED 状态订单增加审核环节，已完成订单不受影响

- **风险:** 调拨逻辑修改影响现有调拨记录
- **缓解:** 历史记录保持不变，仅新调拨按新逻辑执行
