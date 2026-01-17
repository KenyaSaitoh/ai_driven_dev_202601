# berry-books-api-sdd - API_003_orders タスク

担当者: 担当者C（1名）  
推奨スキル: JAX-RS、JPA、トランザクション管理、楽観的ロック  
想定工数: 16時間  
依存タスク: [共通機能タスク](common.md)

---

## 概要

本タスクは、注文APIの実装を行う。注文作成、注文履歴取得、注文詳細取得、注文明細取得を実装する。

* 実装パターン: 独自実装 + 外部API連携
  * 注文処理は本システムで実装
  * 在庫更新はback-office-api経由で実行（楽観的ロック対応）
  * トランザクション管理が重要

---

## タスクリスト

### 1. DTO（リクエスト/レスポンス）

#### T_API003_001: CartItemRequest の作成

* [ ] [P] T_API003_001: CartItemRequest の作成
  * 目的: カートアイテムリクエストDTOを実装する
  * 対象: CartItemRequest.java（Java Record）
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3. API一覧」
  * 注意事項:
    * フィールド: bookId、bookName、publisherName、price、count、version
    * Bean Validationアノテーション: @NotNull、@Positive、@Min

---

#### T_API003_002: OrderRequest の作成

* [ ] [P] T_API003_002: OrderRequest の作成
  * 目的: 注文リクエストDTOを実装する
  * 対象: OrderRequest.java（Java Record）
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3. API一覧」
  * 注意事項:
    * フィールド: cartItems（List<CartItemRequest>）、totalPrice、deliveryPrice、deliveryAddress、settlementType
    * Bean Validationアノテーション: @NotNull、@NotEmpty、@NotBlank、@Positive

---

#### T_API003_003: OrderResponse の作成

* [ ] [P] T_API003_003: OrderResponse の作成
  * 目的: 注文レスポンスDTOを実装する
  * 対象: OrderResponse.java（Java Record）
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3. API一覧」
  * 注意事項:
    * フィールド: orderTranId、orderDate、customerId、totalPrice、deliveryPrice、deliveryAddress、settlementType
    * OrderTranエンティティから変換

---

#### T_API003_004: OrderHistoryTO の作成

* [ ] [P] T_API003_004: OrderHistoryTO の作成
  * 目的: 注文履歴DTOを実装する（非正規化形式）
  * 対象: OrderHistoryTO.java（Java Record）
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3. API一覧」
  * 注意事項:
    * フィールド: orderTranId、orderDate、customerId、totalPrice、deliveryPrice、deliveryAddress、settlementType、orderDetailId、bookId、bookName、publisherName、price、count
    * 1注文明細=1レコードの非正規化形式

---

#### T_API003_005: OrderDetailTO の作成

* [ ] [P] T_API003_005: OrderDetailTO の作成
  * 目的: 注文明細DTOを実装する
  * 対象: OrderDetailTO.java（Java Record）
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3. API一覧」
  * 注意事項:
    * フィールド: orderDetailId、bookId、bookName、publisherName、price、count
    * OrderDetailエンティティから変換

---

### 2. ビジネス例外

#### T_API003_006: OutOfStockException の作成

* [ ] [P] T_API003_006: OutOfStockException の作成
  * 目的: 在庫不足例外を実装する
  * 対象: OutOfStockException.java（RuntimeException）
  * 参照SPEC: [behaviors.md](../specs/baseline/basic_design/behaviors.md) の「4.1 注文作成」
  * 注意事項:
    * メッセージ: "在庫が不足しています: {bookName}"

---

#### T_API003_007: OutOfStockExceptionMapper の作成

* [ ] [P] T_API003_007: OutOfStockExceptionMapper の作成
  * 目的: 在庫不足例外を処理する
  * 対象: OutOfStockExceptionMapper.java（ExceptionMapper）
  * 参照SPEC: [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) の「4.5.2 Exception Mapper」
  * 注意事項:
    * @Providerアノテーションを付与
    * 409 Conflictを返却
    * MediaType.APPLICATION_JSONを明示

---

### 3. 列挙型

#### T_API003_008: SettlementType の作成

* [ ] [P] T_API003_008: SettlementType の作成
  * 目的: 決済方法列挙型を実装する
  * 対象: SettlementType.java（Enum）
  * 参照SPEC: [data_model.md](../specs/baseline/basic_design/data_model.md) の「3.6.5 決済方法」
  * 注意事項:
    * 値: BANK_TRANSFER(1)、CREDIT_CARD(2)、CASH_ON_DELIVERY(3)

---

### 4. REST Resource

#### T_API003_009: OrderResource の作成

* [ ] T_API003_009: OrderResource の作成
  * 目的: 注文APIエンドポイントを実装する
  * 対象: OrderResource.java（JAX-RS Resource）
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3. API一覧」
  * 注意事項:
    * @Path("/orders")を付与
    * @ApplicationScopedを付与
    * OrderService、AuthenContextをインジェクション
    * 実装メソッド:
      1. createOrder(OrderRequest): 注文作成
      2. getOrderHistory(): 注文履歴取得（非正規化形式）
      3. getOrderById(int orderTranId): 注文詳細取得
      4. getOrderDetail(int orderTranId, int orderDetailId): 注文明細取得
    * 認証チェック: AuthenContext.isAuthenticated()
    * エラーハンドリング:
      * 未認証: 401 Unauthorized
      * 在庫不足: OutOfStockException
      * 楽観的ロック競合: OptimisticLockException
      * 注文が見つからない: 404 Not Found
    * ログ出力: INFO（メソッド開始）、WARN（在庫不足、楽観的ロック競合）、ERROR（例外発生）

---

### 5. ユニットテスト

#### T_API003_010: OrderService のテスト（詳細）

* [ ] [P] T_API003_010: OrderService のテスト（詳細）
  * 目的: 注文処理の詳細なテストを作成する
  * 対象: OrderServiceTest.java（JUnit 5 + Mockito）
  * 参照SPEC: [behaviors.md](../specs/baseline/basic_design/behaviors.md) の「4.1 注文作成」
  * 注意事項:
    * Mockitoで外部APIクライアント、Daoをモック
    * テストケース:
      1. 正常系: 注文作成成功
      2. 異常系: 在庫不足
      3. 異常系: 楽観的ロック競合
      4. 異常系: トランザクションロールバック

---

## 成果物チェックリスト

* [ ] DTO（CartItemRequest、OrderRequest、OrderResponse、OrderHistoryTO、OrderDetailTO）が実装されている
* [ ] ビジネス例外（OutOfStockException、OutOfStockExceptionMapper）が実装されている
* [ ] 列挙型（SettlementType）が実装されている
* [ ] REST Resource（OrderResource）が実装されている
* [ ] ユニットテストが作成されている
* [ ] 注文APIが正常に動作する（注文作成、注文履歴取得、注文詳細取得、注文明細取得）
* [ ] トランザクション管理が正常に動作する
* [ ] 楽観的ロック制御が正常に動作する
* [ ] エラーハンドリングが適切に動作する

---

## 動作確認

### 注文作成（成功）

```bash
curl -X POST http://localhost:8080/berry-books-api-sdd/api/orders \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "cartItems": [
      {
        "bookId": 1,
        "bookName": "Java完全理解",
        "publisherName": "技術評論社",
        "price": 3200,
        "count": 2,
        "version": 0
      }
    ],
    "totalPrice": 6400,
    "deliveryPrice": 800,
    "deliveryAddress": "東京都渋谷区1-2-3",
    "settlementType": 1
  }'
```

### 注文履歴取得

```bash
curl -X GET http://localhost:8080/berry-books-api-sdd/api/orders/history \
  -b cookies.txt
```

### 注文詳細取得

```bash
curl -X GET http://localhost:8080/berry-books-api-sdd/api/orders/1
```

### 注文明細取得

```bash
curl -X GET http://localhost:8080/berry-books-api-sdd/api/orders/1/details/1
```

---

## 参考資料

* [functional_design.md](../specs/baseline/basic_design/functional_design.md) - 機能設計書
* [behaviors.md](../specs/baseline/basic_design/behaviors.md) - 振る舞い仕様書
* [data_model.md](../specs/baseline/basic_design/data_model.md) - データモデル仕様書
* [architecture.md](../specs/baseline/basic_design/architecture_design.md) - アーキテクチャ設計書
