# ETL出力ファイルを統合するスクリプト
#
# 個別のETLジョブ（Sales, HR, Inventory）の出力を1つのファイルに統合します。

import os
import sys
import csv
import argparse
import logging

# ロギング設定
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


def merge_csv_files(output_dir: str, output_file: str, keep_individual_files: bool = False):
    # 個別のETL出力ファイルを1つに統合
    #
    # Args:
    #   output_dir: 出力ディレクトリ
    #   output_file: 統合後のファイル名
    #   keep_individual_files: 個別ファイルを残すか
    
    # システムごとの出力ファイル名
    individual_files = [
        'accounting_txn_interface_sales.csv',
        'accounting_txn_interface_hr.csv',
        'accounting_txn_interface_inventory.csv'
    ]
    
    logger.info("=" * 60)
    logger.info("Merging output files from all systems...")
    logger.info("=" * 60)
    
    all_records = []
    merged_count = 0
    header = None
    
    # 各ファイルを読み込み
    for individual_file in individual_files:
        individual_path = os.path.join(output_dir, individual_file)
        
        if not os.path.exists(individual_path):
            logger.warning(f"File not found (skipping): {individual_file}")
            continue
        
        try:
            with open(individual_path, 'r', encoding='utf-8') as f:
                reader = csv.DictReader(f)
                
                # ヘッダーを保存（最初のファイルから）
                if header is None:
                    header = reader.fieldnames
                
                records = list(reader)
                all_records.extend(records)
                record_count = len(records)
                merged_count += record_count
                
                logger.info(f"✓ Merged {record_count:,} records from {individual_file}")
        
        except Exception as e:
            logger.error(f"✗ Error reading {individual_file}: {e}")
            continue
    
    if not all_records:
        logger.error("No records to merge!")
        return False
    
    # 統合ファイルを書き込み
    output_path = os.path.join(output_dir, output_file)
    
    try:
        with open(output_path, 'w', newline='', encoding='utf-8') as f:
            writer = csv.DictWriter(f, fieldnames=header)
            writer.writeheader()
            writer.writerows(all_records)
        
        logger.info("=" * 60)
        logger.info(f"✓ Successfully created integrated file: {output_file}")
        logger.info(f"  Total records: {merged_count:,}")
        logger.info(f"  Output path: {output_path}")
        logger.info("=" * 60)
    
    except Exception as e:
        logger.error(f"Error writing integrated file: {e}")
        return False
    
    # 個別ファイルの削除（オプション）
    if not keep_individual_files:
        logger.info("Cleaning up individual files...")
        for individual_file in individual_files:
            individual_path = os.path.join(output_dir, individual_file)
            if os.path.exists(individual_path):
                try:
                    os.remove(individual_path)
                    logger.info(f"  ✓ Removed {individual_file}")
                except Exception as e:
                    logger.warning(f"  ✗ Could not remove {individual_file}: {e}")
    
    return True


def main():
    parser = argparse.ArgumentParser(description='Merge ETL output files')
    parser.add_argument('--output-dir', type=str, default='output',
                        help='Output directory (default: output)')
    parser.add_argument('--output-file', type=str, default='accounting_txn_interface.csv',
                        help='Integrated output filename (default: accounting_txn_interface.csv)')
    parser.add_argument('--keep-individual-files', action='store_true',
                        help='Keep individual system output files')
    
    args = parser.parse_args()
    
    success = merge_csv_files(
        output_dir=args.output_dir,
        output_file=args.output_file,
        keep_individual_files=args.keep_individual_files
    )
    
    if success:
        logger.info("Merge completed successfully!")
        sys.exit(0)
    else:
        logger.error("Merge failed!")
        sys.exit(1)


if __name__ == "__main__":
    main()

