<template>
  <div ref="chartRef" class="chart"></div>
</template>

<script setup>
import * as echarts from 'echarts'
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'

const props = defineProps({
  points: { type: Array, default: () => [] }
})
const chartRef = ref(null)
let chartInstance = null

const renderChart = () => {
  if (!chartRef.value) return
  if (!chartInstance) chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['采购金额', '销售金额'] },
    xAxis: { type: 'category', data: props.points.map(item => item.period) },
    yAxis: { type: 'value' },
    series: [
      { name: '采购金额', type: 'line', smooth: true, data: props.points.map(item => item.purchaseAmount) },
      { name: '销售金额', type: 'line', smooth: true, data: props.points.map(item => item.salesAmount) }
    ]
  })
}

watch(() => props.points, renderChart, { deep: true })
onMounted(renderChart)
onBeforeUnmount(() => { if (chartInstance) chartInstance.dispose() })
</script>

<style scoped>
.chart { width: 100%; height: 360px; }
</style>
