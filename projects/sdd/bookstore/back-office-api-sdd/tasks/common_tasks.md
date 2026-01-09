# 共通機能タスク

**担当者:** 共通機能チーム（2～3名）  
**推奨スキル:** JPA、Jakarta EE、Bean Validation  
**想定工数:** 8時間  
**依存タスク:** [setup_tasks.md](setup_tasks.md)

---

## タスクリスト

### エンティティクラス作成

#### T_COMMON_001: [P] Bookエンティティの作成

- **目的**: 書籍情報を管理するエンティティクラスを作成する
- **対象**: Book.java（Entityクラス）
- **参照SPEC**: 
  - [data_model.md](../specs/baseline/system/data_model.md) の「3.1 BOOK（書籍マスタ）」
  - [data_model.md](../specs/baseline/system/data_model.md) の「4.1 エンティティクラスとテーブルのマッピング」
- **注意事項**: 
  - `@SecondaryTable`でSTOCKテーブルと結合する（BOOK + STOCK）
  - `@ManyToOne`でCategoryとPublisherをマッピングする
  - deletedフィールド（論理削除フラグ）を含める
  - 在庫情報（quantity, version）をSecondaryTableから取得する

---

#### T_COMMON_002: [P] Stockエンティティの作成

- **目的**: 在庫情報を管理するエンティティクラスを作成する（楽観的ロック対応）
- **対象**: Stock.java（Entityクラス）
- **参照SPEC**: 
  - [data_model.md](../specs/baseline/system/data_model.md) の「3.2 STOCK（在庫マスタ）」
  - [functional_design.md (API_005_stocks)](../specs/baseline/api/API_005_stocks/functional_design.md) の「4. 楽観的ロックの仕組み」
- **注意事項**: 
  - **重要**: `@Version`アノテーションをversionフィールドに付与する（楽観的ロック必須）
  - bookIdをPRIMARY KEYとして設定する
  - quantityフィールド（在庫数）を含める

---

#### T_COMMON_003: [P] Categoryエンティティの作成

- **目的**: カテゴリ情報を管理するエンティティクラスを作成する
- **対象**: Category.java（Entityクラス）
- **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.3 CATEGORY（カテゴリマスタ）」
- **注意事項**: 
  - categoryId（PRIMARY KEY）とcategoryNameを含める
  - マスタデータとして扱う

---

#### T_COMMON_004: [P] Publisherエンティティの作成

- **目的**: 出版社情報を管理するエンティティクラスを作成する
- **対象**: Publisher.java（Entityクラス）
- **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.4 PUBLISHER（出版社マスタ）」
- **注意事項**: 
  - publisherId（PRIMARY KEY）とpublisherNameを含める
  - マスタデータとして扱う

---

#### T_COMMON_005: [P] Employeeエンティティの作成

- **目的**: 社員情報を管理するエンティティクラスを作成する
- **対象**: Employee.java（Entityクラス）
- **参照SPEC**: 
  - [data_model.md](../specs/baseline/system/data_model.md) の「3.5 EMPLOYEE（社員マスタ）」
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「6. セキュリティアーキテクチャ」
- **注意事項**: 
  - employeeCode（UNIQUE KEY）を含める
  - password（BCryptハッシュ）を含める
  - jobRank（職務ランク: 1=ASSOCIATE, 2=MANAGER, 3=DIRECTOR）を含める
  - `@ManyToOne`でDepartmentをマッピングする

---

#### T_COMMON_006: [P] Departmentエンティティの作成

- **目的**: 部署情報を管理するエンティティクラスを作成する
- **対象**: Department.java（Entityクラス）
- **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.6 DEPARTMENT（部署マスタ）」
- **注意事項**: 
  - departmentId（PRIMARY KEY）とdepartmentNameを含める
  - `@OneToMany`でEmployeeとの双方向関係を設定する

---

#### T_COMMON_007: [P] Workflowエンティティの作成

- **目的**: ワークフロー履歴を管理するエンティティクラスを作成する
- **対象**: Workflow.java（Entityクラス）
- **参照SPEC**: 
  - [data_model.md](../specs/baseline/system/data_model.md) の「3.7 WORKFLOW（ワークフロー履歴）」
  - [functional_design.md (API_006_workflows)](../specs/baseline/api/API_006_workflows/functional_design.md) の「7. ワークフロー履歴」
- **注意事項**: 
  - operationId（PRIMARY KEY、自動採番）を含める
  - workflowId（複数行で共通）を含める
  - state（CREATED, APPLIED, APPROVED）を含める
  - operationType（CREATE, APPLY, APPROVE, REJECT）を含める
  - `@ManyToOne`でBook、Category、Publisher、Employeeをマッピングする（insertable=false, updatable=false）

---

### DAOクラス作成

#### T_COMMON_008: BookDaoの作成（JPQL検索）

- **目的**: 書籍情報のデータアクセスクラスを作成する（JPQL使用）
- **対象**: BookDao.java（DAOクラス）
- **参照SPEC**: 
  - [functional_design.md (API_002_books)](../specs/baseline/api/API_002_books/functional_design.md) の「3.4 書籍検索（JPQL）」
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.3 Data Access Layer」
- **注意事項**: 
  - findAll()、findById()、query()メソッドを実装する
  - JPQLでカテゴリ・キーワード検索を実装する
  - 論理削除された書籍（deleted=true）を除外する

---

#### T_COMMON_009: BookDaoCriteriaの作成（Criteria API検索）

- **目的**: 書籍情報のデータアクセスクラスを作成する（Criteria API使用）
- **対象**: BookDaoCriteria.java（DAOクラス）
- **参照SPEC**: 
  - [functional_design.md (API_002_books)](../specs/baseline/api/API_002_books/functional_design.md) の「3.5 書籍検索（Criteria API）」
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.3 Data Access Layer」
- **注意事項**: 
  - **重要**: Criteria APIで動的クエリを構築する
  - searchWithCriteria()メソッドを実装する
  - カテゴリIDとキーワードの有無に応じて動的に条件を追加する
  - タイプセーフなクエリ構築を行う

---

#### T_COMMON_010: [P] StockDaoの作成

- **目的**: 在庫情報のデータアクセスクラスを作成する（楽観的ロック対応）
- **対象**: StockDao.java（DAOクラス）
- **参照SPEC**: 
  - [functional_design.md (API_005_stocks)](../specs/baseline/api/API_005_stocks/functional_design.md) の「4. 楽観的ロックの仕組み」
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.3 Data Access Layer」
- **注意事項**: 
  - findAll()、findById()、update()メソッドを実装する
  - 楽観的ロックはJPAの`@Version`により自動的に処理される

---

#### T_COMMON_011: [P] CategoryDaoの作成

- **目的**: カテゴリ情報のデータアクセスクラスを作成する
- **対象**: CategoryDao.java（DAOクラス）
- **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.3 Data Access Layer」
- **注意事項**: 
  - findAll()メソッドを実装する
  - マスタデータの取得のみ

---

#### T_COMMON_012: [P] PublisherDaoの作成

- **目的**: 出版社情報のデータアクセスクラスを作成する
- **対象**: PublisherDao.java（DAOクラス）
- **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.3 Data Access Layer」
- **注意事項**: 
  - findAll()メソッドを実装する
  - マスタデータの取得のみ

---

#### T_COMMON_013: [P] EmployeeDaoの作成

- **目的**: 社員情報のデータアクセスクラスを作成する
- **対象**: EmployeeDao.java（DAOクラス）
- **参照SPEC**: 
  - [functional_design.md (API_001_auth)](../specs/baseline/api/API_001_auth/functional_design.md) の「3.1 ログイン」
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.3 Data Access Layer」
- **注意事項**: 
  - findByCode()、findById()メソッドを実装する
  - 認証とワークフロー承認権限チェックで使用される

---

#### T_COMMON_014: [P] DepartmentDaoの作成

- **目的**: 部署情報のデータアクセスクラスを作成する
- **対象**: DepartmentDao.java（DAOクラス）
- **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.3 Data Access Layer」
- **注意事項**: 
  - findAll()、findById()メソッドを実装する
  - マスタデータの取得

---

#### T_COMMON_015: [P] WorkflowDaoの作成

- **目的**: ワークフロー履歴のデータアクセスクラスを作成する
- **対象**: WorkflowDao.java（DAOクラス）
- **参照SPEC**: 
  - [functional_design.md (API_006_workflows)](../specs/baseline/api/API_006_workflows/functional_design.md) の「7. ワークフロー履歴」
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.3 Data Access Layer」
- **注意事項**: 
  - findLatestByWorkflowId()、findHistoryByWorkflowId()、persist()メソッドを実装する
  - getNextWorkflowId()でワークフローIDを採番する
  - 最新の状態取得と履歴取得を実装する

---

### 共通DTOクラス作成

#### T_COMMON_016: [P] ErrorResponseの作成

- **目的**: エラーレスポンス用のDTOクラスを作成する
- **対象**: ErrorResponse.java（DTOクラス）
- **参照SPEC**: 
  - [functional_design.md (system)](../specs/baseline/system/functional_design.md) の「6. エラーハンドリング」
  - [functional_design.md (API_001_auth)](../specs/baseline/api/API_001_auth/functional_design.md) の「4.3 ErrorResponse」
- **注意事項**: 
  - errorフィールド（エラー種別）を含める
  - messageフィールド（エラーメッセージ）を含める
  - レコード型（immutable）で実装する

---

#### T_COMMON_017: [P] CategoryInfoの作成

- **目的**: カテゴリ情報のネスト用DTOクラスを作成する
- **対象**: CategoryInfo.java（DTOクラス）
- **参照SPEC**: [functional_design.md (API_002_books)](../specs/baseline/api/API_002_books/functional_design.md) の「4.1 BookTO」
- **注意事項**: 
  - categoryId、categoryNameを含める
  - BookTOのネスト構造として使用される

---

#### T_COMMON_018: [P] PublisherInfoの作成

- **目的**: 出版社情報のネスト用DTOクラスを作成する
- **対象**: PublisherInfo.java（DTOクラス）
- **参照SPEC**: [functional_design.md (API_002_books)](../specs/baseline/api/API_002_books/functional_design.md) の「4.1 BookTO」
- **注意事項**: 
  - publisherId、publisherNameを含める
  - BookTOのネスト構造として使用される

---

### 例外ハンドラ作成

#### T_COMMON_019: [P] GenericExceptionMapperの作成

- **目的**: 一般的な例外をハンドリングするException Mapperを作成する
- **対象**: GenericExceptionMapper.java（Exception Mapperクラス）
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「8. エラーハンドリング」
  - [functional_design.md (system)](../specs/baseline/system/functional_design.md) の「6. エラーハンドリング」
- **注意事項**: 
  - IllegalArgumentException → 400 Bad Request
  - 一般的な例外 → 500 Internal Server Error
  - ErrorResponseを返却する

---

#### T_COMMON_020: [P] OptimisticLockExceptionMapperの作成

- **目的**: 楽観的ロック例外をハンドリングするException Mapperを作成する
- **対象**: OptimisticLockExceptionMapper.java（Exception Mapperクラス）
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「8. エラーハンドリング」
  - [functional_design.md (API_005_stocks)](../specs/baseline/api/API_005_stocks/functional_design.md) の「7. エラーハンドリング」
- **注意事項**: 
  - **重要**: OptimisticLockException → 409 Conflict
  - 「在庫が他のユーザーによって更新されました」メッセージを返す

---

#### T_COMMON_021: [P] NotFoundExceptionMapperの作成

- **目的**: 404エラーをハンドリングするException Mapperを作成する
- **対象**: NotFoundExceptionMapper.java（Exception Mapperクラス）
- **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「8. エラーハンドリング」
- **注意事項**: 
  - NotFoundException → 404 Not Found
  - ErrorResponseを返却する

---

#### T_COMMON_022: [P] WorkflowExceptionMapperの作成

- **目的**: ワークフロー関連の例外をハンドリングするException Mapperを作成する
- **対象**: WorkflowExceptionMapper.java（Exception Mapperクラス）
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「8. エラーハンドリング」
  - [functional_design.md (API_006_workflows)](../specs/baseline/api/API_006_workflows/functional_design.md) の「12. エラーハンドリング」
- **注意事項**: 
  - WorkflowNotFoundException → 404 Not Found
  - InvalidWorkflowStateException → 400 Bad Request
  - UnauthorizedApprovalException → 403 Forbidden

---

### ユーティリティクラス作成

#### T_COMMON_023: JwtUtilの作成

- **目的**: JWT生成・検証のユーティリティクラスを作成する
- **対象**: JwtUtil.java（ユーティリティクラス）
- **参照SPEC**: 
  - [functional_design.md (API_001_auth)](../specs/baseline/api/API_001_auth/functional_design.md) の「3.1.6 JWT構造」
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「6. セキュリティアーキテクチャ」
- **注意事項**: 
  - generateToken()メソッドを実装する（employeeId, employeeCode, departmentIdをペイロードに含める）
  - HMAC-SHA256で署名する
  - MicroProfile Configから秘密鍵と有効期限を取得する
  - getCookieName()、getExpirationSeconds()メソッドを実装する

---

#### T_COMMON_024: [P] MessageUtilの作成

- **目的**: プロパティファイルからメッセージを取得するユーティリティクラスを作成する
- **対象**: MessageUtil.java（ユーティリティクラス）
- **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.5 Cross-Cutting Concerns」
- **注意事項**: 
  - messages.propertiesからメッセージを取得する
  - 日本語メッセージをサポートする

---

### JAX-RS設定

#### T_COMMON_025: ApplicationConfigの作成

- **目的**: JAX-RSアプリケーション設定クラスを作成する
- **対象**: ApplicationConfig.java（JAX-RS Applicationクラス）
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.1 Presentation Layer」
  - [functional_design.md (system)](../specs/baseline/system/functional_design.md) の「2. 機能一覧」
- **注意事項**: 
  - `@ApplicationPath("/api")`でベースパスを設定する
  - すべてのResourceクラスとException Mapperを登録する

---

## 共通機能完了チェックリスト

### エンティティ
- [ ] Bookエンティティが作成された（SecondaryTableでSTOCK結合）
- [ ] Stockエンティティが作成された（@Version付与）
- [ ] Categoryエンティティが作成された
- [ ] Publisherエンティティが作成された
- [ ] Employeeエンティティが作成された
- [ ] Departmentエンティティが作成された
- [ ] Workflowエンティティが作成された

### DAO
- [ ] BookDaoが作成された（JPQL検索）
- [ ] BookDaoCriteriaが作成された（Criteria API検索）
- [ ] StockDaoが作成された
- [ ] CategoryDaoが作成された
- [ ] PublisherDaoが作成された
- [ ] EmployeeDaoが作成された
- [ ] DepartmentDaoが作成された
- [ ] WorkflowDaoが作成された

### DTO
- [ ] ErrorResponseが作成された
- [ ] CategoryInfoが作成された
- [ ] PublisherInfoが作成された

### 例外ハンドラ
- [ ] GenericExceptionMapperが作成された
- [ ] OptimisticLockExceptionMapperが作成された（409 Conflict）
- [ ] NotFoundExceptionMapperが作成された
- [ ] WorkflowExceptionMapperが作成された

### ユーティリティ
- [ ] JwtUtilが作成された
- [ ] MessageUtilが作成された

### JAX-RS設定
- [ ] ApplicationConfigが作成された（/apiベースパス）

---

## 次のステップ

共通機能が完了したら、各API実装タスクに進んでください：
- [認証API](API_001_auth.md)
- [書籍API](API_002_books.md)
- [カテゴリAPI](API_003_categories.md)
- [出版社API](API_004_publishers.md)
- [在庫API](API_005_stocks.md)
- [ワークフローAPI](API_006_workflows.md)
