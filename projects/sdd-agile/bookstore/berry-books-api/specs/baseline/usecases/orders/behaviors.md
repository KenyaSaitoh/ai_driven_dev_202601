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
  Given DBに以下の顧客が存在する:
    テーブル: CUSTOMER
    件数: 1件
    データセット: /datasets/orders/initial-customer.xml
    データ:
      | CUSTOMER_ID | EMAIL            | NAME        |
      | 1           | test@example.com | テストユーザー |
  
  And DBに以下の書籍が存在する:
    テーブル: BOOK
    件数: 1件
    データ:
      | BOOK_ID | TITLE    | PRICE |
      | 1       | Java入門 | 3000  |
  
  And DBの注文テーブルは空である:
    テーブル: ORDER_TRAN
    件数: 0件
  
  And DBの注文明細テーブルは空である:
    テーブル: ORDER_DETAIL
    件数: 0件
  
  And WireMockがPUT /api/stocks/1をスタブしている:
    レスポンス: 200 OK, {"quantity": 8, "version": 2}
  
  When POST /api/orders に 顧客ID=1, 明細=[{bookId:1, quantity:2}] を送る
  
  Then レスポンスは 200 OK
  
  And DBの注文テーブルは以下になる:
    テーブル: ORDER_TRAN
    件数: 1件（+1件追加）
    データセット: /datasets/orders/expected-order-created.xml
    検証:
      - ORDER_TRAN_ID は自動採番される
      - CUSTOMER_ID は 1
      - トランザクションがコミットされる
  
  And DBの注文明細テーブルは以下になる:
    テーブル: ORDER_DETAIL
    件数: 1件（+1件追加）
    データセット: /datasets/orders/expected-order-detail-created.xml
    データ:
      | ORDER_DETAIL_ID | ORDER_TRAN_ID | BOOK_ID | QUANTITY | PRICE |
      | 1               | 1             | 1       | 2        | 3000  |
    検証:
      - 明細分が作成される
      - BOOK_ID は外部キー制約を満たす
  
  And 外部在庫APIが呼ばれる:
    リクエスト: PUT /api/stocks/1

Scenario: 在庫不足で注文に失敗する
  Given DBに顧客と書籍が存在する:
    データセット: /datasets/orders/initial-customer-book.xml
  
  And DBの注文テーブルは空である:
    テーブル: ORDER_TRAN
    件数: 0件
  
  And WireMockがPUT /api/stocks/1を400 Bad Request（在庫不足）で返す
  
  When POST /api/orders に 明細=[{bookId:1, quantity:100}] を送る
  
  Then レスポンスは 409 Conflict
  
  And DBの注文テーブルは変化しない:
    テーブル: ORDER_TRAN
    件数: 0件（変更なし）
    検証:
      - OrderTranは作成されない（ロールバック）
      - トランザクションが正常にロールバックされる
  
  And DBの注文明細テーブルは変化しない:
    テーブル: ORDER_DETAIL
    件数: 0件（変更なし）
```

### Feature: 注文一覧・詳細

```gherkin
Scenario: 顧客別に注文一覧を取得する
  Given DBに以下の注文が存在する:
    テーブル: ORDER_TRAN
    件数: 3件
    データセット: /datasets/orders/initial-multiple-orders.xml
    データ:
      | ORDER_TRAN_ID | CUSTOMER_ID | ORDER_DATE | TOTAL_AMOUNT | STATUS    |
      | 1             | 1           | 2026-01-01 | 5000         | COMPLETED |
      | 2             | 1           | 2026-01-02 | 3000         | PENDING   |
      | 3             | 2           | 2026-01-03 | 8000         | COMPLETED |
  
  When GET /api/orders を 顧客ID=1 で送る
  
  Then レスポンスは 200 OK
  And 2件の注文が返る:
    | orderId | customerId | totalAmount | status    |
    | 1       | 1          | 5000        | COMPLETED |
    | 2       | 1          | 3000        | PENDING   |
  
  And DBの状態は変化しない:
    テーブル: ORDER_TRAN
    件数: 3件（変更なし）
    検証:
      - READ操作のため、DBは更新されない

Scenario: 注文詳細を取得する（明細含む）
  Given DBに以下の注文が存在する:
    テーブル: ORDER_TRAN
    件数: 1件
    データセット: /datasets/orders/initial-order-with-details.xml
    データ:
      | ORDER_TRAN_ID | CUSTOMER_ID | ORDER_DATE | TOTAL_AMOUNT |
      | 1             | 1           | 2026-01-01 | 5000         |
  
  And DBに以下の注文明細が存在する:
    テーブル: ORDER_DETAIL
    件数: 2件
    データ:
      | ORDER_DETAIL_ID | ORDER_TRAN_ID | BOOK_ID | QUANTITY | PRICE | BOOK_NAME | PUBLISHER_NAME |
      | 1               | 1             | 1       | 2        | 2500  | Java入門  | 技術評論社     |
      | 2               | 1             | 2       | 1        | 2500  | Python本  | オライリー     |
  
  When GET /api/orders/1 を送る
  
  Then レスポンスは 200 OK
  And 注文明細が2件含まれる:
    スナップショット:
      | bookName | publisherName | price | quantity |
      | Java入門 | 技術評論社    | 2500  | 2        |
      | Python本 | オライリー    | 2500  | 1        |
  
  And DBの状態は変化しない:
    テーブル: ORDER_TRAN, ORDER_DETAIL
    件数: 1件, 2件（変更なし）
    検証:
      - READ操作のため、DBは更新されない
```

---

## 3. DBUnitデータセット対応表

| シナリオ | 初期データセット | 期待データセット | 検証テーブル |
|---------|----------------|----------------|------------|
| 注文を作成する（在庫更新連携） | `/datasets/orders/initial-customer.xml` | `/datasets/orders/expected-order-created.xml`<br>`/datasets/orders/expected-order-detail-created.xml` | ORDER_TRAN<br>ORDER_DETAIL |
| 在庫不足で注文に失敗する | `/datasets/orders/initial-customer-book.xml` | （ロールバック） | ORDER_TRAN<br>ORDER_DETAIL |
| 顧客別に注文一覧を取得する | `/datasets/orders/initial-multiple-orders.xml` | （変更なし） | ORDER_TRAN |
| 注文詳細を取得する（明細含む） | `/datasets/orders/initial-order-with-details.xml` | （変更なし） | ORDER_TRAN<br>ORDER_DETAIL |

---

## 4. 参照

* [userstory.md](userstory.md)
* [../../common/data_model.md](../../common/data_model.md)
* [../../common/external_interface.md](../../common/external_interface.md)
* DBUnit公式ドキュメント: http://dbunit.sourceforge.net/
