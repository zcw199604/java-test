DELETE FROM abnormal_documents;
DELETE FROM trace_records;
DELETE FROM warning_records;
DELETE FROM inventory_check_reports;
DELETE FROM inventory_records;
DELETE FROM inventories;
DELETE FROM receivable_records;
DELETE FROM payment_records;
DELETE FROM sales_orders;
DELETE FROM sales_publishings;
DELETE FROM purchase_order_tracks;
DELETE FROM purchase_orders;
DELETE FROM purchase_requisitions;
DELETE FROM customers;
DELETE FROM suppliers;
DELETE FROM products;
DELETE FROM warehouses;
DELETE FROM categories;
DELETE FROM messages;
DELETE FROM system_configs;
DELETE FROM password_reset_records;
DELETE FROM captcha_records;
DELETE FROM operation_logs;
DELETE FROM login_logs;
DELETE FROM user_sessions;
DELETE FROM user_data_scopes;
DELETE FROM users;
DELETE FROM role_permissions;
DELETE FROM permissions;
DELETE FROM roles;

INSERT INTO roles (id, code, name, remark, status) VALUES
(1, 'SUPER_ADMIN', '超级管理员', '负责平台全局权限与配置', 'ENABLED'),
(2, 'PURCHASER', '采购专员', '负责采购提报、采购单与入库', 'ENABLED'),
(3, 'SELLER', '销售专员', '负责销售信息、销售单与回款', 'ENABLED'),
(4, 'KEEPER', '库管人员', '负责库存维护、盘点与预警', 'ENABLED');

INSERT INTO permissions (id, code, name, module, action, remark) VALUES
(1, 'system:user:view', '查看用户', 'SYSTEM', 'VIEW', '账号查询'),
(2, 'system:user:edit', '维护用户', 'SYSTEM', 'EDIT', '账号管理'),
(3, 'system:role:edit', '维护角色', 'SYSTEM', 'EDIT', '角色权限管理'),
(4, 'system:config:edit', '维护系统配置', 'SYSTEM', 'EDIT', '系统配置'),
(5, 'purchase:order:edit', '维护采购单', 'PURCHASE', 'EDIT', '采购单增改审取消'),
(6, 'purchase:inbound', '采购入库', 'PURCHASE', 'INBOUND', '采购到货入库'),
(7, 'sales:order:edit', '维护销售单', 'SALES', 'EDIT', '销售单增改审取消'),
(8, 'sales:payment', '销售回款', 'SALES', 'PAYMENT', '销售回款'),
(9, 'inventory:manage', '库存维护', 'INVENTORY', 'EDIT', '库存台账维护'),
(10, 'report:view', '查看报表', 'REPORT', 'VIEW', '统计报表'),
(11, 'logs:view', '查看日志', 'AUDIT', 'VIEW', '登录与操作日志'),
(12, 'message:view', '查看消息', 'MESSAGE', 'VIEW', '消息通知');

INSERT INTO role_permissions (role_code, permission_code) VALUES
('SUPER_ADMIN', 'system:user:view'),
('SUPER_ADMIN', 'system:user:edit'),
('SUPER_ADMIN', 'system:role:edit'),
('SUPER_ADMIN', 'system:config:edit'),
('SUPER_ADMIN', 'purchase:order:edit'),
('SUPER_ADMIN', 'purchase:inbound'),
('SUPER_ADMIN', 'sales:order:edit'),
('SUPER_ADMIN', 'sales:payment'),
('SUPER_ADMIN', 'inventory:manage'),
('SUPER_ADMIN', 'report:view'),
('SUPER_ADMIN', 'logs:view'),
('SUPER_ADMIN', 'message:view'),
('PURCHASER', 'purchase:order:edit'),
('PURCHASER', 'purchase:inbound'),
('PURCHASER', 'message:view'),
('SELLER', 'sales:order:edit'),
('SELLER', 'sales:payment'),
('SELLER', 'message:view'),
('KEEPER', 'inventory:manage'),
('KEEPER', 'message:view'),
('KEEPER', 'report:view');

INSERT INTO users (id, username, password, real_name, role_code, email, phone, status) VALUES
(1, 'admin', '123456', '系统管理员', 'SUPER_ADMIN', 'admin@example.com', '13800000000', 'ENABLED'),
(2, 'buyer', '123456', '采购张三', 'PURCHASER', 'buyer@example.com', '13800000001', 'ENABLED'),
(3, 'seller', '123456', '销售李四', 'SELLER', 'seller@example.com', '13800000002', 'ENABLED'),
(4, 'keeper', '123456', '库管王五', 'KEEPER', 'keeper@example.com', '13800000003', 'ENABLED');

INSERT INTO user_data_scopes (user_id, scope_type, scope_value) VALUES
(1, 'ALL', 'ALL'),
(2, 'WAREHOUSE', 'WH-001'),
(3, 'CUSTOMER', 'ALL'),
(4, 'WAREHOUSE', 'WH-001');

INSERT INTO system_configs (config_key, config_value, config_group, remark, updated_by) VALUES
('inventory.warning.threshold.default', '20', 'WARNING', '默认库存预警阈值', 'system'),
('cache.menu.ttl.seconds', '300', 'CACHE', '菜单缓存秒数', 'system'),
('profile.password.policy', 'min6', 'SECURITY', '密码最小长度要求', 'system');

INSERT INTO categories (id, name, remark, status) VALUES
(1, '高端卷烟', '高端系列', 'ENABLED'),
(2, '中端卷烟', '中端系列', 'ENABLED'),
(3, '经典卷烟', '经典系列', 'ENABLED');

INSERT INTO warehouses (id, code, name, address, status) VALUES
(1, 'WH-001', '中心仓', '武汉市仓储园区', 'ENABLED'),
(2, 'WH-002', '调拨仓', '武汉市备用仓', 'ENABLED');

INSERT INTO suppliers (id, name, contact_name, contact_phone, address, license_no, status) VALUES
(1, '省烟草供应中心', '赵经理', '13800000001', '湖北省武汉市', 'SUP-001', 'ENABLED'),
(2, '华中配送有限公司', '钱经理', '13800000002', '湖南省长沙市', 'SUP-002', 'ENABLED');

INSERT INTO customers (id, name, contact_name, contact_phone, address, credit_limit, status) VALUES
(1, '武汉零售客户A', '陈老板', '13900000001', '武汉市江汉区', 50000, 'ENABLED'),
(2, '长沙零售客户B', '周老板', '13900000002', '长沙市雨花区', 35000, 'ENABLED');

INSERT INTO products (id, code, name, category, unit, unit_price, warning_threshold, default_supplier_id, compliance_code, status) VALUES
(1, 'TB-001', '中华（硬）', '高端卷烟', '条', 450.00, 20, 1, 'CMP-001', 'ENABLED'),
(2, 'TB-002', '玉溪（软）', '中端卷烟', '条', 230.00, 30, 2, 'CMP-002', 'ENABLED'),
(3, 'TB-003', '黄鹤楼（软蓝）', '经典卷烟', '条', 190.00, 15, 1, 'CMP-003', 'ENABLED');

INSERT INTO purchase_requisitions (id, req_no, product_id, quantity, reason, status, created_by) VALUES
(1, 'PR202603240001', 1, 20, '节前备货', 'CREATED', 'buyer');

INSERT INTO purchase_orders (id, order_no, requisition_id, supplier_id, product_id, warehouse_id, quantity, unit_price, total_amount, status, audit_status, audit_by, audit_at, created_by, received_at, inbound_at) VALUES
(1, 'PO20260323001', 1, 1, 1, 1, 20, 420.00, 8400.00, 'INBOUND', 'APPROVED', 'admin', NOW(), 'buyer', NOW(), NOW()),
(2, 'PO20260323002', NULL, 2, 3, 1, 12, 175.00, 2100.00, 'RECEIVED', 'APPROVED', 'admin', NOW(), 'buyer', NOW(), NULL),
(3, 'PO20260323003', NULL, 1, 2, 1, 15, 210.00, 3150.00, 'CREATED', 'PENDING', NULL, NULL, 'buyer', NULL, NULL);

INSERT INTO purchase_order_tracks (purchase_order_id, node_code, node_name, operator, remark) VALUES
(1, 'CREATED', '采购单创建', 'buyer', '初始化数据'),
(1, 'APPROVED', '采购审核', 'admin', '审核通过'),
(1, 'RECEIVED', '采购到货', 'buyer', '初始化到货'),
(1, 'INBOUND', '采购入库', 'buyer', '初始化入库');

INSERT INTO sales_publishings (id, publish_no, title, content, product_id, price, status, created_by) VALUES
(1, 'SP202603240001', '春季畅销促销', '重点推广中端卷烟', 2, 250.00, 'PUBLISHED', 'seller');

INSERT INTO inventories (id, product_id, warehouse_id, warehouse_name, quantity, warning_threshold, locked_qty) VALUES
(1, 1, 1, '中心仓', 40, 20, 0),
(2, 2, 1, '中心仓', 55, 30, 0),
(3, 3, 1, '中心仓', 18, 15, 0),
(4, 1, 2, '调拨仓', 5, 20, 0);

INSERT INTO inventory_records (id, product_id, warehouse_id, biz_type, biz_id, change_qty, before_qty, after_qty, remark, source_module, source_order_no) VALUES
(1, 1, 1, 'PURCHASE_INBOUND', 1, 20, 20, 40, '初始化采购入库', 'PURCHASE', 'PO20260323001'),
(2, 2, 1, 'INITIAL', NULL, 55, 0, 55, '初始化库存', 'SYSTEM', 'INIT'),
(3, 3, 1, 'INITIAL', NULL, 18, 0, 18, '初始化库存', 'SYSTEM', 'INIT');

INSERT INTO sales_orders (id, order_no, publish_id, customer_id, product_id, warehouse_id, quantity, unit_price, total_amount, paid_amount, status, audit_status, audit_by, audit_at, receivable_status, created_by, outbound_at) VALUES
(1, 'SO20260323001', 1, 1, 2, 1, 10, 250.00, 2500.00, 2500.00, 'PAID', 'APPROVED', 'admin', NOW(), 'PAID', 'seller', NOW()),
(2, 'SO20260323002', NULL, 2, 1, 1, 6, 470.00, 2820.00, 0.00, 'CREATED', 'PENDING', NULL, NULL, 'UNPAID', 'seller', NULL);

INSERT INTO payment_records (id, sales_order_id, amount, payer_name, remark) VALUES
(1, 1, 2500.00, '陈老板', '初始化全额回款');

INSERT INTO receivable_records (id, sales_order_id, amount, status, due_date, paid_at, remark) VALUES
(1, 1, 2500.00, 'PAID', NOW(), NOW(), '初始化全额回款');

INSERT INTO trace_records (biz_type, biz_id, order_no, node_code, node_name, operator, remark) VALUES
('PURCHASE', 1, 'PO20260323001', 'INBOUND', '采购入库', 'buyer', '初始化追溯'),
('SALES', 1, 'SO20260323001', 'PAID', '销售回款', 'seller', '初始化追溯');

INSERT INTO messages (user_id, title, content, message_type, biz_type, biz_id, is_read) VALUES
(NULL, '系统初始化', '任务书对齐版后端数据已初始化', 'SYSTEM', 'SYSTEM', NULL, 0);
