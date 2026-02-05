# berry-books-api-sdd-wf プロジェクト

## 📖 概要

Jakarta EE 10とJAX-RS (Jakarta RESTful Web Services) 3.1を使用したオンライン書店「Berry Books」のREST APIアプリケーションです。
書籍検索、JWT認証、注文処理などのEC機能をREST APIとして提供します。

> Note: このプロジェクトは仕様駆動開発（SDD: Specification-Driven Development）の研修用プロジェクトです。

> SDDとは:
> - 詳細な仕様書（specs/）に基づいて、段階的にコードを生成する手法
> - AIを活用して、ドメイン単位で段階的に実装を進める
> - 憲章（principles/）に定められた設計原則とベストプラクティスに従う
> - 汎用Agent Skills (`agent_skills/jakarta-ee-api-base/`) を使用した開発

## 🤖 Agent Skillsを使った開発

このプロジェクトは、汎用的な Jakarta EE マイクロサービス開発 Agent Skills を使用して開発します。

開発は以下の6段階プロセスで進めます（ドメイン単位）：

```
ステップ1: 基本設計（ドメイン構造決定）← AIと対話しながら
    ↓
ステップ2: 詳細設計（ドメイン単位）← AIと対話しながら
    ↓
ステップ3: コード生成（ドメイン単位：実装コード + 単体テスト）
    ↓
ステップ4: 単体テスト実行評価（テスト実行 → カバレッジ分析 → フィードバック）
    ↓
ステップ5: 結合テスト生成（basic_design/{domain}/behaviors.md → JUnit + Weld SE）
    ↓
ステップ6: E2Eテスト生成（requirements/behaviors.md → REST Assured）
```

**ドメイン構成:**
- `common/` - 共通ドメイン（JWT認証、エラーハンドリング等。最優先実装）
- `books_proxy/` - 書籍プロキシドメイン（back-office-apiへのプロキシ）
- `orders/` - 注文管理ドメイン
- `images/` - 画像管理ドメイン

---

### 📋 開発フロー

#### ステップ1: 基本設計（プロジェクト開始時に1回）

requirements.mdから、ドメイン単位の仕様書をAIと対話しながら作成します。

```
@agent_skills/jakarta-ee-api-base/instructions/basic_design.md

仕様書を作成してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/berry-books-api
* spec_directory: projects/sdd-wf/bookstore/berry-books-api/specs/baseline
```

* 対話の流れ:
  1. 既存資料（EXCEL、Word等）の有無を確認します
  2. 既存資料がある場合は、Markdown形式に変換します
  3. ドメイン構成を決定します（common, books_proxy, orders, images）
  4. テンプレートを展開し、各ドメインの仕様書を対話的に作成します
  5. `specs/baseline/basic_design/{domain}/*.md` が生成されます

* 生成されるファイル: 
  ```
  specs/baseline/basic_design/
  ├── common/                  # 共通ドメイン（最優先実装）
  │   ├── architecture_design.md
  │   ├── data_model.md
  │   ├── external_interface.md
  │   ├── functional_design.md
  │   └── behaviors.md
  ├── books_proxy/             # 書籍プロキシドメイン
  │   ├── functional_design.md
  │   └── behaviors.md
  ├── orders/                  # 注文管理ドメイン
  │   ├── functional_design.md
  │   └── behaviors.md
  └── images/                  # 画像管理ドメイン
      ├── functional_design.md
      └── behaviors.md
  ```

---

#### ステップ2: 詳細設計（ドメイン単位、commonから順に実施）

**重要**: commonドメインを最優先で実施してください（他のドメインはcommonに依存）。

コマンドテンプレート（commonドメインの例）:

```
@agent_skills/jakarta-ee-api-base/instructions/detailed_design.md

commonドメインの詳細設計書を作成してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/berry-books-api
* spec_directory: projects/sdd-wf/bookstore/berry-books-api/specs/baseline
* target_type: [tasks/tasks.mdで確認したタスクID]
```

使用例:

```
@agent_skills/jakarta-ee-api-base/instructions/detailed_design.md

commonドメインの詳細設計書を作成してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/berry-books-api
* spec_directory: projects/sdd-wf/bookstore/berry-books-api/specs/baseline
* target_domain: common
```

対話の流れ:
1. AIがSPEC（`basic_design/{target_domain}/`）を読み込み、理解した内容を説明します
2. AIが不明点を質問します
3. あなたが回答します
4. `specs/baseline/detailed_design/{target_domain}/detailed_design.md` と `behaviors.md` が生成されます

注意:
* `common`ドメインを最優先で実行してください
* 他のドメインは`common`に依存する可能性が高いため、commonの詳細設計完了後に実行します
* 詳細設計は対話的なプロセスです。AIの質問には必ず回答してください

---

#### ステップ3: コード生成（ドメイン単位、commonから順に実施）

詳細設計書からドメイン単位でコードを生成します（実装コード + 単体テスト）。

**重要**: `common`ドメインを最優先で実装してください（他のドメインはcommonに依存）。

> 単体テストの方針: ドメイン内のコンポーネント間は実際の連携をテスト。ドメイン外の依存関係のみモック化。

コマンドテンプレート:

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

[ドメイン名]ドメインを実装してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/berry-books-api
* spec_directory: projects/sdd-wf/bookstore/berry-books-api/specs/baseline
* target_domain: [ドメイン名]
```

使用例（commonドメイン）:

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

commonドメインを実装してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/berry-books-api
* spec_directory: projects/sdd-wf/bookstore/berry-books-api/specs/baseline
* target_domain: common
```

使用例（books_proxyドメイン）:

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

books_proxyドメインを実装してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/berry-books-api
* spec_directory: projects/sdd-wf/bookstore/berry-books-api/specs/baseline
* target_domain: books_proxy
```

注意:
* `common`ドメインを最優先で実装してください
* 各ドメインは`common`に依存する可能性が高いため、commonの実装完了後に実行します

---

#### ステップ4: 単体テスト実行評価（ドメイン単位）

実装コードと単体テストが生成されたら、ドメイン単位でテストを実行してカバレッジを評価します。

```
@agent_skills/jakarta-ee-api-base/instructions/unit_test_execution.md

単体テストを実行して評価してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/berry-books-api
* spec_directory: projects/sdd-wf/bookstore/berry-books-api/specs/baseline
* target_domain: books_proxy
```

AIが：
1. 📋 実行可能な単体テストコードを確認
2. 🚀 テストを実行
3. 📊 カバレッジを集計
4. 🔎 80%未満のカバレッジの箇所を特定
5. ✏️ 修正案を提示または修正実施

カバレッジ不足がある場合は、ステップ2（詳細設計）に戻ってフィードバックします。

---

#### ステップ5: 結合テスト生成（全ドメイン完了後）

すべてのドメインの実装が完了したら、結合テスト（JUnit 5 + Weld SE）を生成します。

```
@agent_skills/jakarta-ee-api-base/instructions/integration_test_generation.md

結合テストを生成してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/berry-books-api
* spec_directory: projects/sdd-wf/bookstore/berry-books-api/specs/baseline
* target_domains: all
```

AIが：
1. 📄 `basic_design/{domain}/behaviors.md` （Gherkin形式のビヘイビア定義）を読み込み
2. 🧪 各ドメインのシナリオを結合テストケースに変換
3. 🚀 JUnit 5 + Weld SEを使用したテストコードを生成

---

#### ステップ6: E2Eテスト生成（全ドメイン完了後）

すべてのドメインの実装が完了したら、E2Eテスト（REST Assured）を生成します。

```
@agent_skills/jakarta-ee-api-base/instructions/e2e_test_generation.md

E2Eテストを生成してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/berry-books-api
* spec_directory: projects/sdd-wf/bookstore/berry-books-api/specs/baseline
```

AIが：
1. 📄 requirements/behaviors.md（E2Eテストシナリオ）を読み込む
2. 🧪 REST Assured を使用したE2Eテストを生成
3. 🏷️ `@Tag("e2e")` でE2Eテストを分離

---

### 🔄 基本設計変更対応（手戻り・拡張案件）

結合テストやE2Eテストで不具合が見つかり、基本設計に戻る必要がある場合や、拡張案件で新機能を追加する場合に使用します。

#### 使用方法

1. **ドメイン単位で基本設計SPECを更新**
   ```bash
   vim specs/baseline/basic_design/common/functional_design.md
   vim specs/baseline/basic_design/books_proxy/functional_design.md
   ```

2. **CHANGES.mdを作成して変更内容を記載**
   ```bash
   cp agent_skills/jakarta-ee-api-base/templates/basic_design/CHANGES_template.md \
      specs/baseline/basic_design/CHANGES.md
   vim specs/baseline/basic_design/CHANGES.md
   ```

3. **変更対応を実行**
   ```
   @agent_skills/jakarta-ee-api-base/instructions/basic_design_change.md
   
   基本設計の変更を適用してください
   
   パラメータ:
   * project_root: projects/sdd-wf/bookstore/berry-books-api
   * spec_directory: projects/sdd-wf/bookstore/berry-books-api/specs/baseline
   ```

AIが：
1. 📄 CHANGES.md（変更差分ファイル）を読み込み
2. 🔍 変更の影響を受けるドメインを識別
3. 🎯 既存の指示書を呼び出して、影響を受けるドメインの設計・コード・テストを更新
4. ✅ すべての変更適用後、CHANGES.mdをアーカイブ

#### ディレクトリ構造

```
specs/baseline/basic_design/
  ├── common/                   # 共通ドメイン（マスター、自由に編集）
  │   ├── functional_design.md
  │   ├── data_model.md
  │   └── behaviors.md
  ├── books_proxy/              # 書籍プロキシドメイン（マスター、自由に編集）
  │   ├── functional_design.md
  │   └── behaviors.md
  ├── CHANGES.md                # アクティブな変更（未適用）
  └── changes_archive/          # 履歴
      ├── 20260118_order_cancel.md
      └── 20260125_image_upload.md
```

---

### 📚 詳細情報

詳細は `@agent_skills/jakarta-ee-api-base/README.md` を参照してください。

#### 開発原則

このプロジェクトは、以下の原則に従って開発されます：

* 場所: `@agent_skills/jakarta-ee-api-base/principles/`
  * [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](../../../agent_skills/jakarta-ee-api-base/principles/security.md) - セキュリティ標準
  * [common_rules.md](../../../agent_skills/jakarta-ee-api-base/principles/common_rules.md) - 共通ルール

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

```
berry-books-api/
├── specs/                          # 仕様書（SDD）
│   ├── baseline/
│   │   ├── requirements/           # システム要件
│   │   │   ├── requirements.md    # 要件定義書
│   │   │   └── behaviors.md       # E2Eテスト用（要件を外形的に捉えた振る舞い）
│   │   ├── basic_design/           # 基本設計SPEC（ドメイン単位）
│   │   │   ├── common/             # 共通ドメイン
│   │   │   │   ├── architecture_design.md
│   │   │   │   ├── functional_design.md
│   │   │   │   ├── data_model.md
│   │   │   │   ├── external_interface.md
│   │   │   │   └── behaviors.md   # 結合テスト用
│   │   │   ├── books_proxy/        # 書籍プロキシドメイン
│   │   │   │   ├── functional_design.md
│   │   │   │   └── behaviors.md
│   │   │   ├── orders/             # 注文管理ドメイン
│   │   │   │   ├── functional_design.md
│   │   │   │   └── behaviors.md
│   │   │   └── images/             # 画像管理ドメイン
│   │   │       ├── functional_design.md
│   │   │       └── behaviors.md
│   │   └── detailed_design/        # 詳細設計SPEC（ドメイン単位）
│   │       ├── common/
│   │       │   ├── detailed_design.md
│   │       │   └── behaviors.md   # 単体テスト用
│   │       ├── books_proxy/
│   │       │   ├── detailed_design.md
│   │       │   └── behaviors.md
│   │       ├── orders/
│   │       │   ├── detailed_design.md
│   │       │   └── behaviors.md
│   │       └── images/
│   │           ├── detailed_design.md
│   │           └── behaviors.md
│   └── enhancements/               # 機能拡張仕様
├── principles/                     # 開発憲章
│   └── constitution.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── pro/kensait/berrybooks/
│   │   │       ├── api/              # JAX-RS Resources
│   │   │       │   ├── dto/          # API DTOs (Records)
│   │   │       │   └── exception/    # Exception Mappers
│   │   │       ├── security/         # JWT, AuthenContext（共通ドメイン）
│   │   │       ├── service/          # Business Logic
│   │   │       │   ├── order/        # 注文処理（独自実装）
│   │   │       │   └── delivery/     # 配送料金計算
│   │   │       ├── dao/              # Data Access Objects
│   │   │       ├── entity/           # JPA Entities（注文関連のみ）
│   │   │       ├── external/         # External API Clients
│   │   │       │   ├── BackOfficeRestClient.java
│   │   │       │   ├── CustomerHubRestClient.java
│   │   │       │   └── dto/          # 外部API用DTO
│   │   │       └── util/             # Utilities（共通ドメイン）
│   │   ├── resources/
│   │   │   ├── META-INF/
│   │   │   │   ├── persistence.xml
│   │   │   │   └── microprofile-config.properties
│   │   │   ├── db/
│   │   │   │   ├── schema.sql       # 注文テーブルのみ
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
./gradlew :berry-books-api-sdd-wf:setupHsqldb

# 2. プロジェクトをビルド
./gradlew :berry-books-api-sdd-wf:war

# 3. プロジェクトをデプロイ
./gradlew :berry-books-api-sdd-wf:deploy
```

> 重要: `setupHsqldb`を実行すると、`src/main/resources/db/schema.sql`と`sample_data.sql`が実行されます。

### ⑤ プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# プロジェクトをアンデプロイ
./gradlew :berry-books-api-sdd-wf:undeploy
```

### ⑥ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
./gradlew :berry-books-api-sdd-wf:war
./gradlew :berry-books-api-sdd-wf:deploy
```

## 📍 APIエンドポイント

デプロイ後、以下のベースURLでAPIにアクセスできます：

* ベースURL: http://localhost:8080/berry-books-api-sdd-wf/api
* ウェルカムページ: http://localhost:8080/berry-books-api-sdd-wf/

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
back-office-api.base-url=http://localhost:8080/back-office-api-sdd/api
customer-hub-api.base-url=http://localhost:8080/customer-hub-api/api/customers
```

> 重要: 本システムは以下の外部APIに依存します：
> - back-office-api-sdd: 書籍・在庫・カテゴリ管理
> - customer-hub-api: 顧客管理

## 📝 APIの使用例（curl）

### 1. 新規登録

```bash
curl -X POST http://localhost:8080/berry-books-api-sdd-wf/api/auth/register \
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
curl -X POST http://localhost:8080/berry-books-api-sdd-wf/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@gmail.com",
    "password": "password"
  }' \
  -c cookies.txt
```

### 3. 全書籍取得

```bash
curl -X GET http://localhost:8080/berry-books-api-sdd-wf/api/books
```

### 4. 書籍検索（カテゴリとキーワード）

```bash
curl -X GET "http://localhost:8080/berry-books-api-sdd-wf/api/books/search?categoryId=1&keyword=Java"
```

### 5. 現在のログインユーザー情報取得

```bash
curl -X GET http://localhost:8080/berry-books-api-sdd-wf/api/auth/me \
  -b cookies.txt
```

### 6. 注文作成

```bash
curl -X POST http://localhost:8080/berry-books-api-sdd-wf/api/orders \
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
curl -X GET http://localhost:8080/berry-books-api-sdd-wf/api/orders/history \
  -b cookies.txt
```

### 8. ログアウト

```bash
curl -X POST http://localhost:8080/berry-books-api-sdd-wf/api/auth/logout \
  -b cookies.txt \
  -c cookies.txt
```

## 🧪 テスト

### テストの実行

このプロジェクトには、サービス層のユニットテストが含まれています。テストはJUnit 5とMockitoを使用して実装されています。

#### すべてのテストを実行

```bash
./gradlew :berry-books-api-sdd-wf:test
```

#### 特定のテストクラスを実行

```bash
# AddressUtilのテストのみを実行
./gradlew :berry-books-api-sdd-wf:test --tests "*AddressUtilTest"

# DeliveryFeeServiceのテストのみを実行
./gradlew :berry-books-api-sdd-wf:test --tests "*DeliveryFeeServiceTest"
```

#### テストの継続的実行（変更検知）

```bash
./gradlew :berry-books-api-sdd-wf:test --continuous
```

### テストレポートの確認

テスト実行後、HTMLレポートが生成されます：

```
projects/sdd-wf/bookstore/berry-books-api-wf/build/reports/tests/test/index.html
```

ブラウザで開くとテスト結果の詳細が確認できます。

### テストカバレッジの確認（JaCoCo）

```bash
# テストカバレッジレポートを生成
./gradlew :berry-books-api-sdd-wf:jacocoTestReport

# カバレッジレポートの場所
# projects/sdd-wf/bookstore/berry-books-api-wf/build/reports/jacoco/test/html/index.html
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
./gradlew :berry-books-api-sdd-wf:undeploy
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
./gradlew :berry-books-api-sdd-wf:setupHsqldb
```

## 🧹 SDD成果物のクリーンアップ

仕様駆動開発により何度でも再実装できます。詳細は [ルートREADMEのSDDクリーンアップ節](../../../README.md#仕様駆動開発sddプロジェクトの成果物クリーンアップ) を参照してください。

```bash
# タスクファイルのみ削除
./gradlew :berry-books-api-sdd-wf:cleanTasks

# 詳細設計SPECのみ削除
./gradlew :berry-books-api-sdd-wf:cleanDetailedDesign

# 本番コード・単体テストコードを削除（src/main/, src/test/, build/）
./gradlew :berry-books-api-sdd-wf:cleanCode

# すべて削除（requirements/, basic_design/ は保護）
./gradlew :berry-books-api-sdd-wf:cleanAllSdd
```

* cleanCode の削除対象: 本番コード（src/main/）、単体テストコード（src/test/）、ビルド成果物（build/）。ディレクトリ構造は空で保持されます。
* 保護されるSPEC: `specs/baseline/requirements/`, `specs/baseline/basic_design/`

## 📖 参考リンク

### Agent Skills

* [Agent Skills README](../../../agent_skills/jakarta-ee-api-base/README.md) - 使い方ガイド
* [開発原則](../../../agent_skills/jakarta-ee-api-base/principles/)
  * [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](../../../agent_skills/jakarta-ee-api-base/principles/security.md) - セキュリティ標準
  * [common_rules.md](../../../agent_skills/jakarta-ee-api-base/principles/common_rules.md) - 共通ルール

### Jakarta EE仕様

* [Jakarta EE 10 Platform](https://jakarta.ee/specifications/platform/10/)
* [Jakarta RESTful Web Services 3.1](https://jakarta.ee/specifications/restful-ws/3.1/)
* [Jakarta Persistence 3.1](https://jakarta.ee/specifications/persistence/3.1/)
* [JWT (JSON Web Token)](https://jwt.io/)
* [jjwt - Java JWT Library](https://github.com/jwtk/jjwt)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。
