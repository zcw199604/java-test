import http from './http'

export const fetchCustomers = () => http.get('/customers')
