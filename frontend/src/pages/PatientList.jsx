import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client from '../api/client'
import { useAuth } from '../context/AuthContext'
import Pagination from '../components/Pagination'
import Spinner from '../components/Spinner'

const PAGE_SIZE = 10

export default function PatientList() {
  const { user } = useAuth()
  const [data, setData] = useState(null)   // PagedResponse
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)

  function load(p = page, q = search) {
    setLoading(true)
    client
      .get('/api/patients', { params: { page: p, size: PAGE_SIZE, ...(q ? { search: q } : {}) } })
      .then((res) => setData(res.data))
      .finally(() => setLoading(false))
  }

  // Reload whenever the page changes.
  useEffect(() => { load(page, search) }, [page])

  function handleSearch(e) {
    e.preventDefault()
    setPage(0)
    load(0, search)
  }

  const patients = data?.content ?? []

  return (
    <div className="container fade-in">
      <h1>{user.role === 'DOCTOR' ? 'My Patients' : 'Patients'}</h1>

      <form className="searchbar" onSubmit={handleSearch}>
        <input
          placeholder="Search by name…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <button className="btn" type="submit">Search</button>
        {search && (
          <button type="button" className="btn-link" onClick={() => { setSearch(''); setPage(0); load(0, '') }}>
            Clear
          </button>
        )}
      </form>

      {loading ? (
        <Spinner />
      ) : patients.length === 0 ? (
        <p className="muted">No patients found.</p>
      ) : (
        <>
          <table className="table">
            <thead>
              <tr>
                <th>Name</th><th>Age</th><th>Gender</th><th>Contact</th><th>Doctor</th><th></th>
              </tr>
            </thead>
            <tbody>
              {patients.map((p) => (
                <tr key={p.id}>
                  <td>{p.name}</td>
                  <td>{p.age}</td>
                  <td>{p.gender}</td>
                  <td>{p.contact}</td>
                  <td>{p.assigned_doctor_name || <span className="muted">—</span>}</td>
                  <td><Link to={`/patients/${p.id}`}>View</Link></td>
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
