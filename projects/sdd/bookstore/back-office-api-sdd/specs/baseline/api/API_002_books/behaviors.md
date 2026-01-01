# 書籍API 振る舞い仕様書

## 1. 概要

本ドキュメントは、書籍API（`/api/books`）の動的な振る舞いをシーケンス図、状態遷移図、フローチャートで定義する。

## 2. 書籍一覧取得シーケンス

### 2.1 正常系: 全件取得

```
┌──────┐         ┌────────────┐      ┌───────────┐      ┌────────┐      ┌────────┐
│Client│         │BookResource│      │BookService│      │BookDao │      │Database│
└──┬───┘         └──────┬─────┘      └─────┬─────┘      └───┬────┘      └───┬────┘
   │                     │                   │                │               │
   │GET /api/books       │                   │                │               │
   ├────────────────────►│                   │                │               │
   │                     │                   │                │               │
   │                     │getAllBooks()      │                │               │
   │                     ├──────────────────►│                │               │
   │                     │                   │getBooksAll()   │               │
   │                     │                   ├───────────────►│               │
   │                     │                   │                │findAll()      │
   │                     │                   │                ├──────────────►│
   │                     │                   │                │SELECT b.*,    │
   │                     │                   │                │  s.quantity,  │
   │                     │                   │                │  s.version    │
   │                     │                   │                │FROM BOOK b    │
   │                     │                   │                │JOIN STOCK s   │
   │                     │                   │                │ON b.id=s.id   │
   │                     │                   │                │◄──────────────┤
   │                     │                   │                │List<Book>     │
   │                     │                   │◄───────────────┤               │
   │                     │                   │List<Book>      │               │
   │                     │◄──────────────────┤                │               │
   │                     │List<Book>         │                │               │
   │                     │                   │                │               │
   │                     │convertToBookTO()  │                │               │
   │                     │──────────┐        │                │               │
   │                     │          │        │                │               │
   │                     │ - category info   │                │               │
   │                     │ - publisher info  │                │               │
   │                     │ - stock info      │                │               │
   │                     │◄─────────┘        │                │               │
   │                     │                   │                │               │
   │◄────────────────────┤200 OK             │                │               │
   │[{BookTO}]           │Content-Type:      │                │               │
   │                     │application/json   │                │               │
   │                     │                   │                │               │
```

### 2.2 データ変換の詳細

```
Book Entity                →    BookTO
─────────────────────────────────────────────────────
bookId: 1                  →    bookId: 1
bookName: "サンプル書籍"    →    bookName: "サンプル書籍"
author: "著者名"            →    author: "著者名"
price: 2500                →    price: 2500
imageUrl: "http://..."     →    imageUrl: "http://..."
quantity: 10               →    quantity: 10
version: 1                 →    version: 1

category.categoryId: 1     →    category: {
category.categoryName: "文学"        categoryId: 1,
                                    categoryName: "文学"
                                }

publisher.publisherId: 1   →    publisher: {
publisher.publisherName:            publisherId: 1,
  "出版社A"                           publisherName: "出版社A"
                                }
```

## 3. 書籍詳細取得シーケンス

### 3.1 正常系: 書籍が存在する場合

```
┌──────┐         ┌────────────┐      ┌───────────┐      ┌────────┐      ┌────────┐
│Client│         │BookResource│      │BookService│      │BookDao │      │Database│
└──┬───┘         └──────┬─────┘      └─────┬─────┘      └───┬────┘      └───┬────┘
   │                     │                   │                │               │
   │GET /api/books/1     │                   │                │               │
   ├────────────────────►│                   │                │               │
   │                     │                   │                │               │
   │                     │Extract PathParam  │                │               │
   │                     │──────────┐        │                │               │
   │                     │          │        │                │               │
   │                     │◄─────────┘        │                │               │
   │                     │id = 1             │                │               │
   │                     │                   │                │               │
   │                     │getBookById(1)     │                │               │
   │                     ├──────────────────►│                │               │
   │                     │                   │getBook(1)      │               │
   │                     │                   ├───────────────►│               │
   │                     │                   │                │findById(1)    │
   │                     │                   │                ├──────────────►│
   │                     │                   │                │EntityManager  │
   │                     │                   │                │.find(Book,1)  │
   │                     │                   │                │◄──────────────┤
   │                     │                   │                │Book entity    │
   │                     │                   │◄───────────────┤               │
   │                     │                   │Book entity     │               │
   │                     │◄──────────────────┤                │               │
   │                     │Book entity        │                │               │
   │                     │                   │                │               │
   │                     │Check if null      │                │               │
   │                     │──────────┐        │                │               │
   │                     │          │        │                │               │
   │                     │◄─────────┘        │                │               │
   │                     │not null ✓         │                │               │
   │                     │                   │                │               │
   │                     │convertToBookTO()  │                │               │
   │                     │──────────┐        │                │               │
   │                     │          │        │                │               │
   │                     │◄─────────┘        │                │               │
   │                     │                   │                │               │
   │◄────────────────────┤200 OK             │                │               │
   │{BookTO}             │                   │                │               │
   │                     │                   │                │               │
```

### 3.2 異常系: 書籍が存在しない場合

```
┌──────┐         ┌────────────┐      ┌───────────┐      ┌────────┐      ┌────────┐
│Client│         │BookResource│      │BookService│      │BookDao │      │Database│
└──┬───┘         └──────┬─────┘      └─────┬─────┘      └───┬────┘      └───┬────┘
   │                     │                   │                │               │
   │GET /api/books/9999  │                   │                │               │
   ├────────────────────►│                   │                │               │
   │                     │                   │                │               │
   │                     │getBookById(9999)  │                │               │
   │                     ├──────────────────►│                │               │
   │                     │                   │getBook(9999)   │               │
   │                     │                   ├───────────────►│               │
   │                     │                   │                │findById(9999) │
   │                     │                   │                ├──────────────►│
   │                     │                   │                │◄──────────────┤
   │                     │                   │                │null           │
   │                     │                   │◄───────────────┤               │
   │                     │                   │null            │               │
   │                     │◄──────────────────┤                │               │
   │                     │null               │                │               │
   │                     │                   │                │               │
   │                     │Check if null      │                │               │
   │                     │──────────┐        │                │               │
   │                     │          │        │                │               │
   │                     │◄─────────┘        │                │               │
   │                     │null ✗             │                │               │
   │                     │                   │                │               │
   │◄────────────────────┤404 Not Found      │                │               │
   │{error: "書籍が       │                   │                │               │
   │ 見つかりません"}     │                   │                │               │
   │                     │                   │                │               │
```

## 4. 書籍検索シーケンス（JPQL）

### 4.1 カテゴリ + キーワード検索

```
┌──────┐         ┌────────────┐      ┌───────────┐      ┌────────┐      ┌────────┐
│Client│         │BookResource│      │BookService│      │BookDao │      │Database│
└──┬───┘         └──────┬─────┘      └─────┬─────┘      └───┬────┘      └───┬────┘
   │                     │                   │                │               │
   │GET /api/books/search│                   │                │               │
   │  /jpql?categoryId=1 │                   │                │               │
   │  &keyword=サンプル   │                   │                │               │
   ├────────────────────►│                   │                │               │
   │                     │                   │                │               │
   │                     │Extract Query Params                │               │
   │                     │──────────┐        │                │               │
   │                     │          │        │                │               │
   │                     │◄─────────┘        │                │               │
   │                     │categoryId = 1     │                │               │
   │                     │keyword = "サンプル"│                │               │
   │                     │                   │                │               │
   │                     │searchBooksJpql()  │                │               │
   │                     ├──────────────────►│                │               │
   │                     │                   │                │               │
   │                     │                   │Validate Params │               │
   │                     │                   │──────────┐     │               │
   │                     │                   │          │     │               │
   │                     │                   │◄─────────┘     │               │
   │                     │                   │                │               │
   │                     │                   │Check Conditions│               │
   │                     │                   │──────────┐     │               │
   │                     │                   │          │     │               │
   │                     │                   │ categoryId != 0?               │
   │                     │                   │ keyword not empty?             │
   │                     │                   │◄─────────┘     │               │
   │                     │                   │Both true       │               │
   │                     │                   │                │               │
   │                     │                   │searchBook(     │               │
   │                     │                   │  categoryId,   │               │
   │                     │                   │  keyword)      │               │
   │                     │                   ├───────────────►│               │
   │                     │                   │                │               │
   │                     │                   │                │toLikeWord()   │
   │                     │                   │                │──────────┐    │
   │                     │                   │                │          │    │
   │                     │                   │                │"サンプル" → │
   │                     │                   │                │"%サンプル%"   │
   │                     │                   │                │◄─────────┘    │
   │                     │                   │                │               │
   │                     │                   │                │query()        │
   │                     │                   │                ├──────────────►│
   │                     │                   │                │Named Query:   │
   │                     │                   │                │Book.query     │
   │                     │                   │                │               │
   │                     │                   │                │SELECT b       │
   │                     │                   │                │FROM Book b    │
   │                     │                   │                │WHERE          │
   │                     │                   │                │ b.category    │
   │                     │                   │                │  .id = :cat   │
   │                     │                   │                │ AND (         │
   │                     │                   │                │  b.bookName   │
   │                     │                   │                │   LIKE :key   │
   │                     │                   │                │  OR b.author  │
   │                     │                   │                │   LIKE :key   │
   │                     │                   │                │ )             │
   │                     │                   │                │◄──────────────┤
   │                     │                   │                │List<Book>     │
   │                     │                   │◄───────────────┤               │
   │                     │                   │List<Book>      │               │
   │                     │◄──────────────────┤                │               │
   │                     │List<Book>         │                │               │
   │                     │                   │                │               │
   │                     │Stream & Map       │                │               │
   │                     │──────────┐        │                │               │
   │                     │          │        │                │               │
   │                     │ books.stream()    │                │               │
   │                     │  .map(this::      │                │               │
   │                     │   convertToBookTO)│                │               │
   │                     │  .collect()       │                │               │
   │                     │◄─────────┘        │                │               │
   │                     │                   │                │               │
   │◄────────────────────┤200 OK             │                │               │
   │[{BookTO}]           │                   │                │               │
   │                     │                   │                │               │
```

### 4.2 検索条件の分岐処理フローチャート

```
┌──────────────────┐
│Query Params取得   │
│ categoryId       │
│ keyword          │
└────────┬─────────┘
         │
         ▼
┌────────────────────┐
│Condition Check     │
└┬────┬────┬────┬───┘
 │    │    │    │
 │    │    │    │
 ▼    ▼    ▼    ▼
┌──┐┌──┐┌──┐┌────┐
│両││C ││K ││なし│
│方││a ││e ││    │
│有││t ││y ││    │
│効││有││有││    │
│ ││効││効││    │
└┬─┘└┬─┘└┬─┘└┬───┘
 │   │   │   │
 ▼   ▼   ▼   ▼
searchBook() searchBook() searchBook() getBooksAll()
(cat, key)  (cat)        (key)
 │           │            │            │
 ▼           ▼            ▼            ▼
query()     queryBy      queryBy      findAll()
            Category()   Keyword()
 │           │            │            │
 └───────────┴────────────┴────────────┘
             │
             ▼
      ┌─────────────┐
      │List<Book>   │
      └─────────────┘
```

## 5. 書籍検索シーケンス（Criteria API）

### 5.1 動的クエリ構築

```
┌──────┐         ┌────────────┐      ┌───────────┐      ┌────────┐      ┌────────┐
│Client│         │BookResource│      │BookService│      │BookDao │      │Database│
└──┬───┘         └──────┬─────┘      └─────┬─────┘      └───┬────┘      └───┬────┘
   │                     │                   │                │               │
   │GET /api/books/search│                   │                │               │
   │  /criteria          │                   │                │               │
   │  ?categoryId=1      │                   │                │               │
   │  &keyword=サンプル   │                   │                │               │
   ├────────────────────►│                   │                │               │
   │                     │                   │                │               │
   │                     │searchBooksCriteria()               │               │
   │                     ├──────────────────►│                │               │
   │                     │                   │                │               │
   │                     │                   │searchBookWith  │               │
   │                     │                   │ Criteria()     │               │
   │                     │                   ├───────────────►│               │
   │                     │                   │                │               │
   │                     │                   │                │Build Dynamic  │
   │                     │                   │                │Query          │
   │                     │                   │                │──────────┐    │
   │                     │                   │                │          │    │
   │                     │                   │                │CriteriaBuilder│
   │                     │                   │                │cb = em.get... │
   │                     │                   │                │               │
   │                     │                   │                │CriteriaQuery  │
   │                     │                   │                │<Book> cq =    │
   │                     │                   │                │cb.create...   │
   │                     │                   │                │               │
   │                     │                   │                │Root<Book> book│
   │                     │                   │                │ = cq.from...  │
   │                     │                   │                │               │
   │                     │                   │                │List<Predicate>│
   │                     │                   │                │ predicates    │
   │                     │                   │                │ = new...      │
   │                     │                   │                │               │
   │                     │                   │                │// Category条件│
   │                     │                   │                │if (categoryId │
   │                     │                   │                │  != null &&   │
   │                     │                   │                │  != 0) {      │
   │                     │                   │                │ predicates.add│
   │                     │                   │                │  (cb.equal... │
   │                     │                   │                │}              │
   │                     │                   │                │               │
   │                     │                   │                │// Keyword条件 │
   │                     │                   │                │if (keyword != │
   │                     │                   │                │  null &&      │
   │                     │                   │                │  !isEmpty) {  │
   │                     │                   │                │ Predicate     │
   │                     │                   │                │  nameLike =   │
   │                     │                   │                │  cb.like(...  │
   │                     │                   │                │ Predicate     │
   │                     │                   │                │  authorLike = │
   │                     │                   │                │  cb.like(...  │
   │                     │                   │                │ predicates.add│
   │                     │                   │                │  (cb.or(...   │
   │                     │                   │                │}              │
   │                     │                   │                │               │
   │                     │                   │                │// 条件結合    │
   │                     │                   │                │if (!predicates│
   │                     │                   │                │  .isEmpty) {  │
   │                     │                   │                │ cq.where(     │
   │                     │                   │                │  cb.and(...   │
   │                     │                   │                │}              │
   │                     │                   │                │◄─────────┘    │
   │                     │                   │                │               │
   │                     │                   │                │Execute Query  │
   │                     │                   │                ├──────────────►│
   │                     │                   │                │em.createQuery │
   │                     │                   │                │ (cq)          │
   │                     │                   │                │ .getResultList│
   │                     │                   │                │               │
   │                     │                   │                │◄──────────────┤
   │                     │                   │                │List<Book>     │
   │                     │                   │◄───────────────┤               │
   │                     │                   │List<Book>      │               │
   │                     │◄──────────────────┤                │               │
   │                     │List<Book>         │                │               │
   │                     │                   │                │               │
   │                     │Convert to BookTO  │                │               │
   │                     │──────────┐        │                │               │
   │                     │          │        │                │               │
   │                     │◄─────────┘        │                │               │
   │                     │                   │                │               │
   │◄────────────────────┤200 OK             │                │               │
   │[{BookTO}]           │                   │                │               │
   │                     │                   │                │               │
```

### 5.2 Criteria API vs JPQL 比較フローチャート

```
┌────────────────────────────────────────────────┐
│              検索リクエスト                      │
└────────────┬──────────────┬────────────────────┘
             │              │
             │              │
        /jpql│              │/criteria
             │              │
             ▼              ▼
┌─────────────────┐  ┌─────────────────┐
│JPQL (静的クエリ)│  │Criteria API     │
│                 │  │(動的クエリ)      │
└────────┬────────┘  └────────┬────────┘
         │                    │
         │                    │
         ▼                    ▼
┌─────────────────┐  ┌─────────────────┐
│Named Query使用  │  │コードでクエリ構築│
│・条件固定        │  │・条件可変        │
│・SQL文字列      │  │・タイプセーフ    │
│・パフォーマンス  │  │・柔軟性高い      │
│  良好（キャッシュ│  │・少し遅い        │
│  効く）          │  │                 │
└────────┬────────┘  └────────┬────────┘
         │                    │
         └────────┬───────────┘
                  │
                  ▼
         ┌─────────────────┐
         │同じ結果セット    │
         │List<Book>        │
         └─────────────────┘
```

## 6. カテゴリ一覧取得シーケンス（書籍API経由）

```
┌──────┐         ┌────────────┐      ┌──────────────┐      ┌───────────┐      ┌────────┐
│Client│         │BookResource│      │CategoryService│      │CategoryDao│      │Database│
└──┬───┘         └──────┬─────┘      └───────┬──────┘      └─────┬─────┘      └───┬────┘
   │                     │                     │                    │               │
   │GET /api/books/      │                     │                    │               │
   │  categories         │                     │                    │               │
   ├────────────────────►│                     │                    │               │
   │                     │                     │                    │               │
   │                     │getAllCategories()   │                    │               │
   │                     ├────────────────────►│                    │               │
   │                     │                     │getCategoryMap()    │               │
   │                     │                     ├───────────────────►│               │
   │                     │                     │                    │findAll()      │
   │                     │                     │                    ├──────────────►│
   │                     │                     │                    │SELECT *       │
   │                     │                     │                    │FROM CATEGORY  │
   │                     │                     │                    │◄──────────────┤
   │                     │                     │                    │List<Category> │
   │                     │                     │◄───────────────────┤               │
   │                     │                     │List<Category>      │               │
   │                     │                     │                    │               │
   │                     │                     │Convert to Map      │               │
   │                     │                     │──────────┐         │               │
   │                     │                     │          │         │               │
   │                     │                     │Map<String,Integer> │               │
   │                     │                     │categoryMap = new...│               │
   │                     │                     │for (Category c:... │               │
   │                     │                     │  categoryMap.put(  │               │
   │                     │                     │   c.getName(),     │               │
   │                     │                     │   c.getId()        │               │
   │                     │                     │  );                │               │
   │                     │                     │}                   │               │
   │                     │                     │◄─────────┘         │               │
   │                     │◄────────────────────┤                    │               │
   │                     │Map<String,Integer>  │                    │               │
   │                     │                    │                    │               │
   │◄────────────────────┤200 OK              │                    │               │
   │{"文学": 1,          │                    │                    │               │
   │ "ビジネス": 2, ... }│                    │                    │               │
   │                     │                    │                    │               │
```

## 7. エラーハンドリングシーケンス

### 7.1 404 Not Found（書籍が見つからない）

```
┌──────┐         ┌────────────┐      ┌───────────┐
│Client│         │BookResource│      │BookService│
└──┬───┘         └──────┬─────┘      └─────┬─────┘
   │                     │                   │
   │GET /api/books/9999  │                   │
   ├────────────────────►│                   │
   │                     │getBook(9999)      │
   │                     ├──────────────────►│
   │                     │                   │
   │                     │                   │findById() → null
   │                     │                   │──────────┐
   │                     │                   │          │
   │                     │                   │◄─────────┘
   │                     │◄──────────────────┤null
   │                     │                   │
   │                     │Check null         │
   │                     │──────────┐        │
   │                     │          │        │
   │                     │◄─────────┘        │
   │                     │                   │
   │                     │Build Error        │
   │                     │Response           │
   │                     │──────────┐        │
   │                     │          │        │
   │                     │{error:            │
   │                     │ "書籍が            │
   │                     │  見つかりません"}   │
   │                     │◄─────────┘        │
   │                     │                   │
   │◄────────────────────┤404 Not Found      │
   │{error: "書籍が       │                   │
   │ 見つかりません"}     │                   │
   │                     │                   │
```

## 8. パフォーマンス特性

### 8.1 レスポンスタイム分析

```
書籍一覧取得 (Total: ~200ms)

┌────────────────────────────────────────────────────────┐
│ DB Query (JOIN BOOK+STOCK)               100ms (50%)  │
├────────────────────────────────────────────────────────┤
│ Entity → DTO Conversion                  50ms (25%)   │
├────────────────────────────────────────────────────────┤
│ JSON Serialization                       30ms (15%)   │
├────────────────────────────────────────────────────────┤
│ その他（ネットワーク、オーバーヘッド）     20ms (10%)   │
└────────────────────────────────────────────────────────┘
```

```
書籍検索 (Total: ~500ms)

┌────────────────────────────────────────────────────────┐
│ DB Query (WHERE条件付き)                 300ms (60%)  │
├────────────────────────────────────────────────────────┤
│ Entity → DTO Conversion                  100ms (20%)  │
├────────────────────────────────────────────────────────┤
│ JSON Serialization                       50ms (10%)   │
├────────────────────────────────────────────────────────┤
│ その他（ネットワーク、オーバーヘッド）     50ms (10%)   │
└────────────────────────────────────────────────────────┘
```

### 8.2 JPQL vs Criteria API パフォーマンス比較

```
初回実行（キャッシュなし）:
┌────────────────────────────────────────┐
│ JPQL          ████████░░░░  80ms      │
├────────────────────────────────────────┤
│ Criteria API  ██████████░░  100ms     │
└────────────────────────────────────────┘

2回目以降（キャッシュあり）:
┌────────────────────────────────────────┐
│ JPQL          ███░░░░░░░░░  30ms      │
├────────────────────────────────────────┤
│ Criteria API  ████░░░░░░░░  40ms      │
└────────────────────────────────────────┘

複雑な条件（3条件以上）:
┌────────────────────────────────────────┐
│ JPQL          ████████████  120ms     │
├────────────────────────────────────────┤
│ Criteria API  ██████████░░  100ms     │
└────────────────────────────────────────┘
```

**結論**:
- **単純な条件**: JPQLが高速（Named Queryのキャッシュ効果）
- **複雑な条件**: Criteria APIが有利（動的な条件構築）
- **保守性**: Criteria APIがタイプセーフで優れている

## 9. データフロー全体図

```
┌──────────────────────────────────────────────────────────┐
│                         Client                           │
└───────────────────────┬──────────────────────────────────┘
                        │ HTTP GET
                        │ /api/books?...
                        ▼
┌──────────────────────────────────────────────────────────┐
│                    BookResource                          │
│  - getAllBooks()                                         │
│  - getBookById(id)                                       │
│  - searchBooks(categoryId, keyword)                      │
│  - searchBooksJpql(categoryId, keyword)                  │
│  - searchBooksCriteria(categoryId, keyword)              │
│  - getAllCategories()                                    │
└───────────────────────┬──────────────────────────────────┘
                        │ @Inject
                        ▼
┌──────────────────────────────────────────────────────────┐
│                    BookService                           │
│  - getBooksAll()                                         │
│  - getBook(bookId)                                       │
│  - searchBook(categoryId, keyword)                       │
│  - searchBook(categoryId)                                │
│  - searchBook(keyword)                                   │
│  - searchBookWithCriteria(categoryId, keyword)           │
└───────────────────────┬──────────────────────────────────┘
                        │ @Inject
                        ▼
┌──────────────────────────────────────────────────────────┐
│                      BookDao                             │
│  - findAll()                                             │
│  - findById(id)                                          │
│  - query(categoryId, keyword)                            │
│  - queryByCategory(categoryId)                           │
│  - queryByKeyword(keyword)                               │
│  - searchWithCriteria(categoryId, keyword)               │
└───────────────────────┬──────────────────────────────────┘
                        │ EntityManager / JPQL / Criteria
                        ▼
┌──────────────────────────────────────────────────────────┐
│                     Database                             │
│  - BOOK table                                            │
│  - STOCK table (SecondaryTable)                          │
│  - CATEGORY table (ManyToOne)                            │
│  - PUBLISHER table (ManyToOne)                           │
└──────────────────────────────────────────────────────────┘
```

## 10. ログ出力シーケンス

### 10.1 正常系ログ

```
Time    Level   Message
─────────────────────────────────────────────────────────────
t1      INFO    [ BookResource#getAllBooks ]
t2      INFO    [ BookService#getBooksAll ]
t3      DEBUG   [ BookDao#findAll ] Executing JPQL: SELECT b FROM Book b
t4      DEBUG   [ BookDao#findAll ] Result count: 50
t5      DEBUG   [ BookResource#convertToBookTO ] Converting 50 entities
t6      INFO    [ BookResource#getAllBooks ] Success: 50 books returned
```

### 10.2 検索ログ

```
Time    Level   Message
─────────────────────────────────────────────────────────────
t1      INFO    [ BookResource#searchBooksJpql ] categoryId: 1, keyword: サンプル
t2      INFO    [ BookService#searchBook(categoryId, keyword) ]
t3      DEBUG   [ BookDao#query ] Parameters: categoryId=1, keyword=%サンプル%
t4      DEBUG   [ BookDao#query ] JPQL: WHERE b.category.categoryId = :categoryId AND ...
t5      DEBUG   [ BookDao#query ] Result count: 3
t6      INFO    [ BookResource#searchBooksJpql ] Success: 3 books returned
```

### 10.3 エラーログ

```
Time    Level   Message
─────────────────────────────────────────────────────────────
t1      INFO    [ BookResource#getBookById ] id: 9999
t2      INFO    [ BookService#getBook ]
t3      DEBUG   [ BookDao#findById ] bookId=9999
t4      DEBUG   [ BookDao#findById ] Result: null
t5      WARN    [ BookResource#getBookById ] Book not found: 9999
t6      INFO    [ BookResource#getBookById ] Returning 404 Not Found
```

## 11. 並行処理シナリオ

### 11.1 同時検索リクエスト

```
時刻   ユーザーA                           ユーザーB
t1    GET /api/books/search
      ?categoryId=1
t2                                       GET /api/books/search
                                         ?keyword=サンプル
t3    → DB Query (category=1)
t4                                       → DB Query (keyword=サンプル)
t5    ← Result (10 books)
t6                                       ← Result (5 books)
```

**結果**: 独立したトランザクションで並行実行可能

### 11.2 大量リクエスト時のスケーリング

```
100 concurrent requests

┌────────────────────────────────────────┐
│ Connection Pool (Max: 20)              │
├────────────────────────────────────────┤
│ Active: ████████████████████░  20/20   │
│ Waiting: ████████████████████  80 req  │
└────────────────────────────────────────┘

Response Time:
- First 20:  200ms (normal)
- Next 20:   400ms (wait + execute)
- Next 20:   600ms (wait + execute)
- Next 20:   800ms (wait + execute)
- Last 20:  1000ms (wait + execute)

Average: 600ms
```

**対策**:
- コネクションプール拡大
- キャッシング導入
- ページネーション実装

## 12. 将来の拡張シーケンス

### 12.1 ページネーション（実装予定）

```
┌──────┐         ┌────────────┐      ┌───────────┐      ┌────────┐
│Client│         │BookResource│      │BookService│      │BookDao │
└──┬───┘         └──────┬─────┘      └─────┬─────┘      └───┬────┘
   │                     │                   │                │
   │GET /api/books       │                   │                │
   │  ?page=1&size=20    │                   │                │
   ├────────────────────►│                   │                │
   │                     │                   │                │
   │                     │Extract Pagination │                │
   │                     │Params             │                │
   │                     │──────────┐        │                │
   │                     │          │        │                │
   │                     │ page=1   │        │                │
   │                     │ size=20  │        │                │
   │                     │◄─────────┘        │                │
   │                     │                   │                │
   │                     │getBooks(page,size)│                │
   │                     ├──────────────────►│                │
   │                     │                   │                │
   │                     │                   │findAll(        │
   │                     │                   │  page, size)   │
   │                     │                   ├───────────────►│
   │                     │                   │                │
   │                     │                   │                │setFirstResult│
   │                     │                   │                │ (page*size)  │
   │                     │                   │                │setMaxResults │
   │                     │                   │                │ (size)       │
   │                     │                   │                │              │
   │                     │                   │◄───────────────┤List<Book>    │
   │                     │                   │Page<Book>      │+ totalCount  │
   │                     │◄──────────────────┤                │              │
   │                     │Page<Book>         │                │              │
   │                     │                   │                │              │
   │◄────────────────────┤200 OK             │                │              │
   │{content: [...],     │                   │                │              │
   │ page: 1,            │                   │                │              │
   │ size: 20,           │                   │                │              │
   │ totalElements: 100, │                   │                │              │
   │ totalPages: 5}      │                   │                │              │
   │                     │                   │                │              │
```
