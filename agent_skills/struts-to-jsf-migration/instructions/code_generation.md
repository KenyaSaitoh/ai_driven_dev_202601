# コード生成インストラクション

## 概要

このインストラクションは、仕様書からJSFコードを生成する方法を説明する

重要: 実際のコード生成には、`@agent_skills/jakarta-ee-api-basic/instructions/code_generation.md` を使用する

---

## コード生成の流れ

マイグレーションの全体フローは以下の通りである

```
ステップ1: リバースエンジニアリング（reverse_engineering.md）- Strutsコードから仕様書を生成
    ↓
ステップ2: タスク分解（task_breakdown.md）- 画面単位でタスクリストを作成
    ↓
ステップ3: 詳細設計（detailed_design.md）- AIと対話しながら画面単位で詳細設計 ← 対話的
    ↓
ステップ4: コード生成（code_generation.md）- タスクに従ってJSFコードを生成 ← このステップ
```

### ステップ1: リバースエンジニアリング

既存のStrutsプロジェクトから仕様書を生成する

```
@agent_skills/struts-to-jsf-migration/instructions/reverse_engineering.md

既存のStrutsプロジェクトから仕様書を生成してください

パラメータ
* struts_project_root: <既存Strutsプロジェクトのルート>
* spec_output_directory: <仕様書の出力先>
```

例
```
@agent_skills/struts-to-jsf-migration/instructions/reverse_engineering.md

既存のStrutsプロジェクトから仕様書を生成してください

パラメータ
* struts_project_root: projects/master/person/struts-person
* spec_output_directory: projects/sdd/person/jsf-person-sdd/specs
```

結果
```
specs/
├── baseline/
│   ├── system/
│   │   ├── architecture_design.md
│   │   ├── data_model.md
│   │   └── functional_design.md
│   └── screen/
│       ├── SCREEN_001_PersonList/
│       ├── SCREEN_002_PersonInput/
│       └── ...
```

### ステップ2: タスク分解

仕様書から、実装タスクを分解する

```
@agent_skills/struts-to-jsf-migration/instructions/task_breakdown.md

全タスクを分解してください

パラメータ
* project_root: <JSFプロジェクトのルート>
* spec_directory: <仕様書ディレクトリ>
* output_directory: <タスク出力先>（オプション）
```

例
```
@agent_skills/struts-to-jsf-migration/instructions/task_breakdown.md

全タスクを分解してください

パラメータ
* project_root: projects/sdd/person/jsf-person-sdd
* spec_directory: projects/sdd/person/jsf-person-sdd/specs
```

結果
```
tasks/
├── tasks.md              ← メインタスクリスト
├── setup_tasks.md        ← セットアップ
├── common_tasks.md       ← 共通機能（Entity、Service等）
├── SCREEN_001_PersonList.md   ← 画面別タスク
├── SCREEN_002_PersonInput.md  
└── integration_tasks.md  ← 結合テスト
```

### ステップ3: 詳細設計（画面単位、AIと対話）

画面単位で詳細設計書を作成する。AIと対話しながら不明点を確認する

```
@agent_skills/struts-to-jsf-migration/instructions/detailed_design.md

画面の詳細設計書を作成してください

パラメータ
* project_root: <JSFプロジェクトのルート>
* screen_id: <対象画面ID>
```

例
```
@agent_skills/struts-to-jsf-migration/instructions/detailed_design.md

Person一覧画面の詳細設計書を作成してください

パラメータ
* project_root: projects/sdd/person/jsf-person-sdd
* screen_id: SCREEN_001_PersonList
```

結果
* `specs/baseline/screen/SCREEN_001_PersonList/detailed_design.md` が生成される

### ステップ4: コード生成

タスクファイルに従ってJSFコードを生成する

```
@agent_skills/jakarta-ee-api-basic/instructions/code_generation.md

タスクを実行してください

パラメータ
* project_root: <JSFプロジェクトのルート>
* task_file: <実行するタスクファイル>
* skip_infrastructure: true  # インフラセットアップをスキップ
```

例（セットアップ）
```
@agent_skills/jakarta-ee-api-basic/instructions/code_generation.md

セットアップタスクを実行してください

パラメータ
* project_root: projects/sdd/person/jsf-person-sdd
* task_file: projects/sdd/person/jsf-person-sdd/tasks/setup_tasks.md
* skip_infrastructure: true
```

例（共通機能）
```
@agent_skills/jakarta-ee-api-basic/instructions/code_generation.md

共通機能を実装してください

パラメータ
* project_root: projects/sdd/person/jsf-person-sdd
* task_file: projects/sdd/person/jsf-person-sdd/tasks/common_tasks.md
```

例（画面別実装）
```
@agent_skills/jakarta-ee-api-basic/instructions/code_generation.md

Person一覧画面を実装してください

パラメータ
* project_root: projects/sdd/person/jsf-person-sdd
* task_file: projects/sdd/person/jsf-person-sdd/tasks/SCREEN_001_PersonList.md
```

---

## 生成されるコンポーネント

### 1. Entityクラス（JPA）

* Strutsの対応:
  * Model/Entityクラス（POJO）
  * DAOのSQLマッピング

* JSFでの生成:
  * `@Entity`, `@Table`, `@Id`, `@Column`アノテーション
  * リレーション（`@ManyToOne`, `@OneToMany`等）
  * Bean Validation（`@NotNull`, `@Size`等）

例:
```java
@Entity
@Table(name = "PERSON")
public class Person {
    @Id
    @Column(name = "PERSON_ID")
    private Integer personId;
    
    @NotNull
    @Size(max = 100)
    @Column(name = "PERSON_NAME")
    private String personName;
    
    @Min(0)
    @Max(150)
    @Column(name = "AGE")
    private Integer age;
    
    @Column(name = "GENDER")
    private String gender;
    
    // コンストラクタ、ゲッター、セッター
}
```

### 2. Serviceクラス（CDI + JPA）

* Strutsの対応:
  * EJB（`@Stateless`、JNDIルックアップ）
  * DAO（JDBC + DataSource）

* JSFでの生成:
  * `@RequestScoped` + `@Transactional`
  * `@PersistenceContext`でEntityManager注入
  * JPQLでクエリ実行

例:
```java
@RequestScoped
@Transactional(TxType.REQUIRED)
public class PersonService {
    @PersistenceContext(unitName = "MyPersistenceUnit")
    private EntityManager em;
    
    public List<Person> getPersonList() {
        return em.createQuery("SELECT p FROM Person p", Person.class)
                 .getResultList();
    }
    
    public Person getPerson(Integer personId) {
        return em.find(Person.class, personId);
    }
    
    public void addPerson(Person person) {
        em.persist(person);
    }
    
    public void updatePerson(Person person) {
        em.merge(person);
    }
    
    public void removePerson(Integer personId) {
        Person person = em.find(Person.class, personId);
        em.remove(person);
    }
}
```

### 3. Managed Bean（JSF）

* Strutsの対応:
  * ActionForm（フォームデータの保持）
  * Action（コントローラー）

* JSFでの生成:
  * `@Named` + `@ViewScoped`
  * `@Inject`でServiceを注入
  * プロパティ（ActionFormのフィールドに対応）
  * アクションメソッド（Actionのexecute()に対応）

例:
```java
@ViewScoped
@Named("personTable")
public class PersonTableBean implements Serializable {
    private List<Person> personList;
    
    @Inject
    private PersonService personService;
    
    private Flash flash;
    
    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        flash = facesContext.getExternalContext().getFlash();
        personList = personService.getPersonList();
    }
    
    public String removePerson(Integer personId) {
        personService.removePerson(personId);
        return "PersonTablePage";
    }
    
    public String editPerson(Integer personId) {
        Person person = personService.getPerson(personId);
        flash.put("person", person);
        return "PersonInputPage";
    }
    
    // ゲッター、セッター
}
```

### 4. Facelets XHTML（JSF View）

* Strutsの対応:
  * JSP
  * Strutsタグ（`<logic:iterate>`, `<bean:write>`, `<html:form>`等）

* JSFでの生成:
  * XHTML（Facelets）
  * JSFタグ（`<h:dataTable>`, `<h:outputText>`, `<h:form>`等）
  * Unified EL（`#{...}`）

例:
```xhtml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="jakarta.faces.html"
      xmlns:f="jakarta.faces.core">
<h:head>
    <title>Person一覧</title>
    <h:outputStylesheet library="css" name="style.css"/>
</h:head>
<h:body>
    <h1>Person一覧</h1>
    
    <h:dataTable value="#{personTable.personList}" var="person"
                 styleClass="table">
        <h:column>
            <f:facet name="header">ID</f:facet>
            <h:outputText value="#{person.personId}"/>
        </h:column>
        <h:column>
            <f:facet name="header">名前</f:facet>
            <h:outputText value="#{person.personName}"/>
        </h:column>
        <h:column>
            <f:facet name="header">年齢</f:facet>
            <h:outputText value="#{person.age}"/>
        </h:column>
        <h:column>
            <f:facet name="header">性別</f:facet>
            <h:outputText value="#{person.gender}"/>
        </h:column>
        <h:column>
            <f:facet name="header">操作</f:facet>
            <h:commandButton value="編集" 
                           action="#{personTable.editPerson(person.personId)}"/>
            <h:commandButton value="削除" 
                           action="#{personTable.removePerson(person.personId)}"/>
        </h:column>
    </h:dataTable>
    
    <h:button value="新規登録" outcome="PersonInputPage"/>
</h:body>
</html>
```

### 5. 設定ファイル

* Strutsの対応:
  * `struts-config.xml` - アクションマッピング
  * `web.xml` - Struts設定
  * `ejb-jar.xml` - EJB設定

* JSFでの生成:
  * `web.xml` - JSF設定
  * `beans.xml` - CDI設定
  * `persistence.xml` - JPA設定
  * `faces-config.xml` - ナビゲーションルール（オプション）

例（persistence.xml）:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="3.0"
             xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence 
                                 https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd">
    <persistence-unit name="MyPersistenceUnit" transaction-type="JTA">
        <jta-data-source>java:app/jdbc/testdb</jta-data-source>
        <properties>
            <property name="jakarta.persistence.schema-generation.database.action" value="none"/>
        </properties>
    </persistence-unit>
</persistence>
```

* JNDI名の設定
  * `<jta-data-source>` には、移行元で実際に使用されているJNDI名を設定すること
  * JNDI名は architecture_design.md または data_model.md の該当セクションから取得すること
  * 決め打ちや推測でJNDI名を設定してはならない
  * 例: 
    * 移行元が `java:comp/env/jdbc/HsqldbDS` を使用 → そのまま使用
    * 移行元が `jdbc/HsqldbDS` を使用 → アプリケーションサーバーの名前空間に応じて完全修飾名に変換（例: `java:app/jdbc/HsqldbDS`）
  * Persistence Unit名も仕様書に記載されている場合はそれに従うこと

---

## マイグレーションのポイント

### 1. データソース設定の継続

* Strutsで使用していたJNDIデータソースをそのまま使用する
* `persistence.xml`の`<jta-data-source>`で参照する

### 2. トランザクション管理の移行

* Strutsの場合
  * EJBコンテナがトランザクション管理
  * メソッドがトランザクション境界

* JSFの場合
  * `@Transactional`でトランザクション管理
  * Serviceクラスのメソッドがトランザクション境界

### 3. 画面遷移の移行

* Strutsの場合
  * `struts-config.xml`でマッピング
  * `ActionForward`で遷移先を指定

* JSFの場合
  * アクションメソッドの戻り値（画面ID）で遷移
  * `faces-config.xml`でナビゲーションルール定義（オプション）
  * 暗黙的ナビゲーション（画面ID = XHTMLファイル名）

### 4. データ受け渡しの移行

* Strutsの場合
  * `request.setAttribute()` - リクエストスコープ
  * `session.setAttribute()` - セッションスコープ

* JSFの場合
  * Managed Beanのプロパティ - ViewScoped
  * Flash Scope - 画面間のデータ受け渡し
  * Session Scope - セッション保持

---

## テストの実装

マイグレーション後のシステムが、元のシステムと同等の機能を持つことを、テストで検証する

### ユニットテスト

* Serviceクラスのビジネスロジックをテストする
* JUnit 5 + Mockito
* EntityManagerをモック化する

### 統合テスト

* 画面遷移テスト
* データベースアクセステスト
* Arquillianを使用（オプション）

---

## 参考資料

* [マイグレーションルール](../principles/) - マッピング規則、マイグレーションルール
* [リバースエンジニアリングインストラクション](reverse_engineering.md) - ステップ1: 既存コード分析
* [タスク分解インストラクション](task_breakdown.md) - ステップ2: タスク分解
* [詳細設計インストラクション](detailed_design.md) - ステップ3: 詳細設計
* [jakarta-ee-api-basic/instructions/code_generation.md](../../jakarta-ee-api-basic/instructions/code_generation.md) - コード生成（実行用）
