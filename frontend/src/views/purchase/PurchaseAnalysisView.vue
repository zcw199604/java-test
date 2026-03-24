<template>
  <div class="page-stack">
    <div class="two-column-grid">
      <PageSection title="采购结构分析" description="按供应商与订单金额查看本期采购结构。">
        <AppChart :option="barOption" />
      </PageSection>
      <PageSection title="供应商占比" description="直观观察核心供应商贡献度。">
        <AppChart :option="pieOption" />
      </PageSection>
    </div>
    <PageSection title="采购明细" description="结合列表快速筛选异常金额订单。">
      <AppTable :columns="columns" :rows="rows" />
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import PageSection from '../../components/PageSection.vue'
import AppChart from '../../components/AppChart.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchPurchases } from '../../api/purchase'

const rows = ref([])
const columns = [{ key: 'orderNo', label: '订单号' }, { key: 'supplierName', label: '供应商' }, { key: 'productName', label: '商品' }, { key: 'totalAmount', label: '总额' }, { key: 'status', label: '状态' }]

const barOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: rows.value.map((item) => item.supplierName) },
  yAxis: { type: 'value' },
  series: [{ type: 'bar', data: rows.value.map((item) => item.totalAmount || 0), itemStyle: { color: '#295f4e' } }]
}))

const pieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{ type: 'pie', radius: '62%', data: rows.value.map((item) => ({ name: item.productName, value: item.totalAmount || 0 })) }]
}))

onMounted(async () => {
  const result = await fetchPurchases().catch(() => ({ data: [] }))
  rows.value = result.data || []
})
</script>
