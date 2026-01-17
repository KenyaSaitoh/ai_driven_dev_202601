# API_003_orders - 注文API 詳細設計書

API ID: API_003_orders  
API名: 注文API  
バージョン: 1.0.0  
最終更新日: 2026-01-18  
ステータス: 詳細設計確定

---

## 1. API概要

* ベースパス: `/api/orders`
* 認証: 注文作成、注文履歴取得は認証必須、注文詳細取得・注文明細取得は認証不要
* 実装パターン: 独自実装 + 外部API連携
  * 注文処理は本システムで実装（OrderService、OrderTranDao、OrderDetailDao使用）
  * 在庫更新はback-office-api経由で実行（楽観的ロック対応）
  * トランザクション管理が重要

---

## 2. パッケージ構造

### 2.1 API固有パッケージ

```
pro.kensait.berrybooks
├── api
│   ├── OrderResource.java               # 注文APIエンドポイント
│   ├── dto
│   │   ├── CartItemRequest.java         # カートアイテムリクエストDTO
│   │   ├── OrderRequest.java            # 注文リクエストDTO
│   │   ├── OrderResponse.java           # 注文レスポンスDTO
│   │   ├── OrderHistoryTO.java          # 注文履歴DTO（非正規化形式）
│   │   └── OrderDetailTO.java           # 注文明細DTO
│   └── exception
│       ├── OutOfStockException.java              # 在庫不足例外
│       └── OutOfStockExceptionMapper.java        # 在庫不足例外マッパー
├── service
│   └── order
│       └── OrderService.java            # 注文ビジネスロジック
└── common
    └── SettlementType.java              # 決済方法列挙型
```

注意: 共通コンポーネント（OrderTran、OrderDetail、OrderTranDao、OrderDetailDao、BackOfficeRestClient、AuthenContext、JwtAuthenFilter）は[common/detailed_design.md](../common/detailed_design.md)を参照

---

## 3. Resourceクラス設計

### 3.1 OrderResource（注文APIリソース）

#### 3.1.1 概要

* 責務: 注文APIエンドポイントの提供
* パッケージ: `pro.kensait.berrybooks.api`

#### 3.1.2 クラス定義

* クラス名: `OrderResource`
* アノテーション:
  * `@Path("/orders")` - ベースパス
  * `@ApplicationScoped` - CDI管理Bean（シングルトン）

#### 3.1.3 依存関係

* `@Inject private OrderService orderService;` - 注文ビジネスロジック
* `@Inject private AuthenContext authenContext;` - 認証コンテキスト
* `@Inject private OrderTranDao orderTranDao;` - 注文トランザクションDAO
* `@Inject private OrderDetailDao orderDetailDao;` - 注文明細DAO
* `private static final Logger logger = LoggerFactory.getLogger(OrderResource.class);`

#### 3.1.4 メソッド設計

##### createOrder(OrderRequest request) - 注文作成

* シグネチャ:
```java
@POST
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Response createOrder(@Valid OrderRequest request)
```

* 目的: 注文を作成する
* パラメータ:
  * `OrderRequest request` - 注文リクエスト（cartItems, totalPrice, deliveryPrice, deliveryAddress, settlementType）
  * `@Valid` - Bean Validation実行

* 処理フロー:
  1. ログ出力: `logger.info("[ OrderResource#createOrder ] Start")`
  2. 認証チェック: `authenContext.isAuthenticated()` - 認証済みかどうかを判定
    * 未認証の場合 → 401 Unauthorized（"認証が必要です"）
  3. 顧客ID取得: `authenContext.getCustomerId()` - AuthenContextから顧客ID取得
  4. 注文処理: `orderService.orderBooks(request, customerId)` - 注文ビジネスロジック実行
    * トランザクション内で処理（@Transactional）
    * 在庫チェック、在庫更新、注文トランザクション作成、注文明細作成
  5. OrderResponse生成: `new OrderResponse(orderTran.getOrderTranId(), orderTran.getOrderDate(), orderTran.getCustomerId(), orderTran.getTotalPrice(), orderTran.getDeliveryPrice(), orderTran.getDeliveryAddress(), orderTran.getSettlementType())`
  6. ログ出力: `logger.info("[ OrderResource#createOrder ] Order created: orderTranId={}", orderTran.getOrderTranId())`
  7. レスポンス返却: `Response.ok(response).build()`

* レスポンス: `200 OK + OrderResponse`

* エラーケース:
  * 未認証 → `401 Unauthorized`（JwtAuthenFilterで処理）
  * 在庫不足 → `409 Conflict`（OutOfStockExceptionMapper）
  * 楽観的ロック競合 → `409 Conflict`（OptimisticLockExceptionMapper）
  * バリデーションエラー → `400 Bad Request`（ValidationExceptionMapperで処理）

##### getOrderHistory() - 注文履歴取得

* シグネチャ:
```java
@GET
@Path("/history")
@Produces(MediaType.APPLICATION_JSON)
public Response getOrderHistory()
```

* 目的: ログイン中の顧客の注文履歴を取得する（非正規化形式）
* パラメータ: なし（JWT Cookieから顧客ID取得）
* 認証: 必須（JwtAuthenFilterで処理）

* 処理フロー:
  1. ログ出力: `logger.info("[ OrderResource#getOrderHistory ] Start")`
  2. 認証チェック: `authenContext.isAuthenticated()` - 認証済みかどうかを判定
    * 未認証の場合 → 401 Unauthorized（"認証が必要です"）
  3. 顧客ID取得: `authenContext.getCustomerId()` - AuthenContextから顧客ID取得
  4. 注文履歴取得: `orderTranDao.findByCustomerId(customerId)` - 顧客IDで注文履歴を検索
  5. 注文履歴が0件の場合 → 空リストを返却
  6. 各注文について、注文明細を取得: `orderDetailDao.findByOrderTranId(orderTran.getOrderTranId())`
  7. OrderHistoryTO生成（1注文明細=1レコード）:
    * 各注文明細について、OrderHistoryTOを生成
    * `new OrderHistoryTO(orderTran.getOrderTranId(), orderTran.getOrderDate(), orderTran.getCustomerId(), orderTran.getTotalPrice(), orderTran.getDeliveryPrice(), orderTran.getDeliveryAddress(), orderTran.getSettlementType(), orderDetail.getId().getOrderDetailId(), orderDetail.getBookId(), orderDetail.getBookName(), orderDetail.getPublisherName(), orderDetail.getPrice(), orderDetail.getCount())`
  8. ログ出力: `logger.info("[ OrderResource#getOrderHistory ] Found {} order history records", historyList.size())`
  9. レスポンス返却: `Response.ok(historyList).build()`

* レスポンス: `200 OK + List<OrderHistoryTO>`

* エラーケース:
  * 未認証 → `401 Unauthorized`（JwtAuthenFilterで処理）

##### getOrderById(Integer orderTranId) - 注文詳細取得

* シグネチャ:
```java
@GET
@Path("/{orderTranId}")
@Produces(MediaType.APPLICATION_JSON)
public Response getOrderById(@PathParam("orderTranId") Integer orderTranId)
```

* 目的: 指定IDの注文詳細を取得する
* パラメータ:
  * `Integer orderTranId` - 注文トランザクションID（パスパラメータ）
* 認証: 不要

* 処理フロー:
  1. ログ出力: `logger.info("[ OrderResource#getOrderById ] orderTranId={}", orderTranId)`
  2. 注文トランザクション取得: `orderTranDao.findById(orderTranId)` - 注文トランザクションIDで検索
  3. 注文が存在しない場合 → 404 Not Found（"注文が見つかりません"）
  4. 注文明細取得: `orderDetailDao.findByOrderTranId(orderTranId)` - 注文明細を検索
  5. OrderDetailTO生成:
    * 各注文明細について、OrderDetailTOを生成
    * `new OrderDetailTO(orderDetail.getId().getOrderDetailId(), orderDetail.getBookId(), orderDetail.getBookName(), orderDetail.getPublisherName(), orderDetail.getPrice(), orderDetail.getCount())`
  6. OrderResponse生成: `new OrderResponse(orderTran.getOrderTranId(), orderTran.getOrderDate(), orderTran.getCustomerId(), orderTran.getTotalPrice(), orderTran.getDeliveryPrice(), orderTran.getDeliveryAddress(), orderTran.getSettlementType(), orderDetailList)`
  7. ログ出力: `logger.info("[ OrderResource#getOrderById ] Found order: orderTranId={}", orderTranId)`
  8. レスポンス返却: `Response.ok(response).build()`

* レスポンス: `200 OK + OrderResponse（orderDetails含む）`

* エラーケース:
  * 注文が見つからない → `404 Not Found`

##### getOrderDetail(Integer orderTranId, Integer orderDetailId) - 注文明細取得

* シグネチャ:
```java
@GET
@Path("/{orderTranId}/details/{orderDetailId}")
@Produces(MediaType.APPLICATION_JSON)
public Response getOrderDetail(
    @PathParam("orderTranId") Integer orderTranId,
    @PathParam("orderDetailId") Integer orderDetailId
)
```

* 目的: 指定IDの注文明細を取得する
* パラメータ:
  * `Integer orderTranId` - 注文トランザクションID（パスパラメータ）
  * `Integer orderDetailId` - 注文明細ID（パスパラメータ）
* 認証: 不要

* 処理フロー:
  1. ログ出力: `logger.info("[ OrderResource#getOrderDetail ] orderTranId={}, orderDetailId={}", orderTranId, orderDetailId)`
  2. 複合主キー生成: `new OrderDetailPK(orderTranId, orderDetailId)`
  3. 注文明細取得: `orderDetailDao.findById(pk)` - 複合主キーで検索
  4. 注文明細が存在しない場合 → 404 Not Found（"注文明細が見つかりません"）
  5. OrderDetailTO生成: `new OrderDetailTO(orderDetail.getId().getOrderDetailId(), orderDetail.getBookId(), orderDetail.getBookName(), orderDetail.getPublisherName(), orderDetail.getPrice(), orderDetail.getCount())`
  6. ログ出力: `logger.info("[ OrderResource#getOrderDetail ] Found order detail")`
  7. レスポンス返却: `Response.ok(orderDetailTO).build()`

* レスポンス: `200 OK + OrderDetailTO`

* エラーケース:
  * 注文明細が見つからない → `404 Not Found`

---

## 4. DTO設計

### 4.1 CartItemRequest（カートアイテムリクエストDTO - Record）

#### 4.1.1 概要

* 責務: カートアイテムリクエストデータの転送
* パッケージ: `pro.kensait.berrybooks.api.dto`

#### 4.1.2 レコード定義

```java
public record CartItemRequest(
    @NotNull(message = "書籍IDは必須です")
    Integer bookId,
    
    @NotBlank(message = "書籍名は必須です")
    String bookName,
    
    @NotBlank(message = "出版社名は必須です")
    String publisherName,
    
    @NotNull(message = "価格は必須です")
    @Positive(message = "価格は正の数である必要があります")
    Integer price,
    
    @NotNull(message = "注文数は必須です")
    @Min(value = 1, message = "注文数は1以上である必要があります")
    Integer count,
    
    @NotNull(message = "バージョンは必須です")
    Long version
) {}
```

#### 4.1.3 フィールド

* bookId: 書籍ID
* bookName: 書籍名（スナップショット用）
* publisherName: 出版社名（スナップショット用）
* price: 価格（スナップショット用）
* count: 注文数
* version: バージョン番号（楽観的ロック用）

---

### 4.2 OrderRequest（注文リクエストDTO - Record）

#### 4.2.1 概要

* 責務: 注文リクエストデータの転送
* パッケージ: `pro.kensait.berrybooks.api.dto`

#### 4.2.2 レコード定義

```java
public record OrderRequest(
    @NotNull(message = "カート項目は必須です")
    @NotEmpty(message = "カート項目は空にできません")
    List<CartItemRequest> cartItems,
    
    @NotNull(message = "合計金額は必須です")
    @Positive(message = "合計金額は正の数である必要があります")
    Integer totalPrice,
    
    @NotNull(message = "配送料金は必須です")
    @PositiveOrZero(message = "配送料金は0以上である必要があります")
    Integer deliveryPrice,
    
    @NotBlank(message = "配送先住所は必須です")
    String deliveryAddress,
    
    @NotNull(message = "決済方法は必須です")
    Integer settlementType
) {}
```

#### 4.2.3 フィールド

* cartItems: カート項目リスト
* totalPrice: 合計金額（配送料を含む）
* deliveryPrice: 配送料金
* deliveryAddress: 配送先住所
* settlementType: 決済方法（1:銀行振込, 2:クレジットカード, 3:着払い）

---

### 4.3 OrderResponse（注文レスポンスDTO - Record）

#### 4.3.1 概要

* 責務: 注文レスポンスデータの転送
* パッケージ: `pro.kensait.berrybooks.api.dto`

#### 4.3.2 レコード定義

```java
public record OrderResponse(
    Integer orderTranId,
    LocalDate orderDate,
    Integer customerId,
    Integer totalPrice,
    Integer deliveryPrice,
    String deliveryAddress,
    Integer settlementType,
    List<OrderDetailTO> orderDetails  // 注文詳細取得時のみ使用
) {
    // 注文作成時用のコンストラクタ（orderDetailsなし）
    public OrderResponse(
        Integer orderTranId,
        LocalDate orderDate,
        Integer customerId,
        Integer totalPrice,
        Integer deliveryPrice,
        String deliveryAddress,
        Integer settlementType
    ) {
        this(orderTranId, orderDate, customerId, totalPrice, deliveryPrice, deliveryAddress, settlementType, null);
    }
}
```

#### 4.3.3 フィールド

* orderTranId: 注文トランザクションID
* orderDate: 注文日
* customerId: 顧客ID
* totalPrice: 合計金額
* deliveryPrice: 配送料金
* deliveryAddress: 配送先住所
* settlementType: 決済方法
* orderDetails: 注文明細リスト（注文詳細取得時のみ）

---

### 4.4 OrderHistoryTO（注文履歴DTO - Record）

#### 4.4.1 概要

* 責務: 注文履歴データの転送（1注文明細=1レコードの非正規化形式）
* パッケージ: `pro.kensait.berrybooks.api.dto`

#### 4.4.2 レコード定義

```java
public record OrderHistoryTO(
    Integer orderTranId,
    LocalDate orderDate,
    Integer customerId,
    Integer totalPrice,
    Integer deliveryPrice,
    String deliveryAddress,
    Integer settlementType,
    Integer orderDetailId,
    Integer bookId,
    String bookName,
    String publisherName,
    Integer price,
    Integer count
) {}
```

#### 4.4.3 フィールド

* 注文トランザクション情報:
  * orderTranId: 注文トランザクションID
  * orderDate: 注文日
  * customerId: 顧客ID
  * totalPrice: 合計金額
  * deliveryPrice: 配送料金
  * deliveryAddress: 配送先住所
  * settlementType: 決済方法
* 注文明細情報:
  * orderDetailId: 注文明細ID
  * bookId: 書籍ID
  * bookName: 書籍名
  * publisherName: 出版社名
  * price: 価格
  * count: 注文数

---

### 4.5 OrderDetailTO（注文明細DTO - Record）

#### 4.5.1 概要

* 責務: 注文明細データの転送
* パッケージ: `pro.kensait.berrybooks.api.dto`

#### 4.5.2 レコード定義

```java
public record OrderDetailTO(
    Integer orderDetailId,
    Integer bookId,
    String bookName,
    String publisherName,
    Integer price,
    Integer count
) {}
```

#### 4.5.3 フィールド

* orderDetailId: 注文明細ID
* bookId: 書籍ID
* bookName: 書籍名
* publisherName: 出版社名
* price: 価格
* count: 注文数

---

## 5. ビジネスロジック層（Service）

### 5.1 OrderService（注文ビジネスロジック）

#### 5.1.1 概要

* 責務: 注文ビジネスロジックの実行
* パッケージ: `pro.kensait.berrybooks.service.order`

#### 5.1.2 クラス定義

* クラス名: `OrderService`
* アノテーション:
  * `@ApplicationScoped` - CDI管理Bean（シングルトン）

#### 5.1.3 依存関係

* `@Inject private OrderTranDao orderTranDao;` - 注文トランザクションDAO
* `@Inject private OrderDetailDao orderDetailDao;` - 注文明細DAO
* `@Inject private BackOfficeRestClient backOfficeClient;` - back-office-api連携クライアント
* `private static final Logger logger = LoggerFactory.getLogger(OrderService.class);`

#### 5.1.4 メソッド設計

##### orderBooks(OrderRequest request, Integer customerId)

* シグネチャ:
```java
@Transactional
public OrderTran orderBooks(OrderRequest request, Integer customerId)
```

* 目的: 注文処理を実行する
* アノテーション: `@Transactional` - トランザクション境界
* パラメータ:
  * `OrderRequest request` - 注文リクエスト
  * `Integer customerId` - 顧客ID

* 処理フロー:
  1. ログ出力: `logger.info("[ OrderService#orderBooks ] Start: customerId={}", customerId)`
  2. 各カートアイテムについて処理:
    a. 在庫確認: `backOfficeClient.findStockById(cartItem.bookId())`
      * back-office-apiから在庫情報を取得
    b. 在庫数チェック: `stock.quantity() < cartItem.count()`
      * 在庫不足の場合 → OutOfStockExceptionをスロー（"在庫が不足しています: " + cartItem.bookName()）
    c. 在庫更新: `backOfficeClient.updateStock(cartItem.bookId(), cartItem.version(), stock.quantity() - cartItem.count())`
      * back-office-apiで在庫を更新（楽観的ロック）
      * 楽観的ロック競合の場合 → OptimisticLockExceptionがback-office-apiからスローされる
  3. 注文トランザクション作成:
    a. OrderTranエンティティ生成: `new OrderTran()`
      * orderDate: LocalDate.now()
      * customerId: パラメータから設定
      * totalPrice: request.totalPrice()
      * deliveryPrice: request.deliveryPrice()
      * deliveryAddress: request.deliveryAddress()
      * settlementType: request.settlementType()
    b. 注文トランザクション登録: `orderTranDao.insert(orderTran)`
      * orderTranIdが自動採番される
  4. 注文明細作成:
    a. 各カートアイテムについて、OrderDetailエンティティを生成:
      * 複合主キー: `new OrderDetailPK(orderTran.getOrderTranId(), orderDetailId)`
      * orderDetailId: 1から連番（1, 2, 3, ...）
      * orderTran: 親エンティティの参照
      * bookId: cartItem.bookId()
      * bookName: cartItem.bookName()（スナップショット）
      * publisherName: cartItem.publisherName()（スナップショット）
      * price: cartItem.price()（スナップショット）
      * count: cartItem.count()
    b. 注文明細登録: `orderDetailDao.insert(orderDetail)`
  5. ログ出力: `logger.info("[ OrderService#orderBooks ] Order created: orderTranId={}", orderTran.getOrderTranId())`
  6. 注文トランザクションを返却

* 戻り値: OrderTran（作成された注文トランザクション）

* 例外:
  * `OutOfStockException` - 在庫不足の場合
  * `OptimisticLockException` - 楽観的ロック競合の場合（back-office-apiから）

* トランザクション:
  * `@Transactional`により、注文トランザクション作成と注文明細作成が同一トランザクション内で実行される
  * 例外発生時は自動的にロールバックされる

---

## 6. 例外設計

### 6.1 OutOfStockException（在庫不足例外）

#### 6.1.1 概要

* 責務: 在庫不足エラーの表現
* パッケージ: `pro.kensait.berrybooks.api.exception`

#### 6.1.2 クラス定義

* クラス名: `OutOfStockException`
* 継承: `RuntimeException`

#### 6.1.3 コンストラクタ

```java
public OutOfStockException(String message) {
    super(message);
}
```

* パラメータ:
  * `String message` - エラーメッセージ（例: "在庫が不足しています: Java完全理解"）

---

### 6.2 OutOfStockExceptionMapper（在庫不足例外マッパー）

#### 6.2.1 概要

* 責務: OutOfStockExceptionを409 Conflictレスポンスに変換
* パッケージ: `pro.kensait.berrybooks.api.exception`

#### 6.2.2 クラス定義

* クラス名: `OutOfStockExceptionMapper`
* 実装インターフェース: `ExceptionMapper<OutOfStockException>`
* アノテーション:
  * `@Provider` - JAX-RS Exception Mapper登録

#### 6.2.3 メソッド設計

##### toResponse(OutOfStockException exception)

* シグネチャ:
```java
@Override
public Response toResponse(OutOfStockException exception)
```

* 目的: OutOfStockExceptionを409 Conflictレスポンスに変換
* パラメータ:
  * `OutOfStockException exception` - 在庫不足例外

* 処理フロー:
  1. ログ出力: `logger.warn("[ OutOfStockExceptionMapper ] Out of stock: {}", exception.getMessage())`
  2. ErrorResponse生成: `new ErrorResponse(409, "Conflict", exception.getMessage(), null)`
  3. レスポンス返却: `Response.status(Response.Status.CONFLICT).entity(errorResponse).type(MediaType.APPLICATION_JSON).build()`

* レスポンス: `409 Conflict + ErrorResponse`

---

## 7. 列挙型（Enum）

### 7.1 SettlementType（決済方法）

#### 7.1.1 概要

* 責務: 決済方法の定数定義
* パッケージ: `pro.kensait.berrybooks.common`

#### 7.1.2 列挙定義

```java
public enum SettlementType {
    BANK_TRANSFER(1, "銀行振込"),
    CREDIT_CARD(2, "クレジットカード"),
    CASH_ON_DELIVERY(3, "着払い");
    
    private final int value;
    private final String label;
    
    SettlementType(int value, String label) {
        this.value = value;
        this.label = label;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getLabel() {
        return label;
    }
    
    public static SettlementType fromValue(int value) {
        for (SettlementType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid settlement type: " + value);
    }
}
```

#### 7.1.3 定数

* BANK_TRANSFER(1, "銀行振込")
* CREDIT_CARD(2, "クレジットカード")
* CASH_ON_DELIVERY(3, "着払い")

#### 7.1.4 メソッド

* getValue(): 値を取得
* getLabel(): ラベルを取得
* fromValue(int value): 値から列挙型を取得

---

## 8. 外部API連携

### 8.1 BackOfficeRestClient（back-office-api連携クライアント）

* 詳細: [common/detailed_design.md](../common/detailed_design.md)の「外部API連携層」を参照

#### 8.1.1 使用するメソッド

##### findStockById(Integer bookId)

* 目的: 在庫情報を取得
* エンドポイント: `GET /stocks/{bookId}`
* 戻り値: `StockTO（bookId, quantity, version）`

##### updateStock(Integer bookId, Long version, Integer newQuantity)

* 目的: 在庫を更新（楽観的ロック）
* エンドポイント: `PUT /stocks/{bookId}`
* リクエスト: `{"version": version, "quantity": newQuantity}`
* 戻り値: `StockTO（更新後）`
* 例外: `OptimisticLockException` - version不一致の場合

---

## 9. 設計上の重要なポイント

### 9.1 トランザクション管理

* `OrderService.orderBooks()`メソッドに`@Transactional`を適用
* 外部API呼び出し（在庫更新）→ローカルDB更新（注文トランザクション、注文明細）の順で実行
* 例外発生時は自動的にロールバック（ローカルDBのみ）

### 9.2 分散トランザクション

* 在庫更新（back-office-api）と注文作成（berry-books-api）は分離されたトランザクション
* 在庫更新成功後、注文作成に失敗した場合、在庫の不整合が発生する可能性がある
* 現時点では許容（将来的にSagaパターンや補償トランザクションで対応予定）

### 9.3 楽観的ロック制御

* 在庫の楽観的ロックはback-office-api側で管理
* berry-books-apiはバージョン番号を転送するだけ
* back-office-apiがOptimisticLockExceptionをスローする

### 9.4 スナップショットパターン

* ORDER_DETAILテーブルにbookName、publisherName、priceを保存
* 注文時点の情報を保持し、書籍マスタの変更・削除の影響を受けない
* 注文履歴表示時の外部API呼び出しを削減

### 9.5 非正規化形式

* 注文履歴取得: 1注文明細=1レコードの非正規化形式で返却
* フロントエンドでの表示を容易にする

---

## 10. 参考資料

### 10.1 関連仕様書

* [functional_design.md](../../basic_design/functional_design.md) - 機能設計書
* [behaviors.md](../../basic_design/behaviors.md) - 振る舞い仕様書
* [data_model.md](../../basic_design/data_model.md) - データモデル仕様書
* [architecture_design.md](../../basic_design/architecture_design.md) - アーキテクチャ設計書
* [external_interface.md](../../basic_design/external_interface.md) - 外部インターフェース仕様書

### 10.2 共通コンポーネント

* [common/detailed_design.md](../common/detailed_design.md) - 共通機能詳細設計書

### 10.3 タスクリスト

* [tasks/API_003_orders.md](../../../tasks/API_003_orders.md) - 注文APIタスクリスト

### 10.4 プロジェクト情報

* [README.md](../../../README.md) - プロジェクトREADME
