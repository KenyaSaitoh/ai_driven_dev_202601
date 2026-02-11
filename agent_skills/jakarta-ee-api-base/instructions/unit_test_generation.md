# 単体テストコード生成インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
target_domain: "対象ドメイン名"
```

* 例: ordersドメインの単体テスト生成
```yaml
project_root: "projects/sdd-wf/bookstore/back-office-api"
spec_directory: "projects/sdd-wf/bookstore/back-office-api/specs/baseline"
target_domain: "orders"
```

注意
* パス区切りはOS環境に応じて調整する（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換える
* 以降、`{spec_directory}` と表記されている箇所は、上記で設定した値に置き換える

---

## 概要

このインストラクションは、本番コード（@agent_skills/jakarta-ee-api-base/instructions/code_generation.md で生成されたコード）に対する単体テストコードを生成するためのものである。

重要な原則:
* **コンテキストの分離**: 本番コード生成とは別タスクとして実行することで、コンテキストを明確に分ける
* **ブラックボックステストとホワイトボックステストの両立**: 外形的な振る舞いの正しさと内部のカバレッジを両方確保する

---

## 1. テストコンテキストをロードして分析する

### 読み込むべきドキュメント（優先順）

1. Agent Skillsルール（最優先で確認）
   * @agent_skills/jakarta-ee-api-base/principles/ - Jakarta EE開発の原則、テスト戦略を確認する

2. 必須: `{spec_directory}/basic_design/common/architecture_design.md` で以下を確認する
   * テスト戦略（テストフレームワーク、カバレッジ目標、テスト方針）
   * 技術スタック（言語、バージョン、フレームワーク、ライブラリ）

3. 必須: `{spec_directory}/detailed_design/{target_domain}/detailed_design.md` で対象ドメインの詳細設計を確認する
   * 実装クラス設計、メソッドシグネチャ、アノテーション等
   * ホワイトボックステストの基盤となる実装詳細

4. 必須: `{spec_directory}/detailed_design/{target_domain}/behaviors.md` で対象ドメインの振る舞い仕様を確認する
   * Gherkin記法で記述されたテストシナリオ
   * ブラックボックステストの基盤となる振る舞い仕様

5. 必須: `{spec_directory}/basic_design/{target_domain}/functional_design.md` で対象ドメインの機能設計を確認する
   * ドメイン固有の機能要件、ビジネスルール

6. 存在する場合: `{spec_directory}/basic_design/common/data_model.md` でテーブル定義とERDを確認する

7. 既存の本番コード: `{project_root}/src/main/java` 配下の実装コードを確認する
   * Entity、Dao、Service、Resource、DTO等の実装

---

## 2. 単体テスト生成の基本方針

### 2.1 テストの二つの観点

単体テストは、以下の二つの観点を統合して設計する：

#### ブラックボックステスト（外形的な振る舞いの検証）

* **目的**: コンポーネントの外部から見た振る舞いの正しさを検証する
* **駆動元**: `{spec_directory}/detailed_design/{target_domain}/behaviors.md` の Gherkin シナリオ
* **焦点**: 
  * 入力と出力の関係
  * ビジネスルールの遵守
  * エラーハンドリングの正しさ
  * API契約の遵守
* **テスト設計**:
  * Given（前提条件）: テストデータ、モックのスタブ設定
  * When（操作）: メソッド呼び出し
  * Then（期待結果）: 戻り値、状態、副作用の検証

#### ホワイトボックステスト（内部カバレッジの確保）

* **目的**: コードの内部構造を理解し、すべてのパスとロジックが正しく動作することを検証する
* **駆動元**: `{spec_directory}/detailed_design/{target_domain}/detailed_design.md` のメソッドシグネチャと実装詳細
* **焦点**:
  * コードカバレッジ（行カバレッジ、分岐カバレッジ）
  * 境界値テスト
  * エッジケース
  * 内部状態の変化
* **テスト設計**:
  * 正常系テスト（期待する戻り値が返されるか）
  * 異常系テスト（例外が適切にスローされるか）
  * 境界値テスト（null、空文字列、最大値、最小値等）
  * エッジケーステスト（特殊なケース）

### 2.2 両観点の統合

* Gherkin シナリオをベースにテストケースを設計し、それに加えてコードカバレッジを確保するための追加テストケースを作成する
* 一つのテストケースが複数の観点（振る舞い + カバレッジ）をカバーすることもある
* ブラックボックステストでカバーされない内部パスは、ホワイトボックステストで補完する

---

## 3. テストスコープとモック戦略

### 3.1 テストスコープ

* テストスコープ: ドメインの粒度内
  * 対象ドメイン（例: common, orders, books_proxy）に含まれるコンポーネントをテスト
  * ドメイン内のコンポーネント間は実際の連携でテスト可能
  * ドメイン外の依存関係はモックを使用

### 3.2 モック使用の判断基準

* 同じドメイン内のコンポーネント → モック不要（実際の連携をテスト）
  * 例（ordersドメイン内）: OrderResource → OrderService → OrderDao
* ドメイン外の依存関係 → モックを使用
  * 例: OrderService が AuthService（commonドメイン）に依存する場合、AuthService はモック
  * 例: EntityManager、外部APIクライアント等はモック

---

## 4. テストケース設計

### 4.1 ブラックボックステストケースの設計

`{spec_directory}/detailed_design/{target_domain}/behaviors.md` の Gherkin シナリオを参考に、**JUnit 5** の通常のテストクラスとテストメソッドを生成する。

**Gherkin シナリオからテストメソッドへの変換**

Gherkin記法:
```gherkin
Feature: 注文の作成
  Scenario: 新規注文を作成する
    Given 有効な顧客IDと商品リストが準備されている
    When OrderService.createOrder()を呼び出す
    Then 注文が正常に作成される
    And 注文IDが返される
```

JUnit 5テストメソッド:
```java
@Test
@DisplayName("新規注文を作成する - Gherkinシナリオベース")
void testCreateOrder_Success_FromBehavior() {
    // Given: 有効な顧客IDと商品リストが準備されている
    Long customerId = 1L;
    List<OrderItemRequest> items = Arrays.asList(
        new OrderItemRequest(101L, 2),
        new OrderItemRequest(102L, 1)
    );
    CreateOrderRequest request = new CreateOrderRequest(customerId, items);
    
    // モックのスタブ設定
    when(mockEntityManager.find(Customer.class, customerId)).thenReturn(testCustomer);
    when(mockEntityManager.find(Product.class, 101L)).thenReturn(testProduct1);
    when(mockEntityManager.find(Product.class, 102L)).thenReturn(testProduct2);
    
    // When: OrderService.createOrder()を呼び出す
    OrderTran result = orderService.createOrder(request);
    
    // Then: 注文が正常に作成される
    assertNotNull(result);
    assertNotNull(result.getOrderTranId());
    assertEquals(customerId, result.getCustomerId());
    assertEquals(2, result.getOrderDetails().size());
    
    // 永続化が呼び出されたことを検証
    verify(mockEntityManager).persist(any(OrderTran.class));
}
```

### 4.2 ホワイトボックステストケースの設計

`{spec_directory}/detailed_design/{target_domain}/detailed_design.md` の各メソッドシグネチャに対して、以下のテストメソッドを作成する：

* 正常系テスト（期待する戻り値が返されるか）
* 異常系テスト（例外が適切にスローされるか）
* 境界値テスト（null、空文字列、最大値、最小値等）
* エッジケーステスト

**例: 境界値テスト**

```java
@Test
@DisplayName("注文作成 - 商品リストが空の場合、例外をスロー（境界値）")
void testCreateOrder_EmptyItemsList_ThrowsException() {
    // Given: 商品リストが空
    Long customerId = 1L;
    List<OrderItemRequest> emptyItems = Collections.emptyList();
    CreateOrderRequest request = new CreateOrderRequest(customerId, emptyItems);
    
    // When & Then: BusinessExceptionがスローされる
    BusinessException exception = assertThrows(BusinessException.class, () -> {
        orderService.createOrder(request);
    });
    assertEquals("ERR_EMPTY_ITEMS", exception.getCode());
}

@Test
@DisplayName("注文作成 - 顧客IDがnullの場合、例外をスロー（境界値）")
void testCreateOrder_NullCustomerId_ThrowsException() {
    // Given: 顧客IDがnull
    Long customerId = null;
    List<OrderItemRequest> items = Arrays.asList(new OrderItemRequest(101L, 2));
    CreateOrderRequest request = new CreateOrderRequest(customerId, items);
    
    // When & Then: IllegalArgumentExceptionがスローされる
    assertThrows(IllegalArgumentException.class, () -> {
        orderService.createOrder(request);
    });
}

@Test
@DisplayName("注文作成 - 商品数量が最大値の場合（境界値）")
void testCreateOrder_MaxQuantity() {
    // Given: 商品数量が最大値
    Long customerId = 1L;
    List<OrderItemRequest> items = Arrays.asList(
        new OrderItemRequest(101L, Integer.MAX_VALUE)
    );
    CreateOrderRequest request = new CreateOrderRequest(customerId, items);
    
    when(mockEntityManager.find(Customer.class, customerId)).thenReturn(testCustomer);
    when(mockEntityManager.find(Product.class, 101L)).thenReturn(testProduct1);
    
    // When: 注文を作成
    OrderTran result = orderService.createOrder(request);
    
    // Then: 正常に作成される
    assertNotNull(result);
    assertEquals(Integer.MAX_VALUE, result.getOrderDetails().get(0).getQuantity());
}
```

### 4.3 カバレッジ目標

* `{spec_directory}/basic_design/common/architecture_design.md` で指定されたカバレッジ目標を遵守する
* 一般的な目標値: 行カバレッジ 80%以上、分岐カバレッジ 70%以上
* Gherkin シナリオでカバーされないパスは、ホワイトボックステストで補完する

---

## 5. テストクラスの構造

### 5.1 基本構造

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @InjectMocks
    private OrderService orderService;
    
    @Mock
    private EntityManager entityManager;
    
    @Mock
    private OrderTranDao orderTranDao;
    
    // テストデータ
    private Customer testCustomer;
    private Product testProduct1;
    private Product testProduct2;
    
    @BeforeEach
    void setUp() {
        // テストデータの初期化
        testCustomer = new Customer(1L, "test@example.com", "Test User");
        testProduct1 = new Product(101L, "Product 1", 1000);
        testProduct2 = new Product(102L, "Product 2", 2000);
    }
    
    // ========================================
    // ブラックボックステスト（Gherkinシナリオベース）
    // ========================================
    
    @Nested
    @DisplayName("注文作成の振る舞い（Gherkinシナリオ）")
    class CreateOrderBehaviorTests {
        
        @Test
        @DisplayName("Scenario: 新規注文を作成する")
        void testCreateOrder_Success_FromBehavior() {
            // Gherkin シナリオベースのテスト
        }
        
        @Test
        @DisplayName("Scenario: 存在しない顧客IDで注文作成に失敗する")
        void testCreateOrder_CustomerNotFound_FromBehavior() {
            // Gherkin シナリオベースのテスト
        }
    }
    
    // ========================================
    // ホワイトボックステスト（カバレッジ確保）
    // ========================================
    
    @Nested
    @DisplayName("注文作成の境界値・エッジケース")
    class CreateOrderEdgeCaseTests {
        
        @Test
        @DisplayName("境界値: 商品リストが空")
        void testCreateOrder_EmptyItemsList() {
            // 境界値テスト
        }
        
        @Test
        @DisplayName("境界値: 顧客IDがnull")
        void testCreateOrder_NullCustomerId() {
            // 境界値テスト
        }
        
        @Test
        @DisplayName("エッジケース: 商品数量が最大値")
        void testCreateOrder_MaxQuantity() {
            // エッジケーステスト
        }
    }
}
```

### 5.2 @Nested を使用した構造化

* ブラックボックステストとホワイトボックステストを `@Nested` で明確に分ける
* Gherkin シナリオに基づくテストは「振る舞いテスト」として、境界値・エッジケースは「カバレッジ確保テスト」として分類する

---

## 6. テストデータの準備

### 6.1 テストデータのソース

* `{spec_directory}/detailed_design/{target_domain}/behaviors.md` の具体例
* `{spec_directory}/basic_design/{target_domain}/functional_design.md` のビジネスルール
* `{spec_directory}/basic_design/common/data_model.md` のテーブル定義

### 6.2 テストデータの管理

* テストデータは各テストケース内でセットアップする（テストの独立性を保つ）
* @BeforeEach でテストデータの初期化を行う
* 共通のテストデータは、テストクラスのフィールドとして定義する

---

## 7. テストフレームワークとツール

### 7.1 必須フレームワーク

* **テストフレームワーク: JUnit 5 のみ**（Cucumberは使用しない）
* モックフレームワーク: Mockito
* アサーションライブラリ: JUnit 5 Assertions、AssertJ（オプション）

### 7.2 アノテーションの活用

* `@ExtendWith(MockitoExtension.class)` - Mockitoの有効化
* `@InjectMocks` - テスト対象のオブジェクトに依存関係を注入
* `@Mock` - モックオブジェクトの作成
* `@BeforeEach` - 各テストメソッド実行前の初期化
* `@Nested` - テストクラスの構造化
* `@DisplayName` - テストケースの説明

---

## 8. 実装のステップ

### ステップ1: テストコンテキストの理解

1. architecture_design.md からテスト戦略を確認
2. detailed_design.md から実装詳細を確認
3. behaviors.md から振る舞い仕様を確認
4. 既存の本番コードを確認

### ステップ2: テストクラスの作成

1. テスト対象のコンポーネントごとにテストクラスを作成
2. `@ExtendWith(MockitoExtension.class)` を付与
3. テスト対象のオブジェクトと依存関係をフィールドとして定義

### ステップ3: ブラックボックステストの生成

1. behaviors.md の Gherkin シナリオを確認
2. 各シナリオに対応する JUnit 5 テストメソッドを作成
3. Given-When-Then の流れでテストロジックを記述
4. `@Nested` で「振る舞いテスト」としてグループ化

### ステップ4: ホワイトボックステストの生成

1. detailed_design.md の各メソッドシグネチャを確認
2. 正常系、異常系、境界値、エッジケースのテストメソッドを作成
3. カバレッジ目標を達成するための追加テストケースを作成
4. `@Nested` で「カバレッジ確保テスト」としてグループ化

### ステップ5: テストデータの準備

1. @BeforeEach でテストデータの初期化を行う
2. behaviors.md や functional_design.md から具体例を参照

### ステップ6: 検証

1. 全テストが実行可能であることを確認
2. カバレッジ目標を達成していることを確認
3. behaviors.md のシナリオがすべてカバーされていることを確認

---

## 9. コンポーネント別のテスト設計

### 9.1 Entityのテスト

* **ブラックボックス**: エンティティの振る舞い（バリデーション、リレーションシップ）
* **ホワイトボックス**: getter/setter、equals/hashCode、制約違反

### 9.2 Daoのテスト

* **ブラックボックス**: CRUD操作の正しさ、検索条件の正しさ
* **ホワイトボックス**: 境界値（null、空リスト）、JPQLの正しさ
* **DBUnitの活用（推奨）**: DAO層のテストでは、DBUnitを使用したデータ駆動テストを実装することを推奨
  * テストデータをXML/CSV形式で外部管理
  * データベースの初期状態を明示的に定義
  * 期待するデータベース状態との比較検証
  * 複数のテストケースで共通のテストデータを再利用

**DBUnitを使用したDAOテストの例:**
```java
@ExtendWith(MockitoExtension.class)
class OrderDaoTest {
    
    @InjectMocks
    private OrderDao orderDao;
    
    @Mock
    private EntityManager entityManager;
    
    private IDatabaseTester databaseTester;
    
    @BeforeEach
    void setUp() throws Exception {
        // DBUnitのセットアップ
        databaseTester = new JdbcDatabaseTester(
            "org.hsqldb.jdbcDriver", 
            "jdbc:hsqldb:mem:testdb", 
            "SA", ""
        );
        
        // DatabaseConfigの設定
        DatabaseConfig config = databaseTester.getConnection().getConfig();
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, 
            new HsqldbDataTypeFactory());
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (databaseTester != null) {
            databaseTester.onTearDown();
        }
    }
    
    @Test
    @DisplayName("顧客IDで注文を検索 - 複数件存在する場合")
    void testFindByCustomerId_MultipleOrders() throws Exception {
        // Given: DBUnitでテストデータを投入
        IDataSet dataSet = new FlatXmlDataSetBuilder()
            .setColumnSensing(true)
            .build(getClass().getResourceAsStream("/datasets/dao/orders-by-customer.xml"));
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
        
        // モックの設定（実際のクエリ結果を返す）
        TypedQuery<OrderTran> mockQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(OrderTran.class)))
            .thenReturn(mockQuery);
        
        List<OrderTran> expectedOrders = Arrays.asList(
            createOrderTran(1L, 100L),
            createOrderTran(2L, 100L)
        );
        when(mockQuery.getResultList()).thenReturn(expectedOrders);
        
        // When: 顧客IDで検索
        List<OrderTran> result = orderDao.findByCustomerId(100L);
        
        // Then: 2件の注文が取得される
        assertEquals(2, result.size());
        assertEquals(100L, result.get(0).getCustomerId());
        assertEquals(100L, result.get(1).getCustomerId());
        
        // クエリが正しく実行されたことを検証
        verify(entityManager).createQuery(
            contains("WHERE o.customerId = :customerId"), 
            eq(OrderTran.class)
        );
    }
    
    @Test
    @DisplayName("期間指定で注文を検索 - 日付範囲でフィルタ")
    void testFindByDateRange() throws Exception {
        // Given: DBUnitで複数の日付の注文データを投入
        IDataSet dataSet = new FlatXmlDataSetBuilder()
            .setColumnSensing(true)
            .build(getClass().getResourceAsStream("/datasets/dao/orders-date-range.xml"));
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
        
        // モックの設定
        TypedQuery<OrderTran> mockQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(OrderTran.class)))
            .thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        
        // 期間内の注文のみを返す
        List<OrderTran> expectedOrders = Arrays.asList(
            createOrderTran(1L, 100L, LocalDateTime.of(2024, 1, 15, 10, 0)),
            createOrderTran(2L, 101L, LocalDateTime.of(2024, 1, 20, 14, 30))
        );
        when(mockQuery.getResultList()).thenReturn(expectedOrders);
        
        // When: 2024年1月1日〜31日で検索
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        List<OrderTran> result = orderDao.findByDateRange(startDate, endDate);
        
        // Then: 期間内の注文のみ取得される
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(order -> 
            !order.getOrderDate().toLocalDate().isBefore(startDate) &&
            !order.getOrderDate().toLocalDate().isAfter(endDate)
        ));
        
        // パラメータが正しく設定されたことを検証
        verify(mockQuery).setParameter("startDate", startDate.atStartOfDay());
        verify(mockQuery).setParameter("endDate", endDate.atTime(23, 59, 59));
    }
}
```

**テストデータセット例（/datasets/dao/orders-by-customer.xml）:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<dataset>
  <CUSTOMER CUSTOMER_ID="100" EMAIL="customer@example.com" NAME="Test Customer" />
  <CUSTOMER CUSTOMER_ID="101" EMAIL="customer2@example.com" NAME="Another Customer" />
  
  <PRODUCT PRODUCT_ID="1001" PRODUCT_NAME="Product A" PRICE="1000" />
  <PRODUCT PRODUCT_ID="1002" PRODUCT_NAME="Product B" PRICE="2000" />
  
  <ORDER_TRAN ORDER_TRAN_ID="1" CUSTOMER_ID="100" TOTAL_AMOUNT="5000" 
              STATUS="COMPLETED" ORDER_DATE="2024-01-15 10:00:00" />
  <ORDER_TRAN ORDER_TRAN_ID="2" CUSTOMER_ID="100" TOTAL_AMOUNT="3000" 
              STATUS="PENDING" ORDER_DATE="2024-01-20 14:30:00" />
  <ORDER_TRAN ORDER_TRAN_ID="3" CUSTOMER_ID="101" TOTAL_AMOUNT="8000" 
              STATUS="COMPLETED" ORDER_DATE="2024-01-25 16:45:00" />
</dataset>
```

**テストデータセット例（/datasets/dao/orders-date-range.xml）:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<dataset>
  <CUSTOMER CUSTOMER_ID="100" EMAIL="customer@example.com" NAME="Test Customer" />
  <CUSTOMER CUSTOMER_ID="101" EMAIL="customer2@example.com" NAME="Another Customer" />
  
  <!-- 2024年1月の注文 -->
  <ORDER_TRAN ORDER_TRAN_ID="1" CUSTOMER_ID="100" TOTAL_AMOUNT="5000" 
              STATUS="COMPLETED" ORDER_DATE="2024-01-15 10:00:00" />
  <ORDER_TRAN ORDER_TRAN_ID="2" CUSTOMER_ID="101" TOTAL_AMOUNT="3000" 
              STATUS="PENDING" ORDER_DATE="2024-01-20 14:30:00" />
  
  <!-- 2024年2月の注文（範囲外） -->
  <ORDER_TRAN ORDER_TRAN_ID="3" CUSTOMER_ID="100" TOTAL_AMOUNT="8000" 
              STATUS="COMPLETED" ORDER_DATE="2024-02-05 09:15:00" />
</dataset>
```

**DBUnitを使用するメリット:**
* テストデータの外部管理により、コードとデータを分離
* XMLファイルを見るだけで、テストの前提条件が明確
* 複数のテストケースで共通データセットを再利用可能
* データベースの期待状態を明示的に定義・検証できる

### 9.3 Serviceのテスト

* **ブラックボックス**: ビジネスロジックの正しさ、トランザクション境界
* **ホワイトボックス**: 例外ハンドリング、分岐パス、エッジケース

### 9.4 Resource（JAX-RSエンドポイント）のテスト

* **ブラックボックス**: HTTPステータスコード、レスポンス形式、認証・認可
* **ホワイトボックス**: バリデーションエラー、境界値、エラーレスポンス

---

## 10. 既存コードの扱い

* 既存のテストコードが存在する場合は、それらを削除せずに読み込んで、差分のみを反映する
* ファイルをゼロから作り直すのではなく、既存の内容を尊重して必要な部分のみを追加・修正する
* 新規テストケースの追加、既存テストケースの修正、不要なテストの削除など、必要な変更のみを適用する

---

## 11. 完了検証

* 本番コードに対応する単体テストが生成されていることを確認する
* ブラックボックステスト（Gherkinシナリオベース）が実装されていることを確認する
* ホワイトボックステスト（境界値・エッジケース）が実装されていることを確認する
* カバレッジ目標（architecture_design.md）を達成していることを確認する
* 全てのテストケースがコンパイル可能で、実行可能であることを確認する

---

## 12. 次のステップ

単体テストコード生成完了後は、以下を実施する：

1. 単体テスト実行: @agent_skills/jakarta-ee-api-base/instructions/unit_test_execution.md に従い単体テストを実行し、動作・カバレッジ・不足ケースを確認する
2. 不足しているテストケースを追加する
3. 必要に応じて詳細設計→コード生成→テスト生成→テスト実行のループを行う

---

## 参考資料

* [コード生成インストラクション](code_generation.md) - 本番コード生成
* [単体テスト実行インストラクション](unit_test_execution.md) - 単体テスト実行・評価
* [Jakarta EE開発原則](../principles/) - アーキテクチャ標準、品質基準
