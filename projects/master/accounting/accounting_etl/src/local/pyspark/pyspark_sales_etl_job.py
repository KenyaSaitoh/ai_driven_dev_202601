# 売上データETLジョブ（ローカル実行用・PySpark版）

import sys
import os
import argparse
from datetime import datetime
import logging

# プロジェクトルートをパスに追加
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))

from pyspark.sql import SparkSession
from pyspark.sql.types import StructType, StructField, StringType
from src.etl.sales_transformer import SalesTransformer

# ロギング設定
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


def main():
    parser = argparse.ArgumentParser(description='Sales ETL Job (Local PySpark)')
    parser.add_argument('--input-dir', type=str, default='test_data/sales',
                        help='Input directory path')
    parser.add_argument('--input-file', type=str, default='sales_txn_export.csv',
                        help='Input CSV filename (relative to input-dir)')
    parser.add_argument('--output-dir', type=str, default='output',
                        help='Output directory path')
    parser.add_argument('--output-file', type=str, default='accounting_txn_interface_sales.csv',
                        help='Output CSV filename (relative to output-dir)')
    parser.add_argument('--limit', type=int, default=None,
                        help='Limit number of records to process')
    parser.add_argument('--error-threshold', type=int, default=100,
                        help='Maximum number of errors before aborting')
    parser.add_argument('--master', type=str, default='local[*]',
                        help='Spark master URL (default: local[*] for all cores)')
    
    args = parser.parse_args()
    
    # Sparkセッションの初期化
    spark = SparkSession.builder \
        .appName("Sales ETL Job (Local)") \
        .master(args.master) \
        .config("spark.driver.memory", "2g") \
        .config("spark.executor.memory", "2g") \
        .config("spark.sql.shuffle.partitions", "4") \
        .config("spark.python.worker.faulthandler.enabled", "true") \
        .getOrCreate()
    
    # ログレベルをWARNに設定（Sparkの冗長なログを抑制）
    spark.sparkContext.setLogLevel("WARN")
    
    try:
        logger.info("=" * 60)
        logger.info("Sales ETL Job Started (Local PySpark Execution)")
        logger.info("=" * 60)
        
        batch_id = f"SALE_{datetime.now().strftime('%Y%m%d_%H%M%S')}"
        
        # パスを構築
        input_path = os.path.join(args.input_dir, args.input_file)
        output_path = os.path.join(args.output_dir, args.output_file)
        
        logger.info(f"Batch ID: {batch_id}")
        logger.info(f"Input File: {input_path}")
        logger.info(f"Output File: {output_path}")
        logger.info(f"Limit Records: {args.limit if args.limit else 'No limit'}")
        logger.info(f"Error Threshold: {args.error_threshold}")
        logger.info(f"Spark Master: {args.master}")
        logger.info("=" * 60)
        
        # CSVから売上データを読み込み（PySpark DataFrame）
        logger.info(f"Reading source data from {input_path}...")
        df = spark.read.csv(
            input_path,
            header=True,
            inferSchema=True,
            encoding='utf-8'
        )
        
        # レコード数制限
        if args.limit:
            df = df.limit(args.limit)
        
        record_count = df.count()
        if record_count == 0:
            logger.warning("No source data found")
            spark.stop()
            return
        
        logger.info(f"Fetched {record_count} records from source")
        
        # データ変換（PySpark RDDで並列処理）
        logger.info("Transforming sales data to accounting entries...")
        
        # batch_idとproject_rootをブロードキャスト（各ワーカーで使用）
        batch_id_bc = spark.sparkContext.broadcast(batch_id)
        project_root = os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..'))
        project_root_bc = spark.sparkContext.broadcast(project_root)
        
        # DataFrameをRDDに変換して並列処理
        source_rdd = df.rdd.map(lambda row: row.asDict())
        
        # 変換処理を並列実行（各ワーカーでTransformerをインスタンス化）
        def transform_with_error_handling(record):
            try:
                # ワーカープロセスでsys.pathを設定
                import sys
                if project_root_bc.value not in sys.path:
                    sys.path.insert(0, project_root_bc.value)
                
                # 各ワーカープロセスでTransformerをインスタンス化（シリアライズ不要）
                from src.etl.sales_transformer import SalesTransformer
                transformer = SalesTransformer(batch_id_bc.value)
                transformed = transformer.transform_record(record)
                return ('success', transformed)
            except Exception as e:
                # エラー詳細を含める
                import traceback
                error_detail = traceback.format_exc()
                return ('error', {'record': record, 'error': str(e), 'traceback': error_detail})
        
        result_rdd = source_rdd.map(transform_with_error_handling)
        
        # 成功とエラーを分離
        success_rdd = result_rdd.filter(lambda x: x[0] == 'success').map(lambda x: x[1])
        error_rdd = result_rdd.filter(lambda x: x[0] == 'error')
        
        # 結果を収集
        accounting_records = success_rdd.collect()
        error_records = error_rdd.collect()
        error_count = len(error_records)
        
        logger.info(f"Transformation completed: {len(accounting_records)} accounting entries generated")
        logger.info(f"Errors: {error_count}")
        
        # エラー詳細をログ出力
        if error_records:
            logger.error("Error details:")
            for err in error_records[:5]:  # 最初の5件のみ表示
                error_info = err[1]
                logger.error(f"  Record ID: {error_info.get('record', {}).get('export_id')}")
                logger.error(f"  Error: {error_info.get('error')}")
                if error_info.get('traceback'):
                    logger.error(f"  Traceback:\n{error_info.get('traceback')}")
        
        # エラー閾値チェック
        if error_count >= args.error_threshold:
            logger.error(f"Error threshold ({args.error_threshold}) exceeded. Aborting.")
            raise Exception(f"Too many errors: {error_count}")
        
        # CSVへ書き込み（Python標準ライブラリ）
        if accounting_records:
            logger.info(f"Writing to {output_path}...")
            
            import csv
            os.makedirs(args.output_dir, exist_ok=True)
            
            # CSVファイルに書き込み
            with open(output_path, 'w', newline='', encoding='utf-8') as csvfile:
                if accounting_records:
                    # ヘッダーを最初のレコードのキーから取得
                    fieldnames = accounting_records[0].keys()
                    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
                    
                    writer.writeheader()
                    writer.writerows(accounting_records)
            
            logger.info(f"Successfully wrote {len(accounting_records)} records")
        
        logger.info("=" * 60)
        logger.info("Sales ETL Job Completed Successfully")
        logger.info("=" * 60)
        
    except Exception as e:
        logger.error("=" * 60)
        logger.error("Sales ETL Job Failed")
        logger.error("=" * 60)
        logger.error(f"Error: {str(e)}")
        import traceback
        traceback.print_exc()
        logger.error("=" * 60)
        sys.exit(1)
    finally:
        # Sparkセッションを停止
        spark.stop()


if __name__ == "__main__":
    main()


