<template>
  <div class="page-stack">
    <PageSection title="采购订单" description="展示采购订单、供应商、状态和关键时间节点。">
      <template #actions>
        <el-space>
          <el-button v-permission="'purchase:view'" @click="handleExport">导出 Excel</el-button>
          <el-button v-permission="'purchase:edit'" type="primary" @click="router.push('/purchase/order/create')">新建订单</el-button>
        </el-space>
      </template>
      <div class="toolbar-grid two">
        <el-input v-model="keyword" placeholder="搜索订单号/供应商/商品" clearable />
        <el-select v-model="status" clearable placeholder="全部状态"><el-option label="已创建" value="CREATED" /><el-option label="已到货" value="RECEIVED" /><el-option label="已入库" value="INBOUND" /></el-select>
      </div>
      <AppTable :columns="columns" :rows="filteredRows">
        <template #status="{ value }"><el-tag :type="statusTypeMap[value] || 'info'">{{ statusLabelMap[value] || value }}</el-tag></template>
        <template #actions="{ row }">
          <el-button v-permission="'purchase:edit'" link type="primary" @click="router.push(`/purchase/order/${row.id}/edit`)">编辑</el-button>
          <el-button v-permission="'purchase:edit'" link type="warning" @click="handleReceive(row)" v-if="row.status === 'CREATED'">到货</el-button>
          <el-button v-permission="'purchase:edit'" link type="success" @click="handleInbound(row)" v-if="row.status === 'RECEIVED'">入库</el-button>
        </template>
      </AppTable>
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchPurchases, inboundPurchase, receivePurchase } from '../../api/purchase'
import { exportRowsToExcel } from '../../utils/export'
import { statusLabelMap, statusTypeMap } from '../../utils/format'

const router = useRouter()
const keyword = ref('')
const status = ref('')
const rows = ref([])
const columns = [
  { key: 'orderNo', label: '订单号', minWidth: 170 },
  { key: 'supplierName', label: '供应商' },
  { key: 'productName', label: '商品' },
  { key: 'quantity', label: '数量' },
  { key: 'totalAmount', label: '总额' },
  { key: 'status', label: '状态' },
  { key: 'createdAt', label: '创建时间', minWidth: 180 }
]

const filteredRows = computed(() => rows.value.filter((item) => {
  const matchKeyword = !keyword.value || `${item.orderNo}${item.supplierName}${item.productName}`.includes(keyword.value)
  const matchStatus = !status.value || item.status === status.value
  return matchKeyword && matchStatus
}))

const loadData = async () => {
  const result = await fetchPurchases().catch(() => ({ data: [] }))
  rows.value = result.data || []
}

const handleReceive = async (row) => {
  await receivePurchase(row.id)
  ElMessage.success(`已登记采购单 ${row.orderNo} 到货`)
  loadData()
}

const handleInbound = async (row) => {
  await inboundPurchase(row.id)
  ElMessage.success(`采购单 ${row.orderNo} 已完成入库`)
  loadData()
}

const handleExport = () => exportRowsToExcel(filteredRows.value, '采购订单.xlsx', 'purchase')

onMounted(loadData)
</script>
