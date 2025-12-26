# F-002: ショッピングカート管理 - 振る舞い仕様書

**機能ID:** F-002  
**機能名:** ショッピングカート管理  
**バージョン:** 1.0.0  
**最終更新日:** 2025-12-16

---

## 概要

このドキュメントは、ショッピングカート管理機能の外形的な振る舞いを定義する。

**関連ドキュメント:**
- [functional_design.md](functional_design.md) - 機能設計
- [screen_design.md](screen_design.md) - 画面設計
- [../../system/requirements.md](../../system/requirements.md) - システム要件定義書

---

## 画面マッピング

| 画面ID | 画面名 | ファイル名 | 認証要否 |
|--------|--------|-----------|---------|
| SC-005 | 検索結果画面 | bookSelect.xhtml | 必要 |
| SC-006 | カート画面 | cartView.xhtml | 必要 |

---

## シナリオ一覧

### Scenario 1: 書籍をカートに追加する

```gherkin
Given ユーザーが検索結果画面（bookSelect.xhtml）で書籍を閲覧している
And 書籍の在庫数（Stock.quantity）が2以上である
When ユーザーが書籍の数量フィールドに「2」を入力する
And 「カートへ」ボタンをクリックする
Then CartBean.addBook(bookId, count)が呼び出される
And CartItemがCartSession.cartItemsに追加される
And カートアイテムに以下の情報が保存される
  | フィールド | 値 |
  | bookId | 選択した書籍のID |
  | bookName | 書籍名 |
  | publisherName | 出版社名 |
  | price | 単価 |
  | count | 2 |
  | version | 在庫のVERSION値（BR-012） |
And カート画面（cartView.xhtml）に遷移する
And カートに選択した書籍が数量「2」で表示される
And 小計（price × count）が表示される
And 合計金額（CartSession.totalPrice）が自動計算される（BR-013）
```

**受入基準:**
- [ ] カート追加時に在庫バージョン番号が保存される（楽観的ロック用）
- [ ] 数量が0以下の場合、検証エラー「VAL-004: 数量は1以上を入力してください」が表示される
- [ ] カートアイテムがセッションスコープで保持される（BR-010）

---

### Scenario 2: カート内の書籍を削除する

```gherkin
Given ユーザーがカート画面（cartView.xhtml）でカート内に複数の書籍がある
When ユーザーが特定の書籍の削除チェックボックスをチェックする
And 「選択削除」ボタンをクリックする
Then CartBean.removeSelectedBooks()が呼び出される
And チェックされた書籍（CartItem.remove = true）がCartSession.cartItemsから削除される
And 合計金額（CartSession.totalPrice）が再計算される
And カート画面が更新される
```

**受入基準:**
- [ ] 複数の書籍を同時に削除できる
- [ ] 削除後、カートが空になった場合、「カートが空です」メッセージが表示される
- [ ] 削除操作はセッションに即座に反映される

---

### Scenario 3: カート全体をクリアする

```gherkin
Given ユーザーがカート画面（cartView.xhtml）でカート内に書籍がある
When ユーザーが「カートをクリア」ボタンをクリックする
Then CartBean.clearCart()が呼び出される
And CartSession.clear()が実行される
And 全ての書籍がCartSession.cartItemsから削除される
And 合計金額が0円になる
And カートクリア確認画面（cartClear.xhtml）に遷移する
```

**受入基準:**
- [ ] ワンクリックで全てのアイテムが削除される
- [ ] 確認ダイアログなしで即座にクリアされる
- [ ] クリア後のメッセージが表示される

---

### Scenario 4: 同じ書籍を複数回追加する

```gherkin
Given ユーザーがカートに書籍A（bookId=1、数量2）を追加済み
When ユーザーが検索結果画面から再度書籍A（数量3）をカートに追加する
Then CartBean.addBook()が既存のCartItemを検出する（BR-011）
And 既存のCartItemのcount値が「2 + 3 = 5」に更新される
And 小計（price × 5）が再計算される
And 合計金額が更新される
And カート画面に遷移する
And 書籍Aが1行のみ表示される（重複行なし）
And 数量カラムに「5」が表示される
```

**受入基準:**
- [ ] 同じ書籍IDのアイテムが重複して追加されない
- [ ] 数量が正しく加算される
- [ ] バージョン番号は最新のもので上書きされる

---

## ビジネスルール参照

| ルールID | 説明 | 影響するシナリオ |
|---------|-------------|-----------------|
| BR-010 | カート内容はセッション単位で保持（ログアウトまで） | 全シナリオ |
| BR-011 | 同じ書籍を追加した場合、数量を加算 | Scenario 4 |
| BR-012 | カート追加時点の在庫バージョン番号を保存（楽観的ロック用） | Scenario 1 |
| BR-013 | カート内の合計金額は常に自動計算 | 全シナリオ |

---

## テスト実行チェックリスト

- [ ] Scenario 1: 書籍をカートに追加する
- [ ] Scenario 2: カート内の書籍を削除する
- [ ] Scenario 3: カート全体をクリアする
- [ ] Scenario 4: 同じ書籍を複数回追加する

