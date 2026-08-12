import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { Header } from './components/Header'
import { ProtectedRoute } from './routes/ProtectedRoute'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { HomePage } from './pages/HomePage'
import { EventsPage } from './pages/EventsPage'
import { CreateEventPage } from './pages/CreateEventPage'
import { EditEventPage } from './pages/EditEventPage'
import { EventDetailsPage } from './pages/EventDetailsPage'
import { MyEventsPage } from './pages/MyEventsPage'
import { PaymentPage } from './pages/PaymentPage'
import { MyTicketsPage } from './pages/MyTicketsPage'
import { TicketDetailPage } from './pages/TicketDetailPage'
import { ValidateTicketPage } from './pages/ValidateTicketPage'
import './styles/global.css'
import './styles/auth.css'
import './styles/pages.css'

function App() {
  return (
    <Router>
      <AuthProvider>
        <Header />
        <Routes>
          <Route path="/" element={<Navigate to="/home" replace />} />
          <Route path="/home" element={<HomePage />} />
          <Route path="/events" element={<EventsPage />} />
          <Route
            path="/events/new"
            element={
              <ProtectedRoute roles={['ORGANIZER']}>
                <CreateEventPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/events/:id/edit"
            element={
              <ProtectedRoute roles={['ORGANIZER']}>
                <EditEventPage />
              </ProtectedRoute>
            }
          />
          <Route path="/events/:id" element={<EventDetailsPage />} />
          <Route
            path="/my-events"
            element={
              <ProtectedRoute roles={['ORGANIZER']}>
                <MyEventsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/payment/:id"
            element={
              <ProtectedRoute>
                <PaymentPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/my-tickets"
            element={
              <ProtectedRoute roles={['CUSTOMER']}>
                <MyTicketsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/tickets/:id"
            element={
              <ProtectedRoute>
                <TicketDetailPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/validate"
            element={
              <ProtectedRoute roles={['GATEKEEPER']}>
                <ValidateTicketPage />
              </ProtectedRoute>
            }
          />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="*" element={<Navigate to="/home" replace />} />
        </Routes>
      </AuthProvider>
    </Router>
  )
}

export default App
