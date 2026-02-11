# Playwright E2Eテストコード生成指示書

バージョン: 1.0.0  
最終更新日: 2026-02-11

---

## 目的

Markdown形式のテストシナリオ定義書から、Webアプリケーション向けのPlaywright E2Eテストコード（TypeScript）を自動生成する。

---

## パラメータ

| パラメータ名 | 必須 | デフォルト値 | 説明 |
|------------|------|-------------|------|
| `project_root` | ✓ | - | プロジェクトのルートディレクトリパス |
| `instructions_file` | ✓ | - | テストシナリオ定義書のファイルパス（Markdown形式） |
| `test_output_dir` | - | `{project_root}/tests` | テストコードの出力ディレクトリ |
| `page_objects_dir` | - | `{test_output_dir}/pages` | Page Objectsの出力ディレクトリ |
| `use_page_objects` | - | `true` | Page Object Modelパターンを使用するか |

---

## 実行手順

### ステップ1: テストシナリオ定義書の読み込みと解析

1. **テストシナリオ定義書を読み込む**
   ```
   instructions_file のパスからMarkdownファイルを読み込む
   ```

2. **前提条件を抽出**
   - アプリケーションURL（baseURL）
   - テストユーザーの認証情報
   - その他の前提条件

3. **テストシナリオ一覧を抽出**
   - シナリオ名
   - 説明
   - 検証の有無

4. **各シナリオの詳細を解析**
   - 操作手順（表形式）
   - 操作の種類（ページを開く、入力、クリック、確認、待機等）
   - 対象要素（日本語での記述）
   - 入力値/期待値
   - 備考

### ステップ2: プロジェクト構造の確認

1. **プロジェクトルートの確認**
   ```
   project_root が存在することを確認
   ```

2. **出力ディレクトリの準備**
   ```
   test_output_dir が存在しない場合は作成
   page_objects_dir が存在しない場合は作成（use_page_objects=trueの場合）
   ```

3. **既存ファイルの確認**
   - `playwright.config.ts` の存在確認
   - `package.json` の存在確認
   - 既存テストファイルの確認

### ステップ3: Page Objects の生成（use_page_objects=true の場合）

1. **ページを識別**
   - テストシナリオ定義書からページ遷移を解析
   - 各ページで使用される要素を抽出
   - ページ名を決定（例: LoginPage, BookListPage, CartPage等）

2. **アプリケーションコードから実際のセレクタを推論**
   - `{project_root}/src/` 配下のファイルを解析
   - シナリオ定義書の日本語要素名（例：「メールアドレス」「ログインボタン」）から適切なセレクタを推論
   - 優先順位: `data-testid` > `id` > テキスト > `type`属性 > CSSセレクタ
   - 例：
     - 「メールアドレス」→ `input[type="email"]` または `#email`
     - 「ログインボタン」→ `button[type="submit"]` または `button:has-text("ログイン")`
     - 「カートリンク」→ `a:has-text("カート")`

3. **各ページのPage Objectクラスを生成**
   
   **ファイル名**: `{page_objects_dir}/{ページ名}.ts`
   
   **テンプレート構造**:
   ```typescript
   import { Page, Locator } from '@playwright/test';
   
   export class {ページ名} {
     readonly page: Page;
     // 要素のLocator宣言（推論したセレクタを使用）
     readonly {要素名}: Locator;
     
     constructor(page: Page) {
       this.page = page;
       // 要素のLocatorを初期化
       this.{要素名} = page.locator('{推論したセレクタ}');
     }
     
     // ページ固有のアクションメソッド
     async goto() {
       await this.page.goto('{ページURL}');
     }
     
     async {アクション名}({パラメータ}) {
       // アクションの実装
     }
   }
   ```

4. **命名規則**
   - クラス名: PascalCase（例: `LoginPage`）
   - プロパティ名: camelCase（例: `emailInput`, `loginButton`）
   - メソッド名: camelCase（例: `login`, `fillForm`, `submitOrder`）

### ステップ4: テストコードの生成

1. **各シナリオに対応するテストファイルを生成**
   
   **ファイル名**: `{test_output_dir}/{シナリオ名をケバブケースに変換}.spec.ts`
   
   例: 
   - シナリオ「基本フロー（検証なし）」→ `basic-flow.spec.ts`
   - シナリオ「完全フロー（検証あり）」→ `complete-flow.spec.ts`

2. **テストファイルの構造**
   
   ```typescript
   import { test, expect } from '@playwright/test';
   // Page Objectsをインポート（use_page_objects=trueの場合）
   import { LoginPage } from './pages/LoginPage';
   
   test.describe('{シナリオ名}', () => {
     test('{テスト説明}', async ({ page }) => {
       // ダイアログハンドラーの設定（必要に応じて）
       page.on('dialog', async dialog => {
         await dialog.accept(); // または dialog.dismiss()
       });
       
       // テスト手順の実装
       // ...
     });
   });
   ```

3. **操作の実装（セレクタを自動推論）**
   
   シナリオ定義書の日本語要素名から、Reactコンポーネントを解析して適切なセレクタを自動推論します。
   
   **サポートされる操作一覧**:
   - ページを開く
   - 入力
   - クリック
   - 選択（セレクトボックス）
   - 待機（要素読み込み、APIレスポンス等）
   - 確認（テキスト、URL、要素の状態等）
   - ダイアログ受諾（OKをクリック）
   - ダイアログ却下（キャンセルをクリック）
   
   **ページを開く**:
   ```typescript
   await page.goto('{URL}');
   ```
   
   **入力**（例：「メールアドレス」→ `input[type="email"]`）:
   ```typescript
   const emailInput = page.locator('input[type="email"]'); // 推論
   await emailInput.fill('{入力値}');
   ```
   
   **クリック**（例：「ログインボタン」→ `button[type="submit"]`）:
   ```typescript
   const loginButton = page.locator('button[type="submit"]'); // 推論
   await loginButton.click();
   ```
   
   **選択**（例：「カテゴリ」→ `#category`）:
   ```typescript
   const categorySelect = page.locator('#category'); // 推論
   
   // 選択肢が動的に読み込まれる場合は、読み込み完了を待つ
   // 例：APIから取得される場合
   await page.locator('#category option').nth(1).waitFor({ state: 'attached' });
   
   await categorySelect.selectOption('{値}');
   ```
   
   **注意**: セレクトボックスの選択肢がAPIから非同期に読み込まれる場合、選択肢が読み込まれるのを待つ必要があります。
   
   **確認（テキスト）**（例：「ページタイトル」→ `h2`）:
   ```typescript
   const title = page.locator('h2'); // 推論
   await expect(title).toContainText('{期待値}');
   ```
   
   **確認（URL）**:
   ```typescript
   await expect(page).toHaveURL('{期待URL}');
   ```
   
   **待機（URL遷移）**:
   ```typescript
   await page.waitForURL('{URL}');
   ```
   
   **待機（要素表示）**:
   ```typescript
   await page.waitForSelector('{推論したセレクタ}');
   ```
   
   **待機（選択肢の読み込み完了）** - セレクトボックスの選択肢がAPIから読み込まれる場合:
   ```typescript
   // 例：カテゴリ選択肢が2つ以上になるのを待つ
   await page.locator('#category option').nth(1).waitFor({ state: 'attached' });
   ```
   
   **ダイアログ処理**:
   ```typescript
   // OKをクリック（単純なケース）
   page.on('dialog', async dialog => await dialog.accept());
   
   // キャンセルをクリック
   page.on('dialog', async dialog => await dialog.dismiss());
   ```
   
   **重要**: ダイアログハンドラーは、ダイアログが表示される**前**に設定する必要があります。
   
   **推奨されないパターン**:
   - 複雑な条件分岐（キャンセル→再試行など）
   - フラグを使った状態管理
   
   **推奨されるパターン**:
   - シンプルな accept() または dismiss()
   - テストシナリオ自体をシンプルに保つ（複雑なダイアログフローは避ける）

4. **Page Object Modelを使用する場合**
   
   ```typescript
   const loginPage = new LoginPage(page);
   await loginPage.goto();
   await loginPage.login('email@example.com', 'password');
   ```

5. **コメントの追加**
   - 各ステップにテストシナリオ定義書のステップ番号をコメントで追加
   - 複雑な処理には説明コメントを追加

### ステップ5: 設定ファイルの生成

1. **playwright.config.ts の生成**（存在しない場合のみ）
   
   ```typescript
   import { defineConfig, devices } from '@playwright/test';
   
   export default defineConfig({
     testDir: './tests',
     fullyParallel: true,
     forbidOnly: !!process.env.CI,
     retries: process.env.CI ? 2 : 0,
     workers: process.env.CI ? 1 : undefined,
     reporter: 'html',
     use: {
       baseURL: '{テストシナリオ定義書から抽出したbaseURL}',
       trace: 'on-first-retry',
       screenshot: 'only-on-failure',
       video: 'retain-on-failure',
     },
     projects: [
       {
         name: 'chromium',
         use: { ...devices['Desktop Chrome'] },
       },
     ],
     webServer: {
       command: 'npm run dev',
       url: '{テストシナリオ定義書から抽出したbaseURL}',
       reuseExistingServer: true, // 既存サーバーを再利用（バックグラウンド実行を避ける）
       timeout: 120 * 1000, // 2分
     },
   });
   ```

2. **package.json への依存関係追加**（存在しない場合）
   
   ```json
   {
     "devDependencies": {
       "@playwright/test": "^1.40.0",
       "@types/node": "^20.0.0"
     },
     "scripts": {
       "test:e2e": "playwright test",
       "test:e2e:ui": "playwright test --ui",
       "test:e2e:debug": "playwright test --debug"
     }
   }
   ```

### ステップ6: READMEの生成

1. **README_PLAYWRIGHT.md の生成**
   
   内容:
   - テストの概要
   - セットアップ手順
   - テスト実行方法
   - トラブルシューティング
   - 生成されたファイル一覧
   - 各シナリオの説明

---

## 生成ルール

### 必須事項

1. **遵守するベストプラクティス**
   - `@agent_skills/playwright-react/principles/playwright_best_practices.md` を参照
   - Page Object Modelパターンの適用（use_page_objects=trueの場合）
   - セレクタの優先順位: data-testid > id > text > CSS
   - 自動待機の活用、明示的waitForTimeout()は最小限に
   - **非同期読み込み対策**: セレクトボックスの選択肢がAPIから読み込まれる場合、読み込み完了を待つ
   - **ダイアログ処理**: シンプルな accept/dismiss のみを使用し、複雑な条件分岐は避ける
   - **webServer設定**: `reuseExistingServer: true` を常に使用（バックグラウンド実行を避ける）

2. **TypeScript型定義**
   - 厳密な型定義を使用
   - `any` 型の使用は避ける
   - Page Objectクラスはすべてのプロパティに型を定義

3. **エラーハンドリング**
   - タイムアウト設定を適切に
   - スクリーンショット・動画記録を有効化
   - 失敗時のデバッグ情報を充実

4. **コード品質**
   - ESLint/Prettierに準拠
   - コメントは日本語で記述
   - 変数名・メソッド名は英語（camelCase）

### 任意事項

1. **テストデータの分離**
   - 可能であれば`tests/fixtures/test-data.ts`にテストデータを分離

2. **ヘルパー関数**
   - 共通処理は`tests/helpers/`に分離

3. **カスタムフィクスチャ**
   - 認証状態の保存等、必要に応じてフィクスチャを作成

---

## 出力ファイル一覧

生成されるファイルの一覧:

```
{project_root}/
├── tests/                                # テストディレクトリ
│   ├── pages/                            # Page Objects（use_page_objects=trueの場合）
│   │   ├── {Page1Name}.ts
│   │   ├── {Page2Name}.ts
│   │   └── ...
│   ├── {scenario1-name}.spec.ts         # シナリオ1のテスト
│   ├── {scenario2-name}.spec.ts         # シナリオ2のテスト
│   └── ...
├── playwright.config.ts                  # Playwright設定（存在しない場合のみ）
├── package.json                          # 依存関係（更新）
└── README_PLAYWRIGHT.md                  # テスト実行ガイド
```

---

## 完了確認

すべてのファイル生成が完了したら、以下を確認してユーザーに報告する:

1. **生成されたファイル一覧**
   - Page Objectsの数
   - テストファイルの数
   - 設定ファイルの有無

2. **次のステップの案内**
   ```
   以下のコマンドでテストを実行できます:
   
   # 依存関係のインストール
   npm install
   
   # Playwrightのインストール
   npx playwright install
   
   # テストの実行
   npx playwright test
   
   # UIモードでの実行（デバッグ用）
   npx playwright test --ui
   ```

3. **注意事項**
   - アプリケーションサーバーが起動していることを確認
   - 必要に応じてbaseURLを調整
   - テストデータの準備が必要な場合はその旨を伝える

---

## よくある問題と対策

### 1. セレクトボックスの選択肢が見つからない（"did not find some options"）

**原因**: 選択肢がAPIから非同期に読み込まれるため、読み込み完了前に`selectOption`を実行している

**対策**:
```typescript
// 選択肢の読み込み完了を待つ
await page.locator('#category option').nth(1).waitFor({ state: 'attached' });
await categorySelect.selectOption('2');
```

テストシナリオ定義書では、「選択」操作の前に「待機」ステップを追加することを推奨：

| No. | 操作 | 対象 | 入力値/期待値 | 備考 |
|-----|------|------|-------------|------|
| 12 | 待機 | カテゴリ選択肢 | 読み込み完了を待つ | APIから取得 |
| 13 | 選択 | カテゴリ | 最初の選択肢（すべて以外） |  |

### 2. ダイアログハンドラーの複雑な条件分岐

**避けるべきパターン**:
```typescript
// ❌ 複雑な条件分岐（バグの原因になりやすい）
let dialogAccepted = false;
page.on('dialog', async (dialog) => {
  if (!dialogAccepted) {
    await dialog.dismiss();
    dialogAccepted = false; // バグ: 常にfalseのまま
  } else {
    await dialog.accept();
  }
});
```

**推奨パターン**:
```typescript
// ✅ シンプルなダイアログ処理
page.on('dialog', async dialog => await dialog.accept());
```

テストシナリオ自体をシンプルにし、「キャンセル→再試行」のような複雑なフローは避けることを推奨します。

### 3. 検索結果が表示されない（非同期検索処理）

**原因**: 検索ボタンをクリックした直後は、まだ検索処理中（loading状態）で検索結果が非表示

**対策**: 検索ボタンの`disabled`属性を監視して、検索処理の完了を待つ
```typescript
// Page Objectに検索結果待機メソッドを追加
async waitForSearchResults() {
  // 検索ボタンがdisabled属性を持たなくなるまで待つ（検索処理完了の確認）
  await this.page.waitForFunction(
    () => {
      const jpqlButton = document.querySelector('#search1Button');
      const criteriaButton = document.querySelector('#search2Button');
      return (jpqlButton && !jpqlButton.hasAttribute('disabled')) ||
             (criteriaButton && !criteriaButton.hasAttribute('disabled'));
    },
    { timeout: 15000 }
  );
  // 検索処理が完了してDOMが更新されるまで少し待機
  await this.page.waitForTimeout(300);
}

// テストコードで使用
await bookSearchPage.searchJpqlButton.click();
await bookSearchPage.waitForSearchResults(); // 検索処理完了を待つ
await expect(bookSearchPage.searchResults).toBeVisible();
```

### 4. 検索結果が見つからない（テストデータ不足）

**原因**: 検索条件が厳しすぎて、テストデータベースに該当データが存在しない

**対策**: 確実に結果が表示される条件を選択
- カテゴリは「すべて」を選択
- キーワードは一般的で該当データが多いもの（例：「Java」「Python」など）を使用

```markdown
| 13 | 選択 | カテゴリ | すべて |  |
| 14 | 入力 | キーワード | `Java` |  |
```

**避けるべき例**:
```markdown
| 13 | 選択 | カテゴリ | 最初の選択肢（すべて以外） |  |
| 14 | 入力 | キーワード | `Cloud` |  |
```
→ 特定カテゴリ + 特定キーワードの組み合わせで該当データがない可能性

### 5. テストがバックグラウンドで長時間実行される

**原因**: `webServer.reuseExistingServer`が`false`または`!process.env.CI`で、環境変数が正しく設定されていない

**対策**: `playwright.config.ts`で常に`true`に設定
```typescript
webServer: {
  command: 'npm run dev',
  url: 'http://localhost:5173',
  reuseExistingServer: true, // 既存サーバーを再利用
  timeout: 120 * 1000,
},
```

---

## エラーハンドリング

### テストシナリオ定義書が見つからない場合

```
エラー: テストシナリオ定義書が見つかりません
ファイルパス: {instructions_file}

以下を確認してください:
1. ファイルパスが正しいか
2. ファイルが存在するか
3. 読み取り権限があるか
```

### テストシナリオ定義書のフォーマットが不正な場合

```
警告: テストシナリオ定義書のフォーマットが期待と異なります

以下を確認してください:
1. シナリオ一覧が表形式で記述されているか
2. 各シナリオの詳細が表形式で記述されているか
3. 必須カラム（No., 操作, セレクタ/ID等）が存在するか

テンプレートを参照してください:
@agent_skills/playwright-react/templates/playwright_instructions.md
```

### プロジェクトルートが見つからない場合

```
エラー: プロジェクトルートが見つかりません
ディレクトリパス: {project_root}

以下を確認してください:
1. ディレクトリパスが正しいか
2. ディレクトリが存在するか
3. アクセス権限があるか
```

---

## 実装例

### 例1: ログインシナリオ

**テストシナリオ定義書の入力**（人が書く）:

| No. | 操作 | 対象 | 入力値/期待値 | 備考 |
|-----|------|------|-------------|------|
| 1 | ページを開く |  | `http://localhost:5173/` |  |
| 2 | 入力 | メールアドレス | `alice@example.com` |  |
| 3 | 入力 | パスワード | `password` |  |
| 4 | クリック | ログインボタン |  |  |
| 5 | 確認 | URL | `/books` に遷移 |  |

**生成されるPage Object**:

```typescript
// tests/pages/LoginPage.ts
import { Page, Locator } from '@playwright/test';

export class LoginPage {
  readonly page: Page;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly submitButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.emailInput = page.locator('input[type="email"]');
    this.passwordInput = page.locator('input[type="password"]');
    this.submitButton = page.locator('button[type="submit"]');
  }

  async goto() {
    await this.page.goto('/');
  }

  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.submitButton.click();
  }
}
```

**生成されるテストコード**:

```typescript
// tests/login.spec.ts
import { test, expect } from '@playwright/test';
import { LoginPage } from './pages/LoginPage';

test.describe('ログインシナリオ', () => {
  test('正常にログインできる', async ({ page }) => {
    // ステップ1: ページを開く
    const loginPage = new LoginPage(page);
    await loginPage.goto();

    // ステップ2-4: ログイン
    await loginPage.login('alice@example.com', 'password');

    // ステップ5: URLを検証
    await expect(page).toHaveURL('/books');
  });
});
```

---

## 参考資料

* [Playwrightベストプラクティス](../principles/playwright_best_practices.md)
* [テストシナリオ定義書テンプレート](../templates/playwright_instructions.md)
* [Playwright 公式ドキュメント](https://playwright.dev/)
* [Page Object Model パターン](https://playwright.dev/docs/pom)
