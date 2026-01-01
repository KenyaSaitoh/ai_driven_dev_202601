# アーキテクチャ設計書

## 1. システム概要

### 1.1 システム名
Books Stock API - バックオフィス書籍在庫管理システム

### 1.2 アーキテクチャスタイル
- **アーキテクチャパターン**: レイヤードアーキテクチャ（階層型アーキテクチャ）
- **API設計**: RESTful API
- **データアクセス**: JPA（Jakarta Persistence API）
- **認証方式**: JWT（JSON Web Token）+ Cookie認証

## 2. 全体アーキテクチャ

### 2.1 システム構成図

```
┌─────────────────────────────────────────────────────────┐
│                   Client Application                     │
│              (Browser / Mobile App / etc.)              │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP/HTTPS (REST API)
                     │ JSON形式
┌────────────────────▼────────────────────────────────────┐
│            Application Server (Payara)                   │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Presentation Layer (API Resources)             │   │
│  │  - AuthenResource                               │   │
│  │  - BookResource                                 │   │
│  │  - CategoryResource                             │   │
│  │  - PublisherResource                            │   │
│  │  - StockResource                                │   │
│  │  - WorkflowResource                             │   │
│  └────────────┬────────────────────────────────────┘   │
│               │                                          │
│  ┌────────────▼────────────────────────────────────┐   │
│  │  Business Logic Layer (Services)                │   │
│  │  - BookService                                  │   │
│  │  - CategoryService                              │   │
│  │  - PublisherService                             │   │
│  │  - WorkflowService                              │   │
│  └────────────┬────────────────────────────────────┘   │
│               │                                          │
│  ┌────────────▼────────────────────────────────────┐   │
│  │  Data Access Layer (DAO)                        │   │
│  │  - BookDao                                      │   │
│  │  - CategoryDao                                  │   │
│  │  - PublisherDao                                 │   │
│  │  - StockDao                                     │   │
│  │  - WorkflowDao                                  │   │
│  │  - EmployeeDao                                  │   │
│  │  - DepartmentDao                                │   │
│  └────────────┬────────────────────────────────────┘   │
│               │ JPA                                      │
│  ┌────────────▼────────────────────────────────────┐   │
│  │  Persistence Layer (JPA Entities)               │   │
│  │  - Book, Category, Publisher, Stock             │   │
│  │  - Employee, Department, Workflow               │   │
│  └────────────┬────────────────────────────────────┘   │
│               │                                          │
│  ┌────────────▼────────────────────────────────────┐   │
│  │  Cross-Cutting Concerns                         │   │
│  │  - JwtUtil (Security)                           │   │
│  │  - Exception Mappers                            │   │
│  │  - MessageUtil                                  │   │
│  └─────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────┘
                     │ JDBC
┌────────────────────▼────────────────────────────────────┐
│                  Database (HSQLDB)                       │
│  - BOOK, CATEGORY, PUBLISHER, STOCK                     │
│  - EMPLOYEE, DEPARTMENT, WORKFLOW                       │
└─────────────────────────────────────────────────────────┘
```

## 3. レイヤー設計

### 3.1 Presentation Layer（プレゼンテーション層）

**責務**: クライアントからのHTTPリクエストを受け取り、レスポンスを返す

**主要コンポーネント**:
- `ApplicationConfig`: JAX-RSアプリケーション設定（ベースパス: `/api`）
- `AuthenResource`: 認証API（`/api/auth`）
- `BookResource`: 書籍API（`/api/books`）
- `CategoryResource`: カテゴリAPI（`/api/categories`）
- `PublisherResource`: 出版社API（`/api/publishers`）
- `StockResource`: 在庫API（`/api/stocks`）
- `WorkflowResource`: ワークフローAPI（`/api/workflows`）

**設計方針**:
- JAX-RS（`@Path`, `@GET`, `@POST`, `@PUT`, `@DELETE`）を使用
- DTOオブジェクトでリクエスト/レスポンスをマッピング
- ビジネスロジックはServiceレイヤーに委譲
- 例外処理はException Mapperで統一的に処理

### 3.2 Business Logic Layer（ビジネスロジック層）

**責務**: ビジネスルール、業務ロジックの実装

**主要コンポーネント**:
- `BookService`: 書籍検索ロジック
- `CategoryService`: カテゴリ管理ロジック
- `PublisherService`: 出版社管理ロジック
- `WorkflowService`: ワークフロー管理ロジック（状態遷移、承認権限チェック、書籍マスタ反映）

**設計方針**:
- `@ApplicationScoped`でCDI管理
- `@Transactional`でトランザクション境界を制御
- ビジネスルールの検証と実行
- DAOレイヤーを使用してデータアクセス

### 3.3 Data Access Layer（データアクセス層）

**責務**: データベースへのCRUD操作

**主要コンポーネント**:
- `BookDao`: 書籍データアクセス
- `CategoryDao`: カテゴリデータアクセス
- `PublisherDao`: 出版社データアクセス
- `StockDao`: 在庫データアクセス
- `WorkflowDao`: ワークフローデータアクセス
- `EmployeeDao`: 社員データアクセス
- `DepartmentDao`: 部署データアクセス

**設計方針**:
- JPA（`EntityManager`）を使用
- JPQL、Named Query、Criteria APIによるクエリ実行
- エンティティとDTOの変換

### 3.4 Persistence Layer（永続化層）

**責務**: データベーステーブルとJavaオブジェクトのマッピング

**主要エンティティ**:
- `Book`: 書籍情報（BOOK + STOCK結合）
- `Category`: カテゴリ情報
- `Publisher`: 出版社情報
- `Stock`: 在庫情報（楽観的ロック対応）
- `Employee`: 社員情報
- `Department`: 部署情報
- `Workflow`: ワークフロー履歴情報

**設計方針**:
- JPA `@Entity`アノテーションによるマッピング
- `@ManyToOne`, `@OneToMany`によるリレーションシップ
- `@SecondaryTable`による複数テーブルマッピング（Bookエンティティ）
- `@Version`による楽観的ロック

### 3.5 Cross-Cutting Concerns（横断的関心事）

**セキュリティ**:
- `JwtUtil`: JWT生成・検証
- パスワード認証: BCryptハッシュ

**例外処理**:
- `GenericExceptionMapper`: 一般的な例外ハンドリング
- `OptimisticLockExceptionMapper`: 楽観的ロック例外ハンドリング
- `WorkflowExceptionMapper`: ワークフロー例外ハンドリング
- `NotFoundExceptionMapper`: 404エラーハンドリング

**メッセージ管理**:
- `MessageUtil`: プロパティファイルからのメッセージ取得
- `messages.properties`: 日本語メッセージ
- `ValidationMessages_ja.properties`: バリデーションメッセージ

## 4. 技術スタック

### 4.1 フレームワーク・ライブラリ

| 技術 | バージョン | 用途 |
|------|-----------|------|
| Jakarta EE | 10 | エンタープライズJavaプラットフォーム |
| JAX-RS | 3.1 | RESTful Webサービス |
| JPA (Jakarta Persistence) | 3.1 | ORM（Object-Relational Mapping） |
| CDI (Context and Dependency Injection) | 4.0 | 依存性注入 |
| Bean Validation | 3.0 | バリデーション |
| MicroProfile Config | 3.0 | 設定管理 |
| SLF4J | 2.x | ロギングファサード |
| BCrypt | 0.10.x | パスワードハッシュ化 |
| JJWT | 0.12.x | JWT生成・検証 |

### 4.2 実行環境

| コンポーネント | 製品/技術 |
|---------------|----------|
| Application Server | Payara Server 6.x |
| Database | HSQLDB 2.x |
| JDK | Java 17+ |

### 4.3 設定ファイル

| ファイル | 用途 |
|---------|------|
| `persistence.xml` | JPA設定（データソース、プロパティ） |
| `microprofile-config.properties` | MicroProfile設定（JWT設定） |
| `beans.xml` | CDI設定 |
| `web.xml` | Webアプリケーション設定 |

## 5. データフロー

### 5.1 典型的なリクエストフロー（書籍一覧取得の例）

```
1. Client
    ↓ GET /api/books
2. BookResource#getAllBooks()
    ↓ @Inject BookService
3. BookService#getBooksAll()
    ↓ @Inject BookDao
4. BookDao#findAll()
    ↓ EntityManager (JPQL)
5. Database Query: SELECT * FROM BOOK ...
    ↓ Result Set
6. Book Entities
    ↓ DAO → Service
7. BookTO (Data Transfer Object)
    ↓ Service → Resource
8. JSON Response
    ↓ HTTP Response
9. Client
```

### 5.2 ワークフロー承認フロー

```
1. Client
    ↓ POST /api/workflows/{workflowId}/approve
2. WorkflowResource#approveWorkflow()
    ↓
3. WorkflowService#approveWorkflow()
    ├─ (3-1) 最新ワークフロー状態取得
    ├─ (3-2) 状態チェック（APPLIED?）
    ├─ (3-3) 承認権限チェック
    │   ├─ 職務ランクチェック（MANAGER以上?）
    │   └─ 部署チェック（DIRECTOR or 同一部署?）
    ├─ (3-4) 新規操作履歴作成（STATE=APPROVED）
    ├─ (3-5) ワークフローDAO経由でDB保存
    └─ (3-6) 書籍マスタ反映処理
        ├─ ADD_NEW_BOOK → Book INSERT
        ├─ REMOVE_BOOK → Book論理削除
        └─ ADJUST_BOOK_PRICE → Book価格更新
    ↓
4. WorkflowTO Response
    ↓
5. Client
```

## 6. セキュリティアーキテクチャ

### 6.1 認証フロー

```
1. Client
    ↓ POST /api/auth/login
    ↓ { employeeCode, password }
2. AuthenResource#login()
    ↓
3. EmployeeDao#findByCode(employeeCode)
    ↓
4. パスワード照合（BCrypt.checkpw()）
    ↓ 成功
5. JwtUtil#generateToken()
    ├─ employeeId, employeeCode, departmentId
    ├─ 秘密鍵で署名（HMAC-SHA256）
    └─ 有効期限: 24時間
    ↓
6. HttpOnly Cookie設定
    ├─ Name: back-office-jwt
    ├─ Value: JWT文字列
    ├─ HttpOnly: true
    ├─ Secure: false (開発環境)
    └─ MaxAge: 86400秒
    ↓
7. Response + Set-Cookie Header
    ↓
8. Client (Cookie保存)
```

### 6.2 JWT構造

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "123",  // employeeId
    "employeeCode": "E0001",
    "departmentId": 1,
    "iat": 1234567890,  // 発行日時
    "exp": 1234654290   // 有効期限
  },
  "signature": "..."
}
```

### 6.3 権限制御

| 職務ランク | 値 | 権限 |
|-----------|---|------|
| ASSOCIATE | 1 | 書籍参照、ワークフロー作成 |
| MANAGER | 2 | ASSOCIATE権限 + 同一部署のワークフロー承認 |
| DIRECTOR | 3 | MANAGER権限 + 全部署のワークフロー承認 |

## 7. トランザクション設計

### 7.1 トランザクション境界
- Serviceレイヤーのメソッドを`@Transactional`でマーク
- デフォルト: `@Transactional(TxType.REQUIRED)`
- JTAトランザクションマネージャーによる管理

### 7.2 楽観的ロック
- `Stock`エンティティに`@Version`フィールド
- 更新時にバージョンチェック
- 競合発生時は`OptimisticLockException`をスロー

### 7.3 ワークフロートランザクション
- ワークフロー承認時は以下を1トランザクションで実行:
  1. 操作履歴の追加（WORKFLOW INSERT）
  2. 書籍マスタへの反映（BOOK UPDATE/INSERT/DELETE）
- ロールバック時は両方とも取り消される

## 8. エラーハンドリング

### 8.1 例外マッピング

| Exception | HTTP Status | Mapper |
|-----------|-------------|--------|
| `IllegalArgumentException` | 400 Bad Request | GenericExceptionMapper |
| `NotFoundException` | 404 Not Found | NotFoundExceptionMapper |
| `OptimisticLockException` | 409 Conflict | OptimisticLockExceptionMapper |
| `WorkflowNotFoundException` | 404 Not Found | WorkflowExceptionMapper |
| `InvalidWorkflowStateException` | 400 Bad Request | WorkflowExceptionMapper |
| `UnauthorizedApprovalException` | 403 Forbidden | WorkflowExceptionMapper |
| 一般的な例外 | 500 Internal Server Error | GenericExceptionMapper |

### 8.2 エラーレスポンス形式

```json
{
  "error": "エラータイプ",
  "message": "エラーメッセージ（日本語）"
}
```

## 9. ログ設計

### 9.1 ログレベル
- **INFO**: API呼び出しログ、重要な処理の開始/終了
- **WARN**: 楽観的ロック失敗、認証失敗など
- **ERROR**: 予期しないエラー、システムエラー
- **DEBUG**: 詳細なデバッグ情報

### 9.2 ログ形式
```
[クラス名#メソッド名] ログメッセージ: パラメータ=値, ...
```

例:
```
[ AuthenResource#login ] employeeCode: E0001
[ WorkflowService#approveWorkflow ] workflowId=123
```

## 10. パフォーマンス考慮事項

### 10.1 データベースアクセス
- N+1問題の回避: JOINによる一括取得、JPQL `JOIN FETCH`の活用
- Bookエンティティ: `@SecondaryTable`でBOOK + STOCK結合
- インデックス: 主キー、外部キー、検索条件フィールド

### 10.2 キャッシング
現状はキャッシング未実装。将来的な実装候補:
- JPAセカンドレベルキャッシュ（Category, Publisherなどの参照マスタ）
- アプリケーションレベルキャッシュ（メモリキャッシュ）

### 10.3 接続プール
- アプリケーションサーバーの接続プール機能を使用
- JNDI名: `jdbc/HsqldbDS`

## 11. デプロイメント構成

### 11.1 デプロイメント形式
- WARファイル形式
- Payara Serverにデプロイ

### 11.2 設定の外部化
- `microprofile-config.properties`: デフォルト設定
- 環境変数: 本番環境での設定上書き（JWT秘密鍵など）
- システムプロパティ: 起動時のオプション設定

## 12. 拡張性・保守性

### 12.1 レイヤーの分離
- 各レイヤーは疎結合
- インターフェース（暗黙的にServiceとDAOで分離）
- DTOによるAPI仕様とエンティティの分離

### 12.2 依存性注入
- CDIによる依存性注入
- テスト時のモック化が容易

### 12.3 設定の一元管理
- MicroProfile Configによる設定管理
- プロパティファイルで設定を外部化

## 13. 今後の拡張予定

### 13.1 認証フィルタの実装
- JWT認証フィルタ（`@PreMatching`）
- 現在のログインユーザー情報取得エンドポイントの有効化

### 13.2 監査ログ
- すべてのAPI呼び出しの監査ログ
- ワークフロー操作ログの強化

### 13.3 通知機能
- ワークフロー申請時のメール通知
- ワークフロー承認/却下時の通知

## 14. 参考資料

- Jakarta EE 10 Specification: https://jakarta.ee/specifications/platform/10/
- JAX-RS 3.1 Specification: https://jakarta.ee/specifications/restful-ws/3.1/
- JPA 3.1 Specification: https://jakarta.ee/specifications/persistence/3.1/
- MicroProfile Config: https://microprofile.io/specifications/microprofile-config/
