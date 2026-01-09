# セットアップタスク

**担当者:** 全員（プロジェクト開始時に1回のみ実行）  
**推奨スキル:** Jakarta EE、Payara Server、HSQLDB、Gradle  
**想定工数:** 2時間  
**依存タスク:** なし

---

## タスクリスト

### T_SETUP_001: プロジェクト構造の確認

- **目的**: プロジェクトのディレクトリ構造とビルド設定を確認する
- **対象**: プロジェクトルート、build.gradle、settings.gradle
- **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「2. 全体アーキテクチャ」
- **注意事項**: 
  - レイヤードアーキテクチャ（Presentation、Business Logic、Data Access、Entity）の構造を理解する
  - Gradleビルド設定を確認する

---

### T_SETUP_002: データベース初期化

- **目的**: HSQLDBデータベースを初期化し、初期データを投入する
- **対象**: データベーススキーマ、初期データSQLスクリプト
- **参照SPEC**: 
  - [data_model.md](../specs/baseline/system/data_model.md) の「3. テーブル定義」
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.2 実行環境」
- **注意事項**: 
  - すべてのテーブル（BOOK, STOCK, CATEGORY, PUBLISHER, EMPLOYEE, DEPARTMENT, WORKFLOW）を作成する
  - 初期データ（社員、部署、カテゴリ、出版社、書籍、在庫）を投入する
  - 外部キー制約、インデックスを設定する

---

### T_SETUP_003: Payara Server設定

- **目的**: Payara Server 6.xをセットアップし、データソースを設定する
- **対象**: Payara Server、JDBCリソース、接続プール
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.2 実行環境」
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「10.3 接続プール」
- **注意事項**: 
  - JNDI名: `jdbc/HsqldbDS`でデータソースを設定する
  - 接続プールの設定を行う
  - Payara Serverの起動を確認する

---

### T_SETUP_004: persistence.xml設定

- **目的**: JPA設定ファイルを作成し、データソースとエンティティを設定する
- **対象**: src/main/resources/META-INF/persistence.xml
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.3 設定ファイル」
  - [data_model.md](../specs/baseline/system/data_model.md) の「4. エンティティ関連図（JPA）」
- **注意事項**: 
  - データソースJNDI名を指定する
  - すべてのエンティティクラスを登録する
  - JPAプロパティ（ログレベル、DDL生成など）を設定する

---

### T_SETUP_005: MicroProfile Config設定

- **目的**: MicroProfile Config設定ファイルを作成し、JWT設定を行う
- **対象**: src/main/resources/META-INF/microprofile-config.properties
- **参照SPEC**: 
  - [functional_design.md (API_001_auth)](../specs/baseline/api/API_001_auth/functional_design.md) の「7. 設定」
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.3 設定ファイル」
- **注意事項**: 
  - JWT秘密鍵（jwt.secret-key）を設定する（32文字以上）
  - JWT有効期限（jwt.expiration-ms）を設定する（デフォルト: 86400000ミリ秒 = 24時間）
  - JWT Cookie名（jwt.cookie-name）を設定する（デフォルト: back-office-jwt）

---

### T_SETUP_006: beans.xml設定

- **目的**: CDI設定ファイルを作成する
- **対象**: src/main/resources/META-INF/beans.xml
- **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.3 設定ファイル」
- **注意事項**: 
  - CDI 4.0の設定を行う
  - bean-discovery-mode="all"を設定する

---

### T_SETUP_007: ログ設定

- **目的**: SLF4Jログ設定を行う
- **対象**: ログ設定ファイル（logback.xml等）
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9. ログ設計」
  - [requirements.md](../specs/baseline/system/requirements.md) の「3.4 保守性要件」
- **注意事項**: 
  - ログレベル（INFO、WARN、ERROR、DEBUG）を設定する
  - ログ形式を統一する（[クラス名#メソッド名] メッセージ）
  - ログファイルのローテーション設定を行う

---

### T_SETUP_008: メッセージプロパティファイル作成

- **目的**: エラーメッセージとバリデーションメッセージの日本語プロパティファイルを作成する
- **対象**: 
  - src/main/resources/messages.properties
  - src/main/resources/ValidationMessages_ja.properties
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.5 Cross-Cutting Concerns」
  - [functional_design.md (system)](../specs/baseline/system/functional_design.md) の「6. エラーハンドリング」
- **注意事項**: 
  - すべてのエラーメッセージを日本語で定義する
  - バリデーションメッセージを日本語で定義する
  - MessageUtilクラスで読み込めるようにする

---

### T_SETUP_009: 開発環境の動作確認

- **目的**: セットアップが正しく完了したことを確認する
- **対象**: プロジェクト全体
- **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「11. デプロイメント構成」
- **注意事項**: 
  - Gradleビルドが成功することを確認する
  - Payara Serverにデプロイできることを確認する
  - データベース接続が正常に動作することを確認する
  - アプリケーションが起動することを確認する

---

## セットアップ完了チェックリスト

- [x] プロジェクト構造を理解した
- [x] データベースが初期化され、初期データが投入された（SQLスクリプト修正完了）
- [ ] Payara Serverが起動し、データソースが設定された（インフラセットアップはスキップ）
- [x] persistence.xmlが正しく設定された
- [x] MicroProfile Config設定が完了した
- [x] beans.xmlが作成された
- [x] ログ設定が完了した
- [x] メッセージプロパティファイルが作成された
- [ ] Gradleビルドが成功する
- [ ] アプリケーションがPayara Serverにデプロイできる
- [ ] データベース接続が正常に動作する

---

## 次のステップ

セットアップが完了したら、[共通機能タスク](common_tasks.md)に進んでください。
