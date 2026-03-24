<template>
  <div class="login-screen">
    <div class="login-overlay"></div>
    <el-card class="login-card" shadow="never">
      <div class="login-brand">
        <div class="brand-badge">TC</div>
        <div>
          <h1>烟草采销存协同管理平台</h1>
          <p>采购、库存、销售、统计追溯一体化工作台</p>
        </div>
      </div>
      <el-alert v-if="errorMessage" :title="errorMessage" type="error" :closable="false" show-icon class="login-alert" />
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleSubmit">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号，例如 admin" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" size="large" />
        </el-form-item>
        <div class="login-row">
          <el-form-item label="验证码" prop="captcha" class="captcha-item">
            <el-input v-model="form.captcha" placeholder="请输入验证码" size="large" />
          </el-form-item>
          <div class="captcha-box" @click="refreshCaptcha">{{ captchaText }}</div>
        </div>
        <div class="login-options">
          <el-checkbox v-model="form.remember">记住我</el-checkbox>
          <span>演示账号：admin / buyer / seller / keeper</span>
        </div>
        <el-button type="primary" class="login-btn" size="large" :loading="submitting" @click="handleSubmit">登录系统</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { fetchProfile, login } from '../../api/auth'
import { setToken } from '../../api/http'
import { useAppStore } from '../../stores/app'

interface LoginFormState {
  username: string
  password: string
  captcha: string
  remember: boolean
}

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const errorMessage = ref('')
const captchaText = ref('6A9K')

const form = reactive<LoginFormState>({
  username: 'admin',
  password: '123456',
  captcha: '6A9K',
  remember: true
})

const rules: FormRules<LoginFormState> = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const refreshCaptcha = () => {
  captchaText.value = Math.random().toString(36).slice(2, 6).toUpperCase()
  form.captcha = ''
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.captcha.toUpperCase() !== captchaText.value) {
    errorMessage.value = '验证码错误，请重新输入'
    refreshCaptcha()
    return
  }
  submitting.value = true
  errorMessage.value = ''
  try {
    const result = await login({ username: form.username, password: form.password })
    setToken(result.data.token)
    const profileResult = await fetchProfile()
    appStore.bootstrapProfile(profileResult.data || {})
    ElMessage.success('登录成功，欢迎回来')
    router.push(String(route.query.redirect || '/dashboard'))
  } catch (error: any) {
    errorMessage.value = error?.response?.data?.message || error?.message || '登录失败，请检查账号密码、验证码或后端服务状态'
  } finally {
    submitting.value = false
  }
}
</script>
