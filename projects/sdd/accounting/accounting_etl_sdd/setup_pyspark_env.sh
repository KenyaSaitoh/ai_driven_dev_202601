#!/bin/bash
# PySpark環境自動セットアップスクリプト
# Usage: ./setup_pyspark_env.sh [--skip-python] [--skip-packages]

set -e  # エラーで停止

# カラー出力用
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# パラメータ解析
SKIP_PYTHON=false
SKIP_PACKAGES=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-python)
            SKIP_PYTHON=true
            shift
            ;;
        --skip-packages)
            SKIP_PACKAGES=true
            shift
            ;;
        *)
            echo -e "${RED}Unknown parameter: $1${NC}"
            exit 1
            ;;
    esac
done

echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}PySpark Environment Setup${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""

# プラットフォーム検出
OS_TYPE=$(uname -s)
echo "Detected OS: $OS_TYPE"

# プロジェクトルートを取得
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"

# プラットフォーム別のPython設定
if [[ "$OS_TYPE" == "MINGW"* ]] || [[ "$OS_TYPE" == "MSYS"* ]] || [[ "$OS_TYPE" == "CYGWIN"* ]]; then
    # Windows (Git Bash, MSYS, Cygwin)
    PLATFORM="Windows"
    PYTHON311_DIR="$PROJECT_ROOT/python-3.11.7-embed-amd64"
    PYTHON311_EXE="$PYTHON311_DIR/python.exe"
elif [[ "$OS_TYPE" == "Darwin" ]]; then
    # macOS
    PLATFORM="macOS"
    # macOSではシステムにインストール済みのPython 3.11を使用
    PYTHON311_EXE="python3.11"
else
    # Linux
    PLATFORM="Linux"
    PYTHON311_EXE="python3.11"
fi

echo "Platform: $PLATFORM"
echo ""

# Step 1: Python 3.11のチェック/セットアップ
if [ "$SKIP_PYTHON" = false ]; then
    echo -e "${GREEN}[Step 1/3] Checking Python 3.11...${NC}"
    
    if [[ "$PLATFORM" == "Windows" ]]; then
        # Windows: 埋め込み版Pythonのセットアップ
        if [ -f "$PYTHON311_EXE" ]; then
            echo -e "${YELLOW}  ✓ Python 3.11 already exists at: $PYTHON311_DIR${NC}"
            "$PYTHON311_EXE" --version
        else
            echo -e "${YELLOW}  Downloading Python 3.11.7 (embed)...${NC}"
            
            PYTHON_URL="https://www.python.org/ftp/python/3.11.7/python-3.11.7-embed-amd64.zip"
            PYTHON_ZIP="/tmp/python-3.11.7-embed-amd64.zip"
            
            # ダウンロード
            curl -L -o "$PYTHON_ZIP" "$PYTHON_URL"
            
            # 解凍
            echo -e "${YELLOW}  Extracting to: $PYTHON311_DIR${NC}"
            mkdir -p "$PYTHON311_DIR"
            unzip -q "$PYTHON_ZIP" -d "$PYTHON311_DIR"
            
            # python311._pthを編集してsiteモジュールを有効化
            PTH_FILE="$PYTHON311_DIR/python311._pth"
            sed -i 's/^#import site/import site/' "$PTH_FILE" 2>/dev/null || sed -i '' 's/^#import site/import site/' "$PTH_FILE"
            
            # 一時ファイル削除
            rm -f "$PYTHON_ZIP"
            
            echo -e "${GREEN}  ✓ Python 3.11.7 installed successfully!${NC}"
        fi
    else
        # macOS/Linux: システムにインストール済みのPythonを確認
        if command -v python3.11 &> /dev/null; then
            PYTHON_VERSION=$(python3.11 --version 2>&1)
            echo -e "${YELLOW}  ✓ Python 3.11 found: $PYTHON_VERSION${NC}"
        else
            echo -e "${RED}  ✗ Python 3.11 not found${NC}"
            echo ""
            if [[ "$PLATFORM" == "macOS" ]]; then
                echo "Please install Python 3.11 using Homebrew:"
                echo "  brew install python@3.11"
            else
                echo "Please install Python 3.11 using your package manager:"
                echo "  sudo apt-get install python3.11  # Ubuntu/Debian"
                echo "  sudo yum install python3.11      # RHEL/CentOS"
            fi
            exit 1
        fi
    fi
    echo ""
fi

# Step 2: pipのセットアップ
if [ "$SKIP_PACKAGES" = false ]; then
    echo -e "${GREEN}[Step 2/3] Setting up pip...${NC}"
    
    # pipがインストール済みかチェック
    if "$PYTHON311_EXE" -m pip --version &> /dev/null; then
        echo -e "${YELLOW}  ✓ pip already installed${NC}"
    else
        echo -e "${YELLOW}  Installing pip...${NC}"
        
        GET_PIP_URL="https://bootstrap.pypa.io/get-pip.py"
        GET_PIP_FILE="/tmp/get-pip.py"
        
        curl -L -o "$GET_PIP_FILE" "$GET_PIP_URL"
        "$PYTHON311_EXE" "$GET_PIP_FILE"
        rm -f "$GET_PIP_FILE"
        
        echo -e "${GREEN}  ✓ pip installed successfully!${NC}"
    fi
    echo ""
fi

# Step 3: PySpark + boto3のインストール
if [ "$SKIP_PACKAGES" = false ]; then
    echo -e "${GREEN}[Step 3/3] Installing PySpark and dependencies...${NC}"
    
    cd "$SCRIPT_DIR"
    
    echo -e "${YELLOW}  Installing packages from requirements.txt...${NC}"
    "$PYTHON311_EXE" -m pip install -r requirements.txt --quiet
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}  ✓ PySpark 3.5.3 installed successfully!${NC}"
        echo -e "${GREEN}  ✓ boto3 installed successfully!${NC}"
    else
        echo -e "${RED}  ✗ Package installation failed${NC}"
        exit 1
    fi
    
    echo ""
fi

# 環境確認
echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}Environment Verification${NC}"
echo -e "${CYAN}========================================${NC}"

echo ""
echo -e "${YELLOW}Python:${NC}"
"$PYTHON311_EXE" --version

echo ""
echo -e "${YELLOW}PySpark:${NC}"
"$PYTHON311_EXE" -c "import pyspark; print(f'PySpark {pyspark.__version__}')"

echo ""
echo -e "${YELLOW}Java:${NC}"
java -version 2>&1 | grep -i "version" || echo "Java not found - required for PySpark!"

echo ""
echo -e "${CYAN}========================================${NC}"
echo -e "${GREEN}Setup Complete!${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""
echo -e "${CYAN}To run ETL jobs, use:${NC}"
echo -e "  ./run_pyspark_etl.sh sales --limit 5"
echo ""
if [[ "$PLATFORM" == "Windows" ]]; then
    echo -e "${CYAN}Python 3.11 location:${NC}"
    echo -e "  $PYTHON311_DIR"
else
    echo -e "${CYAN}Python 3.11 command:${NC}"
    echo -e "  python3.11"
fi
echo ""

