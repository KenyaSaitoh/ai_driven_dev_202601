@integration @common
Feature: 共通機能
  顧客認証とデータアクセス機能

  Background:
    Given テストデータベースが初期化されている
    And トランザクションが開始されている

  Scenario: 正しいパスワードで認証成功
    Given WireMockが顧客情報APIをスタブする:
      | customerId | email            | name        |
      | 1          | test@example.com | テストユーザー |
    When CustomerService.authenticate(customerId=1, password="password123")を呼び出す
    Then 認証が成功する
    And Customerオブジェクトが返される

  Scenario: パスワード不一致で認証失敗
    Given WireMockが顧客情報APIをスタブする
    When CustomerService.authenticate(customerId=1, password="wrongpassword")を呼び出す
    Then AuthenticationExceptionがスローされる

  Scenario: 注文履歴を取得
    Given DBに以下の注文が存在する:
      | ORDER_TRAN_ID | CUSTOMER_ID | ORDER_DATE | TOTAL_AMOUNT | STATUS    |
      | 1             | 1           | 2026-01-01 | 5000         | COMPLETED |
      | 2             | 1           | 2026-01-02 | 3000         | PENDING   |
    And DBに以下の注文明細が存在する:
      | ORDER_DETAIL_ID | ORDER_TRAN_ID | BOOK_ID | QUANTITY | PRICE |
      | 1               | 1             | 1       | 2        | 2500  |
      | 2               | 2             | 2       | 1        | 3000  |
    When OrderDao.findByCustomerId(customerId=1)を呼び出す
    Then 顧客ID=1の注文2件が返される:
      | ORDER_TRAN_ID | CUSTOMER_ID | ORDER_DATE |
      | 1             | 1           | 2026-01-01 |
      | 2             | 1           | 2026-01-02 |
    And DBの状態は変化しない

  Scenario: 注文詳細を取得
    Given DBに以下の注文が存在する:
      | ORDER_TRAN_ID | CUSTOMER_ID | ORDER_DATE | TOTAL_AMOUNT | STATUS    |
      | 1             | 1           | 2026-01-01 | 5000         | COMPLETED |
    And DBに以下の注文明細が存在する:
      | ORDER_DETAIL_ID | ORDER_TRAN_ID | BOOK_ID | QUANTITY | PRICE |
      | 1               | 1             | 1       | 2        | 2500  |
      | 2               | 1             | 2       | 1        | 2500  |
    When OrderDao.findById(orderId=1)を呼び出す
    Then 注文詳細が取得される:
      | orderId | customerId | totalAmount | orderItemCount |
      | 1       | 1          | 5000        | 2              |
    And 注文明細が2件含まれる
    And DBの状態は変化しない
