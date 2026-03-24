import http from './http'

export const fetchSales = () => http.get('/sales')
export const createSales = (payload) => http.post('/sales', payload)
export const outboundSales = (id) => http.post(`/sales/${id}/outbound`)
export const paymentSales = (id, payload) => http.post(`/sales/${id}/payment`, payload)
export const fetchSalesStatistics = () => http.get('/sales/statistics')
