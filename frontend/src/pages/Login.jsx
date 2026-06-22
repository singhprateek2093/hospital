import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { BRAND } from '../config/brand'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await login(email, password)
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.detail || 'Login failed')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="login-screen">
      {/* animated decorative blobs in the background */}
      <div className="blob blob-1" />
      <div className="blob blob-2" />
      <div className="blob blob-3" />

      <div className="login-grid">
        {/* Left hero panel */}
        <div className="login-hero">
          <div className="hero-logo float">{BRAND.logo}</div>
          <h1>{BRAND.name}</h1>
          <p className="hero-loc">📍 {BRAND.location}</p>
          <p className="hero-tag">{BRAND.tagline}</p>
          <div className="hero-pills">
            <span className="hero-pill">❤️ Cardiology</span>
            <span className="hero-pill">🧠 Neurology</span>
            <span className="hero-pill">🩺 General</span>
          </div>
        </div>

        {/* Right login card */}
        <form className="card form login-card pop-in" onSubmit={handleSubmit}>
          <h2>Welcome back 👋</h2>
          <p className="muted">Sign in to continue</p>

          <label>Email</label>
          <input value={email} onChange={(e) => setEmail(e.target.value)} type="email"
                 placeholder="you@hospital.in" autoComplete="username" required />

          <label>Password</label>
          <input value={password} onChange={(e) => setPassword(e.target.value)} type="password"
                 placeholder="Your password" autoComplete="current-password" required />

          {error && <div className="error shake">{error}</div>}

          <button className="btn btn-grad" type="submit" disabled={submitting}>
            {submitting ? <span className="spinner spinner-sm" /> : 'Sign in →'}
          </button>
        </form>
      </div>
    </div>
  )
}
