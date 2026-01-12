# データモデル設計書

## 1. 概要

本ドキュメントは、Books Stock API - バックオフィス書籍在庫管理システムのデータモデルを定義する。

## 2. ER図

```mermaid
erDiagram
    DEPARTMENT ||--o{ EMPLOYEE : "所属"
    EMPLOYEE ||--o{ WORKFLOW : "操作"
    CATEGORY ||--o{ BOOK : "分類"
    PUBLISHER ||--o{ BOOK : "出版"
    BOOK ||--|| STOCK : "1:1"
    BOOK ||--o{ WORKFLOW : "対象"
    CATEGORY ||--o{ WORKFLOW : "対象分類"
    PUBLISHER ||--o{ WORKFLOW : "対象出版社"

    DEPARTMENT {
        BIGINT DEPARTMENT_ID PK "部署ID"
        VARCHAR DEPARTMENT_NAME "部署名"
    }

    EMPLOYEE {
        BIGINT EMPLOYEE_ID PK "社員ID"
        VARCHAR EMPLOYEE_CODE UK "社員コード"
        VARCHAR EMPLOYEE_NAME "社員名"
        VARCHAR EMAIL "メールアドレス"
        VARCHAR PASSWORD "パスワード(BCrypt)"
        INTEGER JOB_RANK "職務ランク(1-3)"
        BIGINT DEPARTMENT_ID FK "部署ID"
    }
    
    CATEGORY {
        INTEGER CATEGORY_ID PK "カテゴリID"
        VARCHAR CATEGORY_NAME "カテゴリ名"
    }

    PUBLISHER {
        INTEGER PUBLISHER_ID PK "出版社ID"
        VARCHAR PUBLISHER_NAME "出版社名"
    }
    
    BOOK {
        INTEGER BOOK_ID PK "書籍ID"
        VARCHAR BOOK_NAME "書籍名"
        VARCHAR AUTHOR "著者"
        INTEGER CATEGORY_ID FK "カテゴリID"
        INTEGER PUBLISHER_ID FK "出版社ID"
        DECIMAL PRICE "価格"
        VARCHAR IMAGE_URL "画像URL"
        BOOLEAN DELETED "削除フラグ"
    }
    
    STOCK {
        INTEGER BOOK_ID PK_FK "書籍ID"
        INTEGER QUANTITY "在庫数"
        BIGINT VERSION "バージョン(楽観的ロック)"
    }

    WORKFLOW {
        BIGINT OPERATION_ID PK "操作ID"
        BIGINT WORKFLOW_ID "ワークフローID"
        VARCHAR WORKFLOW_TYPE "ワークフロータイプ"
        VARCHAR STATE "状態"
        INTEGER BOOK_ID FK "対象書籍ID"
        VARCHAR BOOK_NAME "書籍名"
        VARCHAR AUTHOR "著者"
        INTEGER CATEGORY_ID FK "カテゴリID"
        INTEGER PUBLISHER_ID FK "出版社ID"
        DECIMAL PRICE "価格"
        VARCHAR IMAGE_URL "画像URL"
        VARCHAR APPLY_REASON "申請理由"
        DATE START_DATE "適用開始日"
        DATE END_DATE "適用終了日"
        VARCHAR OPERATION_TYPE "操作タイプ"
        BIGINT OPERATED_BY FK "操作者ID"
        TIMESTAMP OPERATED_AT "操作日時"
        VARCHAR OPERATION_REASON "操作理由"
    }
```

## 3. テーブル定義

### 3.1 BOOK（書籍マスタ）

書籍の基本情報を管理するテーブル。

| カラム名 | データ型 | NULL | キー | 説明 |
|---------|---------|------|------|------|
| BOOK_ID | INTEGER | NO | PK | 書籍ID（自動採番） |
| BOOK_NAME | VARCHAR | YES | - | 書籍名 |
| AUTHOR | VARCHAR | YES | - | 著者名 |
| CATEGORY_ID | INTEGER | YES | FK | カテゴリID |
| PUBLISHER_ID | INTEGER | YES | FK | 出版社ID |
| PRICE | DECIMAL | YES | - | 価格 |
| IMAGE_URL | VARCHAR | YES | - | 画像URL |
| DELETED | BOOLEAN | YES | - | 削除フラグ（論理削除） |

* 制約:
  * PRIMARY KEY: BOOK_ID
  * FOREIGN KEY: CATEGORY_ID → CATEGORY(CATEGORY_ID)
  * FOREIGN KEY: PUBLISHER_ID → PUBLISHER(PUBLISHER_ID)

* インデックス:
  * IDX_BOOK_CATEGORY: CATEGORY_ID
  * IDX_BOOK_PUBLISHER: PUBLISHER_ID
  * IDX_BOOK_DELETED: DELETED

### 3.2 STOCK（在庫マスタ）

書籍の在庫情報を管理するテーブル。楽観的ロック対応。

| カラム名 | データ型 | NULL | キー | 説明 |
|---------|---------|------|------|------|
| BOOK_ID | INTEGER | NO | PK, FK | 書籍ID |
| QUANTITY | INTEGER | YES | - | 在庫数 |
| VERSION | BIGINT | YES | - | バージョン（楽観的ロック用） |

* 制約:
  * PRIMARY KEY: BOOK_ID
  * FOREIGN KEY: BOOK_ID → BOOK(BOOK_ID)

* 備考:
  * BOOKテーブルと1:1の関係
  * VERSIONカラムはJPAの`@Version`アノテーションで自動管理
  * 更新時にバージョンが一致しない場合、`OptimisticLockException`が発生

### 3.3 CATEGORY（カテゴリマスタ）

書籍のカテゴリ情報を管理するマスタテーブル。

| カラム名 | データ型 | NULL | キー | 説明 |
|---------|---------|------|------|------|
| CATEGORY_ID | INTEGER | NO | PK | カテゴリID |
| CATEGORY_NAME | VARCHAR | YES | - | カテゴリ名 |

* 制約:
  * PRIMARY KEY: CATEGORY_ID

* データ例:
  * 1: 文学
  * 2: ビジネス
  * 3: 技術
  * 4: 歴史

### 3.4 PUBLISHER（出版社マスタ）

出版社情報を管理するマスタテーブル。

| カラム名 | データ型 | NULL | キー | 説明 |
|---------|---------|------|------|------|
| PUBLISHER_ID | INTEGER | NO | PK | 出版社ID |
| PUBLISHER_NAME | VARCHAR | YES | - | 出版社名 |

* 制約:
  * PRIMARY KEY: PUBLISHER_ID

### 3.5 EMPLOYEE（社員マスタ）

社員情報を管理するテーブル。認証に使用。

| カラム名 | データ型 | NULL | キー | 説明 |
|---------|---------|------|------|------|
| EMPLOYEE_ID | BIGINT | NO | PK | 社員ID |
| EMPLOYEE_CODE | VARCHAR | YES | UK | 社員コード（ログインID） |
| EMPLOYEE_NAME | VARCHAR | YES | - | 社員名 |
| EMAIL | VARCHAR | YES | - | メールアドレス |
| PASSWORD | VARCHAR | YES | - | パスワード（BCryptハッシュ） |
| JOB_RANK | INTEGER | YES | - | 職務ランク（1:ASSOCIATE, 2:MANAGER, 3:DIRECTOR） |
| DEPARTMENT_ID | BIGINT | YES | FK | 部署ID |

* 制約:
  * PRIMARY KEY: EMPLOYEE_ID
  * UNIQUE KEY: EMPLOYEE_CODE
  * FOREIGN KEY: DEPARTMENT_ID → DEPARTMENT(DEPARTMENT_ID)

* インデックス:
  * UK_EMPLOYEE_CODE: EMPLOYEE_CODE (UNIQUE)
  * IDX_EMPLOYEE_DEPT: DEPARTMENT_ID

* 備考:
  * PASSWORDはBCryptでハッシュ化して保存
  * 開発環境では平文パスワードもサポート（本番環境では非推奨）

### 3.6 DEPARTMENT（部署マスタ）

部署情報を管理するマスタテーブル。

| カラム名 | データ型 | NULL | キー | 説明 |
|---------|---------|------|------|------|
| DEPARTMENT_ID | BIGINT | NO | PK | 部署ID |
| DEPARTMENT_NAME | VARCHAR | YES | - | 部署名 |

* 制約:
  * PRIMARY KEY: DEPARTMENT_ID

* データ例:
  * 1: 営業部
  * 2: 管理部
  * 3: 物流部

### 3.7 WORKFLOW（ワークフロー履歴）

ワークフローの操作履歴を管理するテーブル。1つのワークフローに対して複数の操作履歴行を保持。

| カラム名 | データ型 | NULL | キー | 説明 |
|---------|---------|------|------|------|
| OPERATION_ID | BIGINT | NO | PK | 操作ID（主キー、自動採番） |
| WORKFLOW_ID | BIGINT | YES | - | ワークフローID（複数行で共通） |
| WORKFLOW_TYPE | VARCHAR | YES | - | ワークフロータイプ（ADD_NEW_BOOK, REMOVE_BOOK, ADJUST_BOOK_PRICE） |
| STATE | VARCHAR | YES | - | 状態（CREATED, APPLIED, APPROVED） |
| BOOK_ID | INTEGER | YES | FK | 対象書籍ID |
| BOOK_NAME | VARCHAR | YES | - | 書籍名（新規追加時） |
| AUTHOR | VARCHAR | YES | - | 著者（新規追加時） |
| CATEGORY_ID | INTEGER | YES | FK | カテゴリID |
| PUBLISHER_ID | INTEGER | YES | FK | 出版社ID |
| PRICE | DECIMAL | YES | - | 価格 |
| IMAGE_URL | VARCHAR | YES | - | 画像URL |
| APPLY_REASON | VARCHAR | YES | - | 申請理由 |
| START_DATE | DATE | YES | - | 適用開始日（価格改定時） |
| END_DATE | DATE | YES | - | 適用終了日（価格改定時） |
| OPERATION_TYPE | VARCHAR | YES | - | 操作タイプ（CREATE, APPLY, APPROVE, REJECT） |
| OPERATED_BY | BIGINT | YES | FK | 操作者ID |
| OPERATED_AT | TIMESTAMP | YES | - | 操作日時 |
| OPERATION_REASON | VARCHAR | YES | - | 操作理由（承認・却下時） |

* 制約:
  * PRIMARY KEY: OPERATION_ID
  * FOREIGN KEY: BOOK_ID → BOOK(BOOK_ID)
  * FOREIGN KEY: CATEGORY_ID → CATEGORY(CATEGORY_ID)
  * FOREIGN KEY: PUBLISHER_ID → PUBLISHER(PUBLISHER_ID)
  * FOREIGN KEY: OPERATED_BY → EMPLOYEE(EMPLOYEE_ID)

* インデックス:
  * IDX_WORKFLOW_ID: WORKFLOW_ID
  * IDX_WORKFLOW_STATE: STATE
  * IDX_WORKFLOW_TYPE: WORKFLOW_TYPE
  * IDX_WORKFLOW_OPERATED_BY: OPERATED_BY

* 備考:
  * 同じWORKFLOW_IDを持つ複数の行が履歴として保存される
  * 最新の状態は最大のOPERATION_IDまたは最新のOPERATED_ATを持つ行
  * 監査ログとして全操作履歴を保持

## 4. エンティティ関連図（JPA）

### 4.1 エンティティクラスとテーブルのマッピング

| エンティティクラス | テーブル | 説明 |
|------------------|---------|------|
| Book | BOOK + STOCK | `@SecondaryTable`で結合 |
| Stock | STOCK | 在庫情報 |
| Category | CATEGORY | カテゴリマスタ |
| Publisher | PUBLISHER | 出版社マスタ |
| Employee | EMPLOYEE | 社員マスタ |
| Department | DEPARTMENT | 部署マスタ |
| Workflow | WORKFLOW | ワークフロー履歴 |

### 4.2 リレーションシップ

#### Book ↔ Category
* 関係: Many-to-One
* Book側: `@ManyToOne`
* 外部キー: BOOK.CATEGORY_ID → CATEGORY.CATEGORY_ID

#### Book ↔ Publisher
* 関係: Many-to-One
* Book側: `@ManyToOne`
* 外部キー: BOOK.PUBLISHER_ID → PUBLISHER.PUBLISHER_ID

#### Book ↔ Stock
* 関係: One-to-One（同一エンティティ内で`@SecondaryTable`）
* Book側: `@SecondaryTable(name = "STOCK")`
* BOOKとSTOCKはBOOK_IDで結合

#### Employee ↔ Department
* 関係: Many-to-One
* Employee側: `@ManyToOne(fetch = FetchType.EAGER)`
* 外部キー: EMPLOYEE.DEPARTMENT_ID → DEPARTMENT.DEPARTMENT_ID
* フェッチタイプ: EAGER（即時ロード）
  * 理由: 認証APIのログインレスポンスに部署情報（departmentId, departmentName）を含める必要があるため
  * LAZY（遅延ロード）を使用すると、JSON-B シリアライゼーション時に「Generating incomplete JSON」エラーが発生します

#### Department ↔ Employee
* 関係: One-to-Many
* Department側: `@OneToMany(mappedBy = "department")`
* 双方向関係

#### Workflow ↔ Book
* 関係: Many-to-One
* Workflow側: `@ManyToOne`（`insertable=false, updatable=false`）
* 外部キー: WORKFLOW.BOOK_ID → BOOK.BOOK_ID

#### Workflow ↔ Category
* 関係: Many-to-One
* Workflow側: `@ManyToOne`（`insertable=false, updatable=false`）
* 外部キー: WORKFLOW.CATEGORY_ID → CATEGORY.CATEGORY_ID

#### Workflow ↔ Publisher
* 関係: Many-to-One
* Workflow側: `@ManyToOne`（`insertable=false, updatable=false`）
* 外部キー: WORKFLOW.PUBLISHER_ID → PUBLISHER.PUBLISHER_ID

#### Workflow ↔ Employee
* 関係: Many-to-One
* Workflow側: `@ManyToOne`（`insertable=false, updatable=false`）
* 外部キー: WORKFLOW.OPERATED_BY → EMPLOYEE.EMPLOYEE_ID

## 5. データ型マッピング

### 5.1 Java型 ↔ SQL型

| Java型 | SQL型 | 用途 |
|--------|------|------|
| Integer | INTEGER | ID、数値 |
| Long | BIGINT | 大きなID、バージョン |
| String | VARCHAR | 文字列 |
| BigDecimal | DECIMAL | 金額、価格 |
| Boolean | BOOLEAN | フラグ |
| LocalDate | DATE | 日付 |
| LocalDateTime | TIMESTAMP | 日時 |

## 6. データ制約

### 6.1 NOT NULL制約

* 必須項目:
  * すべてのPRIMARY KEY
  * EMPLOYEE.EMPLOYEE_CODE
  * その他のフィールドは基本的にNULL許可（柔軟性のため）

### 6.2 UNIQUE制約

* 一意性保証:
  * EMPLOYEE.EMPLOYEE_CODE（ログインIDとして使用）

### 6.3 CHECK制約

* JOB_RANK: 1, 2, 3のいずれか
* STATE: 'CREATED', 'APPLIED', 'APPROVED'のいずれか
* WORKFLOW_TYPE: 'ADD_NEW_BOOK', 'REMOVE_BOOK', 'ADJUST_BOOK_PRICE'のいずれか
* OPERATION_TYPE: 'CREATE', 'APPLY', 'APPROVE', 'REJECT'のいずれか
* QUANTITY: 0以上
* PRICE: 0以上

### 6.4 外部キー制約

* すべての外部キー参照は制約として定義:
  * ON DELETE: NO ACTION（参照整合性保証）
  * ON UPDATE: CASCADE（ID更新時の連鎖更新）

* テーブル削除時の注意事項:
  * テーブルをDROP する際は、外部キー制約のため、以下のいずれかの対応が必要です：
    1. CASCADE オプション: `DROP TABLE テーブル名 IF EXISTS CASCADE;` を使用
       * 依存する外部キー制約も同時に削除されます
       * 初期化スクリプト（`1_BACK_OFFICE_DROP.sql`）ではこの方式を採用
    2. 削除順序の制御: 外部キーを持つテーブルから先に削除
       * 例: WORKFLOW → STOCK → BOOK → CATEGORY/PUBLISHER
       * 例: EMPLOYEE → DEPARTMENT

* 推奨: 開発環境のスキーマ初期化では、`CASCADE` オプションの使用を推奨します。本番環境では誤削除防止のため、削除順序を制御する方式も検討してください。

## 7. インデックス設計

### 7.1 主キーインデックス
* すべてのテーブルのPRIMARY KEYに自動作成

### 7.2 外部キーインデックス
* すべての外部キーカラムにインデックス作成（JOIN性能向上）

### 7.3 検索用インデックス
* BOOK.CATEGORY_ID: カテゴリ別検索
* BOOK.PUBLISHER_ID: 出版社別検索
* BOOK.DELETED: 論理削除フィルタ
* WORKFLOW.WORKFLOW_ID: 履歴取得
* WORKFLOW.STATE: 状態別検索
* WORKFLOW.WORKFLOW_TYPE: タイプ別検索
* WORKFLOW.OPERATED_BY: 操作者別検索

## 8. データライフサイクル

### 8.1 論理削除
* BOOK: DELETEDフラグで論理削除を実施
* 論理削除された書籍はAPIレスポンス（一覧・検索）から除外される
* 書籍詳細取得APIでは論理削除された書籍も参照可能
* 物理削除は実施しない（データ保持・履歴保持のため）

### 8.2 履歴保持
* WORKFLOW: 全操作履歴を永続的に保持（監査目的）

## 9. データセキュリティ

### 9.1 機密データ
* EMPLOYEE.PASSWORD: BCryptハッシュで保存（不可逆変換）
* EMPLOYEE.EMAIL: 個人情報として扱う

### 9.2 監査ログ
* WORKFLOW: 全操作履歴を保持
  * 操作者（OPERATED_BY）
  * 操作日時（OPERATED_AT）
  * 操作内容（OPERATION_TYPE, OPERATION_REASON）
