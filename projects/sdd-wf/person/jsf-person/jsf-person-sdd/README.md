# jsf-person-sdd プロジェクト

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

マイグレーションは以下の7段階プロセスで進めます：

```
ステップ1: 既存コード分析（Strutsコード → 仕様書）
    ↓
ステップ2: タスク分解（仕様書 → タスクリスト）
    ↓
ステップ3: 詳細設計（画面単位で詳細設計）← AIと対話しながら
    ↓
ステップ4: コード生成（詳細設計→実装→単体テスト）（タスクリスト → JSFコード）
    ↓
ステップ5: 単体テスト実行評価（テスト実行 → カバレッジ分析 → フィードバック）
    ↓
ステップ6: 結合テスト生成（basic_design/behaviors.md → JUnit + Weld SE）
    ↓
ステップ7: E2Eテスト生成（requirements/behaviors.md → Playwright）
```

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
* spec_output_directory: projects/sdd-wf/person/jsf-person-sdd/specs
```

* 生成される仕様書:
  * `requirements.md` - システムの目的、機能要件
  * `architecture_design.md` - 技術スタック、レイヤー構成
  * `functional_design.md` - 画面一覧、画面遷移
  * `data_model.md` - エンティティ、テーブル定義
  * `screen_design.md` - 画面レイアウト、入力項目
  * `behaviors.md` - 画面の振る舞い、バリデーション

---

#### ステップ2: タスク分解（既存コード分析後）

生成された仕様書から実装タスクを分解します。

```
@agent_skills/struts-to-jsf-migration/instructions/task_breakdown.md

全タスクを分解してください。

パラメータ:
* project_root: projects/sdd-wf/person/jsf-person-sdd
* spec_directory: projects/sdd-wf/person/jsf-person-sdd/specs
```

* 生成されるファイル: `tasks/*.md`（タスクリスト）

---

#### ステップ3: 詳細設計（tasks/tasks.mdの順序に従う）

**重要**: 実行順序は `tasks/tasks.md` の「タスク概要」表と「実行順序」セクションを参照してください。
- 「依存タスク」列: このタスクを開始する前に完了が必要なタスク
- 「並行実行可能」列: このタスクと同時に実行可能な他のタスク
- 「レベル」列: 同じレベルのタスクは並行実行可能

コマンドテンプレート:

```
@agent_skills/struts-to-jsf-migration/instructions/detailed_design.md

[タスクID]の詳細設計書を作成してください。

パラメータ:
* project_root: projects/sdd-wf/person/jsf-person-sdd
* spec_directory: projects/sdd-wf/person/jsf-person-sdd/specs/baseline
* target_type: [tasks/tasks.mdで確認したタスクID]
```

対話の流れ:
1. AIがSPEC（basic_design/）を読み込み、理解した内容を説明します
2. AIが不明点を質問します（Managed Bean設計、バリデーション、画面遷移等）
3. あなたが回答します
4. `specs/baseline/detailed_design/[タスクID]/detailed_design.md` と `behaviors.md` が生成されます

注意:
* target_typeは `tasks/tasks.md` のタスクファイル名（拡張子なし）と一致させる
* 依存タスクの詳細設計が完了してから実行する（tasks/tasks.mdの「依存タスク」列を参照）

---

#### ステップ4: コード生成（詳細設計→実装→単体テスト）（詳細設計完了後）

詳細設計書に基づいてJSFコードを生成します。

**重要**: 実行順序は `tasks/tasks.md` の「タスク概要」表と「実行順序」セクションを参照してください。
- 「依存タスク」列を確認し、依存タスクが完了してから実行
- 「並行実行可能」列を確認し、並行実行可能なタスクは同時に実装可能

> 単体テストの方針: タスク粒度内のコンポーネント間は実際の連携をテスト。タスク外の依存関係のみモック化。

コマンドテンプレート:

```
@agent_skills/struts-to-jsf-migration/instructions/code_generation.md

[タスクID]を実装してください。

パラメータ:
* project_root: projects/sdd-wf/person/jsf-person-sdd
* task_file: projects/sdd-wf/person/jsf-person-sdd/tasks/[タスクファイル名]
```

使用例（setup）:

```
@agent_skills/struts-to-jsf-migration/instructions/code_generation.md

setupを実装してください。

パラメータ:
* project_root: projects/sdd-wf/person/jsf-person-sdd
* task_file: projects/sdd-wf/person/jsf-person-sdd/tasks/setup.md
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
* project_root: projects/sdd-wf/person/jsf-person-sdd
* task_file: projects/sdd-wf/person/jsf-person-sdd/tasks/FUNC_001_xxx.md
```

注意: 実際のタスクファイル名は `tasks/tasks.md` を参照してください

使用例（FUNC_002）:

```
@agent_skills/struts-to-jsf-migration/instructions/code_generation.md

FUNC_002を実装してください。

パラメータ:
* project_root: projects/sdd-wf/person/jsf-person-sdd
* task_file: projects/sdd-wf/person/jsf-person-sdd/tasks/FUNC_002_PersonList.md
```

注意:
* タスクファイル名は `tasks/tasks.md` のタスクファイル列と一致させる
* 各タスクファイル（FUNC_XXX.md）のヘッダーにある「依存タスク」を確認して順序を守る

---

#### ステップ5: 単体テスト実行評価

単体テストを実行してカバレッジを分析し、品質を検証します。

```
@agent_skills/struts-to-jsf-migration/instructions/unit_test_execution.md

単体テストを実行してください。

パラメータ:
* project_root: projects/sdd-wf/person/jsf-person-sdd
* target_type: FUNC_002_PersonList
```

AIが：
1. 🧪 テスト実行（gradle test jacocoTestReport）
2. 📊 テスト結果とカバレッジ分析
3. 🔍 問題の分類（テスト失敗、必要な振る舞い、デッドコード）
4. 📋 フィードバックレポート生成
5. 💬 ユーザーに推奨アクションを提示

重要：
* 問題を発見してもユーザー確認なしに修正しない
* Managed Bean はカバレッジ除外推奨（UI層はE2Eで検証）
* 必要に応じてステップ3（詳細設計）に戻ってループ

🔄 フィードバックループ:
```
詳細設計 → コード生成 → テスト実行評価
    ↑                         ↓
    └──── フィードバック ←────┘
```

---

#### ステップ6: 結合テスト生成（単体テスト完了後）

単体テスト完了後に、結合テスト（Integration Test）を生成します。

```
@agent_skills/struts-to-jsf-migration/instructions/it_generation.md

結合テストを生成してください。

パラメータ:
* project_root: projects/sdd-wf/person/jsf-person-sdd
* spec_directory: projects/sdd-wf/person/jsf-person-sdd/specs/baseline
```

AIが：
1. 📄 basic_design/behaviors.md（結合テストシナリオ）を読み込む
2. 🧪 JUnit 5 + Weld SE を使用した結合テストを生成
   * Service層以下（Service + Entity + DB）の連携テスト
   * 実際のDBアクセス（メモリDB）
   * モックは使用しない
   * アプリケーションサーバー不要
3. 🏷️ `@Tag("integration")` で結合テストを分離

実行方法:
```bash
# 結合テストを実行
./gradlew integrationTest
```

---

#### ステップ7: E2Eテスト生成（実装完了後）

全画面実装完了後に、E2Eテスト（End-to-End Test）を生成します。

```
@agent_skills/struts-to-jsf-migration/instructions/e2e_test_generation.md

E2Eテストを生成してください。

パラメータ:
* project_root: projects/sdd-wf/person/jsf-person-sdd
* spec_directory: projects/sdd-wf/person/jsf-person-sdd/specs/baseline
```

AIが：
1. 📄 requirements/behaviors.md（E2Eテストシナリオ）を読み込む
2. 🧪 Playwright を使用したE2Eテストを生成
   * 複数画面にまたがるフローをテスト（一覧 → 入力 → 確認 → 登録）
   * 実際のブラウザ操作
   * 実際のDBアクセスを含む
3. 🏷️ `@Tag("e2e")` でE2Eテストを分離

実行方法:
```bash
# アプリケーションサーバーを起動
./gradlew run

# 別ターミナルでE2Eテストを実行
./gradlew e2eTest
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
   ```bash
   cp agent_skills/struts-to-jsf-migration/templates/basic_design/CHANGES_template.md \
      specs/baseline/basic_design/CHANGES.md
   vim specs/baseline/basic_design/CHANGES.md
   ```

3. **変更対応を実行**
   ```
   @agent_skills/struts-to-jsf-migration/instructions/basic_design_change.md
   
   基本設計の変更を適用してください。
   
   パラメータ:
   * project_root: projects/sdd-wf/person/jsf-person-sdd
   * spec_directory: projects/sdd-wf/person/jsf-person-sdd/specs/baseline
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
  ├── functional_design.md      # マスター（自由に編集）
  ├── screen_design.md          # マスター（自由に編集）
  ├── data_model.md             # マスター（自由に編集）
  ├── CHANGES.md                # アクティブな変更（未適用）
  └── changes_archive/          # 履歴
      ├── 20260118_person_edit.md
      └── 20260125_validation_update.md
```

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
./gradlew :jsf-person-sdd:setupHsqldb

# 2. プロジェクトをビルド
./gradlew :jsf-person-sdd:build

# 3. プロジェクトをデプロイ（データソースも自動作成）
./gradlew :jsf-person-sdd:deploy
```

> Note: デプロイ時にデータソース（`jdbc/HsqldbDS`）が自動的に作成されます。

### ⑤ プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# プロジェクトをアンデプロイ
./gradlew :jsf-person-sdd:undeploy
```

### ⑥ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
./gradlew :jsf-person-sdd:build :jsf-person-sdd:deploy
```

または個別に実行：

```bash
./gradlew :jsf-person-sdd:build
./gradlew :jsf-person-sdd:deploy
```

## 📍 アクセスURL

デプロイ後、以下のURLにアクセス：

* Person一覧: http://localhost:8080/jsf-person-sdd/person/personList.xhtml
* Person入力（新規）: http://localhost:8080/jsf-person-sdd/person/personInput.xhtml
* Person入力（編集）: http://localhost:8080/jsf-person-sdd/person/personInput.xhtml?personId=1
* Person確認: http://localhost:8080/jsf-person-sdd/person/personConfirm.xhtml

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
projects/sdd-wf/person/jsf-person-sdd/
├── specs/                          # 仕様書（マイグレーション時に生成）
│   └── baseline/
│       ├── requirements/           # システム要件
│       │   ├── requirements.md    # 要件定義書
│       │   └── behaviors.md       # E2Eテスト用（要件を外形的に捉えた振る舞い）
│       ├── basic_design/           # 基本設計
│       │   ├── architecture_design.md
│       │   ├── functional_design.md
│       │   ├── data_model.md
│       │   ├── screen_design.md
│       │   ├── external_interface.md
│       │   └── behaviors.md       # 結合テスト用（基本設計を外形的に捉えた振る舞い）
│       └── detailed_design/        # 詳細設計
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
        └── jsf-person-sdd.war
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
./gradlew :jsf-person-sdd:undeploy
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
