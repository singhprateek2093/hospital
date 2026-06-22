import axios from 'axios'

// The base URL of the backend API. Falls back to the local Spring Boot server.
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const client = axios.create({ baseURL })

// --- Request interceptor -----------------------------------------------------
// Attach the JWT (if we have one) to every outgoing request as a Bearer token.
// This is the single place auth is added — individual API calls stay clean.
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// --- Response interceptor ----------------------------------------------------
// If the server ever says 401 (token missing/expired), wipe the session and bounce
// to login. Prevents the app from getting stuck in a broken authenticated state.
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      // Avoid a redirect loop if we're already on the login page.
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default client
