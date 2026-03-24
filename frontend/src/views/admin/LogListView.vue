<template>
  <div class="page-stack">
    <PageSection title="操作日志" description="按时间范围、操作模块筛选并支持导出 Excel。">
      <template #actions>
        <el-button v-permission="'admin:log:view'" @click="handleExport">导出 Excel</el-button>
      </template>
      <div class="toolbar-grid two">
        <el-select v-model="logType" placeholder="请选择日志类型">
          <el-option label="操作日志" value="operation" />
          <el-option label="登录日志" value="login" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索模块、操作人或状态" clearable />
      </div>
      <AppTable :columns="columns" :rows="filteredRows" />
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { exportRowsToExcel } from '../../utils/export'
import { fetchLoginLogs, fetchOperationLogs } from '../../api/system'

const keyword = ref('')
const logType = ref('operation')
const loginRows = ref([])
const operationRows = ref([])
const columns = computed(() => logType.value === 'operation'
  ? [{ key: 'username', label: '操作人' }, { key: 'module', label: '模块' }, { key: 'action', label: '动作' }, { key: 'createdAt', label: '时间', minWidth: 180 }]
  : [{ key: 'username', label: '账号' }, { key: 'status', label: '状态' }, { key: 'message', label: '说明' }, { key: 'createdAt', label: '时间', minWidth: 180 }])

const currentRows = computed(() => (logType.value === 'operation' ? operationRows.value : loginRows.value))
const filteredRows = computed(() => currentRows.value.filter((item) => !keyword.value || JSON.stringify(item).includes(keyword.value)))
const handleExport = () => exportRowsToExcel(filteredRows.value, `${logType.value}-logs.xlsx`, 'logs')

onMounted(async () => {
  const [loginResult, operationResult] = await Promise.all([
    fetchLoginLogs().catch(() => ({ data: [] })),
    fetchOperationLogs().catch(() => ({ data: [] }))
  ])
  loginRows.value = loginResult.data || []
  operationRows.value = operationResult.data || []
})
</script>
