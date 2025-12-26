# HR ETL Job - Python Native Version (ThreadPoolExecutor)
#
# このバージョンはPySparkを使用せず、Python標準ライブラリのみで動作します。
# PySparkがインストールされていない環境や、小規模データの処理に適しています。

import os
import sys
import csv
import logging
import argparse
from datetime import datetime
from decimal import Decimal
from concurrent.futures import ThreadPoolExecutor, as_completed

# プロジェクトルートをパスに追加
project_root = os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..'))
if project_root not in sys.path:
    sys.path.insert(0, project_root)

from src.etl.hr_transformer import HRTransformer

# ロギング設定
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


def process_record(hr_record, payroll_record, transformer):
    # 単一レコードを処理（従業員1人 -> 最大5個の会計仕訳）
    try:
        # 給与CSVデータを変換（Decimalに変換）
        bonus_value = payroll_record.get('bonus', '0')
        payroll_data = {
            'payroll_id': payroll_record.get('payroll_id', ''),
            'payroll_period': payroll_record.get('payroll_period', ''),
            'payment_date': payroll_record.get('payment_date', ''),
            'payroll_date': payroll_record.get('payment_date', ''),
            'basic_salary': Decimal(str(payroll_record.get('basic_salary', 0))),
            'allowances': {
                'HOUSING': Decimal(str(payroll_record.get('allowance_housing', 0))),
                'TRANSPORTATION': Decimal(str(payroll_record.get('allowance_transportation', 0)))
            },
            'deductions': {
                'TAX': Decimal(str(payroll_record.get('deduction_tax', 0))),
                'INSURANCE': Decimal(str(payroll_record.get('deduction_insurance', 0)))
            },
            'bonus': Decimal(str(bonus_value)) if bonus_value and bonus_value != '0' else None,
            'currency_code': payroll_record.get('currency_code', 'JPY'),
            'reversal_flag': payroll_record.get('reversal_flag', 'False') == 'True'
        }
        
        transformed_list = transformer.transform_payroll_record(hr_record, payroll_data)
        return ('success', transformed_list if transformed_list else [])
    except Exception as e:
        return ('error', {'record': hr_record, 'error': str(e)})


def main():
    parser = argparse.ArgumentParser(description='HR ETL Job (Python Native)')
    parser.add_argument('--input-dir', type=str, default='test_data/hr',
                        help='Input directory path')
    parser.add_argument('--input-file', type=str, default='hr_employee_org_export.csv',
                        help='Input CSV filename for employee data (relative to input-dir)')
    parser.add_argument('--payroll-file', type=str, default='hr_payroll_export.csv',
                        help='Input CSV filename for payroll data (relative to input-dir)')
    parser.add_argument('--output-dir', type=str, default='output',
                        help='Output directory path')
    parser.add_argument('--output-file', type=str, default='accounting_txn_interface_hr.csv',
                        help='Output CSV filename (relative to output-dir)')
    parser.add_argument('--payroll-period', type=str, default=datetime.now().strftime('%Y-%m'),
                        help='Payroll period (YYYY-MM format)')
    parser.add_argument('--limit', type=int, default=None,
                        help='Limit number of records to process')
    parser.add_argument('--error-threshold', type=int, default=50,
                        help='Maximum number of errors before aborting')
    parser.add_argument('--max-workers', type=int, default=4,
                        help='Maximum number of parallel workers')
    
    args = parser.parse_args()
    
    try:
        # パス設定
        employee_path = os.path.join(args.input_dir, args.input_file)
        payroll_path = os.path.join(args.input_dir, args.payroll_file)
        output_path = os.path.join(args.output_dir, args.output_file)
        
        # バッチIDの生成
        batch_id = f"HR_{datetime.now().strftime('%Y%m%d_%H%M%S')}"
        
        logger.info("=" * 60)
        logger.info("HR ETL Job Started (Python Native - ThreadPoolExecutor)")
        logger.info("=" * 60)
        logger.info(f"Batch ID: {batch_id}")
        logger.info(f"Employee File: {employee_path}")
        logger.info(f"Payroll File: {payroll_path}")
        logger.info(f"Output File: {output_path}")
        logger.info(f"Payroll Period: {args.payroll_period}")
        logger.info(f"Limit Records: {args.limit if args.limit else 'None'}")
        logger.info(f"Error Threshold: {args.error_threshold}")
        logger.info(f"Max Workers: {args.max_workers}")
        logger.info("=" * 60)
        
        # 従業員データ読み込み
        logger.info(f"Reading employee data from {employee_path}...")
        employee_records = {}
        
        with open(employee_path, 'r', encoding='utf-8') as csvfile:
            reader = csv.DictReader(csvfile)
            for record in reader:
                employee_id = record.get('employee_id')
                if employee_id:
                    employee_records[employee_id] = record
        
        logger.info(f"Loaded {len(employee_records)} employee records")
        
        # 給与データ読み込み
        logger.info(f"Reading payroll data from {payroll_path}...")
        payroll_records = []
        
        with open(payroll_path, 'r', encoding='utf-8') as csvfile:
            reader = csv.DictReader(csvfile)
            for i, record in enumerate(reader):
                if args.limit and i >= args.limit:
                    break
                payroll_records.append(record)
        
        logger.info(f"Loaded {len(payroll_records)} payroll records")
        
        # データ変換（ThreadPoolExecutorで並列処理）
        logger.info("Transforming HR data to accounting entries...")
        
        transformer = HRTransformer(batch_id)
        accounting_records = []
        error_records = []
        
        # 給与データと従業員データをJOIN
        joined_records = []
        for payroll_record in payroll_records:
            employee_id = payroll_record.get('employee_id')
            if employee_id in employee_records:
                joined_records.append({
                    'hr_record': employee_records[employee_id],
                    'payroll_record': payroll_record
                })
            else:
                error_records.append({
                    'record': payroll_record,
                    'error': f"Employee ID {employee_id} not found in employee master"
                })
        
        logger.info(f"Joined {len(joined_records)} records (HR + Payroll)")
        
        with ThreadPoolExecutor(max_workers=args.max_workers) as executor:
            # 全レコードを並列処理
            futures = {
                executor.submit(
                    process_record,
                    joined['hr_record'],
                    joined['payroll_record'],
                    transformer
                ): joined
                for joined in joined_records
            }
            
            # 結果を収集
            for future in as_completed(futures):
                status, result = future.result()
                if status == 'success':
                    # HRは1レコード -> 複数レコード（最大5個）に変換
                    accounting_records.extend(result)
                else:
                    error_records.append(result)
        
        error_count = len(error_records)
        
        logger.info(f"Transformation completed: {len(accounting_records)} accounting entries generated")
        logger.info(f"Errors: {error_count}")
        
        # エラー詳細をログ出力
        if error_records:
            logger.error("Error details:")
            for err in error_records[:5]:  # 最初の5件のみ表示
                logger.error(f"  Employee ID: {err['record'].get('employee_id')}, Error: {err.get('error')}")
        
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
        logger.info("HR ETL Job Completed Successfully")
        logger.info("=" * 60)
        
    except Exception as e:
        logger.error("=" * 60)
        logger.error("HR ETL Job Failed")
        logger.error("=" * 60)
        logger.error(f"Error: {e}")
        logger.error("=" * 60)
        raise


if __name__ == "__main__":
    main()

