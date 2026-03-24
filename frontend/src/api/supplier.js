import http from './http'

export const fetchSuppliers = () => http.get('/suppliers')
