# ユースケース: 注文 - 振る舞い仕様書

ユースケースID: orders  
バージョン: 1.0.0  
最終更新日: 2026-01-31

記法: Gherkin。principles/common_rules.md を参照。

---

## 1. 概要

注文作成・注文一覧・注文詳細の振る舞い。結合テストでは WireMock で back-office-api（在庫）をスタブ化。

---

## 2. テストシナリオ（Gherkin）

### Feature: 注文作成

```gherkin
Scenario: 注文を作成する（在庫更新連携）
  Given 顧客ID=1 が存在する
  And WireMock が PUT /api/stocks/1 をスタブしている（200 OK, 在庫更新後）
  When POST /api/orders に 顧客ID=1, 明細=[{bookId:1, quantity:2}] を送る
  Then レスポンスは 200 OK
  And ORDER_TRAN が1件作成される
  And ORDER_DETAIL が明細分作成される
  And 外部在庫APIが呼ばれる

Scenario: 在庫不足で注文に失敗する
  Given WireMock が PUT /api/stocks/1 を 400 Bad Request（在庫不足）で返す
  When POST /api/orders に 明細=[{bookId:1, quantity:100}] を送る
  Then レスポンスは 409 Conflict
  And OrderTran は作成されない（ロールバック）
```

### Feature: 注文一覧・詳細

```gherkin
Scenario: 顧客別に注文一覧を取得する
  Given DB に OrderTran(customerId=1) が2件、OrderTran(customerId=2) が1件ある
  When GET /api/orders を 顧客ID=1 で送る
  Then レスポンスは 200 OK
  And 2件の注文が返る

Scenario: 注文詳細を取得する（明細含む）
  Given OrderTran(id=1) と OrderDetail が2件ある
  When GET /api/orders/1 を送る
  Then レスポンスは 200 OK
  And 注文明細が2件含まれる（スナップショット: BOOK_NAME, PUBLISHER_NAME, PRICE）
```

---

## 3. 参照

* [userstory.md](userstory.md)
* [../../common/data_model.md](../../common/data_model.md)
* [../../common/external_interface.md](../../common/external_interface.md)
