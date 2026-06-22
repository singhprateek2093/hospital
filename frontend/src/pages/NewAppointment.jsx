import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import client from '../api/client'

export default function NewAppointment() {
  const navigate = useNavigate()
  const [patients, setPatients] = useState([])
  const [doctors, setDoctors] = useState([])
  const [form, setForm] = useState({ patient_id: '', doctor_id: '', scheduled_at: '', reason: '' })
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  // Load dropdown data. Patients are paginated, so grab a big first page.
  useEffect(() => {
    client.get('/api/patients', { params: { size: 100 } }).then((res) => setPatients(res.data.content))
    client.get('/api/doctors').then((res) => setDoctors(res.data))
  }, [])

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      const payload = {
        patient_id: Number(form.patient_id),
        doctor_id: Number(form.doctor_id),
        // datetime-local gives local time without zone; convert to a proper UTC ISO instant.
        scheduled_at: new Date(form.scheduled_at).toISOString(),
        reason: form.reason,
      }
      await client.post('/api/appointments', payload)
      navigate('/appointments')
    } catch (err) {
      const detail = err.response?.data?.detail
      setError(Array.isArray(detail) ? detail.map((d) => `${d.loc}: ${d.msg}`).join(', ') : (detail || 'Could not book appointment'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="container fade-in">
      <h1>Book appointment</h1>
      <form className="card form" onSubmit={handleSubmit}>
        <label>Patient *</label>
        <select value={form.patient_id} onChange={(e) => update('patient_id', e.target.value)} required>
          <option value="">— Select patient —</option>
          {patients.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
        </select>

        <label>Doctor *</label>
        <select value={form.doctor_id} onChange={(e) => update('doctor_id', e.target.value)} required>
          <option value="">— Select doctor —</option>
          {doctors.map((d) => <option key={d.id} value={d.id}>{d.name} ({d.specialization})</option>)}
        </select>

        <label>Date & time *</label>
        <input type="datetime-local" value={form.scheduled_at} onChange={(e) => update('scheduled_at', e.target.value)} required />

        <label>Reason *</label>
        <input value={form.reason} onChange={(e) => update('reason', e.target.value)} required />

        {error && <div className="error">{error}</div>}
        <button className="btn" type="submit" disabled={saving}>
          {saving ? 'Booking…' : 'Book appointment'}
        </button>
      </form>
    </div>
  )
}
