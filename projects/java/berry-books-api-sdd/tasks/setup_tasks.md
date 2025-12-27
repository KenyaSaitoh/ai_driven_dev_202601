# セットアップタスク

**担当者:** 全員（1回のみ実行）  
**推奨スキル:** Gradle、HSQLDB、Payara Server  
**想定工数:** 8時間  
**依存タスク:** なし

---

## 概要

本タスクは、プロジェクト初期化、開発環境構築、データベース設定を行います。全員が開発を開始する前に1回だけ実行します。

---

## タスクリスト

### セクション1: プロジェクト構造の確認

- [X] **T_SETUP_001**: プロジェクトディレクトリ構造の確認
  - **目的**: プロジェクトの基本ディレクトリ構造が存在することを確認する
  - **対象**: `src/main/java/`, `src/main/webapp/`, `src/main/resources/`, `src/test/java/`, `sql/hsqldb/`
  - **参照SPEC**: [README.md](../README.md) の「プロジェクト構成」
  - **注意事項**: 不足しているディレクトリは作成する（`.gitkeep` ファイルを配置）

---

### セクション2: データベース初期化

- [X] **T_SETUP_002**: DDLスクリプトの作成
  - **目的**: データベーススキーマを定義するDDLスクリプトを作成する
  - **対象**: `sql/hsqldb/1_DDL.sql`
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「4. テーブル定義」（全テーブル）
  - **注意事項**: 主キー制約、外部キー制約、一意制約、インデックスを含める。STOCKテーブルのVERSIONカラム（楽観的ロック用）を忘れずに定義する

- [X] **T_SETUP_003**: 初期データスクリプトの作成
  - **目的**: マスタデータとテストデータを投入するスクリプトを作成する
  - **対象**: `sql/hsqldb/2_INSERT.sql`
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「5. サンプルデータ」
  - **注意事項**: 書籍は50冊程度、在庫は各書籍に対して設定、テスト用顧客データを2-3件含める

- [ ] **T_SETUP_004**: HSQLDBサーバーの起動と初期化
  - **目的**: HSQLDBサーバーを起動し、DDLとINSERTスクリプトを実行する
  - **対象**: `./gradlew setupHsqldb` タスクの実行
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「10. データベース構成」
  - **注意事項**: ポート9001、データベース名`testdb`、ユーザー`SA`で起動

---

### セクション3: アプリケーション設定ファイル

- [X] **T_SETUP_005**: persistence.xmlの作成
  - **目的**: JPAの永続化設定を定義する
  - **対象**: `src/main/resources/META-INF/persistence.xml`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「10. データベース構成」
  - **注意事項**: persistence-unit名は`BerryBooksPU`、JTAデータソース`jdbc/HsqldbDS`を指定、EclipseLinkのログレベルは`FINE`

- [X] **T_SETUP_006**: web.xmlの作成
  - **目的**: Webアプリケーションの設定を定義する
  - **対象**: `src/main/webapp/WEB-INF/web.xml`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「12. ビルド＆デプロイ」
  - **注意事項**: Faces Servletのマッピング（`*.xhtml`）、セッションタイムアウト（60分）を設定

- [X] **T_SETUP_007**: faces-config.xmlの作成
  - **目的**: JSFの設定を定義する
  - **対象**: `src/main/webapp/WEB-INF/faces-config.xml`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「1.2 Jakarta EE仕様」
  - **注意事項**: Jakarta Faces 4.0の設定、ナビゲーションルールは明示的に定義しない（暗黙的ナビゲーション使用）

- [X] **T_SETUP_008**: log4j2.xmlの作成
  - **目的**: ロギング設定を定義する
  - **対象**: `src/main/resources/log4j2.xml`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「11. ログ戦略」
  - **注意事項**: コンソールとファイルアペンダーを設定、ログレベルはINFO、ログパターンは`[ ClassName#methodName ] message`

- [X] **T_SETUP_009**: messages.propertiesの作成
  - **目的**: メッセージリソースファイルを作成する
  - **対象**: `src/main/resources/messages.properties`
  - **参照SPEC**: [behaviors.md](../specs/baseline/system/behaviors.md) の「エラーメッセージ一覧」
  - **注意事項**: 検証エラー（VAL-xxx）、ビジネスエラー（BIZ-xxx）、システムエラー（SYS-xxx）のメッセージを定義

---

### セクション4: Payara Server設定

- [ ] **T_SETUP_010**: JDBCコネクションプールの設定
  - **目的**: Payara ServerにHSQLDBコネクションプールを設定する
  - **対象**: Payara Server管理コンソール（http://localhost:4848）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「10.2 コネクションプール」
  - **注意事項**: プール名`HsqldbPool`、ドライバ`org.hsqldb.jdbc.JDBCDriver`、URL`jdbc:hsqldb:hsql://localhost:9001/testdb`、最小10、最大50

- [ ] **T_SETUP_011**: JDBCリソースの設定
  - **目的**: JNDI名でデータソースを公開する
  - **対象**: Payara Server管理コンソール
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「10.1 永続化構成」
  - **注意事項**: JNDI名`jdbc/HsqldbDS`、コネクションプール`HsqldbPool`を指定

---

### セクション5: 静的リソース

- [X] **T_SETUP_012**: CSSファイルの作成
  - **目的**: 共通CSSスタイルを定義する
  - **対象**: `src/main/webapp/resources/css/style.css`
  - **参照SPEC**: [screen_design.md](../specs/baseline/system/screen_design.md) の「カラースキーム」
  - **注意事項**: プライマリカラー`#CF3F4E`、セカンダリカラー`#E8E8E8`、テキスト`#333333`を定義

- [X] **T_SETUP_013**: 書籍カバー画像のコピー
  - **目的**: 書籍カバー画像を`webapp/resources/covers/`にコピーする
  - **対象**: `images/covers/` から `src/main/webapp/resources/covers/` へコピー
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「6. カバー画像の取り扱い」
  - **注意事項**: 約50冊分の画像ファイルと`no-image.jpg`を含める

---

### セクション6: ビルドとデプロイの確認

- [ ] **T_SETUP_014**: プロジェクトのビルド
  - **目的**: Gradleでプロジェクトをビルドし、WARファイルを生成する
  - **対象**: `./gradlew :berry-books-mvc-sdd:war`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「12. ビルド＆デプロイ」
  - **注意事項**: ビルドエラーがないことを確認

- [ ] **T_SETUP_015**: アプリケーションのデプロイ
  - **目的**: Payara ServerにWARファイルをデプロイする
  - **対象**: `./gradlew :berry-books-mvc-sdd:deploy`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「12. ビルド＆デプロイ」
  - **注意事項**: コンテキストルート`/berry-books`でデプロイされることを確認

- [ ] **T_SETUP_016**: 動作確認
  - **目的**: アプリケーションが正常に起動することを確認する
  - **対象**: ブラウザで`http://localhost:8080/berry-books/`にアクセス
  - **参照SPEC**: なし
  - **注意事項**: この時点では画面は表示されなくても良い（404エラーでも可）

---

## 完了条件

- [ ] HSQLDBサーバーが起動し、テーブルと初期データが投入されている
- [ ] Payara Serverが起動し、JDBCリソースが設定されている
- [ ] プロジェクトがビルドでき、WARファイルが生成される
- [ ] アプリケーションがPayara Serverにデプロイされている
- [ ] `http://localhost:8080/berry-books/` にアクセス可能

---

## 次のステップ

セットアップ完了後、[common_tasks.md](common_tasks.md) に進んでください。

