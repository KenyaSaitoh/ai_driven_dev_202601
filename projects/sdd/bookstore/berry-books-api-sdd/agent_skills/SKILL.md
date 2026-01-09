---
name: jakarta-ee-bff-implementation
description: Jakarta EE 10 BFF（Backend for Frontend）パターンの実装を支援。外部API統合、JWT認証、注文処理など、フロントエンド最適化されたAPIの実装をサポート。
---

# Jakarta EE BFF実装 Agent Skill

## 🎯 これは何？

Jakarta EE 10ベースの**BFF（Backend for Frontend）**プロジェクトを実装するための**Agent Skill**です。

**Agent Skills**とは：
- AIコーディングエージェント用の標準化されたインストラクション形式
- Cursor、Claude等のAIツールで使える
- タスク生成からコード実装まで一貫した開発支援

**berry-books-apiの役割:**
- フロントエンド（berry-books-spa）の唯一のエントリーポイント
- 外部APIを統合してフロントエンド向けに最適化されたAPIを提供
- BFF（Backend for Frontend）パターンを採用

---

## 🚀 使い方

### 📋 タスク生成

```
@agent_skills/instructions/task_generation.md

タスクを生成してください。

パラメータ:
- project_root: projects/sdd/bookstore/berry-books-api-sdd
- spec_directory: projects/sdd/bookstore/berry-books-api-sdd/specs
- output_directory: projects/sdd/bookstore/berry-books-api-sdd/tasks
```

**これだけ！** AIが自動で：
1. 📖 仕様書を読み込む
2. 🔧 タスクファイルを生成する
3. 💾 `tasks/`フォルダに保存する

### ⚙️ コード実装

```
@agent_skills/instructions/code_implementation.md

セットアップタスクを実行してください。

パラメータ:
- project_root: projects/sdd/bookstore/berry-books-api-sdd
- task_file: projects/sdd/bookstore/berry-books-api-sdd/tasks/setup_tasks.md
```

AIが：
1. 📄 タスクと仕様書を読み込む
2. 💻 コードを実装する
3. ✅ テストを作成する
4. ☑️ タスクを完了としてマークする

---

## 💡 実践例

### 例1: プロジェクト立ち上げ

```
@agent_skills/instructions/task_generation.md

berry-books-apiの全タスクを生成してください。

パラメータ:
- project_root: projects/sdd/bookstore/berry-books-api-sdd
- spec_directory: projects/sdd/bookstore/berry-books-api-sdd/specs
```

**結果:**
```
tasks/
├── tasks.md              ← メインタスクリスト
├── setup_tasks.md        ← セットアップ
├── common_tasks.md       ← 共通機能
├── API_001_auth.md       ← 認証API
├── API_002_books.md      ← 書籍API
├── API_003_orders.md     ← 注文API
└── integration_tasks.md  ← 結合テスト
```

### 例2: 認証APIを実装

```
@agent_skills/instructions/code_implementation.md
@tasks/API_001_auth.md

認証APIを実装してください。
```

AIが自動実装：
- ✅ LoginRequest.java
- ✅ RegisterRequest.java
- ✅ CustomerHubRestClient.java
- ✅ JwtUtil.java
- ✅ JwtAuthenFilter.java
- ✅ AuthenContext.java
- ✅ AuthenResource.java
- ✅ 各種テスト

### 例3: 並行作業（チーム開発）

**開発者A（認証API）:**
```
@agent_skills/instructions/code_implementation.md
@tasks/API_001_auth.md

認証APIを実装
```

**開発者B（書籍API、同時に実行）:**
```
@agent_skills/instructions/code_implementation.md
@tasks/API_002_books.md

書籍APIを実装
```

**開発者C（注文API、同時に実行）:**
```
@agent_skills/instructions/code_implementation.md
@tasks/API_003_orders.md

注文APIを実装
```

→ **ファイルが衝突しないので並行実行可能！**

---

## 🎯 BFFパターンの特徴

### プロキシパターン
- BookResource、CategoryResource → 外部APIへ透過的転送
- 独自のビジネスロジックなし

### 独自実装パターン
- AuthenResource → JWT認証を実装
- OrderResource → 注文処理を実装

### データ管理の制約
- **実装する**: OrderTran、OrderDetail（注文関連のみ）
- **実装しない**: Book、Stock、Category、Customer（外部API管理）

---

## 📁 ディレクトリ構造

```
agent_skills/
├── SKILL.md                          # このファイル
├── README.md                         # クイックスタートガイド
└── instructions/
    ├── task_generation.md            # タスク生成インストラクション
    └── code_implementation.md        # コード実装インストラクション
```

---

## 📝 バージョン

- **1.0.0** (2025-01-04): 初回リリース
  - タスク生成機能
  - コード実装ガイダンス
  - BFFパターンサポート

---

## 📜 ライセンス

MIT License
