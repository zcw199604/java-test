import http from './http'

export const fetchCustomers = (params = {}) => http.get('/customers', { params })
export const fetchCustomerDetail = (id) => http.get(`/customers/${id}`)
export const createCustomer = (payload) => http.post('/customers', payload)
export const updateCustomer = (id, payload) => http.put(`/customers/${id}`, payload)
export const disableCustomer = (id) => http.delete(`/customers/${id}`)
