import http from './http'

export const fetchDashboardSummary = () => http.get('/dashboard/summary')
