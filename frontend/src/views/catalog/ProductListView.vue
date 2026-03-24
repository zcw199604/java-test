<template>
  <DataPanel title="商品列表" description="展示烟草商品、品类、价格和预警阈值。">
    <SimpleTable :columns="columns" :rows="rows" />
  </DataPanel>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import DataPanel from '../../components/DataPanel.vue'
import SimpleTable from '../../components/SimpleTable.vue'
import { fetchProducts } from '../../api/catalog'

const rows = ref([])
const columns = [
  { key: 'code', label: '商品编码' },
  { key: 'name', label: '商品名称' },
  { key: 'category', label: '品类' },
  { key: 'unitPrice', label: '单价' },
  { key: 'warningThreshold', label: '预警阈值' },
  { key: 'status', label: '状态' }
]

onMounted(async () => {
  try {
    const result = await fetchProducts()
    rows.value = result.data.records || result.data || []
  } catch (error) {
    rows.value = []
  }
})
</script>
