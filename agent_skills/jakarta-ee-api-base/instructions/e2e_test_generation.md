# E2Eテスト生成インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
```

* 例
```yaml
project_root: "projects/sdd/bookstore/back-office-api-sdd"
spec_directory: "projects/sdd/bookstore/back-office-api-sdd/specs/baseline"
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
* テストフレームワーク: REST Assured を使用
* テスト対象: basic_design/behaviors.md（E2Eテスト用）のシナリオ
* 複数API間の連携、実際のHTTPリクエスト/レスポンス、実際のDBアクセスを含む
* アプリケーションサーバーが起動している状態でテストを実行

---

## 1. SPECの読み込みと理解

パラメータで指定されたプロジェクト情報に基づいて、以下の設計ドキュメントを読み込んで分析する

### 1.1 Agent Skillsルール（最優先で確認）

* @agent_skills/jakarta-ee-api-base/principles/ - Jakarta EE開発の原則、テスト標準を確認する
  * このフォルダ配下の原則ドキュメントを読み込み、共通ルールを遵守すること

### 1.2 基本設計の仕様

以下のファイルを読み込み、システム全体の設計を理解する

* {spec_directory}/basic_design/architecture_design.md - 技術スタック、パッケージ構造、テスト設定を確認する
  * ベースURL、ポート番号
  * 認証方式（JWT等）
  * テストフレームワーク設定

* {spec_directory}/basic_design/functional_design.md - システム全体の機能設計（全APIを含む）を確認する
  * 全てのAPI仕様
  * エンドポイント一覧
  * リクエスト/レスポンス形式

* {spec_directory}/basic_design/behaviors.md - E2Eテストシナリオを確認する
  * システム全体の振る舞い
  * API間連携シナリオ
  * エンドツーエンドのフロー
  * 例: 認証 → 書籍検索 → 注文作成 → 在庫更新

---

## 2. REST Assured のセットアップ

### 2.1 依存関係の追加

Gradle の場合、`build.gradle` に以下を追加:

```gradle
dependencies {
    // REST Assured
    testImplementation 'io.rest-assured:rest-assured:5.3.0'
    testImplementation 'io.rest-assured:json-path:5.3.0'
    testImplementation 'io.rest-assured:xml-path:5.3.0'
    
    // JUnit 5
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
    
    // JSON処理
    testImplementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
}

test {
    useJUnitPlatform()
    
    // E2Eテストはタグで分離
    exclude '**/*E2ETest.class'
}

// E2Eテスト専用タスク
task e2eTest(type: Test) {
    useJUnitPlatform {
        includeTags 'e2e'
    }
    description = 'Runs E2E tests'
    group = 'verification'
}
```

### 2.2 REST Assured の設定

テストベースクラスを作成:

```java
package pro.kensait.bookstore.e2e;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

@Tag("e2e")
public abstract class BaseE2ETest {
    
    protected static RequestSpecification requestSpec;
    protected static String baseUrl;
    protected static String jwtToken;
    
    @BeforeAll
    public static void setup() {
        // architecture_design.mdから取得した設定
        baseUrl = System.getProperty("test.baseUrl", "http://localhost:8080/back-office-api-sdd");
        
        RestAssured.baseURI = baseUrl;
        RestAssured.basePath = "/api";
        
        requestSpec = new RequestSpecBuilder()
            .setContentType("application/json")
            .setAccept("application/json")
            .build();
    }
    
    /**
     * JWT認証トークンを取得
     */
    protected static String login(String employeeCode, String password) {
        return RestAssured.given(requestSpec)
            .body(String.format("{\"employeeCode\":\"%s\",\"password\":\"%s\"}", employeeCode, password))
            .when()
            .post("/auth/login")
            .then()
            .statusCode(200)
            .extract()
            .cookie("jwtToken"); // または header から取得
    }
}
```

---

## 3. E2Eテストケース生成

### 3.1 テストケース設計方針

* 1シナリオ＝1テストクラスの粒度
* basic_design/behaviors.md の各Given-When-Thenシナリオを実際のHTTPリクエストとしてテスト
* 複数APIにまたがるエンドツーエンドのフローをテスト
* 実際のDBアクセスを含む（テストデータの準備と検証）
* HTTPステータスコード、レスポンスボディ、ヘッダーの検証

### 3.2 テストケーステンプレート

シナリオ例: 書籍検索から注文作成までのフロー

```java
package pro.kensait.bookstore.e2e;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Tag("e2e")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookOrderE2ETest extends BaseE2ETest {
    
    private static String jwtToken;
    private static Long orderId;
    
    @BeforeAll
    static void setupAuth() {
        // 認証
        jwtToken = login("admin", "password123");
        assertNotNull(jwtToken, "JWT token should be obtained");
    }
    
    @Test
    @Order(1)
    @DisplayName("1. 書籍を検索できる")
    void test_searchBooks() {
        given(requestSpec)
            .queryParam("keyword", "Java")
            .queryParam("categoryId", 1)
        .when()
            .get("/books/search/jpql")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)))
            .body("[0].bookName", containsString("Java"));
    }
    
    @Test
    @Order(2)
    @DisplayName("2. 書籍詳細を取得できる")
    void test_getBookDetail() {
        given(requestSpec)
        .when()
            .get("/books/1")
        .then()
            .statusCode(200)
            .body("bookId", equalTo(1))
            .body("bookName", notNullValue())
            .body("quantity", greaterThanOrEqualTo(0));
    }
    
    @Test
    @Order(3)
    @DisplayName("3. 在庫を確認できる")
    void test_checkStock() {
        given(requestSpec)
            .cookie("jwtToken", jwtToken) // 認証が必要な場合
        .when()
            .get("/stocks/1")
        .then()
            .statusCode(200)
            .body("bookId", equalTo(1))
            .body("quantity", greaterThan(0));
    }
    
    @Test
    @Order(4)
    @DisplayName("4. 注文を作成できる")
    void test_createOrder() {
        String orderRequest = """
            {
                "customerId": 1,
                "items": [
                    {
                        "bookId": 1,
                        "quantity": 2
                    }
                ]
            }
            """;
        
        orderId = given(requestSpec)
            .cookie("jwtToken", jwtToken)
            .body(orderRequest)
        .when()
            .post("/orders")
        .then()
            .statusCode(201)
            .body("orderId", notNullValue())
            .body("status", equalTo("CREATED"))
        .extract()
            .path("orderId");
    }
    
    @Test
    @Order(5)
    @DisplayName("5. 在庫が減少していることを確認")
    void test_verifyStockReduction() {
        given(requestSpec)
            .cookie("jwtToken", jwtToken)
        .when()
            .get("/stocks/1")
        .then()
            .statusCode(200)
            .body("bookId", equalTo(1))
            .body("quantity", lessThan(100)); // 初期値より減っていることを確認
    }
}
```

### 3.3 認証が必要なAPIのテスト

JWT認証が必要な場合:

```java
@Test
@DisplayName("認証が必要なエンドポイント")
void test_authenticatedEndpoint() {
    // 認証なしの場合
    given(requestSpec)
    .when()
        .get("/books/admin/stats")
    .then()
        .statusCode(401); // Unauthorized
    
    // 認証ありの場合
    given(requestSpec)
        .cookie("jwtToken", jwtToken) // または .header("Authorization", "Bearer " + jwtToken)
    .when()
        .get("/books/admin/stats")
    .then()
        .statusCode(200)
        .body("totalBooks", greaterThan(0));
}
```

### 3.4 エラーケースのテスト

```java
@Test
@DisplayName("存在しない書籍IDで404エラー")
void test_bookNotFound() {
    given(requestSpec)
    .when()
        .get("/books/99999")
    .then()
        .statusCode(404)
        .body("message", containsString("not found"));
}

@Test
@DisplayName("不正なリクエストで400エラー")
void test_invalidRequest() {
    String invalidRequest = """
        {
            "bookId": "invalid",
            "quantity": -1
        }
        """;
    
    given(requestSpec)
        .cookie("jwtToken", jwtToken)
        .body(invalidRequest)
    .when()
        .post("/orders")
    .then()
        .statusCode(400);
}
```

---

## 4. テストデータの準備

### 4.1 DBのセットアップ

E2Eテスト用のテストデータを準備:

```java
@BeforeAll
static void setupTestData() {
    // テストデータをDBに挿入
    // 方法1: SQLスクリプトを実行
    // 方法2: REST APIでデータを作成
    // 方法3: DBに直接アクセス（EntityManager等）
}

@AfterAll
static void cleanupTestData() {
    // テストデータをクリーンアップ
}
```

### 4.2 テストデータ管理のベストプラクティス

* テスト間の独立性を保つ
* テストデータは@BeforeEachで準備、@AfterEachでクリーンアップ
* または、各テストで一意のデータを使用（UUID等）

---

## 5. テストの実行

### 5.1 前提条件

E2Eテストを実行する前に:

1. アプリケーションサーバーを起動
   ```bash
   ./gradlew run
   # または
   java -jar build/libs/app.war
   ```

2. データベースが起動していることを確認

3. テスト用データの準備（必要に応じて）

### 5.2 E2Eテストの実行

```bash
# E2Eテストのみ実行
./gradlew e2eTest

# 通常のテスト（単体テスト）のみ実行（E2Eテストは除外）
./gradlew test

# 全てのテストを実行
./gradlew test e2eTest
```

---

## 6. テストレポート

### 6.1 テスト結果の確認

* JUnit 5のレポート: `build/reports/tests/e2eTest/index.html`
* REST Assuredのログ: コンソール出力

### 6.2 ログの有効化

詳細なHTTPリクエスト/レスポンスをログ出力:

```java
given(requestSpec)
    .log().all() // リクエスト全体をログ
.when()
    .get("/books/1")
.then()
    .log().all() // レスポンス全体をログ
    .statusCode(200);
```

---

## 7. basic_design/behaviors.md からのテストケース生成

### 7.1 シナリオの読み取り

basic_design/behaviors.md の各シナリオを読み取り、以下の情報を抽出:

* Given: 初期状態、テストデータの準備
* When: 実行するAPIリクエスト（エンドポイント、メソッド、パラメータ）
* Then: 期待される結果（ステータスコード、レスポンスボディ、DB状態）

### 7.2 シナリオ例とテストコードの対応

behaviors.mdの例:
```
Given: ユーザーがログイン済み
And: 書籍ID=1の在庫が10個ある
When: POST /api/orders で書籍ID=1を3個注文
Then:
  * HTTP 201 Created が返される
  * 注文IDが返される
  * 書籍ID=1の在庫が7個に減る
```

生成されるテストコード:
```java
@Test
@DisplayName("書籍を注文すると在庫が減少する")
void test_orderReducesStock() {
    // Given: ログイン
    String token = login("user", "password");
    
    // Given: 初期在庫を確認
    int initialStock = given(requestSpec)
        .cookie("jwtToken", token)
    .when()
        .get("/stocks/1")
    .then()
        .statusCode(200)
    .extract()
        .path("quantity");
    
    // When: 注文を作成
    String orderRequest = """
        {
            "items": [{"bookId": 1, "quantity": 3}]
        }
        """;
    
    Long orderId = given(requestSpec)
        .cookie("jwtToken", token)
        .body(orderRequest)
    .when()
        .post("/orders")
    .then()
        .statusCode(201)
        .body("orderId", notNullValue())
    .extract()
        .path("orderId");
    
    // Then: 在庫が減少していることを確認
    given(requestSpec)
        .cookie("jwtToken", token)
    .when()
        .get("/stocks/1")
    .then()
        .statusCode(200)
        .body("quantity", equalTo(initialStock - 3));
}
```

---

## 8. CI/CD パイプラインとの統合

### 8.1 GitHub Actions の例

```yaml
name: E2E Tests

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  e2e-test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: testdb
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        ports:
          - 5432:5432
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Build application
        run: ./gradlew build
      
      - name: Start application
        run: |
          ./gradlew run &
          sleep 30 # アプリケーションの起動を待つ
      
      - name: Run E2E tests
        run: ./gradlew e2eTest
      
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: e2e-test-results
          path: build/reports/tests/e2eTest/
```

---

## 9. 注意事項

### 9.1 テスト実行環境

* E2Eテストは実際の環境（またはステージング環境）で実行する
* テスト用のDBを使用し、本番DBは使用しない
* テスト後はデータをクリーンアップする

### 9.2 テストの安定性

* ネットワーク遅延を考慮してタイムアウトを設定
* 外部APIの依存を最小限にする（必要に応じてモックサーバーを使用）
* テスト間の依存関係を避ける

### 9.3 パフォーマンス

* E2Eテストは実行時間が長いため、並列実行を検討
* 重要なシナリオのみをE2Eテストでカバー
* 詳細なテストは単体テストで実施

---

## 10. 参考資料

* REST Assured公式ドキュメント: https://rest-assured.io/
* JUnit 5公式ドキュメント: https://junit.org/junit5/
* basic_design/behaviors.md - E2Eテストシナリオ
* basic_design/functional_design.md - API仕様
* basic_design/architecture_design.md - システム構成
