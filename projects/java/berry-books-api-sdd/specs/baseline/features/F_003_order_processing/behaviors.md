# F-003: 注文処理 - 振る舞い仕様書

**機能ID:** F-003  
**機能名:** 注文処理  
**バージョン:** 1.0.0  
**最終更新日:** 2025-12-16

---

## 概要

このドキュメントは、注文処理機能の外形的な振る舞いを定義する。

**関連ドキュメント:**
- [functional_design.md](functional_design.md) - 機能設計
- [screen_design.md](screen_design.md) - 画面設計
- [../../system/requirements.md](../../system/requirements.md) - システム要件定義書

---

## 画面マッピング

| 画面ID | 画面名 | ファイル名 | 認証要否 |
|--------|--------|-----------|---------|
| SC-006 | カート画面 | cartView.xhtml | 必要 |
| SC-007 | 注文入力画面 | bookOrder.xhtml | 必要 |
| SC-008 | 注文完了画面 | orderSuccess.xhtml | 必要 |
| SC-009 | 注文エラー画面 | orderError.xhtml | 必要 |

---

## シナリオ一覧

### Scenario 1: 注文を確定する（正常系）

```gherkin
Given ユーザーがカート画面（cartView.xhtml）でカート内に書籍がある
And 「注文手続きへ」ボタンをクリックし注文画面（bookOrder.xhtml）に遷移している
And 全ての書籍の在庫（Stock.quantity）が注文数以上である
When ユーザーが配送先住所フィールドに「東京都渋谷区神南1-1-1」を入力する
And 決済方法ラジオボタンで「クレジットカード」（code=2）を選択する
And 配送料金「800円」が自動計算される（BR-020）
And 「注文確定」ボタンをクリックする
Then OrderBean.placeOrder()が呼び出される
And OrderTOが作成される
And OrderService.orderBooks(orderTO)が@Transactionalで実行される
And 在庫チェック（BR-022）が行われる
And 楽観的ロックで在庫が更新される（BR-024）
And VERSIONカラムが自動インクリメントされる
And OrderTranエンティティが作成される
And OrderDetailエンティティが作成される
And トランザクションがコミットされる（BR-025）
And 注文IDが発行される（OrderTran.orderTranId）
And CartSession.clear()が呼び出される
And 注文完了画面（orderSuccess.xhtml）に遷移する
And 注文IDが表示される
```

**受入基準:**
- [ ] トランザクションが正常にコミットされる
- [ ] 在庫数が注文数分減少する
- [ ] カートが完全にクリアされる
- [ ] 注文IDが画面に表示される
- [ ] 注文履歴に新しい注文が追加される

---

### Scenario 2: 在庫不足エラー（異常系）

```gherkin
Given ユーザーがカートに書籍A（bookId=1、数量5）を追加している
And 書籍Aの現在の在庫数（Stock.quantity）が「3」である
When ユーザーが注文画面（bookOrder.xhtml）で注文確定ボタンをクリックする
Then OrderService.orderBooks()内で在庫チェックが行われる
And 在庫数（3）< 注文数（5）が検出される
And OutOfStockExceptionがスローされる
And OrderBeanで例外がキャッチされる
And FacesMessageが追加される: 「BIZ-003: 在庫が不足しています」
And 注文エラー画面（orderError.xhtml）に遷移する
And エラーメッセージが表示される
And 注文トランザクションはロールバックされる
And カートはクリアされない
```

**受入基準:**
- [ ] 在庫チェックが注文確定時（BR-022）に行われる
- [ ] OutOfStockExceptionが適切にハンドリングされる
- [ ] エラーメッセージ「BIZ-003」が表示される
- [ ] トランザクションがロールバックされる
- [ ] カート内容が保持される

---

### Scenario 3: 同時注文による競合（楽観的ロック）

```gherkin
Given ユーザーA（セッションA）とユーザーB（セッションB）が同じ書籍（bookId=1）をカートに追加している
And 書籍の現在の在庫数が5、VERSIONが1である
And ユーザーAのカートアイテムのversion値が1である
And ユーザーBのカートアイテムのversion値が1である
And ユーザーAが数量3、ユーザーBが数量3を注文しようとしている
When ユーザーAが先に注文確定ボタンをクリックする
Then ユーザーAの注文が成功する
And 在庫が5→2に減少する
And VERSIONが1→2にインクリメントされる
And ユーザーAのトランザクションがコミットされる
When ユーザーBが後から注文確定ボタンをクリックする
Then OrderService.orderBooks()内で在庫更新が実行される
And UPDATE文のWHERE条件「version = 1」が一致しない（現在version=2）
And 更新行数が0となる
And OptimisticLockExceptionがスローされる（BR-024）
And OrderBeanで例外がキャッチされる
And FacesMessageが追加される: 「BIZ-004: 他のユーザーが購入済みです。カートを確認してください」
And 注文エラー画面（orderError.xhtml）に遷移する
And ユーザーBのトランザクションはロールバックされる
And ユーザーBの注文は確定されない
And ユーザーBのカートはクリアされない
```

**受入基準:**
- [ ] 楽観的ロック制御（BR-024）が正常に機能する
- [ ] カート追加時のバージョン番号（BR-012）が保存される
- [ ] バージョン不一致時にOptimisticLockExceptionが発生する
- [ ] エラーメッセージ「BIZ-004」が表示される
- [ ] 先着ユーザーの注文のみが成功する
- [ ] 後続ユーザーはカート内容を保持したまま調整できる

---

### Scenario 4: 配送料金の自動計算（通常）

```gherkin
Given ユーザーがカート画面で購入金額3000円のカートを確認している
When ユーザーが「注文手続きへ」ボタンをクリックする
And 注文画面（bookOrder.xhtml）で配送先住所フィールドに「東京都渋谷区」を入力する
Then DeliveryFeeService.calculateDeliveryFee()が呼び出される
And 購入金額3000円 < 5000円であることを確認
And 住所が「沖縄県」で始まらないことを確認
And 配送料金「800円」が返される（BR-020）
And 画面に配送料金「800円」が表示される
And 総合計「3000 + 800 = 3800円」が表示される
```

**受入基準:**
- [ ] 購入金額が5000円未満の場合、通常配送料800円が適用される
- [ ] 配送料金が自動計算される
- [ ] 総合計 = 購入金額 + 配送料金

---

### Scenario 5: 沖縄県への配送料金

```gherkin
Given ユーザーがカート画面で購入金額3000円のカートを確認している
When ユーザーが注文画面（bookOrder.xhtml）で配送先住所フィールドに「沖縄県那覇市」を入力する
Then DeliveryFeeService.calculateDeliveryFee()が呼び出される
And 購入金額3000円 < 5000円であることを確認
And 住所が「沖縄県」で始まることを検出
And 配送料金「1700円」が返される（BR-020）
And 画面に配送料金「1700円」が表示される
And 総合計「3000 + 1700 = 4700円」が表示される
```

**受入基準:**
- [ ] 配送先住所が「沖縄県」で始まる場合、配送料1700円が適用される
- [ ] isOkinawa()メソッドが正しく判定する
- [ ] 沖縄料金は購入金額5000円未満の場合に適用される

---

### Scenario 6: 送料無料（5000円以上）

```gherkin
Given ユーザーがカート画面で購入金額5500円のカートを確認している
When ユーザーが注文画面（bookOrder.xhtml）で配送先住所フィールドに「東京都渋谷区」を入力する
Then DeliveryFeeService.calculateDeliveryFee()が呼び出される
And 購入金額5500円 >= 5000円であることを確認
And 配送料金「0円」が返される（BR-020: 送料無料）
And 画面に配送料金「0円（送料無料）」が表示される
And 総合計「5500 + 0 = 5500円」が表示される
```

**受入基準:**
- [ ] 購入金額が5000円以上の場合、配送料が0円になる
- [ ] isFreeDelivery()メソッドが正しく判定する
- [ ] 総合計が購入金額と一致する

---

## ビジネスルール参照

| ルールID | 説明 | 影響するシナリオ |
|---------|-------------|-----------------|
| BR-020 | 配送料金計算ルール | Scenario 4, 5, 6 |
| BR-021 | 決済方法選択肢 | Scenario 1 |
| BR-022 | 在庫チェックタイミング | Scenario 1, 2 |
| BR-023 | 在庫減算タイミング | Scenario 1 |
| BR-024 | 楽観的ロック制御 | Scenario 1, 3 |
| BR-025 | トランザクション範囲 | Scenario 1, 2, 3 |

---

## テスト実行チェックリスト

- [ ] Scenario 1: 注文を確定する（正常系）
- [ ] Scenario 2: 在庫不足エラー（異常系）
- [ ] Scenario 3: 同時注文による競合（楽観的ロック）
- [ ] Scenario 4: 配送料金の自動計算（通常）
- [ ] Scenario 5: 沖縄県への配送料金
- [ ] Scenario 6: 送料無料（5000円以上）

