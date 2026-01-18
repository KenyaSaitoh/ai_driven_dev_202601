# SKILL: jakarta-ee-api-base の使い方

バージョン: 4.0.0  
最終更新日: 2026-01-18

---

## 概要

Jakarta EE 10とJAX-RS 3.1を使ったREST API サービス開発を支援するAgent Skillです。

このAgent Skillは、SPECからタスク分解、詳細設計、コード生成、単体テスト実行評価、E2Eテストまで6段階で一貫サポートします。

---

## フォルダ構造

```
agent_skills/jakarta-ee-api-base/
│
├── SKILL.md                                    # Agent Skill説明書（エントリポイント）
│                                               # 6段階プロセス、使い方、実践例を記載
│
├── instructions/                               # 開発インストラクション（6段階プロセス）
│   │
│   ├── basic_design.md                        # ステップ1: 基本設計（SPEC作成）
│   │   └─→ 遵守: principles/common_rules.md
│   │   └─→ 参照: frameworks/（該当する場合）
│   │   └─→ 読込: {project_root}/specs/baseline/basic_design/requirements.md
│   │   └─→ 出力: {spec_directory}/basic_design/
│   │              ├── architecture_design.md（テンプレートから展開）
│   │              ├── functional_design.md（全機能を含む）
│   │              ├── data_model.md
│   │              ├── behaviors.md（全振る舞いを含む）
│   │              └── external_interface.md
│   │
│   ├── task_breakdown.md                      # ステップ2: タスク分解
│   │   └─→ 遵守: principles/common_rules.md
│   │   └─→ 参照: frameworks/（該当する場合）
│   │   └─→ 読込: {project_root}/specs/baseline/basic_design/
│   │   └─→ 出力: {project_root}/tasks/
│   │              ├── tasks.md                 # メインタスクリスト（依存関係、実行順序）
│   │              ├── setup.md                 # setupタスク（特別なタスク、常に最初）
│   │              ├── FUNC_001_xxx.md          # 機能タスク（内容はプロジェクト固有）
│   │              ├── FUNC_002_yyy.md          # 機能タスク（内容はプロジェクト固有）
│   │              └── e2e_test.md              # E2Eテストタスク
│   │
│   ├── detailed_design.md                     # ステップ3: 詳細設計
│   │   └─→ 遵守: principles/common_rules.md
│   │   └─→ 参照: frameworks/（該当する場合）
│   │   └─→ 読込: {project_root}/specs/baseline/basic_design/
│   │   │         {project_root}/tasks/
│   │   └─→ 出力: {spec_directory}/detailed_design/
│   │              ├── FUNC_001_xxx/            # 機能タスク1（内容はプロジェクト固有）
│   │              │   ├── detailed_design.md  # 詳細設計
│   │              │   └── behaviors.md        # 単体テスト
│   │              └── FUNC_002_yyy/            # 機能タスク2（内容はプロジェクト固有）
│   │                  ├── detailed_design.md  # 機能固有の実装クラス設計
│   │                  └── behaviors.md        # 機能固有の単体テスト用
│   │
│   ├── code_generation.md                     # ステップ4: コード生成（本番＋単体テスト）
│   │   └─→ 遵守: principles/common_rules.md
│   │   └─→ 参照: frameworks/（該当する場合）
│   │   └─→ 読込: {project_root}/specs/baseline/basic_design/
│   │   │         {project_root}/specs/baseline/detailed_design/
│   │   │         {project_root}/tasks/
│   │   └─→ 出力: {project_root}/src/（コード・テスト）
│   │
│   ├── unit_test_execution.md                 # ステップ5: 単体テスト実行評価
│   │   └─→ 実行: gradle test jacocoTestReport
│   │   └─→ 分析: テスト結果、カバレッジ、未カバーコード
│   │   └─→ 出力: フィードバックレポート
│   │
│   └── e2e_test_generation.md                 # ステップ6: E2Eテスト生成（REST Assured）
│       └─→ 遵守: principles/common_rules.md
│       └─→ 読込: {project_root}/specs/baseline/basic_design/behaviors.md
│       └─→ 出力: {project_root}/src/test/java/（E2Eテスト）
│
├── principles/                                 # 開発原則（全プロジェクト共通）
│   │
│   ├── common_rules.md                        # Jakarta EE開発ルール
│   │                                          # - 仕様ファースト開発
│   │                                          # - アーキテクチャの一貫性
│   │                                          # - テスト駆動品質
│   │                                          # - ドキュメント品質の追求
│   │                                          # - Markdownフォーマット規約
│   │                                          # - タスクの完遂責任
│   │
│   ├── architecture.md                        # Jakarta EE APIアーキテクチャ標準
│   │                                          # - 標準技術スタック
│   │                                          # - レイヤードアーキテクチャ標準
│   │                                          # - デザインパターン標準
│   │                                          # - 開発標準（命名規則、コーディング規約、バリデーション）
│   │                                          # - セキュリティ実装（JWT認証、認証フィルター、認証コンテキスト）
│   │                                          # - トランザクション管理と並行制御（楽観的ロック）
│   │                                          # - エラーハンドリング、ログ出力標準
│   │                                          # - データベース構成、REST API設計原則
│   │                                          # - テスト戦略、パフォーマンス考慮事項
│   │
│   └── security.md                            # セキュリティ標準ガイドライン
│                                              # - JWT認証（HttpOnly Cookie、トークンライフサイクル、CSRF対策）
│                                              # - パスワード管理（BCryptハッシュ化）
│                                              # - データ保護（個人情報、機密情報、暗号化）
│                                              # - 通信セキュリティ（HTTPS/TLS、証明書管理）
│                                              # - セキュアコーディング（SQLインジェクション、XSS、コマンドインジェクション対策）
│                                              # - OWASP Top 10対応
│
└── templates/                                  # SPECテンプレート
    │
    ├── basic_design/                          # 基本設計テンプレート（ステップ1で使用）
    │   ├── architecture_design.md             # アーキテクチャ設計書テンプレート
    │   ├── functional_design.md               # 機能設計書テンプレート（システム全体）
    │   ├── data_model.md                      # データモデル仕様書テンプレート
    │   ├── behaviors.md                       # 振る舞い仕様書テンプレート（E2Eテスト用）
    │   └── external_interface.md              # 外部インターフェース仕様書テンプレート
    │
    └── detailed_design/                       # 詳細設計テンプレート（ステップ3で使用）
        ├── detailed_design.md                 # 詳細設計書テンプレート（実装クラス設計）
        └── behaviors.md                       # 振る舞い仕様書テンプレート（単体テスト用）
```

---

## プロジェクトフォルダ構造

このAgent Skillを使用して開発するプロジェクトの標準フォルダ構造です。

```
{project_root}/                                # プロジェクトルートディレクトリ
│
├── README.md                                  # プロジェクト概要、セットアップ手順
│
├── specs/                                     # 仕様書ディレクトリ
│   └── baseline/                             # ベースライン仕様（バージョン管理される唯一の真実の情報源）
│       │
│       ├── basic_design/                     # ステップ1: 基本設計（システム全体）
│       │   ├── requirements.md              # 要件定義書（所与、既存）
│       │   ├── architecture_design.md       # アーキテクチャ設計書
│       │   ├── functional_design.md         # 機能設計書（全機能を含む）
│       │   ├── data_model.md                # データモデル仕様書
│       │   ├── behaviors.md                 # 振る舞い仕様書（全振る舞い、E2Eテスト用）
│       │   ├── external_interface.md        # 外部インターフェース仕様書
│       │   └── external_interface/          # 外部APIインターフェース定義（OpenAPI YAML等）
│       │       ├── auth-api.yaml           # 例: 認証API定義
│       │       ├── books-api.yaml          # 例: 書籍API定義
│       │       └── （その他API定義）
│       │
│       └── detailed_design/                 # ステップ3: 詳細設計（タスク単位）
│           ├── FUNC_001_xxx/               # 機能タスク1の詳細設計
│           │   ├── detailed_design.md     # 実装クラス設計
│           │   └── behaviors.md           # 単体テスト用振る舞い仕様
│           ├── FUNC_002_yyy/               # 機能タスク2の詳細設計
│           │   ├── detailed_design.md
│           │   └── behaviors.md
│           └── FUNC_003_zzz/               # 機能タスク3の詳細設計
│               ├── detailed_design.md
│               └── behaviors.md
│
├── tasks/                                    # ステップ2: タスク分解の結果
│   ├── tasks.md                             # メインタスクリスト（依存関係、実行順序）
│   ├── setup.md                             # setupタスク（特別なタスク、常に最初）
│   ├── FUNC_001_xxx.md                      # 機能タスク1（例: 認証・認可）
│   ├── FUNC_002_yyy.md                      # 機能タスク2（例: 書籍管理）
│   ├── FUNC_003_zzz.md                      # 機能タスク3（例: 注文管理）
│   └── e2e_test.md                          # E2Eテストタスク
│
├── sql/                                      # データベーススクリプト
│   └── {database_type}/                     # データベース種別（hsqldb, postgresql等）
│       ├── 01_schema.sql                    # スキーマ定義
│       ├── 02_sample_data.sql               # サンプルデータ
│       └── （その他SQLスクリプト）
│
├── src/                                      # ステップ4: コード生成の結果
│   ├── main/
│   │   ├── java/                            # 実装コード
│   │   │   └── {package_structure}/
│   │   │       ├── api/                     # JAX-RS Resources（REST API）
│   │   │       │   ├── AuthResource.java
│   │   │       │   ├── BookResource.java
│   │   │       │   └── （その他Resource）
│   │   │       ├── service/                 # Business Logic
│   │   │       │   ├── AuthService.java
│   │   │       │   ├── BookService.java
│   │   │       │   └── （その他Service）
│   │   │       ├── dao/                     # Data Access
│   │   │       │   ├── BookDao.java
│   │   │       │   ├── UserDao.java
│   │   │       │   └── （その他Dao）
│   │   │       ├── entity/                  # JPA Entities
│   │   │       │   ├── Book.java
│   │   │       │   ├── User.java
│   │   │       │   └── （その他Entity）
│   │   │       ├── dto/                     # Data Transfer Objects
│   │   │       │   ├── BookDto.java
│   │   │       │   ├── UserDto.java
│   │   │       │   └── （その他DTO）
│   │   │       ├── security/                # セキュリティ関連
│   │   │       │   ├── JwtUtil.java
│   │   │       │   ├── AuthFilter.java
│   │   │       │   └── （その他セキュリティコンポーネント）
│   │   │       ├── exception/               # 例外ハンドラー
│   │   │       │   ├── GlobalExceptionMapper.java
│   │   │       │   └── （その他例外）
│   │   │       └── （その他パッケージ）
│   │   │
│   │   ├── resources/                       # アプリケーションリソース
│   │   │   ├── META-INF/
│   │   │   │   └── microprofile-config.properties # 設定ファイル
│   │   │   └── （その他リソース）
│   │   │
│   │   └── webapp/                          # Webアプリケーションリソース
│   │       ├── WEB-INF/
│   │       │   └── beans.xml               # CDI設定
│   │       └── （その他Webリソース）
│   │
│   └── test/
│       ├── java/                            # テストコード
│       │   └── {package_structure}/
│       │       ├── api/                     # Resourceの単体テスト
│       │       │   ├── AuthResourceTest.java
│       │       │   ├── BookResourceTest.java
│       │       │   └── （その他Resourceテスト）
│       │       ├── service/                 # Serviceの単体テスト
│       │       │   ├── AuthServiceTest.java
│       │       │   ├── BookServiceTest.java
│       │       │   └── （その他Serviceテスト）
│       │       ├── dao/                     # Daoの単体テスト
│       │       │   ├── BookDaoTest.java
│       │       │   ├── UserDaoTest.java
│       │       │   └── （その他Daoテスト）
│       │       └── e2e/                     # ステップ6: E2Eテスト
│       │           ├── BaseE2ETest.java    # E2Eテスト基底クラス
│       │           ├── AuthE2ETest.java    # 認証E2Eテスト
│       │           ├── BookE2ETest.java    # 書籍E2Eテスト
│       │           └── （その他E2Eテスト）
│       │
│       └── resources/                       # テストリソース
│           ├── META-INF/
│           │   └── microprofile-config.properties # テスト用設定
│           └── （その他テストリソース）
│
├── build/                                    # ビルド成果物（Git除外）
│   ├── classes/                             # コンパイル済みクラス
│   ├── libs/                                # ビルド済みアーティファクト
│   └── reports/                             # ステップ5: テスト・カバレッジレポート
│       ├── tests/test/
│       │   └── index.html                   # テスト結果（HTML）
│       ├── jacoco/test/
│       │   ├── html/index.html              # カバレッジ（HTML）
│       │   └── jacocoTestReport.json        # カバレッジ（JSON、AI向け）
│       └── test-analysis/
│           ├── test_analysis_report.json    # 分析レポート（JSON）
│           └── test_analysis_report.md      # 分析レポート（Markdown）
│
├── images/                                   # 画像リソース（プロジェクト固有）
│   └── covers/                              # 例: 書籍カバー画像
│       └── （画像ファイル）
│
├── test_script/                              # 手動テストスクリプト（プロジェクト固有）
│   ├── README.md                            # テストスクリプト使い方
│   ├── _common.sh                           # 共通設定・関数
│   ├── test_all.sh                          # 全機能テスト
│   └── （その他テストスクリプト）
│
├── bin/                                      # バイナリ・スクリプト
│   ├── main/
│   └── test/
│
├── build.gradle                              # Gradleビルドスクリプト
├── settings.gradle                           # Gradleプロジェクト設定
└── .gitignore                                # Git除外設定
```

### フォルダ構造の注意事項

1. **specs/baseline/** - バージョン管理される唯一の真実の情報源
   * basic_design/: システム全体の基本設計
   * detailed_design/: タスク単位の詳細設計

2. **tasks/** - タスク分解の結果
   * tasks.md がメインタスクリスト（依存関係、実行順序を記載）
   * 各タスクファイル（FUNC_XXX_xxx.md）が実装タスクを定義

3. **src/main/java/** - 実装コード
   * Jakarta EE標準のレイヤードアーキテクチャ
   * api → service → dao → entity の依存関係

4. **src/test/java/** - テストコード
   * 単体テスト: タスク粒度内のコンポーネントをテスト
   * E2Eテスト: システム全体の振る舞いをテスト

5. **build/reports/** - テスト・カバレッジレポート
   * ステップ5で生成される分析レポート
   * Git除外対象

---

## 6段階プロセス

### ステップ1: 基本設計（SPEC作成）

目的: システム全体を一枚岩として設計し、基本設計SPECを作成する

インストラクション: `basic_design.md`

使い方:
```
@agent_skills/jakarta-ee-api-base/instructions/basic_design.md

SPECを作成してください

パラメータ:
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
```

AIと対話しながら実施:
1. テンプレートを basic_design/ フォルダに展開
2. requirements.mdを読み込み、理解内容を説明
3. ユーザーと対話しながら各SPECの中身を埋める
4. システム全体のSPEC（architecture_design.md、functional_design.md等）を basic_design/ に作成

注意:
* requirements.md（要件定義書）は所与とする（既に存在している前提）
* 基本設計フェーズでは、システム全体を一枚岩として設計する
* 機能単位への分解は、次のタスク分解フェーズで実施する

生成されるファイル:
```
{spec_directory}/basic_design/
├── requirements.md              # 所与（既存）
├── architecture_design.md       # アーキテクチャ設計書
├── functional_design.md         # 機能設計書（全機能を含む）
├── data_model.md                # データモデル仕様書
├── behaviors.md                 # 振る舞い仕様書（全振る舞いを含む、E2Eテスト用）
└── external_interface.md        # 外部インターフェース仕様書
```

---

### ステップ2: タスク分解

目的: basic_design/ を分析して、複数人が並行して作業できる実装タスクリストを生成する

インストラクション: `task_breakdown.md`

使い方:
```
@agent_skills/jakarta-ee-api-base/instructions/task_breakdown.md

タスクを分解してください

パラメータ:
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
* output_directory: <タスク出力先パス（オプション）>
```

AIが自動で実行:
1. basic_design/ を分析
2. 機能を依存関係に基づいて識別し、実装順序を決定
3. タスクファイルを分解・生成
4. `tasks/`フォルダに保存

重要: このタスク分解の結果が、次の詳細設計フェーズで detailed_design/ フォルダ構造を決定します

生成されるファイル:
```
{project_root}/tasks/
├── tasks.md                 # メインタスクリスト（依存関係、実行順序）
├── setup.md                 # setupタスク（特別なタスク、常に最初）
├── FUNC_001_auth.md         # 機能別タスク（例: 認証・認可）
├── FUNC_002_books.md        # 機能別タスク（例: 書籍管理）
├── FUNC_003_orders.md       # 機能別タスク（例: 注文管理）
└── e2e_test.md     # 結合テスト
```

---

### ステップ3: 詳細設計

目的: タスク単位で実装クラス設計と単体テスト用の振る舞い仕様を作成する

インストラクション: `detailed_design.md`

使い方:
```
@agent_skills/jakarta-ee-api-base/instructions/detailed_design.md

詳細設計書を作成してください

パラメータ:
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
* target_type: FUNC_XXX_xxx
```

AIと対話しながら実施:
1. タスク分解の結果に基づいて detailed_design/{target_type}/ フォルダを作成
2. basic_design/functional_design.md を参照して実装設計を作成
3. 実装レベルの detailed_design.md を生成（クラス設計、メソッドシグネチャ、アノテーション）
4. 単体テスト用の behaviors.md を新規作成（タスク粒度内のテストシナリオ）
5. 不明点をユーザーに質問
6. 対話で妥当性・充足性を確認

重要:
* functional_design.md は basic_design/ にのみ存在（唯一の真実の情報源）
* basic_design/behaviors.md: システム全体の振る舞い（E2Eテスト用）
* detailed_design/{target}/behaviors.md: タスク粒度内の振る舞い（単体テスト用）

生成されるファイル:
```
{spec_directory}/detailed_design/
├── FUNC_001_auth/           # 機能タスク1（例: 認証・認可）
│   ├── detailed_design.md  # 詳細設計
│   └── behaviors.md        # 単体テスト
├── FUNC_002_books/          # 機能タスク2（例: 書籍管理）
│   ├── detailed_design.md  # 機能固有の実装クラス設計
│   └── behaviors.md        # 機能固有の単体テスト用
└── FUNC_003_orders/         # 機能タスク3（例: 注文管理）
    ├── detailed_design.md
    └── behaviors.md
```

---

### ステップ4: コード生成（実装+単体テスト）

目的: タスク単位で実装コードと単体テストを生成する

インストラクション: `code_generation.md`

使い方:
```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

タスクを実行してください

パラメータ:
* project_root: <プロジェクトルートパス>
* task_file: <タスクファイルパス>
* skip_infrastructure: true  # setupタスク専用: DB/APサーバーのインストールをスキップ（オプション）
```

AIが自動で実行:
1. タスクと詳細設計（detailed_design/配下）を読み込む
2. コードを生成する（Resource、Service、Dao、Entity、DTO等）
3. タスク粒度内の単体テストを作成する
   * 同じタスク内のコンポーネント間は実際の連携をテスト
   * タスク外の依存関係のみモック化
   * 例: BookService → BookDao は実際の連携、EntityManagerはモック
4. タスクを完了としてマークする

skip_infrastructureパラメータ（setupタスク専用）:
* `true`: DB/APサーバーのインストールをスキップ（既存環境を使用）
* `false`またはパラメータなし: 完全セットアップを実行
* 注意: 機能タスク（FUNC_XXX）実行時はこのパラメータは無視される

生成されるファイル:
```
{project_root}/src/
├── main/java/
│   ├── api/              # JAX-RS Resources
│   ├── service/          # Business Logic
│   ├── dao/              # Data Access
│   └── entity/           # JPA Entities
└── test/java/
    └── （単体テスト）
```

---

### ステップ5: 単体テスト実行評価

目的: 単体テストを実行してカバレッジを分析し、フィードバックを生成する

インストラクション: `unit_test_execution.md`

使い方:
```
@agent_skills/jakarta-ee-api-base/instructions/unit_test_execution.md

単体テストを実行してください

パラメータ:
* project_root: projects/sdd/bookstore/back-office-api-sdd
* target_type: FUNC_002_books
```

AIが自動で実行:
1. テスト実行（gradle test jacocoTestReport）
2. テスト結果とカバレッジ分析
3. 問題の分類（テスト失敗、必要な振る舞い、デッドコード）
4. フィードバックレポート生成
5. ユーザーに推奨アクションを提示

出力:
```
build/reports/
├── tests/test/index.html        # テスト結果（HTML）
├── jacoco/test/
│   ├── html/index.html          # カバレッジ（HTML）
│   └── jacocoTestReport.json    # カバレッジ（JSON、AI向け）
└── test-analysis/
    ├── test_analysis_report.json # 分析レポート（JSON）
    └── test_analysis_report.md   # 分析レポート（Markdown）
```

---

### ステップ6: E2Eテスト生成

目的: システム全体のエンドツーエンドテストを生成する

インストラクション: `e2e_test_generation.md`

使い方:
```
@agent_skills/jakarta-ee-api-base/instructions/e2e_test_generation.md

E2Eテストを生成してください

パラメータ:
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
```

AIが自動で実行:
1. basic_design/behaviors.md（E2Eテストシナリオ）を読み込む
2. REST Assured を使用したE2Eテストを生成する
   * 複数機能間の連携をテスト
   * 実際のHTTPリクエスト/レスポンス
   * 実際のDBアクセスを含む
   * エンドツーエンドのフロー検証
3. テストデータのセットアップ/クリーンアップコードを生成
4. `@Tag("e2e")` でE2Eテストを分離

重要:
* E2Eテストは実装完了後に実行
* アプリケーションサーバーが起動している状態で実行
* `./gradlew e2eTest` で実行（通常の `test` タスクからは除外）

生成されるファイル:
```
{project_root}/src/test/java/
└── e2e/
    ├── BaseE2ETest.java      # E2Eテスト基底クラス
    └── *E2ETest.java         # E2Eテストケース
```

---

## 実践例

### 例1: プロジェクト立ち上げ（6段階）

ステップ1: 基本設計（SPEC作成）
```
@agent_skills/jakarta-ee-api-base/instructions/basic_design.md

SPECを作成してください

パラメータ:
* project_root: projects/sdd/bookstore/back-office-api-sdd
* spec_directory: projects/sdd/bookstore/back-office-api-sdd/specs/baseline
```

AIと対話しながらSPECを作成

ステップ2: タスク分解
```
@agent_skills/jakarta-ee-api-base/instructions/task_breakdown.md

全タスクを分解してください

パラメータ:
* project_root: projects/sdd/bookstore/back-office-api-sdd
* spec_directory: projects/sdd/bookstore/back-office-api-sdd/specs/baseline
```

結果:
```
tasks/
├── tasks.md              ← メインタスクリスト（依存関係、実行順序）
├── setup.md              ← setupタスク（特別なタスク、常に最初）
├── FUNC_001_auth.md      ← 機能タスク（例: 認証・認可）
├── FUNC_002_books.md     ← 機能タスク（例: 書籍管理）
├── FUNC_003_orders.md    ← 機能タスク（例: 注文管理）        
└── e2e_test.md  ← E2Eテスト
```

ステップ3: 詳細設計（書籍機能）
```
@agent_skills/jakarta-ee-api-base/instructions/detailed_design.md

書籍機能の詳細設計書を作成してください

パラメータ:
* project_root: projects/sdd/bookstore/back-office-api-sdd
* spec_directory: projects/sdd/bookstore/back-office-api-sdd/specs/baseline
* target_type: FUNC_002_books

JPQL検索とCriteria API検索の両方を実装する予定です
```

AIとの対話:
```
AI: タスク分解の結果を確認しました。
    detailed_design/FUNC_002_books/ フォルダを作成します。
    basic_design/functional_design.md を参照して書籍機能の実装設計を作成します。
    
    【機能概要】（basic_design/functional_design.mdから）
    * 書籍機能（REST API: /api/books）
    * エンドポイント:
      1. GET /api/books - 全書籍取得
      2. GET /api/books/{id} - 書籍詳細
      3. GET /api/books/search/jpql - JPQL検索
      4. GET /api/books/search/criteria - Criteria API検索
    
    【関連エンティティ】
    * Book（テーブル: BOOK + STOCK）
      * @ManyToOne Category
      * @ManyToOne Publisher
      * @SecondaryTable(name="STOCK") for quantity, version
    
    【不明点・確認事項】
    1. 検索条件はkeywordとcategoryIdでよろしいでしょうか？
    2. keywordはbookNameとauthorの両方を対象にしますか？

ユーザー: はい、その通りです。keywordは両方を対象にしてください。

AI: 承知しました。以下を作成しました：
    → specs/baseline/detailed_design/FUNC_002_books/detailed_design.md（実装クラス設計）
    → specs/baseline/detailed_design/FUNC_002_books/behaviors.md（単体テスト用）
```

ステップ4: コード生成
```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

書籍機能を実装してください

パラメータ:
* project_root: projects/sdd/bookstore/back-office-api-sdd
* task_file: projects/sdd/bookstore/back-office-api-sdd/tasks/FUNC_002_books.md
```

ステップ5: 単体テスト実行評価
```
@agent_skills/jakarta-ee-api-base/instructions/unit_test_execution.md

単体テストを実行してください

パラメータ:
* project_root: projects/sdd/bookstore/back-office-api-sdd
* target_type: FUNC_002_books
```

（フィードバックに基づいて、必要に応じてステップ3に戻る）

ステップ6: E2Eテスト生成
```
@agent_skills/jakarta-ee-api-base/instructions/e2e_test_generation.md

E2Eテストを生成してください

パラメータ:
* project_root: projects/sdd/bookstore/back-office-api-sdd
* spec_directory: projects/sdd/bookstore/back-office-api-sdd/specs/baseline
```

