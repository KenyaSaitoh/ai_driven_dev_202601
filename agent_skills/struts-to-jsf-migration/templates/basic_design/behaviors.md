# [SCREEN_GROUP_NAME] - 画面グループ振る舞い仕様書（E2Eテスト用）

テンプレートパス: templates/basic_design/behaviors.md  
コピー先: {spec_directory}/basic_design/[SCREEN_GROUP_NAME]/behaviors.md  
画面グループ名: [SCREEN_GROUP_NAME]  
バージョン: 1.0.0  
最終更新日: [DATE]

記法: シナリオは Gherkin 記法で記述する（Feature, Scenario, Given, When, Then, And, But）。principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照すること。

DBUnit対応:
* Given句: DB初期状態を明記（テーブル名、件数、データ、対応データセット）
* Then句: DB更新後の期待状態を明記（テーブル名、件数、差分、データ、対応データセット）
* 検証条件: 主キー、外部キー、CASCADE、トランザクションロールバックを明記

---

## 1. 概要

本文書は、[SCREEN_GROUP_NAME]画面グループの基本設計を外形的に捉えた振る舞い仕様書である。E2Eテスト用のシナリオを記述し、複数画面にまたがる画面フローの受入基準を定義する。

DBUnitによるテストデータ管理:
* テストデータは XML/CSV 形式で外部管理（`src/test/resources/datasets/[SCREEN_GROUP_NAME]/`配下）
* Given句でDB初期状態を明記、対応するXMLデータセットを参照
* Then句でDB更新後の期待状態を明記、対応するXMLデータセットを参照
* データベースの状態を明示的に検証

テスト対象:
* 画面グループ内の複数画面にまたがるフロー
* 画面遷移と画面間データ受け渡し
* Managed Bean + Service + Dao + Entity + DB の連携
* 実際のブラウザ操作（Playwright）
* ユーザー操作から結果表示までのエンドツーエンドフロー

テスト対象外:
* 単一メソッドレベルのテスト → detailed_design/{screen_group}/behaviors.mdで記述（単体テスト）

関連ドキュメント:
* [../../requirements/behaviors.md](../../requirements/behaviors.md) - システム全体の振る舞い仕様書（要件レベル）
* [../../requirements/requirements.md](../../requirements/requirements.md) - 要件定義書
* [functional_design.md](functional_design.md) - 画面グループ機能設計書
* [screen_design.md](screen_design.md) - 画面設計書
* [../common/architecture_design.md](../common/architecture_design.md) - アーキテクチャ設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書

---

## 2. 画面グループフロー全体シナリオ

### 2.1 [Feature名1] - [機能説明]

#### Feature: [機能名]

[画面グループの主要機能を記述]

#### Scenario: [シナリオタイトル - 正常系]

* Given（前提条件）:
  * データベースに[エンティティ]が存在する
  * [その他の前提条件]

* When（操作）:
  * ブラウザで [画面1] にアクセスする
  * [操作1] を実行する（例: "新規追加"ボタンをクリック）
  * [画面2] に遷移する
  * [フィールド1] に "[値1]" を入力する
  * [フィールド2] に "[値2]" を入力する
  * [操作2] を実行する（例: "確認画面へ"ボタンをクリック）
  * [画面3] に遷移する
  * 入力内容を確認する
  * [操作3] を実行する（例: "登録"ボタンをクリック）

* Then（期待結果）:
  * [画面1] に遷移する（リダイレクト）
  * 成功メッセージ "[メッセージ]" が表示される
  * 新しい[エンティティ]が一覧に表示される
  * データベースに新しい[エンティティ]が登録される

* And（追加の検証）:
  * [追加の検証項目]

#### DBUnit対応テストデータ

DB初期状態:
```gherkin
Given DBに以下のデータが存在する:
  テーブル: [TABLE_NAME]
  件数: N件
  データセット: /datasets/[SCREEN_GROUP_NAME]/initial-[scenario].xml
  データ:
    | COLUMN1 | COLUMN2 | COLUMN3 |
    | 値1     | 値2     | 値3     |
```

DB更新後の期待状態:
```gherkin
Then DBのテーブルは以下になる:
  テーブル: [TABLE_NAME]
  件数: M件（±差分）
  データセット: /datasets/[SCREEN_GROUP_NAME]/expected-[scenario].xml
  データ:
    | COLUMN1 | COLUMN2 | COLUMN3 |
    | 値1     | 値2     | 値3     |
  検証:
    - [主キー・外部キー・制約の検証条件]
```

---

### 2.2 [Feature名2] - [機能説明 - 異常系/エラーケース]

#### Feature: [機能名]

[エラーケースの機能を記述]

#### Scenario: [シナリオタイトル - バリデーションエラー]

* Given（前提条件）:
  * [前提条件]

* When（操作）:
  * ブラウザで [画面1] にアクセスする
  * [操作1] を実行する
  * [画面2] に遷移する
  * [フィールド1] に無効な値 "[無効な値]" を入力する
  * [操作2] を実行する

* Then（期待結果）:
  * バリデーションエラーメッセージ "[エラーメッセージ]" が表示される
  * [画面2] にとどまる（画面遷移しない）
  * データベースは変更されない

* And（追加の検証）:
  * [追加の検証項目]

---

## 3. 画面別シナリオ

### 3.1 [画面1] - [画面名1]

#### Feature: [機能名]

#### Scenario: [シナリオタイトル - 画面表示]

* Given（前提条件）:
  * データベースに[エンティティ]が [件数] 件存在する

* When（操作）:
  * ブラウザで [画面1] にアクセスする

* Then（期待結果）:
  * [画面1] が表示される
  * [エンティティ]一覧が表示される
  * 一覧には [件数] 件のデータが表示される
  * データは [ソート順] でソートされている

#### Scenario: [シナリオタイトル - ボタンクリック]

* Given（前提条件）:
  * [画面1] が表示されている

* When（操作）:
  * "[ボタン名]" ボタンをクリックする

* Then（期待結果）:
  * [画面2] に遷移する
  * [期待される画面状態]

---

### 3.2 [画面2] - [画面名2]

#### Feature: [機能名]

#### Scenario: [シナリオタイトル - 入力と確認]

* Given（前提条件）:
  * [画面2] が表示されている（新規追加モード）

* When（操作）:
  * [フィールド1] に "[値1]" を入力する
  * [フィールド2] に "[値2]" を入力する
  * "[確認画面へ]" ボタンをクリックする

* Then（期待結果）:
  * [画面3] に遷移する
  * 入力した [フィールド1] の値 "[値1]" が表示される
  * 入力した [フィールド2] の値 "[値2]" が表示される

---

### 3.3 [画面3] - [画面名3]

#### Feature: [機能名]

#### Scenario: [シナリオタイトル - 登録実行]

* Given（前提条件）:
  * [画面3] が表示されている
  * 確認内容: [フィールド1] = "[値1]"、[フィールド2] = "[値2]"

* When（操作）:
  * "[登録]" ボタンをクリックする

* Then（期待結果）:
  * [画面1] にリダイレクトされる
  * 成功メッセージ "[メッセージ]" が表示される
  * 新しい[エンティティ]が一覧に表示される
  * データベースに新しいレコードが登録される

---

## 4. エラーハンドリングシナリオ

### 4.1 バリデーションエラー

#### Feature: [機能名]

#### Scenario: [シナリオタイトル - 必須入力エラー]

* Given（前提条件）:
  * [画面2] が表示されている

* When（操作）:
  * [フィールド1] を空のままにする
  * "[確認画面へ]" ボタンをクリックする

* Then（期待結果）:
  * バリデーションエラーメッセージ "[エラーメッセージ]" が表示される
  * [画面2] にとどまる
  * [画面3] に遷移しない

---

### 4.2 ビジネスルールエラー

#### Feature: [機能名]

#### Scenario: [シナリオタイトル - 重複チェックエラー]

* Given（前提条件）:
  * データベースに [フィールド1] = "[値1]" の[エンティティ]が既に存在する

* When（操作）:
  * [画面2] で [フィールド1] に "[値1]" を入力する
  * "[確認画面へ]" ボタンをクリックする
  * [画面3] で "[登録]" ボタンをクリックする

* Then（期待結果）:
  * エラーメッセージ "[エラーメッセージ]" が表示される
  * [画面2] に戻る
  * データベースは変更されない

---

## 5. セキュリティシナリオ（該当する場合）

### 5.1 認証シナリオ

#### Feature: [機能名]

#### Scenario: [シナリオタイトル - 未認証アクセス]

* Given（前提条件）:
  * ユーザーがログインしていない

* When（操作）:
  * [画面1] にアクセスする

* Then（期待結果）:
  * ログイン画面にリダイレクトされる
  * エラーメッセージ "[エラーメッセージ]" が表示される

---

### 5.2 認可シナリオ

#### Feature: [機能名]

#### Scenario: [シナリオタイトル - 権限不足]

* Given（前提条件）:
  * ユーザーがログインしている
  * ユーザーの役割は "[役割]"（権限不足）

* When（操作）:
  * [画面1] で "[削除]" ボタンをクリックする

* Then（期待結果）:
  * エラーメッセージ "[エラーメッセージ]" が表示される
  * 削除は実行されない
  * データベースは変更されない

---

## 6. 受入基準

### 6.1 機能要件

- [ ] すべての画面フローシナリオが成功する
- [ ] すべてのバリデーションシナリオが成功する
- [ ] すべてのエラーハンドリングシナリオが成功する
- [ ] すべてのセキュリティシナリオが成功する（該当する場合）

### 6.2 品質要件

- [ ] すべての画面が正しく表示される
- [ ] すべてのボタンが正しく動作する
- [ ] すべての画面遷移が正しく実行される
- [ ] すべてのエラーメッセージが正しく表示される
- [ ] データベースの状態が期待通りである

---

## 7. DBUnitデータセット対応表

| シナリオ | 初期データセット | 期待データセット | 検証テーブル |
|---------|----------------|----------------|------------|
| [シナリオ1] | `/datasets/[SCREEN_GROUP_NAME]/initial-[scenario1].xml` | `/datasets/[SCREEN_GROUP_NAME]/expected-[scenario1].xml` | TABLE1<br>TABLE2 |
| [シナリオ2] | `/datasets/[SCREEN_GROUP_NAME]/initial-[scenario2].xml` | `/datasets/[SCREEN_GROUP_NAME]/expected-[scenario2].xml` | TABLE1 |
| [エラーケース] | `/datasets/[SCREEN_GROUP_NAME]/initial-error.xml` | （変更なし・ロールバック） | TABLE1 |

データセット配置ルール:
* ディレクトリ: `src/test/resources/datasets/[SCREEN_GROUP_NAME]/`
* 命名規則:
  * 初期データ: `initial-{scenario-name}.xml`
  * 期待データ: `expected-{scenario-name}.xml`
  * 共通マスター: `common-master-data.xml`

データセット記述例（XML）:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<dataset>
  <TABLE_NAME COLUMN1="値1" COLUMN2="値2" COLUMN3="[null]" />
  <TABLE_NAME COLUMN1="値3" COLUMN2="値4" COLUMN3="値5" />
</dataset>
```

---

## 8. 参考資料

* [functional_design.md](functional_design.md) - 画面グループ機能設計書
* [screen_design.md](screen_design.md) - 画面設計書
* [../../detailed_design/FUNC_XXX/behaviors.md](../../detailed_design/FUNC_XXX/behaviors.md) - 単体テスト用の振る舞い仕様書
* [../../requirements/behaviors.md](../../requirements/behaviors.md) - システム全体の振る舞い仕様書
* [../common/architecture_design.md](../common/architecture_design.md) - アーキテクチャ設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書
* DBUnit公式ドキュメント: http://dbunit.sourceforge.net/
* @agent_skills/struts-to-jsf-migration/instructions/it_generation.md - 結合テスト生成インストラクション
