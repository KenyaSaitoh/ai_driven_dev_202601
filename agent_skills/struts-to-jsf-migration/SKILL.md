---
name: struts-to-jsf-migration
description: Apache Struts 1.xからJakarta Faces (JSF) 4.0へのマイグレーションを支援。仕様駆動アプローチ（Spec-Driven Migration）により、リバースエンジニアリング、仕様書検証、フォワードエンジニアリングの3段階で確実なマイグレーションを実現。
---

# Struts to JSF マイグレーション Agent Skill

## 使い方（4段階プロセス）

### ステップ1: 既存コード分析（Struts → 仕様書）

```
@agent_skills/struts-to-jsf-migration/instructions/reverse_engineering.md
@projects/legacy/struts-app

既存のStrutsプロジェクトから仕様書を生成してください

パラメータ
* struts_project_root: projects/legacy/struts-app
* spec_output_directory: projects/jsf-migration/struts-app-jsf/specs
```

AIが自動で以下を実行する
1. Strutsコード（Action、ActionForm、JSP、EJB、DAO）を分析する
2. 抽象的・論理的な仕様書を生成する
3. `specs/`フォルダに保存する

### ステップ2: タスク分解

```
@agent_skills/struts-to-jsf-migration/instructions/task_breakdown.md

全タスクを分解してください

パラメータ
* project_root: projects/jsf-migration/struts-app-jsf
* spec_directory: projects/jsf-migration/struts-app-jsf/specs
```

AIが自動で以下を実行する
1. 仕様書を読み込む
2. タスクファイルを分解・生成する
3. `tasks/`フォルダに保存する

### ステップ3: 詳細設計

```
@agent_skills/struts-to-jsf-migration/instructions/detailed_design.md

対象: <SCREEN_ID>（例: SCREEN_001_PersonList）

この画面の詳細設計書を作成してください
```

AIと対話しながら以下を実施する
1. 仕様書を読み込み、理解内容を説明する
2. 不明点をユーザーに質問する
3. 対話で妥当性・充足性を確認する
4. `detailed_design.md`を生成する

### ステップ4: コード生成

```
@agent_skills/struts-to-jsf-migration/instructions/code_generation.md

セットアップタスクを実行してください

パラメータ
* project_root: projects/jsf-migration/struts-app-jsf
* task_file: projects/jsf-migration/struts-app-jsf/tasks/setup_tasks.md
```

AIが以下を実施する
1. タスクと詳細設計を読み込む
2. JSFコードを実装する（Managed Bean、Entity、Service等）
3. テストを作成する
4. タスクを完了としてマークする

---

## 実践例

```
@agent_skills/struts-to-jsf-migration/instructions/reverse_engineering.md
@projects/master/person/struts-person

既存のstruts-personプロジェクトから仕様書を生成してください

パラメータ
* struts_project_root: projects/master/person/struts-person
* spec_output_directory: projects/master/person/jsf-person-migrated/specs
```

その後、仕様書検証とコード生成を実施する

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
├── principles/                       # マイグレーション憲章
│   └── constitution.md              # マイグレーション原則、マッピング規則
└── instructions/
    ├── reverse_engineering.md        # ステップ1: 既存コード分析（仕様書生成）
    ├── task_breakdown.md             # ステップ2: タスク分解
    ├── detailed_design.md            # ステップ3: 詳細設計
    └── code_generation.md            # ステップ4: コード生成
```

---

## 参考資料

* [Jakarta EE 10仕様](https://jakarta.ee/specifications/platform/10/)
* [Jakarta Faces 4.0仕様](https://jakarta.ee/specifications/faces/4.0/)
* [Jakarta Persistence 3.1仕様](https://jakarta.ee/specifications/persistence/3.1/)
* [Apache Struts 1.x Documentation](https://struts.apache.org/struts1eol-announcement.html)
