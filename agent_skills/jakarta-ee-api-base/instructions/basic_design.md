# 基本設計インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
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

このインストラクションは、Jakarta EE プロジェクトの基本設計SPECをゼロから作成するためのものである

重要な方針
* テンプレートを使用して、所定のフォルダに展開し、ひな形化された状態をまず作る
* AIとユーザーが対話しながら、各SPECの中身を埋めていく
* 既存資料（EXCEL、Word、PDF等）がある場合は、それを読み込んでMarkdown形式に変換する
* 既存資料がない場合は、テンプレートから対話的に作成する
* requirements.md（要件定義書）は所与とする（既に存在している前提）
* 基本設計フェーズでは、システム全体を一枚岩として設計する（basic_design/配下のみ作成）
* 機能単位への分解は、次のタスク分解フェーズで実施する

作成するSPEC

basic_design/配下（システム全体の基本設計）:
* requirements.md - 要件定義書（所与、既存）
* architecture_design.md - アーキテクチャ設計書（プロジェクト全体のアーキテクチャ）
* functional_design.md - 機能設計書（システム全体の機能設計、全APIを含む）
* data_model.md - データモデル仕様書（ERD、テーブル定義、リレーション）
* behaviors.md - 振る舞い仕様書（システム全体の振る舞い、全APIの振る舞いを含む）
* external_interface.md - 外部インターフェース仕様書（外部API連携、外部システムとの接続）

注意: 
* 基本設計フェーズでは、detailed_design/フォルダは作成しない
* 詳細設計（detailed_design.md）は、タスク分解後の詳細設計フェーズで作成する
* 全ての機能をbasic_design/functional_design.mdに記載する

---

## 1. 前提条件の確認

### 1.1 requirements.mdの確認

{spec_directory}/basic_design/requirements.md が存在することを確認する

* 存在しない場合は、ユーザーに「requirements.mdが見つかりません。先に要件定義書を作成してください」と伝える

### 1.2 Agent Skillsルールの確認

* @agent_skills/jakarta-ee-api-base/principles/ 配下の原則ドキュメントを読み込み、共通ルールを遵守すること
  * 仕様駆動開発の基本ルール
  * Markdownフォーマット規約
  * タスクの完遂責任

---

## 2. テンプレートの展開

### 2.1 基本設計テンプレートの展開

@agent_skills/jakarta-ee-api-base/templates/basic_design/ 配下のテンプレートファイルを {spec_directory}/basic_design/ にコピーする

コピー対象ファイル:
* architecture_design.md - アーキテクチャ設計書
* functional_design.md - 機能設計書（システム全体、全APIを含む）
* data_model.md - データモデル仕様書（ERD、テーブル定義）
* behaviors.md - 振る舞い仕様書（システム全体、全APIの振る舞いを含む）
* external_interface.md - 外部インターフェース仕様書

注意
* requirements.mdは既に存在しているため、コピーしない
* 既にファイルが存在する場合は、ユーザーに「上書きしますか？」と確認する
* テンプレートは「ひな形」として展開する
* 既存資料（EXCEL、Word等）がある場合は、後の工程でそれらを読み込んで変換する

### 2.2 ディレクトリ構造の確認

展開後のディレクトリ構造:

```
{spec_directory}/
└── basic_design/                    # 基本設計の成果物
    ├── requirements.md              # 所与（既存）
    ├── architecture_design.md       # テンプレートから展開
    ├── functional_design.md         # テンプレートから展開（全機能を含む）
    ├── data_model.md                # テンプレートから展開
    ├── behaviors.md                 # templates/basic_design/behaviors.mdから展開（E2Eテスト用）
    └── external_interface.md        # テンプレートから展開
```

重要な方針:
* 基本設計フェーズでは、detailed_design/フォルダは作成しない
* 全ての機能を functional_design.md に記載する
* 全ての振る舞いを behaviors.md に記載する
* 機能単位への分解は、次のタスク分解フェーズで実施する

---

## 3. 対話によるSPEC作成

### 3.1 requirements.mdの理解

{spec_directory}/basic_design/requirements.md を読み込み、以下を理解する

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
* {spec_directory}/basic_design/architecture_design.md のテンプレートを開き、ユーザーと対話しながら各セクションを埋めていく

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
* {spec_directory}/basic_design/data_model.md のテンプレートを開き、ユーザーと対話しながら各セクションを埋めていく

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

### 3.4 functional_design.mdの作成

重要: basic_design/functional_design.mdにはシステム全体の機能設計を記述します。全てのAPI機能を含めます。

まず、既存資料の有無を確認する

質問:
* 「機能設計に関する既存の資料（EXCEL、Word、PDF等）はありますか？」
* 「機能一覧、ユーザーストーリー、ビジネスルール等の資料はありますか？」

既存資料がある場合:
1. 資料を読み込む（ユーザーに@で添付してもらう）
2. 機能一覧、ユーザーストーリー、ビジネスルール等をMarkdown形式に変換する
3. テンプレート構造に合わせて整形する
4. フロー図がある場合は、Mermaid記法に変換する
5. 全ての機能をbasic_design/functional_design.mdに記載
6. 不足している情報をユーザーに確認する

既存資料がない場合:
* {spec_directory}/basic_design/functional_design.md のテンプレートを開き、ユーザーと対話しながら各セクションを埋めていく

主要なセクション:
* システム概要
* 全API機能の設計（認証API、書籍管理API、注文管理API等、全てを列挙）
* 共通機能設計（認証、ログ、エラーハンドリング等）
* ドメインモデルの機能設計（ビジネスルール、バリデーション、状態遷移等）
* システム全体のユーザーフロー（Mermaid）
* システム全体のデータフロー（シーケンス図 - 論理レベル）

重要な方針:
* 全ての機能を一枚岩として記述する（機能の分類はしない）
* 基本設計では、論理レベルのコンポーネント（「書籍サービス」「社員データアクセス」等）またはレイヤー名（「APIレイヤー」「ビジネスロジック」等）のみを記述
* 実装クラス名（BookService、EmployeeDao等）やメソッド名（findById()等）は記述しない
* シーケンス図も論理レベルで記述: `participant 書籍リソース` または `participant APIレイヤー`
* DTO、エンティティの詳細構造、パッケージ名は記述しない（詳細設計で記述）

対話のポイント:
* 「主要な機能を教えてください」
* 「認証、書籍管理、注文管理など、どのような機能がありますか？」
* 「共通的な機能（認証、ログ、エラーハンドリング等）は何がありますか？」
* 「ドメインモデルのビジネスルールは何ですか？」
* 「システム全体のユーザーフローを教えてください」

### 3.5 behaviors.mdの作成

重要: basic_design/behaviors.mdにはシステム全体の振る舞いを記述します。全てのAPI機能の振る舞いを含めます。

まず、既存資料の有無を確認する

質問:
* 「振る舞い仕様（受入基準、テストシナリオ等）に関する既存の資料（EXCEL、Word、PDF等）はありますか？」
* 「テストSPECやシナリオ一覧は既にありますか？」

既存資料がある場合:
1. 資料を読み込む（ユーザーに@で添付してもらう）
2. テストシナリオ、受入基準、エラーケース等をMarkdown形式に変換する
3. テンプレート構造（Given-When-Then形式）に合わせて整形する
4. 全ての振る舞いをbasic_design/behaviors.mdに記載
5. 不足している情報をユーザーに確認する

既存資料がない場合:
* templates/basic_design/behaviors.md を {spec_directory}/basic_design/behaviors.md にコピーして展開し、ユーザーと対話しながら各セクションを埋めていく

主要なセクション:
* システム全体の振る舞い概要
* 全API機能の振る舞い（認証API、書籍管理API、注文管理API等、全てを列挙）
* 共通処理の振る舞い（認証、ログ、エラーハンドリング、トランザクション等）
* ドメインモデルの振る舞い（ビジネスルール、バリデーション、状態遷移等）
* システム全体のシナリオ（Given-When-Then形式）
* エラーケース

重要な方針:
* 全ての振る舞いを一枚岩として記述する（機能の分類はしない）
* Given-When-Then形式で記述する
* 具体的なテストケースを含める

対話のポイント:
* 「システム全体の振る舞いを教えてください」
* 「各機能機能（認証、書籍管理、注文管理等）の振る舞いをGiven-When-Then形式で教えてください」
* 「共通処理（認証、エラーハンドリング等）の振る舞いをGiven-When-Then形式で教えてください」
* 「ドメインモデルのビジネスルールの振る舞いは何ですか？」
* 「エラーケースは何がありますか？」
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
* 外部インターフェースSPECは**本システムが呼び出す外部システムのAPI仕様**のみを記載する
* データベース接続情報、本システムの実装クラス、本システムが公開するAPI仕様は記載しない

既存資料がない場合:
* {spec_directory}/basic_design/external_interface.md のテンプレートを開き、ユーザーと対話しながら各セクションを埋めていく

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

## 4. SPECの検証

### 4.1 整合性チェック

作成したSPECの整合性を確認する

* architecture_design.mdで定義した技術スタックと設計方針が一貫しているか
* data_model.mdで定義したテーブル/ERDが、functional_design.mdの機能要件と整合しているか
* behaviors.mdのシナリオが、functional_design.mdの機能と対応しているか

注意: 
* 基本設計の整合性チェック: 機能要件、ビジネスルール、テーブル定義の整合性
* 詳細設計の整合性チェック: クラス設計（JPAエンティティ、Dao、Service等）とテーブル定義のマッピング、メソッドシグネチャの整合性

### 4.2 不足項目の確認

各SPECのテンプレートに記載されている全てのセクションが埋められているか確認する

* [PROJECT_NAME]、[DATE]、[STATUS]等のプレースホルダーが全て置き換えられているか
* 「該当なし」としてスキップしたセクションが明示されているか

### 4.3 Markdownフォーマット規約の確認

@agent_skills/jakarta-ee-api-base/principles/common_rules.md に記載されたMarkdownフォーマット規約に従っているか確認する

* 箇条書きはアスタリスク（`*`）を使用しているか
* ボールド（太字）を使用していないか
* 見出しレベルで構造化されているか

---

## 5. 完了報告

### 5.1 作成したSPECの一覧

ユーザーに作成したSPECの一覧を報告する

例:
```
以下の基本設計SPECを作成しました：

{spec_directory}/basic_design/
├── requirements.md              # 所与（既存）
├── architecture_design.md       # アーキテクチャ設計書
├── functional_design.md         # 機能設計書（全機能を含む）
├── data_model.md                # データモデル仕様書
├── behaviors.md                 # 振る舞い仕様書（全振る舞いを含む）
└── external_interface.md        # 外部インターフェース仕様書

注意: 
* 詳細設計（detailed_design/）は、タスク分解後の詳細設計フェーズで作成します
* 機能単位への分解は、次のタスク分解フェーズで実施します
```

### 5.2 次のステップの案内

ユーザーに次のステップを案内する

```
次のステップ:
1. タスク分解: @agent_skills/jakarta-ee-api-base/instructions/task_breakdown.md
   - basic_design/ を分析してタスクに分解
   - 機能を依存関係に基づいてを識別
2. 詳細設計: @agent_skills/jakarta-ee-api-base/instructions/detailed_design.md
   - タスク分解の結果に基づいて detailed_design/ フォルダを作成
   - 機能（FUNC_XXX）フォルダを作成
3. コード生成: @agent_skills/jakarta-ee-api-base/instructions/code_generation.md
   - 各タスクに従って実装
```

---

## 6. 基本設計の方針（まとめ）

### 6.1 基本設計の成果物

基本設計フェーズでは、システム全体を一枚岩として設計します。

| ファイル | 記載内容 |
|---------|---------|
| requirements.md | 要件定義書（所与） |
| architecture_design.md | プロジェクト全体のアーキテクチャ設計 |
| functional_design.md | システム全体の機能設計（全APIを含む） |
| data_model.md | ERD、テーブル定義、リレーション |
| behaviors.md | システム全体の振る舞い（全APIの振る舞いを含む） |
| external_interface.md | 外部API連携仕様 |

### 6.2 基本設計の重要な方針

* 全ての機能を functional_design.md に記載する（機能の分類はしない）
* 全ての振る舞いを behaviors.md に記載する（機能の分類はしない）
* detailed_design/フォルダは作成しない
* 論理レベルで記述する（実装クラス名、メソッド名、パッケージ名は記述しない）
* 機能単位への分解は、次のタスク分解フェーズで実施する

---

## 7. 重要な注意事項

### 対話的アプローチ

このインストラクションは、AIとユーザーが対話しながらSPECを作成するためのものである

* AIが一方的にSPECを作成するのではなく、ユーザーに質問しながら進める
* ユーザーの回答を元に、SPECの内容を埋めていく
* 不明点や矛盾があれば、必ずユーザーに確認する

### 既存資料の活用

各SPEC作成時には、必ず既存資料の有無を確認する

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
* basic_design配下に完全な仕様セットを作成する

拡張機能（エンハンスメント）
* {spec_directory} = `{project_root}/specs/enhancements/[拡張名]`
* basic_design配下は必要に応じて作成する（ベースラインを参照する場合は不要）
* 拡張機能固有の仕様のみを作成する

---

## 参考資料

* [common_rules.md](../principles/common_rules.md) - 共通ルール
* [task_breakdown.md](task_breakdown.md) - タスク分解（次工程）
* [detailed_design.md](detailed_design.md) - 詳細設計（次工程）
* [code_generation.md](code_generation.md) - コード生成（次工程）
