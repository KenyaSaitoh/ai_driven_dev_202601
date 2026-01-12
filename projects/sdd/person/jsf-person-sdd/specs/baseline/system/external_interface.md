# 外部インターフェース仕様書

## 1. 概要

本ドキュメントは、人材管理システム（JSF Person）が外部システムを呼び出す際のインターフェース仕様を定義する。

## 2. 外部システム連携状況

人材管理システム（JSF Person）は、現時点では外部システムとの連携を行わない独立したWebアプリケーションとして設計されている。

```mermaid
graph TB
    subgraph "Client Layer"
        Browser[Webブラウザ]
    end
    
    subgraph "Presentation Layer"
        JSF[JSF Managed Bean<br/>PersonListBean<br/>PersonInputBean<br/>PersonConfirmBean]
    end
    
    subgraph "Business Logic Layer"
        Service[PersonService<br/>@Transactional]
    end
    
    subgraph "Data Access Layer"
        JPA[JPA EntityManager<br/>Person Entity]
    end
    
    subgraph "Database Layer"
        DB[(HSQLDB<br/>PERSONテーブル)]
    end
    
    Browser -->|HTTP/HTTPS| JSF
    JSF -->|@Inject| Service
    Service -->|@PersistenceContext| JPA
    JPA -->|JDBC| DB
    
    style JSF fill:#e1f5ff,stroke:#0066cc,stroke-width:3px
```

### 2.1 システムの役割

* JSF Person（本システム）
  * 役割: 人材情報の管理（登録、参照、更新、削除）
  * データベースへの直接アクセス
  * 外部連携: なし（独立したアプリケーション）

### 2.2 本システムが公開するインターフェース

本システムは、Webブラウザからのアクセスのみを受け付ける。REST APIやSOAPサービスは提供しない。

* アクセスURL:
  * PERSON一覧画面: http://localhost:8080/jsf-person/personList.xhtml
  * PERSON入力画面: http://localhost:8080/jsf-person/personInput.xhtml
  * PERSON確認画面: http://localhost:8080/jsf-person/personConfirm.xhtml

* プロトコル:
  * HTTP/HTTPS

* 認証:
  * 本リリースでは認証機能なし
  * 将来的にJakarta Security（FORM認証、Basic認証等）を追加する可能性がある

### 2.3 外部システムからの呼び出し

* 現状: 外部システムからの呼び出しは想定していない
* 将来的な拡張:
  * REST APIの追加（Jakarta RESTful Web Services）
  * 他のシステムからPERSON情報を取得・更新するAPI
  * 認証・認可機能（Jakarta Security、OAuth 2.0等）

## 3. データベース接続

本システムはデータベース（HSQLDB）に接続してデータの永続化を行う。

### 3.1 接続情報

* データベース種別
  * HSQLDB 2.7.x

* 接続方式
  * JDBC（JPA経由）
  * データソース名: java:app/jdbc/testdb
  * 接続プール管理: Payara Server

* JDBC URL
  * jdbc:hsqldb:hsql://localhost:9001/testdb

* 認証情報
  * ユーザー名: SA
  * パスワード: （空文字）

### 3.2 アクセステーブル

本システムがアクセスするテーブル:

* PERSONテーブル
  * 用途: 人材情報の管理
  * アクセス権限: READ、WRITE（全CRUD操作）
  * 操作: SELECT、INSERT、UPDATE、DELETE

詳細なデータモデルについては [data_model.md](data_model.md) を参照してください。

### 3.3 データソース設定（Payara Server）

* データソースの定義:
  * JNDI名: java:app/jdbc/testdb
  * リソースタイプ: javax.sql.DataSource（jakarta.sql.DataSource）
  * JDBCドライバー: org.hsqldb.jdbc.JDBCDriver
  * 接続URL: jdbc:hsqldb:hsql://localhost:9001/testdb
  * ユーザー名: SA
  * パスワード: （空文字）

* 接続プール設定:
  * 最小接続数: 8
  * 最大接続数: 32
  * アイドルタイムアウト: 300秒

### 3.4 トランザクション管理

* トランザクション種別: JTA（Jakarta Transactions）
* トランザクション境界: PersonServiceのメソッドレベル
* トランザクション分離レベル: READ_COMMITTED（HSQLDB デフォルト）

## 4. ファイルシステムアクセス

### 4.1 ファイルアップロード・ダウンロード

* 現状: ファイルアップロード・ダウンロード機能はなし
* 将来的な拡張:
  * 履歴書ファイルのアップロード
  * PERSONリストのCSVエクスポート

### 4.2 ログファイル出力

* ログファイル:
  * Payara ServerのログディレクトリにApplication Serverログが出力される
  * パス: $PAYARA_HOME/glassfish/domains/domain1/logs/server.log

* ログレベル:
  * INFO: 主要な処理（登録、更新、削除）
  * SEVERE: エラー・例外

## 5. メール送信

### 5.1 メール送信機能

* 現状: メール送信機能はなし
* 将来的な拡張:
  * PERSON登録時の通知メール
  * Jakarta Mail APIを使用

## 6. 外部APIとの連携

### 6.1 REST API呼び出し

* 現状: 外部REST APIの呼び出しはなし
* 将来的な拡張:
  * 郵便番号APIとの連携（住所情報の取得）
  * 認証サービスとの連携（OAuth 2.0）

### 6.2 SOAP Webサービス呼び出し

* 現状: SOAP Webサービスの呼び出しはなし
* 将来的な拡張: 必要に応じて検討

## 7. 将来的な拡張性

### 7.1 REST API提供

* 目的: 他のシステムからPERSON情報にアクセスできるようにする
* 技術: Jakarta RESTful Web Services（JAX-RS）
* エンドポイント例:
  * GET /api/persons - 全PERSON取得
  * GET /api/persons/{id} - 指定PERSON取得
  * POST /api/persons - PERSON追加
  * PUT /api/persons/{id} - PERSON更新
  * DELETE /api/persons/{id} - PERSON削除

### 7.2 認証・認可の追加

* 目的: セキュリティ強化
* 技術: Jakarta Security、OAuth 2.0、OpenID Connect
* 認証方式:
  * FORM認証
  * Basic認証
  * Bearer Token認証（REST APIの場合）

### 7.3 メッセージングとの連携

* 目的: 非同期処理、イベント駆動
* 技術: Jakarta Messaging（JMS）、Kafka
* ユースケース:
  * PERSON登録時のイベント送信
  * 他のシステムへの通知

### 7.4 マイクロサービスアーキテクチャへの移行

* 目的: スケーラビリティ、独立したデプロイ
* 技術: Docker、Kubernetes、Jakarta EE + MicroProfile
* 構成:
  * Person Serviceを独立したマイクロサービスとして切り出す
  * API Gatewayを使用してルーティング
  * サービス間通信はREST APIまたはメッセージング

## 8. セキュリティ考慮事項

### 8.1 データベース接続のセキュリティ

* 接続情報の保護:
  * データソース設定はアプリケーションサーバーの設定ファイルに記載
  * パスワードは暗号化またはシークレット管理ツール（Vault等）で管理することを推奨

* 接続暗号化:
  * 必要に応じてJDBC接続のSSL/TLS暗号化を有効化

### 8.2 入力検証

* XSS（Cross-Site Scripting）対策:
  * JSFのデフォルトエスケープ機能を使用
  * <h:outputText>は自動的にHTMLエスケープを行う

* SQLインジェクション対策:
  * JPA（JPQL、Named Parameter）を使用
  * PreparedStatementによるパラメータバインディング
  * 動的なSQLは使用しない

### 8.3 CSRF（Cross-Site Request Forgery）対策

* JSFのCSRFトークン:
  * <h:form>は自動的にjakarta.faces.ViewState hidden fieldを生成
  * サーバー側でトークンを検証

## 9. 参考資料

* [システム要件定義](requirements.md) - システム要件
* [アーキテクチャ設計書](architecture_design.md) - システム全体のアーキテクチャ
* [データモデル](data_model.md) - データベーススキーマの詳細
* [機能設計書](functional_design.md) - 画面遷移とコンポーネント設計
* [振る舞い仕様書](behaviors.md) - システム全体の振る舞い
