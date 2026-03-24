# 项目技术约定

---

## 技术栈
- **核心:** Java 8 / Spring Boot 2.7 / Maven 3.8
- **前端:** Vue 3 / Vite / Vue Router / Pinia / Axios / ECharts
- **数据:** MySQL 8（当前默认连接 `127.0.0.1:3307/tobacco_platform`）

---

## 开发约定
- **代码规范:** 后端遵循 controller/service/model/common/config 分层；前端遵循 api、views、components、stores 分层
- **命名约定:** Java 使用驼峰类名与小写包名；Vue 组件使用 PascalCase；接口路径统一使用 `/api/*`
- **接口约定:** 前端页面字段以当前后端真实返回字段为准，避免前后端自行约定不一致

---

## 错误与日志
- **策略:** 后端统一返回 `ApiResponse` 结构；CSV 导出接口直接返回文件流/文本内容；前端统一在 Axios 拦截器处理请求错误
- **日志:** 当前使用 Spring Boot 默认日志；后续按模块补充分级日志

---

## 测试与流程
- **测试:** 后端至少保证 `mvn test` 通过；前端至少保证 `npm run build` 通过
- **联调:** 采购流程需验证“创建 → 到货 → 入库”，报表需验证 CSV 预览与下载
- **提交:** 建议采用 `type(scope): subject` 的 Commit Message 规范
