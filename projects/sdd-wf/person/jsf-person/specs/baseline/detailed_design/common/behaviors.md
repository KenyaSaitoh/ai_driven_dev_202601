# 共通設計 - 振る舞い仕様書（単体テスト用）

プロジェクトID: jsf-person  
分類: common（共通設計）  
バージョン: 1.0.0  
最終更新日: 2026-02-06

---

## 概要

本ドキュメントは、共通設計の単体テスト用振る舞い仕様を Gherkin 記法で記述する。

* テスト対象: PersonDao
* テスト方式: ブラックボックステスト、依存関係はモック

---

## PersonDao - findAll()

```gherkin
Feature: PersonDao - findAll()
  As a PersonDao
  I want to 全Personを取得する
  So that 一覧表示に使用できる

  Scenario: 複数のPersonが存在する場合
    Given EntityManager にモック設定がされている
    And データベースに以下のPersonが存在する:
      | personId | personName | age | gender |
      | 1        | 田中太郎   | 25  | male   |
      | 2        | 佐藤花子   | 30  | female |
    When PersonDao の findAll() を呼び出す
    Then JPQL "SELECT p FROM Person p ORDER BY p.personId" が実行される
    And 2件のPersonリストが返される
    And リストの順序は personId の昇順である

  Scenario: Personが存在しない場合
    Given EntityManager にモック設定がされている
    And データベースにPersonが存在しない
    When PersonDao の findAll() を呼び出す
    Then 空のリストが返される
```

---

## PersonDao - findById()

```gherkin
Feature: PersonDao - findById()
  As a PersonDao
  I want to 指定されたIDのPersonを取得する
  So that 編集時に使用できる

  Scenario: 指定されたIDのPersonが存在する場合
    Given EntityManager にモック設定がされている
    And personId=1 のPersonが存在する
    When PersonDao の findById(1) を呼び出す
    Then EntityManager の find(Person.class, 1) が実行される
    And 対応するPersonオブジェクトが返される

  Scenario: 指定されたIDのPersonが存在しない場合
    Given EntityManager にモック設定がされている
    And personId=999 のPersonが存在しない
    When PersonDao の findById(999) を呼び出す
    Then null が返される
```

---

## PersonDao - persist()

```gherkin
Feature: PersonDao - persist()
  As a PersonDao
  I want to 新しいPersonを永続化する
  So that データベースに保存できる

  Scenario: 正常にPersonを永続化する
    Given EntityManager にモック設定がされている
    And 新しいPersonオブジェクトが作成されている:
      | personName | age | gender |
      | 山田太郎   | 28  | male   |
    When PersonDao の persist(person) を呼び出す
    Then EntityManager の persist(person) が実行される
    And 例外が発生しない
```

---

## PersonDao - merge()

```gherkin
Feature: PersonDao - merge()
  As a PersonDao
  I want to Personを更新する
  So that 変更をデータベースに反映できる

  Scenario: 正常にPersonを更新する
    Given EntityManager にモック設定がされている
    And 既存のPersonオブジェクトが編集されている:
      | personId | personName | age | gender |
      | 1        | 田中太郎   | 26  | male   |
    When PersonDao の merge(person) を呼び出す
    Then EntityManager の merge(person) が実行される
    And マージされたPersonオブジェクトが返される
```

---

## PersonDao - remove()

```gherkin
Feature: PersonDao - remove()
  As a PersonDao
  I want to Personを削除する
  So that データベースから削除できる

  Scenario: 正常にPersonを削除する
    Given EntityManager にモック設定がされている
    And 削除対象のPersonオブジェクトが存在する:
      | personId | personName | age | gender |
      | 1        | 田中太郎   | 25  | male   |
    When PersonDao の remove(person) を呼び出す
    Then EntityManager の remove(person) が実行される
    And 例外が発生しない
```

---

## 参考資料

* [詳細設計書](./detailed_design.md)
* [振る舞いの記法](../../../../../agent_skills/struts-to-jsf-migration/principles/common_rules.md) - Gherkin記法ガイド
