# 人事・給与データエクスポート仕様書

## 1. 概要

人事システムから会計システムへの従業員・給与データのエクスポート仕様です。給与・手当・控除を会計仕訳に変換します。

---

## 2. 入力データ

### 2.1 従業員マスタ: hr_employee_org_export.csv

#### 2.1.1 ファイル情報

| 項目 | 内容 |
|------|------|
| ファイル名 | hr_employee_org_export.csv |
| ファイルパス | test_data/hr/hr_employee_org_export.csv |
| 文字コード | UTF-8 |
| 形式 | CSV（ヘッダー行あり） |

#### 2.1.2 主要項目

| 項目名 | データ型 | 必須 | 説明 |
|--------|---------|------|------|
| export_id | INTEGER | ✔ | エクスポートID |
| employee_id | VARCHAR(40) | ✔ | 従業員ID（ジョインキー） |
| employee_number | VARCHAR(30) | ✔ | 社員番号 |
| last_name | VARCHAR(60) | ✔ | 姓 |
| first_name | VARCHAR(60) | ✔ | 名 |
| dept_code | VARCHAR(30) | ✔ | 部門コード |
| cost_center_code | VARCHAR(30) | - | コストセンタコード |
| payroll_group | VARCHAR(30) | - | 給与グループ |
| allocation_rule_code | VARCHAR(30) | - | 配賦ルールコード |
| tax_region_code | VARCHAR(20) | - | 課税地域コード |
| bank_account_no | VARCHAR(34) | - | 銀行口座番号 |

### 2.2 給与実績データ: hr_payroll_export.csv

#### 2.2.1 ファイル情報

| 項目 | 内容 |
|------|------|
| ファイル名 | hr_payroll_export.csv |
| ファイルパス | test_data/hr/hr_payroll_export.csv |
| 文字コード | UTF-8 |
| 形式 | CSV（ヘッダー行あり） |
| 件数 | 30件（従業員1人につき1レコード） |

#### 2.2.2 主要項目

| 項目名 | データ型 | 必須 | 説明 |
|--------|---------|------|------|
| payroll_id | VARCHAR(30) | ✔ | 給与ID（例: PAY202501_001） |
| employee_id | VARCHAR(40) | ✔ | 従業員ID（ジョインキー） |
| payroll_period | VARCHAR(7) | ✔ | 給与期間（YYYY-MM形式） |
| payment_date | DATE | ✔ | 支給日 |
| basic_salary | DECIMAL(15,2) | ✔ | 基本給 |
| allowance_housing | DECIMAL(15,2) | - | 住宅手当 |
| allowance_transportation | DECIMAL(15,2) | - | 交通費 |
| deduction_tax | DECIMAL(15,2) | - | 税金控除 |
| deduction_insurance | DECIMAL(15,2) | - | 保険料控除 |
| bonus | DECIMAL(15,2) | - | 賞与（0の場合はNULL扱い） |
| currency_code | VARCHAR(3) | ✔ | 通貨コード（例: JPY） |
| reversal_flag | BOOLEAN | ✔ | 逆仕訳フラグ（True/False） |
| update_timestamp | TIMESTAMP | ✔ | 更新タイムスタンプ |

#### 2.2.3 データ例

```csv
payroll_id,employee_id,payroll_period,payment_date,basic_salary,allowance_housing,allowance_transportation,deduction_tax,deduction_insurance,bonus,currency_code,reversal_flag,update_timestamp
PAY202501_001,EMP00001,2025-01,2025-01-25,450000,80000,30000,55000,25000,0,JPY,False,2025-01-20 10:00:00
PAY202501_002,EMP00002,2025-01,2025-01-25,350000,60000,25000,40000,20000,0,JPY,False,2025-01-20 10:00:00
```

---

## 3. 出力データ: accounting_txn_interface.csv

### 3.1 人事データ出力項目

| 出力項目 | 入力項目/計算式 | 説明 |
|---------|---------------|------|
| batch_id | システム生成 | 形式: HR_YYYYMMDD_HHMMSS |
| source_system | 固定値: 'HR' | ソースシステムコード |
| source_doc_type | 'PAYROLL' | 給与伝票 |
| source_doc_id | payroll_id | 給与ID |
| source_line_id | entry_seq | エントリ連番 |
| accounting_date | payroll_date | 給与支給日 |
| period_name | payroll_period | 給与期間（YYYY-MM） |
| journal_category | 'Payroll' | 仕訳カテゴリ |
| segment_account | ACCOUNT_MAPPING | 勘定科目（後述） |
| segment_department | department_code | 部門コード |
| created_by | 'ETL_HR' | 作成者 |
| updated_by | 'ETL_HR' | 更新者 |

---

## 4. 項目編集定義（Mapping Definition）

### 4.1 人事・給与エクスポート → 会計トランザクションインターフェース

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

### 4.2 変換ロジック補足説明

#### 4.2.1 給与データのジョイン処理

ETLジョブは以下の2つのCSVファイルをジョインします：

1. **従業員マスタ** (`hr_employee_org_export.csv`)
   - employee_id, dept_code, cost_center_code等の組織情報

2. **給与実績データ** (`hr_payroll_export.csv`)
   - payroll_id, basic_salary, allowance_xxx, deduction_xxx等の給与情報

**ジョイン方式**: INNER JOIN（employee_idをキーとして）

**ジョイン処理の流れ**:
1. 従業員マスタを読み込み、employee_idをキーとした辞書を作成
2. 給与実績データを1行ずつ読み込む
3. employee_idで従業員マスタと照合
4. マッチした場合、従業員情報と給与情報を組み合わせて変換処理を実行

#### 4.2.2 複数エントリ生成（片側記録方式）

1従業員あたり、以下の**片側エントリ**を生成します：

**支給項目（借方のみ記録）**:
- 基本給（SALARY）: 借方=給与費（6100）
- 残業代（OVERTIME）: 借方=給与費（6100）
- 住宅手当（ALLOWANCE_HOUSING）: 借方=福利厚生費（6130）
- 交通費（ALLOWANCE_TRANSPORTATION）: 借方=旅費交通費（6120）
- 家族手当（ALLOWANCE_FAMILY）: 借方=給与費（6140）
- 賞与（BONUS）: 借方=賞与費（6110）

**控除項目（貸方のみ記録）**:
- 所得税（DEDUCTION_INCOME_TAX）: 貸方=預り金（2400）
- 住民税（DEDUCTION_RESIDENT_TAX）: 貸方=預り金（2401）
- 社会保険料（DEDUCTION_SOCIAL_INSURANCE）: 貸方=預り金（2402）
- 厚生年金（DEDUCTION_PENSION）: 貸方=預り金（2403）
- 健康保険（DEDUCTION_HEALTH）: 貸方=預り金（2404）
- 雇用保険（DEDUCTION_EMPLOYMENT）: 貸方=預り金（2405）

**重要**: 対側仕訳（未払給与）はERP側の自動仕訳ルールで一括生成されます。

#### 4.2.3 勘定科目マッピング

| 給与項目種別 | payroll_type | segment_account | 借方/貸方 | 説明 |
| ------------ | ------------ | --------------- | --------- | ---- |
| 基本給 | SALARY | 6100 | 借方 | 給与費用 |
| 残業代 | OVERTIME | 6100 | 借方 | 給与費用 |
| 賞与 | BONUS | 6110 | 借方 | 賞与費用 |
| 交通費 | ALLOWANCE_TRANSPORTATION | 6120 | 借方 | 旅費交通費 |
| 住宅手当 | ALLOWANCE_HOUSING | 6130 | 借方 | 福利厚生費 |
| 家族手当 | ALLOWANCE_FAMILY | 6140 | 借方 | 扶養手当 |
| 所得税 | DEDUCTION_INCOME_TAX | 2400 | 貸方 | 所得税預り金 |
| 住民税 | DEDUCTION_RESIDENT_TAX | 2401 | 貸方 | 住民税預り金 |
| 社会保険料 | DEDUCTION_SOCIAL_INSURANCE | 2402 | 貸方 | 社保預り金 |
| 厚生年金 | DEDUCTION_PENSION | 2403 | 貸方 | 年金預り金 |
| 健康保険 | DEDUCTION_HEALTH | 2404 | 貸方 | 健保預り金 |
| 雇用保険 | DEDUCTION_EMPLOYMENT | 2405 | 貸方 | 雇保預り金 |

#### 4.2.4 借方・貸方設定ロジック

**支給項目（手当・給与）の場合**:
- 給与項目種別がALLOWANCE、SALARY、BONUSのいずれかの場合
- 借方金額 = 金額（費用計上）
- 貸方金額 = 0

**控除項目（税金・保険）の場合**:
- 給与項目種別がDEDUCTIONで始まる場合
- 借方金額 = 0
- 貸方金額 = 金額（預り金計上）

**記帳通貨への換算**:
- 借方換算額 = 借方金額 × 為替レート
- 貸方換算額 = 貸方金額 × 為替レート

#### 4.2.5 配賦ルールの適用

従業員の配賦ルールコード（allocation_rule_code）に基づき、複数部門への配賦処理を実施：

**配賦ルール適用例**:
- 配賦ルールコードが'SPLIT_50_50'の場合:
  - 2つの部門に50:50で配賦
  - 部門A: 金額 × 0.5
  - 部門B: 金額 × 0.5
  
- 配賦ルールが設定されていない場合:
  - 単一部門への配賦
  - 従業員の所属部門（dept_code）に全額配賦

#### 4.2.6 給与項目のフィルタリング

金額が0または空の給与項目はエントリを生成しません：

**フィルタリング条件**:
- 基本給: 金額 > 0 の場合のみエントリ生成
- 手当（各種）: 金額 > 0 の場合のみエントリ生成
- 控除（各種）: 金額 > 0 の場合のみエントリ生成

**処理対象外となる項目**:
- 金額が0の項目
- 金額がNULLまたは空の項目

---

## 5. 変換ロジック仕様

### 4.1 給与項目別勘定科目マッピング

#### 4.1.1 基本給

| 給与項目 | 借方勘定 | 貸方勘定 |
|---------|---------|---------|
| 基本給 | 6100 給与費用 | 2110 未払給与 |

**片側記録方式（支給項目）**:
- 借方金額 = 基本給
- 貸方金額 = 0

**注**: 対側仕訳（未払給与）はERP側の自動仕訳ルールで生成

#### 4.1.2 手当（allowances）

| 手当種別 | 借方勘定 | 貸方勘定 |
|---------|---------|---------|
| 交通費（TRANSPORT） | 6200 旅費交通費 | 2110 未払給与 |
| 住宅手当（HOUSING） | 6300 福利厚生費 | 2110 未払給与 |
| 家族手当（FAMILY） | 6100 給与費用 | 2110 未払給与 |

#### 4.1.3 控除（deductions）

| 控除種別 | 借方勘定 | 貸方勘定 |
|---------|---------|---------|
| 所得税（INCOME_TAX） | 2110 未払給与 | 2120 預り金-所得税 |
| 住民税（RESIDENT_TAX） | 2110 未払給与 | 2121 預り金-住民税 |
| 社会保険料（SOCIAL_INSURANCE） | 2110 未払給与 | 2130 預り金-社保 |
| 厚生年金（PENSION） | 2110 未払給与 | 2131 預り金-年金 |
| 健康保険（HEALTH） | 2110 未払給与 | 2132 預り金-健保 |
| 雇用保険（EMPLOYMENT） | 2110 未払給与 | 2133 預り金-雇保 |

### 4.2 複数エントリ生成（片側記録方式）

1従業員あたり、以下の**片側エントリ**を生成：

1. 基本給エントリ（借方：給与費用）
2. 手当エントリ × N（各手当ごと、借方：各種費用）
3. 控除エントリ × M（各控除ごと、貸方：預り金）

**合計**: 1 + N + M エントリ/従業員（すべて片側のみ記録）

**重要**: 対側仕訳（未払給与）はERP側の自動仕訳ルールで一括生成されます。

### 4.3 給与実績データの読み込みとジョイン処理

#### 4.3.1 データ読み込み

ETLジョブは以下の2つのCSVファイルを読み込みます：

1. **従業員マスタ** (`hr_employee_org_export.csv`)
   - 従業員の組織情報、部門コード、コストセンタなどを含む
   
2. **給与実績データ** (`hr_payroll_export.csv`)
   - 各従業員の給与額、手当、控除の実績データ

#### 4.3.2 ジョイン処理

**ジョインキー**: `employee_id`

**ジョイン方式**: INNER JOIN（給与実績がある従業員のみ処理）

**処理フロー**:

1. 従業員マスタを読み込み、辞書化（employee_idをキーとする）
2. 給与実績を読み込み、従業員情報とジョイン
3. 給与データをDecimal型に変換
4. 会計仕訳に変換

#### 4.3.3 ジョイン結果

- **マッチしたレコード**: 会計仕訳に変換して出力
- **マッチしなかったレコード**: 
  - 給与実績のみ存在（従業員マスタなし）→ エラーログ出力、処理スキップ
  - 従業員マスタのみ存在（給与実績なし）→ 変換対象外（正常）

**注**: 実運用では給与計算システムと連携し、給与実績データは月次で更新されます

---

## 5. 実装仕様

### 5.1 モジュール

**ファイル**: `src/etl/hr_transformer.py`

**クラス**: `HRTransformer`

### 5.2 処理フロー

1. 入力CSVを読込（従業員マスタ、給与実績）
2. データジョイン（employee_idをキーにINNER JOIN）
3. 従業員ごとに処理（基本給、手当、控除エントリ作成）
4. 出力CSVへ書込
5. 統合処理
