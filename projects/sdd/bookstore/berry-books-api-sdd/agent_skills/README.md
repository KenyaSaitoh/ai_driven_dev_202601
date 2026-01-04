# Jakarta EE BFF Implementation Agent Skill

## 🎯 これは何？

Jakarta EE 10ベースのBFF（Backend for Frontend）プロジェクトを実装するための**Agent Skill**です。

**Agent Skills**とは：
- AIコーディングエージェント用の標準化されたインストラクション形式
- Claude Code、Cline、Cursor、GitHub Copilotで使える
- プラットフォーム非依存の設計

---

## 🚀 超簡単な使い方（Claude Code/Cline/Cursor）

### 📋 タスク生成

```
@agent_skills/instructions/ja/task_generation.md

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
@agent_skills/instructions/ja/code_implementation.md

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
@agent_skills/instructions/ja/task_generation.md

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
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_001_auth.md

認証APIを実装してください。
完了したらタスクファイルにチェック[X]を入れてください。
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
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_001_auth.md

認証APIを実装
```

**開発者B（書籍API、同時に実行）:**
```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_002_books.md

書籍APIを実装
```

**開発者C（注文API、同時に実行）:**
```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_003_orders.md

注文APIを実装
```

→ **ファイルが衝突しないので並行実行可能！**

---

## 🎨 便利な使い方

### 複数ファイルを同時参照

```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_001_auth.md
@specs/baseline/api/API_001_auth/functional_design.md

認証APIを実装してください。
```

### 段階的実装

```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_001_auth.md

タスクT_API001_001からT_API001_003まで実装してください。
残りは次回やります。
```

### レビュー依頼

```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_001_auth.md

全タスク完了しています。
仕様書との整合性をチェックしてください。
特にBFFパターンの制約に違反していないか確認してください。
```

### 修正依頼

```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_001_auth.md
@src/main/java/pro/kensait/berrybooks/api/AuthenResource.java

テストが失敗しています。
仕様書に従って修正してください。
```

---

## 🔧 実践的なワークフロー

### Day 1: プロジェクト立ち上げ

```
@agent_skills/instructions/ja/task_generation.md

プロジェクト全体のタスクを生成してください。
```

→ タスクファイル群が生成される

### Day 2: セットアップ（全員）

```
@agent_skills/instructions/ja/code_implementation.md
@tasks/setup_tasks.md

セットアップを実行してください。
```

### Day 3-5: API実装（並行作業）

各担当者が独立してAPIを実装

### Day 6: 結合テスト

```
@agent_skills/instructions/ja/code_implementation.md
@tasks/integration_tasks.md

結合テストを実行してください。
```

---

## 🌍 対応プラットフォーム

- ✅ **Claude Code** - `@参照` で簡単使用
- ✅ **Cline (VS Code拡張)** - `@参照` で簡単使用
- ✅ **Cursor** - `@参照` で簡単使用
- ✅ **GitHub Copilot** - `#file:参照` で使用

詳細は `platform_guides/` を参照してください。

---

## 📁 ディレクトリ構造

```
agent_skills/
├── skill.yaml                          # Agent Skills メタデータ
├── README.md                           # このファイル
├── instructions/
│   ├── en/                             # 英語版
│   │   ├── task_generation.md
│   │   └── code_implementation.md
│   └── ja/                             # 日本語版
│       ├── task_generation.md
│       └── code_implementation.md
├── resources/
│   ├── architecture_patterns/
│   │   └── bff_pattern.md              # BFFパターン詳細説明
│   ├── templates/
│   │   └── task_template.md            # タスクテンプレート
│   └── examples/
│       └── sample_output.md            # 出力例
└── platform_guides/
    ├── cursor_cline.md                 # Claude Code/Cline/Cursor用ガイド
    └── github_copilot.md               # GitHub Copilot用ガイド
```

---

## 🎯 BFFパターンの特徴

このAgent Skillは、BFF（Backend for Frontend）パターンに特化しています：

### プロキシパターン
- BookResource、CategoryResource → 外部APIへ透過的転送
- 独自のビジネスロジックなし

### 独自実装パターン
- AuthenResource → JWT認証を実装
- OrderResource → 注文処理を実装

### データ管理の制約
- **実装する**: OrderTran、OrderDetail（注文関連のみ）
- **実装しない**: Book、Stock、Category、Customer（外部API管理）

詳細は `resources/architecture_patterns/bff_pattern.md` を参照してください。

---

## 💬 言語サポート

- 🇯🇵 日本語 (`instructions/ja/`)
- 🇬🇧 英語 (`instructions/en/`)

---

## 📚 詳細ドキュメント

- **プラットフォーム別ガイド**: 
  - Claude Code/Cline/Cursor: `platform_guides/cursor_cline.md`
  - GitHub Copilot: `platform_guides/github_copilot.md`
- **BFFパターン解説**: `resources/architecture_patterns/bff_pattern.md`
- **タスクテンプレート**: `resources/templates/task_template.md`
- **出力例**: `resources/examples/sample_output.md`

---

## 🆘 サポート

困ったときは：

1. **プラットフォーム別ガイドを確認** - `platform_guides/`
2. **サンプル出力を参照** - `resources/examples/sample_output.md`
3. **BFFパターンを理解** - `resources/architecture_patterns/bff_pattern.md`

---

## 📝 バージョン

- **1.0.0** (2025-01-04): 初回リリース
  - タスク生成機能
  - コード実装ガイダンス
  - BFFパターンサポート
  - プラットフォーム非依存設計

---

## 📜 ライセンス

MIT License

---

## 🤝 コントリビューション

Agent Skills標準仕様に従った貢献を歓迎します：
https://github.com/agentskills/agentskills

