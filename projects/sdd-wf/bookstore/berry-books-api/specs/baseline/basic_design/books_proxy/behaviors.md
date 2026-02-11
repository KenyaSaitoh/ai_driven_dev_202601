# 書籍API連携ドメイン - 結合テスト仕様書

プロジェクトID: berry-books-api  
ドメイン: books_proxy  
バージョン: 1.0.0  
最終更新日: 2026-02-05

---

## 1. 概要

本文書は、書籍API連携ドメインのService層以下（外部API呼び出し）の結合テスト仕様を記述する。

---

## 2. BooksProxyService のシナリオ

### 2.1 書籍API連携

#### Feature: 外部APIから書籍一覧を取得

```gherkin
Feature: 書籍一覧取得
  外部API（back-office-api）から書籍一覧を取得する

  Scenario: 書籍一覧を取得（WireMockスタブ）
    Given WireMockが以下をスタブする:
      | Method | Path       | Response                                              |
      | GET    | /api/books | [{"bookId": 1, "bookName": "Java完全理解", "price": 3000}] |
    
    And このAPIではDBに書籍データを保持しない:
      検証:
        - 書籍データは back-office-api（外部API）が管理
        - berry-books-api のDBには書籍テーブルは存在しない
    
    When BooksProxyService.getAllBooks()を呼び出す
    
    Then 外部APIが呼ばれる:
      リクエスト: GET /api/books
    
    And 書籍一覧が返される:
      件数: 1件
      データ:
        | bookId | bookName     | price |
        | 1      | Java完全理解 | 3000  |
    
    And DBの状態は変化しない:
      検証:
        - 外部API連携のみで、DB操作は行われない

  Scenario: 外部API呼び出し失敗
    Given WireMockが以下をスタブする:
      | Method | Path       | Response |
      | GET    | /api/books | 500エラー |
    
    When BooksProxyService.getAllBooks()を呼び出す
    
    Then ExternalApiExceptionがスローされる
    
    And DBの状態は変化しない:
      検証:
        - 外部APIエラーのため、DB操作は行われない
```

---

## 3. DBUnitデータセット対応表

| シナリオ | 初期データセット | 期待データセット | 検証テーブル | 備考 |
|---------|----------------|----------------|------------|------|
| 書籍一覧を取得（WireMockスタブ） | （なし） | （なし） | （なし） | 外部API連携のみ、DB操作なし |
| 外部API呼び出し失敗 | （なし） | （なし） | （なし） | 外部API連携のみ、DB操作なし |

**注意:**
* このドメインは外部API（back-office-api）連携がメインであり、berry-books-api のDBは操作しない
* 書籍データは back-office-api が管理
* テストは WireMock によるスタブ化が中心
* DBUnitは使用しない（DB操作がないため）

---

## 4. 参考資料

* [functional_design.md](functional_design.md) - 書籍API連携機能設計書
* [../common/external_interface.md](../common/external_interface.md) - 外部インターフェース仕様書
* WireMock公式ドキュメント: https://wiremock.org/
