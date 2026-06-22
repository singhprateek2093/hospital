import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import client from '../api/client'

export default function NewPatient() {
  const navigate = useNavigate()
  const [doctors, setDoctors] = useState([])
  const [form, setForm] = useState({
    name: '', age: '', gender: 'M', contact: '', assigned_doctor_id: '',
  })
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  // Load doctors for the assignment dropdown.
  useEffect(() => {
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
        name: form.name,
        age: Number(form.age),
        gender: form.gender,
        contact: form.contact,
        // Empty string -> null (unassigned)
        assigned_doctor_id: form.assigned_doctor_id ? Number(form.assigned_doctor_id) : null,
      }
      const res = await client.post('/api/patients', payload)
      navigate(`/patients/${res.data.id}`)
    } catch (err) {
      const detail = err.response?.data?.detail
      // 422 returns an array of field errors; flatten to a readable string.
      setError(Array.isArray(detail) ? detail.map((d) => `${d.loc}: ${d.msg}`).join(', ') : (detail || 'Could not create patient'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="container fade-in">
      <h1>Register patient</h1>
      <form className="card form" onSubmit={handleSubmit}>
        <label>Name *</label>
        <input value={form.name} onChange={(e) => update('name', e.target.value)} required />

        <label>Age *</label>
        <input type="number" min="0" max="150" value={form.age} onChange={(e) => update('age', e.target.value)} required />

        <label>Gender *</label>
        <select value={form.gender} onChange={(e) => update('gender', e.target.value)}>
          <option value="M">M</option>
          <option value="F">F</option>
          <option value="Other">Other</option>
        </select>

        <label>Contact *</label>
        <input value={form.contact} onChange={(e) => update('contact', e.target.value)} required />

        <label>Assign doctor</label>
        <select value={form.assigned_doctor_id} onChange={(e) => update('assigned_doctor_id', e.target.value)}>
          <option value="">— Unassigned —</option>
          {doctors.map((d) => (
            <option key={d.id} value={d.id}>{d.name} ({d.specialization})</option>
          ))}
        </select>

        {error && <div className="error">{error}</div>}
        <button className="btn" type="submit" disabled={saving}>
          {saving ? 'Saving…' : 'Create patient'}
        </button>
      </form>
    </div>
  )
}
