<template>
  <div class="page-stack">
    <PageSection title="合规追溯" description="支持按订单号、商品批次检索采销存全流程链路。">
      <div class="toolbar-grid two">
        <el-input v-model="keyword" placeholder="请输入订单号、商品名或批次号" clearable />
        <el-button type="primary" @click="buildTrace">开始追溯</el-button>
      </div>
      <el-steps :active="traceRows.length ? traceRows.length : 0" align-center>
        <el-step v-for="item in traceRows" :key="item.title" :title="item.title" :description="item.description" />
      </el-steps>
      <el-dialog v-model="dialogVisible" title="节点明细" width="520px">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="节点">{{ activeNode.title }}</el-descriptions-item>
          <el-descriptions-item label="说明">{{ activeNode.description }}</el-descriptions-item>
        </el-descriptions>
      </el-dialog>
      <div class="trace-node-grid">
        <el-card v-for="item in traceRows" :key="item.title" shadow="hover" @click="showNode(item)">
          <strong>{{ item.title }}</strong>
          <p>{{ item.description }}</p>
        </el-card>
      </div>
    </PageSection>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import PageSection from '../../components/PageSection.vue'

const keyword = ref('YC001')
const dialogVisible = ref(false)
const activeNode = reactive({ title: '', description: '' })
const traceRows = ref([])

const buildTrace = () => {
  traceRows.value = [
    { title: '采购下单', description: `根据 ${keyword.value} 生成采购订单并触发审批` },
    { title: '到货入库', description: '仓库完成批次验收，库存同步入账' },
    { title: '销售出库', description: '销售订单核对库存后生成出库记录' },
    { title: '门店签收', description: '配送完成后形成最终合规留痕' }
  ]
}

const showNode = (item) => {
  Object.assign(activeNode, item)
  dialogVisible.value = true
}
</script>
