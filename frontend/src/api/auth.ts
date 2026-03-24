import http from './http'

export const fetchCaptcha = () => http.get('/auth/captcha')
export const login = (payload: { username: string; password: string; captchaKey?: string; captchaCode?: string }) => http.post('/auth/login', payload)
export const fetchProfile = () => http.get('/auth/profile')
export const logout = () => http.post('/auth/logout')
export const forgotPassword = (payload: { username: string; captchaKey: string; captchaCode: string }) => http.post('/auth/forgot-password', payload)
export const resetPassword = (payload: { username: string; resetToken: string; newPassword: string }) => http.post('/auth/reset-password', payload)
