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
* テストフレームワーク: Playwright を使用
* テスト対象: basic_design/behaviors.md または requirements/behaviors.md（E2Eテスト用）のシナリオ（Gherkin 記法で記述されている前提。@agent_skills/struts-to-jsf-migration/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照）
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

### 2.1 依存関係

E2Eテスト生成に必要なライブラリ（プロジェクトのビルド設定に合わせて追加）:

* Playwright for Java (playwright)
* JUnit 5

* E2Eテストクラスには `@Tag("e2e")` を付与し、通常の単体テスト実行から分離する

### 2.2 ベースクラスのポイント

* `@Tag("e2e")` の abstract ベースクラスを用意する
* @BeforeAll: architecture_design.md のベースURLに合わせ、Playwright.create()、chromium().launch()（headless はプロパティで切り替え可能）
* @AfterAll: browser.close()、playwright.close()
* @BeforeEach: browser.newContext(baseURL, viewport)、context.newPage()。page.onDialog(dialog -> dialog.accept()) で確認ダイアログを自動受け入れ
* @AfterEach: context.close()
* 必要に応じて page.screenshot() でスクリーンショット保存を用意する

---

## 3. E2Eテストケース生成

### 3.1 テストケース設計方針

* 1シナリオ＝1テストクラスの粒度
* basic_design/behaviors.md の各Given-When-Thenシナリオを実際のブラウザ操作としてテスト
* 複数画面にまたがるエンドツーエンドのフローをテスト
* 実際のDBアクセスを含む（テストデータの準備と検証）
* 画面遷移、ボタンクリック、フォーム入力、バリデーション、エラーメッセージの検証

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

---

## 7. 参考資料

* Playwright for Java公式ドキュメント: https://playwright.dev/java/
* JUnit 5公式ドキュメント: https://junit.org/junit5/
* requirements/behaviors.md - E2Eテストシナリオ
* basic_design/screen_design.md - 画面設計
* basic_design/functional_design.md - 機能仕様
* basic_design/architecture_design.md - システム構成
