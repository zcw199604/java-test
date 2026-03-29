import http from './http'

export const fetchInventories = (params = {}) => http.get('/inventories', { params })
export const fetchInventoryRecords = (params = {}) => http.get('/inventory-records', { params })
export const fetchInventoryWarnings = (params = {}) => http.get('/inventory-warnings', { params })
export const createInventoryAction = (payload) => {
  const url = payload.actionType === 'TRANSFER' ? '/inventory-transfers' : '/inventory-checks'
  return http.post(url, payload)
}
export const importInventories = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/inventories/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}
