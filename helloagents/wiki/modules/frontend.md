# frontend

## 目的
提供 Vue 单页应用界面，承载登录、驾驶舱、个人中心、系统管理、采购、库存、销售和统计追溯页面。

## 模块概述
- **职责:** 登录鉴权、动态路由、页面级/按钮级权限控制、后台布局、图表展示、导出能力、暗黑模式、消息提醒与 TypeScript 基础设施
- **状态:** ✅稳定
- **最后更新:** 2026-03-28

## 标准启动命令
- 构建：`cd frontend && npm install && npm run build`
- 运行：`cd backend && mvn spring-boot:run`
- 指定 JDK 运行：`export JAVA_HOME=/mnt/dbz/jdk-21.0.4 && export PATH="$JAVA_HOME/bin:$PATH" && cd backend && mvn spring-boot:run`

## 规范
- 默认交付方式为后端静态托管：前端构建产物写入 `backend/src/main/resources/static/`，由 Spring Boot 统一对外提供页面与 API。
- 常规启动、冒烟测试、验收演示时，不额外启动 `frontend` 项目的 Vite dev server；仅在前端局部开发调试场景下按需使用 `npm run dev`。
- 登录验证码、消息中心、配置页、日志页、追溯页、异常页需优先使用后端真实接口，不保留长期静态假数据。
- API 统一走 `src/api/*`
- 路由统一由 `src/router/route-map.ts` 管理
- 登录态、权限、消息、主题状态统一由 Pinia 管理
- 页面目录按业务域组织，通用区块优先复用 `PageSection`、`AppTable`、`AppChart`、`MenuTree`
- 核心入口、路由、状态、权限工具、HTTP 访问优先使用 TypeScript
- 构建前需通过 `vue-tsc --noEmit`
- 页面字段以当前后端真实返回字段为准
