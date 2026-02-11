# E2Eテスト生成インストラクション（アジャイル）

## パラメータ設定

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
usecase_folder: null        # オプション。指定時はそのユースケースのE2Eテストのみ生成
```

* 例
```yaml
project_root: "projects/sdd-agile/bookstore/berry-books-api"
spec_directory: "projects/sdd-agile/bookstore/berry-books-api/specs/baseline"
```

注意: パス区切りはOS環境に応じて調整する。以降、`{project_root}`, `{spec_directory}` はパラメータで設定した値に置き換える。

---

## 概要

このインストラクションは、アジャイル版のSPECに基づきE2Eテスト（End-to-End Test）を生成するためのものである。

重要な方針
* テストフレームワーク: JUnit 5 + REST Assured
* 外部APIモック: Wiremock（必須） - 外部マイクロサービスをスタブ化
* データベーステスト: DBUnit（必須） - テストデータのセットアップと検証
* テスト対象: usecases/{名}/behaviors.md のシナリオ（Gherkin記法）。E2EとしてAPI層を含む全体フローを検証する
* usecase_folder 未指定時: usecases/ 配下の各 behaviors.md を集約し、複数ユースケースにまたがるE2Eシナリオも生成可能とする
* アプリケーションサーバーが起動している状態でテストを実行する
* 実際のHTTPリクエスト/レスポンス、認証（JWT等）、DBアクセスを含む
* 既存テストの保護: 既存の JUnit + REST Assured テストコードは削除せず、差分を反映する

---

## 1. 読み込むドキュメント

* @agent_skills/jakarta-ee-api-agile/principles/ を確認する
* {spec_directory}/common/architecture_design.md - ベースURL、ポート、認証方式、テスト設定
* {spec_directory}/common/data_model.md, external_interface.md - 必要に応じて参照
* E2Eシナリオの参照元:
  * usecase_folder 指定時: {spec_directory}/usecases/{usecase_folder}/behaviors.md
  * 未指定時: {spec_directory}/usecases/ 配下の各 behaviors.md を参照し、E2E用シナリオを集約

---

## 2. E2Eテストの生成

### 2.1 依存関係（すべて必須）

* REST Assured（rest-assured, json-path, xml-path）- REST APIテスト
* JUnit 5 - テストフレームワーク
* Wiremock (`com.github.tomakehurst:wiremock-jre8:2.35.0`) - 外部APIモック（必須）
* DBUnit (`org.dbunit:dbunit:2.7.3`) - データベーステスト（必須）

### 2.2 JUnit 5 + REST Assured

* `src/test/java` 配下に通常のJUnitテストクラスを作成
* BaseE2ETest を継承（REST Assuredの設定、認証トークン管理、Wiremockサーバー起動、DBUnit初期化）
* @Tag("e2e") を付与
* テストメソッドは @Test アノテーションで実装
* behaviors.md のシナリオを参考に、Given-When-Then の流れでテストを記述
* 認証フロー（ログイン→トークン取得）のセットアップ、複数APIの連携、レスポンス検証、テストデータのセットアップ/クリーンアップを実装する

例:
```java
@Tag("e2e")
class OrderUseCaseE2ETest extends BaseE2ETest {
    @Test
    void testCreateOrderUseCase() {
        // Given: 認証、初期データ
        String token = login("user@example.com", "password");
        
        // When & Then: 複数API呼び出しと検証
        given().header("Authorization", "Bearer " + token)
            .when().post("/api/orders")
            .then().statusCode(201);
    }
}
```

### 2.3 外部APIのモック化（Wiremock - 必須）

重要: E2Eテストでは外部API（他のマイクロサービス）を実際に呼び出すのではなく、Wiremockでスタブ化する。

```java
@BeforeAll
static void setupWiremock() {
    wireMockServer = new WireMockServer(8089);
    wireMockServer.start();
    WireMock.configureFor("localhost", 8089);
    
    // 外部APIのスタブ
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

### 2.4 テストデータの準備（DBUnit - 必須）

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

テストデータファイル（`src/test/resources/dataset/e2e-test-data.xml`）:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<dataset>
    <CUSTOMER CUSTOMER_ID="1" EMAIL="alice@example.com" CUSTOMER_NAME="Alice" />
    <BOOK BOOK_ID="1" BOOK_NAME="Java入門" PRICE="3000" />
    <STOCK BOOK_ID="1" STOCK_COUNT="10" VERSION="1" />
</dataset>
```

---

## 3. テストの実行と評価

E2Eテストコード生成後、以下のステップを実施する:

### 3.1 前提条件

E2Eテスト実行前に以下を確認:

* アプリケーションサーバーが起動済みであること
  * E2Eテストは実際のHTTPリクエストを送信するため、サーバーが起動している必要がある

* テスト用データベースが利用可能であること
  * 本番DBは使用しない
  * テスト用DBが設定されていることを確認

### 3.2 テスト実行

Gradleタスクを使用してE2Eテストを実行:

```bash
cd {project_root}
./gradlew e2eTest
```

* `e2eTest` タスクは、@Tag("e2e") が付与されたテストを実行する
* プロジェクトのbuild.gradleに定義されたタスク名に従うこと

### 3.3 テスト評価

テスト実行後、@agent_skills/jakarta-ee-api-agile/instructions/test_evaluation.md を使用して結果を評価する:

```yaml
project_root: "{project_root}"
jacoco_reports_dir: "{project_root}/build/reports/jacoco/e2eTest"
test_type: "e2e"
spec_directory: "{spec_directory}"
```

---

## 4. 参考

* [it_generation.md](it_generation.md) - 結合テスト生成
* ウォーターフォール版: @agent_skills/jakarta-ee-api-base/instructions/e2e_test_generation.md
