<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h1>烟草采销存协同管理平台</h1>
        <p>请输入账号密码登录系统。当前为前后端分离 Vue 前端。</p>
      </div>
      <form class="login-form" @submit.prevent="handleSubmit">
        <label>
          <span>账号</span>
          <input v-model.trim="form.username" placeholder="请输入账号，如 admin" />
        </label>
        <label>
          <span>密码</span>
          <input v-model.trim="form.password" type="password" placeholder="请输入密码，如 123456" />
        </label>
        <button :disabled="submitting" type="submit">{{ submitting ? '登录中...' : '登录' }}</button>
      </form>
      <p class="tips">演示建议：使用后端种子账号登录，例如 admin / 123456。</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login, fetchProfile } from '../../api/auth'
import { setToken } from '../../api/http'
import { useAppStore } from '../../stores/app'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const submitting = ref(false)
const form = reactive({
  username: 'admin',
  password: '123456'
})

const handleSubmit = async () => {
  if (!form.username || !form.password) {
    window.alert('请输入账号和密码')
    return
  }

  submitting.value = true
  try {
    const loginResult = await login({ ...form })
    setToken(loginResult.data.token)
    const profileResult = await fetchProfile()
    appStore.setProfile(profileResult.data)
    router.push(route.query.redirect || '/dashboard')
  } catch (error) {
    window.alert(error?.response?.data?.message || '登录失败，请检查账号密码或后端服务状态')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #0f172a, #1d4ed8);
}

.login-card {
  width: 420px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.25);
}

.login-header h1 {
  margin: 0 0 10px;
  font-size: 28px;
}

.login-header p,
.tips {
  color: #64748b;
}

.login-form {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

input {
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 15px;
}

button {
  border: none;
  border-radius: 12px;
  padding: 13px 16px;
  background: #2563eb;
  color: #fff;
  cursor: pointer;
}

button:disabled {
  opacity: 0.7;
  cursor: wait;
}
</style>
