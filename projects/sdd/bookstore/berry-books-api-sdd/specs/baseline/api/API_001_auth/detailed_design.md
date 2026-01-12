# API_001 認証API - 詳細設計書（BFFパターン）

* API ID: API_001  
* API名: 認証API  
* パターン: BFF（Backend for Frontend） - 独自実装 + 外部API連携  
* バージョン: 1.0.0  
* 最終更新: 2025-01-10

---

## 1. パッケージ構造

### 1.1 関連パッケージ

```
pro.kensait.berrybooks
├── api
│   ├── AuthenResource.java           # 認証リソース
│   ├── dto
│   │   ├── LoginRequest.java         # ログインリクエスト
│   │   ├── LoginResponse.java        # ログインレスポンス
│   │   ├── RegisterRequest.java      # 新規登録リクエスト
│   │   └── ErrorResponse.java        # エラーレスポンス
│   └── exception
│       └── (共通例外マッパー)
├── security
│   ├── JwtUtil.java                  # JWT生成・検証
│   ├── JwtAuthenFilter.java          # JWT認証フィルター
│   └── AuthenContext.java            # 認証コンテキスト（@RequestScoped）
├── service
│   └── exception
│       └── EmailAlreadyExistsException.java  # メール重複例外
└── external
    ├── CustomerHubRestClient.java    # 顧客管理API連携
    └── dto
        └── CustomerTO.java           # 顧客転送オブジェクト
```

---

## 2. クラス設計

### 2.1 AuthenResource（JAX-RS Resource）

* 責務: 認証APIのエンドポイント提供（BFF層）

* アノテーション:
  * `@Path("/auth")` - ベースパス
  * `@ApplicationScoped` - CDIスコープ

* 主要メソッド:

#### login() - ログイン

```
@POST
@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
```

* パラメータ:
  * `LoginRequest request` - ログインリクエスト
    * `String email` - メールアドレス（必須、@NotBlank、@Email）
    * `String password` - パスワード（必須、@NotBlank）

* 処理フロー:
  1. CustomerHubRestClientで顧客情報を取得（外部API呼び出し）
   * エンドポイント: `GET /customers/query_email?email={email}`
  2. BCryptでパスワード照合
  3. JwtUtilでJWT生成
  4. HttpOnly Cookieに設定
  5. LoginResponseを返却

* レスポンス: `LoginResponse`

* Cookie設定:
  * Cookie名: `berry-books-jwt`
  * HttpOnly: `true`
  * Secure: `false` (開発環境) / `true` (本番環境)
  * MaxAge: `86400秒`（24時間）
  * Path: `/`

* エラーケース:
  * メールアドレスが存在しない → `404 Not Found`
  * パスワード不一致 → `401 Unauthorized`

#### register() - 新規登録

```
@POST
@Path("/register")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
```

* パラメータ:
  * `RegisterRequest request` - 新規登録リクエスト
  * `String customerName` - 顧客名（必須）
  * `String password` - パスワード（必須）
  * `String email` - メールアドレス（必須、@Email）
  * `LocalDate birthday` - 生年月日（任意）
  * `String address` - 住所（任意、都道府県名から始まること）

* 処理フロー:
  1. 住所バリデーション（AddressUtil.startsWithValidPrefecture()）
  2. パスワードをBCryptハッシュ化
  3. CustomerHubRestClientで顧客を登録（外部API呼び出し）
   * エンドポイント: `POST /customers`
  4. JwtUtilでJWT生成
  5. HttpOnly Cookieに設定
  6. LoginResponseを返却

* レスポンス: `LoginResponse`

* エラーケース:
  * メールアドレス重複 → `409 Conflict`

#### logout() - ログアウト

```
@POST
@Path("/logout")
```

* 処理フロー:
  1. Cookie削除（MaxAge=0）
  2. 204 No Contentを返却

#### me() - 現在のログインユーザー情報取得

```
@GET
@Path("/me")
@Produces(MediaType.APPLICATION_JSON)
```

* 認証: 必須（JwtAuthenFilterで検証）

* 処理フロー:
  1. AuthenContextから顧客IDを取得
  2. CustomerHubRestClientで顧客情報を取得（外部API呼び出し）
   * エンドポイント: `GET /customers/{customerId}`
  3. LoginResponseを返却

* レスポンス: `LoginResponse`

---

### 2.2 LoginRequest（DTO - Record）

```java
public record LoginRequest(
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "有効なメールアドレスを入力してください")
    String email,
    
    @NotBlank(message = "パスワードは必須です")
    String password
) {}
```

---

### 2.3 LoginResponse（DTO - Record）

```java
public record LoginResponse(
    Integer customerId,
    String customerName,
    String email,
    LocalDate birthday,
    String address
) {}
```

---

### 2.4 RegisterRequest（DTO - Record）

```java
public record RegisterRequest(
    @NotBlank(message = "顧客名は必須です")
    @Size(max = 30, message = "顧客名は30文字以内で入力してください")
    String customerName,
    
    @NotBlank(message = "パスワードは必須です")
    String password,
    
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "有効なメールアドレスを入力してください")
    @Size(max = 30, message = "メールアドレスは30文字以内で入力してください")
    String email,
    
    LocalDate birthday,  // 任意項目
    
    @Size(max = 120, message = "住所は120文字以内で入力してください")
    String address  // 任意項目（都道府県名から始まること）
) {}
```

---

### 2.5 JwtUtil（セキュリティユーティリティ）

* 責務: JWT生成・検証（BFF層）

* アノテーション:
  * `@ApplicationScoped`

* 主要フィールド:
  * `String secretKey` - JWT署名用秘密鍵（@ConfigProperty）
  * `Long expirationMs` - トークン有効期限（@ConfigProperty）
  * `Key key` - HMAC-SHA256署名キー

* 主要メソッド:

#### generateToken()

* シグネチャ:
```java
public String generateToken(Integer customerId, String email)
```

* JWT Claims:
  * `sub`: customerId（文字列）
  * `email`: メールアドレス
  * `iat`: 発行日時（エポック秒）
  * `exp`: 有効期限（エポック秒）

* 署名アルゴリズム: `HS256`（HMAC-SHA256）

#### validateToken()

* シグネチャ:
```java
public boolean validateToken(String token)
```

#### getCustomerIdFromToken()

* シグネチャ:
```java
public Integer getCustomerIdFromToken(String token)
```

---

### 2.6 JwtAuthenFilter（認証フィルター）

* 責務: JWT認証フィルター（BFF層）

* アノテーション:
  * `@Provider`
  * `@PreMatching`
  * `@Priority(Priorities.AUTHENTICATION)`

* 処理フロー:
  1. HTTPリクエストからコンテキストパスを取得
  2. リクエストURIからコンテキストパスを除外
  3. 公開エンドポイントの場合は認証スキップ
   * `/api/auth/login`
   * `/api/auth/logout`
   * `/api/auth/register`
   * `/api/books`
   * `/api/images`
  4. CookieからJWTを抽出
  5. JwtUtilで検証
  6. 有効な場合、AuthenContextに顧客IDとメールを設定
  7. 無効な場合、401 Unauthorizedを返却

---

### 2.7 CustomerHubRestClient（外部API連携）

* 責務: 顧客管理APIとの連携（BFF特有）

* アノテーション:
  * `@ApplicationScoped`

* 主要フィールド:
  * `Client client` - JAX-RS Client
  * `String baseUrl` - 顧客管理APIベースURL（@ConfigProperty）

* 主要メソッド:

#### getCustomerByEmail()

* シグネチャ:
```java
public CustomerTO getCustomerByEmail(String email)
```

* 外部API呼び出し:
```
GET {baseUrl}/query_email?email={email}
```

#### getCustomerById()

* シグネチャ:
```java
public CustomerTO getCustomerById(Integer customerId)
```

* 外部API呼び出し:
```
GET {baseUrl}/{customerId}
```

#### createCustomer()

* シグネチャ:
```java
public CustomerTO createCustomer(RegisterRequest request, String hashedPassword)
```

* 外部API呼び出し:
```
POST {baseUrl}
Content-Type: application/json
{
  "customerName": "...",
  "password": "...",  // BCryptハッシュ
  "email": "...",
  "birthday": "...",
  "address": "..."
}
```

---

### 2.8 CustomerTO（外部API用DTO - Record）

```java
public record CustomerTO(
    Integer customerId,
    String customerName,
    String password,  // BCryptハッシュ
    String email,
    LocalDate birthday,
    String address
) {}
```

---

## 3. 設定情報

### 3.1 MicroProfile Config

* ファイル: `src/main/resources/META-INF/microprofile-config.properties`

```properties
# JWT秘密鍵（本番環境では環境変数で上書き）
jwt.secret-key=BerryBooksSecretKeyForJWT2024MustBe32CharactersOrMore

# JWT有効期限（ミリ秒）- 24時間
jwt.expiration-ms=86400000

# JWT Cookie名
jwt.cookie-name=berry-books-jwt

# 外部API: 顧客管理API
customer-hub-api.base-url=http://localhost:8080/customer-hub-api/api/customers
```

---

## 4. BFFパターンの特徴

### 4.1 外部API連携

* 顧客情報は`customer-hub-api`が管理
* BFF層は顧客エンティティを持たない
* CustomerHubRestClientを通じて外部APIを呼び出し

### 4.2 JWT認証の責務

* JWT生成・検証はBFF層で実装
* 外部APIには認証情報を転送しない
* BFF層で認証を完結

---

## 5. エラーハンドリング

### 5.1 エラーシナリオ

| エラーケース | HTTPステータス | レスポンス |
|------------|--------------|----------|
| メールアドレスが存在しない | 404 Not Found | `{"error": "Not Found", "message": "顧客が見つかりません"}` |
| パスワード不一致 | 401 Unauthorized | `{"error": "Unauthorized", "message": "認証に失敗しました"}` |
| メールアドレス重複 | 409 Conflict | `{"error": "Conflict", "message": "指定されたメールアドレスは既に登録されています"}` |
| バリデーションエラー | 400 Bad Request | `{"error": "Bad Request", "message": "メールアドレスは必須です"}` |
| 外部API接続エラー | 503 Service Unavailable | `{"error": "Service Unavailable", "message": "外部サービスに接続できません"}` |

---

## 6. テスト要件

### 6.1 ユニットテスト

* 対象: `JwtUtil`, `CustomerService`

* JWT生成テスト
* JWT検証テスト（正常系）
* JWT検証テスト（期限切れ）
* 新規登録テスト（メール重複チェック）

### 6.2 統合テスト

* 対象: `AuthenResource` + `CustomerHubRestClient`

* ログイン成功シナリオ（外部API連携）
* ログイン失敗シナリオ
* 新規登録シナリオ（外部API連携）
* Cookie設定検証

---

## 7. 参考資料

* [functional_design.md](functional_design.md) - 機能設計書
* [behaviors.md](behaviors.md) - 振る舞い仕様書
* [JWT仕様](https://jwt.io/)
* [BCrypt仕様](https://en.wikipedia.org/wiki/Bcrypt)
* [BFFパターン](https://samnewman.io/patterns/architectural/bff/)
