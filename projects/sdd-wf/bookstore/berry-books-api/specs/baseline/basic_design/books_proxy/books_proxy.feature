@integration @books_proxy
Feature: 書籍API連携
  外部API（back-office-api）から書籍情報を取得する

  Scenario: 書籍一覧を取得（WireMockスタブ）
    Given WireMockが以下をスタブする:
      | Method | Path       | Response                                              |
      | GET    | /api/books | [{"bookId": 1, "bookName": "Java完全理解", "price": 3000}] |
    And このAPIではDBに書籍データを保持しない
    When BooksProxyService.getAllBooks()を呼び出す
    Then 外部APIが呼ばれる
    And 書籍一覧が返される:
      | bookId | bookName     | price |
      | 1      | Java完全理解 | 3000  |
    And DBの状態は変化しない

  Scenario: 外部API呼び出し失敗
    Given WireMockが以下をスタブする:
      | Method | Path       | Response |
      | GET    | /api/books | 500エラー |
    When BooksProxyService.getAllBooks()を呼び出す
    Then ExternalApiExceptionがスローされる
    And DBの状態は変化しない
