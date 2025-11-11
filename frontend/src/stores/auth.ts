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
  username: string;
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userEmail: localStorage.getItem('userEmail') || '',
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
        const { token, username } = response.data;
        
        // 保存到状态和本地存储
        this.token = token;
        this.userEmail = username;
        localStorage.setItem('token', token);
        localStorage.setItem('userEmail', username);
        
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
      this.userEmail = '';
      localStorage.removeItem('token');
      localStorage.removeItem('userEmail');
    },

    loadFromLocal() {
      this.token = localStorage.getItem('token') || '';
      this.userEmail = localStorage.getItem('userEmail') || '';
    }
  }
});