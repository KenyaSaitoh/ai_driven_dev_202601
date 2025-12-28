# 期待値データディレクトリ

このディレクトリには、ETL処理の出力結果を検証するための期待値データを格納します。

## ファイル構成

```
expected/
├── accounting_txn_interface_sales.csv      # 売上システムの期待値
├── accounting_txn_interface_hr.csv         # 人事システムの期待値
└── accounting_txn_interface_inventory.csv  # 在庫システムの期待値
```

## テスト検証方法（アサーション）

### 1. キーベースマッチング

期待値と実際の出力をレコード単位で比較します。完全一致ではなく、**キーフィールドでマッチング**する方式を採用しています。

#### キーフィールド（4つの組み合わせで一意にレコードを特定）

```python
key_fields = [
    'source_system',      # 例: 'SALE', 'HR', 'INV'
    'source_doc_type',    # 例: 'INVOICE', 'PAYROLL', 'RCV'
    'source_doc_id',      # 例: 'TXN000001', 'EMP00001__'
    'source_line_id'      # 例: 'LINE000001', 'SALARY', 'MOVL0000001'
]
```

### 2. 検証対象フィールド

キーでマッチングしたレコード同士を、以下のフィールドで比較します：

#### A. 完全一致を要求するフィールド

文字列・日付などの非数値フィールドは完全一致で検証：

```python
exact_match_fields = [
    'status_code',          # 'READY', 'ERROR', etc.
    'ledger_id',            # 'GL001'
    'legal_entity_id',      # 'COMP001'
    'company_code',         # 'COMP001'
    'accounting_date',      # '2025-08-20'
    'period_name',          # '2025-08'
    'journal_category',     # 'Sales', 'Payroll', 'Inventory'
    'currency_code',        # 'USD', 'EUR', 'JPY'
    'segment_account',      # '2300', '4100', etc.
    'segment_department',   # 'DEPT_CU', 'DEPT_02', etc.
    'customer_id',          # 'CUST0004' (NULLを許容)
    'supplier_id',          # (NULLを許容)
]
```

#### B. 許容誤差ありの数値フィールド

金額・為替レートなどの数値は浮動小数点演算の誤差を考慮：

```python
import pytest

# 金額フィールド（許容誤差: 0.01）
def assert_amounts_equal(expected_dr, expected_cr, actual_dr, actual_cr):
    assert abs(float(expected_dr) - float(actual_dr)) < 0.01, \
        f"entered_dr mismatch: expected {expected_dr}, got {actual_dr}"
    assert abs(float(expected_cr) - float(actual_cr)) < 0.01, \
        f"entered_cr mismatch: expected {expected_cr}, got {actual_cr}"

# 検証対象の数値フィールド
numeric_fields_with_tolerance = {
    'entered_dr': 0.01,
    'entered_cr': 0.01,
    'accounted_dr': 0.01,
    'accounted_cr': 0.01,
    'exchange_rate': 0.000001,
    'quantity': 0.0001,
    'tax_amount_entered': 0.01,
}
```

### 3. 検証から除外するフィールド

実行時に動的に生成される値は検証対象外：

```python
excluded_fields = [
    'batch_id',           # 実行ごとに変わる（例: 'SALE_20251029_010550'）
    'load_timestamp',     # 実行時のタイムスタンプ
    'created_by',         # システムが自動設定
    'updated_by',         # システムが自動設定
]
```

### 4. テスト実装例

```python
import csv
import pytest

def read_csv_as_dict(filepath, key_fields):
    """CSVをキーフィールドでインデックス化した辞書に変換"""
    records = {}
    with open(filepath, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            # 複合キーを生成
            key = tuple(row[field] for field in key_fields)
            records[key] = row
    return records

def test_sales_output_matches_expected():
    # 期待値と実際の出力を読み込み
    expected = read_csv_as_dict(
        'test_data/expected/accounting_txn_interface_sales.csv',
        key_fields=['source_system', 'source_doc_type', 'source_doc_id', 'source_line_id']
    )
    actual = read_csv_as_dict(
        'output/accounting_txn_interface_sales.csv',
        key_fields=['source_system', 'source_doc_type', 'source_doc_id', 'source_line_id']
    )
    
    # キーが一致するレコードを検証
    for key, expected_row in expected.items():
        assert key in actual, f"Expected record not found in output: {key}"
        actual_row = actual[key]
        
        # 完全一致フィールドの検証
        assert expected_row['status_code'] == actual_row['status_code']
        assert expected_row['accounting_date'] == actual_row['accounting_date']
        assert expected_row['segment_account'] == actual_row['segment_account']
        
        # 数値フィールドの検証（許容誤差あり）
        assert abs(float(expected_row['entered_dr']) - float(actual_row['entered_dr'])) < 0.01
        assert abs(float(expected_row['entered_cr']) - float(actual_row['entered_cr'])) < 0.01
```

### 5. アサーション失敗時のメッセージ

テストが失敗した場合、以下のような詳細なメッセージが表示されます：

```
AssertionError: Field 'segment_account' mismatch for key ('SALE', 'INVOICE', 'TXN000001', 'LINE000001')
  Expected: '2300'
  Actual:   '2400'

AssertionError: Amount 'entered_dr' mismatch for key ('HR', 'PAYROLL', 'EMP00001__', 'SALARY')
  Expected: 300000.00
  Actual:   305000.00
  Difference: 5000.00 (exceeds tolerance of 0.01)
```

## 注意事項

### タイムスタンプフィールド

以下のフィールドは実行時に動的に生成されるため、期待値には含めないか、検証から除外しています：

- `load_timestamp`
- `created_at`
- `updated_at`

### バッチID

`batch_id`は実行ごとに変わる可能性があるため、検証対象ですが、テスト実行前に期待値を更新する必要がある場合があります。

### 動的な値の扱い

為替レートなど、日付によって変わる値は以下の方法で対応：

1. テストデータの日付を固定する
2. モックを使用してレートを固定する
3. 許容範囲を広げる

## テスト実行

### 個別ジョブのテスト

各システム（Sales, HR, Inventory）の個別出力を検証：

```bash
# 1. ETL個別ジョブを実行（個別ファイルを保持）
python src/local/etl_orchestrator.py --keep-individual-files

# 2. 個別ジョブの出力をテスト
python -m pytest tests/integration/test_etl_output.py::TestETLOutputValidation::test_sales_output_matches_expected -v
python -m pytest tests/integration/test_etl_output.py::TestETLOutputValidation::test_hr_output_matches_expected -v
python -m pytest tests/integration/test_etl_output.py::TestETLOutputValidation::test_inventory_output_matches_expected -v
```

**検証内容:**
- `output/accounting_txn_interface_sales.csv` ⇔ `test_data/expected/accounting_txn_interface_sales.csv`
- `output/accounting_txn_interface_hr.csv` ⇔ `test_data/expected/accounting_txn_interface_hr.csv`
- `output/accounting_txn_interface_inventory.csv` ⇔ `test_data/expected/accounting_txn_interface_inventory.csv`

### 統合ジョブのテスト

全システム統合ファイルを検証：

```bash
# 1. ETL統合ジョブを実行
python src/local/etl_orchestrator.py --cleanup

# 2. 統合ファイルの出力をテスト
python -m pytest tests/integration/test_etl_output.py -v
```

**検証内容:**
- `output/accounting_txn_interface.csv`（統合ファイル）から各システムのデータを抽出
- 抽出したデータを期待値と比較

### すべてのテストを一括実行

```bash
# 統合テストのみ
python -m pytest tests/integration/test_etl_output.py -v

# すべてのテスト（単体テスト + 統合テスト）
python -m pytest tests/ -v
```

### テスト結果の例

#### 成功時

```
tests/integration/test_etl_output.py::TestETLOutputValidation::test_sales_output_matches_expected PASSED   [33%]
tests/integration/test_etl_output.py::TestETLOutputValidation::test_hr_output_matches_expected PASSED      [66%]
tests/integration/test_etl_output.py::TestETLOutputValidation::test_inventory_output_matches_expected PASSED [100%]

============================== 3 passed in 0.45s ==============================
```

#### 失敗時（詳細表示）

```bash
# より詳細なエラー情報を表示
python -m pytest tests/integration/test_etl_output.py -v --tb=long

# スタックトレースを短縮表示
python -m pytest tests/integration/test_etl_output.py -v --tb=short
```

### デバッグ用のテスト実行

```bash
# 最初の失敗で停止
python -m pytest tests/integration/test_etl_output.py -x

# 失敗したテストのみ再実行
python -m pytest tests/integration/test_etl_output.py --lf

# 標準出力を表示（print文のデバッグ用）
python -m pytest tests/integration/test_etl_output.py -s

# 特定のキーワードでフィルタ
python -m pytest tests/integration/ -k "sales" -v
```

### カバレッジ付きでテスト実行

```bash
# カバレッジレポート生成
python -m pytest tests/integration/test_etl_output.py --cov=src/etl --cov-report=html

# カバレッジレポート閲覧
open htmlcov/index.html  # macOS/Linux
start htmlcov/index.html  # Windows
```

## サンプル期待値レコード

### 売上システム（SALE）

```csv
batch_id,source_system,source_doc_type,source_doc_id,source_line_id,status_code,accounting_date,segment_account,segment_department,entered_dr,entered_cr,accounted_dr,accounted_cr
SALE_20251026_155233,SALE,INVOICE,TXN000001,LINE000001,READY,2025-08-20,2300,DEPT_CU,0,177597.3900,0.000000,26971089.9436950300
```

### 人事システム（HR）

```csv
batch_id,source_system,source_doc_type,source_doc_id,source_line_id,status_code,accounting_date,segment_account,segment_department,entered_dr,entered_cr,accounted_dr,accounted_cr
HR_20251026_100000,HR,PAYROLL,EMP001_PAY202510_202510,SALARY,READY,2025-10-25,6100,D001,300000,0,300000,0
HR_20251026_100000,HR,PAYROLL,EMP001_PAY202510_202510,PAYABLE,READY,2025-10-25,2100,D001,0,300000,0,300000
```

### 在庫システム（INV）

```csv
batch_id,source_system,source_doc_type,source_doc_id,source_line_id,status_code,accounting_date,segment_account,segment_department,entered_dr,entered_cr,accounted_dr,accounted_cr
INV_20251026_100000,INV,MOVEMENT,MOVE000001,LINE000001,READY,2025-10-20,5000,DEPT_WH,150000,0,150000,0
```

## トラブルシューティング

### テストが失敗する場合

1. **レコード数の不一致**
   - 入力データが変更されていないか確認
   - ETLロジックの変更により出力レコード数が変わっていないか確認

2. **金額の不一致**
   - 為替レートが変わっている可能性
   - 計算ロジックの変更を確認

3. **フィールド値の不一致**
   - マッピングルールの変更を確認
   - マスタデータの変更を確認


