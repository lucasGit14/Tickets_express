import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import '../styles/header.css'

export function Header() {
  const { user, logout, isAuthenticated, loading } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const role = user?.role

  return (
    <header className="header">
      <div className="header-container">
        <Link to="/" className="logo">
          Tickets Express
        </Link>
        <nav className="nav">
          <Link to="/events">Eventos</Link>
          {isAuthenticated && role === 'ORGANIZER' && (
            <>
              <Link to="/my-events">Meus Eventos</Link>
              <Link to="/events/new">Criar evento</Link>
            </>
          )}
          {isAuthenticated && role === 'CUSTOMER' && (
            <Link to="/my-tickets">Meus Ingressos</Link>
          )}
          {isAuthenticated && role === 'GATEKEEPER' && (
            <Link to="/validate">Validar</Link>
          )}
        </nav>
        <div className="auth-section">
          {loading ? (
            <span className="user-name">...</span>
          ) : isAuthenticated ? (
            <>
              <span className="user-name">{user?.name}</span>
              <button type="button" onClick={handleLogout} className="logout-btn">
                Sair
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="login-link">
                Login
              </Link>
              <Link to="/register" className="register-link">
                Cadastro
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
