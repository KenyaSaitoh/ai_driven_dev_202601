# berry-books-api-sdd-agile - データモデル仕様書（共通）

プロジェクトID: berry-books-api-sdd-agile  
バージョン: 3.0.0  
最終更新日: 2026-01-10  
ステータス: サービス分離アーキテクチャ対応完了

* 変更履歴:
  * v3.0.0 (2026-01-10): サービス分離アーキテクチャ対応
  * 本ファイルはアジャイル構成の common/ に配置。機能仕様は usecases/{名}/ を参照。

---

## 1. 概要

本文書は、berry-books-api REST APIのデータベーススキーマ（RDB論理設計）を記述する。

* データベース種別: HSQLDB

注意: 
* JPAエンティティクラスの設計（@Entity、@Column等のアノテーション、Javaクラス構造）は詳細設計フェーズで実施します
* データソース設定（JNDI名、接続URL、接続プール等）はarchitecture_design.mdに記載します
* berry-books-apiは注文データ（ORDER_TRAN、ORDER_DETAIL）のみを直接管理します。書籍・在庫・カテゴリ・顧客データは外部APIを通じてアクセスします

---

## 2. ER図

### 2.1 全体ER図

```mermaid
erDiagram
    ORDER_TRAN ||--o{ ORDER_DETAIL : contains

    ORDER_TRAN {
        int ORDER_TRAN_ID PK
        date ORDER_DATE
        int CUSTOMER_ID "論理参照のみ（customer-hub-api）"
        int TOTAL_PRICE
        int DELIVERY_PRICE
        varchar DELIVERY_ADDRESS
        int SETTLEMENT_TYPE "1=Bank, 2=Credit, 3=COD"
    }

    ORDER_DETAIL {
        int ORDER_TRAN_ID PK_FK
        int ORDER_DETAIL_ID PK
        int BOOK_ID "論理参照のみ（back-office-api）"
        varchar(100) BOOK_NAME "スナップショット"
        varchar(50) PUBLISHER_NAME "スナップショット"
        int PRICE "スナップショット"
        int COUNT
    }
```

### 2.2 他のサービスのテーブル構造（参考）

* back-office-api 管理テーブル: BOOK, STOCK, CATEGORY, PUBLISHER（本サービスでは管理しない）
* customer-hub-api 管理テーブル: CUSTOMER（本サービスでは管理しない）。詳細は [external_interface.md](external_interface.md) 参照。

---

## 3. テーブル定義（berry-books-api 管理分）

### 3.1 ORDER_TRAN（注文トランザクション）

#### 3.1.1 テーブル概要

注文トランザクションテーブル。顧客の注文情報を管理する。

#### 3.1.2 テーブル構造

| カラム名 | データ型 | PK | FK | NN | UQ | デフォルト | 説明 |
|---------|---------|----|----|----|----|----------|------|
| ORDER_TRAN_ID | INTEGER | ✓ | | ✓ | | IDENTITY | 注文トランザクションID |
| ORDER_DATE | DATE | | | ✓ | | | 注文日 |
| CUSTOMER_ID | INT | | | ✓ | | | 顧客ID（論理参照のみ） |
| TOTAL_PRICE | INT | | | ✓ | | | 注文金額合計（配送料を含む） |
| DELIVERY_PRICE | INT | | | ✓ | | | 配送料金 |
| DELIVERY_ADDRESS | VARCHAR(30) | | | ✓ | | | 配送先住所 |
| SETTLEMENT_TYPE | INT | | | ✓ | | | 決済方法（1:銀行振込, 2:クレジットカード, 3:着払い） |

#### 3.1.3 決済方法（SETTLEMENT_TYPE）

| 値 | 説明 |
|---|------|
| 1 | 銀行振込 |
| 2 | クレジットカード |
| 3 | 着払い |

---

### 3.2 ORDER_DETAIL（注文明細）

#### 3.2.1 テーブル概要

注文明細テーブル。複合主キー（ORDER_TRAN_ID, ORDER_DETAIL_ID）を使用する。

#### 3.2.2 テーブル構造

| カラム名 | データ型 | PK | FK | NN | UQ | デフォルト | 説明 |
|---------|---------|----|----|----|----|----------|------|
| ORDER_TRAN_ID | INT | ✓ | ✓ | ✓ | | | 注文トランザクションID |
| ORDER_DETAIL_ID | INT | ✓ | | ✓ | | | 注文明細ID（注文内で一意） |
| BOOK_ID | INT | | | ✓ | | | 書籍ID（論理参照のみ） |
| BOOK_NAME | VARCHAR(100) | | | ✓ | | | 書籍名（スナップショット） |
| PUBLISHER_NAME | VARCHAR(50) | | | ✓ | | | 出版社名（スナップショット） |
| PRICE | INT | | | ✓ | | | 価格（スナップショット） |
| COUNT | INT | | | ✓ | | | 注文数 |

---

## 4. インデックス設計

| テーブル | インデックス名 | カラム | タイプ | 目的 |
|---------|--------------|--------|--------|------|
| ORDER_TRAN | PK_ORDER_TRAN | ORDER_TRAN_ID | PRIMARY KEY | 主キー |
| ORDER_TRAN | IDX_CUSTOMER_ID | CUSTOMER_ID | INDEX | 顧客別注文履歴検索 |
| ORDER_DETAIL | PK_ORDER_DETAIL | (ORDER_TRAN_ID, ORDER_DETAIL_ID) | PRIMARY KEY | 複合主キー |

---

## 5. データ整合性ルール

* 子テーブル ORDER_DETAIL の ORDER_TRAN_ID → ORDER_TRAN.ORDER_TRAN_ID（CASCADE）
* トランザクション分離レベル: READ_COMMITTED
* 在庫更新は外部API（back-office-api）側で楽観的ロック。本サービスは注文データのみ管理。

---

## 6. 参考資料（アジャイル構成）

* [architecture_design.md](architecture_design.md) - アーキテクチャ設計書
* [external_interface.md](external_interface.md) - 外部インターフェース仕様書
* usecases/{名}/userstory.md - 各ユースケースのユーザーストーリー・受入基準
* usecases/{名}/behaviors.md - 各ユースケースの振る舞い仕様書
