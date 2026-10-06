export const money = (n) => `₹${Number(n || 0).toLocaleString('en-IN')}`;
export const fmtDate = (d) => (d ? new Date(d).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }) : '');
export const fmtTime = (t) => {
  if (!t) return '';
  const [h, m] = t.split(':');
  const hh = Number(h);
  return `${hh % 12 || 12}:${m} ${hh >= 12 ? 'PM' : 'AM'}`;
};
