import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import '../styles/Header.css';

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
    <header className="header">
      <div className="header-content">
        <div className="header-left">
          <Link to="/workflows" className="header-logo">
            バックオフィスシステム
          </Link>
          {isAuthenticated && (
            <nav className="header-nav">
              <Link to="/workflows" className="nav-link">
                ワークフロー一覧
              </Link>
              <Link to="/workflows/create" className="nav-link">
                新規作成
              </Link>
            </nav>
          )}
        </div>

        {isAuthenticated && employee && (
          <div className="header-right">
            <span className="user-info">
              {employee.employeeName} ({employee.employeeCode})
              {employee.departmentName && ` - ${employee.departmentName}`}
            </span>
            <button onClick={handleLogout} className="logout-button">
              ログアウト
            </button>
          </div>
        )}
      </div>
    </header>
  );
};

export default Header;

