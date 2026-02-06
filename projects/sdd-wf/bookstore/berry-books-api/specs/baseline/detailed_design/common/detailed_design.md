# common - 共通ドメイン詳細設計書

ドメイン名: common（共通ドメイン、最優先実装）  
バージョン: 1.0.0  
最終更新: 2026-02-06

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
* 責務: 全ドメインが依存する基盤機能（エンティティ、DAO、JWT認証、外部API連携）を提供
* 依存関係: なし（最優先実装、他のすべてのドメインがこのcommonに依存）

---

## 2. クラス構成

### 2.1 パッケージ構造

```
pro.kensait.berrybooks
├── entity/                    # JPAエンティティ（注文関連のみ）
│   ├── OrderTran.java
│   ├── OrderDetail.java
│   └── OrderDetailPK.java
├── dao/                       # データアクセス（注文関連のみ）
│   ├── OrderTranDao.java
│   └── OrderDetailDao.java
├── security/                  # セキュリティ（JWT認証）
│   ├── JwtUtil.java
│   ├── JwtAuthenFilter.java
│   └── AuthenInfo.java
├── external/                  # 外部API連携
│   ├── BackOfficeRestClient.java
│   ├── CustomerHubRestClient.java
│   └── dto/
│       ├── BookTO.java
│       ├── StockTO.java
│       └── CustomerTO.java
├── api/
│   └── exception/             # 共通例外マッパー
│       ├── OutOfStockExceptionMapper.java
│       ├── OptimisticLockExceptionMapper.java
│       ├── ValidationExceptionMapper.java
│       └── GenericExceptionMapper.java
└── common/                    # 共通クラス
    ├── ErrorResponse.java     # エラーレスポンスDTO
    └── exception/
        ├── OutOfStockException.java
        └── AuthenticationException.java
```

---

## 3. エンティティ設計

### 3.1 Entity - OrderTran

**責務**: 注文トランザクション情報を管理

**アノテーション**:
* `@Entity`
* `@Table(name = "ORDER_TRAN")`

**主要フィールド**:
* `Integer orderTranId` - `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`
* `LocalDate orderDate` - `@Column(name = "ORDER_DATE", nullable = false)`
* `Integer customerId` - `@Column(name = "CUSTOMER_ID", nullable = false)` ※論理参照のみ
* `Integer totalPrice` - `@Column(name = "TOTAL_PRICE", nullable = false)`
* `Integer deliveryPrice` - `@Column(name = "DELIVERY_PRICE", nullable = false)`
* `String deliveryAddress` - `@Column(name = "DELIVERY_ADDRESS", nullable = false, length = 30)`
* `Integer settlementType` - `@Column(name = "SETTLEMENT_TYPE", nullable = false)`
* `List<OrderDetail> orderDetails` - `@OneToMany(mappedBy = "orderTran", cascade = CascadeType.ALL)`

**リレーション**:
* `@OneToMany` - OrderDetail（注文明細）

---

### 3.2 Entity - OrderDetail

**責務**: 注文明細情報を管理（スナップショットパターン）

**アノテーション**:
* `@Entity`
* `@Table(name = "ORDER_DETAIL")`

**主要フィールド**:
* `OrderDetailPK id` - `@EmbeddedId`（複合主キー）
* `OrderTran orderTran` - `@ManyToOne @MapsId("orderTranId") @JoinColumn(name = "ORDER_TRAN_ID")`
* `Integer bookId` - `@Column(name = "BOOK_ID", nullable = false)` ※論理参照のみ
* `String bookName` - `@Column(name = "BOOK_NAME", nullable = false, length = 100)` ※スナップショット
* `String publisherName` - `@Column(name = "PUBLISHER_NAME", nullable = false, length = 50)` ※スナップショット
* `Integer price` - `@Column(name = "PRICE", nullable = false)` ※スナップショット
* `Integer count` - `@Column(name = "COUNT", nullable = false)`

**リレーション**:
* `@ManyToOne` - OrderTran（注文トランザクション）

---

### 3.3 Embeddable - OrderDetailPK

**責務**: 注文明細の複合主キー

**アノテーション**:
* `@Embeddable`

**主要フィールド**:
* `Integer orderTranId` - `@Column(name = "ORDER_TRAN_ID")`
* `Integer orderDetailId` - `@Column(name = "ORDER_DETAIL_ID")`

**注意**: `equals()` と `hashCode()` を実装する必要がある

---

## 4. DAO設計

### 4.1 DAO - OrderTranDao

**責務**: 注文トランザクションのCRUD操作とクエリ実行

**アノテーション**:
* `@ApplicationScoped`

**依存関係**:
* `@PersistenceContext EntityManager em`

**主要メソッド**:

#### insert

```java
public OrderTran insert(OrderTran orderTran)
```

* **目的**: 注文トランザクションを登録

#### findById

```java
public Optional<OrderTran> findById(Integer orderTranId)
```

* **目的**: 注文IDで注文トランザクションを取得
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

* **目的**: 顧客IDで注文履歴を取得
* **JPQLクエリ**:
  ```sql
  SELECT o FROM OrderTran o 
  LEFT JOIN FETCH o.orderDetails 
  WHERE o.customerId = :customerId 
  ORDER BY o.orderDate DESC
  ```

---

### 4.2 DAO - OrderDetailDao

**責務**: 注文明細のCRUD操作

**アノテーション**:
* `@ApplicationScoped`

**依存関係**:
* `@PersistenceContext EntityManager em`

**主要メソッド**:

#### insert

```java
public OrderDetail insert(OrderDetail orderDetail)
```

* **目的**: 注文明細を登録

#### findByOrderTranId

```java
public List<OrderDetail> findByOrderTranId(Integer orderTranId)
```

* **目的**: 注文IDで注文明細一覧を取得
* **JPQLクエリ**:
  ```sql
  SELECT od FROM OrderDetail od 
  WHERE od.id.orderTranId = :orderTranId 
  ORDER BY od.id.orderDetailId
  ```

---

## 5. セキュリティコンポーネント

### 5.1 Utility - JwtUtil

**責務**: JWT生成・検証、Claims抽出

**アノテーション**:
* `@ApplicationScoped`

**主要メソッド**:

#### generateToken

```java
public String generateToken(Integer customerId, String email)
```

* **目的**: 顧客ID、メールアドレスからJWTトークンを生成
* **Claims**: `customerId`, `email`, `iat`, `exp`
* **有効期限**: 24時間
* **アルゴリズム**: HMAC-SHA256
* **秘密鍵**: MicroProfile Configから読み込み（設定キー: `jwt.secret`）

#### validateToken

```java
public boolean validateToken(String token)
```

* **目的**: JWTトークンの有効性を検証

#### getCustomerIdFromToken

```java
public Integer getCustomerIdFromToken(String token)
```

* **目的**: JWTトークンから顧客IDを抽出

#### getEmailFromToken

```java
public String getEmailFromToken(String token)
```

* **目的**: JWTトークンからメールアドレスを抽出

---

### 5.2 Filter - JwtAuthenFilter

**責務**: JWT認証フィルター処理

**アノテーション**:
* `@WebFilter(urlPatterns = "/api/*")`

**依存関係**:
* `@Inject JwtUtil jwtUtil`
* `@Inject AuthenInfo authenInfo`

**認証除外パス**:
* `/api/auth/login`
* `/api/auth/logout`
* `/api/auth/register`
* `/api/books`（GET）
* `/api/categories`（GET）
* `/api/images`

**処理フロー**（概要のみ）:
1. リクエストから `berry_auth` Cookieを取得
2. 除外パスの場合はフィルター処理をスキップ
3. JWTトークンを検証
4. 検証成功時、AuthenInfoにユーザー情報を設定
5. 検証失敗時、401 Unauthorizedを返却

---

### 5.3 CDI Bean - AuthenInfo

**責務**: 認証情報のスレッドローカル管理

**アノテーション**:
* `@RequestScoped`

**主要フィールド**:
* `Integer customerId`
* `String email`

**主要メソッド**:

```java
public void setCustomerId(Integer customerId)
public Integer getCustomerId()
public void setEmail(String email)
public String getEmail()
```

---

## 6. 外部API連携クライアント

### 6.1 RestClient - BackOfficeRestClient

**責務**: back-office-api（書籍・在庫・カテゴリ管理）との連携

**アノテーション**:
* `@ApplicationScoped`

**設定**:
* **ベースURL**: MicroProfile Configから読み込み（設定キー: `back-office-api.base-url`）
* **デフォルト値**: `http://localhost:8080/back-office-api/api`
* **タイムアウト**: 接続30秒、読み取り60秒

**主要メソッド**:

#### getAllBooks

```java
public List<BookTO> getAllBooks()
```

* **エンドポイント**: `GET /books`
* **目的**: 全書籍を在庫情報と共に取得

#### getBookById

```java
public BookTO getBookById(Integer bookId)
```

* **エンドポイント**: `GET /books/{bookId}`
* **目的**: 書籍詳細を取得

#### searchBooksJpql

```java
public List<BookTO> searchBooksJpql(Integer categoryId, String keyword)
```

* **エンドポイント**: `GET /books/search/jpql?categoryId={categoryId}&keyword={keyword}`
* **目的**: カテゴリIDまたはキーワードで書籍を検索（JPQL使用）

#### getAllCategories

```java
public Map<String, Integer> getAllCategories()
```

* **エンドポイント**: `GET /categories`
* **目的**: カテゴリ一覧をマップ形式で取得

#### findStockById

```java
public StockTO findStockById(Integer bookId)
```

* **エンドポイント**: `GET /stocks/{bookId}`
* **目的**: 在庫情報を取得

#### updateStock

```java
public StockTO updateStock(Integer bookId, Integer quantity, Long version)
```

* **エンドポイント**: `PUT /stocks/{bookId}`
* **目的**: 在庫を更新（楽観的ロック対応）
* **リクエストボディ**: `{"quantity": <quantity>, "version": <version>}`

---

### 6.2 RestClient - CustomerHubRestClient

**責務**: customer-hub-api（顧客管理）との連携

**アノテーション**:
* `@ApplicationScoped`

**設定**:
* **ベースURL**: MicroProfile Configから読み込み（設定キー: `customer-hub-api.base-url`）
* **デフォルト値**: `http://localhost:8080/customer-hub-api/customers`
* **タイムアウト**: 接続30秒、読み取り60秒

**主要メソッド**:

#### findByEmail

```java
public CustomerTO findByEmail(String email)
```

* **エンドポイント**: `GET /customers/query_email?email={email}`
* **目的**: メールアドレスで顧客を検索

#### findById

```java
public CustomerTO findById(Integer customerId)
```

* **エンドポイント**: `GET /customers/{customerId}`
* **目的**: 顧客IDで顧客情報を取得

#### register

```java
public CustomerTO register(CustomerTO customerTO)
```

* **エンドポイント**: `POST /customers/`
* **目的**: 新規顧客を登録

---

## 7. 外部API用DTO

### 7.1 Record - BookTO

**目的**: 書籍情報の転送（外部APIレスポンス）

**フィールド**:
* `Integer bookId`
* `String bookName`
* `String author`
* `Integer categoryId`
* `String categoryName`
* `Integer publisherId`
* `String publisherName`
* `Integer price`
* `Integer quantity` ※在庫数（@SecondaryTableによる結合）
* `Long version` ※楽観的ロックバージョン

---

### 7.2 Record - StockTO

**目的**: 在庫情報の転送（外部APIレスポンス）

**フィールド**:
* `Integer bookId`
* `String bookName`
* `Integer quantity`
* `Long version`

---

### 7.3 Record - CustomerTO

**目的**: 顧客情報の転送（外部APIレスポンス・リクエスト）

**フィールド**:
* `Integer customerId`
* `String customerName`
* `String password` ※BCryptハッシュ
* `String email`
* `LocalDate birthday`
* `String address`

---

## 8. 共通例外クラス

### 8.1 Exception - OutOfStockException

**責務**: 在庫不足エラーを表現

**継承**: `RuntimeException`

**コンストラクタ**:
```java
public OutOfStockException(String message)
```

---

### 8.2 Exception - AuthenticationException

**責務**: 認証失敗エラーを表現

**継承**: `RuntimeException`

**コンストラクタ**:
```java
public AuthenticationException(String message)
```

---

## 9. Exception Mapper

### 9.1 ExceptionMapper - OutOfStockExceptionMapper

**責務**: OutOfStockExceptionを409 Conflictレスポンスにマッピング

**アノテーション**:
* `@Provider`

**実装インターフェース**: `ExceptionMapper<OutOfStockException>`

---

### 9.2 ExceptionMapper - OptimisticLockExceptionMapper

**責務**: OptimisticLockExceptionを409 Conflictレスポンスにマッピング

**アノテーション**:
* `@Provider`

**実装インターフェース**: `ExceptionMapper<OptimisticLockException>`

---

### 9.3 ExceptionMapper - ValidationExceptionMapper

**責務**: ConstraintViolationExceptionを400 Bad Requestレスポンスにマッピング

**アノテーション**:
* `@Provider`

**実装インターフェース**: `ExceptionMapper<ConstraintViolationException>`

---

### 9.4 ExceptionMapper - GenericExceptionMapper

**責務**: 予期しない例外を500 Internal Server Errorレスポンスにマッピング

**アノテーション**:
* `@Provider`

**実装インターフェース**: `ExceptionMapper<Exception>`

---

## 10. 共通DTO

### 10.1 Record - ErrorResponse

**目的**: 統一的なエラーレスポンス形式

**フィールド**:
* `int status` - HTTPステータスコード
* `String error` - エラー種別
* `String message` - エラーメッセージ
* `String path` - リクエストパス

---

## 11. 設定情報

### 11.1 MicroProfile Config

**ファイル**: `src/main/resources/META-INF/microprofile-config.properties`

**設定項目**:
```properties
# JWT設定
jwt.secret=your-secret-key-at-least-256-bits-long-for-hs256-algorithm

# 外部API設定
back-office-api.base-url=http://localhost:8080/back-office-api/api
customer-hub-api.base-url=http://localhost:8080/customer-hub-api/customers
```

**読み込み優先順位**:
1. システムプロパティ
2. 環境変数
3. プロパティファイル
4. デフォルト値

---

## 12. トランザクション設計

* **トランザクション境界**: Service層（注文処理のみ、commonドメインではトランザクション不要）
* **伝播レベル**: デフォルト（REQUIRED）

---

## 13. 参考資料

* [behaviors.md](behaviors.md) - 単体テスト用振る舞い仕様書
* [../../basic_design/common/functional_design.md](../../basic_design/common/functional_design.md) - 共通機能設計書
* [../../basic_design/common/data_model.md](../../basic_design/common/data_model.md) - データモデル仕様書
* [../../basic_design/common/external_interface.md](../../basic_design/common/external_interface.md) - 外部インターフェース仕様書
* [../../basic_design/common/architecture_design.md](../../basic_design/common/architecture_design.md) - アーキテクチャ設計書
