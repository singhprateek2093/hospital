import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { BRAND } from '../config/brand'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  if (!user) return null

  const isStaff = user.role === 'ADMIN' || user.role === 'RECEPTIONIST'

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <nav className="navbar">
      <div className="navbar-left">
        <Link to="/" className="brand">
          <span className="brand-logo">{BRAND.logo}</span>
          <span className="brand-text">
            {BRAND.name}
            <small>{BRAND.city}, {BRAND.state}</small>
          </span>
        </Link>
        <div className="nav-links">
          <NavLink to="/" end>🏠 Dashboard</NavLink>
          <NavLink to="/patients">👥 Patients</NavLink>
          <NavLink to="/appointments">📅 Appointments</NavLink>
          {isStaff && <NavLink to="/patients/new">➕ New Patient</NavLink>}
          {user.role === 'ADMIN' && <NavLink to="/register">👤 Add User</NavLink>}
        </div>
      </div>
      <div className="navbar-right">
        <span className="badge">{user.name} · {user.role}</span>
        <NavLink to="/change-password" className="nav-pw" title="Change password">🔑 Password</NavLink>
        <button className="btn-logout" onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  )
}
