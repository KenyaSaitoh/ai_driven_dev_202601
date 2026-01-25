# Jakarta EE API アーキテクチャ標準

ファイル名: architecture.md  
バージョン: 3.0.0  
制定日: 2026-01-17  
最終更新日: 2026-01-17

## 概要

このドキュメントは、Jakarta EE標準Agent Skillsを使用するすべてのプロジェクトで適用されるアーキテクチャ標準、開発ガイドライン、技術的対応方針を定義する。

* 対象技術スタック: Jakarta EE 10, JPA 3.1, JAX-RS 3.1, CDI 4.0, Bean Validation 3.0
* 対応アプリケーションサーバー: Payara Server 6.x
* アーキテクチャパターン: レイヤードアーキテクチャ

---

## 1. 標準技術スタック

### 1.1 コアプラットフォーム

| レイヤー | 技術 | バージョン | 選定理由 |
|-------|-----------|---------|-----------|
| ランタイム | JDK | 21 | 最新LTS、パフォーマンス向上、新機能活用 |
| プラットフォーム | Jakarta EE | 10.0 | エンタープライズJavaの標準、豊富な機能 |
| アプリサーバー | Payara Server | 6.x | Jakarta EE 10完全対応、安定性 |
| データベース | HSQLDB | 2.7.x | 軽量、開発・テスト環境に適している |
| ビルドツール | Gradle | 8.x | 柔軟なビルド設定、依存関係管理 |

### 1.2 Jakarta EE仕様

| 仕様 | バージョン | 目的 |
|--------------|---------|---------|
| Jakarta RESTful Web Services (JAX-RS) | 3.1 | REST APIエンドポイント |
| Jakarta Persistence (JPA) | 3.1 | O/Rマッピング、データアクセス |
| Jakarta Transactions (JTA) | 2.0 | トランザクション管理 |
| Jakarta CDI | 4.0 | 依存性注入、スコープ管理 |
| Jakarta Bean Validation | 3.0 | 入力検証 |
| Jakarta Servlet | 6.0 | HTTP処理、フィルター |

### 1.3 追加ライブラリ

| ライブラリ | 目的 | 選定理由 |
|---------|---------|-----------|
| jjwt 0.12.6 | JWT生成・検証 | 安定性、セキュリティ、使いやすさ |
| BCrypt | パスワードハッシュ化 | 業界標準、レインボーテーブル攻撃耐性 |
| SLF4J + Log4j2 | ログ出力 | 構造化ログ、パフォーマンス |
| JUnit 5 | 単体テスト | モダンなテストフレームワーク |
| Mockito | モッキング | サービス層のテスト |

---

## 2. レイヤードアーキテクチャ標準

### 2.1 レイヤー構成

```
+---------------------------+
| Presentation Layer        |  JAX-RS Resources
| (API Layer)               |
+---------------------------+
           ↓
+---------------------------+
| Security Layer            |  JWT Filter, Authentication
+---------------------------+
           ↓
+---------------------------+
| Business Logic Layer      |  Services, Business Rules
+---------------------------+
           ↓
+---------------------------+
| Data Access Layer         |  DAOs, Repositories
+---------------------------+
           ↓
+---------------------------+
| Persistence Layer         |  JPA Entities
+---------------------------+
           ↓
+---------------------------+
| Database                  |  HSQLDB
+---------------------------+
```

### 2.2 レイヤーの責務

| レイヤー | 責務 | 実装方式 |
|-------|-----------------|-------------------|
| API Layer (JAX-RS Resource) | • HTTPリクエスト・レスポンス処理<br/>• JWT認証情報の取得<br/>• ビジネスロジック実行<br/>• DTOマッピング | JAX-RS `@Path`, `@GET`, `@POST`, etc. |
| Security Layer | • JWT生成・検証<br/>• Cookie管理<br/>• 認証フィルター処理<br/>• 認証情報のスレッドローカル管理 | Servlet Filter, JWT utilities |
| Service Layer | • ビジネスロジック<br/>• トランザクション境界<br/>• 外部API呼び出し（該当する場合） | `@ApplicationScoped`, `@Transactional` |
| DAO Layer | • データのCRUD操作<br/>• JPQL実行<br/>• エンティティライフサイクル管理 | JPA `EntityManager` |
| Entity Layer | • データ構造<br/>• リレーションシップ定義<br/>• データベースマッピング | JPA `@Entity`, `@Table` |

### 2.3 パッケージング規約

| レイヤー | パッケージ | 責務 | 命名規則 |
|---------|-----------|------|---------|
| Presentation | `api` | REST APIエンドポイント | `*Resource` |
| | `api.dto` | リクエスト/レスポンスDTO | `*Request`, `*Response` |
| | `api.exception` | API層の例外マッパー | `*ExceptionMapper` |
| Security | `security` | JWT認証関連 | 用途に応じた命名 |
| Business Logic | `service.*` | ビジネスロジック | `*Service` |
| Data Access | `dao` | データアクセス | `*Dao` |
| Persistence | `entity` | JPAエンティティ | エンティティ名 |
| Cross-Cutting | `common` | 共通クラス | 用途に応じた命名 |
| | `util` | ユーティリティ | `*Util` |

---

## 3. デザインパターン標準

### 3.1 適用パターン

| パターン | 目的 | 適用箇所 |
|---------|------|---------|
| REST Resource Pattern | HTTPエンドポイント提供 | `*Resource` |
| サービスレイヤー | ビジネスロジック集約 | `*Service` |
| リポジトリ (DAO) | データアクセス | `*Dao` |
| DTO (Record) | データ転送オブジェクト | `*Request`, `*Response`, `*TO` |
| JWT認証 | ステートレス認証 | `JwtAuthenFilter`, `JwtUtil` |
| 依存性注入 | 疎結合 | `@Inject` (CDI) |
| 楽観的ロック | 並行制御 | `@Version` |
| トランザクション | データ整合性 | `@Transactional` |

### 3.2 DTO設計方針

* Java Recordを優先的に使用する
* イミュータブルなデータ転送オブジェクトとして設計する
* バリデーションアノテーションを適切に配置する

---

## 4. 開発標準

### 4.1 クラス命名規則

| コンポーネントタイプ | パッケージ | クラス名パターン | 例 |
|------------------|----------|----------------|-----|
| REST Resource | `api` | `EntityName + Resource` | `AuthenResource`, `BookResource` |
| DTO (Request) | `api.dto` | `Purpose + Request` | `LoginRequest`, `OrderRequest` |
| DTO (Response) | `api.dto` | `Purpose + Response` | `LoginResponse`, `OrderResponse` |
| Exception Mapper | `api.exception` | `ExceptionName + Mapper` | `OutOfStockExceptionMapper` |
| Service | `service.*` | `EntityName + Service` | `BookService`, `OrderService` |
| DAO | `dao` | `EntityName + Dao` | `BookDao`, `StockDao` |
| Entity | `entity` | PascalCase 名詞 | `Book`, `OrderTran` |
| Exception | `service.*` | `ErrorType + Exception` | `OutOfStockException` |
| Enum | `common` | PascalCase | `SettlementType` |
| Utility | `util` / `common` | `FeatureName + Util` | `MessageUtil`, `AddressUtil` |

### 4.2 コーディング規約

* 命名規則:
  * クラス名: PascalCase
  * メソッド名: camelCase
  * 定数: UPPER_SNAKE_CASE
  * パッケージ名: lowercase

* コードフォーマット:
  * インデント: スペース4つ
  * 行の最大長: 120文字
  * Java 21の新機能を積極的に活用（Records, Sealed Classes等）

### 4.3 インポートの推奨事項

* Jakarta EEパッケージ: `jakarta.*`
* 標準Javaライブラリ: `java.*`, `javax.*`（javax.cryptoなどレガシー）
* プロジェクト内: `pro.kensait.*`
* サードパーティ: その他（jjwt、slf4j等）

### 4.4 入力バリデーション

* Bean Validation（Jakarta Bean Validation 3.0）を使用
* すべてのリクエストDTOにバリデーションアノテーションを適用

* 主要なバリデーションアノテーション:

| アノテーション | 用途 |
|------------|------|
| `@NotNull` | null禁止 |
| `@NotBlank` | 空文字、null禁止 |
| `@Size(min, max)` | 文字列長、コレクションサイズ |
| `@Min`, `@Max` | 数値の範囲 |
| `@Email` | メールアドレス形式 |
| `@Pattern(regexp)` | 正規表現パターンマッチ |

* 実装例:

```java
public record LoginRequest(
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレス形式が不正です")
    @Size(max = 30, message = "メールアドレスは30文字以内で入力してください")
    String email,
    
    @NotBlank(message = "パスワードは必須です")
    @Size(min = 1, max = 60, message = "パスワードは60文字以内で入力してください")
    String password
) {}
```

---

## 5. セキュリティ実装

### 5.1 JWT認証設定

* MicroProfile Config設定（`microprofile-config.properties`）:

```properties
# JWT秘密鍵（本番環境では環境変数で上書きすること）
jwt.secret-key=your-secret-key-at-least-32-characters-long

# JWT有効期限（ミリ秒）デフォルト: 24時間
jwt.expiration-ms=86400000

# JWT Cookie名
jwt.cookie-name=<project-name>-jwt
```

* JwtUtil初期化処理:

```java
@ApplicationScoped
public class JwtUtil {
    
    @Inject
    @ConfigProperty(name = "jwt.secret-key")
    private String secretKey;
    
    @Inject
    @ConfigProperty(name = "jwt.expiration-ms")
    private Long expirationMs;
    
    @Inject
    @ConfigProperty(name = "jwt.cookie-name")
    private String cookieName;
    
    private SecretKey key;
    
    @PostConstruct
    public void init() {
        // @ConfigPropertyが失敗した場合のフォールバック
        if (secretKey == null || secretKey.isEmpty()) {
            logger.warn("jwt.secret-key is not configured, using default value.");
            secretKey = "default-secret-key-for-development-only";
        }
        if (expirationMs == null) {
            logger.warn("jwt.expiration-ms is not configured, using default value.");
            expirationMs = 86400000L; // 24 hours
        }
        
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        logger.info("JWT secret key initialized, expiration: {} ms", expirationMs);
    }
    
    // JWT生成・検証メソッド
    // ...
}
```

### 5.2 認証フィルター実装

* JAX-RS ContainerRequestFilterを使用してJWT認証を実装
* 公開エンドポイントと認証必須エンドポイントを分離

* 実装例:

```java
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenFilter implements ContainerRequestFilter {
    
    @Inject
    private JwtUtil jwtUtil;
    
    @Inject
    private AuthenContext authenContext;
    
    @Context
    private HttpServletRequest httpServletRequest;
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestUri = requestContext.getUriInfo().getPath();
        
        // 認証不要なパス（公開API）
        if (isPublicPath(requestUri)) {
            return;
        }
        
        try {
            // CookieからJWTを抽出
            String jwt = jwtUtil.extractJwtFromRequest(httpServletRequest);
            
            if (jwt != null && jwtUtil.validateToken(jwt)) {
                // JWTからユーザー情報を取得
                Long userId = jwtUtil.getUserIdFromToken(jwt);
                String email = jwtUtil.getEmailFromToken(jwt);
                
                // AuthenContextに認証情報を設定
                authenContext.setUserId(userId);
                authenContext.setEmail(email);
            } else {
                // JWT認証必須のパスで未認証の場合はエラー
                if (isSecuredPath(requestUri)) {
                    requestContext.abortWith(
                        Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{\"error\":\"認証が必要です\"}")
                            .build()
                    );
                }
            }
        } catch (Exception e) {
            logger.error("[ JwtAuthenFilter ] Authentication error: {}", e.getMessage());
            if (isSecuredPath(requestUri)) {
                requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"認証エラー\"}")
                        .build()
                );
            }
        }
    }
    
    /**
     * 公開パス（認証不要）かどうかを判定
     * プロジェクトごとにカスタマイズ
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("auth/login") 
                || path.startsWith("auth/register")
                || path.startsWith("auth/logout")
                || path.startsWith("books")
                || path.startsWith("images");
    }
    
    /**
     * 認証必須パスかどうかを判定
     * プロジェクトごとにカスタマイズ
     */
    private boolean isSecuredPath(String path) {
        return path.startsWith("orders") 
                || path.startsWith("workflows")
                || path.startsWith("auth/me");
    }
}
```

### 5.3 認証コンテキスト管理

* `@RequestScoped` CDI Beanでユーザー情報を管理
* リクエストスコープのため、リクエスト終了時に自動的にクリーンアップされる
* 認証フィルターでインジェクションして認証情報を設定
* Resourceクラスでインジェクションして認証情報を取得

* 実装例:

```java
@RequestScoped
public class AuthenContext implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private String email;
    // プロジェクトに応じて追加のフィールド
    // (例: departmentId, employeeCode, customerId)
    
    public AuthenContext() {
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public boolean isAuthenticated() {
        return userId != null && email != null;
    }
}
```

* 認証フィルターでの使用:

```java
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenFilter implements ContainerRequestFilter {
    
    @Inject
    private JwtUtil jwtUtil;
    
    @Inject
    private AuthenContext authenContext;
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String jwt = jwtUtil.extractJwtFromRequest(httpServletRequest);
        
        if (jwt != null && jwtUtil.validateToken(jwt)) {
            Long userId = jwtUtil.getUserIdFromToken(jwt);
            String email = jwtUtil.getEmailFromToken(jwt);
            
            // AuthenContextに認証情報を設定
            authenContext.setUserId(userId);
            authenContext.setEmail(email);
        }
    }
}
```

* Resourceクラスでの使用:

```java
@Path("/orders")
@ApplicationScoped
public class OrderResource {
    
    @Inject
    private AuthenContext authenContext;
    
    @POST
    public Response createOrder(OrderRequest request) {
        // 認証情報を取得
        Long userId = authenContext.getUserId();
        
        if (!authenContext.isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        // ビジネスロジックを実行
        // ...
    }
}
```

---

## 6. トランザクション管理

### 6.1 トランザクション境界

* Serviceレイヤーのメソッドを`@Transactional`でマーク
* デフォルト: `@Transactional(TxType.REQUIRED)`
* JTAトランザクションマネージャーによる管理

### 6.2 トランザクション戦略

| トランザクション範囲 | 管理方式 | 実装 |
|------------------|---------|------|
| 単一データベース操作 | JTA宣言的トランザクション | `@Transactional` |
| 複数サービスにまたがる操作 | 結果整合性（Eventual Consistency） | サービス間連携 |

### 6.3 エラーハンドリング

* トランザクション内で例外が発生した場合、自動的にロールバック
* ビジネス例外は適切にキャッチし、クライアントに返却
* システム例外はログに記録し、500 Internal Server Errorを返却

### 6.2 並行制御（楽観的ロック）

#### 6.2.1 楽観的ロック戦略

* `@Version`アノテーションを使用
* 更新時にバージョン番号を自動的にインクリメント
* 競合発生時は`OptimisticLockException`をスロー

### 7.2 実装方法

```java
@Entity
public class Stock {
    @Id
    private Integer bookId;
    
    @Version
    private Integer version;
    
    private Integer quantity;
    
    // getters, setters
}
```

#### 6.2.3 エラーレスポンス

* HTTPステータス: 409 Conflict
* エラーメッセージ: 「データが他のユーザーによって更新されました」

### 4.5 エラーハンドリング

#### 4.5.1 例外階層

```
RuntimeException
├── BusinessException（ビジネス例外の基底クラス）
│   ├── OutOfStockException
│   ├── EmailAlreadyExistsException
│   └── その他ビジネス例外
├── OptimisticLockException（JPA標準）
└── ConstraintViolationException（Bean Validation標準）
```

#### 4.5.2 Exception Mapper

| Exception | HTTP Status | 目的 |
|-----------|------------|------|
| `BusinessException` | 409 Conflict | ビジネスルール違反 |
| `OptimisticLockException` | 409 Conflict | 楽観的ロック競合 |
| `ConstraintViolationException` | 400 Bad Request | バリデーションエラー |
| `NotFoundException` | 404 Not Found | リソース不存在 |
| `Exception` (その他) | 500 Internal Server Error | システムエラー |

#### 4.5.3 統一的なエラーレスポンス形式

* ErrorResponse構造（Java Record）:

| フィールド名 | 型 | 説明 |
|------------|---|------|
| status | int | HTTPステータスコード |
| error | String | エラー種別 |
| message | String | エラーメッセージ |
| path | String | リクエストパス |

### 4.6 ログ出力標準

#### 4.6.1 ログフレームワーク

```
SLF4J (API) → Log4j2 (Implementation)
```

* 依存関係:
  * `org.slf4j:slf4j-api:2.0.x` - SLF4J API（ロギングファサード）
  * `org.apache.logging.log4j:log4j-core:2.21.x` - Log4j2コア実装
  * `org.apache.logging.log4j:log4j-slf4j2-impl:2.21.x` - SLF4J 2.xバインディング

#### 4.6.2 ログレベル

| レベル | 用途 | 例 |
|-------|-------|-----|
| ERROR | システムエラー、予期しない例外 | データベース接続エラー、NullPointerException |
| WARN | ビジネス例外、警告 | 在庫不足、楽観的ロック競合、認証失敗 |
| INFO | メソッド開始点、主要イベント | API呼び出し、注文作成、ログイン成功 |
| DEBUG | 詳細フロー、パラメータ値 | SQLクエリ、メソッド引数、JWT検証プロセス |

#### 4.6.3 ログパターン

```
* 標準形式:
INFO  [ ClassName#methodName ] message

* パラメータ付き:
INFO  [ ClassName#methodName ] param1=value1, param2=value2

* 例外:
ERROR [ ClassName#methodName ] Error message
java.lang.RuntimeException: ...
    at ...
```

#### 4.6.4 ログ出力方針

* 出力対象:
  * 全REST APIエンドポイントのエントリ
  * 全パブリックサービスメソッドのエントリ
  * ビジネス例外の発生（WARN）
  * システム例外の発生（ERROR、スタックトレース付き）
  * 重要な状態変更（データ更新、作成など）

---

## 7. データベース構成標準

### 7.1 永続化構成

* persistence.xml:

```xml
<persistence-unit name="ProjectNamePU" transaction-type="JTA">
    <jta-data-source>jdbc/HsqldbDS</jta-data-source>
    <properties>
        <property name="jakarta.persistence.schema-generation.database.action" 
                  value="none"/>
        <property name="eclipselink.logging.level" value="FINE"/>
        <property name="eclipselink.logging.parameters" value="true"/>
    </properties>
</persistence-unit>
```

### 14.2 コネクションプール

| パラメータ | 値 | 備考 |
|-----------|-------|-------|
| JNDI名 | jdbc/HsqldbDS | DataSource JNDI ルックアップ |
| プール名 | HsqldbPool | コネクションプール識別子 |
| ドライバ | org.hsqldb.jdbc.JDBCDriver | HSQLDB JDBC ドライバ |
| URL | jdbc:hsqldb:hsql://localhost:9001/testdb | TCP接続 |
| ユーザー | SA | デフォルトHSQLDBユーザー |
| パスワード | (空) | パスワードなし |
| 最小プールサイズ | 10 | 最小接続数 |
| 最大プールサイズ | 50 | 最大接続数 |

### 7.3 データベース制約

* トランザクション分離レベル: READ_COMMITTED（デフォルト）
* 接続タイムアウト: 30秒
* アイドルタイムアウト: 300秒

---

## 8. REST API設計原則

### 8.1 基本原則

* リソース指向: エンドポイントは名詞（`/api/books`, `/api/orders`）
* HTTPメソッド: GET（取得）、POST（作成）、PUT（更新）、DELETE（削除）
* ステートレス: サーバーサイドセッションを使用しない
* JSON形式: リクエスト・レスポンスはJSON

### 8.2 HTTPステータスコード

* 200 OK: 成功
* 201 Created: 作成成功
* 400 Bad Request: バリデーションエラー
* 401 Unauthorized: 認証エラー
* 403 Forbidden: 認可エラー
* 404 Not Found: リソース不存在
* 409 Conflict: ビジネスエラー（在庫不足、楽観的ロック競合）
* 500 Internal Server Error: システムエラー

### 8.3 静的リソースアクセスのベストプラクティス

* WAR内リソースへのアクセス:

* 非推奨アプローチ:
  * ファイルシステムの相対パスを使用したFileオブジェクトの生成
  * FileInputStreamによる直接読み込み
  * 問題点: 開発環境でのみ動作、デプロイ後は動作しない

* 推奨アプローチ:
  * ServletContextをインジェクション
  * ServletContext.getResourceAsStream()を使用してリソースを取得
  * パス形式: `/resources/images/1.jpg`のようなWARルート相対パス
  * 利点: デプロイ後も正常に動作

* 理由:
  * WARファイルはアーカイブ形式で、ファイルシステムAPIでは直接アクセス不可
  * ServletContextはコンテナが提供するAPIで、WAR内外のリソースに統一的にアクセス可能
  * 開発環境と本番環境で動作を統一できる

---

## 9. テスト戦略

### 9.1 テストピラミッド

```
E2E Tests (Playwright)
    ↑
Integration Tests (JUnit 5)
    ↑
Unit Tests (JUnit 5 + Mockito)
```

### 9.2 テストアプローチ

| テストタイプ | ツール | カバレッジ目標 | 対象 |
|------------|--------|--------------|------|
| ユニットテスト | JUnit 5 + Mockito | 80%以上 | サービス層のビジネスロジック |
| 結合テスト | JUnit 5 | 主要フロー | サービス + DAO + データベース |
| E2Eテスト | Playwright | 主要シナリオ | REST API全体フロー |

* ユニットテスト:
  * サービスレイヤーの全パブリックメソッド
  * ビジネスロジックの境界値テスト
  * エラーシナリオのテスト
  * DAOをMockitoでモック

* E2Eテスト:
  * ログイン → 主要業務フロー
  * エラーケース（認証エラー、バリデーションエラー等）
  * HTTP Client（curl, Postman等）でのAPI呼び出し

---

## 10. パフォーマンス考慮事項

### 10.1 最適化戦略

| 項目 | 戦略 | 期待効果 |
|------|------|---------|
| 接続プール | 最小10、最大50接続 | データベース接続オーバーヘッド削減 |
| JWT検証キャッシュ | （将来検討）JWT検証結果のキャッシュ | CPU使用率削減 |
| インデックス | 外部キー、検索対象カラムにインデックス | クエリ実行速度向上 |
| ステートレス設計 | JWT認証、サーバーサイドセッションなし | 水平スケーリング対応 |

### 10.2 データベースアクセス

* インデックス: 主キー、外部キー、検索条件フィールド

### 10.3 キャッシング

* 現状はキャッシング未実装。将来的な実装候補:
  * JPAセカンドレベルキャッシュ（参照マスタ）
  * アプリケーションレベルキャッシュ（メモリキャッシュ）

---

## 11. 技術的対応方針

### 11.1 設定管理

* 設定ファイル: MicroProfile Config仕様に準拠

* 管理項目:
  * JWT設定（秘密鍵、有効期限、Cookie名）
  * 外部サービスURL（該当する場合）

* 実装方針:
  * 設定値はアプリケーション初期化時に明示的に読み込み
  * デフォルト値を必ず設定し、設定ファイルがない場合でも動作可能
  * 初期化ログで設定値を確認可能にする

* 採用理由:
  * 環境間での設定値の一貫性を保証
  * デプロイ時の設定ミスを早期発見
  * 開発・テスト・本番環境での柔軟な設定変更

### 11.2 依存性注入（CDI）

* 役割:
  * CDIコンテナの明示的有効化
  * 依存性注入の動作保証
  * 外部設定読み込み機能の有効化

* 設定方針: Jakarta EE 10推奨設定を使用

### 11.3 エラーハンドリング統一

* 基本方針:
  * すべてのエラーレスポンスでコンテンツタイプを明示
  * 統一されたエラーレスポンス形式（ErrorResponse DTO）
  * 適切なHTTPステータスコードの返却

* エラー分類:
  * ビジネスエラー: 409 Conflict
  * バリデーションエラー: 400 Bad Request
  * 認証エラー: 401 Unauthorized
  * 認可エラー: 403 Forbidden
  * リソース不存在: 404 Not Found
  * システムエラー: 500 Internal Server Error

### 11.4 認証・認可機構

* 公開エンドポイント:
  * ログイン、ログアウト、ユーザー登録（該当する場合）
  * プロジェクト固有の公開エンドポイント

* 認証必須エンドポイント:
  * プロジェクト固有の保護されたエンドポイント

* 認証処理フロー:
  1. リクエストからJWT Cookieを抽出
  2. JWTトークンの検証（署名、有効期限）
  3. 認証情報をスレッドローカルに保存
  4. 認証失敗時は401エラーを返却

---

## 12. 技術リスクと軽減策

| リスク | 確率 | 影響度 | 軽減策 |
|--------|------|--------|--------|
| JWT秘密鍵漏洩 | 低 | 高 | 環境変数管理、定期的な鍵ローテーション |
| 楽観的ロック競合多発 | 中 | 中 | エラーメッセージで再試行を促す、UI側でリトライ機構 |
| データベース接続枯渇 | 低 | 高 | 接続プールサイズの適切な設定、接続リーク監視 |

---

## 13. 参考資料

### 13.1 公式ドキュメント

* Jakarta EE 10: https://jakarta.ee/specifications/platform/10/
* JAX-RS 3.1: https://jakarta.ee/specifications/restful-ws/3.1/
* JPA 3.1: https://jakarta.ee/specifications/persistence/3.1/
* JWT (JSON Web Token): https://jwt.io/
* jjwt: https://github.com/jwtk/jjwt
* MicroProfile Config: https://microprofile.io/specifications/microprofile-config/

### 13.2 関連原則ドキュメント

* [common_rules.md](common_rules.md) - Jakarta EE 仕様駆動開発 共通ルール
* [security.md](security.md) - セキュリティ標準ガイドライン
