# language: ja
@e2e
Feature: 書籍API - E2Eテスト
  berry-books-api の書籍情報取得機能のE2Eテスト

  Background: 
    Given E2Eテスト環境が起動している
    And berry-books-apiがデプロイされている

  Scenario: 書籍一覧取得 - 正常系
    When 書籍一覧取得APIを "/api/books" で呼び出す
    Then ステータスコードは 200 である
    And レスポンスに書籍一覧が含まれる
    And 各書籍にはカテゴリ、出版社、在庫情報が含まれる

  Scenario: 書籍詳細取得 - 正常系
    When 書籍詳細取得APIを "/api/books/1" で呼び出す
    Then ステータスコードは 200 である
    And レスポンスに書籍IDが "1" の情報が含まれる

  Scenario: 書籍詳細取得 - 存在しないID
    When 書籍詳細取得APIを "/api/books/999" で呼び出す
    Then ステータスコードは 404 である
    And エラーメッセージに "書籍が見つかりません" が含まれる

  Scenario: 書籍検索 - カテゴリIDで検索
    When 書籍検索APIを "/api/books/search" でカテゴリID "1" で呼び出す
    Then ステータスコードは 200 である
    And レスポンスに検索結果が含まれる

  Scenario: 書籍検索 - キーワードで検索
    When 書籍検索APIを "/api/books/search" でキーワード "Java" で呼び出す
    Then ステータスコードは 200 である
    And レスポンスに検索結果が含まれる

  Scenario: 書籍検索 - 検索結果が0件
    When 書籍検索APIを "/api/books/search" でキーワード "存在しないキーワードXYZ123" で呼び出す
    Then ステータスコードは 200 である
    And レスポンスは空の配列である

  Scenario: カテゴリ一覧取得 - 正常系
    When カテゴリ一覧取得APIを "/api/books/categories" で呼び出す
    Then ステータスコードは 200 である
    And レスポンスにカテゴリMapが含まれる
