# 詳細設計インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
target_domain: "対象ドメイン名"
```

例:
```yaml
project_root: "projects/sdd-wf/bookstore/back-office-api"
spec_directory: "projects/sdd-wf/bookstore/back-office-api/specs/baseline"
target_domain: "common"
```

---

## 概要

基本設計SPEC（basic_design/{target_domain}/）から詳細設計書（detailed_design/{target_domain}/）を生成する

重要な方針:
- 簡潔性の原則（最重要）: 詳細設計書は、基本設計とコードの「橋渡し」となる設計判断のみを簡潔に記載する。後から人が修正する可能性を考慮し、必要最小限の情報のみを記載する。コード例は記載しない（次のステップでコード生成するため）
- 既存ファイルの扱い: 既存の detailed_design.md や behaviors.md が存在する場合は、それらを削除せずに読み込んで、差分のみを反映する
- commonは最優先で詳細設計を作成する（他のドメインはcommonに依存）
- AIが仕様を理解し、人と対話しながら妥当性・充足性を確認する
- 不明点は必ずユーザーに質問する（推測や仮定で設計を進めることは厳禁）

詳細設計で記載すべき情報（最小限、箇条書きでOK）:
- クラス名と責務（1行）
- 主要メソッドのシグネチャ（引数、戻り値、例外）のみ
- 設計判断を示すアノテーション（@Transactional, @Path等）
- JPQLクエリ（WHERE句、JOIN等の設計判断）
- 依存関係（@Inject対象）

詳細設計で記載すべきでない情報:
- メソッドの実装詳細、処理ステップ、ループ、条件分岐
- すべてのフィールド定義、getter/setter
- バリデーションの詳細
- 基本設計SPECの内容の繰り返し
- コード例（コード生成フェーズで生成するため）

behaviors.mdの種別（すべてGherkin記法で記述）:
- requirements/behaviors.md: E2Eテスト用（システム全体、API層含む、Gherkin記法）
- basic_design/{target_domain}/behaviors.md: 結合テスト用（ドメイン内の連携シナリオ、Gherkin記法）
- detailed_design/{target_domain}/behaviors.md: 単体テスト用（1メソッド単位、ブラックボックステスト、Gherkin記法、本フェーズで新規作成）

---

## 1. SPECの読み込みと理解

以下のドキュメントを読み込んで分析する（{project_root}, {spec_directory}, {target_domain} はパラメータで指定された値に置き換える）

### 1.1 Agent Skillsルール（最優先）
- @agent_skills/jakarta-ee-api-base/principles/ 配下の原則ドキュメントを読み込み、共通ルールを遵守する

### 1.2 フレームワーク仕様（該当する場合）
- @agent_skills/jakarta-ee-api-base/frameworks/ 配下のフレームワーク固有のSPECやサンプルを確認する

### 1.3 基本設計の仕様（共通）
- {spec_directory}/basic_design/common/architecture_design.md - 技術スタック、パッケージ構造を確認
- {spec_directory}/basic_design/common/data_model.md - テーブル定義とERDを確認（該当する場合）
- {spec_directory}/basic_design/common/external_interface.md - 外部API仕様を確認（該当する場合）
- {spec_directory}/basic_design/common/functional_design.md - 共通機能設計を確認

### 1.4 基本設計の仕様（対象ドメイン）
- {spec_directory}/basic_design/{target_domain}/functional_design.md - ドメインの機能設計を確認
- {spec_directory}/basic_design/{target_domain}/behaviors.md - ドメインの振る舞いを確認（該当する場合）

---

## 2. 対話による確認と理解の提示

### 2.1 理解内容の提示
基本設計SPECを読み込んだ後、理解した内容をユーザーに説明する:
- ドメイン名と責務
- 実装対象のコンポーネント種別（Resource/Service/Dao/Entity/DTO等）
- 依存関係（commonに依存、または他のドメインに依存）

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
- 生成先: {spec_directory}/detailed_design/{target_domain}/detailed_design.md
- テンプレート: @agent_skills/jakarta-ee-api-base/templates/detailed_design/detailed_design.md
- 既存ファイルがある場合は、差分のみを反映する

記載内容（箇条書きでOK、コード例なし）:
- クラス名と責務（1行）
- 主要メソッドのシグネチャ（引数、戻り値、例外）
- 設計判断を示すアノテーション
- JPQLクエリ（WHERE句、JOIN等）
- 依存関係（@Inject対象）

#### 3.1.2 behaviors.md（単体テスト用の振る舞い仕様書）
- 生成先: {spec_directory}/detailed_design/{target_domain}/behaviors.md
- テンプレート: @agent_skills/jakarta-ee-api-base/templates/detailed_design/behaviors.md
- 既存ファイルがある場合は、差分のみを反映する
- 記法: Gherkin 記法で記述する（Feature, Scenario, Given, When, Then, And, But）
- Gherkin記法の詳細: @agent_skills/jakarta-ee-api-base/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照

記載内容（Gherkin 記法で記述）:
- メソッドレベルの単体テストシナリオ（ブラックボックステスト）
- 各シナリオは Feature, Scenario, Given, When, Then で構成
- 依存関係はモックを使用
- 1メソッド＝1 Scenario の粒度
- 境界値テスト、異常系テストもGherkin記法で記述

---

## 4. 完了報告

詳細設計書の生成が完了したら、以下を報告する:
- 生成されたファイルのパス
- 対象ドメインの実装クラス構成（箇条書き）
- 次のステップ（コード生成）の案内

---

## 参考資料

- [basic_design.md](basic_design.md) - 基本設計（前工程）
- [code_generation.md](code_generation.md) - コード生成（次工程）
- [Jakarta EE 10仕様](https://jakarta.ee/specifications/)
- [JPA仕様](https://jakarta.ee/specifications/persistence/3.1/)
- [JAX-RS仕様](https://jakarta.ee/specifications/restful-ws/3.1/)
