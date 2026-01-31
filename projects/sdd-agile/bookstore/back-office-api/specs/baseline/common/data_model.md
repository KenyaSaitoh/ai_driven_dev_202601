# back-office-api - データモデル仕様書（共通）

プロジェクトID: back-office-api  
バージョン: 2.0.0  
最終更新日: 2026-01-31  
ステータス: 確定

---

## 1. 概要

本ドキュメントは、Books Stock API（back-office-api）のデータベーススキーマ（RDB論理設計）を記述する（共通SPEC）。アジャイル構成では common/ に配置し、機能仕様は usecases/{名}/ を参照する。

* データベース種別: HSQLDB
* JPAエンティティの設計（@Entity、@Column等）は詳細設計で実施。データソース設定は architecture_design.md 参照。

---

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

---

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

* 制約: PRIMARY KEY BOOK_ID, FOREIGN KEY CATEGORY_ID → CATEGORY(CATEGORY_ID), PUBLISHER_ID → PUBLISHER(PUBLISHER_ID)
* インデックス: IDX_BOOK_CATEGORY(CATEGORY_ID), IDX_BOOK_PUBLISHER(PUBLISHER_ID), IDX_BOOK_DELETED(DELETED)

### 3.2 STOCK（在庫マスタ）

書籍の在庫情報を管理するテーブル。楽観的ロック対応。

| カラム名 | データ型 | NULL | キー | 説明 |
|---------|---------|------|------|------|
| BOOK_ID | INTEGER | NO | PK, FK | 書籍ID |
| QUANTITY | INTEGER | YES | - | 在庫数 |
| VERSION | BIGINT | YES | - | バージョン（楽観的ロック用） |

* 制約: PRIMARY KEY BOOK_ID, FOREIGN KEY BOOK_ID → BOOK(BOOK_ID)
* BOOKと1:1。VERSIONはJPAの`@Version`で自動管理。更新競合時は`OptimisticLockException`→409 Conflict

### 3.3 CATEGORY（カテゴリマスタ）

| カラム名 | データ型 | NULL | キー | 説明 |
|---------|---------|------|------|------|
| CATEGORY_ID | INTEGER | NO | PK | カテゴリID |
| CATEGORY_NAME | VARCHAR | YES | - | カテゴリ名 |

* 制約: PRIMARY KEY CATEGORY_ID

### 3.4 PUBLISHER（出版社マスタ）

| カラム名 | データ型 | NULL | キー | 説明 |
|---------|---------|------|------|------|
| PUBLISHER_ID | INTEGER | NO | PK | 出版社ID |
| PUBLISHER_NAME | VARCHAR | YES | - | 出版社名 |

* 制約: PRIMARY KEY PUBLISHER_ID

### 3.5 EMPLOYEE（社員マスタ）

社員情報を管理。認証に使用。

| カラム名 | データ型 | NULL | キー | 説明 |
|---------|---------|------|------|------|
| EMPLOYEE_ID | BIGINT | NO | PK | 社員ID |
| EMPLOYEE_CODE | VARCHAR | YES | UK | 社員コード（ログインID） |
| EMPLOYEE_NAME | VARCHAR | YES | - | 社員名 |
| EMAIL | VARCHAR | YES | - | メールアドレス |
| PASSWORD | VARCHAR | YES | - | パスワード（BCryptハッシュ） |
| JOB_RANK | INTEGER | YES | - | 職務ランク（1:ASSOCIATE, 2:MANAGER, 3:DIRECTOR） |
| DEPARTMENT_ID | BIGINT | YES | FK | 部署ID |

* 制約: PRIMARY KEY EMPLOYEE_ID, UNIQUE EMPLOYEE_CODE, FOREIGN KEY DEPARTMENT_ID → DEPARTMENT(DEPARTMENT_ID)
* PASSWORDはBCryptでハッシュ化して保存

### 3.6 DEPARTMENT（部署マスタ）

| カラム名 | データ型 | NULL | キー | 説明 |
|---------|---------|------|------|------|
| DEPARTMENT_ID | BIGINT | NO | PK | 部署ID |
| DEPARTMENT_NAME | VARCHAR | YES | - | 部署名 |

* 制約: PRIMARY KEY DEPARTMENT_ID

### 3.7 WORKFLOW（ワークフロー履歴）

ワークフローの操作履歴。同一WORKFLOW_IDで複数行が履歴として保持される。

| カラム名 | データ型 | NULL | キー | 説明 |
|---------|---------|------|------|------|
| OPERATION_ID | BIGINT | NO | PK | 操作ID（自動採番） |
| WORKFLOW_ID | BIGINT | YES | - | ワークフローID（複数行で共通） |
| WORKFLOW_TYPE | VARCHAR | YES | - | ADD_NEW_BOOK, REMOVE_BOOK, ADJUST_BOOK_PRICE |
| STATE | VARCHAR | YES | - | CREATED, APPLIED, APPROVED |
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
| OPERATION_TYPE | VARCHAR | YES | - | CREATE, APPLY, APPROVE, REJECT |
| OPERATED_BY | BIGINT | YES | FK | 操作者ID |
| OPERATED_AT | TIMESTAMP | YES | - | 操作日時 |
| OPERATION_REASON | VARCHAR | YES | - | 操作理由（承認・却下時） |

* 制約: PRIMARY KEY OPERATION_ID, FOREIGN KEY BOOK_ID→BOOK, CATEGORY_ID→CATEGORY, PUBLISHER_ID→PUBLISHER, OPERATED_BY→EMPLOYEE
* インデックス: IDX_WORKFLOW_ID, IDX_WORKFLOW_STATE, IDX_WORKFLOW_TYPE, IDX_WORKFLOW_OPERATED_BY

---

## 4. エンティティ・リレーション（参考）

| エンティティ | テーブル | 備考 |
|-------------|---------|------|
| Book | BOOK + STOCK | @SecondaryTable で結合可 |
| Stock | STOCK | 在庫情報 |
| Category | CATEGORY | カテゴリマスタ |
| Publisher | PUBLISHER | 出版社マスタ |
| Employee | EMPLOYEE | 社員マスタ |
| Department | DEPARTMENT | 部署マスタ |
| Workflow | WORKFLOW | ワークフロー履歴 |

* Book ↔ Category / Publisher: Many-to-One
* Book ↔ Stock: One-to-One（同一エンティティ内 @SecondaryTable 可）
* Employee ↔ Department: Many-to-One（認証レスポンスに部署を含める場合は EAGER 等を検討）
* Workflow ↔ Book, Category, Publisher, Employee: Many-to-One

---

## 5. データ制約・ライフサイクル

* **NOT NULL**: 全PK、EMPLOYEE.EMPLOYEE_CODE
* **UNIQUE**: EMPLOYEE.EMPLOYEE_CODE
* **CHECK**: JOB_RANK 1–3, STATE CREATED/APPLIED/APPROVED, WORKFLOW_TYPE/OPERATION_TYPE 規定値, QUANTITY≥0, PRICE≥0
* **論理削除**: BOOK.DELETED。論理削除書籍は一覧・検索から除外。物理削除は行わない
* **履歴**: WORKFLOW は全操作履歴を保持（監査）
* **機密**: EMPLOYEE.PASSWORD は BCrypt、EMAIL は個人情報として扱う

---

## 6. 参考資料（アジャイル構成）

* [architecture_design.md](architecture_design.md) - アーキテクチャ設計書
* [external_interface.md](external_interface.md) - 外部インターフェース（本システムは外部呼び出しなし）
* usecases/{名}/userstory.md - 各ユースケースのユーザーストーリー・受入基準
* usecases/{名}/behaviors.md - 各ユースケースの振る舞い仕様書
