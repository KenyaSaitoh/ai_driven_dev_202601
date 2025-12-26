# berry-books - 外部インターフェース仕様書

**プロジェクトID:** berry-books  
**バージョン:** 1.2.0  
**最終更新日:** 2025-12-16  
**ステータス:** REST API連携追加

---

## 1. 概要

本システムは、顧客管理機能について、berry-books-restプロジェクトが提供するREST APIを経由してCUSTOMERテーブルにアクセスします。直接的なデータベースアクセスは行いません。

---

## 2. 外部システム連携一覧

### 2.1 berry-books-rest API（顧客管理）

**連携有無:** あり

**連携先:** berry-books-rest プロジェクト

**連携目的:** CUSTOMERテーブルへのアクセスをREST API経由で行う

**連携方式:**
- 通信方式: REST API (JAX-RS)
- データ形式: JSON (JSON-B)
- 認証方式: なし（同一アプリケーションサーバー内）

**連携タイミング:**
- リアルタイム（顧客情報取得、認証、登録時）

**送信データ:**
- GET /customers/{customerId}: 顧客ID
- GET /customers/query_email?email={email}: メールアドレス
- POST /customers/: CustomerTO (customerName, email, birthday, address)

**受信データ:**
- CustomerTO: customerId, customerName, email, birthday, address

**エラー処理:**
- 404 Not Found: 顧客が見つからない場合
- 409 Conflict: メールアドレス重複の場合
- 500 Internal Server Error: その他のエラー

**API ベースURL:**
- http://localhost:8080/berry-books-rest

**主要エンドポイント:**
- `GET /customers/{customerId}` - 顧客取得（主キー検索）
- `GET /customers/query_email?email={email}` - 顧客取得（メールアドレス検索、ログイン用）
- `POST /customers/` - 顧客新規登録

### 2.2 決済システム

**連携有無:** なし

### 2.3 配送業者システム

**連携有無:** なし

### 2.4 メール配信サービス

**連携有無:** なし

### 2.5 外部マスタ連携

**連携有無:** なし

### 2.6 会計システム

**連携有無:** なし

---

## 3. 連携インターフェース仕様書（テンプレート）

外部システム連携が発生する場合、以下の観点で仕様書を記載する。

### 3.X [外部システム名]

**連携先:** [システム名]

**連携目的:** [何のために連携するか]

**連携方式:**
- 通信方式: [REST API / SOAP / ファイル連携 等]
- データ形式: [JSON / XML / CSV 等]
- 認証方式: [API Key / OAuth / Basic認証 等]

**連携タイミング:**
- [リアルタイム / バッチ（日次/月次） / イベント駆動 等]

**送信データ:**
- [データ項目一覧]

**受信データ:**
- [データ項目一覧]

**エラー処理:**
- [タイムアウト / リトライ / 例外ハンドリング の方針]

---

## 4. 参考資料

- [requirements.md](requirements.md) - 要件定義書
- [architecture_design.md](architecture_design.md) - アーキテクチャ設計書


