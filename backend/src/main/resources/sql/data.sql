DELETE FROM payment_records;
DELETE FROM sales_orders;
DELETE FROM inventory_records;
DELETE FROM purchase_orders;
DELETE FROM inventories;
DELETE FROM customers;
DELETE FROM suppliers;
DELETE FROM categories;
DELETE FROM products;
DELETE FROM users;
DELETE FROM roles;

INSERT INTO roles (id, code, name, remark) VALUES
(1, 'ADMIN', '平台管理员', '负责账号权限、基础资料和报表管理'),
(2, 'PURCHASER', '采购专员', '负责采购订单与到货入库'),
(3, 'SELLER', '销售专员', '负责销售订单、出库与回款'),
(4, 'KEEPER', '库管人员', '负责库存台账、盘点与预警');

INSERT INTO users (id, username, password, real_name, role_code, status) VALUES
(1, 'admin', '123456', '系统管理员', 'ADMIN', 'ENABLED'),
(2, 'buyer', '123456', '采购张三', 'PURCHASER', 'ENABLED'),
(3, 'seller', '123456', '销售李四', 'SELLER', 'ENABLED'),
(4, 'keeper', '123456', '库管王五', 'KEEPER', 'ENABLED');

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

INSERT INTO inventories (id, product_id, warehouse_name, quantity, warning_threshold) VALUES
(1, 1, '中心仓', 40, 20),
(2, 2, '中心仓', 55, 30),
(3, 3, '中心仓', 18, 15);

INSERT INTO purchase_orders (id, order_no, supplier_id, product_id, quantity, unit_price, total_amount, status, created_by, received_at, inbound_at) VALUES
(1, 'PO20260323001', 1, 1, 20, 420.00, 8400.00, 'INBOUND', 'buyer', NOW(), NOW()),
(2, 'PO20260323002', 2, 3, 12, 175.00, 2100.00, 'RECEIVED', 'buyer', NOW(), NULL),
(3, 'PO20260323003', 1, 2, 15, 210.00, 3150.00, 'CREATED', 'buyer', NULL, NULL);

INSERT INTO inventory_records (id, product_id, biz_type, biz_id, change_qty, before_qty, after_qty, operator_name, remark) VALUES
(1, 1, 'PURCHASE_INBOUND', 1, 20, 20, 40, '系统初始化', '初始化采购入库'),
(2, 2, 'INITIAL', NULL, 55, 0, 55, '系统初始化', '初始化库存'),
(3, 3, 'INITIAL', NULL, 18, 0, 18, '系统初始化', '初始化库存');

INSERT INTO sales_orders (id, order_no, customer_id, product_id, quantity, unit_price, total_amount, paid_amount, status, created_by, outbound_at) VALUES
(1, 'SO20260323001', 1, 2, 10, 250.00, 2500.00, 2500.00, 'PAID', 'seller', NOW()),
(2, 'SO20260323002', 2, 1, 6, 470.00, 2820.00, 0.00, 'CREATED', 'seller', NULL);

INSERT INTO payment_records (id, sales_order_id, amount, payer_name, remark) VALUES
(1, 1, 2500.00, '陈老板', '初始化全额回款');
