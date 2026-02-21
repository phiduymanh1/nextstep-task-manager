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
    // If unsuccessful response, reject with metaData
    if (!metaData.success) {
      return Promise.reject(metaData);
    }
    // Success response, return it
    return response;
  },
  // Error (status code outside 2xx)
  async (error: AxiosError) => {
    const originalRequest = error.config as
      | (InternalAxiosRequestConfig & { _retry?: boolean })
      | undefined;

    if (!originalRequest) {
      return Promise.reject(error);
    }
    // if the server return 401 Unauthorized, clear the access token
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !originalRequest.url?.includes('/auth/refresh')
    ) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return axiosInstance(originalRequest);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // 👇 CALL REFRESH API
        const res = await refreshToken();
        const newAccessToken = res.accessToken;

        // save token
        localStorage.setItem('accessToken', newAccessToken);

        processQueue(newAccessToken);

        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError);
        await handleLogout();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }
    return Promise.reject(error);
  }
);
