# API_003_orders - 注文APIタスク

担当者: 担当者C（1名）  
推奨スキル: JAX-RS、JPA、トランザクション管理、楽観的ロック  
想定工数: 8時間  
依存タスク: [common_tasks.md](common_tasks.md)

---

## タスク一覧

### DTO作成

* [X] [P] T_API003_001: CartItemRequestの作成
  * 目的: カートアイテムリクエストDTOを実装する
  * 対象: CartItemRequest.java（Record）
  * 参照SPEC: [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「4.2 CartItemRequest」
  * 注意事項: bookId、bookName、publisherName、price、count、versionフィールド、Bean Validation

---

* [X] [P] T_API003_002: OrderRequestの作成
  * 目的: 注文リクエストDTOを実装する
  * 対象: OrderRequest.java（Record）
  * 参照SPEC: [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「4.1 OrderRequest」
  * 注意事項: cartItems、totalPrice、deliveryPrice、deliveryAddress、settlementTypeフィールド、Bean Validation

---

* [X] [P] T_API003_003: OrderDetailResponseの作成
  * 目的: 注文明細レスポンスDTOを実装する
  * 対象: OrderDetailResponse.java（Record）
  * 参照SPEC: [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「4.5 OrderDetailResponse」
  * 注意事項: orderDetailId、bookId、bookName、publisherName、price、countフィールド

---

* [X] [P] T_API003_004: OrderResponseの作成
  * 目的: 注文レスポンスDTOを実装する
  * 対象: OrderResponse.java（Record）
  * 参照SPEC: [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「4.3 OrderResponse」
  * 注意事項: orderTranId、orderDate、totalPrice、deliveryPrice、deliveryAddress、settlementType、orderDetailsフィールド

---

* [X] [P] T_API003_005: OrderHistoryResponseの作成
  * 目的: 注文履歴レスポンスDTO（非正規化）を実装する
  * 対象: OrderHistoryResponse.java（Record）
  * 参照SPEC: [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「4.4 OrderHistoryResponse」
  * 注意事項: orderDate、orderTranId、orderDetailId、bookName、publisherName、price、countフィールド

---

### Service作成

* [X] T_API003_006: DeliveryFeeServiceの作成
  * 目的: 配送料金計算サービスを実装する
  * 対象: DeliveryFeeService.java（@ApplicationScoped）
  * 参照SPEC: 
    * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「2.3 コンポーネントの責務」
    * [behaviors.md](../specs/baseline/system/behaviors.md) の「7. 配送料金計算」
  * 注意事項: calculateDeliveryFeeメソッドを実装、購入金額10,000円未満で800円、10,000円以上で無料、沖縄県の場合は特別料金

---

* [X] T_API003_007: OrderServiceの作成
  * 目的: 注文処理サービスを実装する
  * 対象: OrderService.java（@ApplicationScoped）
  * 参照SPEC: 
    * [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3.1.6 シーケンス図」
    * [API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) の「4.1 注文作成」
  * 注意事項: OrderTranDao、OrderDetailDao、BackOfficeRestClientをインジェクション、@Transactionalを使用

---

### 注文処理ビジネスロジック

* [X] T_API003_008: orderBooksメソッドの実装
  * 目的: 注文作成ビジネスロジックを実装する
  * 対象: OrderService.orderBooks()
  * 参照SPEC: 
    * [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3.1.5 ビジネスルール」
    * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「7. トランザクション管理」
  * 注意事項: 在庫チェック、在庫更新（楽観的ロック）、注文トランザクション作成、注文明細作成、トランザクション境界（@Transactional）

---

* [X] T_API003_009: findOrderHistoryByCustomerIdメソッドの実装
  * 目的: 注文履歴取得ロジックを実装する
  * 対象: OrderService.findOrderHistoryByCustomerId()
  * 参照SPEC: 
    * [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3.2.5 ビジネスルール」
    * [data_model.md](../specs/baseline/system/data_model.md) の「5.2 注文履歴取得」
  * 注意事項: OrderTranDao.findByCustomerId()を呼び出し、外部APIで書籍情報を取得してDTOを返却

---

* [X] T_API003_010: findOrderByIdメソッドの実装
  * 目的: 注文詳細取得ロジックを実装する
  * 対象: OrderService.findOrderById()
  * 参照SPEC: [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3.3 注文詳細取得」
  * 注意事項: OrderTranDao.findById()を呼び出し、OrderResponseに変換、外部APIで書籍情報を取得

---

* [X] T_API003_011: findOrderDetailByIdメソッドの実装
  * 目的: 注文明細取得ロジックを実装する
  * 対象: OrderService.findOrderDetailById()
  * 参照SPEC: [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3.4 注文明細取得」
  * 注意事項: OrderDetailDao.findByOrderDetailPK()を呼び出し、OrderDetailResponseに変換、外部APIで書籍情報を取得

---

### Resource作成

* [X] T_API003_012: OrderResourceの作成
  * 目的: 注文APIエンドポイントを実装する
  * 対象: OrderResource.java（@Path("/orders"), @ApplicationScoped）
  * 参照SPEC: 
    * [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3. API仕様」
    * [API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) の「4. 注文API」
  * 注意事項: OrderServiceをインジェクション、AuthenContextをインジェクション（認証情報取得）

---

### エンドポイント実装

* [X] T_API003_013: createOrderメソッドの実装
  * 目的: 注文作成エンドポイントを実装する
  * 対象: OrderResource.createOrder()
  * 参照SPEC: [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3.1 注文作成」
  * 注意事項: JWT認証必須、AuthenContextからcustomerIdを取得、OrderService.orderBooks()呼び出し

---

* [X] T_API003_014: getOrderHistoryメソッドの実装
  * 目的: 注文履歴取得エンドポイントを実装する
  * 対象: OrderResource.getOrderHistory()
  * 参照SPEC: [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3.2 注文履歴取得」
  * 注意事項: JWT認証必須、AuthenContextからcustomerIdを取得、OrderService.findOrderHistoryByCustomerId()呼び出し

---

* [X] T_API003_015: getOrderByIdメソッドの実装
  * 目的: 注文詳細取得エンドポイントを実装する
  * 対象: OrderResource.getOrderById()
  * 参照SPEC: [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3.3 注文詳細取得」
  * 注意事項: 認証不要、OrderService.findOrderById()呼び出し、404エラーハンドリング

---

* [X] T_API003_016: getOrderDetailByIdメソッドの実装
  * 目的: 注文明細取得エンドポイントを実装する
  * 対象: OrderResource.getOrderDetailById()
  * 参照SPEC: [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3.4 注文明細取得」
  * 注意事項: 認証不要、OrderService.findOrderDetailById()呼び出し、404エラーハンドリング

---

### 単体テスト

* [ ] [P] T_API003_017: DeliveryFeeServiceのテスト
  * 目的: 配送料金計算サービスの単体テストを実装する
  * 対象: DeliveryFeeServiceTest.java（JUnit 5）
  * 参照SPEC: [behaviors.md](../specs/baseline/system/behaviors.md) の「7. 配送料金計算」
  * 注意事項: 購入金額10,000円未満/以上、沖縄県/その他の地域のテスト

---

* [ ] [P] T_API003_018: OrderServiceのテスト（注文作成）
  * 目的: 注文作成ビジネスロジックの単体テストを実装する
  * 対象: OrderServiceTest.java（JUnit 5 + Mockito）
  * 参照SPEC: [API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) の「4.1 注文作成」
  * 注意事項: OrderTranDao、OrderDetailDao、BackOfficeRestClientをモック、在庫不足、楽観的ロック競合のテスト

---

* [ ] [P] T_API003_019: OrderResourceのテスト
  * 目的: OrderResourceの単体テストを実装する
  * 対象: OrderResourceTest.java（JUnit 5 + Mockito）
  * 参照SPEC: [API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) の「4. 注文API」
  * 注意事項: OrderServiceをモック、認証必須エンドポイントのテスト

---

### APIテスト（オプション）

* [ ] [P] T_API003_020: 注文APIのE2Eテスト
  * 目的: 注文APIのE2Eテストを実装する（オプション）
  * 対象: OrderApiE2ETest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) の「4. 注文API」
  * 注意事項: ログイン → 注文作成 → 注文履歴取得のシナリオテスト、在庫不足・楽観的ロック競合のテスト、@Tag("e2e")でタグ付け

---

## 実装時の注意事項

### トランザクション管理

@Transactionalの適用:
```java
@Transactional
public OrderTran orderBooks(OrderRequest orderRequest, Integer customerId) {
    // トランザクション境界
}
```

トランザクション範囲:
1. 在庫可用性チェック（外部API呼び出し）
2. 在庫更新（外部API呼び出し・楽観的ロック）
3. 注文トランザクション作成（ローカルDB）
4. 注文明細作成（ローカルDB）
5. コミット（正常終了時）
6. ロールバック（例外発生時）

### 楽観的ロック制御

楽観的ロックはback-office-api側で実装されています。

処理フロー:
1. カート追加時: VERSION値を取得しカートアイテムに保存
2. 注文確定時: 保存したVERSION値でBackOfficeRestClient.updateStock()呼び出し
3. 成功時: 在庫数が減算され、VERSION値が自動インクリメント
4. 失敗時: 409 Conflict（OptimisticLockException）、ユーザーにエラー表示

### 在庫不足エラーハンドリング

```java
StockTO stock = backOfficeRestClient.findStockById(bookId);
if (stock.quantity() < count) {
    throw new OutOfStockException(bookId, bookName);
}
```

---

## 参考資料

* [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) - 注文API機能設計書
* [API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) - 注文API受入基準
* [architecture_design.md](../specs/baseline/system/architecture_design.md) - トランザクション管理・楽観的ロック
* [data_model.md](../specs/baseline/system/data_model.md) - データモデル仕様書
