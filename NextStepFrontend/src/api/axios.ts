import axios from 'axios';
import { MIME_TYPE } from '@/constants/Const';

// Configured axios instance
const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL, // Api base URL from environment
  timeout: 10000,                        // Max 10 seconds timeout
  withCredentials: true,                 // Send cookies + credentials in CORS requests
  headers: {
    Accept: MIME_TYPE.JSON,              // Only accept JSON responses
  },
});

export default axiosInstance;
