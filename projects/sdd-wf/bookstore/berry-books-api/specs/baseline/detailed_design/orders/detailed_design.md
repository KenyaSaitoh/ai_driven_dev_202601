# orders - ドメイン詳細設計書

ドメイン名: orders  
バージョン: 1.0.0  
最終更新: 2026-02-07

---

## 重要な原則

この詳細設計書は、**基本設計（what/why）と実装コード（how）の橋渡しとなる設計判断のみを簡潔に記載**します。

**記載すべき情報**:
* クラス名と責務（1行）
* 主要メソッドのシグネチャ（引数、戻り値、例外）
* 設計判断を示すアノテーション（@Transactional, @Path等）
* JPQLクエリ（WHERE句、JOIN等の設計判断）
* 依存関係（@Inject対象）

**記載すべきでない情報**:
* メソッドの実装詳細、処理ステップ
* すべてのフィールド定義、getter/setter
* バリデーションの詳細
* 基本設計SPECの内容の繰り返し

---

## 1. ドメイン概要

* ドメイン名: orders
* 責務: 注文作成、在庫確認・更新（外部API連携）、注文履歴取得
* 依存関係: commonドメインに依存（Entity、Dao、JWT認証、外部API連携）

---

## 2. クラス構成

### 2.1 パッケージ構造

```
pro.kensait.berrybooks
├── api/                          # JAX-RS Resources
│   ├── OrderResource.java        # 注文API
│   └── dto/
│       ├── OrderRequest.java     # 注文作成リクエスト
│       ├── OrderResponse.java    # 注文作成レスポンス
│       └── CartItemRequest.java  # カートアイテムDTO
├── service/
│   └── order/
│       ├── OrderService.java     # 注文処理サービス
│       └── DeliveryFeeService.java  # 配送料金計算
├── dao/                          # commonドメインで定義
│   ├── OrderTranDao.java
│   └── OrderDetailDao.java
├── entity/                       # commonドメインで定義
│   ├── OrderTran.java
│   ├── OrderDetail.java
│   └── OrderDetailPK.java
├── external/                     # commonドメインで定義
│   └── BackOfficeRestClient.java
└── security/                     # commonドメインで定義
    └── AuthenContext.java
```

---

## 3. コンポーネント設計

### 3.1 Resource - OrderResource

**責務**: 注文APIエンドポイントの提供（認証必須）

**アノテーション**:
* `@Path("/orders")` - エンドポイントパス
* `@ApplicationScoped` - アプリケーションスコープ
* `@Produces(MediaType.APPLICATION_JSON)` - JSON応答
* `@Consumes(MediaType.APPLICATION_JSON)` - JSONリクエスト

**依存関係**:
* `@Inject OrderService orderService` - 注文処理サービス
* `@Inject AuthenContext authenContext` - 認証コンテキスト（@RequestScoped）

**主要メソッド**:

#### createOrder

```java
@POST
public Response createOrder(@Valid OrderRequest request) throws OutOfStockException, OptimisticLockException
```

* **目的**: 注文作成（認証必須、在庫確認・更新を含む）
* **認証**: AuthenContextからcustomerIdを取得
* **戻り値**: 201 Created + OrderResponse
* **例外**: OutOfStockException（在庫不足、409 Conflict）、OptimisticLockException（楽観的ロック競合、409 Conflict）

#### getOrderHistory

```java
@GET
@Path("/history")
public Response getOrderHistory()
```

* **目的**: ログインユーザーの注文履歴を取得（認証必須）
* **認証**: AuthenContextからcustomerIdを取得
* **戻り値**: 200 OK + List<OrderResponse>
* **最大件数**: 100件（将来的にページネーション対応）

#### getOrderById

```java
@GET
@Path("/{tranId}")
public Response getOrderById(@PathParam("tranId") Integer tranId)
```

* **目的**: 注文詳細を取得（認証不要、公開API）
* **戻り値**: 200 OK + OrderResponse、404 Not Found

#### getOrderDetailById

```java
@GET
@Path("/{tranId}/details/{detailId}")
public Response getOrderDetailById(
    @PathParam("tranId") Integer tranId,
    @PathParam("detailId") Integer detailId
)
```

* **目的**: 注文明細を取得（認証不要、公開API）
* **戻り値**: 200 OK + OrderDetailResponse、404 Not Found

---

### 3.2 Service - OrderService

**責務**: 注文処理のビジネスロジック（トランザクション境界）

**アノテーション**:
* `@ApplicationScoped` - アプリケーションスコープ
* `@Transactional` - トランザクション管理（注文作成・明細作成）

**依存関係**:
* `@Inject OrderTranDao orderTranDao` - 注文データアクセス
* `@Inject OrderDetailDao orderDetailDao` - 注文明細データアクセス
* `@Inject BackOfficeRestClient backOfficeClient` - 外部API（在庫管理）
* `@Inject DeliveryFeeService deliveryFeeService` - 配送料金計算

**主要メソッド**:

#### createOrder

```java
@Transactional
public OrderTran createOrder(OrderRequest request, Integer customerId) 
    throws OutOfStockException, OptimisticLockException
```

* **目的**: 注文作成（在庫確認・更新、注文トランザクション作成、注文明細作成）
* **トランザクション境界**: このメソッド全体
* **処理フロー**:
  1. カートアイテムごとに在庫確認（backOfficeClient.findStockById）
  2. 在庫数が注文数以上であることを確認（不足時はOutOfStockExceptionをスロー）
  3. 在庫更新（backOfficeClient.updateStock、楽観的ロック対応）
  4. 注文トランザクション作成（orderTranDao.insert）
  5. 注文明細作成（orderDetailDao.insert × N）
  6. コミット（正常終了）、ロールバック（例外発生時）
* **外部APIタイムアウト**: 10秒（MicroProfile Configで設定可能）
* **例外**: OutOfStockException、OptimisticLockException

**注意**: 在庫更新（外部API）が成功し、注文作成（ローカルDB）が失敗した場合、在庫の補償トランザクションは現時点では未実装（将来対応としてSagaパターン導入を検討）

#### getOrderHistory

```java
public List<OrderTran> getOrderHistory(Integer customerId)
```

* **目的**: 顧客の注文履歴を取得
* **JPQLクエリ**:
  ```sql
  SELECT o FROM OrderTran o 
  WHERE o.customerId = :customerId 
  ORDER BY o.orderDate DESC
  ```
* **最大件数**: 100件（将来的にページネーション対応）

#### getOrderById

```java
public OrderTran getOrderById(Integer tranId)
```

* **目的**: 注文詳細を取得（注文明細を含む）
* **JPQLクエリ**:
  ```sql
  SELECT o FROM OrderTran o 
  LEFT JOIN FETCH o.orderDetails 
  WHERE o.orderTranId = :tranId
  ```

---

### 3.3 Service - DeliveryFeeService

**責務**: 配送料金の計算

**アノテーション**:
* `@ApplicationScoped` - アプリケーションスコープ

**依存関係**: なし

**主要メソッド**:

#### calculateDeliveryFee

```java
public int calculateDeliveryFee(String address, int totalPrice)
```

* **目的**: 住所と注文金額から配送料金を計算
* **ロジック**: 
  * 注文金額が5000円以上の場合は送料無料
  * 北海道・沖縄の場合は1500円
  * その他の地域は800円
* **戻り値**: 配送料金（円）

---

### 3.4 Dao - OrderTranDao

**責務**: ORDER_TRANテーブルのCRUD操作

**アノテーション**:
* `@ApplicationScoped` - アプリケーションスコープ

**依存関係**:
* `@PersistenceContext EntityManager em` - JPAエンティティマネージャー

**主要メソッド**:

#### insert

```java
public OrderTran insert(OrderTran orderTran)
```

* **目的**: 注文トランザクションを作成
* **戻り値**: 永続化されたOrderTran（orderTranId自動採番済み）

#### findById

```java
public OrderTran findById(Integer orderTranId)
```

* **目的**: 注文トランザクションIDで検索
* **JPQLクエリ**:
  ```sql
  SELECT o FROM OrderTran o 
  LEFT JOIN FETCH o.orderDetails 
  WHERE o.orderTranId = :orderTranId
  ```

#### findByCustomerId

```java
public List<OrderTran> findByCustomerId(Integer customerId)
```

* **目的**: 顧客IDで注文履歴を検索
* **JPQLクエリ**:
  ```sql
  SELECT o FROM OrderTran o 
  WHERE o.customerId = :customerId 
  ORDER BY o.orderDate DESC
  ```
* **最大件数**: 100件（setMaxResults）

---

### 3.5 Dao - OrderDetailDao

**責務**: ORDER_DETAILテーブルのCRUD操作

**アノテーション**:
* `@ApplicationScoped` - アプリケーションスコープ

**依存関係**:
* `@PersistenceContext EntityManager em` - JPAエンティティマネージャー

**主要メソッド**:

#### insert

```java
public OrderDetail insert(OrderDetail orderDetail)
```

* **目的**: 注文明細を作成
* **戻り値**: 永続化されたOrderDetail

#### findById

```java
public OrderDetail findById(OrderDetailPK pk)
```

* **目的**: 複合主キーで注文明細を検索
* **JPQLクエリ**:
  ```sql
  SELECT d FROM OrderDetail d 
  WHERE d.orderDetailPK.orderTranId = :orderTranId 
    AND d.orderDetailPK.orderDetailId = :orderDetailId
  ```

---

## 4. DTO設計

### 4.1 OrderRequest

**目的**: 注文作成リクエスト

**フィールド**:
* `List<CartItemRequest> cartItems` - カートアイテムリスト
* `Integer totalPrice` - 注文金額合計
* `Integer deliveryPrice` - 配送料金
* `String deliveryAddress` - 配送先住所
* `Integer settlementType` - 決済方法（1:銀行振込, 2:クレジットカード, 3:着払い）

**バリデーション**:
* `@NotNull` on cartItems
* `@NotEmpty` on cartItems
* `@Valid` on cartItems（ネストされたバリデーション）
* `@NotNull` on totalPrice
* `@Min(0)` on totalPrice
* `@NotNull` on deliveryPrice
* `@Min(0)` on deliveryPrice
* `@NotBlank` on deliveryAddress
* `@Size(max = 120)` on deliveryAddress
* `@NotNull` on settlementType
* `@Min(1)` on settlementType
* `@Max(3)` on settlementType

---

### 4.2 CartItemRequest

**目的**: カートアイテムDTO（注文明細1件分）

**フィールド**:
* `Integer bookId` - 書籍ID
* `String bookName` - 書籍名（スナップショット）
* `String publisherName` - 出版社名（スナップショット）
* `Integer price` - 価格（スナップショット）
* `Integer count` - 注文数
* `Long version` - バージョン番号（楽観的ロック用）

**バリデーション**:
* `@NotNull` on bookId
* `@NotBlank` on bookName
* `@NotBlank` on publisherName
* `@NotNull` on price
* `@Min(0)` on price
* `@NotNull` on count
* `@Min(1)` on count
* `@NotNull` on version

---

### 4.3 OrderResponse

**目的**: 注文作成レスポンス、注文履歴レスポンス

**フィールド**:
* `Integer orderTranId` - 注文トランザクションID
* `LocalDate orderDate` - 注文日
* `Integer customerId` - 顧客ID
* `Integer totalPrice` - 注文金額合計
* `Integer deliveryPrice` - 配送料金
* `String deliveryAddress` - 配送先住所
* `Integer settlementType` - 決済方法
* `List<OrderDetailResponse> orderDetails` - 注文明細リスト

---

### 4.4 OrderDetailResponse

**目的**: 注文明細レスポンス

**フィールド**:
* `Integer orderDetailId` - 注文明細ID
* `Integer bookId` - 書籍ID
* `String bookName` - 書籍名（スナップショット）
* `String publisherName` - 出版社名（スナップショット）
* `Integer price` - 価格（スナップショット）
* `Integer count` - 注文数

---

## 5. エンティティ設計（commonドメインで定義済み）

### 5.1 OrderTran

**テーブル名**: `ORDER_TRAN`

**主要フィールド**:
* `Integer orderTranId` - `@Id, @GeneratedValue(strategy = GenerationType.IDENTITY)`
* `LocalDate orderDate` - `@Column(name = "ORDER_DATE")`
* `Integer customerId` - `@Column(name = "CUSTOMER_ID")`（外部キー制約なし、論理参照のみ）
* `Integer totalPrice` - `@Column(name = "TOTAL_PRICE")`
* `Integer deliveryPrice` - `@Column(name = "DELIVERY_PRICE")`
* `String deliveryAddress` - `@Column(name = "DELIVERY_ADDRESS")`
* `Integer settlementType` - `@Column(name = "SETTLEMENT_TYPE")`

**リレーション**:
* `@OneToMany(mappedBy = "orderTran", cascade = CascadeType.ALL)` - OrderDetail（注文明細）

**アノテーション**:
* `@Entity`
* `@Table(name = "ORDER_TRAN")`

---

### 5.2 OrderDetail

**テーブル名**: `ORDER_DETAIL`

**主要フィールド**:
* `OrderDetailPK orderDetailPK` - `@EmbeddedId`（複合主キー）
* `Integer bookId` - `@Column(name = "BOOK_ID")`（外部キー制約なし、論理参照のみ）
* `String bookName` - `@Column(name = "BOOK_NAME")`（スナップショット）
* `String publisherName` - `@Column(name = "PUBLISHER_NAME")`（スナップショット）
* `Integer price` - `@Column(name = "PRICE")`（スナップショット）
* `Integer count` - `@Column(name = "COUNT")`

**リレーション**:
* `@ManyToOne` - OrderTran
* `@JoinColumn(name = "ORDER_TRAN_ID", insertable = false, updatable = false)`

**アノテーション**:
* `@Entity`
* `@Table(name = "ORDER_DETAIL")`

---

### 5.3 OrderDetailPK

**目的**: 注文明細の複合主キー

**フィールド**:
* `Integer orderTranId` - `@Column(name = "ORDER_TRAN_ID")`
* `Integer orderDetailId` - `@Column(name = "ORDER_DETAIL_ID")`

**アノテーション**:
* `@Embeddable`

---

## 6. 外部API連携（commonドメインで定義済み）

### 6.1 BackOfficeRestClient

**連携先**: back-office-api（書籍・在庫管理）

**主要メソッド**:

#### findStockById

```java
public StockTO findStockById(Integer bookId)
```

* **エンドポイント**: `GET /stocks/{bookId}`
* **目的**: 在庫情報を取得
* **タイムアウト**: 10秒
* **戻り値**: StockTO（bookId, quantity, version）
* **例外**: WebApplicationException（404 Not Found等）

#### updateStock

```java
public StockTO updateStock(Integer bookId, Long version, Integer newQuantity) 
    throws OptimisticLockException
```

* **エンドポイント**: `PUT /stocks/{bookId}`
* **目的**: 在庫数を更新（楽観的ロック対応）
* **リクエストボディ**: `{"quantity": newQuantity, "version": version}`
* **タイムアウト**: 10秒
* **戻り値**: 更新後のStockTO
* **例外**: WebApplicationException（409 Conflict = OptimisticLockException）

---

## 7. トランザクション設計

* **トランザクション境界**: OrderService層
* **伝播レベル**: REQUIRED（デフォルト）
* **分離レベル**: READ_COMMITTED（デフォルト）

**トランザクション範囲**:
* ローカルDB操作（注文トランザクション作成、注文明細作成）のみトランザクション管理
* 外部API呼び出し（在庫確認・更新）は外部APIの独立トランザクション

**エラーハンドリング**:
* OutOfStockException: 在庫不足時にスロー、トランザクションロールバック、409 Conflict
* OptimisticLockException: 楽観的ロック競合時にスロー、トランザクションロールバック、409 Conflict
* その他の例外: トランザクションロールバック、500 Internal Server Error

**将来対応**:
* 在庫更新（外部API）成功後、注文作成（ローカルDB）失敗時の補償トランザクション（Sagaパターン）
* 外部APIのリトライ処理（指数バックオフ）

---

## 8. セキュリティ設計

**認証必須エンドポイント**:
* POST /api/orders（注文作成）
* GET /api/orders/history（注文履歴取得）

**認証不要エンドポイント**:
* GET /api/orders/{tranId}（注文詳細取得）
* GET /api/orders/{tranId}/details/{detailId}（注文明細取得）

**認証コンテキスト**:
* AuthenContext（@RequestScoped）からcustomerIdを取得
* 注文履歴取得時は、ログインユーザーのcustomerIdでフィルタリング

---

## 9. パフォーマンス考慮事項

**最大件数制限**:
* 注文履歴取得: 100件（将来的にページネーション対応）

**タイムアウト設定**:
* 外部API呼び出し: 10秒（MicroProfile Configで設定可能）

**インデックス**:
* ORDER_TRAN.CUSTOMER_ID（注文履歴検索の最適化）

**N+1問題の回避**:
* 注文詳細取得時は LEFT JOIN FETCH で注文明細を一括取得

---

## 10. 参考資料

* [behaviors.md](behaviors.md) - 単体テスト用振る舞い仕様書
* `../../basic_design/orders/functional_design.md` - ドメイン機能設計書
* `../../basic_design/orders/behaviors.md` - ドメイン振る舞い仕様書（結合テスト用）
* `../../basic_design/common/data_model.md` - データモデル仕様書
* `../../basic_design/common/external_interface.md` - 外部インターフェース仕様書
