import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { seatService, showService, errMsg } from '../services/api.js';
import useAsync from '../hooks/useAsync.js';
import { Loader, ErrorBox } from '../components/Common.jsx';
import { money, fmtDate, fmtTime } from '../utils/format.js';

export default function SeatSelection() {
  const { showId } = useParams();
  const nav = useNavigate();
  const show = useAsync(() => showService.get(showId), [showId]);
  const seats = useAsync(() => seatService.list(showId), [showId]);
  const [selected, setSelected] = useState([]);
  const [holdUntil, setHoldUntil] = useState(null);
  const [left, setLeft] = useState(0);
  const [msg, setMsg] = useState('');
  const [busy, setBusy] = useState(false);

  // Countdown is only a visual aid. The backend decides when a hold is really over.
  useEffect(() => {
    if (!holdUntil) return;
    const tick = () => {
      const s = Math.max(0, Math.round((new Date(holdUntil) - Date.now()) / 1000));
      setLeft(s);
      if (s === 0) {
        setHoldUntil(null); setSelected([]);
        setMsg('Your hold expired. Pick your seats again.');
        seats.reload();
      }
    };
    tick();
    const t = setInterval(tick, 1000);
    return () => clearInterval(t);
  }, [holdUntil]); // eslint-disable-line

  const rows = useMemo(() => {
    const map = {};
    (seats.data || []).forEach((s) => { (map[s.rowLabel] ||= []).push(s); });
    return Object.entries(map);
  }, [seats.data]);

  const byId = useMemo(() => Object.fromEntries((seats.data || []).map((s) => [s.id, s])), [seats.data]);
  const total = selected.reduce((sum, id) => sum + Number(byId[id]?.price || 0), 0);

  const toggle = (s) => {
    if (holdUntil || s.status !== 'AVAILABLE') return;
    setMsg('');
    setSelected((cur) => (cur.includes(s.id) ? cur.filter((x) => x !== s.id) : [...cur, s.id]));
  };

  const hold = async () => {
    setBusy(true); setMsg('');
    try {
      const r = await seatService.hold(showId, selected);
      setHoldUntil(r.holdExpiresAt);
    } catch (e) { setMsg(errMsg(e)); setSelected([]); seats.reload(); } finally { setBusy(false); }
  };
  const confirm = async () => {
    setBusy(true); setMsg('');
    try {
      const b = await seatService.confirm(showId, selected);
      nav(`/booking-success/${b.id}`);
    } catch (e) { setMsg(errMsg(e)); setHoldUntil(null); setSelected([]); seats.reload(); } finally { setBusy(false); }
  };

  if (show.loading || seats.loading) return <Loader />;
  if (show.error || seats.error) return <ErrorBox message={show.error || seats.error} onRetry={() => { show.reload(); seats.reload(); }} />;
  const sh = show.data;
  const mm = String(Math.floor(left / 60)).padStart(2, '0'), ss = String(left % 60).padStart(2, '0');

  return (
    <>
      <h2>{sh.eventTitle}</h2>
      <p className="muted">{sh.venue} · {fmtDate(sh.showDate)} · {fmtTime(sh.startTime)}</p>
      <div className="seat-layout">
        <section className="card hall">
          <div className="screen">SCREEN</div>
          {rows.map(([label, list]) => (
            <div className="seat-row" key={label}>
              <span className="row-label">{label}</span>
              {list.map((s) => {
                const mine = selected.includes(s.id) || s.heldByMe;
                const state = mine ? 'selected' : s.status.toLowerCase();
                return (
                  <button key={s.id} className={`seat ${state}`} disabled={!mine && s.status !== 'AVAILABLE'}
                    onClick={() => toggle(s)} title={`${s.label} · ${money(s.price)}`}>{s.seatNumber}</button>
                );
              })}
            </div>
          ))}
          <div className="legend">
            <span><i className="seat available" />Available</span><span><i className="seat selected" />Selected</span>
            <span><i className="seat held" />Held by someone</span><span><i className="seat booked" />Booked</span>
          </div>
        </section>
        <aside className="card summary">
          <h3>Your selection</h3>
          {msg && <div className="alert error">{msg}</div>}
          {selected.length === 0 ? <p className="muted">Tap seats to select them.</p> : (
            <>
              <p>{selected.map((id) => byId[id]?.label).join(', ')}</p>
              <p className="total">{money(total)}</p>
            </>
          )}
          {holdUntil ? (
            <>
              <div className="timer">{mm}:{ss}</div>
              <p className="muted small">Seats are held for you. Confirm before the timer ends.</p>
              <button className="btn" disabled={busy} onClick={confirm}>{busy ? 'Booking…' : 'Confirm booking'}</button>
            </>
          ) : (
            <button className="btn" disabled={busy || selected.length === 0} onClick={hold}>
              {busy ? 'Holding…' : 'Hold seats for 5 minutes'}</button>
          )}
          <p className="muted small">The server recalculates the price when you confirm.</p>
        </aside>
      </div>
    </>
  );
}
