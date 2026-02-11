@integration @orders
Feature: 注文管理
  在庫を確認して注文を作成し、注文履歴を取得する

  Background:
    Given テストデータベースが初期化されている
    And トランザクションが開始されている

  Scenario: 注文作成（正常系）
    Given WireMockが在庫APIをスタブする:
      | Method | Path           | Response                    |
      | GET    | /api/stocks/1  | {quantity: 10, version: 1}  |
      | PUT    | /api/stocks/1  | {quantity: 8, version: 2}   |
    And DBに以下の顧客が存在する:
      | CUSTOMER_ID | EMAIL            | NAME        |
      | 1           | test@example.com | テストユーザー |
    And DBに以下の書籍が存在する:
      | BOOK_ID | TITLE      | PRICE |
      | 1       | Java入門   | 3000  |
    And DBの注文テーブルは空である
    And DBの注文明細テーブルは空である
    When OrderService.createOrder(customerId=1, items=[{bookId: 1, quantity: 2}])を呼び出す
    Then DBの注文テーブルに1件追加される:
      | ORDER_TRAN_ID | CUSTOMER_ID | TOTAL_AMOUNT | STATUS    |
      | 1             | 1           | 6000         | COMPLETED |
    And DBの注文明細テーブルに1件追加される:
      | ORDER_DETAIL_ID | ORDER_TRAN_ID | BOOK_ID | QUANTITY | PRICE |
      | 1               | 1             | 1       | 2        | 3000  |
    And 外部APIで在庫が更新される

  Scenario: 在庫不足で注文失敗
    Given WireMockが在庫APIをスタブする:
      | Method | Path           | Response                   |
      | GET    | /api/stocks/1  | {quantity: 1, version: 1}  |
    And DBに顧客と書籍が存在する
    And DBの注文テーブルは空である
    When OrderService.createOrder(customerId=1, items=[{bookId: 1, quantity: 2}])を呼び出す
    Then InsufficientStockExceptionがスローされる
    And DBの注文テーブルは変化しない
    And DBの注文明細テーブルは変化しない

  Scenario: 楽観的ロック競合で注文失敗
    Given WireMockが在庫APIをスタブする:
      | Method | Path           | Response                    |
      | GET    | /api/stocks/1  | {quantity: 10, version: 1}  |
      | PUT    | /api/stocks/1  | 409エラー（楽観的ロック競合） |
    And DBに顧客と書籍が存在する
    And DBの注文テーブルは空である
    When OrderService.createOrder(customerId=1, items=[{bookId: 1, quantity: 2}])を呼び出す
    Then OptimisticLockExceptionがスローされる
    And DBの注文テーブルは変化しない
    And DBの注文明細テーブルは変化しない

  Scenario: 注文履歴を取得
    Given DBに以下の注文が存在する:
      | ORDER_TRAN_ID | CUSTOMER_ID | ORDER_DATE | TOTAL_AMOUNT | STATUS    |
      | 1             | 1           | 2026-01-01 | 5000         | COMPLETED |
      | 2             | 1           | 2026-01-02 | 3000         | PENDING   |
    And DBに以下の注文明細が存在する:
      | ORDER_DETAIL_ID | ORDER_TRAN_ID | BOOK_ID | QUANTITY | PRICE |
      | 1               | 1             | 1       | 2        | 2500  |
      | 2               | 2             | 2       | 1        | 3000  |
    When OrderService.getOrderHistory(customerId=1)を呼び出す
    Then 顧客ID=1の注文履歴が2件返される:
      | orderId | orderDate  | totalAmount | status    |
      | 1       | 2026-01-01 | 5000        | COMPLETED |
      | 2       | 2026-01-02 | 3000        | PENDING   |
    And DBの状態は変化しない
