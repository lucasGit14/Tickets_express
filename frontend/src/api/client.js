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

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const url = error.config?.url || ''
    const isAuthEndpoint =
      url.includes('/auth/login') || url.includes('/auth/register')

    if (status === 401 && !isAuthEndpoint) {
      localStorage.removeItem('token')
      const path = window.location.pathname
      if (path !== '/login' && path !== '/register') {
        window.location.assign('/login')
      }
    }

    return Promise.reject(error)
  }
)

export const authAPI = {
  login: (email, password) => api.post('/auth/login', { email, password }),
  register: (name, email, password, role) =>
    api.post('/auth/register', { name, email, password, role }),
  me: () => api.get('/auth/me'),
}

export const eventAPI = {
  listPublished: () => api.get('/events'),
  listMine: () => api.get('/events/mine'),
  getById: (id) => api.get(`/events/${id}`),
  create: (data) => api.post('/events', data),
  update: (id, data) => api.put(`/events/${id}`, data),
  publish: (id) => api.post(`/events/${id}/publish`),
  cancel: (id) => api.post(`/events/${id}/cancel`),
  listReservations: (id) => api.get(`/events/${id}/reservations`),
  createSeats: (eventId, seats) => api.post(`/events/${eventId}/seats`, seats),
  listSeats: (eventId) => api.get(`/events/${eventId}/seats`),
}

export const reservationAPI = {
  reserve: (eventId, seatIds) =>
    api.post('/reservations', { eventId, seatIds }),
  pay: (id) => api.post(`/reservations/${id}/pay`),
  cancel: (id) => api.post(`/reservations/${id}/cancel`),
  listMine: () => api.get('/reservations/me'),
  getById: (id) => api.get(`/reservations/${id}`),
}

export const ticketAPI = {
  listMine: () => api.get('/tickets/me'),
  getById: (id) => api.get(`/tickets/${id}`),
  validate: (code) => api.post('/tickets/validate', { code }),
  transfer: (id, email) => api.post(`/tickets/${id}/transfer`, { email }),
}

export default api
