# 既存コード分析インストラクション（仕様書生成）

## パラメータ設定

実行前に以下のパラメータを設定してください:

```yaml
struts_project_root: "ここに既存Strutsプロジェクトのルートパスを入力"
spec_output_directory: "ここに仕様書の出力先パスを入力"
```

* 例:
```yaml
struts_project_root: "projects/legacy/struts-app"
spec_output_directory: "projects/jsf-migration/struts-app-jsf/specs"
```

注意: 
* パス区切りはOS環境に応じて調整してください（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{struts_project_root}` と表記されている箇所は、上記で設定した値に置き換えてください
* 以降、`{spec_output_directory}` と表記されている箇所は、上記で設定した値に置き換えてください

---

## 概要

このインストラクションは、既存のApache Struts 1.xプロジェクトから抽象的・論理的な仕様書を生成するためのものです。

重要な方針:
* Code-to-Codeの直接変換ではなく、まず仕様書を生成
* Strutsの実装詳細に囚われず、ビジネスロジックとビジネスルールを抽出
* 生成された仕様書は、JSFフレームワークに依存しない抽象的なものとする
* 技術スタックの記載は移行先（Jakarta EE 10、JSF 4.0）を前提とする

出力先:
* `{spec_output_directory}/baseline/system/` - システムレベルの仕様書
* `{spec_output_directory}/baseline/screen/` - 画面単位の仕様書

---

## 1. Strutsプロジェクトの分析

パラメータで指定されたStrutsプロジェクトを分析してください。

注意: `{struts_project_root}` は、パラメータで指定されたパスに置き換えてください。

### 1.1 Agent Skills憲章（最優先で確認）

* @agent_skills/struts-to-jsf-migration/principles/ - マイグレーション原則、マッピング規則を確認
  * `constitution.md` - マイグレーション憲章
  * Code-to-Spec-to-Codeアプローチ、マッピング規則、Markdownフォーマット規約を確認

### 1.2 Strutsプロジェクト構造の把握

以下のディレクトリとファイルを探索してください：

* ソースコード:
  * `{struts_project_root}/src/main/java/` - Javaソースコード
  * `{struts_project_root}/src/main/webapp/` - JSP、設定ファイル

* 主要な構成要素:
  * Action: `*Action.java` - コントローラークラス
  * ActionForm: `*Form.java` - フォームクラス
  * EJB Service: `*Service.java`, `*ServiceBean.java` - ビジネスロジック
  * DAO: `*Dao.java` - データアクセス層
  * Model/Entity: `*.java`（パッケージ: model, entity等）
  * JSP: `*.jsp` - ビューファイル

* 設定ファイル:
  * `struts-config.xml` - Strutsマッピング設定
  * `web.xml` - Webアプリケーション設定
  * `ejb-jar.xml` - EJB設定（存在する場合）
  * `ApplicationResources.properties` - メッセージリソース

* SQLスクリプト:
  * `{struts_project_root}/sql/` - データベーススキーマ、初期データ

### 1.3 コンポーネント間の関係を把握

以下の関係を分析してください：

* 画面遷移:
  * struts-config.xmlのaction-mappings
  * ActionのActionForward
  * JSPのリンクとフォームアクション

* データフロー:
  * ActionForm → Action → Service → DAO → Database
  * Database → DAO → Service → Action → JSP

* ビジネスロジック:
  * Serviceクラスのメソッド
  * バリデーションロジック
  * トランザクション境界

---

## 2. 仕様書の生成

### 2.1 システムレベルの仕様書

以下の仕様書を `{spec_output_directory}/baseline/system/` に生成してください：

#### requirements.md

* システムの目的:
  * Strutsプロジェクトの全体的な目的を記述
  * READMEやドキュメントから抽出

* 機能要件:
  * 実装されている機能の一覧
  * Actionクラス、JSPから抽出

* 非機能要件:
  * データベース種類（HSQLDBなど）
  * 認証・認可の有無
  * トランザクション要件

#### architecture_design.md

* 技術スタック（移行先を記載）:
  * Java 21
  * Jakarta EE 10
  * Jakarta Faces (JSF) 4.0
  * Jakarta Persistence (JPA) 3.1
  * Jakarta CDI 4.0
  * Payara Server 6（またはWildFly）
  * データベース: 既存のまま（例: HSQLDB 2.7.x）

* パッケージ構造:
  * Strutsのパッケージ構造を参考に、JSF向けに最適化
  * 例: `com.example.app.bean`, `com.example.app.entity`, `com.example.app.service`

* レイヤー構成:
  * Presentation Layer: JSF Managed Bean
  * Business Logic Layer: CDI Service（`@Transactional`）
  * Data Access Layer: JPA Entity、EntityManager
  * Database Layer: 既存データベース

* アーキテクチャパターン:
  * MVC（Model-View-Controller）
  * CDIによる依存性注入
  * JPAによる永続化
  * トランザクション管理（`@Transactional`）

#### functional_design.md

* 画面一覧:
  * 各JSPファイルから画面を抽出
  * 画面ID、画面名、URL、目的を記載

* 画面遷移図:
  * Mermaid形式で画面遷移を図示
  * struts-config.xmlとActionForwardから抽出

* コンポーネント設計:
  * 各画面に対応するManaged Bean
  * 各画面で使用するServiceクラス
  * 画面間のデータ受け渡し（Flash Scope、Session Scope等）

#### data_model.md

* エンティティ一覧:
  * StrutsのModel/Entityクラスから抽出
  * または、DAOクラスとSQLスクリプトから推測

* テーブル定義:
  * SQLスクリプト（`CREATE TABLE`）から抽出
  * カラム名、データ型、制約（PRIMARY KEY、FOREIGN KEY、NOT NULL等）

* エンティティリレーション:
  * テーブル間の関係（OneToMany、ManyToOne等）
  * SQLスクリプトのFOREIGN KEY制約から抽出

* JPA設計:
  * `@Entity`, `@Table`, `@Id`, `@Column`アノテーション
  * リレーションアノテーション（`@ManyToOne`, `@OneToMany`等）

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

### 2.2 画面単位の仕様書

各JSPファイルに対して、以下の仕様書を `{spec_output_directory}/baseline/screen/SCREEN_XXX_<画面名>/` に生成してください：

#### screen_design.md

* 画面ID: SCREEN_XXX_<画面名>（例: SCREEN_001_PersonList）
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
* SQLクエリ
* CRUD操作（Create, Read, Update, Delete）
* ResultSetからオブジェクトへのマッピング

JSFへのマッピング:
* DAO → JPAのEntityManager操作
* SQL → JPQL
* `PreparedStatement` → `em.createQuery()`
* 手動マッピング → 自動マッピング（Entity）

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

## 4. 仕様書生成のガイドライン

### 4.1 抽象化の原則

* Strutsの実装詳細は含めない:
  * ❌ 「ActionForwardを返す」
  * ✅ 「画面XXXに遷移する」

* ビジネスロジックに焦点を当てる:
  * ❌ 「JNDIルックアップでEJBを取得し、メソッドを呼び出す」
  * ✅ 「PersonServiceを使用して全PERSONを取得する」

* フレームワーク非依存の記述:
  * ❌ 「request.setAttribute()でデータを設定」
  * ✅ 「画面に表示するデータを準備する」

### 4.2 JSFアーキテクチャへの適応

仕様書には、JSFの技術スタックを前提とした設計を記載します：

* Managed Bean: `@Named`, `@ViewScoped`
* CDI: `@Inject`
* JPA: `@Entity`, `@PersistenceContext`
* トランザクション: `@Transactional`
* Facelets: XHTML、`<h:*>`タグ

### 4.3 データモデルの継続性

* データベーススキーマは変更しない
* テーブル名、カラム名は既存のまま使用
* JPA Entityは既存テーブルにマッピング

### 4.4 ビジネスロジックの保全

* StrutsのServiceクラスやEJBのビジネスロジックを正確に抽出
* バリデーションルール、計算ロジック、状態遷移を仕様書に記載
* ビジネスルールを失わないように注意

---

## 5. 生成手順

1. Strutsプロジェクト探索: `{struts_project_root}` 配下のファイルを探索し、構造を把握
2. コンポーネント分析: Action、ActionForm、EJB、DAO、JSPを分析
3. 関係抽出: 画面遷移、データフロー、ビジネスロジックの関係を抽出
4. 仕様書生成: システムレベルと画面単位の仕様書を生成
5. 検証: 生成された仕様書の完全性を確認（次のステップで実施）

---

## 6. 重要な注意事項

### Markdownフォーマット規約

生成する仕様書は、憲章に記載されたフォーマット規約に従ってください：

* 箇条書きはアスタリスク（`*`）を使用（ハイフン `–` は使用しない）
* ボールド（太字）は使用しない
* 見出しレベル（`#`、`##`、`###`）で構造化
* コード例の前には箇条書き形式で説明を記載

### プロジェクトルートの扱い

* `{struts_project_root}` は、パラメータで明示的に指定されたパスに置き換えてください
* `{spec_output_directory}` は、パラメータで明示的に指定されたパスに置き換えてください
* 相対パスでも絶対パスでも構いません

### 既存ドキュメントの活用

* READMEファイルがあれば、システムの目的や概要を抽出
* SQLスクリプトがあれば、テーブル定義を抽出
* ドキュメントフォルダがあれば、参考資料として活用

### 不明点の扱い

* コードから抽出できない情報（例: 業務ルールの背景、非機能要件の詳細）は、仕様書に「要確認」として記載
* 次のステップ（仕様書検証）でユーザーに質問して補完

---

## 7. 成果物チェックリスト

生成された仕様書が満たすべき要件：

* システムレベルの仕様書（requirements.md、architecture_design.md、functional_design.md、data_model.md、behaviors.md）が生成されている
* 各画面単位の仕様書（screen_design.md、functional_design.md、behaviors.md）が生成されている
* Strutsの実装詳細に囚われず、抽象的・論理的な記述になっている
* JSFアーキテクチャ（Managed Bean、CDI、JPA、Facelets）を前提とした設計になっている
* データベーススキーマは既存のまま継続される設計になっている
* ビジネスロジックとビジネスルールが正確に抽出されている
* Markdownフォーマット規約に従っている

---

## 参考資料

* [マイグレーション憲章](../principles/constitution.md) - マッピング規則、原則
* [タスク分解インストラクション](task_breakdown.md) - 次のステップ（ステップ2）
* [詳細設計インストラクション](detailed_design.md) - 次のステップ（ステップ3）
* [コード生成インストラクション](code_generation.md) - 次のステップ（ステップ4）
