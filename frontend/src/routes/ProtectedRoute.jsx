import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

export function ProtectedRoute({ children, roles }) {
  const { isAuthenticated, user, loading } = useAuth()

  if (loading) {
    return (
      <div className="page-container">
        <p className="state-message">Carregando...</p>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  if (roles?.length > 0 && !roles.includes(user?.role)) {
    return <Navigate to="/home" replace />
  }

  return children
}
