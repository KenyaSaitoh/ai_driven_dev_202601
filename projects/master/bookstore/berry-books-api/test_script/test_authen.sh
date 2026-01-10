#!/bin/bash
# ===========================================
# 認証API テストスクリプト
# ===========================================

API_BASE="http://localhost:8080/berry-books-api"
COOKIES_FILE="cookies_auth.txt"

echo "========================================="
echo "  認証API テスト"
echo "========================================="
echo ""

# クリーンアップ
rm -f $COOKIES_FILE

# ===========================================
# 1. ユーザー登録 (POST /api/auth/register)
# ===========================================
echo "1️⃣  新規ユーザー登録"
echo "-------------------------------------------"

REGISTER_DATA='{
  "customerName": "テストユーザー",
  "email": "test'$(date +%s)'@example.com",
  "password": "testpass123",
  "birthday": "1995-05-15",
  "address": "東京都新宿区テスト1-2-3"
}'

echo "リクエスト:"
echo "$REGISTER_DATA" | grep -v '^$'
echo ""

REGISTER_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  -X POST "$API_BASE/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "$REGISTER_DATA")

HTTP_STATUS=$(echo "$REGISTER_RESPONSE" | grep -oP 'HTTP_STATUS:\K\d+')
RESPONSE_BODY=$(echo "$REGISTER_RESPONSE" | sed 's/HTTP_STATUS:[0-9]*//')

echo "レスポンス (HTTP $HTTP_STATUS):"
echo "$RESPONSE_BODY" | head -100
echo ""
echo ""

# ===========================================
# 2. ログイン (POST /api/auth/login)
# ===========================================
echo "2️⃣  ログイン"
echo "-------------------------------------------"

LOGIN_DATA='{
  "email": "alice@gmail.com",
  "password": "password"
}'

echo "リクエスト:"
echo "$LOGIN_DATA"
echo ""

LOGIN_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  -X POST "$API_BASE/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "$LOGIN_DATA" \
  -c $COOKIES_FILE)

HTTP_STATUS=$(echo "$LOGIN_RESPONSE" | grep -oP 'HTTP_STATUS:\K\d+')
RESPONSE_BODY=$(echo "$LOGIN_RESPONSE" | sed 's/HTTP_STATUS:[0-9]*//')

if [ "$HTTP_STATUS" == "200" ]; then
    echo "✅ ログイン成功 (HTTP $HTTP_STATUS)"
    echo "レスポンス:"
    echo "$RESPONSE_BODY"
else
    echo "❌ ログイン失敗 (HTTP $HTTP_STATUS)"
    echo "$RESPONSE_BODY"
    exit 1
fi
echo ""
echo ""

# ===========================================
# 3. 現在のユーザー情報取得 (GET /api/auth/me)
# ===========================================
echo "3️⃣  現在のユーザー情報取得"
echo "-------------------------------------------"

ME_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  "$API_BASE/api/auth/me" \
  -b $COOKIES_FILE)

HTTP_STATUS=$(echo "$ME_RESPONSE" | grep -oP 'HTTP_STATUS:\K\d+')
RESPONSE_BODY=$(echo "$ME_RESPONSE" | sed 's/HTTP_STATUS:[0-9]*//')

if [ "$HTTP_STATUS" == "200" ]; then
    echo "✅ ユーザー情報取得成功 (HTTP $HTTP_STATUS)"
    echo "レスポンス:"
    echo "$RESPONSE_BODY"
else
    echo "❌ ユーザー情報取得失敗 (HTTP $HTTP_STATUS)"
    echo "$RESPONSE_BODY"
fi
echo ""
echo ""

# ===========================================
# 4. ログアウト (POST /api/auth/logout)
# ===========================================
echo "4️⃣  ログアウト"
echo "-------------------------------------------"

LOGOUT_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  -X POST "$API_BASE/api/auth/logout" \
  -b $COOKIES_FILE)

HTTP_STATUS=$(echo "$LOGOUT_RESPONSE" | grep -oP 'HTTP_STATUS:\K\d+')
RESPONSE_BODY=$(echo "$LOGOUT_RESPONSE" | sed 's/HTTP_STATUS:[0-9]*//')

if [ "$HTTP_STATUS" == "200" ]; then
    echo "✅ ログアウト成功 (HTTP $HTTP_STATUS)"
else
    echo "❌ ログアウト失敗 (HTTP $HTTP_STATUS)"
    echo "$RESPONSE_BODY"
fi
echo ""
echo ""

# ===========================================
# 5. ログアウト後のアクセステスト（失敗するはず）
# ===========================================
echo "5️⃣  ログアウト後のアクセステスト（401エラーが正常）"
echo "-------------------------------------------"

TEST_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  "$API_BASE/api/auth/me" \
  -b $COOKIES_FILE)

HTTP_STATUS=$(echo "$TEST_RESPONSE" | grep -oP 'HTTP_STATUS:\K\d+')

if [ "$HTTP_STATUS" == "401" ] || [ "$HTTP_STATUS" == "403" ]; then
    echo "✅ 正常：認証エラーが発生しました (HTTP $HTTP_STATUS)"
else
    echo "⚠️  予期しないステータス: HTTP $HTTP_STATUS"
fi
echo ""

# クリーンアップ
rm -f $COOKIES_FILE

echo "========================================="
echo "✨ 認証APIテスト完了"
echo "========================================="

