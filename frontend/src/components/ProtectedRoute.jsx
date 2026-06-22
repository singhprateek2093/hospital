import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/**
 * Wraps routes that require login (and optionally a specific role).
 *  - still checking the token? show nothing (avoids a flash of the login page)
 *  - not logged in?           -> redirect to /login
 *  - wrong role?              -> redirect to the dashboard
 *
 * NOTE: this is convenience/UX only. Real enforcement is on the backend — a doctor
 * who hand-crafts a request still can't see another doctor's patient (403).
 */
export default function ProtectedRoute({ children, roles }) {
  const { user, loading } = useAuth()

  if (loading) return null
  if (!user) return <Navigate to="/login" replace />
  if (roles && !roles.includes(user.role)) return <Navigate to="/" replace />

  return children
}
