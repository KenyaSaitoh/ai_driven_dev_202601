#!/bin/bash
# PySpark ETL実行スクリプト（Python 3.11環境）
# Usage: ./run_pyspark_etl.sh [sales|hr|inventory|all] [--limit N]

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
            echo "Usage: ./run_pyspark_etl.sh [sales|hr|inventory|all] [--limit N]"
            exit 1
            ;;
    esac
done

# スクリプトのディレクトリに移動
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Python実行ファイルパスの決定
# 環境変数 PYTHON311_PATH が設定されていればそれを使用、なければシステムPythonを使用
if [ -n "$PYTHON311_PATH" ]; then
    PYTHON311="$PYTHON311_PATH"
else
    # プラットフォーム検出
    OS_TYPE=$(uname -s)
    
    if [[ "$OS_TYPE" == "MINGW"* ]] || [[ "$OS_TYPE" == "MSYS"* ]] || [[ "$OS_TYPE" == "CYGWIN"* ]]; then
        # Windows (Git Bash): システムPythonを使用
        PYTHON311="python"
    elif [[ "$OS_TYPE" == "Darwin" ]]; then
        # macOS: システムPython 3.11を使用
        PYTHON311="python3.11"
    else
        # Linux: システムPython 3.11を使用
        PYTHON311="python3.11"
    fi
fi

# PySpark環境変数を設定
export PYSPARK_PYTHON="$PYTHON311"
export PYSPARK_DRIVER_PYTHON="$PYTHON311"

echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}PySpark ETL Job Runner (Python 3.11)${NC}"
echo -e "${CYAN}========================================${NC}"
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
        run_etl_job "HR ETL" "src/local/pyspark/pyspark_hr_etl_job.py" "--payroll-period" "2025-08"
        ;;
    inventory)
        run_etl_job "Inventory ETL" "src/local/pyspark/pyspark_inventory_etl_job.py"
        ;;
    all)
        run_etl_job "Sales ETL" "src/local/pyspark/pyspark_sales_etl_job.py"
        run_etl_job "HR ETL" "src/local/pyspark/pyspark_hr_etl_job.py" "--payroll-period" "2025-08"
        run_etl_job "Inventory ETL" "src/local/pyspark/pyspark_inventory_etl_job.py"
        
        # 個別ファイルを統合
        echo ""
        echo -e "${GREEN}[$(date '+%H:%M:%S')] Merging output files...${NC}"
        python merge_etl_outputs.py
        
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

