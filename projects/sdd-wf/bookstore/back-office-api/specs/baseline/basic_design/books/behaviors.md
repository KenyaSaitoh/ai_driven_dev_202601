# 書籍管理ドメイン - 結合テスト仕様書

プロジェクトID: back-office-api  
ドメイン: books  
バージョン: 1.0.0  
最終更新日: 2026-02-05

---

## 1. 概要

本文書は、書籍管理ドメインのService層以下（Service + DAO + Entity + DB）の結合テスト仕様を記述する。

---

## 2. BookService のシナリオ

### 2.1 書籍検索

#### Feature: カテゴリで書籍を検索

```gherkin
Feature: 書籍検索
  カテゴリとキーワードで書籍を検索する

  Scenario: カテゴリで書籍を検索
    Given DBに以下の書籍が存在する:
      テーブル: BOOK
      件数: 3件
      データセット: /datasets/books/initial-books-by-category.xml
      データ:
        | BOOK_ID | BOOK_NAME     | CATEGORY_ID | PRICE |
        | 1       | Java完全理解  | 1           | 3000  |
        | 2       | Spring入門    | 1           | 2500  |
        | 3       | 文学作品      | 2           | 2000  |
    
    When BookService.searchBooks(categoryId=1, keyword=null)を呼び出す
    
    Then カテゴリID=1の書籍2件が返される:
      データ:
        | bookId | bookName     | categoryId |
        | 1      | Java完全理解 | 1          |
        | 2      | Spring入門   | 1          |
    
    And DBの状態は変化しない:
      テーブル: BOOK
      件数: 3件（変更なし）
      検証:
        - READ操作のため、DBは更新されない

  Scenario: キーワードで書籍を検索
    Given DBに以下の書籍が存在する:
      テーブル: BOOK
      件数: 2件
      データセット: /datasets/books/initial-books-for-keyword-search.xml
      データ:
        | BOOK_ID | BOOK_NAME        | CATEGORY_ID |
        | 1       | Java完全理解     | 1           |
        | 2       | JavaScript入門   | 1           |
    
    When BookService.searchBooks(categoryId=null, keyword="Java")を呼び出す
    
    Then "Java"を含む書籍2件が返される:
      データ:
        | bookId | bookName        |
        | 1      | Java完全理解    |
        | 2      | JavaScript入門  |
    
    And DBの状態は変化しない:
      テーブル: BOOK
      件数: 2件（変更なし）

  Scenario: 書籍詳細を取得（カテゴリ・出版社・在庫を含む）
    Given DBに以下の書籍が存在する:
      テーブル: BOOK
      件数: 1件
      データセット: /datasets/books/initial-book-detail.xml
      データ:
        | BOOK_ID | BOOK_NAME    | CATEGORY_ID | PUBLISHER_ID | PRICE |
        | 1       | Java完全理解 | 1           | 1            | 3000  |
    
    And DBに以下のカテゴリが存在する:
      テーブル: CATEGORY
      件数: 1件
      データ:
        | CATEGORY_ID | CATEGORY_NAME    |
        | 1           | プログラミング   |
    
    And DBに以下の出版社が存在する:
      テーブル: PUBLISHER
      件数: 1件
      データ:
        | PUBLISHER_ID | PUBLISHER_NAME |
        | 1            | 技術評論社     |
    
    And DBに以下の在庫が存在する:
      テーブル: STOCK
      件数: 1件
      データ:
        | BOOK_ID | QUANTITY | VERSION |
        | 1       | 10       | 1       |
    
    When BookService.getBookDetail(bookId=1)を呼び出す
    
    Then 書籍詳細が取得される:
      データ:
        | bookName     | categoryName   | publisherName | quantity |
        | Java完全理解 | プログラミング | 技術評論社    | 10       |
      検証:
        - BOOK, CATEGORY, PUBLISHER, STOCK のリレーションが正しく取得される
    
    And DBの状態は変化しない:
      テーブル: BOOK, CATEGORY, PUBLISHER, STOCK
      検証:
        - READ操作のため、DBは更新されない
```

---

## 3. DBUnitデータセット対応表

| シナリオ | 初期データセット | 期待データセット | 検証テーブル |
|---------|----------------|----------------|------------|
| カテゴリで書籍を検索 | `/datasets/books/initial-books-by-category.xml` | （変更なし） | BOOK |
| キーワードで書籍を検索 | `/datasets/books/initial-books-for-keyword-search.xml` | （変更なし） | BOOK |
| 書籍詳細を取得（リレーション含む） | `/datasets/books/initial-book-detail.xml` | （変更なし） | BOOK<br>CATEGORY<br>PUBLISHER<br>STOCK |

---

## 4. 参考資料

* [functional_design.md](functional_design.md) - 書籍管理機能設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書
* DBUnit公式ドキュメント: http://dbunit.sourceforge.net/
