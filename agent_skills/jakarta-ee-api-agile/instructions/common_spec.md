# 業務共通SPEC 作成インストラクション（アジャイル）

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力（例: specs/baseline）"
```

* 例: projects/sdd-agile/bookstore/berry-books-api
```yaml
project_root: "projects/sdd-agile/bookstore/berry-books-api"
spec_directory: "projects/sdd-agile/bookstore/berry-books-api/specs/baseline"
```

注意
* パス区切りはOS環境に応じて調整する（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換える
* 以降、`{spec_directory}` と表記されている箇所は、上記で設定した値に置き換える

---

## 概要

このインストラクションは、アジャイル版プロジェクトの業務共通SPEC（common/）を作成・更新するためのものである。

重要な方針
* common/ には data_model.md, external_interface.md, architecture_design.md の3ファイルのみを配置する
* functional_design.md は作らない（機能は各ユースケースの userstory.md / behaviors.md で定義する）
* プロダクトバックログや既存資料（ウォーターフォール用 basic_design 等）があれば、対話的に業務共通SPECの内容を埋める
* 既存の basic_design から業務共通SPEC用3ファイルへ移行する場合は、basic_design の同名ファイルを common/ にコピー・整理してよい

作成するSPEC

{spec_directory}/common/ 配下:
* data_model.md - 共通データモデル（ERD、テーブル定義、リレーション）
* external_interface.md - 共通外部インターフェース（本システムが呼び出す外部API）
* architecture_design.md - 共通アーキテクチャ設計（技術スタック、レイヤー、パッケージ、セキュリティ等）

---

## 1. 前提条件の確認

### 1.1 Agent Skillsルール（最優先で確認）

* @agent_skills/jakarta-ee-api-agile/principles/ - Jakarta EE開発の原則、アーキテクチャ標準、セキュリティ標準を確認する
  * このフォルダ配下の原則ドキュメントを読み込み、共通ルールを遵守すること
  * プロジェクト固有のルールがある場合は `{project_root}/principles/` も確認すること

### 1.2 既存SPECの確認（移行時）

* {spec_directory}/basic_design/ が存在する場合（ウォーターフォール用）:
  * data_model.md, external_interface.md, architecture_design.md を読み、業務共通SPEC（common/）用にコピー・整理するかユーザーに確認する
* common/ が既に存在する場合:
  * 既存の業務共通SPEC（common/）の3ファイルを更新するか、新規作成するかユーザーに確認する

---

## 2. テンプレートの展開

### 2.1 業務共通SPEC用テンプレートの展開

@agent_skills/jakarta-ee-api-agile/templates/common/ 配下のテンプレートファイルを {spec_directory}/common/ にコピーする

コピー対象ファイル:
* data_model.md - データモデル仕様書
* external_interface.md - 外部インターフェース仕様書
* architecture_design.md - アーキテクチャ設計書

注意
* 既に業務共通SPEC（common/）にファイルが存在する場合は、ユーザーに「上書きしますか？」と確認する
* 既存の basic_design から移行する場合は、コピー後にプレースホルダーを既存内容で置き換える

### 2.2 ディレクトリ構造の確認

展開後のディレクトリ構造:

```
{spec_directory}/
├── common/                    # 業務共通SPEC（本インストラクションの成果物）
│   ├── data_model.md
│   ├── external_interface.md
│   └── architecture_design.md
└── usecases/                  # ユースケース単位（別インストラクションで作成）
    └── {usecase_name}/
        ├── userstory.md
        └── behaviors.md
```

---

## 3. 対話による業務共通SPECの作成・更新

### 3.1 architecture_design.md

* 既存資料（basic_design/architecture_design.md 等）がある場合: 内容を読み、common/architecture_design.md に反映する
* 既存資料がない場合: テンプレートを開き、ユーザーと対話しながら技術スタック、レイヤー、パッケージ構造、セキュリティ、トランザクション、エラーハンドリング等を埋める
* 参照先の原則は agent_skills/jakarta-ee-api-agile/principles/ にすること

### 3.2 data_model.md

* 既存資料がある場合: テーブル定義、ER図、制約、リレーションを common/data_model.md に反映する
* 既存資料がない場合: 対話でエンティティ（テーブル）、カラム、PK/FK、インデックス、整合性ルールを定義する
* RDB論理設計のみ記述する。JPAエンティティクラス設計は詳細設計フェーズで実施する

### 3.3 external_interface.md

* 本システムが呼び出す外部システムがある場合: 連携一覧、エンドポイント、認証方式、リクエスト/レスポンスを記述する
* 外部連携が不要な場合: 「本システムは外部システムを呼び出さない」と記載し、必要最小限のセクションのみ残す

---

## 4. SPECの検証

* 業務共通SPECの3ファイル間の整合性を確認する（data_model のテーブルと architecture の永続化方針、external_interface と architecture の連携方針）
* プレースホルダー（[PROJECT_NAME]、[DATE] 等）が残っていないか確認する
* @agent_skills/jakarta-ee-api-agile/principles/common_rules.md の Markdown フォーマット規約に従っているか確認する

---

## 5. 完了報告

ユーザーに以下を報告する

* 作成・更新した業務共通SPECの一覧: data_model.md, external_interface.md, architecture_design.md のパス
* 次のステップの案内: ユースケースSPEC作成（usecase_spec.md）、または実装（必要なら task_breakdown.md で tasks/ を用意してから code_generation.md）は業務共通SPEC完了後に行う

---

## 参考

* [usecase_spec.md](usecase_spec.md) - ユースケースSPEC作成
* [SKILL.md](../SKILL.md) - 本スキル全体の使い方
