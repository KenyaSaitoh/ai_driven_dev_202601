# Jakarta EE Microservice Implementation Agent Skill

## 🎯 これは何？

Jakarta EE 10ベースの**マイクロサービス**プロジェクトを実装するための**Agent Skill**です。

**Agent Skills**とは：
- AIコーディングアシスタント用の標準化されたインストラクション形式
- Cursor、GitHub Copilot、ChatGPT、Geminiなど、どのAIでも使える
- プラットフォーム非依存の設計

**back-office-apiの役割:**
- 書籍・在庫・カテゴリ・出版社の完全管理
- BFF（berry-books-api）から呼ばれるバックエンドマイクロサービス
- REST APIとしてデータ管理機能を提供

---

## 🚀 超簡単な使い方（Cursor/Cline）

### 📋 タスク生成

```
@agent_skills/instructions/ja/task_generation.md

タスクを生成してください。

パラメータ:
- project_root: projects/sdd/bookstore/back-office-api-sdd
- spec_directory: projects/sdd/bookstore/back-office-api-sdd/specs
- output_directory: projects/sdd/bookstore/back-office-api-sdd/tasks
```

**これだけ！** Claudeが自動で：
1. 📖 仕様書を読み込む
2. 🔧 タスクファイルを生成する
3. 💾 `tasks/`フォルダに保存する

### ⚙️ コード実装

```
@agent_skills/instructions/ja/code_implementation.md

セットアップタスクを実行してください。

パラメータ:
- project_root: projects/sdd/bookstore/back-office-api-sdd
- task_file: projects/sdd/bookstore/back-office-api-sdd/tasks/setup_tasks.md
```

Claudeが：
1. 📄 タスクと仕様書を読み込む
2. 💻 コードを実装する
3. ✅ テストを作成する
4. ☑️ タスクを完了としてマークする

---

## 💡 実践例

### 例1: プロジェクト立ち上げ

```
@agent_skills/instructions/ja/task_generation.md

back-office-apiの全タスクを生成してください。

パラメータ:
- project_root: projects/sdd/bookstore/back-office-api-sdd
- spec_directory: projects/sdd/bookstore/back-office-api-sdd/specs
```

**結果:**
```
tasks/
├── tasks.md              ← メインタスクリスト
├── setup_tasks.md        ← セットアップ
├── common_tasks.md       ← 共通機能（全エンティティ含む）
├── API_001_books.md      ← 書籍API（2種類の検索含む）
├── API_002_stocks.md     ← 在庫API（楽観的ロック含む）
├── API_003_categories.md ← カテゴリAPI
└── integration_tasks.md  ← 結合テスト
```

### 例2: 書籍APIを実装

```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_001_books.md

書籍APIを実装してください。
JPQL検索とCriteria API検索の両方を実装してください。
```

Claudeが自動実装：
- ✅ Book.java（エンティティ）
- ✅ Publisher.java（エンティティ）
- ✅ Category.java（エンティティ）
- ✅ BookDao.java（JPQL検索）
- ✅ BookDaoCriteria.java（Criteria API検索）
- ✅ BookService.java
- ✅ BookResource.java（REST API）
- ✅ 各種テスト

### 例3: 在庫管理（楽観的ロック）

```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_002_stocks.md

在庫APIを実装してください。
楽観的ロック（@Version）を使った在庫更新を実装してください。
```

Claudeが自動実装：
- ✅ Stock.java（@Versionアノテーション付き）
- ✅ StockDao.java
- ✅ StockService.java（楽観的ロック処理）
- ✅ StockResource.java（REST API）
- ✅ OptimisticLockExceptionMapper（例外ハンドラ）
- ✅ 各種テスト（競合シナリオ含む）

---

## 🎨 便利な使い方

### 複数ファイルを同時参照

```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_001_books.md
@specs/baseline/api/API_001_books/functional_design.md

書籍APIを実装してください。
```

### JPQL検索とCriteria API検索の両方実装

```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_001_books.md

BookDaoでJPQL検索を実装してください。
次に、BookDaoCriteriaでCriteria API検索を実装してください。
両方のDaoをBookServiceから使えるようにしてください。
```

### 楽観的ロックのテスト

```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_002_stocks.md

在庫更新のテストを実装してください。
特に、2つのトランザクションが同時に在庫を更新しようとする
競合シナリオのテストを含めてください。
```

### レビュー依頼

```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_002_stocks.md

在庫API実装が完了しました。
楽観的ロックが正しく実装されているか確認してください。
@Versionアノテーションと競合処理が適切か検証してください。
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

### Day 3: 共通エンティティ実装

```
@agent_skills/instructions/ja/code_implementation.md
@tasks/common_tasks.md

全エンティティ（Book、Stock、Category、Publisher）を実装してください。
Stockエンティティには@Versionアノテーションを付けてください。
```

### Day 4-6: API実装（並行作業可能）

**開発者A（書籍API）:**
```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_001_books.md

書籍APIを実装
```

**開発者B（在庫API）:**
```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_002_stocks.md

在庫APIを実装
```

**開発者C（カテゴリAPI）:**
```
@agent_skills/instructions/ja/code_implementation.md
@tasks/API_003_categories.md

カテゴリAPIを実装
```

### Day 7: 結合テスト

```
@agent_skills/instructions/ja/code_implementation.md
@tasks/integration_tasks.md

結合テストを実行してください。
```

---

## 🎯 back-office-apiの特徴

### マイクロサービスアーキテクチャ
- BFF（berry-books-api）から呼ばれるバックエンドサービス
- REST APIとしてデータ管理機能を提供
- CORS設定でクロスオリジンリクエストに対応
- 独立したデータベースを管理

### 楽観的ロック（Optimistic Locking）
- **Stock**エンティティに`@Version`アノテーションを使用
- 在庫更新時の競合を検出
- `OptimisticLockException`を適切に処理
- 競合時はHTTP 409 Conflictを返す

### 2種類の書籍検索実装

#### JPQL検索（`BookDao`）
- JPQLクエリで動的検索を実装
- カテゴリ、出版社、タイトルでフィルタリング
- シンプルで読みやすいコード

#### Criteria API検索（`BookDaoCriteria`）
- JPA Criteria APIで型安全な検索を実装
- 同じ検索機能をCriteria APIで実現
- コンパイル時の型チェックが効く

**両方の実装を比較学習できる設計！**

### 全エンティティを管理
- ✅ Book（書籍）
- ✅ Stock（在庫）
- ✅ Category（カテゴリ）
- ✅ Publisher（出版社）

**※ BFFとは異なり、すべてのエンティティを実装します**

---

## 🌍 対応プラットフォーム

- ✅ **Cursor** - `@参照` で簡単使用
- ✅ **Cline (VS Code拡張)** - `@参照` で簡単使用
- ✅ **GitHub Copilot** - `#file:参照` で使用
- ✅ **ChatGPT** - 内容をコピペ
- ✅ **Claude.ai** - 内容をコピペ
- ✅ **Gemini** - 内容をコピペ
- ✅ **その他のAI** - API経由でも使用可能

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
│   │   └── microservice_pattern.md     # マイクロサービスパターン説明
│   ├── templates/
│   │   └── task_template.md            # タスクテンプレート
│   └── examples/
│       └── sample_output.md            # 出力例
└── platform_guides/
    ├── cursor_cline.md                 # Cursor/Cline用ガイド
    ├── github_copilot.md               # GitHub Copilot用ガイド
    └── other_platforms.md              # その他プラットフォーム
```

---

## 🔑 重要な実装ポイント

### 1. 楽観的ロック
```java
@Entity
public class Stock {
    @Version
    private int version;  // 楽観的ロック用
    // ...
}
```

### 2. 2種類の検索実装

**JPQL:**
```java
@ApplicationScoped
public class BookDao {
    public List<Book> searchBooks(String keyword, Integer categoryId) {
        // JPQL動的クエリ
    }
}
```

**Criteria API:**
```java
@ApplicationScoped
public class BookDaoCriteria {
    public List<Book> searchBooks(String keyword, Integer categoryId) {
        // Criteria API型安全クエリ
    }
}
```

### 3. マイクロサービスとしてのCORS設定
```java
@Provider
public class CorsFilter implements ContainerResponseFilter {
    // CORSヘッダー設定
}
```

---

## 💬 言語サポート

- 🇯🇵 日本語 (`instructions/ja/`)
- 🇬🇧 英語 (`instructions/en/`)

---

## 📚 詳細ドキュメント

- **プラットフォーム別ガイド**: 
  - Cursor/Cline: `platform_guides/cursor_cline.md`
  - GitHub Copilot: `platform_guides/github_copilot.md`
  - その他: `platform_guides/other_platforms.md`
- **マイクロサービスパターン解説**: `resources/architecture_patterns/microservice_pattern.md`
- **タスクテンプレート**: `resources/templates/task_template.md`
- **出力例**: `resources/examples/sample_output.md`

---

## 🆘 サポート

困ったときは：

1. **プラットフォーム別ガイドを確認** - `platform_guides/`
2. **サンプル出力を参照** - `resources/examples/sample_output.md`
3. **マイクロサービスパターンを理解** - `resources/architecture_patterns/microservice_pattern.md`

---

## 📝 バージョン

- **1.0.0** (2025-01-04): 初回リリース
  - タスク生成機能
  - コード実装ガイダンス
  - マイクロサービスパターンサポート
  - 楽観的ロック実装ガイド
  - 2種類の検索実装（JPQL / Criteria API）
  - プラットフォーム非依存設計

---

## 📜 ライセンス

MIT License

---

## 🤝 コントリビューション

Agent Skills標準仕様に従った貢献を歓迎します：
https://github.com/agentskills/agentskills

