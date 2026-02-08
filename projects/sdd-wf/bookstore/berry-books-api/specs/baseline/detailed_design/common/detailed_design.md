# common - ドメイン詳細設計書

ドメイン名: common  
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

* ドメイン名: common
* 責務: 注文データの永続化、JWT認証、外部API連携、共通ユーティリティ
* 依存関係: なし（他のすべてのドメインがcommonに依存）

---

## 2. クラス構成

### 2.1 パッケージ構造

```
pro.kensait.berrybooks
├── entity/                 # JPAエンティティ
│   ├── OrderTran.java
│   ├── OrderDetail.java
│   └── OrderDetailPK.java
├── dao/                    # データアクセス
│   ├── OrderTranDao.java
│   └── OrderDetailDao.java
├── security/               # セキュリティ
│   ├── JwtUtil.java
│   ├── JwtAuthenFilter.java
│   └── AuthenticatedUser.java
├── external/               # 外部API連携
│   ├── BackOfficeRestClient.java
│   ├── CustomerHubRestClient.java
│   └── dto/
│       ├── BookTO.java
│       ├── StockTO.java
│       ├── CategoryTO.java
│       └── CustomerTO.java
└── util/                   # ユーティリティ
    └── PasswordUtil.java
```

---

## 3. コンポーネント設計

### 3.1 Entity - OrderTran

**責務**: 注文トランザクションデータの永続化

**アノテーション**:
* `@Entity`
* `@Table(name = "ORDER_TRAN")`

**主要フィールド**:
* `Integer orderTranId` - `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`
* `LocalDate orderDate` - `@Column(name = "ORDER_DATE")`
* `Integer customerId` - `@Column(name = "CUSTOMER_ID")`（論理参照のみ）
* `Integer totalPrice` - `@Column(name = "TOTAL_PRICE")`
* `Integer deliveryPrice` - `@Column(name = "DELIVERY_PRICE")`
* `String deliveryAddress` - `@Column(name = "DELIVERY_ADDRESS")`
* `Integer settlementType` - `@Column(name = "SETTLEMENT_TYPE")`
* `List<OrderDetail> orderDetails` - `@OneToMany(mappedBy = "orderTran", cascade = CascadeType.ALL)`

---

### 3.2 Entity - OrderDetail

**責務**: 注文明細データの永続化（スナップショットパターン）

**アノテーション**:
* `@Entity`
* `@Table(name = "ORDER_DETAIL")`
* `@IdClass(OrderDetailPK.class)`

**主要フィールド**:
* `Integer orderTranId` - `@Id @Column(name = "ORDER_TRAN_ID")`
* `Integer orderDetailId` - `@Id @Column(name = "ORDER_DETAIL_ID")`
* `Integer bookId` - `@Column(name = "BOOK_ID")`（論理参照のみ）
* `String bookName` - `@Column(name = "BOOK_NAME")`（スナップショット）
* `String publisherName` - `@Column(name = "PUBLISHER_NAME")`（スナップショット）
* `Integer price` - `@Column(name = "PRICE")`（スナップショット）
* `Integer count` - `@Column(name = "COUNT")`
* `OrderTran orderTran` - `@ManyToOne @JoinColumn(name = "ORDER_TRAN_ID", insertable = false, updatable = false)`

---

### 3.3 Entity - OrderDetailPK

**責務**: OrderDetailの複合主キー

**アノテーション**:
* `@Embeddable`

**フィールド**:
* `Integer orderTranId`
* `Integer orderDetailId`

**メソッド**:
* `equals(Object o)` - 複合主キーの比較
* `hashCode()` - 複合主キーのハッシュ値

---

### 3.4 DAO - OrderTranDao

**責務**: ORDER_TRANテーブルのCRUD操作

**アノテーション**:
* `@ApplicationScoped`

**依存関係**:
* `@PersistenceContext EntityManager em`

**主要メソッド**:

#### insert

```java
public OrderTran insert(OrderTran orderTran)
```

* **目的**: 注文トランザクションの登録
* **処理**: `em.persist(orderTran)`

#### findById

```java
public OrderTran findById(Integer orderTranId)
```

* **目的**: 注文トランザクションの主キー検索
* **処理**: `em.find(OrderTran.class, orderTranId)`

#### findByCustomerId

```java
public List<OrderTran> findByCustomerId(Integer customerId)
```

* **目的**: 顧客の注文履歴取得
* **JPQLクエリ**:
  ```sql
  SELECT o FROM OrderTran o
  WHERE o.customerId = :customerId
  ORDER BY o.orderDate DESC
  ```

---

### 3.5 DAO - OrderDetailDao

**責務**: ORDER_DETAILテーブルのCRUD操作

**アノテーション**:
* `@ApplicationScoped`

**依存関係**:
* `@PersistenceContext EntityManager em`

**主要メソッド**:

#### insert

```java
public OrderDetail insert(OrderDetail orderDetail)
```

* **目的**: 注文明細の登録
* **処理**: `em.persist(orderDetail)`

#### findByOrderTranId

```java
public List<OrderDetail> findByOrderTranId(Integer orderTranId)
```

* **目的**: 注文IDで明細一覧取得
* **JPQLクエリ**:
  ```sql
  SELECT od FROM OrderDetail od
  WHERE od.orderTranId = :orderTranId
  ORDER BY od.orderDetailId
  ```

---

### 3.6 Security - JwtUtil

**責務**: JWT生成・検証ユーティリティ

**アノテーション**:
* `@ApplicationScoped`

**依存関係**:
* なし（io.jsonwebtoken.Jwtsライブラリを使用）

**主要メソッド**:

#### generateToken

```java
public String generateToken(Integer customerId, String customerName)
```

* **目的**: JWT生成
* **アルゴリズム**: HMAC-SHA256
* **有効期限**: 24時間
* **クレーム**: customerId, customerName

#### validateToken

```java
public Claims validateToken(String token) throws JwtException
```

* **目的**: JWT検証
* **例外**: JwtException（トークン無効時）

#### extractCustomerId

```java
public Integer extractCustomerId(String token)
```

* **目的**: トークンからcustomerIdを抽出

---

### 3.7 Security - JwtAuthenFilter

**責務**: JWT認証フィルター

**アノテーション**:
* `@Provider`
* `@Priority(Priorities.AUTHENTICATION)`

**依存関係**:
* `@Inject JwtUtil jwtUtil`
* `@Inject AuthenticatedUser authenticatedUser`

**実装インターフェース**:
* `ContainerRequestFilter`

**主要メソッド**:

#### filter

```java
public void filter(ContainerRequestContext requestContext) throws IOException
```

* **目的**: リクエストごとのJWT認証
* **認証除外パス**: `/api/auth/login`, `/api/auth/logout`, `/api/auth/register`, `/api/books`, `/api/images`
* **処理**: Cookieからトークン取得 → 検証 → AuthenticatedUserに設定

---

### 3.8 Security - AuthenticatedUser

**責務**: 認証済みユーザー情報の保持

**アノテーション**:
* `@RequestScoped`

**フィールド**:
* `Integer customerId`
* `String customerName`

**メソッド**:
* `setCustomerId(Integer customerId)`
* `setCustomerName(String customerName)`
* `getCustomerId()`
* `getCustomerName()`
* `isAuthenticated()`

---

### 3.9 External - BackOfficeRestClient

**責務**: back-office-api連携（書籍・在庫・カテゴリ）

**アノテーション**:
* `@ApplicationScoped`

**依存関係**:
* `@Inject @RestClient BackOfficeApi backOfficeApi`（MicroProfile REST Client）

**主要メソッド**:

#### findAllBooks

```java
public List<BookTO> findAllBooks()
```

* **エンドポイント**: `GET /books`

#### findBookById

```java
public BookTO findBookById(Integer bookId)
```

* **エンドポイント**: `GET /books/{bookId}`

#### findStockById

```java
public StockTO findStockById(Integer bookId)
```

* **エンドポイント**: `GET /stocks/{bookId}`

#### updateStock

```java
public StockTO updateStock(Integer bookId, Long version, Integer newQuantity)
```

* **エンドポイント**: `PUT /stocks/{bookId}`
* **楽観的ロック**: versionパラメータを送信

#### findAllCategories

```java
public List<CategoryTO> findAllCategories()
```

* **エンドポイント**: `GET /categories`

---

### 3.10 External - CustomerHubRestClient

**責務**: customer-hub-api連携（顧客管理）

**アノテーション**:
* `@ApplicationScoped`

**依存関係**:
* `@Inject @RestClient CustomerHubApi customerHubApi`（MicroProfile REST Client）

**主要メソッド**:

#### findByEmail

```java
public CustomerTO findByEmail(String email)
```

* **エンドポイント**: `GET /customers/email/{email}`

#### createCustomer

```java
public CustomerTO createCustomer(CustomerTO customer)
```

* **エンドポイント**: `POST /customers`

---

### 3.11 Util - PasswordUtil

**責務**: パスワードハッシュ化・検証

**アノテーション**:
* `@ApplicationScoped`

**主要メソッド**:

#### hashPassword

```java
public String hashPassword(String plainPassword)
```

* **目的**: BCryptハッシュ化
* **アルゴリズム**: BCrypt（cost=10）

#### verifyPassword

```java
public boolean verifyPassword(String plainPassword, String hashedPassword)
```

* **目的**: パスワード検証

---

## 4. DTO設計

### 4.1 外部API用DTO

#### BookTO

**目的**: 書籍情報の転送

**フィールド**:
* `Integer bookId`
* `String bookName`
* `String author`
* `Integer categoryId`
* `Integer publisherId`
* `String publisherName`
* `Integer price`
* `Integer quantity`
* `Long version`

#### StockTO

**目的**: 在庫情報の転送

**フィールド**:
* `Integer bookId`
* `Integer quantity`
* `Long version`

#### CategoryTO

**目的**: カテゴリ情報の転送

**フィールド**:
* `Integer categoryId`
* `String categoryName`

#### CustomerTO

**目的**: 顧客情報の転送

**フィールド**:
* `Integer customerId`
* `String customerName`
* `String password`
* `String email`
* `LocalDate birthday`
* `String address`

---

## 5. トランザクション設計

* **トランザクション境界**: Service層（OrderService等）
* **伝播レベル**: REQUIRED（デフォルト）
* **DAO層**: トランザクション管理なし（Service層で管理）

---

## 6. セキュリティ設計

### 6.1 JWT仕様

* **アルゴリズム**: HMAC-SHA256
* **シークレットキー**: 環境変数から取得
* **有効期限**: 24時間
* **Cookie設定**: HttpOnly, Secure（本番環境）

### 6.2 認証フロー

1. JwtAuthenFilterがリクエストを受信
2. Cookieからトークン取得
3. JwtUtilでトークン検証
4. AuthenticatedUserに認証情報を設定
5. Resourceでは@Inject AuthenticatedUserから情報取得

---

## 7. 外部API連携設計

### 7.1 REST Client設定

* **実装方式**: MicroProfile REST Client
* **設定方法**: microprofile-config.properties
* **タイムアウト**: 接続30秒、読み取り30秒
* **リトライ**: なし（将来的に実装可能）

### 7.2 エラーハンドリング

* **外部APIエラー**: WebApplicationExceptionとしてスロー
* **ネットワークエラー**: ProcessingExceptionとしてスロー
* **例外マッパー**: ExceptionMapperで統一的なエラーレスポンス

---

## 8. 参考資料

* [behaviors.md](behaviors.md) - 単体テスト用振る舞い仕様書
* [../../basic_design/common/functional_design.md](../../basic_design/common/functional_design.md) - 共通機能設計書
* [../../basic_design/common/data_model.md](../../basic_design/common/data_model.md) - データモデル仕様書
* [../../basic_design/common/architecture_design.md](../../basic_design/common/architecture_design.md) - アーキテクチャ設計書