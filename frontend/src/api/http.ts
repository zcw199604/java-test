import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

const TOKEN_KEY = 'tobacco_token'

export const getToken = (): string => localStorage.getItem(TOKEN_KEY) || ''
export const setToken = (token: string): void => localStorage.setItem(TOKEN_KEY, token)
export const clearToken = (): void => localStorage.removeItem(TOKEN_KEY)

const http: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 12000
})

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const status = error?.response?.status
    const message = error?.response?.data?.message || '请求失败，请稍后重试'
    if (status === 401) {
      clearToken()
      ElMessage.error('登录状态已失效，请重新登录')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    } else if (status === 403) {
      ElMessage.error('当前账号没有访问权限')
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export default http
