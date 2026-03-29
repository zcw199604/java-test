import http from './http'

export const fetchDashboardSummary = () => http.get('/dashboard/summary')

export const fetchDashboardSalesHistory = (params) => http.get('/dashboard/sales-history', { params })
