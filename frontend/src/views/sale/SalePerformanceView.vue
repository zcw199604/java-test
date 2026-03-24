<template>
  <div class="page-stack">
    <div class="kpi-grid three">
      <KpiCard label="月度销售额" :value="metrics.salesAmount" hint="结合真实销售统计接口和演示趋势" tag-text="月度" />
      <KpiCard label="回款率" :value="metrics.paymentRate" hint="订单回款占比" tag-text="回款" tag-type="success" />
      <KpiCard label="区域覆盖" :value="metrics.regionCount" hint="当前活跃销售区域数" tag-text="区域" tag-type="warning" />
    </div>
    <div class="two-column-grid">
      <PageSection title="销售趋势" description="折线图展示销售额变化。"><AppChart :option="lineOption" /></PageSection>
      <PageSection title="绩效仪表盘" description="面向销售主管的达成率展示。"><AppChart :option="gaugeOption" /></PageSection>
    </div>
    <PageSection title="区域分析" description="查看区域客户贡献与趋势。">
      <AppTable :columns="columns" :rows="areaRows" :pagination="false" />
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import PageSection from '../../components/PageSection.vue'
import KpiCard from '../../components/KpiCard.vue'
import AppChart from '../../components/AppChart.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchSalesStatistics } from '../../api/sales'

const metrics = reactive({ salesAmount: '¥0.00', paymentRate: '0%', regionCount: 6 })
const areaRows = ref([
  { region: '杭州', amount: '¥128,000', stores: 42 },
  { region: '宁波', amount: '¥96,000', stores: 31 },
  { region: '绍兴', amount: '¥72,000', stores: 24 }
])
const columns = [{ key: 'region', label: '区域' }, { key: 'amount', label: '销售额' }, { key: 'stores', label: '门店数' }]

const lineOption = computed(() => ({ xAxis: { type: 'category', data: ['1周', '2周', '3周', '4周'] }, yAxis: { type: 'value' }, series: [{ type: 'line', smooth: true, data: [32, 46, 58, 73] }] }))
const gaugeOption = computed(() => ({ series: [{ type: 'gauge', progress: { show: true }, data: [{ value: 82, name: '达成率' }] }] }))

onMounted(async () => {
  const result = await fetchSalesStatistics().catch(() => ({ data: {} }))
  const data = result.data || {}
  metrics.salesAmount = data.totalSalesAmount ? `¥${data.totalSalesAmount}` : '¥286,000'
  metrics.paymentRate = data.paymentRate ? `${data.paymentRate}%` : '87%'
})
</script>
