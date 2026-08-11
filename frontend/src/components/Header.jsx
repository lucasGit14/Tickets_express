import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import '../styles/header.css'

export function Header() {
  const { user, logout, isAuthenticated } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <header className="header">
      <div className="header-container">
        <Link to="/" className="logo">
          Tickets Express
        </Link>
        <nav className="nav">
          <Link to="/events">Eventos</Link>
          {isAuthenticated && <Link to="/my-events">Meus Eventos</Link>}
        </nav>
        <div className="auth-section">
          {isAuthenticated ? (
            <>
              <span className="user-name">{user?.name}</span>
              <button onClick={handleLogout} className="logout-btn">
                Logout
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
