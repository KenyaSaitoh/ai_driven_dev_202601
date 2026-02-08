# E2Eテスト生成インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
```

* 例
```yaml
project_root: "projects/sdd-wf/bookstore/back-office-api-sdd"
spec_directory: "projects/sdd-wf/bookstore/back-office-api-sdd/specs/baseline"
```

注意
* パス区切りはOS環境に応じて調整する（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換える
* 以降、`{spec_directory}` と表記されている箇所は、上記で設定した値に置き換える

---

## 概要

このインストラクションは、REST API のE2Eテスト（End-to-End Test）を生成するためのものである

重要な方針
* 実装完了後にE2Eテストを生成する（code_generation.mdの次のステップ）
* **テストフレームワーク（2種類を並行使用）:**
  * **主: JUnit 5 + REST Assured** - 従来型のE2Eテスト（必須）
  * **補助・実験的: JUnit 5 + Cucumber + REST Assured** - Gherkin記法によるBDD形式テスト（オプション）
* テスト対象: requirements/behaviors.md（E2Eテスト用）のシナリオ（Gherkin 記法で記述されている前提。@agent_skills/jakarta-ee-api-base/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照）
* 複数機能間の連携、実際のHTTPリクエスト/レスポンス、実際のDBアクセスを含む
* アプリケーションサーバーが起動している状態でテストを実行
* **既存テストの扱い（重要）:**
  * 既存の JUnit + REST Assured テストコードは削除せずに保護する
  * 既存の Cucumber テストコード（.feature ファイルやステップ定義）が存在する場合は、それらを削除せずに読み込んで、差分のみを反映する
  * ファイルをゼロから作り直すのではなく、既存の内容を尊重して必要なテストケースのみを追加・修正する
  * 新規テストファイルが必要な場合のみ、新規作成する

---

## 1. SPECの読み込みと理解

パラメータで指定されたプロジェクト情報に基づいて、以下の設計ドキュメントを読み込んで分析する

### 1.1 Agent Skillsルール（最優先で確認）

* @agent_skills/jakarta-ee-api-base/principles/ - Jakarta EE開発の原則、アーキテクチャ標準、品質基準、セキュリティ標準を確認する
  * このフォルダ配下の原則ドキュメントを読み込み、共通ルールを遵守すること
  * 重要: E2Eテスト生成においても、ルールドキュメントに記載されたすべてのルールを遵守すること
  * 注意: Agent Skills配下のルールは全プロジェクト共通。プロジェクト固有のルールがある場合は `{project_root}/principles/` も確認すること

### 1.2 基本設計の仕様

以下のファイルを読み込み、システム全体の設計を理解する

* {spec_directory}/basic_design/architecture_design.md - 技術スタック、パッケージ構造、テスト設定を確認する
  * ベースURL、ポート番号
  * 認証方式（JWT等）
  * テストフレームワーク設定

* {spec_directory}/basic_design/functional_design.md - システム全体の機能設計（全APIを含む）を確認する
  * 全てのAPI仕様
  * エンドポイント一覧
  * リクエスト/レスポンス形式

* {spec_directory}/requirements/behaviors.md - E2Eテストシナリオを確認する
  * システム全体の振る舞い
  * API間連携シナリオ
  * E2Eのフロー
  * 例: 認証 → 書籍検索 → 注文作成 → 在庫更新

---

## 2. REST Assured のセットアップ

### 2.1 依存関係

E2Eテスト生成に必要なライブラリ:

* REST Assured（rest-assured, json-path, xml-path）
* JUnit 5: `org.junit.jupiter:junit-jupiter:5.10.0`
* JUnit Platform: `org.junit.platform:junit-platform-launcher:1.10.0`
* JUnit Platform Suite: `org.junit.platform:junit-platform-suite:1.10.0` (Cucumber使用時に必要)
* Jackson（JSON処理）

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
* @BeforeAll: architecture_design.md のベースURL・ポートに合わせ、RestAssured.baseURI/basePath と RequestSpecBuilder で Content-Type/Accept を設定
* 認証が必要なAPI向けに、ログインAPIを呼びトークン（cookie または header）を返す login(employeeCode, password) を用意する

---

## 3. E2Eテストケース生成

### 3.1 テストケース設計方針（共通）

* requirements/behaviors.md のシナリオに基づいてテストを生成
* 複数APIにまたがるE2Eのフローをテスト
* 実際のDBアクセスを含む（テストデータの準備と検証）
* HTTPステータスコード、レスポンスボディ、ヘッダーの検証
* @Tag("e2e") を付与し、e2eTest タスクで実行されるようにする

### 3.2 主テスト: JUnit 5 + REST Assured（従来型、必須）

* `src/test/java` 配下に通常のJUnitテストクラスを作成
* BaseE2ETest を継承（REST Assuredの設定、認証トークン管理）
* @Tag("e2e") を付与
* テストメソッドは @Test アノテーションで実装
* behaviors.md のシナリオを参考に、Given-When-Then の流れでテストを記述

**例:**
```java
@Tag("e2e")
class OrderE2ETest extends BaseE2ETest {
    @Test
    void testCreateOrder_E2E() {
        // Given: 認証トークン取得、初期データ準備
        String token = login("user@example.com", "password");
        
        // When: API呼び出し
        Response response = given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body(orderRequest)
            .when()
            .post("/api/orders");
        
        // Then: レスポンス検証
        response.then()
            .statusCode(201)
            .body("orderId", notNullValue());
    }
}
```

### 3.3 補助テスト: JUnit 5 + Cucumber + REST Assured（BDD形式、実験的・オプション）

* requirements/behaviors.md（E2Eテスト用）の Gherkin シナリオを、**Cucumber .feature ファイル**（`src/test/resources/features/e2e` 配下）と **Cucumber ステップ定義**（Java、REST Assured を利用）に変換する
* 1シナリオ＝1 Feature または 1 Scenario の粒度で .feature に記述
* 各 Given-When-Then を実際のHTTPリクエストとしてステップ定義で実装
* feature およびステップ定義に @Tag("e2e") を付与
* **注意**: Cucumberテストは補助的・実験的な位置づけであり、従来のJUnit + REST Assuredテストを置き換えるものではない

**重要: Cucumberの日本語アノテーション問題について**
* Cucumberの日本語アノテーション（`io.cucumber.java.ja.*`）はコンパイルエラーが発生する可能性がある
* **推奨**: Cucumberテストは完全にオプショナルなので、**生成をスキップすることを推奨**
* どうしてもCucumberテストが必要な場合は、英語アノテーション（`io.cucumber.java.en.*`）を使用すること
  * `@Given`, `@When`, `@Then`, `@And` は `io.cucumber.java.en` パッケージから import
  * .feature ファイルも英語で記述する（`# language: ja` は使用しない）
* Cucumberテストを生成しない場合でも、.feature ファイル（ドキュメント用）は作成してよい（ステップ定義なし）

### 3.4 Wiremock の利用

* E2Eテストでも、外部サービスのスタブ化が必要な場合は Wiremock を利用可能
* Wiremock を使用したテストも削除せず、既存テストと共存させる

### 3.2 テストケースのポイント

* 1シナリオ＝1テストクラス、BaseE2ETest を継承。複数APIにまたがるフローは @Order で順序付け可能
* Given: @BeforeAll で login() によりトークン取得。必要に応じて GET で初期状態（在庫数など）を取得
* When: given(requestSpec).queryParam/body(...).when().get/post/put/delete(エンドポイント).then() で HTTP リクエスト送信
* Then: .statusCode(期待値)、.body("jsonPath", Matcher) でレスポンス検証。必要なら .extract().path("jsonPath") で値を取り次のテストに渡す
* functional_design.md のエンドポイント・リクエスト形式に合わせてパス・ボディを組み立てる

### 3.3 認証が必要なAPI

* 認証なしで呼ぶと 401 になることを statusCode(401) で検証。認証ありでは cookie("jwtToken", token) または header("Authorization", "Bearer " + token) を付与して 200 とボディを検証する

### 3.4 エラーケース

* 404: 存在しないIDで GET し statusCode(404)、body("message", ...) を検証
* 400: 不正なボディで POST し statusCode(400) を検証

---

## 4. テストデータの準備

### 4.1 DBのセットアップ

* E2E用データは SQL スクリプト・REST API 経由・または DB 直接のいずれかで準備。@AfterAll でクリーンアップする

### 4.2 テストデータ管理のベストプラクティス

@agent_skills/jakarta-ee-api-base/principles/architecture.md の「9.4 テストデータ管理」を参照する。

---

## 5. requirements/behaviors.md からのテストケース生成

### 5.1 シナリオの読み取り

requirements/behaviors.md は Gherkin 記法で記述されている。@agent_skills/jakarta-ee-api-base/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照の上、各シナリオから Given/When/Then を抽出する。

### 5.2 シナリオとテストの対応

* Given: login() でトークン取得。必要なら GET で初期状態（在庫数など）を .extract().path() で取得
* When: POST/GET 等でエンドポイントを呼び出し（functional_design の形式に合わせる）
* Then: statusCode(201/200)、body で注文ID・在庫数などを検証。在庫減少は GET で再取得して initialStock - 注文数 と比較する

---

## 6. 注意事項

### 6.1 テスト実行環境

* E2Eテストはアプリケーションサーバー起動済みの状態で実行する。テスト用DBを使用し、本番DBは使用しない。テスト後はデータをクリーンアップする。

### 6.2 テストの安定性

* ネットワーク遅延を考慮してタイムアウトを設定。テスト間の依存関係を避ける。

### 6.3 既存の単体テスト用Cucumberテストランナーとの競合回避

* 既存の `src/test/java/.../cucumber/CucumberTestRunner.java` は単体テスト用である
* E2Eテストを実行する際、CucumberTestRunnerが存在するとコンパイルエラーが発生する可能性がある（JUnit Platform Suiteの依存関係が不足）
* 対処方法:
  * プロジェクトのbuild.gradleまたは共通のbuild.gradleに `org.junit.platform:junit-platform-suite` を追加する
  * CucumberTestRunnerのインポート文を明示的に記述する（ワイルドカードインポートを避ける）
  * または、CucumberTestRunnerを単体テスト専用として保持し、E2Eテストでは従来のJUnit + REST Assuredのみを使用する

---

## 7. 参考資料

* REST Assured公式ドキュメント: https://rest-assured.io/
* JUnit 5公式ドキュメント: https://junit.org/junit5/
* requirements/behaviors.md - E2Eテストシナリオ
* basic_design/functional_design.md - API仕様
* basic_design/architecture_design.md - システム構成
