# Struts to JSF マイグレーション - クイックスタートガイド

Apache Struts 1.xからJakarta Faces (JSF) 4.0へのマイグレーションを6ステップで実現します。

---

## 🎯 マイグレーションアプローチ

```
Struts コード → SPEC生成 → タスク分解 → 詳細設計 → JSF コード生成 → テスト実行評価 → E2Eテスト
 (既存分析)    (画面単位)   (AIと対話)   (実装+単体)    (品質検証)      (Playwright)
```

Code-to-Codeの直接変換ではなく、一度SPECとして抽象化することで：
* レガシーな設計パターンを持ち込まない
* 最新のJakarta EE 10ベストプラクティスを採用
* ビジネスロジックを正確に保全

---

## 🚀 6ステップでマイグレーション

### ステップ1: 🔍 既存コード分析

既存のStrutsコードからSPECを生成します。

```
@agent_skills/struts-to-jsf-migration/instructions/reverse_engineering.md
@projects/legacy/struts-app

既存のStrutsプロジェクトからSPECを生成してください。

パラメータ:
* struts_project_root: projects/legacy/struts-app
* spec_output_directory: projects/jsf-migration/struts-app-jsf/specs
```

生成されるSPEC:
* `requirements.md` - システムの目的、機能要件
* `architecture_design.md` - 技術スタック、レイヤー構成
* `functional_design.md` - 画面一覧、画面遷移
* `data_model.md` - エンティティ、テーブル定義
* `screen_design.md` - 画面レイアウト、入力項目
* `behaviors.md` - 画面の振る舞い、バリデーション

### ステップ2: 📋 タスク分解

生成されたSPECから実装タスクを分解します。

```
@agent_skills/struts-to-jsf-migration/instructions/task_breakdown.md

全タスクを分解してください。

パラメータ:
* project_root: projects/jsf-migration/struts-app-jsf
* spec_directory: projects/jsf-migration/struts-app-jsf/specs
```

タスクファイルを生成：
* `setup.md` - セットアップ（特別なタスク、常に最初）
* `FUNC_001_xxx.md` - 機能別タスク（内容はプロジェクト固有、例: Entity、Service等）
* `FUNC_002_yyy.md` - 機能別タスク（内容はプロジェクト固有、例: 画面機能）

### ステップ3: 📝 詳細設計（画面単位、AIと対話）

画面単位で詳細設計書を作成します。

```
@agent_skills/struts-to-jsf-migration/instructions/detailed_design.md

画面の詳細設計書を作成してください。

パラメータ:
* project_root: projects/jsf-migration/struts-app-jsf
* target_type: FUNC_001_PersonList
```

AIと対話しながら：
* Managed Bean設計を確認
* バリデーションルールを確認
* 画面遷移とデータ受け渡しを確認
* 詳細設計書を生成

### ステップ4: ⚙️ JSFコード生成（詳細設計→実装→単体テスト）

詳細設計書に基づいてJSFコードと単体テストを生成します。

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

セットアップタスクを実行してください。

パラメータ:
* project_root: projects/jsf-migration/struts-app-jsf
* task_file: projects/jsf-migration/struts-app-jsf/tasks/setup.md
* skip_infrastructure: true
```

AIが：
1. 💻 Managed Bean、Service、Dao、Entity等を生成
2. 🎨 Facelets XHTML（画面）を生成
3. ✅ タスク粒度内の単体テストを生成
   * 同じタスク内のコンポーネント間は実際の連携をテスト
   * 例: PersonListBean → PersonService → PersonDao は実際の連携、EntityManagerはモック

### ステップ5: 🔍 単体テスト実行評価

```
@agent_skills/struts-to-jsf-migration/instructions/unit_test_execution.md

単体テストを実行してください。

パラメータ:
* project_root: projects/jsf-migration/struts-app-jsf
* target_type: FUNC_001_PersonList
```

AIが：
1. 🧪 テスト実行（gradle test jacocoTestReport）
2. 📊 テスト結果とカバレッジを分析
3. 🔍 問題を分類:
   * テスト失敗（アサーション、例外、タイムアウト）
   * 必要な振る舞い（テストが不足）
   * デッドコード（到達不可能・冗長）
   * 設計の誤り（仕様との不一致）
4. 📋 フィードバックレポートを生成
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

### ステップ6: 🔗 結合テスト生成

```
@agent_skills/struts-to-jsf-migration/instructions/it_generation.md

結合テストを生成してください。

パラメータ:
* project_root: projects/jsf-migration/struts-app-jsf
* spec_directory: projects/jsf-migration/struts-app-jsf/specs/baseline
```

AIが：
1. 📄 basic_design/behaviors.md（結合テストシナリオ）を読み込む
2. 🧪 JUnit 5 + Weld SE を使用した結合テストを生成する
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

### ステップ7: 🧪 E2Eテスト生成

```
@agent_skills/struts-to-jsf-migration/instructions/e2e_test_generation.md

E2Eテストを生成してください。

パラメータ:
* project_root: projects/jsf-migration/struts-app-jsf
* spec_directory: projects/jsf-migration/struts-app-jsf/specs/baseline
```

AIが：
1. 📄 requirements/behaviors.md（E2Eテストシナリオ）を読み込む
2. 🧪 Playwright を使用したE2Eテストを生成する
   * 複数画面にまたがるフローをテスト
   * 実際のブラウザ操作
   * 実際のDBアクセスを含む
   * エンドツーエンドのフロー検証
3. 📋 テストデータのセットアップ/クリーンアップコードを生成
4. 🏷️ `@Tag("e2e")` でE2Eテストを分離

重要：
* E2Eテストは実装完了後に実行
* アプリケーションサーバーが起動している状態で実行
* `./gradlew e2eTest` で実行（通常の `test` タスクからは除外）

---

## 🔄 基本設計変更対応（手戻り・拡張案件）

### いつ使う？

* 結合テストやE2Eテストで不具合が見つかり、基本設計に戻る必要がある場合（baseline手戻り）
* 拡張案件（enhancements）で新機能を追加し、基本設計を更新する場合
* マイグレーション過程で設計の不整合が判明し、基本設計の変更が必要な場合

### 実行方法

```
@agent_skills/struts-to-jsf-migration/instructions/basic_design_change.md

基本設計の変更を検出して、影響を受けるファイルを更新してください。

パラメータ:
* project_root: projects/jsf-migration/struts-app-jsf
* spec_directory: projects/jsf-migration/struts-app-jsf/specs/baseline
```

AIが：
1. 📄 CHANGES.md（変更差分ファイル）を読み込み
2. 🔍 変更の影響を受けるファイル（詳細設計、コード、XHTML、テスト）を特定
3. 📋 変更タスクファイル（`tasks/change_tasks.md`）を生成
4. 🎯 既存の指示書を呼び出して、影響を受けるファイルを更新
5. ✅ すべての変更適用後、CHANGES.mdをアーカイブ

### 変更差分管理

```
specs/baseline/basic_design/
  ├── functional_design.md (or .xlsx)
  ├── screen_design.md (or .xlsx)
  ├── CHANGES.md              # アクティブな変更
  └── changes_archive/        # 適用済み変更
      ├── 20260118_person_edit.md
      └── 20260125_validation_update.md
```

重要:
* マスターファイル（functional_design.md等）は自由に更新
* 変更内容はCHANGES.mdに明示的に記載
* Markdown、EXCEL、PDF等、形式非依存

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

#### ステップ1: SPEC生成

```
@agent_skills/struts-to-jsf-migration/instructions/reverse_engineering.md
@projects/master/person/struts-person

既存のstruts-personプロジェクトからSPECを生成してください。

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
* target_type: FUNC_001_PersonList
```

#### ステップ4: JSFコード生成

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

セットアップタスクを実行してください。

パラメータ:
* project_root: projects/master/person/person-jsf-migrated
* task_file: projects/master/person/person-jsf-migrated/tasks/setup.md
* skip_infrastructure: true
```

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

Person一覧画面を実装してください。

パラメータ:
* project_root: projects/master/person/person-jsf-migrated
* task_file: projects/master/person/person-jsf-migrated/tasks/FUNC_001_PersonList.md
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
