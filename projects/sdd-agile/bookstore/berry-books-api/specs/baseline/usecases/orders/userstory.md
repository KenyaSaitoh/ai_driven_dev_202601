# ユースケース: 注文作成・注文履歴

ユースケースID: orders  
バージョン: 1.0.0  
最終更新日: 2026-01-31  
ステータス: 確定

---

## 1. ユーザーストーリー

**As a** 認証済み顧客  
**I want to** カート内容で注文を確定し、注文履歴を確認できる  
**So that** 書籍を購入し、過去の注文を追える

---

## 2. 受入基準

* AC1: 認証済みユーザーが注文リクエスト（顧客ID、配送先、決済方法、明細リスト）を送ると注文が作成され、ORDER_TRAN と ORDER_DETAIL が永続化される
* AC2: 在庫が不足している場合は 409 Conflict（OutOfStockException）を返し、注文は作成されない
* AC3: 在庫更新で楽観的ロックに失敗した場合は 409 Conflict（OptimisticLockException）を返し、トランザクションはロールバックされる
* AC4: 注文履歴は顧客IDで取得でき、注文明細（書籍名・出版社名・価格・数量のスナップショット）を含む
* AC5: 注文作成時、back-office-api で在庫更新を行い、同一トランザクション内で注文・明細を登録する

---

## 3. 概要

注文処理は本システムの OrderService で実装。在庫確認・在庫更新は back-office-api（BackOfficeRestClient）を呼び、注文データは common/data_model の ORDER_TRAN, ORDER_DETAIL に保存。スナップショットパターンで書籍名・出版社名・価格を明細に保存。

---

## 4. API仕様

| メソッド | パス | 説明 | 認証 |
|---------|------|------|------|
| POST | /api/orders | 注文作成 | 必要 |
| GET | /api/orders | 注文一覧（顧客別） | 必要 |
| GET | /api/orders/{tranId} | 注文詳細（明細含む） | 必要 |

* リクエスト: OrderRequest（配送先、決済方法、明細リスト。明細は bookId, quantity, version 等）
* レスポンス: OrderResponse（注文ID、日付、合計、配送料、明細リスト）
* common/data_model の ORDER_TRAN, ORDER_DETAIL と整合

---

## 5. ビジネスルール

* BR-ORDER-001: 注文作成時、在庫数が注文数以上であること
* BR-ORDER-002: 在庫更新は楽観的ロック（back-office-api の @Version）で並行制御
* BR-ORDER-003: 注文トランザクションと注文明細は同一トランザクション内で作成
* BR-ORDER-004: 注文履歴は 1 明細 = 1 レコードの非正規化形式。BOOK_NAME, PUBLISHER_NAME, PRICE はスナップショット

---

## 6. 参照

* [../../common/data_model.md](../../common/data_model.md) - ORDER_TRAN, ORDER_DETAIL
* [../../common/external_interface.md](../../common/external_interface.md) - back-office-api 在庫
* [../../common/architecture_design.md](../../common/architecture_design.md) - トランザクション・スナップショット
* [behaviors.md](behaviors.md) - 本ユースケースの振る舞い
