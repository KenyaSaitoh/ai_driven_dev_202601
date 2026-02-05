# FUNC_006_orders_api - 注文API

## メタデータ

* タスクID: FUNC_006
* 機能タイプ: API
* 依存タスク: FUNC_001, FUNC_002, FUNC_003
* 並行実行可能: FUNC_004, FUNC_005, FUNC_007
* 担当者: 担当者E
* 推奨スキル: JAX-RS, トランザクション管理, 外部API連携
* 想定工数: 8時間

## 実装内容

注文APIエンドポイントを実装する。
注文作成、注文履歴取得、注文詳細取得の機能を提供する。

---

## タスクリスト

### 1. Resourceクラスの作成

* [ ] T_FUNC006_001: OrderResourceの作成
  * 目的: 注文APIエンドポイントを実装する
  * 対象: pro.kensait.berrybooks.api.OrderResource
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3.1 API実装方式」
  * 注意事項:
    * @Path("/orders")
    * @ApplicationScoped
    * OrderService、AuthenContextを注入
    * 認証必須（JwtAuthenFilter適用）

* [ ] T_FUNC006_002: POST /orders エンドポイントの実装
  * 目的: 注文作成処理を実装する
  * 対象: OrderResource.createOrder()
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「7.1 注文作成フロー」
    * [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「5.2.2 OrderService.orderBooks() の処理フロー」
  * 注意事項:
    * @POST, @Consumes(APPLICATION_JSON), @Produces(APPLICATION_JSON)
    * OrderRequestを受け取る
    * AuthenContextからcustomerIdを取得
    * OrderService.createOrder()を呼び出し
    * OrderResponseを返却
    * ステータスコード: 200 OK

* [ ] T_FUNC006_003: GET /orders エンドポイントの実装
  * 目的: ログイン中のユーザーの注文履歴を取得する
  * 対象: OrderResource.getMyOrders()
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「5.1.1 ビジネスルール」
  * 注意事項:
    * @GET, @Produces(APPLICATION_JSON)
    * AuthenContextからcustomerIdを取得
    * OrderService.findOrdersByCustomerId()を呼び出し
    * OrderResponse[]を返却

* [ ] T_FUNC006_004: GET /orders/{orderTranId} エンドポイントの実装
  * 目的: 指定された注文IDの詳細を取得する
  * 対象: OrderResource.getOrderById()
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「5.1.1 ビジネスルール」
  * 注意事項:
    * @GET, @Path("/{orderTranId}"), @Produces(APPLICATION_JSON)
    * @PathParam("orderTranId") int orderTranId
    * OrderService.findOrderById()を呼び出し
    * OrderResponseを返却

### 2. DTOマッピング

* [ ] T_FUNC006_005: OrderTran → OrderResponse マッピングの実装
  * 目的: EntityからDTOへの変換ロジックを実装する
  * 対象: OrderResource内のマッピングメソッド
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「3.2 DTO設計方針」
  * 注意事項:
    * OrderTranエンティティをOrderResponseに変換
    * OrderDetailエンティティをOrderDetailResponseに変換
    * 関連エンティティも含めた完全なマッピング

* [ ] T_FUNC006_006: OrderRequest バリデーションの実装
  * 目的: リクエストDTOの妥当性を検証する
  * 対象: OrderResource.createOrder()
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8. エラーハンドリング」
  * 注意事項:
    * @Valid アノテーション使用
    * Bean Validation実行
    * バリデーションエラー時: 400 Bad Request

### 3. エラーハンドリング

* [ ] T_FUNC006_007: 在庫不足時の処理
  * 目的: 在庫不足時に適切なエラーレスポンスを返す
  * 対象: OrderResource.createOrder()
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8.1 エラーレスポンス仕様」
  * 注意事項:
    * OrderServiceからOutOfStockExceptionがスローされる
    * OutOfStockExceptionMapperで409 Conflictに変換
    * ErrorResponseを返却

* [ ] T_FUNC006_008: 楽観的ロック失敗時の処理
  * 目的: 楽観的ロック失敗時に適切なエラーレスポンスを返す
  * 対象: OrderResource.createOrder()
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「6. 並行制御（楽観的ロック）」
  * 注意事項:
    * OrderServiceからOptimisticLockExceptionがスローされる
    * OptimisticLockExceptionMapperで409 Conflictに変換
    * ErrorResponseを返却

* [ ] T_FUNC006_009: 注文未検出時の処理
  * 目的: 指定された注文IDが見つからない場合の処理を実装する
  * 対象: OrderResource.getOrderById()
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8.1 エラーレスポンス仕様」
  * 注意事項:
    * OrderServiceからResourceNotFoundExceptionがスローされる
    * ResourceNotFoundExceptionMapperで404 Not Foundに変換
    * ErrorResponseを返却

### 4. 権限チェック

* [ ] T_FUNC006_010: 注文詳細の権限チェック
  * 目的: 注文詳細取得時に本人確認を実施する
  * 対象: OrderResource.getOrderById()
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「4.1 認証・認可」
  * 注意事項:
    * AuthenContextからcustomerIdを取得
    * 注文のcustomerIdと一致しない場合は403 Forbiddenを返す

---

## 完了条件

* [ ] 注文作成処理が正常に動作する
* [ ] 注文履歴取得が正常に動作する
* [ ] 注文詳細取得が正常に動作する
* [ ] 在庫不足時に409 Conflictが返される
* [ ] 楽観的ロック失敗時に409 Conflictが返される
* [ ] 注文未検出時に404 Not Foundが返される
* [ ] 他人の注文詳細取得時に403 Forbiddenが返される
* [ ] Bean Validationが正常に動作する
* [ ] 単体テストが全て成功する

---

## 参考資料

* [../specs/baseline/basic_design/architecture_design.md](../specs/baseline/basic_design/architecture_design.md) - アーキテクチャ設計書
* [../specs/baseline/basic_design/functional_design.md](../specs/baseline/basic_design/functional_design.md) - 機能設計書
* [../specs/baseline/basic_design/data_model.md](../specs/baseline/basic_design/data_model.md) - データモデル仕様書
* [../specs/baseline/basic_design/behaviors.md](../specs/baseline/basic_design/behaviors.md) - 振る舞い仕様書
