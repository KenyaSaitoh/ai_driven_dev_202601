# API_001 認証API - 詳細設計書

* API ID: API_001  
* API名: 認証API  
* バージョン: 1.0.0  
* 最終更新: 2025-01-10

---

## 1. パッケージ構造

### 1.1 関連パッケージ

```
pro.kensait.backoffice
├── api
│   ├── AuthenResource.java           # 認証リソース
│   ├── dto
│   │   ├── LoginRequest.java         # ログインリクエストDTO
│   │   └── LoginResponse.java        # ログインレスポンスDTO
│   └── exception
│       └── (共通例外マッパー)
├── security
│   └── JwtUtil.java                  # JWT生成・検証ユーティリティ
├── dao
│   └── EmployeeDao.java              # 社員データアクセス
└── entity
    └── Employee.java                 # 社員エンティティ
```

---

## 2. クラス設計

### 2.1 AuthenResource（JAX-RS Resource）

* 責務: 認証APIのエンドポイント提供

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
    * `String employeeCode` - 社員コード（必須、@NotBlank）
    * `String password` - パスワード（必須、@NotBlank）

* 処理フロー:
  1. EmployeeDaoで社員情報を取得
  2. BCryptでパスワード照合
  3. JwtUtilでJWT生成
  4. HttpOnly Cookieに設定
  5. LoginResponseを返却

* レスポンス:
  * `LoginResponse` - ログイン成功レスポンス
  * `Integer employeeId` - 社員ID
  * `String employeeCode` - 社員コード
  * `String employeeName` - 社員名
  * `Integer departmentId` - 部署ID
  * `String departmentName` - 部署名
  * `Integer jobRank` - 職務ランク

* Cookie設定:
  * Cookie名: `back-office-jwt`
  * HttpOnly: `true`
  * Secure: `false` (開発環境) / `true` (本番環境)
  * MaxAge: `86400秒`（24時間）
  * Path: `/`

* エラーケース:
  * 社員コードが存在しない → `404 Not Found`
  * パスワードが一致しない → `401 Unauthorized`

---

### 2.2 LoginRequest（DTO - Record）

```java
public record LoginRequest(
    @NotBlank(message = "社員コードは必須です")
    String employeeCode,
    
    @NotBlank(message = "パスワードは必須です")
    String password
) {}
```

---

### 2.3 LoginResponse（DTO - Record）

```java
public record LoginResponse(
    Integer employeeId,
    String employeeCode,
    String employeeName,
    Integer departmentId,
    String departmentName,
    Integer jobRank
) {}
```

---

### 2.4 JwtUtil（セキュリティユーティリティ）

* 責務: JWT生成・検証

* アノテーション:
  * `@ApplicationScoped`

* 主要フィールド:
  * `String secretKey` - JWT署名用秘密鍵（@Inject @ConfigProperty）
  * `Long expirationMs` - トークン有効期限（@Inject @ConfigProperty）
  * `String cookieName` - Cookie名（@Inject @ConfigProperty）
  * `Key key` - HMAC-SHA256署名キー

* 注意: MicroProfile Config の `@ConfigProperty` を使用する場合、CDI による注入を有効にするために `@Inject` アノテーションを併用する必要があります。

* 主要メソッド:

#### generateToken()

* シグネチャ:
```java
public String generateToken(Integer employeeId, String employeeCode, Integer departmentId)
```

* JWT Claims:
  * `sub`: employeeId（文字列）
  * `employeeCode`: 社員コード
  * `departmentId`: 部署ID
  * `iat`: 発行日時（エポック秒）
  * `exp`: 有効期限（エポック秒）

* 署名アルゴリズム: `HS256`（HMAC-SHA256）

#### validateToken()

* シグネチャ:
```java
public boolean validateToken(String token)
```

* 検証項目:
  * JWT署名の検証
  * 有効期限チェック
  * フォーマット検証

---

### 2.5 EmployeeDao（データアクセス層）

* 責務: 社員データのCRUD操作

* アノテーション:
  * `@ApplicationScoped`

* 主要メソッド:

#### findByCode()

* シグネチャ:
```java
public Employee findByCode(String employeeCode)
```

* JPQL:
```sql
SELECT e FROM Employee e 
JOIN FETCH e.department 
WHERE e.employeeCode = :employeeCode
```

* 戻り値: `Employee`エンティティまたは`null`

---

### 2.6 Employee（エンティティ）

* テーブル: `EMPLOYEE`

* 主要フィールド:

| フィールド名 | 型 | カラム名 | 制約 | 説明 |
|------------|---|---------|-----|------|
| `employeeId` | `Integer` | `EMPLOYEE_ID` | `@Id @GeneratedValue` | 社員ID（主キー） |
| `employeeCode` | `String` | `EMPLOYEE_CODE` | `@Column(unique=true, nullable=false)` | 社員コード |
| `employeeName` | `String` | `EMPLOYEE_NAME` | `@Column(nullable=false)` | 社員名 |
| `password` | `String` | `PASSWORD` | `@Column(nullable=false)` | パスワード（BCryptハッシュ） |
| `jobRank` | `Integer` | `JOB_RANK` | `@Column(nullable=false)` | 職務ランク |
| `department` | `Department` | `DEPARTMENT_ID` | `@ManyToOne(fetch=FetchType.EAGER)` | 所属部署 |

* リレーション:
  * `@ManyToOne(fetch = FetchType.EAGER)` - Department（多対一）
  * EAGER フェッチ: ログイン時のレスポンスに部署情報（departmentId, departmentName）を含めるため、Department を即座にロードする必要があります
  * LAZY フェッチを使用すると、JSON-B シリアライゼーション時に「Generating incomplete JSON」エラーが発生します

---

## 3. 設定情報

### 3.1 MicroProfile Config

* ファイル: `src/main/resources/META-INF/microprofile-config.properties`

```properties
# JWT秘密鍵（本番環境では環境変数で上書き）

jwt.secret-key=BackOfficeSecretKeyForJWT2024MustBe32CharactersOrMore

# JWT有効期限（ミリ秒）- 24時間

jwt.expiration-ms=86400000

# JWT Cookie名

jwt.cookie-name=back-office-jwt
```

---

## 4. セキュリティ考慮事項

### 4.1 パスワード管理

* ハッシュ化: BCrypt（ワークファクター: 10）
* 保存形式: ハッシュ値のみ保存（平文保存禁止）
* 照合: `BCrypt.checkpw(plainPassword, hashedPassword)`

### 4.2 JWT セキュリティ

* Cookie設定:
  * `HttpOnly`: `true` - XSS攻撃対策
  * `Secure`: `true`（本番） - HTTPS通信のみ
  * `SameSite`: 未設定（デフォルト: Lax）

* 秘密鍵管理:
  * 環境変数で管理（本番環境）
  * 最低32文字以上
  * 定期的なローテーション推奨

### 4.3 ブルートフォース攻撃対策

* 現状: 未実装  
* 将来検討: レート制限、アカウントロック機構

---

## 5. エラーハンドリング

### 5.1 エラーシナリオ

| エラーケース | HTTPステータス | レスポンス |
|------------|--------------|----------|
| 社員コードが存在しない | 404 Not Found | `{"error": "Not Found", "message": "社員が見つかりません"}` |
| パスワード不一致 | 401 Unauthorized | `{"error": "Unauthorized", "message": "認証に失敗しました"}` |
| バリデーションエラー | 400 Bad Request | `{"error": "Bad Request", "message": "社員コードは必須です"}` |
| JWTエラー | 500 Internal Server Error | `{"error": "Internal Server Error", "message": "認証トークンの生成に失敗しました"}` |

---

## 6. テスト要件

### 6.1 ユニットテスト

* 対象: `JwtUtil`

* JWT生成テスト
* JWT検証テスト（正常系）
* JWT検証テスト（期限切れ）
* JWT検証テスト（不正な署名）

### 6.2 統合テスト

* 対象: `AuthenResource` + `EmployeeDao`

* ログイン成功シナリオ
* ログイン失敗シナリオ（社員コード不正）
* ログイン失敗シナリオ（パスワード不正）
* Cookie設定検証

---

## 7. 参考資料

* [functional_design.md](functional_design.md) - 機能設計書
* [behaviors.md](behaviors.md) - 振る舞い仕様書
* [JWT仕様](https://jwt.io/)
* [BCrypt仕様](https://en.wikipedia.org/wiki/Bcrypt)
