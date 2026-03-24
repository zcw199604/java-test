# 技术方案: 前端第二轮优化（性能、权限交互、TypeScript）

## 方案概述
本轮优化围绕“性能、可维护性、权限交互”三条主线推进：
- 通过 Vite 手动拆包、图表动态加载和页面异步路由减少主包压力；
- 通过全局 `v-permission` 指令和 `/403` 页面完善页面级与按钮级权限反馈；
- 通过 TypeScript 改造前端核心入口、路由、状态、工具和关键页面，并引入 `vue-tsc` 形成静态校验流程。

## 关键设计

### 1. 性能优化
- `vite.config.js` 中为 Vue、Element Plus、ECharts、xlsx、Axios 等模块拆分独立 chunk。
- 图表组件改为动态导入 ECharts，避免首屏立即加载全部图表逻辑。
- 驾驶舱、个人中心与无权限页改为异步路由组件。

### 2. 权限与交互优化
- 新增 `directives/permission.ts`，统一隐藏无权限按钮。
- 路由守卫在无权限场景跳转 `/403`，给出明确说明与回退路径。
- 登录页新增错误提示和验证码刷新；驾驶舱新增加载/空态/异常提示。
- 通用表格组件支持 `loading` 与自定义空态文本。

### 3. TypeScript 改造
- 新增 `tsconfig.json`、`env.d.ts`、`types/` 与 Router Meta 扩展声明。
- 核心入口、路由、状态、权限工具、导出工具、HTTP/Auth API 改为 TypeScript。
- 主布局、登录页、驾驶舱、无权限页、图表/表格组件切换为 `lang=ts`。
- `package.json` 引入 `type-check`，构建前先执行 `vue-tsc --noEmit`。

## 风险与规避
- **第三方库类型噪音:** 通过 `skipLibCheck` 控制依赖类型噪音，优先保证项目自身类型收敛。
- **构建拆包后请求增多:** 保留按业务价值拆包而非过度碎片化，平衡缓存收益与请求数。
- **TypeScript 改造范围过大:** 优先迁移核心基础设施和高频页面，保留其余业务页逐步演进空间。

## ADR
| adr_id | title | date | status | affected_modules | details |
|--------|-------|------|--------|------------------|---------|
| ADR-007 | 前端构建流程增加 TypeScript 静态校验并进行手动拆包 | 2026-03-24 | ✅已采纳 | frontend, build | 在构建前执行 `vue-tsc`，并对核心依赖进行 vendor chunk 拆分 |
| ADR-008 | 前端权限控制新增按钮级指令与无权限提示页 | 2026-03-24 | ✅已采纳 | frontend, auth, admin, procurement, sales, inventory, report | 使用 `v-permission` + `/403` 提升权限交互清晰度 |
