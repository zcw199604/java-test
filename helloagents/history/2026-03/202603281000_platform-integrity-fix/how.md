# 技术设计: 平台功能完整性修复（10项高/中优先级问题）

## 技术方案

### 核心技术
- Java 8 + Spring Boot 2.7 + JdbcTemplate
- Vue 3 + Element Plus + TypeScript
- MySQL DDL (`CREATE TABLE IF NOT EXISTS`)
- Apache POI 5.2.5（Excel 导入）

### 实现要点
- 所有新增表使用 `IF NOT EXISTS` 确保幂等
- 订单审核采用简单状态机，不引入工作流引擎
- 消息推送采用同步写库方式，前端轮询拉取（不引入 WebSocket）
- Excel 导入复用现有 `ExcelUtil.importWorkbook()`，新增业务校验层
- 追溯记录通过在 Service 方法中直接调用 `AuditService.trace()` 实现

---

## 问题 #1: schema.sql 缺失表定义

### 数据模型

```sql
-- 验证码记录
CREATE TABLE IF NOT EXISTS captcha_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uuid VARCHAR(64) NOT NULL UNIQUE,
    code VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'NEW',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at DATETIME NOT NULL
);

-- 用户会话
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(128) NOT NULL UNIQUE,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 密码重置记录
CREATE TABLE IF NOT EXISTS password_reset_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(128) NOT NULL UNIQUE,
    status VARCHAR(16) NOT NULL DEFAULT 'NEW',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at DATETIME NOT NULL
);

-- 权限定义
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(128) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    module VARCHAR(64),
    remark VARCHAR(255)
);

-- 角色权限关联
CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL,
    permission_code VARCHAR(128) NOT NULL,
    UNIQUE KEY uk_role_perm (role_code, permission_code)
);

-- 系统配置
CREATE TABLE IF NOT EXISTS system_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(128) NOT NULL UNIQUE,
    config_value VARCHAR(512),
    remark VARCHAR(255),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 仓库
CREATE TABLE IF NOT EXISTS warehouses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL UNIQUE,
    address VARCHAR(255),
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED'
);

-- 用户数据范围
CREATE TABLE IF NOT EXISTS user_data_scopes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    scope_type VARCHAR(64) NOT NULL,
    scope_value VARCHAR(255) NOT NULL
);

-- 登录日志
CREATE TABLE IF NOT EXISTS login_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    username VARCHAR(64),
    ip VARCHAR(64),
    device VARCHAR(255),
    status VARCHAR(16) NOT NULL,
    message VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 操作日志
CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    username VARCHAR(64),
    module VARCHAR(64),
    action VARCHAR(64),
    biz_type VARCHAR(64),
    biz_id BIGINT,
    detail TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 追溯记录
CREATE TABLE IF NOT EXISTS trace_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    biz_type VARCHAR(64) NOT NULL,
    biz_id BIGINT,
    order_no VARCHAR(64),
    node_code VARCHAR(64) NOT NULL,
    node_name VARCHAR(128) NOT NULL,
    operator VARCHAR(64),
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 异常单据
CREATE TABLE IF NOT EXISTS abnormal_documents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    biz_type VARCHAR(64) NOT NULL,
    biz_id BIGINT,
    order_no VARCHAR(64),
    abnormal_type VARCHAR(64) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    reported_by VARCHAR(64),
    audited_by VARCHAR(64),
    detail TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 消息
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    message_type VARCHAR(32) NOT NULL,
    biz_type VARCHAR(64),
    biz_id BIGINT,
    is_read TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at DATETIME
);
```

---

## 问题 #2: 采购/销售订单审核流程

### 状态流转设计

```
采购单: CREATED → APPROVED → RECEIVED → INBOUND
                ↘ REJECTED
        CREATED/REJECTED → CANCELLED

销售单: CREATED → APPROVED → OUTBOUND → PARTIAL_PAID → PAID
                ↘ REJECTED
        CREATED/REJECTED → CANCELLED
```

### API设计

#### POST /api/purchases/{id}/audit
- **请求:** `{ "decision": "APPROVED|REJECTED", "remark": "审核意见" }`
- **响应:** `PurchaseOrderItem`
- **逻辑:** 仅 ADMIN 角色可操作，仅 CREATED 状态可审核

#### POST /api/purchases/{id}/cancel
- **请求:** `{ "reason": "取消原因" }`
- **响应:** `PurchaseOrderItem`
- **逻辑:** 仅 CREATED/REJECTED 状态可取消

#### POST /api/sales/{id}/audit
- **请求:** `{ "decision": "APPROVED|REJECTED", "remark": "审核意见" }`
- **响应:** `SalesOrderItem`

#### POST /api/sales/{id}/cancel
- **请求:** `{ "reason": "取消原因" }`
- **响应:** `SalesOrderItem`

### 数据模型变更

```sql
-- purchase_orders 新增字段
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS audited_by VARCHAR(64) NULL;
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS audited_at DATETIME NULL;
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS audit_remark VARCHAR(255) NULL;
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS cancel_reason VARCHAR(255) NULL;

-- sales_orders 新增字段
ALTER TABLE sales_orders ADD COLUMN IF NOT EXISTS audited_by VARCHAR(64) NULL;
ALTER TABLE sales_orders ADD COLUMN IF NOT EXISTS audited_at DATETIME NULL;
ALTER TABLE sales_orders ADD COLUMN IF NOT EXISTS audit_remark VARCHAR(255) NULL;
ALTER TABLE sales_orders ADD COLUMN IF NOT EXISTS cancel_reason VARCHAR(255) NULL;
```

### 业务规则变更
- `PurchaseService.receive()`: 前置条件从 `status=CREATED` 改为 `status=APPROVED`
- `SalesService.outbound()`: 前置条件从 `status=CREATED` 改为 `status=APPROVED`

---

## 问题 #3: 采购/销售订单取消功能

与问题 #2 合并实现，共享 cancel 接口和 CANCELLED 状态。

---

## 问题 #4: Excel 导入业务入口

### API设计

#### POST /api/purchases/import
- **请求:** `multipart/form-data`，字段 `file`（Excel 文件）
- **响应:** `{ "success": N, "failed": M, "errors": [...] }`
- **表头规范:** 供应商名称、商品编码、数量、单价

#### POST /api/sales/import
- **请求:** `multipart/form-data`，字段 `file`
- **响应:** 同上
- **表头规范:** 客户名称、商品编码、数量、单价

#### POST /api/inventories/import
- **请求:** `multipart/form-data`，字段 `file`
- **响应:** 同上
- **表头规范:** 商品编码、仓库名称、数量、预警阈值

### 实现要点
- 复用 `ExcelUtil.importWorkbook()` 读取数据
- 新增 `ExcelUtil.require/toInteger/toBigDecimal` 做字段校验
- 逐行校验 → 收集错误 → 批量写入成功行 → 返回汇总结果
- 事务策略: 整批成功或整批回滚（单事务）

---

## 问题 #5: 消息自动推送机制

### 实现要点

在以下业务节点注入 `MessageService.createMessage()` 调用:

| 触发节点 | 消息接收人 | messageType | bizType | 消息标题模板 |
|---------|----------|-------------|---------|------------|
| 采购入库后库存低于预警 | 库管人员(KEEPER) | ALERT | INVENTORY | "库存预警: {商品名} 当前库存{数量}" |
| 销售出库后库存低于预警 | 库管人员(KEEPER) | ALERT | INVENTORY | 同上 |
| 订单审核通过 | 订单创建者 | NOTICE | PURCHASE/SALES | "订单{单号}审核通过" |
| 订单审核驳回 | 订单创建者 | NOTICE | PURCHASE/SALES | "订单{单号}被驳回: {原因}" |
| 异常单据上报 | ADMIN用户 | ALERT | ABNORMAL | "异常单据: {单号}" |

### 接收人查询逻辑
- 按 role_code 查询用户ID: `SELECT id FROM users WHERE role_code=? AND status='ENABLED'`
- 全局通知: user_id 传 null

---

## 问题 #6: 库存调拨逻辑修复

### 当前问题
`InventoryService.transfer()` 中 `change_qty=0, afterQty=beforeQty`，未实际扣减库存。

### 修复方案
```java
public String transfer(InventoryChangeRequest request, String operatorName) {
    Integer beforeQty = ...; // 现有逻辑
    if (beforeQty == null) throw ...;
    int changeQty = request.getQuantity(); // 调拨数量（负数表示调出）
    if (beforeQty < changeQty) throw new IllegalArgumentException("库存不足，无法调拨");
    int afterQty = beforeQty - changeQty;
    jdbcTemplate.update("update inventories set quantity=? where product_id=?", afterQty, request.getProductId());
    // 插入流水记录，change_qty 为 -changeQty
    ...
}
```

### 前端适配
`InventoryFlowView.vue` 调拨表单增加数量校验提示。

---

## 问题 #7: 追溯记录自动写入

### 实现要点

在 Service 方法中直接调用现有 `AuditService.trace()`:

| 业务节点 | biz_type | node_code | node_name |
|---------|----------|-----------|-----------|
| 采购订单创建 | PURCHASE | CREATE | 创建采购单 |
| 采购订单审核 | PURCHASE | AUDIT | 审核采购单 |
| 采购到货登记 | PURCHASE | RECEIVE | 到货登记 |
| 采购入库 | PURCHASE | INBOUND | 采购入库 |
| 销售订单创建 | SALES | CREATE | 创建销售单 |
| 销售订单审核 | SALES | AUDIT | 审核销售单 |
| 销售出库 | SALES | OUTBOUND | 销售出库 |
| 销售回款 | SALES | PAYMENT | 销售回款 |

### 注入方式
- `PurchaseService` 和 `SalesService` 构造器注入 `AuditService`
- 在每个业务方法末尾调用 `auditService.trace(...)`

---

## 问题 #8: 销售信息发布模块

### 数据模型

```sql
CREATE TABLE IF NOT EXISTS bulletins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    category VARCHAR(64) NOT NULL DEFAULT 'NOTICE',
    status VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED',
    created_by VARCHAR(64),
    expired_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### API设计

#### GET /api/bulletins
- **响应:** 公告列表（按创建时间倒序）

#### POST /api/bulletins
- **请求:** `{ "title": "", "content": "", "category": "NOTICE|PROMOTION", "expiredAt": "" }`
- **权限:** SELLER / ADMIN

### 前端
- 新增 `SaleBulletinView.vue` 页面，含公告列表和新建表单
- 路由: `/sale/bulletin`
- 菜单: 销售管理 → 销售信息发布

---

## 问题 #9: 异常单据审核接口

### API设计

#### POST /api/reports/abnormal-docs/{id}/audit
- **请求:** `{ "decision": "APPROVED|REJECTED", "remark": "审核意见" }`
- **响应:** 更新后的异常单据
- **逻辑:** 仅 ADMIN 角色，仅 PENDING 状态可审核
- **字段更新:** `status=APPROVED/REJECTED`, `audited_by=当前用户`

### 前端适配
`ExceptionAuditView.vue` 已有审核对话框 UI，需确认 API 调用路径正确。

---

## 问题 #10: 权限初始数据

### seed.sql 新增数据

权限定义覆盖以下模块:
- `dashboard:view` - 驾驶舱
- `purchase:view/create/audit/cancel/import` - 采购管理
- `sales:view/create/audit/cancel/import` - 销售管理
- `inventory:view/transfer/check/import` - 库存管理
- `report:view/export` - 报表
- `admin:user/role/config/log/base-data` - 系统管理
- `trace:view` - 合规追溯
- `audit:view/process` - 异常审核
- `bulletin:view/create` - 销售信息发布

角色权限映射:
- **ADMIN:** 全部权限
- **PURCHASER:** dashboard:view, purchase:*, inventory:view, report:view
- **SELLER:** dashboard:view, sales:*, bulletin:*, report:view
- **KEEPER:** dashboard:view, inventory:*, report:view

---

## 安全与性能

- **安全:** Excel 导入需校验文件大小（≤5MB）和行数（≤1000行），防止 DoS
- **安全:** 审核/取消接口需验证操作者角色权限
- **性能:** 消息推送为同步写库，对单条操作性能影响可忽略

## 测试与部署

- **测试:** 现有 `PlatformIntegrationTest` 已包含审核接口测试用例，修复后应能通过
- **部署:** schema.sql 全部 `IF NOT EXISTS`，可直接在已有环境执行
