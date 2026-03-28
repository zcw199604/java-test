import http from './http'

export const fetchInventories = () => http.get('/inventories')
export const fetchInventoryRecords = () => http.get('/inventory-records')
export const fetchInventoryWarnings = () => http.get('/inventory-warnings')
export const createInventoryAction = (payload) => {
  const url = payload.actionType === 'TRANSFER' ? '/inventory-transfers' : '/inventory-checks'
  return http.post(url, payload)
}
export const importInventories = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/inventories/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}
