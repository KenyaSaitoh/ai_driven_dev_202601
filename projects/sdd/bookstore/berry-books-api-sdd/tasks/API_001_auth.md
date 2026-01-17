# API_001_auth - 認証API実装タスク

担当者: 担当者A（1名）  
推奨スキル: JAX-RS, JWT, BCrypt, 外部API連携  
想定工数: 6時間  
依存タスク: [common_tasks.md](common_tasks.md)

---

## 概要

認証APIを実装する。JWT認証、ログイン・ログアウト・新規登録・ユーザー情報取得のエンドポイントを提供する。顧客情報はcustomer-hub-apiから取得する。

---

## タスクリスト

### DTO層

#### T_API001_001: [P] LoginRequest の作成

* 目的: ログインリクエストDTOを実装する
* 対象: LoginRequest.java (Record型DTO)
* 参照SPEC: [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「4.1 LoginRequest」
* 注意事項: Record型、email, passwordフィールド、Bean Validation（@NotBlank, @Email）

---

#### T_API001_002: [P] LoginResponse の作成

* 目的: ログインレスポンスDTOを実装する
* 対象: LoginResponse.java (Record型DTO)
* 参照SPEC: [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「4.2 LoginResponse」
* 注意事項: Record型、customerId, customerName, email, birthday, addressフィールド

---

#### T_API001_003: [P] RegisterRequest の作成

* 目的: 新規登録リクエストDTOを実装する
* 対象: RegisterRequest.java (Record型DTO)
* 参照SPEC: [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「4.3 RegisterRequest」
* 注意事項: Record型、customerName, password, email, birthday, addressフィールド、Bean Validation

---

### Resource層

#### T_API001_004: AuthenResource の作成

* 目的: 認証APIのRESTエンドポイントを実装する
* 対象: AuthenResource.java (JAX-RS Resourceクラス)
* 参照SPEC: 
  * [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「3. API仕様」
  * [API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) の「2. 認証API (`/api/auth`)」
* 注意事項: 
  * @Path("/auth")
  * @ApplicationScoped
  * CustomerHubRestClientをインジェクション
  * JwtUtilをインジェクション
  * login, logout, register, getCurrentUserメソッド
  * BCrypt.checkpw()でパスワード照合
  * JWT CookieはHttpOnly, Path=/, MaxAge=86400

---

### ユニットテスト

#### T_API001_005: [P] AuthenResourceのテスト作成

* 目的: AuthenResourceのユニットテストを実装する
* 対象: AuthenResourceTest.java (JUnit 5テストクラス)
* 参照SPEC: [API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) の「2. 認証API (`/api/auth`)」
* 注意事項: 
  * JUnit 5 + Mockitoを使用
  * CustomerHubRestClient、JwtUtilをモック化
  * 正常系・異常系テストケース
  * ログイン成功、ログイン失敗（メールアドレス不存在、パスワード不一致）
  * 新規登録成功、新規登録失敗（メールアドレス重複、住所検証エラー）

---

## 完了基準

* [ ] LoginRequest DTOが実装されている
* [ ] LoginResponse DTOが実装されている
* [ ] RegisterRequest DTOが実装されている
* [ ] AuthenResource（ログイン、ログアウト、新規登録、ユーザー情報取得）が実装されている
* [ ] AuthenResourceのユニットテストが実装されている
* [ ] ログインエンドポイント（POST /api/auth/login）が動作する
* [ ] ログアウトエンドポイント（POST /api/auth/logout）が動作する
* [ ] 新規登録エンドポイント（POST /api/auth/register）が動作する
* [ ] ユーザー情報取得エンドポイント（GET /api/auth/me）が動作する

---

## 参考資料

* [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) - 認証API機能設計書
* [API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) - 認証API受入基準
* [architecture_design.md](../specs/baseline/system/architecture_design.md) - アーキテクチャ設計書
* [external_interface.md](../specs/baseline/system/external_interface.md) - 外部インターフェース仕様書
