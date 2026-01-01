# 外部インターフェース設計書

## 1. 概要

本ドキュメントは、Books Stock API - バックオフィス書籍在庫管理システムの外部インターフェースを定義する。

## 2. インターフェース概要

### 2.1 システム境界

```
┌─────────────────────────────────────────────────────┐
│          Client Applications                        │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐   │
│  │  Web       │  │  Mobile    │  │  Admin     │   │
│  │  Browser   │  │  App       │  │  Tool      │   │
│  └────────────┘  └────────────┘  └────────────┘   │
└──────────┬─────────────┬──────────────┬───────────┘
           │             │              │
           │   HTTP/HTTPS (JSON)        │
           │             │              │
┌──────────▼─────────────▼──────────────▼───────────┐
│      Books Stock API (Back Office API)            │
│                                                    │
│  REST API Endpoints (Base: /api)                  │
│  - /auth      : 認証API                           │
│  - /books     : 書籍API                           │
│  - /categories: カテゴリAPI                        │
│  - /publishers: 出版社API                         │
│  - /stocks    : 在庫API                           │
│  - /workflows : ワークフローAPI                    │
└────────────────────────────────────────────────────┘
```

### 2.2 通信プロトコル
- **プロトコル**: HTTP/HTTPS
- **データ形式**: JSON
- **文字エンコーディング**: UTF-8
- **認証方式**: JWT (JSON Web Token) + HttpOnly Cookie

## 3. REST API仕様

### 3.1 共通仕様

#### 3.1.1 ベースURL
```
開発環境: http://localhost:8080/api
本番環境: https://api.example.com/api
```

#### 3.1.2 HTTPメソッド

| メソッド | 用途 | 冪等性 |
|---------|------|--------|
| GET | リソース取得 | Yes |
| POST | リソース作成、操作実行 | No |
| PUT | リソース更新 | Yes |
| DELETE | リソース削除 | Yes |

#### 3.1.3 HTTPステータスコード

| コード | 意味 | 用途 |
|-------|------|------|
| 200 | OK | 成功（GET, PUT, DELETE） |
| 201 | Created | 作成成功（POST） |
| 400 | Bad Request | リクエストが不正 |
| 401 | Unauthorized | 認証失敗 |
| 403 | Forbidden | 権限不足 |
| 404 | Not Found | リソースが見つからない |
| 409 | Conflict | 楽観的ロック失敗 |
| 500 | Internal Server Error | サーバー内部エラー |
| 501 | Not Implemented | 未実装機能 |

#### 3.1.4 共通リクエストヘッダー

| ヘッダー | 必須 | 説明 |
|---------|------|------|
| Content-Type | Yes (POST/PUT) | `application/json` |
| Accept | No | `application/json` |
| Cookie | Yes (認証必要なAPI) | JWT含むCookie |

#### 3.1.5 共通レスポンスヘッダー

| ヘッダー | 説明 |
|---------|------|
| Content-Type | `application/json; charset=UTF-8` |
| Set-Cookie | JWT Cookie（ログイン時） |

#### 3.1.6 エラーレスポンス形式

```json
{
  "error": "エラータイプ",
  "message": "エラーメッセージ（日本語）"
}
```

例:
```json
{
  "error": "Unauthorized",
  "message": "社員コードまたはパスワードが正しくありません"
}
```

### 3.2 認証API（/api/auth）

#### 3.2.1 ログイン

**エンドポイント**: `POST /api/auth/login`

**リクエスト**:
```json
{
  "employeeCode": "E0001",
  "password": "password123"
}
```

**レスポンス（成功）**: `200 OK`
```json
{
  "employeeId": 1,
  "employeeCode": "E0001",
  "employeeName": "山田太郎",
  "email": "yamada@example.com",
  "jobRank": 2,
  "departmentId": 1,
  "departmentName": "営業部"
}
```

**Set-Cookie**:
```
back-office-jwt=<JWT_TOKEN>; Path=/; Max-Age=86400; HttpOnly
```

**レスポンス（失敗）**: `401 Unauthorized`
```json
{
  "error": "Unauthorized",
  "message": "社員コードまたはパスワードが正しくありません"
}
```

#### 3.2.2 ログアウト

**エンドポイント**: `POST /api/auth/logout`

**リクエスト**: なし

**レスポンス**: `200 OK`（レスポンスボディなし）

**Set-Cookie**:
```
back-office-jwt=; Path=/; Max-Age=0; HttpOnly
```

#### 3.2.3 現在のユーザー情報取得

**エンドポイント**: `GET /api/auth/me`

**リクエスト**: なし（Cookieから認証情報を取得）

**レスポンス**: `501 Not Implemented`（未実装）
```json
{
  "error": "Not Implemented",
  "message": "この機能は未実装です"
}
```

### 3.3 書籍API（/api/books）

#### 3.3.1 書籍一覧取得

**エンドポイント**: `GET /api/books`

**リクエスト**: なし

**レスポンス**: `200 OK`
```json
[
  {
    "bookId": 1,
    "bookName": "サンプル書籍",
    "author": "著者名",
    "price": 2500,
    "imageUrl": "http://example.com/image.jpg",
    "quantity": 10,
    "version": 1,
    "category": {
      "categoryId": 1,
      "categoryName": "文学"
    },
    "publisher": {
      "publisherId": 1,
      "publisherName": "出版社A"
    }
  }
]
```

#### 3.3.2 書籍詳細取得

**エンドポイント**: `GET /api/books/{id}`

**パスパラメータ**:
- `id`: 書籍ID（Integer）

**レスポンス（成功）**: `200 OK`
```json
{
  "bookId": 1,
  "bookName": "サンプル書籍",
  "author": "著者名",
  "price": 2500,
  "imageUrl": "http://example.com/image.jpg",
  "quantity": 10,
  "version": 1,
  "category": {
    "categoryId": 1,
    "categoryName": "文学"
  },
  "publisher": {
    "publisherId": 1,
    "publisherName": "出版社A"
  }
}
```

**レスポンス（失敗）**: `404 Not Found`
```json
{
  "error": "書籍が見つかりません"
}
```

#### 3.3.3 書籍検索（JPQL）

**エンドポイント**: `GET /api/books/search/jpql`

**クエリパラメータ**:
- `categoryId`: カテゴリID（オプション、Integer）
- `keyword`: 検索キーワード（オプション、String）

**例**:
- `/api/books/search/jpql?categoryId=1&keyword=サンプル`
- `/api/books/search/jpql?keyword=サンプル`
- `/api/books/search/jpql?categoryId=1`

**レスポンス**: `200 OK`（書籍一覧と同じ形式）

#### 3.3.4 書籍検索（Criteria API）

**エンドポイント**: `GET /api/books/search/criteria`

**クエリパラメータ**:
- `categoryId`: カテゴリID（オプション、Integer）
- `keyword`: 検索キーワード（オプション、String）

**レスポンス**: `200 OK`（書籍一覧と同じ形式）

#### 3.3.5 書籍検索（デフォルト）

**エンドポイント**: `GET /api/books/search`

**クエリパラメータ**:
- `categoryId`: カテゴリID（オプション、Integer）
- `keyword`: 検索キーワード（オプション、String）

**備考**: 内部的にJPQLを使用（後方互換性のため）

**レスポンス**: `200 OK`（書籍一覧と同じ形式）

#### 3.3.6 カテゴリ一覧取得（書籍API経由）

**エンドポイント**: `GET /api/books/categories`

**レスポンス**: `200 OK`
```json
{
  "文学": 1,
  "ビジネス": 2,
  "技術": 3
}
```

### 3.4 カテゴリAPI（/api/categories）

#### 3.4.1 カテゴリ一覧取得

**エンドポイント**: `GET /api/categories`

**レスポンス**: `200 OK`
```json
[
  {
    "categoryId": 1,
    "categoryName": "文学"
  },
  {
    "categoryId": 2,
    "categoryName": "ビジネス"
  }
]
```

### 3.5 出版社API（/api/publishers）

#### 3.5.1 出版社一覧取得

**エンドポイント**: `GET /api/publishers`

**レスポンス**: `200 OK`
```json
[
  {
    "publisherId": 1,
    "publisherName": "出版社A"
  },
  {
    "publisherId": 2,
    "publisherName": "出版社B"
  }
]
```

### 3.6 在庫API（/api/stocks）

#### 3.6.1 在庫一覧取得

**エンドポイント**: `GET /api/stocks`

**レスポンス**: `200 OK`
```json
[
  {
    "bookId": 1,
    "quantity": 10,
    "version": 1
  },
  {
    "bookId": 2,
    "quantity": 5,
    "version": 2
  }
]
```

#### 3.6.2 在庫情報取得

**エンドポイント**: `GET /api/stocks/{bookId}`

**パスパラメータ**:
- `bookId`: 書籍ID（Integer）

**レスポンス（成功）**: `200 OK`
```json
{
  "bookId": 1,
  "quantity": 10,
  "version": 1
}
```

**レスポンス（失敗）**: `404 Not Found`
```json
{
  "error": "在庫情報が見つかりません"
}
```

#### 3.6.3 在庫更新

**エンドポイント**: `PUT /api/stocks/{bookId}`

**パスパラメータ**:
- `bookId`: 書籍ID（Integer）

**リクエスト**:
```json
{
  "version": 1,
  "quantity": 15
}
```

**レスポンス（成功）**: `200 OK`
```json
{
  "bookId": 1,
  "quantity": 15,
  "version": 2
}
```

**レスポンス（楽観的ロック失敗）**: `409 Conflict`
```json
{
  "error": "在庫が他のユーザーによって更新されました。再度お試しください。"
}
```

### 3.7 ワークフローAPI（/api/workflows）

#### 3.7.1 ワークフロー作成

**エンドポイント**: `POST /api/workflows`

**リクエスト（新規書籍追加）**:
```json
{
  "workflowType": "ADD_NEW_BOOK",
  "createdBy": 1,
  "bookName": "新しい書籍",
  "author": "著者名",
  "price": 3000,
  "imageUrl": "http://example.com/new.jpg",
  "categoryId": 1,
  "publisherId": 2,
  "applyReason": "新商品として追加"
}
```

**リクエスト（既存書籍削除）**:
```json
{
  "workflowType": "REMOVE_BOOK",
  "createdBy": 1,
  "bookId": 123,
  "applyReason": "販売終了のため削除"
}
```

**リクエスト（価格改定）**:
```json
{
  "workflowType": "ADJUST_BOOK_PRICE",
  "createdBy": 1,
  "bookId": 123,
  "price": 2000,
  "startDate": "2025-01-01",
  "endDate": "2025-03-31",
  "applyReason": "キャンペーン価格"
}
```

**レスポンス**: `201 Created`
```json
{
  "operationId": 1,
  "workflowId": 1,
  "workflowType": "ADD_NEW_BOOK",
  "state": "CREATED",
  "bookName": "新しい書籍",
  "author": "著者名",
  "price": 3000,
  "categoryId": 1,
  "categoryName": "文学",
  "publisherId": 2,
  "publisherName": "出版社B",
  "applyReason": "新商品として追加",
  "operationType": "CREATE",
  "operatedBy": 1,
  "operatorName": "山田太郎",
  "operatorCode": "E0001",
  "jobRank": 2,
  "departmentId": 1,
  "departmentName": "営業部",
  "operatedAt": "2025-01-01T10:00:00"
}
```

#### 3.7.2 ワークフロー更新（一時保存）

**エンドポイント**: `PUT /api/workflows/{workflowId}`

**パスパラメータ**:
- `workflowId`: ワークフローID（Long）

**リクエスト**:
```json
{
  "updatedBy": 1,
  "bookName": "更新された書籍名",
  "price": 3500,
  "applyReason": "更新された理由"
}
```

**レスポンス**: `200 OK`（ワークフローTO）

#### 3.7.3 ワークフロー一覧取得

**エンドポイント**: `GET /api/workflows`

**クエリパラメータ**:
- `state`: 状態（オプション、String: CREATED, APPLIED, APPROVED）
- `workflowType`: ワークフロータイプ（オプション、String）
- `employeeId`: 社員ID（オプション、Long）

**例**:
- `/api/workflows?state=APPLIED&employeeId=1`
- `/api/workflows?workflowType=ADD_NEW_BOOK`

**レスポンス**: `200 OK`
```json
[
  {
    "operationId": 5,
    "workflowId": 2,
    "workflowType": "REMOVE_BOOK",
    "state": "APPLIED",
    "bookId": 123,
    "bookName": "削除対象書籍",
    "applyReason": "販売終了",
    "operationType": "APPLY",
    "operatedBy": 1,
    "operatorName": "山田太郎",
    "operatedAt": "2025-01-02T14:30:00"
  }
]
```

#### 3.7.4 ワークフロー履歴取得

**エンドポイント**: `GET /api/workflows/{workflowId}/history`

**パスパラメータ**:
- `workflowId`: ワークフローID（Long）

**レスポンス**: `200 OK`
```json
[
  {
    "operationId": 1,
    "workflowId": 1,
    "workflowType": "ADD_NEW_BOOK",
    "state": "CREATED",
    "operationType": "CREATE",
    "operatedBy": 1,
    "operatorName": "山田太郎",
    "operatedAt": "2025-01-01T10:00:00"
  },
  {
    "operationId": 2,
    "workflowId": 1,
    "workflowType": "ADD_NEW_BOOK",
    "state": "APPLIED",
    "operationType": "APPLY",
    "operatedBy": 1,
    "operatorName": "山田太郎",
    "operatedAt": "2025-01-01T11:00:00"
  },
  {
    "operationId": 3,
    "workflowId": 1,
    "workflowType": "ADD_NEW_BOOK",
    "state": "APPROVED",
    "operationType": "APPROVE",
    "operatedBy": 2,
    "operatorName": "佐藤花子",
    "operatedAt": "2025-01-01T15:00:00"
  }
]
```

#### 3.7.5 ワークフロー申請

**エンドポイント**: `POST /api/workflows/{workflowId}/apply`

**パスパラメータ**:
- `workflowId`: ワークフローID（Long）

**リクエスト**:
```json
{
  "operatedBy": 1,
  "operationReason": "申請します"
}
```

**レスポンス**: `200 OK`（ワークフローTO）

#### 3.7.6 ワークフロー承認

**エンドポイント**: `POST /api/workflows/{workflowId}/approve`

**パスパラメータ**:
- `workflowId`: ワークフローID（Long）

**リクエスト**:
```json
{
  "operatedBy": 2,
  "operationReason": "承認します"
}
```

**レスポンス（成功）**: `200 OK`（ワークフローTO）

**レスポンス（権限不足）**: `403 Forbidden`
```json
{
  "error": "UnauthorizedApprovalException",
  "message": "承認権限がありません。MANAGER以上の職務ランクが必要です。"
}
```

#### 3.7.7 ワークフロー却下

**エンドポイント**: `POST /api/workflows/{workflowId}/reject`

**パスパラメータ**:
- `workflowId`: ワークフローID（Long）

**リクエスト**:
```json
{
  "operatedBy": 2,
  "operationReason": "内容に不備があるため差し戻します"
}
```

**レスポンス**: `200 OK`（ワークフローTO、stateはCREATEDに戻る）

## 4. データ型定義

### 4.1 共通データ型

| 型名 | JSON型 | 説明 | 例 |
|------|--------|------|---|
| Integer | number | 整数 | 123 |
| Long | number | 長整数 | 9876543210 |
| BigDecimal | number | 金額・価格 | 2500 |
| String | string | 文字列 | "サンプル" |
| Boolean | boolean | 真偽値 | true |
| LocalDate | string | 日付（ISO 8601） | "2025-01-01" |
| LocalDateTime | string | 日時（ISO 8601） | "2025-01-01T10:30:00" |

### 4.2 列挙型

#### JobRank（職務ランク）
- `1`: ASSOCIATE（一般社員）
- `2`: MANAGER（マネージャー）
- `3`: DIRECTOR（ディレクター）

#### WorkflowType（ワークフロータイプ）
- `ADD_NEW_BOOK`: 新規書籍追加
- `REMOVE_BOOK`: 既存書籍削除
- `ADJUST_BOOK_PRICE`: 書籍価格改定

#### WorkflowState（ワークフロー状態）
- `CREATED`: 作成済み
- `APPLIED`: 申請中
- `APPROVED`: 承認済み

#### WorkflowOperationType（ワークフロー操作タイプ）
- `CREATE`: 作成
- `APPLY`: 申請
- `APPROVE`: 承認
- `REJECT`: 差戻

## 5. 非機能要件

### 5.1 性能要件
- **レスポンスタイム**: 通常時1秒以内
- **スループット**: 100リクエスト/秒

### 5.2 セキュリティ要件
- **認証**: JWT（有効期限24時間）
- **Cookie**: HttpOnly属性でXSS対策
- **HTTPS**: 本番環境では必須
- **パスワード**: BCryptでハッシュ化

### 5.3 可用性要件
- **稼働率**: 99%以上
- **エラーハンドリング**: 全APIで統一的なエラーレスポンス

## 6. 外部システム連携（将来）

現時点では外部システムとの連携はないが、将来的に以下を想定:
- メール通知サービス（ワークフロー通知）
- 在庫管理システム（実在庫連携）
- 会計システム（売上連携）
