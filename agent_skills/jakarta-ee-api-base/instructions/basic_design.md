# 基本設計インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
```

* 例1: ベースライン（初回リリース版）
```yaml
project_root: "projects/sdd-wf/bookstore/back-office-api-sdd"
spec_directory: "projects/sdd-wf/bookstore/back-office-api-sdd/specs/baseline"
```

* 例2: 拡張機能（エンハンスメント）
```yaml
project_root: "projects/sdd-wf/bookstore/back-office-api-sdd"
spec_directory: "projects/sdd-wf/bookstore/back-office-api-sdd/specs/enhancements/202512_inventory_alert"
```

注意
* パス区切りはOS環境に応じて調整する（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換える
* 以降、`{spec_directory}` と表記されている箇所は、上記で設定した値に置き換える

---

## 概要

このインストラクションは、Jakarta EE プロジェクトの基本設計SPECを作成・更新するためのものである

重要: このプロセスは何度も繰り返し実行することを想定しています。初回実行時はゼロからの作成、2回目以降は既存SPECの増分更新となります。

重要な方針
* テンプレートを使用して、所定のフォルダに展開し、ひな形化された状態をまず作る
* AIとユーザーが対話しながら、各SPECの中身を埋めていく
* 既存資料（EXCEL、Word、PDF等）がある場合は、それを読み込んでMarkdown形式に変換する
* 既存資料がない場合は、テンプレートから対話的に作成する
* 既存SPECの扱い（最重要）:
  * 既存の基本設計SPEC（architecture_design.md, functional_design.md, behaviors.md等）が存在する場合は、必ず以下の手順を実行する:
    1. 既存SPECファイルをすべて読み込む
    2. インプットファイル（requirements.md等）の現在の内容と前回の内容を比較し、差分を特定する（ユーザーに変更点を確認する）
    3. 差分に関連する箇所のみをSPECファイルに反映する（追加、修正、削除）
    4. 変更のない箇所は一切触らない
  * ファイルをゼロから作り直すのではなく、既存の内容を尊重して必要な部分のみを追加・修正する
  * 新規SPECファイルが必要な場合のみ、テンプレートから作成する
  * 差分更新の原則: 「変更があった部分だけを更新する」
* requirements.md（要件定義書）は所与とする（既に存在している前提）
* 基本設計は機能（ドメイン）単位でフォルダ分割する
* フォルダ構成＝実装順序となる（common/ → 各ドメインフォルダ）

作成するSPEC

requirements/配下（システム要件）:
* requirements.md - 要件定義書（所与、既存）
* behaviors.md - E2Eテスト用の振る舞い仕様書（システム全体のエンドツーエンドシナリオ）

basic_design/配下（機能単位の基本設計）:
* common/（固定フォルダ名：最優先実装する共通ドメイン）
  * architecture_design.md - アーキテクチャ設計書（プロジェクト全体のアーキテクチャ）
  * data_model.md - データモデル仕様書（共通エンティティのERD、テーブル定義、リレーション）
  * external_interface.md - 外部インターフェース仕様書（外部API連携、外部システムとの接続）
  * functional_design.md - 共通機能設計書（認証、JWT、共通Service等）
  * behaviors.md - 共通機能の振る舞い仕様書（Gherkin記法）

* {ドメイン名}/（可変フォルダ名：プロジェクト固有のドメイン）
  * functional_design.md - ドメイン機能設計書（そのドメインの機能設計）
  * behaviors.md - ドメインの振る舞い仕様書（Gherkin記法）

フォルダ構成のルール:
* common/は固定名で必ず最初に作成・実装する
* common/以外のフォルダ名は任意（プロジェクトのドメインに応じて命名）
* common/以外のフォルダはcommon/に依存する
* フォルダ構成＝実装順序（common/ → 各ドメイン）
* 基本設計フェーズでは、detailed_design/フォルダは作成しない
* 詳細設計（detailed_design/）は、詳細設計フェーズで各フォルダに対応して作成する
* 振る舞いの記法: behaviors.md に記載するシナリオは Gherkin 記法で記述する。@agent_skills/jakarta-ee-api-base/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照すること。

---

## 1. 前提条件の確認と既存SPECの読み込み

### 1.1 実行モードの判定（初回 or 更新）

最初に以下を確認して、実行モードを判定する

1. 既存SPECファイルの存在確認:
   * {spec_directory}/basic_design/ 配下に既存のSPECファイル（architecture_design.md, data_model.md, functional_design.md, behaviors.md等）が存在するか確認する

2. 実行モードの判定:
   * 既存SPECが存在しない → 初回作成モード（セクション2以降の全手順を実行）
   * 既存SPECが存在する → 増分更新モード（以下の差分更新プロセスを実行）

### 1.2 増分更新モードの場合の手順

既存SPECが存在する場合は、以下の手順で差分更新を行う

ステップ1: 既存SPECファイルの読み込み

以下のすべての既存SPECファイルを読み込む:
* {spec_directory}/basic_design/common/architecture_design.md
* {spec_directory}/basic_design/common/data_model.md
* {spec_directory}/basic_design/common/external_interface.md
* {spec_directory}/basic_design/common/functional_design.md
* {spec_directory}/basic_design/common/behaviors.md
* {spec_directory}/basic_design/{各ドメイン}/functional_design.md
* {spec_directory}/basic_design/{各ドメイン}/behaviors.md

ステップ2: インプットファイルの変更点の特定

ユーザーに以下を確認する:

質問:
* 「requirements.mdまたは既存資料に変更がありましたか？」
* 「具体的にどのような変更がありましたか？（新機能追加、機能修正、データモデル変更等）」
* 「変更箇所を教えてください（例: 新しいエンドポイント追加、テーブルのカラム追加、ビジネスルール変更等）」

ステップ3: 影響範囲の特定

変更内容から、更新が必要なSPECファイルとセクションを特定する

例:
* 新しいAPI機能追加 → functional_design.md, behaviors.mdのみ更新
* テーブルのカラム追加 → data_model.mdのみ更新
* 新しい外部API連携追加 → external_interface.mdのみ更新
* 新しいドメイン追加 → 新規ドメインフォルダ作成

ステップ4: 差分のみを更新

特定した影響範囲のSPECファイルに対してのみ、以下を実行:
1. 該当セクションの既存内容を確認
2. 変更内容を反映（追加、修正、削除）
3. 他のセクションは一切変更しない

重要: 
* 変更のない箇所は絶対に触らない
* ファイル全体を作り直さない
* 差分のみを最小限の変更で反映する

ステップ5: 整合性の確認

更新後、関連するSPECファイル間の整合性を確認する:
* data_model.mdとfunctional_design.mdの整合性
* functional_design.mdとbehaviors.mdの整合性

不整合がある場合は、ユーザーに確認して調整する

増分更新モードの場合は、以降のセクション2-3は実行せず、セクション4（SPECの検証）に進む

### 1.3 requirements.mdの確認

{spec_directory}/requirements/requirements.md が存在することを確認する

* 存在しない場合は、ユーザーに「requirements.mdが見つかりません。先に要件定義書を作成してください」と伝える

### 1.4 Agent Skillsルール（最優先で確認）

* @agent_skills/jakarta-ee-api-base/principles/ - Jakarta EE開発の原則、アーキテクチャ標準、品質基準、セキュリティ標準を確認する
  * このフォルダ配下の原則ドキュメントを読み込み、共通ルールを遵守すること
  * 重要: 基本設計においても、ルールドキュメントに記載されたすべてのルールを遵守すること
  * 注意: Agent Skills配下のルールは全プロジェクト共通。プロジェクト固有のルールがある場合は `{project_root}/principles/` も確認すること

---

## 2. テンプレートの展開とフォルダ構成（初回作成モードのみ）

注意: このセクションは初回作成モードの場合のみ実行します。増分更新モードの場合は、セクション1.2の手順に従って差分更新を行い、このセクションはスキップします。

### 2.1 common/フォルダの作成とテンプレート展開

まず、{spec_directory}/basic_design/common/ フォルダを作成し、テンプレートを展開する

@agent_skills/jakarta-ee-api-base/templates/basic_design/ 配下のテンプレートを {spec_directory}/basic_design/common/ にコピー:

* architecture_design.md - アーキテクチャ設計書（プロジェクト全体）
* data_model.md - データモデル仕様書（共通エンティティ）
* external_interface.md - 外部インターフェース仕様書
* functional_design.md - 共通機能設計書（認証、JWT等）
* behaviors.md - 共通機能の振る舞い仕様書

requirements/配下のテンプレート（E2Eテスト用）:
* behaviors.md - E2Eテスト用の振る舞い仕様書

### 2.2 ドメインフォルダの作成

ユーザーと対話しながら、プロジェクトのドメインを識別し、フォルダを作成する

質問例:
* 「このプロジェクトには、common以外にどのようなドメイン（機能単位）がありますか？」
* 「例: 注文管理、書籍管理、画像配信など」

各ドメインフォルダに以下のテンプレートを展開:
* functional_design.md - ドメイン機能設計書
* behaviors.md - ドメインの振る舞い仕様書

### 2.3 ディレクトリ構造の確認

展開後のディレクトリ構造（例）:

```
{spec_directory}/
├── requirements/
│   ├── requirements.md              # 所与（既存）
│   └── behaviors.md                 # E2Eテスト用
└── basic_design/
    ├── common/                      # 固定：共通ドメイン（最優先実装）
    │   ├── architecture_design.md
    │   ├── data_model.md
    │   ├── external_interface.md
    │   ├── functional_design.md
    │   └── behaviors.md
    ├── orders/                      # 可変：プロジェクト固有ドメイン
    │   ├── functional_design.md
    │   └── behaviors.md
    ├── books_proxy/                 # 可変：プロジェクト固有ドメイン
    │   ├── functional_design.md
    │   └── behaviors.md
    └── images/                      # 可変：プロジェクト固有ドメイン
        ├── functional_design.md
        └── behaviors.md
```

重要な原則:
* common/は固定名、他のフォルダ名はプロジェクト固有
* common/を最初に作成・実装する（他のドメインはcommonに依存）
* 基本設計フェーズでは、detailed_design/フォルダは作成しない
* フォルダ構成＝実装順序（common/ → 各ドメイン）

注意:
* 既にファイルが存在する場合は、増分更新モード（セクション1.2）に従って処理する
* テンプレートは「ひな形」として展開する
* 既存資料（EXCEL、Word等）がある場合は、後の工程でそれらを読み込んで変換する

---

## 3. 対話によるSPEC作成（初回作成モードのみ）

注意: このセクションは初回作成モードの場合のみ実行します。増分更新モードの場合は、セクション1.2の手順に従って差分更新を行い、このセクションはスキップします。

### 3.1 requirements.mdの理解

{spec_directory}/requirements/requirements.md を読み込み、以下を理解する

* プロジェクト概要
* 目的と対象ユーザー
* 機能要件
* 非機能要件
* 成功基準

ユーザーに理解した内容を説明し、不明点を質問する

### 3.2 architecture_design.mdの作成

注意: 既存のarchitecture_design.mdが存在する場合は、セクション1.2の増分更新モードに従って、変更箇所のみを更新してください。

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

注意: 既存のdata_model.mdが存在する場合は、セクション1.2の増分更新モードに従って、変更箇所のみを更新してください（例: 新しいテーブル追加、カラム追加、リレーション変更等）。

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

### 3.4 functional_design.mdの作成（common/と各ドメイン）

#### 3.4.1 common/functional_design.mdの作成

重要: common/functional_design.mdには共通機能（認証、JWT、共通Service等）を記述します。

注意: 既存のfunctional_design.mdが存在する場合は、セクション1.2の増分更新モードに従って、変更箇所のみを更新してください（例: 新しいAPI追加、ビジネスルール変更等）。

まず、既存資料の有無を確認する

質問:
* 「共通機能設計に関する既存の資料（EXCEL、Word、PDF等）はありますか？」
* 「認証、JWT、共通Service等の資料はありますか？」

既存資料がある場合:
1. 資料を読み込む（ユーザーに@で添付してもらう）
2. 共通機能、認証仕様、ビジネスルール等をMarkdown形式に変換する
3. テンプレート構造に合わせて整形する
4. common/functional_design.mdに記載
5. 不足している情報をユーザーに確認する

既存資料がない場合:
* {spec_directory}/basic_design/common/functional_design.md のテンプレートを開き、ユーザーと対話しながら各セクションを埋めていく

主要なセクション:
* システム概要
* 共通機能設計（認証、JWT、ログ、エラーハンドリング等）
* ドメインモデルの機能設計（共通エンティティのビジネスルール、バリデーション、状態遷移等）

対話のポイント:
* 「認証方式は何を使用しますか？（JWT、セッション等）」
* 「共通的な機能（認証、ログ、エラーハンドリング等）は何がありますか？」
* 「共通エンティティのビジネスルールは何ですか？」

#### 3.4.2 各ドメインのfunctional_design.mdの作成

各ドメインフォルダ（orders/, books_proxy/, images/等）のfunctional_design.mdを作成します。

注意: 既存のfunctional_design.mdが存在する場合は、セクション1.2の増分更新モードに従って、変更箇所のみを更新してください。

質問:
* 「{ドメイン名}の機能設計に関する既存の資料はありますか？」

既存資料がある場合:
1. 資料を読み込む（ユーザーに@で添付してもらう）
2. ドメイン固有の機能、API仕様、ビジネスルール等をMarkdown形式に変換する
3. テンプレート構造に合わせて整形する
4. {ドメイン名}/functional_design.mdに記載
5. 不足している情報をユーザーに確認する

既存資料がない場合:
* {spec_directory}/basic_design/{ドメイン名}/functional_design.md のテンプレートを開き、ユーザーと対話しながら各セクションを埋めていく

主要なセクション:
* ドメイン概要
* API機能の設計（エンドポイント、リクエスト/レスポンス等）
* ドメイン固有のビジネスルール
* データフロー（シーケンス図 - 論理レベル）

重要な方針:
* 基本設計では、論理レベルのコンポーネント（「注文サービス」「書籍データアクセス」等）またはレイヤー名（「APIレイヤー」「ビジネスロジック」等）のみを記述
* 実装クラス名（OrderService、BookDao等）やメソッド名（findById()等）は記述しない
* シーケンス図も論理レベルで記述: `participant 注文リソース` または `participant APIレイヤー`
* DTO、エンティティの詳細構造、パッケージ名は記述しない（詳細設計で記述）

対話のポイント:
* 「{ドメイン名}の主要な機能を教えてください」
* 「どのようなAPI機能がありますか？」
* 「ドメイン固有のビジネスルールは何ですか？」
* 「データフローを教えてください」

### 3.5 behaviors.mdの作成（common/と各ドメイン）

#### 3.5.1 common/behaviors.mdの作成

重要: common/behaviors.mdには共通機能の振る舞い（認証、JWT、エラーハンドリング等）を記述します。

注意: 既存のbehaviors.mdが存在する場合は、セクション1.2の増分更新モードに従って、変更箇所のみを更新してください（例: 新しいシナリオ追加、既存シナリオの修正等）。

まず、既存資料の有無を確認する

質問:
* 「共通機能の振る舞い仕様（受入基準、テストシナリオ等）に関する既存の資料はありますか？」

既存資料がある場合:
1. 資料を読み込む（ユーザーに@で添付してもらう）
2. テストシナリオ、受入基準、エラーケース等をMarkdown形式に変換する
3. Gherkin 記法（Feature, Scenario, Given, When, Then 等）で整形する
4. common/behaviors.mdに記載
5. 不足している情報をユーザーに確認する

既存資料がない場合:
* templates/basic_design/behaviors.md を {spec_directory}/basic_design/common/behaviors.md にコピーして展開し、ユーザーと対話しながら各セクションを埋めていく

主要なセクション:
* 共通機能の振る舞い概要
* 共通処理の振る舞い（認証、ログ、エラーハンドリング、トランザクション等）
* ドメインモデルの振る舞い（共通エンティティのビジネスルール、バリデーション、状態遷移等）
* Gherkin 記法のシナリオ（Feature, Scenario, Given, When, Then 等）
* エラーケース

対話のポイント:
* 「共通機能の振る舞いをGiven-When-Then形式で教えてください」
* 「認証処理の振る舞いは何ですか？」
* 「エラーハンドリングの振る舞いは何ですか？」
* 「エラーケースは何がありますか？」

#### 3.5.2 各ドメインのbehaviors.mdの作成

各ドメインフォルダ（orders/, books_proxy/, images/等）のbehaviors.mdを作成します。

注意: 既存のbehaviors.mdが存在する場合は、セクション1.2の増分更新モードに従って、変更箇所のみを更新してください。

質問:
* 「{ドメイン名}の振る舞い仕様に関する既存の資料はありますか？」

既存資料がある場合:
1. 資料を読み込む（ユーザーに@で添付してもらう）
2. テストシナリオ、受入基準、エラーケース等をMarkdown形式に変換する
3. Gherkin 記法で整形する
4. {ドメイン名}/behaviors.mdに記載
5. 不足している情報をユーザーに確認する

既存資料がない場合:
* templates/basic_design/behaviors.md を {spec_directory}/basic_design/{ドメイン名}/behaviors.md にコピーして展開し、ユーザーと対話しながら各セクションを埋めていく

主要なセクション:
* ドメインの振る舞い概要
* API機能の振る舞い（エンドポイント、リクエスト/レスポンス等）
* ドメイン固有のビジネスルールの振る舞い
* Gherkin 記法のシナリオ（Feature, Scenario, Given, When, Then 等）
* エラーケース

重要な方針:
* Gherkin 記法で記述する（Feature, Scenario, Given, When, Then, And, But 等のキーワードを使用）
* 具体的なテストケースを含める

対話のポイント:
* 「{ドメイン名}の振る舞いをGiven-When-Then形式で教えてください」
* 「API機能の振る舞いは何ですか？」
* 「ドメイン固有のビジネスルールの振る舞いは何ですか？」
* 「エラーケースは何がありますか？」

### 3.6 external_interface.mdの作成

注意: 既存のexternal_interface.mdが存在する場合は、セクション1.2の増分更新モードに従って、変更箇所のみを更新してください（例: 新しい外部システム追加、エンドポイント追加等）。

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
* 外部インターフェースSPECは本システムが呼び出す外部システムのAPI仕様のみを記載する
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

@agent_skills/jakarta-ee-api-base/principles/common_rules.md の「Markdownフォーマット規約」に従っているか確認する。

---

## 5. 完了報告

### 5.1 作成したSPECの一覧

ユーザーに作成したSPECの一覧を報告する

例:
```
以下の基本設計SPECを作成しました：

{spec_directory}/
├── requirements/
│   ├── requirements.md              # 所与（既存）
│   └── behaviors.md                 # E2Eテスト用
└── basic_design/
    ├── common/                      # 共通ドメイン（最優先実装）
    │   ├── architecture_design.md   # アーキテクチャ設計書
    │   ├── data_model.md            # データモデル仕様書
    │   ├── external_interface.md    # 外部インターフェース仕様書
    │   ├── functional_design.md     # 共通機能設計書
    │   └── behaviors.md             # 共通機能の振る舞い仕様書
    ├── orders/                      # 注文管理ドメイン
    │   ├── functional_design.md     # 注文機能設計書
    │   └── behaviors.md             # 注文の振る舞い仕様書
    ├── books_proxy/                 # 書籍API連携ドメイン
    │   ├── functional_design.md     # 書籍連携機能設計書
    │   └── behaviors.md             # 書籍連携の振る舞い仕様書
    └── images/                      # 画像配信ドメイン
        ├── functional_design.md     # 画像配信機能設計書
        └── behaviors.md             # 画像配信の振る舞い仕様書

注意: 
* フォルダ構成＝実装順序（common/ → 各ドメイン）
* 詳細設計（detailed_design/）は、詳細設計フェーズで各フォルダに対応して作成します
```

### 5.2 次のステップの案内

ユーザーに次のステップを案内する

```
次のステップ:
1. 詳細設計: @agent_skills/jakarta-ee-api-base/instructions/detailed_design.md
   - common/の詳細設計を作成（Entity, Dao, JWT等）
   - 各ドメインの詳細設計を作成（Resource, Service, DTO等）
2. コード生成: @agent_skills/jakarta-ee-api-base/instructions/code_generation.md
   - common/を先に実装
   - 各ドメインを実装
```

---

## 6. 基本設計の方針（まとめ）

### 6.1 基本設計の成果物

基本設計フェーズでは、機能（ドメイン）単位でフォルダ分割します。

| フォルダ | ファイル | 記載内容 |
|---------|---------|---------|
| requirements/ | requirements.md | 要件定義書（所与） |
| requirements/ | behaviors.md | E2Eテスト用の振る舞い仕様書 |
| basic_design/common/ | architecture_design.md | プロジェクト全体のアーキテクチャ設計 |
| basic_design/common/ | data_model.md | 共通エンティティのERD、テーブル定義、リレーション |
| basic_design/common/ | external_interface.md | 外部API連携仕様 |
| basic_design/common/ | functional_design.md | 共通機能設計（認証、JWT等） |
| basic_design/common/ | behaviors.md | 共通機能の振る舞い仕様書 |
| basic_design/{ドメイン}/ | functional_design.md | ドメイン機能設計 |
| basic_design/{ドメイン}/ | behaviors.md | ドメインの振る舞い仕様書 |

### 6.2 基本設計の重要な方針

* common/は固定名で必ず最初に作成・実装する
* common/以外のフォルダ名はプロジェクト固有（ドメインに応じて命名）
* フォルダ構成＝実装順序（common/ → 各ドメイン）
* detailed_design/フォルダは作成しない（詳細設計フェーズで作成）
* 論理レベルで記述する（実装クラス名、メソッド名、パッケージ名は記述しない）
* 基本設計の各フォルダが、詳細設計・コード生成の単位となる

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

@agent_skills/jakarta-ee-api-base/principles/ 配下の原則ドキュメントを遵守する。

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
* [detailed_design.md](detailed_design.md) - 詳細設計（次工程）
* [code_generation.md](code_generation.md) - コード生成（次工程）
