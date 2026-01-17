# Jakarta EE API セキュリティ標準

ファイル名: security.md  
バージョン: 2.0.0  
制定日: 2026-01-17  
最終更新日: 2026-01-17

## 概要

このドキュメントは、Jakarta EE APIプロジェクトで適用すべきセキュリティ実装標準を定義する。

* 対象技術スタック: Jakarta EE 10, JAX-RS 3.1, JWT, BCrypt
* セキュリティ標準: OWASP Top 10対応

---

## 1. 認証

### 1.1 JWT (JSON Web Token) 認証

* 推奨事項:
  * ステートレス認証を実現するためJWTを使用
  * HttpOnly Cookieでトークンを管理（XSS攻撃対策）
  * トークン有効期限を設定（デフォルト: 24時間）
  * HMAC-SHA256アルゴリズムで署名

* JWT構造:
  * Header: `alg=HS256, typ=JWT`
  * Payload: ユーザーID、メールアドレス/社員コード、部署ID（該当する場合）、発行時刻（iat）、有効期限（exp）
  * Signature: HMACSHA256（秘密鍵で署名）

* 実装例（ペイロード）:

```json
{
  "sub": "1234567890",
  "email": "user@example.com",
  "iat": 1516239022,
  "exp": 1516325422
}
```

### 1.2 Cookie設定

| 属性 | 値 | 目的 |
|-----|---|------|
| HttpOnly | true | XSS攻撃対策（JavaScriptからアクセス不可） |
| Secure | true（本番環境）<br/>false（開発環境） | HTTPS通信のみでCookie送信 |
| Path | / | アプリケーション全体で有効 |
| MaxAge | 86400秒（24時間） | トークン有効期限と一致 |
| SameSite | Strict / Lax | CSRF攻撃対策 |

* Cookie命名規則: `<project-name>-jwt`
  * 例: `berry-books-jwt`, `back-office-jwt`

### 1.3 JWT秘密鍵のセキュリティ要件

* 秘密鍵要件:
  * 最小長: 32文字以上
  * ランダム生成された文字列を使用
  * 環境変数で管理（本番環境では必須）
  * ソースコードにハードコードしない
  * デフォルト値は開発環境のみ使用

### 1.4 トークンライフサイクル

* ステートレス認証: サーバーサイドセッションは使用しない
* トークン有効期限: 24時間（MicroProfile Configで設定可能）
* 有効期限切れトークンは自動的に無効化
* ログイン成功時に新しいJWTを発行（トークン固定攻撃対策）

### 1.5 CSRF（Cross-Site Request Forgery）対策

* SameSite Cookie属性を使用（Strict / Lax）
* 状態変更操作（POST, PUT, DELETE）は認証必須
* JWTをHttpOnly Cookieで管理することでXSS経由のCSRFを防止

---

## 2. パスワード管理

### 2.1 パスワードハッシュ化

* BCryptアルゴリズムを使用
* ソルトは自動生成
* ラウンド数: デフォルト（10）

* 実装例:

```java
// パスワードハッシュ化
String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

// パスワード検証
boolean isValid = BCrypt.checkpw(plainPassword, hashedPassword);
```

### 2.2 パスワード要件（推奨）

* 最小長: 8文字以上
* 最大長: 100文字
* 複雑性要件（該当する場合）:
  * 大文字、小文字、数字、特殊文字を含む

---

## 3. データ保護

### 3.1 個人情報保護

* 個人情報の取り扱い:
  * 必要最小限の情報のみ取得
  * ログに個人情報を出力しない
  * データベースの暗号化（該当する場合）

### 3.2 機密情報の取り扱い

* JWT秘密鍵、データベース接続情報は環境変数で管理
* ソースコードにハードコードしない
* `.gitignore`に機密情報ファイルを追加

### 3.3 データ暗号化

* 通信の暗号化: HTTPS/TLS（本番環境では必須）
* データベースの暗号化: 機密情報は暗号化して保存（該当する場合）

---

## 4. 通信セキュリティ

### 4.1 HTTPS/TLS

* 本番環境ではHTTPS必須
* TLS 1.2以上を使用
* Secure Cookie属性を有効化

### 4.2 証明書管理

* 信頼できる認証局（CA）の証明書を使用
* 証明書の有効期限を定期的に確認
* 自己署名証明書は開発環境のみ使用

### 4.3 セキュアな通信プロトコル

* HTTP: 開発環境のみ
* HTTPS: 本番環境必須
* 外部API連携もHTTPS推奨

---

## 5. セキュアコーディング

### 5.1 コーディング規約

* OWASP Secure Coding Practicesに従う
* 入力値は常にバリデーション
* 出力値は適切にエスケープ
* 例外処理で機密情報を漏らさない

### 5.2 SQLインジェクション対策

* JPQLまたはCriteria APIを使用
* ネイティブクエリは使用しない（やむを得ない場合はパラメーターバインディングを使用）
* 動的クエリ生成時は文字列連結を避ける

* 実装例:

```java
// JPQL with parameter binding
String jpql = "SELECT b FROM Book b WHERE b.name LIKE :name";
TypedQuery<Book> query = em.createQuery(jpql, Book.class);
query.setParameter("name", "%" + searchTerm + "%");
```

### 5.3 クロスサイトスクリプティング（XSS）対策

* HTTPOnly Cookieでトークン管理
* Content-Type: application/jsonを明示
* レスポンスのHTMLエスケープ（該当する場合）

### 5.4 コマンドインジェクション対策

* 外部コマンド実行は原則禁止
* やむを得ない場合は入力値を厳格に検証

---

## 6. 脆弱性対策

### 6.1 OWASP Top 10対応

| 脆弱性 | 対策 |
|-------|------|
| A01:2021 - Broken Access Control | JWT認証、認可チェック |
| A02:2021 - Cryptographic Failures | HTTPS、BCryptハッシュ化 |
| A03:2021 - Injection | JPQL、Bean Validation |
| A04:2021 - Insecure Design | セキュアな設計パターン |
| A05:2021 - Security Misconfiguration | 適切な設定管理 |
| A06:2021 - Vulnerable Components | 依存関係の管理 |
| A07:2021 - Authentication Failures | JWT認証、BCrypt |
| A08:2021 - Software and Data Integrity Failures | コード署名、検証 |
| A09:2021 - Logging Failures | 適切なログ出力 |
| A10:2021 - Server-Side Request Forgery | 外部API検証 |

---

## 7. 監査ログ

### 7.1 監査ログ要件

* 記録すべき操作:
  * ユーザーログイン/ログアウト
  * データの作成・更新・削除
  * 権限変更
  * 重要な設定変更

* 監査ログの要件:
  * 誰が（ユーザーID）
  * いつ（タイムスタンプ）
  * 何を（操作内容）
  * どうした（結果）

---

## 8. 参考資料

### 8.1 公式ドキュメント

* Jakarta Security: https://jakarta.ee/specifications/security/3.0/
* OWASP Top 10: https://owasp.org/www-project-top-ten/
* OWASP Cheat Sheet Series: https://cheatsheetseries.owasp.org/

### 8.2 関連標準

* [architecture.md](architecture.md) - Jakarta EE APIアーキテクチャ標準
* [common_rules.md](common_rules.md) - 共通ルール

### 8.3 ライブラリ

* jjwt: https://github.com/jwtk/jjwt
* BCrypt: https://github.com/jeremyh/jBCrypt
