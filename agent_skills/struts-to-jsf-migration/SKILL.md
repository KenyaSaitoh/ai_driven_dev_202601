---
name: struts-to-jsf-migration
description: Apache Struts 1.xからJakarta Faces (JSF) 4.0へのマイグレーションを支援。仕様駆動アプローチ（Spec-Driven Migration）により、リバースエンジニアリング、仕様書検証、フォワードエンジニアリングの3段階で確実なマイグレーションを実現。
---

# Struts to JSF マイグレーション Agent Skill

## 🎯 これは何？

Apache Struts 1.xからJakarta Faces (JSF) 4.0へのマイグレーションを支援する汎用Agent Skillです。

Agent Skillsとは：
* AIコーディングエージェント用の標準化されたインストラクション形式
* Cursor、Claude等のAIツールで使える
* 既存コード分析 → 仕様書検証 → 仕様駆動開発の3段階で確実なマイグレーション

マイグレーション哲学：
* Code-to-Codeではなく、Code-to-Spec-to-Code
* レガシーコードから抽象的・論理的な仕様書を生成
* 仕様書をベースに最新アーキテクチャでJSFコードを生成

このAgent Skillsに含まれるもの:
* instructions/: 3段階のマイグレーションインストラクション
* principles/: マイグレーション憲章（マッピング規則、原則）

---

## 🚀 使い方（3段階プロセス）

### ステップ1: 🔍 既存コード分析（Struts → 仕様書）

```
@agent_skills/struts-to-jsf-migration/instructions/reverse_engineering.md
@projects/legacy/struts-app

既存のStrutsプロジェクトから仕様書を生成してください。

パラメータ:
* struts_project_root: projects/legacy/struts-app
* spec_output_directory: projects/jsf-migration/struts-app-jsf/specs
```

これだけ！ AIが自動で：
1. 📖 Strutsコード（Action、ActionForm、JSP、EJB、DAO）を分析
2. 🔧 抽象的・論理的な仕様書を生成
3. 💾 `specs/`フォルダに保存

生成される仕様書：
* requirements.md: システムの目的、機能要件
* architecture_design.md: 技術スタック、レイヤー構成
* functional_design.md: 画面一覧、画面遷移、コンポーネント設計
* data_model.md: エンティティ、テーブル定義、リレーション
* screen_design.md: 画面レイアウト、入力項目、ボタンアクション
* behaviors.md: 画面の振る舞い、バリデーション、エラーハンドリング

### ステップ2: ✅ 仕様書の検証と調整

```
@agent_skills/struts-to-jsf-migration/instructions/spec_validation.md
@projects/jsf-migration/struts-app-jsf/specs

生成された仕様書を検証してください。

パラメータ:
* spec_directory: projects/jsf-migration/struts-app-jsf/specs
```

AIと対話しながら：
1. 📖 生成された仕様書をレビュー
2. ❓ 不明点や不整合をユーザーに報告
3. 💬 JSFアーキテクチャへの適応を提案
4. 📝 仕様書を調整・改善

### ステップ3: ⚙️ 仕様駆動開発（仕様書 → JSF）

```
@agent_skills/jakarta-ee-standard/instructions/task_breakdown.md

全タスクを分解してください。

パラメータ:
* project_root: projects/jsf-migration/struts-app-jsf
* spec_directory: projects/jsf-migration/struts-app-jsf/specs
```

その後、jakarta-ee-standardスキルの詳細設計とコード生成を使用：

```
@agent_skills/jakarta-ee-standard/instructions/detailed_design.md
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
```

AIが：
1. 📄 仕様書からタスクを分解
2. 💻 JSFコードを実装（Managed Bean、Entity、Service等）
3. ✅ テストを作成
4. ☑️ タスクを完了としてマーク

---

## 💡 実践例

### 例: Struts人材管理システムのJSFマイグレーション

ステップ1: 既存コード分析

```
@agent_skills/struts-to-jsf-migration/instructions/reverse_engineering.md
@projects/master/person/struts-person

既存のstruts-personプロジェクトから仕様書を生成してください。

パラメータ:
* struts_project_root: projects/master/person/struts-person
* spec_output_directory: projects/master/person/jsf-person-migrated/specs
```

結果:
```
specs/
├── requirements.md              ← 人材管理システムの要件
├── architecture_design.md       ← JSFアーキテクチャ設計
├── functional_design.md         ← 画面一覧、画面遷移
├── data_model.md                ← PERSONエンティティ
├── screen_design.md             ← 画面レイアウト
└── behaviors.md                 ← バリデーション、エラー処理
```

ステップ2: 仕様書の検証

```
@agent_skills/struts-to-jsf-migration/instructions/spec_validation.md
@projects/master/person/jsf-person-migrated/specs

生成された仕様書を検証してください。
```

AIとの対話例:
```
AI: 生成した仕様書を確認しました。以下の点を確認させてください：
    
    1. データモデル: PERSONエンティティ（personId, personName, age, gender）
    2. 画面: 一覧、入力、確認、更新、削除
    3. ビジネスロジック: PersonService（CRUD操作）
    
    JSFアーキテクチャへの適応提案:
    * Struts ActionForm → PersonInputBean（@ViewScoped）
    * Struts Action → PersonTableBean のアクションメソッド
    * EJB（JNDIルックアップ） → PersonService（@Inject）
    * DAO（JDBC） → JPA（EntityManager）
    
    この理解でよろしいでしょうか？

ユーザー: はい、その通りです。

AI: 仕様書を確定しました。次のステップ（タスク分解）に進めます。
```

ステップ3: JSFコード生成

```
@agent_skills/jakarta-ee-standard/instructions/task_breakdown.md

全タスクを分解してください。

パラメータ:
* project_root: projects/master/person/jsf-person-migrated
* spec_directory: projects/master/person/jsf-person-migrated/specs
```

その後:
```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md

セットアップタスクを実行してください。

パラメータ:
* project_root: projects/master/person/jsf-person-migrated
* task_file: projects/master/person/jsf-person-migrated/tasks/setup_tasks.md
```

---

## 🎯 マイグレーション対象

### Strutsの構成要素

既存コード分析で対象となるStrutsの構成要素：

* ActionForm: リクエストパラメータの保持
* Action: ビジネスロジックの呼び出し
* struts-config.xml: マッピング設定
* JSPタグライブラリ: `<logic:iterate>`, `<bean:write>`, `<html:form>`等
* EJB: ステートレスセッションBean（JNDIルックアップ）
* DAO: JDBC + DataSource

### JSFの構成要素

仕様駆動開発で生成されるJSFの構成要素：

* Managed Bean: `@Named`, `@ViewScoped`
* CDI: `@Inject`で依存性注入
* JPA: EntityManager、JPQL
* トランザクション: `@Transactional`
* Facelets XHTML: `<h:dataTable>`, `<h:outputText>`, `<h:form>`等

---

## 📁 ディレクトリ構造

```
agent_skills/struts-to-jsf-migration/
├── SKILL.md                          # このファイル
├── README.md                         # クイックスタートガイド
├── principles/                       # マイグレーション憲章
│   └── constitution.md              # マイグレーション原則、マッピング規則
└── instructions/
    ├── reverse_engineering.md        # ステップ1: 既存コード分析（仕様書生成）
    ├── spec_validation.md            # ステップ2: 仕様書検証・調整
    └── spec_driven_development.md    # ステップ3: 仕様駆動開発（参照用）
```

---

## 🔑 重要なマッピング規則

### 1. Struts ActionForm → JSF Managed Bean

Struts:
```java
public class PersonForm extends ActionForm {
    private String personId;
    private String personName;
    // ...
}
```

JSF:
```java
@ViewScoped
@Named("personInput")
public class PersonInputBean implements Serializable {
    private Integer personId;
    private String personName;
    // ...
}
```

### 2. Struts Action → JSF アクションメソッド

Struts:
```java
public class PersonListAction extends Action {
    public ActionForward execute(...) {
        PersonService service = (PersonService) ctx.lookup("...");
        List<Person> list = service.getAllPersons();
        request.setAttribute("personList", list);
        return mapping.findForward("success");
    }
}
```

JSF:
```java
@ViewScoped
@Named("personTable")
public class PersonTableBean implements Serializable {
    @Inject
    private PersonService personService;
    
    private List<Person> personList;
    
    @PostConstruct
    public void init() {
        personList = personService.getPersonList();
    }
}
```

### 3. Struts JSPタグ → JSF Faceletsタグ

Struts:
```jsp
<logic:iterate id="person" name="personList">
    <bean:write name="person" property="personName"/>
</logic:iterate>
```

JSF:
```xhtml
<h:dataTable value="#{personTable.personList}" var="person">
    <h:column>
        <h:outputText value="#{person.personName}"/>
    </h:column>
</h:dataTable>
```

### 4. DAO（JDBC） → JPA

Struts:
```java
public class PersonDao {
    public List<Person> findAll() {
        DataSource ds = (DataSource) ctx.lookup("jdbc/HsqldbDS");
        Connection conn = ds.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM PERSON");
        // ...
    }
}
```

JSF:
```java
@RequestScoped
@Transactional
public class PersonService {
    @PersistenceContext
    private EntityManager em;
    
    public List<Person> getPersonList() {
        return em.createQuery("SELECT p FROM Person p", Person.class)
                 .getResultList();
    }
}
```

---

## 📚 参考資料

* [Jakarta EE 10仕様](https://jakarta.ee/specifications/platform/10/)
* [Jakarta Faces 4.0仕様](https://jakarta.ee/specifications/faces/4.0/)
* [Jakarta Persistence 3.1仕様](https://jakarta.ee/specifications/persistence/3.1/)
* [Apache Struts 1.x Documentation](https://struts.apache.org/struts1eol-announcement.html)

---

## 🎓 マイグレーションのベストプラクティス

### 段階的マイグレーション

大規模システムの場合、機能単位で段階的にマイグレーション：

1. 最もシンプルな機能から開始（例: 一覧表示）
2. CRUD操作を含む機能（例: 登録・更新・削除）
3. 複雑なビジネスロジックを含む機能

### テストの重要性

マイグレーション後、元のシステムと同等の機能を持つことをテストで検証：

* 画面遷移テスト
* ビジネスロジックテスト
* データベースアクセステスト

### アーキテクチャの刷新

レガシーな設計パターンをそのまま移植せず、最新のベストプラクティスを採用：

* CDIによる依存性注入
* JPAによる永続化
* トランザクション管理の一元化
* Faceletsによるテンプレート化
