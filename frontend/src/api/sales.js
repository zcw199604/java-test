import http from './http'

export const fetchSales = () => http.get('/sales')
export const fetchSalesDetail = (id) => http.get(`/sales/${id}`)
export const createSales = (payload) => http.post('/sales', payload)
export const updateSales = (id, payload) => http.put(`/sales/${id}`, payload)
export const outboundSales = (id, payload = {}) => http.post(`/sales/${id}/outbound`, payload)
export const paymentSales = (id, payload) => http.post(`/sales/${id}/payment`, payload)
export const fetchSalesStatistics = () => http.get('/sales/statistics')
export const auditSales = (id, data) => http.post(`/sales/${id}/audit`, data)
export const cancelSales = (id, data) => http.post(`/sales/${id}/cancel`, data)
export const importSales = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/sales/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}
