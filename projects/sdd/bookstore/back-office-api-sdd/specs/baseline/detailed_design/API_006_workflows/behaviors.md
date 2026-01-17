# API_006_workflows - ワークフローAPI受入基準

* API ID: API_006_workflows  
* API名: ワークフローAPI  
* ベースパス:/api/workflows`  
* バージョン: 2.0.0  
* 最終更新日: 2025-01-02

---

## 1. 概要

本文書は、ワークフローAPIの受入基準を記述する。書籍マスタ変更の申請・承認フローに関するテストシナリオを詳細に定義する。

---

## 2. ワークフロー作成 (`POS/api/workflows`)

### 2.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| WF-CREATE-001 | 新規書籍追加ワークフローを作成できる | - | POS/api/workflows<br/>{workflowType: "ADD_NEW_BOOK", ...} | 201 Created<br/>state: "CREATED"<br/>operationType: "CREATE" |
| WF-CREATE-002 | 既存書籍削除ワークフローを作成できる | - | POS/api/workflows<br/>{workflowType: "REMOVE_BOOK", bookId: 1} | 201 Created<br/>state: "CREATED" |
| WF-CREATE-003 | 価格改定ワークフローを作成できる | - | POS/api/workflows<br/>{workflowType: "ADJUST_BOOK_PRICE", ...} | 201 Created<br/>state: "CREATED" |
| WF-CREATE-004 | 作成者情報が記録される | createdBy=1 | ワークフロー作成 | 201 Created<br/>operatedBy: 1が記録される |

### 2.2 異常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| WF-CREATE-E001 | workflowTypeが指定されていない | - | POS/api/workflows<br/>{createdBy: 1} | 400 Bad Request<br/>Bean Validationエラー |
| WF-CREATE-E002 | createdByが指定されていない | - | POS/api/workflows<br/>{workflowType: "ADD_NEW_BOOK"} | 400 Bad Request<br/>Bean Validationエラー |

---

## 3. ワークフロー更新 (`PU/api/workflows/{workflowId}`)

### 3.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| WF-UPDATE-001 | CREATED状態のワークフローを更新できる | workflowId=1, state=CREATED | PU/api/workflows/1<br/>{updatedBy: 1, bookName: "更新"} | 200 OK<br/>state: "CREATED"<br/>内容が更新される |

### 3.2 異常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| WF-UPDATE-E001 | APPLIED状態では更新できない | workflowId=1, state=APPLIED | PU/api/workflows/1 | 400 Bad Request<br/>"現在の状態では操作できません" |
| WF-UPDATE-E002 | APPROVED状態では更新できない | workflowId=1, state=APPROVED | PU/api/workflows/1 | 400 Bad Request<br/>"現在の状態では操作できません" |

---

## 4. ワークフロー申請 (`POS/api/workflows/{workflowId}/apply`)

### 4.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| WF-APPLY-001 | CREATED状態から申請できる | workflowId=1, state=CREATED | POS/api/workflows/1/apply<br/>{operatedBy: 1} | 200 OK<br/>state: "APPLIED"<br/>operationType: "APPLY" |
| WF-APPLY-002 | 申請理由が記録される | state=CREATED | POS/api/workflows/1/apply<br/>{operationReason: "申請します"} | 200 OK<br/>operationReason: "申請します"が記録 |

### 4.2 異常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| WF-APPLY-E001 | APPLIED状態から申請できない | workflowId=1, state=APPLIED | POS/api/workflows/1/apply | 400 Bad Request<br/>"現在の状態では操作できません" |
| WF-APPLY-E002 | APPROVED状態から申請できない | workflowId=1, state=APPROVED | POS/api/workflows/1/apply | 400 Bad Request<br/>"現在の状態では操作できません" |

---

## 5. ワークフロー承認 (`POS/api/workflows/{workflowId}/approve`)

### 5.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| WF-APPROVE-001 | APPLIED状態から承認できる | workflowId=1, state=APPLIED<br/>operatedBy=2 (MANAGER) | POS/api/workflows/1/approve<br/>{operatedBy: 2} | 200 OK<br/>state: "APPROVED"<br/>operationType: "APPROVE" |
| WF-APPROVE-002 | 新規書籍が書籍マスタに反映される | workflowType=ADD_NEW_BOOK | 承認 | 200 OK<br/>BOOKテーブルとSTOCKテーブルにINSERT |
| WF-APPROVE-003 | 書籍削除が反映される | workflowType=REMOVE_BOOK | 承認 | 200 OK<br/>BOOK.DELETED=trueに更新 |
| WF-APPROVE-004 | 価格改定が反映される | workflowType=ADJUST_BOOK_PRICE | 承認 | 200 OK<br/>BOOK.PRICEが更新 |
| WF-APPROVE-005 | DIRECTORは全部署を承認できる | operatedBy=3 (DIRECTOR)<br/>申請者の部署が異なる | 承認 | 200 OK<br/>承認成功 |
| WF-APPROVE-006 | MANAGERは同一部署のみ承認できる | operatedBy=2 (MANAGER)<br/>申請者と同じ部署 | 承認 | 200 OK<br/>承認成功 |

### 5.2 異常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| WF-APPROVE-E001 | CREATED状態から承認できない | workflowId=1, state=CREATED | POS/api/workflows/1/approve | 400 Bad Request<br/>"現在の状態では操作できません" |
| WF-APPROVE-E002 | APPROVED状態から承認できない | workflowId=1, state=APPROVED | POS/api/workflows/1/approve | 400 Bad Request<br/>"現在の状態では操作できません" |
| WF-APPROVE-E003 | ASSOCIATEは承認できない | operatedBy=1 (ASSOCIATE, jobRank=1) | POS/api/workflows/1/approve | 403 Forbidden<br/>"承認権限がありません" |
| WF-APPROVE-E004 | MANAGERは他部署を承認できない | operatedBy=2 (MANAGER, departmentId=1)<br/>申請者 (departmentId=2) | POS/api/workflows/1/approve | 403 Forbidden<br/>"承認権限がありません" |

---

## 6. ワークフロー却下 (`POS/api/workflows/{workflowId}/reject`)

### 6.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| WF-REJECT-001 | APPLIED状態から却下できる | workflowId=1, state=APPLIED | POS/api/workflows/1/reject<br/>{operatedBy: 2} | 200 OK<br/>state: "CREATED"に戻る<br/>operationType: "REJECT" |
| WF-REJECT-002 | 却下理由が記録される | state=APPLIED | POS/api/workflows/1/reject<br/>{operationReason: "内容不備"} | 200 OK<br/>operationReason: "内容不備"が記録 |
| WF-REJECT-003 | 却下後、再申請できる | state=CREATED (却下後) | POS/api/workflows/1/apply | 200 OK<br/>state: "APPLIED"に遷移 |

### 6.2 異常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| WF-REJECT-E001 | CREATED状態から却下できない | workflowId=1, state=CREATED | POS/api/workflows/1/reject | 400 Bad Request<br/>"現在の状態では操作できません" |
| WF-REJECT-E002 | APPROVED状態から却下できない | workflowId=1, state=APPROVED | POS/api/workflows/1/reject | 400 Bad Request<br/>"現在の状態では操作できません" |

---

## 7. ワークフロー一覧取得 (`GE/api/workflows`)

### 7.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| WF-LIST-001 | すべてのワークフローを取得できる | ワークフローが存在 | GE/api/workflows | 200 OK<br/>ワークフロー一覧が返される |
| WF-LIST-002 | 状態で絞り込みできる | state=APPLIEDのワークフローが存在 | GE/api/workflows?state=APPLIED | 200 OK<br/>state=APPLIEDのみ返される |
| WF-LIST-003 | ワークフロータイプで絞り込みできる | - | GE/api/workflows?workflowType=ADD_NEW_BOOK | 200 OK<br/>該当タイプのみ返される |
| WF-LIST-004 | 社員IDで絞り込みできる | - | GE/api/workflows?employeeId=1 | 200 OK<br/>該当社員のワークフローのみ返される |

---

## 8. ワークフロー履歴取得 (`GE/api/workflows/{workflowId}/history`)

### 8.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| WF-HIST-001 | ワークフローの全履歴を取得できる | workflowId=1に3件の操作履歴 | GE/api/workflows/1/history | 200 OK<br/>3件の操作履歴が返される |
| WF-HIST-002 | 履歴は時系列順 | - | GE/api/workflows/1/history | 200 OK<br/>operatedAt昇順で返される |
| WF-HIST-003 | 各履歴に操作者情報が含まれる | - | GE/api/workflows/1/history | 200 OK<br/>operatorName, operatorCode等が含まれる |

---

## 9. 承認権限受入基準

### 9.1 職務ランク

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| AUTH-001 | ASSOCIATE (jobRank=1)は承認不可 | operatedBy=1 (ASSOCIATE) | 承認試行 | 403 Forbidden |
| AUTH-002 | MANAGER (jobRank=2)は同一部署のみ承認可 | operatedBy=2 (MANAGER, dept=1)<br/>申請者 (dept=1) | 承認試行 | 200 OK |
| AUTH-003 | DIRECTOR (jobRank=3)は全部署承認可 | operatedBy=3 (DIRECTOR)<br/>申請者 (任意の部署) | 承認試行 | 200 OK |

---

## 10. 状態遷移受入基準

### 10.1 許可される遷移

| シナリオID | 説明 | 遷移 | 受入基準 |
|-----------|------|------|---------|
| STATE-001 | 作成から申請 | CREATED → APPLIED | 許可 |
| STATE-002 | 申請から承認 | APPLIED → APPROVED | 許可 |
| STATE-003 | 申請から却下 | APPLIED → CREATED | 許可 |

### 10.2 許可されない遷移

| シナリオID | 説明 | 遷移 | 受入基準 |
|-----------|------|------|---------|
| STATE-E001 | 作成から直接承認 | CREATED → APPROVED | 400 Bad Request |
| STATE-E002 | 承認済みから他状態 | APPROVED → ANY | 400 Bad Request |

---

## 11. トランザクション受入基準

### 11.1 トランザクション整合性

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| TRAN-001 | 承認処理は1トランザクション | - | 承認 | WORKFLOW_OPERATION INSERTと<br/>BOOK INSERT/UPDATEが同時にコミット |
| TRAN-002 | エラー時は全てロールバック | BOOK INSERT失敗 | 承認試行 | WORKFLOW_OPERATIONもロールバック |

---

## 12. パフォーマンス受入基準

### 12.1 レスポンスタイム

| シナリオID | API | 受入基準 |
|-----------|-----|---------|
| PERF-001 | POS/api/workflows | 500ms以内（95パーセンタイル） |
| PERF-002 | PU/api/workflows/{id} | 500ms以内（95パーセンタイル） |
| PERF-003 | POS/api/workflows/{id}/apply | 500ms以内（95パーセンタイル） |
| PERF-004 | POS/api/workflows/{id}/approve | 1秒以内（95パーセンタイル） |
| PERF-005 | GE/api/workflows | 500ms以内（95パーセンタイル） |

---

## 13. ログ出力受入基準

### 13.1 ログレベル

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| LOG-001 | ワークフロー作成時はINFOレベル | - | POS/api/workflows | INFO: [WorkflowResource#create] Created workflow: id=1 |
| LOG-002 | 承認成功時はINFOレベル | - | POS/api/workflows/1/approve成功 | INFO: [WorkflowResource#approve] Approved workflow: id=1 |
| LOG-003 | 承認権限不足時はWARNレベル | 権限不足 | 承認試行 | WARN: [WorkflowService#approve] Unauthorized approval attempt |
| LOG-004 | 不正な状態遷移時はWARNレベル | APPROVED状態 | 承認試行 | WARN: [WorkflowService#approve] Invalid state transition |

---

## 14. 関連ドキュメント

* [functional_design.md](functional_design.md) - ワークフローAPI機能設計書
* [../../basic_design/behaviors.md](../../basic_design/behaviors.md) - 全体受入基準
* [../../basic_design/architecture_design.md](../../basic_design/architecture_design.md) - アーキテクチャ設計書
