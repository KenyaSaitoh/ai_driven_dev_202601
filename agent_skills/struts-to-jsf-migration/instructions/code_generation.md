# コード生成インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
task_file: "ここに実行するタスクファイルのパスを入力"
skip_infrastructure: false  # trueの場合、インフラセットアップをスキップ
```

* 例
```yaml
project_root: "projects/sdd/person/jsf-person-sdd"
task_file: "projects/sdd/person/jsf-person-sdd/tasks/setup_tasks.md"
skip_infrastructure: true  # インフラセットアップをスキップ
```

注意
* パス区切りはOS環境に応じて調整する（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換える

---

## 実装の実行

重要: 指定されたタスクファイルのタスクのみを実行し、完了したら停止する。次のタスクに自動的に進んではいけない

パラメータとして指定されたプロジェクトルートとタスクファイルに基づいて、以下を実行する

### 1. 実装コンテキストをロードして分析する

#### 読み込むべきドキュメント（優先順）

1. 最優先: `@agent_skills/struts-to-jsf-migration/principles/` 配下のすべての原則ドキュメントでマイグレーションルール、アーキテクチャ標準、セキュリティ標準、マッピング規則を確認する
   * [architecture.md](../principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
   * [security.md](../principles/security.md) - セキュリティ標準
   * [common_rules.md](../principles/common_rules.md) - マイグレーション共通ルール、マッピング規則

2. フレームワーク仕様（該当する場合）: `@agent_skills/struts-to-jsf-migration/frameworks/` 配下に格納されたフレームワーク固有のSPECやサンプルコードを確認する
   * 特定のフレームワーク（ライブラリ、ツール等）の使用方法、設計パターン、実装例を参照する
   * 詳細設計やコード生成時に、フレームワーク仕様に従った実装を行う

3. 必須: 指定されたタスクファイルで完全なタスクリストと実行計画を確認する
   * タスクの「参照SPEC」はMarkdownリンク形式で記述されている（クリック可能）
   * リンク先のSPECファイルと指定されたセクションを必ず参照する

4. 必須: `{project_root}/specs/baseline/basic_design/architecture_design.md` で以下を確認する
   * 技術スタック（言語、バージョン、フレームワーク、ライブラリ）
   * アーキテクチャパターンとレイヤー構成
   * パッケージ構造と命名規則
   * デザインパターン、トランザクション戦略、並行制御
   * ログ戦略、エラーハンドリング、セキュリティ
   * テスト戦略（テストフレームワーク、カバレッジ目標、テスト方針）
   * セッション管理方針（ViewScoped、Flash Scope、Session Scope）
   * コード生成時は、ここで定義された技術スタックを厳密に遵守すること

5. 必須: `{project_root}/specs/baseline/basic_design/requirements.md` で機能要件と成功基準を確認する

6. 必須: `{project_root}/specs/baseline/basic_design/functional_design.md` でシステム全体の機能設計、画面一覧、画面遷移図を確認する

7. 必須: `{project_root}/specs/baseline/basic_design/detailed_design.md` で共通処理、JPAエンティティ、Serviceの詳細設計を確認する（存在する場合）

8. 必須: `{project_root}/specs/baseline/detailed_design/screen/*/functional_design.md` で画面固有のManaged Bean設計、Service設計、データアクセス設計を確認する

9. 必須: `{project_root}/specs/baseline/detailed_design/screen/*/detailed_design.md` で画面固有の詳細設計を確認する（存在する場合）

10. 存在する場合: `{project_root}/specs/baseline/basic_design/data_model.md` でテーブル定義とERDを確認する

11. 存在する場合: `{project_root}/specs/baseline/basic_design/behaviors.md` でシステム全体の振る舞い、共通処理の振る舞い、受入基準を確認する

12. 存在する場合: `{project_root}/specs/baseline/detailed_design/screen/*/behaviors.md` で画面固有の受入基準とテストシナリオを確認する

13. 存在する場合: `{project_root}/specs/baseline/detailed_design/screen/*/screen_design.md` で画面レイアウト、入力項目、バリデーションを確認する

14. 存在する場合: `{project_root}/specs/baseline/basic_design/external_interface.md` で外部連携仕様とAPI仕様を確認する

15. 静的リソース: `{project_root}/resources/` フォルダの静的ファイル（画像等）を確認し、セットアップ時に適切な場所にコピーする

* 注意: `{project_root}` は、パラメータで明示的に指定されたプロジェクトルートのパスに置き換える

### 2. タスク構造を解析して抽出する

* タスク構成: セットアップ、共通機能、画面別実装、結合・テスト
* タスク依存関係: 順次実行対並列実行ルール
* タスク詳細: ID、説明、ファイルパス、並列マーカー[P]
* 実行フロー: 順序と依存関係の要件

### 3. タスク計画に従って実装を実行する

* タスクごとの実行: 次のタスクに進む前に各タスクを完了する
* セットアップタスク
  * `skip_infrastructure: true`の場合、インフラ関連タスク（DB/APサーバーのインストール等）はスキップする
  * アプリケーション固有のセットアップ（スキーマ作成、初期データ、静的リソース配置等）は実行する
  * リソース配置（画像ファイルのコピー等）を最優先で実行する
* 依存関係の尊重: 順次タスクは順番に実行、並列タスク[P]は一緒に実行可能
* TDDアプローチに従う: 対応する実装の前にテストを実行する（プロジェクトがTDDを採用している場合）
* ファイルベースの調整: 同じファイルに影響するタスクは順次実行必須
* 検証チェックポイント: 進む前に各タスクの完了を検証する

### 4. 実装実行ルール

#### プロジェクトルートの使用

全てのパス操作は、パラメータで指定されたプロジェクトルートを基準に行います

#### 技術スタックの遵守

architecture_design.mdに記載された技術スタックを厳密に遵守する

* プログラミング言語: architecture_design.mdを確認する
* フレームワーク: architecture_design.mdを確認する
* ライブラリとバージョン: architecture_design.mdを確認する
* テストフレームワーク: architecture_design.mdを確認する
* データベース: architecture_design.mdを確認する
* 記載されたバージョン番号を正確に使用する: 異なるバージョンを使用しない

#### ルールの遵守

`@agent_skills/struts-to-jsf-migration/principles/` 配下の原則ドキュメントに記載された共通ルールを全ての実装で遵守する
* テストカバレッジ基準、アーキテクチャパターン、コーディング規約に従う
* 品質基準、セキュリティ要件、パフォーマンス基準を満たす
* マイグレーション特有のマッピング規則（Struts → JSF）に従う
* プロジェクト固有のルール（`{project_root}/principles/`）がある場合は、それも併せて遵守する

#### SPEC修正の制約

コード生成時におけるSPECの修正には厳格な制約がある

* ✅ 修正可能なSPEC
  * `detailed_design.md`（詳細設計書）のみ修正可能
  * 実装時に発見した設計の不整合の修正
  * クラス設計の改善やメソッドシグネチャの調整
  * 実装詳細レベルの変更

* ❌ 修正禁止のSPEC
以下の上位SPECは絶対に修正しないこと
* `requirements.md` - 要件定義
* `architecture_design.md` - アーキテクチャ設計
* `functional_design.md` - 機能設計
* `data_model.md` - データモデル
* `behaviors.md` - 振る舞い仕様
* `screen_design.md` - 画面設計
* `external_interface.md` - 外部インターフェース仕様
* その他すべての上位SPEC

* 対応方針
  * 上位SPECは参照のみに使用し、変更しない
  * 実装詳細の調整が必要な場合は`detailed_design.md`で対応する
  * 上位仕様との矛盾を発見した場合は、実装を停止しユーザーに報告する

#### セットアップ優先

プロジェクト構造、依存関係、構成を初期化する
* 静的リソースの配置: 必要な画像やファイルを適切な場所にコピーする
* データベーススキーマのセットアップ

注意: インフラセットアップのスキップ
* `skip_infrastructure: true` パラメータが指定された場合、以下のインフラ関連タスクはスキップする
  * データベースサーバーのインストール・起動
  * アプリケーションサーバーのインストール・設定
  * ミドルウェアのセットアップ
* スキップ可能な理由: 開発環境がすでに構築済みの場合や、CI/CD環境で実行する場合
* 実行するタスク: データベーススキーマ作成、初期データ投入、静的リソース配置などのアプリケーション固有のセットアップは実行する

#### コードの前にテスト

契約、エンティティ、結合シナリオのテストを作成する（TDDの場合）

#### コア開発

Entity、Service、Managed Bean、Facelets XHTMLを実装する

#### 結合作業

データベース接続、ミドルウェア、ロギング、外部サービス

#### 仕上げと検証

ユニットテスト、パフォーマンス最適化、ドキュメント

### 5. 単体テスト生成ガイドライン（タスク粒度内のテスト）

重要: このフェーズで生成するのはタスク粒度内の単体テストである

#### 5.1 基本方針

* テストスコープ: タスクの粒度内
  * タスク分解で定義された1つのタスク（例: SCREEN_001_PersonList）に含まれるコンポーネントをテスト
  * タスク内のコンポーネント間は実際の連携でテスト可能
  * タスク外の依存関係はモックを使用
  
* モック使用の判断基準:
  * 同じタスク内のコンポーネント → モック不要（実際の連携をテスト）
    * 例: PersonListBean → PersonService → PersonDao （同じタスク内）
  * タスク外の依存関係 → モックを使用
    * 例: PersonService が ExternalService に依存する場合、ExternalService はモック
    * 例: EntityManager、外部APIクライアント等はモック

* テストフレームワーク: architecture_design.mdで指定されたフレームワークを使用する（JUnit 5 + Mockito等）
* テストカバレッジ: architecture_design.mdの目標値を遵守する

#### 5.2 テストケース設計

* detailed_design/screen/配下の各画面のbehaviors.md（単体テスト用）の各Given-When-Thenシナリオから対応するテストケースを実装する
* detailed_design/screen/配下の各画面のdetailed_design.mdの各メソッドシグネチャに対して、以下のテストを作成する：
  * 正常系テスト（期待する戻り値が返されるか）
  * 異常系テスト（例外が適切にスローされるか）
  * 境界値テスト（null、空文字列、最大値、最小値等）
  * バリデーションテスト

#### 5.3 テストの例

ケース1: タスク内の実際の連携をテスト
```java
@ExtendWith(MockitoExtension.class)
class PersonListBeanTest {
    @Mock
    private EntityManager em; // タスク外の依存関係はモック
    
    private PersonDao personDao;
    private PersonService personService;
    private PersonListBean personListBean;
    
    @BeforeEach
    void setUp() {
        personDao = new PersonDao(em); // タスク内のコンポーネントは実インスタンス
        personService = new PersonService(personDao); // 実際の連携をテスト
        personListBean = new PersonListBean(personService);
    }
    
    @Test
    void testDeletePerson_正常系() {
        // Given
        Long personId = 1L;
        Person person = new Person();
        person.setPersonId(personId);
        when(em.find(Person.class, personId)).thenReturn(person);
        
        // When
        String result = personListBean.deletePerson(personId); // Bean→Service→Daoの実際の連携
        
        // Then
        assertNotNull(result);
        verify(em, times(1)).remove(person);
    }
}
```

ケース2: タスク外の依存関係をモック
```java
@ExtendWith(MockitoExtension.class)
class PersonServiceTest {
    @Mock
    private ExternalApiClient externalApiClient; // 別タスクのクライアントはモック
    
    private PersonService personService;
    
    @Test
    void testNotifyExternalSystem() {
        // Given
        doNothing().when(externalApiClient).notify(any());
        
        // When
        personService.notifyExternalSystem(personId);
        
        // Then
        verify(externalApiClient, times(1)).notify(any());
    }
}
```

#### 5.4 テストデータ

* テストデータはdetailed_design/screen/配下のbehaviors.md（単体テスト用）やbasic_design/screen_design.mdの具体例を参考に作成する
* テストデータは各テストケース内でセットアップする（テストの独立性を保つ）

---

## コンポーネント別の参照ドキュメント優先度と使用方法

### 重要: 全てのコンポーネント生成時の共通確認事項

architecture_design.mdを参照して以下を確認すること
* 言語・バージョン
* パッケージ配置
* 命名規則
* アノテーション
* ログ出力

### Entity生成時

* 技術スタック: architecture_design.mdでJakarta Persistence（JPA）バージョンを確認する
* 第一参照: data_model.md
  * テーブル構造、カラム定義、データ型、制約条件を確認する
  * エンティティ間の関係を確認する
  * 検証ルールを確認する
* 第二参照: functional_design.md
  * クラス設計、属性名、メソッドシグネチャを確認する
  * ビジネスロジックメソッドがあれば実装する
* 並行制御: architecture_design.mdで楽観的ロックの使用を確認する

### Service層生成時

* 技術スタック: architecture_design.mdでJakarta CDI, Transactionsバージョンを確認する
* スコープ: architecture_design.mdで適切なスコープを確認する（通常は`@RequestScoped`）
* トランザクション: architecture_design.mdで@Transactionalの使用方法を確認する
* 第一参照: screen/配下の該当画面のfunctional_design.md
  * Serviceクラスのメソッドシグネチャ、ビジネスロジック、処理フローを確認する
  * トランザクション境界、例外ハンドリング、バリデーションロジックを確認する
* 第二参照: screen/配下の該当画面のbehaviors.md
  * 各メソッドの振る舞い、業務ルール、制約条件を確認する
  * エッジケースや異常系の処理を確認する
* 例外処理: architecture_design.mdに従う

### Managed Bean生成時

* 技術スタック: architecture_design.mdでJakarta Faces（JSF）バージョンとCDIを確認する
* スコープ: functional_design.mdで指定されたスコープを確認する（通常は`@ViewScoped`）
* 第一参照: screen/配下の該当画面のfunctional_design.md
  * Managed Bean設計、プロパティ、アクションメソッドを確認する
  * 画面遷移、Flash Scopeでのデータ受け渡しを確認する
* 第二参照: screen/配下の該当画面のbehaviors.md
  * 画面の振る舞い、初期表示、ボタンクリック時の処理を確認する
  * バリデーションエラー時の処理、エラーメッセージ表示を確認する
* 第三参照: screen/配下の該当画面のscreen_design.md
  * 画面レイアウト、入力項目、バリデーションルールを確認する

### Facelets XHTML生成時

* 技術スタック: architecture_design.mdでJakarta Faces（JSF）バージョンを確認する
* 第一参照: screen/配下の該当画面のscreen_design.md
  * 画面レイアウト（テーブル、フォーム、ボタン等）を確認する
  * 表示項目、入力項目、バリデーションルールを確認する
* 第二参照: screen/配下の該当画面のfunctional_design.md
  * Managed Beanとのバインディング（`#{beanName.property}`）を確認する
  * アクションメソッドの呼び出しを確認する
* JSFコンポーネント:
  * `<h:form>` - メインフォーム
  * `<h:dataTable>` - データテーブル（一覧表示）
  * `<h:inputText>` - テキスト入力
  * `<h:commandButton>` - ボタン
  * `<h:outputText>` - テキスト表示
  * `<h:messages>` - エラーメッセージ表示

### DTO/Model生成時

* 第一参照: screen/配下の該当画面のfunctional_design.md
  * DTO構造、フィールド名、データ型を確認する
  * バリデーションアノテーションを確認する
* 第二参照: data_model.md
  * Entityとの対応関係、変換ロジックを確認する

### Filter/Interceptor生成時

* 技術スタック: architecture_design.mdでJakarta Servlet, Jakarta Facesを確認する
* 第一参照: architecture_design.md
  * 認証フィルターの設計、処理フロー、エラーハンドリングを確認する
  * セッション管理、ロギングインターセプターの仕様を確認する
* 第二参照: functional_design.md
  * セキュリティ要件、認証・認可の仕様を確認する

### 外部連携コンポーネント生成時

* 技術スタック: architecture_design.mdで外部API連携クライアントの使用を確認する
* 第一参照: external_interface.md
  * API仕様を確認する
  * OpenAPI YAMLファイルがあれば、スキーマ定義、認証方式、エラーレスポンスを確認する
  * 通信プロトコル、タイムアウト設定、リトライポリシーを確認する
* 第二参照: functional_design.md
  * 連携クラスの設計、メソッド名、エラーハンドリングを確認する

---

## 進捗追跡とエラーハンドリング

* 完了した各タスク後に進捗を報告する
* 順次実行タスクが失敗した場合は実行を停止する
* 並列実行タスク[P]の場合、成功したタスクを続行し、失敗したタスクを報告する
* デバッグのためのコンテキスト付きの明確なエラーメッセージを提供する
* 実装を続行できない場合は次の手順を提案する
* 重要: 完了したタスクについては、タスクファイルでタスクを[X]としてマークする

---

## 完了検証

* ルールドキュメントのルールと品質基準が遵守されていることを確認する
* 全ての必須タスクが完了していることを確認する
* 実装された機能が要件定義と一致することを確認する
* テストがパスし、カバレッジが要件を満たすことを検証する
* 実装がアーキテクチャ設計に従っていることを確認する
* クラス設計が機能設計仕様と一致することを検証する
* SPECとのトレーサビリティ検証
  * screen/配下の各画面のbehaviors.mdの受入基準（Given-When-Then）が全てテストケースでカバーされていることを確認する
  * screen/配下の各画面のfunctional_design.mdで定義された全てのManaged Bean、Service、クラス、メソッドが実装されていることを確認する
  * data_model.mdで定義された全ての制約条件（NOT NULL, UNIQUE, FK等）が実装されていることを確認する
  * external_interface.mdで定義された全てのAPI仕様が実装されていることを確認する
  * 静的リソースが正しく配置されていることを確認する
  * 完了した作業の要約とともに最終ステータスを報告する
  * このタスクファイルのタスクがすべて完了したら、ここで停止する

---

## JSF特有の実装要件

このスキルは、JSFアプリケーションの特性に対応した実装を行う

### Managed Bean実装

* アノテーション: `@Named` + スコープアノテーション（`@ViewScoped`, `@RequestScoped`, `@SessionScoped`）
* `Serializable`実装: スコープによってはシリアライズが必要
* CDI依存性注入: `@Inject`でServiceを注入
* ライフサイクル: `@PostConstruct`で初期化処理
* Flash Scope: 画面間のデータ受け渡しに使用
* FacesMessage: エラーメッセージやインフォメッセージの表示

### Facelets XHTML実装

* XMLネームスペース: `xmlns:h="jakarta.faces.html"`, `xmlns:f="jakarta.faces.core"`
* Unified EL: `#{beanName.property}`, `#{beanName.actionMethod()}`
* データテーブル: `<h:dataTable>` でリスト表示
* フォーム: `<h:form>`, `<h:inputText>`, `<h:commandButton>`
* バリデーション: Bean Validationアノテーションと連携
* メッセージ表示: `<h:messages>`, `<h:message>`

### セッション管理

* ViewScoped: 画面表示中のみ有効（最も一般的）
* Flash Scope: 画面遷移時の1回限りのデータ受け渡し
* Session Scope: セッション全体で共有するデータ（使用は最小限に）
* RequestScoped: リクエストごとに破棄（ステートレス処理）

### データソース設定

* JNDI名の設定: architecture_design.mdのデータソース設定セクションで確認する
* 移行元で使用していたJNDI名を継続使用する
* persistence.xmlで同じJNDI名を設定する

### トランザクション管理

* Serviceクラスに`@Transactional`を適用
* メソッドレベルでトランザクション境界を制御
* 例外発生時は自動的にロールバック

---

## 重要な注意事項

### タスクの実行範囲

* このインストラクションは、タスクファイルに完全なタスク分解が存在することを前提とする
* タスクが不完全または欠落している場合は、まず `task_breakdown.md` インストラクションを使用してタスクリストを生成する
* 指定されたタスクファイルのタスクのみを実行する。他のタスクファイル（例: 次の機能のタスク）に自動的に進んではいけない
* タスクは分業の単位である。1つのタスクが完了したら、次のタスクに進む前にユーザーの確認を待つ

### JSF特有の注意点

* 画面（UI）が含まれるため、Managed Bean と Facelets XHTML の実装を行う
* 画面遷移は暗黙的ナビゲーション（戻り値が画面ID）または faces-config.xml で管理
* セッション管理（ViewScoped、Flash Scope、Session Scope）を考慮
* Bean Validation、JSFライフサイクル、Unified ELを活用
* Facelets XHTMLはXML形式で、Jakarta Faces名前空間を使用

### プロジェクトルートの扱い

* `{project_root}` は、パラメータで明示的に指定されたパスに置き換える
* 相対パスでも絶対パスでも構わない
* 全てのファイル操作は、このプロジェクトルートを基準に行う

---

## マイグレーション特有の考慮事項

### 1. データソース設定の継続

* Strutsで使用していたJNDIデータソースをそのまま使用する
* architecture_design.mdに記載されているJNDI名を確認する
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

## 参考資料

* [マイグレーション原則](../principles/) - マイグレーションルール、アーキテクチャ標準、セキュリティ標準、マッピング規則
  * [architecture.md](../principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](../principles/security.md) - セキュリティ標準
  * [common_rules.md](../principles/common_rules.md) - 共通ルール、マッピング規則
* [リバースエンジニアリングインストラクション](reverse_engineering.md) - ステップ1: 既存コード分析
* [タスク分解インストラクション](task_breakdown.md) - ステップ2: タスク分解
* [詳細設計インストラクション](detailed_design.md) - ステップ3: 詳細設計
