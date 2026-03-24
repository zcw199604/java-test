CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    remark VARCHAR(255),
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED'
);

CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(128) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    module VARCHAR(64),
    action VARCHAR(64),
    remark VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL,
    permission_code VARCHAR(128) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    real_name VARCHAR(64) NOT NULL,
    role_code VARCHAR(64) NOT NULL,
    email VARCHAR(128),
    phone VARCHAR(32),
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_data_scopes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    scope_type VARCHAR(64) NOT NULL,
    scope_value VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS login_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL,
    username VARCHAR(64),
    ip VARCHAR(64),
    device VARCHAR(255),
    status VARCHAR(16),
    message VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL,
    username VARCHAR(64),
    module VARCHAR(64),
    action VARCHAR(64),
    biz_type VARCHAR(64),
    biz_id BIGINT NULL,
    detail VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS captcha_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    captcha_key VARCHAR(128) NOT NULL,
    captcha_code VARCHAR(32) NOT NULL,
    expire_at DATETIME NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS password_reset_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    reset_token VARCHAR(128) NOT NULL,
    expire_at DATETIME NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS system_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(128) NOT NULL UNIQUE,
    config_value VARCHAR(500),
    config_group VARCHAR(64),
    remark VARCHAR(255),
    updated_by VARCHAR(64),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL,
    title VARCHAR(128) NOT NULL,
    content VARCHAR(500),
    message_type VARCHAR(64),
    biz_type VARCHAR(64),
    biz_id BIGINT NULL,
    is_read TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_token VARCHAR(128) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    role_code VARCHAR(64) NOT NULL,
    expire_at DATETIME NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_access_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL UNIQUE,
    remark VARCHAR(255),
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED'
);

CREATE TABLE IF NOT EXISTS warehouses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    address VARCHAR(255),
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
    default_supplier_id BIGINT NULL,
    compliance_code VARCHAR(128),
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED'
);

CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    contact_name VARCHAR(64),
    contact_phone VARCHAR(32),
    address VARCHAR(255),
    license_no VARCHAR(128),
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED'
);

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    contact_name VARCHAR(64),
    contact_phone VARCHAR(32),
    address VARCHAR(255),
    credit_limit DECIMAL(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED'
);

CREATE TABLE IF NOT EXISTS purchase_requisitions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    req_no VARCHAR(64) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    reason VARCHAR(255),
    status VARCHAR(16) NOT NULL,
    created_by VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    audited_by VARCHAR(64),
    audited_at DATETIME NULL,
    audit_remark VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS purchase_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    requisition_id BIGINT NULL,
    supplier_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(16) NOT NULL,
    audit_status VARCHAR(16) DEFAULT 'PENDING',
    audit_by VARCHAR(64),
    audit_at DATETIME NULL,
    audit_remark VARCHAR(255),
    cancel_reason VARCHAR(255),
    excel_batch_no VARCHAR(64),
    created_by VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    received_at DATETIME NULL,
    inbound_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS purchase_order_tracks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    purchase_order_id BIGINT NOT NULL,
    node_code VARCHAR(64),
    node_name VARCHAR(128),
    operator VARCHAR(64),
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sales_publishings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    publish_no VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(128) NOT NULL,
    content VARCHAR(500),
    product_id BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_by VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sales_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    publish_id BIGINT NULL,
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL,
    audit_status VARCHAR(16) DEFAULT 'PENDING',
    audit_by VARCHAR(64),
    audit_at DATETIME NULL,
    audit_remark VARCHAR(255),
    cancel_reason VARCHAR(255),
    receivable_status VARCHAR(32) DEFAULT 'UNPAID',
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

CREATE TABLE IF NOT EXISTS receivable_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sales_order_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(32),
    due_date DATETIME NULL,
    paid_at DATETIME NULL,
    remark VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS inventories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL DEFAULT 1,
    warehouse_name VARCHAR(128) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    warning_threshold INT NOT NULL DEFAULT 0,
    locked_qty INT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_product_warehouse(product_id, warehouse_id)
);

CREATE TABLE IF NOT EXISTS inventory_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NULL,
    biz_type VARCHAR(32) NOT NULL,
    biz_id BIGINT NULL,
    change_qty INT NOT NULL,
    before_qty INT NOT NULL,
    after_qty INT NOT NULL,
    remark VARCHAR(255),
    source_module VARCHAR(64),
    source_order_no VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory_check_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_no VARCHAR(64) NOT NULL,
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    system_qty INT NOT NULL,
    actual_qty INT NOT NULL,
    profit_loss_qty INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_by VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS warning_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warning_type VARCHAR(64) NOT NULL,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    threshold INT NOT NULL,
    current_value INT NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS trace_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    biz_type VARCHAR(64) NOT NULL,
    biz_id BIGINT NOT NULL,
    order_no VARCHAR(64),
    node_code VARCHAR(64),
    node_name VARCHAR(128),
    operator VARCHAR(64),
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS abnormal_documents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    biz_type VARCHAR(64) NOT NULL,
    biz_id BIGINT NOT NULL,
    order_no VARCHAR(64),
    abnormal_type VARCHAR(64),
    status VARCHAR(16) NOT NULL,
    detail VARCHAR(255),
    reported_by VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    audited_by VARCHAR(64),
    audited_at DATETIME NULL
);
