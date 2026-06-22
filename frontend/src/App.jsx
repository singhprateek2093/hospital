import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import PatientList from './pages/PatientList'
import PatientDetail from './pages/PatientDetail'
import NewPatient from './pages/NewPatient'
import Register from './pages/Register'
import Appointments from './pages/Appointments'
import NewAppointment from './pages/NewAppointment'
import ChangePassword from './pages/ChangePassword'

/**
 * Route map. The `roles` prop on ProtectedRoute restricts a page to certain roles
 * (UX only — the backend independently enforces every rule).
 */
export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="app-shell">
          <Navbar />
          <main className="app-main">
        <Routes>
          <Route path="/login" element={<Login />} />

          <Route path="/" element={
            <ProtectedRoute><Dashboard /></ProtectedRoute>
          } />

          <Route path="/patients" element={
            <ProtectedRoute><PatientList /></ProtectedRoute>
          } />

          <Route path="/patients/new" element={
            <ProtectedRoute roles={['ADMIN', 'RECEPTIONIST']}><NewPatient /></ProtectedRoute>
          } />

          <Route path="/patients/:id" element={
            <ProtectedRoute><PatientDetail /></ProtectedRoute>
          } />

          <Route path="/appointments" element={
            <ProtectedRoute><Appointments /></ProtectedRoute>
          } />

          <Route path="/appointments/new" element={
            <ProtectedRoute roles={['ADMIN', 'RECEPTIONIST']}><NewAppointment /></ProtectedRoute>
          } />

          <Route path="/register" element={
            <ProtectedRoute roles={['ADMIN']}><Register /></ProtectedRoute>
          } />

          <Route path="/change-password" element={
            <ProtectedRoute><ChangePassword /></ProtectedRoute>
          } />
        </Routes>
          </main>
          <Footer />
        </div>
      </BrowserRouter>
    </AuthProvider>
  )
}
