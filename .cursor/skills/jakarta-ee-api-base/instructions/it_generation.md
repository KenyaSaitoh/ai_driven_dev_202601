# 結合テスト生成インストラクション

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

このインストラクションは、REST APIの結合テスト（Integration Test）を生成するためのものである

重要な方針
* 単体テスト実行評価後に結合テストを生成する（unit_test_execution.mdの次のステップ）
* テストフレームワーク: JUnit 5 + Weld SE（CDIコンテナ）
* テスト対象: basic_design/behaviors.md（結合テスト用）のシナリオ
* Service層以下（Service + DAO + Entity）の実際の連携をテスト
* モックは使用しない（外部APIのみWireMockでスタブ化）
* アプリケーションサーバーは不要（Weld SEでCDIコンテナを起動）

---

## 1. SPECの読み込みと理解

パラメータで指定されたプロジェクト情報に基づいて、以下の設計ドキュメントを読み込んで分析する

### 1.1 Agent Skillsルール（最優先で確認）

* @agent_skills/jakarta-ee-api-base/principles/ - Jakarta EE開発の原則、アーキテクチャ標準、品質基準、セキュリティ標準を確認する
  * このフォルダ配下の原則ドキュメントを読み込み、共通ルールを遵守すること
  * 重要: 結合テスト生成においても、ルールドキュメントに記載されたすべてのルールを遵守すること
  * 注意: Agent Skills配下のルールは全プロジェクト共通。プロジェクト固有のルールがある場合は `{project_root}/principles/` も確認すること

### 1.2 基本設計の仕様

以下のファイルを読み込み、システム全体の設計を理解する

* {spec_directory}/basic_design/architecture_design.md - 技術スタック、パッケージ構造、テスト設定を確認する
  * 使用技術スタック
  * データソース設定（JNDI名）
  * 外部API連携設定

* {spec_directory}/basic_design/functional_design.md - システム全体の機能設計（全APIを含む）を確認する
  * 全てのAPI仕様
  * ビジネスロジック
  * データフロー

* {spec_directory}/basic_design/data_model.md - データモデルを確認する（該当する場合）
  * エンティティ定義
  * リレーション
  * 制約

* {spec_directory}/basic_design/behaviors.md - 結合テストシナリオを確認する
  * Service層以下の振る舞い
  * ビジネスロジックの検証シナリオ
  * データアクセスの検証シナリオ
  * 外部API連携の検証シナリオ
  * 例: OrderService → OrderDao → DB + 外部在庫API呼び出し

* {spec_directory}/basic_design/external_interface.md - 外部API仕様を確認する（該当する場合）
  * 外部APIエンドポイント
  * リクエスト/レスポンス形式
  * WireMockスタブ化の対象

---

## 2. Weld SE と WireMock のセットアップ

### 2.1 依存関係の追加

Gradle の場合、`build.gradle` に以下を追加:

```gradle
dependencies {
    // Weld SE (CDI Container)
    testImplementation 'org.jboss.weld.se:weld-se-core:5.1.0.Final'
    
    // WireMock (外部APIモック)
    testImplementation 'com.github.tomakehurst:wiremock-jre8:2.35.0'
    
    // JUnit 5
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
    
    // Hibernate EntityManager (JPA実装)
    testImplementation 'org.hibernate.orm:hibernate-core:6.4.0.Final'
    
    // HSQLDB (テスト用DB)
    testRuntimeOnly 'org.hsqldb:hsqldb:2.7.2'
}

test {
    useJUnitPlatform()
    
    // 結合テストは除外（通常のテスト実行から）
    exclude '**/*IntegrationTest.class'
}

// 結合テスト専用タスク
task integrationTest(type: Test) {
    useJUnitPlatform {
        includeTags 'integration'
    }
    description = 'Runs integration tests with Weld SE and real database'
    group = 'verification'
    
    // 結合テスト用の環境変数
    systemProperty 'weld.se.debug', 'false'
}
```

### 2.2 Weld SE の設定

`src/test/resources/META-INF/beans.xml` を作成:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                           https://jakarta.ee/xml/ns/jakartaee/beans_4_0.xsd"
       version="4.0"
       bean-discovery-mode="all">
</beans>
```

### 2.3 テスト用persistence.xmlの設定

`src/test/resources/META-INF/persistence.xml` を作成:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
                                 https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"
             version="3.0">
    <persistence-unit name="test-pu" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
        
        <!-- テスト対象のエンティティクラスを列挙 -->
        <!-- 例: <class>pro.kensait.bookstore.entity.Book</class> -->
        
        <properties>
            <!-- HSQLDB（メモリDB）設定 -->
            <property name="jakarta.persistence.jdbc.driver" value="org.hsqldb.jdbcDriver"/>
            <property name="jakarta.persistence.jdbc.url" value="jdbc:hsqldb:mem:testdb"/>
            <property name="jakarta.persistence.jdbc.user" value="SA"/>
            <property name="jakarta.persistence.jdbc.password" value=""/>
            
            <!-- Hibernate設定 -->
            <property name="hibernate.dialect" value="org.hibernate.dialect.HSQLDialect"/>
            <property name="hibernate.hbm2ddl.auto" value="create-drop"/>
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

---

## 3. 結合テストケース生成

### 3.1 テストケース設計方針

* 1シナリオ＝1テストクラスの粒度
* basic_design/behaviors.md の各Given-When-Thenシナリオをテスト
* Service層のビジネスロジックを中心にテスト
* 実際のDB（メモリDB）を使用
* 外部APIはWireMockでスタブ化
* API層（Resource）は含まない（E2Eテストで検証）

### 3.2 テストベースクラス

全結合テストで共通のベースクラスを作成:

```java
package pro.kensait.bookstore.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;

@Tag("integration")
public abstract class BaseIntegrationTest {
    
    protected static SeContainer container;
    protected static WireMockServer wireMock;
    
    protected EntityManager em;
    
    @BeforeAll
    static void setupContainer() {
        // Weld SE（CDIコンテナ）を起動
        container = SeContainerInitializer.newInstance().initialize();
        
        // WireMock起動（外部API用）
        wireMock = new WireMockServer(8089);
        wireMock.start();
        configureFor("localhost", 8089);
    }
    
    @AfterAll
    static void teardownContainer() {
        if (wireMock != null) {
            wireMock.stop();
        }
        if (container != null) {
            container.close();
        }
    }
    
    @BeforeEach
    void setup() {
        // EntityManagerを取得
        em = container.select(EntityManager.class).get();
        
        // トランザクション開始
        em.getTransaction().begin();
    }
    
    @AfterEach
    void cleanup() {
        // トランザクションロールバック（テストデータクリーンアップ）
        if (em != null && em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        
        // WireMockスタブをリセット
        if (wireMock != null) {
            wireMock.resetAll();
        }
    }
}
```

### 3.3 テストケーステンプレート

シナリオ例: 注文処理（Service + DAO + DB + 外部在庫API）

```java
package pro.kensait.bookstore.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import pro.kensait.bookstore.service.OrderService;
import pro.kensait.bookstore.entity.OrderTran;
import pro.kensait.bookstore.api.dto.OrderRequest;
import pro.kensait.bookstore.api.dto.OrderResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderServiceIntegrationTest extends BaseIntegrationTest {
    
    private OrderService orderService;
    
    @BeforeEach
    void setupService() {
        // CDI経由でServiceを取得（モックなし）
        orderService = container.select(OrderService.class).get();
    }
    
    @Test
    @Order(1)
    @DisplayName("注文作成: 在庫更新成功")
    void test_orderBooks_Success() {
        // Arrange: WireMockで在庫APIをスタブ化
        stubFor(put(urlEqualTo("/api/stocks/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"bookId\": 1, \"quantity\": 8, \"version\": 1}")));
        
        // テストデータ準備（実際のDBに挿入）
        // ... Customer, Book等のテストデータを準備 ...
        
        // Act: Serviceメソッドを直接呼び出し
        OrderRequest request = new OrderRequest(
            List.of(new CartItem(1L, "Java完全理解", "技術評論社", 3200, 2, 0)),
            6400,
            800,
            "東京都渋谷区1-2-3",
            1
        );
        
        OrderResponse result = orderService.orderBooks(request);
        
        // Assert: 実際のDBに保存されたか検証
        em.flush(); // 強制的にDBに反映
        
        OrderTran order = em.find(OrderTran.class, result.getTranId());
        assertNotNull(order, "注文がDBに保存されていること");
        assertEquals(6400, order.getTotalPrice(), "合計金額が正しいこと");
        assertEquals(800, order.getDeliveryPrice(), "配送料金が正しいこと");
        assertEquals(1, order.getSettlementType(), "決済方法が正しいこと");
        
        // 外部在庫APIが呼ばれたことを検証
        verify(putRequestedFor(urlEqualTo("/api/stocks/1"))
            .withRequestBody(matchingJsonPath("$.quantity", equalTo("8")))
            .withRequestBody(matchingJsonPath("$.version", equalTo("0"))));
    }
    
    @Test
    @Order(2)
    @DisplayName("注文作成: 在庫不足エラー")
    void test_orderBooks_OutOfStock() {
        // Arrange: WireMockで在庫不足エラーをスタブ化
        stubFor(put(urlEqualTo("/api/stocks/1"))
            .willReturn(aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"在庫不足\"}")));
        
        // Act & Assert: 例外がスローされることを検証
        OrderRequest request = new OrderRequest(
            List.of(new CartItem(1L, "Java完全理解", "技術評論社", 3200, 100, 0)),
            320000,
            800,
            "東京都渋谷区1-2-3",
            1
        );
        
        assertThrows(OutOfStockException.class, () -> {
            orderService.orderBooks(request);
        });
        
        // トランザクションがロールバックされることを検証
        // （BeforeEachで開始したトランザクションがロールバックされる）
    }
    
    @Test
    @Order(3)
    @DisplayName("注文作成: 楽観的ロック競合")
    void test_orderBooks_OptimisticLock() {
        // Arrange: WireMockで楽観的ロック競合をスタブ化
        stubFor(put(urlEqualTo("/api/stocks/1"))
            .willReturn(aResponse()
                .withStatus(409)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"データが更新されています\"}")));
        
        // Act & Assert
        OrderRequest request = new OrderRequest(
            List.of(new CartItem(1L, "Java完全理解", "技術評論社", 3200, 2, 0)),
            6400,
            800,
            "東京都渋谷区1-2-3",
            1
        );
        
        assertThrows(OptimisticLockException.class, () -> {
            orderService.orderBooks(request);
        });
    }
}
```

### 3.4 DAO層の結合テスト

Daoの実際のDBアクセスをテスト:

```java
package pro.kensait.bookstore.integration;

import org.junit.jupiter.api.*;
import pro.kensait.bookstore.dao.BookDao;
import pro.kensait.bookstore.entity.Book;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class BookDaoIntegrationTest extends BaseIntegrationTest {
    
    private BookDao bookDao;
    
    @BeforeEach
    void setupDao() {
        bookDao = container.select(BookDao.class).get();
    }
    
    @Test
    @DisplayName("書籍検索: JPQL動的クエリ")
    void test_searchBooks_JPQL() {
        // Arrange: テストデータ準備
        Book book1 = new Book();
        book1.setBookName("Java完全理解");
        book1.setCategoryId(1L);
        book1.setPrice(3200);
        em.persist(book1);
        
        Book book2 = new Book();
        book2.setBookName("Spring入門");
        book2.setCategoryId(1L);
        book2.setPrice(2800);
        em.persist(book2);
        
        em.flush();
        em.clear(); // 1次キャッシュをクリア
        
        // Act: 実際のJPQLクエリを実行
        List<Book> results = bookDao.searchByJpql("Java", 1L);
        
        // Assert
        assertEquals(1, results.size(), "検索結果が1件であること");
        assertEquals("Java完全理解", results.get(0).getBookName());
    }
    
    @Test
    @DisplayName("書籍検索: Criteria API")
    void test_searchBooks_Criteria() {
        // Arrange: テストデータ準備（同上）
        // ...
        
        // Act: 実際のCriteria APIクエリを実行
        List<Book> results = bookDao.searchByCriteria("Java", 1L);
        
        // Assert
        assertEquals(1, results.size());
        assertEquals("Java完全理解", results.get(0).getBookName());
    }
}
```

---

## 4. テストデータの準備

### 4.1 DBのセットアップ

結合テスト用のテストデータを準備:

```java
@BeforeEach
void setupTestData() {
    // テストデータをDBに挿入
    // EntityManagerを使用してエンティティを永続化
    
    Category category = new Category();
    category.setCategoryName("プログラミング");
    em.persist(category);
    
    Publisher publisher = new Publisher();
    publisher.setPublisherName("技術評論社");
    em.persist(publisher);
    
    Book book = new Book();
    book.setBookName("Java完全理解");
    book.setCategory(category);
    book.setPublisher(publisher);
    book.setPrice(3200);
    em.persist(book);
    
    em.flush(); // 強制的にDBに反映
}
```

### 4.2 テストデータ管理のベストプラクティス

* テスト間の独立性を保つ
* @BeforeEachでテストデータ準備
* @AfterEachでトランザクションロールバック（自動クリーンアップ）
* 各テストで一意のデータを使用（UUID等）

---

## 5. WireMockによる外部APIスタブ化

### 5.1 外部APIのスタブ設定

```java
@BeforeEach
void setupWireMock() {
    // 書籍API（back-office-api）のスタブ
    stubFor(get(urlEqualTo("/api/books/1"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("""
                {
                    "bookId": 1,
                    "bookName": "Java完全理解",
                    "price": 3200,
                    "categoryId": 1,
                    "publisherId": 1
                }
                """)));
    
    // 在庫API（back-office-api）のスタブ
    stubFor(put(urlPathMatching("/api/stocks/.*"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"quantity\": 8, \"version\": 1}")));
    
    // 顧客API（customer-hub-api）のスタブ
    stubFor(get(urlPathMatching("/api/customers/.*"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("""
                {
                    "customerId": 1,
                    "customerName": "山田太郎",
                    "email": "yamada@example.com"
                }
                """)));
}
```

### 5.2 エラーケースのスタブ

```java
// 在庫不足エラー
stubFor(put(urlEqualTo("/api/stocks/999"))
    .willReturn(aResponse()
        .withStatus(400)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"message\": \"在庫不足\"}")));

// 外部APIタイムアウト
stubFor(get(urlEqualTo("/api/books/timeout"))
    .willReturn(aResponse()
        .withStatus(500)
        .withFixedDelay(5000))); // 5秒遅延

// 外部API認証エラー
stubFor(post(urlEqualTo("/api/auth/login"))
    .willReturn(aResponse()
        .withStatus(401)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"message\": \"認証失敗\"}")));
```

---

## 6. テストの実行

### 6.1 前提条件

結合テストを実行する前に:

1. HSQLDBサーバーを起動（メモリDBの場合は不要）
2. アプリケーションサーバーは**不要**（Weld SEで起動）
3. WireMockは自動起動（@BeforeAllで設定）

### 6.2 結合テストの実行

```bash
# 結合テストのみ実行
./gradlew integrationTest

# 通常のテスト（単体テスト）のみ実行（結合テストは除外）
./gradlew test

# 全てのテストを実行
./gradlew test integrationTest
```

---

## 7. テストレポート

### 7.1 テスト結果の確認

* JUnit 5のレポート: `build/reports/tests/integrationTest/index.html`
* コンソール出力: SQLログ、CDIログ

### 7.2 ログの有効化

詳細なログを出力:

```java
// persistence.xmlで設定
<property name="hibernate.show_sql" value="true"/>
<property name="hibernate.format_sql" value="true"/>
<property name="hibernate.use_sql_comments" value="true"/>
```

---

## 8. basic_design/behaviors.md からのテストケース生成

### 8.1 シナリオの読み取り

basic_design/behaviors.md の各シナリオを読み取り、以下の情報を抽出:

* Given: 初期状態、テストデータの準備、外部APIのスタブ設定
* When: 実行するServiceメソッド、パラメータ
* Then: 期待される結果（DBの状態、戻り値、例外）

### 8.2 シナリオ例とテストコードの対応

behaviors.mdの例:
```
Given: 書籍ID=1の在庫が10個ある
And: 顧客ID=1が存在する
When: OrderService.orderBooks()で書籍ID=1を3個注文
Then:
  * 注文が作成される（ORDER_TRAN、ORDER_DETAIL）
  * 外部在庫APIが呼ばれ、在庫が7個に減る
  * 注文IDが返される
```

生成されるテストコード:
```java
@Test
@DisplayName("書籍を注文すると在庫が減少する")
void test_orderReducesStock() {
    // Given: 初期データ準備
    // ... Customer, Book等のエンティティを準備 ...
    
    // Given: 在庫APIのスタブ設定
    stubFor(put(urlEqualTo("/api/stocks/1"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\"quantity\": 7, \"version\": 1}")));
    
    // When: 注文を作成
    OrderRequest request = new OrderRequest(
        List.of(new CartItem(1L, "Java完全理解", "技術評論社", 3200, 3, 0)),
        9600,
        800,
        "東京都渋谷区1-2-3",
        1
    );
    
    OrderResponse result = orderService.orderBooks(request);
    
    // Then: 注文が作成されたことを検証
    assertNotNull(result.getTranId(), "注文IDが返されること");
    
    OrderTran order = em.find(OrderTran.class, result.getTranId());
    assertNotNull(order, "注文がDBに保存されていること");
    assertEquals(9600, order.getTotalPrice(), "合計金額が正しいこと");
    
    // Then: 在庫APIが呼ばれたことを検証
    verify(putRequestedFor(urlEqualTo("/api/stocks/1"))
        .withRequestBody(matchingJsonPath("$.quantity", equalTo("7"))));
}
```

---

## 9. CI/CD パイプラインとの統合

### 9.1 GitHub Actions の例

```yaml
name: Integration Tests

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  integration-test:
    runs-on: ubuntu-latest
    
    services:
      hsqldb:
        image: hsqldb:latest
        ports:
          - 9001:9001
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run integration tests
        run: ./gradlew integrationTest
      
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: integration-test-results
          path: build/reports/tests/integrationTest/
```

---

## 10. 注意事項

### 10.1 テスト実行環境

* 結合テストは実際のDB（メモリDB）を使用する
* アプリケーションサーバーは不要（Weld SEで起動）
* WireMockは自動起動・自動停止
* テスト後はトランザクションロールバックで自動クリーンアップ

### 10.2 テストの安定性

* テスト間の独立性を保つ（@BeforeEach/@AfterEachで初期化・クリーンアップ）
* 外部APIへの依存を最小限にする（WireMockでスタブ化）
* テストデータは一意にする（UUID等）
* トランザクション境界を明確にする

### 10.3 パフォーマンス

* 結合テストは単体テストより遅いが、E2Eテストより速い
* 重要なビジネスロジックのみを結合テストでカバー
* 詳細なテストは単体テストで実施
* 並列実行を検討（独立したテストケース）

### 10.4 単体テスト vs 結合テスト vs E2Eテスト

| テスト種別 | 対象 | モック | 実行環境 | 速度 | 目的 |
|-----------|------|--------|---------|------|------|
| 単体テスト | 個別クラス | あり（タスク外依存） | JUnit | 速い | クラスのロジック検証 |
| 結合テスト | Service + DAO + DB | 外部APIのみスタブ | JUnit + Weld SE | 中速 | ビジネスロジック + データアクセス検証 |
| E2Eテスト | 全体（API層含む） | なし | REST Assured + APサーバー | 遅い | ユーザー視点の全体フロー検証 |

---

## 11. 参考資料

* Weld SE公式ドキュメント: https://weld.cdi-spec.org/
* WireMock公式ドキュメント: https://wiremock.org/
* JUnit 5公式ドキュメント: https://junit.org/junit5/
* basic_design/behaviors.md - 結合テストシナリオ
* basic_design/functional_design.md - 機能仕様
* basic_design/architecture_design.md - システム構成
