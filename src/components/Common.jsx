import { Link, Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export const Loader = () => <div className="center-msg"><div className="spinner" />Loading…</div>;
export const ErrorBox = ({ message, onRetry }) => (
  <div className="alert error">{message}{onRetry && <button className="btn small" onClick={onRetry}>Try again</button>}</div>
);
export const Empty = ({ title, hint, to, cta }) => (
  <div className="empty"><h3>{title}</h3><p>{hint}</p>{to && <Link className="btn" to={to}>{cta}</Link>}</div>
);
export const StatusBadge = ({ status }) => <span className={`badge ${String(status).toLowerCase()}`}>{status}</span>;

export function ProtectedRoute({ admin }) {
  const { user, isAdmin } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (admin && !isAdmin) return <Navigate to="/" replace />;
  return <Outlet />;
}
