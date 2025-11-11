import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080/api',
  timeout: 20000,
});

api.interceptors.request.use(config => {
  const auth = useAuthStore();
  const token = auth.token;
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;