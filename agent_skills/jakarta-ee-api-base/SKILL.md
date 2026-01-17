---
name: jakarta-ee-api-service-development
description: Jakarta EE 10とJAX-RS 3.1を使ったREST APIサービス開発を支援。エンティティ実装、外部API連携など多様な実装要件に対応。仕様書からタスク分解、詳細設計、コード生成まで3段階で一貫サポート。
---

# Jakarta EE API サービス開発 Agent Skill

## 使い方（4段階プロセス）

### ステップ1: 基本設計（仕様書作成）

```
@agent_skills/jakarta-ee-api-base/instructions/basic_design.md

仕様書を作成してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <仕様書ディレクトリパス>
```

AIと対話しながら以下を実施（対話的プロセス）
1. テンプレートを所定のフォルダに展開
2. requirements.mdを読み込み、理解内容を説明
3. ユーザーと対話しながら各仕様書の中身を埋める
4. システムレベル仕様書（architecture_design.md、functional_design.md等）を作成
5. API単位仕様書（api/配下）を作成

注意: requirements.md（要件定義書）は所与とする（既に存在している前提）

### ステップ2: タスク分解

```
@agent_skills/jakarta-ee-api-base/instructions/task_breakdown.md

タスクを分解してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <仕様書ディレクトリパス>
* output_directory: <タスク出力先パス>
```

AIが自動で以下を実行
1. 仕様書を読み込み
2. タスクファイルを分解・生成
3. `tasks/`フォルダに保存

### ステップ3: 詳細設計

```
@agent_skills/jakarta-ee-api-base/instructions/detailed_design.md

API詳細設計書を作成してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <仕様書ディレクトリパス>
* api_id: <API_ID>（例: API_001_auth）
```

AIと対話しながら以下を実施（対話的プロセス）
1. 仕様書を読み込み、理解内容を説明
2. 不明点をユーザーに質問
3. 対話で妥当性・充足性を確認
4. `detailed_design.md`を生成

### ステップ4: コード生成

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

セットアップタスクを実行してください

パラメータ
* project_root: <プロジェクトルートパス>
* task_file: <タスクファイルパス>
```

AIが自動で以下を実行
1. タスクと詳細設計を読み込み
2. コードを生成
3. テストを作成
4. タスクを完了としてマーク

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
├── templates/                        # 仕様書テンプレート
│   ├── architecture_design.md
│   ├── functional_design.md
│   ├── data_model.md
│   ├── behaviors.md
│   └── external_interface.md
└── instructions/
    ├── basic_design.md               # ステップ1: 基本設計（仕様書作成）
    ├── task_breakdown.md             # ステップ2: タスク分解
    ├── detailed_design.md            # ステップ3: 詳細設計
    └── code_generation.md            # ステップ4: コード生成
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
