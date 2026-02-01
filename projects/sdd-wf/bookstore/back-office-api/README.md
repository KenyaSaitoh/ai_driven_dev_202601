# back-office-api-sdd-wf プロジェクト

## 📖 概要

Jakarta EE 10とJAX-RS (Jakarta RESTful Web Services) 3.1を使用したオンライン書店「Berry Books」のバックオフィスAPIアプリケーションです。
書籍・在庫・カテゴリ・出版社の完全なデータ管理をREST APIとして提供するマイクロサービスです。

> Note: このプロジェクトは仕様駆動開発（SDD: Specification-Driven Development）の研修用プロジェクトです。

> SDDとは:
> - 詳細な仕様書（specs/）に基づいて、段階的にコードを生成する手法
> - AIを活用して、仕様書からタスクリスト（tasks/）を生成し、タスクに従って実装を進める
> - 憲章（principles/）に定められた設計原則とベストプラクティスに従う
> - 汎用Agent Skills (`agent_skills/jakarta-ee-api-base/`) を使用した開発

## 🤖 Agent Skillsを使った開発

このプロジェクトは、汎用的な Jakarta EE マイクロサービス開発 Agent Skills を使用して開発します。

開発は以下の7段階プロセスで進めます：

```
ステップ1: 基本設計（SPEC作成）← AIと対話しながら
    ↓
ステップ2: タスク分解（SPEC → タスクリスト）
    ↓
ステップ3: 詳細設計（SPEC → 詳細設計書）← AIと対話しながら
    ↓
ステップ4: コード生成（詳細設計書 → 実装コード + 単体テスト）
    ↓
ステップ5: 単体テスト実行評価（テスト実行 → カバレッジ分析 → フィードバック）
    ↓
ステップ6: 結合テスト生成（basic_design/behaviors.md → JUnit + Weld SE）
    ↓
ステップ7: E2Eテスト生成（requirements/behaviors.md → REST Assured）
```

---

### 📋 開発フロー

#### ステップ1: 基本設計（プロジェクト開始時に1回）

requirements.mdから、システム全体と機能単位の仕様書をAIと対話しながら作成します。

```
@agent_skills/jakarta-ee-api-base/instructions/basic_design.md

仕様書を作成してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api-wf
* spec_directory: projects/sdd-wf/bookstore/back-office-api-wf/specs/baseline
```

* 対話の流れ:
  1. 既存資料（EXCEL、Word等）の有無を確認します
  2. 既存資料がある場合は、Markdown形式に変換します
  3. テンプレートを展開し、各仕様書を対話的に作成します
  4. `specs/baseline/basic_design/*.md` が生成されます

* 生成されるファイル: `specs/baseline/basic_design/*.md`（基本設計SPEC）

---

#### ステップ2: タスク分解（プロジェクト開始時に1回）

仕様書から実装タスクリストを分解・生成します。

```
@agent_skills/jakarta-ee-api-base/instructions/task_breakdown.md

全タスクを分解してください。

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api-wf
* spec_directory: projects/sdd-wf/bookstore/back-office-api-wf/specs/baseline
```

* 生成されるファイル: `tasks/*.md`（タスクリスト）

---

#### ステップ3: 詳細設計（tasks/tasks.mdの順序に従う）

**重要**: 実行順序は `tasks/tasks.md` の「タスク概要」表と「実行順序」セクションを参照してください。
- 「依存タスク」列: このタスクを開始する前に完了が必要なタスク
- 「並行実行可能」列: このタスクと同時に実行可能な他のタスク
- 「レベル」列: 同じレベルのタスクは並行実行可能

コマンドテンプレート:

```
@agent_skills/jakarta-ee-api-base/instructions/detailed_design.md

[タスクID]の詳細設計書を作成してください。

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api-wf
* spec_directory: projects/sdd-wf/bookstore/back-office-api-wf/specs/baseline
* target_type: [tasks/tasks.mdで確認したタスクID]
```

対話の流れ:
1. AIがSPEC（basic_design/）を読み込み、理解した内容を説明します
2. AIが不明点を質問します
3. あなたが回答します
4. `specs/baseline/detailed_design/[タスクID]/detailed_design.md` と `behaviors.md` が生成されます

注意:
* target_typeは `tasks/tasks.md` のタスクファイル名（拡張子なし）と一致させる
* 依存タスクの詳細設計が完了してから実行する（tasks/tasks.mdの「依存タスク」列を参照）

---

#### ステップ4: コード生成（tasks/tasks.mdの順序に従う）

**重要**: 実行順序は `tasks/tasks.md` の「タスク概要」表と「実行順序」セクションを参照してください。
- 「依存タスク」列を確認し、依存タスクが完了してから実行
- 「並行実行可能」列を確認し、並行実行可能なタスクは同時に実装可能

> 単体テストの方針: タスク粒度内のコンポーネント間は実際の連携をテスト。タスク外の依存関係のみモック化。

コマンドテンプレート:

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

[タスクID]を実装してください。

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api-wf
* task_file: projects/sdd-wf/bookstore/back-office-api-wf/tasks/[タスクファイル名]
```

使用例（setup）:

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

setupを実装してください。

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api-wf
* task_file: projects/sdd-wf/bookstore/back-office-api-wf/tasks/setup.md
* skip_infrastructure: true  # setupタスク専用: DB/APサーバーのインストールをスキップ
```

注意:
* `skip_infrastructure` はsetupタスク実行時のみ有効
* 機能タスク（FUNC_XXX）ではこのパラメータは無視される

使用例（機能タスク）:

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

機能タスクを実装してください。

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api-wf
* task_file: projects/sdd-wf/bookstore/back-office-api-wf/tasks/FUNC_001_xxx.md
```

注意: 実際のタスクファイル名は `tasks/tasks.md` を参照してください

使用例（FUNC_002）:

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

FUNC_002を実装してください。

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api-wf
* task_file: projects/sdd-wf/bookstore/back-office-api-wf/tasks/FUNC_002_books.md
```

注意:
* タスクファイル名は `tasks/tasks.md` のタスクファイル列と一致させる
* 各タスクファイル（FUNC_XXX.md）のヘッダーにある「依存タスク」を確認して順序を守る

---

#### ステップ5: 単体テスト実行評価

単体テストを実行してカバレッジを分析し、品質を検証します。

```
@agent_skills/jakarta-ee-api-base/instructions/unit_test_execution.md

単体テストを実行してください。

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api-wf
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
* 必要に応じてステップ3（詳細設計）に戻ってループ

🔄 フィードバックループ:
```
詳細設計 → コード生成 → テスト実行評価
    ↑                         ↓
    └──── フィードバック ←────┘
```

---

#### ステップ6: 結合テスト生成（単体テスト完了後）

単体テスト完了後に、結合テスト（Integration Test）を生成します。

```
@agent_skills/jakarta-ee-api-base/instructions/it_generation.md

結合テストを生成してください。

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api-wf
* spec_directory: projects/sdd-wf/bookstore/back-office-api-wf/specs/baseline
```

AIが：
1. 📄 basic_design/behaviors.md（結合テストシナリオ）を読み込む
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

#### ステップ7: E2Eテスト生成（実装完了後）

全機能実装完了後に、E2Eテスト（End-to-End Test）を生成します。

```
@agent_skills/jakarta-ee-api-base/instructions/e2e_test_generation.md

E2Eテストを生成してください。

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api-wf
* spec_directory: projects/sdd-wf/bookstore/back-office-api-wf/specs/baseline
```

AIが：
1. 📄 requirements/behaviors.md（E2Eテストシナリオ）を読み込む
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

### 🔄 基本設計変更対応（手戻り・拡張案件）

結合テストやE2Eテストで不具合が見つかり、基本設計に戻る必要がある場合や、拡張案件で新機能を追加する場合に使用します。

#### 使用方法

1. 基本設計SPECのマスターファイルを更新
   ```bash
   vim specs/baseline/basic_design/functional_design.md
   vim specs/baseline/basic_design/data_model.md
   ```

2. CHANGES.mdを作成して変更内容を記載
   ```bash
   cp agent_skills/jakarta-ee-api-base/templates/basic_design/CHANGES_template.md \
      specs/baseline/basic_design/CHANGES.md
   vim specs/baseline/basic_design/CHANGES.md
   ```

3. 変更対応を実行
   ```
   @agent_skills/jakarta-ee-api-base/instructions/basic_design_change.md
   
   基本設計の変更を適用してください。
   
   パラメータ:
   * project_root: projects/sdd-wf/bookstore/back-office-api-wf
   * spec_directory: projects/sdd-wf/bookstore/back-office-api-wf/specs/baseline
   ```

AIが：
1. 📄 CHANGES.md（変更差分ファイル）を読み込み
2. 🔍 変更の影響を受けるファイル（詳細設計、コード、テスト）を特定
3. 📋 変更タスクファイル（`tasks/change_tasks.md`）を生成
4. 🎯 既存の指示書を呼び出して、影響を受けるファイルを更新
5. ✅ すべての変更適用後、CHANGES.mdをアーカイブ

#### ディレクトリ構造

```
specs/baseline/basic_design/
  ├── functional_design.md      # マスター（自由に編集）
  ├── data_model.md             # マスター（自由に編集）
  ├── CHANGES.md                # アクティブな変更（未適用）
  └── changes_archive/          # 履歴
      ├── 20260118_book_discount.md
      └── 20260125_stock_alert.md
```

---

### 📚 詳細情報

詳細は `@agent_skills/jakarta-ee-api-base/README.md` を参照してください。

#### 開発原則

このプロジェクトは、以下の原則に従って開発されます：

* 場所: `@agent_skills/jakarta-ee-api-base/principles/`
  * [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](../../../agent_skills/jakarta-ee-api-base/principles/security.md) - セキュリティ標準
  * [common_rules.md](../../../agent_skills/jakarta-ee-api-base/principles/common_rules.md) - 共通ルール

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
back-office-api-sdd-wf/
├── specs/                          # 仕様書（SDD）
│   ├── baseline/
│   │   ├── requirements/           # システム要件
│   │   │   ├── requirements.md    # 要件定義書
│   │   │   └── behaviors.md       # E2Eテスト用（要件を外形的に捉えた振る舞い）
│   │   ├── basic_design/           # 基本設計SPEC
│   │   │   ├── architecture_design.md
│   │   │   ├── functional_design.md
│   │   │   ├── data_model.md
│   │   │   └── behaviors.md       # 結合テスト用（基本設計を外形的に捉えた振る舞い）
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
./gradlew :back-office-api-sdd-wf:setupHsqldb

# 2. プロジェクトをビルド
./gradlew :back-office-api-sdd-wf:war

# 3. プロジェクトをデプロイ
./gradlew :back-office-api-sdd-wf:deploy
```

> 重要: `setupHsqldb`を実行すると、`src/main/resources/db/schema.sql`と`sample_data.sql`が実行されます。

### ⑤ プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# プロジェクトをアンデプロイ
./gradlew :back-office-api-sdd-wf:undeploy
```

### ⑥ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
./gradlew :back-office-api-sdd-wf:war
./gradlew :back-office-api-sdd-wf:deploy
```

## 📍 APIエンドポイント

デプロイ後、以下のベースURLでAPIにアクセスできます：

* ベースURL: http://localhost:8080/back-office-api-sdd-wf/api
* ウェルカムページ: http://localhost:8080/back-office-api-sdd-wf/

## 📝 APIの使用例（curl）

### 1. 全書籍取得

```bash
curl -X GET http://localhost:8080/back-office-api-sdd-wf/api/books
```

### 2. 書籍検索（JPQL）

```bash
curl -X GET "http://localhost:8080/back-office-api-sdd-wf/api/books/search/jpql?keyword=Java&categoryId=1"
```

### 3. 書籍検索（Criteria API）

```bash
curl -X GET "http://localhost:8080/back-office-api-sdd-wf/api/books/search/criteria?keyword=Java&categoryId=1"
```

### 4. 在庫取得

```bash
curl -X GET http://localhost:8080/back-office-api-sdd-wf/api/stocks/1
```

### 5. 在庫更新（楽観的ロック）

```bash
curl -X PUT http://localhost:8080/back-office-api-sdd-wf/api/stocks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 50,
    "version": 0
  }'
```

> 重要: `version`パラメータが異なる場合、HTTP 409 Conflictが返されます。

### 6. カテゴリ一覧取得

```bash
curl -X GET http://localhost:8080/back-office-api-sdd-wf/api/categories
```

## 🧪 テスト

### テストの実行

このプロジェクトには、サービス層のユニットテストが含まれています。テストはJUnit 5とMockitoを使用して実装されています。

#### すべてのテストを実行

```bash
./gradlew :back-office-api-sdd-wf:test
```

#### 特定のテストクラスを実行

```bash
# BookServiceのテストのみを実行
./gradlew :back-office-api-sdd-wf:test --tests "*BookServiceTest"

# StockServiceのテストのみを実行（楽観的ロックテスト含む）
./gradlew :back-office-api-sdd-wf:test --tests "*StockServiceTest"
```

#### テストの継続的実行（変更検知）

```bash
./gradlew :back-office-api-sdd-wf:test --continuous
```

### テストレポートの確認

テスト実行後、HTMLレポートが生成されます：

```
projects/sdd-wf/bookstore/back-office-api-wf/build/reports/tests/test/index.html
```

ブラウザで開くとテスト結果の詳細が確認できます。

### テストカバレッジの確認（JaCoCo）

```bash
# テストカバレッジレポートを生成
./gradlew :back-office-api-sdd-wf:jacocoTestReport

# カバレッジレポートの場所
# projects/sdd-wf/bookstore/back-office-api-wf/build/reports/jacoco/test/html/index.html
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
./gradlew :back-office-api-sdd-wf:undeploy
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
./gradlew :back-office-api-sdd-wf:setupHsqldb
```

## 🧹 SDD成果物のクリーンアップ

仕様駆動開発により何度でも再実装できます。詳細は [ルートREADMEのSDDクリーンアップ節](../../../README.md#仕様駆動開発sddプロジェクトの成果物クリーンアップ) を参照してください。

```bash
# タスクファイルのみ削除
./gradlew :back-office-api-sdd-wf:cleanTasks

# 詳細設計SPECのみ削除
./gradlew :back-office-api-sdd-wf:cleanDetailedDesign

# 実装コードのみ削除（src/, build/）
./gradlew :back-office-api-sdd-wf:cleanCode

# すべて削除（requirements/, basic_design/ は保護）
./gradlew :back-office-api-sdd-wf:cleanAllSdd
```

* 保護されるSPEC: `specs/baseline/requirements/`, `specs/baseline/basic_design/`

## 📖 参考リンク

### Agent Skills

* [Agent Skills README](../../../agent_skills/jakarta-ee-api-base/README.md) - 使い方ガイド
* [開発原則](../../../agent_skills/jakarta-ee-api-base/principles/)
  * [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](../../../agent_skills/jakarta-ee-api-base/principles/security.md) - セキュリティ標準
  * [common_rules.md](../../../agent_skills/jakarta-ee-api-base/principles/common_rules.md) - 共通ルール

### Jakarta EE仕様

* [Jakarta EE 10 Platform](https://jakarta.ee/specifications/platform/10/)
* [Jakarta RESTful Web Services 3.1](https://jakarta.ee/specifications/restful-ws/3.1/)
* [Jakarta Persistence 3.1](https://jakarta.ee/specifications/persistence/3.1/)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。
