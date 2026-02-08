# E2Eテスト生成インストラクション（アジャイル）

## パラメータ設定

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
usecase_folder: null        # オプション。指定時はそのユースケースのE2Eテストのみ生成
build_script_path: null     # オプション。build.gradleファイルのパス（未指定時は project_root の build.gradle を使用）
                            # マルチプロジェクト構成の場合、ルートの build.gradle または サブプロジェクトの build.gradle を指定
                            # 例: "./build.gradle" (リポジトリルート) または "d:/GitHubRepos/.../build.gradle" (絶対パス)
```

* 例
```yaml
project_root: "projects/sdd-agile/bookstore/berry-books-api"
spec_directory: "projects/sdd-agile/bookstore/berry-books-api/specs/baseline"
build_script_path: "./build.gradle"  # マルチプロジェクト構成用
```

注意: パス区切りはOS環境に応じて調整する。以降、`{project_root}`, `{spec_directory}` はパラメータで設定した値に置き換える。

---

## 概要

このインストラクションは、アジャイル版のSPECに基づきE2Eテスト（End-to-End Test）を生成するためのものである。

重要な方針
* **テストフレームワーク（2種類を並行使用）:**
  * **主: JUnit 5 + REST Assured** - 従来型のE2Eテスト（必須）
  * **補助・実験的: JUnit 5 + Cucumber + REST Assured** - Gherkin記法によるBDD形式テスト（オプション）
* テスト対象: usecases/{名}/behaviors.md のシナリオ（Gherkin記法）。E2EとしてAPI層を含む全体フローを検証する
* usecase_folder 未指定時: usecases/ 配下の各 behaviors.md を集約し、複数ユースケースにまたがるE2Eシナリオも生成可能とする
* アプリケーションサーバーが起動している状態でテストを実行する
* 実際のHTTPリクエスト/レスポンス、認証（JWT等）、DBアクセスを含む
* **既存テストの保護**: 既存の JUnit + REST Assured テストコードは削除せず、必要に応じてCucumberテストを追加する

---

## 1. 読み込むドキュメント

* @agent_skills/jakarta-ee-api-agile/principles/ を確認する
* {spec_directory}/common/architecture_design.md - ベースURL、ポート、認証方式、テスト設定
* {spec_directory}/common/data_model.md, external_interface.md - 必要に応じて参照
* E2Eシナリオの参照元:
  * usecase_folder 指定時: {spec_directory}/usecases/{usecase_folder}/behaviors.md
  * 未指定時: {spec_directory}/usecases/ 配下の各 behaviors.md を参照し、E2E用シナリオを集約

---

## 2. E2Eテストの生成

**マルチプロジェクト構成の考慮:**
* Gradleのマルチプロジェクト構成の場合、build.gradleの場所はサブプロジェクトごとに異なる
* build_script_path パラメータでルートの build.gradle のパスを指定すること（例: "./build.gradle"）
* 指定されたパスからディレクトリ部分を抽出してそのディレクトリで `./gradlew` コマンドを実行する
* ルートプロジェクトの build.gradle でサブプロジェクトのタスクを実行する場合は `:subproject:e2eTest` のような形式を使用

### 2.1 主テスト: JUnit 5 + REST Assured（従来型、必須）

* `src/test/java` 配下に通常のJUnitテストクラスを作成
* BaseE2ETest を継承（REST Assuredの設定、認証トークン管理）
* @Tag("e2e") を付与
* テストメソッドは @Test アノテーションで実装
* behaviors.md のシナリオを参考に、Given-When-Then の流れでテストを記述
* 認証フロー（ログイン→トークン取得）のセットアップ、複数APIの連携、レスポンス検証、テストデータのセットアップ/クリーンアップを実装する

**例:**
```java
@Tag("e2e")
class OrderUseCaseE2ETest extends BaseE2ETest {
    @Test
    void testCreateOrderUseCase() {
        // Given: 認証、初期データ
        String token = login("user@example.com", "password");
        
        // When & Then: 複数API呼び出しと検証
        given().header("Authorization", "Bearer " + token)
            .when().post("/api/orders")
            .then().statusCode(201);
    }
}
```

### 2.2 補助テスト: JUnit 5 + Cucumber + REST Assured（BDD形式、実験的・オプション）

* usecases/{名}/behaviors.md の Gherkin シナリオを、**Cucumber の .feature ファイル**（`src/test/resources/features/e2e` 配下）と **Cucumber ステップ定義**（Java、REST Assured を利用）に変換する
* 認証フロー（ログイン→トークン取得）のセットアップ、複数APIの連携、レスポンス検証、テストデータのセットアップ/クリーンアップをステップ定義内に実装する
* feature およびステップ定義に @Tag("e2e") を付与し、プロジェクトの e2eTest タスクで実行されるようにする
* **注意**: Cucumberテストは補助的・実験的な位置づけであり、従来のJUnit + REST Assuredテストを置き換えるものではない

### 2.3 Wiremock の利用

* E2Eテストでも、外部サービスのスタブ化が必要な場合は Wiremock を利用可能
* Wiremock を使用したテストも削除せず、既存テストと共存させる

---

## 3. 参考

* [it_generation.md](it_generation.md) - 結合テスト生成
* ウォーターフォール版: @agent_skills/jakarta-ee-api-base/instructions/e2e_test_generation.md
