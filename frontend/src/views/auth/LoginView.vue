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
          <el-link type="primary" @click="openForgotDialog">忘记密码？</el-link>
        </div>
        <div class="login-options">
          <span>演示账号：admin / buyer / seller / keeper</span>
          <span v-if="captchaExpiredAt">验证码有效期至 {{ captchaExpiredAt }}</span>
        </div>
        <el-button type="primary" class="login-btn" size="large" :loading="submitting" @click="handleSubmit">登录系统</el-button>
      </el-form>
    </el-card>

    <el-dialog v-model="forgotVisible" title="忘记密码" width="520px">
      <el-form label-position="top" :model="forgotForm">
        <el-form-item label="账号">
          <el-input v-model="forgotForm.username" placeholder="请输入需要找回的账号" />
        </el-form-item>
        <div class="login-row">
          <el-form-item label="验证码" class="captcha-item">
            <el-input v-model="forgotForm.captchaCode" placeholder="请输入验证码" />
          </el-form-item>
          <div class="captcha-box" @click="refreshCaptcha">{{ captchaText }}</div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="forgotVisible = false">取消</el-button>
        <el-button type="primary" :loading="forgotSubmitting" @click="handleForgotPassword">获取重置凭证</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { fetchCaptcha, fetchProfile, forgotPassword, login } from '../../api/auth'
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
const forgotSubmitting = ref(false)
const forgotVisible = ref(false)
const errorMessage = ref('')
const captchaKey = ref('')
const captchaText = ref('----')
const captchaExpiredAt = ref('')

const form = reactive<LoginFormState>({
  username: 'admin',
  password: '123456',
  captcha: '',
  remember: true
})

const forgotForm = reactive({
  username: '',
  captchaCode: ''
})

const rules: FormRules<LoginFormState> = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const refreshCaptcha = async () => {
  const result = await fetchCaptcha().catch(() => ({ data: null }))
  const data = result.data || {}
  captchaKey.value = data.captchaKey || data.uuid || ''
  captchaText.value = data.captchaCode || data.code || '----'
  captchaExpiredAt.value = data.expiredAt || ''
  form.captcha = ''
  forgotForm.captchaCode = ''
}

const openForgotDialog = async () => {
  forgotVisible.value = true
  forgotForm.username = form.username
  forgotForm.captchaCode = ''
  if (!captchaKey.value) {
    await refreshCaptcha()
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!captchaKey.value) {
    await refreshCaptcha()
  }
  submitting.value = true
  errorMessage.value = ''
  try {
    const result = await login({ username: form.username, password: form.password, captchaKey: captchaKey.value, captchaCode: form.captcha })
    setToken(result.data.token)
    const profileResult = await fetchProfile()
    appStore.bootstrapProfile(profileResult.data || {})
    ElMessage.success('登录成功，欢迎回来')
    router.push(String(route.query.redirect || '/dashboard'))
  } catch (error: any) {
    errorMessage.value = error?.response?.data?.message || error?.message || '登录失败，请检查账号密码、验证码或后端服务状态'
    await refreshCaptcha()
  } finally {
    submitting.value = false
  }
}

const handleForgotPassword = async () => {
  if (!forgotForm.username || !forgotForm.captchaCode) {
    ElMessage.warning('请填写账号和验证码')
    return
  }
  forgotSubmitting.value = true
  try {
    const result = await forgotPassword({ username: forgotForm.username, captchaKey: captchaKey.value, captchaCode: forgotForm.captchaCode })
    const token = result.data?.resetToken || result.data?.token
    ElMessage.success('已生成重置凭证，正在跳转到重置页面')
    forgotVisible.value = false
    router.push({ path: '/reset-password', query: { username: forgotForm.username, token: token || '' } })
  } catch (error: any) {
    errorMessage.value = error?.response?.data?.message || error?.message || '获取重置凭证失败，请稍后重试'
  } finally {
    forgotSubmitting.value = false
    await refreshCaptcha()
  }
}

refreshCaptcha().catch(() => undefined)
</script>
