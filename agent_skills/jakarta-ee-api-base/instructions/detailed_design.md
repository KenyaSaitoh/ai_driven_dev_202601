# 詳細設計インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
target_type: "FUNC_XXX_xxx"
```

* 例1: FUNC_001の詳細設計
```yaml
project_root: "projects/sdd/bookstore/back-office-api-sdd"
spec_directory: "projects/sdd/bookstore/back-office-api-sdd/specs/baseline"
target_type: "FUNC_001_auth"
```

* 例2: 別の機能の詳細設計
```yaml
project_root: "projects/sdd/bookstore/back-office-api-sdd"
spec_directory: "projects/sdd/bookstore/back-office-api-sdd/specs/baseline"
target_type: "FUNC_002_books"
```

注意
* パス区切りはOS環境に応じて調整する（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換える
* 以降、`{spec_directory}` と表記されている箇所は、上記で設定した値に置き換える
* `{target_type}` はタスク分解で決定されたFUNC_XXX_xxx形式のタスクIDに置き換える
* アーキテクチャパターンはSPECから自動判定する

---

## 概要

このインストラクションは、基本設計SPEC（basic_design/）とタスク分解の結果から詳細設計書（detailed_design/）を生成するためのものである

重要な方針
* タスク分解で識別された機能（FUNC_XXX）に対して詳細設計を作成する
* basic_design/functional_design.md を参照して、実装レベルの detailed_design.md を作成する
* 単体テスト用の behaviors.md を新規作成する（templates/detailed_design/behaviors.mdから、E2Eテスト用の basic_design/behaviors.md とは別物）
* AIが仕様を理解し、人と対話しながら妥当性・充足性を確認する
* 推測せず、不明点は必ずユーザーに質問する
* アーキテクチャパターンはSPECから判断する（パラメータ指定不要）

基本設計と詳細設計の分界点:
* 基本設計（basic_design/）: 機能要件（functional_design.md）、システム全体の振る舞い（結合テスト用 behaviors.md）
* 詳細設計（detailed_design/）: 実装クラス設計（detailed_design.md）、実装単位の振る舞い（単体テスト用 behaviors.md）

behaviors.mdの違い:
* basic_design/behaviors.md: システム全体の振る舞い（E2Eテスト用）
  * API間連携、システム統合シナリオ
  * 複数コンポーネントにまたがるE2Eのフロー
  * 実際のDBアクセス、外部API呼び出しを含む
  * REST Assuredを使用したHTTPリクエスト/レスポンステスト
  
* detailed_design/{target}/behaviors.md: タスク粒度内の単体テスト用の振る舞い
  * タスク内のコンポーネント間の連携をテスト
  * タスク内のコンポーネントはモック不要（実際の連携をテスト）
  * タスク外の依存関係のみモックを使用
  * 例: BookServiceとBookDaoが同じタスクなら、両方とも実インスタンスでテスト（EntityManagerのみモック）

フォルダ構造
* `{spec_directory}/basic_design/` - 基本設計（フェーズ1で作成済み）
  * functional_design.md - 全機能の要件（唯一の真実の情報源）
  * behaviors.md - システム全体の振る舞い（E2Eテスト用）
* `{spec_directory}/detailed_design/FUNC_XXX_xxx/` - 機能単位の詳細設計（タスク分解で識別）
  * detailed_design.md - 実装クラス設計
  * behaviors.md - 単体テスト用の振る舞い

機能の例（内容はプロジェクト固有で、タスク分解の結果により決定）:
* `FUNC_001_auth/` - 例: 認証・認可機能
* `FUNC_002_books/` - 例: 書籍管理機能
* `FUNC_003_orders/` - 例: 注文管理機能
* `FUNC_004_inventory/` - 例: 在庫管理機能

注意
* 詳細設計フェーズで初めて detailed_design/ フォルダを作成する
* タスク分解の結果（FUNC_XXX）に基づいてフォルダを作成する
* functional_design.md は basic_design/ にのみ存在する（detailed_design/ には作成しない）
* basic_design/functional_design.md を参照して detailed_design.md を作成する
* behaviors.md は単体テスト用に新規作成する（templates/detailed_design/behaviors.mdから、E2Eテスト用の basic_design/behaviors.md とは別物）

---

## 1. SPECの読み込みと理解

パラメータで指定されたプロジェクト情報に基づいて、以下の設計ドキュメントを読み込んで分析する

注意: `{project_root}`, `{spec_directory}`, `{api_id}` は、パラメータで指定された値に置き換える

### 1.1 Agent Skillsルール（最優先で確認）

* @agent_skills/jakarta-ee-api-base/principles/ - Jakarta EE開発の原則、アーキテクチャ標準、品質基準、セキュリティ標準を確認する
  * このフォルダ配下の原則ドキュメントを読み込み、共通ルールを遵守すること
  * 重要: 詳細設計においても、ルールドキュメントに記載されたすべてのルールを遵守すること
  * 注意: Agent Skills配下のルールは全プロジェクト共通。プロジェクト固有のルールがある場合は `{project_root}/principles/` も確認すること

### 1.2 フレームワーク仕様（該当する場合）

* @agent_skills/jakarta-ee-api-base/frameworks/ - フレームワーク固有のSPECやサンプルコードを確認する
  * 特定のフレームワーク（ライブラリ、ツール等）の使用方法、設計パターン、実装例を参照する
  * 詳細設計時に、フレームワーク仕様に従った設計を行う

### 1.3 基本設計の仕様

以下のファイルを読み込み、システム全体の設計を理解する

* {spec_directory}/basic_design/architecture_design.md - 技術スタック、パッケージ構造、セキュリティ方式を確認する

* {spec_directory}/basic_design/functional_design.md - システム全体の機能設計（全APIを含む）を確認する
  * システム全体の機能概要
  * 全てのAPI機能の設計
  * 共通機能の設計（認証、ログ、エラーハンドリング等）
  * ドメインモデルの機能設計（ビジネスルール、バリデーション、状態遷移等）

* {spec_directory}/basic_design/data_model.md - テーブル定義とERDを確認し、JPAエンティティクラスを設計する
  * 注意: data_model.mdはRDB論理設計（テーブル、カラム、制約）のみ記述
  * JPAエンティティクラスの設計（@Entity, @Column, @ManyToOne等のアノテーション、Java型、リレーションマッピング）は該当する機能タスクのdetailed_design.mdで実施（通常はEntity/Daoを担当する機能タスク）

* {spec_directory}/basic_design/behaviors.md - 結合テスト用の振る舞い（Service層以下）を確認する
* {spec_directory}/requirements/behaviors.md - E2Eテスト用の振る舞い（システム全体）を確認する（参考）

注意: 
* 基本設計フェーズでは、全ての機能が basic_design/ に一枚岩として記述されている
* タスク分解フェーズで、機能が依存関係に基づいて識別され、実装順序が決定されている
* 詳細設計フェーズで、basic_design/ から該当部分を抽出して detailed_design/ フォルダを作成する

### 1.4 タスク分解の結果

以下のタスクファイルを読み込み、対象の範囲を理解する

* {project_root}/tasks/{target_type}.md - 対象機能のタスク
  * 例: FUNC_001_auth.md
  * 例: FUNC_002_books.md
  * 例: FUNC_003_orders.md

タスクファイルから、以下を確認する：
* 実装対象のコンポーネント（Resource、Service、Dao、DTO等）
* 参照すべき SPEC（basic_design/ の該当セクション）
* 依存関係

注意: システム全体の機能設計、共通処理の振る舞いはbasic_design/functional_design.md、basic_design/behaviors.mdを参照する

---

## 2. 理解内容の確認と対話

### 2.1 理解した内容をユーザーに説明

以下の形式で、AIが理解した内容をユーザーに説明する

```markdown
## 理解した内容

### API概要

* API名: <API名>
* ベースパス: <パス>
* 主要機能: <機能の箇条書き>
* 実装範囲: <Entity/Dao/Service実装の有無、外部API連携の有無>

### エンドポイント

1. <メソッド> <パス> - <機能>
   * リクエスト: <形式>
   * レスポンス: <形式>
   * 認証: <要/不要>

### 関連エンティティ

* <エンティティ名>（テーブル: <テーブル名>）
  * 主要フィールド: <フィールド一覧>

### 外部API連携（該当する場合）

* <外部API名>: <連携内容>

### 不明点・確認事項

1. <不明点1>
2. <不明点2>
```

### 2.2 対話による確認

ユーザーに質問すべき項目

1. パッケージ構造
   * 「このAPIで実装するクラスは以下でよろしいでしょうか」
   * 実装するコンポーネントのリスト

2. DTO設計
   * 「リクエスト/レスポンスのDTOは以下の形式でよろしいでしょうか」

3. ビジネスロジック
   * 「以下のビジネスルールの理解は正しいでしょうか」
   * バリデーション、計算ロジック、状態遷移等

4. エラーハンドリング
   * 「以下のエラーケースで漏れはないでしょうか」
   * 想定されるエラーシナリオ

5. 外部API連携
   * 「外部APIの呼び出しは以下の理解で正しいでしょうか」
   * エンドポイント、リクエスト/レスポンス形式

6. トランザクション管理
   * 「トランザクション境界は以下でよろしいでしょうか」
   * トランザクション管理の配置場所

### 2.3 不足情報の補完

ユーザーからのフィードバックを受けて、以下を補完する

* SPECに記載されていない実装詳細
* クラス間の依存関係
* メソッドシグネチャの詳細
* エラーメッセージの文言
* 設定情報

---

## 3. 詳細設計書の生成

### 3.1 生成するファイル

詳細設計フェーズでは、以下の2つのファイルを生成する：

#### 3.1.1 detailed_design.md（実装クラス設計）

```
{spec_directory}/detailed_design/{target_type}/detailed_design.md
```

* 例1: `{project_root}/specs/baseline/detailed_design/FUNC_001_auth/detailed_design.md`
* 例2: `{project_root}/specs/baseline/detailed_design/FUNC_002_order_service/detailed_design.md`
* 例3: `{project_root}/specs/baseline/detailed_design/FUNC_003_books/detailed_design.md`

* 共通記載内容（全機能タイプ）:
  * 実装クラス設計（クラス名、パッケージ、アノテーション）
  * メソッドシグネチャ（引数、戻り値、例外）
  * 依存性注入の設計（@Inject、@Named等）
  * JPQL/Criteria APIのクエリ設計（該当する場合）
  * DTOとエンティティのマッピング設計（該当する場合）

機能別の記載内容（タスク内容による）:

FUNC_001の例（Entity、Dao等を含む）:
  * ドメインモデル（JPAエンティティ）の詳細設計
  * Daoクラスの詳細設計
  * ユーティリティクラス、共通例外クラスの詳細設計
  * セキュリティコンポーネント（JWT、認証フィルター等）の詳細設計

FUNC_002の例（複数機能から使用されるService）:
  * Serviceクラスの詳細設計
  * ビジネスロジック、トランザクション境界の設計

FUNC_003の例（REST API機能）:
  * Resourceクラス（JAX-RS）の詳細設計
  * 機能固有のDTOクラス（Request、Response）の詳細設計
  * 機能固有のビジネスロジック（Serviceメソッド）の詳細設計
  * 外部API連携クライアント（該当する場合）の詳細設計

#### 3.1.2 behaviors.md（純粋な単体テスト用の振る舞い）

テンプレート: templates/detailed_design/behaviors.md  
コピー先:
```
{spec_directory}/detailed_design/{target_type}/behaviors.md
```

* 記載内容:
  * メソッドレベルの単体テストシナリオ（Given-When-Then形式）
  * 依存関係はモックを使用（@Mock, Mockito等）
  * 1メソッド＝1テストケースの粒度
  * 境界値テスト、異常系テスト（nullチェック、例外処理等）
  * 単体テストの受入基準

* テスト対象の例:
  * Service層: `BookService.findById(Long id)` → BookDaoをモック化
  * Resource層: `BookResource.getBook(Long id)` → BookServiceをモック化
  * Dao層: `BookDao.findById(Long id)` → EntityManagerをモック化

* テストに含まれないもの:
  * DBアクセス（実際のデータベース接続）
  * 外部API呼び出し（実際のHTTP通信）
  * トランザクション処理
  * 複数クラスにまたがる統合シナリオ

重要: 
* requirements/behaviors.md（E2Eテスト用）、basic_design/behaviors.md（結合テスト用）とは完全に別物
* requirements/behaviors.md: システム全体のE2Eシナリオ、API層含む全体フロー
* basic_design/behaviors.md: Service層以下の連携シナリオ、実際のDB操作
* detailed_design/{target}/behaviors.md: 純粋な単体テスト、1メソッド単位、依存関係はモック

### 3.2 システム全体の詳細設計書テンプレート

```markdown
# システム詳細設計書

* プロジェクトID: {project_id}  
* バージョン: 1.0.0  
* 最終更新: <日付>

---

## 1. ドメインモデル（JPAエンティティ）

### 1.1 <Entity名>

* テーブル: \`<テーブル名>\`

* 主要フィールド:

|| フィールド名 | 型 | カラム名 | 制約 | 説明 |
||------------|---|---------|-----|------|
|| \`<フィールド>\` | \`<型>\` | \`<カラム>\` | \`<制約>\` | <説明> |

* アノテーション:
\`\`\`java
@Entity
@Table(name = "<テーブル名>")
\`\`\`

* リレーション:
  * \`@ManyToOne\` - <関連エンティティ>

---

## 2. データアクセス層（Dao）

### 2.1 <Dao名>

* 責務: <責務の説明>

* アノテーション:
  * \`@ApplicationScoped\`（依存性注入）

* 主要メソッド:

#### <メソッド名>()

* シグネチャ:
\`\`\`java
public <戻り値型> <メソッド名>(<引数>)
\`\`\`

* JPQL:
\`\`\`sql
<JPQLクエリ>
\`\`\`

---

## 3. ビジネスロジック層（共通Service）

### 3.1 <Service名>

* 責務: <責務の説明>

* アノテーション:
  * \`@ApplicationScoped\`（依存性注入）
  * \`@Transactional\`（該当する場合）

* 主要メソッド:

#### <メソッド名>()

* シグネチャ:
\`\`\`java
public <戻り値型> <メソッド名>(<引数>)
\`\`\`

* 処理:
  1. <ステップ1>
  2. <ステップ2>

---

## 4. セキュリティコンポーネント

### 4.1 JwtUtil

* 責務: JWT生成・検証

* 主要メソッド:

#### generateToken()

* シグネチャ:
\`\`\`java
public String generateToken(Long userId, String email)
\`\`\`

---

## 5. ユーティリティクラス

### 5.1 <Utility名>

* 責務: <責務の説明>

* 主要メソッド:

---

## 6. 共通例外クラス

### 6.1 <Exception名>

* 責務: <責務の説明>

---

## 7. 設定情報

### 7.1 MicroProfile Config

* ファイル: \`src/main/resources/META-INF/microprofile-config.properties\`

\`\`\`properties
<設定項目>
\`\`\`

---

## 8. 参考資料

* [data_model.md](data_model.md) - データモデル仕様書
* [architecture_design.md](architecture_design.md) - アーキテクチャ設計書
```

### 3.3 機能固有の詳細設計書テンプレート

```markdown
# {api_id} <API名> - API詳細設計書

* API ID: {api_id}  
* API名: <API名>  
* バージョン: 1.0.0  
* 最終更新: <日付>

---

## 1. API概要

* ベースパス: \`<パス>\`
* 認証: <要/不要>
* 実装パターン: <完全なCRUD実装 / プロキシ転送 / 独自実装 + 外部連携 / 静的リソース配信>

---

## 2. パッケージ構造

### 2.1 機能固有パッケージ

\`\`\`
<ベースパッケージ>
├── api
│   ├── <Resource名>.java
│   ├── dto
│   │   ├── <Request名>.java
│   │   └── <Response名>.java
│   └── exception
│       └── <ExceptionMapper名>.java（該当する場合）
├── service（機能固有のビジネスロジックがある場合）
│   └── <パッケージ>
│       └── <Service名>.java
└── external（外部API連携がある場合）
    ├── <RestClient名>.java
    └── dto
        └── <外部API用DTO名>.java
\`\`\`

注意: エンティティ、Dao、共通Serviceは依存タスクの詳細設計を参照してください（タスクファイルのメタデータ「依存タスク」欄を確認）

---

## 3. Resourceクラス設計

### 3.1 <Resource名>（JAX-RS Resource）

* 責務: <責務の説明>

* アノテーション:
  * \`@Path("<パス>")\` - ベースパス
  * \`@ApplicationScoped\` - CDIスコープ（依存性注入）

* 主要メソッド:

#### <メソッド名>() - <機能名>

\`\`\`
@<HTTPメソッド>
@Path("<パス>")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
\`\`\`

* パラメータ:
  * \`<型> <変数名>\` - <説明>

* 処理フロー:
  1. <ステップ1>
  2. <ステップ2>
  3. <ステップ3>

* レスポンス: \`<型>\`

* エラーケース:
  * <エラーケース1> → \`<HTTPステータス>\`

---

## 4. DTO設計

### 4.1 <Request名>（リクエストDTO - Record）

\`\`\`java
public record <名前>(
    @NotBlank(message = "<メッセージ>")
    String <フィールド名>,
    
    <その他フィールド>
) {}
\`\`\`

---

### 2.3 <Service名>（ビジネスロジック層）

* 責務: <責務の説明>

* アノテーション:
  * \`@ApplicationScoped\`（依存性注入）

* 主要メソッド:

#### <メソッド名>()

* シグネチャ:
\`\`\`java
public <戻り値型> <メソッド名>(<引数>)
\`\`\`

* 処理:
  1. <ステップ1>
  2. <ステップ2>

---

### 2.4 <Dao名>（データアクセス層）

* 責務: <責務の説明>

* アノテーション:
  * \`@ApplicationScoped\`（依存性注入）

* 主要メソッド:

#### <メソッド名>()

* シグネチャ:
\`\`\`java
public <戻り値型> <メソッド名>(<引数>)
\`\`\`

* JPQL:
\`\`\`sql
<JPQLクエリ>
\`\`\`

---

### 2.5 <Entity名>（エンティティ）

* テーブル: \`<テーブル名>\`

* 主要フィールド:

| フィールド名 | 型 | カラム名 | 制約 | 説明 |
|------------|---|---------|-----|------|
| \`<フィールド>\` | \`<型>\` | \`<カラム>\` | \`<制約>\` | <説明> |

* アノテーション:
\`\`\`java
@Entity
@Table(name = "<テーブル名>")
\`\`\`

* リレーション:
  * \`@ManyToOne\` - <関連エンティティ>

---

## 4. 設定情報

### 4.1 MicroProfile Config

* ファイル: \`src/main/resources/META-INF/microprofile-config.properties\`

\`\`\`properties
<設定項目>
\`\`\`

---

## 5. エラーハンドリング

### 5.1 エラーシナリオ

| エラーケース | HTTPステータス | レスポンス |
|------------|--------------|----------|
| <エラー1> | <ステータス> | \`<レスポンス>\` |

---

## 6. テスト要件

### 6.1 ユニットテスト

* 対象: \`<クラス名>\`

* <テストケース1>
* <テストケース2>

### 6.2 結合テスト

* 対象: <対象の説明>

* <テストケース1>
* <テストケース2>

---

## 7. 参考資料

* [functional_design.md](functional_design.md) - 機能設計書
* [behaviors.md](behaviors.md) - 振る舞い仕様書
```

---

## 4. 機能別の設計内容（タスク分解の結果による）

タスク分解で決定された各FUNC_XXXに対して、以下のような内容を記載します。

### 4.1 記載内容の例

例: Entity、Daoを含む機能タスクの場合:
* 出力先: `{spec_directory}/detailed_design/FUNC_XXX_yyy/detailed_design.md`
* 記載内容:
  * ドメインモデル（JPAエンティティ）の詳細設計
  * Daoクラスの詳細設計
  * セキュリティコンポーネント（JwtUtil、JwtAuthenFilter等）の詳細設計
  * ユーティリティクラスの詳細設計
  * 共通例外クラス、Exception Mapperの詳細設計
  * 設定情報（MicroProfile Config、persistence.xml等）

FUNC_002の例（複数機能から使用されるService）:
* 出力先: `{spec_directory}/detailed_design/FUNC_002_order_service/detailed_design.md`
* 記載内容:
  * 複数の機能で共有されるServiceクラスの詳細設計
  * ビジネスロジック、トランザクション境界の設計
  * ドメインルールの実装設計

FUNC_003の例（REST API機能）:
* 出力先: `{spec_directory}/detailed_design/FUNC_003_books_api/detailed_design.md`
* 記載内容:
  * Resourceクラス（JAX-RS）の詳細設計
  * 機能固有のDTOクラス（Request、Response）の詳細設計
  * 機能固有のビジネスロジック（Serviceメソッド）の詳細設計
  * 外部API連携クライアント（該当する場合）の詳細設計
  * 機能固有のエラーハンドリング
  * 機能固有のテスト要件

### 4.2 配置の判断基準（依存関係ベース）

| 設計対象 | 配置 | 判断基準 |
|---------|---------|---------|
| JPAエンティティ | 依存されるFUNC（例: FUNC_001） | 複数の機能から依存される |
| Dao | 依存されるFUNC（例: FUNC_001） | 複数の機能から依存される |
| セキュリティ | 依存されるFUNC（例: FUNC_001） | 複数の機能から依存される |
| ユーティリティ | 依存されるFUNC（例: FUNC_001） | 複数の機能から依存される |
| 複数機能で使用されるService | 依存されるFUNC（例: FUNC_002） | 複数の機能から依存される |
| Resource（JAX-RS） | 特定のFUNC（例: FUNC_003） | 特定のAPIのみで使用 |
| 機能固有のDTO | 特定のFUNC（例: FUNC_003） | 特定のAPIのみで使用 |
| 機能固有のService | 特定のFUNC（例: FUNC_003） | 特定のAPIのみで使用（並行作業を考慮） |
| 外部API連携クライアント | 使用範囲に応じて | 単一機能→そのFUNC、複数機能→依存されるFUNC |

重要: 
* タスク分解で依存関係が決定されている
* 依存される側のFUNCを先に実装する必要がある
* 特定の機能のみで使用されるコンポーネントは、その機能のフォルダに配置することで、担当者が独立して作業できる

---

## 5. SPECからの実装範囲判定

AIは以下の情報から、実装すべきクラスを判断する

### 5.1 data_model.mdの確認

* テーブル定義（ERD）がある場合
  * ✅ JPAエンティティクラスの設計が必要（ERDからマッピング）
  * ✅ Dao実装が必要
  * ✅ Service実装が必要
  * ✅ トランザクション管理
  * ✅ 並行制御

* エンティティ定義がない場合
  * ❌ Entity, Dao, Service不要

### 5.2 external_interface.mdの確認

* 外部API定義がある場合
  * ✅ RestClient実装が必要
  * ✅ 外部API用DTO定義
  * ✅ エラーハンドリング

* 外部API定義がない場合
  * ❌ RestClient不要

### 5.3 実装パターンの判定

| data_model.md | external_interface.md | 実装パターン | 例 |
|--------------|---------------------|----------|---|
| ❌ なし | ❌ なし | 静的リソース配信 | 画像API |
| ❌ なし | ✅ あり | プロキシ転送のみ | 書籍API（外部API転送） |
| ✅ あり | ❌ なし | 完全なCRUD実装 | 書籍API（エンティティ実装） |
| ✅ あり | ✅ あり | 独自実装 + 外部連携 | 注文API（複合実装） |

### 4.4 詳細設計に含めるべき内容

* 完全なCRUD実装の場合
  * エンティティの完全な定義
  * Daoの実装
  * Serviceのビジネスロジック
  * トランザクション管理

* プロキシ転送のみの場合
  * Resourceの実装
  * RestClientの実装
  * 外部API用DTOの定義
  * 「実装されていないクラス」を明記

* 独自実装 + 外部連携の場合
  * 自プロジェクトで管理するコンポーネント
  * 外部API連携用のクライアント
  * 分散トランザクションの考慮
  * エラーハンドリング

---

## 6. 実装チェックリスト

詳細設計書を作成する前に、以下を確認する

### 仕様理解の確認

* [ ] APIの目的と機能を理解している
* [ ] エンドポイント一覧を把握している
* [ ] リクエスト/レスポンス形式を理解している
* [ ] ビジネスルールを理解している
* [ ] エラーケースを把握している

### パッケージ構造の確認

* [ ] ベースパッケージを確認した
* [ ] 命名規則を確認した
* [ ] 実装要件（エンティティ実装、外部API連携等）を確認した
* [ ] 実装が必要なクラスをリストアップした
* [ ] 実装が不要なクラス（外部API管理の場合）を確認した

### データモデルの確認

* [ ] エンティティのテーブル定義を確認した
* [ ] フィールド、型、制約を確認した
* [ ] リレーションを確認した
* [ ] 楽観的ロックの有無を確認した

### 外部API連携の確認

* [ ] 外部APIのエンドポイントを確認した
* [ ] リクエスト/レスポンス形式を確認した
* [ ] エラーハンドリングを確認した

### 対話による確認

* [ ] ユーザーに理解内容を説明した
* [ ] 不明点をユーザーに質問した
* [ ] ユーザーからのフィードバックを反映した
* [ ] 不足情報を補完した

---

## 7. 注意事項

### SPECの優先順位

詳細が矛盾する場合、以下の優先順位で判断する

1. {spec_directory}/detailed_design/FUNC_XXX/functional_design.md（最優先）
2. {spec_directory}/detailed_design/FUNC_XXX/behaviors.md
3. {spec_directory}/basic_design/architecture_design.md
4. {spec_directory}/basic_design/functional_design.md
5. ベースライン仕様（拡張機能の場合、system配下が存在しない場合）

### 不明点の扱い

不明点がある場合は、必ずユーザーに質問する

推測で詳細設計を作成しない

### 実装範囲の判定

SPECから以下を確認する

* {spec_directory}/system/data_model.md: テーブル定義（ERD）の有無 → JPAエンティティ/Dao/Service実装の必要性
* {spec_directory}/system/external_interface.md: 外部API定義の有無 → RestClient実装の必要性
* {spec_directory}/detailed_design/FUNC_XXX/functional_design.md: エンドポイント定義 → Resource実装の必要性

重要な分界点:
* 基本設計（data_model.md）: RDB論理設計のみ（テーブル、カラム、型、制約、リレーション）
* 詳細設計（detailed_design.md）: JPAエンティティクラス設計（@Entity, @Table, @Column, @ManyToOne等のアノテーション、Java型、フィールド名、リレーションマッピング）
* JPAエンティティクラスの設計は、data_model.mdのERD（テーブル定義）から詳細設計フェーズでマッピングして作成します

Jakarta EEによるAPIシステムとして、SPECの内容から判断する

注意: 拡張機能の場合、system配下の仕様が存在しない場合はベースラインの仕様を参照する

### 既存のdetailed_design.mdの扱いと反復的なブラッシュアップ

詳細設計は一度で完璧になることはない。以下のタイミングで更新が必要:

#### 更新が必要なケース

1. コード生成時に設計の不整合を発見
2. 単体テスト実装時に設計の不足を発見
3. テスト実行時に設計の誤りを発見
4. カバレッジ分析で不足やデッドコードを発見
5. レビュー時に改善点を発見

#### すでにdetailed_design.mdが存在する場合

1. 必須: 既存のdetailed_design.mdを読み込む
2. 分析: 現在の設計内容を理解する
3. ユーザー確認: 以下を確認する
   ```
   既存の詳細設計書が見つかりました
   
   どのように進めますか？
   A. 全面的に書き直す（上書き）
   B. 特定のセクションのみ更新する
   C. 不足セクションを追加する
   D. 既存の内容を確認してから判断する
   ```
4. 更新: 選択に応じて実行
5. 履歴: 「最終更新」日付を更新

#### 設計の改善パターン

以下のような改善が典型的:
* メソッドシグネチャの調整
* エラーハンドリングの追加・明確化
* バリデーションロジックの明確化
* テストケースの追加
* パフォーマンス考慮事項の追加
* デッドコードの明記と削除理由の記載

#### フィードバックループ

品質を高めるため、以下のループを繰り返す:
```
詳細設計 → コード生成 → テスト実行 → 評価
    ↑                              ↓
    └──────── フィードバック ←─────┘
```

各イテレーションで:
* 単体テスト実行結果を確認
* カバレッジギャップを分析
* 不足している振る舞いをbehaviors.mdに追加
* デッドコードをdetailed_design.mdに明記
* 設計の誤りを修正

### ベースラインと拡張機能の違い

ベースライン（初回リリース版）
* {spec_directory} = `{project_root}/specs/baseline`
* system配下にシステム全体の仕様が存在する
* 完全な仕様セットが揃っている

拡張機能（エンハンスメント）
* {spec_directory} = `{project_root}/specs/enhancements/[拡張名]`
* system配下が存在しない場合がある
* その場合はベースラインのsystem仕様を参照する
* api配下には拡張機能固有のAPI仕様のみが存在する

---

## 参考資料

* [task_breakdown.md](task_breakdown.md) - タスク分解（前工程）
* [code_generation.md](code_generation.md) - コード生成（次工程）
* [Jakarta EE 10仕様](https://jakarta.ee/specifications/)
* [JPA仕様](https://jakarta.ee/specifications/persistence/3.1/)
* [JAX-RS仕様](https://jakarta.ee/specifications/restful-ws/3.1/)
