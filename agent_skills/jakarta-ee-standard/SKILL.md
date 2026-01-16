---
name: jakarta-ee-api-service-development
description: Jakarta EE 10とJAX-RS 3.1を使ったREST APIサービス開発を支援。エンティティ実装、外部API連携など多様な実装要件に対応。仕様書からタスク分解、詳細設計、コード実装まで3段階で一貫サポート。
---

# Jakarta EE API サービス開発 Agent Skill

## 使い方（3段階プロセス）

### ステップ1: タスク分解

```
@agent_skills/jakarta-ee-standard/instructions/task_breakdown.md

タスクを分解してください

パラメータ
* project_root: <プロジェクトルートパス>
* spec_directory: <仕様書ディレクトリパス>
* output_directory: <タスク出力先パス>
```

AIが自動で以下を実行する
1. 仕様書を読み込む
2. タスクファイルを分解・生成する
3. `tasks/`フォルダに保存する

### ステップ2: 詳細設計

```
@agent_skills/jakarta-ee-standard/instructions/detailed_design.md

対象: <API_ID>（例: API_001_auth）

このAPIの詳細設計書を作成してください
```

AIと対話しながら以下を実施する
1. 仕様書を読み込み、理解内容を説明する
2. 不明点をユーザーに質問する
3. 対話で妥当性・充足性を確認する
4. `detailed_design.md`を生成する

### ステップ3: コード生成

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md

セットアップタスクを実行してください

パラメータ
* project_root: <プロジェクトルートパス>
* task_file: <タスクファイルパス>
```

AIが以下を実施する
1. タスクと詳細設計を読み込む
2. コードを実装する
3. テストを作成する
4. タスクを完了としてマークする

---

## 実践例

```
@agent_skills/jakarta-ee-standard/instructions/task_breakdown.md

全タスクを分解してください

パラメータ
* project_root: projects/sdd/bookstore/back-office-api-sdd
* spec_directory: projects/sdd/bookstore/back-office-api-sdd/specs
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
agent_skills/jakarta-ee-standard/
├── SKILL.md                          # このファイル
├── README.md                         # クイックスタートガイド
├── principles/                       # 開発憲章（全プロジェクト共通）
│   └── constitution.md              # Jakarta EE開発憲章
└── instructions/
    ├── task_breakdown.md             # ステップ1: タスク分解
    ├── detailed_design.md            # ステップ2: 詳細設計
    └── code_generation.md            # ステップ3: コード生成
```

---

## 参考資料

* [Jakarta EE 10仕様](https://jakarta.ee/specifications/platform/10/)
* [JAX-RS 3.1仕様](https://jakarta.ee/specifications/restful-ws/3.1/)
* [Jakarta Persistence 3.1仕様](https://jakarta.ee/specifications/persistence/3.1/)
* [Jakarta CDI 4.0仕様](https://jakarta.ee/specifications/cdi/4.0/)
