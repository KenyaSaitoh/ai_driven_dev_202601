# 結合テスト生成インストラクション（アジャイル）

## パラメータ設定

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "ここにSPECディレクトリのパスを入力"
usecase_folder: null  # オプション。指定時はそのユースケースの結合テストのみ生成
```

* 例
```yaml
project_root: "projects/sdd-agile/bookstore/berry-books-api"
spec_directory: "projects/sdd-agile/bookstore/berry-books-api/specs/baseline"
```

注意: パス区切りはOS環境に応じて調整する。以降、`{project_root}`, `{spec_directory}` はパラメータで設定した値に置き換える。

---

## 概要

このインストラクションは、アジャイル版のSPECに基づき結合テスト（Integration Test）を生成するためのものである。

重要な方針
* テストフレームワーク: JUnit 5 + Cucumber（cucumber-junit-platform-engine）+ Weld SE（CDIコンテナ）
* テスト対象: usecases/{名}/behaviors.md のシナリオ（Gherkin記法）。common 用の振る舞いが common/behaviors.md 等で定義されていればそれも参照する
* Service層以下（Service + DAO + Entity）の実際の連携をテストする。外部APIは WireMock でスタブ化
* アプリケーションサーバーは不要（Weld SE で CDI コンテナを起動）

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

* usecases/{名}/behaviors.md の Gherkin シナリオを、**Cucumber の .feature ファイル**（`src/test/resources/features/integration` 配下）と **Cucumber ステップ定義**（Java、Weld SE を利用）に変換する
* Service層以下を実装で動かし、実際のDB（メモリDB）を使用する
* 外部API呼び出しは WireMock でスタブ化する（external_interface.md に従う）
* feature およびステップ定義に @Tag("integration") を付与し、プロジェクトの integrationTest タスクで実行されるようにする

---

## 3. 参考

* [e2e_test_generation.md](e2e_test_generation.md) - E2Eテスト生成
* ウォーターフォール版: @agent_skills/jakarta-ee-api-base/instructions/it_generation.md
