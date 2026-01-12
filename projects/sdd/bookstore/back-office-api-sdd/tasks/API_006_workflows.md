# API_006_workflows - ワークフローAPIタスク

担当者: 1名  
推奨スキル: JAX-RS、JPA、トランザクション管理、ビジネスロジック  
想定工数: 16時間  
依存タスク: [common_tasks.md](common_tasks.md)

---

## 概要

このタスクリストは、ワークフローAPI（作成、更新、申請、承認、却下、一覧取得、履歴取得）の実装タスクを含みます。複雑なビジネスロジックとトランザクション管理が必要です。

---

## タスクリスト

## 1. DTO（Data Transfer Objects）

### T_API006_001: WorkflowTOの作成

* 目的: ワークフロー情報レスポンス用のDTOを実装する
* 対象: `pro.kensait.backoffice.api.dto.WorkflowTO`（Record）
* 参照SPEC: 
  * [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「2.1 POST /api/workflows」
* 注意事項: 
  * Java Record形式で実装
  * フィールド: 
    * `operationId`（Long）
    * `workflowId`（Long）
    * `workflowType`（String）- ADD_NEW_BOOK, REMOVE_BOOK, ADJUST_BOOK_PRICE
    * `state`（String）- CREATED, APPLIED, APPROVED
    * `bookId`（Integer）
    * `bookName`（String）
    * `author`（String）
    * `categoryId`（Integer）
    * `publisherId`（Integer）
    * `price`（BigDecimal）
    * `imageUrl`（String）
    * `applyReason`（String）
    * `startDate`（LocalDate）
    * `endDate`（LocalDate）
    * `operationType`（String）- CREATE, APPLY, APPROVE, REJECT
    * `operatedBy`（Long）
    * `operatedAt`（LocalDateTime）
    * `operationReason`（String）

---

### T_API006_002: WorkflowCreateRequestの作成

* 目的: ワークフロー作成リクエスト用のDTOを実装する
* 対象: `pro.kensait.backoffice.api.dto.WorkflowCreateRequest`（Record）
* 参照SPEC: 
  * [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「2.1 POST /api/workflows」
* 注意事項: 
  * Java Record形式で実装
  * フィールド（共通）: `workflowType`、`createdBy`、`applyReason`
  * フィールド（ADD_NEW_BOOK）: `bookName`、`author`、`price`、`imageUrl`、`categoryId`、`publisherId`
  * フィールド（REMOVE_BOOK）: `bookId`
  * フィールド（ADJUST_BOOK_PRICE）: `bookId`、`price`、`startDate`、`endDate`
  * Bean Validation: `@NotNull`、`@NotBlank`

---

### T_API006_003: WorkflowUpdateRequestの作成

* 目的: ワークフロー更新リクエスト用のDTOを実装する
* 対象: `pro.kensait.backoffice.api.dto.WorkflowUpdateRequest`（Record）
* 参照SPEC: 
  * [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「2.2 PUT /api/workflows/{workflowId}」
* 注意事項: 
  * Java Record形式で実装
  * フィールド: `updatedBy`、ワークフロータイプごとの更新項目

---

### T_API006_004: WorkflowOperationRequestの作成

* 目的: ワークフロー操作（申請、承認、却下）リクエスト用のDTOを実装する
* 対象: `pro.kensait.backoffice.api.dto.WorkflowOperationRequest`（Record）
* 参照SPEC: 
  * [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「2.3 POST /api/workflows/{workflowId}/apply」
* 注意事項: 
  * Java Record形式で実装
  * フィールド: `operatedBy`、`operationReason`（オプション）

---

## 2. Enum型

### T_API006_005: [P] ワークフロー関連Enum型の作成

* 目的: ワークフロータイプ、状態、操作タイプのEnum型を実装する
* 対象: 
  * `pro.kensait.backoffice.common.WorkflowType`（Enum）
  * `pro.kensait.backoffice.common.WorkflowState`（Enum）
  * `pro.kensait.backoffice.common.OperationType`（Enum）
  * `pro.kensait.backoffice.common.JobRankType`（Enum）
* 参照SPEC: 
  * [data_model.md](../specs/baseline/system/data_model.md) の「3.7 WORKFLOW」
* 注意事項: 
  * WorkflowType: ADD_NEW_BOOK, REMOVE_BOOK, ADJUST_BOOK_PRICE
  * WorkflowState: CREATED, APPLIED, APPROVED
  * OperationType: CREATE, APPLY, APPROVE, REJECT
  * JobRankType: ASSOCIATE(1), MANAGER(2), DIRECTOR(3)

---

## 3. ビジネスロジック（Service）

### T_API006_006: WorkflowServiceの作成

* 目的: ワークフロー管理ビジネスロジックを実装する
* 対象: `pro.kensait.backoffice.service.WorkflowService`
* 参照SPEC: 
  * [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「3. ビジネスロジック」
  * [functional_design.md](../specs/baseline/system/functional_design.md) の「3.7 F-WORKFLOW-001」～「3.10 F-WORKFLOW-008」
* 注意事項: 
  * `@ApplicationScoped`、`@Transactional`
  * WorkflowDao、EmployeeDao、BookDao、DepartmentDaoを`@Inject`
  * メソッド:
    * `createWorkflow(WorkflowCreateRequest request)`: ワークフロー作成
    * `updateWorkflow(Long workflowId, WorkflowUpdateRequest request)`: ワークフロー更新
    * `applyWorkflow(Long workflowId, WorkflowOperationRequest request)`: ワークフロー申請
    * `approveWorkflow(Long workflowId, WorkflowOperationRequest request)`: ワークフロー承認
    * `rejectWorkflow(Long workflowId, WorkflowOperationRequest request)`: ワークフロー却下
    * `getWorkflows(String state, String workflowType)`: ワークフロー一覧取得
    * `getWorkflowHistory(Long workflowId)`: ワークフロー履歴取得
    * `checkApprovalAuthority(Employee approver, Workflow workflow)`: 承認権限チェック
    * `applyToBookMaster(Workflow workflow)`: 書籍マスタ反映
    * WorkflowエンティティをWorkflowTOに変換

---

### T_API006_007: ワークフロー状態遷移ロジックの実装

* 目的: ワークフロー状態遷移のビジネスルールを実装する
* 対象: `WorkflowService`内のメソッド
* 参照SPEC: 
  * [functional_design.md](../specs/baseline/system/functional_design.md) の「4.2 BR-WORKFLOW-001」
* 注意事項: 
  * 状態遷移: CREATED → APPLIED → APPROVED
  * 却下: APPLIED → CREATED
  * 不正な状態遷移の場合は`InvalidWorkflowStateException`をスロー
  * 状態チェック:
    * 申請: CREATEDのみ可
    * 承認: APPLIEDのみ可
    * 却下: APPLIEDのみ可
    * 更新: CREATEDのみ可

---

### T_API006_008: 承認権限チェックロジックの実装

* 目的: 承認権限のビジネスルールを実装する
* 対象: `WorkflowService#checkApprovalAuthority()`
* 参照SPEC: 
  * [functional_design.md](../specs/baseline/system/functional_design.md) の「4.2 BR-WORKFLOW-002」
* 注意事項: 
  * 職務ランクチェック:
    * ASSOCIATE（JOB_RANK=1）: 承認不可
    * MANAGER（JOB_RANK=2）: 同一部署のみ承認可
    * DIRECTOR（JOB_RANK=3）: 全部署承認可
    * 権限不足の場合は`UnauthorizedApprovalException`をスロー

---

### T_API006_009: 書籍マスタ反映ロジックの実装

* 目的: 承認されたワークフローを書籍マスタに反映する
* 対象: `WorkflowService#applyToBookMaster()`
* 参照SPEC: 
  * [functional_design.md](../specs/baseline/system/functional_design.md) の「3.10 F-WORKFLOW-008」
* 注意事項: 
  * ADD_NEW_BOOK: Book + Stock INSERT
    * 初期在庫数: 0
    * 初期バージョン: 0
    * deletedフラグ: false
  * REMOVE_BOOK: Book論理削除（deleted = true）
  * ADJUST_BOOK_PRICE: Book価格更新
  * EntityManager.persist()、EntityManager.flush()を使用
  * トランザクション内で実行（ワークフロー履歴追加と同時）

---

## 4. プレゼンテーション層（Resource）

### T_API006_010: WorkflowResourceの作成

* 目的: ワークフローAPIのエンドポイントを実装する
* 対象: `pro.kensait.backoffice.api.WorkflowResource`
* 参照SPEC: 
  * [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「2. エンドポイント仕様」
  * [behaviors.md](../specs/baseline/api/API_006_workflows/behaviors.md) の「2. ワークフローAPI」
* 注意事項: 
  * `@Path("/workflows")`、`@ApplicationScoped`
  * WorkflowServiceを`@Inject`
  * エンドポイント:
    * `@POST`: ワークフロー作成（201 Created）
    * `@PUT @Path("/{workflowId}")`: ワークフロー更新（200 OK）
    * `@POST @Path("/{workflowId}/apply")`: ワークフロー申請（200 OK）
    * `@POST @Path("/{workflowId}/approve")`: ワークフロー承認（200 OK）
    * `@POST @Path("/{workflowId}/reject")`: ワークフロー却下（200 OK）
    * `@GET`: ワークフロー一覧取得（200 OK）
      * クエリパラメータ: `state`、`workflowType`
    * `@GET @Path("/{workflowId}/history")`: ワークフロー履歴取得（200 OK）
    * レスポンス: WorkflowTO または List<WorkflowTO>
    * HTTPステータス: 200 OK、201 Created、400 Bad Request、403 Forbidden、404 Not Found
    * ログ出力: INFO（API呼び出し）、WARN（権限不足）

---

## 5. テスト

### T_API006_011: ワークフロー作成の単体テスト

* 目的: WorkflowService（作成）のテストケースを実装する
* 対象: `pro.kensait.backoffice.service.WorkflowServiceTest`
* 参照SPEC: 
  * [behaviors.md](../specs/baseline/api/API_006_workflows/behaviors.md) の「2. ワークフローAPI」
* 注意事項: 
  * JUnit 5 + Mockito使用
  * WorkflowDao、EmployeeDaoをモック化
  * テストケース:
    * `testCreateWorkflow_AddNewBook()`: 新規書籍追加ワークフロー作成
    * `testCreateWorkflow_RemoveBook()`: 書籍削除ワークフロー作成
    * `testCreateWorkflow_AdjustPrice()`: 価格改定ワークフロー作成

---

### T_API006_012: ワークフロー申請・承認の単体テスト

* 目的: WorkflowService（申請・承認）のテストケースを実装する
* 対象: `pro.kensait.backoffice.service.WorkflowServiceTest`
* 参照SPEC: 
  * [behaviors.md](../specs/baseline/api/API_006_workflows/behaviors.md) の「2. ワークフローAPI」
* 注意事項: 
  * テストケース:
    * `testApplyWorkflow_Success()`: 申請成功（CREATED → APPLIED）
    * `testApplyWorkflow_InvalidState()`: 不正な状態（400 Bad Request）
    * `testApproveWorkflow_Success()`: 承認成功（APPLIED → APPROVED）
    * `testApproveWorkflow_Unauthorized()`: 権限不足（403 Forbidden）
    * `testApproveWorkflow_InvalidState()`: 不正な状態（400 Bad Request）

---

### T_API006_013: 書籍マスタ反映の単体テスト

* 目的: WorkflowService（書籍マスタ反映）のテストケースを実装する
* 対象: `pro.kensait.backoffice.service.WorkflowServiceTest`
* 参照SPEC: 
  * [functional_design.md](../specs/baseline/system/functional_design.md) の「3.10 F-WORKFLOW-008」
* 注意事項: 
  * テストケース:
    * `testApplyToBookMaster_AddNewBook()`: 新規書籍追加の反映
    * `testApplyToBookMaster_RemoveBook()`: 書籍削除（論理削除）の反映
    * `testApplyToBookMaster_AdjustPrice()`: 価格改定の反映

---

## 完了確認

### チェックリスト

#### DTO
* [X] WorkflowTO
* [X] WorkflowCreateRequest
* [X] WorkflowUpdateRequest
* [X] WorkflowOperationRequest

#### Enum型
* [X] WorkflowType
* [X] WorkflowState
* [X] OperationType
* [X] JobRankType

#### ビジネスロジック
* [X] WorkflowService
* [X] ワークフロー状態遷移ロジック
* [X] 承認権限チェックロジック
* [X] 書籍マスタ反映ロジック

#### プレゼンテーション層
* [X] WorkflowResource

#### テスト
* [X] ワークフロー作成の単体テスト
* [X] ワークフロー申請・承認の単体テスト
* [X] 書籍マスタ反映の単体テスト

### 動作確認

以下のcurlコマンドで動作確認:

#### ワークフロー作成（新規書籍追加）
```bash
curl -X POST http://localhost:8080/back-office-api-sdd/api/workflows \
  -H "Content-Type: application/json" \
  -d '{
    "workflowType": "ADD_NEW_BOOK",
    "createdBy": 1,
    "applyReason": "新刊発売のため",
    "bookName": "Java 21入門",
    "author": "山田太郎",
    "price": 3200,
    "imageUrl": "http://example.com/image.jpg",
    "categoryId": 3,
    "publisherId": 1
  }'
```

#### ワークフロー申請
```bash
curl -X POST http://localhost:8080/back-office-api-sdd/api/workflows/1/apply \
  -H "Content-Type: application/json" \
  -d '{
    "operatedBy": 1
  }'
```

#### ワークフロー承認
```bash
curl -X POST http://localhost:8080/back-office-api-sdd/api/workflows/1/approve \
  -H "Content-Type: application/json" \
  -d '{
    "operatedBy": 2,
    "operationReason": "承認します"
  }'
```

#### ワークフロー一覧取得（状態フィルタ）
```bash
curl -X GET "http://localhost:8080/back-office-api-sdd/api/workflows?state=APPLIED"
```

#### ワークフロー履歴取得
```bash
curl -X GET http://localhost:8080/back-office-api-sdd/api/workflows/1/history
```

---

## 次のステップ

このAPI実装完了後、結合テストに進んでください:

* [integration_tasks.md](integration_tasks.md) - 結合テスト

---

タスクファイル作成日: 2025-01-10  
想定実行順序: 8番目（共通機能実装後）
