# Struts to JSF マイグレーション - クイックスタートガイド

Apache Struts 1.xからJakarta Faces (JSF) 4.0へのマイグレーションを4ステップで実現します。

---

## 🎯 マイグレーションアプローチ

```
Struts コード → 仕様書生成 → タスク分解 → 詳細設計 → JSF コード生成
 (既存コード分析)  (画面単位)  (AIと対話)  (コード生成)
```

Code-to-Codeの直接変換ではなく、一度仕様書として抽象化することで：
* レガシーな設計パターンを持ち込まない
* 最新のJakarta EE 10ベストプラクティスを採用
* ビジネスロジックを正確に保全

---

## 🚀 4ステップでマイグレーション

### ステップ1: 🔍 既存コード分析

既存のStrutsコードから仕様書を生成します。

```
@agent_skills/struts-to-jsf-migration/instructions/reverse_engineering.md
@projects/legacy/struts-app

既存のStrutsプロジェクトから仕様書を生成してください。

パラメータ:
* struts_project_root: projects/legacy/struts-app
* spec_output_directory: projects/jsf-migration/struts-app-jsf/specs
```

生成される仕様書:
* `requirements.md` - システムの目的、機能要件
* `architecture_design.md` - 技術スタック、レイヤー構成
* `functional_design.md` - 画面一覧、画面遷移
* `data_model.md` - エンティティ、テーブル定義
* `screen_design.md` - 画面レイアウト、入力項目
* `behaviors.md` - 画面の振る舞い、バリデーション

### ステップ2: 📋 タスク分解

生成された仕様書から実装タスクを分解します。

```
@agent_skills/struts-to-jsf-migration/instructions/task_breakdown.md

全タスクを分解してください。

パラメータ:
* project_root: projects/jsf-migration/struts-app-jsf
* spec_directory: projects/jsf-migration/struts-app-jsf/specs
```

画面単位でタスクファイルを生成：
* `setup_tasks.md` - セットアップ
* `common_tasks.md` - 共通機能（Entity、Service等）
* `SCREEN_XXX_*.md` - 各画面の実装タスク
* `integration_tasks.md` - 結合テスト

### ステップ3: 📝 詳細設計（画面単位、AIと対話）

画面単位で詳細設計書を作成します。

```
@agent_skills/struts-to-jsf-migration/instructions/detailed_design.md

画面の詳細設計書を作成してください。

パラメータ:
* project_root: projects/jsf-migration/struts-app-jsf
* screen_id: SCREEN_001_PersonList
```

AIと対話しながら：
* Managed Bean設計を確認
* バリデーションルールを確認
* 画面遷移とデータ受け渡しを確認
* 詳細設計書を生成

### ステップ4: ⚙️ JSFコード生成

詳細設計書に基づいてJSFコードを生成します。

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

セットアップタスクを実行してください。

パラメータ:
* project_root: projects/jsf-migration/struts-app-jsf
* task_file: projects/jsf-migration/struts-app-jsf/tasks/setup_tasks.md
* skip_infrastructure: true
```

その後、画面別にコードを生成：

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

Person一覧画面を実装してください。

パラメータ:
* project_root: projects/jsf-migration/struts-app-jsf
* task_file: projects/jsf-migration/struts-app-jsf/tasks/SCREEN_001_PersonList.md
```

---

## 📚 マイグレーション対象

### Struts 1.x → JSF 4.0

| Struts | JSF |
|--------|-----|
| ActionForm | Managed Bean（`@Named`, `@ViewScoped`） |
| Action | Managed Beanのアクションメソッド |
| EJB（JNDIルックアップ） | CDI（`@Inject`） |
| DAO（JDBC） | JPA（EntityManager） |
| JSPタグ（`<logic:iterate>`） | Faceletsタグ（`<h:dataTable>`） |
| `struts-config.xml` | ナビゲーション（戻り値 or `faces-config.xml`） |

---

## 📁 ディレクトリ構造

```
agent_skills/struts-to-jsf-migration/
├── README.md                         # このファイル
├── SKILL.md                          # 詳細ガイド
├── principles/
│   ├── architecture.md              # Jakarta EE APIアーキテクチャ標準
│   ├── security.md                  # セキュリティ標準
│   └── common_rules.md              # マイグレーション共通ルール、マッピング規則
└── instructions/
    ├── reverse_engineering.md        # ステップ1: 既存コード分析
    ├── task_breakdown.md             # ステップ2: タスク分解
    ├── detailed_design.md            # ステップ3: 詳細設計（画面単位）
    └── code_generation.md            # ステップ4: コード生成（参照用）
```

---

## 💡 実践例

### 人材管理システムのマイグレーション

既存のStruts人材管理システム（`struts-person`）をJSFにマイグレーションします。

#### ステップ1: 仕様書生成

```
@agent_skills/struts-to-jsf-migration/instructions/reverse_engineering.md
@projects/master/person/struts-person

既存のstruts-personプロジェクトから仕様書を生成してください。

パラメータ:
* struts_project_root: projects/master/person/struts-person
* spec_output_directory: projects/master/person/person-jsf-migrated/specs
```

#### ステップ2: タスク分解

```
@agent_skills/struts-to-jsf-migration/instructions/task_breakdown.md

全タスクを分解してください。

パラメータ:
* project_root: projects/master/person/person-jsf-migrated
* spec_directory: projects/master/person/person-jsf-migrated/specs
```

#### ステップ3: 詳細設計

```
@agent_skills/struts-to-jsf-migration/instructions/detailed_design.md

Person一覧画面の詳細設計書を作成してください。

パラメータ:
* project_root: projects/master/person/person-jsf-migrated
* screen_id: SCREEN_001_PersonList
```

#### ステップ4: JSFコード生成

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

セットアップタスクを実行してください。

パラメータ:
* project_root: projects/master/person/person-jsf-migrated
* task_file: projects/master/person/person-jsf-migrated/tasks/setup_tasks.md
* skip_infrastructure: true
```

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

Person一覧画面を実装してください。

パラメータ:
* project_root: projects/master/person/person-jsf-migrated
* task_file: projects/master/person/person-jsf-migrated/tasks/SCREEN_001_PersonList.md
```

---

## 🔑 マイグレーションのポイント

### データベースは変更しない

* 既存のデータベーススキーマをそのまま使用
* JPA Entityで既存テーブルにマッピング
* マイグレーションの範囲をアプリケーション層に限定

### ビジネスロジックを保全

* Strutsのビジネスロジックを正確に抽出
* JSFでも同じビジネスルールを実装
* テストで同等性を検証

### 最新アーキテクチャを採用

* CDIによる依存性注入
* JPAによる永続化
* Bean Validationによる宣言的検証
* Faceletsによるテンプレート化

### 段階的マイグレーション

* 大規模システムは機能単位で段階的に実施
* 各段階でテストと検証を実施
* リスクを最小化

---

## 📖 参考資料

* [SKILL.md](SKILL.md) - 詳細ガイド
* [マイグレーション原則](principles/) - マイグレーションルール、アーキテクチャ標準、セキュリティ標準
  * [architecture.md](principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](principles/security.md) - セキュリティ標準
  * [common_rules.md](principles/common_rules.md) - 共通ルール、マッピング規則
* [Jakarta EE 10仕様](https://jakarta.ee/specifications/platform/10/)
* [Jakarta Faces 4.0仕様](https://jakarta.ee/specifications/faces/4.0/)

---

## 🎓 サポートされるStrutsバージョン

* Apache Struts 1.x（1.3.10等）
* Java EE 8ベースのStrutsアプリケーション
* EJB 3.2使用のアプリケーション
* JDBC + DataSource使用のアプリケーション

---

## ✅ マイグレーション後の技術スタック

* Java 21
* Jakarta EE 10
* Jakarta Faces (JSF) 4.0
* Jakarta Persistence (JPA) 3.1
* Jakarta CDI 4.0
* Payara Server 6（またはWildFly）
* 既存データベース（HSQLDB等）

---

Happy Migration! 🚀
