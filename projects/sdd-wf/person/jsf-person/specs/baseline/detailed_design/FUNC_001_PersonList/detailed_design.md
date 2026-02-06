# FUNC_001_PersonList - 詳細設計書

プロジェクトID: jsf-person  
機能ID: FUNC_001_PersonList  
バージョン: 1.0.0  
最終更新日: 2026-02-06

---

## 概要

本ドキュメントは、FUNC_001_PersonList（PERSON一覧画面）の詳細設計を記述する。

* 対象画面: personList.xhtml
* 対象Bean: PersonListBean
* 責務: 全PERSONを一覧表示し、編集・削除を可能にする

---

## 1. PersonListBean

### 1.1 クラス設計

* クラス名: `PersonListBean`
* パッケージ: `pro.kensait.jsfperson.bean`
* 責務: PERSON一覧画面のManagedBean
* スコープ: `@ViewScoped`
* アノテーション: `@Named("personListBean")`

### 1.2 依存関係

* `PersonService` (via `@Inject`)

### 1.3 主要フィールド

* `List<Person> personList` - 表示するPERSONリスト

### 1.4 主要メソッド

* `void init()` - `@PostConstruct`, PersonService.getAllPersons()を呼び出してpersonListを初期化
* `void deletePerson(Integer personId)` - PersonService.deletePerson()を呼び出し、init()で再読み込み

---

## 2. PersonService

### 2.1 クラス設計

* クラス名: `PersonService`
* パッケージ: `pro.kensait.jsfperson.service`
* 責務: Person関連のビジネスロジック
* スコープ: `@RequestScoped`
* アノテーション: `@Transactional`

### 2.2 依存関係

* `PersonDao` (via `@Inject`)

### 2.3 主要メソッド（一覧画面用）

* `List<Person> getAllPersons()` - PersonDao.findAll()を呼び出す
* `void deletePerson(Integer personId)` - PersonDao.findById()で取得後、PersonDao.remove()で削除

---

## 3. 画面遷移

* 新規追加ボタン → `personInput.xhtml` (パラメータなし)
* 編集ボタン → `personInput.xhtml?personId=xxx` (personIdをパラメータで渡す)
* 削除ボタン → 同一画面で削除後、リスト再表示

---

## 4. 関連ドキュメント

* [基本設計 - 機能設計書](../../basic_design/person_management/functional_design.md)
* [基本設計 - 画面設計書](../../basic_design/person_management/screen_design.md)
* [単体テスト - 振る舞い仕様](./behaviors.md)
