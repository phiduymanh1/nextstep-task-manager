import { clearAccessToken } from '@/utils/token';
import { logout } from '@/services/auth.service';

export const handleLogout = async () => {
  try {
    await logout();
  } catch {
    // ignore error (vd token hết hạn)
  } finally {
    clearAccessToken();
    window.location.href = '/login';
  }
};
