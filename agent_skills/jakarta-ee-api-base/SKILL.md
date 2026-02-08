---
name: jakarta-ee-api-base
description: Jakarta EE 10とJAX-RS 3.1を使ったREST APIサービス開発を支援。エンティティ実装、外部API連携など多様な実装要件に対応。SPECから詳細設計、コード生成、単体テスト実行評価、結合テスト、E2Eテストまで6段階で一貫サポート。基本設計変更対応も含む。ドメインベースのフォルダ構成で並行開発を実現。
---

# Jakarta EE API サービス開発 Agent Skill

## 使い方（6段階プロセス）

### ステップ1: 基本設計（SPEC作成）

```
@agent_skills/jakarta-ee-api-base/instructions/basic_design.md

SPECを作成してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
```

AIと対話しながら以下を実施（対話的プロセス）
1. テンプレートを basic_design/common/ フォルダに展開
   * @agent_skills/jakarta-ee-api-base/templates/basic_design/ から5ファイルをコピー
2. requirements.mdを読み込み、理解内容を説明
3. ユーザーと対話しながら各SPECの中身を埋める
4. ドメインフォルダを作成（common/ + 各ドメイン）
5. 各ドメインのSPEC（functional_design.md、behaviors.md等）を作成

テンプレート:
* architecture_design.md - アーキテクチャ設計書
* data_model.md - データモデル仕様書
* external_interface.md - 外部インターフェース仕様書
* functional_design.md - 機能設計書
* behaviors.md - 振る舞い仕様書（結合テスト用）

注意: 
* requirements.md（要件定義書）は所与とする（既に存在している前提）
* 基本設計はドメイン単位で分割する（common/ + 各ドメイン）
* フォルダ構成＝実装順序（common/ → 各ドメイン）
* 既存SPECファイルがある場合は、削除せずに差分のみを反映する

### ステップ2: 詳細設計

```
@agent_skills/jakarta-ee-api-base/instructions/detailed_design.md

詳細設計書を作成してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
* target_domain: <ドメイン名>
```

AIと対話しながら以下を実施（対話的プロセス）
1. basic_design/{target_domain}/ の内容に基づいて detailed_design/{target_domain}/ フォルダを作成
2. テンプレートを参照して詳細設計を生成
   * @agent_skills/jakarta-ee-api-base/templates/detailed_design/ から2ファイルをコピー
3. basic_design/{target_domain}/functional_design.md を参照して実装レベルの detailed_design.md を生成
   * 基本設計とコードの「橋渡し」となる設計判断のみを簡潔に記載
   * クラス名と責務、主要メソッドのシグネチャ、設計判断を示すアノテーション等
   * 実装詳細（処理ステップ等）は記載しない（後から人が修正する可能性を考慮）
4. 単体テスト用の behaviors.md を新規作成（メソッドレベルのテストシナリオ）

テンプレート:
* detailed_design.md - 詳細設計書
* behaviors.md - 振る舞い仕様書（単体テスト用）

重要: 
* commonを最優先で詳細設計する（他のドメインはcommonに依存）
* 既存ファイルがある場合は、削除せずに差分のみを反映する
* functional_design.md は basic_design/{target_domain}/ に存在（ドメインごとの真実の情報源）
* behaviors.md はE2Eテスト用（requirements/）、結合テスト用（basic_design/{domain}/）、単体テスト用（detailed_design/{domain}/）の3種類

### ステップ3: コード生成（詳細設計→実装→単体テスト）

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

ドメインを実装してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
* target_domain: <ドメイン名>
* skip_infrastructure: false  # commonドメイン初回setup時のみ: true の場合、インフラセットアップをスキップ
```

AIが自動で以下を実行
1. 詳細設計（detailed_design/{target_domain}/）を読み込み
2. 実装コードを生成（Resource、Service、Dao、Entity、DTO等）
   * 既存コードがある場合は、削除せずに差分のみを反映する
   * commonドメイン初回時: skip_infrastructure=true の場合、DB/APサーバーのインストールをスキップ（スキーマ作成・初期データは実行）
3. ドメイン粒度内の単体テストを作成
   * ドメイン内のコンポーネント間は実際の連携をテスト
   * ドメイン外の依存関係のみモック化
   * 既存テストがある場合は、削除せずに差分のみを反映する
4. commonを最優先で実装（他のドメインはcommonに依存）

### ステップ4: 単体テスト実行評価

```
@agent_skills/jakarta-ee-api-base/instructions/unit_test_execution.md

単体テストを実行してください

パラメータ
* project_root: <プロジェクトルートパス>
* target_domain: <ドメイン名>
* build_script_path: null  # オプション（通常は不要）。マルチプロジェクト構成の場合のみ指定（例: "build.gradle"）
```

AIが自動で以下を実行
1. テスト実行（gradle test jacocoTestReport）
   * 通常は project_root で実行
   * マルチプロジェクト構成の場合のみ、build_script_path で指定した build.gradle のディレクトリでGradleタスクを実行
2. テスト結果とカバレッジ分析
3. 問題の分類（テスト失敗、必要な振る舞い、デッドコード、設計の誤り）
4. フィードバックレポート生成
5. ユーザーに推奨アクションを提示

重要: 
* 問題を発見してもユーザー確認なしに修正しない
* カバレッジ不足やデッドコードを具体的に提案
* 必要に応じてステップ2（詳細設計）に戻ってループ
* マルチプロジェクト構成の場合のみ、build_script_path にルートの build.gradle のパスを指定します（例: "build.gradle"）

フィードバックループ:
```
詳細設計 → コード生成 → テスト実行評価
    ↑                         ↓
    └──── フィードバック ←────┘
```

### ステップ5: 結合テスト生成

```
@agent_skills/jakarta-ee-api-base/instructions/it_generation.md

結合テストを生成してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
* target_domains: all
* build_script_path: null  # オプション（通常は不要）。マルチプロジェクト構成の場合のみ指定
```

AIが自動で以下を実行
1. basic_design/{domain}/behaviors.md（結合テストシナリオ）を各ドメインから読み込み
2. JUnit 5 + Cucumber + Weld SE を使用した結合テストを生成
   * Service層以下（Service + DAO + Entity + DB）の連携テスト
   * 実際のDBアクセス（メモリDB）
   * 外部APIはWireMockでスタブ化
   * 既存テストがある場合は、削除せずに差分のみを反映する
3. テスト生成後、自動的に結合テストを実行（./gradlew integrationTest）
   * テスト結果を分析し、成功/失敗を報告

### ステップ6: E2Eテスト生成

```
@agent_skills/jakarta-ee-api-base/instructions/e2e_test_generation.md

E2Eテストを生成してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <SPECディレクトリパス>
* build_script_path: null  # オプション（通常は不要）。マルチプロジェクト構成の場合のみ指定
```

AIが自動で以下を実行
1. requirements/behaviors.md（E2Eテストシナリオ）を読み込み
2. JUnit 5 + Cucumber + REST Assured を使用したE2Eテストを生成
   * API層を含む全体フロー
   * 実際のHTTPリクエスト/レスポンス
   * 既存テストがある場合は、削除せずに差分のみを反映する
3. テストデータのセットアップ/クリーンアップコードを生成
4. テスト生成後、自動的にE2Eテストを実行（./gradlew e2eTest）
   * アプリケーションサーバーが起動していることを確認
   * テスト結果を分析し、成功/失敗を報告

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
2. 変更の影響を受けるドメインを識別
3. 影響を受けるドメインの詳細設計・コード・テストを更新
4. CHANGES.mdをアーカイブ

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

### 詳細設計の作成（ドメイン単位）

```
@agent_skills/jakarta-ee-api-base/instructions/detailed_design.md

commonドメインの詳細設計を作成してください

パラメータ
* project_root: projects/sdd-wf/bookstore/back-office-api
* spec_directory: projects/sdd-wf/bookstore/back-office-api/specs/baseline
* target_domain: common
```

### コード生成（ドメイン単位）

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

commonドメインのコードを生成してください

パラメータ
* project_root: projects/sdd-wf/bookstore/back-office-api
* spec_directory: projects/sdd-wf/bookstore/back-office-api/specs/baseline
* target_domain: common
```

その後、他のドメイン（orders, books_proxy等）の詳細設計とコード生成を実施する

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
│   │   ├── behaviors.md            # 結合テスト用
│   │   └── external_interface.md
│   ├── requirements/                # 要件定義用テンプレート
│   │   └── behaviors.md            # E2Eテスト用
│   └── detailed_design/             # 詳細設計用テンプレート
│       ├── detailed_design.md
│       └── behaviors.md            # 単体テスト用
└── instructions/
    ├── basic_design.md               # ステップ1: 基本設計（SPEC作成、ドメインフォルダ構成）
    ├── detailed_design.md            # ステップ2: 詳細設計（ドメイン単位）
    ├── code_generation.md            # ステップ3: コード生成（実装+単体テスト、ドメイン単位）
    ├── unit_test_execution.md        # ステップ4: 単体テスト実行評価
    ├── it_generation.md              # ステップ5: 結合テスト生成（Cucumber + Weld SE）
    ├── e2e_test_generation.md        # ステップ6: E2Eテスト生成（Cucumber + REST Assured）
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
