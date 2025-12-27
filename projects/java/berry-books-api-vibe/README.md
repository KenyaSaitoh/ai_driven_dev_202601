# berry-books-api-vibe プロジェクト

## 📖 概要

Jakarta EE 10とJAX-RS (Jakarta RESTful Web Services) 3.1を使用したオンライン書店「**Berry Books**」のREST APIアプリケーションです。
書籍検索、JWT認証、注文処理などのEC機能をREST APIとして提供します。

> **Note:** このプロジェクトは元々JSF (Jakarta Server Faces) 4.0を使用したMVCアプリケーションでしたが、JAX-RS REST APIに変換されました。

> **Note:** このプロジェクトは**データベース初期化**を担当します。関連プロジェクトと同じデータベースを共有します。

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

```bash
# 1. データベーステーブルとデータを作成（このプロジェクトが担当）
./gradlew :berry-books-api-vibe:setupHsqldb

# 2. プロジェクトをビルド
./gradlew :berry-books-api-vibe:war

# 3. プロジェクトをデプロイ
./gradlew :berry-books-api-vibe:deploy
```

> **重要:** `setupHsqldb`は**このプロジェクトで実行**してください。関連プロジェクトでも同じデータベースを使用します。

### ⑤ プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# プロジェクトをアンデプロイ
./gradlew :berry-books-api-vibe:undeploy
```

### ⑥ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
./gradlew :berry-books-api-vibe:war
./gradlew :berry-books-api-vibe:deploy
```

## 📍 APIエンドポイント

デプロイ後、以下のベースURLでAPIにアクセスできます：

- **ベースURL**: http://localhost:8080/berry-books-api-vibe/api

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
| GET | `/api/books` | 全書籍取得 | 不要 |
| GET | `/api/books/{id}` | 書籍詳細取得 | 不要 |
| GET | `/api/books/search?categoryId={id}&keyword={keyword}` | 書籍検索 | 不要 |
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

## 📝 APIの使用例（curl）

### 1. 新規登録

```bash
curl -X POST http://localhost:8080/berry-books-api-vibe/api/auth/register \
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
curl -X POST http://localhost:8080/berry-books-api-vibe/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@gmail.com",
    "password": "password"
  }' \
  -c cookies.txt
```

### 3. 全書籍取得

```bash
curl -X GET http://localhost:8080/berry-books-api-vibe/api/books
```

### 4. 書籍検索（カテゴリとキーワード）

```bash
curl -X GET "http://localhost:8080/berry-books-api-vibe/api/books/search?categoryId=1&keyword=Java"
```

### 5. 現在のログインユーザー情報取得

```bash
curl -X GET http://localhost:8080/berry-books-api-vibe/api/auth/me \
  -b cookies.txt
```

### 6. 注文作成

```bash
curl -X POST http://localhost:8080/berry-books-api-vibe/api/orders \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "cartItems": [
      {
        "bookId": 1,
        "bookName": "Java入門",
        "publisherName": "技術評論社",
        "price": 3000,
        "count": 2,
        "version": 1
      }
    ],
    "totalPrice": 6000,
    "deliveryPrice": 800,
    "deliveryAddress": "東京都渋谷区1-2-3",
    "settlementType": 1
  }'
```

### 7. 注文履歴取得

```bash
curl -X GET http://localhost:8080/berry-books-api-vibe/api/orders/history \
  -b cookies.txt
```

### 8. ログアウト

```bash
curl -X POST http://localhost:8080/berry-books-api-vibe/api/auth/logout \
  -b cookies.txt \
  -c cookies.txt
```

## 🎯 プロジェクト構成

```
projects/berry-books-api-vibe/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── pro/kensait/berrybooks/
│   │   │       ├── api/              # JAX-RS Resource（コントローラー層）
│   │   │       │   ├── AuthResource.java
│   │   │       │   ├── BookResource.java
│   │   │       │   ├── OrderResource.java
│   │   │       │   ├── ImageResource.java
│   │   │       │   ├── ApplicationConfig.java
│   │   │       │   ├── dto/          # Data Transfer Object
│   │   │       │   └── exception/    # Exception Mapper
│   │   │       ├── security/         # JWT認証
│   │   │       │   ├── JwtUtil.java
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   └── SecuredResource.java
│   │   │       ├── service/          # ビジネスロジック
│   │   │       ├── dao/              # データアクセス層
│   │   │       ├── entity/           # JPAエンティティ
│   │   │       ├── client/           # 外部API連携
│   │   │       ├── common/           # 共通ユーティリティ
│   │   │       └── util/             # ユーティリティ
│   │   ├── resources/
│   │   │   ├── META-INF/
│   │   │   │   ├── persistence.xml          # JPA設定
│   │   │   │   └── microprofile-config.properties  # JWT設定
│   │   │   ├── messages.properties          # メッセージリソース
│   │   │   └── ValidationMessages_ja.properties
│   │   └── webapp/
│   │       ├── resources/
│   │       │   └── images/
│   │       │       └── covers/        # 書籍表紙画像
│   │       └── WEB-INF/
│   │           ├── web.xml
│   │           └── beans.xml
│   └── test/
│       └── java/
│           └── pro/kensait/berrybooks/service/   # サービス層のテスト
├── sql/
│   └── hsqldb/                        # SQLスクリプト
└── build/
    ├── libs/
    │   └── berry-books-api-vibe.war
    └── reports/
        ├── tests/test/                # テストレポート
        └── jacoco/test/html/          # カバレッジレポート
```

## 📦 パッケージ構成

```
pro.kensait.berrybooks/
├── api/                    # JAX-RS Resource層
│   ├── AuthResource.java         # 認証API
│   ├── BookResource.java         # 書籍API
│   ├── OrderResource.java        # 注文API
│   ├── ImageResource.java        # 画像API
│   ├── ApplicationConfig.java    # JAX-RS設定
│   ├── dto/                      # Data Transfer Object
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   ├── RegisterRequest.java
│   │   ├── OrderRequest.java
│   │   ├── OrderResponse.java
│   │   ├── CartItemRequest.java
│   │   └── ErrorResponse.java
│   └── exception/                # Exception Mapper
│       ├── OutOfStockExceptionMapper.java
│       ├── OptimisticLockExceptionMapper.java
│       ├── ValidationExceptionMapper.java
│       └── GenericExceptionMapper.java
├── security/               # JWT認証
│   ├── JwtUtil.java              # JWT生成・検証
│   ├── JwtAuthenticationFilter.java  # JWT認証フィルター
│   └── SecuredResource.java      # 認証済みリソース情報
├── common/                 # 共通ユーティリティ・定数
│   ├── MessageUtil.java          # メッセージ取得ユーティリティ
│   ├── SettlementType.java       # 決済方法のEnum（定数化）
│   └── Const.java                # 定数クラス
├── util/                   # ユーティリティ
│   └── AddressUtil.java          # 住所関連ユーティリティ
├── service/                # ビジネスロジック（CDI Bean）
│   ├── book/
│   ├── category/
│   ├── customer/
│   ├── delivery/
│   └── order/
│       ├── OrderService.java
│       ├── OrderTO.java
│       └── CartItem.java         # DTO化されたカート項目
├── dao/                    # データアクセス層
│   ├── BookDao.java
│   ├── CategoryDao.java
│   ├── StockDao.java
│   ├── OrderTranDao.java
│   └── OrderDetailDao.java
├── entity/                 # JPAエンティティ
│   ├── Book.java
│   ├── Category.java
│   ├── Publisher.java
│   ├── Stock.java
│   ├── Customer.java
│   ├── OrderTran.java
│   └── OrderDetail.java
└── client/                 # 外部API連携
    ├── CustomerRestClient.java   # 外部顧客API連携
    └── dto/
        ├── CustomerTO.java
        └── ErrorResponse.java
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

**注:** 顧客情報は外部の`berry-books-rest` API経由でアクセス（外部システム連携）

### 主要クラス

#### 1. AuthResource (JAX-RS Resource)

`@Path("/auth")`と`@ApplicationScoped`を使用。認証機能（ログイン、登録、ログアウト）を提供。JWT Cookie を発行・削除します。

#### 2. BookResource (JAX-RS Resource)

`@Path("/books")`と`@ApplicationScoped`を使用。書籍検索機能を提供。

#### 3. OrderResource (JAX-RS Resource)

`@Path("/orders")`と`@ApplicationScoped`を使用。注文処理と注文履歴表示を実装。JWT認証必須。

#### 4. JwtAuthenticationFilter (ContainerRequestFilter)

`@Provider`と`@Priority(Priorities.AUTHENTICATION)`を使用。すべてのリクエストでJWTを検証し、認証情報を`SecuredResource`に設定します。

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
./gradlew :berry-books-api-vibe:undeploy
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
./gradlew :berry-books-api-vibe:setupHsqldb
```

## 🔗 関連プロジェクト

関連プロジェクトは**このプロジェクト（berry-books-api-vibe）でセットアップしたデータベース**を使用します。

## 📖 参考リンク

- [Jakarta EE 10 Platform](https://jakarta.ee/specifications/platform/10/)
- [Jakarta RESTful Web Services 3.1](https://jakarta.ee/specifications/restful-ws/3.1/)
- [JWT (JSON Web Token)](https://jwt.io/)
- [jjwt - Java JWT Library](https://github.com/jwtk/jjwt)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。
