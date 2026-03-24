# 项目技术约定

---

## 技术栈
- **核心:** Java 8 / Spring Boot 2.7 / Maven 3.8
- **安全:** Apache Shiro（密码哈希、认证授权基础）
- **数据访问:** Spring JDBC
- **导入导出:** Apache POI（Excel）
- **前端:** Vue 3 / Vite / Vue Router / Pinia / Axios / ECharts
- **数据:** MySQL 8（当前默认连接 `127.0.0.1:3307/tobacco_platform`）

---

## 开发约定
- **代码规范:** 后端遵循 controller/service/model/common/config 分层；前端遵循 api、views、components、stores 分层
- **命名约定:** Java 使用驼峰类名与小写包名；Vue 组件使用 PascalCase；接口路径统一使用 `/api/*`
- **接口约定:** 前端页面字段以当前后端真实返回字段为准；Excel 导入导出统一使用 `.xlsx`
- **权限约定:** 用户 → 角色 → 权限点 → 数据范围，多角色菜单能力由后端统一返回

---

## 错误与日志
- **策略:** 后端统一返回 `ApiResponse` 结构；Excel 导出接口直接返回文件流；前端统一在 Axios 拦截器处理请求错误
- **日志:** 登录日志、操作日志、追溯记录、异常单据记录统一进入数据库表

---

## 测试与流程
- **测试:** 后端优先保证集成测试覆盖认证、RBAC、采销存、日志、报表与 Excel 流程
- **联调:** 采购流程验证“提报 → 建单 → 审核 → 到货 → 入库”；销售流程验证“发布 → 建单 → 审核 → 出库 → 回款”；库存流程验证“调拨 → 盘点 → 预警”
- **提交:** 建议采用 `type(scope): subject` 的 Commit Message 规范
