import http, { getToken } from './http'

export const fetchPurchaseSummary = () => http.get('/reports/purchase-summary')
export const fetchSalesSummary = () => http.get('/reports/sales-summary')
export const fetchInventorySummary = () => http.get('/reports/inventory-summary')
export const fetchTrend = () => http.get('/reports/trend')

export const fetchExportData = async () => {
  const response = await fetch('/api/reports/export', {
    headers: { Authorization: `Bearer ${getToken()}` }
  })
  return { data: await response.text() }
}

export const exportReport = async () => {
  const response = await fetch('/api/reports/export', {
    headers: { Authorization: `Bearer ${getToken()}` }
  })
  const blob = await response.blob()
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'report-summary.csv'
  link.click()
  window.URL.revokeObjectURL(url)
}
