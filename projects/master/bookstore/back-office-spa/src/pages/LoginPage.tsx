import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const LoginPage: React.FC = () => {
  const [employeeCode, setEmployeeCode] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await login(employeeCode, password);
      navigate('/workflows');
    } catch (err: any) {
      console.error('Login failed:', err);
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('ログインに失敗しました。社員コードとパスワードを確認してください。');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary to-primary-dark px-4">
      <div className="bg-white rounded-2xl shadow-primary-lg p-8 w-full max-w-md">
        <h1 className="text-3xl font-bold text-center text-primary mb-2">
          Berry Books
        </h1>
        <p className="text-center text-gray-600 mb-8">バックオフィスシステム</p>
        
        <form onSubmit={handleLogin} className="space-y-6">
          <div>
            <label htmlFor="employeeCode" className="block text-sm font-semibold text-gray-700 mb-2">
              社員コード
            </label>
                  <input
              id="employeeCode"
                    type="text"
                    value={employeeCode}
                    onChange={(e) => setEmployeeCode(e.target.value)}
              className="form-input"
              placeholder="Eで始まる6桁の社員コード"
                    required
                    disabled={loading}
                  />
          </div>

          <div>
            <label htmlFor="password" className="block text-sm font-semibold text-gray-700 mb-2">
              パスワード
            </label>
                  <input
              id="password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
              className="form-input"
                    placeholder="パスワード"
                    required
                    disabled={loading}
                  />
          </div>

          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          <button 
            type="submit" 
            className="btn-primary w-full disabled:opacity-50 disabled:cursor-not-allowed" 
            disabled={loading}
          >
            {loading ? 'ログイン中...' : 'ログイン'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;

