# 認証API (API_001_auth) タスク

**担当者:** 担当者A（1名）  
**推奨スキル:** Jakarta EE、JAX-RS、JWT認証、外部API連携  
**想定工数:** 6時間  
**依存タスク:** [common_tasks.md](common_tasks.md)

---

## 概要

認証APIを実装します。ログイン、ログアウト、新規登録、ユーザー情報取得の4つのエンドポイントを含みます。JWT Cookie認証とberry-books-rest API連携を実装します。

---

## タスクリスト

### 2.1 DTO/Record実装

- [ ] [P] **T_API001_001**: LoginRequest レコードの作成
  - **目的**: ログインリクエストDTOを実装する
  - **対象**: `pro.kensait.berrybooks.api.dto.LoginRequest`
  - **参照SPEC**: [api/API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「4.1 LoginRequest」
  - **注意事項**: @NotBlank, @Email アノテーションを使用

- [ ] [P] **T_API001_002**: LoginResponse レコードの作成
  - **目的**: ログインレスポンスDTOを実装する
  - **対象**: `pro.kensait.berrybooks.api.dto.LoginResponse`
  - **参照SPEC**: [api/API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「4.2 LoginResponse」
  - **注意事項**: Java Record を使用

- [ ] [P] **T_API001_003**: RegisterRequest レコードの作成
  - **目的**: 新規登録リクエストDTOを実装する
  - **対象**: `pro.kensait.berrybooks.api.dto.RegisterRequest`
  - **参照SPEC**: [api/API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「4.3 RegisterRequest」
  - **注意事項**: @NotBlank, @Email, @Size アノテーションを使用

---

### 2.2 例外クラス実装

- [ ] [P] **T_API001_004**: EmailAlreadyExistsException の作成
  - **目的**: メールアドレス重複例外を実装する
  - **対象**: `pro.kensait.berrybooks.service.customer.EmailAlreadyExistsException`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「8.1 例外階層」
  - **注意事項**: RuntimeException を継承

---

### 2.3 Resourceクラス実装

- [ ] **T_API001_005**: AuthResource の作成（ログイン機能）
  - **目的**: ログインエンドポイントを実装する
  - **対象**: `pro.kensait.berrybooks.api.AuthResource` の login() メソッド
  - **参照SPEC**: 
    - [api/API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「3.1 ログイン」
    - [api/API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) の「2. ログイン」
  - **注意事項**: 
    - @Path("/auth"), @POST, @Path("/login") を使用
    - CustomerRestClient で顧客情報を取得
    - BCrypt でパスワード検証
    - JwtUtil で JWT 生成
    - Set-Cookie ヘッダーで JWT Cookie を返す

- [ ] **T_API001_006**: AuthResource の作成（ログアウト機能）
  - **目的**: ログアウトエンドポイントを実装する
  - **対象**: `pro.kensait.berrybooks.api.AuthResource` の logout() メソッド
  - **参照SPEC**: 
    - [api/API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「3.2 ログアウト」
    - [api/API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) の「3. ログアウト」
  - **注意事項**: 
    - @POST, @Path("/logout") を使用
    - Set-Cookie ヘッダーで JWT Cookie を削除（MaxAge=0）

- [ ] **T_API001_007**: AuthResource の作成（新規登録機能）
  - **目的**: 新規登録エンドポイントを実装する
  - **対象**: `pro.kensait.berrybooks.api.AuthResource` の register() メソッド
  - **参照SPEC**: 
    - [api/API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「3.3 新規登録」
    - [api/API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) の「4. 新規登録」
  - **注意事項**: 
    - @POST, @Path("/register") を使用
    - AddressUtil で住所検証
    - BCrypt でパスワードハッシュ化
    - CustomerRestClient で顧客登録
    - JwtUtil で JWT 生成（自動ログイン）

- [ ] **T_API001_008**: AuthResource の作成（ユーザー情報取得機能）
  - **目的**: 現在のログインユーザー情報取得エンドポイントを実装する
  - **対象**: `pro.kensait.berrybooks.api.AuthResource` の getCurrentUser() メソッド
  - **参照SPEC**: 
    - [api/API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「3.4 現在のログインユーザー情報取得」
    - [api/API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) の「5. 現在のログインユーザー情報取得」
  - **注意事項**: 
    - @GET, @Path("/me") を使用
    - SecuredResource から customerId を取得
    - CustomerRestClient で顧客情報を取得

---

### 2.4 ユニットテスト

- [ ] [P] **T_API001_009**: AuthResource のユニットテスト（ログイン）
  - **目的**: ログイン機能のテストを実装する
  - **対象**: `pro.kensait.berrybooks.api.AuthResourceTest`
  - **参照SPEC**: [api/API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) の「2. ログイン」
  - **注意事項**: 
    - JUnit 5 + Mockito を使用
    - CustomerRestClient をモック
    - 正常系、メールアドレス不存在、パスワード不一致のテスト

- [ ] [P] **T_API001_010**: AuthResource のユニットテスト（新規登録）
  - **目的**: 新規登録機能のテストを実装する
  - **対象**: `pro.kensait.berrybooks.api.AuthResourceTest`
  - **参照SPEC**: [api/API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) の「4. 新規登録」
  - **注意事項**: 
    - JUnit 5 + Mockito を使用
    - CustomerRestClient をモック
    - 正常系、メールアドレス重複、住所検証エラーのテスト

---

### 2.5 APIテスト（E2E）

- [ ] **T_API001_011**: 認証APIのE2Eテスト
  - **目的**: 認証API全体のE2Eテストを実装する
  - **対象**: E2Eテストスクリプト（JUnit 5またはPlaywright）
  - **参照SPEC**: [api/API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md)
  - **注意事項**: 
    - ログイン → ユーザー情報取得 → ログアウト のフローをテスト
    - 新規登録 → 自動ログイン のフローをテスト
    - 認証エラーのテスト（JWT未設定、JWT無効）

---

## 完了条件

以下の全ての条件を満たしていること：

- [ ] 全てのDTO/Recordが作成されている
- [ ] AuthResourceの全てのエンドポイント（login, logout, register, me）が実装されている
- [ ] ユニットテストが実装され、カバレッジが80%以上である
- [ ] E2Eテストが実装され、主要フローが正常に動作する
- [ ] JWT Cookie認証が正常に機能する
- [ ] 外部API（berry-books-rest）との連携が正常に機能する

---

## 参考資料

- [api/API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) - 認証API機能設計書
- [api/API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) - 認証API受入基準
- [architecture_design.md](../specs/baseline/system/architecture_design.md) - アーキテクチャ設計書
- [external_interface.md](../specs/baseline/system/external_interface.md) - 外部インターフェース仕様書
