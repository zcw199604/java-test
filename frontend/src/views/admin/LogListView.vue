<template>
  <div class="page-stack">
    <PageSection title="操作日志" description="按时间范围、操作模块筛选并支持导出 Excel。">
      <template #actions>
        <el-button v-permission="'admin:log:view'" @click="handleExport">导出 Excel</el-button>
      </template>
      <div class="toolbar-grid two">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        <el-input v-model="keyword" placeholder="搜索模块或操作人" clearable />
      </div>
      <AppTable :columns="columns" :rows="filteredRows" />
    </PageSection>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { exportRowsToExcel } from '../../utils/export'

const keyword = ref('')
const dateRange = ref([])
const rows = ref([
  { operator: 'admin', module: '用户管理', action: '新增账号', time: '2026-03-24 09:00:00' },
  { operator: 'buyer', module: '采购订单', action: '创建订单', time: '2026-03-24 09:25:00' },
  { operator: 'keeper', module: '库存盘点', action: '提交盘点', time: '2026-03-24 10:45:00' }
])
const columns = [{ key: 'operator', label: '操作人' }, { key: 'module', label: '模块' }, { key: 'action', label: '动作' }, { key: 'time', label: '时间', minWidth: 180 }]

const filteredRows = computed(() => rows.value.filter((item) => !keyword.value || `${item.operator}${item.module}${item.action}`.includes(keyword.value)))
const handleExport = () => exportRowsToExcel(filteredRows.value, '操作日志.xlsx', 'logs')
</script>
