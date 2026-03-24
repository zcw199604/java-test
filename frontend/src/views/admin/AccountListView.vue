<template>
  <div class="page-stack">
    <PageSection title="账号管理" description="统一管理系统账号、角色、指派范围和启停用状态。">
      <template #actions>
        <el-button v-permission="'admin:account:view'" type="primary" @click="dialogVisible = true">新增账号</el-button>
      </template>
      <div class="toolbar-grid">
        <el-input v-model="keyword" placeholder="搜索账号/姓名/角色" clearable />
      </div>
      <AppTable :columns="columns" :rows="filteredRows">
        <template #status="{ value }">
          <el-tag :type="value === 'ENABLED' ? 'success' : 'danger'">{{ value === 'ENABLED' ? '启用' : '停用' }}</el-tag>
        </template>
      </AppTable>
    </PageSection>

    <el-dialog v-model="dialogVisible" title="新增账号" width="520px">
      <el-form :model="form" label-position="top">
        <el-form-item label="账号"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="初始密码"><el-input v-model="form.password" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleCode" style="width: 100%">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="采购专员" value="PURCHASER" />
            <el-option label="销售专员" value="SELLER" />
            <el-option label="库管人员" value="KEEPER" />
          </el-select>
        </el-form-item>
        <el-form-item label="指派范围"><el-input v-model="form.scopeValue" placeholder="如 WH-001 / ALL" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createUser, fetchUsers } from '../../api/system'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'

const keyword = ref('')
const rows = ref([])
const dialogVisible = ref(false)
const form = reactive({ username: '', password: '123456', realName: '', roleCode: 'PURCHASER', scopeType: 'WAREHOUSE', scopeValue: 'WH-001' })
const columns = [
  { key: 'username', label: '账号' },
  { key: 'realName', label: '姓名' },
  { key: 'roleName', label: '角色' },
  { key: 'status', label: '状态', slot: 'status' },
  { key: 'createdAt', label: '创建时间' }
]

const filteredRows = computed(() => rows.value.filter((item) => !keyword.value || JSON.stringify(item).includes(keyword.value)))

const loadData = async () => {
  const result = await fetchUsers().catch(() => ({ data: [] }))
  rows.value = result.data || []
}

const handleSave = async () => {
  await createUser(form)
  ElMessage.success('账号已创建')
  dialogVisible.value = false
  Object.assign(form, { username: '', password: '123456', realName: '', roleCode: 'PURCHASER', scopeType: 'WAREHOUSE', scopeValue: 'WH-001' })
  await loadData()
}

onMounted(() => {
  loadData().catch(() => undefined)
})
</script>
