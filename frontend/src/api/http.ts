import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

const TOKEN_KEY = 'tobacco_token'
const DEFAULT_ERROR_MESSAGE = '请求失败，请稍后重试'

interface ApiResponse<T = unknown> {
  code?: number
  message?: string
  data?: T
}

interface BusinessError extends Error {
  code?: number
  data?: unknown
  response?: {
    data?: ApiResponse
  }
  isBusinessError?: boolean
}

export const getToken = (): string => localStorage.getItem(TOKEN_KEY) || ''
export const setToken = (token: string): void => localStorage.setItem(TOKEN_KEY, token)
export const clearToken = (): void => localStorage.removeItem(TOKEN_KEY)

const http: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 12000
})

const createBusinessError = (payload: ApiResponse): BusinessError => {
  const message = payload.message || DEFAULT_ERROR_MESSAGE
  const error = new Error(message) as BusinessError
  error.code = payload.code
  error.data = payload.data
  error.response = { data: payload }
  error.isBusinessError = true
  return error
}

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
});

(http.interceptors.response.use as any)(
  (response: any) => {
    const payload = response.data as ApiResponse | Blob | string | undefined
    if (payload && typeof payload === 'object' && 'code' in payload) {
      if (payload.code === 0) {
        return payload
      }
      const error = createBusinessError(payload)
      ElMessage.error(error.message || DEFAULT_ERROR_MESSAGE)
      return Promise.reject(error)
    }
    return payload
  },
  (error: any) => {
    const status = error?.response?.status
    const message = error?.response?.data?.message || error?.message || DEFAULT_ERROR_MESSAGE
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
