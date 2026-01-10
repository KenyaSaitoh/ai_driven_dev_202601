# セットアップタスク

**担当者:** 全員（初回のみ1回実行）  
**推奨スキル:** Gradle、データベース設定、アプリケーションサーバー設定  
**想定工数:** 2時間  
**依存タスク:** なし

---

## タスク一覧

### T_SETUP_001: プロジェクト構造の確認

- [ ] **T_SETUP_001**: プロジェクト構造の確認
  - **目的**: プロジェクトのディレクトリ構造とファイル配置を確認する
  - **対象**: プロジェクトルート全体
  - **参照SPEC**: [README.md](../README.md) の「プロジェクト構成」
  - **注意事項**: BFFパターンのため、エンティティは注文関連のみ実装

---

### T_SETUP_002: 依存関係の確認

- [ ] **T_SETUP_002**: build.gradleの依存関係確認
  - **目的**: 必要なライブラリが全て定義されているか確認する
  - **対象**: build.gradle
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「1.3 追加ライブラリ」
  - **注意事項**: jjwt 0.12.6、BCrypt、SLF4J、Log4j2、JUnit 5、Mockitoが含まれていること

---

### T_SETUP_003: データベーススキーマ作成スクリプト

- [ ] **T_SETUP_003**: schema.sqlの作成
  - **目的**: 注文データ用のテーブル作成DDLを作成する
  - **対象**: src/main/resources/db/schema.sql
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.6 ORDER_TRAN」「3.7 ORDER_DETAIL」
  - **注意事項**: ORDER_TRAN、ORDER_DETAILのみ作成（BOOK、STOCK、CUSTOMER等は外部APIで管理）

---

### T_SETUP_004: サンプルデータ作成スクリプト

- [ ] **T_SETUP_004**: sample_data.sqlの作成
  - **目的**: 開発・テスト用のサンプルデータを準備する
  - **対象**: src/main/resources/db/sample_data.sql
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.6.6 サンプルデータ」「3.7.6 サンプルデータ」
  - **注意事項**: 注文データのみ作成（書籍・顧客データは外部APIで管理）

---

### T_SETUP_005: persistence.xmlの作成

- [ ] **T_SETUP_005**: persistence.xmlの作成
  - **目的**: JPA永続化ユニットを設定する
  - **対象**: src/main/resources/META-INF/persistence.xml
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「10.1 永続化構成」
  - **注意事項**: JTA DataSource（jdbc/HsqldbDS）を使用、schema-generation.database.action=none

---

### T_SETUP_006: MicroProfile設定ファイル

- [ ] **T_SETUP_006**: microprofile-config.propertiesの作成
  - **目的**: JWT設定と外部API URLを設定する
  - **対象**: src/main/resources/META-INF/microprofile-config.properties
  - **参照SPEC**: 
    - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「6.2 JWT設定」
    - [external_interface.md](../specs/baseline/system/external_interface.md) の「3.1 概要」「14.1 概要」
  - **注意事項**: jwt.secret-key、jwt.expiration-ms、jwt.cookie-name、back-office-api.base-url、customer-hub-api.base-urlを設定

---

### T_SETUP_007: Log4j2設定ファイル

- [ ] **T_SETUP_007**: log4j2.xmlの作成
  - **目的**: ログ出力設定を行う
  - **対象**: src/main/resources/log4j2.xml
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「11. ログ戦略」
  - **注意事項**: コンソール出力とファイル出力を設定、INFOレベルで出力

---

### T_SETUP_008: メッセージプロパティファイル

- [ ] **T_SETUP_008**: messages.propertiesの作成
  - **目的**: エラーメッセージ等の国際化対応を準備する
  - **対象**: src/main/resources/messages.properties
  - **参照SPEC**: [functional_design.md](../specs/baseline/system/functional_design.md) の「6. エラーレスポンス仕様」
  - **注意事項**: エラーメッセージを定義（オプション）

---

### T_SETUP_009: web.xmlの作成

- [ ] **T_SETUP_009**: web.xmlの作成
  - **目的**: Webアプリケーション設定を行う
  - **対象**: src/main/webapp/WEB-INF/web.xml
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「2.2 BFF レイヤードアーキテクチャ」
  - **注意事項**: JAX-RSアプリケーションパスを設定（/api）

---

### T_SETUP_010: 画像リソースの配置

- [ ] **T_SETUP_010**: 書籍表紙画像の配置
  - **目的**: 書籍表紙画像とフォールバック画像を配置する
  - **対象**: src/main/webapp/resources/images/covers/
  - **参照SPEC**: [API_004_images/functional_design.md](../specs/baseline/api/API_004_images/functional_design.md) の「4. ファイル命名規則」
  - **注意事項**: {bookId}.jpg形式で配置、no-image.jpgを必ず配置

---

### T_SETUP_011: データベースセットアップ実行

- [ ] **T_SETUP_011**: データベースの初期化
  - **目的**: スキーマとサンプルデータを投入する
  - **対象**: Gradleタスク実行
  - **参照SPEC**: [README.md](../README.md) の「④ プロジェクトを開始するときに1回だけ実行」
  - **注意事項**: `./gradlew :berry-books-api-sdd:setupHsqldb` を実行

---

### T_SETUP_012: プロジェクトビルド確認

- [ ] **T_SETUP_012**: プロジェクトのビルド
  - **目的**: プロジェクトが正常にビルドできることを確認する
  - **対象**: Gradleビルド
  - **参照SPEC**: [README.md](../README.md) の「④ プロジェクトを開始するときに1回だけ実行」
  - **注意事項**: `./gradlew :berry-books-api-sdd:war` でWARファイル生成を確認

---

### T_SETUP_013: デプロイ確認

- [ ] **T_SETUP_013**: Payara Serverへのデプロイ
  - **目的**: アプリケーションがPayara Serverに正常にデプロイできることを確認する
  - **対象**: デプロイ実行
  - **参照SPEC**: [README.md](../README.md) の「④ プロジェクトを開始するときに1回だけ実行」
  - **注意事項**: `./gradlew :berry-books-api-sdd:deploy` を実行、http://localhost:8080/berry-books-api-sdd/ にアクセス可能か確認

---

## セットアップ完了チェックリスト

- [ ] HSQLDBサーバーが起動している（`./gradlew startHsqldb`）
- [ ] Payara Serverが起動している（`./gradlew startPayara`）
- [ ] データソース（jdbc/HsqldbDS）が作成されている
- [ ] データベーススキーマが作成されている（ORDER_TRAN、ORDER_DETAIL）
- [ ] サンプルデータが投入されている
- [ ] WARファイルがビルドされている
- [ ] アプリケーションがデプロイされている
- [ ] ウェルカムページにアクセスできる（http://localhost:8080/berry-books-api-sdd/）
- [ ] 外部API（back-office-api、customer-hub-api）が起動している

---

## 参考資料

- [README.md](../README.md) - プロジェクトREADME
- [architecture_design.md](../specs/baseline/system/architecture_design.md) - アーキテクチャ設計書
- [data_model.md](../specs/baseline/system/data_model.md) - データモデル仕様書
