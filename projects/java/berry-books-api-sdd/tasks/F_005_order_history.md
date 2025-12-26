# F-005: 注文履歴参照

**担当者:** 担当者E（1名）  
**推奨スキル:** JSF、JPA、JOIN FETCH  
**想定工数:** 8時間  
**依存タスク:** common_tasks.md

---

## 概要

本タスクは、注文履歴参照機能を実装します。過去の注文履歴を一覧表示し、注文詳細を確認できます。

---

## タスクリスト

### セクション1: データアクセス層（F-003で作成済みのクラスへの追加）

- [X] **T_F005_001**: OrderTranDaoへのメソッド追加
  - **目的**: 注文履歴と注文詳細取得のためのメソッドを追加する
  - **対象**: `pro.kensait.berrybooks.dao.OrderTranDao`（F-003で作成済み）
  - **参照SPEC**: 
    - [functional_design.md](../specs/baseline/features/F_005_order_history/functional_design.md) の「6.2 データアクセス層」
    - [behaviors.md](../specs/baseline/system/behaviors.md) の「BR-040, BR-041, BR-042」
  - **注意事項**: 
    - メソッド: `findByCustomerId(Integer customerId)`（既にF-003で実装済みの場合はスキップ）
      - 顧客IDで注文履歴を取得（BR-040）
      - 注文日降順でソート（BR-041）
      - JPQL: `SELECT o FROM OrderTran o WHERE o.customerId = :customerId ORDER BY o.orderDate DESC`
    - メソッド: `findByIdWithDetails(Long orderTranId)`
      - 注文と明細をJOIN FETCHで取得（N+1問題回避）
      - JPQL: `SELECT o FROM OrderTran o LEFT JOIN FETCH o.orderDetails WHERE o.orderTranId = :orderTranId`

- [X] **T_F005_002**: OrderDetailDaoへのメソッド追加確認
  - **目的**: 注文明細取得のためのメソッドを確認する
  - **対象**: `pro.kensait.berrybooks.dao.OrderDetailDao`（F-003で作成済み）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_005_order_history/functional_design.md) の「6.2 データアクセス層」
  - **注意事項**: 
    - メソッド: `findByOrderTranId(Long orderTranId)`（既にF-003で実装済みの場合はスキップ）

---

### セクション2: ビジネスロジック層（F-003で作成済みのクラスへの追加）

- [X] **T_F005_003**: OrderServiceへのメソッド追加
  - **目的**: 注文履歴と注文詳細取得のためのメソッドを追加する
  - **対象**: `pro.kensait.berrybooks.service.order.OrderService`（F-003で作成済み）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_005_order_history/functional_design.md) の「6.3 ビジネスロジック層」
  - **注意事項**: 
    - メソッド: `getOrderHistory(Integer customerId)`（既にF-003で実装済みの場合はスキップ）
      - `orderTranDao.findByCustomerId(customerId)`を呼び出し
      - `List<OrderHistoryTO>`に変換（注文ID、注文日、合計金額、配送料金、決済方法名）
      - 決済方法名は`SettlementType`から取得
    - メソッド: `getOrderDetail(Long orderTranId)`（既にF-003で実装済みの場合はスキップ）
      - `orderTranDao.findByIdWithDetails(orderTranId)`を呼び出し
      - `OrderSummaryTO`に変換（注文トランザクション、注文明細リスト）
    - ログ出力: `INFO  [ OrderService#getOrderHistory ] customerId={}`

---

### セクション3: プレゼンテーション層（Managed Bean）

- [X] **T_F005_004**: OrderHistoryBeanの作成
  - **目的**: 注文履歴画面のコントローラーを実装する
  - **対象**: `pro.kensait.berrybooks.web.order.OrderHistoryBean`（Managed Bean）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_005_order_history/functional_design.md) の「6.4 プレゼンテーション層」
  - **注意事項**: 
    - `@Named`、`@ViewScoped`を付与
    - `Serializable`を実装
    - `@Inject`で`OrderService`, `CustomerBean`をインジェクト
    - フィールド: `orderHistoryList`（List<OrderHistoryTO>）、`orderSummary`（OrderSummaryTO）
    - `@PostConstruct`メソッド: `loadOrderHistory()`を呼び出し
    - メソッド: `loadOrderHistory()`
      - `CustomerBean`からログイン中の顧客IDを取得
      - `orderService.getOrderHistory(customerId)`を呼び出し
      - `orderHistoryList`に結果を設定
    - メソッド: `getOrderDetail(Long orderTranId)`
      - `orderService.getOrderDetail(orderTranId)`を呼び出し
      - `orderSummary`に結果を設定
      - `orderDetail.xhtml`に遷移
    - ログ出力: `INFO  [ OrderHistoryBean#loadOrderHistory ]`

---

### セクション4: ビュー層（XHTML）

- [X] **T_F005_005**: orderHistory.xhtmlの作成
  - **目的**: 注文履歴一覧画面のビューを実装する
  - **対象**: `src/main/webapp/orderHistory.xhtml`（Facelets XHTML）
  - **参照SPEC**: [screen_design.md](../specs/baseline/features/F_005_order_history/screen_design.md) の「1. 注文履歴一覧画面」
  - **注意事項**: 
    - `<h:dataTable>`で注文履歴を表示（`#{orderHistoryBean.orderHistoryList}`をソース）
    - 各行に注文情報（注文番号、注文日、合計金額、配送料金、決済方法名）を表示
    - 「詳細を見る」リンク: `<h:commandLink value="詳細" action="#{orderHistoryBean.getOrderDetail(orderHistory.orderTranId)}"/>`
    - 注文履歴が空の場合: 「注文履歴はありません」メッセージ表示
    - ヘッダーにナビゲーションメニュー（書籍検索、カート、注文履歴、ログアウト）を配置

- [X] **T_F005_006**: orderDetail.xhtmlの作成
  - **目的**: 注文詳細画面のビューを実装する
  - **対象**: `src/main/webapp/orderDetail.xhtml`（Facelets XHTML）
  - **参照SPEC**: [screen_design.md](../specs/baseline/features/F_005_order_history/screen_design.md) の「2. 注文詳細画面」
  - **注意事項**: 
    - 注文情報表示: 注文番号、注文日、配送先住所、決済方法、配送料金、合計金額
    - `<h:dataTable>`で注文明細を表示（`#{orderHistoryBean.orderSummary.orderDetails}`をソース）
    - 各行に明細情報（書籍名、単価、数量、小計）を表示
    - 「注文履歴に戻る」ボタン: `<h:commandButton value="戻る" action="orderHistory"/>`

---

### セクション5: テスト

- [X] **T_F005_007**: OrderServiceのユニットテストの追加
  - **目的**: OrderServiceの注文履歴・詳細取得ロジックをテストする
  - **対象**: `src/test/java/.../service/order/OrderServiceTest.java`（F-003で作成済み、テストケース追加）
  - **参照SPEC**: 
    - [constitution.md](../memory/constitution.md) の「原則3: テスト駆動品質」
    - [functional_design.md](../specs/baseline/features/F_005_order_history/functional_design.md) の「6.3 ビジネスロジック層」
  - **注意事項**: 
    - Mockitoで`OrderTranDao`, `OrderDetailDao`をモック
    - テストケース: 
      - 注文履歴取得（正常系）
      - 注文履歴が空（正常系）
      - 注文詳細取得（正常系）

---

## 並行実行可能なタスク

以下のタスクは並行して実行できます：

- T_F005_001, T_F005_002（Daoメソッド追加）は並行可能
- T_F005_003（Serviceメソッド追加）はT_F005_001, T_F005_002完了後
- T_F005_004（OrderHistoryBean）はT_F005_003完了後
- T_F005_005, T_F005_006（ビュー）はT_F005_004完了後
- T_F005_007（テスト）はT_F005_003完成後

---

## 完了条件

- [X] OrderTranDao、OrderDetailDaoへのメソッド追加が完了している
- [X] OrderServiceへのメソッド追加が完了している
- [X] OrderHistoryBeanが実装されている
- [X] orderHistory.xhtml、orderDetail.xhtmlが実装されている
- [X] 注文履歴が顧客IDでフィルタリングされている（BR-040）
- [X] 注文日降順でソートされている（BR-041）
- [X] 注文詳細が注文IDで取得されている（BR-042）
- [X] JOIN FETCHでN+1問題を回避している
- [X] 単体テストが追加され、カバレッジ80%以上
- [X] プロジェクトがビルドでき、エラーがない
- [ ] 手動テスト: 注文履歴一覧、注文詳細が正常に表示される

---

## 次のステップ

F-005完了後、他の機能タスクと並行して作業を進めてください。全機能完了後、[integration_tasks.md](integration_tasks.md) に進んでください。

