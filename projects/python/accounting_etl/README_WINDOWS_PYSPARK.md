# Windows埋め込み版Python + PySpark 実行ガイド

このガイドでは、Windows環境で埋め込み版Python（python-3.11.7-embed-amd64）を使用してPySpark ETLを実行するための手順を説明します。

> **Note**: 
> - このガイドはWindows環境専用です
> - Git Bash使用を前提としています
> - 一般的な環境（システムインストール版Python）については [README.md](README.md) を参照してください

## 前提条件

以下がインストール済みであることを前提とします：

- **Python 3.11埋め込み版**（`python-3.11.7-embed-amd64`）がローカルに配置済み
  - 例: `D:\Python\python-3.11.7-embed-amd64`
- **pip**がPython環境にインストール済み
- **Java 11以上**がシステムにインストール済み
- **Git Bash**がインストール済み

## セットアップ手順

### 1. プロジェクトディレクトリに移動

```bash
cd projects/python/accounting_etl
```

### 2. 開発・テスト環境のインストール

埋め込み版Pythonを使用してPySpark+テストツールをインストールします。

```bash
# Pythonのパス（例）
/d/Python/python-3.11.7-embed-amd64/python.exe -m pip install -r requirements-dev.txt
```

> **Note**: 
> - このファイルにはPySpark、pytest、その他のテストツールが含まれています
> - Pythonのインストール場所が異なる場合は、パスを適宜変更してください

## PySpark ETL実行方法

PySpark版のETLジョブは、`src/local/pyspark/pyspark_*.py` という命名規則のファイルを使用します。

### 出力フォルダのクリーンアップ（初回/終了時）

```bash
# 環境変数を設定
export PYTHON311_PATH="/d/Python/python-3.11.7-embed-amd64/python.exe"

# 前回の出力をクリーンアップ
$PYTHON311_PATH cleanup_output.py --force
```

> **Note**: 
> - プロジェクト開始時に1回実行（前回の出力をクリア）
> - プロジェクト終了時に1回実行（クリーンアップ）

### 全ジョブ実行

```bash
# 環境変数を設定
export PYTHON311_PATH="/d/Python/python-3.11.7-embed-amd64/python.exe"

# 全ETLジョブを並列実行
./run_pyspark_etl.sh all
```

### 個別ジョブ実行

```bash
# 環境変数を設定
export PYTHON311_PATH="/d/Python/python-3.11.7-embed-amd64/python.exe"

# Sales ETL
./run_pyspark_etl.sh sales

# HR ETL
./run_pyspark_etl.sh hr

# Inventory ETL
./run_pyspark_etl.sh inventory
```

> **Note**: これらのスクリプトは内部で以下のPySparkジョブを実行します：
> - `src/local/pyspark/pyspark_sales_etl_job.py`
> - `src/local/pyspark/pyspark_hr_etl_job.py`
> - `src/local/pyspark/pyspark_inventory_etl_job.py`

### 出力ファイルの確認

```bash
# 出力ファイル一覧
ls -lh output/

# 統合ファイルの内容確認
head -n 10 output/accounting_txn_interface.csv

# レコード数確認
tail -n +2 output/accounting_txn_interface.csv | wc -l
```

## 自動テスト（検証）

ETL実行後、出力結果をpytestで自動検証します。

```bash
# 環境変数を設定
export PYTHON311_PATH="/d/Python/python-3.11.7-embed-amd64/python.exe"

# 単体テスト（Transformerロジックの検証）
$PYTHON311_PATH -m pytest tests/unit/ -v

# 統合テスト（ETL出力ファイルの検証）
$PYTHON311_PATH -m pytest tests/integration/ -v

# 全テストを実行
$PYTHON311_PATH -m pytest tests/ -v
```

> **Note**: 統合テストは、上記「PySpark ETL実行方法」でETLを実行し、`output/accounting_txn_interface.csv`が生成された後に実行してください。

## トラブルシューティング

### 環境変数が設定されていない

```bash
# 環境変数の確認
echo $PYTHON311_PATH

# 空の場合は設定
export PYTHON311_PATH="/d/Python/python-3.11.7-embed-amd64/python.exe"
```

### Pythonが見つからない

```bash
# Pythonの存在確認
ls -l /d/Python/python-3.11.7-embed-amd64/python.exe

# バージョン確認
/d/Python/python-3.11.7-embed-amd64/python.exe --version
```

### PySparkが動作しない

1. **Javaがインストールされているか確認**
   ```bash
   java -version
   ```

2. **環境変数PYTHON311_PATHが正しく設定されているか確認**
   ```bash
   echo $PYTHON311_PATH
   ```

3. **PySparkがインストールされているか確認**
   ```bash
   /d/Python/python-3.11.7-embed-amd64/python.exe -m pip show pyspark
   ```

### メモリ不足エラー

PySparkは各ジョブで約2GBのメモリを使用します。システムに十分なメモリがあることを確認してください。

### パス関連のエラー

Git Bashでは、パスを `/d/Python/...` のようにスラッシュ（`/`）で記述してください。

### Spark警告メッセージ（無害）

以下の警告は**正常な動作**です。ジョブは正常に完了しています：

```
WARN Shell: Did not find winutils.exe
```
- Windows環境でのHadoop警告
- 機能に影響なし

```
WARN NativeCodeLoader: Unable to load native-hadoop library
```
- ネイティブライブラリの警告
- Java実装で代替されるため問題なし

```
SparkException: Could not find HeartbeatReceiver
```
- **ジョブ完了後**のクリーンアップ時の警告
- ETL処理は正常に完了しています
- ハートビートスレッドのシャットダウン順序によるものです
- **重要**: このエラーは処理結果に影響しません

**見分け方**:
```bash
# ✓ 問題なし（このパターンなら安全）
INFO - Successfully wrote XXX records
INFO - ETL Job Completed Successfully
WARN/ERROR - （その後にSparkの警告）  ← ★この順序なら無害
[timestamp] ETL completed successfully!

# ✗ 本当の問題（このパターンなら要対処）
ERROR - Failed to transform record
ERROR - Failed to write output
[timestamp] ETL FAILED!
```

**確認方法**:
```bash
# 出力ファイルの検証
bash verify_output.sh
```

## インストール場所について

この手順書では、以下の場所を例として使用しています：

```
D:\Python\python-3.11.7-embed-amd64\
```

**別の場所にインストールしている場合：**
1. 上記すべての手順のパスを適宜変更
2. 環境変数`PYTHON311_PATH`を正しいパスに設定

例：
```bash
# 別の場所の例
export PYTHON311_PATH="/c/Python311/python.exe"
```

## 関連ドキュメント

- [README.md](README.md) - プロジェクト全体の概要（システムインストール版Python前提）
- [ARCHITECTURE.md](ARCHITECTURE.md) - アーキテクチャ詳細
- [src/local/README.md](src/local/README.md) - ローカル実行の詳細
- [tests/README.md](tests/README.md) - テスト実行ガイド
