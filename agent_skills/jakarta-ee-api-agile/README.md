# Jakarta EE API アジャイル開発 Agent Skill

最終更新日: 2026-02-01

---

## 概要

Jakarta EE 10とJAX-RS 3.1を使ったREST APIのアジャイル・仕様駆動開発を支援するAgent Skillです。

業務共通SPEC（common/）を先行して作成し、ユースケース単位（usecases/{名}/）でSPECと実装を進めます。タスクは common と各ユースケースで既に決まっており、各人がそれに従ってコード生成を実行します。合流ポイントは結合テストです。振る舞い仕様（behaviors.md）は Gherkin 記法で記述し、単体・結合・E2Eテストは Cucumber（.feature + ステップ定義）で生成する前提です。アジャイルでは基本設計SPECの「変更管理」は行わず、SPEC を編集したうえで code_generation を target 指定で再実行し、既存コードへ差分を漸進的に反映する運用です。

対象プロジェクト例: `projects/sdd-agile/bookstore/berry-books-api`, `projects/sdd-agile/bookstore/back-office-api`

## クイックスタート

1. 業務共通SPEC作成: `@agent_skills/jakarta-ee-api-agile/instructions/common_spec.md` で common/ に3SPECを作成
2. ユースケースSPEC作成: `@agent_skills/jakarta-ee-api-agile/instructions/usecase_spec.md` で各ユースケースに userstory.md, behaviors.md を作成
3. 実装: `code_generation.md` で target=common または target=usecases/{名} を指定してコード生成
4. 合流: 結合テストで common + 全ユースケースを検証

---

## フォルダ構造

```
agent_skills/jakarta-ee-api-agile/
│
├── SKILL.md                                    # Agent Skill説明書（エントリポイント）
│
├── instructions/                               # 開発インストラクション
│   │
│   ├── common_spec.md                          # ステップ1: 業務共通SPEC作成
│   │   └─→ 遵守: principles/common_rules.md
│   │   └─→ 読込: 既存資料（basic_design 等、移行時）
│   │   └─→ 出力: {spec_directory}/common/
│   │              ├── data_model.md
│   │              ├── external_interface.md
│   │              └── architecture_design.md
│   │
│   ├── usecase_spec.md                         # ステップ2: ユースケースSPEC作成
│   │   └─→ 遵守: principles/common_rules.md
│   │   └─→ 読込: {spec_directory}/common/（3SPEC）
│   │   └─→ 出力: {spec_directory}/usecases/{usecase_folder}/
│   │              ├── userstory.md
│   │              └── behaviors.md
│   │
│   ├── code_generation.md                      # ステップ3: コード生成（本番＋単体テスト）
│   │   └─→ 遵守: principles/common_rules.md
│   │   └─→ 読込: {spec_directory}/common/（target=common 時）
│   │   │         {spec_directory}/usecases/{名}/（target=usecases/{名} 時）
│   │   └─→ 出力: {project_root}/src/（コード・テスト）
│   │
│   ├── unit_test_execution.md                  # ステップ4: 単体テスト実行評価
│   │   └─→ 実行: ./gradlew test jacocoTestReport（プロジェクトの build.gradle に従う）
│   │   └─→ 分析: テスト結果、カバレッジ、未カバーコード
│   │   └─→ 出力: フィードバックレポート
│   │
│   ├── it_generation.md                        # ステップ5: 結合テスト生成（Cucumber + Weld SE）
│   │   └─→ 遵守: principles/common_rules.md
│   │   └─→ 読込: {spec_directory}/usecases/ 配下の behaviors.md（Gherkin）
│   │   └─→ 出力: {project_root}/src/test/（.feature + ステップ定義、integration/）
│   │   └─→ 特徴: common + 全ユースケースの連携を検証、実DB、外部APIはWireMock
│   │
│   └── e2e_test_generation.md                  # ステップ6: E2Eテスト生成（Cucumber + REST Assured）
│       └─→ 遵守: principles/common_rules.md
│       └─→ 読込: {spec_directory}/usecases/ 配下の behaviors.md（Gherkin）
│       └─→ 出力: {project_root}/src/test/（.feature + ステップ定義、e2e/）
│       └─→ 特徴: 複数ユースケース間連携、実HTTP・実DB
│
├── principles/                                 # 開発原則（全プロジェクト共通）
│   ├── common_rules.md                          # Jakarta EE開発ルール
│   ├── architecture.md                         # Jakarta EE APIアーキテクチャ標準
│   └── security.md                             # セキュリティ標準ガイドライン
│
└── templates/                                  # SPECテンプレート
    ├── common/                                 # 業務共通用
    │   ├── architecture_design.md
    │   ├── data_model.md
    │   └── external_interface.md
    └── usecases/
        └── _sample/
            ├── userstory.md
            └── behaviors.md
```

---

## プロジェクトフォルダ構造

このAgent Skillを使用して開発するプロジェクトの標準フォルダ構造です。ウォーターフォール版の requirements/、basic_design/、tasks/、detailed_design/ は使用しません。

```
{project_root}/                                # プロジェクトルートディレクトリ
│
├── README.md                                  # プロジェクト概要、セットアップ手順
│
├── specs/                                     # 仕様書ディレクトリ
│   └── baseline/                              # ベースライン仕様
│       │
│       ├── common/                            # 業務共通SPEC（先に作成）
│       │   ├── architecture_design.md        # アーキテクチャ設計書
│       │   ├── data_model.md                 # データモデル仕様書
│       │   └── external_interface.md        # 外部インターフェース仕様書
│       │
│       └── usecases/                          # ユースケース単位
│           ├── order-creation/                # 例: 注文作成
│           │   ├── userstory.md              # ユーザーストーリー、受入基準
│           │   └── behaviors.md              # 振る舞い仕様（Gherkin、結合・E2Eテスト用）
│           ├── book-search/                    # 例: 書籍検索
│           │   ├── userstory.md
│           │   └── behaviors.md
│           └── （その他ユースケース）/
│               ├── userstory.md
│               └── behaviors.md
│
├── sql/                                       # データベーススクリプト
│   └── {database_type}/
│       ├── 01_schema.sql
│       └── 02_sample_data.sql
│
├── src/
│   ├── main/
│   │   ├── java/                              # 実装コード（api, service, dao, entity, dto 等）
│   │   ├── resources/
│   │   └── webapp/
│   │
│   └── test/
│       ├── java/                              # 単体・結合・E2Eテスト（ステップ定義等）
│       │   └── {package_structure}/
│       │       ├── （単体テスト）
│       │       ├── integration/               # 結合テスト（@Tag("integration")）
│       │       └── e2e/                       # E2Eテスト（@Tag("e2e")）
│       └── resources/
│           ├── META-INF/
│           └── features/                      # Cucumber .feature（Gherkin）
│               ├── unit/
│               ├── integration/
│               └── e2e/
│
├── build/                                     # ビルド成果物（Git除外）
│   └── reports/                               # テスト・カバレッジレポート
│
├── build.gradle
├── settings.gradle
└── .gitignore
```

### フォルダ構造の注意事項

1. **specs/baseline/common/** - 業務共通の唯一の真実の情報源
   * data_model.md, external_interface.md, architecture_design.md の3ファイルのみ。functional_design.md は作らない。

2. **specs/baseline/usecases/** - ユースケース単位
   * 1ユースケース = 1フォルダ。userstory.md と behaviors.md を配置。
   * behaviors.md は Gherkin 記法で記述し、結合テスト・E2Eテストの参照元となる。

3. **タスク分解・詳細設計は行わない**
   * tasks/ フォルダや detailed_design/ は使用しない。common と各ユースケースのSPECを直接 code_generation の駆動元とする。

4. **src/main/java/** - Jakarta EE 標準のレイヤードアーキテクチャ（api → service → dao → entity）。

5. **src/test/** - 単体（@Tag("unit")）、結合（@Tag("integration")）、E2E（@Tag("e2e")）。Cucumber .feature + ステップ定義。

---

## プロセス（アジャイル）

### ステップ1: 業務共通SPEC作成

目的: 共通のデータモデル・外部IF・アーキテクチャを定義する

インストラクション: `common_spec.md`

使い方:
```
@agent_skills/jakarta-ee-api-agile/instructions/common_spec.md

業務共通SPEC（common/）を作成してください

パラメータ:
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス（例: specs/baseline）>
```

AIと対話しながら実施:
1. テンプレートを {spec_directory}/common/ に展開
2. common/ に data_model.md, external_interface.md, architecture_design.md を作成・更新
3. 共通のデータモデル・外部IF・アーキテクチャを定義（functional_design は作らない）

注意:
* 既存の basic_design から移行する場合は、同名ファイルを common/ にコピー・整理してよい

生成されるファイル:
```
{spec_directory}/common/
├── architecture_design.md       # アーキテクチャ設計書
├── data_model.md                # データモデル仕様書
└── external_interface.md        # 外部インターフェース仕様書
```

---

### ステップ2: ユースケースSPEC作成

目的: ユースケース単位でユーザーストーリーと振る舞い仕様を作成する

インストラクション: `usecase_spec.md`

使い方:
```
@agent_skills/jakarta-ee-api-agile/instructions/usecase_spec.md

ユースケースSPECを作成してください

パラメータ:
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
* usecase_folder: <ユースケースフォルダ名（例: order-creation）>
```

AIと対話しながら実施:
1. common/ の3SPECを読み、矛盾しないようにユースケースSPECを作成
2. {spec_directory}/usecases/{usecase_folder}/ に userstory.md, behaviors.md を作成・更新
3. 振る舞いは Gherkin 記法で記述（Cucumber .feature の元になる）

生成されるファイル:
```
{spec_directory}/usecases/{usecase_folder}/
├── userstory.md                 # ユーザーストーリー、受入基準、API仕様
└── behaviors.md                 # 振る舞い仕様（Gherkin、結合・E2Eテスト用）
```

---

### ステップ3: コード生成（実装＋単体テスト）

目的: 指定した対象（common または usecases/{名}）の実装コードと単体テストを生成・更新する

インストラクション: `code_generation.md`

使い方:
```
@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md

指定した対象のコードを生成してください

パラメータ:
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>（オプション、デフォルト: {project_root}/specs/baseline）
* target: common または usecases/<フォルダ名>（例: usecases/order-creation）
* skip_infrastructure: true  # 初回セットアップ時のみ、DB/APサーバーをスキップ（オプション）
```

AIが自動で実行:
1. target に応じて common 用かユースケース用かを判別
2. target=common: common/ の3SPECを参照して実装
3. target=usecases/{名}: common/ + usecases/{名}/userstory.md, behaviors.md を参照して実装
4. 単体テストを生成（Cucumber .feature + ステップ定義の場合あり）
5. 既存コードがある場合は、SPEC に基づく差分を漸進的に反映（ゼロから全体を再生成しない）

重要:
* 指定された target のみを実行し、完了したら停止する
* 仕様に曖昧点や判断に迷う点がある場合は、ユーザーに確認してからコード生成を進める

生成されるファイル:
```
{project_root}/src/
├── main/java/                   # Resource, Service, Dao, Entity, DTO 等
└── test/
    ├── java/.../                # 単体テスト（Cucumber ステップ定義等）
    └── resources/features/unit/ # Cucumber .feature（該当する場合）
```

---

### ステップ4: 単体テスト実行評価

目的: 単体テストを実行してカバレッジを分析し、フィードバックを生成する

インストラクション: `unit_test_execution.md`

使い方:
```
@agent_skills/jakarta-ee-api-agile/instructions/unit_test_execution.md

単体テストを実行してください

パラメータ:
* project_root: <プロジェクトルートパス>
* target: common または usecases/<フォルダ名>
```

AIが自動で実行:
1. テスト実行（`./gradlew test jacocoTestReport`。プロジェクトの build.gradle に従う）
2. テスト結果とカバレッジ分析
3. 問題の分類（テスト失敗、必要な振る舞い、デッドコード）
4. フィードバックレポート生成と推奨アクションの提示

出力: build/reports/ 配下（tests/test/, jacoco/test/, test-analysis/）

---

### ステップ5: 結合テスト生成（合流ポイント）

目的: common と各ユースケースの実装が連携して動くことを検証する結合テストを生成する

インストラクション: `it_generation.md`

使い方:
```
@agent_skills/jakarta-ee-api-agile/instructions/it_generation.md

結合テストを生成してください

パラメータ:
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
* usecase_folder: <ユースケースフォルダ名>（オプション。指定時はそのユースケースの結合テストのみ）
```

AIが自動で実行:
1. usecases/ 配下の behaviors.md（Gherkin）を読み込む（usecase_folder 指定時はそのフォルダのみ）
2. JUnit 5 + Cucumber（.feature + ステップ定義）+ Weld SE で結合テストを生成
   * Service層以下（Service + DAO + Entity）の実際の連携をテスト
   * 実際のDB（メモリDB）。外部APIは WireMock でスタブ化
3. `@Tag("integration")` で結合テストを分離

生成されるファイル:
```
{project_root}/src/test/
├── java/.../integration/
│   ├── BaseIntegrationTest.java
│   └── *IT.java
└── resources/features/integration/
    └── *.feature
```

---

### ステップ6: E2Eテスト生成

目的: 複数ユースケースにまたがるエンドツーエンドテストを生成する

インストラクション: `e2e_test_generation.md`

使い方:
```
@agent_skills/jakarta-ee-api-agile/instructions/e2e_test_generation.md

E2Eテストを生成してください

パラメータ:
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
* usecase_folder: <ユースケースフォルダ名>（オプション。指定時はそのユースケースのE2Eのみ）
```

AIが自動で実行:
1. usecases/ 配下の behaviors.md（Gherkin）を読み込む
2. JUnit 5 + Cucumber（.feature + ステップ定義）+ REST Assured でE2Eテストを生成
   * API層を含む全体フロー、実際のHTTPリクエスト/レスポンス、実DB
3. `@Tag("e2e")` でE2Eテストを分離

重要: アプリケーションサーバーが起動している状態で実行する

生成されるファイル:
```
{project_root}/src/test/
├── java/.../e2e/
│   ├── BaseE2ETest.java
│   └── *E2ETest.java
└── resources/features/e2e/
    └── *.feature
```

---

## SPECの更新について（アジャイル）

アジャイルでは、基本設計SPECの「変更管理」（CHANGES.md の作成・アーカイブ等）は行いません。

* common または usecases/{名} の SPEC を直接編集する
* 編集後、code_generation.md を target 指定で再実行し、既存コードへ差分を反映する
* 他 target のコードは破壊しない。変更が必要な箇所のみ漸進的に更新する

---

## ウォーターフォール版との違い

| 観点 | ウォーターフォール (jakarta-ee-api-base) | アジャイル (本スキル) |
|------|------------------------------------------|------------------------|
| SPEC配置 | baseline/basic_design/ 一枚岩 + requirements/ | baseline/common/ + baseline/usecases/{名}/ |
| 機能単位 | タスク分解で FUNC_XXX を抽出 | ユースケースフォルダで事前に単位を定義 |
| 駆動元 | functional_design + basic_design 全体 | common の3SPEC + 各 usecases/{名}/ の userstory + behaviors |
| 変更対応 | CHANGES.md で変更管理、変更タスク生成 | SPEC を直接編集し、code_generation を再実行して差分反映 |

---

## 実践例

### 例1: プロジェクト立ち上げ（アジャイル）

ステップ1: 業務共通SPEC作成
```
@agent_skills/jakarta-ee-api-agile/instructions/common_spec.md

業務共通SPECを作成してください

パラメータ:
* project_root: projects/sdd-agile/bookstore/berry-books-api
* spec_directory: projects/sdd-agile/bookstore/berry-books-api/specs/baseline
```

AIと対話しながら common/ の3SPECを作成

ステップ2: ユースケースSPEC作成（例: 注文作成）
```
@agent_skills/jakarta-ee-api-agile/instructions/usecase_spec.md

ユースケースSPECを作成してください

パラメータ:
* project_root: projects/sdd-agile/bookstore/berry-books-api
* spec_directory: projects/sdd-agile/bookstore/berry-books-api/specs/baseline
* usecase_folder: order-creation
```

ステップ3: コード生成（common から先行）
```
@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md

common のコードを生成してください

パラメータ:
* project_root: projects/sdd-agile/bookstore/berry-books-api
* target: common
```

ステップ4: コード生成（ユースケース）
```
@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md

注文作成ユースケースのコードを生成してください

パラメータ:
* project_root: projects/sdd-agile/bookstore/berry-books-api
* target: usecases/order-creation
```

ステップ5: 単体テスト実行評価
```
@agent_skills/jakarta-ee-api-agile/instructions/unit_test_execution.md

単体テストを実行してください

パラメータ:
* project_root: projects/sdd-agile/bookstore/berry-books-api
* target: usecases/order-creation
```

ステップ6: 結合テスト・E2Eテスト生成
```
@agent_skills/jakarta-ee-api-agile/instructions/it_generation.md

結合テストを生成してください

パラメータ:
* project_root: projects/sdd-agile/bookstore/berry-books-api
* spec_directory: projects/sdd-agile/bookstore/berry-books-api/specs/baseline
```

```
@agent_skills/jakarta-ee-api-agile/instructions/e2e_test_generation.md

E2Eテストを生成してください

パラメータ:
* project_root: projects/sdd-agile/bookstore/berry-books-api
* spec_directory: projects/sdd-agile/bookstore/berry-books-api/specs/baseline
```

実行方法（プロジェクトの build.gradle に定義されたタスクに従う）:
```bash
./gradlew integrationTest
./gradlew e2eTest   # アプリケーションサーバー起動後、別ターミナルで実行
```

---

### 例2: SPEC変更後の差分反映（アジャイル）

注文キャンセルを userstory に追加した場合の例。

1. specs/baseline/usecases/order-creation/userstory.md と behaviors.md を編集
2. code_generation を再実行
```
@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md

order-creation のコードを更新してください

パラメータ:
* project_root: projects/sdd-agile/bookstore/berry-books-api
* target: usecases/order-creation
```

3. 既存コードに対して、SPEC に基づく差分のみが反映される。他ユースケースのコードは変更しない

---

## 参考

* [SKILL.md](SKILL.md) - エントリポイント、クイックリファレンス
* [開発原則](principles/) - アーキテクチャ標準、セキュリティ標準、共通ルール
* ウォーターフォール版: [jakarta-ee-api-base](../jakarta-ee-api-base/SKILL.md)
