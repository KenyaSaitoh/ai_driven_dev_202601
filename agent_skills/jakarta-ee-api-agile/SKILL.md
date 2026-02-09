---
name: jakarta-ee-api-agile
description: Jakarta EE 10とJAX-RS 3.1を使ったREST APIのアジャイル・仕様駆動開発を支援。業務共通SPEC（common/）先行、ユースケース単位の開発。本番コードとテストコードの生成を分離し、ブラックボックス・ホワイトボックス両方の観点からテストを実装。タスクは common と各 usecases で既に決まっており、各人がそれに従って実装。合流ポイントは結合テスト。
---

# Jakarta EE API アジャイル開発 Agent Skill

## 使い方（アジャイル・業務共通先行・ユースケース単位）

### 対象プロジェクト

* アジャイル版の対象: `projects/sdd-agile/bookstore/` 配下（berry-books-api, back-office-api 等）
* SPEC配置: `specs/baseline/common/`（共通）と `specs/baseline/usecases/{フォルダ名}/`（ユースケース単位）
* 既存のウォーターフォール用SPEC（basic_design/, requirements/）は、本スキルの駆動元としては使用しない。common + usecases 構成を前提とする。

### ステップ1: 業務共通SPEC 作成

```
@agent_skills/jakarta-ee-api-agile/instructions/common_spec.md

業務共通SPEC（common/）を作成してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス（例: specs/baseline）>
```

AIと対話しながら以下を実施
1. テンプレートを `{spec_directory}/common/` に展開
   * @agent_skills/jakarta-ee-api-agile/templates/common/ から3ファイルをコピー
2. common/ に data_model.md, external_interface.md, architecture_design.md を作成・更新
3. 共通のデータモデル・外部IF・アーキテクチャを定義（functional_design は作らない）

### ステップ2: ユースケース SPEC 作成

```
@agent_skills/jakarta-ee-api-agile/instructions/usecase_spec.md

ユースケースSPECを作成してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
* usecase_folder: <ユースケースフォルダ名（例: order-creation）>
```

AIと対話しながら以下を実施
1. common/ の3SPECを読み、矛盾しないようにユースケースSPECを作成
2. テンプレートを `{spec_directory}/usecases/{usecase_folder}/` に展開
   * @agent_skills/jakarta-ee-api-agile/templates/usecases/ から2ファイルをコピー
3. userstory.md, behaviors.md を作成・更新

### ステップ3: 実装（common → 各ユースケース）

タスクは既に業務共通SPEC（common/）と各ユースケースSPEC（usecases/{名}/）で決まっている。各人がそれに従ってコード生成を実行すればよい。アジャイルでは、既存コードに対して code_generation を何度でも再実行し、SPEC に基づく差分を漸進的に反映していく。合流ポイントは結合テスト（ステップ6）である。

### ステップ4: 本番コード生成

```
@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md

指定した対象（common または usecases/{名}）の本番コードを生成してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス（オプション、デフォルト: {project_root}/specs/baseline）>
* target: common または usecases/<フォルダ名>（例: usecases/order-creation）
* skip_infrastructure: <setup 時のみ、オプション>
```

AIが自動で以下を実行
1. target に応じて common 用かユースケース用かを判別
2. target=common: common/ の3SPECを参照して本番コード実装
3. target=usecases/{名}: common/ + usecases/{名}/userstory.md, behaviors.md を参照して本番コード実装
4. 既存コードがある場合は、削除せずに差分のみを反映する

注意: 単体テストコード生成は次のステップ（ステップ5）で実施

### ステップ5: 単体テストコード生成

```
@agent_skills/jakarta-ee-api-agile/instructions/unit_test_generation.md

単体テストコードを生成してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス（オプション、デフォルト: {project_root}/specs/baseline）>
* target: common または usecases/{名}
```

AIが自動で以下を実行
1. target に応じた SPEC（common または usecases/{名}/behaviors.md）を読み込む
2. 本番コード（ステップ4で生成）に対応する単体テストコードを生成
   * **ブラックボックステスト**: behaviors.mdのGherkinシナリオから外形的な振る舞いをテスト
   * **ホワイトボックステスト**: 境界値・エッジケース・分岐パスをカバーするテスト
   * 既存テストがある場合は、削除せずに差分のみを反映する

重要: テスト生成のみを実施（テスト実行は手動で ./gradlew test を実行）

### ステップ6: テスト評価（単体/結合/E2E共通）

```
@agent_skills/jakarta-ee-api-agile/instructions/test_evaluation.md

テスト実行結果を評価してください

パラメータ
* project_root: <プロジェクトルートパス>
* jacoco_reports_dir: build/reports/jacoco/test
* test_type: unit  # unit, integration, e2e のいずれか
* spec_directory: <SPECディレクトリパス>
```

前提: テストは既に実行済み（./gradlew test または integrationTest または e2eTest）

AIが自動で以下を実行
1. Jacocoレポート（XML）を読み込む
2. カバレッジ評価（行、分岐、メソッド）
3. パッケージ別/クラス別/メソッド別カバレッジ分析
4. デッドコード検出
5. テスト品質評価（テスト数、失敗分析）
6. 評価レポート生成
7. ユーザーに推奨アクションを提示

重要: テスト実行は不要（既に実行済みのレポートを評価）

### ステップ7: 結合テスト・E2Eテスト生成（合流ポイント）

業務共通と各ユースケースの実装は、ここで合流する。結合テストで common + 全ユースケースが連携して動くことを検証する。

```
@agent_skills/jakarta-ee-api-agile/instructions/it_generation.md

結合テストコードを生成してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
* usecase_folder: null  # オプション。指定時はそのユースケースのみ
```

common / usecases 配下の behaviors を参照して結合テストを生成

重要: テスト生成のみを実施（テスト実行は手動で ./gradlew integrationTest を実行）

```
@agent_skills/jakarta-ee-api-agile/instructions/e2e_test_generation.md

E2Eテストコードを生成してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
```

common / usecases 配下の behaviors を参照してE2Eテストを生成

重要: テスト生成のみを実施（テスト実行は手動で ./gradlew e2eTest を実行。アプリケーションサーバー起動が前提）

テスト実行後、ステップ6（test_evaluation.md）でtest_type=integration または e2eとして評価

SPEC の更新について
* アジャイルでは基本設計SPECの「変更管理」は行わない。common または usecases/{名} の SPEC を編集したうえで、code_generation.md を target 指定で再実行し、既存コードへ差分を反映すればよい（spec_change は不要）

---

## ディレクトリ構造（アジャイル）

```
specs/baseline/
├── common/                          # 業務共通SPEC（先に作成）
│   ├── data_model.md
│   ├── external_interface.md
│   └── architecture_design.md
└── usecases/                        # ユースケース単位
    ├── <usecase_name_1>/
│   │   ├── userstory.md
│   │   └── behaviors.md
│   └── <usecase_name_2>/
│       ├── userstory.md
│       └── behaviors.md
```

```
agent_skills/jakarta-ee-api-agile/
├── SKILL.md
├── README.md
├── principles/
│   ├── architecture.md
│   ├── common_rules.md
│   └── security.md
├── templates/
│   ├── common/
│   │   ├── data_model.md
│   │   ├── external_interface.md
│   │   └── architecture_design.md
│   └── usecases/
│       ├── userstory.md
│       └── behaviors.md
└── instructions/
    ├── common_spec.md
    ├── usecase_spec.md
    ├── code_generation.md
    ├── unit_test_execution.md
    ├── it_generation.md
    └── e2e_test_generation.md
```

---

## 参考

* [開発原則](principles/) - アーキテクチャ標準、セキュリティ標準、共通ルール
* ウォーターフォール版: [jakarta-ee-api-base](agent_skills/jakarta-ee-api-base/SKILL.md)
