<template>
  <div class="page-stack">
    <div class="kpi-grid three">
      <KpiCard label="采购汇总" :value="purchaseLabel" hint="采购单数与累计金额" tag-text="采购" />
      <KpiCard label="销售汇总" :value="salesLabel" hint="销售单数与累计金额" tag-text="销售" tag-type="success" />
      <KpiCard label="库存汇总" :value="inventoryLabel" hint="库存品项数与总库存" tag-text="库存" tag-type="warning" />
    </div>
    <div class="report-board">
      <PageSection title="经营趋势" description="大屏视图展示整体经营趋势与导出能力。">
        <template #actions><el-button v-permission="'report:view'" type="primary" @click="handleExport">一键导出</el-button></template>
        <AppChart :option="lineOption" />
      </PageSection>
      <PageSection title="库存构成" description="按库存量快速定位重点商品。"><AppChart :option="barOption" /></PageSection>
      <PageSection title="CSV 预览" description="直观查看导出接口返回的真实内容。"><pre class="csv-preview">{{ exportPreview }}</pre></PageSection>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import PageSection from '../../components/PageSection.vue'
import KpiCard from '../../components/KpiCard.vue'
import AppChart from '../../components/AppChart.vue'
import { fetchInventorySummary, fetchPurchaseSummary, fetchSalesSummary, fetchTrend, fetchExportData, exportReport } from '../../api/report'
import { fetchInventories } from '../../api/inventory'

const purchaseSummary = ref({})
const salesSummary = ref({})
const inventorySummary = ref({})
const inventoryRows = ref([])
const trendRows = ref([])
const exportPreview = ref('')

const purchaseLabel = computed(() => `${purchaseSummary.value.count || 0} 单 / ${purchaseSummary.value.amount || 0}`)
const salesLabel = computed(() => `${salesSummary.value.count || 0} 单 / ${salesSummary.value.amount || 0}`)
const inventoryLabel = computed(() => `${inventorySummary.value.count || 0} 项 / ${inventorySummary.value.amount || 0}`)
const lineOption = computed(() => ({ xAxis: { type: 'category', data: trendRows.value.map((item) => item.label || item.day || item.date) }, yAxis: { type: 'value' }, series: [{ type: 'line', data: trendRows.value.map((item) => item.value || item.amount || 0), smooth: true }] }))
const barOption = computed(() => ({ xAxis: { type: 'category', data: inventoryRows.value.map((item) => item.productName) }, yAxis: { type: 'value' }, series: [{ type: 'bar', data: inventoryRows.value.map((item) => item.quantity || 0) }] }))
const handleExport = async () => exportReport()

onMounted(async () => {
  const [p, s, i, trend, exportText, inventories] = await Promise.all([
    fetchPurchaseSummary().catch(() => ({ data: {} })),
    fetchSalesSummary().catch(() => ({ data: {} })),
    fetchInventorySummary().catch(() => ({ data: {} })),
    fetchTrend().catch(() => ({ data: [] })),
    fetchExportData().catch(() => ({ data: '' })),
    fetchInventories().catch(() => ({ data: [] }))
  ])
  purchaseSummary.value = p.data || {}
  salesSummary.value = s.data || {}
  inventorySummary.value = i.data || {}
  trendRows.value = trend.data || []
  exportPreview.value = exportText.data || ''
  inventoryRows.value = inventories.data || []
})
</script>
