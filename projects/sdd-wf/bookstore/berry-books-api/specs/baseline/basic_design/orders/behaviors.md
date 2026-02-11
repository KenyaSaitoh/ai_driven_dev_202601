# 注文管理ドメイン - 結合テスト仕様書

プロジェクトID: berry-books-api  
ドメイン: orders  
バージョン: 1.0.0  
最終更新日: 2026-02-05

---

## 1. 概要

本文書は、注文管理ドメインのService層以下（Service + DAO + Entity + DB + 外部API）の結合テスト仕様を記述する。

---

## 2. OrderService のシナリオ

### 2.1 注文作成

#### Feature: 注文作成（在庫確認・更新含む）

```gherkin
Feature: 注文作成
  在庫を確認して注文を作成する

  Scenario: 注文作成（正常系）
    Given WireMockが在庫APIをスタブする:
      | Method | Path           | Response                    |
      | GET    | /api/stocks/1  | {quantity: 10, version: 1}  |
      | PUT    | /api/stocks/1  | {quantity: 8, version: 2}   |
    
    And DBに以下の顧客が存在する:
      テーブル: CUSTOMER
      件数: 1件
      データセット: /datasets/orders/initial-customer.xml
      データ:
        | CUSTOMER_ID | EMAIL            | NAME        |
        | 1           | test@example.com | テストユーザー |
    
    And DBに以下の書籍が存在する:
      テーブル: BOOK
      件数: 1件
      データセット: /datasets/orders/initial-book.xml
      データ:
        | BOOK_ID | TITLE      | PRICE |
        | 1       | Java入門   | 3000  |
    
    And DBの注文テーブルは空である:
      テーブル: ORDER_TRAN
      件数: 0件
    
    And DBの注文明細テーブルは空である:
      テーブル: ORDER_DETAIL
      件数: 0件
    
    When OrderService.createOrder(customerId=1, items=[{bookId: 1, quantity: 2}])を呼び出す
    
    Then DBの注文テーブルは以下になる:
      テーブル: ORDER_TRAN
      件数: 1件（+1件追加）
      データセット: /datasets/orders/expected-order-created.xml
      データ:
        | ORDER_TRAN_ID | CUSTOMER_ID | ORDER_DATE | TOTAL_AMOUNT | STATUS    |
        | 1             | 1           | 現在日付    | 6000         | COMPLETED |
      検証:
        - ORDER_TRAN_ID は自動採番される（NOT NULL）
        - CUSTOMER_ID は外部キー制約を満たす
        - ORDER_DATE は現在日時
        - TOTAL_AMOUNT は 3000 * 2 = 6000
    
    And DBの注文明細テーブルは以下になる:
      テーブル: ORDER_DETAIL
      件数: 1件（+1件追加）
      データセット: /datasets/orders/expected-order-detail-created.xml
      データ:
        | ORDER_DETAIL_ID | ORDER_TRAN_ID | BOOK_ID | QUANTITY | PRICE |
        | 1               | 1             | 1       | 2        | 3000  |
      検証:
        - ORDER_DETAIL_ID は自動採番される（NOT NULL）
        - ORDER_TRAN_ID は ORDER_TRAN.ORDER_TRAN_ID を参照
        - BOOK_ID は外部キー制約を満たす
        - QUANTITY は注文数量と一致
    
    And 外部APIで在庫が更新される:
      リクエスト: PUT /api/stocks/1
      ボディ: {quantity: 8, version: 2}

  Scenario: 在庫不足で注文失敗
    Given WireMockが在庫APIをスタブする:
      | Method | Path           | Response                   |
      | GET    | /api/stocks/1  | {quantity: 1, version: 1}  |
    
    And DBに顧客と書籍が存在する:
      データセット: /datasets/orders/initial-customer-book.xml
    
    And DBの注文テーブルは空である:
      テーブル: ORDER_TRAN
      件数: 0件
    
    When OrderService.createOrder(customerId=1, items=[{bookId: 1, quantity: 2}])を呼び出す
    
    Then InsufficientStockExceptionがスローされる
    
    And DBの注文テーブルは変化しない:
      テーブル: ORDER_TRAN
      件数: 0件（変更なし）
      検証:
        - 注文は1件も作成されない（ロールバック）
    
    And DBの注文明細テーブルは変化しない:
      テーブル: ORDER_DETAIL
      件数: 0件（変更なし）

  Scenario: 楽観的ロック競合で注文失敗
    Given WireMockが在庫APIをスタブする:
      | Method | Path           | Response                    |
      | GET    | /api/stocks/1  | {quantity: 10, version: 1}  |
      | PUT    | /api/stocks/1  | 409エラー（楽観的ロック競合） |
    
    And DBに顧客と書籍が存在する:
      データセット: /datasets/orders/initial-customer-book.xml
    
    And DBの注文テーブルは空である:
      テーブル: ORDER_TRAN
      件数: 0件
    
    When OrderService.createOrder(customerId=1, items=[{bookId: 1, quantity: 2}])を呼び出す
    
    Then OptimisticLockExceptionがスローされる
    
    And DBの注文テーブルは変化しない:
      テーブル: ORDER_TRAN
      件数: 0件（ロールバック）
      検証:
        - トランザクションがロールバックされる
        - 注文は1件も作成されない
    
    And DBの注文明細テーブルは変化しない:
      テーブル: ORDER_DETAIL
      件数: 0件（ロールバック）
```

### 2.2 注文履歴取得

#### Feature: 注文履歴取得

```gherkin
Feature: 注文履歴取得
  顧客の注文履歴を取得する

  Scenario: 注文履歴を取得
    Given DBに以下の注文が存在する:
      テーブル: ORDER_TRAN
      件数: 2件
      データセット: /datasets/orders/initial-order-history.xml
      データ:
        | ORDER_TRAN_ID | CUSTOMER_ID | ORDER_DATE | TOTAL_AMOUNT | STATUS    |
        | 1             | 1           | 2026-01-01 | 5000         | COMPLETED |
        | 2             | 1           | 2026-01-02 | 3000         | PENDING   |
    
    And DBに以下の注文明細が存在する:
      テーブル: ORDER_DETAIL
      件数: 2件
      データ:
        | ORDER_DETAIL_ID | ORDER_TRAN_ID | BOOK_ID | QUANTITY | PRICE |
        | 1               | 1             | 1       | 2        | 2500  |
        | 2               | 2             | 2       | 1        | 3000  |
    
    When OrderService.getOrderHistory(customerId=1)を呼び出す
    
    Then 顧客ID=1の注文履歴が返される:
      件数: 2件
      データ:
        | orderId | orderDate  | totalAmount | status    | bookId | quantity |
        | 1       | 2026-01-01 | 5000        | COMPLETED | 1      | 2        |
        | 2       | 2026-01-02 | 3000        | PENDING   | 2      | 1        |
    
    And DBの状態は変化しない:
      テーブル: ORDER_TRAN, ORDER_DETAIL
      件数: 2件（変更なし）
      検証:
        - READ操作のため、DBは更新されない
```

---

## 3. DBUnitデータセット対応表

| シナリオ | 初期データセット | 期待データセット | 検証テーブル |
|---------|----------------|----------------|------------|
| 注文作成（正常系） | `/datasets/orders/initial-customer.xml`<br>`/datasets/orders/initial-book.xml` | `/datasets/orders/expected-order-created.xml`<br>`/datasets/orders/expected-order-detail-created.xml` | ORDER_TRAN<br>ORDER_DETAIL |
| 在庫不足で注文失敗 | `/datasets/orders/initial-customer-book.xml` | （変更なし） | ORDER_TRAN<br>ORDER_DETAIL |
| 楽観的ロック競合 | `/datasets/orders/initial-customer-book.xml` | （ロールバック） | ORDER_TRAN<br>ORDER_DETAIL |
| 注文履歴取得 | `/datasets/orders/initial-order-history.xml` | （変更なし） | ORDER_TRAN<br>ORDER_DETAIL |

---

## 4. 参考資料

* [functional_design.md](functional_design.md) - 注文管理機能設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書
* [../common/external_interface.md](../common/external_interface.md) - 外部インターフェース仕様書
* DBUnit公式ドキュメント: http://dbunit.sourceforge.net/
