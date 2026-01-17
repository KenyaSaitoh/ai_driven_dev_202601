# セットアップタスク

担当者: 全員  
推奨スキル: Gradle, Jakarta EE, HSQLDB  
想定工数: 2時間  
依存タスク: なし

---

## 概要

プロジェクトの初期設定を実施する。開発環境のセットアップ、データベーススキーマの作成、設定ファイルの配置、静的リソースの準備を行う。

---

## タスクリスト

### T_SETUP_001: プロジェクト構造の確認

* 目的: プロジェクトディレクトリ構造が正しく存在することを確認する
* 対象: プロジェクトルート、src/main/java、src/main/resources、src/main/webapp
* 参照SPEC: [README.md](../README.md) の「プロジェクト構成」
* 注意事項: ディレクトリが不足している場合は作成する

---

### T_SETUP_002: build.gradle の確認

* 目的: 依存関係とビルド設定が正しく定義されていることを確認する
* 対象: build.gradle
* 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「1. 技術スタック」
* 注意事項: Jakarta EE 10、JPA 3.1、JAX-RS 3.1、jjwt 0.12.6、BCrypt、JUnit 5、Mockitoが含まれていること

---

### T_SETUP_003: データベーススキーマの作成

* 目的: ORDER_TRAN、ORDER_DETAIL テーブルを作成する
* 対象: src/main/resources/db/schema.sql
* 参照SPEC: [data_model.md](../specs/baseline/system/data_model.md) の「3. テーブル定義」
* 注意事項: 注文関連テーブルのみ作成。書籍・在庫・カテゴリ・顧客テーブルは外部APIが管理

---

### T_SETUP_004: サンプルデータの準備

* 目的: テスト用の初期データを準備する
* 対象: src/main/resources/db/sample_data.sql
* 参照SPEC: [data_model.md](../specs/baseline/system/data_model.md)
* 注意事項: 注文データのサンプル（任意）。書籍・顧客データは外部APIが管理

---

### T_SETUP_005: persistence.xml の作成

* 目的: JPA設定ファイルを作成する
* 対象: src/main/resources/META-INF/persistence.xml
* 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「10.1 永続化構成」
* 注意事項: JNDI名は`jdbc/HsqldbDS`、スキーマ生成は`none`に設定

---

### T_SETUP_006: MicroProfile Config設定ファイルの作成

* 目的: JWT設定、外部API URLを定義する
* 対象: src/main/resources/META-INF/microprofile-config.properties
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「6.2 JWT設定」
  * [external_interface.md](../specs/baseline/system/external_interface.md) の「9.1 設定ファイル」
* 注意事項: JWT秘密鍵、有効期限、Cookie名、外部API URLを設定

---

### T_SETUP_007: beans.xml の作成

* 目的: CDIコンテナを有効化する
* 対象: src/main/webapp/WEB-INF/beans.xml
* 参照SPEC: [external_interface.md](../specs/baseline/system/external_interface.md) の「10. CDI設定」
* 注意事項: bean-discovery-mode="all"を設定

---

### T_SETUP_008: web.xml の作成

* 目的: Servletコンテナ設定を定義する
* 対象: src/main/webapp/WEB-INF/web.xml
* 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md)
* 注意事項: JAX-RS Applicationの設定、ウェルカムページの設定

---

### T_SETUP_009: Log4j2設定ファイルの作成

* 目的: ログ出力設定を定義する
* 対象: src/main/resources/log4j2.xml
* 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「11. ログ戦略」
* 注意事項: SLF4J + Log4j2を使用、INFO/WARN/ERROR/DEBUGレベル設定

---

### T_SETUP_010: メッセージプロパティファイルの作成

* 目的: エラーメッセージの外部化
* 対象: src/main/resources/messages.properties
* 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md)
* 注意事項: エラーメッセージ、バリデーションメッセージを定義

---

### T_SETUP_011: 静的リソースの配置

* 目的: 書籍表紙画像とフォールバック画像を配置する
* 対象: src/main/webapp/resources/images/covers/
* 参照SPEC: [API_004_images/functional_design.md](../specs/baseline/api/API_004_images/functional_design.md) の「4. ファイル命名規則」
* 注意事項: no-image.jpg（フォールバック画像）は必須。書籍画像は{bookId}.jpg形式

---

### T_SETUP_012: HSQLDBデータベースの初期化

* 目的: データベースを作成し、スキーマとサンプルデータを投入する
* 対象: Gradleタスク実行（setupHsqldb）
* 参照SPEC: [README.md](../README.md) の「④ プロジェクトを開始するときに1回だけ実行」
* 注意事項: `./gradlew :berry-books-api-sdd:setupHsqldb`を実行

---

### T_SETUP_013: プロジェクトのビルド確認

* 目的: プロジェクトが正常にビルドできることを確認する
* 対象: Gradleタスク実行（war）
* 参照SPEC: [README.md](../README.md)
* 注意事項: `./gradlew :berry-books-api-sdd:war`を実行してビルド成功を確認

---

## 完了基準

* [ ] プロジェクト構造が正しく存在する
* [ ] build.gradleに必要な依存関係が定義されている
* [ ] データベーススキーマが作成されている
* [ ] persistence.xmlが作成されている
* [ ] microprofile-config.propertiesが作成されている
* [ ] beans.xmlが作成されている
* [ ] web.xmlが作成されている
* [ ] log4j2.xmlが作成されている
* [ ] messages.propertiesが作成されている
* [ ] 静的リソース（画像）が配置されている
* [ ] HSQLDBデータベースが初期化されている
* [ ] プロジェクトが正常にビルドできる

---

## 参考資料

* [README.md](../README.md) - プロジェクトREADME
* [architecture_design.md](../specs/baseline/system/architecture_design.md) - アーキテクチャ設計書
* [data_model.md](../specs/baseline/system/data_model.md) - データモデル仕様書
* [external_interface.md](../specs/baseline/system/external_interface.md) - 外部インターフェース仕様書
