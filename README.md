# 烟草采销存协同管理平台

本仓库当前已实现一个可运行、可联调的**前后端分离采销存平台**：
- **后端：** Java 8 + Spring Boot 2.7 + Spring JDBC + MySQL
- **前端：** Vue 3 + Vite + Pinia + Vue Router

> 注意：前端采用 **Vue 单页应用** 实现，而不是 JSP。

## 目录结构

```text
.
├── backend/                  # Spring Boot 后端
├── frontend/                 # Vue 3 前端
├── helloagents/              # 知识库、方案包、历史记录
├── 开题报告 .doc
└── 烟草采销存协同管理平台的设计与实现-任务书.docx
```

## 当前系统能力

### 1. 认证与权限
- JWT 登录、登录态恢复、退出登录
- 按角色返回菜单能力（管理员、采购、销售、库管）

### 2. 系统与基础资料
- 用户列表、角色列表
- 商品列表、品类数据
- 供应商列表、客户列表

### 3. 采购管理
- 采购单列表与创建
- 支持独立的**到货登记**与**采购入库**两个动作
- 入库后自动更新库存台账与库存流水

### 4. 库存管理
- 库存台账、库存流水、库存预警
- 盘点登记、调拨登记

### 5. 销售管理
- 销售单列表与创建
- 销售出库、回款登记、销售统计

### 6. 报表与首页
- 首页概览统计（后端实时数据）
- 采购汇总、销售汇总、库存汇总、趋势图
- `/api/reports/export` 导出真实 CSV，前端支持预览与下载

## 启动方式

### 默认方式：仅启动后端（推荐）

前端页面默认通过后端静态资源方式提供，不需要额外启动前端项目。发布或联调前先构建前端：

```bash
cd frontend
npm install
npm run build
```

构建产物会写入 `backend/src/main/resources/static/`，随后启动后端：

```bash
cd backend
mvn spring-boot:run
```

默认地址：`http://localhost:8080`

### 标准启动命令

```bash
# 1) 构建前端静态资源
cd frontend
npm install
npm run build

# 2) 启动后端（统一提供 API + 页面）
cd ../backend
mvn spring-boot:run
```

如需使用本机指定 JDK 启动：

```bash
export JAVA_HOME=/mnt/dbz/jdk-21.0.4
export PATH="$JAVA_HOME/bin:$PATH"
cd backend
mvn spring-boot:run
```

### 可选方式：仅前端专项调试时启动 dev server

```bash
cd frontend
npm install
npm run dev
```

默认地址：`http://localhost:5173`

前端已配置 `/api` 代理到后端 `http://localhost:8080`。

> 说明：常规验收、接口冒烟和集成联调以“后端统一提供页面 + API”为准，不建议长期依赖独立前端 dev server。

### 3. 后端统一托管前端（可选）

如果需要只通过后端访问整套系统：

```bash
cd frontend
npm run build

cd ../backend
mvn spring-boot:run
```

随后直接访问：`http://localhost:8080`

说明：
- 前端构建产物会输出到 `backend/src/main/resources/static`
- Spring Boot 会直接提供静态资源
- 已补齐常用 Vue Router history 路由的回退转发，可直接刷新业务页面

## 默认演示账号

- 管理员：`admin / 123456`
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

## 当前功能覆盖

- 认证登录：JWT 登录、登录态恢复、退出登录
- 系统管理：用户列表、角色列表
- 基础资料：商品列表、供应商列表、客户列表
- 采购管理：采购单列表、创建、到货、入库
- 库存管理：库存台账、库存流水、预警、盘点、调拨登记
- 销售管理：销售单列表、创建、出库、回款、统计
- 报表中心：采购汇总、销售汇总、库存汇总、趋势图、CSV 导出

## 验证方式

- 后端：`cd backend && mvn test`
- 前端：`cd frontend && npm run build`

## 可继续扩展的方向

1. 增加用户、角色、商品、供应商、客户的完整 CRUD 能力
2. 增强权限模型与细粒度按钮级控制
3. 完善采购、销售、库存的查询筛选与业务校验
4. 补充更多自动化测试、部署说明与论文支撑材料
