<template>
  <DataPanel title="商品管理" description="支持按关键字、状态、品类筛选，并维护商品基础资料与预警阈值。">
    <template #actions>
      <el-button type="primary" @click="openCreateDialog">新增商品</el-button>
    </template>

    <div class="toolbar">
      <el-input v-model="filters.keyword" placeholder="搜索编码/名称/品类" clearable class="filter-item" @keyup.enter="loadProducts" />
      <el-select v-model="filters.status" placeholder="状态" clearable class="filter-item">
        <el-option label="启用" value="ENABLED" />
        <el-option label="停用" value="DISABLED" />
      </el-select>
      <el-select v-model="filters.category" placeholder="品类" clearable filterable class="filter-item">
        <el-option v-for="item in categoryOptions" :key="item.id || item.name" :label="item.name" :value="item.name" />
      </el-select>
      <el-button type="primary" @click="loadProducts">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <AppTable :columns="columns" :rows="rows" :loading="loading" empty-text="暂无商品数据">
      <template #unitPrice="{ value }">{{ formatPrice(value) }}</template>
      <template #actions="{ row }">
        <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
        <el-button link type="danger" :disabled="row.status === 'DISABLED'" @click="handleDisable(row)">停用</el-button>
      </template>
    </AppTable>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="商品编码" prop="code">
          <el-input v-model="form.code" maxlength="64" />
        </el-form-item>
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" maxlength="128" />
        </el-form-item>
        <el-form-item label="商品品类" prop="category">
          <el-select v-model="form.category" filterable allow-create default-first-option clearable class="full-width">
            <el-option v-for="item in categoryOptions" :key="item.id || item.name" :label="item.name" :value="item.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" maxlength="32" />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="form.unitPrice" :min="0" :precision="2" :step="1" class="full-width" />
        </el-form-item>
        <el-form-item label="预警阈值" prop="warningThreshold">
          <el-input-number v-model="form.warningThreshold" :min="0" :step="1" class="full-width" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </DataPanel>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DataPanel from '../../components/DataPanel.vue'
import AppTable from '../../components/AppTable.vue'
import { createProduct, disableProduct, fetchCategories, fetchProducts, updateProduct } from '../../api/catalog'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref()
const rows = ref([])
const categoryOptions = ref([])

const filters = reactive({
  keyword: '',
  status: '',
  category: ''
})

const createEmptyForm = () => ({
  code: '',
  name: '',
  category: '',
  unit: '条',
  unitPrice: 0,
  warningThreshold: 0
})

const form = reactive(createEmptyForm())

const rules = {
  code: [{ required: true, message: '请输入商品编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请输入或选择商品品类', trigger: 'change' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  unitPrice: [{ required: true, message: '请输入商品单价', trigger: 'change' }],
  warningThreshold: [{ required: true, message: '请输入预警阈值', trigger: 'change' }]
}

const columns = [
  { key: 'code', label: '商品编码', minWidth: 140 },
  { key: 'name', label: '商品名称', minWidth: 180 },
  { key: 'category', label: '品类', minWidth: 140 },
  { key: 'unit', label: '单位', width: 90 },
  { key: 'unitPrice', label: '单价', minWidth: 100 },
  { key: 'warningThreshold', label: '预警阈值', minWidth: 110 },
  { key: 'status', label: '状态', width: 100 }
]

const formatPrice = (value) => {
  if (value === null || value === undefined || value === '') return '--'
  return `¥${Number(value).toFixed(2)}`
}

const assignForm = (payload = {}) => {
  Object.assign(form, createEmptyForm(), {
    code: payload.code || '',
    name: payload.name || '',
    category: payload.category || '',
    unit: payload.unit || '条',
    unitPrice: Number(payload.unitPrice ?? 0),
    warningThreshold: Number(payload.warningThreshold ?? 0)
  })
}

const loadCategories = async () => {
  const result = await fetchCategories().catch(() => ({ data: [] }))
  categoryOptions.value = result.data || []
}

const loadProducts = async () => {
  loading.value = true
  try {
    const result = await fetchProducts({
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
      category: filters.category || undefined
    })
    rows.value = result.data || []
  } catch (error) {
    rows.value = []
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.keyword = ''
  filters.status = ''
  filters.category = ''
  loadProducts()
}

const openCreateDialog = () => {
  isEdit.value = false
  editingId.value = null
  assignForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  editingId.value = row.id
  assignForm(row)
  dialogVisible.value = true
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload = {
      code: form.code,
      name: form.name,
      category: form.category,
      unit: form.unit,
      unitPrice: Number(form.unitPrice),
      warningThreshold: Number(form.warningThreshold)
    }
    if (isEdit.value && editingId.value) {
      await updateProduct(editingId.value, payload)
      ElMessage.success('商品信息已更新')
    } else {
      await createProduct(payload)
      ElMessage.success('商品已新增')
    }
    dialogVisible.value = false
    await Promise.all([loadProducts(), loadCategories()])
  } catch (error) {
    // 消息由拦截器统一处理
  } finally {
    submitting.value = false
  }
}

const handleDisable = async (row) => {
  await ElMessageBox.confirm(`确认停用商品“${row.name}”吗？`, '停用商品', { type: 'warning' })
  try {
    await disableProduct(row.id)
    ElMessage.success('商品已停用')
    await loadProducts()
  } catch (error) {
    // 消息由拦截器统一处理
  }
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadProducts()])
})
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.filter-item {
  width: 220px;
}

.full-width {
  width: 100%;
}
</style>
