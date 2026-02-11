# 単体テストコード生成インストラクション（アジャイル）

## パラメータ設定

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "SPECディレクトリのパス（オプション、デフォルト: {project_root}/specs/baseline）"
target: "common"  # または "usecases/order-creation" のように usecases/{フォルダ名}
```

* 例: 業務共通（common）の単体テスト生成
```yaml
project_root: "projects/sdd-agile/bookstore/berry-books-api"
target: "common"
```

* 例: ユースケースの単体テスト生成
```yaml
project_root: "projects/sdd-agile/bookstore/berry-books-api"
target: "usecases/order-creation"
```

注意: パス区切りはOS環境に応じて調整する。以降、`{project_root}`, `{spec_directory}`, `{target}` はパラメータで設定した値に置き換える。`spec_directory` 未指定時は `{project_root}/specs/baseline` とする。

---

## 概要

このインストラクションは、本番コード（@agent_skills/jakarta-ee-api-agile/instructions/code_generation.md で生成されたコード）に対する単体テストコードを生成するためのものである。

重要な原則:
* **コンテキストの分離**: 本番コード生成とは別タスクとして実行することで、コンテキストを明確に分ける
* **ブラックボックステストとホワイトボックステストの両立**: 外形的な振る舞いの正しさと内部のカバレッジを両方確保する
* **アジャイルにおける位置づけ**: 何度でも繰り返し実行することを前提とする。既存のテストコードに対して、SPEC に基づく差分を反映させる

重要: 指定された target のみを実行し、完了したら停止する。次の対象に自動的に進んではいけない。

---

## 1. 対象の判別

* `target` パラメータから、common 用かユースケース用かを判別する
* `target` が `common` → 業務共通の単体テスト生成
* `target` が `usecases/{フォルダ名}` → そのユースケースの単体テスト生成（usecase_folder はフォルダ名とする）

---

## 2. テストコンテキストをロードして分析する（共通）

### 読み込むべきドキュメント（共通）

1. Agent Skillsルール: @agent_skills/jakarta-ee-api-agile/principles/ を最優先で確認する

2. 共通SPEC（常に参照）:
   * `{spec_directory}/common/architecture_design.md` - 技術スタック、レイヤー、テスト戦略、カバレッジ目標
   * `{spec_directory}/common/data_model.md` - テーブル定義、ERD
   * `{spec_directory}/common/external_interface.md` - 外部連携（該当時）

3. 既存の本番コード: `{project_root}/src/main/java` 配下の実装コードを確認する

---

## 3. テストコンテキストをロードして分析する（target=common の場合）

* 上記「共通」のみ。common の3SPECを駆動元に単体テストを生成する。
* 機能要件の「唯一の真実」は common の3SPEC。

---

## 4. テストコンテキストをロードして分析する（target=usecases/{名} の場合）

* 上記「共通」に加え:
* `{spec_directory}/usecases/{usecase_folder}/userstory.md` - ユーザーストーリー、受入基準、API仕様、ビジネスルール
* `{spec_directory}/usecases/{usecase_folder}/behaviors.md` - 振る舞い・テストシナリオ（Gherkin記法）
* common の3SPECで既に定義された Entity/Dao 等を参照する

---

## 5. 単体テスト生成の基本方針

### 5.1 テストの二つの観点

単体テストは、以下の二つの観点を統合して設計する：

#### ブラックボックステスト（外形的な振る舞いの検証）

* **目的**: コンポーネントの外部から見た振る舞いの正しさを検証する
* **駆動元**: 
  * target=common: common の3SPECから抽出した振る舞い
  * target=usecases/{名}: `{spec_directory}/usecases/{usecase_folder}/behaviors.md` の Gherkin シナリオ
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
* **駆動元**: 
  * 既存の本番コード（`{project_root}/src/main/java`）のメソッドシグネチャと実装詳細
  * common/architecture_design.md のテスト戦略
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

### 5.2 両観点の統合

* Gherkin シナリオ（または SPEC から抽出した振る舞い）をベースにテストケースを設計し、それに加えてコードカバレッジを確保するための追加テストケースを作成する
* 一つのテストケースが複数の観点（振る舞い + カバレッジ）をカバーすることもある
* ブラックボックステストでカバーされない内部パスは、ホワイトボックステストで補完する

---

## 6. テストスコープとモック戦略

### 6.1 テストスコープ

* テストスコープ: タスク粒度内
  * target=common: common に含まれるコンポーネント（Entity、Dao、Filter等）をテスト
  * target=usecases/{名}: そのユースケースに含まれるコンポーネント（Resource、Service、DTO等）をテスト
  * 同一対象内のコンポーネント間は実際の連携でテスト可能
  * 対象外の依存関係はモックを使用

### 6.2 モック使用の判断基準

* 同一対象内のコンポーネント → モック不要（実際の連携をテスト）
  * 例（usecases/order-creation内）: OrderResource → OrderService → OrderDao
* 対象外の依存関係 → モックを使用
  * 例: OrderService が AuthService（common）に依存する場合、AuthService はモック
  * 例: EntityManager、外部APIクライアント等はモック

---

## 7. テストケース設計

### 7.1 ブラックボックステストケースの設計（target=usecases/{名} の場合）

`{spec_directory}/usecases/{usecase_folder}/behaviors.md` の Gherkin シナリオを参考に、**JUnit 5** の通常のテストクラスとテストメソッドを生成する。

**Gherkin シナリオからテストメソッドへの変換**

Gherkin記法:
```gherkin
Feature: 注文の作成
  Scenario: 新規注文を作成する
    Given 有効な顧客IDと商品リストが準備されている
    When POST /api/orders でリクエストを送信する
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
    when(mockOrderService.createOrder(request)).thenReturn(testOrder);
    
    // When: POST /api/orders でリクエストを送信する
    Response response = orderResource.createOrder(request);
    
    // Then: 注文が正常に作成される
    assertEquals(201, response.getStatus());
    OrderResponse body = (OrderResponse) response.getEntity();
    assertNotNull(body.getOrderId());
    
    // Serviceが呼び出されたことを検証
    verify(mockOrderService).createOrder(request);
}
```

### 7.2 ブラックボックステストケースの設計（target=common の場合）

common の3SPEC（architecture_design.md、data_model.md、external_interface.md）から抽出した振る舞いをベースにテストケースを設計する。

**例: Entity のバリデーション**

```java
@Test
@DisplayName("OrderTran作成 - 正常系")
void testCreateOrderTran_Success() {
    // Given: 有効なOrderTranオブジェクト
    OrderTran orderTran = new OrderTran();
    orderTran.setCustomerId(1L);
    orderTran.setTotalAmount(new BigDecimal("5000"));
    
    // When: Entityを作成
    Set<ConstraintViolation<OrderTran>> violations = validator.validate(orderTran);
    
    // Then: バリデーションエラーなし
    assertTrue(violations.isEmpty());
}
```

### 7.3 ホワイトボックステストケースの設計

既存の本番コードの各メソッドシグネチャに対して、以下のテストメソッドを作成する：

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
```

### 7.4 カバレッジ目標

* `{spec_directory}/common/architecture_design.md` で指定されたカバレッジ目標を遵守する
* 一般的な目標値: 行カバレッジ 80%以上、分岐カバレッジ 70%以上
* Gherkin シナリオでカバーされないパスは、ホワイトボックステストで補完する

---

## 8. テストクラスの構造

### 8.1 基本構造

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

### 8.2 @Nested を使用した構造化

* ブラックボックステストとホワイトボックステストを `@Nested` で明確に分ける
* Gherkin シナリオに基づくテストは「振る舞いテスト」として、境界値・エッジケースは「カバレッジ確保テスト」として分類する

---

## 9. テストデータの準備

### 9.1 テストデータのソース

* target=usecases/{名}: `{spec_directory}/usecases/{usecase_folder}/behaviors.md` の具体例
* target=usecases/{名}: `{spec_directory}/usecases/{usecase_folder}/userstory.md` のビジネスルール
* target=common: `{spec_directory}/common/data_model.md` のテーブル定義
* target=common: `{spec_directory}/common/architecture_design.md` の共通仕様

### 9.2 テストデータの管理

* テストデータは各テストケース内でセットアップする（テストの独立性を保つ）
* @BeforeEach でテストデータの初期化を行う
* 共通のテストデータは、テストクラスのフィールドとして定義する

---

## 10. テストフレームワークとツール

### 10.1 必須フレームワーク

* **テストフレームワーク: JUnit 5 のみ**（Cucumberは使用しない）
* モックフレームワーク: Mockito
* アサーションライブラリ: JUnit 5 Assertions、AssertJ（オプション）

### 10.2 アノテーションの活用

* `@ExtendWith(MockitoExtension.class)` - Mockitoの有効化
* `@InjectMocks` - テスト対象のオブジェクトに依存関係を注入
* `@Mock` - モックオブジェクトの作成
* `@BeforeEach` - 各テストメソッド実行前の初期化
* `@Nested` - テストクラスの構造化
* `@DisplayName` - テストケースの説明

---

## 11. 実装のステップ

### ステップ1: テストコンテキストの理解

1. common/architecture_design.md からテスト戦略を確認
2. target に応じた SPEC を確認（common の3SPEC、または userstory.md + behaviors.md）
3. 既存の本番コードを確認

### ステップ2: テストクラスの作成

1. テスト対象のコンポーネントごとにテストクラスを作成
2. `@ExtendWith(MockitoExtension.class)` を付与
3. テスト対象のオブジェクトと依存関係をフィールドとして定義

### ステップ3: ブラックボックステストの生成

1. target=usecases/{名}: behaviors.md の Gherkin シナリオを確認し、各シナリオに対応する JUnit 5 テストメソッドを作成
2. target=common: common の3SPEC から抽出した振る舞いに基づくテストメソッドを作成
3. Given-When-Then の流れでテストロジックを記述
4. `@Nested` で「振る舞いテスト」としてグループ化

### ステップ4: ホワイトボックステストの生成

1. 既存の本番コードの各メソッドシグネチャを確認
2. 正常系、異常系、境界値、エッジケースのテストメソッドを作成
3. カバレッジ目標を達成するための追加テストケースを作成
4. `@Nested` で「カバレッジ確保テスト」としてグループ化

### ステップ5: テストデータの準備

1. @BeforeEach でテストデータの初期化を行う
2. behaviors.md や userstory.md、data_model.md から具体例を参照

### ステップ6: 検証

1. 全テストが実行可能であることを確認
2. カバレッジ目標を達成していることを確認
3. behaviors.md のシナリオがすべてカバーされていることを確認（target=usecases/{名} の場合）

---

## 12. コンポーネント別のテスト設計

### 12.1 Entityのテスト（target=common）

* **ブラックボックス**: エンティティの振る舞い（バリデーション、リレーションシップ）
* **ホワイトボックス**: getter/setter、equals/hashCode、制約違反

### 12.2 Daoのテスト（target=common）

* **ブラックボックス**: CRUD操作の正しさ、検索条件の正しさ
* **ホワイトボックス**: 境界値（null、空リスト）、JPQLの正しさ
* **DBUnitの活用（推奨）**: DAO層のテストでは、DBUnitを使用したデータ駆動テストを実装することを推奨
  * テストデータをXML/CSV形式で外部管理
  * データベースの初期状態を明示的に定義
  * 期待するデータベース状態との比較検証

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
    @DisplayName("注文IDで検索 - 存在しない場合はnull")
    void testFindById_NotFound() throws Exception {
        // Given: 空のデータベース
        IDataSet dataSet = new FlatXmlDataSetBuilder()
            .build(getClass().getResourceAsStream("/datasets/dao/empty.xml"));
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
        
        when(entityManager.find(OrderTran.class, 999L)).thenReturn(null);
        
        // When: 存在しないIDで検索
        OrderTran result = orderDao.findById(999L);
        
        // Then: nullが返される
        assertNull(result);
    }
}
```

**テストデータセット例（/datasets/dao/orders-by-customer.xml）:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<dataset>
  <CUSTOMER CUSTOMER_ID="100" EMAIL="customer@example.com" NAME="Test Customer" />
  <ORDER_TRAN ORDER_TRAN_ID="1" CUSTOMER_ID="100" TOTAL_AMOUNT="5000" STATUS="COMPLETED" />
  <ORDER_TRAN ORDER_TRAN_ID="2" CUSTOMER_ID="100" TOTAL_AMOUNT="3000" STATUS="PENDING" />
</dataset>
```

### 12.3 Serviceのテスト（target=usecases/{名}）

* **ブラックボックス**: ビジネスロジックの正しさ、トランザクション境界
* **ホワイトボックス**: 例外ハンドリング、分岐パス、エッジケース

### 12.4 Resource（JAX-RSエンドポイント）のテスト（target=usecases/{名}）

* **ブラックボックス**: HTTPステータスコード、レスポンス形式、認証・認可
* **ホワイトボックス**: バリデーションエラー、境界値、エラーレスポンス

---

## 13. 既存コードの扱い

* 既存のテストコードが存在する場合は、現在の SPEC と既存テストコードの差分を検出し、必要な追加・修正・削除のみを反映する（漸進的更新）
* ファイルをゼロから作り直すのではなく、既存の内容を尊重して必要な部分のみを追加・修正する
* 新規テストケースの追加、既存テストケースの修正、不要なテストの削除など、必要な変更のみを適用する

---

## 14. 完了検証

* 本番コードに対応する単体テストが生成されていることを確認する
* ブラックボックステスト（Gherkinシナリオベース、または SPEC ベース）が実装されていることを確認する
* ホワイトボックステスト（境界値・エッジケース）が実装されていることを確認する
* カバレッジ目標（common/architecture_design.md）を達成していることを確認する
* 全てのテストケースがコンパイル可能で、実行可能であることを確認する

---

## 15. 次のステップ

単体テストコード生成完了後は、以下を実施する：

1. 単体テスト実行: @agent_skills/jakarta-ee-api-base/instructions/unit_test_execution.md に従い単体テストを実行し、動作・カバレッジ・不足ケースを確認する
2. 不足しているテストケースを追加する
3. 必要に応じてSPEC→コード生成→テスト生成→テスト実行のループを行う

---

## 参考資料

* [コード生成インストラクション](code_generation.md) - 本番コード生成
* ウォーターフォール版: @agent_skills/jakarta-ee-api-base/instructions/unit_test_generation.md（パス・参照元の違いを除き実行方針は同一）
* [単体テスト実行インストラクション](../../../jakarta-ee-api-base/instructions/unit_test_execution.md) - 単体テスト実行・評価
