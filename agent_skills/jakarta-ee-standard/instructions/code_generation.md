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
project_root: "projects/sdd/bookstore/back-office-api-sdd"
task_file: "projects/sdd/bookstore/back-office-api-sdd/tasks/setup_tasks.md"
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

1. 最優先: `@agent_skills/jakarta-ee-standard/principles/` 配下の共通憲章ファイルでJakarta EE開発の原則、アーキテクチャ方針、品質基準を確認する。プロジェクト固有の憲章がある場合は `{project_root}/principles/` も併せて確認する

2. 必須: 指定されたタスクファイルで完全なタスクリストと実行計画を確認する
   * タスクの「参照SPEC」はMarkdownリンク形式で記述されている（クリック可能）
   * リンク先のSPECファイルと指定されたセクションを必ず参照する

3. 必須: `{project_root}/specs/baseline/system/architecture_design.md` で以下を確認する
   * 技術スタック（言語、バージョン、フレームワーク、ライブラリ）
   * アーキテクチャパターンとレイヤー構成
   * パッケージ構造と命名規則
   * デザインパターン、トランザクション戦略、並行制御
   * ログ戦略、エラーハンドリング、セキュリティ
   * テスト戦略（テストフレームワーク、カバレッジ目標、テスト方針）
   * コード生成時は、ここで定義された技術スタックを厳密に遵守すること

4. 必須: `{project_root}/specs/baseline/system/requirements.md` で機能要件と成功基準を確認する

5. 必須: `{project_root}/specs/baseline/system/functional_design.md` でシステム全体の機能設計概要を確認する

6. 必須: `{project_root}/specs/baseline/api/*/functional_design.md` でクラス設計、メソッド、エンドポイント仕様を確認する

7. 存在する場合: `{project_root}/specs/baseline/system/data_model.md` でエンティティと関係を確認する

8. 存在する場合: `{project_root}/specs/baseline/api/*/behaviors.md` で受入基準とテストシナリオを確認する

9. 存在する場合: `{project_root}/specs/baseline/system/external_interface.md` で外部連携仕様とAPI仕様を確認する

10. 静的リソース: `{project_root}/resources/` フォルダの静的ファイル（画像等）を確認し、セットアップ時に適切な場所にコピーする

* 注意: `{project_root}` は、パラメータで明示的に指定されたプロジェクトルートのパスに置き換える

### 2. タスク構造を解析して抽出する

* タスク構成: セットアップ、共通機能、API別実装、結合・テスト
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

#### 憲章の遵守
`@agent_skills/jakarta-ee-standard/principles/` 配下の共通憲章に記載された開発原則を全ての実装で遵守する
* テストカバレッジ基準、アーキテクチャパターン、コーディング規約に従う
* 品質基準、セキュリティ要件、パフォーマンス基準を満たす
* プロジェクト固有の憲章（`{project_root}/principles/`）がある場合は、それも併せて遵守する

#### 仕様書修正の制約
コード生成時における仕様書の修正には厳格な制約がある

* ✅ 修正可能な仕様書
  * `detailed_design.md`（詳細設計書）のみ修正可能
  * 実装時に発見した設計の不整合の修正
  * クラス設計の改善やメソッドシグネチャの調整
  * 実装詳細レベルの変更

* ❌ 修正禁止の仕様書
以下の上位仕様書は絶対に修正しないこと
* `requirements.md` - 要件定義
* `architecture_design.md` - アーキテクチャ設計
* `functional_design.md` - 機能設計
* `data_model.md` - データモデル
* `behaviors.md` - 振る舞い仕様
* `external_interface.md` - 外部インターフェース仕様
* その他すべての上位仕様書

* 対応方針
  * 上位仕様書は参照のみに使用し、変更しない
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
Entity、Dao、Service、Resource（JAX-RSエンドポイント）を実装する

#### 結合作業
データベース接続、ミドルウェア、ロギング、外部サービス

#### 仕上げと検証
ユニットテスト、パフォーマンス最適化、ドキュメント

### 5. 単体テスト生成ガイドライン

* テストフレームワーク: architecture_design.mdで指定されたフレームワークを使用する
* テストカバレッジ: architecture_design.mdの目標値を遵守する
* api/配下の各APIのbehaviors.mdの各Given-When-Thenシナリオから対応するテストケースを抽出して実装する
* api/配下の各APIのfunctional_design.mdの各メソッドシグネチャに対して、正常系・異常系・境界値のテストを作成する
* data_model.mdのエンティティ検証ルールをテストで検証する
* テストデータはbehaviors.mdやfunctional_design.mdの具体例を参考に作成する
* モックやスタブが必要な場合は、architecture_design.mdに従う

### 6. API統合テスト生成ガイドライン

* テストフレームワーク: REST AssuredまたはJAX-RS Clientを使用してREST APIをテストする
* テストフレームワークの選定: architecture_design.mdを確認する
* api/配下の各APIのfunctional_design.mdのエンドポイント仕様に基づいてテストを作成する
* api/配下の各APIのbehaviors.mdのシナリオを実際のHTTPリクエストとしてテストする
* JWT認証が必要なエンドポイントについては、事前にログインしてトークンを取得する
* HTTPステータスコード、レスポンスボディ、ヘッダーの検証
* エラーケースの検証
* テストは`src/test/java/<パッケージ>/api/`配下に配置する（パッケージはarchitecture_design.mdを参照）
* ビルド時の除外設定: API統合テストは通常のビルドでは実行されず、個別に実行する設定にする
  * JUnit 5の`@Tag("integration")`アノテーションをAPI統合テストクラスに付与する
  * Gradleの`build.gradle`で、testタスクから"integration"タグを除外する設定を追加する
  * 統合テスト専用のGradleタスクを定義し、個別実行可能にする
  * テストクラス名の命名規則で識別可能にする

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

### Dao生成時

* 技術スタック: architecture_design.mdでJakarta Persistence（JPA）とCDIバージョンを確認する
* スコープ: architecture_design.mdで適切なスコープを確認する
* 第一参照: api/配下の該当APIのfunctional_design.md
  * Daoインターフェース、メソッドシグネチャ、戻り値の型を確認する
  * クエリメソッドの動作仕様を確認する
* 第二参照: data_model.md
  * SQLクエリ設計の参考
  * インデックスやパフォーマンス考慮事項を確認する

### Service層生成時

* 技術スタック: architecture_design.mdでJakarta CDI, Transactionsバージョンを確認する
* スコープ: architecture_design.mdで適切なスコープを確認する
* トランザクション: architecture_design.mdで@Transactionalの使用方法を確認する
* 第一参照: api/配下の該当APIのfunctional_design.md
  * Serviceクラスのメソッドシグネチャ、ビジネスロジック、処理フローを確認する
  * トランザクション境界、例外ハンドリング、バリデーションロジックを確認する
* 第二参照: api/配下の該当APIのbehaviors.md
  * 各メソッドの振る舞い、業務ルール、制約条件を確認する
  * エッジケースや異常系の処理を確認する
* 例外処理: architecture_design.mdに従う

### Resource（JAX-RSエンドポイント）生成時

* 技術スタック: architecture_design.mdでJAX-RS（Jakarta RESTful Web Services）バージョンを確認する
* スコープ: architecture_design.mdで適切なスコープを確認する
* 第一参照: api/配下の該当APIのfunctional_design.md
  * Resourceクラスの設計、エンドポイント仕様を確認する
  * リクエスト/レスポンス形式、ステータスコード、エラーハンドリングを確認する
  * JWT認証の要否、権限チェックを確認する
* 第二参照: api/配下の該当APIのbehaviors.md
  * 各エンドポイントの振る舞い、エラーケース、バリデーションルールを確認する
* セキュリティ: architecture_design.mdで認証の実装方法を確認する

### DTO/Response生成時

* 第一参照: api/配下の該当APIのfunctional_design.md
  * リクエストDTO、レスポンスDTOの構造、フィールド名、データ型を確認する
  * バリデーションアノテーションを確認する
* 第二参照: data_model.md
  * Entityとの対応関係、変換ロジックを確認する

### Filter/Interceptor生成時

* 技術スタック: architecture_design.mdでJakarta Servlet, JAX-RS Filtersを確認する
* 第一参照: architecture_design.md
  * 認証フィルターの設計、処理フロー、エラーハンドリングを確認する
  * CORS設定、ロギングインターセプターの仕様を確認する
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

### API統合テスト生成時（REST Assured/JAX-RS Client）

* 第一参照: api/配下の該当APIのfunctional_design.md
  * エンドポイント仕様を確認する
  * 認証要否、ステータスコード、エラーレスポンスを確認する
* 第二参照: api/配下の該当APIのbehaviors.md
  * Given-When-ThenシナリオをHTTPリクエスト/レスポンスのテストに変換する
  * エラーケースのレスポンスを確認する
* 第三参照: system/のfunctional_design.md
  * API間の連携、データの流れ、セッション管理を確認する

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

* 憲章の原則と品質基準が遵守されていることを確認する
* 全ての必須タスクが完了していることを確認する
* 実装された機能が要件定義と一致することを確認する
* テストがパスし、カバレッジが要件を満たすことを検証する
* 実装がアーキテクチャ設計に従っていることを確認する
* クラス設計が機能設計仕様と一致することを検証する
* 仕様書とのトレーサビリティ検証
  * api/配下の各APIのbehaviors.mdの受入基準（Given-When-Then）が全てテストケースでカバーされていることを確認する
  * api/配下の各APIのfunctional_design.mdで定義された全てのエンドポイント、DTO、クラス、メソッドが実装されていることを確認する
  * data_model.mdで定義された全ての制約条件（NOT NULL, UNIQUE, FK等）が実装されていることを確認する
  * external_interface.mdで定義された全てのAPI仕様が実装されていることを確認する
  * 静的リソースが正しく配置されていることを確認する
  * 完了した作業の要約とともに最終ステータスを報告する
  * このタスクファイルのタスクがすべて完了したら、ここで停止する

---

## アーキテクチャパターン別の実装ガイド

このスキルは、仕様書（`{project_root}/specs/baseline/system/architecture_design.md`）に記載されたアーキテクチャパターンに自動的に適応する

### マイクロサービスパターンの場合

* 識別方法: architecture_design.mdに「マイクロサービス」「独立したデータ管理」「全エンティティ実装」等の記載がある

* 実装の特徴
  * 全エンティティを実装する
  * 全Dao、Service、Resourceを実装する
  * CORS設定でクロスオリジンリクエスト対応
  * 並行制御を実装する
  * 複数種類の検索実装

### BFFパターンの場合

* 識別方法: architecture_design.mdに「BFF」「Backend for Frontend」「プロキシ」「外部API統合」等の記載がある

* 実装の特徴
  * プロキシパターン: 特定のResourceは外部APIへ透過的転送のみ実装する
  * 独自実装パターン: 特定のResourceはビジネスロジックを実装する
  * エンティティ制限: 一部のエンティティのみ実装、他は外部API管理
  * 外部API連携: RestClientを実装する
  * 認証基盤を実装する

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
