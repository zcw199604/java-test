# 方案设计

## 总体方案
采用“后端聚合真实销售订单 + 前端驾驶舱图表切换”的实现方式。

### 1. 后端接口扩展
新增 `GET /api/dashboard/sales-history`：
- 参数：
  - `metric=quantity|amount`，默认 `quantity`
  - `days=7|30`，默认 `7`
  - `limit`，默认 `6`
- 返回：
  - `metric`
  - `periods`：日期数组
  - `series`：烟品名称与每个时间点的值数组

### 2. 数据口径
统计 `sales_orders` 中已进入销售闭环的单据，包含：
- `OUTBOUND`
- `PARTIAL_PAID`
- `PAID`

按“日期 + 商品”聚合：
- 数量：`sum(quantity)`
- 金额：`sum(total_amount)`

为防止图表线条过多，先按指定周期内累计值排序，仅返回 Top N 烟品。

### 3. MyBatis 实现
在 `DashboardMapper` 中增加：
- 查询 Top 烟品列表
- 查询指定商品在指定周期内的逐日聚合值

服务层负责：
- 生成日期序列
- 聚合并补齐缺失日期为 0
- 组装图表所需结构

### 4. 前端实现
在 `DashboardView.vue` 中：
- 保留原 KPI 与预警区域
- 将原硬编码趋势图替换为真实业务图表，并在右侧操作区增加：
  - 指标切换：数量 / 销售金额
  - 周期切换：近 7 天 / 近 30 天
- 新增接口调用，页面首次加载与切换时刷新数据
- 无数据时展示空状态提示

### 5. 验证策略
- 后端补充集成测试，校验接口返回结构与指标切换。
- 前端执行构建验证，确保 TypeScript/Vue 编译通过。
- 后端执行测试或至少编译验证，确保 Mapper/Controller 正常。
