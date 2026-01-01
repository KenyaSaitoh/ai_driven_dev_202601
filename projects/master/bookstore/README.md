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
# プロジェクトルートで実行
# HSQLDBドライバをPayara Serverにインストール
./gradlew installHsqldbDriver
```

### ② MAC固有の作業（初回のみ実行）

```bash
# プロジェクトルートで実行
# 実行権限を付与
chmod +x gradlew
chmod +x payara6/bin/*
chmod +x tomee8/bin/*
chmod +x projects/master/accounting/accounting_etl/*.sh
chmod +x projects/sdd/accounting/accounting_etl_sdd/*.sh
```

> **Note**: このステップはmacOS/Linuxのみ必要です。Windowsでは不要です。

### ③ 研修開催につき初回に1回だけ実行

環境をクリーンな状態から開始する場合：

```bash
# プロジェクトルートで実行

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
# プロジェクトルートで実行
./gradlew :berry-books-api:setupHsqldb      # 注文管理テーブル
./gradlew :back-office-api:setupHsqldb      # 書籍・在庫テーブル
./gradlew :customer-hub-api:setupHsqldb     # 顧客テーブル
```

### ⑤ ビルド

```bash
# プロジェクトルートで実行
./gradlew :berry-books-api:war
./gradlew :back-office-api:war
./gradlew :customer-hub-api:war
```

### ⑥ デプロイ

```bash
# プロジェクトルートで実行
./gradlew :berry-books-api:deploy
./gradlew :back-office-api:deploy
./gradlew :customer-hub-api:deploy
```

### ⑦ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
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
# プロジェクトルートで実行
tail -f -n 50 payara6/glassfish/domains/domain1/logs/server.log
```

> **Note**: Windowsでは**Git Bash**を使用してください。

これにより、API呼び出しのログ、エラー、警告などをリアルタイムで確認できます。

## 🧹 クリーンアップ

### プロジェクトを終了するとき

作業終了時にアプリケーションをアンデプロイします：

```bash
# プロジェクトルートで実行
./gradlew :berry-books-api:undeploy
./gradlew :back-office-api:undeploy
./gradlew :customer-hub-api:undeploy
```

### 研修開催につき最後に1回だけ実行（環境全体のクリーンアップ）

研修終了時に環境全体をクリーンアップする場合：

```bash
# プロジェクトルートで実行

# すべてのアプリケーションをアンデプロイし、データソースを削除
./gradlew cleanupAll

# サーバーを停止
./gradlew stopPayara
./gradlew stopHsqldb
```

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
| **デスクトップ** | Java Swing | - |

## 📚 関連ドキュメント

- [プロジェクトルートのREADME.md](../../../README.md) - 全体的なセットアップと環境設定
- [各プロジェクトのREADME.md](.) - プロジェクト固有の詳細情報
- [仕様駆動開発版](../../sdd/bookstore/) - SDD研修用プロジェクト
- [Vibe Coding版](../../vibe/bookstore/) - Vibe Coding研修用プロジェクト

