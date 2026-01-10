# Jakarta EE API サービス開発 Agent Skill

## 🎯 これは何？

Jakarta EE 10とJAX-RS 3.1を使った**REST API サービスプロジェクト**全般を実装するための**汎用Agent Skill**です。

**このAgent Skillsに含まれるもの:**
- **instructions/**: 3段階の開発インストラクション（タスク分解、詳細設計、コード生成）
- **principles/**: Jakarta EE開発の共通憲章（全プロジェクトで遵守すべき開発原則、品質基準、アーキテクチャ方針）

**実装可能なアーキテクチャパターン:**

Jakarta EE-based REST APIサービスを実装することで、**結果的に**以下のようなアーキテクチャパターンを実現できます：

- **単体REST APIサービス**: 独立したバックエンドAPI
- **マイクロサービス**: 独立したデータ管理サービス
- **BFF（Backend for Frontend）**: フロントエンド最適化API
- **API統合サービス**: 複数の外部APIを統合

---

## 🚀 超簡単な使い方（3段階プロセス）

### ステップ1: 📋 タスク分解

```
@agent_skills/jakarta-ee-standard/instructions/task_breakdown.md

タスクを分解してください。

パラメータ:
- project_root: <プロジェクトルートパス>
- spec_directory: <仕様書ディレクトリパス>
- output_directory: <タスク出力先パス（オプション）>
```

**これだけ！** AIが自動で：
1. 📖 仕様書を読み込む
2. 🔧 タスクファイルを分解・生成する
3. 💾 `tasks/`フォルダに保存する

### ステップ2: 🎨 詳細設計（NEW!）

```
@agent_skills/jakarta-ee-standard/instructions/detailed_design.md
@<プロジェクトパス>/specs

対象: <API_ID>（例: API_001_auth）

このAPIの詳細設計書を作成してください。
```

**AIと対話しながら**：
1. 📖 仕様書を読み込み、理解内容を説明
2. ❓ 不明点をユーザーに質問
3. 💬 対話で妥当性・充足性を確認
4. 📝 `detailed_design.md`を生成

**なぜ必要？**
- 仕様書の理解を人が確認できる
- 不足情報を補完できる
- コード生成の精度が向上する

### ステップ3: ⚙️ コード生成

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@<プロジェクトパス>/specs/baseline/api/<API_ID>/detailed_design.md

セットアップタスクを実行してください。

パラメータ:
- project_root: <プロジェクトルートパス>
- task_file: <タスクファイルパス>
- skip_infrastructure: true  # インフラセットアップをスキップ（オプション）
```

AIが：
1. 📄 タスクと詳細設計を読み込む
2. 💻 コードを実装する
3. ✅ テストを作成する
4. ☑️ タスクを完了としてマークする

**💡 skip_infrastructureパラメータ:**
- `true`: DB/APサーバーのセットアップをスキップ（既存環境を使用）
- `false`またはパラメータなし: 完全セットアップを実行

---

## 📜 開発憲章

このAgent Skillsには、Jakarta EE開発で遵守すべき**共通憲章**が含まれています：

**場所**: `@agent_skills/jakarta-ee-standard/principles/constitution.md`

**主な原則**:
1. **仕様ファースト開発**: すべての機能開発は詳細な仕様書の作成から始める
2. **アーキテクチャの一貫性**: Jakarta EE 10のベストプラクティスに従う
3. **テスト駆動品質**: すべてのビジネスロジックに対して単体テストを作成
4. **ドキュメント品質の追求**: コードとSPECドキュメントを常に最新に保つ

**注意**: 
- この憲章は全Jakarta EEプロジェクトで共通
- プロジェクト固有の憲章がある場合は、それも併せて遵守してください

---

## 💡 実践例

### 例1: プロジェクト立ち上げ（REST APIサービス - 3段階）

**ステップ1: タスク分解**
```
@agent_skills/jakarta-ee-standard/instructions/task_breakdown.md

全タスクを分解してください。

パラメータ:
- project_root: projects/sdd/bookstore/back-office-api-sdd
- spec_directory: projects/sdd/bookstore/back-office-api-sdd/specs
```

**結果:**
```
tasks/
├── tasks.md              ← メインタスクリスト
├── setup_tasks.md        ← セットアップ
├── common_tasks.md       ← 共通機能
├── API_001_auth.md       ← API別タスク
├── API_002_books.md        
└── integration_tasks.md  ← 結合テスト
```

**ステップ2: 詳細設計（書籍API）**
```
@agent_skills/jakarta-ee-standard/instructions/detailed_design.md
@projects/sdd/bookstore/back-office-api-sdd/specs

対象: API_002_books

書籍APIの詳細設計書を作成してください。
JPQL検索とCriteria API検索の両方を実装する予定です。
```

**AIとの対話:**
```
AI: 理解した内容を説明します...
    【API概要】
    - 書籍API（/api/books）
    - エンドポイント:
      1. GET /api/books - 全書籍取得
      2. GET /api/books/{id} - 書籍詳細
      3. GET /api/books/search/jpql - JPQL検索
      4. GET /api/books/search/criteria - Criteria API検索
    
    【関連エンティティ】
    - Book（テーブル: BOOK + STOCK）
      - @ManyToOne Category
      - @ManyToOne Publisher
      - @SecondaryTable(name="STOCK") for quantity, version
    
    【不明点・確認事項】
    1. 検索条件はkeywordとcategoryIdでよろしいでしょうか？
    2. keywordはbookNameとauthorの両方を対象にしますか？

ユーザー: はい、その通りです。keywordは両方を対象にしてください。

AI: 承知しました。detailed_design.mdを作成しました。
    → specs/baseline/api/API_002_books/detailed_design.md
```

**ステップ3: コード生成**
```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/API_002_books.md
@projects/sdd/bookstore/back-office-api-sdd/specs/baseline/api/API_002_books/detailed_design.md

書籍APIを実装してください。
```

### 例2: フロントエンド最適化API（BFFパターン - 3段階）

**ステップ1: タスク分解**
```
@agent_skills/jakarta-ee-standard/instructions/task_breakdown.md

全タスクを分解してください。

パラメータ:
- project_root: projects/sdd/bookstore/berry-books-api-sdd
- spec_directory: projects/sdd/bookstore/berry-books-api-sdd/specs
```

**ステップ2: 詳細設計（注文API - 独自実装 + 外部API連携）**
```
@agent_skills/jakarta-ee-standard/instructions/detailed_design.md
@projects/sdd/bookstore/berry-books-api-sdd/specs

対象: API_003_orders

注文APIの詳細設計書を作成してください。
独自実装 + 外部API連携で、結果的にBFFパターンになります。
```

**AIとの対話:**
```
AI: 理解した内容を説明します...
    【API概要】
    - 注文API（/api/orders）
    - REST APIサービス（結果的にBFFパターン）
    
    【実装するクラス】
    - OrderResource, OrderService, OrderDao
    - Entity: OrderTran, OrderDetail（ローカルDB）
    
    【外部API連携】
    - BackOfficeRestClient: 在庫チェック、在庫更新
    - 楽観的ロック対応
    
    【不明点・確認事項】
    1. 在庫更新失敗時のロールバック戦略は？
    2. 配送料金の計算ロジックは仕様書にありますか？

ユーザー: 在庫更新失敗時はOptimisticLockExceptionをスローしてください。
         配送料金は東京都内500円、関東圏700円、その他1000円です。

AI: 承知しました。DeliveryFeeServiceを追加します。
    detailed_design.mdを作成しました。
```

**ステップ3: コード生成**
```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@projects/sdd/bookstore/berry-books-api-sdd/specs/baseline/api/API_003_orders/detailed_design.md

注文APIを実装してください。
```

AIが自動実装：
- ✅ エンティティ
- ✅ Dao（JPQL検索 + Criteria API検索）
- ✅ Service
- ✅ Resource（REST API）
- ✅ 各種テスト

### 例4: 楽観的ロック実装

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@projects/sdd/bookstore/back-office-api-sdd/tasks/API_002_stocks.md

在庫APIを実装してください。
楽観的ロック（@Version）を使った在庫更新を実装してください。
```

AIが自動実装：
- ✅ エンティティ（@Versionアノテーション付き）
- ✅ Dao
- ✅ Service（楽観的ロック処理）
- ✅ Resource（REST API）
- ✅ OptimisticLockExceptionMapper（HTTP 409 Conflict）
- ✅ 各種テスト（競合シナリオ含む）

### 例5: 外部API統合 + JWT認証（BFFパターン化）

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@projects/sdd/bookstore/berry-books-api-sdd/tasks/API_001_auth.md

認証APIを実装してください。
```

AIが自動実装：
- ✅ JWT認証基盤（JwtUtil、JwtAuthenFilter、AuthenContext）
- ✅ 外部APIクライアント（RestClient）
- ✅ Resource（REST API）
- ✅ 各種テスト

### 例6: 並行作業（チーム開発）

**開発者A:**
```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@<プロジェクトパス>/tasks/API_001_xxx.md

API_001を実装
```

**開発者B（同時に実行）:**
```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@<プロジェクトパス>/tasks/API_002_yyy.md

API_002を実装
```

**開発者C（同時に実行）:**
```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@<プロジェクトパス>/tasks/API_003_zzz.md

API_003を実装
```

→ **ファイルが衝突しないので並行実行可能！**

---

## 🎨 便利な使い方

### 複数ファイルを同時参照

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@<プロジェクトパス>/tasks/API_001_xxx.md
@<プロジェクトパス>/specs/baseline/api/API_001_xxx/functional_design.md

API_001を実装してください。
```

### 段階的実装

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@<プロジェクトパス>/tasks/API_001_xxx.md

タスクT_API001_001からT_API001_003まで実装してください。
残りは次回やります。
```

### レビュー依頼

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@<プロジェクトパス>/tasks/API_001_xxx.md

全タスク完了しています。
仕様書との整合性をチェックしてください。
```

---

## 🔧 実践的なワークフロー

### Day 1: プロジェクト立ち上げ

```
@agent_skills/jakarta-ee-standard/instructions/task_breakdown.md

プロジェクト全体のタスクを分解してください。
```

→ タスクファイル群が生成される

### Day 2: セットアップ（全員）

**パターンA: フルセットアップ（初回のみ）**
```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@<プロジェクトパス>/tasks/setup_tasks.md

セットアップを実行してください。

パラメータ:
- project_root: <プロジェクトルートパス>
- task_file: <プロジェクトルートパス>/tasks/setup_tasks.md
- skip_infrastructure: false
```

**パターンB: アプリケーションセットアップのみ（開発環境構築済みの場合）**
```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@<プロジェクトパス>/tasks/setup_tasks.md

セットアップを実行してください（インフラセットアップはスキップ）。

パラメータ:
- project_root: <プロジェクトルートパス>
- task_file: <プロジェクトルートパス>/tasks/setup_tasks.md
- skip_infrastructure: true
```

**💡 skip_infrastructureオプション:**
- `false`（デフォルト）: データベースサーバー、アプリケーションサーバーのインストールを含む完全セットアップ
- `true`: インフラは既存環境を使用し、スキーマ作成・初期データ投入・静的リソース配置のみ実行

### Day 3-5: API実装（並行作業）

各担当者が独立してAPIを実装

### Day 6: 結合テスト

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@<プロジェクトパス>/tasks/integration_tasks.md

結合テストを実行してください。
```

---

## 🎯 対応する主要機能

### Jakarta EE-based REST API
本質的には**Jakarta EE 10とJAX-RS 3.1を使ったREST APIサービス**の開発を支援します。
実装方法により、結果的に以下のようなパターンを実現できます：

### 独立したデータ管理API（マイクロサービス化可能）
- REST APIとしてのデータ提供
- CORS設定でクロスオリジン対応
- 全エンティティの完全管理
- 独立したデータベース管理

### フロントエンド最適化API（BFFパターン化可能）
- フロントエンド向けに最適化されたエンドポイント
- 複数の外部APIを統合
- **プロキシパターン**: 外部APIへの透過的転送
- **独自実装パターン**: JWT認証、独自ビジネスロジック

### 楽観的ロック（Optimistic Locking）
- `@Version`アノテーションを使用
- 更新時の競合を検出
- `OptimisticLockException`を適切に処理
- 競合時はHTTP 409 Conflictを返す

### 2種類の検索実装

#### JPQL検索
- JPQLクエリで動的検索を実装
- シンプルで読みやすいコード

#### Criteria API検索
- JPA Criteria APIで型安全な検索を実装
- コンパイル時の型チェックが効く

**両方の実装を比較学習できる設計！**

### REST API統合
- 外部APIクライアント（JAX-RS Client）
- マイクロサービス間連携
- タイムアウト、リトライ処理
- エラーハンドリング

### JWT認証
- JWT生成・検証
- 認証フィルター
- 認証コンテキスト
- 権限チェック

### CORS対応
- クロスオリジンリクエスト許可
- レスポンスヘッダー設定
- プリフライトリクエスト対応

---

## 📁 ディレクトリ構造

```
agent_skills/
├── SKILL.md                          # Agent Skill説明書
├── README.md                         # このファイル
└── instructions/
    ├── task_breakdown.md             # タスク分解インストラクション
    ├── detailed_design.md            # 詳細設計インストラクション
    └── code_generation.md            # コード生成インストラクション
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
