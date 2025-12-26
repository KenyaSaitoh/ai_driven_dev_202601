# 在庫移動データエクスポート仕様書

## 1. 概要

在庫システムから会計システムへの在庫移動データのエクスポート仕様です。入庫・出庫・調整を会計仕訳に変換します。

---

## 2. 入力データ: inv_movement_export.csv

### 2.1 ファイル情報

| 項目 | 内容 |
|------|------|
| ファイル名 | inv_movement_export.csv |
| ファイルパス | test_data/inventory/inv_movement_export.csv |
| 文字コード | UTF-8 |
| 形式 | CSV（ヘッダー行あり） |

### 2.2 主要項目

| 項目名 | データ型 | 必須 | 説明 |
|--------|---------|------|------|
| export_id | INTEGER | ✔ | エクスポートID |
| movement_id | VARCHAR(40) | ✔ | 移動ID |
| movement_type | VARCHAR(20) | ✔ | 移動タイプ（RCV/ISS/ADJ） |
| movement_timestamp | TIMESTAMP | ✔ | 移動日時 |
| product_code | VARCHAR(40) | ✔ | 商品コード |
| product_name | VARCHAR(200) | - | 商品名 |
| quantity | DECIMAL(19,6) | ✔ | 数量 |
| unit_of_measure | VARCHAR(10) | ✔ | 単位 |
| unit_cost | DECIMAL(19,2) | ✔ | 単位原価 |
| total_cost | DECIMAL(19,2) | - | 総原価 |
| warehouse_code | VARCHAR(30) | ✔ | 倉庫コード |
| location_code | VARCHAR(30) | - | ロケーションコード |
| lot_number | VARCHAR(40) | - | ロット番号 |
| serial_number | VARCHAR(40) | - | シリアル番号 |

---

## 3. 出力データ: accounting_txn_interface.csv

### 3.1 在庫データ出力項目

| 出力項目 | 入力項目/計算式 | 説明 |
|---------|---------------|------|
| batch_id | システム生成 | 形式: INV_YYYYMMDD_HHMMSS |
| source_system | 固定値: 'INV' | ソースシステムコード |
| source_doc_type | movement_type | RCV/ISS/ADJ |
| source_doc_id | movement_id | 移動ID |
| accounting_date | movement_timestamp | 移動日 |
| period_name | YYYY-MM形式 | 会計期間 |
| journal_category | 'Inventory' | 仕訳カテゴリ |
| segment_account | ACCOUNT_MAPPING | 勘定科目（後述） |
| segment_product | product_code | 商品コード |
| segment_department | warehouse_code | 倉庫コード |
| quantity | quantity | 数量 |
| uom_code | unit_of_measure | 単位コード |
| created_by | 'ETL_INV' | 作成者 |
| updated_by | 'ETL_INV' | 更新者 |

---

## 4. 項目編集定義（Mapping Definition）

### 4.1 在庫移動エクスポート → 会計トランザクションインターフェース

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

### 4.2 変換ロジック補足説明

#### 4.2.1 片側記録方式（One-Sided Entry）

本システムでは**片側記録方式**を採用し、すべての在庫移動を**借方（entered_dr）のみ記録**します：

- **入庫（RCV）**: 借方=棚卸資産（1300）
  - 対側仕訳（貸方：買掛金）はERP側の自動仕訳ルールで生成
  
- **出庫（ISS）**: 借方=売上原価（5100）
  - 対側仕訳（貸方：棚卸資産）はERP側で生成
  
- **調整（ADJ）**:
  - 増加調整: 借方=棚卸資産（1300）、対側=棚卸差異（ERP側生成）
  - 減少調整: 借方=棚卸差異（5200）、対側=棚卸資産（ERP側生成）
  
- **振替（TRF）**: 借方のみ記録、対側はERP側で自動生成

#### 4.2.2 勘定科目マッピングルール

| movement_type | quantity条件 | segment_account | 説明 |
| ------------- | ------------ | --------------- | ---- |
| RCV | - | 1300 | 棚卸資産（入庫） |
| ISS | - | 5100 | 売上原価（出庫） |
| ADJ | quantity > 0 | 1300 | 棚卸資産（増加調整） |
| ADJ | quantity < 0 | 5200 | 棚卸差異（減少調整） |
| CNT | quantity > 0 | 1300 | 棚卸資産（棚卸増加） |
| CNT | quantity < 0 | 5200 | 棚卸差異（棚卸減少） |
| TRF | - | 1300 | 棚卸資産（振替） |
| CST | variance有 | 1200 | 在庫評価勘定（原価修正） |

#### 4.2.3 原価計算ロジック

**単価の決定**:
- 原価計算方法がSTD（標準原価）の場合: 標準原価を使用
- 原価計算方法がAVG（移動平均原価）の場合: 移動平均原価を使用
- その他の場合: 記録単価を使用

**移動金額の計算**:
- 移動金額 = |数量| × 単価（絶対値）

**片側記録方式の適用**:
- 借方金額 = 移動金額
- 貸方金額 = 0（すべて借方のみ記録）

**記帳通貨への換算**:
- 借方換算額 = 借方金額 × 為替レート
- 貸方換算額 = 0

#### 4.2.4 差異仕訳の生成

標準原価差異がある場合、別途差異仕訳を生成します：

**差異レコードの生成条件**:
- 差異金額が0でない場合

**差異レコードの内容**:
- 明細ID: 元の明細ID + "_VAR"
- 勘定科目: 5200（在庫差異）
- 仕訳カテゴリ: Variance
- 借方金額: |差異金額|（絶対値）
- 貸方金額: 0

---

## 5. 変換ロジック仕様

### 4.1 移動タイプ別勘定科目マッピング

| 移動タイプ | コード | 借方勘定 | 貸方勘定 | 説明 |
|-----------|------|---------|---------|------|
| 入庫 | RCV | 1300 棚卸資産 | 2100 買掛金 | 仕入による入庫 |
| 出庫 | ISS | 5100 売上原価 | 1300 棚卸資産 | 販売による出庫 |
| 調整（増） | ADJ | 1300 棚卸資産 | 5200 棚卸差異 | 棚卸による増加 |
| 調整（減） | ADJ | 5200 棚卸差異 | 1300 棚卸資産 | 棚卸による減少 |

### 4.2 金額計算（片側記録方式）

- 総原価計算: cost_amount = quantity × unit_cost
- 片側記録方式: すべて借方に記録
  - 入庫（RCV）: 借方=棚卸資産（1300）
  - 出庫（ISS）: 借方=売上原価（5100）
  - 調整（ADJ）増加: 借方=棚卸資産（1300）
  - 調整（ADJ）減少: 借方=棚卸差異（5200）
- 対側仕訳はERP側で自動生成

### 4.3 原価評価方法

- **採用方法**: 移動平均法
- **単位原価**: 入庫時に記録された原価を使用
- **評価差異**: 月次で調整仕訳を計上（将来対応）

---

## 5. 実装仕様

### 5.1 モジュール

**ファイル**: `src/etl/inventory_transformer.py`

**クラス**: `InventoryTransformer`

### 5.2 処理フロー

1. 入力CSVを読込
2. 移動ごとに処理（移動タイプ判定、勘定科目決定、原価計算、会計レコード生成）
3. 出力CSVへ書込
4. 統合処理

---

## 6. テストケース

### 6.1 正常系

| No | テストケース | 期待結果 |
|----|------------|---------|
| 1 | 入庫（RCV） | 借方=1300（棚卸資産）、貸方=2100（買掛金） |
| 2 | 出庫（ISS） | 借方=5100（売上原価）、貸方=1300（棚卸資産） |
| 3 | 調整（増） | 借方=1300（棚卸資産）、貸方=5200（棚卸差異） |
| 4 | 調整（減） | 借方=5200（棚卸差異）、貸方=1300（棚卸資産） |

### 6.2 異常系

| No | テストケース | 期待結果 |
|----|------------|---------|
| 1 | product_code が NULL | status_code=ERROR, error_code=E_VALIDATION |
| 2 | quantity が 0 | status_code=ERROR, error_code=E_VALIDATION |
| 3 | unit_cost が NULL | status_code=ERROR, error_code=E_VALIDATION |

