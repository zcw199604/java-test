<template>
  <div class="page-stack">
    <div class="kpi-grid">
      <KpiCard label="采购订单" :value="formatNumber(summary.purchaseCount)" hint="今日订单与在途入库联动监控" tag-text="采购" />
      <KpiCard label="库存记录" :value="formatNumber(summary.inventoryCount)" hint="按库存台账实时汇总" tag-text="库存" tag-type="warning" />
      <KpiCard label="销售订单" :value="formatNumber(summary.salesCount)" hint="订单、出库与回款进度联查" tag-text="销售" tag-type="success" />
      <KpiCard label="预警消息" :value="formatNumber(summary.warningCount)" hint="低库存和异常待办集中提醒" tag-text="预警" tag-type="danger" />
    </div>

    <el-alert v-if="loadError" :title="loadError" type="warning" show-icon :closable="false" />

    <div class="two-column-grid">
      <PageSection title="历史烟品销售对比" :description="salesHistoryDescription">
        <template #actions>
          <div class="dashboard-toolbar">
            <el-radio-group :model-value="salesMetric" size="small" @change="handleMetricChange">
              <el-radio-button label="quantity">销售数量</el-radio-button>
              <el-radio-button label="amount">销售金额</el-radio-button>
            </el-radio-group>
            <el-radio-group :model-value="salesDays" size="small" @change="handleDaysChange">
              <el-radio-button :label="7">近7天</el-radio-button>
              <el-radio-button :label="30">近30天</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        <el-skeleton :loading="salesHistoryLoading" animated>
          <template #default>
            <el-empty v-if="!salesHistory.series.length" description="最近周期暂无历史销售数据" />
            <AppChart v-else :option="salesHistoryOption" :loading="salesHistoryLoading" />
          </template>
        </el-skeleton>
      </PageSection>
      <PageSection title="预警消息" description="库存阈值、流程堵点和异常提醒集中呈现。">
        <el-skeleton :loading="loading" animated :rows="4">
          <template #default>
            <el-empty v-if="!warningList.length" description="暂无预警消息" />
            <el-timeline v-else>
              <el-timeline-item v-for="item in warningList" :key="item.id" :type="item.type" :timestamp="item.time">
                <strong>{{ item.title }}</strong>
                <p>{{ item.content }}</p>
              </el-timeline-item>
            </el-timeline>
          </template>
        </el-skeleton>
      </PageSection>
    </div>

    <div class="two-column-grid">
      <PageSection title="最近操作日志" description="便于值班人员快速回看关键动作。">
        <AppTable :columns="logColumns" :rows="logRows" :pagination="false" :loading="loading" empty-text="暂无操作日志" />
      </PageSection>
      <PageSection title="工作台入口" description="根据当前角色展示重点模块入口。">
        <div class="portal-grid">
          <el-card v-for="item in visibleMenus" :key="item.path" class="portal-card" shadow="hover" @click="router.push(item.path)">
            <div class="portal-title">{{ item.title }}</div>
            <p>进入 {{ item.title }}，处理对应业务任务。</p>
          </el-card>
        </div>
      </PageSection>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { EChartsOption } from 'echarts'
import PageSection from '../../components/PageSection.vue'
import KpiCard from '../../components/KpiCard.vue'
import AppChart from '../../components/AppChart.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchDashboardSalesHistory, fetchDashboardSummary } from '../../api/dashboard'
import { fetchInventoryWarnings } from '../../api/inventory'
import { useAppStore } from '../../stores/app'
import { formatCurrency, formatNumber } from '../../utils/format'

interface SummaryState {
  purchaseCount: number
  inventoryCount: number
  salesCount: number
  warningCount: number
}

interface WarningViewItem {
  id: string
  title: string
  content: string
  type: 'warning' | 'primary' | 'success' | 'danger'
  time: string
}

interface SalesHistorySeries {
  productId: number
  productName: string
  values: Array<number | string>
}

interface SalesHistoryState {
  metric: 'quantity' | 'amount'
  periods: string[]
  series: SalesHistorySeries[]
}

const router = useRouter()
const appStore = useAppStore()
const loading = ref(true)
const salesHistoryLoading = ref(false)
const loadError = ref('')
const salesMetric = ref<'quantity' | 'amount'>('quantity')
const salesDays = ref<7 | 30>(7)
const summary = reactive<SummaryState>({ purchaseCount: 0, inventoryCount: 0, salesCount: 0, warningCount: 0 })
const warningList = ref<WarningViewItem[]>([])
const salesHistory = reactive<SalesHistoryState>({ metric: 'quantity', periods: [], series: [] })
const logRows = ref<Array<Record<string, unknown>>>([
  { operator: '系统管理员', action: '登录系统', module: '工作台', time: '2026-03-24 09:00:00' },
  { operator: '采购专员', action: '创建采购单', module: '采购管理', time: '2026-03-24 09:18:00' },
  { operator: '库管人员', action: '更新库存盘点', module: '库存管理', time: '2026-03-24 10:05:00' }
])
const logColumns: Array<{ key: string; label: string; minWidth?: number }> = [
  { key: 'operator', label: '操作人' },
  { key: 'action', label: '动作' },
  { key: 'module', label: '模块' },
  { key: 'time', label: '时间', minWidth: 180 }
]

const visibleMenus = computed(() => appStore.menuGroups.flatMap((group) => group.children).slice(0, 6))
const salesMetricLabel = computed(() => (salesMetric.value === 'amount' ? '销售金额' : '销售数量'))
const salesHistoryDescription = computed(() => `展示近 ${salesDays.value} 天重点烟品${salesMetricLabel.value}变化，支持按真实销售订单动态对比。`)
const salesHistoryOption = computed<EChartsOption>(() => ({
  tooltip: {
    trigger: 'axis',
    valueFormatter: (value: unknown) => {
      const numericValue = Number(Array.isArray(value) ? value[0] : value || 0)
      return salesMetric.value === 'amount' ? formatCurrency(numericValue) : `${formatNumber(numericValue)} 条`
    }
  },
  legend: { type: 'scroll', top: 0 },
  grid: { left: 24, right: 24, bottom: 24, top: 56, containLabel: true },
  xAxis: { type: 'category', data: salesHistory.periods },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: (value: number) => (salesMetric.value === 'amount' ? formatNumber(value) : `${value}`)
    }
  },
  series: salesHistory.series.map((item) => ({
    name: item.productName,
    type: 'line',
    smooth: true,
    showSymbol: false,
    emphasis: { focus: 'series' },
    data: item.values.map((value) => Number(value || 0))
  }))
}))

const loadSalesHistory = async () => {
  salesHistoryLoading.value = true
  try {
    const result = await fetchDashboardSalesHistory({ metric: salesMetric.value, days: salesDays.value, limit: 6 })
    salesHistory.metric = (result.data?.metric || salesMetric.value) as 'quantity' | 'amount'
    salesHistory.periods = result.data?.periods || []
    salesHistory.series = (result.data?.series || []) as SalesHistorySeries[]
  } finally {
    salesHistoryLoading.value = false
  }
}

const loadData = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const [dashboardResult, warningResult] = await Promise.all([
      fetchDashboardSummary().catch(() => ({ data: {} })),
      fetchInventoryWarnings().catch(() => ({ data: [] })),
      loadSalesHistory()
    ])
    Object.assign(summary, dashboardResult.data || {})
    warningList.value = (warningResult.data || []).map((item: Record<string, unknown>, index: number) => ({
      id: String(item.id || index + 1),
      title: `${item.productName || '库存项'} 库存预警`,
      content: `当前库存 ${item.quantity || 0}，低于预警阈值 ${item.warningThreshold || 0}`,
      type: 'warning',
      time: String(item.updatedAt || '刚刚')
    }))
    appStore.setNotifications(warningList.value)
  } catch (error) {
    loadError.value = '驾驶舱数据加载异常，当前已展示默认内容。'
  } finally {
    loading.value = false
  }
}

const handleMetricChange = async (value: 'quantity' | 'amount') => {
  salesMetric.value = value
  await loadSalesHistory()
}

const handleDaysChange = async (value: 7 | 30) => {
  salesDays.value = value
  await loadSalesHistory()
}

onMounted(loadData)
</script>

<style scoped>
.dashboard-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}
</style>
