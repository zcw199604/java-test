<template>
  <div class="page-stack">
    <PageSection title="库存追溯台账" description="按时间轴展示库存变化轨迹和业务链路。">
      <div class="toolbar">
        <el-select v-model="warehouseId" placeholder="仓库筛选" clearable class="filter-item" @change="loadData">
          <el-option label="全部仓库" :value="null" />
          <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-button @click="loadData">查询</el-button>
      </div>
      <el-timeline>
        <el-timeline-item v-for="item in rows" :key="item.id || item.createdAt" :timestamp="item.createdAt">
          <div class="ledger-title">{{ item.productName }} · {{ statusLabelMap[item.bizType] || item.bizType }}</div>
          <p>
            仓库：{{ item.warehouseName || '全部仓库' }}；
            库存由 {{ item.beforeQty }} 变更至 {{ item.afterQty }}；
            操作人：{{ item.operatorName || '系统' }}；
            备注：{{ item.remark || '无' }}
          </p>
        </el-timeline-item>
      </el-timeline>
    </PageSection>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { statusLabelMap } from '../../utils/format'
import PageSection from '../../components/PageSection.vue'
import { fetchInventoryRecords } from '../../api/inventory'
import { fetchWarehouses } from '../../api/system'

const rows = ref([])
const warehouseOptions = ref([])
const warehouseId = ref(null)

const loadData = async () => {
  const result = await fetchInventoryRecords({ warehouseId: warehouseId.value || undefined }).catch(() => ({ data: [] }))
  rows.value = result.data || []
}

const loadWarehouses = async () => {
  const result = await fetchWarehouses().catch(() => ({ data: [] }))
  warehouseOptions.value = (result.data || []).filter((item) => item.status !== 'DISABLED')
}

onMounted(async () => {
  await Promise.all([loadWarehouses(), loadData()])
})
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-item { width: 240px; }
</style>
