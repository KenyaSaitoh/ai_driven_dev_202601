# ワークフロー管理ドメイン - 結合テスト仕様書

プロジェクトID: back-office-api  
ドメイン: workflows  
バージョン: 1.0.0  
最終更新日: 2026-02-05

---

## 1. 概要

本文書は、ワークフロー管理ドメインのService層以下（Service + DAO + Entity + DB）の結合テスト仕様を記述する。

---

## 2. WorkflowService のシナリオ

### 2.1 ワークフロー管理

#### Feature: ワークフロー作成と状態遷移

```gherkin
Feature: ワークフロー管理
  ワークフローを作成し、申請・承認・却下を行う

  Scenario: ワークフロー作成
    Given DBに以下の社員が存在する:
      テーブル: EMPLOYEE
      件数: 1件
      データセット: /datasets/workflows/initial-employee.xml
      データ:
        | EMPLOYEE_ID | NAME   | ROLE  |
        | 1           | 山田太郎 | STAFF |
    
    And DBのワークフローテーブルは空である:
      テーブル: WORKFLOW
      件数: 0件
    
    When WorkflowService.createWorkflow(workflowType="ADD_NEW_BOOK", operatedBy=1)を呼び出す
    
    Then DBのワークフローテーブルは以下になる:
      テーブル: WORKFLOW
      件数: 1件（+1件追加）
      データセット: /datasets/workflows/expected-workflow-created.xml
      データ:
        | WORKFLOW_ID | WORKFLOW_TYPE | STATE   | OPERATED_BY |
        | 1           | ADD_NEW_BOOK  | CREATED | 1           |
      検証:
        - WORKFLOW_ID は自動採番される
        - STATE は CREATED で作成される

  Scenario: ワークフロー申請
    Given DBにDRAFT状態のワークフローが存在する:
      テーブル: WORKFLOW
      件数: 1件
      データセット: /datasets/workflows/initial-workflow-draft.xml
      データ:
        | WORKFLOW_ID | WORKFLOW_TYPE | STATE   | OPERATED_BY |
        | 1           | ADD_NEW_BOOK  | CREATED | 1           |
    
    When WorkflowService.applyWorkflow(workflowId=1, operatedBy=1)を呼び出す
    
    Then DBのワークフローテーブルは以下になる:
      テーブル: WORKFLOW
      件数: 1件（変更なし）
      データセット: /datasets/workflows/expected-workflow-applied.xml
      データ:
        | WORKFLOW_ID | STATE   | APPLIED_AT |
        | 1           | APPLIED | 現在時刻    |
      検証:
        - STATE が CREATED から APPLIED に変更される
        - APPLIED_AT に現在時刻が設定される

  Scenario: ワークフロー承認（管理職のみ）
    Given DBにAPPLIED状態のワークフローが存在する:
      テーブル: WORKFLOW
      件数: 1件
      データセット: /datasets/workflows/initial-workflow-applied.xml
      データ:
        | WORKFLOW_ID | STATE   |
        | 1           | APPLIED |
    
    And DBに管理職社員が存在する:
      テーブル: EMPLOYEE
      データ:
        | EMPLOYEE_ID | NAME   | ROLE    |
        | 2           | 佐藤花子 | MANAGER |
    
    When WorkflowService.approveWorkflow(workflowId=1, approvedBy=2)を呼び出す
    
    Then DBのワークフローテーブルは以下になる:
      テーブル: WORKFLOW
      件数: 1件（変更なし）
      データセット: /datasets/workflows/expected-workflow-approved.xml
      データ:
        | WORKFLOW_ID | STATE    | APPROVED_BY | APPROVED_AT |
        | 1           | APPROVED | 2           | 現在時刻     |
      検証:
        - STATE が APPLIED から APPROVED に変更される
        - APPROVED_BY に承認者ID=2 が設定される

  Scenario: ワークフロー却下
    Given DBにAPPLIED状態のワークフローが存在する:
      テーブル: WORKFLOW
      件数: 1件
      データセット: /datasets/workflows/initial-workflow-applied.xml
      データ:
        | WORKFLOW_ID | STATE   |
        | 1           | APPLIED |
    
    And DBに管理職社員が存在する:
      テーブル: EMPLOYEE
      データ:
        | EMPLOYEE_ID | NAME   | ROLE    |
        | 2           | 佐藤花子 | MANAGER |
    
    When WorkflowService.rejectWorkflow(workflowId=1, rejectedBy=2, reason="理由")を呼び出す
    
    Then DBのワークフローテーブルは以下になる:
      テーブル: WORKFLOW
      件数: 1件（変更なし）
      データセット: /datasets/workflows/expected-workflow-rejected.xml
      データ:
        | WORKFLOW_ID | STATE   | REJECTED_BY | REJECTED_AT |
        | 1           | CREATED | 2           | 現在時刻     |
      検証:
        - STATE が APPLIED から CREATED に変更される（却下）
        - REJECTED_BY に却下者ID=2 が設定される
```

---

## 3. DBUnitデータセット対応表

| シナリオ | 初期データセット | 期待データセット | 検証テーブル |
|---------|----------------|----------------|------------|
| ワークフロー作成 | `/datasets/workflows/initial-employee.xml` | `/datasets/workflows/expected-workflow-created.xml` | WORKFLOW |
| ワークフロー申請 | `/datasets/workflows/initial-workflow-draft.xml` | `/datasets/workflows/expected-workflow-applied.xml` | WORKFLOW |
| ワークフロー承認 | `/datasets/workflows/initial-workflow-applied.xml` | `/datasets/workflows/expected-workflow-approved.xml` | WORKFLOW |
| ワークフロー却下 | `/datasets/workflows/initial-workflow-applied.xml` | `/datasets/workflows/expected-workflow-rejected.xml` | WORKFLOW |
| ワークフロー一覧を取得する | `/datasets/workflows/initial-workflows.xml` | （変更なし） | WORKFLOW |
| ワークフロー操作履歴を取得する | `/datasets/workflows/initial-workflow-history.xml` | （変更なし） | WORKFLOW<br>WORKFLOW_HISTORY |

---

## 4. 参照

* [functional_design.md](functional_design.md) - ワークフロー管理機能設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書
* [../../common/architecture_design.md](../../common/architecture_design.md) - アーキテクチャ設計書
* DBUnit公式ドキュメント: http://dbunit.sourceforge.net/
