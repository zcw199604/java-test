<template>
  <div class="page-stack">
    <PageSection title="合规追溯" description="按订单号、商品、仓库和节点类型检索采购、销售、调拨、盘点统一链路。">
      <div class="toolbar-grid four">
        <el-input v-model="keyword" placeholder="请输入订单号、商品名、仓库或备注" clearable />
        <el-select v-model="warehouseId" placeholder="仓库筛选" clearable>
          <el-option label="全部仓库" :value="null" />
          <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-select v-model="bizType" placeholder="业务类型" clearable>
          <el-option label="采购" value="PURCHASE" />
          <el-option label="销售" value="SALES" />
          <el-option label="库存" value="INVENTORY" />
        </el-select>
        <el-select v-model="nodeCode" placeholder="节点类型" clearable>
          <el-option label="创建" value="CREATE" />
          <el-option label="审核" value="AUDIT" />
          <el-option label="到货" value="RECEIVE" />
          <el-option label="采购入库" value="INBOUND" />
          <el-option label="销售出库" value="OUTBOUND" />
          <el-option label="销售回款" value="PAYMENT" />
          <el-option label="库存调拨" value="TRANSFER" />
          <el-option label="库存盘点" value="CHECK" />
          <el-option label="调拨入库" value="TRANSFER_IN" />
          <el-option label="调拨出库" value="TRANSFER_OUT" />
          <el-option label="采购入库流水" value="PURCHASE_INBOUND" />
          <el-option label="销售出库流水" value="SALES_OUTBOUND" />
        </el-select>
      </div>
      <div class="dialog-footer">
        <el-button @click="loadData">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      <el-timeline>
        <el-timeline-item v-for="item in traceRows" :key="item.id" :timestamp="item.createdAt">
          <div class="trace-node-grid single">
            <el-card shadow="hover">
              <div class="trace-header">
                <strong>{{ item.nodeName || item.nodeCode }}</strong>
                <el-tag size="small" :type="item.sourceType === 'INVENTORY' ? 'warning' : 'primary'">
                  {{ item.sourceType === 'INVENTORY' ? '库存变化' : '业务节点' }}
                </el-tag>
              </div>
              <p>业务：{{ bizTypeLabel(item.bizType) }} / 订单号：{{ item.orderNo || '--' }}</p>
              <p>商品：{{ item.productName || '--' }} / 仓库：{{ item.warehouseName || '--' }}</p>
              <p v-if="item.fromWarehouseName || item.toWarehouseName">调拨：{{ item.fromWarehouseName || '--' }} → {{ item.toWarehouseName || '--' }}</p>
              <p v-if="item.changeQty !== null && item.changeQty !== undefined">库存变化：{{ item.beforeQty ?? '--' }} → {{ item.afterQty ?? '--' }}（{{ item.changeQty > 0 ? '+' : '' }}{{ item.changeQty }}）</p>
              <p>操作人：{{ item.operator || '--' }}</p>
              <p>备注：{{ item.remark || '无' }}</p>
            </el-card>
          </div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-if="!traceRows.length" description="暂无符合条件的追溯记录" />
    </PageSection>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import PageSection from '../../components/PageSection.vue'
import { fetchComplianceTrace } from '../../api/report'
import { fetchWarehouses } from '../../api/system'

const keyword = ref('')
const warehouseId = ref(null)
const bizType = ref('')
const nodeCode = ref('')
const traceRows = ref([])
const warehouseOptions = ref([])

const bizTypeLabel = (value) => ({ PURCHASE: '采购', SALES: '销售', INVENTORY: '库存' }[value] || value || '--')

const loadWarehouses = async () => {
  const result = await fetchWarehouses().catch(() => ({ data: [] }))
  warehouseOptions.value = (result.data || []).filter((item) => item.status !== 'DISABLED')
}

const loadData = async () => {
  const result = await fetchComplianceTrace({
    keyword: keyword.value || undefined,
    warehouseId: warehouseId.value || undefined,
    bizType: bizType.value || undefined,
    nodeCode: nodeCode.value || undefined
  }).catch(() => ({ data: [] }))
  traceRows.value = result.data || []
}

const resetFilters = async () => {
  keyword.value = ''
  warehouseId.value = null
  bizType.value = ''
  nodeCode.value = ''
  await loadData()
}

onMounted(async () => {
  await Promise.all([loadWarehouses(), loadData()])
})
</script>

<style scoped>
.trace-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.single {
  grid-template-columns: 1fr;
}
</style>
