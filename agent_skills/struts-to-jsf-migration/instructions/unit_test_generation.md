# 単体テストコード生成インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
target_domain: "対象ドメイン名"
```

* 例: person_managementドメインの単体テスト生成
```yaml
project_root: "projects/sdd-wf/person/jsf-person-sdd"
spec_directory: "projects/sdd-wf/person/jsf-person-sdd/specs/baseline"
target_domain: "person_management"
```

注意
* パス区切りはOS環境に応じて調整する（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換える
* 以降、`{spec_directory}` と表記されている箇所は、上記で設定した値に置き換える

---

## 概要

このインストラクションは、本番コード（@agent_skills/struts-to-jsf-migration/instructions/code_generation.md で生成されたコード）に対する単体テストコードを生成するためのものである。

重要な原則:
* コンテキストの分離: 本番コード生成とは別タスクとして実行することで、コンテキストを明確に分ける
* ブラックボックステストとホワイトボックステストの両立: 外形的な振る舞いの正しさと内部のカバレッジを両方確保する
* JSF特有の考慮事項: Managed Bean、Service、Entityに焦点を当てる（XHTMLはE2Eテストで検証）

---

## 1. テストコンテキストをロードして分析する

### 読み込むべきドキュメント（優先順）

1. Agent Skillsルール（最優先で確認）
   * @agent_skills/struts-to-jsf-migration/principles/ - マイグレーションルール、テスト戦略を確認する

2. 必須: `{spec_directory}/basic_design/architecture_design.md` で以下を確認する
   * テスト戦略（テストフレームワーク、カバレッジ目標、テスト方針）
   * 技術スタック（言語、バージョン、フレームワーク、ライブラリ）
   * セッション管理方針（ViewScoped、Flash Scope、Session Scope）

3. 必須: `{spec_directory}/detailed_design/{target_domain}/detailed_design.md` で対象ドメインの詳細設計を確認する
   * 実装クラス設計（Managed Bean、Service、Entity）、メソッドシグネチャ、アノテーション等
   * ホワイトボックステストの基盤となる実装詳細

4. 必須: `{spec_directory}/detailed_design/{target_domain}/behaviors.md` で対象ドメインの振る舞い仕様を確認する
   * Gherkin記法で記述されたテストシナリオ
   * ブラックボックステストの基盤となる振る舞い仕様

5. 必須: `{spec_directory}/basic_design/functional_design.md` でシステム全体の機能設計を確認する
   * 画面一覧、画面遷移図

6. 存在する場合: `{spec_directory}/basic_design/data_model.md` でテーブル定義とERDを確認する

7. 存在する場合: `{spec_directory}/basic_design/screen_design.md` で画面レイアウト、入力項目を確認する

8. 既存の本番コード: `{project_root}/src/main/java` 配下の実装コードを確認する
   * Entity、Service、Managed Bean、DTO等の実装

---

## 2. 単体テスト生成の基本方針

### 2.1 テストの二つの観点

単体テストは、以下の二つの観点を統合して設計する：

#### ブラックボックステスト（外形的な振る舞いの検証）

* 目的: コンポーネントの外部から見た振る舞いの正しさを検証する
* 駆動元: `{spec_directory}/detailed_design/{target_domain}/behaviors.md` の Gherkin シナリオ
* 焦点: 
  * 入力と出力の関係
  * ビジネスルールの遵守
  * 画面遷移の正しさ
  * エラーハンドリングの正しさ
  * バリデーションの正しさ
* テスト設計:
  * Given（前提条件）: テストデータ、モックのスタブ設定、セッション・Flashスコープの初期化
  * When（操作）: アクションメソッド呼び出し、Serviceメソッド呼び出し
  * Then（期待結果）: 戻り値（画面ID）、Bean状態、FacesMessage、副作用の検証

#### ホワイトボックステスト（内部カバレッジの確保）

* 目的: コードの内部構造を理解し、すべてのパスとロジックが正しく動作することを検証する
* 駆動元: `{spec_directory}/detailed_design/{target_domain}/detailed_design.md` のメソッドシグネチャと実装詳細
* 焦点:
  * コードカバレッジ（行カバレッジ、分岐カバレッジ）
  * 境界値テスト
  * バリデーションエラーケース
  * エッジケース
  * 内部状態の変化
* テスト設計:
  * 正常系テスト（期待する戻り値が返されるか）
  * 異常系テスト（例外が適切にスローされるか）
  * 境界値テスト（null、空文字列、最大値、最小値等）
  * バリデーションエラーテスト

### 2.2 両観点の統合

* Gherkin シナリオをベースにテストケースを設計し、それに加えてコードカバレッジを確保するための追加テストケースを作成する
* 一つのテストケースが複数の観点（振る舞い + カバレッジ）をカバーすることもある
* ブラックボックステストでカバーされない内部パスは、ホワイトボックステストで補完する

---

## 3. テストスコープとモック戦略

### 3.1 テストスコープ

* テストスコープ: 実装対象ドメイン内
  * 実装対象のドメイン（例: person_management）に含まれるコンポーネントをテスト
  * ドメイン内のコンポーネント間は実際の連携でテスト可能
  * ドメイン外の依存関係はモックを使用

### 3.2 モック使用の判断基準

* 同じドメイン内のコンポーネント → モック不要（実際の連携をテスト）
  * 例: PersonBean → PersonService → EntityManager （同じドメイン内）
* ドメイン外の依存関係 → モックを使用
  * 例: PersonService が ExternalService に依存する場合、ExternalService はモック
  * 例: EntityManager、外部APIクライアント等はモック

### 3.3 Managed Beanのテスト戦略

* 基本方針: Managed Beanは基本的にテスト対象外（カバレッジ除外推奨、E2Eテストで検証）
* 例外: ビジネスロジックが Managed Bean に実装されている場合はテスト対象とする
* 重点: Service層のテストに注力する

---

## 4. テストケース設計

### 4.1 ブラックボックステストケースの設計

`{spec_directory}/detailed_design/{target_domain}/behaviors.md` の Gherkin シナリオを参考に、JUnit 5 の通常のテストクラスとテストメソッドを生成する。

Gherkin シナリオからテストメソッドへの変換

Gherkin記法:
```gherkin
Feature: 人物一覧表示
  Scenario: 全ての人物を一覧表示する
    Given データベースに複数の人物が登録されている
    When PersonBean.loadPersons()を呼び出す
    Then 全ての人物が取得される
    And Beanのpersonsプロパティに設定される
```

JUnit 5テストメソッド:
```java
@Test
@DisplayName("全ての人物を一覧表示する - Gherkinシナリオベース")
void testLoadPersons_Success_FromBehavior() {
    // Given: データベースに複数の人物が登録されている
    List<Person> testPersons = Arrays.asList(
        new Person("太郎", "山田", 30),
        new Person("花子", "鈴木", 25)
    );
    when(mockPersonService.findAll()).thenReturn(testPersons);
    
    // When: PersonBean.loadPersons()を呼び出す
    personBean.loadPersons();
    
    // Then: 全ての人物が取得される
    List<Person> result = personBean.getPersons();
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("太郎", result.get(0).getFirstName());
    assertEquals("花子", result.get(1).getFirstName());
    
    // Serviceが呼び出されたことを検証
    verify(mockPersonService).findAll();
}
```

### 4.2 ホワイトボックステストケースの設計

`{spec_directory}/detailed_design/{target_domain}/detailed_design.md` の各メソッドシグネチャに対して、以下のテストメソッドを作成する：

* 正常系テスト（期待する戻り値が返されるか）
* 異常系テスト（例外が適切にスローされるか）
* 境界値テスト（null、空文字列、最大値、最小値等）
* バリデーションエラーテスト

例: 境界値テスト

```java
@Test
@DisplayName("人物登録 - 名前が空文字列の場合、バリデーションエラー（境界値）")
void testSavePerson_EmptyFirstName_ValidationError() {
    // Given: 名前が空文字列
    Person person = new Person("", "山田", 30);
    personBean.setPerson(person);
    
    when(mockValidator.validate(person)).thenReturn(Set.of(
        createConstraintViolation("firstName", "名前は必須です")
    ));
    
    // When: savePerson()を呼び出す
    String outcome = personBean.savePerson();
    
    // Then: エラーメッセージが設定される
    assertNull(outcome); // 画面遷移なし
    verify(mockFacesContext).addMessage(eq(null), any(FacesMessage.class));
}

@Test
@DisplayName("人物登録 - 年齢がnullの場合、バリデーションエラー（境界値）")
void testSavePerson_NullAge_ValidationError() {
    // Given: 年齢がnull
    Person person = new Person("太郎", "山田", null);
    personBean.setPerson(person);
    
    // When & Then: バリデーションエラーが発生
    when(mockValidator.validate(person)).thenReturn(Set.of(
        createConstraintViolation("age", "年齢は必須です")
    ));
    
    String outcome = personBean.savePerson();
    assertNull(outcome);
}

@Test
@DisplayName("人物登録 - 年齢が最大値の場合（境界値）")
void testSavePerson_MaxAge() {
    // Given: 年齢が最大値
    Person person = new Person("太郎", "山田", Integer.MAX_VALUE);
    personBean.setPerson(person);
    
    when(mockPersonService.save(person)).thenReturn(person);
    
    // When: 人物を登録
    String outcome = personBean.savePerson();
    
    // Then: 正常に登録される
    assertEquals("person-list", outcome);
    verify(mockPersonService).save(person);
}
```

### 4.3 カバレッジ目標

* `{spec_directory}/basic_design/architecture_design.md` で指定されたカバレッジ目標を遵守する
* 一般的な目標値: 行カバレッジ 80%以上、分岐カバレッジ 70%以上
* Gherkin シナリオでカバーされないパスは、ホワイトボックステストで補完する

---

## 5. テストクラスの構造

### 5.1 Serviceクラスのテスト構造

```java
@ExtendWith(MockitoExtension.class)
class PersonServiceTest {
    
    @InjectMocks
    private PersonService personService;
    
    @Mock
    private EntityManager entityManager;
    
    // テストデータ
    private Person testPerson1;
    private Person testPerson2;
    
    @BeforeEach
    void setUp() {
        // テストデータの初期化
        testPerson1 = new Person("太郎", "山田", 30);
        testPerson2 = new Person("花子", "鈴木", 25);
    }
    
    // ========================================
    // ブラックボックステスト（Gherkinシナリオベース）
    // ========================================
    
    @Nested
    @DisplayName("人物検索の振る舞い（Gherkinシナリオ）")
    class FindPersonBehaviorTests {
        
        @Test
        @DisplayName("Scenario: 全ての人物を検索する")
        void testFindAll_Success_FromBehavior() {
            // Gherkin シナリオベースのテスト
        }
        
        @Test
        @DisplayName("Scenario: 存在しないIDで検索に失敗する")
        void testFindById_NotFound_FromBehavior() {
            // Gherkin シナリオベースのテスト
        }
    }
    
    // ========================================
    // ホワイトボックステスト（カバレッジ確保）
    // ========================================
    
    @Nested
    @DisplayName("人物登録の境界値・エッジケース")
    class SavePersonEdgeCaseTests {
        
        @Test
        @DisplayName("境界値: 名前が空文字列")
        void testSave_EmptyFirstName() {
            // 境界値テスト
        }
        
        @Test
        @DisplayName("境界値: 年齢がnull")
        void testSave_NullAge() {
            // 境界値テスト
        }
        
        @Test
        @DisplayName("エッジケース: 年齢が最大値")
        void testSave_MaxAge() {
            // エッジケーステスト
        }
    }
}
```

### 5.2 Managed Beanのテスト構造（該当する場合）

```java
@ExtendWith(MockitoExtension.class)
class PersonBeanTest {
    
    @InjectMocks
    private PersonBean personBean;
    
    @Mock
    private PersonService personService;
    
    @Mock
    private FacesContext facesContext;
    
    @Mock
    private Flash flash;
    
    private Person testPerson;
    
    @BeforeEach
    void setUp() {
        // FacesContextのモック設定
        when(facesContext.getExternalContext()).thenReturn(mock(ExternalContext.class));
        when(facesContext.getExternalContext().getFlash()).thenReturn(flash);
        
        // テストデータの初期化
        testPerson = new Person("太郎", "山田", 30);
    }
    
    // ========================================
    // ブラックボックステスト（Gherkinシナリオベース）
    // ========================================
    
    @Nested
    @DisplayName("画面遷移の振る舞い（Gherkinシナリオ）")
    class NavigationBehaviorTests {
        
        @Test
        @DisplayName("Scenario: 人物登録後、一覧画面に遷移する")
        void testSavePerson_Success_NavigateToList() {
            // Given
            personBean.setPerson(testPerson);
            when(personService.save(testPerson)).thenReturn(testPerson);
            
            // When
            String outcome = personBean.savePerson();
            
            // Then
            assertEquals("person-list", outcome);
            verify(flash).put("message", "登録しました");
        }
    }
    
    // ========================================
    // ホワイトボックステスト（カバレッジ確保）
    // ========================================
    
    @Nested
    @DisplayName("バリデーションエラーの処理")
    class ValidationErrorTests {
        
        @Test
        @DisplayName("境界値: 名前が空の場合、エラーメッセージを表示")
        void testSavePerson_EmptyName_ShowsError() {
            // 境界値テスト
        }
    }
}
```

### 5.3 @Nested を使用した構造化

* ブラックボックステストとホワイトボックステストを `@Nested` で明確に分ける
* Gherkin シナリオに基づくテストは「振る舞いテスト」として、境界値・エッジケースは「カバレッジ確保テスト」として分類する

---

## 6. テストデータの準備

### 6.1 テストデータのソース

* `{spec_directory}/detailed_design/{target_domain}/behaviors.md` の具体例
* `{spec_directory}/basic_design/functional_design.md` のビジネスルール
* `{spec_directory}/basic_design/screen_design.md` の入力項目例
* `{spec_directory}/basic_design/data_model.md` のテーブル定義

### 6.2 テストデータの管理

* テストデータは各テストケース内でセットアップする（テストの独立性を保つ）
* @BeforeEach でテストデータの初期化を行う
* 共通のテストデータは、テストクラスのフィールドとして定義する

---

## 7. テストフレームワークとツール

### 7.1 必須フレームワーク

* テストフレームワーク: JUnit 5 のみ（Cucumberは使用しない）
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

1. テスト対象のコンポーネントごとにテストクラスを作成（Service優先、必要に応じてManaged Bean）
2. `@ExtendWith(MockitoExtension.class)` を付与
3. テスト対象のオブジェクトと依存関係をフィールドとして定義

### ステップ3: ブラックボックステストの生成

1. behaviors.md の Gherkin シナリオを確認
2. 各シナリオに対応する JUnit 5 テストメソッドを作成
3. Given-When-Then の流れでテストロジックを記述
4. `@Nested` で「振る舞いテスト」としてグループ化

### ステップ4: ホワイトボックステストの生成

1. detailed_design.md の各メソッドシグネチャを確認
2. 正常系、異常系、境界値、バリデーションエラーのテストメソッドを作成
3. カバレッジ目標を達成するための追加テストケースを作成
4. `@Nested` で「カバレッジ確保テスト」としてグループ化

### ステップ5: テストデータの準備

1. @BeforeEach でテストデータの初期化を行う
2. behaviors.md や screen_design.md から具体例を参照

### ステップ6: 検証

1. 全テストが実行可能であることを確認
2. カバレッジ目標を達成していることを確認
3. behaviors.md のシナリオがすべてカバーされていることを確認

---

## 9. コンポーネント別のテスト設計

### 9.1 Entityのテスト

* ブラックボックス: エンティティの振る舞い（バリデーション、リレーションシップ）
* ホワイトボックス: getter/setter、equals/hashCode、制約違反

### 9.2 Serviceのテスト（最重要）

* ブラックボックス: ビジネスロジックの正しさ、トランザクション境界
* ホワイトボックス: 例外ハンドリング、分岐パス、エッジケース
* DBUnitの活用（推奨）: Service層がデータアクセス処理を含む場合、DBUnitを使用したデータ駆動テストを実装することを推奨
  * テストデータをXML/CSV形式で外部管理
  * データベースの初期状態を明示的に定義
  * 期待するデータベース状態との比較検証

DBUnitを使用したServiceテストの例:
```java
@ExtendWith(MockitoExtension.class)
class PersonServiceTest {
    
    @InjectMocks
    private PersonService personService;
    
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
    @DisplayName("全ての人物を検索 - 複数件存在する場合")
    void testFindAll_MultiplePersons() throws Exception {
        // Given: DBUnitでテストデータを投入
        IDataSet dataSet = new FlatXmlDataSetBuilder()
            .setColumnSensing(true)
            .build(getClass().getResourceAsStream("/datasets/service/persons-findall.xml"));
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
        
        // モックの設定（実際のクエリ結果を返す）
        TypedQuery<Person> mockQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Person.class)))
            .thenReturn(mockQuery);
        
        List<Person> expectedPersons = Arrays.asList(
            new Person("太郎", "山田", 30),
            new Person("花子", "鈴木", 25),
            new Person("次郎", "佐藤", 35)
        );
        when(mockQuery.getResultList()).thenReturn(expectedPersons);
        
        // When: 全検索
        List<Person> result = personService.findAll();
        
        // Then: 3件の人物が取得される
        assertEquals(3, result.size());
        assertEquals("太郎", result.get(0).getFirstName());
        assertEquals("花子", result.get(1).getFirstName());
        assertEquals("次郎", result.get(2).getFirstName());
        
        // クエリが正しく実行されたことを検証
        verify(entityManager).createQuery(
            contains("SELECT p FROM Person p"), 
            eq(Person.class)
        );
    }
    
    @Test
    @DisplayName("年齢範囲で人物を検索")
    void testFindByAgeRange() throws Exception {
        // Given: DBUnitで様々な年齢の人物データを投入
        IDataSet dataSet = new FlatXmlDataSetBuilder()
            .setColumnSensing(true)
            .build(getClass().getResourceAsStream("/datasets/service/persons-age-range.xml"));
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
        
        // モックの設定
        TypedQuery<Person> mockQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Person.class)))
            .thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        
        // 年齢範囲内の人物のみを返す
        List<Person> expectedPersons = Arrays.asList(
            new Person("太郎", "山田", 30),
            new Person("花子", "鈴木", 25)
        );
        when(mockQuery.getResultList()).thenReturn(expectedPersons);
        
        // When: 20歳〜30歳で検索
        List<Person> result = personService.findByAgeRange(20, 30);
        
        // Then: 範囲内の人物のみ取得される
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(person -> 
            person.getAge() >= 20 && person.getAge() <= 30
        ));
        
        // パラメータが正しく設定されたことを検証
        verify(mockQuery).setParameter("minAge", 20);
        verify(mockQuery).setParameter("maxAge", 30);
    }
}
```

テストデータセット例（/datasets/service/persons-findall.xml）:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<dataset>
  <PERSON PERSON_ID="1" FIRST_NAME="太郎" LAST_NAME="山田" AGE="30" />
  <PERSON PERSON_ID="2" FIRST_NAME="花子" LAST_NAME="鈴木" AGE="25" />
  <PERSON PERSON_ID="3" FIRST_NAME="次郎" LAST_NAME="佐藤" AGE="35" />
</dataset>
```

テストデータセット例（/datasets/service/persons-age-range.xml）:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<dataset>
  <PERSON PERSON_ID="1" FIRST_NAME="太郎" LAST_NAME="山田" AGE="30" />
  <PERSON PERSON_ID="2" FIRST_NAME="花子" LAST_NAME="鈴木" AGE="25" />
  <PERSON PERSON_ID="3" FIRST_NAME="次郎" LAST_NAME="佐藤" AGE="35" />
  <PERSON PERSON_ID="4" FIRST_NAME="四郎" LAST_NAME="高橋" AGE="18" />
  <PERSON PERSON_ID="5" FIRST_NAME="五郎" LAST_NAME="伊藤" AGE="45" />
</dataset>
```

### 9.3 Managed Beanのテスト（該当する場合のみ）

* ブラックボックス: 画面遷移の正しさ、FacesMessageの生成、Flash Scopeの使用
* ホワイトボックス: バリデーションエラー、アクションメソッドの分岐

### 9.4 Facelets XHTMLのテスト

* 基本方針: 単体テストの対象外（E2Eテストで検証）

---

## 10. JSF特有のテスト考慮事項

### 10.1 FacesContextのモック

```java
@Mock
private FacesContext facesContext;

@Mock
private ExternalContext externalContext;

@Mock
private Flash flash;

@BeforeEach
void setUp() {
    when(facesContext.getExternalContext()).thenReturn(externalContext);
    when(externalContext.getFlash()).thenReturn(flash);
}
```

### 10.2 Flash Scopeのテスト

```java
@Test
void testSavePerson_Success_FlashMessage() {
    // Given
    personBean.setPerson(testPerson);
    when(personService.save(testPerson)).thenReturn(testPerson);
    
    // When
    String outcome = personBean.savePerson();
    
    // Then
    verify(flash).put("message", "登録しました");
}
```

### 10.3 FacesMessageのテスト

```java
@Test
void testSavePerson_Error_FacesMessage() {
    // Given
    personBean.setPerson(testPerson);
    when(personService.save(testPerson)).thenThrow(new RuntimeException("DB Error"));
    
    // When
    String outcome = personBean.savePerson();
    
    // Then
    assertNull(outcome); // 画面遷移なし
    verify(facesContext).addMessage(eq(null), argThat(msg -> 
        msg.getSeverity() == FacesMessage.SEVERITY_ERROR
    ));
}
```

---

## 11. 既存コードの扱い

* 既存のテストコードが存在する場合は、それらを削除せずに読み込んで、差分のみを反映する
* ファイルをゼロから作り直すのではなく、既存の内容を尊重して必要な部分のみを追加・修正する
* 新規テストケースの追加、既存テストケースの修正、不要なテストの削除など、必要な変更のみを適用する

---

## 12. 完了検証

* 本番コードに対応する単体テストが生成されていることを確認する（Service層優先）
* ブラックボックステスト（Gherkinシナリオベース）が実装されていることを確認する
* ホワイトボックステスト（境界値・バリデーションエラー）が実装されていることを確認する
* カバレッジ目標（architecture_design.md）を達成していることを確認する
* 全てのテストケースがコンパイル可能で、実行可能であることを確認する

---

## 13. 次のステップ

単体テストコード生成完了後は、以下を実施する：

1. 単体テスト実行: @agent_skills/struts-to-jsf-migration/instructions/unit_test_execution.md に従い単体テストを実行し、動作・カバレッジ・不足ケースを確認する
2. 不足しているテストケースを追加する
3. 必要に応じて詳細設計→コード生成→テスト生成→テスト実行のループを行う

---

## 参考資料

* [コード生成インストラクション](code_generation.md) - 本番コード生成
* [単体テスト実行インストラクション](unit_test_execution.md) - 単体テスト実行・評価
* [マイグレーション原則](../principles/) - アーキテクチャ標準、セキュリティ標準、マッピング規則
