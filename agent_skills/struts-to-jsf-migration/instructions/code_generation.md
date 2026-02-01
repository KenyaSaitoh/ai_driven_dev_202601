# コード生成インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
task_file: "ここに実行するタスクファイルのパスを入力"
skip_infrastructure: false  # setupタスク専用: trueの場合、インフラセットアップをスキップ
```

* 例1: setupタスクの実行
```yaml
project_root: "projects/sdd-wf/person/jsf-person-sdd"
task_file: "projects/sdd-wf/person/jsf-person-sdd/tasks/setup.md"
skip_infrastructure: true  # setupタスク実行時のみ有効: DB/APサーバーのインストールをスキップ
```

* 例2: 機能タスクの実行
```yaml
project_root: "projects/sdd-wf/person/jsf-person-sdd"
task_file: "projects/sdd-wf/person/jsf-person-sdd/tasks/FUNC_002_PersonList.md"
skip_infrastructure: false  # 機能タスクではこのパラメータは無視される
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

1. Agent Skillsルール（最優先で確認）

* @agent_skills/struts-to-jsf-migration/principles/ - マイグレーションルール、アーキテクチャ標準、品質基準、セキュリティ標準を確認する
  * このフォルダ配下の原則ドキュメントを読み込み、共通ルールを遵守すること
  * 重要: コード生成においても、ルールドキュメントに記載されたすべてのルールを遵守すること
  * 注意: Agent Skills配下のルールは全プロジェクト共通。プロジェクト固有のルールがある場合は `{project_root}/principles/` も確認すること

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

5. 必須: `{project_root}/specs/baseline/requirements/requirements.md` で機能要件と成功基準を確認する

6. 必須: `{project_root}/specs/baseline/basic_design/functional_design.md` でシステム全体の機能設計、画面一覧、画面遷移図を確認する

7. 必須: `{project_root}/specs/baseline/basic_design/detailed_design.md` で共通処理、JPAエンティティ、Serviceの詳細設計を確認する（存在する場合）

8. 必須: `{project_root}/specs/baseline/detailed_design/screen/*/functional_design.md` で機能固有のManaged Bean設計、Service設計、データアクセス設計を確認する

9. 必須: `{project_root}/specs/baseline/detailed_design/screen/*/detailed_design.md` で機能固有の詳細設計を確認する（存在する場合）

10. 存在する場合: `{project_root}/specs/baseline/basic_design/data_model.md` でテーブル定義とERDを確認する

11. 存在する場合: `{project_root}/specs/baseline/basic_design/behaviors.md` でシステム全体の振る舞い、共通処理の振る舞い、受入基準を確認する

12. 存在する場合: `{project_root}/specs/baseline/detailed_design/screen/*/behaviors.md` で機能固有の受入基準とテストシナリオを確認する

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

重要: 各タスクの実行は必ず以下の順序で完了すること

1. 本番コード生成: Entity、Service、Managed Bean、Facelets XHTML、DTO等の実装コードを生成
2. 単体テスト生成: 生成した本番コードに対応する単体テストコードを生成（必須）
3. タスク完了マーク: タスクファイルでタスクを[X]としてマーク

* タスクごとの実行: 次のタスクに進む前に各タスクを完了する（本番コード生成→単体テスト生成の両方を完了）
* setupタスク（特別なタスク）の実行時のみ:
  * `skip_infrastructure: true`の場合、インフラ関連タスク（DB/APサーバーのインストール等）はスキップする
  * `skip_infrastructure: false`の場合、すべてのセットアップを実行する
  * アプリケーション固有のセットアップ（スキーマ作成、初期データ、静的リソース配置等）は常に実行する
  * リソース配置（画像ファイルのコピー等）を最優先で実行する
* 機能タスク（FUNC_XXX）の実行時:
  * `skip_infrastructure` パラメータは無視される
  * タスクファイルに記載された実装内容に従う
  * 必ず本番コード生成の後に単体テスト生成を実行する
* 依存関係の尊重: 順次タスクは順番に実行、並列タスク[P]は一緒に実行可能
* TDDアプローチに従う: 対応する実装の前にテストを実行する（プロジェクトがTDDを採用している場合）
* ファイルベースの調整: 同じファイルに影響するタスクは順次実行必須
* 検証チェックポイント: 進む前に各タスクの完了を検証する（本番コードと単体テストの両方が生成されていることを確認）

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

`@agent_skills/struts-to-jsf-migration/principles/` 配下の原則ドキュメントを遵守する。プロジェクト固有のルール（`{project_root}/principles/`）がある場合は、それも併せて遵守する。

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

#### コア開発（本番コード生成）

Entity、Service、Managed Bean、Facelets XHTML、DTO等の実装コードを生成する

重要: 本番コード生成が完了したら、必ず次のステップ（単体テスト生成）に進むこと。処理を停止してはいけない。

#### 単体テスト生成（必須）

重要: 本番コード生成の直後に、必ず単体テスト生成を実行すること

* 生成した本番コード（Entity、Service、Managed Bean、Facelets XHTML、DTO等）に対応する単体テストコードを生成する
* セクション5「単体テスト生成ガイドライン」に従ってテストを実装する
* テストフレームワーク: JUnit 5 + Cucumber（cucumber-junit-platform-engine）。architecture_design.mdで指定されたカバレッジ等に従う
* テストカバレッジ: architecture_design.mdの目標値を遵守する
* テストケース設計:
  * detailed_design/screen/配下の各機能のbehaviors.md（単体テスト用）の Gherkin シナリオから **Cucumber .feature ファイル**（`src/test/resources/features/unit` 配下）と **Cucumber ステップ定義**（Java）を生成する
  * detailed_design/screen/配下の各機能のdetailed_design.mdの各メソッドシグネチャに対して、正常系、異常系、境界値、バリデーションのシナリオを .feature に反映し、対応するステップ定義を作成
* モック使用の判断:
  * 同じタスク内のコンポーネント → モック不要（実際の連携をテスト）
  * タスク外の依存関係 → モックを使用

実行順序の確認:
1. ✅ 本番コード生成完了
2. ✅ 単体テスト生成完了 ← ここまで完了してから処理を終了する
3. ✅ タスク完了マーク

#### 結合作業

データベース接続、ミドルウェア、ロギング、外部サービス

#### 仕上げと検証

パフォーマンス最適化、ドキュメント

注意: ユニットテストは上記「単体テスト生成」ステップで既に生成済みである

### 5. 単体テスト生成ガイドライン（タスク粒度内のテスト）

重要: このフェーズで生成するのはタスク粒度内の単体テストである

実行タイミング: セクション4「コア開発（本番コード生成）」の直後に必ず実行すること

本番コード生成が完了したら、処理を停止せずに必ずこのセクションに従って単体テストを生成すること。

#### 5.1 基本方針

* テストスコープ: タスクの粒度内
  * タスク分解で定義された1つのタスク（例: FUNC_001_PersonList）に含まれるコンポーネントをテスト
  * タスク内のコンポーネント間は実際の連携でテスト可能
  * タスク外の依存関係はモックを使用
  
* モック使用の判断基準:
  * 同じタスク内のコンポーネント → モック不要（実際の連携をテスト）
    * 例: PersonListBean → PersonService → PersonDao （同じタスク内）
  * タスク外の依存関係 → モックを使用
    * 例: PersonService が ExternalService に依存する場合、ExternalService はモック
    * 例: EntityManager、外部APIクライアント等はモック

* テストフレームワーク: JUnit 5 + Cucumber（cucumber-junit-platform-engine）。ステップ定義は `src/test/java` の適切なパッケージ（例: `...cucumber.steps`）に配置する
* テストカバレッジ: architecture_design.mdの目標値を遵守する
* feature ファイルの配置: `src/test/resources/features/unit` 配下。既存の test / integrationTest / e2eTest タスクは JUnit Platform で実行されるため、Cucumber シナリオも同じタスクに含まれる

#### 5.2 テストケース設計

* detailed_design/detailed_design/配下の各機能のbehaviors.md（単体テスト用）の Gherkin シナリオから **Cucumber .feature** と **Cucumber ステップ定義** を生成する
* detailed_design/detailed_design/配下の各機能のdetailed_design.mdの各メソッドシグネチャに対して、以下のテストを .feature とステップ定義で作成する：
  * 正常系テスト（期待する戻り値が返されるか）
  * 異常系テスト（例外が適切にスローされるか）
  * 境界値テスト（null、空文字列、最大値、最小値等）
  * バリデーションテスト

#### 5.3 単体テストのポイント

* タスク内の連携: タスク外の依存（EntityManager、他タスクのService/Client等）は @Mock、タスク内の Dao/Service/Bean は実インスタンスで Given-When-Then（when(...).thenReturn(...)、メソッド呼び出し、assert/verify）を書く
* タスク外の依存: 他タスクのクライアント等は @Mock、doNothing().when(...).メソッド(any()) 等でスタブし、対象メソッドの戻り値・副作用を検証する

#### 5.4 テストデータ

* テストデータはdetailed_design/screen/配下のbehaviors.md（単体テスト用）やbasic_design/screen_design.mdの具体例を参考に作成する
* テストデータは各テストケース内でセットアップする（テストの独立性を保つ）

---

## コンポーネント別の参照先

### 重要: 全てのコンポーネント生成時の共通確認事項

@agent_skills/struts-to-jsf-migration/principles/architecture.md およびプロジェクトの architecture_design.md を参照し、技術スタック・パッケージ・命名規則・アノテーション・ログ方針を遵守する。

### コンポーネント別の参照先（第一参照＝主、第二参照＝補足）

| コンポーネント | 第一参照 | 第二参照 | 補足 |
|---------------|----------|----------|------|
| Entity | data_model.md（テーブル・カラム・制約・リレーション） | functional_design.md | 楽観的ロックは architecture に従う |
| Service | detailed_design/…/detailed_design.md（メソッド・処理フロー・トランザクション） | 該当画面の behaviors.md | スコープ（通常 @RequestScoped）・@Transactional は architecture に従う |
| Managed Bean | detailed_design/…/detailed_design.md（プロパティ・アクションメソッド・画面遷移・Flash Scope） | 該当画面の behaviors.md、screen_design.md | スコープ（通常 @ViewScoped）は functional_design に従う |
| Facelets XHTML | 該当画面の screen_design.md（レイアウト・表示・入力・バリデーション） | detailed_design/…/detailed_design.md（Beanバインディング・アクション） | h:form, h:dataTable, h:inputText, h:commandButton, h:messages 等 |
| DTO/Model | detailed_design/…/detailed_design.md（構造・フィールド・バリデーション） | data_model.md（Entity対応） | — |
| Filter/Interceptor | architecture_design.md（認証フィルター・セッション管理） | functional_design.md（認証・認可仕様） | — |
| 外部連携 | external_interface.md（API仕様・OpenAPI・タイムアウト） | functional_design.md | — |

---

## 進捗と完了

* 完了したタスクはタスクファイルで [X] とする。失敗時は停止しユーザーに報告する

---

## 完了検証

* 本番コード生成と単体テスト生成の両方が完了していることを確認する
* principles および architecture_design.md に従っていること、detailed_design/behaviors.md のシナリオがテストでカバーされていること、detailed_design で定義された Managed Bean・Service・クラス・メソッドが実装されていることを確認する
* このタスクファイルのタスクがすべて完了したら停止する

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

## 既存コードの扱いと反復的な開発

このインストラクションは、新規生成と既存コードの改修の両方に対応する。

### 既存コードがある場合の確認

実装前に以下を確認する:

1. 対象ファイルが既に存在するか確認
2. 既存コードがある場合:
   - 既存コードを読み込んで理解する
   - 詳細設計書との差異を確認する
   - ユーザーに改修方針を確認:
     ```
     既存のファイルが見つかりました: {ファイル名}
     
     どのように進めますか？
     A. 全面的に再生成する（既存コードを上書き）
     B. 既存コードを保持して部分修正する
     C. 不足部分のみ追加する
     D. 既存コードを確認してから判断する
     ```

### 新規生成 vs 改修の判断

新規生成の場合:
* ファイルが存在しない
* 詳細設計書に基づいて完全に生成
* テストコードも同時に生成

改修の場合:
* ファイルが既に存在する
* 詳細設計書との差異を確認
* 既存の良い実装は保持
* 不足部分や誤りのみ修正
* テストコードも対応して更新

### 実装と詳細設計の同期

重要原則:
* コード修正時は詳細設計書も更新する
* 詳細設計書が常に実装の真実を反映する
* 乖離が発生した場合は即座に同期する

同期が必要なケース:
* メソッドシグネチャの変更
* クラス構造の変更
* エラーハンドリングの追加
* バリデーションロジックの変更

---

## 次のステップ

コード生成タスク完了後は、@agent_skills/struts-to-jsf-migration/instructions/unit_test_execution.md に従い単体テストを実行し、動作・カバレッジ・不足ケースを確認する。必要に応じて詳細設計→コード生成→テスト実行のループを行う。

---

## 参考資料

* [マイグレーション原則](../principles/) - マイグレーションルール、アーキテクチャ標準、セキュリティ標準、マッピング規則
  * [architecture.md](../principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](../principles/security.md) - セキュリティ標準
  * [common_rules.md](../principles/common_rules.md) - 共通ルール、マッピング規則
* [リバースエンジニアリングインストラクション](reverse_engineering.md) - ステップ1: 既存コード分析
* [タスク分解インストラクション](task_breakdown.md) - ステップ2: タスク分解
* [詳細設計インストラクション](detailed_design.md) - ステップ3: 詳細設計
* [単体テスト実行インストラクション](unit_test_execution.md) - ステップ4: 単体テスト実行・評価
