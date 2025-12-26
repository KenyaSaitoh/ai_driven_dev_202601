# berry-books-rest プロジェクト

## 📖 概要

Jakarta EE 10とJAX-RS (Jakarta RESTful Web Services) 3.1を使用したオンライン書店「**Berry Books**」のREST APIアプリケーションです。
顧客管理機能をRESTful APIとして提供します。

> **Note:** このプロジェクトは`berry-books-1`プロジェクトと同じデータベースを共有します。

## 🚀 セットアップとコマンド実行ガイド

### 前提条件

- JDK 21以上
- Gradle 8.x以上
- Payara Server 6（プロジェクトルートの`payara6/`に配置）
- HSQLDB（プロジェクトルートの`hsqldb/`に配置）

### ① プロジェクトを開始するときに1回だけ実行

> **Note:** データベースのセットアップは`berry-books-1`プロジェクトで行います。  
> まだ実行していない場合は、先に以下を実行してください：
> ```bash
> ./gradlew :projects:java:berry-books-1:setupHsqldb
> ```

```bash
# 1. プロジェクトをビルド
./gradlew :projects:java:berry-books-rest:war

# 2. プロジェクトをデプロイ
./gradlew :projects:java:berry-books-rest:deploy
```

### ② プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# プロジェクトをアンデプロイ
./gradlew :projects:java:berry-books-rest:undeploy
```

### ③ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
./gradlew :projects:java:berry-books-rest:war
./gradlew :projects:java:berry-books-rest:deploy
```

## 📍 アクセスURL

デプロイ後、以下のURLでAPIにアクセス：

- **顧客取得**: http://localhost:8080/berry-books-rest/customers/1
- **顧客の注文履歴取得**: http://localhost:8080/berry-books-rest/customers/1/orders
- **顧客検索（メール）**: http://localhost:8080/berry-books-rest/customers/query_email?email=alice@gmail.com

## 🎯 プロジェクト構成

```
projects/berry-books-rest/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── pro/kensait/berrybooks/
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
└── build/
    └── libs/
        └── berry-books-rest.war
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
pro.kensait.berrybooks/
├── config/              # JAX-RS設定
│   └── ApplicationConfig.java
├── resource/            # JAX-RSリソース（REST エンドポイント）
│   ├── CustomerResource.java
│   └── CustomerExceptionMapper.java
├── service/             # ビジネスロジック（CDI Bean）
│   └── CustomerService.java
├── dao/                 # データアクセス層
│   ├── CustomerDao.java
│   └── OrderTranDao.java
├── entity/              # JPAエンティティ
│   ├── Customer.java
│   ├── OrderTran.java
│   ├── OrderDetail.java
│   ├── OrderDetailPK.java
│   ├── Book.java
│   ├── Category.java
│   └── Publisher.java
├── dto/                 # データ転送オブジェクト
│   ├── CustomerTO.java
│   ├── CustomerStatsTO.java
│   ├── OrderHistoryTO.java
│   ├── OrderItemTO.java
│   └── ErrorResponse.java
└── exception/           # 例外クラス
    ├── CustomerNotFoundException.java
    └── CustomerExistsException.java
```

## 🌐 API仕様

### エンドポイント一覧

| メソッド | パス | 説明 | リクエストボディ | レスポンス |
|---------|------|------|----------------|-----------|
| `GET` | `/customers/` | 全顧客と統計情報を取得 | - | `CustomerStatsTO[]` |
| `GET` | `/customers/{customerId}` | 顧客を取得（主キー検索） | - | `CustomerTO` |
| `GET` | `/customers/{customerId}/orders` | 顧客の注文履歴を取得 | - | `OrderHistoryTO[]` |
| `GET` | `/customers/query_email?email={email}` | 顧客を取得（メールアドレス検索） | - | `CustomerTO` |
| `GET` | `/customers/query_birthday?birthday={date}` | 顧客リストを取得（誕生日検索） | - | `CustomerTO[]` |
| `POST` | `/customers/` | 顧客を新規登録 | `CustomerTO` | `CustomerTO` |
| `PUT` | `/customers/{customerId}` | 顧客を更新 | `CustomerTO` | - |
| `DELETE` | `/customers/{customerId}` | 顧客を削除 | - | - |

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
curl -X GET http://localhost:8080/berry-books-rest/customers/
```

**レスポンス例:**
```json
[
  {
    "customerId": 1,
    "customerName": "Alice Johnson",
    "email": "alice@gmail.com",
    "birthday": "1990-05-15",
    "address": "123 Main St, Springfield",
    "orderCount": 5,
    "totalBooks": 12
  },
  {
    "customerId": 2,
    "customerName": "Bob Smith",
    "email": "bob@gmail.com",
    "birthday": "1985-08-22",
    "address": "456 Oak Ave, Shelbyville",
    "orderCount": 3,
    "totalBooks": 7
  }
]
```

#### 2. 顧客を取得（主キー検索）

```bash
curl -X GET http://localhost:8080/berry-books-rest/customers/1
```

#### 3. 顧客の注文履歴を取得

```bash
curl -X GET http://localhost:8080/berry-books-rest/customers/1/orders
```

**レスポンス例:**
```json
[
  {
    "orderTranId": 1,
    "orderDate": "2024-01-15",
    "totalPrice": 3500,
    "deliveryPrice": 500,
    "deliveryAddress": "123 Main St, Springfield",
    "settlementType": 1,
    "items": [
      {
        "orderDetailId": 1,
        "bookId": 101,
        "bookName": "Java Programming",
        "author": "John Doe",
        "price": 3000,
        "count": 1
      }
    ]
  }
]
```

#### 4. 顧客を取得（メールアドレス検索）

```bash
curl -X GET "http://localhost:8080/berry-books-rest/customers/query_email?email=yamada@example.com"
```

#### 5. 顧客を新規登録

```bash
curl -X POST http://localhost:8080/berry-books-rest/customers/ \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "山田太郎",
    "email": "yamada@example.com",
    "birthday": "1990-01-01",
    "address": "東京都渋谷区"
  }'
```

> **Note:** パスワード管理は別途実装が必要です。現在のAPIではパスワードフィールドは含まれていません。

#### 6. 顧客を更新

```bash
curl -X PUT http://localhost:8080/berry-books-rest/customers/1 \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "山田太郎",
    "email": "yamada@example.com",
    "birthday": "1990-01-01",
    "address": "大阪府大阪市"
  }'
```

#### 7. 顧客を削除

```bash
curl -X DELETE http://localhost:8080/berry-books-rest/customers/1
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
./gradlew :projects:java:berry-books-rest:undeploy
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
./gradlew :projects:java:berry-books:setupHsqldb
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

- **berry-books**: 同じデータベースを使用するJSF MVCプロジェクト（データベース初期化も担当）

## 📖 参考リンク

- [Jakarta EE 10 Platform](https://jakarta.ee/specifications/platform/10/)
- [Jakarta RESTful Web Services (JAX-RS) 3.1](https://jakarta.ee/specifications/restful-ws/3.1/)
- [Jakarta JSON Binding (JSON-B) 3.0](https://jakarta.ee/specifications/jsonb/3.0/)
- [Hibernate ORM Documentation](https://hibernate.org/orm/documentation/6.4/)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。
