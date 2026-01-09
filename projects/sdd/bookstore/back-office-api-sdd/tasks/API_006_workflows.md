# ワークフローAPI タスク

**担当者:** 担当者F（1名）  
**推奨スキル:** JAX-RS、JPA、トランザクション管理、ビジネスロジック  
**想定工数:** 10時間  
**依存タスク:** [common_tasks.md](common_tasks.md)

---

## タスクリスト

### T_API006_001: [P] WorkflowCreateRequestの作成

- **目的**: ワークフロー作成リクエスト用のDTOクラスを作成する
- **対象**: WorkflowCreateRequest.java（DTOクラス）
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「10.1 WorkflowCreateRequest」
- **注意事項**: 
  - workflowType、createdBy、applyReasonを含める
  - ワークフロータイプごとの追加フィールド（bookId、bookName、author、price、imageUrl、categoryId、publisherId、startDate、endDate）を含める
  - Bean Validationアノテーションを付与する

---

### T_API006_002: [P] WorkflowTOの作成

- **目的**: ワークフロー情報のデータ転送オブジェクトを作成する
- **対象**: WorkflowTO.java（DTOクラス）
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「10.2 WorkflowTO」
- **注意事項**: 
  - operationId、workflowId、workflowType、state、operationType、operatedBy、operatorName、operatedAtを含める
  - その他のワークフロー関連フィールドを含める

---

### T_API006_003: [P] WorkflowServiceの作成

- **目的**: ワークフロー管理のビジネスロジッククラスを作成する
- **対象**: WorkflowService.java（Serviceクラス）
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.2 Business Logic Layer」
  - [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「5. エンドポイント詳細」
- **注意事項**: 
  - `@ApplicationScoped`でCDI管理する
  - `@Transactional`でトランザクション境界を制御する
  - createWorkflow()、updateWorkflow()、applyWorkflow()、approveWorkflow()、rejectWorkflow()メソッドを実装する
  - checkApprovalAuthority()、applyToBookMaster()メソッドを実装する

---

### T_API006_004: [P] WorkflowResourceの作成

- **目的**: ワークフローAPIのエンドポイントを実装するResourceクラスを作成する
- **対象**: WorkflowResource.java（JAX-RS Resourceクラス）
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「2. エンドポイント一覧」
  - [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「5. エンドポイント詳細」
- **注意事項**: 
  - `@Path("/workflows")`でベースパスを設定する
  - createWorkflow()、updateWorkflow()、getWorkflows()、getWorkflowHistory()、applyWorkflow()、approveWorkflow()、rejectWorkflow()メソッドを実装する

---

### T_API006_005: createWorkflow()メソッドの実装

- **目的**: ワークフロー作成機能を実装する
- **対象**: WorkflowService#createWorkflow()、WorkflowResource#createWorkflow()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「5.1 ワークフロー作成」
- **注意事項**: 
  - 作成者の存在チェック（EmployeeDao）
  - ワークフロータイプのバリデーション
  - 次のワークフローIDを採番（WorkflowDao）
  - Workflowエンティティを生成（STATE=CREATED、OPERATION_TYPE=CREATE）
  - WorkflowDaoでINSERT
  - 201 Createdを返す
  - ログ出力（INFO）を行う

---

### T_API006_006: updateWorkflow()メソッドの実装

- **目的**: ワークフロー更新機能を実装する（一時保存）
- **対象**: WorkflowService#updateWorkflow()、WorkflowResource#updateWorkflow()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「5.2 ワークフロー更新」
- **注意事項**: 
  - 最新の状態を取得（WorkflowDao）
  - 状態チェック：CREATEDでない場合 → 400 Bad Request
  - 更新者の存在チェック（EmployeeDao）
  - 既存のCREATEレコードを直接更新
  - EntityManager#flush()で即座に反映
  - ログ出力（INFO）を行う

---

### T_API006_007: applyWorkflow()メソッドの実装

- **目的**: ワークフロー申請機能を実装する
- **対象**: WorkflowService#applyWorkflow()、WorkflowResource#applyWorkflow()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「5.3 ワークフロー申請」
- **注意事項**: 
  - 最新の状態を取得（WorkflowDao）
  - 状態チェック：CREATEDでない場合 → 400 Bad Request
  - 新しい操作履歴を作成（STATE=APPLIED、OPERATION_TYPE=APPLY）
  - WorkflowDaoでINSERT
  - ログ出力（INFO）を行う

---

### T_API006_008: approveWorkflow()メソッドの実装

- **目的**: ワークフロー承認機能を実装する
- **対象**: WorkflowService#approveWorkflow()、WorkflowResource#approveWorkflow()
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「5.4 ワークフロー承認」
  - [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「6. 承認権限ルール」
- **注意事項**: 
  - **重要**: 承認権限チェックを実装する
  - 最新の状態を取得（WorkflowDao）
  - 状態チェック：APPLIEDでない場合 → 400 Bad Request
  - 承認者の取得（EmployeeDao）
  - 職務ランクチェック：MANAGER以上（JobRankType）
  - 部署チェック：DIRECTORは全部署OK、MANAGERは同一部署のみ
  - 権限不足の場合 → 403 Forbidden
  - 新しい操作履歴を作成（STATE=APPROVED、OPERATION_TYPE=APPROVE）
  - WorkflowDaoでINSERT
  - **書籍マスタへの反映処理**を実行
  - トランザクションコミット（ワークフロー履歴 + 書籍マスタ更新）
  - ログ出力（INFO、WARN）を行う

---

### T_API006_009: rejectWorkflow()メソッドの実装

- **目的**: ワークフロー却下機能を実装する
- **対象**: WorkflowService#rejectWorkflow()、WorkflowResource#rejectWorkflow()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「5.5 ワークフロー却下」
- **注意事項**: 
  - 最新の状態を取得（WorkflowDao）
  - 状態チェック：APPLIEDでない場合 → 400 Bad Request
  - 新しい操作履歴を作成（STATE=CREATED、OPERATION_TYPE=REJECT）
  - WorkflowDaoでINSERT
  - ログ出力（INFO）を行う

---

### T_API006_010: applyToBookMaster()メソッドの実装

- **目的**: 承認されたワークフローの内容を書籍マスタに反映する
- **対象**: WorkflowService#applyToBookMaster()
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「8. 書籍マスタ反映（承認時）」
  - [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「9. トランザクション管理」
- **注意事項**: 
  - **ADD_NEW_BOOK**: 新しいBookエンティティを作成してINSERT（在庫情報も自動作成）
  - **REMOVE_BOOK**: BookDaoで対象書籍を取得してdeletedフラグをtrueに設定
  - **ADJUST_BOOK_PRICE**: BookDaoで対象書籍を取得してpriceフィールドを更新
  - EntityManager#persist()、EntityManager#flush()を使用
  - トランザクション内で実行される

---

### T_API006_011: checkApprovalAuthority()メソッドの実装

- **目的**: 承認権限チェック機能を実装する
- **対象**: WorkflowService#checkApprovalAuthority()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「6. 承認権限ルール」
- **注意事項**: 
  - 承認者の職務ランクを取得
  - MANAGER未満の場合 → UnauthorizedApprovalException
  - DIRECTORの場合 → 承認OK（全部署）
  - MANAGERの場合 → 部署IDチェック（同一部署のみ）
  - 権限不足の場合 → UnauthorizedApprovalException

---

### T_API006_012: getWorkflows()メソッドの実装

- **目的**: ワークフロー一覧取得機能を実装する
- **対象**: WorkflowResource#getWorkflows()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「5.6 ワークフロー一覧取得」
- **注意事項**: 
  - クエリパラメータからstate、workflowTypeを取得
  - WorkflowDaoで条件に応じてフィルタリング
  - 最新の状態のみを取得
  - ログ出力（INFO）を行う

---

### T_API006_013: getWorkflowHistory()メソッドの実装

- **目的**: ワークフロー履歴取得機能を実装する
- **対象**: WorkflowResource#getWorkflowHistory()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「5.7 ワークフロー履歴取得」
- **注意事項**: 
  - パスパラメータからworkflowIdを取得
  - WorkflowDaoで指定したworkflowIdの全操作履歴を取得
  - 操作日時の昇順でソート
  - ログ出力（INFO）を行う

---

### T_API006_014: [P] ワークフローAPI単体テストの作成

- **目的**: ワークフローAPIの単体テストを作成する
- **対象**: WorkflowResourceTest.java（テストクラス）
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_006_workflows/functional_design.md) の「12. テスト仕様」
- **注意事項**: 
  - 正常系: ワークフロー作成 → 201 Created
  - 正常系: ワークフロー申請 → 200 OK（CREATED → APPLIED）
  - 正常系: ワークフロー承認 → 200 OK（APPLIED → APPROVED）+ 書籍マスタ反映
  - 正常系: ワークフロー却下 → 200 OK（APPLIED → CREATED）
  - 異常系: 不正な状態遷移 → 400 Bad Request
  - 異常系: 承認権限不足 → 403 Forbidden
  - 異常系: ワークフローが見つからない → 404 Not Found

---

## API実装完了チェックリスト

- [ ] WorkflowCreateRequestが作成された
- [ ] WorkflowTOが作成された
- [ ] WorkflowServiceが作成された
- [ ] WorkflowResourceが作成された
- [ ] createWorkflow()メソッドが実装された
  - [ ] ワークフローID採番
  - [ ] 201 Createdレスポンス
  - [ ] ログ出力が実装された
- [ ] updateWorkflow()メソッドが実装された
  - [ ] 状態チェック（CREATED）
  - [ ] ログ出力が実装された
- [ ] applyWorkflow()メソッドが実装された
  - [ ] 状態遷移（CREATED → APPLIED）
  - [ ] ログ出力が実装された
- [ ] approveWorkflow()メソッドが実装された
  - [ ] **承認権限チェックが実装された**
  - [ ] 状態遷移（APPLIED → APPROVED）
  - [ ] **書籍マスタ反映が実装された**
  - [ ] トランザクション管理
  - [ ] ログ出力が実装された
- [ ] rejectWorkflow()メソッドが実装された
  - [ ] 状態遷移（APPLIED → CREATED）
  - [ ] ログ出力が実装された
- [ ] applyToBookMaster()メソッドが実装された
  - [ ] ADD_NEW_BOOK処理
  - [ ] REMOVE_BOOK処理（論理削除）
  - [ ] ADJUST_BOOK_PRICE処理
- [ ] checkApprovalAuthority()メソッドが実装された
  - [ ] 職務ランクチェック
  - [ ] 部署チェック
- [ ] getWorkflows()メソッドが実装された
  - [ ] フィルタリング機能
  - [ ] ログ出力が実装された
- [ ] getWorkflowHistory()メソッドが実装された
  - [ ] 履歴取得
  - [ ] ログ出力が実装された
- [ ] ワークフローAPI単体テストが作成された
  - [ ] 正常系テストが実装された
  - [ ] 異常系テストが実装された
  - [ ] 承認権限テストが実装された

---

## 次のステップ

ワークフローAPIが完了したら、他のAPI実装タスクと並行して進めることができます：
- [認証API](API_001_auth.md)
- [書籍API](API_002_books.md)
- [カテゴリAPI](API_003_categories.md)
- [出版社API](API_004_publishers.md)
- [在庫API](API_005_stocks.md)

すべてのAPI実装が完了したら、[結合テスト](integration_tasks.md)に進んでください。
