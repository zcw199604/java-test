<template>
  <div class="page-stack">
    <PageSection title="仓库管理" description="维护仓库名称、地址和启停用状态，支撑库存管理闭环。">
      <template #actions>
        <el-button type="primary" @click="openCreateDialog">新增仓库</el-button>
      </template>

      <AppTable :columns="columns" :rows="rows" :loading="loading" empty-text="暂无仓库数据">
        <template #actions="{ row }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button link type="danger" :disabled="row.status === 'DISABLED'" @click="handleStatus(row, 'DISABLED')">停用</el-button>
          <el-button link type="success" :disabled="row.status === 'ENABLED'" @click="handleStatus(row, 'ENABLED')">启用</el-button>
        </template>
      </AppTable>

      <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑仓库' : '新增仓库'" width="560px">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="仓库名称" prop="name">
            <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item label="地址" prop="address">
            <el-input v-model="form.address" type="textarea" :rows="3" />
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
import { ElMessage } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { createWarehouse, fetchWarehouseDetail, fetchWarehouses, updateWarehouse, updateWarehouseStatus } from '../../api/system'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref()
const rows = ref([])
const form = reactive({ name: '', address: '', status: 'ENABLED' })
const rules = { name: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }] }
const columns = [
  { key: 'code', label: '编码', width: 100 },
  { key: 'name', label: '仓库名称', minWidth: 180 },
  { key: 'address', label: '地址', minWidth: 220 },
  { key: 'status', label: '状态', width: 100 }
]

const resetForm = (payload = {}) => Object.assign(form, { name: '', address: '', status: 'ENABLED' }, payload)
const loadWarehouses = async () => {
  loading.value = true
  try {
    const result = await fetchWarehouses()
    rows.value = result.data || []
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
  const result = await fetchWarehouseDetail(row.id).catch(() => ({ data: row }))
  resetForm(result.data || row)
  dialogVisible.value = true
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value && editingId.value) {
      await updateWarehouse(editingId.value, { ...form })
      ElMessage.success('仓库信息已更新')
    } else {
      await createWarehouse({ ...form })
      ElMessage.success('仓库已新增')
    }
    dialogVisible.value = false
    await loadWarehouses()
  } finally {
    submitting.value = false
  }
}

const handleStatus = async (row, status) => {
  await updateWarehouseStatus(row.id, { status })
  ElMessage.success(`仓库已${status === 'ENABLED' ? '启用' : '停用'}`)
  await loadWarehouses()
}

onMounted(loadWarehouses)
</script>
