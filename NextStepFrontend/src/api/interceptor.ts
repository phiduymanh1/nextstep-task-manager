import type {
  AxiosResponse,
  AxiosError,
  InternalAxiosRequestConfig,
} from 'axios';
import axiosInstance from './axios';
import { getAccessToken } from '@/utils/token';
import { HTTP_HEADER, MIME_TYPE } from '@/constants/Const';
import type { ApiResponse } from '@/types/api.type';
import { refreshToken } from '@/services/auth.service';
import { handleLogout } from '@/utils/auth';
import { notifyError } from '@/utils/notify';

let isRefreshing = false;
let failedQueue: {
  resolve: (value: string) => void;
  reject: (reason?: unknown) => void;
}[] = [];

const processQueue = (error: unknown, token?: string) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else if (token) {
      prom.resolve(token);
    }
  });

  failedQueue = [];
};

/**Request interceptors, Run before the request is sent */
axiosInstance.interceptors.request.use((config) => {
  // Get token from local storage
  const token = getAccessToken();
  // Ensure headers are not undefined
  config.headers = config.headers ?? {};
  // If token exists, add Bearer token to Authorization header
  if (token) {
    config.headers[HTTP_HEADER.AUTHORIZATION] = `Bearer ${token}`;
  }
  // Set Content-Type to application/json if data is present
  if (config.data) {
    config.headers[HTTP_HEADER.CONTENT_TYPE] = MIME_TYPE.JSON;
  }
  return config; // Return modified config
});

/**Response interceptor, Run after the request is sent */
axiosInstance.interceptors.response.use(
  // Success (status code 2xx)
  (response: AxiosResponse<ApiResponse<unknown>>) => {
    const { metaData } = response.data;
    // const method = response.config.method?.toUpperCase();
    // If unsuccessful response, reject with metaData
    if (!metaData.success) {
      notifyError(metaData.message || 'Something went wrong');
      return Promise.reject(metaData);
    }

    // if (metaData.success && method !== 'GET') {
    //   notifySuccess(metaData.message || 'Discussion successful!');
    // }
    // Success response, return it
    return response;
  },
  // Error (status code outside 2xx)
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const originalRequest = error.config as
      | (InternalAxiosRequestConfig & { _retry?: boolean })
      | undefined;

    if (!error.response) {
      notifyError('Network error. Please check your connection.');
      return Promise.reject(error);
    }

    const status = error.response?.status;
    const backendMessage =
      error.response?.data?.metaData?.message || 'Unexpected error';

    // if the server return 401 Unauthorized, clear the access token
    if (
      status === 401 &&
      originalRequest &&
      !originalRequest._retry &&
      !originalRequest.url?.includes('/auth')
    ) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${token}`;
          }
          return axiosInstance(originalRequest);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const res = await refreshToken();
        const newAccessToken = res.accessToken;

        localStorage.setItem('accessToken', newAccessToken);

        processQueue(null, newAccessToken);

        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        }

        return axiosInstance(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError);

        notifyError('Session expired. Please login again.');

        await handleLogout();
        window.location.href = '/login';

        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    /**
     * 403
     */
    if (status === 403) {
      notifyError(backendMessage || 'You do not have permission.');
      return Promise.reject(error);
    }

    /**
     * 500
     */
    if (status === 500) {
      notifyError(backendMessage || 'Server error. Please try again later.');
      return Promise.reject(error);
    }

    /**
     * Other errors (400, 404...)
     */
    notifyError(backendMessage);

    return Promise.reject(error);
  }
);
