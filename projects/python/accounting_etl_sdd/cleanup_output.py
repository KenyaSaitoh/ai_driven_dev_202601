# 出力フォルダクリーンアップスクリプト
# 実行: python cleanup_output.py

import sys
import os
import argparse
import logging

# プロジェクトルートをパスに追加
sys.path.insert(0, os.path.abspath(os.path.dirname(__file__)))

# ロギング設定
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


def cleanup_output_dir(output_dir: str, force: bool = False):
    """
    出力ディレクトリをクリーンアップ
    
    Args:
        output_dir: クリーンアップ対象ディレクトリ
        force: 確認をスキップして強制実行
    """
    try:
        if not os.path.exists(output_dir):
            logger.info(f"Output directory does not exist: {output_dir}")
            os.makedirs(output_dir, exist_ok=True)
            logger.info(f"Created output directory: {output_dir}")
            return
        
        # 削除対象ファイルを取得
        files_to_delete = []
        for filename in os.listdir(output_dir):
            file_path = os.path.join(output_dir, filename)
            if os.path.isfile(file_path) and filename != '.gitkeep':
                files_to_delete.append(file_path)
        
        if not files_to_delete:
            logger.info("No files to delete in output directory")
            return
        
        # ファイル情報を表示
        logger.info("=" * 60)
        logger.info(f"Found {len(files_to_delete)} file(s) to delete:")
        total_size = 0
        for file_path in files_to_delete:
            size = os.path.getsize(file_path)
            total_size += size
            logger.info(f"  - {os.path.basename(file_path)} ({size:,} bytes)")
        logger.info(f"Total size: {total_size:,} bytes ({total_size / 1024:.2f} KB)")
        logger.info("=" * 60)
        
        # 確認
        if not force:
            response = input("Delete these files? [y/N]: ")
            if response.lower() != 'y':
                logger.info("Cleanup cancelled by user")
                return
        
        # ファイル削除
        deleted_count = 0
        for file_path in files_to_delete:
            try:
                os.remove(file_path)
                logger.debug(f"Deleted: {file_path}")
                deleted_count += 1
            except Exception as e:
                logger.error(f"Failed to delete {file_path}: {e}")
        
        logger.info("=" * 60)
        logger.info(f"Cleanup completed: {deleted_count}/{len(files_to_delete)} files deleted")
        logger.info("=" * 60)
        
    except Exception as e:
        logger.error(f"Error during cleanup: {e}")
        raise


def main():
    parser = argparse.ArgumentParser(
        description='Clean up output directory',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python cleanup_output.py              # Interactive mode (with confirmation)
  python cleanup_output.py --force      # Force mode (no confirmation)
  python cleanup_output.py --dir custom_output  # Custom directory
"""
    )
    parser.add_argument('--dir', type=str, default='output',
                        help='Output directory to clean up (default: output)')
    parser.add_argument('--force', '-f', action='store_true',
                        help='Force cleanup without confirmation')
    parser.add_argument('--verbose', '-v', action='store_true',
                        help='Verbose output')
    
    args = parser.parse_args()
    
    # ログレベル調整
    if args.verbose:
        logging.getLogger().setLevel(logging.DEBUG)
    
    try:
        logger.info("Starting output directory cleanup...")
        cleanup_output_dir(args.dir, force=args.force)
        logger.info("Cleanup process completed successfully")
    except Exception as e:
        logger.error(f"Cleanup failed: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
