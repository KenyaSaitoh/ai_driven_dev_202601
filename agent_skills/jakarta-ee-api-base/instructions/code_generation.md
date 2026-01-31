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
project_root: "projects/sdd/bookstore/back-office-api-sdd"
task_file: "projects/sdd/bookstore/back-office-api-sdd/tasks/setup.md"
skip_infrastructure: true  # setupタスク実行時のみ有効: DB/APサーバーのインストールをスキップ
```

* 例2: 機能タスクの実行
```yaml
project_root: "projects/sdd/bookstore/back-office-api-sdd"
task_file: "projects/sdd/bookstore/back-office-api-sdd/tasks/FUNC_002_books.md"
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

* @agent_skills/jakarta-ee-api-base/principles/ - Jakarta EE開発の原則、アーキテクチャ標準、品質基準、セキュリティ標準を確認する
  * このフォルダ配下の原則ドキュメントを読み込み、共通ルールを遵守すること
  * 重要: コード生成においても、ルールドキュメントに記載されたすべてのルールを遵守すること
  * 注意: Agent Skills配下のルールは全プロジェクト共通。プロジェクト固有のルールがある場合は `{project_root}/principles/` も確認すること

2. フレームワーク仕様（該当する場合）: `@agent_skills/jakarta-ee-api-base/frameworks/` 配下に格納されたフレームワーク固有のSPECやサンプルコードを確認する
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
   * コード生成時は、ここで定義された技術スタックを厳密に遵守すること

5. 必須: `{project_root}/specs/baseline/requirements/requirements.md` で機能要件と成功基準を確認する

6. 必須: `{project_root}/specs/baseline/basic_design/functional_design.md` でシステム全体の機能設計（全APIを含む）を確認する
   * これが唯一の真実の情報源（Single Source of Truth）
   * 全ての機能要件、エンドポイント仕様、ビジネスルールはここに記載されている

7. 必須: 依存タスクの詳細設計を確認する（存在する場合）
   * タスクファイルのメタデータ「依存タスク」欄を参照
   * 各依存タスクの `{project_root}/specs/baseline/detailed_design/{依存タスクID}/detailed_design.md` を確認
   * 例: JPAエンティティ、Dao、セキュリティコンポーネント等

8. 必須: 現在のタスクの詳細設計を確認する（存在する場合）
   * `{project_root}/specs/baseline/detailed_design/{task_id}/detailed_design.md`
   * 実装クラス設計、メソッドシグネチャ、アノテーション等
   * 実装クラス設計、メソッドシグネチャ、アノテーション等

9. 必須: 現在のタスクの振る舞い仕様を確認する（存在する場合）
   * `{project_root}/specs/baseline/detailed_design/{task_id}/behaviors.md`
   * メソッドレベルのテストシナリオ（Gherkin 記法で記述されている前提）

10. 存在する場合: `{project_root}/specs/baseline/basic_design/data_model.md` でテーブル定義とERDを確認する

11. 存在する場合: `{project_root}/specs/baseline/basic_design/behaviors.md` でシステム全体の振る舞い（全APIの振る舞いを含む）を確認する

12. 存在する場合: `{project_root}/specs/baseline/detailed_design/*/behaviors.md` で機能固有の受入基準とテストシナリオを確認する

13. 存在する場合: `{project_root}/specs/baseline/basic_design/external_interface.md` で外部連携仕様とAPI仕様を確認する

14. 静的リソース: `{project_root}/resources/` フォルダの静的ファイル（画像等）を確認し、セットアップ時に適切な場所にコピーする

* 注意: `{project_root}` は、パラメータで明示的に指定されたプロジェクトルートのパスに置き換える

### 2. タスク構造を解析して抽出する

* タスク構成: セットアップ、共通機能、API別実装、結合・テスト
* タスク依存関係: 順次実行対並列実行ルール
* タスク詳細: ID、説明、ファイルパス、並列マーカー[P]
* 実行フロー: 順序と依存関係の要件

### 3. タスク計画に従って実装を実行する

重要: 各タスクの実行は必ず以下の順序で完了すること

1. 本番コード生成: Entity、Dao、Service、Resource（JAX-RSエンドポイント）、DTO等の実装コードを生成
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

`@agent_skills/jakarta-ee-api-base/principles/` 配下の原則ドキュメントを遵守する。プロジェクト固有のルール（`{project_root}/principles/`）がある場合は、それも併せて遵守する。

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

Entity、Dao、Service、Resource（JAX-RSエンドポイント）、DTO等の実装コードを生成する

重要: 本番コード生成が完了したら、必ず次のステップ（単体テスト生成）に進むこと。処理を停止してはいけない。

#### 単体テスト生成（必須）

重要: 本番コード生成の直後に、必ず単体テスト生成を実行すること

* 生成した本番コード（Entity、Dao、Service、Resource、DTO等）に対応する単体テストコードを生成する
* セクション5「単体テスト生成ガイドライン」に従ってテストを実装する
* テストフレームワーク: architecture_design.mdで指定されたフレームワークを使用する（JUnit 5 + Mockito等）
* テストカバレッジ: architecture_design.mdの目標値を遵守する
* テストケース設計:
  * detailed_design/配下の各タスクのbehaviors.md（単体テスト用）の各シナリオ（Gherkin 記法）から対応するテストケースを実装
  * detailed_design/配下の各タスクのdetailed_design.mdの各メソッドシグネチャに対して、正常系、異常系、境界値、エッジケースのテストを作成
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
  * タスク分解で定義された1つのタスク（例: FUNC_002_books）に含まれるコンポーネントをテスト
  * タスク内のコンポーネント間は実際の連携でテスト可能
  * タスク外の依存関係はモックを使用
  
* モック使用の判断基準:
  * 同じタスク内のコンポーネント → モック不要（実際の連携をテスト）
    * 例: BookResource → BookService → BookDao （同じタスク内）
  * タスク外の依存関係 → モックを使用
    * 例: BookService が AuthService に依存する場合、AuthService はモック
    * 例: EntityManager、外部APIクライアント等はモック

* テストフレームワーク: architecture_design.mdで指定されたフレームワークを使用する（JUnit 5 + Mockito等）
* テストカバレッジ: architecture_design.mdの目標値を遵守する

#### 5.2 テストケース設計

* detailed_design/配下の各タスクのbehaviors.md（単体テスト用）の各シナリオ（Gherkin 記法の Given-When-Then）から対応するテストケースを実装する
* detailed_design/配下の各タスクのdetailed_design.mdの各メソッドシグネチャに対して、以下のテストを作成する：
  * 正常系テスト（期待する戻り値が返されるか）
  * 異常系テスト（例外が適切にスローされるか）
  * 境界値テスト（null、空文字列、最大値、最小値等）
  * エッジケーステスト

#### 5.3 単体テストのポイント

* タスク内の連携: タスク外の依存（EntityManager、他タスクのService等）は @Mock、タスク内の Dao/Service は実インスタンスで Given-When-Then（when(...).thenReturn(...)、メソッド呼び出し、assert）を書く
* タスク外の依存: 他タスクの Service 等は @Mock、when(...).thenReturn(...) でスタブし、対象メソッドの戻り値・例外を検証する

#### 5.4 テストデータ

* テストデータはdetailed_design/配下のbehaviors.md（単体テスト用）やbasic_design/functional_design.mdの具体例を参考に作成する
* テストデータは各テストケース内でセットアップする（テストの独立性を保つ）

---

## コンポーネント別の参照ドキュメント優先度と使用方法

### 重要: 全てのコンポーネント生成時の共通確認事項

@agent_skills/jakarta-ee-api-base/principles/architecture.md およびプロジェクトの architecture_design.md を参照し、技術スタック・パッケージ・命名規則・アノテーション・ログ方針を遵守する。

### コンポーネント別の参照先（第一参照＝主、第二参照＝補足）

| コンポーネント | 第一参照 | 第二参照 | 補足 |
|---------------|----------|----------|------|
| Entity | data_model.md（テーブル・カラム・制約・リレーション） | functional_design.md | 楽観的ロックは architecture に従う |
| Dao | detailed_design/…/detailed_design.md（メソッド・クエリ仕様） | data_model.md | スコープ・JPAは architecture に従う |
| Service | detailed_design/…/detailed_design.md（メソッド・処理フロー・トランザクション） | 該当 behaviors.md | トランザクション・例外は architecture に従う |
| Resource | detailed_design/…/detailed_design.md（エンドポイント・DTO・認証要否） | 該当 behaviors.md | 認証・セキュリティは architecture に従う |
| DTO/Response | detailed_design/…/detailed_design.md（構造・フィールド・バリデーション） | data_model.md（Entity対応） | — |
| Filter/Interceptor | architecture_design.md（認証フィルター・CORS） | functional_design.md（認証・認可仕様） | — |
| 外部連携 | external_interface.md（API仕様・OpenAPI・タイムアウト） | functional_design.md | — |

---

## 進捗と完了

* 完了したタスクはタスクファイルで [X] とする。失敗時は停止しユーザーに報告する

---

## 完了検証

* 本番コード生成と単体テスト生成の両方が完了していることを確認する
* principles および architecture_design.md に従っていること、detailed_design/behaviors.md のシナリオがテストでカバーされていること、detailed_design で定義されたクラス・メソッド・エンドポイントが実装されていることを確認する
* このタスクファイルのタスクがすべて完了したら停止する

---

## 実装要件に応じたガイド

このスキルは、SPEC（`{project_root}/specs/baseline/basic_design/architecture_design.md`）に記載された実装要件に自動的に適応する

### エンティティ実装が必要な場合

* 識別方法: data_model.mdが存在する、またはarchitecture_design.mdに「エンティティ実装」「JPA」等の記載がある

* 実装の特徴
  * エンティティを実装する
  * Dao、Service、Resourceを実装する
  * トランザクション管理を実装する
  * 必要に応じて並行制御（楽観的ロック）を実装する
  * 必要に応じて複数種類の検索実装（JPQL、Criteria API）

### 外部API連携が必要な場合

* 識別方法: external_interface.mdが存在する、またはarchitecture_design.mdに「外部API連携」「RestClient」等の記載がある

* 実装の特徴
  * プロキシ転送: 特定のResourceは外部APIへ透過的転送のみ実装する
  * 独自実装: 特定のResourceはビジネスロジックを実装する
  * RestClientを実装する
  * 必要に応じて認証基盤を実装する

* データ管理の制約
  * functional_design.mdとexternal_interface.mdを参照して、どのエンティティを実装するか判断する
  * 実装しないエンティティ（外部API管理）は、DTOとして定義のみ

---

## 重要な注意事項

### タスクの実行範囲

* このインストラクションは、タスクファイルに完全なタスク分解が存在することを前提とする
* タスクが不完全または欠落している場合は、まず `task_generation.md` インストラクションを使用してタスクリストを生成する
* 指定されたタスクファイルのタスクのみを実行する。他のタスクファイル（例: 次の機能のタスク）に自動的に進んではいけない
* タスクは分業の単位である。1つのタスクが完了したら、次のタスクに進む前にユーザーの確認を待つ

### REST API特有の注意点

* 画面（UI）は含まれないため、View/XHTMLの実装は行わない
* エンドポイントのテストにはREST AssuredまたはJAX-RS Clientを使用する
* JWT認証、CORS、HTTPステータスコードの適切な使用を考慮する
* リクエスト/レスポンスのJSON形式のバリデーションを実装する

### プロジェクトルートの扱い

* `{project_root}` は、パラメータで明示的に指定されたパスに置き換える
* 相対パスでも絶対パスでも構わない
* 全てのファイル操作は、このプロジェクトルートを基準に行う

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

コード生成タスク完了後は、@agent_skills/jakarta-ee-api-base/instructions/unit_test_execution.md に従い単体テストを実行し、動作・カバレッジ・不足ケースを確認する。必要に応じて詳細設計→コード生成→テスト実行のループを行う。
