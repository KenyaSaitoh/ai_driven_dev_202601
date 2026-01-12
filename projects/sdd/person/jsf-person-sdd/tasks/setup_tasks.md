# セットアップタスク

担当者: 全員（プロジェクト開始時に1回だけ実行）
推奨スキル: Java開発環境、Gradle、Payara Server、HSQLDB
想定工数: 2時間
依存タスク: なし

## タスク一覧

* [X] T_SETUP_001: 開発環境の確認
  * 目的: 必要な開発ツールとバージョンを確認する
  * 対象: JDK 21、Gradle 8.x、Payara Server 6、HSQLDB 2.7.x
  * 参照SPEC: [requirements.md](../specs/baseline/system/requirements.md) の「4.1 技術スタック」
  * 注意事項: JDK 21以上、Gradle 8.x以上が必要

* [X] T_SETUP_002: プロジェクト構造の作成
  * 目的: Gradleプロジェクトの基本ディレクトリ構造を作成する
  * 対象: src/main/java、src/main/resources、src/main/webapp、src/test/java
  * 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.3 リソースファイル配置」
  * 注意事項: パッケージ構造は pro.kensait.jsf.person 配下に作成

* [X] T_SETUP_003: build.gradle の作成
  * 目的: Gradle ビルドファイルを作成し、依存関係を定義する
  * 対象: build.gradle
  * 参照SPEC: 
    * [requirements.md](../specs/baseline/system/requirements.md) の「4.1 技術スタック」
    * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「2. 技術スタック」
  * 注意事項: Jakarta EE 10、JSF 4.0、JPA 3.1、CDI 4.0の依存関係を含める

* [X] T_SETUP_004: web.xml の作成
  * 目的: Webアプリケーション設定ファイルを作成する
  * 対象: src/main/webapp/WEB-INF/web.xml
  * 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.3 リソースファイル配置」
  * 注意事項: Jakarta Servlet 6.0の名前空間を使用

* [X] T_SETUP_005: beans.xml の作成
  * 目的: CDI設定ファイルを作成する
  * 対象: src/main/webapp/WEB-INF/beans.xml
  * 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.4 依存性注入（CDI）」
  * 注意事項: bean-discovery-mode="all" を設定

* [X] T_SETUP_006: faces-config.xml の作成（オプション）
  * 目的: JSF設定ファイルを作成する（必要な場合のみ）
  * 対象: src/main/webapp/WEB-INF/faces-config.xml
  * 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.3 リソースファイル配置」
  * 注意事項: JSF 4.0の名前空間を使用。画面遷移は戻り値で制御するため、navigation-ruleは不要

* [SKIPPED] T_SETUP_007: HSQLDBサーバーの起動
  * 目的: HSQLDBサーバーを起動する
  * 対象: HSQLDBサーバー（localhost:9001）
  * 参照SPEC: [requirements.md](../specs/baseline/system/requirements.md) の「4.2 データベース要件」
  * 注意事項: skip_infrastructure=trueのためスキップ（既に起動済みと想定）

* [SKIPPED] T_SETUP_008: Payara Serverの起動
  * 目的: Payara Serverを起動する
  * 対象: Payara Server 6
  * 参照SPEC: [requirements.md](../specs/baseline/system/requirements.md) の「4.1 技術スタック」
  * 注意事項: skip_infrastructure=trueのためスキップ（既に起動済みと想定）

* [SKIPPED] T_SETUP_009: データソースの作成
  * 目的: Payara Serverにデータソースを作成する
  * 対象: java:app/jdbc/testdb
  * 参照SPEC: 
    * [data_model.md](../specs/baseline/system/data_model.md) の「8. データソース設定」
    * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「6.2 データソース設定」
  * 注意事項: skip_infrastructure=trueのためスキップ（既に作成済みと想定）

* [X] T_SETUP_010: データベーステーブルの作成
  * 目的: PERSONテーブルを作成し、初期データを登録する
  * 対象: PERSONテーブル
  * 参照SPEC: 
    * [data_model.md](../specs/baseline/system/data_model.md) の「3.1 PERSONテーブル」
    * [data_model.md](../specs/baseline/system/data_model.md) の「3.2 初期データ」
  * 注意事項: プロジェクトディレクトリで `./gradlew :jsf-person-sdd:setupHsqldb` を実行

* [X] T_SETUP_011: CSSファイルの作成
  * 目的: 画面スタイルシートを作成する
  * 対象: src/main/webapp/resources/css/style.css
  * 参照SPEC: 
    * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「7.4 スタイルシート」
    * [SCREEN_001_PersonList画面設計](../specs/baseline/screen/SCREEN_001_PersonList/screen_design.md) の「6. スタイルシート設計」
  * 注意事項: テーブル、フォーム、ボタンのスタイルを定義

* [X] T_SETUP_012: セットアップの動作確認
  * 目的: セットアップが正しく完了したことを確認する
  * 対象: プロジェクト全体
  * 参照SPEC: [requirements.md](../specs/baseline/system/requirements.md)
  * 注意事項: データベース接続、Payara Server起動、プロジェクトビルドを確認

## 注意事項

* すべてのタスクは順次実行（並行実行不可）
* T_SETUP_001〜T_SETUP_006は開発環境によって調整が必要
* T_SETUP_007〜T_SETUP_010はルートの`build.gradle`で定義されたタスクを使用
* セットアップ完了後、共通機能タスクに進む

## 参考資料

* [システム要件定義](../specs/baseline/system/requirements.md)
* [アーキテクチャ設計書](../specs/baseline/system/architecture_design.md)
* [データモデル](../specs/baseline/system/data_model.md)
* [マイグレーション憲章](../../../agent_skills/struts-to-jsf-migration/principles/constitution.md)
