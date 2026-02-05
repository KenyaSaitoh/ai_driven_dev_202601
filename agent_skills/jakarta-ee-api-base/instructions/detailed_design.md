# 詳細設計インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
target_domain: "対象ドメイン名"
```

* 例1: commonの詳細設計
```yaml
project_root: "projects/sdd-wf/bookstore/back-office-api-sdd"
spec_directory: "projects/sdd-wf/bookstore/back-office-api-sdd/specs/baseline"
target_domain: "common"
```

* 例2: ordersドメインの詳細設計
```yaml
project_root: "projects/sdd-wf/bookstore/back-office-api-sdd"
spec_directory: "projects/sdd-wf/bookstore/back-office-api-sdd/specs/baseline"
target_domain: "orders"
```

注意
* パス区切りはOS環境に応じて調整する（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換える
* 以降、`{spec_directory}` と表記されている箇所は、上記で設定した値に置き換える
* `{target_domain}` は basic_design/ 配下のドメインフォルダ名に置き換える
* アーキテクチャパターンはSPECから自動判定する
* commonは最優先で実装する必要がある

---

## 概要

このインストラクションは、基本設計SPEC（basic_design/{target_domain}/）から詳細設計書（detailed_design/{target_domain}/）を生成するためのものである

重要な方針
* 簡潔性の原則（最重要）:
  * 詳細設計書は、基本設計とコードの「橋渡し」となる設計判断のみを簡潔に記載する
  * 後から人が修正する可能性を考慮し、必要最小限の情報のみを記載する
  * ソースコードに近い詳細な情報（実装詳細、処理ステップ等）は記載しない
* 既存ファイルの扱い（重要）:
  * 既存の detailed_design.md や behaviors.md が存在する場合は、それらを削除せずに読み込んで、差分のみを反映する
  * ファイルをゼロから作り直すのではなく、既存の内容を尊重して必要な部分のみを追加・修正する
  * 新規作成が必要な場合のみ、テンプレートから作成する
* basic_design/配下のドメインフォルダ（common/, orders/, books_proxy/等）ごとに詳細設計を作成する
* commonは最優先で詳細設計を作成する（他のドメインはcommonに依存）
* basic_design/{target_domain}/functional_design.md を参照して、実装レベルの detailed_design.md を作成する
* 単体テスト用 behaviors.md をテンプレートから新規作成する（種別は下記「behaviors.mdの種別」参照）
* AIが仕様を理解し、人と対話しながら妥当性・充足性を確認する
* 不明点の確認（最重要）:
  * 判断に迷う点、複数の解釈が可能な点、SPECに明示されていない実装判断は必ずユーザーに質問する
  * 推測や仮定で設計を進めることは厳禁
  * 必要に応じて何度でも確認するが、SPECから明らかなこと、技術的常識は質問不要
  * 詳細設計の精度が後続のコード生成品質を左右するため、ここが最も重要な工程
* アーキテクチャパターンはSPECから判断する（パラメータ指定不要）

基本設計と詳細設計の分界点:
* 基本設計（basic_design/{target_domain}/）: 機能要件（functional_design.md）、ドメインの振る舞い（結合テスト用 behaviors.md）
* 詳細設計（detailed_design/{target_domain}/）: 実装クラス設計（detailed_design.md）、実装単位の振る舞い（単体テスト用 behaviors.md）

重要な原則 - 詳細設計書は「橋渡し情報」のみ簡潔に記載:
* 詳細設計書の役割: 基本設計（what/why）と実装コード（how）の「橋渡し」となる設計判断のみを記載する
* 簡潔性の原則: 後から人が修正する可能性を考慮し、必要最小限の情報のみを記載する
* 冗長性の排除: 基本設計SPECに既に記載されている情報や、コードから自明な情報は記載しない

詳細設計で記載すべき情報（最小限）:
* クラス構成: クラス名と責務（1行）
* 主要メソッド: シグネチャ（引数、戻り値、例外）のみ。実装ロジックは記載しない
* 設計判断を示すアノテーション: @Transactional, @Path等の重要な設計判断のみ
* JPQL/クエリ: データアクセスの設計判断（WHERE句、JOIN等）
* 依存関係: どのコンポーネントを注入するか（@Inject対象）
* DTOマッピング方針: 変換の方針のみ（フィールド毎の詳細マッピングは不要）

詳細設計で記載すべきでない情報:
* ❌ メソッドの実装詳細（処理ステップの詳細、ループ、条件分岐等）
* ❌ すべてのフィールド定義（data_model.mdから明らか）
* ❌ getter/setterの列挙
* ❌ バリデーションの詳細（基本設計から導出可能）
* ❌ 細かいエラーハンドリングの実装詳細
* ❌ 基本設計SPECの内容の繰り返し

behaviors.mdの種別（本インストラクションで参照する区別）:
* requirements/behaviors.md: E2Eテスト用（システム全体、API層含む全体フロー）
* basic_design/{target_domain}/behaviors.md: 結合テスト用（ドメイン内の連携シナリオ、実際のDB操作）
* detailed_design/{target_domain}/behaviors.md: 単体テスト用（1メソッド単位、依存関係はモック。本フェーズで新規作成）

フォルダ構造
* `{spec_directory}/basic_design/` - 基本設計（基本設計フェーズで作成済み）
  * common/ - 共通ドメイン（Entity, Dao, JWT等。最優先実装）
    * architecture_design.md - アーキテクチャ設計
    * data_model.md - 共通エンティティのデータモデル
    * external_interface.md - 外部API仕様
    * functional_design.md - 共通機能の要件
    * behaviors.md - 共通機能の振る舞い（結合テスト用）
  * {ドメイン名}/ - プロジェクト固有ドメイン（orders, books_proxy, images等）
    * functional_design.md - ドメインの機能要件
    * behaviors.md - ドメインの振る舞い（結合テスト用）

* `{spec_directory}/detailed_design/` - 詳細設計（本フェーズで作成）
  * common/ - 共通ドメインの詳細設計
    * detailed_design.md - 実装クラス設計（Entity, Dao, JWT等）
    * behaviors.md - 単体テスト用（Gherkin 記法）
  * {ドメイン名}/ - ドメインの詳細設計
    * detailed_design.md - 実装クラス設計（Resource, Service, DTO等）
    * behaviors.md - 単体テスト用（Gherkin 記法）

ドメインフォルダの例（プロジェクトにより異なる）:
* `common/` - 固定：共通ドメイン（Entity, Dao, JWT等）
* `orders/` - 可変：注文管理ドメイン
* `books_proxy/` - 可変：書籍API連携ドメイン
* `images/` - 可変：画像配信ドメイン

注意
* 詳細設計フェーズで初めて detailed_design/ フォルダを作成する
* basic_design/配下のドメインフォルダ構成と同じ構成で detailed_design/ を作成する
* functional_design.md は basic_design/{target_domain}/ にのみ存在する（detailed_design/ には作成しない）
* basic_design/{target_domain}/functional_design.md を参照して detailed_design/{target_domain}/detailed_design.md を作成する
* behaviors.md は単体テスト用に新規作成する（templates/detailed_design/behaviors.mdから）

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

### 1.3 基本設計の仕様（共通）

まず、共通ドメインのSPECを読み込み、システム全体の設計を理解する

* {spec_directory}/basic_design/common/architecture_design.md - 技術スタック、パッケージ構造、セキュリティ方式を確認する

* {spec_directory}/basic_design/common/data_model.md - テーブル定義とERDを確認し、JPAエンティティクラスを設計する
  * 注意: data_model.mdはRDB論理設計（テーブル、カラム、制約）のみ記述
  * JPAエンティティクラスの設計（@Entity, @Column, @ManyToOne等のアノテーション、Java型、リレーションマッピング）はdetailed_design/common/detailed_design.mdで実施

* {spec_directory}/basic_design/common/external_interface.md - 外部API仕様を確認する

* {spec_directory}/basic_design/common/functional_design.md - 共通機能設計（認証、JWT、ログ、エラーハンドリング等）を確認する

* {spec_directory}/basic_design/common/behaviors.md - 共通機能の振る舞い（結合テスト用）を確認する

### 1.4 基本設計の仕様（対象ドメイン）

対象ドメインのSPECを読み込み、ドメインの設計を理解する

* {spec_directory}/basic_design/{target_domain}/functional_design.md - ドメインの機能設計を確認する
  * ドメインの機能概要
  * API機能の設計（エンドポイント、リクエスト/レスポンス等）
  * ドメイン固有のビジネスルール

* {spec_directory}/basic_design/{target_domain}/behaviors.md - ドメインの振る舞い（結合テスト用）を確認する

* {spec_directory}/requirements/behaviors.md - E2Eテスト用の振る舞い（システム全体）を確認する（参考）

注意: 
* commonは最優先で詳細設計を行う（他のドメインはcommonに依存）
* 各ドメインの詳細設計は、common/の詳細設計が完了していることを前提とする
* 詳細設計フェーズで、basic_design/{target_domain}/ から該当部分を抽出して detailed_design/{target_domain}/ を作成する

---

## 2. 理解内容の確認と対話

### 2.1 理解した内容をユーザーに説明

以下の形式で、AIが理解した内容をユーザーに説明する

注意: このセクションでは基本設計SPECから理解した内容の確認を行う。詳細設計で追加すべき実装レベルの情報（クラス名、メソッドシグネチャ等）の確認は「2.2 対話による確認」で実施する。

```markdown
## 理解した内容（基本設計SPECからの確認）

### 対象ドメインの範囲

* ドメイン名: <{target_domain}>
* 実装対象: <Resource/Service/Dao/Entity/DTO等のコンポーネント種別>
* 依存関係: <commonに依存、または他のドメインに依存>

### 基本設計SPECからの情報

* ドメイン概要（functional_design.mdより）: <概要>
* ビジネスルール（functional_design.mdより）: <ルール>
* 関連テーブル（common/data_model.mdより、該当する場合）: <テーブル一覧>
* 外部API連携（common/external_interface.mdより、該当する場合）: <連携内容>

### 詳細設計で追加すべき情報（最小限・簡潔に）

基本設計とコードの橋渡しとなる設計判断のみ、ユーザーと対話しながら決定します：

1. クラス構成: クラス名と責務（1行）
2. 主要メソッド: シグネチャ（引数、戻り値、例外）のみ
3. 設計判断を示すアノテーション: @Transactional, @Path等のみ
4. JPQLクエリ: WHERE句、JOIN等の設計判断のみ
5. 依存関係: @Inject対象の明示

記載しない情報:
- メソッドの実装詳細、処理ステップ
- すべてのフィールド定義、getter/setter
- バリデーションの詳細

### 不明点・確認事項

1. <不明点1>
2. <不明点2>
```

### 2.2 対話による確認

以下の観点で、SPECに明記されていない場合はユーザーに質問する。具体的な確認例は2.3および「6. 非機能要件の確認」を参照。

1. パッケージ構造・実装するコンポーネントのリスト
2. DTO設計（リクエスト/レスポンス形式）
3. ビジネスロジック（バリデーション、計算ロジック、状態遷移）
4. エラーハンドリング（想定されるエラーシナリオ）
5. 外部API連携（エンドポイント、リクエスト/レスポンス形式）
6. トランザクション管理（境界の配置、伝播）
7. セキュリティ実装（認証・認可、JWT、入力検証、ログマスキング、パスワードハッシュ等）
8. パフォーマンス実装（最大件数、ページネーション、タイムアウト、リトライ、キャッシュ、N+1回避等）
9. データ整合性・トランザクション（境界、同時更新制御、論理/物理削除、分散トランザクション等）

### 2.3 質問の原則

質問すべきケース（判断に迷う点）:

1. 複数の実装方法が考えられる場合
   * 例: キャッシュ方式（Redis vs インメモリ）、認証方式（JWT vs セッション）
   * 例: トランザクション境界の配置（Service層 vs Resource層）

2. SPECに明示されていないビジネスルール
   * 例: エラー時のリトライ回数、タイムアウト値
   * 例: データ削除時の論理削除 vs 物理削除
   * 例: バリデーションエラー時の具体的なメッセージ文言

3. エッジケースの扱い
   * 例: null値の扱い、空リストの扱い
   * 例: 同時更新時の制御方法（楽観ロック vs 悲観ロック）

4. 設定値・環境依存の情報
   * 例: JNDI名、接続プール設定、タイムアウト値（SPECに記載がない場合）

5. テストケースの期待値
   * 例: 境界値テストの具体的な入力値と期待結果（SPECに記載がない場合）

6. セキュリティ実装（最優先で確認）— 実装方式は必ず人間が判断。AIは推測で決定しない。
7. パフォーマンス実装（必ず確認）— 業務要件に依存するため推測不可。
8. データ整合性・トランザクション実装（必ず確認）— データ重要度により戦略が異なる。

質問不要なケース（自己判断可能）:

1. SPECに明確に記載されている内容
   * 例: テーブル定義、API仕様、画面レイアウト

2. 技術的な標準・常識
   * 例: REST APIのステータスコード（200, 404, 500等）
   * 例: JPAの基本的なアノテーション使用方法
   * 例: 標準的な命名規則（get/set、find/save等）

3. Agent Skillsルールで明示されている内容
   * 例: アーキテクチャパターン、コーディング規約、セキュリティ標準

4. フレームワークのベストプラクティス
   * 例: Jakarta EEの依存性注入パターン
   * 例: トランザクション管理の標準的な配置

バランスの取れた対話:

* 「これは〜という理解で進めますが、以下の点のみ確認させてください」という形で、明確な点と不明確な点を分けて提示する
* 一度に10項目も20項目も質問するのではなく、本質的な判断ポイントに絞る
* 技術的な実装詳細（SPECから自動的に導出できる内容）は質問しない

### 2.4 不足情報の補完

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
{spec_directory}/detailed_design/{target_domain}/detailed_design.md
```

* 例1: `{project_root}/specs/baseline/detailed_design/common/detailed_design.md`
* 例2: `{project_root}/specs/baseline/detailed_design/orders/detailed_design.md`
* 例3: `{project_root}/specs/baseline/detailed_design/books_proxy/detailed_design.md`

* 記載内容（簡潔に、設計判断のみ）:
  * クラス名と責務（1行）
  * 主要メソッドのシグネチャ（引数、戻り値、例外）のみ
  * 設計判断を示すアノテーション（@Transactional, @Path等）
  * JPQLクエリ（WHERE句、JOIN等の設計判断のみ）
  * 依存関係（@Inject対象）

* 記載しない内容:
  * メソッドの実装詳細、処理ステップ
  * すべてのフィールド定義、getter/setter
  * バリデーションの詳細
  * 基本設計SPECの内容の繰り返し

ドメイン別の設計対象（ドメインにより異なる）:

common/の例（共通ドメイン：最優先実装）:
  * JPAエンティティ（テーブルマッピング、リレーション）
  * Daoクラス（JPQLクエリ）
  * セキュリティコンポーネント（JWT、認証フィルター）

orders/の例（注文管理ドメイン）:
  * Resourceクラス（JAX-RS、エンドポイント）
  * ドメイン固有のDTO（Request、Response）
  * ドメイン固有のService（ビジネスロジック）

books_proxy/の例（書籍API連携ドメイン）:
  * Resourceクラス（JAX-RS、プロキシ転送）
  * 外部API連携クライアント（RestClient）
  * 外部API用DTO

#### 3.1.2 behaviors.md（純粋な単体テスト用の振る舞い）

テンプレート: templates/detailed_design/behaviors.md  
コピー先:
```
{spec_directory}/detailed_design/{target_domain}/behaviors.md
```

* 記載内容:
  * メソッドレベルの単体テストシナリオ（Gherkin 記法: Feature, Scenario, Given, When, Then 等で記述）
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

* behaviors.md の種別と役割は、本ドキュメント冒頭「概要」の「behaviors.mdの種別」を参照する。

重要な注意事項 - 詳細設計書は簡潔に:
* 詳細設計書は、基本設計とコードの「橋渡し」となる設計判断のみを簡潔に記載する
* 後から人が修正する可能性を考慮し、必要最小限の情報のみを記載する
* 実装詳細（処理ステップ、ループ、条件分岐等）は記載しない
* 基本設計SPECに既に記載されている情報は記載しない
* コードから自明な情報（getter/setter、すべてのフィールド定義等）は記載しない
* 以下のテンプレート例は参考構成として提示するものであり、実際の記載は必要最小限にすること

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

\`\`\`java
public String generateToken(Long userId, String email)
\`\`\`

* 目的: <1行で記載>

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

## 4. ドメイン別の設計内容

basic_design/配下のドメインフォルダごとに、以下のような内容を記載します。

重要な原則:
* 基本設計SPEC（basic_design/{target_domain}/）に既に記載されている情報は重複記載しない
* 詳細設計書には実装レベルの情報（クラス名、メソッドシグネチャ、アノテーション等）のみを記載する
* 例: テーブル定義（common/data_model.md）→ JPAエンティティクラスの実装設計（detailed_design/common/detailed_design.md）
* 例: API仕様（{domain}/functional_design.md）→ Resourceクラス、DTOクラスの実装設計（detailed_design/{domain}/detailed_design.md）

### 4.1 記載内容の例

common/の場合（共通ドメイン：最優先実装）:
* 出力先: `{spec_directory}/detailed_design/common/detailed_design.md`
* 記載内容（実装レベルの情報のみ）:
  * ドメインモデル（JPAエンティティ）の実装設計
    * クラス名、パッケージ、JPAアノテーション（@Entity, @Table, @Column等）
    * リレーションマッピング（@ManyToOne, @JoinColumn等）
    * 注意: テーブル定義はcommon/data_model.mdに記載済みのため重複記載しない
  * Daoクラスの実装設計
    * クラス名、メソッドシグネチャ、JPQLクエリ
  * セキュリティコンポーネント（JwtUtil、JwtAuthenFilter等）の実装設計
    * クラス名、メソッドシグネチャ、依存性注入
  * ユーティリティクラスの実装設計
  * 共通例外クラス、Exception Mapperの実装設計
  * 設定情報（MicroProfile Config、persistence.xml等）

orders/の場合（注文管理ドメイン）:
* 出力先: `{spec_directory}/detailed_design/orders/detailed_design.md`
* 記載内容（実装レベルの情報のみ）:
  * Resourceクラス（JAX-RS）の実装設計
    * クラス名、メソッドシグネチャ、JAX-RSアノテーション（@Path, @GET, @POST等）
    * 注意: API仕様（エンドポイント、HTTPメソッド）はorders/functional_design.mdに記載済みのため重複記載しない
  * ドメイン固有のDTOクラス（Request、Response）の実装設計
    * クラス名、フィールド定義、バリデーションアノテーション
  * ドメイン固有のビジネスロジック（Serviceメソッド）の実装設計
    * メソッドシグネチャ、処理フロー
    * トランザクション境界の実装（@Transactionalの配置）
    * 注意: ビジネスルールはorders/functional_design.mdに記載済みのため重複記載しない
  * ドメイン固有のエラーハンドリングの実装設計
  * ドメイン固有の単体テスト要件（behaviors.mdに記載）

books_proxy/の場合（書籍API連携ドメイン）:
* 出力先: `{spec_directory}/detailed_design/books_proxy/detailed_design.md`
* 記載内容（実装レベルの情報のみ）:
  * Resourceクラス（JAX-RS）の実装設計
    * クラス名、メソッドシグネチャ、JAX-RSアノテーション
  * 外部API連携クライアント（RestClient）の実装設計
    * インターフェース名、メソッドシグネチャ、MicroProfile REST Client設定
    * 注意: 外部API仕様はcommon/external_interface.mdに記載済みのため重複記載しない
  * 外部API用DTOクラスの実装設計
    * クラス名、フィールド定義

### 4.2 配置の判断基準（ドメインベース）

| 設計対象 | 配置 | 判断基準 |
|---------|---------|---------|
| JPAエンティティ | common/ | 複数のドメインから依存される |
| Dao | common/ | 複数のドメインから依存される |
| セキュリティ | common/ | 複数のドメインから依存される |
| ユーティリティ | common/ | 複数のドメインから依存される |
| 共通Service | common/ | 複数のドメインから依存される場合 |
| Resource（JAX-RS） | 各ドメイン/ | ドメイン固有のAPI |
| ドメイン固有のDTO | 各ドメイン/ | ドメイン固有のリクエスト/レスポンス |
| ドメイン固有のService | 各ドメイン/ | ドメイン固有のビジネスロジック |
| 外部API連携クライアント | common/ | 複数ドメインから使用される場合、単一ドメインなら各ドメイン/ |

重要: 
* common/は必ず最初に実装する（他のドメインはcommonに依存）
* 各ドメインのコンポーネントは、そのドメインフォルダに配置
* ドメイン固有のコンポーネントは、各ドメインフォルダに配置することで、並行作業が可能

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

### 5.4 詳細設計に含めるべき内容

重要: 基本設計SPECに既に記載されている情報（テーブル定義、API仕様、ビジネスルール等）は詳細設計書に重複記載しない。実装レベルの情報のみを記載する。

* 完全なCRUD実装の場合
  * エンティティの実装設計（クラス名、パッケージ、JPAアノテーション、リレーションマッピング）
    * 注意: テーブル定義（テーブル名、カラム名、型）はdata_model.mdに記載済みのため重複記載しない
  * Daoの実装設計（クラス名、メソッドシグネチャ、JPQLクエリ）
  * Serviceの実装設計（クラス名、メソッドシグネチャ、依存性注入）
    * 注意: ビジネスルールはfunctional_design.mdに記載済みのため重複記載しない
  * トランザクション管理の実装（@Transactionalの配置、伝播設定）

* プロキシ転送のみの場合
  * Resourceの実装設計（クラス名、メソッドシグネチャ、JAX-RSアノテーション）
    * 注意: API仕様（エンドポイント、HTTPメソッド）はfunctional_design.mdに記載済みのため重複記載しない
  * RestClientの実装設計（インターフェース名、メソッドシグネチャ、MicroProfile REST Client設定）
    * 注意: 外部API仕様はexternal_interface.mdに記載済みのため重複記載しない
  * 外部API用DTOの実装設計（クラス名、フィールド定義）
  * 「実装されていないクラス」（Entity、Dao等）を明記

* 独自実装 + 外部連携の場合
  * 自プロジェクトで管理するコンポーネントの実装設計（上記「完全なCRUD実装」を参照）
  * 外部API連携クライアントの実装設計（上記「プロキシ転送のみ」を参照）
  * 分散トランザクションの実装設計（2フェーズコミット、Saga等の実装方式）
  * エラーハンドリングの実装設計（例外クラス、ExceptionMapperの設計）

---

## 6. 非機能要件の確認（セキュリティ・パフォーマンス・データ整合性）

セキュリティ・パフォーマンス・データ整合性に関する実装判断で、SPECに明記されていないものは必ずユーザーに確認する。AIは推測で決定しない。

* 確認すべき観点・カテゴリ: @agent_skills/jakarta-ee-api-base/principles/architecture.md の「11.5 非機能要件の確認原則（詳細設計時）」を参照
* 対応方針: SPECに明記されていればそのまま反映。明記されていなければ選択肢と推奨を提示し、ユーザーに判断を仰ぐ

---

## 7. 実装チェックリスト

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

## 8. 注意事項

### 基本設計SPECとの重複回避（最重要原則）

詳細設計書は、基本設計SPECを補完するものであり、既存情報を重複記載しない。

#### 重複して記載しない情報

基本設計SPECに既に記載されている以下の情報は、詳細設計書に重複記載しない：

1. API概要・目的（functional_design.mdに記載）
   * APIの目的、対象ユーザー、ベースパス等の概要情報
   * エンドポイント一覧、HTTPメソッド、パス、概要

2. ビジネスルール（functional_design.mdに記載）
   * ビジネスロジックの要件、バリデーションルール
   * 状態遷移、計算ロジックの仕様

3. テーブル定義（data_model.mdに記載）
   * テーブル名、カラム名、データ型、制約
   * リレーションシップ、外部キー制約

4. 外部API仕様（external_interface.mdに記載）
   * 外部APIのエンドポイント、リクエスト/レスポンス構造

#### 詳細設計で追加すべき情報（実装レベル）

詳細設計書には、基本設計SPECに存在しない以下の実装レベル情報のみを記載する：

1. 実装クラス設計
   * クラス名（BookResource、BookService、BookDao等）
   * パッケージ名（com.example.api.books等）
   * クラスのアノテーション（@Path, @ApplicationScoped, @Entity等）

2. メソッド設計
   * メソッド名（findById、save、delete等）
   * メソッドシグネチャ（引数型、戻り値型、例外）
   * メソッドのアノテーション（@GET, @POST, @Transactional等）

3. DTO設計
   * DTOクラス名（BookRequest、BookResponse等）
   * DTOフィールド（型、バリデーションアノテーション）
   * Record形式の定義

4. JPAエンティティ設計
   * エンティティクラス名（Book、Author等）
   * フィールド名とJava型（Long id、String title等）
   * JPAアノテーション（@Entity, @Table, @Column, @ManyToOne等）
   * リレーションマッピング（@JoinColumn, cascade設定等）

5. データアクセス設計
   * JPQLクエリ（SELECT, JOIN, WHERE句等）
   * Criteria API設計
   * NamedQueryの定義

6. 依存性注入設計
   * @Inject、@Named等の配置
   * CDIスコープ（@ApplicationScoped、@RequestScoped等）

7. 実装固有の処理フロー
   * 具体的なメソッド呼び出しシーケンス
   * 例外ハンドリングの実装方法
   * トランザクション境界の配置

#### 記載方法の指針

* 基本設計SPECの情報を前提として、実装レベルの詳細のみを補完する
* 例: テーブル定義（data_model.md）→ JPAエンティティクラス設計（detailed_design.md）
* 例: API仕様（functional_design.md）→ Resourceクラス、DTOクラス設計（detailed_design.md）
* 例: ビジネスルール（functional_design.md）→ Serviceメソッド実装設計（detailed_design.md）

### SPECの優先順位

詳細が矛盾する場合、以下の優先順位で判断する

1. {spec_directory}/basic_design/functional_design.md（最優先）
2. {spec_directory}/detailed_design/FUNC_XXX/detailed_design.md、behaviors.md
3. {spec_directory}/basic_design/architecture_design.md
4. ベースライン仕様（拡張機能の場合、system配下が存在しない場合）

### 不明点の扱い

「2.3 質問の原則」に従う。原則: 判断に迷う点は質問、明確な点は自己判断。推測や仮定で詳細設計を作成しない。詳細設計の精度がコード生成の品質を左右するため、不明点は必ずユーザーに確認する。

### 実装範囲の判定

「5. SPECからの実装範囲判定」を参照する。data_model.md（ERDの有無）→ Entity/Dao/Service の要否、external_interface.md → RestClient の要否。拡張機能で system 配下が存在しない場合はベースライン仕様を参照する。

### 既存のdetailed_design.mdの扱いと反復的なブラッシュアップ

詳細設計は一度で完璧になることはない。以下のタイミングで更新が必要:

#### 更新が必要なケース

1. コード生成時に設計の不整合を発見
2. 単体テスト実装時に設計の不足を発見
3. テスト実行時に設計の誤りを発見
4. カバレッジ分析で不足やデッドコードを発見
5. レビュー時に改善点を発見

#### すでに detailed_design/{target_domain}/detailed_design.md が存在する場合

1. 必須: 既存のdetailed_design.mdを読み込む
2. 分析: 現在の設計内容を理解する
3. ユーザー確認: 以下を確認する
   ```
   {target_domain}の詳細設計書が既に存在します
   
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

## 最重要原則の再確認

詳細設計書を作成する際は、以下の原則を必ず守ること：

### 簡潔性の原則
* 詳細設計書は「橋渡し情報」のみ簡潔に記載する
* 後から人が修正する可能性を考慮し、必要最小限の情報のみを記載する
* ソースコードに近い詳細な情報は記載しない

### 記載すべき情報（最小限）
- ✅ クラス名と責務（1行）
- ✅ 主要メソッドのシグネチャ（引数、戻り値、例外）
- ✅ 設計判断を示すアノテーション（@Transactional, @Path等）
- ✅ JPQLクエリ（WHERE句、JOIN等の設計判断）
- ✅ 依存関係（@Inject対象）

### 記載すべきでない情報
- ❌ メソッドの実装詳細、処理ステップ、ループ、条件分岐
- ❌ すべてのフィールド定義、getter/setter
- ❌ バリデーションの詳細
- ❌ 基本設計SPECの内容の繰り返し
- ❌ コードから自明な情報

---

## 参考資料

* [basic_design.md](basic_design.md) - 基本設計（前工程）
* [code_generation.md](code_generation.md) - コード生成（次工程）
* [Jakarta EE 10仕様](https://jakarta.ee/specifications/)
* [JPA仕様](https://jakarta.ee/specifications/persistence/3.1/)
* [JAX-RS仕様](https://jakarta.ee/specifications/restful-ws/3.1/)
