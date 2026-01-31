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
  Given 認証済み社員が存在する
  When POST /api/workflows にワークフロー内容を送る
  Then レスポンスは 201 Created
  And ワークフローが CREATED 状態で作成される

Scenario: ワークフローを申請する（CREATED → APPLIED）
  Given ワークフローID=1 が CREATED 状態で存在する
  When POST /api/workflows/1/apply を送る
  Then レスポンスは 200 OK
  And ワークフローが APPLIED 状態になる

Scenario: ワークフローを承認する（APPLIED → APPROVED）
  Given ワークフローID=1 が APPLIED 状態で存在する
  And 認証済み社員に承認権限がある
  When POST /api/workflows/1/approve を送る
  Then レスポンスは 200 OK
  And ワークフローが APPROVED 状態になる
  And 書籍マスタに反映される（該当する場合）

Scenario: 権限のない社員が承認しようとする
  Given ワークフローID=1 が APPLIED 状態で存在する
  And 認証済み社員に承認権限がない
  When POST /api/workflows/1/approve を送る
  Then レスポンスは 403 Forbidden
```

### Feature: ワークフロー一覧・履歴

```gherkin
Scenario: ワークフロー一覧を取得する
  Given DB にワークフローが存在する
  When GET /api/workflows を送る
  Then レスポンスは 200 OK
  And ワークフロー一覧が返る

Scenario: ワークフロー操作履歴を取得する
  Given ワークフローID=1 に操作履歴が存在する
  When GET /api/workflows/1/history を送る
  Then レスポンスは 200 OK
  And 操作履歴一覧が返る
```

---

## 3. 参照

* [userstory.md](userstory.md)
* [../../common/data_model.md](../../common/data_model.md)
* [../../common/architecture_design.md](../../common/architecture_design.md)
