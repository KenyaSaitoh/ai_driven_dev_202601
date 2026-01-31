import api from './api';
import { Employee, LoginRequest } from '../types';

/**
 * 認証サービス
 */
export const authService = {
  /**
   * ログイン（社員コード + パスワード）
   */
  login: async (employeeCode: string, password: string): Promise<Employee> => {
    const request: LoginRequest = { employeeCode, password };
    const response = await api.post<Employee>('/auth/login', request);
    return response.data;
  },

  /**
   * ログアウト
   */
  logout: async (): Promise<void> => {
    await api.post('/auth/logout');
  },

  /**
   * 現在のログインユーザー情報を取得
   * TODO: バックエンドの/auth/me実装後に有効化
   */
  getCurrentUser: async (): Promise<Employee> => {
    const response = await api.get<Employee>('/auth/me');
    return response.data;
  },
};

