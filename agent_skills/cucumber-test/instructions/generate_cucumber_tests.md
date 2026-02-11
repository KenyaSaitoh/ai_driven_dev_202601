# Cucumber BDD結合テスト生成指示書

バージョン: 1.0.0  
最終更新日: 2026-02-11

---

## 目的

Jakarta EEプロジェクトのビジネス要件を分析し、Cucumber BDD（振る舞い駆動開発）による結合テストコード（Java + Gherkin）を自動生成する。

---

## パラメータ

| パラメータ名 | 必須 | デフォルト値 | 説明 |
|------------|------|-------------|------|
| `project_path` | ✓ | - | プロジェクトのルートディレクトリパス |
| `package_root` | ✓ | - | ベースパッケージ名（例: pro.kensait.berrybooks） |
| `feature_file` | - | - | 既存のFeatureファイルのパス（省略時は自動生成） |
| `test_output_dir` | - | `{project_path}/src/test/java` | テストコードの出力ディレクトリ |
| `features_output_dir` | - | `{project_path}/src/test/resources/features` | Featureファイルの出力ディレクトリ |
| `generate_readme` | - | `true` | README_CUCUMBER.mdを生成するか |

---

## 実行手順

### ステップ1: プロジェクト構造の解析

1. **プロジェクトルートの確認**
   ```
   project_path が存在することを確認
   ```

2. **ビジネスロジックの解析**
   ```
   {project_path}/src/main/java/{package_rootのパス}/ 配下を解析
   ```

3. **主要機能の識別**
   
   以下のパッケージを探索し、ビジネス機能を特定:
   - `service` - ビジネスロジック層（テスト対象）
   - `dao` - データアクセス層
   - `entity` - エンティティ
   - `api` - REST APIエンドポイント

4. **既存Featureファイルの確認**
   
   `feature_file`パラメータが指定されている場合、既存のFeatureファイルを読み込む。
   指定されていない場合は、ビジネスロジックから自動生成する。

### ステップ2: Featureファイル（Gherkin）の生成または解析

#### 2-1. 既存Featureファイルの解析（feature_file指定時）

1. **Featureファイルを読み込む**
   ```
   feature_file のパスからGherkinファイルを読み込む
   ```

2. **シナリオを抽出**
   - 機能名（Feature）
   - 背景（Background）
   - シナリオ（Scenario）
   - シナリオアウトライン（Scenario Outline）
   - ステップ（Given, When, Then, And, But）

#### 2-2. Featureファイルの自動生成（feature_file未指定時）

1. **ビジネス機能の分析**
   
   サービス層のメソッドを分析し、主要なビジネス機能を特定:
   - 注文管理（OrderService）
   - 顧客管理（CustomerService）
   - 在庫管理（StockService）
   - 書籍検索（BookService）

2. **Gherkinシナリオの生成**
   
   各ビジネス機能に対してGherkinシナリオを生成:
   
   ```gherkin
   # language: ja
   @integration
   機能: {機能名}
   
     背景:
       前提 テストデータベースが初期化されている
   
     シナリオ: {正常系シナリオ}
       前提 {前提条件}
       もし {アクション}
       ならば {期待結果}
   
     シナリオ: {異常系シナリオ}
       前提 {前提条件}
       もし {アクション}
       ならば {エラー処理の確認}
   ```

3. **Featureファイルの命名**
   
   機能名に基づいてファイル名を決定:
   - 注文管理 → `order_management.feature`
   - 顧客管理 → `customer_management.feature`
   - 書籍検索 → `book_search.feature`

### ステップ3: 出力ディレクトリの準備

1. **テストディレクトリの作成**
   ```
   {test_output_dir}/{package_rootのパス}/cucumber/
   {test_output_dir}/{package_rootのパス}/cucumber/steps/
   {test_output_dir}/{package_rootのパス}/cucumber/support/
   ```

2. **Featureディレクトリの作成**
   ```
   {features_output_dir}/
   ```

### ステップ4: Step Definitionsクラスの生成

#### 4-1. Step Definitionsクラスの構造

**ファイルパス**: `{test_output_dir}/{package_rootのパス}/cucumber/steps/{Feature名}Steps.java`

**生成内容**:

```java
package {package_root}.cucumber.steps;

import io.cucumber.java.ja.*;
import io.cucumber.datatable.DataTable;
import jakarta.inject.Inject;
import {package_root}.service.*;
import {package_root}.entity.*;
import {package_root}.cucumber.support.TestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {機能名}のStep Definitionsクラス
 * 
 * Gherkinシナリオのステップを実装
 */
public class {Feature名}Steps {

    @Inject
    private TestContext testContext;
    
    @Inject
    private {Service名} {service名};

    /**
     * 前提条件のステップ
     */
    @前提("{Gherkinステップのパターン}")
    public void setupPrecondition(String param) {
        // 前提条件のセットアップ
    }

    /**
     * アクションのステップ
     */
    @もし("{Gherkinステップのパターン}")
    public void performAction(int param) {
        try {
            // ビジネスロジックの実行
            testContext.setResult({service名}.execute(param));
        } catch (Exception e) {
            testContext.setException(e);
        }
    }

    /**
     * 期待結果の検証ステップ
     */
    @ならば("{Gherkinステップのパターン}")
    public void verifyResult() {
        assertNotNull(testContext.getResult());
        // 期待結果の検証
    }
}
```

**注意点**:
- Gherkinステップのパターンは正規表現で定義
- パラメータは`{int}`, `{string}`, `{double}`等で受け取る
- DataTableは複数行のデータを扱う際に使用
- 例外は`TestContext`に保存して後で検証

#### 4-2. CommonStepsクラスの生成

共通ステップは`CommonSteps.java`に集約:

```java
package {package_root}.cucumber.steps;

import io.cucumber.java.ja.*;
import jakarta.inject.Inject;
import {package_root}.cucumber.support.TestContext;
import {package_root}.cucumber.support.TestDatabase;

public class CommonSteps {

    @Inject
    private TestContext testContext;
    
    @Inject
    private TestDatabase testDatabase;

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

### ステップ5: テストサポートクラスの生成

#### 5-1. TestContextクラス

**ファイルパス**: `{test_output_dir}/{package_rootのパス}/cucumber/support/TestContext.java`

**目的**: テストシナリオ間でデータを共有

```java
package {package_root}.cucumber.support;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.*;

/**
 * Cucumberテストのコンテキスト管理クラス
 * 
 * テストシナリオ間でデータを共有するための中央管理クラス
 */
@ApplicationScoped
public class TestContext {
    
    @Inject
    private EntityManager em;
    
    // テストデータ
    private String currentCustomer;
    private Object result;
    private Exception exception;
    private Map<String, Object> dataStore = new HashMap<>();
    
    // Getters and Setters
    
    public String getCurrentCustomer() {
        return currentCustomer;
    }
    
    public void setCurrentCustomer(String currentCustomer) {
        this.currentCustomer = currentCustomer;
    }
    
    public Object getResult() {
        return result;
    }
    
    public void setResult(Object result) {
        this.result = result;
    }
    
    public Exception getException() {
        return exception;
    }
    
    public void setException(Exception exception) {
        this.exception = exception;
    }
    
    public void put(String key, Object value) {
        dataStore.put(key, value);
    }
    
    public Object get(String key) {
        return dataStore.get(key);
    }
    
    /**
     * コンテキストをクリア
     */
    public void clear() {
        currentCustomer = null;
        result = null;
        exception = null;
        dataStore.clear();
    }
    
    /**
     * EntityManagerを取得
     */
    public EntityManager getEntityManager() {
        return em;
    }
}
```

#### 5-2. Hooksクラス

**ファイルパス**: `{test_output_dir}/{package_rootのパス}/cucumber/support/Hooks.java`

**目的**: テストライフサイクル管理（トランザクション管理）

```java
package {package_root}.cucumber.support;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.AfterAll;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;

/**
 * Cucumberテストのライフサイクル管理クラス
 * 
 * CDIコンテナの起動・シャットダウン、トランザクション管理を行う
 */
public class Hooks {
    
    private static SeContainer container;
    
    @Inject
    private EntityManager em;
    
    @Inject
    private TestContext testContext;
    
    /**
     * 全テスト実行前: CDIコンテナ起動
     */
    @BeforeAll
    public static void startCdiContainer() {
        container = SeContainerInitializer.newInstance().initialize();
    }
    
    /**
     * 各シナリオ実行前: トランザクション開始
     */
    @Before
    public void beforeScenario() {
        em.getTransaction().begin();
        testContext.clear();
    }
    
    /**
     * 各シナリオ実行後: トランザクションロールバック
     */
    @After
    public void afterScenario() {
        if (em != null && em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
    
    /**
     * 全テスト実行後: CDIコンテナシャットダウン
     */
    @AfterAll
    public static void stopCdiContainer() {
        if (container != null) {
            container.close();
        }
    }
}
```

#### 5-3. TestDatabaseクラス

**ファイルパス**: `{test_output_dir}/{package_rootのパス}/cucumber/support/TestDatabase.java`

**目的**: テストデータベースの初期化

```java
package {package_root}.cucumber.support;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

/**
 * テストデータベース管理クラス
 * 
 * テストデータのセットアップと初期化を行う
 */
@ApplicationScoped
public class TestDatabase {
    
    @Inject
    private EntityManager em;
    
    /**
     * データベースを初期化
     */
    public void initialize() {
        // 必要に応じてテストデータをセットアップ
    }
    
    /**
     * テストデータをセットアップ
     */
    public void setupTestData() {
        // テストデータの投入
    }
    
    /**
     * データベースをクリア
     */
    public void clear() {
        // データのクリア
    }
}
```

### ステップ6: テストランナークラスの生成

**ファイルパス**: `{test_output_dir}/{package_rootのパス}/cucumber/CucumberIntegrationTestRunner.java`

```java
package {package_root}.cucumber;

import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Cucumber結合テストランナー
 * 
 * JUnit 5のテストスイートとして実行される
 * Featureファイルを読み込み、Step Definitionsを実行する
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "pretty, html:build/reports/cucumber/cucumber.html, json:build/reports/cucumber/cucumber.json"
)
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME,
    value = "{package_root}.cucumber.steps"
)
@ConfigurationParameter(
    key = FILTER_TAGS_PROPERTY_NAME,
    value = "@integration"
)
public class CucumberIntegrationTestRunner {
}
```

### ステップ7: テスト用設定ファイルの生成

#### 7-1. beans.xml

**ファイルパス**: `{project_path}/src/test/resources/META-INF/beans.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                           https://jakarta.ee/xml/ns/jakartaee/beans_4_0.xsd"
       bean-discovery-mode="all"
       version="4.0">
</beans>
```

#### 7-2. persistence.xml（テスト用）

**ファイルパス**: `{project_path}/src/test/resources/META-INF/persistence.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
                                 https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"
             version="3.0">
    <persistence-unit name="test-pu" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
        
        <!-- エンティティクラスを自動スキャン -->
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
            <property name="hibernate.format_sql" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

### ステップ8: README_CUCUMBER.md の生成（generate_readme=trueの場合）

**ファイルパス**: `{project_path}/README_CUCUMBER.md`

**生成内容**:

```markdown
# Cucumber BDD結合テスト

## 概要

このプロジェクトでは、Cucumberを使用してビジネスシナリオを検証しています。

## Featureファイル

### {Feature名1}
{機能の説明}

### {Feature名2}
{機能の説明}

## テスト実行方法

### すべての結合テストを実行
```bash
./gradlew :{プロジェクト名}:integrationTest
```

### 特定のタグを持つテストのみ実行
```bash
./gradlew :{プロジェクト名}:integrationTest -Dcucumber.filter.tags="@order"
```

### HTMLレポートの確認
テスト実行後、以下にレポートが生成されます：
```
build/reports/cucumber/cucumber.html
```

## アーキテクチャ

### CDI統合
Weld SEを使用してCDI Beanを起動し、実際のサービス層・DAO層をテストします。

### トランザクション管理
各シナリオごとにトランザクションをロールバックし、テストの独立性を保証します。

## トラブルシューティング

### CDI Beanが見つからない
`src/test/resources/META-INF/beans.xml` を確認してください。

### トランザクションがロールバックされない
`Hooks.java` のトランザクション管理を確認してください。

## 参考資料

- [Cucumber公式ドキュメント](https://cucumber.io/docs/cucumber/)
- [Gherkin Reference](https://cucumber.io/docs/gherkin/reference/)
- [Cucumberベストプラクティス](../../agent_skills/cucumber-test/principles/cucumber_best_practices.md)
```

### ステップ9: 完了確認と報告

すべてのファイル生成が完了したら、以下を確認してユーザーに報告する:

1. **生成されたファイル一覧**
   - Featureファイルの数
   - Step Definitionsクラスの数
   - テストサポートクラス
   - テストランナー

2. **次のステップの案内**
   ```
   以下のコマンドでテストを実行できます:
   
   # 結合テスト（Cucumberテスト）を実行
   ./gradlew :{プロジェクト名}:integrationTest
   
   # HTMLレポートの確認
   # build/reports/cucumber/cucumber.html を開く
   ```

3. **注意事項**
   - Cucumber依存関係がbuild.gradleに追加されていることを確認
   - テストが失敗した場合は、ビジネスロジックまたはシナリオを確認
   - Featureファイルは日本語で記述可能（`# language: ja`を追加）

---

## 生成ルール

### 必須事項

1. **遵守するベストプラクティス**
   - `@agent_skills/cucumber-test/principles/cucumber_best_practices.md` を参照
   - Gherkinシナリオは自然言語で記述
   - Step Definitionsは再利用可能に
   - JUnit 5との統合
   - CDI統合（Weld SE）
   - トランザクション管理

2. **Java型定義**
   - 厳密な型定義を使用
   - Jakarta EE 10のアノテーションを使用
   - `@Inject`で依存性注入

3. **コメント**
   - 日本語でクラス・メソッドの説明を記述
   - Gherkinステップの意図を明確に記述
   - 各クラスにJavadocを追加

4. **コード品質**
   - 標準的なJavaコーディング規約に準拠
   - 変数名・メソッド名は英語（camelCase）
   - クラス名はPascalCase

### 任意事項

1. **タグの使用**
   - `@integration` - 結合テストタグ
   - `@order` - 注文管理関連
   - `@customer` - 顧客管理関連

2. **シナリオアウトライン**
   - 複数のデータパターンをテストする場合に使用

---

## エラーハンドリング

### プロジェクトルートが見つからない場合

```
エラー: プロジェクトルートが見つかりません
ディレクトリパス: {project_path}

以下を確認してください:
1. ディレクトリパスが正しいか
2. ディレクトリが存在するか
3. アクセス権限があるか
```

### パッケージが見つからない場合

```
エラー: ベースパッケージが見つかりません
パッケージ: {package_root}

以下を確認してください:
1. パッケージ名が正しいか
2. src/main/java配下にパッケージが存在するか
```

### Featureファイルが不正な場合

```
警告: Featureファイルのフォーマットが不正です

以下を確認してください:
1. Gherkin構文が正しいか
2. Given-When-Thenの構造が適切か
3. パラメータが正しく定義されているか
```

---

## 参考資料

* [Cucumberベストプラクティス](../principles/cucumber_best_practices.md)
* [Featureファイルテンプレート](../templates/cucumber_feature_template.md)
* [Cucumber公式ドキュメント](https://cucumber.io/docs/cucumber/)
* [Gherkin Reference](https://cucumber.io/docs/gherkin/reference/)
