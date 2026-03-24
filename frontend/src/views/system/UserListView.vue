<template>
  <DataPanel title="用户列表" description="展示系统账号、角色归属和启停用状态。">
    <SimpleTable :columns="columns" :rows="rows" />
  </DataPanel>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import DataPanel from '../../components/DataPanel.vue'
import SimpleTable from '../../components/SimpleTable.vue'
import { fetchUsers } from '../../api/system'

const rows = ref([])
const columns = [
  { key: 'username', label: '账号' },
  { key: 'realName', label: '姓名' },
  { key: 'roleName', label: '角色' },
  { key: 'status', label: '状态' },
  { key: 'createdAt', label: '创建时间' }
]

onMounted(async () => {
  try {
    const result = await fetchUsers()
    rows.value = result.data.records || result.data || []
  } catch (error) {
    rows.value = []
  }
})
</script>
