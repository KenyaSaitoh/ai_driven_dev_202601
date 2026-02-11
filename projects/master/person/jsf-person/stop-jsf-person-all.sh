#!/bin/bash

###############################################################################
# JSF Person Application Stopper
# 
# このスクリプトは以下の処理を自動実行します：
# 1. jsf-person アプリケーションをアンデプロイ
# 2. Payara Serverを停止
# 3. HSQLDBサーバーを停止
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
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../" && pwd)"
cd "$PROJECT_ROOT"

# ログファイル
LOG_DIR="$PROJECT_ROOT/logs"
mkdir -p "$LOG_DIR"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
LOG_FILE="$LOG_DIR/stop-jsf-person-all_${TIMESTAMP}.log"

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
log "JSF Person アプリケーション停止スクリプト"
log "=============================================="
echo ""

###############################################################################
# ステップ1: jsf-person をアンデプロイ
###############################################################################

log "ステップ1: jsf-person をアンデプロイしています..."
echo ""

./gradlew :jsf-person:undeploy >> "$LOG_FILE" 2>&1 || error_continue "jsf-person のアンデプロイに失敗しました（デプロイされていない可能性があります）"
log "✓ jsf-person をアンデプロイしました"
echo ""

###############################################################################
# ステップ2: Payara Serverを停止
###############################################################################

log "ステップ2: Payara Server を停止しています..."
echo ""

./gradlew stopPayara >> "$LOG_FILE" 2>&1 || error_continue "Payara Server の停止に失敗しました"
log "✓ Payara Server を停止しました"
echo ""

###############################################################################
# ステップ3: HSQLDBサーバーを停止
###############################################################################

log "ステップ3: HSQLDB サーバーを停止しています..."
echo ""

./gradlew stopHsqldb >> "$LOG_FILE" 2>&1 || error_continue "HSQLDB サーバーの停止に失敗しました"
log "✓ HSQLDB サーバーを停止しました"
echo ""

# 完了メッセージ
echo ""
log "=============================================="
log "アプリケーションを停止しました！"
log "=============================================="
echo ""
log "${BLUE}■ ログファイル${NC}"
log "  $LOG_FILE"
echo ""
log "=============================================="
