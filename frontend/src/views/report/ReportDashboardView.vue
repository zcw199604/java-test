<template>
  <div class="page-stack">
    <div class="kpi-grid three">
      <KpiCard label="采购汇总" :value="purchaseLabel" hint="采购单数与累计金额" tag-text="采购" />
      <KpiCard label="销售汇总" :value="salesLabel" hint="销售单数与累计金额" tag-text="销售" tag-type="success" />
      <KpiCard label="库存汇总" :value="inventoryLabel" hint="库存品项数与总库存" tag-text="库存" tag-type="warning" />
    </div>
    <div class="kpi-grid three">
      <KpiCard label="应收金额" :value="receivableLabel" hint="PSI 汇总接口返回的应收账款" tag-text="回款" tag-type="danger" />
      <KpiCard label="预警品项" :value="warningCount" hint="采销存联动接口返回的库存预警数量" tag-text="预警" />
      <KpiCard label="联动分类" :value="linkageRows.length" hint="具备采销联动分析的品类数" tag-text="联动" tag-type="success" />
    </div>
    <div class="report-board">
      <PageSection title="经营趋势" description="大屏视图展示整体经营趋势与导出能力。">
        <template #actions><el-button v-permission="'report:view'" type="primary" @click="handleExport">一键导出</el-button></template>
        <AppChart :option="lineOption" />
      </PageSection>
      <PageSection title="库存构成" description="按库存量快速定位重点商品。"><AppChart :option="barOption" /></PageSection>
      <PageSection title="采销存联动" description="按品类查看采购额与销售额联动关系。"><AppChart :option="linkageOption" /></PageSection>
      <PageSection title="导出预览" description="直观查看导出接口当前说明。"><pre class="csv-preview">{{ exportPreview }}</pre></PageSection>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import PageSection from '../../components/PageSection.vue'
import KpiCard from '../../components/KpiCard.vue'
import AppChart from '../../components/AppChart.vue'
import { fetchExportData, exportReport, fetchInventorySummary, fetchLinkage, fetchPsiSummary, fetchTrend } from '../../api/report'
import { fetchInventories } from '../../api/inventory'

const psiSummary = ref({ purchase: {}, sales: {}, inventory: {}, receivableAmount: 0 })
const inventoryRows = ref([])
const trendRows = ref([])
const exportPreview = ref('')
const linkageRows = ref([])
const warningRows = ref([])

const purchaseLabel = computed(() => `${psiSummary.value.purchase?.count || 0} 单 / ${psiSummary.value.purchase?.amount || 0}`)
const salesLabel = computed(() => `${psiSummary.value.sales?.count || 0} 单 / ${psiSummary.value.sales?.amount || 0}`)
const inventoryLabel = computed(() => `${psiSummary.value.inventory?.count || 0} 项 / ${psiSummary.value.inventory?.amount || 0}`)
const receivableLabel = computed(() => String(psiSummary.value.receivableAmount || 0))
const warningCount = computed(() => warningRows.value.length)
const lineOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['采购金额', '销售金额'] },
  xAxis: { type: 'category', data: trendRows.value.map((item) => item.period || '--') },
  yAxis: { type: 'value' },
  series: [
    { name: '采购金额', type: 'line', smooth: true, data: trendRows.value.map((item) => Number(item.purchaseAmount || 0)) },
    { name: '销售金额', type: 'line', smooth: true, data: trendRows.value.map((item) => Number(item.salesAmount || 0)) }
  ]
}))
const barOption = computed(() => ({ xAxis: { type: 'category', data: inventoryRows.value.map((item) => item.productName) }, yAxis: { type: 'value' }, series: [{ type: 'bar', data: inventoryRows.value.map((item) => item.quantity || 0) }] }))
const linkageOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['采购额', '销售额'] },
  xAxis: { type: 'category', data: linkageRows.value.map((item) => item.category || '未分类') },
  yAxis: { type: 'value' },
  series: [
    { name: '采购额', type: 'bar', data: linkageRows.value.map((item) => Number(item.purchaseAmount || 0)) },
    { name: '销售额', type: 'bar', data: linkageRows.value.map((item) => Number(item.salesAmount || 0)) }
  ]
}))
const handleExport = async () => exportReport()

onMounted(async () => {
  const [psi, trend, exportText, inventories, linkage] = await Promise.all([
    fetchPsiSummary().catch(() => ({ data: {} })),
    fetchTrend().catch(() => ({ data: [] })),
    fetchExportData().catch(() => ({ data: '' })),
    fetchInventories().catch(() => ({ data: [] })),
    fetchLinkage().catch(() => ({ data: {} })),
    fetchInventorySummary().catch(() => ({ data: {} }))
  ])
  psiSummary.value = psi.data || psiSummary.value
  trendRows.value = trend.data || []
  exportPreview.value = exportText.data || ''
  inventoryRows.value = inventories.data || []
  linkageRows.value = linkage.data?.categoryPurchaseSales || []
  warningRows.value = linkage.data?.inventoryWarnings || []
})
</script>
