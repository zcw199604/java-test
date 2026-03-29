# 项目技术约定

---

## 技术栈
- **核心:** Java 8 / Spring Boot 2.7.18 / Maven
- **认证与权限:** Apache Shiro + Bearer 会话令牌，登录状态落库到 `user_sessions`
- **数据访问:** MyBatis 注解 / SqlProvider；Mapper 统一位于 `backend/src/main/java/com/example/tobacco/mapper/`
- **导入导出:** Apache POI（后端 Excel 导入导出）+ `xlsx` / `file-saver`（前端本地导出）
- **前端:** Vue 3 / Vite / Vue Router / Pinia / Axios / Element Plus / ECharts / TypeScript / vue-tsc
- **数据:** MySQL 8（默认连接 `127.0.0.1:3307/tobacco_platform`）

---

## 开发约定
- **分层约定:** 后端遵循 `controller/service/model/common/config` 分层；前端遵循 `api/views/components/stores/router/utils` 分层
- **命名约定:** Java 使用驼峰类名和小写包名；Vue 组件使用 PascalCase；接口统一使用 `/api/*`
- **接口约定:** 除文件下载接口外，后端统一返回 `ApiResponse { code, message, data }`；前端以真实后端字段和 `code == 0` 为准
- **认证约定:** 浏览器通过 `Authorization: Bearer <token>` 访问受保护接口；`JwtTokenUtil` 仅保留兼容壳，不再承担真实鉴权
- **前端约定:** 页面入口和菜单权限以 `src/router/route-map.ts`、`src/utils/access.ts` 为准；按钮级权限通过指令和权限映射控制
- **导出口径:** 平台对外导出统一使用 `.xlsx`；报表导出由后端返回二进制文件流，本地列表导出由前端生成 Excel

---

## 运行约定
- **默认运行形态:** 先构建 `frontend`，再启动 `backend`，由 Spring Boot 同时提供页面和 API
- **构建输出:** `vite build` 的输出目录固定为 `backend/src/main/resources/static/`
- **数据库初始化:** `spring.sql.init.mode=always`，后端启动会执行 `schema.sql` 与 `data.sql`，当前演示数据会被重置
- **联调口径:** 常规演示、验收、冒烟以 `http://localhost:8080` 为准；`http://localhost:5173` 仅用于前端专项调试
- **PowerShell 启动示例:** `cd frontend; npm install; npm run build; cd ..\backend; mvn spring-boot:run`

---

## 错误与日志
- **错误处理:** 后端业务异常通过 `ApiResponse.message` 返回；前端 Axios 拦截器统一处理业务失败、`401` 和 `403`
- **日志沉淀:** 登录日志、操作日志、追溯记录、异常单据、站内消息均以数据库表为准
- **安全约定:** 审核、到货、入库、出库、回款、调拨、盘点等关键动作必须保留审计留痕

---

## 测试与流程
- **后端测试:** 以 `mvn test` 为主，重点覆盖认证、RBAC、采销存链路、消息、日志和报表
- **前端验证:** 至少通过 `npm run build`，其内部会先执行 `npm run type-check`
- **联调场景:** 采购验证“建单 → 审核 → 到货 → 入库”；销售验证“建单 → 审核 → 出库 → 回款”；库存验证“按仓调拨 → 盘点 → 预警”
- **提交规范:** 建议采用 `type(scope): subject` 的 Commit Message 规范
