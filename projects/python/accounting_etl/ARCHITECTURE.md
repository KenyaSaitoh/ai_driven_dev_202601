# システムアーキテクチャ

## 設計原則

このプロジェクトは**共通変換ロジック**と**環境固有ラッパー**を分離した設計です。

```
┌─────────────────────────────────────────────────────────────┐
│                  共通変換ロジック（環境非依存）                  │
│                    src/etl/ + src/common/                    │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────┐      │
│  │SalesTransform│  │HRTransformer │  │InventoryTrans │      │
│  └──────────────┘  └──────────────┘  └───────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            ↑ ↑ ↑
            ┌───────────────┘ │ └───────────────┐
            │                 │                 │
┌───────────┴────┐  ┌─────────┴─────┐  ┌────────┴────────┐
│Python標準版    │  │  PySpark版    │  │  AWS Glue版     │
│(ThreadPool)    │  │  (Local)      │  │  (Production)   │
│ローカル小規模   │  │ローカル大規模  │  │本番環境（S3）    │
└────────────────┘  └───────────────┘  └─────────────────┘
```

## コード構成

```
src/
├── etl/                          ✅ 共通（環境非依存）
│   ├── sales_transformer.py         → 売上変換ロジック
│   ├── hr_transformer.py            → HR変換ロジック
│   └── inventory_transformer.py     → 在庫変換ロジック
│
├── common/                       ✅ 共通（環境非依存）
│   ├── utils.py                     → ユーティリティ
│   ├── config.py                    → 設定
│   └── csv_handler.py               → CSV入出力
│
├── local/                        🔧 環境固有ラッパー
│   ├── python_native/               → Python標準版
│   ├── pyspark/                     → PySpark版
│   └── etl_orchestrator.py          → オーケストレーター
│
└── aws_glue/                     🔧 環境固有ラッパー
    ├── sales_etl_job.py             → AWS Glue用エントリポイント
    ├── hr_etl_job.py
    └── inventory_etl_job.py
```

## 実行方式の比較

| 観点 | Python標準版 | PySpark版（ローカル） | AWS Glue版（本番） |
|-----|-------------|---------------------|-------------------|
| **パス** | `src/local/python_native/` | `src/local/pyspark/` | `src/aws_glue/` |
| **役割** | 開発・テスト | 本番検証 | 本番稼働 |
| **並列処理** | ThreadPoolExecutor | PySpark RDD | PySpark RDD |
| **データソース** | ローカルCSV | ローカルCSV | S3 |
| **変換ロジック** | ✅ 共通 | ✅ 共通 | ✅ 共通 |
| **セットアップ** | 30秒 | 5分 | AWSコンソール |
| **適用シーン** | 開発初期 | 本番前検証 | 本番稼働 |

## データフロー

### Python標準版

```
CSV File → csv.DictReader → ThreadPoolExecutor.map(transform) → csv.Writer → CSV File
```

### PySpark版

```
CSV File → spark.read.csv → RDD.map(transform) → collect() → csv.Writer → CSV File
```

### AWS Glue版

```
S3 → spark.read.csv → RDD.map(transform) → spark.write.csv → S3
```

## 重要な原則

**変換ロジック（`src/etl/`と`src/common/`）は完全に共通です。**

これにより：
1. ローカルでテストしたコードが本番でもそのまま動作
2. バグ修正が1箇所で全環境に反映
3. 開発効率が劇的に向上

## 開発ワークフロー

```
1. Python標準版で開発・デバッグ（高速イテレーション）
   python src/local/python_native/standalone_sales_etl_job.py --limit 10
   
2. PySpark版で本番相当の検証（AWS Glueと同じエンジン）
   ./run_pyspark_etl.sh sales --limit 1000
   
3. AWS Glueへデプロイ（ローカルと同じコードが動作）
   aws glue start-job-run --job-name sales-etl-job
```

## パフォーマンス特性

| 方式 | 起動時間 | 処理時間（1万レコード） | 総実行時間 |
|-----|---------|---------------------|----------|
| Python標準版 | ~0.1秒 | ~5秒 | 5.1秒 |
| PySpark版 | ~10秒 | ~5秒 | 15秒 |
| AWS Glue版 | ~30秒 | ~5秒 | 35秒 |

**推奨:**
- 小規模データ（〜1万レコード）: Python標準版
- 大規模データ（10万〜）: PySpark版/Glue版
