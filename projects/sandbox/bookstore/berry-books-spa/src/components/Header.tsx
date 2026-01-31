import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useCart } from '../contexts/CartContext';

const Header: React.FC = () => {
  const { user, logout } = useAuth();
  const { itemCount } = useCart();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/');
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  if (!user) {
    return null;
  }

  return (
    <header className="bg-white border-b-2 border-accent">
      <div className="py-6 px-10">
        <div className="flex justify-between items-center mb-4">
          <div className="flex gap-6 items-center">
            <Link to="/books" className="text-primary-dark hover:text-primary-darker font-bold text-lg transition-colors">
              書籍一覧
            </Link>
            <Link to="/books/search" className="text-primary-dark hover:text-primary-darker font-bold text-lg transition-colors">
              書籍検索
            </Link>
            <button
              onClick={() => navigate('/orders/history')}
              className="text-primary-dark hover:text-primary-darker font-bold text-lg transition-colors"
            >
              注文履歴（方式1）
            </button>
            <button
              onClick={() => navigate('/orders/history')}
              className="text-primary-dark hover:text-primary-darker font-bold text-lg transition-colors"
            >
              注文履歴（方式2）
            </button>
            <button
              onClick={() => navigate('/orders/history')}
              className="text-primary-dark hover:text-primary-darker font-bold text-lg transition-colors"
            >
              注文履歴（方式3）
            </button>
          </div>
          <div className="flex items-center gap-6">
            <Link to="/cart" className="text-primary-dark hover:text-primary-darker font-bold text-lg transition-colors relative mr-4">
              カート
              {itemCount > 0 && (
                <span className="absolute -top-2 -right-6 bg-primary text-white text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center">
                  {itemCount}
                </span>
              )}
            </Link>
            <span className="text-gray-600">
              {user.customerName} さん
            </span>
            <button
              onClick={handleLogout}
              className="btn-primary-sm"
            >
              ログアウト
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;

