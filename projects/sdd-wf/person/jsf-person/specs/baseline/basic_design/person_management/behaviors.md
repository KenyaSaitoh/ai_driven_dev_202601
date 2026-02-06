# Person管理画面グループ - 振る舞い仕様書

プロジェクトID: jsf-person  
画面グループ: person_management  
バージョン: 1.0.0  
最終更新日: 2026-02-06

---

## 概要

本ドキュメントは、Person管理画面グループの振る舞いを Gherkin 記法で記述する。
E2Eテスト用の振る舞い仕様として使用する（複数画面にまたがる遷移フロー）。

* 画面グループ: 関連する画面群（一覧、入力、確認）をまとめたもの
* JSFは画面中心のサーバーサイドMVCフレームワーク

---

## 1. PERSON一覧表示

### シナリオ: PERSON一覧の初期表示

```gherkin
Feature: PERSON一覧表示
  As a ユーザー
  I want to 全PERSONを一覧表示する
  So that PERSON情報を確認できる

  Scenario: PERSON一覧の初期表示
    Given データベースに以下のPERSONが登録されている:
      | personId | personName | age | gender |
      | 1        | 田中太郎   | 25  | male   |
      | 2        | 佐藤花子   | 30  | female |
      | 3        | 鈴木次郎   | 45  | male   |
    When ユーザーが "personList.xhtml" にアクセスする
    Then PersonListBean の init() メソッドが実行される
    And PersonService の getAllPersons() が呼び出される
    And データベースから全PERSONが取得される
    And 画面に以下のPERSONが表示される:
      | personId | personName | age | gender |
      | 1        | 田中太郎   | 25  | 男性   |
      | 2        | 佐藤花子   | 30  | 女性   |
      | 3        | 鈴木次郎   | 45  | 男性   |
    And 各PERSONに "編集" ボタンが表示される
    And 各PERSONに "削除" ボタンが表示される
```

---

## 2. PERSON新規追加

### シナリオ: 新しいPERSONの追加

```gherkin
Feature: PERSON新規追加
  As a ユーザー
  I want to 新しいPERSONを追加する
  So that PERSON情報を管理できる

  Scenario: 正常に新規PERSONを追加する
    Given ユーザーが一覧画面を表示している
    When ユーザーが "新規追加" ボタンをクリックする
    Then "personInput.xhtml" に遷移する
    And PersonInputBean の init() メソッドが実行される
    And personId は null である
    
    When ユーザーが以下の情報を入力する:
      | フィールド | 値       |
      | 名前       | 山田太郎 |
      | 年齢       | 28       |
      | 性別       | 男性     |
    And ユーザーが "確認画面へ" ボタンをクリックする
    Then PersonInputBean の confirm() メソッドが実行される
    And 入力データが PersonInputBean に保持される
    And "personConfirm.xhtml" に遷移する
    
    When ユーザーが確認画面で "登録" ボタンをクリックする
    Then PersonConfirmBean の save() メソッドが実行される
    And PersonService の addPerson() が呼び出される
    And EntityManager の persist() でデータベースに保存される
    And トランザクションがコミットされる
    And "personList.xhtml" にリダイレクトされる
    And 一覧画面に新しいPERSONが表示される
```

---

## 3. PERSON編集

### シナリオ: 既存PERSONの編集

```gherkin
Feature: PERSON編集
  As a ユーザー
  I want to 既存のPERSONを編集する
  So that PERSON情報を最新に保つ

  Scenario: 正常にPERSONを編集する
    Given データベースに以下のPERSONが登録されている:
      | personId | personName | age | gender |
      | 1        | 田中太郎   | 25  | male   |
    And ユーザーが一覧画面を表示している
    When ユーザーが personId=1 の "編集" ボタンをクリックする
    Then "personInput.xhtml?personId=1" に遷移する
    And PersonInputBean の init() メソッドが実行される
    And PersonService の getPersonById(1) が呼び出される
    And 既存データが入力フォームにプリセットされる:
      | フィールド | 値       |
      | 名前       | 田中太郎 |
      | 年齢       | 25       |
      | 性別       | 男性     |
    
    When ユーザーが年齢を "26" に変更する
    And ユーザーが "確認画面へ" ボタンをクリックする
    Then "personConfirm.xhtml" に遷移する
    
    When ユーザーが確認画面で "登録" ボタンをクリックする
    Then PersonService の updatePerson() が呼び出される
    And EntityManager の merge() でデータベースが更新される
    And "personList.xhtml" にリダイレクトされる
    And 一覧画面に更新されたPERSONが表示される
```

---

## 4. PERSON削除

### シナリオ: PERSONの削除

```gherkin
Feature: PERSON削除
  As a ユーザー
  I want to PERSONを削除する
  So that 不要なPERSON情報を削除できる

  Scenario: 正常にPERSONを削除する
    Given データベースに以下のPERSONが登録されている:
      | personId | personName | age | gender |
      | 1        | 田中太郎   | 25  | male   |
      | 2        | 佐藤花子   | 30  | female |
    And ユーザーが一覧画面を表示している
    When ユーザーが personId=1 の "削除" ボタンをクリックする
    Then JavaScript の削除確認ダイアログが表示される
    
    When ユーザーが "OK" をクリックする
    Then PersonListBean の deletePerson(1) メソッドが実行される
    And PersonService の deletePerson(1) が呼び出される
    And EntityManager の remove() でデータベースから削除される
    And PersonListBean の init() が再度呼び出される
    And 一覧画面に削除後のPERSONリストが表示される
    And personId=1 のPERSONは表示されない
    And personId=2 のPERSONは表示される

  Scenario: 削除をキャンセルする
    Given ユーザーが一覧画面を表示している
    When ユーザーが personId=1 の "削除" ボタンをクリックする
    Then JavaScript の削除確認ダイアログが表示される
    
    When ユーザーが "キャンセル" をクリックする
    Then 削除処理は実行されない
    And 一覧画面がそのまま表示される
```

---

## 5. バリデーション

### シナリオ: 入力バリデーションエラー

```gherkin
Feature: 入力バリデーション
  As a システム
  I want to ユーザー入力を検証する
  So that データの整合性を保つ

  Scenario: 名前が未入力
    Given ユーザーが入力画面を表示している
    When ユーザーが名前を入力せずに "確認画面へ" ボタンをクリックする
    Then バリデーションエラーが発生する
    And 入力画面が再表示される
    And エラーメッセージ "名前を入力してください" が表示される

  Scenario: 名前が31文字以上
    Given ユーザーが入力画面を表示している
    When ユーザーが31文字以上の名前を入力する
    And ユーザーが "確認画面へ" ボタンをクリックする
    Then バリデーションエラーが発生する
    And エラーメッセージ "名前は30文字以内で入力してください" が表示される

  Scenario: 年齢が負の数
    Given ユーザーが入力画面を表示している
    When ユーザーが年齢に "-1" を入力する
    And ユーザーが "確認画面へ" ボタンをクリックする
    Then バリデーションエラーが発生する
    And エラーメッセージ "年齢は0以上で入力してください" が表示される

  Scenario: 年齢が151以上
    Given ユーザーが入力画面を表示している
    When ユーザーが年齢に "151" を入力する
    And ユーザーが "確認画面へ" ボタンをクリックする
    Then バリデーションエラーが発生する
    And エラーメッセージ "年齢は150以下で入力してください" が表示される

  Scenario: 性別が未選択
    Given ユーザーが入力画面を表示している
    When ユーザーが性別を選択せずに "確認画面へ" ボタンをクリックする
    Then バリデーションエラーが発生する
    And エラーメッセージ "性別を選択してください" が表示される
```

---

## 6. キャンセル・戻る

### シナリオ: 入力画面からキャンセル

```gherkin
Feature: キャンセル操作
  As a ユーザー
  I want to 入力をキャンセルする
  So that 入力を破棄して一覧に戻れる

  Scenario: 入力画面からキャンセル
    Given ユーザーが入力画面を表示している
    And ユーザーが何らかのデータを入力している
    When ユーザーが "キャンセル" ボタンをクリックする
    Then PersonInputBean の cancel() メソッドが実行される
    And "personList.xhtml" にリダイレクトされる
    And 入力データは破棄される
    And 一覧画面が表示される

  Scenario: 確認画面から戻る
    Given ユーザーが確認画面を表示している
    When ユーザーが "戻る" ボタンをクリックする
    Then PersonConfirmBean の back() メソッドが実行される
    And ブラウザ履歴で入力画面に戻る
    And 入力データは保持される (@ViewScoped)
    And 入力画面が再表示される
```

---

## 7. エラーハンドリング

### シナリオ: データベースエラー

```gherkin
Feature: エラーハンドリング
  As a システム
  I want to エラーを適切に処理する
  So that ユーザーに適切なフィードバックを提供できる

  Scenario: 登録時のデータベースエラー
    Given ユーザーが確認画面を表示している
    When PersonService の addPerson() でデータベースエラーが発生する
    Then RuntimeException がスローされる
    And トランザクションが自動的にロールバックされる
    And エラーメッセージが表示される
    And データベースに不完全なデータは保存されない
```

---

## 参考資料

* [機能設計書](./functional_design.md) - 画面遷移とコンポーネント設計
* [画面設計書](./screen_design.md) - 画面レイアウトとバリデーション
* [データモデル](../common/data_model.md) - Personエンティティ定義
* [振る舞いの記法](../../../../../agent_skills/struts-to-jsf-migration/principles/common_rules.md) - Gherkin記法ガイド
