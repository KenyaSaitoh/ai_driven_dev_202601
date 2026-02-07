---
name: jakarta-ee-api-agile
description: Jakarta EE 10とJAX-RS 3.1を使ったREST APIのアジャイル・仕様駆動開発を支援。業務共通SPEC（common/）先行、ユースケース単位の開発。タスクは common と各 usecases で既に決まっており、各人がそれに従って実装。合流ポイントは結合テスト。
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

### ステップ4: コード生成

```
@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md

指定した対象（common または usecases/{名}）のコードを生成してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス（オプション、デフォルト: {project_root}/specs/baseline）>
* target: common または usecases/<フォルダ名>（例: usecases/order-creation）
* skip_infrastructure: <setup 時のみ、オプション>
```

AIが自動で以下を実行
1. target に応じて common 用かユースケース用かを判別
2. target=common: common/ の3SPECを参照して実装
3. target=usecases/{名}: common/ + usecases/{名}/userstory.md, behaviors.md を参照して実装
4. 単体テストを生成

### ステップ5: 単体テスト実行評価

```
@agent_skills/jakarta-ee-api-agile/instructions/unit_test_execution.md

単体テストを実行してください

パラメータ
* project_root: <プロジェクトルートパス>
* target: common または usecases/{名}
```

### ステップ6: 結合テスト・E2Eテスト生成（合流ポイント）

業務共通と各ユースケースの実装は、ここで合流する。結合テストで common + 全ユースケースが連携して動くことを検証する。

```
@agent_skills/jakarta-ee-api-agile/instructions/it_generation.md
@agent_skills/jakarta-ee-api-agile/instructions/e2e_test_generation.md
```

common / usecases 配下の behaviors を参照して結合・E2Eテストを生成

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
