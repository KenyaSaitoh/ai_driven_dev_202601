import api from './api';
import { LoginRequest, LoginResponse, RegisterRequest } from '../types';

export const authService = {
  // ログイン
  login: async (data: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/auth/login', data);
    return response.data;
  },

  // ログアウト
  logout: async (): Promise<void> => {
    await api.post('/auth/logout');
  },

  // 新規登録
  register: async (data: RegisterRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/auth/register', data);
    return response.data;
  },

  // 現在のユーザー情報取得
  getCurrentUser: async (): Promise<LoginResponse> => {
    const response = await api.get<LoginResponse>('/auth/me');
    return response.data;
  },
};

