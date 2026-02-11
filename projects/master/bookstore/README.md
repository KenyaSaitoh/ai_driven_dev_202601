# Bookstore Projects - Jakarta EE 10 REST API

## 📖 概要

Jakarta EE 10とPayara Serverを使用したオンライン書店「Berry Books」のフルスタックアプリケーション。マイクロサービス構成（3つのREST API）とReact SPAによるモダンなWebアプリケーションです。

## 📌 プロジェクト構成

### REST API（バックエンド）

| プロジェクト | 説明 | 主要機能 |
|------------|------|---------|
| **berry-books-api** | 注文管理API（BFF） | JWT認証、注文処理、書籍・在庫API（プロキシ） |
| **back-office-api** | 書籍・在庫管理API | 書籍マスター、カテゴリ・出版社管理、在庫管理 |
| **customer-hub-api** | 顧客管理API | 顧客CRUD、統計情報（注文件数・購入冊数） |

### SPA（フロントエンド - React + TypeScript）

| プロジェクト | 説明 | ポート |
|------------|------|-------|
| **berry-books-spa** | 注文管理フロントエンド | 5173 |
| **back-office-spa** | 書籍管理フロントエンド | 3001 |
| **customer-hub-spa** | 顧客管理フロントエンド | 3000 |

### Desktop

- **customer-hub-swing** - 顧客管理デスクトップアプリ（Java Swing）

## 🚀 クイックスタート

### 前提条件

- **JDK 21以上** / **Gradle 8.x以上** / **Node.js 18以上**
- **Payara Server 6** / **HSQLDB** (プロジェクトルートに配置済み)
- **Windows**: Git Bash必須（`./gradlew`コマンド実行用）

> **Note**: すべてのコマンドはプロジェクトルート（`ai_driven_dev_202601/`）で実行します。

### ⚡ 自動起動（推奨）

**フルスタック一括起動:**
```bash
# プロジェクトルートで実行
cd projects/master/bookstore
./start-bookstore-all.sh
```

自動実行内容：Payara/HSQLDB起動 → データソース設定 → API 3つのDB初期化・ビルド・デプロイ → SPA 3つの起動

**一括停止:**
```bash
cd projects/master/bookstore
./stop-bookstore-all.sh
```

### 手動セットアップ（初回のみ）

```bash
# 1. HSQLDBドライバインストール
./gradlew installHsqldbDriver

# 2. macOS/Linux: 実行権限付与
chmod +x gradlew payara6/bin/*

# 3. 環境初期化
./gradlew initPayaraDomainConfig
./gradlew startHsqldb
./gradlew startPayara
./gradlew setupDataSource

# 4. データベース初期化
./gradlew :berry-books-api:setupHsqldb
./gradlew :back-office-api:setupHsqldb
./gradlew :customer-hub-api:setupHsqldb

# 5. ビルド・デプロイ
./gradlew :berry-books-api:war :berry-books-api:deploy
./gradlew :back-office-api:war :back-office-api:deploy
./gradlew :customer-hub-api:war :customer-hub-api:deploy

# 6. SPA起動（各ディレクトリで実行）
cd projects/master/bookstore/berry-books-spa && npm install && npm run dev
cd projects/master/bookstore/back-office-spa && npm install && npm run dev
cd projects/master/bookstore/customer-hub-spa && npm install && npm run dev
```

## 🌐 アクセス情報

### SPA（フロントエンド）

| アプリ | URL | 説明 |
|-------|-----|------|
| Berry Books SPA | http://localhost:5173 | オンライン書店（注文管理） |
| Back Office SPA | http://localhost:3001 | 書籍管理（ワークフロー） |
| Customer Hub SPA | http://localhost:3000 | 顧客管理（統計表示） |

### REST API（バックエンド）

| API | ベースURL |
|-----|----------|
| berry-books-api | http://localhost:8080/berry-books-api/api |
| back-office-api | http://localhost:8080/back-office-api/api |
| customer-hub-api | http://localhost:8080/customer-hub-api/customers |

### テストアカウント

**Berry Books SPA:**
- メールアドレス: `alice@example.com` / パスワード: `password`

**Back Office SPA:**
- 社員コード: `EMP001` / パスワード: `password`

## 🧪 テスト

### Cucumber BDD結合テスト

ビジネスシナリオをGherkin形式で記述し、自動テストを実行します。

**テスト生成（Agent Skills使用）:**

```bash
# ステップ1: behaviors.mdからFeatureファイルを生成
@agent_skills/cucumber-test/instructions/generate_feature_from_behaviors.md

# ステップ2: Step DefinitionsとTest Runnerを生成
@agent_skills/cucumber-test/instructions/generate_cucumber_tests.md

パラメータ:
* project_path: projects/master/bookstore/berry-books-api
* package_root: pro.kensait.berrybooks
```

**テスト実行:**
```bash
# すべてのCucumberテストを実行
./gradlew :berry-books-api:integrationTest
./gradlew :back-office-api:integrationTest

# 特定のタグのみ実行
./gradlew :berry-books-api:integrationTest -Dcucumber.filter.tags="@order"
```

### ArchUnit アーキテクチャテスト

レイヤー依存関係、命名規則、アノテーションルールなどのアーキテクチャ制約を自動検証します。

**テスト生成（Agent Skills使用）:**

```bash
@agent_skills/archiunit-test/instructions/generate_archunit_tests.md

パラメータ:
* project_path: projects/master/bookstore/berry-books-api
* package_root: pro.kensait.berrybooks
```

**テスト実行:**
```bash
# すべてのArchiUnitテストを実行
./gradlew :berry-books-api:test --tests "*architecture.*"
./gradlew :back-office-api:test --tests "*architecture.*"

# 特定のルールのみ実行
./gradlew :berry-books-api:test --tests "*LayeredArchitectureTest"
./gradlew :berry-books-api:test --tests "*NamingConventionTest"
```

### Playwright E2Eテスト

Berry Books SPAのUIテストを実行します。Page Object Modelパターンを使用した保守性の高いテストコードです。

**テストシナリオ:**
- 基本フロー（ログイン → カート → 注文）
- 完全フロー（全画面検証）
- 新規顧客登録
- 書籍検索と注文
- カート操作

**テスト生成（Agent Skills使用）:**

```bash
@agent_skills/playwright-e2e-test/instructions/generate_playwright_tests.md

パラメータ:
* project_root: projects/master/bookstore/berry-books-spa
* instructions_file: projects/master/bookstore/berry-books-spa/playwright_berry-books.md
```

**テスト実行:**
```bash
cd projects/master/bookstore/berry-books-spa

# 依存関係とブラウザのインストール
npm install
npx playwright install

# すべてのテストを実行
npx playwright test

# UIモードで実行（デバッグ推奨）
npx playwright test --ui

# レポート表示
npx playwright show-report
```

> **Note**: テスト実行前にバックエンドAPI（`http://localhost:8080/berry-books-api`）とSPA（`http://localhost:5173`）を起動してください。

## 🗄️ データベース

### HSQLDB接続情報

- **データベース**: testdb / **ユーザー**: SA / **パスワード**: （空文字）
- **TCPサーバー**: localhost:9001 / **JNDI**: jdbc/HsqldbDS

### SQLクライアント接続

**Windows (Git Bash):**
```bash
java -cp "../../../hsqldb/lib/hsqldb.jar;../../../hsqldb/lib/sqltool.jar" \
  org.hsqldb.cmdline.SqlTool --rcFile ../../../hsqldb/sqltool.rc testdb
```

**macOS/Linux:**
```bash
java -cp "../../../hsqldb/lib/hsqldb.jar:../../../hsqldb/lib/sqltool.jar" \
  org.hsqldb.cmdline.SqlTool --rcFile ../../../hsqldb/sqltool.rc testdb
```

**基本コマンド:**
```sql
\dt                    -- テーブル一覧
\d PERSON              -- テーブル構造
SELECT * FROM PERSON;  -- データ確認
\q                     -- 終了
```

### データベースリセット

```bash
./gradlew stopHsqldb
rm -f hsqldb/data/testdb.*
./gradlew startHsqldb
./gradlew :berry-books-api:setupHsqldb :back-office-api:setupHsqldb :customer-hub-api:setupHsqldb
```

## 🗂️ プロジェクト詳細

各APIとSPAの詳細な仕様、データモデル、エンドポイント情報は以下を参照：

### アーキテクチャ

```
berry-books-spa → berry-books-api (BFF) → back-office-api / customer-hub-api
back-office-spa → back-office-api
customer-hub-spa → customer-hub-api
```

**BFFパターン:** `berry-books-api`がフロントエンドの単一エントリーポイントとして、内部マイクロサービスを統合します。

### 主要機能

**berry-books-api (BFF):**
- JWT認証（Cookie）、注文処理、書籍・在庫APIプロキシ
- エンドポイント: `/api/auth/*`, `/api/orders/*`, `/api/books/*`, `/api/categories/*`

**back-office-api:**
- 書籍CRUD、カテゴリ・出版社管理、在庫管理、書籍検索（JPQL/Criteria API）
- エンドポイント: `/api/books/*`, `/api/categories/*`, `/api/images/*`

**customer-hub-api:**
- 顧客CRUD、統計情報（注文件数・購入冊数）
- エンドポイント: `/customers/*`

## 🔧 使用技術

**バックエンド:** Jakarta EE 10、Payara Server 6、JAX-RS 3.1、JPA 3.1、CDI 4.0、Bean Validation 3.0、HSQLDB 2.7.x、JWT (jjwt 0.12.6)、BCrypt

**フロントエンド:** React 18、TypeScript 5、Vite 5、React Router v6、Tailwind CSS、Axios

**テスト:** JUnit 5、Mockito、ArchiUnit、Cucumber、Weld SE、Playwright、JaCoCo

**ビルド:** Gradle 8.x+、npm 9+

## 📚 関連リンク

- [プロジェクトルートREADME](../../../README.md) - 全体設定
- [Jakarta EE 10 Platform](https://jakarta.ee/specifications/platform/10/)
- [Playwright公式ドキュメント](https://playwright.dev/)
- [仕様駆動開発版](../../sdd-wf/bookstore/) - SDD研修用
- [Vibe Coding版](../../vibe/bookstore/) - Vibe Coding研修用

## 📊 その他

**ログ監視:**
```bash
tail -f -n 50 payara6/glassfish/domains/domain1/logs/server.log
```

**一括停止:**
```bash
cd projects/master/bookstore
./stop-bookstore-all.sh
```

