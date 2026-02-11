# ユースケース: 書籍 - 振る舞い仕様書

ユースケースID: books  
バージョン: 1.0.0  
最終更新日: 2026-01-31

記法: Gherkin。

---

## 1. 概要

書籍一覧・詳細・検索の振る舞い。

---

## 2. テストシナリオ（Gherkin）

### Feature: 書籍一覧・詳細

```gherkin
Scenario: 書籍一覧を取得する
  Given DBに以下の書籍が存在する:
    テーブル: BOOK
    件数: 3件
    データセット: /datasets/books/initial-books.xml
    データ:
      | BOOK_ID | TITLE      | PRICE | STOCK | CATEGORY_ID | PUBLISHER_ID |
      | 1       | Java入門   | 3000  | 10    | 1           | 1            |
      | 2       | Python実践 | 2500  | 5     | 1           | 2            |
      | 3       | Web開発   | 4000  | 8     | 2           | 1            |
  
  When GET /api/books を送る
  
  Then レスポンスは 200 OK
  And 書籍配列が返る（在庫・カテゴリ・出版社含む）:
    件数: 3件
  
  And DBの状態は変化しない:
    テーブル: BOOK
    件数: 3件（変更なし）
    検証:
      - READ操作のため、DBは更新されない

Scenario: 書籍詳細を取得する
  Given DBに書籍ID=1が存在する:
    テーブル: BOOK
    件数: 1件
    データセット: /datasets/books/initial-book-detail.xml
    データ:
      | BOOK_ID | TITLE    | PRICE | STOCK | CATEGORY_ID | PUBLISHER_ID |
      | 1       | Java入門 | 3000  | 10    | 1           | 1            |
  
  When GET /api/books/1 を送る
  
  Then レスポンスは 200 OK
  And 書籍詳細が返る:
    | bookId | title    | price | stock |
    | 1      | Java入門 | 3000  | 10    |
  
  And DBの状態は変化しない:
    テーブル: BOOK
    件数: 1件（変更なし）

Scenario: 存在しない書籍IDで詳細を取得する
  Given DBに書籍が存在する:
    テーブル: BOOK
    件数: 1件
    データ:
      | BOOK_ID | TITLE    | PRICE |
      | 1       | Java入門 | 3000  |
  
  When GET /api/books/999 を送る
  
  Then レスポンスは 404 Not Found
  
  And DBの状態は変化しない:
    テーブル: BOOK
    件数: 1件（変更なし）
```

### Feature: 書籍検索

```gherkin
Scenario: カテゴリIDで検索する（JPQL）
  Given DBにカテゴリID=1の書籍が存在する:
    テーブル: BOOK
    件数: 3件
    データセット: /datasets/books/initial-books-by-category.xml
    データ:
      | BOOK_ID | TITLE      | PRICE | CATEGORY_ID |
      | 1       | Java入門   | 3000  | 1           |
      | 2       | Python実践 | 2500  | 1           |
      | 3       | Web開発   | 4000  | 2           |
  
  When GET /api/books/search/jpql?categoryId=1 を送る
  
  Then レスポンスは 200 OK
  And 該当書籍配列が返る:
    件数: 2件
    データ:
      | bookId | title      | categoryId |
      | 1      | Java入門   | 1          |
      | 2      | Python実践 | 1          |
  
  And DBの状態は変化しない:
    テーブル: BOOK
    件数: 3件（変更なし）
    検証:
      - READ操作のため、DBは更新されない
```

---

## 3. DBUnitデータセット対応表

| シナリオ | 初期データセット | 期待データセット | 検証テーブル |
|---------|----------------|----------------|------------|
| 書籍一覧を取得する | `/datasets/books/initial-books.xml` | （変更なし） | BOOK |
| 書籍詳細を取得する | `/datasets/books/initial-book-detail.xml` | （変更なし） | BOOK |
| 存在しない書籍IDで詳細を取得する | `/datasets/books/initial-books.xml` | （変更なし） | BOOK |
| カテゴリIDで検索する | `/datasets/books/initial-books-by-category.xml` | （変更なし） | BOOK |

---

## 4. 参照

* [userstory.md](userstory.md)
* [../../common/data_model.md](../../common/data_model.md)
* DBUnit公式ドキュメント: http://dbunit.sourceforge.net/
