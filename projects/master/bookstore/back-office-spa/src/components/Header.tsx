import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const Header: React.FC = () => {
  const { employee, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    if (window.confirm('ログアウトしてもよろしいですか？')) {
      try {
        await logout();
        navigate('/login');
      } catch (error) {
        console.error('Logout failed:', error);
        alert('ログアウトに失敗しました');
      }
    }
  };

  return (
    <header className="bg-gradient-primary shadow-primary-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center space-x-8">
            <Link 
              to="/workflows" 
              className="text-white text-xl font-bold hover:text-accent-light transition-colors duration-300"
            >
              Berry Books バックオフィス
            </Link>
            {isAuthenticated && (
              <nav className="flex space-x-4">
                <Link 
                  to="/workflows" 
                  className="text-white hover:text-accent-light px-3 py-2 rounded-md text-sm font-medium transition-colors duration-300"
                >
                  ワークフロー一覧
                </Link>
                <Link 
                  to="/workflows/create" 
                  className="text-white hover:text-accent-light px-3 py-2 rounded-md text-sm font-medium transition-colors duration-300"
                >
                  新規作成
                </Link>
              </nav>
            )}
          </div>

          {isAuthenticated && employee && (
            <div className="flex items-center space-x-4">
              <span className="text-white text-sm">
                {employee.employeeName} ({employee.employeeCode})
                {employee.departmentName && ` - ${employee.departmentName}`}
              </span>
              <button 
                onClick={handleLogout} 
                className="bg-white text-primary hover:bg-accent-light px-4 py-2 rounded-lg text-sm font-semibold transition-all duration-300 shadow-md hover:shadow-lg"
              >
                ログアウト
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;

