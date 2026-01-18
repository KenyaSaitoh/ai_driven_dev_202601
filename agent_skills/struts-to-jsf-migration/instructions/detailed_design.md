# 詳細設計インストラクション（JSF画面単位）

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
target_type: "ここに対象画面のIDを入力（例: FUNC_001_PersonList）"
```

* 例1: ベースライン（初回リリース版）
```yaml
project_root: "projects/sdd/person/jsf-person-sdd"
spec_directory: "projects/sdd/person/jsf-person-sdd/specs/baseline"
target_type: "FUNC_001_PersonList"
```

* 例2: 拡張機能（エンハンスメント）
```yaml
project_root: "projects/sdd/person/jsf-person-sdd"
spec_directory: "projects/sdd/person/jsf-person-sdd/specs/enhancements/202512_person_search"
target_type: "FUNC_005_PersonSearch"
```

注意
* パス区切りはOS環境に応じて調整する（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換える
* 以降、`{spec_directory}` と表記されている箇所は、上記で設定した値に置き換える
* `{target_type}` は対象画面のIDに置き換える

---

## 概要

このインストラクションは、基本設計SPEC（basic_design/）から画面の詳細設計書を生成するためのものである

重要な方針
* basic_design/functional_design.md、screen_design.mdを参照して、実装レベルのdetailed_design.mdを作成する
* 純粋な単体テスト用のbehaviors.mdを新規作成する（結合テスト用のbasic_design/behaviors.mdとは別物）
* AIが仕様を理解し、人と対話しながら妥当性・充足性を確認する
* 不足情報を補いながら詳細設計を作成する
* 推測せず、不明点は必ずユーザーに質問する

生成するファイル
* `{spec_directory}/detailed_design/detailed_design/FUNC_XXX/detailed_design.md` - 実装クラス設計（Managed Bean、画面遷移等）
* `{spec_directory}/detailed_design/detailed_design/FUNC_XXX/behaviors.md` - 純粋な単体テスト用の振る舞い

behaviors.mdの違い:
* basic_design/behaviors.md: システム全体の振る舞い（E2Eテスト用）
  * 画面間遷移、複数画面にまたがるエンドツーエンドのフロー
  * 実際のDBアクセス、画面レンダリングを含む
  * Playwrightを使用したブラウザ操作テスト
  
* detailed_design/detailed_design/FUNC_XXX/behaviors.md: タスク粒度内の単体テスト用の振る舞い
  * タスク内のコンポーネント間の連携をテスト
  * タスク内のコンポーネントはモック不要（実際の連携をテスト）
  * タスク外の依存関係のみモックを使用
  * 例: PersonListBeanとPersonServiceが同じタスクなら、両方とも実インスタンスでテスト（EntityManagerのみモック）

注意
* ベースライン（初回リリース版）の場合: `{project_root}/specs/baseline/detailed_design/detailed_design/FUNC_XXX/`
* 拡張機能の場合: `{project_root}/specs/enhancements/[拡張名]/detailed_design/detailed_design/FUNC_XXX/`
* `{spec_directory}` パラメータで柔軟に指定可能

---

## 1. SPECの読み込みと理解

パラメータで指定されたプロジェクト情報に基づいて、以下の設計ドキュメントを読み込んで分析してください。

注意: `{project_root}`, `{spec_directory}`, `{target_type}` は、パラメータで指定された値に置き換えてください。

### 1.1 Agent Skillsルール（最優先で確認）

* @agent_skills/struts-to-jsf-migration/principles/ - マイグレーションルール、アーキテクチャ標準、品質基準、セキュリティ標準を確認する
  * このフォルダ配下の原則ドキュメントを読み込み、共通ルールを遵守すること
  * 重要: 詳細設計においても、ルールドキュメントに記載されたすべてのルールを遵守すること
  * 注意: Agent Skills配下のルールは全プロジェクト共通。プロジェクト固有のルールがある場合は `{project_root}/principles/` も確認すること

### 1.2 フレームワーク仕様（該当する場合）

* @agent_skills/struts-to-jsf-migration/frameworks/ - フレームワーク固有のSPECやサンプルコードを確認する
* @agent_skills/jakarta-ee-api-base/frameworks/ - フレームワーク固有のSPECやサンプルコードを確認する
  * 特定のフレームワーク（ライブラリ、ツール等）の使用方法、設計パターン、実装例を参照する
  * 詳細設計時に、フレームワーク仕様に従った設計を行う

### 1.3 システムレベルの仕様

以下のファイルを読み込み、システム全体の設計を理解してください：

* {spec_directory}/basic_design/architecture_design.md - 技術スタック、パッケージ構造を確認
  * ベースパッケージ（例: `pro.kensait.jsf.person`）
  * パッケージ階層の規約
  * 使用技術スタック（Jakarta EE 10、JPA、JSF等）
  * セッション管理方針（ViewScoped、Flash Scope等）
  * データソース設定セクションでJNDI名を確認（Service実装時に参照）

* {spec_directory}/basic_design/functional_design.md - システム全体の機能設計概要を確認
  * 画面遷移図
  * 画面間のデータ受け渡し方式

* {spec_directory}/basic_design/data_model.md - テーブル定義（ERD）からJPAエンティティクラスを設計
  * テーブル定義（カラム、型、制約）からフィールドをマッピング
  * テーブル間のリレーションシップから@ManyToOne、@OneToMany等を設計
  * 主キー、複合主キーから@Id、@EmbeddedId等を設計

注意: data_model.mdはRDB論理設計のみ。JPAエンティティクラスは詳細設計フェーズでERDから設計します

注意: 拡張機能の場合、システムレベルの仕様が存在しない場合がある。その場合はベースラインの仕様を参照する

### 1.4 対象画面の仕様

以下のファイルを読み込み、対象画面の詳細を理解してください：

* {spec_directory}/detailed_design/detailed_design/FUNC_XXX/screen_design.md - 画面設計書
  * 画面レイアウト（テーブル、フォーム、ボタン等）
  * 入力項目（項目名、型、必須/任意、バリデーション）
  * 表示データ（一覧表示、詳細表示等）

* {spec_directory}/detailed_design/detailed_design/FUNC_XXX/functional_design.md - 画面機能設計書
  * Managed Bean設計（Bean名、スコープ、プロパティ、アクションメソッド）
  * Service設計（メソッドシグネチャ、ビジネスロジック）
  * データアクセス設計（Entity、JPQL）

* {spec_directory}/detailed_design/detailed_design/FUNC_XXX/behaviors.md - 画面振る舞い仕様書
  * 画面の振る舞い（初期表示、ボタンクリック、バリデーションエラー等）
  * エラーケース
  * Given-When-Thenシナリオ

---

## 2. 理解内容の確認と対話

### 2.1 理解した内容をユーザーに説明

以下の形式で、AIが理解した内容をユーザーに説明してください：

```markdown
## 理解した内容

### 画面概要

* 画面ID: <画面ID>
* 画面名: <画面名>
* URL: <アクセスURL>
* 目的: <画面の目的>

### 画面レイアウト

* 表示項目: <表示項目の一覧>
* 入力項目: <入力項目の一覧>
* ボタン: <ボタンの一覧と遷移先>

### Managed Bean

* Bean名: <Bean名>
* スコープ: <@ViewScoped等>
* プロパティ: <プロパティ一覧>
* アクションメソッド: <メソッド一覧と遷移先>

### Service

* Service名: <Service名>
* メソッド: <メソッド一覧>

### 関連エンティティ

* <エンティティ名>（テーブル: <テーブル名>）
  * 主要フィールド: <フィールド一覧>

### 画面遷移

* <遷移元> → <遷移先> - <条件>

### 不明点・確認事項

1. <不明点1>
2. <不明点2>
```

### 2.2 対話による確認

ユーザーに質問すべき項目:

1. パッケージ構造
   * 「この画面で実装するクラスは以下でよろしいでしょうか？」
   * Managed Bean、Service、Entity、DTOのリスト

2. Managed Bean設計
   * 「Managed Beanのスコープは以下でよろしいでしょうか？」
   * `@ViewScoped`, `@RequestScoped`, `@SessionScoped`
   * 「プロパティとアクションメソッドは以下でよろしいでしょうか？」

3. 画面レイアウト
   * 「画面の表示項目とレイアウトは以下の理解でよろしいでしょうか？」
   * テーブル、フォーム、ボタンの配置

4. バリデーション
   * 「バリデーションルールは以下でよろしいでしょうか？」
   * Bean Validationアノテーション、カスタムバリデーション

5. 画面遷移
   * 「画面遷移は以下の理解でよろしいでしょうか？」
   * 遷移先画面、Flash Scopeでのデータ受け渡し

6. ビジネスロジック
   * 「以下のビジネスルールの理解は正しいでしょうか？」
   * バリデーション、計算ロジック、状態遷移等

7. エラーハンドリング
   * 「以下のエラーケースで漏れはないでしょうか？」
   * 想定されるエラーシナリオ

8. セッション管理
   * 「画面間のデータ受け渡しは以下でよろしいでしょうか？」
   * Flash Scope、Session Scope、リクエストパラメータ

9. データソース設定
   * 「persistence.xmlで使用するJNDI名は以下でよろしいでしょうか？」
   * architecture_design.mdのデータソース設定セクションまたはpersistence.xml設定情報セクションから抽出したJNDI名を確認
   * 移行元で実際に使用されているJNDI名であることを確認

### 2.3 不足情報の補完

ユーザーからのフィードバックを受けて、以下を補完してください：

* SPECに記載されていない実装詳細
* クラス間の依存関係
* メソッドシグネチャの詳細
* エラーメッセージの文言
* バリデーションメッセージの文言

---

## 3. detailed_design.mdの生成

### 3.1 出力先

以下の場所に`detailed_design.md`を作成してください：

```
{spec_directory}/detailed_design/detailed_design/FUNC_XXX/detailed_design.md
```

* ベースラインの例: `{project_root}/specs/baseline/detailed_design/detailed_design/FUNC_001_PersonList/detailed_design.md`
* 拡張機能の例: `{project_root}/specs/enhancements/202512_person_search/detailed_design/FUNC_005_PersonSearch/detailed_design.md`

### 3.2 詳細設計書のテンプレート

```markdown
# {target_type} <画面名> - 詳細設計書

* 画面ID: {target_type}  
* 画面名: <画面名>  
* バージョン: 1.0.0  
* 最終更新: <日付>

---

## 1. パッケージ構造

### 1.1 関連パッケージ

\`\`\`
<ベースパッケージ>
├── bean
│   └── <ManagedBean名>.java       # JSF Managed Bean
├── entity
│   └── <Entity名>.java            # JPA Entity
├── service
│   └── <Service名>.java           # CDI Service
└── model (オプション)
    └── <DTO名>.java               # 画面間データ受け渡し用DTO
\`\`\`

---

## 2. クラス設計

### 2.1 <ManagedBean名>（JSF Managed Bean）

* 責務: <責務の説明>

* アノテーション:
  * \`@Named("<Bean名>")\` - JSF Managed Bean名
  * \`@ViewScoped\` - ビュースコープ（画面表示中のみ有効）

* プロパティ:

| プロパティ名 | 型 | 説明 |
|------------|---|------|
| \`<プロパティ名>\` | \`<型>\` | <説明> |

* アクションメソッド:

#### <メソッド名>() - <機能名>

* シグネチャ:
\`\`\`java
public String <メソッド名>()
\`\`\`

* 処理フロー:
  1. <ステップ1>
  2. <ステップ2>
  3. <ステップ3>

* 戻り値: \`"<画面ID>"\` - 遷移先画面

* エラーケース:
  * <エラーケース1> → <処理>

---

### 2.2 <Service名>（ビジネスロジック層）

* 責務: <責務の説明>

* アノテーション:
  * \`@RequestScoped\`
  * \`@Transactional(TxType.REQUIRED)\`

* 主要メソッド:

#### <メソッド名>()

* シグネチャ:
\`\`\`java
public <戻り値型> <メソッド名>(<引数>)
\`\`\`

* 処理:
  1. <ステップ1>
  2. <ステップ2>

* JPQL:
\`\`\`sql
<JPQLクエリ>
\`\`\`

---

### 2.3 <Entity名>（エンティティ）

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

* Bean Validation:
  * \`@NotNull\`, \`@Size\`, \`@Min\`, \`@Max\` 等

---

## 3. 画面設計

### 3.1 Facelets XHTML構造

* ファイル名: \`<画面名>.xhtml\`

* 主要コンポーネント:
  * \`<h:form>\` - メインフォーム
  * \`<h:dataTable>\` - データテーブル（一覧表示）
  * \`<h:inputText>\` - テキスト入力
  * \`<h:commandButton>\` - ボタン

* レイアウト:
\`\`\`xhtml
<例>
<h:form>
    <h:dataTable value="#{bean.list}" var="item">
        <h:column>
            <f:facet name="header">項目名</f:facet>
            <h:outputText value="#{item.property}"/>
        </h:column>
    </h:dataTable>
    
    <h:commandButton value="ボタン" action="#{bean.action}"/>
</h:form>
\`\`\`

---

## 4. 画面遷移

### 4.1 遷移パターン

| 遷移元 | 遷移先 | 条件 | データ受け渡し |
|-------|-------|-----|--------------|
| <画面名> | <遷移先画面名> | <条件> | <Flash Scope/Session Scope> |

### 4.2 Flash Scopeでのデータ受け渡し

* 送信側（遷移元）:
\`\`\`java
flash.put("key", value);
return "遷移先画面ID";
\`\`\`

* 受信側（遷移先）:
\`\`\`java
Object value = flash.get("key");
\`\`\`

---

## 5. バリデーション

### 5.1 Bean Validation

| フィールド | バリデーション | メッセージ |
|----------|--------------|----------|
| \`<フィールド名>\` | \`@NotNull\` | <メッセージ> |
| \`<フィールド名>\` | \`@Size(max=100)\` | <メッセージ> |

### 5.2 カスタムバリデーション

* メソッド名: \`<メソッド名>\`
* 検証内容: <検証内容>
* エラー時の処理: <処理>

---

## 6. エラーハンドリング

### 6.1 エラーシナリオ

| エラーケース | 処理 | メッセージ |
|------------|-----|----------|
| <エラー1> | <処理> | <メッセージ> |

### 6.2 エラーメッセージ表示

* Faceletsでの表示:
\`\`\`xhtml
<h:messages globalOnly="true"/>
<h:message for="inputId"/>
\`\`\`

* Managed Beanからのメッセージ追加:
\`\`\`java
FacesContext.getCurrentInstance().addMessage(null, 
    new FacesMessage(FacesMessage.SEVERITY_ERROR, "エラーメッセージ", null));
\`\`\`

---

## 7. テスト要件

### 7.1 ユニットテスト

* 対象: \`<クラス名>\`

* <テストケース1>
* <テストケース2>

### 7.2 画面テスト

* 対象: <画面名>

* <テストケース1>
* <テストケース2>

---

## 8. 参考資料

* [screen_design.md](screen_design.md) - 画面設計書
* [functional_design.md](functional_design.md) - 機能設計書
* [behaviors.md](behaviors.md) - 振る舞い仕様書
```

---

## 4. 実装チェックリスト

詳細設計書を作成する前に、以下を確認してください：

### 仕様理解の確認

* [ ] 画面の目的と機能を理解している
* [ ] 表示項目と入力項目を把握している
* [ ] ボタンと画面遷移を把握している
* [ ] ビジネスルールを理解している
* [ ] エラーケースを把握している

### パッケージ構造の確認

* [ ] ベースパッケージを確認した
* [ ] 命名規則を確認した
* [ ] 実装が必要なクラスをリストアップした

### Managed Bean設計の確認

* [ ] Bean名とスコープを確認した
* [ ] プロパティ一覧を確認した
* [ ] アクションメソッド一覧を確認した
* [ ] Flash Scopeでのデータ受け渡しを確認した

### データモデルの確認

* [ ] data_model.mdのテーブル定義（ERD）を確認した
* [ ] テーブルのカラム、型、制約からJPAエンティティフィールドを設計した
* [ ] テーブル間のリレーションシップからJPAアノテーション（@OneToMany等）を設計した

### 対話による確認

* [ ] ユーザーに理解内容を説明した
* [ ] 不明点をユーザーに質問した
* [ ] ユーザーからのフィードバックを反映した
* [ ] 不足情報を補完した

---

## 5. 注意事項

### SPECの優先順位

詳細が矛盾する場合、以下の優先順位で判断してください：

1. {spec_directory}/detailed_design/detailed_design/FUNC_XXX/screen_design.md（最優先）
2. {spec_directory}/detailed_design/detailed_design/FUNC_XXX/functional_design.md
3. {spec_directory}/detailed_design/detailed_design/FUNC_XXX/behaviors.md
4. {spec_directory}/basic_design/architecture_design.md
5. {spec_directory}/basic_design/functional_design.md
6. ベースライン仕様（拡張機能の場合、system配下が存在しない場合）

### 不明点の扱い

不明点がある場合は、必ずユーザーに質問してください。

推測で詳細設計を作成しないでください。

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
* screen配下には拡張機能固有の画面仕様のみが存在する

---

## 参考資料

* [マイグレーションルール](../principles/) - マッピング規則、マイグレーションルール
* [共通ルール](../../jakarta-ee-api-base/principles/) - 共通ルール
* [task_breakdown.md](task_breakdown.md) - タスク分解（前工程）
* [code_generation.md](code_generation.md) - コード生成（次工程）
* [Jakarta EE 10仕様](https://jakarta.ee/specifications/)
* [Jakarta Server Faces 4.0](https://jakarta.ee/specifications/faces/4.0/)
* [Jakarta Persistence (JPA) 3.1](https://jakarta.ee/specifications/persistence/3.1/)
