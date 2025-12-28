# back-office-api プロジェクト

## 📖 概要

Jakarta EE 10とJAX-RS (Jakarta RESTful Web Services) 3.1を使用したオンライン書店「**Berry Books**」のREST APIアプリケーションです。
書籍・在庫管理機能をRESTful APIとして提供します。

> **Note:** このプロジェクトは`berry-books-api`プロジェクトと同じデータベースを共有します。

## 🔧 使用している技術

### 本番環境

- **Jakarta EE 10**
- **Payara Server 6**
- **JAX-RS (Jakarta RESTful Web Services) 3.1** - REST API
- **Jakarta Persistence (JPA) 3.1** - Hibernate実装
- **Jakarta Transactions (JTA)**
- **Jakarta CDI 4.0**
- **Jakarta Bean Validation 3.0**
- **HSQLDB 2.7.x**
- **JWT (JSON Web Token)** - jjwt 0.12.6
- **BCrypt** - パスワードハッシュ化

### テスト環境

- **JUnit 5** - テストフレームワーク
- **Mockito** - モックライブラリ
- **JaCoCo** - カバレッジツール（オプション）

## 🚀 セットアップとコマンド実行ガイド

### 前提条件

- JDK 21以上
- Gradle 8.x以上
- Payara Server 6（プロジェクトルートの`payara6/`に配置）
- HSQLDB（プロジェクトルートの`hsqldb/`に配置）

> **Note:** ① と ② の手順は、ルートの`README.md`を参照してください。

### ③ 依存関係の確認

このプロジェクトを開始する前に、以下が起動していることを確認してください：

- **① HSQLDBサーバー** （`./gradlew startHsqldb`）
- **② Payara Server** （`./gradlew startPayara`）

### ④ プロジェクトを開始するときに1回だけ実行

> **Note:** データベースのセットアップは`berry-books-api`プロジェクトで行います。  
> まだ実行していない場合は、先に以下を実行してください：
> ```bash
> ./gradlew :berry-books-api:setupHsqldb
> ```

```bash
# 1. プロジェクトをビルド
./gradlew :back-office-api:war

# 2. プロジェクトをデプロイ
./gradlew :back-office-api:deploy
```

### ⑤ プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# プロジェクトをアンデプロイ
./gradlew :back-office-api:undeploy
```

### ⑥ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
./gradlew :back-office-api:war
./gradlew :back-office-api:deploy
```

## 📍 APIエンドポイント

デプロイ後、以下のベースURLでAPIにアクセスできます：

- **ベースURL**: http://localhost:8080/back-office-api/api

### 書籍API (`/api/books`)

| メソッド | エンドポイント | 説明 | 認証 |
|---------|--------------|------|-----|
| GET | `/api/books` | 全書籍取得 | 不要 |
| GET | `/api/books/{id}` | 書籍詳細取得 | 不要 |
| GET | `/api/books/search?categoryId={id}&keyword={keyword}` | 書籍検索 | 不要 |

### カテゴリAPI (`/api/categories`)

| メソッド | エンドポイント | 説明 | 認証 |
|---------|--------------|------|-----|
| GET | `/api/categories` | カテゴリ一覧取得 | 不要 |


## 📝 APIの使用例（curl）

### 1. 全書籍取得

```bash
curl -X GET http://localhost:8080/back-office-api/api/books
```

### 2. 書籍詳細取得

```bash
curl -X GET http://localhost:8080/back-office-api/api/books/1
```

### 3. 書籍検索（カテゴリとキーワード）

```bash
curl -X GET "http://localhost:8080/back-office-api/api/books/search?categoryId=1&keyword=Java"
```

### 4. カテゴリ一覧取得

```bash
curl -X GET http://localhost:8080/back-office-api/api/categories
```

## 🧪 テスト

### テストの実行

このプロジェクトには、サービス層のユニットテストが含まれています。テストはJUnit 5とMockitoを使用して実装されています。

#### すべてのテストを実行

```bash
./gradlew :back-office-api:test
```

#### 特定のテストクラスを実行

```bash
# BookServiceのテストのみを実行
./gradlew :back-office-api:test --tests "*BookServiceTest"
```

#### テストの継続的実行（変更検知）

```bash
./gradlew :back-office-api:test --continuous
```

### テストレポートの確認

テスト実行後、HTMLレポートが生成されます：

```
projects/master/bookstore/back-office-api/build/reports/tests/test/index.html
```

ブラウザで開くとテスト結果の詳細が確認できます。

## 🎯 プロジェクト構成

```
projects/master/bookstore/back-office-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── pro/kensait/backoffice/
│   │   │       ├── config/           # JAX-RS設定
│   │   │       ├── resource/         # REST エンドポイント
│   │   │       ├── service/          # ビジネスロジック
│   │   │       ├── dao/              # データアクセス層
│   │   │       ├── entity/           # JPAエンティティ
│   │   │       ├── dto/              # データ転送オブジェクト
│   │   │       ├── exception/        # 例外クラス
│   │   │       └── common/           # 共通ユーティリティ
│   │   ├── resources/
│   │   │   ├── META-INF/
│   │   │   │   └── persistence.xml  # JPA設定
│   │   │   └── messages.properties  # メッセージリソース
│   │   └── webapp/
│   │       └── WEB-INF/
│   │           ├── web.xml
│   │           └── beans.xml
│   └── test/
│       └── java/
│           └── pro/kensait/backoffice/service/   # サービス層のテスト
├── sql/
│   └── hsqldb/                       # SQLスクリプト
└── build/
    └── libs/
        └── back-office-api.war
```

## 📦 パッケージ構成

```
pro.kensait.backoffice/
├── config/                 # JAX-RS設定
│   └── ApplicationConfig.java
├── resource/               # JAX-RSリソース（REST エンドポイント）
│   ├── BookResource.java
│   ├── CategoryResource.java
│   └── BookExceptionMapper.java
├── service/                # ビジネスロジック（CDI Bean）
│   ├── book/
│   │   └── BookService.java
│   └── category/
│       └── CategoryService.java
├── dao/                    # データアクセス層
│   ├── BookDao.java
│   ├── CategoryDao.java
│   ├── PublisherDao.java
│   └── StockDao.java
├── entity/                 # JPAエンティティ
│   ├── Book.java
│   ├── Category.java
│   ├── Publisher.java
│   └── Stock.java
├── dto/                    # データ転送オブジェクト
│   ├── BookTO.java
│   ├── CategoryTO.java
│   └── ErrorResponse.java
├── exception/              # 例外クラス
│   ├── BookNotFoundException.java
│   └── OutOfStockException.java
└── common/                 # 共通ユーティリティ
    └── MessageUtil.java
```

## 📚 アーキテクチャ

### レイヤー構成

```
REST Client / SPA
    ↓ HTTP/JSON
JAX-RS Resource (@Path, @ApplicationScoped)
    ↓
CDI Service (@ApplicationScoped)
    ↓
DAO (@ApplicationScoped)
    ↓ JPA
Database (HSQLDB)
```

### 主要クラス

#### 1. BookResource (JAX-RS Resource)

`@Path("/books")`と`@ApplicationScoped`を使用。書籍検索機能を提供。

#### 2. CategoryResource (JAX-RS Resource)

`@Path("/categories")`と`@ApplicationScoped`を使用。カテゴリ一覧機能を提供。

#### 3. BookService (CDI Bean)

`@ApplicationScoped`と`@Transactional`でトランザクション管理。

#### 4. BookDao (DAO)

`@PersistenceContext`で`EntityManager`を注入し、JPQL/Criteria APIでデータアクセス。

## 📝 データソース設定について

このプロジェクトはルートの`build.gradle`で定義されたタスクを使用してデータソースを作成します。

### 設定内容

- **JNDI名**: `jdbc/HsqldbDS`
- **データベース**: `testdb`
- **ユーザー**: `SA`
- **パスワード**: （空文字）
- **TCPサーバー**: `localhost:9001`

データソースはPayara Serverのドメイン設定に登録されます。

### ⚠️ 注意事項

- HSQLDB Databaseサーバーが起動している必要があります
- データソース作成はPayara Server起動後に実行してください
- 初回のみ実行が必要です（2回目以降は不要）

## 🛑 アプリケーションを停止する

### アプリケーションのアンデプロイ

```bash
./gradlew :back-office-api:undeploy
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
./gradlew :berry-books-api:setupHsqldb
```

## 🔗 関連プロジェクト

- **berry-books-api**: 注文管理API（このプロジェクトのAPIを呼び出す）
- **customer-hub-api**: 顧客管理API

## 📖 参考リンク

- [Jakarta EE 10 Platform](https://jakarta.ee/specifications/platform/10/)
- [Jakarta RESTful Web Services 3.1](https://jakarta.ee/specifications/restful-ws/3.1/)
- [JWT (JSON Web Token)](https://jwt.io/)
- [jjwt - Java JWT Library](https://github.com/jwtk/jjwt)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。
