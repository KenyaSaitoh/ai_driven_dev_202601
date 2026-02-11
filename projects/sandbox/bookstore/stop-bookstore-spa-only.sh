#!/bin/bash

###############################################################################
# Bookstore SPA Stop Script
# 
# SPAを停止するスクリプトです
#
# 使用方法:
#   ./stop-bookstore-spa-only.sh
###############################################################################

set -e

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

# ログ出力関数
log() {
    local message="[$(date +'%Y-%m-%d %H:%M:%S')] $1"
    echo -e "${GREEN}${message}${NC}"
}

log_warn() {
    local message="[WARNING] $1"
    echo -e "${YELLOW}${message}${NC}"
}

log_info() {
    local message="[INFO] $1"
    echo -e "${BLUE}${message}${NC}"
}

echo ""
log "=============================================="
log "Bookstore SPA Stop"
log "=============================================="
echo ""

# SPAプロセスを停止
log "SPAプロセスを停止中..."

# Windowsの場合はtasklistとtaskkillを使用
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
    log_info "Windows環境を検出しました"
    
    # berry-books-spaのポート5173を使用しているプロセスを検索
    BERRY_PID=$(netstat -ano | grep ":5173" | grep "LISTENING" | awk '{print $5}' | head -n 1)
    if [ -n "$BERRY_PID" ]; then
        log_info "  -> berry-books-spa (PID: $BERRY_PID) を停止中..."
        taskkill //PID $BERRY_PID //F > /dev/null 2>&1 || true
        sleep 2
    else
        log_warn "  -> berry-books-spa は起動していません"
    fi
    
    # back-office-spaのポート3001を使用しているプロセスを検索
    BACKOFFICE_PID=$(netstat -ano | grep ":3001" | grep "LISTENING" | awk '{print $5}' | head -n 1)
    if [ -n "$BACKOFFICE_PID" ]; then
        log_info "  -> back-office-spa (PID: $BACKOFFICE_PID) を停止中..."
        taskkill //PID $BACKOFFICE_PID //F > /dev/null 2>&1 || true
        sleep 2
    else
        log_warn "  -> back-office-spa は起動していません"
    fi
    
    # customer-hub-spaのポート3000を使用しているプロセスを検索
    CUSTOMER_PID=$(netstat -ano | grep ":3000" | grep "LISTENING" | awk '{print $5}' | head -n 1)
    if [ -n "$CUSTOMER_PID" ]; then
        log_info "  -> customer-hub-spa (PID: $CUSTOMER_PID) を停止中..."
        taskkill //PID $CUSTOMER_PID //F > /dev/null 2>&1 || true
        sleep 2
    else
        log_warn "  -> customer-hub-spa は起動していません"
    fi
else
    # macOS/Linuxの場合
    log_info "Unix環境を検出しました"
    
    # ポートを使用しているプロセスを停止
    lsof -ti:5173 | xargs kill -9 > /dev/null 2>&1 || log_warn "  -> berry-books-spa は起動していません"
    lsof -ti:3001 | xargs kill -9 > /dev/null 2>&1 || log_warn "  -> back-office-spa は起動していません"
    lsof -ti:3000 | xargs kill -9 > /dev/null 2>&1 || log_warn "  -> customer-hub-spa は起動していません"
    sleep 2
fi

echo ""
log "=============================================="
log "SPA の停止が完了しました！"
log "=============================================="
echo ""
log "${GREEN}■ 停止したSPA${NC}"
log "  - berry-books-spa:  PORT 5173"
log "  - back-office-spa:  PORT 3001"
log "  - customer-hub-spa: PORT 3000"
echo ""
