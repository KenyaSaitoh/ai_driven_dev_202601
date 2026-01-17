# タスク分解インストラクション（JSF画面単位）

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
output_directory: "ここにタスク出力先のパスを入力（オプション）"
```

* 例
```yaml
project_root: "projects/sdd/person/jsf-person-sdd"
spec_directory: "projects/sdd/person/jsf-person-sdd/specs"
output_directory: "projects/sdd/person/jsf-person-sdd/tasks"
```

注意
* パス区切りはOS環境に応じて調整する（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換える

---

## 概要

このインストラクションは、完成したSPECから複数人が並行して作業できる実装タスクリストを分解・生成するためのものである

重要な方針
* タスクリストは抽象度の高いレベルで作成する
* ソースコードや詳細な実装手順は含めない
* 各タスクは「何を作成・修正するか」を明確に示す
* 詳細な実装は次の「実装フェーズ（コード生成）」でSPECを参照して行う

出力先
* ベースプロジェクトの場合: `{project_root}/tasks/` ディレクトリ
* 拡張の場合: `{spec_directory}/tasks/` ディレクトリ
* 重要: `{project_root}` と `{spec_directory}` は、パラメータで指定した値に置き換える

---

## 1. 設計ドキュメントの分析

パラメータで指定されたプロジェクト情報に基づいて、以下の設計ドキュメントを読み込んで分析してください。

注意: `{project_root}` は、パラメータで指定されたパスに置き換えてください。全てのパスはそのプロジェクトルートを基準とした相対パスです。

### Agent Skillsルール（最優先で確認）

* @agent_skills/struts-to-jsf-migration/principles/ - マイグレーションルール、アーキテクチャ標準、マッピング規則、セキュリティ標準を確認
  * このフォルダ配下のすべてのMarkdownファイルを読み込み、マイグレーションルールを遵守すること
  * Code-to-Spec-to-Codeアプローチ、マッピング規則を確認
* @agent_skills/jakarta-ee-api-base/principles/ - Jakarta EE開発の原則
  * このフォルダ配下の原則ドキュメントを読み込み、共通ルールを遵守すること
  * 重要: タスク分解においても、ルールドキュメントに記載されたすべてのルール（テストカバレッジ基準、アーキテクチャパターン、コーディング規約など）を遵守すること
  * 注意: Agent Skills配下のルールは全プロジェクト共通。プロジェクト固有のルールがある場合は `{project_root}/principles/` も確認すること

### フレームワーク仕様（該当する場合）

* @agent_skills/struts-to-jsf-migration/frameworks/ - フレームワーク固有のSPECやサンプルコードを確認する
* @agent_skills/jakarta-ee-api-base/frameworks/ - フレームワーク固有のSPECやサンプルコードを確認する
  * 特定のフレームワーク（ライブラリ、ツール等）の使用方法、設計パターン、実装例を参照する
  * タスク分解時に、フレームワーク固有の実装要件を考慮する

### 必須ドキュメント（system/配下）

* architecture_design.md - 技術スタック、アーキテクチャパターン、ライブラリを確認
  * アーキテクチャパターン: サーバーサイドMVC（JSF）
  * データ管理方針を確認: エンティティ実装、JPA/EntityManager
  * セッション管理方針を確認: ViewScoped、Flash Scope等
  * データソース設定セクションでJNDI名を確認（persistence.xml設定に必要）
* functional_design.md - システム全体の機能設計概要と各画面へのリンクを確認

### オプションドキュメント（system/配下、存在する場合）

* requirements.md - システムの目的、機能要件
* data_model.md - エンティティとデータベーススキーマを確認
  * persistence.xml設定情報セクションでJNDI名を確認（persistence.xml設定に必要）
* behaviors.md - システム全体の振る舞い概要と各画面へのリンクを確認

### 画面単位ドキュメント（screen/配下）

* screen/SCREEN_XXX_*/ - 各画面単位のディレクトリ（例: SCREEN_001_PersonList, SCREEN_002_PersonInput）
  * screen_design.md - 画面レイアウト、入力項目、バリデーション
  * functional_design.md - Managed Bean、Service、データアクセス設計
  * behaviors.md - 画面の振る舞い仕様（受入基準、Given-When-Then）

注意: プロジェクトによって利用可能なドキュメントは異なります。利用可能なものに基づいてタスクを生成してください。

---

## 2. タスクファイルの分割構造

* 出力先の決定:
  * ベースプロジェクトの場合: `{project_root}/tasks/` ディレクトリ
  * 拡張の場合: `{spec_directory}/tasks/` ディレクトリ

重要: 
* `{project_root}` は、パラメータで指定されたパスに置き換えてください
* ベースプロジェクトのタスクは `{project_root}/tasks/` に配置
* 拡張のタスクは `{spec_directory}/tasks/` に配置

複数人が並行して作業できるように、以下のようにタスクファイルを分割して生成してください：

### 2.1 メインタスクリスト

`tasks/tasks.md` （指定された出力先に配置）
* プロジェクト全体の実行順序を示すメインタスクリスト
* 各タスクと担当者割り当ての概要
* 他のタスクファイルへのリンク集
* タスク間の依存関係を明示

### 2.2 セットアップタスク

`tasks/setup_tasks.md`
* プロジェクト初期化（全員が実行前に1回だけ）
* 開発環境セットアップ
* データベース初期化
* アプリケーションサーバー設定
* ログ設定
* 静的リソース配置（CSS、画像等）

### 2.3 共通機能タスク

`tasks/common_tasks.md`
* 複数画面で共有される共通コンポーネント
* エンティティ: architecture_design.mdとdata_model.mdから実装対象を判断
* Service: ビジネスロジック層（CDI + JPA）
* 共通DTO/モデル: 画面間のデータ受け渡し用
* 共通ユーティリティ: MessageUtil、ValidationUtil等
* 設定ファイル: persistence.xml、beans.xml、web.xml等

* 注意: 共通コンポーネントの具体的な内容は、architecture_design.md、functional_design.md、data_model.mdから判断してください。

* persistence.xmlのJNDI名設定
  * architecture_design.mdに記載されているJNDI名を使用すること
  * 移行元で実際に使用されているJNDI名を正確に設定すること
  * 決め打ちや推測でJNDI名を設定してはならない
  * 参照SPEC: architecture_design.mdの「データソース設定」セクションまたは「persistence.xml設定情報」セクション

### 2.4 画面別タスク

SPECから画面を抽出してタスクファイルを生成：

#### 画面の識別と抽出

1. 画面の識別
   * requirements.md、screen/ ディレクトリ、functional_design.mdから画面を抽出
   * 各画面の範囲と責務を分析
   * 画面間の依存関係を把握

2. タスクファイルの命名規則
   * 基本形式：`tasks/[SCREEN_ID]_[画面名].md`
   * 例：`SCREEN_001_PersonList.md`、`SCREEN_002_PersonInput.md`
   * screen/配下のディレクトリ名と対応させる
   * 注意: ファイル名はアンダースコア区切りを使用

3. 各画面タスクファイルの内容
   * 画面固有のManaged Bean（JSF Managed Bean）
   * 画面固有のService（該当する場合）
   * 画面固有のDTO/モデル（該当する場合）
   * Facelets XHTML（画面レイアウト）
   * 画面固有のテストケース（単体テスト、画面テスト）
   * 担当者: 1名（画面単位で独立して実装可能）

4. 画面分割の判断基準
   * 小規模プロジェクト（1-3画面）: 1つの`all_screens.md`にまとめても可
   * 中規模プロジェクト（4-10画面）: 画面単位でファイル分割
   * 大規模プロジェクト（10+画面）: 画面グループごとにディレクトリ分割も検討

### 2.5 結合テストタスク

`tasks/integration_tasks.md`
* 画面間結合テスト（画面遷移、データ受け渡し）
* E2Eテスト（Selenium/Arquillian等） - 主要な業務フローをテスト
* セッション管理テスト（ViewScoped、Flash Scope、Session Scope）
* バリデーションテスト
* 並行処理テスト（該当する場合）
* 最終検証

---

## 3. タスク分解ルール

### 3.1 並行実行の判断基準

* [P]マークを付与する条件（並行実行可能）:
  * 異なるファイルを編集するタスク
  * 異なるエンティティの実装
  * 異なる画面の実装
  * 独立したテストケース

* 順次実行（[P]なし）が必要な条件:
  * 同じファイルを編集するタスク
  * 依存関係があるタスク（Entity → Service → Managed Bean → XHTML の順序等）

### 3.2 タスクの粒度

重要: タスクは抽象度の高いレベルで定義し、ソースコードは含めない

各タスクは以下の粒度で分割：
* Entity/Model: 1エンティティクラスの作成/修正
* DTO/Model: 1 DTOまたはモデルクラスの作成/修正
* Service: 1 Serviceクラスの作成/修正（複雑な場合は複数タスクに分割）
* Managed Bean: 1 Managed Beanクラスの作成/修正
* XHTML: 1画面のXHTMLファイルの作成/修正
* Test: 1テストクラスの作成/修正

* タスクの記述レベル:
  * 「何を作成・修正するか」を明確に記述
  * 「どのような機能を実装するか」を簡潔に記述
  * ソースコードや詳細な実装手順は記述しない
  * 詳細な実装は次の「実装フェーズ（コード生成）」で行う

### 3.3 依存関係の順序付け

以下の順序でタスクを配置：

1. セットアップ (全ての前提)
   * 開発環境構築
   * プロジェクト初期化
   * データベース設定

2. 共通機能 (複数画面で共有)
   * 共通Entity/Model（architecture_design.mdから判断）
   * 共通Service（JPA、EntityManager）
   * 共通Utility/Helper
   * 設定ファイル（persistence.xml、beans.xml等）

3. 画面別実装（画面単位） (並行実行可能)
   * 一般的な実装順序: DTO/Model → Entity → Service → Managed Bean → XHTML
   * 各画面は独立して実装可能
   * 注意: 実装順序はプロジェクトのアーキテクチャに従う

4. 結合テスト (全画面実装後)
   * 画面間結合
   * E2Eテスト - 主要な業務フローベース
   * セッション管理テスト
   * バリデーションテスト

---

## 4. タスクファイルのフォーマット

各タスクファイルには以下の情報を含めてください：

### 4.1 ヘッダー情報

```markdown
# [タスクファイル名]

担当者: [想定人数と役割]
推奨スキル: [必要なスキルセット]
想定工数: [時間]
依存タスク: [前提となるタスクファイル]
```

### 4.2 タスクリスト

```markdown
* [ ] [P] タスク X.X.X: [タスク名]
  * 目的: [このタスクで実現する機能・目的]
  * 対象: [作成/修正するコンポーネント名やファイル名]
  * 参照SPEC: [参照するSPEC（Markdownリンク形式）] の「[セクション番号 セクション名]」
  * 注意事項: [考慮すべき点があれば記載]
```

* タスクID命名規則:
  * タスクIDはアンダースコア区切りを使用します（例: `T_SETUP_001`, `T_SCREEN001_003`）
  * ハイフンは使用しません（例: ~~`T-SETUP-001`~~, ~~`T-SCREEN001-003`~~）
  * 形式: `T_[カテゴリ]_[連番]` または `T_[SCREEN_ID]_[連番]`

* SPEC参照の記述規則:
  * 必須: Markdownリンク形式で記述し、クリックで直接SPECファイルに飛べるようにする
  * 必須: 相対パスを使用（タスクファイルからSPECファイルへの相対パス）
  * 必須: 具体的なセクション番号とセクション名を明記
  * 形式: `[ファイル名](相対パス) の「セクション番号 セクション名」`

* 記述例:

```markdown
* [ ] T_SCREEN001_003: PersonTableBean の作成
  * 目的: Person一覧画面のManaged Beanを実装する
  * 対象: PersonTableBean.java (JSF Managed Bean)
  * 参照SPEC: [functional_design.md](../specs/baseline/detailed_design/screen/SCREEN_001_PersonList/functional_design.md) の「2.1 PersonTableBean」
  * 注意事項: ViewScopedで実装し、画面遷移時にFlash Scopeでデータを受け渡すこと
```

* 複数SPEC参照の例:

```markdown
* [ ] T_SCREEN001_004: PersonTablePage.xhtml の作成
  * 目的: Person一覧画面のXHTMLを実装する
  * 対象: PersonTablePage.xhtml (Facelets XHTML)
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/detailed_design/screen/SCREEN_001_PersonList/screen_design.md) の「2. 画面レイアウト」
    * [functional_design.md](../specs/baseline/detailed_design/screen/SCREEN_001_PersonList/functional_design.md) の「3. 画面遷移」
  * 注意事項: h:dataTableを使用してPersonリストを表示すること
```

注意: ソースコードや詳細な実装手順は記述しない

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
| 2. SCREEN_001 | SCREEN_001_xxx.md | 担当者A | 可能 | [分析から算出] |
| 3. SCREEN_002 | SCREEN_002_yyy.md | 担当者B | 可能 | [分析から算出] |
| ... | ... | ... | ... | ... |
| N. 結合テスト | integration_tasks.md | 全員 | 一部可能 | [分析から算出] |

### 実行順序

1. タスク0: セットアップ（全員で実行）
2. タスク1: 共通機能（共通機能チームが実装）
3. タスク2～N-1: 画面別実装（各担当者が並行実行） ← ここが並行化のポイント
4. タスクN: 結合テスト（全員で実施）

### タスクファイル一覧

* [セットアップタスク](setup_tasks.md)
* [共通機能タスク](common_tasks.md)
* [SCREEN_001のタスク](SCREEN_001_xxx.md)
* [SCREEN_002のタスク](SCREEN_002_yyy.md)
* ...
* [結合テストタスク](integration_tasks.md)

## 依存関係図

[Mermaid形式で依存関係を図示]
\```

* 生成時の注意:
  * プロジェクト名はrequirements.mdから取得
  * 画面の数、命名、分割方法はSPECから判断
  * 想定工数は各タスクの複雑度分析から算出
  * ファイル名はすべてアンダースコア区切りを使用

---

## 6. 成果物チェックリスト

生成されるタスクファイルが満たすべき要件：
* ソースコードや詳細な実装手順を含まず、「何を作成・修正するか」を明確に記述
* [P]マークで並行実行可能なタスクを明示し、依存関係を明確に記述
* タスクIDはアンダースコア区切り（例: `T_SETUP_001`）で一意に付与
* JSFアーキテクチャに応じた要件:
  * Managed Bean（`@Named`, `@ViewScoped`等）の実装
  * Facelets XHTML（`<h:*>`タグ）の実装
  * CDI + JPA（`@Inject`, `@PersistenceContext`）の実装
  * セッション管理（ViewScoped、Flash Scope等）の考慮
  * Bean Validationの実装

---

## 7. 生成手順

1. ルールとSPEC分析: `@agent_skills/struts-to-jsf-migration/principles/` と `@agent_skills/jakarta-ee-api-base/principles/` 配下の共通ルールドキュメントと全SPECファイル（system/とscreen/）を読み込み、共通ルールと機能全体を把握
2. アーキテクチャ識別: architecture_design.mdからアーキテクチャパターン（サーバーサイドMVC: JSF）を識別
3. 画面抽出: 実装が必要な画面を抽出し、依存関係と共通コンポーネントを識別
4. タスク分割: 画面数に応じた適切なファイル分割方法を決定（小規模: 少数ファイル、中規模: 画面別、大規模: グループ別）
5. タスク構成: 各SPECからタスクを抽出し、セットアップ/共通/画面別/結合に分類・順序付け
6. 並行化判定: [P]マークを付与し、タスクファイルを指定された出力先に生成
7. メインリスト生成: `tasks/tasks.md` に全体概要と実行計画を生成

* 注意: `{project_root}` と出力先はパラメータで指定されます。ファイル名・タスクIDは全てアンダースコア区切りを使用。

---

## 8. 重要な注意事項

### 命名規則

* ファイル名・タスクIDは全てアンダースコア区切り（例: `setup_tasks.md`, `T_SETUP_001`）
* ハイフン（`-`）は使用しません

### SPEC参照の記述

全てのタスクの「参照SPEC」は以下の形式で記述してください：
* Markdownリンク形式でクリック可能にする（例: `[functional_design.md](相対パス)`）
* 具体的なセクション番号とセクション名を明記（例: `の「2.1 PersonTableBean」`）
* 複数SPEC参照の場合は箇条書きで列挙
* system/とscreen/配下の両方のドキュメントを適切に参照する

### タスク分解のルール

* ルール遵守: `@agent_skills/struts-to-jsf-migration/principles/` と `@agent_skills/jakarta-ee-api-base/principles/` 配下の原則ドキュメント（共通ルール、マイグレーションルール、品質基準、セキュリティ標準、組織標準）を必ず遵守。プロジェクト固有の原則がある場合は `{project_root}/principles/` も併せて遵守
* 抽象度の維持: タスクは「何を作るか」のみを記述。ソースコードや詳細な実装手順は記述しない
* アーキテクチャ適応: architecture_design.mdからアーキテクチャパターンを識別し、適切なタスクを分解・生成
  * サーバーサイドMVC（JSF）: Managed Bean、Facelets XHTML、CDI + JPA、セッション管理
* 既存コード考慮: 既存実装がある場合は、修正タスクと新規作成タスクを明確に区別

### JSF特有の注意点

* 画面（UI）が含まれるため、Managed Bean と Facelets XHTML のタスクを生成する
* 画面遷移は暗黙的ナビゲーション（戻り値が画面ID）または faces-config.xml で管理
* セッション管理（ViewScoped、Flash Scope、Session Scope）を考慮
* Bean Validation、JSFライフサイクル、Unified ELを活用

### プロジェクトルートの扱い

* `{project_root}` は、パラメータで明示的に指定されたパスに置き換えてください
* 相対パスでも絶対パスでも構いません
* 全てのファイル操作は、このプロジェクトルートを基準に行います

---

## 参考資料

* [マイグレーションルール](../principles/) - マッピング規則、マイグレーションルール
* [共通ルール](../../jakarta-ee-api-base/principles/) - 共通ルール
* [code_generation.md](code_generation.md) - 次のステップ（コード生成）
