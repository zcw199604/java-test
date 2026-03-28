<template>
  <div class="page-stack">
    <PageSection title="角色权限" description="通过权限树批量分配模块访问与操作能力。">
      <div class="two-column-grid compact">
        <el-card shadow="never">
          <div class="tree-title">角色列表</div>
          <el-radio-group v-model="currentRoleCode" @change="applyRoleSelection">
            <el-radio-button v-for="item in roles" :key="item.code" :label="item.code">{{ item.name }}</el-radio-button>
          </el-radio-group>
          <el-descriptions v-if="currentRole" :column="1" border class="role-meta">
            <el-descriptions-item label="角色编码">{{ currentRole.code }}</el-descriptions-item>
            <el-descriptions-item label="角色名称">{{ currentRole.name }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ currentRole.remark || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
        <el-card shadow="never">
          <div class="tree-title">权限树</div>
          <el-tree ref="treeRef" :data="treeData" show-checkbox node-key="id" default-expand-all />
          <div class="dialog-footer">
            <el-button v-permission="'admin:role:view'" type="primary" :loading="saving" @click="handleSave">保存权限</el-button>
          </div>
        </el-card>
      </div>
    </PageSection>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchPermissions, fetchRoles, updateRole } from '../../api/system'
import PageSection from '../../components/PageSection.vue'

const treeRef = ref(null)
const currentRoleCode = ref('')
const roles = ref([])
const permissions = ref([])
const saving = ref(false)

const currentRole = computed(() => roles.value.find((item) => item.code === currentRoleCode.value) || null)

const treeData = computed(() => {
  const groups = {}
  permissions.value.forEach((item) => {
    const group = item.module || 'OTHER'
    if (!groups[group]) {
      groups[group] = { id: `group:${group}`, label: group, children: [] }
    }
    groups[group].children.push({ id: item.code, label: `${item.name} (${item.code})` })
  })
  return Object.values(groups)
})

const applyRoleSelection = async () => {
  await nextTick()
  treeRef.value?.setCheckedKeys?.(currentRole.value?.permissions || [])
}

const handleSave = async () => {
  if (!currentRole.value) return
  saving.value = true
  try {
    const checked = (treeRef.value?.getCheckedKeys?.() || []).filter((item) => typeof item === 'string' && item.includes(':'))
    await updateRole(currentRole.value.code, {
      name: currentRole.value.name,
      remark: currentRole.value.remark,
      permissions: checked
    })
    currentRole.value.permissions = checked
    ElMessage.success(`已为 ${currentRole.value.name} 保存 ${checked.length} 项权限`)
  } catch (error) {
    // 统一拦截器处理
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const [roleResult, permissionResult] = await Promise.all([
    fetchRoles().catch(() => ({ data: [] })),
    fetchPermissions().catch(() => ({ data: [] }))
  ])
  roles.value = (roleResult.data || []).map((item) => ({ ...item, permissions: item.permissions || [] }))
  permissions.value = permissionResult.data || []
  if (roles.value.length > 0) {
    currentRoleCode.value = roles.value[0].code
    applyRoleSelection().catch(() => undefined)
  }
})
</script>
