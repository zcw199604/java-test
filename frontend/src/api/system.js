import http from './http'

export const fetchUsers = () => http.get('/users')
export const fetchRoles = () => http.get('/roles')
