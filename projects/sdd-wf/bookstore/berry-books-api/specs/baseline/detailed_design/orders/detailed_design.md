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
* 責務: 注文処理、注文履歴管理、配送料金計算
* 依存関係: commonドメイン（エンティティ、DAO）、externalドメイン（外部API連携）

---

## 2. クラス構成

### 2.1 パッケージ構造

```
pro.kensait.berrybooks
├── api/                         # プレゼンテーション層
│   ├── OrderResource.java
│   └── dto/
│       ├── OrderRequest.java
│       ├── CartItemRequest.java
│       ├── OrderResponse.java
│       └── OrderDetailResponse.java
├── service/                     # ビジネスロジック層
│   ├── OrderService.java
│   └── DeliveryFeeService.java
├── dao/                         # データアクセス層
│   ├── OrderTranDao.java
│   └── OrderDetailDao.java
└── entity/                      # エンティティ層
    ├── OrderTran.java
    ├── OrderDetail.java
    └── OrderDetailPK.java
```

---

## 3. コンポーネント設計

### 3.1 Resource - OrderResource

**責務**: 注文API（注文作成、注文履歴取得、注文詳細取得）

**アノテーション**:
* `@Path("/orders")` - エンドポイントパス
* `@ApplicationScoped` - CDIスコープ
* `@Produces(MediaType.APPLICATION_JSON)` - JSON形式レスポンス
* `@Consumes(MediaType.APPLICATION_JSON)` - JSON形式リクエスト

**依存関係**:
* `@Inject OrderService orderService` - 注文ビジネスロジック
* `@Inject AuthenticatedUser authenticatedUser` - 認証済みユーザー情報

**主要メソッド**:

#### createOrder

```java
@POST
public Response createOrder(OrderRequest orderRequest)
```

* **目的**: 注文を作成
* **認証**: 必須（JwtAuthenFilterで検証済み）
* **処理**: orderService.createOrder()を呼び出し
* **戻り値**: 201 Created + OrderResponse
* **例外**: 400 Bad Request（在庫不足）、409 Conflict（楽観的ロック失敗）、500 Internal Server Error

#### getOrderHistory

```java
@GET
@Path("/history")
public Response getOrderHistory()
```

* **目的**: ログイン中の顧客の注文履歴を取得
* **認証**: 必須
* **処理**: authenticatedUser.getCustomerId()で顧客IDを取得し、orderService.getOrderHistory()を呼び出し
* **戻り値**: 200 OK + List<OrderResponse>

#### getOrderDetail

```java
@GET
@Path("/{tranId}")
public Response getOrderDetail(@PathParam("tranId") Integer tranId)
```

* **目的**: 注文詳細を取得
* **認証**: 不要
* **処理**: orderService.getOrderDetail()を呼び出し
* **戻り値**: 200 OK + OrderResponse
* **例外**: 404 Not Found（注文が存在しない）

---

### 3.2 Service - OrderService

**責務**: 注文ビジネスロジック（注文作成、注文履歴取得、在庫更新連携）

**アノテーション**:
* `@ApplicationScoped` - CDIスコープ
* `@Transactional` - トランザクション管理

**依存関係**:
* `@Inject OrderTranDao orderTranDao` - 注文トランザクションDAO
* `@Inject OrderDetailDao orderDetailDao` - 注文明細DAO
* `@Inject BackOfficeRestClient backOfficeClient` - 在庫管理API連携
* `@Inject DeliveryFeeService deliveryFeeService` - 配送料金計算
* `@Inject AuthenticatedUser authenticatedUser` - 認証済みユーザー情報

**主要メソッド**:

#### createOrder

```java
@Transactional
public OrderTran createOrder(OrderRequest orderRequest)
```

* **目的**: 注文を作成し、在庫を更新
* **処理フロー**:
  1. 各書籍の在庫確認（backOfficeClient.findStockById()）
  2. 在庫数が注文数以上であることを確認（不足時はOutOfStockExceptionをスロー）
  3. 配送料金計算（deliveryFeeService.calculateDeliveryFee()）
  4. OrderTranエンティティを作成し、orderTranDao.insert()
  5. OrderDetailエンティティを作成（スナップショット値を設定）、orderDetailDao.insert()
  6. 在庫更新（backOfficeClient.updateStock()）
  7. トランザクションコミット
* **戻り値**: 作成されたOrderTran
* **例外**: OutOfStockException（在庫不足）、OptimisticLockException（楽観的ロック失敗）

#### getOrderHistory

```java
public List<OrderTran> getOrderHistory(Integer customerId)
```

* **目的**: 顧客の注文履歴を取得
* **処理**: orderTranDao.findByCustomerId()を呼び出し
* **戻り値**: 注文トランザクションのリスト（降順）

#### getOrderDetail

```java
public OrderTran getOrderDetail(Integer orderTranId)
```

* **目的**: 注文詳細を取得
* **処理**: orderTranDao.findById()を呼び出し
* **戻り値**: 注文トランザクション（存在しない場合はnull）

---

### 3.3 Service - DeliveryFeeService

**責務**: 配送料金計算

**アノテーション**:
* `@ApplicationScoped` - CDIスコープ

**主要メソッド**:

#### calculateDeliveryFee

```java
public Integer calculateDeliveryFee(Integer totalAmount, String deliveryAddress)
```

* **目的**: 購入金額と配送先住所から配送料金を計算
* **計算ロジック**:
  * 購入金額が5000円以上の場合: 配送料無料（0円）
  * 配送先が沖縄県の場合: 800円
  * その他の場合: 400円
* **戻り値**: 配送料金（円）

---

### 3.4 DTO - OrderRequest

**責務**: 注文作成リクエスト

**実装方式**: Java Record

**フィールド**:
* `List<CartItemRequest> cartItems` - カート内アイテムのリスト
* `String deliveryAddress` - 配送先住所
* `Integer settlementType` - 決済方法（1:銀行振込, 2:クレジットカード, 3:着払い）

---

### 3.5 DTO - CartItemRequest

**責務**: カートアイテム情報

**実装方式**: Java Record

**フィールド**:
* `Integer bookId` - 書籍ID
* `String bookName` - 書籍名（スナップショット用）
* `String publisherName` - 出版社名（スナップショット用）
* `Integer price` - 価格（スナップショット用）
* `Integer count` - 注文数
* `Long version` - バージョン番号（楽観的ロック用）

---

### 3.6 DTO - OrderResponse

**責務**: 注文情報レスポンス

**実装方式**: Java Record

**フィールド**:
* `Integer orderTranId` - 注文ID
* `LocalDate orderDate` - 注文日
* `Integer customerId` - 顧客ID
* `Integer totalPrice` - 合計金額（配送料含む）
* `Integer deliveryPrice` - 配送料金
* `String deliveryAddress` - 配送先住所
* `Integer settlementType` - 決済方法
* `List<OrderDetailResponse> orderDetails` - 注文明細リスト

---

### 3.7 DTO - OrderDetailResponse

**責務**: 注文明細情報レスポンス

**実装方式**: Java Record

**フィールド**:
* `Integer orderDetailId` - 注文明細ID
* `Integer bookId` - 書籍ID
* `String bookName` - 書籍名（スナップショット）
* `String publisherName` - 出版社名（スナップショット）
* `Integer price` - 価格（スナップショット）
* `Integer count` - 注文数

---

### 3.8 DAO - OrderTranDao

**責務**: 注文トランザクションのデータアクセス

**アノテーション**:
* `@ApplicationScoped` - CDIスコープ
* `@PersistenceContext(unitName = "BerryBooksPU")` - 永続化コンテキスト

**依存関係**:
* `@PersistenceContext EntityManager em` - JPAエンティティマネージャー

**主要メソッド**:

#### insert

```java
public OrderTran insert(OrderTran orderTran)
```

* **目的**: 注文トランザクションを登録
* **戻り値**: 永続化された注文トランザクション（ORDER_TRAN_IDが自動採番される）

#### findById

```java
public OrderTran findById(Integer orderTranId)
```

* **目的**: 注文IDで検索
* **戻り値**: 注文トランザクション（存在しない場合はnull）

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

---

### 3.9 DAO - OrderDetailDao

**責務**: 注文明細のデータアクセス

**アノテーション**:
* `@ApplicationScoped` - CDIスコープ
* `@PersistenceContext(unitName = "BerryBooksPU")` - 永続化コンテキスト

**依存関係**:
* `@PersistenceContext EntityManager em` - JPAエンティティマネージャー

**主要メソッド**:

#### insert

```java
public OrderDetail insert(OrderDetail orderDetail)
```

* **目的**: 注文明細を登録
* **戻り値**: 永続化された注文明細

#### findByOrderTranId

```java
public List<OrderDetail> findByOrderTranId(Integer orderTranId)
```

* **目的**: 注文IDで注文明細一覧を検索
* **JPQLクエリ**:
  ```sql
  SELECT od FROM OrderDetail od 
  WHERE od.orderTranId = :orderTranId 
  ORDER BY od.orderDetailId
  ```

---

## 4. エンティティ設計

### 4.1 OrderTran

**テーブル名**: `ORDER_TRAN`

**責務**: 注文トランザクション情報を管理

**主要フィールド**:
* `Integer orderTranId` - `@Id @GeneratedValue(strategy = IDENTITY) @Column(name="ORDER_TRAN_ID")`
* `LocalDate orderDate` - `@Column(name="ORDER_DATE", nullable=false)`
* `Integer customerId` - `@Column(name="CUSTOMER_ID", nullable=false)` （論理参照のみ）
* `Integer totalPrice` - `@Column(name="TOTAL_PRICE", nullable=false)`
* `Integer deliveryPrice` - `@Column(name="DELIVERY_PRICE", nullable=false)`
* `String deliveryAddress` - `@Column(name="DELIVERY_ADDRESS", nullable=false, length=30)`
* `Integer settlementType` - `@Column(name="SETTLEMENT_TYPE", nullable=false)` （1:銀行振込, 2:クレジットカード, 3:着払い）

**リレーション**:
* `@OneToMany(mappedBy = "orderTran", cascade = CascadeType.ALL, orphanRemoval = true) List<OrderDetail> orderDetails` - 注文明細

**アノテーション**:
* `@Entity`
* `@Table(name = "ORDER_TRAN")`

**重要メソッド**:
* `addOrderDetail(OrderDetail)` - 注文明細を追加し双方向リレーションを設定
* `removeOrderDetail(OrderDetail)` - 注文明細を削除し双方向リレーションを解除

---

### 4.2 OrderDetail

**テーブル名**: `ORDER_DETAIL`

**責務**: 注文明細情報を管理（スナップショットパターン適用）

**主要フィールド**:
* `Integer orderTranId` - `@Id @Column(name="ORDER_TRAN_ID")` （複合主キー）
* `Integer orderDetailId` - `@Id @Column(name="ORDER_DETAIL_ID")` （複合主キー）
* `Integer bookId` - `@Column(name="BOOK_ID", nullable=false)` （論理参照のみ）
* `String bookName` - `@Column(name="BOOK_NAME", nullable=false, length=100)` （スナップショット）
* `String publisherName` - `@Column(name="PUBLISHER_NAME", nullable=false, length=50)` （スナップショット）
* `Integer price` - `@Column(name="PRICE", nullable=false)` （スナップショット）
* `Integer count` - `@Column(name="COUNT", nullable=false)`

**リレーション**:
* `@ManyToOne @JoinColumn(name = "ORDER_TRAN_ID", insertable = false, updatable = false) OrderTran orderTran` - 注文トランザクション

**アノテーション**:
* `@Entity`
* `@Table(name = "ORDER_DETAIL")`
* `@IdClass(OrderDetailPK.class)` - 複合主キー

---

### 4.3 OrderDetailPK

**責務**: OrderDetailの複合主キークラス

**フィールド**:
* `Integer orderTranId` - 注文トランザクションID
* `Integer orderDetailId` - 注文明細ID

**アノテーション**:
* `implements Serializable`

**必須メソッド**:
* `equals(Object)` - 同値性判定
* `hashCode()` - ハッシュコード計算

---

## 5. トランザクション設計

* **トランザクション境界**: Service層（OrderService）
* **伝播レベル**: REQUIRED（デフォルト）
* **注文作成時の一貫性**: OrderTranとOrderDetailは同一トランザクション内で作成

---

## 6. 設計上の重要事項

### 6.1 スナップショットパターン

OrderDetailエンティティは、注文時点の書籍情報をスナップショットとして保存します:

* `bookName` - 注文時点の書籍名
* `publisherName` - 注文時点の出版社名
* `price` - 注文時点の価格

これにより、書籍マスタの変更が注文履歴に影響しません。

### 6.2 論理参照

* `OrderTran.customerId` - customer-hub-apiで管理される顧客への論理参照（外部キー制約なし）
* `OrderDetail.bookId` - back-office-apiで管理される書籍への論理参照（外部キー制約なし）

### 6.3 複合主キー

OrderDetailは`(ORDER_TRAN_ID, ORDER_DETAIL_ID)`の複合主キーを使用:

* ORDER_TRAN_ID: 親の注文トランザクションID
* ORDER_DETAIL_ID: 注文内での連番（1, 2, 3, ...）

---

---

## 7. 実装状況

### 7.1 完了したコンポーネント

| コンポーネント | ステータス | ファイル名 |
|--------------|----------|----------|
| Entity | ✅ 完了 | OrderTran.java, OrderDetail.java, OrderDetailPK.java |
| DAO | ✅ 完了 | OrderTranDao.java, OrderDetailDao.java |
| Service | ✅ 完了 | OrderService.java, DeliveryFeeService.java |
| Resource | ✅ 完了 | OrderResource.java |
| DTO | ✅ 完了 | OrderRequest.java, CartItemRequest.java, OrderResponse.java, OrderDetailResponse.java |
| 単体テスト(Entity/DAO) | ✅ 完了 | OrderTranTest.java, OrderDetailTest.java, OrderDetailPKTest.java, OrderTranDaoTest.java, OrderDetailDaoTest.java |
| 単体テスト(Service/Resource) | ✅ 完了 | DeliveryFeeServiceTest.java, OrderServiceTest.java, OrderResourceTest.java |

### 7.2 実装の特徴

1. **スナップショットパターン**: OrderDetailに注文時点の書籍情報を保存
2. **トランザクション管理**: OrderServiceの@Transactionalで一貫性を保証
3. **外部API連携**: BackOfficeRestClientを使用した在庫管理
4. **エラーハンドリング**: 在庫不足、楽観的ロック失敗の適切な処理
5. **認証統合**: AuthenticatedUserを使用した認証情報の取得

---

## 8. 参考資料

* [behaviors.md](behaviors.md) - 単体テスト用振る舞い仕様書
* [../../basic_design/orders/functional_design.md](../../basic_design/orders/functional_design.md) - ドメイン機能設計書
* [../../basic_design/common/data_model.md](../../basic_design/common/data_model.md) - データモデル仕様書
* [../../basic_design/common/architecture_design.md](../../basic_design/common/architecture_design.md) - アーキテクチャ設計書
