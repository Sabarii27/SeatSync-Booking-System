import { createContext, useContext, useState } from 'react';
import { authService } from '../services/api.js';

const AuthContext = createContext(null);
export const useAuth = () => useContext(AuthContext);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => JSON.parse(localStorage.getItem('user') || 'null'));
  const save = (data) => {
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(data.user));
    setUser(data.user);
  };
  const login = async (email, password) => save(await authService.login({ email, password }));
  const register = async (name, email, password) => save(await authService.register({ name, email, password }));
  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };
  return (
    <AuthContext.Provider value={{ user, role: user?.role, isAdmin: user?.role === 'ADMIN', login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
