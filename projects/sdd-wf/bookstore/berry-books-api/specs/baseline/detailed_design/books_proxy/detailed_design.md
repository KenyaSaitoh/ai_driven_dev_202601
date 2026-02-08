# books_proxy - ドメイン詳細設計書

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
* 責務: back-office-apiから書籍情報を取得し、フロントエンドに提供（プロキシ転送パターン）
* 依存関係: commonに依存（BackOfficeRestClientを使用）

---

## 2. クラス構成

### 2.1 パッケージ構造

```
pro.kensait.berrybooks
└── api/                    # JAX-RS Resources
    ├── BookResource.java
    ├── CategoryResource.java
    └── dto/
        └── ErrorResponse.java（共通）
```

**重要な設計判断**:
* Service層なし: 外部API呼び出しのみのため、ビジネスロジック層は不要
* DAO層なし: 書籍データの永続化はback-office-apiが管理
* Entity層なし: 書籍エンティティはback-office-apiが管理
* 外部API連携: commonドメインのBackOfficeRestClientを使用

---

## 3. コンポーネント設計

### 3.1 Resource - BookResource

**責務**: 書籍API（back-office-apiへのプロキシ転送）

**アノテーション**:
* `@Path("/books")`
* `@ApplicationScoped`
* `@Produces(MediaType.APPLICATION_JSON)`

**依存関係**:
* `@Inject BackOfficeRestClient backOfficeClient` - back-office-api連携クライアント

**主要メソッド**:

#### getAllBooks

```java
@GET
public Response getAllBooks()
```

* **目的**: 全書籍の一覧取得（在庫・カテゴリ・出版社情報含む）
* **認証**: 不要
* **処理**: `backOfficeClient.findAllBooks()`を呼び出し、結果をそのまま返却
* **戻り値**: 200 OK + List<BookTO>
* **エラー**: 500 Internal Server Error（外部API呼び出し失敗時）

#### getBookById

```java
@GET
@Path("/{bookId}")
public Response getBookById(@PathParam("bookId") Integer bookId)
```

* **目的**: 書籍IDで書籍詳細を取得
* **認証**: 不要
* **処理**: `backOfficeClient.findBookById(bookId)`を呼び出し
* **戻り値**: 200 OK + BookTO
* **エラー**: 404 Not Found（書籍が存在しない）、500 Internal Server Error

#### searchBooksJpql

```java
@GET
@Path("/search/jpql")
public Response searchBooksJpql(
    @QueryParam("categoryId") Integer categoryId,
    @QueryParam("keyword") String keyword)
```

* **目的**: カテゴリIDまたはキーワードで書籍検索（JPQL版）
* **認証**: 不要
* **処理**: `backOfficeClient.searchBooksJpql(categoryId, keyword)`を呼び出し
* **戻り値**: 200 OK + List<BookTO>
* **エラー**: 500 Internal Server Error

#### searchBooksCriteria

```java
@GET
@Path("/search/criteria")
public Response searchBooksCriteria(
    @QueryParam("categoryId") Integer categoryId,
    @QueryParam("keyword") String keyword)
```

* **目的**: カテゴリIDまたはキーワードで書籍検索（Criteria API版）
* **認証**: 不要
* **処理**: `backOfficeClient.searchBooksCriteria(categoryId, keyword)`を呼び出し
* **戻り値**: 200 OK + List<BookTO>
* **エラー**: 500 Internal Server Error

---

### 3.2 Resource - CategoryResource

**責務**: カテゴリAPI（back-office-apiへのプロキシ転送）

**アノテーション**:
* `@Path("/categories")`
* `@ApplicationScoped`
* `@Produces(MediaType.APPLICATION_JSON)`

**依存関係**:
* `@Inject BackOfficeRestClient backOfficeClient` - back-office-api連携クライアント

**主要メソッド**:

#### getAllCategories

```java
@GET
public Response getAllCategories()
```

* **目的**: カテゴリ一覧をマップ形式で取得
* **認証**: 不要
* **処理**: `backOfficeClient.findAllCategories()`を呼び出し
* **戻り値**: 200 OK + Map<String, Integer>（キー: カテゴリ名、値: カテゴリID）
* **エラー**: 500 Internal Server Error

---

## 4. DTO設計

### 4.1 BookTO（外部API用DTO）

**パッケージ**: `pro.kensait.berrybooks.external.dto`（commonドメインで定義済み）

**目的**: 書籍情報の転送オブジェクト

**フィールド**:
* `Integer bookId` - 書籍ID
* `String bookName` - 書籍名
* `String author` - 著者
* `Integer categoryId` - カテゴリID
* `Integer publisherId` - 出版社ID
* `String publisherName` - 出版社名
* `Integer price` - 価格
* `Integer quantity` - 在庫数
* `Long version` - バージョン番号（楽観的ロック用）

---

### 4.2 CategoryTO（外部API用DTO）

**パッケージ**: `pro.kensait.berrybooks.external.dto`（commonドメインで定義済み）

**目的**: カテゴリ情報の転送オブジェクト

**フィールド**:
* `Integer categoryId` - カテゴリID
* `String categoryName` - カテゴリ名

---

## 5. 外部API連携

### 5.1 BackOfficeRestClient使用

**実装方式**: commonドメインのBackOfficeRestClientを使用

**呼び出しエンドポイント**:
* `GET /books` - 書籍一覧取得
* `GET /books/{bookId}` - 書籍詳細取得
* `GET /books/search/jpql?categoryId={}&keyword={}` - 書籍検索（JPQL）
* `GET /books/search/criteria?categoryId={}&keyword={}` - 書籍検索（Criteria API）
* `GET /categories` - カテゴリ一覧取得

**エラーハンドリング**:
* 外部API呼び出し失敗時: RuntimeExceptionをスロー
* ExceptionMapperで統一的なエラーレスポンス（500 Internal Server Error）

---

## 6. セキュリティ設計

### 6.1 認証設定

**認証除外パス**: `/api/books`, `/api/categories`（すべてのエンドポイント）

**理由**: 書籍情報とカテゴリ情報は公開情報のため、認証不要

**実装**: JwtAuthenFilterで上記パスを認証除外リストに追加済み

---

## 7. エラーハンドリング

### 7.1 例外マッピング

| 例外 | HTTPステータス | 説明 |
|-----|--------------|------|
| WebApplicationException（404） | 404 Not Found | 書籍が存在しない |
| ProcessingException | 503 Service Unavailable | 外部API接続エラー |
| その他RuntimeException | 500 Internal Server Error | システムエラー |

**実装**: ExceptionMapperで統一的なエラーレスポンス（ErrorResponse DTO）を返却

---

## 8. 参考資料

* [behaviors.md](behaviors.md) - 単体テスト用振る舞い仕様書
* [../../basic_design/books_proxy/functional_design.md](../../basic_design/books_proxy/functional_design.md) - 書籍API連携機能設計書
* [../../basic_design/books_proxy/behaviors.md](../../basic_design/books_proxy/behaviors.md) - 結合テスト用振る舞い仕様書
* [../../basic_design/common/external_interface.md](../../basic_design/common/external_interface.md) - 外部インターフェース仕様書
* [../../basic_design/common/architecture_design.md](../../basic_design/common/architecture_design.md) - アーキテクチャ設計書