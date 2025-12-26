# Sales ETL Job - Python Native Version (ThreadPoolExecutor)
#
# このバージョンはPySparkを使用せず、Python標準ライブラリのみで動作します。
# PySparkがインストールされていない環境や、小規模データの処理に適しています。

import os
import sys
import csv
import logging
import argparse
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed

# プロジェクトルートをパスに追加
project_root = os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..'))
if project_root not in sys.path:
    sys.path.insert(0, project_root)

from src.etl.sales_transformer import SalesTransformer

# ロギング設定
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


def process_record(record, transformer):
    # 単一レコードを処理
    try:
        transformed = transformer.transform_record(record)
        return ('success', transformed)
    except Exception as e:
        return ('error', {'record': record, 'error': str(e)})


def main():
    parser = argparse.ArgumentParser(description='Sales ETL Job (Python Native)')
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
    parser.add_argument('--max-workers', type=int, default=4,
                        help='Maximum number of parallel workers')
    
    args = parser.parse_args()
    
    try:
        # パス設定
        input_path = os.path.join(args.input_dir, args.input_file)
        output_path = os.path.join(args.output_dir, args.output_file)
        
        # バッチIDの生成
        batch_id = f"SALE_{datetime.now().strftime('%Y%m%d_%H%M%S')}"
        
        logger.info("=" * 60)
        logger.info("Sales ETL Job Started (Python Native - ThreadPoolExecutor)")
        logger.info("=" * 60)
        logger.info(f"Batch ID: {batch_id}")
        logger.info(f"Input File: {input_path}")
        logger.info(f"Output File: {output_path}")
        logger.info(f"Limit Records: {args.limit if args.limit else 'None'}")
        logger.info(f"Error Threshold: {args.error_threshold}")
        logger.info(f"Max Workers: {args.max_workers}")
        logger.info("=" * 60)
        
        # データ読み込み
        logger.info(f"Reading source data from {input_path}...")
        source_records = []
        
        with open(input_path, 'r', encoding='utf-8') as csvfile:
            reader = csv.DictReader(csvfile)
            for i, record in enumerate(reader):
                if args.limit and i >= args.limit:
                    break
                source_records.append(record)
        
        logger.info(f"Fetched {len(source_records)} records from source")
        
        # データ変換（ThreadPoolExecutorで並列処理）
        logger.info("Transforming sales data to accounting entries...")
        
        transformer = SalesTransformer(batch_id)
        accounting_records = []
        error_records = []
        
        with ThreadPoolExecutor(max_workers=args.max_workers) as executor:
            # 全レコードを並列処理
            futures = {
                executor.submit(process_record, record, transformer): record
                for record in source_records
            }
            
            # 結果を収集
            for future in as_completed(futures):
                status, result = future.result()
                if status == 'success':
                    accounting_records.append(result)
                else:
                    error_records.append(result)
        
        error_count = len(error_records)
        
        logger.info(f"Transformation completed: {len(accounting_records)} accounting entries generated")
        logger.info(f"Errors: {error_count}")
        
        # エラー詳細をログ出力
        if error_records:
            logger.error("Error details:")
            for err in error_records[:5]:  # 最初の5件のみ表示
                logger.error(f"  Record ID: {err['record'].get('export_id')}, Error: {err.get('error')}")
        
        # エラー閾値チェック
        if error_count >= args.error_threshold:
            logger.error(f"Error threshold ({args.error_threshold}) exceeded. Aborting.")
            raise Exception(f"Too many errors: {error_count}")
        
        # CSVへ書き込み
        if accounting_records:
            logger.info(f"Writing to {output_path}...")
            
            os.makedirs(args.output_dir, exist_ok=True)
            
            with open(output_path, 'w', newline='', encoding='utf-8') as csvfile:
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
        logger.error(f"Error: {e}")
        logger.error("=" * 60)
        raise


if __name__ == "__main__":
    main()

