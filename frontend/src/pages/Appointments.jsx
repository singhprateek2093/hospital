import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client from '../api/client'
import { useAuth } from '../context/AuthContext'
import Pagination from '../components/Pagination'
import Spinner from '../components/Spinner'

const PAGE_SIZE = 10

const STATUS_CLASS = {
  SCHEDULED: 'pill pill-blue',
  COMPLETED: 'pill pill-green',
  CANCELLED: 'pill pill-red',
}

export default function Appointments() {
  const { user } = useAuth()
  const [data, setData] = useState(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)

  const isStaff = user.role === 'ADMIN' || user.role === 'RECEPTIONIST'

  function load(p = page) {
    setLoading(true)
    client
      .get('/api/appointments', { params: { page: p, size: PAGE_SIZE } })
      .then((res) => setData(res.data))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load(page) }, [page])

  async function setStatus(id, status) {
    await client.patch(`/api/appointments/${id}/status`, { status })
    load(page) // refresh
  }

  const appts = data?.content ?? []

  return (
    <div className="container fade-in">
      <div className="row-between">
        <h1>{user.role === 'DOCTOR' ? 'My Schedule' : 'Appointments'}</h1>
        {isStaff && <Link className="btn" to="/appointments/new">+ Book appointment</Link>}
      </div>

      {loading ? (
        <Spinner />
      ) : appts.length === 0 ? (
        <p className="muted">No appointments.</p>
      ) : (
        <>
          <table className="table">
            <thead>
              <tr>
                <th>When</th><th>Patient</th><th>Doctor</th><th>Reason</th><th>Status</th><th></th>
              </tr>
            </thead>
            <tbody>
              {appts.map((a) => (
                <tr key={a.id}>
                  <td>{new Date(a.scheduled_at).toLocaleString()}</td>
                  <td>{a.patient_name}</td>
                  <td>{a.doctor_name}</td>
                  <td>{a.reason}</td>
                  <td><span className={STATUS_CLASS[a.status]}>{a.status}</span></td>
                  <td>
                    {a.status === 'SCHEDULED' && (
                      <div className="actions">
                        <button className="btn-link" onClick={() => setStatus(a.id, 'COMPLETED')}>Complete</button>
                        <button className="btn-link danger" onClick={() => setStatus(a.id, 'CANCELLED')}>Cancel</button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <Pagination data={data} onPage={setPage} />
        </>
      )}
    </div>
  )
}
