<template>
  <div class="page-stack">
    <PageSection title="采购订单" description="展示采购订单、供应商、状态和关键时间节点。">
      <template #actions>
        <el-space>
          <el-upload :show-file-list="false" accept=".xlsx,.xls" :http-request="handleImport">
            <el-button v-permission="'purchase:edit'">导入 Excel</el-button>
          </el-upload>
          <el-button v-permission="'purchase:view'" @click="handleExport">导出 Excel</el-button>
          <el-button v-permission="'purchase:edit'" type="primary" @click="router.push('/purchase/order/create')">新建订单</el-button>
        </el-space>
      </template>
      <div class="toolbar-grid two">
        <el-input v-model="keyword" placeholder="搜索订单号/供应商/商品" clearable />
        <el-select v-model="statusFilter" clearable placeholder="全部状态">
          <el-option label="待审核" value="CREATED" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="已到货" value="RECEIVED" />
          <el-option label="已入库" value="INBOUND" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </div>
      <AppTable :columns="columns" :rows="filteredRows">
        <template #status="{ value }"><el-tag :type="statusTypeMap[value] || 'info'">{{ statusLabelMap[value] || value }}</el-tag></template>
        <template #actions="{ row }">
          <el-button v-permission="'order:approve'" link type="primary" @click="openAuditDialog(row)" v-if="row.status === 'CREATED'">审核</el-button>
          <el-button v-permission="'purchase:edit'" link type="danger" @click="handleCancel(row)" v-if="row.status === 'CREATED' || row.status === 'REJECTED'">取消</el-button>
          <el-button v-permission="'order:approve'" link type="warning" @click="handleReceive(row)" v-if="row.status === 'APPROVED'">到货</el-button>
          <el-button v-permission="'order:approve'" link type="success" @click="handleInbound(row)" v-if="row.status === 'RECEIVED'">入库</el-button>
        </template>
      </AppTable>
    </PageSection>

    <el-dialog v-model="auditDialogVisible" title="审核采购订单" width="480px">
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
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchPurchases, inboundPurchase, receivePurchase, auditPurchase, cancelPurchase, importPurchases } from '../../api/purchase'
import { exportRowsToExcel } from '../../utils/export'
import { statusLabelMap, statusTypeMap } from '../../utils/format'

const router = useRouter()
const keyword = ref('')
const statusFilter = ref('')
const rows = ref([])
const auditDialogVisible = ref(false)
const auditTarget = ref(null)
const auditRemark = ref('')

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
  const matchStatus = !statusFilter.value || item.status === statusFilter.value
  return matchKeyword && matchStatus
}))

const loadData = async () => {
  const result = await fetchPurchases().catch(() => ({ data: [] }))
  rows.value = result.data || []
}

const openAuditDialog = (row) => {
  auditTarget.value = row
  auditRemark.value = ''
  auditDialogVisible.value = true
}

const handleAudit = async (decision) => {
  await auditPurchase(auditTarget.value.id, { decision, remark: auditRemark.value })
  ElMessage.success(decision === 'APPROVED' ? '审核通过' : '已驳回')
  auditDialogVisible.value = false
  loadData()
}

const handleCancel = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', { confirmButtonText: '确认取消', cancelButtonText: '返回', type: 'warning' }).catch(() => ({ value: null }))
  if (value === null) return
  await cancelPurchase(row.id, { reason: value })
  ElMessage.success(`采购单 ${row.orderNo} 已取消`)
  loadData()
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

const handleImport = async ({ file }) => {
  try {
    const result = await importPurchases(file)
    const d = result.data || {}
    ElMessage.success(`导入完成: 成功${d.success || 0}条, 失败${d.failed || 0}条`)
    if (d.errors) ElMessage.warning(d.errors)
    loadData()
  } catch (e) {
    ElMessage.error('导入失败: ' + (e.message || '未知错误'))
  }
}

const handleExport = () => exportRowsToExcel(filteredRows.value, '采购订单.xlsx', 'purchase')

onMounted(loadData)
</script>
