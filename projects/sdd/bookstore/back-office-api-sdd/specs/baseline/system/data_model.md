# データモデル設計書

## 1. 概要

本ドキュメントは、Books Stock API - バックオフィス書籍在庫管理システムのデータモデルを定義する。

## 2. ER図

```
┌─────────────┐       ┌──────────────┐
│ DEPARTMENT  │       │  CATEGORY    │
│─────────────│       │──────────────│
│ *DEPARTMENT_ID│◄─┐   │ *CATEGORY_ID │
│  DEPARTMENT_NAME│  │   │  CATEGORY_NAME│
└─────────────┘  │   └──────┬───────┘
                 │          │
                 │          │
┌─────────────┐  │          │
│ EMPLOYEE    │  │          │
│─────────────│  │          │
│ *EMPLOYEE_ID│  │          │
│  EMPLOYEE_CODE│  │          │
│  EMPLOYEE_NAME│  │          │
│  EMAIL      │  │          │
│  PASSWORD   │  │          │
│  JOB_RANK   │  │          │
│  DEPARTMENT_ID├──┘          │
└──────┬──────┘             │
       │                    │
       │                    │
       │          ┌─────────▼──────┐      ┌──────────────┐
       │          │     BOOK        │      │  PUBLISHER   │
       │          │─────────────────│      │──────────────│
       │          │ *BOOK_ID        │      │ *PUBLISHER_ID│
       │          │  BOOK_NAME      │      │  PUBLISHER_NAME│
       │          │  AUTHOR         │      └──────┬───────┘
       │          │  CATEGORY_ID    ├─────────────┘
       │          │  PUBLISHER_ID   ├──────────────┘
       │          │  PRICE          │
       │          │  IMAGE_URL      │
       │          │  DELETED        │
       │          └────────┬────────┘
       │                   │
       │                   │ 1:1
       │          ┌────────▼────────┐
       │          │     STOCK       │
       │          │─────────────────│
       │          │ *BOOK_ID        │
       │          │  QUANTITY       │
       │          │  VERSION        │
       │          └────────┬────────┘
       │                   │
       │                   │
       │          ┌────────▼────────┐
       │          │   WORKFLOW      │
       │          │─────────────────│
       │          │ *OPERATION_ID   │
       │          │  WORKFLOW_ID    │
       │          │  WORKFLOW_TYPE  │
       │          │  STATE          │
       │          │  BOOK_ID        │
       │          │  BOOK_NAME      │
       │          │  AUTHOR         │
       │          │  CATEGORY_ID    │
       │          │  PUBLISHER_ID   │
       │          │  PRICE          │
       │          │  IMAGE_URL      │
       │          │  APPLY_REASON   │
       │          │  START_DATE     │
       │          │  END_DATE       │
       │          │  OPERATION_TYPE │
       │          │  OPERATED_BY    │
       │          │  OPERATED_AT    │
       │          │  OPERATION_REASON│
       └──────────┴─────────────────┘
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

**制約**:
- PRIMARY KEY: BOOK_ID
- FOREIGN KEY: CATEGORY_ID → CATEGORY(CATEGORY_ID)
- FOREIGN KEY: PUBLISHER_ID → PUBLISHER(PUBLISHER_ID)

**インデックス**:
- IDX_BOOK_CATEGORY: CATEGORY_ID
- IDX_BOOK_PUBLISHER: PUBLISHER_ID
- IDX_BOOK_DELETED: DELETED

### 3.2 STOCK（在庫マスタ）

書籍の在庫情報を管理するテーブル。楽観的ロック対応。

| カラム名 | データ型 | NULL | キー | 説明 |
|---------|---------|------|------|------|
| BOOK_ID | INTEGER | NO | PK, FK | 書籍ID |
| QUANTITY | INTEGER | YES | - | 在庫数 |
| VERSION | BIGINT | YES | - | バージョン（楽観的ロック用） |

**制約**:
- PRIMARY KEY: BOOK_ID
- FOREIGN KEY: BOOK_ID → BOOK(BOOK_ID)

**備考**:
- BOOKテーブルと1:1の関係
- VERSIONカラムはJPAの`@Version`アノテーションで自動管理
- 更新時にバージョンが一致しない場合、`OptimisticLockException`が発生

### 3.3 CATEGORY（カテゴリマスタ）

書籍のカテゴリ情報を管理するマスタテーブル。

| カラム名 | データ型 | NULL | キー | 説明 |
|---------|---------|------|------|------|
| CATEGORY_ID | INTEGER | NO | PK | カテゴリID |
| CATEGORY_NAME | VARCHAR | YES | - | カテゴリ名 |

**制約**:
- PRIMARY KEY: CATEGORY_ID

**データ例**:
- 1: 文学
- 2: ビジネス
- 3: 技術
- 4: 歴史

### 3.4 PUBLISHER（出版社マスタ）

出版社情報を管理するマスタテーブル。

| カラム名 | データ型 | NULL | キー | 説明 |
|---------|---------|------|------|------|
| PUBLISHER_ID | INTEGER | NO | PK | 出版社ID |
| PUBLISHER_NAME | VARCHAR | YES | - | 出版社名 |

**制約**:
- PRIMARY KEY: PUBLISHER_ID

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

**制約**:
- PRIMARY KEY: EMPLOYEE_ID
- UNIQUE KEY: EMPLOYEE_CODE
- FOREIGN KEY: DEPARTMENT_ID → DEPARTMENT(DEPARTMENT_ID)

**インデックス**:
- UK_EMPLOYEE_CODE: EMPLOYEE_CODE (UNIQUE)
- IDX_EMPLOYEE_DEPT: DEPARTMENT_ID

**備考**:
- PASSWORDはBCryptでハッシュ化して保存
- 開発環境では平文パスワードもサポート（本番環境では非推奨）

### 3.6 DEPARTMENT（部署マスタ）

部署情報を管理するマスタテーブル。

| カラム名 | データ型 | NULL | キー | 説明 |
|---------|---------|------|------|------|
| DEPARTMENT_ID | BIGINT | NO | PK | 部署ID |
| DEPARTMENT_NAME | VARCHAR | YES | - | 部署名 |

**制約**:
- PRIMARY KEY: DEPARTMENT_ID

**データ例**:
- 1: 営業部
- 2: 管理部
- 3: 物流部

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

**制約**:
- PRIMARY KEY: OPERATION_ID
- FOREIGN KEY: BOOK_ID → BOOK(BOOK_ID)
- FOREIGN KEY: CATEGORY_ID → CATEGORY(CATEGORY_ID)
- FOREIGN KEY: PUBLISHER_ID → PUBLISHER(PUBLISHER_ID)
- FOREIGN KEY: OPERATED_BY → EMPLOYEE(EMPLOYEE_ID)

**インデックス**:
- IDX_WORKFLOW_ID: WORKFLOW_ID
- IDX_WORKFLOW_STATE: STATE
- IDX_WORKFLOW_TYPE: WORKFLOW_TYPE
- IDX_WORKFLOW_OPERATED_BY: OPERATED_BY

**備考**:
- 同じWORKFLOW_IDを持つ複数の行が履歴として保存される
- 最新の状態は最大のOPERATION_IDまたは最新のOPERATED_ATを持つ行
- 監査ログとして全操作履歴を保持

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
- 関係: Many-to-One
- Book側: `@ManyToOne`
- 外部キー: BOOK.CATEGORY_ID → CATEGORY.CATEGORY_ID

#### Book ↔ Publisher
- 関係: Many-to-One
- Book側: `@ManyToOne`
- 外部キー: BOOK.PUBLISHER_ID → PUBLISHER.PUBLISHER_ID

#### Book ↔ Stock
- 関係: One-to-One（同一エンティティ内で`@SecondaryTable`）
- Book側: `@SecondaryTable(name = "STOCK")`
- BOOKとSTOCKはBOOK_IDで結合

#### Employee ↔ Department
- 関係: Many-to-One
- Employee側: `@ManyToOne`
- 外部キー: EMPLOYEE.DEPARTMENT_ID → DEPARTMENT.DEPARTMENT_ID

#### Department ↔ Employee
- 関係: One-to-Many
- Department側: `@OneToMany(mappedBy = "department")`
- 双方向関係

#### Workflow ↔ Book
- 関係: Many-to-One
- Workflow側: `@ManyToOne`（`insertable=false, updatable=false`）
- 外部キー: WORKFLOW.BOOK_ID → BOOK.BOOK_ID

#### Workflow ↔ Category
- 関係: Many-to-One
- Workflow側: `@ManyToOne`（`insertable=false, updatable=false`）
- 外部キー: WORKFLOW.CATEGORY_ID → CATEGORY.CATEGORY_ID

#### Workflow ↔ Publisher
- 関係: Many-to-One
- Workflow側: `@ManyToOne`（`insertable=false, updatable=false`）
- 外部キー: WORKFLOW.PUBLISHER_ID → PUBLISHER.PUBLISHER_ID

#### Workflow ↔ Employee
- 関係: Many-to-One
- Workflow側: `@ManyToOne`（`insertable=false, updatable=false`）
- 外部キー: WORKFLOW.OPERATED_BY → EMPLOYEE.EMPLOYEE_ID

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

必須項目:
- すべてのPRIMARY KEY
- EMPLOYEE.EMPLOYEE_CODE
- その他のフィールドは基本的にNULL許可（柔軟性のため）

### 6.2 UNIQUE制約

一意性保証:
- EMPLOYEE.EMPLOYEE_CODE（ログインIDとして使用）

### 6.3 CHECK制約

- JOB_RANK: 1, 2, 3のいずれか
- STATE: 'CREATED', 'APPLIED', 'APPROVED'のいずれか
- WORKFLOW_TYPE: 'ADD_NEW_BOOK', 'REMOVE_BOOK', 'ADJUST_BOOK_PRICE'のいずれか
- OPERATION_TYPE: 'CREATE', 'APPLY', 'APPROVE', 'REJECT'のいずれか
- QUANTITY: 0以上
- PRICE: 0以上

### 6.4 外部キー制約

すべての外部キー参照は制約として定義:
- ON DELETE: NO ACTION（参照整合性保証）
- ON UPDATE: CASCADE（ID更新時の連鎖更新）

## 7. インデックス設計

### 7.1 主キーインデックス
- すべてのテーブルのPRIMARY KEYに自動作成

### 7.2 外部キーインデックス
- すべての外部キーカラムにインデックス作成（JOIN性能向上）

### 7.3 検索用インデックス
- BOOK.CATEGORY_ID: カテゴリ別検索
- BOOK.PUBLISHER_ID: 出版社別検索
- BOOK.DELETED: 論理削除フィルタ
- WORKFLOW.WORKFLOW_ID: 履歴取得
- WORKFLOW.STATE: 状態別検索
- WORKFLOW.WORKFLOW_TYPE: タイプ別検索
- WORKFLOW.OPERATED_BY: 操作者別検索

## 8. データ容量見積もり

### 8.1 想定レコード数

| テーブル | 初期 | 1年後 | 5年後 |
|---------|------|-------|-------|
| BOOK | 1,000 | 5,000 | 20,000 |
| STOCK | 1,000 | 5,000 | 20,000 |
| CATEGORY | 10 | 20 | 50 |
| PUBLISHER | 50 | 100 | 500 |
| EMPLOYEE | 20 | 50 | 100 |
| DEPARTMENT | 5 | 10 | 20 |
| WORKFLOW | 100 | 5,000 | 50,000 |

### 8.2 ストレージ見積もり

| テーブル | 行サイズ(byte) | 5年後レコード数 | 容量見積もり |
|---------|---------------|----------------|-------------|
| BOOK | 500 | 20,000 | 10 MB |
| STOCK | 100 | 20,000 | 2 MB |
| CATEGORY | 100 | 50 | < 1 MB |
| PUBLISHER | 100 | 500 | < 1 MB |
| EMPLOYEE | 300 | 100 | < 1 MB |
| DEPARTMENT | 100 | 20 | < 1 MB |
| WORKFLOW | 800 | 50,000 | 40 MB |

**合計**: 約55 MB（5年後想定）

## 9. データライフサイクル

### 9.1 論理削除
- **BOOK**: DELETEDフラグで論理削除
- 物理削除は実施しない（履歴保持のため）

### 9.2 履歴保持
- **WORKFLOW**: 全操作履歴を永続的に保持（監査目的）
- 将来的にはアーカイブテーブルへの移行も検討

### 9.3 マスタデータ更新
- **CATEGORY, PUBLISHER**: 手動メンテナンス（管理者）
- **EMPLOYEE, DEPARTMENT**: 人事システムからの連携（将来）

## 10. データセキュリティ

### 10.1 機密データ
- **EMPLOYEE.PASSWORD**: BCryptハッシュで保存（不可逆変換）
- **EMPLOYEE.EMAIL**: 個人情報として扱う

### 10.2 監査ログ
- **WORKFLOW**: 全操作履歴を保持
  - 操作者（OPERATED_BY）
  - 操作日時（OPERATED_AT）
  - 操作内容（OPERATION_TYPE, OPERATION_REASON）

## 11. データ移行

### 11.1 初期データ投入
以下のマスタデータは初期投入が必要:
- CATEGORY（カテゴリマスタ）
- PUBLISHER（出版社マスタ）
- DEPARTMENT（部署マスタ）
- EMPLOYEE（社員マスタ）
- BOOK（書籍マスタ）
- STOCK（在庫マスタ）

### 11.2 テストデータ
開発・テスト環境用のテストデータを準備:
- サンプル社員（各職務ランク）
- サンプル書籍（各カテゴリ）
- サンプルワークフロー（各状態）

## 12. バックアップ・リカバリ

### 12.1 バックアップ戦略
- **フルバックアップ**: 日次（深夜）
- **トランザクションログ**: リアルタイム

### 12.2 リカバリポイント
- RPO（Recovery Point Objective）: 24時間以内
- RTO（Recovery Time Objective）: 2時間以内

## 13. データベース管理

### 13.1 統計情報更新
- 定期的な統計情報の更新（クエリ最適化のため）

### 13.2 インデックス再構築
- 断片化が進んだ場合のインデックス再構築

### 13.3 容量監視
- ディスク使用量の監視
- テーブルサイズの監視
- 成長率の分析
