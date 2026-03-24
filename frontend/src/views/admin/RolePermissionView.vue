<template>
  <div class="page-stack">
    <PageSection title="角色权限" description="通过权限树批量分配模块访问与操作能力。">
      <div class="two-column-grid compact">
        <el-card shadow="never">
          <div class="tree-title">角色列表</div>
          <el-radio-group v-model="currentRole">
            <el-radio-button v-for="item in roles" :key="item.code" :label="item.code">{{ item.name }}</el-radio-button>
          </el-radio-group>
        </el-card>
        <el-card shadow="never">
          <div class="tree-title">权限树</div>
          <el-tree ref="treeRef" :data="treeData" show-checkbox node-key="id" default-expand-all />
          <div class="dialog-footer">
            <el-button v-permission="'admin:role:view'" type="primary" @click="handleSave">保存权限</el-button>
          </div>
        </el-card>
      </div>
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchPermissions, fetchRoles, updateRole } from '../../api/system'
import PageSection from '../../components/PageSection.vue'

const treeRef = ref(null)
const currentRole = ref('SUPER_ADMIN')
const roles = ref([])
const permissions = ref([])

const treeData = computed(() => {
  const groups = {}
  permissions.value.forEach((item) => {
    const group = item.module || 'OTHER'
    if (!groups[group]) {
      groups[group] = { id: group, label: group, children: [] }
    }
    groups[group].children.push({ id: item.code, label: `${item.name} (${item.code})` })
  })
  return Object.values(groups)
})

const handleSave = async () => {
  const checked = treeRef.value?.getCheckedKeys?.() || []
  await updateRole(currentRole.value, { permissions: checked })
  ElMessage.success(`已为 ${currentRole.value} 保存 ${checked.length} 项权限`)
}

onMounted(async () => {
  const [roleResult, permissionResult] = await Promise.all([
    fetchRoles().catch(() => ({ data: [] })),
    fetchPermissions().catch(() => ({ data: [] }))
  ])
  roles.value = roleResult.data || []
  permissions.value = permissionResult.data || []
  if (roles.value.length > 0) {
    currentRole.value = roles.value[0].code
  }
})
</script>
