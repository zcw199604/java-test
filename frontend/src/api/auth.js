import http from './http'

export const login = (payload) => http.post('/auth/login', payload)
export const fetchProfile = () => http.get('/auth/profile')
export const logout = () => http.post('/auth/logout')
