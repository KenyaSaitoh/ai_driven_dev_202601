# コード生成インストラクション（アジャイル）

## パラメータ設定

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "SPECディレクトリのパス（オプション、デフォルト: {project_root}/specs/baseline）"
target: "common"  # または "usecases/order-creation" のように usecases/{フォルダ名}
skip_infrastructure: false  # setup 時のみ: true の場合、インフラセットアップをスキップ
```

* 例: 業務共通（common）の実装
```yaml
project_root: "projects/sdd-agile/bookstore/berry-books-api"
target: "common"
```

* 例: ユースケースの実装
```yaml
project_root: "projects/sdd-agile/bookstore/berry-books-api"
target: "usecases/order-creation"
```

注意: パス区切りはOS環境に応じて調整する。以降、`{project_root}`, `{spec_directory}`, `{target}` はパラメータで設定した値に置き換える。`spec_directory` 未指定時は `{project_root}/specs/baseline` とする。

---

## 概要

このインストラクションは、業務共通SPEC（common/）と各ユースケースSPEC（usecases/{名}/）を駆動元に、指定された対象（common または usecases/{名}）の実装コードと単体テストを生成・更新するためのものである。タスクファイル（tasks/）は不要である。

アジャイルにおける位置づけ
* 本インストラクションは、何度でも繰り返し実行することを前提とする。既に出来上がっているコードに対して、SPEC に基づく差分を反映させ、漸進的にコードを更新していく
* 基本設計SPECの「変更管理」は行わない。SPEC を編集したうえで、本インストラクションで target を指定して再実行すればよい
* 既存のコード・テストがある場合は、SPEC との差分を反映する形で更新する。ゼロから全体を再生成するのではなく、変更が必要な箇所を漸進的に更新する

重要: 指定された target のみを実行し、完了したら停止する。次の対象に自動的に進んではいけない。詳細設計書（detailed_design）は必須としない。SPEC から仕様を読み込んだあと、曖昧な点や重要な設計判断がある場合はいきなりコード生成を始めず、必ず人に確認してから実装に進む（本インストラクション「5. 仕様の確認と対話」を参照）。

---

## 1. 対象の判別

* `target` パラメータから、common 用かユースケース用かを判別する
* `target` が `common` → 業務共通の実装
* `target` が `usecases/{フォルダ名}` → そのユースケースの実装（usecase_folder はフォルダ名とする）

---

## 2. 読み込むドキュメント（共通）

1. Agent Skillsルール: @agent_skills/jakarta-ee-api-agile/principles/ を最優先で確認する
2. 共通SPEC（常に参照）:
   * `{spec_directory}/common/architecture_design.md` - 技術スタック、レイヤー、パッケージ、セキュリティ、テスト戦略
   * `{spec_directory}/common/data_model.md` - テーブル定義、ERD
   * `{spec_directory}/common/external_interface.md` - 外部連携（該当時）

---

## 3. 読み込むドキュメント（target=common の場合）

* 上記「共通」のみ。common の3SPECを駆動元に実装する。
* 機能要件の「唯一の真実」は common の3SPEC。

---

## 4. 読み込むドキュメント（target=usecases/{名} の場合）

* 上記「共通」に加え:
* `{spec_directory}/usecases/{usecase_folder}/userstory.md` - ユーザーストーリー、受入基準、API仕様、ビジネスルール
* `{spec_directory}/usecases/{usecase_folder}/behaviors.md` - 振る舞い・テストシナリオ
* common の3SPECで既に定義された Entity/Dao 等を参照する

---

## 5. 仕様の確認と対話（コード生成前・必須）

SPEC を読み込んだあと、いきなりコード生成を始めず、以下を実施する。

### 5.1 方針

* AIが仕様を理解し、人と対話しながら曖昧点・設計判断を確認する
* 不明点の確認（最重要）
  * 判断に迷う点、複数の解釈が可能な点、SPEC に明示されていない実装判断は必ずユーザーに質問する
  * 推測や仮定でコード生成を進めることは厳禁
  * 必要に応じて何度でも確認する。SPEC から明らかなこと、技術的常識は質問不要
  * コード生成の品質はここでの確認に依存するため、最も重要なステップとする
* 曖昧点や確認事項が1つでもある場合は、ユーザーの返答を得るまでコード生成を開始しない

### 5.2 理解した内容の説明と確認事項の提示

以下の形式で、AI が理解した内容をユーザーに説明し、不明点・確認事項をまとめて提示する。

```markdown
## 理解した内容

### 対象
* target: <common または usecases/{名}>
* 実装範囲: <Entity/Dao/Service/Resource 等の箇条書き>

### 主要な仕様（要約）
* <architecture_design / data_model / userstory 等から抽出した要点>

### 不明点・確認事項（コード生成前に判断が必要な点）
1. <確認事項1>
2. <確認事項2>
```

確認事項が0件の場合のみ「確認事項はありません。コード生成に進みます。」と明示したうえで次節へ進む。1件以上ある場合はユーザーの返答を待つ。

### 5.3 確認すべき観点（該当する場合に質問）

以下の観点で、SPEC に明記されていない場合はユーザーに質問する。

1. パッケージ構造・実装するコンポーネントのリスト（common と userstory で齟齬がある場合など）
2. DTO 設計（リクエスト/レスポンス形式の曖昧な部分）
3. ビジネスロジック（バリデーション、計算ロジック、状態遷移で SPEC に書かれていない部分）
4. エラーハンドリング（想定されるエラーシナリオ・HTTP ステータス・メッセージ）
5. 外部 API 連携（external_interface にないエンドポイントや形式）
6. トランザクション管理（境界の配置、伝播）
7. セキュリティ実装（認証・認可、JWT、入力検証、ログマスキング等）— 実装方式は必ず人間が判断。AI は推測で決定しない
8. パフォーマンス実装（最大件数、ページネーション、タイムアウト、リトライ、キャッシュ、N+1 回避等）— 業務要件に依存するため推測不可
9. データ整合性・トランザクション（境界、同時更新制御、論理/物理削除等）— データ重要度により戦略が異なる

### 5.4 質問の原則

質問すべきケース（判断に迷う点）

* 複数の実装方法が考えられる場合（例: キャッシュ方式、認証方式、トランザクション境界の配置）
* SPEC に明示されていないビジネスルール（例: リトライ回数、論理削除 vs 物理削除、バリデーション文言）
* エッジケースの扱い（例: null/空リストの扱い、楽観ロック vs 悲観ロック）
* 設定値・環境依存の情報（例: JNDI 名、タイムアウト値で SPEC に記載がない場合）
* セキュリティ・パフォーマンス・データ整合性の実装方針（SPEC に明記されていない場合）

質問不要なケース（自己判断可能）

* SPEC に明確に記載されている内容（テーブル定義、API 仕様等）
* 技術的な標準・常識（REST のステータスコード、JPA の基本用法、命名規則等）
* Agent Skills ルール（principles/）で明示されている内容
* フレームワークのベストプラクティス

バランス

* 「〜という理解で進めます。以下の点のみ確認させてください」と、明確な点と不明確な点を分けて提示する
* 本質的な判断ポイントに絞り、一度に多数の質問にしない
* SPEC から自動的に導出できる技術詳細は質問しない

### 5.5 不足情報の補完

ユーザーからのフィードバックを受けて、以下を補完したうえでコード生成に進む。

* SPEC に記載されていない実装詳細
* エラーメッセージの文言、設定値など

---

## 6. 実装の実行（本番コード生成のみ）

* 前提: 上記「5. 仕様の確認と対話」で不明点・確認事項が 0 件であるか、ユーザーから返答を得て補完済みであること。未確認のままコード生成を開始しない
* 指定された target（common または usecases/{名}）のみを実行する
* 既存コードがある場合: 現在の SPEC と既存コードの差分を検出し、必要な追加・修正・削除のみを反映する（漸進的更新）。既存の手書きコードや他 target で生成したコードは破壊しない
* 既存コードがない場合: 本番コード生成を実行する
* target=common で setup に相当する場合: skip_infrastructure に応じてインフラをスキップするかどうか判断する。アプリケーション固有のセットアップ（スキーマ、初期データ、静的リソース）は常に実行する
* 技術スタック・パッケージ・命名規則は common/architecture_design.md に厳密に従う
* 上位SPEC（common の3ファイル、usecases/{名}/userstory.md, behaviors.md）は修正しない

重要: このタスクは本番コード生成のみを行う。単体テスト生成は別タスク（@agent_skills/jakarta-ee-api-agile/instructions/unit_test_generation.md）で実施する

---

## 7. コンポーネント別参照

| コンポーネント | common タスク時の参照 | ユースケースタスク時の参照 |
|---------------|----------------------|---------------------------|
| Entity | data_model.md, architecture_design.md | data_model.md, userstory.md |
| Dao | data_model.md, architecture_design.md | userstory.md, common/（data_model 等） |
| Service | architecture_design.md, data_model.md | userstory.md, behaviors.md, common/ |
| Resource | architecture_design.md | userstory.md（API仕様）, behaviors.md |
| DTO | data_model.md | userstory.md, data_model.md |
| Filter/外部連携 | architecture_design.md, external_interface.md | architecture_design.md, external_interface.md, userstory.md |

---

## 8. 次のステップ

本番コード生成完了後は、以下を実施する：

1. 単体テストコード生成: @agent_skills/jakarta-ee-api-agile/instructions/unit_test_generation.md を使用して単体テストを生成する
2. 単体テスト実行: @agent_skills/jakarta-ee-api-base/instructions/unit_test_execution.md に従い単体テストを実行し、動作・カバレッジ・不足ケースを確認する
3. 必要に応じてSPEC→コード生成→テスト生成→テスト実行のループを行う

---

## 9. 参考

* ウォーターフォール版: @agent_skills/jakarta-ee-api-base/instructions/code_generation.md（パス・参照元の違いを除き実行方針は同一）
* 仕様確認・対話の考え方: @agent_skills/jakarta-ee-api-base/instructions/detailed_design.md（「2. 理解内容の確認と対話」「2.3 質問の原則」をコード生成前の確認に流用）
