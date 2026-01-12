# API_002 書籍API - 詳細設計書

* API ID: API_002  
* API名: 書籍API  
* バージョン: 1.0.0  
* 最終更新: 2025-01-10

---

## 1. パッケージ構造

### 1.1 関連パッケージ

```
pro.kensait.backoffice
├── api
│   ├── BookResource.java             # 書籍リソース
│   ├── dto
│   │   ├── BookTO.java               # 書籍転送オブジェクト
│   │   └── ErrorResponse.java        # エラーレスポンス
│   └── exception
│       └── (共通例外マッパー)
├── service
│   └── book
│       └── BookService.java          # 書籍サービス
├── dao
│   └── BookDao.java                  # 書籍データアクセス
└── entity
    ├── Book.java                     # 書籍エンティティ
    ├── Category.java                 # カテゴリエンティティ（参照）
    ├── Publisher.java                # 出版社エンティティ（参照）
    └── Stock.java                    # 在庫エンティティ（参照）
```

---

## 2. クラス設計

### 2.1 BookResource（JAX-RS Resource）

* 責務: 書籍APIのエンドポイント提供

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
  1. BookServiceで全書籍を取得
  2. BookTOリストを返却

* レスポンス: `List<BookTO>`

#### getBookById() - 書籍詳細取得

```
@GET
@Path("/{id}")
@Produces(MediaType.APPLICATION_JSON)
```

* パラメータ:
  * `@PathParam("id") Integer bookId` - 書籍ID

* 処理フロー:
  1. BookServiceで書籍を取得
  2. 存在しない場合は404エラー
  3. BookTOを返却

* レスポンス: `BookTO`

#### searchBooks() - 書籍検索（JPQL）

```
@GET
@Path("/search/jpql")
@Produces(MediaType.APPLICATION_JSON)
```

* クエリパラメータ:
  * `@QueryParam("keyword") String keyword` - キーワード（オプション）
  * `@QueryParam("categoryId") Integer categoryId` - カテゴリID（オプション）

* 処理フロー:
  1. BookServiceでJPQL検索を実行
  2. BookTOリストを返却

* レスポンス: `List<BookTO>`

#### searchBooksCriteria() - 書籍検索（Criteria API）

```
@GET
@Path("/search/criteria")
@Produces(MediaType.APPLICATION_JSON)
```

* クエリパラメータ:
  * `@QueryParam("keyword") String keyword` - キーワード（オプション）
  * `@QueryParam("categoryId") Integer categoryId` - カテゴリID（オプション）

* 処理フロー:
  1. BookServiceでCriteria API検索を実行
  2. BookTOリストを返却

* レスポンス: `List<BookTO>`

---

### 2.2 BookTO（DTO - Record）

```java
public record BookTO(
    Integer bookId,
    String bookName,
    String author,
    Integer price,
    String isbn,
    Integer categoryId,
    String categoryName,
    Integer publisherId,
    String publisherName,
    Integer quantity,
    Integer version
) {}
```

* フィールド説明:
  * `bookId`: 書籍ID
  * `bookName`: 書籍名
  * `author`: 著者
  * `price`: 価格
  * `isbn`: ISBN
  * `categoryId`: カテゴリID
  * `categoryName`: カテゴリ名
  * `publisherId`: 出版社ID
  * `publisherName`: 出版社名
  * `quantity`: 在庫数
  * `version`: 楽観的ロックバージョン

---

### 2.3 BookService（ビジネスロジック層）

* 責務: 書籍検索ビジネスロジック

* アノテーション:
  * `@ApplicationScoped`

* 主要メソッド:

#### getBooksAll()

* シグネチャ:
```java
public List<BookTO> getBooksAll()
```

* 処理:
  1. BookDaoで全書籍を取得
  2. BookエンティティをBookTOに変換
  3. BookTOリストを返却

#### getBookById()

* シグネチャ:
```java
public BookTO getBookById(Integer bookId)
```

* 処理:
  1. BookDaoで書籍を取得
  2. 存在しない場合はnullを返却
  3. BookエンティティをBookTOに変換

#### searchBooksJpql()

* シグネチャ:
```java
public List<BookTO> searchBooksJpql(String keyword, Integer categoryId)
```

* 処理:
  1. BookDaoでJPQL検索を実行
  2. BookエンティティリストをBookTOリストに変換

#### searchBooksCriteria()

* シグネチャ:
```java
public List<BookTO> searchBooksCriteria(String keyword, Integer categoryId)
```

* 処理:
  1. BookDaoでCriteria API検索を実行
  2. BookエンティティリストをBookTOリストに変換

---

### 2.4 BookDao（データアクセス層）

* 責務: 書籍データのCRUD操作

* アノテーション:
  * `@ApplicationScoped`

* 主要メソッド:

#### findAll()

* シグネチャ:
```java
public List<Book> findAll()
```

* JPQL:
```sql
SELECT b FROM Book b 
JOIN FETCH b.category 
JOIN FETCH b.publisher 
ORDER BY b.bookId
```

#### findById()

* シグネチャ:
```java
public Book findById(Integer bookId)
```

* JPQL:
```sql
SELECT b FROM Book b 
JOIN FETCH b.category 
JOIN FETCH b.publisher 
WHERE b.bookId = :bookId
```

#### searchByJpql()

* シグネチャ:
```java
public List<Book> searchByJpql(String keyword, Integer categoryId)
```

* JPQL（動的クエリ）:
```sql
SELECT b FROM Book b 
JOIN FETCH b.category 
JOIN FETCH b.publisher 
WHERE (:keyword IS NULL OR b.bookName LIKE :keyword OR b.author LIKE :keyword)
  AND (:categoryId IS NULL OR b.category.categoryId = :categoryId)
ORDER BY b.bookId
```

#### searchByCriteria()

* シグネチャ:
```java
public List<Book> searchByCriteria(String keyword, Integer categoryId)
```

* Criteria API実装:
  1. CriteriaBuilderを取得
  2. CriteriaQueryを作成
  3. Rootを定義
  4. JOIN FETCHでcategoryとpublisherを取得
  5. 動的にWHERE句を構築
     * keywordが指定されている場合: bookNameまたはauthorに部分一致
     * categoryIdが指定されている場合: categoryIdで完全一致
  6. ORDER BY bookIdを追加
  7. TypedQueryを実行

---

### 2.5 Book（エンティティ）

* テーブル: `BOOK` + `STOCK`（@SecondaryTable）

* 主要フィールド:

| フィールド名 | 型 | カラム名 | テーブル | 制約 | 説明 |
|------------|---|---------|---------|-----|------|
| `bookId` | `Integer` | `BOOK_ID` | BOOK | `@Id @GeneratedValue` | 書籍ID（主キー） |
| `bookName` | `String` | `BOOK_NAME` | BOOK | `@Column(nullable=false)` | 書籍名 |
| `author` | `String` | `AUTHOR` | BOOK | `@Column(nullable=false)` | 著者 |
| `price` | `Integer` | `PRICE` | BOOK | `@Column(nullable=false)` | 価格 |
| `isbn` | `String` | `ISBN` | BOOK | `@Column(unique=true)` | ISBN |
| `category` | `Category` | `CATEGORY_ID` | BOOK | `@ManyToOne` | カテゴリ |
| `publisher` | `Publisher` | `PUBLISHER_ID` | BOOK | `@ManyToOne` | 出版社 |
| `quantity` | `Integer` | `QUANTITY` | STOCK | `@Column(table="STOCK")` | 在庫数 |
| `version` | `Integer` | `VERSION` | STOCK | `@Column(table="STOCK") @Version` | 楽観的ロックバージョン |

* アノテーション:
```java
@Entity
@Table(name = "BOOK")
@SecondaryTable(name = "STOCK", pkJoinColumns = @PrimaryKeyJoinColumn(name = "BOOK_ID"))
```

* リレーション:
  * `@ManyToOne` - Category（多対一）
  * `@ManyToOne` - Publisher（多対一）

---

## 3. 検索実装の比較

### 3.1 JPQL検索

* 特徴:
  * 文字列ベースのクエリ
  * 動的クエリの構築が容易
  * 可読性が高い

* 実装例:
```java
String jpql = "SELECT b FROM Book b " +
              "JOIN FETCH b.category " +
              "JOIN FETCH b.publisher " +
              "WHERE (:keyword IS NULL OR b.bookName LIKE :keyword OR b.author LIKE :keyword) " +
              "AND (:categoryId IS NULL OR b.category.categoryId = :categoryId) " +
              "ORDER BY b.bookId";
```

### 3.2 Criteria API検索

* 特徴:
  * 型安全なクエリ
  * コンパイル時のチェック
  * 動的クエリの構築が複雑

* 実装例:
```java
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Book> cq = cb.createQuery(Book.class);
Root<Book> book = cq.from(Book.class);
book.fetch("category", JoinType.INNER);
book.fetch("publisher", JoinType.INNER);

List<Predicate> predicates = new ArrayList<>();
if (keyword != null && !keyword.isEmpty()) {
    String likePattern = "%" + keyword + "%";
    predicates.add(cb.or(
        cb.like(book.get("bookName"), likePattern),
        cb.like(book.get("author"), likePattern)
    ));
}
if (categoryId != null) {
    predicates.add(cb.equal(book.get("category").get("categoryId"), categoryId));
}

cq.where(predicates.toArray(new Predicate[0]));
cq.orderBy(cb.asc(book.get("bookId")));
```

---

## 4. エラーハンドリング

### 4.1 エラーシナリオ

| エラーケース | HTTPステータス | レスポンス |
|------------|--------------|----------|
| 書籍IDが存在しない | 404 Not Found | `{"error": "Not Found", "message": "書籍が見つかりません"}` |
| 検索結果が0件 | 200 OK | `[]`（空配列） |
| 不正なパラメータ | 400 Bad Request | `{"error": "Bad Request", "message": "パラメータが不正です"}` |

---

## 5. テスト要件

### 5.1 ユニットテスト

* 対象: `BookService`

* 全書籍取得テスト
* 書籍詳細取得テスト（正常系）
* 書籍詳細取得テスト（存在しない書籍）
* JPQL検索テスト（キーワードのみ）
* JPQL検索テスト（カテゴリIDのみ）
* JPQL検索テスト（両方指定）
* Criteria API検索テスト（同上）

### 5.2 統合テスト

* 対象: `BookResource` + `BookService` + `BookDao`

* 全書籍取得API呼び出し
* 書籍詳細取得API呼び出し
* 書籍検索API呼び出し（JPQL）
* 書籍検索API呼び出し（Criteria API）

---

## 6. 参考資料

* [functional_design.md](functional_design.md) - 機能設計書
* [behaviors.md](behaviors.md) - 振る舞い仕様書
* [JPA仕様](https://jakarta.ee/specifications/persistence/3.1/)
* [Criteria API](https://jakarta.ee/specifications/persistence/3.1/jakarta-persistence-spec-3.1.html#a6652)
