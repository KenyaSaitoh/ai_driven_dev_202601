# language: ja
@e2e
Feature: 注文API - E2Eテスト
  berry-books-api の注文機能のE2Eテスト

  Background: 
    Given E2Eテスト環境が起動している
    And berry-books-apiがデプロイされている

  Scenario: 注文作成 - 正常系
    Given 顧客 "alice@gmail.com" でログイン済み
    And 書籍ID "1" の在庫が十分にある
    When 注文作成APIを "/api/orders" で呼び出す
      | bookId   | 1 |
      | quantity | 1 |
    Then ステータスコードは 200 である
    And レスポンスに注文IDが含まれる
    And レスポンスに注文明細が含まれる
    And 在庫が減算される

  Scenario: 注文作成 - 複数書籍
    Given 顧客 "alice@gmail.com" でログイン済み
    And 書籍ID "1" と "2" の在庫が十分にある
    When 注文作成APIを "/api/orders" で複数書籍を呼び出す
      | bookId | quantity |
      | 1      | 1        |
      | 2      | 1        |
    Then ステータスコードは 200 である
    And レスポンスに "2" 件の注文明細が含まれる

  Scenario: 注文作成 - 未認証
    When 注文作成APIを "/api/orders" で呼び出す（認証なし）
      | bookId   | 1 |
      | quantity | 1 |
    Then ステータスコードは 401 である
    And エラーメッセージに "認証が必要です" が含まれる

  Scenario: 注文作成 - 在庫不足
    Given 顧客 "alice@gmail.com" でログイン済み
    And 書籍ID "1" の在庫数を取得する
    When 注文作成APIを "/api/orders" で在庫を超える数量で呼び出す
    Then ステータスコードは 409 である
    And エラーメッセージに "在庫が不足しています" が含まれる

  Scenario: 注文履歴取得 - 正常系
    Given 顧客 "alice@gmail.com" でログイン済み
    And 顧客の注文履歴が存在する
    When 注文履歴取得APIを "/api/orders/history" で呼び出す
    Then ステータスコードは 200 である
    And レスポンスに注文履歴が含まれる

  Scenario: 注文履歴取得 - 未認証
    When 注文履歴取得APIを "/api/orders/history" で呼び出す（認証なし）
    Then ステータスコードは 401 である
    And エラーメッセージに "認証が必要です" が含まれる

  Scenario: 注文詳細取得 - 正常系
    Given 注文ID "1" が存在する
    When 注文詳細取得APIを "/api/orders/1" で呼び出す
    Then ステータスコードは 200 である
    And レスポンスに注文IDが "1" の詳細が含まれる
    And レスポンスに注文明細リストが含まれる

  Scenario: 注文詳細取得 - 存在しないID
    When 注文詳細取得APIを "/api/orders/999999" で呼び出す
    Then ステータスコードは 404 である
    And エラーメッセージに "注文が見つかりません" が含まれる
