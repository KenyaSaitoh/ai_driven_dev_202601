# FUNC_004_auth_api - 認証API

## メタデータ

* タスクID: FUNC_004
* 機能タイプ: API
* 依存タスク: FUNC_001
* 並行実行可能: FUNC_005, FUNC_006, FUNC_007
* 担当者: 担当者C
* 推奨スキル: JAX-RS, JWT, CDI, 外部API連携
* 想定工数: 6時間

## 実装内容

認証APIエンドポイントを実装する。
ログイン、ログアウト、新規登録、現在ユーザー取得の機能を提供する。

---

## タスクリスト

### 1. Resourceクラスの作成

* [ ] T_FUNC004_001: AuthenResourceの作成
  * 目的: 認証APIエンドポイントを実装する
  * 対象: pro.kensait.berrybooks.api.AuthenResource
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3.1 API実装方式」
  * 注意事項:
    * @Path("/auth")
    * @ApplicationScoped
    * JwtUtil、CustomerHubRestClient、PasswordUtil、AuthenContextを注入

* [ ] T_FUNC004_002: POST /auth/login エンドポイントの実装
  * 目的: ログイン処理を実装する
  * 対象: AuthenResource.login()
  * 参照SPEC: 
    * [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「4.1 顧客検索（メールアドレス）」
    * [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「4.1.2 認証フロー」
  * 注意事項:
    * @POST, @Path("/login"), @Consumes(APPLICATION_JSON), @Produces(APPLICATION_JSON)
    * CustomerHubRestClient.findByEmail()で顧客取得
    * PasswordUtil.verifyPassword()でパスワード検証
    * JwtUtil.generateToken()でJWT生成
    * HttpOnly CookieでJWT設定
    * LoginResponseを返却

* [ ] T_FUNC004_003: POST /auth/logout エンドポイントの実装
  * 目的: ログアウト処理を実装する
  * 対象: AuthenResource.logout()
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「4.1.2 認証フロー」
  * 注意事項:
    * @POST, @Path("/logout")
    * HttpOnly CookieのAUTH-TOKENをクリア
    * maxAge=0で即座に削除
    * 200 OKを返却

* [ ] T_FUNC004_004: POST /auth/register エンドポイントの実装
  * 目的: 新規登録処理を実装する
  * 対象: AuthenResource.register()
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「4.3 顧客登録」
  * 注意事項:
    * @POST, @Path("/register"), @Consumes(APPLICATION_JSON), @Produces(APPLICATION_JSON)
    * PasswordUtil.hashPassword()でパスワードハッシュ化
    * CustomerHubRestClient.register()で顧客登録
    * JwtUtil.generateToken()でJWT生成
    * HttpOnly CookieでJWT設定
    * LoginResponseを返却

* [ ] T_FUNC004_005: GET /auth/me エンドポイントの実装
  * 目的: 現在ログイン中のユーザー情報を取得する
  * 対象: AuthenResource.getCurrentUser()
  * 参照SPEC: 
    * [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「4.2 顧客検索（顧客ID）」
    * [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「2.2 コンポーネントの責務」
  * 注意事項:
    * @GET, @Path("/me"), @Produces(APPLICATION_JSON)
    * AuthenContextからcustomerIdを取得
    * CustomerHubRestClient.findById()で顧客情報取得
    * LoginResponseを返却

### 2. リクエスト/レスポンスDTOの作成

* [ ] T_FUNC004_006: LoginRequestの作成
  * 目的: ログインリクエストを表現するDTOを作成する
  * 対象: pro.kensait.berrybooks.api.dto.LoginRequest
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「3.2 DTO設計方針」
  * 注意事項:
    * Javaレコード型（immutable）
    * email, password
    * @NotBlank, @Email

* [ ] T_FUNC004_007: LoginResponseの作成
  * 目的: ログインレスポンスを表現するDTOを作成する
  * 対象: pro.kensait.berrybooks.api.dto.LoginResponse
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「3.2 DTO設計方針」
  * 注意事項:
    * Javaレコード型（immutable）
    * customerId, customerName, email

* [ ] T_FUNC004_008: RegisterRequestの作成
  * 目的: 新規登録リクエストを表現するDTOを作成する
  * 対象: pro.kensait.berrybooks.api.dto.RegisterRequest
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「3.2 DTO設計方針」
  * 注意事項:
    * Javaレコード型（immutable）
    * customerName, email, password, birthday, address
    * @NotBlank, @Email, @Size

### 3. Cookieハンドリング

* [ ] T_FUNC004_009: JWT Cookie設定の実装
  * 目的: HttpOnly CookieでJWTを安全に管理する
  * 対象: AuthenResource内のCookie設定ロジック
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「4.1.1 認証方式」
  * 注意事項:
    * Cookie名: AUTH-TOKEN
    * HttpOnly: true
    * Secure: false（開発環境）
    * SameSite: Lax
    * maxAge: 24時間（86400秒）
    * Path: /

* [ ] T_FUNC004_010: JWT Cookieクリアの実装
  * 目的: ログアウト時にCookieをクリアする
  * 対象: AuthenResource.logout()
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「4.1.2 認証フロー」
  * 注意事項:
    * maxAge: 0
    * Cookie削除

### 4. エラーハンドリング

* [ ] T_FUNC004_011: 認証失敗時の処理
  * 目的: パスワード不一致時にAuthenticationExceptionをスローする
  * 対象: AuthenResource.login()
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8.1 エラーレスポンス仕様」
  * 注意事項:
    * 顧客が見つからない場合: 401 Unauthorized
    * パスワード不一致の場合: 401 Unauthorized

* [ ] T_FUNC004_012: メールアドレス重複時の処理
  * 目的: 新規登録時のメールアドレス重複を処理する
  * 対象: AuthenResource.register()
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8.1 エラーレスポンス仕様」
  * 注意事項:
    * CustomerHubRestClientから409 Conflictが返された場合
    * CustomerExistsExceptionをスロー

---

## 完了条件

* [ ] ログイン処理が正常に動作する
* [ ] ログアウト処理が正常に動作する
* [ ] 新規登録処理が正常に動作する
* [ ] 現在ユーザー取得が正常に動作する
* [ ] JWT Cookieが正しく設定される
* [ ] 認証失敗時に401 Unauthorizedが返される
* [ ] メールアドレス重複時に409 Conflictが返される
* [ ] 単体テストが全て成功する

---

## 参考資料

* [../specs/baseline/basic_design/architecture_design.md](../specs/baseline/basic_design/architecture_design.md) - アーキテクチャ設計書
* [../specs/baseline/basic_design/functional_design.md](../specs/baseline/basic_design/functional_design.md) - 機能設計書
* [../specs/baseline/basic_design/external_interface.md](../specs/baseline/basic_design/external_interface.md) - 外部インターフェース仕様書
* [../specs/baseline/basic_design/behaviors.md](../specs/baseline/basic_design/behaviors.md) - 振る舞い仕様書
