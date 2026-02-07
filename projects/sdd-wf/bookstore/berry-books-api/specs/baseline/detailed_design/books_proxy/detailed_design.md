# books_proxy - 書籍プロキシドメイン詳細設計書

ドメイン名: books_proxy  
バージョン: 1.0.0  
最終更新: 2026-02-07

---

## 重要な原則

この詳細設計書は、**基本設計（what/why）と実装コード（how）の橋渡しとなる設計判断のみを簡潔に記載**します。

**記載すべき情報**:
* クラス名と責務（1行）
* 主要メソッドのシグネチャ（引数、戻り値、例外）
* 設計判断を示すアノテーション（@Transactional, @Path等）
* JPQLクエリ（WHERE句、JOIN等の設計判断）
* 依存関係（@Inject対象）

**記載すべきでない情報**:
* メソッドの実装詳細、処理ステップ
* すべてのフィールド定義、getter/setter
* バリデーションの詳細
* 基本設計SPECの内容の繰り返し

---

## 1. ドメイン概要

* ドメイン名: books_proxy
* 責務: back-office-apiから書籍情報を取得し、フロントエンドに提供するプロキシ層
* 依存関係: commonドメインに依存（BackOfficeRestClient、外部API用DTO）

---

## 2. クラス構成

### 2.1 パッケージ構造

```
pro.kensait.berrybooks
└── api/                       # JAX-RS Resources
    └── BookResource.java      # 書籍APIエンドポイント（プロキシ）
```

* 注意: books_proxyドメインはプロキシ転送パターンのため、Service層、DAO層、Entity層は実装しない
* 外部API連携はcommonドメインのBackOfficeRestClientを使用

---

## 3. コンポーネント設計

### 3.1 Resource - BookResource

**責務**: 書籍API、カテゴリAPIのエンドポイントを提供し、back-office-apiへリクエストをプロキシ転送

**アノテーション**:
* `@Path("/books")`
* `@ApplicationScoped`

**依存関係**:
* `@Inject BackOfficeRestClient backOfficeRestClient` - 外部API呼び出し

**主要メソッド**:

#### getAllBooks

```java
@GET
@Produces(MediaType.APPLICATION_JSON)
public Response getAllBooks()
```

* **目的**: 全書籍を在庫情報と共に取得
* **外部API**: `GET /books` (back-office-api)
* **認証**: 不要（公開エンドポイント）
* **レスポンス**:
  * 200 OK: `List<BookTO>`
  * 500 Internal Server Error: エラー時

---

#### getBookById

```java
@GET
@Path("/{bookId}")
@Produces(MediaType.APPLICATION_JSON)
public Response getBookById(@PathParam("bookId") Integer bookId)
```

* **目的**: 指定された書籍IDの詳細情報を取得
* **外部API**: `GET /books/{bookId}` (back-office-api)
* **認証**: 不要（公開エンドポイント）
* **レスポンス**:
  * 200 OK: `BookTO`
  * 404 Not Found: 書籍が見つからない
  * 500 Internal Server Error: エラー時

---

#### searchBooksJpql

```java
@GET
@Path("/search/jpql")
@Produces(MediaType.APPLICATION_JSON)
public Response searchBooksJpql(
    @QueryParam("categoryId") Integer categoryId,
    @QueryParam("keyword") String keyword
)
```

* **目的**: カテゴリIDまたはキーワードで書籍を検索（JPQL使用）
* **外部API**: `GET /books/search/jpql?categoryId={categoryId}&keyword={keyword}` (back-office-api)
* **認証**: 不要（公開エンドポイント）
* **レスポンス**:
  * 200 OK: `List<BookTO>`
  * 500 Internal Server Error: エラー時

---

#### searchBooksCriteria

```java
@GET
@Path("/search/criteria")
@Produces(MediaType.APPLICATION_JSON)
public Response searchBooksCriteria(
    @QueryParam("categoryId") Integer categoryId,
    @QueryParam("keyword") String keyword
)
```

* **目的**: カテゴリIDまたはキーワードで書籍を検索（Criteria API使用）
* **外部API**: `GET /books/search/criteria?categoryId={categoryId}&keyword={keyword}` (back-office-api)
* **認証**: 不要（公開エンドポイント）
* **レスポンス**:
  * 200 OK: `List<BookTO>`
  * 500 Internal Server Error: エラー時

---

#### getAllCategories

```java
@GET
@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
public Response getAllCategories()
```

* **目的**: カテゴリ一覧をマップ形式で取得
* **外部API**: `GET /categories` (back-office-api)
* **認証**: 不要（公開エンドポイント）
* **レスポンス**:
  * 200 OK: `Map<String, Integer>` (カテゴリ名 → カテゴリID)
  * 500 Internal Server Error: エラー時

---

## 4. 外部API連携

### 4.1 BackOfficeRestClient（commonドメイン）

books_proxyドメインは、commonドメインで実装済みのBackOfficeRestClientを使用して外部APIを呼び出す。

**使用するメソッド**:
* `getAllBooks()` - 書籍一覧取得
* `getBookById(Integer bookId)` - 書籍詳細取得
* `searchBooksJpql(Integer categoryId, String keyword)` - 書籍検索（JPQL）
* `searchBooksCriteria(Integer categoryId, String keyword)` - 書籍検索（Criteria API）
* `getAllCategories()` - カテゴリ一覧取得

**設定**:
* ベースURL: `back-office-api.base-url`（MicroProfile Config）
* デフォルト値: `http://localhost:8080/back-office-api/api`
* タイムアウト: 接続30秒、読み取り60秒

---

## 5. エラーハンドリング

### 5.1 外部APIエラーの扱い

* **404 Not Found**: 外部APIが返す404をそのまま転送
* **500 Internal Server Error**: 外部APIが返す500をそのまま転送
* **ネットワークエラー、タイムアウト**: 503 Service Unavailableを返却
* **例外マッピング**: commonドメインのGenericExceptionMapperで統一的に処理

### 5.2 ログ出力方針

* **INFO**: API呼び出し開始（メソッド名、パラメータ）
* **WARN**: 外部APIエラー（404, 500等）
* **ERROR**: ネットワークエラー、タイムアウト、予期しない例外

---

## 6. 認証・認可

### 6.1 認証除外エンドポイント

books_proxyドメインの全エンドポイントは認証不要（公開API）:
* `/api/books` - 書籍一覧取得
* `/api/books/{bookId}` - 書籍詳細取得
* `/api/books/search/jpql` - 書籍検索（JPQL）
* `/api/books/search/criteria` - 書籍検索（Criteria API）
* `/api/books/categories` - カテゴリ一覧取得

これらのエンドポイントは、JwtAuthenFilterで認証チェックをスキップする。

---

## 7. パフォーマンス考慮事項

### 7.1 キャッシング

* 現状: キャッシング未実装（常に最新データを外部APIから取得）
* 将来的な実装候補:
  * カテゴリ一覧のキャッシング（TTL: 1時間）
  * 書籍一覧のキャッシング（TTL: 5分）

### 7.2 タイムアウト

* BackOfficeRestClientのデフォルトタイムアウトを使用
* 接続タイムアウト: 30秒
* 読み取りタイムアウト: 60秒

---

## 8. トランザクション設計

* トランザクション境界: なし（プロキシ転送のみ、データベースアクセスなし）

---

## 9. 参考資料

* [behaviors.md](behaviors.md) - 単体テスト用振る舞い仕様書
* [../../basic_design/books_proxy/functional_design.md](../../basic_design/books_proxy/functional_design.md) - 書籍プロキシ機能設計書
* [../../basic_design/books_proxy/behaviors.md](../../basic_design/books_proxy/behaviors.md) - 書籍プロキシ振る舞い仕様書（結合テスト用）
* [../../basic_design/common/external_interface.md](../../basic_design/common/external_interface.md) - 外部インターフェース仕様書
* [../../basic_design/common/architecture_design.md](../../basic_design/common/architecture_design.md) - アーキテクチャ設計書
* [../common/detailed_design.md](../common/detailed_design.md) - 共通ドメイン詳細設計書
