# API_003_orders - 注文API実装タスク

担当者: 担当者C（1名）  
推奨スキル: JAX-RS, JPA, トランザクション管理, 外部API連携  
想定工数: 8時間  
依存タスク: [common_tasks.md](common_tasks.md)

---

## 概要

注文APIを実装する。注文作成、注文履歴取得、注文詳細取得、注文明細取得のエンドポイントを提供する。注文データは本システムで管理し、在庫更新はback-office-apiに連携する。

---

## タスクリスト

### DTO層

#### T_API003_001: [P] OrderRequest の作成

* 目的: 注文リクエストDTOを実装する
* 対象: OrderRequest.java (Record型DTO)
* 参照SPEC: [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「4.1 OrderRequest」
* 注意事項: Record型、cartItems, totalPrice, deliveryPrice, deliveryAddress, settlementTypeフィールド、Bean Validation

---

#### T_API003_002: [P] CartItemRequest の作成

* 目的: カートアイテムリクエストDTOを実装する
* 対象: CartItemRequest.java (Record型DTO)
* 参照SPEC: [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「4.2 CartItemRequest」
* 注意事項: Record型、bookId, bookName, publisherName, price, count, versionフィールド、Bean Validation

---

#### T_API003_003: [P] OrderResponse の作成

* 目的: 注文レスポンスDTOを実装する
* 対象: OrderResponse.java (Record型DTO)
* 参照SPEC: [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「4.3 OrderResponse」
* 注意事項: Record型、orderTranId, orderDate, totalPrice, deliveryPrice, deliveryAddress, settlementType, orderDetailsフィールド

---

#### T_API003_004: [P] OrderHistoryResponse の作成

* 目的: 注文履歴レスポンスDTOを実装する
* 対象: OrderHistoryResponse.java (Record型DTO)
* 参照SPEC: [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「4.4 OrderHistoryResponse」
* 注意事項: Record型、orderDate, orderTranId, orderDetailId, bookName, publisherName, price, countフィールド（非正規化）

---

#### T_API003_005: [P] OrderDetailResponse の作成

* 目的: 注文明細レスポンスDTOを実装する
* 対象: OrderDetailResponse.java (Record型DTO)
* 参照SPEC: [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「4.5 OrderDetailResponse」
* 注意事項: Record型、orderDetailId, bookId, bookName, publisherName, price, countフィールド

---

### Service層

#### T_API003_006: OrderService の作成

* 目的: 注文ビジネスロジックを実装する
* 対象: OrderService.java (サービスクラス)
* 参照SPEC: 
  * [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3.1 注文作成」、「7. トランザクション管理」
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「7. トランザクション管理」
* 注意事項: 
  * @ApplicationScoped
  * @Transactional（トランザクション境界）
  * OrderTranDao, OrderDetailDao, BackOfficeRestClient, DeliveryFeeServiceをインジェクション
  * orderBooksメソッド（注文作成）: 在庫チェック → 在庫更新（外部API） → 注文トランザクション作成 → 注文明細作成
  * 在庫不足時はOutOfStockException、楽観的ロック失敗時はOptimisticLockException
  * スナップショットパターン（bookName, publisherName, priceをOrderDetailに保存）

---

### Resource層

#### T_API003_007: OrderResource の作成

* 目的: 注文APIのRESTエンドポイントを実装する
* 対象: OrderResource.java (JAX-RS Resourceクラス)
* 参照SPEC: 
  * [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3. API仕様」
  * [API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) の「4. 注文API (`/api/orders`)」
* 注意事項: 
  * @Path("/orders")
  * @ApplicationScoped
  * OrderService, OrderTranDao, OrderDetailDaoをインジェクション
  * createOrder, getOrderHistory, getOrderDetail, getOrderDetailItemメソッド
  * 注文作成・注文履歴は認証必須（AuthenContextから顧客ID取得）
  * 注文詳細・注文明細は認証不要

---

### ユニットテスト

#### T_API003_008: [P] OrderServiceのテスト作成

* 目的: OrderServiceのユニットテストを実装する
* 対象: OrderServiceTest.java (JUnit 5テストクラス)
* 参照SPEC: [API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) の「4.1 注文作成」
* 注意事項: 
  * JUnit 5 + Mockitoを使用
  * OrderTranDao, OrderDetailDao, BackOfficeRestClient, DeliveryFeeServiceをモック化
  * 正常系・異常系テストケース
  * 注文作成成功、在庫不足エラー、楽観的ロック競合エラー
  * トランザクションロールバック検証

---

#### T_API003_009: [P] OrderResourceのテスト作成

* 目的: OrderResourceのユニットテストを実装する
* 対象: OrderResourceTest.java (JUnit 5テストクラス)
* 参照SPEC: [API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) の「4. 注文API (`/api/orders`)」
* 注意事項: 
  * JUnit 5 + Mockitoを使用
  * OrderService, OrderTranDao, OrderDetailDaoをモック化
  * 正常系・異常系テストケース
  * 注文作成成功、注文履歴取得成功、注文詳細取得成功

---

## 完了基準

* [ ] OrderRequest DTOが実装されている
* [ ] CartItemRequest DTOが実装されている
* [ ] OrderResponse DTOが実装されている
* [ ] OrderHistoryResponse DTOが実装されている
* [ ] OrderDetailResponse DTOが実装されている
* [ ] OrderService（注文ビジネスロジック、トランザクション管理）が実装されている
* [ ] OrderResource（注文作成、注文履歴取得、注文詳細取得、注文明細取得）が実装されている
* [ ] OrderServiceのユニットテストが実装されている
* [ ] OrderResourceのユニットテストが実装されている
* [ ] 注文作成エンドポイント（POST /api/orders）が動作する
* [ ] 注文履歴取得エンドポイント（GET /api/orders/history）が動作する
* [ ] 注文詳細取得エンドポイント（GET /api/orders/{tranId}）が動作する
* [ ] 注文明細取得エンドポイント（GET /api/orders/{tranId}/details/{detailId}）が動作する

---

## 参考資料

* [API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) - 注文API機能設計書
* [API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) - 注文API受入基準
* [architecture_design.md](../specs/baseline/system/architecture_design.md) - アーキテクチャ設計書
* [data_model.md](../specs/baseline/system/data_model.md) - データモデル仕様書
