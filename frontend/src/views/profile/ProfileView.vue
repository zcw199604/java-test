<template>
  <div class="page-stack">
    <PageSection title="个人中心" description="查看基本信息、修改密码并浏览当前授权范围。">
      <el-tabs>
        <el-tab-pane label="基本信息">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="账号">{{ appStore.profile?.username }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ appStore.userName }}</el-descriptions-item>
            <el-descriptions-item label="角色">{{ appStore.roleName }}</el-descriptions-item>
            <el-descriptions-item label="权限数">{{ appStore.permissions.length }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        <el-tab-pane label="修改密码">
          <el-form :model="passwordForm" label-position="top" class="single-form">
            <el-form-item label="原密码"><el-input v-model="passwordForm.oldPassword" type="password" show-password /></el-form-item>
            <el-form-item label="新密码"><el-input v-model="passwordForm.newPassword" type="password" show-password /></el-form-item>
            <el-form-item label="确认密码"><el-input v-model="passwordForm.confirmPassword" type="password" show-password /></el-form-item>
            <el-button type="primary" :loading="submitting" @click="handlePasswordSave">保存修改</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="我的权限">
          <el-space wrap>
            <el-tag v-for="permission in appStore.permissions" :key="permission" type="success">{{ permission }}</el-tag>
          </el-space>
        </el-tab-pane>
      </el-tabs>
    </PageSection>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword } from '../../api/system'
import { useAppStore } from '../../stores/app'
import PageSection from '../../components/PageSection.vue'

const appStore = useAppStore()
const submitting = ref(false)
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const resetForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

const handlePasswordSave = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请完整填写密码信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  submitting.value = true
  try {
    await changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
    ElMessage.success('密码修改成功，请牢记新密码')
    resetForm()
  } finally {
    submitting.value = false
  }
}
</script>
