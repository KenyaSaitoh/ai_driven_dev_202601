@unit @common
Feature: 共通ドメイン（単体テスト）
  Entity、DAO、Security、External、Utilのテスト

  Scenario: 新規注文を登録する
    Given EntityManagerがモック化されている
    And OrderTranオブジェクトが準備されている
    When OrderTranDao.insert(orderTran)を呼び出す
    Then EntityManager.persist()が呼び出される
    And 引数として渡されたOrderTranが返される

  Scenario: 顧客の注文履歴を新しい順で取得する
    Given EntityManagerがモック化されている
    And 顧客ID=1の注文が複数存在する
    When OrderTranDao.findByCustomerId(1)を呼び出す
    Then JPQLクエリが実行される
    And 注文リストが注文日の降順で返される
    And リストの最初の要素が最新の注文である

  Scenario: スナップショットパターンで注文明細を登録する
    Given EntityManagerがモック化されている
    And OrderDetailオブジェクトが準備されている
    And スナップショット値が設定されている
    When OrderDetailDao.insert(orderDetail)を呼び出す
    Then EntityManager.persist()が呼び出される
    And スナップショット値が保持される

  Scenario: 顧客情報からJWTを生成する
    Given customerId: 1
    And customerName: "山田太郎"
    And シークレットキーが設定されている
    When JwtUtil.generateToken(1, "山田太郎")を呼び出す
    Then JWT文字列が返される
    And トークンにcustomerIdとcustomerNameが含まれる
    And 有効期限が24時間後に設定されている

  Scenario: 有効なJWTを検証する
    Given 有効なJWTトークンが生成されている
    And トークン有効期限内である
    When JwtUtil.validateToken(token)を呼び出す
    Then Claimsオブジェクトが返される
    And customerId: 1が抽出できる
    And customerName: "山田太郎"が抽出できる

  Scenario: 期限切れのJWTを検証する
    Given 期限切れのJWTトークンが準備されている
    When JwtUtil.validateToken(expiredToken)を呼び出す
    Then JwtExceptionがスローされる
    And エラーメッセージ: "Token expired"

  Scenario: 不正な署名のJWTを検証する
    Given 不正な署名のJWTトークンが準備されている
    When JwtUtil.validateToken(invalidToken)を呼び出す
    Then JwtExceptionがスローされる
    And エラーメッセージ: "Invalid signature"

  Scenario: 認証除外パスへのアクセス
    Given リクエストパス: "/api/auth/login"
    And Cookieにトークンなし
    When JwtAuthenFilter.filter(requestContext)を呼び出す
    Then 認証チェックがスキップされる
    And リクエストが通過する

  Scenario: 有効なトークンで認証必須パスにアクセス
    Given リクエストパス: "/api/orders"
    And Cookieに有効なJWTトークンが含まれる
    And JwtUtil.validateToken()が成功
    When JwtAuthenFilter.filter(requestContext)を呼び出す
    Then JwtUtil.validateToken()が呼び出される
    And AuthenticatedUserに認証情報が設定される
    And リクエストが通過する

  Scenario: トークンなしで認証必須パスにアクセス
    Given リクエストパス: "/api/orders"
    And Cookieにトークンなし
    When JwtAuthenFilter.filter(requestContext)を呼び出す
    Then 401 Unauthorizedレスポンスが返される
    And エラーメッセージ: "Authentication required"

  Scenario: リクエストスコープで認証情報を保持する
    Given AuthenticatedUserインスタンスが生成されている
    When setCustomerId(1)を呼び出す
    And setCustomerName("山田太郎")を呼び出す
    Then getCustomerId()が1を返す
    And getCustomerName()が"山田太郎"を返す
    And isAuthenticated()がtrueを返す

  Scenario: back-office-apiから書籍一覧を取得する
    Given BackOfficeApiインターフェースがモック化されている
    And findAllBooks()がBookTOのリストを返す
    When BackOfficeRestClient.findAllBooks()を呼び出す
    Then GET /booksが呼び出される
    And BookTOのリストが返される

  Scenario: 楽観的ロックで在庫を更新する
    Given BackOfficeApiインターフェースがモック化されている
    And updateStock(1, 5L, 95)が成功する
    When BackOfficeRestClient.updateStock(1, 5L, 95)を呼び出す
    Then PUT /stocks/1が呼び出される
    And 更新後のStockTO（version=6）が返される

  Scenario: バージョン不一致で楽観的ロックに失敗する
    Given BackOfficeApiインターフェースがモック化されている
    And updateStock(1, 3L, 95)が409 Conflictを返す
    When BackOfficeRestClient.updateStock(1, 3L, 95)を呼び出す
    Then WebApplicationExceptionがスローされる
    And ステータスコード: 409 Conflict

  Scenario: メールアドレスで顧客を検索する
    Given CustomerHubApiインターフェースがモック化されている
    And findByEmail("yamada@example.com")がCustomerTOを返す
    When CustomerHubRestClient.findByEmail("yamada@example.com")を呼び出す
    Then GET /customers/email/yamada@example.comが呼び出される
    And CustomerTOが返される

  Scenario: 新規顧客を登録する
    Given CustomerHubApiインターフェースがモック化されている
    And createCustomer(customerTO)が登録済みCustomerTOを返す
    When CustomerHubRestClient.createCustomer(customerTO)を呼び出す
    Then POST /customersが呼び出される
    And customerId付きのCustomerTOが返される

  Scenario: 平文パスワードをBCryptでハッシュ化する
    Given 平文パスワード: "password123"
    When PasswordUtil.hashPassword("password123")を呼び出す
    Then BCryptハッシュ文字列が返される
    And ハッシュ長: 60文字
    And ハッシュ形式: "$2a$10$..."で始まる

  Scenario: 正しいパスワードを検証する
    Given 平文パスワード: "password123"
    And ハッシュ化されたパスワード: "$2a$10$..."
    When PasswordUtil.verifyPassword("password123", hashedPassword)を呼び出す
    Then trueが返される

  Scenario: 誤ったパスワードを検証する
    Given 平文パスワード: "wrongpassword"
    And ハッシュ化されたパスワード: "$2a$10$..."
    When PasswordUtil.verifyPassword("wrongpassword", hashedPassword)を呼び出す
    Then falseが返される

  Scenario: 同じ値の複合主キーを比較する
    Given OrderDetailPK pk1 = new OrderDetailPK(1, 1)
    And OrderDetailPK pk2 = new OrderDetailPK(1, 1)
    When pk1.equals(pk2)を呼び出す
    Then trueが返される
    And pk1.hashCode() == pk2.hashCode()

  Scenario: 異なる値の複合主キーを比較する
    Given OrderDetailPK pk1 = new OrderDetailPK(1, 1)
    And OrderDetailPK pk2 = new OrderDetailPK(1, 2)
    When pk1.equals(pk2)を呼び出す
    Then falseが返される
