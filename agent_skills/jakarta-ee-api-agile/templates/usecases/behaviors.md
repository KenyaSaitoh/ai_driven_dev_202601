# ユースケース: [USECASE_NAME] - 振る舞い仕様書

ユースケースID: [USECASE_ID]  
バージョン: 1.0.0  
最終更新日: [DATE]

記法: シナリオは Gherkin 記法で記述する（Feature, Scenario, Given, When, Then, And, But）。agent_skills/jakarta-ee-api-agile/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照すること。

---

## 1. 概要

本文書は、ユースケース「[USECASE_NAME]」の振る舞い、受入基準、テストシナリオを記述する。

* 結合テスト・E2Eテスト・単体テストのシナリオ元として使用する。コード生成時は本 behaviors.md を参照して単体テストを生成する
* 共通の振る舞い（認証、エラーハンドリング等）は common/ または他ユースケースと整合させる

* 関連ドキュメント:
  * [userstory.md](userstory.md) - 本ユースケースのユーザーストーリー・受入基準
  * [../../common/architecture_design.md](../../common/architecture_design.md) - 共通アーキテクチャ
  * [../../common/data_model.md](../../common/data_model.md) - 共通データモデル

---

## 2. テストシナリオ（Gherkin）

### 2.1 [シナリオグループ名]

#### Feature: [機能名]

```gherkin
Feature: [機能の短い説明]

  Scenario: [シナリオ名]
    Given [前提条件]
    And [追加の前提]
    When [操作・イベント]
    Then [期待結果]
    And [追加の期待結果]
```

#### Scenario: [別シナリオ名]

```gherkin
  Scenario: [シナリオ名]
    Given [前提条件]
    When [操作]
    Then [期待結果]
```

---

### 2.2 エラーケース

```gherkin
  Scenario: [エラーケース名]
    Given [前提条件]
    When [不正な操作または条件]
    Then [エラーレスポンスまたは例外]
```

---

## 3. 受入基準との対応

| 受入基準 | 対応シナリオ |
|---------|-------------|
| AC1: [受入基準1] | Scenario: [シナリオ名] |
| AC2: [受入基準2] | Scenario: [シナリオ名] |

---

## 4. 参照

* [userstory.md](userstory.md) - ユーザーストーリー・受入基準
* [../../common/behaviors.md](../../common/behaviors.md) - 共通振る舞い（存在する場合）
* [../../common/architecture_design.md](../../common/architecture_design.md) - 共通アーキテクチャ
