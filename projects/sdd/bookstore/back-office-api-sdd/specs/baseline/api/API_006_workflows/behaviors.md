# ワークフローAPI 振る舞い仕様書

## 1. 概要

本ドキュメントは、ワークフローAPI（`/api/workflows`）の動的な振る舞いをシーケンス図、状態遷移図、フローチャートで定義する。ワークフローAPIは、書籍マスタの変更を申請・承認制御するための複雑なビジネスロジックを含む。

## 2. ワークフロー作成シーケンス

### 2.1 正常系: 新規書籍追加ワークフロー作成

```
┌──────┐         ┌────────────┐      ┌──────────────┐      ┌───────────┐      ┌────────┐
│Client│         │WorkflowResource     │WorkflowService      │WorkflowDao│      │Database│
└──┬───┘         └──────┬─────┘      └───────┬──────┘      └─────┬─────┘      └───┬────┘
   │                     │                     │                    │               │
   │POST /api/workflows  │                     │                    │               │
   │{                    │                     │                    │               │
   │ workflowType:       │                     │                    │               │
   │  "ADD_NEW_BOOK",    │                     │                    │               │
   │ bookData: {...},    │                     │                    │               │
   │ operatedBy: 1       │                     │                    │               │
   │}                    │                     │                    │               │
   ├────────────────────►│                     │                    │               │
   │                     │                     │                    │               │
   │                     │LOG INFO             │                    │               │
   │                     │──────────┐          │                    │               │
   │                     │          │          │                    │               │
   │                     │◄─────────┘          │                    │               │
   │                     │                     │                    │               │
   │                     │createWorkflow()     │                    │               │
   │                     ├────────────────────►│                    │               │
   │                     │                     │                    │               │
   │                     │                     │Generate workflowId │               │
   │                     │                     │──────────┐         │               │
   │                     │                     │          │         │               │
   │                     │                     │UUID.random│         │               │
   │                     │                     │◄─────────┘         │               │
   │                     │                     │                    │               │
   │                     │                     │Build Workflow      │               │
   │                     │                     │entity              │               │
   │                     │                     │──────────┐         │               │
   │                     │                     │          │         │               │
   │                     │                     │- workflowId        │               │
   │                     │                     │- workflowType      │               │
   │                     │                     │- state=CREATED     │               │
   │                     │                     │- operation=CREATE  │               │
   │                     │                     │- bookData (JSON)   │               │
   │                     │                     │- operatedBy        │               │
   │                     │                     │- timestamp         │               │
   │                     │                     │◄─────────┘         │               │
   │                     │                     │                    │               │
   │                     │                     │insert()            │               │
   │                     │                     ├───────────────────►│               │
   │                     │                     │                    │INSERT INTO    │
   │                     │                     │                    │ WORKFLOW      │
   │                     │                     │                    │VALUES(...)    │
   │                     │                     │                    ├──────────────►│
   │                     │                     │                    │◄──────────────┤
   │                     │                     │◄───────────────────┤               │
   │                     │                     │Workflow entity     │               │
   │                     │◄────────────────────┤                    │               │
   │                     │Workflow entity      │                    │               │
   │                     │                     │                    │               │
   │                     │Convert to DTO       │                    │               │
   │                     │──────────┐          │                    │               │
   │                     │          │          │                    │               │
   │                     │◄─────────┘          │                    │               │
   │                     │                     │                    │               │
   │◄────────────────────┤201 Created          │                    │               │
   │{WorkflowTO}         │Location:            │                    │               │
   │                     │ /workflows/         │                    │               │
   │                     │  {workflowId}       │                    │               │
   │                     │                     │                    │               │
```

## 3. ワークフロー更新シーケンス

### 3.1 正常系: CREATED状態のワークフローを更新

```
┌──────┐         ┌────────────┐      ┌──────────────┐      ┌───────────┐      ┌────────┐
│Client│         │WorkflowResource     │WorkflowService      │WorkflowDao│      │Database│
└──┬───┘         └──────┬─────┘      └───────┬──────┘      └─────┬─────┘      └───┬────┘
   │                     │                     │                    │               │
   │PUT /api/workflows/  │                     │                    │               │
   │  {workflowId}       │                     │                    │               │
   │{                    │                     │                    │               │
   │ bookData: {...},    │                     │                    │               │
   │ operatedBy: 1       │                     │                    │               │
   │}                    │                     │                    │               │
   ├────────────────────►│                     │                    │               │
   │                     │                     │                    │               │
   │                     │updateWorkflow()     │                    │               │
   │                     ├────────────────────►│                    │               │
   │                     │                     │                    │               │
   │                     │                     │findLatestState()   │               │
   │                     │                     ├───────────────────►│               │
   │                     │                     │                    │SELECT FROM    │
   │                     │                     │                    │ WORKFLOW      │
   │                     │                     │                    │WHERE          │
   │                     │                     │                    │ workflow_id=  │
   │                     │                     │                    │ORDER BY       │
   │                     │                     │                    │ operation_id  │
   │                     │                     │                    │ DESC LIMIT 1  │
   │                     │                     │                    ├──────────────►│
   │                     │                     │                    │◄──────────────┤
   │                     │                     │                    │Workflow       │
   │                     │                     │◄───────────────────┤ (state=CREATED)│
   │                     │                     │Workflow entity     │               │
   │                     │                     │                    │               │
   │                     │                     │Check state         │               │
   │                     │                     │──────────┐         │               │
   │                     │                     │          │         │               │
   │                     │                     │state==CREATED?     │               │
   │                     │                     │◄─────────┘         │               │
   │                     │                     │Yes ✓               │               │
   │                     │                     │                    │               │
   │                     │                     │Build new Workflow  │               │
   │                     │                     │with operation=UPDATE│              │
   │                     │                     │──────────┐         │               │
   │                     │                     │          │         │               │
   │                     │                     │◄─────────┘         │               │
   │                     │                     │                    │               │
   │                     │                     │insert()            │               │
   │                     │                     ├───────────────────►│               │
   │                     │                     │                    │INSERT INTO    │
   │                     │                     │                    │ WORKFLOW      │
   │                     │                     │                    │(operation=    │
   │                     │                     │                    │ UPDATE)       │
   │                     │                     │                    ├──────────────►│
   │                     │                     │                    │◄──────────────┤
   │                     │                     │◄───────────────────┤               │
   │                     │◄────────────────────┤                    │               │
   │                     │                     │                    │               │
   │◄────────────────────┤200 OK               │                    │               │
   │{WorkflowTO}         │                     │                    │               │
   │                     │                     │                    │               │
```

## 4. ワークフロー申請シーケンス

### 4.1 正常系: CREATED → APPLIED

```
┌──────┐         ┌────────────┐      ┌──────────────┐      ┌───────────┐      ┌────────┐
│Client│         │WorkflowResource     │WorkflowService      │WorkflowDao│      │Database│
└──┬───┘         └──────┬─────┘      └───────┬──────┘      └─────┬─────┘      └───┬────┘
   │                     │                     │                    │               │
   │POST /api/workflows/ │                     │                    │               │
   │  {workflowId}/apply │                     │                    │               │
   │{                    │                     │                    │               │
   │ operatedBy: 1       │                     │                    │               │
   │}                    │                     │                    │               │
   ├────────────────────►│                     │                    │               │
   │                     │                     │                    │               │
   │                     │applyWorkflow()      │                    │               │
   │                     ├────────────────────►│                    │               │
   │                     │                     │                    │               │
   │                     │                     │findLatestState()   │               │
   │                     │                     ├───────────────────►│               │
   │                     │                     │◄───────────────────┤               │
   │                     │                     │Workflow            │               │
   │                     │                     │ (state=CREATED)    │               │
   │                     │                     │                    │               │
   │                     │                     │Check state         │               │
   │                     │                     │──────────┐         │               │
   │                     │                     │          │         │               │
   │                     │                     │state==CREATED?     │               │
   │                     │                     │◄─────────┘         │               │
   │                     │                     │Yes ✓               │               │
   │                     │                     │                    │               │
   │                     │                     │Build new Workflow  │               │
   │                     │                     │entity              │               │
   │                     │                     │──────────┐         │               │
   │                     │                     │          │         │               │
   │                     │                     │- same workflowId   │               │
   │                     │                     │- state=APPLIED     │               │
   │                     │                     │- operation=APPLY   │               │
   │                     │                     │- operatedBy        │               │
   │                     │                     │- timestamp=now()   │               │
   │                     │                     │◄─────────┘         │               │
   │                     │                     │                    │               │
   │                     │                     │insert()            │               │
   │                     │                     ├───────────────────►│               │
   │                     │                     │                    │INSERT         │
   │                     │                     │                    ├──────────────►│
   │                     │                     │                    │◄──────────────┤
   │                     │                     │◄───────────────────┤               │
   │                     │◄────────────────────┤                    │               │
   │                     │                     │                    │               │
   │◄────────────────────┤200 OK               │                    │               │
   │{WorkflowTO}         │                     │                    │               │
   │ state: "APPLIED"    │                     │                    │               │
   │                     │                     │                    │               │
```

## 5. ワークフロー承認シーケンス

```
承認者  WorkflowResource  WorkflowService  WorkflowDao  EmployeeDao  BookDao  Database
  │          │                │               │            │           │          │
  ├─POST /approve            │               │            │           │          │
  │  {operatedBy:2}          │               │            │           │          │
  │          ├─approveWorkflow()             │            │           │          │
  │          │                ├─findLatest()─►│            │           │          │
  │          │                │               ├─SELECT───────────────────────────►│
  │          │                │◄─Workflow────┤  (state=APPLIED)                  │
  │          │                │               │            │           │          │
  │          │                ├─checkApprovalAuthority()   │           │          │
  │          │                │               │            │           │          │
  │          │                ├─findById()───────────────►│           │          │
  │          │                │◄─Employee────────────────┤            │          │
  │          │                │  (jobRank=2)              │            │          │
  │          │                ├─jobRank check             │            │          │
  │          │                ├─department check          │            │          │
  │          │                │               │            │           │          │
  │          │                ├─insert()─────►│            │           │          │
  │          │                │               ├─INSERT WORKFLOW───────────────────►│
  │          │                │               │  (new operation history)          │
  │          │                │               │            │           │          │
  │          │                ├─applyToBookMaster()        │           │          │
  │          │                │               │            ├─persist()─►│          │
  │          │                │               │            │  (new Book├─INSERT──►│
  │          │                │               │            │            │  BOOK+  │
  │          │                │               │            │            │  STOCK  │
  │          │                │               │            │            │          │
  │          │                ├──────Commit Transaction────────────────────────────►│
  │          │                │               │            │            │  (両方成功)│
  │          │                │               │            │            │          │
  │          │◄─WorkflowTO────┤               │            │            │          │
  │◄─200 OK─┤                │               │            │            │          │
  │{workflow}│                │               │            │            │          │
```

### 5.2 承認権限チェックの詳細

```
WorkflowService    EmployeeDao    Database
       │                │             │
       ├─checkApprovalAuthority()    │
       │  (workflowId, approverId)   │
       │                │             │
       ├─findById()────►│             │
       │  (approverId)  ├─SELECT─────►│
       │                │  (EMPLOYEE) │
       │◄─Approver─────┤◄────────────┤
       │  (jobRank=2)   │             │
       │                │             │
       ├─JobRankType.fromRank()      │
       │  → MANAGER     │             │
       │                │             │
       ├─isAtLeast(MANAGER)?         │
       │  → true        │             │
       │                │             │
       └─Authorization OK             │
```

## 6. ワークフロー却下シーケンス

### 6.1 正常系: APPLIED → CREATED (却下)

```
┌──────┐         ┌────────────┐      ┌──────────────┐      ┌───────────┐      ┌────────┐
│Client│         │WorkflowResource     │WorkflowService      │WorkflowDao│      │Database│
└──┬───┘         └──────┬─────┘      └───────┬──────┘      └─────┬─────┘      └───┬────┘
   │                     │                     │                    │               │
   │POST /api/workflows/ │                     │                    │               │
   │  {workflowId}/      │                     │                    │               │
   │  reject             │                     │                    │               │
   │{                    │                     │                    │               │
   │ operatedBy: 2,      │                     │                    │               │
   │ reason: "要再検討"   │                     │                    │               │
   │}                    │                     │                    │               │
   ├────────────────────►│                     │                    │               │
   │                     │                     │                    │               │
   │                     │LOG INFO             │                    │               │
   │                     │──────────┐          │                    │               │
   │                     │          │          │                    │               │
   │                     │◄─────────┘          │                    │               │
   │                     │                     │                    │               │
   │                     │rejectWorkflow()     │                    │               │
   │                     ├────────────────────►│                    │               │
   │                     │                     │                    │               │
   │                     │                     │findLatestState()   │               │
   │                     │                     ├───────────────────►│               │
   │                     │                     │                    │SELECT         │
   │                     │                     │                    ├──────────────►│
   │                     │                     │                    │◄──────────────┤
   │                     │                     │◄───────────────────┤               │
   │                     │                     │Workflow            │               │
   │                     │                     │ (state=APPLIED)    │               │
   │                     │                     │                    │               │
   │                     │                     │Check state         │               │
   │                     │                     │──────────┐         │               │
   │                     │                     │          │         │               │
   │                     │                     │state==APPLIED?     │               │
   │                     │                     │◄─────────┘         │               │
   │                     │                     │Yes ✓               │               │
   │                     │                     │                    │               │
   │                     │                     │Check Authority     │               │
   │                     │                     │──────────┐         │               │
   │                     │                     │          │         │               │
   │                     │                     │checkApprovalAuthority()            │
   │                     │                     │◄─────────┘         │               │
   │                     │                     │OK ✓                │               │
   │                     │                     │                    │               │
   │                     │                     │Build new Workflow  │               │
   │                     │                     │entity              │               │
   │                     │                     │──────────┐         │               │
   │                     │                     │          │         │               │
   │                     │                     │- same workflowId   │               │
   │                     │                     │- state=CREATED     │               │
   │                     │                     │- operation=REJECT  │               │
   │                     │                     │- operatedBy: 2     │               │
   │                     │                     │- reason: "要再検討" │               │
   │                     │                     │- timestamp=now()   │               │
   │                     │                     │◄─────────┘         │               │
   │                     │                     │                    │               │
   │                     │                     │insert()            │               │
   │                     │                     ├───────────────────►│               │
   │                     │                     │                    │INSERT         │
   │                     │                     │                    ├──────────────►│
   │                     │                     │                    │◄──────────────┤
   │                     │                     │◄───────────────────┤               │
   │                     │◄────────────────────┤                    │               │
   │                     │                     │                    │               │
   │◄────────────────────┤200 OK               │                    │               │
   │{WorkflowTO}         │                     │                    │               │
   │ state: "CREATED"    │                     │                    │               │
   │                     │                     │                    │               │
```

## 7. ワークフロー履歴取得シーケンス

### 7.1 特定ワークフローの履歴取得

```
┌──────┐         ┌────────────┐      ┌──────────────┐      ┌───────────┐      ┌────────┐
│Client│         │WorkflowResource     │WorkflowService      │WorkflowDao│      │Database│
└──┬───┘         └──────┬─────┘      └───────┬──────┘      └─────┬─────┘      └───┬────┘
   │                     │                     │                    │               │
   │GET /api/workflows/  │                     │                    │               │
   │  {workflowId}/      │                     │                    │               │
   │  history            │                     │                    │               │
   ├────────────────────►│                     │                    │               │
   │                     │                     │                    │               │
   │                     │getWorkflowHistory() │                    │               │
   │                     ├────────────────────►│                    │               │
   │                     │                     │                    │               │
   │                     │                     │findByWorkflowId()  │               │
   │                     │                     ├───────────────────►│               │
   │                     │                     │                    │SELECT *       │
   │                     │                     │                    │FROM WORKFLOW  │
   │                     │                     │                    │WHERE          │
   │                     │                     │                    │ workflow_id=  │
   │                     │                     │                    │  :workflowId  │
   │                     │                     │                    │ORDER BY       │
   │                     │                     │                    │ operation_id  │
   │                     │                     │                    ├──────────────►│
   │                     │                     │                    │◄──────────────┤
   │                     │                     │                    │List<Workflow> │
   │                     │                     │                    │ [CREATE,      │
   │                     │                     │                    │  APPLY,       │
   │                     │                     │                    │  APPROVE]     │
   │                     │                     │◄───────────────────┤               │
   │                     │                     │List<Workflow>      │               │
   │                     │◄────────────────────┤                    │               │
   │                     │List<Workflow>       │                    │               │
   │                     │                     │                    │               │
   │                     │Convert to DTOs      │                    │               │
   │                     │──────────┐          │                    │               │
   │                     │          │          │                    │               │
   │                     │◄─────────┘          │                    │               │
   │                     │                     │                    │               │
   │◄────────────────────┤200 OK               │                    │               │
   │[{                   │                     │                    │               │
   │  operationId: 1,    │                     │                    │               │
   │  operation: "CREATE",│                    │                    │               │
   │  state: "CREATED",  │                     │                    │               │
   │  timestamp: "...",  │                     │                    │               │
   │  operatedBy: 1      │                     │                    │               │
   │},{                  │                     │                    │               │
   │  operationId: 2,    │                     │                    │               │
   │  operation: "APPLY",│                     │                    │               │
   │  state: "APPLIED",  │                     │                    │               │
   │  timestamp: "...",  │                     │                    │               │
   │  operatedBy: 1      │                     │                    │               │
   │},{                  │                     │                    │               │
   │  operationId: 3,    │                     │                    │               │
   │  operation: "APPROVE",                    │                    │               │
   │  state: "APPROVED", │                     │                    │               │
   │  timestamp: "...",  │                     │                    │               │
   │  operatedBy: 2      │                     │                    │               │
   │}]                   │                     │                    │               │
   │                     │                     │                    │               │
```

## 8. 承認権限チェックシーケンス

```
WorkflowService    EmployeeDao    WorkflowDao    Database
       │                │              │             │
       ├─checkApprovalAuthority()     │             │
       │  (workflowId, approverId)    │             │
       │                │              │             │
       ├─findById()────►│              │             │
       │  (approverId)  │              │             │
       │                ├─SELECT──────────────────►│
       │◄─Approver─────┤              │  (EMPLOYEE)│
       │  (jobRank=2)   │              │             │
       │                │              │             │
       ├─JobRankType.fromRank()       │             │
       │  → MANAGER     │              │             │
       │                │              │             │
       ├─isAtLeast(MANAGER)?          │             │
       │  → true        │              │             │
       │                │              │             │
       ├─DIRECTOR?      │              │             │
       │  → false       │              │             │
       │                │              │             │
       ├─findByWorkflowId()───────────►│             │
       │                │              ├─SELECT──────►│
       │◄─History──────────────────────┤  (WORKFLOW) │
       │  [CREATE op]   │              │             │
       │                │              │             │
       ├─findById()────►│              │             │
       │  (requesterあId)│              │             │
       │◄─Requester────┤              │             │
       │  (deptId=1)    │              │             │
       │                │              │             │
       ├─same dept?     │              │             │
       │  (1 == 1) ✓    │              │             │
       │                │              │             │
       └─OK             │              │             │
```

## 3. ワークフロー状態遷移図

```
         ┌─────────────┐
    ────►│   CREATED   │◄──────┐
         │  (作成済み)   │       │
         └──────┬──────┘       │
                │               │
                │ apply()       │ reject()
                │               │
         ┌──────▼──────┐       │
         │   APPLIED   │───────┘
         │  (申請中)     │
         └──────┬──────┘
                │
                │ approve()
                │
         ┌──────▼──────┐
         │  APPROVED   │
         │  (承認済み)   │
         └─────────────┘
```

## 4. 承認権限フローチャート

```
┌──────────────┐
│承認者を取得    │
└──────┬───────┘
       │
       ▼
┌──────────────┐      No
│職務ランク      ├──────────► 403 Forbidden
│MANAGER以上?   │             "承認権限がありません"
└──────┬───────┘
       │Yes
       ▼
┌──────────────┐      Yes
│DIRECTOR?     ├──────────► 承認OK
│              │             (全部署OK)
└──────┬───────┘
       │No
       ▼
┌──────────────┐
│申請者を取得    │
│(CREATE操作者) │
└──────┬───────┘
       │
       ▼
┌──────────────┐      Yes
│同一部署?      ├──────────► 承認OK
│              │             (MANAGER)
└──────┬───────┘
       │No
       ▼
┌──────────────┐
│403 Forbidden │
│"部署が異なる" │
└──────────────┘
```

## 5. トランザクション振る舞い

### 5.1 成功時

```
┌─────────────────────────────────────┐
│ Transaction Scope                   │
│                                     │
│  1. INSERT WORKFLOW                 │
│     (operation_id, state=APPROVED)  │
│                                     │
│  2. INSERT/UPDATE BOOK              │
│     (書籍マスタ反映)                  │
│                                     │
│  3. COMMIT ✓                        │
│                                     │
└─────────────────────────────────────┘
```

### 5.2 失敗時（書籍マスタ反映でエラー）

```
┌─────────────────────────────────────┐
│ Transaction Scope                   │
│                                     │
│  1. INSERT WORKFLOW                 │
│     (operation_id, state=APPROVED)  │
│                                     │
│  2. INSERT/UPDATE BOOK              │
│     → Exception ✗                   │
│                                     │
│  3. ROLLBACK                        │
│     (ワークフロー履歴も保存されない)   │
│                                     │
└─────────────────────────────────────┘
```

## 6. 並行処理シナリオ

### 6.1 同じワークフローに対する並行承認（発生しない）

```
時刻   承認者A                    承認者B
t1    GET /workflows?state=APPLIED
      → ワークフローID=1表示
t2                                GET /workflows?state=APPLIED
                                  → ワークフローID=1表示
t3    POST /approve (workflowId=1)
t4    → 成功 (state→APPROVED)
t5                                POST /approve (workflowId=1)
t6                                → 400 Bad Request
                                  "承認できる状態ではありません"
                                  (stateがAPPLIEDでない)
```

**理由**: 最新の状態を取得して状態チェックするため、並行承認は防止される。

## 7. 書籍マスタ反映フローチャート

```
┌──────────────┐
│workflowType?  │
└┬────┬────┬──┘
 │    │    │
ADD  REMOVE ADJUST
NEW   BOOK  PRICE
BOOK  │    │
 │    │    │
 ▼    ▼    ▼
┌──┐┌──┐┌──┐
│新││論││価│
│規││理││格│
│書││削││更│
│籍││除││新│
│+││(││(│
│在││DE││UP│
│庫││LE││DA│
│IN││TE││TE│
│SE││D=││)│
│RT││tr││ │
│)││ue││ │
│ ││)││ │
└┬─┘└┬─┘└┬─┘
 │   │   │
 └───┴───┘
     │
     ▼
┌──────────────┐
│COMMIT        │
└──────────────┘
```

## 8. ログ出力シーケンス

### 8.1 承認成功時

```
Time    Level   Message
────────────────────────────────────────────────────────────────
t1      INFO    [ WorkflowResource#approveWorkflow ] workflowId=1
t2      INFO    [ WorkflowService#approveWorkflow ] workflowId=1
t3      INFO    [ WorkflowService#checkApprovalAuthority ] workflowId=1, approverId=2
t4      INFO    [ WorkflowService#applyToBookMaster ] workflowType=ADD_NEW_BOOK
t5      INFO    新規書籍と在庫情報を追加しました: bookId=10, bookName=xxx, quantity=0
```

### 8.2 承認失敗時（権限不足）

```
Time    Level   Message
────────────────────────────────────────────────────────────────
t1      INFO    [ WorkflowResource#approveWorkflow ] workflowId=1
t2      INFO    [ WorkflowService#approveWorkflow ] workflowId=1
t3      INFO    [ WorkflowService#checkApprovalAuthority ] workflowId=1, approverId=1
t4      ERROR   ワークフロー承認エラー
                pro.kensait.backoffice.service.workflow.UnauthorizedApprovalException:
                承認権限がありません。MANAGER以上の職務ランクが必要です。
```

## 9. パフォーマンス特性

```
承認処理の内訳 (Total: ~1000ms)

┌──────────────────────────────────────────────────────────┐
│ DB Query (最新状態取得)                    100ms (10%)    │
├──────────────────────────────────────────────────────────┤
│ DB Query (承認者取得)                      100ms (10%)    │
├──────────────────────────────────────────────────────────┤
│ DB Query (申請者取得)                      100ms (10%)    │
├──────────────────────────────────────────────────────────┤
│ DB INSERT (ワークフロー履歴)               200ms (20%)    │
├──────────────────────────────────────────────────────────┤
│ DB INSERT/UPDATE (書籍マスタ反映)         300ms (30%)    │
├──────────────────────────────────────────────────────────┤
│ その他（ビジネスロジック、変換）            200ms (20%)    │
└──────────────────────────────────────────────────────────┘
```

**ボトルネック**: 書籍マスタへの反映処理（トランザクション内でのINSERT/UPDATE）

