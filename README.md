# Jakarta EE 10 Web Projects - Payara Server Edition

## 📖 概要

Jakarta EE 10とReactを使用したフルスタックWebアプリケーションの学習プロジェクト集です。

- **バックエンド**: Jakarta EE 10（JAX-RS, CDI, JPA）+ Payara Server
- **フロントエンド**: React + TypeScript + Vite
- **レガシー**: Servlet/JSP、JSF（段階的学習用）

### 📌 代表プロジェクト（クイックスタート推奨）

**Bookstoreドメイン**のフルスタックプロジェクトから始めることをお勧めします：
   
- **REST API**（バックエンド）: `berry-books-api`、`back-office-api`、`customer-hub-api`
- **SPA**（フロントエンド）: `berry-books-spa`、`back-office-spa`、`customer-hub-spa`（master/bookstoreのみ）
- **Desktop**（Swing）: `customer-hub-swing`
   
詳細は [projects/master/bookstore/README.md](projects/master/bookstore/README.md) を参照してください。

## 📁 プロジェクト構成

このリポジトリは複数の技術スタックを含むマルチプロジェクト構成です。
プロジェクトは以下の2つのカテゴリに分類されています。

> **Note**: このREADMEでは、環境全体のセットアップと基本的なコマンドを説明します。個別のプロジェクトについては、各ドメインフォルダやプロジェクトのREADME.mdを参照してください（例：[projects/master/bookstore/README.md](projects/master/bookstore/README.md)）。

### プロジェクトカテゴリ

1. **master/** - 完成版プロジェクト（参照用）
   - 動作確認済みの完成版コード
   - 学習のリファレンス実装として利用

2. **sdd/** - 仕様駆動開発（Specification-Driven Development）プロジェクト（研修用）
   - 仕様書からコードを生成する手法を学習
   - AIを活用した段階的な実装プロセスを体験

### ディレクトリ構造

```
ai_driven_dev_202601/
├── projects/
│   ├── master/                                      # 完成版プロジェクト（参照用）
│   │   ├── bookstore/                               # 書店ドメイン
│   │   │   ├── berry-books-api/                     # REST API: 注文管理【完成版】
│   │   │   ├── berry-books-spa/                     # SPA: 注文管理フロントエンド (React+TS)【完成版】
│   │   │   ├── back-office-api/                     # REST API: 書籍・在庫管理【完成版】
│   │   │   ├── back-office-spa/                     # SPA: 書籍管理フロントエンド (React+TS)【完成版】
│   │   │   ├── customer-hub-api/                    # REST API: 顧客管理【完成版】
│   │   │   ├── customer-hub-spa/                    # SPA: 顧客管理フロントエンド (React+TS)【完成版】
│   │   │   └── customer-hub-swing/                  # Desktop: 顧客管理 (Swing)【完成版】
│   │   └── person/                                  # 人物管理ドメイン
│   │       ├── jsf-person/                          # Person管理（JSF + JPA）【完成版】
│   │       └── struts-person/                       # Person管理（Struts 1.3 + EJB）【完成版】
│   └── sdd/                                         # 仕様駆動開発（SDD）プロジェクト（研修用）
│       ├── bookstore/                               # 書店ドメイン
│       │   ├── berry-books-api-sdd/                 # REST API: 注文管理（SDD研修用）
│       │   └── back-office-api-sdd/                 # REST API: 書籍・在庫管理（SDD研修用）
│       └── person/                                  # 人物管理ドメイン
│           └── jsf-person-sdd/                      # Person管理（JSF + JPA, SDD研修用）
│
├── struts-to-jsf-person-sdd/                        # マイグレーションプロジェクト（Struts → JSF）
│
├── payara6/                                         # Payara Server 6
├── hsqldb/                                          # HSQLDB Database Server
├── tomee8/                                          # Apache TomEE 8 (Struts用)
│
├── build.gradle                                     # Javaプロジェクト用ビルド設定
├── settings.gradle                                  # Gradleマルチプロジェクト設定
└── env-conf.gradle                                  # 環境設定
```

## 🚀 セットアップとコマンド実行ガイド

### 前提条件

#### バックエンド開発

- **JDK 21以上**
- **Gradle 8.x以上**
- **Payara Server 6** (プロジェクトルートの`payara6/`に配置済み)
- **HSQLDB** (プロジェクトルートの`hsqldb/`に配置済み)

#### フロントエンド開発

- **Node.js 18以上**
- **npm または yarn**

#### 共通

- **Windows**: Git Bash（Gradleコマンド実行用）

> **Note**: すべてのコマンドはbash形式（`./gradlew`）です。WindowsではGit Bashを使用してください。

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
chmod +x tomee8/bin/*
chmod +x projects/master/accounting/accounting_etl/*.sh
chmod +x projects/sdd/accounting/accounting_etl_sdd/*.sh
```

> **Note**: このステップはmacOS/Linuxのみ必要です。Windowsでは不要です。

### ③ 研修開催につき初回に1回だけ実行（バックエンド環境）

```bash
# 1. Payara Serverのdomain.xmlを初期化
./gradlew initPayaraDomainConfig

# 2. HSQLDBサーバーを起動
./gradlew startHsqldb

# 3. Payara Serverを起動
./gradlew startPayara

# 4. データソースをセットアップ
./gradlew setupDataSource
```

### ④ 研修開催につき最後に1回だけ実行（CleanUp）

```bash
# すべてのアプリケーションをアンデプロイし、データソースを削除
./gradlew cleanupAll

# サーバーを停止
./gradlew stopPayara
./gradlew stopHsqldb
```

### ⑤ プロジェクトを開始する

#### バックエンド（REST API）

```bash
# データベーステーブルとデータを作成
./gradlew :<api-project-name>:setupHsqldb

# ビルド＆デプロイ
./gradlew :<api-project-name>:war
./gradlew :<api-project-name>:deploy
```

#### フロントエンド（SPA）

> **Note**: SPAプロジェクトは現在 `master/bookstore` ディレクトリにのみ存在します。

```bash
# SPAプロジェクトディレクトリに移動
cd projects/master/bookstore/<spa-project-name>

# 依存関係をインストール（初回のみ）
npm install

# 開発サーバーを起動
npm run dev
```

> **Note**: 具体的なコマンドについては、各ドメインのREADME.mdを参照してください（例：[projects/master/bookstore/README.md](projects/master/bookstore/README.md)）。

### ⑥ プロジェクトを終了する

**バックエンド（REST API）:**
```bash
./gradlew :<api-project-name>:undeploy
```

**フロントエンド（SPA）:**
```bash
# 開発サーバーのターミナルで Ctrl+C
```

### ⑦ アプリケーション更新時

**バックエンド（REST API）:**
```bash
./gradlew :<api-project-name>:war
./gradlew :<api-project-name>:deploy
```

**フロントエンド（SPA）:**
```bash
# ファイル保存で自動再読み込み（HMR）
# プロダクションビルドの場合: npm run build
```

## 🧹 仕様駆動開発（SDD）プロジェクトの成果物クリーンアップ

SDDプロジェクト（berry-books-api-sdd、back-office-api-sdd、jsf-person-sdd）は、仕様駆動開発により何度でも再実装できます。

成果物は3タイプに分けてクリーンアップできます。基本設計SPEC（specs/*/basic_design/）は絶対に削除されません。

### タイプ1: タスクファイルのみクリーンアップ

タスク分解の結果（tasks/）の中身を削除します。フォルダ自体は空のまま残ります。

```bash
./gradlew :berry-books-api-sdd:cleanTasks
./gradlew :back-office-api-sdd:cleanTasks
./gradlew :jsf-person-sdd:cleanTasks
```

削除されるもの: `tasks/`の中身（フォルダは空のまま残る）
保持されるもの: `specs/*/basic_design/`, `specs/*/detailed_design/`, `src/`

### タイプ2: 詳細設計SPECのクリーンアップ

詳細設計SPEC（specs/*/detailed_design/）の中身を削除します。フォルダ自体は空のまま残ります。

```bash
./gradlew :berry-books-api-sdd:cleanDetailedDesign
./gradlew :back-office-api-sdd:cleanDetailedDesign
./gradlew :jsf-person-sdd:cleanDetailedDesign
```

削除されるもの: `specs/*/detailed_design/`の中身（フォルダは空のまま残る）
保持されるもの: `specs/*/basic_design/`, `tasks/`, `src/`

### タイプ3: 実装コードのクリーンアップ

生成された実装コード（src/、build/）を削除します。

```bash
./gradlew :berry-books-api-sdd:cleanCode
./gradlew :back-office-api-sdd:cleanCode
./gradlew :jsf-person-sdd:cleanCode
```

削除されるもの: `src/main/`, `src/test/`, `build/`
保持されるもの: `specs/*/basic_design/`, `specs/*/detailed_design/`, `tasks/`

### すべてをクリーンアップ（基本設計SPECは保護）

タスク、詳細設計SPEC、実装コードをすべて削除します。

```bash
./gradlew :berry-books-api-sdd:cleanAllSdd
./gradlew :back-office-api-sdd:cleanAllSdd
./gradlew :jsf-person-sdd:cleanAllSdd
```

削除されるもの: `tasks/`の中身、`specs/*/detailed_design/`の中身、`src/`、`build/`
保持されるもの: `specs/*/basic_design/` （基本設計SPECは絶対に削除されない）
空のまま残るフォルダ: `tasks/`, `specs/*/detailed_design/`

詳細は各プロジェクトのREADME.mdを参照してください：
- [projects/sdd/bookstore/berry-books-api-sdd/README.md](projects/sdd/bookstore/berry-books-api-sdd/README.md)
- [projects/sdd/bookstore/back-office-api-sdd/README.md](projects/sdd/bookstore/back-office-api-sdd/README.md)
- [projects/sdd/person/jsf-person-sdd/README.md](projects/sdd/person/jsf-person-sdd/README.md)

---

## 🌐 アプリケーションへのアクセス

### バックエンド（REST API）

Payara Server上で動作（ポート: 8080）：

```
http://localhost:8080/<context-root>/api/...
```

### フロントエンド（SPA）

Vite開発サーバー上で動作：

```
http://localhost:5173   # berry-books-spa
http://localhost:5174   # back-office-spa
http://localhost:3000   # customer-hub-spa
```

> **Note**: SPAは開発サーバー起動時にポート番号が自動的に割り当てられます。ターミナルに表示されるURLを確認してください。

各プロジェクト固有のアクセスURL、ログイン情報などの詳細は、各ドメインのREADME.mdを参照してください（例：[projects/master/bookstore/README.md](projects/master/bookstore/README.md)）。

## 📊 ログをリアルタイム監視

### バックエンド（Payara Server）

別のターミナルでサーバーログを監視：

```bash
tail -f -n 50 payara6/glassfish/domains/domain1/logs/server.log
```

> **Note**: Windowsでは**Git Bash**を使用してください。

### フロントエンド（SPA）

開発サーバー起動時のターミナルに自動的にログが表示されます：

- HTTP リクエスト
- HMR（Hot Module Replacement）の更新
- ビルドエラー・警告
- TypeScriptの型チェックエラー

## 📋 Gradle タスク

### ビルドタスク

| タスク | 説明 |
|--------|------|
| `war` | WARファイルを作成 |
| `build` | プロジェクト全体をビルド |
| `clean` | ビルド成果物を削除 |

### Payara Serverタスク

| タスク | 説明 |
|--------|------|
| `startPayara` | Payara Serverを起動 |
| `stopPayara` | Payara Serverを停止 |
| `restartPayara` | Payara Serverを再起動 |
| `statusPayara` | Payara Serverのステータスを確認 |
| `killAllJava` | 全てのJavaプロセスを強制終了（緊急時用） |
| `initPayaraDomainConfig` | domain.xmlを初期状態にリセット（研修開催時に実行） |
| `setupDataSource` | HSQLDBデータソースをセットアップ（既存削除→作成を自動実行） |
| `installHsqldbDriver` | HSQLDBドライバをPayara Serverにコピー（初回のみ） |
| `createConnectionPool` | JDBCコネクションプールを作成 |
| `createDataSource` | JDBCリソース（データソース）を作成 |
| `pingConnectionPool` | コネクションプールをテスト |
| `cleanupAll` | すべてをクリーンアップ（全アプリアンデプロイ＋データソース削除） |
| `undeployAllApps` | デプロイ済みの全アプリケーションをアンデプロイ |
| `deleteDataSource` | JDBCリソース（データソース）を削除 |
| `deleteConnectionPool` | JDBCコネクションプールを削除 |

### デプロイタスク（各プロジェクト）

| タスク | 説明 |
|--------|------|
| `deploy` | WARファイルをPayara Serverにデプロイ |
| `undeploy` | アプリケーションをアンデプロイ |

### データベースタスク

| タスク | 説明 |
|--------|------|
| `startHsqldb` | HSQLDB Databaseサーバーを起動 |
| `stopHsqldb` | HSQLDB Databaseサーバーを停止 |
| `setupHsqldb` | プロジェクト固有の初期データをセットアップ（各プロジェクト） |

### 仕様駆動開発（SDD）タスク

| タスク | 説明 |
|--------|------|
| `cleanTasks` | タスクファイル（tasks/）を削除 |
| `cleanDetailedDesign` | 詳細設計SPEC（specs/*/detailed_design/）を削除 |
| `cleanCode` | 実装コード（src/、build/）を削除 |
| `cleanAllSdd` | すべてのSDD成果物を削除（基本設計SPECは保護） |
| `e2eTest` | E2Eテストを実行（@Tag("e2e")のテスト） |

注意: 基本設計SPEC（specs/*/basic_design/）は絶対に削除されません。

### ユーティリティタスク

| タスク | 説明 |
|--------|------|
| `exploreExcelFiles` | 指定ディレクトリ内のExcelファイル (.xlsx) を再帰的に検索し、ZIP形式で展開 |

#### exploreExcelFilesの使用方法

Excelファイル (.xlsx) を検索してZIP展開するタスクです。Excelファイルの内部構造を確認したい場合に便利です。

**基本的な使い方:**

```bash
# 指定ディレクトリ内のすべての.xlsxファイルを展開
./gradlew exploreExcelFiles -PtargetDir=<対象ディレクトリパス>
```

**実行例:**

```bash
# 特定のプロジェクトのspecディレクトリを対象にする場合
./gradlew exploreExcelFiles -PtargetDir=projects/master/<domain>/<project-name>/spec
```

**処理内容:**

1. 指定ディレクトリを再帰的に検索し、すべての`.xlsx`ファイルを検出
2. 各Excelファイルを`.zip`形式に変換
3. タイムスタンプ付きフォルダ（`yyyyMMdd_HHmmss`形式）に展開
4. 展開後、一時的な`.zip`ファイルは自動削除

**出力例:**

```
projects/master/<domain>/<project-name>/spec/
├── 設計書.xlsx
└── 20251029_143025/        # タイムスタンプフォルダ
    ├── [Content_Types].xml
    ├── _rels/
    ├── docProps/
    └── xl/
        ├── workbook.xml
        ├── worksheets/
        └── ...
```

> **Note**: Excelファイル (.xlsx) は内部的にZIP圧縮されたXMLファイルの集合体です。このタスクでその構造を確認できます。

## 🗄️ データベース設定

### HSQLDB接続情報

- **データベース名**: testdb
- **ユーザー名**: SA
- **パスワード**: （空文字）
- **TCPサーバー**: localhost:9001
- **JNDI名**: jdbc/HsqldbDS

接続設定は`env-conf.gradle`で管理されています。

### ターミナルからHSQLDBへ接続（SQLクライアント）

コマンドラインからSQLを実行する場合は、SqlToolを使用します：

**Windows (Git Bash) の場合:**
```bash
java -cp "hsqldb/lib/hsqldb.jar;hsqldb/lib/sqltool.jar" org.hsqldb.cmdline.SqlTool --rcFile hsqldb/sqltool.rc testdb
```

**macOS / Linux の場合:**
```bash
java -cp "hsqldb/lib/hsqldb.jar:hsqldb/lib/sqltool.jar" org.hsqldb.cmdline.SqlTool --rcFile hsqldb/sqltool.rc testdb
```

> **重要**: 
> - **Windows (Git Bash)**: クラスパス区切りは `;`（Javaに渡す引数はWindowsネイティブ形式）
> - **macOS/Linux**: クラスパス区切りは `:`

接続設定は`hsqldb/sqltool.rc`に記述されています。

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

## 🧹 環境のクリーンアップ

研修終了時に環境をクリーンアップするには：

### バックエンド

```bash
# すべてのアプリ、データソース、コネクションプールを削除
./gradlew cleanupAll

# サーバーを停止
./gradlew stopPayara
./gradlew stopHsqldb
```

### フロントエンド

```bash
# 開発サーバーを停止（各SPAのターミナルで Ctrl+C）

# （オプション）node_modulesを削除してディスク容量を節約
cd projects/master/<domain>/<spa-project-name>
rm -rf node_modules
```

## 🔧 使用技術

### バックエンド

| カテゴリ | 技術 | バージョン |
|---------|------|----------|
| **Java** | JDK | 21+ |
| **アプリケーションサーバー** | Payara Server | 6 |
| **Jakarta EE** | Platform | 10.0 |
| **JAX-RS** | Jakarta RESTful Web Services | 3.1 |
| **CDI** | Jakarta CDI | 4.0 |
| **JPA** | Jakarta Persistence | 3.1 |
| **データベース** | HSQLDB | 2.7.x |
| **ビルドツール** | Gradle | 8.x+ |

### フロントエンド

| カテゴリ | 技術 | バージョン |
|---------|------|----------|
| **JavaScript** | Node.js | 18+ |
| **フレームワーク** | React | 18.x |
| **言語** | TypeScript | 5.x |
| **ビルドツール** | Vite | 5.x |
| **スタイリング** | Tailwind CSS / CSS Modules | - |
| **パッケージマネージャー** | npm / yarn | - |

### レガシー技術（学習用）

| カテゴリ | 技術 | バージョン |
|---------|------|----------|
| **Servlet** | Jakarta Servlet | 6.0 |
| **JSP** | Jakarta Server Pages | 3.1 |
| **JSF** | Jakarta Faces | 4.0 |
| **JSTL** | Jakarta Standard Tag Library | 3.0 |
| **Swing** | Java Swing | - |

## 📚 ドキュメント

- **[設定ファイル](env-conf.gradle)** - Payara ServerとHSQLDB Database環境設定
- **[domain.xml.template](payara6/glassfish/domains/domain1/config/domain.xml.template)** - Payara Serverのクリーンな初期設定（Git管理対象）
- **各プロジェクトのREADME.md** - プロジェクト固有の詳細情報

### 設定ファイルのテンプレート管理について

- **`domain.xml.template`**: Git管理対象の初期設定ファイル（デプロイ情報・データソース設定なし）
- **`domain.xml`**: 実行時に使用される設定ファイル（Git管理対象外、実行時に動的に変更される）
- 研修開催時に`initPayaraDomainConfig`タスクでテンプレートから初期化される

## 🐛 トラブルシューティング

### バックエンド（REST API）

#### Payara Serverが起動しない

```bash
# ステータス確認
./gradlew statusPayara

# 再起動
./gradlew stopPayara
./gradlew startPayara

# プロセスが残っている場合（緊急時）
./gradlew killAllJava
```

#### データベース接続エラー

```bash
# HSQLDBサーバーを起動
./gradlew startHsqldb

# データソースを再セットアップ
./gradlew setupDataSource
./gradlew pingConnectionPool
```

#### デプロイエラー

```bash
# アンデプロイして再デプロイ
./gradlew :<api-project-name>:undeploy
./gradlew :<api-project-name>:deploy
```

#### ビルドエラー

```bash
./gradlew clean build
```

### フロントエンド（SPA）

#### 依存関係のインストールエラー

```bash
# node_modulesを削除して再インストール
rm -rf node_modules package-lock.json
npm install

# または yarn を使用
rm -rf node_modules yarn.lock
yarn install
```

#### 開発サーバーが起動しない

```bash
# ポートが使用中の場合、別のポートを指定
npm run dev -- --port 5175

# または、既存のプロセスを終了
# Windows: タスクマネージャーでNode.jsプロセスを終了
# macOS/Linux: lsof -ti:5173 | xargs kill
```

#### API接続エラー（CORS）

SPAからAPIへのアクセスでCORSエラーが発生する場合、バックエンドAPIが正しく起動しているか確認してください：

```bash
# APIのステータス確認
curl http://localhost:8080/<context-root>/api/...
```

#### ビルドエラー

```bash
# キャッシュをクリアして再ビルド
npm run build -- --force

# TypeScriptエラーの場合
npx tsc --noEmit
```
