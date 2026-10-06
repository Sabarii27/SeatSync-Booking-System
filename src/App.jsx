import { Routes, Route } from 'react-router-dom';
import MainLayout from './layouts/MainLayout.jsx';
import { ProtectedRoute } from './components/Common.jsx';
import { Home, Login, Register, Events, EventDetails, Profile, NotFound } from './pages/Public.jsx';
import SeatSelection from './pages/SeatSelection.jsx';
import { BookingSuccess, MyBookings, BookingDetails } from './pages/Bookings.jsx';
import { AdminDashboard, AdminEvents, AdminShows, AdminBookings, AdminUsers } from './pages/Admin.jsx';

export default function App() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/events" element={<Events />} />
        <Route path="/events/:id" element={<EventDetails />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/shows/:showId/seats" element={<SeatSelection />} />
          <Route path="/booking-success/:id" element={<BookingSuccess />} />
          <Route path="/my-bookings" element={<MyBookings />} />
          <Route path="/bookings/:id" element={<BookingDetails />} />
          <Route path="/profile" element={<Profile />} />
        </Route>
        <Route element={<ProtectedRoute admin />}>
          <Route path="/admin" element={<AdminDashboard />} />
          <Route path="/admin/events" element={<AdminEvents />} />
          <Route path="/admin/events/:id/shows" element={<AdminShows />} />
          <Route path="/admin/bookings" element={<AdminBookings />} />
          <Route path="/admin/users" element={<AdminUsers />} />
        </Route>
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  );
}
