# F-003: 注文処理

**担当者:** 担当者C（1名）  
**推奨スキル:** Jakarta EE、JPA、トランザクション管理、楽観的ロック  
**想定工数:** 16時間  
**依存タスク:** common_tasks.md

---

## 概要

本タスクは、注文処理機能を実装します。カート内の書籍を購入し、配送先と決済方法を指定して注文を確定します。配送料金の自動計算、在庫減算、楽観的ロック制御を含みます。

---

## タスクリスト

### セクション1: 例外クラス

- [X] **T_F003_001**: OutOfStockExceptionの作成
  - **目的**: 在庫不足時にスローするカスタム例外クラスを実装する
  - **対象**: `pro.kensait.berrybooks.service.order.OutOfStockException`（例外クラス）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「8. エラーハンドリング方針」
  - **注意事項**: 
    - `RuntimeException`を継承
    - フィールド: `bookId`（Integer）、`bookName`（String）
    - コンストラクタで`bookId`と`bookName`を受け取る

---

### セクション2: 転送オブジェクト（TO）

- [X] **T_F003_002**: OrderTOの作成
  - **目的**: 注文情報をレイヤー間で転送するTOクラスを実装する
  - **対象**: `pro.kensait.berrybooks.service.order.OrderTO`（転送オブジェクト）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_003_order_processing/functional_design.md) の「6.1 転送オブジェクト（TO）」
  - **注意事項**: 
    - フィールド: `customerId`, `deliveryAddress`, `deliveryPrice`, `settlementCode`, `cartItems`（List<CartItem>）
    - getter/setterを実装
    - `Serializable`を実装

- [X] **T_F003_003**: OrderHistoryTOの作成
  - **目的**: 注文履歴情報をレイヤー間で転送するTOクラスを実装する
  - **対象**: `pro.kensait.berrybooks.service.order.OrderHistoryTO`（転送オブジェクト）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_003_order_processing/functional_design.md) の「6.1 転送オブジェクト（TO）」
  - **注意事項**: 
    - フィールド: `orderTranId`, `orderDate`, `totalPrice`, `deliveryPrice`, `settlementName`
    - getter/setterを実装

- [X] **T_F003_004**: OrderSummaryTOの作成
  - **目的**: 注文サマリー情報をレイヤー間で転送するTOクラスを実装する
  - **対象**: `pro.kensait.berrybooks.service.order.OrderSummaryTO`（転送オブジェクト）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_003_order_processing/functional_design.md) の「6.1 転送オブジェクト（TO）」
  - **注意事項**: 
    - フィールド: `orderTran`（OrderTran）、`orderDetails`（List<OrderDetail>）
    - getter/setterを実装

---

### セクション3: データアクセス層

- [X] **T_F003_005**: StockDaoの作成
  - **目的**: 在庫エンティティのCRUD操作を実装する（楽観的ロック付き）
  - **対象**: `pro.kensait.berrybooks.dao.StockDao`（DAOクラス）
  - **参照SPEC**: 
    - [functional_design.md](../specs/baseline/features/F_003_order_processing/functional_design.md) の「6.3 データアクセス層」
    - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「7. 並行制御」
  - **注意事項**: 
    - `@ApplicationScoped`を付与
    - `@PersistenceContext`で`EntityManager`をインジェクト
    - メソッド: `findById(Integer bookId)`（書籍IDで在庫を検索、`Stock`エンティティ取得）
    - メソッド: `update(Stock stock)`
      - `entityManager.merge(stock)`で在庫を更新
      - JPAの`@Version`機能により、WHERE句に`AND VERSION = ?`が自動追加される
      - VERSION不一致の場合、`OptimisticLockException`が自動スロー

- [X] **T_F003_006**: OrderTranDaoの作成
  - **目的**: 注文トランザクションエンティティのCRUD操作を実装する
  - **対象**: `pro.kensait.berrybooks.dao.OrderTranDao`（DAOクラス）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_003_order_processing/functional_design.md) の「6.3 データアクセス層」
  - **注意事項**: 
    - `@ApplicationScoped`を付与
    - `@PersistenceContext`で`EntityManager`をインジェクト
    - メソッド: `persist(OrderTran orderTran)`（注文トランザクションを登録）
    - メソッド: `findByCustomerId(Integer customerId)`（顧客IDで注文履歴を取得、注文日降順）
    - メソッド: `findById(Long orderTranId)`（注文IDで検索）

- [X] **T_F003_007**: OrderDetailDaoの作成
  - **目的**: 注文明細エンティティのCRUD操作を実装する
  - **対象**: `pro.kensait.berrybooks.dao.OrderDetailDao`（DAOクラス）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_003_order_processing/functional_design.md) の「6.3 データアクセス層」
  - **注意事項**: 
    - `@ApplicationScoped`を付与
    - `@PersistenceContext`で`EntityManager`をインジェクト
    - メソッド: `persist(OrderDetail orderDetail)`（注文明細を登録）
    - メソッド: `findByOrderTranId(Long orderTranId)`（注文IDで明細を取得）

---

### セクション4: ビジネスロジック層

- [X] **T_F003_008**: OrderServiceの作成
  - **目的**: 注文処理のビジネスロジックを実装する
  - **対象**: `pro.kensait.berrybooks.service.order.OrderService`（サービスクラス）
  - **参照SPEC**: 
    - [functional_design.md](../specs/baseline/features/F_003_order_processing/functional_design.md) の「6.2 ビジネスロジック層」
    - [behaviors.md](../specs/baseline/system/behaviors.md) の「BR-022, BR-023, BR-024, BR-025」
  - **注意事項**: 
    - `@ApplicationScoped`を付与
    - `@Inject`で`StockDao`, `OrderTranDao`, `OrderDetailDao`をインジェクト
    - メソッド: `orderBooks(OrderTO orderTO)` 
      - **@Transactionalを付与**（単一トランザクション、BR-025）
      - 処理フロー:
        1. カートアイテムごとに在庫チェック（BR-022）
        2. 在庫数 < 注文数の場合: `OutOfStockException`をスロー
        3. カートアイテムに保存したVERSION値で在庫を更新（BR-024）
        4. 在庫数を減算（BR-023）
        5. `OrderTran`エンティティを作成・登録
        6. `OrderDetail`エンティティを作成・登録（カートアイテムごと）
        7. トランザクションコミット
      - ログ出力: `INFO  [ OrderService#orderBooks ] customerId={}, totalPrice={}`
      - 楽観的ロック競合時: `OptimisticLockException`が自動スロー
    - メソッド: `getOrderHistory(Integer customerId)`（注文履歴を取得、F-005で使用）
    - メソッド: `getOrderDetail(Long orderTranId)`（注文詳細を取得、F-005で使用）

---

### セクション5: プレゼンテーション層（Managed Bean）

- [X] **T_F003_009**: OrderBeanの作成
  - **目的**: 注文処理のコントローラーを実装する
  - **対象**: `pro.kensait.berrybooks.web.order.OrderBean`（Managed Bean）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_003_order_processing/functional_design.md) の「6.4 プレゼンテーション層」
  - **注意事項**: 
    - `@Named`、`@ViewScoped`を付与
    - `Serializable`を実装
    - `@Inject`で`OrderService`, `DeliveryFeeService`, `CartSession`, `CustomerBean`をインジェクト
    - フィールド: `orderTO`（OrderTO）、`deliveryAddress`（String）、`settlementCode`（Integer）
    - `@PostConstruct`メソッド: カートが空の場合、エラーメッセージ表示（BIZ-005）
    - メソッド: `calculateDeliveryFee()`
      - `deliveryFeeService.calculateDeliveryFee(cartSession.totalPrice, deliveryAddress)`を呼び出し
      - 配送料金を`cartSession.deliveryPrice`に設定
    - メソッド: `placeOrder()`
      - `OrderTO`を作成（`customerId`, `deliveryAddress`, `deliveryPrice`, `settlementCode`, `cartItems`）
      - `orderService.orderBooks(orderTO)`を呼び出し
      - 成功時: カートをクリア、`orderSuccess.xhtml`に遷移
      - `OutOfStockException`発生時: エラーメッセージ表示、`orderError.xhtml`に遷移
      - `OptimisticLockException`発生時: エラーメッセージ表示（「他のユーザーが購入済みです」）、`orderError.xhtml`に遷移
    - ログ出力: `INFO  [ OrderBean#placeOrder ]`

---

### セクション6: ビュー層（XHTML）

- [X] **T_F003_010**: bookOrder.xhtmlの作成
  - **目的**: 注文入力画面のビューを実装する
  - **対象**: `src/main/webapp/bookOrder.xhtml`（Facelets XHTML）
  - **参照SPEC**: [screen_design.md](../specs/baseline/features/F_003_order_processing/screen_design.md) の「1. 注文入力画面」
  - **注意事項**: 
    - `<h:form>`で注文入力フォームを作成
    - カート内容を表示（`<h:dataTable>`で`#{cartSession.cartItems}`）
    - 配送先住所入力: `<h:inputText value="#{orderBean.deliveryAddress}"/>`（必須検証）
    - 決済方法選択: `<h:selectOneMenu value="#{orderBean.settlementCode}"/>`（銀行振込、クレジットカード、着払い）
    - 配送料金計算: `<h:commandButton value="配送料金を計算" action="#{orderBean.calculateDeliveryFee}"/>`
    - 配送料金表示: `#{cartSession.deliveryPrice}円`
    - 合計金額表示: `#{cartSession.totalPrice + cartSession.deliveryPrice}円`
    - 「注文確定」ボタン: `<h:commandButton value="注文確定" action="#{orderBean.placeOrder}"/>`

- [X] **T_F003_011**: orderSuccess.xhtmlの作成
  - **目的**: 注文完了画面のビューを実装する
  - **対象**: `src/main/webapp/orderSuccess.xhtml`（Facelets XHTML）
  - **参照SPEC**: [screen_design.md](../specs/baseline/features/F_003_order_processing/screen_design.md) の「2. 注文完了画面」
  - **注意事項**: 
    - 注文完了メッセージ表示: 「注文が完了しました」
    - 注文番号、注文日、合計金額を表示
    - 「注文履歴を見る」ボタン: `orderHistory.xhtml`に遷移
    - 「書籍検索に戻る」ボタン: `bookSearch.xhtml`に遷移

- [X] **T_F003_012**: orderError.xhtmlの作成
  - **目的**: 注文エラー画面のビューを実装する
  - **対象**: `src/main/webapp/orderError.xhtml`（Facelets XHTML）
  - **参照SPEC**: [screen_design.md](../specs/baseline/features/F_003_order_processing/screen_design.md) の「3. 注文エラー画面」
  - **注意事項**: 
    - エラーメッセージ表示: 「在庫不足」または「他のユーザーが購入済み」
    - 「カートに戻る」ボタン: `cartView.xhtml`に遷移

---

### セクション7: テスト

- [X] **T_F003_013**: DeliveryFeeServiceのユニットテストの作成
  - **目的**: 配送料金計算ロジックをテストする
  - **対象**: `src/test/java/.../service/delivery/DeliveryFeeServiceTest.java`（JUnit 5テスト）
  - **参照SPEC**: 
    - [behaviors.md](../specs/baseline/system/behaviors.md) の「BR-020」
    - [constitution.md](../memory/constitution.md) の「原則3: テスト駆動品質」
  - **注意事項**: 
    - テストケース: 
      - 購入金額4999円、東京都 → 配送料800円
      - 購入金額5000円、東京都 → 配送料0円（送料無料）
      - 購入金額3000円、沖縄県 → 配送料1700円
      - 購入金額5000円、沖縄県 → 配送料0円（送料無料優先）

- [X] **T_F003_014**: OrderServiceのユニットテストの作成
  - **目的**: 注文処理のビジネスロジックをテストする
  - **対象**: `src/test/java/.../service/order/OrderServiceTest.java`（JUnit 5テスト）
  - **参照SPEC**: 
    - [constitution.md](../memory/constitution.md) の「原則3: テスト駆動品質」
    - [functional_design.md](../specs/baseline/features/F_003_order_processing/functional_design.md) の「6.2 ビジネスロジック層」
  - **注意事項**: 
    - Mockitoで`StockDao`, `OrderTranDao`, `OrderDetailDao`をモック
    - テストケース: 
      - 注文確定（正常系）
      - 在庫不足（異常系、`OutOfStockException`）
      - 楽観的ロック競合（異常系、`OptimisticLockException`）

---

## 並行実行可能なタスク

以下のタスクは並行して実行できます：

- T_F003_001（例外クラス）は他タスクと独立
- T_F003_002〜004（TOクラス）は並行可能
- T_F003_005〜007（Daoクラス）は並行可能
- T_F003_008（OrderService）はT_F003_001, T_F003_002, T_F003_005〜007完了後
- T_F003_009（OrderBean）はT_F003_008完了後
- T_F003_010〜012（ビュー）はT_F003_009完了後
- T_F003_013〜014（テスト）は対応するクラス完成後

---

## 完了条件

- [X] OutOfStockException、OrderTO、OrderHistoryTO、OrderSummaryTOが実装されている
- [X] StockDao、OrderTranDao、OrderDetailDaoが実装されている
- [X] OrderService、OrderBeanが実装されている
- [X] bookOrder.xhtml、orderSuccess.xhtml、orderError.xhtmlが実装されている
- [X] 配送料金計算が正しく実装されている（BR-020）
- [X] 在庫チェック・在庫減算が正しく実装されている（BR-022, BR-023）
- [X] 楽観的ロック制御が正しく実装されている（BR-024）
- [X] トランザクション境界が正しく設定されている（BR-025）
- [X] 単体テストが実装され、カバレッジ80%以上
- [ ] プロジェクトがビルドでき、エラーがない
- [ ] 手動テスト: 注文確定、在庫不足、楽観的ロック競合が正常に動作する

---

## 次のステップ

F-003完了後、他の機能タスクと並行して作業を進めてください。全機能完了後、[integration_tasks.md](integration_tasks.md) に進んでください。

