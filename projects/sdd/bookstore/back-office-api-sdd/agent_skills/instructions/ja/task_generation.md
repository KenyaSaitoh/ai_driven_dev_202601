# タスク生成インストラクション

## Agent Skillについて

このインストラクションは**Agent Skills標準仕様**（v1.0）に準拠しています。

**プラットフォーム非依存:**
- Claude (Anthropic)
- GitHub Copilot
- ChatGPT (OpenAI)
- Gemini (Google)
- Cursor
- その他のAIコーディングアシスタント

上記いずれのAIプラットフォームでも利用可能です。

**プラットフォーム固有の操作方法:**
- ファイル読み込み、ファイル作成などの具体的な操作方法は、プラットフォームによって異なります
- 詳細は `../../platform_guides/` を参照してください

---

## パラメータ設定

**実行前に以下のパラメータを設定してください:**

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここに仕様書ディレクトリのパスを入力"
output_directory: "ここにタスク出力先のパスを入力（オプション）"
```

**例（back-office-apiの場合）:**
```yaml
project_root: "projects/sdd/bookstore/back-office-api-sdd"
spec_directory: "projects/sdd/bookstore/back-office-api-sdd/specs"
output_directory: "projects/sdd/bookstore/back-office-api-sdd/tasks"
```

**注意:** 
- パス区切りはOS環境に応じて調整してください（Windows: `\`, Unix/Linux/Mac: `/`）
- 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換えてください

---

## プロジェクトの役割（例: back-office-api）

**back-office-apiの場合のマイクロサービスパターン:**

- **アーキテクチャパターン**: Microservice（マイクロサービス）
- **役割**: 書籍・在庫・カテゴリ・出版社の完全なデータ管理サービス
- **呼び出し元**: BFF（berry-books-api）からREST APIとして呼ばれる
- **管理データ**: 
  - Book（書籍）
  - Stock（在庫）- **楽観的ロック必須**
  - Category（カテゴリ）
  - Publisher（出版社）
- **重要な実装要件**:
  - **楽観的ロック**: Stockエンティティに`@Version`アノテーション必須
  - **2種類の書籍検索**: JPQL検索とCriteria API検索の両方を実装
  - **全エンティティ実装**: Book、Stock、Category、Publisherすべて実装
  - **CORS対応**: BFFからのクロスオリジンリクエストに対応
  - **エラーハンドリング**: OptimisticLockException → HTTP 409 Conflict

**注意:** 上記はback-office-apiの例です。あなたのプロジェクトの役割は、仕様書（`{spec_directory}/system/architecture_design.md`）から判断してください。

---

## 概要

このインストラクションは、完成したSPECから**複数人が並行して作業できる**実装タスクリストを生成するためのものです。

**重要な方針:**
- タスクリストは**抽象度の高いレベル**で作成します
- **ソースコードや詳細な実装手順は含めません**
- 各タスクは「何を作成・修正するか」を明確に示します
- 詳細な実装は次の「実装フェーズ（コード生成）」で仕様書を参照して行います

**出力先:**
- **ベースプロジェクトの場合**: `{project_root}/tasks/` ディレクトリ
- **拡張の場合**: `{spec_directory}/tasks/` ディレクトリ
- **重要**: `{project_root}` と `{spec_directory}` は、パラメータで指定した値に置き換えてください

---

## 1. 設計ドキュメントの分析

パラメータで指定されたプロジェクト情報に基づいて、以下の設計ドキュメントを読み込んで分析してください：

**ファイル読み込みの方法:**
- ファイル読み込み操作は、使用しているAIプラットフォームによって異なります
- **Cursor/Cline**: ファイル読み込みツールを使用
- **GitHub Copilot**: ワークスペースコンテキスト参照機能を使用
- **ChatGPT**: ユーザーにファイル内容の貼り付けを依頼
- **その他**: `../../platform_guides/` を参照してください

**注意:** `{project_root}` は、パラメータで指定されたパスに置き換えてください。全てのパスはそのプロジェクトルートを基準とした相対パスです。

### プロジェクト憲章（最優先で確認）
- **principles/** - プロジェクトの開発原則、アーキテクチャ方針、品質基準、組織標準を確認
  - `constitution.md` - プロジェクト開発憲章
  - その他、組織やチームで共通的に使われる憲章ファイル
  - **重要**: タスク生成においても、憲章に記載された原則（テストカバレッジ基準、アーキテクチャパターン、コーディング規約など）を遵守すること

### 必須ドキュメント（system/配下）
- **architecture_design.md** - 技術スタック、アーキテクチャ、ライブラリを確認
- **functional_design.md** - システム全体の機能設計概要と各APIへのリンクを確認

### オプションドキュメント（system/配下、存在する場合）
- **data_model.md** - エンティティとデータベーススキーマを確認
- **behaviors.md** - システム全体の振る舞い概要と各APIへのリンクを確認
- **external_interface.md** - 外部連携とAPI仕様（OpenAPI YAML含む）を確認

### API単位ドキュメント（api/配下）
- **api/API_XXX_yyyy/** - 各API単位のディレクトリ（例: API_001_books, API_002_stocks）
  - **functional_design.md** - 各APIの詳細な機能設計（エンドポイント、リクエスト/レスポンス、ビジネスルール）
  - **behaviors.md** - 各APIの振る舞い仕様（受入基準、Given-When-Then）

**注意:** プロジェクトによって利用可能なドキュメントは異なります。利用可能なものに基づいてタスクを生成してください。

---

## 2. タスクファイルの分割構造

**出力先の決定:**
- **ベースプロジェクトの場合**: `{project_root}/tasks/` ディレクトリ
- **拡張の場合**: `{spec_directory}/tasks/` ディレクトリ

**重要:** 
- `{project_root}` は、パラメータで指定されたパスに置き換えてください
- ベースプロジェクトのタスクは `{project_root}/tasks/` に配置
- 拡張のタスクは `{spec_directory}/tasks/` に配置（例: `specs/enhancements/202512_inventory_alert/tasks/`）

複数人が並行して作業できるように、以下のようにタスクファイルを分割して生成してください：

### 2.1 メインタスクリスト
**`tasks/tasks.md`** （指定された出力先に配置）
- プロジェクト全体の実行順序を示すメインタスクリスト
- 各タスクと担当者割り当ての概要
- 他のタスクファイルへのリンク集
- タスク間の依存関係を明示

### 2.2 セットアップタスク
**`tasks/setup_tasks.md`**
- プロジェクト初期化（全員が実行前に1回だけ）
- 開発環境セットアップ
- データベース初期化
- アプリケーションサーバー設定
- ログ設定
- 静的リソース配置（画像等）

### 2.3 共通機能タスク
**`tasks/common_tasks.md`**
- 複数機能で共有される共通コンポーネント
- **全エンティティ**: Book, Stock, Category, Publisher（すべて実装）
- **重要**: Stockエンティティには`@Version`アノテーション必須（楽観的ロック）
- **共通DAO**: 各エンティティのDAO
  - BookDao（JPQL検索）
  - BookDaoCriteria（Criteria API検索）- **両方実装**
  - StockDao（楽観的ロック対応）
  - CategoryDao
  - PublisherDao
- **共通サービス**: 必要に応じて
- **共通DTO/レスポンスモデル**: ErrorResponse等
- **例外ハンドラ**: OptimisticLockExceptionMapper（HTTP 409 Conflict）
- **CORSフィルター**: BFFからのリクエスト対応

**注意 - マイクロサービスパターン**: 
このプロジェクトは**すべてのエンティティとDAOを実装**します：
- ✅ Book, Stock, Category, Publisherエンティティ
- ✅ BookDao, BookDaoCriteria, StockDao, CategoryDao, PublisherDao
- ✅ 全サービスクラス
- ✅ すべてのREST APIエンドポイント

共通コンポーネントの具体的な内容は、プロジェクトのSPECから判断してください。

### 2.4 機能別タスク（API単位）

**SPECから機能（API）を抽出してタスクファイルを生成：**

#### 機能の識別と抽出
1. **機能（API）の識別**
   - requirements.md、api/ ディレクトリ、functional_design.mdから機能を抽出
   - 各APIの範囲と責務を分析（書籍API、在庫API、カテゴリAPI、出版社API等）
   - API間の依存関係を把握

2. **タスクファイルの命名規則**
   - 基本形式：`tasks/[API_ID]_[API名].md`
   - 例：`API_001_books.md`、`API_002_stocks.md`、`API_003_categories.md`
   - api/配下のディレクトリ名と対応させる
   - **注意**: ファイル名はアンダースコア区切りを使用

3. **各機能タスクファイルの内容**
   - API固有のResourceクラス（JAX-RSエンドポイント）
   - API固有のServiceクラス
   - API固有のDaoクラス
   - API固有のDTO/レスポンスモデル
   - API固有のテストケース（単体テスト、APIテスト、**並行処理テスト**）
   - **担当者:** 1名（API単位で独立して実装可能）

4. **機能分割の判断基準**
   - **小規模プロジェクト（1-3 API）**: 1つの`all_apis.md`にまとめても可
   - **中規模プロジェクト（4-10 API）**: API単位でファイル分割
   - **大規模プロジェクト（10+ API）**: APIグループごとにディレクトリ分割も検討

### 2.5 結合テストタスク
**`tasks/integration_tasks.md`**
- API間結合テスト
- E2E APIテスト（REST Assured/JAX-RS Client） - 主要な業務フローをAPIシーケンスでテスト
- **並行処理テスト**: 在庫更新の競合シナリオテスト（重要！）
- パフォーマンステスト
- CORS動作確認
- 最終検証

---

## 3. タスク生成ルール

### 3.1 並行実行の判断基準

**[P]マークを付与する条件（並行実行可能）:**
- 異なるファイルを編集するタスク
- 異なるエンティティの実装
- 異なるAPIの実装
- 独立したテストケース

**順次実行（[P]なし）が必要な条件:**
- 同じファイルを編集するタスク
- 依存関係があるタスク（Entity → Dao → Service → Resource の順序等）

### 3.2 タスクの粒度

**重要: タスクは抽象度の高いレベルで定義し、ソースコードは含めない**

各タスクは以下の粒度で分割：
- **Entity/Model**: 1エンティティクラスの作成/修正
- **DTO/Response**: 1 DTOまたはレスポンスモデルクラスの作成/修正
- **Dao**: 1 Daoクラスの作成/修正
- **Service**: 1 Serviceクラスの作成/修正（複雑な場合は複数タスクに分割）
- **Resource**: 1 Resourceクラス（JAX-RSエンドポイント）の作成/修正
- **Filter/Interceptor/ExceptionMapper**: 1コンポーネントの作成/修正
- **Test**: 1テストクラスの作成/修正

**注意**: 上記の用語はプロジェクトの技術スタックに応じて読み替えてください。

**タスクの記述レベル:**
- 「何を作成・修正するか」を明確に記述
- 「どのような機能を実装するか」を簡潔に記述
- **ソースコードや詳細な実装手順は記述しない**
- 詳細な実装は次の「実装フェーズ（コード生成）」で行う

### 3.3 依存関係の順序付け

以下の順序でタスクを配置：

1. **セットアップ** (全ての前提)
   - 開発環境構築
   - プロジェクト初期化
   - データベース設定

2. **共通機能** (複数機能で共有)
   - 全Entity（Book, Stock, Category, Publisher）
   - 全Dao（BookDao, BookDaoCriteria, StockDao等）
   - 共通Service
   - CORSフィルター、例外ハンドラ
   - 共通DTO/Response

3. **機能別実装（API単位）** (並行実行可能)
   - 一般的な実装順序: DTO/Response → Entity → Dao → Service → Resource
   - 各APIは独立して実装可能
   - **注意**: 実装順序はプロジェクトのアーキテクチャに従う

4. **結合テスト** (全API実装後)
   - API間結合
   - E2E APIテスト（REST Assured） - 主要な業務フローベース
   - **並行処理テスト**（在庫更新競合シナリオ）
   - パフォーマンステスト

---

## 4. タスクファイルのフォーマット

各タスクファイルには以下の情報を含めてください：

### 4.1 ヘッダー情報
```markdown
# [タスクファイル名]

**担当者:** [想定人数と役割]
**推奨スキル:** [必要なスキルセット]
**想定工数:** [時間]
**依存タスク:** [前提となるタスクファイル]
```

### 4.2 タスクリスト
```markdown
- [ ] [P] **タスク X.X.X**: [タスク名]
  - **目的**: [このタスクで実現する機能・目的]
  - **対象**: [作成/修正するコンポーネント名やファイル名]
  - **参照SPEC**: [参照する仕様書（Markdownリンク形式）] の「[セクション番号 セクション名]」
  - **注意事項**: [考慮すべき点があれば記載]
```

**タスクID命名規則:**
- タスクIDは**アンダースコア区切り**を使用します（例: `T_SETUP_001`, `T_API001_003`）
- **ハイフンは使用しません**（例: ~~`T-SETUP-001`~~, ~~`T-API001-003`~~）
- 形式: `T_[カテゴリ]_[連番]` または `T_[API_ID]_[連番]`

**SPEC参照の記述規則:**
- **必須**: Markdownリンク形式で記述し、クリックで直接SPECファイルに飛べるようにする
- **必須**: 相対パスを使用（タスクファイルからSPECファイルへの相対パス）
- **必須**: 具体的なセクション番号とセクション名を明記
- **形式**: `[ファイル名](相対パス) の「セクション番号 セクション名」`

**記述例:**
```markdown
- [ ] **T_API002_003**: StockDao の作成
  - **目的**: 在庫情報の検索・更新機能を実装する
  - **対象**: StockDao.java (DAOクラス)
  - **参照SPEC**: [functional_design.md](../specs/baseline/api/API_002_stocks/functional_design.md) の「2.2 StockDao」
  - **注意事項**: 楽観的ロックに対応したupdate処理を実装すること
```

**複数SPEC参照の例:**
```markdown
- [ ] **T_API002_006**: StockResource の作成
  - **目的**: 在庫管理APIエンドポイントを実装する
  - **対象**: StockResource.java (JAX-RS Resourceクラス)
  - **参照SPEC**: 
    - [functional_design.md](../specs/baseline/api/API_002_stocks/functional_design.md) の「2. エンドポイント仕様」
    - [behaviors.md](../specs/baseline/api/API_002_stocks/behaviors.md) の「2. 在庫API」
  - **注意事項**: OptimisticLockException発生時はHTTP 409 Conflictを返すこと
```

**注意: ソースコードや詳細な実装手順は記述しない**

---

## 5. メインタスクリスト（tasks.md）の構造

`{project_root}/tasks/tasks.md` は以下の構造で生成：

```markdown
# [プロジェクト名] - 実装タスクリスト

## 全体構成と担当割り当て

### タスク概要
| タスク | タスクファイル | 担当者 | 並行実行 | 想定工数 |
|---------|--------------|--------|---------|---------|
| 0. セットアップ | setup_tasks.md | 全員 | 不可 | [分析から算出] |
| 1. 共通機能 | common_tasks.md | 共通機能チーム | 一部可能 | [分析から算出] |
| 2. 書籍API | API_001_books.md | 担当者A | 可能 | [分析から算出] |
| 3. 在庫API | API_002_stocks.md | 担当者B | 可能 | [分析から算出] |
| 4. カテゴリAPI | API_003_categories.md | 担当者C | 可能 | [分析から算出] |
| ... | ... | ... | ... | ... |
| N. 結合テスト | integration_tasks.md | 全員 | 一部可能 | [分析から算出] |

### 実行順序
1. タスク0: セットアップ（全員で実行）
2. タスク1: 共通機能（共通機能チームが実装）
3. タスク2～N-1: API別実装（各担当者が並行実行） ← ここが並行化のポイント
4. タスクN: 結合テスト（全員で実施）

### タスクファイル一覧
- [セットアップタスク](setup_tasks.md)
- [共通機能タスク](common_tasks.md)
- [書籍APIのタスク](API_001_books.md)
- [在庫APIのタスク](API_002_stocks.md)
- ...
- [結合テストタスク](integration_tasks.md)

## 依存関係図
[Mermaid形式で依存関係を図示]
\```

**生成時の注意:**
- プロジェクト名はrequirements.mdから取得
- APIの数、命名、分割方法はSPECから判断
- 想定工数は各タスクの複雑度分析から算出
- ファイル名はすべてアンダースコア区切りを使用

---

## 6. 成果物チェックリスト

生成されるタスクファイルが満たすべき要件：
- ソースコードや詳細な実装手順を含まず、「何を作成・修正するか」を明確に記述
- [P]マークで並行実行可能なタスクを明示し、依存関係を明確に記述
- タスクIDはアンダースコア区切り（例: `T_SETUP_001`）で一意に付与
- **back-office-api特有の要件**:
  - Stockエンティティの@Version注記がタスクに明記されている
  - JPQL検索とCriteria API検索の両方がタスクに含まれている
  - OptimisticLockExceptionHandlerがタスクに含まれている
  - 並行処理テストがタスクに含まれている

---

## 7. 生成手順

1. **憲章とSPEC分析**: `principles/` 配下の憲章と全SPECファイル（system/とapi/）を読み込み、開発原則と機能全体を把握
2. **機能（API）抽出**: 実装が必要なAPIを抽出し、依存関係と共通コンポーネントを識別
3. **タスク分割**: API数に応じた適切なファイル分割方法を決定（小規模: 少数ファイル、中規模: API別、大規模: グループ別）
4. **タスク構成**: 各SPECからタスクを抽出し、セットアップ/共通/API別/結合に分類・順序付け
5. **並行化判定**: [P]マークを付与し、タスクファイルを指定された出力先に生成
6. **メインリスト生成**: `tasks/tasks.md` に全体概要と実行計画を生成

**ファイル生成の方法:**
- ファイル作成操作は、使用しているAIプラットフォームによって異なります
- 詳細は `../../platform_guides/` を参照してください

**注意**: `{project_root}` と出力先はパラメータで指定されます。ファイル名・タスクIDは全てアンダースコア区切りを使用。

---

## 8. 重要な注意事項

### 命名規則
- ファイル名・タスクIDは全てアンダースコア区切り（例: `setup_tasks.md`, `T_SETUP_001`）
- ハイフン（`-`）は使用しません

### SPEC参照の記述
全てのタスクの「参照SPEC」は以下の形式で記述してください：
- Markdownリンク形式でクリック可能にする（例: `[functional_design.md](相対パス)`）
- 具体的なセクション番号とセクション名を明記（例: `の「2.2 StockDao」`）
- 複数SPEC参照の場合は箇条書きで列挙
- system/とapi/配下の両方のドキュメントを適切に参照する

### タスク生成の原則
- **憲章遵守**: `{project_root}/principles/` 配下の憲章（開発原則、品質基準、組織標準）を必ず遵守
- **抽象度の維持**: タスクは「何を作るか」のみを記述。ソースコードや詳細な実装手順は記述しない
- **既存コード考慮**: 既存実装がある場合は、修正タスクと新規作成タスクを明確に区別
- **APIテスト設定**: E2E APIテストは通常ビルドから除外し、個別実行可能にする設定（JUnit `@Tag`, Gradle設定等）を明記

### back-office-api特有の注意点
- **全エンティティ実装**: Book、Stock、Category、Publisherすべてのエンティティを実装
- **楽観的ロック必須**: Stockエンティティには必ず`@Version`アノテーションを付ける
- **2種類の検索実装**: BookDaoとBookDaoCriteriaの両方を実装
- **並行処理テスト**: 在庫更新の競合シナリオテストを必ず含める
- **CORS対応**: BFFからの呼び出しに対応するCORSフィルターを実装
- **例外ハンドリング**: OptimisticLockException → HTTP 409 Conflictの変換を実装

### REST API特有の注意点
- 画面（UI）は含まれないため、View/XHTMLに関するタスクは生成しない
- API エンドポイント（Resource）のテストは REST Assured や JAX-RS Client を使用
- CORS、HTTPステータスコードの適切な使用を考慮

### プロジェクトルートの扱い
- `{project_root}` は、パラメータで明示的に指定されたパスに置き換えてください
- 相対パスでも絶対パスでも構いません
- 全てのファイル操作は、このプロジェクトルートを基準に行います

---

## プラットフォーム別ガイド

このインストラクションの実行方法は、AIプラットフォームによって異なります。
詳細は以下を参照してください：

- **Cursor/Cline**: `../../platform_guides/cursor_cline.md`
- **GitHub Copilot**: `../../platform_guides/github_copilot.md`
- **その他**: `../../platform_guides/other_platforms.md`

