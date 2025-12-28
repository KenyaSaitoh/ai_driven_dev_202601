#!/bin/bash
# ===========================================
# 書籍API テストスクリプト
# ===========================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/_common.sh"

API_BASE="http://localhost:8080/back-office-api"

echo "==========================================="
echo "  書籍API テスト"
echo "==========================================="
echo ""

# ===========================================
# 1. 書籍一覧取得
# ===========================================
echo "1️⃣  書籍一覧取得（全件）"
echo "-------------------------------------------"

RESPONSE=$(api_get "$API_BASE/api/books")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "書籍一覧取得成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス（最初の800文字）:"
    echo "$BODY" | head -c 800
    echo ""
    echo "..."
    
    # 書籍数をカウント
    BOOK_COUNT=$(echo "$BODY" | grep -o '"bookId":' | wc -l)
    echo ""
    print_info "取得した書籍数: $BOOK_COUNT 冊"
else
    print_error "書籍一覧取得失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

# ===========================================
# 2. 書籍詳細取得
# ===========================================
echo "2️⃣  書籍詳細取得（Book ID: 1）"
echo "-------------------------------------------"

RESPONSE=$(api_get "$API_BASE/api/books/1")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "書籍詳細取得成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス:"
    echo "$BODY"
else
    print_error "書籍詳細取得失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

# ===========================================
# 3. 書籍詳細取得（複数）
# ===========================================
echo "3️⃣  書籍詳細取得（複数の書籍）"
echo "-------------------------------------------"

for BOOK_ID in 2 5 10; do
    echo ""
    echo "📖 Book ID: $BOOK_ID"
    
    RESPONSE=$(api_get "$API_BASE/api/books/$BOOK_ID")
    HTTP_STATUS=$(extract_http_status "$RESPONSE")
    BODY=$(extract_response_body "$RESPONSE")
    
    if [ "$HTTP_STATUS" == "200" ]; then
        print_success "取得成功 (HTTP $HTTP_STATUS)"
        echo "$BODY" | head -c 300
        echo ""
        echo "..."
    else
        print_error "取得失敗 (HTTP $HTTP_STATUS)"
    fi
done

echo ""
echo ""

# ===========================================
# 4. 存在しない書籍IDでエラーテスト
# ===========================================
echo "4️⃣  存在しない書籍IDでエラーテスト（Book ID: 99999）"
echo "-------------------------------------------"

RESPONSE=$(api_get "$API_BASE/api/books/99999")
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

# ===========================================
# 5. カテゴリ一覧取得
# ===========================================
echo "5️⃣  カテゴリ一覧取得"
echo "-------------------------------------------"

RESPONSE=$(api_get "$API_BASE/api/categories")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "カテゴリ一覧取得成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス:"
    echo "$BODY"
else
    print_error "カテゴリ一覧取得失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

echo "==========================================="
echo "✨ 書籍APIテスト完了"
echo "==========================================="
echo ""
print_info "テストされたエンドポイント:"
echo "   ✓ GET /api/books"
echo "   ✓ GET /api/books/{id}"
echo "   ✓ GET /api/categories"
echo ""


