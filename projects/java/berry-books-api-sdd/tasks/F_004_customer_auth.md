# F-004: 顧客管理・認証

**担当者:** 担当者D（1名）  
**推奨スキル:** JSF、CDI、REST APIクライアント、セッション管理  
**想定工数:** 14時間  
**依存タスク:** common_tasks.md

---

## 概要

本タスクは、顧客管理・認証機能を実装します。新規顧客登録、ログイン、ログアウト、認証フィルター、REST API連携（berry-books-rest）を含みます。

---

## タスクリスト

### セクション1: 例外クラス

- [X] **T_F004_001**: EmailAlreadyExistsExceptionの作成
  - **目的**: メールアドレス重複時にスローするカスタム例外クラスを実装する
  - **対象**: `pro.kensait.berrybooks.service.customer.EmailAlreadyExistsException`（例外クラス）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「8. エラーハンドリング方針」
  - **注意事項**: 
    - `RuntimeException`を継承
    - フィールド: `email`（String）
    - コンストラクタで`email`を受け取る

---

### セクション2: REST APIクライアント層

- [X] **T_F004_002**: CustomerRestClientの作成
  - **目的**: berry-books-rest APIへのREST API呼び出しを実装する
  - **対象**: `pro.kensait.berrybooks.client.customer.CustomerRestClient`（REST APIクライアント）
  - **参照SPEC**: 
    - [external_interface.md](../specs/baseline/system/external_interface.md) の「2. berry-books-rest API連携」
    - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「2.2 コンポーネントの責務」
  - **注意事項**: 
    - `@ApplicationScoped`を付与
    - Jakarta REST Clientを使用（`ClientBuilder`）
    - ベースURL: `http://localhost:8080/berry-books-rest`
    - メソッド: `getCustomer(Integer customerId)`
      - エンドポイント: `GET /customers/{customerId}`
      - レスポンス: `Customer`エンティティ（JSON → オブジェクト）
      - エラーハンドリング: 404 Not Found → null返却
    - メソッド: `getCustomerByEmail(String email)`
      - エンドポイント: `GET /customers/query_email?email={email}`
      - レスポンス: `Customer`エンティティ（JSON → オブジェクト）
      - エラーハンドリング: 404 Not Found → null返却
    - メソッド: `registerCustomer(Customer customer)`
      - エンドポイント: `POST /customers/`
      - リクエストボディ: `Customer`エンティティ（オブジェクト → JSON）
      - レスポンス: `Customer`エンティティ（JSON → オブジェクト）
      - エラーハンドリング: 409 Conflict → `EmailAlreadyExistsException`をスロー
    - ログ出力: `INFO  [ CustomerRestClient#getCustomer ] customerId={}`

---

### セクション3: ビジネスロジック層

- [X] **T_F004_003**: CustomerServiceの作成
  - **目的**: 顧客管理のビジネスロジックを実装する
  - **対象**: `pro.kensait.berrybooks.service.customer.CustomerService`（サービスクラス）
  - **参照SPEC**: 
    - [functional_design.md](../specs/baseline/features/F_004_customer_auth/functional_design.md) の「6.2 ビジネスロジック層」
    - [behaviors.md](../specs/baseline/system/behaviors.md) の「BR-030, BR-031」
  - **注意事項**: 
    - `@ApplicationScoped`を付与
    - `@Inject`で`CustomerRestClient`をインジェクト
    - メソッド: `register(Customer customer)`
      - `customerRestClient.registerCustomer(customer)`を呼び出し
      - メールアドレス重複の場合: `EmailAlreadyExistsException`がスロー（BR-030）
    - メソッド: `authenticate(String email, String password)`
      - `customerRestClient.getCustomerByEmail(email)`を呼び出し
      - 顧客が存在しない場合: null返却
      - パスワードが一致しない場合: null返却（平文比較、BR-031）
      - 認証成功: `Customer`オブジェクトを返却
    - メソッド: `findByEmail(String email)`
      - `customerRestClient.getCustomerByEmail(email)`を呼び出し
    - ログ出力: `INFO  [ CustomerService#authenticate ] email={}`

---

### セクション4: プレゼンテーション層（セッションBean）

- [X] **T_F004_004**: CustomerBeanの作成
  - **目的**: 顧客情報とログイン状態を管理するセッションスコープBeanを実装する
  - **対象**: `pro.kensait.berrybooks.web.customer.CustomerBean`（セッションスコープBean）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_004_customer_auth/functional_design.md) の「6.3 プレゼンテーション層」
  - **注意事項**: 
    - `@Named`、`@SessionScoped`を付与
    - `Serializable`を実装
    - フィールド: `customer`（Customer）
    - メソッド: `isLoggedIn()`（`customer != null`を返す）
    - メソッド: `logout()`
      - セッションをクリア（`customer = null`）
      - `index.xhtml`に遷移
    - メソッド: `getCustomer()`、`setCustomer(Customer)`

---

### セクション5: プレゼンテーション層（Managed Bean）

- [X] **T_F004_005**: LoginBeanの作成
  - **目的**: ログイン処理のコントローラーを実装する
  - **対象**: `pro.kensait.berrybooks.web.login.LoginBean`（Managed Bean）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_004_customer_auth/functional_design.md) の「6.3 プレゼンテーション層」
  - **注意事項**: 
    - `@Named`、`@ViewScoped`を付与
    - `Serializable`を実装
    - `@Inject`で`CustomerService`, `CustomerBean`をインジェクト
    - フィールド: `email`（String）、`password`（String）
    - メソッド: `login()`
      - `customerService.authenticate(email, password)`を呼び出し
      - 認証成功: `CustomerBean`に顧客情報を設定、`bookSearch.xhtml`に遷移
      - 認証失敗: エラーメッセージ表示（「メールアドレスまたはパスワードが正しくありません」、BIZ-002）
    - メソッド: `navigateToRegister()`
      - `customerInput.xhtml`に遷移
    - ログ出力: `INFO  [ LoginBean#login ] email={}`

- [X] **T_F004_006**: CustomerRegisterBeanの作成
  - **目的**: 新規顧客登録処理のコントローラーを実装する
  - **対象**: `pro.kensait.berrybooks.web.customer.CustomerRegisterBean`（Managed Bean）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_004_customer_auth/functional_design.md) の「6.3 プレゼンテーション層」
  - **注意事項**: 
    - `@Named`、`@ViewScoped`を付与
    - `Serializable`を実装
    - `@Inject`で`CustomerService`をインジェクト
    - フィールド: `customer`（Customer）
    - メソッド: `register()`
      - `customerService.register(customer)`を呼び出し
      - 成功: `customerOutput.xhtml`に遷移
      - `EmailAlreadyExistsException`発生時: エラーメッセージ表示（「メールアドレスが既に登録されています」、BIZ-001）
    - ログ出力: `INFO  [ CustomerRegisterBean#register ] email={}`

---

### セクション6: ビュー層（XHTML）

- [X] **T_F004_007**: index.xhtmlの作成
  - **目的**: ログイン画面のビューを実装する
  - **対象**: `src/main/webapp/index.xhtml`（Facelets XHTML）
  - **参照SPEC**: [screen_design.md](../specs/baseline/features/F_004_customer_auth/screen_design.md) の「1. ログイン画面」
  - **注意事項**: 
    - `<h:form>`でログインフォームを作成
    - メールアドレス入力: `<h:inputText value="#{loginBean.email}"/>`（必須検証）
    - パスワード入力: `<h:inputSecret value="#{loginBean.password}"/>`（必須検証）
    - 「ログイン」ボタン: `<h:commandButton value="ログイン" action="#{loginBean.login}"/>`
    - 「新規登録」リンク: `<h:link value="新規登録" outcome="customerInput"/>`

- [X] **T_F004_008**: customerInput.xhtmlの作成
  - **目的**: 新規登録画面のビューを実装する
  - **対象**: `src/main/webapp/customerInput.xhtml`（Facelets XHTML）
  - **参照SPEC**: [screen_design.md](../specs/baseline/features/F_004_customer_auth/screen_design.md) の「2. 新規登録画面」
  - **注意事項**: 
    - `<h:form>`で登録フォームを作成
    - 顧客名入力: `<h:inputText value="#{customerRegisterBean.customer.customerName}"/>`（必須検証）
    - メールアドレス入力: `<h:inputText value="#{customerRegisterBean.customer.email}"/>`（必須検証、メールアドレス形式検証）
    - パスワード入力: `<h:inputSecret value="#{customerRegisterBean.customer.password}"/>`（必須検証）
    - 生年月日入力: `<h:inputText value="#{customerRegisterBean.customer.birthday}"/>`（任意）
    - 住所入力: `<h:inputTextarea value="#{customerRegisterBean.customer.address}"/>`（任意）
    - 「登録」ボタン: `<h:commandButton value="登録" action="#{customerRegisterBean.register}"/>`

- [X] **T_F004_009**: customerOutput.xhtmlの作成
  - **目的**: 登録完了画面のビューを実装する
  - **対象**: `src/main/webapp/customerOutput.xhtml`（Facelets XHTML）
  - **参照SPEC**: [screen_design.md](../specs/baseline/features/F_004_customer_auth/screen_design.md) の「3. 登録完了画面」
  - **注意事項**: 
    - 登録完了メッセージ表示: 「登録が完了しました」
    - 登録内容（顧客名、メールアドレス）を表示
    - 「ログイン画面へ」リンク: `<h:link value="ログイン画面へ" outcome="index"/>`

---

### セクション7: 認証フィルター（既にcommon_tasksで作成済み）

- [X] **T_F004_010**: AuthenticationFilterの動作確認
  - **目的**: 認証フィルターが正しく動作することを確認する
  - **対象**: `pro.kensait.berrybooks.web.filter.AuthenticationFilter`（common_tasksで作成済み）
  - **参照SPEC**: 
    - [functional_design.md](../specs/baseline/features/F_004_customer_auth/functional_design.md) の「6.4 認証フィルター」
    - [behaviors.md](../specs/baseline/system/behaviors.md) の「BR-033, BR-034」
  - **注意事項**: 
    - 公開ページ（`index.xhtml`, `customerInput.xhtml`, `customerOutput.xhtml`）はスキップ（BR-033）
    - 未ログインユーザーが保護ページにアクセスした場合、`index.xhtml`にリダイレクト（BR-034）
    - セッションタイムアウト（60分、BR-032）後も`index.xhtml`にリダイレクト

---

### セクション8: テスト

- [X] **T_F004_011**: CustomerServiceのユニットテストの作成
  - **目的**: CustomerServiceのビジネスロジックをテストする
  - **対象**: `src/test/java/.../service/customer/CustomerServiceTest.java`（JUnit 5テスト）
  - **参照SPEC**: 
    - [constitution.md](../memory/constitution.md) の「原則3: テスト駆動品質」
    - [functional_design.md](../specs/baseline/features/F_004_customer_auth/functional_design.md) の「6.2 ビジネスロジック層」
  - **注意事項**: 
    - Mockitoで`CustomerRestClient`をモック
    - テストケース: 
      - 顧客登録（正常系）
      - メールアドレス重複（異常系、`EmailAlreadyExistsException`）
      - 認証成功（正常系）
      - 認証失敗（異常系、null返却）

---

## 並行実行可能なタスク

以下のタスクは並行して実行できます：

- T_F004_001（例外クラス）は他タスクと独立
- T_F004_002（REST APIクライアント）は他タスクと独立
- T_F004_003（CustomerService）はT_F004_001, T_F004_002完了後
- T_F004_004（CustomerBean）はT_F004_003完了後
- T_F004_005, T_F004_006（Managed Bean）はT_F004_003, T_F004_004完了後
- T_F004_007〜009（ビュー）はT_F004_005, T_F004_006完了後
- T_F004_010（認証フィルター確認）はビュー完成後
- T_F004_011（テスト）はT_F004_003完成後

---

## 完了条件

- [X] EmailAlreadyExistsException、CustomerRestClient、CustomerServiceが実装されている
- [X] CustomerBean、LoginBean、CustomerRegisterBeanが実装されている
- [X] index.xhtml、customerInput.xhtml、customerOutput.xhtmlが実装されている
- [X] REST API連携（berry-books-rest）が正しく実装されている
- [X] 認証フィルターが正しく動作する（公開ページはスキップ、保護ページは認証必須）
- [X] メールアドレス重複チェックが正しく実装されている（BR-030）
- [X] パスワードは平文保存されている（BR-031）
- [X] セッションタイムアウトが60分に設定されている（BR-032）
- [X] 単体テストが実装され、カバレッジ80%以上
- [X] プロジェクトがビルドでき、エラーがない
- [ ] 手動テスト: 新規登録、ログイン、ログアウト、認証フィルターが正常に動作する

---

## 次のステップ

F-004完了後、他の機能タスクと並行して作業を進めてください。全機能完了後、[integration_tasks.md](integration_tasks.md) に進んでください。

