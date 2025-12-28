# Berry Books SPA プロジェクト

## 📖 概要

Berry Books オンライン書店のSPAフロントエンド（React + TypeScript）です。
書籍の閲覧、カート管理、注文処理などの機能を提供します。

## 🚀 セットアップとコマンド実行ガイド

### 前提条件

- Node.js 18以上
- npm または yarn
- バックエンドAPIが起動していること（マイクロサービス構成）

### ③ 依存関係の確認

このプロジェクトを開始する前に、以下が起動していることを確認してください：

**※ 全てのGradleコマンドはリポジトリルートから実行してください**

- **① HSQLDBサーバー** （`./gradlew startHsqldb`）
- **② Payara Server** （`./gradlew startPayara`）
- **③ バックエンドAPI**（マイクロサービス構成）
  - `./gradlew :berry-books-api:deploy` - 認証・注文API
  - `./gradlew :back-office-api:deploy` - 書籍・在庫API
  - `./gradlew :customer-hub-api:deploy` - 顧客API（オプション）

### ④ プロジェクトを開始するときに1回だけ実行

```bash
# 1. プロジェクトのディレクトリに移動
cd projects/master/bookstore/berry-books-spa

# 2. 依存関係をインストール（初回のみ）
npm install

# 3. 開発サーバーを起動（Vite）
npm run dev
```

> **Note**: Windowsでは**Git Bash**を使用してください。

開発サーバーは http://localhost:5173 で起動します。

> **Note**: このプロジェクトはViteを使用しています。高速なHMR（Hot Module Replacement）による開発体験を提供します。

インストール後、VSCodeを再読み込みすることをお勧めします：
- `Ctrl+Shift+P` → "Reload Window" を実行

### ⑤ プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# 開発サーバーのターミナルで Ctrl+C を押す
```

### ⑥ アプリケーション作成・更新のたびに実行

開発中はファイルを保存すると**自動的に再読み込み**されます（HMR）。手動での再起動は不要です。

**プロダクション用ビルド:**

```bash
# プロダクション用にビルド
npm run build

# ビルド後のプレビュー（任意）
npm run preview
```

ビルドされたファイルは `dist/` ディレクトリに出力されます。

## 📍 アクセスURL

- **開発環境**: http://localhost:5173

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

- **React 18** + **TypeScript 5**
- **React Router v6** - ルーティング
- **React Context API** - 状態管理
- **Tailwind CSS** - スタイリング
- **Axios** - HTTPクライアント
- **Vite 5** - ビルドツール

## 🎨 デザイン仕様

- **テーマカラー**: ストロベリーレッド (#CF3F4E, #A32D3A, #E55563)
- **フォント**: Segoe UI, Tahoma, Geneva, Verdana, sans-serif
- **サイト名**: Berry Books（オンライン書店）
- **デザイン**: customer-spa と統一されたスタイルを Tailwind CSS で実装
- **レスポンシブデザイン**: モダンなグラデーションとシャドウ効果

## 🎯 主な機能

### 1. 認証機能
- ログイン（メールアドレス・パスワード）
- 新規ユーザー登録
- JWT + HttpOnly Cookie による認証
- ログアウト

### 2. 書籍閲覧機能
- 書籍一覧表示
- 書籍検索（カテゴリ・キーワード）
- 書籍表紙画像表示
- 在庫状況表示

### 3. ショッピングカート
- カートへの追加
- カート内容の確認
- 個数の変更・削除
- LocalStorage での永続化

### 4. 注文機能
- 注文の作成
- 注文履歴の表示
- 注文詳細の確認
- 配送先・決済方法の指定

### 5. エラーハンドリング
- API エラーの適切な表示
- ネットワークエラー対応
- 楽観的ロック例外・在庫切れ例外の処理

## 🏗️ アーキテクチャ（BFFパターン）

このSPAは**BFF（Backend for Frontend）パターン**を採用しており、`berry-books-api`が唯一のエントリーポイントとなります。

```
Berry Books SPA (React)
    ↓ HTTP/JSON
berry-books-api (BFF)
    ├─→ back-office-api     (書籍・在庫管理)
    └─→ customer-hub-api   (顧客管理)
```

### アーキテクチャの利点
- **単一エントリーポイント**: SPAは`berry-books-api`のみにアクセス
- **バックエンド間連携**: マイクロサービス間の通信はサーバー側で完結
- **セキュリティ**: 内部APIを外部に公開しない
- **CORS簡素化**: 単一ドメインへのアクセスのみ

## 🌐 API仕様（すべてberry-books-apiを経由）

### 認証API
- `POST /api/auth/login` - ログイン
- `POST /api/auth/logout` - ログアウト
- `POST /api/auth/register` - 新規登録
- `GET /api/auth/me` - 現在のユーザー情報取得

### 書籍API（back-office-apiへプロキシ）
- `GET /api/books` - 書籍一覧取得
- `GET /api/books/{id}` - 書籍詳細取得
- `GET /api/books/search/jpql?categoryId=&keyword=` - 書籍検索（JPQL）
- `GET /api/books/search/criteria?categoryId=&keyword=` - 書籍検索（Criteria API）

### カテゴリAPI（back-office-apiへプロキシ）
- `GET /api/categories` - カテゴリ一覧取得

### 画像API（back-office-apiへプロキシ）
- `GET /api/images/covers/{bookId}` - 書籍表紙画像取得

### 注文API
- `POST /api/orders` - 注文作成
- `GET /api/orders/history` - 注文履歴取得
- `GET /api/orders/{tranId}` - 注文詳細取得

詳細は各APIのREADMEを参照してください：
- [berry-books-api](../berry-books-api/README.md)（BFF）
- [back-office-api](../back-office-api/README.md)（内部API）
- [customer-hub-api](../customer-hub-api/README.md)（内部API）

## 🔄 プロキシ設定（Vite）

開発環境では、Viteのプロキシ機能を使用してすべてのAPIリクエストを`berry-books-api`（BFF）に転送します：

```typescript
// vite.config.ts
  proxy: {
    '/api': {
    target: 'http://localhost:8080/berry-books-api',
      changeOrigin: true,
  }
}
```

`berry-books-api`が内部的に`back-office-api`や`customer-hub-api`を呼び出します。

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
./gradlew :berry-books-api:setupHsqldb
./gradlew :back-office-api:setupHsqldb
./gradlew :customer-hub-api:setupHsqldb
```

### ④ バックエンドAPIをデプロイ

```bash
# リポジトリルートで実行
./gradlew :berry-books-api:deploy
./gradlew :back-office-api:deploy
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
- メールアドレス: `alice@gmail.com`
- パスワード: `password`

または、新規登録から新しいアカウントを作成してください。

## 🛑 アプリケーションを停止する

### 停止手順

```bash
# 1. フロントエンドを停止（開発サーバーのターミナルで Ctrl+C）

# 2. バックエンドAPIをアンデプロイ（リポジトリルートで実行）
./gradlew :berry-books-api:undeploy
./gradlew :back-office-api:undeploy
./gradlew :customer-hub-api:undeploy

# 3. Payara Serverを停止（リポジトリルートで実行）
./gradlew stopPayara

# 4. HSQLDBサーバーを停止（リポジトリルートで実行）
./gradlew stopHsqldb
```

## 📖 参考リンク

- [React Documentation](https://react.dev/)
- [TypeScript Documentation](https://www.typescriptlang.org/docs/)
- [Vite Documentation](https://vitejs.dev/)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [berry-books-api](../berry-books-api/README.md)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。

