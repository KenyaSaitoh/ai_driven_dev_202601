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
    Given DBに以下の在庫が存在する:
      テーブル: STOCK
      件数: 1件
      データセット: /datasets/stocks/initial-stock-before-update.xml
      データ:
        | BOOK_ID | QUANTITY | VERSION |
        | 1       | 10       | 1       |
    
    When StockService.updateStock(bookId=1, quantity=15, version=1)を呼び出す
    
    Then DBの在庫テーブルは以下になる:
      テーブル: STOCK
      件数: 1件（変更なし）
      データセット: /datasets/stocks/expected-stock-updated.xml
      データ:
        | BOOK_ID | QUANTITY | VERSION |
        | 1       | 15       | 2       |
      検証:
        - QUANTITY が 10 から 15 に更新される
        - VERSION が 1 から 2 にインクリメントされる
        - 楽観的ロックが正常に動作する

  Scenario: 楽観的ロック競合検知
    Given DBに以下の在庫が存在する:
      テーブル: STOCK
      件数: 1件
      データセット: /datasets/stocks/initial-stock-version-conflict.xml
      データ:
        | BOOK_ID | QUANTITY | VERSION |
        | 1       | 10       | 2       |
    
    When StockService.updateStock(bookId=1, quantity=15, version=1)を呼び出す（古いバージョン）
    
    Then OptimisticLockExceptionがスローされる
    
    And DBの在庫テーブルは変化しない:
      テーブル: STOCK
      件数: 1件（変更なし）
      データ:
        | BOOK_ID | QUANTITY | VERSION |
        | 1       | 10       | 2       |
      検証:
        - 在庫は更新されない
        - VERSION は 2 のまま変化しない
        - 楽観的ロック競合が検知される

  Scenario: 外部在庫API連携（WireMockスタブ）
    Given DBに以下の在庫が存在する:
      テーブル: STOCK
      件数: 1件
      データセット: /datasets/stocks/initial-stock-before-sync.xml
      データ:
        | BOOK_ID | QUANTITY | VERSION |
        | 1       | 10       | 1       |
    
    And WireMockが以下をスタブする:
      | Method | Path           | Response                   |
      | PUT    | /api/stocks/1  | {quantity: 15, version: 2} |
    
    When StockService.syncWithExternalStock(bookId=1)を呼び出す
    
    Then 外部APIが呼ばれる:
      リクエスト: PUT /api/stocks/1
    
    And DBの在庫テーブルは以下になる:
      テーブル: STOCK
      件数: 1件（変更なし）
      データセット: /datasets/stocks/expected-stock-synced.xml
      データ:
        | BOOK_ID | QUANTITY | VERSION |
        | 1       | 15       | 2       |
      検証:
        - レスポンスに基づき QUANTITY が 10 から 15 に更新される
        - VERSION が 1 から 2 にインクリメントされる
```

---

## 3. DBUnitデータセット対応表

| シナリオ | 初期データセット | 期待データセット | 検証テーブル |
|---------|----------------|----------------|------------|
| 在庫数を更新（正常系） | `/datasets/stocks/initial-stock-before-update.xml` | `/datasets/stocks/expected-stock-updated.xml` | STOCK |
| 楽観的ロック競合検知 | `/datasets/stocks/initial-stock-version-conflict.xml` | （変更なし） | STOCK |
| 外部在庫API連携 | `/datasets/stocks/initial-stock-before-sync.xml` | `/datasets/stocks/expected-stock-synced.xml` | STOCK |

---

## 4. 参考資料

* [functional_design.md](functional_design.md) - 在庫管理機能設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書
* DBUnit公式ドキュメント: http://dbunit.sourceforge.net/
