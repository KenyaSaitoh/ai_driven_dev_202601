# Jakarta EE 10 Web Projects - Payara Server Edition

## 📖 概要

Jakarta EE 10とPayara Serverを使用したWebアプリケーションの学習プロジェクト集です。
Servlet/JSP、JSF、CDI、JAX-RSを段階的に学習できます。

## 📁 プロジェクト構成

このリポジトリは複数の技術スタックを含むマルチプロジェクト構成です：

```
jee_micro_vsc/
├── projects/
│   ├── java/                           # Jakarta EE (Java) プロジェクト
│   │   ├── berry-books-mvc/            # Berry Books Webアプリケーション（MVC）【完成版】
│   │   ├── berry-books-mvc-sdd/        # Berry Books Webアプリケーション（仕様駆動開発）【研修用】
│   │   ├── berry-books-rest/           # Berry Books REST API【完成版】
│   │   ├── jsf_person/                 # Person管理（JSF + JPA CRUD）【完成版】
│   │   ├── struts_person/              # Person管理（Struts 1.3 + EJB）【完成版】
│   │   └── struts-to-jsf-person-sdd/   # StrutsからJakarta EEへの移行プロジェクト【研修用】
│   ├── python/                         # Pythonプロジェクト
│   │   ├── accounting_etl/             # ERP会計統合ETL【完成版】
│   │   └── accounting_etl_sdd/         # ERP会計統合ETL【研修用】
│   └── react/                          # Reactプロジェクト
│       ├── berry-books-frontend/       # Berry Books 管理者画面【完成版】
│       └── berry-books-frontend-vibe/  # Berry Books 管理者画面【研修用】
│
├── payara6/                            # Payara Server 6
├── hsqldb/                             # HSQLDB Database Server
├── tomee8/                             # Apache TomEE 8 (Struts用)
│
├── build.gradle                        # Javaプロジェクト用ビルド設定
├── settings.gradle                     # Gradleマルチプロジェクト設定
└── env-conf.gradle                     # 環境設定
```

## 🚀 セットアップとコマンド実行ガイド

### 前提条件

- **JDK 21以上**
- **Gradle 8.x以上**
- **Payara Server 6** (プロジェクトルートの`payara6/`に配置済み)
- **HSQLDB** (プロジェクトルートの`hsqldb/`に配置済み)
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
chmod +x projects/python/accounting_etl/*.sh
chmod +x projects/python/accounting_etl_sdd/*.sh
```

> **Note**: このステップはmacOS/Linuxのみ必要です。Windowsでは不要です。

### ③ 研修開催につき初回に1回だけ実行

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

### ④ 研修開催につき最後に1回だけ実行（CleanUp）

```bash
# すべてのアプリケーションをアンデプロイし、データソースを削除
./gradlew cleanupAll

# サーバーを停止
./gradlew stopPayara
./gradlew stopHsqldb
```

### ⑤ プロジェクトを開始するときに1回だけ実行

```bash
# プロジェクトのデータベーステーブルとデータを作成
# 例：berry-books-mvc、berry-books-mvc-sdd、berry-books-rest共通
./gradlew :projects:java:berry-books-mvc:setupHsqldb

# プロジェクトをビルド
./gradlew :projects:java:berry-books-mvc:war
# または
./gradlew :projects:java:berry-books-rest:war

# プロジェクトをデプロイ
./gradlew :projects:java:berry-books-mvc:deploy
# または
./gradlew :projects:java:berry-books-rest:deploy
```

### ⑥ プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# プロジェクトをアンデプロイ
# 例：berry-books-mvcの場合
./gradlew :projects:java:berry-books-mvc:undeploy
# 例：berry-books-restの場合
./gradlew :projects:java:berry-books-rest:undeploy
```

### ⑦ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
# 例：berry-books-mvcの場合
./gradlew :projects:java:berry-books-mvc:war
./gradlew :projects:java:berry-books-mvc:deploy
# 例：berry-books-restの場合
./gradlew :projects:java:berry-books-rest:war
./gradlew :projects:java:berry-books-rest:deploy
```

## 🧹 仕様駆動開発（SDD）プロジェクトの成果物クリーンアップ

**berry-books-mvc-sdd** プロジェクトは、仕様駆動開発により何度でも再実装できます。

### 成果物をクリーンアップ（ディレクトリ構造は保持）

```bash
# berry-books-mvc-sddの成果物をクリーンアップ
./gradlew :projects:java:berry-books-mvc-sdd:cleanSddArtifacts
```

**このタスクが削除するもの:**
- `src/` 配下のすべてのファイル（Java、XHTML、CSS、設定ファイルなど）
- `sql/hsqldb/` 配下のすべてのSQLファイル

**このタスクが保持するもの:**
- 以下のディレクトリ構造（空のディレクトリも含む）：
  - `src/main/java`
  - `src/main/resources/META-INF`
  - `src/main/webapp/resources`
  - `src/main/webapp/WEB-INF`
  - `src/test/java`
  - `src/test/resources`
  - `sql/hsqldb`
- `specs/` フォルダ（SPEC）
- `instructions/` フォルダ（インストラクション）
- `memory/` フォルダ（憲章）
- その他のプロジェクト設定ファイル

**クリーンアップ後の再実装:**
1. `@instructions/tasks.md` を使ってタスクリストを生成
2. `@instructions/implement.md` を使ってタスクに従って実装

詳細は `projects/java/berry-books-mvc-sdd/README.md` を参照してください。

---

## 🌐 アプリケーションへのログイン

プロジェクトごとのアクセスURL例：
```
http://localhost:8080/berry-books-mvc
http://localhost:8080/berry-books-rest/customers/1
```

### ログイン情報

- **メールアドレス**: alice@gmail.com
- **パスワード**: password

## 📊 ログをリアルタイム監視（別のターミナル）

```bash
tail -f -n 50 payara6/glassfish/domains/domain1/logs/server.log
```

> **Note**: Windowsでは**Git Bash**を使用してください。

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
| `cleanSddArtifacts` | 成果物をクリーンアップ（berry-books-mvc-sdd専用、ディレクトリ構造は保持） |

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
# berry-books-mvcのspecディレクトリを対象にする場合
./gradlew exploreExcelFiles -PtargetDir=projects/java/berry-books-mvc/spec
```

**処理内容:**

1. 指定ディレクトリを再帰的に検索し、すべての`.xlsx`ファイルを検出
2. 各Excelファイルを`.zip`形式に変換
3. タイムスタンプ付きフォルダ（`yyyyMMdd_HHmmss`形式）に展開
4. 展開後、一時的な`.zip`ファイルは自動削除

**出力例:**

```
projects/java/berry-books-mvc/spec/
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

**Windows PowerShell / CMD の場合:**
```powershell
java -cp "hsqldb\lib\hsqldb.jar;hsqldb\lib\sqltool.jar" org.hsqldb.cmdline.SqlTool --rcFile hsqldb\sqltool.rc testdb
```

**Git Bash / macOS / Linux の場合:**
```bash
java -cp "hsqldb/lib/hsqldb.jar:hsqldb/lib/sqltool.jar" org.hsqldb.cmdline.SqlTool --rcFile hsqldb/sqltool.rc testdb
```

> **重要**: 
> - **PowerShell/CMD**: クラスパス区切りは `;`、パス区切りは `\`
> - **Git Bash/Unix**: クラスパス区切りは `:`、パス区切りは `/`

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

```bash
# すべてのアプリ、データソース、コネクションプールを削除
./gradlew cleanupAll

# サーバーを停止
./gradlew stopPayara
./gradlew stopHsqldb
```

## 🔧 使用技術

| カテゴリ | 技術 | バージョン |
|---------|------|----------|
| **Java** | JDK | 21+ |
| **アプリケーションサーバー** | Payara Server | 6 |
| **Jakarta EE** | Platform | 10.0 |
| **Servlet** | Jakarta Servlet | 6.0 |
| **JSP** | Jakarta Server Pages | 3.1 |
| **JSF** | Jakarta Faces | 4.0 |
| **CDI** | Jakarta CDI | 4.0 |
| **JPA** | Jakarta Persistence | 3.1 |
| **JAX-RS** | Jakarta RESTful Web Services | 3.1 |
| **JSTL** | Jakarta Standard Tag Library | 3.0 |
| **データベース** | HSQLDB | 2.7.x |
| **ビルドツール** | Gradle | 8.x+ |

## 📚 ドキュメント

- **[設定ファイル](env-conf.gradle)** - Payara ServerとHSQLDB Database環境設定
- **[domain.xml.template](payara6/glassfish/domains/domain1/config/domain.xml.template)** - Payara Serverのクリーンな初期設定（Git管理対象）
- **[server.xml.template](tomee8/conf/server.xml.template)** - TomEE 8のクリーンな初期設定（Git管理対象）
- **各プロジェクトのREADME.md** - プロジェクト固有の詳細情報

### 設定ファイルのテンプレート管理について

#### Payara Server - domain.xml

- **`domain.xml.template`**: Git管理対象の初期設定ファイル（デプロイ情報・データソース設定なし）
- **`domain.xml`**: 実行時に使用される設定ファイル（Git管理対象外、実行時に動的に変更される）
- 研修開催時に`initPayaraDomainConfig`タスクでテンプレートから初期化される

#### TomEE 8 - server.xml

- **`server.xml.template`**: Git管理対象の初期設定ファイル（デフォルトポート8080）
- **`server.xml`**: 実行時に使用される設定ファイル（Git管理対象外、`configureTomee8Ports`で動的に変更される）
- 研修開催時に`initTomee8Config`タスクでテンプレートから初期化される
- **`tomee.xml`**: データソース設定（Git管理対象、手動で設定済み）

## 🐛 トラブルシューティング

### Payara Serverが起動しない

Payara Serverのドメインステータスを確認：
```bash
./gradlew statusPayara
```

既存のドメインをクリーンアップして再起動：
```bash
./gradlew stopPayara
./gradlew startPayara
```

プロセスが残っている場合（緊急時）：
```bash
# 全てのJavaプロセスを強制終了（Gradleも含む）
./gradlew killAllJava
```

### データベース接続エラー

1. HSQLDB Databaseサーバーが起動していることを確認：
```bash
./gradlew startHsqldb
```

2. データソースがセットアップされていることを確認：
```bash
./gradlew setupDataSource
./gradlew pingConnectionPool
```

3. `env-conf.gradle`の接続情報を確認

### デプロイエラー

アプリケーションをアンデプロイしてから再デプロイ：
```bash
# berry-books-mvcの場合
./gradlew :projects:java:berry-books-mvc:undeploy
./gradlew :projects:java:berry-books-mvc:deploy
```

### ビルドエラー

クリーンビルドを実行：
```bash
./gradlew clean build
```
