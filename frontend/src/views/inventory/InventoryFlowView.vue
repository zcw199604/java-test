<template>
  <div class="page-stack">
    <PageSection title="库存流水" description="按入库、出库、调拨分类展示，并支持独立登记表单。">
      <div class="toolbar">
        <el-select v-model="warehouseId" placeholder="仓库筛选" clearable class="filter-item" @change="loadData">
          <el-option label="全部仓库" :value="null" />
          <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-button @click="loadData">查询</el-button>
      </div>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="入库" name="inbound"><AppTable :columns="columns" :rows="inboundRows" /></el-tab-pane>
        <el-tab-pane label="出库" name="outbound"><AppTable :columns="columns" :rows="outboundRows" /></el-tab-pane>
        <el-tab-pane label="调拨" name="transfer">
          <AppTable :columns="columns" :rows="transferRows" :pagination="false" />
          <el-form :model="form" label-position="top" class="form-grid-card">
            <el-form-item label="商品">
              <el-select v-model="form.productId" placeholder="选择商品" filterable>
                <el-option v-for="item in productOptions" :key="item.id" :label="`${item.code} / ${item.name}`" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="调出仓库">
              <el-select v-model="form.fromWarehouseId" placeholder="选择调出仓库">
                <el-option v-for="item in warehouseOptions" :key="`from-${item.id}`" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="调入仓库">
              <el-select v-model="form.toWarehouseId" placeholder="选择调入仓库">
                <el-option v-for="item in targetWarehouseOptions" :key="`to-${item.id}`" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="调拨数量"><el-input-number v-model="form.quantity" :min="1" style="width: 100%" /></el-form-item>
            <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
          </el-form>
          <div class="dialog-footer"><el-button v-permission="'inventory:edit'" type="primary" @click="handleSubmit">提交调拨</el-button></div>
        </el-tab-pane>
      </el-tabs>
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchInventoryRecords, createInventoryAction } from '../../api/inventory'
import { fetchWarehouses } from '../../api/system'
import { fetchProducts } from '../../api/catalog'

const rows = ref([])
const warehouseOptions = ref([])
const productOptions = ref([])
const activeTab = ref('inbound')
const warehouseId = ref(null)
const form = reactive({
  actionType: 'TRANSFER',
  productId: null,
  fromWarehouseId: null,
  toWarehouseId: null,
  quantity: 10,
  remark: '仓库间调拨'
})
const columns = [
  { key: 'productName', label: '商品' },
  { key: 'warehouseName', label: '当前仓库' },
  { key: 'fromWarehouseName', label: '调出仓库' },
  { key: 'toWarehouseName', label: '调入仓库' },
  { key: 'bizType', label: '类型' },
  { key: 'changeQty', label: '变动量' },
  { key: 'afterQty', label: '结果库存' },
  { key: 'createdAt', label: '时间', minWidth: 180 }
]
const inboundRows = computed(() => rows.value.filter((item) => ['PURCHASE_INBOUND', 'INITIAL', 'CHECK', 'TRANSFER_IN'].includes(item.bizType)))
const outboundRows = computed(() => rows.value.filter((item) => ['SALES_OUTBOUND', 'OUTBOUND', 'TRANSFER_OUT'].includes(item.bizType)))
const transferRows = computed(() => rows.value.filter((item) => ['TRANSFER', 'TRANSFER_IN', 'TRANSFER_OUT'].includes(item.bizType)))
const targetWarehouseOptions = computed(() => warehouseOptions.value.filter((item) => item.id !== form.fromWarehouseId))

watch(() => form.fromWarehouseId, (value) => {
  if (value && value === form.toWarehouseId) {
    form.toWarehouseId = targetWarehouseOptions.value[0]?.id || null
  }
})

const loadOptions = async () => {
  const [warehouseResult, productResult] = await Promise.all([
    fetchWarehouses().catch(() => ({ data: [] })),
    fetchProducts().catch(() => ({ data: [] }))
  ])
  warehouseOptions.value = (warehouseResult.data || []).filter((item) => item.status !== 'DISABLED')
  productOptions.value = productResult.data || []
  if (!form.productId) form.productId = productOptions.value[0]?.id || null
  if (!form.fromWarehouseId) form.fromWarehouseId = warehouseOptions.value[0]?.id || null
  if (!form.toWarehouseId) form.toWarehouseId = targetWarehouseOptions.value[0]?.id || warehouseOptions.value[1]?.id || null
}

const loadData = async () => {
  const result = await fetchInventoryRecords({
    warehouseId: warehouseId.value || undefined
  }).catch(() => ({ data: [] }))
  rows.value = result.data || []
}

const handleTabChange = async () => {
  await loadData()
}

const handleSubmit = async () => {
  if (!form.productId || !form.fromWarehouseId || !form.toWarehouseId) {
    ElMessage.warning('请选择商品、调出仓库和调入仓库')
    return
  }
  if (form.fromWarehouseId === form.toWarehouseId) {
    ElMessage.warning('调出仓库和调入仓库不能相同')
    return
  }
  await createInventoryAction({ ...form })
  ElMessage.success('调拨登记成功')
  await loadData()
}

onMounted(async () => {
  await Promise.all([loadOptions(), loadData()])
})
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-item { width: 240px; }
</style>
