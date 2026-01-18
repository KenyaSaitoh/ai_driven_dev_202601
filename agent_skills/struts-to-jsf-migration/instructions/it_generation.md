# 結合テスト生成インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
```

* 例
```yaml
project_root: "projects/sdd/person/jsf-person-sdd"
spec_directory: "projects/sdd/person/jsf-person-sdd/specs/baseline"
```

注意
* パス区切りはOS環境に応じて調整する（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換える
* 以降、`{spec_directory}` と表記されている箇所は、上記で設定した値に置き換える

---

## 概要

このインストラクションは、JSF Webアプリケーションの結合テスト（Integration Test）を生成するためのものである

重要な方針
* 単体テスト実行評価後に結合テストを生成する（unit_test_execution.mdの次のステップ）
* テストフレームワーク: JUnit 5 + Weld SE（CDIコンテナ）
* テスト対象: basic_design/behaviors.md（結合テスト用）のシナリオ
* Service層以下（Service + Entity）の実際の連携をテスト
* モックは使用しない（実際のDB操作）
* アプリケーションサーバーは不要（Weld SEでCDIコンテナを起動）
* Managed Beanは結合テストの対象外（E2Eテストで検証）

---

## 1. SPECの読み込みと理解

パラメータで指定されたプロジェクト情報に基づいて、以下の設計ドキュメントを読み込んで分析する

### 1.1 Agent Skillsルール（最優先で確認）

* @agent_skills/struts-to-jsf-migration/principles/ - マイグレーションルール、アーキテクチャ標準、品質基準、セキュリティ標準を確認する
* @agent_skills/jakarta-ee-api-base/principles/ - Jakarta EE開発の原則を確認する
  * これらのフォルダ配下の原則ドキュメントを読み込み、共通ルールを遵守すること
  * 重要: 結合テスト生成においても、ルールドキュメントに記載されたすべてのルールを遵守すること
  * 注意: Agent Skills配下のルールは全プロジェクト共通。プロジェクト固有のルールがある場合は `{project_root}/principles/` も確認すること

### 1.2 基本設計の仕様

以下のファイルを読み込み、システム全体の設計を理解する

* {spec_directory}/basic_design/architecture_design.md - 技術スタック、パッケージ構造、テスト設定を確認する
  * 使用技術スタック
  * データソース設定（JNDI名）
  * セッション管理方針

* {spec_directory}/basic_design/functional_design.md - システム全体の機能設計を確認する
  * 全ての機能仕様
  * ビジネスロジック
  * データフロー
  * 画面遷移

* {spec_directory}/basic_design/data_model.md - データモデルを確認する
  * エンティティ定義
  * リレーション
  * 制約

* {spec_directory}/basic_design/behaviors.md - 結合テストシナリオを確認する
  * Service層の振る舞い
  * ビジネスロジックの検証シナリオ
  * データアクセスの検証シナリオ
  * トランザクション処理の検証
  * 例: PersonService → EntityManager → DB

---

## 2. Weld SE のセットアップ

### 2.1 依存関係の追加

Gradle の場合、`build.gradle` に以下を追加:

```gradle
dependencies {
    // Weld SE (CDI Container)
    testImplementation 'org.jboss.weld.se:weld-se-core:5.1.0.Final'
    
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
        <!-- 例: <class>pro.kensait.jsf.person.entity.Person</class> -->
        
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
* Managed Beanは対象外（E2Eテストで検証）
* トランザクション管理の検証

### 3.2 テストベースクラス

全結合テストで共通のベースクラスを作成:

```java
package pro.kensait.jsf.person.integration;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;

@Tag("integration")
public abstract class BaseIntegrationTest {
    
    protected static SeContainer container;
    
    protected EntityManager em;
    
    @BeforeAll
    static void setupContainer() {
        // Weld SE（CDIコンテナ）を起動
        container = SeContainerInitializer.newInstance().initialize();
    }
    
    @AfterAll
    static void teardownContainer() {
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
    }
}
```

### 3.3 テストケーステンプレート

シナリオ例: Person作成・検索（Service + EntityManager + DB）

```java
package pro.kensait.jsf.person.integration;

import org.junit.jupiter.api.*;
import pro.kensait.jsf.person.service.PersonService;
import pro.kensait.jsf.person.entity.Person;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PersonServiceIntegrationTest extends BaseIntegrationTest {
    
    private PersonService personService;
    
    @BeforeEach
    void setupService() {
        // CDI経由でServiceを取得（モックなし）
        personService = container.select(PersonService.class).get();
    }
    
    @Test
    @Order(1)
    @DisplayName("Person作成: 正常系")
    void test_createPerson_Success() {
        // Arrange: テストデータ準備
        Person person = new Person();
        person.setPersonName("山田太郎");
        person.setAge(30);
        person.setGender("male");
        
        // Act: Serviceメソッドを直接呼び出し
        personService.createPerson(person);
        
        // Assert: 実際のDBに保存されたか検証
        em.flush(); // 強制的にDBに反映
        em.clear(); // 1次キャッシュをクリア
        
        Person saved = em.find(Person.class, person.getPersonId());
        assertNotNull(saved, "PersonがDBに保存されていること");
        assertEquals("山田太郎", saved.getPersonName(), "名前が正しいこと");
        assertEquals(30, saved.getAge(), "年齢が正しいこと");
        assertEquals("male", saved.getGender(), "性別が正しいこと");
    }
    
    @Test
    @Order(2)
    @DisplayName("Person検索: 全件取得")
    void test_findAllPersons() {
        // Arrange: テストデータ準備
        Person person1 = new Person();
        person1.setPersonName("山田太郎");
        person1.setAge(30);
        person1.setGender("male");
        em.persist(person1);
        
        Person person2 = new Person();
        person2.setPersonName("佐藤花子");
        person2.setAge(25);
        person2.setGender("female");
        em.persist(person2);
        
        em.flush();
        em.clear();
        
        // Act: 全件検索
        List<Person> results = personService.findAllPersons();
        
        // Assert
        assertEquals(2, results.size(), "2件取得できること");
        assertTrue(results.stream().anyMatch(p -> "山田太郎".equals(p.getPersonName())));
        assertTrue(results.stream().anyMatch(p -> "佐藤花子".equals(p.getPersonName())));
    }
    
    @Test
    @Order(3)
    @DisplayName("Person更新: 正常系")
    void test_updatePerson_Success() {
        // Arrange: 既存データ準備
        Person person = new Person();
        person.setPersonName("山田太郎");
        person.setAge(30);
        person.setGender("male");
        em.persist(person);
        em.flush();
        em.clear();
        
        // Act: 更新
        Person toUpdate = em.find(Person.class, person.getPersonId());
        toUpdate.setAge(31);
        personService.updatePerson(toUpdate);
        
        em.flush();
        em.clear();
        
        // Assert: 更新されたか検証
        Person updated = em.find(Person.class, person.getPersonId());
        assertEquals(31, updated.getAge(), "年齢が更新されていること");
    }
    
    @Test
    @Order(4)
    @DisplayName("Person削除: 正常系")
    void test_deletePerson_Success() {
        // Arrange: 既存データ準備
        Person person = new Person();
        person.setPersonName("山田太郎");
        person.setAge(30);
        person.setGender("male");
        em.persist(person);
        em.flush();
        Long personId = person.getPersonId();
        em.clear();
        
        // Act: 削除
        personService.deletePerson(personId);
        
        em.flush();
        em.clear();
        
        // Assert: 削除されたか検証
        Person deleted = em.find(Person.class, personId);
        assertNull(deleted, "Personが削除されていること");
    }
    
    @Test
    @Order(5)
    @DisplayName("トランザクション: ロールバック検証")
    void test_transaction_Rollback() {
        // Arrange: テストデータ準備
        Person person = new Person();
        person.setPersonName("山田太郎");
        person.setAge(30);
        person.setGender("male");
        
        // Act: 例外を発生させてトランザクションをロールバック
        assertThrows(Exception.class, () -> {
            personService.createPerson(person);
            // 意図的に例外をスロー
            throw new RuntimeException("Test exception");
        });
        
        // Assert: ロールバックされていることを検証
        // （@AfterEachでロールバックされるため、DBには保存されていない）
    }
}
```

### 3.4 Bean Validationの結合テスト

Bean Validationが実際に動作することを検証:

```java
package pro.kensait.jsf.person.integration;

import jakarta.validation.*;
import org.junit.jupiter.api.*;
import pro.kensait.jsf.person.entity.Person;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class PersonValidationIntegrationTest extends BaseIntegrationTest {
    
    private Validator validator;
    
    @BeforeEach
    void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    @DisplayName("Bean Validation: 名前が空の場合エラー")
    void test_validation_EmptyName() {
        // Arrange
        Person person = new Person();
        person.setPersonName(""); // 空文字
        person.setAge(30);
        person.setGender("male");
        
        // Act
        Set<ConstraintViolation<Person>> violations = validator.validate(person);
        
        // Assert
        assertFalse(violations.isEmpty(), "バリデーションエラーが発生すること");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("personName")));
    }
    
    @Test
    @DisplayName("Bean Validation: 年齢が範囲外の場合エラー")
    void test_validation_InvalidAge() {
        // Arrange
        Person person = new Person();
        person.setPersonName("山田太郎");
        person.setAge(-1); // 不正な値
        person.setGender("male");
        
        // Act
        Set<ConstraintViolation<Person>> violations = validator.validate(person);
        
        // Assert
        assertFalse(violations.isEmpty(), "バリデーションエラーが発生すること");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("age")));
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
    
    Person person1 = new Person();
    person1.setPersonName("山田太郎");
    person1.setAge(30);
    person1.setGender("male");
    em.persist(person1);
    
    Person person2 = new Person();
    person2.setPersonName("佐藤花子");
    person2.setAge(25);
    person2.setGender("female");
    em.persist(person2);
    
    em.flush(); // 強制的にDBに反映
}
```

### 4.2 テストデータ管理のベストプラクティス

* テスト間の独立性を保つ
* @BeforeEachでテストデータ準備
* @AfterEachでトランザクションロールバック（自動クリーンアップ）
* 各テストで一意のデータを使用（UUID等）

---

## 5. テストの実行

### 5.1 前提条件

結合テストを実行する前に:

1. HSQLDBサーバーを起動（メモリDBの場合は不要）
2. アプリケーションサーバーは**不要**（Weld SEで起動）

### 5.2 結合テストの実行

```bash
# 結合テストのみ実行
./gradlew integrationTest

# 通常のテスト（単体テスト）のみ実行（結合テストは除外）
./gradlew test

# 全てのテストを実行
./gradlew test integrationTest
```

---

## 6. テストレポート

### 6.1 テスト結果の確認

* JUnit 5のレポート: `build/reports/tests/integrationTest/index.html`
* コンソール出力: SQLログ、CDIログ

### 6.2 ログの有効化

詳細なログを出力:

```java
// persistence.xmlで設定
<property name="hibernate.show_sql" value="true"/>
<property name="hibernate.format_sql" value="true"/>
<property name="hibernate.use_sql_comments" value="true"/>
```

---

## 7. basic_design/behaviors.md からのテストケース生成

### 7.1 シナリオの読み取り

basic_design/behaviors.md の各シナリオを読み取り、以下の情報を抽出:

* Given: 初期状態、テストデータの準備
* When: 実行するServiceメソッド、パラメータ
* Then: 期待される結果（DBの状態、戻り値、例外）

### 7.2 シナリオ例とテストコードの対応

behaviors.mdの例:
```
Given: DBにPersonが存在しない
When: PersonService.createPerson()で新規Personを作成
Then:
  * Personがテーブルに挿入される
  * personIdが自動採番される
  * 作成されたPersonが検索可能になる
```

生成されるテストコード:
```java
@Test
@DisplayName("Person作成: 新規Person作成")
void test_createPerson() {
    // Given: 初期状態（DBは空）
    
    // When: 新規Person作成
    Person person = new Person();
    person.setPersonName("山田太郎");
    person.setAge(30);
    person.setGender("male");
    
    personService.createPerson(person);
    
    // Then: DBに挿入されたことを検証
    assertNotNull(person.getPersonId(), "personIdが自動採番されること");
    
    em.flush();
    em.clear();
    
    Person saved = em.find(Person.class, person.getPersonId());
    assertNotNull(saved, "PersonがDBに保存されていること");
    assertEquals("山田太郎", saved.getPersonName(), "名前が正しいこと");
    
    // Then: 検索可能になることを検証
    List<Person> all = personService.findAllPersons();
    assertTrue(all.stream().anyMatch(p -> p.getPersonId().equals(person.getPersonId())));
}
```

---

## 8. CI/CD パイプラインとの統合

### 8.1 GitHub Actions の例

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

## 9. 注意事項

### 9.1 テスト実行環境

* 結合テストは実際のDB（メモリDB）を使用する
* アプリケーションサーバーは不要（Weld SEで起動）
* テスト後はトランザクションロールバックで自動クリーンアップ

### 9.2 テストの安定性

* テスト間の独立性を保つ（@BeforeEach/@AfterEachで初期化・クリーンアップ）
* テストデータは一意にする（UUID等）
* トランザクション境界を明確にする

### 9.3 パフォーマンス

* 結合テストは単体テストより遅いが、E2Eテストより速い
* 重要なビジネスロジックのみを結合テストでカバー
* 詳細なテストは単体テストで実施
* 並列実行を検討（独立したテストケース）

### 9.4 Managed Beanは対象外

* Managed Beanは結合テストの対象外（UI層）
* Managed Beanのテストは以下で実施:
  * 単体テスト: Mockitoでモック化
  * E2Eテスト: Playwrightで実際の画面操作

### 9.5 単体テスト vs 結合テスト vs E2Eテスト

| テスト種別 | 対象 | モック | 実行環境 | 速度 | 目的 |
|-----------|------|--------|---------|------|------|
| 単体テスト | 個別クラス | あり（タスク外依存） | JUnit | 速い | クラスのロジック検証 |
| 結合テスト | Service + Entity + DB | なし | JUnit + Weld SE | 中速 | ビジネスロジック + データアクセス検証 |
| E2Eテスト | 全体（Managed Bean + 画面） | なし | Playwright + APサーバー | 遅い | ユーザー視点の全体フロー検証 |

---

## 10. 参考資料

* Weld SE公式ドキュメント: https://weld.cdi-spec.org/
* JUnit 5公式ドキュメント: https://junit.org/junit5/
* basic_design/behaviors.md - 結合テストシナリオ
* basic_design/functional_design.md - 機能仕様
* basic_design/architecture_design.md - システム構成
