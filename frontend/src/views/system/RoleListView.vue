<template>
  <DataPanel title="角色列表" description="展示系统角色、编码和权限说明。">
    <SimpleTable :columns="columns" :rows="rows" />
  </DataPanel>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import DataPanel from '../../components/DataPanel.vue'
import SimpleTable from '../../components/SimpleTable.vue'
import { fetchRoles } from '../../api/system'

const rows = ref([])
const columns = [
  { key: 'code', label: '角色编码' },
  { key: 'name', label: '角色名称' },
  { key: 'remark', label: '说明' }
]

onMounted(async () => {
  try {
    const result = await fetchRoles()
    rows.value = result.data.records || result.data || []
  } catch (error) {
    rows.value = []
  }
})
</script>
