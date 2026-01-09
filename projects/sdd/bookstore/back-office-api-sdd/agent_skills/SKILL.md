---
name: jakarta-ee-microservice-implementation
description: Jakarta EE 10マイクロサービスプロジェクトの実装を支援。仕様書からタスク生成、コード実装、テスト作成まで一貫サポート。楽観的ロック、JPQL/Criteria API検索、CORS対応を含む。
---

# Jakarta EE マイクロサービス実装 Agent Skill

## 🎯 これは何？

Jakarta EE 10ベースの**マイクロサービス**プロジェクトを実装するための**Agent Skill**です。

**Agent Skills**とは：
- AIコーディングエージェント用の標準化されたインストラクション形式
- Cursor、Claude等のAIツールで使える
- タスク生成からコード実装まで一貫した開発支援

**back-office-apiの役割:**
- 書籍・在庫・カテゴリ・出版社の完全管理
- BFF（berry-books-api）から呼ばれるバックエンドマイクロサービス
- REST APIとしてデータ管理機能を提供

---

## 🚀 使い方

### 📋 タスク生成

```
@agent_skills/instructions/task_generation.md

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
@agent_skills/instructions/code_implementation.md

セットアップタスクを実行してください。

パラメータ:
- project_root: projects/sdd/bookstore/back-office-api-sdd
- task_file: projects/sdd/bookstore/back-office-api-sdd/tasks/setup_tasks.md
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
@agent_skills/instructions/code_implementation.md
@tasks/API_001_books.md

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
@agent_skills/instructions/code_implementation.md
@tasks/API_002_stocks.md

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
├── SKILL.md                          # このファイル
├── README.md                         # クイックスタートガイド
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
