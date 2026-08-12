import { createContext, useContext, useState } from 'react';

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(false);

  const login = async (email, password) => {
    setLoading(true);
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });

    if (!response.ok) throw new Error('Falha na autenticação');

    const data = await response.json();
    setUser(data.user || data);
    if (data.token) localStorage.setItem('token', data.token);
    setLoading(false);
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('token');
  };

  const isAuthenticated = !!user;

  return (
      <AuthContext.Provider value={{ user, login, logout, isAuthenticated, loading }}>
        {children}
      </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  return context || { user: null, login: async () => {}, logout: () => {} };
}

export default AuthContext;