# berry-books-api - 外部インターフェース仕様書（共通）

プロジェクトID: berry-books-api  
バージョン: 2.0.0  
最終更新日: 2026-01-12  
ステータス: 実装完了

---

## 1. 概要

本ドキュメントは、berry-books-apiが外部システムを呼び出す際のインターフェース仕様を記述する（共通SPEC）。

berry-books-apiは、フロントエンド（berry-books-spa）と複数のバックエンドサービス（customer-hub-api、back-office-api）を仲介するバックエンドサービスである。

---

## 2. 外部システム一覧

| システムID | システム名 | 連携方式 | 目的 | 必須/任意 |
|-----------|----------|---------|------|----------|
| EXT-001 | customer-hub-api | REST API (HTTP/JSON) | 顧客情報管理（CRUD） | 必須 |
| EXT-002 | back-office-api | REST API (HTTP/JSON) | 書籍・在庫・カテゴリ管理 | 必須 |

---

## 3. customer-hub-api連携

* ベースURL: `http://localhost:8080/customer-hub-api/customers`
* 目的: 顧客情報の取得、認証、登録
* 主要エンドポイント: GET /customers/query_email?email=, GET /customers/{customerId}, POST /customers/
* 実装クラス: CustomerHubRestClient
* 詳細: ユースケース auth の userstory.md / behaviors.md を参照

---

## 4. back-office-api連携

* ベースURL: `http://localhost:8080/back-office-api/api`
* 目的: 書籍・在庫・カテゴリ・出版社情報の管理
* 主要エンドポイント: GET /books, GET /books/{bookId}, GET /books/search/jpql, GET /categories, GET /stocks/{bookId}, PUT /stocks/{bookId}
* 実装クラス: BackOfficeRestClient
* 詳細: ユースケース books, orders の userstory.md / behaviors.md を参照。OpenAPI仕様は [common/openapi/](openapi/) に格納。

---

## 5. エラーハンドリング・設定

* HTTPステータス: 200, 404, 409, 500 に応じた処理。ProcessingException 時は 503。
* 設定: META-INF/microprofile-config.properties に customer-hub-api.base-url, back-office-api.base-url

---

## 6. 参考資料（アジャイル構成）

* [architecture_design.md](architecture_design.md) - アーキテクチャ設計書
* [data_model.md](data_model.md) - データモデル仕様書
* usecases/{名}/userstory.md - 各ユースケースのユーザーストーリー
