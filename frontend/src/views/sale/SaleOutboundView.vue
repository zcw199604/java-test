<template>
  <div class="page-stack">
    <PageSection title="销售出库" description="仅管理员、超级管理员或库管可对已审核通过的销售单执行出库确认并自动扣减库存。">
      <div class="toolbar-grid three">
        <el-select v-model="selectedId" placeholder="选择待出库销售单" clearable>
          <el-option v-for="item in outboundRows" :key="item.id" :label="`${item.orderNo} / ${item.customerName}`" :value="item.id" />
        </el-select>
        <el-select v-model="warehouseId" placeholder="选择出库仓库" clearable>
          <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-input v-model="deliveryInfo" placeholder="请输入配送信息" />
      </div>
      <AppTable :columns="columns" :rows="outboundRows" :pagination="false" />
      <div class="dialog-footer">
        <el-button v-permission="['sales:outbound', 'order:approve']" type="primary" :disabled="!selectedId || !warehouseId" @click="handleOutbound">确认出库</el-button>
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
import { fetchSales, outboundSales } from '../../api/sales'
import { fetchWarehouses } from '../../api/system'

const route = useRoute()
const rows = ref([])
const warehouseOptions = ref([])
const selectedId = ref(null)
const warehouseId = ref(null)
const deliveryInfo = ref('杭州主城区当日配送')
const columns = [
  { key: 'orderNo', label: '订单号' },
  { key: 'customerName', label: '客户' },
  { key: 'productName', label: '商品' },
  { key: 'quantity', label: '数量' },
  { key: 'warehouseName', label: '仓库' }
]
const outboundRows = computed(() => rows.value.filter((item) => item.status === 'APPROVED'))
const selectedOrder = computed(() => outboundRows.value.find((item) => item.id === selectedId.value))

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
  selectedId.value = outboundRows.value.some((item) => Number(item.id) === routeId)
    ? routeId
    : (outboundRows.value[0]?.id || null)
  warehouseId.value = outboundRows.value.find((item) => item.id === selectedId.value)?.warehouseId || warehouseOptions.value[0]?.id || null
}

const handleOutbound = async () => {
  await outboundSales(selectedId.value, { warehouseId: warehouseId.value, remark: deliveryInfo.value })
  ElMessage.success(`已完成出库，并从所选仓库登记配送信息：${deliveryInfo.value}`)
  await loadData()
}

onMounted(loadData)
</script>
