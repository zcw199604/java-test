<template>
  <div class="page-stack">
    <PageSection title="销售出库" description="仅管理员、超级管理员或库管可对已审核通过的销售单执行出库确认并自动扣减库存。">
      <div class="toolbar-grid two">
        <el-select v-model="selectedId" placeholder="选择待出库销售单">
          <el-option v-for="item in outboundRows" :key="item.id" :label="`${item.orderNo} / ${item.customerName}`" :value="item.id" />
        </el-select>
        <el-input v-model="deliveryInfo" placeholder="请输入配送信息" />
      </div>
      <AppTable :columns="columns" :rows="outboundRows" :pagination="false" />
      <div class="dialog-footer">
        <el-button v-permission="['sale:edit', 'inventory:edit']" type="primary" :disabled="!selectedId" @click="handleOutbound">确认出库</el-button>
      </div>
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchSales, outboundSales } from '../../api/sales'

const rows = ref([])
const selectedId = ref(null)
const deliveryInfo = ref('杭州主城区当日配送')
const columns = [{ key: 'orderNo', label: '订单号' }, { key: 'customerName', label: '客户' }, { key: 'productName', label: '商品' }, { key: 'quantity', label: '数量' }]
const outboundRows = computed(() => rows.value.filter((item) => item.status === 'APPROVED'))

const loadData = async () => {
  const result = await fetchSales().catch(() => ({ data: [] }))
  rows.value = result.data || []
  selectedId.value = outboundRows.value[0]?.id || null
}

const handleOutbound = async () => {
  await outboundSales(selectedId.value)
  ElMessage.success(`已完成出库并登记配送信息：${deliveryInfo.value}`)
  loadData()
}

onMounted(loadData)
</script>
