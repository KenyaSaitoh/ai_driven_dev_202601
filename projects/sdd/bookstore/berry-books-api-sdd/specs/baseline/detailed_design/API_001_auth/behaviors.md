# API_001_auth - 認証API 単体テスト用振る舞い仕様書

API ID: API_001_auth  
API名: 認証API  
バージョン: 1.0.0  
最終更新日: 2026-01-18  
ステータス: 単体テスト仕様確定

---

## 1. 概要

本文書は、API_001_auth（認証API）の単体テスト用の振る舞いを記述する。

* テストレベル: 単体テスト
* テストフレームワーク: JUnit 5 + Mockito
* モック対象: CustomerHubRestClient、JwtUtil、AuthenContext
* 実装対象: AddressUtil、AuthenResource

注意: 
* このbehaviors.mdは単体テスト用であり、basic_design/behaviors.md（E2Eテスト用）とは別物です
* E2Eテスト用の振る舞い仕様は[basic_design/behaviors.md](../../basic_design/behaviors.md)を参照してください

---

## 2. AddressUtil 単体テスト

### 2.1 テスト対象

* クラス: `pro.kensait.berrybooks.util.AddressUtil`
* メソッド: `validateAddress(String address)`

### 2.2 テストケース

#### T_ADDR_001: 都道府県名から始まる住所（正常系）

* Given（前提条件）:
  * 住所: "東京都渋谷区1-2-3"

* When（操作）:
  * AddressUtil.validateAddress("東京都渋谷区1-2-3")を呼び出す

* Then（期待結果）:
  * 例外が発生しない

---

#### T_ADDR_002: 都道府県名から始まらない住所（異常系）

* Given（前提条件）:
  * 住所: "渋谷区1-2-3"

* When（操作）:
  * AddressUtil.validateAddress("渋谷区1-2-3")を呼び出す

* Then（期待結果）:
  * IllegalArgumentExceptionが発生する
  * メッセージ: "住所は都道府県名から始めてください"

---

#### T_ADDR_003: null住所（異常系）

* Given（前提条件）:
  * 住所: null

* When（操作）:
  * AddressUtil.validateAddress(null)を呼び出す

* Then（期待結果）:
  * IllegalArgumentExceptionが発生する

---

#### T_ADDR_004: 空文字列住所（異常系）

* Given（前提条件）:
  * 住所: ""

* When（操作）:
  * AddressUtil.validateAddress("")を呼び出す

* Then（期待結果）:
  * IllegalArgumentExceptionが発生する

---

#### T_ADDR_005: 沖縄県の住所（正常系）

* Given（前提条件）:
  * 住所: "沖縄県那覇市1-2-3"

* When（操作）:
  * AddressUtil.validateAddress("沖縄県那覇市1-2-3")を呼び出す

* Then（期待結果）:
  * 例外が発生しない

---

#### T_ADDR_006: 北海道の住所（正常系）

* Given（前提条件）:
  * 住所: "北海道札幌市1-2-3"

* When（操作）:
  * AddressUtil.validateAddress("北海道札幌市1-2-3")を呼び出す

* Then（期待結果）:
  * 例外が発生しない

---

## 3. AuthenResource.login() 単体テスト

### 3.1 テスト対象

* クラス: `pro.kensait.berrybooks.api.AuthenResource`
* メソッド: `login(LoginRequest request)`

### 3.2 モック設定

* `@Mock private CustomerHubRestClient customerHubClient;`
* `@Mock private JwtUtil jwtUtil;`
* `@InjectMocks private AuthenResource authenResource;`

### 3.3 テストケース

#### T_LOGIN_001: ログイン成功（BCryptハッシュ）

* Given（前提条件）:
  * LoginRequest: email="alice@gmail.com", password="password"
  * CustomerTO: customerId=1, customerName="Alice", email="alice@gmail.com", password="$2a$10$..." (BCryptハッシュ), birthday=1990-01-01, address="東京都渋谷区1-2-3"
  * `customerHubClient.findByEmail("alice@gmail.com")` → CustomerTOを返却
  * `jwtUtil.generateToken(1L, "alice@gmail.com")` → "jwt-token-123"を返却
  * `jwtUtil.getCookieName()` → "berry-books-jwt"を返却

* When（操作）:
  * authenResource.login(loginRequest)を呼び出す

* Then（期待結果）:
  * Response.status = 200 OK
  * Response.entity = LoginResponse(1, "Alice", "alice@gmail.com", 1990-01-01, "東京都渋谷区1-2-3")
  * Response.cookies に "berry-books-jwt" Cookieが含まれる
  * Cookie.value = "jwt-token-123"
  * Cookie.httpOnly = true
  * Cookie.path = "/"
  * Cookie.maxAge = 86400秒

* モック検証:
  * `customerHubClient.findByEmail("alice@gmail.com")` が1回呼ばれる
  * `jwtUtil.generateToken(1L, "alice@gmail.com")` が1回呼ばれる

---

#### T_LOGIN_002: ログイン失敗（メールアドレス未登録）

* Given（前提条件）:
  * LoginRequest: email="notfound@gmail.com", password="password"
  * `customerHubClient.findByEmail("notfound@gmail.com")` → nullを返却

* When（操作）:
  * authenResource.login(loginRequest)を呼び出す

* Then（期待結果）:
  * Response.status = 401 Unauthorized
  * Response.entity = ErrorResponse(401, "Unauthorized", "メールアドレスまたはパスワードが正しくありません", "/api/auth/login")

* モック検証:
  * `customerHubClient.findByEmail("notfound@gmail.com")` が1回呼ばれる
  * `jwtUtil.generateToken()` は呼ばれない

---

#### T_LOGIN_003: ログイン失敗（パスワード不一致）

* Given（前提条件）:
  * LoginRequest: email="alice@gmail.com", password="wrongpassword"
  * CustomerTO: customerId=1, email="alice@gmail.com", password="$2a$10$..." (BCryptハッシュ)
  * `customerHubClient.findByEmail("alice@gmail.com")` → CustomerTOを返却
  * BCrypt.checkpw("wrongpassword", "$2a$10$...") → false

* When（操作）:
  * authenResource.login(loginRequest)を呼び出す

* Then（期待結果）:
  * Response.status = 401 Unauthorized
  * Response.entity = ErrorResponse(401, "Unauthorized", "メールアドレスまたはパスワードが正しくありません", "/api/auth/login")

* モック検証:
  * `customerHubClient.findByEmail("alice@gmail.com")` が1回呼ばれる
  * `jwtUtil.generateToken()` は呼ばれない

---

#### T_LOGIN_004: ログイン成功（平文パスワード、開発環境）

* Given（前提条件）:
  * LoginRequest: email="alice@gmail.com", password="password"
  * CustomerTO: customerId=1, email="alice@gmail.com", password="password" (平文)
  * `customerHubClient.findByEmail("alice@gmail.com")` → CustomerTOを返却
  * BCrypt.checkpw("password", "password") → false（BCryptハッシュでない）
  * 平文比較: "password".equals("password") → true
  * `jwtUtil.generateToken(1L, "alice@gmail.com")` → "jwt-token-123"を返却

* When（操作）:
  * authenResource.login(loginRequest)を呼び出す

* Then（期待結果）:
  * Response.status = 200 OK
  * Response.entity = LoginResponse(1, "Alice", "alice@gmail.com", ...)
  * Response.cookies に "berry-books-jwt" Cookieが含まれる

* モック検証:
  * `customerHubClient.findByEmail("alice@gmail.com")` が1回呼ばれる
  * `jwtUtil.generateToken(1L, "alice@gmail.com")` が1回呼ばれる

---

## 4. AuthenResource.logout() 単体テスト

### 4.1 テスト対象

* クラス: `pro.kensait.berrybooks.api.AuthenResource`
* メソッド: `logout()`

### 4.2 モック設定

* `@Mock private JwtUtil jwtUtil;`
* `@InjectMocks private AuthenResource authenResource;`

### 4.3 テストケース

#### T_LOGOUT_001: ログアウト成功

* Given（前提条件）:
  * `jwtUtil.getCookieName()` → "berry-books-jwt"を返却

* When（操作）:
  * authenResource.logout()を呼び出す

* Then（期待結果）:
  * Response.status = 200 OK
  * Response.cookies に "berry-books-jwt" Cookieが含まれる
  * Cookie.value = ""（空文字列）
  * Cookie.maxAge = 0（Cookie削除）
  * Cookie.httpOnly = true
  * Cookie.path = "/"

* モック検証:
  * `jwtUtil.getCookieName()` が1回呼ばれる

---

#### T_LOGOUT_002: ログアウト成功（未ログイン状態でも成功）

* Given（前提条件）:
  * JWT Cookieは未設定
  * `jwtUtil.getCookieName()` → "berry-books-jwt"を返却

* When（操作）:
  * authenResource.logout()を呼び出す

* Then（期待結果）:
  * Response.status = 200 OK
  * Response.cookies に "berry-books-jwt" Cookieが含まれる（削除Cookie）
  * Cookie.maxAge = 0

* 注意:
  * 未ログイン状態でもログアウトは成功する（冪等性）

---

## 5. AuthenResource.register() 単体テスト

### 5.1 テスト対象

* クラス: `pro.kensait.berrybooks.api.AuthenResource`
* メソッド: `register(RegisterRequest request)`

### 5.2 モック設定

* `@Mock private CustomerHubRestClient customerHubClient;`
* `@Mock private JwtUtil jwtUtil;`
* `@InjectMocks private AuthenResource authenResource;`

### 5.3 テストケース

#### T_REGISTER_001: 新規登録成功

* Given（前提条件）:
  * RegisterRequest: customerName="山田太郎", password="password123", email="yamada@example.com", birthday=1990-01-01, address="東京都渋谷区1-2-3"
  * `customerHubClient.findByEmail("yamada@example.com")` → nullを返却（メールアドレス未登録）
  * `customerHubClient.register(any(CustomerTO.class))` → CustomerTO(customerId=10, ..., password="$2a$10$...")を返却
  * `jwtUtil.generateToken(10L, "yamada@example.com")` → "jwt-token-456"を返却
  * `jwtUtil.getCookieName()` → "berry-books-jwt"を返却

* When（操作）:
  * authenResource.register(registerRequest)を呼び出す

* Then（期待結果）:
  * Response.status = 200 OK
  * Response.entity = LoginResponse(10, "山田太郎", "yamada@example.com", 1990-01-01, "東京都渋谷区1-2-3")
  * Response.cookies に "berry-books-jwt" Cookieが含まれる
  * Cookie.value = "jwt-token-456"
  * Cookie.httpOnly = true
  * Cookie.maxAge = 86400秒

* モック検証:
  * `customerHubClient.findByEmail("yamada@example.com")` が1回呼ばれる
  * `customerHubClient.register()` が1回呼ばれる
    * 引数のCustomerTO.password()はBCryptハッシュであること
  * `jwtUtil.generateToken(10L, "yamada@example.com")` が1回呼ばれる

---

#### T_REGISTER_002: 新規登録失敗（メールアドレス重複）

* Given（前提条件）:
  * RegisterRequest: email="alice@gmail.com", address="東京都渋谷区1-2-3", ...
  * `customerHubClient.findByEmail("alice@gmail.com")` → CustomerTO(customerId=1, ...)を返却（既に登録済み）

* When（操作）:
  * authenResource.register(registerRequest)を呼び出す

* Then（期待結果）:
  * EmailAlreadyExistsExceptionがスローされる
  * メッセージ: "指定されたメールアドレスは既に登録されています"

* モック検証:
  * `customerHubClient.findByEmail("alice@gmail.com")` が1回呼ばれる
  * `customerHubClient.register()` は呼ばれない
  * `jwtUtil.generateToken()` は呼ばれない

---

#### T_REGISTER_003: 新規登録失敗（住所バリデーションエラー）

* Given（前提条件）:
  * RegisterRequest: email="yamada@example.com", address="渋谷区1-2-3", ...（都道府県名なし）

* When（操作）:
  * authenResource.register(registerRequest)を呼び出す

* Then（期待結果）:
  * IllegalArgumentExceptionがスローされる
  * メッセージ: "住所は都道府県名から始めてください"

* モック検証:
  * `customerHubClient.findByEmail()` は呼ばれない（住所バリデーションが先に実行される）
  * `customerHubClient.register()` は呼ばれない

---

#### T_REGISTER_004: 新規登録成功（birthdayがnull）

* Given（前提条件）:
  * RegisterRequest: customerName="山田太郎", password="password123", email="yamada@example.com", birthday=null, address="東京都渋谷区1-2-3"
  * `customerHubClient.findByEmail("yamada@example.com")` → nullを返却
  * `customerHubClient.register(any(CustomerTO.class))` → CustomerTO(customerId=10, birthday=null, ...)を返却
  * `jwtUtil.generateToken(10L, "yamada@example.com")` → "jwt-token-456"を返却

* When（操作）:
  * authenResource.register(registerRequest)を呼び出す

* Then（期待結果）:
  * Response.status = 200 OK
  * Response.entity = LoginResponse(10, "山田太郎", "yamada@example.com", null, "東京都渋谷区1-2-3")

* モック検証:
  * `customerHubClient.register()` が1回呼ばれる
    * 引数のCustomerTO.birthday()がnullであること

---

#### T_REGISTER_005: 新規登録成功（沖縄県の住所）

* Given（前提条件）:
  * RegisterRequest: email="yamada@example.com", address="沖縄県那覇市1-2-3", ...
  * `customerHubClient.findByEmail("yamada@example.com")` → nullを返却
  * `customerHubClient.register(any(CustomerTO.class))` → CustomerTO(customerId=10, address="沖縄県那覇市1-2-3", ...)を返却
  * `jwtUtil.generateToken(10L, "yamada@example.com")` → "jwt-token-456"を返却

* When（操作）:
  * authenResource.register(registerRequest)を呼び出す

* Then（期待結果）:
  * Response.status = 200 OK
  * Response.entity = LoginResponse(10, ..., address="沖縄県那覇市1-2-3")

* モック検証:
  * `customerHubClient.register()` が1回呼ばれる

---

## 6. AuthenResource.getCurrentUser() 単体テスト

### 6.1 テスト対象

* クラス: `pro.kensait.berrybooks.api.AuthenResource`
* メソッド: `getCurrentUser()`

### 6.2 モック設定

* `@Mock private CustomerHubRestClient customerHubClient;`
* `@Mock private AuthenContext authenContext;`
* `@InjectMocks private AuthenResource authenResource;`

### 6.3 テストケース

#### T_ME_001: 現在のユーザー情報取得成功

* Given（前提条件）:
  * `authenContext.isAuthenticated()` → trueを返却
  * `authenContext.getCustomerId()` → 1Lを返却
  * CustomerTO: customerId=1, customerName="Alice", email="alice@gmail.com", birthday=1990-01-01, address="東京都渋谷区1-2-3"
  * `customerHubClient.findById(1L)` → CustomerTOを返却

* When（操作）:
  * authenResource.getCurrentUser()を呼び出す

* Then（期待結果）:
  * Response.status = 200 OK
  * Response.entity = LoginResponse(1, "Alice", "alice@gmail.com", 1990-01-01, "東京都渋谷区1-2-3")

* モック検証:
  * `authenContext.isAuthenticated()` が1回呼ばれる
  * `authenContext.getCustomerId()` が1回呼ばれる
  * `customerHubClient.findById(1L)` が1回呼ばれる

---

#### T_ME_002: 現在のユーザー情報取得失敗（未認証）

* Given（前提条件）:
  * `authenContext.isAuthenticated()` → falseを返却

* When（操作）:
  * authenResource.getCurrentUser()を呼び出す

* Then（期待結果）:
  * Response.status = 401 Unauthorized
  * Response.entity = ErrorResponse(401, "Unauthorized", "認証が必要です", "/api/auth/me")

* モック検証:
  * `authenContext.isAuthenticated()` が1回呼ばれる
  * `authenContext.getCustomerId()` は呼ばれない
  * `customerHubClient.findById()` は呼ばれない

---

#### T_ME_003: 現在のユーザー情報取得失敗（顧客が存在しない）

* Given（前提条件）:
  * `authenContext.isAuthenticated()` → trueを返却
  * `authenContext.getCustomerId()` → 999Lを返却（存在しない顧客ID）
  * `customerHubClient.findById(999L)` → nullを返却

* When（操作）:
  * authenResource.getCurrentUser()を呼び出す

* Then（期待結果）:
  * Response.status = 404 Not Found
  * Response.entity = ErrorResponse(404, "Not Found", "顧客が見つかりません", "/api/auth/me")

* モック検証:
  * `authenContext.isAuthenticated()` が1回呼ばれる
  * `authenContext.getCustomerId()` が1回呼ばれる
  * `customerHubClient.findById(999L)` が1回呼ばれる

---

## 7. 単体テスト受入基準

### 7.1 カバレッジ目標

* クラスカバレッジ: 100%
* メソッドカバレッジ: 100%
* 行カバレッジ: 80%以上

### 7.2 テスト実行環境

* JUnit 5 (Jupiter)
* Mockito 5.x
* AssertJ（アサーション）

### 7.3 テスト実行方法

```bash
# 全単体テストを実行
./gradlew :berry-books-api-sdd:test

# AddressUtilのテストのみを実行
./gradlew :berry-books-api-sdd:test --tests "*AddressUtilTest"

# AuthenResourceのテストのみを実行
./gradlew :berry-books-api-sdd:test --tests "*AuthenResourceTest"
```

---

## 8. モック化の方針

### 8.1 モック対象

| コンポーネント | モック化 | 理由 |
|------------|---------|------|
| CustomerHubRestClient | ✅ Yes | 外部API連携（タスク外の依存関係） |
| JwtUtil | ✅ Yes | JWT生成・検証（共通機能、タスク外） |
| AuthenContext | ✅ Yes | 認証コンテキスト（リクエストスコープ、タスク外） |
| AddressUtil | ❌ No | 本タスクで実装するユーティリティ（実インスタンスでテスト） |

### 8.2 モック化の理由

* タスク外の依存関係のみモックを使用
* タスク内のコンポーネント（AddressUtil、AuthenResource）は実インスタンスでテスト
* これにより、タスク内のコンポーネント間の連携をテスト

---

## 9. 参考資料

### 9.1 関連仕様書

* [detailed_design.md](detailed_design.md) - API_001_auth詳細設計書
* [../../basic_design/behaviors.md](../../basic_design/behaviors.md) - システム全体の振る舞い仕様書（E2Eテスト用）
* [../../basic_design/functional_design.md](../../basic_design/functional_design.md) - 機能設計書

### 9.2 タスク

* [../../../tasks/API_001_auth.md](../../../tasks/API_001_auth.md) - 認証APIタスク

### 9.3 Agent Skills

* [../../../../../agent_skills/jakarta-ee-api-base/principles/architecture.md](../../../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
* [../../../../../agent_skills/jakarta-ee-api-base/principles/security.md](../../../../../agent_skills/jakarta-ee-api-base/principles/security.md) - セキュリティ標準
