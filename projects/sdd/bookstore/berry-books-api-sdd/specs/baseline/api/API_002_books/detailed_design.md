# API_002 書籍API - 詳細設計書（BFFパターン - プロキシ）

* API ID: API_002  
* API名: 書籍API  
* パターン: BFF（Backend for Frontend） - プロキシパターン  
* バージョン: 1.0.0  
* 最終更新: 2025-01-10

---

## 1. パッケージ構造

### 1.1 関連パッケージ

```
pro.kensait.berrybooks
├── api
│   ├── BookResource.java             # 書籍リソース（プロキシ）
│   └── CategoryResource.java         # カテゴリリソース（プロキシ）
└── external
    ├── BackOfficeRestClient.java    # バックオフィスAPI連携
    └── dto
        ├── BookTO.java               # 書籍転送オブジェクト
        └── ErrorResponse.java        # エラーレスポンス
```

* 注: このAPIはプロキシパターンのため、以下のクラスは実装されていません：
* `BookService` - サービス層なし
* `BookDao` - DAO層なし
* `Book` エンティティ - エンティティなし

すべての処理は`BackOfficeRestClient`を通じて`back-office-api`に転送されます。

---

## 2. クラス設計

### 2.1 BookResource（JAX-RS Resource - プロキシ）

* 責務: 書籍APIのプロキシエンドポイント提供

* アノテーション:
  * `@Path("/books")` - ベースパス
  * `@ApplicationScoped` - CDIスコープ

* 主要メソッド:

#### getAllBooks() - 全書籍取得

```
@GET
@Produces(MediaType.APPLICATION_JSON)
```

* 処理フロー:
  1. BackOfficeRestClientで全書籍を取得（外部API呼び出し）
   * エンドポイント: `GET /api/books`
  2. BookTOリストをそのまま返却

* レスポンス: `List<BookTO>`

* プロキシ先: `back-office-api` の `GET /api/books`

#### getBookById() - 書籍詳細取得

```
@GET
@Path("/{id}")
@Produces(MediaType.APPLICATION_JSON)
```

* パラメータ:
  * `@PathParam("id") Integer bookId` - 書籍ID

* 処理フロー:
  1. BackOfficeRestClientで書籍を取得（外部API呼び出し）
   * エンドポイント: `GET /api/books/{id}`
  2. BookTOをそのまま返却

* レスポンス: `BookTO`

* プロキシ先: `back-office-api` の `GET /api/books/{id}`

#### searchBooks() - 書籍検索

```
@GET
@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
```

* クエリパラメータ:
  * `@QueryParam("keyword") String keyword` - キーワード（オプション）
  * `@QueryParam("categoryId") Integer categoryId` - カテゴリID（オプション）

* 処理フロー:
  1. BackOfficeRestClientで書籍検索を実行（外部API呼び出し）
   * エンドポイント: `GET /api/books/search/jpql?keyword={keyword}&categoryId={categoryId}`
  2. BookTOリストをそのまま返却

* レスポンス: `List<BookTO>`

* プロキシ先: `back-office-api` の `GET /api/books/search/jpql`

---

### 2.2 CategoryResource（JAX-RS Resource - プロキシ）

* 責務: カテゴリAPIのプロキシエンドポイント提供

* アノテーション:
  * `@Path("/categories")` - ベースパス
  * `@ApplicationScoped` - CDIスコープ

* 主要メソッド:

#### getAllCategories() - 全カテゴリ取得

```
@GET
@Produces(MediaType.APPLICATION_JSON)
```

* プロキシ先: `back-office-api` の `GET /api/categories`

---

### 2.3 BackOfficeRestClient（外部API連携）

* 責務: バックオフィスAPIとの連携（BFF特有）

* アノテーション:
  * `@ApplicationScoped`

* 主要フィールド:
  * `Client client` - JAX-RS Client
  * `String baseUrl` - バックオフィスAPIベースURL（@ConfigProperty）

* 主要メソッド:

#### getAllBooks()

* シグネチャ:
```java
public List<BookTO> getAllBooks()
```

* 外部API呼び出し:
```
GET {baseUrl}/books
```

#### getBookById()

* シグネチャ:
```java
public BookTO getBookById(Integer bookId)
```

* 外部API呼び出し:
```
GET {baseUrl}/books/{bookId}
```

#### searchBooks()

* シグネチャ:
```java
public List<BookTO> searchBooks(String keyword, Integer categoryId)
```

* 外部API呼び出し:
```
GET {baseUrl}/books/search/jpql?keyword={keyword}&categoryId={categoryId}
```

#### getAllCategories()

* シグネチャ:
```java
public Map<String, Integer> getAllCategories()
```

* 外部API呼び出し:
```
GET {baseUrl}/categories
```

* 戻り値: カテゴリ名 → カテゴリID のマップ

#### updateStock()

* シグネチャ:
```java
public StockTO updateStock(Integer bookId, Long version, Integer newQuantity)
```

* 外部API呼び出し:
```
PUT {baseUrl}/stocks/{bookId}
```

* リクエストボディ:
```json
{
  "quantity": 8,
  "version": 0
}
```

* 重要: リクエストボディのフィールド名は `back-office-api-sdd` の `StockUpdateRequest` と一致する必要があります：
* `quantity` - 更新後の在庫数
* `version` - 楽観的ロック用バージョン番号

* レスポンス: `StockTO`（更新後の在庫情報）

* 例外:
  * `OptimisticLockException` - バージョン不一致（409 Conflict）
  * `RuntimeException` - その他のエラー（400, 500など）

---

### 2.4 BookTO（外部API用DTO - JavaBeans）

```java
public class BookTO {
    private Integer bookId;
    private String bookName;
    private String author;
    private BigDecimal price;
    private String imageUrl;
    private Integer quantity;
    private Long version;
    private CategoryInfo category;
    private PublisherInfo publisher;
    
    // Getter/Setter省略
    
    public static class CategoryInfo {
        private Integer categoryId;
        private String categoryName;
        // Getter/Setter省略
    }
    
    public static class PublisherInfo {
        private Integer publisherId;
        private String publisherName;
        // Getter/Setter省略
    }
}
```

* 注: このDTOは`back-office-api-sdd`から返却されるものをそのまま使用します。

* フィールド説明:
  * `bookId`: 書籍ID
  * `bookName`: 書籍名
  * `author`: 著者
  * `price`: 価格（BigDecimal型、JSONではnumber型として返される）
  * `imageUrl`: 画像URL（例: `/api/images/covers/1`）
  * `quantity`: 在庫数（STOCKテーブルから取得）
  * `version`: バージョン番号（楽観的ロック用）
  * `category`: カテゴリ情報（ネスト構造）
  * `publisher`: 出版社情報（ネスト構造）

* データ構造の特徴:
  * `back-office-api-sdd`では、BookエンティティがSecondaryTableアノテーションを使ってSTOCKテーブルと結合している
  * そのため、BookTOには在庫情報（quantity, version）がフラットな構造で含まれる
  * カテゴリと出版社は内部クラス（CategoryInfo, PublisherInfo）としてネストされる
  * JSON-Bによって自動的にJSONにシリアライズされる

* JSONレスポンス例:
```json
{
  "bookId": 1,
  "bookName": "Java SEディープダイブ",
  "author": "Michael Johnson",
  "price": 3400,
  "imageUrl": "/api/images/covers/1",
  "quantity": 3,
  "version": 0,
  "category": {
    "categoryId": 1,
    "categoryName": "Java"
  },
  "publisher": {
    "publisherId": 3,
    "publisherName": "ネットワークノード出版"
  }
}
```

---

## 3. 設定情報

### 3.1 MicroProfile Config

* ファイル: `src/main/resources/META-INF/microprofile-config.properties`

```properties
# 外部API: バックオフィスAPI
back-office-api.base-url=http://localhost:8080/back-office-api-sdd/api
```

---

## 4. プロキシパターンの特徴

### 4.1 実装の簡潔性

* サービス層なし: BookServiceは実装されていない
* DAO層なし: BookDaoは実装されていない
* エンティティなし: Bookエンティティは実装されていない

### 4.2 処理フロー

```
Client → BFF (BookResource) → BackOfficeRestClient → back-office-api
                                                           ↓
                                                     BookService
                                                           ↓
                                                       BookDao
                                                           ↓
                                                       Database
```

### 4.3 利点

* 保守性: ビジネスロジックは`back-office-api`で一元管理
* 簡潔性: BFF層は単純な転送のみ
* 柔軟性: バックエンドの変更がBFF層に影響しない

---

## 5. エラーハンドリング

### 5.1 エラーシナリオ

| エラーケース | HTTPステータス | 処理 |
|------------|--------------|------|
| 書籍IDが存在しない | 404 Not Found | `back-office-api`からのエラーをそのまま転送 |
| 検索結果が0件 | 200 OK | 空配列をそのまま転送 |
| 外部API接続エラー | 503 Service Unavailable | BFF層でエラーレスポンスを生成 |

### 5.2 外部API接続エラー

* 例外: `ProcessingException`, `WebApplicationException`

* 処理:
```java
try {
    return backOfficeClient.getAllBooks();
} catch (ProcessingException e) {
    throw new ServiceUnavailableException("バックオフィスAPIに接続できません");
}
```

---

## 6. テスト要件

### 6.1 ユニットテスト

* 対象: なし（プロキシパターンのため、ロジックが存在しない）

### 6.2 統合テスト

* 対象: `BookResource` + `BackOfficeRestClient` + `back-office-api`

* 全書籍取得API呼び出し
* 書籍詳細取得API呼び出し
* 書籍検索API呼び出し
* 外部API接続エラーシナリオ

---

## 7. 実装状況

### 7.1 実装完了クラス

* 実装日: 2026-01-10

| クラス | 状態 | 備考 |
|--------|------|------|
| BookResource | ✅ 完了 | プロキシパターンで実装 |
| CategoryResource | ✅ 完了 | プロキシパターンで実装 |
| BackOfficeRestClient | ✅ 完了 | ConfigProvider方式で実装 |

### 7.2 実装時の技術的対応

#### 7.2.1 MicroProfile Config読み込み方式

* 問題: `@ConfigProperty`インジェクションが機能しない場合がある

* 解決策: `ConfigProvider`を直接使用する方式を採用

* 実装例（BackOfficeRestClient.java）:
```java
import org.eclipse.microprofile.config.ConfigProvider;

@PostConstruct
public void init() {
    this.client = ClientBuilder.newClient();
    this.baseUrl = ConfigProvider.getConfig()
            .getOptionalValue("back-office-api.base-url", String.class)
            .orElse("http://localhost:8080/back-office-api-sdd/api");
    logger.info("BackOfficeRestClient initialized, baseUrl: {}", baseUrl);
}
```

* 理由:
  * より確実な設定読み込み
  * デフォルト値の明示的な管理
  * 環境依存の問題を回避

#### 7.2.2 CDI有効化（beans.xml）

* ファイル: `src/main/webapp/WEB-INF/beans.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee 
                           https://jakarta.ee/xml/ns/jakartaee/beans_3_0.xsd"
       bean-discovery-mode="all" version="3.0">
</beans>
```

* 必要性:
  * CDIコンテナの有効化
  * `@Inject`、`@ApplicationScoped`の動作に必須
  * MicroProfile Configの正常動作に必要

#### 7.2.3 レスポンスのMediaType明示

* すべてのエンドポイントでMediaType指定:
```java
@GET
@Produces(MediaType.APPLICATION_JSON)
public Response getAllBooks() {
    // ...
    return Response.ok(books).build();
}
```

* 404エラーの場合も明示:
```java
return Response.status(Response.Status.NOT_FOUND)
        .entity("{\"error\":\"書籍が見つかりません\"}")
        .type(MediaType.APPLICATION_JSON)
        .build();
```

### 7.3 動作確認

* テスト実行日: 2026-01-10

| エンドポイント | 状態 | HTTPステータス |
|--------------|------|---------------|
| `GET /api/books` | ✅ 動作 | 200 OK |
| `GET /api/books/{id}` | ✅ 動作 | 200 OK / 404 Not Found |
| `GET /api/books/search/jpql` | ✅ 動作 | 200 OK |
| `GET /api/books/search/criteria` | ✅ 動作 | 200 OK |
| `GET /api/categories` | ✅ 動作 | 200 OK |

* テスト結果例:
```json
// GET /api/books
[
  {
    "author": "Michael Johnson",
    "bookId": 1,
    "bookName": "Java SEディープダイブ",
    "category": {
      "categoryId": 1,
      "categoryName": "Java"
    },
    "price": 3400,
    "publisher": {
      "publisherId": 3,
      "publisherName": "ネットワークノード出版"
    }
  }
]
```

---

## 8. 参考資料

* [functional_design.md](functional_design.md) - 機能設計書
* [behaviors.md](behaviors.md) - 振る舞い仕様書
* [BFFパターン](https://samnewman.io/patterns/architectural/bff/)
* [プロキシパターン](https://en.wikipedia.org/wiki/Proxy_pattern)
