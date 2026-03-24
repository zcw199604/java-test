USE tobacco_platform;

INSERT INTO roles (id, code, name, remark) VALUES
(1, 'ADMIN', '平台管理员', '系统超级管理员'),
(2, 'PURCHASER', '采购专员', '负责采购流程'),
(3, 'SELLER', '销售专员', '负责销售流程'),
(4, 'KEEPER', '库管人员', '负责库存管理')
ON DUPLICATE KEY UPDATE name = VALUES(name), remark = VALUES(remark);

INSERT INTO users (id, username, password, real_name, role_code, status) VALUES
(1, 'admin', '123456', '系统管理员', 'ADMIN', 'ENABLED'),
(2, 'buyer', '123456', '采购示例用户', 'PURCHASER', 'ENABLED'),
(3, 'seller', '123456', '销售示例用户', 'SELLER', 'ENABLED'),
(4, 'keeper', '123456', '库管示例用户', 'KEEPER', 'ENABLED')
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), role_code = VALUES(role_code), status = VALUES(status);

INSERT INTO products (id, code, name, category, unit, unit_price, warning_threshold, status) VALUES
(1, 'YC001', '利群（软长嘴）', '卷烟', '条', 550.00, 20, 'ENABLED'),
(2, 'YC002', '中华（硬）', '卷烟', '条', 850.00, 10, 'ENABLED'),
(3, 'YC003', '黄鹤楼（软蓝）', '卷烟', '条', 420.00, 15, 'ENABLED')
ON DUPLICATE KEY UPDATE name = VALUES(name), unit_price = VALUES(unit_price), warning_threshold = VALUES(warning_threshold), status = VALUES(status);

INSERT INTO suppliers (id, name, contact_name, contact_phone, address, status) VALUES
(1, '浙江烟草供应中心', '王经理', '13800000001', '浙江省杭州市', 'ENABLED'),
(2, '华东烟草配送公司', '李经理', '13800000002', '江苏省南京市', 'ENABLED')
ON DUPLICATE KEY UPDATE contact_name = VALUES(contact_name), contact_phone = VALUES(contact_phone), address = VALUES(address), status = VALUES(status);
