# 基本設計インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここに仕様書ディレクトリのパスを入力"
```

* 例1: ベースライン（初回リリース版）
```yaml
project_root: "projects/sdd/bookstore/back-office-api-sdd"
spec_directory: "projects/sdd/bookstore/back-office-api-sdd/specs/baseline"
```

* 例2: 拡張機能（エンハンスメント）
```yaml
project_root: "projects/sdd/bookstore/back-office-api-sdd"
spec_directory: "projects/sdd/bookstore/back-office-api-sdd/specs/enhancements/202512_inventory_alert"
```

注意
* パス区切りはOS環境に応じて調整する（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換える
* 以降、`{spec_directory}` と表記されている箇所は、上記で設定した値に置き換える

---

## 概要

このインストラクションは、Jakarta EE プロジェクトの仕様書をゼロから作成するためのものである

重要な方針
* テンプレートを使用して、所定のフォルダに展開し、ひな形化された状態をまず作る
* AIとユーザーが対話しながら、各仕様書の中身を埋めていく
* 既存資料（EXCEL、Word、PDF等）がある場合は、それを読み込んでMarkdown形式に変換する
* 既存資料がない場合は、テンプレートから対話的に作成する
* requirements.md（要件定義書）は所与とする（既に存在している前提）
* システム全体の仕様書（system/配下）とAPI単位の仕様書（api/配下）を作成する

作成する仕様書

system/配下（システム全体、共通処理、ドメインモデル）:
* architecture_design.md - アーキテクチャ設計書（プロジェクト固有のアーキテクチャ）
* functional_design.md - 機能設計書（システム全体の機能概要、共通サービス、ドメインモデルの機能設計）
* detailed_design.md - 詳細設計書（共通処理、ドメインモデル（JPAエンティティ）、共通サービスの詳細設計）
* data_model.md - データモデル仕様書（ERD、テーブル定義、リレーション）
* behaviors.md - 振る舞い仕様書（システム全体の振る舞い、共通処理の振る舞い、受入基準）
* external_interface.md - 外部インターフェース仕様書（外部API連携、外部システムとの接続）

api/配下（API単位、API固有の設計）:
* api/{api_id}/functional_design.md - API機能設計書（API固有のエンドポイント、リクエスト/レスポンス、ビジネスルール）
* api/{api_id}/detailed_design.md - API詳細設計書（Resource、DTO、API特有のビジネスロジックの詳細設計）
* api/{api_id}/behaviors.md - API振る舞い仕様書（API固有の振る舞い、テストシナリオ、受入基準）

---

## 1. 前提条件の確認

### 1.1 requirements.mdの確認

{spec_directory}/system/requirements.md が存在することを確認する

* 存在しない場合は、ユーザーに「requirements.mdが見つかりません。先に要件定義書を作成してください」と伝える

### 1.2 Agent Skillsルールの確認

* @agent_skills/jakarta-ee-api-base/principles/ 配下の原則ドキュメントを読み込み、共通ルールを遵守すること
  * 仕様駆動開発の基本ルール
  * Markdownフォーマット規約
  * タスクの完遂責任

---

## 2. テンプレートの展開

### 2.1 システムレベル仕様書の展開

@agent_skills/jakarta-ee-api-base/templates/ 配下のテンプレートファイルを {spec_directory}/system/ にコピーする

コピー対象ファイル（system用テンプレート）:
* architecture_design.md - アーキテクチャ設計書
* functional_design.md - システム機能設計書（システム全体、共通サービス、ドメインモデル）
* data_model.md - データモデル仕様書（ERD、テーブル定義）
* behaviors.md - システム振る舞い仕様書（システム全体、共通処理の振る舞い）
* external_interface.md - 外部インターフェース仕様書

注意
* requirements.mdは既に存在しているため、コピーしない
* system_detailed_design.md → system/detailed_design.md としてコピー（詳細設計フェーズで使用）
* 既にファイルが存在する場合は、ユーザーに「上書きしますか？」と確認する
* テンプレートは「ひな形」として展開する
* 既存資料（EXCEL、Word等）がある場合は、後の工程でそれらを読み込んで変換する

### 2.2 API用テンプレートの準備

API単位の仕様書は、APIを識別した後に作成します。使用するテンプレート：
* api_functional_design.md - API機能設計書テンプレート（API固有のエンドポイント、ビジネスルール）
* api_detailed_design.md - API詳細設計書テンプレート（Resource、DTO、API特有のService）
* api_behaviors.md - API振る舞い仕様書テンプレート（API固有の振る舞い、テストシナリオ）

### 2.2 ディレクトリ構造の確認

展開後のディレクトリ構造:

```
{spec_directory}/
├── system/                          # システム全体、共通処理、ドメインモデル
│   ├── requirements.md              # 所与（既存）
│   ├── architecture_design.md       # テンプレートから展開
│   ├── functional_design.md         # テンプレートから展開（システム全体、共通サービス、ドメインモデル）
│   ├── detailed_design.md           # 詳細設計フェーズで作成（共通処理、JPAエンティティ、共通サービス）
│   ├── data_model.md                # テンプレートから展開
│   ├── behaviors.md                 # テンプレートから展開（システム全体、共通処理の振る舞い）
│   └── external_interface.md        # テンプレートから展開
└── api/                             # API単位の仕様書（API固有の設計）
    └── API_XXX_yyyy/
        ├── functional_design.md     # API固有の機能設計
        ├── detailed_design.md       # 詳細設計フェーズで作成（Resource、DTO、API特有のサービス）
        └── behaviors.md             # API固有の振る舞い
```

---

## 3. 対話による仕様書作成（システムレベル）

### 3.1 requirements.mdの理解

{spec_directory}/system/requirements.md を読み込み、以下を理解する

* プロジェクト概要
* 目的と対象ユーザー
* 機能要件
* 非機能要件
* 成功基準

ユーザーに理解した内容を説明し、不明点を質問する

### 3.2 architecture_design.mdの作成

まず、既存資料の有無を確認する

質問:
* 「アーキテクチャ設計に関する既存の資料（EXCEL、Word、PDF等）はありますか？」

既存資料がある場合:
1. 資料を読み込む（ユーザーに@で添付してもらう）
2. 資料の内容をMarkdown形式に変換する
3. テンプレート構造に合わせて整形する
4. 不足している情報をユーザーに確認する

既存資料がない場合:
* {spec_directory}/system/architecture_design.md のテンプレートを開き、ユーザーと対話しながら各セクションを埋めていく

主要なセクション:
* 技術スタック
  * コアプラットフォーム（ランタイム、プラットフォーム、アプリサーバー、データベース、ビルドツール）
  * フレームワーク仕様（Jakarta EE、JAX-RS、JPA、CDI等）
  * 追加ライブラリ
* アーキテクチャ設計
  * アーキテクチャパターン（レイヤードアーキテクチャ等）
  * コンポーネントの責務
* パッケージ構造と命名規則
* トランザクション管理
* 並行制御
* エラーハンドリング戦略
* セキュリティアーキテクチャ
* テスト戦略

対話のポイント:
* 「このプロジェクトで使用する技術スタックを教えてください」
* 「Jakarta EEのどのバージョンを使用しますか？」
* 「データベースは何を使用しますか？」
* 「アーキテクチャパターンはどれを採用しますか？（レイヤードアーキテクチャ、マイクロサービス等）」
* 「認証方式は何を使用しますか？（JWT、セッション等）」

### 3.3 data_model.mdの作成

まず、既存資料の有無を確認する

質問:
* 「データモデルに関する既存の資料（EXCEL、Word、PDF等）はありますか？」
* 「テーブル定義書やER図は既にありますか？」

既存資料がある場合:
1. 資料を読み込む（ユーザーに@で添付してもらう）
2. テーブル定義、ER図、エンティティ情報をMarkdown形式に変換する
3. テンプレート構造に合わせて整形する
4. ER図がEXCELや画像形式の場合は、Mermaid記法に変換する
5. 不足している情報（データ整合性ルール等）をユーザーに確認する

既存資料がない場合:
* {spec_directory}/system/data_model.md のテンプレートを開き、ユーザーと対話しながら各セクションを埋めていく

主要なセクション:
* ER図（Mermaid）
* テーブル定義
* データ整合性ルール（制約、外部キー、カーディナリティ）
* リレーションシップ

注意: 
* data_model.mdは純粋なRDB論理設計（テーブル、カラム、制約、リレーション）のみを記述します
* JPAエンティティクラスの設計（@Entity, @Table, @Column, @ManyToOne等のアノテーション付きJavaクラス）は詳細設計フェーズで実施します
* ここでの「エンティティ」は、RDBの論理エンティティ（テーブル）を指します

対話のポイント:
* 「どのようなテーブルが必要ですか？」
* 「各テーブルのカラム（データ型、制約）を教えてください」
* 「テーブル間のリレーションシップは何ですか？（1:1、1:N、N:M）」
* 「外部キー制約はどうしますか？（CASCADE、RESTRICT等）」

重要な分界点:
* 基本設計（data_model.md）: RDB論理設計のみ（テーブル、カラム、型、制約、リレーション）
* 詳細設計（detailed_design.md）: JPAエンティティクラス設計（@Entity, @Table, @Column, @ManyToOne等のアノテーション、Javaの型、フィールド名）
* ここでの「エンティティ」: RDBの論理エンティティ（テーブル）を指します。JPAエンティティクラスは詳細設計で扱います

### 3.4 functional_design.md（system）の作成

重要: system/functional_design.mdはシステム全体の機能設計、共通サービス、ドメインモデルの機能設計を記述します。API固有の機能設計はapi/{api_id}/functional_design.mdに記述します。

まず、既存資料の有無を確認する

質問:
* 「機能設計に関する既存の資料（EXCEL、Word、PDF等）はありますか？」
* 「機能一覧、ユーザーストーリー、ビジネスルール等の資料はありますか？」

既存資料がある場合:
1. 資料を読み込む（ユーザーに@で添付してもらう）
2. 機能一覧、ユーザーストーリー、ビジネスルール等をMarkdown形式に変換する
3. テンプレート構造に合わせて整形する
4. フロー図がある場合は、Mermaid記法に変換する
5. システム全体の機能と共通処理に関する情報をsystem/functional_design.mdに記載
6. API固有の情報はapi/{api_id}/functional_design.mdに分離する準備をする
7. 不足している情報をユーザーに確認する

既存資料がない場合:
* {spec_directory}/system/functional_design.md のテンプレートを開き、ユーザーと対話しながら各セクションを埋めていく

主要なセクション（system/functional_design.md）:
* システム概要
* 共通機能設計（認証、ログ、エラーハンドリング等）
* ドメインモデルの機能設計（ビジネスルール、バリデーション、状態遷移等）
* システム全体のユーザーフロー（Mermaid）
* システム全体のデータフロー（シーケンス図 - 論理レベル）
* API一覧とリンク（各API詳細はapi/{api_id}/functional_design.mdへ）

重要な分界点:
* system/functional_design.md: システム全体の機能概要、共通サービス、ドメインモデルの機能設計
* api/{api_id}/functional_design.md: API固有のエンドポイント、リクエスト/レスポンス、ビジネスルール
* 基本設計では、論理レベルのコンポーネント（「書籍サービス」「社員データアクセス」等）またはレイヤー名（「APIレイヤー」「ビジネスロジック」等）のみを記述し、実装クラス名（BookService、EmployeeDao等）やメソッド名（findById()等）は記述しません
* シーケンス図も論理レベルで記述: `participant BookResource` → `participant 書籍リソース` または `participant APIレイヤー`
* DTO、エンティティの詳細構造、パッケージ名は記述しません（詳細設計で記述）

対話のポイント:
* 「主要な機能を教えてください」
* 「共通的な機能（認証、ログ、エラーハンドリング等）は何がありますか？」
* 「ドメインモデルのビジネスルールは何ですか？」
* 「システム全体のユーザーフローを教えてください」
* 「APIは何個ありますか？それぞれのAPIの概要を教えてください」

### 3.5 behaviors.md（system）の作成

重要: system/behaviors.mdはシステム全体の振る舞い、共通処理の振る舞い、受入基準を記述します。API固有の振る舞いはapi/{api_id}/behaviors.mdに記述します。

まず、既存資料の有無を確認する

質問:
* 「振る舞い仕様（受入基準、テストシナリオ等）に関する既存の資料（EXCEL、Word、PDF等）はありますか？」
* 「テスト仕様書やシナリオ一覧は既にありますか？」

既存資料がある場合:
1. 資料を読み込む（ユーザーに@で添付してもらう）
2. テストシナリオ、受入基準、エラーケース等をMarkdown形式に変換する
3. テンプレート構造（Given-When-Then形式）に合わせて整形する
4. システム全体の振る舞いと共通処理に関する情報をsystem/behaviors.mdに記載
5. API固有の情報はapi/{api_id}/behaviors.mdに分離する準備をする
6. 不足している情報をユーザーに確認する

既存資料がない場合:
* {spec_directory}/system/behaviors.md のテンプレートを開き、ユーザーと対話しながら各セクションを埋めていく

主要なセクション（system/behaviors.md）:
* システム全体の振る舞い概要
* 共通処理の振る舞い（認証、ログ、エラーハンドリング、トランザクション等）
* ドメインモデルの振る舞い（ビジネスルール、バリデーション、状態遷移等）
* システム全体のシナリオ（Given-When-Then形式）
* 共通的なエラーケース
* API一覧とリンク（各API詳細はapi/{api_id}/behaviors.mdへ）

対話のポイント:
* 「システム全体の振る舞いを教えてください」
* 「共通処理（認証、エラーハンドリング等）の振る舞いをGiven-When-Then形式で教えてください」
* 「ドメインモデルのビジネスルールの振る舞いは何ですか？」
* 「共通的なエラーケースは何がありますか？」
* 「エラーメッセージはどう表示しますか？」

### 3.6 external_interface.mdの作成

まず、既存資料の有無を確認する

質問:
* 「外部インターフェースに関する既存の資料（EXCEL、Word、PDF等）はありますか？」
* 「本システムが呼び出す外部システムの一覧、OpenAPI仕様書等の資料はありますか？」

既存資料がある場合:
1. 資料を読み込む（ユーザーに@で添付してもらう）
2. 外部システム連携一覧、外部APIの仕様（エンドポイント、リクエスト/レスポンス構造等）をMarkdown形式に変換する
3. テンプレート構造に合わせて整形する
4. システム構成図がある場合は、Mermaid記法に変換する
5. 不足している情報（認証方式、ベースURL、エラーレスポンス等）をユーザーに確認する

注意:
* 外部インターフェース仕様書は**本システムが呼び出す外部システムのAPI仕様**のみを記載する
* データベース接続情報、本システムの実装クラス、本システムが公開するAPI仕様は記載しない

既存資料がない場合:
* {spec_directory}/system/external_interface.md のテンプレートを開き、ユーザーと対話しながら各セクションを埋めていく

主要なセクション:
* 外部システム連携一覧（本システムが呼び出す外部システムのリスト）
* 外部システムが提供するAPI仕様
* OpenAPI仕様書への参照（ある場合）

対話のポイント:
* 「本システムが呼び出す外部システムはありますか？」
* 「外部システムのAPI仕様書（OpenAPI YAML等）は入手できますか？」
* 「外部システムのベースURLは何ですか？」
* 「認証方式は何ですか？（なし、API Key、OAuth 2.0、JWT Bearer Token等）」
* 「各エンドポイントのリクエスト/レスポンス構造を教えてください」

注意
* 外部連携が不要な場合は、「該当なし」として記載し、このセクションをスキップする

---

## 4. API単位の仕様書作成

### 4.1 APIの抽出

functional_design.mdとrequirements.mdから、実装が必要なAPIを抽出する

例:
* API_001_auth - 認証API
* API_002_books - 書籍管理API
* API_003_stocks - 在庫管理API

### 4.2 API単位ディレクトリの作成とテンプレート展開

各APIごとに {spec_directory}/api/{api_id}/ ディレクトリを作成し、API用テンプレートをコピーする

コピー対象ファイル（API用テンプレート）:
* api_functional_design.md → api/{api_id}/functional_design.md
* api_behaviors.md → api/{api_id}/behaviors.md

注意: api_detailed_design.mdは詳細設計フェーズで使用

例:
```
{spec_directory}/api/
├── API_001_auth/
│   ├── functional_design.md     # api_functional_design.mdからコピー
│   └── behaviors.md             # api_behaviors.mdからコピー
├── API_002_books/
│   ├── functional_design.md
│   └── behaviors.md
└── API_003_stocks/
    ├── functional_design.md
    └── behaviors.md
```

### 4.3 API functional_design.md（api/{api_id}）の作成

重要: api/{api_id}/functional_design.mdはAPI固有のエンドポイント、リクエスト/レスポンス、ビジネスルールを記述します。システム全体の機能設計はsystem/functional_design.mdに記述します。

各APIごとに、既存資料の有無を確認する

質問:
* 「{api_id}のAPI仕様に関する既存の資料（EXCEL、Word、PDF、OpenAPI YAML等）はありますか？」
* 「エンドポイント一覧、リクエスト/レスポンス定義等の資料はありますか？」

既存資料がある場合:
1. 資料を読み込む（ユーザーに@で添付してもらう）
2. API固有のエンドポイント仕様、リクエスト/レスポンス定義、ビジネスルール等をMarkdown形式に変換する
3. OpenAPI (YAML)形式の場合は、Markdownテーブルとコードブロックに変換する
4. システム全体や共通処理に関する情報は除外し、API固有の情報のみに絞る
5. 不足している情報（ビジネスロジック、エラーハンドリング等）をユーザーに確認する

重要な分界点:
* system/functional_design.md: システム全体、共通サービス、ドメインモデルの機能設計
* api/{api_id}/functional_design.md: API固有のエンドポイント、リクエスト/レスポンス構造、ビジネスロジック（論理レベル）
* api/{api_id}/detailed_design.md: Resourceクラス、API固有のServiceクラス、DTOクラス、メソッド定義、アノテーション
* 注意: 既存資料にクラス設計（Javaクラス、DTO等）が含まれている場合でも、基本設計書には含めず、詳細設計フェーズで記述します

既存資料がない場合:
* ユーザーと対話しながら各APIの functional_design.md を作成する

主要なセクション（api/{api_id}/functional_design.md）:
* API概要（このAPI固有の概要）
* エンドポイント仕様（このAPIが提供するエンドポイント）
  * パス
  * HTTPメソッド
  * リクエストパラメータ
  * リクエストボディ
  * レスポンスボディ
  * ステータスコード
* ビジネスロジック（このAPI固有のビジネスルール）
* 外部API連携（このAPIが呼び出す外部API）

注意: クラス設計（Resource, DTO）は詳細設計フェーズで実施します。基本設計では記述しません。

対話のポイント:
* 「このAPIのエンドポイントを教えてください」
* 「リクエスト/レスポンスの形式を教えてください」
* 「このAPI固有のビジネスロジックは何ですか？」
* 「このAPIは外部APIを呼び出しますか？」

### 4.4 API behaviors.md（api/{api_id}）の作成

重要: api/{api_id}/behaviors.mdはAPI固有の振る舞い、テストシナリオ、受入基準を記述します。システム全体や共通処理の振る舞いはsystem/behaviors.mdに記述します。

各APIごとに、既存資料の有無を確認する

質問:
* 「{api_id}のテストシナリオや受入基準に関する既存の資料（EXCEL、Word、PDF等）はありますか？」

既存資料がある場合:
1. 資料を読み込む（ユーザーに@で添付してもらう）
2. API固有のテストシナリオ、エラーケース、受入基準等をMarkdown形式に変換する
3. テンプレート構造（Given-When-Then形式）に合わせて整形する
4. システム全体や共通処理に関する情報は除外し、API固有の情報のみに絞る
5. 不足している情報をユーザーに確認する

既存資料がない場合:
* ユーザーと対話しながら各APIの behaviors.md を作成する

主要なセクション（api/{api_id}/behaviors.md）:
* API固有のシナリオ（Given-When-Then形式）
* API固有のエラーケース
* API固有の受入基準

対話のポイント:
* 「このAPIの振る舞いをGiven-When-Then形式で教えてください」
* 「このAPI固有のエラーケースは何がありますか？」
* 「このAPIの受入基準は何ですか？」

---

## 5. 仕様書の検証

### 5.1 整合性チェック

作成した仕様書の整合性を確認する

* architecture_design.mdで定義した技術スタックと設計方針が一貫しているか
* data_model.mdで定義したテーブル/ERDが、functional_design.mdの機能要件と整合しているか
* behaviors.mdのシナリオが、functional_design.mdの機能と対応しているか

注意: 
* 基本設計の整合性チェック: 機能要件、ビジネスルール、テーブル定義の整合性
* 詳細設計の整合性チェック: クラス設計（JPAエンティティ、Dao、Service等）とテーブル定義のマッピング、メソッドシグネチャの整合性

### 5.2 不足項目の確認

各仕様書のテンプレートに記載されている全てのセクションが埋められているか確認する

* [PROJECT_NAME]、[DATE]、[STATUS]等のプレースホルダーが全て置き換えられているか
* 「該当なし」としてスキップしたセクションが明示されているか

### 5.3 Markdownフォーマット規約の確認

@agent_skills/jakarta-ee-api-base/principles/common_rules.md に記載されたMarkdownフォーマット規約に従っているか確認する

* 箇条書きはアスタリスク（`*`）を使用しているか
* ボールド（太字）を使用していないか
* 見出しレベルで構造化されているか

---

## 6. 完了報告

### 6.1 作成した仕様書の一覧

ユーザーに作成した仕様書の一覧を報告する

例:
```
以下の仕様書を作成しました：

システムレベル仕様書（システム全体、共通処理、ドメインモデル）:
* {spec_directory}/system/architecture_design.md - アーキテクチャ設計書
* {spec_directory}/system/functional_design.md - システム機能設計書
* {spec_directory}/system/data_model.md - データモデル仕様書
* {spec_directory}/system/behaviors.md - システム振る舞い仕様書
* {spec_directory}/system/external_interface.md - 外部インターフェース仕様書

注意: system/detailed_design.mdは詳細設計フェーズで作成します

API単位仕様書（API固有の設計）:
* {spec_directory}/api/API_001_auth/functional_design.md - API機能設計書
* {spec_directory}/api/API_001_auth/behaviors.md - API振る舞い仕様書
* {spec_directory}/api/API_002_books/functional_design.md
* {spec_directory}/api/API_002_books/behaviors.md
* {spec_directory}/api/API_003_stocks/functional_design.md
* {spec_directory}/api/API_003_stocks/behaviors.md

注意: api/{api_id}/detailed_design.mdは詳細設計フェーズで作成します
```

### 6.2 次のステップの案内

ユーザーに次のステップを案内する

```
次のステップ:
1. タスク分解: @agent_skills/jakarta-ee-api-base/instructions/task_breakdown.md
2. 詳細設計: @agent_skills/jakarta-ee-api-base/instructions/detailed_design.md
3. コード生成: @agent_skills/jakarta-ee-api-base/instructions/code_generation.md
```

---

## 7. systemとapiの使い分け（まとめ）

### 7.1 system配下の仕様書

システム全体、共通処理、ドメインモデルに関する仕様書を配置します。

| ファイル | 記載内容 | 作成フェーズ |
|---------|---------|-----------|
| architecture_design.md | プロジェクト固有のアーキテクチャ設計 | 基本設計 |
| functional_design.md | システム全体の機能設計、共通サービス、ドメインモデルの機能設計 | 基本設計 |
| detailed_design.md | 共通処理、JPAエンティティ、Dao、共通Serviceの詳細設計 | 詳細設計 |
| data_model.md | ERD、テーブル定義、リレーション | 基本設計 |
| behaviors.md | システム全体の振る舞い、共通処理の振る舞い、受入基準 | 基本設計 |
| external_interface.md | 外部API連携仕様 | 基本設計 |

### 7.2 api配下の仕様書

API固有の設計情報を配置します。

| ファイル | 記載内容 | 作成フェーズ |
|---------|---------|-----------|
| api/{api_id}/functional_design.md | API固有のエンドポイント、リクエスト/レスポンス、ビジネスルール | 基本設計 |
| api/{api_id}/detailed_design.md | Resource、DTO、API特有のServiceの詳細設計 | 詳細設計 |
| api/{api_id}/behaviors.md | API固有の振る舞い、テストシナリオ、受入基準 | 基本設計 |

### 7.3 使い分けの判断基準

| 設計対象 | 配置場所 | 理由 |
|---------|---------|------|
| 共通処理（認証、ログ、エラーハンドリング） | system/ | 複数のAPIで共有される |
| ドメインモデル（JPAエンティティ、Dao） | system/ | ビジネスロジックの核心 |
| 共通Service | system/ | 複数のAPIで共有されるビジネスロジック |
| システム全体の振る舞い | system/ | システム統合テストの基準 |
| API固有のエンドポイント | api/{api_id}/ | 特定のAPIにのみ関連する |
| API固有のResource、DTO | api/{api_id}/ | 特定のAPIにのみ関連する |
| API固有のビジネスロジック（Service） | api/{api_id}/ | 特定のAPIにのみ関連する（並行作業を考慮） |

---

## 8. 重要な注意事項

### 対話的アプローチ

このインストラクションは、AIとユーザーが対話しながら仕様書を作成するためのものである

* AIが一方的に仕様書を作成するのではなく、ユーザーに質問しながら進める
* ユーザーの回答を元に、仕様書の内容を埋めていく
* 不明点や矛盾があれば、必ずユーザーに確認する

### 既存資料の活用

各仕様書作成時には、必ず既存資料の有無を確認する

* 既存資料の形式: EXCEL、Word、PDF、OpenAPI YAML、画像（ER図等）、その他
* 既存資料がある場合:
  * ユーザーに@で添付してもらう
  * 資料の内容を読み込み、Markdown形式に変換する
  * テンプレート構造に合わせて整形する
  * 図表がある場合は、可能な限りMermaid記法に変換する
  * 不足している情報をユーザーに確認する
* 既存資料がない場合:
  * テンプレートを使用して、対話的に作成する

既存資料変換時の注意点:
* EXCELのテーブル定義は、Markdownテーブルに変換する
* ER図（画像）は、可能な限りMermaid ER図記法に変換する
* OpenAPI YAML仕様は、Markdownテーブルとコードブロックに変換する
* 既存資料の情報が不完全な場合は、ユーザーに追加情報を確認する
* 既存資料とテンプレート構造が異なる場合は、テンプレート構造に合わせて整形する

### テンプレートの柔軟な活用

テンプレートはあくまでひな形であり、プロジェクトの特性に応じて柔軟に変更する

* 不要なセクションは「該当なし」として記載する
* 必要に応じてセクションを追加する
* プロジェクト固有の要件を反映する

### ルールの遵守

@agent_skills/jakarta-ee-api-base/principles/ 配下の原則ドキュメントを遵守する

* 仕様駆動開発の基本ルール
* Markdownフォーマット規約
* タスクの完遂責任

### ベースラインと拡張機能の違い

ベースライン（初回リリース版）
* {spec_directory} = `{project_root}/specs/baseline`
* system配下に完全な仕様セットを作成する

拡張機能（エンハンスメント）
* {spec_directory} = `{project_root}/specs/enhancements/[拡張名]`
* system配下は必要に応じて作成する（ベースラインを参照する場合は不要）
* api配下には拡張機能固有のAPI仕様のみを作成する

---

## 参考資料

* [common_rules.md](../principles/common_rules.md) - 共通ルール
* [task_breakdown.md](task_breakdown.md) - タスク分解（次工程）
* [detailed_design.md](detailed_design.md) - 詳細設計（次工程）
* [code_generation.md](code_generation.md) - コード生成（次工程）
