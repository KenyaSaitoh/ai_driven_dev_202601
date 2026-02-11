# Changelog

## 2026/2/10

### Added

#### DBUnitテストフレームワークの導入
* build.gradle: 全Javaプロジェクトのテスト依存関係に `org.dbunit:dbunit:2.7.3` を追加。データベーステストにおける初期状態の準備と期待値の検証をDBUnitで実施する前提とする

#### DBUnitによるデータベーステストの組み込み（agent_skills）
* agent_skills/jakarta-ee-api-agile/instructions/unit_test_generation.md: DaoレイヤーのテストでDBUnitを推奨。JdbcDatabaseTesterを使用したデータ駆動テストの実装例を追加
* agent_skills/jakarta-ee-api-agile/instructions/it_generation.md: 結合テストでDBUnitを必須化。BaseIntegrationTestへのDBUnit統合（setUpAll, tearDownAll, setUp, tearDown, setupDatabaseTester, loadDataSet, getDatabaseTable, assertDatabaseState メソッド）、XMLデータセットの使用パターン、ベストプラクティスを追加
* agent_skills/jakarta-ee-api-base/instructions/unit_test_generation.md: DaoレイヤーのテストでDBUnitを推奨（モックEntityManagerを使用したJdbcDatabaseTesterの実装例を追加）
* agent_skills/jakarta-ee-api-base/instructions/it_generation.md: 結合テストでDBUnitを必須化。BaseIntegrationTestへのDBUnit統合、データセット管理、テストデータ準備の詳細手順を追加。参考資料にDBUnit公式リンクを追加
* agent_skills/struts-to-jsf-migration/instructions/unit_test_generation.md: ServiceレイヤーのテストでDBUnitを推奨（データアクセスを伴う場合）
* agent_skills/struts-to-jsf-migration/instructions/it_generation.md: 結合テストでDBUnitを必須化。BaseIntegrationTestへのDBUnit統合、データセット管理の詳細を追加

### Changed

#### 振る舞い仕様書テンプレートのDBUnit対応
* agent_skills/jakarta-ee-api-base/templates/basic_design/behaviors.md: Gherkinシナリオに詳細なDB状態管理を追加。Given句で初期DB状態（テーブル名、件数、データセットパス、データ）を明記、Then句で期待DB状態（更新後のデータ、検証条件：PK自動採番、FK制約、CASCADE削除、楽観的ロックのVERSION、トランザクションのコミット/ロールバック）を明記。CREATE/UPDATE/DELETE/READ/Transactionの各パターン例、DBUnitデータセット対応表、DBUnit参考資料リンクを追加
* agent_skills/struts-to-jsf-migration/templates/basic_design/behaviors.md: 上記と同様にDB状態管理をGherkinシナリオに追加（E2E重視の構成に適合）

#### プロジェクトの振る舞い仕様書のDBUnit対応（12ファイル）
* projects/sdd-agile/bookstore/back-office-api/specs/baseline/usecases: stocks/behaviors.md（在庫管理：READ + UPDATE + 楽観的ロック）、workflow/behaviors.md（ワークフロー管理：状態遷移 CREATED→APPLIED→APPROVED）、publisher/behaviors.md（出版社一覧：READ）、category/behaviors.md（カテゴリ一覧：READ）にDBUnit対応を追加
* projects/sdd-agile/bookstore/berry-books-api/specs/baseline/usecases: orders/behaviors.md（注文管理：CREATE + ロールバック + 外部API連携）、auth/behaviors.md（認証管理：外部API連携のみ、DB操作なし）、books/behaviors.md（書籍管理：外部API連携のみ、DB操作なし）にDBUnit対応を追加
* projects/sdd-wf/bookstore/back-office-api/specs/baseline/basic_design: books/behaviors.md（書籍管理：READ + リレーション）、stocks/behaviors.md（在庫管理：UPDATE + 楽観的ロック + 外部API連携）、workflows/behaviors.md（ワークフロー管理：状態遷移 + 却下）、common/behaviors.md（共通ドメイン：認証 + DAO + リレーション）にDBUnit対応を追加
* projects/sdd-wf/bookstore/berry-books-api/specs/baseline/basic_design: books_proxy/behaviors.md（書籍API連携：外部API連携のみ、DB操作なし）にDBUnit対応を追加
* 全振る舞い仕様書に以下を追加: Given句での初期DB状態の明記（テーブル、件数、データセットパス）、Then句での期待DB状態の明記（更新結果、検証条件）、READ操作での「DBの状態は変化しない」の明記、トランザクション制御（コミット/ロールバック）の検証、DBUnitデータセット対応表、DBUnit/WireMock参考資料リンク

## 2026/2/11

### Added

#### Playwright E2Eテストの自動生成
* projects/master/bookstore/berry-books-spa: Playwright E2Eテストコードを自動生成（5つのテストシナリオ、8つのPage Objectクラス、playwright.config.ts、README_PLAYWRIGHT.md）
* テストシナリオ定義書（playwright_berry-books.md）から、セレクタの自動推論とPage Object Modelパターンによるテストコードを生成

### Changed

#### SPAのREADME簡素化
* 全SPAのREADME（master/sandbox/sdd-wf/sdd-agile bookstore配下）: Java/Gradle/Payara/HSQLDBの詳細な起動手順を削除し、フロントエンド開発に集中した内容に変更。バックエンドAPIの起動方法は各プロジェクトのルートREADME.mdを参照するよう案内

#### READMEの表現統一
* projects/master/bookstore/README.md: 「研修開催につき最後に1回だけ実行」→「プロジェクトを終了するときに実行」に変更し、表現を統一
* projects/master/bookstore/README.md: 全Gradleコマンドブロックから冗長な「# プロジェクトルートで実行」コメントを削除（84行目に明記済み）

#### 起動スクリプトのログイン情報表示
* 全プロジェクト（master / sandbox / sdd-wf / sdd-agile）の `run-bookstore-all.sh` にログイン情報セクションを追加: berry-books-spa（alice@example.com）および back-office-spa（E00001）のログイン認証情報を起動完了時にコンソールに表示

#### 要件定義書のEARS記法への統合
* projects/sdd-wf/bookstore/back-office-api/specs/baseline/requirements: requirements.mdをEARS記法（Event-driven, Unwanted behavior, Optional features, State-driven）に変換。behaviors.mdの受入基準を統合し、behaviors.mdを削除
* projects/sdd-wf/bookstore/berry-books-api/specs/baseline/requirements: requirements.mdをEARS記法に変換。認証API、書籍API、注文API、画像APIの要件をWHEN/IF-THEN/WHERE/WHILEパターンで記述。behaviors.mdを削除
* projects/sdd-wf/person/jsf-person/specs/baseline/requirements: requirements.mdをEARS記法に変換。PERSON一覧表示、追加、編集、削除の機能要件をEARSパターンで記述。behaviors.mdを削除
* 全requirements.md: 機能要件ID（FR-XXX-001など）に一意識別子を付与し、トレーサビリティを確保。太字表記（**による）を削除し、プレーンテキストに統一

## 2026/2/5

### Changed

#### タスクベースからドメインベースの開発への移行
* agent_skills/jakarta-ee-api-base: 開発プロセスを7段階から6段階に変更（タスク分解ステップを削除）。基本設計から直接ドメイン単位で詳細設計・コード生成を実行する方式に変更
* プロジェクトREADME.md（back-office-api, berry-books-api）: 7段階プロセス→6段階プロセスに更新。ステップ2（タスク分解）を削除し、各ステップをドメイン単位の実行に変更。target_type/task_file → target_domain にパラメータ名を変更。プロジェクト名の -wf サフィックスを削除（back-office-api-wf → back-office-api）
* プロジェクトREADME.md: ドメイン構成セクションを追加（back-office-api: common, books, categories, publishers, stocks, workflows / berry-books-api: common, books_proxy, orders, images）
* プロジェクトREADME.md: プロジェクト構成セクションをドメインベースのディレクトリ構造に更新（tasks/ および FUNC_XXX フォルダを削除、basic_design/ および detailed_design/ 配下にドメインフォルダを追加）
* agent_skills/jakarta-ee-api-base/instructions/basic_design_change.md: 変更タスクファイル（tasks/change_tasks.md）の生成から、影響を受けるドメインの識別に変更
* agent_skills/jakarta-ee-api-base/SKILL.md: 6段階プロセス、ドメイン単位の実行、target_domain パラメータ、既存ファイルへの差分反映を反映。実践例のプロジェクト名を back-office-api-sdd → back-office-api に修正

#### 詳細設計書の簡潔化
* agent_skills/jakarta-ee-api-base/instructions/detailed_design.md: 1112行→約150行に削減（約87%削減）。「詳細設計書は基本設計とコードの橋渡しとなる設計判断のみを簡潔に記載」する原則を明記。すべてのコード例を削除。テンプレート例のセクション（### 3.2, 3.3 等）を削除。箇条書き中心の記述に変更
* agent_skills/struts-to-jsf-migration/instructions/detailed_design.md: 657行→約160行に削減（約76%削減）。コード例を削除し、箇条書き中心に変更
* 詳細設計で記載すべき情報を明確化: クラス名と責務（1行）、主要メソッドのシグネチャ、設計判断を示すアノテーション、JPQLクエリ、依存関係
* 詳細設計で記載すべきでない情報を明確化: メソッドの実装詳細、すべてのフィールド定義、getter/setter、バリデーションの詳細、基本設計SPECの繰り返し、コード例

#### 既存ファイルへの差分反映の原則を追加
* agent_skills/jakarta-ee-api-base: 全指示書（basic_design.md, detailed_design.md, code_generation.md, it_generation.md, e2e_test_generation.md）に「既存ファイルがある場合は、削除せずに読み込んで、差分のみを反映する」という方針を追加
* 既存の成果物を尊重し、ファイルをゼロから作り直すことを禁止。必要な変更のみを適用する方式に統一

#### テンプレートの更新
* agent_skills/jakarta-ee-api-base/templates/basic_design/functional_design.md: システム全体の機能設計書からドメイン単位の機能設計書に変更。ドメインの責務、依存関係、提供する機能を記載する形式に更新
* agent_skills/jakarta-ee-api-base/templates/basic_design/CHANGES_template.md: タスクベースからドメインベースの変更管理に更新。影響を受けるドメインを識別し、ドメイン単位でのファイル変更を記載する形式に変更
* agent_skills/jakarta-ee-api-base/templates/basic_design/behaviors.md: システム全体からドメイン単位の結合テスト用振る舞い仕様書に更新。ドメイン内のService層以下の連携テストシナリオを記載
* agent_skills/jakarta-ee-api-base/templates/detailed_design/detailed_design.md: ドメイン単位の簡潔な詳細設計書テンプレートに更新。クラス構成、コンポーネント設計、DTO設計、エンティティ設計等を最小限の情報で記載
* agent_skills/jakarta-ee-api-base/templates/detailed_design/behaviors.md: ドメイン粒度の単体テスト用振る舞い仕様書に更新。ドメイン内のコンポーネント間は実際の連携をテスト、ドメイン外の依存はモック化
* agent_skills/jakarta-ee-api-base/templates/requirements/behaviors.md: リンク構造をドメインベースに更新。basic_design/{domain}/behaviors.md へのリンクを修正

#### 太字表記の削除
* agent_skills/jakarta-ee-api-base/instructions: 全ファイル（detailed_design.md, code_generation.md, it_generation.md, e2e_test_generation.md, basic_design.md, unit_test_execution.md）から `**` による太字表記を削除（計70箇所）

#### Gherkin記法の明確化
* agent_skills/jakarta-ee-api-base/instructions/detailed_design.md: behaviors.md が Gherkin 記法で記述されること、ブラックボックステストであることを明記。Gherkin記法の詳細は principles/common_rules.md を参照することを追加
* agent_skills/struts-to-jsf-migration/instructions/detailed_design.md: 同様にGherkin記法、ブラックボックステストを明記

### Removed

#### タスク関連ファイル・フォルダの削除
* projects/sdd-wf/bookstore/back-office-api/tasks フォルダを削除（ドメインベース開発では不要）
* projects/sdd-wf/bookstore/berry-books-api/tasks フォルダを削除（ドメインベース開発では不要）
* プロジェクトREADME.md: tasks/ フォルダ、tasks/tasks.md、FUNC_XXX ディレクトリへの参照をすべて削除

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
