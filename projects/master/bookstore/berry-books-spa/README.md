# Berry Books SPA プロジェクト

## 📖 概要

Berry Books オンライン書店のSPAフロントエンド（React + TypeScript）です。
書籍の閲覧、カート管理、注文処理などの機能を提供します。

## 🚀 セットアップとコマンド実行ガイド

### 前提条件

* **Node.js 18以上**
* **npm または yarn**
* **バックエンドAPI**が起動していること（マイクロサービス構成）
  * `berry-books-api` - 認証・注文API
  * `back-office-api` - 書籍・在庫API
  * `customer-hub-api` - 顧客API（オプション）

> **Note:** バックエンドAPIの起動方法は、[bookstore/README.md](../README.md)を参照してください。

### セットアップ手順

```bash
# 1. プロジェクトのディレクトリに移動
cd projects/sdd-wf/bookstore/berry-books-spa

# 2. 依存関係をインストール（初回のみ）
npm install

# 3. 開発サーバーを起動（Vite）
npm run dev
```

> **Note**: Windowsでは**Git Bash**を使用してください。

開発サーバーは http://localhost:5173 で起動します。

> **Note**: このプロジェクトはViteを使用しています。高速なHMR（Hot Module Replacement）による開発体験を提供します。

インストール後、VSCodeを再読み込みすることをお勧めします：
* `Ctrl+Shift+P` → "Reload Window" を実行

### 開発サーバーの停止

```bash
# 開発サーバーのターミナルで Ctrl+C を押す
```

### プロダクション用ビルド

```bash
# プロダクション用にビルド
npm run build

# ビルド後のプレビュー（任意）
npm run preview
```

ビルドされたファイルは `dist/` ディレクトリに出力されます。

## 📍 アクセスURL

* **開発環境**: http://localhost:5173

## 🎯 プロジェクト構成

```
berry-books-spa/
├── src/
│   ├── components/         # 共通コンポーネント
│   │   ├── Layout.tsx
│   │   ├── Header.tsx
│   │   ├── PrivateRoute.tsx
│   │   └── BookCard.tsx
│   ├── contexts/           # グローバル状態管理
│   │   ├── AuthContext.tsx
│   │   └── CartContext.tsx
│   ├── pages/              # ページコンポーネント
│   │   ├── LoginPage.tsx
│   │   ├── BookListPage.tsx
│   │   ├── BookSearchPage.tsx
│   │   ├── CartPage.tsx
│   │   ├── OrderHistoryPage.tsx
│   │   └── OrderDetailPage.tsx
│   ├── services/           # API呼び出し
│   │   ├── api.ts
│   │   ├── authService.ts
│   │   ├── bookService.ts
│   │   └── orderService.ts
│   ├── types/              # TypeScript型定義
│   │   └── index.ts
│   ├── styles/             # スタイル
│   │   └── index.css
│   ├── App.tsx             # ルーティング設定
│   └── main.tsx            # エントリーポイント
├── index.html
├── package.json
├── tailwind.config.js
├── postcss.config.js
├── tsconfig.json
├── vite.config.ts
└── README.md
```

## 🔧 使用している技術

* **React 18** + **TypeScript 5**
* **React Router v6** - ルーティング
* **React Context API** - 状態管理
* **Tailwind CSS** - スタイリング
* **Axios** - HTTPクライアント
* **Vite 5** - ビルドツール

## 🎨 デザイン仕様

* **テーマカラー**: ストロベリーレッド (#CF3F4E, #A32D3A, #E55563)
* **フォント**: Segoe UI, Tahoma, Geneva, Verdana, sans-serif
* **サイト名**: Berry Books（オンライン書店）
* **デザイン**: customer-spa と統一されたスタイルを Tailwind CSS で実装
* **レスポンシブデザイン**: モダンなグラデーションとシャドウ効果

## 🎯 主な機能

### 1. 認証機能
* ログイン（メールアドレス・パスワード）
* 新規ユーザー登録
* JWT + HttpOnly Cookie による認証
* ログアウト

### 2. 書籍閲覧機能
* 書籍一覧表示
* 書籍検索（カテゴリ・キーワード）
* 書籍表紙画像表示
* 在庫状況表示

### 3. ショッピングカート
* カートへの追加
* カート内容の確認
* 個数の変更・削除
* LocalStorage での永続化

### 4. 注文機能
* 注文の作成
* 注文履歴の表示
* 注文詳細の確認
* 配送先・決済方法の指定

### 5. エラーハンドリング
* API エラーの適切な表示
* ネットワークエラー対応
* 楽観的ロック例外・在庫切れ例外の処理

## 🏗️ アーキテクチャ（BFFパターン）

このSPAは**BFF（Backend for Frontend）パターン**を採用しており、`berry-books-api-sdd`が唯一のエントリーポイントとなります。

```
Berry Books SPA (React)
    ↓ HTTP/JSON
berry-books-api-sdd (BFF)
    ├─→ back-office-api-sdd     (書籍・在庫管理)
    └─→ customer-hub-api        (顧客管理)
```

### アーキテクチャの利点
* **単一エントリーポイント**: SPAは`berry-books-api-sdd`のみにアクセス
* **バックエンド間連携**: マイクロサービス間の通信はサーバー側で完結
* **セキュリティ**: 内部APIを外部に公開しない
* **CORS簡素化**: 単一ドメインへのアクセスのみ

## 🌐 API仕様（すべてberry-books-api-sddを経由）

### 認証API
* `POST /api/auth/login` - ログイン
* `POST /api/auth/logout` - ログアウト
* `POST /api/auth/register` - 新規登録
* `GET /api/auth/me` - 現在のユーザー情報取得

### 書籍API（back-office-api-sddへプロキシ）
* `GET /api/books` - 書籍一覧取得
* `GET /api/books/{id}` - 書籍詳細取得
* `GET /api/books/search/jpql?categoryId=&keyword=` - 書籍検索（JPQL）
* `GET /api/books/search/criteria?categoryId=&keyword=` - 書籍検索（Criteria API）

### カテゴリAPI（back-office-api-sddへプロキシ）
* `GET /api/categories` - カテゴリ一覧取得

### 画像API（back-office-api-sddへプロキシ）
* `GET /api/images/covers/{bookId}` - 書籍表紙画像取得

### 注文API
* `POST /api/orders` - 注文作成
* `GET /api/orders/history` - 注文履歴取得
* `GET /api/orders/{tranId}` - 注文詳細取得

詳細は各APIのREADMEを参照してください：
* [berry-books-api-sdd](../berry-books-api-sdd/README.md)（BFF）
* [back-office-api-sdd](../back-office-api-sdd/README.md)（内部API）
* [customer-hub-api](../customer-hub-api/README.md)（内部API）

## 🔄 プロキシ設定（Vite）

開発環境では、Viteのプロキシ機能を使用してすべてのAPIリクエストを`berry-books-api-sdd`（BFF）に転送します：

```typescript
// vite.config.ts
  proxy: {
    '/api': {
    target: 'http://localhost:8080/berry-books-api-sdd',
      changeOrigin: true,
  }
}
```

`berry-books-api-sdd`が内部的に`back-office-api-sdd`や`customer-hub-api`を呼び出します。

詳細は `vite.config.ts` を参照してください。

## ⚙️ 起動手順（全体）

以下は、システム全体を起動する完全な手順です。

### ① HSQLDBサーバーを起動

```bash
# リポジトリルートで実行
./gradlew startHsqldb
```

### ② Payara Serverを起動

```bash
# リポジトリルートで実行
./gradlew startPayara
```

### ③ データベースを初期化（初回のみ）

```bash
# リポジトリルートで実行
./gradlew :berry-books-api-sdd:setupHsqldb
./gradlew :back-office-api-sdd:setupHsqldb
./gradlew :customer-hub-api:setupHsqldb
```

### ④ バックエンドAPIをデプロイ

```bash
# リポジトリルートで実行
./gradlew :berry-books-api-sdd:deploy
./gradlew :back-office-api-sdd:deploy
./gradlew :customer-hub-api:deploy
```

### ⑤ フロントエンドを起動

```bash
# berry-books-spaディレクトリで実行
cd projects/master/bookstore/berry-books-spa
npm install  # 初回のみ
npm run dev
```

### ⑥ ブラウザでアクセス

http://localhost:5173 にアクセスして書店アプリを確認できます。

**初回ログイン用テストユーザー:**
* メールアドレス: `alice@example.com`
* パスワード: `password`

または、新規登録から新しいアカウントを作成してください。

## 🛑 アプリケーションを停止する

### 停止手順

```bash
# 1. フロントエンドを停止（開発サーバーのターミナルで Ctrl+C）

# 2. バックエンドAPIをアンデプロイ（リポジトリルートで実行）
./gradlew :berry-books-api-sdd:undeploy
./gradlew :back-office-api-sdd:undeploy
./gradlew :customer-hub-api:undeploy

# 3. Payara Serverを停止（リポジトリルートで実行）
./gradlew stopPayara

# 4. HSQLDBサーバーを停止（リポジトリルートで実行）
./gradlew stopHsqldb
```

## 🧪 Playwright E2Eテスト

### 概要

このE2Eテストスイートは、Berry Booksオンライン書店の主要な機能を検証します。Page Object Modelパターンを使用して、保守性と可読性の高いテストコードを実現しています。

#### テスト対象機能

- ユーザー認証（ログイン・新規登録）
- 書籍一覧表示
- 書籍検索（静的クエリ・動的クエリ）
- カート操作（追加・削除・クリア）
- 注文処理
- 注文履歴表示

### セットアップ

#### 前提条件

- Node.js 18以上
- npm 9以上
- バックエンドAPIサーバーが起動していること

#### 1. 依存関係のインストール

```bash
npm install
```

#### 2. Playwrightブラウザのインストール

```bash
npx playwright install
```

このコマンドは、Chromium、Firefox、WebKitブラウザをインストールします。

#### 3. 環境確認とサーバー起動

テストを実行する前に、以下を確認してください：

**✅ バックエンドAPIサーバーの起動確認**

以下のAPIがすべて起動している必要があります：
```bash
# プロジェクトルートから確認
# 1. berry-books-api が起動していること
curl http://localhost:8080/berry-books-api/api/auth/me

# 2. back-office-api が起動していること
curl http://localhost:8080/back-office-api/api/books

# 3. customer-hub-api が起動していること
curl http://localhost:8080/customer-hub-api/customers
```

起動していない場合は、[bookstore/README.md](../README.md)を参照して起動してください。

**✅ フロントエンド開発サーバーの起動（重要）**

Playwrightテストを実行する前に、**別のターミナルで**フロントエンド開発サーバーを起動する必要があります：

```bash
# このディレクトリ（berry-books-spa）で実行
npm run dev
```

開発サーバーが起動すると、以下のように表示されます：
```
  VITE v5.0.8  ready in 500 ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
  ➜  press h to show help
```

> **重要**: 
> - フロントエンドサーバーは**テスト実行前に起動**しておく必要があります
> - `playwright.config.ts`では`webServer`設定がコメントアウトされているため、自動起動されません
> - テスト中はこのターミナルを開いたままにしてください

**✅ テストデータの確認**

テストデータベースに以下のテストユーザーが登録されていることを確認：
- メールアドレス: `alice@example.com`
- パスワード: `password`

### テスト実行方法

**前提**: 上記の「3. 環境確認とサーバー起動」ですべてのサーバーが起動済みであること

#### 基本的な実行

すべてのテストを実行：

```bash
npm run test:e2e
```

または

```bash
npx playwright test
```

#### UIモードで実行（デバッグ推奨）

Playwright Test UIを使用すると、テストの実行状況を視覚的に確認できます：

```bash
npm run test:e2e:ui
```

このモードでは以下が可能です：
- テストの実行・一時停止・ステップ実行
- 各ステップでのスクリーンショット確認
- ネットワークリクエストの監視
- DOMスナップショットの閲覧

#### デバッグモードで実行

特定のテストをデバッグする場合：

```bash
npm run test:e2e:debug
```

または特定のテストファイルを指定：

```bash
npx playwright test tests/complete-flow.spec.ts --debug
```

#### 特定のテストファイルを実行

```bash
npx playwright test tests/basic-flow.spec.ts
```

#### ヘッドレスモードをオフにして実行

ブラウザを表示しながらテストを実行：

```bash
npx playwright test --headed
```

#### テストレポートを表示

テスト実行後、HTMLレポートを表示：

```bash
npm run test:e2e:report
```

または

```bash
npx playwright show-report
```

### テストシナリオ

#### シナリオ 1: 基本フロー（検証なし）

**ファイル**: `tests/basic-flow.spec.ts`

**目的**: ログインから注文完了までの基本操作を確認する

**テスト内容**:
- ログイン
- 書籍一覧から複数の書籍をカートに追加
- 書籍検索機能を使用してカートに追加（カテゴリ選択あり）
- カート内の商品を削除（ダイアログ確認あり）
- 注文確認・注文実行
- ログアウト

**実行時間**: 約30秒

**注意事項**:
- カテゴリ選択肢はAPIから非同期に読み込まれるため、読み込み完了を待つ必要があります
- カート削除時の確認ダイアログは事前にハンドラーを設定します

#### シナリオ 2: 完全フロー（検証あり）

**ファイル**: `tests/complete-flow.spec.ts`

**目的**: 全画面遷移と各ページの要素を検証する

**テスト内容**:
- 各ページのタイトル検証
- 書籍一覧の件数確認
- 検索結果の検証
- カート内商品数の確認
- 注文IDの検証
- URL遷移の検証

**実行時間**: 約45秒

**特徴**: 最も包括的なE2Eテストで、UI要素とデータの整合性を詳細に検証します。

#### シナリオ 3: 新規顧客登録

**ファイル**: `tests/customer-registration.spec.ts`

**目的**: 新規ユーザー登録フローを確認する

**テスト内容**:
- 新規登録フォームの表示
- ユーザー情報の入力
- 登録完了後の自動ログイン
- ヘッダーにユーザー名が表示されることを確認

**実行時間**: 約15秒

#### シナリオ 4: 書籍検索と注文（Criteria API版）

**ファイル**: `tests/book-search-and-order.spec.ts`

**目的**: 動的クエリ（Criteria API）を使った検索から注文までのフローを確認する

**テスト内容**:
- Criteria API版の書籍検索
- 検索結果からカートに追加
- 沖縄県への配送（配送料¥1,700）
- クレジットカード決済
- 注文履歴の表示

**実行時間**: 約25秒

**特徴**: バックエンドの動的クエリ機能と配送料計算ロジックを検証します。

#### シナリオ 5: カート操作（複数商品の削除）

**ファイル**: `tests/cart-operations.spec.ts`

**目的**: カート内の複数商品を同時に削除する操作を確認する

**テスト内容**:
- 複数商品をカートに追加
- 複数商品を選択して一括削除
- カートのクリア
- 空のカートメッセージの表示確認

**実行時間**: 約20秒

**特徴**: カート操作の複雑なインタラクションを検証します。

### 生成されたファイル一覧

#### ディレクトリ構造

```
berry-books-spa/
├── tests/
│   ├── pages/                              # Page Objectsディレクトリ
│   │   ├── LoginPage.ts                   # ログインページ
│   │   ├── RegisterPage.ts                # 新規登録ページ
│   │   ├── BookListPage.ts                # 書籍一覧ページ
│   │   ├── BookSearchPage.ts              # 書籍検索ページ
│   │   ├── CartPage.ts                    # カートページ
│   │   ├── CartAddedPage.ts               # カート追加確認ページ
│   │   ├── OrderConfirmPage.ts            # 注文確認ページ
│   │   ├── OrderSuccessPage.ts            # 注文完了ページ
│   │   └── HeaderNav.ts                   # ヘッダーナビゲーション
│   ├── basic-flow.spec.ts                 # シナリオ1のテスト
│   ├── complete-flow.spec.ts              # シナリオ2のテスト
│   ├── customer-registration.spec.ts      # シナリオ3のテスト
│   ├── book-search-and-order.spec.ts      # シナリオ4のテスト
│   └── cart-operations.spec.ts            # シナリオ5のテスト
├── playwright.config.ts                    # Playwright設定ファイル
├── package.json                            # 依存関係（更新済み）
└── README.md                               # このファイル
```

#### Page Objectsの概要

各Page Objectクラスは以下を提供します：

- **Locator定義**: ページ上の要素を識別するセレクタ
- **アクションメソッド**: ページ固有の操作（例: `login()`, `addToCart()`）
- **ナビゲーションメソッド**: ページ遷移（例: `goto()`）

**メリット**:
- テストコードの可読性向上
- 要素セレクタの一元管理
- UI変更時のメンテナンス性向上

### トラブルシューティング

#### テストが失敗する場合

##### 1. バックエンドAPIサーバーが起動していない

**症状**: テストがタイムアウトする、または404エラーが発生する

**解決策**:
```bash
# バックエンドサーバーを起動
cd ../../back-office-api
./gradlew bootRun
```

##### 2. フロントエンドサーバーが起動していない

**症状**: `localhost:5173`に接続できない

**解決策**:
Playwrightは自動的にフロントエンドサーバーを起動しますが、手動で起動することも可能です：
```bash
npm run dev
```

##### 3. ポート5173が使用中

**症状**: `Address already in use`エラー

**解決策**:
```bash
# プロセスを確認
lsof -i :5173

# プロセスを終了
kill -9 <PID>
```

##### 4. テストデータが不足している

**症状**: 書籍が見つからない、検索結果が0件

**解決策**:
データベースを初期化してテストデータを投入してください。

##### 5. ダイアログが表示されずにテストが失敗する

**症状**: `dialog.accept()` や `dialog.dismiss()` が失敗する

**解決策**:
ダイアログハンドラーは、ダイアログが表示される**前**に設定する必要があります：
```typescript
// ✅ 正しい
page.on('dialog', async dialog => await dialog.accept());
await button.click(); // ダイアログを表示するボタン

// ❌ 間違い
await button.click();
page.on('dialog', async dialog => await dialog.accept()); // 遅すぎる
```

##### 6. カテゴリ選択で "did not find some options" エラーが発生する

**症状**: `selectOption: Test timeout of 30000ms exceeded` と `did not find some options` エラーが発生する

**原因**: カテゴリ選択肢がAPIから非同期に読み込まれるため、選択肢が読み込まれる前に `selectOption` を実行している

**解決策**:
カテゴリ選択肢が読み込まれるのを待ってから選択を実行します：
```typescript
// カテゴリ選択肢が2つ以上になるのを待つ（"すべて"以外の選択肢が読み込まれる）
await page.locator('#category option').nth(1).waitFor({ state: 'attached' });

// その後、選択を実行
await categorySelect.selectOption('2');
```

または、より柔軟なアプローチ：
```typescript
// 最初の選択肢（"すべて"以外）の値を取得して選択
const firstOptionValue = await page.locator('#category option').nth(1).getAttribute('value');
if (firstOptionValue) {
  await categorySelect.selectOption(firstOptionValue);
}
```

#### パフォーマンスの問題

##### テストの実行が遅い

**解決策1**: 並列実行ワーカー数を増やす

`playwright.config.ts` で以下を変更：
```typescript
workers: process.env.CI ? 1 : 4, // 4ワーカーに増やす
```

**解決策2**: 特定のテストのみ実行

```bash
npx playwright test tests/basic-flow.spec.ts
```

#### デバッグのヒント

##### 1. スクリーンショットを確認する

失敗したテストのスクリーンショットは `test-results/` ディレクトリに保存されます。

##### 2. トレースを確認する

```bash
npx playwright show-trace test-results/.../.../trace.zip
```

トレースビューアーで以下を確認できます：
- 各ステップのスクリーンショット
- ネットワークリクエスト
- コンソールログ
- DOMスナップショット

##### 3. ヘッドレスモードをオフにする

```bash
npx playwright test --headed --debug
```

ブラウザを表示しながらステップ実行できます。

##### 4. `page.pause()` を使用する

テストコード内で一時停止：
```typescript
await page.pause(); // インスペクタが開く
```

### CI/CD統合

#### GitHub Actions の例

`.github/workflows/e2e-tests.yml`:

```yaml
name: E2E Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: 18
          
      - name: Install dependencies
        run: npm ci
        
      - name: Install Playwright browsers
        run: npx playwright install --with-deps
        
      - name: Run E2E tests
        run: npm run test:e2e
        
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: playwright-report
          path: playwright-report/
```

## 📖 参考リンク

* [React Documentation](https://react.dev/)
* [TypeScript Documentation](https://www.typescriptlang.org/docs/)
* [Vite Documentation](https://vitejs.dev/)
* [Tailwind CSS Documentation](https://tailwindcss.com/docs)
* [Playwright公式ドキュメント](https://playwright.dev/)
* [Page Object Model パターン](https://playwright.dev/docs/pom)
* [Playwrightベストプラクティス](https://playwright.dev/docs/best-practices)
* [berry-books-api-sdd](../berry-books-api-sdd/README.md)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。

