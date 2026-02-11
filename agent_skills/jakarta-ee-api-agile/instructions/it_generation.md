# 結合テスト生成インストラクション（アジャイル）

## パラメータ設定

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
usecase_folder: null        # オプション。指定時はそのユースケースの結合テストのみ生成
```

* 例
```yaml
project_root: "projects/sdd-agile/bookstore/berry-books-api"
spec_directory: "projects/sdd-agile/bookstore/berry-books-api/specs/baseline"
```

注意: パス区切りはOS環境に応じて調整する。以降、`{project_root}`, `{spec_directory}` はパラメータで設定した値に置き換える。

---

## 概要

このインストラクションは、アジャイル版のSPECに基づき結合テスト（Integration Test）を生成するためのものである。

重要な方針
* **テストフレームワーク: JUnit 5 + Weld SE（CDIコンテナ）**
* テスト対象: usecases/{名}/behaviors.md のシナリオ（Gherkin記法）。common 用の振る舞いが common/behaviors.md 等で定義されていればそれも参照する
* Service層以下（Service + DAO + Entity）の実際の連携をテストする。外部APIは WireMock でスタブ化
* アプリケーションサーバーは不要（Weld SE で CDI コンテナを起動）
* **既存テストの保護**: 既存の JUnit + Weld テストコードは削除せず、差分を反映する

---

## 1. 読み込むドキュメント

* @agent_skills/jakarta-ee-api-agile/principles/ を確認する
* {spec_directory}/common/architecture_design.md - 技術スタック、データソース、テスト設定
* {spec_directory}/common/data_model.md - データモデル
* {spec_directory}/common/external_interface.md - 外部API（WireMock スタブ化の対象）
* 結合テストシナリオの参照元:
  * usecase_folder 指定時: {spec_directory}/usecases/{usecase_folder}/behaviors.md
  * 未指定時: {spec_directory}/usecases/ 配下の各 behaviors.md を集約してシナリオを収集

---

## 2. 結合テストの生成

### 2.1 JUnit 5 + Weld SE + DBUnit

* `src/test/java` 配下に通常のJUnitテストクラスを作成
* BaseIntegrationTest を継承（Weld SE によるCDIコンテナ起動、EntityManager管理）
* @Tag("integration") を付与
* Service層以下を実装で動かし、実際のDB（メモリDB）を使用する
* **DBUnit（必須）**: テストデータの投入・検証に DBUnit を使用する
* 外部API呼び出しは WireMock でスタブ化する（external_interface.md に従う）
* テストメソッドは @Test アノテーションで実装

**DBUnitの使用目的:**
* テストデータの外部ファイル管理（XML/CSV形式）
* データベースの初期状態の再現性確保
* テスト実行前のクリーンアップとセットアップの自動化
* テスト実行後のデータベース状態の検証

**例:**
```java
@Tag("integration")
class OrderServiceIntegrationTest extends BaseIntegrationTest {
    @Test
    void testCreateOrder_Success() throws Exception {
        // Arrange: DBUnitでテストデータを投入
        loadDataSet("/datasets/orders/initial-data.xml");
        
        // WireMock スタブ設定
        stubFor(get(urlEqualTo("/api/stock/123"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("{\"available\": true}")));
        
        // Act: Service メソッド呼び出し
        Order order = orderService.createOrder(createOrderRequest);
        em.flush();
        em.clear();
        
        // Assert: DBUnitで期待データと比較
        ITable actualTable = getDatabaseTable("ORDER_TRAN");
        assertEquals(2, actualTable.getRowCount());
        
        // WireMock verify
        verify(getRequestedFor(urlEqualTo("/api/stock/123")));
    }
}
```

### 2.2 RestAssured や Wiremock の直接利用

* 結合テストでは、必要に応じて RestAssured や Wiremock を直接利用したテストも作成可能
* これらのテストも削除せず、既存テストと共存させる

---

## 3. DBUnitによるテストデータ管理（必須）

### 3.1 DBUnitの依存関係

* DBUnit: `org.dbunit:dbunit:2.7.3`（プロジェクトのbuild.gradleに追加済み）
* SLF4J: DBUnitのログ出力に使用

### 3.2 テストデータセットの作成

テストデータは XML または CSV 形式で `src/test/resources/datasets` 配下に配置する:

**ディレクトリ構造例:**
```
src/test/resources/datasets/
  ├── orders/
  │   ├── initial-data.xml
  │   ├── expected-after-create.xml
  │   └── customer-with-orders.xml
  ├── customers/
  │   └── test-customers.xml
  └── common/
      └── base-data.xml
```

**XML形式（FlatXmlDataSet）:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<dataset>
  <CUSTOMER CUSTOMER_ID="1" EMAIL="test@example.com" NAME="Test User" />
  <CUSTOMER CUSTOMER_ID="2" EMAIL="user2@example.com" NAME="User Two" />
  
  <PRODUCT PRODUCT_ID="101" PRODUCT_NAME="Product 1" PRICE="1000" />
  <PRODUCT PRODUCT_ID="102" PRODUCT_NAME="Product 2" PRICE="2000" />
  
  <ORDER_TRAN ORDER_TRAN_ID="1" CUSTOMER_ID="1" TOTAL_AMOUNT="3000" STATUS="COMPLETED" />
  <ORDER_DETAIL ORDER_DETAIL_ID="1" ORDER_TRAN_ID="1" PRODUCT_ID="101" QUANTITY="2" PRICE="1000" />
  <ORDER_DETAIL ORDER_DETAIL_ID="2" ORDER_TRAN_ID="1" PRODUCT_ID="102" QUANTITY="1" PRICE="2000" />
</dataset>
```

**CSV形式（各テーブル1ファイル）:**
```csv
CUSTOMER_ID,EMAIL,NAME
1,test@example.com,Test User
2,user2@example.com,User Two
```

### 3.3 BaseIntegrationTestへのDBUnit統合

BaseIntegrationTestクラスにDBUnitサポートを追加する:

```java
@Tag("integration")
public abstract class BaseIntegrationTest {
    
    protected static WeldContainer container;
    protected static EntityManagerFactory emf;
    protected EntityManager em;
    protected IDatabaseTester databaseTester;
    
    @BeforeAll
    public static void setUpAll() {
        // Weld SE とEntityManagerFactoryの初期化
        Weld weld = new Weld().enableDiscovery()
            .addPackages(true, BaseIntegrationTest.class.getPackage());
        container = weld.initialize();
        emf = Persistence.createEntityManagerFactory("test-pu");
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
        
        // DatabaseConfigの設定
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
            
            Assertion.assertEquals(expectedTable, actualTable);
        }
    }
}
```

### 3.4 DBUnitを使用したテストケースの実装パターン

**パターン1: 初期データ投入 + DB状態検証**
```java
@Test
void testCreateOrder_Success() throws Exception {
    // Arrange: DBUnitで初期データ投入
    loadDataSet("/datasets/orders/initial-customers-products.xml");
    
    CreateOrderRequest request = new CreateOrderRequest(1L, 
        Arrays.asList(new OrderItemRequest(101L, 2)));
    
    // Act
    Order order = orderService.createOrder(request);
    em.flush();
    em.clear();
    
    // Assert: DBUnit でテーブル検証
    ITable orderTable = getDatabaseTable("ORDER_TRAN");
    assertEquals(1, orderTable.getRowCount());
    assertEquals("1", orderTable.getValue(0, "CUSTOMER_ID").toString());
    
    ITable detailTable = getDatabaseTable("ORDER_DETAIL");
    assertEquals(1, detailTable.getRowCount());
    assertEquals("2", detailTable.getValue(0, "QUANTITY").toString());
}
```

**パターン2: 期待データセットとの比較**
```java
@Test
void testUpdateOrder_Success() throws Exception {
    // Arrange: 初期状態を投入
    loadDataSet("/datasets/orders/order-before-update.xml");
    
    // Act: 注文を更新
    orderService.updateOrderStatus(1L, "SHIPPED");
    em.flush();
    em.clear();
    
    // Assert: 期待する状態と比較
    assertDatabaseState("/datasets/orders/order-after-update.xml", 
        "ORDER_TRAN", "ORDER_DETAIL");
}
```

**パターン3: 複数データセットの組み合わせ**
```java
@Test
void testComplexScenario() throws Exception {
    // 共通マスターデータ + シナリオ固有データ
    loadDataSet("/datasets/common/master-data.xml");
    loadDataSet("/datasets/orders/scenario-specific-data.xml");
    
    // テスト実行...
}
```

### 3.5 DBUnitのベストプラクティス

1. **データセットの粒度**
   * 1テストケース = 1データセット（または複数の組み合わせ）
   * 共通マスターデータは別ファイルに分離
   * シナリオ固有データは専用ファイルに配置

2. **カラム名とテーブル名**
   * データベースの実際のカラム名・テーブル名を使用（大文字/小文字を統一）
   * `setColumnSensing(true)` で未定義カラムを自動検出

3. **NULL値の扱い**
   * XMLでNULL値を表現: `<TABLE COLUMN="[null]" />`
   * 空文字列とNULLの区別に注意

4. **日付・時刻の扱い**
   * ISO 8601形式で記述: `2024-01-01 12:00:00`
   * タイムスタンプは固定値を使用（再現性確保）

5. **外部キー制約**
   * 親テーブル → 子テーブルの順でデータ投入
   * CASCADE設定を考慮したデータセット設計

6. **テストの独立性**
   * 各テストで CLEAN_INSERT を使用（既存データをクリア）
   * @AfterEach でトランザクションロールバック

### 3.6 DAO層の結合テストでのDBUnit活用

DAO層のテストでは、DBUnitを使用してデータ駆動テストを実装する:

```java
@Tag("integration")
class OrderDaoIntegrationTest extends BaseIntegrationTest {
    
    private OrderDao orderDao;
    
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        orderDao = container.select(OrderDao.class).get();
    }
    
    @Test
    void testFindByCustomerId_MultipleOrders() throws Exception {
        // Arrange: 顧客1が3件、顧客2が2件の注文を持つ
        loadDataSet("/datasets/orders/multiple-customers-orders.xml");
        
        // Act
        List<OrderTran> orders = orderDao.findByCustomerId(1L);
        
        // Assert
        assertEquals(3, orders.size());
        // 詳細な検証...
    }
    
    @Test
    void testDeleteOrder_CascadeDetails() throws Exception {
        // Arrange
        loadDataSet("/datasets/orders/order-with-details.xml");
        
        // Act
        orderDao.delete(1L);
        em.flush();
        em.clear();
        
        // Assert: 注文と明細がすべて削除されている
        ITable orderTable = getDatabaseTable("ORDER_TRAN");
        assertEquals(0, orderTable.getRowCount());
        
        ITable detailTable = getDatabaseTable("ORDER_DETAIL");
        assertEquals(0, detailTable.getRowCount());
    }
}
```

---

## 4. テストの実行と評価

結合テストコード生成後、以下のステップを実施する:

### 3.1 テスト実行

Gradleタスクを使用して結合テストを実行:

```bash
cd {project_root}
./gradlew integrationTest
```

* `integrationTest` タスクは、@Tag("integration") が付与されたテストを実行する
* プロジェクトのbuild.gradleに定義されたタスク名に従うこと

### 3.2 テスト評価

テスト実行後、@agent_skills/jakarta-ee-api-agile/instructions/test_evaluation.md を使用して結果を評価する:

```yaml
project_root: "{project_root}"
jacoco_reports_dir: "{project_root}/build/reports/jacoco/integrationTest"
test_type: "integration"
spec_directory: "{spec_directory}"
```

---

## 5. 参考資料

* [e2e_test_generation.md](e2e_test_generation.md) - E2Eテスト生成
* ウォーターフォール版: @agent_skills/jakarta-ee-api-base/instructions/it_generation.md
* DBUnit公式ドキュメント: http://dbunit.sourceforge.net/
* DBUnit使用例: http://dbunit.sourceforge.net/bestpractices.html
