# back-office-api-sdd-agile プロジェクト（sdd-agile）

## 📖 概要

Jakarta EE 10とJAX-RS (Jakarta RESTful Web Services) 3.1を使用したオンライン書店「Berry Books」のバックオフィスAPIアプリケーションです。
書籍・在庫・カテゴリ・出版社の完全なデータ管理をREST APIとして提供するマイクロサービスです。

> **sdd-agile**: このプロジェクトはアジャイル開発向けの仕様駆動開発用です（`projects/sdd-agile/bookstore/` に配置）。  
> **Gradle プロジェクト名**: `back-office-api-sdd-agile`（ビルド・デプロイ時はこの名前を使用。コンテキストルート: `/back-office-api-sdd-agile`）

> Note: このプロジェクトは仕様駆動開発（SDD: Specification-Driven Development）の研修用プロジェクトです。

> SDDとは:
> - 業務共通SPEC（common/）とユースケースSPEC（usecases/{名}/）に基づいて実装を進める手法（ウォーターフォールの basic_design/ や detailed_design/ は不要）
> - タスク分解は不要。target 指定で common または usecases/{名} 単位でコード生成
> - Agent Skills (`agent_skills/jakarta-ee-api-agile/`) の principles/ に定められた設計原則に従う

## 🤖 Agent Skillsを使った開発（アジャイル）

このプロジェクトは、アジャイル向け Jakarta EE API 開発 Agent Skills（jakarta-ee-api-agile）を使用します。SPECは `specs/baseline/common/` と `specs/baseline/usecases/{名}/` で管理します。**タスク分解（tasks/）は不要**です。

開発は以下の流れで進めます：

```
ステップ1: 業務共通SPEC + ユースケースSPEC
    ↓
ステップ2: コード生成（target=common または target=usecases/{名}）
    ↓
ステップ3: 単体テスト実行
    ↓
ステップ4: 単体テスト評価（カバレッジ分析）
    ↓
ステップ5: 結合テスト生成
    ↓
ステップ6: E2Eテスト生成
```

---

### 📋 開発フロー

#### ステップ1: 業務共通SPEC + ユースケースSPEC（プロジェクト開始時・拡張時）

業務共通SPEC（`common/`）とユースケースSPEC（`usecases/{名}/`）を配置します。

```
@agent_skills/jakarta-ee-api-agile/instructions/common_spec.md
@agent_skills/jakarta-ee-api-agile/instructions/usecase_spec.md

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api
* spec_directory: projects/sdd-agile/bookstore/back-office-api/specs/baseline
```

**配置:** `specs/baseline/common/*.md`, `specs/baseline/usecases/{名}/userstory.md`, `behaviors.md`

---

#### ステップ2: コード生成（target 指定で実装＋単体テスト）

**target** で指定した対象（common または usecases/{名}）の実装と単体テストを生成します。タスクファイル不要。

**実行順序:** `target=common` → 各ユースケース `target=usecases/{名}`

コマンド例（業務共通・common）:

```
@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md

業務共通（common）のコードを生成してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api
* target: common
* skip_infrastructure: true  # 初回setup時: DB/APサーバーセットアップをスキップする場合
```

コマンド例（ユースケース）:

```
@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md

ユースケース books のコードを生成してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api
* target: usecases/books
```

他のユースケース（auth, stocks, category, publisher, workflow）も同様に `target: usecases/{名}` で実装。

**SPEC変更時:** targetを指定して再実行すれば差分が反映されます。

---

#### ステップ3: 単体テスト実行

生成された単体テストを実行してJacocoレポートを生成します。

```bash
# リポジトリルート（ai_driven_dev_202601/）で実行
cd ../../../../  # プロジェクトルートからリポジトリルートへ移動

# 単体テストを実行してJacocoレポートを生成
./gradlew :back-office-api-sdd-agile:test :back-office-api-sdd-agile:jacocoTestReport
```

テストレポート: `build/reports/tests/test/index.html`
Jacocoレポート: `build/reports/jacoco/test/html/index.html`

---

#### ステップ4: 単体テスト評価（カバレッジ分析）

Jacocoレポートを分析し、テスト品質を評価します。

```
@agent_skills/jakarta-ee-api-agile/instructions/test_evaluation.md

テスト実行結果を評価してください

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api
* jacoco_reports_dir: build/reports/jacoco/test
* test_type: unit
* spec_directory: projects/sdd-agile/bookstore/back-office-api/specs/baseline
```

AIが実行する内容:
1. 📊 Jacocoレポート（XML）を読み込み
2. 📈 カバレッジ評価（行、分岐、メソッド）
3. 🔍 パッケージ別/クラス別/メソッド別分析
4. ⚠️ デッドコード検出
5. 💬 改善提案

**注意:** カバレッジ不足がある場合は、SPEC見直しまたはコード生成に戻ってフィードバック

---

#### ステップ5: 結合テスト生成（単体テスト完了後）

単体テスト完了後に、結合テスト（JUnit 5 + Weld SE）を生成します。

```
@agent_skills/jakarta-ee-api-agile/instructions/it_generation.md

結合テストコードを生成してください

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api
* spec_directory: projects/sdd-agile/bookstore/back-office-api/specs/baseline
```

**テスト実行:**
```bash
# リポジトリルート（ai_driven_dev_202601/）で実行
./gradlew :back-office-api-sdd-agile:integrationTest :back-office-api-sdd-agile:jacocoIntegrationTestReport
```

AIが`usecases/*/behaviors.md`を読み込み、JUnit 5 + Weld SEを使用した結合テストを生成。

---

#### ステップ6: E2Eテスト生成（実装完了後）

全機能実装完了後に、E2Eテスト（JUnit 5 + REST Assured）を生成します。

```
@agent_skills/jakarta-ee-api-agile/instructions/e2e_test_generation.md

E2Eテストコードを生成してください

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api
* spec_directory: projects/sdd-agile/bookstore/back-office-api/specs/baseline
```

**実行方法:**（アプリケーションサーバー起動後）
```bash
# 1. アプリケーションをビルド＆デプロイ
./gradlew :back-office-api-sdd-agile:war
./gradlew :back-office-api-sdd-agile:deploy

# 2. E2Eテストを実行
./gradlew :back-office-api-sdd-agile:e2eTest :back-office-api-sdd-agile:jacocoE2eTestReport
```

AIが`usecases/*/behaviors.md`を読み込み、JUnit 5 + REST Assuredを使用したE2Eテストを生成。

---

### 🔄 SPEC変更時の対応

SPEC更新後、`code_generation.md` で target を指定して再実行すれば差分が反映されます。

---

### 📚 詳細情報

詳細は `@agent_skills/jakarta-ee-api-agile/README.md` を参照してください。

#### 開発原則

詳細は `@agent_skills/jakarta-ee-api-agile/principles/` を参照:
* [architecture.md](../../../agent_skills/jakarta-ee-api-agile/principles/architecture.md) - アーキテクチャ標準
* [security.md](../../../agent_skills/jakarta-ee-api-agile/principles/security.md) - セキュリティ標準
* [common_rules.md](../../../agent_skills/jakarta-ee-api-agile/principles/common_rules.md) - 共通ルール

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

### SPEC構成（アジャイル）

```
specs/baseline/
├── common/                     # 業務共通SPEC
│   ├── architecture_design.md
│   ├── data_model.md
│   └── external_interface.md
└── usecases/                   # ユースケース別（各フォルダに userstory.md, behaviors.md）
    ├── auth/
    ├── books/
    ├── category/
    ├── publisher/
    ├── stocks/
    └── workflow/
```

**注意:** ウォーターフォール版の`basic_design/`、`detailed_design/`、`tasks/`は不使用。

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

**マルチプロジェクト構成について:**
* このプロジェクトは、リポジトリルートの `build.gradle` を使用するマルチプロジェクト構成です
* Gradleコマンドは、リポジトリルート（`ai_driven_dev_202601/`）で実行します
* プロジェクト指定は `:プロジェクト名:タスク名` の形式を使用します（例: `:back-office-api-sdd-agile:test`）

#### すべてのテストを実行

リポジトリルートから実行:
```bash
cd ai_driven_dev_202601
./gradlew :back-office-api-sdd-agile:test
```

またはプロジェクトルートから実行（相対パスでgradlewを指定）:
```bash
cd projects/sdd-agile/bookstore/back-office-api
../../../../gradlew test
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
projects/sdd-agile/bookstore/back-office-api/build/reports/tests/test/index.html
```

ブラウザで開くとテスト結果の詳細が確認できます。

### テストカバレッジの確認（JaCoCo）

```bash
# テストカバレッジレポートを生成
./gradlew :back-office-api-sdd-agile:jacocoTestReport

# カバレッジレポートの場所
# projects/sdd-agile/bookstore/back-office-api/build/reports/jacoco/test/html/index.html
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

## 🧹 SDD成果物のクリーンアップ

仕様駆動開発により何度でも再実装できます。

```bash
# 本番コード・単体テストコードを削除（src/main/, src/test/, build/）
./gradlew :back-office-api-sdd-agile:cleanCode
```

**保護されるSPEC:** `specs/baseline/common/`, `specs/baseline/usecases/`

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
