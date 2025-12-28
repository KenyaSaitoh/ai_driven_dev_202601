import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { Employee } from '../types';
import { authService } from '../services/authService';

interface AuthContextType {
  employee: Employee | null;
  loading: boolean;
  login: (employeeCode: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  isAuthenticated: boolean;
  // 権限チェック用のヘルパー
  isManager: () => boolean;
  isDirector: () => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
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
  const [employee, setEmployee] = useState<Employee | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  // 初期化時に認証状態を確認
  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    try {
      // TODO: バックエンドの/auth/me実装後に有効化
      // const currentEmployee = await authService.getCurrentUser();
      // setEmployee(currentEmployee);
      
      // 暫定的に認証状態なしで開始
      setEmployee(null);
    } catch (error) {
      console.error('Authentication check failed:', error);
      setEmployee(null);
    } finally {
      setLoading(false);
    }
  };

  const login = async (employeeCode: string, password: string) => {
    try {
      const loggedInEmployee = await authService.login(employeeCode, password);
      setEmployee(loggedInEmployee);
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  };

  const logout = async () => {
    try {
      await authService.logout();
      setEmployee(null);
    } catch (error) {
      console.error('Logout failed:', error);
      throw error;
    }
  };

  const isManager = (): boolean => {
    return employee !== null && employee.jobRank >= 2;  // MANAGER or DIRECTOR
  };

  const isDirector = (): boolean => {
    return employee !== null && employee.jobRank >= 3;  // DIRECTOR
  };

  const value: AuthContextType = {
    employee,
    loading,
    login,
    logout,
    isAuthenticated: employee !== null,
    isManager,
    isDirector,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

