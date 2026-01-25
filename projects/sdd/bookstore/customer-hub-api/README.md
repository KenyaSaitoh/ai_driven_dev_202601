# customer-hub-api プロジェクト

## 📖 概要

Jakarta EE 10とJAX-RS (Jakarta RESTful Web Services) 3.1を使用したオンライン書店「**Berry Books**」のREST APIアプリケーションです。
顧客管理機能をRESTful APIとして提供します。

> **Note:** このプロジェクトは`berry-books-api`プロジェクトと同じデータベースを共有します。

## 🚀 セットアップとコマンド実行ガイド

### 前提条件

- JDK 21以上
- Gradle 8.x以上
- Payara Server 6（プロジェクトルートの`payara6/`に配置）
- HSQLDB（プロジェクトルートの`hsqldb/`に配置）

### ① プロジェクトを開始するときに1回だけ実行

```bash
# 1. データベースのセットアップ（CUSTOMERテーブルとテストデータの作成）
./gradlew :customer-hub-api:setupHsqldb

# 2. プロジェクトをビルド
./gradlew :customer-hub-api:war

# 3. プロジェクトをデプロイ
./gradlew :customer-hub-api:deploy
```

> **Note:** このプロジェクトは独自にCUSTOMERテーブルを管理します。

### ② プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# プロジェクトをアンデプロイ
./gradlew :customer-hub-api:undeploy
```

### ③ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
./gradlew :customer-hub-api:war
./gradlew :customer-hub-api:deploy
```

## 📍 アクセスURL

デプロイ後、以下のURLでAPIにアクセス：

- **顧客取得**: http://localhost:8080/customer-hub-api/customers/1
- **顧客検索（メール）**: http://localhost:8080/customer-hub-api/customers/query_email?email=alice@example.com

## 🎯 プロジェクト構成

```
projects/master/bookstore/customer-hub-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── pro/kensait/customerhub/
│   │   │       ├── config/         # JAX-RS設定
│   │   │       ├── resource/       # REST エンドポイント
│   │   │       ├── service/        # ビジネスロジック
│   │   │       ├── dao/            # データアクセス層
│   │   │       ├── entity/         # JPAエンティティ
│   │   │       ├── dto/            # データ転送オブジェクト
│   │   │       └── exception/      # 例外クラス
│   │   ├── resources/
│   │   │   └── META-INF/
│   │   │       └── persistence.xml  # JPA設定
│   │   └── webapp/
│   │       └── WEB-INF/
│   │           ├── web.xml
│   │           └── beans.xml
│   └── test/
├── sql/
│   └── hsqldb/                     # SQLスクリプト
└── build/
    └── libs/
        └── customer-hub-api.war
```

## 🔧 使用している技術

- **Jakarta EE 10**
- **Payara Server 6**
- **Jakarta RESTful Web Services (JAX-RS) 3.1**
- **Jakarta Persistence (JPA) 3.1** - Hibernate実装
- **Jakarta Transactions (JTA)**
- **Jakarta CDI 4.0**
- **Jakarta JSON Binding (JSON-B) 3.0** - Yasson実装
- **HSQLDB 2.7.x**

## 📦 パッケージ構成

```
pro.kensait.customerhub/
├── config/              # JAX-RS設定
│   └── ApplicationConfig.java
├── resource/            # JAX-RSリソース（REST エンドポイント）
│   ├── CustomerResource.java
│   └── CustomerExceptionMapper.java
├── service/             # ビジネスロジック（CDI Bean）
│   └── CustomerService.java
├── dao/                 # データアクセス層
│   └── CustomerDao.java
├── entity/              # JPAエンティティ
│   └── Customer.java
├── dto/                 # データ転送オブジェクト
│   ├── CustomerTO.java
│   └── ErrorResponse.java
└── exception/           # 例外クラス
    ├── CustomerNotFoundException.java
    └── CustomerExistsException.java
```

## 🌐 API仕様

### エンドポイント一覧

| メソッド | パス | 説明 | リクエストボディ | レスポンス |
|---------|------|------|----------------|-----------|
| `GET` | `/customers/{customerId}` | 顧客を取得（主キー検索） | - | `CustomerTO` |
| `GET` | `/customers/query_email?email={email}` | 顧客を取得（メールアドレス検索） | - | `CustomerTO` |
| `POST` | `/customers/` | 顧客を新規登録 | `CustomerTO` | `CustomerTO` |

### データモデル (CustomerTO)

顧客の基本情報。セキュリティのため、パスワードは含まれません。

```json
{
  "customerId": 1,
  "customerName": "山田太郎",
  "email": "yamada@example.com",
  "birthday": "1990-01-01",
  "address": "東京都渋谷区"
}
```

### データモデル (CustomerStatsTO)

顧客の基本情報と統計情報（注文件数、購入冊数）を含む。

```json
{
  "customerId": 1,
  "customerName": "山田太郎",
  "email": "yamada@example.com",
  "birthday": "1990-01-01",
  "address": "東京都渋谷区",
  "orderCount": 5,
  "totalBooks": 12
}
```

### データモデル (OrderHistoryTO)

顧客の注文履歴情報。

```json
{
  "orderTranId": 1,
  "orderDate": "2024-01-15",
  "totalPrice": 3500,
  "deliveryPrice": 500,
  "deliveryAddress": "東京都渋谷区...",
  "settlementType": 1,
  "items": [
    {
      "orderDetailId": 1,
      "bookId": 101,
      "bookName": "Java入門",
      "author": "山田太郎",
      "price": 3000,
      "count": 1
    }
  ]
}
```

### データモデル (OrderItemTO)

注文明細（購入した書籍）の情報。

```json
{
  "orderDetailId": 1,
  "bookId": 101,
  "bookName": "Java入門",
  "author": "山田太郎",
  "price": 3000,
  "count": 1
}
```

### エラーレスポンス (ErrorResponse)

```json
{
  "code": "customer.not-found",
  "message": "指定されたメールアドレスは存在しません"
}
```

## 📝 API使用例

### curlコマンドでのテスト

#### 1. 全顧客と統計情報を取得

```bash
curl -X GET http://localhost:8080/customer-hub-api/customers/1
```

**レスポンス例:**
```json
[
  {
    "customerId": 1,
    "customerName": "Alice",
    "email": "alice@example.com",
    "birthday": "1998-04-10",
    "address": "東京都中央区1-1-1",
    "orderCount": 5,
    "totalBooks": 12
  },
  {
    "customerId": 2,
    "customerName": "Bob",
    "email": "bob@example.com",
    "birthday": "1988-05-10",
    "address": "東京都杉並区2-2-2",
    "orderCount": 3,
    "totalBooks": 7
  }
]
```

#### 2. 顧客を取得（メールアドレス検索）

```bash
curl -X GET "http://localhost:8080/customer-hub-api/customers/query_email?email=alice@example.com"
```

#### 3. 顧客を新規登録

```bash
curl -X POST http://localhost:8080/customer-hub-api/customers/ \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "山田太郎",
    "password": "password123",
    "email": "yamada@example.com",
    "birthday": "1990-01-01",
    "address": "東京都渋谷区"
  }'
```

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
./gradlew :customer-hub-api:undeploy
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

# 初期データをセットアップ（berry-booksプロジェクトで実行）
./gradlew :berry-books-api:setupHsqldb
```

## 📚 アーキテクチャ

### レイヤー構成

```
JAX-RS Resource (API Layer)
    ↓
CDI Service (@ApplicationScoped, @Transactional)
    ↓
DAO (@ApplicationScoped, @PersistenceContext)
    ↓
JPA Entity (@Entity)
    ↓
Database (HSQLDB)
```

### 主要クラス

#### 1. ApplicationConfig.java (JAX-RS設定)

```java
@ApplicationPath("/")
public class ApplicationConfig extends Application {
    // デフォルトでは全てのJAX-RSリソースが自動検出される
}
```

#### 2. CustomerResource.java (JAX-RSリソース)

JAX-RSの`@Path`, `@GET`, `@POST`, `@PUT`, `@DELETE`を使用してREST APIを実装。

#### 3. CustomerExceptionMapper.java (例外マッパー)

`@Provider`を使用して、カスタム例外をHTTPレスポンスに変換。

```java
@Provider
public class CustomerExceptionMapper implements ExceptionMapper<RuntimeException> {
    // CustomerNotFoundException → 404
    // CustomerExistsException → 409
    // その他 → 500
}
```

#### 4. CustomerService.java (CDI Bean)

`@ApplicationScoped`と`@Transactional`でトランザクション管理。

#### 5. CustomerDao.java (DAO)

`@PersistenceContext`で`EntityManager`を注入し、JPQL/Criteria APIでデータアクセス。

## 🔗 関連プロジェクト

bookstoreドメインの他のプロジェクト：

### REST API（バックエンド）

- **berry-books-api**: 注文管理REST API（書籍の注文処理、認証・認可、JWT認証）
- **back-office-api**: 書籍・在庫管理REST API（書籍マスター、出版社・カテゴリ管理、在庫管理）
- **customer-hub-api**: 顧客管理REST API（このプロジェクト）

### SPA（フロントエンド）

- **berry-books-spa**: 注文管理フロントエンド（React + TypeScript）
- **back-office-spa**: 書籍管理フロントエンド（React + TypeScript）
- **customer-hub-spa**: 顧客管理フロントエンド（React + TypeScript）

### Desktop

- **customer-hub-swing**: 顧客管理デスクトップアプリケーション（Java Swing）

詳細は [projects/master/bookstore/README.md](../README.md) を参照してください。

## 📖 参考リンク

- [Jakarta EE 10 Platform](https://jakarta.ee/specifications/platform/10/)
- [Jakarta RESTful Web Services (JAX-RS) 3.1](https://jakarta.ee/specifications/restful-ws/3.1/)
- [Jakarta JSON Binding (JSON-B) 3.0](https://jakarta.ee/specifications/jsonb/3.0/)
- [Hibernate ORM Documentation](https://hibernate.org/orm/documentation/6.4/)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。
