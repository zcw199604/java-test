<template>
  <div class="page-stack">
    <PageSection title="销售订单" description="查看客户订单、出库进度与回款状态。">
      <template #actions>
        <el-space>
          <el-button v-permission="'sale:view'" @click="handleExport">导出 Excel</el-button>
          <el-button v-permission="'sale:edit'" type="primary" @click="router.push('/sale/order/create')">新建销售单</el-button>
        </el-space>
      </template>
      <AppTable :columns="columns" :rows="rows">
        <template #status="{ value }"><el-tag :type="value === 'PAID' ? 'success' : 'warning'">{{ value === 'PAID' ? '已回款' : '处理中' }}</el-tag></template>
        <template #actions="{ row }">
          <el-button v-permission="'sale:edit'" link type="primary" @click="router.push(`/sale/order/${row.id}/edit`)">编辑</el-button>
          <el-button v-permission="'sale:edit'" link type="warning" @click="handleOutbound(row)" v-if="row.status === 'CREATED'">出库</el-button>
          <el-button v-permission="'sale:edit'" link type="success" @click="handlePayment(row)" v-if="row.status !== 'PAID'">回款</el-button>
        </template>
      </AppTable>
    </PageSection>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchSales, outboundSales, paymentSales } from '../../api/sales'
import { exportRowsToExcel } from '../../utils/export'

const router = useRouter()
const rows = ref([])
const columns = [{ key: 'orderNo', label: '订单号' }, { key: 'customerName', label: '客户' }, { key: 'productName', label: '商品' }, { key: 'totalAmount', label: '订单金额' }, { key: 'paidAmount', label: '回款金额' }, { key: 'status', label: '回款状态' }]

const loadData = async () => {
  const result = await fetchSales().catch(() => ({ data: [] }))
  rows.value = result.data || []
}

const handleOutbound = async (row) => {
  await outboundSales(row.id)
  ElMessage.success(`销售单 ${row.orderNo} 已完成出库`)
  loadData()
}

const handlePayment = async (row) => {
  await paymentSales(row.id, { amount: row.totalAmount || 0, payerName: row.customerName || '客户', remark: '前端回款登记' })
  ElMessage.success(`销售单 ${row.orderNo} 已登记回款`)
  loadData()
}

const handleExport = () => exportRowsToExcel(rows.value, '销售订单.xlsx', 'sale')

onMounted(loadData)
</script>
