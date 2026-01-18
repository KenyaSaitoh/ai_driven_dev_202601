---
name: jakarta-ee-api-base
description: Jakarta EE 10とJAX-RS 3.1を使ったREST APIサービス開発を支援。エンティティ実装、外部API連携など多様な実装要件に対応。SPECからタスク分解、詳細設計、コード生成、単体テスト実行評価、結合テスト、E2Eテストまで7段階で一貫サポート。基本設計変更対応も含む。
---

# Jakarta EE API サービス開発 Agent Skill

## 使い方（7段階プロセス）

### ステップ1: 基本設計（SPEC作成）

```
@agent_skills/jakarta-ee-api-base/instructions/basic_design.md

SPECを作成してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
```

AIと対話しながら以下を実施（対話的プロセス）
1. テンプレートを basic_design/ フォルダに展開
2. requirements.mdを読み込み、理解内容を説明
3. ユーザーと対話しながら各SPECの中身を埋める
4. システム全体のSPEC（architecture_design.md、functional_design.md等）を basic_design/ に作成

注意: 
* requirements.md（要件定義書）は所与とする（既に存在している前提）
* 基本設計フェーズでは、システム全体を一枚岩として設計する
* 機能単位への分解は、次のタスク分解フェーズで実施する

### ステップ2: タスク分解

```
@agent_skills/jakarta-ee-api-base/instructions/task_breakdown.md

タスクを分解してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
* output_directory: <タスク出力先パス>
```

AIが自動で以下を実行
1. basic_design/ を分析
2. 機能を依存関係に基づいて識別し、実装順序を決定
3. タスクファイルを分解・生成して `tasks/`フォルダに保存

重要: このタスク分解の結果が、次の詳細設計フェーズで detailed_design/ フォルダ構造を決定する

### ステップ3: 詳細設計

```
@agent_skills/jakarta-ee-api-base/instructions/detailed_design.md

詳細設計書を作成してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
* target_type: FUNC_XXX_xxx
```

AIと対話しながら以下を実施（対話的プロセス）
1. タスク分解の結果に基づいて detailed_design/{target_type}/ フォルダを作成
2. basic_design/functional_design.md を参照して実装レベルの detailed_design.md を生成（クラス設計、メソッドシグネチャ）
3. 単体テスト用の behaviors.md を新規作成（メソッドレベルのテストシナリオ）

重要: 
* functional_design.md は basic_design/ にのみ存在（唯一の真実の情報源）
* behaviors.md はE2Eテスト用（basic_design/）と単体テスト用（detailed_design/）で別物

### ステップ4: コード生成（詳細設計→実装→単体テスト）

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

セットアップタスクを実行してください

パラメータ
* project_root: <プロジェクトルートパス>
* task_file: <タスクファイルパス>
```

AIが自動で以下を実行
1. タスクと詳細設計を読み込み
2. 実装コードを生成（Resource、Service、Dao、Entity、DTO等）
3. タスク粒度内の単体テストを作成
   * タスク内のコンポーネント間は実際の連携をテスト
   * タスク外の依存関係のみモック化
4. タスクを完了としてマーク

### ステップ5: 単体テスト実行評価

```
@agent_skills/jakarta-ee-api-base/instructions/unit_test_execution.md

単体テストを実行してください

パラメータ
* project_root: <プロジェクトルートパス>
* target_type: FUNC_XXX_xxx
```

AIが自動で以下を実行
1. テスト実行（gradle test jacocoTestReport）
2. テスト結果とカバレッジ分析
3. 問題の分類（テスト失敗、必要な振る舞い、デッドコード、設計の誤り）
4. フィードバックレポート生成
5. ユーザーに推奨アクションを提示

重要: 
* 問題を発見してもユーザー確認なしに修正しない
* カバレッジ不足やデッドコードを具体的に提案
* 必要に応じてステップ3（詳細設計）に戻ってループ

フィードバックループ:
```
詳細設計 → コード生成 → テスト実行評価
    ↑                         ↓
    └──── フィードバック ←────┘
```

### ステップ6: 結合テスト生成

```
@agent_skills/jakarta-ee-api-base/instructions/it_generation.md

結合テストを生成してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
```

AIが自動で以下を実行
1. basic_design/behaviors.md（結合テストシナリオ）を読み込み
2. JUnit 5 + Weld SE を使用した結合テストを生成
   * Service層以下（Service + DAO + Entity + DB）の連携テスト
   * 実際のDBアクセス（メモリDB）
   * 外部APIはWireMockでスタブ化

### ステップ7: E2Eテスト生成

```
@agent_skills/jakarta-ee-api-base/instructions/e2e_test_generation.md

E2Eテストを生成してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
```

AIが自動で以下を実行
1. requirements/behaviors.md（E2Eテストシナリオ）を読み込み
2. REST Assured を使用したE2Eテストを生成
   * API層を含む全体フロー
   * 実際のHTTPリクエスト/レスポンス
3. テストデータのセットアップ/クリーンアップコードを生成

---

## 🔄 基本設計変更対応（手戻り・拡張案件）

```
@agent_skills/jakarta-ee-api-base/instructions/basic_design_change.md

基本設計の変更を適用してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
* change_spec: <変更差分ファイルパス>（省略可、デフォルト: {spec_directory}/basic_design/CHANGES.md）
```

AIが自動で以下を実行
1. CHANGES.md（変更差分ファイル）を読み込み
2. 変更の影響を受けるファイルを特定
3. 変更タスクファイル（`tasks/change_tasks.md`）を生成
4. 既存の指示書を呼び出して更新
5. CHANGES.mdをアーカイブ

使用方法:
1. 基本設計SPECのマスターファイル（functional_design.md等）を自由に編集
2. CHANGES.mdを作成して変更内容を明示的に記載
3. 上記コマンドを実行
4. 適用後、CHANGES.mdは自動的にchanges_archive/に移動

重要:
* マスターファイルはMarkdown、EXCEL、PDF、Word等、任意の形式で管理可能
* 変更内容はCHANGES.mdに明示的に記載（形式非依存）

---

## 実践例

```
@agent_skills/jakarta-ee-api-base/instructions/task_breakdown.md

全タスクを分解してください

パラメータ
* project_root: projects/sdd/bookstore/back-office-api-sdd
* spec_directory: projects/sdd/bookstore/back-office-api-sdd/specs/baseline
```

その後、詳細設計とコード生成を実施する

---

## 対応する主要機能

* JAX-RS 3.1によるREST API実装
* JPA 3.1によるデータ永続化（JPQL、Criteria API）
* CDI 4.0による依存性注入
* トランザクション管理（`@Transactional`）
* 楽観的ロック（`@Version`）
* JWT認証・認可
* CORS対応
* 外部API統合（RestClient）
* 例外ハンドリング（ExceptionMapper）

---

## ディレクトリ構造

```
agent_skills/jakarta-ee-api-base/
├── SKILL.md                          # このファイル
├── README.md                         # クイックスタートガイド
├── principles/                       # 原則（全プロジェクト共通）
│   ├── architecture.md              # Jakarta EE APIアーキテクチャ標準
│   ├── security.md                  # セキュリティ標準
│   └── common_rules.md              # 共通ルール
├── templates/                        # SPECテンプレート
│   ├── basic_design/                # 基本設計用テンプレート
│   │   ├── architecture_design.md
│   │   ├── functional_design.md
│   │   ├── data_model.md
│   │   ├── behaviors.md            # E2Eテスト用
│   │   └── external_interface.md
│   └── detailed_design/             # 詳細設計用テンプレート
│       ├── detailed_design.md
│       └── behaviors.md            # 単体テスト用
└── instructions/
    ├── basic_design.md               # ステップ1: 基本設計（SPEC作成）
    ├── task_breakdown.md             # ステップ2: タスク分解
    ├── detailed_design.md            # ステップ3: 詳細設計
    ├── code_generation.md            # ステップ4: コード生成（実装+単体テスト）
    ├── unit_test_execution.md        # ステップ5: 単体テスト実行評価
    ├── it_generation.md              # ステップ6: 結合テスト生成（JUnit + Weld SE）
    ├── e2e_test_generation.md        # ステップ7: E2Eテスト生成（REST Assured）
    └── basic_design_change.md        # 基本設計変更対応（手戻り・拡張案件）
```

---

## 参考資料

* [開発原則](principles/) - アーキテクチャ標準、セキュリティ標準、共通ルール
  * [architecture.md](principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](principles/security.md) - セキュリティ標準
  * [common_rules.md](principles/common_rules.md) - 共通ルール
* [Jakarta EE 10仕様](https://jakarta.ee/specifications/platform/10/)
* [JAX-RS 3.1仕様](https://jakarta.ee/specifications/restful-ws/3.1/)
* [Jakarta Persistence 3.1仕様](https://jakarta.ee/specifications/persistence/3.1/)
* [Jakarta CDI 4.0仕様](https://jakarta.ee/specifications/cdi/4.0/)
