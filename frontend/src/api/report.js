import { ElMessage } from 'element-plus'
import http, { getToken } from './http'

export const fetchPurchaseSummary = () => http.get('/reports/purchase-summary')
export const fetchSalesSummary = () => http.get('/reports/sales-summary')
export const fetchInventorySummary = () => http.get('/reports/inventory-summary')
export const fetchTrend = () => http.get('/reports/trend')
export const fetchPsiSummary = () => http.get('/reports/psi-summary')
export const fetchComplianceTrace = (params = {}) => http.get('/reports/compliance-trace', { params })
export const fetchAbnormalDocs = () => http.get('/reports/abnormal-docs')
export const auditAbnormalDoc = (id, data) => http.post(`/reports/abnormal-docs/${id}/audit`, data)
export const fetchLinkage = () => http.get('/reports/linkage')

const DEFAULT_EXPORT_ERROR = '导出失败，请稍后重试'

const resolveExportResponse = async (response) => {
  const contentType = response.headers.get('content-type') || ''
  const cloned = response.clone()

  if (contentType.includes('application/json')) {
    const payload = await cloned.json().catch(() => ({}))
    if (!response.ok || payload?.code !== 0) {
      const message = payload?.message || DEFAULT_EXPORT_ERROR
      ElMessage.error(message)
      throw new Error(message)
    }
    return payload
  }

  if (!response.ok) {
    const text = await cloned.text().catch(() => '')
    const message = text || response.statusText || DEFAULT_EXPORT_ERROR
    ElMessage.error(message)
    throw new Error(message)
  }

  return response
}

export const fetchExportData = async () => {
  const response = await fetch('/api/reports/export', {
    headers: { Authorization: `Bearer ${getToken()}` }
  })
  const validResponse = await resolveExportResponse(response)
  if (!(validResponse instanceof Response)) {
    const message = validResponse?.message || DEFAULT_EXPORT_ERROR
    return { data: message }
  }
  const blob = await validResponse.blob()
  return {
    data: `当前导出格式: Excel (.xlsx)
文件大小: ${blob.size} bytes
说明: 后端已从 CSV 导出升级为 Apache POI Excel 导出。`
  }
}

export const exportReport = async () => {
  const response = await fetch('/api/reports/export', {
    headers: { Authorization: `Bearer ${getToken()}` }
  })
  const validResponse = await resolveExportResponse(response)
  if (!(validResponse instanceof Response)) {
    return
  }
  const blob = await validResponse.blob()
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'report-summary.xlsx'
  link.click()
  window.URL.revokeObjectURL(url)
}
