#!/bin/bash

###############################################################################
# Bookstore Full Stack Application Stopper
# 
# このスクリプトは以下の処理を自動実行します：
# 1. React SPA（3つ）を停止
# 2. Jakarta EE API（3つ）をアンデプロイ
# 3. Payara Serverを停止
# 4. HSQLDBサーバーを停止
###############################################################################

set -e  # エラーが発生したら即座に終了

# 色の定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# スクリプトのルートディレクトリ（プロジェクトルート）
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../" && pwd)"
cd "$PROJECT_ROOT"

# ログファイル
LOG_DIR="$PROJECT_ROOT/logs"
mkdir -p "$LOG_DIR"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
LOG_FILE="$LOG_DIR/stop-bookstore-all_${TIMESTAMP}.log"

# ログ出力関数
log() {
    local message="[$(date +'%Y-%m-%d %H:%M:%S')] $1"
    echo -e "${GREEN}${message}${NC}"
    echo "$message" >> "$LOG_FILE"
}

log_error() {
    local message="[ERROR] $1"
    echo -e "${RED}${message}${NC}"
    echo "$message" >> "$LOG_FILE"
}

log_warn() {
    local message="[WARNING] $1"
    echo -e "${YELLOW}${message}${NC}"
    echo "$message" >> "$LOG_FILE"
}

log_info() {
    local message="[INFO] $1"
    echo -e "${BLUE}${message}${NC}"
    echo "$message" >> "$LOG_FILE"
}

# エラーハンドラー（継続実行）
error_continue() {
    log_warn "$1"
}

echo ""
log "=============================================="
log "Bookstore アプリケーション停止スクリプト"
log "=============================================="
echo ""

###############################################################################
# ステップ1: React SPA（3つ）を停止
###############################################################################

log "ステップ1: React SPA を停止しています..."
echo ""

# SPAのポート一覧
SPA_PORTS=(5173 3001 3000)
SPA_NAMES=("berry-books-spa" "back-office-spa" "customer-hub-spa")

for i in "${!SPA_PORTS[@]}"; do
    PORT=${SPA_PORTS[$i]}
    NAME=${SPA_NAMES[$i]}
    
    log_info "$NAME (ポート: $PORT) を停止中..."
    
    if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
        # Windows (Git Bash)
        PIDS=$(netstat -ano 2>/dev/null | grep ":$PORT " | grep "LISTENING" | awk '{print $5}' | sort -u)
        if [ -n "$PIDS" ]; then
            for PID in $PIDS; do
                taskkill //F //PID "$PID" 2>/dev/null || error_continue "PID $PID の停止に失敗しました"
            done
            log "✓ $NAME を停止しました (ポート: $PORT)"
        else
            log_warn "$NAME は起動していません (ポート: $PORT)"
        fi
    else
        # macOS / Linux
        PIDS=$(lsof -ti ":$PORT" 2>/dev/null || true)
        if [ -n "$PIDS" ]; then
            echo "$PIDS" | xargs kill -9 2>/dev/null || error_continue "ポート $PORT のプロセス停止に失敗しました"
            log "✓ $NAME を停止しました (ポート: $PORT)"
        else
            log_warn "$NAME は起動していません (ポート: $PORT)"
        fi
    fi
done

echo ""

###############################################################################
# ステップ2: Jakarta EE API（3つ）をアンデプロイ
###############################################################################

log "ステップ2: Jakarta EE API をアンデプロイしています..."
echo ""

# API一覧
APIS=("berry-books-api" "back-office-api" "customer-hub-api")

for API in "${APIS[@]}"; do
    log_info "$API をアンデプロイ中..."
    ./gradlew ":$API:undeploy" >> "$LOG_FILE" 2>&1 || error_continue "$API のアンデプロイに失敗しました（デプロイされていない可能性があります）"
    log "✓ $API をアンデプロイしました"
done

echo ""

###############################################################################
# ステップ3: Payara Serverを停止
###############################################################################

log "ステップ3: Payara Server を停止しています..."
echo ""

./gradlew stopPayara >> "$LOG_FILE" 2>&1 || error_continue "Payara Server の停止に失敗しました"
log "✓ Payara Server を停止しました"
echo ""

###############################################################################
# ステップ4: HSQLDBサーバーを停止
###############################################################################

log "ステップ4: HSQLDB サーバーを停止しています..."
echo ""

./gradlew stopHsqldb >> "$LOG_FILE" 2>&1 || error_continue "HSQLDB サーバーの停止に失敗しました"
log "✓ HSQLDB サーバーを停止しました"
echo ""

# 完了メッセージ
echo ""
log "=============================================="
log "すべてのアプリケーションを停止しました！"
log "=============================================="
echo ""
log "${BLUE}■ ログファイル${NC}"
log "  $LOG_FILE"
echo ""
log "=============================================="
