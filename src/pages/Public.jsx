import { useState } from 'react';
import { Link, useNavigate, useParams, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { eventService, authService, errMsg } from '../services/api.js';
import useAsync from '../hooks/useAsync.js';
import { Loader, ErrorBox, Empty } from '../components/Common.jsx';
import { fmtDate, fmtTime } from '../utils/format.js';

const EventCard = ({ e }) => (
  <Link to={`/events/${e.id}`} className="card event-card">
    <div className="poster">{e.title.slice(0, 1)}</div>
    <div className="card-body">
      <h3>{e.title}</h3>
      <p className="muted">{e.venue}</p>
      <p className="muted small">{e.duration} min</p>
    </div>
  </Link>
);

export function Home() {
  const { data, loading, error, reload } = useAsync(() => eventService.list(), []);
  return (
    <>
      <section className="hero">
        <h1>Pick your seat. Keep it for five minutes. Own it.</h1>
        <p>Every seat is locked in the database the moment you choose it, so nobody else can take it while you check out.</p>
        <Link className="btn big" to="/events">Browse events</Link>
      </section>
      <h2>Now showing</h2>
      {loading ? <Loader /> : error ? <ErrorBox message={error} onRetry={reload} /> :
        <div className="grid">{data.slice(0, 4).map((e) => <EventCard key={e.id} e={e} />)}</div>}
    </>
  );
}

export function Events() {
  const [q, setQ] = useState('');
  const { data, loading, error, reload } = useAsync(() => eventService.list(q), [q]);
  return (
    <>
      <div className="row between"><h2>Events</h2>
        <input className="input search" placeholder="Search by title" value={q} onChange={(e) => setQ(e.target.value)} /></div>
      {loading ? <Loader /> : error ? <ErrorBox message={error} onRetry={reload} /> :
        data.length === 0 ? <Empty title="No events found" hint="Try a different search." /> :
        <div className="grid">{data.map((e) => <EventCard key={e.id} e={e} />)}</div>}
    </>
  );
}

export function EventDetails() {
  const { id } = useParams();
  const ev = useAsync(() => eventService.get(id), [id]);
  const shows = useAsync(() => eventService.shows(id), [id]);
  if (ev.loading) return <Loader />;
  if (ev.error) return <ErrorBox message={ev.error} onRetry={ev.reload} />;
  const e = ev.data;
  return (
    <>
      <section className="detail">
        <div className="poster big-poster">{e.title.slice(0, 1)}</div>
        <div><h1>{e.title}</h1><p className="muted">{e.venue} · {e.duration} min</p><p>{e.description}</p></div>
      </section>
      <h2>Choose a show</h2>
      {shows.loading ? <Loader /> : shows.error ? <ErrorBox message={shows.error} /> :
        shows.data.length === 0 ? <Empty title="No shows scheduled" hint="Check back soon." /> :
        <div className="grid shows">{shows.data.map((s) => (
          <Link key={s.id} to={`/shows/${s.id}/seats`} className="card show-card">
            <strong>{fmtTime(s.startTime)}</strong><span>{fmtDate(s.showDate)}</span>
            <span className="muted small">{s.availableSeats} of {s.totalSeats} seats left</span>
          </Link>))}</div>}
    </>
  );
}

function AuthForm({ mode }) {
  const { login, register } = useAuth();
  const nav = useNavigate();
  const from = useLocation().state?.from || '/events';
  const [f, setF] = useState({ name: '', email: '', password: '' });
  const [err, setErr] = useState('');
  const [busy, setBusy] = useState(false);
  const isReg = mode === 'register';
  const submit = async (ev) => {
    ev.preventDefault();
    if (isReg && f.name.trim().length < 2) return setErr('Enter your name.');
    if (!/^\S+@\S+\.\S+$/.test(f.email)) return setErr('Enter a valid email address.');
    if (f.password.length < 6) return setErr('Password needs at least 6 characters.');
    setBusy(true); setErr('');
    try {
      if (isReg) await register(f.name, f.email, f.password); else await login(f.email, f.password);
      nav(from);
    } catch (e) { setErr(errMsg(e)); } finally { setBusy(false); }
  };
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });
  return (
    <form className="card form narrow" onSubmit={submit}>
      <h2>{isReg ? 'Create your account' : 'Welcome back'}</h2>
      {err && <div className="alert error">{err}</div>}
      {isReg && <label>Name<input className="input" value={f.name} onChange={set('name')} /></label>}
      <label>Email<input className="input" type="email" value={f.email} onChange={set('email')} /></label>
      <label>Password<input className="input" type="password" value={f.password} onChange={set('password')} /></label>
      <button className="btn" disabled={busy}>{busy ? 'Please wait…' : isReg ? 'Sign up' : 'Log in'}</button>
      <p className="muted small">{isReg ? <>Already registered? <Link to="/login">Log in</Link></> : <>New here? <Link to="/register">Create an account</Link></>}</p>
    </form>
  );
}
export const Login = () => <AuthForm mode="login" />;
export const Register = () => <AuthForm mode="register" />;

export function Profile() {
  const { data, loading, error } = useAsync(() => authService.profile(), []);
  if (loading) return <Loader />;
  if (error) return <ErrorBox message={error} />;
  return (
    <div className="card form narrow"><h2>Your profile</h2>
      <p><strong>Name</strong><br />{data.name}</p><p><strong>Email</strong><br />{data.email}</p>
      <p><strong>Role</strong><br />{data.role}</p><p><strong>Member since</strong><br />{fmtDate(data.createdAt)}</p></div>
  );
}

export const NotFound = () => <Empty title="Page not found" hint="That page doesn't exist." to="/" cta="Go home" />;
