# メインETL処理 - 在庫システム
# AWS Glueジョブとして実行される（S3 CSV入出力版）

import sys
import logging
from datetime import datetime, timezone
from decimal import Decimal
import boto3
import csv
from io import StringIO

from awsglue.transforms import *
from awsglue.utils import getResolvedOptions
from pyspark.context import SparkContext
from awsglue.context import GlueContext
from awsglue.job import Job

# 共通モジュールのインポート
from src.common.utils import ETLUtils
from src.etl.inventory_transformer import InventoryTransformer

# ログ設定
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


# メイン処理
def main():
    
    # Glueジョブパラメータの取得
    args = getResolvedOptions(sys.argv, [
        'JOB_NAME',
        's3_bucket',
        'input_prefix',
        'input_file',
        'output_prefix',
        'output_file',
        'batch_id',
        'movement_types',
        'limit_records',
        'error_threshold'
    ])

    # Spark/Glueコンテキストの初期化
    sc = SparkContext()
    glueContext = GlueContext(sc)
    spark = glueContext.spark_session
    job = Job(glueContext)
    job.init(args['JOB_NAME'], args)

    try:
        # パラメータの取得
        s3_bucket = args.get('s3_bucket')
        input_prefix = args.get('input_prefix', 'input/inventory')
        input_file = args.get('input_file', 'inv_movement_export.csv')
        output_prefix = args.get('output_prefix', 'output')
        output_file = args.get('output_file', 'accounting_txn_interface_inventory.csv')
        batch_id = args.get('batch_id')
        movement_types = args.get('movement_types', 'ALL')
        limit_records = int(args.get('limit_records', 0)) if args.get('limit_records') else None
        error_threshold = int(args.get('error_threshold', 100))

        # バッチIDが指定されていない場合は生成
        if not batch_id:
            batch_id = f"INV_{datetime.now().strftime('%Y%m%d_%H%M%S')}"

        # S3パスの構築
        input_path = f"s3://{s3_bucket}/{input_prefix}/{input_file}"
        output_path = f"s3://{s3_bucket}/{output_prefix}/{output_file}"

        logger.info("=" * 60)
        logger.info("Inventory ETL Job Started (AWS Glue S3 CSV Execution)")
        logger.info("=" * 60)
        logger.info(f"Batch ID: {batch_id}")
        logger.info(f"Input Path: {input_path}")
        logger.info(f"Output Path: {output_path}")
        logger.info(f"Movement Types: {movement_types}")
        logger.info(f"Limit Records: {limit_records if limit_records else 'No limit'}")
        logger.info(f"Error Threshold: {error_threshold}")
        logger.info("=" * 60)

        # S3クライアントの初期化
        s3_client = boto3.client('s3')

        # CSVデータの読み込み
        logger.info(f"Reading source data from {input_path}...")
        source_data = read_csv_from_s3(s3_client, s3_bucket, f"{input_prefix}/{input_file}")
        
        # 移動タイプでフィルタリング
        if movement_types != 'ALL':
            movement_type_list = [mt.strip() for mt in movement_types.split(',')]
            source_data = [r for r in source_data if r.get('movement_type') in movement_type_list]
        
        if limit_records:
            source_data = source_data[:limit_records]
        
        if not source_data:
            logger.warning("No source data found")
            return

        logger.info(f"Fetched {len(source_data)} records from source")

        # 変換処理の初期化
        transformer = InventoryTransformer(batch_id)

        # データ変換
        logger.info("Transforming inventory data to accounting entries...")
        accounting_records = []
        error_count = 0

        for source_record in source_data:
            try:
                # レコードの変換
                target_record = transformer.transform_record(source_record)
                
                # datetime オブジェクトを文字列に変換
                for key, value in target_record.items():
                    if isinstance(value, datetime):
                        target_record[key] = value.strftime('%Y-%m-%d %H:%M:%S')
                    elif isinstance(value, Decimal):
                        target_record[key] = str(value)
                
                accounting_records.append(target_record)
                
            except Exception as e:
                error_count += 1
                logger.error(f"Error transforming record: {e}")
                if error_count > error_threshold:
                    raise

        logger.info(f"Transformation completed: {len(accounting_records)} accounting entries generated")
        logger.info(f"Errors: {error_count}")

        # CSVへ書き込み
        if accounting_records:
            logger.info(f"Writing to {output_path}...")
            write_csv_to_s3(s3_client, s3_bucket, f"{output_prefix}/{output_file}", accounting_records)
            logger.info(f"Successfully wrote {len(accounting_records)} records")

        logger.info("=" * 60)
        logger.info("Inventory ETL Job Completed Successfully")
        logger.info("=" * 60)

    except Exception as e:
        logger.error(f"Inventory ETL job failed: {e}")
        raise

    finally:
        # リソースのクリーンアップ
        job.commit()


# S3からCSVを読み込む
def read_csv_from_s3(s3_client, bucket, key):
    try:
        response = s3_client.get_object(Bucket=bucket, Key=key)
        content = response['Body'].read().decode('utf-8')
        
        records = []
        csv_reader = csv.DictReader(StringIO(content))
        for row in csv_reader:
            records.append(row)
        
        logger.info(f"Successfully read {len(records)} records from s3://{bucket}/{key}")
        return records
    except Exception as e:
        logger.error(f"Error reading CSV from S3: {e}")
        raise


# S3にCSVを書き込む
def write_csv_to_s3(s3_client, bucket, key, records):
    if not records:
        logger.warning(f"No records to write to s3://{bucket}/{key}")
        return
    
    try:
        output = StringIO()
        fieldnames = list(records[0].keys())
        writer = csv.DictWriter(output, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(records)
        
        s3_client.put_object(
            Bucket=bucket,
            Key=key,
            Body=output.getvalue().encode('utf-8')
        )
        
        logger.info(f"Successfully wrote {len(records)} records to s3://{bucket}/{key}")
    except Exception as e:
        logger.error(f"Error writing CSV to S3: {e}")
        raise


if __name__ == "__main__":
    main()
