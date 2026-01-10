# API_001_auth - 認証APIタスク

**担当者:** 1名  
**推奨スキル:** JAX-RS、JWT、BCrypt、セキュリティ  
**想定工数:** 8時間  
**依存タスク:** [common_tasks.md](common_tasks.md)

---

## 概要

このタスクリストは、認証API（ログイン、ログアウト）の実装タスクを含みます。JWT認証とHttpOnly Cookieによるトークン管理を実装します。

---

## タスクリスト

### T_API001_001: LoginRequestの作成

- **目的**: ログインリクエスト用のDTOを実装する
- **対象**: `pro.kensait.backoffice.api.dto.LoginRequest`（Record）
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「2.1 POST /api/auth/login」
- **注意事項**: 
  - Java Record形式で実装
  - フィールド: `employeeCode`（String）、`password`（String）
  - Bean Validation: `@NotBlank`、`@Size`

---

### T_API001_002: LoginResponseの作成

- **目的**: ログインレスポンス用のDTOを実装する
- **対象**: `pro.kensait.backoffice.api.dto.LoginResponse`（Record）
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「2.1 POST /api/auth/login」
- **注意事項**: 
  - Java Record形式で実装
  - フィールド: `employeeId`（Long）、`employeeCode`（String）、`employeeName`（String）、`departmentId`（Long）、`departmentName`（String）、`jobRank`（Integer）
  - パスワード情報は含めない

---

### T_API001_003: AuthenResourceの作成

- **目的**: 認証APIのエンドポイントを実装する
- **対象**: `pro.kensait.backoffice.api.AuthenResource`
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「2. エンドポイント仕様」
  - [behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) の「2. 認証API」
- **注意事項**: 
  - `@Path("/auth")`、`@ApplicationScoped`
  - `@POST @Path("/login")`: ログイン
    - リクエスト: LoginRequest
    - レスポンス: LoginResponse + Set-Cookie（JWT）
    - HTTPステータス: 200 OK（成功）、401 Unauthorized（失敗）
  - `@POST @Path("/logout")`: ログアウト
    - レスポンス: 空 + Set-Cookie（削除用、MaxAge=0）
    - HTTPステータス: 200 OK
  - `@GET @Path("/me")`: 現在のユーザー情報取得（未実装、将来拡張用）
  - EmployeeDaoを`@Inject`
  - JwtUtilを`@Inject`
  - BCrypt.checkpw()でパスワード照合
  - HttpOnly Cookie設定:
    - Name: `back-office-jwt`
    - HttpOnly: true
    - Secure: false（開発環境）
    - MaxAge: 86400秒（24時間）
  - ログ出力: INFO（成功）、WARN（失敗）

---

### T_API001_004: 認証APIの単体テスト

- **目的**: AuthenResourceのテストケースを実装する
- **対象**: `pro.kensait.backoffice.api.AuthenResourceTest`
- **参照SPEC**: 
  - [behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) の「2. 認証API」
- **注意事項**: 
  - JUnit 5 + Mockito使用
  - EmployeeDao、JwtUtilをモック化
  - テストケース:
    - `testLogin_Success()`: 正常系（社員コード・パスワードが正しい）
    - `testLogin_InvalidEmployeeCode()`: 社員コードが存在しない（401）
    - `testLogin_InvalidPassword()`: パスワードが一致しない（401）
    - `testLogout()`: ログアウト成功

---

## 完了確認

### チェックリスト

- [X] LoginRequest
- [X] LoginResponse
- [X] AuthenResource（ログイン、ログアウト）
- [X] 認証APIの単体テスト

### 動作確認

以下のcurlコマンドで動作確認:

#### ログイン（成功）
```bash
curl -X POST http://localhost:8080/back-office-api-sdd/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "employeeCode": "E0001",
    "password": "password123"
  }' \
  -c cookies.txt
```

#### ログイン（失敗 - 社員コードが存在しない）
```bash
curl -X POST http://localhost:8080/back-office-api-sdd/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "employeeCode": "INVALID",
    "password": "password123"
  }'
```

#### ログアウト
```bash
curl -X POST http://localhost:8080/back-office-api-sdd/api/auth/logout \
  -b cookies.txt
```

---

## 次のステップ

このAPI実装完了後、以下のタスクに並行して進めます:

- [API_002_books.md](API_002_books.md) - 書籍API
- [API_003_categories.md](API_003_categories.md) - カテゴリAPI
- [API_004_publishers.md](API_004_publishers.md) - 出版社API
- [API_005_stocks.md](API_005_stocks.md) - 在庫API
- [API_006_workflows.md](API_006_workflows.md) - ワークフローAPI

---

**タスクファイル作成日:** 2025-01-10  
**想定実行順序:** 3番目（共通機能実装後）
