import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { Header } from './components/Header'
import { ProtectedRoute } from './routes/ProtectedRoute'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { HomePage } from './pages/HomePage'
import { EventsPage } from './pages/EventsPage'
import { CreateEventPage } from './pages/CreateEventPage'
import { EventDetailsPage } from './pages/EventDetailsPage'
import './styles/global.css'

function App() {
  return (
    <Router>
      <AuthProvider>
        <Header />
        <Routes>
          <Route path="/" element={<Navigate to="/home" replace />} />
          <Route path="/home" element={<HomePage />} />
          <Route path="/events" element={<EventsPage />} />
          <Route path="/events/new" element={<ProtectedRoute><CreateEventPage /></ProtectedRoute>} />
          <Route path="/events/:id" element={<EventDetailsPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="*" element={<Navigate to="/home" replace />} />
        </Routes>
      </AuthProvider>
    </Router>
  )
}

export default App

