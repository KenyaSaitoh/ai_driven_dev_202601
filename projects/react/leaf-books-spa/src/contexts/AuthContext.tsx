import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User, RegisterRequest, AuthContextType } from '../types';
import { authService } from '../services/authService';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  // 認証状態のチェック
  const checkAuth = async (): Promise<void> => {
    try {
      const userData = await authService.getCurrentUser();
      setUser({
        customerId: userData.customerId,
        customerName: userData.customerName,
        email: userData.email,
        birthday: userData.birthday,
        address: userData.address,
      });
    } catch (error) {
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  // ログイン
  const login = async (email: string, password: string): Promise<void> => {
    const response = await authService.login({ email, password });
    setUser({
      customerId: response.customerId,
      customerName: response.customerName,
      email: response.email,
      birthday: response.birthday,
      address: response.address,
    });
  };

  // 新規登録
  const register = async (data: RegisterRequest): Promise<void> => {
    const response = await authService.register(data);
    setUser({
      customerId: response.customerId,
      customerName: response.customerName,
      email: response.email,
      birthday: response.birthday,
      address: response.address,
    });
  };

  // ログアウト
  const logout = async (): Promise<void> => {
    await authService.logout();
    setUser(null);
  };

  // 初回マウント時に認証状態をチェック
  useEffect(() => {
    checkAuth();
  }, []);

  const value: AuthContextType = {
    user,
    loading,
    login,
    register,
    logout,
    checkAuth,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

