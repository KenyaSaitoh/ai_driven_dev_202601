import axios from 'axios';
import Cookies from 'js-cookie';

// Axiosインスタンスの作成
const api = axios.create({
  baseURL: '/api',
  withCredentials: true, // Cookie を含める
  headers: {
    'Content-Type': 'application/json',
  },
});

// CSRFトークンを取得する関数
const getCsrfToken = (): string | undefined => {
  return Cookies.get('XSRF-TOKEN');
};

// リクエストインターセプター: CSRFトークンをヘッダーに追加
api.interceptors.request.use(
  (config) => {
    const csrfToken = getCsrfToken();
    if (csrfToken && config.method && ['post', 'put', 'delete', 'patch'].includes(config.method.toLowerCase())) {
      config.headers['X-XSRF-TOKEN'] = csrfToken;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// レスポンスインターセプター: エラーハンドリング
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      // サーバーからエラーレスポンスが返ってきた場合
      const status = error.response.status;
      
      if (status === 401) {
        // 未認証エラーの場合、ログインページにリダイレクト
        if (window.location.pathname !== '/') {
          window.location.href = '/';
        }
      }
    }
    
    return Promise.reject(error);
  }
);

export default api;

