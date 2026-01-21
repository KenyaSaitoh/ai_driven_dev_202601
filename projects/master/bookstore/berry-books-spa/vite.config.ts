import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // すべてのAPIリクエストを berry-books-api に転送
      // berry-books-api が BFF（Backend for Frontend）として
      // 他のマイクロサービス（back-office-api、customer-hub-api）を呼び出す
      '/api': {
        target: 'http://localhost:8080/berry-books-api',
        changeOrigin: true,
      }
    }
  }
})

