#!/bin/bash
# ===========================================
# 共通関数ライブラリ
# ===========================================

# HTTPステータスコードを抽出
extract_http_status() {
    local response="$1"
    echo "$response" | grep "HTTP_STATUS:" | sed 's/.*HTTP_STATUS://'
}

# レスポンスボディを抽出（HTTPステータス行を除去）
extract_response_body() {
    local response="$1"
    echo "$response" | sed '/HTTP_STATUS:/d'
}

# APIリクエストを実行（GET）
api_get() {
    local url="$1"
    local cookies_file="$2"
    
    if [ -z "$cookies_file" ]; then
        curl -s -w "\nHTTP_STATUS:%{http_code}" "$url"
    else
        curl -s -w "\nHTTP_STATUS:%{http_code}" "$url" -b "$cookies_file"
    fi
}

# APIリクエストを実行（POST）
api_post() {
    local url="$1"
    local data="$2"
    local cookies_file="$3"
    
    if [ -z "$cookies_file" ]; then
        curl -s -w "\nHTTP_STATUS:%{http_code}" \
            -X POST "$url" \
            -H "Content-Type: application/json" \
            -d "$data"
    else
        curl -s -w "\nHTTP_STATUS:%{http_code}" \
            -X POST "$url" \
            -H "Content-Type: application/json" \
            -d "$data" \
            -b "$cookies_file"
    fi
}

# APIリクエストを実行（POST with Cookie save）
api_post_with_cookie() {
    local url="$1"
    local data="$2"
    local cookies_file="$3"
    
    curl -s -w "\nHTTP_STATUS:%{http_code}" \
        -X POST "$url" \
        -H "Content-Type: application/json" \
        -d "$data" \
        -c "$cookies_file"
}

# カラー出力
print_success() {
    echo "✅ $1"
}

print_error() {
    echo "❌ $1"
}

print_warning() {
    echo "⚠️  $1"
}

print_info() {
    echo "📝 $1"
}

