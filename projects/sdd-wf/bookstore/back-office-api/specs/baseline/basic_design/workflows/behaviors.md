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
    Given 社員ID=1（一般社員）が存在する
    When WorkflowService.createWorkflow(workflowType="ADD_NEW_BOOK", operatedBy=1)を呼び出す
    Then Workflowエンティティが作成される:
      | state | operatedBy |
      | DRAFT | 1          |

  Scenario: ワークフロー申請
    Given Workflow(id=1, state="DRAFT")が存在する
    When WorkflowService.applyWorkflow(workflowId=1, operatedBy=1)を呼び出す
    Then 状態が更新される:
      | state   | appliedAt |
      | APPLIED | 現在時刻   |

  Scenario: ワークフロー承認（管理職のみ）
    Given Workflow(id=1, state="APPLIED")が存在する
    And 社員ID=2（管理職）が存在する
    When WorkflowService.approveWorkflow(workflowId=1, approvedBy=2)を呼び出す
    Then 状態が更新される:
      | state    | approvedBy | approvedAt |
      | APPROVED | 2          | 現在時刻    |

  Scenario: ワークフロー却下
    Given Workflow(id=1, state="APPLIED")が存在する
    And 社員ID=2（管理職）が存在する
    When WorkflowService.rejectWorkflow(workflowId=1, rejectedBy=2, reason="理由")を呼び出す
    Then 状態が更新される:
      | state   | rejectedBy | rejectedAt |
      | CREATED | 2          | 現在時刻    |
```

---

## 3. 参考資料

* [functional_design.md](functional_design.md) - ワークフロー管理機能設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書
