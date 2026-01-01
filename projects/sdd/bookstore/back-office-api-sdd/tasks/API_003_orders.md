# 注文API (API_003_orders) タスク

**担当者:** 担当者C（1名）  
**推奨スキル:** Jakarta EE、JAX-RS、JPA、トランザクション管理、楽観的ロック  
**想定工数:** 7時間  
**依存タスク:** [common_tasks.md](common_tasks.md)

---

## 概要

注文APIを実装します。注文作成、注文履歴取得、注文詳細取得、注文明細取得の4つのエンドポイントを含みます。楽観的ロック制御、トランザクション管理、在庫管理を実装します。

---

## タスクリスト

### 4.1 DTO/Record実装

- [ ] [P] **T_API003_001**: OrderRequest レコードの作成
  - **目的**: 注文リクエストDTOを実装する
  - **対象**: `pro.kensait.berrybooks.api.dto.OrderRequest`
  - **参照SPEC**: [api/API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「4.1 OrderRequest」
  - **注意事項**: @Valid, @NotNull, @NotBlank アノテーションを使用

- [ ] [P] **T_API003_002**: CartItemRequest レコードの作成
  - **目的**: カートアイテムリクエストDTOを実装する
  - **対象**: `pro.kensait.berrybooks.api.dto.CartItemRequest`
  - **参照SPEC**: [api/API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「4.2 CartItemRequest」
  - **注意事項**: @NotNull, @NotBlank アノテーションを使用

- [ ] [P] **T_API003_003**: OrderResponse レコードの作成
  - **目的**: 注文レスポンスDTOを実装する
  - **対象**: `pro.kensait.berrybooks.api.dto.OrderResponse`
  - **参照SPEC**: [api/API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「4.3 OrderResponse」
  - **注意事項**: Java Record を使用

- [ ] [P] **T_API003_004**: OrderDetailResponse レコードの作成
  - **目的**: 注文明細レスポンスDTOを実装する
  - **対象**: `pro.kensait.berrybooks.api.dto.OrderDetailResponse`
  - **参照SPEC**: [api/API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「4.5 OrderDetailResponse」
  - **注意事項**: Java Record を使用

---

### 4.2 例外クラス実装

- [ ] [P] **T_API003_005**: OutOfStockException の作成
  - **目的**: 在庫不足例外を実装する
  - **対象**: `pro.kensait.berrybooks.service.order.OutOfStockException`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「8.1 例外階層」
  - **注意事項**: RuntimeException を継承、bookId と bookName を保持

- [ ] [P] **T_API003_006**: OutOfStockExceptionMapper の作成
  - **目的**: 在庫不足例外マッパーを実装する
  - **対象**: `pro.kensait.berrybooks.api.exception.OutOfStockExceptionMapper`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「8.2 Exception Mapper」
  - **注意事項**: @Provider を使用、409 Conflict を返す

- [ ] [P] **T_API003_007**: OptimisticLockExceptionMapper の作成
  - **目的**: 楽観的ロック競合例外マッパーを実装する
  - **対象**: `pro.kensait.berrybooks.api.exception.OptimisticLockExceptionMapper`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「8.2 Exception Mapper」
  - **注意事項**: @Provider を使用、409 Conflict を返す

---

### 4.3 転送オブジェクト実装

- [ ] [P] **T_API003_008**: OrderTO クラスの作成
  - **目的**: 注文転送オブジェクト（POJO）を実装する
  - **対象**: `pro.kensait.berrybooks.service.order.OrderTO`
  - **参照SPEC**: [api/API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3.1 注文作成」
  - **注意事項**: サービス層で使用するPOJO、CartItemリストを保持

- [ ] [P] **T_API003_009**: CartItem クラスの作成
  - **目的**: カートアイテム（POJO）を実装する
  - **対象**: `pro.kensait.berrybooks.service.order.CartItem`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.1 パッケージ編成」
  - **注意事項**: bookId, count, version を保持

- [ ] [P] **T_API003_010**: OrderHistoryTO レコードの作成
  - **目的**: 注文履歴DTO（非正規化）を実装する
  - **対象**: `pro.kensait.berrybooks.service.order.OrderHistoryTO`
  - **参照SPEC**: 
    - [api/API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「4.4 OrderHistoryResponse」
    - [data_model.md](../specs/baseline/system/data_model.md) の「5.2 注文履歴取得（非正規化DTO）」
  - **注意事項**: Java Record を使用、1注文明細=1レコード

- [ ] [P] **T_API003_011**: OrderSummaryTO レコードの作成
  - **目的**: 注文サマリーDTOを実装する
  - **対象**: `pro.kensait.berrybooks.service.order.OrderSummaryTO`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.1 パッケージ編成」
  - **注意事項**: Java Record を使用

---

### 4.4 DAOクラス実装

- [ ] **T_API003_012**: StockDao の作成
  - **目的**: 在庫データアクセスクラスを実装する
  - **対象**: `pro.kensait.berrybooks.dao.StockDao`
  - **参照SPEC**: [api/API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「6. 楽観的ロック制御」
  - **注意事項**: 
    - @ApplicationScoped を使用
    - findByBookId(), updateStock(bookId, quantity, version) メソッドを実装
    - updateStock() は WHERE VERSION = :version で楽観的ロックを実装

- [ ] **T_API003_013**: OrderTranDao の作成
  - **目的**: 注文トランザクションデータアクセスクラスを実装する
  - **対象**: `pro.kensait.berrybooks.dao.OrderTranDao`
  - **参照SPEC**: [api/API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3.2 注文履歴取得」
  - **注意事項**: 
    - @ApplicationScoped を使用
    - insert(), findByCustomerId(), findByOrderTranId() メソッドを実装

- [ ] **T_API003_014**: OrderDetailDao の作成
  - **目的**: 注文明細データアクセスクラスを実装する
  - **対象**: `pro.kensait.berrybooks.dao.OrderDetailDao`
  - **参照SPEC**: [api/API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3.4 注文明細取得」
  - **注意事項**: 
    - @ApplicationScoped を使用
    - insert(), findByOrderDetailId() メソッドを実装

---

### 4.5 Serviceクラス実装

- [ ] **T_API003_015**: OrderServiceIF インターフェースの作成
  - **目的**: 注文サービスインターフェースを定義する
  - **対象**: `pro.kensait.berrybooks.service.order.OrderServiceIF`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.1 パッケージ編成」
  - **注意事項**: orderBooks(), getOrderHistory() メソッドを定義

- [ ] **T_API003_016**: OrderService の作成
  - **目的**: 注文ビジネスロジックを実装する
  - **対象**: `pro.kensait.berrybooks.service.order.OrderService`
  - **参照SPEC**: 
    - [api/API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「7. トランザクション管理」
    - [api/API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) の「2. 注文作成」
  - **注意事項**: 
    - @ApplicationScoped, @Transactional を使用
    - orderBooks() メソッド: 在庫チェック、在庫更新（楽観的ロック）、注文作成
    - OutOfStockException, OptimisticLockException をスロー
    - DeliveryFeeService を注入して配送料金を計算

---

### 4.6 Resourceクラス実装

- [ ] **T_API003_017**: OrderResource の作成（注文作成機能）
  - **目的**: 注文作成エンドポイントを実装する
  - **対象**: `pro.kensait.berrybooks.api.OrderResource` の createOrder() メソッド
  - **参照SPEC**: 
    - [api/API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3.1 注文作成」
    - [api/API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) の「2. 注文作成」
  - **注意事項**: 
    - @Path("/orders"), @POST を使用
    - SecuredResource から customerId を取得（認証必須）
    - OrderService を注入
    - OutOfStockException, OptimisticLockException を捕捉して409 Conflict

- [ ] **T_API003_018**: OrderResource の作成（注文履歴取得機能）
  - **目的**: 注文履歴取得エンドポイントを実装する
  - **対象**: `pro.kensait.berrybooks.api.OrderResource` の getOrderHistory() メソッド
  - **参照SPEC**: 
    - [api/API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3.2 注文履歴取得」
    - [api/API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) の「3. 注文履歴取得」
  - **注意事項**: 
    - @GET, @Path("/history") を使用
    - SecuredResource から customerId を取得（認証必須）
    - 非正規化DTO（OrderHistoryTO）を返す

- [ ] **T_API003_019**: OrderResource の作成（注文詳細取得機能）
  - **目的**: 注文詳細取得エンドポイントを実装する
  - **対象**: `pro.kensait.berrybooks.api.OrderResource` の getOrderDetail() メソッド
  - **参照SPEC**: 
    - [api/API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3.3 注文詳細取得」
    - [api/API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) の「4. 注文詳細取得」
  - **注意事項**: 
    - @GET, @Path("/{tranId}") を使用
    - 認証不要
    - 注文が見つからない場合は404 Not Found

- [ ] **T_API003_020**: OrderResource の作成（注文明細取得機能）
  - **目的**: 注文明細取得エンドポイントを実装する
  - **対象**: `pro.kensait.berrybooks.api.OrderResource` の getOrderDetailItem() メソッド
  - **参照SPEC**: 
    - [api/API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) の「3.4 注文明細取得」
    - [api/API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) の「5. 注文明細取得」
  - **注意事項**: 
    - @GET, @Path("/{tranId}/details/{detailId}") を使用
    - 認証不要
    - 注文明細が見つからない場合は404 Not Found

---

### 4.7 ユニットテスト

- [ ] [P] **T_API003_021**: OrderService のユニットテスト（注文作成）
  - **目的**: 注文作成ビジネスロジックのテストを実装する
  - **対象**: `pro.kensait.berrybooks.service.order.OrderServiceTest`
  - **参照SPEC**: [api/API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) の「2. 注文作成」
  - **注意事項**: 
    - JUnit 5 + Mockito を使用
    - StockDao, OrderTranDao, OrderDetailDao をモック
    - 正常系、在庫不足、楽観的ロック競合のテスト

- [ ] [P] **T_API003_022**: OrderService のユニットテスト（トランザクションロールバック）
  - **目的**: トランザクションロールバックのテストを実装する
  - **対象**: `pro.kensait.berrybooks.service.order.OrderServiceTest`
  - **参照SPEC**: [api/API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) の「7. トランザクション管理」
  - **注意事項**: 
    - JUnit 5 + Mockito を使用
    - 在庫不足時に全ての変更がロールバックされることを確認

- [ ] [P] **T_API003_023**: OrderResource のユニットテスト
  - **目的**: 注文APIエンドポイントのテストを実装する
  - **対象**: `pro.kensait.berrybooks.api.OrderResourceTest`
  - **参照SPEC**: [api/API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md)
  - **注意事項**: 
    - JUnit 5 + Mockito を使用
    - OrderService をモック
    - 正常系、認証エラー、ビジネス例外のテスト

---

### 4.8 APIテスト（E2E）

- [ ] **T_API003_024**: 注文APIのE2Eテスト
  - **目的**: 注文API全体のE2Eテストを実装する
  - **対象**: E2Eテストスクリプト（JUnit 5またはPlaywright）
  - **参照SPEC**: [api/API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md)
  - **注意事項**: 
    - ログイン → 注文作成 → 注文履歴取得 のフローをテスト
    - 在庫不足エラーのテスト
    - 楽観的ロック競合エラーのテスト（並行実行）
    - トランザクションロールバックのテスト

---

## 完了条件

以下の全ての条件を満たしていること：

- [ ] 全てのDTO/Record、例外クラスが作成されている
- [ ] StockDao、OrderTranDao、OrderDetailDao、OrderServiceが実装されている
- [ ] OrderResourceの全てのエンドポイント（注文作成、履歴、詳細、明細）が実装されている
- [ ] 楽観的ロック制御が正常に機能する
- [ ] トランザクション管理が正常に機能する（ロールバック含む）
- [ ] ユニットテストが実装され、カバレッジが80%以上である
- [ ] E2Eテストが実装され、主要フローが正常に動作する

---

## 参考資料

- [api/API_003_orders/functional_design.md](../specs/baseline/api/API_003_orders/functional_design.md) - 注文API機能設計書
- [api/API_003_orders/behaviors.md](../specs/baseline/api/API_003_orders/behaviors.md) - 注文API受入基準
- [architecture_design.md](../specs/baseline/system/architecture_design.md) - アーキテクチャ設計書
- [data_model.md](../specs/baseline/system/data_model.md) - データモデル仕様書
