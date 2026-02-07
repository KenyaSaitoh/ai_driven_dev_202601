# ユースケース SPEC 作成インストラクション（アジャイル）

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力（例: specs/baseline）"
usecase_folder: "ここにユースケースフォルダ名を入力（例: order-creation）"
```

* 例: projects/sdd-agile/bookstore/berry-books-api、ユースケース order-creation
```yaml
project_root: "projects/sdd-agile/bookstore/berry-books-api"
spec_directory: "projects/sdd-agile/bookstore/berry-books-api/specs/baseline"
usecase_folder: "order-creation"
```

注意
* パス区切りはOS環境に応じて調整する
* 以降、`{project_root}`, `{spec_directory}`, `{usecase_folder}` は、上記で設定した値に置き換える

---

## 概要

このインストラクションは、アジャイル版プロジェクトのユースケース単位SPECを作成・更新するためのものである。

重要な方針
* 必ず先に {spec_directory}/common/ の data_model.md, external_interface.md, architecture_design.md を読み、共通仕様と矛盾しないようにユースケースSPECを作成する
* 1ユースケース = 1フォルダ。フォルダ名は任意（例: order-creation, book-search, auth）
* 各ユースケースフォルダには userstory.md と behaviors.md を配置する
* ユーザーストーリー・受入基準・振る舞い（Gherkin）を、業務共通SPECの3SPECと整合させて記述する

作成するSPEC

{spec_directory}/usecases/{usecase_folder}/ 配下:
* userstory.md - ユーザーストーリー、受入基準、API仕様（該当時）、ビジネスルール
* behaviors.md - 振る舞い・テストシナリオ（Gherkin記法）。結合テスト・E2Eテストの参照元

---

## 1. 前提条件の確認

### 1.1 業務共通SPEC の存在確認

* {spec_directory}/common/data_model.md, external_interface.md, architecture_design.md が存在することを確認する
* 存在しない場合は、ユーザーに「先に業務共通SPECを作成してください（common_spec.md を実行）」と伝える

### 1.2 Agent Skillsルール（最優先で確認）

* @agent_skills/jakarta-ee-api-agile/principles/ を確認し、共通ルールを遵守する

---

## 2. 業務共通SPEC の読み込み

ユースケースSPECを書く前に、以下を読み共通仕様を把握する

* {spec_directory}/common/architecture_design.md - 技術スタック、レイヤー、パッケージ、セキュリティ
* {spec_directory}/common/data_model.md - テーブル定義、ERD、リレーション（本ユースケースで使用するエンティティを特定）
* {spec_directory}/common/external_interface.md - 外部連携（本ユースケースで呼び出す外部APIがあれば把握）

---

## 3. ユースケースフォルダの作成とテンプレート展開

### 3.1 フォルダ作成

* {spec_directory}/usecases/{usecase_folder}/ が存在しなければ作成する

### 3.2 テンプレートのコピー

* @agent_skills/jakarta-ee-api-agile/templates/usecases/userstory.md を {spec_directory}/usecases/{usecase_folder}/userstory.md にコピーする
* @agent_skills/jakarta-ee-api-agile/templates/usecases/behaviors.md を {spec_directory}/usecases/{usecase_folder}/behaviors.md にコピーする
* 既にファイルが存在する場合は、更新するか上書きするかユーザーに確認する

---

## 4. 対話による userstory.md の作成・更新

### 4.1 ユーザーストーリー

* 「As a / I want to / So that」形式で記述する
* 業務共通SPECのスコープ（データモデル・外部IF・アーキテクチャ）の範囲内で実現可能なストーリーにする

### 4.2 受入基準

* AC1, AC2, ... と番号付けし、検証可能な条件を書く
* 後の behaviors.md のシナリオと対応させる

### 4.3 API仕様（該当する場合）

* このユースケースが提供するエンドポイント（メソッド、パス、説明）を一覧する
* リクエスト/レスポンス構造、バリデーション、エラーコードは業務共通SPECの data_model / external_interface と矛盾しないようにする

### 4.4 ビジネスルール

* 本ユースケースに固有のビジネスルールを列挙する

---

## 5. 対話による behaviors.md の作成・更新

### 5.1 振る舞いの記法

* Gherkin 記法（Feature, Scenario, Given, When, Then）で記述する
* @agent_skills/jakarta-ee-api-agile/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照する

### 5.2 シナリオ

* userstory.md の受入基準に対応するシナリオを書く
* 正常系・異常系（バリデーションエラー、ビジネスエラー、認証エラー等）を含める
* 結合テスト・E2Eテスト生成時に、この behaviors.md が参照元となることを意識する

### 5.3 受入基準との対応表

* 受入基準とシナリオの対応を表で明示する（任意だが推奨）

---

## 6. 検証と完了報告

* userstory.md と behaviors.md の内容が一致しているか確認する
* 業務共通SPECの3SPECとの矛盾がないか確認する
* ユーザーに作成・更新したファイルのパスと、次のステップ（実装、または任意で実装タスク一覧の作成）を案内する

---

## 参考

* [common_spec.md](common_spec.md) - 業務共通SPEC作成
* [code_generation.md](code_generation.md) - コード生成
