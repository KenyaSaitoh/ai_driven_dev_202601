# berry-books-api - API_002_books 詳細設計書

API ID: API_002_books  
API名: 書籍API  
バージョン: 1.0.0  
最終更新日: 2026-01-18  
ステータス: 詳細設計確定

---

## 1. API概要

本文書は、書籍API（API_002_books）の詳細設計を記述する。

* 実装パターン: 外部API呼び出し（プロキシ転送パターン）
* 外部連携先: back-office-api-sdd（書籍・在庫・カテゴリ管理）
* 実装範囲: JAX-RS Resource のみ（Entity、Dao、Serviceは不要）
* ベースパッケージ: `pro.kensait.berrybooks.api`

* 参照仕様書:
  * [functional_design.md](../../basic_design/functional_design.md) - 機能設計書
  * [architecture_design.md](../../basic_design/architecture_design.md) - アーキテクチャ設計書
  * [external_interface.md](../../basic_design/external_interface.md) - 外部インターフェース仕様書
  * [common/detailed_design.md](../common/detailed_design.md) - 共通機能詳細設計書

---

## 2. パッケージ構造

### 2.1 API固有パッケージ

```
pro.kensait.berrybooks
└── api                          # Presentation Layer (JAX-RS Resources)
    ├── BookResource.java
    └── CategoryResource.java
```

注意: 
* 書籍情報は外部API（back-office-api-sdd）が管理
* Entity、Dao、Serviceは実装しない
* 外部API用DTO（BookTO、CategoryTO等）は共通機能で定義済み
* BackOfficeRestClientは共通機能で実装済み

---

## 3. Resourceクラス設計

### 3.1 BookResource（書籍API Resource）

#### 3.1.1 概要

* 責務: 書籍APIエンドポイントの提供（外部API呼び出し）
* パッケージ: `pro.kensait.berrybooks.api`
* ベースパス: `/api/books`

#### 3.1.2 クラス定義

* クラス名: `BookResource`
* アノテーション:
  * `@Path("/books")` - ベースパス
  * `@ApplicationScoped` - CDI管理Bean（シングルトン）
  * `@Produces(MediaType.APPLICATION_JSON)` - レスポンス形式（クラスレベル）
  * `@Consumes(MediaType.APPLICATION_JSON)` - リクエスト形式（クラスレベル）

#### 3.1.3 依存関係

* `@Inject private BackOfficeRestClient backOfficeClient;` - back-office-api連携クライアント
* `private static final Logger logger = LoggerFactory.getLogger(BookResource.class);`

#### 3.1.4 メソッド設計

##### getAllBooks() - 全書籍取得

* シグネチャ:
```java
@GET
public Response getAllBooks()
```

* 目的: 全書籍を取得（在庫・カテゴリ・出版社情報を含む）
* エンドポイント: `GET /api/books`
* 認証: 不要（公開エンドポイント）

* 処理フロー:
  1. INFO ログ出力: "全書籍取得開始"
  2. BackOfficeRestClientを呼び出し:
    * `backOfficeClient.getAllBooks()` - 書籍一覧取得
  3. INFO ログ出力: "全書籍取得完了: {件数}"
  4. Response返却:
    * ステータス: 200 OK
    * ボディ: List&lt;BookTO&gt;

* レスポンス: `Response.ok(books).build()`
  * HTTPステータス: 200 OK
  * Content-Type: application/json
  * ボディ: 書籍配列（BookTO[]）

* エラーケース:
  * 外部API接続エラー: GenericExceptionMapperで500エラーに変換
  * その他の例外: GenericExceptionMapperで500エラーに変換

##### getBookById(int bookId) - 書籍詳細取得

* シグネチャ:
```java
@GET
@Path("/{id}")
public Response getBookById(@PathParam("id") int bookId)
```

* 目的: 指定された書籍IDの詳細情報を取得
* エンドポイント: `GET /api/books/{id}`
* 認証: 不要（公開エンドポイント）

* パラメータ:
  * `@PathParam("id") int bookId` - 書籍ID（パスパラメータ）

* 処理フロー:
  1. INFO ログ出力: "書籍詳細取得開始: bookId={}"
  2. BackOfficeRestClientを呼び出し:
    * `backOfficeClient.getBookById(bookId)` - 書籍詳細取得
  3. nullチェック:
    * 書籍が見つからない場合（null）:
      * WARN ログ出力: "書籍が見つかりません: bookId={}"
      * ErrorResponse構築（404 Not Found）
      * Response返却（404）
  4. INFO ログ出力: "書籍詳細取得完了: bookId={}"
  5. Response返却:
    * ステータス: 200 OK
    * ボディ: BookTO

* レスポンス:
  * 成功時: `Response.ok(book).build()`
    * HTTPステータス: 200 OK
    * ボディ: BookTO
  * 失敗時: `Response.status(404).entity(errorResponse).build()`
    * HTTPステータス: 404 Not Found
    * ボディ: ErrorResponse

* エラーケース:
  * 書籍が見つからない: 404 Not Found
  * 外部API接続エラー: GenericExceptionMapperで500エラーに変換

##### searchBooksJpql(Integer categoryId, String keyword) - 書籍検索（JPQL版）

* シグネチャ:
```java
@GET
@Path("/search/jpql")
public Response searchBooksJpql(
    @QueryParam("categoryId") Integer categoryId,
    @QueryParam("keyword") String keyword
)
```

* 目的: カテゴリIDまたはキーワードで書籍を検索（JPQL使用）
* エンドポイント: `GET /api/books/search/jpql?categoryId={id}&keyword={keyword}`
* 認証: 不要（公開エンドポイント）

* パラメータ:
  * `@QueryParam("categoryId") Integer categoryId` - カテゴリID（クエリパラメータ、オプション）
  * `@QueryParam("keyword") String keyword` - キーワード（クエリパラメータ、オプション）

* 処理フロー:
  1. INFO ログ出力: "書籍検索（JPQL）開始: categoryId={}, keyword={}"
  2. BackOfficeRestClientを呼び出し:
    * `backOfficeClient.searchBooksJpql(categoryId, keyword)` - 書籍検索
  3. INFO ログ出力: "書籍検索（JPQL）完了: {件数}"
  4. Response返却:
    * ステータス: 200 OK
    * ボディ: List&lt;BookTO&gt;

* レスポンス: `Response.ok(books).build()`
  * HTTPステータス: 200 OK
  * ボディ: 書籍配列（BookTO[]）

* 検索条件:
  * categoryId と keyword の両方がnull/空: 全書籍を返却
  * categoryId のみ指定: カテゴリIDで絞り込み
  * keyword のみ指定: 書籍名または著者名で部分一致検索
  * categoryId と keyword の両方指定: AND条件で検索

* エラーケース:
  * 外部API接続エラー: GenericExceptionMapperで500エラーに変換

##### searchBooksCriteria(Integer categoryId, String keyword) - 書籍検索（Criteria API版）

* シグネチャ:
```java
@GET
@Path("/search/criteria")
public Response searchBooksCriteria(
    @QueryParam("categoryId") Integer categoryId,
    @QueryParam("keyword") String keyword
)
```

* 目的: カテゴリIDまたはキーワードで書籍を検索（Criteria API使用）
* エンドポイント: `GET /api/books/search/criteria?categoryId={id}&keyword={keyword}`
* 認証: 不要（公開エンドポイント）

* パラメータ:
  * `@QueryParam("categoryId") Integer categoryId` - カテゴリID（クエリパラメータ、オプション）
  * `@QueryParam("keyword") String keyword` - キーワード（クエリパラメータ、オプション）

* 処理フロー:
  1. INFO ログ出力: "書籍検索（Criteria API）開始: categoryId={}, keyword={}"
  2. BackOfficeRestClientを呼び出し:
    * `backOfficeClient.searchBooksCriteria(categoryId, keyword)` - 書籍検索
  3. INFO ログ出力: "書籍検索（Criteria API）完了: {件数}"
  4. Response返却:
    * ステータス: 200 OK
    * ボディ: List&lt;BookTO&gt;

* レスポンス: `Response.ok(books).build()`
  * HTTPステータス: 200 OK
  * ボディ: 書籍配列（BookTO[]）

* 検索条件: searchBooksJpql() と同様

* エラーケース:
  * 外部API接続エラー: GenericExceptionMapperで500エラーに変換

---

### 3.2 CategoryResource（カテゴリAPI Resource）

#### 3.2.1 概要

* 責務: カテゴリAPIエンドポイントの提供（外部API呼び出し）
* パッケージ: `pro.kensait.berrybooks.api`
* ベースパス: `/api/categories`

#### 3.2.2 クラス定義

* クラス名: `CategoryResource`
* アノテーション:
  * `@Path("/categories")` - ベースパス（注意: 複数形）
  * `@ApplicationScoped` - CDI管理Bean（シングルトン）
  * `@Produces(MediaType.APPLICATION_JSON)` - レスポンス形式（クラスレベル）
  * `@Consumes(MediaType.APPLICATION_JSON)` - リクエスト形式（クラスレベル）

#### 3.2.3 依存関係

* `@Inject private BackOfficeRestClient backOfficeClient;` - back-office-api連携クライアント
* `private static final Logger logger = LoggerFactory.getLogger(CategoryResource.class);`

#### 3.2.4 メソッド設計

##### getAllCategories() - カテゴリ一覧取得

* シグネチャ:
```java
@GET
public Response getAllCategories()
```

* 目的: カテゴリ一覧をマップ形式で取得
* エンドポイント: `GET /api/categories`
* 認証: 不要（公開エンドポイント）

* 処理フロー:
  1. INFO ログ出力: "カテゴリ一覧取得開始"
  2. BackOfficeRestClientを呼び出し:
    * `backOfficeClient.getAllCategories()` - カテゴリ一覧取得
  3. INFO ログ出力: "カテゴリ一覧取得完了: {件数}"
  4. Response返却:
    * ステータス: 200 OK
    * ボディ: Map&lt;String, Integer&gt;

* レスポンス: `Response.ok(categories).build()`
  * HTTPステータス: 200 OK
  * Content-Type: application/json
  * ボディ: カテゴリマップ（例: `{"Java": 1, "JavaScript": 2, ...}`）

* エラーケース:
  * 外部API接続エラー: GenericExceptionMapperで500エラーに変換

---

## 4. 外部API連携

### 4.1 BackOfficeRestClient（共通機能で実装済み）

#### 4.1.1 概要

* 実装状況: 共通機能で実装済み（`common/detailed_design.md`参照）
* パッケージ: `pro.kensait.berrybooks.external`
* 連携先: back-office-api-sdd

#### 4.1.2 使用するメソッド

| メソッド | 目的 | 呼び出し元 |
|---------|------|-----------|
| `getAllBooks()` | 全書籍取得 | BookResource.getAllBooks() |
| `getBookById(Integer bookId)` | 書籍詳細取得 | BookResource.getBookById() |
| `searchBooksJpql(Integer categoryId, String keyword)` | 書籍検索（JPQL） | BookResource.searchBooksJpql() |
| `searchBooksCriteria(Integer categoryId, String keyword)` | 書籍検索（Criteria API） | BookResource.searchBooksCriteria() |
| `getAllCategories()` | カテゴリ一覧取得 | CategoryResource.getAllCategories() |

#### 4.1.3 外部API用DTO（共通機能で定義済み）

* `BookTO`: 書籍情報（bookId, bookName, author, category, publisher, price, quantity, version）
* `CategoryTO`: カテゴリ情報（categoryId, categoryName）
* `PublisherTO`: 出版社情報（publisherId, publisherName）

詳細: [common/detailed_design.md](../common/detailed_design.md) の「6. 外部API用DTO」を参照

---

## 5. エラーハンドリング

### 5.1 エラーシナリオ

| エラーケース | HTTPステータス | 処理 |
|------------|--------------|------|
| 書籍が見つからない（getBookById） | 404 Not Found | ErrorResponse構築、WARNログ出力 |
| 外部API接続エラー | 500 Internal Server Error | GenericExceptionMapperで処理 |
| その他の予期しない例外 | 500 Internal Server Error | GenericExceptionMapperで処理 |

### 5.2 ErrorResponseフォーマット

404 Not Foundの例:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "書籍が見つかりません",
  "path": "/api/books/999"
}
```

注意: GenericExceptionMapperは共通機能で実装済み（`common/detailed_design.md`参照）

---

## 6. ログ出力

### 6.1 ログレベルと出力タイミング

| ログレベル | 出力タイミング | 例 |
|----------|--------------|-----|
| INFO | メソッド開始 | "全書籍取得開始" |
| INFO | メソッド完了 | "全書籍取得完了: 10件" |
| WARN | 書籍が見つからない | "書籍が見つかりません: bookId=999" |
| ERROR | 例外発生 | GenericExceptionMapperで出力 |

### 6.2 ログ出力例

```java
logger.info("全書籍取得開始");
logger.info("全書籍取得完了: {}件", books.size());
logger.warn("書籍が見つかりません: bookId={}", bookId);
```

---

## 7. 設定情報

### 7.1 外部API設定

* ファイル: `src/main/resources/META-INF/microprofile-config.properties`

```properties
# back-office-api ベースURL
back-office-api.base-url=http://localhost:8080/back-office-api-sdd/api
```

* 設定読み込み: BackOfficeRestClient（共通機能）が@PostConstructで読み込み
* デフォルト値: `http://localhost:8080/back-office-api-sdd/api`

---

## 8. テスト要件

### 8.1 ユニットテスト

単体テストの詳細は [behaviors.md](behaviors.md) を参照してください。

#### 8.1.1 テスト対象

* `BookResource`
  * getAllBooks()
  * getBookById(int bookId)
  * searchBooksJpql(Integer categoryId, String keyword)
  * searchBooksCriteria(Integer categoryId, String keyword)

* `CategoryResource`
  * getAllCategories()

#### 8.1.2 テスト方針

* BackOfficeRestClientをモック化（@Mock）
* 正常系・異常系の両方をテスト
* Given-When-Then形式でテストシナリオを記述

---

## 9. 実装チェックリスト

実装時に確認すべき項目:

* [ ] BookResourceのクラス定義（@Path, @ApplicationScoped）
* [ ] CategoryResourceのクラス定義（@Path, @ApplicationScoped）
* [ ] BackOfficeRestClientの依存性注入（@Inject）
* [ ] 全書籍取得メソッド（getAllBooks）
* [ ] 書籍詳細取得メソッド（getBookById）
* [ ] 書籍検索メソッド（JPQL版、Criteria API版）
* [ ] カテゴリ一覧取得メソッド（getAllCategories）
* [ ] 404 Not Foundエラーハンドリング
* [ ] ログ出力（INFO、WARN）
* [ ] 単体テスト（behaviors.md参照）

---

## 10. 参考資料

### 10.1 システムレベルドキュメント

* [functional_design.md](../../basic_design/functional_design.md) - 機能設計書
* [architecture_design.md](../../basic_design/architecture_design.md) - アーキテクチャ設計書
* [external_interface.md](../../basic_design/external_interface.md) - 外部インターフェース仕様書
* [behaviors.md](../../basic_design/behaviors.md) - システム振る舞い仕様書（E2Eテスト用）

### 10.2 詳細設計ドキュメント

* [common/detailed_design.md](../common/detailed_design.md) - 共通機能詳細設計書
* [behaviors.md](behaviors.md) - 単体テスト用振る舞い仕様書

### 10.3 タスク情報

* [tasks/API_002_books.md](../../../tasks/API_002_books.md) - タスクファイル

### 10.4 プロジェクト情報

* [README.md](../../../README.md) - プロジェクトREADME

### 10.5 Agent Skills

* [architecture.md](../../../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
* [common_rules.md](../../../../../agent_skills/jakarta-ee-api-base/principles/common_rules.md) - 共通ルール
