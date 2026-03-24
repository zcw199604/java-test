<template>
  <div class="page-stack">
    <div class="kpi-grid three">
      <KpiCard label="销售额" :value="metrics.salesAmount" hint="当前账号可见销售订单汇总" tag-text="销售" />
      <KpiCard label="回款率" :value="metrics.paymentRate" hint="当前账号订单回款占比" tag-text="回款" tag-type="success" />
      <KpiCard label="订单数" :value="metrics.orderCount" hint="当前账号销售订单数" tag-text="订单" tag-type="warning" />
    </div>
    <div class="two-column-grid">
      <PageSection title="销售趋势" description="按订单创建时间统计当前账号销售额变化。"><AppChart :option="lineOption" /></PageSection>
      <PageSection title="绩效仪表盘" description="基于当前账号回款率计算达成率。"><AppChart :option="gaugeOption" /></PageSection>
    </div>
    <PageSection title="客户分析" description="当前账号名下客户销售贡献。">
      <AppTable :columns="columns" :rows="customerRows" :pagination="false" />
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import PageSection from '../../components/PageSection.vue'
import KpiCard from '../../components/KpiCard.vue'
import AppChart from '../../components/AppChart.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchSales } from '../../api/sales'

const rows = ref([])
const metrics = reactive({ salesAmount: '¥0.00', paymentRate: '0%', orderCount: 0 })
const columns = [{ key: 'customerName', label: '客户' }, { key: 'amount', label: '销售额' }, { key: 'orderCount', label: '订单数' }]

const trendRows = computed(() => {
  const map = new Map()
  rows.value.forEach((item) => {
    const key = String(item.createdAt || '').slice(0, 10) || '未知日期'
    map.set(key, (map.get(key) || 0) + Number(item.totalAmount || 0))
  })
  return Array.from(map.entries()).map(([date, amount]) => ({ date, amount }))
})

const customerRows = computed(() => {
  const map = new Map()
  rows.value.forEach((item) => {
    const key = item.customerName || '未知客户'
    const current = map.get(key) || { customerName: key, amount: 0, orderCount: 0 }
    current.amount += Number(item.totalAmount || 0)
    current.orderCount += 1
    map.set(key, current)
  })
  return Array.from(map.values()).map((item) => ({ ...item, amount: `¥${item.amount.toFixed(2)}` }))
})

const lineOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: trendRows.value.map((item) => item.date) },
  yAxis: { type: 'value' },
  series: [{ type: 'line', smooth: true, data: trendRows.value.map((item) => item.amount) }]
}))

const gaugeOption = computed(() => ({
  series: [{ type: 'gauge', progress: { show: true }, data: [{ value: Number(metrics.paymentRate.replace('%', '')) || 0, name: '回款率' }] }]
}))

onMounted(async () => {
  const result = await fetchSales().catch(() => ({ data: [] }))
  rows.value = result.data || []
  const totalAmount = rows.value.reduce((sum, item) => sum + Number(item.totalAmount || 0), 0)
  const paidAmount = rows.value.reduce((sum, item) => sum + Number(item.paidAmount || 0), 0)
  metrics.salesAmount = `¥${totalAmount.toFixed(2)}`
  metrics.orderCount = rows.value.length
  metrics.paymentRate = totalAmount > 0 ? `${((paidAmount / totalAmount) * 100).toFixed(0)}%` : '0%'
})
</script>
