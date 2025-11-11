import { defineStore } from 'pinia';
import api from '@/api/axios';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  email: string;
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    email: localStorage.getItem('email') || '',
    isLoading: false,
    error: ''
  }),

  getters: {
    isAuthenticated: (state) => !!state.token
  },

  actions: {
    async login(request: LoginRequest) {
      this.isLoading = true;
      this.error = '';
      try {
        const response = await api.post<AuthResponse>('/auth/login', request);
        const { token, email } = response.data;
        
        // 保存到状态和本地存储
        this.token = token;
        this.email = email;
        localStorage.setItem('token', token);
        localStorage.setItem('email', email);
        
        return true;
      } catch (error: any) {
        this.error = error.response?.data || '登录失败';
        return false;
      } finally {
        this.isLoading = false;
      }
    },

    async register(request: RegisterRequest) {
      this.isLoading = true;
      this.error = '';
      try {
        await api.post('/auth/register', request);
        return true;
      } catch (error: any) {
        this.error = error.response?.data || '注册失败';
        return false;
      } finally {
        this.isLoading = false;
      }
    },

    logout() {
      this.token = '';
      this.email = '';
      localStorage.removeItem('token');
      localStorage.removeItem('email');
    },

    loadFromLocal() {
      this.token = localStorage.getItem('token') || '';
      this.email = localStorage.getItem('email') || '';
    }
  }
});