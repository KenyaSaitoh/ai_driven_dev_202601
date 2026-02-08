# E2Eテスト生成インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
```

* 例
```yaml
project_root: "projects/sdd-wf/person/jsf-person-sdd"
spec_directory: "projects/sdd-wf/person/jsf-person-sdd/specs/baseline"
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
* **テストフレームワーク（2種類を並行使用）:**
  * **主: JUnit 5 + Playwright** - 従来型のE2Eテスト（必須）
  * **補助・実験的: JUnit 5 + Cucumber + Playwright** - Gherkin記法によるBDD形式テスト（オプション）
* テスト対象: basic_design/behaviors.md または requirements/behaviors.md（E2Eテスト用）のシナリオ（Gherkin 記法で記述されている前提。@agent_skills/struts-to-jsf-migration/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照）
* 複数画面にまたがるフロー、実際のブラウザ操作、実際のDBアクセスを含む
* アプリケーションサーバーが起動している状態でテストを実行
* **既存テストの扱い（重要）:**
  * 既存の JUnit + Playwright テストコードは削除せずに保護する
  * 既存の Cucumber テストコード（.feature ファイルやステップ定義）が存在する場合は、それらを削除せずに読み込んで、差分のみを反映する
  * ファイルをゼロから作り直すのではなく、既存の内容を尊重して必要なテストケースのみを追加・修正する
  * 新規テストファイルが必要な場合のみ、新規作成する

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

### 2.1 依存関係

E2Eテスト生成に必要なライブラリ:

* Playwright for Java (playwright)
* JUnit 5: `org.junit.jupiter:junit-jupiter:5.10.0`
* JUnit Platform: `org.junit.platform:junit-platform-launcher:1.10.0`
* JUnit Platform Suite: `org.junit.platform:junit-platform-suite:1.10.0` (Cucumber使用時に必要)

* E2Eテストクラスには `@Tag("e2e")` を付与し、通常の単体テスト実行から分離する

**依存関係の追加方法:**
* まず、対象プロジェクトの `build.gradle` を確認する
* プロジェクト内に `build.gradle` が存在しない、または依存関係が定義されていない場合:
  * 親ディレクトリやプロジェクトルートの `build.gradle` を探索する
  * 共通のビルドファイルで `subprojects` ブロックや全プロジェクト共通設定が定義されている可能性がある
  * 見つかった場合、そちらに依存関係を追加する
* `e2eTest` タスクについても同様に、既存の定義を確認してから追加の要否を判断する

**マルチプロジェクト構成の考慮:**
* Gradleのマルチプロジェクト構成の場合、build.gradleの場所はサブプロジェクトごとに異なる
* テスト実行時は、適切なbuild.gradleが存在するディレクトリで `./gradlew` コマンドを実行する必要がある
* ルートプロジェクトの build.gradle でサブプロジェクトのタスクを実行する場合は `:subproject:e2eTest` のような形式を使用

### 2.2 ベースクラスのポイント

* `@Tag("e2e")` の abstract ベースクラスを用意する
* @BeforeAll: architecture_design.md のベースURLに合わせ、Playwright.create()、chromium().launch()（headless はプロパティで切り替え可能）
* @AfterAll: browser.close()、playwright.close()
* @BeforeEach: browser.newContext(baseURL, viewport)、context.newPage()。page.onDialog(dialog -> dialog.accept()) で確認ダイアログを自動受け入れ
* @AfterEach: context.close()
* 必要に応じて page.screenshot() でスクリーンショット保存を用意する

---

## 3. E2Eテストケース生成

### 3.1 テストケース設計方針（共通）

* basic_design/behaviors.md または requirements/behaviors.md のシナリオに基づいてテストを生成
* 複数画面にまたがるエンドツーエンドのフローをテスト
* 実際のDBアクセスを含む（テストデータの準備と検証）
* 画面遷移、ボタンクリック、フォーム入力、バリデーション、エラーメッセージの検証
* @Tag("e2e") を付与し、e2eTest タスクで実行されるようにする

### 3.2 主テスト: JUnit 5 + Playwright（従来型、必須）

* `src/test/java` 配下に通常のJUnitテストクラスを作成
* BaseE2ETest を継承（Playwrightの設定、Browser/Page管理）
* @Tag("e2e") を付与
* テストメソッドは @Test アノテーションで実装
* behaviors.md のシナリオを参考に、Given-When-Then の流れでテストを記述

**例:**
```java
@Tag("e2e")
class PersonListE2ETest extends BaseE2ETest {
    @Test
    void testCreatePerson_E2E() {
        // Given: 一覧画面を表示
        page.navigate(baseUrl + "/personList.xhtml");
        
        // When: 新規追加ボタンをクリックして入力
        page.locator("text=新規追加").click();
        page.locator("input[name*='firstName']").fill("太郎");
        page.locator("input[name*='lastName']").fill("山田");
        page.locator("input[type='submit']").click();
        
        // Then: 一覧画面に戻り、データが表示される
        page.waitForURL("**/personList.xhtml");
        assertThat(page.locator("td:has-text('太郎')")).isVisible();
    }
}
```

### 3.3 補助テスト: JUnit 5 + Cucumber + Playwright（BDD形式、実験的・オプション）

* basic_design/behaviors.md または requirements/behaviors.md（E2Eテスト用）の Gherkin シナリオを、**Cucumber .feature ファイル**（`src/test/resources/features/e2e` 配下）と **Cucumber ステップ定義**（Java、Playwright を利用）に変換する
* 1シナリオ＝1 Feature または 1 Scenario の粒度で .feature に記述
* 各 Given-When-Then を実際のブラウザ操作としてステップ定義で実装
* feature およびステップ定義に @Tag("e2e") を付与
* **注意**: Cucumberテストは補助的・実験的な位置づけであり、従来のJUnit + Playwrightテストを置き換えるものではない

**重要: Cucumberの日本語アノテーション問題について**
* Cucumberの日本語アノテーション（`io.cucumber.java.ja.*`）はコンパイルエラーが発生する可能性がある
* **推奨**: Cucumberテストは完全にオプショナルなので、**生成をスキップすることを推奨**
* どうしてもCucumberテストが必要な場合は、英語アノテーション（`io.cucumber.java.en.*`）を使用すること
  * `@Given`, `@When`, `@Then`, `@And` は `io.cucumber.java.en` パッケージから import
  * .feature ファイルも英語で記述する（`# language: ja` は使用しない）
* Cucumberテストを生成しない場合でも、.feature ファイル（ドキュメント用）は作成してよい（ステップ定義なし）

### 3.2 テストケースのポイント

* 1シナリオ＝1テストクラス、BaseE2ETest を継承。screen_design.md / functional_design.md の画面パス・遷移に合わせる
* When: page.navigate(画面パス) で画面表示、page.locator(セレクタ).click() / page.fill() / page.check() で操作
* Then: page.waitForURL("**/期待する.xhtml") で遷移待ち、assertThat(page.locator(...)).hasText() / isVisible() / hasValue() で検証
* 複数画面フローは @Order で順序付け、一覧→入力→確認→登録→一覧 の流れを 1 テストで検証してもよい
* セレクタは JSF の name 属性（例: input[name*='personName']）やラベルテキスト（text=新規追加）を用いる

### 3.3 バリデーションエラーのテスト

* 不正値（空・範囲外）を入力して送信し、遷移しないこと（hasURL で入力画面のまま）とエラーメッセージ表示（.error-messages やメッセージ文言の locator）を検証する

### 3.4 編集・削除フローのテスト

* 編集: 一覧で「編集」クリック → personInput.xhtml?personId=* に遷移、既存値がプリセットされていること、変更して登録後一覧で反映されることを検証
* 削除: 一覧で「削除」クリック、onDialog で確認ダイアログを accept、行数が減ることを検証する

---

## 4. テストデータの準備

### 4.1 DBのセットアップ

* E2E用データは SQL スクリプト・画面操作経由・または DB 直接のいずれかで準備。@AfterAll でクリーンアップする

### 4.2 テストデータ管理のベストプラクティス

* テスト間の独立性を保つ。各テストで一意のデータを使用（UUID、タイムスタンプ等）。@BeforeEach で準備、@AfterEach でクリーンアップ

---

## 5. basic_design/behaviors.md または requirements/behaviors.md からのテストケース生成

### 5.1 シナリオの読み取り

behaviors.md は Gherkin 記法で記述されている。@agent_skills/struts-to-jsf-migration/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照の上、各シナリオから Given/When/Then を抽出する。

### 5.2 シナリオとテストの対応

* Given: page.navigate(画面) で初期画面を表示し、必要なら assert で状態を確認する
* When: ボタン・リンクのクリック、フォーム入力（fill/check）で操作する
* Then: waitForURL で遷移先を待ち、locator でタイトル・入力値・一覧の表示内容を検証する。新規追加モードでは URL に personId が含まれないこと、入力フォームが空であることを検証する

---

## 6. 注意事項

### 6.1 テスト実行環境

* E2Eテストはアプリケーションサーバー起動済みの状態で実行する。テスト用DBを使用し、本番DBは使用しない。テスト後はデータをクリーンアップする。

### 6.2 テストの安定性

* 非同期処理を考慮して waitForURL / waitForSelector で待機する。ネットワーク遅延を考慮してタイムアウトを設定する。

### 6.3 ブラウザ

* Playwright は chromium / firefox / webkit を選択可能。通常は chromium で十分。

### 6.4 既存の単体テスト用Cucumberテストランナーとの競合回避

* 既存の `src/test/java/.../cucumber/CucumberTestRunner.java` は単体テスト用である
* E2Eテストを実行する際、CucumberTestRunnerが存在するとコンパイルエラーが発生する可能性がある（JUnit Platform Suiteの依存関係が不足）
* 対処方法:
  * プロジェクトのbuild.gradleまたは共通のbuild.gradleに `org.junit.platform:junit-platform-suite` を追加する
  * CucumberTestRunnerのインポート文を明示的に記述する（ワイルドカードインポートを避ける）
  * または、CucumberTestRunnerを単体テスト専用として保持し、E2Eテストでは従来のJUnit + Playwrightのみを使用する

---

## 7. 参考資料

* Playwright for Java公式ドキュメント: https://playwright.dev/java/
* JUnit 5公式ドキュメント: https://junit.org/junit5/
* requirements/behaviors.md - E2Eテストシナリオ
* basic_design/screen_design.md - 画面設計
* basic_design/functional_design.md - 機能仕様
* basic_design/architecture_design.md - システム構成
