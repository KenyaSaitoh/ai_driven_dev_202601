#!/bin/bash
# ===========================================
# 出版社API テストスクリプト
# ===========================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/_common.sh"

API_BASE="http://localhost:8080/back-office-api"

echo "==========================================="
echo "  出版社API テスト"
echo "==========================================="
echo ""

# ===========================================
# 1. 出版社一覧取得
# ===========================================
echo "1️⃣  出版社一覧取得（全件）"
echo "-------------------------------------------"

RESPONSE=$(api_get "$API_BASE/api/publishers")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "出版社一覧取得成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス:"
    echo "$BODY"
    echo ""
    
    # 出版社数をカウント
    PUBLISHER_COUNT=$(echo "$BODY" | grep -o '"publisherId":' | wc -l)
    print_info "取得した出版社数: $PUBLISHER_COUNT 社"
else
    print_error "出版社一覧取得失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

# ===========================================
# 2. 出版社データの妥当性チェック
# ===========================================
echo "2️⃣  出版社データの妥当性チェック"
echo "-------------------------------------------"

if [ "$HTTP_STATUS" == "200" ]; then
    # publisherIdが含まれているか確認
    if echo "$BODY" | grep -q '"publisherId"'; then
        print_success "publisherId フィールドが含まれています"
    else
        print_error "publisherId フィールドが見つかりません"
    fi
    
    # publisherNameが含まれているか確認
    if echo "$BODY" | grep -q '"publisherName"'; then
        print_success "publisherName フィールドが含まれています"
    else
        print_error "publisherName フィールドが見つかりません"
    fi
    
    # 配列形式か確認
    if echo "$BODY" | grep -q '^\['; then
        print_success "配列形式で返されています"
    else
        print_error "配列形式ではありません"
    fi
fi

echo ""
echo ""

echo "==========================================="
echo "✨ 出版社APIテスト完了"
echo "==========================================="
echo ""
print_info "テストされたエンドポイント:"
echo "   ✓ GET /api/publishers"
echo ""


