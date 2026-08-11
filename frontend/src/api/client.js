import axios from 'axios'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_URL,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export const authAPI = {
  login: (email, password) => api.post('/auth/login', { email, password }),
  register: (name, email, password, role) =>
    api.post('/auth/register', { name, email, password, role }),
}

export const eventAPI = {
  listPublished: () => api.get('/events'),
  create: (data) => api.post('/events', data),
  getById: (id) => api.get(`/events/${id}`),
  update: (id, data) => api.put(`/events/${id}`, data),
  createSeats: (eventId, seats) => api.post(`/events/${eventId}/seats`, seats),
  listSeats: (eventId) => api.get(`/events/${eventId}/seats`),
}

export const reservationAPI = {
  reserve: (eventId, seatIds) =>
    api.post('/reservations', { eventId, seatIds }),
}

export default api
