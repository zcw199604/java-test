<template>
  <div class="page-stack">
    <PageSection title="销售订单" description="查看客户订单、出库进度与回款状态。">
      <template #actions>
        <el-space>
          <el-upload :show-file-list="false" accept=".xlsx,.xls" :http-request="handleImport">
            <el-button v-permission="'sale:edit'">导入 Excel</el-button>
          </el-upload>
          <el-button v-permission="'sale:view'" @click="handleExport">导出 Excel</el-button>
          <el-button v-permission="'sale:edit'" type="primary" @click="router.push('/sale/order/create')">新建销售单</el-button>
        </el-space>
      </template>
      <AppTable :columns="columns" :rows="rows">
        <template #status="{ value }"><el-tag :type="statusTypeMap[value] || 'info'">{{ statusLabelMap[value] || value }}</el-tag></template>
        <template #actions="{ row }">
          <el-button v-permission="'order:approve'" link type="primary" @click="openAuditDialog(row)" v-if="row.status === 'CREATED'">审核</el-button>
          <el-button v-permission="'sale:edit'" link type="danger" @click="handleCancel(row)" v-if="row.status === 'CREATED' || row.status === 'REJECTED'">取消</el-button>
          <el-button v-permission="'order:approve'" link type="warning" @click="handleOutbound(row)" v-if="row.status === 'APPROVED'">出库</el-button>
          <el-button v-permission="'sale:edit'" link type="success" @click="handlePayment(row)" v-if="row.status !== 'PAID' && row.status !== 'CREATED' && row.status !== 'APPROVED' && row.status !== 'REJECTED' && row.status !== 'CANCELLED'">回款</el-button>
        </template>
      </AppTable>
    </PageSection>

    <el-dialog v-model="auditDialogVisible" title="审核销售订单" width="480px">
      <p>订单号: {{ auditTarget?.orderNo }}</p>
      <el-form>
        <el-form-item label="审核意见">
          <el-input v-model="auditRemark" type="textarea" :rows="3" placeholder="请输入审核意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleAudit('REJECTED')">驳回</el-button>
        <el-button type="primary" @click="handleAudit('APPROVED')">通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchSales, paymentSales, auditSales, cancelSales, importSales } from '../../api/sales'
import { exportRowsToExcel } from '../../utils/export'
import { statusLabelMap, statusTypeMap } from '../../utils/format'

const router = useRouter()
const rows = ref([])
const auditDialogVisible = ref(false)
const auditTarget = ref(null)
const auditRemark = ref('')

const columns = [{ key: 'orderNo', label: '订单号' }, { key: 'customerName', label: '客户' }, { key: 'productName', label: '商品' }, { key: 'totalAmount', label: '订单金额' }, { key: 'paidAmount', label: '回款金额' }, { key: 'status', label: '回款状态' }]

const loadData = async () => {
  const result = await fetchSales().catch(() => ({ data: [] }))
  rows.value = result.data || []
}

const openAuditDialog = (row) => {
  auditTarget.value = row
  auditRemark.value = ''
  auditDialogVisible.value = true
}

const handleAudit = async (decision) => {
  await auditSales(auditTarget.value.id, { decision, remark: auditRemark.value })
  ElMessage.success(decision === 'APPROVED' ? '审核通过' : '已驳回')
  auditDialogVisible.value = false
  loadData()
}

const handleCancel = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', { confirmButtonText: '确认取消', cancelButtonText: '返回', type: 'warning' }).catch(() => ({ value: null }))
  if (value === null) return
  await cancelSales(row.id, { reason: value })
  ElMessage.success(`销售单 ${row.orderNo} 已取消`)
  loadData()
}

const handleOutbound = (row) => {
  router.push({ path: '/sale/outbound', query: { id: String(row.id) } })
}

const handlePayment = async (row) => {
  await paymentSales(row.id, { amount: row.totalAmount || 0, payerName: row.customerName || '客户', remark: '前端回款登记' })
  ElMessage.success(`销售单 ${row.orderNo} 已登记回款`)
  loadData()
}

const handleImport = async ({ file }) => {
  try {
    const result = await importSales(file)
    const d = result.data || {}
    ElMessage.success(`导入完成: 成功${d.success || 0}条, 失败${d.failed || 0}条`)
    if (d.errors) ElMessage.warning(d.errors)
    loadData()
  } catch (e) {
    ElMessage.error('导入失败: ' + (e.message || '未知错误'))
  }
}

const handleExport = () => exportRowsToExcel(rows.value, '销售订单.xlsx', 'sale')

onMounted(loadData)
</script>
