<template>
  <div v-loading="loading" ref="chartRef" class="chart-container" :style="{ minHeight: height }"></div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import type { ECharts, EChartsOption } from 'echarts'

const props = withDefaults(defineProps<{
  option: EChartsOption
  height?: string
  loading?: boolean
}>(), {
  height: '320px',
  loading: false
})

const chartRef = ref<HTMLDivElement | null>(null)
let chartInstance: ECharts | null = null
let echartsModule: typeof import('echarts') | null = null

const ensureModule = async () => {
  if (!echartsModule) {
    echartsModule = await import('echarts')
  }
  return echartsModule
}

const renderChart = async () => {
  await nextTick()
  if (!chartRef.value) return
  const echarts = await ensureModule()
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
    window.addEventListener('resize', handleResize)
  }
  chartInstance.setOption(props.option, true)
}

const handleResize = () => chartInstance?.resize()

watch(() => props.option, renderChart, { deep: true })
onMounted(renderChart)
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<style scoped>
.chart-container { width: 100%; }
</style>
