# 烟草采销存协同管理平台

本仓库提供一个可运行的烟草采销存协同平台，当前交付口径为“前端构建产物由 Spring Boot 统一托管”。浏览器默认访问 `http://localhost:8080`，接口与页面行为以当前代码实现为准。

## 当前技术栈

- 后端：Java 8、Spring Boot 2.7.18、Apache Shiro、MyBatis 注解 / SqlProvider、MySQL 8、Apache POI
- 前端：Vue 3、Vite、Vue Router、Pinia、Element Plus、ECharts、Axios、TypeScript
- 认证：`Authorization: Bearer <token>`，登录令牌持久化到 `user_sessions`
- 交付：`frontend` 构建产物输出到 `backend/src/main/resources/static/`

## 功能覆盖

### 1. 认证与工作台
- 登录验证码、忘记密码、重置密码、退出登录
- 驾驶舱、个人中心、消息中心、暗黑模式

### 2. 系统管理与基础资料
- 账号、角色权限、系统配置
- 商品、品类、供应商、客户、仓库维护
- 登录日志、操作日志

### 3. 采购管理
- 采购单列表、新建、编辑、审核、取消、到货、入库
- 采购追溯与 Excel 导入

### 4. 销售管理
- 销售信息发布
- 销售单列表、新建、编辑、审核、取消、出库、回款
- 应收、销售统计与 Excel 导入

### 5. 库存管理
- 多仓库存总览、库存流水、库存预警
- 调拨、盘点、库存追溯台账、Excel 导入

### 6. 报表与追溯
- 采购 / 销售 / 库存汇总
- 趋势图、PSI 汇总、联动分析
- 合规追溯、异常单据审核
- `/api/reports/export` Excel 导出

## 目录结构

```text
.
├── backend/                  # Spring Boot 后端与静态资源承载
├── frontend/                 # Vue 3 前端源码
├── helloagents/              # 知识库、方案包、历史记录
├── 开题报告 .doc
└── 烟草采销存协同管理平台的设计与实现-任务书.docx
```

## 启动前准备

- 可用的 MySQL 数据库实例
- JDK 8
- Node.js 与 npm
- Maven

默认数据库配置位于 `backend/src/main/resources/application.yml`：

- Host：`127.0.0.1`
- Port：`3307`
- Database：`tobacco_platform`
- Username：`root`
- Password：`root123`

也可通过环境变量覆盖：`DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD`。

> 注意：后端配置了 `spring.sql.init.mode=always`。每次启动都会执行 `schema.sql` 与 `data.sql`，并重置演示数据。

## 默认启动方式

推荐以“后端统一托管页面 + API”的方式运行：

```powershell
cd frontend
npm install
npm run build

cd ..\backend
mvn spring-boot:run
```

启动后访问：`http://localhost:8080`

## 前端专项调试

仅在前端局部开发时单独启动 Vite：

```powershell
cd backend
mvn spring-boot:run

cd ..\frontend
npm install
npm run dev
```

调试地址：`http://localhost:5173`  
前端已配置 `/api` 代理到 `http://localhost:8080`。

## 默认演示账号

- 超级管理员：`admin / 123456`
- 普通管理员：`manager / 123456`
- 采购专员：`buyer / 123456`
- 销售专员：`seller / 123456`
- 库管人员：`keeper / 123456`

## 验证方式

- 后端：`cd backend && mvn test`
- 前端：`cd frontend && npm run build`

## 相关文档

- 知识库根目录：`helloagents/`
- 项目约定：`helloagents/project.md`
- API 手册：`helloagents/wiki/api.md`
- 数据模型：`helloagents/wiki/data.md`
