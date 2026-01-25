#!/bin/bash

###############################################################################
# Bookstore SPA Restart Script
# 
# SPAを再起動するスクリプトです
#
# 使用方法:
#   ./run-bookstore-spa.sh           # 通常の再起動
#   ./run-bookstore-spa.sh --clean   # Viteキャッシュクリア + 再起動
#   ./run-bookstore-spa.sh --rebuild # 完全リビルド (node_modules削除 + npm install + 再起動)
###############################################################################

set -e

# オプション処理
CLEAN_CACHE=false
FULL_REBUILD=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --clean)
            CLEAN_CACHE=true
            shift
            ;;
        --rebuild|--full-clean)
            FULL_REBUILD=true
            shift
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [--clean|--rebuild]"
            exit 1
            ;;
    esac
done

# 色の定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# スクリプトのルートディレクトリ（プロジェクトルート）
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../" && pwd)"
cd "$PROJECT_ROOT"

# ログディレクトリ
LOG_DIR="$PROJECT_ROOT/logs"
mkdir -p "$LOG_DIR"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# ログ出力関数（文字化け対策: 色コード付きを標準出力、色コードなしをログファイル）
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
log "Bookstore SPA Restart"
if [ "$FULL_REBUILD" = true ]; then
    log "モード: 完全リビルド (node_modules削除 + npm install)"
elif [ "$CLEAN_CACHE" = true ]; then
    log "モード: キャッシュクリア (Viteキャッシュ削除)"
else
    log "モード: 通常再起動"
fi
log "=============================================="
echo ""

# ステップ1: 既存のSPAプロセスを停止
log "STEP 1: 既存のSPAプロセスを停止..."

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

log "✓ プロセスの停止が完了しました"
echo ""

# ステップ2: クリーンアップ/リビルド（オプション）
STEP_NUM=2

if [ "$FULL_REBUILD" = true ]; then
    log "STEP $STEP_NUM: 完全リビルド（node_modules削除 + npm install）..."
    
    # berry-books-spa
    log_info "  -> berry-books-spa をリビルド中..."
    cd "$PROJECT_ROOT/projects/master/bookstore/berry-books-spa"
    rm -rf node_modules package-lock.json
    npm install >> "$LOG_DIR/berry-books-spa_${TIMESTAMP}.log" 2>&1
    
    # back-office-spa
    log_info "  -> back-office-spa をリビルド中..."
    cd "$PROJECT_ROOT/projects/master/bookstore/back-office-spa"
    rm -rf node_modules package-lock.json
    npm install >> "$LOG_DIR/back-office-spa_${TIMESTAMP}.log" 2>&1
    
    # customer-hub-spa
    log_info "  -> customer-hub-spa をリビルド中..."
    cd "$PROJECT_ROOT/projects/master/bookstore/customer-hub-spa"
    rm -rf node_modules package-lock.json
    npm install >> "$LOG_DIR/customer-hub-spa_${TIMESTAMP}.log" 2>&1
    
    cd "$PROJECT_ROOT"
    log "✓ 完全リビルドが完了しました"
    echo ""
    STEP_NUM=$((STEP_NUM + 1))
    
elif [ "$CLEAN_CACHE" = true ]; then
    log "STEP $STEP_NUM: Viteキャッシュをクリア..."
    
    log_info "  -> berry-books-spa のキャッシュをクリア中..."
    rm -rf "$PROJECT_ROOT/projects/master/bookstore/berry-books-spa/node_modules/.vite"
    
    log_info "  -> back-office-spa のキャッシュをクリア中..."
    rm -rf "$PROJECT_ROOT/projects/master/bookstore/back-office-spa/node_modules/.vite"
    
    log_info "  -> customer-hub-spa のキャッシュをクリア中..."
    rm -rf "$PROJECT_ROOT/projects/master/bookstore/customer-hub-spa/node_modules/.vite"
    
    log "✓ キャッシュのクリアが完了しました"
    echo ""
    STEP_NUM=$((STEP_NUM + 1))
fi

# berry-books-spa を起動
log "STEP $STEP_NUM: berry-books-spa を起動..."
cd "$PROJECT_ROOT/projects/master/bookstore/berry-books-spa"
nohup npm run dev > "$LOG_DIR/berry-books-spa_${TIMESTAMP}.log" 2>&1 &
BERRY_SPA_PID=$!
log "✓ berry-books-spa が起動しました (PID: $BERRY_SPA_PID, PORT: 5173)"
echo ""
STEP_NUM=$((STEP_NUM + 1))

# back-office-spa を起動
log "STEP $STEP_NUM: back-office-spa を起動..."
cd "$PROJECT_ROOT/projects/master/bookstore/back-office-spa"
nohup npm run dev > "$LOG_DIR/back-office-spa_${TIMESTAMP}.log" 2>&1 &
BACKOFFICE_SPA_PID=$!
log "✓ back-office-spa が起動しました (PID: $BACKOFFICE_SPA_PID, PORT: 3001)"
echo ""
STEP_NUM=$((STEP_NUM + 1))

# customer-hub-spa を起動
log "STEP $STEP_NUM: customer-hub-spa を起動..."
cd "$PROJECT_ROOT/projects/master/bookstore/customer-hub-spa"
nohup npm run dev > "$LOG_DIR/customer-hub-spa_${TIMESTAMP}.log" 2>&1 &
CUSTOMER_SPA_PID=$!
log "✓ customer-hub-spa が起動しました (PID: $CUSTOMER_SPA_PID, PORT: 3000)"
echo ""

# 起動待機
log_info "SPA の起動完了を待機中（15秒）..."
sleep 15

# 完了メッセージ
cd "$PROJECT_ROOT"
echo ""
log "=============================================="
if [ "$FULL_REBUILD" = true ]; then
    log "SPA の完全リビルドと再起動が完了しました！"
elif [ "$CLEAN_CACHE" = true ]; then
    log "SPA のキャッシュクリアと再起動が完了しました！"
else
    log "SPA の再起動が完了しました！"
fi
log "=============================================="
echo ""
log "${GREEN}■ フロントエンド SPA${NC}"
log "  - berry-books-spa:  http://localhost:5173 (PID: $BERRY_SPA_PID)"
log "  - back-office-spa:  http://localhost:3001 (PID: $BACKOFFICE_SPA_PID)"
log "  - customer-hub-spa: http://localhost:3000 (PID: $CUSTOMER_SPA_PID)"
echo ""
log "${GREEN}■ ログイン情報${NC}"
log "  back-office-spa:"
log "    社員コード: E00001"
log "    パスワード: password"
echo ""
log "  berry-books-spa:"
log "    メール: alice@example.com"
log "    パスワード: password"
echo ""
log "${YELLOW}■ 停止方法${NC}"
log "  kill $BERRY_SPA_PID $BACKOFFICE_SPA_PID $CUSTOMER_SPA_PID"
echo ""
log "${BLUE}■ ログファイル${NC}"
log "  $LOG_DIR/berry-books-spa_${TIMESTAMP}.log"
log "  $LOG_DIR/back-office-spa_${TIMESTAMP}.log"
log "  $LOG_DIR/customer-hub-spa_${TIMESTAMP}.log"
echo ""
log "${BLUE}■ スクリプトの使い方${NC}"
log "  通常再起動:           ./run-bookstore-spa.sh"
log "  キャッシュクリア:     ./run-bookstore-spa.sh --clean"
log "  完全リビルド:         ./run-bookstore-spa.sh --rebuild"
echo ""
log "=============================================="
