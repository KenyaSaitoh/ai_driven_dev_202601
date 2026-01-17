# berry-books-api-sdd - セットアップタスク

担当者: 全員（プロジェクト開始時に1回のみ実行）  
推奨スキル: Gradle、Payara Server、HSQLDB、Jakarta EE  
想定工数: 2時間  
依存タスク: なし

---

## 概要

本タスクは、berry-books-api-sddプロジェクトの初期セットアップを行う。全員が実行前に1回だけ実行する。

---

## タスクリスト

### T_SETUP_001: プロジェクト構造の確認

* [X] T_SETUP_001: プロジェクト構造の確認
  * 目的: プロジェクトディレクトリ構造を確認する
  * 対象: プロジェクトルートディレクトリ
  * 参照SPEC: [README.md](../README.md) の「プロジェクト構成」
  * 注意事項: src/main/java、src/main/resources、src/test/javaが存在することを確認

---

### T_SETUP_002: 依存関係の確認

* [SKIPPED] T_SETUP_002: 依存関係の確認
  * 目的: 外部サービスの起動状態を確認する
  * 対象: HSQLDB、Payara Server、back-office-api、customer-hub-api
  * 参照SPEC: [README.md](../README.md) の「前提条件」
  * 注意事項: 以下のサービスが起動していること
    * HSQLDBサーバー（ルートREADME参照: `./gradlew startHsqldb`）
    * Payara Server（ルートREADME参照: `./gradlew startPayara`）
    * back-office-api（http://localhost:8080/back-office-api-sdd/api）
    * customer-hub-api（http://localhost:8080/customer-hub-api/api/customers）
  * SKIPPED: Infrastructure setup (skip_infrastructure: true)

---

### T_SETUP_003: データベース初期化

* [READY] T_SETUP_003: データベース初期化
  * 目的: データベーステーブルとサンプルデータを作成する
  * 対象: ORDER_TRAN、ORDER_DETAILテーブル
  * 参照SPEC: [data_model.md](../specs/baseline/basic_design/data_model.md) の「3. テーブル定義」
  * 注意事項: 以下のコマンドを実行
    ```bash
    ./gradlew :berry-books-api-sdd:setupHsqldb
    ```
  * 実行されるファイル:
    * `sql/hsqldb/01_schema.sql`（テーブル作成）
    * `sql/hsqldb/02_sample_data.sql`（サンプルデータ）
  * READY: SQL files created, execute when HSQLDB is running

---

### T_SETUP_004: プロジェクトビルド

* [X] T_SETUP_004: プロジェクトビルド
  * 目的: プロジェクトをビルドしてWARファイルを生成する
  * 対象: berry-books-api-sdd.war
  * 参照SPEC: [README.md](../README.md) の「④ プロジェクトを開始するときに1回だけ実行」
  * 注意事項: 以下のコマンドを実行
    ```bash
    ./gradlew :berry-books-api-sdd:war
    ```
  * COMPLETED: WAR file built successfully

---

### T_SETUP_005: アプリケーションデプロイ

* [READY] T_SETUP_005: アプリケーションデプロイ
  * 目的: WARファイルをPayara Serverにデプロイする
  * 対象: Payara Server
  * 参照SPEC: [README.md](../README.md) の「④ プロジェクトを開始するときに1回だけ実行」
  * 注意事項: 以下のコマンドを実行
    ```bash
    ./gradlew :berry-books-api-sdd:deploy
    ```
  * READY: Execute when Payara Server is running

---

### T_SETUP_006: 動作確認

* [READY] T_SETUP_006: 動作確認
  * 目的: アプリケーションが正常に起動したことを確認する
  * 対象: ウェルカムページ、API
  * 参照SPEC: [README.md](../README.md) の「📍 APIエンドポイント」
  * 注意事項: 以下のURLにアクセスして確認
    * ウェルカムページ: http://localhost:8080/berry-books-api-sdd/
    * 書籍一覧API: http://localhost:8080/berry-books-api-sdd/api/books
  * READY: Execute after deployment

---

## 成果物チェックリスト

* [X] プロジェクト構造を理解している
* [READY] 外部サービス（HSQLDB、Payara、back-office-api、customer-hub-api）が起動している
* [READY] データベーステーブルが作成されている (SQL files ready)
* [X] WARファイルが生成されている
* [READY] アプリケーションがPayara Serverにデプロイされている (ready to deploy)
* [READY] ウェルカムページにアクセスできる (after deployment)
* [READY] 書籍一覧APIが正常にレスポンスを返す (after deployment + code implementation)

---

## 参考資料

* [README.md](../README.md) - プロジェクトREADME
* [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) - アーキテクチャ設計書
* [data_model.md](../specs/baseline/basic_design/data_model.md) - データモデル仕様書
* ルートREADME - HSQLDBとPayara Serverの起動手順
