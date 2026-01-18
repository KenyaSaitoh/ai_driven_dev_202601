# 既存コード分析インストラクション（SPEC生成）

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
struts_project_root: "ここに既存Strutsプロジェクトのルートパスを入力"
spec_output_directory: "ここにSPECの出力先パスを入力"
```

* 例
```yaml
struts_project_root: "projects/legacy/struts-app"
spec_output_directory: "projects/jsf-migration/struts-app-jsf/specs"
```

注意
* パス区切りはOS環境に応じて調整する（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{struts_project_root}` と表記されている箇所は、上記で設定した値に置き換える
* 以降、`{spec_output_directory}` と表記されている箇所は、上記で設定した値に置き換える

---

## 概要

このインストラクションは、既存のApache Struts 1.xプロジェクトから抽象的・論理的なSPECを生成するためのものである

重要な方針
* Code-to-Codeの直接変換ではなく、まずSPECを生成する
* Strutsの実装詳細に囚われず、ビジネスロジックとビジネスルールを抽出する
* 生成されたSPECは、JSFフレームワークに依存しない抽象的なものとする
* 技術スタックの記載は移行先を前提とする

出力先
* `{spec_output_directory}/baseline/basic_design/` - システムレベルのSPEC
* `{spec_output_directory}/baseline/detailed_design/screen/` - 画面単位のSPEC

---

## 1. Strutsプロジェクトの分析

パラメータで指定されたStrutsプロジェクトを分析する

注意: `{struts_project_root}` は、パラメータで指定されたパスに置き換える

### 1.1 Agent Skillsルール（最優先で確認）

* @agent_skills/struts-to-jsf-migration/principles/ - マイグレーションルール、アーキテクチャ標準、マッピング規則、セキュリティ標準を確認する
  * このフォルダ配下のすべてのMarkdownファイルを読み込み、マイグレーションルールを遵守すること
  * Code-to-Spec-to-Codeアプローチ、マッピング規則、Markdownフォーマット規約を確認する

### 1.2 フレームワーク仕様（該当する場合）

* @agent_skills/struts-to-jsf-migration/frameworks/ - フレームワーク固有のSPECやサンプルコードを確認する
* @agent_skills/jakarta-ee-api-base/frameworks/ - フレームワーク固有のSPECやサンプルコードを確認する
  * 特定のフレームワーク（ライブラリ、ツール等）の使用方法、設計パターン、実装例を参照する
  * SPEC生成時に、フレームワーク仕様を考慮した設計を記載する

### 1.3 Strutsプロジェクト構造の把握

以下のディレクトリとファイルを探索する

* ソースコード
  * `{struts_project_root}/src/main/java/` - Javaソースコード
  * `{struts_project_root}/src/main/webapp/` - JSP、設定ファイル

* 主要な構成要素
  * Action: `*Action.java` - コントローラークラス
  * ActionForm: `*Form.java` - フォームクラス
  * EJB Service: `*Service.java`, `*ServiceBean.java` - ビジネスロジック
  * DAO: `*Dao.java` - データアクセス層
  * Model/Entity: `*.java`（パッケージ: model, entity等）
  * JSP: `*.jsp` - ビューファイル

* 設定ファイル
  * `struts-config.xml` - Strutsマッピング設定
  * `web.xml` - Webアプリケーション設定
  * `ejb-jar.xml` - EJB設定（存在する場合）
  * `ApplicationResources.properties` - メッセージリソース

* SQLスクリプト
  * `{struts_project_root}/sql/` - データベーススキーマ、初期データ

### 1.4 データソースとJNDI名の特定

移行先でJPAのpersistence.xmlを設定するために、既存のデータソース設定を正確に把握する

* JNDI名は複数箇所で定義される可能性があり、決め打ちはできない。以下の全箇所を調査すること

調査対象:
* `web.xml` - `<resource-ref>` 要素
  * `<res-ref-name>` タグで参照名を確認（例: `jdbc/HsqldbDS`）
  * `<res-type>` タグでDataSource型を確認
  
* DAOクラス - JNDIルックアップコード
  * `InitialContext.lookup("java:comp/env/jdbc/...")` のような記述
  * 実際に使用されているJNDI名を確認
  
* `ejb-jar.xml` - `<resource-ref>` 要素（存在する場合）
  * EJBからのデータソース参照設定
  
* アプリケーションサーバー設定ファイル（存在する場合）
  * `context.xml`, `tomee.xml` 等
  * 実際のデータソースリソース定義（JDBCドライバー、接続URL、認証情報）

抽出すべき情報:
* JNDI参照名（例: `jdbc/HsqldbDS`）
* 完全修飾JNDI名（例: `java:comp/env/jdbc/HsqldbDS`）
* データベース種類（HSQLDB、PostgreSQL、MySQL等）
* JTAマネージドかどうか

注意: プロジェクトによってJNDI名の命名規則は異なるため、必ず実際のコードと設定ファイルから抽出すること

### 1.5 コンポーネント間の関係を把握

以下の関係を分析する

* 画面遷移
  * struts-config.xmlのaction-mappings
  * ActionのActionForward
  * JSPのリンクとフォームアクション

* データフロー
  * ActionForm → Action → Service → DAO → Database
  * Database → DAO → Service → Action → JSP

* ビジネスロジック
  * Serviceクラスのメソッド
  * バリデーションロジック
  * トランザクション境界

---

## 2. SPECの生成

### 2.1 システムレベルのSPEC

以下のSPECを `{spec_output_directory}/baseline/basic_design/` に生成する

#### requirements.md

* システムの目的
  * Strutsプロジェクトの全体的な目的を記述する
  * READMEやドキュメントから抽出する

* 機能要件
  * 実装されている機能の一覧
  * Actionクラス、JSPから抽出する

* 非機能要件
  * データベース種類
  * 認証・認可の有無
  * トランザクション要件

#### architecture_design.md

* 技術スタック（移行先を記載）
  * 移行先の技術スタックを記載する

* パッケージ構造
  * Strutsのパッケージ構造を参考に、JSF向けに最適化する

* レイヤー構成
  * Presentation Layer: JSF Managed Bean
  * Business Logic Layer: CDI Service
  * Data Access Layer: JPA Entity、EntityManager
  * Database Layer: 既存データベース

* アーキテクチャパターン
  * MVC（Model-View-Controller）
  * CDIによる依存性注入
  * JPAによる永続化
  * トランザクション管理

* データソース設定（インフラ構成）
  * 移行元で使用しているJNDI名を明記する（web.xml、DAOクラス、アプリケーションサーバー設定から抽出）
  * JNDI参照名（例: `jdbc/HsqldbDS`）
  * 完全修飾JNDI名（例: `java:comp/env/jdbc/HsqldbDS` または `java:app/jdbc/testdb`）
  * データベース種類（HSQLDB、PostgreSQL、MySQL等）
  * JDBCドライバークラス、接続URL
  * JTAマネージド: true/false
  
* persistence.xml設定情報
  * `<jta-data-source>` に設定するJNDI名（上記データソース設定から取得）
  * Persistence Unit名（例: `personPU`）
  * トランザクションタイプ: JTA（通常）
  * 注意: この情報は詳細設計フェーズでpersistence.xml生成時に使用される

#### functional_design.md

* 画面一覧:
  * 各JSPファイルから画面を抽出
  * 画面ID、画面名、URL、目的を記載

* 画面遷移図:
  * Mermaid形式で画面遷移を図示
  * struts-config.xmlとActionForwardから抽出

* コンポーネント設計:
  * 各機能に対応するManaged Bean
  * 各機能で使用するServiceクラス
  * 画面間のデータ受け渡し（Flash Scope、Session Scope等）

#### data_model.md

* エンティティ一覧（論理名）:
  * StrutsのModel/Entityクラスから抽出
  * または、DAOクラスとSQLスクリプトから推測
  * 注意: 論理エンティティ名のみ。JPAエンティティクラス名は詳細設計で決定

* テーブル定義（RDB論理設計）:
  * SQLスクリプト（`CREATE TABLE`）から抽出
  * カラム名、データ型、制約（PRIMARY KEY、FOREIGN KEY、NOT NULL等）
  * インデックス定義

* テーブル間のリレーション:
  * FOREIGN KEY制約から抽出
  * カーディナリティ（1:1、1:N、N:M）

注意: 
* data_model.mdはRDB論理設計（テーブル、カラム、制約）のみを記述
* JPAエンティティクラスの設計（@Entity、@Column等のアノテーション、Javaクラス構造）は詳細設計フェーズで実施
* persistence.xml設定情報（JNDI名、Persistence Unit名）はarchitecture_design.mdに記載

#### behaviors.md

* システム全体の振る舞い:
  * 主要な業務フロー
  * Serviceクラスのビジネスロジックから抽出

* エラーハンドリング:
  * 例外処理のパターン
  * エラーメッセージの表示方法

* バリデーション:
  * 入力検証ルール
  * ActionFormのvalidate()メソッドまたはJSPから抽出

### 2.2 画面単位のSPEC

各JSPファイルに対して、以下のSPECを `{spec_output_directory}/baseline/detailed_design/detailed_design/FUNC_XXX_<画面名>/` に生成してください：

#### screen_design.md

* 画面ID: FUNC_XXX_<画面名>（例: FUNC_001_PersonList）
* 画面名: <画面の日本語名>
* URL: <アクセスURL>
* 目的: <画面の目的>

* 画面レイアウト:
  * 表示項目（一覧表、入力フォーム、ボタン等）
  * JSPのHTMLタグとStrutsタグから抽出

* 入力項目:
  * 項目名、型、必須/任意、バリデーションルール
  * ActionFormのフィールドから抽出

* ボタンとアクション:
  * ボタン名、アクション、遷移先画面
  * JSPの`<html:submit>`とActionForwardから抽出

* 表示データ:
  * 表示するデータの一覧
  * JSPの`<logic:iterate>`と`<bean:write>`から抽出

#### functional_design.md

* Managed Bean設計:
  * Bean名、スコープ（`@ViewScoped`等）
  * プロパティ（ActionFormのフィールドに対応）
  * アクションメソッド（Actionのexecute()に対応）

* Serviceクラス設計:
  * Service名、メソッドシグネチャ
  * ビジネスロジックの概要
  * Struts ServiceクラスやEJBから抽出

* データアクセス:
  * 使用するEntityクラス
  * JPQLクエリ（DAOのSQLをJPQLに変換）

#### behaviors.md

* 画面の振る舞い:
  * 初期表示時の動作（`@PostConstruct`）
  * ボタンクリック時の動作（アクションメソッド）
  * バリデーションエラー時の動作

* Given-When-Thenシナリオ:
  * 主要なユースケースをシナリオ形式で記述
  * Actionのロジックから抽出

* エラーケース:
  * 想定されるエラー
  * エラーメッセージ
  * Actionのエラーハンドリングから抽出

---

## 3. Strutsコンポーネントの分析方法

### 3.1 Action分析

* クラス名: `*Action.java`
* 継承: `org.apache.struts.action.Action`
* メソッド: `execute(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)`

抽出すべき情報:
* ビジネスロジックの呼び出し（EJBルックアップ、Serviceメソッド呼び出し）
* リクエストスコープへのデータ設定（`request.setAttribute()`）
* 遷移先の決定（`ActionForward`）
* エラーハンドリング（例外処理）

JSFへのマッピング:
* Action → Managed Beanのアクションメソッド
* `execute()` → `public String actionMethod()`
* `ActionForward` → `return "画面ID"`
* EJBルックアップ → `@Inject private Service service`

### 3.2 ActionForm分析

* クラス名: `*Form.java`
* 継承: `org.apache.struts.action.ActionForm`
* フィールド: リクエストパラメータ（文字列型が多い）

抽出すべき情報:
* フィールド名、型
* バリデーションメソッド（`validate()`）
* リセットメソッド（`reset()`）

JSFへのマッピング:
* ActionForm → Managed Beanのプロパティ
* String型フィールド → 適切な型（Integer, Date等）に変換
* `validate()` → Bean Validationアノテーション（`@NotNull`, `@Size`等）

### 3.3 EJB Service分析

* クラス名: `*Service.java`, `*ServiceBean.java`
* アノテーション: `@Stateless`（EJB 3.x）
* メソッド: ビジネスロジック

抽出すべき情報:
* メソッドシグネチャ
* ビジネスロジックの内容
* トランザクション境界
* DAOの呼び出し

JSFへのマッピング:
* EJB → CDI Service
* `@Stateless` → `@RequestScoped` + `@Transactional`
* JNDIルックアップ → `@Inject`

### 3.4 DAO分析

* クラス名: `*Dao.java`
* データソースルックアップ: `InitialContext.lookup("jdbc/...")`
* JDBC操作: `PreparedStatement`, `ResultSet`

抽出すべき情報:
* JNDIルックアップで使用されている完全なJNDI名（例: `java:comp/env/jdbc/HsqldbDS`）
  * この情報は必ずarchitecture_design.mdに記載すること
  * 複数のDAOがある場合、全てのJNDI名を確認すること（通常は統一されているが、例外もある）
* SQLクエリ
* CRUD操作（Create, Read, Update, Delete）
* ResultSetからオブジェクトへのマッピング

JSFへのマッピング:
* DAO → JPAのEntityManager操作
* SQL → JPQL
* `PreparedStatement` → `em.createQuery()`
* 手動マッピング → 自動マッピング（Entity）
* JNDIデータソース → persistence.xmlの`<jta-data-source>`で同じJNDI名を使用

### 3.5 JSP分析

* ファイル名: `*.jsp`
* Strutsタグライブラリ: `<logic:*>`, `<bean:*>`, `<html:*>`

抽出すべき情報:
* 画面レイアウト（テーブル、フォーム、ボタン等）
* 表示データ（`<bean:write>`）
* ループ処理（`<logic:iterate>`）
* フォーム（`<html:form>`, `<html:text>`, `<html:submit>`）
* リンク（`<html:link>`, `<a href="...">`）

JSFへのマッピング:
* JSP → XHTML（Facelets）
* `<logic:iterate>` → `<h:dataTable>` または `<ui:repeat>`
* `<bean:write>` → `<h:outputText value="#{...}"/>`
* `<html:form>` → `<h:form>`
* `<html:text>` → `<h:inputText value="#{...}"/>`
* `<html:submit>` → `<h:commandButton action="#{...}"/>`

### 3.6 struts-config.xml分析

* action-mappings: URLとActionのマッピング
* form-beans: ActionFormの定義
* global-forwards: グローバル遷移先
* message-resources: メッセージリソース

抽出すべき情報:
* URLパターン（path属性）
* Actionクラス（type属性）
* ActionForm（name属性）
* 遷移先（forward要素）

JSFへのマッピング:
* action-mappings → faces-config.xmlのnavigation-rule（または`public String actionMethod()`の戻り値）
* form-beans → Managed Beanのプロパティ
* global-forwards → グローバルナビゲーション

---

## 4. SPEC生成のガイドライン

### 4.1 抽象化のルール

* Strutsの実装詳細は含めない
  * ❌ 「ActionForwardを返す」
  * ✅ 「画面XXXに遷移する」

* ビジネスロジックに焦点を当てる
  * ❌ 「JNDIルックアップでEJBを取得し、メソッドを呼び出す」
  * ✅ 「PersonServiceを使用して全PERSONを取得する」

* フレームワーク非依存の記述
  * ❌ 「request.setAttribute()でデータを設定」
  * ✅ 「画面に表示するデータを準備する」

### 4.2 JSFアーキテクチャへの適応

SPECには、JSFの技術スタックを前提とした設計を記載する

* Managed Bean: `@Named`, `@ViewScoped`
* CDI: `@Inject`
* JPA: `@Entity`, `@PersistenceContext`
* トランザクション: `@Transactional`
* Facelets: XHTML、`<h:*>`タグ

### 4.3 データモデルの継続性

* データベーススキーマは変更しない
* テーブル名、カラム名は既存のまま使用する
* JPA Entityは既存テーブルにマッピングする

### 4.4 ビジネスロジックの保全

* StrutsのServiceクラスやEJBのビジネスロジックを正確に抽出する
* バリデーションルール、計算ロジック、状態遷移をSPECに記載する
* ビジネスルールを失わないように注意する

---

## 5. 生成手順

1. Strutsプロジェクト探索: `{struts_project_root}` 配下のファイルを探索し、構造を把握する
2. コンポーネント分析: Action、ActionForm、EJB、DAO、JSPを分析する
3. 関係抽出: 画面遷移、データフロー、ビジネスロジックの関係を抽出する
4. SPEC生成: システムレベルと画面単位のSPECを生成する
5. 検証: 生成されたSPECの完全性を確認する（次のステップで実施）

---

## 6. 重要な注意事項

### Markdownフォーマット規約

生成するSPECは、ルールドキュメントに記載されたフォーマット規約に従う

* 箇条書きはアスタリスク（`*`）を使用する（ハイフン `–` は使用しない）
* ボールド（太字）は使用しない
* 見出しレベル（`#`、`##`、`###`）で構造化する
* コード例の前には箇条書き形式で説明を記載する

### プロジェクトルートの扱い

* `{struts_project_root}` は、パラメータで明示的に指定されたパスに置き換える
* `{spec_output_directory}` は、パラメータで明示的に指定されたパスに置き換える
* 相対パスでも絶対パスでも構わない

### 既存ドキュメントの活用

* READMEファイルがあれば、システムの目的や概要を抽出する
* SQLスクリプトがあれば、テーブル定義を抽出する
* ドキュメントフォルダがあれば、参考資料として活用する

### 不明点の扱い

* コードから抽出できない情報は、SPECに「要確認」として記載する
* 次のステップ（SPEC検証）でユーザーに質問して補完する

---

## 7. 成果物チェックリスト

生成されたSPECが満たすべき要件

* システムレベルのSPECが生成されている
* 各機能単位のSPECが生成されている
* Strutsの実装詳細に囚われず、抽象的・論理的な記述になっている
* JSFアーキテクチャを前提とした設計になっている
* データベーススキーマは既存のまま継続される設計になっている
* ビジネスロジックとビジネスルールが正確に抽出されている
* Markdownフォーマット規約に従っている

---

## 参考資料

* [マイグレーションルール](../principles/) - マッピング規則、マイグレーションルール
* [タスク分解インストラクション](task_breakdown.md) - 次のステップ（ステップ2）
* [詳細設計インストラクション](detailed_design.md) - 次のステップ（ステップ3）
* [コード生成インストラクション](code_generation.md) - 次のステップ（ステップ4）
