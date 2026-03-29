import http from './http'

export const fetchPurchases = () => http.get('/purchases')
export const fetchPurchaseDetail = (id) => http.get(`/purchases/${id}`)
export const createPurchase = (payload) => http.post('/purchases', payload)
export const updatePurchase = (id, payload) => http.put(`/purchases/${id}`, payload)
export const receivePurchase = (id) => http.post(`/purchases/${id}/receive`)
export const inboundPurchase = (id, payload = {}) => http.post(`/purchases/${id}/inbound`, payload)
export const auditPurchase = (id, data) => http.post(`/purchases/${id}/audit`, data)
export const cancelPurchase = (id, data) => http.post(`/purchases/${id}/cancel`, data)
export const importPurchases = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/purchases/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}
