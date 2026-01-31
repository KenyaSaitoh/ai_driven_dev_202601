#!/bin/bash

###############################################################################
# Bookstore Full Stack Application Launcher (SDD)
# 
# このスクリプトは以下の処理を自動実行します：
# 1. GlassFish (Payara Server) の初期化と起動
# 2. HSQLDB サーバーの起動
# 3. データソースのセットアップ
# 4. 3つのJakarta EE APIのDB初期化、WAR化、デプロイ
#    - back-office-api-sdd
#    - berry-books-api-sdd
#    - customer-hub-api
# 5. 3つのReact SPAの依存関係インストールと起動
#    - berry-books-spa (http://localhost:5173)
#    - back-office-spa (http://localhost:3001)
#    - customer-hub-spa (http://localhost:3000)
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
LOG_FILE="$LOG_DIR/run-bookstore-all-sdd_${TIMESTAMP}.log"

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

# 開始メッセージ
echo ""
log "=============================================="
log "Bookstore Full Stack Launcher (SDD)"
log "=============================================="
echo ""

# ステップ1: Payara Server (GlassFish) の初期化
log "STEP 1: Payara Server の初期化を実行..."
if ! ./gradlew initPayaraDomainConfig >> "$LOG_FILE" 2>&1; then
    log_warn "initPayaraDomainConfig が失敗しましたが、続行します（既に初期化済みの可能性）"
else
    log "✓ Payara Server の初期化が完了しました"
fi
echo ""

# ステップ2: HSQLDB サーバーの起動
log "STEP 2: HSQLDB サーバーを起動..."
if ! ./gradlew startHsqldb >> "$LOG_FILE" 2>&1; then
    log_warn "HSQLDB の起動が失敗しましたが、続行します（既に起動中の可能性）"
else
    log "✓ HSQLDB サーバーが起動しました"
fi
echo ""

# ステップ3: Payara Server の起動
log "STEP 3: Payara Server を起動..."
if ! ./gradlew startPayara >> "$LOG_FILE" 2>&1; then
    log_warn "Payara Server の起動が失敗しましたが、続行します（既に起動中の可能性）"
else
    log "✓ Payara Server が起動しました"
fi
echo ""

# サーバー起動を待機
log_info "サーバーの起動を待機中（10秒）..."
sleep 10

# ステップ4: データソースのセットアップ
log "STEP 4: データソースをセットアップ..."
if ! ./gradlew setupDataSource >> "$LOG_FILE" 2>&1; then
    log_warn "データソースのセットアップが失敗しましたが、続行します（既に作成済みの可能性）"
else
    log "✓ データソースのセットアップが完了しました"
fi
echo ""

# ステップ5: back-office-api-sdd のセットアップとデプロイ
log "STEP 5: back-office-api-sdd のセットアップとデプロイ..."
log_info "  -> データベーステーブルを作成中..."
./gradlew :back-office-api-sdd-agile:setupHsqldb >> "$LOG_FILE" 2>&1 || error_exit "back-office-api-sdd の DB セットアップに失敗"
log_info "  -> WAR ファイルをビルド中..."
./gradlew :back-office-api-sdd-agile:war >> "$LOG_FILE" 2>&1 || error_exit "back-office-api-sdd のビルドに失敗"
log_info "  -> デプロイ中..."
./gradlew :back-office-api-sdd-agile:deploy >> "$LOG_FILE" 2>&1 || error_exit "back-office-api-sdd のデプロイに失敗"
log "✓ back-office-api-sdd のデプロイが完了しました"
echo ""

# ステップ6: berry-books-api-sdd のセットアップとデプロイ
log "STEP 6: berry-books-api-sdd のセットアップとデプロイ..."
log_info "  -> データベーステーブルを作成中..."
./gradlew :berry-books-api-sdd-agile:setupHsqldb >> "$LOG_FILE" 2>&1 || error_exit "berry-books-api-sdd の DB セットアップに失敗"
log_info "  -> WAR ファイルをビルド中..."
./gradlew :berry-books-api-sdd-agile:war >> "$LOG_FILE" 2>&1 || error_exit "berry-books-api-sdd のビルドに失敗"
log_info "  -> デプロイ中..."
./gradlew :berry-books-api-sdd-agile:deploy >> "$LOG_FILE" 2>&1 || error_exit "berry-books-api-sdd のデプロイに失敗"
log "✓ berry-books-api-sdd のデプロイが完了しました"
echo ""

# ステップ7: customer-hub-api のセットアップとデプロイ
log "STEP 7: customer-hub-api のセットアップとデプロイ..."
log_info "  -> データベーステーブルを作成中..."
./gradlew :customer-hub-api:setupHsqldb >> "$LOG_FILE" 2>&1 || error_exit "customer-hub-api の DB セットアップに失敗"
log_info "  -> WAR ファイルをビルド中..."
./gradlew :customer-hub-api:war >> "$LOG_FILE" 2>&1 || error_exit "customer-hub-api のビルドに失敗"
log_info "  -> デプロイ中..."
./gradlew :customer-hub-api:deploy >> "$LOG_FILE" 2>&1 || error_exit "customer-hub-api のデプロイに失敗"
log "✓ customer-hub-api のデプロイが完了しました"
echo ""

# APIデプロイの完了を待機
log_info "API のデプロイ完了を待機中（5秒）..."
sleep 5

# ステップ8: berry-books-spa のセットアップと起動
log "STEP 8: berry-books-spa のセットアップと起動..."
cd "$PROJECT_ROOT/projects/master/bookstore/berry-books-spa"
log_info "  -> 依存関係をインストール中..."
if [ ! -d "node_modules" ]; then
    npm install >> "$LOG_FILE" 2>&1 || error_exit "berry-books-spa の npm install に失敗"
else
    log_info "  -> node_modules が既に存在するため、スキップします"
fi
log_info "  -> 開発サーバーを起動中（バックグラウンド）..."
nohup npm run dev > "$LOG_DIR/berry-books-spa_${TIMESTAMP}.log" 2>&1 &
BERRY_SPA_PID=$!
log "✓ berry-books-spa が起動しました (PID: $BERRY_SPA_PID, PORT: 5173)"
echo ""

# ステップ9: back-office-spa のセットアップと起動
log "STEP 9: back-office-spa のセットアップと起動..."
cd "$PROJECT_ROOT/projects/master/bookstore/back-office-spa"
log_info "  -> 依存関係をインストール中..."
if [ ! -d "node_modules" ]; then
    npm install >> "$LOG_FILE" 2>&1 || error_exit "back-office-spa の npm install に失敗"
else
    log_info "  -> node_modules が既に存在するため、スキップします"
fi
log_info "  -> 開発サーバーを起動中（バックグラウンド）..."
nohup npm run dev > "$LOG_DIR/back-office-spa_${TIMESTAMP}.log" 2>&1 &
BACKOFFICE_SPA_PID=$!
log "✓ back-office-spa が起動しました (PID: $BACKOFFICE_SPA_PID, PORT: 3001)"
echo ""

# ステップ10: customer-hub-spa のセットアップと起動
log "STEP 10: customer-hub-spa のセットアップと起動..."
cd "$PROJECT_ROOT/projects/master/bookstore/customer-hub-spa"
log_info "  -> 依存関係をインストール中..."
if [ ! -d "node_modules" ]; then
    npm install >> "$LOG_FILE" 2>&1 || error_exit "customer-hub-spa の npm install に失敗"
else
    log_info "  -> node_modules が既に存在するため、スキップします"
fi
log_info "  -> 開発サーバーを起動中（バックグラウンド）..."
nohup npm run dev > "$LOG_DIR/customer-hub-spa_${TIMESTAMP}.log" 2>&1 &
CUSTOMER_SPA_PID=$!
log "✓ customer-hub-spa が起動しました (PID: $CUSTOMER_SPA_PID, PORT: 3000)"
echo ""

# SPAの起動を待機
log_info "SPA の起動完了を待機中（15秒）..."
sleep 15

# 完了メッセージ
cd "$PROJECT_ROOT"
echo ""
log "=============================================="
log "セットアップが完了しました！"
log "=============================================="
echo ""
log "${GREEN}■ バックエンド API (SDD)${NC}"
log "  - back-office-api-sdd:  http://localhost:8080/back-office-api-sdd/api"
log "  - berry-books-api-sdd:  http://localhost:8080/berry-books-api-sdd/api"
log "  - customer-hub-api:     http://localhost:8080/customer-hub-api/api"
echo ""
log "${GREEN}■ フロントエンド SPA${NC}"
log "  - berry-books-spa:  http://localhost:5173 (PID: $BERRY_SPA_PID)"
log "  - back-office-spa:  http://localhost:3001 (PID: $BACKOFFICE_SPA_PID)"
log "  - customer-hub-spa: http://localhost:3000 (PID: $CUSTOMER_SPA_PID)"
echo ""
log "${YELLOW}■ 停止方法${NC}"
log "  バックエンド:"
log "    ./gradlew cleanupAll"
log "    ./gradlew stopPayara"
log "    ./gradlew stopHsqldb"
echo ""
log "  フロントエンド:"
log "    kill $BERRY_SPA_PID $BACKOFFICE_SPA_PID $CUSTOMER_SPA_PID"
echo ""
log "${BLUE}■ ログファイル${NC}"
log "  メインログ: $LOG_FILE"
log "  SPAログ:    $LOG_DIR/berry-books-spa_${TIMESTAMP}.log"
log "              $LOG_DIR/back-office-spa_${TIMESTAMP}.log"
log "              $LOG_DIR/customer-hub-spa_${TIMESTAMP}.log"
echo ""
log "=============================================="
