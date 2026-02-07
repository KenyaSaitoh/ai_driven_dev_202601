# language: ja
@e2e
Feature: 認証API - E2Eテスト
  berry-books-api の認証機能のE2Eテスト

  Background: 
    Given E2Eテスト環境が起動している
    And berry-books-apiがデプロイされている

  Scenario: ログイン - 正常系
    Given 顧客 "alice@gmail.com" がパスワード "password" で登録されている
    When ログインAPIを "/api/auth/login" で呼び出す
      | email    | alice@gmail.com |
      | password | password        |
    Then ステータスコードは 200 である
    And JWT Cookieが発行される
    And レスポンスに顧客情報が含まれる

  Scenario: ログイン - メールアドレスが存在しない
    When ログインAPIを "/api/auth/login" で呼び出す
      | email    | notfound@example.com |
      | password | password             |
    Then ステータスコードは 401 である
    And エラーメッセージに "メールアドレスまたはパスワードが正しくありません" が含まれる

  Scenario: ログイン - パスワードが一致しない
    Given 顧客 "alice@gmail.com" がパスワード "password" で登録されている
    When ログインAPIを "/api/auth/login" で呼び出す
      | email    | alice@gmail.com |
      | password | wrongpassword   |
    Then ステータスコードは 401 である
    And エラーメッセージに "メールアドレスまたはパスワードが正しくありません" が含まれる

  Scenario: ログアウト - 正常系
    Given 顧客 "alice@gmail.com" でログイン済み
    When ログアウトAPIを "/api/auth/logout" で呼び出す
    Then ステータスコードは 200 である
    And JWT Cookieが削除される

  Scenario: 新規登録 - 正常系
    When 新規登録APIを "/api/auth/register" で呼び出す
      | email        | e2e.cucumber@example.com |
      | password     | password                 |
      | customerName | Cucumber テストユーザー   |
      | address      | 東京都渋谷区1-2-3         |
    Then ステータスコードは 200 である
    And JWT Cookieが発行される
    And レスポンスに顧客情報が含まれる

  Scenario: 新規登録 - メールアドレスが既に存在
    Given 顧客 "alice@gmail.com" が既に登録されている
    When 新規登録APIを "/api/auth/register" で呼び出す
      | email        | alice@gmail.com   |
      | password     | password          |
      | customerName | Duplicate User    |
      | address      | 東京都新宿区1-1-1 |
    Then ステータスコードは 409 である
    And エラーメッセージに "既に登録されています" が含まれる

  Scenario: 現在のユーザー情報取得 - 正常系
    Given 顧客 "alice@gmail.com" でログイン済み
    When ユーザー情報取得APIを "/api/auth/me" で呼び出す
    Then ステータスコードは 200 である
    And レスポンスに顧客情報が含まれる
    And レスポンスのメールアドレスは "alice@gmail.com" である

  Scenario: 現在のユーザー情報取得 - 未認証
    When ユーザー情報取得APIを "/api/auth/me" で呼び出す（認証なし）
    Then ステータスコードは 401 である
    And エラーメッセージに "認証が必要です" が含まれる
