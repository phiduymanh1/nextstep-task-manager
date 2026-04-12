import { Routes, Route } from 'react-router-dom';
import Home from '@/pages/Home';
import Login from './pages/login/Login';
import Dashboard from '@/pages/dashboard/Dashboard';
import { Toaster } from 'react-hot-toast';
import ProfilePage from './pages/profile/ProfilePage';
import DashboardLayout from './pages/dashboard/DashboardLayout';
import WorkspaceHome from './pages/workspace/WorkspaceHome';
import BoardDetail from './pages/board/BoardDetail';

function App() {
  return (
    <>
      <Toaster
        position="top-center"
        toastOptions={{
          duration: 3000,
          style: {
            borderRadius: '10px',
            background: '#111',
            color: '#fff',
            fontSize: '14px',
          },
        }}
      />

      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/" element={<DashboardLayout />}>
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="workspace/:slug/home" element={<WorkspaceHome />} />
          <Route
            path="workspace/:slug/board/:boardSlug"
            element={<BoardDetail />}
          />
        </Route>
      </Routes>
    </>
  );
}

export default App;
