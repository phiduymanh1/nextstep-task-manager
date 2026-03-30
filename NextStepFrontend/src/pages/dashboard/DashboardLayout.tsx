import Sidebar from '@/components/layout/sidebarLayout/Sidebar';
import Navbar from '@/components/layout/navbarLayout/Navbar';
import { Outlet, useLocation } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import '@/assets/styles/Dashboard.css';

export default function DashboardLayout() {
  const location = useLocation();

  const isBoardPage = location.pathname.includes('/board/');

  return (
    <div className="dashboard">
      <Sidebar />

      <div className="dashboard-main">
        <Navbar />

        <div className="dashboard-content">
          {isBoardPage ? (
            <div className="dashboard-page">
              <Outlet />
            </div>
          ) : (
            <AnimatePresence mode="wait">
              <motion.div
                key={location.pathname}
                className="dashboard-page"
                initial={{ opacity: 0, x: 10 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -10 }}
                transition={{ duration: 0.2 }}
              >
                <Outlet />
              </motion.div>
            </AnimatePresence>
          )}
        </div>
      </div>
    </div>
  );
}
