# 外部インターフェース仕様書

## 1. 概要

本ドキュメントは、人材管理システム（JSF Person）が外部システムを呼び出す際のインターフェース仕様を定義する。

## 2. 外部システム連携状況

人材管理システム（JSF Person）は、外部システムを呼び出さない独立したWebアプリケーションとして設計されている。

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

### 2.1 外部システム呼び出し

本システムは外部システムを呼び出さない。

---

## 3. 将来の拡張性

### 3.1 REST API呼び出し

* 現状: 外部REST APIの呼び出しはなし
* 将来的な拡張:
  * 郵便番号APIとの連携（住所情報の取得）
  * 認証サービスとの連携（OAuth 2.0）

### 3.2 SOAP Webサービス呼び出し

* 現状: SOAP Webサービスの呼び出しはなし
* 将来的な拡張: 必要に応じて検討

### 3.3 REST API提供

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

### 3.5 メッセージングとの連携

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
  * サービス間通信はREST APIまたはメッセージング

---

## 4. 参考資料

* [システム要件定義](requirements.md) - システム要件
* [アーキテクチャ設計書](architecture_design.md) - システム全体のアーキテクチャ
* [データモデル](data_model.md) - データベーススキーマの詳細
* [機能設計書](functional_design.md) - 画面遷移とコンポーネント設計
* [振る舞い仕様書](behaviors.md) - システム全体の振る舞い
