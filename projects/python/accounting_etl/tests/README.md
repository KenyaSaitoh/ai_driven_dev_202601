# テストディレクトリ

このディレクトリには、accounting_etlプロジェクトのテストコードが含まれています。

## ディレクトリ構成

```
tests/
├── conftest.py                      # pytest共通設定とフィクスチャ
├── unit/                            # 単体テスト
│   ├── test_common/                 # commonモジュールのテスト
│   │   ├── test_config.py
│   │   ├── test_csv_handler.py
│   │   └── test_utils.py
│   └── test_etl/                    # etlモジュールのテスト
│       ├── test_hr_transformer.py
│       ├── test_inventory_transformer.py
│       └── test_sales_transformer.py
├── integration/                     # 統合テスト
│   └── test_etl_output.py          # ETL出力結果の検証
└── local/                           # ローカル実行テスト（src/localから移動予定）
```

## テストの種類

### 1. 単体テスト（Unit Tests）

個別のモジュール、クラス、関数のテスト。

**場所**: `tests/unit/`

**実行方法**:
```bash
# 全単体テストを実行
pytest tests/unit/ -v

# 特定のモジュールのみ
pytest tests/unit/test_etl/test_hr_transformer.py -v

# 特定のテストクラスのみ
pytest tests/unit/test_etl/test_hr_transformer.py::TestHRTransformer -v

# 特定のテスト関数のみ
pytest tests/unit/test_etl/test_hr_transformer.py::TestHRTransformer::test_transform_payroll_record_success -v
```

### 2. 統合テスト（Integration Tests）

ETLプロセス全体を通した出力結果の検証。

**場所**: `tests/integration/`

**実行方法**:
```bash
# 全統合テストを実行
pytest tests/integration/ -v

# ETL出力検証テストのみ
pytest tests/integration/test_etl_output.py -v

# 特定システムのみ
pytest tests/integration/test_etl_output.py::TestETLOutputValidation::test_sales_output_matches_expected -v
```

### 3. ローカル実行テスト

ローカル環境でETLを実際に実行するテスト。

**場所**: `tests/local/` (将来的に`src/local/`から移動予定)

**実行方法**:
```bash
# オーケストレーターを実行
python src/local/etl_orchestrator.py --cleanup

# 個別ジョブを実行
python src/local/standalone_hr_etl_job.py \
  --input-dir test_data/hr \
  --input-file hr_employee_org_export.csv \
  --output-dir output \
  --output-file accounting_txn_interface_hr.csv
```

## テスト実行

### 前提条件

1. 依存パッケージのインストール
   ```bash
   pip install -r requirements.txt
   pip install pytest pytest-cov
   ```

2. テストデータの配置
   - `test_data/hr/`, `test_data/sales/`, `test_data/inventory/` に入力データ
   - `test_data/expected/` に期待値データ（オプション）

### 基本的な実行コマンド

```bash
# 全テストを実行
pytest

# 詳細表示
pytest -v

# 特定のディレクトリのみ
pytest tests/unit/ -v
pytest tests/integration/ -v

# カバレッジレポート付き
pytest --cov=src --cov-report=html

# 失敗したテストのみ再実行
pytest --lf

# 並列実行（pytest-xdist）
pytest -n auto
```

### マーカーを使用した実行

```python
# テストにマーカーを付ける例
@pytest.mark.slow
def test_large_dataset():
    pass

@pytest.mark.integration
def test_end_to_end():
    pass
```

```bash
# スローテストをスキップ
pytest -m "not slow"

# 統合テストのみ実行
pytest -m integration
```

## テストの書き方

### 単体テストの例

```python
# tests/unit/test_etl/test_sales_transformer.py
import pytest
from src.etl.sales_transformer import SalesTransformer

class TestSalesTransformer:
    @pytest.fixture
    def transformer(self):
        return SalesTransformer(batch_id="TEST_001")
    
    def test_transform_invoice(self, transformer):
        input_record = {
            'invoice_id': 'INV001',
            'amount': 1000,
            'currency': 'JPY'
        }
        
        result = transformer.transform_record(input_record)
        
        assert result['source_doc_id'] == 'INV001'
        assert result['entered_dr'] == 1000
        assert result['currency_code'] == 'JPY'
```

### 統合テストの例（キーマッチング方式）

```python
# tests/integration/test_etl_output.py
def test_sales_output_matches_expected(output_file, expected_file, 
                                      csv_reader, key_matcher):
    # ファイル読み込み
    actual_records = csv_reader(output_file)
    expected_records = csv_reader(expected_file)
    
    # キーでインデックス化
    key_fields = ['source_system', 'source_doc_id', 'source_line_id']
    actual_index = key_matcher(actual_records, key_fields)
    expected_index = key_matcher(expected_records, key_fields)
    
    # レコード数確認
    assert len(actual_index) == len(expected_index)
    
    # キーごとに値を比較
    for key, expected_record in expected_index.items():
        actual_record = actual_index[key]
        assert actual_record['segment_account'] == expected_record['segment_account']
        assert actual_record['entered_dr'] == expected_record['entered_dr']
```

## フィクスチャ（共通設定）

`conftest.py`には共通で使用するフィクスチャが定義されています：

- `project_root_dir`: プロジェクトルートのパス
- `test_data_dir`: テストデータディレクトリのパス
- `expected_data_dir`: 期待値データディレクトリのパス
- `output_dir`: 出力ディレクトリのパス
- `csv_reader`: CSV読み込みヘルパー関数
- `key_matcher`: キーマッチング用のインデックス作成関数
- `decimal_comparator`: 数値比較関数（丸め誤差考慮）

## テストデータ管理

### 入力データ

**場所**: `test_data/hr/`, `test_data/sales/`, `test_data/inventory/`

- 実際のETL処理で使用する入力CSVファイル
- サンプルデータまたは本番に近いデータを配置

### 期待値データ

**場所**: `test_data/expected/`

- ETL処理の期待される出力結果
- キーマッチング方式で検証するため、全レコードは不要
- 代表的なパターンやエッジケースのレコードのみでOK

詳細は `test_data/expected/README.md` を参照。

## CI/CD統合

### GitHub Actions の例

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-python@v2
        with:
          python-version: '3.9'
      - name: Install dependencies
        run: |
          pip install -r requirements.txt
          pip install pytest pytest-cov
      - name: Run tests
        run: pytest --cov=src --cov-report=xml
      - name: Upload coverage
        uses: codecov/codecov-action@v2
```

## トラブルシューティング

### テストが見つからない

```bash
# pytestがテストを検出できているか確認
pytest --collect-only

# パスの問題の可能性
# conftest.pyでsys.path設定を確認
```

### インポートエラー

```python
# プロジェクトルートをパスに追加（conftest.pyで実施済み）
import sys
from pathlib import Path
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))
```

### 期待値テストが失敗

1. 期待値ファイルが存在するか確認
   ```bash
   ls -la test_data/expected/
   ```

2. ETLを再実行して出力を確認
   ```bash
   python src/local/etl_orchestrator.py --cleanup --keep-individual-files
   diff output/accounting_txn_interface_sales.csv test_data/expected/accounting_txn_interface_sales.csv
   ```

3. 期待値を更新
   ```bash
   cp output/accounting_txn_interface_sales.csv test_data/expected/
   ```

## ベストプラクティス

1. **テストの独立性**: 各テストは独立して実行できるようにする
2. **テストデータの最小化**: 必要最小限のデータでテストする
3. **明確なテスト名**: テストの目的が分かる名前を付ける
4. **アサーションメッセージ**: 失敗時に原因が分かるメッセージを付ける
5. **フィクスチャの活用**: 共通セットアップはフィクスチャで提供
6. **キーマッチング検証**: 全量一致ではなく、キーでマッチングして主要フィールドを検証

## 参考リンク

- [pytest公式ドキュメント](https://docs.pytest.org/)
- [pytest-cov](https://pytest-cov.readthedocs.io/)
- [期待値データの作成方法](../test_data/expected/README.md)

