<template>
  <div class="page-stack">
    <PageSection title="采购入库" description="由管理员、超级管理员或库管确认采购到货后的最终入库，库存将同步更新。">
      <div class="toolbar-grid three">
        <el-select v-model="selectedId" placeholder="选择待入库订单" clearable>
          <el-option v-for="item in receivableRows" :key="item.id" :label="`${item.orderNo} / ${item.productName}`" :value="item.id" />
        </el-select>
        <el-select v-model="warehouseId" placeholder="选择入库仓库" clearable>
          <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-input v-model="batchNo" placeholder="请输入批次号，例如 20260324-A" />
      </div>
      <AppTable :columns="columns" :rows="receivableRows" :pagination="false">
        <template #status="{ value }"><el-tag :type="statusTypeMap[value] || 'warning'">{{ statusLabelMap[value] || value }}</el-tag></template>
      </AppTable>
      <div class="dialog-footer">
        <el-button v-permission="['purchase:inbound', 'order:approve']" type="primary" :disabled="!selectedId || !warehouseId" @click="handleInbound">确认入库</el-button>
      </div>
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchPurchases, inboundPurchase } from '../../api/purchase'
import { fetchWarehouses } from '../../api/system'
import { statusLabelMap, statusTypeMap } from '../../utils/format'

const route = useRoute()
const rows = ref([])
const warehouseOptions = ref([])
const selectedId = ref(null)
const warehouseId = ref(null)
const batchNo = ref('20260324-A')
const columns = [
  { key: 'orderNo', label: '订单号' },
  { key: 'productName', label: '商品' },
  { key: 'quantity', label: '数量' },
  { key: 'warehouseName', label: '仓库' },
  { key: 'status', label: '状态' }
]
const receivableRows = computed(() => rows.value.filter((item) => item.status === 'RECEIVED'))
const selectedOrder = computed(() => receivableRows.value.find((item) => item.id === selectedId.value))

watch(selectedOrder, (order) => {
  warehouseId.value = order?.warehouseId || warehouseId.value || warehouseOptions.value[0]?.id || null
})

const loadData = async () => {
  const [purchaseResult, warehouseResult] = await Promise.all([
    fetchPurchases().catch(() => ({ data: [] })),
    fetchWarehouses().catch(() => ({ data: [] }))
  ])
  rows.value = purchaseResult.data || []
  warehouseOptions.value = (warehouseResult.data || []).filter((item) => item.status !== 'DISABLED')
  const routeId = route.query.id ? Number(route.query.id) : null
  selectedId.value = receivableRows.value.some((item) => Number(item.id) === routeId)
    ? routeId
    : (receivableRows.value[0]?.id || null)
  warehouseId.value = receivableRows.value.find((item) => item.id === selectedId.value)?.warehouseId || warehouseOptions.value[0]?.id || null
}

const handleInbound = async () => {
  await inboundPurchase(selectedId.value, { warehouseId: warehouseId.value, remark: batchNo.value })
  ElMessage.success(`批次 ${batchNo.value} 入库完成，库存已同步至所选仓库`)
  await loadData()
}

onMounted(loadData)
</script>
