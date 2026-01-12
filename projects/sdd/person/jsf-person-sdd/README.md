# jsf-person-sdd プロジェクト

## 📖 概要

Apache Struts 1.xからJakarta Faces (JSF) 4.0にマイグレーションされた人材管理システムです。
JSFとJPA (Java Persistence API) を組み合わせたデータベースCRUD操作を実装しています。

* **移行元**: `@projects/master/person/struts-person`（Apache Struts 1.3.10）
* **移行先**: このプロジェクト（Jakarta Faces 4.0 + Jakarta EE 10）

> **Note:** このプロジェクトは**Strutsマイグレーション研修用プロジェクト**です。

> **マイグレーションアプローチ:**
> * 既存のStrutsコードから仕様書を生成（既存コード分析）
> * 仕様書を検証・調整してJSFアーキテクチャに適応
> * 仕様書からJSFコードを生成（仕様駆動開発）
> * **汎用Agent Skills** (`agent_skills/struts-to-jsf-migration/`) を使用したマイグレーション

## 🤖 Agent Skillsを使ったマイグレーション

このプロジェクトは、汎用的な **Struts to JSF マイグレーション Agent Skills** を使用してマイグレーションします。

マイグレーションは以下の**4段階プロセス**で進めます：

```
ステップ1: 既存コード分析（Strutsコード → 仕様書）
    ↓
ステップ2: タスク分解（仕様書 → タスクリスト）
    ↓
ステップ3: 詳細設計（画面単位で詳細設計）← AIと対話しながら
    ↓
ステップ4: コード生成（タスクリスト → JSFコード）
```

---

### 📋 マイグレーションフロー

#### ステップ1: 既存コード分析（最初に1回）

既存のStrutsプロジェクトから仕様書を生成します。

```
@agent_skills/struts-to-jsf-migration/instructions/reverse_engineering.md
@projects/sdd/person/struts-person

既存のStrutsプロジェクトから仕様書を生成してください。

パラメータ:
* struts_project_root: projects/master/person/struts-person
* spec_output_directory: projects/sdd/person/jsf-person-sdd/specs
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
* project_root: projects/sdd/person/jsf-person-sdd
* spec_directory: projects/sdd/person/jsf-person-sdd/specs
```

* 生成されるファイル: `tasks/*.md`（タスクリスト）

---

#### ステップ3: 詳細設計（タスク分解後、画面単位）

画面単位で詳細設計書を**AIと対話しながら**作成します。

```
@agent_skills/struts-to-jsf-migration/instructions/detailed_design.md

画面の詳細設計書を作成してください。

パラメータ:
* project_root: projects/sdd/person/jsf-person-sdd
* screen_id: SCREEN_001_PersonList
```

* 対話の流れ:
  1. AIが仕様書を読み込み、理解した内容を説明します
  2. AIが不明点を質問します（Managed Bean設計、バリデーション、画面遷移等）
  3. あなたが回答します
  4. 詳細設計書が生成されます

* 重要: 詳細設計は**対話的なプロセス**です。AIが質問してきたら、必ず回答してください。

---

#### ステップ4: コード生成（詳細設計完了後）

詳細設計書に基づいてJSFコードを生成します。

* 実行順序: 
  1. **セットアップタスク** → 2. **共通機能タスク** → 3. **各画面実装**

##### 4-1. セットアップタスク（最初に1回）

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md

セットアップタスクを実行してください。

パラメータ:
* project_root: projects/sdd/person/jsf-person-sdd
* task_file: projects/sdd/person/jsf-person-sdd/tasks/setup_tasks.md
* skip_infrastructure: true
```

##### 4-2. 共通機能タスク（セットアップ後に1回）

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md

共通機能タスクを実行してください。

パラメータ:
* project_root: projects/sdd/person/jsf-person-sdd
* task_file: projects/sdd/person/jsf-person-sdd/tasks/common_tasks.md
```

* 実装される共通機能:
  * Personエンティティ（JPA）
  * PersonService（CDI + JPA）
  * 設定ファイル（persistence.xml、beans.xml等）

##### 4-3. 各画面の実装（共通機能完了後）

* SCREEN_001_PersonList（Person一覧画面）:

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md

Person一覧画面を実装してください。

パラメータ:
* project_root: projects/sdd/person/jsf-person-sdd
* task_file: projects/sdd/person/jsf-person-sdd/tasks/SCREEN_001_PersonList.md
```

* SCREEN_002_PersonInput（Person入力画面）:

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md

Person入力画面を実装してください。

パラメータ:
* project_root: projects/sdd/person/jsf-person-sdd
* task_file: projects/sdd/person/jsf-person-sdd/tasks/SCREEN_002_PersonInput.md
```

* SCREEN_003_PersonConfirm（Person確認画面）:

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md

Person確認画面を実装してください。

パラメータ:
* project_root: projects/sdd/person/jsf-person-sdd
* task_file: projects/sdd/person/jsf-person-sdd/tasks/SCREEN_003_PersonConfirm.md
```

---

### 📚 詳細情報

* マイグレーション詳細: `@agent_skills/struts-to-jsf-migration/README.md` を参照
* Jakarta EE開発詳細: `@agent_skills/jakarta-ee-standard/README.md` を参照

## 🎯 マイグレーション対象（Struts → JSF）

### Strutsの構成要素

* **ActionForm**: PersonForm（フォームデータの保持）
* **Action**: PersonListAction、PersonInputAction、PersonUpdateAction等
* **EJB**: PersonServiceBean（`@Stateless`、JNDIルックアップ）
* **DAO**: PersonDao（JDBC + DataSource）
* **JSP**: personList.jsp、personInput.jsp等（Strutsタグライブラリ）

### JSFの構成要素

* **Managed Bean**: PersonListBean、PersonInputBean、PersonConfirmBean（`@Named`, `@ViewScoped`）
* **CDI**: `@Inject`で依存性注入
* **Service**: PersonService（`@RequestScoped`, `@Transactional`）
* **JPA**: Person Entity（`@Entity`）、EntityManager
* **Facelets XHTML**: personList.xhtml、personInput.xhtml、personConfirm.xhtml

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

> **Note:** ① と ② の手順は、ルートの`README.md`を参照してください。

### ③ 依存関係の確認

このプロジェクトを開始する前に、以下が起動していることを確認してください：

* **① HSQLDBサーバー** （`./gradlew startHsqldb`）
* **② Payara Server** （`./gradlew startPayara`）

### ④ プロジェクトを開始するときに1回だけ実行

```bash
# 1. データベーステーブルとデータを作成
./gradlew :jsf-person-sdd:setupHsqldb

# 2. プロジェクトをビルド
./gradlew :jsf-person-sdd:build

# 3. プロジェクトをデプロイ（データソースも自動作成）
./gradlew :jsf-person-sdd:deploy
```

> **Note**: デプロイ時にデータソース（`jdbc/HsqldbDS`）が自動的に作成されます。

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

* **Person一覧**: http://localhost:8080/jsf-person-sdd/person/personList.xhtml
* **Person入力（新規）**: http://localhost:8080/jsf-person-sdd/person/personInput.xhtml
* **Person入力（編集）**: http://localhost:8080/jsf-person-sdd/person/personInput.xhtml?personId=1
* **Person確認**: http://localhost:8080/jsf-person-sdd/person/personConfirm.xhtml

## ✅ 実装状況

### 完了した機能

- ✅ **セットアップ**: プロジェクト構成、依存関係、設定ファイル
- ✅ **共通機能**: Person Entity、PersonService（JPA + CDI）
- ✅ **SCREEN_001_PersonList**: 一覧表示、削除機能
- ✅ **SCREEN_002_PersonInput**: 新規登録・編集画面、Bean Validation
- ✅ **SCREEN_003_PersonConfirm**: 確認画面、登録・更新処理

### 技術的な特徴

- **JSF 4.0 Managed Bean**: `@Named` + `@ViewScoped` でステート管理
- **CDI依存性注入**: `@Inject` でサービス層を注入
- **JPA + JTA**: EntityManagerによる型安全なデータアクセス、トランザクション管理
- **Bean Validation**: `@NotNull`, `@Size`, `@Min`, `@Max` による宣言的バリデーション
- **Facelets XHTML**: JSF標準のビューテクノロジー
- **データソース**: `jdbc/HsqldbDS` (HSQLDB) をJNDI経由で利用

## 🎯 プロジェクト構成

```
projects/sdd/person/jsf-person-sdd/
├── specs/                          # 仕様書（マイグレーション時に生成）
│   └── baseline/
│       ├── system/
│       │   ├── requirements.md
│       │   ├── architecture_design.md
│       │   ├── functional_design.md
│       │   ├── data_model.md
│       │   ├── behaviors.md
│       │   └── external_interface.md
│       └── screen/
│           ├── SCREEN_001_PersonList/
│           ├── SCREEN_002_PersonInput/
│           └── SCREEN_003_PersonConfirm/
├── tasks/                          # タスクリスト（AI生成）
│   ├── tasks.md
│   ├── setup_tasks.md
│   ├── common_tasks.md
│   ├── SCREEN_001_PersonList.md
│   ├── SCREEN_002_PersonInput.md
│   ├── SCREEN_003_PersonConfirm.md
│   └── integration_tasks.md
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

* **Jakarta EE 10**
* **Payara Server 6**
* **Jakarta Faces (JSF) 4.0**
* **Jakarta Persistence (JPA) 3.1**
* **Jakarta Transactions (JTA)**
* **Jakarta CDI 4.0**
* **HSQLDB 2.7.x**

## 📝 データソース設定について

このプロジェクトはルートの`build.gradle`で定義されたタスクを使用してデータソースを作成します。

### 設定内容

* **JNDI名**: `jdbc/HsqldbDS`
* **データベース**: `testdb`
* **ユーザー**: `SA`
* **パスワード**: （空文字）
* **TCPサーバー**: `localhost:9001`
* **接続URL**: `jdbc:hsqldb:hsql://localhost:9001/testdb`

データソースはPayara Serverのドメイン設定に登録されます。

### 設定ファイル

* **env-conf.gradle**: データソースのJNDI名と接続情報を定義
* **persistence.xml**: JPA設定でデータソースを参照（`<jta-data-source>jdbc/HsqldbDS</jta-data-source>`）

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

> **Note**: Windowsでは**Git Bash**を使用してください。

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

* **ActionForm → Managed Bean**: フォームデータはManaged Beanのプロパティで管理
* **Action → アクションメソッド**: `execute()`メソッドがアクションメソッドに変換
* **EJB（JNDI） → CDI（@Inject）**: 依存性注入で簡潔に
* **DAO（JDBC） → JPA**: JPQL/EntityManagerで型安全に
* **JSPタグ → Faceletsタグ**: `<logic:iterate>` → `<h:dataTable>`、`<html:form>` → `<h:form>`等
* **データソースJNDI**: 実装環境では `jdbc/HsqldbDS` を使用（persistence.xmlで設定）

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

* [Jakarta EE 10 Platform](https://jakarta.ee/specifications/platform/10/)
* [Jakarta Server Faces 4.0](https://jakarta.ee/specifications/faces/4.0/)
* [Jakarta Persistence (JPA) 3.1](https://jakarta.ee/specifications/persistence/3.1/)
* [Hibernate ORM Documentation](https://hibernate.org/orm/documentation/6.4/)
* [Agent Skills - Struts to JSF Migration](../../agent_skills/struts-to-jsf-migration/README.md)
* [Agent Skills - Jakarta EE Standard](../../agent_skills/jakarta-ee-standard/README.md)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。
