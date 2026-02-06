# FUNC_001_PersonList - 振る舞い仕様書（単体テスト用）

プロジェクトID: jsf-person  
機能ID: FUNC_001_PersonList  
バージョン: 1.0.0  
最終更新日: 2026-02-06

---

## 概要

本ドキュメントは、FUNC_001_PersonList の単体テスト用振る舞い仕様を Gherkin 記法で記述する。

* テスト対象: PersonListBean, PersonService
* テスト方式: ブラックボックステスト、依存関係はモック

---

## PersonListBean - init()

```gherkin
Feature: PersonListBean - init()
  As a PersonListBean
  I want to PERSON一覧を初期化する
  So that 画面に表示できる

  Scenario: 複数のPersonが存在する場合
    Given PersonService にモック設定がされている
    And PersonService.getAllPersons() が以下を返す:
      | personId | personName | age | gender |
      | 1        | 田中太郎   | 25  | male   |
      | 2        | 佐藤花子   | 30  | female |
    When PersonListBean の init() が呼び出される
    Then PersonService.getAllPersons() が1回呼び出される
    And personList フィールドに2件のPersonが設定される

  Scenario: Personが存在しない場合
    Given PersonService にモック設定がされている
    And PersonService.getAllPersons() が空リストを返す
    When PersonListBean の init() が呼び出される
    Then personList フィールドは空リストである
```

---

## PersonListBean - deletePerson()

```gherkin
Feature: PersonListBean - deletePerson()
  As a PersonListBean
  I want to 指定されたPersonを削除する
  So that 一覧から削除できる

  Scenario: 正常にPersonを削除する
    Given PersonService にモック設定がされている
    And personId=1 のPersonが存在する
    When PersonListBean の deletePerson(1) が呼び出される
    Then PersonService.deletePerson(1) が1回呼び出される
    And PersonService.getAllPersons() が再度呼び出される
    And personList フィールドが更新される
```

---

## PersonService - getAllPersons()

```gherkin
Feature: PersonService - getAllPersons()
  As a PersonService
  I want to 全Personを取得する
  So that 一覧画面に表示できる

  Scenario: PersonDaoから正常に取得する
    Given PersonDao にモック設定がされている
    And PersonDao.findAll() が2件のPersonを返す
    When PersonService の getAllPersons() が呼び出される
    Then PersonDao.findAll() が1回呼び出される
    And 2件のPersonリストが返される
```

---

## PersonService - deletePerson()

```gherkin
Feature: PersonService - deletePerson()
  As a PersonService
  I want to 指定されたPersonを削除する
  So that データベースから削除できる

  Scenario: 正常にPersonを削除する
    Given PersonDao にモック設定がされている
    And PersonDao.findById(1) が既存Personを返す
    When PersonService の deletePerson(1) が呼び出される
    Then PersonDao.findById(1) が1回呼び出される
    And PersonDao.remove(person) が1回呼び出される
    And トランザクションがコミットされる

  Scenario: 存在しないPersonを削除しようとする
    Given PersonDao にモック設定がされている
    And PersonDao.findById(999) が null を返す
    When PersonService の deletePerson(999) が呼び出される
    Then 例外が発生する（または何もしない）
```

---

## 参考資料

* [詳細設計書](./detailed_design.md)
* [基本設計 - 振る舞い仕様](../../basic_design/person_management/behaviors.md)
* [振る舞いの記法](../../../../../agent_skills/struts-to-jsf-migration/principles/common_rules.md)
