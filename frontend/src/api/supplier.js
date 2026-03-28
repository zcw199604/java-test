import http from './http'

export const fetchSuppliers = (params = {}) => http.get('/suppliers', { params })
export const createSupplier = (payload) => http.post('/suppliers', payload)
export const updateSupplier = (id, payload) => http.put(`/suppliers/${id}`, payload)
export const disableSupplier = (id) => http.delete(`/suppliers/${id}`)
