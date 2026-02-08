# 結合テスト生成インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
build_script_path: null     # オプション。build.gradleファイルのパス（未指定時は project_root の build.gradle を使用）
                            # マルチプロジェクト構成の場合、ルートの build.gradle または サブプロジェクトの build.gradle を指定
                            # 例: "./build.gradle" (リポジトリルート) または "d:/GitHubRepos/.../build.gradle" (絶対パス)
```

* 例
```yaml
project_root: "projects/sdd-wf/person/jsf-person"
spec_directory: "projects/sdd-wf/person/jsf-person/specs/baseline"
build_script_path: "./build.gradle"  # マルチプロジェクト構成用
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
* **テストフレームワーク（2種類を並行使用）:**
  * **主: JUnit 5 + Weld SE（CDIコンテナ）** - 従来型の結合テスト（必須）
  * **補助・実験的: JUnit 5 + Cucumber + Weld SE** - Gherkin記法によるBDD形式テスト（オプション）
* テスト対象: basic_design/behaviors.md（結合テスト用）のシナリオ（Gherkin 記法で記述されている前提。@agent_skills/struts-to-jsf-migration/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照）
* Service層以下（Service + Entity）の実際の連携をテスト
* モックは使用しない（実際のDB操作）
* アプリケーションサーバーは不要（Weld SEでCDIコンテナを起動）
* Managed Beanは結合テストの対象外（E2Eテストで検証）
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

### 2.1 依存関係

結合テスト生成に必要なライブラリ:

* Weld SE (CDI): `org.jboss.weld.se:weld-se-core:5.1.0.Final`
* WireMock (外部APIスタブ): `com.github.tomakehurst:wiremock-jre8:2.35.0`（必要な場合）
* Hibernate (JPA実装): `org.hibernate.orm:hibernate-core:6.4.0.Final`
* JUnit 5: `org.junit.jupiter:junit-jupiter:5.10.0`
* JUnit Platform: `org.junit.platform:junit-platform-launcher:1.10.0`
* JUnit Platform Suite: `org.junit.platform:junit-platform-suite:1.10.0`
* HSQLDB: `org.hsqldb:hsqldb:2.7.2`

* 結合テストクラスには `@Tag("integration")` を付与し、通常の単体テスト実行から分離する

**依存関係の追加方法:**
* まず、対象プロジェクトの `build.gradle` を確認する
* プロジェクト内に `build.gradle` が存在しない、または依存関係が定義されていない場合:
  * 親ディレクトリやプロジェクトルートの `build.gradle` を探索する
  * 共通のビルドファイルで `subprojects` ブロックや全プロジェクト共通設定が定義されている可能性がある
  * 見つかった場合、そちらに依存関係を追加する
* `integrationTest` タスクについても同様に、既存の定義を確認してから追加の要否を判断する

**マルチプロジェクト構成の考慮:**
* Gradleのマルチプロジェクト構成の場合、build.gradleの場所はサブプロジェクトごとに異なる
* build_script_path パラメータでルートの build.gradle のパスを指定すること（例: "./build.gradle"）
* 指定されたパスからディレクトリ部分を抽出してそのディレクトリで `./gradlew` コマンドを実行する
* ルートプロジェクトの build.gradle でサブプロジェクトのタスクを実行する場合は `:subproject:integrationTest` のような形式を使用

### 2.2 Weld SE の設定

* `src/test/resources/META-INF/beans.xml`: Jakarta EE Beans 4.0、`bean-discovery-mode="all"`

### 2.3 テスト用 persistence.xml

* `src/test/resources/META-INF/persistence.xml`: persistence-unit 名は `test-pu`、transaction-type は RESOURCE_LOCAL
* テスト対象のエンティティを `<class>` で列挙
* HSQLDB メモリ（jdbc:hsqldb:mem:testdb）、Hibernate で `hbm2ddl.auto=create-drop`、dialect=HSQLDialect

---

## 3. 結合テストケース生成

### 3.1 テストケース設計方針（共通）

* basic_design/behaviors.md のシナリオに基づいてテストを生成
* Service層のビジネスロジックを中心にテスト
* 実際のDB（メモリDB）を使用
* Managed Beanは対象外（E2Eテストで検証）
* トランザクション管理の検証
* @Tag("integration") を付与し、integrationTest タスクで実行されるようにする

### 3.2 主テスト: JUnit 5 + Weld SE（従来型、必須）

* `src/test/java` 配下に通常のJUnitテストクラスを作成
* BaseIntegrationTest を継承（Weld SE によるCDIコンテナ起動、EntityManager管理）
* @Tag("integration") を付与
* テストメソッドは @Test アノテーションで実装
* behaviors.md のシナリオを参考に、Given-When-Then の流れでテストを記述

**例:**
```java
@Tag("integration")
class PersonServiceIntegrationTest extends BaseIntegrationTest {
    @Test
    void testCreatePerson_Success() {
        // Given: テストデータ投入
        Person person = new Person("太郎", "山田", 30);
        
        // When: Service メソッド呼び出し
        personService.create(person);
        em.flush();
        em.clear();
        
        // Then: DB検証
        Person saved = em.find(Person.class, person.getId());
        assertNotNull(saved);
        assertEquals("太郎", saved.getFirstName());
    }
}
```

### 3.3 補助テスト: JUnit 5 + Cucumber + Weld SE（BDD形式、実験的・オプション）

* basic_design/behaviors.md の Gherkin シナリオを、**Cucumber .feature ファイル**（`src/test/resources/features/integration` 配下）と **Cucumber ステップ定義**（Java、Weld SE を利用）に変換する
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

### 3.2 テストベースクラス

全結合テストで共通の abstract ベースクラスを用意する。ポイント:

* `@Tag("integration")` を付与
* @BeforeAll: 
  * Weld SE 起動（`new Weld().enableDiscovery().addPackages(true, BaseIntegrationTest.class.getPackage()).initialize()`）
  * EntityManagerFactory 作成（`Persistence.createEntityManagerFactory("test-pu")`）
* @AfterAll: EntityManagerFactory.close()、container.close()
* @BeforeEach: EntityManager 取得、`em.getTransaction().begin()`
* @AfterEach: トランザクションがアクティブなら rollback

**重要な注意点:**
* Weld SEは `enableDiscovery()` が必須（beans.xmlなしでCDI Beanを検出するため）
* EntityManagerProducerで `@Disposes` パラメータには `@PersistenceContext` を付与しない（コンパイルエラー回避）

### 3.3 テストケース（Service層）のポイント

* 1シナリオ＝1テストクラス、BaseIntegrationTest を継承
* @BeforeEach: container.select(Service.class).get() で Service 取得（モックなし）
* Arrange: エンティティを組み立てて em.persist、または既存データを em.find で取得
* Act: Service の create/update/delete/find メソッドを直接呼び出し
* Assert: em.flush() と em.clear() の後、em.find で永続化結果を検証（件数・フィールド値）
* トランザクションロールバック: 例外をスローするケースで assertThrows を使い、@AfterEach の rollback でDBに残らないことを前提に検証する

### 3.4 Bean Validation の結合テストのポイント

* Validation.buildDefaultValidatorFactory().getValidator() で Validator を取得
* エンティティに不正値（空文字・範囲外の数値等）をセットし、validator.validate(entity) で Set&lt;ConstraintViolation&gt; を取得
* violations が空でないこと、propertyPath が期待するフィールドであることを検証する

---

## 4. テストデータの準備

### 4.1 DBのセットアップ

* @BeforeEach で EntityManager を使い、em.persist でテストデータを投入し、em.flush() で反映する

### 4.2 テストデータ管理のベストプラクティス

@agent_skills/struts-to-jsf-migration/principles/architecture.md の「9.4 テストデータ管理」を参照する。

---

## 5. basic_design/behaviors.md からのテストケース生成

### 5.1 シナリオの読み取り

basic_design/behaviors.md は Gherkin 記法で記述されている。@agent_skills/struts-to-jsf-migration/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照の上、各シナリオから Given/When/Then を抽出する。

### 5.2 シナリオとテストの対応

* Given: 初期状態（DBが空、または em.persist で準備したデータ）で再現する
* When: Service のメソッドを直接呼び出す（引数は functional_design / data_model に合わせる）
* Then: 戻り値・自動採番されたIDの assert、em.flush()/em.clear() 後の em.find で永続化結果を検証、必要なら findAll で検索可能になることを検証する

---

## 6. 注意事項

### 6.1 テスト実行環境

* 結合テストは実際のDB（メモリDB）を使用する。アプリケーションサーバーは不要（Weld SEで起動）。テスト後はトランザクションロールバックで自動クリーンアップ。

### 6.2 テストの安定性

* テスト間の独立性を保つ（@BeforeEach/@AfterEachで初期化・クリーンアップ）。テストデータは一意にする（UUID等）。トランザクション境界を明確にする。

### 6.3 Managed Beanは対象外

* Managed Beanは結合テストの対象外（UI層）。単体テストでは Mockito、E2Eでは Playwright で検証する。

### 6.4 単体テスト vs 結合テスト vs E2Eテスト

| テスト種別 | 対象 | モック | 実行環境 | 目的 |
|-----------|------|--------|---------|------|
| 単体テスト | 個別クラス | あり（タスク外依存） | JUnit | クラスのロジック検証 |
| 結合テスト | Service + Entity + DB | なし | JUnit + Weld SE | ビジネスロジック + データアクセス検証 |
| E2Eテスト | 全体（Managed Bean + 画面） | なし | Playwright + APサーバー | ユーザー視点の全体フロー検証 |

---

## 7. 参考資料

* Weld SE公式ドキュメント: https://weld.cdi-spec.org/
* JUnit 5公式ドキュメント: https://junit.org/junit5/
* basic_design/behaviors.md - 結合テストシナリオ
* basic_design/functional_design.md - 機能仕様
* basic_design/architecture_design.md - システム構成
