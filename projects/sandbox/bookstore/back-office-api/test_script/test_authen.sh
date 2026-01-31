#!/bin/bash
# ===========================================
# 認証API テストスクリプト
# ===========================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/_common.sh"

API_BASE="http://localhost:8080/back-office-api"
COOKIES="cookies_authen.txt"

echo "==========================================="
echo "  認証API テスト"
echo "==========================================="
echo ""

# クリーンアップ
rm -f $COOKIES

# ===========================================
# 1. ログイン（成功）
# ===========================================
echo "1️⃣  ログイン（成功）"
echo "-------------------------------------------"

LOGIN_DATA='{
  "employeeCode": "E00001",
  "password": "pass001"
}'

RESPONSE=$(api_post_with_cookie "$API_BASE/api/auth/login" "$LOGIN_DATA" "$COOKIES")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "ログイン成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス:"
    echo "$BODY"
    echo ""
    
    # EmployeeIDを抽出
    EMPLOYEE_ID=$(echo "$BODY" | grep -o '"employeeId":[0-9]*' | head -1 | cut -d':' -f2)
    print_info "ログインした社員ID: $EMPLOYEE_ID"
    
    # Cookieが設定されたか確認
    if [ -f "$COOKIES" ]; then
        print_success "認証Cookieが保存されました"
    else
        print_warning "認証Cookieが保存されませんでした"
    fi
else
    print_error "ログイン失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

# ===========================================
# 2. ログイン（失敗 - 存在しない社員コード）
# ===========================================
echo "2️⃣  ログイン（失敗 - 存在しない社員コード）"
echo "-------------------------------------------"

INVALID_LOGIN_DATA='{
  "employeeCode": "INVALID",
  "password": "pass001"
}'

RESPONSE=$(api_post "$API_BASE/api/auth/login" "$INVALID_LOGIN_DATA")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "401" ]; then
    print_success "401エラーが正しく返されました (HTTP $HTTP_STATUS)"
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
# 3. ログイン（失敗 - 間違ったパスワード）
# ===========================================
echo "3️⃣  ログイン（失敗 - 間違ったパスワード）"
echo "-------------------------------------------"

WRONG_PASSWORD_DATA='{
  "employeeCode": "E00001",
  "password": "wrongpassword"
}'

RESPONSE=$(api_post "$API_BASE/api/auth/login" "$WRONG_PASSWORD_DATA")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "401" ]; then
    print_success "401エラーが正しく返されました (HTTP $HTTP_STATUS)"
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
# 4. 現在のログインユーザー情報取得（未実装）
# ===========================================
echo "4️⃣  現在のログインユーザー情報取得（未実装）"
echo "-------------------------------------------"

RESPONSE=$(api_get "$API_BASE/api/auth/me" "$COOKIES")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "501" ]; then
    print_success "501エラー（未実装）が正しく返されました (HTTP $HTTP_STATUS)"
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
# 5. ログアウト
# ===========================================
echo "5️⃣  ログアウト"
echo "-------------------------------------------"

RESPONSE=$(api_post "$API_BASE/api/auth/logout" "{}" "$COOKIES")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "ログアウト成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス:"
    echo "$BODY"
else
    print_error "ログアウト失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

# ===========================================
# 6. 複数社員でログインテスト
# ===========================================
echo "6️⃣  複数社員でログインテスト"
echo "-------------------------------------------"

EMPLOYEES=(
    '{"employeeCode":"E00002","password":"pass002"}'
    '{"employeeCode":"E00006","password":"pass006"}'
    '{"employeeCode":"E00009","password":"pass009"}'
)

for LOGIN_DATA in "${EMPLOYEES[@]}"; do
    EMPLOYEE_CODE=$(echo "$LOGIN_DATA" | grep -o '"employeeCode":"[^"]*"' | cut -d'"' -f4)
    echo ""
    echo "👤 社員コード: $EMPLOYEE_CODE"
    
    RESPONSE=$(api_post "$API_BASE/api/auth/login" "$LOGIN_DATA")
    HTTP_STATUS=$(extract_http_status "$RESPONSE")
    BODY=$(extract_response_body "$RESPONSE")
    
    if [ "$HTTP_STATUS" == "200" ]; then
        EMPLOYEE_NAME=$(echo "$BODY" | grep -o '"employeeName":"[^"]*"' | cut -d'"' -f4)
        print_success "ログイン成功: $EMPLOYEE_NAME (HTTP $HTTP_STATUS)"
    else
        print_error "ログイン失敗 (HTTP $HTTP_STATUS)"
    fi
done

echo ""
echo ""

# クリーンアップ
rm -f $COOKIES

echo "==========================================="
echo "✨ 認証APIテスト完了"
echo "==========================================="
echo ""
print_info "テストされたエンドポイント:"
echo "   ✓ POST /api/auth/login (成功)"
echo "   ✓ POST /api/auth/login (失敗 - 存在しない社員)"
echo "   ✓ POST /api/auth/login (失敗 - 間違ったパスワード)"
echo "   ✓ GET  /api/auth/me (未実装)"
echo "   ✓ POST /api/auth/logout"
echo ""


