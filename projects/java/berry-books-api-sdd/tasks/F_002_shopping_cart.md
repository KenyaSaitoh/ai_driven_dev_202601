# F-002: ショッピングカート管理

**担当者:** 担当者B（1名）  
**推奨スキル:** JSF、CDI、セッション管理  
**想定工数:** 10時間  
**依存タスク:** common_tasks.md

---

## 概要

本タスクは、ショッピングカート管理機能を実装します。書籍をカートに追加、カート内容の表示、数量変更、削除、合計金額の自動計算を行います。カート内容はセッションスコープで管理します。

---

## タスクリスト

### セクション1: プレゼンテーション層（DTO）

- [X] **T_F002_001**: CartItemクラスの作成
  - **目的**: カート内の書籍情報を保持するDTOクラスを実装する
  - **対象**: `pro.kensait.berrybooks.web.cart.CartItem`（DTOクラス）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_002_shopping_cart/functional_design.md) の「6.1 プレゼンテーション層」
  - **注意事項**: 
    - フィールド: `bookId`, `bookName`, `publisherName`, `price`, `count`, `version`（在庫バージョン番号、楽観的ロック用）, `remove`（削除フラグ）
    - `Serializable`を実装
    - getter/setterを実装
    - `getSubTotal()`メソッド: 小計（price × count）を返す

---

### セクション2: プレゼンテーション層（セッションBean）

- [X] **T_F002_002**: CartSessionの作成
  - **目的**: セッションスコープでカート状態を管理するBeanを実装する
  - **対象**: `pro.kensait.berrybooks.web.cart.CartSession`（セッションスコープBean）
  - **参照SPEC**: 
    - [functional_design.md](../specs/baseline/features/F_002_shopping_cart/functional_design.md) の「6.1 プレゼンテーション層」
    - [behaviors.md](../specs/baseline/system/behaviors.md) の「BR-010, BR-011, BR-012, BR-013」
  - **注意事項**: 
    - `@Named`、`@SessionScoped`を付与
    - `Serializable`を実装
    - フィールド: `cartItems`（List<CartItem>）、`totalPrice`（BigDecimal）、`deliveryPrice`（BigDecimal）、`deliveryAddress`（String）
    - メソッド: `clear()`（カートをクリア）
    - メソッド: `addItem(CartItem item)`
      - 同じ書籍IDが既に存在する場合: 数量を加算（BR-011）
      - 存在しない場合: カートアイテムを追加
      - 追加後、`calculateTotalPrice()`を呼び出し
    - メソッド: `removeItem(Integer bookId)`（カートからアイテムを削除）
    - メソッド: `calculateTotalPrice()`（合計金額を計算、BR-013）
    - メソッド: `getCartItems()`（カートアイテムのリストを返す）
    - メソッド: `isEmpty()`（カートが空かどうかを判定）

---

### セクション3: プレゼンテーション層（Managed Bean）

- [X] **T_F002_003**: CartBeanの作成
  - **目的**: カート画面のコントローラーを実装する
  - **対象**: `pro.kensait.berrybooks.web.cart.CartBean`（Managed Bean）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_002_shopping_cart/functional_design.md) の「6.1 プレゼンテーション層」
  - **注意事項**: 
    - `@Named`、`@ViewScoped`を付与
    - `Serializable`を実装
    - `@Inject`で`CartSession`をインジェクト
    - メソッド: `updateCart()`
      - カートアイテムの削除フラグ（`remove`）をチェック
      - 削除フラグがtrueの場合: `cartSession.removeItem()`を呼び出し
      - 合計金額を再計算
    - メソッド: `proceedToOrder()`
      - カートが空の場合: エラーメッセージ表示、注文画面に遷移しない（BIZ-005）
      - カートが空でない場合: `bookOrder.xhtml`に遷移
    - ログ出力: `INFO  [ CartBean#updateCart ]`, `INFO  [ CartBean#proceedToOrder ]`

---

### セクション4: F-001との連携

- [X] **T_F002_004**: BookSearchBeanのaddToCart()メソッドの実装
  - **目的**: 書籍検索画面からカートに追加する機能を実装する
  - **対象**: `pro.kensait.berrybooks.web.book.BookSearchBean`の`addToCart()`メソッド（F-001で作成したクラスに追加）
  - **参照SPEC**: 
    - [functional_design.md](../specs/baseline/features/F_002_shopping_cart/functional_design.md) の「4. 機能フロー」
    - [behaviors.md](../specs/baseline/system/behaviors.md) の「BR-012」
  - **注意事項**: 
    - `@Inject`で`CartSession`をインジェクト
    - メソッド: `addToCart(Book book)`
      - 在庫情報（`Stock`）を取得し、在庫バージョン番号を保存（BR-012）
      - `CartItem`を作成（`bookId`, `bookName`, `publisherName`, `price`, `count=1`, `version`）
      - `cartSession.addItem(cartItem)`を呼び出し
      - 成功メッセージ表示: 「カートに追加しました」
      - `cartView.xhtml`に遷移
    - ログ出力: `INFO  [ BookSearchBean#addToCart ] bookId={}`

---

### セクション5: ビュー層（XHTML）

- [X] **T_F002_005**: cartView.xhtmlの作成
  - **目的**: カート確認画面のビューを実装する
  - **対象**: `src/main/webapp/cartView.xhtml`（Facelets XHTML）
  - **参照SPEC**: [screen_design.md](../specs/baseline/features/F_002_shopping_cart/screen_design.md) の「1. カート確認画面」
  - **注意事項**: 
    - `<h:form>`でカート編集フォームを作成
    - `<h:dataTable>`でカートアイテムを表示（`#{cartSession.cartItems}`をソース）
    - 各行に書籍情報（書籍名、出版社名、単価、数量、小計）を表示
    - 数量は`<h:inputText>`で編集可能
    - 削除は`<h:selectBooleanCheckbox>`で削除フラグを設定
    - `<h:commandButton>`で「更新」ボタン（`#{cartBean.updateCart}`アクション）
    - 合計金額を表示（`#{cartSession.totalPrice}`）
    - `<h:commandButton>`で「注文に進む」ボタン（`#{cartBean.proceedToOrder}`アクション）
    - カートが空の場合: 「カートは空です」メッセージ表示
    - ヘッダーにナビゲーションメニュー（書籍検索、カート、注文履歴、ログアウト）を配置

---

### セクション6: テスト

- [X] **T_F002_006**: CartSessionの単体テストの作成
  - **目的**: CartSessionのカート管理ロジックをテストする
  - **対象**: `src/test/java/.../web/cart/CartSessionTest.java`（JUnit 5テスト）
  - **参照SPEC**: 
    - [constitution.md](../memory/constitution.md) の「原則3: テスト駆動品質」
    - [functional_design.md](../specs/baseline/features/F_002_shopping_cart/functional_design.md) の「6.1 プレゼンテーション層」
  - **注意事項**: 
    - テストケース: 
      - カートアイテム追加（正常系）
      - 同じ書籍を複数回追加（数量加算、BR-011）
      - カートアイテム削除（正常系）
      - 合計金額計算（正常系、BR-013）
      - カートクリア（正常系）

---

## 並行実行可能なタスク

以下のタスクは並行して実行できます：

- T_F002_001（CartItem）は他タスクと独立
- T_F002_002（CartSession）はT_F002_001完了後
- T_F002_003（CartBean）はT_F002_002完了後
- T_F002_004（BookSearchBeanとの連携）はT_F002_002完了後
- T_F002_005（ビュー）はT_F002_003完了後
- T_F002_006（テスト）はT_F002_002完成後

---

## 完了条件

- [X] CartItem、CartSession、CartBeanが実装されている
- [X] BookSearchBeanの`addToCart()`メソッドが実装されている
- [X] cartView.xhtmlが実装されている
- [X] カート追加時に在庫バージョン番号を保存している（BR-012）
- [X] 同じ書籍を追加した場合、数量が加算される（BR-011）
- [X] 合計金額が自動計算される（BR-013）
- [X] 単体テストが実装され、カバレッジ80%以上
- [ ] プロジェクトがビルドでき、エラーがない
- [ ] 手動テスト: カート追加、カート更新、カート削除が正常に動作する

---

## 次のステップ

F-002完了後、他の機能タスクと並行して作業を進めてください。全機能完了後、[integration_tasks.md](integration_tasks.md) に進んでください。

