import http from './http'

export const fetchPurchases = () => http.get('/purchases')
export const createPurchase = (payload) => http.post('/purchases', payload)
export const receivePurchase = (id) => http.post(`/purchases/${id}/receive`)
export const inboundPurchase = (id) => http.post(`/purchases/${id}/inbound`)
