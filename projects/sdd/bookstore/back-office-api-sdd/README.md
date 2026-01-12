# back-office-api-sdd プロジェクト

## 📖 概要

Jakarta EE 10とJAX-RS (Jakarta RESTful Web Services) 3.1を使用したオンライン書店「**Berry Books**」のバックオフィスAPIアプリケーションです。
書籍・在庫・カテゴリ・出版社の完全なデータ管理をREST APIとして提供するマイクロサービスです。

> **Note:** このプロジェクトは**仕様駆動開発（SDD: Specification-Driven Development）**の研修用プロジェクトです。

> **SDDとは:**
> - 詳細な仕様書（specs/）に基づいて、段階的にコードを生成する手法
> - AIを活用して、仕様書からタスクリスト（tasks/）を生成し、タスクに従って実装を進める
> - 憲章（principles/）に定められた設計原則とベストプラクティスに従う
> - **汎用Agent Skills** (`agent_skills/jakarta-ee-standard/`) を使用した開発

## 🤖 Agent Skillsを使った開発

このプロジェクトは、汎用的な **Jakarta EE マイクロサービス開発 Agent Skills** を使用して開発します。

開発は以下の**3段階プロセス**で進めます：

```
ステップ1: タスク分解（仕様書 → タスクリスト）
    ↓
ステップ2: 詳細設計（仕様書 → 詳細設計書）← AIと対話しながら
    ↓
ステップ3: コード生成（詳細設計書 → 実装コード）
```

---

### 📋 開発フロー

#### ステップ1: タスク分解（プロジェクト開始時に1回）

仕様書から実装タスクリストを分解・生成します。

```
@agent_skills/jakarta-ee-standard/instructions/task_breakdown.md

全タスクを分解してください。

パラメータ:
* project_root: projects/sdd/bookstore/back-office-api-sdd
* spec_directory: projects/sdd/bookstore/back-office-api-sdd/specs
```

* 生成されるファイル: `tasks/*.md`（タスクリスト）

---

#### ステップ2: 詳細設計（各APIごとに実施）

各APIの詳細設計書を**AIと対話しながら**作成します。

* 実行順序: `tasks/tasks.md`の「実行順序」セクションを参照してください。

* 対話の流れ:
  1. AIが仕様書を読み込み、理解した内容を説明します
  2. AIが不明点を質問します
  3. あなたが回答します
  4. `specs/baseline/api/API_XXX_*/detailed_design.md` が生成されます

---

* 全APIの詳細設計コマンド（コピペ用）:

##### API_001_auth（認証API）

```
@agent_skills/jakarta-ee-standard/instructions/detailed_design.md
@projects/sdd/bookstore/back-office-api-sdd/specs

対象: API_001_auth

認証APIの詳細設計書を作成してください。
```

##### API_002_books（書籍API - JPQL + Criteria API）

```
@agent_skills/jakarta-ee-standard/instructions/detailed_design.md
@projects/sdd/bookstore/back-office-api-sdd/specs

対象: API_002_books

書籍APIの詳細設計書を作成してください。
JPQL検索とCriteria API検索の両方を実装する予定です。
```

##### API_003_categories（カテゴリAPI）

```
@agent_skills/jakarta-ee-standard/instructions/detailed_design.md
@projects/sdd/bookstore/back-office-api-sdd/specs

対象: API_003_categories

カテゴリAPIの詳細設計書を作成してください。
```

##### API_004_publishers（出版社API）

```
@agent_skills/jakarta-ee-standard/instructions/detailed_design.md
@projects/sdd/bookstore/back-office-api-sdd/specs

対象: API_004_publishers

出版社APIの詳細設計書を作成してください。
```

##### API_005_stocks（在庫API - 楽観的ロック）

```
@agent_skills/jakarta-ee-standard/instructions/detailed_design.md
@projects/sdd/bookstore/back-office-api-sdd/specs

対象: API_005_stocks

在庫APIの詳細設計書を作成してください。
楽観的ロック（@Version）を使用した在庫更新を実装する予定です。
```

##### API_006_workflows（ワークフローAPI）

```
@agent_skills/jakarta-ee-standard/instructions/detailed_design.md
@projects/sdd/bookstore/back-office-api-sdd/specs

対象: API_006_workflows

ワークフローAPIの詳細設計書を作成してください。
```

* 重要: 詳細設計は**対話的なプロセス**です。AIが質問してきたら、必ず回答してください。

---

#### ステップ3: コード生成（詳細設計完了後）

詳細設計書をもとに、実装コードを生成します。

* 実行順序: 
1. **セットアップタスク** → 2. **共通機能タスク** → 3. **各API実装**

> **重要**: 共通機能タスク（エンティティ、DAO、DTO、ユーティリティ等）を先に実装してから、各API実装に進んでください。

##### 3-1. セットアップタスク（最初に1回）

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md

セットアップタスクを実行してください。

パラメータ:
* project_root: projects/sdd/bookstore/back-office-api-sdd
* task_file: projects/sdd/bookstore/back-office-api-sdd/tasks/setup_tasks.md
* skip_infrastructure: true
```

##### 3-2. 共通機能タスク（セットアップ後に1回）

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md

共通機能タスクを実行してください。

パラメータ:
* project_root: projects/sdd/bookstore/back-office-api-sdd
* task_file: projects/sdd/bookstore/back-office-api-sdd/tasks/common_tasks.md
```

* 実装される共通機能:
  * 全エンティティ（Book, Stock, Category, Publisher, Employee, Department, Workflow）
  * 全DAO（JPQLとCriteria API対応）
  * 共通DTO・例外クラス
  * セキュリティ基盤（JWT、BCrypt）
  * ユーティリティクラス

##### 3-3. 各APIの実装（共通機能完了後にコピペ用）

詳細設計書を参照しながら、各APIを実装します。

* API_001_auth:

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@projects/sdd/bookstore/back-office-api-sdd/specs/baseline/api/API_001_auth/detailed_design.md

認証APIを実装してください。

パラメータ:
* project_root: projects/sdd/bookstore/back-office-api-sdd
* task_file: projects/sdd/bookstore/back-office-api-sdd/tasks/API_001_auth.md
```

* API_002_books:

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@projects/sdd/bookstore/back-office-api-sdd/specs/baseline/api/API_002_books/detailed_design.md

書籍APIを実装してください。

パラメータ:
* project_root: projects/sdd/bookstore/back-office-api-sdd
* task_file: projects/sdd/bookstore/back-office-api-sdd/tasks/API_002_books.md
```

* API_003_categories:

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@projects/sdd/bookstore/back-office-api-sdd/specs/baseline/api/API_003_categories/detailed_design.md

カテゴリAPIを実装してください。

パラメータ:
* project_root: projects/sdd/bookstore/back-office-api-sdd
* task_file: projects/sdd/bookstore/back-office-api-sdd/tasks/API_003_categories.md
```

* API_004_publishers:

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@projects/sdd/bookstore/back-office-api-sdd/specs/baseline/api/API_004_publishers/detailed_design.md

出版社APIを実装してください。

パラメータ:
* project_root: projects/sdd/bookstore/back-office-api-sdd
* task_file: projects/sdd/bookstore/back-office-api-sdd/tasks/API_004_publishers.md
```

* API_005_stocks:

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@projects/sdd/bookstore/back-office-api-sdd/specs/baseline/api/API_005_stocks/detailed_design.md

在庫APIを実装してください（楽観的ロック対応）。

パラメータ:
* project_root: projects/sdd/bookstore/back-office-api-sdd
* task_file: projects/sdd/bookstore/back-office-api-sdd/tasks/API_005_stocks.md
```

* API_006_workflows:

```
@agent_skills/jakarta-ee-standard/instructions/code_generation.md
@projects/sdd/bookstore/back-office-api-sdd/specs/baseline/api/API_006_workflows/detailed_design.md

ワークフローAPIを実装してください。

パラメータ:
* project_root: projects/sdd/bookstore/back-office-api-sdd
* task_file: projects/sdd/bookstore/back-office-api-sdd/tasks/API_006_workflows.md
```

---

### 📚 詳細情報

詳細は `@agent_skills/jakarta-ee-standard/README.md` を参照してください。

## 🎯 プロジェクトの特徴（マイクロサービスパターン）

### アーキテクチャ
* **独立したデータ管理サービス**: 書籍・在庫・カテゴリ・出版社の完全管理
* **マイクロサービス**: BFF（berry-books-api）から呼ばれるバックエンドサービス
* **REST API**: データ管理機能をREST APIとして提供
* **CORS対応**: クロスオリジンリクエストに対応

### 実装する全エンティティ
* ✅ **Book**（書籍）
* ✅ **Stock**（在庫）- **楽観的ロック必須（@Version）**
* ✅ **Category**（カテゴリ）
* ✅ **Publisher**（出版社）

### 重要な実装要件

#### 楽観的ロック（Optimistic Locking）
* Stockエンティティに`@Version`アノテーション使用
* 在庫更新時の競合を検出
* `OptimisticLockException` → HTTP 409 Conflict

#### 2種類の書籍検索実装
* **JPQL検索**（`BookDao`）: 動的クエリ、シンプル
* **Criteria API検索**（`BookDaoCriteria`）: 型安全、コンパイル時チェック
* **両方実装**: 比較学習が可能

#### CORS設定
* BFF（berry-books-api）からのクロスオリジンリクエスト対応
* `CorsFilter`実装

## 🔧 使用している技術

### 本番環境

* **Jakarta EE 10**
* **Payara Server 6**
* **JAX-RS (Jakarta RESTful Web Services) 3.1** - REST API
* **Jakarta Persistence (JPA) 3.1** - Hibernate実装
* **Jakarta Transactions (JTA)**
* **Jakarta CDI 4.0**
* **Jakarta Bean Validation 3.0**
* **HSQLDB 2.7.x**

### テスト環境

* **JUnit 5** - テストフレームワーク
* **Mockito** - モックライブラリ
* **JaCoCo** - カバレッジツール（オプション）

## プロジェクト構成

```
back-office-api-sdd/
├── specs/                          # 仕様書（SDD）
│   ├── baseline/
│   │   ├── system/
│   │   │   ├── requirements.md
│   │   │   ├── architecture_design.md
│   │   │   ├── functional_design.md
│   │   │   ├── data_model.md
│   │   │   └── behaviors.md
│   │   └── api/
│   │       ├── API_001_books/
│   │       ├── API_002_stocks/
│   │       └── API_003_categories/
│   └── enhancements/               # 機能拡張仕様
├── principles/                     # 開発憲章
│   └── constitution.md
├── tasks/                          # タスクリスト（AI生成）
│   ├── tasks.md
│   ├── setup_tasks.md
│   ├── common_tasks.md
│   ├── API_001_books.md
│   ├── API_002_stocks.md
│   └── integration_tasks.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── pro/kensait/backoffice/
│   │   │       ├── api/              # JAX-RS Resources
│   │   │       │   ├── dto/          # API DTOs (Records)
│   │   │       │   └── exception/    # Exception Mappers
│   │   │       ├── service/          # Business Logic
│   │   │       ├── dao/              # Data Access Objects
│   │   │       ├── entity/           # JPA Entities (Book, Stock, Category, Publisher)
│   │   │       ├── util/             # Utilities
│   │   │       └── common/           # Common Classes
│   │   ├── resources/
│   │   │   ├── META-INF/
│   │   │   │   └── persistence.xml
│   │   │   ├── db/
│   │   │   │   ├── schema.sql
│   │   │   │   └── sample_data.sql
│   │   │   └── log4j2.xml
│   │   └── webapp/
│   │       └── WEB-INF/
│   │           └── web.xml
│   └── test/
│       └── java/
│           └── pro/kensait/backoffice/
├── build.gradle
└── README.md
```

## API仕様

### 書籍API (`/api/books`)

| メソッド | エンドポイント | 説明 |
|---------|--------------|------|
| GET | `/api/books` | 全書籍取得 |
| GET | `/api/books/{id}` | 書籍詳細取得 |
| GET | `/api/books/search/jpql` | 書籍検索（JPQL） |
| GET | `/api/books/search/criteria` | 書籍検索（Criteria API） |
| POST | `/api/books` | 書籍登録 |
| PUT | `/api/books/{id}` | 書籍更新 |
| DELETE | `/api/books/{id}` | 書籍削除 |

### 在庫API (`/api/stocks`)

| メソッド | エンドポイント | 説明 | 注意 |
|---------|--------------|------|-----|
| GET | `/api/stocks` | 全在庫取得 | |
| GET | `/api/stocks/{bookId}` | 在庫取得 | |
| PUT | `/api/stocks/{bookId}` | 在庫更新 | **楽観的ロック対応** |

* 重要: 在庫更新時は`version`パラメータが必須。競合時はHTTP 409 Conflictを返す。

### カテゴリAPI (`/api/categories`)

| メソッド | エンドポイント | 説明 |
|---------|--------------|------|
| GET | `/api/categories` | 全カテゴリ取得 |
| GET | `/api/categories/{id}` | カテゴリ詳細取得 |

### 出版社API (`/api/publishers`)

| メソッド | エンドポイント | 説明 |
|---------|--------------|------|
| GET | `/api/publishers` | 全出版社取得 |
| GET | `/api/publishers/{id}` | 出版社詳細取得 |

## 🚀 セットアップとコマンド実行ガイド

### 前提条件

* JDK 21以上
* Gradle 8.x以上
* Payara Server 6（プロジェクトルートの`payara6/`に配置）
* HSQLDB（プロジェクトルートの`hsqldb/`に配置）

> **Note:** ① と ② の手順は、ルートの`README.md`を参照してください。

### ③ 依存関係の確認

このプロジェクトを開始する前に、以下が起動していることを確認してください：

* **① HSQLDBサーバー** （`./gradlew startHsqldb`）
* **② Payara Server** （`./gradlew startPayara`）

### ④ プロジェクトを開始するときに1回だけ実行

```bash
# 1. データベーステーブルとデータを作成
./gradlew :back-office-api-sdd:setupHsqldb

# 2. プロジェクトをビルド
./gradlew :back-office-api-sdd:war

# 3. プロジェクトをデプロイ
./gradlew :back-office-api-sdd:deploy
```

> **重要:** `setupHsqldb`を実行すると、`src/main/resources/db/schema.sql`と`sample_data.sql`が実行されます。

### ⑤ プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# プロジェクトをアンデプロイ
./gradlew :back-office-api-sdd:undeploy
```

### ⑥ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
./gradlew :back-office-api-sdd:war
./gradlew :back-office-api-sdd:deploy
```

## 📍 APIエンドポイント

デプロイ後、以下のベースURLでAPIにアクセスできます：

* **ベースURL**: http://localhost:8080/back-office-api-sdd/api
* **ウェルカムページ**: http://localhost:8080/back-office-api-sdd/

## 📝 APIの使用例（curl）

### 1. 全書籍取得

```bash
curl -X GET http://localhost:8080/back-office-api-sdd/api/books
```

### 2. 書籍検索（JPQL）

```bash
curl -X GET "http://localhost:8080/back-office-api-sdd/api/books/search/jpql?keyword=Java&categoryId=1"
```

### 3. 書籍検索（Criteria API）

```bash
curl -X GET "http://localhost:8080/back-office-api-sdd/api/books/search/criteria?keyword=Java&categoryId=1"
```

### 4. 在庫取得

```bash
curl -X GET http://localhost:8080/back-office-api-sdd/api/stocks/1
```

### 5. 在庫更新（楽観的ロック）

```bash
curl -X PUT http://localhost:8080/back-office-api-sdd/api/stocks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 50,
    "version": 0
  }'
```

> **重要**: `version`パラメータが異なる場合、HTTP 409 Conflictが返されます。

### 6. カテゴリ一覧取得

```bash
curl -X GET http://localhost:8080/back-office-api-sdd/api/categories
```

## 🧪 テスト

### テストの実行

このプロジェクトには、サービス層のユニットテストが含まれています。テストはJUnit 5とMockitoを使用して実装されています。

#### すべてのテストを実行

```bash
./gradlew :back-office-api-sdd:test
```

#### 特定のテストクラスを実行

```bash
# BookServiceのテストのみを実行
./gradlew :back-office-api-sdd:test --tests "*BookServiceTest"

# StockServiceのテストのみを実行（楽観的ロックテスト含む）
./gradlew :back-office-api-sdd:test --tests "*StockServiceTest"
```

#### テストの継続的実行（変更検知）

```bash
./gradlew :back-office-api-sdd:test --continuous
```

### テストレポートの確認

テスト実行後、HTMLレポートが生成されます：

```
projects/sdd/bookstore/back-office-api-sdd/build/reports/tests/test/index.html
```

ブラウザで開くとテスト結果の詳細が確認できます。

### テストカバレッジの確認（JaCoCo）

```bash
# テストカバレッジレポートを生成
./gradlew :back-office-api-sdd:jacocoTestReport

# カバレッジレポートの場所
# projects/sdd/bookstore/back-office-api-sdd/build/reports/jacoco/test/html/index.html
```

## 📚 アーキテクチャ

### レイヤー構成

```
BFF (berry-books-api)
    ↓ HTTP/JSON
JAX-RS Resource (@Path, @ApplicationScoped)
    ↓ CORS Filter
CDI Service (@ApplicationScoped)
    ↓
DAO (@ApplicationScoped)
    ↓ JPA
Database (HSQLDB)
```

**注:** このAPIはBFF（berry-books-api）から呼ばれるマイクロサービスです。

### 主要な設計パターン

* **REST Resource Pattern**: JAX-RS
* **Service Layer Pattern**: CDI + Transactional
* **Repository Pattern**: DAO
* **DTO Pattern**: Java Records
* **Dependency Injection**: CDI
* **Optimistic Locking**: `@Version`（在庫管理）
* **Exception Mapper**: JAX-RS
* **CORS Filter**: クロスオリジン対応

### 楽観的ロック制御

在庫テーブル（`STOCK`）に`@Version`カラムを使用し、更新時の同時更新による不整合を防止します。

### トランザクション管理

`StockService.updateStock()`メソッドに`@Transactional`を適用し、在庫更新をアトミックに実行します。

## 📝 データソース設定について

このプロジェクトはルートの`build.gradle`で定義されたタスクを使用してデータソースを作成します。

### 設定内容

* **JNDI名**: `jdbc/HsqldbDS`
* **データベース**: `testdb`
* **ユーザー**: `SA`
* **パスワード**: （空文字）
* **TCPサーバー**: `localhost:9001`

データソースはPayara Serverのドメイン設定に登録されます。

### ⚠️ 注意事項

* HSQLDB Databaseサーバーが起動している必要があります
* データソース作成はPayara Server起動後に実行してください
* 初回のみ実行が必要です（2回目以降は不要）

## 🛑 アプリケーションを停止する

### アプリケーションのアンデプロイ

```bash
./gradlew :back-office-api-sdd:undeploy
```

### Payara Server全体を停止

```bash
./gradlew stopPayara
```

### HSQLDBサーバーを停止

```bash
./gradlew stopHsqldb
```

## 🔍 ログ監視

別のターミナルでログをリアルタイム監視：

```bash
tail -f -n 50 payara6/glassfish/domains/domain1/logs/server.log
```

> **Note**: Windowsでは**Git Bash**を使用してください。

## 🧪 データベースのリセット

データベースを初期状態に戻したい場合：

```bash
# HSQLDBサーバーを停止
./gradlew stopHsqldb

# データファイルを削除
rm -f hsqldb/data/testdb.*

# HSQLDBサーバーを再起動
./gradlew startHsqldb

# 初期データをセットアップ
./gradlew :back-office-api-sdd:setupHsqldb
```

## 📖 参考リンク

* [Jakarta EE 10 Platform](https://jakarta.ee/specifications/platform/10/)
* [Jakarta RESTful Web Services 3.1](https://jakarta.ee/specifications/restful-ws/3.1/)
* [Agent Skills Documentation](https://agentskills.io/what-are-skills)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。
