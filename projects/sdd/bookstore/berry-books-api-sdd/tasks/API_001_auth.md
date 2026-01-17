# berry-books-api-sdd - API_001_auth タスク

担当者: 担当者A（1名）  
推奨スキル: JAX-RS、JWT、BCrypt、REST Client  
想定工数: 12時間  
依存タスク: [共通機能タスク](common.md)

---

## 概要

本タスクは、認証APIの実装を行う。JWT認証、ログイン、ログアウト、新規登録、現在のログインユーザー情報取得を実装する。

* 実装パターン: 独自実装 + 外部API連携
  * JWT生成・検証は本システムで実装
  * 顧客情報はcustomer-hub-api経由で取得

---

## タスクリスト

### 1. DTO（リクエスト/レスポンス）

#### T_API001_001: LoginRequest の作成

* [ ] [P] T_API001_001: LoginRequest の作成
  * 目的: ログインリクエストDTOを実装する
  * 対象: LoginRequest.java（Java Record）
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3. API一覧」
  * 注意事項:
    * フィールド: email、password
    * Bean Validationアノテーション: @NotBlank、@Email、@Size

---

#### T_API001_002: LoginResponse の作成

* [ ] [P] T_API001_002: LoginResponse の作成
  * 目的: ログインレスポンスDTOを実装する
  * 対象: LoginResponse.java（Java Record）
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3. API一覧」
  * 注意事項:
    * フィールド: customerId、customerName、email、birthday、address
    * パスワードは含めない

---

#### T_API001_003: RegisterRequest の作成

* [ ] [P] T_API001_003: RegisterRequest の作成
  * 目的: 新規登録リクエストDTOを実装する
  * 対象: RegisterRequest.java（Java Record）
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3. API一覧」
  * 注意事項:
    * フィールド: customerName、password、email、birthday、address
    * Bean Validationアノテーション: @NotBlank、@Email、@Size、@Pattern

---

### 2. ビジネス例外

#### T_API001_004: EmailAlreadyExistsException の作成

* [ ] [P] T_API001_004: EmailAlreadyExistsException の作成
  * 目的: メールアドレス重複例外を実装する
  * 対象: EmailAlreadyExistsException.java（RuntimeException）
  * 参照SPEC: [behaviors.md](../specs/baseline/basic_design/behaviors.md) の「2.3 新規登録」
  * 注意事項:
    * メッセージ: "指定されたメールアドレスは既に登録されています"

---

#### T_API001_005: EmailAlreadyExistsExceptionMapper の作成

* [ ] [P] T_API001_005: EmailAlreadyExistsExceptionMapper の作成
  * 目的: メールアドレス重複例外を処理する
  * 対象: EmailAlreadyExistsExceptionMapper.java（ExceptionMapper）
  * 参照SPEC: [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) の「4.5.2 Exception Mapper」
  * 注意事項:
    * @Providerアノテーションを付与
    * 409 Conflictを返却
    * MediaType.APPLICATION_JSONを明示

---

### 3. ユーティリティ

#### T_API001_006: AddressUtil の作成

* [ ] [P] T_API001_006: AddressUtil の作成
  * 目的: 住所バリデーションユーティリティを実装する
  * 対象: AddressUtil.java（ユーティリティクラス）
  * 参照SPEC: [behaviors.md](../specs/baseline/basic_design/behaviors.md) の「2.3 新規登録」
  * 注意事項:
    * 実装メソッド: validateAddress(String address)
    * バリデーションルール: 住所は都道府県名から始まること
    * 無効な場合: IllegalArgumentExceptionをスロー

---

### 4. REST Resource

#### T_API001_007: AuthenResource の作成

* [ ] T_API001_007: AuthenResource の作成
  * 目的: 認証APIエンドポイントを実装する
  * 対象: AuthenResource.java（JAX-RS Resource）
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3. API一覧」
  * 注意事項:
    * @Path("/auth")を付与
    * @ApplicationScopedを付与
    * CustomerHubRestClient、JwtUtilをインジェクション
    * 実装メソッド:
      1. login(LoginRequest): JWT Cookie発行
      2. logout(): JWT Cookie削除
      3. register(RegisterRequest): 新規登録 + JWT Cookie発行
      4. getCurrentUser(): 現在のログインユーザー情報取得
    * エラーハンドリング:
      * 認証失敗: 401 Unauthorized
      * メールアドレス重複: EmailAlreadyExistsException
      * 住所バリデーションエラー: IllegalArgumentException
    * ログ出力: INFO（メソッド開始）、WARN（認証失敗）、ERROR（例外発生）

---

### 5. ユニットテスト

#### T_API001_008: AddressUtil のテスト

* [ ] [P] T_API001_008: AddressUtil のテスト
  * 目的: 住所バリデーションロジックをテストする
  * 対象: AddressUtilTest.java（JUnit 5）
  * 参照SPEC: [behaviors.md](../specs/baseline/basic_design/behaviors.md) の「2.3 新規登録」
  * 注意事項:
    * 正常系: 都道府県名から始まる住所
    * 異常系: 都道府県名から始まらない住所

---

## 成果物チェックリスト

* [ ] DTO（LoginRequest、LoginResponse、RegisterRequest）が実装されている
* [ ] ビジネス例外（EmailAlreadyExistsException、EmailAlreadyExistsExceptionMapper）が実装されている
* [ ] ユーティリティ（AddressUtil）が実装されている
* [ ] REST Resource（AuthenResource）が実装されている
* [ ] ユニットテストが作成されている
* [ ] 認証APIが正常に動作する（ログイン、ログアウト、新規登録、現在のユーザー情報取得）
* [ ] JWT Cookieが正しく発行・削除される
* [ ] エラーハンドリングが適切に動作する

---

## 動作確認

### ログインAPI（成功）

```bash
curl -X POST http://localhost:8080/berry-books-api-sdd/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@gmail.com",
    "password": "password"
  }' \
  -c cookies.txt
```

### ログアウトAPI

```bash
curl -X POST http://localhost:8080/berry-books-api-sdd/api/auth/logout \
  -b cookies.txt \
  -c cookies.txt
```

### 新規登録API

```bash
curl -X POST http://localhost:8080/berry-books-api-sdd/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "山田太郎",
    "password": "password123",
    "email": "yamada@example.com",
    "birthday": "1990-01-01",
    "address": "東京都渋谷区1-2-3"
  }' \
  -c cookies.txt
```

### 現在のユーザー情報取得API

```bash
curl -X GET http://localhost:8080/berry-books-api-sdd/api/auth/me \
  -b cookies.txt
```

---

## 参考資料

* [functional_design.md](../specs/baseline/basic_design/functional_design.md) - 機能設計書
* [behaviors.md](../specs/baseline/basic_design/behaviors.md) - 振る舞い仕様書
* [external_interface.md](../specs/baseline/basic_design/external_interface.md) - 外部インターフェース仕様書
* [security.md](../../../agent_skills/jakarta-ee-api-base/principles/security.md) - セキュリティ標準
