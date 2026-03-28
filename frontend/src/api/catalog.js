import http from './http'

export const fetchProducts = (params = {}) => http.get('/products', { params })
export const fetchCategories = () => http.get('/categories')
export const createProduct = (payload) => http.post('/products', payload)
export const updateProduct = (id, payload) => http.put(`/products/${id}`, payload)
export const disableProduct = (id) => http.delete(`/products/${id}`)
