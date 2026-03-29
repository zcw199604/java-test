<template>
  <div class="login-screen">
    <div class="login-overlay"></div>
    <el-card class="login-card" shadow="never">
      <div class="login-brand">
        <div class="brand-badge">TC</div>
        <div>
          <h1>重置密码</h1>
          <p>请输入账号、重置凭证和新密码，完成密码重置。</p>
        </div>
      </div>
      <el-form label-position="top" :model="form">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="重置凭证">
          <el-input v-model="form.resetToken" placeholder="请输入从忘记密码流程获取的凭证" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="form.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <el-button type="primary" class="login-btn" :loading="submitting" @click="handleSubmit">确认重置</el-button>
        <el-button class="login-btn" @click="router.push('/login')">返回登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { resetPassword } from '../../api/auth'

const router = useRouter()
const route = useRoute()
const submitting = ref(false)
const form = reactive({
  username: String(route.query.username || ''),
  resetToken: String(route.query.token || ''),
  newPassword: '',
  confirmPassword: ''
})

const handleSubmit = async () => {
  if (!form.username || !form.resetToken || !form.newPassword || !form.confirmPassword) {
    ElMessage.warning('请完整填写重置信息')
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  submitting.value = true
  try {
    await resetPassword({ username: form.username, resetToken: form.resetToken, newPassword: form.newPassword })
    ElMessage.success('密码已重置，请重新登录')
    router.push('/login')
  } finally {
    submitting.value = false
  }
}
</script>
