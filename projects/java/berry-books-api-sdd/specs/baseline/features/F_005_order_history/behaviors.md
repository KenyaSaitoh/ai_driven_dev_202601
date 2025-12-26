# F-005: 注文履歴参照 - 振る舞い仕様書

**機能ID:** F-005  
**機能名:** 注文履歴参照  
**バージョン:** 1.0.0  
**最終更新日:** 2025-12-16

---

## 概要

このドキュメントは、注文履歴参照機能の外形的な振る舞いを定義する。

**関連ドキュメント:**
- [functional_design.md](functional_design.md) - 機能設計
- [screen_design.md](screen_design.md) - 画面設計
- [../../system/requirements.md](../../system/requirements.md) - システム要件定義書

---

## 画面マッピング

| 画面ID | 画面名 | ファイル名 | 認証要否 |
|--------|--------|-----------|---------|
| SC-010 | 注文履歴画面 | orderHistory.xhtml | 必要 |
| SC-011 | 注文詳細画面 | orderDetail.xhtml | 必要 |

---

## シナリオ一覧

### Scenario 1: 注文履歴一覧を表示する

```gherkin
Given ユーザー（customerId=1）がログイン済み
And データベースに以下の注文が存在する
  | orderTranId | customerId | orderDate | totalPrice | deliveryPrice | settlementCode |
  | 1001 | 1 | 2025-12-01 | 3000 | 800 | 2 |
  | 1002 | 1 | 2025-12-05 | 5500 | 0 | 1 |
  | 1003 | 1 | 2025-12-10 | 4200 | 800 | 3 |
When ユーザーが書籍検索画面のナビゲーションメニューで「注文履歴」リンクをクリックする
Then 注文履歴画面（orderHistory.xhtml）に遷移する
And OrderHistoryBean.loadOrderHistory()が呼び出される
And OrderService.getOrderHistory(customerId=1)が呼び出される
And OrderTranDao.findByCustomerId(1)で注文を取得（BR-040）
And 注文日降順（BR-041）でソートされる
And 注文一覧がh:dataTableで表示される
And 各注文には以下の情報が表示される
  | フィールド | データソース |
  | 注文ID | OrderTran.orderTranId |
  | 注文日 | OrderTran.orderDate |
  | 配送先 | OrderTran.deliveryAddress |
  | 決済方法 | SettlementType.getDisplayNameByCode(settlementCode) |
  | 合計金額 | OrderTran.totalPrice + deliveryPrice |
```

**受入基準:**
- [ ] ログイン中の顧客の注文のみが表示される（BR-040）
- [ ] 注文が新しい順に表示される（BR-041）
- [ ] 決済方法コードが表示名に変換される（SettlementType.getDisplayNameByCode）
- [ ] 各注文行から注文詳細画面へのリンクがある

---

### Scenario 2: 注文詳細を表示する

```gherkin
Given ユーザーが注文履歴一覧（orderHistory.xhtml）を閲覧している
And 注文ID 1001の詳細情報が以下の通り存在する
  | 注文ヘッダー | 値 |
  | orderTranId | 1001 |
  | orderDate | 2025-12-01 |
  | deliveryAddress | 東京都渋谷区神南1-1-1 |
  | deliveryPrice | 800 |
  | settlementCode | 2 (クレジットカード) |
And 注文明細が以下の通り存在する
  | orderDetailId | bookName | price | count | subtotal |
  | 1 | Java SEディープダイブ | 3400 | 1 | 3400 |
  | 2 | SQLの冒険～RDBの深層 | 2200 | 1 | 2200 |
When ユーザーが注文ID 1001の行をクリックする
Then 注文詳細画面（orderDetail.xhtml）に遷移する
And OrderHistoryBean.loadOrderDetail()が呼び出される
And OrderService.getOrderDetail(1001)が呼び出される（BR-042）
And OrderTranDao.findByIdWithDetails(1001)でJOIN FETCHを使用して注文と明細を取得
And 注文ヘッダー情報が表示される
And 注文明細テーブル（h:dataTable）が表示される
And 配送料金「800円」が表示される
And 総合計「6100円」が表示される
```

**受入基準:**
- [ ] 注文IDで正しく注文詳細が取得される（BR-042）
- [ ] JOIN FETCHでN+1問題を回避する
- [ ] 全ての注文明細が表示される
- [ ] 合計金額の計算が正しい（明細合計 + 配送料金）

---

### Scenario 3: 注文履歴が空の場合

```gherkin
Given ユーザー（customerId=99）がログイン済み
And データベースにcustomerId=99の注文レコードが存在しない
When ユーザーが「注文履歴」メニューをクリックする
Then 注文履歴画面（orderHistory.xhtml）に遷移する
And OrderHistoryBean.loadOrderHistory()が呼び出される
And OrderService.getOrderHistory(customerId=99)が空のリストを返す
And 画面に「注文履歴はありません」というメッセージが表示される
And 注文テーブルは表示されない
And 「書籍検索へ」リンクが表示される
```

**受入基準:**
- [ ] 注文がない場合、エラーではなく案内メッセージが表示される
- [ ] 空のテーブルではなく、ユーザーフレンドリーなメッセージが表示される
- [ ] ユーザーが書籍検索画面に戻れる

---

## ビジネスルール参照

| ルールID | 説明 | 影響するシナリオ |
|---------|-------------|-----------------|
| BR-040 | 注文履歴は顧客IDでフィルタリング | Scenario 1 |
| BR-041 | 注文日降順（新しい順）でソート | Scenario 1 |
| BR-042 | 注文詳細は注文IDで取得 | Scenario 2 |

---

## テスト実行チェックリスト

- [ ] Scenario 1: 注文履歴一覧を表示する
- [ ] Scenario 2: 注文詳細を表示する
- [ ] Scenario 3: 注文履歴が空の場合

