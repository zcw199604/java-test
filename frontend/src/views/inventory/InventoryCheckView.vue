<template>
  <div class="page-stack">
    <PageSection title="库存盘点" description="支持按仓库盘点库存、生成盈亏报告，并将差异同步更新到对应仓库库存。">
      <div class="toolbar">
        <el-select v-model="warehouseId" placeholder="选择盘点仓库" clearable class="filter-item" @change="loadData">
          <el-option label="全部仓库" :value="null" />
          <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-button @click="loadData">查询</el-button>
      </div>
      <el-table :data="rows" border stripe>
        <el-table-column prop="productName" label="商品" min-width="160" />
        <el-table-column prop="warehouseName" label="仓库" min-width="120" />
        <el-table-column prop="quantity" label="账面库存" min-width="120" />
        <el-table-column label="实盘数量" min-width="160">
          <template #default="scope">
            <el-input-number v-model="scope.row.actualQuantity" :min="0" />
          </template>
        </el-table-column>
      </el-table>
      <div class="dialog-footer">
        <el-button v-permission="'inventory:edit'" @click="handleReport">生成盈亏报告</el-button>
        <el-button
          v-permission="'inventory:edit'"
          type="primary"
          :disabled="!changedRows.length || submitting"
          :loading="submitting"
          @click="handleSubmit"
        >
          提交盘点结果
        </el-button>
      </div>
      <el-card shadow="never" v-if="reportRows.length">
        <div class="tree-title">盈亏报告</div>
        <AppTable :columns="reportColumns" :rows="reportRows" :pagination="false" />
      </el-card>
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { createInventoryAction, fetchInventories } from '../../api/inventory'
import { fetchWarehouses } from '../../api/system'

const rows = ref([])
const reportRows = ref([])
const submitting = ref(false)
const warehouseId = ref(null)
const warehouseOptions = ref([])
const reportColumns = [
  { key: 'productName', label: '商品' },
  { key: 'warehouseName', label: '仓库' },
  { key: 'quantity', label: '账面' },
  { key: 'actualQuantity', label: '实盘' },
  { key: 'difference', label: '差异' }
]

const buildReportRows = () => rows.value.map((item) => ({
  ...item,
  difference: Number(item.actualQuantity || 0) - Number(item.quantity || 0)
})).filter((item) => item.difference !== 0)

const changedRows = computed(() => buildReportRows())

const loadWarehouses = async () => {
  const result = await fetchWarehouses().catch(() => ({ data: [] }))
  warehouseOptions.value = (result.data || []).filter((item) => item.status !== 'DISABLED')
}

const loadData = async () => {
  const result = await fetchInventories({ warehouseId: warehouseId.value || undefined }).catch(() => ({ data: [] }))
  rows.value = (result.data || []).map((item) => ({ ...item, actualQuantity: item.quantity }))
  reportRows.value = []
}

const handleReport = () => {
  reportRows.value = buildReportRows()
  if (!reportRows.value.length) {
    ElMessage.info('当前无盘点差异，无需提交')
  }
}

const handleSubmit = async () => {
  if (!changedRows.value.length) {
    ElMessage.info('当前无盘点差异，无需提交')
    return
  }
  submitting.value = true
  try {
    for (const item of changedRows.value) {
      await createInventoryAction({
        actionType: 'CHECK',
        productId: item.productId,
        warehouseId: item.warehouseId,
        quantity: item.actualQuantity,
        remark: `库存盘点调整，${item.warehouseName || '中心仓'}实盘数量更新为 ${item.actualQuantity}`
      })
    }
    ElMessage.success(`已提交 ${changedRows.value.length} 条盘点结果，当前库存已更新`)
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadWarehouses(), loadData()])
})
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-item { width: 240px; }
</style>
