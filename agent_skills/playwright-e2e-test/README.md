# Playwright E2Eテスト生成 Agent Skill

バージョン: 1.0.0  
最終更新日: 2026-02-11

---

## 概要

WebアプリケーションのPlaywright E2Eテストコードをテストシナリオ定義書から自動生成するAgent Skillです。

このAgent Skillは、Markdown形式のテストシナリオ定義書を読み込み、ページ遷移、要素操作、検証を含む高品質なテストコードを生成します。Page Object Modelパターンに対応し、保守性の高いテストコードを実現します。

対象プロジェクト例: SPAアプリケーション（React、Vue.js等）、従来型Webアプリケーション

## クイックスタート

1. テストシナリオ定義書を準備（Markdown形式）
2. `@agent_skills/playwright-e2e-test/instructions/generate_playwright_tests.md` でテストコード生成

```
@agent_skills/playwright-e2e-test/instructions/generate_playwright_tests.md

テストシナリオ定義書からPlaywrightテストコードを生成してください

パラメータ
* project_root: projects/master/bookstore/berry-books-spa
* instructions_file: projects/master/bookstore/berry-books-spa/playwright_berry-books.md
```

---

## フォルダ構造

```
agent_skills/playwright-e2e-test/
│
├── SKILL.md                                    # Agent Skill説明書（エントリポイント）
│
├── instructions/                               # 開発インストラクション
│   └── generate_playwright_tests.md           # テストコード生成指示
│       └─→ 遵守: principles/playwright_best_practices.md
│       └─→ 読込: {instructions_file}（テストシナリオ定義書）
│       └─→ 解析: {project_root}/src/（アプリケーションコード）
│       └─→ 出力: {test_output_dir}/（テストコード）
│                  {page_objects_dir}/（Page Objects）
│                  {project_root}/playwright.config.ts
│                  {project_root}/README_PLAYWRIGHT.md
│
├── principles/                                 # 開発原則（全プロジェクト共通）
│   └── playwright_best_practices.md           # Playwrightベストプラクティス
│                                               - Page Object Modelパターン
│                                               - セレクタ戦略
│                                               - 待機処理のベストプラクティス
│                                               - エラーハンドリング
│                                               - テストデータ管理
│                                               - 並列実行とテスト独立性
│
└── templates/                                  # テンプレート
    └── playwright_instructions.md             # テストシナリオ定義書テンプレート
```

---

## プロジェクトフォルダ構造

このAgent Skillを使用して生成されるテストコードの標準フォルダ構造です。

```
{project_root}/                                # プロジェクトルートディレクトリ
│
├── tests/                                     # Playwrightテストディレクトリ
│   ├── pages/                                 # Page Objects
│   │   ├── LoginPage.ts                       # ログインページ
│   │   ├── BookListPage.ts                    # 書籍一覧ページ
│   │   ├── BookSearchPage.ts                  # 書籍検索ページ
│   │   ├── CartPage.ts                        # カートページ
│   │   ├── OrderConfirmPage.ts                # 注文確認ページ
│   │   └── OrderSuccessPage.ts                # 注文成功ページ
│   │
│   ├── fixtures/                              # テストフィクスチャ
│   │   └── test-data.ts                       # テストデータ
│   │
│   ├── basic-flow.spec.ts                     # 基本フローテスト
│   ├── complete-flow.spec.ts                  # 完全フローテスト
│   ├── customer-registration.spec.ts          # 顧客登録テスト
│   ├── book-search-order.spec.ts              # 書籍検索・注文テスト
│   └── cart-operations.spec.ts                # カート操作テスト
│
├── playwright.config.ts                       # Playwright設定ファイル
├── README_PLAYWRIGHT.md                       # テスト実行方法
└── package.json                               # 依存関係管理
```

---

## テストシナリオ定義書の形式

テストシナリオ定義書は以下の形式で記述します。

### 必須セクション

1. **概要** - テスト対象アプリの概要
2. **前提条件** - アプリケーションURL、テストユーザー等
3. **テストシナリオ一覧** - シナリオの一覧表
4. **シナリオ詳細** - 各シナリオの手順（表形式）

### シナリオ表の形式

| No. | 操作 | 対象 | 入力値/期待値 | 備考 |
|-----|------|------|-------------|------|
| 1 | ページを開く |  | `http://localhost:5173/` |  |
| 2 | 入力 | メールアドレス | `alice@example.com` |  |
| 3 | クリック | ログインボタン |  |  |
| 4 | 確認 | URL | `/books` に遷移 |  |

**重要**: セレクタは不要です。「対象」列には日本語で要素名を記述してください（例：「メールアドレス」「ログインボタン」）。AIがReactコンポーネントを解析して適切なセレクタを自動推論します。

### サポートされる操作

- **ページを開く** - 指定URLでページを開く
- **入力** - テキストボックスに値を入力
- **クリック** - ボタンやリンクをクリック
- **選択** - セレクトボックスで値を選択
- **待機** - 要素の読み込み完了やAPIレスポンスを待つ
- **確認** - 要素のテキストや状態を検証
- **ダイアログ受諾/ダイアログ却下** - confirm/alertダイアログを処理

**セレクタの自動推論**: 
AIが「対象」列の日本語記述からアプリケーションコードを解析し、適切なセレクタを自動生成します。
- 例：「メールアドレス」→ `input[type="email"]` または `#email`
- 例：「ログインボタン」→ `button[type="submit"]` または `button:has-text("ログイン")`

詳細は [テストシナリオ定義書テンプレート](templates/playwright_instructions.md) を参照してください。

---

## テストコード生成オプション

### Page Object Modelの使用

デフォルトでPage Object Modelパターンを使用します。

**有効化（デフォルト）:**
```
パラメータ
* use_page_objects: true
```

各ページごとにクラスを生成し、要素セレクタとアクションをカプセル化します。

**無効化:**
```
パラメータ
* use_page_objects: false
```

Page Objectsを使用せず、直接的なテストコードを生成します（小規模プロジェクト向け）。

### 出力ディレクトリのカスタマイズ

```
パラメータ
* test_output_dir: tests/e2e          # テストコードの出力先
* page_objects_dir: tests/e2e/pages   # Page Objectsの出力先
```

---

## 実践例

### 例1: 基本的な使用方法

```
@agent_skills/playwright-e2e-test/instructions/generate_playwright_tests.md

テストシナリオ定義書からPlaywrightテストコードを生成してください

パラメータ
* project_root: projects/master/bookstore/berry-books-spa
* instructions_file: projects/master/bookstore/berry-books-spa/playwright_berry-books.md
```

AIが自動で実行:
1. テストシナリオ定義書を読み込む
2. シナリオを解析
3. アプリケーションコードを解析してセレクタを推論
4. Page Objectsを生成
5. テストコードを生成
6. 設定ファイルを生成

### 例2: 出力先をカスタマイズ

```
@agent_skills/playwright-e2e-test/instructions/generate_playwright_tests.md

テストシナリオ定義書からPlaywrightテストコードを生成してください

パラメータ
* project_root: projects/master/bookstore/berry-books-spa
* instructions_file: projects/master/bookstore/berry-books-spa/playwright_berry-books.md
* test_output_dir: e2e-tests
* page_objects_dir: e2e-tests/page-objects
```

### 例3: Page Object Modelを使用しない

```
@agent_skills/playwright-e2e-test/instructions/generate_playwright_tests.md

テストシナリオ定義書からPlaywrightテストコードを生成してください

パラメータ
* project_root: projects/master/bookstore/berry-books-spa
* instructions_file: projects/master/bookstore/berry-books-spa/playwright_berry-books.md
* use_page_objects: false
```

---

## 生成されるファイル

### 1. Page Objects（use_page_objects=trueの場合）

```typescript
// tests/pages/LoginPage.ts
import { Page, Locator } from '@playwright/test';

export class LoginPage {
  readonly page: Page;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly loginButton: Locator;

  constructor(page: Page) {
    this.page = page;
    // アプリケーションコードから推論したセレクタを使用
    this.emailInput = page.locator('input[type="email"]');
    this.passwordInput = page.locator('input[type="password"]');
    this.loginButton = page.locator('button[type="submit"]');
  }

  async goto() {
    await this.page.goto('/');
  }

  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.loginButton.click();
  }
}
```

### 2. テストコード

```typescript
// tests/basic-flow.spec.ts
import { test, expect } from '@playwright/test';
import { LoginPage } from './pages/LoginPage';
import { BookListPage } from './pages/BookListPage';

test.describe('基本フロー（検証なし）', () => {
  test('ログインから注文までの基本操作', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login('alice@example.com', 'password');
    
    await expect(page).toHaveURL('/books');
    
    const bookListPage = new BookListPage(page);
    await bookListPage.addToCart(0); // 1番目の書籍
    
    // ... 以降の処理
  });
});
```

### 3. Playwright設定ファイル

```typescript
// playwright.config.ts
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
  },
});
```

### 4. README

テスト実行方法、トラブルシューティング、ベストプラクティスを含むREADMEが生成されます。

---

## テスト実行方法

生成されたテストを実行するには:

```bash
# 依存関係のインストール
npm install

# Playwrightのインストール
npx playwright install

# 全テストを実行
npx playwright test

# 特定のテストを実行
npx playwright test basic-flow.spec.ts

# UIモードで実行（デバッグ用）
npx playwright test --ui

# デバッグモードで実行
npx playwright test --debug

# レポートを表示
npx playwright show-report
```

---

## ベストプラクティス

### 1. Page Object Modelの活用

Page Objectsを使用することで、UIの変更に強いテストコードを作成できます。

### 2. セレクタの優先順位

1. `data-testid` 属性（推奨）
2. `id` 属性
3. テキストベース（`:has-text()`）
4. CSS セレクタ

### 3. 待機処理

自動待機を活用し、明示的な `waitForTimeout()` は最小限に。

### 4. テストの独立性

各テストは独立して実行できるように設計します。

詳細は [Playwrightベストプラクティス](principles/playwright_best_practices.md) を参照してください。

---

## トラブルシューティング

### セレクトボックスの選択肢が見つからない

**症状**: `selectOption: Test timeout exceeded` と `did not find some options` エラー

**原因**: 選択肢がAPIから非同期に読み込まれるため、読み込み完了前に選択を実行している

**解決策**: テストシナリオ定義書に「待機」ステップを追加

| No. | 操作 | 対象 | 入力値/期待値 | 備考 |
|-----|------|------|-------------|------|
| 12 | 待機 | カテゴリ選択肢 | 読み込み完了を待つ | APIから取得 |
| 13 | 選択 | カテゴリ | 最初の選択肢（すべて以外） |  |

生成されるコード:
```typescript
// 選択肢の読み込み完了を待つ
await page.locator('#category option').nth(1).waitFor({ state: 'attached' });
await categorySelect.selectOption('2');
```

### ダイアログ処理の失敗

**症状**: ダイアログが期待通りに動作せず、テストがハングする

**原因**: 複雑な条件分岐やフラグ管理のバグ

**解決策**: テストシナリオをシンプルに保つ
- 「キャンセル→再試行」のような複雑なフローは避ける
- シンプルな accept または dismiss のみを使用

### 検索結果が表示されない

**症状**: 検索ボタンをクリック直後に検索結果の表示確認が失敗する

**原因**: 検索処理中（loading状態）で検索結果がまだ非表示

**解決策**: 検索処理の完了を待つメソッドをPage Objectに追加

```typescript
// BookSearchPage.ts
async waitForSearchResults() {
  // 検索ボタンがdisabled属性を持たなくなるまで待つ
  await this.page.waitForFunction(
    () => {
      const jpqlButton = document.querySelector('#search1Button');
      return jpqlButton && !jpqlButton.hasAttribute('disabled');
    },
    { timeout: 15000 }
  );
  await this.page.waitForTimeout(300);
}

// テストで使用
await bookSearchPage.searchJpqlButton.click();
await bookSearchPage.waitForSearchResults();
await expect(bookSearchPage.searchResults).toBeVisible();
```

**また、検索条件を確実に結果が表示される条件にする**:
- カテゴリ: すべて
- キーワード: Java（一般的で該当データが多いもの）

### タイムアウトエラー

テスト実行時にタイムアウトが発生する場合:

```typescript
// playwright.config.ts
export default defineConfig({
  timeout: 60000, // 60秒に延長
  use: {
    actionTimeout: 10000, // アクション毎のタイムアウトを10秒に
  },
});
```

### 要素が見つからない

セレクタが正しいか確認します:

```bash
# Playwright Inspectorを使用
npx playwright test --debug
```

### 並列実行時の問題

テストが独立していることを確認します:

```typescript
// playwright.config.ts
export default defineConfig({
  fullyParallel: false, // 並列実行を無効化
  workers: 1, // 1ワーカーのみ使用
});
```

---

## 参考

* [SKILL.md](SKILL.md) - エントリポイント、クイックリファレンス
* [Playwrightベストプラクティス](principles/playwright_best_practices.md)
* [テストシナリオ定義書テンプレート](templates/playwright_instructions.md)
* [Playwright 公式ドキュメント](https://playwright.dev/)
