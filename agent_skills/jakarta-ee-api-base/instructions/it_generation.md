# 結合テスト生成インストラクション

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

このインストラクションは、REST APIの結合テスト（Integration Test）を生成するためのものである

重要な方針
* 単体テスト実行評価後に結合テストを生成する（unit_test_execution.mdの次のステップ）
* テストフレームワーク: JUnit 5 + Weld SE（CDIコンテナ）
* テスト対象: basic_design/behaviors.md（結合テスト用）のシナリオ（Gherkin 記法で記述されている前提。@agent_skills/jakarta-ee-api-base/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照）
* Service層以下（Service + DAO + Entity）の実際の連携をテスト
* モックは使用しない（外部APIのみWireMockでスタブ化）
* アプリケーションサーバーは不要（Weld SEでCDIコンテナを起動）

---

## 1. SPECの読み込みと理解

パラメータで指定されたプロジェクト情報に基づいて、以下の設計ドキュメントを読み込んで分析する

### 1.1 Agent Skillsルール（最優先で確認）

* @agent_skills/jakarta-ee-api-base/principles/ - Jakarta EE開発の原則、アーキテクチャ標準、品質基準、セキュリティ標準を確認する
  * このフォルダ配下の原則ドキュメントを読み込み、共通ルールを遵守すること
  * 重要: 結合テスト生成においても、ルールドキュメントに記載されたすべてのルールを遵守すること
  * 注意: Agent Skills配下のルールは全プロジェクト共通。プロジェクト固有のルールがある場合は `{project_root}/principles/` も確認すること

### 1.2 基本設計の仕様

以下のファイルを読み込み、システム全体の設計を理解する

* {spec_directory}/basic_design/architecture_design.md - 技術スタック、パッケージ構造、テスト設定を確認する
  * 使用技術スタック
  * データソース設定（JNDI名）
  * 外部API連携設定

* {spec_directory}/basic_design/functional_design.md - システム全体の機能設計（全APIを含む）を確認する
  * 全てのAPI仕様
  * ビジネスロジック
  * データフロー

* {spec_directory}/basic_design/data_model.md - データモデルを確認する（該当する場合）
  * エンティティ定義
  * リレーション
  * 制約

* {spec_directory}/basic_design/behaviors.md - 結合テストシナリオを確認する
  * Service層以下の振る舞い
  * ビジネスロジックの検証シナリオ
  * データアクセスの検証シナリオ
  * 外部API連携の検証シナリオ
  * 例: OrderService → OrderDao → DB + 外部在庫API呼び出し

* {spec_directory}/basic_design/external_interface.md - 外部API仕様を確認する（該当する場合）
  * 外部APIエンドポイント
  * リクエスト/レスポンス形式
  * WireMockスタブ化の対象

---

## 2. Weld SE と WireMock のセットアップ

### 2.1 依存関係

結合テスト生成に必要なライブラリ（プロジェクトのビルド設定に合わせて追加）:

* Weld SE (CDI): weld-se-core
* WireMock (外部APIスタブ): wiremock-jre8
* JUnit 5: junit-jupiter
* JPA: Hibernate + HSQLDB (テスト用DB)

* 結合テストクラスには `@Tag("integration")` を付与し、通常の単体テスト実行から分離する

### 2.2 Weld SE の設定

* `src/test/resources/META-INF/beans.xml`: Jakarta EE Beans 4.0、`bean-discovery-mode="all"`

### 2.3 テスト用 persistence.xml

* `src/test/resources/META-INF/persistence.xml`: persistence-unit 名は `test-pu`、transaction-type は RESOURCE_LOCAL
* テスト対象のエンティティを `<class>` で列挙
* HSQLDB メモリ（jdbc:hsqldb:mem:testdb）、Hibernate で `hbm2ddl.auto=create-drop`、dialect=HSQLDialect

---

## 3. 結合テストケース生成

### 3.1 テストケース設計方針

* 1シナリオ＝1テストクラスの粒度
* basic_design/behaviors.md の各シナリオ（Gherkin 記法の Given-When-Then）をテスト
* Service層のビジネスロジックを中心にテスト
* 実際のDB（メモリDB）を使用
* 外部APIはWireMockでスタブ化
* API層（Resource）は含まない（E2Eテストで検証）

### 3.2 テストベースクラス

全結合テストで共通の abstract ベースクラスを用意する。ポイント:

* `@Tag("integration")` を付与
* `@BeforeAll`: SeContainerInitializer で Weld SE 起動、WireMockServer 起動・configureFor(localhost, ポート)
* `@AfterAll`: WireMock 停止、container.close()
* `@BeforeEach`: container から EntityManager 取得、`em.getTransaction().begin()`
* `@AfterEach`: トランザクションがアクティブなら rollback、wireMock.resetAll()

### 3.3 テストケース（Service層）のポイント

* 1シナリオ＝1テストクラス、BaseIntegrationTest を継承
* `@BeforeEach`: container.select(Service.class).get() で Service 取得（モックなし）
* Arrange: stubFor で外部APIのレスポンスをスタブ（URL・ステータス・ボディ）、em.persist でテストデータをDBに投入
* Act: Service のメソッドを直接呼び出し
* Assert: em.flush() 後に em.find で永続化結果を検証、verify() で外部APIが期待どおり呼ばれたことを検証
* 例外ケース: スタブでエラーレスポンスを返し、assertThrows(期待する例外.class, () -> service.メソッド(...)) で検証

### 3.4 DAO層の結合テストのポイント

* BaseIntegrationTest を継承、container から Dao を取得
* Arrange: em.persist でテストデータを投入、em.flush() と em.clear() でキャッシュをクリア
* Act: Dao の検索メソッドを実行
* Assert: 件数・内容を assert で検証

---

## 4. テストデータの準備

### 4.1 DBのセットアップ

* @BeforeEach で EntityManager を使い、依存関係を満たすエンティティを em.persist で投入し、em.flush() で反映する

### 4.2 テストデータ管理のベストプラクティス

@agent_skills/jakarta-ee-api-base/principles/architecture.md の「9.4 テストデータ管理」を参照する。

---

## 5. WireMockによる外部APIスタブ化

### 5.1 外部APIのスタブ設定

* external_interface.md に合わせ、stubFor(get/put/post(...).urlEqualTo/urlPathMatching(...)).willReturn(aResponse().withStatus(...).withHeader("Content-Type","application/json").withBody(...)) でスタブを定義する。@BeforeEach または 各 @Test の Arrange で設定

### 5.2 エラーケースのスタブ

* 在庫不足・タイムアウト・認証エラーなど、シナリオに応じて withStatus(400/401/409/500) と withBody でエラーレスポンスを返すスタブを用意する。遅延が必要な場合は withFixedDelay

---

## 6. basic_design/behaviors.md からのテストケース生成

### 6.1 シナリオの読み取り

basic_design/behaviors.md は Gherkin 記法で記述されている。@agent_skills/jakarta-ee-api-base/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照の上、各シナリオから Given/When/Then を抽出する。

### 6.2 シナリオとテストの対応

* Given: 初期データ（em.persist）と外部APIスタブ（stubFor）で再現する
* When: Service のメソッドを直接呼び出す（引数は functional_design / data_model に合わせる）
* Then: 戻り値の assert、em.flush() 後の em.find で永続化結果を検証、verify() で外部API呼び出しを検証する

---

## 7. 注意事項

### 7.1 テスト実行環境

* 結合テストは実際のDB（メモリDB）を使用する。アプリケーションサーバーは不要（Weld SEで起動）。WireMockは @BeforeAll で起動・@AfterAll で停止。テスト後はトランザクションロールバックで自動クリーンアップ。

### 7.2 テストの安定性

* テスト間の独立性を保つ（@BeforeEach/@AfterEachで初期化・クリーンアップ）。外部APIはWireMockでスタブ化。テストデータは一意にする（UUID等）。トランザクション境界を明確にする。

### 7.3 単体テスト vs 結合テスト vs E2Eテスト

| テスト種別 | 対象 | モック | 実行環境 | 目的 |
|-----------|------|--------|---------|------|
| 単体テスト | 個別クラス | あり（タスク外依存） | JUnit | クラスのロジック検証 |
| 結合テスト | Service + DAO + DB | 外部APIのみスタブ | JUnit + Weld SE | ビジネスロジック + データアクセス検証 |
| E2Eテスト | 全体（API層含む） | なし | REST Assured + APサーバー | ユーザー視点の全体フロー検証 |

---

## 8. 参考資料

* Weld SE公式ドキュメント: https://weld.cdi-spec.org/
* WireMock公式ドキュメント: https://wiremock.org/
* JUnit 5公式ドキュメント: https://junit.org/junit5/
* basic_design/behaviors.md - 結合テストシナリオ
* basic_design/functional_design.md - 機能仕様
* basic_design/architecture_design.md - システム構成
