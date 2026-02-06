# FUNC_002_PersonInput - 詳細設計書

プロジェクトID: jsf-person  
機能ID: FUNC_002_PersonInput  
バージョン: 1.0.0  
最終更新日: 2026-02-06

---

## 概要

本ドキュメントは、FUNC_002_PersonInput（PERSON入力画面）の詳細設計を記述する。

* 対象画面: personInput.xhtml
* 対象Bean: PersonInputBean
* 責務: PERSON情報の入力・編集を受け付け、バリデーション後に確認画面へ遷移

---

## 1. PersonInputBean

### 1.1 クラス設計

* クラス名: `PersonInputBean`
* パッケージ: `pro.kensait.jsfperson.bean`
* 責務: PERSON入力画面のManagedBean
* スコープ: `@ViewScoped`
* アノテーション: `@Named("personInputBean")`

### 1.2 依存関係

* `PersonService` (via `@Inject`)

### 1.3 主要フィールド

* `Integer personId` - URLパラメータから取得、編集モード判定に使用
* `String personName` - 名前入力、`@NotNull`, `@Size(min=1, max=30)`
* `Integer age` - 年齢入力、`@NotNull`, `@Min(0)`, `@Max(150)`
* `String gender` - 性別入力、`@NotNull`

### 1.4 主要メソッド

* `void init()` - `@PostConstruct`, personIdがnullでない場合はPersonService.getPersonById()で既存データを取得してフィールドに設定
* `String confirm()` - バリデーション後、`personConfirm` を返して確認画面へ遷移
* `String cancel()` - `personList?faces-redirect=true` を返して一覧画面へリダイレクト

---

## 2. PersonService

### 2.1 主要メソッド（入力画面用）

* `Person getPersonById(Integer personId)` - PersonDao.findById()を呼び出す

---

## 3. バリデーション

* Bean Validation アノテーションをフィールドに適用
* JSFが自動的にバリデーションを実行
* エラー時は入力画面を再表示し、`<h:messages>`にエラーメッセージを表示

---

## 4. 画面遷移

* 確認画面へボタン → `personConfirm.xhtml` (ViewScopedでデータ引き継ぎ)
* キャンセルボタン → `personList.xhtml` (リダイレクト)

---

## 5. 関連ドキュメント

* [基本設計 - 機能設計書](../../basic_design/person_management/functional_design.md)
* [基本設計 - 画面設計書](../../basic_design/person_management/screen_design.md)
* [単体テスト - 振る舞い仕様](./behaviors.md)
