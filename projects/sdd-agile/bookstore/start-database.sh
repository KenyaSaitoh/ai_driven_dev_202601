#!/bin/bash

###############################################################################
# Bookstore Database Starter
# 
# このスクリプトは以下の処理を自動実行します：
# 1. HSQLDB サーバーの起動（起動中であれば再起動）
# 2. データベース接続の確認
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
LOG_FILE="$LOG_DIR/start-database_${TIMESTAMP}.log"

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

# エラーハンドラー
error_exit() {
    log_error "$1"
    log_error "データベース起動が失敗しました。ログファイルを確認してください: $LOG_FILE"
    exit 1
}

# ポートがLISTEN中かチェック
is_port_in_use() {
    local port=$1
    if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
        netstat -ano 2>/dev/null | grep -E ":$port[^0-9]|:\s*$port\s" | grep -q "LISTENING"
    else
        (lsof -i ":$port" 2>/dev/null | grep -q LISTEN) || (bash -c "echo >/dev/tcp/127.0.0.1/$port" 2>/dev/null)
    fi
}

echo ""
log "=============================================="
log "データベース起動スクリプト"
log "=============================================="
echo ""

###############################################################################
# ステップ1: HSQLDB の状態確認
###############################################################################

log "ステップ1: HSQLDB の状態を確認しています..."
echo ""

HSQLDB_PORT=9001

if is_port_in_use $HSQLDB_PORT; then
    log_warn "HSQLDB は既に起動しています (ポート: $HSQLDB_PORT)"
    log_info "HSQLDB を再起動します..."
    
    # 停止
    ./gradlew stopHsqldb >> "$LOG_FILE" 2>&1 || error_exit "HSQLDB の停止に失敗しました"
    log "✓ HSQLDB を停止しました"
    
    # 起動が完全に終わるのを待つ
    sleep 3
else
    log_info "HSQLDB は起動していません"
fi

echo ""

###############################################################################
# ステップ2: HSQLDB を起動
###############################################################################

log "ステップ2: HSQLDB を起動しています..."
echo ""

./gradlew startHsqldb >> "$LOG_FILE" 2>&1 || error_exit "HSQLDB の起動に失敗しました"
log "✓ HSQLDB を起動しました (ポート: $HSQLDB_PORT)"

# 起動を待機
log_info "HSQLDB の起動完了を待機中（5秒）..."
sleep 5

# 接続確認
log_info "データベース接続を確認しています..."
if is_port_in_use $HSQLDB_PORT; then
    log "✓ HSQLDB への接続を確認しました"
else
    error_exit "HSQLDB への接続に失敗しました"
fi

echo ""

# 完了メッセージ
echo ""
log "=============================================="
log "データベースが起動しました！"
log "=============================================="
echo ""
log "${GREEN}■ HSQLDB 接続情報${NC}"
log "  ポート: $HSQLDB_PORT"
log "  データベース名: testdb"
log "  ユーザー名: SA"
log "  パスワード: （空文字）"
log "  JDBC URL: jdbc:hsqldb:hsql://localhost:9001/testdb"
echo ""
log "${YELLOW}■ 停止方法${NC}"
log "  ./gradlew stopHsqldb"
echo ""
log "${BLUE}■ SQLクライアント接続方法${NC}"
log "  Windows (Git Bash):"
log "    java -cp \"hsqldb/lib/hsqldb.jar;hsqldb/lib/sqltool.jar\" org.hsqldb.cmdline.SqlTool --rcFile hsqldb/sqltool.rc testdb"
echo ""
log "  macOS / Linux:"
log "    java -cp \"hsqldb/lib/hsqldb.jar:hsqldb/lib/sqltool.jar\" org.hsqldb.cmdline.SqlTool --rcFile hsqldb/sqltool.rc testdb"
echo ""
log "${BLUE}■ ログファイル${NC}"
log "  $LOG_FILE"
echo ""
log "=============================================="
