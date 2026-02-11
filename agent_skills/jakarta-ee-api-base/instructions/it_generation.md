# 結合テスト生成インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
target_domains: "対象ドメイン名（カンマ区切りで複数指定可能、または all）"
```

* 例1: 特定のドメインの結合テスト
```yaml
project_root: "projects/sdd-wf/bookstore/back-office-api"
spec_directory: "projects/sdd-wf/bookstore/back-office-api/specs/baseline"
target_domains: "common,orders"
```

* 例2: すべてのドメインの結合テスト
```yaml
project_root: "projects/sdd-wf/bookstore/back-office-api"
spec_directory: "projects/sdd-wf/bookstore/back-office-api/specs/baseline"
target_domains: "all"
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
* **テストフレームワーク（2種類を並行使用）:**
  * **主: JUnit 5 + Weld SE（CDIコンテナ）** - 従来型の結合テスト（必須）
  * **補助・実験的: JUnit 5 + Cucumber + Weld SE** - Gherkin記法によるBDD形式テスト（オプション）
* テスト対象: basic_design/{domain}/behaviors.md（結合テスト用）のシナリオ（Gherkin 記法で記述されている前提。@agent_skills/jakarta-ee-api-base/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照）
* Service層以下（Service + DAO + Entity）の実際の連携をテスト
* モックは使用しない（外部APIのみWireMockでスタブ化）
* アプリケーションサーバーは不要（Weld SEでCDIコンテナを起動）
* ドメイン単位または全ドメインの結合テストを生成
* **既存テストの扱い（重要）:**
  * 既存の JUnit + Weld テストコードは削除せずに保護する
  * 既存の Cucumber テストコード（.feature ファイルやステップ定義）が存在する場合は、それらを削除せずに読み込んで、差分のみを反映する
  * ファイルをゼロから作り直すのではなく、既存の内容を尊重して必要なテストケースのみを追加・修正する
  * 新規テストファイルが必要な場合のみ、新規作成する
  * **既存の単体テスト用CucumberTestRunner.java（src/test/java/.../cucumber/）は削除しない**
    * このファイルは単体テスト専用で、結合テストとは独立している
    * ルートbuild.gradleに `junit-platform-suite` 依存関係が追加されているため、コンパイルエラーは発生しない

---

## 1. SPECの読み込みと理解

パラメータで指定されたプロジェクト情報に基づいて、以下の設計ドキュメントを読み込んで分析する

### 1.1 Agent Skillsルール（最優先で確認）

* @agent_skills/jakarta-ee-api-base/principles/ - Jakarta EE開発の原則、アーキテクチャ標準、品質基準、セキュリティ標準を確認する
  * このフォルダ配下の原則ドキュメントを読み込み、共通ルールを遵守すること
  * 重要: 結合テスト生成においても、ルールドキュメントに記載されたすべてのルールを遵守すること
  * 注意: Agent Skills配下のルールは全プロジェクト共通。プロジェクト固有のルールがある場合は `{project_root}/principles/` も確認すること

### 1.2 基本設計の仕様（共通）

まず、共通ドメインのSPECを読み込み、システム全体の設計を理解する

* {spec_directory}/basic_design/common/architecture_design.md - 技術スタック、パッケージ構造、テスト設定を確認する
  * 使用技術スタック
  * データソース設定（JNDI名）
  * 外部API連携設定

* {spec_directory}/basic_design/common/data_model.md - データモデルを確認する（該当する場合）
  * エンティティ定義
  * リレーション
  * 制約

* {spec_directory}/basic_design/common/functional_design.md - 共通機能設計（認証、JWT、ログ、エラーハンドリング等）を確認する

* {spec_directory}/basic_design/common/behaviors.md - 共通機能の振る舞い（結合テスト用）を確認する

* {spec_directory}/basic_design/common/external_interface.md - 外部API仕様を確認する（該当する場合）
  * 外部APIエンドポイント
  * リクエスト/レスポンス形式
  * WireMockスタブ化の対象

### 1.3 基本設計の仕様（対象ドメイン）

対象ドメインのSPECを読み込み、ドメインの設計を理解する

* {spec_directory}/basic_design/{target_domain}/functional_design.md - ドメインの機能設計を確認する
  * ドメインのAPI仕様
  * ビジネスロジック
  * データフロー

* {spec_directory}/basic_design/{target_domain}/behaviors.md - ドメインの振る舞い（結合テスト用）を確認する
  * Service層以下の振る舞い
  * ビジネスロジックの検証シナリオ
  * データアクセスの検証シナリオ
  * 外部API連携の検証シナリオ
  * 例: OrderService → OrderDao → DB + 外部在庫API呼び出し

注意:
* target_domains が "all" の場合、basic_design/配下のすべてのドメインフォルダから behaviors.md を読み込む
* target_domains が複数ドメイン指定の場合、指定されたドメインの behaviors.md のみを読み込む

---

## 2. Weld SE と WireMock のセットアップ

### 2.1 依存関係

結合テスト生成に必要なライブラリ:

* Weld SE (CDI): `org.jboss.weld.se:weld-se-core:5.1.0.Final`
* WireMock (外部APIスタブ): `com.github.tomakehurst:wiremock-jre8:2.35.0`
* Hibernate (JPA実装): `org.hibernate.orm:hibernate-core:6.4.0.Final`
* JUnit 5: `org.junit.jupiter:junit-jupiter:5.10.0`
* JUnit Platform: `org.junit.platform:junit-platform-launcher:1.10.0`
* JUnit Platform Suite: `org.junit.platform:junit-platform-suite:1.10.0`
* JAX-RS Client: `org.glassfish.jersey.core:jersey-client:3.1.3`
* JAX-RS JSON処理: `org.glassfish.jersey.media:jersey-media-json-binding:3.1.3`
* HSQLDB: `org.hsqldb:hsqldb:2.7.2`

* 結合テストクラスには `@Tag("integration")` を付与し、通常の単体テスト実行から分離する

**依存関係の追加方法:**
* まず、対象プロジェクトの `build.gradle` を確認する
* プロジェクト内に `build.gradle` が存在しない、または依存関係が定義されていない場合:
  * 親ディレクトリやプロジェクトルートの `build.gradle` を探索する
  * 共通のビルドファイルで `subprojects` ブロックや全プロジェクト共通設定が定義されている可能性がある
  * 見つかった場合、そちらに依存関係を追加する
* `integrationTest` タスクについても同様に、既存の定義を確認してから追加の要否を判断する

### 2.2 Weld SE の設定

* `src/test/resources/META-INF/beans.xml`: Jakarta EE Beans 4.0、`bean-discovery-mode="all"`

### 2.3 テスト用 persistence.xml

* `src/test/resources/META-INF/persistence.xml`: persistence-unit 名は `test-pu`、transaction-type は RESOURCE_LOCAL
* テスト対象のエンティティを `<class>` で列挙
* HSQLDB メモリ（jdbc:hsqldb:mem:testdb）、Hibernate で `hbm2ddl.auto=create-drop`、dialect=HSQLDialect

### 2.4 DBUnitの導入（必須）

結合テストでは、テストデータの管理にDBUnitを使用する（必須）:

* DBUnit: `org.dbunit:dbunit:2.7.3`（プロジェクトのbuild.gradleに追加済み）
* テストデータをXML/CSV形式で外部管理
* データベースの初期状態を明示的に定義し、再現性を確保
* テスト実行前のクリーンアップとセットアップの自動化
* テスト実行後のデータベース状態の検証

**テストデータセットの配置:**
```
src/test/resources/datasets/
  ├── {domain}/
  │   ├── initial-data.xml
  │   ├── expected-after-operation.xml
  │   └── scenario-specific-data.xml
  └── common/
      └── master-data.xml
```

### 2.5 EntityManagerProducer（CDI経由でEntityManagerを提供）

Weld SEでEntityManagerをCDI経由で注入できるようにするProducerクラスを作成する:

```java
package pro.kensait.berrybooks.integration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class EntityManagerProducer {
    
    private static EntityManagerFactory emf;
    
    static {
        emf = Persistence.createEntityManagerFactory("test-pu");
    }
    
    @Produces
    @RequestScoped
    @PersistenceContext(unitName = "BerryBooksPU")
    public EntityManager createEntityManager() {
        return emf.createEntityManager();
    }
    
    // 重要: @Disposesパラメータに@PersistenceContextを付与しない
    public void closeEntityManager(@Disposes EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
    
    public static void closeEntityManagerFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
```

---

## 3. DBUnitによるテストデータ管理（必須）

### 3.1 BaseIntegrationTestへのDBUnit統合

BaseIntegrationTestクラスにDBUnitサポートを追加する:

```java
package pro.kensait.berrybooks.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.*;

import java.io.File;

@Tag("integration")
public abstract class BaseIntegrationTest {
    
    protected static WeldContainer container;
    protected static WireMockServer wireMockServer;
    protected static EntityManagerFactory emf;
    
    protected EntityManager em;
    protected IDatabaseTester databaseTester;
    
    @BeforeAll
    public static void setUpAll() {
        // Weld SE の起動
        Weld weld = new Weld()
            .enableDiscovery()
            .addPackages(true, BaseIntegrationTest.class.getPackage());
        container = weld.initialize();
        
        // WireMockServer の起動
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
        
        // EntityManagerFactory の作成
        emf = Persistence.createEntityManagerFactory("test-pu");
    }
    
    @AfterAll
    public static void tearDownAll() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
        if (container != null) {
            container.close();
        }
    }
    
    @BeforeEach
    public void setUp() throws Exception {
        em = emf.createEntityManager();
        em.getTransaction().begin();
        
        // DBUnitのセットアップ
        setupDatabaseTester();
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        // トランザクションロールバック
        if (em != null && em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        if (em != null && em.isOpen()) {
            em.close();
        }
        
        // WireMock リセット
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.resetAll();
        }
        
        // DBUnit クリーンアップ
        if (databaseTester != null) {
            databaseTester.onTearDown();
        }
    }
    
    /**
     * DBUnitのセットアップ
     */
    protected void setupDatabaseTester() throws Exception {
        // JDBC接続情報（persistence.xmlと同じ）
        String jdbcUrl = "jdbc:hsqldb:mem:testdb";
        String user = "SA";
        String password = "";
        
        // JdbcDatabaseTesterの作成
        databaseTester = new JdbcDatabaseTester(
            "org.hsqldb.jdbcDriver", jdbcUrl, user, password
        );
        
        // DatabaseConfigの設定（HSQLDB用）
        DatabaseConfig config = databaseTester.getConnection().getConfig();
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, 
            new HsqldbDataTypeFactory());
    }
    
    /**
     * XMLデータセットをロードしてDBに投入
     */
    protected void loadDataSet(String dataSetPath) throws Exception {
        IDataSet dataSet = new FlatXmlDataSetBuilder()
            .setColumnSensing(true)
            .build(getClass().getResourceAsStream(dataSetPath));
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
    }
    
    /**
     * CSVデータセットをロードしてDBに投入
     */
    protected void loadCsvDataSet(String dataSetDir) throws Exception {
        IDataSet dataSet = new CsvDataSet(new File(dataSetDir));
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
    }
    
    /**
     * データベースの特定テーブルを取得
     */
    protected ITable getDatabaseTable(String tableName) throws Exception {
        return databaseTester.getConnection()
            .createDataSet()
            .getTable(tableName);
    }
    
    /**
     * データベースの状態を期待XMLと比較
     */
    protected void assertDatabaseState(String expectedDataSetPath, String... tableNames) 
            throws Exception {
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder()
            .setColumnSensing(true)
            .build(getClass().getResourceAsStream(expectedDataSetPath));
        
        IDataSet actualDataSet = databaseTester.getConnection().createDataSet(tableNames);
        
        for (String tableName : tableNames) {
            ITable expectedTable = expectedDataSet.getTable(tableName);
            ITable actualTable = actualDataSet.getTable(tableName);
            
            org.dbunit.Assertion.assertEquals(expectedTable, actualTable);
        }
    }
    
    protected void persistAndFlush(Object entity) {
        em.persist(entity);
        em.flush();
    }
    
    protected void clearEntityCache() {
        em.clear();
    }
}
```

### 3.2 XMLデータセットの作成例

**`src/test/resources/datasets/orders/initial-data.xml`:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<dataset>
  <!-- 顧客マスター -->
  <CUSTOMER CUSTOMER_ID="1" EMAIL="customer1@example.com" NAME="Customer One" />
  <CUSTOMER CUSTOMER_ID="2" EMAIL="customer2@example.com" NAME="Customer Two" />
  
  <!-- 商品マスター -->
  <PRODUCT PRODUCT_ID="101" PRODUCT_NAME="Product A" PRICE="1000" STOCK="100" />
  <PRODUCT PRODUCT_ID="102" PRODUCT_NAME="Product B" PRICE="2000" STOCK="50" />
  
  <!-- 既存注文（検証用） -->
  <ORDER_TRAN ORDER_TRAN_ID="1" CUSTOMER_ID="1" TOTAL_AMOUNT="3000" 
              STATUS="COMPLETED" ORDER_DATE="2024-01-01 10:00:00" />
  <ORDER_DETAIL ORDER_DETAIL_ID="1" ORDER_TRAN_ID="1" PRODUCT_ID="101" 
                QUANTITY="2" PRICE="1000" />
  <ORDER_DETAIL ORDER_DETAIL_ID="2" ORDER_TRAN_ID="1" PRODUCT_ID="102" 
                QUANTITY="1" PRICE="2000" />
</dataset>
```

### 3.3 DBUnitを使用したテストケースの実装パターン

**パターン1: 初期データ投入 + DB状態検証**
```java
@Tag("integration")
class OrderServiceIntegrationTest extends BaseIntegrationTest {
    
    private OrderService orderService;
    
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        orderService = container.select(OrderService.class).get();
    }
    
    @Test
    void testCreateOrder_Success() throws Exception {
        // Arrange: DBUnitで初期データ投入（顧客、商品マスター）
        loadDataSet("/datasets/orders/initial-customers-products.xml");
        
        CreateOrderRequest request = new CreateOrderRequest(1L, 
            Arrays.asList(
                new OrderItemRequest(101L, 2),
                new OrderItemRequest(102L, 1)
            ));
        
        // WireMockスタブ設定（外部在庫API）
        stubFor(get(urlEqualTo("/api/stock/101"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("{\"available\": true, \"stock\": 100}")));
        stubFor(get(urlEqualTo("/api/stock/102"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("{\"available\": true, \"stock\": 50}")));
        
        // Act
        OrderTran order = orderService.createOrder(request);
        em.flush();
        em.clear();
        
        // Assert: DBUnit でテーブル検証
        ITable orderTable = getDatabaseTable("ORDER_TRAN");
        assertEquals(1, orderTable.getRowCount());
        assertEquals("1", orderTable.getValue(0, "CUSTOMER_ID").toString());
        assertEquals("3000", orderTable.getValue(0, "TOTAL_AMOUNT").toString());
        
        ITable detailTable = getDatabaseTable("ORDER_DETAIL");
        assertEquals(2, detailTable.getRowCount());
        
        // WireMock verify
        verify(getRequestedFor(urlEqualTo("/api/stock/101")));
        verify(getRequestedFor(urlEqualTo("/api/stock/102")));
    }
}
```

**パターン2: 期待データセットとの完全比較**
```java
@Test
void testUpdateOrderStatus_Success() throws Exception {
    // Arrange: 更新前の状態を投入
    loadDataSet("/datasets/orders/order-before-status-update.xml");
    
    // Act: ステータスを更新
    orderService.updateOrderStatus(1L, "SHIPPED");
    em.flush();
    em.clear();
    
    // Assert: 期待する状態と完全一致を検証
    assertDatabaseState("/datasets/orders/order-after-status-update.xml", 
        "ORDER_TRAN");
}
```

**パターン3: DAO層の複雑な検索テスト**
```java
@Test
void testFindOrdersByDateRange_MultipleResults() throws Exception {
    // Arrange: 複数の注文データを投入
    loadDataSet("/datasets/orders/orders-in-date-range.xml");
    
    OrderDao orderDao = container.select(OrderDao.class).get();
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);
    
    // Act
    List<OrderTran> orders = orderDao.findByDateRange(startDate, endDate);
    
    // Assert
    assertEquals(5, orders.size());
    // 日付順でソートされていることを確認
    for (int i = 0; i < orders.size() - 1; i++) {
        assertTrue(orders.get(i).getOrderDate()
            .isBefore(orders.get(i + 1).getOrderDate()) ||
            orders.get(i).getOrderDate()
            .equals(orders.get(i + 1).getOrderDate()));
    }
}
```

### 3.4 DBUnitのベストプラクティス

1. **データセットの粒度**
   * 1テストケース = 1データセット（または複数の組み合わせ）
   * 共通マスターデータは別ファイルに分離
   * シナリオ固有データは専用ファイルに配置

2. **データセットのネーミング規則**
   * `{feature}-initial-data.xml` - テスト開始時の初期状態
   * `{feature}-expected-result.xml` - テスト実行後の期待状態
   * `{feature}-scenario-{name}.xml` - 特定シナリオ用データ

3. **カラム名とテーブル名**
   * データベースの実際のカラム名・テーブル名を使用（大文字/小文字を統一）
   * `setColumnSensing(true)` で未定義カラムを自動検出

4. **NULL値の扱い**
   * XMLでNULL値を表現: `<TABLE COLUMN="[null]" />`
   * 空文字列とNULLの区別に注意

5. **日付・時刻の扱い**
   * ISO 8601形式で記述: `2024-01-01 12:00:00`
   * タイムスタンプは固定値を使用（再現性確保）

6. **外部キー制約**
   * 親テーブル → 子テーブルの順でデータ投入
   * CASCADE設定を考慮したデータセット設計

7. **テストの独立性**
   * 各テストで CLEAN_INSERT を使用（既存データをクリア）
   * @AfterEach でトランザクションロールバック

---

## 4. 結合テストケース生成

### 3.1 テストケース設計方針（共通）

* 対象ドメインの basic_design/{target_domain}/behaviors.md のシナリオに基づいてテストを生成
* Service層のビジネスロジックを中心にテスト
* 実際のDB（メモリDB）を使用
* 外部APIはWireMockでスタブ化
* API層（Resource）は含まない（E2Eテストで検証）
* @Tag("integration") を付与し、integrationTest タスクで実行されるようにする
* target_domains が "all" の場合、すべてのドメインの behaviors.md からテストを生成

### 3.2 主テスト: JUnit 5 + Weld SE（従来型、必須）

* `src/test/java` 配下に通常のJUnitテストクラスを作成
* BaseIntegrationTest を継承（Weld SE によるCDIコンテナ起動、EntityManager管理、WireMock管理）
* @Tag("integration") を付与
* テストメソッドは @Test アノテーションで実装
* behaviors.md のシナリオを参考に、Given-When-Then の流れでテストを記述

**重要: Weld SEの初期化**
* Weld SEは `enableDiscovery()` を明示的に呼び出してCDIを有効化する
* `addPackages(true, BaseIntegrationTest.class.getPackage())` でパッケージをスキャン対象に追加
* これにより、beans.xmlなしでもCDI Beanが検出される

**例:**
```java
@Tag("integration")
class OrderServiceIntegrationTest extends BaseIntegrationTest {
    @Test
    void testCreateOrder_Success() {
        // Given: WireMock スタブ設定、テストデータ投入
        stubFor(get(urlEqualTo("/api/stock/123"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("{\"available\": true}")));
        
        // When: Service メソッド呼び出し
        Order order = orderService.createOrder(createOrderRequest);
        
        // Then: DB検証、WireMock verify
        assertNotNull(order.getId());
        verify(getRequestedFor(urlEqualTo("/api/stock/123")));
    }
}
```

### 3.3 補助テスト: JUnit 5 + Cucumber + Weld SE（BDD形式、実験的・オプション）

* behaviors.md の Gherkin シナリオを、**Cucumber の .feature ファイル**（`src/test/resources/features/integration` 配下）と **Cucumber ステップ定義**（Java、Weld SE を利用）に変換する
* 1シナリオ＝1 Feature または 1 Scenario の粒度で .feature に記述
* feature およびステップ定義に @Tag("integration") を付与
* **注意**: Cucumberテストは補助的・実験的な位置づけであり、従来のJUnit + Weldテストを置き換えるものではない

**重要: Cucumberの日本語アノテーション問題について**
* Cucumberの日本語アノテーション（`io.cucumber.java.ja.*`）はコンパイルエラーが発生する可能性がある
* **推奨**: Cucumberテストは完全にオプショナルなので、**生成をスキップすることを推奨**
* どうしてもCucumberテストが必要な場合は、英語アノテーション（`io.cucumber.java.en.*`）を使用すること
  * `@Given`, `@When`, `@Then`, `@And` は `io.cucumber.java.en` パッケージから import
  * .feature ファイルも英語で記述する（`# language: ja` は使用しない）
* Cucumberテストを生成しない場合でも、.feature ファイル（ドキュメント用）は作成してよい（ステップ定義なし）

**Cucumberテストランナーの注意点:**
* 単体テスト用のCucumberTestRunnerは結合テストと競合する可能性がある
* 単体テスト用のCucumberTestRunnerは `src/test/java/.../cucumber/` に配置し、結合テストとは分離する
* 結合テストではCucumberTestRunnerを使用せず、.featureファイルのみを作成する（オプション）

### 3.4 RestAssured や Wiremock の直接利用

* 結合テストでは、必要に応じて RestAssured や Wiremock を直接利用したテストも作成可能
* これらのテストも削除せず、既存テストと共存させる
* 例: REST APIエンドポイントを直接呼び出す結合テスト（アプリケーションサーバー起動が必要）

### 3.2 テストベースクラス

全結合テストで共通の abstract ベースクラスを用意する。ポイント:

* `@Tag("integration")` を付与
* `@BeforeAll`: 
  * Weld SE 起動（`new Weld().enableDiscovery().addPackages(true, BaseIntegrationTest.class.getPackage()).initialize()`）
  * WireMockServer 起動（`new WireMockServer(WireMockConfiguration.wireMockConfig().port(8089))`）
  * WireMock.configureFor("localhost", 8089)
  * EntityManagerFactory 作成（`Persistence.createEntityManagerFactory("test-pu")`）
* `@AfterAll`: EntityManagerFactory.close()、WireMock 停止、container.close()
* `@BeforeEach`: EntityManager 取得、`em.getTransaction().begin()`
* `@AfterEach`: トランザクションがアクティブなら rollback、wireMock.resetAll()

**重要な注意点:**
* Weld SEは `enableDiscovery()` が必須（beans.xmlなしでCDI Beanを検出するため）
* WireMockの初期化は `WireMockConfiguration.wireMockConfig()` を使用（バージョン2.x系の互換性対応）
* EntityManagerProducerで `@Disposes` パラメータには `@PersistenceContext` を付与しない（コンパイルエラー回避）

**BaseIntegrationTest実装例:**

```java
package pro.kensait.berrybooks.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag("integration")
public abstract class BaseIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(BaseIntegrationTest.class);
    
    protected static WeldContainer container;
    protected static WireMockServer wireMockServer;
    protected static EntityManagerFactory emf;
    
    protected EntityManager em;
    
    @BeforeAll
    public static void setUpAll() {
        logger.info("[ BaseIntegrationTest#setUpAll ] Starting integration test environment");
        
        // Weld SE の起動（enableDiscovery()で明示的にCDIを有効化）
        Weld weld = new Weld()
            .enableDiscovery()
            .addPackages(true, BaseIntegrationTest.class.getPackage());
        container = weld.initialize();
        logger.info("[ BaseIntegrationTest#setUpAll ] Weld SE container started");
        
        // WireMockServer の起動
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
        logger.info("[ BaseIntegrationTest#setUpAll ] WireMockServer started on port 8089");
        
        // EntityManagerFactory の作成
        emf = Persistence.createEntityManagerFactory("test-pu");
        logger.info("[ BaseIntegrationTest#setUpAll ] EntityManagerFactory created");
    }
    
    @AfterAll
    public static void tearDownAll() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
        if (container != null) {
            container.close();
        }
    }
    
    @BeforeEach
    public void setUp() {
        em = emf.createEntityManager();
        em.getTransaction().begin();
    }
    
    @AfterEach
    public void tearDown() {
        if (em != null && em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        if (em != null && em.isOpen()) {
            em.close();
        }
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.resetAll();
        }
    }
    
    protected void persistAndFlush(Object entity) {
        em.persist(entity);
        em.flush();
    }
    
    protected void clearEntityCache() {
        em.clear();
    }
}
```

### 3.3 テストケース（Service層）のポイント

* 1シナリオ＝1テストクラス、BaseIntegrationTest を継承
* `@BeforeEach`: container.select(Service.class).get() で Service 取得（モックなし）
* Arrange: stubFor で外部APIのレスポンスをスタブ（URL・ステータス・ボディ）、em.persist でテストデータをDBに投入
* Act: Service のメソッドを直接呼び出し
* Assert: em.flush() 後に em.find で永続化結果を検証、verify() で外部APIが期待どおり呼ばれたことを検証
* 例外ケース: スタブでエラーレスポンスを返し、assertThrows(期待する例外.class, () -> service.メソッド(...)) で検証

### 3.3.1 JAX-RS Clientを使用した外部API連携テスト

外部APIを直接呼び出す結合テスト（WireMockでスタブ化）を作成する場合:

* ClientBuilder.newClient() でクライアントを作成
* JSON処理は自動的に利用可能（jersey-media-json-bindingがクラスパスにあるため）
* `@BeforeEach` でクライアント初期化、`@AfterEach` でクライアントクローズ
* WireMock の stubFor でスタブを設定、verify でリクエスト検証

**実装例:**

```java
@Tag("integration")
class BackOfficeRestClientIntegrationTest extends BaseIntegrationTest {
    
    private Client client;
    private String baseUrl;
    
    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        this.client = ClientBuilder.newClient();
        this.baseUrl = "http://localhost:8089/api";
    }
    
    @AfterEach
    @Override
    public void tearDown() {
        if (client != null) {
            client.close();
        }
        super.tearDown();
    }
    
    @Test
    void testGetAllBooks_Success() {
        // Given: WireMockスタブ設定
        stubFor(get(urlEqualTo("/api/books"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[{\"bookId\": 1, \"bookName\": \"Java完全理解\"}]")));
        
        // When: GET /api/books
        try (Response response = client.target(baseUrl)
                .path("/books")
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            
            // Then: 200 OK
            assertEquals(200, response.getStatus());
            List<BookTO> books = response.readEntity(new GenericType<List<BookTO>>() {});
            assertNotNull(books);
            assertEquals(1, books.size());
        }
        
        verify(getRequestedFor(urlEqualTo("/api/books")));
    }
}
```

### 3.4 DAO層の結合テストのポイント

* BaseIntegrationTest を継承、container から Dao を取得
* Arrange: em.persist でテストデータを投入、em.flush() と em.clear() でキャッシュをクリア
* Act: Dao の検索メソッドを実行
* Assert: 件数・内容を assert で検証

---

## 5. テストデータの準備

### 5.1 DBのセットアップ（DBUnit使用を推奨）

結合テストでは、テストデータの投入に **DBUnit を使用する（必須）**:

* XMLまたはCSV形式でテストデータを外部ファイルとして管理
* `loadDataSet()` メソッドで初期データを投入
* EntityManagerを直接使用する方法との併用も可能

**DBUnit使用例（推奨）:**
```java
@Test
void testBusinessLogic() throws Exception {
    // DBUnitでマスターデータと初期状態を投入
    loadDataSet("/datasets/scenario/initial-state.xml");
    
    // 必要に応じてEntityManagerで追加データ投入
    em.persist(additionalEntity);
    em.flush();
    
    // テスト実行...
}
```

**EntityManager直接使用例（補助的）:**
```java
@Test
void testSimpleCase() {
    // シンプルなケースではEntityManagerを直接使用してもよい
    Customer customer = new Customer("test@example.com", "Test User");
    em.persist(customer);
    em.flush();
    em.clear();
    
    // テスト実行...
}
```

### 5.2 テストデータ管理のベストプラクティス

@agent_skills/jakarta-ee-api-base/principles/architecture.md の「9.4 テストデータ管理」と、上記「3. DBUnitによるテストデータ管理」を参照する。

**重要なポイント:**
* 結合テストでは DBUnit を優先的に使用する
* テストデータをコードから分離し、XMLまたはCSVで管理
* データセットの再利用性を高める
* 外部キー制約を考慮したデータ投入順序

---

## 5. WireMockによる外部APIスタブ化

### 5.1 外部APIのスタブ設定

* external_interface.md に合わせ、stubFor(get/put/post(...).urlEqualTo/urlPathMatching(...)).willReturn(aResponse().withStatus(...).withHeader("Content-Type","application/json").withBody(...)) でスタブを定義する。@BeforeEach または 各 @Test の Arrange で設定

### 5.2 エラーケースのスタブ

* 在庫不足・タイムアウト・認証エラーなど、シナリオに応じて withStatus(400/401/409/500) と withBody でエラーレスポンスを返すスタブを用意する。遅延が必要な場合は withFixedDelay

---

## 6. basic_design/{target_domain}/behaviors.md からのテストケース生成

### 6.1 シナリオの読み取り

basic_design/{target_domain}/behaviors.md は Gherkin 記法で記述されている。@agent_skills/jakarta-ee-api-base/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照の上、各シナリオから Given/When/Then を抽出する。

注意:
* target_domains が "all" の場合、すべてのドメインの behaviors.md からシナリオを読み取る
* 各ドメインのシナリオは独立してテストする

### 6.2 シナリオとテストの対応

* Given: 初期データ（em.persist）と外部APIスタブ（stubFor）で再現する
* When: Service のメソッドを直接呼び出す（引数は functional_design / data_model に合わせる）
* Then: 戻り値の assert、em.flush() 後の em.find で永続化結果を検証、verify() で外部API呼び出しを検証する

---

## 7. 注意事項

### 7.1 テスト実行環境

* 結合テストは実際のDB（メモリDB）を使用する。アプリケーションサーバーは不要（Weld SEで起動）。WireMockは @BeforeAll で起動・@AfterAll で停止。テスト後はトランザクションロールバックで自動クリーンアップ。

### 7.2 テストの安定性

* テスト間の独立性を保つ（@BeforeEach/@AfterEachで初期化・クリーンアップ）。外部APIはWireMockでスタブ化。テストデータは一意にする（UUID等）。トランザクション境界を明確にする。

### 7.3 既存の単体テスト用Cucumberテストランナーとの競合回避

* 既存の `src/test/java/.../cucumber/CucumberTestRunner.java` は単体テスト用である
* 結合テストを実行する際、CucumberTestRunnerが存在するとコンパイルエラーが発生する可能性がある（JUnit Platform Suiteの依存関係が不足）
* 対処方法:
  * プロジェクトのbuild.gradleまたは共通のbuild.gradleに `org.junit.platform:junit-platform-suite` を追加する
  * CucumberTestRunnerのインポート文を明示的に記述する（ワイルドカードインポートを避ける）
  * または、CucumberTestRunnerを単体テスト専用として保持し、結合テストではCucumberを使用しない（.featureファイルのみ作成）

### 7.4 JAX-RS ClientでのJSON処理

* 外部API連携テストでJAX-RS Clientを使用する場合、JSON処理プロバイダーが必要
* build.gradleに以下が定義されている必要がある:
  * `org.glassfish.jersey.core:jersey-client`
  * `org.glassfish.jersey.media:jersey-media-json-binding`
* これにより、Response.readEntity() でJSONを自動的にデシリアライズできる
* プロジェクト内のbuild.gradleに定義がない場合、親ディレクトリやプロジェクトルートのbuild.gradleを確認する

### 7.3 単体テスト vs 結合テスト vs E2Eテスト

| テスト種別 | 対象 | モック | 実行環境 | 目的 |
|-----------|------|--------|---------|------|
| 単体テスト | 個別クラス | あり（タスク外依存） | JUnit | クラスのロジック検証 |
| 結合テスト | Service + DAO + DB | 外部APIのみスタブ | JUnit + Weld SE | ビジネスロジック + データアクセス検証 |
| E2Eテスト | 全体（API層含む） | なし | REST Assured + APサーバー | ユーザー視点の全体フロー検証 |

---

## 8. テストの実行と評価

結合テストコード生成後、以下のステップを実施する:

### 8.1 テスト実行

Gradleタスクを使用して結合テストを実行:

```bash
cd {project_root}
./gradlew integrationTest
```

* `integrationTest` タスクは、@Tag("integration") が付与されたテストを実行する
* プロジェクトのbuild.gradleに定義されたタスク名に従うこと

### 8.2 テスト評価

テスト実行後、@agent_skills/jakarta-ee-api-base/instructions/test_evaluation.md を使用して結果を評価する:

```yaml
project_root: "{project_root}"
jacoco_reports_dir: "{project_root}/build/reports/jacoco/integrationTest"
test_type: "integration"
spec_directory: "{spec_directory}"
```

---

## 10. 参考資料

* Weld SE公式ドキュメント: https://weld.cdi-spec.org/
* WireMock公式ドキュメント: https://wiremock.org/
* JUnit 5公式ドキュメント: https://junit.org/junit5/
* **DBUnit公式ドキュメント: http://dbunit.sourceforge.net/**
* **DBUnitベストプラクティス: http://dbunit.sourceforge.net/bestpractices.html**
* basic_design/{target_domain}/behaviors.md - 結合テストシナリオ（ドメイン単位）
* basic_design/{target_domain}/functional_design.md - 機能仕様（ドメイン単位）
* basic_design/common/architecture_design.md - システム構成
