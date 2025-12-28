# berry-books-api-sdd プロジェクト

## 📖 概要

Jakarta EE 10とJAX-RS (Jakarta RESTful Web Services) 3.1を使用したオンライン書店「**Berry Books**」のREST APIアプリケーションです。
書籍検索、JWT認証、注文処理などのEC機能をREST APIとして提供します。

> **Note:** このプロジェクトは**仕様駆動開発（SDD: Specification-Driven Development）**の研修用プロジェクトです。

> **SDDとは:**
> - 詳細な仕様書（specs/）に基づいて、段階的にコードを生成する手法
> - AIを活用して、仕様書からタスクリスト（tasks/）を生成し、タスクに従って実装を進める
> - 憲章（principles/）に定められた設計原則とベストプラクティスに従う
> - 完成版（master/berry-books-api）と同等の品質を目指す

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

## プロジェクト構成

```
berry-books-api-sdd/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── pro/kensait/berrybooks/
│   │   │       ├── api/              # JAX-RS Resources
│   │   │       │   ├── dto/          # API DTOs (Records)
│   │   │       │   └── exception/    # Exception Mappers
│   │   │       ├── security/         # JWT, SecuredResource
│   │   │       ├── service/          # Business Logic
│   │   │       │   ├── order/
│   │   │       │   ├── book/
│   │   │       │   ├── category/
│   │   │       │   ├── customer/
│   │   │       │   └── delivery/
│   │   │       ├── dao/              # Data Access Objects
│   │   │       ├── entity/           # JPA Entities
│   │   │       ├── external/         # External API Clients
│   │   │       │   └── dto/
│   │   │       ├── util/             # Utilities
│   │   │       └── common/           # Common Classes
│   │   ├── resources/
│   │   │   ├── META-INF/
│   │   │   │   ├── persistence.xml
│   │   │   │   └── microprofile-config.properties
│   │   │   ├── db/
│   │   │   │   ├── schema.sql
│   │   │   │   └── sample_data.sql
│   │   │   ├── log4j2.xml
│   │   │   └── messages.properties
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml
│   │       └── resources/
│   │           └── images/
│   │               └── covers/
│   └── test/
│       └── java/
│           └── pro/kensait/berrybooks/
├── build.gradle
└── README.md
```

## API仕様

### 認証API (`/api/auth`)

| メソッド | エンドポイント | 説明 | 認証 |
|---------|--------------|------|-----|
| POST | `/api/auth/login` | ログイン（JWT Cookie発行） | 不要 |
| POST | `/api/auth/logout` | ログアウト（Cookie削除） | 不要 |
| POST | `/api/auth/register` | 新規登録 | 不要 |
| GET | `/api/auth/me` | 現在のログインユーザー情報取得 | 必須 |

### 書籍API (`/api/books`)

| メソッド | エンドポイント | 説明 | 認証 |
|---------|--------------|------|-----|
| GET | `/api/books` | 書籍一覧取得 | 不要 |
| GET | `/api/books/{id}` | 書籍詳細取得 | 不要 |
| GET | `/api/books/search` | 書籍検索 | 不要 |
| GET | `/api/books/categories` | カテゴリ一覧取得 | 不要 |

### 注文API (`/api/orders`)

| メソッド | エンドポイント | 説明 | 認証 |
|---------|--------------|------|-----|
| POST | `/api/orders` | 注文作成 | 必須 |
| GET | `/api/orders/history` | 注文履歴取得 | 必須 |
| GET | `/api/orders/{tranId}` | 注文詳細取得 | 不要 |
| GET | `/api/orders/{tranId}/details/{detailId}` | 注文明細取得 | 不要 |

### 画像API (`/api/images`)

| メソッド | エンドポイント | 説明 | 認証 |
|---------|--------------|------|-----|
| GET | `/api/images/covers/{bookId}` | 書籍表紙画像取得 | 不要 |

**画像ファイル配置場所**: `src/main/webapp/resources/images/covers/`

**画像ファイル命名規則**: `{bookId}.jpg`（例: `1.jpg`, `2.jpg`）

**重要な実装詳細**:
- ServletContextを使用してWAR内リソースにアクセス
- 画像が存在しない場合は`no-image.jpg`をフォールバックとして返却
- デプロイ後もWARアーカイブ内から画像を配信可能

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

```bash
# 1. データベーステーブルとデータを作成
./gradlew :berry-books-api-sdd:setupHsqldb

# 2. プロジェクトをビルド
./gradlew :berry-books-api-sdd:war

# 3. プロジェクトをデプロイ
./gradlew :berry-books-api-sdd:deploy
```

> **重要:** `setupHsqldb`を実行すると、`src/main/resources/db/schema.sql`と`sample_data.sql`が実行されます。

### ⑤ プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# プロジェクトをアンデプロイ
./gradlew :berry-books-api-sdd:undeploy
```

### ⑥ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
./gradlew :berry-books-api-sdd:war
./gradlew :berry-books-api-sdd:deploy
```

## 📍 APIエンドポイント

デプロイ後、以下のベースURLでAPIにアクセスできます：

- **ベースURL**: http://localhost:8080/berry-books-api-sdd/api
- **ウェルカムページ**: http://localhost:8080/berry-books-api-sdd/

## 🔐 JWT認証

このAPIはJWT (JSON Web Token) ベースの認証を使用します。

### 認証フロー

1. クライアントが `/api/auth/login` にメールアドレスとパスワードを送信
2. 認証成功時、サーバーがJWTを生成し、HttpOnly Cookieで返却
3. 以降のリクエストで、ブラウザが自動的にCookieを送信
4. サーバー側で`JwtAuthenticationFilter`がCookieからJWTを抽出・検証
5. 認証必須のエンドポイントでは、JWTが有効でない場合401エラーを返す

### JWT設定

設定は`src/main/resources/META-INF/microprofile-config.properties`で管理されます：

```properties
# JWT秘密鍵（本番環境では環境変数で上書きすること）
jwt.secret-key=BerryBooksSecretKeyForJWT2024MustBe32CharactersOrMore

# JWT有効期限（ミリ秒）デフォルト: 24時間
jwt.expiration-ms=86400000

# JWT Cookie名
jwt.cookie-name=berry-books-jwt
```

> **重要:** 本番環境では、システムプロパティまたは環境変数で`jwt.secret-key`を上書きしてください。

### 外部API設定

```properties
customer.api.base-url=http://localhost:8080/customer-api/customers
```

## 📝 APIの使用例（curl）

### 1. 新規登録

```bash
curl -X POST http://localhost:8080/berry-books-api-sdd/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "山田太郎",
    "password": "password123",
    "email": "yamada@example.com",
    "birthday": "1990-01-01",
    "address": "東京都渋谷区1-2-3"
  }' \
  -c cookies.txt
```

### 2. ログイン

```bash
curl -X POST http://localhost:8080/berry-books-api-sdd/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@gmail.com",
    "password": "password"
  }' \
  -c cookies.txt
```

### 3. 全書籍取得

```bash
curl -X GET http://localhost:8080/berry-books-api-sdd/api/books
```

### 4. 書籍検索（カテゴリとキーワード）

```bash
curl -X GET "http://localhost:8080/berry-books-api-sdd/api/books/search?categoryId=1&keyword=Java"
```

### 5. 現在のログインユーザー情報取得

```bash
curl -X GET http://localhost:8080/berry-books-api-sdd/api/auth/me \
  -b cookies.txt
```

### 6. 注文作成

```bash
curl -X POST http://localhost:8080/berry-books-api-sdd/api/orders \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "cartItems": [
      {
        "bookId": 1,
        "bookName": "Java完全理解",
        "publisherName": "技術評論社",
        "price": 3200,
        "count": 2,
        "version": 0
      }
    ],
    "totalPrice": 6400,
    "deliveryPrice": 800,
    "deliveryAddress": "東京都渋谷区1-2-3",
    "settlementType": 1
  }'
```

### 7. 注文履歴取得

```bash
curl -X GET http://localhost:8080/berry-books-api-sdd/api/orders/history \
  -b cookies.txt
```

### 8. ログアウト

```bash
curl -X POST http://localhost:8080/berry-books-api-sdd/api/auth/logout \
  -b cookies.txt \
  -c cookies.txt
```

## 🧪 テスト

### テストの実行

このプロジェクトには、サービス層のユニットテストが含まれています。テストはJUnit 5とMockitoを使用して実装されています。

#### すべてのテストを実行

```bash
./gradlew :berry-books-api-sdd:test
```

#### 特定のテストクラスを実行

```bash
# AddressUtilのテストのみを実行
./gradlew :berry-books-api-sdd:test --tests "*AddressUtilTest"

# DeliveryFeeServiceのテストのみを実行
./gradlew :berry-books-api-sdd:test --tests "*DeliveryFeeServiceTest"
```

#### テストの継続的実行（変更検知）

```bash
./gradlew :berry-books-api-sdd:test --continuous
```

### テストレポートの確認

テスト実行後、HTMLレポートが生成されます：

```
projects/sdd/bookstore/berry-books-api-sdd/build/reports/tests/test/index.html
```

ブラウザで開くとテスト結果の詳細が確認できます。

### テストカバレッジの確認（JaCoCo）

```bash
# テストカバレッジレポートを生成
./gradlew :berry-books-api-sdd:jacocoTestReport

# カバレッジレポートの場所
# projects/sdd/bookstore/berry-books-api-sdd/build/reports/jacoco/test/html/index.html
```

## 📚 アーキテクチャ

### レイヤー構成

```
REST Client / SPA
    ↓ HTTP/JSON
JAX-RS Resource (@Path, @ApplicationScoped)
    ↓ JWT Authentication Filter
CDI Service (@ApplicationScoped)
    ↓
DAO (@ApplicationScoped) + REST Client
    ↓ JPA / HTTP
Database (HSQLDB) + External Customer API
```

**注:** 顧客情報は外部の`customer-api` REST API経由でアクセス（外部システム連携）

### 主要な設計パターン

- **REST Resource Pattern**: JAX-RS
- **Service Layer Pattern**: CDI + Transactional
- **Repository Pattern**: DAO
- **DTO Pattern**: Java Records
- **JWT Authentication**: HttpOnly Cookie
- **Dependency Injection**: CDI
- **Optimistic Locking**: `@Version`
- **Exception Mapper**: JAX-RS

### 楽観的ロック制御

在庫テーブル（`STOCK`）に`@Version`カラムを使用し、注文時の同時購入による在庫不整合を防止します。

### トランザクション管理

`OrderService.orderBooks()`メソッドに`@Transactional`を適用し、注文作成と在庫更新をアトミックに実行します。

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
./gradlew :berry-books-api-sdd:undeploy
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
./gradlew :berry-books-api-sdd:setupHsqldb
```

## 📖 参考リンク

- [Jakarta EE 10 Platform](https://jakarta.ee/specifications/platform/10/)
- [Jakarta RESTful Web Services 3.1](https://jakarta.ee/specifications/restful-ws/3.1/)
- [JWT (JSON Web Token)](https://jwt.io/)
- [jjwt - Java JWT Library](https://github.com/jwtk/jjwt)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。
