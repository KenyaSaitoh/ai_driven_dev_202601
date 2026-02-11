# Playwright ベストプラクティス

バージョン: 1.0.0  
最終更新日: 2026-02-11

---

## 目次

1. [Page Object Modelパターン](#page-object-modelパターン)
2. [セレクタ戦略](#セレクタ戦略)
3. [待機処理](#待機処理)
4. [エラーハンドリング](#エラーハンドリング)
5. [テストデータ管理](#テストデータ管理)
6. [並列実行とテスト独立性](#並列実行とテスト独立性)
7. [スクリーンショットと動画](#スクリーンショットと動画)
8. [デバッグ](#デバッグ)

---

## Page Object Modelパターン

### 概要

Page Object Model（POM）は、UIテストの保守性を向上させるデザインパターンです。各ページをクラスとして表現し、要素とアクションをカプセル化します。

### 実装ガイドライン

#### 1. 基本構造

```typescript
import { Page, Locator } from '@playwright/test';

export class PageName {
  readonly page: Page;
  // 要素のLocator
  readonly element: Locator;

  constructor(page: Page) {
    this.page = page;
    this.element = page.locator('selector');
  }

  // ページ固有のアクション
  async actionName() {
    await this.element.click();
  }
}
```

#### 2. 命名規則

- **クラス名**: PascalCase + "Page" 接尾辞
  - 良い例: `LoginPage`, `BookListPage`, `CartPage`
  - 悪い例: `login`, `bookList`, `Cart`

- **要素プロパティ名**: camelCase + 要素タイプ接尾辞
  - 良い例: `emailInput`, `submitButton`, `errorMessage`
  - 悪い例: `email`, `btn`, `error`

- **メソッド名**: camelCase + 動詞で開始
  - 良い例: `login()`, `fillForm()`, `submitOrder()`
  - 悪い例: `loginMethod()`, `form()`, `order()`

#### 3. ページ遷移の処理

```typescript
export class LoginPage {
  constructor(page: Page) {
    this.page = page;
  }

  async goto() {
    await this.page.goto('/login');
  }

  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.submitButton.click();
    // 次のページを返す
    return new BookListPage(this.page);
  }
}
```

#### 4. 複雑なアクションのカプセル化

```typescript
export class CartPage {
  async addMultipleItems(bookIds: number[]) {
    for (const bookId of bookIds) {
      await this.addItemButton(bookId).click();
      await this.page.waitForURL('/cart/added');
      await this.continueShoppingLink.click();
    }
  }

  private addItemButton(bookId: number): Locator {
    return this.page.locator(`button[data-book-id="${bookId}"]`);
  }
}
```

### アンチパターン

❌ **悪い例**: テストコード内に直接セレクタを記述

```typescript
test('ログイン', async ({ page }) => {
  await page.locator('input[type="email"]').fill('test@example.com');
  await page.locator('input[type="password"]').fill('password');
  await page.locator('button[type="submit"]').click();
});
```

✅ **良い例**: Page Objectを使用

```typescript
test('ログイン', async ({ page }) => {
  const loginPage = new LoginPage(page);
  await loginPage.goto();
  await loginPage.login('test@example.com', 'password');
});
```

---

## セレクタ戦略

### セレクタの優先順位

1. **`data-testid` 属性**（最推奨）
   ```typescript
   page.locator('[data-testid="login-button"]')
   ```
   
   理由: テスト専用の属性で、UIの変更に強い

2. **`id` 属性**
   ```typescript
   page.locator('#loginButton')
   ```
   
   理由: 一意で変更されにくい

3. **ラベルやテキスト**
   ```typescript
   page.locator('button:has-text("ログイン")')
   page.getByRole('button', { name: 'ログイン' })
   ```
   
   理由: ユーザーが見ている内容に基づく

4. **CSSセレクタ**
   ```typescript
   page.locator('button[type="submit"]')
   ```
   
   理由: 最後の手段、変更に弱い

### セレクタのベストプラクティス

#### 1. 安定したセレクタを使用

✅ **良い例**:
```typescript
page.locator('[data-testid="add-to-cart"]')
page.locator('#checkout-button')
page.getByRole('button', { name: '注文する' })
```

❌ **悪い例**:
```typescript
page.locator('div > div > button:nth-child(3)') // 脆弱
page.locator('.css-abc123') // 自動生成クラス名
```

#### 2. 相対的な検索を活用

```typescript
// 特定のカード内のボタンを探す
const bookCard = page.locator('[data-testid="book-card"]').nth(2);
const addButton = bookCard.locator('button:has-text("カートに追加")');
```

#### 3. 複数マッチを避ける

```typescript
// 悪い例: 複数の要素にマッチする可能性
await page.locator('button').click();

// 良い例: 一意に特定
await page.locator('button[type="submit"]:has-text("ログイン")').click();
```

---

## 待機処理

### 自動待機の活用

Playwrightは多くの操作で自動的に要素が準備できるまで待機します。

```typescript
// 自動的に要素が表示されるまで待機
await page.locator('button').click();
await page.locator('input').fill('text');
```

### 明示的な待機が必要な場合

#### 1. URL遷移の待機

```typescript
await page.waitForURL('/books');
await page.waitForURL(/\/orders\/\d+/); // 正規表現も使用可能
```

#### 2. 要素の状態変化の待機

```typescript
// 要素が表示されるまで待機
await page.waitForSelector('[data-testid="success-message"]');

// 要素が非表示になるまで待機
await page.waitForSelector('[data-testid="loading-spinner"]', { state: 'hidden' });
```

#### 3. ネットワークリクエストの待機

```typescript
await Promise.all([
  page.waitForResponse(resp => resp.url().includes('/api/orders') && resp.status() === 200),
  page.locator('button:has-text("注文する")').click()
]);
```

### アンチパターン

❌ **悪い例**: 固定時間の待機

```typescript
await page.waitForTimeout(5000); // 避けるべき
```

✅ **良い例**: 条件ベースの待機

```typescript
await page.waitForSelector('[data-testid="order-success"]');
```

---

## エラーハンドリング

### タイムアウト設定

#### 1. グローバル設定

```typescript
// playwright.config.ts
export default defineConfig({
  timeout: 60000, // テスト全体のタイムアウト
  use: {
    actionTimeout: 10000, // アクション毎のタイムアウト
    navigationTimeout: 30000, // ナビゲーションのタイムアウト
  },
});
```

#### 2. テスト毎の設定

```typescript
test.setTimeout(120000); // 2分

test('長時間実行テスト', async ({ page }) => {
  // ...
});
```

### スクリーンショットとトレース

```typescript
// playwright.config.ts
export default defineConfig({
  use: {
    screenshot: 'only-on-failure', // 失敗時のみスクリーンショット
    video: 'retain-on-failure', // 失敗時のみ動画を保存
    trace: 'on-first-retry', // 最初のリトライ時にトレースを記録
  },
});
```

### エラー時の情報収集

```typescript
test('注文処理', async ({ page }) => {
  try {
    await page.locator('button:has-text("注文する")').click();
    await expect(page).toHaveURL('/orders/success');
  } catch (error) {
    // エラー時にページ情報を出力
    console.log('Current URL:', page.url());
    console.log('Page title:', await page.title());
    await page.screenshot({ path: 'error-screenshot.png' });
    throw error;
  }
});
```

---

## テストデータ管理

### テストフィクスチャの使用

```typescript
// tests/fixtures/test-data.ts
export const testUsers = {
  validUser: {
    email: 'alice@example.com',
    password: 'password',
  },
  adminUser: {
    email: 'admin@example.com',
    password: 'admin123',
  },
};

export const testBooks = [
  { id: 1, title: 'Java SE ディープダイブ', price: 4500 },
  { id: 2, title: 'Python 実践入門', price: 3200 },
];
```

```typescript
// tests/login.spec.ts
import { testUsers } from './fixtures/test-data';

test('ログイン', async ({ page }) => {
  const loginPage = new LoginPage(page);
  await loginPage.login(testUsers.validUser.email, testUsers.validUser.password);
});
```

### 環境変数の活用

```typescript
// .env
BASE_URL=http://localhost:5173
TEST_USER_EMAIL=alice@example.com
TEST_USER_PASSWORD=password
```

```typescript
// playwright.config.ts
import dotenv from 'dotenv';
dotenv.config();

export default defineConfig({
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:5173',
  },
});
```

---

## 並列実行とテスト独立性

### テストの独立性を確保

各テストは他のテストに依存せず、独立して実行できる必要があります。

❌ **悪い例**: テスト間で状態を共有

```typescript
let orderId: string;

test('注文作成', async ({ page }) => {
  orderId = await createOrder(page); // グローバル変数に保存
});

test('注文詳細表示', async ({ page }) => {
  await page.goto(`/orders/${orderId}`); // 前のテストに依存
});
```

✅ **良い例**: 各テストで独立してセットアップ

```typescript
test('注文詳細表示', async ({ page }) => {
  // 前提条件を自分でセットアップ
  const orderId = await createOrder(page);
  await page.goto(`/orders/${orderId}`);
});
```

### 並列実行の設定

```typescript
// playwright.config.ts
export default defineConfig({
  fullyParallel: true, // 全テストを並列実行
  workers: process.env.CI ? 2 : undefined, // CI環境では2ワーカー
});
```

### テスト間のデータクリーンアップ

```typescript
test.afterEach(async ({ page }) => {
  // カートをクリア
  await page.goto('/cart');
  await page.locator('button:has-text("カートをクリア")').click();
});
```

---

## スクリーンショットと動画

### スクリーンショットの取得

```typescript
// 手動でスクリーンショットを取得
await page.screenshot({ path: 'screenshot.png' });

// 全ページのスクリーンショット
await page.screenshot({ path: 'fullpage.png', fullPage: true });

// 特定要素のスクリーンショット
await page.locator('[data-testid="order-summary"]').screenshot({ path: 'summary.png' });
```

### 動画記録

```typescript
// playwright.config.ts
export default defineConfig({
  use: {
    video: {
      mode: 'on', // 'on' | 'off' | 'retain-on-failure' | 'on-first-retry'
      size: { width: 1280, height: 720 },
    },
  },
});
```

---

## デバッグ

### Playwright Inspector の使用

```bash
# デバッグモードでテストを実行
npx playwright test --debug

# 特定のテストをデバッグ
npx playwright test login.spec.ts --debug
```

### UIモードの使用

```bash
# UIモードでテストを実行（推奨）
npx playwright test --ui
```

### console.log を使用したデバッグ

```typescript
test('デバッグ例', async ({ page }) => {
  console.log('Current URL:', page.url());
  
  const elements = await page.locator('button').all();
  console.log('Number of buttons:', elements.length);
  
  await page.pause(); // ブラウザを一時停止
});
```

### トレースビューアの使用

```bash
# トレースを表示
npx playwright show-trace trace.zip
```

---

## まとめ

### 重要なポイント

1. **Page Object Modelを活用**して保守性を向上
2. **安定したセレクタ**（data-testid > id > text > CSS）を優先
3. **自動待機を活用**し、固定時間待機は避ける
4. **テストの独立性**を確保し、並列実行に対応
5. **エラーハンドリング**を適切に行い、デバッグ情報を充実
6. **スクリーンショット・動画**を活用してトラブルシューティング

### チェックリスト

- [ ] Page Objectを使用している
- [ ] data-testid または id を優先的に使用している
- [ ] waitForTimeout() を使用していない
- [ ] 各テストが独立して実行できる
- [ ] エラー時の情報収集が適切
- [ ] タイムアウト設定が適切
- [ ] テストデータが外部ファイルに分離されている
- [ ] 並列実行に対応している

---

## 参考資料

* [Playwright 公式ドキュメント](https://playwright.dev/)
* [Page Object Model パターン](https://playwright.dev/docs/pom)
* [Best Practices](https://playwright.dev/docs/best-practices)
* [Debugging Tests](https://playwright.dev/docs/debug)
