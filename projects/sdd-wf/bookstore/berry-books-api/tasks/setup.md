# setup - プロジェクト初期化タスク

## メタデータ

* タスクID: setup
* 依存タスク: なし
* 並行実行可能: なし
* 担当者: 全員（プロジェクト開始時に1回のみ実行）
* 推奨スキル: Jakarta EE, Gradle, HSQLDB, Payara Server
* 想定工数: 4時間

## 実装内容

プロジェクト開始時の初期化作業を実施する。全ての機能タスクはこのsetupタスクが完了してから開始できる。

---

## タスクリスト

### 1. プロジェクト構造の作成

* [ ] T_SETUP_001: Gradleプロジェクト初期化
  * 目的: ビルド環境を構築する
  * 対象: build.gradle, settings.gradle, gradle.properties
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「1. バックエンドサービスアーキテクチャ」
  * 注意事項: Jakarta EE 10、Payara Server 6.x対応

* [ ] T_SETUP_002: パッケージ構造の作成
  * 目的: レイヤードアーキテクチャに従ったパッケージ構造を作成する
  * 対象: pro.kensait.berrybooks配下のパッケージ
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「4. パッケージ構造」
  * 注意事項: api, security, service, dao, entity, external, common, util パッケージを作成

### 2. 依存関係の設定

* [ ] T_SETUP_003: Jakarta EE依存関係の追加
  * 目的: Jakarta EE 10の各種仕様を使用可能にする
  * 対象: build.gradle
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「1.1 アーキテクチャパターン」
  * 注意事項: 
    * jakarta.platform:jakarta.jakartaee-api:10.0.0
    * jakarta.persistence:jakarta.persistence-api:3.1.0
    * jakarta.ws.rs:jakarta.ws.rs-api:3.1.0
    * jakarta.inject:jakarta.inject-api:2.0.1

* [ ] T_SETUP_004: JWTライブラリの追加
  * 目的: JWT認証機能を実装するためのライブラリを追加する
  * 対象: build.gradle
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「2.2 コンポーネントの責務」
  * 注意事項: io.jsonwebtoken:jjwt-api, jjwt-impl, jjwt-jackson

* [ ] T_SETUP_005: BCryptライブラリの追加
  * 目的: パスワードハッシュ化機能を追加する
  * 対象: build.gradle
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「3. customer-hub-api連携」
  * 注意事項: org.mindrot:jbcrypt

* [ ] T_SETUP_006: ログライブラリの追加
  * 目的: SLF4J + Log4j2によるログ出力環境を構築する
  * 対象: build.gradle
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「2.2 コンポーネントの責務」
  * 注意事項: org.slf4j:slf4j-api, org.apache.logging.log4j

* [ ] T_SETUP_007: テストライブラリの追加
  * 目的: JUnit 5、Mockito等のテスト環境を構築する
  * 対象: build.gradle
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「1.3 追加ライブラリ」
  * 注意事項: JUnit 5、Mockito、REST Assured、WireMock、Weld SE

### 3. 設定ファイルの作成

* [ ] T_SETUP_008: persistence.xmlの作成
  * 目的: JPA設定ファイルを作成する
  * 対象: src/main/resources/META-INF/persistence.xml
  * 参照SPEC: [data_model.md](../specs/baseline/basic_design/data_model.md) の「1. 概要」
  * 注意事項: 
    * データソースJNDI名: jdbc/berrybooks
    * トランザクションタイプ: JTA
    * スキーマ自動生成は無効化

* [ ] T_SETUP_009: beans.xmlの作成
  * 目的: CDI設定ファイルを作成する
  * 対象: src/main/webapp/WEB-INF/beans.xml
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「10. CDI設定」
  * 注意事項: bean-discovery-mode="all"

* [ ] T_SETUP_010: web.xmlの作成
  * 目的: Servlet設定ファイルを作成する
  * 対象: src/main/webapp/WEB-INF/web.xml
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「2.1 システム構成図」
  * 注意事項: JAX-RS Application、セッションタイムアウト設定

* [ ] T_SETUP_011: microprofile-config.propertiesの作成
  * 目的: MicroProfile Config設定ファイルを作成する
  * 対象: src/main/resources/META-INF/microprofile-config.properties
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「9. 設定管理」
  * 注意事項: 
    * customer-hub-api.base-url
    * back-office-api.base-url
    * jwt.secret
    * jwt.expiration

* [ ] T_SETUP_012: log4j2.xmlの作成
  * 目的: Log4j2設定ファイルを作成する
  * 対象: src/main/resources/log4j2.xml
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「4.2 ログ処理」
  * 注意事項: コンソール出力、ローリングファイル出力

### 4. データベース設定

* [ ] T_SETUP_013: DDLスクリプトの作成
  * 目的: ORDER_TRAN、ORDER_DETAILテーブルのDDLを作成する
  * 対象: sql/hsqldb/01_schema.sql
  * 参照SPEC: [data_model.md](../specs/baseline/basic_design/data_model.md) の「3. テーブル定義」
  * 注意事項: 
    * ORDER_TRANテーブル（ORDER_TRAN_ID, ORDER_DATE, CUSTOMER_ID, TOTAL_PRICE, DELIVERY_PRICE, DELIVERY_ADDRESS, SETTLEMENT_TYPE）
    * ORDER_DETAILテーブル（ORDER_TRAN_ID, ORDER_DETAIL_ID, BOOK_ID, BOOK_NAME, PUBLISHER_NAME, PRICE, COUNT）
    * 外部キー制約は ORDER_DETAIL -> ORDER_TRAN のみ
    * インデックス作成

* [ ] T_SETUP_014: サンプルデータスクリプトの作成
  * 目的: 開発・テスト用のサンプルデータを作成する
  * 対象: sql/hsqldb/02_sample_data.sql
  * 参照SPEC: [data_model.md](../specs/baseline/basic_design/data_model.md) の「3.6 ORDER_TRAN」、「3.7 ORDER_DETAIL」
  * 注意事項: 注文トランザクションと注文明細のサンプルデータを作成

### 5. アプリケーションサーバー設定

* [ ] T_SETUP_015: Payara Server設定
  * 目的: Payara Serverにデータソースを設定する
  * 対象: asadmin コマンドまたは管理コンソール
  * 参照SPEC: [data_model.md](../specs/baseline/basic_design/data_model.md) の「1. 概要」
  * 注意事項:
    * JDBCリソースJNDI名: jdbc/berrybooks
    * 接続プール設定（最小/最大接続数）
    * HSQLDB JDBCドライバーの配置

* [ ] T_SETUP_016: CORS設定
  * 目的: SPAからのクロスオリジンリクエストを許可する
  * 対象: CorsFilter（または設定ファイル）
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「1.1 アーキテクチャパターン」
  * 注意事項: 
    * 許可オリジン: http://localhost:5173（開発環境）
    * 許可メソッド: GET, POST, PUT, DELETE
    * 許可ヘッダー: Content-Type, Authorization
    * 認証情報（Cookie）の許可

### 6. 静的リソースの配置

* [ ] T_SETUP_017: 画像リソースの配置
  * 目的: 書籍カバー画像をWAR内に配置する
  * 対象: src/main/resources/images/covers/
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「2.3 静的リソース配信（画像API）」
  * 注意事項: 
    * 画像ファイル: 1.jpg, 2.jpg, ..., no-image.jpg
    * パス: /resources/images/covers/（WARルート相対）

### 7. JAX-RS Application設定

* [ ] T_SETUP_018: ApplicationConfigの作成
  * 目的: JAX-RS Applicationクラスを作成する
  * 対象: pro.kensait.berrybooks.api.ApplicationConfig
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「2.1 システム構成図」
  * 注意事項: 
    * @ApplicationPath("/api")
    * CORSフィルター、JWTフィルター、ExceptionMapperの登録

### 8. 開発環境セットアップ

* [ ] T_SETUP_019: IDE設定
  * 目的: IntelliJ IDEA / Eclipse等のIDE設定を行う
  * 対象: .idea/, .settings/, .project
  * 参照SPEC: なし（プロジェクト固有）
  * 注意事項: 
    * Java 21 SDK設定
    * Payara Server設定
    * Gradle統合

* [ ] T_SETUP_020: ビルド検証
  * 目的: プロジェクトが正常にビルドできることを確認する
  * 対象: ./gradlew build
  * 参照SPEC: なし（プロジェクト固有）
  * 注意事項: 
    * エラーなくビルドが完了すること
    * WARファイルが生成されること
    * デプロイが成功すること

---

## 完了条件

* [ ] Gradleビルドが成功する
* [ ] WARファイルが生成される
* [ ] Payara Serverにデプロイできる
* [ ] データベース接続が確認できる
* [ ] ログが正常に出力される
* [ ] 静的リソース（画像）がアクセスできる

---

## 参考資料

* [../specs/baseline/requirements/requirements.md](../specs/baseline/requirements/requirements.md) - 要件定義書
* [../specs/baseline/basic_design/architecture_design.md](../specs/baseline/basic_design/architecture_design.md) - アーキテクチャ設計書
* [../specs/baseline/basic_design/data_model.md](../specs/baseline/basic_design/data_model.md) - データモデル仕様書
* [../specs/baseline/basic_design/external_interface.md](../specs/baseline/basic_design/external_interface.md) - 外部インターフェース仕様書
