<template>
  <DataPanel title="供应商列表" description="展示供应商基础资料和联系人信息。">
    <SimpleTable :columns="columns" :rows="rows" />
  </DataPanel>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import DataPanel from '../../components/DataPanel.vue'
import SimpleTable from '../../components/SimpleTable.vue'
import { fetchSuppliers } from '../../api/supplier'

const rows = ref([])
const columns = [
  { key: 'name', label: '供应商名称' },
  { key: 'contactName', label: '联系人' },
  { key: 'contactPhone', label: '联系电话' },
  { key: 'address', label: '地址' },
  { key: 'status', label: '状态' }
]

onMounted(async () => {
  try {
    const result = await fetchSuppliers()
    rows.value = result.data.records || result.data || []
  } catch (error) {
    rows.value = []
  }
})
</script>
