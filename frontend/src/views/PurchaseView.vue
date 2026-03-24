<template>
  <div class="stack">
    <DataPanel title="创建采购单" description="录入供应商、商品、数量和单价，生成新的采购订单。">
      <template #actions>
        <button class="primary-btn" @click="submitPurchase">新增采购单</button>
      </template>
      <div class="form-grid">
        <label>
          <span>供应商</span>
          <select v-model.number="form.supplierId">
            <option v-for="item in suppliers" :key="item.id" :value="item.id">{{ item.name }}</option>
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

    <DataPanel title="采购单列表" description="支持查看当前采购单，并分步执行到货登记与采购入库。">
      <SimpleTable :columns="columns" :rows="rows" />
      <div class="action-list">
        <button v-for="item in receiveRows" :key="`receive-${item.id}`" class="minor-btn" @click="handleReceive(item.id)">
          到货 {{ item.orderNo }}
        </button>
        <button v-for="item in inboundRows" :key="`inbound-${item.id}`" class="minor-btn success" @click="handleInbound(item.id)">
          入库 {{ item.orderNo }}
        </button>
      </div>
    </DataPanel>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import DataPanel from '../components/DataPanel.vue'
import SimpleTable from '../components/SimpleTable.vue'
import { fetchPurchases, createPurchase, receivePurchase, inboundPurchase } from '../api/purchase'
import { fetchProducts } from '../api/catalog'
import { fetchSuppliers } from '../api/supplier'

const rows = ref([])
const products = ref([])
const suppliers = ref([])
const form = reactive({ supplierId: 1, productId: 1, quantity: 10, unitPrice: 200 })

const columns = [
  { key: 'orderNo', label: '采购单号' },
  { key: 'supplierName', label: '供应商' },
  { key: 'productName', label: '商品' },
  { key: 'quantity', label: '数量' },
  { key: 'unitPrice', label: '单价' },
  { key: 'totalAmount', label: '总额' },
  { key: 'status', label: '状态' },
  { key: 'createdAt', label: '创建时间' },
  { key: 'receivedAt', label: '到货时间' },
  { key: 'inboundAt', label: '入库时间' }
]

const receiveRows = computed(() => rows.value.filter(item => item.status === 'CREATED'))
const inboundRows = computed(() => rows.value.filter(item => item.status === 'RECEIVED'))

const loadData = async () => {
  const [purchaseResult, productResult, supplierResult] = await Promise.all([
    fetchPurchases(),
    fetchProducts(),
    fetchSuppliers()
  ])
  rows.value = purchaseResult.data || []
  products.value = productResult.data || []
  suppliers.value = supplierResult.data || []
}

const submitPurchase = async () => {
  await createPurchase({ ...form })
  await loadData()
}

const handleReceive = async (id) => {
  await receivePurchase(id)
  await loadData()
}

const handleInbound = async (id) => {
  await inboundPurchase(id)
  await loadData()
}

onMounted(() => {
  loadData().catch(() => {
    rows.value = []
  })
})
</script>
