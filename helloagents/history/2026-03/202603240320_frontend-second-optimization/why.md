# 变更提案: 前端第二轮优化（性能、权限交互、TypeScript）

## 需求背景
用户要求在已完成后台页面重构的基础上继续进行第二轮优化，并明确要求三类能力全部推进：性能优化、权限与交互优化、TypeScript 改造。

## 变更目标
1. 降低首屏和主包体压力，增强构建可维护性。
2. 补齐按钮级权限与无权限访问反馈，改善加载/错误交互。
3. 将前端核心基础设施升级为 TypeScript，新增类型校验流程。
4. 保持现有业务页面可运行，并通过构建验证。

## 影响范围
- **模块:** frontend, auth, router, admin, procurement, sales, inventory, report
- **文件:** `frontend/package*.json`、`frontend/tsconfig.json`、`frontend/src/{main,router,stores,utils,directives,api,components,views}`、知识库与历史索引

## 成功标准
- 构建脚本增加 `type-check`，且 `npm run build` 通过。
- 构建结果完成 vendor 拆包，主入口体积明显下降。
- 按钮级权限可在前端隐藏无权限操作入口。
- 出现无权限访问时有专门提示页面。
- 核心前端基础设施已迁移为 TypeScript。
