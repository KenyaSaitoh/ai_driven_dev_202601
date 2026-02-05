# 共通ドメイン - 機能設計書

プロジェクトID: back-office-api  
ドメイン: common（共通ドメイン、最優先実装）  
バージョン: 1.0.0  
最終更新日: 2026-02-05

---

## 1. 概要

本ドキュメントは、共通ドメインの機能を定義する。共通ドメインは、他のすべてのドメインから依存される基盤となる機能を提供する。

* エンティティ（Book, Category, Publisher, Stock, Employee, Workflow等）
* Dao（BookDao, CategoryDao, PublisherDao, StockDao, EmployeeDao, WorkflowDao等）
* 認証機能（JWT生成・検証）
* 共通Service（該当する場合）

---

## 2. 認証機能

### 2.1 F-AUTH-001: ログイン

#### 2.1.1 機能概要

社員コードとパスワードによる認証を行い、成功時はJWTトークンを発行してHttpOnly Cookieに設定する。

#### 2.1.2 エンドポイント

* メソッド: POST
* パス: `/api/auth/login`
* 認証: 不要

#### 2.1.3 入力

* 社員コード（employeeCode）: String
* パスワード（password）: String

#### 2.1.4 処理フロー

1. リクエストから社員コードとパスワードを取得
2. 社員コードで社員情報をデータベースから検索
3. 社員が存在しない場合 → 401 Unauthorized
4. パスワード照合
   * ハッシュ化パスワード：ハッシュアルゴリズムで検証
   * 平文パスワード：文字列比較（開発環境のみ）
5. パスワードが一致しない場合 → 401 Unauthorized
6. JWT生成
   * Payload: employeeId, employeeCode, departmentId
   * 署名: HMAC-SHA256
   * 有効期限: 24時間
7. HttpOnly Cookieを生成
   * Cookie名: アプリケーション固有の名前
   * Value: JWT文字列
   * HttpOnly: true
   * Secure: 本番環境ではtrue
   * MaxAge: 24時間（秒単位）
8. レスポンス生成（社員情報 + Set-Cookie）

#### 2.1.5 出力

* 成功（200 OK）: LoginResponse + Set-Cookie
* 失敗（401 Unauthorized）: ErrorResponse
* エラー（500 Internal Server Error）: ErrorResponse

#### 2.1.6 関連コンポーネント

* 認証リソース（ログイン処理）
* EmployeeDao（社員コード検索）
* JwtUtil（トークン生成）
* パスワードハッシュ検証

#### 2.1.7 バリデーションルール

* `employeeCode`: 必須、20文字以内
* `password`: 必須、100文字以内

### 2.2 F-AUTH-002: ログアウト

#### 2.2.1 機能概要

現在のJWTトークンを無効化する（Cookieを削除）。

#### 2.2.2 エンドポイント

* メソッド: POST
* パス: `/api/auth/logout`
* 認証: 不要

#### 2.2.3 入力

なし

#### 2.2.4 処理フロー

1. HttpOnly Cookieを削除（MaxAge=0）
2. レスポンス生成（Set-Cookie）

#### 2.2.5 出力

* 成功（200 OK）: 空のレスポンス + Set-Cookie（削除用）

### 2.3 F-AUTH-003: ユーザー情報取得

#### 2.3.1 機能概要

現在ログイン中のユーザー情報を取得する（未実装）。

#### 2.3.2 エンドポイント

* メソッド: GET
* パス: `/api/auth/me`
* 認証: 必要（JWT）

---

## 3. ビジネスルール

### 3.1 認証・認可ルール

#### BR-AUTH-001: パスワード照合

* ハッシュ化パスワード：ハッシュアルゴリズムで検証
* 平文パスワード：文字列比較（開発環境のみ、本番環境では非推奨）

#### BR-AUTH-002: JWT有効期限

* デフォルト：24時間
* 設定により変更可能

---

## 4. セキュリティ考慮事項

### 4.1 認証トークン

* JWTはHttpOnly Cookieで保持
* JavaScriptからアクセス不可（XSS対策）

### 4.2 パスワード保護

* BCryptでハッシュ化（不可逆変換）
* ソルト自動生成

---

## 5. エンティティ一覧

以下のエンティティはcommonドメインで定義され、他のドメインから参照される。

* Book - 書籍
* Category - カテゴリ
* Publisher - 出版社
* Stock - 在庫（@SecondaryTable）
* Employee - 社員
* Department - 部署
* Workflow - ワークフロー
* WorkflowHistory - ワークフロー履歴

詳細なテーブル定義は `data_model.md` を参照。

---

## 6. Dao一覧

以下のDaoクラスはcommonドメインで実装される。

* BookDao - 書籍データアクセス
* CategoryDao - カテゴリデータアクセス
* PublisherDao - 出版社データアクセス
* StockDao - 在庫データアクセス
* EmployeeDao - 社員データアクセス
* WorkflowDao - ワークフローデータアクセス
* WorkflowHistoryDao - ワークフロー履歴データアクセス

---

## 7. 共通ユーティリティ

* JwtUtil - JWT生成・検証
* PasswordUtil - パスワードハッシュ化・検証
* JwtAuthenFilter - JWT認証フィルター

---

## 8. トランザクション管理

### 8.1 トランザクション境界

* サービスレイヤーでトランザクション境界を定義
* デフォルト：既存トランザクションがあれば参加、なければ新規作成
* 例外発生時は自動ロールバック

---

## 9. 参考資料

* [architecture_design.md](architecture_design.md) - アーキテクチャ設計書
* [data_model.md](data_model.md) - データモデル仕様書
* [external_interface.md](external_interface.md) - 外部インターフェース仕様書
* [behaviors.md](behaviors.md) - 共通機能の振る舞い仕様書（結合テスト用）
