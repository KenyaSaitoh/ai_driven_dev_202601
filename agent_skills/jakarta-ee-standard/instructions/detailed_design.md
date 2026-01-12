# 詳細設計インストラクション

## パラメータ設定

実行前に以下のパラメータを設定してください:

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
api_id: "ここに対象APIのIDを入力（例: API_001_auth）"
```

* 例:
```yaml
project_root: "projects/sdd/bookstore/back-office-api-sdd"
api_id: "API_002_books"
```

注意: 
* パス区切りはOS環境に応じて調整してください（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換えてください
* `{api_id}` は対象APIのID（例: API_001_auth, API_002_books）に置き換えてください
* アーキテクチャパターン（マイクロサービス/BFF）は仕様書から自動判定します

---

## 概要

このインストラクションは、仕様書（functional_design.md、behaviors.md、architecture_design.md）からAPI機能の詳細設計書（detailed_design.md）を生成するためのものです。

重要な方針:
* AIが仕様を理解し、人と対話しながら妥当性・充足性を確認
* 不足情報を補いながら詳細設計を作成
* 仕様書は抽象的、detailed_design.mdは具体的
* 推測せず、不明点は必ずユーザーに質問
* アーキテクチャパターンは仕様書から判断する（パラメータ指定不要）

出力先:
* `{project_root}/specs/baseline/api/{api_id}/detailed_design.md`

---

## 1. 仕様書の読み込みと理解

パラメータで指定されたプロジェクト情報に基づいて、以下の設計ドキュメントを読み込んで分析してください。

注意: `{project_root}` および `{api_id}` は、パラメータで指定された値に置き換えてください。

### 1.1 Agent Skills憲章（最優先で確認）

* @agent_skills/jakarta-ee-standard/principles/ - Jakarta EE開発の共通原則、アーキテクチャ方針、品質基準を確認
  * `constitution.md` - Jakarta EE開発憲章
  * その他、組織やチームで共通的に使われる憲章ファイル
  * 重要: 詳細設計においても、憲章に記載された原則（命名規則、設計パターン、コーディング規約など）を遵守すること
  * 注意: Agent Skills配下の憲章は全プロジェクト共通。プロジェクト固有の憲章がある場合は `{project_root}/principles/` も確認すること

### 1.2 システムレベルの仕様

以下のファイルを読み込み、システム全体の設計を理解してください：

* architecture_design.md - 技術スタック、パッケージ構造を確認
  * ベースパッケージ（例: `pro.kensait.backoffice`）
  * パッケージ階層の規約
  * 使用技術スタック（Jakarta EE 10、JPA、JAX-RS等）
  * セキュリティ方式（JWT、認証フィルター等）

* functional_design.md - システム全体の機能設計概要を確認

* data_model.md - エンティティとデータベーススキーマを確認
  * 対象エンティティのテーブル定義
  * フィールド、型、制約
  * リレーション（@ManyToOne、@OneToMany等）
  * 楽観的ロック（@Version）の有無

### 1.3 対象APIの仕様

以下のファイルを読み込み、対象APIの詳細を理解してください：

* api/{api_id}/functional_design.md - API機能設計書
  * APIのベースパス（例: `/api/books`）
  * エンドポイント一覧（メソッド、パス、機能）
  * リクエスト/レスポンス形式
  * ビジネスルール

* api/{api_id}/behaviors.md - API振る舞い仕様書
  * エラーケース
  * 外部API連携の有無（BFFの場合）
  * 受入基準

---

## 2. 理解内容の確認と対話

### 2.1 理解した内容をユーザーに説明

以下の形式で、AIが理解した内容をユーザーに説明してください：

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

### 外部API連携（BFFの場合）
* <外部API名>: <連携内容>

### 不明点・確認事項
1. <不明点1>
2. <不明点2>
```

### 2.2 対話による確認

ユーザーに質問すべき項目:

1. パッケージ構造
   * 「このAPIで実装するクラスは以下でよろしいでしょうか？」
   * Resource、Service、Dao、Entity、DTOのリスト

2. DTO設計
   * 「リクエスト/レスポンスのDTOは以下の形式でよろしいでしょうか？」
   * Record型を使用するか、POJOを使用するか

3. ビジネスロジック
   * 「以下のビジネスルールの理解は正しいでしょうか？」
   * バリデーション、計算ロジック、状態遷移等

4. エラーハンドリング
   * 「以下のエラーケースで漏れはないでしょうか？」
   * 想定されるエラーシナリオ

5. 外部API連携（BFFの場合）
   * 「外部APIの呼び出しは以下の理解で正しいでしょうか？」
   * エンドポイント、リクエスト/レスポンス形式

6. トランザクション管理
   * 「トランザクション境界は以下でよろしいでしょうか？」
   * @Transactionalの配置場所

### 2.3 不足情報の補完

ユーザーからのフィードバックを受けて、以下を補完してください：

* 仕様書に記載されていない実装詳細
* クラス間の依存関係
* メソッドシグネチャの詳細
* エラーメッセージの文言
* 設定情報（MicroProfile Config）

---

## 3. detailed_design.mdの生成

### 3.1 出力先

以下の場所に`detailed_design.md`を作成してください：

```
{project_root}/specs/baseline/api/{api_id}/detailed_design.md
```

### 3.2 詳細設計書のテンプレート

```markdown
# {api_id} <API名> - 詳細設計書

* API ID: {api_id}  
* API名: <API名>  
* バージョン: 1.0.0  
* 最終更新: <日付>

---

## 1. パッケージ構造

### 1.1 関連パッケージ

\`\`\`
<ベースパッケージ>
├── api
│   ├── <Resource名>.java
│   ├── dto
│   │   ├── <Request名>.java
│   │   └── <Response名>.java
│   └── exception
│       └── <ExceptionMapper名>.java
├── service
│   └── <パッケージ>
│       └── <Service名>.java
├── dao
│   └── <Dao名>.java
└── entity
    └── <Entity名>.java
\`\`\`

* 注: <BFFの場合の特記事項>

---

## 2. クラス設計

### 2.1 <Resource名>（JAX-RS Resource）

* 責務: <責務の説明>

* アノテーション:
  * \`@Path("<パス>")\` - ベースパス
  * \`@ApplicationScoped\` - CDIスコープ

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

### 2.2 <Request/Response名>（DTO - Record）

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
  * \`@ApplicationScoped\`

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
  * \`@ApplicationScoped\`

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

## 3. パターン固有の実装

（マイクロサービスの場合: 検索実装の比較、楽観的ロック等）
（BFFの場合: 外部API連携、プロキシパターン、分散トランザクション等）

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

### 6.2 統合テスト

* 対象: <対象の説明>

* <テストケース1>
* <テストケース2>

---

## 7. 参考資料

* [functional_design.md](functional_design.md) - 機能設計書
* [behaviors.md](behaviors.md) - 振る舞い仕様書
```

---

## 4. 仕様書からの実装範囲判定

AIは以下の情報から、実装すべきクラスを判断してください：

### 4.1 data_model.mdの確認

* エンティティ定義がある場合:
  * ✅ Entity実装が必要
  * ✅ Dao実装が必要
  * ✅ Service実装が必要
  * ✅ トランザクション管理（@Transactional）
  * ✅ 楽観的ロック（@Versionがあれば）

* エンティティ定義がない場合:
  * ❌ Entity, Dao, Service不要

### 4.2 external_interface.mdの確認

* 外部API定義がある場合:
  * ✅ RestClient実装が必要
  * ✅ 外部API用DTO定義
  * ✅ エラーハンドリング（接続エラー、タイムアウト）

* 外部API定義がない場合:
  * ❌ RestClient不要

### 4.3 実装パターンの判定

| data_model.md | external_interface.md | 実装パターン | 例 |
|--------------|---------------------|----------|---|
| ❌ なし | ❌ なし | 静的リソース配信 | 画像API |
| ❌ なし | ✅ あり | プロキシ転送のみ | 書籍API（BFF） |
| ✅ あり | ❌ なし | 完全なCRUD実装 | 書籍API（マイクロサービス） |
| ✅ あり | ✅ あり | 独自実装 + 外部連携 | 注文API（BFF） |

### 4.4 詳細設計に含めるべき内容

* 完全なCRUD実装の場合:
  * エンティティの完全な定義（@Entity、@Table、フィールド、リレーション）
  * Daoの実装（JPQL、Criteria API）
  * Serviceのビジネスロジック
  * トランザクション管理（@Transactional）

* プロキシ転送のみの場合:
  * Resourceの実装（プロキシ転送）
  * RestClientの実装（外部API呼び出し）
  * 外部API用DTOの定義
  * 「実装されていないクラス」を明記（Entity, Dao, Service）

* 独自実装 + 外部連携の場合:
  * 自プロジェクトで管理するEntity、Dao、Service
  * 外部API連携用のRestClient
  * 分散トランザクションの考慮（結果整合性、Saga）
  * エラーハンドリング（ロールバック戦略）

---

## 5. 実装チェックリスト

詳細設計書を作成する前に、以下を確認してください：

### 仕様理解の確認
* [ ] APIの目的と機能を理解している
* [ ] エンドポイント一覧を把握している
* [ ] リクエスト/レスポンス形式を理解している
* [ ] ビジネスルールを理解している
* [ ] エラーケースを把握している

### パッケージ構造の確認
* [ ] ベースパッケージを確認した
* [ ] 命名規則を確認した
* [ ] アーキテクチャパターンを確認した
* [ ] 実装が必要なクラスをリストアップした
* [ ] 実装が不要なクラス（BFFの場合）を確認した

### データモデルの確認
* [ ] エンティティのテーブル定義を確認した
* [ ] フィールド、型、制約を確認した
* [ ] リレーションを確認した
* [ ] 楽観的ロックの有無を確認した

### 外部API連携の確認（BFFの場合）
* [ ] 外部APIのエンドポイントを確認した
* [ ] リクエスト/レスポンス形式を確認した
* [ ] エラーハンドリングを確認した

### 対話による確認
* [ ] ユーザーに理解内容を説明した
* [ ] 不明点をユーザーに質問した
* [ ] ユーザーからのフィードバックを反映した
* [ ] 不足情報を補完した

---

## 6. 注意事項

### 仕様書の優先順位

詳細が矛盾する場合、以下の優先順位で判断してください：

1. API固有のfunctional_design.md（最優先）
2. API固有のbehaviors.md
3. systemレベルのarchitecture_design.md
4. systemレベルのfunctional_design.md

### 不明点の扱い

不明点がある場合は、必ずユーザーに質問してください。

推測で詳細設計を作成しないでください。

### 実装範囲の判定

仕様書から以下を確認してください：

* data_model.md: エンティティ定義の有無 → Entity/Dao/Service実装の必要性
* external_interface.md: 外部API定義の有無 → RestClient実装の必要性
* functional_design.md: エンドポイント定義 → Resource実装の必要性

「マイクロサービス」「BFF」といったラベルに依存せず、仕様書の内容から判断してください。

### 既存のdetailed_design.mdの扱い

すでにdetailed_design.mdが存在する場合：
* ユーザーに「既存のファイルを上書きしますか？」と確認
* 上書きの場合は、既存の内容を読んで良い部分を継承
* 追記の場合は、不足セクションのみを追加

---

## 参考資料

* [task_breakdown.md](task_breakdown.md) - タスク分解（前工程）
* [code_generation.md](code_generation.md) - コード生成（次工程）
* [Jakarta EE 10仕様](https://jakarta.ee/specifications/)
* [JPA仕様](https://jakarta.ee/specifications/persistence/3.1/)
* [JAX-RS仕様](https://jakarta.ee/specifications/restful-ws/3.1/)
