# API_006 ワークフローAPI - 詳細設計書

**API ID**: API_006  
**API名**: ワークフローAPI  
**バージョン**: 1.0.0  
**最終更新**: 2025-01-10

---

## 1. パッケージ構造

```
pro.kensait.backoffice
├── api
│   ├── WorkflowResource.java        # ワークフローリソース
│   ├── dto
│   │   ├── WorkflowTO.java          # ワークフロー転送オブジェクト
│   │   ├── WorkflowCreateRequest.java   # ワークフロー作成リクエスト
│   │   └── WorkflowOperationRequest.java  # ワークフロー操作リクエスト
│   └── exception
│       └── WorkflowExceptionMapper.java  # ワークフロー例外マッパー
├── service
│   └── workflow
│       ├── WorkflowService.java     # ワークフローサービス
│       ├── WorkflowType.java        # ワークフロー種別列挙型
│       ├── WorkflowStateType.java   # ワークフロー状態列挙型
│       ├── WorkflowOperationType.java  # ワークフロー操作種別列挙型
│       ├── JobRankType.java         # 職務ランク列挙型
│       ├── WorkflowNotFoundException.java  # ワークフロー未検出例外
│       ├── InvalidWorkflowStateException.java  # 不正状態例外
│       └── UnauthorizedApprovalException.java  # 承認権限不足例外
├── dao
│   ├── WorkflowDao.java             # ワークフローデータアクセス
│   ├── EmployeeDao.java             # 社員データアクセス
│   ├── DepartmentDao.java           # 部署データアクセス
│   └── BookDao.java                 # 書籍データアクセス
└── entity
    ├── Workflow.java                # ワークフローエンティティ
    ├── Employee.java                # 社員エンティティ
    ├── Department.java              # 部署エンティティ
    └── Book.java                    # 書籍エンティティ
```

---

## 2. クラス設計

### 2.1 WorkflowResource（JAX-RS Resource）

**ベースパス**: `/workflows`

**エンドポイント**:
- `GET /workflows` - ワークフロー一覧取得
- `GET /workflows/{workflowId}` - ワークフロー詳細取得
- `POST /workflows` - ワークフロー作成
- `POST /workflows/{workflowId}/approve` - ワークフロー承認
- `POST /workflows/{workflowId}/reject` - ワークフロー却下

### 2.2 WorkflowTO（DTO - Record）

```java
public record WorkflowTO(
    Integer workflowId,
    String workflowType,      // WorkflowType.name()
    String state,             // WorkflowStateType.name()
    Integer bookId,
    String bookName,
    Integer price,
    Integer operatorId,
    String operatorName,
    Integer approverId,
    String approverName,
    LocalDateTime operatedAt,
    String operationType      // WorkflowOperationType.name()
) {}
```

### 2.3 WorkflowService

**主要メソッド**:
- `List<WorkflowTO> getWorkflowsAll()` - 全ワークフロー取得
- `WorkflowTO getWorkflowById(Integer workflowId)` - ワークフロー詳細取得
- `WorkflowTO createWorkflow(WorkflowCreateRequest request, Integer employeeId)` - ワークフロー作成
- `WorkflowTO approveWorkflow(Integer workflowId, Integer employeeId)` - ワークフロー承認
- `WorkflowTO rejectWorkflow(Integer workflowId, Integer employeeId)` - ワークフロー却下

**ビジネスルール**:
1. **状態遷移**: APPLIED → APPROVED/REJECTED
2. **承認権限チェック**:
   - MANAGER以上の職務ランクが必要
   - DIRECTORは全部署のワークフローを承認可能
   - MANAGERは同一部署のワークフローのみ承認可能
3. **書籍マスタ反映**:
   - APPROVED時に書籍マスタに反映
   - ADD_NEW_BOOK: 書籍INSERT
   - REMOVE_BOOK: 書籍論理削除
   - ADJUST_BOOK_PRICE: 書籍価格UPDATE

### 2.4 Workflow（エンティティ）

**テーブル**: `WORKFLOW`

| フィールド名 | 型 | カラム名 | 制約 | 説明 |
|------------|---|---------|-----|------|
| `workflowId` | `Integer` | `WORKFLOW_ID` | `@Id @GeneratedValue` | ワークフローID |
| `workflowType` | `WorkflowType` | `WORKFLOW_TYPE` | `@Enumerated(STRING)` | ワークフロー種別 |
| `state` | `WorkflowStateType` | `STATE` | `@Enumerated(STRING)` | 状態 |
| `bookId` | `Integer` | `BOOK_ID` | | 対象書籍ID |
| `bookName` | `String` | `BOOK_NAME` | | 書籍名 |
| `price` | `Integer` | `PRICE` | | 価格 |
| `operator` | `Employee` | `OPERATOR_ID` | `@ManyToOne` | 操作者 |
| `approver` | `Employee` | `APPROVER_ID` | `@ManyToOne` | 承認者 |
| `operatedAt` | `LocalDateTime` | `OPERATED_AT` | | 操作日時 |
| `operationType` | `WorkflowOperationType` | `OPERATION_TYPE` | `@Enumerated(STRING)` | 操作種別 |

---

## 3. 列挙型定義

### 3.1 WorkflowType（ワークフロー種別）

```java
public enum WorkflowType {
    ADD_NEW_BOOK,      // 新規書籍追加
    REMOVE_BOOK,       // 書籍削除
    ADJUST_BOOK_PRICE  // 書籍価格調整
}
```

### 3.2 WorkflowStateType（ワークフロー状態）

```java
public enum WorkflowStateType {
    APPLIED,   // 申請済み
    APPROVED,  // 承認済み
    REJECTED   // 却下済み
}
```

### 3.3 WorkflowOperationType（ワークフロー操作種別）

```java
public enum WorkflowOperationType {
    APPLY,    // 申請
    APPROVE,  // 承認
    REJECT    // 却下
}
```

### 3.4 JobRankType（職務ランク）

```java
public enum JobRankType {
    ASSOCIATE(1),  // 一般社員
    MANAGER(2),    // マネージャー
    DIRECTOR(3);   // ディレクター
    
    private final int rank;
}
```

---

## 4. ビジネスロジック詳細

### 4.1 承認権限チェック

**条件**:
1. 承認者の職務ランクがMANAGER以上
2. DIRECTORの場合: 全部署のワークフローを承認可能
3. MANAGERの場合: 申請者と同一部署のワークフローのみ承認可能

**例外**:
- `UnauthorizedApprovalException` - 承認権限不足

### 4.2 書籍マスタ反映

**ADD_NEW_BOOK**:
```java
Book newBook = new Book();
newBook.setBookName(workflow.getBookName());
newBook.setPrice(workflow.getPrice());
// ... その他のフィールド設定
bookDao.insert(newBook);
```

**REMOVE_BOOK**:
```java
Book book = bookDao.findById(workflow.getBookId());
book.setDeleted(true);  // 論理削除
bookDao.update(book);
```

**ADJUST_BOOK_PRICE**:
```java
Book book = bookDao.findById(workflow.getBookId());
book.setPrice(workflow.getPrice());
bookDao.update(book);
```

---

## 5. エラーハンドリング

### 5.1 例外マッピング

| Exception | HTTPステータス | メッセージ |
|-----------|--------------|----------|
| `WorkflowNotFoundException` | 404 Not Found | "ワークフローが見つかりません" |
| `InvalidWorkflowStateException` | 400 Bad Request | "ワークフローの状態が不正です" |
| `UnauthorizedApprovalException` | 403 Forbidden | "承認権限がありません" |

---

## 6. テスト要件

### 6.1 ユニットテスト

**対象**: `WorkflowService`

- ワークフロー作成テスト
- ワークフロー承認テスト（DIRECTOR）
- ワークフロー承認テスト（MANAGER・同一部署）
- ワークフロー承認テスト（MANAGER・異なる部署→例外）
- ワークフロー承認テスト（ASSOCIATE→例外）
- 書籍マスタ反映テスト（ADD_NEW_BOOK）
- 書籍マスタ反映テスト（REMOVE_BOOK）
- 書籍マスタ反映テスト（ADJUST_BOOK_PRICE）

---

## 7. 参考資料

- [functional_design.md](functional_design.md) - 機能設計書
- [behaviors.md](behaviors.md) - 振る舞い仕様書
