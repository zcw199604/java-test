<template>
  <div class="page-stack">
    <PageSection title="异常审核" description="仅超级管理员可见，用于审核异常库存、异常出库和可疑单据。">
      <AppTable :columns="columns" :rows="rows">
        <template #actions="{ row }">
          <el-button v-permission="'audit:view'" link type="success" @click="openDialog(row, '通过')">通过</el-button>
          <el-button v-permission="'audit:view'" link type="danger" @click="openDialog(row, '驳回')">驳回</el-button>
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
import { fetchAbnormalDocs } from '../../api/report'

const rows = ref([])
const columns = [{ key: 'orderNo', label: '异常单号' }, { key: 'abnormalType', label: '异常类型' }, { key: 'reportedBy', label: '提交人' }, { key: 'status', label: '状态' }]
const dialogVisible = ref(false)
const actionLabel = ref('')
const remark = ref('')

const openDialog = (row, action) => {
  actionLabel.value = `${action} ${row.orderNo}`
  dialogVisible.value = true
}

const handleSubmit = () => {
  dialogVisible.value = false
  ElMessage.success('异常审核结果已提交（当前后端仅提供查询能力）')
}

onMounted(async () => {
  const result = await fetchAbnormalDocs().catch(() => ({ data: [] }))
  rows.value = result.data || []
})
</script>
