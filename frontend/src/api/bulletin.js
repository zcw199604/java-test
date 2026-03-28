import http from './http'

export const fetchBulletins = () => http.get('/bulletins')
export const createBulletin = (payload) => http.post('/bulletins', payload)
