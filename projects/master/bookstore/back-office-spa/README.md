# Back Office SPA - ワークフロー管理システム

## 概要

書籍管理システムの社内バックオフィス用フロントエンドアプリケーション。
書籍の新規作成・削除・価格調整のワークフロー管理機能を提供します。

**技術スタック:**
- React 18
- TypeScript
- Vite
- React Router
- Axios
- Tailwind CSS

**デザイン:**
- **テーマカラー**: Berry Books ストロベリーレッド (#CF3F4E)
- **スタイリング**: Tailwind CSS
- **レスポンシブデザイン**: モダンなグラデーションとシャドウ効果

## 主な機能

### 認証・認可
- 社員コード + パスワードによるログイン認証
- JWT（Cookie）による認証状態管理
- ロールベースアクセス制御（RBAC）

### ワークフロー管理
- **ワークフロー作成**: 新規書籍作成、書籍削除、価格調整のワークフローを作成
- **ワークフロー一覧**: 状態・種別でフィルタリング可能な一覧表示
- **ワークフロー詳細**: 詳細情報と操作履歴の表示
- **申請**: 作成済みワークフローを上司に申請
- **承認/差戻**: マネージャー以上が申請を承認または差戻

### 権限管理
- **CREATED状態**: 作成者本人のみ参照可能
- **APPLIED状態**: 作成者と同じ部署の直属の上司（マネージャー以上）のみ参照・操作可能
- **APPROVED状態**: 全員参照可能

## ディレクトリ構成

```
src/
├── components/       # 共通コンポーネント
│   ├── Header.tsx
│   ├── Layout.tsx
│   └── ProtectedRoute.tsx
├── contexts/         # コンテキスト（状態管理）
│   └── AuthContext.tsx
├── pages/            # ページコンポーネント
│   ├── LoginPage.tsx
│   ├── WorkflowListPage.tsx
│   ├── WorkflowDetailPage.tsx
│   └── CreateWorkflowPage.tsx
├── services/         # API通信層
│   ├── api.ts
│   ├── authService.ts
│   └── workflowService.ts
├── types/            # TypeScript型定義
│   └── index.ts
├── App.tsx           # ルーティング設定
├── main.tsx          # エントリーポイント
└── index.css         # Tailwind CSSスタイル
```

## セットアップ

### 前提条件

- Node.js 16+ / npm 8+
- バックエンドAPI (`back-office-api`) が起動している必要があります

### 依存関係のインストール

```bash
cd projects/master/bookstore/back-office-spa
npm install
```

### 開発サーバーの起動

```bash
npm run dev
```

アプリケーションは http://localhost:3001 で起動します。

### ビルド

```bash
npm run build
```

ビルド成果物は `dist/` ディレクトリに出力されます。

### プレビュー

```bash
npm run preview
```

## API通信

### プロキシ設定

開発環境では、Viteのプロキシ機能を使用してバックエンドAPIと通信します。

`vite.config.ts`:
```typescript
export default defineConfig({
  server: {
    port: 3001,
    proxy: {
      '/api': {
        target: 'http://localhost:8080/back-office-api',
        changeOrigin: true,
      }
    }
  }
})
```

### バックエンドAPI

- **ベースURL**: `http://localhost:8080/back-office-api`
- **認証**: Cookie（JWT）による認証
- **認証エンドポイント**:
  - `POST /api/auth/login` - ログイン
  - `POST /api/auth/logout` - ログアウト
  - `GET /api/auth/me` - 現在のユーザー情報取得（未実装）
- **ワークフローエンドポイント**:
  - `GET /api/workflows` - ワークフロー一覧取得
  - `GET /api/workflows/{workflowId}` - ワークフロー履歴取得
  - `POST /api/workflows` - ワークフロー作成
  - `POST /api/workflows/{workflowId}/apply` - 申請
  - `POST /api/workflows/{workflowId}/approve` - 承認
  - `POST /api/workflows/{workflowId}/reject` - 差戻
- **マスターデータエンドポイント**:
  - `GET /api/categories` - カテゴリ一覧
  - `GET /api/publishers` - 出版社一覧（要実装）
  - `GET /api/books` - 書籍一覧

## ワークフローの状態と操作

### 状態（State）

| 状態 | 説明 | 表示名 |
|------|------|--------|
| CREATED | 作成済み（申請前） | 作成済 |
| APPLIED | 申請中（承認待ち） | 申請中 |
| APPROVED | 承認済み | 承認済 |

### 操作（Operation Type）

| 操作 | 説明 | 実行者 |
|------|------|--------|
| CREATE | ワークフロー作成 | 作成者 |
| APPLY | 申請 | 作成者 |
| APPROVE | 承認 | マネージャー以上（同部署） |
| REJECT | 差戻 | マネージャー以上（同部署） |

### ワークフロータイプ

| タイプ | 説明 | 必要な情報 |
|--------|------|-----------|
| ADD_NEW_BOOK | 新規書籍の追加 | 書籍名、著者、価格、カテゴリ、出版社、画像URL（任意） |
| REMOVE_BOOK | 既存書籍の削除 | 書籍ID |
| ADJUST_BOOK_PRICE | 書籍価格の改定 | 書籍ID、改定後価格、開始日、終了日 |

## テストアカウント

バックエンドのテストデータに応じて、以下のようなアカウントが利用可能です：

| 社員コード | パスワード | 役職 | 部署 |
|-----------|----------|------|------|
| EMP001 | password | ASSOCIATE | 営業部 |
| EMP002 | password | MANAGER | 営業部 |
| EMP003 | password | DIRECTOR | 営業部 |

## 開発ガイド

### コンポーネント作成ガイドライン

1. **関数コンポーネント**: React.FCを使用
2. **TypeScript**: 型安全性を確保
3. **状態管理**: useStateとuseContextを活用
4. **API通信**: servicesディレクトリの関数を使用
5. **エラーハンドリング**: try-catchでエラーをキャッチし、ユーザーに適切なメッセージを表示

### スタイリングガイドライン

1. **Tailwind CSS**: Utility-firstアプローチでスタイリング
2. **テーマカラー**: Berry Books統一のストロベリーレッド
3. **レスポンシブデザイン**: Tailwindのレスポンシブユーティリティを活用
4. **共通スタイル**: `index.css`で定義されたカスタムクラスを使用（`btn-primary`, `form-input`など）

## トラブルシューティング

### 認証エラー（401）

- バックエンドAPIが起動しているか確認
- 社員コードとパスワードが正しいか確認
- Cookieが正しく設定されているか確認（開発者ツール）

### プロキシエラー

- `vite.config.ts`のプロキシ設定を確認
- バックエンドAPIのベースURLが正しいか確認

### ビルドエラー

- `node_modules/`を削除して`npm install`を再実行
- TypeScriptの型エラーを修正

## 今後の改善点

- [ ] `/api/auth/me`エンドポイントの実装と統合
- [ ] `/api/publishers`エンドポイントの実装
- [ ] ワークフロー一覧でのページネーション
- [ ] より詳細な権限チェック（部署IDベース）
- [ ] ワークフロー検索機能
- [ ] ワークフロー統計・レポート機能
- [ ] 通知機能（申請・承認時）
- [ ] ユニットテスト・E2Eテストの追加
- [x] Tailwind CSSへの移行
- [x] Berry Booksテーマカラーの適用
- [x] カテゴリAPIのArray形式対応

## ライセンス

このプロジェクトは学習目的で作成されています。
