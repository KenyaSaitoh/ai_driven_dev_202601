# API_001_auth - 認証APIタスク

担当者: 担当者A（1名）  
推奨スキル: JAX-RS、JWT認証、REST Client API、BCrypt  
想定工数: 6時間  
依存タスク: [common_tasks.md](common_tasks.md)

---

## タスク一覧

### DTO作成

* [X] [P] T_API001_001: LoginRequestの作成
  * 目的: ログインリクエストDTOを実装する
  * 対象: LoginRequest.java（Record）
  * 参照SPEC: [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「4.1 LoginRequest」
  * 注意事項: email、passwordフィールド、Bean Validation（@NotBlank、@Email）

---

* [X] [P] T_API001_002: LoginResponseの作成
  * 目的: ログインレスポンスDTOを実装する
  * 対象: LoginResponse.java（Record）
  * 参照SPEC: [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「4.2 LoginResponse」
  * 注意事項: customerId、customerName、email、birthday、addressフィールド

---

* [X] [P] T_API001_003: RegisterRequestの作成
  * 目的: 新規登録リクエストDTOを実装する
  * 対象: RegisterRequest.java（Record）
  * 参照SPEC: [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「4.3 RegisterRequest」
  * 注意事項: customerName、password、email、birthday、addressフィールド、Bean Validation

---

### Resource作成

* [X] T_API001_004: AuthenResourceの作成
  * 目的: 認証APIエンドポイントを実装する
  * 対象: AuthenResource.java（@Path("/auth"), @ApplicationScoped）
  * 参照SPEC: 
    * [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「3. API仕様」
    * [API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) の「2. 認証API」
  * 注意事項: login、logout、register、getCurrentUserエンドポイントを実装、CustomerHubRestClient、JwtUtilをインジェクション

---

### 認証ロジック実装

* [X] T_API001_005: loginメソッドの実装
  * 目的: ログイン処理を実装する
  * 対象: AuthenResource.login()
  * 参照SPEC: 
    * [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「3.1 ログイン」
    * [API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) の「2.1 ログイン」
  * 注意事項: メールアドレスで顧客検索、BCryptパスワード照合、JWT Cookie発行、401エラーハンドリング

---

* [X] T_API001_006: logoutメソッドの実装
  * 目的: ログアウト処理を実装する
  * 対象: AuthenResource.logout()
  * 参照SPEC: [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「3.2 ログアウト」
  * 注意事項: JWT Cookie削除（MaxAge=0）

---

* [X] T_API001_007: registerメソッドの実装
  * 目的: 新規登録処理を実装する
  * 対象: AuthenResource.register()
  * 参照SPEC: 
    * [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「3.3 新規登録」
    * [API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) の「2.3 新規登録」
  * 注意事項: パスワードBCryptハッシュ化、住所バリデーション（AddressUtil.startsWithValidPrefecture）、CustomerHubRestClient.register()呼び出し、JWT Cookie発行

---

* [X] T_API001_008: getCurrentUserメソッドの実装
  * 目的: 現在のログインユーザー情報取得処理を実装する
  * 対象: AuthenResource.getCurrentUser()
  * 参照SPEC: [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「3.4 現在のログインユーザー情報取得」
  * 注意事項: AuthenContextからcustomerIdを取得、CustomerHubRestClient.findById()呼び出し

---

### 単体テスト

* [X] [P] T_API001_009: AuthenResourceのテスト（login）
  * 目的: ログイン処理の単体テストを実装する
  * 対象: AuthenResourceTest.java（JUnit 5 + Mockito）
  * 参照SPEC: [API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) の「2.1 ログイン」
  * 注意事項: CustomerHubRestClientをモック、正常系・異常系（メールアドレス不存在、パスワード不一致）をテスト

---

* [X] [P] T_API001_010: AuthenResourceのテスト（register）
  * 目的: 新規登録処理の単体テストを実装する
  * 対象: AuthenResourceTest.java（JUnit 5 + Mockito）
  * 参照SPEC: [API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) の「2.3 新規登録」
  * 注意事項: 住所バリデーションテスト、メールアドレス重複テスト

---

### APIテスト（オプション）

* [ ] [P] T_API001_011: 認証APIのE2Eテスト
  * 目的: 認証APIのE2Eテストを実装する（オプション）
  * 対象: AuthApiE2ETest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) の「2. 認証API」
  * 注意事項: ログイン → JWT Cookie確認 → /api/auth/me → ログアウトのシナリオテスト、@Tag("e2e")でタグ付け

---

## 実装時の注意事項

### JWT Cookie設定

| 属性 | 値 | 目的 |
|-----|-----|------|
| HttpOnly | true | XSS攻撃対策（JavaScriptからアクセス不可） |
| Secure | false（開発環境）、true（本番環境） | HTTPS通信のみでCookie送信 |
| Path | / | 全パスでCookieを送信 |
| MaxAge | 86400（24時間） | Cookie有効期限 |

### パスワードハッシュ化

* アルゴリズム: BCrypt
* Cost: 10（デフォルト）
* ハッシュ長: 60文字

実装例:

```java
String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
boolean isValid = BCrypt.checkpw(plainPassword, hashedPassword);
```

### 住所バリデーション

AddressUtil.startsWithValidPrefecture()を使用して、住所が都道府県名から始まることを検証する。

---

## 参考資料

* [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) - 認証API機能設計書
* [API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) - 認証API受入基準
* [external_interface.md](../specs/baseline/system/external_interface.md) - 外部API連携仕様（customer-hub-api）
* [architecture_design.md](../specs/baseline/system/architecture_design.md) - JWT認証アーキテクチャ
