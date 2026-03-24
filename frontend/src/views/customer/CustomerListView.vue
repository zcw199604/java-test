<template>
  <DataPanel title="客户管理" description="展示零售客户基础资料，用于销售下单与回款关联。">
    <SimpleTable :columns="columns" :rows="rows" />
  </DataPanel>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import DataPanel from '../../components/DataPanel.vue'
import SimpleTable from '../../components/SimpleTable.vue'
import { fetchCustomers } from '../../api/customer'

const rows = ref([])
const columns = [
  { key: 'name', label: '客户名称' },
  { key: 'contactName', label: '联系人' },
  { key: 'contactPhone', label: '联系电话' },
  { key: 'address', label: '地址' },
  { key: 'status', label: '状态' }
]

onMounted(async () => {
  try {
    const result = await fetchCustomers()
    rows.value = result.data || []
  } catch (error) {
    rows.value = []
  }
})
</script>
