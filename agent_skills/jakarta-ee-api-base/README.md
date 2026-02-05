# Jakarta EE API サービス開発 Agent Skill

バージョン: 6.0.0  
最終更新日: 2026-02-05

---

## 概要

Jakarta EE 10とJAX-RS 3.1を使ったREST API サービス開発を支援するAgent Skillです。

このAgent Skillは、SPECから詳細設計、コード生成、単体テスト実行評価、結合テスト、E2Eテストまで6段階で一貫サポートします。ドメインベースのフォルダ構成により、並行開発を実現。振る舞い仕様（behaviors.md）は Gherkin 記法で記述し、単体・結合・E2Eテストは Cucumber（.feature + ステップ定義）で生成する前提です。さらに、基本設計変更対応により、手戻りや拡張案件にも対応します。

対象プロジェクト例: `projects/sdd-wf/bookstore/back-office-api`, `projects/sdd-wf/bookstore/berry-books-api`

## クイックスタート

1. 基本設計: `@agent_skills/jakarta-ee-api-base/instructions/basic_design.md` で SPEC を作成（ドメインフォルダ構成）
2. 詳細設計: `@agent_skills/jakarta-ee-api-base/instructions/detailed_design.md` でドメイン単位の詳細設計を作成
3. コード生成: `@agent_skills/jakarta-ee-api-base/instructions/code_generation.md` で target_domain 指定して実装
4. 単体テスト評価: `unit_test_execution.md` → 結合テスト: `it_generation.md` → E2Eテスト: `e2e_test_generation.md`

---

## フォルダ構造

```
agent_skills/jakarta-ee-api-base/
│
├── SKILL.md                                    # Agent Skill説明書（エントリポイント）
│                                               # 7段階プロセス、使い方、実践例を記載
│
├── instructions/                               # 開発インストラクション（6段階プロセス + 変更対応）
│   │
│   ├── basic_design.md                        # ステップ1: 基本設計（SPEC作成）
│   │   └─→ 遵守: principles/common_rules.md
│   │   └─→ 参照: frameworks/（該当する場合）
│   │   └─→ 読込: {project_root}/specs/baseline/requirements/requirements.md
│   │   └─→ 出力: {spec_directory}/requirements/
│   │              ├── requirements.md（所与）
│   │              └── behaviors.md（E2Eテスト用、要件を外形的に捉えた振る舞い）
│   │              {spec_directory}/basic_design/
│   │              ├── common/                  # 共通ドメイン（最優先実装）
│   │              │   ├── architecture_design.md
│   │              │   ├── data_model.md
│   │              │   ├── external_interface.md
│   │              │   ├── functional_design.md
│   │              │   └── behaviors.md
│   │              ├── {domain1}/               # ドメイン1
│   │              │   ├── functional_design.md
│   │              │   └── behaviors.md
│   │              └── {domain2}/               # ドメイン2
│   │                  ├── functional_design.md
│   │                  └── behaviors.md
│   │
│   ├── detailed_design.md                     # ステップ2: 詳細設計（ドメイン単位）
│   │   └─→ 遵守: principles/common_rules.md
│   │   └─→ 参照: frameworks/（該当する場合）
│   │   └─→ 読込: {project_root}/specs/baseline/basic_design/{domain}/
│   │   └─→ 出力: {spec_directory}/detailed_design/
│   │              ├── common/                  # 共通ドメイン（最優先実装）
│   │              │   ├── detailed_design.md
│   │              │   └── behaviors.md
│   │              ├── {domain1}/               # ドメイン1
│   │              │   ├── detailed_design.md
│   │              │   └── behaviors.md
│   │              └── {domain2}/               # ドメイン2
│   │                  ├── detailed_design.md
│   │                  └── behaviors.md
│   │
│   ├── code_generation.md                     # ステップ3: コード生成（本番＋単体テスト、ドメイン単位）
│   │   └─→ 遵守: principles/common_rules.md
│   │   └─→ 参照: frameworks/（該当する場合）
│   │   └─→ 読込: {project_root}/specs/baseline/basic_design/{domain}/
│   │   │         {project_root}/specs/baseline/detailed_design/{domain}/
│   │   └─→ 出力: {project_root}/src/（コード・テスト）
│   │
│   ├── unit_test_execution.md                 # ステップ4: 単体テスト実行評価
│   │   └─→ 実行: ./gradlew test jacocoTestReport（プロジェクトの build.gradle に従う）
│   │   └─→ 分析: テスト結果、カバレッジ、未カバーコード
│   │   └─→ 出力: フィードバックレポート
│   │
│   ├── it_generation.md                       # ステップ5: 結合テスト生成（Cucumber + Weld SE）
│   │   └─→ 遵守: principles/common_rules.md
│   │   └─→ 読込: {project_root}/specs/baseline/basic_design/{domain}/behaviors.md（Gherkin）
│   │   └─→ 出力: {project_root}/src/test/（.feature + ステップ定義、integration/）
│   │   └─→ 特徴: Cucumber .feature + ステップ定義、Service層以下・実DB、外部APIはWireMock
│   │
│   ├── e2e_test_generation.md                 # ステップ6: E2Eテスト生成（Cucumber + REST Assured）
│   │   └─→ 遵守: principles/common_rules.md
│   │   └─→ 読込: {project_root}/specs/baseline/requirements/behaviors.md（Gherkin）
│   │   └─→ 出力: {project_root}/src/test/（.feature + ステップ定義、e2e/）
│   │   └─→ 特徴: Cucumber .feature + ステップ定義、複数機能間連携、実HTTP・実DB
│   │
│   └── basic_design_change.md                 # 基本設計変更対応（手戻り・拡張案件、ドメイン単位）
│       └─→ 遵守: principles/common_rules.md
│       └─→ 読込: {spec_directory}/basic_design/CHANGES.md
│       └─→ 処理: 変更影響分析、影響ドメイン識別、既存指示書呼び出し
│       └─→ 出力: 更新された設計/コード
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
│   │                                          # - トランザクション管理と並行制御（悲観的ロック、楽観的ロック）
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
    ├── requirements/                          # 要件定義テンプレート（ステップ1で使用）
    │   └── behaviors.md                       # 振る舞い仕様書テンプレート（E2Eテスト用）
    │
    ├── basic_design/                          # 基本設計テンプレート（ステップ1で使用）
    │   ├── architecture_design.md             # アーキテクチャ設計書テンプレート
    │   ├── functional_design.md               # 機能設計書テンプレート（システム全体）
    │   ├── data_model.md                      # データモデル仕様書テンプレート
    │   ├── behaviors.md                       # 振る舞い仕様書テンプレート（結合テスト用）
    │   ├── external_interface.md              # 外部インターフェース仕様書テンプレート
    │   └── CHANGES_template.md                # 変更差分ファイルテンプレート（変更対応で使用）
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
│       ├── requirements/                    # 要件定義（所与）
│       │   ├── requirements.md              # 要件定義書（所与、既存）
│       │   └── behaviors.md                 # 振る舞い仕様書（E2Eテスト用、要件を外形的に捉えた振る舞い）
│       │
│       ├── basic_design/                    # ステップ1: 基本設計（システム全体）
│       │   ├── architecture_design.md       # アーキテクチャ設計書
│       │   ├── functional_design.md         # 機能設計書（全機能を含む）
│       │   ├── data_model.md                # データモデル仕様書
│       │   ├── behaviors.md                 # 振る舞い仕様書（結合テスト用、基本設計を外形的に捉えた振る舞い）
│       │   ├── external_interface.md        # 外部インターフェース仕様書
│       │   ├── CHANGES.md                   # アクティブな変更（未適用、変更対応時に作成）
│       │   ├── changes_archive/             # 適用済み変更のアーカイブ
│       │   │   ├── 20260118_order_cancel.md # 例: 過去の変更履歴
│       │   │   └── 20260125_order_history.md
│       │   └── external_interface/          # 外部APIインターフェース定義（OpenAPI YAML等）
│       │       ├── auth-api.yaml           # 例: 認証API定義
│       │       ├── books-api.yaml          # 例: 書籍API定義
│       │       └── （その他API定義）
│       │
│       └── detailed_design/                 # ステップ2: 詳細設計（ドメイン単位）
│           ├── common/                     # 共通ドメインの詳細設計（最優先実装）
│           │   ├── detailed_design.md     # 実装クラス設計
│           │   └── behaviors.md           # 単体テスト用振る舞い仕様
│           ├── orders/                     # ドメイン1の詳細設計
│           │   ├── detailed_design.md
│           │   └── behaviors.md
│           └── books_proxy/                # ドメイン2の詳細設計
│               ├── detailed_design.md
│               └── behaviors.md
│
├── detailed_design/                         # ステップ2: 詳細設計の結果
│   ├── common/                              # 共通ドメイン（最優先実装）
│   ├── orders/                              # ドメイン1（例: 注文管理）
│   └── books_proxy/                         # ドメイン2（例: 書籍API連携）
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
│       │       ├── api/                     # Resourceの単体テスト（@Tag("unit")）
│       │       │   ├── AuthResourceTest.java
│       │       │   ├── BookResourceTest.java
│       │       │   └── （その他Resourceテスト）
│       │       ├── service/                 # Serviceの単体テスト（@Tag("unit")）
│       │       │   ├── AuthServiceTest.java
│       │       │   ├── BookServiceTest.java
│       │       │   └── （その他Serviceテスト）
│       │       ├── dao/                     # Daoの単体テスト（@Tag("unit")）
│       │       │   ├── BookDaoTest.java
│       │       │   ├── UserDaoTest.java
│       │       │   └── （その他Daoテスト）
│       │       ├── integration/             # ステップ6: 結合テスト（@Tag("integration")）
│       │       │   ├── BaseIntegrationTest.java # 結合テスト基底クラス（Weld SE）
│       │       │   ├── BookServiceIT.java   # 結合テスト用ステップ定義等
│       │       │   └── （その他結合テスト）
│       │       └── e2e/                     # ステップ7: E2Eテスト（@Tag("e2e")）
│       │           ├── BaseE2ETest.java    # E2Eテスト基底クラス
│       │           ├── AuthE2ETest.java    # E2Eテスト用ステップ定義等
│       │           └── （その他E2Eテスト）
│       │
│       └── resources/                       # テストリソース
│           ├── META-INF/
│           │   └── microprofile-config.properties # テスト用設定
│           ├── features/                     # Cucumber .feature（Gherkin）
│           │   ├── unit/                    # 単体テスト用（該当する場合）
│           │   ├── integration/             # 結合テスト用
│           │   └── e2e/                     # E2Eテスト用
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
   * requirements/: 要件定義（所与）とE2Eテスト用振る舞い仕様
   * basic_design/: システム全体の基本設計と結合テスト用振る舞い仕様
   * detailed_design/: タスク単位の詳細設計と単体テスト用振る舞い仕様

2. **振る舞い仕様書（behaviors.md）の3種類**
   * requirements/behaviors.md: E2Eテスト用（要件を外形的に捉えた振る舞い）
   * basic_design/behaviors.md: 結合テスト用（基本設計を外形的に捉えた振る舞い）
   * detailed_design/{target}/behaviors.md: 単体テスト用（タスク粒度内の振る舞い）

3. **detailed_design/** - 詳細設計の結果
   * ドメイン単位で詳細設計を管理
   * common/ は最優先で実装（他のドメインはcommonに依存）

4. **src/main/java/** - 実装コード
   * Jakarta EE標準のレイヤードアーキテクチャ
   * api → service → dao → entity の依存関係

5. **src/test/** - テストコード（3層構造）
   * 単体テスト（@Tag("unit")）: モックを使用、コンポーネント単体をテスト。Cucumber .feature（features/unit）＋ステップ定義の場合あり
   * 結合テスト（@Tag("integration")）: Cucumber .feature（features/integration）＋ステップ定義、Weld SE、実DB、外部APIはWireMock
   * E2Eテスト（@Tag("e2e")）: Cucumber .feature（features/e2e）＋ステップ定義、REST Assured、実サーバー・実DB

6. **build/reports/** - テスト・カバレッジレポート
   * ステップ5で生成される分析レポート
   * Git除外対象

7. **CHANGES.md** - 基本設計変更管理
   * basic_design/CHANGES.md: アクティブな変更（未適用）
   * basic_design/changes_archive/: 適用済み変更の履歴

---

## 6段階プロセス

### ステップ1: 基本設計（SPEC作成）

目的: ドメイン単位で基本設計SPECを作成する

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
1. requirements/requirements.mdを読み込み、理解内容を説明
2. requirements/behaviors.md（E2Eテスト用）を作成
3. テンプレートを basic_design/ フォルダに展開
4. ユーザーと対話しながら各SPECの中身を埋める
5. システム全体のSPEC（architecture_design.md、functional_design.md等）を basic_design/ に作成
6. basic_design/behaviors.md（結合テスト用）を作成

注意:
* requirements.md（要件定義書）は所与とする（既に存在している前提）
* 基本設計フェーズでは、ドメイン単位で設計を行う
* ドメイン構造は実装順序を決定する（common/ → 各ドメイン）
* 振る舞い仕様書は3種類作成（Gherkin記法で記述。Cucumber .feature の元になる）:
  - requirements/behaviors.md: E2Eテスト用（要件を外形的に捉えた振る舞い）
  - basic_design/{domain}/behaviors.md: 結合テスト用（ドメイン内の連携シナリオ）
  - detailed_design/{domain}/behaviors.md: 単体テスト用（メソッドレベルのテスト）
  - basic_design/behaviors.md: 結合テスト用（基本設計を外形的に捉えた振る舞い）

生成されるファイル:
```
{spec_directory}/requirements/
├── requirements.md              # 所与（既存）
└── behaviors.md                 # E2Eテスト用（要件を外形的に捉えた振る舞い）

{spec_directory}/basic_design/
├── architecture_design.md       # アーキテクチャ設計書
├── functional_design.md         # 機能設計書（全機能を含む）
├── data_model.md                # データモデル仕様書
├── behaviors.md                 # 結合テスト用（基本設計を外形的に捉えた振る舞い）
└── external_interface.md       # 外部インターフェース仕様書
```

---

### ステップ2: 詳細設計

目的: ドメイン単位で実装クラス設計と単体テスト用の振る舞い仕様を作成する

インストラクション: `detailed_design.md`

使い方:
```
@agent_skills/jakarta-ee-api-base/instructions/detailed_design.md

詳細設計書を作成してください

パラメータ:
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
* target_domain: common
```

AIと対話しながら実施:
1. basic_design/{target_domain}/ の設計を参照して detailed_design/{target_domain}/ フォルダを作成
2. basic_design/{target_domain}/functional_design.md を参照して実装設計を作成
3. 実装レベルの detailed_design.md を生成（クラス設計、メソッドシグネチャ、アノテーション）
4. 単体テスト用の behaviors.md を新規作成（ドメイン単位のテストシナリオ）
5. 不明点をユーザーに質問
6. 対話で妥当性・充足性を確認

重要:
* functional_design.md は basic_design/{domain}/ にのみ存在（唯一の真実の情報源）
* commonは最優先で詳細設計を作成（他のドメインはcommonに依存）
* 振る舞い仕様書の3種類の使い分け:
  - requirements/behaviors.md: E2Eテスト用（要件を外形的に捉えた振る舞い）
  - basic_design/{domain}/behaviors.md: 結合テスト用（ドメイン内の連携シナリオ）
  - detailed_design/{domain}/behaviors.md: 単体テスト用（ドメイン単位の振る舞い）

生成されるファイル:
```
{spec_directory}/detailed_design/
├── common/                  # 共通ドメイン（最優先実装）
│   ├── detailed_design.md  # 詳細設計
│   └── behaviors.md        # 単体テスト
├── orders/                 # ドメイン1（例: 注文管理）
│   ├── detailed_design.md  # ドメイン固有の実装クラス設計
│   └── behaviors.md        # ドメイン固有の単体テスト用
└── books_proxy/            # ドメイン2（例: 書籍API連携）
    ├── detailed_design.md
    └── behaviors.md
```

---

### ステップ3: コード生成（実装+単体テスト）

目的: ドメイン単位で実装コードと単体テストを生成する

インストラクション: `code_generation.md`

使い方:
```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

ドメインのコードを生成してください

パラメータ:
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
* target_domain: common
```

AIが自動で実行:
1. 詳細設計（detailed_design/{target_domain}/）を読み込む
2. コードを生成する（Resource、Service、Dao、Entity、DTO等）
3. ドメイン単位の単体テストを作成する
   * detailed_design/{target_domain}/behaviors.md（Gherkin）から Cucumber .feature（features/unit）とステップ定義（Java）を生成
   * 同じドメイン内のコンポーネント間は実際の連携をテスト
   * ドメイン外の依存関係のみモック化
   * 例: OrderService → OrderDao は実際の連携、EntityManagerはモック

生成されるファイル:
```
{project_root}/src/
├── main/java/
│   ├── api/              # JAX-RS Resources
│   ├── service/          # Business Logic
│   ├── dao/              # Data Access
│   └── entity/           # JPA Entities
└── test/
    ├── java/.../         # 単体テスト（JUnit 5、Cucumber ステップ定義等）
    └── resources/features/unit/  # Cucumber .feature（該当する場合）
```

---

### ステップ4: 単体テスト実行評価

目的: 単体テストを実行してカバレッジを分析し、フィードバックを生成する

インストラクション: `unit_test_execution.md`

使い方:
```
@agent_skills/jakarta-ee-api-base/instructions/unit_test_execution.md

単体テストを実行してください

パラメータ:
* project_root: <プロジェクトルートパス>
* target_type: FUNC_XXX_xxx
```

AIが自動で実行:
1. テスト実行（`./gradlew test jacocoTestReport`。Windowsの場合は `gradlew.bat` を使用。プロジェクトの build.gradle に従う）
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

### ステップ5: 結合テスト生成

目的: Service層とDAO層の結合テストを生成する

インストラクション: `it_generation.md`

使い方:
```
@agent_skills/jakarta-ee-api-base/instructions/it_generation.md

結合テストを生成してください

パラメータ:
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
```

AIが自動で実行:
1. basic_design/behaviors.md（結合テストシナリオ、Gherkin記法）を読み込む
2. JUnit 5 + Cucumber（.feature + ステップ定義）+ Weld SE を使用した結合テストを生成する
   * basic_design/behaviors.md のシナリオを Cucumber の .feature とステップ定義（Java）に変換
   * Service層以下（Service + DAO + Entity）の実際の連携をテスト
   * 実際のDB接続（メモリDB）。外部APIは WireMock でスタブ化
   * CDI環境（Weld SE）でのコンポーネント連携
3. テストデータのセットアップ/クリーンアップコードを生成
4. `@Tag("integration")` で結合テストを分離

重要:
* 結合テストは単体テスト完了後に実行
* API層は含まない（Service層以下をテスト）
* 実行方法はプロジェクトの build.gradle に定義されたタスクに従う（例: `./gradlew integrationTest`。通常の `test` タスクからは除外）

生成されるファイル:
```
{project_root}/src/test/
├── java/.../integration/
│   ├── BaseIntegrationTest.java   # 結合テスト基底クラス（Weld SE設定）
│   └── *IT.java                   # 結合テスト用ステップ定義等
└── resources/features/integration/
    └── *.feature                  # Cucumber 結合テストシナリオ
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
1. requirements/behaviors.md（E2Eテストシナリオ、Gherkin記法）を読み込む
2. JUnit 5 + Cucumber（.feature + ステップ定義）+ REST Assured を使用したE2Eテストを生成する
   * requirements/behaviors.md のシナリオを Cucumber の .feature とステップ定義（Java、REST Assured）に変換
   * 複数機能間の連携、実際のHTTPリクエスト/レスポンス、実際のDBアクセスを含む
   * エンドツーエンドのフロー検証
3. テストデータのセットアップ/クリーンアップコードを生成
4. `@Tag("e2e")` でE2Eテストを分離

重要:
* E2Eテストは実装完了後に実行
* アプリケーションサーバーが起動している状態で実行
* 実行方法はプロジェクトの build.gradle に定義されたタスクに従う（例: `./gradlew e2eTest`。通常の `test` タスクからは除外）

生成されるファイル:
```
{project_root}/src/test/
├── java/.../e2e/
│   ├── BaseE2ETest.java       # E2Eテスト基底クラス
│   └── *E2ETest.java         # E2Eテスト用ステップ定義等
└── resources/features/e2e/
    └── *.feature              # Cucumber E2Eテストシナリオ
```

---

## 基本設計変更対応（手戻り・拡張案件）

目的: 結合テストやE2Eテストで不具合が見つかり、基本設計に戻る必要がある場合や、拡張案件で新機能を追加する場合に対応する

インストラクション: `basic_design_change.md`

使い方:
```
@agent_skills/jakarta-ee-api-base/instructions/basic_design_change.md

基本設計の変更を適用してください

パラメータ:
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
* change_spec: <変更差分ファイルパス>（省略可、デフォルト: {spec_directory}/basic_design/CHANGES.md）
```

実行手順:
1. 基本設計SPECのマスターファイル（functional_design.md、data_model.md等）を自由に編集
2. CHANGES.mdを作成して変更内容を明示的に記載
   ```bash
   cp agent_skills/jakarta-ee-api-base/templates/basic_design/CHANGES_template.md \
      specs/baseline/basic_design/CHANGES.md
   vim specs/baseline/basic_design/CHANGES.md
   ```
3. 上記コマンドを実行

AIが自動で実行:
1. CHANGES.md（変更差分ファイル）を読み込み
2. 変更の影響を受けるドメインを識別
3. 既存の指示書を呼び出して、影響を受けるドメインの設計・コード・テストを更新
   * detailed_design.md → 詳細設計更新
   * code_generation.md → コード更新
   * it_generation.md → 結合テスト更新
   * e2e_test_generation.md → E2Eテスト更新
5. すべての変更適用後、CHANGES.mdをアーカイブ

重要:
* マスターファイルはMarkdown、EXCEL、PDF、Word等、任意の形式で管理可能
* 変更内容はCHANGES.mdに明示的に記載（形式非依存）
* 適用後、CHANGES.mdは自動的に`changes_archive/`に移動され、履歴として保管

ディレクトリ構造:
```
{spec_directory}/basic_design/
├── functional_design.md      # マスター（自由に編集）
├── data_model.md             # マスター（自由に編集）
├── CHANGES.md                # アクティブな変更（未適用）
└── changes_archive/          # 履歴
    ├── 20260118_order_cancel.md
    └── 20260125_order_history.md
```

---

## 実践例

### 例1: プロジェクト立ち上げ（7段階）

ステップ1: 基本設計（SPEC作成）
```
@agent_skills/jakarta-ee-api-base/instructions/basic_design.md

SPECを作成してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api
* spec_directory: projects/sdd-wf/bookstore/back-office-api/specs/baseline
```

AIと対話しながらSPECを作成（ドメイン構成を決定）

ステップ2: 詳細設計（commonドメイン）
```
@agent_skills/jakarta-ee-api-base/instructions/detailed_design.md

commonドメインの詳細設計書を作成してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api
* spec_directory: projects/sdd-wf/bookstore/back-office-api/specs/baseline
* target_domain: common
```

AIとの対話:
```
AI: basic_design/common/ の設計を確認しました。
    detailed_design/common/ フォルダを作成します。
    
    【共通ドメイン概要】（basic_design/common/から）
    * エンティティ: Book, Category, Publisher
    * Dao: BookDao, CategoryDao, PublisherDao
    * セキュリティ: JwtUtil, JwtAuthenFilter
    * 認証API: /api/auth/login
    
    【不明点・確認事項】
    1. 検索条件はkeywordとcategoryIdでよろしいでしょうか？
    2. keywordはbookNameとauthorの両方を対象にしますか？

ユーザー: はい、その通りです。keywordは両方を対象にしてください。

AI: 承知しました。以下を作成しました：
    → specs/baseline/detailed_design/common/detailed_design.md（実装クラス設計）
    → specs/baseline/detailed_design/common/behaviors.md（単体テスト用）
```

ステップ3: コード生成（commonドメイン）
```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

commonドメインを実装してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api
* spec_directory: projects/sdd-wf/bookstore/back-office-api/specs/baseline
* target_domain: common
```

ステップ4: 単体テスト実行評価
```
@agent_skills/jakarta-ee-api-base/instructions/unit_test_execution.md

単体テストを実行してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api
* target_domain: common
```

（commonドメイン完了後、ordersやbooks_proxyなど他のドメインの詳細設計・コード生成・単体テストを実施）

ステップ5: 結合テスト生成
```
@agent_skills/jakarta-ee-api-base/instructions/it_generation.md

結合テストを生成してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api
* spec_directory: projects/sdd-wf/bookstore/back-office-api/specs/baseline
* target_domains: all
```

実行方法（プロジェクトの build.gradle に定義されたタスクに従う）:
```bash
./gradlew integrationTest
```

ステップ6: E2Eテスト生成
```
@agent_skills/jakarta-ee-api-base/instructions/e2e_test_generation.md

E2Eテストを生成してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api
* spec_directory: projects/sdd-wf/bookstore/back-office-api/specs/baseline
```

実行方法（プロジェクトの build.gradle に定義されたタスクに従う）:
```bash
# アプリケーションサーバーを起動
./gradlew run

# 別ターミナルでE2Eテストを実行
./gradlew e2eTest
```

---

### 例2: 基本設計変更対応（手戻り・拡張案件）

E2Eテストで不具合が見つかり、注文キャンセル機能を追加する必要が発生した場合の例。

**ステップ1: 基本設計SPECを更新**

```bash
# 機能設計書を編集してキャンセル機能を追加
vim specs/baseline/basic_design/functional_design.md

# データモデルを編集してcancel_reasonカラムを追加
vim specs/baseline/basic_design/data_model.md
```

**ステップ2: CHANGES.mdを作成**

```bash
# テンプレートをコピー
cp agent_skills/jakarta-ee-api-base/templates/basic_design/CHANGES_template.md \
   specs/baseline/basic_design/CHANGES.md

# 変更内容を明示的に記載
vim specs/baseline/basic_design/CHANGES.md
```

CHANGES.mdの記載例:
```markdown
# 基本設計変更記録

## [2026-01-18] 注文キャンセル機能追加

### 変更対象
- functional_design.md
- data_model.md
- behaviors.md（結合テスト用）

### 変更内容

#### functional_design.md の変更
##### セクション「API一覧」
**追加**:
- API_002_Order に DELETE /orders/{id} エンドポイント追加
  - リクエスト: cancel_reason（必須、VARCHAR(255)）
  - レスポンス: 200 OK または 404 Not Found

#### data_model.md の変更
##### テーブル「ORDER_TRAN」
**追加**:
| カラム名 | 型 | NULL | デフォルト | 説明 |
|---------|-----|------|-----------|------|
| cancel_reason | VARCHAR(255) | YES | NULL | キャンセル理由 |

#### behaviors.md の変更（結合テスト用）
**追加**:
- シナリオ: 注文キャンセル
  - 前提: 注文が作成済み
  - 実行: DELETE /orders/{id} with cancel_reason
  - 期待: 注文ステータスがCANCELLEDに更新される

### 変更理由
E2Eテストで誤注文のキャンセル機能がないことが判明。
顧客からの要望もあり、追加が必要と判断。

### 影響範囲（推定）
- 影響ドメイン: orders
- 詳細設計: detailed_design/orders/detailed_design.md
- コード: OrderResource.java, OrderService.java, OrderDao.java
- テスト: 単体テスト、結合テスト、E2Eテスト
```

**ステップ3: 変更対応を実行**

```
@agent_skills/jakarta-ee-api-base/instructions/basic_design_change.md

基本設計の変更を適用してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api
* spec_directory: projects/sdd-wf/bookstore/back-office-api/specs/baseline
```

AIが自動で実行:
1. CHANGES.mdを読み込み
2. 影響ドメイン識別（orders ドメインが影響を受ける）
3. 以下の指示書を順次呼び出し:
   - detailed_design.md → ordersドメインの詳細設計更新
   - code_generation.md → ordersドメインのコード更新
   - unit_test_execution.md → ordersドメインの単体テスト実行
   - it_generation.md → ordersドメインの結合テスト更新
   - e2e_test_generation.md → E2Eテスト更新
4. CHANGES.mdを `changes_archive/20260118_order_cancel.md` に移動

**ステップ4: テスト実行**

```bash
# 単体テスト
./gradlew test

# 結合テスト
./gradlew integrationTest

# E2Eテスト
./gradlew run  # 別ターミナル
./gradlew e2eTest
```

---

## 参考

* [SKILL.md](SKILL.md) - エントリポイント、クイックリファレンス
* [開発原則](principles/) - アーキテクチャ標準、セキュリティ標準、共通ルール
  * [architecture.md](principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](principles/security.md) - セキュリティ標準
  * [common_rules.md](principles/common_rules.md) - 共通ルール
* アジャイル版: [jakarta-ee-api-agile](../jakarta-ee-api-agile/README.md)
