<template>
  <div class="stack">
    <DataPanel title="库存动作登记" description="支持盘点和调拨的简化登记，提交后会自动生成库存流水。">
      <template #actions>
        <button class="primary-btn" @click="submitAction">提交动作</button>
      </template>
      <div class="form-grid">
        <label>
          <span>动作类型</span>
          <select v-model="form.actionType">
            <option value="CHECK">盘点</option>
            <option value="TRANSFER">调拨登记</option>
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
          <span>备注</span>
          <input v-model="form.remark" placeholder="请输入备注" />
        </label>
      </div>
    </DataPanel>

    <DataPanel title="库存台账" description="查看当前库存与预警阈值。">
      <SimpleTable :columns="inventoryColumns" :rows="inventories" />
    </DataPanel>

    <DataPanel title="库存预警" description="数量低于阈值的商品会显示在此。">
      <SimpleTable :columns="inventoryColumns" :rows="warnings" />
    </DataPanel>

    <DataPanel title="库存流水" description="展示采购入库、销售出库、盘点和调拨等记录。">
      <SimpleTable :columns="recordColumns" :rows="records" />
    </DataPanel>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import DataPanel from '../components/DataPanel.vue'
import SimpleTable from '../components/SimpleTable.vue'
import { fetchProducts } from '../api/catalog'
import { fetchInventories, fetchInventoryRecords, fetchInventoryWarnings, createInventoryAction } from '../api/inventory'

const products = ref([])
const inventories = ref([])
const warnings = ref([])
const records = ref([])
const form = reactive({ actionType: 'CHECK', productId: 1, quantity: 20, remark: '前端快速登记' })

const inventoryColumns = [
  { key: 'productName', label: '商品名称' },
  { key: 'warehouseName', label: '仓库' },
  { key: 'quantity', label: '库存数量' },
  { key: 'warningThreshold', label: '预警阈值' },
  { key: 'updatedAt', label: '更新时间' }
]

const recordColumns = [
  { key: 'productName', label: '商品' },
  { key: 'bizType', label: '类型' },
  { key: 'changeQty', label: '变动量' },
  { key: 'beforeQty', label: '变动前' },
  { key: 'afterQty', label: '变动后' },
  { key: 'remark', label: '备注' },
  { key: 'createdAt', label: '时间' }
]

const loadData = async () => {
  const [productResult, inventoryResult, recordResult, warningResult] = await Promise.all([
    fetchProducts(),
    fetchInventories(),
    fetchInventoryRecords(),
    fetchInventoryWarnings()
  ])
  products.value = productResult.data || []
  inventories.value = inventoryResult.data || []
  records.value = recordResult.data || []
  warnings.value = warningResult.data || []
}

const submitAction = async () => {
  await createInventoryAction({ ...form })
  await loadData()
}

onMounted(() => { loadData().catch(() => { inventories.value = [] }) })
</script>
