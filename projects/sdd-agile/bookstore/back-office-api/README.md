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
ステップ1: 業務共通SPEC + ユースケースSPEC（common/ + usecases/{名}/）
    ↓
ステップ2: コード生成（target=common または target=usecases/{名}）
    ↓
ステップ3: 単体テスト実行評価
    ↓
ステップ4: 結合テスト生成（usecases/*/behaviors.md → JUnit + Weld SE）
    ↓
ステップ5: E2Eテスト生成（usecases 等の behaviors → REST Assured）
```

---

### 📋 開発フロー

#### ステップ1: 業務共通SPEC + ユースケースSPEC（プロジェクト開始時・拡張時）

業務共通SPEC（data_model, external_interface, architecture_design）を先に整え、各ユースケースに userstory.md / behaviors.md を配置します。

```
@agent_skills/jakarta-ee-api-agile/instructions/common_spec.md   # 業務共通SPEC（common/）の3SPEC
@agent_skills/jakarta-ee-api-agile/instructions/usecase_spec.md   # usecases/{名}/ の userstory + behaviors

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api
* spec_directory: projects/sdd-agile/bookstore/back-office-api/specs/baseline
```

* 配置: `specs/baseline/common/*.md`, `specs/baseline/usecases/{auth|books|category|publisher|stocks|workflow}/userstory.md`, `behaviors.md`

---

#### ステップ2: コード生成（target 指定で実装＋単体テスト）

業務共通SPEC（common/）の3SPEC と usecases/{名}/userstory.md, behaviors.md を駆動元に、**target** で指定した対象の実装と単体テストを生成します。タスクファイル（tasks/）は不要です。

**実行順序**: 先に `target=common` で業務共通実装を完了し、続いて各ユースケースを `target=usecases/{名}` で順に実装します。

使用例（業務共通・common）:

```
@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md

業務共通（common）のコードを生成してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api
* target: common
* skip_infrastructure: true  # 初回setup時: DB/APサーバーセットアップをスキップする場合
```

使用例（ユースケース）:

```
@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md

ユースケース books のコードを生成してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api
* target: usecases/books
```

* 同様に `target: usecases/auth`, `target: usecases/stocks`, `target: usecases/category`, `target: usecases/publisher`, `target: usecases/workflow` 等で各ユースケースを実装

**SPEC変更時**: SPEC を編集したうえで、本インストラクションで target を指定して再実行すれば差分が反映されます。

---

#### ステップ3: 単体テスト実行評価

単体テストを実行してカバレッジを分析し、品質を検証します。

```
@agent_skills/jakarta-ee-api-agile/instructions/unit_test_execution.md

単体テストを実行してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api
* target: common   # または usecases/books 等、対象ユースケース
* build_script_path: ./build.gradle  # マルチプロジェクト構成用（リポジトリルートのbuild.gradleを指定）
```

**マルチプロジェクト構成について:**
* このプロジェクトは、リポジトリルートの `build.gradle` を使用するマルチプロジェクト構成です
* `build_script_path` パラメータでリポジトリルートの `build.gradle` ファイルのパスを指定します（例: "./build.gradle"）
* 指定されたパスからディレクトリ部分が抽出され、そのディレクトリでGradleタスクが実行されます
* 未指定の場合はデフォルトで `project_root` が使用されますが、マルチプロジェクト構成ではルートの build.gradle を使うため、明示的に指定することを推奨します

AIが：
1. 🧪 テスト実行（gradle test jacocoTestReport）
2. 📊 テスト結果とカバレッジ分析
3. 🔍 問題の分類（テスト失敗、必要な振る舞い、デッドコード）
4. 📋 フィードバックレポート生成
5. 💬 ユーザーに推奨アクションを提示

重要：
* 問題を発見してもユーザー確認なしに修正しない
* カバレッジ不足やデッドコードを具体的に提案
* 必要に応じてコード生成（または 業務共通SPEC/ユースケースSPEC の見直し）に戻ってループ

🔄 フィードバックループ:
```
コード生成 → テスト実行評価
    ↑              ↓
    └── フィードバック ←┘
```

---

#### ステップ4: 結合テスト生成（単体テスト完了後）

単体テスト完了後に、結合テスト（Integration Test）を生成します。

```
@agent_skills/jakarta-ee-api-agile/instructions/it_generation.md

結合テストを生成してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api
* spec_directory: projects/sdd-agile/bookstore/back-office-api/specs/baseline
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

#### ステップ5: E2Eテスト生成（実装完了後）

全機能実装完了後に、E2Eテスト（End-to-End Test）を生成します。

```
@agent_skills/jakarta-ee-api-agile/instructions/e2e_test_generation.md

E2Eテストを生成してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/back-office-api
* spec_directory: projects/sdd-agile/bookstore/back-office-api/specs/baseline
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

### 🔄 SPEC変更時

業務共通SPEC またはユースケースの仕様を変更した場合、`@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md` で target を指定して再実行すれば、差分が反映されます。

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

### SPEC構成（アジャイル・本プロジェクトの実際の構造）

```
specs/
└── baseline/
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

* ウォーターフォール版の `basic_design/`、`requirements/`、`detailed_design/`、`tasks/` は本フローでは使用しません。

### プロジェクト全体

```
back-office-api/
├── specs/                          # 上記の通り
├── sql/hsqldb/                     # DDL・DML（setupHsqldb で使用）
├── src/
│   ├── main/java/
│   ├── main/resources/
│   └── main/webapp/
├── images/covers/                  # 書籍表紙画像
├── test_script/                    # APIテストスクリプト
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

仕様駆動開発により何度でも再実装できます。詳細は [ルートREADMEのSDDクリーンアップ節](../../../README.md#仕様駆動開発sddプロジェクトの成果物クリーンアップ) を参照してください。

```bash
# 本番コード・単体テストコードを削除（src/main/, src/test/, build/）。common/, usecases/ は保護されます
./gradlew :back-office-api-sdd-agile:cleanCode
```

* sdd-agile ではタスク分解・詳細設計を行わないため、`cleanCode` のみが対象です。
* 削除対象: 本番コード（src/main/）、単体テストコード（src/test/）、ビルド成果物（build/）。ディレクトリ構造は空で保持されます。
* 保護されるSPEC: `specs/baseline/common/`, `specs/baseline/usecases/`

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
