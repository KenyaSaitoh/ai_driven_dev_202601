# E2Eテスト生成インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
```

* 例
```yaml
project_root: "projects/sdd-wf/bookstore/back-office-api"
spec_directory: "projects/sdd-wf/bookstore/back-office-api/specs/baseline"
```

注意
* パス区切りはOS環境に応じて調整する（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換える
* 以降、`{spec_directory}` と表記されている箇所は、上記で設定した値に置き換える

---

## 概要

このインストラクションは、REST API のE2Eテスト（End-to-End Test）を生成するためのものである

重要な方針
* 実装完了後にE2Eテストを生成する（code_generation.mdの次のステップ）
* テストフレームワーク: JUnit 5 + REST Assured
* 外部APIモック: Wiremock（必須） - 外部マイクロサービスをスタブ化
* データベーステスト: DBUnit（必須） - テストデータのセットアップとDB更新の検証
* テスト対象:
  * requirements/requirements.md（EARS記法）: システム全体の機能要件を理解
  * basic_design/{ドメイン名}/functional_design.md: API仕様（エンドポイント、リクエスト/レスポンス形式）
  * basic_design/{ドメイン名}/behaviors.md（Gherkin記法）: 各ドメインの振る舞いとDB更新を確認
* 複数機能間の連携、実際のHTTPリクエスト/レスポンス、実際のDBアクセスとDB更新の検証を含む
* アプリケーションサーバーが起動している状態でテストを実行
* 既存テストの扱い（重要）:
  * 既存の JUnit + REST Assured テストコードは削除せずに保護する
  * ファイルをゼロから作り直すのではなく、既存の内容を尊重して必要なテストケースのみを追加・修正する
  * 新規テストファイルが必要な場合のみ、新規作成する

---

## 1. SPECの読み込みと理解

パラメータで指定されたプロジェクト情報に基づいて、以下の設計ドキュメントを読み込んで分析する

### 1.1 Agent Skillsルール（最優先で確認）

* @agent_skills/jakarta-ee-api-base/principles/ - Jakarta EE開発の原則、アーキテクチャ標準、品質基準、セキュリティ標準を確認する
  * このフォルダ配下の原則ドキュメントを読み込み、共通ルールを遵守すること
  * 重要: E2Eテスト生成においても、ルールドキュメントに記載されたすべてのルールを遵守すること
  * 注意: Agent Skills配下のルールは全プロジェクト共通。プロジェクト固有のルールがある場合は `{project_root}/principles/` も確認すること

### 1.2 要件定義書（EARS記法）

以下のファイルを読み込み、システム全体の機能要件を理解する

* {spec_directory}/requirements/requirements.md - 機能要件（EARS記法）を確認する
  * WHEN/IF-THEN/WHERE形式で記述された機能要件
  * システム全体の機能仕様
  * 認証・認可、各種業務機能の要件
  * エラーハンドリング要件
  * 注: プロジェクトルートが決まれば、要件定義書のパスは `{spec_directory}/requirements/requirements.md` で決め打ち可能

### 1.3 基本設計の仕様

以下のファイルを読み込み、システム全体の設計とテスト設計を理解する

* {spec_directory}/basic_design/common/architecture_design.md - 技術スタック、パッケージ構造、テスト設定を確認する
  * ベースURL、ポート番号
  * 認証方式（JWT等）
  * テストフレームワーク設定

* {spec_directory}/basic_design/common/functional_design.md - 共通API仕様を確認する

* {spec_directory}/basic_design/{ドメイン名}/functional_design.md - 各ドメインのAPI仕様を確認する
  * 全てのAPI仕様
  * エンドポイント一覧
  * リクエスト/レスポンス形式

* {spec_directory}/basic_design/{ドメイン名}/behaviors.md - 各ドメインの振る舞いとDB更新を確認する
  * Gherkin記法で記述されたシナリオ
  * Service層以下の結合テスト仕様
  * DBの初期状態とテストデータ
  * DB更新の期待値（在庫減少、データ追加/更新/削除など）
  * 注: プロジェクトルートが決まれば、基本設計書のパスは `{spec_directory}/basic_design/` で決め打ち可能

---

## 2. REST Assured のセットアップ

### 2.1 依存関係

E2Eテスト生成に必要なライブラリ（すべて必須）:

* REST Assured（rest-assured, json-path, xml-path）- REST APIテスト
* JUnit 5: `org.junit.jupiter:junit-jupiter:5.10.0` - テストフレームワーク
* JUnit Platform: `org.junit.platform:junit-platform-launcher:1.10.0` - テスト実行エンジン
* Wiremock (`com.github.tomakehurst:wiremock-jre8:2.35.0`) - 外部APIモック（必須）
* DBUnit (`org.dbunit:dbunit:2.7.3`) - データベーステスト（必須）
* Jackson - JSON処理

* E2Eテストクラスには `@Tag("e2e")` を付与し、通常の単体テスト実行から分離する

依存関係の追加方法:
* まず、対象プロジェクトの `build.gradle` を確認する
* プロジェクト内に `build.gradle` が存在しない、または依存関係が定義されていない場合:
  * 親ディレクトリやプロジェクトルートの `build.gradle` を探索する
  * 共通のビルドファイルで `subprojects` ブロックや全プロジェクト共通設定が定義されている可能性がある
  * 見つかった場合、そちらに依存関係を追加する
* `e2eTest` タスクについても同様に、既存の定義を確認してから追加の要否を判断する

### 2.2 ベースクラスのポイント

* `@Tag("e2e")` の abstract ベースクラスを用意する
* @BeforeAll: architecture_design.md のベースURL・ポートに合わせ、RestAssured.baseURI/basePath と RequestSpecBuilder で Content-Type/Accept を設定
* 認証が必要なAPI向けに、ログインAPIを呼びトークン（cookie または header）を返す login(employeeCode, password) を用意する
* Wiremockサーバーの起動: @BeforeAll で WireMockServer を起動し、外部API（customer-hub-api等）をスタブ化する
* DBUnit初期化: テストデータのセットアップ用にDBUnitのIDatabaseConnectionを準備する

---

## 3. E2Eテストケース生成

### 3.1 テストケース設計方針（共通）

* requirements/behaviors.md のシナリオに基づいてテストを生成
* 複数APIにまたがるE2Eのフローをテスト
* 実際のDBアクセスを含む（テストデータの準備と検証）
* HTTPステータスコード、レスポンスボディ、ヘッダーの検証
* @Tag("e2e") を付与し、e2eTest タスクで実行されるようにする

### 3.2 JUnit 5 + REST Assured

* `src/test/java` 配下に通常のJUnitテストクラスを作成
* BaseE2ETest を継承（REST Assuredの設定、認証トークン管理）
* @Tag("e2e") を付与
* テストメソッドは @Test アノテーションで実装
* behaviors.md のシナリオを参考に、Given-When-Then の流れでテストを記述

例:
```java
@Tag("e2e")
class OrderE2ETest extends BaseE2ETest {
    @Test
    void testCreateOrder_E2E() {
        // Given: 認証トークン取得、初期データ準備
        String token = login("user@example.com", "password");
        
        // When: API呼び出し
        Response response = given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body(orderRequest)
            .when()
            .post("/api/orders");
        
        // Then: レスポンス検証
        response.then()
            .statusCode(201)
            .body("orderId", notNullValue());
    }
}
```

### 3.3 外部APIのモック化（Wiremock - 必須）

重要: E2Eテストでは外部API（他のマイクロサービス）を実際に呼び出すのではなく、Wiremockでスタブ化する。

```java
@BeforeAll
static void setupWiremock() {
    wireMockServer = new WireMockServer(8089);
    wireMockServer.start();
    WireMock.configureFor("localhost", 8089);
    
    // 外部API（customer-hub-api）のスタブ
    stubFor(get(urlEqualTo("/api/customers/1"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"customerId\":1,\"name\":\"Alice\"}")));
}

@AfterAll
static void teardownWiremock() {
    if (wireMockServer != null) {
        wireMockServer.stop();
    }
}
```

* 外部APIのエンドポイントを特定し、必要なレスポンスをスタブ化する
* functional_design.md から外部システム連携の仕様を確認する
* 正常系だけでなく、エラーレスポンス（404, 500等）もスタブ化する

### 3.2 テストケースのポイント

* 1シナリオ＝1テストクラス、BaseE2ETest を継承。複数APIにまたがるフローは @Order で順序付け可能
* Given: @BeforeAll で login() によりトークン取得。必要に応じて GET で初期状態（在庫数など）を取得
* When: given(requestSpec).queryParam/body(...).when().get/post/put/delete(エンドポイント).then() で HTTP リクエスト送信
* Then: .statusCode(期待値)、.body("jsonPath", Matcher) でレスポンス検証。必要なら .extract().path("jsonPath") で値を取り次のテストに渡す
* functional_design.md のエンドポイント・リクエスト形式に合わせてパス・ボディを組み立てる

### 3.3 認証が必要なAPI

* 認証なしで呼ぶと 401 になることを statusCode(401) で検証。認証ありでは cookie("jwtToken", token) または header("Authorization", "Bearer " + token) を付与して 200 とボディを検証する

### 3.4 エラーケース

* 404: 存在しないIDで GET し statusCode(404)、body("message", ...) を検証
* 400: 不正なボディで POST し statusCode(400) を検証

---

## 4. テストデータの準備（DBUnit - 必須）

### 4.1 DBUnitによるデータセットアップ

重要: E2EテストではDBUnitを使用してテストデータを準備する。

```java
private static IDatabaseConnection connection;
private static IDataSet dataSet;

@BeforeAll
static void setupDatabase() throws Exception {
    // DBUnit接続
    Connection jdbcConnection = DriverManager.getConnection(
        "jdbc:hsqldb:hsql://localhost:9001/testdb", "SA", "");
    connection = new DatabaseConnection(jdbcConnection);
    
    // テストデータのロード
    dataSet = new FlatXmlDataSetBuilder()
        .build(BaseE2ETest.class.getResourceAsStream("/dataset/e2e-test-data.xml"));
    
    // データベースにテストデータを投入
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
}

@AfterAll
static void cleanupDatabase() throws Exception {
    if (connection != null) {
        // テストデータのクリーンアップ
        DatabaseOperation.DELETE_ALL.execute(connection, dataSet);
        connection.close();
    }
}
```

### 4.2 テストデータファイル（XML）

`src/test/resources/dataset/e2e-test-data.xml` にテストデータを定義:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<dataset>
    <CUSTOMER CUSTOMER_ID="1" EMAIL="alice@example.com" CUSTOMER_NAME="Alice" />
    <BOOK BOOK_ID="1" BOOK_NAME="Java入門" PRICE="3000" PUBLISHER_ID="1" CATEGORY_ID="1" />
    <STOCK BOOK_ID="1" STOCK_COUNT="10" VERSION="1" />
</dataset>
```

### 4.3 DB更新の検証

重要: E2Eテストでは、API呼び出し後のDB更新をDBUnitで検証する。basic_design/{ドメイン名}/behaviors.md の期待値に基づいて検証を実装する。

テスト実行後、DBUnitを使用してデータベースの状態を検証:

```java
@Test
void testOrderCreation_UpdatesStock() throws Exception {
    // Given: 初期データセットアップ（behaviors.mdのデータセットを参照）
    // 初期在庫数: 10
    
    // When: 注文作成
    given().body(orderRequest).post("/api/orders").then().statusCode(201);
    
    // Then: 在庫が減少していることを検証（behaviors.mdの期待値に基づく）
    ITable actualTable = connection.createQueryTable("STOCK",
        "SELECT * FROM STOCK WHERE BOOK_ID = 1");
    assertEquals(8, actualTable.getValue(0, "STOCK_COUNT")); // 10 - 2 = 8
}
```

behaviors.md の例:
```gherkin
Scenario: 書籍を検索して注文を作成する
  Given DBに以下の在庫が存在する:
    | BOOK_ID | STOCK_COUNT | VERSION |
    | 1       | 10          | 1       |
  
  When 書籍ID=1を2冊注文する
  
  Then 注文が作成される
  And DBの在庫が更新される:
    | BOOK_ID | STOCK_COUNT | VERSION |
    | 1       | 8           | 2       |
```

上記シナリオに基づいて、DBUnitでSTOCK_COUNTが8に、VERSIONが2に更新されていることを検証する。

### 4.4 テストデータ管理のベストプラクティス

@agent_skills/jakarta-ee-api-base/principles/architecture.md の「9.4 テストデータ管理」を参照する。

---

## 5. 要件定義書・基本設計書からのテストケース生成

### 5.1 要件定義書（EARS記法）の読み取り

requirements/requirements.md は EARS記法で記述されている。以下の形式で機能要件を理解する:

* WHEN: トリガー条件（「〜したとき」）
* IF-THEN: 条件分岐（「もし〜ならば、〜しなければならない」）
* WHERE: 環境条件（「〜の場合」）

例:
```
FR-BOOK-001: WHEN 書籍一覧取得リクエストが送信されたとき、システムは論理削除されていない全書籍情報をカテゴリ、出版社、在庫情報とともに返さなければならない。

FR-BOOK-005: IF 指定された書籍IDが存在しない場合、THEN システムは404 Not Foundエラーを返し、"書籍が見つかりません"というメッセージを表示しなければならない。
```

### 5.2 基本設計書（behaviors.md）の読み取り

basic_design/{ドメイン名}/behaviors.md は Gherkin 記法で記述されている。@agent_skills/jakarta-ee-api-base/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照の上、各シナリオから Given/When/Then を抽出する。

重要: behaviors.mdには以下の情報が含まれる:
* DBの初期状態（Given: DBに以下のデータが存在する）
* テストデータファイルのパス（データセット: /datasets/xxx.xml）
* DB更新の期待値（Then: DBの状態は以下のように更新される）

### 5.3 E2Eテストの設計方針

requirements/requirements.md（EARS記法）と basic_design/{ドメイン名}/behaviors.md（Gherkin記法）を組み合わせてE2Eテストを生成する:

1. requirements.md から機能要件を理解
   * 正常系の要件（WHEN）
   * 異常系の要件（IF-THEN）
   * 環境条件（WHERE）

2. functional_design.md からAPI仕様を確認
   * エンドポイント、HTTPメソッド
   * リクエスト/レスポンス形式
   * ステータスコード

3. behaviors.md からDB更新を確認
   * 初期データのセットアップ
   * DB更新の期待値（在庫減少、データ追加/更新/削除など）

### 5.4 シナリオとテストの対応

* Given: login() でトークン取得。DBUnit で初期データをセットアップ（behaviors.md のデータセットを参照）
* When: POST/GET 等でエンドポイントを呼び出し（functional_design.md の形式に合わせる）
* Then: 
  * statusCode(201/200) でステータスコードを検証（requirements.md の要件に合わせる）
  * body で注文ID・在庫数などレスポンスボディを検証
  * DBUnit でDB更新を検証（behaviors.md の期待値に合わせる）
    * 例: 在庫減少は ITable で STOCK テーブルを取得して initialStock - 注文数 と比較する

---

## 6. 注意事項

### 6.1 テスト実行環境

* E2Eテストはアプリケーションサーバー起動済みの状態で実行する。テスト用DBを使用し、本番DBは使用しない。テスト後はデータをクリーンアップする。

### 6.2 テストの安定性

* ネットワーク遅延を考慮してタイムアウトを設定。テスト間の依存関係を避ける。


---

## 7. テストの実行と評価

E2Eテストコード生成後、以下のステップを実施する:

### 7.1 前提条件

E2Eテスト実行前に以下を確認:

* アプリケーションサーバーが起動済みであること
  * E2Eテストは実際のHTTPリクエストを送信するため、サーバーが起動している必要がある

* テスト用データベースが利用可能であること
  * 本番DBは使用しない
  * テスト用DBが設定されていることを確認

### 7.2 テスト実行

Gradleタスクを使用してE2Eテストを実行:

```bash
cd {project_root}
./gradlew e2eTest
```

* `e2eTest` タスクは、@Tag("e2e") が付与されたテストを実行する
* プロジェクトのbuild.gradleに定義されたタスク名に従うこと

### 7.3 テスト評価

テスト実行後、@agent_skills/jakarta-ee-api-base/instructions/test_evaluation.md を使用して結果を評価する:

```yaml
project_root: "{project_root}"
jacoco_reports_dir: "{project_root}/build/reports/jacoco/e2eTest"
test_type: "e2e"
spec_directory: "{spec_directory}"
```

---

## 8. 参考資料

* REST Assured公式ドキュメント: https://rest-assured.io/
* JUnit 5公式ドキュメント: https://junit.org/junit5/
* DBUnit公式ドキュメント: http://dbunit.sourceforge.net/
* requirements/requirements.md - 機能要件（EARS記法）
* basic_design/{ドメイン名}/functional_design.md - API仕様
* basic_design/{ドメイン名}/behaviors.md - 振る舞い仕様とDB更新（Gherkin記法）
* basic_design/common/architecture_design.md - システム構成
