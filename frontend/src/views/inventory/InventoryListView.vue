<template>
  <div class="page-stack">
    <div class="kpi-grid three">
      <KpiCard label="库存品项" :value="rows.length" hint="当前库存记录总数" tag-text="库存" />
      <KpiCard label="预警品项" :value="warningCount" hint="低于或等于预警阈值的商品数" tag-text="预警" tag-type="danger" />
      <KpiCard label="仓库数" :value="warehouseCount" hint="库存覆盖的仓库数量" tag-text="仓库" tag-type="success" />
    </div>

    <PageSection title="库存总览" description="实时展示库存、仓库和预警阈值，支持颜色编码提醒。">
      <template #actions>
        <el-space>
          <el-upload :show-file-list="false" accept=".xlsx,.xls" :http-request="handleImport">
            <el-button v-permission="'inventory:import'">导入 Excel</el-button>
          </el-upload>
          <el-button v-permission="'inventory:view'" @click="handleExport">导出 Excel</el-button>
        </el-space>
      </template>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索商品/仓库" clearable class="filter-item" />
        <el-select v-model="statusFilter" placeholder="库存状态" clearable class="filter-item">
          <el-option label="预警中" value="warning" />
          <el-option label="正常" value="normal" />
        </el-select>
      </div>
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
import { ElMessage } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import KpiCard from '../../components/KpiCard.vue'
import { fetchInventories, importInventories } from '../../api/inventory'
import { exportRowsToExcel } from '../../utils/export'

const keyword = ref('')
const statusFilter = ref('')
const rows = ref([])
const columns = [{ key: 'productName', label: '商品' }, { key: 'warehouseName', label: '仓库' }, { key: 'quantity', label: '实时库存' }, { key: 'warningThreshold', label: '预警阈值' }, { key: 'updatedAt', label: '更新时间', minWidth: 180 }]
const warningCount = computed(() => rows.value.filter((item) => Number(item.quantity) <= Number(item.warningThreshold)).length)
const warehouseCount = computed(() => new Set(rows.value.map((item) => item.warehouseName)).size)
const filteredRows = computed(() => rows.value.filter((item) => {
  const matchKeyword = !keyword.value || `${item.productName}${item.warehouseName}`.includes(keyword.value)
  const isWarning = Number(item.quantity) <= Number(item.warningThreshold)
  const matchStatus = !statusFilter.value || (statusFilter.value === 'warning' ? isWarning : !isWarning)
  return matchKeyword && matchStatus
}))
const handleExport = () => exportRowsToExcel(filteredRows.value, '库存总览.xlsx', 'inventory')

const loadData = async () => {
  const result = await fetchInventories().catch(() => ({ data: [] }))
  rows.value = result.data || []
}

const handleImport = async ({ file }) => {
  try {
    const result = await importInventories(file)
    const d = result.data || {}
    ElMessage.success(`导入完成: 成功${d.success || 0}条, 失败${d.failed || 0}条`)
    if (d.errors) ElMessage.warning(d.errors)
    loadData()
  } catch (e) {
    ElMessage.error('导入失败: ' + (e.message || '未知错误'))
  }
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-item { width: 240px; }
</style>
