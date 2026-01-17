# API_001_auth - 認証API受入基準

* API ID: API_001_auth  
* API名: 認証API  
* ベースパス: `/api/auth`  
* バージョン: 2.0.0  
* 最終更新日: 2025-01-02

---

## 1. 概要

本文書は、認証APIの受入基準を記述する。各エンドポイントについて、正常系・異常系の振る舞いを定義し、テストシナリオの基礎とする。

---

## 2. ログイン (`POST /api/auth/login`)

### 2.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| AUTH-LOGIN-001 | 正しい社員コードとパスワードでログインできる | 社員が登録されている（E0001, password） | 社員コード・パスワードでログインリクエスト | 200 OK<br/>JWT Cookieが発行される<br/>社員情報が返される |
| AUTH-LOGIN-002 | BCryptハッシュ化されたパスワードでログインできる | パスワードがBCryptハッシュで保存されている | 平文パスワードでログインリクエスト | 200 OK<br/>BCrypt.checkpw()で照合成功<br/>JWT Cookieが発行される |
| AUTH-LOGIN-003 | 平文パスワード（開発環境）でログインできる | パスワードが平文で保存されている | 平文パスワードでログインリクエスト | 200 OK<br/>平文比較で照合成功<br/>JWT Cookieが発行される |
| AUTH-LOGIN-004 | JWTが正しく生成される | ログイン成功 | JWTのペイロードを確認 | employeeId, employeeCode, departmentId, iat, expが含まれる |
| AUTH-LOGIN-005 | JWT Cookieの属性が正しい | ログイン成功 | Set-Cookieヘッダーを確認 | HttpOnly=true<br/>Max-Age=86400<br/>Path=/ |

### 2.2 異常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| AUTH-LOGIN-E001 | 社員コードが存在しない場合、エラー | 社員コードが未登録 | 未登録の社員コードでログインリクエスト | 401 Unauthorized<br/>"社員コードまたはパスワードが正しくありません" |
| AUTH-LOGIN-E002 | パスワードが一致しない場合、エラー | 社員コードは存在するがパスワードが不一致 | 誤ったパスワードでログインリクエスト | 401 Unauthorized<br/>"社員コードまたはパスワードが正しくありません" |
| AUTH-LOGIN-E003 | 社員コードが空の場合、エラー | - | 社員コードが空でログインリクエスト | 400 Bad Request<br/>Bean Validationエラー |
| AUTH-LOGIN-E004 | パスワードが空の場合、エラー | - | パスワードが空でログインリクエスト | 400 Bad Request<br/>Bean Validationエラー |
| AUTH-LOGIN-E005 | 社員コードが20文字を超える場合、エラー | - | employeeCode="E123456789012345678901"でログインリクエスト | 400 Bad Request<br/>Bean Validationエラー |
| AUTH-LOGIN-E006 | パスワードが100文字を超える場合、エラー | - | password=(101文字)でログインリクエスト | 400 Bad Request<br/>Bean Validationエラー |

---

## 3. ログアウト (`POST /api/auth/logout`)

### 3.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| AUTH-LOGOUT-001 | ログアウトできる | ログイン済み | ログアウトリクエスト | 200 OK<br/>JWT Cookie削除（MaxAge=0） |
| AUTH-LOGOUT-002 | 未ログイン状態でもログアウトできる | 未ログイン | ログアウトリクエスト | 200 OK<br/>JWT Cookie削除（MaxAge=0） |

---

## 4. 現在のユーザー情報取得 (`GET /api/auth/me`)

### 4.1 正常系（将来実装予定）

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| AUTH-ME-001 | ログイン中の社員情報を取得できる | JWT Cookieが設定されている | /api/auth/meにリクエスト | 200 OK<br/>社員情報が返される |

### 4.2 異常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| AUTH-ME-E001 | 未実装機能を呼び出した場合、エラー | - | /api/auth/meにリクエスト | 501 Not Implemented<br/>"この機能は未実装です" |

---

## 5. JWT認証（将来実装予定）

### 5.1 JWT検証

#### 5.1.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| JWT-001 | 有効なJWT Cookieでリクエストできる | 有効なJWT Cookie | 認証必須APIにリクエスト | APIが正常に実行される<br/>SecuredResourceにemployeeIdが設定される |
| JWT-002 | JWTの有効期限内はリクエストできる | JWT発行から23時間後 | 認証必須APIにリクエスト | APIが正常に実行される |

#### 5.1.2 異常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| JWT-E001 | JWT Cookieが設定されていない場合、認証エラー | JWT Cookie未設定 | 認証必須APIにリクエスト | 401 Unauthorized |
| JWT-E002 | JWTが改ざんされている場合、認証エラー | JWT Cookieが改ざんされている | 認証必須APIにリクエスト | 401 Unauthorized |
| JWT-E003 | JWTが期限切れの場合、認証エラー | JWT Cookieが期限切れ（24時間経過） | 認証必須APIにリクエスト | 401 Unauthorized |
| JWT-E004 | JWTの署名が無効な場合、認証エラー | JWTの署名が無効 | 認証必須APIにリクエスト | 401 Unauthorized |

---

## 6. セキュリティ受入基準

### 6.1 認証・認可

| シナリオID | 説明 | 受入基準 |
|-----------|------|---------|
| SEC-001 | JWT Cookie は HttpOnly | JavaScriptからアクセス不可 |
| SEC-002 | パスワードはBCryptハッシュ化 | パスワードが平文で保存されていない（本番環境） |
| SEC-003 | JWT有効期限は24時間 | 24時間後にJWTが無効になる |
| SEC-004 | JWT署名アルゴリズムはHMAC-SHA256 | JWT HeaderでalgがHS256 |
| SEC-005 | JWT秘密鍵は32文字以上 | jwt.secret-keyが32文字以上 |
| SEC-006 | 認証失敗時は詳細な理由を返さない | 社員不存在もパスワード不一致も同じエラーメッセージ |

---

## 7. パフォーマンス受入基準

### 7.1 レスポンスタイム

| シナリオID | API | 受入基準 |
|-----------|-----|---------|
| PERF-001 | POST /api/auth/login | 500ms以内（95パーセンタイル） |
| PERF-002 | POST /api/auth/logout | 50ms以内（95パーセンタイル） |
| PERF-003 | GET /api/auth/me | 500ms以内（95パーセンタイル）（将来実装時） |

---

## 8. ログ出力受入基準

### 8.1 ログレベル

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| LOG-001 | ログイン成功時はINFOレベルでログ出力 | - | ログイン成功 | INFO: [AuthenResource#login] employeeCode: E0001<br/>INFO: [AuthenResource#login] Login successful: E0001 |
| LOG-002 | 社員不存在時はWARNレベルでログ出力 | 社員コードが未登録 | ログイン失敗 | WARN: [AuthenResource#login] Employee not found: INVALID |
| LOG-003 | パスワード不一致時はWARNレベルでログ出力 | パスワードが不一致 | ログイン失敗 | WARN: [AuthenResource#login] Password mismatch for employeeCode: E0001 |
| LOG-004 | 予期しないエラー時はERRORレベルでログ出力 | - | 予期しないエラー発生 | ERROR: [AuthenResource#login] Unexpected error |

---

## 9. 並行処理受入基準

### 9.1 同時ログイン

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| CONC-001 | 同一社員が複数回ログインできる | - | 同じ社員コードで2回ログイン | 両方とも成功<br/>異なるJWTが発行される |
| CONC-002 | 複数のJWTが同時に有効 | 同一社員が2つのJWT保持 | 両方のJWTでAPIリクエスト | 両方とも成功（ステートレス設計） |

---

## 10. 関連ドキュメント

* [functional_design.md](functional_design.md) - 認証API機能設計書
* [../../system/behaviors.md](../../system/behaviors.md) - 全体受入基準
* [../../system/architecture_design.md](../../system/architecture_design.md) - アーキテクチャ設計書
