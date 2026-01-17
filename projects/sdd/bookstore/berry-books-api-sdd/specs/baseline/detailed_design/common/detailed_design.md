# berry-books-api - 共通機能詳細設計書

プロジェクトID: berry-books-api  
バージョン: 1.0.0  
最終更新日: 2026-01-18  
ステータス: 詳細設計確定

---

## 1. 概要

本文書は、berry-books-apiプロジェクトの共通機能（注文ドメインエンティティ、DAO、JWT認証基盤、外部API連携クライアント、共通DTO、例外ハンドラ等）の詳細設計を記述する。

* 実装対象: 複数のAPIで共有される共通コンポーネント
* ベースパッケージ: `pro.kensait.berrybooks`
* 参照仕様書:
  * [data_model.md](../../basic_design/data_model.md) - データモデル仕様書
  * [functional_design.md](../../basic_design/functional_design.md) - 機能設計書
  * [architecture_design.md](../../basic_design/architecture_design.md) - アーキテクチャ設計書
  * [external_interface.md](../../basic_design/external_interface.md) - 外部インターフェース仕様書

---

## 2. ドメインモデル（JPAエンティティ）

### 2.1 OrderTran（注文トランザクション）

#### 2.1.1 概要

* 責務: 注文トランザクション情報の管理
* テーブル: `ORDER_TRAN`
* パッケージ: `pro.kensait.berrybooks.entity`

#### 2.1.2 クラス定義

* クラス名: `OrderTran`
* アノテーション:
  * `@Entity` - JPAエンティティ
  * `@Table(name = "ORDER_TRAN")` - テーブルマッピング

#### 2.1.3 フィールド設計

| フィールド名 | Java型 | カラム名 | アノテーション | 説明 |
|------------|-------|---------|-------------|------|
| orderTranId | Integer | ORDER_TRAN_ID | @Id, @GeneratedValue(strategy = GenerationType.IDENTITY) | 注文トランザクションID（自動採番） |
| orderDate | LocalDate | ORDER_DATE | @Column(name = "ORDER_DATE", nullable = false) | 注文日 |
| customerId | Integer | CUSTOMER_ID | @Column(name = "CUSTOMER_ID", nullable = false) | 顧客ID（論理参照のみ、外部キー制約なし） |
| totalPrice | Integer | TOTAL_PRICE | @Column(name = "TOTAL_PRICE", nullable = false) | 注文金額合計（配送料を含む） |
| deliveryPrice | Integer | DELIVERY_PRICE | @Column(name = "DELIVERY_PRICE", nullable = false) | 配送料金 |
| deliveryAddress | String | DELIVERY_ADDRESS | @Column(name = "DELIVERY_ADDRESS", nullable = false, length = 30) | 配送先住所 |
| settlementType | Integer | SETTLEMENT_TYPE | @Column(name = "SETTLEMENT_TYPE", nullable = false) | 決済方法（1:銀行振込, 2:クレジットカード, 3:着払い） |
| orderDetails | List&lt;OrderDetail&gt; | - | @OneToMany(mappedBy = "orderTran", cascade = CascadeType.ALL, orphanRemoval = true) | 注文明細リスト |

#### 2.1.4 リレーション

* `@OneToMany(mappedBy = "orderTran", cascade = CascadeType.ALL, orphanRemoval = true)`
* 関連エンティティ: OrderDetail
* カスケード操作: 注文削除時に明細も削除

#### 2.1.5 重要な設計ポイント

* CUSTOMER_ID: 論理参照のみ（外部キー制約なし、customer-hub-apiが顧客データを管理）
* @GeneratedValue(strategy = GenerationType.IDENTITY): データベースの自動採番機能を使用
* orderDetails: 双方向関連（注文から明細へのナビゲーション）

---

### 2.2 OrderDetail（注文明細）

#### 2.2.1 概要

* 責務: 注文明細情報の管理
* テーブル: `ORDER_DETAIL`
* パッケージ: `pro.kensait.berrybooks.entity`

#### 2.2.2 クラス定義

* クラス名: `OrderDetail`
* アノテーション:
  * `@Entity` - JPAエンティティ
  * `@Table(name = "ORDER_DETAIL")` - テーブルマッピング

#### 2.2.3 フィールド設計

| フィールド名 | Java型 | カラム名 | アノテーション | 説明 |
|------------|-------|---------|-------------|------|
| id | OrderDetailPK | - | @EmbeddedId | 複合主キー |
| orderTran | OrderTran | - | @MapsId("orderTranId"), @ManyToOne(fetch = FetchType.LAZY), @JoinColumn(name = "ORDER_TRAN_ID") | 注文トランザクション（親） |
| bookId | Integer | BOOK_ID | @Column(name = "BOOK_ID", nullable = false) | 書籍ID（論理参照のみ、外部キー制約なし） |
| bookName | String | BOOK_NAME | @Column(name = "BOOK_NAME", nullable = false, length = 100) | 書籍名（スナップショット） |
| publisherName | String | PUBLISHER_NAME | @Column(name = "PUBLISHER_NAME", nullable = false, length = 50) | 出版社名（スナップショット） |
| price | Integer | PRICE | @Column(name = "PRICE", nullable = false) | 価格（スナップショット） |
| count | Integer | COUNT | @Column(name = "COUNT", nullable = false) | 注文数 |

#### 2.2.4 リレーション

* `@ManyToOne(fetch = FetchType.LAZY)`
* 関連エンティティ: OrderTran
* フェッチ戦略: LAZY（明細から注文への遅延ロード）

#### 2.2.5 重要な設計ポイント

* 複合主キー: `@EmbeddedId`を使用
* BOOK_ID: 論理参照のみ（外部キー制約なし、back-office-apiが書籍データを管理）
* スナップショットパターン: BOOK_NAME、PUBLISHER_NAME、PRICEを保存（注文時点の値）
* `@MapsId("orderTranId")`: 複合主キーの一部をリレーションシップから取得

---

### 2.3 OrderDetailPK（複合主キー）

#### 2.3.1 概要

* 責務: 注文明細の複合主キー
* パッケージ: `pro.kensait.berrybooks.entity`

#### 2.3.2 クラス定義

* クラス名: `OrderDetailPK`
* アノテーション:
  * `@Embeddable` - 埋め込み可能な値オブジェクト
* 実装インターフェース: `Serializable`

#### 2.3.3 フィールド設計

| フィールド名 | Java型 | カラム名 | アノテーション | 説明 |
|------------|-------|---------|-------------|------|
| orderTranId | Integer | ORDER_TRAN_ID | @Column(name = "ORDER_TRAN_ID") | 注文トランザクションID |
| orderDetailId | Integer | ORDER_DETAIL_ID | @Column(name = "ORDER_DETAIL_ID") | 注文明細ID（注文内で一意） |

#### 2.3.4 メソッド

* コンストラクタ:
  * `OrderDetailPK()` - デフォルトコンストラクタ（JPA要件）
  * `OrderDetailPK(Integer orderTranId, Integer orderDetailId)` - 全引数コンストラクタ

* `equals(Object o)`: 同一性判定
* `hashCode()`: ハッシュコード計算
* getter/setter: 各フィールドのアクセサ

#### 2.3.5 重要な設計ポイント

* Serializable実装: JPAの複合主キー要件
* equals(), hashCode(): 複合主キーの同一性判定に必要
* デフォルトコンストラクタ: JPA仕様の要件

---

## 3. データアクセス層（DAO）

### 3.1 OrderTranDao（注文トランザクションDAO）

#### 3.1.1 概要

* 責務: 注文トランザクションのCRUD操作
* パッケージ: `pro.kensait.berrybooks.dao`

#### 3.1.2 クラス定義

* クラス名: `OrderTranDao`
* アノテーション:
  * `@ApplicationScoped` - CDI管理Bean（シングルトン）

#### 3.1.3 依存関係

* `@Inject private EntityManager em;` - JPAエンティティマネージャー

#### 3.1.4 メソッド設計

##### insert(OrderTran orderTran)

* シグネチャ:
```java
public OrderTran insert(OrderTran orderTran)
```

* 目的: 注文トランザクションを登録
* 処理:
  1. `em.persist(orderTran)` - エンティティを永続化
  2. `em.flush()` - 即座にINSERT文を実行（IDを取得）
  3. 永続化されたOrderTranを返却

* 戻り値: 永続化されたOrderTran（orderTranIdが自動採番済み）

##### findById(Integer orderTranId)

* シグネチャ:
```java
public OrderTran findById(Integer orderTranId)
```

* 目的: 注文トランザクションIDで検索
* 処理:
  1. `em.find(OrderTran.class, orderTranId)` - 主キー検索
  2. 検索結果を返却（存在しない場合はnull）

* 戻り値: OrderTran（存在しない場合はnull）

##### findByCustomerId(Integer customerId)

* シグネチャ:
```java
public List<OrderTran> findByCustomerId(Integer customerId)
```

* 目的: 顧客IDで注文履歴を検索
* JPQL:
```sql
SELECT o FROM OrderTran o WHERE o.customerId = :customerId ORDER BY o.orderDate DESC
```

* 処理:
  1. TypedQueryを作成
  2. パラメータcustomerIdを設定
  3. getResultList()で全件取得
  4. 注文日降順でソート

* 戻り値: List&lt;OrderTran&gt;（0件の場合は空リスト）

---

### 3.2 OrderDetailDao（注文明細DAO）

#### 3.2.1 概要

* 責務: 注文明細のCRUD操作
* パッケージ: `pro.kensait.berrybooks.dao`

#### 3.2.2 クラス定義

* クラス名: `OrderDetailDao`
* アノテーション:
  * `@ApplicationScoped` - CDI管理Bean（シングルトン）

#### 3.2.3 依存関係

* `@Inject private EntityManager em;` - JPAエンティティマネージャー

#### 3.2.4 メソッド設計

##### insert(OrderDetail orderDetail)

* シグネチャ:
```java
public OrderDetail insert(OrderDetail orderDetail)
```

* 目的: 注文明細を登録
* 処理:
  1. `em.persist(orderDetail)` - エンティティを永続化
  2. 永続化されたOrderDetailを返却

* 戻り値: 永続化されたOrderDetail

##### findByOrderTranId(Integer orderTranId)

* シグネチャ:
```java
public List<OrderDetail> findByOrderTranId(Integer orderTranId)
```

* 目的: 注文トランザクションIDで明細を検索
* JPQL:
```sql
SELECT d FROM OrderDetail d WHERE d.id.orderTranId = :orderTranId ORDER BY d.id.orderDetailId
```

* 処理:
  1. TypedQueryを作成
  2. パラメータorderTranIdを設定
  3. getResultList()で全件取得
  4. 明細ID昇順でソート

* 戻り値: List&lt;OrderDetail&gt;（0件の場合は空リスト）

##### findById(OrderDetailPK id)

* シグネチャ:
```java
public OrderDetail findById(OrderDetailPK id)
```

* 目的: 複合主キーで注文明細を検索
* 処理:
  1. `em.find(OrderDetail.class, id)` - 主キー検索
  2. 検索結果を返却（存在しない場合はnull）

* 戻り値: OrderDetail（存在しない場合はnull）

---

## 4. セキュリティ層

### 4.1 JwtUtil（JWT生成・検証ユーティリティ）

#### 4.1.1 概要

* 責務: JWT生成・検証
* パッケージ: `pro.kensait.berrybooks.security`
* ライブラリ: jjwt 0.12.6

#### 4.1.2 クラス定義

* クラス名: `JwtUtil`
* アノテーション:
  * `@ApplicationScoped` - CDI管理Bean（シングルトン）

#### 4.1.3 依存関係（設定値）

* MicroProfile Configによる設定読み込み:
  * `@Inject @ConfigProperty(name = "jwt.secret-key") private String secretKey;`
  * `@Inject @ConfigProperty(name = "jwt.expiration-ms") private Long expirationMs;`
  * `@Inject @ConfigProperty(name = "jwt.cookie-name") private String cookieName;`

* フィールド:
  * `private SecretKey key;` - HMAC-SHA256秘密鍵
  * `private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);`

#### 4.1.4 初期化処理

##### init()

* シグネチャ:
```java
@PostConstruct
public void init()
```

* 目的: JWT秘密鍵の初期化
* 処理:
  1. secretKeyがnullまたは空の場合、デフォルト値を設定
  2. expirationMsがnullの場合、デフォルト値（86400000L = 24時間）を設定
  3. `Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))` - 秘密鍵生成
  4. 初期化完了ログ出力

* デフォルト値:
  * secretKey: "BerryBooksSecretKeyForJWT2024MustBe32CharactersOrMore"（開発環境用）
  * expirationMs: 86400000L（24時間）

#### 4.1.5 メソッド設計

##### generateToken(Long customerId, String email)

* シグネチャ:
```java
public String generateToken(Long customerId, String email)
```

* 目的: JWTトークン生成
* 処理:
  1. 現在時刻（now）と有効期限（expiration）を計算
  2. Jwts.builder()でJWTを構築:
    * subject: customerIdの文字列表現
    * claim("email", email): メールアドレスをクレームに追加
    * issuedAt(now): 発行時刻
    * expiration(expiration): 有効期限
    * signWith(key): HMAC-SHA256で署名
  3. JWT文字列を返却

* 戻り値: JWT文字列（例: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."）

##### validateToken(String token)

* シグネチャ:
```java
public boolean validateToken(String token)
```

* 目的: JWTトークンの検証
* 処理:
  1. Jwts.parser().verifyWith(key).build() - パーサー構築
  2. parseSignedClaims(token) - 署名検証とクレーム解析
  3. 成功時: true を返却
  4. 例外発生時（署名不正、期限切れ等）: false を返却

* 戻り値: 検証成功=true、失敗=false

##### getUserIdFromToken(String token)

* シグネチャ:
```java
public Long getUserIdFromToken(String token)
```

* 目的: JWTからユーザーIDを取得
* 処理:
  1. Jwts.parser().verifyWith(key).build() - パーサー構築
  2. parseSignedClaims(token).getPayload().getSubject() - subject取得
  3. Long.parseLong(subject) - 顧客IDに変換
  4. 顧客IDを返却

* 戻り値: 顧客ID（Long）

##### getEmailFromToken(String token)

* シグネチャ:
```java
public String getEmailFromToken(String token)
```

* 目的: JWTからメールアドレスを取得
* 処理:
  1. Jwts.parser().verifyWith(key).build() - パーサー構築
  2. parseSignedClaims(token).getPayload().get("email", String.class) - emailクレーム取得
  3. メールアドレスを返却

* 戻り値: メールアドレス（String）

##### extractJwtFromRequest(HttpServletRequest request)

* シグネチャ:
```java
public String extractJwtFromRequest(HttpServletRequest request)
```

* 目的: HTTPリクエストからJWT Cookieを抽出
* 処理:
  1. request.getCookies() - Cookieの配列を取得
  2. Cookieがnullの場合、nullを返却
  3. Cookie配列をループし、Cookie名がcookieNameと一致するものを検索
  4. 一致するCookieが見つかった場合、その値を返却
  5. 見つからない場合、nullを返却

* 戻り値: JWT文字列（Cookie未設定の場合はnull）

---

### 4.2 AuthenContext（認証コンテキスト）

#### 4.2.1 概要

* 責務: 認証コンテキスト管理（リクエストスコープ）
* パッケージ: `pro.kensait.berrybooks.security`

#### 4.2.2 クラス定義

* クラス名: `AuthenContext`
* アノテーション:
  * `@RequestScoped` - CDI管理Bean（リクエストスコープ）
* 実装インターフェース: `Serializable`

#### 4.2.3 フィールド設計

| フィールド名 | Java型 | 説明 |
|------------|-------|------|
| customerId | Long | 顧客ID（JWT Claims由来） |
| email | String | メールアドレス（JWT Claims由来） |

#### 4.2.4 メソッド設計

* コンストラクタ: `AuthenContext()` - デフォルトコンストラクタ

* getter/setter: 各フィールドのアクセサ

##### isAuthenticated()

* シグネチャ:
```java
public boolean isAuthenticated()
```

* 目的: 認証済みかどうかを判定
* 処理:
  1. customerIdとemailがともにnullでない場合、trueを返却
  2. それ以外の場合、falseを返却

* 戻り値: 認証済み=true、未認証=false

---

### 4.3 JwtAuthenFilter（JWT認証フィルター）

#### 4.3.1 概要

* 責務: JWT認証フィルター処理
* パッケージ: `pro.kensait.berrybooks.security`

#### 4.3.2 クラス定義

* クラス名: `JwtAuthenFilter`
* アノテーション:
  * `@Provider` - JAX-RS Provider
  * `@Priority(Priorities.AUTHENTICATION)` - 認証フィルターの優先度
* 実装インターフェース: `ContainerRequestFilter`

#### 4.3.3 依存関係

* `@Inject private JwtUtil jwtUtil;` - JWT生成・検証ユーティリティ
* `@Inject private AuthenContext authenContext;` - 認証コンテキスト
* `@Context private HttpServletRequest httpServletRequest;` - HTTPリクエスト

#### 4.3.4 定数定義

* 公開エンドポイント（認証不要）:
```java
private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
    "auth/login",
    "auth/logout",
    "auth/register",
    "books",
    "images",
    "/api/auth/login",
    "/api/auth/logout",
    "/api/auth/register",
    "/api/books",
    "/api/images"
);
```

* 認証必須エンドポイント:
```java
private static final Set<String> SECURED_ENDPOINTS = Set.of(
    "orders",
    "auth/me",
    "/api/orders",
    "/api/auth/me"
);
```

#### 4.3.5 メソッド設計

##### filter(ContainerRequestContext requestContext)

* シグネチャ:
```java
@Override
public void filter(ContainerRequestContext requestContext) throws IOException
```

* 目的: リクエストの認証処理
* 処理フロー:
  1. リクエストパス取得: `requestContext.getUriInfo().getPath()`
  2. 公開エンドポイント判定: `isPublicPath(requestUri)` → 認証スキップ
  3. JWT Cookie抽出: `jwtUtil.extractJwtFromRequest(httpServletRequest)`
  4. JWT検証: `jwtUtil.validateToken(jwt)`
  5. 検証成功時:
    * `jwtUtil.getUserIdFromToken(jwt)` - 顧客ID取得
    * `jwtUtil.getEmailFromToken(jwt)` - メールアドレス取得
    * `authenContext.setCustomerId(customerId)` - 認証コンテキストに設定
    * `authenContext.setEmail(email)` - 認証コンテキストに設定
  6. 検証失敗時（認証必須パスの場合）:
    * 401 Unauthorizedレスポンスを返却
    * `requestContext.abortWith(Response.status(401).type(MediaType.APPLICATION_JSON).entity(errorResponse).build())`

* エラーレスポンス例:
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "認証が必要です",
  "path": "/api/orders"
}
```

##### isPublicPath(String path)

* シグネチャ:
```java
private boolean isPublicPath(String path)
```

* 目的: 公開パス（認証不要）かどうかを判定
* 処理:
  1. PUBLIC_ENDPOINTSの各パターンとパスを比較
  2. パスがいずれかのパターンで始まる場合、trueを返却
  3. それ以外の場合、falseを返却

* 戻り値: 公開パス=true、それ以外=false

##### isSecuredPath(String path)

* シグネチャ:
```java
private boolean isSecuredPath(String path)
```

* 目的: 認証必須パスかどうかを判定
* 処理:
  1. SECURED_ENDPOINTSの各パターンとパスを比較
  2. パスがいずれかのパターンで始まる場合、trueを返却
  3. それ以外の場合、falseを返却

* 戻り値: 認証必須パス=true、それ以外=false

---

## 5. 外部API連携層

### 5.1 BackOfficeRestClient（back-office-api連携クライアント）

#### 5.1.1 概要

* 責務: back-office-apiとのREST API連携
* パッケージ: `pro.kensait.berrybooks.external`
* 連携先: back-office-api（書籍・在庫・カテゴリ管理）

#### 5.1.2 クラス定義

* クラス名: `BackOfficeRestClient`
* アノテーション:
  * `@ApplicationScoped` - CDI管理Bean（シングルトン）

#### 5.1.3 依存関係

* フィールド:
  * `private String baseUrl;` - 外部APIのベースURL
  * `private Client client;` - JAX-RS Client
  * `private static final Logger logger = LoggerFactory.getLogger(BackOfficeRestClient.class);`

#### 5.1.4 初期化処理

##### init()

* シグネチャ:
```java
@PostConstruct
public void init()
```

* 目的: REST Clientの初期化と設定読み込み
* 処理:
  1. ConfigProvider.getConfig()でMicroProfile Configを取得
  2. config.getOptionalValue("back-office-api.base-url", String.class)で設定値を読み込み
  3. 設定値が存在しない場合、デフォルト値を設定
  4. ClientBuilder.newClient() - JAX-RS Clientを生成
  5. 初期化完了ログ出力

* デフォルト値: `"http://localhost:8080/back-office-api-sdd/api"`

#### 5.1.5 メソッド設計

##### getAllBooks()

* シグネチャ:
```java
public List<BookTO> getAllBooks()
```

* 目的: 全書籍を取得
* エンドポイント: `GET /books`
* 処理:
  1. `client.target(baseUrl).path("/books")` - リクエスト構築
  2. `request(MediaType.APPLICATION_JSON)` - Content-Type設定
  3. `get(new GenericType<List<BookTO>>() {})` - GET実行
  4. 書籍リストを返却

* 戻り値: List&lt;BookTO&gt;

##### getBookById(Integer bookId)

* シグネチャ:
```java
public BookTO getBookById(Integer bookId)
```

* 目的: 書籍詳細を取得
* エンドポイント: `GET /books/{bookId}`
* 処理:
  1. `client.target(baseUrl).path("/books/{id}").resolveTemplate("id", bookId)` - パス構築
  2. `request(MediaType.APPLICATION_JSON).get(BookTO.class)` - GET実行
  3. 書籍情報を返却
  4. 404 Not Foundの場合、nullを返却

* 戻り値: BookTO（存在しない場合はnull）

##### searchBooksJpql(Integer categoryId, String keyword)

* シグネチャ:
```java
public List<BookTO> searchBooksJpql(Integer categoryId, String keyword)
```

* 目的: 書籍検索（JPQL）
* エンドポイント: `GET /books/search/jpql?categoryId={id}&keyword={keyword}`
* 処理:
  1. WebTargetを構築
  2. categoryIdがnullでない場合、クエリパラメータに追加
  3. keywordがnullまたは空でない場合、クエリパラメータに追加
  4. GET実行
  5. 書籍リストを返却

* 戻り値: List&lt;BookTO&gt;

##### searchBooksCriteria(Integer categoryId, String keyword)

* シグネチャ:
```java
public List<BookTO> searchBooksCriteria(Integer categoryId, String keyword)
```

* 目的: 書籍検索（Criteria API）
* エンドポイント: `GET /books/search/criteria?categoryId={id}&keyword={keyword}`
* 処理: searchBooksJpqlと同様

* 戻り値: List&lt;BookTO&gt;

##### getAllCategories()

* シグネチャ:
```java
public Map<String, Integer> getAllCategories()
```

* 目的: カテゴリ一覧を取得
* エンドポイント: `GET /categories`
* 処理:
  1. `client.target(baseUrl).path("/categories")` - リクエスト構築
  2. `request(MediaType.APPLICATION_JSON).get(new GenericType<Map<String, Integer>>() {})` - GET実行
  3. カテゴリマップを返却

* 戻り値: Map&lt;String, Integer&gt;（キー: カテゴリ名、値: カテゴリID）

##### findStockById(Integer bookId)

* シグネチャ:
```java
public StockTO findStockById(Integer bookId)
```

* 目的: 在庫情報を取得
* エンドポイント: `GET /stocks/{bookId}`
* 処理:
  1. `client.target(baseUrl).path("/stocks/{id}").resolveTemplate("id", bookId)` - パス構築
  2. `request(MediaType.APPLICATION_JSON).get(StockTO.class)` - GET実行
  3. 在庫情報を返却
  4. 404 Not Foundの場合、nullを返却

* 戻り値: StockTO（存在しない場合はnull）

##### updateStock(Integer bookId, Integer version, Integer newQuantity)

* シグネチャ:
```java
public StockTO updateStock(Integer bookId, Integer version, Integer newQuantity)
```

* 目的: 在庫更新（楽観的ロック対応）
* エンドポイント: `PUT /stocks/{bookId}`
* リクエストボディ:
```json
{
  "quantity": 8,
  "version": 1
}
```

* 処理:
  1. リクエストボディ構築（Map形式）
  2. `client.target(baseUrl).path("/stocks/{id}").resolveTemplate("id", bookId)` - パス構築
  3. `request(MediaType.APPLICATION_JSON).put(Entity.json(requestBody), StockTO.class)` - PUT実行
  4. 更新された在庫情報を返却
  5. 409 Conflictの場合、OptimisticLockExceptionをスロー

* 戻り値: StockTO（更新後の在庫情報）
* 例外: OptimisticLockException（楽観的ロック失敗時）

---

### 5.2 CustomerHubRestClient（customer-hub-api連携クライアント）

#### 5.2.1 概要

* 責務: customer-hub-apiとのREST API連携
* パッケージ: `pro.kensait.berrybooks.external`
* 連携先: customer-hub-api（顧客管理）

#### 5.2.2 クラス定義

* クラス名: `CustomerHubRestClient`
* アノテーション:
  * `@ApplicationScoped` - CDI管理Bean（シングルトン）

#### 5.2.3 依存関係

* フィールド:
  * `private String baseUrl;` - 外部APIのベースURL
  * `private Client client;` - JAX-RS Client
  * `private static final Logger logger = LoggerFactory.getLogger(CustomerHubRestClient.class);`

#### 5.2.4 初期化処理

##### init()

* シグネチャ:
```java
@PostConstruct
public void init()
```

* 目的: REST Clientの初期化と設定読み込み
* 処理:
  1. ConfigProvider.getConfig()でMicroProfile Configを取得
  2. config.getOptionalValue("customer-hub-api.base-url", String.class)で設定値を読み込み
  3. 設定値が存在しない場合、デフォルト値を設定
  4. ClientBuilder.newClient() - JAX-RS Clientを生成
  5. 初期化完了ログ出力

* デフォルト値: `"http://localhost:8080/customer-hub-api/api/customers"`

#### 5.2.5 メソッド設計

##### findByEmail(String email)

* シグネチャ:
```java
public CustomerTO findByEmail(String email)
```

* 目的: メールアドレスで顧客を検索
* エンドポイント: `GET /customers/query_email?email={email}`
* 処理:
  1. `client.target(baseUrl).path("/query_email").queryParam("email", email)` - リクエスト構築
  2. `request(MediaType.APPLICATION_JSON).get(CustomerTO.class)` - GET実行
  3. 顧客情報を返却
  4. 404 Not Foundの場合、nullを返却

* 戻り値: CustomerTO（存在しない場合はnull）

##### findById(Long customerId)

* シグネチャ:
```java
public CustomerTO findById(Long customerId)
```

* 目的: 顧客IDで顧客を検索
* エンドポイント: `GET /customers/{customerId}`
* 処理:
  1. `client.target(baseUrl).path("/{id}").resolveTemplate("id", customerId)` - パス構築
  2. `request(MediaType.APPLICATION_JSON).get(CustomerTO.class)` - GET実行
  3. 顧客情報を返却
  4. 404 Not Foundの場合、nullを返却

* 戻り値: CustomerTO（存在しない場合はnull）

##### register(CustomerTO customer)

* シグネチャ:
```java
public CustomerTO register(CustomerTO customer)
```

* 目的: 新規顧客を登録
* エンドポイント: `POST /customers/`
* リクエストボディ: CustomerTO（customerId=null, password=BCryptハッシュ）
* 処理:
  1. `client.target(baseUrl)` - リクエスト構築
  2. `request(MediaType.APPLICATION_JSON).post(Entity.json(customer), CustomerTO.class)` - POST実行
  3. 作成された顧客情報を返却
  4. 409 Conflictの場合、EmailAlreadyExistsExceptionをスロー

* 戻り値: CustomerTO（作成された顧客情報）
* 例外: EmailAlreadyExistsException（メールアドレス重複時）

---

## 6. 外部API用DTO

### 6.1 CustomerTO

#### 6.1.1 概要

* 責務: 顧客情報の転送オブジェクト
* パッケージ: `pro.kensait.berrybooks.external.dto`

#### 6.1.2 クラス定義

* 構造種別: Java Record（イミュータブル）
* クラス名: `CustomerTO`

#### 6.1.3 コンポーネント設計

```java
public record CustomerTO(
    Integer customerId,
    String customerName,
    String password,
    String email,
    LocalDate birthday,
    String address
) {}
```

---

### 6.2 BookTO

#### 6.2.1 概要

* 責務: 書籍情報の転送オブジェクト
* パッケージ: `pro.kensait.berrybooks.external.dto`

#### 6.2.2 クラス定義

* 構造種別: Java Record（イミュータブル）
* クラス名: `BookTO`

#### 6.2.3 コンポーネント設計

```java
public record BookTO(
    Integer bookId,
    String bookName,
    String author,
    CategoryTO category,
    PublisherTO publisher,
    Integer price,
    Integer quantity,
    Long version
) {}
```

---

### 6.3 CategoryTO

#### 6.3.1 概要

* 責務: カテゴリ情報の転送オブジェクト
* パッケージ: `pro.kensait.berrybooks.external.dto`

#### 6.3.2 クラス定義

* 構造種別: Java Record（イミュータブル）
* クラス名: `CategoryTO`

#### 6.3.3 コンポーネント設計

```java
public record CategoryTO(
    Integer categoryId,
    String categoryName
) {}
```

---

### 6.4 PublisherTO

#### 6.4.1 概要

* 責務: 出版社情報の転送オブジェクト
* パッケージ: `pro.kensait.berrybooks.external.dto`

#### 6.4.2 クラス定義

* 構造種別: Java Record（イミュータブル）
* クラス名: `PublisherTO`

#### 6.4.3 コンポーネント設計

```java
public record PublisherTO(
    Integer publisherId,
    String publisherName
) {}
```

---

### 6.5 StockTO

#### 6.5.1 概要

* 責務: 在庫情報の転送オブジェクト
* パッケージ: `pro.kensait.berrybooks.external.dto`

#### 6.5.2 クラス定義

* 構造種別: Java Record（イミュータブル）
* クラス名: `StockTO`

#### 6.5.3 コンポーネント設計

```java
public record StockTO(
    Integer bookId,
    String bookName,
    Integer quantity,
    Long version
) {}
```

---

## 7. 共通DTO

### 7.1 ErrorResponse

#### 7.1.1 概要

* 責務: 統一的なエラーレスポンス形式
* パッケージ: `pro.kensait.berrybooks.common`

#### 7.1.2 クラス定義

* 構造種別: Java Record（イミュータブル）
* クラス名: `ErrorResponse`

#### 7.1.3 コンポーネント設計

```java
public record ErrorResponse(
    int status,
    String error,
    String message,
    String path
) {}
```

#### 7.1.4 使用例

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "在庫が不足しています: Java完全理解",
  "path": "/api/orders"
}
```

---

## 8. ビジネスロジック層

### 8.1 DeliveryFeeService（配送料金計算サービス）

#### 8.1.1 概要

* 責務: 配送料金計算ロジック
* パッケージ: `pro.kensait.berrybooks.service.delivery`

#### 8.1.2 クラス定義

* クラス名: `DeliveryFeeService`
* アノテーション:
  * `@ApplicationScoped` - CDI管理Bean（シングルトン）

#### 8.1.3 定数定義

```java
private static final int FREE_SHIPPING_THRESHOLD = 10000; // 送料無料の閾値（円）
private static final int STANDARD_SHIPPING_FEE = 800;     // 標準配送料金（円）
private static final int OKINAWA_SHIPPING_FEE = 1500;     // 沖縄県配送料金（円）
```

#### 8.1.4 メソッド設計

##### calculateDeliveryFee(Integer totalPrice, String deliveryAddress)

* シグネチャ:
```java
public Integer calculateDeliveryFee(Integer totalPrice, String deliveryAddress)
```

* 目的: 配送料金を計算
* 処理:
  1. 購入金額チェック:
    * totalPrice >= 10,000円 → 送料無料（0円）
  2. 配送先住所チェック:
    * deliveryAddress.startsWith("沖縄県") → 1,500円
    * それ以外 → 800円
  3. 配送料金を返却

* 戻り値: 配送料金（Integer）

* 計算ロジック:
  * 購入金額10,000円未満の場合、標準配送料金800円
  * 購入金額10,000円以上の場合、送料無料（0円）
  * 沖縄県の場合、特別配送料金1,500円

---

### 8.2 OrderService（注文処理サービス）

#### 8.2.1 概要

* 責務: 注文処理のビジネスロジック
* パッケージ: `pro.kensait.berrybooks.service.order`

#### 8.2.2 クラス定義

* クラス名: `OrderService`
* アノテーション:
  * `@ApplicationScoped` - CDI管理Bean（シングルトン）
  * `@Transactional` - トランザクション境界

#### 8.2.3 依存関係

* `@Inject private OrderTranDao orderTranDao;` - 注文トランザクションDAO
* `@Inject private OrderDetailDao orderDetailDao;` - 注文明細DAO
* `@Inject private BackOfficeRestClient backOfficeClient;` - back-office-api連携
* `@Inject private DeliveryFeeService deliveryFeeService;` - 配送料金計算
* `private static final Logger logger = LoggerFactory.getLogger(OrderService.class);`

#### 8.2.4 メソッド設計

##### orderBooks(Long customerId, OrderRequest request)

* シグネチャ:
```java
@Transactional
public OrderTran orderBooks(Long customerId, OrderRequest request)
```

* 目的: 注文処理
* トランザクション: @Transactionalによる宣言的トランザクション管理
* 処理フロー:
  1. ログ出力: 注文処理開始
  2. カート項目ごとに在庫確認・更新:
    * `backOfficeClient.findStockById(item.bookId())` - 在庫情報取得
    * 在庫数チェック: `stock.quantity() < item.count()` → OutOfStockExceptionをスロー
    * `backOfficeClient.updateStock(item.bookId(), item.version(), newQuantity)` - 在庫更新
    * 楽観的ロック失敗時: OptimisticLockExceptionをスロー（再スロー）
  3. 配送料金計算:
    * `deliveryFeeService.calculateDeliveryFee(request.totalPrice(), request.deliveryAddress())` - 配送料金計算
  4. 注文トランザクション作成:
    * OrderTranエンティティ生成
    * `orderTranDao.insert(orderTran)` - 注文トランザクション登録
  5. 注文明細作成（カート項目ごと）:
    * OrderDetailエンティティ生成
    * 複合主キー設定: OrderDetailPK(orderTranId, detailId)
    * スナップショット保存: bookName, publisherName, price
    * `orderDetailDao.insert(orderDetail)` - 注文明細登録
  6. ログ出力: 注文処理完了
  7. 注文トランザクション返却

* 戻り値: OrderTran（作成された注文トランザクション）
* 例外:
  * OutOfStockException: 在庫不足時
  * OptimisticLockException: 楽観的ロック失敗時（トランザクションロールバック）

* トランザクション挙動:
  * 正常終了: コミット（注文トランザクション、注文明細、在庫更新がすべて確定）
  * 例外発生: ロールバック（注文トランザクション、注文明細が削除、在庫は外部API側で管理）

---

## 9. 例外ハンドラ

### 9.1 GenericExceptionMapper（一般例外マッパー）

#### 9.1.1 概要

* 責務: 一般的な例外を処理
* パッケージ: `pro.kensait.berrybooks.api.exception`

#### 9.1.2 クラス定義

* クラス名: `GenericExceptionMapper`
* アノテーション:
  * `@Provider` - JAX-RS Provider
* 実装インターフェース: `ExceptionMapper<Exception>`

#### 9.1.3 メソッド設計

##### toResponse(Exception exception)

* シグネチャ:
```java
@Override
public Response toResponse(Exception exception)
```

* 目的: 例外をHTTPレスポンスに変換
* 処理:
  1. エラーログ出力: `logger.error("Unexpected error", exception)`
  2. ErrorResponse構築:
    * status: 500
    * error: "Internal Server Error"
    * message: exception.getMessage()
    * path: (リクエストパスは取得不可、空文字列)
  3. Response返却:
    * ステータス: 500 Internal Server Error
    * Content-Type: application/json
    * ボディ: ErrorResponse

* 戻り値: Response（500 Internal Server Error）

---

### 9.2 ValidationExceptionMapper（バリデーション例外マッパー）

#### 9.2.1 概要

* 責務: Bean Validationエラーを処理
* パッケージ: `pro.kensait.berrybooks.api.exception`

#### 9.2.2 クラス定義

* クラス名: `ValidationExceptionMapper`
* アノテーション:
  * `@Provider` - JAX-RS Provider
* 実装インターフェース: `ExceptionMapper<ConstraintViolationException>`

#### 9.2.3 メソッド設計

##### toResponse(ConstraintViolationException exception)

* シグネチャ:
```java
@Override
public Response toResponse(ConstraintViolationException exception)
```

* 目的: バリデーションエラーをHTTPレスポンスに変換
* 処理:
  1. 制約違反メッセージ収集:
    * `exception.getConstraintViolations()` - 制約違反セット取得
    * 各制約違反のメッセージを連結（改行区切り）
  2. 警告ログ出力: `logger.warn("Validation error: {}", message)`
  3. ErrorResponse構築:
    * status: 400
    * error: "Bad Request"
    * message: 制約違反メッセージ
    * path: (リクエストパスは取得不可、空文字列)
  4. Response返却:
    * ステータス: 400 Bad Request
    * Content-Type: application/json
    * ボディ: ErrorResponse

* 戻り値: Response（400 Bad Request）

---

### 9.3 OptimisticLockExceptionMapper（楽観的ロック例外マッパー）

#### 9.3.1 概要

* 責務: 楽観的ロック競合エラーを処理
* パッケージ: `pro.kensait.berrybooks.api.exception`

#### 9.3.2 クラス定義

* クラス名: `OptimisticLockExceptionMapper`
* アノテーション:
  * `@Provider` - JAX-RS Provider
* 実装インターフェース: `ExceptionMapper<OptimisticLockException>`

#### 9.3.3 メソッド設計

##### toResponse(OptimisticLockException exception)

* シグネチャ:
```java
@Override
public Response toResponse(OptimisticLockException exception)
```

* 目的: 楽観的ロック競合エラーをHTTPレスポンスに変換
* 処理:
  1. 警告ログ出力: `logger.warn("Optimistic lock conflict")`
  2. ErrorResponse構築:
    * status: 409
    * error: "Conflict"
    * message: "他のユーザーが購入済みです。最新の在庫情報を確認してください。"
    * path: (リクエストパスは取得不可、空文字列)
  3. Response返却:
    * ステータス: 409 Conflict
    * Content-Type: application/json
    * ボディ: ErrorResponse

* 戻り値: Response（409 Conflict）

---

## 10. 設定ファイル

### 10.1 persistence.xml

#### 10.1.1 概要

* 目的: JPA永続化設定
* パス: `src/main/resources/META-INF/persistence.xml`

#### 10.1.2 設定内容

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
             https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"
             version="3.0">
    
    <persistence-unit name="BerryBooksPU" transaction-type="JTA">
        <jta-data-source>jdbc/HsqldbDS</jta-data-source>
        
        <!-- エンティティクラス -->
        <class>pro.kensait.berrybooks.entity.OrderTran</class>
        <class>pro.kensait.berrybooks.entity.OrderDetail</class>
        
        <properties>
            <!-- スキーマ生成は手動管理 -->
            <property name="jakarta.persistence.schema-generation.database.action" value="none"/>
            
            <!-- ログ設定（開発環境） -->
            <property name="eclipselink.logging.level" value="FINE"/>
            <property name="eclipselink.logging.parameters" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

#### 10.1.3 設定項目

* persistence-unit名: `BerryBooksPU`
* transaction-type: `JTA`（コンテナ管理トランザクション）
* jta-data-source: `jdbc/HsqldbDS`（JNDI名）
* エンティティクラス: OrderTran, OrderDetail
* schema-generation: `none`（手動管理）
* logging.level: `FINE`（開発環境用、本番環境ではINFO推奨）

---

### 10.2 microprofile-config.properties

#### 10.2.1 概要

* 目的: MicroProfile Config設定
* パス: `src/main/resources/META-INF/microprofile-config.properties`

#### 10.2.2 設定内容

```properties
# JWT設定
jwt.secret-key=BerryBooksSecretKeyForJWT2024MustBe32CharactersOrMore
jwt.expiration-ms=86400000
jwt.cookie-name=berry-books-jwt

# 外部API設定
customer-hub-api.base-url=http://localhost:8080/customer-hub-api/api/customers
back-office-api.base-url=http://localhost:8080/back-office-api-sdd/api
```

#### 10.2.3 設定項目

* JWT設定:
  * `jwt.secret-key`: JWT秘密鍵（32文字以上、本番環境では環境変数で上書き）
  * `jwt.expiration-ms`: JWT有効期限（ミリ秒、86400000 = 24時間）
  * `jwt.cookie-name`: JWT Cookie名

* 外部API設定:
  * `customer-hub-api.base-url`: customer-hub-apiのベースURL
  * `back-office-api.base-url`: back-office-apiのベースURL

* 設定読み込み優先順位:
  1. システムプロパティ
  2. 環境変数
  3. プロパティファイル
  4. デフォルト値

---

### 10.3 beans.xml

#### 10.3.1 概要

* 目的: CDIコンテナ有効化
* パス: `src/main/webapp/WEB-INF/beans.xml`

#### 10.3.2 設定内容

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
       https://jakarta.ee/xml/ns/jakartaee/beans_4_0.xsd"
       version="4.0"
       bean-discovery-mode="all">
</beans>
```

#### 10.3.3 設定項目

* bean-discovery-mode: `all`（全てのクラスをCDI Bean候補として検出）
* version: `4.0`（Jakarta EE 10標準）

* 役割:
  * CDIコンテナの有効化
  * 依存性注入（@Inject）の動作保証
  * MicroProfile Configの正常動作に必要

---

## 11. パッケージ構造

### 11.1 パッケージ一覧

```
pro.kensait.berrybooks
├── entity                        # Persistence Layer（注文エンティティのみ）
│   ├── OrderTran.java
│   ├── OrderDetail.java
│   └── OrderDetailPK.java
├── dao                           # Data Access Layer（注文データのみ）
│   ├── OrderTranDao.java
│   └── OrderDetailDao.java
├── security                      # Security Layer
│   ├── JwtUtil.java
│   ├── AuthenContext.java
│   └── JwtAuthenFilter.java
├── service                       # Business Logic Layer
│   ├── order
│   │   └── OrderService.java
│   └── delivery
│       └── DeliveryFeeService.java
├── external                      # External Integration Layer
│   ├── BackOfficeRestClient.java
│   ├── CustomerHubRestClient.java
│   └── dto
│       ├── CustomerTO.java
│       ├── BookTO.java
│       ├── CategoryTO.java
│       ├── PublisherTO.java
│       └── StockTO.java
├── api.exception                 # Exception Mappers
│   ├── GenericExceptionMapper.java
│   ├── ValidationExceptionMapper.java
│   └── OptimisticLockExceptionMapper.java
└── common                        # Common Classes
    └── ErrorResponse.java
```

---

## 12. 参考資料

### 12.1 関連仕様書

* [data_model.md](../../basic_design/data_model.md) - データモデル仕様書
* [functional_design.md](../../basic_design/functional_design.md) - 機能設計書
* [architecture_design.md](../../basic_design/architecture_design.md) - アーキテクチャ設計書
* [external_interface.md](../../basic_design/external_interface.md) - 外部インターフェース仕様書
* [behaviors.md](../../basic_design/behaviors.md) - 振る舞い仕様書（E2Eテスト用）

### 12.2 関連タスク

* [common.md](../../../tasks/common.md) - 共通機能タスク

### 12.3 プロジェクト情報

* [README.md](../../../README.md) - プロジェクトREADME

### 12.4 Agent Skills

* [architecture.md](../../../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
* [security.md](../../../../../agent_skills/jakarta-ee-api-base/principles/security.md) - セキュリティ標準
* [common_rules.md](../../../../../agent_skills/jakarta-ee-api-base/principles/common_rules.md) - 共通ルール
