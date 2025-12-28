#!/bin/bash
# PySpark ETL実行スクリプト（Windows埋め込み版Python 3.11専用）
# Usage: ./run_pyspark_etl_win_embed.sh [sales|hr|inventory|all] [--limit N]
#
# 前提条件:
#   - 環境変数 PYTHON311_PATH が設定されていること
#   - 例: export PYTHON311_PATH="/d/Python/python-3.11.7-embed-amd64/python.exe"

# カラー出力用
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# デフォルト値
JOB="all"
LIMIT=0

# パラメータ解析
while [[ $# -gt 0 ]]; do
    case $1 in
        sales|hr|inventory|all)
            JOB="$1"
            shift
            ;;
        --limit)
            LIMIT="$2"
            shift 2
            ;;
        *)
            echo -e "${RED}Unknown parameter: $1${NC}"
            echo "Usage: ./run_pyspark_etl_win_embed.sh [sales|hr|inventory|all] [--limit N]"
            exit 1
            ;;
    esac
done

# スクリプトのディレクトリに移動
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Python311_PATH環境変数のチェック（必須）
if [ -z "$PYTHON311_PATH" ]; then
    echo -e "${RED}ERROR: PYTHON311_PATH environment variable is not set!${NC}"
    echo ""
    echo -e "${YELLOW}Please set PYTHON311_PATH to your embedded Python installation:${NC}"
    echo -e "${CYAN}  export PYTHON311_PATH=\"/d/Python/python-3.11.7-embed-amd64/python.exe\"${NC}"
    echo ""
    echo -e "${YELLOW}For more details, see:${NC} README_WINDOWS_PYSPARK.md"
    exit 1
fi

# Pythonの存在確認
if [ ! -f "$PYTHON311_PATH" ]; then
    echo -e "${RED}ERROR: Python executable not found at:${NC} $PYTHON311_PATH"
    echo ""
    echo -e "${YELLOW}Please check your PYTHON311_PATH setting.${NC}"
    exit 1
fi

PYTHON311="$PYTHON311_PATH"

# PySpark環境変数を設定（埋め込み版Python使用）
export PYSPARK_PYTHON="$PYTHON311"
export PYSPARK_DRIVER_PYTHON="$PYTHON311"

# PYTHONPATHにプロジェクトルートを追加（ワーカープロセスでsrcモジュールを認識させる）
export PYTHONPATH="$SCRIPT_DIR:$PYTHONPATH"

echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}PySpark ETL Job Runner${NC}"
echo -e "${CYAN}(Windows Embedded Python 3.11)${NC}"
echo -e "${CYAN}========================================${NC}"
echo -e "${YELLOW}Python: $PYTHON311${NC}"
echo ""

# Pythonバージョン確認
PYTHON_VERSION=$("$PYTHON311" --version 2>&1)
echo -e "${CYAN}Python Version: ${PYTHON_VERSION}${NC}"
echo ""

# ジョブ実行関数
run_etl_job() {
    local job_name="$1"
    local script_path="$2"
    shift 2
    local args=("$@")
    
    echo -e "${GREEN}[$(date '+%H:%M:%S')] Starting $job_name...${NC}"
    
    if [ "$LIMIT" -gt 0 ]; then
        args+=("--limit" "$LIMIT")
    fi
    
    "$PYTHON311" "$script_path" "${args[@]}"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}[$(date '+%H:%M:%S')] $job_name completed successfully!${NC}"
    else
        echo -e "${RED}[$(date '+%H:%M:%S')] $job_name failed with exit code $?${NC}"
        exit $?
    fi
    echo ""
}

# ジョブ実行（小文字に変換して比較）
JOB_LOWER=$(echo "$JOB" | tr '[:upper:]' '[:lower:]')
case "$JOB_LOWER" in
    sales)
        run_etl_job "Sales ETL" "src/local/pyspark/pyspark_sales_etl_job.py"
        ;;
    hr)
        run_etl_job "HR ETL" "src/local/pyspark/pyspark_hr_etl_job.py"
        ;;
    inventory)
        run_etl_job "Inventory ETL" "src/local/pyspark/pyspark_inventory_etl_job.py"
        ;;
    all)
        run_etl_job "Sales ETL" "src/local/pyspark/pyspark_sales_etl_job.py"
        run_etl_job "HR ETL" "src/local/pyspark/pyspark_hr_etl_job.py"
        run_etl_job "Inventory ETL" "src/local/pyspark/pyspark_inventory_etl_job.py"
        
        # 個別ファイルを統合
        echo ""
        echo -e "${GREEN}[$(date '+%H:%M:%S')] Merging output files...${NC}"
        "$PYTHON311" merge_etl_outputs.py
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}[$(date '+%H:%M:%S')] Merge completed successfully!${NC}"
        else
            echo -e "${RED}[$(date '+%H:%M:%S')] Merge failed${NC}"
            exit $?
        fi
        ;;
    *)
        echo -e "${RED}Invalid job name: $JOB${NC}"
        echo -e "${YELLOW}Valid options: sales, hr, inventory, all${NC}"
        exit 1
        ;;
esac

echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}All jobs completed!${NC}"
echo -e "${CYAN}========================================${NC}"


