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
    When OrderService.createOrder(customerId=1, items=[{bookId: 1, quantity: 2}])を呼び出す
    Then DBに注文が作成される:
      | customerId | orderDate  |
      | 1          | 現在日付    |
    And DBに注文明細が作成される:
      | bookId | quantity |
      | 1      | 2        |
    And 外部APIで在庫が更新される

  Scenario: 在庫不足で注文失敗
    Given WireMockが在庫APIをスタブする:
      | Method | Path           | Response                   |
      | GET    | /api/stocks/1  | {quantity: 1, version: 1}  |
    When OrderService.createOrder(customerId=1, items=[{bookId: 1, quantity: 2}])を呼び出す
    Then InsufficientStockExceptionがスローされる
    And DBに注文は作成されない

  Scenario: 楽観的ロック競合で注文失敗
    Given WireMockが在庫APIをスタブする:
      | Method | Path           | Response                    |
      | GET    | /api/stocks/1  | {quantity: 10, version: 1}  |
      | PUT    | /api/stocks/1  | 409エラー（楽観的ロック競合） |
    When OrderService.createOrder(customerId=1, items=[{bookId: 1, quantity: 2}])を呼び出す
    Then OptimisticLockExceptionがスローされる
    And DBに注文は作成されない（ロールバック）
```

### 2.2 注文履歴取得

#### Feature: 注文履歴取得

```gherkin
Feature: 注文履歴取得
  顧客の注文履歴を取得する

  Scenario: 注文履歴を取得
    Given DBに注文が存在する:
      | orderId | customerId | orderDate  |
      | 1       | 1          | 2026-01-01 |
      | 2       | 1          | 2026-01-02 |
    And 注文明細が存在する:
      | orderItemId | orderId | bookId | quantity |
      | 1           | 1       | 1      | 2        |
      | 2           | 2       | 2      | 1        |
    When OrderService.getOrderHistory(customerId=1)を呼び出す
    Then 顧客ID=1の注文履歴が返される:
      | orderId | orderDate  | bookId | quantity |
      | 1       | 2026-01-01 | 1      | 2        |
      | 2       | 2026-01-02 | 2      | 1        |
```

---

## 3. 参考資料

* [functional_design.md](functional_design.md) - 注文管理機能設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書
* [../common/external_interface.md](../common/external_interface.md) - 外部インターフェース仕様書
