<template>
  <div class="page-stack">
    <PageSection :title="isEdit ? '编辑销售单' : '新建销售单'" description="采用步骤条录入客户、商品和数量，并实时校验库存阈值。">
      <el-steps :active="2" simple finish-status="success">
        <el-step title="选择客户" />
        <el-step title="录入商品" />
        <el-step title="确认出库策略" />
      </el-steps>
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
      <div class="dialog-footer">
        <el-button @click="router.back()">取消</el-button>
        <el-button v-permission="'sale:edit'" type="primary" @click="handleSubmit">保存订单</el-button>
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
import { createSales } from '../../api/sales'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => Boolean(route.params.id))
const products = ref([])
const customers = ref([])
const inventories = ref([])
const form = reactive({ customerId: 1, productId: 1, quantity: 5, unitPrice: 480 })

const lowStockWarning = computed(() => {
  const current = inventories.value.find((item) => item.productId === form.productId || item.productName === products.value.find((p) => p.id === form.productId)?.name)
  return current && Number(current.quantity) <= Number(current.warningThreshold)
})

const handleSubmit = async () => {
  await createSales({ ...form })
  ElMessage.success(isEdit.value ? '销售单已重新保存' : '销售单已创建')
  router.push('/sale/order')
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
})
</script>
