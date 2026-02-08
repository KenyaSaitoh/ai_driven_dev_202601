# berry-books-api-sdd-agile プロジェクト（sdd-agile）

## 📖 概要

Jakarta EE 10とJAX-RS (Jakarta RESTful Web Services) 3.1を使用したオンライン書店「Berry Books」のREST APIアプリケーションです。
書籍検索、JWT認証、注文処理などのEC機能をREST APIとして提供します。

> **sdd-agile**: このプロジェクトはアジャイル開発向けの仕様駆動開発用です（`projects/sdd-agile/bookstore/` に配置）。  
> **Gradle プロジェクト名**: `berry-books-api-sdd-agile`（ビルド・デプロイ時はこの名前を使用。コンテキストルート: `/berry-books-api-sdd-agile`）

> Note: このプロジェクトは仕様駆動開発（SDD: Specification-Driven Development）の研修用プロジェクトです。

> SDDとは:
> - 業務共通SPEC（common/）とユースケースSPEC（usecases/{名}/）に基づいて実装を進める手法（ウォーターフォールの basic_design/ や detailed_design/ は不要）
> - タスク分解は不要。target 指定で common または usecases/{名} 単位でコード生成
> - Agent Skills (`agent_skills/jakarta-ee-api-agile/`) の principles/ に定められた設計原則に従う

## 🤖 Agent Skillsを使った開発（アジャイル）

このプロジェクトは、アジャイル向け Jakarta EE API 開発 Agent Skills（jakarta-ee-api-agile）を使用します。SPECは `specs/baseline/common/` と `specs/baseline/usecases/{名}/` で管理します。**タスク分解（tasks/）は不要**です。

開発は以下の流れで進めます：

```
ステップ1: 業務共通SPEC + ユースケースSPEC（common/ + usecases/{名}/）
    ↓
ステップ2: コード生成（target=common または target=usecases/{名}）
    ↓
ステップ3: 単体テスト実行評価
    ↓
ステップ4: 結合テスト生成（usecases/*/behaviors.md → JUnit + Weld SE）
    ↓
ステップ5: E2Eテスト生成（usecases 等の behaviors → REST Assured）
```

---

### 📋 開発フロー

#### ステップ1: 業務共通SPEC + ユースケースSPEC（プロジェクト開始時・拡張時）

業務共通SPEC（data_model, external_interface, architecture_design）を先に整え、各ユースケースに userstory.md / behaviors.md を配置します。外部API（back-office-api）連携用のOpenAPIは `common/openapi/` に格納します。

```
@agent_skills/jakarta-ee-api-agile/instructions/common_spec.md
@agent_skills/jakarta-ee-api-agile/instructions/usecase_spec.md

パラメータ:
* project_root: projects/sdd-agile/bookstore/berry-books-api
* spec_directory: projects/sdd-agile/bookstore/berry-books-api/specs/baseline
```

* 配置: `specs/baseline/common/*.md`, `specs/baseline/common/openapi/*.yaml`, `specs/baseline/usecases/{auth|books|images|orders}/userstory.md`, `behaviors.md`

---

#### ステップ2: コード生成（target 指定で実装＋単体テスト）

業務共通SPEC（common/）の3SPEC と usecases/{名}/userstory.md, behaviors.md を駆動元に、**target** で指定した対象の実装と単体テストを生成します。タスクファイル（tasks/）は不要です。

**実行順序**: 先に `target=common` で業務共通実装を完了し、続いて各ユースケースを `target=usecases/{名}` で順に実装します。

使用例（業務共通・common）:

```
@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md

業務共通（common）のコードを生成してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/berry-books-api
* target: common
* skip_infrastructure: true  # 初回setup時: DB/APサーバーセットアップをスキップする場合
```

使用例（ユースケース）:

```
@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md

ユースケース auth のコードを生成してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/berry-books-api
* target: usecases/auth
```

* 同様に `target: usecases/books`, `target: usecases/images`, `target: usecases/orders` 等で各ユースケースを実装

**SPEC変更時**: SPEC を編集したうえで、本インストラクションで target を指定して再実行すれば差分が反映されます。

---

#### ステップ3: 単体テスト実行評価

単体テストを実行してカバレッジを分析し、品質を検証します。

```
@agent_skills/jakarta-ee-api-agile/instructions/unit_test_execution.md

単体テストを実行してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/berry-books-api
* target: common   # または usecases/books 等、対象ユースケース
* gradle_project_dir: projects/sdd-agile/bookstore/berry-books-api  # マルチプロジェクト構成用（ルートbuild.gradleを使用するため）
```

**マルチプロジェクト構成について:**
* このプロジェクトは、リポジトリルートの `build.gradle` を使用するマルチプロジェクト構成です
* `gradle_project_dir` パラメータでプロジェクトルートのパスを指定することで、適切なディレクトリでGradleタスクが実行されます
* 未指定の場合はデフォルトで `project_root` が使用されますが、マルチプロジェクト構成ではルートの build.gradle を使うため、明示的に指定することを推奨します

AIが：
1. 🧪 テスト実行（gradle test jacocoTestReport）
2. 📊 テスト結果とカバレッジ分析
3. 🔍 問題の分類（テスト失敗、必要な振る舞い、デッドコード）
4. 📋 フィードバックレポート生成
5. 💬 ユーザーに推奨アクションを提示

重要：
* 問題を発見してもユーザー確認なしに修正しない
* カバレッジ不足やデッドコードを具体的に提案
* 必要に応じてコード生成（または 業務共通SPEC/ユースケースSPEC の見直し）に戻ってループ

🔄 フィードバックループ:
```
コード生成 → テスト実行評価
    ↑              ↓
    └── フィードバック ←┘
```

---

#### ステップ4: 結合テスト生成（単体テスト完了後）

単体テスト完了後に、結合テスト（Integration Test）を生成します。

```
@agent_skills/jakarta-ee-api-agile/instructions/it_generation.md

結合テストを生成してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/berry-books-api
* spec_directory: projects/sdd-agile/bookstore/berry-books-api/specs/baseline
```

AIが：
1. 📄 usecases/*/behaviors.md（結合テストシナリオ）を読み込む
2. 🧪 JUnit 5 + Weld SE を使用した結合テストを生成
   * Service層以下（Service + DAO + Entity + DB）の連携テスト
   * 実際のDBアクセス（メモリDB）
   * 外部APIはWireMockでスタブ化
   * アプリケーションサーバー不要
3. 🏷️ `@Tag("integration")` で結合テストを分離

実行方法:
```bash
# 結合テストを実行
./gradlew integrationTest
```

---

#### ステップ5: E2Eテスト生成（実装完了後）

全機能実装完了後に、E2Eテスト（End-to-End Test）を生成します。

```
@agent_skills/jakarta-ee-api-agile/instructions/e2e_test_generation.md

E2Eテストを生成してください。

パラメータ:
* project_root: projects/sdd-agile/bookstore/berry-books-api
* spec_directory: projects/sdd-agile/bookstore/berry-books-api/specs/baseline
```

AIが：
1. 📄 usecases/*/behaviors.md 等（E2Eテストシナリオ）を読み込む
2. 🧪 REST Assured を使用したE2Eテストを生成
   * 複数API間の連携テスト（認証 → 書籍検索 → 注文作成等）
   * 外部API連携のテスト（back-office-api、customer-hub-api）
   * 実際のHTTPリクエスト/レスポンス
3. 🏷️ `@Tag("e2e")` でE2Eテストを分離

実行方法:
```bash
# アプリケーションサーバーを起動
./gradlew run

# 別ターミナルでE2Eテストを実行
./gradlew e2eTest
```

---

### 🔄 SPEC変更時

業務共通SPEC またはユースケースの仕様を変更した場合、`@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md` で target を指定して再実行すれば、差分が反映されます。

---

### 📚 詳細情報

詳細は `@agent_skills/jakarta-ee-api-agile/README.md` を参照してください。

#### 開発原則

このプロジェクトは、以下の原則に従って開発されます：

* 場所: `@agent_skills/jakarta-ee-api-agile/principles/`
  * [architecture.md](../../../agent_skills/jakarta-ee-api-agile/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](../../../agent_skills/jakarta-ee-api-agile/principles/security.md) - セキュリティ標準
  * [common_rules.md](../../../agent_skills/jakarta-ee-api-agile/principles/common_rules.md) - 共通ルール

* 主な内容:
  * 標準技術スタック（Jakarta EE 10、JPA 3.1、JAX-RS 3.1）
  * レイヤードアーキテクチャ（API、Security、Service、DAO、Entity）
  * 開発標準（命名規則、コーディング規約、バリデーション、エラーハンドリング）
  * セキュリティ実装（JWT認証、HttpOnly Cookie、認証フィルター）
  * トランザクション管理、外部API連携
  * テスト戦略、パフォーマンス考慮事項

## 🎯 プロジェクトの特徴

### アーキテクチャ
* バックエンドサービス: フロントエンド（berry-books-spa）の唯一のエントリーポイント
* マイクロサービス統合: 複数のバックエンドマイクロサービスを統合
* フロントエンド最適化: フロントエンド向けに最適化されたAPIを提供

### 実装パターン

#### 外部API呼び出し
* BookResource: 書籍情報を`back-office-api`から取得
* CategoryResource: カテゴリ情報を`back-office-api`から取得

#### 独自のビジネスロジック実装
* AuthenResource: JWT認証 + `customer-hub-api`連携
* OrderResource: 注文処理 + 在庫管理連携
* ImageResource: WAR内リソース配信

### データ管理の制約
* 実装する: OrderTran、OrderDetail（注文関連のみ）
* 実装しない: Book、Stock、Category、Customer（外部API管理）

### 外部API連携
* BackOfficeRestClient: `back-office-api`との連携（書籍・在庫・カテゴリ管理）
* CustomerHubRestClient: `customer-hub-api`との連携（顧客管理）

### JWT認証
* JWT生成・検証は本システムで実装
* HttpOnly Cookieで安全に管理
* 認証必須エンドポイントの保護

## 🔧 使用している技術

### 本番環境

* Jakarta EE 10
* Payara Server 6
* JAX-RS (Jakarta RESTful Web Services) 3.1 - REST API
* Jakarta Persistence (JPA) 3.1 - Hibernate実装
* Jakarta Transactions (JTA)
* Jakarta CDI 4.0
* Jakarta Bean Validation 3.0
* HSQLDB 2.7.x
* JWT (JSON Web Token) - jjwt 0.12.6
* BCrypt - パスワードハッシュ化

### テスト環境

* JUnit 5 - テストフレームワーク
* Mockito - モックライブラリ
* JaCoCo - カバレッジツール（オプション）

## プロジェクト構成

### SPEC構成（アジャイル・本プロジェクトの実際の構造）

```
specs/
└── baseline/
    ├── common/                     # 業務共通SPEC
    │   ├── architecture_design.md
    │   ├── data_model.md
    │   ├── external_interface.md
    │   └── openapi/                # 外部API（back-office-api）連携用 OpenAPI
    │       ├── auth-api.yaml
    │       ├── books-api.yaml
    │       ├── categories-api.yaml
    │       ├── publishers-api.yaml
    │       ├── stocks-api.yaml
    │       └── workflows-api.yaml
    └── usecases/                   # ユースケース別（各フォルダに userstory.md, behaviors.md）
        ├── auth/
        ├── books/
        ├── images/
        └── orders/
```

* ウォーターフォール版の `basic_design/`、`requirements/`、`detailed_design/`、`tasks/` は本フローでは使用しません。

### プロジェクト全体

```
berry-books-api/
├── specs/                          # 上記の通り
├── sql/hsqldb/                     # DDL・DML（setupHsqldb で使用）
├── src/
│   ├── main/java/
│   ├── main/resources/
│   └── main/webapp/
├── images/covers/                  # 書籍表紙画像
├── test_script/                    # APIテストスクリプト
├── build.gradle
└── README.md
```

---

## 📊 実装状況

* 最終更新: 2026-01-10

### ✅ 実装完了コンポーネント

| レイヤー | クラス | 状態 | 備考 |
|---------|-------|------|------|
| API | AuthenResource | ✅ 完了 | JWT認証、外部API連携 |
| API | BookResource | ✅ 完了 | 外部API呼び出し（2026-01-10実装） |
| API | CategoryResource | ✅ 完了 | 外部API呼び出し（2026-01-10実装） |
| API | OrderResource | ✅ 完了 | 注文処理、在庫管理連携 |
| API | ImageResource | ✅ 完了 | WAR内リソース配信 |
| External | BackOfficeRestClient | ✅ 完了 | ConfigProvider方式（2026-01-10修正） |
| External | CustomerHubRestClient | ✅ 完了 | ConfigProvider方式（2026-01-10修正） |
| Security | JwtUtil | ✅ 完了 | JWT生成・検証 |
| Security | JwtAuthenFilter | ✅ 完了 | MediaType設定、PUBLIC_ENDPOINTS拡張 |
| Exception | 全ExceptionMapper | ✅ 完了 | MediaType設定追加（2026-01-10修正） |
| Config | beans.xml | ✅ 完了 | CDI有効化（2026-01-10追加） |
| Config | microprofile-config.properties | ✅ 完了 | 外部API URL設定 |

### 🔧 技術的対応（2026-01-10実施）

#### 1. MicroProfile Config読み込み方式の変更

* `@ConfigProperty`から`ConfigProvider.getConfig()`方式へ変更
* `@PostConstruct`で明示的に設定を読み込み
* 環境依存の問題を回避し、より確実な設定読み込みを実現

* 対象: `BackOfficeRestClient.java`, `CustomerHubRestClient.java`

#### 2. CDI有効化（beans.xml追加）

* `src/main/webapp/WEB-INF/beans.xml`を追加
* CDIコンテナの有効化（`@Inject`、`@ApplicationScoped`の動作に必須）
* MicroProfile Configの正常動作に必要

#### 3. エラーレスポンスのMediaType明示

* 全ExceptionMapperで`.type(MediaType.APPLICATION_JSON)`を追加
* PayaraがJSONシリアライザーを判断できるように修正

* 対象:
  * `GenericExceptionMapper`, `OutOfStockExceptionMapper`, `ValidationExceptionMapper`, `OptimisticLockExceptionMapper`, `JwtAuthenFilter`

#### 4. JwtAuthenFilterのPUBLIC_ENDPOINTS拡張

* `/api`プレフィックスあり・なし両方のパスを登録
* Payaraのコンテキストパス処理に対応

### 🧪 動作確認済みAPI

* テスト実行日: 2026-01-10

| API | エンドポイント | HTTPステータス | 備考 |
|-----|--------------|---------------|------|
| 書籍API | GET /api/books | 200 OK | ✅ 動作確認済み |
| 書籍API | GET /api/books/{id} | 200 OK / 404 Not Found | ✅ 動作確認済み |
| 書籍API | GET /api/books/search/jpql | 200 OK | ✅ 動作確認済み |
| 書籍API | GET /api/books/search/criteria | 200 OK | ✅ 動作確認済み |
| カテゴリAPI | GET /api/categories | 200 OK | ✅ 動作確認済み |
| 画像API | GET /api/images/covers/{id} | 200 OK | ✅ 動作確認済み |
| 認証API | POST /api/auth/login | 401 Unauthorized | ⚠️ テストユーザー未登録 |
| ログアウトAPI | POST /api/auth/logout | 500 Internal Server Error | 🔍 調査中 |
| 注文API | POST /api/orders | 401 Unauthorized | ⚠️ 未ログイン |

---

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

* 画像ファイル配置場所: `src/main/webapp/resources/images/covers/`

* 画像ファイル命名規則: `{bookId}.jpg`（例: `1.jpg`, `2.jpg`）

* 重要な実装詳細:
  * ServletContextを使用してWAR内リソースにアクセス
  * 画像が存在しない場合は`no-image.jpg`をフォールバックとして返却
  * デプロイ後もWARアーカイブ内から画像を配信可能

## 🚀 セットアップとコマンド実行ガイド

### 前提条件

* JDK 21以上
* Gradle 8.x以上
* Payara Server 6（プロジェクトルートの`payara6/`に配置）
* HSQLDB（プロジェクトルートの`hsqldb/`に配置）

> Note: ① と ② の手順は、ルートの`README.md`を参照してください。

### ③ 依存関係の確認

このプロジェクトを開始する前に、以下が起動していることを確認してください：

* ① HSQLDBサーバー （`./gradlew startHsqldb`）
* ② Payara Server （`./gradlew startPayara`）

### ④ プロジェクトを開始するときに1回だけ実行

```bash
# 1. データベーステーブルとデータを作成
./gradlew :berry-books-api-sdd-agile:setupHsqldb

# 2. プロジェクトをビルド
./gradlew :berry-books-api-sdd-agile:war

# 3. プロジェクトをデプロイ
./gradlew :berry-books-api-sdd-agile:deploy
```

> 重要: `setupHsqldb`を実行すると、`src/main/resources/db/schema.sql`と`sample_data.sql`が実行されます。

### ⑤ プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# プロジェクトをアンデプロイ
./gradlew :berry-books-api-sdd-agile:undeploy
```

### ⑥ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
./gradlew :berry-books-api-sdd-agile:war
./gradlew :berry-books-api-sdd-agile:deploy
```

## 📍 APIエンドポイント

デプロイ後、以下のベースURLでAPIにアクセスできます：

* ベースURL: http://localhost:8080/berry-books-api-sdd-agile/api
* ウェルカムページ: http://localhost:8080/berry-books-api-sdd-agile/

## 🔐 JWT認証

このAPIはJWT (JSON Web Token) ベースの認証を使用します。

### 認証フロー

1. クライアントが `/api/auth/login` にメールアドレスとパスワードを送信
2. 認証成功時、サーバーがJWTを生成し、HttpOnly Cookieで返却
3. 以降のリクエストで、ブラウザが自動的にCookieを送信
4. サーバー側で`JwtAuthenFilter`がCookieからJWTを抽出・検証
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

> 重要: 本番環境では、システムプロパティまたは環境変数で`jwt.secret-key`を上書きしてください。

### 外部API設定

```properties
# 外部APIのベースURL
back-office-api.base-url=http://localhost:8080/back-office-api-sdd-agile/api
customer-hub-api.base-url=http://localhost:8080/customer-hub-api/api/customers
```

> 重要: 本システムは以下の外部APIに依存します：
> - back-office-api-sdd-agile: 書籍・在庫・カテゴリ管理
> - customer-hub-api: 顧客管理

## 📝 APIの使用例（curl）

### 1. 新規登録

```bash
curl -X POST http://localhost:8080/berry-books-api-sdd-agile/api/auth/register \
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
curl -X POST http://localhost:8080/berry-books-api-sdd-agile/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@gmail.com",
    "password": "password"
  }' \
  -c cookies.txt
```

### 3. 全書籍取得

```bash
curl -X GET http://localhost:8080/berry-books-api-sdd-agile/api/books
```

### 4. 書籍検索（カテゴリとキーワード）

```bash
curl -X GET "http://localhost:8080/berry-books-api-sdd-agile/api/books/search?categoryId=1&keyword=Java"
```

### 5. 現在のログインユーザー情報取得

```bash
curl -X GET http://localhost:8080/berry-books-api-sdd-agile/api/auth/me \
  -b cookies.txt
```

### 6. 注文作成

```bash
curl -X POST http://localhost:8080/berry-books-api-sdd-agile/api/orders \
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
curl -X GET http://localhost:8080/berry-books-api-sdd-agile/api/orders/history \
  -b cookies.txt
```

### 8. ログアウト

```bash
curl -X POST http://localhost:8080/berry-books-api-sdd-agile/api/auth/logout \
  -b cookies.txt \
  -c cookies.txt
```

## 🧪 テスト

### テストの実行

このプロジェクトには、サービス層のユニットテストが含まれています。テストはJUnit 5とMockitoを使用して実装されています。

**マルチプロジェクト構成について:**
* このプロジェクトは、リポジトリルートの `build.gradle` を使用するマルチプロジェクト構成です
* Gradleコマンドは、リポジトリルート（`ai_driven_dev_202601/`）で実行します
* プロジェクト指定は `:プロジェクト名:タスク名` の形式を使用します（例: `:berry-books-api-sdd-agile:test`）

#### すべてのテストを実行

リポジトリルートから実行:
```bash
cd ai_driven_dev_202601
./gradlew :berry-books-api-sdd-agile:test
```

またはプロジェクトルートから実行（相対パスでgradlewを指定）:
```bash
cd projects/sdd-agile/bookstore/berry-books-api
../../../../gradlew test
```

#### 特定のテストクラスを実行

```bash
# AddressUtilのテストのみを実行
./gradlew :berry-books-api-sdd-agile:test --tests "*AddressUtilTest"

# DeliveryFeeServiceのテストのみを実行
./gradlew :berry-books-api-sdd-agile:test --tests "*DeliveryFeeServiceTest"
```

#### テストの継続的実行（変更検知）

```bash
./gradlew :berry-books-api-sdd-agile:test --continuous
```

### テストレポートの確認

テスト実行後、HTMLレポートが生成されます：

```
projects/sdd-agile/bookstore/berry-books-api/build/reports/tests/test/index.html
```

ブラウザで開くとテスト結果の詳細が確認できます。

### テストカバレッジの確認（JaCoCo）

```bash
# テストカバレッジレポートを生成
./gradlew :berry-books-api-sdd-agile:jacocoTestReport

# カバレッジレポートの場所
# projects/sdd-agile/bookstore/berry-books-api/build/reports/jacoco/test/html/index.html
```

## 📚 アーキテクチャ

### レイヤー構成

```
REST Client / SPA (berry-books-spa)
    ↓ HTTP/JSON
JAX-RS Resource (@Path, @ApplicationScoped)
    ↓ JWT Authentication Filter
CDI Service (@ApplicationScoped)
    ↓
REST Client → back-office-api (書籍・在庫・カテゴリ)
REST Client → customer-hub-api (顧客)
DAO (@ApplicationScoped)
    ↓ JPA
Database (HSQLDB) ← 注文データのみ管理
```

本システムの役割:
* フロントエンドの唯一のエントリーポイント
* 注文管理という独自のドメインを持つ
* 必要に応じて外部システム（書籍管理、顧客管理）を呼び出す
* 注文処理、配送料金計算などのビジネスロジックを実装

### 主要な設計パターン

* REST Resource Pattern: JAX-RS（HTTPエンドポイント）
* Service Layer Pattern: CDI + Transactional（注文ビジネスロジック）
* Repository Pattern: DAO（注文データアクセス）
* DTO Pattern: Java Records（データ転送）
* JWT Authentication: HttpOnly Cookie（認証管理）
* Dependency Injection: CDI（依存性注入）
* REST Client Pattern: 外部API連携（back-office-api、customer-hub-api）
* Exception Mapper: JAX-RS（エラーハンドリング）

### データ管理の分離

本システムで管理するデータ:
* 注文トランザクション（ORDER_TRAN）
* 注文明細（ORDER_DETAIL）

外部APIで管理するデータ（本システムでは管理しない）:
* 書籍・在庫・カテゴリ（back-office-api）
* 顧客情報（customer-hub-api）

### トランザクション管理

`OrderService.orderBooks()`メソッドに`@Transactional`を適用し、注文作成をアトミックに実行します。
在庫更新は`back-office-api`へのREST API呼び出しで行います（分散トランザクション）。

## 📝 データソース設定について

このプロジェクトはルートの`build.gradle`で定義されたタスクを使用してデータソースを作成します。

### 設定内容

* JNDI名: `jdbc/HsqldbDS`
* データベース: `testdb`
* ユーザー: `SA`
* パスワード: （空文字）
* TCPサーバー: `localhost:9001`

データソースはPayara Serverのドメイン設定に登録されます。

### ⚠️ 注意事項

* HSQLDB Databaseサーバーが起動している必要があります
* データソース作成はPayara Server起動後に実行してください
* 初回のみ実行が必要です（2回目以降は不要）

## 🛑 アプリケーションを停止する

### アプリケーションのアンデプロイ

```bash
./gradlew :berry-books-api-sdd-agile:undeploy
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

> Note: WindowsではGit Bashを使用してください。

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
./gradlew :berry-books-api-sdd-agile:setupHsqldb
```

## 🧹 SDD成果物のクリーンアップ

仕様駆動開発により何度でも再実装できます。詳細は [ルートREADMEのSDDクリーンアップ節](../../../README.md#仕様駆動開発sddプロジェクトの成果物クリーンアップ) を参照してください。

```bash
# 本番コード・単体テストコードを削除（src/main/, src/test/, build/）。common/, usecases/ は保護されます
./gradlew :berry-books-api-sdd-agile:cleanCode
```

* sdd-agile ではタスク分解・詳細設計を行わないため、`cleanCode` のみが対象です。
* 削除対象: 本番コード（src/main/）、単体テストコード（src/test/）、ビルド成果物（build/）。ディレクトリ構造は空で保持されます。
* 保護されるSPEC: `specs/baseline/common/`, `specs/baseline/usecases/`

## 📖 参考リンク

### Agent Skills

* [Agent Skills README](../../../agent_skills/jakarta-ee-api-agile/README.md) - 使い方ガイド
* [開発原則](../../../agent_skills/jakarta-ee-api-agile/principles/)
  * [architecture.md](../../../agent_skills/jakarta-ee-api-agile/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](../../../agent_skills/jakarta-ee-api-agile/principles/security.md) - セキュリティ標準
  * [common_rules.md](../../../agent_skills/jakarta-ee-api-agile/principles/common_rules.md) - 共通ルール

### Jakarta EE仕様

* [Jakarta EE 10 Platform](https://jakarta.ee/specifications/platform/10/)
* [Jakarta RESTful Web Services 3.1](https://jakarta.ee/specifications/restful-ws/3.1/)
* [Jakarta Persistence 3.1](https://jakarta.ee/specifications/persistence/3.1/)
* [JWT (JSON Web Token)](https://jwt.io/)
* [jjwt - Java JWT Library](https://github.com/jwtk/jjwt)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。
