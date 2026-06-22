import { createContext, useContext, useEffect, useState } from 'react'
import client from '../api/client'

/**
 * Holds the current user + auth actions for the whole app.
 *
 * Flow:
 *  - login()  -> POST /api/auth/login, store JWT, then load the user via /api/auth/me
 *  - on app load, if a token exists, re-fetch /api/auth/me so a refresh keeps you logged in
 *  - logout() -> drop the token and user
 */
const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)     // { id, name, email, role } | null
  const [loading, setLoading] = useState(true) // true while we check an existing token

  // On first render, restore the session from a stored token (if any).
  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) {
      setLoading(false)
      return
    }
    client
      .get('/api/auth/me')
      .then((res) => setUser(res.data))
      .catch(() => localStorage.removeItem('token')) // bad/expired token
      .finally(() => setLoading(false))
  }, [])

  async function login(email, password) {
    const res = await client.post('/api/auth/login', { email, password })
    localStorage.setItem('token', res.data.access_token)
    // Now that the token is stored, the interceptor will attach it to /me.
    const me = await client.get('/api/auth/me')
    setUser(me.data)
    return me.data
  }

  function logout() {
    localStorage.removeItem('token')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

// Convenience hook so components do: const { user, login } = useAuth()
export function useAuth() {
  return useContext(AuthContext)
}
