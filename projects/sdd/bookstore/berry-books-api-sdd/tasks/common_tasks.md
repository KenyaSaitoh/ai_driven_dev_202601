# berry-books-api-sdd - 共通機能タスク

担当者: 共通機能チーム（1名）  
推奨スキル: JPA、JAX-RS、CDI、JWT、REST Client  
想定工数: 16時間  
依存タスク: [セットアップタスク](setup.md)

---

## 概要

本タスクは、複数のAPIで共有される共通コンポーネントを実装する。注文エンティティ、Dao、JWT認証基盤、外部API連携クライアント、共通DTO、例外ハンドラなどを含む。

* 重要: このプロジェクトはBackend Service Architectureを採用しているため、注文関連のエンティティとDaoのみを実装する。書籍・在庫・顧客エンティティは外部APIで管理される。

---

## タスクリスト

### 1. エンティティ（注文ドメインのみ）

#### T_COMMON_001: OrderTran エンティティの作成

* [X] T_COMMON_001: OrderTran エンティティの作成
  * 目的: 注文トランザクションのJPAエンティティを実装する
  * 対象: OrderTran.java（JPAエンティティクラス）
  * 参照SPEC: [data_model.md](../specs/baseline/basic_design/data_model.md) の「3.6 ORDER_TRAN」
  * 注意事項: 
    * CUSTOMER_IDは論理参照のみ（外部キー制約なし）
    * @GeneratedValue(strategy = GenerationType.IDENTITY)で自動採番
    * @OneToMany(mappedBy = "orderTran", cascade = CascadeType.ALL)でOrderDetailとの関連

---

#### T_COMMON_002: OrderDetail エンティティの作成

* [X] T_COMMON_002: OrderDetail エンティティの作成
  * 目的: 注文明細のJPAエンティティを実装する
  * 対象: OrderDetail.java（JPAエンティティクラス）
  * 参照SPEC: [data_model.md](../specs/baseline/basic_design/data_model.md) の「3.7 ORDER_DETAIL」
  * 注意事項:
    * @EmbeddedIdで複合主キー（OrderDetailPK）を使用
    * BOOK_IDは論理参照のみ（外部キー制約なし）
    * スナップショットパターン: BOOK_NAME、PUBLISHER_NAME、PRICEを保持
    * @ManyToOne(fetch = FetchType.LAZY)でOrderTranとの関連

---

#### T_COMMON_003: OrderDetailPK 複合主キーの作成

* [X] T_COMMON_003: OrderDetailPK 複合主キーの作成
  * 目的: 注文明細の複合主キークラスを実装する
  * 対象: OrderDetailPK.java（@Embeddableクラス）
  * 参照SPEC: [data_model.md](../specs/baseline/basic_design/data_model.md) の「3.7.5 複合主キー」
  * 注意事項:
    * @Embeddableアノテーションを付与
    * Serializable実装
    * equals()、hashCode()をオーバーライド
    * フィールド: orderTranId、orderDetailId

---

### 2. データアクセス層（注文ドメインのみ）

#### T_COMMON_004: OrderTranDao の作成

* [X] T_COMMON_004: OrderTranDao の作成
  * 目的: 注文トランザクションのCRUD操作を実装する
  * 対象: OrderTranDao.java（DAOクラス）
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「5. ドメインモデル機能設計」
  * 注意事項:
    * @ApplicationScopedを付与
    * EntityManagerをインジェクション
    * 実装メソッド: insert()、findById()、findByCustomerId()

---

#### T_COMMON_005: OrderDetailDao の作成

* [X] T_COMMON_005: OrderDetailDao の作成
  * 目的: 注文明細のCRUD操作を実装する
  * 対象: OrderDetailDao.java（DAOクラス）
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「5. ドメインモデル機能設計」
  * 注意事項:
    * @ApplicationScopedを付与
    * EntityManagerをインジェクション
    * 実装メソッド: insert()、findByOrderTranId()、findById()

---

### 3. セキュリティ層

#### T_COMMON_006: JwtUtil の作成

* [ ] T_COMMON_006: JwtUtil の作成
  * 目的: JWT生成・検証ユーティリティを実装する
  * 対象: JwtUtil.java（CDI Bean）
  * 参照SPEC: [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) の「5.1 JWT認証設定」
  * 注意事項:
    * @ApplicationScopedを付与
    * MicroProfile Configで設定値を読み込み
    * 実装メソッド: generateToken()、validateToken()、getUserIdFromToken()、extractJwtFromRequest()
    * jjwt 0.12.6を使用（HMAC-SHA256）

---

#### T_COMMON_007: AuthenContext の作成

* [X] T_COMMON_007: AuthenContext の作成
  * 目的: 認証コンテキストを管理するCDI Beanを実装する
  * 対象: AuthenContext.java（@RequestScoped CDI Bean）
  * 参照SPEC: [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) の「5.3 認証コンテキスト管理」
  * 注意事項:
    * @RequestScopedを付与
    * Serializable実装
    * フィールド: customerId、email
    * 実装メソッド: isAuthenticated()

---

#### T_COMMON_008: JwtAuthenFilter の作成

* [ ] T_COMMON_008: JwtAuthenFilter の作成
  * 目的: JWT認証フィルターを実装する
  * 対象: JwtAuthenFilter.java（ContainerRequestFilter）
  * 参照SPEC: [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) の「5.2 認証フィルター実装」
  * 注意事項:
    * @Providerアノテーションを付与
    * @Priority(Priorities.AUTHENTICATION)で優先度設定
    * 公開エンドポイント: /auth/login、/auth/logout、/auth/register、/books、/images
    * 認証必須エンドポイント: /orders、/auth/me
    * MediaType.APPLICATION_JSONを明示

---

### 4. 外部API連携層

#### T_COMMON_009: BackOfficeRestClient の作成

* [X] T_COMMON_009: BackOfficeRestClient の作成
  * 目的: back-office-apiとのREST API連携を実装する
  * 対象: BackOfficeRestClient.java（CDI Bean）
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「6. back-office-api連携」
  * 注意事項:
    * @ApplicationScopedを付与
    * ConfigProvider.getConfig()で設定値を読み込み
    * 実装メソッド: getAllBooks()、getBookById()、searchBooksJpql()、searchBooksCriteria()、getAllCategories()、findStockById()、updateStock()
    * JAX-RS Clientを使用

---

#### T_COMMON_010: CustomerHubRestClient の作成

* [ ] T_COMMON_010: CustomerHubRestClient の作成
  * 目的: customer-hub-apiとのREST API連携を実装する
  * 対象: CustomerHubRestClient.java（CDI Bean）
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「3. customer-hub-api連携」
  * 注意事項:
    * @ApplicationScopedを付与
    * ConfigProvider.getConfig()で設定値を読み込み
    * 実装メソッド: findByEmail()、findById()、register()
    * JAX-RS Clientを使用

---

### 5. 外部API用DTO

#### T_COMMON_011: 外部API用DTOの作成

* [X] T_COMMON_011: 外部API用DTOの作成
  * 目的: 外部APIのリクエスト/レスポンスDTOを実装する
  * 対象: external.dto パッケージ（Java Records）
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「5. CustomerTO」「9. BookTO」
  * 注意事項:
    * CustomerTO: customerId、customerName、password、email、birthday、address
    * BookTO: bookId、bookName、author、category、publisher、price、quantity、version
    * CategoryTO: categoryId、categoryName
    * StockTO: bookId、bookName、quantity、version
    * Java Recordsを使用

---

### 6. 共通DTO

#### T_COMMON_012: ErrorResponse の作成

* [ ] T_COMMON_012: ErrorResponse の作成
  * 目的: 統一的なエラーレスポンス形式を実装する
  * 対象: ErrorResponse.java（Java Record）
  * 参照SPEC: [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) の「4.5.3 統一的なエラーレスポンス形式」
  * 注意事項:
    * フィールド: status、error、message、path
    * Java Recordsを使用

---

### 7. ビジネスロジック層

#### T_COMMON_013: DeliveryFeeService の作成

* [X] T_COMMON_013: DeliveryFeeService の作成
  * 目的: 配送料金計算ロジックを実装する
  * 対象: DeliveryFeeService.java（CDI Bean）
  * 参照SPEC: [behaviors.md](../specs/baseline/basic_design/behaviors.md) の「7. 配送料金計算」
  * 注意事項:
    * @ApplicationScopedを付与
    * 実装メソッド: calculateDeliveryFee(totalPrice, deliveryAddress)
    * 計算ロジック: 購入金額10,000円未満は800円、10,000円以上は無料
    * 沖縄県の場合は追加料金（1,500円）

---

#### T_COMMON_014: OrderService の作成

* [ ] T_COMMON_014: OrderService の作成
  * 目的: 注文処理のビジネスロジックを実装する
  * 対象: OrderService.java（CDI Bean）
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「7. システム全体のデータフロー」
  * 注意事項:
    * @ApplicationScopedを付与
    * @Transactionalでトランザクション境界を定義
    * 実装メソッド: orderBooks(OrderRequest)
    * 処理フロー:
      1. カート項目ごとに在庫確認（BackOfficeRestClient）
      2. 在庫更新（BackOfficeRestClient、楽観的ロック）
      3. 配送料金計算（DeliveryFeeService）
      4. 注文トランザクション作成（OrderTranDao）
      5. 注文明細作成（OrderDetailDao）
    * 例外処理: OutOfStockException、OptimisticLockException

---

### 8. 例外ハンドラ

#### T_COMMON_015: GenericExceptionMapper の作成

* [X] T_COMMON_015: GenericExceptionMapper の作成
  * 目的: 一般的な例外を処理する
  * 対象: GenericExceptionMapper.java（ExceptionMapper）
  * 参照SPEC: [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) の「4.5.2 Exception Mapper」
  * 注意事項:
    * @Providerアノテーションを付与
    * 500 Internal Server Errorを返却
    * MediaType.APPLICATION_JSONを明示
    * エラー詳細をログ出力（ERROR）

---

#### T_COMMON_016: ValidationExceptionMapper の作成

* [ ] T_COMMON_016: ValidationExceptionMapper の作成
  * 目的: Bean Validationエラーを処理する
  * 対象: ValidationExceptionMapper.java（ExceptionMapper）
  * 参照SPEC: [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) の「4.5.2 Exception Mapper」
  * 注意事項:
    * @Providerアノテーションを付与
    * ConstraintViolationExceptionを処理
    * 400 Bad Requestを返却
    * MediaType.APPLICATION_JSONを明示

---

#### T_COMMON_017: OptimisticLockExceptionMapper の作成

* [X] T_COMMON_017: OptimisticLockExceptionMapper の作成
  * 目的: 楽観的ロック競合エラーを処理する
  * 対象: OptimisticLockExceptionMapper.java（ExceptionMapper）
  * 参照SPEC: [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) の「4.5.2 Exception Mapper」
  * 注意事項:
    * @Providerアノテーションを付与
    * OptimisticLockExceptionを処理
    * 409 Conflictを返却
    * MediaType.APPLICATION_JSONを明示
    * エラーメッセージ: "他のユーザーが購入済みです。最新の在庫情報を確認してください。"

---

### 9. 設定ファイル

#### T_COMMON_018: persistence.xml の作成

* [ ] T_COMMON_018: persistence.xml の作成
  * 目的: JPA永続化設定を定義する
  * 対象: persistence.xml（設定ファイル）
  * 参照SPEC: [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) の「7.1 永続化構成」
  * 注意事項:
    * persistence-unit名: "BerryBooksPU"
    * jta-data-source: "jdbc/HsqldbDS"
    * エンティティクラス: OrderTran、OrderDetail
    * eclipselink.logging.level: "FINE"（開発環境）

---

#### T_COMMON_019: microprofile-config.properties の作成

* [X] T_COMMON_019: microprofile-config.properties の作成
  * 目的: MicroProfile Config設定を定義する
  * 対象: microprofile-config.properties（設定ファイル）
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「9. 設定管理」
  * 注意事項:
    * jwt.secret-key: 32文字以上の秘密鍵
    * jwt.expiration-ms: 86400000（24時間）
    * jwt.cookie-name: "berry-books-jwt"
    * customer-hub-api.base-url: "http://localhost:8080/customer-hub-api/api/customers"
    * back-office-api.base-url: "http://localhost:8080/back-office-api-sdd/api"

---

#### T_COMMON_020: beans.xml の作成

* [ ] T_COMMON_020: beans.xml の作成
  * 目的: CDIコンテナを有効化する
  * 対象: beans.xml（設定ファイル）
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「10. CDI設定」
  * 注意事項:
    * bean-discovery-mode="all"
    * Jakarta EE 10標準に準拠

---

### 10. ユニットテスト

#### T_COMMON_021: DeliveryFeeService のテスト

* [X] [P] T_COMMON_021: DeliveryFeeService のテスト
  * 目的: 配送料金計算ロジックのテストを作成する
  * 対象: DeliveryFeeServiceTest.java（JUnit 5）
  * 参照SPEC: [behaviors.md](../specs/baseline/basic_design/behaviors.md) の「7. 配送料金計算」
  * 注意事項:
    * 購入金額10,000円未満: 800円
    * 購入金額10,000円以上: 0円
    * 沖縄県の場合: 1,500円

---

#### T_COMMON_022: OrderService のテスト

* [X] [P] T_COMMON_022: OrderService のテスト
  * 目的: 注文処理のビジネスロジックをテストする
  * 対象: OrderServiceTest.java（JUnit 5 + Mockito）
  * 参照SPEC: [behaviors.md](../specs/baseline/basic_design/behaviors.md) の「4. 注文API」
  * 注意事項:
    * Mockitoで外部APIクライアント、Daoをモック
    * 正常系: 注文作成成功
    * 異常系: 在庫不足、楽観的ロック競合

---

## 成果物チェックリスト

* [X] 注文エンティティ（OrderTran、OrderDetail、OrderDetailPK）が実装されている
* [X] 注文Dao（OrderTranDao、OrderDetailDao）が実装されている
* [X] JWT認証基盤（JwtUtil、JwtAuthenFilter、AuthenContext）が実装されている
* [X] 外部API連携クライアント（BackOfficeRestClient、CustomerHubRestClient）が実装されている
* [X] 外部API用DTO（CustomerTO、BookTO等）が実装されている
* [X] 共通DTO（ErrorResponse）が実装されている
* [X] ビジネスロジック層（OrderService、DeliveryFeeService）が実装されている
* [X] 例外ハンドラ（GenericExceptionMapper、ValidationExceptionMapper、OptimisticLockExceptionMapper）が実装されている
* [X] 設定ファイル（persistence.xml、microprofile-config.properties、beans.xml）が作成されている
* [X] ユニットテストが作成されている
* [X] 全てのコンポーネントがコンパイルエラーなく動作する

---

## 参考資料

* [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
* [security.md](../../../agent_skills/jakarta-ee-api-base/principles/security.md) - セキュリティ標準
* [common_rules.md](../../../agent_skills/jakarta-ee-api-base/principles/common_rules.md) - 共通ルール
* [data_model.md](../specs/baseline/basic_design/data_model.md) - データモデル仕様書
* [functional_design.md](../specs/baseline/basic_design/functional_design.md) - 機能設計書
* [external_interface.md](../specs/baseline/basic_design/external_interface.md) - 外部インターフェース仕様書
