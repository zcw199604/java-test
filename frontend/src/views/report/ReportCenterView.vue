<template>
  <div class="page-grid">
    <DataPanel title="汇总报表" description="展示采购、销售和库存汇总信息。">
      <template #actions>
        <button class="primary-btn" @click="handleExport">导出 Excel</button>
      </template>
      <div class="stats-row">
        <div class="stat-box">
          <strong>{{ purchaseSummary.count || 0 }}</strong>
          <span>采购单数 / {{ purchaseSummary.amount || 0 }}</span>
        </div>
        <div class="stat-box">
          <strong>{{ salesSummary.count || 0 }}</strong>
          <span>销售单数 / {{ salesSummary.amount || 0 }}</span>
        </div>
        <div class="stat-box">
          <strong>{{ inventorySummary.count || 0 }}</strong>
          <span>库存品项数 / {{ inventorySummary.amount || 0 }}</span>
        </div>
      </div>
    </DataPanel>

    <DataPanel title="库存台账" description="基于库存接口展示实际库存明细。">
      <SimpleTable :columns="inventoryColumns" :rows="inventoryRows" />
    </DataPanel>

    <DataPanel title="经营趋势图" description="使用 ECharts 展示当前趋势。">
      <TrendChart :points="trends" />
    </DataPanel>

    <DataPanel title="导出说明" description="展示导出接口当前输出格式。">
      <pre class="export-box">{{ exportPreview }}</pre>
    </DataPanel>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import DataPanel from '../../components/DataPanel.vue'
import SimpleTable from '../../components/SimpleTable.vue'
import TrendChart from '../../components/TrendChart.vue'
import { fetchInventories } from '../../api/inventory'
import { fetchPurchaseSummary, fetchSalesSummary, fetchInventorySummary, fetchTrend, fetchExportData, exportReport } from '../../api/report'

const purchaseSummary = ref({})
const salesSummary = ref({})
const inventorySummary = ref({})
const inventoryRows = ref([])
const trends = ref([])
const exportPreview = ref('')
const inventoryColumns = [
  { key: 'productName', label: '商品' },
  { key: 'warehouseName', label: '仓库' },
  { key: 'quantity', label: '库存数量' },
  { key: 'warningThreshold', label: '预警阈值' }
]

const handleExport = async () => {
  await exportReport()
}

onMounted(async () => {
  const [p, s, i, rows, t, e] = await Promise.all([
    fetchPurchaseSummary(),
    fetchSalesSummary(),
    fetchInventorySummary(),
    fetchInventories(),
    fetchTrend(),
    fetchExportData()
  ])
  purchaseSummary.value = p.data
  salesSummary.value = s.data
  inventorySummary.value = i.data
  inventoryRows.value = rows.data
  trends.value = t.data
  exportPreview.value = e.data
})
</script>
