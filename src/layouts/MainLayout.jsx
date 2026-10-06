import { Outlet } from 'react-router-dom';
import Navbar from '../components/Navbar.jsx';

export default function MainLayout() {
  return (
    <>
      <Navbar />
      <main className="container"><Outlet /></main>
      <footer className="footer">SeatSync: two people, one seat, one winner.</footer>
    </>
  );
}
