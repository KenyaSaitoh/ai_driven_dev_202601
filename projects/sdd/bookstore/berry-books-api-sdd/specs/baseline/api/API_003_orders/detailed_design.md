# API_003 注文API - 詳細設計書（BFFパターン - 独自実装）

* API ID: API_003  
* API名: 注文API  
* パターン: BFF（Backend for Frontend） - 独自実装 + 外部API連携  
* バージョン: 1.0.0  
* 最終更新: 2025-01-10

---

## 1. パッケージ構造

### 1.1 関連パッケージ

```
pro.kensait.berrybooks
├── api
│   ├── OrderResource.java            # 注文リソース
│   ├── dto
│   │   ├── OrderRequest.java         # 注文リクエスト
│   │   ├── OrderResponse.java        # 注文レスポンス
│   │   ├── OrderHistoryResponse.java # 注文履歴レスポンス
│   │   ├── OrderDetailResponse.java  # 注文明細レスポンス
│   │   └── CartItemRequest.java      # カートアイテムリクエスト
│   └── exception
│       ├── OutOfStockExceptionMapper.java          # 在庫切れ例外マッパー
│       └── OptimisticLockExceptionMapper.java      # 楽観的ロック例外マッパー
├── service
│   ├── order
│   │   ├── OrderService.java         # 注文サービス
│   │   ├── OrderServiceIF.java       # 注文サービスインターフェース
│   │   ├── OrderTO.java              # 注文転送オブジェクト（POJO）
│   │   ├── OrderHistoryTO.java       # 注文履歴DTO（Record）
│   │   ├── OrderSummaryTO.java       # 注文サマリーDTO（Record）
│   │   ├── CartItem.java             # カートアイテム（POJO）
│   │   └── OutOfStockException.java  # 在庫切れ例外
│   └── delivery
│       └── DeliveryFeeService.java   # 配送料金計算サービス
├── dao
│   ├── OrderTranDao.java             # 注文トランザクションDAO
│   └── OrderDetailDao.java           # 注文明細DAO
├── entity
│   ├── OrderTran.java                # 注文トランザクションエンティティ
│   ├── OrderDetail.java              # 注文明細エンティティ
│   └── OrderDetailPK.java            # 注文明細複合キー
├── external
│   ├── BackOfficeRestClient.java   # バックオフィスAPI連携（在庫更新）
│   └── dto
│       ├── StockTO.java              # 在庫転送オブジェクト
│       └── StockUpdateRequest.java   # 在庫更新リクエスト
└── common
    └── SettlementType.java           # 決済方法列挙型
```

---

## 2. クラス設計

### 2.1 OrderResource（JAX-RS Resource）

* 責務: 注文APIのエンドポイント提供

* アノテーション:
  * `@Path("/orders")` - ベースパス
  * `@ApplicationScoped` - CDIスコープ

* 主要メソッド:

#### createOrder() - 注文作成

```
@POST
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
```

* 認証: 必須（JwtAuthenFilterで検証）

* パラメータ:
  * `OrderRequest request` - 注文リクエスト

* 処理フロー:
  1. AuthenContextから顧客IDを取得
  2. OrderServiceで注文処理を実行
   * 在庫チェック（BackOfficeRestClient経由）
   * 在庫更新（BackOfficeRestClient経由、楽観的ロック）
   * 注文トランザクション作成（ローカルDB）
   * 注文明細作成（ローカルDB）
  3. OrderResponseを返却

* レスポンス: `OrderResponse`

#### getOrderHistory() - 注文履歴取得

```
@GET
@Path("/history")
@Produces(MediaType.APPLICATION_JSON)
```

* 認証: 必須

* 処理フロー:
  1. AuthenContextから顧客IDを取得
  2. OrderServiceで注文履歴を取得
  3. OrderHistoryResponseリストを返却

* レスポンス: `List<OrderHistoryResponse>`

#### getOrderById() - 注文詳細取得

```
@GET
@Path("/{tranId}")
@Produces(MediaType.APPLICATION_JSON)
```

* パラメータ:
  * `@PathParam("tranId") Integer tranId` - 注文ID

* レスポンス: `OrderResponse`

---

### 2.2 OrderRequest（DTO - Record）

```java
public record OrderRequest(
    @NotNull @Size(min = 1)
    List<CartItemRequest> cartItems,
    
    @NotNull @Min(0)
    Integer totalPrice,
    
    @NotNull @Min(0)
    Integer deliveryPrice,
    
    @NotBlank
    String deliveryAddress,
    
    @NotNull
    Integer settlementType  // 1: クレジットカード, 2: 代金引換
) {}
```

### 2.3 CartItemRequest（DTO - Record）

```java
public record CartItemRequest(
    @NotNull
    Integer bookId,
    
    @NotBlank
    String bookName,
    
    @NotBlank
    String publisherName,
    
    @NotNull @Min(0)
    Integer price,
    
    @NotNull @Min(1)
    Integer count,
    
    @NotNull
    Integer version  // 楽観的ロックバージョン
) {}
```

### 2.4 OrderResponse（DTO - Record）

```java
public record OrderResponse(
    Integer orderTranId,
    Integer customerId,
    LocalDateTime orderDate,
    Integer totalPrice,
    Integer deliveryPrice,
    String deliveryAddress,
    Integer settlementType,
    List<OrderDetailResponse> orderDetails
) {}
```

* 注: フィールド名は SPA の期待する構造に合わせています（`orderTranId`, `orderDate`, `orderDetails`）。

---

### 2.5 OrderService（ビジネスロジック層）

* 責務: 注文処理ビジネスロジック（BFF独自実装）

* アノテーション:
  * `@ApplicationScoped`

* 主要メソッド:

#### orderBooks()

* シグネチャ:
```java
@Transactional
public OrderTO orderBooks(OrderTO orderTO)
```

* 処理フロー:
  1. 在庫チェック（BackOfficeRestClient経由）
   ```java
   for (CartItem item : orderTO.getCartItems()) {
       BookTO book = backOfficeClient.findBookById(item.getBookId());
       if (book.getQuantity() < item.getCount()) {
           throw new OutOfStockException(item.getBookId(), item.getBookName());
       }
   }
   ```

2. 在庫更新（BackOfficeRestClient経由、楽観的ロック）
   ```java
   for (CartItem item : orderTO.getCartItems()) {
       BookTO book = backOfficeClient.findBookById(item.getBookId());
       int newQuantity = book.getQuantity() - item.getCount();
       backOfficeClient.updateStock(
           item.getBookId(),      // 書籍ID
           book.getVersion(),     // バージョン番号（楽観的ロック用）
           newQuantity            // 更新後の在庫数
       );
   }
   ```

* BackOfficeRestClient.updateStock() の実装:
```java
public StockTO updateStock(Integer bookId, Long version, Integer newQuantity) {
    String url = baseUrl + "/stocks/" + bookId;
    
    // リクエストボディ（StockUpdateRequestと一致）
    Map<String, Object> requestBody = Map.of(
        "quantity", newQuantity,  // ← "quantity" フィールド名が重要
        "version", version
    );
    
    Response response = client.target(url)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(requestBody));
    // ...
}
```

* 注: 
* 以前のバージョンでは `StockTO` を使用していましたが、現在は `BookTO` に在庫情報（`quantity`, `version`）が含まれているため、`BookTO` を使用します
* リクエストボディのフィールド名は `back-office-api-sdd` の `StockUpdateRequest` と一致する必要があります（`quantity` と `version`）

3. 注文トランザクション作成（ローカルDB）
   ```java
   OrderTran orderTran = new OrderTran();
   orderTran.setCustomerId(customerId);
   orderTran.setOrderDate(LocalDate.now());  // DATE型
   orderTran.setTotalPrice(request.totalPrice());
   orderTran.setDeliveryPrice(request.deliveryPrice());
   orderTran.setDeliveryAddress(request.deliveryAddress());
   orderTran.setSettlementType(request.settlementType());
   orderTranDao.insert(orderTran);
   ```

4. 注文明細作成（ローカルDB、スナップショットパターン）
   ```java
   int lineNo = 1;
   for (CartItemRequest item : request.cartItems()) {
       // 外部APIから書籍情報を取得（スナップショット保存のため）
       BookTO book = backOfficeClient.findBookById(item.bookId());
       
       if (book == null || book.getPublisher() == null) {
           throw new RuntimeException("書籍情報の取得に失敗しました: bookId=" + item.bookId());
       }
       
       OrderDetail detail = new OrderDetail();
       detail.setOrderTranId(orderTran.getOrderTranId());
       detail.setOrderDetailId(lineNo++);
       detail.setBookId(item.bookId());
       detail.setBookName(book.getBookName());                           // スナップショット
       detail.setPublisherName(book.getPublisher().getPublisherName()); // スナップショット
       detail.setPrice(item.price());                                    // スナップショット
       detail.setCount(item.count());
       orderDetailDao.insert(detail);
   }
   ```
   
   スナップショットパターン: 注文時点の書籍名、出版社名、価格をDBに保存します。
   * メリット1: 書籍マスタが変更されても、過去の注文履歴は影響を受けない
   * メリット2: 書籍が削除されても、注文履歴を正常に表示できる
   * メリット3: 注文履歴表示時に外部API呼び出しが不要（パフォーマンス向上）

* エラーハンドリング:
  * `OutOfStockException` - 在庫不足時にスロー
  * `OptimisticLockException` - 楽観的ロック競合時（外部APIから）
  * トランザクションロールバック - エラー時

#### findOrderHistoryByCustomerId()

* シグネチャ:
```java
public List<OrderHistoryResponse> findOrderHistoryByCustomerId(Integer customerId)
```

* 処理フロー:
  1. 注文トランザクション取得（ローカルDB）
   ```java
   List<OrderTran> orders = orderTranDao.findByCustomerId(customerId);
   ```

2. 各注文の明細を取得（スナップショットデータを使用）
   ```java
   for (OrderTran order : orders) {
       List<OrderDetail> details = orderDetailDao.findByOrderTranId(order.getOrderTranId());
       
       for (OrderDetail detail : details) {
           // スナップショットデータを使用（外部API呼び出し不要）
           history.add(new OrderHistoryResponse(
               order.getOrderDate().atStartOfDay(),
               order.getOrderTranId(),
               detail.getOrderDetailId(),
               detail.getBookName(),        // スナップショット
               detail.getPublisherName(),   // スナップショット
               detail.getPrice(),
               detail.getCount()
           ));
       }
   }
   ```

* パフォーマンス最適化:
  * 外部API呼び出しが不要（スナップショットデータを使用）
  * 書籍マスタの変更や削除に影響されない

#### findOrderById()

* シグネチャ:
```java
public OrderResponse findOrderById(Integer tranId)
```

* 処理フロー:
  1. 注文トランザクション取得（ローカルDB）
   ```java
   OrderTran orderTran = orderTranDao.findById(tranId);
   ```

2. 注文明細を取得（スナップショットデータを使用）
   ```java
   List<OrderDetail> details = orderDetailDao.findByOrderTranId(tranId);
   
   for (OrderDetail detail : details) {
       // スナップショットデータを使用（外部API呼び出し不要）
       detailResponses.add(new OrderDetailResponse(
           detail.getOrderDetailId(),
           detail.getBookId(),
           detail.getBookName(),        // スナップショット
           detail.getPublisherName(),   // スナップショット
           detail.getPrice(),
           detail.getCount()
       ));
   }
   ```

---

### 2.6 DeliveryFeeService（配送料金計算）

* 責務: 配送料金の計算（BFF独自実装）

* アノテーション:
  * `@ApplicationScoped`

* 主要メソッド:

#### calculateDeliveryFee()

* シグネチャ:
```java
public Integer calculateDeliveryFee(String address)
```

* 計算ロジック:
```java
if (AddressUtil.isTokyoMetropolitanArea(address)) {
    return 500;  // 東京都内: 500円
} else if (AddressUtil.isKantoArea(address)) {
    return 700;  // 関東圏: 700円
} else {
    return 1000; // その他: 1000円
}
```

---

### 2.7 OrderTran（エンティティ）

* テーブル: `ORDER_TRAN`

| フィールド名 | 型 | カラム名 | 制約 | 説明 |
|------------|---|---------|-----|------|
| `orderTranId` | `Integer` | `ORDER_TRAN_ID` | `@Id @GeneratedValue` | 注文ID（主キー） |
| `customerId` | `Integer` | `CUSTOMER_ID` | `@Column(nullable=false)` | 顧客ID |
| `orderDate` | `LocalDate` | `ORDER_DATE` | `@Column(nullable=false)` | 注文日（DATE型） |
| `totalPrice` | `Integer` | `TOTAL_PRICE` | `@Column(nullable=false)` | 合計金額 |
| `deliveryPrice` | `Integer` | `DELIVERY_PRICE` | `@Column(nullable=false)` | 配送料 |
| `deliveryAddress` | `String` | `DELIVERY_ADDRESS` | `@Column(length=30, nullable=false)` | 配送先住所 |
| `settlementType` | `Integer` | `SETTLEMENT_TYPE` | `@Column(nullable=false)` | 決済方法 |

---

### 2.8 OrderDetail（エンティティ）

* テーブル: `ORDER_DETAIL`

| フィールド名 | 型 | カラム名 | 制約 | 説明 |
|------------|---|---------|-----|------|
| `orderTranId` | `Integer` | `ORDER_TRAN_ID` | `@Id`（複合キー） | 注文ID |
| `orderDetailId` | `Integer` | `ORDER_DETAIL_ID` | `@Id`（複合キー） | 明細番号 |
| `bookId` | `Integer` | `BOOK_ID` | `@Column(nullable=false)` | 書籍ID（論理参照のみ、外部キー制約なし） |
| `bookName` | `String` | `BOOK_NAME` | `@Column(length=80, nullable=false)` | 書籍名（スナップショット） |
| `publisherName` | `String` | `PUBLISHER_NAME` | `@Column(length=80)` | 出版社名（スナップショット） |
| `price` | `Integer` | `PRICE` | `@Column(nullable=false)` | 単価（スナップショット） |
| `count` | `Integer` | `COUNT` | `@Column(nullable=false)` | 数量 |

* 複合キー: `OrderDetailPK`（orderTranId, orderDetailId）

* スナップショットパターン: `BOOK_NAME`、`PUBLISHER_NAME`、`PRICE`は注文時点のデータを保持します。
* 目的: 書籍マスタの変更や削除に影響されない注文履歴の保持
* 実装: 注文登録時に外部API（back-office-api-sdd）から取得した書籍情報を保存

---

## 3. BFFパターンの特徴（分散トランザクション）

### 3.1 トランザクション分離

* 外部API（在庫更新）:
  * `back-office-api`が独立してトランザクション管理
  * BFF層からは制御不可

* ローカルDB（注文作成）:
  * `@Transactional`でBFF層がトランザクション管理

### 3.2 結果整合性（Eventual Consistency）

* 問題:
  * 在庫更新成功 → 注文作成失敗の場合、不整合が発生

* 現状の対応:
  * エラーログを記録
  * 運用でのリカバリー

* 将来の対応:
  * Sagaパターンの導入
  * 補償トランザクションの実装

---

## 4. エラーハンドリング

### 4.1 エラーシナリオ

| エラーケース | HTTPステータス | レスポンス |
|------------|--------------|----------|
| 在庫不足 | 409 Conflict | `{"error": "Conflict", "message": "在庫が不足しています: Java入門"}` |
| 楽観的ロック競合 | 409 Conflict | `{"error": "Conflict", "message": "他のユーザーが購入済みです"}` |
| バリデーションエラー | 400 Bad Request | `{"error": "Bad Request", "message": "配送先住所は必須です"}` |
| 外部API接続エラー | 503 Service Unavailable | `{"error": "Service Unavailable", "message": "在庫管理システムに接続できません"}` |

---

## 5. テスト要件

### 5.1 ユニットテスト

* 対象: `OrderService`, `DeliveryFeeService`

* 注文処理テスト（正常系）
* 在庫不足テスト
* 配送料金計算テスト（東京都内）
* 配送料金計算テスト（関東圏）
* 配送料金計算テスト（その他）

### 5.2 統合テスト

* 対象: `OrderResource` + `OrderService` + `BackOfficeRestClient`

* 注文作成API呼び出し（正常系、外部API連携）
* 注文作成API呼び出し（在庫不足）
* 注文作成API呼び出し（楽観的ロック競合）
* 注文履歴取得API呼び出し

---

## 6. 参考資料

* [functional_design.md](functional_design.md) - 機能設計書
* [behaviors.md](behaviors.md) - 振る舞い仕様書
* [BFFパターン](https://samnewman.io/patterns/architectural/bff/)
* [Sagaパターン](https://microservices.io/patterns/data/saga.html)
