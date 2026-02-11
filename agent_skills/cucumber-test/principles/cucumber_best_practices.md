# Cucumberベストプラクティス

バージョン: 1.0.0  
最終更新日: 2026-02-11

---

## 概要

このドキュメントは、Jakarta EEプロジェクトでCucumberを使用する際のベストプラクティスをまとめています。

---

## 1. Gherkinシナリオの記述

### 1.1 基本原則

**ビジネス言語で記述**: 技術的な詳細は避け、ビジネス要件を自然言語で表現します。

**良い例**:
```gherkin
シナリオ: 顧客が書籍を注文する
  前提 顧客"alice@example.com"でログインしている
  かつ 書籍"Java入門"の在庫が10冊ある
  もし 書籍"Java入門"を2冊カートに追加する
  かつ 注文を確定する
  ならば 注文が成功する
  かつ 在庫が8冊になる
```

**悪い例**:
```gherkin
シナリオ: 注文テスト
  前提 OrderServiceが初期化されている
  もし createOrder()メソッドを呼び出す
  ならば データベースにINSERTされる
```

### 1.2 Given-When-Then構文

**Given（前提条件）**: テストの前提条件を記述
```gherkin
前提 顧客"alice@example.com"でログインしている
かつ 書籍ID 1 の在庫が10冊ある
```

**When（アクション）**: 実行する操作を記述
```gherkin
もし 書籍ID 1 を2冊カートに追加する
かつ 注文を確定する
```

**Then（期待結果）**: 期待される結果を記述
```gherkin
ならば 注文が成功する
かつ 注文番号が発行される
かつ 在庫が8冊になる
```

### 1.3 データテーブル

複数のデータを扱う場合は、データテーブルを使用します。

```gherkin
シナリオ: 複数の書籍を注文する
  前提 以下の書籍が在庫に存在する
    | 書籍ID | 書籍名       | 価格  | 在庫数 |
    | 1      | Java入門     | 3000  | 10     |
    | 2      | Python基礎   | 2800  | 5      |
    | 3      | Ruby実践     | 3200  | 8      |
  もし 以下の書籍をカートに追加する
    | 書籍ID | 冊数 |
    | 1      | 2    |
    | 2      | 1    |
  かつ 注文を確定する
  ならば 注文が成功する
```

### 1.4 シナリオアウトライン

同じシナリオを複数のデータパターンでテストする場合は、シナリオアウトラインを使用します。

```gherkin
シナリオアウトライン: 書籍を注文する
  前提 書籍ID <書籍ID> の在庫が<在庫数>冊ある
  もし 書籍ID <書籍ID> を<注文数>冊カートに追加する
  かつ 注文を確定する
  ならば 注文が<結果>する

  例:
    | 書籍ID | 在庫数 | 注文数 | 結果 |
    | 1      | 10     | 2      | 成功 |
    | 2      | 5      | 3      | 成功 |
    | 3      | 1      | 2      | 失敗 |
```

---

## 2. Step Definitionsの実装

### 2.1 再利用可能なステップ

共通ステップは`CommonSteps`クラスに集約します。

```java
public class CommonSteps {
    
    @前提("テストデータベースが初期化されている")
    public void initializeDatabase() {
        testDatabase.initialize();
    }
    
    @前提("顧客{string}でログインしている")
    public void loginAsCustomer(String email) {
        testContext.setCurrentCustomer(email);
    }
}
```

### 2.2 パラメータ化

正規表現でパラメータを受け取ります。

```java
// 整数パラメータ
@かつ("書籍ID {int} の在庫が{int}冊ある")
public void setStockForBook(int bookId, int stock) {
    testContext.setStock(bookId, stock);
}

// 文字列パラメータ
@前提("顧客{string}でログインしている")
public void loginAsCustomer(String email) {
    testContext.setCurrentCustomer(email);
}

// 浮動小数点パラメータ
@ならば("合計金額が{double}円である")
public void verifyTotalPrice(double expectedPrice) {
    assertEquals(expectedPrice, testContext.getTotalPrice());
}
```

### 2.3 DataTableの使用

データテーブルは`DataTable`型で受け取ります。

```java
@前提("以下の書籍が在庫に存在する")
public void setupBooks(DataTable dataTable) {
    List<Map<String, String>> rows = dataTable.asMaps();
    for (Map<String, String> row : rows) {
        int bookId = Integer.parseInt(row.get("書籍ID"));
        String bookName = row.get("書籍名");
        int price = Integer.parseInt(row.get("価格"));
        int stock = Integer.parseInt(row.get("在庫数"));
        
        testContext.addBook(bookId, bookName, price, stock);
    }
}
```

### 2.4 アサーション

JUnit Assertionsを使用して期待結果を検証します。

```java
@ならば("注文が成功する")
public void orderShouldSucceed() {
    assertNull(testContext.getException(), "例外が発生してはならない");
    assertNotNull(testContext.getOrder(), "注文が作成されている必要がある");
    assertTrue(testContext.getOrder().getTranId() > 0, "注文番号が発行されている必要がある");
}

@ならば("エラーメッセージ{string}が表示される")
public void errorMessageShouldBeDisplayed(String expectedMessage) {
    assertNotNull(testContext.getException(), "例外が発生している必要がある");
    assertTrue(
        testContext.getException().getMessage().contains(expectedMessage),
        "エラーメッセージに「" + expectedMessage + "」が含まれている必要がある"
    );
}
```

---

## 3. CDI統合

### 3.1 Weld SEの使用

Weld SEを使用してCDI Beanを起動します。

```java
public class Hooks {
    
    private static SeContainer container;
    
    @BeforeAll
    public static void startCdiContainer() {
        container = SeContainerInitializer.newInstance().initialize();
    }
    
    @AfterAll
    public static void stopCdiContainer() {
        if (container != null) {
            container.close();
        }
    }
}
```

### 3.2 依存性注入

`@Inject`でサービス層・DAO層を注入します。

```java
public class OrderManagementSteps {
    
    @Inject
    private OrderService orderService;
    
    @Inject
    private TestContext testContext;
    
    @もし("注文を確定する")
    public void confirmOrder() {
        try {
            Order order = orderService.createOrder(
                testContext.getCartItems(),
                testContext.getCurrentCustomer()
            );
            testContext.setOrder(order);
        } catch (Exception e) {
            testContext.setException(e);
        }
    }
}
```

### 3.3 スコープ管理

テストコンテキストは`@ApplicationScoped`でシングルトンとして管理します。

```java
@ApplicationScoped
public class TestContext {
    
    private String currentCustomer;
    private Order order;
    private Exception exception;
    
    // Getters and Setters
    
    public void clear() {
        currentCustomer = null;
        order = null;
        exception = null;
    }
}
```

---

## 4. データベーステスト

### 4.1 トランザクション管理

各シナリオごとにトランザクションをロールバックします。

```java
public class Hooks {
    
    @Inject
    private EntityManager em;
    
    @Before
    public void beforeScenario() {
        em.getTransaction().begin();
        testContext.clear();
    }
    
    @After
    public void afterScenario() {
        if (em != null && em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}
```

### 4.2 テストデータのセットアップ

テストデータは`TestDatabase`クラスで管理します。

```java
@ApplicationScoped
public class TestDatabase {
    
    @Inject
    private EntityManager em;
    
    public void initialize() {
        // 必要に応じてテストデータをセットアップ
    }
    
    public void setupCustomer(String email, String name) {
        Customer customer = new Customer();
        customer.setEmail(email);
        customer.setCustomerName(name);
        em.persist(customer);
        em.flush();
    }
    
    public void setupBook(int bookId, String bookName, int price, int stock) {
        Book book = new Book();
        book.setBookId(bookId);
        book.setBookName(bookName);
        book.setPrice(price);
        em.persist(book);
        
        Stock stockEntity = new Stock();
        stockEntity.setBookId(bookId);
        stockEntity.setStockCount(stock);
        em.persist(stockEntity);
        
        em.flush();
    }
}
```

### 4.3 テスト用persistence.xml

テスト用のpersistence.xmlを`src/test/resources/META-INF/`に配置します。

```xml
<persistence-unit name="test-pu" transaction-type="RESOURCE_LOCAL">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
    <exclude-unlisted-classes>false</exclude-unlisted-classes>
    
    <properties>
        <!-- HSQLDB設定 -->
        <property name="jakarta.persistence.jdbc.driver" value="org.hsqldb.jdbcDriver"/>
        <property name="jakarta.persistence.jdbc.url" value="jdbc:hsqldb:mem:testdb"/>
        <property name="jakarta.persistence.jdbc.user" value="SA"/>
        <property name="jakarta.persistence.jdbc.password" value=""/>
        
        <!-- Hibernate設定 -->
        <property name="hibernate.dialect" value="org.hibernate.dialect.HSQLDialect"/>
        <property name="hibernate.hbm2ddl.auto" value="create-drop"/>
        <property name="hibernate.show_sql" value="false"/>
    </properties>
</persistence-unit>
```

---

## 5. テストの実行とレポート

### 5.1 テスト実行

```bash
# すべての結合テストを実行
./gradlew integrationTest

# 特定のタグを持つテストのみ実行
./gradlew integrationTest -Dcucumber.filter.tags="@order"

# 複数のタグを組み合わせる
./gradlew integrationTest -Dcucumber.filter.tags="@integration and @order"
```

### 5.2 HTMLレポート

Cucumberは自動的にHTMLレポートを生成します。

```java
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "pretty, html:build/reports/cucumber/cucumber.html, json:build/reports/cucumber/cucumber.json"
)
```

生成されるレポート:
- `build/reports/cucumber/cucumber.html` - HTMLレポート
- `build/reports/cucumber/cucumber.json` - JSONレポート

---

## 6. タグの使用

### 6.1 タグの定義

Featureファイルやシナリオにタグを付けて、実行対象を絞り込みます。

```gherkin
@integration
機能: 注文管理

  @order @positive
  シナリオ: 正常な注文処理
    前提 顧客"alice@example.com"でログインしている
    もし 注文を確定する
    ならば 注文が成功する

  @order @negative
  シナリオ: 在庫不足による注文失敗
    前提 書籍ID 1 の在庫が0冊である
    もし 注文を確定する
    ならば 注文が失敗する
```

### 6.2 タグフィルター

```bash
# @orderタグを持つテストのみ実行
./gradlew integrationTest -Dcucumber.filter.tags="@order"

# @orderかつ@positiveタグを持つテストのみ実行
./gradlew integrationTest -Dcucumber.filter.tags="@order and @positive"

# @orderまたは@customerタグを持つテストのみ実行
./gradlew integrationTest -Dcucumber.filter.tags="@order or @customer"

# @negativeタグを持たないテストのみ実行
./gradlew integrationTest -Dcucumber.filter.tags="not @negative"
```

---

## 7. よくある問題と解決策

### 7.1 CDI Beanが見つからない

**症状**: `UnsatisfiedResolutionException` エラー

**原因**: beans.xmlが不足またはCDI Beanが正しくスキャンされていない

**解決策**: 
```xml
<!-- src/test/resources/META-INF/beans.xml -->
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       bean-discovery-mode="all">
</beans>
```

### 7.2 トランザクションがロールバックされない

**症状**: テストデータがデータベースに残る

**原因**: トランザクション管理が不適切

**解決策**: 
```java
@After
public void afterScenario() {
    if (em != null && em.getTransaction().isActive()) {
        em.getTransaction().rollback();
    }
}
```

### 7.3 Featureファイルが見つからない

**症状**: `No features found` エラー

**原因**: Featureファイルのパスが間違っている

**解決策**: 
- `src/test/resources/features/` にFeatureファイルを配置
- `@SelectClasspathResource("features")` を確認

### 7.4 Step Definitionが見つからない

**症状**: `Undefined step` エラー

**原因**: Step Definitionのパターンが一致しない

**解決策**: 
- Gherkinステップのパターンを確認
- `@ConfigurationParameter(key = GLUE_PROPERTY_NAME, ...)` を確認

---

## 8. ベストプラクティスのまとめ

### 8.1 Gherkinシナリオ

- **ビジネス言語で記述**: 技術的な詳細は避ける
- **Given-When-Then**: 前提条件、アクション、期待結果を明確に
- **データテーブル**: 複数のデータを扱う場合は表形式で
- **シナリオアウトライン**: 同じシナリオを複数のデータパターンでテスト

### 8.2 Step Definitions

- **再利用可能なステップ**: 共通ステップは`CommonSteps`に集約
- **パラメータ化**: 正規表現でパラメータを受け取る
- **アサーション**: JUnit Assertionsを使用

### 8.3 CDI統合

- **Weld SE**: CDIコンテナを起動
- **依存性注入**: `@Inject`でサービス層・DAO層を注入
- **スコープ**: `@ApplicationScoped`でシングルトン

### 8.4 データベーステスト

- **トランザクション管理**: 各シナリオ後にロールバック
- **テストデータ**: Featureファイルで定義
- **独立性**: 各シナリオは独立して実行可能

---

## 9. 参考資料

- [Cucumber公式ドキュメント](https://cucumber.io/docs/cucumber/)
- [Gherkin Reference](https://cucumber.io/docs/gherkin/reference/)
- [Weld SE Documentation](https://docs.jboss.org/weld/reference/latest/en-US/html/environments.html)
- [Jakarta EE 10 Platform](https://jakarta.ee/specifications/platform/10/)

---

## まとめ

Cucumberを活用することで、以下のメリットが得られます：

1. **ビジネス要件の可視化**: Gherkin形式で誰でも理解できるシナリオ
2. **生きたドキュメント**: テストがそのままドキュメントになる
3. **チーム間のコミュニケーション**: ビジネス担当者とエンジニアの共通言語
4. **高品質な結合テスト**: CDI統合により実際のサービス層をテスト

プロジェクトのビジネス要件に合わせて、適切なシナリオを定義し、継続的に改善していくことが重要です。
