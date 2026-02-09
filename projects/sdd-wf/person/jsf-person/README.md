# jsf-person-sdd-wf プロジェクト

## 📖 概要

Apache Struts 1.xからJakarta Faces (JSF) 4.0にマイグレーションされた人材管理システムです。
JSFとJPA (Java Persistence API) を組み合わせたデータベースCRUD操作を実装しています。

* 移行元: `@projects/master/person/struts-person`（Apache Struts 1.3.10）
* 移行先: このプロジェクト（Jakarta Faces 4.0 + Jakarta EE 10）

> Note: このプロジェクトはStrutsマイグレーション研修用プロジェクトです。

> マイグレーションアプローチ:
> * 既存のStrutsコードから仕様書を生成（既存コード分析）
> * 仕様書を検証・調整してJSFアーキテクチャに適応
> * 仕様書からJSFコードを生成（仕様駆動開発）
> * 汎用Agent Skills (`agent_skills/struts-to-jsf-migration/`) を使用したマイグレーション

## 🤖 Agent Skillsを使ったマイグレーション

このプロジェクトは、汎用的な Struts to JSF マイグレーション Agent Skills を使用してマイグレーションします。

マイグレーションは以下の6段階プロセスで進めます（ドメイン単位）：

```
ステップ1: 既存コード分析（Strutsコード → 仕様書）
    ↓
ステップ2: 詳細設計（ドメイン単位で詳細設計）← AIと対話しながら
    ↓
ステップ3: 本番コード生成（詳細設計→実装）（ドメイン単位でJSFコード生成）
    ↓
ステップ4: 単体テストコード生成（ブラックボックス + ホワイトボックス）
    ↓
ステップ5: テスト評価（テスト実行 → カバレッジ分析 → フィードバック）
    ↓
ステップ5: 結合テスト生成（basic_design/behaviors.md → JUnit + Weld SE）
    ↓
ステップ6: E2Eテスト生成（requirements/behaviors.md → Playwright）
```

**ドメイン構成:**
- `common/` - 共通ドメイン（Entity、Dao等。最優先実装）
- `person_management/` - Person管理画面グループ（一覧、入力、確認等）

---

### 📋 マイグレーションフロー

#### ステップ1: 既存コード分析（最初に1回）

既存のStrutsプロジェクトから仕様書を生成します。

```
@agent_skills/struts-to-jsf-migration/instructions/reverse_engineering.md
@projects/sdd-wf/person/struts-person

既存のStrutsプロジェクトから仕様書を生成してください。

パラメータ:
* struts_project_root: projects/master/person/struts-person
* spec_output_directory: projects/sdd-wf/person/jsf-person/specs
```

* 生成される仕様書:
  * `requirements/requirements.md` - システムの目的、機能要件
  * `requirements/behaviors.md` - 要件レベルの振る舞い
  * `basic_design/common/architecture_design.md` - 技術スタック、レイヤー構成
  * `basic_design/common/data_model.md` - エンティティ、テーブル定義
  * `basic_design/common/external_interface.md` - 外部連携仕様
  * `basic_design/{domain}/functional_design.md` - 画面一覧、画面遷移
  * `basic_design/{domain}/screen_design.md` - 画面レイアウト、入力項目
  * `basic_design/{domain}/behaviors.md` - 画面の振る舞い（E2Eテスト用、Gherkin記法）

---

#### ステップ2: 詳細設計（ドメイン単位）

基本設計SPEC（basic_design/）から詳細設計書を生成します。AIと対話しながら進めます。

**実行順序**: commonドメインを最優先で実装し、その後person_managementドメインを実装します。

使用例（commonドメイン）:

```
@agent_skills/struts-to-jsf-migration/instructions/detailed_design.md

commonドメインの詳細設計書を作成してください

パラメータ:
* project_root: projects/sdd-wf/person/jsf-person
* spec_directory: projects/sdd-wf/person/jsf-person/specs/baseline
* target_domain: common
```

使用例（person_managementドメイン）:

```
@agent_skills/struts-to-jsf-migration/instructions/code_generation.md

setupを実装してください。

パラメータ:
* project_root: projects/sdd-wf/person/jsf-person
* task_file: projects/sdd-wf/person/jsf-person/tasks/setup.md
* skip_infrastructure: true  # setupタスク専用: DB/APサーバーのインストールをスキップ
```

注意:
* `skip_infrastructure` はsetupタスク実行時のみ有効
* 機能タスク（FUNC_XXX）ではこのパラメータは無視される

使用例（機能タスク）:

```
@agent_skills/struts-to-jsf-migration/instructions/code_generation.md

機能タスクを実装してください。

パラメータ:
* project_root: projects/sdd-wf/person/jsf-person
* task_file: projects/sdd-wf/person/jsf-person/tasks/FUNC_001_xxx.md
```

注意: 実際のタスクファイル名は `tasks/tasks.md` を参照してください

使用例（FUNC_002）:

```
@agent_skills/struts-to-jsf-migration/instructions/code_generation.md

FUNC_002を実装してください。

パラメータ:
* project_root: projects/sdd-wf/person/jsf-person
* task_file: projects/sdd-wf/person/jsf-person/tasks/FUNC_002_PersonList.md
```

注意:
* タスクファイル名は `tasks/tasks.md` のタスクファイル列と一致させる
* 各タスクファイル（FUNC_XXX.md）のヘッダーにある「依存タスク」を確認して順序を守る

---

#### ステップ4: 単体テスト実行評価

テスト実行結果を評価してカバレッジを分析し、品質を検証します。

前提: テストを実行し、Jacocoレポートを生成済み

```bash
# リポジトリルート（ai_driven_dev_202601/）で実行
cd ../../../  # プロジェクトルートからリポジトリルートへ移動

# テストを実行してJacocoレポートを生成
./gradlew :jsf-person-sdd-wf:test :jsf-person-sdd-wf:jacocoTestReport
```

```
@agent_skills/struts-to-jsf-migration/instructions/test_evaluation.md

テスト実行結果を評価してください

パラメータ:
* project_root: projects/sdd-wf/person/jsf-person
* jacoco_reports_dir: build/reports/jacoco/test
* test_type: unit
* spec_directory: projects/sdd-wf/person/jsf-person/specs/baseline
```

AIが：
1. 📊 Jacocoレポート（XML）を読み込む
2. 📈 カバレッジ評価（行、分岐、メソッド）
3. 🔍 パッケージ別/クラス別/メソッド別カバレッジ分析
4. ⚠️ デッドコード検出
5. 📋 評価レポート生成
6. 💬 ユーザーに推奨アクションを提示

重要：
* テスト実行は不要（既に実行済みのレポートを評価）
* 問題を発見してもユーザー確認なしに修正しない
* Managed Bean はカバレッジ除外推奨（UI層はE2Eで検証）
* カバレッジ不足やデッドコードを具体的に提案
* 必要に応じてステップ2（詳細設計）に戻ってループ

🔄 フィードバックループ:
```
詳細設計 → 本番コード生成 → テストコード生成 → テスト実行 → テスト評価
    ↑                                                        ↓
    └──────────────────── フィードバック ←──────────────────┘
```

---

#### ステップ5: 結合テスト生成（全ドメイン完了後）

単体テスト完了後に、結合テスト（Integration Test）を生成します。

```
@agent_skills/struts-to-jsf-migration/instructions/it_generation.md

結合テストコードを生成してください

パラメータ:
* project_root: projects/sdd-wf/person/jsf-person
* spec_directory: projects/sdd-wf/person/jsf-person/specs/baseline
```

重要: テスト生成のみを実施（テスト実行はリポジトリルートから手動で実行）

AIが：
1. 📄 basic_design/behaviors.md（結合テストシナリオ）を読み込む
2. 🧪 JUnit 5 + Weld SE を使用した結合テストを生成
   * Service層以下（Service + Entity + DB）の連携テスト
   * 実際のDBアクセス（メモリDB）
   * モックは使用しない
   * アプリケーションサーバー不要
   * 既存テストがある場合は、削除せずに差分のみを反映する
3. 🏷️ `@Tag("integration")` で結合テストを分離

実行方法:
```bash
# リポジトリルート（ai_driven_dev_202601/）で実行
cd ../../../  # プロジェクトルートからリポジトリルートへ移動

# 結合テストを実行してJacocoレポートを生成
./gradlew :jsf-person-sdd-wf:integrationTest :jsf-person-sdd-wf:jacocoIntegrationTestReport

# テスト実行後、評価を実施
# @agent_skills/struts-to-jsf-migration/instructions/test_evaluation.md
# パラメータ: test_type=integration, jacoco_reports_dir=build/reports/jacoco/integrationTest
```

---

#### ステップ6: E2Eテスト生成（全ドメイン完了後）

全画面実装完了後に、E2Eテスト（End-to-End Test）を生成します。

```
@agent_skills/struts-to-jsf-migration/instructions/e2e_test_generation.md

E2Eテストコードを生成してください

パラメータ:
* project_root: projects/sdd-wf/person/jsf-person
* spec_directory: projects/sdd-wf/person/jsf-person/specs/baseline
```

重要: テスト生成のみを実施（テスト実行はリポジトリルートから手動で実行。アプリケーションサーバー起動が前提）

AIが：
1. 📄 requirements/behaviors.md（E2Eテストシナリオ）を読み込む
2. 🧪 Playwright を使用したE2Eテストを生成
   * 複数画面にまたがるフローをテスト（一覧 → 入力 → 確認 → 登録）
   * 実際のブラウザ操作
   * 実際のDBアクセスを含む
   * 既存テストがある場合は、削除せずに差分のみを反映する
3. 🏷️ `@Tag("e2e")` でE2Eテストを分離

実行方法:
```bash
# リポジトリルート（ai_driven_dev_202601/）で実行
cd ../../../  # プロジェクトルートからリポジトリルートへ移動

# 1. アプリケーションをビルド＆デプロイ
./gradlew :jsf-person-sdd-wf:war
./gradlew :jsf-person-sdd-wf:deploy

# 2. 別ターミナルでE2Eテストを実行してJacocoレポートを生成
./gradlew :jsf-person-sdd-wf:e2eTest :jsf-person-sdd-wf:jacocoE2eTestReport

# テスト実行後、評価を実施
# @agent_skills/struts-to-jsf-migration/instructions/test_evaluation.md
# パラメータ: test_type=e2e, jacoco_reports_dir=build/reports/jacoco/e2eTest
```

---

### 🔄 基本設計変更対応（手戻り・拡張案件）

結合テストやE2Eテストで不具合が見つかり、基本設計に戻る必要がある場合や、拡張案件で新機能を追加する場合に使用します。

#### 使用方法

1. **基本設計SPECのマスターファイルを更新**
   ```bash
   vim specs/baseline/basic_design/functional_design.md
   vim specs/baseline/basic_design/screen_design.md
   ```

2. **CHANGES.mdを作成して変更内容を記載**
   
   変更の粒度に応じて適切な場所にCHANGES.mdを作成：
   
   * 共通設計の変更（Entity、アーキテクチャ等）:
     ```bash
     vim specs/baseline/basic_design/common/CHANGES.md
     ```
   
   * 画面グループ固有設計の変更（画面、機能等）:
     ```bash
     vim specs/baseline/basic_design/person_management/CHANGES.md
     ```
   
   注意: 画面グループは関連する画面群（一覧、入力、確認等）をまとめたもの

3. **変更対応を実行**
   ```
   @agent_skills/struts-to-jsf-migration/instructions/basic_design_change.md
   
   基本設計の変更を適用してください。
   
   パラメータ:
   * project_root: projects/sdd-wf/person/jsf-person
   * spec_directory: projects/sdd-wf/person/jsf-person/specs/baseline
   ```

AIが：
1. 📄 CHANGES.md（変更差分ファイル）を読み込み
2. 🔍 変更の影響を受けるファイル（詳細設計、コード、XHTML、テスト）を特定
3. 📋 変更タスクファイル（`tasks/change_tasks.md`）を生成
4. 🎯 既存の指示書を呼び出して、影響を受けるファイルを更新
5. ✅ すべての変更適用後、CHANGES.mdをアーカイブ

#### ディレクトリ構造

```
specs/baseline/basic_design/
  ├── common/                           # 共通設計（必須）
  │   ├── architecture_design.md        # マスター（自由に編集）
  │   ├── data_model.md                 # マスター（自由に編集）
  │   ├── external_interface.md         # マスター（自由に編集）
  │   ├── functional_design.md          # 共通機能設計
  │   ├── behaviors.md                  # 共通振る舞い
  │   ├── CHANGES.md                    # 共通設計の変更管理
  │   └── changes_archive/              # 共通設計の適用済み変更
  │       └── 20260120_entity_update.md
  └── person_management/                # Person管理画面グループ
      ├── functional_design.md          # マスター（自由に編集）- 画面一覧、画面遷移図
      ├── screen_design.md              # マスター（自由に編集）- 全画面の画面設計
      ├── behaviors.md                  # E2Eテスト用（複数画面またぐフロー、Gherkin記法）
      ├── CHANGES.md                    # 画面グループ設計の変更管理
      └── changes_archive/              # 画面グループ設計の適用済み変更
          ├── 20260118_person_edit.md
          └── 20260125_validation_update.md
```

注意:
* JSFは画面中心のサーバーサイドMVCフレームワーク
* 画面グループ: 関連する画面群（一覧、入力、確認等）をまとめたもの
* 画面グループ内で画面遷移フローを持つ

---

### 📚 詳細情報

* マイグレーション詳細: `@agent_skills/struts-to-jsf-migration/README.md` を参照
* Jakarta EE開発詳細: `@agent_skills/jakarta-ee-api-base/README.md` を参照

#### 開発原則

このプロジェクトは、以下の原則に従って開発されます：

* 場所: `@agent_skills/struts-to-jsf-migration/principles/`
  * [architecture.md](../../../agent_skills/struts-to-jsf-migration/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](../../../agent_skills/struts-to-jsf-migration/principles/security.md) - セキュリティ標準
  * [common_rules.md](../../../agent_skills/struts-to-jsf-migration/principles/common_rules.md) - 共通ルール、マッピング規則

* 主な内容:
  * 標準技術スタック（Jakarta EE 10、Jakarta Faces 4.0、JPA 3.1）
  * レイヤードアーキテクチャ（Managed Bean、Service、Entity）
  * 開発標準（命名規則、コーディング規約、バリデーション）
  * セキュリティ実装（JWT認証、パスワード管理）
  * トランザクション管理、セッション管理（ViewScoped、Flash Scope）
  * テスト戦略、パフォーマンス考慮事項

## 🎯 マイグレーション対象（Struts → JSF）

### Strutsの構成要素

* ActionForm: PersonForm（フォームデータの保持）
* Action: PersonListAction、PersonInputAction、PersonUpdateAction等
* EJB: PersonServiceBean（`@Stateless`、JNDIルックアップ）
* DAO: PersonDao（JDBC + DataSource）
* JSP: personList.jsp、personInput.jsp等（Strutsタグライブラリ）

### JSFの構成要素

* Managed Bean: PersonListBean、PersonInputBean、PersonConfirmBean（`@Named`, `@ViewScoped`）
* CDI: `@Inject`で依存性注入
* Service: PersonService（`@RequestScoped`, `@Transactional`）
* JPA: Person Entity（`@Entity`）、EntityManager
* Facelets XHTML: personList.xhtml、personInput.xhtml、personConfirm.xhtml

### データベースの継続性

* データベーススキーマは変更しません
* 既存のPERSONテーブルをそのまま使用
* JPA Entityで既存テーブルにマッピング

## 🚀 セットアップとコマンド実行ガイド

### 前提条件

* JDK 21以上
* Gradle 8.x以上
* Payara Server 6（プロジェクトルートの`payara6/`に配置）
* HSQLDB（プロジェクトルートの`hsqldb/`に配置）

> Note: ① と ② の手順は、ルートの`README.md`を参照してください。

### ③ 依存関係の確認

このプロジェクトを開始する前に、以下が起動していることを確認してください：

* ① HSQLDBサーバー （`./gradlew startHsqldb`）
* ② Payara Server （`./gradlew startPayara`）

### ④ プロジェクトを開始するときに1回だけ実行

```bash
# 1. データベーステーブルとデータを作成
./gradlew :jsf-person-sdd-wf:setupHsqldb

# 2. プロジェクトをビルド
./gradlew :jsf-person-sdd-wf:build

# 3. プロジェクトをデプロイ（データソースも自動作成）
./gradlew :jsf-person-sdd-wf:deploy
```

> Note: デプロイ時にデータソース（`jdbc/HsqldbDS`）が自動的に作成されます。

### ⑤ プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# プロジェクトをアンデプロイ
./gradlew :jsf-person-sdd-wf:undeploy
```

### ⑥ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
./gradlew :jsf-person-sdd-wf:build :jsf-person-sdd-wf:deploy
```

または個別に実行：

```bash
./gradlew :jsf-person-sdd-wf:build
./gradlew :jsf-person-sdd-wf:deploy
```

## 📍 アクセスURL

デプロイ後、以下のURLにアクセス：

* Person一覧: http://localhost:8080/jsf-person-sdd-wf/person/personList.xhtml
* Person入力（新規）: http://localhost:8080/jsf-person-sdd-wf/person/personInput.xhtml
* Person入力（編集）: http://localhost:8080/jsf-person-sdd-wf/person/personInput.xhtml?personId=1
* Person確認: http://localhost:8080/jsf-person-sdd-wf/person/personConfirm.xhtml

## ✅ 実装状況

### 完了した機能

- ✅ セットアップ: プロジェクト構成、依存関係、設定ファイル
- ✅ FUNC_001: Person Entity、PersonService（JPA + CDI）
- ✅ SCREEN_001_PersonList: 一覧表示、削除機能
- ✅ SCREEN_002_PersonInput: 新規登録・編集画面、Bean Validation
- ✅ SCREEN_003_PersonConfirm: 確認画面、登録・更新処理

### 技術的な特徴

- JSF 4.0 Managed Bean: `@Named` + `@ViewScoped` でステート管理
- CDI依存性注入: `@Inject` でサービス層を注入
- JPA + JTA: EntityManagerによる型安全なデータアクセス、トランザクション管理
- Bean Validation: `@NotNull`, `@Size`, `@Min`, `@Max` による宣言的バリデーション
- Facelets XHTML: JSF標準のビューテクノロジー
- データソース: `jdbc/HsqldbDS` (HSQLDB) をJNDI経由で利用

## 🎯 プロジェクト構成

```
projects/sdd-wf/person/jsf-person/
├── specs/                          # 仕様書（マイグレーション時に生成）
│   ├── README.md                   # Specs構成ガイド
│   └── baseline/
│       ├── requirements/           # システム要件
│       │   ├── requirements.md    # 要件定義書
│       │   └── behaviors.md       # 要件レベルの振る舞い
│       ├── basic_design/           # 基本設計
│       │   ├── common/             # 共通ドメイン（必須）
│       │   │   ├── architecture_design.md
│       │   │   ├── data_model.md
│       │   │   ├── external_interface.md
│       │   │   ├── functional_design.md
│       │   │   ├── behaviors.md
│       │   │   ├── CHANGES.md
│       │   │   └── changes_archive/
│       │   └── person_management/  # Person管理ドメイン
│       │       ├── functional_design.md
│       │       ├── screen_design.md
│       │       ├── behaviors.md   # E2Eテスト用（Gherkin記法）
│       │       ├── CHANGES.md
│       │       └── changes_archive/
│       └── detailed_design/        # 詳細設計
│           ├── common/             # 共通詳細設計
│           │   ├── detailed_design.md
│           │   └── behaviors.md   # 単体テスト用
│           ├── FUNC_001_PersonList/
│           │   ├── detailed_design.md
│           │   └── behaviors.md   # 単体テスト用
│           ├── FUNC_002_PersonInput/
│           │   ├── detailed_design.md
│           │   └── behaviors.md
│           └── FUNC_003_PersonConfirm/
│               ├── detailed_design.md
│               └── behaviors.md
├── tasks/                          # タスクリスト（AI生成）
│   ├── tasks.md
│   ├── setup.md
│   ├── FUNC_001_common.md
│   ├── FUNC_002_PersonList.md
│   ├── FUNC_003_PersonInput.md
│   └── FUNC_003_PersonConfirm.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── pro/kensait/jsf/person/
│   │   │       ├── bean/
│   │   │       │   ├── PersonListBean.java        # JSF Managed Bean
│   │   │       │   ├── PersonInputBean.java
│   │   │       │   └── PersonConfirmBean.java
│   │   │       ├── entity/
│   │   │       │   └── Person.java                # JPA Entity
│   │   │       └── service/
│   │   │           └── PersonService.java         # CDI Service
│   │   ├── resources/
│   │   │   └── META-INF/
│   │   │       └── persistence.xml            # JPA設定
│   │   └── webapp/
│   │       ├── person/
│   │       │   ├── personList.xhtml       # Facelets XHTML
│   │       │   ├── personInput.xhtml
│   │       │   └── personConfirm.xhtml
│   │       ├── resources/
│   │       │   └── css/
│   │       │       └── style.css
│   │       └── WEB-INF/
│   │           ├── web.xml
│   │           ├── beans.xml
│   │           └── faces-config.xml
│   └── test/
│       └── java/
│           └── pro/kensait/jsf/person/
│               ├── bean/
│               │   ├── PersonListBeanTest.java
│               │   ├── PersonInputBeanTest.java
│               │   └── PersonConfirmBeanTest.java
│               └── service/
│                   └── PersonServiceTest.java
├── sql/
│   └── hsqldb/                     # SQLスクリプト
│       ├── 1_PERSON_DROP.sql
│       ├── 2_PERSON_DDL.sql
│       └── 3_PERSON_DML.sql
└── build/
    └── libs/
        └── jsf-person-sdd-wf.war
```

## 🔧 使用している技術

* Jakarta EE 10
* Payara Server 6
* Jakarta Faces (JSF) 4.0
* Jakarta Persistence (JPA) 3.1
* Jakarta Transactions (JTA)
* Jakarta CDI 4.0
* HSQLDB 2.7.x

## 📝 データソース設定について

このプロジェクトはルートの`build.gradle`で定義されたタスクを使用してデータソースを作成します。

### 設定内容

* JNDI名: `jdbc/HsqldbDS`
* データベース: `testdb`
* ユーザー: `SA`
* パスワード: （空文字）
* TCPサーバー: `localhost:9001`
* 接続URL: `jdbc:hsqldb:hsql://localhost:9001/testdb`

データソースはPayara Serverのドメイン設定に登録されます。

### 設定ファイル

* env-conf.gradle: データソースのJNDI名と接続情報を定義
* persistence.xml: JPA設定でデータソースを参照（`<jta-data-source>jdbc/HsqldbDS</jta-data-source>`）

### ⚠️ 注意事項

* HSQLDB Databaseサーバーが起動している必要があります
* データソースは自動的に作成されます（初回デプロイ時）
* 仕様書では `java:app/jdbc/testdb` と記載されていますが、実装環境では `jdbc/HsqldbDS` を使用します

## 🛑 アプリケーションを停止する

### アプリケーションのアンデプロイ

```bash
./gradlew :jsf-person-sdd-wf:undeploy
```

### Payara Server全体を停止

```bash
./gradlew stopPayara
```

### HSQLDBサーバーを停止

```bash
./gradlew stopHsqldb
```

## 🔍 ログ監視

別のターミナルでログをリアルタイム監視：

```bash
tail -f -n 50 payara6/glassfish/domains/domain1/logs/server.log
```

> Note: WindowsではGit Bashを使用してください。

## 📚 アーキテクチャ（Struts → JSF）

### Strutsのアーキテクチャ（移行元）

```
JSP View (Struts Tags)
    ↓
Action (Controller)
    ↓
EJB Service (@Stateless, JNDI Lookup)
    ↓
DAO (JDBC + DataSource)
    ↓
Database (HSQLDB)
```

### JSFのアーキテクチャ（移行先）

```
JSF View (Facelets XHTML)
    ↓
JSF Managed Bean (@Named, @ViewScoped)
    ↓
CDI Service (@RequestScoped, @Transactional)
    ↓
JPA Entity (@Entity)
    ↓
Database (HSQLDB)
```

### マイグレーションのポイント

* ActionForm → Managed Bean: フォームデータはManaged Beanのプロパティで管理
* Action → アクションメソッド: `execute()`メソッドがアクションメソッドに変換
* EJB（JNDI） → CDI（@Inject）: 依存性注入で簡潔に
* DAO（JDBC） → JPA: JPQL/EntityManagerで型安全に
* JSPタグ → Faceletsタグ: `<logic:iterate>` → `<h:dataTable>`、`<html:form>` → `<h:form>`等
* データソースJNDI: 実装環境では `jdbc/HsqldbDS` を使用（persistence.xmlで設定）

### 主要クラス

#### 1. PersonListBean.java (JSF Managed Bean)

* Struts: PersonListAction
* `@Named`と`@ViewScoped`を使用して、画面とビジネスロジックを仲介
* アクションメソッドで画面遷移を制御

#### 2. PersonService.java (CDI Service)

* Struts: PersonServiceBean（EJB）
* `@RequestScoped`と`@Transactional`でトランザクション管理
* EntityManagerを使用してJPQLでCRUD操作

#### 3. Person.java (JPA Entity)

* Struts: Personモデル（POJO）
* `@Entity`でデータベーステーブルとマッピング
* Bean Validationで検証ルールを宣言的に定義

## 🧹 SDD成果物のクリーンアップ

仕様駆動開発により何度でも再実装できます。詳細は [ルートREADMEのSDDクリーンアップ節](../../../../README.md#仕様駆動開発sddプロジェクトの成果物クリーンアップ) を参照してください。

```bash
# 詳細設計SPECのみ削除
./gradlew :jsf-person-sdd-wf:cleanDetailedDesign

# 本番コード・単体テストコードを削除（src/main/, src/test/, build/）
./gradlew :jsf-person-sdd-wf:cleanCode

# すべて削除（requirements/, basic_design/ は保護）
./gradlew :jsf-person-sdd-wf:cleanAllSdd
```

* cleanCode の削除対象: 本番コード（src/main/）、単体テストコード（src/test/）、ビルド成果物（build/）。ディレクトリ構造は空で保持されます。
* 保護されるSPEC: `specs/baseline/requirements/`, `specs/baseline/basic_design/`

## 📖 参考リンク

### Agent Skills

* [Struts to JSF Migration README](../../../agent_skills/struts-to-jsf-migration/README.md) - マイグレーションガイド
* [Jakarta EE API Base README](../../../agent_skills/jakarta-ee-api-base/README.md) - 開発ガイド
* [開発原則](../../../agent_skills/struts-to-jsf-migration/principles/)
  * [architecture.md](../../../agent_skills/struts-to-jsf-migration/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](../../../agent_skills/struts-to-jsf-migration/principles/security.md) - セキュリティ標準
  * [common_rules.md](../../../agent_skills/struts-to-jsf-migration/principles/common_rules.md) - 共通ルール、マッピング規則

### Jakarta EE仕様

* [Jakarta EE 10 Platform](https://jakarta.ee/specifications/platform/10/)
* [Jakarta Server Faces 4.0](https://jakarta.ee/specifications/faces/4.0/)
* [Jakarta Persistence (JPA) 3.1](https://jakarta.ee/specifications/persistence/3.1/)
* [Hibernate ORM Documentation](https://hibernate.org/orm/documentation/6.4/)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。
