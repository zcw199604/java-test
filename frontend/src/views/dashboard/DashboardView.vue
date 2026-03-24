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
      <PageSection title="采销存趋势" description="采购、库存、销售三条业务曲线联动查看。">
        <AppChart :option="trendOption" :loading="loading" />
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
import { fetchDashboardSummary } from '../../api/dashboard'
import { fetchInventoryWarnings } from '../../api/inventory'
import { useAppStore } from '../../stores/app'
import { formatNumber } from '../../utils/format'

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

const router = useRouter()
const appStore = useAppStore()
const loading = ref(true)
const loadError = ref('')
const summary = reactive<SummaryState>({ purchaseCount: 0, inventoryCount: 0, salesCount: 0, warningCount: 0 })
const warningList = ref<WarningViewItem[]>([])
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

const trendOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['采购', '库存', '销售'] },
  grid: { left: 24, right: 24, bottom: 24, containLabel: true },
  xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
  yAxis: { type: 'value' },
  series: [
    { name: '采购', type: 'line', smooth: true, data: [12, 15, 18, 13, 20, 22, 17] },
    { name: '库存', type: 'line', smooth: true, data: [68, 66, 64, 61, 60, 58, 55] },
    { name: '销售', type: 'line', smooth: true, data: [8, 11, 14, 16, 15, 19, 21] }
  ]
}))

const loadData = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const [dashboardResult, warningResult] = await Promise.all([
      fetchDashboardSummary().catch(() => ({ data: {} })),
      fetchInventoryWarnings().catch(() => ({ data: [] }))
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

onMounted(loadData)
</script>
