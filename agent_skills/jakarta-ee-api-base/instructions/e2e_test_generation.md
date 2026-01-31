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
* テストフレームワーク: REST Assured を使用
* テスト対象: requirements/behaviors.md（E2Eテスト用）のシナリオ（Gherkin 記法で記述されている前提。@agent_skills/jakarta-ee-api-base/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照）
* 複数機能間の連携、実際のHTTPリクエスト/レスポンス、実際のDBアクセスを含む
* アプリケーションサーバーが起動している状態でテストを実行

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

E2Eテスト生成に必要なライブラリ（プロジェクトのビルド設定に合わせて追加）:

* REST Assured（rest-assured, json-path, xml-path）
* JUnit 5
* Jackson（JSON処理）

* E2Eテストクラスには `@Tag("e2e")` を付与し、通常の単体テスト実行から分離する

### 2.2 ベースクラスのポイント

* `@Tag("e2e")` の abstract ベースクラスを用意する
* @BeforeAll: architecture_design.md のベースURL・ポートに合わせ、RestAssured.baseURI/basePath と RequestSpecBuilder で Content-Type/Accept を設定
* 認証が必要なAPI向けに、ログインAPIを呼びトークン（cookie または header）を返す login(employeeCode, password) を用意する

---

## 3. E2Eテストケース生成

### 3.1 テストケース設計方針

* 1シナリオ＝1テストクラスの粒度
* basic_design/behaviors.md の各Given-When-Thenシナリオを実際のHTTPリクエストとしてテスト
* 複数APIにまたがるE2Eのフローをテスト
* 実際のDBアクセスを含む（テストデータの準備と検証）
* HTTPステータスコード、レスポンスボディ、ヘッダーの検証

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

---

## 7. 参考資料

* REST Assured公式ドキュメント: https://rest-assured.io/
* JUnit 5公式ドキュメント: https://junit.org/junit5/
* requirements/behaviors.md - E2Eテストシナリオ
* basic_design/functional_design.md - API仕様
* basic_design/architecture_design.md - システム構成
