import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import '../styles/LoginPage.css';

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
    <div className="login-container">
      <div className="login-content">
        <h1 className="login-title">Berry Books バックオフィスシステム</h1>
        <hr className="login-divider" />
        
        <form onSubmit={handleLogin} className="login-form">
          <table className="login-table">
            <tbody>
              <tr>
                <td className="login-label">社員コード</td>
                <td className="login-input-cell">
                  <input
                    type="text"
                    value={employeeCode}
                    onChange={(e) => setEmployeeCode(e.target.value)}
                    className="login-input"
                    placeholder="例: E99999"
                    required
                    disabled={loading}
                  />
                </td>
              </tr>
              <tr>
                <td className="login-label">パスワード</td>
                <td className="login-input-cell">
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="login-input"
                    placeholder="パスワードを入力"
                    required
                    disabled={loading}
                  />
                </td>
              </tr>
            </tbody>
          </table>

          {error && (
            <div className="login-error">
              {error}
            </div>
          )}

          <button type="submit" className="login-button" disabled={loading}>
            {loading ? 'ログイン中...' : 'ログイン'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;

