# Jakarta EE マイクロサービス実装 Agent Skill

## 🎯 これは何？

Jakarta EE 10ベースの**マイクロサービス**プロジェクトを実装するための**Agent Skill**です。

**back-office-apiの役割:**
- 書籍・在庫・カテゴリ・出版社の完全管理
- BFF（berry-books-api）から呼ばれるバックエンドマイクロサービス
- REST APIとしてデータ管理機能を提供

---

## 🚀 超簡単な使い方

### 📋 タスク生成

```
@projects/sdd/bookstore/back-office-api-sdd/agent_skills/instructions/task_generation.md

タスクを生成してください。

パラメータ:
- project_root: projects/sdd/bookstore/back-office-api-sdd
- spec_directory: projects/sdd/bookstore/back-office-api-sdd/specs
- output_directory: projects/sdd/bookstore/back-office-api-sdd/tasks
```

**これだけ！** AIが自動で：
1. 📖 仕様書を読み込む
2. 🔧 タスクファイルを生成する
3. 💾 `tasks/`フォルダに保存する

### ⚙️ コード実装

```
@projects/sdd/bookstore/back-office-api-sdd/agent_skills/instructions/code_implementation.md

セットアップタスクを実行してください。

パラメータ:
- project_root: projects/sdd/bookstore/back-office-api-sdd
- task_file: projects/sdd/bookstore/back-office-api-sdd/tasks/setup_tasks.md
- skip_infrastructure: true  # インフラセットアップをスキップ（オプション）
```

AIが：
1. 📄 タスクと仕様書を読み込む
2. 💻 コードを実装する
3. ✅ テストを作成する
4. ☑️ タスクを完了としてマークする

**💡 skip_infrastructureパラメータ:**
- `true`: DB/APサーバーのセットアップをスキップ（既存環境を使用）
- `false`またはパラメータなし: 完全セットアップを実行

---

## 💡 実践例

### 例1: プロジェクト立ち上げ

```
@projects/sdd/bookstore/back-office-api-sdd/agent_skills/instructions/task_generation.md

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
@projects/sdd/bookstore/back-office-api-sdd/agent_skills/instructions/code_implementation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/API_001_books.md

書籍APIを実装してください。
JPQL検索とCriteria API検索の両方を実装してください。
```

AIが自動実装：
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
@projects/sdd/bookstore/back-office-api-sdd/agent_skills/instructions/code_implementation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/API_002_stocks.md

在庫APIを実装してください。
楽観的ロック（@Version）を使った在庫更新を実装してください。
```

AIが自動実装：
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
@projects/sdd/bookstore/back-office-api-sdd/agent_skills/instructions/code_implementation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/API_001_books.md
@projects/sdd/bookstore/back-office-api-sdd/specs/baseline/api/API_001_books/functional_design.md

書籍APIを実装してください。
```

### JPQL検索とCriteria API検索の両方実装

```
@projects/sdd/bookstore/back-office-api-sdd/agent_skills/instructions/code_implementation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/API_001_books.md

BookDaoでJPQL検索を実装してください。
次に、BookDaoCriteriaでCriteria API検索を実装してください。
両方のDaoをBookServiceから使えるようにしてください。
```

### 楽観的ロックのテスト

```
@projects/sdd/bookstore/back-office-api-sdd/agent_skills/instructions/code_implementation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/API_002_stocks.md

在庫更新のテストを実装してください。
特に、2つのトランザクションが同時に在庫を更新しようとする
競合シナリオのテストを含めてください。
```

### レビュー依頼

```
@projects/sdd/bookstore/back-office-api-sdd/agent_skills/instructions/code_implementation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/API_002_stocks.md

在庫API実装が完了しました。
楽観的ロックが正しく実装されているか確認してください。
@Versionアノテーションと競合処理が適切か検証してください。
```

---

## 🔧 実践的なワークフロー

### Day 1: プロジェクト立ち上げ

```
@projects/sdd/bookstore/back-office-api-sdd/agent_skills/instructions/task_generation.md

プロジェクト全体のタスクを生成してください。
```

→ タスクファイル群が生成される

### Day 2: セットアップ（全員）

**パターンA: フルセットアップ（初回のみ）**
```
@projects/sdd/bookstore/back-office-api-sdd/agent_skills/instructions/code_implementation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/setup_tasks.md

セットアップを実行してください。

パラメータ:
- project_root: projects/sdd/bookstore/back-office-api-sdd
- task_file: projects/sdd/bookstore/back-office-api-sdd/tasks/setup_tasks.md
- skip_infrastructure: false
```

**パターンB: アプリケーションセットアップのみ（開発環境構築済みの場合）**
```
@projects/sdd/bookstore/back-office-api-sdd/agent_skills/instructions/code_implementation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/setup_tasks.md

セットアップを実行してください（インフラセットアップはスキップ）。

パラメータ:
- project_root: projects/sdd/bookstore/back-office-api-sdd
- task_file: projects/sdd/bookstore/back-office-api-sdd/tasks/setup_tasks.md
- skip_infrastructure: true
```

**💡 skip_infrastructureオプション:**
- `false`（デフォルト）: データベースサーバー、アプリケーションサーバーのインストールを含む完全セットアップ
- `true`: インフラは既存環境を使用し、スキーマ作成・初期データ投入・静的リソース配置のみ実行

### Day 3: 共通エンティティ実装

```
@projects/sdd/bookstore/back-office-api-sdd/agent_skills/instructions/code_implementation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/common_tasks.md

全エンティティ（Book、Stock、Category、Publisher）を実装してください。
Stockエンティティには@Versionアノテーションを付けてください。
```

### Day 4-6: API実装（並行作業可能）

**開発者A（書籍API）:**
```
@projects/sdd/bookstore/back-office-api-sdd/agent_skills/instructions/code_implementation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/API_001_books.md

書籍APIを実装
```

**開発者B（在庫API）:**
```
@projects/sdd/bookstore/back-office-api-sdd/agent_skills/instructions/code_implementation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/API_002_stocks.md

在庫APIを実装
```

**開発者C（カテゴリAPI）:**
```
@projects/sdd/bookstore/back-office-api-sdd/agent_skills/instructions/code_implementation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/API_003_categories.md

カテゴリAPIを実装
```

### Day 7: 結合テスト

```
@projects/sdd/bookstore/back-office-api-sdd/agent_skills/instructions/code_implementation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/integration_tasks.md

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

## 📁 ディレクトリ構造

```
agent_skills/
├── SKILL.md                          # Agent Skill説明書
├── README.md                         # このファイル
└── instructions/
    ├── task_generation.md            # タスク生成インストラクション
    └── code_implementation.md        # コード実装インストラクション
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

## 📝 バージョン

- **1.0.0** (2025-01-04): 初回リリース
  - タスク生成機能
  - コード実装ガイダンス
  - マイクロサービスパターンサポート
  - 楽観的ロック実装ガイド
  - 2種類の検索実装（JPQL / Criteria API）

---

## 📜 ライセンス

MIT License
