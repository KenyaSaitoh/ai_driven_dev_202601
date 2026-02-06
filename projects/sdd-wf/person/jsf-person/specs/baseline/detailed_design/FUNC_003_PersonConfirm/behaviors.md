# FUNC_003_PersonConfirm - 振る舞い仕様書（単体テスト用）

プロジェクトID: jsf-person  
機能ID: FUNC_003_PersonConfirm  
バージョン: 1.0.0  
最終更新日: 2026-02-06

---

## 概要

本ドキュメントは、FUNC_003_PersonConfirm の単体テスト用振る舞い仕様を Gherkin 記法で記述する。

* テスト対象: PersonConfirmBean, PersonService
* テスト方式: ブラックボックステスト、依存関係はモック

---

## PersonConfirmBean - save() (新規追加)

```gherkin
Feature: PersonConfirmBean - save() (新規追加)
  As a PersonConfirmBean
  I want to 新しいPersonを登録する
  So that データベースに保存できる

  Scenario: 正常に新規Personを登録する
    Given PersonInputBean にモック設定がされている
    And PersonInputBean のフィールドが以下である:
      | personId | personName | age | gender |
      | null     | 山田太郎   | 28  | male   |
    And PersonService にモック設定がされている
    When PersonConfirmBean の save() が呼び出される
    Then PersonInputBean からデータが取得される
    And 新しいPersonオブジェクトが作成される
    And PersonService.addPerson(person) が1回呼び出される
    And "personList?faces-redirect=true" が返される
```

---

## PersonConfirmBean - save() (更新)

```gherkin
Feature: PersonConfirmBean - save() (更新)
  As a PersonConfirmBean
  I want to 既存のPersonを更新する
  So that データベースに変更を反映できる

  Scenario: 正常に既存Personを更新する
    Given PersonInputBean にモック設定がされている
    And PersonInputBean のフィールドが以下である:
      | personId | personName | age | gender |
      | 1        | 田中太郎   | 26  | male   |
    And PersonService にモック設定がされている
    When PersonConfirmBean の save() が呼び出される
    Then PersonInputBean からデータが取得される
    And Personオブジェクトが作成される（personId=1を含む）
    And PersonService.updatePerson(person) が1回呼び出される
    And "personList?faces-redirect=true" が返される
```

---

## PersonConfirmBean - back()

```gherkin
Feature: PersonConfirmBean - back()
  As a PersonConfirmBean
  I want to 入力画面に戻る
  So that 入力内容を修正できる

  Scenario: 正常に入力画面に戻る
    When PersonConfirmBean の back() が呼び出される
    Then "personInput" が返される（またはJavaScriptのhistory.back()が実行される）
```

---

## PersonService - addPerson()

```gherkin
Feature: PersonService - addPerson()
  As a PersonService
  I want to 新しいPersonを追加する
  So that データベースに保存できる

  Scenario: 正常にPersonを追加する
    Given PersonDao にモック設定がされている
    And 新しいPersonオブジェクトが作成されている:
      | personName | age | gender |
      | 山田太郎   | 28  | male   |
    When PersonService の addPerson(person) が呼び出される
    Then PersonDao.persist(person) が1回呼び出される
    And トランザクションがコミットされる

  Scenario: データベースエラーが発生する
    Given PersonDao にモック設定がされている
    And PersonDao.persist() が PersistenceException をスローする
    When PersonService の addPerson(person) が呼び出される
    Then RuntimeException がスローされる
    And トランザクションが自動的にロールバックされる
```

---

## PersonService - updatePerson()

```gherkin
Feature: PersonService - updatePerson()
  As a PersonService
  I want to 既存のPersonを更新する
  So that 変更をデータベースに反映できる

  Scenario: 正常にPersonを更新する
    Given PersonDao にモック設定がされている
    And 既存のPersonオブジェクトが編集されている:
      | personId | personName | age | gender |
      | 1        | 田中太郎   | 26  | male   |
    When PersonService の updatePerson(person) が呼び出される
    Then PersonDao.merge(person) が1回呼び出される
    And トランザクションがコミットされる

  Scenario: データベースエラーが発生する
    Given PersonDao にモック設定がされている
    And PersonDao.merge() が PersistenceException をスローする
    When PersonService の updatePerson(person) が呼び出される
    Then RuntimeException がスローされる
    And トランザクションが自動的にロールバックされる
```

---

## 参考資料

* [詳細設計書](./detailed_design.md)
* [基本設計 - 振る舞い仕様](../../basic_design/person_management/behaviors.md)
* [振る舞いの記法](../../../../../agent_skills/struts-to-jsf-migration/principles/common_rules.md)
