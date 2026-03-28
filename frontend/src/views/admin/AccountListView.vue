<template>
  <div class="page-stack">
    <PageSection title="账号管理" description="统一管理系统账号、角色、指派范围和启停用状态。">
      <template #actions>
        <el-button v-permission="'admin:account:view'" type="primary" @click="openCreateDialog">新增账号</el-button>
      </template>

      <div class="toolbar-grid">
        <el-input v-model="keyword" placeholder="搜索账号/姓名/角色/范围" clearable @keyup.enter="loadData" />
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable>
          <el-option label="启用" value="ENABLED" />
          <el-option label="停用" value="DISABLED" />
        </el-select>
        <el-button type="primary" @click="loadData">刷新</el-button>
      </div>

      <AppTable :columns="columns" :rows="filteredRows" :loading="loading" empty-text="暂无账号数据">
        <template #status="{ value }">
          <el-tag :type="value === 'ENABLED' ? 'success' : 'danger'">{{ value === 'ENABLED' ? '启用' : '停用' }}</el-tag>
        </template>
        <template #scopeValue="{ row }">
          <span>{{ row.scopeType ? `${row.scopeType}:${row.scopeValue || 'ALL'}` : '--' }}</span>
        </template>
        <template #actions="{ row }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button link :type="row.status === 'ENABLED' ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 'ENABLED' ? '停用' : '启用' }}
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </AppTable>
    </PageSection>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑账号' : '新增账号'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="isEdit ? '重置密码（留空则不修改）' : '初始密码'" :prop="isEdit ? '' : 'password'">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="form.roleCode" style="width: 100%">
            <el-option v-for="item in roles" :key="item.code" :label="item.name" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="ENABLED">启用</el-radio>
            <el-radio label="DISABLED">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="数据范围类型">
          <el-select v-model="form.scopeType" style="width: 100%" clearable>
            <el-option label="全部" value="ALL" />
            <el-option label="仓库" value="WAREHOUSE" />
            <el-option label="部门" value="DEPARTMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="指派范围值">
          <el-input v-model="form.scopeValue" placeholder="如 ALL / WH-001 / 采购部" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createUser, deleteUser, fetchUserDetail, fetchUsers, fetchRoles, updateUser, updateUserStatus } from '../../api/system'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'

const loading = ref(false)
const submitting = ref(false)
const keyword = ref('')
const statusFilter = ref('')
const rows = ref([])
const roles = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref()

const createEmptyForm = () => ({
  username: '',
  password: '123456',
  realName: '',
  roleCode: 'PURCHASER',
  status: 'ENABLED',
  scopeType: 'WAREHOUSE',
  scopeValue: 'WH-001'
})

const form = reactive(createEmptyForm())

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请选择角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const columns = [
  { key: 'username', label: '账号', minWidth: 120 },
  { key: 'realName', label: '姓名', minWidth: 120 },
  { key: 'roleName', label: '角色', minWidth: 140 },
  { key: 'scopeValue', label: '数据范围', minWidth: 160 },
  { key: 'status', label: '状态', width: 100 },
  { key: 'createdAt', label: '创建时间', minWidth: 180 }
]

const filteredRows = computed(() => rows.value.filter((item) => {
  const matchKeyword = !keyword.value || JSON.stringify(item).includes(keyword.value)
  const matchStatus = !statusFilter.value || item.status === statusFilter.value
  return matchKeyword && matchStatus
}))

const resetForm = () => {
  Object.assign(form, createEmptyForm())
}

const loadData = async () => {
  loading.value = true
  try {
    const [userResult, roleResult] = await Promise.all([
      fetchUsers().catch(() => ({ data: [] })),
      fetchRoles().catch(() => ({ data: [] }))
    ])
    rows.value = userResult.data || []
    roles.value = roleResult.data || []
    if (!roles.value.find((item) => item.code === form.roleCode) && roles.value.length > 0) {
      form.roleCode = roles.value[0].code
    }
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  isEdit.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = async (row) => {
  isEdit.value = true
  editingId.value = row.id
  resetForm()
  form.password = ''
  try {
    const result = await fetchUserDetail(row.id)
    const detail = result.data || {}
    Object.assign(form, {
      username: detail.username || row.username,
      password: '',
      realName: detail.realName || row.realName,
      roleCode: detail.roleCode || row.roleCode,
      status: detail.status || row.status,
      scopeType: detail.scopeType || row.scopeType || 'WAREHOUSE',
      scopeValue: detail.scopeValue || row.scopeValue || 'ALL'
    })
    dialogVisible.value = true
  } catch (error) {
    // 统一拦截器处理
  }
}

const handleSave = async () => {
  if (!isEdit.value) {
    await formRef.value?.validate()
  } else {
    await formRef.value?.validateField(['realName', 'roleCode', 'status'])
  }
  submitting.value = true
  try {
    const payload = {
      username: form.username,
      password: form.password,
      realName: form.realName,
      roleCode: form.roleCode,
      status: form.status,
      scopeType: form.scopeType,
      scopeValue: form.scopeValue
    }
    if (isEdit.value && editingId.value) {
      await updateUser(editingId.value, payload)
      ElMessage.success('账号信息已更新')
    } else {
      await createUser(payload)
      ElMessage.success('账号已创建')
    }
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    // 统一拦截器处理
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row) => {
  const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  await ElMessageBox.confirm(`确认将账号“${row.username}”设为${nextStatus === 'ENABLED' ? '启用' : '停用'}吗？`, '更新账号状态', { type: 'warning' })
  try {
    await updateUserStatus(row.id, { status: nextStatus })
    ElMessage.success(`账号已${nextStatus === 'ENABLED' ? '启用' : '停用'}`)
    await loadData()
  } catch (error) {
    // 统一拦截器处理
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认永久删除账号“${row.username}”吗？此操作不可恢复。`, '删除账号', { type: 'warning' })
  try {
    await deleteUser(row.id)
    ElMessage.success('账号已删除')
    await loadData()
  } catch (error) {
    // 统一拦截器处理
  }
}

onMounted(() => {
  loadData().catch(() => undefined)
})
</script>
