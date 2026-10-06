import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { bookingService, errMsg } from '../services/api.js';
import useAsync from '../hooks/useAsync.js';
import { Loader, ErrorBox, Empty, StatusBadge } from '../components/Common.jsx';
import { money, fmtDate, fmtTime } from '../utils/format.js';

export function BookingSuccess() {
  const { id } = useParams();
  const { data, loading, error } = useAsync(() => bookingService.get(id), [id]);
  if (loading) return <Loader />;
  if (error) return <ErrorBox message={error} />;
  return (
    <div className="card form narrow center">
      <div className="tick">✓</div><h2>You're booked</h2>
      <p>{data.eventTitle} · {fmtDate(data.showDate)} · {fmtTime(data.startTime)}</p>
      <p><strong>Seats:</strong> {data.seats.join(', ')}</p>
      <p className="total">{money(data.totalAmount)}</p>
      <Link className="btn" to={`/bookings/${data.id}`}>View booking</Link>
    </div>
  );
}

export function MyBookings() {
  const { data, loading, error, reload } = useAsync(() => bookingService.list(), []);
  if (loading) return <Loader />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;
  return (
    <>
      <h2>My bookings</h2>
      {data.length === 0 ? <Empty title="No bookings yet" hint="Find an event and pick your seats." to="/events" cta="Browse events" /> :
        <div className="list">{data.map((b) => (
          <Link key={b.id} to={`/bookings/${b.id}`} className="card row between">
            <div><strong>#{b.id} · {b.eventTitle}</strong>
              <p className="muted small">{fmtDate(b.showDate)} · {fmtTime(b.startTime)} · {b.seats.join(', ')}</p></div>
            <div className="right"><StatusBadge status={b.status} /><p>{money(b.totalAmount)}</p></div>
          </Link>))}</div>}
    </>
  );
}

export function BookingDetails() {
  const { id } = useParams();
  const { data, loading, error, reload } = useAsync(() => bookingService.get(id), [id]);
  const [msg, setMsg] = useState('');
  if (loading) return <Loader />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;
  const cancel = async () => {
    if (!window.confirm('Cancel this booking? Your seats will be released.')) return;
    try { await bookingService.cancel(id); setMsg(''); reload(); } catch (e) { setMsg(errMsg(e)); }
  };
  return (
    <div className="card form narrow">
      <div className="row between"><h2>Booking #{data.id}</h2><StatusBadge status={data.status} /></div>
      {msg && <div className="alert error">{msg}</div>}
      <p><strong>{data.eventTitle}</strong><br />{data.venue}</p>
      <p>{fmtDate(data.showDate)} · {fmtTime(data.startTime)}</p>
      <p><strong>Seats:</strong> {data.seats.join(', ')}</p>
      <p><strong>Total:</strong> {money(data.totalAmount)}</p>
      <p className="muted small">Booked on {fmtDate(data.createdAt)}</p>
      {data.status === 'CONFIRMED' && <button className="btn danger" onClick={cancel}>Cancel booking</button>}
      <Link to="/my-bookings">Back to my bookings</Link>
    </div>
  );
}
