# ERP会計統合ETLシステム 基本設計書

## 第1部: アーキテクチャ設計

---

## 1. システムアーキテクチャ概要

### 1.1 アーキテクチャの基本方針

本システムは**データベース製品非依存**のCSV入出力ベースETLシステムとして設計されています。

#### 設計原則

1. **データベース非依存**: 特定のRDB製品に依存せず、CSV形式で入出力
2. **環境共通化**: ローカル実行とクラウド実行で共通の変換ロジックを使用
3. **シンプル性**: 標準的なCSV形式により、システム間連携を簡素化
4. **保守性**: データストアへの依存を排除し、保守性を向上

### 1.2 全体構成（ローカル実行）

```
┌─────────────────────────────────────────────────────────────┐
│                   ローカル実行環境                             │
│                                                              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │           上流システム（CSVエクスポート）                │  │
│  │                                                         │  │
│  │  ┌───────────┐  ┌───────────┐  ┌───────────┐         │  │
│  │  │   売上    │  │   人事    │  │   在庫    │         │  │
│  │  │  システム │  │  システム │  │  システム │         │  │
│  │  └─────┬─────┘  └─────┬─────┘  └─────┬─────┘         │  │
│  │        │ CSV出力       │              │               │  │
│  └────────┼───────────────┼──────────────┼───────────────┘  │
│           ↓               ↓              ↓                  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              test_data/（入力データ）                   │  │
│  │                                                         │  │
│  │  ・test_data/sales/sales_txn_export.csv                │  │
│  │  ・test_data/hr/hr_employee_org_export.csv             │  │
│  │  ・test_data/inventory/inv_movement_export.csv         │  │
│  └────────┬──────────────────────────────────────────────┘  │
│           │                                                  │
│           ↓                                                  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │          Python ETL処理（src/local/）                   │  │
│  │                                                         │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │   etl_orchestrator.py（統合オーケストレーター）  │  │  │
│  │  │   ・並列/順次実行制御                            │  │  │
│  │  │   ・個別ファイル統合                            │  │  │
│  │  │   ・Python Native/PySpark選択可能               │  │  │
│  │  └────────────┬────────────────────────────────────┘  │  │
│  │               │                                         │  │
│  │               ↓ 並列実行                                │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐           │  │
│  │  │ python_  │  │ python_  │  │ python_  │           │  │
│  │  │ native/ │  │ native/ │  │ native/ │           │  │
│  │  │ pyspark/ │  │ pyspark/ │  │ pyspark/ │           │  │
│  │  │ Sales ETL│  │ HR ETL   │  │ Inv ETL  │           │  │
│  │  └────┬─────┘  └────┬─────┘  └────┬─────┘           │  │
│  │       │             │              │                   │  │
│  │       └─────────────┴──────────────┘                   │  │
│  │                     │                                   │  │
│  │                     ↓ 共通変換ロジック                   │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │        共通Transformerモジュール（src/etl/）     │  │  │
│  │  │  ・SalesTransformer（環境非依存）                │  │  │
│  │  │  ・HRTransformer（環境非依存）                   │  │  │
│  │  │  ・InventoryTransformer（環境非依存）            │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  └────────┬──────────────────────────────────────────────┘  │
│           │                                                  │
│           ↓                                                  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              output/（出力データ）                       │  │
│  │                                                         │  │
│  │  accounting_txn_interface.csv（統合ファイル）           │  │
│  │  ・売上: 1000件                                         │  │
│  │  ・人事: 150件                                          │  │
│  │  ・在庫: 1331件                                         │  │
│  │  = 合計: 2481件（全システム統合）                        │  │
│  └────────┬──────────────────────────────────────────────┘  │
│           │                                                  │
│           ↓ CSV取込                                         │
│  ┌───────────────────────────────────────────────────────┐  │
│  │            会計ERPパッケージ                            │  │
│  │  ・統合CSVを一括取込                                    │  │
│  │  ・会計システムの標準インポート機能を使用                │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 1.3 全体構成（AWS Glue実行）

```
┌─────────────────────────────────────────────────────────────┐
│                     AWS Cloud                                │
│                                                              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              Amazon S3（データストア）                   │  │
│  │                                                         │  │
│  │  input/（入力データ）                                   │  │
│  │  ├── sales/sales_txn_export.csv                        │  │
│  │  ├── hr/hr_employee_org_export.csv                     │  │
│  │  └── inventory/inv_movement_export.csv                 │  │
│  └────────┬──────────────────────────────────────────────┘  │
│           │                                                  │
│           ↓ S3読込                                          │
│  ┌───────────────────────────────────────────────────────┐  │
│  │          AWS Glue ETL Jobs（src/aws_glue/）            │  │
│  │                                                         │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐           │  │
│  │  │ Sales    │  │ HR       │  │Inventory │           │  │
│  │  │ ETL Job  │  │ ETL Job  │  │ ETL Job  │           │  │
│  │  └────┬─────┘  └────┬─────┘  └────┬─────┘           │  │
│  │       │             │              │                   │  │
│  │       └─────────────┴──────────────┘                   │  │
│  │                     │                                   │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │        共通Transformerモジュール（src/etl/）     │  │  │
│  │  │  ※ローカル実行と共通のロジック                   │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  └────────┬──────────────────────────────────────────────┘  │
│           │                                                  │
│           ↓ S3書込                                          │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              Amazon S3（データストア）                   │  │
│  │                                                         │  │
│  │  output/                                                │  │
│  │  └── accounting_txn_interface.csv（統合ファイル）       │  │
│  └────────┬──────────────────────────────────────────────┘  │
│           │                                                  │
│           ↓ S3→オンプレミス連携                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │            会計ERPパッケージ                            │  │
│  │  ・S3から統合CSVをダウンロード                          │  │
│  │  ・会計システムの標準インポート機能を使用                │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. アーキテクチャパターン

### 2.1 ETLアプローチ

本システムは**シンプルETLパターン**を採用する。

```
Extract（抽出） → Transform（変換） → Load（出力）

1. Extract:  CSV読込（ファイルシステム or S3）
2. Transform: Python変換ロジック（共通Transformer）
3. Load:      CSV出力（統合ファイル生成）
```

#### 採用理由

1. **シンプル性**: CSV形式により複雑なデータストア依存を排除
2. **保守性**: 標準的なファイル形式で可読性が高く、デバッグが容易
3. **互換性**: 会計パッケージの標準インポート機能をそのまま活用
4. **移植性**: 特定のクラウド・データベース製品に依存しない

### 2.2 データフロー

#### ローカル実行フロー

```
1. 事前準備
   └── cleanup_output.py --force
       └── 前回実行の出力ファイルをクリーンアップ

2. ETL実行
   └── etl_orchestrator.py --execution_mode parallel --systems ALL
       ├── standalone_sales_etl_job.py
       │   └── test_data/sales/ → output/accounting_txn_interface_sales.csv
       ├── standalone_hr_etl_job.py
       │   └── test_data/hr/ → output/accounting_txn_interface_hr.csv
       └── standalone_inventory_etl_job.py
           └── test_data/inventory/ → output/accounting_txn_interface_inventory.csv

3. 統合処理
   └── merge_output_files()
       └── 個別CSV → output/accounting_txn_interface.csv（統合）

4. 会計取込
   └── 会計ERPパッケージ
       └── accounting_txn_interface.csvをインポート
```

#### AWS Glue実行フロー

```
1. S3アップロード
   └── aws s3 cp test_data/ s3://bucket/input/ --recursive

2. Glueジョブ実行
   ├── sales_etl_job.py: S3 input/sales/ → S3 output/sales.csv
   ├── hr_etl_job.py: S3 input/hr/ → S3 output/hr.csv
   └── inventory_etl_job.py: S3 input/inventory/ → S3 output/inventory.csv

3. 統合処理（Lambda or Glue）
   └── 個別CSV → S3 output/accounting_txn_interface.csv

4. 会計取込
   └── S3 download → 会計ERPパッケージインポート
```

---

## 3. コンポーネント設計

### 3.1 実行環境の分離

```
src/
├── local/                          # ローカル実行用
│   ├── etl_orchestrator.py         # オーケストレーター
│   ├── python_native/              # Python標準版
│   │   ├── standalone_sales_etl_job.py
│   │   ├── standalone_hr_etl_job.py
│   │   └── standalone_inventory_etl_job.py
│   └── pyspark/                    # PySpark版
│       ├── standalone_sales_etl_job.py
│       ├── standalone_hr_etl_job.py
│       └── standalone_inventory_etl_job.py
│
├── aws_glue/                       # AWS Glue実行用
│   ├── sales_etl_job.py
│   ├── hr_etl_job.py
│   └── inventory_etl_job.py
│
├── common/                         # 環境共通モジュール
│   ├── config.py
│   ├── csv_handler.py
│   └── utils.py
│
└── etl/                            # 変換ロジック（環境非依存）
    ├── sales_transformer.py
    ├── hr_transformer.py
    └── inventory_transformer.py

tests/                              # テストコード
├── unit/                           # 単体テスト
│   └── test_etl/
└── integration/                    # 統合テスト
    └── test_etl_output.py

test_data/expected/                 # 期待値データ（テスト用）
```

#### 設計方針

- **src/local/python_native/**: Python標準ライブラリ、ThreadPoolExecutor
- **src/local/pyspark/**: PySpark（ローカルモード）、RDD変換
- **src/aws_glue/**: S3ベース、boto3 + PySpark実行
- **src/common/**: CSV読み書き、ユーティリティ
- **src/etl/**: **環境非依存の変換ロジック（共通・再利用）**
- **tests/**: pytest単体テスト、統合テスト（業務キー照合）

### 3.2 統合オーケストレーター

#### etl_orchestrator.py（ローカル用）

**責務**:
1. 複数ETLジョブの並列/順次実行制御
2. 個別出力ファイルの統合処理
3. エラーハンドリングと結果レポート

**主要機能**:

**run_parallel()**:
- 並列実行機能
- ThreadPoolExecutorで並列実行
- 各ジョブをsubprocessで起動
    
**run_sequential()**:
- 順次実行機能
- エラー発生時は後続スキップ
    
**merge_output_files()**:
- 個別ファイル統合機能
- 各システムの個別CSVを読込
- 1つの統合CSVに結合
- 個別ファイルは削除（オプション）

**パラメータ**:
- `--execution_mode`: parallel/sequential
- `--systems`: ALL or SALE,HR,INV
- `--input-base-dir`: デフォルト test_data
- `--output-dir`: デフォルト output
- `--output-file`: デフォルト accounting_txn_interface.csv
- `--cleanup`: 実行前クリーンアップ（開発用）

### 3.3 個別ETLジョブ

#### standalone_sales_etl_job.py（例）

**メイン処理フロー**:

1. **パラメータ取得**:
   - input_dir: 入力ディレクトリ（例: test_data/sales）
   - output_dir: 出力ディレクトリ（例: output）

2. **CSV読込**:
   - ソースデータをCSVファイルから読み込む

3. **変換処理**:
   - Transformerインスタンスを生成
   - 各レコードに対して変換処理を実行
   - 変換後レコードをリストに追加

4. **CSV出力**:
   - 変換後レコードをCSVファイルに書き込む

**パラメータ**:
- `--input-dir`: 入力ディレクトリ
- `--input-file`: 入力CSVファイル名
- `--output-dir`: 出力ディレクトリ
- `--output-file`: 出力CSVファイル名
- `--limit`: 処理件数制限（テスト用）
- `--error-threshold`: エラー閾値

---

## 4. 非機能要件

### 4.1 性能要件

| 項目 | 要件 | 実装 |
|------|------|------|
| 処理時間 | 全システム合計2,481件を5秒以内 | 並列実行で1.2秒達成 |
| スループット | 1日1回のバッチ実行 | 十分対応可能 |
| 同時実行 | 3システム並列実行 | Python Native: ThreadPoolExecutor<br>PySpark: local[*] |

### 4.2 可用性要件

| 項目 | 要件 | 実装 |
|------|------|------|
| エラーハンドリング | 個別ジョブエラー時も他ジョブ継続 | 並列実行で個別管理 |
| リトライ | ジョブ単位で再実行可能 | システム指定実行対応 |
| ログ | 全処理ログを標準出力 | loggingモジュール |

### 4.3 保守性要件

| 項目 | 要件 | 実装 |
|------|------|------|
| テスタビリティ | 個別ジョブ単独実行可能 | standalone_*_etl_job.py |
| 可読性 | CSV形式で中間結果確認可能 | 全ファイルCSV |
| 拡張性 | 新規システム追加が容易 | Transformer追加のみ |

---

## 5. セキュリティ設計

### 5.1 データアクセス制御

#### ローカル実行
- ファイルシステムのOS権限で制御
- test_data/: 読取専用
- output/: 読み書き権限

#### AWS Glue実行
- IAMロールによるS3アクセス制御
- 入力バケット: 読取専用
- 出力バケット: 読み書き権限

### 5.2 データ保護

#### 機密情報の取扱い
- 個人情報（PII）: 氏名、給与情報等
- 取扱方針:
  - CSV出力時に暗号化オプション（将来対応）
  - 出力ファイルは業務終了後削除
  - AWS S3は暗号化必須

---

## 6. 運用設計

### 6.1 バッチ実行フロー

```
本番運用フロー:

1. 事前クリーンアップ
   python cleanup_output.py --force

2. ETLバッチ実行
   python src/local/etl_orchestrator.py \
     --execution_mode parallel \
     --systems ALL

3. 結果確認
   dir output
   # accounting_txn_interface.csv存在確認

4. 会計システム取込
   会計ERPの標準インポート機能で取込

5. 事後処理
   - ログアーカイブ
   - 出力ファイルバックアップ
```

### 6.2 監視・アラート

#### ローカル実行
- 標準出力のログを監視
- エラー発生時はexit code != 0
- 成功/失敗をジョブスケジューラーで検知

#### AWS Glue実行
- CloudWatch Logs
- CloudWatch Metrics
- SNS通知（エラー時）

---

## 7. 今後の拡張

### 7.1 短期（3ヶ月以内）
- [ ] 増分処理対応（差分抽出）
- [ ] エラーデータの再処理機能
- [ ] 統計情報レポート出力

### 7.2 中期（6ヶ月以内）
- [ ] スケジュール実行自動化
- [ ] 会計システムへの自動取込
- [ ] データ品質チェック強化

### 7.3 長期（1年以内）
- [ ] リアルタイム連携対応
- [ ] 他システム（固定資産、経費等）追加
- [ ] BIツール連携

---

## 付録

### A. 関連ドキュメント

- [基本設計書 - モジュール・データモデル編](./02_module_data_design.md)
- [詳細設計書 - 売上ETL](../detail_design/sales_txn_export.md)
- [詳細設計書 - 人事ETL](../detail_design/hr_employee_org_export.md)
- [詳細設計書 - 在庫ETL](../detail_design/inv_movement_export.md)
- [README](../../README.md)

### B. 用語集

| 用語 | 説明 |
|------|------|
| CSV | Comma-Separated Values。カンマ区切りテキストファイル形式 |
| ETL | Extract-Transform-Load。データ統合処理 |
| Transformer | ETL変換ロジックを実装したモジュール |
| オーケストレーター | 複数ジョブの実行を統制するモジュール |
| 統合ファイル | 全システムのデータを1つにまとめたCSVファイル |
