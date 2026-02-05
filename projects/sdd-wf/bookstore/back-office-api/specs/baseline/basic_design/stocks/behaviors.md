# 在庫管理ドメイン - 結合テスト仕様書

プロジェクトID: back-office-api  
ドメイン: stocks  
バージョン: 1.0.0  
最終更新日: 2026-02-05

---

## 1. 概要

本文書は、在庫管理ドメインのService層以下（Service + DAO + Entity + DB）の結合テスト仕様を記述する。

---

## 2. StockService のシナリオ

### 2.1 在庫管理

#### Feature: 在庫数を更新

```gherkin
Feature: 在庫更新
  在庫数を更新する（楽観的ロック対応）

  Scenario: 在庫数を更新（正常系）
    Given DBに在庫が存在する:
      | bookId | quantity | version |
      | 1      | 10       | 1       |
    When StockService.updateStock(bookId=1, quantity=15, version=1)を呼び出す
    Then DBの在庫が更新される:
      | bookId | quantity | version |
      | 1      | 15       | 2       |

  Scenario: 楽観的ロック競合検知
    Given DBに在庫が存在する:
      | bookId | quantity | version |
      | 1      | 10       | 2       |
    When StockService.updateStock(bookId=1, quantity=15, version=1)を呼び出す（古いバージョン）
    Then OptimisticLockExceptionがスローされる
    And DBの在庫は更新されない

  Scenario: 外部在庫API連携（WireMockスタブ）
    Given WireMockが以下をスタブする:
      | Method | Path           | Response                        |
      | PUT    | /api/stocks/1  | {quantity: 15, version: 2}      |
    When StockService.syncWithExternalStock(bookId=1)を呼び出す
    Then 外部APIが呼ばれる
    And レスポンスに基づきDBが更新される
```

---

## 3. 参考資料

* [functional_design.md](functional_design.md) - 在庫管理機能設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書
