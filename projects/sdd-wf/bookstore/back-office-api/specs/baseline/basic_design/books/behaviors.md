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
      | bookId | bookName      | categoryId |
      | 1      | Java完全理解   | 1          |
      | 2      | Spring入門     | 1          |
      | 3      | 文学作品       | 2          |
    When BookService.searchBooks(categoryId=1, keyword=null)を呼び出す
    Then カテゴリID=1の書籍2件が返される

  Scenario: キーワードで書籍を検索
    Given DBに書籍が存在する:
      | bookId | bookName          | categoryId |
      | 1      | Java完全理解       | 1          |
      | 2      | JavaScript入門     | 1          |
    When BookService.searchBooks(categoryId=null, keyword="Java")を呼び出す
    Then "Java"を含む書籍2件が返される

  Scenario: 書籍詳細を取得（カテゴリ・出版社・在庫を含む）
    Given DBに書籍とリレーションデータが存在する:
      | bookId | bookName      | categoryId | publisherId |
      | 1      | Java完全理解   | 1          | 1           |
    And Category(id=1, name="プログラミング")が存在する
    And Publisher(id=1, name="技術評論社")が存在する
    And Stock(bookId=1, quantity=10)が存在する
    When BookService.getBookDetail(bookId=1)を呼び出す
    Then 書籍詳細が取得される:
      | bookName      | categoryName      | publisherName | quantity |
      | Java完全理解   | プログラミング      | 技術評論社     | 10       |
```

---

## 3. 参考資料

* [functional_design.md](functional_design.md) - 書籍管理機能設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書
