<template>
  <div class="page-stack">
    <PageSection title="库存流水" description="按入库、出库、调拨分类展示，并支持独立登记表单。">
      <el-tabs>
        <el-tab-pane label="入库"><AppTable :columns="columns" :rows="inboundRows" /></el-tab-pane>
        <el-tab-pane label="出库"><AppTable :columns="columns" :rows="outboundRows" /></el-tab-pane>
        <el-tab-pane label="调拨">
          <el-form :model="form" label-position="top" class="form-grid-card">
            <el-form-item label="商品编码"><el-input v-model="form.productCode" /></el-form-item>
            <el-form-item label="调拨数量"><el-input-number v-model="form.quantity" :min="1" style="width: 100%" /></el-form-item>
            <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
          </el-form>
          <div class="dialog-footer"><el-button v-permission="'inventory:edit'" type="primary" @click="handleSubmit">提交调拨</el-button></div>
        </el-tab-pane>
      </el-tabs>
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchInventoryRecords, createInventoryAction } from '../../api/inventory'

const rows = ref([])
const form = reactive({ productCode: 'YC001', quantity: 10, remark: '门店间调拨', actionType: 'TRANSFER', productId: 1 })
const columns = [{ key: 'productName', label: '商品' }, { key: 'bizType', label: '类型' }, { key: 'changeQty', label: '变动量' }, { key: 'afterQty', label: '结果库存' }, { key: 'createdAt', label: '时间', minWidth: 180 }]
const inboundRows = computed(() => rows.value.filter((item) => ['PURCHASE', 'CHECK'].includes(item.bizType)))
const outboundRows = computed(() => rows.value.filter((item) => ['SALE', 'OUTBOUND'].includes(item.bizType)))

const loadData = async () => {
  const result = await fetchInventoryRecords().catch(() => ({ data: [] }))
  rows.value = result.data || []
}

const handleSubmit = async () => {
  await createInventoryAction({ ...form })
  ElMessage.success('调拨登记成功')
  loadData()
}

onMounted(loadData)
</script>
