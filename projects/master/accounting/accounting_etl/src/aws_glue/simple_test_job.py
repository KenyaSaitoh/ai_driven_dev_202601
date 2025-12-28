# 簡単なテスト用ETLジョブ - S3のCSVデータを読み込んで変換

import sys
import logging
from datetime import datetime, timezone
import boto3
import csv
from io import StringIO

from awsglue.transforms import *
from awsglue.utils import getResolvedOptions
from pyspark.context import SparkContext
from awsglue.context import GlueContext
from awsglue.job import Job

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
        'output_file'
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
        input_prefix = args.get('input_prefix', 'input/sales')
        input_file = args.get('input_file', 'sales_txn_export.csv')
        output_prefix = args.get('output_prefix', 'output')
        output_file = args.get('output_file', 'test_output.csv')

        # S3パスの構築
        input_path = f"s3://{s3_bucket}/{input_prefix}/{input_file}"
        output_path = f"s3://{s3_bucket}/{output_prefix}/{output_file}"

        logger.info("=" * 60)
        logger.info("Simple Test ETL Job Started (AWS Glue S3 CSV)")
        logger.info("=" * 60)
        logger.info(f"Input Path: {input_path}")
        logger.info(f"Output Path: {output_path}")
        logger.info("=" * 60)

        # S3クライアントの初期化
        s3_client = boto3.client('s3')

        # CSVデータの読み込み
        logger.info(f"Reading data from {input_path}...")
        records = read_csv_from_s3(s3_client, s3_bucket, f"{input_prefix}/{input_file}")

        logger.info(f"Read {len(records)} records")

        # 簡単な変換: タイムスタンプを追加
        transformed_records = []
        for record in records:
            record['processing_timestamp'] = datetime.now(timezone.utc).isoformat()
            record['processing_date'] = datetime.now(timezone.utc).strftime('%Y-%m-%d')
            transformed_records.append(record)

        logger.info(f"Transformed {len(transformed_records)} records")

        # CSVで出力
        logger.info(f"Writing data to {output_path}...")
        write_csv_to_s3(s3_client, s3_bucket, f"{output_prefix}/{output_file}", transformed_records)

        logger.info("=" * 60)
        logger.info(f"Processed {len(transformed_records)} records successfully")
        logger.info("Simple Test ETL Job Completed Successfully")
        logger.info("=" * 60)

    except Exception as e:
        logger.error(f"ETL job failed: {e}")
        raise

    finally:
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

