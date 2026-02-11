# customer-hub-spa プロジェクト

## 📖 概要

Berry Books オンライン書店の顧客管理SPA（React + TypeScript）です。
顧客一覧を表示し、注文件数と購入冊数の統計情報を確認できます。

## 🚀 セットアップとコマンド実行ガイド

### 前提条件

- **Node.js 18以上**
- **npm または yarn**
- **バックエンドAPI** (`customer-hub-api`) が起動していること

> **Note:** バックエンドAPIの起動方法は、各プロジェクトのルートREADME.mdを参照してください。

### セットアップ手順

```bash
# 1. プロジェクトのディレクトリに移動
cd projects/master/bookstore/customer-hub-spa

# 2. 依存関係をインストール（初回のみ）
npm install

# 3. 開発サーバーを起動（Vite）
npm run dev
```

> **Note**: Windowsでは**Git Bash**を使用してください。

開発サーバーは http://localhost:3000 で起動します。

> **Note**: このプロジェクトはViteを使用しています。高速なHMR（Hot Module Replacement）による開発体験を提供します。

インストール後、VSCodeを再読み込みすることをお勧めします：
- `Ctrl+Shift+P` → "Reload Window" を実行

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

- **開発環境**: http://localhost:3000

## 🎯 プロジェクト構成

```
projects/master/bookstore/customer-hub-spa/
├── src/
│   ├── components/
│   │   └── CustomerList.tsx    # 顧客一覧コンポーネント
│   ├── styles/
│   │   └── App.css             # アプリケーションスタイル
│   ├── types.ts                # TypeScript型定義
│   ├── App.tsx                 # メインアプリコンポーネント
│   └── main.tsx                # エントリーポイント
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── README.md
```

## 🔧 使用している技術

- **React 18**
- **TypeScript 5**
- **Vite 5** - ビルドツール
- **CSS3** - Berry Booksテーマ

## 🎨 デザイン仕様

- **レスポンシブデザイン**: モダンなグラデーションとシャドウ効果

## 🎯 主な機能

### 1. 顧客一覧表示
- 全顧客の情報をテーブル表示
- 顧客ID、顧客名、メールアドレス、生年月日、住所を表示

### 2. 顧客情報編集
- 各顧客行の「編集」ボタンをクリックして編集ダイアログを表示
- 顧客名、メールアドレス、生年月日、住所を編集可能
- フォームバリデーション機能搭載
- 編集後、REST APIに更新データを送信

### 3. 統計情報表示
- 各顧客の注文件数
- 各顧客の購入冊数（合計）

### 4. リアルタイムデータ取得
- REST APIからデータを取得
- ローディング状態とエラーハンドリング

## 🌐 API仕様

このフロントエンドは以下のAPIを使用します：

### 1. 顧客一覧取得
- **エンドポイント**: `GET /api/customers/`
- **レスポンス**: `CustomerStatsTO[]`

### 2. 顧客情報更新
- **エンドポイント**: `PUT /api/customers/{customerId}`
- **リクエストボディ**: 
```json
{
  "customerName": "山田太郎",
  "email": "yamada@example.com",
  "birthday": "1990-01-01",
  "address": "東京都渋谷区"
}
```
- **レスポンス**: 成功時は200 OK

### データモデル (CustomerStatsTO)

```json
{
  "customerId": 1,
  "customerName": "山田太郎",
  "email": "yamada@example.com",
  "birthday": "1990-01-01",
  "address": "東京都渋谷区",
  "orderCount": 5,
  "totalBooks": 12
}
```

## 🔄 プロキシ設定

開発環境では、Viteのプロキシ機能を使用してAPIリクエストを転送します。

```typescript
// vite.config.ts
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    }
  }
}
```

## 📚 関連ドキュメント

- [Bookstore プロジェクト全体のREADME](../README.md) - システム全体の起動・停止手順
- [Customer Hub API README](../customer-hub-api/README.md) - バックエンドAPIの詳細

## 📖 参考リンク

- [React Documentation](https://react.dev/)
- [TypeScript Documentation](https://www.typescriptlang.org/docs/)
- [Vite Documentation](https://vitejs.dev/)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。

