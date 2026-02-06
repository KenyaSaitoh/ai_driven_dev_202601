# 詳細設計インストラクション（JSF画面単位）

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
target_type: "ここに対象画面のIDを入力（例: FUNC_001_PersonList）"
```

例:
```yaml
project_root: "projects/sdd-wf/person/jsf-person"
spec_directory: "projects/sdd-wf/person/jsf-person/specs/baseline"
target_type: "FUNC_001_PersonList"
```

---

## 概要

基本設計SPEC（basic_design/）から画面の詳細設計書を生成する

重要な方針:
- 簡潔性の原則（最重要）: 詳細設計書は、基本設計とコードの「橋渡し」となる設計判断のみを簡潔に記載する。後から人が修正する可能性を考慮し、必要最小限の情報のみを記載する。コード例は記載しない（次のステップでコード生成するため）
- 既存ファイルの扱い: 既存の detailed_design.md や behaviors.md が存在する場合は、それらを削除せずに読み込んで、差分のみを反映する
- AIが仕様を理解し、人と対話しながら妥当性・充足性を確認する
- 不明点は必ずユーザーに質問する（推測や仮定で設計を進めることは厳禁）

詳細設計で記載すべき情報（最小限、箇条書きでOK）:
- クラス名と責務（1行）
- 主要メソッドのシグネチャ（引数、戻り値、例外）のみ
- 設計判断を示すアノテーション（@ViewScoped, @Named等）
- JPQLクエリ（WHERE句、JOIN等の設計判断）
- 依存関係（@Inject対象）
- 画面遷移ルール（outcome）

詳細設計で記載すべきでない情報:
- メソッドの実装詳細、処理ステップ、ループ、条件分岐
- すべてのフィールド定義、getter/setter
- バリデーションの詳細
- 基本設計SPECの内容の繰り返し
- コード例（コード生成フェーズで生成するため）

生成するファイル:
- {spec_directory}/detailed_design/detailed_design/FUNC_XXX/detailed_design.md - 実装クラス設計
- {spec_directory}/detailed_design/detailed_design/FUNC_XXX/behaviors.md - 単体テスト用（Gherkin 記法）

behaviors.mdの種別:
- basic_design/behaviors.md: 結合・E2Eテスト用（画面間遷移、実際のDB・画面レンダリング）
- detailed_design/detailed_design/FUNC_XXX/behaviors.md: 単体テスト用（1メソッド単位、依存関係はモック）

---

## 1. SPECの読み込みと理解

以下のドキュメントを読み込んで分析する（{project_root}, {spec_directory}, {target_type} はパラメータで指定された値に置き換える）

### 1.1 Agent Skillsルール（最優先）
- @agent_skills/struts-to-jsf-migration/principles/ 配下の原則ドキュメントを読み込み、共通ルールを遵守する
- @agent_skills/jakarta-ee-api-base/frameworks/ 配下のフレームワーク固有のSPECも確認する

### 1.2 システムレベルの仕様
- {spec_directory}/basic_design/architecture_design.md - 技術スタック、パッケージ構造、セッション管理方針を確認
- {spec_directory}/basic_design/functional_design.md - システム全体の機能設計、画面遷移図を確認
- {spec_directory}/basic_design/data_model.md - テーブル定義（ERD）を確認

### 1.3 対象画面の仕様
- {spec_directory}/basic_design/screen_design.md - 対象画面（{target_type}）の画面設計を確認
  - レイアウト、入力項目、ボタン、バリデーションルール、初期表示
- {spec_directory}/basic_design/behaviors.md - 対象画面の振る舞い（該当する場合）を確認

---

## 2. 対話による確認と理解の提示

### 2.1 理解内容の提示
基本設計SPECを読み込んだ後、理解した内容をユーザーに説明する:
- 対象画面ID: {target_type}
- 画面の責務と主要機能
- 実装対象のコンポーネント（Managed Bean、Service、Entity等）
- 画面遷移

### 2.2 不明点の質問
SPECに明記されていない以下の点はユーザーに質問する:
- 複数の実装方法が考えられる場合
- SPECに明示されていないビジネスルール
- エッジケースの扱い
- 設定値・環境依存の情報
- セキュリティ実装（最優先で確認）
- パフォーマンス実装（必ず確認）
- データ整合性・トランザクション実装（必ず確認）

質問不要なケース:
- SPECに明確に記載されている内容
- 技術的な標準・常識
- Agent Skillsルールで明示されている内容
- フレームワークのベストプラクティス

---

## 3. 詳細設計書の生成

### 3.1 生成するファイル

#### 3.1.1 detailed_design.md（実装クラス設計）
- 生成先: {spec_directory}/detailed_design/detailed_design/{target_type}/detailed_design.md
- テンプレート: @agent_skills/struts-to-jsf-migration/templates/detailed_design/detailed_design.md
- 既存ファイルがある場合は、差分のみを反映する

記載内容（箇条書きでOK、コード例なし）:
- Managed Bean（クラス名、責務、スコープ、主要メソッドのシグネチャ）
- Service（クラス名、責務、主要メソッドのシグネチャ、JPQLクエリ）
- Entity（クラス名、テーブルマッピング、リレーション）
- DTO（クラス名、フィールド、バリデーション）
- 画面遷移ルール（outcome）
- 依存関係（@Inject対象）

#### 3.1.2 behaviors.md（単体テスト用の振る舞い）
- 生成先: {spec_directory}/detailed_design/detailed_design/{target_type}/behaviors.md
- テンプレート: @agent_skills/struts-to-jsf-migration/templates/detailed_design/behaviors.md
- 既存ファイルがある場合は、差分のみを反映する

記載内容（Gherkin 記法）:
- メソッドレベルの単体テストシナリオ
- 依存関係はモックを使用
- 1メソッド＝1テストケースの粒度
- 境界値テスト、異常系テスト

---

## 4. 完了報告

詳細設計書の生成が完了したら、以下を報告する:
- 生成されたファイルのパス
- 対象画面の実装クラス構成（箇条書き）
- 次のステップ（コード生成）の案内

---

## 参考資料

- [basic_design.md](basic_design.md) - 基本設計（前工程）
- [code_generation.md](code_generation.md) - コード生成（次工程）
- [Jakarta EE 10仕様](https://jakarta.ee/specifications/)
- [JSF仕様](https://jakarta.ee/specifications/faces/4.0/)
- [JPA仕様](https://jakarta.ee/specifications/persistence/3.1/)
