# セットアップタスク

**担当者:** 全員（各開発者が実行前に1回だけ実行）  
**推奨スキル:** Java開発環境構築、データベース基礎知識  
**想定工数:** 2時間  
**依存タスク:** なし

---

## 概要

プロジェクトの開発環境を構築し、データベースを初期化します。全員が実装開始前に1回だけ実行してください。

---

## タスクリスト

### 0.1 開発環境セットアップ

- [ ] **T_SETUP_001**: JDK 21のインストール
  - **目的**: Java開発環境を構築する
  - **対象**: JDK 21（LTS）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「1.1 コアプラットフォーム」
  - **注意事項**: JAVA_HOME環境変数を設定すること

- [ ] **T_SETUP_002**: Payara Server 6のインストール
  - **目的**: Jakarta EE 10対応アプリケーションサーバーを準備する
  - **対象**: Payara Server 6.x
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「1.1 コアプラットフォーム」
  - **注意事項**: Payaraのポート設定を確認すること（デフォルト: 8080, 4848）

- [ ] **T_SETUP_003**: Gradle 8のインストール
  - **目的**: ビルドツールを準備する
  - **対象**: Gradle 8.x
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「1.1 コアプラットフォーム」
  - **注意事項**: Gradle Wrapperを使用する場合は不要

- [ ] **T_SETUP_004**: IDEのインストールと設定
  - **目的**: 開発用IDEを準備する
  - **対象**: IntelliJ IDEA / Eclipse / VS Code
  - **参照SPEC**: なし
  - **注意事項**: Jakarta EE、JAX-RS、JPAのプラグインをインストールすること

---

### 0.2 プロジェクトセットアップ

- [ ] **T_SETUP_005**: プロジェクトのクローン
  - **目的**: プロジェクトのソースコードを取得する
  - **対象**: Gitリポジトリ
  - **参照SPEC**: [README.md](../README.md)
  - **注意事項**: なし

- [ ] **T_SETUP_006**: プロジェクトの依存関係解決
  - **目的**: 必要なライブラリをダウンロードする
  - **対象**: Gradle依存関係
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「1.3 追加ライブラリ」
  - **注意事項**: `./gradlew :berry-books-api:dependencies` で確認

---

### 0.3 データベースセットアップ

- [ ] **T_SETUP_007**: HSQLDBサーバーの起動
  - **目的**: データベースサーバーを起動する
  - **対象**: HSQLDB 2.7.x
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9. データベース構成」
  - **注意事項**: `./gradlew startHsqldb` で起動、ポート9001でリッスン

- [ ] **T_SETUP_008**: データベース初期化
  - **目的**: テーブル作成とサンプルデータ投入を実行する
  - **対象**: DDL、DMLスクリプト
  - **参照SPEC**: 
    - [data_model.md](../specs/baseline/system/data_model.md) の「3. テーブル定義」
    - `sql/hsqldb/1_DDL.sql`, `sql/hsqldb/2_INSERT.sql`
  - **注意事項**: `./gradlew :berry-books-api:setupHsqldb` で実行

- [ ] **T_SETUP_009**: データソース設定
  - **目的**: Payara ServerにJNDIデータソースを設定する
  - **対象**: jdbc/HsqldbDS
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.2 コネクションプール」
  - **注意事項**: 
    - JNDI名: `jdbc/HsqldbDS`
    - 接続URL: `jdbc:hsqldb:hsql://localhost:9001/testdb`
    - ユーザー: `SA`, パスワード: (空)

---

### 0.4 アプリケーション設定

- [ ] **T_SETUP_010**: persistence.xmlの確認
  - **目的**: JPA設定を確認する
  - **対象**: `src/main/resources/META-INF/persistence.xml`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.1 永続化構成」
  - **注意事項**: JTA DataSourceが`jdbc/HsqldbDS`に設定されていることを確認

- [ ] **T_SETUP_011**: Log4j2設定ファイルの配置
  - **目的**: ログ出力設定を行う
  - **対象**: `src/main/resources/log4j2.xml`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「10. ログ戦略」
  - **注意事項**: ログレベルはINFO、エラーログはWARN/ERROR

- [ ] **T_SETUP_012**: JWT設定ファイルの配置
  - **目的**: JWT秘密鍵と有効期限を設定する
  - **対象**: `src/main/resources/META-INF/microprofile-config.properties`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「5.2 JWT設定」
  - **注意事項**: 
    - `jwt.secret-key`: 秘密鍵（32文字以上）
    - `jwt.expiration-ms`: 86400000（24時間）
    - `jwt.cookie-name`: berry-books-jwt

---

### 0.5 外部API連携設定

- [ ] **T_SETUP_013**: berry-books-rest APIの起動確認
  - **目的**: 外部顧客管理APIが利用可能であることを確認する
  - **対象**: berry-books-rest API
  - **参照SPEC**: [external_interface.md](../specs/baseline/system/external_interface.md) の「3. berry-books-rest API連携」
  - **注意事項**: 
    - ベースURL: `http://localhost:8080/customer-api/customers`
    - エンドポイント確認: `GET /customers/query_email?email=alice@gmail.com`

- [ ] **T_SETUP_014**: 外部API接続設定
  - **目的**: 外部APIのベースURLを設定する
  - **対象**: `META-INF/microprofile-config.properties`
  - **参照SPEC**: [external_interface.md](../specs/baseline/system/external_interface.md) の「3.1 概要」
  - **注意事項**: `customer.api.base-url=http://localhost:8080/customer-api/customers`

---

### 0.6 静的リソース配置

- [ ] **T_SETUP_015**: 書籍表紙画像の配置
  - **目的**: 書籍表紙画像ファイルを配置する
  - **対象**: `src/main/webapp/resources/images/covers/`
  - **参照SPEC**: 
    - [functional_design.md](../specs/baseline/system/functional_design.md) の「5. 画像API」
    - [api/API_004_images/functional_design.md](../specs/baseline/api/API_004_images/functional_design.md) の「4. ファイル命名規則」
  - **注意事項**: 
    - ファイル名: `{書籍名}.jpg`
    - フォールバック画像: `no-image.jpg`

---

### 0.7 動作確認

- [ ] **T_SETUP_016**: プロジェクトのビルド
  - **目的**: プロジェクトが正常にビルドできることを確認する
  - **対象**: WARファイル
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「12.1 ビルドプロセス」
  - **注意事項**: `./gradlew :berry-books-api:war` で実行

- [ ] **T_SETUP_017**: アプリケーションのデプロイ
  - **目的**: Payara Serverにアプリケーションをデプロイする
  - **対象**: berry-books-api.war
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「12.2 デプロイアーキテクチャ」
  - **注意事項**: `./gradlew :berry-books-api:deploy` で実行

- [ ] **T_SETUP_018**: 動作確認
  - **目的**: アプリケーションが正常に起動していることを確認する
  - **対象**: REST APIエンドポイント
  - **参照SPEC**: [functional_design.md](../specs/baseline/system/functional_design.md)
  - **注意事項**: 
    - ベースURL: `http://localhost:8080/berry-books-api/api`
    - 確認エンドポイント: `GET /api/books`（認証不要）

---

## 完了条件

以下の全ての条件を満たしていること：

- [ ] 開発環境（JDK 21、Payara Server 6、Gradle 8）がインストールされている
- [ ] HSQLDBサーバーが起動し、データベースが初期化されている
- [ ] プロジェクトが正常にビルドできる
- [ ] アプリケーションがPayara Serverにデプロイされている
- [ ] REST APIエンドポイント（`GET /api/books`）が正常にレスポンスを返す

---

## トラブルシューティング

### データベース接続エラー
- HSQLDBサーバーが起動しているか確認
- ポート9001が使用可能か確認
- データソース設定（JNDI名、接続URL）が正しいか確認

### ビルドエラー
- JDK 21がインストールされているか確認
- JAVA_HOME環境変数が設定されているか確認
- Gradleの依存関係が解決されているか確認

### デプロイエラー
- Payara Serverが起動しているか確認
- ポート8080が使用可能か確認
- WARファイルが正常に生成されているか確認

---

## 参考資料

- [README.md](../README.md) - プロジェクトREADME
- [architecture_design.md](../specs/baseline/system/architecture_design.md) - アーキテクチャ設計書
- [data_model.md](../specs/baseline/system/data_model.md) - データモデル仕様書
- [external_interface.md](../specs/baseline/system/external_interface.md) - 外部インターフェース仕様書
