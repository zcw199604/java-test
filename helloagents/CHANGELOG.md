# Changelog

本文件记录项目所有重要变更。
格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/),
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### 变更
- 修正文档与代码现状不一致的问题，更新 README、项目技术约定与知识库
- 将 `/api/reports/export` 调整为真实 CSV 导出接口，前端同步支持 CSV 预览与下载
- 修复报表中心和商品列表的前端字段/接口使用，统一以后端真实返回为准
- 将采购流程拆分为“到货”和“入库”两个动作，新增 `RECEIVED` 状态与到货时间
- 将首页概览统计调整为数据库实时统计值，移除硬编码数据

## [0.1.0] - 2026-03-23

### 新增
- 初始化烟草采销存协同管理平台前后端分离工程
- 新增 Spring Boot 后端骨架与健康检查/概览接口
- 新增 Vue 3 + Vite 前端骨架与核心业务页面导航
- 新增 HelloAGENTS 知识库、方案包与历史索引
- 新增采购、库存、销售、报表模块的前后端实现与临时 MySQL 数据闭环
- 新增报表 CSV 导出与 ECharts 趋势图页面
