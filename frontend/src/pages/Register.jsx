import { useState } from 'react'
import client from '../api/client'

// Admin-only page to create new user accounts (doctors / receptionists / admins).
export default function Register() {
  const [form, setForm] = useState({
    name: '', email: '', password: '', role: 'DOCTOR', specialization: '',
  })
  const [message, setMessage] = useState(null) // { type, text }
  const [saving, setSaving] = useState(false)

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setMessage(null)
    setSaving(true)
    try {
      const payload = { ...form }
      if (payload.role !== 'DOCTOR') delete payload.specialization
      const res = await client.post('/api/auth/register', payload)
      setMessage({ type: 'ok', text: `Created ${res.data.name} (${res.data.role})` })
      setForm({ name: '', email: '', password: '', role: 'DOCTOR', specialization: '' })
    } catch (err) {
      const detail = err.response?.data?.detail
      setMessage({
        type: 'err',
        text: Array.isArray(detail) ? detail.map((d) => `${d.loc}: ${d.msg}`).join(', ') : (detail || 'Could not create user'),
      })
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="container fade-in">
      <h1>Add user</h1>
      <form className="card form" onSubmit={handleSubmit}>
        <label>Name *</label>
        <input value={form.name} onChange={(e) => update('name', e.target.value)} required />

        <label>Email *</label>
        <input type="email" value={form.email} onChange={(e) => update('email', e.target.value)} required />

        <label>Password * (min 6 chars)</label>
        <input type="password" value={form.password} onChange={(e) => update('password', e.target.value)} required />

        <label>Role *</label>
        <select value={form.role} onChange={(e) => update('role', e.target.value)}>
          <option value="DOCTOR">DOCTOR</option>
          <option value="RECEPTIONIST">RECEPTIONIST</option>
          <option value="ADMIN">ADMIN</option>
        </select>

        {form.role === 'DOCTOR' && (
          <>
            <label>Specialization *</label>
            <input value={form.specialization} onChange={(e) => update('specialization', e.target.value)} required />
          </>
        )}

        {message && <div className={message.type === 'ok' ? 'success' : 'error'}>{message.text}</div>}
        <button className="btn" type="submit" disabled={saving}>
          {saving ? 'Saving…' : 'Create user'}
        </button>
      </form>
    </div>
  )
}
