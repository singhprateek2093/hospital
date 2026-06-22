import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client from '../api/client'
import { useAuth } from '../context/AuthContext'
import { BRAND } from '../config/brand'
import useCountUp from '../hooks/useCountUp'

function StatTile({ icon, value, label, accent }) {
  const animated = useCountUp(value)
  return (
    <div className={`card stat lift ${accent}`}>
      <div className="stat-icon">{icon}</div>
      <div className="stat-num">{value == null ? '…' : animated}</div>
      <div className="stat-label">{label}</div>
    </div>
  )
}

export default function Dashboard() {
  const { user } = useAuth()
  const [patientCount, setPatientCount] = useState(null)
  const [apptCount, setApptCount] = useState(null)

  useEffect(() => {
    client.get('/api/patients', { params: { size: 1 } })
      .then((res) => setPatientCount(res.data.total_elements)).catch(() => setPatientCount(0))
    client.get('/api/appointments', { params: { size: 1 } })
      .then((res) => setApptCount(res.data.total_elements)).catch(() => setApptCount(0))
  }, [])

  const isDoctor = user.role === 'DOCTOR'
  const isStaff = user.role === 'ADMIN' || user.role === 'RECEPTIONIST'

  return (
    <div className="container fade-in">
      <div className="welcome-banner">
        <div>
          <h1>Welcome, {user.name} 👋</h1>
          <p className="muted">Signed in as <span className="role-chip">{user.role}</span> · {BRAND.name}, {BRAND.city}</p>
        </div>
        <div className="banner-art">{BRAND.logo}</div>
      </div>

      <div className="cards">
        <StatTile icon="👥" value={patientCount} accent="accent-blue"
                  label={isDoctor ? 'Your patients' : 'Total patients'} />
        <StatTile icon="📅" value={apptCount} accent="accent-green"
                  label={isDoctor ? 'Your appointments' : 'Total appointments'} />

        <Link to="/patients" className="card action lift">
          <div className="action-icon">🔍</div>
          <h3>View patients →</h3>
          <p className="muted">{isDoctor ? 'Your patient list' : 'All patients'}</p>
        </Link>

        <Link to="/appointments" className="card action lift">
          <div className="action-icon">🗓️</div>
          <h3>{isDoctor ? 'My schedule →' : 'Appointments →'}</h3>
          <p className="muted">{isDoctor ? 'Your upcoming visits' : 'Book & manage visits'}</p>
        </Link>

        {isStaff && (
          <Link to="/patients/new" className="card action lift">
            <div className="action-icon">🧾</div>
            <h3>Register a patient →</h3>
            <p className="muted">Create a new patient record</p>
          </Link>
        )}

        {user.role === 'ADMIN' && (
          <Link to="/register" className="card action lift">
            <div className="action-icon">👤</div>
            <h3>Add a user →</h3>
            <p className="muted">Create doctor / staff accounts</p>
          </Link>
        )}
      </div>
    </div>
  )
}
