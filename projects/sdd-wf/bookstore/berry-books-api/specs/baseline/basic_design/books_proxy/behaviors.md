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
      | Method | Path       | Response                                       |
      | GET    | /api/books | [{"bookId": 1, "bookName": "Java完全理解"}] |
    When BooksProxyService.getAllBooks()を呼び出す
    Then 外部APIが呼ばれる
    And 書籍一覧が返される

  Scenario: 外部API呼び出し失敗
    Given WireMockが以下をスタブする:
      | Method | Path       | Response      |
      | GET    | /api/books | 500エラー      |
    When BooksProxyService.getAllBooks()を呼び出す
    Then ExternalApiExceptionがスローされる
```

---

## 3. 参考資料

* [functional_design.md](functional_design.md) - 書籍API連携機能設計書
* [../common/external_interface.md](../common/external_interface.md) - 外部インターフェース仕様書
