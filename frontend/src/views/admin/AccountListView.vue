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
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="角色"><el-select v-model="form.roleCode" style="width: 100%"><el-option label="平台管理员" value="ADMIN" /><el-option label="采购专员" value="PURCHASER" /><el-option label="销售专员" value="SELLER" /><el-option label="库管人员" value="KEEPER" /></el-select></el-form-item>
        <el-form-item label="指派范围"><el-input v-model="form.scope" /></el-form-item>
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
import { fetchUsers } from '../../api/system'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'

const keyword = ref('')
const rows = ref([])
const dialogVisible = ref(false)
const form = reactive({ username: '', realName: '', roleCode: 'PURCHASER', scope: '默认辖区' })
const columns = [
  { key: 'username', label: '账号' },
  { key: 'realName', label: '姓名' },
  { key: 'roleName', label: '角色' },
  { key: 'scope', label: '指派范围' },
  { key: 'status', label: '状态' }
]

const filteredRows = computed(() => rows.value.filter((item) => !keyword.value || [item.username, item.realName, item.roleName].filter(Boolean).join(' ').includes(keyword.value)))

const handleSave = () => {
  rows.value.unshift({ ...form, roleName: form.roleCode, status: 'ENABLED' })
  dialogVisible.value = false
  ElMessage.success('演示环境已完成新增账号交互设计')
}

onMounted(async () => {
  const result = await fetchUsers().catch(() => ({ data: [] }))
  rows.value = (result.data.records || result.data || []).map((item) => ({ ...item, scope: item.scope || '全区域' }))
})
</script>
