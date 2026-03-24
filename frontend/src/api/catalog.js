import http from './http'

export const fetchProducts = () => http.get('/products')
