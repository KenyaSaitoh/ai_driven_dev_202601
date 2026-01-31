#!/bin/bash
# ===========================================
# 書籍API テストスクリプト
# ===========================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/_common.sh"

API_BASE="http://localhost:8080/back-office-api-sdd"

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

# ===========================================
# 6. 書籍検索（デフォルト - JPQL）
# ===========================================
echo "6️⃣  書籍検索（デフォルト - JPQL）"
echo "-------------------------------------------"

# キーワードのみで検索
echo ""
echo "🔍 キーワード検索: 'Java'"

RESPONSE=$(api_get "$API_BASE/api/books/search?keyword=Java")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "キーワード検索成功 (HTTP $HTTP_STATUS)"
    echo ""
    BOOK_COUNT=$(echo "$BODY" | grep -o '"bookId":' | wc -l)
    print_info "検索結果: $BOOK_COUNT 冊"
    echo ""
    echo "レスポンス（最初の500文字）:"
    echo "$BODY" | head -c 500
    echo ""
    echo "..."
else
    print_error "キーワード検索失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""

# カテゴリIDとキーワードで検索
echo ""
echo "🔍 カテゴリ + キーワード検索: categoryId=1, keyword='Java'"

RESPONSE=$(api_get "$API_BASE/api/books/search?categoryId=1&keyword=Java")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "カテゴリ + キーワード検索成功 (HTTP $HTTP_STATUS)"
    echo ""
    BOOK_COUNT=$(echo "$BODY" | grep -o '"bookId":' | wc -l)
    print_info "検索結果: $BOOK_COUNT 冊"
    echo ""
    echo "レスポンス（最初の500文字）:"
    echo "$BODY" | head -c 500
    echo ""
    echo "..."
else
    print_error "カテゴリ + キーワード検索失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""

# カテゴリIDのみで検索
echo ""
echo "🔍 カテゴリ検索: categoryId=2"

RESPONSE=$(api_get "$API_BASE/api/books/search?categoryId=2")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "カテゴリ検索成功 (HTTP $HTTP_STATUS)"
    echo ""
    BOOK_COUNT=$(echo "$BODY" | grep -o '"bookId":' | wc -l)
    print_info "検索結果: $BOOK_COUNT 冊"
else
    print_error "カテゴリ検索失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

# ===========================================
# 7. 書籍検索（JPQL - 明示的）
# ===========================================
echo "7️⃣  書籍検索（JPQL - 明示的）"
echo "-------------------------------------------"

RESPONSE=$(api_get "$API_BASE/api/books/search/jpql?keyword=Spring")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "JPQL検索成功 (HTTP $HTTP_STATUS)"
    echo ""
    BOOK_COUNT=$(echo "$BODY" | grep -o '"bookId":' | wc -l)
    print_info "検索結果: $BOOK_COUNT 冊"
    echo ""
    echo "レスポンス（最初の500文字）:"
    echo "$BODY" | head -c 500
    echo ""
    echo "..."
else
    print_error "JPQL検索失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

# ===========================================
# 8. 書籍検索（Criteria API）
# ===========================================
echo "8️⃣  書籍検索（Criteria API - 動的クエリ）"
echo "-------------------------------------------"

# キーワードのみで検索
echo ""
echo "🔍 Criteria検索（キーワード: 'データベース'）"

RESPONSE=$(api_get "$API_BASE/api/books/search/criteria?keyword=データベース")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "Criteria検索成功 (HTTP $HTTP_STATUS)"
    echo ""
    BOOK_COUNT=$(echo "$BODY" | grep -o '"bookId":' | wc -l)
    print_info "検索結果: $BOOK_COUNT 冊"
    echo ""
    echo "レスポンス（最初の500文字）:"
    echo "$BODY" | head -c 500
    echo ""
    echo "..."
else
    print_error "Criteria検索失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""

# カテゴリとキーワードで検索
echo ""
echo "🔍 Criteria検索（categoryId=3, keyword='SQL'）"

RESPONSE=$(api_get "$API_BASE/api/books/search/criteria?categoryId=3&keyword=SQL")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "Criteria検索成功 (HTTP $HTTP_STATUS)"
    echo ""
    BOOK_COUNT=$(echo "$BODY" | grep -o '"bookId":' | wc -l)
    print_info "検索結果: $BOOK_COUNT 冊"
else
    print_error "Criteria検索失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""

# カテゴリのみで検索
echo ""
echo "🔍 Criteria検索（categoryId=1）"

RESPONSE=$(api_get "$API_BASE/api/books/search/criteria?categoryId=1")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "Criteria検索成功 (HTTP $HTTP_STATUS)"
    echo ""
    BOOK_COUNT=$(echo "$BODY" | grep -o '"bookId":' | wc -l)
    print_info "検索結果: $BOOK_COUNT 冊"
else
    print_error "Criteria検索失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

# ===========================================
# 9. 書籍のカテゴリ一覧取得（/books/categories）
# ===========================================
echo "9️⃣  書籍のカテゴリ一覧取得（/books/categories）"
echo "-------------------------------------------"

RESPONSE=$(api_get "$API_BASE/api/books/categories")
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
echo "   ✓ GET /api/books/search (キーワード, カテゴリ+キーワード, カテゴリのみ)"
echo "   ✓ GET /api/books/search/jpql"
echo "   ✓ GET /api/books/search/criteria (キーワード, カテゴリ+キーワード, カテゴリのみ)"
echo "   ✓ GET /api/books/categories"
echo "   ✓ GET /api/categories"
echo ""


