# ETLオーケストレーター（ローカル実行用・Python標準版）

import sys
import os
import argparse
from datetime import datetime
import logging
import subprocess
from concurrent.futures import ThreadPoolExecutor, as_completed

# プロジェクトルートをパスに追加
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))

from src.common.csv_handler import cleanup_output_dir

# ロギング設定
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


# 各ETLジョブの設定（Python標準版）
ETL_JOBS = {
    'SALE': {
        'name': 'Sales ETL',
        'script': 'src/local/python_native/standalone_sales_etl_job.py',
        'input_dir': 'sales',
        'input_file': 'sales_txn_export.csv',
        'output_file': 'accounting_txn_interface_sales.csv'
    },
    'HR': {
        'name': 'HR ETL',
        'script': 'src/local/python_native/standalone_hr_etl_job.py',
        'input_dir': 'hr',
        'input_file': 'hr_employee_org_export.csv',
        'output_file': 'accounting_txn_interface_hr.csv'
    },
    'INV': {
        'name': 'Inventory ETL',
        'script': 'src/local/python_native/standalone_inventory_etl_job.py',
        'input_dir': 'inventory',
        'input_file': 'inv_movement_export.csv',
        'output_file': 'accounting_txn_interface_inventory.csv'
    }
}


# 単一のETLジョブを実行（Python標準版）
def run_etl_job(system_code: str, job_config: dict, input_base_dir: str, output_dir: str, 
                max_workers: int = 4) -> dict:
    logger.info(f"Starting ETL job for system: {system_code}")
    
    script_path = job_config['script']
    logger.info(f"Executing Python job: {script_path}")
    
    # 入力ディレクトリパスを構築
    input_dir = os.path.join(input_base_dir, job_config['input_dir'])
    
    try:
        # Python標準版ETLジョブを実行
        result = subprocess.run(
            [
                sys.executable, script_path,
                '--input-dir', input_dir,
                '--input-file', job_config['input_file'],
                '--output-dir', output_dir,
                '--output-file', job_config['output_file'],
                '--max-workers', str(max_workers)
            ],
            capture_output=True,
            text=True,
            encoding='utf-8',
            errors='replace'
        )
        
        if result.returncode == 0:
            logger.info(f"ETL job completed successfully for system: {system_code}")
            return {
                'system': system_code,
                'status': 'SUCCESS',
                'message': 'Job completed successfully'
            }
        else:
            logger.error(f"ETL job failed for system: {system_code}")
            logger.error(f"Error output: {result.stderr}")
            return {
                'system': system_code,
                'status': 'ERROR',
                'message': 'Job execution failed',
                'error': result.stderr
            }
    except Exception as e:
        logger.error(f"Exception occurred while running ETL job for {system_code}: {e}")
        return {
            'system': system_code,
            'status': 'ERROR',
            'message': str(e)
        }


# 複数のETLジョブを並列実行
def run_parallel(systems: list, input_base_dir: str, output_dir: str, max_workers: int) -> list:
    logger.info("Running ETL jobs in parallel...")
    results = []
    
    # Python標準版ジョブを並列実行
    with ThreadPoolExecutor(max_workers=len(systems)) as executor:
        futures = {
            executor.submit(run_etl_job, system, ETL_JOBS[system], input_base_dir, output_dir, max_workers): system
            for system in systems
        }
        
        for future in as_completed(futures):
            system = futures[future]
            try:
                result = future.result()
                results.append(result)
            except Exception as e:
                logger.error(f"Error in parallel execution for {system}: {e}")
                results.append({
                    'system': system,
                    'status': 'ERROR',
                    'message': str(e)
                })
    
    return results


# 複数のETLジョブを順次実行
def run_sequential(systems: list, input_base_dir: str, output_dir: str, max_workers: int) -> list:
    logger.info("Running ETL jobs sequentially...")
    results = []
    
    for system in systems:
        result = run_etl_job(system, ETL_JOBS[system], input_base_dir, output_dir, max_workers)
        results.append(result)
        
        # エラーが発生した場合は後続のジョブをスキップ
        if result['status'] == 'ERROR':
            logger.error(f"Stopping sequential execution due to error in {system}")
            break
    
    return results


# 個別出力ファイルを統合ファイルにマージ
def merge_output_files(output_dir: str, output_file: str, target_systems: list, keep_individual_files: bool):
    from src.common.csv_handler import read_csv, write_csv
    
    logger.info("Merging output files from all systems...")
    
    # システムごとの出力ファイル名マッピング
    system_output_files = {
        'SALE': 'accounting_txn_interface_sales.csv',
        'HR': 'accounting_txn_interface_hr.csv',
        'INV': 'accounting_txn_interface_inventory.csv'
    }
    
    all_records = []
    merged_count = 0
    
    for system in target_systems:
        individual_file = system_output_files.get(system)
        if not individual_file:
            logger.warning(f"Unknown system: {system}")
            continue
        
        individual_path = os.path.join(output_dir, individual_file)
        
        if not os.path.exists(individual_path):
            logger.warning(f"Output file not found: {individual_path}")
            continue
        
        try:
            records = read_csv(individual_path)
            all_records.extend(records)
            merged_count += len(records)
            logger.info(f"Merged {len(records)} records from {individual_file}")
            
            # 個別ファイルを削除（オプション）
            if not keep_individual_files:
                os.remove(individual_path)
                logger.debug(f"Deleted individual file: {individual_path}")
        
        except Exception as e:
            logger.error(f"Error reading {individual_path}: {e}")
    
    # 統合ファイルを書き込み
    if all_records:
        integrated_path = os.path.join(output_dir, output_file)
        write_csv(integrated_path, all_records)
        logger.info(f"Successfully created integrated file: {integrated_path}")
        logger.info(f"Total records in integrated file: {merged_count}")
    else:
        logger.warning("No records to merge")
    
    logger.info("=" * 60)


def main():
    parser = argparse.ArgumentParser(description='ETL Orchestrator (Local Python Native)')
    parser.add_argument('--execution_mode', type=str, default='parallel',
                        choices=['parallel', 'sequential'],
                        help='Execution mode: parallel or sequential')
    parser.add_argument('--systems', type=str, default='ALL',
                        help='Systems to run (comma-separated or ALL)')
    parser.add_argument('--input-base-dir', type=str, default='test_data',
                        help='Base input directory path')
    parser.add_argument('--output-dir', type=str, default='output',
                        help='Output directory path')
    parser.add_argument('--output-file', type=str, default='accounting_txn_interface.csv',
                        help='Integrated output CSV file name')
    parser.add_argument('--batch_date', type=str, default=datetime.now().strftime('%Y-%m-%d'),
                        help='Batch date (YYYY-MM-DD format)')
    parser.add_argument('--cleanup', action='store_true',
                        help='Clean up output directory before execution')
    parser.add_argument('--keep-individual-files', action='store_true',
                        help='Keep individual system output files (default: delete after merge)')
    parser.add_argument('--max-workers', type=int, default=4,
                        help='Maximum number of worker threads for parallel execution (default: 4)')
    
    args = parser.parse_args()
    
    # 実行対象システムを決定
    if args.systems == 'ALL':
        target_systems = list(ETL_JOBS.keys())
    else:
        target_systems = [s.strip() for s in args.systems.split(',')]
    
    try:
        logger.info("=" * 60)
        logger.info("ETL Orchestration Started (Python Native)")
        logger.info("=" * 60)
        logger.info(f"Execution Mode: {args.execution_mode}")
        logger.info(f"Target Systems: {', '.join(target_systems)}")
        logger.info(f"Input Base Dir: {args.input_base_dir}")
        logger.info(f"Output Dir: {args.output_dir}")
        logger.info(f"Integrated Output File: {args.output_file}")
        logger.info(f"Batch Date: {args.batch_date}")
        logger.info(f"Max Workers: {args.max_workers}")
        logger.info(f"Start Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        logger.info("=" * 60)
        
        start_time = datetime.now()
        
        # 出力ディレクトリのクリーンアップ
        if args.cleanup:
            logger.info("Cleaning up output directory...")
            cleanup_output_dir(args.output_dir)
        
        # ETLジョブ実行
        if args.execution_mode == 'parallel':
            results = run_parallel(target_systems, args.input_base_dir, args.output_dir, args.max_workers)
        else:
            results = run_sequential(target_systems, args.input_base_dir, args.output_dir, args.max_workers)
        
        # 個別ファイルを統合ファイルにマージ
        logger.info("=" * 60)
        logger.info("Merging individual output files into integrated file...")
        merge_output_files(args.output_dir, args.output_file, target_systems, args.keep_individual_files)
        
        end_time = datetime.now()
        duration = end_time - start_time
        
        # 結果サマリー
        logger.info("=" * 60)
        logger.info("ETL Orchestration Completed")
        logger.info("=" * 60)
        logger.info(f"End Time: {end_time.strftime('%Y-%m-%d %H:%M:%S')}")
        logger.info(f"Duration: {duration}")
        logger.info("")
        logger.info("Results Summary:")
        
        success_count = 0
        failed_count = 0
        
        for result in results:
            status_symbol = "✓" if result['status'] == 'SUCCESS' else "✗"
            logger.info(f"  {status_symbol} {result['system']}: {result['status']} - {result['message']}")
            if result['status'] == 'SUCCESS':
                success_count += 1
            else:
                failed_count += 1
        
        logger.info("")
        logger.info(f"Total Jobs: {len(results)}")
        logger.info(f"Success: {success_count}")
        logger.info(f"Failed: {failed_count}")
        logger.info("=" * 60)
        
        # エラーがあった場合は非ゼロで終了
        if failed_count > 0:
            sys.exit(1)
        
    except Exception as e:
        logger.error("=" * 60)
        logger.error("ETL Orchestration Failed")
        logger.error("=" * 60)
        logger.error(f"Error: {str(e)}")
        import traceback
        traceback.print_exc()
        logger.error("=" * 60)
        sys.exit(1)


if __name__ == "__main__":
    main()
