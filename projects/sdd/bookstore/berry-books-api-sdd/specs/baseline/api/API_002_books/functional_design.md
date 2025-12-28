# API_002_books - 書籍API機能設計書

**API ID:** API_002_books  
**API名:** 書籍API  
**ベースパス:** `/api/books`  
**バージョン:** 2.0.0  
**最終更新日:** 2025-12-27

---

## 1. 概要

書籍APIは、書籍の一覧取得、詳細取得、検索、カテゴリ一覧取得を提供する。すべてのエンドポイントは認証不要で、公開APIとして利用できる。

**認証要否**: 不要（公開API）

---

## 2. エンドポイント一覧

| メソッド | エンドポイント | 説明 | 認証 |
|---------|--------------|------|-----|
| GET | `/api/books` | 全書籍取得 | 不要 |
| GET | `/api/books/{id}` | 書籍詳細取得 | 不要 |
| GET | `/api/books/search?categoryId={id}&keyword={keyword}` | 書籍検索 | 不要 |
| GET | `/api/books/categories` | カテゴリ一覧取得 | 不要 |

---

## 3. API仕様

### 3.1 書籍一覧取得

#### 3.1.1 エンドポイント

```
GET /api/books
```

#### 3.1.2 概要

全書籍を取得する。認証不要。

#### 3.1.3 リクエスト

**リクエストパラメータ**: なし

#### 3.1.4 レスポンス

**成功時 (200 OK)**:

```json
[
  {
    "bookId": 1,
    "bookName": "Java入門",
    "author": "山田太郎",
    "categoryId": 1,
    "publisherId": 1,
    "price": 3000,
    "category": {
      "categoryId": 1,
      "categoryName": "Java"
    },
    "publisher": {
      "publisherId": 1,
      "publisherName": "技術評論社"
    },
    "stock": {
      "bookId": 1,
      "quantity": 10,
      "version": 1
    }
  }
]
```

---

### 3.2 書籍詳細取得

#### 3.2.1 エンドポイント

```
GET /api/books/{id}
```

#### 3.2.2 概要

指定されたIDの書籍を取得する。認証不要。

#### 3.2.3 リクエスト

**パスパラメータ**:

| パラメータ | 型 | 説明 |
|----------|---|------|
| id | integer | 書籍ID |

#### 3.2.4 レスポンス

**成功時 (200 OK)**:

```json
{
  "bookId": 1,
  "bookName": "Java入門",
  "author": "山田太郎",
  "categoryId": 1,
  "publisherId": 1,
  "price": 3000,
  "category": {
    "categoryId": 1,
    "categoryName": "Java"
  },
  "publisher": {
    "publisherId": 1,
    "publisherName": "技術評論社"
  },
  "stock": {
    "bookId": 1,
    "quantity": 10,
    "version": 1
  }
}
```

**エラー時 (404 Not Found)**:

```json
{
  "error": "書籍が見つかりません"
}
```

---

### 3.3 書籍検索

#### 3.3.1 エンドポイント

```
GET /api/books/search?categoryId={id}&keyword={keyword}
```

#### 3.3.2 概要

カテゴリIDとキーワードで書籍を検索する。認証不要。

#### 3.3.3 リクエスト

**クエリパラメータ**:

| パラメータ | 型 | 必須 | 説明 |
|----------|---|------|------|
| categoryId | integer | - | カテゴリID（0または未指定=全カテゴリ） |
| keyword | string | - | キーワード（書籍名、著者名で部分一致検索） |

**検索パターン**:

| categoryId | keyword | 動作 |
|-----------|---------|------|
| 未指定/0 | 未指定 | 全書籍取得 |
| 未指定/0 | 指定 | キーワードで検索 |
| 指定 | 未指定 | カテゴリで検索 |
| 指定 | 指定 | カテゴリ+キーワードで検索 |

#### 3.3.4 レスポンス

**成功時 (200 OK)**:

```json
[
  {
    "bookId": 1,
    "bookName": "Java入門",
    "author": "山田太郎",
    "categoryId": 1,
    "publisherId": 1,
    "price": 3000,
    "category": {
      "categoryId": 1,
      "categoryName": "Java"
    },
    "publisher": {
      "publisherId": 1,
      "publisherName": "技術評論社"
    },
    "stock": {
      "bookId": 1,
      "quantity": 10,
      "version": 1
    }
  }
]
```

#### 3.3.5 ビジネスルール

| ルールID | 説明 |
|---------|-------------|
| BR-BOOK-001 | キーワード検索は書籍名、著者名で部分一致（LIKE '%keyword%'） |
| BR-BOOK-002 | カテゴリIDが0の場合は全カテゴリを対象 |
| BR-BOOK-003 | 検索結果には在庫情報（Stock）を含む |

---

### 3.4 カテゴリ一覧取得

#### 3.4.1 エンドポイント

```
GET /api/books/categories
```

#### 3.4.2 概要

全カテゴリをMapで取得する。認証不要。

#### 3.4.3 レスポンス

**成功時 (200 OK)**:

```json
{
  "Java": 1,
  "JavaScript": 2,
  "Python": 3,
  "データベース": 4,
  "AWS": 5,
  "生成AI": 6
}
```

---

## 4. データ構造

### 4.1 Bookエンティティ

| フィールド | 型 | 説明 |
|----------|---|------|
| bookId | integer | 書籍ID |
| bookName | string | 書籍名 |
| author | string | 著者 |
| categoryId | integer | カテゴリID |
| publisherId | integer | 出版社ID |
| price | integer | 価格 |
| category | Category | カテゴリ情報 |
| publisher | Publisher | 出版社情報 |
| stock | Stock | 在庫情報 |

### 4.2 Category

| フィールド | 型 | 説明 |
|----------|---|------|
| categoryId | integer | カテゴリID |
| categoryName | string | カテゴリ名 |

### 4.3 Publisher

| フィールド | 型 | 説明 |
|----------|---|------|
| publisherId | integer | 出版社ID |
| publisherName | string | 出版社名 |

### 4.4 Stock

| フィールド | 型 | 説明 |
|----------|---|------|
| bookId | integer | 書籍ID |
| quantity | integer | 在庫数 |
| version | long | バージョン番号（楽観的ロック用） |

---

## 5. エラーハンドリング

### 5.1 エラーメッセージ一覧

| エラーコード | HTTPステータス | メッセージ | 発生条件 |
|------------|--------------|----------|---------|
| BOOK-001 | 404 | 書籍が見つかりません | 指定IDの書籍が存在しない |

---

## 6. パフォーマンス最適化

### 6.1 N+1問題の回避

**問題**: 書籍一覧取得時に、カテゴリ、出版社、在庫を個別にSELECT（N+1問題）

**解決策**: JOIN FETCHを使用して一括取得

```java
@Query("SELECT b FROM Book b " +
       "JOIN FETCH b.category " +
       "JOIN FETCH b.publisher " +
       "LEFT JOIN FETCH b.stock " +
       "WHERE b.category.categoryId = :categoryId")
List<Book> findByCategoryId(@Param("categoryId") Integer categoryId);
```

---

## 7. 関連ドキュメント

- [behaviors.md](behaviors.md) - 書籍APIの受入基準
- [../../system/functional_design.md](../../system/functional_design.md) - 全体機能設計書
- [../../system/architecture_design.md](../../system/architecture_design.md) - アーキテクチャ設計書
- [../../system/data_model.md](../../system/data_model.md) - データモデル仕様書

