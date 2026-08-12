import { useState, useCallback, useEffect } from 'react'
import { AuthContext } from './auth-context'
import { authAPI } from '../api/client'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(() => localStorage.getItem('token'))
  const [loading, setLoading] = useState(() => !!localStorage.getItem('token'))

  useEffect(() => {
    let cancelled = false
    const stored = localStorage.getItem('token')

    if (!stored) {
      return undefined
    }

    authAPI
      .me()
      .then((res) => {
        if (cancelled) return
        const data = res.data
        setUser({
          id: data.id,
          name: data.name,
          email: data.email,
          role: data.role,
        })
        setToken(stored)
      })
      .catch(() => {
        if (cancelled) return
        localStorage.removeItem('token')
        setToken(null)
        setUser(null)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  const login = useCallback((newToken, userData) => {
    setToken(newToken)
    setUser({
      id: userData.id,
      name: userData.name,
      email: userData.email,
      role: userData.role,
    })
    localStorage.setItem('token', newToken)
  }, [])

  const logout = useCallback(() => {
    setToken(null)
    setUser(null)
    localStorage.removeItem('token')
  }, [])

  const value = {
    user,
    token,
    loading,
    login,
    logout,
    isAuthenticated: !!token && !!user,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
