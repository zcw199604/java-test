DELETE FROM payment_records;
DELETE FROM sales_orders;
DELETE FROM inventory_records;
DELETE FROM purchase_orders;
DELETE FROM inventories;
DELETE FROM warehouses;
DELETE FROM customers;
DELETE FROM suppliers;
DELETE FROM categories;
DELETE FROM products;
DELETE FROM user_data_scopes;
DELETE FROM role_permissions;
DELETE FROM permissions;
DELETE FROM users;
DELETE FROM roles;

INSERT INTO roles (id, code, name, remark) VALUES
(1, 'SUPER_ADMIN', '超级管理员', '拥有系统全量管理能力与最高权限'),
(2, 'ADMIN', '普通管理员', '负责账号权限、基础资料和报表管理'),
(3, 'PURCHASER', '采购专员', '负责采购订单与到货入库'),
(4, 'SELLER', '销售专员', '负责销售订单、出库与回款'),
(5, 'KEEPER', '库管人员', '负责库存台账、盘点与预警');

INSERT INTO users (id, username, password, real_name, role_code, status) VALUES
(1, 'admin', '123456', '超级管理员', 'SUPER_ADMIN', 'ENABLED'),
(2, 'manager', '123456', '普通管理员', 'ADMIN', 'ENABLED'),
(3, 'buyer', '123456', '采购张三', 'PURCHASER', 'ENABLED'),
(4, 'seller', '123456', '销售李四', 'SELLER', 'ENABLED'),
(5, 'keeper', '123456', '库管王五', 'KEEPER', 'ENABLED');

INSERT INTO categories (id, name, remark, status) VALUES
(1, '高端卷烟', '高端系列', 'ENABLED'),
(2, '中端卷烟', '中端系列', 'ENABLED'),
(3, '经典卷烟', '经典系列', 'ENABLED');

INSERT INTO products (id, code, name, category, unit, unit_price, warning_threshold, status) VALUES
(1, 'TB-001', '中华（硬）', '高端卷烟', '条', 450.00, 20, 'ENABLED'),
(2, 'TB-002', '玉溪（软）', '中端卷烟', '条', 230.00, 30, 'ENABLED'),
(3, 'TB-003', '黄鹤楼（软蓝）', '经典卷烟', '条', 190.00, 15, 'ENABLED');

INSERT INTO suppliers (id, name, contact_name, contact_phone, address, status) VALUES
(1, '省烟草供应中心', '赵经理', '13800000001', '湖北省武汉市', 'ENABLED'),
(2, '华中配送有限公司', '钱经理', '13800000002', '湖南省长沙市', 'ENABLED');

INSERT INTO customers (id, name, contact_name, contact_phone, address, status) VALUES
(1, '武汉零售客户A', '陈老板', '13900000001', '武汉市江汉区', 'ENABLED'),
(2, '长沙零售客户B', '周老板', '13900000002', '长沙市雨花区', 'ENABLED');

INSERT INTO warehouses (id, name, address, status) VALUES
(1, '中心仓', '武汉市中心仓', 'ENABLED'),
(2, '东区仓', '武汉市东区仓', 'ENABLED'),
(3, '南区仓', '武汉市南区仓', 'ENABLED');

INSERT INTO inventories (id, product_id, warehouse_id, warehouse_name, quantity, warning_threshold) VALUES
(1, 1, 1, '中心仓', 40, 20),
(2, 2, 1, '中心仓', 55, 30),
(3, 3, 1, '中心仓', 18, 15),
(4, 1, 2, '东区仓', 12, 20),
(5, 2, 3, '南区仓', 8, 30);

INSERT INTO purchase_orders (id, order_no, supplier_id, product_id, quantity, unit_price, total_amount, status, created_by, warehouse_id, warehouse_name, received_at, inbound_at) VALUES
(1, 'PO20260323001', 1, 1, 20, 420.00, 8400.00, 'INBOUND', 'buyer', 1, '中心仓', NOW(), NOW()),
(2, 'PO20260323002', 2, 3, 12, 175.00, 2100.00, 'RECEIVED', 'buyer', NULL, NULL, NOW(), NULL),
(3, 'PO20260323003', 1, 2, 15, 210.00, 3150.00, 'CREATED', 'buyer', NULL, NULL, NULL, NULL);

INSERT INTO inventory_records (id, product_id, biz_type, biz_id, warehouse_id, warehouse_name, from_warehouse_id, from_warehouse_name, to_warehouse_id, to_warehouse_name, change_qty, before_qty, after_qty, operator_name, remark) VALUES
(1, 1, 'PURCHASE_INBOUND', 1, 1, '中心仓', NULL, NULL, NULL, NULL, 20, 20, 40, '系统初始化', '初始化采购入库'),
(2, 2, 'INITIAL', NULL, 1, '中心仓', NULL, NULL, NULL, NULL, 55, 0, 55, '系统初始化', '初始化库存'),
(3, 3, 'INITIAL', NULL, 1, '中心仓', NULL, NULL, NULL, NULL, 18, 0, 18, '系统初始化', '初始化库存'),
(4, 1, 'TRANSFER_OUT', NULL, 1, '中心仓', 1, '中心仓', 2, '东区仓', -8, 48, 40, '系统初始化', '初始化调拨出库'),
(5, 1, 'TRANSFER_IN', NULL, 2, '东区仓', 1, '中心仓', 2, '东区仓', 8, 4, 12, '系统初始化', '初始化调拨入库');

INSERT INTO sales_orders (id, order_no, customer_id, product_id, quantity, unit_price, total_amount, paid_amount, status, created_by, warehouse_id, warehouse_name, outbound_at) VALUES
(1, 'SO20260323001', 1, 2, 10, 250.00, 2500.00, 2500.00, 'PAID', 'seller', 1, '中心仓', NOW()),
(2, 'SO20260323002', 2, 1, 6, 470.00, 2820.00, 0.00, 'CREATED', 'seller', NULL, NULL, NULL);

INSERT INTO payment_records (id, sales_order_id, amount, payer_name, remark) VALUES
(1, 1, 2500.00, '陈老板', '初始化全额回款');

INSERT INTO permissions (id, code, name, module, remark) VALUES
(1, 'dashboard:view', '驾驶舱查看', 'dashboard', '查看首页驾驶舱'),
(2, 'system:user:view', '用户查看', 'system', '查看账号信息'),
(3, 'system:user:edit', '用户编辑', 'system', '维护账号信息'),
(4, 'system:role:edit', '角色权限编辑', 'system', '维护角色权限'),
(5, 'system:config:edit', '系统配置编辑', 'system', '维护系统配置'),
(6, 'purchase:view', '采购查看', 'purchase', '查看采购单'),
(7, 'purchase:create', '采购创建', 'purchase', '创建采购单'),
(8, 'purchase:cancel', '采购取消', 'purchase', '取消采购单'),
(9, 'purchase:import', '采购导入', 'purchase', '导入采购数据'),
(10, 'purchase:inbound', '采购入库', 'purchase', '执行到货与入库'),
(11, 'sales:view', '销售查看', 'sales', '查看销售单'),
(12, 'sales:create', '销售创建', 'sales', '创建销售单'),
(13, 'sales:cancel', '销售取消', 'sales', '取消销售单'),
(14, 'sales:import', '销售导入', 'sales', '导入销售数据'),
(15, 'sales:outbound', '销售出库', 'sales', '执行销售出库'),
(16, 'sales:payment', '销售回款', 'sales', '登记销售回款'),
(17, 'inventory:view', '库存查看', 'inventory', '查看库存台账'),
(18, 'inventory:transfer', '库存调拨', 'inventory', '执行库存调拨'),
(19, 'inventory:check', '库存盘点', 'inventory', '执行库存盘点'),
(20, 'inventory:import', '库存导入', 'inventory', '导入库存数据'),
(21, 'report:view', '报表查看', 'report', '查看报表'),
(22, 'trace:view', '追溯查看', 'trace', '查看追溯链路'),
(23, 'logs:view', '日志查看', 'log', '查看系统日志'),
(24, 'message:view', '消息查看', 'message', '查看站内消息'),
(25, 'audit:view', '异常审核', 'audit', '审核异常单据'),
(26, 'bulletin:view', '销售公告查看', 'bulletin', '查看销售公告'),
(27, 'bulletin:create', '销售公告发布', 'bulletin', '创建销售公告');

INSERT INTO role_permissions (role_code, permission_code)
SELECT 'SUPER_ADMIN', code FROM permissions;

INSERT INTO role_permissions (role_code, permission_code)
SELECT 'ADMIN', code FROM permissions;

INSERT INTO role_permissions (role_code, permission_code) VALUES
('PURCHASER', 'dashboard:view'),
('PURCHASER', 'purchase:view'),
('PURCHASER', 'purchase:create'),
('PURCHASER', 'purchase:cancel'),
('PURCHASER', 'purchase:import'),
('PURCHASER', 'purchase:inbound'),
('PURCHASER', 'inventory:view'),
('PURCHASER', 'report:view'),
('PURCHASER', 'trace:view');

INSERT INTO role_permissions (role_code, permission_code) VALUES
('SELLER', 'dashboard:view'),
('SELLER', 'sales:view'),
('SELLER', 'sales:create'),
('SELLER', 'sales:cancel'),
('SELLER', 'sales:import'),
('SELLER', 'sales:outbound'),
('SELLER', 'sales:payment'),
('SELLER', 'bulletin:view'),
('SELLER', 'bulletin:create'),
('SELLER', 'report:view'),
('SELLER', 'trace:view');

INSERT INTO role_permissions (role_code, permission_code) VALUES
('KEEPER', 'dashboard:view'),
('KEEPER', 'inventory:view'),
('KEEPER', 'inventory:transfer'),
('KEEPER', 'inventory:check'),
('KEEPER', 'inventory:import'),
('KEEPER', 'report:view'),
('KEEPER', 'trace:view');
