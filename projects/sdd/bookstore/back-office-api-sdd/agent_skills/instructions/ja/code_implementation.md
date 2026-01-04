# コード実装インストラクション

## Agent Skillについて

このインストラクションは**Agent Skills標準仕様**（v1.0）に準拠しています。

**プラットフォーム非依存:**
- Claude (Anthropic)
- GitHub Copilot
- ChatGPT (OpenAI)
- Gemini (Google)
- Cursor
- その他のAIコーディングアシスタント

上記いずれのAIプラットフォームでも利用可能です。

**プラットフォーム固有の操作方法:**
- ファイル読み込み、ファイル作成、編集などの具体的な操作方法は、プラットフォームによって異なります
- 詳細は `../../platform_guides/` を参照してください

---

## パラメータ設定

**実行前に以下のパラメータを設定してください:**

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
task_file: "ここに実行するタスクファイルのパスを入力"
```

**例（back-office-apiの場合）:**
```yaml
project_root: "projects/sdd/bookstore/back-office-api-sdd"
task_file: "projects/sdd/bookstore/back-office-api-sdd/tasks/setup_tasks.md"
```

**注意:** 
- パス区切りはOS環境に応じて調整してください（Windows: `\`, Unix/Linux/Mac: `/`）
- 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換えてください

---

## 実装の実行

**重要: 指定されたタスクファイルのタスクのみを実行し、完了したら停止してください。次のタスクに自動的に進んではいけません。**

パラメータとして指定されたプロジェクトルートとタスクファイルに基づいて、以下を実行してください：

### 1. 実装コンテキストをロードして分析してください

**ファイル読み込みの方法:**
- ファイル読み込み操作は、使用しているAIプラットフォームによって異なります
- **Cursor/Cline**: ファイル読み込みツールを使用
- **GitHub Copilot**: ワークスペースコンテキスト参照機能を使用
- **ChatGPT**: ユーザーにファイル内容の貼り付けを依頼
- **その他**: `../../platform_guides/` を参照してください

#### 読み込むべきドキュメント（優先順）

1. **最優先**: `{project_root}/principles/` 配下の憲章ファイルでプロジェクトの開発原則、アーキテクチャ方針、品質基準、組織標準を確認

2. **必須**: 指定されたタスクファイルで完全なタスクリストと実行計画を確認
   - タスクの「参照SPEC」はMarkdownリンク形式で記述されています（クリック可能）
   - リンク先のSPECファイルと指定されたセクションを必ず参照してください

3. **必須**: `{project_root}/specs/baseline/system/architecture_design.md` で以下を確認：
   - 技術スタック（言語、バージョン、フレームワーク、ライブラリ）
   - アーキテクチャパターンとレイヤー構成
   - パッケージ構造と命名規則
   - デザインパターン、トランザクション戦略、並行制御
   - ログ戦略、エラーハンドリング、セキュリティ
   - テスト戦略（テストフレームワーク、カバレッジ目標、テスト方針）
   - **コード生成時は、ここで定義された技術スタックを厳密に遵守すること**

4. **必須**: `{project_root}/specs/baseline/system/requirements.md` で機能要件と成功基準を確認

5. **必須**: `{project_root}/specs/baseline/system/functional_design.md` でシステム全体の機能設計概要を確認

6. **必須**: `{project_root}/specs/baseline/api/*/functional_design.md` でクラス設計、メソッド、エンドポイント仕様を確認

7. **存在する場合**: `{project_root}/specs/baseline/system/data_model.md` でエンティティと関係を確認

8. **存在する場合**: `{project_root}/specs/baseline/api/*/behaviors.md` で受入基準とテストシナリオを確認

9. **存在する場合**: `{project_root}/specs/baseline/system/external_interface.md` で外部連携仕様とAPI仕様を確認

10. **静的リソース**: `{project_root}/resources/` フォルダの静的ファイル（画像等）を確認し、セットアップ時に適切な場所にコピー

**注意**: `{project_root}` は、パラメータで明示的に指定されたプロジェクトルートのパスに置き換えてください。

### 2. タスク構造を解析して抽出してください

- **タスク構成**: セットアップ、共通機能、API別実装、結合・テスト
- **タスク依存関係**: 順次実行対並列実行ルール
- **タスク詳細**: ID、説明、ファイルパス、並列マーカー[P]
- **実行フロー**: 順序と依存関係の要件

### 3. タスク計画に従って実装を実行してください

- **タスクごとの実行**: 次のタスクに進む前に各タスクを完了
- **セットアップタスク**: リソース配置（画像ファイルのコピー等）を最優先で実行
- **依存関係の尊重**: 順次タスクは順番に実行、並列タスク[P]は一緒に実行可能
- **TDDアプローチに従う**: 対応する実装の前にテストを実行（プロジェクトがTDDを採用している場合）
- **ファイルベースの調整**: 同じファイルに影響するタスクは順次実行必須
- **検証チェックポイント**: 進む前に各タスクの完了を検証

### 4. 実装実行ルール

#### プロジェクトルートの使用
全てのパス操作は、パラメータで指定されたプロジェクトルートを基準に行います

#### 技術スタックの遵守
architecture_design.mdに記載された技術スタックを厳密に遵守

- **プログラミング言語**: architecture_design.mdの「1.1 コアプラットフォーム」を確認（例: Java 21, Jakarta EE 10等）
- **フレームワーク**: architecture_design.mdの「1.2 Jakarta EE仕様」を確認（例: JAX-RS 3.1, JPA 3.1等）
- **ライブラリとバージョン**: architecture_design.mdの「1.3 追加ライブラリ」を確認（例: jjwt 0.12.6, SLF4J, Log4j2等）
- **テストフレームワーク**: architecture_design.mdの「テスト戦略」を確認（例: JUnit 5, Mockito, REST Assured等）
- **データベース**: architecture_design.mdの「データベース構成」を確認（例: HSQLDB, コネクションプール設定等）
- **記載されたバージョン番号を正確に使用**: 異なるバージョンを使用しない

#### 憲章の遵守
`{project_root}/principles/` 配下の憲章に記載された開発原則を全ての実装で遵守
- テストカバレッジ基準、アーキテクチャパターン、コーディング規約に従う
- 品質基準、セキュリティ要件、パフォーマンス基準を満たす

#### セットアップ優先
プロジェクト構造、依存関係、構成を初期化
- **静的リソースの配置**: 必要な画像やファイルを適切な場所にコピー
- データベーススキーマのセットアップ

#### コードの前にテスト
契約、エンティティ、結合シナリオのテストを作成（TDDの場合）

#### コア開発
Entity、Dao、Service、Resource（JAX-RSエンドポイント）を実装

#### 結合作業
データベース接続、ミドルウェア、ロギング、外部サービス

#### 仕上げと検証
ユニットテスト、パフォーマンス最適化、ドキュメント

### 5. 単体テスト生成ガイドライン

- **テストフレームワーク**: architecture_design.mdの「テスト戦略」で指定されたフレームワークを使用（例: JUnit 5, Mockito）
- **テストカバレッジ**: architecture_design.mdの「テストカバレッジ」の目標値を遵守（例: サービスレイヤー80%以上）
- api/配下の各APIのbehaviors.mdの各Given-When-Thenシナリオから対応するテストケースを抽出して実装
- api/配下の各APIのfunctional_design.mdの各メソッドシグネチャに対して、正常系・異常系・境界値のテストを作成
- data_model.mdのエンティティ検証ルール（制約条件、バリデーション、一意性制約）をテストで検証
- テストデータはbehaviors.mdやfunctional_design.mdの具体例を参考に作成
- モックやスタブが必要な場合は、architecture_design.mdの「テスト方針」に従う

### 6. API統合テスト生成ガイドライン

- **テストフレームワーク**: REST AssuredまたはJAX-RS Clientを使用してREST APIをテスト
- **テストフレームワークの選定**: architecture_design.mdの「追加ライブラリ」と「テスト戦略」を確認
- api/配下の各APIのfunctional_design.mdのエンドポイント仕様（リクエスト/レスポンス形式）に基づいてテストを作成
- api/配下の各APIのbehaviors.mdのシナリオを実際のHTTPリクエストとしてテスト
- JWT認証が必要なエンドポイントについては、事前にログインしてトークンを取得
- HTTPステータスコード、レスポンスボディ、ヘッダーの検証
- エラーケース（400, 401, 404, 500等）の検証
- テストは`src/test/java/<パッケージ>/api/`配下に配置（パッケージはarchitecture_design.mdの「パッケージ構造」を参照）
- **ビルド時の除外設定**: API統合テストは通常のビルド（`./gradlew test`）では実行されず、個別に実行する設定にする
  - JUnit 5の`@Tag("integration")`アノテーションをAPI統合テストクラスに付与
  - Gradleの`build.gradle`で、testタスクから"integration"タグを除外する設定を追加
  - 統合テスト専用のGradleタスク（例: `integrationTest`）を定義し、個別実行可能にする
  - テストクラス名の命名規則で識別可能にする（例: `*IntegrationTest.java`パターン）

---

## コンポーネント別の参照ドキュメント優先度と使用方法

### 重要: 全てのコンポーネント生成時の共通確認事項

architecture_design.mdの以下を参照すること：
- **言語・バージョン**: 「1.1 コアプラットフォーム」（Java 21等）
- **パッケージ配置**: 「パッケージ編成」
- **命名規則**: 「命名規則」
- **アノテーション**: 「1.2 Jakarta EE仕様」と「状態管理」（@Entity, @Path, @Inject等）
- **ログ出力**: 「ログ戦略」（SLF4J使用、ログレベル、出力方針）

### Entity生成時

- **技術スタック**: architecture_design.mdの「1.2 Jakarta EE仕様」でJakarta Persistence（JPA）バージョンを確認
- **第一参照**: data_model.md
  - テーブル構造、カラム定義、データ型、制約条件（NOT NULL, UNIQUE等）を確認
  - エンティティ間の関係（OneToMany, ManyToOne等）を確認
  - 検証ルール（@NotNull, @Size等のアノテーション）を確認
- **第二参照**: functional_design.md
  - クラス設計、属性名、メソッドシグネチャを確認
  - ビジネスロジックメソッド（calculateTotal等）があれば実装
- **並行制御**: architecture_design.mdの「並行制御」で楽観的ロック（@Version）の使用を確認

### Dao生成時

- **技術スタック**: architecture_design.mdの「1.2 Jakarta EE仕様」でJakarta Persistence（JPA）とCDIバージョンを確認
- **スコープ**: architecture_design.mdの「状態管理」で@ApplicationScopedを使用
- **第一参照**: api/配下の該当APIのfunctional_design.md
  - Daoインターフェース、メソッドシグネチャ、戻り値の型を確認
  - クエリメソッドの動作仕様（検索条件、ソート順、結合条件）を確認
- **第二参照**: data_model.md
  - SQLクエリ設計の参考（テーブル名、カラム名、結合条件）
  - インデックスやパフォーマンス考慮事項を確認

### Service層生成時

- **技術スタック**: architecture_design.mdの「1.2 Jakarta EE仕様」でJakarta CDI, Transactionsバージョンを確認
- **スコープ**: architecture_design.mdの「状態管理」で@ApplicationScopedを使用
- **トランザクション**: architecture_design.mdの「トランザクション管理」で@Transactionalの使用方法を確認
- **第一参照**: api/配下の該当APIのfunctional_design.md
  - Serviceクラスのメソッドシグネチャ、ビジネスロジック、処理フローを確認
  - トランザクション境界、例外ハンドリング、バリデーションロジックを確認
- **第二参照**: api/配下の該当APIのbehaviors.md
  - 各メソッドの振る舞い（Given-When-Then）、業務ルール、制約条件を確認
  - エッジケースや異常系の処理を確認
- **例外処理**: architecture_design.mdの「エラーハンドリング方針」に従う

### Resource（JAX-RSエンドポイント）生成時

- **技術スタック**: architecture_design.mdの「1.2 Jakarta EE仕様」でJAX-RS（Jakarta RESTful Web Services）バージョンを確認
- **スコープ**: architecture_design.mdの「状態管理」で適切なスコープ（通常@RequestScoped）を選択
- **第一参照**: api/配下の該当APIのfunctional_design.md
  - Resourceクラスの設計、エンドポイント仕様（HTTPメソッド、パス、パラメータ）を確認
  - リクエスト/レスポンス形式（JSON）、ステータスコード、エラーハンドリングを確認
  - JWT認証の要否、権限チェックを確認
- **第二参照**: api/配下の該当APIのbehaviors.md
  - 各エンドポイントの振る舞い、エラーケース、バリデーションルールを確認
- **セキュリティ**: architecture_design.mdの「セキュリティ設計」でJWT認証の実装方法を確認

### DTO/Response生成時

- **第一参照**: api/配下の該当APIのfunctional_design.md
  - リクエストDTO、レスポンスDTOの構造、フィールド名、データ型を確認
  - バリデーションアノテーション（@NotNull, @Size等）を確認
- **第二参照**: data_model.md
  - Entityとの対応関係、変換ロジックを確認

### Filter/Interceptor生成時

- **技術スタック**: architecture_design.mdの「1.2 Jakarta EE仕様」でJakarta Servlet, JAX-RS Filtersを確認
- **第一参照**: architecture_design.md
  - JWT認証フィルターの設計、処理フロー、エラーハンドリングを確認
  - CORS設定、ロギングインターセプターの仕様を確認
- **第二参照**: functional_design.md
  - セキュリティ要件、認証・認可の仕様を確認

### 外部連携コンポーネント生成時

- **技術スタック**: architecture_design.mdの「1.3 追加ライブラリ」でJAX-RS Clientの使用を確認
- **第一参照**: external_interface.md
  - API仕様（エンドポイント、HTTPメソッド、リクエスト/レスポンス形式）を確認
  - OpenAPI YAMLファイルがあれば、スキーマ定義、認証方式、エラーレスポンスを確認
  - 通信プロトコル、タイムアウト設定、リトライポリシーを確認
- **第二参照**: functional_design.md
  - 連携クラスの設計、メソッド名、エラーハンドリングを確認

### API統合テスト生成時（REST Assured/JAX-RS Client）

- **第一参照**: api/配下の該当APIのfunctional_design.md
  - エンドポイント仕様（HTTPメソッド、パス、リクエスト/レスポンス形式）を確認
  - 認証要否、ステータスコード、エラーレスポンスを確認
- **第二参照**: api/配下の該当APIのbehaviors.md
  - Given-When-ThenシナリオをHTTPリクエスト/レスポンスのテストに変換
  - エラーケース（在庫不足、認証失敗等）のレスポンスを確認
- **第三参照**: system/のfunctional_design.md
  - API間の連携、データの流れ、セッション（JWT）管理を確認

---

## ドキュメント参照の基本方針

- **architecture_design.md**: 技術スタック、アーキテクチャパターン、品質基準など、プロジェクト全体の技術的指針を提供
- **system/functional_design.md**: システム全体の機能設計概要と各APIへのリンクを提供
- **api/配下のfunctional_design.md**: 各APIの具体的なクラス設計、メソッドシグネチャ、エンドポイント仕様など、実装レベルの詳細を提供
- **api/配下のbehaviors.md**: 各APIの振る舞い仕様（Given-When-Then）、受入基準、テストシナリオを提供
- **各コンポーネントの参照方法**: 上記のセクションを参照

---

## 進捗追跡とエラーハンドリング

- 完了した各タスク後に進捗を報告
- 順次実行タスクが失敗した場合は実行を停止
- 並列実行タスク[P]の場合、成功したタスクを続行し、失敗したタスクを報告
- デバッグのためのコンテキスト付きの明確なエラーメッセージを提供
- 実装を続行できない場合は次の手順を提案
- **重要**: 完了したタスクについては、タスクファイルでタスクを[X]としてマーク

**タスク更新の方法:**
- ファイル編集操作は、使用しているAIプラットフォームによって異なります
- 詳細は `../../platform_guides/` を参照してください

---

## 完了検証

- 憲章の原則と品質基準が遵守されていることを確認
- 全ての必須タスクが完了していることを確認
- 実装された機能が要件定義と一致することを確認
- テストがパスし、カバレッジが要件を満たすことを検証
- 実装がアーキテクチャ設計に従っていることを確認
- クラス設計が機能設計仕様と一致することを検証
- **仕様書とのトレーサビリティ検証**:
  - api/配下の各APIのbehaviors.mdの受入基準（Given-When-Then）が全てテストケースでカバーされていることを確認
  - api/配下の各APIのfunctional_design.mdで定義された全てのエンドポイント、DTO、クラス、メソッドが実装されていることを確認
  - data_model.mdで定義された全ての制約条件（NOT NULL, UNIQUE, FK等）が実装されていることを確認
  - external_interface.mdで定義された全てのAPI仕様が実装されていることを確認
- 静的リソースが正しく配置されていることを確認
- 完了した作業の要約とともに最終ステータスを報告
- **このタスクファイルのタスクがすべて完了したら、ここで停止する**

---

## マイクロサービスパターン特有の実装要件

### back-office-apiのアーキテクチャ特性

**マイクロサービスとしての役割:**
- 書籍・在庫・カテゴリ・出版社の完全なデータ管理サービス
- BFF（berry-books-api）からREST APIとして呼ばれる
- 独立したデータベースを管理
- CORS対応でクロスオリジンリクエストを受け入れ

### 全エンティティを実装

**実装するエンティティ（すべて）**:
- ✅ Book（書籍）
- ✅ Stock（在庫）- **@Versionアノテーション必須**
- ✅ Category（カテゴリ）
- ✅ Publisher（出版社）

**実装するDAO（すべて）**:
- ✅ BookDao（JPQL検索）
- ✅ BookDaoCriteria（Criteria API検索）- **両方実装**
- ✅ StockDao（楽観的ロック対応）
- ✅ CategoryDao
- ✅ PublisherDao

**実装するService（すべて）**:
- ✅ BookService（2種類の検索メソッド）
- ✅ StockService（楽観的ロック処理）
- ✅ CategoryService
- ✅ PublisherService

**実装するResource（すべて）**:
- ✅ BookResource（2種類の検索エンドポイント）
- ✅ StockResource（楽観的ロック対応）
- ✅ CategoryResource
- ✅ PublisherResource

### 楽観的ロック（Optimistic Locking）の実装

**Stock EntityにVarious** 在庫管理では、複数のトランザクションが同時に在庫を更新する可能性があります。楽観的ロックで競合を検出し、データの整合性を保ちます。

**実装方法:**

#### 1. Stock Entityに@Versionアノテーション

```java
@Entity
@Table(name = "STOCK")
public class Stock {
    @Id
    @Column(name = "BOOK_ID")
    private Integer bookId;
    
    @Column(name = "QUANTITY")
    private Integer quantity;
    
    @Version  // 楽観的ロック
    @Column(name = "VERSION")
    private int version;
    
    // getters, setters
}
```

#### 2. OptimisticLockExceptionMapper

`OptimisticLockException`が発生したら、HTTP 409 Conflictを返します：

```java
@Provider
public class OptimisticLockExceptionMapper 
    implements ExceptionMapper<OptimisticLockException> {
    
    @Override
    public Response toResponse(OptimisticLockException exception) {
        ErrorResponse error = new ErrorResponse(
            "Stock has been modified by another transaction. Please retry."
        );
        return Response.status(Response.Status.CONFLICT).entity(error).build();
    }
}
```

#### 3. StockService

楽観的ロックを考慮した在庫更新処理：

```java
@ApplicationScoped
public class StockService {
    @Inject
    private StockDao stockDao;
    
    @Transactional
    public void updateStock(Integer bookId, Integer quantity, int version) 
        throws OptimisticLockException {
        Stock stock = stockDao.findById(bookId);
        stock.setQuantity(quantity);
        // versionチェックはJPAが自動的に行う
        stockDao.update(stock);
    }
}
```

#### 4. StockResource

```java
@Path("/stocks")
@ApplicationScoped
public class StockResource {
    @Inject
    private StockService stockService;
    
    @PUT
    @Path("/{bookId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateStock(
        @PathParam("bookId") Integer bookId,
        StockUpdateRequest request
    ) {
        try {
            stockService.updateStock(bookId, request.getQuantity(), request.getVersion());
            return Response.ok().build();
        } catch (OptimisticLockException e) {
            // ExceptionMapperが自動的にHTTP 409に変換
            throw e;
        }
    }
}
```

**重要**: BFFがHTTP 409を受け取ったら、在庫を再取得して更新を再試行します。

### 2種類の書籍検索実装

**back-office-apiでは、同じ検索機能を2つの方法で実装します:**

#### 1. JPQL検索（BookDao）

JPQLを使った動的検索：

```java
@ApplicationScoped
public class BookDao {
    @Inject
    private EntityManager em;
    
    public List<Book> searchBooks(String keyword, Integer categoryId, Integer publisherId) {
        StringBuilder jpql = new StringBuilder(
            "SELECT b FROM Book b " +
            "LEFT JOIN FETCH b.category " +
            "LEFT JOIN FETCH b.publisher " +
            "WHERE 1=1"
        );
        
        if (keyword != null) {
            jpql.append(" AND b.title LIKE :keyword");
        }
        // ... 動的条件追加
        
        TypedQuery<Book> query = em.createQuery(jpql.toString(), Book.class);
        // ... パラメータ設定
        
        return query.getResultList();
    }
}
```

**エンドポイント**: `GET /books/search/jpql`

#### 2. Criteria API検索（BookDaoCriteria）

型安全なCriteria APIを使った検索：

```java
@ApplicationScoped
public class BookDaoCriteria {
    @Inject
    private EntityManager em;
    
    public List<Book> searchBooks(String keyword, Integer categoryId, Integer publisherId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Book> cq = cb.createQuery(Book.class);
        Root<Book> book = cq.from(Book.class);
        
        book.fetch("category", JoinType.LEFT);
        book.fetch("publisher", JoinType.LEFT);
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (keyword != null) {
            predicates.add(cb.like(book.get("title"), "%" + keyword + "%"));
        }
        // ... 動的条件追加
        
        cq.where(predicates.toArray(new Predicate[0]));
        
        return em.createQuery(cq).getResultList();
    }
}
```

**エンドポイント**: `GET /books/search/criteria`

#### 3. BookService

両方のDaoを使い分け：

```java
@ApplicationScoped
public class BookService {
    @Inject
    private BookDao bookDao;
    
    @Inject
    private BookDaoCriteria bookDaoCriteria;
    
    public List<Book> searchBooksJpql(String keyword, Integer categoryId, Integer publisherId) {
        return bookDao.searchBooks(keyword, categoryId, publisherId);
    }
    
    public List<Book> searchBooksCriteria(String keyword, Integer categoryId, Integer publisherId) {
        return bookDaoCriteria.searchBooks(keyword, categoryId, publisherId);
    }
}
```

#### 4. BookResource

2つの検索エンドポイントを公開：

```java
@Path("/books")
@ApplicationScoped
public class BookResource {
    @Inject
    private BookService bookService;
    
    @GET
    @Path("/search/jpql")
    public Response searchBooksJpql(
        @QueryParam("keyword") String keyword,
        @QueryParam("categoryId") Integer categoryId,
        @QueryParam("publisherId") Integer publisherId
    ) {
        List<Book> books = bookService.searchBooksJpql(keyword, categoryId, publisherId);
        return Response.ok(books).build();
    }
    
    @GET
    @Path("/search/criteria")
    public Response searchBooksCriteria(
        @QueryParam("keyword") String keyword,
        @QueryParam("categoryId") Integer categoryId,
        @QueryParam("publisherId") Integer publisherId
    ) {
        List<Book> books = bookService.searchBooksCriteria(keyword, categoryId, publisherId);
        return Response.ok(books).build();
    }
}
```

**両方の実装は同じ結果を返します。学習・比較用に両方実装します。**

### CORS設定

BFFからのクロスオリジンリクエストを受け入れるため、CORSフィルターを実装：

```java
@Provider
public class CorsFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext,
                      ContainerResponseContext responseContext) {
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", 
            "GET, POST, PUT, DELETE, OPTIONS");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", 
            "Content-Type, Authorization");
    }
}
```

### マイクロサービスとしてのテスト戦略

#### 1. 単体テスト
- ServiceクラスのビジネスロジックをテOST
- Daoをモック

#### 2. 並行処理テスト（重要！）
- 在庫更新の競合シナリオをテスト
- 2つのトランザクションが同時に更新を試みる
- 1つは成功、もう1つはOptimisticLockExceptionを検証

#### 3. 検索実装比較テスト
- JPQL検索とCriteria API検索が同じ結果を返すことを検証

#### 4. 統合テスト
- REST API経由でテスト
- HTTP 409 Conflictが正しく返されることを確認

---

## 重要な注意事項

### タスクの実行範囲
- このインストラクションは、タスクファイルに完全なタスク分解が存在することを前提としています
- タスクが不完全または欠落している場合は、まず `task_generation.md` インストラクションを使用してタスクリストを生成してください
- **指定されたタスクファイルのタスクのみを実行してください。他のタスクファイル（例: 次の機能のタスク）に自動的に進んではいけません。**
- タスクは分業の単位です。1つのタスクが完了したら、次のタスクに進む前にユーザーの確認を待ってください。

### REST API特有の注意点
- 画面（UI）は含まれないため、View/XHTMLの実装は行わない
- エンドポイントのテストにはREST AssuredまたはJAX-RS Clientを使用
- JWT認証、CORS、HTTPステータスコードの適切な使用を考慮
- リクエスト/レスポンスのJSON形式のバリデーションを実装

### プロジェクトルートの扱い
- `{project_root}` は、パラメータで明示的に指定されたパスに置き換えてください
- 相対パスでも絶対パスでも構いません
- 全てのファイル操作は、このプロジェクトルートを基準に行います

---

## プラットフォーム別ガイド

このインストラクションの実行方法は、AIプラットフォームによって異なります。
詳細は以下を参照してください：

- **Cursor/Cline**: `../../platform_guides/cursor_cline.md`
- **GitHub Copilot**: `../../platform_guides/github_copilot.md`
- **その他**: `../../platform_guides/other_platforms.md`

