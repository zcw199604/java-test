# API 手册

## 概述
当前系统已形成前后端可联调的完整最小闭环，覆盖认证、系统管理、基础资料、采购、库存、销售、报表等模块。前端基于动态路由与角色权限组织页面，但接口能力仍以后端真实实现为准。

## 认证方式
- 登录接口：`POST /api/auth/login`
- 鉴权方式：`Authorization: Bearer <token>`
- 当前登录态：`GET /api/auth/profile`

---

## 核心接口

### 认证与系统
- `POST /api/auth/login`：登录
- `GET /api/auth/profile`：当前用户信息（前端用于初始化角色/权限/菜单）
- `POST /api/auth/logout`：退出登录
- `GET /api/users`：用户列表
- `GET /api/roles`：角色列表

### 基础资料
- `GET /api/products`：商品列表
- `GET /api/suppliers`：供应商列表
- `GET /api/customers`：客户列表

### 采购管理
- `GET /api/purchases`：采购单列表
- `POST /api/purchases`：创建采购单
- `POST /api/purchases/{id}/receive`：登记到货，状态更新为 `RECEIVED`
- `POST /api/purchases/{id}/inbound`：采购入库，状态更新为 `INBOUND` 并写入库存流水

### 库存管理
- `GET /api/inventories`：库存台账
- `GET /api/inventory-records`：库存流水
- `GET /api/inventory-warnings`：库存预警
- `POST /api/inventory-checks`：库存盘点
- `POST /api/inventory-transfers`：库存调拨登记

### 销售管理
- `GET /api/sales`：销售单列表
- `POST /api/sales`：创建销售单
- `POST /api/sales/{id}/outbound`：销售出库
- `POST /api/sales/{id}/payment`：回款登记
- `GET /api/sales/statistics`：销售统计

### 驾驶舱与报表
- `GET /api/dashboard/summary`：首页概览（实时统计值）
- `GET /api/reports/purchase-summary`：采购汇总
- `GET /api/reports/sales-summary`：销售汇总
- `GET /api/reports/inventory-summary`：库存汇总
- `GET /api/reports/trend`：趋势数据
- `GET /api/reports/export`：导出真实 CSV 内容

## 页面与接口映射
- 驾驶舱：`/dashboard` → `/api/dashboard/summary`、`/api/inventory-warnings`
- 采购管理：`/purchase/*` → `/api/purchases`、`/api/suppliers`、`/api/products`
- 销售管理：`/sale/*` → `/api/sales`、`/api/customers`、`/api/products`
- 库存管理：`/inventory/*` → `/api/inventories`、`/api/inventory-records`、`/api/inventory-warnings`
- 经营大屏：`/report/dashboard` → `/api/reports/*`
- 管理页中的账号/角色/基础资料：优先复用 `/api/users`、`/api/roles`、`/api/products`、`/api/suppliers`、`/api/customers`

## 错误码
| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 400 | 参数错误或业务校验失败 |
| 401 | 未登录或令牌失效 |
| 403 | 无权限访问 |
| 500 | 服务器内部异常 |
