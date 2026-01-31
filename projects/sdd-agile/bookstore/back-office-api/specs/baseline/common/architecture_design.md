# back-office-api - アーキテクチャ設計書（共通）

プロジェクトID: back-office-api  
バージョン: 2.0.0  
最終更新日: 2026-01-31  
ステータス: REST API アーキテクチャ確定

---

## 概要

このドキュメントは、back-office-apiプロジェクト固有のアーキテクチャ設計を記述する（アジャイル共通SPEC）。

* 共通的な技術スタック、開発ガイドライン、技術的対応方針については、以下を参照すること：
  * [architecture.md](../../../../../agent_skills/jakarta-ee-api-agile/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [common_rules.md](../../../../../agent_skills/jakarta-ee-api-agile/principles/common_rules.md) - 共通ルール
  * [security.md](../../../../../agent_skills/jakarta-ee-api-agile/principles/security.md) - セキュリティ標準

---

## 1. システム概要

* システム名: Books Stock API - バックオフィス書籍在庫管理システム
* アーキテクチャ: レイヤードアーキテクチャ、RESTful API、JPA、JWT認証
* 外部連携: なし（独立したバックエンドサービス）

---

## 2. レイヤー・パッケージ

* Presentation: AuthenResource, BookResource, CategoryResource, PublisherResource, StockResource, WorkflowResource
* Security: JwtAuthenFilter, JwtUtil
* Business: 各Service（BookService, CategoryService, PublisherService, StockService, WorkflowService 等）
* Data Access: 各Dao（BookDao, CategoryDao, PublisherDao, StockDao, WorkflowDao 等）
* Persistence: Entity（Book, Category, Publisher, Stock, Workflow, Employee, Department 等）

* ベースパッケージ: pro.kensait.（例: pro.kensait.backoffice.api）。各ユースケースの詳細設計: detailed_design/usecases/{名}/ を参照。

---

## 3. 認証・トランザクション・楽観的ロック

* 認証: JWT（HttpOnly Cookie）。認証除外: /api/auth/login, /api/auth/logout
* トランザクション: Service 層で @Transactional
* 楽観的ロック: STOCK テーブルに @Version。更新競合時は 409 Conflict（OptimisticLockExceptionMapper）

---

## 4. 参考資料（アジャイル構成）

* [data_model.md](data_model.md) - データモデル仕様書
* [external_interface.md](external_interface.md) - 外部インターフェース仕様書
* usecases/{名}/userstory.md - 各ユースケースのユーザーストーリー・受入基準
* usecases/{名}/behaviors.md - 各ユースケースの振る舞い仕様書
* agent_skills/jakarta-ee-api-agile/principles/ - 共通原則・標準
