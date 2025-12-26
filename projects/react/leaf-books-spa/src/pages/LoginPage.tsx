import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { login, register, user } = useAuth();
  const [isRegister, setIsRegister] = useState(false);
  const [error, setError] = useState<string>('');
  
  // ログインフォーム
  const [loginData, setLoginData] = useState({
    email: '',
    password: '',
  });

  // 新規登録フォーム
  const [registerData, setRegisterData] = useState({
    customerName: '',
    email: '',
    password: '',
    birthday: '',
    address: '',
  });

  // ログイン済みの場合は書籍一覧へリダイレクト
  React.useEffect(() => {
    if (user) {
      navigate('/books');
    }
  }, [user, navigate]);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    try {
      await login(loginData.email, loginData.password);
      navigate('/books');
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'ログインに失敗しました';
      setError(errorMessage);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    try {
      await register(registerData);
      navigate('/books');
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || '登録に失敗しました';
      setError(errorMessage);
    }
  };

  return (
    <div className="min-h-screen bg-white py-12 px-10">
      <h2 className="text-4xl font-bold text-primary-darker text-center mb-8">
        ようこそ！テックブックストアへ
      </h2>
      <hr className="mb-8 border-t-2 border-primary opacity-50" />

      {!isRegister ? (
        // ログインフォーム
        <div>
          <div className="mb-4 text-lg">登録済みのお客様</div>
          <form onSubmit={handleLogin} className="max-w-3xl mx-auto bg-white p-10 rounded-xl">
            <table className="w-full">
              <tbody>
                <tr>
                  <td className="w-48 py-3 font-semibold text-primary-darker">メールアドレス</td>
                  <td className="py-3">
                    <input
                      type="email"
                      value={loginData.email}
                      onChange={(e) => setLoginData({ ...loginData, email: e.target.value })}
                      className="form-input"
                      required
                    />
                  </td>
                </tr>
                <tr>
                  <td className="py-3 font-semibold text-primary-darker">パスワード</td>
                  <td className="py-3">
                    <input
                      type="password"
                      value={loginData.password}
                      onChange={(e) => setLoginData({ ...loginData, password: e.target.value })}
                      className="form-input"
                      required
                    />
                  </td>
                </tr>
              </tbody>
            </table>

            {error && (
              <div className="mt-4 p-3 bg-error-light border border-error rounded-lg">
                <p className="error-text">{error}</p>
              </div>
            )}

            <button type="submit" className="btn-primary mt-6">
              ログイン
            </button>
          </form>

          <hr className="my-8 border-t-2 border-primary opacity-50" />

          <div className="mb-4 text-lg">新規のお客様</div>
          <button
            onClick={() => setIsRegister(true)}
            className="link-primary text-lg"
          >
            お客様のご登録
          </button>
        </div>
      ) : (
        // 新規登録フォーム
        <div>
          <div className="mb-4 text-lg">お客様情報の登録</div>
          <form onSubmit={handleRegister} className="max-w-4xl mx-auto bg-white p-10 rounded-xl">
            <table className="w-full">
              <tbody>
                <tr>
                  <td className="w-64 py-3 font-semibold text-primary-darker whitespace-nowrap">お客様名</td>
                  <td className="py-3">
                    <input
                      type="text"
                      value={registerData.customerName}
                      onChange={(e) => setRegisterData({ ...registerData, customerName: e.target.value })}
                      className="form-input"
                      required
                    />
                  </td>
                </tr>
                <tr>
                  <td className="py-3 font-semibold text-primary-darker whitespace-nowrap">メールアドレス</td>
                  <td className="py-3">
                    <input
                      type="email"
                      value={registerData.email}
                      onChange={(e) => setRegisterData({ ...registerData, email: e.target.value })}
                      className="form-input"
                      required
                    />
                  </td>
                </tr>
                <tr>
                  <td className="py-3 font-semibold text-primary-darker whitespace-nowrap">パスワード</td>
                  <td className="py-3">
                    <input
                      type="password"
                      value={registerData.password}
                      onChange={(e) => setRegisterData({ ...registerData, password: e.target.value })}
                      className="form-input"
                      required
                    />
                  </td>
                </tr>
                <tr>
                  <td className="py-3 font-semibold text-primary-darker whitespace-nowrap">生年月日</td>
                  <td className="py-3">
                    <input
                      type="date"
                      value={registerData.birthday}
                      onChange={(e) => setRegisterData({ ...registerData, birthday: e.target.value })}
                      className="form-input"
                      required
                    />
                  </td>
                </tr>
                <tr>
                  <td className="py-3 font-semibold text-primary-darker whitespace-nowrap">住所（都道府県名から）</td>
                  <td className="py-3">
                    <input
                      type="text"
                      value={registerData.address}
                      onChange={(e) => setRegisterData({ ...registerData, address: e.target.value })}
                      className="form-input"
                      placeholder="例：東京都渋谷区..."
                      required
                    />
                  </td>
                </tr>
              </tbody>
            </table>

            {error && (
              <div className="mt-4 p-3 bg-error-light border border-error rounded-lg">
                <p className="error-text">{error}</p>
              </div>
            )}

            <div className="flex gap-4 mt-6">
              <button type="submit" className="btn-primary">
                登録
              </button>
              <button
                type="button"
                onClick={() => {
                  setIsRegister(false);
                  setError('');
                }}
                className="btn-primary"
              >
                キャンセル
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};

export default LoginPage;

