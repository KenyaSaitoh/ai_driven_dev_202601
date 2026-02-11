#!/bin/bash

###############################################################################
# JSF Person Application Launcher (SDD)
# 
# このスクリプトは以下の処理を自動実行します：
# 1. HSQLDB サーバーの起動
# 2. Payara Server の起動
# 3. データソースのセットアップ
# 4. jsf-person-sdd-wf のDB初期化、WAR化、デプロイ
###############################################################################

set -e  # エラーが発生したら即座に終了

# 文字エンコーディング設定（Windows環境での文字化け対策）
export LANG=C.UTF-8
export LC_ALL=C.UTF-8

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
LOG_FILE="$LOG_DIR/start-jsf-person-all-sdd_${TIMESTAMP}.log"

# ログ出力関数（文字化け対策: 色コード付きを標準出力、色コードなしをログファイル）
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
    log_error "セットアップが失敗しました。ログファイルを確認してください: $LOG_FILE"
    exit 1
}

# ポートがLISTEN中かチェック（起動済みならスキップ用）
# HSQLDB=9001, Payara admin=4848
is_port_in_use() {
    local port=$1
    if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
        netstat -ano 2>/dev/null | grep -E ":$port[^0-9]|:\s*$port\s" | grep -q "LISTENING"
    else
        (lsof -i ":$port" 2>/dev/null | grep -q LISTEN) || (bash -c "echo >/dev/tcp/127.0.0.1/$port" 2>/dev/null)
    fi
}

# 開始メッセージ
echo ""
log "=============================================="
log "JSF Person Application Launcher (SDD)"
log "=============================================="
echo ""

# ステップ1: HSQLDB サーバーの起動
log "STEP 1: HSQLDB サーバーを起動..."
if is_port_in_use 9001; then
    log_warn "HSQLDB は既に起動中のためスキップしました（port 9001）"
else
    if ! ./gradlew startHsqldb >> "$LOG_FILE" 2>&1; then
        log_warn "HSQLDB の起動が失敗しましたが、続行します（既に起動中の可能性）"
    else
        log "✓ HSQLDB サーバーが起動しました"
    fi
fi
echo ""

# ステップ2: Payara Server の起動
log "STEP 2: Payara Server を起動..."
if is_port_in_use 4848; then
    log_warn "Payara Server は既に起動中のためスキップしました（port 4848）"
else
    if ! ./gradlew startPayara >> "$LOG_FILE" 2>&1; then
        log_warn "Payara Server の起動が失敗しましたが、続行します（既に起動中の可能性）"
    else
        log "✓ Payara Server が起動しました"
    fi
fi
echo ""

# サーバー起動を待機
log_info "サーバーの起動を待機中（10秒）..."
sleep 10

# ステップ3: データソースのセットアップ
log "STEP 3: データソースをセットアップ..."
if ! ./gradlew setupDataSource >> "$LOG_FILE" 2>&1; then
    log_warn "データソースのセットアップが失敗しましたが、続行します（既に作成済みの可能性）"
else
    log "✓ データソースのセットアップが完了しました"
fi
echo ""

# ステップ4: jsf-person-sdd-wf のセットアップとデプロイ
log "STEP 4: jsf-person-sdd-wf のセットアップとデプロイ..."
log_info "  -> データベーステーブルを作成中..."
./gradlew :jsf-person-sdd-wf:setupHsqldb >> "$LOG_FILE" 2>&1 || error_exit "jsf-person-sdd-wf の DB セットアップに失敗"
log_info "  -> WAR ファイルをビルド中..."
./gradlew :jsf-person-sdd-wf:war >> "$LOG_FILE" 2>&1 || error_exit "jsf-person-sdd-wf のビルドに失敗"
log_info "  -> デプロイ中..."
./gradlew :jsf-person-sdd-wf:deploy >> "$LOG_FILE" 2>&1 || error_exit "jsf-person-sdd-wf のデプロイに失敗"
log "✓ jsf-person-sdd-wf のデプロイが完了しました"
echo ""

# デプロイの完了を待機
log_info "アプリケーションのデプロイ完了を待機中（5秒）..."
sleep 5

# 完了メッセージ
cd "$PROJECT_ROOT"
echo ""
log "=============================================="
log "セットアップが完了しました！"
log "=============================================="
echo ""
log "${GREEN}■ アプリケーション${NC}"
log "  - jsf-person: http://localhost:8080/jsf-person/faces/PersonTablePage.xhtml"
echo ""
log "${YELLOW}■ 停止方法${NC}"
log "  アプリケーション:"
log "    ./gradlew :jsf-person-sdd-wf:undeploy"
log ""
log "  サーバー:"
log "    ./gradlew stopPayara"
log "    ./gradlew stopHsqldb"
echo ""
log "${BLUE}■ ログファイル${NC}"
log "  メインログ: $LOG_FILE"
log "  サーバーログ: $PROJECT_ROOT/payara6/glassfish/domains/domain1/logs/server.log"
echo ""
log "=============================================="
