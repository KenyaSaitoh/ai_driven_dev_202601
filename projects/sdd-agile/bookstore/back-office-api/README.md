# back-office-api-sdd-agile プロジェクト（sdd-agile）

## 📖 概要

Jakarta EE 10とJAX-RS (Jakarta RESTful Web Services) 3.1を使用したオンライン書店「Berry Books」のバックオフィスAPIアプリケーションです。
書籍・在庫・カテゴリ・出版社の完全なデータ管理をREST APIとして提供するマイクロサービスです。

> **sdd-agile**: このプロジェクトはアジャイル開発向けの仕様駆動開発用です（`projects/sdd-agile/bookstore/` に配置）。  
> **Gradle プロジェクト名**: `back-office-api-sdd-agile`（ビルド・デプロイ時はこの名前を使用。コンテキストルート: `/back-office-api-sdd-agile`）

> Note: このプロジェクトは仕様駆動開発（SDD: Specification-Driven Development）の研修用プロジェクトです。

> SDDとは:
> - 詳細な仕様書（specs/）に基づいて、段階的にコードを生成する手法
> - AIを活用して、仕様書からタスクリスト（tasks/）を生成し、タスクに従って実装を進める
> - 憲章（principles/）に定められた設計原則とベストプラクティスに従う
> - アジャイル用Agent Skills (`agent_skills/jakarta-ee-api-agile/`) を使用した開発

## 🤖 Agent Skillsを使った開発（アジャイル）

このプロジェクトは、アジャイル向け Jakarta EE API 開発 Agent Skills（jakarta-ee-api-agile）を使用します。SPECは `specs/baseline/common/` と `specs/baseline/usecases/{名}/` で管理します。

開発は以下の流れで進めます：

```
ステップ1: common SPEC + ユースケースSPEC（common/ + usecases/{名}/）
    ↓
ステップ2: タスク分解（common + ユースケース単位）
    ↓
ステップ3: コード生成（common 先行 → ユースケース単位）
    ↓
ステップ4: 単体テスト実行評価
    ↓
ステップ5: 結合テスト生成（usecases/*/behaviors.md → JUnit + Weld SE）
    ↓
ステップ6: E2Eテスト生成（usecases 等の behaviors → REST Assured）
```

---

### 📋 開発フロー

#### ステップ1: common SPEC + ユースケースSPEC（プロジェクト開始時・拡張時）

common（data_model, external_interface, architecture_design）を先に整え、各ユースケースに userstory.md / behaviors.md を配置します。

```
@agent_skills/jakarta-ee-api-agile/instructions/common_spec.md   # common/ の3SPEC
@agent_skills/jakarta-ee-api-agile/instructions/usecase_spec.md   # usecases/{名}/ の userstory + behaviors

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api
* spec_directory: projects/sdd-agile/bookstore/back-office-api/specs/baseline
```

* 配置: `specs/baseline/common/*.md`, `specs/baseline/usecases/{auth|books|category|publisher|stocks|workflow}/userstory.md`, `behaviors.md`

---

#### ステップ2: タスク分解（common + ユースケース単位）

common 用タスクとユースケース別タスクに分解します。

```
@agent_skills/jakarta-ee-api-agile/instructions/task_breakdown.md

全タスクを分解してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api
* spec_directory: projects/sdd-agile/bookstore/back-office-api/specs/baseline
```

* 参照: `specs/baseline/common/`, `specs/baseline/usecases/{名}/`

---

#### ステップ3: コード生成（tasks/tasks.mdの順序に従う）

common/ の3SPEC と usecases/{名}/userstory.md, behaviors.md を駆動元に、タスクに従い実装と単体テストを生成します。

**重要**: 実行順序は `tasks/tasks.md` の「タスク概要」表と「実行順序」セクションを参照してください。
- 「依存タスク」列を確認し、依存タスクが完了してから実行
- 「並行実行可能」列を確認し、並行実行可能なタスクは同時に実装可能

> 単体テストの方針: タスク粒度内のコンポーネント間は実際の連携をテスト。タスク外の依存関係のみモック化。

コマンドテンプレート:

```
@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md

[タスクID]を実装してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api-agile
* task_file: projects/sdd-agile/bookstore/back-office-api-agile/tasks/[タスクファイル名]
```

使用例（setup）:

```
@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md

setupを実装してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api-agile
* task_file: projects/sdd-agile/bookstore/back-office-api-agile/tasks/setup.md
* skip_infrastructure: true  # setupタスク専用: DB/APサーバーのインストールをスキップ
```

注意:
* `skip_infrastructure` はsetupタスク実行時のみ有効
* 機能タスク（FUNC_XXX）ではこのパラメータは無視される

使用例（機能タスク）:

```
@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md

機能タスクを実装してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api-agile
* task_file: projects/sdd-agile/bookstore/back-office-api-agile/tasks/FUNC_001_xxx.md
```

注意: 実際のタスクファイル名は `tasks/tasks.md` を参照してください

使用例（FUNC_002）:

```
@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md

FUNC_002を実装してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api-agile
* task_file: projects/sdd-agile/bookstore/back-office-api-agile/tasks/FUNC_002_books.md
```

注意:
* タスクファイル名は `tasks/tasks.md` のタスクファイル列と一致させる
* 各タスクファイル（FUNC_XXX.md）のヘッダーにある「依存タスク」を確認して順序を守る

---

#### ステップ5: 単体テスト実行評価

単体テストを実行してカバレッジを分析し、品質を検証します。

```
@agent_skills/jakarta-ee-api-agile/instructions/unit_test_execution.md

単体テストを実行してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api-agile
* target_type: FUNC_002_books
```

AIが：
1. 🧪 テスト実行（gradle test jacocoTestReport）
2. 📊 テスト結果とカバレッジ分析
3. 🔍 問題の分類（テスト失敗、必要な振る舞い、デッドコード）
4. 📋 フィードバックレポート生成
5. 💬 ユーザーに推奨アクションを提示

重要：
* 問題を発見してもユーザー確認なしに修正しない
* カバレッジ不足やデッドコードを具体的に提案
* 必要に応じてコード生成（または common/usecases SPEC の見直し）に戻ってループ

🔄 フィードバックループ:
```
詳細設計 → コード生成 → テスト実行評価
    ↑                         ↓
    └──── フィードバック ←────┘
```

---

#### ステップ5: 結合テスト生成（単体テスト完了後）

単体テスト完了後に、結合テスト（Integration Test）を生成します。

```
@agent_skills/jakarta-ee-api-base/instructions/it_generation.md

結合テストを生成してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api-agile
* spec_directory: projects/sdd-agile/bookstore/back-office-api-agile/specs/baseline
```

AIが：
1. 📄 usecases/*/behaviors.md（結合テストシナリオ）を読み込む
2. 🧪 JUnit 5 + Weld SE を使用した結合テストを生成
   * Service層以下（Service + DAO + Entity + DB）の連携テスト
   * 実際のDBアクセス（メモリDB）
   * 外部APIはWireMockでスタブ化
   * アプリケーションサーバー不要
3. 🏷️ `@Tag("integration")` で結合テストを分離

実行方法:
```bash
# 結合テストを実行
./gradlew integrationTest
```

---

#### ステップ6: E2Eテスト生成（実装完了後）

全機能実装完了後に、E2Eテスト（End-to-End Test）を生成します。

```
@agent_skills/jakarta-ee-api-agile/instructions/e2e_test_generation.md

E2Eテストを生成してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api-agile
* spec_directory: projects/sdd-agile/bookstore/back-office-api-agile/specs/baseline
```

AIが：
1. 📄 usecases/*/behaviors.md 等（E2Eテストシナリオ）を読み込む
2. 🧪 REST Assured を使用したE2Eテストを生成
   * 複数API間の連携テスト（認証 → 書籍検索 → 在庫更新等）
   * 実際のHTTPリクエスト/レスポンス
   * 実際のDBアクセスを含む
3. 🏷️ `@Tag("e2e")` でE2Eテストを分離

実行方法:
```bash
# アプリケーションサーバーを起動
./gradlew run

# 別ターミナルでE2Eテストを実行
./gradlew e2eTest
```

---

### 🔄 SPEC変更対応（アジャイル）

common またはユースケースの仕様変更時は、`@agent_skills/jakarta-ee-api-agile/instructions/spec_change.md` を使用して影響範囲を適用します。変更対象は `specs/baseline/common/` または `specs/baseline/usecases/{名}/` の userstory.md / behaviors.md / common の3SPECです。

---

### 📚 詳細情報

詳細は `@agent_skills/jakarta-ee-api-agile/README.md` を参照してください。

#### 開発原則

このプロジェクトは、以下の原則に従って開発されます：

* 場所: `@agent_skills/jakarta-ee-api-agile/principles/`
  * [architecture.md](../../../agent_skills/jakarta-ee-api-agile/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](../../../agent_skills/jakarta-ee-api-agile/principles/security.md) - セキュリティ標準
  * [common_rules.md](../../../agent_skills/jakarta-ee-api-agile/principles/common_rules.md) - 共通ルール

* 主な内容:
  * 標準技術スタック（Jakarta EE 10、JPA 3.1、JAX-RS 3.1）
  * レイヤードアーキテクチャ（API、Service、DAO、Entity）
  * 開発標準（命名規則、コーディング規約、バリデーション、エラーハンドリング）
  * セキュリティ実装（JWT認証、認証フィルター）
  * トランザクション管理と並行制御（楽観的ロック）
  * テスト戦略、パフォーマンス考慮事項

## 🎯 プロジェクトの特徴（マイクロサービスパターン）

### アーキテクチャ
* 独立したデータ管理サービス: 書籍・在庫・カテゴリ・出版社の完全管理
* マイクロサービス: berry-books-apiから呼ばれるバックエンドサービス
* REST API: データ管理機能をREST APIとして提供
* CORS対応: クロスオリジンリクエストに対応

### 実装する全エンティティ
* ✅ Book（書籍）
* ✅ Stock（在庫）- 楽観的ロック必須（@Version）
* ✅ Category（カテゴリ）
* ✅ Publisher（出版社）

### 重要な実装要件

#### 楽観的ロック（Optimistic Locking）
* Stockエンティティに`@Version`アノテーション使用
* 在庫更新時の競合を検出
* `OptimisticLockException` → HTTP 409 Conflict

#### 2種類の書籍検索実装
* JPQL検索（`BookDao`）: 動的クエリ、シンプル
* Criteria API検索（`BookDaoCriteria`）: 型安全、コンパイル時チェック
* 両方実装: 比較学習が可能

#### CORS設定
* berry-books-apiからのクロスオリジンリクエスト対応
* `CorsFilter`実装

## 🔧 使用している技術

### 本番環境

* Jakarta EE 10
* Payara Server 6
* JAX-RS (Jakarta RESTful Web Services) 3.1 - REST API
* Jakarta Persistence (JPA) 3.1 - Hibernate実装
* Jakarta Transactions (JTA)
* Jakarta CDI 4.0
* Jakarta Bean Validation 3.0
* HSQLDB 2.7.x

### テスト環境

* JUnit 5 - テストフレームワーク
* Mockito - モックライブラリ
* JaCoCo - カバレッジツール（オプション）

## プロジェクト構成

```
back-office-api-sdd-agile/
├── specs/                          # 仕様書（SDD）
│   ├── baseline/
│   │   ├── common/                 # 共通SPEC
│   │   │   ├── data_model.md
│   │   │   ├── external_interface.md
│   │   │   └── architecture_design.md
│   │   ├── usecases/               # ユースケース別
│   │   │   ├── auth/               # userstory.md, behaviors.md
│   │   │   ├── books/
│   │   │   ├── category/
│   │   │   ├── publisher/
│   │   │   ├── stocks/
│   │   │   └── workflow/
│   │   └── detailed_design/        # 詳細設計SPEC
│   │       ├── common/
│   │       │   ├── detailed_design.md
│   │       │   └── behaviors.md   # 単体テスト用
│   │       ├── FUNC_001_books/
│   │       │   ├── detailed_design.md
│   │       │   └── behaviors.md
│   │       ├── FUNC_002_stocks/
│   │       │   ├── detailed_design.md
│   │       │   └── behaviors.md
│   │       └── FUNC_003_categories/
│   │           ├── detailed_design.md
│   │           └── behaviors.md
│   └── enhancements/               # 機能拡張仕様
├── principles/                     # 開発憲章
│   └── constitution.md
├── tasks/                          # タスクリスト（AI生成）
│   ├── tasks.md
│   ├── setup.md
│   ├── FUNC_001_infrastructure.md
│   ├── FUNC_001_books.md
│   └── FUNC_002_stocks.md
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
│   │   │       └── FUNC_001_infrastructure/   # Infrastructure
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
| PUT | `/api/stocks/{bookId}` | 在庫更新 | 楽観的ロック対応 |

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

> Note: ① と ② の手順は、ルートの`README.md`を参照してください。

### ③ 依存関係の確認

このプロジェクトを開始する前に、以下が起動していることを確認してください：

* ① HSQLDBサーバー （`./gradlew startHsqldb`）
* ② Payara Server （`./gradlew startPayara`）

### ④ プロジェクトを開始するときに1回だけ実行

```bash
# 1. データベーステーブルとデータを作成
./gradlew :back-office-api-sdd-agile:setupHsqldb

# 2. プロジェクトをビルド
./gradlew :back-office-api-sdd-agile:war

# 3. プロジェクトをデプロイ
./gradlew :back-office-api-sdd-agile:deploy
```

> 重要: `setupHsqldb`を実行すると、`src/main/resources/db/schema.sql`と`sample_data.sql`が実行されます。

### ⑤ プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# プロジェクトをアンデプロイ
./gradlew :back-office-api-sdd-agile:undeploy
```

### ⑥ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
./gradlew :back-office-api-sdd-agile:war
./gradlew :back-office-api-sdd-agile:deploy
```

## 📍 APIエンドポイント

デプロイ後、以下のベースURLでAPIにアクセスできます：

* ベースURL: http://localhost:8080/back-office-api-sdd-agile/api
* ウェルカムページ: http://localhost:8080/back-office-api-sdd-agile/

## 📝 APIの使用例（curl）

### 1. 全書籍取得

```bash
curl -X GET http://localhost:8080/back-office-api-sdd-agile/api/books
```

### 2. 書籍検索（JPQL）

```bash
curl -X GET "http://localhost:8080/back-office-api-sdd-agile/api/books/search/jpql?keyword=Java&categoryId=1"
```

### 3. 書籍検索（Criteria API）

```bash
curl -X GET "http://localhost:8080/back-office-api-sdd-agile/api/books/search/criteria?keyword=Java&categoryId=1"
```

### 4. 在庫取得

```bash
curl -X GET http://localhost:8080/back-office-api-sdd-agile/api/stocks/1
```

### 5. 在庫更新（楽観的ロック）

```bash
curl -X PUT http://localhost:8080/back-office-api-sdd-agile/api/stocks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 50,
    "version": 0
  }'
```

> 重要: `version`パラメータが異なる場合、HTTP 409 Conflictが返されます。

### 6. カテゴリ一覧取得

```bash
curl -X GET http://localhost:8080/back-office-api-sdd-agile/api/categories
```

## 🧪 テスト

### テストの実行

このプロジェクトには、サービス層のユニットテストが含まれています。テストはJUnit 5とMockitoを使用して実装されています。

#### すべてのテストを実行

```bash
./gradlew :back-office-api-sdd-agile:test
```

#### 特定のテストクラスを実行

```bash
# BookServiceのテストのみを実行
./gradlew :back-office-api-sdd-agile:test --tests "*BookServiceTest"

# StockServiceのテストのみを実行（楽観的ロックテスト含む）
./gradlew :back-office-api-sdd-agile:test --tests "*StockServiceTest"
```

#### テストの継続的実行（変更検知）

```bash
./gradlew :back-office-api-sdd-agile:test --continuous
```

### テストレポートの確認

テスト実行後、HTMLレポートが生成されます：

```
projects/sdd-agile/bookstore/back-office-api-agile/build/reports/tests/test/index.html
```

ブラウザで開くとテスト結果の詳細が確認できます。

### テストカバレッジの確認（JaCoCo）

```bash
# テストカバレッジレポートを生成
./gradlew :back-office-api-sdd-agile:jacocoTestReport

# カバレッジレポートの場所
# projects/sdd-agile/bookstore/back-office-api-agile/build/reports/jacoco/test/html/index.html
```

## 📚 アーキテクチャ

### レイヤー構成

```
berry-books-api
    ↓ HTTP/JSON
JAX-RS Resource (@Path, @ApplicationScoped)
    ↓ CORS Filter
CDI Service (@ApplicationScoped)
    ↓
DAO (@ApplicationScoped)
    ↓ JPA
Database (HSQLDB)
```

注: このAPIはberry-books-apiから呼ばれるマイクロサービスです。

### 主要な設計パターン

* REST Resource Pattern: JAX-RS
* Service Layer Pattern: CDI + Transactional
* Repository Pattern: DAO
* DTO Pattern: Java Records
* Dependency Injection: CDI
* Optimistic Locking: `@Version`（在庫管理）
* Exception Mapper: JAX-RS
* CORS Filter: クロスオリジン対応

### 楽観的ロック制御

在庫テーブル（`STOCK`）に`@Version`カラムを使用し、更新時の同時更新による不整合を防止します。

### トランザクション管理

`StockService.updateStock()`メソッドに`@Transactional`を適用し、在庫更新をアトミックに実行します。

## 📝 データソース設定について

このプロジェクトはルートの`build.gradle`で定義されたタスクを使用してデータソースを作成します。

### 設定内容

* JNDI名: `jdbc/HsqldbDS`
* データベース: `testdb`
* ユーザー: `SA`
* パスワード: （空文字）
* TCPサーバー: `localhost:9001`

データソースはPayara Serverのドメイン設定に登録されます。

### ⚠️ 注意事項

* HSQLDB Databaseサーバーが起動している必要があります
* データソース作成はPayara Server起動後に実行してください
* 初回のみ実行が必要です（2回目以降は不要）

## 🛑 アプリケーションを停止する

### アプリケーションのアンデプロイ

```bash
./gradlew :back-office-api-sdd-agile:undeploy
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

> Note: WindowsではGit Bashを使用してください。

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
./gradlew :back-office-api-sdd-agile:setupHsqldb
```

## 📖 参考リンク

### Agent Skills

* [Agent Skills README](../../../agent_skills/jakarta-ee-api-agile/README.md) - 使い方ガイド
* [開発原則](../../../agent_skills/jakarta-ee-api-agile/principles/)
  * [architecture.md](../../../agent_skills/jakarta-ee-api-agile/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](../../../agent_skills/jakarta-ee-api-agile/principles/security.md) - セキュリティ標準
  * [common_rules.md](../../../agent_skills/jakarta-ee-api-agile/principles/common_rules.md) - 共通ルール

### Jakarta EE仕様

* [Jakarta EE 10 Platform](https://jakarta.ee/specifications/platform/10/)
* [Jakarta RESTful Web Services 3.1](https://jakarta.ee/specifications/restful-ws/3.1/)
* [Jakarta Persistence 3.1](https://jakarta.ee/specifications/persistence/3.1/)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。
