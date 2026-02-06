# FUNC_003_PersonConfirm - 詳細設計書

プロジェクトID: jsf-person  
機能ID: FUNC_003_PersonConfirm  
バージョン: 1.0.0  
最終更新日: 2026-02-06

---

## 概要

本ドキュメントは、FUNC_003_PersonConfirm（PERSON確認画面）の詳細設計を記述する。

* 対象画面: personConfirm.xhtml
* 対象Bean: PersonConfirmBean
* 責務: 入力内容を確認し、登録または更新を実行

---

## 1. PersonConfirmBean

### 1.1 クラス設計

* クラス名: `PersonConfirmBean`
* パッケージ: `pro.kensait.jsfperson.bean`
* 責務: PERSON確認画面のManagedBean
* スコープ: `@ViewScoped`
* アノテーション: `@Named("personConfirmBean")`

### 1.2 依存関係

* `PersonInputBean` (via `@Inject`) - 入力データを取得
* `PersonService` (via `@Inject`) - 登録・更新処理を呼び出す

### 1.3 主要フィールド

* なし（PersonInputBeanから入力データを取得）

### 1.4 主要メソッド

* `String save()` - PersonInputBeanからデータを取得してPersonオブジェクトを作成、personIdがnullなら新規追加（PersonService.addPerson()）、nullでなければ更新（PersonService.updatePerson()）、成功時は`personList?faces-redirect=true`を返す
* `String back()` - JavaScriptの`history.back()`を実行（または`personInput`を返す）

---

## 2. PersonService

### 2.1 主要メソッド（確認画面用）

* `void addPerson(Person person)` - PersonDao.persist()を呼び出す、`@Transactional`
* `void updatePerson(Person person)` - PersonDao.merge()を呼び出す、`@Transactional`

---

## 3. 画面遷移

* 登録ボタン → `personList.xhtml` (リダイレクト)
* 戻るボタン → `personInput.xhtml` (history.back()またはナビゲーション)

---

## 4. トランザクション

* PersonServiceのaddPerson()とupdatePerson()は`@Transactional`
* エラー発生時は自動ロールバック

---

## 5. 関連ドキュメント

* [基本設計 - 機能設計書](../../basic_design/person_management/functional_design.md)
* [基本設計 - 画面設計書](../../basic_design/person_management/screen_design.md)
* [単体テスト - 振る舞い仕様](./behaviors.md)
