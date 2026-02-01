# Changelog

## 2026/2/1

### Changed

#### 振舞仕様書（Gherkin）からのテスト生成を Cucumber ベースに統一
* agent_skills/jakarta-ee-api-agile: `code_generation.md` の単体テスト生成を、behaviors.md の Gherkin から **Cucumber .feature**（`src/test/resources/features/unit`）と **Cucumber ステップ定義**（Java）に変換する方針に変更。JUnit 5 + Cucumber（cucumber-junit-platform-engine）を明記
* agent_skills/jakarta-ee-api-agile: `it_generation.md` を、結合テストを Cucumber .feature（`features/integration`）＋ステップ定義（Weld SE）に変換する旨に変更。`@Tag("integration")` を記載
* agent_skills/jakarta-ee-api-agile: `e2e_test_generation.md` を、E2E を Cucumber .feature（`features/e2e`）＋ステップ定義（REST Assured）に変換する旨に変更。`@Tag("e2e")` を記載
* agent_skills/jakarta-ee-api-base: `code_generation.md` の単体テスト生成ガイドラインを、detailed_design/behaviors.md の Gherkin から Cucumber .feature とステップ定義を生成する形に変更。`it_generation.md`・`e2e_test_generation.md` も同様に Cucumber .feature＋ステップ定義に統一
* agent_skills/struts-to-jsf-migration: `code_generation.md`・`it_generation.md`・`e2e_test_generation.md` を、behaviors.md から Cucumber .feature と Cucumber ステップ定義を生成する旨に変更（単体: features/unit、結合: features/integration、E2E: features/e2e）。E2E は Playwright をステップ定義で利用

#### agent_skills（jakarta-ee-api-base / struts-to-jsf-migration）の簡略化・7段階化
* agent_skills/jakarta-ee-api-base: プロセスを6段階から7段階に変更（結合テスト生成・E2Eテスト生成を独立ステップに）。`it_generation.md`・`e2e_test_generation.md` から GitHub Actions・Gradle タスク固有の記述を削除し、依存・設定・テストテンプレートを抽象化。実行・レポート・CI は「注意事項」「参照資料」に集約
* agent_skills/jakarta-ee-api-base: `code_generation.md` の単体テスト例を簡略化（モック/実インスタンスの扱いを要約）。進捗・完了条件・コンポーネント参照を `principles/architecture.md` および表形式に整理
* agent_skills/jakarta-ee-api-base: `detailed_design.md` の非機能要件の長文を削除し、`principles/architecture.md` 参照に変更。SPEC 優先順位の記載を修正
* agent_skills/jakarta-ee-api-base: `principles/architecture.md` に「11.5 非機能要件の確認原則（詳細設計時）」を追加。セキュリティ・性能・データ整合性など SPEC にない項目はユーザー確認とする旨を明記
* agent_skills/jakarta-ee-api-base: SKILL.md・README.md のクイックスタートから Gradle 実行コマンドを削除し、プロジェクトのビルド設定に従う旨に変更。ディレクトリ構成を現状に合わせて更新
* agent_skills/struts-to-jsf-migration: 上記と同様に 7 段階化、`it_generation.md`・`e2e_test_generation.md`・`code_generation.md`・`detailed_design.md` の簡略化・architecture 参照への統一を実施
* agent_skills/struts-to-jsf-migration: `principles/architecture.md` に「11.5 非機能要件の確認原則」を追加（ViewScoped・Flash Scope・Session Scope 等の JSF 向け確認ポイントを含む）
* agent_skills/struts-to-jsf-migration: SKILL.md・README.md を 7 段階・構成更新に合わせて修正。README の「JSFコード生成」参照先を `jakarta-ee-api-base/instructions/code_generation.md` から `struts-to-jsf-migration/instructions/code_generation.md` に修正

#### agent_skills（jakarta-ee-api-agile）
* 「common SPEC」を「業務共通SPEC」に表記統一（SKILL.md, README.md, instructions, principles）
* 合流ポイントを「実装タスク一覧」から「結合テスト」に変更。タスクは業務共通SPEC・各ユースケースSPECで既に決まっており、各人がそれに従って実装する前提を明記
* タスク分解を廃止。code_generation の入力を task_file から target（common または usecases/{名}）に変更し、SPEC を直接参照してコード生成するように変更
* Markdown の `**...**`（太字）を廃止。principles/common_rules.md の「ボールドは使用しない」に合わせてスキル全体で通常表記に統一
* 基本設計SPECの変更管理を行わない方針に統一。SPEC を編集したうえで code_generation を target 指定で再実行し、既存コードへ差分を反映する運用に変更
* instructions/code_generation.md: アジャイルにおける位置づけを追記。何度でも再実行し、既存コードに対して SPEC に基づく差分を漸進的に反映する前提を明記。既存コードがある場合は差分のみ反映し、他 target のコードは破壊しない旨を追加

#### プロジェクト構造・ビルド（sdd-wf / sdd-agile / sandbox、Gradle、ルート README）
* フォルダ `projects/sdd` を `projects/sdd-wf` にリネームしたことに伴い、`settings.gradle`、ルート `README.md`、`CHANGELOG.md`、`agent_skills`（jakarta-ee-api-base / struts-to-jsf-migration）の instructions・SKILL・README、`docs/`、`projects/master`・`projects/sandbox`・`projects/sdd-wf`・`projects/sdd-agile` 配下の README・test_script 等のパスを `projects/sdd-wf` に統一
* ルート `README.md`: プロジェクトカテゴリを 4 つに再構成（master / sandbox / sdd-wf / sdd-agile）。master を「模範解答・手を入れない想定」、sandbox を「トライアル用（プロンプトエンジニアリング等）」と明記。ディレクトリ構造ツリーに sandbox と sdd-agile を追加
* ルート `README.md`: クイックスタートに master 用・sandbox 用の実行方法を分けて記載。代表プロジェクト・SPA の説明に sandbox を追加。SDD クリーン手順に sdd-agile 用コマンドと `cleanupAllSddProjects` を追記
* `settings.gradle`: Master を「模範解答・手を入れない想定」、SDD を sdd-wf（ウォーターフォール）と sdd-agile（アジャイル）に分けてコメント整理。sdd-agile 用に `berry-books-api-sdd-agile`・`back-office-api-sdd-agile` の projectDir を追加。sandbox 用に `berry-books-api-sandbox`・`back-office-api-sandbox`・`customer-hub-api-sandbox` の projectDir を追加
* `build.gradle`: JWT/BCrypt および REST Assured の対象に `*-sdd-agile`・`*-sandbox` を追加。sddProjects と cleanupAllSddProjects に sdd-agile の 2 プロジェクトを追加

#### 各プロジェクト配下（README・スクリプト・vite）
* `projects/sdd-agile/bookstore/` 配下: README（back-office-api-sdd, berry-books-api-sdd）に sdd-agile 用の注記・Gradle 名（-agile）・コンテキストルートを追記。Gradle コマンド例・URL を -agile に統一。test_script/README・test_images.sh・run-bookstore-all.sh・test_all.sh・specs/baseline/basic_design/behaviors.md のパス・Gradle 名・API_BASE を sdd-agile 用に更新
* `projects/sandbox/bookstore/`: README を「Sandbox（プロンプトエンジニアリング用）」に変更し、Gradle 名（-sandbox）・コンテキストルート・コマンド例・URL を -sandbox に統一。customer-hub-swing は sandbox に含まない旨を記載。run-bookstore-all.sh の Gradle タスクと SPA パスを -sandbox 用に変更。run-bookstore-spa.sh のパスを `projects/sandbox/bookstore/` に変更
* `run-berry-books-all.sh` を `run-bookstore-all.sh` にリネームし、`projects/master/bookstore/` に移動
* `restart-berry-books-spa.sh` を `run-bookstore-spa.sh` にリネームし、`projects/master/bookstore/` に移動
* ルート直下の `README.md` のクイックスタートセクションを更新（スクリプト名・実行パス・Bookstore表記に統一）
* `projects/master/bookstore/README.md` にフルスタック自動起動（`run-bookstore-all.sh` / `run-bookstore-spa.sh`）の説明を追加
* 各スクリプトは `projects/master/bookstore` から実行し、プロジェクトルートを相対パスで参照するように変更
* `projects/master/bookstore/back-office-spa/vite.config.ts` のプロキシ設定を変更: `back-office-api` → `back-office-api-sdd`
* `projects/master/bookstore/berry-books-spa/vite.config.ts` のプロキシ設定を変更: `berry-books-api` → `berry-books-api-sdd`

#### code_generation の実行順序・完了条件
* `agent_skills/jakarta-ee-api-base/instructions/code_generation.md` を修正: 本番コード生成後に単体テスト生成まで確実に完了するよう実行順序と完了条件を明確化
* `agent_skills/struts-to-jsf-migration/instructions/code_generation.md` を修正: 本番コード生成後に単体テスト生成まで確実に完了するよう実行順序と完了条件を明確化

### Fixed

#### ドキュメント
* `agent_skills/jakarta-ee-api-base/instructions/code_generation.md` の番号重複を修正（「11. 静的リソース」を「14.」に修正）

### Added

#### Cucumber（Gherkin / BDD）のテスト依存関係
* `build.gradle`: 全 Java 系プロジェクト（customer-hub-swing を除く）のテスト依存に Cucumber を追加。`io.cucumber:cucumber-java:7.33.0` と `io.cucumber:cucumber-junit-platform-engine:7.33.0` を testImplementation で指定。振舞仕様書（behaviors.md）の Gherkin から .feature とステップ定義でテストを実行する前提とする

#### Gradle ビルドへの組み込み
* アジャイル向け仕様駆動開発用に `projects/sdd-agile`（bookstore）を Gradle ビルドに組み込み: `settings.gradle` に `berry-books-api-sdd-agile`・`back-office-api-sdd-agile` を追加（projectDir は `projects/sdd-agile/bookstore/berry-books-api-sdd` 等）
* プロンプトエンジニアリング等のトライアル用に `projects/sandbox`（bookstore、master からコピーした完成版）を Gradle ビルドに組み込み: `settings.gradle` に `berry-books-api-sandbox`・`back-office-api-sandbox`・`customer-hub-api-sandbox` を追加（projectDir は `projects/sandbox/bookstore/berry-books-api` 等）

#### 起動スクリプト
* `projects/master/person/jsf-person/run-jsf-person-all.sh` を追加: JSF Person アプリケーションの一括起動スクリプト
* `projects/master/person/struts-person/run-struts-person-all.sh` を追加: Struts Person アプリケーションの一括起動スクリプト
* `projects/sdd-wf/bookstore/run-bookstore-all.sh` を追加: SDD版Bookstoreフルスタック一括起動スクリプト（back-office-api-sdd、berry-books-api-sdd、customer-hub-api + 3つのSPA）
* `projects/sdd-wf/bookstore/run-bookstore-spa.sh` を追加: SDD版Bookstore SPA再起動スクリプト
* `projects/sdd-wf/person/jsf-person-sdd/run-jsf-person-all.sh` を追加: SDD版JSF Personアプリケーションの一括起動スクリプト
