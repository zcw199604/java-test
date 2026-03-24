<template>
  <div class="page-grid">
    <DataPanel title="汇总报表" description="展示采购、销售和库存汇总信息。">
      <template #actions>
        <button class="primary-btn" @click="handleExport">导出 CSV</button>
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
      </div>
    </DataPanel>

    <DataPanel title="库存汇总" description="展示库存台账用于报表分析。">
      <SimpleTable :columns="inventoryColumns" :rows="inventoryRows" />
    </DataPanel>

    <DataPanel title="经营趋势图" description="使用 ECharts 展示当前趋势。">
      <TrendChart :points="trends" />
    </DataPanel>

    <DataPanel title="经营趋势明细" description="按日展示采购与销售趋势点。">
      <SimpleTable :columns="trendColumns" :rows="trends" />
    </DataPanel>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import DataPanel from '../../components/DataPanel.vue'
import SimpleTable from '../../components/SimpleTable.vue'
import TrendChart from '../../components/TrendChart.vue'
import { fetchPurchaseSummary, fetchSalesSummary, fetchInventorySummary, fetchTrend, exportReport } from '../../api/report'

const purchaseSummary = ref({})
const salesSummary = ref({})
const inventoryRows = ref([])
const trends = ref([])
const inventoryColumns = [
  { key: 'productName', label: '商品' },
  { key: 'warehouseName', label: '仓库' },
  { key: 'quantity', label: '库存数量' },
  { key: 'warningThreshold', label: '预警阈值' }
]
const trendColumns = [
  { key: 'period', label: '日期' },
  { key: 'purchaseAmount', label: '采购金额' },
  { key: 'salesAmount', label: '销售金额' }
]
const handleExport = async () => { await exportReport() }

onMounted(async () => {
  const [p, s, i, t] = await Promise.all([
    fetchPurchaseSummary(),
    fetchSalesSummary(),
    fetchInventorySummary(),
    fetchTrend()
  ])
  purchaseSummary.value = p.data
  salesSummary.value = s.data
  inventoryRows.value = i.data
  trends.value = t.data
})
</script>

<style scoped>
.page-grid { display: grid; gap: 24px; }
.stats-row { display: flex; gap: 16px; }
.stat-box { background: #f8fafc; border-radius: 14px; padding: 18px 20px; display: flex; flex-direction: column; gap: 8px; min-width: 220px; }
.stat-box strong { font-size: 28px; }
.primary-btn { border: none; background: #2563eb; color: #fff; border-radius: 10px; padding: 10px 14px; cursor: pointer; }
</style>
