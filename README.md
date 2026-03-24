# 烟草采销存协同管理平台

本仓库当前已升级为面向任务书验收的**前后端分离采销存平台**：
- **后端：** Java 8 + Spring Boot 2.7 + Spring JDBC + MySQL + Apache Shiro + Apache POI
- **前端：** Vue 3 + Vite + Pinia + Vue Router

> 注意：前端仍采用 **Vue 单页应用**，不是 JSP；本次重点补齐后端能力并对齐任务书。

## 目录结构

```text
.
├── backend/                  # Spring Boot 后端
├── frontend/                 # Vue 3 前端
├── helloagents/              # 知识库、方案包、历史记录
└── README.md
```

## 当前后端能力

### 1. 认证与权限
- Apache Shiro 密码哈希与认证编排
- 登录、登出、当前用户信息
- 验证码生成
- 忘记密码 / 重置密码
- 用户、角色、权限、数据范围基础 RBAC 能力

### 2. 超级管理员与系统管理
- 用户新增、修改、启停、详情、数据范围维护
- 角色与权限配置
- 系统配置管理
- 仓库管理
- 登录日志、操作日志
- 个人中心与修改密码
- 站内消息读取

### 3. 基础资料
- 品类、商品完整维护
- 供应商完整维护
- 客户完整维护
- 商品价格、预警阈值、基础合规字段

### 4. 采购管理
- 采购需求提报
- 采购单创建、修改、审核、取消、到货、入库
- 采购跟踪链路
- 采购分析
- Excel 导入导出

### 5. 销售管理
- 销售信息发布
- 销售单创建、修改、审核、取消、出库
- 回款登记与应收状态查询
- 销售统计
- Excel 导入导出

### 6. 库存管理
- 库存台账、出入库/调拨流水
- 真实调拨、盘点盈亏上报
- 预警查询与预警历史
- Excel 导入导出

### 7. 统计、追溯与异常
- 采销存汇总
- 合规追溯
- 异常单据监控
- 采购/销售/库存联动数据
- 报表 Excel 导出

## 启动方式

### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认地址：`http://localhost:8080`

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认地址：`http://localhost:5173`

## 默认演示账号

- 超级管理员：`admin / 123456`
- 采购：`buyer / 123456`
- 销售：`seller / 123456`
- 库管：`keeper / 123456`

## 开发数据库

当前默认使用 MySQL：
- Host: `127.0.0.1`
- Port: `3307`
- Database: `tobacco_platform`
- Username: `root`
- Password: `root123`

后端配置文件：`backend/src/main/resources/application.yml`
