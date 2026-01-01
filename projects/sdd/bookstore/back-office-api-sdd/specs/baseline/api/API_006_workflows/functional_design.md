# ワークフローAPI 機能設計書

## 1. API概要

- **API名**: ワークフローAPI（Workflows API）
- **ベースパス**: `/api/workflows`
- **目的**: 書籍マスタ変更のワークフロー管理（申請・承認フロー）

## 2. エンドポイント一覧

| メソッド | パス | 機能 |
|---------|------|------|
| POST | `/api/workflows` | ワークフロー作成 |
| PUT | `/api/workflows/{workflowId}` | ワークフロー更新（一時保存） |
| GET | `/api/workflows` | ワークフロー一覧取得 |
| GET | `/api/workflows/{workflowId}/history` | ワークフロー履歴取得 |
| POST | `/api/workflows/{workflowId}/apply` | ワークフロー申請 |
| POST | `/api/workflows/{workflowId}/approve` | ワークフロー承認 |
| POST | `/api/workflows/{workflowId}/reject` | ワークフロー却下 |

## 3. ワークフロータイプ

| タイプ | 説明 | 必須フィールド |
|-------|------|---------------|
| ADD_NEW_BOOK | 新規書籍追加 | bookName, author, price, categoryId, publisherId |
| REMOVE_BOOK | 既存書籍削除 | bookId |
| ADJUST_BOOK_PRICE | 書籍価格改定 | bookId, price, startDate, endDate |

## 4. ワークフロー状態遷移

```
CREATED → APPLIED → APPROVED
       ← REJECTED ←
```

| 状態 | 説明 | 可能な操作 |
|------|------|-----------|
| CREATED | 作成済み | UPDATE, APPLY |
| APPLIED | 申請中 | APPROVE, REJECT |
| APPROVED | 承認済み | なし（終了） |

## 5. エンドポイント詳細

### 5.1 ワークフロー作成

**エンドポイント**: `POST /api/workflows`

**リクエスト（新規書籍追加）**:
```json
{
  "workflowType": "ADD_NEW_BOOK",
  "createdBy": 1,
  "bookName": "新しい書籍",
  "author": "著者名",
  "price": 3000,
  "imageUrl": "http://example.com/new.jpg",
  "categoryId": 1,
  "publisherId": 2,
  "applyReason": "新商品として追加"
}
```

**レスポンス（201 Created）**:
```json
{
  "operationId": 1,
  "workflowId": 1,
  "workflowType": "ADD_NEW_BOOK",
  "state": "CREATED",
  "bookName": "新しい書籍",
  "operationType": "CREATE",
  "operatedBy": 1,
  "operatorName": "山田太郎",
  "operatedAt": "2025-01-01T10:00:00"
}
```

### 5.2 ワークフロー承認

**エンドポイント**: `POST /api/workflows/{workflowId}/approve`

**リクエスト**:
```json
{
  "operatedBy": 2,
  "operationReason": "承認します"
}
```

**処理フロー**:
1. 最新の状態を取得（APPLIEDか？）
2. 承認権限チェック
   - 職務ランク: MANAGER以上
   - 部署: DIRECTORは全部署、MANAGERは同一部署のみ
3. 新しい操作履歴を作成（STATE=APPROVED）
4. 書籍マスタへの反映
   - ADD_NEW_BOOK → Book + Stock INSERT
   - REMOVE_BOOK → Book論理削除（DELETED=true）
   - ADJUST_BOOK_PRICE → Book価格UPDATE
5. トランザクションコミット（ワークフロー + 書籍マスタ）

**レスポンス（200 OK）**: WorkflowTO

**レスポンス（403 Forbidden）**:
```json
{
  "error": "UnauthorizedApprovalException",
  "message": "承認権限がありません"
}
```

## 6. 承認権限ルール

### 6.1 職務ランク

| ランク | 値 | 権限 |
|-------|---|------|
| ASSOCIATE | 1 | 承認不可 |
| MANAGER | 2 | 同一部署のみ承認可 |
| DIRECTOR | 3 | 全部署承認可 |

### 6.2 承認権限チェックフローチャート

```
┌────────────────┐
│承認者の職務ランク│
│取得             │
└───────┬────────┘
        │
        ▼
    ┌──────────────┐
    │MANAGER以上?   │
    └┬──────────┬──┘
     │No        │Yes
     │          │
     ▼          ▼
┌────────┐  ┌──────────────┐
│403     │  │DIRECTOR?     │
│Forbidden  └┬──────────┬──┘
└────────┘   │Yes       │No
             │          │
             ▼          ▼
         ┌──────┐  ┌──────────────┐
         │承認OK │  │同一部署?      │
         └──────┘  └┬──────────┬──┘
                    │Yes       │No
                    │          │
                    ▼          ▼
                ┌──────┐  ┌────────┐
                │承認OK │  │403     │
                └──────┘  │Forbidden
                          └────────┘
```

## 7. ワークフロー履歴

ワークフローの操作履歴は全て保存される（監査目的）:

| 操作 | STATE | OPERATION_TYPE |
|------|-------|---------------|
| 作成 | CREATED | CREATE |
| 更新 | CREATED | CREATE（同じ行を更新） |
| 申請 | APPLIED | APPLY |
| 承認 | APPROVED | APPROVE |
| 却下 | CREATED | REJECT |

**例**:
```
OPERATION_ID  WORKFLOW_ID  STATE     OPERATION_TYPE  OPERATED_AT
1             1            CREATED   CREATE          2025-01-01 10:00
2             1            APPLIED   APPLY           2025-01-01 11:00
3             1            APPROVED  APPROVE         2025-01-01 15:00
```

## 8. 書籍マスタ反映（承認時）

### 8.1 ADD_NEW_BOOK

```sql
-- BOOK INSERT
INSERT INTO BOOK (BOOK_NAME, AUTHOR, PRICE, CATEGORY_ID, PUBLISHER_ID, IMAGE_URL, DELETED)
VALUES (?, ?, ?, ?, ?, ?, false);

-- STOCK INSERT (自動: SecondaryTable)
INSERT INTO STOCK (BOOK_ID, QUANTITY, VERSION)
VALUES (?, 0, 0);
```

### 8.2 REMOVE_BOOK

```sql
UPDATE BOOK 
SET DELETED = true 
WHERE BOOK_ID = ?;
```

### 8.3 ADJUST_BOOK_PRICE

```sql
UPDATE BOOK 
SET PRICE = ? 
WHERE BOOK_ID = ?;
```

## 9. トランザクション管理

承認処理は1トランザクション:
1. ワークフロー履歴INSERT
2. 書籍マスタ更新

どちらか失敗した場合、両方ロールバック。

## 10. DTO

### 10.1 WorkflowCreateRequest

```java
public class WorkflowCreateRequest {
    private String workflowType;
    private Long createdBy;
    private Integer bookId;
    private String bookName;
    private String author;
    private BigDecimal price;
    private String imageUrl;
    private Integer categoryId;
    private Integer publisherId;
    private String applyReason;
    private LocalDate startDate;
    private LocalDate endDate;
}
```

### 10.2 WorkflowTO

```java
public class WorkflowTO {
    private Long operationId;
    private Long workflowId;
    private String workflowType;
    private String state;
    private Integer bookId;
    private String bookName;
    private String operationType;
    private Long operatedBy;
    private String operatorName;
    private LocalDateTime operatedAt;
    // ... その他多数
}
```

## 11. ビジネスルール

| ルールID | ルール内容 |
|---------|-----------|
| BR-WORKFLOW-001 | CREATED状態のみ更新可能 |
| BR-WORKFLOW-002 | CREATED状態から申請可能 |
| BR-WORKFLOW-003 | APPLIED状態から承認・却下可能 |
| BR-WORKFLOW-004 | APPROVED状態は終了（変更不可） |
| BR-WORKFLOW-005 | 却下するとCREATED状態に戻る |
| BR-WORKFLOW-006 | 承認時に書籍マスタに反映 |
| BR-WORKFLOW-007 | すべての操作履歴を保存 |

## 12. エラーハンドリング

| エラー | HTTPステータス | メッセージ |
|-------|---------------|-----------|
| ワークフローが見つからない | 404 Not Found | ワークフローが見つかりません |
| 不正な状態遷移 | 400 Bad Request | 現在の状態では操作できません |
| 承認権限不足 | 403 Forbidden | 承認権限がありません |

## 13. 関連コンポーネント

- `WorkflowResource`
- `WorkflowService`
- `WorkflowDao`
- `EmployeeDao`
- `BookDao`

## 14. パフォーマンス要件

- **ワークフロー一覧取得**: 500ms以内
- **承認処理**: 1秒以内

