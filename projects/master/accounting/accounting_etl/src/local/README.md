# ローカル実行スクリプト ディレクトリ構成

このディレクトリには、ローカル環境でETL処理を実行するためのスクリプトが格納されています。

## 📁 ディレクトリ構成

```
src/local/
├── python_native/              # Python標準版（PySparkなし）
│   ├── standalone_sales_etl_job.py
│   ├── standalone_hr_etl_job.py
│   └── standalone_inventory_etl_job.py
│
├── pyspark/                    # PySpark版（並列分散処理）
│   ├── standalone_sales_etl_job.py
│   ├── standalone_hr_etl_job.py
│   └── standalone_inventory_etl_job.py
│
└── etl_orchestrator.py         # オーケストレーター（どちらでも使用可能）
```

## 🎯 各バージョンの特徴と使い分け

### **Python標準版** (`python_native/`)

**特徴:**
- ✅ **PySparkなし**で動作（Python標準ライブラリのみ）
- ✅ セットアップが簡単（`pip install boto3`のみ）
- ✅ 小規模データ（数千〜数万レコード）に最適
- ✅ 環境依存が少ない
- 🔧 `ThreadPoolExecutor`で並列処理（マルチスレッド）

**適用シーン:**
- 開発初期の動作確認
- PySparkのセットアップが困難な環境
- 小規模データのテスト実行
- CI/CDパイプラインでの軽量テスト

**実行方法:**
```bash
python src/local/python_native/standalone_sales_etl_job.py --limit 5 --max-workers 4
```

### **PySpark版** (`pyspark/`)

**特徴:**
- ✅ **PySpark**で並列分散処理
- ✅ 大規模データ（数十万〜数百万レコード）に最適
- ✅ AWS Glueと**同じエンジン**で動作
- ✅ 本番環境の動作を正確に再現
- 🔧 RDDベースの並列処理（マルチプロセス）

**適用シーン:**
- 本番環境（AWS Glue）の事前検証
- 大規模データの処理
- パフォーマンステスト
- AWS Glueへのデプロイ前のテスト

**実行方法:**
```bash
# 依存パッケージのインストール（初回のみ）
pip install -r requirements.txt

# 実行
./run_pyspark_etl.sh sales --limit 5
```

## 🔄 コード構成の詳細

### **共通部分（環境非依存）**

以下のコンポーネントは**全ての実行環境で共通**です：

```
src/
├── etl/                        # ✅ 共通：変換ロジック
│   ├── sales_transformer.py       → 完全共通
│   ├── hr_transformer.py           → 完全共通
│   └── inventory_transformer.py    → 完全共通
│
├── common/                     # ✅ 共通：ユーティリティ
│   ├── utils.py                    → 完全共通
│   ├── account_mapper.py           → 完全共通
│   └── data_validator.py           → 完全共通
│
└── aws_glue/                   # ⚙️ AWS Glue専用
    └── glue_job_*.py               → AWS Glue実行用エントリポイント
```

**重要:** 
- `src/etl/`と`src/common/`の変換ロジックは**環境を問わず完全に共通**
- `src/local/`と`src/aws_glue/`は実行環境の**ラッパー**に過ぎません
- ローカルでテストした変換ロジックは、AWS Glueでもそのまま動作します

### **環境依存部分（ラッパー）**

| 環境 | パス | 役割 | 使用技術 |
|-----|------|------|---------|
| **ローカル（標準）** | `src/local/python_native/` | CSV読み書き + ThreadPoolExecutor | Python標準ライブラリ |
| **ローカル（PySpark）** | `src/local/pyspark/` | CSV読み書き + PySpark RDD | PySpark 3.5.3 |
| **本番（AWS Glue）** | `src/aws_glue/` | S3読み書き + PySpark | AWS Glue + PySpark |

## 🔀 実行フロー比較

### Python標準版

```
入力CSV → csv.DictReader 
       → ThreadPoolExecutor.map(transformer.transform_record) 
       → csv.DictWriter → 出力CSV
```

### PySpark版

```
入力CSV → spark.read.csv 
       → RDD.map(transformer.transform_record) 
       → Python csv.DictWriter → 出力CSV
```

### AWS Glue版

```
S3 → spark.read.csv 
  → RDD.map(transformer.transform_record) 
  → spark.write.csv → S3
```

## 📊 パフォーマンス比較

| データサイズ | Python標準版 | PySpark版 | 推奨 |
|------------|-------------|-----------|-----|
| 〜1,000レコード | 数秒 | 10-15秒（起動オーバーヘッド） | 標準版 |
| 1,000〜10,000 | 数秒〜数十秒 | 10-20秒 | 標準版 |
| 10,000〜100,000 | 数十秒〜数分 | 20-60秒 | PySpark版 |
| 100,000〜 | 数分以上 | 数分 | PySpark版 |

## 🚀 実行例

### Python標準版の実行

```bash
# Sales ETL（5レコード、4並列）
python src/local/python_native/standalone_sales_etl_job.py --limit 5 --max-workers 4

# HR ETL
python src/local/python_native/standalone_hr_etl_job.py --payroll-period 2025-10

# Inventory ETL
python src/local/python_native/standalone_inventory_etl_job.py --movement-types RECEIPT,SHIPMENT
```

### PySpark版の実行

```bash
# 依存パッケージのインストール（初回のみ）
pip install -r requirements.txt

# Sales ETL（5レコード）
./run_pyspark_etl.sh sales --limit 5

# 全ジョブ実行
./run_pyspark_etl.sh all
```

## 💡 開発ガイドライン

### 新しい変換ロジックを追加する場合

1. **変換ロジックは`src/etl/`に実装**（環境非依存）
2. **ユーティリティは`src/common/`に実装**（環境非依存）
3. **実行スクリプトは環境ごとに作成**:
   - `src/local/python_native/` → Python標準版
   - `src/local/pyspark/` → PySpark版
   - `src/aws_glue/` → AWS Glue版

### テスト戦略

1. **まず標準版でクイックテスト**（数秒で動作確認）
2. **PySpark版で本番相当の検証**（AWS Glue動作を保証）
3. **AWS Glueへデプロイ**（ローカルと同じコードが動作）

## 🔧 トラブルシューティング

### PySparkが動かない場合

→ Python標準版を使用してください：
```bash
python src/local/python_native/standalone_sales_etl_job.py --limit 5
```

### Python標準版が遅い場合

→ `--max-workers`を増やしてください：
```bash
python src/local/python_native/standalone_sales_etl_job.py --max-workers 8
```

### 環境の切り替え

- **Python標準版**: `src/local/python_native/`のスクリプトを直接実行
- **PySpark版**: `run_pyspark_etl.sh`を使用
- **どちらも**: `etl_orchestrator.py`は両方で使用可能（ただし調整が必要）

