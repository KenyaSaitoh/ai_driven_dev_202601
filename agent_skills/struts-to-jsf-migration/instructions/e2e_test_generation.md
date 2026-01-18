# E2Eテスト生成インストラクション

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

このインストラクションは、JSF Webアプリケーションのend-to-end (E2E) テストを生成するためのものである

重要な方針
* 実装完了後にE2Eテストを生成する（code_generation.mdの次のステップ）
* テストフレームワーク: Playwright を使用
* テスト対象: basic_design/behaviors.md（E2Eテスト用）のシナリオ
* 複数画面にまたがるフロー、実際のブラウザ操作、実際のDBアクセスを含む
* アプリケーションサーバーが起動している状態でテストを実行

---

## 1. SPECの読み込みと理解

パラメータで指定されたプロジェクト情報に基づいて、以下の設計ドキュメントを読み込んで分析する

### 1.1 Agent Skillsルール（最優先で確認）

* @agent_skills/struts-to-jsf-migration/principles/ - マイグレーションルール、アーキテクチャ標準、品質基準、セキュリティ標準を確認する
  * このフォルダ配下の原則ドキュメントを読み込み、共通ルールを遵守すること
  * 重要: E2Eテスト生成においても、ルールドキュメントに記載されたすべてのルールを遵守すること
  * 注意: Agent Skills配下のルールは全プロジェクト共通。プロジェクト固有のルールがある場合は `{project_root}/principles/` も確認すること

### 1.2 基本設計の仕様

以下のファイルを読み込み、システム全体の設計を理解する

* {spec_directory}/basic_design/architecture_design.md - 技術スタック、パッケージ構造、テスト設定を確認する
  * ベースURL、ポート番号
  * セッション管理方式
  * テストフレームワーク設定

* {spec_directory}/basic_design/screen_design.md - 全画面の設計を確認する
  * 画面一覧
  * 画面遷移
  * フィールド、ボタン等のUI要素

* {spec_directory}/basic_design/functional_design.md - システム全体の機能設計を確認する
  * 全ての機能仕様
  * 画面遷移フロー
  * データ受け渡し

* {spec_directory}/basic_design/behaviors.md - E2Eテストシナリオを確認する
  * システム全体の振る舞い
  * 画面間遷移シナリオ
  * エンドツーエンドのフロー
  * 例: 一覧表示 → 新規追加 → 入力 → 確認 → 登録 → 一覧表示

---

## 2. Playwright のセットアップ

### 2.1 プロジェクトへの追加

Playwright for Javaを使用する場合:

Gradle依存関係追加 (`build.gradle`):

```gradle
dependencies {
    // Playwright
    testImplementation 'com.microsoft.playwright:playwright:1.40.0'
    
    // JUnit 5
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
}

test {
    useJUnitPlatform()
    
    // E2Eテストはタグで分離
    exclude '**/*E2ETest.class'
}

// E2Eテスト専用タスク
task e2eTest(type: Test) {
    useJUnitPlatform {
        includeTags 'e2e'
    }
    description = 'Runs E2E tests with Playwright'
    group = 'verification'
    
    // ヘッドレスモード切り替え
    systemProperty 'headless', System.getProperty('headless', 'true')
}
```

### 2.2 Playwright の初期化

テストベースクラスを作成:

```java
package pro.kensait.person.e2e;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

@Tag("e2e")
public abstract class BaseE2ETest {
    
    protected static Playwright playwright;
    protected static Browser browser;
    protected BrowserContext context;
    protected Page page;
    
    protected static String baseUrl;
    
    @BeforeAll
    static void launchBrowser() {
        // architecture_design.mdから取得した設定
        baseUrl = System.getProperty("test.baseUrl", "http://localhost:8080/jsf-person-sdd");
        
        playwright = Playwright.create();
        
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
            .setHeadless(headless)
            .setSlowMo(50)); // デバッグ時に操作を確認しやすくする
    }
    
    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
    
    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext(new Browser.NewContextOptions()
            .setBaseURL(baseUrl)
            .setViewportSize(1280, 720));
        
        page = context.newPage();
        
        // スクリーンショット保存先
        page.onDialog(dialog -> dialog.accept()); // アラートダイアログを自動で受け入れ
    }
    
    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
        }
    }
    
    /**
     * スクリーンショットを保存
     */
    protected void takeScreenshot(String filename) {
        page.screenshot(new Page.ScreenshotOptions()
            .setPath(Paths.get("build/screenshots/" + filename))
            .setFullPage(true));
    }
}
```

---

## 3. E2Eテストケース生成

### 3.1 テストケース設計方針

* 1シナリオ＝1テストクラスの粒度
* basic_design/behaviors.md の各Given-When-Thenシナリオを実際のブラウザ操作としてテスト
* 複数画面にまたがるエンドツーエンドのフローをテスト
* 実際のDBアクセスを含む（テストデータの準備と検証）
* 画面遷移、ボタンクリック、フォーム入力、バリデーション、エラーメッセージの検証

### 3.2 テストケーステンプレート

シナリオ例: PERSON新規追加フロー

```java
package pro.kensait.person.e2e;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.*;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("e2e")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PersonAddE2ETest extends BaseE2ETest {
    
    @Test
    @Order(1)
    @DisplayName("1. 一覧画面を表示できる")
    void test_displayPersonList() {
        // When: 一覧画面にアクセス
        page.navigate("/personList.xhtml");
        
        // Then: ページタイトルが表示される
        assertThat(page.locator("h1")).hasText("PERSON一覧");
        
        // 新規追加ボタンが表示される
        assertThat(page.locator("text=新規追加")).isVisible();
        
        // テーブルが表示される
        assertThat(page.locator("table")).isVisible();
    }
    
    @Test
    @Order(2)
    @DisplayName("2. 新規追加ボタンをクリックして入力画面に遷移")
    void test_navigateToInputScreen() {
        // Given: 一覧画面を表示
        page.navigate("/personList.xhtml");
        
        // When: 新規追加ボタンをクリック
        page.click("text=新規追加");
        
        // Then: 入力画面に遷移
        page.waitForURL("**/personInput.xhtml");
        assertThat(page.locator("h1")).hasText("PERSON入力");
        
        // 入力フォームが表示される
        assertThat(page.locator("input[name*='personName']")).isVisible();
        assertThat(page.locator("input[name*='age']")).isVisible();
        assertThat(page.locator("input[type='radio']")).first().isVisible();
    }
    
    @Test
    @Order(3)
    @DisplayName("3. PERSON情報を入力して確認画面に遷移")
    void test_inputPersonData() {
        // Given: 入力画面を表示
        page.navigate("/personInput.xhtml");
        
        // When: フォームに入力
        page.fill("input[name*='personName']", "山田太郎");
        page.fill("input[name*='age']", "30");
        page.check("input[value='male']"); // 性別: 男性
        
        // 確認画面へボタンをクリック
        page.click("text=確認画面へ");
        
        // Then: 確認画面に遷移
        page.waitForURL("**/personConfirm.xhtml");
        assertThat(page.locator("h1")).hasText("PERSON確認");
        
        // 入力内容が表示される
        assertThat(page.locator("text=山田太郎")).isVisible();
        assertThat(page.locator("text=30")).isVisible();
        assertThat(page.locator("text=男性")).isVisible();
    }
    
    @Test
    @Order(4)
    @DisplayName("4. 登録ボタンをクリックしてDBに保存")
    void test_registerPerson() {
        // Given: 確認画面まで遷移
        page.navigate("/personInput.xhtml");
        page.fill("input[name*='personName']", "佐藤花子");
        page.fill("input[name*='age']", "25");
        page.check("input[value='female']"); // 性別: 女性
        page.click("text=確認画面へ");
        page.waitForURL("**/personConfirm.xhtml");
        
        // When: 登録ボタンをクリック
        page.click("text=登録");
        
        // Then: 一覧画面にリダイレクト
        page.waitForURL("**/personList.xhtml");
        assertThat(page.locator("h1")).hasText("PERSON一覧");
        
        // 新しく登録したPERSONが一覧に表示される
        assertThat(page.locator("text=佐藤花子")).isVisible();
        assertThat(page.locator("text=25")).isVisible();
        assertThat(page.locator("text=女性")).isVisible();
    }
    
    @Test
    @Order(5)
    @DisplayName("5. エンドツーエンドフロー: 新規追加→登録→一覧確認")
    void test_fullAddFlow() {
        // 1. 一覧画面にアクセス
        page.navigate("/personList.xhtml");
        assertThat(page.locator("h1")).hasText("PERSON一覧");
        
        // 2. 新規追加ボタンをクリック
        page.click("text=新規追加");
        page.waitForURL("**/personInput.xhtml");
        
        // 3. フォームに入力
        page.fill("input[name*='personName']", "鈴木一郎");
        page.fill("input[name*='age']", "40");
        page.check("input[value='male']");
        page.click("text=確認画面へ");
        
        // 4. 確認画面で内容確認
        page.waitForURL("**/personConfirm.xhtml");
        assertThat(page.locator("text=鈴木一郎")).isVisible();
        
        // 5. 登録
        page.click("text=登録");
        
        // 6. 一覧画面で確認
        page.waitForURL("**/personList.xhtml");
        assertThat(page.locator("text=鈴木一郎")).isVisible();
        
        takeScreenshot("person-add-success.png");
    }
}
```

### 3.3 バリデーションエラーのテスト

```java
@Test
@DisplayName("必須入力チェック: 名前が空の場合エラーメッセージ表示")
void test_validationError_emptyName() {
    // Given: 入力画面を表示
    page.navigate("/personInput.xhtml");
    
    // When: 名前を空のまま確認画面へ
    page.fill("input[name*='age']", "30");
    page.check("input[value='male']");
    page.click("text=確認画面へ");
    
    // Then: エラーメッセージが表示される
    assertThat(page.locator(".error-messages")).isVisible();
    assertThat(page.locator("text=名前を入力してください")).isVisible();
    
    // 入力画面のまま遷移しない
    assertThat(page).hasURL(baseUrl + "/personInput.xhtml");
}

@Test
@DisplayName("境界値チェック: 年齢が負の数の場合エラー")
void test_validationError_negativeAge() {
    page.navigate("/personInput.xhtml");
    
    page.fill("input[name*='personName']", "テスト太郎");
    page.fill("input[name*='age']", "-1");
    page.check("input[value='male']");
    page.click("text=確認画面へ");
    
    assertThat(page.locator(".error-messages")).isVisible();
    assertThat(page.locator("text=年齢は0以上で入力してください")).isVisible();
}
```

### 3.4 編集・削除フローのテスト

```java
@Test
@DisplayName("PERSON編集フロー")
void test_editPerson() {
    // Given: 一覧画面に既存PERSONが表示されている
    page.navigate("/personList.xhtml");
    
    // When: 編集ボタンをクリック（例: 1番目のPERSON）
    page.locator("text=編集").first().click();
    page.waitForURL("**/personInput.xhtml?personId=*");
    
    // Then: 既存データがプリセットされている
    assertThat(page.locator("input[name*='personName']")).not().hasValue("");
    
    // When: 年齢を変更
    page.fill("input[name*='age']", "35");
    page.click("text=確認画面へ");
    page.waitForURL("**/personConfirm.xhtml");
    page.click("text=登録");
    
    // Then: 一覧画面で更新後の値が表示される
    page.waitForURL("**/personList.xhtml");
    assertThat(page.locator("text=35")).isVisible();
}

@Test
@DisplayName("PERSON削除フロー")
void test_deletePerson() {
    // Given: 一覧画面
    page.navigate("/personList.xhtml");
    
    // 削除前の行数を記録
    int initialRowCount = page.locator("table tbody tr").count();
    
    // When: 削除ボタンをクリック（JavaScriptの確認ダイアログが表示される）
    page.onDialog(dialog -> {
        assertThat(dialog.message()).contains("削除してもよろしいですか");
        dialog.accept(); // OKをクリック
    });
    
    page.locator("text=削除").first().click();
    
    // Then: 行数が1つ減る
    page.waitForTimeout(500); // 削除処理完了を待つ
    assertThat(page.locator("table tbody tr")).hasCount(initialRowCount - 1);
}
```

---

## 4. テストデータの準備

### 4.1 DBのセットアップ

E2Eテスト用のテストデータを準備:

```java
@BeforeAll
static void setupTestData() {
    // テストデータをDBに挿入
    // 方法1: SQLスクリプトを実行
    // 方法2: アプリケーション経由でデータを作成（画面操作）
    // 方法3: DBに直接アクセス（EntityManager等）
}

@AfterAll
static void cleanupTestData() {
    // テストデータをクリーンアップ
}
```

### 4.2 テストデータ管理のベストプラクティス

* テスト間の独立性を保つ
* 各テストで一意のデータを使用（UUID、タイムスタンプ等）
* @BeforeEachでテストデータ準備、@AfterEachでクリーンアップ

---

## 5. テストの実行

### 5.1 前提条件

E2Eテストを実行する前に:

1. アプリケーションサーバーを起動
   ```bash
   ./gradlew run
   # または
   java -jar build/libs/app.war
   ```

2. データベースが起動していることを確認

3. テスト用データの準備（必要に応じて）

### 5.2 E2Eテストの実行

```bash
# ヘッドレスモードで実行（デフォルト）
./gradlew e2eTest

# ブラウザを表示して実行（デバッグ用）
./gradlew e2eTest -Dheadless=false

# 通常のテスト（単体テスト）のみ実行（E2Eテストは除外）
./gradlew test

# 全てのテストを実行
./gradlew test e2eTest
```

### 5.3 Playwright CLI ツール

Playwrightの便利なツール:

```bash
# コードジェネレーター（画面操作を記録してコード生成）
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="codegen http://localhost:8080/jsf-person-sdd/personList.xhtml"

# インスペクター（デバッグ）
# テストコードに以下を追加:
page.pause(); // ここでブラウザが一時停止し、手動で操作・検証できる
```

---

## 6. テストレポート

### 6.1 テスト結果の確認

* JUnit 5のレポート: `build/reports/tests/e2eTest/index.html`
* スクリーンショット: `build/screenshots/`
* ビデオ録画（オプション）: `build/videos/`

### 6.2 スクリーンショットとビデオ録画

```java
@BeforeEach
void createContextAndPage() {
    context = browser.newContext(new Browser.NewContextOptions()
        .setBaseURL(baseUrl)
        .setViewportSize(1280, 720)
        .setRecordVideoDir(Paths.get("build/videos/")) // ビデオ録画を有効化
        .setRecordVideoSize(1280, 720));
    
    page = context.newPage();
}

@AfterEach
void closeContextWithVideo(TestInfo testInfo) {
    if (testInfo.getTestMethod().isPresent()) {
        String testName = testInfo.getTestMethod().get().getName();
        
        // テスト失敗時のみスクリーンショットを保存
        if (testInfo.getExecutionException().isPresent()) {
            takeScreenshot(testName + "-failure.png");
        }
    }
    
    if (context != null) {
        context.close(); // ビデオが自動保存される
    }
}
```

---

## 7. requirements/behaviors.md からのテストケース生成

### 7.1 シナリオの読み取り

requirements/behaviors.md の各シナリオを読み取り、以下の情報を抽出:

* Given: 初期状態、表示する画面、テストデータの準備
* When: 実行するユーザー操作（ボタンクリック、入力、選択等）
* Then: 期待される結果（画面遷移、表示内容、DB状態）

### 7.2 シナリオ例とテストコードの対応

behaviors.mdの例:
```
Given: ユーザーが一覧画面を表示している
When: 新規追加ボタンをクリックする
Then:
  * personInput.xhtmlに遷移する
  * personIdは指定されない（新規追加モード）
  * 入力フォームが空の状態で表示される
```

生成されるテストコード:
```java
@Test
@DisplayName("新規追加ボタンをクリックして入力画面に遷移")
void test_navigateToInputScreen() {
    // Given: 一覧画面を表示
    page.navigate("/personList.xhtml");
    assertThat(page.locator("h1")).hasText("PERSON一覧");
    
    // When: 新規追加ボタンをクリック
    page.click("text=新規追加");
    
    // Then: 入力画面に遷移
    page.waitForURL("**/personInput.xhtml");
    assertThat(page.locator("h1")).hasText("PERSON入力");
    
    // personIdがURLパラメータに含まれないことを確認（新規追加モード）
    assertThat(page.url()).not().contains("personId=");
    
    // 入力フォームが空
    assertThat(page.locator("input[name*='personName']")).hasValue("");
    assertThat(page.locator("input[name*='age']")).hasValue("");
}
```

---

## 8. CI/CD パイプラインとの統合

### 8.1 GitHub Actions の例

```yaml
name: E2E Tests

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  e2e-test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: testdb
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        ports:
          - 5432:5432
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Install Playwright browsers
        run: mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps"
      
      - name: Build application
        run: ./gradlew build
      
      - name: Start application
        run: |
          ./gradlew run &
          sleep 30 # アプリケーションの起動を待つ
      
      - name: Run E2E tests
        run: ./gradlew e2eTest -Dheadless=true
      
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: e2e-test-results
          path: |
            build/reports/tests/e2eTest/
            build/screenshots/
            build/videos/
```

---

## 9. 注意事項

### 9.1 テスト実行環境

* E2Eテストは実際の環境（またはステージング環境）で実行する
* テスト用のDBを使用し、本番DBは使用しない
* テスト後はデータをクリーンアップする

### 9.2 テストの安定性

* 非同期処理を考慮して適切な待機を設定（`page.waitForURL`, `page.waitForSelector`）
* JavaScriptの実行完了を待つ
* ネットワーク遅延を考慮してタイムアウトを設定

### 9.3 パフォーマンス

* E2Eテストは実行時間が長いため、並列実行を検討
* 重要なシナリオのみをE2Eテストでカバー
* 詳細なテストは単体テストで実施

### 9.4 ブラウザ互換性

Playwrightは複数のブラウザをサポート:

```java
// Chromium
browser = playwright.chromium().launch();

// Firefox
browser = playwright.firefox().launch();

// WebKit (Safari)
browser = playwright.webkit().launch();
```

---

## 10. 参考資料

* Playwright for Java公式ドキュメント: https://playwright.dev/java/
* JUnit 5公式ドキュメント: https://junit.org/junit5/
* requirements/behaviors.md - E2Eテストシナリオ
* basic_design/screen_design.md - 画面設計
* basic_design/functional_design.md - 機能仕様
* basic_design/architecture_design.md - システム構成
