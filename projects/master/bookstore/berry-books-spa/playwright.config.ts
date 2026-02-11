import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright E2Eテスト設定ファイル
 * 
 * @see https://playwright.dev/docs/test-configuration
 */
export default defineConfig({
  // テストディレクトリ
  testDir: './tests',
  
  // すべてのテストを並列実行
  fullyParallel: true,
  
  // CI環境では.only を禁止
  forbidOnly: !!process.env.CI,
  
  // CI環境では2回までリトライ
  retries: process.env.CI ? 2 : 0,
  
  // CI環境では1ワーカー、ローカルでは未定義（CPUコア数に応じて自動）
  workers: process.env.CI ? 1 : undefined,
  
  // HTMLレポートを生成
  reporter: 'html',
  
  // すべてのテストで共通の設定
  use: {
    // ベースURL
    baseURL: 'http://localhost:5173',
    
    // 失敗時の最初のリトライでトレースを記録
    trace: 'on-first-retry',
    
    // 失敗時のみスクリーンショットを撮影
    screenshot: 'only-on-failure',
    
    // 失敗時のみ動画を記録
    video: 'retain-on-failure',
  },

  // テストプロジェクトの設定
  projects: [
    {
      name: 'chrome',
      use: { 
        ...devices['Desktop Chrome'],
        channel: 'chrome' // インストール済みのGoogle Chromeを使用
      },
    },

    // 必要に応じて他のブラウザを有効化
    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] },
    // },

    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] },
    // },

    /* モバイルブラウザでのテスト */
    // {
    //   name: 'Mobile Chrome',
    //   use: { ...devices['Pixel 5'] },
    // },
    // {
    //   name: 'Mobile Safari',
    //   use: { ...devices['iPhone 12'] },
    // },
  ],

  // Webサーバーの自動起動設定
  // 手動でサーバーを起動する場合は、このセクションをコメントアウトしてください
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: true, // 既存サーバーを再利用（重要）
    timeout: 120 * 1000, // 2分
  },
});
