<template>
  <div class="stack">
    <DataPanel title="创建销售单" description="选择客户和商品后创建销售订单。">
      <template #actions>
        <button class="primary-btn" @click="submitSales">新增销售单</button>
      </template>
      <div class="form-grid">
        <label>
          <span>客户</span>
          <select v-model.number="form.customerId">
            <option v-for="item in customers" :key="item.id" :value="item.id">{{ item.name }}</option>
          </select>
        </label>
        <label>
          <span>商品</span>
          <select v-model.number="form.productId">
            <option v-for="item in products" :key="item.id" :value="item.id">{{ item.name }}</option>
          </select>
        </label>
        <label>
          <span>数量</span>
          <input v-model.number="form.quantity" type="number" min="1" />
        </label>
        <label>
          <span>单价</span>
          <input v-model.number="form.unitPrice" type="number" min="0" step="0.01" />
        </label>
      </div>
    </DataPanel>

    <DataPanel title="销售单列表" description="支持执行出库和回款登记。">
      <SimpleTable :columns="columns" :rows="rows" />
      <div class="action-list">
        <button v-for="item in outboundRows" :key="`out-${item.id}`" class="minor-btn" @click="handleOutbound(item.id)">出库 {{ item.orderNo }}</button>
        <button v-for="item in payableRows" :key="`pay-${item.id}`" class="minor-btn success" @click="handlePayment(item.id)">回款 {{ item.orderNo }}</button>
      </div>
    </DataPanel>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import DataPanel from '../components/DataPanel.vue'
import SimpleTable from '../components/SimpleTable.vue'
import { fetchProducts } from '../api/catalog'
import { fetchCustomers } from '../api/customer'
import { fetchSales, createSales, outboundSales, paymentSales } from '../api/sales'

const rows = ref([])
const products = ref([])
const customers = ref([])
const form = reactive({ customerId: 1, productId: 1, quantity: 5, unitPrice: 480 })

const columns = [
  { key: 'orderNo', label: '销售单号' },
  { key: 'customerName', label: '客户' },
  { key: 'productName', label: '商品' },
  { key: 'quantity', label: '数量' },
  { key: 'unitPrice', label: '单价' },
  { key: 'totalAmount', label: '总额' },
  { key: 'paidAmount', label: '已回款' },
  { key: 'status', label: '状态' },
  { key: 'createdAt', label: '创建时间' },
  { key: 'outboundAt', label: '出库时间' }
]

const outboundRows = computed(() => rows.value.filter(item => item.status === 'CREATED'))
const payableRows = computed(() => rows.value.filter(item => item.status !== 'PAID'))

const loadData = async () => {
  const [salesResult, productResult, customerResult] = await Promise.all([fetchSales(), fetchProducts(), fetchCustomers()])
  rows.value = salesResult.data || []
  products.value = productResult.data || []
  customers.value = customerResult.data || []
}

const submitSales = async () => {
  await createSales({ ...form })
  await loadData()
}

const handleOutbound = async (id) => {
  await outboundSales(id)
  await loadData()
}

const handlePayment = async (id) => {
  await paymentSales(id, { amount: 500, payerName: '演示付款方', remark: '前端快速登记' })
  await loadData()
}

onMounted(() => { loadData().catch(() => { rows.value = [] }) })
</script>
