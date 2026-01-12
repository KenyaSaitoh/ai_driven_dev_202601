# berry-books-api - 外部インターフェース仕様書

プロジェクトID: berry-books-api  
バージョン: 2.0.0  
最終更新日: 2026-01-12  
ステータス: 実装完了

---

## 1. 概要

本ドキュメントは、berry-books-api（BFF）が外部システムを呼び出す際のインターフェース仕様を記述する。

berry-books-apiは、Backend for Frontend（BFF）パターンに基づき、フロントエンド（berry-books-spa）と複数のバックエンドサービス（customer-hub-api、back-office-api）を仲介する。

---

## 2. 外部システム一覧

| システムID | システム名 | 連携方式 | 目的 | 必須/任意 |
|-----------|----------|---------|------|----------|
| EXT-001 | customer-hub-api | REST API (HTTP/JSON) | 顧客情報管理（CRUD） | 必須 |
| EXT-002 | back-office-api | REST API (HTTP/JSON) | 書籍・在庫・カテゴリ管理 | 必須 |

---

## 3. customer-hub-api連携

### 3.1 概要

* システム名: customer-hub-api
* 目的: 顧客情報の取得、認証、登録
* 連携方式: REST API (HTTP/JSON)
* プロトコル: HTTP
* 認証方式: なし（同一ネットワーク内の信頼された通信）
* ベースURL: `http://localhost:8080/customer-hub-api/customers`

* 設定方法:
  1. システムプロパティ: `-Dcustomer-hub-api.base-url=...`
  2. 環境変数: `CUSTOMER_HUB_API_BASE_URL=...`
  3. プロパティファイル: `META-INF/microprofile-config.properties`
  4. デフォルト値: `http://localhost:8080/customer-hub-api/customers`

### 3.2 実装クラス

* クラス名: `pro.kensait.berrybooks.external.CustomerHubRestClient`
* スコープ: アプリケーションスコープ（シングルトン）
* 使用ライブラリ: Jakarta RESTful Web Services Client API

---

## 4. customer-hub-api エンドポイント

### 4.1 顧客検索（メールアドレス）

* エンドポイント: `GET /customers/query_email?email={email}`
* 目的: ログイン認証時にメールアドレスで顧客を検索
* 呼び出し元: `AuthenResource.login(LoginRequest)`

* リクエスト:
  * クエリパラメータ: `email` (string, 必須)

* レスポンス:
  * 200 OK: 顧客情報（CustomerTO）
  * 404 Not Found: 顧客が見つからない

* 処理フロー:
  1. メールアドレスで顧客情報を取得
  2. 顧客が存在しない場合は401エラーを返却
  3. 顧客が存在する場合はパスワード照合処理へ進む

---

### 4.2 顧客検索（顧客ID）

* エンドポイント: `GET /customers/{customerId}`
* 目的: JWT Claimsから顧客IDを取得して顧客情報を取得
* 呼び出し元: `AuthenResource.getCurrentUser()`

* リクエスト:
  * パスパラメータ: `customerId` (integer, 必須)

* レスポンス:
  * 200 OK: 顧客情報（CustomerTO）
  * 404 Not Found: 顧客が見つからない

* 処理フロー:
  1. JWT ClaimsからcustomerIdを取得
  2. 顧客IDで顧客情報を取得
  3. 顧客情報を返却

---

### 4.3 顧客登録

* エンドポイント: `POST /customers/`
* 目的: 新規顧客を登録
* 呼び出し元: `AuthenResource.register(RegisterRequest)`

* リクエスト:
  * Content-Type: `application/json`
  * ボディ: CustomerTO（customerId=null, password=BCryptハッシュ）

* レスポンス:
  * 200 OK: 作成された顧客情報（CustomerTO）
  * 409 Conflict: メールアドレス重複

* 処理フロー:
  1. 新しいCustomerオブジェクトを生成
  2. パスワードをBCryptでハッシュ化
  3. customer-hub-apiへ登録リクエスト送信
  4. 作成された顧客情報を返却

---

## 5. CustomerTO スキーマ

* パッケージ: `pro.kensait.berrybooks.external.dto`
* 構造種別: レコード型（immutable）

* フィールド:

| フィールド名 | 型 | 説明 |
|------------|---|------|
| customerId | Integer | 顧客ID |
| customerName | String | 顧客名 |
| password | String | パスワード（ハッシュ化済み） |
| email | String | メールアドレス |
| birthday | LocalDate | 生年月日 |
| address | String | 住所 |

---

## 6. back-office-api連携

### 6.1 概要

* システム名: back-office-api
* 目的: 書籍・在庫・カテゴリ・出版社情報の管理
* 連携方式: REST API (HTTP/JSON)
* プロトコル: HTTP
* 認証方式: なし（同一ネットワーク内の信頼された通信）
* ベースURL: `http://localhost:8080/back-office-api/api`

* 設定方法:
  1. システムプロパティ: `-Dback-office-api.base-url=...`
  2. 環境変数: `BACK_OFFICE_API_BASE_URL=...`
  3. プロパティファイル: `META-INF/microprofile-config.properties`
  4. デフォルト値: `http://localhost:8080/back-office-api/api`

### 6.2 実装クラス

* クラス名: `pro.kensait.berrybooks.external.BackOfficeRestClient`
* スコープ: アプリケーションスコープ（シングルトン）
* 使用ライブラリ: Jakarta RESTful Web Services Client API

---

## 7. back-office-api エンドポイント

### 7.1 書籍一覧取得

* エンドポイント: `GET /books`
* 目的: 全書籍を在庫・カテゴリ・出版社情報と共に取得
* 呼び出し元: `BookResource.getAllBooks()`
* レスポンス: 書籍配列（BookTO[]）

* BookTOの構造:
  * `@SecondaryTable`により、BOOKテーブルとSTOCKテーブルを結合
  * 在庫情報（quantity, version）がフラットな構造で含まれる
  * カテゴリと出版社はネストされたオブジェクト

---

### 7.2 書籍詳細取得

* エンドポイント: `GET /books/{bookId}`
* 目的: 指定された書籍IDの詳細情報を取得
* 呼び出し元: `BookResource.getBookById(int bookId)`

* リクエスト:
  * パスパラメータ: `bookId` (integer, 必須)

* レスポンス:
  * 200 OK: 書籍情報（BookTO）
  * 404 Not Found: 書籍が見つからない

---

### 7.3 書籍検索（JPQL）

* エンドポイント: `GET /books/search/jpql?categoryId={id}&keyword={keyword}`
* 目的: カテゴリIDまたはキーワードで書籍を検索（JPQL使用）
* 呼び出し元: `BookResource.searchBooksJpql(...)`

* リクエスト:
  * クエリパラメータ: `categoryId` (integer, オプション)
  * クエリパラメータ: `keyword` (string, オプション)

* レスポンス: 書籍配列（BookTO[]）

---

### 7.4 書籍検索（Criteria API）

* エンドポイント: `GET /books/search/criteria?categoryId={id}&keyword={keyword}`
* 目的: カテゴリIDまたはキーワードで書籍を検索（Criteria API使用）
* 呼び出し元: `BookResource.searchBooksCriteria(...)`
* リクエスト: 7.3と同じ
* レスポンス: 書籍配列（BookTO[]）

---

### 7.5 カテゴリ一覧取得

* エンドポイント: `GET /categories`
* 目的: カテゴリ一覧をマップ形式で取得
* 呼び出し元: `CategoryResource.getAllCategories()`

* レスポンス: カテゴリマップ（Map<String, Integer>）
  * キー: カテゴリ名
  * 値: カテゴリID

---

### 7.6 在庫取得

* エンドポイント: `GET /stocks/{bookId}`
* 目的: 指定された書籍IDの在庫情報を取得
* 呼び出し元: `OrderService.createOrder(...)`（在庫確認時）

* リクエスト:
  * パスパラメータ: `bookId` (integer, 必須)

* レスポンス:
  * 200 OK: 在庫情報（StockTO）
  * 404 Not Found: 在庫情報が見つからない

* StockTOの構造:
  * bookId, bookName, quantity, version

---

### 7.7 在庫更新（楽観的ロック対応）

* エンドポイント: `PUT /stocks/{bookId}`
* 目的: 指定された書籍IDの在庫数を更新
* 呼び出し元: `OrderService.createOrder(...)`（在庫減算時）

* リクエスト:
  * パスパラメータ: `bookId` (integer, 必須)
  * Content-Type: `application/json`
  * ボディ: `{"quantity": 8, "version": 1}`

* レスポンス:
  * 200 OK: 更新された在庫情報（StockTO）
  * 404 Not Found: 在庫情報が見つからない
  * 409 Conflict: 楽観的ロック失敗

* 重要: フィールド名は `quantity` および `version`（旧バージョンの `newQuantity` は使用不可）

---

## 8. エラーハンドリング

### 8.1 HTTPステータスコード対応

| ステータスコード | 説明 | 対応 |
|---------------|------|------|
| 200 OK | 成功 | レスポンスを返す |
| 404 Not Found | リソースが見つからない | nullまたは例外をスロー |
| 409 Conflict | 楽観的ロック失敗/重複 | 専用例外をスロー |
| 500 Internal Server Error | サーバーエラー | RuntimeExceptionをスロー |

### 8.2 例外処理

* ProcessingException: ネットワークエラー、接続タイムアウト
  * 503 Service Unavailableで応答
  * 適切なエラーメッセージを返却

* WebApplicationException: 4xx/5xxエラー
  * 外部APIのステータスコードに応じた処理
  * 404の場合は適切な例外に変換

---

## 9. 設定管理

### 9.1 設定ファイル

* パス: `src/main/resources/META-INF/microprofile-config.properties`

* 設定項目:
```properties
customer-hub-api.base-url=http://localhost:8080/customer-hub-api/customers
back-office-api.base-url=http://localhost:8080/back-office-api/api
```

### 9.2 設定読み込み優先順位

MicroProfile Config標準に従う：
1. システムプロパティ
2. 環境変数
3. プロパティファイル
4. デフォルト値

* 採用方式: `ConfigProvider.getConfig()`による明示的な読み込み

---

## 10. CDI設定

* beans.xml: `src/main/webapp/WEB-INF/beans.xml`

* 設定内容:
  * `bean-discovery-mode="all"`
  * Jakarta EE 10標準に準拠

* 役割:
  * CDIコンテナの有効化
  * 依存性注入の動作保証
  * MicroProfile Configの正常動作に必要

---

## 11. ログ出力

### 11.1 ログレベル

* レベル: INFO（通常）、WARN（エラー発生時）、ERROR（例外発生時）

### 11.2 ログ内容

* API呼び出し開始（メソッド名、パラメータ）
* リクエストURL
* レスポンスステータス
* エラー詳細（例外発生時）

---

## 12. 本システムが公開するAPI仕様について

本システムが外部に公開するAPI仕様については、各APIディレクトリ配下のOpenAPI (YAML) 仕様書を参照してください：

| API | OpenAPI仕様書 | 説明 |
|-----|-------------|------|
| 認証API | [API_001_auth/openapi.yaml](../api/API_001_auth/openapi.yaml) | ログイン・ログアウト・新規登録 |
| 書籍API | [API_002_books/openapi.yaml](../api/API_002_books/openapi.yaml) | 書籍情報の参照・検索（プロキシ） |
| 注文API | [API_003_orders/openapi.yaml](../api/API_003_orders/openapi.yaml) | 注文作成・履歴取得 |
| 画像API | [API_004_images/openapi.yaml](../api/API_004_images/openapi.yaml) | 書籍表紙画像配信 |

---

## 13. 参考資料

本外部インターフェース仕様書に関連する詳細ドキュメント：

* [requirements.md](requirements.md) - 要件定義書
* [functional_design.md](functional_design.md) - 機能設計書
* [architecture_design.md](architecture_design.md) - アーキテクチャ設計書
* [behaviors.md](behaviors.md) - 振る舞い仕様書（受入基準）
* [data_model.md](data_model.md) - データモデル仕様書
* MicroProfile Config: https://microprofile.io/specifications/microprofile-config/
