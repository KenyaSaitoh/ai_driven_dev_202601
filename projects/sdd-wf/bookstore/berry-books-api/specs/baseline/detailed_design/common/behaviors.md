# common - 振る舞い仕様書（単体テスト用）

ドメイン名: common  
バージョン: 1.0.0  
最終更新日: 2026-02-07

記法: シナリオは Gherkin 記法で記述する（Feature, Scenario, Given, When, Then, And, But）。principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照すること。

---

## 1. 概要

本文書は、commonドメインの単体テスト用の振る舞い、テストシナリオ、受入基準を記述する。

テスト対象:
* Entity（OrderTran、OrderDetail、OrderDetailPK）
* DAO（OrderTranDao、OrderDetailDao）
* Security（JwtUtil、JwtAuthenFilter、AuthenticatedUser）
* External（BackOfficeRestClient、CustomerHubRestClient）
* Util（PasswordUtil）

単体テストの範囲:
* commonドメイン内の機能をテスト
* ドメイン外の依存（外部API、EntityManager等）はモック化

関連ドキュメント:
* [detailed_design.md](detailed_design.md) - 詳細設計書
* [../../basic_design/common/functional_design.md](../../basic_design/common/functional_design.md) - 共通機能設計書

---

## 2. テストシナリオ

### 2.1 OrderTranDao - 注文トランザクション登録

#### Feature: 注文トランザクションの登録

#### Scenario: 新規注文を登録する

* Given（前提条件）:
  * EntityManagerがモック化されている
  * OrderTranオブジェクトが準備されている

* When（操作）:
  * OrderTranDao.insert(orderTran)を呼び出す

* Then（期待結果）:
  * EntityManager.persist()が呼び出される
  * 引数として渡されたOrderTranが返される

#### テストデータ
* 入力:
  ```
  orderDate: 2026-02-07
  customerId: 1
  totalPrice: 5400
  deliveryPrice: 400
  deliveryAddress: "東京都渋谷区1-1-1"
  settlementType: 2
  ```

---

### 2.2 OrderTranDao - 注文履歴検索

#### Feature: 顧客IDで注文履歴を検索

#### Scenario: 顧客の注文履歴を新しい順で取得する

* Given（前提条件）:
  * EntityManagerがモック化されている
  * 顧客ID=1の注文が複数存在する
  * モック設定: TypedQueryが注文リストを返す（ORDER BY orderDate DESC）

* When（操作）:
  * OrderTranDao.findByCustomerId(1)を呼び出す

* Then（期待結果）:
  * JPQLクエリが実行される: "SELECT o FROM OrderTran o WHERE o.customerId = :customerId ORDER BY o.orderDate DESC"
  * 注文リストが注文日の降順で返される

* And（追加の検証）:
  * リストの最初の要素が最新の注文である

#### テストデータ（境界値）
* 顧客ID: 1（存在する顧客）
* 顧客ID: 999（存在しない顧客） → 空リストを返す

---

### 2.3 OrderDetailDao - 注文明細登録

#### Feature: 注文明細の登録

#### Scenario: スナップショットパターンで注文明細を登録する

* Given（前提条件）:
  * EntityManagerがモック化されている
  * OrderDetailオブジェクトが準備されている
  * スナップショット値（bookName、publisherName、price）が設定されている

* When（操作）:
  * OrderDetailDao.insert(orderDetail)を呼び出す

* Then（期待結果）:
  * EntityManager.persist()が呼び出される
  * スナップショット値が保持される

#### テストデータ
* 入力:
  ```
  orderTranId: 1
  orderDetailId: 1
  bookId: 10
  bookName: "Java入門"（スナップショット）
  publisherName: "技術評論社"（スナップショット）
  price: 3000（スナップショット）
  count: 2
  ```

---

### 2.4 JwtUtil - JWT生成

#### Feature: JWT生成

#### Scenario: 顧客情報からJWTを生成する

* Given（前提条件）:
  * customerId: 1
  * customerName: "山田太郎"
  * シークレットキーが設定されている

* When（操作）:
  * JwtUtil.generateToken(1, "山田太郎")を呼び出す

* Then（期待結果）:
  * JWT文字列が返される
  * トークンにcustomerIdとcustomerNameが含まれる
  * 有効期限が24時間後に設定されている

---

### 2.5 JwtUtil - JWT検証（正常系）

#### Feature: JWT検証

#### Scenario: 有効なJWTを検証する

* Given（前提条件）:
  * 有効なJWTトークンが生成されている
  * トークン有効期限内である

* When（操作）:
  * JwtUtil.validateToken(token)を呼び出す

* Then（期待結果）:
  * Claimsオブジェクトが返される
  * customerId: 1が抽出できる
  * customerName: "山田太郎"が抽出できる

---

### 2.6 JwtUtil - JWT検証（異常系：期限切れ）

#### Feature: JWT検証

#### Scenario: 期限切れのJWTを検証する

* Given（前提条件）:
  * 期限切れのJWTトークンが準備されている

* When（操作）:
  * JwtUtil.validateToken(expiredToken)を呼び出す

* Then（期待結果）:
  * JwtExceptionがスローされる
  * エラーメッセージ: "Token expired"

---

### 2.7 JwtUtil - JWT検証（異常系：不正な署名）

#### Feature: JWT検証

#### Scenario: 不正な署名のJWTを検証する

* Given（前提条件）:
  * 不正な署名のJWTトークンが準備されている

* When（操作）:
  * JwtUtil.validateToken(invalidToken)を呼び出す

* Then（期待結果）:
  * JwtExceptionがスローされる
  * エラーメッセージ: "Invalid signature"

---

### 2.8 JwtAuthenFilter - 認証除外パス

#### Feature: 認証フィルター

#### Scenario: 認証除外パスへのアクセス

* Given（前提条件）:
  * リクエストパス: "/api/auth/login"
  * Cookieにトークンなし

* When（操作）:
  * JwtAuthenFilter.filter(requestContext)を呼び出す

* Then（期待結果）:
  * 認証チェックがスキップされる
  * リクエストが通過する

#### テストデータ（認証除外パス）
* `/api/auth/login`
* `/api/auth/logout`
* `/api/auth/register`
* `/api/books`
* `/api/images/covers/1`

---

### 2.9 JwtAuthenFilter - 認証必須パス（正常系）

#### Feature: 認証フィルター

#### Scenario: 有効なトークンで認証必須パスにアクセス

* Given（前提条件）:
  * リクエストパス: "/api/orders"
  * Cookieに有効なJWTトークンが含まれる
  * モック設定: JwtUtil.validateToken()が成功

* When（操作）:
  * JwtAuthenFilter.filter(requestContext)を呼び出す

* Then（期待結果）:
  * JwtUtil.validateToken()が呼び出される
  * AuthenticatedUserに認証情報が設定される
  * リクエストが通過する

* And（追加の検証）:
  * AuthenticatedUser.getCustomerId()が1を返す

---

### 2.10 JwtAuthenFilter - 認証必須パス（異常系：トークンなし）

#### Feature: 認証フィルター

#### Scenario: トークンなしで認証必須パスにアクセス

* Given（前提条件）:
  * リクエストパス: "/api/orders"
  * Cookieにトークンなし

* When（操作）:
  * JwtAuthenFilter.filter(requestContext)を呼び出す

* Then（期待結果）:
  * 401 Unauthorizedレスポンスが返される
  * エラーメッセージ: "Authentication required"

---

### 2.11 AuthenticatedUser - 認証情報の保持

#### Feature: 認証情報の保持

#### Scenario: リクエストスコープで認証情報を保持する

* Given（前提条件）:
  * AuthenticatedUserインスタンスが生成されている

* When（操作）:
  * setCustomerId(1)を呼び出す
  * setCustomerName("山田太郎")を呼び出す

* Then（期待結果）:
  * getCustomerId()が1を返す
  * getCustomerName()が"山田太郎"を返す
  * isAuthenticated()がtrueを返す

---

### 2.12 BackOfficeRestClient - 書籍一覧取得

#### Feature: 外部API連携（書籍一覧）

#### Scenario: back-office-apiから書籍一覧を取得する

* Given（前提条件）:
  * BackOfficeApiインターフェースがモック化されている
  * モック設定: findAllBooks()がBookTOのリストを返す

* When（操作）:
  * BackOfficeRestClient.findAllBooks()を呼び出す

* Then（期待結果）:
  * GET /booksが呼び出される
  * BookTOのリストが返される

#### テストデータ
* 期待される出力:
  ```
  [
    { bookId: 1, bookName: "Java入門", price: 3000 },
    { bookId: 2, bookName: "Spring Boot実践", price: 3500 }
  ]
  ```

---

### 2.13 BackOfficeRestClient - 在庫更新（楽観的ロック成功）

#### Feature: 外部API連携（在庫更新）

#### Scenario: 楽観的ロックで在庫を更新する

* Given（前提条件）:
  * BackOfficeApiインターフェースがモック化されている
  * モック設定: updateStock(1, 5L, 95)が成功する

* When（操作）:
  * BackOfficeRestClient.updateStock(1, 5L, 95)を呼び出す

* Then（期待結果）:
  * PUT /stocks/1が呼び出される
  * 更新後のStockTO（version=6）が返される

#### テストデータ
* 入力:
  ```
  bookId: 1
  version: 5
  newQuantity: 95
  ```
* 期待される出力:
  ```
  { bookId: 1, quantity: 95, version: 6 }
  ```

---

### 2.14 BackOfficeRestClient - 在庫更新（楽観的ロック失敗）

#### Feature: 外部API連携（在庫更新）

#### Scenario: バージョン不一致で楽観的ロックに失敗する

* Given（前提条件）:
  * BackOfficeApiインターフェースがモック化されている
  * モック設定: updateStock(1, 3L, 95)が409 Conflictを返す

* When（操作）:
  * BackOfficeRestClient.updateStock(1, 3L, 95)を呼び出す

* Then（期待結果）:
  * WebApplicationExceptionがスローされる
  * ステータスコード: 409 Conflict

---

### 2.15 CustomerHubRestClient - メールアドレスで顧客検索

#### Feature: 外部API連携（顧客検索）

#### Scenario: メールアドレスで顧客を検索する

* Given（前提条件）:
  * CustomerHubApiインターフェースがモック化されている
  * モック設定: findByEmail("yamada@example.com")がCustomerTOを返す

* When（操作）:
  * CustomerHubRestClient.findByEmail("yamada@example.com")を呼び出す

* Then（期待結果）:
  * GET /customers/email/yamada@example.comが呼び出される
  * CustomerTOが返される

#### テストデータ
* 入力: "yamada@example.com"
* 期待される出力:
  ```
  {
    customerId: 1,
    customerName: "山田太郎",
    email: "yamada@example.com"
  }
  ```

---

### 2.16 CustomerHubRestClient - 顧客登録

#### Feature: 外部API連携（顧客登録）

#### Scenario: 新規顧客を登録する

* Given（前提条件）:
  * CustomerHubApiインターフェースがモック化されている
  * モック設定: createCustomer(customerTO)が登録済みCustomerTOを返す

* When（操作）:
  * CustomerHubRestClient.createCustomer(customerTO)を呼び出す

* Then（期待結果）:
  * POST /customersが呼び出される
  * customerId付きのCustomerTOが返される

#### テストデータ
* 入力:
  ```
  {
    customerName: "鈴木花子",
    email: "suzuki@example.com",
    password: "hashedPassword"
  }
  ```
* 期待される出力:
  ```
  {
    customerId: 2,
    customerName: "鈴木花子",
    email: "suzuki@example.com"
  }
  ```

---

### 2.17 PasswordUtil - パスワードハッシュ化

#### Feature: パスワードハッシュ化

#### Scenario: 平文パスワードをBCryptでハッシュ化する

* Given（前提条件）:
  * 平文パスワード: "password123"

* When（操作）:
  * PasswordUtil.hashPassword("password123")を呼び出す

* Then（期待結果）:
  * BCryptハッシュ文字列が返される
  * ハッシュ長: 60文字
  * ハッシュ形式: "$2a$10$..."で始まる

---

### 2.18 PasswordUtil - パスワード検証（正常系）

#### Feature: パスワード検証

#### Scenario: 正しいパスワードを検証する

* Given（前提条件）:
  * 平文パスワード: "password123"
  * ハッシュ化されたパスワード: "$2a$10$..."

* When（操作）:
  * PasswordUtil.verifyPassword("password123", hashedPassword)を呼び出す

* Then（期待結果）:
  * trueが返される

---

### 2.19 PasswordUtil - パスワード検証（異常系：不一致）

#### Feature: パスワード検証

#### Scenario: 誤ったパスワードを検証する

* Given（前提条件）:
  * 平文パスワード: "wrongpassword"
  * ハッシュ化されたパスワード: "$2a$10$..."（"password123"のハッシュ）

* When（操作）:
  * PasswordUtil.verifyPassword("wrongpassword", hashedPassword)を呼び出す

* Then（期待結果）:
  * falseが返される

---

### 2.20 OrderDetailPK - equals/hashCode

#### Feature: 複合主キーの比較

#### Scenario: 同じ値の複合主キーを比較する

* Given（前提条件）:
  * OrderDetailPK pk1 = new OrderDetailPK(1, 1)
  * OrderDetailPK pk2 = new OrderDetailPK(1, 1)

* When（操作）:
  * pk1.equals(pk2)を呼び出す

* Then（期待結果）:
  * trueが返される
  * pk1.hashCode() == pk2.hashCode()

---

### 2.21 OrderDetailPK - equals/hashCode（異常系）

#### Feature: 複合主キーの比較

#### Scenario: 異なる値の複合主キーを比較する

* Given（前提条件）:
  * OrderDetailPK pk1 = new OrderDetailPK(1, 1)
  * OrderDetailPK pk2 = new OrderDetailPK(1, 2)

* When（操作）:
  * pk1.equals(pk2)を呼び出す

* Then（期待結果）:
  * falseが返される

---

## 3. モック化の方針

### 3.1 ドメイン内の依存関係
* 同じcommonドメイン内のクラス → 実際の連携をテスト可能

### 3.2 ドメイン外の依存関係
* EntityManager → モック化
* 外部APIインターフェース（BackOfficeApi、CustomerHubApi） → モック化
* ContainerRequestContext → モック化
* Cookie → モック化

---

## 4. カバレッジ目標

* ステートメントカバレッジ: 80%以上
* ブランチカバレッジ: 70%以上

---

## 5. 受入基準

### 5.1 機能要件
* すべての正常系テストが成功する
* すべての異常系テストが成功する
* すべての境界値テストが成功する
* JPQLクエリが正しく実行される
* JWT生成・検証が正しく動作する
* 外部API連携が正しく動作する

### 5.2 品質要件
* カバレッジ目標を達成する
* テストコードにコメントが適切に記載されている
* テストケースが独立している（テスト間の依存関係がない）
* モックが適切に設定されている

---

## 6. 参考資料

* [detailed_design.md](detailed_design.md) - 詳細設計書
* [../../basic_design/common/functional_design.md](../../basic_design/common/functional_design.md) - 共通機能設計書
* [../../basic_design/common/data_model.md](../../basic_design/common/data_model.md) - データモデル仕様書
* [../../basic_design/common/architecture_design.md](../../basic_design/common/architecture_design.md) - アーキテクチャ設計書