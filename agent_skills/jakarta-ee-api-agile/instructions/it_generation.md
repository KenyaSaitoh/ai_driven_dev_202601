# 結合テスト生成インストラクション（アジャイル）

## パラメータ設定

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
usecase_folder: null        # オプション。指定時はそのユースケースの結合テストのみ生成
build_script_path: null     # オプション（通常は不要）。マルチプロジェクト構成の場合のみ指定
                            # build.gradleファイルのパス（未指定時は project_root の build.gradle を使用）
                            # 例: "build.gradle" (リポジトリルート) または "d:/GitHubRepos/.../build.gradle" (絶対パス)
```

* 例
```yaml
project_root: "projects/sdd-agile/bookstore/berry-books-api"
spec_directory: "projects/sdd-agile/bookstore/berry-books-api/specs/baseline"
build_script_path: "build.gradle"  # マルチプロジェクト構成の場合のみ指定（リポジトリルート）
```

注意: パス区切りはOS環境に応じて調整する。以降、`{project_root}`, `{spec_directory}` はパラメータで設定した値に置き換える。

---

## 概要

このインストラクションは、アジャイル版のSPECに基づき結合テスト（Integration Test）を生成するためのものである。

重要な方針
* **テストフレームワーク（2種類を並行使用）:**
  * **主: JUnit 5 + Weld SE（CDIコンテナ）** - 従来型の結合テスト（必須）
  * **補助・実験的: JUnit 5 + Cucumber + Weld SE** - Gherkin記法によるBDD形式テスト（オプション）
* テスト対象: usecases/{名}/behaviors.md のシナリオ（Gherkin記法）。common 用の振る舞いが common/behaviors.md 等で定義されていればそれも参照する
* Service層以下（Service + DAO + Entity）の実際の連携をテストする。外部APIは WireMock でスタブ化
* アプリケーションサーバーは不要（Weld SE で CDI コンテナを起動）
* **既存テストの保護**: 既存の JUnit + Weld テストコードは削除せず、必要に応じてCucumberテストを追加する

---

## 1. 読み込むドキュメント

* @agent_skills/jakarta-ee-api-agile/principles/ を確認する
* {spec_directory}/common/architecture_design.md - 技術スタック、データソース、テスト設定
* {spec_directory}/common/data_model.md - データモデル
* {spec_directory}/common/external_interface.md - 外部API（WireMock スタブ化の対象）
* 結合テストシナリオの参照元:
  * usecase_folder 指定時: {spec_directory}/usecases/{usecase_folder}/behaviors.md
  * 未指定時: {spec_directory}/usecases/ 配下の各 behaviors.md を集約してシナリオを収集

---

## 2. 結合テストの生成

**マルチプロジェクト構成の考慮:**
* 通常は build_script_path の指定は不要です（project_root の build.gradle を自動使用）
* マルチプロジェクト構成の場合のみ、以下の対応が必要です：
  * 依存関係の追加や設定は、適切なbuild.gradleに対して行います
  * build_script_path パラメータでルートの build.gradle のパスを指定します（例: "build.gradle"）
  * 指定されたパスからディレクトリ部分を抽出してそのディレクトリで `./gradlew` コマンドを実行します
  * ルートプロジェクトの build.gradle でサブプロジェクトのタスクを実行する場合は `:subproject:integrationTest` のような形式を使用します

### 2.1 主テスト: JUnit 5 + Weld SE（従来型、必須）

* `src/test/java` 配下に通常のJUnitテストクラスを作成
* BaseIntegrationTest を継承（Weld SE によるCDIコンテナ起動、EntityManager管理）
* @Tag("integration") を付与
* Service層以下を実装で動かし、実際のDB（メモリDB）を使用する
* 外部API呼び出しは WireMock でスタブ化する（external_interface.md に従う）
* テストメソッドは @Test アノテーションで実装

**例:**
```java
@Tag("integration")
class OrderServiceIntegrationTest extends BaseIntegrationTest {
    @Test
    void testCreateOrder_Success() {
        // Arrange: WireMock スタブ設定、テストデータ投入
        // Act: Service メソッド呼び出し
        // Assert: DB検証、WireMock verify
    }
}
```

### 2.2 補助テスト: JUnit 5 + Cucumber + Weld SE（BDD形式、実験的・オプション）

* usecases/{名}/behaviors.md の Gherkin シナリオを、**Cucumber の .feature ファイル**（`src/test/resources/features/integration` 配下）と **Cucumber ステップ定義**（Java、Weld SE を利用）に変換する
* Service層以下を実装で動かし、実際のDB（メモリDB）を使用する
* 外部API呼び出しは WireMock でスタブ化する（external_interface.md に従う）
* feature およびステップ定義に @Tag("integration") を付与し、プロジェクトの integrationTest タスクで実行されるようにする
* **注意**: Cucumberテストは補助的・実験的な位置づけであり、従来のJUnit + Weldテストを置き換えるものではない

### 2.3 RestAssured や Wiremock の直接利用

* 結合テストでは、必要に応じて RestAssured や Wiremock を直接利用したテストも作成可能
* これらのテストも削除せず、既存テストと共存させる

---

## 3. 結合テスト実行

テストコード生成後、自動的に結合テストを実行する。

### 3.1 実行ディレクトリの決定

* `build_script_path` パラメータが指定されている場合:
  * `build_script_path` のディレクトリ部分を抽出（例: "build.gradle" → "."）
  * そのディレクトリに `cd` してからGradleタスクを実行
* `build_script_path` パラメータが未指定の場合（通常）:
  * `{project_root}` でGradleタスクを実行

### 3.2 Gradleタスク実行

```bash
# build_script_path のディレクトリで以下を実行
./gradlew integrationTest
```

実行するタスク:
* `integrationTest` - 結合テスト実行（@Tag("integration") が付与されたテスト）
* プロジェクトのbuild.gradleに定義されたタスク名に従うこと

マルチプロジェクト構成の場合:
* ルートの build.gradle から実行する場合: `./gradlew :subproject:integrationTest`
* サブプロジェクトの build.gradle から実行する場合: `./gradlew integrationTest`

### 3.3 テスト結果の確認

テスト実行後、以下を確認する:

1. **テスト結果レポート**
   * `{project_root}/build/reports/tests/integrationTest/index.html`
   * テスト成功数、失敗数、スキップ数を確認

2. **失敗したテストの分析**
   * 失敗したテストのスタックトレースを確認
   * 失敗の原因を特定（アサーション失敗、例外、タイムアウト等）

3. **エラーメッセージ**
   * Gradleの実行ログからエラーメッセージを抽出
   * コンパイルエラー、依存関係の問題、設定ミス等を確認

### 3.4 結果の報告

テスト実行結果をユーザーに報告する:

* **成功時**: 
  * "結合テストが正常に完了しました"
  * テスト件数と実行時間を表示
  
* **失敗時**:
  * "結合テストで失敗が検出されました"
  * 失敗したテストの詳細を表示
  * 推奨される対応策を提示

---

## 4. 参考

* [e2e_test_generation.md](e2e_test_generation.md) - E2Eテスト生成
* ウォーターフォール版: @agent_skills/jakarta-ee-api-base/instructions/it_generation.md
