import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import client from '../api/client'

export default function PatientDetail() {
  const { id } = useParams()
  const [patient, setPatient] = useState(null)
  const [appointments, setAppointments] = useState([])
  const [error, setError] = useState('')
  // Intake form fields
  const [symptoms, setSymptoms] = useState('')
  const [vitals, setVitals] = useState('')
  const [notes, setNotes] = useState('')
  const [saving, setSaving] = useState(false)

  function load() {
    setError('')
    client
      .get(`/api/patients/${id}`)
      .then((res) => setPatient(res.data))
      .catch((err) => setError(err.response?.data?.detail || 'Could not load patient'))
    // This patient's appointments (server filters by role + patient).
    client
      .get('/api/appointments', { params: { patientId: id, size: 50 } })
      .then((res) => setAppointments(res.data.content))
      .catch(() => setAppointments([]))
  }

  useEffect(() => { load() }, [id])

  async function addIntake(e) {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      await client.post(`/api/patients/${id}/intake`, { symptoms, vitals, notes })
      setSymptoms(''); setVitals(''); setNotes('')
      load() // refresh the history
    } catch (err) {
      setError(err.response?.data?.detail || 'Could not save intake')
    } finally {
      setSaving(false)
    }
  }

  // 403 (doctor viewing someone else's patient) or 404 lands here.
  if (error && !patient) {
    return (
      <div className="container fade-in">
        <div className="error">{error}</div>
        <Link to="/patients">← Back to patients</Link>
      </div>
    )
  }

  if (!patient) return <div className="container"><p className="muted">Loading…</p></div>

  return (
    <div className="container fade-in">
      <Link to="/patients">← Back to patients</Link>
      <h1>{patient.name}</h1>
      <div className="card">
        <div className="kv"><span>Age</span><strong>{patient.age}</strong></div>
        <div className="kv"><span>Gender</span><strong>{patient.gender}</strong></div>
        <div className="kv"><span>Contact</span><strong>{patient.contact}</strong></div>
        <div className="kv"><span>Assigned doctor</span><strong>{patient.assigned_doctor_name || '—'}</strong></div>
      </div>

      <h2>Appointments</h2>
      {appointments.length === 0 ? (
        <p className="muted">No appointments for this patient.</p>
      ) : (
        <table className="table">
          <thead>
            <tr><th>When</th><th>Doctor</th><th>Reason</th><th>Status</th></tr>
          </thead>
          <tbody>
            {appointments.map((a) => (
              <tr key={a.id}>
                <td>{new Date(a.scheduled_at).toLocaleString()}</td>
                <td>{a.doctor_name}</td>
                <td>{a.reason}</td>
                <td><span className={`pill pill-${a.status === 'COMPLETED' ? 'green' : a.status === 'CANCELLED' ? 'red' : 'blue'}`}>{a.status}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <h2>Intake history</h2>
      {patient.intake_forms.length === 0 ? (
        <p className="muted">No intake records yet.</p>
      ) : (
        <div className="cards">
          {patient.intake_forms.map((f) => (
            <div className="card" key={f.id}>
              <div className="kv"><span>Symptoms</span><strong>{f.symptoms}</strong></div>
              <div className="kv"><span>Vitals</span><strong>{f.vitals || '—'}</strong></div>
              <div className="kv"><span>Notes</span><strong>{f.notes || '—'}</strong></div>
              <div className="muted small">{new Date(f.date).toLocaleString()}</div>
            </div>
          ))}
        </div>
      )}

      <h2>Add intake</h2>
      <form className="card form" onSubmit={addIntake}>
        <label>Symptoms *</label>
        <input value={symptoms} onChange={(e) => setSymptoms(e.target.value)} required />
        <label>Vitals</label>
        <input value={vitals} onChange={(e) => setVitals(e.target.value)} />
        <label>Notes</label>
        <textarea value={notes} onChange={(e) => setNotes(e.target.value)} rows={3} />
        {error && <div className="error">{error}</div>}
        <button className="btn" type="submit" disabled={saving}>
          {saving ? 'Saving…' : 'Add intake'}
        </button>
      </form>
    </div>
  )
}
