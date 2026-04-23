<template>
  <div class="page-stack">
    <PageSection title="销售出库" description="由管理员、超级管理员或库管选择仓库后完成销售出库，库存会同步扣减。">
      <div class="toolbar-grid three">
        <el-select v-model="selectedId" placeholder="选择待出库订单" clearable>
          <el-option v-for="item in outboundableRows" :key="item.id" :label="`${item.orderNo} / ${item.productName}`" :value="item.id" />
        </el-select>
        <el-select v-model="warehouseId" placeholder="选择出库仓库" clearable>
          <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-input v-model="remark" placeholder="请输入出库备注，例如 订单拣货完成" />
      </div>
      <AppTable :columns="columns" :rows="outboundableRows" :pagination="false">
        <template #status="{ value }"><el-tag :type="statusTypeMap[value] || 'warning'">{{ statusLabelMap[value] || value }}</el-tag></template>
      </AppTable>
      <div class="dialog-footer">
        <el-button @click="router.push('/sale/order')">返回订单列表</el-button>
        <el-button v-permission="'order:approve'" type="primary" :disabled="!selectedId || !warehouseId" :loading="submitting" @click="handleOutbound">确认出库</el-button>
      </div>
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchSales, outboundSales } from '../../api/sales'
import { fetchWarehouses } from '../../api/system'
import { statusLabelMap, statusTypeMap } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const rows = ref([])
const warehouseOptions = ref([])
const selectedId = ref(null)
const warehouseId = ref(null)
const remark = ref('销售出库')
const submitting = ref(false)

const columns = [
  { key: 'orderNo', label: '订单号', minWidth: 170 },
  { key: 'customerName', label: '客户' },
  { key: 'productName', label: '商品' },
  { key: 'quantity', label: '数量' },
  { key: 'warehouseName', label: '已选仓库' },
  { key: 'status', label: '状态' }
]

const outboundableRows = computed(() => rows.value.filter((item) => item.status === 'APPROVED'))
const selectedOrder = computed(() => outboundableRows.value.find((item) => Number(item.id) === Number(selectedId.value)))

watch(selectedOrder, (order) => {
  warehouseId.value = order?.warehouseId || warehouseId.value || warehouseOptions.value[0]?.id || null
})

const loadData = async () => {
  const [salesResult, warehouseResult] = await Promise.all([
    fetchSales().catch(() => ({ data: [] })),
    fetchWarehouses().catch(() => ({ data: [] }))
  ])
  rows.value = salesResult.data || []
  warehouseOptions.value = (warehouseResult.data || []).filter((item) => item.status !== 'DISABLED')
  const routeId = route.query.id ? Number(route.query.id) : null
  selectedId.value = outboundableRows.value.some((item) => Number(item.id) === routeId)
    ? routeId
    : (outboundableRows.value[0]?.id || null)
  warehouseId.value = outboundableRows.value.find((item) => Number(item.id) === Number(selectedId.value))?.warehouseId || warehouseOptions.value[0]?.id || null
}

const handleOutbound = async () => {
  if (!selectedId.value || !warehouseId.value) {
    ElMessage.warning('请选择待出库订单和仓库')
    return
  }
  submitting.value = true
  try {
    await outboundSales(selectedId.value, { warehouseId: warehouseId.value, remark: remark.value })
    ElMessage.success('销售出库完成，库存已同步扣减')
    router.push('/sale/order')
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
