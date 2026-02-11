# Bookstore Projects - Jakarta EE 10 REST API

## 📖 概要

書店ドメインの完成版プロジェクト集です。Jakarta EE 10とPayara Serverを使用したREST API開発を学習できます。

## 📌 プロジェクト一覧

### API（バックエンド）

1. **berry-books-api** - 注文管理REST API
   - 書籍の注文処理、認証・認可、JWT認証
   - [詳細はこちら](berry-books-api/README.md)
   
2. **back-office-api** - 書籍・在庫管理REST API
   - 書籍マスター、出版社・カテゴリ管理、在庫管理
   - [詳細はこちら](back-office-api/README.md)
   
3. **customer-hub-api** - 顧客管理REST API
   - 顧客情報のCRUD操作、シンプルなREST API実装
   - [詳細はこちら](customer-hub-api/README.md)

### SPA（フロントエンド）

4. **berry-books-spa** - Berry Books SPA (React)
   - Berry Books APIのフロントエンド
   - [詳細はこちら](berry-books-spa/README.md)

5. **back-office-spa** - Back Office SPA (React)
   - Back Office APIのフロントエンド
   - [詳細はこちら](back-office-spa/README.md)

6. **customer-hub-spa** - Customer Hub SPA (React)
   - Customer Hub APIのフロントエンド
   - [詳細はこちら](customer-hub-spa/README.md)

### Swing（デスクトップアプリケーション）

7. **customer-hub-swing** - Customer Hub Swing
   - Customer Hub APIのSwingクライアント
   - [詳細はこちら](customer-hub-swing/README.md)

## 🚀 クイックスタート

### ⚡ フルスタック自動起動（推奨）

全アプリケーション（バックエンドAPI 3つ + フロントエンドSPA 3つ）を一括起動できる自動化スクリプトを用意しています。

```bash
# このディレクトリ（projects/master/bookstore）から実行
./start-bookstore-all.sh
```

このスクリプトは以下を自動実行します：
1. Payara Server の初期化と起動
2. HSQLDB サーバーの起動
3. データソースのセットアップ
4. 3つのJakarta EE APIのDB初期化、WAR化、デプロイ
5. 3つのReact SPAの依存関係インストールと起動

### 📦 その他の便利スクリプト

```bash
# SPAのみを再起動
./start-bookstore-spa-only.sh

# SPAのみを停止
./stop-bookstore-spa-only.sh

# すべてを停止（SPA、API、Payara、HSQLDB）
./stop-bookstore-all.sh

# データベースのみ起動/再起動
./start-database.sh
```

### 前提条件

- **JDK 21以上**
- **Gradle 8.x以上**
- **Payara Server 6** (プロジェクトルートの`payara6/`に配置済み)
- **HSQLDB** (プロジェクトルートの`hsqldb/`に配置済み)
- **Windows**: Git Bash（Gradleコマンド実行用）

> **Note**: すべてのコマンドはbash形式（`./gradlew`）です。WindowsではGit Bashを使用してください。
> 
> コマンドはすべてプロジェクトルート（`ai_driven_dev_202601/`）で実行します。

### ① 研修環境セットアップ後に1回だけ実行

```bash
# HSQLDBドライバをPayara Serverにインストール
./gradlew installHsqldbDriver
```

### ② MAC固有の作業（初回のみ実行）

```bash
# 実行権限を付与
chmod +x gradlew
chmod +x payara6/bin/*
chmod +x projects/master/accounting/accounting_etl/*.sh
chmod +x projects/sdd-wf/accounting/accounting_etl_sdd/*.sh
```

> **Note**: このステップはmacOS/Linuxのみ必要です。Windowsでは不要です。

### ③ 研修開催につき初回に1回だけ実行

環境をクリーンな状態から開始する場合：

```bash
# 1. Payara Serverのdomain.xmlを初期化（クリーンな状態にリセット）
./gradlew initPayaraDomainConfig

# 2. HSQLDBサーバーを起動
./gradlew startHsqldb

# 3. Payara Serverを起動
./gradlew startPayara

# 4. データソースをセットアップ（既存削除→コネクションプール作成→データソース作成）
./gradlew setupDataSource

# ※ setupDataSourceは以下を自動実行します：
#   1. deleteDataSource（既存のデータソースを削除）
#   2. deleteConnectionPool（既存の接続プールを削除）
#   3. createConnectionPool（新しい接続プールを作成）
#   4. createDataSource（新しいデータソースを作成）
```

### ④ データベースセットアップ

各プロジェクトを開始する前に、データベーステーブルとデータを作成します：

```bash
./gradlew :berry-books-api:setupHsqldb      # 注文管理テーブル
./gradlew :back-office-api:setupHsqldb      # 書籍・在庫テーブル
./gradlew :customer-hub-api:setupHsqldb     # 顧客テーブル
```

### ⑤ ビルド

```bash
./gradlew :berry-books-api:war
./gradlew :back-office-api:war
./gradlew :customer-hub-api:war
```

### ⑥ デプロイ

```bash
./gradlew :berry-books-api:deploy
./gradlew :back-office-api:deploy
./gradlew :customer-hub-api:deploy
```

### ⑦ アプリケーション作成・更新のたびに実行

```bash
# 例：berry-books-apiの場合
./gradlew :berry-books-api:war
./gradlew :berry-books-api:deploy

# 例：back-office-apiの場合
./gradlew :back-office-api:war
./gradlew :back-office-api:deploy

# 例：customer-hub-apiの場合
./gradlew :customer-hub-api:war
./gradlew :customer-hub-api:deploy
```

## 🌐 アプリケーションへのアクセス

各プロジェクトのAPIエンドポイント：

```
# Berry Books API（注文管理）
http://localhost:8080/berry-books-api/api/books

# Back Office API（書籍・在庫管理）
http://localhost:8080/back-office-api/api/books

# Customer Hub API（顧客管理）
http://localhost:8080/customer-hub-api/customers/1
```

### ログイン情報

- **メールアドレス**: alice@gmail.com
- **パスワード**: password

## 📊 ログをリアルタイム監視

別のターミナルでPayara Serverのログをリアルタイムに監視できます：

```bash
tail -f -n 50 payara6/glassfish/domains/domain1/logs/server.log
```

> **Note**: Windowsでは**Git Bash**を使用してください。

これにより、API呼び出しのログ、エラー、警告などをリアルタイムで確認できます。

## 🧹 クリーンアップ

### ⚡ 一括停止（推奨）

すべてのアプリケーション（SPA、API、Payara、HSQLDB）を一括停止できる自動化スクリプトを用意しています：

```bash
# このディレクトリ（projects/master/bookstore）から実行
./stop-bookstore-all.sh
```

このスクリプトは以下を自動実行します：
1. React SPA（3つ）を停止
2. Jakarta EE API（3つ）をアンデプロイ
3. Payara Serverを停止
4. HSQLDBサーバーを停止

### 個別に停止する場合

作業終了時にアプリケーションを個別にアンデプロイします：

```bash
./gradlew :berry-books-api:undeploy
./gradlew :back-office-api:undeploy
./gradlew :customer-hub-api:undeploy
```

### プロジェクトを終了するときに実行（環境全体のクリーンアップ）

プロジェクト終了時に環境全体をクリーンアップする場合：

```bash
# すべてのアプリケーションをアンデプロイし、データソースを削除
./gradlew cleanupAll

# サーバーを停止
./gradlew stopPayara
./gradlew stopHsqldb
```

## 🧪 E2Eテスト（Playwright）

### テストシナリオ定義書

Berry Books SPAのE2Eテストシナリオ定義書が用意されています：

- [berry-books-spa/playwright_berry-books.md](berry-books-spa/playwright_berry-books.md)

テストシナリオ定義書は、人が読みやすい日本語で記述されており、以下の5つのシナリオをカバーしています：

1. **基本フロー** - ログインから注文までの基本操作
2. **完全フロー** - 全画面遷移と検証を含む完全なE2Eテスト
3. **新規顧客登録** - 新規ユーザー登録のフロー
4. **書籍検索と注文** - カテゴリ検索から注文までのフロー
5. **カート操作** - カート内の商品削除操作

### Playwrightテストコードの自動生成

Agent Skillsを使用して、テストシナリオ定義書からPlaywrightテストコードを自動生成できます：

```
@agent_skills/playwright-e2e-test/instructions/generate_playwright_tests.md

テストシナリオ定義書からPlaywrightテストコードを生成してください

パラメータ
* project_root: projects/master/bookstore/berry-books-spa
* instructions_file: projects/master/bookstore/berry-books-spa/playwright_berry-books.md
```

AIが自動で以下を実行します：
1. テストシナリオ定義書を読み込み
2. アプリケーションコードを解析してセレクタを自動推論
3. Page Object Model（POM）クラスを生成
4. TypeScriptテストコードを生成
5. 設定ファイル（`playwright.config.ts`）を生成

### テストの実行

生成されたテストを実行するには：

```bash
# berry-books-spaディレクトリで実行

# 依存関係のインストール
npm install

# Playwrightのインストール
npx playwright install

# 全テストを実行
npx playwright test

# UIモードで実行（デバッグ用）
npx playwright test --ui

# レポートを表示
npx playwright show-report
```

> **Note**: テスト実行前に、Berry Books API（`http://localhost:8080/berry-books-api`）とSPA（`http://localhost:5173`）が起動している必要があります。

### 詳細情報

- **[Berry Books SPA README](berry-books-spa/README.md)** - Playwrightテスト実行方法、トラブルシューティング、CI/CD統合など
- [Playwright Agent Skills](../../../agent_skills/playwright-e2e-test/README.md) - テストコード自動生成の詳細
- [Playwrightベストプラクティス](../../../agent_skills/playwright-e2e-test/principles/playwright_best_practices.md)

---

## 📋 各プロジェクトの詳細

各プロジェクトの詳細な情報は、それぞれのREADME.mdを参照してください：

- [berry-books-api/README.md](berry-books-api/README.md)
- [back-office-api/README.md](back-office-api/README.md)
- [customer-hub-api/README.md](customer-hub-api/README.md)
- [berry-books-spa/README.md](berry-books-spa/README.md)
- [back-office-spa/README.md](back-office-spa/README.md)
- [customer-hub-spa/README.md](customer-hub-spa/README.md)
- [customer-hub-swing/README.md](customer-hub-swing/README.md)

## 🗄️ データベース設定

### ⚡ データベース起動/再起動

データベース（HSQLDB）のみを起動または再起動するスクリプトを用意しています：

```bash
# このディレクトリ（projects/master/bookstore）から実行
./start-database.sh
```

このスクリプトは以下を実行します：
- HSQLDBが起動中の場合は再起動
- HSQLDBが停止中の場合は起動
- 接続確認と詳細情報を表示

### HSQLDB接続情報

- **データベース名**: testdb
- **ユーザー名**: SA
- **パスワード**: （空文字）
- **TCPサーバー**: localhost:9001
- **JNDI名**: jdbc/HsqldbDS

接続設定は`../../../env-conf.gradle`で管理されています。

### ターミナルからHSQLDBへ接続（SQLクライアント）

コマンドラインからSQLを実行する場合は、SqlToolを使用します：

**Windows (Git Bash) の場合:**
```bash
java -cp "../../../hsqldb/lib/hsqldb.jar;../../../hsqldb/lib/sqltool.jar" org.hsqldb.cmdline.SqlTool --rcFile ../../../hsqldb/sqltool.rc testdb
```

**macOS / Linux の場合:**
```bash
java -cp "../../../hsqldb/lib/hsqldb.jar:../../../hsqldb/lib/sqltool.jar" org.hsqldb.cmdline.SqlTool --rcFile ../../../hsqldb/sqltool.rc testdb
```

> **重要**: 
> - **Windows (Git Bash)**: クラスパス区切りは `;`（Javaに渡す引数はWindowsネイティブ形式）
> - **macOS/Linux**: クラスパス区切りは `:`

接続設定は`../../../hsqldb/sqltool.rc`に記述されています。

**SQLの実行例:**

```sql
-- テーブル一覧を表示
\dt

-- テーブルの構造を確認
\d PERSON

-- データを確認
SELECT * FROM PERSON;

-- 終了
\q
```

## 🔧 使用技術

| カテゴリ | 技術 | バージョン |
|---------|------|----------|
| **Java** | JDK | 21+ |
| **アプリケーションサーバー** | Payara Server | 6 |
| **Jakarta EE** | Platform | 10.0 |
| **JAX-RS** | Jakarta RESTful Web Services | 3.1 |
| **CDI** | Jakarta CDI | 4.0 |
| **JPA** | Jakarta Persistence | 3.1 |
| **Bean Validation** | Jakarta Bean Validation | 3.0 |
| **データベース** | HSQLDB | 2.7.x |
| **ビルドツール** | Gradle | 8.x+ |
| **フロントエンド** | React + TypeScript | - |
| **E2Eテスト** | Playwright | - |
| **デスクトップ** | Java Swing | - |

## 📚 関連ドキュメント

- [プロジェクトルートのREADME.md](../../../README.md) - 全体的なセットアップと環境設定
- [各プロジェクトのREADME.md](.) - プロジェクト固有の詳細情報
- [仕様駆動開発版](../../sdd-wf/bookstore/) - SDD研修用プロジェクト
- [Vibe Coding版](../../vibe/bookstore/) - Vibe Coding研修用プロジェクト

