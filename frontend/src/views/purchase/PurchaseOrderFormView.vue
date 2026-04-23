<template>
  <div class="page-stack">
    <PageSection :title="pageTitle" :description="pageDescription">
      <el-steps :active="2" finish-status="success" simple>
        <el-step title="选择供应商" />
        <el-step title="录入明细" />
        <el-step title="提交审核" />
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
                <el-tag size="small">{{ item.nodeCode || '--' }}</el-tag>
              </div>
              <p>操作人：{{ item.operator || '--' }}</p>
              <p>备注：{{ item.remark || '无' }}</p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无流程记录" />
        </el-card>
      </template>

      <el-form v-else :model="form" label-position="top" class="form-grid-card">
        <el-form-item label="供应商">
          <el-select v-model="form.supplierId" placeholder="请选择供应商">
            <el-option v-for="item in suppliers" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品">
          <el-select v-model="form.productId" placeholder="请选择商品">
            <el-option v-for="item in products" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量"><el-input-number v-model="form.quantity" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="单价"><el-input-number v-model="form.unitPrice" :min="0" :precision="2" style="width: 100%" /></el-form-item>
      </el-form>

      <div class="dialog-footer">
        <el-button @click="router.back()">{{ isDetail ? '返回' : '取消' }}</el-button>
        <el-button v-if="!isDetail" v-permission="'purchase:edit'" type="primary" :loading="submitting" @click="handleSubmit">{{ isEdit ? '保存并重新提交审核' : '提交审核' }}</el-button>
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
import { fetchSuppliers } from '../../api/supplier'
import { createPurchase, fetchPurchaseDetail, fetchPurchaseTrace, updatePurchase } from '../../api/purchase'
import { statusLabelMap } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => route.name === 'purchase-order-edit')
const isDetail = computed(() => route.name === 'purchase-order-detail')
const pageTitle = computed(() => (isDetail.value ? '采购单详情' : (isEdit.value ? '编辑采购单' : '新建采购单')))
const pageDescription = computed(() => (isDetail.value ? '查看采购单明细、仓库信息与完整流程时间。' : '采用三步式向导，完成供应商选择、明细录入和审核提交。'))
const submitting = ref(false)
const products = ref([])
const suppliers = ref([])
const detailData = ref({})
const traceRows = ref([])
const form = reactive({ supplierId: undefined, productId: undefined, quantity: 10, unitPrice: 520 })

const displayValue = (value, fallback = '--') => {
  if (value === null || value === undefined || value === '') return fallback
  return value
}

const displayMoney = (value) => {
  if (value === null || value === undefined || value === '') return '--'
  const amount = Number(value)
  return Number.isNaN(amount) ? value : amount.toFixed(2)
}

const summaryCards = computed(() => ([
  { label: '订单号', value: displayValue(detailData.value.orderNo) },
  { label: '状态', value: displayValue(statusLabelMap[detailData.value.status] || detailData.value.status) },
  { label: '总额', value: displayMoney(detailData.value.totalAmount) },
  { label: '入库仓库', value: displayValue(detailData.value.warehouseName) }
]))

const detailFields = computed(() => ([
  { label: '订单号', value: displayValue(detailData.value.orderNo) },
  { label: '状态', value: displayValue(statusLabelMap[detailData.value.status] || detailData.value.status) },
  { label: '供应商', value: displayValue(detailData.value.supplierName) },
  { label: '商品', value: displayValue(detailData.value.productName) },
  { label: '数量', value: displayValue(detailData.value.quantity) },
  { label: '单价', value: displayMoney(detailData.value.unitPrice) },
  { label: '总额', value: displayMoney(detailData.value.totalAmount) },
  { label: '创建人', value: displayValue(detailData.value.createdBy) },
  { label: '创建时间', value: displayValue(detailData.value.createdAt) },
  { label: '审核人', value: displayValue(detailData.value.auditedBy) },
  { label: '审核时间', value: displayValue(detailData.value.auditedAt) },
  { label: '审核意见', value: displayValue(detailData.value.auditRemark, '无') },
  { label: '到货时间', value: displayValue(detailData.value.receivedAt) },
  { label: '入库时间', value: displayValue(detailData.value.inboundAt) },
  { label: '入库仓库', value: displayValue(detailData.value.warehouseName) },
  { label: '取消原因', value: displayValue(detailData.value.cancelReason, '无') }
]))

const loadDetail = async () => {
  if (!route.params.id) return
  const result = await fetchPurchaseDetail(route.params.id).catch(() => ({ data: null }))
  const data = result.data || {}
  detailData.value = data
  form.supplierId = data.supplierId
  form.productId = data.productId
  form.quantity = Number(data.quantity || 1)
  form.unitPrice = Number(data.unitPrice || 0)
}

const loadTrace = async () => {
  if (!route.params.id || !isDetail.value) return
  const result = await fetchPurchaseTrace(route.params.id).catch(() => ({ data: [] }))
  traceRows.value = result.data || []
}

const handleSubmit = async () => {
  if (!form.supplierId || !form.productId) {
    ElMessage.warning('请选择供应商和商品')
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updatePurchase(route.params.id, { ...form })
      ElMessage.success('采购单已按新配置重新提交审核')
    } else {
      await createPurchase({ ...form })
      ElMessage.success('采购单已提交审核')
    }
    router.push('/purchase/order')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const [productResult, supplierResult] = await Promise.all([
    fetchProducts().catch(() => ({ data: [] })),
    fetchSuppliers().catch(() => ({ data: [] }))
  ])
  products.value = productResult.data || []
  suppliers.value = supplierResult.data || []
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
