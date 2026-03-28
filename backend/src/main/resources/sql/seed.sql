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

-- ============================================================
-- 权限定义初始数据
-- ============================================================
INSERT INTO permissions (id, code, name, module, remark) VALUES
(1,  'dashboard:view',     '驾驶舱查看',     'dashboard', '查看首页概览'),
(2,  'purchase:view',      '采购订单查看',   'purchase',  '查看采购订单列表'),
(3,  'purchase:create',    '采购订单创建',   'purchase',  '创建采购订单'),
(4,  'purchase:audit',     '采购订单审核',   'purchase',  '审核采购订单'),
(5,  'purchase:cancel',    '采购订单取消',   'purchase',  '取消采购订单'),
(6,  'purchase:import',    '采购订单导入',   'purchase',  'Excel批量导入'),
(7,  'purchase:inbound',   '采购入库',       'purchase',  '采购到货入库操作'),
(8,  'sales:view',         '销售订单查看',   'sales',     '查看销售订单列表'),
(9,  'sales:create',       '销售订单创建',   'sales',     '创建销售订单'),
(10, 'sales:audit',        '销售订单审核',   'sales',     '审核销售订单'),
(11, 'sales:cancel',       '销售订单取消',   'sales',     '取消销售订单'),
(12, 'sales:import',       '销售订单导入',   'sales',     'Excel批量导入'),
(13, 'sales:outbound',     '销售出库',       'sales',     '销售出库操作'),
(14, 'sales:payment',      '销售回款',       'sales',     '销售回款登记'),
(15, 'inventory:view',     '库存查看',       'inventory', '查看库存列表'),
(16, 'inventory:transfer', '库存调拨',       'inventory', '库存调拨操作'),
(17, 'inventory:check',    '库存盘点',       'inventory', '库存盘点操作'),
(18, 'inventory:import',   '库存导入',       'inventory', 'Excel批量导入'),
(19, 'report:view',        '报表查看',       'report',    '查看报表'),
(20, 'report:export',      '报表导出',       'report',    '导出报表Excel'),
(21, 'admin:user',         '用户管理',       'admin',     '用户增删改查'),
(22, 'admin:role',         '角色管理',       'admin',     '角色权限管理'),
(23, 'admin:config',       '系统配置',       'admin',     '系统参数配置'),
(24, 'admin:log',          '日志管理',       'admin',     '查看系统日志'),
(25, 'admin:base-data',    '基础资料管理',   'admin',     '品类供应商客户仓库管理'),
(26, 'trace:view',         '合规追溯查看',   'trace',     '查看追溯记录'),
(27, 'audit:view',         '异常审核查看',   'audit',     '查看异常单据'),
(28, 'audit:process',      '异常审核处理',   'audit',     '审核异常单据'),
(29, 'bulletin:view',      '销售公告查看',   'bulletin',  '查看销售公告'),
(30, 'bulletin:create',    '销售公告发布',   'bulletin',  '创建销售公告')
ON DUPLICATE KEY UPDATE name = VALUES(name), module = VALUES(module), remark = VALUES(remark);

-- ============================================================
-- 角色权限映射初始数据
-- ============================================================

-- ADMIN: 全部权限
INSERT INTO role_permissions (role_code, permission_code)
SELECT 'ADMIN', code FROM permissions
ON DUPLICATE KEY UPDATE role_code = VALUES(role_code);

-- PURCHASER: 驾驶舱 + 采购全部 + 库存查看 + 报表查看 + 追溯查看
INSERT INTO role_permissions (role_code, permission_code) VALUES
('PURCHASER', 'dashboard:view'),
('PURCHASER', 'purchase:view'),
('PURCHASER', 'purchase:create'),
('PURCHASER', 'purchase:cancel'),
('PURCHASER', 'purchase:import'),
('PURCHASER', 'purchase:inbound'),
('PURCHASER', 'inventory:view'),
('PURCHASER', 'report:view'),
('PURCHASER', 'trace:view')
ON DUPLICATE KEY UPDATE role_code = VALUES(role_code);

-- SELLER: 驾驶舱 + 销售全部 + 公告 + 报表查看 + 追溯查看
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
('SELLER', 'trace:view')
ON DUPLICATE KEY UPDATE role_code = VALUES(role_code);

-- KEEPER: 驾驶舱 + 库存全部 + 报表查看 + 追溯查看
INSERT INTO role_permissions (role_code, permission_code) VALUES
('KEEPER', 'dashboard:view'),
('KEEPER', 'inventory:view'),
('KEEPER', 'inventory:transfer'),
('KEEPER', 'inventory:check'),
('KEEPER', 'inventory:import'),
('KEEPER', 'report:view'),
('KEEPER', 'trace:view')
ON DUPLICATE KEY UPDATE role_code = VALUES(role_code);
