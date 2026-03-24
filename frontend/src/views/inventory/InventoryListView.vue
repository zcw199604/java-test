<template>
  <div class="page-stack">
    <PageSection title="库存总览" description="实时展示库存、仓库和预警阈值，支持颜色编码提醒。">
      <template #actions>
        <el-button v-permission="'inventory:view'" @click="handleExport">导出 Excel</el-button>
      </template>
      <el-input v-model="keyword" placeholder="搜索商品/仓库" clearable class="toolbar-single" />
      <AppTable :columns="columns" :rows="filteredRows">
        <template #quantity="{ row }">
          <el-text :type="Number(row.quantity) <= Number(row.warningThreshold) ? 'danger' : 'success'">{{ row.quantity }}</el-text>
        </template>
      </AppTable>
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchInventories } from '../../api/inventory'
import { exportRowsToExcel } from '../../utils/export'

const keyword = ref('')
const rows = ref([])
const columns = [{ key: 'productName', label: '商品' }, { key: 'warehouseName', label: '仓库' }, { key: 'quantity', label: '实时库存' }, { key: 'warningThreshold', label: '预警阈值' }, { key: 'updatedAt', label: '更新时间', minWidth: 180 }]
const filteredRows = computed(() => rows.value.filter((item) => !keyword.value || `${item.productName}${item.warehouseName}`.includes(keyword.value)))
const handleExport = () => exportRowsToExcel(filteredRows.value, '库存总览.xlsx', 'inventory')

onMounted(async () => {
  const result = await fetchInventories().catch(() => ({ data: [] }))
  rows.value = result.data || []
})
</script>
