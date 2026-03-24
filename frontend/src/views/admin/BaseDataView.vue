<template>
  <div class="page-stack">
    <PageSection title="基础资料" description="按品类、供应商、客户、仓库和价格分类维护基础数据。">
      <el-tabs>
        <el-tab-pane label="品类"><AppTable :columns="productColumns" :rows="products" /></el-tab-pane>
        <el-tab-pane label="供应商"><AppTable :columns="supplierColumns" :rows="suppliers" /></el-tab-pane>
        <el-tab-pane label="客户"><AppTable :columns="customerColumns" :rows="customers" /></el-tab-pane>
        <el-tab-pane label="仓库"><AppTable :columns="warehouseColumns" :rows="warehouses" :pagination="false" /></el-tab-pane>
        <el-tab-pane label="价格"><AppTable :columns="priceColumns" :rows="products" /></el-tab-pane>
      </el-tabs>
    </PageSection>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchProducts } from '../../api/catalog'
import { fetchSuppliers } from '../../api/supplier'
import { fetchCustomers } from '../../api/customer'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'

const products = ref([])
const suppliers = ref([])
const customers = ref([])
const warehouses = ref([{ name: '杭州中心仓', manager: '库管A', status: '启用' }, { name: '绍兴分仓', manager: '库管B', status: '启用' }])
const productColumns = [{ key: 'code', label: '编码' }, { key: 'name', label: '名称' }, { key: 'category', label: '品类' }, { key: 'status', label: '状态' }]
const supplierColumns = [{ key: 'name', label: '供应商' }, { key: 'contactName', label: '联系人' }, { key: 'contactPhone', label: '电话' }]
const customerColumns = [{ key: 'name', label: '客户' }, { key: 'contactName', label: '联系人' }, { key: 'contactPhone', label: '电话' }]
const warehouseColumns = [{ key: 'name', label: '仓库' }, { key: 'manager', label: '负责人' }, { key: 'status', label: '状态' }]
const priceColumns = [{ key: 'name', label: '商品' }, { key: 'unitPrice', label: '单价' }, { key: 'warningThreshold', label: '预警阈值' }]

onMounted(async () => {
  const [productResult, supplierResult, customerResult] = await Promise.all([
    fetchProducts().catch(() => ({ data: [] })),
    fetchSuppliers().catch(() => ({ data: [] })),
    fetchCustomers().catch(() => ({ data: [] }))
  ])
  products.value = productResult.data || []
  suppliers.value = supplierResult.data || []
  customers.value = customerResult.data || []
})
</script>
