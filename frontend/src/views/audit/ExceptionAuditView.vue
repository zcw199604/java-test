<template>
  <div class="page-stack">
    <PageSection title="异常审核" description="仅超级管理员可见，用于审核异常库存、异常出库和可疑单据。">
      <AppTable :columns="columns" :rows="rows">
        <template #status="{ value }"><el-tag :type="value === 'APPROVED' ? 'success' : value === 'REJECTED' ? 'danger' : 'warning'">{{ value === 'APPROVED' ? '已通过' : value === 'REJECTED' ? '已驳回' : '待审核' }}</el-tag></template>
        <template #actions="{ row }">
          <el-button v-permission="'audit:process'" link type="success" @click="openDialog(row, 'APPROVED')" v-if="row.status === 'PENDING'">通过</el-button>
          <el-button v-permission="'audit:process'" link type="danger" @click="openDialog(row, 'REJECTED')" v-if="row.status === 'PENDING'">驳回</el-button>
        </template>
      </AppTable>
    </PageSection>

    <el-dialog v-model="dialogVisible" title="审核意见" width="520px">
      <el-form label-position="top">
        <el-form-item label="审核动作"><el-input :model-value="actionLabel" disabled /></el-form-item>
        <el-form-item label="意见"><el-input v-model="remark" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchAbnormalDocs, auditAbnormalDoc } from '../../api/report'

const rows = ref([])
const columns = [{ key: 'orderNo', label: '异常单号' }, { key: 'abnormalType', label: '异常类型' }, { key: 'reportedBy', label: '提交人' }, { key: 'status', label: '状态' }]
const dialogVisible = ref(false)
const actionLabel = ref('')
const remark = ref('')
const currentRow = ref(null)
const currentDecision = ref('')

const openDialog = (row, decision) => {
  currentRow.value = row
  currentDecision.value = decision
  actionLabel.value = `${decision === 'APPROVED' ? '通过' : '驳回'} ${row.orderNo}`
  remark.value = ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await auditAbnormalDoc(currentRow.value.id, { decision: currentDecision.value, remark: remark.value })
    ElMessage.success('审核结果已提交')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('提交失败: ' + (e.message || '未知错误'))
  }
}

const loadData = async () => {
  const result = await fetchAbnormalDocs().catch(() => ({ data: [] }))
  rows.value = result.data || []
}

onMounted(loadData)
</script>
