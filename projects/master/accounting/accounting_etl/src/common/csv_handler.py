# CSV入出力ハンドラー

import csv
import os
from typing import List, Dict, Any
import logging

logger = logging.getLogger(__name__)


# CSVファイルを読み込んで辞書のリストとして返す
def read_csv(file_path: str) -> List[Dict[str, Any]]:
    records = []
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            for row in reader:
                records.append(row)
        logger.info(f"Successfully read {len(records)} records from {file_path}")
        return records
    except Exception as e:
        logger.error(f"Error reading CSV file {file_path}: {e}")
        raise


# 辞書のリストをCSVファイルに書き込む
def write_csv(file_path: str, records: List[Dict[str, Any]], fieldnames: List[str] = None):
    try:
        # 出力ディレクトリが存在しない場合は作成
        os.makedirs(os.path.dirname(file_path), exist_ok=True)
        
        if not records:
            logger.warning(f"No records to write to {file_path}")
            return
        
        # フィールド名が指定されていない場合は最初のレコードから取得
        if fieldnames is None:
            fieldnames = list(records[0].keys())
        
        with open(file_path, 'w', encoding='utf-8', newline='') as f:
            writer = csv.DictWriter(f, fieldnames=fieldnames)
            writer.writeheader()
            writer.writerows(records)
        
        logger.info(f"Successfully wrote {len(records)} records to {file_path}")
    except Exception as e:
        logger.error(f"Error writing CSV file {file_path}: {e}")
        raise


# 出力ディレクトリをクリーンアップする
def cleanup_output_dir(output_dir: str):
    try:
        if os.path.exists(output_dir):
            for filename in os.listdir(output_dir):
                file_path = os.path.join(output_dir, filename)
                if os.path.isfile(file_path):
                    os.remove(file_path)
                    logger.debug(f"Removed file: {file_path}")
            logger.info(f"Cleaned up output directory: {output_dir}")
        else:
            os.makedirs(output_dir, exist_ok=True)
            logger.info(f"Created output directory: {output_dir}")
    except Exception as e:
        logger.error(f"Error cleaning up output directory {output_dir}: {e}")
        raise

