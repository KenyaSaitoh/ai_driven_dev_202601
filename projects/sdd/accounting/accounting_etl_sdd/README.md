# ERP会計統合ETL（SDD研修用）

## 概要

このプロジェクトは、上流システム（売上・人事・在庫）から会計パッケージへのデータ統合を行うETL処理です。

> **Note:** このプロジェクトは**仕様駆動開発（SDD: Specification-Driven Development）**の研修用プロジェクトです。
> 仕様書（spec/）に基づいて、AIを活用してPython ETLパイプラインを段階的に実装します。

**特徴**:
- **データベース非依存**: RDB製品に依存せず、CSV入出力で動作
- **ローカル/クラウド対応**: ローカル（ファイルシステム）とAWS（S3）の両方で実行可能
- **共通変換ロジック**: Transformerモジュールは環境を問わず共通
- **テスト環境完備**: 本番コードの品質を担保するテストフレームワーク
- **クロスプラットフォーム**: Windows / macOS / Linuxで動作

## 前提条件

- **Python 3.11**（システムにインストール済み）
- **Java 11以上**（PySpark版を使用する場合のみ）
- **Git Bash**（Windows環境の場合）

> **重要**: 
> - **PySpark版を使用する場合、Python 3.11を推奨します**。Python 3.12ではPySparkに互換性問題があります
> - Python 3.12を使用している場合は、**Python標準版（PySpark不要）を使用してください**
> - システムインストール版Python以外（埋め込み版など）を使用する場合は、環境変数`PYTHON311_PATH`を設定してください
> - Windows環境で埋め込み版Pythonを使用する場合の詳細は [README_WINDOWS_PYSPARK.md](README_WINDOWS_PYSPARK.md) を参照

## セットアップ

### 1. プロジェクトディレクトリに移動

```bash
cd projects/sdd/accounting/accounting_etl_sdd
```

### 2. 依存パッケージのインストール

**開発・テスト用（Python標準版）:**
```bash
pip install boto3
```

**開発・テスト用（PySpark版）:**
```bash
pip install -r requirements-dev.txt
```

**本番用（PySpark版）:**
```bash
pip install -r requirements.txt
```

## 実行方法

### Python標準版（PySparkなし）

#### 出力フォルダのクリーンアップ（初回/終了時）

```bash
# 前回の出力をクリーンアップ
python cleanup_output.py --force
```

> **Note**: 
> - プロジェクト開始時に1回実行（前回の出力をクリア）
> - プロジェクト終了時に1回実行（クリーンアップ）

#### 全ジョブ実行

```bash
# オーケストレーター経由で全ジョブを並列実行（推奨）
python src/local/etl_orchestrator.py --mode python --limit 100

# 全件処理の場合（limitなし）
python src/local/etl_orchestrator.py --mode python
```

#### 個別ジョブ実行

```bash
# Sales ETL
python src/local/python_native/standalone_sales_etl_job.py --limit 100

# HR ETL
python src/local/python_native/standalone_hr_etl_job.py --limit 100

# Inventory ETL
python src/local/python_native/standalone_inventory_etl_job.py --limit 100
```

#### 出力ファイルの確認

```bash
# 出力ファイル一覧
ls -lh output/

# 統合ファイルの内容確認
head -n 10 output/accounting_txn_interface.csv

# レコード数確認（ヘッダー除く）
tail -n +2 output/accounting_txn_interface.csv | wc -l
```

### PySpark版

#### 実行スクリプトの選択

PySpark版には2つの実行スクリプトがあります：

1. **`run_pyspark_etl.sh`** - 一般的な環境用
   - システムにインストールされたPythonを使用
   - Linux、macOS、Windows（システムPython）で使用

2. **`run_pyspark_etl_win_embed.sh`** - Windows埋め込み版Python専用
   - `python-3.11.7-embed-amd64` など埋め込み版Pythonを使用
   - 環境変数 `PYTHON311_PATH` の設定が必須
   - 詳細は [README_WINDOWS_PYSPARK.md](README_WINDOWS_PYSPARK.md) を参照

> **どちらを使うべきか**:
> - **一般的な環境（システムPython）**: `./run_pyspark_etl.sh` を使用
> - **Windows埋め込み版Python 3.11**: `./run_pyspark_etl_win_embed.sh` を使用（環境変数設定必須）

#### 出力フォルダのクリーンアップ（初回/終了時）

```bash
# 前回の出力をクリーンアップ
python cleanup_output.py --force
```

> **Note**: 
> - プロジェクト開始時に1回実行（前回の出力をクリア）
> - プロジェクト終了時に1回実行（クリーンアップ）

#### 全ジョブ実行（一般的な環境）

```bash
# 全ETLジョブを並列実行
./run_pyspark_etl.sh all
```

#### 個別ジョブ実行（一般的な環境）

```bash
# Sales ETL
./run_pyspark_etl.sh sales

# HR ETL
./run_pyspark_etl.sh hr

# Inventory ETL
./run_pyspark_etl.sh inventory
```

#### 出力ファイルの確認

```bash
# 出力ファイル一覧
ls -lh output/

# 統合ファイルの内容確認
head -n 10 output/accounting_txn_interface.csv

# レコード数確認（ヘッダー除く）
tail -n +2 output/accounting_txn_interface.csv | wc -l
```

## 自動テスト（検証）

ETL実行後、pytestで出力結果を自動検証します。

### 前提条件

テスト実行には、上記「実行方法」でETLを実行し、`output/accounting_txn_interface.csv`が生成されている必要があります。

### 単体テスト

変換ロジック（Transformer）の動作を検証します。

```bash
# 全単体テストを実行
python -m pytest tests/unit/ -v

# 特定モジュールのみ
python -m pytest tests/unit/test_etl/test_hr_transformer.py -v
```

### 統合テスト

ETL出力ファイルの内容を期待値データと照合します。

```bash
# 全システムの期待値データを検証
python -m pytest tests/integration/test_etl_output.py -v

# 特定システムのみ検証
python -m pytest tests/integration/test_etl_output.py::TestETLOutputValidation::test_sales_output_matches_expected -v
```

詳細は [tests/README.md](tests/README.md) を参照。

## アーキテクチャ

```
[上流システム]        [ETL処理]             [会計システム]
  ┌──────────┐      ┌─────────┐         ┌────────────────┐
  │ 売上CSV  │─────→│         │         │                │
  │ 人事CSV  │─────→│ Python  │────────→│ 統合CSV取込    │
  │ 在庫CSV  │─────→│  ETL    │  1ファイル │ (会計パッケージ) │
  └──────────┘      └─────────┘         └────────────────┘
  
  ローカル: test_data/ → Transformer → output/accounting_txn_interface.csv
  AWS:      S3://input/ → Transformer → S3://output/accounting_txn_interface.csv
```

### コード構成

```
src/
├── etl/                          # 変換ロジック（環境非依存）
│   ├── sales_transformer.py
│   ├── hr_transformer.py
│   └── inventory_transformer.py
├── common/                       # 共通モジュール（環境非依存）
│   ├── config.py
│   ├── csv_handler.py
│   └── utils.py
├── local/                        # ローカル実行用
│   ├── python_native/            → Python標準版（PySparkなし）
│   ├── pyspark/                  → PySpark版
│   └── etl_orchestrator.py       → オーケストレーター
└── aws_glue/                     # AWS Glue実行用（本番環境）
    ├── sales_etl_job.py
    ├── hr_etl_job.py
    └── inventory_etl_job.py
```

**重要な原則:**
- 変換ロジック（`src/etl/`）と共通モジュール（`src/common/`）は完全に環境非依存
- ローカルでテストしたコードが本番（AWS Glue）でもそのまま動作
- Python標準版とPySpark版は同じ変換ロジックを使用

詳細は [ARCHITECTURE.md](ARCHITECTURE.md) を参照。

## ディレクトリ構成

```
projects/sdd/accounting/accounting_etl_sdd/
├── src/                       # プロダクションコード
│   ├── common/               # 共通モジュール
│   ├── etl/                  # ETL変換モジュール（環境非依存）
│   ├── aws_glue/             # AWS Glue実行用ETLジョブ
│   └── local/                # ローカル実行用ETLジョブ
│
├── tests/                     # テストコード
│   ├── unit/                 # 単体テスト
│   └── integration/          # 統合テスト
│
├── test_data/                 # テストデータ
│   ├── sales/
│   ├── hr/
│   ├── inventory/
│   └── expected/             # 期待値データ（検証用）
│
├── output/                    # 出力データ（ETL処理結果）
│
├── ddl/                       # DDL定義（参考資料）
│   ├── source/               # 上流システムテーブルDDL
│   └── erp/                  # 会計システムテーブルDDL
│
├── spec/                      # 仕様書
│   ├── req_def/              # 要件定義書
│   ├── basic_design/         # 基本設計書
│   └── detail_design/        # 詳細設計書
│
├── cleanup_output.py              # 出力フォルダクリーンアップ
├── merge_etl_outputs.py           # ETL出力統合スクリプト
├── run_pyspark_etl.sh             # PySpark ETL実行スクリプト
├── pytest.ini                     # pytest設定
├── requirements.txt               # Python依存パッケージ（本番用）
├── requirements-dev.txt           # 開発・テスト用依存パッケージ
├── README.md                      # このファイル
├── README_WINDOWS_PYSPARK.md      # Windows + Python 3.11以外の環境セットアップガイド
└── ARCHITECTURE.md                # アーキテクチャドキュメント
```

## AWS Glue実行（本番環境）

AWS Glue環境での実行は `src/aws_glue/` 配下のスクリプトを使用します。

**特徴**:
- **実行エンジン**: PySpark on AWS Glue（ローカルと同じエンジン）
- **データソース**: S3からCSV読み込み
- **データ出力**: S3へCSV書き込み
- **変換ロジック**: ローカルと完全に同じTransformerモジュールを使用（`src/etl/`）

### AWS Glueジョブパラメータ例

#### Sales ETL Job

```json
{
  "--s3_bucket": "your-s3-bucket-name",
  "--input_prefix": "input/sales",
  "--input_file": "sales_txn_export.csv",
  "--output_prefix": "output",
  "--output_file": "accounting_txn_interface_sales.csv",
  "--batch_id": "SALE_20251026_120000",
  "--limit_records": "1000",
  "--error_threshold": "100"
}
```

### セットアップ手順

1. **S3バケットに入力CSVをアップロード**
   ```bash
   aws s3 cp test_data/sales/sales_txn_export.csv s3://your-bucket/input/sales/
   aws s3 cp test_data/hr/hr_employee_org_export.csv s3://your-bucket/input/hr/
   aws s3 cp test_data/inventory/inv_movement_export.csv s3://your-bucket/input/inventory/
   ```

2. **共通モジュールをパッケージ化**
   ```bash
   cd src
   zip -r ../modules.zip common/ etl/
   cd ..
   aws s3 cp modules.zip s3://your-bucket/scripts/
   ```

3. **AWS Glue Jobを作成**
   - Script: `s3://your-bucket/scripts/sales_etl_job.py`
   - Python library path: `s3://your-bucket/scripts/modules.zip`
   - Job parameters: 上記JSON参照

4. **ジョブ実行**
   ```bash
   aws glue start-job-run --job-name sales-etl-job
   ```

## ライセンス

社内利用限定
