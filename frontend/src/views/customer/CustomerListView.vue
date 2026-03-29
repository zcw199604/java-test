<template>
  <div class="page-stack">
    <PageSection title="客户管理" description="支持按关键字、状态筛选，并维护客户联系人与地址信息。">
      <template #actions>
        <el-button type="primary" @click="openCreateDialog">新增客户</el-button>
      </template>

      <div class="toolbar">
        <el-input v-model="filters.keyword" placeholder="搜索客户/联系人/电话" clearable class="filter-item" @keyup.enter="loadCustomers" />
        <el-select v-model="filters.status" placeholder="状态" clearable class="filter-item">
          <el-option label="启用" value="ENABLED" />
          <el-option label="停用" value="DISABLED" />
        </el-select>
        <el-button type="primary" @click="loadCustomers">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <AppTable :columns="columns" :rows="rows" :loading="loading" empty-text="暂无客户数据">
        <template #actions="{ row }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button link type="danger" :disabled="row.status === 'DISABLED'" @click="handleDisable(row)">停用</el-button>
        </template>
      </AppTable>

      <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑客户' : '新增客户'" width="560px" destroy-on-close>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="客户名称" prop="name">
            <el-input v-model="form.name" maxlength="128" />
          </el-form-item>
          <el-form-item label="联系人" prop="contactName">
            <el-input v-model="form.contactName" maxlength="64" />
          </el-form-item>
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="form.contactPhone" maxlength="32" />
          </el-form-item>
          <el-form-item label="联系地址" prop="address">
            <el-input v-model="form.address" type="textarea" :rows="3" maxlength="255" show-word-limit />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" class="full-width">
              <el-option label="启用" value="ENABLED" />
              <el-option label="停用" value="DISABLED" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
        </template>
      </el-dialog>
    </PageSection>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { createCustomer, disableCustomer, fetchCustomerDetail, fetchCustomers, updateCustomer } from '../../api/customer'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref()
const rows = ref([])

const filters = reactive({ keyword: '', status: '' })
const createEmptyForm = () => ({ name: '', contactName: '', contactPhone: '', address: '', status: 'ENABLED' })
const form = reactive(createEmptyForm())
const rules = { name: [{ required: true, message: '请输入客户名称', trigger: 'blur' }] }
const columns = [
  { key: 'name', label: '客户名称', minWidth: 180 },
  { key: 'contactName', label: '联系人', minWidth: 120 },
  { key: 'contactPhone', label: '联系电话', minWidth: 140 },
  { key: 'address', label: '地址', minWidth: 220 },
  { key: 'status', label: '状态', width: 100 }
]

const assignForm = (payload = {}) => Object.assign(form, createEmptyForm(), payload)

const loadCustomers = async () => {
  loading.value = true
  try {
    const result = await fetchCustomers({ keyword: filters.keyword || undefined, status: filters.status || undefined })
    rows.value = result.data || []
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.keyword = ''
  filters.status = ''
  loadCustomers()
}

const openCreateDialog = () => {
  isEdit.value = false
  editingId.value = null
  assignForm()
  dialogVisible.value = true
}

const openEditDialog = async (row) => {
  isEdit.value = true
  editingId.value = row.id
  const result = await fetchCustomerDetail(row.id).catch(() => ({ data: row }))
  assignForm(result.data || row)
  dialogVisible.value = true
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload = { ...form }
    if (isEdit.value && editingId.value) {
      await updateCustomer(editingId.value, payload)
      ElMessage.success('客户信息已更新')
    } else {
      await createCustomer(payload)
      ElMessage.success('客户已新增')
    }
    dialogVisible.value = false
    await loadCustomers()
  } finally {
    submitting.value = false
  }
}

const handleDisable = async (row) => {
  await ElMessageBox.confirm(`确认停用客户“${row.name}”吗？`, '停用客户', { type: 'warning' })
  await disableCustomer(row.id)
  ElMessage.success('客户已停用')
  await loadCustomers()
}

onMounted(loadCustomers)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-item { width: 260px; }
</style>
