# FUNC_002_PersonInput - 振る舞い仕様書（単体テスト用）

プロジェクトID: jsf-person  
機能ID: FUNC_002_PersonInput  
バージョン: 1.0.0  
最終更新日: 2026-02-06

---

## 概要

本ドキュメントは、FUNC_002_PersonInput の単体テスト用振る舞い仕様を Gherkin 記法で記述する。

* テスト対象: PersonInputBean, PersonService
* テスト方式: ブラックボックステスト、依存関係はモック

---

## PersonInputBean - init() (新規追加モード)

```gherkin
Feature: PersonInputBean - init() (新規追加モード)
  As a PersonInputBean
  I want to 新規追加モードで初期化する
  So that 空のフォームを表示できる

  Scenario: personIdがnullの場合（新規追加モード）
    Given PersonInputBean が作成されている
    And personId フィールドが null である
    When init() が呼び出される
    Then PersonService.getPersonById() は呼び出されない
    And personName フィールドは null である
    And age フィールドは null である
    And gender フィールドは null である
```

---

## PersonInputBean - init() (編集モード)

```gherkin
Feature: PersonInputBean - init() (編集モード)
  As a PersonInputBean
  I want to 編集モードで初期化する
  So that 既存データをフォームにプリセットできる

  Scenario: personIdが指定されている場合（編集モード）
    Given PersonInputBean が作成されている
    And personId フィールドが 1 である
    And PersonService にモック設定がされている
    And PersonService.getPersonById(1) が以下のPersonを返す:
      | personId | personName | age | gender |
      | 1        | 田中太郎   | 25  | male   |
    When init() が呼び出される
    Then PersonService.getPersonById(1) が1回呼び出される
    And personName フィールドが "田中太郎" である
    And age フィールドが 25 である
    And gender フィールドが "male" である
```

---

## PersonInputBean - confirm()

```gherkin
Feature: PersonInputBean - confirm()
  As a PersonInputBean
  I want to 確認画面へ遷移する
  So that 入力データを確認できる

  Scenario: 正常に確認画面へ遷移する
    Given PersonInputBean のフィールドに値が設定されている:
      | personName | age | gender |
      | 山田太郎   | 28  | male   |
    When confirm() が呼び出される
    Then "personConfirm" が返される
    And フィールドの値は保持される
```

---

## PersonInputBean - cancel()

```gherkin
Feature: PersonInputBean - cancel()
  As a PersonInputBean
  I want to 一覧画面へ戻る
  So that 入力をキャンセルできる

  Scenario: 正常に一覧画面へ戻る
    When cancel() が呼び出される
    Then "personList?faces-redirect=true" が返される
```

---

## PersonService - getPersonById()

```gherkin
Feature: PersonService - getPersonById()
  As a PersonService
  I want to 指定されたIDのPersonを取得する
  So that 編集画面にデータをプリセットできる

  Scenario: 正常にPersonを取得する
    Given PersonDao にモック設定がされている
    And PersonDao.findById(1) が既存Personを返す
    When PersonService の getPersonById(1) が呼び出される
    Then PersonDao.findById(1) が1回呼び出される
    And 対応するPersonオブジェクトが返される

  Scenario: 存在しないIDを指定する
    Given PersonDao にモック設定がされている
    And PersonDao.findById(999) が null を返す
    When PersonService の getPersonById(999) が呼び出される
    Then null が返される
```

---

## Bean Validation

```gherkin
Feature: Bean Validation
  As a JSF Framework
  I want to 入力値をバリデーションする
  So that データの整合性を保つ

  Scenario: 名前が未入力
    Given PersonInputBean のフィールドが以下である:
      | personName | age | gender |
      | null       | 25  | male   |
    When Bean Validationが実行される
    Then バリデーションエラーが発生する
    And エラーメッセージ "名前を入力してください" が生成される

  Scenario: 年齢が範囲外（負の数）
    Given PersonInputBean のフィールドが以下である:
      | personName | age | gender |
      | 山田太郎   | -1  | male   |
    When Bean Validationが実行される
    Then バリデーションエラーが発生する
    And エラーメッセージ "年齢は0以上で入力してください" が生成される

  Scenario: すべて正常な入力
    Given PersonInputBean のフィールドが以下である:
      | personName | age | gender |
      | 山田太郎   | 28  | male   |
    When Bean Validationが実行される
    Then バリデーションエラーは発生しない
```

---

## 参考資料

* [詳細設計書](./detailed_design.md)
* [基本設計 - 振る舞い仕様](../../basic_design/person_management/behaviors.md)
* [振る舞いの記法](../../../../../agent_skills/struts-to-jsf-migration/principles/common_rules.md)
