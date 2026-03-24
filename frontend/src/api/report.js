import http, { getToken } from './http'

export const fetchPurchaseSummary = () => http.get('/reports/purchase-summary')
export const fetchSalesSummary = () => http.get('/reports/sales-summary')
export const fetchInventorySummary = () => http.get('/reports/inventory-summary')
export const fetchTrend = () => http.get('/reports/trend')
export const fetchPsiSummary = () => http.get('/reports/psi-summary')
export const fetchComplianceTrace = () => http.get('/reports/compliance-trace')
export const fetchAbnormalDocs = () => http.get('/reports/abnormal-docs')
export const fetchLinkage = () => http.get('/reports/linkage')

export const fetchExportData = async () => {
  const response = await fetch('/api/reports/export', {
    headers: { Authorization: `Bearer ${getToken()}` }
  })
  const blob = await response.blob()
  return {
    data: `当前导出格式: Excel (.xlsx)\n文件大小: ${blob.size} bytes\n说明: 后端已从 CSV 导出升级为 Apache POI Excel 导出。`
  }
}

export const exportReport = async () => {
  const response = await fetch('/api/reports/export', {
    headers: { Authorization: `Bearer ${getToken()}` }
  })
  const blob = await response.blob()
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'report-summary.xlsx'
  link.click()
  window.URL.revokeObjectURL(url)
}
