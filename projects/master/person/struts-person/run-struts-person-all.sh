#!/bin/bash

###############################################################################
# Struts Person Application Launcher
# 
# このスクリプトは以下の処理を自動実行します：
# 1. TomEE 8 の初期設定（初回のみ、既に設定済みの場合はスキップ）
# 2. HSQLDB サーバーの起動
# 3. struts-person のDB初期化
# 4. TomEE 8 の起動（バックグラウンド）
# 5. struts-person のWAR化、デプロイ
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
LOG_FILE="$LOG_DIR/run-struts-person-all_${TIMESTAMP}.log"

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
log "Struts Person Application Launcher"
log "=============================================="
echo ""

# ステップ1: TomEE 8 の初期設定（初回のみ）
log "STEP 1: TomEE 8 の初期設定を確認..."
log_info "  -> server.xml を初期化中..."
if ! ./gradlew :struts-person:initTomee8Config >> "$LOG_FILE" 2>&1; then
    log_warn "initTomee8Config が失敗しましたが、続行します（既に初期化済みの可能性）"
else
    log "✓ server.xml の初期化が完了しました"
fi

log_info "  -> データソースを設定中..."
if ! ./gradlew :struts-person:configureTomee8DataSource >> "$LOG_FILE" 2>&1; then
    log_warn "configureTomee8DataSource が失敗しましたが、続行します（既に設定済みの可能性）"
else
    log "✓ データソースの設定が完了しました"
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

# ステップ3: struts-person のデータベースセットアップ
log "STEP 3: struts-person のデータベースセットアップ..."
log_info "  -> データベーステーブルを作成中..."
./gradlew :struts-person:setupHsqldb >> "$LOG_FILE" 2>&1 || error_exit "struts-person の DB セットアップに失敗"
log "✓ データベースセットアップが完了しました"
echo ""

# ステップ4: TomEE 8 の起動（バックグラウンド）
log "STEP 4: TomEE 8 を起動（バックグラウンド）..."
if ! ./gradlew :struts-person:startTomee8Background >> "$LOG_FILE" 2>&1; then
    log_warn "TomEE 8 の起動が失敗しましたが、続行します（既に起動中の可能性）"
else
    log "✓ TomEE 8 の起動コマンドを実行しました"
fi
echo ""

# サーバー起動完了を待機（ポート8080がリッスン状態になるまで）
log_info "TomEE 8 の起動完了を待機中..."
MAX_WAIT=120  # 最大待機時間（秒）
WAIT_INTERVAL=2  # チェック間隔（秒）
ELAPSED=0
PORT=8080

while [ $ELAPSED -lt $MAX_WAIT ]; do
    # Windows環境の場合
    if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
        if netstat -ano | grep ":$PORT" | grep "LISTENING" > /dev/null 2>&1; then
            log "✓ TomEE 8 が起動しました（ポート $PORT がリッスン状態）"
            break
        fi
    else
        # Unix/Linux/Mac環境の場合
        if lsof -ti:$PORT > /dev/null 2>&1 || nc -z localhost $PORT > /dev/null 2>&1; then
            log "✓ TomEE 8 が起動しました（ポート $PORT がリッスン状態）"
            break
        fi
    fi
    
    sleep $WAIT_INTERVAL
    ELAPSED=$((ELAPSED + WAIT_INTERVAL))
    
    if [ $((ELAPSED % 10)) -eq 0 ]; then
        log_info "  待機中... (${ELAPSED}秒経過)"
    fi
done

if [ $ELAPSED -ge $MAX_WAIT ]; then
    log_warn "TomEE 8 の起動確認がタイムアウトしました（${MAX_WAIT}秒経過）"
    log_warn "サーバーログを確認してください: $PROJECT_ROOT/tomee8/logs/catalina.out"
    log_warn "続行しますが、デプロイが失敗する可能性があります"
else
    # 追加の待機時間（ContainerSystemの初期化完了を待つ）
    log_info "ContainerSystem の初期化完了を待機中（5秒）..."
    sleep 5
fi
echo ""

# ステップ5: struts-person のビルドとデプロイ
log "STEP 5: struts-person のビルドとデプロイ..."
log_info "  -> WAR ファイルをビルド中..."
log_info "  -> デプロイ中..."
./gradlew :struts-person:deployToTomee8 >> "$LOG_FILE" 2>&1 || error_exit "struts-person のデプロイに失敗"
log "✓ struts-person のデプロイが完了しました"
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
log "  - struts-person: http://localhost:8080/struts-person/"
echo ""
log "${YELLOW}■ 停止方法${NC}"
log "  アプリケーション:"
log "    ./gradlew :struts-person:undeployFromTomee8"
log ""
log "  サーバー:"
log "    ./gradlew :struts-person:stopTomee8"
log "    ./gradlew stopHsqldb"
echo ""
log "${BLUE}■ ログファイル${NC}"
log "  メインログ: $LOG_FILE"
log "  サーバーログ: $PROJECT_ROOT/tomee8/logs/catalina.out"
echo ""
log "${YELLOW}■ 注意事項${NC}"
log "  TomEE 8 はバックグラウンドで起動しています。"
log "  フォアグラウンドモード（ログを直接確認）で起動したい場合は、"
log "  手動で以下を実行してください:"
log "    ./gradlew :struts-person:startTomee8"
echo ""
log "=============================================="
