import { useState } from 'react'
import client from '../api/client'
import { useAuth } from '../context/AuthContext'

// Self-service password change for the logged-in user (any role).
export default function ChangePassword() {
  const { user } = useAuth()
  const [form, setForm] = useState({ current: '', next: '', confirm: '' })
  const [message, setMessage] = useState(null) // { type, text }
  const [saving, setSaving] = useState(false)

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setMessage(null)

    // Client-side check: new password typed twice must match.
    if (form.next !== form.confirm) {
      setMessage({ type: 'err', text: 'New passwords do not match' })
      return
    }

    setSaving(true)
    try {
      await client.post('/api/auth/change-password', {
        current_password: form.current,
        new_password: form.next,
      })
      setMessage({ type: 'ok', text: 'Password updated successfully ✅' })
      setForm({ current: '', next: '', confirm: '' })
    } catch (err) {
      const detail = err.response?.data?.detail
      setMessage({
        type: 'err',
        text: Array.isArray(detail) ? detail.map((d) => `${d.loc}: ${d.msg}`).join(', ') : (detail || 'Could not update password'),
      })
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="container fade-in">
      <h1>Change password</h1>
      <p className="muted">Signed in as <strong>{user.name}</strong> ({user.email})</p>

      <form className="card form" onSubmit={handleSubmit}>
        <label>Current password *</label>
        <input type="password" autoComplete="current-password"
               value={form.current} onChange={(e) => update('current', e.target.value)} required />

        <label>New password * (min 6 chars)</label>
        <input type="password" autoComplete="new-password"
               value={form.next} onChange={(e) => update('next', e.target.value)} required />

        <label>Confirm new password *</label>
        <input type="password" autoComplete="new-password"
               value={form.confirm} onChange={(e) => update('confirm', e.target.value)} required />

        {message && <div className={message.type === 'ok' ? 'success' : 'error'}>{message.text}</div>}

        <button className="btn btn-grad" type="submit" disabled={saving}>
          {saving ? <span className="spinner spinner-sm" /> : 'Update password'}
        </button>
      </form>
    </div>
  )
}
