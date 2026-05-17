# 答辩用项目代码结构与模块实现过程说明

> 适用对象：完全不熟悉项目代码、但需要在答辩时讲清楚“一个页面或一个模块是怎么实现的”的同学。
>
> 项目路径：`/mnt/java-test`
>
> 项目类型：前后端分离管理系统
>
> 技术栈：Vue 3 + Spring Boot + MyBatis + MySQL

---

## 1. 先用一句话讲清楚这个项目

答辩时可以先这样说：

> 本项目是一个烟草业务管理平台，采用前后端分离架构。前端使用 Vue 3 负责页面展示和用户交互，后端使用 Spring Boot 提供接口，数据库使用 MySQL 保存业务数据，后端通过 MyBatis 操作数据库。用户在页面上点击按钮后，请求会从 Vue 页面进入前端 API 文件，再发送到后端 Controller，Controller 调用 Service 处理业务逻辑，Service 再调用 Mapper 执行 SQL，最后数据返回给前端页面展示。

这一段非常重要，因为它把整个项目的主线讲清楚了：

```text
用户操作页面
  -> Vue 页面
  -> 前端 api 文件
  -> Axios 请求
  -> 后端 Controller
  -> 后端 Service
  -> MyBatis Mapper
  -> MySQL 数据库
  -> 返回结果给页面
```

答辩时如果老师问“你这个功能怎么实现的”，你基本都可以按这条链路回答。

---

## 2. 项目目录怎么理解

项目根目录是：

```text
/mnt/java-test
```

主要目录如下：

```text
/mnt/java-test
├── frontend        前端项目，Vue 页面、路由、接口请求都在这里
├── backend         后端项目，Spring Boot 接口、业务逻辑、数据库操作都在这里
├── helloagents     项目知识库，记录项目架构、模块说明、历史变更
├── README.md       项目基础说明
└── docs            补充文档，本文件就在这里
```

小白理解版：

- `frontend`：用户能看到的页面。
- `backend`：页面背后的接口和业务规则。
- `database/MySQL`：真正保存数据的地方。
- `helloagents`：项目说明书和历史记录。
- `docs`：答辩、说明、补充文档。

---

## 3. 前端目录怎么看

前端目录：

```text
/mnt/java-test/frontend
```

重点看这几个目录：

```text
frontend/src
├── views       页面文件，一个 View 基本对应一个页面
├── api         前端请求接口，负责调用后端
├── router      前端路由，决定哪个地址打开哪个页面
├── components  公共组件，比如表格、卡片、图表
├── stores      前端状态管理
├── utils       工具函数，比如导出 Excel、格式化状态
└── main.ts     前端入口文件
```

### 3.1 views 是什么

`views` 里面放的是页面。比如：

```text
frontend/src/views/sale/SaleOrderListView.vue
```

这个文件就是“销售订单列表页面”。

常见页面如下：

```text
销售订单页面:
frontend/src/views/sale/SaleOrderListView.vue

新建/编辑/详情销售单页面:
frontend/src/views/sale/SaleOrderFormView.vue

销售出库页面:
frontend/src/views/sale/SaleOutboundView.vue

采购订单页面:
frontend/src/views/purchase/PurchaseOrderListView.vue

采购单表单页面:
frontend/src/views/purchase/PurchaseOrderFormView.vue

采购入库页面:
frontend/src/views/purchase/PurchaseInboundView.vue

库存总览页面:
frontend/src/views/inventory/InventoryListView.vue

库存流水页面:
frontend/src/views/inventory/InventoryFlowView.vue

账号管理页面:
frontend/src/views/admin/AccountListView.vue

角色权限页面:
frontend/src/views/admin/RolePermissionView.vue

商品管理页面:
frontend/src/views/catalog/ProductListView.vue
```

答辩时可以说：

> 前端页面主要放在 `frontend/src/views` 目录下，每个业务页面对应一个 Vue 文件。页面文件中一般分为两部分：`template` 写页面结构，`script setup` 写数据加载、按钮事件和接口调用逻辑。

### 3.2 api 是什么

`api` 目录负责封装前端请求。比如销售模块：

```text
frontend/src/api/sales.js
```

里面会有类似这样的接口方法：

```text
fetchSales          获取销售订单列表
fetchSalesDetail    获取销售订单详情
createSales         新建销售订单
updateSales         修改销售订单
auditSales          审核销售订单
cancelSales         取消销售订单
outboundSales       销售出库
paymentSales        销售回款
importSales         导入销售订单 Excel
```

答辩时可以说：

> 页面本身不直接写完整请求地址，而是调用 `frontend/src/api` 目录下封装好的方法。这样页面只关心业务操作，比如“获取销售列表”或“审核订单”，具体请求地址集中维护在 api 文件中。

### 3.3 http.ts 是什么

文件位置：

```text
frontend/src/api/http.ts
```

它是前端所有接口请求的统一入口。

它主要做三件事：

1. 设置后端接口基础路径为 `/api`。
2. 请求时自动带上登录 token。
3. 响应时统一处理成功、失败、401 未登录、403 无权限等情况。

小白理解：

> `http.ts` 就像前端的“请求总管”。所有接口请求都先经过它，它会帮我们加登录凭证，也会统一处理错误提示。

答辩时可以说：

> 前端使用 Axios 发送请求，并在 `http.ts` 中做统一封装。请求拦截器会从浏览器本地存储中读取 token，并放入 `Authorization: Bearer token` 请求头。响应拦截器会判断后端返回的 `code`，如果 `code == 0` 就认为成功，否则统一弹出错误信息。

### 3.4 router 是什么

文件位置：

```text
frontend/src/router/route-map.ts
```

它负责配置页面地址和页面文件的对应关系。

比如：

```text
/sale/order
  -> frontend/src/views/sale/SaleOrderListView.vue

/sale/order/create
  -> frontend/src/views/sale/SaleOrderFormView.vue

/purchase/order
  -> frontend/src/views/purchase/PurchaseOrderListView.vue

/inventory/list
  -> frontend/src/views/inventory/InventoryListView.vue
```

答辩时可以说：

> 前端路由配置在 `frontend/src/router/route-map.ts` 中。路由中不仅配置了路径和页面组件，还配置了页面标题、菜单分组和权限标识。用户登录后，系统会根据用户权限决定能看到哪些菜单。

---

## 4. 后端目录怎么看

后端目录：

```text
/mnt/java-test/backend
```

重点看：

```text
backend/src/main/java/com/example/tobacco
├── auth        登录、退出、验证码、当前用户
├── sales       销售订单、销售出库、回款、公告
├── purchase    采购订单、到货、入库
├── inventory   库存总览、库存流水、调拨、盘点
├── catalog     商品、品类
├── supplier    供应商
├── customer    客户
├── system      用户、角色、权限、仓库、系统配置
├── report      报表
├── audit       操作日志、追溯、异常审核
├── message     站内消息
├── mapper      MyBatis 数据库操作
├── model       请求对象和返回对象
├── common      统一返回结构、全局异常处理
├── config      Spring、Shiro、Web 配置
├── interceptor 登录拦截器
└── util        工具类
```

后端核心分层：

```text
Controller  接收请求
Service     处理业务逻辑
Mapper      操作数据库
Model       承载请求参数和返回数据
```

答辩时可以说：

> 后端采用典型的 Controller、Service、Mapper 分层。Controller 只做请求接收和响应返回，Service 负责业务规则，比如状态判断、权限判断、金额计算、库存校验，Mapper 负责真正执行 SQL 操作数据库。

---

## 5. 数据库文件怎么看

数据库结构文件：

```text
backend/src/main/resources/sql/schema.sql
```

初始化数据文件：

```text
backend/src/main/resources/sql/data.sql
backend/src/main/resources/sql/seed.sql
```

重点表：

```text
roles                 角色表
users                 用户表
permissions           权限表
role_permissions      角色权限关联表
user_sessions         登录会话表

products              商品表
categories            商品分类表
suppliers             供应商表
customers             客户表
warehouses            仓库表

purchase_orders       采购订单表
sales_orders          销售订单表
payment_records       回款记录表

inventories           库存表
inventory_records     库存流水表

trace_records         追溯记录表
operation_logs        操作日志表
login_logs            登录日志表
messages              站内消息表
bulletins             销售公告表
abnormal_documents    异常单据表
```

小白理解：

- `users`：谁在使用系统。
- `roles`：用户是什么角色。
- `permissions`：角色有哪些权限。
- `products`：卖什么商品。
- `suppliers`：从谁那里采购。
- `customers`：卖给谁。
- `purchase_orders`：采购单。
- `sales_orders`：销售单。
- `inventories`：现在库存有多少。
- `inventory_records`：库存变化历史。
- `trace_records`：业务流程追溯记录。
- `operation_logs`：谁做了什么操作。
- `messages`：系统提醒。

答辩时可以说：

> 数据库表结构统一维护在 `schema.sql` 中。核心业务表包括采购订单表、销售订单表、库存表、库存流水表、用户权限相关表。系统启动时会执行初始化 SQL，保证演示环境有基础数据。

---

## 6. 一个功能从页面到数据库的完整流程

以“销售订单列表页面”为例。

### 6.1 第一步：用户打开页面

用户访问：

```text
/sale/order
```

前端路由在这里配置：

```text
frontend/src/router/route-map.ts
```

它会找到销售订单页面：

```text
frontend/src/views/sale/SaleOrderListView.vue
```

答辩时可以说：

> 用户访问 `/sale/order` 时，Vue Router 会根据 `route-map.ts` 找到 `SaleOrderListView.vue` 页面进行展示。

### 6.2 第二步：页面加载数据

销售订单页面中有一个 `loadData` 方法。

它会调用：

```text
fetchSales()
```

这个方法来自：

```text
frontend/src/api/sales.js
```

`fetchSales()` 对应的后端地址是：

```text
GET /api/sales
```

答辩时可以说：

> 页面加载时会执行 `loadData` 方法，调用 `fetchSales` 获取销售订单列表。`fetchSales` 是前端封装的接口方法，它最终会向后端发送 `GET /api/sales` 请求。

### 6.3 第三步：请求进入后端 Controller

后端入口文件：

```text
backend/src/main/java/com/example/tobacco/sales/SalesController.java
```

对应方法：

```text
list()
```

它负责处理：

```text
GET /api/sales
```

答辩时可以说：

> 请求到达后端后，首先进入 `SalesController`。Controller 通过 `@RequestMapping("/api/sales")` 定义模块路径，通过 `@GetMapping` 定义列表查询接口，然后调用 `SalesService.list()` 获取数据。

### 6.4 第四步：Service 处理业务逻辑

业务逻辑文件：

```text
backend/src/main/java/com/example/tobacco/sales/SalesService.java
```

销售列表方法：

```text
list(username, roleCode)
```

它会判断用户角色：

- 如果是销售员，只能看自己创建的销售单。
- 如果是管理员或其他有权限角色，可以看全部销售单。

答辩时可以说：

> `SalesService` 是业务处理层。它不直接接收 HTTP 请求，而是处理具体业务规则。比如销售列表会根据当前用户的角色判断查询范围，销售员只能查看自己创建的订单，管理员可以查看全部订单。

### 6.5 第五步：Mapper 查询数据库

数据库访问文件：

```text
backend/src/main/java/com/example/tobacco/mapper/sales/SalesMapper.java
```

销售列表查询：

```text
list(username, isSeller)
```

它查询的核心表：

```text
sales_orders
customers
products
```

小白理解：

> Mapper 就是“和数据库说话”的地方。Service 告诉 Mapper 要查销售订单，Mapper 就执行 SQL，从数据库表里取出数据。

答辩时可以说：

> 数据库查询由 `SalesMapper` 完成。销售订单列表会以 `sales_orders` 为主表，并关联 `customers` 和 `products` 表，把客户名称和商品名称一起查出来返回给前端。

### 6.6 第六步：数据返回前端

后端统一返回：

```text
ApiResponse {
  code,
  message,
  data
}
```

前端 `http.ts` 会判断：

```text
code == 0
```

如果成功，就把 `data` 给页面。

页面把数据放进：

```text
rows
```

然后表格组件展示。

答辩时可以说：

> 后端统一使用 `ApiResponse` 返回结果，前端响应拦截器会判断 `code` 是否为 0。如果成功，页面把返回的 `data` 赋值给 `rows`，表格组件根据 `rows` 渲染订单列表。

---

## 7. 销售订单模块详细讲解

销售模块是答辩最适合讲的模块之一，因为它包含页面、接口、业务逻辑、数据库、库存变化、日志追溯。

### 7.1 销售订单模块涉及哪些文件

前端页面：

```text
frontend/src/views/sale/SaleOrderListView.vue
frontend/src/views/sale/SaleOrderFormView.vue
frontend/src/views/sale/SaleOutboundView.vue
frontend/src/views/sale/SalePerformanceView.vue
frontend/src/views/sale/SaleBulletinView.vue
```

前端接口：

```text
frontend/src/api/sales.js
frontend/src/api/bulletin.js
```

后端接口：

```text
backend/src/main/java/com/example/tobacco/sales/SalesController.java
backend/src/main/java/com/example/tobacco/sales/BulletinController.java
```

业务逻辑：

```text
backend/src/main/java/com/example/tobacco/sales/SalesService.java
backend/src/main/java/com/example/tobacco/sales/BulletinService.java
```

数据库操作：

```text
backend/src/main/java/com/example/tobacco/mapper/sales/SalesMapper.java
backend/src/main/java/com/example/tobacco/mapper/sales/SalesSqlProvider.java
backend/src/main/java/com/example/tobacco/mapper/sales/BulletinMapper.java
```

数据模型：

```text
backend/src/main/java/com/example/tobacco/model/CreateSalesRequest.java
backend/src/main/java/com/example/tobacco/model/SalesOrderItem.java
backend/src/main/java/com/example/tobacco/model/PaymentRequest.java
backend/src/main/java/com/example/tobacco/model/AuditRequest.java
backend/src/main/java/com/example/tobacco/model/CancelRequest.java
backend/src/main/java/com/example/tobacco/model/WarehouseActionRequest.java
```

数据库表：

```text
sales_orders
payment_records
inventory_records
inventories
trace_records
operation_logs
messages
bulletins
```

### 7.2 销售订单列表怎么实现

流程：

```text
打开 /sale/order
  -> SaleOrderListView.vue
  -> onMounted(loadData)
  -> fetchSales()
  -> GET /api/sales
  -> SalesController.list()
  -> SalesService.list()
  -> SalesMapper.list()
  -> 查询 sales_orders
  -> 返回订单列表
  -> rows 赋值
  -> AppTable 展示
```

答辩可说：

> 销售订单列表页面在加载时调用 `fetchSales` 方法，请求后端 `/api/sales`。后端 `SalesController` 接收请求后调用 `SalesService.list`，Service 根据当前登录用户的角色判断数据范围，然后调用 `SalesMapper` 查询销售订单表，并关联客户和商品信息，最后返回给前端表格展示。

### 7.3 新建销售单怎么实现

流程：

```text
点击新建销售单
  -> 跳转 /sale/order/create
  -> SaleOrderFormView.vue
  -> 用户填写客户、商品、数量、单价
  -> createSales(payload)
  -> POST /api/sales
  -> SalesController.create()
  -> SalesService.create()
  -> 计算 total_amount
  -> 生成 order_no
  -> SalesMapper.insertOrder()
  -> 插入 sales_orders
  -> 写 trace_records 追溯记录
  -> 返回新订单详情
```

关键业务点：

- 订单号由后端生成，格式类似 `SO + 年月日时分秒`。
- 总金额由后端计算：`单价 * 数量`。
- 新订单状态为 `CREATED`，表示待审核。
- 创建后会写入追溯记录。

答辩可说：

> 新建销售单时，前端只提交客户、商品、数量和单价等基础数据。后端会在 `SalesService.create` 中计算订单总金额，生成销售单号，设置初始状态为 `CREATED`，然后通过 `SalesMapper.insertOrder` 插入 `sales_orders` 表，同时写入追溯记录，方便后续查询订单流程。

### 7.4 审核销售单怎么实现

流程：

```text
销售订单列表点击审核
  -> 打开审核弹窗
  -> 用户选择通过或驳回
  -> auditSales(id, data)
  -> POST /api/sales/{id}/audit
  -> SalesController.audit()
  -> SalesService.audit()
  -> 判断当前用户是否有审核权限
  -> 判断订单是否为 CREATED
  -> 更新 sales_orders.status
  -> 写 trace_records
  -> 写 operation_logs
  -> 给创建人发送 messages
  -> 返回更新后的订单
```

关键业务点：

- 只有待审核状态 `CREATED` 的订单可以审核。
- 审核结果只能是 `APPROVED` 或 `REJECTED`。
- 审核通过后状态变为 `APPROVED`。
- 审核驳回后状态变为 `REJECTED`。
- 审核后会给创建人发送站内消息。

答辩可说：

> 销售单审核功能在前端通过审核弹窗提交审核意见，后端在 `SalesService.audit` 中先校验当前角色是否具备审核权限，再校验订单是否处于待审核状态。审核通过会把状态改为 `APPROVED`，驳回则改为 `REJECTED`，同时记录追溯、操作日志，并向订单创建人发送消息提醒。

### 7.5 销售出库怎么实现

流程：

```text
销售订单审核通过
  -> 页面显示出库按钮
  -> 点击出库
  -> 跳转 /sale/outbound?id=订单id
  -> 选择仓库
  -> outboundSales(id, payload)
  -> POST /api/sales/{id}/outbound
  -> SalesController.outbound()
  -> SalesService.outbound()
  -> 校验角色权限
  -> 校验订单状态必须是 APPROVED
  -> 校验仓库存在
  -> 查询库存 inventories
  -> 判断库存是否足够
  -> 扣减库存
  -> 更新销售单状态为 OUTBOUND
  -> 写 inventory_records 库存流水
  -> 写 trace_records 追溯记录
  -> 写 operation_logs 操作日志
  -> 判断是否触发库存预警
  -> 返回结果
```

关键业务点：

- 销售单必须审核通过才能出库。
- 出库必须选择仓库。
- 如果库存不足，后端会拒绝出库。
- 出库会扣减 `inventories` 表库存数量。
- 出库会新增一条 `inventory_records` 记录。
- 出库后销售单状态变为 `OUTBOUND`。
- 如果库存低于预警阈值，会发送消息给库管。

答辩可说：

> 销售出库是销售模块里比较核心的业务。后端会先判断销售单是否已经审核通过，再根据选择的仓库查询当前库存。如果库存不足，就抛出业务异常；如果库存足够，就扣减库存表数量，更新销售单状态为已出库，并插入库存流水记录。这样既保证库存数据准确，也能追溯每一次库存变化。

### 7.6 销售回款怎么实现

流程：

```text
点击回款
  -> paymentSales(id, payload)
  -> POST /api/sales/{id}/payment
  -> SalesController.payment()
  -> SalesService.payment()
  -> 查询订单详情
  -> 累加已回款金额 paid_amount
  -> 判断是否已全部回款
  -> 更新 sales_orders.paid_amount 和 status
  -> 插入 payment_records
  -> 写 trace_records
  -> 写 operation_logs
  -> 返回订单详情
```

状态变化：

```text
未完全回款:
OUTBOUND -> PARTIAL_PAID

完全回款:
任意可回款状态 -> PAID
```

答辩可说：

> 回款功能会把本次回款金额累加到订单的已回款金额中。如果已回款金额大于或等于订单总金额，订单状态变为 `PAID`；如果只回了一部分，则变为 `PARTIAL_PAID`。同时系统会插入一条回款记录，方便后续查看回款历史。

---

## 8. 采购订单模块详细讲解

采购模块和销售模块结构很相似，适合用来说明你理解了代码结构。

### 8.1 采购模块涉及哪些文件

前端页面：

```text
frontend/src/views/purchase/PurchaseOrderListView.vue
frontend/src/views/purchase/PurchaseOrderFormView.vue
frontend/src/views/purchase/PurchaseInboundView.vue
frontend/src/views/purchase/PurchaseAnalysisView.vue
```

前端接口：

```text
frontend/src/api/purchase.js
```

后端接口：

```text
backend/src/main/java/com/example/tobacco/purchase/PurchaseController.java
backend/src/main/java/com/example/tobacco/purchase/PurchaseRequisitionController.java
```

业务逻辑：

```text
backend/src/main/java/com/example/tobacco/purchase/PurchaseService.java
```

数据库操作：

```text
backend/src/main/java/com/example/tobacco/mapper/purchase/PurchaseMapper.java
```

数据库表：

```text
purchase_orders
inventories
inventory_records
trace_records
operation_logs
messages
```

### 8.2 采购单流程

采购订单主要流程：

```text
创建采购单
  -> 待审核 CREATED
  -> 审核通过 APPROVED
  -> 到货 RECEIVED
  -> 入库 INBOUND
  -> 库存增加
```

答辩可说：

> 采购模块实现了从采购建单、审核、到货到入库的完整流程。采购单创建后先进入待审核状态，审核通过后可以登记到货，到货后再选择仓库进行入库。入库时系统会增加库存，并写入库存流水和追溯记录。

### 8.3 采购入库怎么讲

流程：

```text
采购单审核并到货
  -> 打开采购入库页面
  -> 选择待入库订单和仓库
  -> POST /api/purchases/{id}/inbound
  -> PurchaseController.inbound()
  -> PurchaseService.inbound()
  -> 校验订单状态
  -> 查询仓库
  -> 增加 inventories 库存
  -> 更新采购单状态为 INBOUND
  -> 插入 inventory_records
  -> 写 trace_records
  -> 返回结果
```

答辩可说：

> 采购入库和销售出库正好相反。采购入库会增加库存，销售出库会减少库存。采购入库时，后端会校验采购单状态，确认可以入库后，根据商品和仓库更新库存表，同时插入库存流水，用于后续库存追溯。

---

## 9. 库存模块详细讲解

库存模块是连接采购和销售的核心模块。

### 9.1 库存模块涉及哪些文件

前端页面：

```text
frontend/src/views/inventory/InventoryListView.vue
frontend/src/views/inventory/InventoryFlowView.vue
frontend/src/views/inventory/InventoryCheckView.vue
frontend/src/views/inventory/InventoryLedgerView.vue
```

前端接口：

```text
frontend/src/api/inventory.js
```

后端接口：

```text
backend/src/main/java/com/example/tobacco/inventory/InventoryController.java
```

业务逻辑：

```text
backend/src/main/java/com/example/tobacco/inventory/InventoryService.java
```

数据库操作：

```text
backend/src/main/java/com/example/tobacco/mapper/inventory/InventoryMapper.java
backend/src/main/java/com/example/tobacco/mapper/inventory/InventorySqlProvider.java
```

数据库表：

```text
inventories
inventory_records
products
warehouses
messages
```

### 9.2 库存表怎么理解

库存表：

```text
inventories
```

重要字段：

```text
product_id       商品 ID
warehouse_id     仓库 ID
warehouse_name   仓库名称
quantity         当前库存数量
warning_threshold 预警阈值
updated_at       更新时间
```

这个项目的库存不是只按商品统计，而是：

```text
商品 + 仓库
```

也就是说：

```text
同一个商品在不同仓库，可以有不同库存数量。
```

答辩可说：

> 库存表采用商品加仓库的双维度设计，也就是同一个商品在不同仓库分别维护库存数量。这样可以支持多仓库场景，避免只记录商品总库存导致无法判断具体仓库库存的问题。

### 9.3 库存流水怎么理解

库存流水表：

```text
inventory_records
```

它记录每次库存变化。

重要字段：

```text
product_id        商品 ID
biz_type          业务类型，比如采购入库、销售出库、盘点、调拨
biz_id            关联业务单据 ID
change_qty        变化数量
before_qty        变化前库存
after_qty         变化后库存
warehouse_id      仓库 ID
warehouse_name    仓库名称
operator_name     操作人
remark            备注
created_at        创建时间
```

小白理解：

> `inventories` 记录当前库存是多少，`inventory_records` 记录为什么变成了这个数量。

答辩可说：

> 库存模块中，`inventories` 表保存当前库存结果，`inventory_records` 表保存库存变化过程。比如一次销售出库会让库存减少，同时插入一条流水，记录变化前数量、变化后数量、变化原因和操作人。这样库存变化可以追溯。

### 9.4 库存调拨怎么讲

库存调拨含义：

```text
把某个商品从 A 仓库转移到 B 仓库
```

流程：

```text
选择商品、源仓库、目标仓库、数量
  -> POST /api/inventory-transfers
  -> InventoryController.transfer()
  -> InventoryService.transfer()
  -> 校验源仓库库存是否足够
  -> 源仓库库存减少
  -> 目标仓库库存增加
  -> 插入库存流水
  -> 返回结果
```

答辩可说：

> 库存调拨不会改变全平台总库存，只是把库存从一个仓库移动到另一个仓库。后端会校验源仓库库存是否足够，然后同时更新源仓库和目标仓库库存，并写入调拨流水。

### 9.5 库存盘点怎么讲

库存盘点含义：

```text
用实际盘点数量修正系统库存数量
```

流程：

```text
选择商品和仓库
  -> 输入实际盘点数量
  -> POST /api/inventory-checks
  -> InventoryController.check()
  -> InventoryService.check()
  -> 查询当前系统库存
  -> 计算差异
  -> 更新库存为实际数量
  -> 插入库存流水
  -> 返回结果
```

答辩可说：

> 库存盘点用于修正系统库存。系统会拿实际盘点数量和当前库存数量做对比，计算盈亏差异，然后把库存更新为实际数量，同时记录一条盘点流水。

---

## 10. 登录和权限怎么讲

登录模块也经常被老师问到。

### 10.1 登录相关文件

前端页面：

```text
frontend/src/views/auth/LoginView.vue
frontend/src/views/auth/ResetPasswordView.vue
```

前端接口：

```text
frontend/src/api/auth.ts
```

后端接口：

```text
backend/src/main/java/com/example/tobacco/auth/AuthController.java
```

业务逻辑：

```text
backend/src/main/java/com/example/tobacco/auth/AuthService.java
backend/src/main/java/com/example/tobacco/auth/ShiroRealm.java
```

拦截器：

```text
backend/src/main/java/com/example/tobacco/interceptor/AuthInterceptor.java
```

配置：

```text
backend/src/main/java/com/example/tobacco/config/ShiroConfig.java
backend/src/main/java/com/example/tobacco/config/WebConfig.java
```

数据库操作：

```text
backend/src/main/java/com/example/tobacco/mapper/auth/AuthMapper.java
backend/src/main/java/com/example/tobacco/mapper/auth/AuthSqlProvider.java
```

数据库表：

```text
users
roles
permissions
role_permissions
user_sessions
login_logs
captcha_records
password_reset_records
```

### 10.2 登录流程

流程：

```text
用户输入账号密码和验证码
  -> 前端调用登录接口
  -> POST /api/auth/login
  -> AuthController.login()
  -> AuthService.login()
  -> 校验验证码
  -> 校验账号密码
  -> 查询用户角色和权限
  -> 生成 token
  -> 写入 user_sessions
  -> 写入 login_logs
  -> 返回 token 和用户信息
  -> 前端保存 token
```

答辩可说：

> 登录时，前端提交账号、密码和验证码到 `/api/auth/login`。后端先校验验证码，再校验账号密码，校验成功后生成会话 token，写入 `user_sessions` 表，同时记录登录日志。前端拿到 token 后保存在浏览器本地，后续请求都会自动带上这个 token。

### 10.3 权限控制怎么讲

权限分两层：

```text
前端权限:
控制菜单、页面、按钮是否显示

后端权限:
控制接口是否允许访问，防止绕过前端直接请求接口
```

前端权限位置：

```text
frontend/src/router/route-map.ts
frontend/src/utils/access.ts
frontend/src/directives/permission.ts
```

后端权限位置：

```text
backend/src/main/java/com/example/tobacco/interceptor/AuthInterceptor.java
backend/src/main/java/com/example/tobacco/auth/ShiroRealm.java
backend/src/main/java/com/example/tobacco/config/ShiroConfig.java
```

答辩可说：

> 权限控制分为前端和后端两部分。前端根据用户权限控制菜单和按钮是否显示，后端通过登录拦截器和权限体系校验接口访问权限。这样即使用户绕过前端页面直接请求接口，后端也能进行权限校验。

---

## 11. Controller、Service、Mapper 分别讲什么

这是答辩高频问题。

### 11.1 Controller 层

Controller 负责：

- 定义接口地址。
- 接收前端传来的参数。
- 从请求中获取登录用户信息。
- 调用 Service。
- 返回统一响应。

Controller 不应该写复杂业务。

答辩可说：

> Controller 是接口入口层，主要负责接收 HTTP 请求、读取请求参数、调用 Service，并把 Service 的结果包装成统一响应返回给前端。复杂业务逻辑不会放在 Controller，而是放在 Service 中。

### 11.2 Service 层

Service 负责：

- 业务规则判断。
- 状态流转。
- 权限范围判断。
- 金额计算。
- 库存校验。
- 调用多个 Mapper 完成一个业务动作。
- 事务控制。
- 写日志、写追溯、发消息。

答辩可说：

> Service 是业务核心层。比如销售出库，不只是更新订单状态，还要校验库存、扣减库存、写库存流水、写操作日志、写追溯记录，并可能触发库存预警消息，所以这些逻辑都放在 Service 里。

### 11.3 Mapper 层

Mapper 负责：

- 写 SQL。
- 查询数据库。
- 插入数据。
- 更新数据。
- 删除或禁用数据。

答辩可说：

> Mapper 是数据访问层，负责和数据库交互。本项目使用 MyBatis 注解和 SqlProvider 实现 SQL，简单 SQL 写在 Mapper 注解中，复杂动态查询放在 SqlProvider 中。

### 11.4 Model 层

Model 负责：

- 接收前端请求参数。
- 承载后端返回数据。

例如：

```text
CreateSalesRequest    新建销售单请求参数
SalesOrderItem        销售单返回对象
PaymentRequest        回款请求参数
AuditRequest          审核请求参数
CancelRequest         取消请求参数
```

答辩可说：

> Model 用来定义请求和响应的数据结构。比如创建销售单时，前端传来的客户 ID、商品 ID、数量、单价会封装到 `CreateSalesRequest` 中；查询销售单返回的数据会封装成 `SalesOrderItem`。

---

## 12. 老师让现场改代码时怎么定位

这是最实用的一部分。

### 12.1 如果老师让你改页面文字

比如老师说：

> 把“销售订单”改成“销售单管理”。

你应该找：

```text
frontend/src/views/sale/SaleOrderListView.vue
```

或者路由标题：

```text
frontend/src/router/route-map.ts
```

判断方法：

- 页面正文标题多半在 `View.vue`。
- 左侧菜单标题多半在 `route-map.ts`。

### 12.2 如果老师让你新增表格列

比如老师说：

> 销售订单列表加一列“创建人”。

你要依次检查：

```text
1. 前端表格列:
   frontend/src/views/sale/SaleOrderListView.vue

2. 后端返回对象是否有字段:
   backend/src/main/java/com/example/tobacco/model/SalesOrderItem.java

3. Mapper 查询 SQL 是否查了这个字段:
   backend/src/main/java/com/example/tobacco/mapper/sales/SalesMapper.java

4. 数据库表是否有这个字段:
   backend/src/main/resources/sql/schema.sql
```

答辩现场可以说：

> 如果只是页面已经有字段但没展示，我只改前端列配置即可；如果后端没有返回这个字段，就要从 Model、Mapper 查询和数据库字段一起检查。

### 12.3 如果老师让你改按钮显示条件

比如老师说：

> 只有已审核的销售单才能显示出库按钮。

找：

```text
frontend/src/views/sale/SaleOrderListView.vue
```

重点看：

```text
v-if="row.status === 'APPROVED'"
```

答辩现场可以说：

> 按钮显示条件在前端页面中通过 `v-if` 控制，但真正的业务限制不能只靠前端，还需要后端 Service 再校验一次，防止用户绕过页面直接调接口。

后端也要看：

```text
backend/src/main/java/com/example/tobacco/sales/SalesService.java
```

因为出库业务中会判断订单状态是否为 `APPROVED`。

### 12.4 如果老师让你改接口地址

比如老师说：

> 销售列表接口地址在哪？

前端：

```text
frontend/src/api/sales.js
```

后端：

```text
backend/src/main/java/com/example/tobacco/sales/SalesController.java
```

对应关系：

```text
前端 http.get('/sales')
  + http.ts baseURL '/api'
  = 实际请求 /api/sales

后端 @RequestMapping("/api/sales")
  + @GetMapping
  = 接收 GET /api/sales
```

答辩现场可以说：

> 前端 API 文件里写的是相对路径 `/sales`，因为 Axios 在 `http.ts` 中统一配置了 `baseURL: /api`，所以最终请求地址是 `/api/sales`。后端 `SalesController` 使用 `@RequestMapping("/api/sales")` 和 `@GetMapping` 接收这个请求。

### 12.5 如果老师让你改业务规则

比如老师说：

> 销售出库时，库存低于 10 不允许出库。

应该找：

```text
backend/src/main/java/com/example/tobacco/sales/SalesService.java
```

因为这是业务规则，不应该只改前端。

可能还要看：

```text
backend/src/main/java/com/example/tobacco/mapper/sales/SalesMapper.java
```

因为 Service 需要通过 Mapper 查询库存。

答辩现场可以说：

> 这种规则属于后端业务规则，应该放在 Service 层。前端可以做提示，但不能只依赖前端校验，因为接口可能被直接调用。Service 层校验通过后，再调用 Mapper 更新数据库。

### 12.6 如果老师让你新增数据库字段

比如老师说：

> 销售订单新增一个备注字段。

需要改：

```text
1. 数据库表结构:
   backend/src/main/resources/sql/schema.sql

2. 后端返回对象:
   backend/src/main/java/com/example/tobacco/model/SalesOrderItem.java

3. 请求对象:
   backend/src/main/java/com/example/tobacco/model/CreateSalesRequest.java

4. Mapper 插入、查询、更新 SQL:
   backend/src/main/java/com/example/tobacco/mapper/sales/SalesMapper.java

5. Service 是否需要处理:
   backend/src/main/java/com/example/tobacco/sales/SalesService.java

6. 前端表单:
   frontend/src/views/sale/SaleOrderFormView.vue

7. 前端列表或详情展示:
   frontend/src/views/sale/SaleOrderListView.vue
```

答辩现场可以说：

> 新增字段不能只改数据库。还要让前端能输入，后端请求对象能接收，Mapper 能插入和查询，返回对象能带回给前端，最后页面才能展示。

### 12.7 如果老师让你查一个功能对应哪个文件

最快方法：

```text
看页面路径
  -> 去 route-map.ts 找页面文件
  -> 去 views 找按钮事件
  -> 去 api 找请求方法
  -> 去 Controller 找接口
  -> 去 Service 找业务逻辑
  -> 去 Mapper 找 SQL
```

例如要查“销售回款”：

```text
1. 页面按钮:
   SaleOrderListView.vue

2. 前端方法:
   handlePayment()

3. 前端接口:
   paymentSales()

4. 后端接口:
   SalesController.payment()

5. 后端业务:
   SalesService.payment()

6. 数据库操作:
   SalesMapper.updatePayment()
   SalesMapper.insertPaymentRecord()

7. 数据表:
   sales_orders
   payment_records
```

---

## 13. 常见模块和文件速查表

| 模块 | 前端页面 | 前端 API | 后端 Controller | 后端 Service | Mapper | 主要表 |
|---|---|---|---|---|---|---|
| 登录认证 | `views/auth` | `api/auth.ts` | `AuthController` | `AuthService` | `AuthMapper` | `users`, `user_sessions`, `login_logs` |
| 销售订单 | `views/sale` | `api/sales.js` | `SalesController` | `SalesService` | `SalesMapper` | `sales_orders`, `payment_records` |
| 销售公告 | `views/sale/SaleBulletinView.vue` | `api/bulletin.js` | `BulletinController` | `BulletinService` | `BulletinMapper` | `bulletins` |
| 采购订单 | `views/purchase` | `api/purchase.js` | `PurchaseController` | `PurchaseService` | `PurchaseMapper` | `purchase_orders` |
| 库存管理 | `views/inventory` | `api/inventory.js` | `InventoryController` | `InventoryService` | `InventoryMapper` | `inventories`, `inventory_records` |
| 商品管理 | `views/catalog/ProductListView.vue` | `api/catalog.js` | `CatalogController` | `CatalogService` | `CatalogMapper` | `products`, `categories` |
| 供应商 | `views/supplier/SupplierListView.vue` | `api/supplier.js` | `SupplierController` | `SupplierService` | `SupplierMapper` | `suppliers` |
| 客户 | `views/customer/CustomerListView.vue` | `api/customer.js` | `CustomerController` | `CustomerService` | `CustomerMapper` | `customers` |
| 系统管理 | `views/admin` | `api/system.js` | `SystemController` | `SystemService` | `SystemMapper` | `users`, `roles`, `permissions` |
| 报表 | `views/report` | `api/report.js` | `ReportController` | `ReportService` | `ReportMapper` | 多张业务表 |
| 消息中心 | `views/message` | `api/message.js` | `MessageController` | `MessageService` | `MessageMapper` | `messages` |

---

## 14. 答辩时可以直接背的总流程

如果老师问：

> 你讲一下一个模块是怎么实现的。

你可以这样回答：

> 我以销售订单模块为例说明。首先，前端页面在 `frontend/src/views/sale/SaleOrderListView.vue` 中实现，负责展示销售订单列表和处理用户点击事件，比如审核、取消、出库、回款。页面不会直接写复杂请求，而是调用 `frontend/src/api/sales.js` 中封装的方法，例如 `fetchSales`、`auditSales`、`outboundSales`。这些方法通过统一的 Axios 实例发送请求，Axios 在 `http.ts` 中配置了 `/api` 基础路径和 token 拦截器。
>
> 请求到达后端后，会进入 `SalesController`。Controller 根据不同接口路径调用 `SalesService`。比如查询列表调用 `list`，审核调用 `audit`，出库调用 `outbound`。真正的业务逻辑在 `SalesService` 中完成，包括订单状态判断、用户权限判断、库存校验、金额计算、日志记录和消息提醒。
>
> Service 需要读写数据库时，会调用 `SalesMapper`。Mapper 使用 MyBatis 注解或 SqlProvider 执行 SQL。销售订单主要操作 `sales_orders` 表，回款会操作 `payment_records` 表，出库会操作 `inventories` 和 `inventory_records` 表，业务追溯会写入 `trace_records`。最后后端通过统一的 `ApiResponse` 返回结果，前端收到数据后刷新页面。

---

## 15. 各层职责的通俗比喻

为了更好记，可以这样理解：

```text
Vue 页面
  像前台窗口，负责和用户打交道。

api 文件
  像电话簿，记录要打给后端哪个接口。

http.ts
  像总机，所有电话都经过它，它会加 token，也会处理错误。

Controller
  像接待员，负责接收请求，然后转给业务人员。

Service
  像业务经理，负责判断规则、处理流程、决定怎么做。

Mapper
  像数据库操作员，负责查表、插入、更新。

数据库
  像档案室，保存所有真实数据。
```

---

## 16. 如果老师让你画流程图

可以画这个通用图：

```text
用户点击按钮
    |
    v
Vue 页面 View.vue
    |
    v
前端 api/*.js
    |
    v
Axios http.ts
    |
    v
后端 Controller
    |
    v
后端 Service
    |
    v
MyBatis Mapper
    |
    v
MySQL 数据库
```

销售出库专用图：

```text
点击出库
  -> 前端调用 outboundSales
  -> POST /api/sales/{id}/outbound
  -> SalesController.outbound
  -> SalesService.outbound
  -> 校验订单状态
  -> 校验仓库
  -> 查询库存
  -> 判断库存是否足够
  -> 扣减库存
  -> 更新销售单状态
  -> 写库存流水
  -> 写追溯记录
  -> 返回前端
```

采购入库专用图：

```text
点击入库
  -> 前端调用 inbound 接口
  -> POST /api/purchases/{id}/inbound
  -> PurchaseController.inbound
  -> PurchaseService.inbound
  -> 校验采购单状态
  -> 校验仓库
  -> 增加库存
  -> 更新采购单状态
  -> 写库存流水
  -> 写追溯记录
  -> 返回前端
```

---

## 17. 修改代码时的安全顺序

现场改代码不要慌，按这个顺序：

```text
1. 先确定是前端问题还是后端问题
2. 如果是显示问题，先找 views
3. 如果是请求问题，找 api
4. 如果是接口问题，找 Controller
5. 如果是业务规则问题，找 Service
6. 如果是数据问题，找 Mapper 和 schema.sql
7. 改完后刷新页面或重启服务验证
```

判断方法：

```text
页面文字不对:
  多半改 View.vue 或 route-map.ts

按钮没有出现:
  看 View.vue 的 v-if 和 v-permission

接口请求失败:
  看 api 文件和 Controller 路径是否对应

返回数据缺字段:
  看 Model、Mapper 查询 SQL、数据库字段

业务规则不对:
  看 Service

数据库没保存:
  看 Mapper insert/update SQL
```

---

## 18. 答辩常见问题和参考回答

### 18.1 你这个项目是前后端分离吗

参考回答：

> 是。前端使用 Vue 3 开发页面，后端使用 Spring Boot 提供 `/api` 接口。前端通过 Axios 请求后端接口，后端返回 JSON 数据。项目构建后，前端静态资源会打包到后端 `static` 目录，由 Spring Boot 统一提供访问。

### 18.2 前端怎么请求后端

参考回答：

> 前端请求统一封装在 `frontend/src/api` 目录中，底层使用 Axios。Axios 实例在 `http.ts` 中统一配置了 `baseURL: /api`，并通过请求拦截器自动添加登录 token。页面只需要调用封装好的业务方法，比如 `fetchSales()`，不需要每个页面重复写完整请求逻辑。

### 18.3 后端接口怎么设计

参考回答：

> 后端接口按业务模块划分，比如销售模块是 `/api/sales`，采购模块是 `/api/purchases`，库存模块是 `/api/inventories` 或 `/api/inventory-records`。Controller 负责接收请求，Service 处理业务逻辑，Mapper 操作数据库。除文件下载接口外，后端统一返回 `ApiResponse`。

### 18.4 你怎么保证业务规则不被绕过

参考回答：

> 前端会做按钮显示和基础校验，但关键业务规则一定放在后端 Service 层。比如销售出库，前端只在审核通过后显示按钮，但后端仍然会校验订单状态、用户角色、仓库是否存在、库存是否足够。这样即使用户绕过前端直接调用接口，后端也能保证数据安全。

### 18.5 数据库怎么设计

参考回答：

> 数据库按照业务模块拆表。用户权限相关有 `users`、`roles`、`permissions`、`role_permissions`；基础资料有 `products`、`suppliers`、`customers`、`warehouses`；业务单据有 `purchase_orders` 和 `sales_orders`；库存有 `inventories` 和 `inventory_records`；日志追溯有 `trace_records`、`operation_logs`、`login_logs`。这样既能保存业务结果，也能保存操作过程。

### 18.6 为什么要有库存流水表

参考回答：

> 库存表只保存当前库存数量，但不知道这个数量是怎么变化来的。所以需要库存流水表记录每一次库存变化，包括变化前数量、变化后数量、变化原因、操作人和时间。这样可以实现库存追溯，也方便排查数据问题。

### 18.7 为什么 Controller 不直接操作数据库

参考回答：

> 如果 Controller 直接操作数据库，会导致接口层和业务逻辑耦合，代码难维护。项目采用 Controller、Service、Mapper 分层，Controller 只负责接收请求，Service 负责业务规则，Mapper 负责数据库操作。这样职责清晰，也方便后期修改和测试。

### 18.8 为什么要有统一返回 ApiResponse

参考回答：

> 统一返回结构可以让前端用同一种方式处理接口结果。后端返回 `code`、`message`、`data`，前端响应拦截器只要判断 `code` 是否为 0，就知道请求是否成功。这样减少重复代码，也方便统一错误提示。

### 18.9 登录 token 是怎么用的

参考回答：

> 用户登录成功后，后端生成 token 并保存到 `user_sessions` 表，前端把 token 保存到浏览器本地。之后每次请求，前端都会在请求头中带上 `Authorization: Bearer token`。后端拦截器会校验 token 是否有效，并把用户信息放到请求上下文中，业务接口就可以知道当前是谁在操作。

### 18.10 如果让你新增一个字段，你怎么改

参考回答：

> 我会先改数据库表结构，在 `schema.sql` 中增加字段；然后改后端请求对象和返回对象；再改 Mapper 的 insert、update、select SQL；如果 Service 有业务规则，也要同步处理；最后改前端表单和列表展示。这样从数据库到后端再到前端形成完整闭环。

---

## 19. 现场修改示例一：销售订单列表新增“创建人”列

需求：

```text
在销售订单列表中显示创建人
```

修改思路：

```text
1. 确认数据库 sales_orders 有 created_by 字段
2. 确认 SalesMapper 查询时返回 createdBy
3. 确认 SalesOrderItem 有 createdBy 属性
4. 在 SaleOrderListView.vue 的 columns 中加一列
```

可能涉及文件：

```text
frontend/src/views/sale/SaleOrderListView.vue
backend/src/main/java/com/example/tobacco/model/SalesOrderItem.java
backend/src/main/java/com/example/tobacco/mapper/sales/SalesMapper.java
```

答辩解释：

> 因为 `created_by` 字段已经在销售订单表中存在，所以先看后端是否已经查询并返回。如果后端已经返回 `createdBy`，那只需要在前端表格列配置中新增一列即可。如果没有返回，就需要补充 Mapper 查询字段和返回模型字段。

---

## 20. 现场修改示例二：销售出库增加库存下限限制

需求：

```text
出库后库存不能低于 10
```

修改位置：

```text
backend/src/main/java/com/example/tobacco/sales/SalesService.java
```

为什么改这里：

```text
这是业务规则，必须放在后端 Service 中。
```

修改思路：

```text
1. 在销售出库方法中查询 beforeQty
2. 计算 afterQty = beforeQty - 出库数量
3. 判断 afterQty 是否小于 10
4. 如果小于 10，抛出业务异常
5. 如果不小于 10，继续扣减库存
```

答辩解释：

> 这个限制不能只放在前端，因为前端校验可以被绕过。应该在 `SalesService.outbound` 中增加判断，在真正扣减库存前计算出库后的库存数量，如果不满足规则就拒绝操作。

---

## 21. 现场修改示例三：修改菜单名称

需求：

```text
把“销售订单”改成“销售单管理”
```

可能修改两个地方：

页面标题：

```text
frontend/src/views/sale/SaleOrderListView.vue
```

菜单标题：

```text
frontend/src/router/route-map.ts
```

答辩解释：

> 如果只是页面内部标题，就改页面文件；如果左侧菜单也要变，就改路由配置中的 `meta.title`。

---

## 22. 现场修改示例四：新增一个查询条件

需求：

```text
销售订单列表按客户名称搜索
```

完整修改链路：

```text
1. 前端页面增加搜索输入框:
   SaleOrderListView.vue

2. 前端 api 方法增加参数:
   sales.js

3. 后端 Controller 接收请求参数:
   SalesController.java

4. Service 传递参数:
   SalesService.java

5. Mapper SQL 增加 where 条件:
   SalesMapper.java 或 SalesSqlProvider.java
```

答辩解释：

> 查询条件是从前端输入开始，一直传到 Mapper SQL 的。前端负责收集客户名称，Controller 接收参数，Service 转交给 Mapper，Mapper 在 SQL 中增加客户名称模糊查询条件。

---

## 23. 各业务状态怎么记

### 23.1 销售订单状态

```text
CREATED       待审核
APPROVED      已审核，待出库
REJECTED      已驳回
CANCELLED     已取消
OUTBOUND      已出库，待回款
PARTIAL_PAID  部分回款
PAID          已回款
```

销售主流程：

```text
CREATED -> APPROVED -> OUTBOUND -> PARTIAL_PAID -> PAID
```

异常流程：

```text
CREATED -> REJECTED
CREATED/REJECTED -> CANCELLED
```

### 23.2 采购订单状态

```text
CREATED   待审核
APPROVED  已审核
REJECTED  已驳回
CANCELLED 已取消
RECEIVED  已到货
INBOUND   已入库
```

采购主流程：

```text
CREATED -> APPROVED -> RECEIVED -> INBOUND
```

---

## 24. 运行和验证怎么说

项目运行约定：

```text
前端构建:
cd frontend
npm install
npm run build

后端启动:
cd backend
mvn spring-boot:run
```

访问地址：

```text
http://localhost:8080
```

前端开发调试地址：

```text
http://localhost:5173
```

答辩可说：

> 常规演示时，先构建前端，构建结果会输出到后端静态资源目录，然后启动 Spring Boot。最终通过 `http://localhost:8080` 访问系统，页面和后端接口都由 Spring Boot 统一提供。

---

## 25. 你最需要记住的 10 句话

1. 本项目采用 Vue 3 + Spring Boot + MyBatis + MySQL 的前后端分离架构。
2. 前端页面在 `frontend/src/views`，接口请求在 `frontend/src/api`。
3. 后端采用 Controller、Service、Mapper 分层。
4. Controller 负责接收请求，Service 负责业务逻辑，Mapper 负责数据库操作。
5. 前端通过 Axios 请求后端，统一封装在 `http.ts`。
6. 后端接口统一以 `/api` 开头，统一返回 `ApiResponse`。
7. 登录成功后前端保存 token，后续请求自动携带 `Authorization` 请求头。
8. 销售出库会扣减库存，采购入库会增加库存。
9. `inventories` 保存当前库存，`inventory_records` 保存库存变化历史。
10. 现场改代码时，先从页面找按钮事件，再找 api、Controller、Service、Mapper 和数据库表。

---

## 26. 最后答辩总结模板

答辩最后可以这样总结：

> 总体来说，这个项目的实现思路是比较清晰的。前端负责页面展示、表单输入和按钮操作，后端负责接口、业务规则和数据处理，数据库负责持久化保存业务数据。以销售订单为例，用户在页面点击操作后，前端通过 api 文件调用后端接口，后端 Controller 接收请求，Service 完成状态判断、库存校验、日志追溯等业务逻辑，Mapper 执行 SQL 操作数据库，最后把结果返回给前端刷新页面。后续如果要修改功能，我会先根据页面找到对应 View 文件，再顺着 api、Controller、Service、Mapper 和数据库表逐层定位。

---

## 27. 考前复习建议

如果时间很少，优先看这些文件：

```text
1. 前端路由:
   frontend/src/router/route-map.ts

2. 前端请求封装:
   frontend/src/api/http.ts

3. 销售页面:
   frontend/src/views/sale/SaleOrderListView.vue
   frontend/src/views/sale/SaleOrderFormView.vue
   frontend/src/views/sale/SaleOutboundView.vue

4. 销售接口:
   frontend/src/api/sales.js
   backend/src/main/java/com/example/tobacco/sales/SalesController.java
   backend/src/main/java/com/example/tobacco/sales/SalesService.java
   backend/src/main/java/com/example/tobacco/mapper/sales/SalesMapper.java

5. 采购接口:
   backend/src/main/java/com/example/tobacco/purchase/PurchaseController.java
   backend/src/main/java/com/example/tobacco/purchase/PurchaseService.java

6. 库存接口:
   backend/src/main/java/com/example/tobacco/inventory/InventoryController.java
   backend/src/main/java/com/example/tobacco/inventory/InventoryService.java

7. 数据库:
   backend/src/main/resources/sql/schema.sql
```

复习顺序：

```text
先看 route-map.ts 知道页面在哪
再看 SaleOrderListView.vue 知道按钮在哪
再看 sales.js 知道请求去哪
再看 SalesController 知道接口怎么接
再看 SalesService 知道业务怎么处理
再看 SalesMapper 知道数据库怎么改
最后看 schema.sql 知道表结构
```

记住这条线，答辩时就不会乱。
