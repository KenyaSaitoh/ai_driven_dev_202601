#!/bin/bash
# ===========================================
# 在庫API テストスクリプト
# ===========================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/_common.sh"

API_BASE="http://localhost:8080/back-office-api"

echo "==========================================="
echo "  在庫API テスト"
echo "==========================================="
echo ""

# ===========================================
# 1. 在庫一覧取得
# ===========================================
echo "1️⃣  在庫一覧取得"
echo "-------------------------------------------"

RESPONSE=$(api_get "$API_BASE/api/stocks")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "在庫一覧取得成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス（最初の1000文字）:"
    echo "$BODY" | head -c 1000
    echo ""
    echo "..."
    
    # 在庫数をカウント
    STOCK_COUNT=$(echo "$BODY" | grep -o '"bookId":' | wc -l)
    echo ""
    print_info "取得した在庫数: $STOCK_COUNT 件"
else
    print_error "在庫一覧取得失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

# ===========================================
# 2. 在庫詳細取得
# ===========================================
echo "2️⃣  在庫詳細取得（Book ID: 1）"
echo "-------------------------------------------"

RESPONSE=$(api_get "$API_BASE/api/stocks/1")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "在庫詳細取得成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス:"
    echo "$BODY"
else
    print_error "在庫詳細取得失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

# ===========================================
# 3. 在庫更新
# ===========================================
echo "3️⃣  在庫更新（Book ID: 1）"
echo "-------------------------------------------"

# 現在の在庫を取得
RESPONSE=$(api_get "$API_BASE/api/stocks/1")
CURRENT_STOCK=$(extract_response_body "$RESPONSE")
CURRENT_QUANTITY=$(echo "$CURRENT_STOCK" | grep -o '"quantityInStock":[0-9]*' | cut -d':' -f2)

print_info "現在の在庫数: $CURRENT_QUANTITY"
echo ""

# 在庫を更新（+10）
NEW_QUANTITY=$((CURRENT_QUANTITY + 10))

UPDATE_DATA="{
  \"quantityInStock\": $NEW_QUANTITY
}"

RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
    -X PUT "$API_BASE/api/stocks/1" \
    -H "Content-Type: application/json" \
    -d "$UPDATE_DATA")

HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "在庫更新成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス:"
    echo "$BODY"
    
    # 更新後の在庫を確認
    echo ""
    print_info "更新後の在庫数: $NEW_QUANTITY"
else
    print_error "在庫更新失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

# ===========================================
# 4. 在庫更新（楽観ロックテスト）
# ===========================================
echo "4️⃣  楽観ロックテスト（古いバージョンで更新）"
echo "-------------------------------------------"

# 古いバージョン番号で更新を試みる
OLD_VERSION_DATA='{
  "quantityInStock": 100,
  "version": 0
}'

RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
    -X PUT "$API_BASE/api/stocks/2" \
    -H "Content-Type: application/json" \
    -d "$OLD_VERSION_DATA")

HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "409" ]; then
    print_success "楽観ロックエラーが正しく検出されました (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス:"
    echo "$BODY"
else
    print_warning "予期しないステータスコード: $HTTP_STATUS"
    echo "$BODY"
fi

echo ""
echo ""

# ===========================================
# 5. 在庫詳細取得（複数）
# ===========================================
echo "5️⃣  在庫詳細取得（複数の書籍）"
echo "-------------------------------------------"

for BOOK_ID in 3 5 10; do
    echo ""
    echo "📦 Book ID: $BOOK_ID"
    
    RESPONSE=$(api_get "$API_BASE/api/stocks/$BOOK_ID")
    HTTP_STATUS=$(extract_http_status "$RESPONSE")
    BODY=$(extract_response_body "$RESPONSE")
    
    if [ "$HTTP_STATUS" == "200" ]; then
        print_success "取得成功 (HTTP $HTTP_STATUS)"
        echo "$BODY"
    else
        print_error "取得失敗 (HTTP $HTTP_STATUS)"
    fi
done

echo ""
echo ""

# ===========================================
# 6. 存在しない書籍IDでエラーテスト
# ===========================================
echo "6️⃣  存在しない書籍IDでエラーテスト（Book ID: 99999）"
echo "-------------------------------------------"

RESPONSE=$(api_get "$API_BASE/api/stocks/99999")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "404" ]; then
    print_success "404エラーが正しく返されました (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス:"
    echo "$BODY"
else
    print_warning "予期しないステータスコード: $HTTP_STATUS"
    echo "$BODY"
fi

echo ""
echo ""

echo "==========================================="
echo "✨ 在庫APIテスト完了"
echo "==========================================="
echo ""
print_info "テストされたエンドポイント:"
echo "   ✓ GET /api/stocks"
echo "   ✓ GET /api/stocks/{bookId}"
echo "   ✓ PUT /api/stocks/{bookId}"
echo "   ✓ 楽観ロックテスト"
echo ""


