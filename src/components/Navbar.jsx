import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth();
  const nav = useNavigate();
  return (
    <header className="navbar">
      <Link to="/" className="brand">Seat<span>Sync</span></Link>
      <nav>
        <NavLink to="/events">Events</NavLink>
        {user && !isAdmin && <NavLink to="/my-bookings">My bookings</NavLink>}
        {isAdmin && <NavLink to="/admin">Admin</NavLink>}
        {user ? (
          <>
            <NavLink to="/profile">{user.name}</NavLink>
            <button className="btn ghost small" onClick={() => { logout(); nav('/'); }}>Log out</button>
          </>
        ) : (
          <>
            <NavLink to="/login">Log in</NavLink>
            <Link className="btn small" to="/register">Sign up</Link>
          </>
        )}
      </nav>
    </header>
  );
}
