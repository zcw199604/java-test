CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    remark VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    real_name VARCHAR(64) NOT NULL,
    role_code VARCHAR(64) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL UNIQUE,
    remark VARCHAR(255),
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED'
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    category VARCHAR(64) NOT NULL,
    unit VARCHAR(32) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    warning_threshold INT NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED'
);

CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    contact_name VARCHAR(64),
    contact_phone VARCHAR(32),
    address VARCHAR(255),
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED'
);

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    contact_name VARCHAR(64),
    contact_phone VARCHAR(32),
    address VARCHAR(255),
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED'
);

CREATE TABLE IF NOT EXISTS purchase_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_by VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    received_at DATETIME NULL,
    inbound_at DATETIME NULL
);

SET @purchase_received_at_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'purchase_orders'
      AND COLUMN_NAME = 'received_at'
);
SET @purchase_received_at_ddl = IF(
    @purchase_received_at_exists = 0,
    'ALTER TABLE purchase_orders ADD COLUMN received_at DATETIME NULL',
    'SELECT 1'
);
PREPARE purchase_received_at_stmt FROM @purchase_received_at_ddl;
EXECUTE purchase_received_at_stmt;
DEALLOCATE PREPARE purchase_received_at_stmt;

CREATE TABLE IF NOT EXISTS inventories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL UNIQUE,
    warehouse_name VARCHAR(128) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    warning_threshold INT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    biz_type VARCHAR(32) NOT NULL,
    biz_id BIGINT NULL,
    change_qty INT NOT NULL,
    before_qty INT NOT NULL,
    after_qty INT NOT NULL,
    operator_name VARCHAR(64) NULL,
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

SET @inventory_records_operator_name_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inventory_records'
      AND COLUMN_NAME = 'operator_name'
);
SET @inventory_records_operator_name_ddl = IF(
    @inventory_records_operator_name_exists = 0,
    'ALTER TABLE inventory_records ADD COLUMN operator_name VARCHAR(64) NULL AFTER after_qty',
    'SELECT 1'
);
PREPARE inventory_records_operator_name_stmt FROM @inventory_records_operator_name_ddl;
EXECUTE inventory_records_operator_name_stmt;
DEALLOCATE PREPARE inventory_records_operator_name_stmt;

CREATE TABLE IF NOT EXISTS sales_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL,
    created_by VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    outbound_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS payment_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sales_order_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payer_name VARCHAR(64),
    remark VARCHAR(255),
    paid_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 以下为功能完整性修复追加的表和字段（202603281000）
-- ============================================================

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

-- 销售信息发布（公告）
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

-- purchase_orders 审核/取消字段
SET @po_audited_by_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'purchase_orders' AND COLUMN_NAME = 'audited_by'
);
SET @po_audited_by_ddl = IF(@po_audited_by_exists = 0,
    'ALTER TABLE purchase_orders ADD COLUMN audited_by VARCHAR(64) NULL, ADD COLUMN audited_at DATETIME NULL, ADD COLUMN audit_remark VARCHAR(255) NULL, ADD COLUMN cancel_reason VARCHAR(255) NULL',
    'SELECT 1');
PREPARE po_audited_by_stmt FROM @po_audited_by_ddl;
EXECUTE po_audited_by_stmt;
DEALLOCATE PREPARE po_audited_by_stmt;

-- sales_orders 审核/取消字段
SET @so_audited_by_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sales_orders' AND COLUMN_NAME = 'audited_by'
);
SET @so_audited_by_ddl = IF(@so_audited_by_exists = 0,
    'ALTER TABLE sales_orders ADD COLUMN audited_by VARCHAR(64) NULL, ADD COLUMN audited_at DATETIME NULL, ADD COLUMN audit_remark VARCHAR(255) NULL, ADD COLUMN cancel_reason VARCHAR(255) NULL',
    'SELECT 1');
PREPARE so_audited_by_stmt FROM @so_audited_by_ddl;
EXECUTE so_audited_by_stmt;
DEALLOCATE PREPARE so_audited_by_stmt;
