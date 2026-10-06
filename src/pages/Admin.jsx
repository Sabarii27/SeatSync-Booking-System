import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { adminService, eventService, errMsg } from '../services/api.js';
import useAsync from '../hooks/useAsync.js';
import { Loader, ErrorBox, Empty, StatusBadge } from '../components/Common.jsx';
import { money, fmtDate, fmtTime } from '../utils/format.js';

export function AdminDashboard() {
  const ev = useAsync(() => eventService.list(), []);
  const bk = useAsync(() => adminService.bookings(), []);
  const us = useAsync(() => adminService.users(), []);
  const revenue = (bk.data || []).filter((b) => b.status === 'CONFIRMED').reduce((s, b) => s + Number(b.totalAmount), 0);
  return (
    <>
      <h2>Admin dashboard</h2>
      <div className="grid stats">
        <Link to="/admin/events" className="card stat"><b>{ev.data?.length ?? '–'}</b>Events</Link>
        <Link to="/admin/bookings" className="card stat"><b>{bk.data?.length ?? '–'}</b>Bookings</Link>
        <Link to="/admin/users" className="card stat"><b>{us.data?.length ?? '–'}</b>Users</Link>
        <div className="card stat"><b>{money(revenue)}</b>Confirmed revenue</div>
      </div>
    </>
  );
}

const blank = { title: '', description: '', venue: '', duration: 120 };

export function AdminEvents() {
  const { data, loading, error, reload } = useAsync(() => eventService.list(), []);
  const [f, setF] = useState(blank);
  const [editId, setEditId] = useState(null);
  const [msg, setMsg] = useState('');
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });
  const save = async (e) => {
    e.preventDefault();
    if (!f.title.trim() || !f.venue.trim()) return setMsg('Title and venue are required.');
    try {
      const body = { ...f, duration: Number(f.duration) };
      editId ? await adminService.updateEvent(editId, body) : await adminService.createEvent(body);
      setF(blank); setEditId(null); setMsg(''); reload();
    } catch (er) { setMsg(errMsg(er)); }
  };
  const del = async (id) => {
    if (!window.confirm('Delete this event and all its shows?')) return;
    try { await adminService.deleteEvent(id); reload(); } catch (er) { setMsg(errMsg(er)); }
  };
  return (
    <>
      <h2>Manage events</h2>
      <form className="card form" onSubmit={save}>
        <h3>{editId ? 'Edit event' : 'New event'}</h3>
        {msg && <div className="alert error">{msg}</div>}
        <div className="two">
          <label>Title<input className="input" value={f.title} onChange={set('title')} /></label>
          <label>Venue<input className="input" value={f.venue} onChange={set('venue')} /></label>
        </div>
        <label>Description<textarea className="input" rows="3" value={f.description} onChange={set('description')} /></label>
        <label>Duration (minutes)<input className="input" type="number" min="1" value={f.duration} onChange={set('duration')} /></label>
        <div className="row"><button className="btn">{editId ? 'Save changes' : 'Create event'}</button>
          {editId && <button type="button" className="btn ghost" onClick={() => { setEditId(null); setF(blank); }}>Cancel</button>}</div>
      </form>
      {loading ? <Loader /> : error ? <ErrorBox message={error} onRetry={reload} /> :
        data.length === 0 ? <Empty title="No events yet" hint="Create the first one above." /> :
        <div className="table-wrap"><table><thead><tr><th>Title</th><th>Venue</th><th>Min</th><th></th></tr></thead><tbody>
          {data.map((e) => (<tr key={e.id}><td>{e.title}</td><td>{e.venue}</td><td>{e.duration}</td>
            <td className="actions"><Link className="btn small ghost" to={`/admin/events/${e.id}/shows`}>Shows</Link>
              <button className="btn small ghost" onClick={() => { setEditId(e.id); setF({ title: e.title, description: e.description || '', venue: e.venue, duration: e.duration }); window.scrollTo(0, 0); }}>Edit</button>
              <button className="btn small danger" onClick={() => del(e.id)}>Delete</button></td></tr>))}
        </tbody></table></div>}
    </>
  );
}

export function AdminShows() {
  const { id } = useParams();
  const ev = useAsync(() => eventService.get(id), [id]);
  const shows = useAsync(() => eventService.shows(id), [id]);
  const [f, setF] = useState({ showDate: '', startTime: '10:00', endTime: '12:30', rows: 4, seatsPerRow: 5, price: 250 });
  const [msg, setMsg] = useState('');
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });
  const create = async (e) => {
    e.preventDefault();
    if (!f.showDate) return setMsg('Pick a date.');
    try {
      await adminService.createShow({ eventId: Number(id), ...f, rows: Number(f.rows), seatsPerRow: Number(f.seatsPerRow), price: Number(f.price) });
      setMsg(''); shows.reload();
    } catch (er) { setMsg(errMsg(er)); }
  };
  const del = async (sid) => {
    if (!window.confirm('Delete this show?')) return;
    try { await adminService.deleteShow(sid); shows.reload(); } catch (er) { setMsg(errMsg(er)); }
  };
  return (
    <>
      <h2>Shows{ev.data ? ` for ${ev.data.title}` : ''}</h2>
      <form className="card form" onSubmit={create}>
        <h3>New show (seats are generated automatically)</h3>
        {msg && <div className="alert error">{msg}</div>}
        <div className="three">
          <label>Date<input className="input" type="date" value={f.showDate} onChange={set('showDate')} /></label>
          <label>Start<input className="input" type="time" value={f.startTime} onChange={set('startTime')} /></label>
          <label>End<input className="input" type="time" value={f.endTime} onChange={set('endTime')} /></label>
          <label>Rows<input className="input" type="number" min="1" max="26" value={f.rows} onChange={set('rows')} /></label>
          <label>Seats per row<input className="input" type="number" min="1" max="30" value={f.seatsPerRow} onChange={set('seatsPerRow')} /></label>
          <label>Price (₹)<input className="input" type="number" min="1" value={f.price} onChange={set('price')} /></label>
        </div>
        <button className="btn">Create show</button>
      </form>
      {shows.loading ? <Loader /> : shows.error ? <ErrorBox message={shows.error} /> :
        shows.data.length === 0 ? <Empty title="No shows yet" hint="Create one above." /> :
        <div className="table-wrap"><table><thead><tr><th>Date</th><th>Time</th><th>Seats left</th><th></th></tr></thead><tbody>
          {shows.data.map((s) => (<tr key={s.id}><td>{fmtDate(s.showDate)}</td><td>{fmtTime(s.startTime)}</td>
            <td>{s.availableSeats}/{s.totalSeats}</td>
            <td className="actions"><button className="btn small danger" onClick={() => del(s.id)}>Delete</button></td></tr>))}
        </tbody></table></div>}
    </>
  );
}

export function AdminBookings() {
  const { data, loading, error, reload } = useAsync(() => adminService.bookings(), []);
  const [msg, setMsg] = useState('');
  const change = async (id, status) => {
    try { await adminService.setBookingStatus(id, status); setMsg(''); reload(); } catch (e) { setMsg(errMsg(e)); }
  };
  if (loading) return <Loader />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;
  return (
    <>
      <h2>All bookings</h2>
      {msg && <div className="alert error">{msg}</div>}
      {data.length === 0 ? <Empty title="No bookings yet" hint="Bookings appear here as users confirm seats." /> :
        <div className="table-wrap"><table><thead><tr><th>#</th><th>User</th><th>Event</th><th>Seats</th><th>Amount</th><th>Status</th><th></th></tr></thead><tbody>
          {data.map((b) => (<tr key={b.id}><td>{b.id}</td><td>{b.userEmail}</td><td>{b.eventTitle}<br /><span className="muted small">{fmtDate(b.showDate)} {fmtTime(b.startTime)}</span></td>
            <td>{b.seats.join(', ')}</td><td>{money(b.totalAmount)}</td><td><StatusBadge status={b.status} /></td>
            <td><select className="input small" value="" onChange={(e) => e.target.value && change(b.id, e.target.value)}>
              <option value="">Change…</option><option>CONFIRMED</option><option>CANCELLED</option><option>EXPIRED</option></select></td></tr>))}
        </tbody></table></div>}
    </>
  );
}

export function AdminUsers() {
  const { data, loading, error, reload } = useAsync(() => adminService.users(), []);
  if (loading) return <Loader />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;
  return (
    <>
      <h2>Users</h2>
      <div className="table-wrap"><table><thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Joined</th></tr></thead><tbody>
        {data.map((u) => (<tr key={u.id}><td>{u.name}</td><td>{u.email}</td><td>{u.role}</td><td>{fmtDate(u.createdAt)}</td></tr>))}
      </tbody></table></div>
    </>
  );
}
