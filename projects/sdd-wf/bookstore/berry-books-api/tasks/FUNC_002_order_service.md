# FUNC_002_order_service - 注文サービス

## メタデータ

* タスクID: FUNC_002
* 機能タイプ: ビジネスサービス
* 依存タスク: FUNC_001
* 並行実行可能: FUNC_003
* 担当者: 担当者A
* 推奨スキル: Jakarta EE, JPA, JTA, トランザクション管理, 外部API連携
* 想定工数: 8時間

## 実装内容

注文処理のビジネスロジックを実装する。
このサービスは、注文作成、注文履歴取得、注文詳細取得、在庫確認・更新（外部API経由）を担当する。

---

## タスクリスト

### 1. サービスクラスの作成

* [ ] T_FUNC002_001: OrderServiceの作成
  * 目的: 注文処理のビジネスロジックを実装する
  * 対象: pro.kensait.berrybooks.service.OrderService
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「5.1 注文ドメイン」
    * [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「5. トランザクション管理」
  * 注意事項:
    * @ApplicationScoped
    * @Transactional
    * OrderTranDao、OrderDetailDao、BackOfficeRestClient、DeliveryFeeServiceを注入

* [ ] T_FUNC002_002: createOrder()メソッドの実装
  * 目的: 注文作成処理を実装する
  * 対象: OrderService.createOrder()
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「7. システム全体のデータフロー」
    * [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「5.1 トランザクション境界とマイクロサービス連携」
  * 注意事項:
    * トランザクション境界設定
    * 在庫確認（BackOfficeRestClient.getStock()）
    * 在庫更新（BackOfficeRestClient.updateStock()）
    * OrderTran作成
    * OrderDetail作成（複数）
    * 配送料金計算（DeliveryFeeService）
    * スナップショット保存（bookName, publisherName, price）

* [ ] T_FUNC002_003: findOrdersByCustomerId()メソッドの実装
  * 目的: 顧客IDで注文履歴を取得する
  * 対象: OrderService.findOrdersByCustomerId()
  * 参照SPEC: 
    * [data_model.md](../specs/baseline/basic_design/data_model.md) の「4.2.2 注文履歴検索」
    * [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「5.1.1 ビジネスルール」
  * 注意事項:
    * OrderTranDao.findByCustomerId()呼び出し
    * 注文日の降順でソート
    * JOIN FETCHでOrderDetailも取得（N+1問題回避）

* [ ] T_FUNC002_004: findOrderById()メソッドの実装
  * 目的: 注文IDで注文詳細を取得する
  * 対象: OrderService.findOrderById()
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「5.1.1 ビジネスルール」
  * 注意事項:
    * OrderTranDao.findById()呼び出し
    * JOIN FETCHでOrderDetailも取得
    * 注文が見つからない場合はResourceNotFoundExceptionをスロー

### 2. リクエスト/レスポンスDTOの作成

* [ ] T_FUNC002_005: OrderRequestの作成
  * 目的: 注文リクエストを表現するDTOを作成する
  * 対象: pro.kensait.berrybooks.api.dto.OrderRequest
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「3.2 DTO設計方針」
  * 注意事項:
    * Javaレコード型（immutable）
    * customerId, deliveryAddress, settlementType, cartItems
    * @NotNull, @NotEmpty, @Valid

* [ ] T_FUNC002_006: CartItemRequestの作成
  * 目的: カートアイテムを表現するDTOを作成する
  * 対象: pro.kensait.berrybooks.api.dto.CartItemRequest
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「3.2 DTO設計方針」
  * 注意事項:
    * Javaレコード型（immutable）
    * bookId, bookName, publisherName, price, quantity, version
    * @NotNull, @Min

* [ ] T_FUNC002_007: OrderResponseの作成
  * 目的: 注文レスポンスを表現するDTOを作成する
  * 対象: pro.kensait.berrybooks.api.dto.OrderResponse
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「3.2 DTO設計方針」
  * 注意事項:
    * Javaレコード型（immutable）
    * orderTranId, orderDate, customerId, totalPrice, deliveryPrice, deliveryAddress, settlementType, orderDetails

* [ ] T_FUNC002_008: OrderDetailResponseの作成
  * 目的: 注文明細レスポンスを表現するDTOを作成する
  * 対象: pro.kensait.berrybooks.api.dto.OrderDetailResponse
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「3.2 DTO設計方針」
  * 注意事項:
    * Javaレコード型（immutable）
    * orderDetailId, bookId, bookName, publisherName, price, count

### 3. ビジネスルールバリデーション

* [ ] T_FUNC002_009: 在庫可用性チェックの実装
  * 目的: 注文時に在庫数が十分か確認する
  * 対象: OrderService内のバリデーション
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「5.1.1 ビジネスルール」
  * 注意事項:
    * BR-ORDER-001: 在庫数 >= 注文数
    * 在庫不足の場合はOutOfStockExceptionをスロー

* [ ] T_FUNC002_010: 注文金額の計算と検証
  * 目的: 注文金額の合計を計算し、検証する
  * 対象: OrderService内の計算ロジック
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「5.1.1 ビジネスルール」
  * 注意事項:
    * 注文金額 = Σ(書籍価格 × 数量) + 配送料金
    * totalPrice = itemsSubtotal + deliveryPrice

### 4. エラーハンドリング

* [ ] T_FUNC002_011: 楽観的ロック失敗時の処理
  * 目的: 在庫更新時の楽観的ロック失敗を処理する
  * 対象: OrderService.createOrder()
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「6. 並行制御（楽観的ロック）」
  * 注意事項:
    * BackOfficeRestClientから409 Conflictが返された場合
    * OptimisticLockExceptionをスロー
    * トランザクションロールバック

* [ ] T_FUNC002_012: 外部APIエラー時の処理
  * 目的: 外部API呼び出し失敗時の処理を実装する
  * 対象: OrderService.createOrder()
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「5.2.3 エラーハンドリング」
  * 注意事項:
    * WebApplicationException、ProcessingExceptionのキャッチ
    * 適切な例外に変換してスロー
    * トランザクションロールバック

---

## 完了条件

* [ ] 注文作成処理が正常に動作する
* [ ] 在庫確認・更新が正常に動作する（外部API経由）
* [ ] 注文履歴取得が正常に動作する
* [ ] 注文詳細取得が正常に動作する
* [ ] 在庫不足時にOutOfStockExceptionがスローされる
* [ ] 楽観的ロック失敗時にOptimisticLockExceptionがスローされる
* [ ] トランザクション境界が正しく設定されている
* [ ] N+1問題が発生していない
* [ ] 単体テストが全て成功する

---

## 参考資料

* [../specs/baseline/basic_design/architecture_design.md](../specs/baseline/basic_design/architecture_design.md) - アーキテクチャ設計書
* [../specs/baseline/basic_design/functional_design.md](../specs/baseline/basic_design/functional_design.md) - 機能設計書
* [../specs/baseline/basic_design/data_model.md](../specs/baseline/basic_design/data_model.md) - データモデル仕様書
* [../specs/baseline/basic_design/behaviors.md](../specs/baseline/basic_design/behaviors.md) - 振る舞い仕様書
