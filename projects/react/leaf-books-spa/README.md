# Leaf Books SPA プロジェクト

## 📖 概要

Leaf Books オンライン書店のSPAフロントエンド（React + TypeScript）です。
書籍の閲覧、カート管理、注文処理などの機能を提供します。

## 🚀 セットアップとコマンド実行ガイド

### 前提条件

- Node.js 18以上
- npm または yarn
- leaf-books-api（バックエンド）が起動していること

### ③ 依存関係の確認

このプロジェクトを開始する前に、以下が起動していることを確認してください：

- **① HSQLDBサーバー** （`./gradlew startHsqldb`）
- **② leaf-books-api** （`cd projects/leaf-books-api && ./gradlew bootRun`）

### ④ プロジェクトを開始するときに1回だけ実行

```bash
# 1. プロジェクトのディレクトリに移動
cd projects/leaf-books-spa

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
leaf-books-spa/
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

- **テーマカラー**: 新緑グリーン (#8BC34A, #7CB342, #558B2F)
- **フォント**: Segoe UI, Tahoma, Geneva, Verdana, sans-serif
- **サイト名**: Leaf Books（テックブックストア）
- **デザイン**: leaf-books-mvc のスタイルを Tailwind CSS で再現
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

## 🌐 API仕様

このフロントエンドは leaf-books-api の以下のAPIを使用します：

### 認証API
- `POST /api/auth/login` - ログイン
- `POST /api/auth/logout` - ログアウト
- `POST /api/auth/register` - 新規登録
- `GET /api/auth/me` - 現在のユーザー情報取得

### 書籍API
- `GET /api/books` - 書籍一覧取得
- `GET /api/books/{id}` - 書籍詳細取得
- `GET /api/books/search?categoryId=&keyword=` - 書籍検索
- `GET /api/categories` - カテゴリ一覧取得

### 注文API
- `POST /api/orders` - 注文作成
- `GET /api/orders/history` - 注文履歴取得
- `GET /api/orders/{tranId}` - 注文詳細取得

### 画像API
- `GET /api/images/covers/{bookId}` - 書籍表紙画像取得

詳細は [leaf-books-api](../leaf-books-api/README.md) を参照してください。

## 🔄 プロキシ設定

開発環境では、Viteのプロキシ機能を使用してAPIリクエストを転送します。

```typescript
// vite.config.ts
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    }
  }
}
```

## ⚙️ 起動手順（全体）

以下は、システム全体を起動する完全な手順です。

### ① HSQLDBサーバーを起動

```bash
# プロジェクトルートで実行
./gradlew startHsqldb
```

### ② leaf-books-api を起動

```bash
# プロジェクトルートで実行
cd projects/leaf-books-api
./gradlew bootRun
```

### ③ フロントエンドを起動

```bash
# leaf-books-spaディレクトリで実行
cd projects/leaf-books-spa
npm install  # 初回のみ
npm run dev
```

### ④ ブラウザでアクセス

http://localhost:5173 にアクセスして書店アプリを確認できます。

**初回ログイン用テストユーザー:**
- メールアドレス: `alice@gmail.com`
- パスワード: `password`

または、新規登録から新しいアカウントを作成してください。

## 🛑 アプリケーションを停止する

### 停止手順

```bash
# 1. フロントエンドを停止（開発サーバーのターミナルで Ctrl+C）

# 2. leaf-books-apiを停止（Ctrl+C）

# 3. HSQLDBサーバーを停止（プロジェクトルートで実行）
./gradlew stopHsqldb
```

## 📖 参考リンク

- [React Documentation](https://react.dev/)
- [TypeScript Documentation](https://www.typescriptlang.org/docs/)
- [Vite Documentation](https://vitejs.dev/)
- [leaf-books-api](../leaf-books-api/README.md)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。

