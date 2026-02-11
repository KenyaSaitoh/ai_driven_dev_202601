# ユースケース: ワークフロー - 振る舞い仕様書

ユースケースID: workflow  
バージョン: 1.0.0  
最終更新日: 2026-01-31

記法: Gherkin。

---

## 1. 概要

ワークフロー作成・更新・申請・承認・却下・一覧・履歴・書籍反映の振る舞い。

---

## 2. テストシナリオ（Gherkin）

### Feature: ワークフロー作成・申請・承認

```gherkin
Scenario: ワークフローを作成する
  Given DBに以下の社員が存在する:
    テーブル: EMPLOYEE
    件数: 1件
    データセット: /datasets/workflow/initial-employee.xml
    データ:
      | EMPLOYEE_ID | NAME   | ROLE   |
      | 1           | 山田太郎 | STAFF |
  
  And DBのワークフローテーブルは空である:
    テーブル: WORKFLOW
    件数: 0件
  
  When POST /api/workflows にワークフロー内容を送る
  
  Then レスポンスは 201 Created
  
  And DBのワークフローテーブルは以下になる:
    テーブル: WORKFLOW
    件数: 1件（+1件追加）
    データセット: /datasets/workflow/expected-workflow-created.xml
    データ:
      | WORKFLOW_ID | WORKFLOW_TYPE | STATE   | OPERATED_BY |
      | 1           | ADD_NEW_BOOK  | CREATED | 1           |
    検証:
      - WORKFLOW_ID は自動採番される
      - STATE は CREATED で作成される
      - OPERATED_BY は社員ID=1

Scenario: ワークフローを申請する（CREATED → APPLIED）
  Given DBにCREATED状態のワークフローが存在する:
    テーブル: WORKFLOW
    件数: 1件
    データセット: /datasets/workflow/initial-workflow-draft.xml
    データ:
      | WORKFLOW_ID | WORKFLOW_TYPE | STATE   | OPERATED_BY |
      | 1           | ADD_NEW_BOOK  | CREATED | 1           |
  
  When POST /api/workflows/1/apply を送る
  
  Then レスポンスは 200 OK
  
  And DBのワークフローテーブルは以下になる:
    テーブル: WORKFLOW
    件数: 1件（変更なし）
    データセット: /datasets/workflow/expected-workflow-applied.xml
    データ:
      | WORKFLOW_ID | WORKFLOW_TYPE | STATE   | APPLIED_AT |
      | 1           | ADD_NEW_BOOK  | APPLIED | 現在時刻    |
    検証:
      - STATE が CREATED から APPLIED に変更される
      - APPLIED_AT に現在時刻が設定される

Scenario: ワークフローを承認する（APPLIED → APPROVED）
  Given DBにAPPLIED状態のワークフローが存在する:
    テーブル: WORKFLOW
    件数: 1件
    データセット: /datasets/workflow/initial-workflow-applied.xml
    データ:
      | WORKFLOW_ID | STATE   | OPERATED_BY |
      | 1           | APPLIED | 1           |
  
  And DBに管理職社員が存在する:
    テーブル: EMPLOYEE
    データ:
      | EMPLOYEE_ID | NAME   | ROLE    |
      | 2           | 佐藤花子 | MANAGER |
  
  When POST /api/workflows/1/approve を送る
  
  Then レスポンスは 200 OK
  
  And DBのワークフローテーブルは以下になる:
    テーブル: WORKFLOW
    件数: 1件（変更なし）
    データセット: /datasets/workflow/expected-workflow-approved.xml
    データ:
      | WORKFLOW_ID | STATE    | APPROVED_BY | APPROVED_AT |
      | 1           | APPROVED | 2           | 現在時刻     |
    検証:
      - STATE が APPLIED から APPROVED に変更される
      - APPROVED_BY に承認者ID=2 が設定される
      - APPROVED_AT に現在時刻が設定される
  
  And 書籍マスタに反映される（該当する場合）:
    テーブル: BOOK
    検証:
      - ワークフローの内容に基づき書籍マスタが更新される

Scenario: 権限のない社員が承認しようとする
  Given DBにAPPLIED状態のワークフローが存在する:
    テーブル: WORKFLOW
    件数: 1件
    データ:
      | WORKFLOW_ID | STATE   |
      | 1           | APPLIED |
  
  And DBに一般社員が存在する:
    テーブル: EMPLOYEE
    データ:
      | EMPLOYEE_ID | NAME   | ROLE  |
      | 1           | 山田太郎 | STAFF |
  
  When POST /api/workflows/1/approve を送る
  
  Then レスポンスは 403 Forbidden
  
  And DBのワークフローテーブルは変化しない:
    テーブル: WORKFLOW
    件数: 1件（変更なし）
    データ:
      | WORKFLOW_ID | STATE   |
      | 1           | APPLIED |
    検証:
      - STATE は APPLIED のまま変化しない
      - 権限エラーのためロールバックされる
```

### Feature: ワークフロー一覧・履歴

```gherkin
Scenario: ワークフロー一覧を取得する
  Given DBに以下のワークフローが存在する:
    テーブル: WORKFLOW
    件数: 3件
    データセット: /datasets/workflow/initial-workflows.xml
    データ:
      | WORKFLOW_ID | WORKFLOW_TYPE | STATE    | OPERATED_BY |
      | 1           | ADD_NEW_BOOK  | CREATED  | 1           |
      | 2           | UPDATE_BOOK   | APPLIED  | 1           |
      | 3           | DELETE_BOOK   | APPROVED | 2           |
  
  When GET /api/workflows を送る
  
  Then レスポンスは 200 OK
  And ワークフロー一覧が返る:
    件数: 3件
  
  And DBの状態は変化しない:
    テーブル: WORKFLOW
    件数: 3件（変更なし）
    検証:
      - READ操作のため、DBは更新されない

Scenario: ワークフロー操作履歴を取得する
  Given DBに以下のワークフローが存在する:
    テーブル: WORKFLOW
    件数: 1件
    データ:
      | WORKFLOW_ID | STATE    |
      | 1           | APPROVED |
  
  And DBに以下の操作履歴が存在する:
    テーブル: WORKFLOW_HISTORY
    件数: 3件
    データセット: /datasets/workflow/initial-workflow-history.xml
    データ:
      | HISTORY_ID | WORKFLOW_ID | ACTION   | OPERATED_BY | OPERATED_AT |
      | 1          | 1           | CREATED  | 1           | 2026-01-01  |
      | 2          | 1           | APPLIED  | 1           | 2026-01-02  |
      | 3          | 1           | APPROVED | 2           | 2026-01-03  |
  
  When GET /api/workflows/1/history を送る
  
  Then レスポンスは 200 OK
  And 操作履歴一覧が返る:
    件数: 3件
    データ:
      | action   | operatedBy | operatedAt |
      | CREATED  | 1          | 2026-01-01 |
      | APPLIED  | 1          | 2026-01-02 |
      | APPROVED | 2          | 2026-01-03 |
  
  And DBの状態は変化しない:
    テーブル: WORKFLOW, WORKFLOW_HISTORY
    検証:
      - READ操作のため、DBは更新されない
```

---

## 3. DBUnitデータセット対応表

| シナリオ | 初期データセット | 期待データセット | 検証テーブル |
|---------|----------------|----------------|------------|
| ワークフロー作成 | `/datasets/workflow/initial-employee.xml` | `/datasets/workflow/expected-workflow-created.xml` | WORKFLOW |
| ワークフロー申請（CREATED→APPLIED） | `/datasets/workflow/initial-workflow-draft.xml` | `/datasets/workflow/expected-workflow-applied.xml` | WORKFLOW |
| ワークフロー承認（APPLIED→APPROVED） | `/datasets/workflow/initial-workflow-applied.xml` | `/datasets/workflow/expected-workflow-approved.xml` | WORKFLOW<br>BOOK |
| 権限のない社員が承認 | `/datasets/workflow/initial-workflow-applied.xml` | （変更なし） | WORKFLOW |
| ワークフロー一覧を取得する | `/datasets/workflow/initial-workflows.xml` | （変更なし） | WORKFLOW |
| ワークフロー操作履歴を取得する | `/datasets/workflow/initial-workflow-history.xml` | （変更なし） | WORKFLOW<br>WORKFLOW_HISTORY |

---

## 4. 参照

* [userstory.md](userstory.md)
* [../../common/data_model.md](../../common/data_model.md)
* [../../common/architecture_design.md](../../common/architecture_design.md)
* DBUnit公式ドキュメント: http://dbunit.sourceforge.net/
