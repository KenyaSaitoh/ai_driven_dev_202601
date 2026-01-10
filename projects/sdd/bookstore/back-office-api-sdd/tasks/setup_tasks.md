# セットアップタスク

**担当者:** 全員（プロジェクト開始時に1回だけ実行）  
**推奨スキル:** Jakarta EE、Gradle、データベース基礎知識  
**想定工数:** 2時間  
**依存タスク:** なし

---

## 概要

このタスクリストは、プロジェクト開始時に1回だけ実行する初期セットアップタスクを含みます。開発環境の構築、データベースの初期化、アプリケーションサーバーの設定を行います。

---

## タスクリスト

### T_SETUP_001: プロジェクト構造の作成

- **目的**: プロジェクトのディレクトリ構造とビルド設定を作成する
- **対象**: 
  - `build.gradle`（ビルド設定）
  - `src/main/java/` ディレクトリ
  - `src/main/resources/` ディレクトリ
  - `src/main/webapp/` ディレクトリ
  - `src/test/java/` ディレクトリ
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3. パッケージ構造」
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「5. 技術スタック」
- **注意事項**: 
  - パッケージ構造は `pro.kensait.backoffice` をベースとする
  - Jakarta EE 10、JAX-RS 3.1、JPA 3.1の依存関係を追加

---

### T_SETUP_002: データベーススキーマの作成

- **目的**: データベーステーブルを作成する
- **対象**: 
  - `src/main/resources/db/schema.sql`（DDL）
- **参照SPEC**: 
  - [data_model.md](../specs/baseline/system/data_model.md) の「3. テーブル定義」
- **注意事項**: 
  - 全7テーブル（BOOK, STOCK, CATEGORY, PUBLISHER, EMPLOYEE, DEPARTMENT, WORKFLOW）を作成
  - 外部キー制約、インデックスを定義
  - STOCKテーブルのVERSIONカラムを楽観的ロック用に定義

---

### T_SETUP_003: サンプルデータの準備

- **目的**: 開発・テスト用の初期データを準備する
- **対象**: 
  - `src/main/resources/db/sample_data.sql`（DML）
- **参照SPEC**: 
  - [data_model.md](../specs/baseline/system/data_model.md) の「11. データ移行」
- **注意事項**: 
  - カテゴリマスタ（CATEGORY）
  - 出版社マスタ（PUBLISHER）
  - 部署マスタ（DEPARTMENT）
  - 社員マスタ（EMPLOYEE）- パスワードはBCryptハッシュ化
  - 書籍マスタ（BOOK）- サンプル書籍データ
  - 在庫マスタ（STOCK）- 初期在庫データ

---

### T_SETUP_004: JPA設定ファイルの作成

- **目的**: JPA（Hibernate）の設定ファイルを作成する
- **対象**: 
  - `src/main/resources/META-INF/persistence.xml`
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「5.3 設定ファイル」
- **注意事項**: 
  - データソースJNDI名: `jdbc/HsqldbDS`
  - Hibernate方言: `org.hibernate.dialect.HSQLDialect`
  - `hibernate.hbm2ddl.auto`: `none`（手動スキーマ管理）
  - SQLログ出力設定

---

### T_SETUP_005: Webアプリケーション設定ファイルの作成

- **目的**: Webアプリケーションの設定ファイルを作成する
- **対象**: 
  - `src/main/webapp/WEB-INF/web.xml`
  - `src/main/resources/META-INF/beans.xml`（CDI設定）
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「5.3 設定ファイル」
- **注意事項**: 
  - `web.xml`はJakarta EE 10仕様に準拠
  - `beans.xml`は`bean-discovery-mode="all"`を設定

---

### T_SETUP_006: MicroProfile Config設定ファイルの作成

- **目的**: JWT認証の設定ファイルを作成する
- **対象**: 
  - `src/main/resources/META-INF/microprofile-config.properties`
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「7. セキュリティアーキテクチャ」
- **注意事項**: 
  - `jwt.secret-key`: JWT署名用秘密鍵（256ビット以上）
  - `jwt.expiration-ms`: 86400000（24時間）
  - 本番環境では環境変数で秘密鍵を上書き

---

### T_SETUP_007: ログ設定ファイルの作成

- **目的**: SLF4J + Log4j2のログ設定を作成する
- **対象**: 
  - `src/main/resources/log4j2.xml`
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「10. ログ設計」
- **注意事項**: 
  - コンソール出力とファイル出力を設定
  - ログレベル: INFO（デフォルト）、DEBUG（開発環境）
  - ログフォーマット: `[クラス名#メソッド名] メッセージ`

---

### T_SETUP_008: メッセージプロパティファイルの作成

- **目的**: エラーメッセージとバリデーションメッセージの日本語化ファイルを作成する
- **対象**: 
  - `src/main/resources/messages.properties`（日本語メッセージ）
  - `src/main/resources/ValidationMessages_ja.properties`（バリデーションメッセージ）
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/system/functional_design.md) の「6. エラーハンドリング」
- **注意事項**: 
  - 全エラーメッセージを日本語化
  - Bean Validationメッセージをカスタマイズ

---

### T_SETUP_009: データベース初期化スクリプトの実行

- **目的**: HSQLDBにスキーマとサンプルデータを投入する
- **対象**: Gradleタスク `setupHsqldb`
- **参照SPEC**: 
  - [README.md](../README.md) の「④ プロジェクトを開始するときに1回だけ実行」
- **注意事項**: 
  - HSQLDBサーバーが起動していることを確認
  - `schema.sql` → `sample_data.sql` の順で実行
  - データソース（`jdbc/HsqldbDS`）がPayara Serverに登録されていることを確認

---

### T_SETUP_010: 初回ビルドとデプロイ

- **目的**: プロジェクトをビルドしてPayara Serverにデプロイする
- **対象**: 
  - Gradleタスク `war`
  - Gradleタスク `deploy`
- **参照SPEC**: 
  - [README.md](../README.md) の「④ プロジェクトを開始するときに1回だけ実行」
- **注意事項**: 
  - Payara Serverが起動していることを確認
  - デプロイ後、ウェルカムページ（`http://localhost:8080/back-office-api-sdd/`）にアクセス可能であることを確認

---

## セットアップ完了確認

### チェックリスト

- [ ] プロジェクト構造が作成されている
- [ ] データベーステーブルが作成されている（7テーブル）
- [ ] サンプルデータが投入されている
- [ ] JPA設定ファイルが作成されている
- [ ] Webアプリケーション設定ファイルが作成されている
- [ ] MicroProfile Config設定ファイルが作成されている
- [ ] ログ設定ファイルが作成されている
- [ ] メッセージプロパティファイルが作成されている
- [ ] アプリケーションが正常にデプロイされている

### 動作確認

以下のコマンドでデータベース接続を確認:

```sql
-- HSQLDBマネージャーで実行
SELECT * FROM CATEGORY;
SELECT * FROM PUBLISHER;
SELECT * FROM DEPARTMENT;
SELECT * FROM EMPLOYEE;
SELECT * FROM BOOK;
SELECT * FROM STOCK;
```

---

## 次のステップ

セットアップ完了後、以下のタスクに進んでください:

- [common_tasks.md](common_tasks.md) - 共通機能の実装

---

**タスクファイル作成日:** 2025-01-10  
**想定実行順序:** 1番目（最初に実行）
