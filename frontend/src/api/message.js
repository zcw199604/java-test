import http from './http'

export const fetchMessages = () => http.get('/messages')
export const readMessage = (id) => http.post(`/messages/${id}/read`)
