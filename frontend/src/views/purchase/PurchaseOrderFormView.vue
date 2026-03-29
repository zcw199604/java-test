<template>
  <div class="page-stack">
    <PageSection :title="isEdit ? '编辑采购单' : '新建采购单'" description="采用三步式向导，完成供应商选择、明细录入和审核提交。">
      <el-steps :active="2" finish-status="success" simple>
        <el-step title="选择供应商" />
        <el-step title="录入明细" />
        <el-step title="提交审核" />
      </el-steps>
      <el-form :model="form" label-position="top" class="form-grid-card">
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
        <el-button @click="router.back()">取消</el-button>
        <el-button v-permission="'purchase:edit'" type="primary" :loading="submitting" @click="handleSubmit">{{ isEdit ? '保存并重新提交审核' : '提交审核' }}</el-button>
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
import { createPurchase, fetchPurchaseDetail, updatePurchase } from '../../api/purchase'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => Boolean(route.params.id))
const submitting = ref(false)
const products = ref([])
const suppliers = ref([])
const form = reactive({ supplierId: undefined, productId: undefined, quantity: 10, unitPrice: 520 })

const loadDetail = async () => {
  if (!isEdit.value) return
  const result = await fetchPurchaseDetail(route.params.id).catch(() => ({ data: null }))
  const data = result.data || {}
  form.supplierId = data.supplierId
  form.productId = data.productId
  form.quantity = Number(data.quantity || 1)
  form.unitPrice = Number(data.unitPrice || 0)
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
  await loadDetail()
})
</script>
