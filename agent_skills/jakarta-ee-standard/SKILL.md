---
name: jakarta-ee-api-service-development
description: Jakarta EE 10とJAX-RS 3.1を使ったREST APIサービス開発を支援。単体API、マイクロサービス、BFFなど多様なアーキテクチャに対応。仕様書からタスク分解、詳細設計、コード実装まで3段階で一貫サポート。
---

# Jakarta EE API サービス開発 Agent Skill

## 🎯 これは何？

Jakarta EE 10とJAX-RS 3.1を使ったREST API サービスプロジェクト全般を実装するための汎用Agent Skillです。

Agent Skillsとは：
* AIコーディングエージェント用の標準化されたインストラクション形式
* Cursor、Claude等のAIツールで使える
* タスク分解 → 詳細設計 → コード実装の3段階で一貫した開発支援

このAgent Skillsに含まれるもの:
* instructions/: 3段階の開発インストラクション（タスク分解、詳細設計、コード生成）
* principles/: Jakarta EE開発の共通憲章（開発原則、品質基準、アーキテクチャ方針）

実装可能なアーキテクチャパターン:

Jakarta EE-based REST APIサービスを実装することで、結果的に以下のようなアーキテクチャパターンを実現できます：

* 単体REST APIサービス: 独立したバックエンドAPI
* マイクロサービス: 独立したデータ管理サービス（例: back-office-api）
* BFF（Backend for Frontend）: フロントエンド最適化API（例: berry-books-api）
* API統合サービス: 複数の外部APIを統合するサービス

---

## 🚀 使い方（3段階プロセス）

### ステップ1: 📋 タスク分解

```
@agent_skills/jakarta-ee-standard/instructions/task_breakdown.md

タスクを分解してください。

パラメータ:
* project_root: <プロジェクトルートパス>
* spec_directory: <仕様書ディレクトリパス>
* output_directory: <タスク出力先パス>
```

これだけ！ AIが自動で：
1. 📖 仕様書を読み込む
2. 🔧 タスクファイルを分解・生成する
3. 💾 `tasks/`フォルダに保存する

### ステップ2: 🎨 詳細設計

```
@agent_skills/jakarta-ee-standard/instructions/detailed_design.md

対象: <API_ID>（例: API_001_auth）

このAPIの詳細設計書を作成してください。
```

AIと対話しながら：
1. 📖 仕様書を読み込み、理解内容を説明
2. ❓ 不明点をユーザーに質問
3. 💬 対話で妥当性・充足性を確認
4. 📝 `detailed_design.md`を生成

### ステップ3: ⚙️ コード生成

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md

セットアップタスクを実行してください。

パラメータ:
* project_root: <プロジェクトルートパス>
* task_file: <タスクファイルパス>
```

AIが：
1. 📄 タスクと詳細設計を読み込む
2. 💻 コードを実装する
3. ✅ テストを作成する
4. ☑️ タスクを完了としてマークする

---

## 💡 実践例

### 例1: REST APIサービスプロジェクトの立ち上げ（3段階）

ステップ1: タスク分解
```
@agent_skills/jakarta-ee-standard/instructions/task_breakdown.md

全タスクを分解してください。

パラメータ:
* project_root: projects/sdd/bookstore/back-office-api-sdd
* spec_directory: projects/sdd/bookstore/back-office-api-sdd/specs
```

結果:
```
tasks/
├── tasks.md              ← メインタスクリスト
├── setup_tasks.md        ← セットアップ
├── common_tasks.md       ← 共通機能
├── API_001_auth.md       ← API別タスク
├── API_002_books.md        
└── integration_tasks.md  ← 結合テスト
```

ステップ2: 詳細設計（書籍API）
```
@agent_skills/jakarta-ee-standard/instructions/detailed_design.md
@projects/sdd/bookstore/back-office-api-sdd/specs

対象: API_002_books

書籍APIの詳細設計書を作成してください。
JPQL検索とCriteria API検索の両方を実装する予定です。
```

AIとの対話例:
```
AI: 理解した内容を説明します...
    * 書籍API（/api/books）
    * 全書籍取得、詳細取得、検索（JPQL/Criteria API）
    * Entity: Book（@ManyToOne Category, Publisher）
    
    不明点: 検索条件はkeywordとcategoryIdでよろしいでしょうか？

ユーザー: はい、その通りです。

AI: detailed_design.mdを作成しました。
    specs/baseline/api/API_002_books/detailed_design.md
```

ステップ3: コード生成
```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/API_002_books.md
@projects/sdd/bookstore/back-office-api-sdd/specs/baseline/api/API_002_books/detailed_design.md

書籍APIを実装してください。
```

### 例2: APIを実装（JPQL + Criteria API検索）

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/API_001_books.md

書籍APIを実装してください。
JPQL検索とCriteria API検索の両方を実装してください。
```

AIが自動実装：
* ✅ エンティティ
* ✅ Dao（JPQL検索 + Criteria API検索）
* ✅ Service
* ✅ Resource（REST API）
* ✅ 各種テスト

### 例3: 楽観的ロック実装

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/API_002_stocks.md

在庫APIを実装してください。
楽観的ロック（@Version）を使った在庫更新を実装してください。
```

AIが自動実装：
* ✅ エンティティ（@Versionアノテーション付き）
* ✅ Dao
* ✅ Service（楽観的ロック処理）
* ✅ Resource（REST API）
* ✅ OptimisticLockExceptionMapper（HTTP 409 Conflict）
* ✅ 各種テスト（競合シナリオ含む）

### 例4: BFFパターン（外部API統合 + JWT認証）

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@projects/sdd/bookstore/berry-books-api-sdd/tasks/API_001_auth.md

認証APIを実装してください（JWT + 外部API連携）。
```

AIが自動実装：
* ✅ JWT認証基盤（JwtUtil、JwtAuthenFilter、AuthenContext）
* ✅ 外部APIクライアント（RestClient）
* ✅ Resource（REST API）
* ✅ 各種テスト

---

## 🎯 対応する主要機能

### Jakarta EE-based REST API
本質的にはJakarta EE 10とJAX-RS 3.1を使ったREST APIサービスの開発を支援します。
実装方法により、結果的に以下のようなパターンを実現できます：

### 独立したデータ管理API（マイクロサービス化可能）
* REST APIとしてのデータ提供
* CORS設定でクロスオリジン対応
* 全エンティティの完全管理
* 独立したデータベース管理

### フロントエンド最適化API（BFFパターン化可能）
* フロントエンド向けに最適化されたエンドポイント
* 複数の外部APIを統合
* プロキシパターン: 外部APIへの透過的転送
* 独自実装パターン: JWT認証、独自ビジネスロジック

### 楽観的ロック（Optimistic Locking）
* `@Version`アノテーションを使用
* 更新時の競合検出
* `OptimisticLockException`処理
* HTTP 409 Conflictレスポンス

### 2種類の検索実装
* JPQL検索: 動的クエリ、シンプルで読みやすい
* Criteria API検索: 型安全、コンパイル時チェック
* 両方実装: 比較学習が可能

### REST API統合
* 外部APIクライアント（JAX-RS Client）
* マイクロサービス間連携
* タイムアウト、リトライ処理
* エラーハンドリング

### JWT認証
* JWT生成・検証
* 認証フィルター
* 認証コンテキスト
* 権限チェック

### CORS対応
* クロスオリジンリクエスト許可
* レスポンスヘッダー設定
* プリフライトリクエスト対応

---

## 📁 ディレクトリ構造

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

* JPQL:
```java
@ApplicationScoped
public class BookDao {
    public List<Book> searchBooks(String keyword, Integer categoryId) {
        // JPQL動的クエリ
    }
}
```

* Criteria API:
```java
@ApplicationScoped
public class BookDaoCriteria {
    public List<Book> searchBooks(String keyword, Integer categoryId) {
        // Criteria API型安全クエリ
    }
}
```

### 3. CORS設定
```java
@Provider
public class CorsFilter implements ContainerResponseFilter {
    // CORSヘッダー設定
}
```

### 4. 外部API連携
```java
@ApplicationScoped
public class BackOfficeRestClient {
    private Client client;
    
    @ConfigProperty(name = "back-office-api.base-url")
    private String baseUrl;
    
    public BookTO getBook(Integer bookId) {
        // REST API呼び出し
    }
}
```

### 5. JWT認証
```java
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenFilter implements ContainerRequestFilter {
    // JWT検証とコンテキスト設定
}
```
