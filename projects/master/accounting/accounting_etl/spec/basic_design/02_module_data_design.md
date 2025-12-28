# ERP会計統合ETLシステム 基本設計書

## 第2部: モジュール設計・データモデル設計

---

## 1. モジュール構成

### 1.1 全体モジュール構成

```
projects/python/accounting_etl/
├── src/
│   ├── local/                         # ローカル実行用ETLジョブ
│   │   ├── etl_orchestrator.py        # 統合オーケストレーター
│   │   ├── python_native/             # Python標準版
│   │   │   ├── standalone_sales_etl_job.py
│   │   │   ├── standalone_hr_etl_job.py
│   │   │   └── standalone_inventory_etl_job.py
│   │   └── pyspark/                   # PySpark版
│   │       ├── standalone_sales_etl_job.py
│   │       ├── standalone_hr_etl_job.py
│   │       └── standalone_inventory_etl_job.py
│   │
│   ├── aws_glue/                      # AWS Glue実行用ETLジョブ
│   │   ├── sales_etl_job.py
│   │   ├── hr_etl_job.py
│   │   └── inventory_etl_job.py
│   │
│   ├── common/                        # 環境共通モジュール
│   │   ├── config.py                  # 設定・定数定義
│   │   ├── csv_handler.py             # CSV入出力ハンドラー
│   │   └── utils.py                   # ユーティリティ
│   │
│   └── etl/                           # ETL変換モジュール（環境非依存）
│       ├── sales_transformer.py       # 売上データ変換
│       ├── hr_transformer.py          # 人事データ変換（必須項目バリデーション）
│       └── inventory_transformer.py   # 在庫データ変換
│
├── tests/                             # テストコード
│   ├── conftest.py                    # pytest共通設定
│   ├── unit/                          # 単体テスト
│   │   └── test_etl/
│   │       └── test_hr_transformer.py
│   └── integration/                   # 統合テスト
│       └── test_etl_output.py         # 出力検証（業務キー照合）
│
├── test_data/                         # テストデータ（入力CSV）
│   ├── sales/sales_txn_export.csv
│   ├── hr/
│   │   ├── hr_employee_org_export.csv    # 従業員マスタ
│   │   └── hr_payroll_export.csv         # 給与実績
│   ├── inventory/inv_movement_export.csv
│   └── expected/                      # 期待値データ（テスト用）
│       ├── accounting_txn_interface_sales.csv
│       ├── accounting_txn_interface_hr.csv
│       └── accounting_txn_interface_inventory.csv
│
├── output/                            # 出力データ（ETL処理結果）
│   └── accounting_txn_interface.csv   # 統合ファイル（2,481件）
│
├── merge_etl_outputs.py               # 個別ファイル統合スクリプト
├── cleanup_output.py                  # 出力クリーンアップ
├── requirements.txt                   # Python依存（PySpark含む）
├── requirements-dev.txt               # 開発依存（pytest含む）
├── pytest.ini                         # pytestプロジェクト設定
└── README.md                          # システム説明書
```

---

## 2. 共通モジュール設計

### 2.1 config.py - 設定管理モジュール

#### 責務
- 会計勘定科目マッピングルールの定義
- 税コード、通貨コード等のマスタ定義
- エラーコード定義
- システムコード定義

#### 主要な定義

**ソースシステム定義**:
- SALE: 売上システム
- HR: 人事システム
- INV: 在庫システム

**勘定科目マッピング**:
- 4100: 売上高
- 1100: 売掛金
- 6100: 給与費用
- 2110: 未払給与
- 1300: 棚卸資産
- 5100: 売上原価

**税コードマッピング**:
- TAX_STANDARD: 標準税率 10%
- TAX_REDUCED: 軽減税率 8%
- TAX_EXEMPT: 非課税 0%

**エラーコード定義**:
- E_VALIDATION: 検証エラー
- E_MAPPING: マッピングエラー
- E_CALCULATION: 計算エラー
- E_PROCESSING: 処理エラー

### 2.2 csv_handler.py - CSV入出力モジュール

#### 責務
- CSVファイルの読み込み
- CSVファイルの書き込み
- 出力ディレクトリのクリーンアップ

#### 主要関数

**read_csv()**:
- CSVファイルを読み込む
- 引数: ファイルパス
- 戻り値: レコードのリスト（各レコードは辞書）
- エラー: FileNotFoundError、Exception

**write_csv()**:
- CSVファイルを書き込む
- 引数: 出力ファイルパス、レコードリスト
- エラー: Exception

**cleanup_output_dir()**:
- 出力ディレクトリをクリーンアップする
- 引数: クリーンアップ対象ディレクトリ
- 戻り値: 削除したファイルパスのリスト

### 2.3 utils.py - ユーティリティモジュール

#### 責務
- 日付・時刻処理
- 数値計算（四捨五入、税額計算）
- バッチID生成
- データ検証

#### 主要クラス

**主要機能**:
- バッチID生成（形式: SALE_YYYYMMDD_HHMMSS）
- 税額計算
- 四捨五入
- 必須項目検証

---

## 3. ETL変換モジュール設計

### 3.1 Transformerの共通インターフェース

すべてのTransformerは以下の共通パターンに従う：

**主要メソッド**:
- `transform_record()`: 1レコードを変換
- `_create_base_record()`: 基本項目を設定
- `_create_error_record()`: エラーレコードを生成

### 3.2 SalesTransformer - 売上データ変換

#### 責務
売上トランザクションを会計仕訳に変換する。

#### 変換ロジック

**変換ルール**:
1. トランザクションタイプに応じた勘定科目決定（INVOICE/SHIP→売上高、ORDER→仕訳なし）
2. 税額計算（tax_amount = unit_price × quantity × tax_rate）
3. 外貨換算（JPY以外の場合、exchange_rateで換算）
4. セグメント情報設定（部門、商品、顧客）

**出力項目マッピング**:

| 出力項目 | 入力項目/計算式 |
|---------|---------------|
| batch_id | システム生成（SALE_YYYYMMDD_HHMMSS） |
| source_system | 固定値: 'SALE' |
| source_doc_type | source_record['txn_type'] |
| source_doc_id | source_record['source_txn_id'] |
| source_line_id | source_record['source_line_id'] |
| event_timestamp | source_record['event_timestamp'] |
| accounting_date | source_record['invoice_date'] or source_record['order_date'] |
| segment_account | ACCOUNT_MAPPING['REVENUE']['account'] |
| customer_id | source_record['customer_code'] |
| entered_dr | 0 |
| entered_cr | unit_price * quantity_shipped |
| accounted_dr | 0 |
| accounted_cr | entered_cr * exchange_rate |

### 3.3 HRTransformer - 人事データ変換

#### 責務
従業員・給与データを会計仕訳に変換する。

#### 変換ロジック

**バリデーション**:
- employee_id必須（不在時エラーレコード返却）
- department_code必須（不在時エラーレコード返却）

**変換ルール**:
1. **基本給**:
   - 借方: 給与費用(6100)
   - 貸方: 未払給与(2110)

2. **各種手当**:
   - 交通費: 借方=旅費交通費(6200)
   - 住宅手当: 借方=福利厚生費(6300)

3. **各種控除**:
   - 所得税: 貸方=預り金-所得税(2120)
   - 社会保険料: 貸方=預り金-社保(2130)

4. **部門配賦**:
   - segment_departmentに従業員の部門コード(department_code)を設定

**戻り値**: 給与項目ごとの仕訳レコードリスト

**出力例**（1従業員あたり複数エントリ）:

```
エントリ1: 基本給
  借方: 給与費用(6100) 300,000円
  貸方: 未払給与(2110) 300,000円

エントリ2: 交通費
  借方: 旅費交通費(6200) 20,000円
  貸方: 未払給与(2110) 20,000円

エントリ3: 所得税控除
  借方: 未払給与(2110) 25,000円
  貸方: 預り金-所得税(2120) 25,000円
```

### 3.4 InventoryTransformer - 在庫データ変換

#### 責務
在庫移動トランザクションを会計仕訳に変換する。

#### 変換ロジック

**変換ルール**:
1. **移動タイプ別勘定科目決定**:
   - RCV (入庫): 借方=在庫(1300)、貸方=買掛金(2100)
   - ISS (出庫): 借方=売上原価(5100)、貸方=在庫(1300)
   - ADJ (調整): 借方/貸方=在庫(1300)、相手=棚卸差異(5200)

2. **原価計算**:
   - 原価金額 = 単価 × 数量

3. **セグメント情報**:
   - segment_product: 商品コード
   - segment_department: 倉庫/拠点コード

**移動タイプ別マッピング**:

| 移動タイプ | 借方勘定 | 貸方勘定 |
|-----------|---------|---------|
| RCV（入庫） | 1300 棚卸資産 | 2100 買掛金 |
| ISS（出庫） | 5100 売上原価 | 1300 棚卸資産 |
| ADJ（調整）+ | 1300 棚卸資産 | 5200 棚卸差異 |
| ADJ（調整）- | 5200 棚卸差異 | 1300 棚卸資産 |

---

## 4. 実行モジュール設計

### 4.1 etl_orchestrator.py - 統合オーケストレーター

#### 責務
- 複数ETLジョブの並列/順次実行制御
- 個別出力ファイルの統合処理
- エラーハンドリングと結果レポート

#### 主要関数

**run_parallel()**:
- 並列実行処理
- ThreadPoolExecutorで並列実行
- 各ジョブをsubprocessで起動
- 全ジョブ完了を待機

**run_sequential()**:
- 順次実行処理
- エラー発生時は後続ジョブをスキップ
- デバッグ用途

**merge_output_files()**:
- 個別ファイル統合処理
- 処理内容:
  1. 各システムの個別CSVを読込（accounting_txn_interface_sales.csv、accounting_txn_interface_hr.csv、accounting_txn_interface_inventory.csv）
  2. 1つの統合CSVに結合（accounting_txn_interface.csv）
  3. 個別ファイルを削除（オプション）

#### 実行フロー

```
1. パラメータ取得
   ↓
2. クリーンアップ（--cleanupオプション時）
   ↓
3. 並列/順次実行
   ├── standalone_sales_etl_job.py
   ├── standalone_hr_etl_job.py
   └── standalone_inventory_etl_job.py
   ↓
4. 個別ファイル統合
   merge_output_files()
   ↓
5. 結果サマリー出力
```

### 4.2 standalone_*_etl_job.py - 個別ETLジョブ

#### 共通構造

**メイン処理フロー**:

1. **パラメータ解析**:
   - --input-dir: 入力ディレクトリ（デフォルト: test_data/sales）
   - --input-file: 入力ファイル名（デフォルト: sales_txn_export.csv）
   - --output-dir: 出力ディレクトリ（デフォルト: output）
   - --output-file: 出力ファイル名（デフォルト: accounting_txn_interface_sales.csv）
   - --limit: 処理レコード数制限（デフォルト: なし）
   - --error-threshold: エラー閾値（デフォルト: 100）

2. **CSV読込**:
   - 入力ファイルパスを構築
   - CSVファイルを読み込む

3. **変換処理**:
   - Transformerインスタンス生成
   - 各ソースレコードに対して:
     - 変換処理を実行
     - datetime型を文字列に変換（YYYY-MM-DD HH:MM:SS形式）
     - 変換後レコードをリストに追加
   - エラーカウントがerror_thresholdを超えた場合は例外発生

4. **CSV出力**:
   - 出力ファイルパスを構築
   - 変換後レコードをCSVファイルに書き込む

---

## 5. データモデル設計

### 5.1 入力データモデル（CSV）

#### test_data/sales/sales_txn_export.csv

**主要カラム**:

| カラム名 | データ型 | 説明 |
|---------|---------|------|
| export_id | INTEGER | エクスポートID |
| source_txn_id | VARCHAR | 原票トランザクションID |
| source_line_id | VARCHAR | 原票明細ID |
| txn_type | VARCHAR | トランザクションタイプ（ORDER/SHIP/INVOICE） |
| event_timestamp | TIMESTAMP | イベント発生日時 |
| customer_code | VARCHAR | 顧客コード |
| product_code | VARCHAR | 商品コード |
| quantity_shipped | DECIMAL | 出荷数量 |
| unit_price | DECIMAL | 単価 |
| currency_code | VARCHAR | 通貨コード |
| tax_code | VARCHAR | 税コード |
| tax_rate | DECIMAL | 税率 |

#### test_data/hr/hr_employee_org_export.csv（従業員マスタ）

**主要カラム**:

| カラム名 | データ型 | 説明 |
|---------|---------|------|
| export_id | INTEGER | エクスポートID |
| employee_id | VARCHAR | 従業員ID（ジョインキー） |
| employee_number | VARCHAR | 社員番号 |
| last_name | VARCHAR | 姓 |
| first_name | VARCHAR | 名 |
| dept_code | VARCHAR | 部門コード |
| cost_center_code | VARCHAR | コストセンタコード |
| payroll_group | VARCHAR | 給与グループ |
| allocation_rule_code | VARCHAR | 配賦ルールコード |
| tax_region_code | VARCHAR | 課税地域コード |
| bank_account_no | VARCHAR | 銀行口座番号 |

#### test_data/hr/hr_payroll_export.csv（給与実績）

**ファイル情報**:
- 従業員マスタと `employee_id` でジョイン
- INNER JOIN方式（給与実績がある従業員のみ処理）
- 30件（従業員1人につき1レコード）

**主要カラム**:

| カラム名 | データ型 | 説明 |
|---------|---------|------|
| payroll_id | VARCHAR | 給与ID（例: PAY202501_001） |
| employee_id | VARCHAR | 従業員ID（ジョインキー） |
| payroll_period | VARCHAR | 給与期間（YYYY-MM形式） |
| payment_date | DATE | 支給日 |
| basic_salary | DECIMAL | 基本給 |
| allowance_housing | DECIMAL | 住宅手当 |
| allowance_transportation | DECIMAL | 交通費 |
| deduction_tax | DECIMAL | 税金控除 |
| deduction_insurance | DECIMAL | 保険料控除 |
| bonus | DECIMAL | 賞与（0の場合はNULL扱い） |
| currency_code | VARCHAR | 通貨コード（例: JPY） |
| reversal_flag | BOOLEAN | 逆仕訳フラグ（True/False） |
| update_timestamp | TIMESTAMP | 更新タイムスタンプ |

#### test_data/inventory/inv_movement_export.csv

**主要カラム**:

| カラム名 | データ型 | 説明 |
|---------|---------|------|
| export_id | INTEGER | エクスポートID |
| movement_id | VARCHAR | 移動ID |
| movement_type | VARCHAR | 移動タイプ（RCV/ISS/ADJ） |
| product_code | VARCHAR | 商品コード |
| quantity | DECIMAL | 数量 |
| unit_cost | DECIMAL | 単位原価 |
| warehouse_code | VARCHAR | 倉庫コード |

### 5.2 出力データモデル（CSV）

#### output/accounting_txn_interface.csv（統合ファイル）

**全カラム定義** (52カラム):

| カラム名 | データ型 | 必須 | 説明 |
|---------|---------|------|------|
| batch_id | VARCHAR(50) | ✔ | バッチID |
| source_system | VARCHAR(10) | ✔ | ソースシステムコード（SALE/HR/INV） |
| source_doc_type | VARCHAR(20) | ✔ | 原票伝票タイプ |
| source_doc_id | VARCHAR(40) | ✔ | 原票伝票ID |
| source_line_id | VARCHAR(40) | ✔ | 原票明細ID |
| event_timestamp | TIMESTAMP | ✔ | イベント発生日時 |
| load_timestamp | TIMESTAMP | ✔ | ロードタイムスタンプ |
| status_code | VARCHAR(20) | ✔ | ステータスコード（READY/ERROR） |
| error_code | VARCHAR(20) | - | エラーコード |
| error_message | VARCHAR(500) | - | エラーメッセージ |
| ledger_id | VARCHAR(30) | ✔ | 元帳ID |
| legal_entity_id | VARCHAR(30) | ✔ | 法人ID |
| business_unit | VARCHAR(30) | - | 事業単位 |
| company_code | VARCHAR(30) | ✔ | 会社コード |
| accounting_date | DATE | ✔ | 会計日付 |
| period_name | VARCHAR(20) | ✔ | 会計期間（YYYY-MM） |
| journal_category | VARCHAR(40) | ✔ | 仕訳カテゴリ |
| currency_code | VARCHAR(3) | ✔ | 通貨コード（JPY/USD/EUR） |
| exchange_rate_type | VARCHAR(20) | - | 為替レートタイプ |
| exchange_rate | DECIMAL(19,6) | ✔ | 為替レート |
| segment_account | VARCHAR(30) | ✔ | セグメント-勘定科目 |
| segment_department | VARCHAR(30) | - | セグメント-部門 |
| segment_product | VARCHAR(30) | - | セグメント-商品 |
| segment_project | VARCHAR(30) | - | セグメント-プロジェクト |
| segment_interco | VARCHAR(30) | - | セグメント-内部取引 |
| segment_custom1 | VARCHAR(30) | - | セグメント-カスタム1 |
| segment_custom2 | VARCHAR(30) | - | セグメント-カスタム2 |
| customer_id | VARCHAR(40) | - | 顧客ID |
| supplier_id | VARCHAR(40) | - | 仕入先ID |
| bank_account_id | VARCHAR(40) | - | 銀行口座ID |
| invoice_id | VARCHAR(40) | - | 請求書ID |
| invoice_line_id | VARCHAR(40) | - | 請求書明細ID |
| po_id | VARCHAR(40) | - | 発注ID |
| receipt_id | VARCHAR(40) | - | 入庫ID |
| asset_id | VARCHAR(40) | - | 資産ID |
| project_id | VARCHAR(40) | - | プロジェクトID |
| quantity | DECIMAL(19,6) | - | 数量 |
| uom_code | VARCHAR(10) | - | 単位コード |
| tax_code | VARCHAR(20) | - | 税コード |
| tax_rate | DECIMAL(7,6) | - | 税率 |
| tax_amount_entered | DECIMAL(19,2) | - | 税額（入力通貨） |
| revrec_rule_code | VARCHAR(30) | - | 収益認識ルールコード |
| revrec_start_date | DATE | - | 収益認識開始日 |
| revrec_end_date | DATE | - | 収益認識終了日 |
| description | VARCHAR(500) | - | 摘要 |
| reference1 | VARCHAR(100) | - | 参照1 |
| reference2 | VARCHAR(100) | - | 参照2 |
| reference3 | VARCHAR(100) | - | 参照3 |
| reference4 | VARCHAR(100) | - | 参照4 |
| reference5 | VARCHAR(100) | - | 参照5 |
| reversal_flag | BOOLEAN | ✔ | 取消フラグ |
| reversed_interface_id | VARCHAR(40) | - | 取消元インターフェースID |
| created_by | VARCHAR(50) | ✔ | 作成者 |
| updated_by | VARCHAR(50) | ✔ | 更新者 |
| **entered_dr** | **DECIMAL(19,2)** | ✔ | **借方金額（入力通貨）** |
| **entered_cr** | **DECIMAL(19,2)** | ✔ | **貸方金額（入力通貨）** |
| **accounted_dr** | **DECIMAL(19,2)** | ✔ | **借方金額（機能通貨）** |
| **accounted_cr** | **DECIMAL(19,2)** | ✔ | **貸方金額（機能通貨）** |

---

## 6. 項目編集定義（データマッピング）

### 6.1 売上トランザクションエクスポート → 会計トランザクションインターフェース

| Target Field (accounting_txn_interface) | Source Field (sales_txn_export) | Transformation Logic | Mandatory | Validation / Error Code |
| --------------------------------------- | ------------------------------- | -------------------- | --------- | ----------------------- |
| **識別/監査** |
| batch_id | システム生成 | SALE_YYYYMMDD_HHMMSS形式 | ✔ | - |
| source_system | 固定値 | 'SALE' | ✔ | - |
| source_doc_type | txn_type | ORDER/SHIP/INVOICE/CM/PMT | ✔ | E_VALIDATION |
| source_doc_id | source_txn_id | 直接マッピング | ✔ | E_VALIDATION |
| source_line_id | source_line_id | 直接マッピング | ✔ | E_VALIDATION |
| event_timestamp | event_timestamp | 直接マッピング | ✔ | - |
| load_timestamp | システム生成 | ETL実行時刻（UTC） | ✔ | - |
| status_code | 検証結果 | READY/ERROR | ✔ | - |
| error_code | エラー時設定 | E_VALIDATION/E_MAPPING等 | △ | - |
| error_message | エラー時設定 | エラー内容 | △ | - |
| **会社/会計カレンダ/通貨** |
| ledger_id | 固定値 | 'GL001' | ✔ | - |
| legal_entity_id | 固定値 | 'COMP001' | ✔ | - |
| business_unit | 固定値 | 'BU001' | ✔ | - |
| company_code | 固定値 | 'COMP001' | ✔ | - |
| accounting_date | invoice_date or event_timestamp | 日付変換 | ✔ | E_VALIDATION |
| period_name | accounting_date | YYYY-MM形式に変換 | ✔ | - |
| journal_category | txn_type | Invoice→'Sales', PMT→'Payment' | ✔ | - |
| currency_code | currency_code | 直接マッピング | ✔ | E_VALIDATION |
| exchange_rate_type | 固定値 | 'Corporate' | △ | - |
| exchange_rate | exchange_rate or 計算 | 為替レート取得（JPYは1.0） | △ | E_CUR_002 |
| **セグメント** |
| segment_account | txn_type + tax_code | 勘定科目マッピング | ✔ | E_MAPPING |
| segment_department | customer_code | 顧客コードから部門マッピング | △ | - |
| segment_product | product_code | 商品コード正規化（大文字） | △ | E_MDM_001 |
| segment_project | NULL | プロジェクトコード（将来対応） | - | - |
| segment_interco | NULL | 内部取引コード（将来対応） | - | - |
| segment_custom1 | campaign_code | キャンペーンコード | - | - |
| segment_custom2 | salesperson_code | 営業担当者コード | - | - |
| **取引先/サブレジャ** |
| customer_id | customer_code | 顧客コード正規化（大文字） | △ | E_MDM_002 |
| supplier_id | NULL | 仕入先ID（売上では使用しない） | - | - |
| bank_account_id | NULL | 銀行口座ID | - | - |
| invoice_id | invoice_id | 直接マッピング | △ | - |
| invoice_line_id | source_line_id | 直接マッピング | △ | - |
| po_id | NULL | 発注書ID | - | - |
| receipt_id | NULL | 入庫ID | - | - |
| asset_id | NULL | 固定資産ID | - | - |
| project_id | NULL | プロジェクトID | - | - |
| **金額/数量** |
| entered_dr | net_amount | 返品時のみ設定（片側記録） | ✔ | - |
| entered_cr | net_amount | 通常売上時設定（片側記録） | ✔ | W_ZERO（両方0の場合） |
| accounted_dr | entered_dr × exchange_rate | 記帳通貨換算（借方） | ✔ | - |
| accounted_cr | entered_cr × exchange_rate | 記帳通貨換算（貸方） | ✔ | - |
| quantity | quantity_shipped | 出荷数量 | △ | - |
| uom_code | uom_code | 単位コード（EA/KG/L） | △ | - |
| **税/収益認識** |
| tax_code | tax_code | 税コード | △ | E_TAX_001 |
| tax_rate | tax_rate | 税率 | △ | - |
| tax_amount_entered | tax_amount | 税額（入力通貨） | △ | - |
| revrec_rule_code | revrec_rule_code | 収益認識ルール | - | - |
| revrec_start_date | invoice_date | 収益認識開始日 | - | - |
| revrec_end_date | NULL | 収益認識終了日 | - | - |
| **説明/参照** |
| description | product_name + order_id + customer_code | 摘要（最大500文字） | △ | - |
| reference1 | order_id | 受注ID | - | - |
| reference2 | invoice_id | 請求ID | - | - |
| reference3 | shipment_id | 出荷ID | - | - |
| reference4 | reference1 | 参照情報1 | - | - |
| reference5 | reference2 | 参照情報2 | - | - |
| **取消/反転** |
| reversal_flag | return_flag or cancel_flag | 取消フラグ（True/False） | △ | - |
| reversed_interface_id | NULL | 取消元インターフェースID | - | - |
| **作成/更新** |
| created_by | 固定値 | 'ETL_SALES' | ✔ | - |
| updated_by | 固定値 | 'ETL_SALES' | ✔ | - |

**変換ロジック補足**:

- **片側記録方式**: 通常売上は貸方（entered_cr）のみ記録、返品は借方（entered_dr）のみ記録
- **勘定科目マッピング**: INVOICE→4100（売上高）、SHIP→4100、PMT→1100（現金預金）
- **外貨換算**: JPY以外の場合、exchange_rateで換算（JPYは1.0）

### 6.2 在庫移動エクスポート → 会計トランザクションインターフェース

| Target Field (accounting_txn_interface) | Source Field (inv_movement_export) | Transformation Logic | Mandatory | Validation / Error Code |
| --------------------------------------- | ---------------------------------- | -------------------- | --------- | ----------------------- |
| **識別/監査** |
| batch_id | システム生成 | INV_YYYYMMDD_HHMMSS形式 | ✔ | - |
| source_system | 固定値 | 'INV' | ✔ | - |
| source_doc_type | movement_type | RCV/ISS/ADJ/CNT/TRF/CST | ✔ | E_INV_001 |
| source_doc_id | movement_id | 直接マッピング | ✔ | E_KEY_DUP |
| source_line_id | movement_line_id | 直接マッピング | ✔ | E_KEY_DUP |
| event_timestamp | movement_timestamp | 直接マッピング | ✔ | - |
| load_timestamp | システム生成 | ETL実行時刻（UTC） | ✔ | - |
| status_code | 検証結果 | READY/ERROR | ✔ | - |
| error_code | エラー時設定 | E_VALIDATION/E_MAPPING等 | △ | - |
| error_message | エラー時設定 | エラー内容 | △ | - |
| **会社/会計カレンダ/通貨** |
| ledger_id | 固定値 | 'GL001' | ✔ | - |
| legal_entity_id | 固定値 | 'COMP001' | ✔ | - |
| business_unit | 固定値 | 'BU001' | ✔ | - |
| company_code | 固定値 | 'COMP001' | ✔ | - |
| accounting_date | movement_timestamp | 日付変換 | ✔ | E_VALIDATION |
| period_name | accounting_date | YYYY-MM形式に変換 | ✔ | - |
| journal_category | movement_type | RCV→'Inventory', ISS→'Cost', ADJ→'Adjustment' | ✔ | E_MAP_CAT |
| currency_code | currency_code | 直接マッピング | △ | E_CUR_001 |
| exchange_rate_type | 固定値 | 'Corporate' | △ | - |
| exchange_rate | exchange_rate or 計算 | 為替レート取得（JPYは1.0） | △ | E_CUR_002 |
| **セグメント** |
| segment_account | movement_type + cost_method | 勘定科目マッピング（詳細は後述） | ✔ | E_ACC_002 |
| segment_department | inventory_org | 在庫組織から部門マッピング | △ | Default if missing |
| segment_product | item_code | 品目コード正規化（大文字） | △ | E_MDM_ITEM |
| segment_project | project_code | プロジェクトコード | - | - |
| segment_interco | NULL | 内部取引コード | - | - |
| segment_custom1 | inventory_org | 在庫組織コード | - | - |
| segment_custom2 | subinventory_code | サブ在庫コード | - | - |
| **取引先/サブレジャ** |
| customer_id | NULL | 顧客ID | - | - |
| supplier_id | source_doc_type='PO'時 | 発注書から仕入先特定 | △ | - |
| bank_account_id | NULL | 銀行口座ID | - | - |
| invoice_id | NULL | 請求書ID | - | - |
| invoice_line_id | NULL | 請求書明細ID | - | - |
| po_id | source_doc_type='PO'時 | 発注書ID | △ | - |
| receipt_id | movement_type='RCV'時 | movement_id | △ | - |
| asset_id | NULL | 固定資産ID | - | - |
| project_id | project_code | プロジェクトID | - | - |
| **金額/数量** |
| entered_dr | quantity × unit_cost | 移動金額（すべて借方記録） | ✔ | E_COST_001 |
| entered_cr | 固定値 | 0（片側記録方式） | ✔ | - |
| accounted_dr | entered_dr × exchange_rate | 記帳通貨換算（借方） | ✔ | - |
| accounted_cr | 固定値 | 0（片側記録方式） | ✔ | - |
| quantity | quantity | 数量 | ✔ | W_ZERO（0の場合） |
| uom_code | uom_code | 単位コード（EA/KG/L） | △ | - |
| **税/収益認識** |
| tax_code | NULL | 税コード（在庫では不使用） | - | - |
| tax_rate | NULL | 税率 | - | - |
| tax_amount_entered | NULL | 税額 | - | - |
| revrec_rule_code | NULL | 収益認識ルール | - | - |
| revrec_start_date | NULL | 収益認識開始日 | - | - |
| revrec_end_date | NULL | 収益認識終了日 | - | - |
| **説明/参照** |
| description | movement_type + item_description + location_code | 摘要（最大500文字） | △ | - |
| reference1 | source_doc_id | 発生元伝票ID | - | - |
| reference2 | wip_job_id | 製造オーダーID | - | - |
| reference3 | lot_no | ロット番号 | - | - |
| reference4 | serial_no | シリアル番号 | - | - |
| reference5 | reason_code | 理由コード | - | - |
| **取消/反転** |
| reversal_flag | reason_code | 'REVERSAL'/'CANCEL'含む時True | △ | - |
| reversed_interface_id | NULL | 取消元インターフェースID | - | - |
| **作成/更新** |
| created_by | 固定値 | 'ETL_INV' | ✔ | - |
| updated_by | 固定値 | 'ETL_INV' | ✔ | - |

**変換ロジック補足**:

- **片側記録方式**: すべて借方（entered_dr）のみ記録、貸方は常に0
- **勘定科目マッピング**: RCV→1300（棚卸資産）、ISS→5100（売上原価）、ADJ（増加）→1300、ADJ（減少）→5200（棚卸差異）
- **原価計算**: STD→標準原価、AVG→移動平均原価、その他→記録単価

### 6.3 人事・給与エクスポート → 会計トランザクションインターフェース

| Target Field (accounting_txn_interface) | Source Field (hr_employee_org_export + hr_payroll_export) | Transformation Logic | Mandatory | Validation / Error Code |
| --------------------------------------- | --------------------------------------------------------- | -------------------- | --------- | ----------------------- |
| **識別/監査** |
| batch_id | システム生成 | HR_YYYYMMDD_HHMMSS形式 | ✔ | - |
| source_system | 固定値 | 'HR' | ✔ | - |
| source_doc_type | 固定値 | 'PAYROLL' | ✔ | - |
| source_doc_id | employee_id + payroll_id + payroll_period | 複合キー生成 | ✔ | E_KEY_DUP |
| source_line_id | payroll_type | SALARY/ALLOWANCE_xxx/DEDUCTION_xxx | ✔ | - |
| event_timestamp | payment_date | 給与支給日 | ✔ | - |
| load_timestamp | システム生成 | ETL実行時刻（UTC） | ✔ | - |
| status_code | 検証結果 | READY/ERROR | ✔ | - |
| error_code | エラー時設定 | E_VALIDATION/E_MAPPING等 | △ | - |
| error_message | エラー時設定 | エラー内容 | △ | - |
| **会社/会計カレンダ/通貨** |
| ledger_id | 固定値 | 'GL001' | ✔ | - |
| legal_entity_id | 固定値 | 'COMP001' | ✔ | - |
| business_unit | 固定値 | 'BU001' | ✔ | - |
| company_code | 固定値 | 'COMP001' | ✔ | - |
| accounting_date | payment_date | 日付変換 | ✔ | E_VALIDATION |
| period_name | payroll_period | YYYY-MM形式（直接使用） | ✔ | - |
| journal_category | payroll_type | SALARY→'Payroll', BONUS→'Bonus', TAX→'Tax' | ✔ | - |
| currency_code | currency_code | 直接マッピング | ✔ | E_VALIDATION |
| exchange_rate_type | 固定値 | 'Corporate' | △ | - |
| exchange_rate | 計算 | 為替レート取得（JPYは1.0） | △ | E_CUR_002 |
| **セグメント** |
| segment_account | payroll_type | 給与項目別勘定科目マッピング（詳細は後述） | ✔ | E_ACC_PAY |
| segment_department | dept_code | 部門コード（配賦ルール適用） | ✔ | E_VALIDATION |
| segment_product | NULL | 商品コード（給与では不使用） | - | - |
| segment_project | allocation_rule_code | プロジェクト配賦（工数連携） | △ | - |
| segment_interco | NULL | 内部取引コード | - | - |
| segment_custom1 | cost_center_code | コストセンタコード | - | - |
| segment_custom2 | payroll_group | 給与グループ | - | - |
| **取引先/サブレジャ** |
| customer_id | NULL | 顧客ID | - | - |
| supplier_id | NULL | 仕入先ID | - | - |
| bank_account_id | bank_account_no | 銀行口座番号 | △ | - |
| invoice_id | NULL | 請求書ID | - | - |
| invoice_line_id | NULL | 請求書明細ID | - | - |
| po_id | NULL | 発注書ID | - | - |
| receipt_id | NULL | 入庫ID | - | - |
| asset_id | NULL | 固定資産ID | - | - |
| project_id | allocation_rule_code | プロジェクトID（配賦ルール） | △ | - |
| **金額/数量** |
| entered_dr | payroll_amount | 支給項目時設定（片側記録） | ✔ | E_PAY_SUM |
| entered_cr | payroll_amount | 控除項目時設定（片側記録） | ✔ | - |
| accounted_dr | entered_dr × exchange_rate | 記帳通貨換算（借方） | ✔ | - |
| accounted_cr | entered_cr × exchange_rate | 記帳通貨換算（貸方） | ✔ | - |
| quantity | NULL | 数量（給与では不使用） | - | - |
| uom_code | NULL | 単位コード | - | - |
| **税/収益認識** |
| tax_code | payroll_type + tax_region_code | 給与税コード生成 | △ | - |
| tax_rate | NULL | 税率 | - | - |
| tax_amount_entered | NULL | 税額 | - | - |
| revrec_rule_code | NULL | 収益認識ルール | - | - |
| revrec_start_date | NULL | 収益認識開始日 | - | - |
| revrec_end_date | NULL | 収益認識終了日 | - | - |
| **説明/参照** |
| description | employee_number + last_name + first_name + payroll_type | 摘要（最大500文字） | △ | - |
| reference1 | employee_number | 社員番号 | △ | - |
| reference2 | payroll_group | 給与グループ | - | - |
| reference3 | allocation_rule_code | 配賦ルールコード | - | - |
| reference4 | payroll_id | 給与ID | - | - |
| reference5 | NULL | 予備 | - | - |
| **取消/反転** |
| reversal_flag | reversal_flag | 取消フラグ（True/False） | ✔ | - |
| reversed_interface_id | NULL | 取消元インターフェースID | - | - |
| **作成/更新** |
| created_by | 固定値 | 'ETL_HR' | ✔ | - |
| updated_by | 固定値 | 'ETL_HR' | ✔ | - |

**変換ロジック補足**:

- **データジョイン**: hr_employee_org_export（従業員マスタ）とhr_payroll_export（給与実績）をemployee_idでINNER JOIN
- **複数エントリ生成**: 1従業員あたり複数仕訳（基本給、各種手当、各種控除）
- **片側記録方式**: 支給項目は借方（entered_dr）のみ、控除項目は貸方（entered_cr）のみ記録
- **勘定科目マッピング**: SALARY→6100（給与費）、ALLOWANCE_HOUSING→6130（福利厚生費）、DEDUCTION_TAX→2400（預り金）等

---

## 7. エラーハンドリング設計

### 7.1 エラー分類

| エラーコード | 説明 | 処理 |
|------------|------|------|
| E_VALIDATION | 入力検証エラー | ERROR statusで出力 |
| E_MAPPING | マッピングエラー | ERROR statusで出力 |
| E_CALCULATION | 計算エラー | ERROR statusで出力 |
| E_PROCESSING | 処理エラー | ログ記録、処理継続 |

### 7.2 エラーレコード出力

エラー発生時も、以下の最小限情報でCSV出力：

```csv
batch_id,source_system,source_doc_id,status_code,error_code,error_message,...
SALE_20251026_120000,SALE,TXN001,ERROR,E_VALIDATION,顧客コードが不正です,...
```

---

## 8. パフォーマンス設計

### 8.1 処理性能目標

| 項目 | 目標値 | 実績 |
|------|--------|------|
| 売上ETL（1000件） | 1秒以内 | Python Native: 0.8秒<br>PySpark: 2.5秒 |
| 人事ETL（150件） | 1秒以内 | Python Native: 0.3秒<br>PySpark: 2.2秒 |
| 在庫ETL（1331件） | 1秒以内 | Python Native: 0.9秒<br>PySpark: 2.8秒 |
| 統合処理 | 1秒以内 | 0.2秒 |
| **合計** | **3秒以内** | Python Native: **1.2秒**<br>PySpark: **7.5秒** |

### 8.2 最適化手法

**Python Native版:**
1. **並列実行**: ThreadPoolExecutorで3ジョブ同時実行
2. **バッチ処理**: メモリ上で一括変換
3. **シンプル処理**: CSV直接処理

**PySpark版:**
1. **ローカルモード**: local[*]（全CPUコア利用）
2. **RDD変換**: map()による並列変換
3. **broadcast変数**: プロジェクトパス、batch_idの共有

