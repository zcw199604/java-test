<template>
  <div class="page-stack">
    <PageSection title="采购入库" description="由管理员、超级管理员或库管确认采购到货后的最终入库，库存将同步更新。">
      <div class="toolbar-grid two">
        <el-select v-model="selectedId" placeholder="选择待入库订单">
          <el-option v-for="item in receivableRows" :key="item.id" :label="`${item.orderNo} / ${item.productName}`" :value="item.id" />
        </el-select>
        <el-input v-model="batchNo" placeholder="请输入批次号，例如 20260324-A" />
      </div>
      <AppTable :columns="columns" :rows="receivableRows" :pagination="false">
        <template #status="{ value }"><el-tag :type="statusTypeMap[value] || 'warning'">{{ statusLabelMap[value] || value }}</el-tag></template>
      </AppTable>
      <div class="dialog-footer">
        <el-button v-permission="['purchase:edit', 'inventory:edit']" type="primary" :disabled="!selectedId" @click="handleInbound">确认入库</el-button>
      </div>
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchPurchases, inboundPurchase } from '../../api/purchase'
import { statusLabelMap, statusTypeMap } from '../../utils/format'

const rows = ref([])
const selectedId = ref(null)
const batchNo = ref('20260324-A')
const columns = [{ key: 'orderNo', label: '订单号' }, { key: 'productName', label: '商品' }, { key: 'quantity', label: '数量' }, { key: 'status', label: '状态' }]
const receivableRows = computed(() => rows.value.filter((item) => item.status === 'RECEIVED'))

const loadData = async () => {
  const result = await fetchPurchases().catch(() => ({ data: [] }))
  rows.value = result.data || []
  selectedId.value = receivableRows.value[0]?.id || null
}

const handleInbound = async () => {
  await inboundPurchase(selectedId.value)
  ElMessage.success(`批次 ${batchNo.value} 入库完成，库存已同步`)
  loadData()
}

onMounted(loadData)
</script>
