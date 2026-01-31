# アーキテクチャ設計書

## 1. 概要

本ドキュメントは、人材管理システム（JSF Person）のアーキテクチャ設計を定義する。

## 2. 技術スタック

### 2.1 アプリケーション基盤

* プログラミング言語
  * Java 21

* Jakarta EE 10
  * Jakarta Faces (JSF) 4.0 - プレゼンテーション層
  * Jakarta Persistence (JPA) 3.1 - データアクセス層
  * Jakarta CDI 4.0 - 依存性注入
  * Jakarta Transactions (JTA) 2.0 - トランザクション管理
  * Jakarta Servlet 6.0 - Webアプリケーション基盤
  * Jakarta Expression Language 5.0 - 式言語

### 2.2 実行環境

* アプリケーションサーバー
  * Payara Server 6.x（Jakarta EE 10対応）

* データベース
  * HSQLDB 2.7.x（既存データベースを継続使用）

### 2.3 ビルドツール

* ビルドツール
  * Gradle 8.x

## 3. アーキテクチャパターン

### 3.1 レイヤードアーキテクチャ

本システムは、レイヤードアーキテクチャ（Layered Architecture）を採用する。

```mermaid
graph TB
    subgraph "Presentation Layer"
        View[XHTML View<br/>Facelets]
        Bean[Managed Bean<br/>@Named, @ViewScoped]
    end
    
    subgraph "Business Logic Layer"
        Service[Service<br/>@RequestScoped, @Transactional]
    end
    
    subgraph "Data Access Layer"
        Entity[JPA Entity<br/>@Entity]
        EM[EntityManager<br/>@PersistenceContext]
    end
    
    subgraph "Database Layer"
        DB[(HSQLDB<br/>PERSONテーブル)]
    end
    
    View -->|EL式| Bean
    Bean -->|@Inject| Service
    Service -->|@PersistenceContext| EM
    EM -->|JPQL| Entity
    Entity -->|JDBC| DB
```

### 3.2 レイヤーの責務

* Presentation Layer（プレゼンテーション層）
  * 役割: ユーザーインターフェースの提供、画面制御、画面遷移
  * 技術: JSF Managed Bean、Facelets XHTML
  * 主要コンポーネント: PersonListBean、PersonInputBean、PersonConfirmBean
  * 責務:
    * ユーザー入力の受け取り
    * 画面表示データの準備
    * 画面遷移の制御
    * Serviceクラスの呼び出し
    * エラーメッセージの表示

* Business Logic Layer（ビジネスロジック層）
  * 役割: ビジネスロジックの実装、トランザクション管理
  * 技術: CDI Service、@Transactional
  * 主要コンポーネント: PersonService
  * 責務:
    * ビジネスルールの実装
    * トランザクション境界の定義
    * データアクセス層の呼び出し
    * ビジネス例外のハンドリング

* Data Access Layer（データアクセス層）
  * 役割: データベースアクセス、エンティティ管理
  * 技術: JPA、EntityManager、JPQL
  * 主要コンポーネント: Person Entity、EntityManager
  * 責務:
    * エンティティのCRUD操作
    * JPQLクエリの実行
    * エンティティとデータベースのマッピング

* Database Layer（データベース層）
  * 役割: データの永続化
  * 技術: HSQLDB
  * 主要コンポーネント: PERSONテーブル
  * 責務:
    * データの保存と取得
    * データ整合性の保証

### 3.3 MVC（Model-View-Controller）パターン

JSFは標準的にMVCパターンを実装している:

* Model: JPA Entity（Person）
* View: Facelets XHTML（personList.xhtml、personInput.xhtml、personConfirm.xhtml）
* Controller: Managed Bean（PersonListBean、PersonInputBean、PersonConfirmBean）

### 3.4 依存性注入（CDI）

* CDI（Contexts and Dependency Injection）を使用して、レイヤー間の依存性を管理する
* @Injectアノテーションによる依存性注入
* コンストラクタインジェクションまたはフィールドインジェクション

```java
@Named
@ViewScoped
public class PersonListBean implements Serializable {
    @Inject
    private PersonService personService;
}
```

```java
@RequestScoped
@Transactional
public class PersonService {
    @PersistenceContext
    private EntityManager em;
}
```

### 3.5 トランザクション管理

* JTA（Jakarta Transactions）による宣言的トランザクション管理
* @Transactionalアノテーションでトランザクション境界を定義
* Serviceクラスのパブリックメソッドがトランザクション境界

```java
@RequestScoped
@Transactional
public class PersonService {
    public void addPerson(Person person) {
        // トランザクション内で実行される
    }
}
```

## 4. パッケージ構造

### 4.1 パッケージ階層

```
pro.kensait.jsf.person/
├── bean/                   # Presentation Layer
│   ├── PersonListBean.java
│   ├── PersonInputBean.java
│   └── PersonConfirmBean.java
├── service/                # Business Logic Layer
│   └── PersonService.java
└── entity/                 # Data Access Layer
    └── Person.java
```

### 4.2 パッケージ詳細

* pro.kensait.jsf.person.bean
  * JSF Managed Beanを配置
  * @Named、@ViewScopedアノテーションを使用
  * ビューとの連携、画面遷移制御を担当

* pro.kensait.jsf.person.service
  * ビジネスロジックを実装するServiceクラスを配置
  * @RequestScoped、@Transactionalアノテーションを使用
  * トランザクション境界を定義

* pro.kensait.jsf.person.entity
  * JPA Entityクラスを配置
  * @Entity、@Tableアノテーションを使用
  * データベーステーブルとのマッピング

### 4.3 リソースファイル配置

```
src/main/
├── java/
│   └── pro/kensait/jsf/person/
│       ├── bean/
│       ├── service/
│       └── entity/
├── resources/
│   └── META-INF/
│       └── persistence.xml     # JPA設定
└── webapp/
    ├── WEB-INF/
    │   ├── web.xml             # Webアプリケーション設定
    │   └── faces-config.xml    # JSF設定（オプション）
    ├── personList.xhtml
    ├── personInput.xhtml
    ├── personConfirm.xhtml
    └── resources/
        └── css/
            └── style.css       # スタイルシート
```

## 5. 主要コンポーネント設計

### 5.1 Presentation Layer

#### 5.1.1 Managed Bean

* PersonListBean
  * 役割: PERSON一覧画面の制御
  * スコープ: @ViewScoped
  * 主要メソッド:
    * init(): 画面初期表示時にPERSONリストを取得
    * getPersonList(): PERSONリストを返す

* PersonInputBean
  * 役割: PERSON入力画面の制御
  * スコープ: @ViewScoped
  * 主要メソッド:
    * init(): 画面初期表示時に編集モードの場合はデータを取得
    * confirm(): 確認画面に遷移
    * cancel(): 一覧画面に戻る

* PersonConfirmBean
  * 役割: PERSON確認画面の制御
  * スコープ: @ViewScoped
  * 主要メソッド:
    * save(): データを登録・更新して一覧画面に遷移
    * back(): 入力画面に戻る

#### 5.1.2 画面遷移方式

* アクションメソッドの戻り値で画面遷移を制御
* faces-config.xmlのnavigation-ruleは使用せず、戻り値で直接XHTMLファイル名を指定
* リダイレクト方式: 戻り値に "?faces-redirect=true" を付加

```java
public String save() {
    personService.addPerson(person);
    return "personList?faces-redirect=true";
}
```

### 5.2 Business Logic Layer

#### 5.2.1 PersonService

* 役割: PERSONビジネスロジックの実装
* スコープ: @RequestScoped
* トランザクション: @Transactional（クラスレベル）
* 主要メソッド:
  * getAllPersons(): 全PERSONを取得
  * getPersonById(Integer personId): IDでPERSONを取得
  * addPerson(Person person): PERSONを追加
  * updatePerson(Person person): PERSONを更新
  * deletePerson(Integer personId): PERSONを削除

#### 5.2.2 トランザクション境界

* PersonServiceのすべてのパブリックメソッドがトランザクション境界
* RuntimeExceptionが発生した場合、自動的にロールバック
* トランザクション分離レベル: READ_COMMITTED

### 5.3 Data Access Layer

#### 5.3.1 Person Entity

* 役割: PERSONテーブルとのマッピング
* アノテーション:
  * @Entity: JPAエンティティとして定義
  * @Table(name = "PERSON"): テーブル名を指定
  * @Id: 主キー
  * @GeneratedValue(strategy = GenerationType.IDENTITY): 自動採番
  * @Column: カラムマッピング

```java
@Entity
@Table(name = "PERSON")
public class Person implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PERSON_ID")
    private Integer personId;
    
    @Column(name = "PERSON_NAME", nullable = false, length = 30)
    private String personName;
    
    @Column(name = "AGE", nullable = false)
    private Integer age;
    
    @Column(name = "GENDER", nullable = false, length = 10)
    private String gender;
}
```

#### 5.3.2 データアクセス方式

* EntityManagerを使用したJPQL
* DAOクラスは作成せず、ServiceクラスでEntityManagerを直接使用
* 理由: 小規模システムのため、DAOレイヤーは冗長

```java
@RequestScoped
@Transactional
public class PersonService {
    @PersistenceContext
    private EntityManager em;
    
    public List<Person> getAllPersons() {
        return em.createQuery("SELECT p FROM Person p ORDER BY p.personId", Person.class)
                 .getResultList();
    }
}
```

## 6. データベース接続設定

### 6.1 データソース設定（インフラ構成）

* JNDI名: `java:app/jdbc/testdb`
* データベース種別: HSQLDB 2.7.x
* JDBCドライバー: org.hsqldb.jdbc.JDBCDriver
* 接続URL: jdbc:hsqldb:hsql://localhost:9001/testdb
* ユーザー名: SA
* パスワード: （空文字）
* 接続プール管理: Payara Server
* JTAマネージド: true

注意: 接続プール設定（最小/最大接続数、タイムアウト等）の詳細は、インフラ構築時またはデプロイメント設定で決定します。

### 6.2 persistence.xml設定情報

* Persistence Unit名: personPU
* `<jta-data-source>`: java:app/jdbc/testdb（上記データソース設定のJNDI名）
* トランザクションタイプ: JTA（Jakarta Transactions）
* スキーマ自動生成: none（既存スキーマを使用）

注意: persistence.xmlの具体的なXMLコード、エンティティクラスの登録、詳細プロパティ設定は詳細設計フェーズで記述します。

## 7. ビュー技術（Facelets）

### 7.1 Facelets XHTML

* JSPではなく、Facelets XHTMLを使用
* 拡張子: .xhtml
* JSF標準のビューテクノロジー

### 7.2 JSFタグライブラリ

* Core Tags（xmlns:f="jakarta.faces.core"）
  * メタデータ、イベント、コンバーター

* HTML Tags（xmlns:h="jakarta.faces.html"）
  * HTML要素の生成
  * <h:form>、<h:inputText>、<h:commandButton>、<h:dataTable>等

* Facelets Tags（xmlns:ui="jakarta.faces.facelets"）
  * テンプレート、コンポジション

### 7.3 Expression Language（EL）

* EL 5.0を使用
* 式言語: #{bean.property}
* メソッド呼び出し: #{bean.actionMethod()}

### 7.4 スタイルシート

* CSS3を使用
* resources/css/style.cssに配置
* JSFリソースハンドリング: <h:outputStylesheet name="css/style.css"/>

## 8. セキュリティ設計

### 8.1 認証・認可

* 本リリースでは認証・認可機能を実装しない
* 将来的にJakarta Securityを使用した認証・認可を追加する可能性がある

### 8.2 入力検証

* Bean Validationアノテーションによる入力検証
* @NotNull、@Size、@Min、@Max等を使用

### 8.3 XSS対策

* JSFのデフォルトエスケープ機能を使用
* <h:outputText>は自動的にHTMLエスケープを行う

### 8.4 CSRF対策

* JSFの<h:form>は自動的にCSRFトークンを生成
* jakarta.faces.ViewState hidden fieldによる保護

## 9. エラーハンドリング設計

### 9.1 例外処理戦略

* ビジネスロジック層で発生した例外はRuntimeExceptionでラップ
* プレゼンテーション層でユーザーフレンドリーなメッセージを表示

### 9.2 例外の種類

* RuntimeException（非チェック例外）
  * トランザクションを自動的にロールバック
  * ビジネスロジックエラー、データベースエラー

### 9.3 エラーメッセージ表示

* <h:messages>コンポーネントでエラーメッセージを表示
* FacesContext.addMessage()でメッセージを追加

## 10. ログ設計

### 10.1 ロギングフレームワーク

* Jakarta EE標準のjava.util.loggingを使用
* または、SLF4J + Logbackを使用する可能性がある

### 10.2 ログレベル

* SEVERE: 重大なエラー
* WARNING: 警告
* INFO: 情報メッセージ（登録・更新・削除等）
* FINE: デバッグ情報

### 10.3 ログ出力

* Serviceクラスで主要な処理のログを出力
* 例外発生時はスタックトレースをログに記録

## 11. デプロイメント

### 11.1 デプロイ形式

* WARファイル
* ファイル名: jsf-person.war

### 11.2 コンテキストパス

* /jsf-person

### 11.3 アクセスURL

* 一覧画面: http://localhost:8080/jsf-person/personList.xhtml

## 12. 既存システムとの差異

### 12.1 Strutsアーキテクチャ（移行前）

* Presentation Layer: Struts Action + ActionForm + JSP（Strutsタグライブラリ）
* Business Logic Layer: EJB 3.2（Stateless Session Bean、JNDIルックアップ）
* Data Access Layer: DAO（JDBC + DataSource、PreparedStatement）

### 12.2 JSFアーキテクチャ（移行後）

* Presentation Layer: JSF Managed Bean + Facelets XHTML（JSFタグライブラリ）
* Business Logic Layer: CDI Service（@RequestScoped、@Inject、@Transactional）
* Data Access Layer: JPA（EntityManager、JPQL、Entity）

### 12.3 主要な改善点

* 依存性注入
  * 移行前: JNDIルックアップでEJBを取得
  * 移行後: @Injectで依存性注入

* データアクセス
  * 移行前: JDBC + PreparedStatement + 手動マッピング
  * 移行後: JPA + JPQL + 自動マッピング

* ビュー技術
  * 移行前: JSP + Strutsタグライブラリ
  * 移行後: Facelets XHTML + JSFタグライブラリ

* トランザクション管理
  * 移行前: EJBコンテナ管理トランザクション
  * 移行後: JTA + @Transactional

## 13. 参考資料

* [システム要件定義](requirements.md) - システム要件
* [データモデル](data_model.md) - データベーススキーマの詳細
* [機能設計書](functional_design.md) - 画面遷移とコンポーネント設計
* [振る舞い仕様書](behaviors.md) - システム全体の振る舞い
* [Jakarta EE 10仕様](https://jakarta.ee/specifications/platform/10/)
* [Jakarta Faces 4.0仕様](https://jakarta.ee/specifications/faces/4.0/)
