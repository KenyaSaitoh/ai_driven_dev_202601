# 売上トランザクションエクスポート仕様書

## 1. 概要

売上システムから会計システムへの売上トランザクションデータのエクスポート仕様です。受注・出荷・請求・入金に関わるトランザクションを会計仕訳に変換します。

---

## 2. 入力データ: sales_txn_export.csv

### 2.1 ファイル情報

| 項目 | 内容 |
|------|------|
| ファイル名 | sales_txn_export.csv |
| ファイルパス | test_data/sales/sales_txn_export.csv |
| 文字コード | UTF-8 |
| 形式 | CSV（ヘッダー行あり） |
| 区切り文字 | カンマ（,） |

### 2.2 基本項目

| 項目名 | データ型 | 必須 | 説明 | 値の例 |
|--------|---------|------|------|--------|
| export_id | INTEGER | ✔ | エクスポートID（主キー） | 1 |
| batch_id | VARCHAR(40) | ✔ | バッチID | BATCH_20251026_001 |
| source_txn_id | VARCHAR(40) | ✔ | 原票トランザクションID | TXN000001 |
| source_line_id | VARCHAR(40) | ✔ | 原票明細ID | LINE000001 |
| txn_type | VARCHAR(20) | ✔ | トランザクションタイプ | INVOICE, SHIP, ORDER, CM, PMT |
| txn_status | VARCHAR(20) | ✔ | トランザクションステータス | OPEN, CLOSED, CANCELLED |
| event_timestamp | TIMESTAMP | ✔ | イベント発生日時 | 2025-10-26 12:30:00 |
| update_timestamp | TIMESTAMP | ✔ | 更新タイムスタンプ | 2025-10-26 15:00:00 |

### 2.3 伝票情報

| 項目名 | データ型 | 必須 | 説明 | 値の例 |
|--------|---------|------|------|--------|
| order_id | VARCHAR(40) | - | 受注ID | ORD000001 |
| order_date | DATE | - | 受注日 | 2025-10-20 |
| invoice_id | VARCHAR(40) | - | 請求ID | INV000001 |
| invoice_date | DATE | - | 請求日 | 2025-10-26 |
| shipment_id | VARCHAR(40) | - | 出荷ID | SHIP000001 |
| shipment_date | DATE | - | 出荷日 | 2025-10-25 |

### 2.4 顧客・営業情報

| 項目名 | データ型 | 必須 | 説明 | 値の例 |
|--------|---------|------|------|--------|
| customer_code | VARCHAR(40) | ✔ | 顧客コード | CUST0001 |
| bill_to_site | VARCHAR(60) | - | 請求先サイト | 東京本社 |
| ship_to_site | VARCHAR(60) | - | 出荷先サイト | 大阪支店 |
| salesperson_code | VARCHAR(30) | - | 営業担当者コード | SALES01 |
| campaign_code | VARCHAR(30) | - | キャンペーンコード | CAMP2025Q4 |

### 2.5 商品・数量情報

| 項目名 | データ型 | 必須 | 説明 | 値の例 |
|--------|---------|------|------|--------|
| product_code | VARCHAR(40) | ✔ | 商品コード | PROD0001 |
| product_name | VARCHAR(200) | - | 商品名 | ノートPC ThinkPad X1 |
| quantity_ordered | DECIMAL(19,6) | - | 受注数量 | 10.000000 |
| quantity_shipped | DECIMAL(19,6) | - | 出荷数量 | 10.000000 |
| quantity_invoiced | DECIMAL(19,6) | - | 請求数量 | 10.000000 |
| unit_of_measure | VARCHAR(10) | - | 単位 | EA, KG, L |

### 2.6 金額・税情報

| 項目名 | データ型 | 必須 | 説明 | 値の例 |
|--------|---------|------|------|--------|
| unit_price | DECIMAL(19,2) | ✔ | 単価 | 150000.00 |
| line_amount | DECIMAL(19,2) | - | 明細金額 | 1500000.00 |
| currency_code | VARCHAR(3) | ✔ | 通貨コード | JPY, USD, EUR |
| exchange_rate | DECIMAL(19,6) | - | 為替レート | 150.500000 |
| tax_code | VARCHAR(20) | ✔ | 税コード | TAX_STANDARD, TAX_REDUCED |
| tax_rate | DECIMAL(7,6) | ✔ | 税率 | 0.100000 |
| tax_amount | DECIMAL(19,2) | - | 税額 | 150000.00 |

### 2.7 参照情報

| 項目名 | データ型 | 必須 | 説明 | 値の例 |
|--------|---------|------|------|--------|
| reference1 | VARCHAR(100) | - | 参照1 | 案件番号: PRJ2025-001 |
| reference2 | VARCHAR(100) | - | 参照2 | 契約番号: CNT2025-0050 |
| remarks | VARCHAR(500) | - | 備考 | 初回取引、要与信確認 |
| reversal_flag | BOOLEAN | - | 取消フラグ | FALSE |

---

## 3. 出力データ: accounting_txn_interface.csv

### 3.1 ファイル情報

| 項目 | 内容 |
|------|------|
| ファイル名 | accounting_txn_interface.csv（統合ファイル） |
| ファイルパス | output/accounting_txn_interface.csv |
| 文字コード | UTF-8 |
| 形式 | CSV（ヘッダー行あり） |
| 区切り文字 | カンマ（,） |
| 備考 | 売上・人事・在庫の全データを含む統合ファイル |

### 3.2 売上データ出力項目

**会計インターフェースへの変換仕様**

| 出力項目 | 入力項目/計算式 | 説明 |
|---------|---------------|------|
| batch_id | システム生成 | 形式: SALE_YYYYMMDD_HHMMSS |
| source_system | 固定値: 'SALE' | ソースシステムコード |
| source_doc_type | txn_type | INVOICE, SHIP, ORDER等 |
| source_doc_id | source_txn_id | 原票トランザクションID |
| source_line_id | source_line_id | 原票明細ID |
| event_timestamp | event_timestamp | イベント発生日時 |
| load_timestamp | システム生成 | ETL実行時刻（UTC） |
| status_code | 'READY' or 'ERROR' | 処理ステータス |
| error_code | エラー時設定 | E_VALIDATION, E_MAPPING等 |
| error_message | エラー時設定 | エラー内容 |
| ledger_id | 固定値: 'GL001' | 総勘定元帳ID |
| legal_entity_id | 固定値: 'COMP001' | 法人ID |
| business_unit | 固定値: 'BU001' | 事業単位 |
| company_code | 固定値: 'COMP001' | 会社コード |
| accounting_date | invoice_date or order_date | 会計日付 |
| period_name | YYYY-MM形式 | 会計期間 |
| journal_category | 'Sales' | 仕訳カテゴリ |
| currency_code | currency_code | 通貨コード |
| exchange_rate_type | 'Corporate' | 為替レートタイプ |
| exchange_rate | exchange_rate or 1.0 | 為替レート |
| segment_account | ACCOUNT_MAPPING | 勘定科目（後述） |
| segment_department | DEPT_マッピング | 部門コード |
| segment_product | product_code | 商品コード |
| segment_project | NULL | プロジェクトコード（将来対応） |
| segment_interco | NULL | 内部取引（将来対応） |
| segment_custom1 | campaign_code | キャンペーンコード |
| segment_custom2 | salesperson_code | 営業担当者コード |
| customer_id | customer_code | 顧客コード |
| supplier_id | NULL | 仕入先コード（売上では使用しない） |
| invoice_id | invoice_id | 請求書ID |
| invoice_line_id | source_line_id | 請求書明細ID |
| quantity | quantity_shipped | 数量 |
| uom_code | unit_of_measure | 単位コード |
| tax_code | tax_code | 税コード |
| tax_rate | tax_rate | 税率 |
| tax_amount_entered | tax_amount | 税額（入力通貨） |
| revrec_rule_code | 'MONTHLY' | 収益認識ルール |
| revrec_start_date | invoice_date | 収益認識開始日 |
| description | 商品名＋顧客情報 | 摘要 |
| reference1 | order_id | 参照1（受注番号） |
| reference2 | shipment_id | 参照2（出荷番号） |
| reference3 | reference1 | 参照3 |
| reference4 | reference2 | 参照4 |
| reversal_flag | reversal_flag | 取消フラグ |
| reversed_interface_id | NULL | 取消元ID |
| created_by | 'ETL_SALES' | 作成者 |
| updated_by | 'ETL_SALES' | 更新者 |
| **entered_dr** | **0** | **借方金額（入力通貨）** ※売上は貸方のみ |
| **entered_cr** | **unit_price × quantity_shipped** | **貸方金額（入力通貨）** |
| **accounted_dr** | **0** | **借方金額（機能通貨）** |
| **accounted_cr** | **entered_cr × exchange_rate** | **貸方金額（機能通貨）** |

---

## 4. 項目編集定義（Mapping Definition）

### 4.1 売上トランザクションエクスポート → 会計トランザクションインターフェース

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

### 4.2 変換ロジック補足説明

#### 4.2.1 片側記録方式（One-Sided Entry）

本システムでは**片側記録方式**を採用しています：

- **通常売上（INVOICE/SHIP）**: 貸方（entered_cr）のみ記録、借方は0
  - 売上高勘定（4100）に貸方計上
  - 対側仕訳（借方：売掛金）はERP側の自動仕訳ルールで生成
  
- **返品（return_flag=True）**: 借方（entered_dr）のみ記録、貸方は0
  - 売上高勘定（4100）に借方計上（マイナス売上）
  - 対側仕訳（貸方：売掛金）はERP側で生成

- **入金（PMT）**: 借方（entered_dr）のみ記録、貸方は0
  - 現金預金勘定（1100）に借方計上
  - 対側仕訳（貸方：売掛金）はERP側で生成

#### 4.2.2 勘定科目マッピングルール

| txn_type | tax_code | segment_account | 説明 |
| -------- | -------- | --------------- | ---- |
| INVOICE | （通常） | 4100 | 売上高 |
| SHIP | （通常） | 4100 | 売上高（出荷基準） |
| PMT | - | 1100 | 現金預金 |
| CM | - | 4100 | クレジットメモ（返品） |

#### 4.2.3 外貨換算ロジック

**為替レート取得**:
- 通貨コードがJPYの場合: exchange_rate = 1.0
- JPY以外の場合: ソースレコードのexchange_rateを使用、または社内公式レートを参照

**記帳通貨（JPY）への換算**:
- 借方換算額 = 借方入力額 × 為替レート
- 貸方換算額 = 貸方入力額 × 為替レート

---

## 5. 変換ロジック仕様

### 4.1 勘定科目マッピング

#### 4.1.1 トランザクションタイプ別マッピング

| トランザクションタイプ | 借方勘定 | 貸方勘定 | 備考 |
|---------------------|---------|---------|------|
| INVOICE（請求） | 1100 売掛金 | 4100 売上高 | 請求書発行時 |
| SHIP（出荷） | 1100 売掛金 | 4100 売上高 | 出荷基準売上計上 |
| ORDER（受注） | - | - | 仕訳なし（受注段階） |
| CM（クレジットメモ） | 4100 売上高 | 1100 売掛金 | 返品・値引 |
| PMT（入金） | 1010 現金預金 | 1100 売掛金 | 入金消込 |

#### 4.1.2 本システムの対象範囲

**対象**: INVOICE, SHIP（売上計上）  
**対象外**: ORDER（受注のみ）、CM（返品）、PMT（入金）← 将来対応

### 4.2 税額計算ロジック

**税額計算**:
- 税額 = 単価 × 出荷数量 × 税率（小数第2位で四捨五入）

**総額計算**:
- 総額（税込） = （単価 × 出荷数量）+ 税額

**税区分**:
- TAX_STANDARD: 標準税率 10%
- TAX_REDUCED: 軽減税率 8%
- TAX_EXEMPT: 非課税 0%

### 4.3 外貨換算ロジック

**機能通貨（JPY）への換算処理**:
- 通貨コードがJPYの場合: 換算不要（記帳通貨金額 = 入力通貨金額）
- JPY以外の場合: 記帳通貨金額 = 入力通貨金額 × 為替レート（小数第2位で四捨五入）

**為替レートタイプ**: Corporate（社内公式レート）

### 4.4 エラーハンドリング

#### 4.4.1 必須項目チェック

以下の項目が欠落している場合、ERROR statusで出力：

- source_txn_id
- source_line_id
- txn_type
- customer_code
- product_code
- unit_price
- quantity_shipped (INVOICE, SHIP時)
- currency_code
- tax_code
- tax_rate

**エラーコード**: E_VALIDATION

#### 4.4.2 マッピングエラー

- 未登録の顧客コード
- 未登録の商品コード  
- 未登録の税コード

**エラーコード**: E_MAPPING

---

## 5. 実装仕様

### 5.1 モジュール

**ファイル**: `src/etl/sales_transformer.py`

**クラス**: `SalesTransformer`

### 5.2 処理フロー

1. 入力CSVを読込
2. 1行ずつ変換処理（必須項目チェック、トランザクションタイプ判定、勘定科目決定、税額計算、外貨換算）
3. エラーハンドリング（ERROR statusで出力、error_thresholdを超えたら処理中断）
4. 出力CSVへ書込
5. 統合処理（オーケストレーターが実行）

---

## 6. テストケース

### 6.1 正常系

| No | テストケース | 期待結果 |
|----|------------|---------|
| 1 | 通常の請求（INVOICE） | segment_account=4100, 貸方=unit_price×quantity |
| 2 | 出荷（SHIP） | segment_account=4100, 貸方=unit_price×quantity |
| 3 | 外貨取引（USD） | accounted_cr = entered_cr × exchange_rate |
| 4 | 税込取引（TAX_STANDARD 10%） | tax_amount = 金額 × 0.10 |

### 6.2 異常系

| No | テストケース | 期待結果 |
|----|------------|---------|
| 1 | customer_code が NULL | status_code=ERROR, error_code=E_VALIDATION |
| 2 | unit_price が 0 | status_code=ERROR, error_code=E_VALIDATION |
| 3 | 未登録の税コード | status_code=ERROR, error_code=E_MAPPING |
| 4 | quantity_shipped が NULL（INVOICE時） | status_code=ERROR, error_code=E_VALIDATION |

