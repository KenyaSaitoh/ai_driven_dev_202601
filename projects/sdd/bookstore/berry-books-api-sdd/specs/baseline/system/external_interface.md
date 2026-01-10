# berry-books-api - 外部インターフェース仕様書

**プロジェクトID:** berry-books-api  
**バージョン:** 2.0.0  
**最終更新日:** 2025-12-27  
**ステータス:** 外部API仕様確定

---

## 1. 概要

本文書は、berry-books-api REST APIが連携する外部システムのインターフェース仕様を記述する。現在、berry-books-rest API（顧客管理API）との連携を実装している。

---

## 2. 外部システム一覧

| システムID | システム名 | 連携方式 | 目的 | 必須/任意 |
|-----------|----------|---------|------|----------|
| EXT-001 | customer-hub-api | REST API (JAX-RS) | 顧客情報管理（CRUD） | 必須 |
| EXT-002 | back-office-api | REST API (JAX-RS) | 書籍・在庫・カテゴリ管理 | 必須 |

---

## 3. customer-hub-api連携

### 3.1 概要

**システム名**: customer-hub-api

**目的**: CUSTOMERテーブルへのアクセス（顧客情報取得、認証、登録）

**連携方式**: REST API (HTTP/JSON)

**プロトコル**: HTTP

**認証方式**: なし（同一ネットワーク内の信頼された通信）

**ベースURL**: `http://localhost:8080/customer-hub-api/customers`

設定方法:

1. システムプロパティ: `-Dcustomer-hub-api.base-url=...`
2. 環境変数: `CUSTOMER_HUB_API_BASE_URL=...`
3. プロパティファイル: `META-INF/microprofile-config.properties`
4. デフォルト値: `http://localhost:8080/customer-hub-api/customers`

### 3.2 依存関係

**実装クラス**: `pro.kensait.berrybooks.external.CustomerHubRestClient`

**スコープ**: アプリケーションスコープ（シングルトン）

**使用ライブラリ**: Jakarta RESTful Web Services Client API

クラス構成:
* RESTクライアントインスタンスを保持
* ベースURL設定値を保持
* 初期化メソッド（PostConstruct）:
  - RESTクライアントインスタンスを生成
  - 設定ファイルからベースURLを読み込み

---

## 4. APIエンドポイント仕様

### 4.1 顧客検索（メールアドレス）

#### 4.1.1 エンドポイント

```
GET /customers/query_email?email={email}
```

#### 4.1.2 概要

メールアドレスで顧客を検索する。ログイン認証時に使用する。

#### 4.1.3 リクエスト

**HTTPメソッド**: GET

クエリパラメータ:

| パラメータ | 型 | 必須 | 説明 |
|----------|---|------|------|
| email | string | ✓ | メールアドレス |

リクエスト例:

```
GET http://localhost:8080/customer-api/customers/query_email?email=alice@gmail.com
```

#### 4.1.4 レスポンス

成功時 (200 OK):

```json
{
  "customerId": 1,
  "customerName": "Alice",
  "password": "password",
  "email": "alice@gmail.com",
  "birthday": "1990-01-01",
  "address": "東京都渋谷区1-2-3"
}
```

顧客が見つからない場合 (404 Not Found):

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "顧客が見つかりません",
  "path": "/customers/query_email"
}
```

#### 4.1.5 呼び出し元

**クラス**: `pro.kensait.berrybooks.api.AuthenResource`

**メソッド**: `login(LoginRequest)`

**呼び出しタイミング**: ログイン時

処理フロー:
1. CustomerRestClientのfindByEmail()メソッドを呼び出し
2. メールアドレスで顧客情報を取得
3. 顧客が存在しない場合:
   - HTTPステータス 401 Unauthorized を返却
4. 顧客が存在する場合:
   - パスワード照合処理へ進む

---

### 4.2 顧客検索（顧客ID）

#### 4.2.1 エンドポイント

```
GET /customers/{customerId}
```

#### 4.2.2 概要

顧客IDで顧客を検索する。JWT Claimsから顧客IDを取得して顧客情報を取得する。

#### 4.2.3 リクエスト

**HTTPメソッド**: GET

パスパラメータ:

| パラメータ | 型 | 必須 | 説明 |
|----------|---|------|------|
| customerId | integer | ✓ | 顧客ID |

リクエスト例:

```
GET http://localhost:8080/customer-api/customers/1
```

#### 4.2.4 レスポンス

成功時 (200 OK):

```json
{
  "customerId": 1,
  "customerName": "Alice",
  "password": "password",
  "email": "alice@gmail.com",
  "birthday": "1990-01-01",
  "address": "東京都渋谷区1-2-3"
}
```

顧客が見つからない場合 (404 Not Found):

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "顧客が見つかりません",
  "path": "/customers/1"
}
```

#### 4.2.5 呼び出し元

**クラス**: `pro.kensait.berrybooks.api.AuthenResource`

**メソッド**: `getCurrentUser()`

**呼び出しタイミング**: JWT認証後、ログイン中の顧客情報取得時

処理フロー:
1. JWT ClaimsからcustomerIdを取得
2. CustomerRestClientのfindById()メソッドを呼び出し
3. 顧客IDで顧客情報を取得
4. 顧客情報を返却

---

### 4.3 顧客登録

#### 4.3.1 エンドポイント

```
POST /customers/
```

#### 4.3.2 概要

新規顧客を登録する。

#### 4.3.3 リクエスト

**HTTPメソッド**: POST

**Content-Type**: `application/json`

リクエストボディ:

```json
{
  "customerId": null,
  "customerName": "山田太郎",
  "password": "$2a$10$...",
  "email": "yamada@example.com",
  "birthday": "1990-01-01",
  "address": "東京都渋谷区1-2-3"
}
```

| フィールド | 型 | 必須 | 説明 |
|----------|---|------|------|
| customerId | integer | - | 顧客ID（新規登録時はnull） |
| customerName | string | ✓ | 顧客名 |
| password | string | ✓ | パスワード（BCryptハッシュ） |
| email | string | ✓ | メールアドレス（一意制約） |
| birthday | string (date) | - | 生年月日 |
| address | string | - | 住所 |

#### 4.3.4 レスポンス

成功時 (200 OK):

```json
{
  "customerId": 10,
  "customerName": "山田太郎",
  "password": "$2a$10$...",
  "email": "yamada@example.com",
  "birthday": "1990-01-01",
  "address": "東京都渋谷区1-2-3"
}
```

メールアドレス重複 (409 Conflict):

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "指定されたメールアドレスは既に登録されています",
  "path": "/customers/"
}
```

#### 4.3.5 呼び出し元

**クラス**: `pro.kensait.berrybooks.api.AuthenResource`

**メソッド**: `register(RegisterRequest)`

**呼び出しタイミング**: 新規登録時

処理フロー:
1. 新しいCustomerオブジェクトを生成
2. リクエストから各フィールドを設定:
   - customerName: リクエストの顧客名
   - password: BCryptでハッシュ化したパスワード
   - email: リクエストのメールアドレス
   - その他のフィールド
3. CustomerRestClientのregister()メソッドを呼び出し
4. 作成された顧客情報を返却

---

## 5. データ転送オブジェクト (DTO)

### 5.1 CustomerTO

**パッケージ**: `pro.kensait.berrybooks.external.dto`

**構造種別**: レコード型（immutableなデータ転送オブジェクト）

フィールド構成:

| フィールド名 | 型 | 説明 |
|------------|---|------|
| customerId | Integer | 顧客ID |
| customerName | String | 顧客名 |
| password | String | パスワード（ハッシュ化済み） |
| email | String | メールアドレス |
| birthday | LocalDate | 生年月日 |
| address | String | 住所 |

### 5.2 ErrorResponse

**パッケージ**: `pro.kensait.berrybooks.external.dto`

**構造種別**: レコード型（immutableなデータ転送オブジェクト）

フィールド構成:

| フィールド名 | 型 | 説明 |
|------------|---|------|
| status | int | HTTPステータスコード |
| error | String | エラー種別 |
| message | String | エラーメッセージ |
| path | String | リクエストパス |

---

## 6. エラーハンドリング

### 6.1 HTTPステータスコード

| ステータスコード | 説明 | 対応 |
|---------------|------|------|
| 200 OK | 成功 | レスポンスを返す |
| 404 Not Found | 顧客が見つからない | null を返す |
| 409 Conflict | メールアドレス重複 | EmailAlreadyExistsException をスロー |
| 500 Internal Server Error | サーバーエラー | RuntimeException をスロー |

### 6.2 例外マッピング

HTTPレスポンス処理ロジック:

レスポンスステータスコードに応じた分岐処理:

* 200 OK:
  - レスポンスボディをCustomerTOとして読み取り
  - CustomerエンティティオブジェクトへDTOを変換
  - 変換結果を返却

* 404 Not Found:
  - ログに「Customer not found」を出力
  - nullを返却

* 409 Conflict:
  - レスポンスボディをErrorResponseとして読み取り
  - EmailAlreadyExistsExceptionをスロー（メールアドレスとエラーメッセージを含む）

* その他:
  - ログに「Unexpected response status」を出力（ステータスコード含む）
  - RuntimeExceptionをスロー（「Failed to call external API」メッセージ）

---

## 7. リトライポリシー

### 7.1 リトライ設定

**現在の実装**: リトライなし

**将来の拡張**: 以下のリトライポリシーを検討

| 項目 | 値 |
|------|-----|
| リトライ対象 | 500 Internal Server Error, タイムアウト |
| リトライ回数 | 3回 |
| リトライ間隔 | 指数バックオフ（1秒, 2秒, 4秒） |
| リトライ失敗時 | RuntimeException をスロー |

---

## 8. タイムアウト設定

### 8.1 タイムアウト設定

**現在の実装**: デフォルト値を使用（無制限）

推奨設定:

| 項目 | 値 |
|------|-----|
| 接続タイムアウト | 5秒 |
| 読み取りタイムアウト | 10秒 |

将来の実装方針:

RESTクライアントビルダーを使用して以下のタイムアウトを設定:
* 接続タイムアウト: 5秒
* 読み取りタイムアウト: 10秒
* 時間単位: SECONDS

---

## 9. 監視・ログ

### 9.1 ログ出力

**ログレベル**: INFO

ログ内容:

* API呼び出し開始（メソッド名、パラメータ）
* リクエストURL
* レスポンスステータス
* エラー詳細（例外発生時）

ログ例:

```
INFO  [ CustomerRestClient#findByEmail ] email=alice@gmail.com
INFO  Request URL: http://localhost:8080/customer-api/customers/query_email?email=alice@gmail.com
INFO  Customer found: customerId=1
```

```
WARN  Customer not found: email=unknown@example.com
```

```
ERROR Error calling REST API: findByEmail
java.net.ConnectException: Connection refused
    at ...
```

---

## 10. セキュリティ考慮事項

### 10.1 認証・認可

**現在の実装**: 認証なし（同一ネットワーク内の信頼された通信）

本番環境の推奨:

* APIキー: リクエストヘッダーに`X-API-Key`を追加
* 相互TLS (mTLS): クライアント証明書による認証
* JWT: マイクロサービス間の認証トークン

### 10.2 通信暗号化

**現在の実装**: HTTP（非暗号化）

**本番環境の推奨**: HTTPS（TLS/SSL暗号化）

```
https://localhost:8443/customer-api/customers
```

### 10.3 データマスキング

**パスワード**: ログ出力時にマスキング

```
INFO  Customer found: email=alice@gmail.com, password=****
```

---

## 11. パフォーマンス考慮事項

### 11.1 レスポンスタイム

| API | 目標レスポンスタイム |
|-----|-------------------|
| GET /customers/query_email | 100ms以内 |
| GET /customers/{customerId} | 100ms以内 |
| POST /customers/ | 200ms以内 |

### 11.2 スループット

| 項目 | 目標値 |
|------|--------|
| 同時接続数 | 50接続 |
| スループット | 100 req/sec |

### 11.3 コネクションプーリング

**将来の拡張**: Apache HttpClient等のコネクションプールを使用

コネクションプール設定方針:
* プールマネージャー: PoolingHttpClientConnectionManager
* 最大接続数: 100
* ルート単位のデフォルト最大接続数: 20

---

## 12. テスト戦略

### 12.1 ユニットテスト

**モック戦略**: Mockitoを使用して外部APIをモック化

テスト手順:
1. **モック設定**:
   - CustomerRestClientをモックオブジェクトとして定義
   - findByEmail()メソッドの戻り値をスタブ化
   - テスト用の顧客オブジェクトを返却するよう設定

2. **テスト実行**:
   - AuthenResourceのlogin()メソッドを呼び出し
   - ログインリクエストを渡す

3. **検証**:
   - レスポンスのHTTPステータスコードが200であることを確認

### 12.2 結合テスト

**テストサーバー**: berry-books-rest APIのテストインスタンスを起動

**テストデータ**: テスト用の顧客データを事前登録

### 12.3 E2Eテスト

**ツール**: Playwright, curl

テストシナリオ:

1. 新規登録 → ログイン → 顧客情報取得
2. エラーケース（メールアドレス重複、顧客が見つからない）

---

## 13. 障害対応

### 13.1 障害シナリオと対応

| 障害シナリオ | 影響 | 対応 |
|-----------|------|------|
| berry-books-rest APIがダウン | ログイン・新規登録不可 | エラーメッセージ表示、リトライ（将来） |
| ネットワーク障害 | 外部API呼び出し失敗 | タイムアウト設定、リトライ |
| レスポンスタイムアウト | API呼び出しが遅延 | タイムアウト設定、監視アラート |
| データ不整合 | 顧客情報が存在しない | 404エラーとして処理 |

### 13.2 サーキットブレーカー

**将来の拡張**: サーキットブレーカーパターンを実装（MicroProfile Fault Tolerance）

サーキットブレーカー設定方針:
* リクエスト量閾値: 4（最小4リクエストで評価開始）
* 失敗率閾値: 0.5（50%以上の失敗でオープン）
* 遅延時間: 1000ms（オープン状態からハーフオープンへの遅延）

**適用メソッド**: findByEmail(String email)
* 外部API呼び出しを実行
* 失敗率が閾値を超えるとサーキットがオープンし、即座に失敗を返す

---

## 14. back-office-api連携

### 14.1 概要

**システム名**: back-office-api

**目的**: 書籍・在庫・カテゴリ管理（BOOK、STOCK、CATEGORY、PUBLISHERテーブルへのアクセス）

**連携方式**: REST API (HTTP/JSON)

**プロトコル**: HTTP

**認証方式**: なし（同一ネットワーク内の信頼された通信）

**ベースURL**: `http://localhost:8080/back-office-api/api`

設定方法:

1. システムプロパティ: `-Dback-office-api.base-url=...`
2. 環境変数: `BACK_OFFICE_API_BASE_URL=...`
3. プロパティファイル: `META-INF/microprofile-config.properties`
4. デフォルト値: `http://localhost:8080/back-office-api/api`

### 14.2 依存関係

**実装クラス**: `pro.kensait.berrybooks.external.BackOfficeRestClient`

**スコープ**: アプリケーションスコープ（シングルトン）

**使用ライブラリ**: Jakarta RESTful Web Services Client API

---

## 15. back-office-api エンドポイント仕様

### 15.1 書籍一覧取得

#### 15.1.1 エンドポイント

```
GET /books
```

#### 15.1.2 概要

全書籍を在庫・カテゴリ・出版社情報と共に取得する。

#### 15.1.3 レスポンス

成功時 (200 OK):

```json
[
  {
    "bookId": 1,
    "bookName": "Java入門",
    "author": "山田太郎",
    "price": 3000,
    "imageUrl": "/api/images/covers/1",
    "quantity": 10,
    "version": 0,
    "category": {
      "categoryId": 1,
      "categoryName": "Java"
    },
    "publisher": {
      "publisherId": 1,
      "publisherName": "技術評論社"
    }
  }
]
```

**BookTO の構造**:
- `back-office-api-sdd` の Book エンティティは `@SecondaryTable` アノテーションを使用して BOOK テーブルと STOCK テーブルを結合
- そのため、BookTO には在庫情報（`quantity`, `version`）がフラットな構造で含まれる
- カテゴリと出版社は内部クラス（`CategoryInfo`, `PublisherInfo`）としてネストされる

---

### 15.2 書籍詳細取得

#### 15.2.1 エンドポイント

```
GET /books/{bookId}
```

#### 15.2.2 リクエスト

パスパラメータ:

| パラメータ | 型 | 必須 | 説明 |
|----------|---|------|------|
| bookId | integer | ✓ | 書籍ID |

#### 15.2.3 レスポンス

成功時 (200 OK): 書籍一覧と同じ形式

失敗時 (404 Not Found): 書籍が見つからない

---

### 15.3 書籍検索（JPQL）

#### 15.3.1 エンドポイント

```
GET /books/search/jpql?categoryId={id}&keyword={keyword}
```

#### 15.3.2 リクエスト

クエリパラメータ:

| パラメータ | 型 | 必須 | 説明 |
|----------|---|------|------|
| categoryId | integer | - | カテゴリID（0または未指定=全カテゴリ） |
| keyword | string | - | キーワード（書籍名、著者名で部分一致検索） |

---

### 15.4 書籍検索（Criteria API）

#### 15.4.1 エンドポイント

```
GET /books/search/criteria?categoryId={id}&keyword={keyword}
```

#### 15.4.2 概要

Criteria APIを使用した書籍検索。パラメータは15.3と同じ。

---

### 15.5 カテゴリ一覧取得

#### 15.5.1 エンドポイント

```
GET /categories
```

#### 15.5.2 レスポンス

成功時 (200 OK):

```json
{
  "Java": 1,
  "SpringBoot": 2,
  "SQL": 3
}
```

**形式**: カテゴリ名 → カテゴリID のマップ

---

### 15.6 在庫取得

#### 15.6.1 エンドポイント

```
GET /stocks/{bookId}
```

#### 15.6.2 レスポンス

成功時 (200 OK):

```json
{
  "bookId": 1,
  "bookName": "Java SEディープダイブ",
  "quantity": 10,
  "version": 0
}
```

**注**: `StockTO` は書籍名も含みます。これは在庫情報の表示時に便利です。

---

### 15.7 在庫更新（楽観的ロック対応）

#### 15.7.1 エンドポイント

```
PUT /stocks/{bookId}
```

#### 15.7.2 リクエスト

パスパラメータ:

| パラメータ | 型 | 必須 | 説明 |
|----------|---|------|------|
| bookId | integer | ✓ | 書籍ID |

リクエストボディ:

```json
{
  "quantity": 8,
  "version": 1
}
```

| フィールド | 型 | 必須 | 説明 |
|----------|---|------|------|
| quantity | integer | ✓ | 更新後の在庫数 |
| version | long | ✓ | 楽観的ロック用バージョン番号 |

**重要**: フィールド名は `back-office-api-sdd` の `StockUpdateRequest` と一致する必要があります。以前のバージョンでは `newQuantity` を使用していましたが、現在は `quantity` が正しいフィールド名です。

#### 15.7.3 レスポンス

成功時 (200 OK):

```json
{
  "bookId": 1,
  "bookName": "Java SEディープダイブ",
  "quantity": 8,
  "version": 1
}
```

**注**: バージョン番号は更新後に自動的にインクリメントされます（0 → 1）。

楽観的ロック失敗 (409 Conflict):

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "在庫が他のユーザーによって更新されました",
  "path": "/stocks/1"
}
```

---

## 17. 将来の拡張

### 17.1 追加予定のAPI連携

| システム名 | 目的 | 連携方式 | 優先度 |
|----------|------|---------|--------|
| 決済システム | クレジットカード決済 | REST API | 高 |
| 配送業者API | 配送依頼、配送状況追跡 | REST API | 中 |
| メール配信サービス | 注文確認メール送信 | REST API | 中 |

### 17.2 アーキテクチャの拡張性

**メッセージキュー**: 非同期処理（Kafka, RabbitMQ）

**イベント駆動**: 注文イベントを他のマイクロサービスに配信

**サービスメッシュ**: Istio, Linkerdによるマイクロサービス間通信の制御

---

## 18. 実装状況と技術的対応方針

**最終更新**: 2026-01-10

### 18.1 実装完了状況

| 外部API連携クライアント | 状態 | 連携先 | 主な機能 |
|---------------------|------|--------|---------|
| CustomerHubRestClient | ✅ 完了 | customer-hub-api | 顧客検索・登録 |
| BackOfficeRestClient | ✅ 完了 | back-office-api | 書籍・在庫・カテゴリ |

### 18.2 技術的対応方針

#### 18.2.1 設定管理

**設定ファイル**: `src/main/resources/META-INF/microprofile-config.properties`

**設定項目**:
- `customer-hub-api.base-url`: 顧客管理APIのベースURL
- `back-office-api.base-url`: バックオフィスAPIのベースURL

**設定読み込み優先順位** (MicroProfile Config標準):
1. システムプロパティ
2. 環境変数
3. プロパティファイル
4. デフォルト値

**採用方式**: `ConfigProvider.getConfig()`による明示的な設定読み込み

#### 18.2.2 CDI設定

**beans.xml**: `src/main/webapp/WEB-INF/beans.xml`

**設定内容**:
- `bean-discovery-mode="all"`
- Jakarta EE 10標準に準拠

**役割**:
- CDIコンテナの有効化
- 依存性注入の動作保証
- MicroProfile Configの正常動作に必要

#### 18.2.3 エラーハンドリング

**ProcessingException**: ネットワークエラー、接続タイムアウト
- 503 Service Unavailableで応答
- 適切なエラーメッセージを返却

**WebApplicationException**: 4xx/5xxエラー
- 外部APIのステータスコードに応じた処理
- 404の場合は適切なNotFoundExceptionに変換

#### 18.2.4 ログ出力

**方針**:
- 外部API呼び出し時にURL、メソッドをログ出力
- エラー発生時にスタックトレースを記録
- 初期化時にbaseURL設定値を確認

### 18.3 動作確認結果

**テスト実行日**: 2026-01-10

| 外部API | エンドポイント | 動作状況 | 備考 |
|---------|--------------|---------|------|
| customer-hub-api | GET /customers/query_email | ✅ 正常 | メールアドレスで顧客検索 |
| customer-hub-api | POST /customers | ✅ 正常 | 顧客登録 |
| back-office-api | GET /books | ✅ 正常 | 全書籍取得 |
| back-office-api | GET /books/{id} | ✅ 正常 | 書籍詳細取得 |
| back-office-api | GET /categories | ✅ 正常 | カテゴリ一覧取得 |
| back-office-api | PUT /inventory/{id}/decrement | ✅ 正常 | 在庫減算 |

**確認事項**:
- 外部API連携が正常に動作
- 設定ファイルからのURL読み込みが正常
- エラーハンドリングが適切に機能
- ログ出力が適切に記録

### 18.4 トラブルシューティング

#### 18.4.1 設定読み込み問題

**症状**: baseURLが`null`になる

**原因**:
- プロパティキーの誤記
- beans.xmlの不在
- 初期化タイミングの問題

**対応方針**:
- ConfigProviderを使用した明示的な読み込み
- @PostConstructでの初期化
- デフォルト値の設定

#### 18.4.2 接続エラー

**症状**: "URI is not absolute"エラー

**原因**: baseURLが未設定または空文字

**対応方針**: 設定値の検証と適切なデフォルト値の提供

---

## 19. 参考資料

本外部インターフェース仕様書に関連する詳細ドキュメント：

* [requirements.md](requirements.md) - 要件定義書
* [functional_design.md](functional_design.md) - 機能設計書（API仕様）
* [architecture_design.md](architecture_design.md) - アーキテクチャ設計書
* [behaviors.md](behaviors.md) - 振る舞い仕様書（受入基準）
* [data_model.md](data_model.md) - データモデル仕様書
* [README.md](../../README.md) - プロジェクトREADME
* MicroProfile Config: https://microprofile.io/specifications/microprofile-config/

---

## 20. 連絡先

**customer-hub-api担当者**: [担当者名]

**back-office-api担当者**: [担当者名]

**障害連絡先**: [メールアドレス/Slack]

**APIドキュメント**: [URL]
