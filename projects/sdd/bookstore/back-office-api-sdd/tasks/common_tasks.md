# 共通機能タスク

担当者: 共通機能チーム（2-3名推奨）  
推奨スキル: Jakarta EE、JPA、JAX-RS、JWT、セキュリティ  
想定工数: 16時間  
依存タスク: [setup_tasks.md](setup_tasks.md)

---

## 概要

このタスクリストは、全API機能で共有される共通コンポーネントの実装タスクを含みます。エンティティ、DAO、DTO、ユーティリティ、セキュリティ基盤、例外ハンドラーを実装します。

重要: これらのコンポーネントは、各API実装の前提となるため、優先的に実装してください。

---

## タスクリスト

## 1. エンティティ（JPA Entities）

### T_COMMON_001: Categoryエンティティの作成

* 目的: カテゴリ情報を表すJPAエンティティを実装する
* 対象: `pro.kensait.backoffice.entity.Category`
* 参照SPEC: 
  * [data_model.md](../specs/baseline/system/data_model.md) の「3.3 CATEGORY（カテゴリマスタ）」
* 注意事項: 
  * `@Entity`、`@Table(name = "CATEGORY")`
  * 主キー: `categoryId`（INTEGER）
  * `@OneToMany` でBookエンティティとの関連を定義（双方向関係）

---

### T_COMMON_002: Publisherエンティティの作成

* 目的: 出版社情報を表すJPAエンティティを実装する
* 対象: `pro.kensait.backoffice.entity.Publisher`
* 参照SPEC: 
  * [data_model.md](../specs/baseline/system/data_model.md) の「3.4 PUBLISHER（出版社マスタ）」
* 注意事項: 
  * `@Entity`、`@Table(name = "PUBLISHER")`
  * 主キー: `publisherId`（INTEGER）
  * `@OneToMany` でBookエンティティとの関連を定義（双方向関係）

---

### T_COMMON_003: Bookエンティティの作成

* 目的: 書籍情報を表すJPAエンティティを実装する（BOOK + STOCK結合）
* 対象: `pro.kensait.backoffice.entity.Book`
* 参照SPEC: 
  * [data_model.md](../specs/baseline/system/data_model.md) の「3.1 BOOK（書籍マスタ）」
  * [data_model.md](../specs/baseline/system/data_model.md) の「3.2 STOCK（在庫マスタ）」
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.4 Persistence Layer」
* 注意事項: 
  * `@Entity`、`@Table(name = "BOOK")`
  * `@SecondaryTable(name = "STOCK", pkJoinColumns = @PrimaryKeyJoinColumn(name = "BOOK_ID"))`
  * 主キー: `bookId`（INTEGER、自動採番）
  * `@ManyToOne` でCategory、Publisherとの関連を定義
  * 在庫情報（quantity, version）は`@Column(table = "STOCK")`で定義
  * `version`フィールドに`@Version`を付与（楽観的ロック）
  * 論理削除フラグ: `deleted`（Boolean）

---

### T_COMMON_004: Stockエンティティの作成

* 目的: 在庫情報を表すJPAエンティティを実装する（楽観的ロック対応）
* 対象: `pro.kensait.backoffice.entity.Stock`
* 参照SPEC: 
  * [data_model.md](../specs/baseline/system/data_model.md) の「3.2 STOCK（在庫マスタ）」
* 注意事項: 
  * `@Entity`、`@Table(name = "STOCK")`
  * 主キー: `bookId`（INTEGER、外部キー）
  * `@Version`アノテーションで楽観的ロック実装
  * `version`フィールド: Long型

---

### T_COMMON_005: Departmentエンティティの作成

* 目的: 部署情報を表すJPAエンティティを実装する
* 対象: `pro.kensait.backoffice.entity.Department`
* 参照SPEC: 
  * [data_model.md](../specs/baseline/system/data_model.md) の「3.6 DEPARTMENT（部署マスタ）」
* 注意事項: 
  * `@Entity`、`@Table(name = "DEPARTMENT")`
  * 主キー: `departmentId`（BIGINT）
  * `@OneToMany` でEmployeeエンティティとの関連を定義（双方向関係）

---

### T_COMMON_006: Employeeエンティティの作成

* 目的: 社員情報を表すJPAエンティティを実装する
* 対象: `pro.kensait.backoffice.entity.Employee`
* 参照SPEC: 
  * [data_model.md](../specs/baseline/system/data_model.md) の「3.5 EMPLOYEE（社員マスタ）」
* 注意事項: 
  * `@Entity`、`@Table(name = "EMPLOYEE")`
  * 主キー: `employeeId`（BIGINT）
  * ユニークキー: `employeeCode`（`@Column(unique = true)`）
  * `@ManyToOne` でDepartmentとの関連を定義
  * `jobRank`フィールド: Integer（1: ASSOCIATE, 2: MANAGER, 3: DIRECTOR）
  * `password`フィールド: BCryptハッシュ化されたパスワード

---

### T_COMMON_007: Workflowエンティティの作成

* 目的: ワークフロー履歴を表すJPAエンティティを実装する
* 対象: `pro.kensait.backoffice.entity.Workflow`
* 参照SPEC: 
  * [data_model.md](../specs/baseline/system/data_model.md) の「3.7 WORKFLOW（ワークフロー履歴）」
* 注意事項: 
  * `@Entity`、`@Table(name = "WORKFLOW")`
  * 主キー: `operationId`（BIGINT、自動採番）
  * `workflowId`フィールド: 複数の操作履歴で共有するID
  * `@ManyToOne` でBook、Category、Publisher、Employeeとの関連を定義（`insertable=false, updatable=false`）
  * ワークフロータイプ: String（ADD_NEW_BOOK, REMOVE_BOOK, ADJUST_BOOK_PRICE）
  * 状態: String（CREATED, APPLIED, APPROVED）
  * 操作タイプ: String（CREATE, APPLY, APPROVE, REJECT）

---

## 2. DAO（Data Access Objects）

### T_COMMON_008: [P] BookDaoの作成（JPQL）

* 目的: 書籍データアクセス用のDAOを実装する（JPQL使用）
* 対象: `pro.kensait.backoffice.dao.BookDao`
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.3 Data Access Layer」
  * [functional_design.md](../specs/baseline/system/functional_design.md) の「3.3 F-BOOK-001」
* 注意事項: 
  * `@ApplicationScoped`、`EntityManager`を`@Inject`
  * `findAll()`: 全書籍取得（論理削除されていない書籍のみ）
  * `findById(Integer bookId)`: ID指定取得
  * `findByCategory(Integer categoryId)`: カテゴリ別検索
  * `findByKeyword(String keyword)`: キーワード検索（書籍名、著者名）
  * `findByCategoryAndKeyword(Integer categoryId, String keyword)`: 複合検索
  * JPQLで動的クエリを実装

---

### T_COMMON_009: [P] BookDaoCriteriaの作成（Criteria API）

* 目的: 書籍データアクセス用のDAOを実装する（Criteria API使用）
* 対象: `pro.kensait.backoffice.dao.BookDaoCriteria`
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.3 Data Access Layer」
  * [functional_design.md](../specs/baseline/system/functional_design.md) の「3.6 F-BOOK-004」
* 注意事項: 
  * `@ApplicationScoped`、`EntityManager`を`@Inject`
  * `search(Integer categoryId, String keyword)`: 動的検索（Criteria API）
  * `CriteriaBuilder`、`CriteriaQuery`を使用
  * 型安全な動的クエリ構築
  * 検索条件は動的に追加（categoryId、keyword）

---

### T_COMMON_010: [P] CategoryDaoの作成

* 目的: カテゴリデータアクセス用のDAOを実装する
* 対象: `pro.kensait.backoffice.dao.CategoryDao`
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.3 Data Access Layer」
* 注意事項: 
  * `@ApplicationScoped`、`EntityManager`を`@Inject`
  * `findAll()`: 全カテゴリ取得
  * `findById(Integer categoryId)`: ID指定取得

---

### T_COMMON_011: [P] PublisherDaoの作成

* 目的: 出版社データアクセス用のDAOを実装する
* 対象: `pro.kensait.backoffice.dao.PublisherDao`
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.3 Data Access Layer」
* 注意事項: 
  * `@ApplicationScoped`、`EntityManager`を`@Inject`
  * `findAll()`: 全出版社取得
  * `findById(Integer publisherId)`: ID指定取得

---

### T_COMMON_012: [P] StockDaoの作成

* 目的: 在庫データアクセス用のDAOを実装する（楽観的ロック対応）
* 対象: `pro.kensait.backoffice.dao.StockDao`
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.3 Data Access Layer」
  * [functional_design.md](../specs/baseline/system/functional_design.md) の「3.6 F-STOCK-003」
* 注意事項: 
  * `@ApplicationScoped`、`EntityManager`を`@Inject`
  * `findAll()`: 全在庫取得
  * `findById(Integer bookId)`: ID指定取得
  * 楽観的ロックは`@Version`アノテーションで自動処理される
  * `OptimisticLockException`をスローする可能性がある

---

### T_COMMON_013: [P] EmployeeDaoの作成

* 目的: 社員データアクセス用のDAOを実装する
* 対象: `pro.kensait.backoffice.dao.EmployeeDao`
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.3 Data Access Layer」
  * [functional_design.md](../specs/baseline/system/functional_design.md) の「3.1 F-AUTH-001」
* 注意事項: 
  * `@ApplicationScoped`、`EntityManager`を`@Inject`
  * `findById(Long employeeId)`: ID指定取得
  * `findByCode(String employeeCode)`: 社員コード指定取得（ログイン時に使用）

---

### T_COMMON_014: [P] DepartmentDaoの作成

* 目的: 部署データアクセス用のDAOを実装する
* 対象: `pro.kensait.backoffice.dao.DepartmentDao`
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.3 Data Access Layer」
* 注意事項: 
  * `@ApplicationScoped`、`EntityManager`を`@Inject`
  * `findById(Long departmentId)`: ID指定取得
  * `findAll()`: 全部署取得

---

### T_COMMON_015: [P] WorkflowDaoの作成

* 目的: ワークフローデータアクセス用のDAOを実装する
* 対象: `pro.kensait.backoffice.dao.WorkflowDao`
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.3 Data Access Layer」
  * [functional_design.md](../specs/baseline/system/functional_design.md) の「3.7 F-WORKFLOW-001」
* 注意事項: 
  * `@ApplicationScoped`、`EntityManager`を`@Inject`
  * `insert(Workflow workflow)`: ワークフロー履歴挿入
  * `findLatestByWorkflowId(Long workflowId)`: 最新状態取得（最大OPERATION_ID）
  * `findHistoryByWorkflowId(Long workflowId)`: 全履歴取得
  * `findAll(String state, String workflowType)`: フィルタリング付き一覧取得
  * `getNextWorkflowId()`: 次のワークフローID採番

---

## 3. 共通DTO（Data Transfer Objects）

### T_COMMON_016: [P] ErrorResponseの作成

* 目的: エラーレスポンス用のDTOを実装する
* 対象: `pro.kensait.backoffice.api.dto.ErrorResponse`（Record）
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.2 エラーレスポンス形式」
* 注意事項: 
  * Java Record形式で実装
  * フィールド: `error`（String）、`message`（String）

---

### T_COMMON_017: [P] CategoryInfoの作成

* 目的: カテゴリ情報用のDTOを実装する（ネスト用）
* 対象: `pro.kensait.backoffice.api.dto.CategoryInfo`（Record）
* 参照SPEC: 
  * [functional_design.md](../specs/baseline/system/functional_design.md) の「3.3 F-BOOK-001」
* 注意事項: 
  * Java Record形式で実装
  * フィールド: `categoryId`（Integer）、`categoryName`（String）

---

### T_COMMON_018: [P] PublisherInfoの作成

* 目的: 出版社情報用のDTOを実装する（ネスト用）
* 対象: `pro.kensait.backoffice.api.dto.PublisherInfo`（Record）
* 参照SPEC: 
  * [functional_design.md](../specs/baseline/system/functional_design.md) の「3.3 F-BOOK-001」
* 注意事項: 
  * Java Record形式で実装
  * フィールド: `publisherId`（Integer）、`publisherName`（String）

---

## 4. セキュリティ基盤

### T_COMMON_019: JwtUtilの作成

* 目的: JWT生成・検証ユーティリティを実装する
* 対象: `pro.kensait.backoffice.security.JwtUtil`
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「7. セキュリティアーキテクチャ」
  * [functional_design.md](../specs/baseline/system/functional_design.md) の「3.1 F-AUTH-001」
* 注意事項: 
  * `@ApplicationScoped`
  * MicroProfile Configで`jwt.secret-key`、`jwt.expiration-ms`を読み込み
  * `generateToken(Long employeeId, String employeeCode, Long departmentId)`: JWT生成
  * `validateToken(String token)`: JWT検証
  * `getClaimsFromToken(String token)`: クレーム取得
  * JJWT（io.jsonwebtoken）ライブラリ使用
  * 署名アルゴリズム: HMAC-SHA256
  * Payload: sub（employeeId）、employeeCode、departmentId、iat（発行日時）、exp（有効期限）

---

### T_COMMON_020: BCryptパスワードユーティリティの作成

* 目的: BCryptパスワードハッシュ化・検証ユーティリティを実装する
* 対象: `pro.kensait.backoffice.security.PasswordUtil`（オプション）
* 参照SPEC: 
  * [functional_design.md](../specs/baseline/system/functional_design.md) の「4.1 BR-AUTH-001」
* 注意事項: 
  * BCrypt（`org.mindrot:jbcrypt`）ライブラリ使用
  * `checkPassword(String plainPassword, String hashedPassword)`: パスワード照合
  * BCryptハッシュ判定（`$2a$`、`$2b$`、`$2y$`で始まる）
  * 平文パスワードの場合は文字列比較（開発環境のみ）

---

## 5. ユーティリティ

### T_COMMON_021: MessageUtilの作成

* 目的: メッセージプロパティファイルから日本語メッセージを取得するユーティリティを実装する
* 対象: `pro.kensait.backoffice.util.MessageUtil`
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.5 Cross-Cutting Concerns」
* 注意事項: 
  * `@ApplicationScoped`
  * `ResourceBundle`を使用して`messages.properties`から読み込み
  * `getMessage(String key)`: キーに対応するメッセージ取得
  * `getMessage(String key, Object... params)`: パラメータ付きメッセージ取得

---

## 6. 例外クラス

### T_COMMON_022: [P] ワークフロー例外クラスの作成

* 目的: ワークフロー関連の業務例外クラスを実装する
* 対象: 
  * `pro.kensait.backoffice.exception.WorkflowNotFoundException`
  * `pro.kensait.backoffice.exception.InvalidWorkflowStateException`
  * `pro.kensait.backoffice.exception.UnauthorizedApprovalException`
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.1 例外マッピング」
* 注意事項: 
  * 全て`RuntimeException`を継承
  * コンストラクタでメッセージを受け取る

---

## 7. 例外ハンドラー（Exception Mappers）

### T_COMMON_023: GenericExceptionMapperの作成

* 目的: 一般的な例外を処理するException Mapperを実装する
* 対象: `pro.kensait.backoffice.api.exception.GenericExceptionMapper`
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9. エラーハンドリング」
* 注意事項: 
  * `@Provider`
  * `ExceptionMapper<Exception>`を実装
  * `IllegalArgumentException` → 400 Bad Request
  * その他の例外 → 500 Internal Server Error
  * ErrorResponse形式でレスポンス

---

### T_COMMON_024: NotFoundExceptionMapperの作成

* 目的: 404エラーを処理するException Mapperを実装する
* 対象: `pro.kensait.backoffice.api.exception.NotFoundExceptionMapper`
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.1 例外マッピング」
* 注意事項: 
  * `@Provider`
  * `ExceptionMapper<NotFoundException>`を実装
  * 404 Not Found
  * ErrorResponse形式でレスポンス

---

### T_COMMON_025: OptimisticLockExceptionMapperの作成

* 目的: 楽観的ロック例外を処理するException Mapperを実装する
* 対象: `pro.kensait.backoffice.api.exception.OptimisticLockExceptionMapper`
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.1 例外マッピング」
* 注意事項: 
  * `@Provider`
  * `ExceptionMapper<OptimisticLockException>`を実装
  * 409 Conflict
  * ErrorResponse形式でレスポンス
  * メッセージ: 「他のユーザーによって更新されました。再度お試しください。」

---

### T_COMMON_026: WorkflowExceptionMapperの作成

* 目的: ワークフロー例外を処理するException Mapperを実装する
* 対象: `pro.kensait.backoffice.api.exception.WorkflowExceptionMapper`
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.1 例外マッピング」
* 注意事項: 
  * `@Provider`
  * 複数のワークフロー例外に対応:
    * `WorkflowNotFoundException` → 404 Not Found
    * `InvalidWorkflowStateException` → 400 Bad Request
    * `UnauthorizedApprovalException` → 403 Forbidden
    * ErrorResponse形式でレスポンス

---

## 8. JAX-RS設定

### T_COMMON_027: ApplicationConfigの作成

* 目的: JAX-RSアプリケーション設定クラスを実装する
* 対象: `pro.kensait.backoffice.api.ApplicationConfig`
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.1 Presentation Layer」
* 注意事項: 
  * `jakarta.ws.rs.core.Application`を継承
  * `@ApplicationPath("/api")`
  * ベースパス: `/api`

---

## 完了確認

### チェックリスト

#### エンティティ
* [ ] Category
* [ ] Publisher
* [ ] Book（`@SecondaryTable`でSTOCK結合）
* [ ] Stock（`@Version`で楽観的ロック）
* [ ] Department
* [ ] Employee
* [ ] Workflow

#### DAO
* [ ] BookDao（JPQL）
* [ ] BookDaoCriteria（Criteria API）
* [ ] CategoryDao
* [ ] PublisherDao
* [ ] StockDao
* [ ] EmployeeDao
* [ ] DepartmentDao
* [ ] WorkflowDao

#### DTO
* [ ] ErrorResponse
* [ ] CategoryInfo
* [ ] PublisherInfo

#### セキュリティ
* [ ] JwtUtil
* [ ] PasswordUtil

#### ユーティリティ
* [ ] MessageUtil

#### 例外クラス
* [ ] WorkflowNotFoundException
* [ ] InvalidWorkflowStateException
* [ ] UnauthorizedApprovalException

#### Exception Mappers
* [ ] GenericExceptionMapper
* [ ] NotFoundExceptionMapper
* [ ] OptimisticLockExceptionMapper
* [ ] WorkflowExceptionMapper

#### JAX-RS設定
* [ ] ApplicationConfig

---

## 次のステップ

共通機能実装完了後、以下のタスクに並行して進んでください:

* [API_001_auth.md](API_001_auth.md) - 認証API
* [API_002_books.md](API_002_books.md) - 書籍API
* [API_003_categories.md](API_003_categories.md) - カテゴリAPI
* [API_004_publishers.md](API_004_publishers.md) - 出版社API
* [API_005_stocks.md](API_005_stocks.md) - 在庫API
* [API_006_workflows.md](API_006_workflows.md) - ワークフローAPI

---

タスクファイル作成日: 2025-01-10  
想定実行順序: 2番目（セットアップ後）
