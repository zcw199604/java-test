<template>
  <div class="page-stack">
    <PageSection title="库存追溯台账" description="按仓库、类型和关键字查看采购入库、销售出库、调拨、盘点的完整库存变化轨迹。">
      <div class="toolbar-grid four">
        <el-select v-model="warehouseId" placeholder="仓库筛选" clearable class="filter-item">
          <el-option label="全部仓库" :value="null" />
          <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-select v-model="bizType" placeholder="类型筛选" clearable>
          <el-option label="采购入库" value="PURCHASE_INBOUND" />
          <el-option label="销售出库" value="SALES_OUTBOUND" />
          <el-option label="调拨入库" value="TRANSFER_IN" />
          <el-option label="调拨出库" value="TRANSFER_OUT" />
          <el-option label="库存盘点" value="CHECK" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索商品、仓库、备注或操作人" clearable />
        <el-button @click="loadData">查询</el-button>
      </div>
      <el-timeline>
        <el-timeline-item v-for="item in rows" :key="item.id || item.createdAt" :timestamp="item.createdAt">
          <div class="ledger-title">{{ item.productName }} · {{ statusLabelMap[item.bizType] || item.bizType }}</div>
          <p>
            仓库：{{ item.warehouseName || '全部仓库' }}；
            库存由 {{ item.beforeQty }} 变更至 {{ item.afterQty }}；
            操作人：{{ item.operatorName || '系统' }}；
            备注：{{ item.remark || '无' }}
          </p>
          <p v-if="item.fromWarehouseName || item.toWarehouseName">调拨路线：{{ item.fromWarehouseName || '--' }} → {{ item.toWarehouseName || '--' }}</p>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-if="!rows.length" description="暂无符合条件的库存追溯记录" />
    </PageSection>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { statusLabelMap } from '../../utils/format'
import PageSection from '../../components/PageSection.vue'
import { fetchInventoryRecords } from '../../api/inventory'
import { fetchWarehouses } from '../../api/system'

const rows = ref([])
const warehouseOptions = ref([])
const warehouseId = ref(null)
const bizType = ref('')
const keyword = ref('')

const loadData = async () => {
  const result = await fetchInventoryRecords({
    warehouseId: warehouseId.value || undefined,
    bizType: bizType.value || undefined,
    keyword: keyword.value || undefined
  }).catch(() => ({ data: [] }))
  rows.value = result.data || []
}

const loadWarehouses = async () => {
  const result = await fetchWarehouses().catch(() => ({ data: [] }))
  warehouseOptions.value = (result.data || []).filter((item) => item.status !== 'DISABLED')
}

onMounted(async () => {
  await Promise.all([loadWarehouses(), loadData()])
})
</script>

<style scoped>
.filter-item { width: 240px; }
</style>
