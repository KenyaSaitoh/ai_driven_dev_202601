# person_management - 画面グループ振る舞い仕様書（E2Eテスト用）

画面グループ名: person_management
バージョン: 1.0.0
最終更新日: 2026-02-08

記法: シナリオは Gherkin 記法で記述する（Feature, Scenario, Given, When, Then, And, But）。principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照すること。

---

## 1. 概要

本文書は、person_management画面グループの基本設計を外形的に捉えた振る舞い仕様書である。E2Eテスト用のシナリオを記述し、複数画面にまたがる画面フローの受入基準を定義する。

テスト対象:
* 画面グループ内の複数画面にまたがるフロー
* 画面遷移と画面間データ受け渡し
* Managed Bean + Service + Dao + Entity + DB の連携
* 実際のブラウザ操作（Playwright）
* ユーザー操作から結果表示までのエンドツーエンドフロー

テスト対象外:
* 単一メソッドレベルのテスト → detailed_design/{screen_group}/behaviors.mdで記述（単体テスト）

関連ドキュメント:
* [functional_design.md](functional_design.md) - 画面グループ機能設計書
* [screen_design.md](screen_design.md) - 画面設計書
* [../common/architecture_design.md](../common/architecture_design.md) - アーキテクチャ設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書

---

## 2. 画面グループフロー全体シナリオ

### 2.1 PERSON新規追加フロー - 正常系

#### Feature: PERSON新規追加

ユーザーが新しいPERSON情報を登録する機能

#### Scenario: 新規PERSONを登録する

* Given（前提条件）:
  * データベースにPERSONが3件存在する（Alice、Bob、Carol）
  * ブラウザでPERSON一覧画面（personList.xhtml）にアクセスしている

* When（操作）:
  * "新規追加"リンクをクリックする
  * PERSON入力画面（personInput.xhtml）に遷移する
  * "名前"フィールドに "David" を入力する
  * "年齢"フィールドに "40" を入力する
  * "性別"ラジオボタンで "男性" を選択する
  * "確認画面へ"ボタンをクリックする
  * PERSON確認画面（personConfirm.xhtml）に遷移する
  * 表示内容を確認する（名前: David、年齢: 40、性別: 男性）
  * "登録"ボタンをクリックする

* Then（期待結果）:
  * PERSON一覧画面（personList.xhtml）にリダイレクトされる
  * 一覧に新しいPERSON "David" が表示される
  * 一覧には4件のデータが表示される（Alice、Bob、Carol、David）
  * データベースにPERSON "David"が登録される

* And（追加の検証）:
  * データベースには4件のPERSONが存在する
  * 新しいPERSONのPERSON_IDは4である

#### テストデータ

* 初期データ:
  ```sql
  DELETE FROM PERSON;
  INSERT INTO PERSON (PERSON_ID, PERSON_NAME, AGE, GENDER) VALUES(1, 'Alice', 35, 'female');
  INSERT INTO PERSON (PERSON_ID, PERSON_NAME, AGE, GENDER) VALUES(2, 'Bob', 20, 'male');
  INSERT INTO PERSON (PERSON_ID, PERSON_NAME, AGE, GENDER) VALUES(3, 'Carol', 30, 'female');
  ```

* 期待されるデータ:
  ```sql
  SELECT * FROM PERSON ORDER BY PERSON_ID;
  -- 期待結果:
  -- PERSON_ID | PERSON_NAME | AGE | GENDER
  -- 1         | Alice       | 35  | female
  -- 2         | Bob         | 20  | male
  -- 3         | Carol       | 30  | female
  -- 4         | David       | 40  | male
  ```

---

### 2.2 PERSON編集フロー - 正常系

#### Feature: PERSON編集

ユーザーが既存のPERSON情報を編集する機能

#### Scenario: 既存PERSONを編集する

* Given（前提条件）:
  * データベースにPERSONが3件存在する（Alice、Bob、Carol）
  * ブラウザでPERSON一覧画面（personList.xhtml）にアクセスしている

* When（操作）:
  * Alice（PERSON_ID=1）の"編集"リンクをクリックする
  * PERSON入力画面（personInput.xhtml?personId=1）に遷移する
  * 既存データが表示される（名前: Alice、年齢: 35、性別: 女性）
  * "年齢"フィールドを "36" に変更する
  * "確認画面へ"ボタンをクリックする
  * PERSON確認画面（personConfirm.xhtml）に遷移する
  * 表示内容を確認する（名前: Alice、年齢: 36、性別: 女性）
  * "登録"ボタンをクリックする

* Then（期待結果）:
  * PERSON一覧画面（personList.xhtml）にリダイレクトされる
  * 一覧にAliceの年齢が "36" と表示される
  * データベースのAliceの年齢が36に更新される

* And（追加の検証）:
  * データベースには3件のPERSONが存在する
  * AliceのPERSON_IDは1のまま変わらない

#### テストデータ

* 初期データ:
  ```sql
  DELETE FROM PERSON;
  INSERT INTO PERSON (PERSON_ID, PERSON_NAME, AGE, GENDER) VALUES(1, 'Alice', 35, 'female');
  INSERT INTO PERSON (PERSON_ID, PERSON_NAME, AGE, GENDER) VALUES(2, 'Bob', 20, 'male');
  INSERT INTO PERSON (PERSON_ID, PERSON_NAME, AGE, GENDER) VALUES(3, 'Carol', 30, 'female');
  ```

* 期待されるデータ:
  ```sql
  SELECT * FROM PERSON WHERE PERSON_ID = 1;
  -- 期待結果:
  -- PERSON_ID | PERSON_NAME | AGE | GENDER
  -- 1         | Alice       | 36  | female
  ```

---

### 2.3 PERSON削除フロー - 正常系

#### Feature: PERSON削除

ユーザーが既存のPERSON情報を削除する機能

#### Scenario: 既存PERSONを削除する

* Given（前提条件）:
  * データベースにPERSONが3件存在する（Alice、Bob、Carol）
  * ブラウザでPERSON一覧画面（personList.xhtml）にアクセスしている

* When（操作）:
  * Bob（PERSON_ID=2）の"削除"リンクをクリックする
  * JavaScript削除確認ダイアログが表示される
  * OKボタンをクリックする
  * PERSON一覧画面（personList.xhtml）にリダイレクトされる

* Then（期待結果）:
  * 一覧にBobが表示されない
  * 一覧には2件のデータが表示される（Alice、Carol）
  * データベースからBobが削除される

* And（追加の検証）:
  * データベースには2件のPERSONが存在する
  * Bob（PERSON_ID=2）は存在しない

#### テストデータ

* 初期データ:
  ```sql
  DELETE FROM PERSON;
  INSERT INTO PERSON (PERSON_ID, PERSON_NAME, AGE, GENDER) VALUES(1, 'Alice', 35, 'female');
  INSERT INTO PERSON (PERSON_ID, PERSON_NAME, AGE, GENDER) VALUES(2, 'Bob', 20, 'male');
  INSERT INTO PERSON (PERSON_ID, PERSON_NAME, AGE, GENDER) VALUES(3, 'Carol', 30, 'female');
  ```

* 期待されるデータ:
  ```sql
  SELECT * FROM PERSON ORDER BY PERSON_ID;
  -- 期待結果:
  -- PERSON_ID | PERSON_NAME | AGE | GENDER
  -- 1         | Alice       | 35  | female
  -- 3         | Carol       | 30  | female
  ```

---

## 3. 画面別シナリオ

### 3.1 PERSON一覧 - 画面表示

#### Feature: PERSON一覧画面表示

#### Scenario: PERSON一覧画面を表示する

* Given（前提条件）:
  * データベースにPERSONが3件存在する（Alice、Bob、Carol）

* When（操作）:
  * ブラウザでPERSON一覧画面（personList.xhtml）にアクセスする

* Then（期待結果）:
  * PERSON一覧画面が表示される
  * 一覧には3件のデータが表示される
  * データはPERSON_ID昇順でソートされている（Alice、Bob、Carol）
  * 各行に"編集"リンクと"削除"リンクが表示される

#### Scenario: データが0件の場合の一覧表示

* Given（前提条件）:
  * データベースにPERSONが存在しない

* When（操作）:
  * ブラウザでPERSON一覧画面（personList.xhtml）にアクセスする

* Then（期待結果）:
  * PERSON一覧画面が表示される
  * 一覧テーブルは表示されるが、データ行は0件である
  * エラーメッセージは表示されない

---

### 3.2 PERSON入力 - 入力と確認

#### Feature: PERSON入力画面

#### Scenario: 新規追加モードで入力画面を表示する

* Given（前提条件）:
  * ブラウザでPERSON一覧画面（personList.xhtml）にアクセスしている

* When（操作）:
  * "新規追加"リンクをクリックする
  * PERSON入力画面（personInput.xhtml）に遷移する

* Then（期待結果）:
  * PERSON入力画面が表示される
  * すべての入力フィールドが空である
  * "確認画面へ"ボタンと"キャンセル"ボタンが表示される

#### Scenario: 編集モードで入力画面を表示する

* Given（前提条件）:
  * データベースにPERSONが3件存在する（Alice、Bob、Carol）
  * ブラウザでPERSON一覧画面（personList.xhtml）にアクセスしている

* When（操作）:
  * Alice（PERSON_ID=1）の"編集"リンクをクリックする
  * PERSON入力画面（personInput.xhtml?personId=1）に遷移する

* Then（期待結果）:
  * PERSON入力画面が表示される
  * 入力フィールドに既存データが表示される（名前: Alice、年齢: 35、性別: 女性）
  * "確認画面へ"ボタンと"キャンセル"ボタンが表示される

---

### 3.3 PERSON確認 - 登録実行

#### Feature: PERSON確認画面

#### Scenario: 確認画面で入力内容を確認する

* Given（前提条件）:
  * PERSON入力画面で入力を完了している
  * 名前: "David"、年齢: "40"、性別: "男性"

* When（操作）:
  * "確認画面へ"ボタンをクリックする
  * PERSON確認画面（personConfirm.xhtml）に遷移する

* Then（期待結果）:
  * PERSON確認画面が表示される
  * 入力した名前 "David" が表示される
  * 入力した年齢 "40" が表示される
  * 入力した性別 "男性" が表示される（"male"ではなく"男性"と表示）
  * "登録"ボタンと"戻る"ボタンが表示される

#### Scenario: 戻るボタンで入力画面に戻る

* Given（前提条件）:
  * PERSON確認画面が表示されている
  * 確認内容: 名前: "David"、年齢: "40"、性別: "男性"

* When（操作）:
  * "戻る"ボタンをクリックする

* Then（期待結果）:
  * PERSON入力画面に戻る（history.back()）
  * 入力内容が保持されている（名前: David、年齢: 40、性別: 男性）

---

## 4. エラーハンドリングシナリオ

### 4.1 バリデーションエラー

#### Feature: PERSON入力バリデーション

#### Scenario: 名前が空の場合のバリデーションエラー

* Given（前提条件）:
  * PERSON入力画面が表示されている（新規追加モード）

* When（操作）:
  * "名前"フィールドを空のままにする
  * "年齢"フィールドに "40" を入力する
  * "性別"ラジオボタンで "男性" を選択する
  * "確認画面へ"ボタンをクリックする

* Then（期待結果）:
  * バリデーションエラーメッセージ "名前は必須です" が表示される
  * PERSON入力画面にとどまる（画面遷移しない）
  * PERSON確認画面に遷移しない
  * データベースは変更されない

#### Scenario: 名前が30文字を超える場合のバリデーションエラー

* Given（前提条件）:
  * PERSON入力画面が表示されている（新規追加モード）

* When（操作）:
  * "名前"フィールドに31文字の文字列を入力する
  * "年齢"フィールドに "40" を入力する
  * "性別"ラジオボタンで "男性" を選択する
  * "確認画面へ"ボタンをクリックする

* Then（期待結果）:
  * バリデーションエラーメッセージ "名前は30文字以内で入力してください" が表示される
  * PERSON入力画面にとどまる
  * データベースは変更されない

#### Scenario: 年齢が空の場合のバリデーションエラー

* Given（前提条件）:
  * PERSON入力画面が表示されている（新規追加モード）

* When（操作）:
  * "名前"フィールドに "David" を入力する
  * "年齢"フィールドを空のままにする
  * "性別"ラジオボタンで "男性" を選択する
  * "確認画面へ"ボタンをクリックする

* Then（期待結果）:
  * バリデーションエラーメッセージ "年齢は必須です" が表示される
  * PERSON入力画面にとどまる
  * データベースは変更されない

#### Scenario: 年齢が数値以外の場合のバリデーションエラー

* Given（前提条件）:
  * PERSON入力画面が表示されている（新規追加モード）

* When（操作）:
  * "名前"フィールドに "David" を入力する
  * "年齢"フィールドに "abc" を入力する（数値以外）
  * "性別"ラジオボタンで "男性" を選択する
  * "確認画面へ"ボタンをクリックする

* Then（期待結果）:
  * バリデーションエラーメッセージ "年齢は数値で入力してください" が表示される
  * PERSON入力画面にとどまる
  * データベースは変更されない

#### Scenario: 性別が選択されていない場合のバリデーションエラー

* Given（前提条件）:
  * PERSON入力画面が表示されている（新規追加モード）

* When（操作）:
  * "名前"フィールドに "David" を入力する
  * "年齢"フィールドに "40" を入力する
  * "性別"ラジオボタンを選択しない
  * "確認画面へ"ボタンをクリックする

* Then（期待結果）:
  * バリデーションエラーメッセージ "性別は必須です" が表示される
  * PERSON入力画面にとどまる
  * データベースは変更されない

---

### 4.2 削除確認キャンセル

#### Feature: PERSON削除確認ダイアログ

#### Scenario: 削除確認ダイアログでキャンセルをクリックする

* Given（前提条件）:
  * データベースにPERSONが3件存在する（Alice、Bob、Carol）
  * ブラウザでPERSON一覧画面（personList.xhtml）にアクセスしている

* When（操作）:
  * Bob（PERSON_ID=2）の"削除"リンクをクリックする
  * JavaScript削除確認ダイアログが表示される
  * キャンセルボタンをクリックする

* Then（期待結果）:
  * 削除は実行されない
  * PERSON一覧画面にとどまる
  * 一覧には3件のデータが表示される（Alice、Bob、Carol）
  * データベースは変更されない

---

## 5. 受入基準

### 5.1 機能要件

- [ ] すべての画面フローシナリオが成功する
- [ ] すべてのバリデーションシナリオが成功する
- [ ] すべてのエラーハンドリングシナリオが成功する

### 5.2 品質要件

- [ ] すべての画面が正しく表示される
- [ ] すべてのボタンが正しく動作する
- [ ] すべての画面遷移が正しく実行される
- [ ] すべてのエラーメッセージが正しく表示される
- [ ] データベースの状態が期待通りである

---

## 6. 参考資料

* [functional_design.md](functional_design.md) - 画面グループ機能設計書
* [screen_design.md](screen_design.md) - 画面設計書
* [../common/architecture_design.md](../common/architecture_design.md) - アーキテクチャ設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書
