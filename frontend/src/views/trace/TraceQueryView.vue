<template>
  <div class="page-stack">
    <PageSection title="合规追溯" description="支持按订单号、商品批次检索采销存全流程链路。">
      <div class="toolbar-grid two">
        <el-input v-model="keyword" placeholder="请输入订单号、商品名或批次号" clearable />
        <el-button type="primary" @click="buildTrace">开始追溯</el-button>
      </div>
      <el-steps :active="traceRows.length ? traceRows.length : 0" align-center>
        <el-step v-for="item in traceRows" :key="item.id" :title="item.nodeName || item.nodeCode" :description="item.remark" />
      </el-steps>
      <div class="trace-node-grid">
        <el-card v-for="item in traceRows" :key="item.id" shadow="hover">
          <strong>{{ item.orderNo }}</strong>
          <p>{{ item.nodeName || item.nodeCode }} / {{ item.operator }}</p>
          <span>{{ item.createdAt }}</span>
        </el-card>
      </div>
    </PageSection>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import PageSection from '../../components/PageSection.vue'
import { fetchComplianceTrace } from '../../api/report'

const keyword = ref('')
const traceRows = ref([])

const buildTrace = async () => {
  const result = await fetchComplianceTrace().catch(() => ({ data: [] }))
  const rows = result.data || []
  traceRows.value = keyword.value
    ? rows.filter((item) => JSON.stringify(item).includes(keyword.value))
    : rows
}
</script>
