<template>
  <div class="page-stack">
    <PageSection :title="pageTitle" :description="pageDescription">
      <el-steps :active="2" simple finish-status="success">
        <el-step title="选择客户" />
        <el-step title="录入商品" />
        <el-step title="确认出库策略" />
      </el-steps>

      <template v-if="isDetail">
        <div class="toolbar-grid four">
          <el-card v-for="item in summaryCards" :key="item.label" shadow="never">
            <strong>{{ item.label }}</strong>
            <p>{{ item.value }}</p>
          </el-card>
        </div>

        <el-card shadow="never" class="detail-card">
          <template #header>订单详情</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item v-for="item in detailFields" :key="item.label" :label="item.label">
              {{ item.value }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <template #header>流程时间线</template>
          <el-timeline v-if="traceRows.length">
            <el-timeline-item v-for="item in traceRows" :key="item.id || `${item.nodeCode}-${item.createdAt}`" :timestamp="item.createdAt || '--'">
              <div class="trace-title">
                <strong>{{ item.nodeName || item.nodeCode || '--' }}</strong>
                <el-tag size="small" :type="item.nodeCode === 'PAYMENT' ? 'success' : 'primary'">{{ item.nodeCode || '--' }}</el-tag>
              </div>
              <p>操作人：{{ item.operator || '--' }}</p>
              <p>备注：{{ item.remark || '无' }}</p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无流程记录" />
        </el-card>
      </template>

      <template v-else>
        <el-form :model="form" label-position="top" class="form-grid-card">
          <el-form-item label="客户">
            <el-select v-model="form.customerId"><el-option v-for="item in customers" :key="item.id" :label="item.name" :value="item.id" /></el-select>
          </el-form-item>
          <el-form-item label="商品">
            <el-select v-model="form.productId"><el-option v-for="item in products" :key="item.id" :label="item.name" :value="item.id" /></el-select>
          </el-form-item>
          <el-form-item label="数量"><el-input-number v-model="form.quantity" :min="1" style="width: 100%" /></el-form-item>
          <el-form-item label="单价"><el-input-number v-model="form.unitPrice" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        </el-form>
        <el-alert v-if="lowStockWarning" type="error" :closable="false" show-icon title="当前库存低于安全阈值，请谨慎提交销售订单" />
      </template>

      <div class="dialog-footer">
        <el-button @click="router.back()">{{ isDetail ? '返回' : '取消' }}</el-button>
        <el-button v-if="!isDetail" v-permission="'sale:edit'" type="primary" :loading="submitting" @click="handleSubmit">{{ isEdit ? '保存并重新提交审核' : '保存订单' }}</el-button>
      </div>
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import { fetchProducts } from '../../api/catalog'
import { fetchCustomers } from '../../api/customer'
import { fetchInventories } from '../../api/inventory'
import { createSales, fetchSalesDetail, fetchSalesTrace, updateSales } from '../../api/sales'
import { statusLabelMap } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => route.name === 'sale-order-edit')
const isDetail = computed(() => route.name === 'sale-order-detail')
const pageTitle = computed(() => (isDetail.value ? '销售单详情' : (isEdit.value ? '编辑销售单' : '新建销售单')))
const pageDescription = computed(() => (isDetail.value ? '查看销售单明细、出库仓库与完整流程时间。' : '采用步骤条录入客户、商品和数量，并实时校验库存阈值。'))
const submitting = ref(false)
const products = ref([])
const customers = ref([])
const inventories = ref([])
const detailData = ref({})
const traceRows = ref([])
const form = reactive({ customerId: undefined, productId: undefined, quantity: 5, unitPrice: 480 })

const displayValue = (value, fallback = '--') => {
  if (value === null || value === undefined || value === '') return fallback
  return value
}

const displayMoney = (value) => {
  if (value === null || value === undefined || value === '') return '--'
  const amount = Number(value)
  return Number.isNaN(amount) ? value : amount.toFixed(2)
}

const receivableAmount = computed(() => {
  const total = Number(detailData.value.totalAmount || 0)
  const paid = Number(detailData.value.paidAmount || 0)
  return Math.max(total - paid, 0)
})

const summaryCards = computed(() => ([
  { label: '订单号', value: displayValue(detailData.value.orderNo) },
  { label: '订单状态', value: displayValue(statusLabelMap[detailData.value.status] || detailData.value.status) },
  { label: '出库仓库', value: displayValue(detailData.value.warehouseName) },
  { label: '回款金额', value: displayMoney(detailData.value.paidAmount) }
]))

const detailFields = computed(() => ([
  { label: '订单号', value: displayValue(detailData.value.orderNo) },
  { label: '订单状态', value: displayValue(statusLabelMap[detailData.value.status] || detailData.value.status) },
  { label: '客户', value: displayValue(detailData.value.customerName) },
  { label: '商品', value: displayValue(detailData.value.productName) },
  { label: '数量', value: displayValue(detailData.value.quantity) },
  { label: '单价', value: displayMoney(detailData.value.unitPrice) },
  { label: '订单金额', value: displayMoney(detailData.value.totalAmount) },
  { label: '回款金额', value: displayMoney(detailData.value.paidAmount) },
  { label: '待回款金额', value: displayMoney(receivableAmount.value) },
  { label: '创建人', value: displayValue(detailData.value.createdBy) },
  { label: '创建时间', value: displayValue(detailData.value.createdAt) },
  { label: '审核人', value: displayValue(detailData.value.auditedBy) },
  { label: '审核时间', value: displayValue(detailData.value.auditedAt) },
  { label: '审核意见', value: displayValue(detailData.value.auditRemark, '无') },
  { label: '出库时间', value: displayValue(detailData.value.outboundAt) },
  { label: '出库仓库', value: displayValue(detailData.value.warehouseName) },
  { label: '取消原因', value: displayValue(detailData.value.cancelReason, '无') }
]))

const lowStockWarning = computed(() => {
  const current = inventories.value.find((item) => item.productId === form.productId || item.productName === products.value.find((p) => p.id === form.productId)?.name)
  return current && Number(current.quantity) <= Number(current.warningThreshold)
})

const loadDetail = async () => {
  if (!route.params.id) return
  const result = await fetchSalesDetail(route.params.id).catch(() => ({ data: null }))
  const data = result.data || {}
  detailData.value = data
  form.customerId = data.customerId
  form.productId = data.productId
  form.quantity = Number(data.quantity || 1)
  form.unitPrice = Number(data.unitPrice || 0)
}

const loadTrace = async () => {
  if (!route.params.id || !isDetail.value) return
  const result = await fetchSalesTrace(route.params.id).catch(() => ({ data: [] }))
  traceRows.value = result.data || []
}

const handleSubmit = async () => {
  if (!form.customerId || !form.productId) {
    ElMessage.warning('请选择客户和商品')
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateSales(route.params.id, { ...form })
      ElMessage.success('销售单已重新保存并回到待审核状态')
    } else {
      await createSales({ ...form })
      ElMessage.success('销售单已创建')
    }
    router.push('/sale/order')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const [productResult, customerResult, inventoryResult] = await Promise.all([
    fetchProducts().catch(() => ({ data: [] })),
    fetchCustomers().catch(() => ({ data: [] })),
    fetchInventories().catch(() => ({ data: [] }))
  ])
  products.value = productResult.data || []
  customers.value = customerResult.data || []
  inventories.value = inventoryResult.data || []
  await Promise.all([loadDetail(), loadTrace()])
})
</script>

<style scoped>
.detail-card {
  margin-top: 16px;
}

.trace-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
</style>
