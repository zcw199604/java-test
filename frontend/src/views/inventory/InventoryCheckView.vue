<template>
  <div class="page-stack">
    <PageSection title="库存盘点" description="支持批量编辑盘点数量并生成盈亏报告。">
      <el-table :data="rows" border stripe>
        <el-table-column prop="productName" label="商品" min-width="160" />
        <el-table-column prop="warehouseName" label="仓库" min-width="120" />
        <el-table-column prop="quantity" label="账面库存" min-width="120" />
        <el-table-column label="实盘数量" min-width="160">
          <template #default="scope">
            <el-input-number v-model="scope.row.actualQuantity" :min="0" />
          </template>
        </el-table-column>
      </el-table>
      <div class="dialog-footer">
        <el-button v-permission="'inventory:edit'" @click="handleReport">生成盈亏报告</el-button>
      </div>
      <el-card shadow="never" v-if="reportRows.length">
        <div class="tree-title">盈亏报告</div>
        <AppTable :columns="reportColumns" :rows="reportRows" :pagination="false" />
      </el-card>
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchInventories } from '../../api/inventory'

const rows = ref([])
const reportRows = ref([])
const reportColumns = [{ key: 'productName', label: '商品' }, { key: 'quantity', label: '账面' }, { key: 'actualQuantity', label: '实盘' }, { key: 'difference', label: '差异' }]

const handleReport = () => {
  reportRows.value = rows.value.map((item) => ({ ...item, difference: Number(item.actualQuantity || 0) - Number(item.quantity || 0) }))
}

onMounted(async () => {
  const result = await fetchInventories().catch(() => ({ data: [] }))
  rows.value = (result.data || []).map((item) => ({ ...item, actualQuantity: item.quantity }))
})
</script>
