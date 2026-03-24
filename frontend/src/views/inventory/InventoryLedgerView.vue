<template>
  <div class="page-stack">
    <PageSection title="库存追溯台账" description="按时间轴展示库存变化轨迹和业务链路。">
      <el-timeline>
        <el-timeline-item v-for="item in rows" :key="item.id || item.createdAt" :timestamp="item.createdAt">
          <div class="ledger-title">{{ item.productName }} · {{ item.bizType }}</div>
          <p>库存由 {{ item.beforeQty }} 变更至 {{ item.afterQty }}，备注：{{ item.remark || '无' }}</p>
        </el-timeline-item>
      </el-timeline>
    </PageSection>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import PageSection from '../../components/PageSection.vue'
import { fetchInventoryRecords } from '../../api/inventory'

const rows = ref([])

onMounted(async () => {
  const result = await fetchInventoryRecords().catch(() => ({ data: [] }))
  rows.value = result.data || []
})
</script>
