# 共通ドメイン - 機能設計書

プロジェクトID: berry-books-api  
ドメイン: common（共通ドメイン、最優先実装）  
バージョン: 1.0.0  
最終更新日: 2026-02-05

---

## 1. 概要

本ドキュメントは、共通ドメインの機能を定義する。共通ドメインは、他のすべてのドメインから依存される基盤となる機能を提供する。

* エンティティ（Order, OrderItem, Customer等）
* Dao（OrderDao, OrderItemDao等）
* 認証機能（JWT生成・検証）
* 共通Service（該当する場合）

---

## 2. 認証機能

### 2.1 認証方式

* 認証方式: JWT（JSON Web Token）
* トークン管理: HttpOnly Cookie
* トークン有効期限: 24時間
* アルゴリズム: HMAC-SHA256

### 2.2 認証フロー

1. ログイン時にJWT生成
2. HttpOnly CookieでJWTを管理
3. 認証必須APIは、JWTフィルターで検証
4. 認証除外エンドポイント:
   * `/api/auth/login`
   * `/api/auth/logout`
   * `/api/auth/register`
   * `/api/books/*`
   * `/api/images/*`

### 2.3 外部連携による顧客認証

* 顧客情報は、customer-hub-api経由で取得
* パスワード照合は本システムで実施

---

## 3. エンティティ一覧

以下のエンティティはcommonドメインで定義され、他のドメインから参照される。

* Order - 注文トランザクション
* OrderItem - 注文明細
* Customer - 顧客（外部API経由で取得、永続化なし）

詳細なテーブル定義は `data_model.md` を参照。

---

## 4. Dao一覧

以下のDaoクラスはcommonドメインで実装される。

* OrderDao - 注文データアクセス
* OrderItemDao - 注文明細データアクセス

---

## 5. 共通ユーティリティ

* JwtUtil - JWT生成・検証
* PasswordUtil - パスワードハッシュ化・検証
* JwtAuthenFilter - JWT認証フィルター

---

## 6. ログ処理

* ログレベル: ERROR, WARN, INFO, DEBUG
* ログ出力方針: SLF4J + Log4j2
* 重要なイベント: ログイン成功/失敗、注文作成、外部API呼び出し

---

## 7. エラーハンドリング

* エラーレスポンス形式: ErrorResponse（status, error, message, path）
* 例外マッピング: Exception Mapperで統一的なエラーレスポンスを返却

---

## 8. 参考資料

* [architecture_design.md](architecture_design.md) - アーキテクチャ設計書
* [data_model.md](data_model.md) - データモデル仕様書
* [external_interface.md](external_interface.md) - 外部インターフェース仕様書
* [behaviors.md](behaviors.md) - 共通機能の振る舞い仕様書（結合テスト用）
