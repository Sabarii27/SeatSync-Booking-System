import axios from 'axios';

const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api' });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401 && localStorage.getItem('token')) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export const errMsg = (e) => e.response?.data?.message || e.message || 'Something went wrong';
const d = (p) => p.then((r) => r.data);

export const authService = {
  login: (body) => d(api.post('/auth/login', body)),
  register: (body) => d(api.post('/auth/register', body)),
  profile: () => d(api.get('/profile')),
};
export const eventService = {
  list: (q) => d(api.get('/events', { params: q ? { q } : {} })),
  get: (id) => d(api.get(`/events/${id}`)),
  shows: (id) => d(api.get(`/events/${id}/shows`)),
};
export const showService = { get: (id) => d(api.get(`/shows/${id}`)) };
export const seatService = {
  list: (showId) => d(api.get(`/shows/${showId}/seats`)),
  hold: (showId, seatIds) => d(api.post(`/shows/${showId}/holds`, { seatIds })),
  confirm: (showId, seatIds) => d(api.post(`/shows/${showId}/confirm`, { seatIds })),
};
export const bookingService = {
  list: () => d(api.get('/bookings')),
  get: (id) => d(api.get(`/bookings/${id}`)),
  cancel: (id) => d(api.post(`/bookings/${id}/cancel`)),
};
export const adminService = {
  createEvent: (b) => d(api.post('/admin/events', b)),
  updateEvent: (id, b) => d(api.put(`/admin/events/${id}`, b)),
  deleteEvent: (id) => d(api.delete(`/admin/events/${id}`)),
  createShow: (b) => d(api.post('/admin/shows', b)),
  deleteShow: (id) => d(api.delete(`/admin/shows/${id}`)),
  users: () => d(api.get('/admin/users')),
  bookings: () => d(api.get('/admin/bookings')),
  setBookingStatus: (id, status) => d(api.patch(`/admin/bookings/${id}/status`, { status })),
};
export default api;
