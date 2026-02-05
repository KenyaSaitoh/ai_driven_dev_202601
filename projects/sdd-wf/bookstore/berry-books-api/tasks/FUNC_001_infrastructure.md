# FUNC_001_infrastructure - 基盤コンポーネント

## メタデータ

* タスクID: FUNC_001
* 機能タイプ: 基盤
* 依存タスク: setup
* 並行実行可能: なし
* 担当者: チームA（3名推奨）
* 推奨スキル: Jakarta EE, JPA, JAX-RS, CDI, JWT, REST Client
* 想定工数: 16時間

## 実装内容

プロジェクト全体で使用される基盤コンポーネントを実装する。
このタスクには、Entity、DAO、外部API連携クライアント、JWT認証基盤、共通フィルター、例外マッパーが含まれる。

---

## タスクリスト

### 1. Entityクラスの作成

* [ ] T_FUNC001_001: OrderTranエンティティの作成
  * 目的: 注文トランザクション情報を管理するエンティティを作成する
  * 対象: pro.kensait.berrybooks.entity.OrderTran
  * 参照SPEC: [data_model.md](../specs/baseline/basic_design/data_model.md) の「3.6 ORDER_TRAN」
  * 注意事項:
    * @Entity, @Table, @Id, @GeneratedValue
    * OneToMany関係: OrderDetail
    * バリデーション: @NotNull, @Min

* [ ] T_FUNC001_002: OrderDetailエンティティの作成
  * 目的: 注文明細情報を管理するエンティティを作成する
  * 対象: pro.kensait.berrybooks.entity.OrderDetail
  * 参照SPEC: [data_model.md](../specs/baseline/basic_design/data_model.md) の「3.7 ORDER_DETAIL」
  * 注意事項:
    * 複合主キー: @EmbeddedId（OrderDetailPK）
    * ManyToOne関係: OrderTran
    * スナップショットフィールド: bookName, publisherName, price

* [ ] T_FUNC001_003: OrderDetailPKクラスの作成
  * 目的: 注文明細の複合主キーを定義する
  * 対象: pro.kensait.berrybooks.entity.OrderDetailPK
  * 参照SPEC: [data_model.md](../specs/baseline/basic_design/data_model.md) の「3.7.5 複合主キー」
  * 注意事項:
    * @Embeddable
    * equals()、hashCode()の実装
    * orderTranId、orderDetailId

### 2. DAOクラスの作成

* [ ] T_FUNC001_004: OrderTranDaoの作成
  * 目的: 注文トランザクションのデータアクセスを提供する
  * 対象: pro.kensait.berrybooks.dao.OrderTranDao
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「5. ドメインモデル機能設計」
  * 注意事項:
    * @ApplicationScoped
    * EntityManager注入
    * insert(), findById(), findByCustomerId(), findAll()

* [ ] T_FUNC001_005: OrderDetailDaoの作成
  * 目的: 注文明細のデータアクセスを提供する
  * 対象: pro.kensait.berrybooks.dao.OrderDetailDao
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「5. ドメインモデル機能設計」
  * 注意事項:
    * @ApplicationScoped
    * EntityManager注入
    * insert(), findByOrderTranId()

### 3. 外部API連携クライアント

* [ ] T_FUNC001_006: CustomerHubRestClientの作成
  * 目的: customer-hub-apiとの連携を提供する
  * 対象: pro.kensait.berrybooks.external.CustomerHubRestClient
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「3. customer-hub-api連携」、「4. customer-hub-api エンドポイント」
  * 注意事項:
    * @ApplicationScoped
    * JAX-RS Client API使用
    * findByEmail(), findById(), register()

* [ ] T_FUNC001_007: BackOfficeRestClientの作成
  * 目的: back-office-apiとの連携を提供する
  * 対象: pro.kensait.berrybooks.external.BackOfficeRestClient
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「6. back-office-api連携」、「7. back-office-api エンドポイント概要」
  * 注意事項:
    * @ApplicationScoped
    * JAX-RS Client API使用
    * getAllBooks(), getBookById(), searchBooksJpql(), getAllCategories(), getStock(), updateStock()

* [ ] T_FUNC001_008: CustomerTOの作成
  * 目的: customer-hub-apiのレスポンスをマッピングするDTOを作成する
  * 対象: pro.kensait.berrybooks.external.dto.CustomerTO
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「5. CustomerTO スキーマ」
  * 注意事項:
    * Javaレコード型（immutable）
    * customerId, customerName, password, email, birthday, address

* [ ] T_FUNC001_009: BookTOの作成
  * 目的: back-office-apiのレスポンスをマッピングするDTOを作成する
  * 対象: pro.kensait.berrybooks.external.dto.BookTO
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「7.1 書籍一覧取得」
  * 注意事項:
    * Javaレコード型（immutable）
    * bookId, bookName, author, categoryId, publisherId, price, quantity, version

* [ ] T_FUNC001_010: StockTOの作成
  * 目的: back-office-apiの在庫レスポンスをマッピングするDTOを作成する
  * 対象: pro.kensait.berrybooks.external.dto.StockTO
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「7.6 在庫取得」
  * 注意事項:
    * Javaレコード型（immutable）
    * bookId, bookName, quantity, version

### 4. JWT認証基盤

* [ ] T_FUNC001_011: JwtUtilの作成
  * 目的: JWT生成・検証機能を提供する
  * 対象: pro.kensait.berrybooks.security.JwtUtil
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「4.1 認証・認可」
  * 注意事項:
    * @ApplicationScoped
    * generateToken(), validateToken(), extractCustomerId()
    * jjwtライブラリ使用
    * シークレットキー、有効期限（24時間）

* [ ] T_FUNC001_012: JwtAuthenFilterの作成
  * 目的: JWT認証フィルターを実装する
  * 対象: pro.kensait.berrybooks.security.JwtAuthenFilter
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「7. 認証除外エンドポイント」
  * 注意事項:
    * ContainerRequestFilter実装
    * @Provider
    * Cookie（AUTH-TOKEN）からJWT取得
    * 認証除外パス: /api/auth/login, /api/auth/logout, /api/auth/register, /api/books, /api/images

* [ ] T_FUNC001_013: AuthenContextの作成
  * 目的: 認証情報をスレッドローカルで管理する
  * 対象: pro.kensait.berrybooks.security.AuthenContext
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「2.2 コンポーネントの責務」
  * 注意事項:
    * @RequestScoped
    * customerId, email

### 5. 共通DTOクラス

* [ ] T_FUNC001_014: ErrorResponseの作成
  * 目的: 統一的なエラーレスポンス形式を定義する
  * 対象: pro.kensait.berrybooks.api.dto.ErrorResponse
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8. エラーハンドリング」
  * 注意事項:
    * Javaレコード型
    * status, error, message, path

### 6. 例外クラス

* [ ] T_FUNC001_015: AuthenticationExceptionの作成
  * 目的: 認証エラーを表現する例外を作成する
  * 対象: pro.kensait.berrybooks.common.exception.AuthenticationException
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8. エラーハンドリング」
  * 注意事項: RuntimeException継承

* [ ] T_FUNC001_016: OutOfStockExceptionの作成
  * 目的: 在庫不足エラーを表現する例外を作成する
  * 対象: pro.kensait.berrybooks.common.exception.OutOfStockException
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「5.1.1 ビジネスルール」
  * 注意事項: RuntimeException継承

* [ ] T_FUNC001_017: OptimisticLockExceptionの作成
  * 目的: 楽観的ロック失敗を表現する例外を作成する
  * 対象: pro.kensait.berrybooks.common.exception.OptimisticLockException
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「6. 並行制御（楽観的ロック）」
  * 注意事項: RuntimeException継承

* [ ] T_FUNC001_018: CustomerExistsExceptionの作成
  * 目的: 顧客重複エラーを表現する例外を作成する
  * 対象: pro.kensait.berrybooks.common.exception.CustomerExistsException
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8. エラーハンドリング」
  * 注意事項: RuntimeException継承

* [ ] T_FUNC001_019: ResourceNotFoundExceptionの作成
  * 目的: リソース未検出エラーを表現する例外を作成する
  * 対象: pro.kensait.berrybooks.common.exception.ResourceNotFoundException
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8. エラーハンドリング」
  * 注意事項: RuntimeException継承

### 7. Exception Mapper

* [ ] T_FUNC001_020: AuthenticationExceptionMapperの作成
  * 目的: 認証エラーを401 Unauthorizedにマッピングする
  * 対象: pro.kensait.berrybooks.api.exception.AuthenticationExceptionMapper
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8.1 エラーレスポンス仕様」
  * 注意事項:
    * ExceptionMapper実装
    * @Provider
    * 401 Unauthorized + ErrorResponse

* [ ] T_FUNC001_021: OutOfStockExceptionMapperの作成
  * 目的: 在庫不足エラーを409 Conflictにマッピングする
  * 対象: pro.kensait.berrybooks.api.exception.OutOfStockExceptionMapper
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8.1 エラーレスポンス仕様」
  * 注意事項:
    * ExceptionMapper実装
    * @Provider
    * 409 Conflict + ErrorResponse

* [ ] T_FUNC001_022: OptimisticLockExceptionMapperの作成
  * 目的: 楽観的ロック失敗を409 Conflictにマッピングする
  * 対象: pro.kensait.berrybooks.api.exception.OptimisticLockExceptionMapper
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8.1 エラーレスポンス仕様」
  * 注意事項:
    * ExceptionMapper実装
    * @Provider
    * 409 Conflict + ErrorResponse

* [ ] T_FUNC001_023: CustomerExistsExceptionMapperの作成
  * 目的: 顧客重複エラーを409 Conflictにマッピングする
  * 対象: pro.kensait.berrybooks.api.exception.CustomerExistsExceptionMapper
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8.1 エラーレスポンス仕様」
  * 注意事項:
    * ExceptionMapper実装
    * @Provider
    * 409 Conflict + ErrorResponse

* [ ] T_FUNC001_024: ResourceNotFoundExceptionMapperの作成
  * 目的: リソース未検出エラーを404 Not Foundにマッピングする
  * 対象: pro.kensait.berrybooks.api.exception.ResourceNotFoundExceptionMapper
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8.1 エラーレスポンス仕様」
  * 注意事項:
    * ExceptionMapper実装
    * @Provider
    * 404 Not Found + ErrorResponse

* [ ] T_FUNC001_025: GenericExceptionMapperの作成
  * 目的: 未処理の例外を500 Internal Server Errorにマッピングする
  * 対象: pro.kensait.berrybooks.api.exception.GenericExceptionMapper
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8.1 エラーレスポンス仕様」
  * 注意事項:
    * ExceptionMapper<Exception>実装
    * @Provider
    * 500 Internal Server Error + ErrorResponse

### 8. ユーティリティクラス

* [ ] T_FUNC001_026: PasswordUtilの作成
  * 目的: BCryptによるパスワードハッシュ化・検証機能を提供する
  * 対象: pro.kensait.berrybooks.util.PasswordUtil
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「4.3 顧客登録」
  * 注意事項:
    * hashPassword()
    * verifyPassword()
    * BCrypt.checkpw()使用

---

## 完了条件

* [ ] 全てのEntityクラスがJPA仕様に準拠している
* [ ] 全てのDAOクラスがEntityManagerを使用してCRUD操作を実装している
* [ ] 外部API連携クライアントが正常に動作する
* [ ] JWT生成・検証機能が正常に動作する
* [ ] 認証フィルターが認証除外パスを正しく判定する
* [ ] 全てのException Mapperが適切なHTTPステータスコードを返す
* [ ] 単体テストが全て成功する

---

## 参考資料

* [../specs/baseline/basic_design/architecture_design.md](../specs/baseline/basic_design/architecture_design.md) - アーキテクチャ設計書
* [../specs/baseline/basic_design/functional_design.md](../specs/baseline/basic_design/functional_design.md) - 機能設計書
* [../specs/baseline/basic_design/data_model.md](../specs/baseline/basic_design/data_model.md) - データモデル仕様書
* [../specs/baseline/basic_design/external_interface.md](../specs/baseline/basic_design/external_interface.md) - 外部インターフェース仕様書
* [../specs/baseline/basic_design/behaviors.md](../specs/baseline/basic_design/behaviors.md) - 振る舞い仕様書
