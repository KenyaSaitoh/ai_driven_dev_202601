---
name: struts-to-jsf-migration
description: Apache Struts 1.xからJakarta Faces (JSF) 4.0へのマイグレーションを支援。仕様駆動アプローチ（Spec-Driven Migration）により、リバースエンジニアリング、タスク分解、詳細設計、コード生成、結合テスト、E2Eテストの7段階で確実なマイグレーションを実現。基本設計変更対応も含む。
---

# Struts to JSF マイグレーション Agent Skill

## 使い方（7段階プロセス）

### ステップ1: リバースエンジニアリング

```
@agent_skills/struts-to-jsf-migration/instructions/reverse_engineering.md
@projects/legacy/struts-app

既存のStrutsプロジェクトからSPECを生成してください

パラメータ
* struts_project_root: projects/legacy/struts-app
* spec_output_directory: projects/jsf-migration/struts-app-jsf/specs
```

AIが自動で以下を実行
1. Strutsコード（Action、ActionForm、JSP、EJB、DAO）を分析
2. 抽象的・論理的なSPECを生成
3. `specs/`フォルダに保存

### ステップ2: タスク分解

```
@agent_skills/struts-to-jsf-migration/instructions/task_breakdown.md

全タスクを分解してください

パラメータ
* project_root: projects/jsf-migration/struts-app-jsf
* spec_directory: projects/jsf-migration/struts-app-jsf/specs/baseline
```

AIが自動で以下を実行
1. SPECを読み込み
2. タスクファイルを分解・生成
3. `tasks/`フォルダに保存

### ステップ3: 詳細設計

```
@agent_skills/struts-to-jsf-migration/instructions/detailed_design.md

画面の詳細設計書を作成してください

パラメータ
* project_root: projects/jsf-migration/struts-app-jsf
* spec_directory: projects/jsf-migration/struts-app-jsf/specs/baseline
* target_type: FUNC_001_PersonList
```

AIと対話しながら以下を実施（対話的プロセス）
1. SPECを読み込み、理解内容を説明
2. 不明点をユーザーに質問
3. 対話で妥当性・充足性を確認
4. `detailed_design.md`を生成

### ステップ4: コード生成（詳細設計→実装→単体テスト）

```
@agent_skills/struts-to-jsf-migration/instructions/code_generation.md

セットアップタスクを実行してください

パラメータ
* project_root: projects/jsf-migration/struts-app-jsf
* task_file: projects/jsf-migration/struts-app-jsf/tasks/setup.md
```

AIが自動で以下を実行
1. タスクと詳細設計を読み込み
2. JSFコードを生成（Managed Bean、Entity、Service、Facelets XHTML等）
3. タスク粒度内の単体テストを作成
   * タスク内のコンポーネント間は実際の連携をテスト
   * タスク外の依存関係のみモック化
4. タスクを完了としてマーク

### ステップ5: 単体テスト実行評価

```
@agent_skills/struts-to-jsf-migration/instructions/unit_test_execution.md

単体テストを実行してください

パラメータ
* project_root: projects/jsf-migration/struts-app-jsf
* target_type: FUNC_001_PersonList
```

AIが自動で以下を実行
1. テスト実行（gradle test jacocoTestReport）
2. テスト結果とカバレッジ分析
3. 問題の分類（テスト失敗、必要な振る舞い、デッドコード、設計の誤り）
4. フィードバックレポート生成
5. ユーザーに推奨アクションを提示

重要:
* 問題を発見してもユーザー確認なしに修正しない
* Managed Bean はカバレッジ除外推奨（UI層はE2Eで検証）
* 必要に応じてステップ3（詳細設計）に戻ってループ

フィードバックループ:
```
詳細設計 → コード生成 → テスト実行評価
    ↑                         ↓
    └──── フィードバック ←────┘
```

### ステップ6: 結合テスト生成

```
@agent_skills/struts-to-jsf-migration/instructions/it_generation.md

結合テストを生成してください

パラメータ
* project_root: projects/jsf-migration/struts-app-jsf
* spec_directory: projects/jsf-migration/struts-app-jsf/specs/baseline
```

AIが自動で以下を実行
1. basic_design/behaviors.md（結合テストシナリオ）を読み込み
2. JUnit 5 + Weld SE を使用した結合テストを生成
   * Service層以下（Service + DAO + Entity + DB）の連携テスト
   * 実際のDBアクセス（メモリDB）

### ステップ7: E2Eテスト生成

```
@agent_skills/struts-to-jsf-migration/instructions/e2e_test_generation.md

E2Eテストを生成してください

パラメータ
* project_root: projects/jsf-migration/struts-app-jsf
* spec_directory: projects/jsf-migration/struts-app-jsf/specs/baseline
```

AIが自動で以下を実行
1. requirements/behaviors.md（E2Eテストシナリオ）を読み込み
2. Playwright を使用したE2Eテストを生成
   * 複数画面にまたがるフローをテスト
   * 実際のブラウザ操作
3. テストデータのセットアップ/クリーンアップコードを生成

---

## 🔄 基本設計変更対応（手戻り・拡張案件）

```
@agent_skills/struts-to-jsf-migration/instructions/basic_design_change.md

基本設計の変更を適用してください

パラメータ
* project_root: projects/jsf-migration/struts-app-jsf
* spec_directory: projects/jsf-migration/struts-app-jsf/specs/baseline
* change_spec: <変更差分ファイルパス>（省略可、デフォルト: {spec_directory}/basic_design/CHANGES.md）
```

AIが自動で以下を実行
1. CHANGES.md（変更差分ファイル）を読み込み
2. 変更の影響を受けるファイル（詳細設計、コード、XHTML、テスト）を特定
3. 変更タスクファイル（`tasks/change_tasks.md`）を生成
4. 既存の指示書を呼び出して更新
5. CHANGES.mdをアーカイブ

使用方法:
1. 基本設計SPECのマスターファイル（functional_design.md、screen_design.md等）を自由に編集
2. CHANGES.mdを作成して変更内容を明示的に記載
3. 上記コマンドを実行
4. 適用後、CHANGES.mdは自動的にchanges_archive/に移動

重要:
* マスターファイルはMarkdown、EXCEL、PDF、Word等、任意の形式で管理可能
* 変更内容はCHANGES.mdに明示的に記載（形式非依存）

---

## 実践例

```
@agent_skills/struts-to-jsf-migration/instructions/reverse_engineering.md
@projects/master/person/struts-person

既存のstruts-personプロジェクトからSPECを生成してください

パラメータ
* struts_project_root: projects/master/person/struts-person
* spec_output_directory: projects/master/person/jsf-person-migrated/specs
```

その後、SPEC検証とコード生成を実施する

---

## マイグレーション対象

### Strutsの構成要素

既存コード分析で対象となるStrutsの構成要素

* ActionForm: リクエストパラメータの保持
* Action: ビジネスロジックの呼び出し
* struts-config.xml: マッピング設定
* JSPタグライブラリ: `<logic:iterate>`, `<bean:write>`, `<html:form>`等
* EJB: ステートレスセッションBean（JNDIルックアップ）
* DAO: JDBC + DataSource

### JSFの構成要素

仕様駆動開発で生成されるJSFの構成要素

* Managed Bean: `@Named`, `@ViewScoped`
* CDI: `@Inject`で依存性注入
* JPA: EntityManager、JPQL
* トランザクション: `@Transactional`
* Facelets XHTML: `<h:dataTable>`, `<h:outputText>`, `<h:form>`等

---

## ディレクトリ構造

```
agent_skills/struts-to-jsf-migration/
├── SKILL.md                          # このファイル
├── README.md                         # クイックスタートガイド
├── principles/                       # マイグレーション原則
│   ├── architecture.md              # Jakarta EE APIアーキテクチャ標準
│   ├── security.md                  # セキュリティ標準
│   └── common_rules.md              # マイグレーションルール、マッピング規則
└── instructions/
    ├── reverse_engineering.md        # ステップ1: リバースエンジニアリング
    ├── task_breakdown.md             # ステップ2: タスク分解
    ├── detailed_design.md            # ステップ3: 詳細設計
    ├── code_generation.md            # ステップ4: コード生成（実装+単体テスト）
    ├── unit_test_execution.md        # ステップ5: 単体テスト実行評価
    ├── it_generation.md              # ステップ6: 結合テスト生成（JUnit + Weld SE）
    ├── e2e_test_generation.md        # ステップ7: E2Eテスト生成（Playwright）
    └── basic_design_change.md        # 基本設計変更対応（手戻り・拡張案件）
```

---

## 参考資料

* [マイグレーション原則](principles/) - マイグレーションルール、アーキテクチャ標準、セキュリティ標準
  * [architecture.md](principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](principles/security.md) - セキュリティ標準
  * [common_rules.md](principles/common_rules.md) - 共通ルール、マッピング規則
* [Jakarta EE 10仕様](https://jakarta.ee/specifications/platform/10/)
* [Jakarta Faces 4.0仕様](https://jakarta.ee/specifications/faces/4.0/)
* [Jakarta Persistence 3.1仕様](https://jakarta.ee/specifications/persistence/3.1/)
* [Apache Struts 1.x Documentation](https://struts.apache.org/struts1eol-announcement.html)
