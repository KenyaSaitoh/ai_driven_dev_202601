import axios from 'axios';

// Axiosインスタンスを作成
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,  // Cookie（JWT）を含める
});

// レスポンスインターセプター（エラーハンドリング）
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // サーバーがエラーレスポンスを返した場合
      console.error('API Error:', error.response.data);
      
      // 401エラー（未認証）の場合はログインページにリダイレクト
      if (error.response.status === 401) {
        window.location.href = '/login';
      }
    } else if (error.request) {
      // リクエストは送信されたが、レスポンスがない
      console.error('No response received:', error.request);
    } else {
      // リクエスト設定中にエラーが発生
      console.error('Error setting up request:', error.message);
    }
    return Promise.reject(error);
  }
);

export default api;

