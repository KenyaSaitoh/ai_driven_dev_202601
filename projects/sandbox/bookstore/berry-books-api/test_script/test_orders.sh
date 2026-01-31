#!/bin/bash
# ===========================================
# 注文API テストスクリプト
# ===========================================

API_BASE="http://localhost:8080/berry-books-api"
COOKIES_FILE="cookies_orders.txt"

echo "========================================="
echo "  注文API テスト"
echo "========================================="
echo ""

# クリーンアップ
rm -f $COOKIES_FILE

# ===========================================
# 事前準備: ログイン
# ===========================================
echo "🔐 ログイン中..."

LOGIN_DATA='{"email":"alice@gmail.com","password":"password"}'

LOGIN_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  -X POST "$API_BASE/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "$LOGIN_DATA" \
  -c $COOKIES_FILE)

HTTP_STATUS=$(echo "$LOGIN_RESPONSE" | grep -oP 'HTTP_STATUS:\K\d+')

if [ "$HTTP_STATUS" != "200" ]; then
    echo "❌ ログインに失敗しました (HTTP $HTTP_STATUS)"
    exit 1
fi

echo "✅ ログイン成功"
echo ""
echo ""

# ===========================================
# 1. 注文作成 (POST /api/orders)
# ===========================================
echo "1️⃣  注文作成"
echo "-------------------------------------------"

ORDER_DATA='{
  "cartItems": [
    {
      "bookId": 1,
      "quantity": 2
    },
    {
      "bookId": 5,
      "quantity": 1
    },
    {
      "bookId": 10,
      "quantity": 3
    }
  ]
}'

echo "リクエスト:"
echo "$ORDER_DATA"
echo ""

ORDER_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  -X POST "$API_BASE/api/orders" \
  -H "Content-Type: application/json" \
  -d "$ORDER_DATA" \
  -b $COOKIES_FILE)

HTTP_STATUS=$(echo "$ORDER_RESPONSE" | grep -oP 'HTTP_STATUS:\K\d+')
RESPONSE_BODY=$(echo "$ORDER_RESPONSE" | sed 's/HTTP_STATUS:[0-9]*//')

if [ "$HTTP_STATUS" == "200" ] || [ "$HTTP_STATUS" == "201" ]; then
    echo "✅ 注文作成成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス:"
    echo "$RESPONSE_BODY"
    echo ""
    # 注文IDを抽出
    ORDER_ID=$(echo "$RESPONSE_BODY" | grep -oP '"tranId":\K[0-9]+' | head -1)
    if [ ! -z "$ORDER_ID" ]; then
        echo "📝 作成された注文ID: $ORDER_ID"
        # 次のテストのために保存
        echo "$ORDER_ID" > /tmp/last_order_id.txt
    fi
else
    echo "❌ 注文作成失敗 (HTTP $HTTP_STATUS)"
    echo "$RESPONSE_BODY"
fi
echo ""
echo ""

# ===========================================
# 2. 注文履歴取得 (GET /api/orders/history)
# ===========================================
echo "2️⃣  注文履歴取得"
echo "-------------------------------------------"

HISTORY_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  "$API_BASE/api/orders/history" \
  -b $COOKIES_FILE)

HTTP_STATUS=$(echo "$HISTORY_RESPONSE" | grep -oP 'HTTP_STATUS:\K\d+')
RESPONSE_BODY=$(echo "$HISTORY_RESPONSE" | sed 's/HTTP_STATUS:[0-9]*//')

if [ "$HTTP_STATUS" == "200" ]; then
    echo "✅ 注文履歴取得成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス（最初の1200文字）:"
    echo "$RESPONSE_BODY" | head -c 1200
    echo ""
    echo "..."
    echo ""
    ORDER_COUNT=$(echo "$RESPONSE_BODY" | grep -o '"tranId"' | wc -l)
    echo "📦 注文件数: $ORDER_COUNT 件"
else
    echo "❌ 注文履歴取得失敗 (HTTP $HTTP_STATUS)"
    echo "$RESPONSE_BODY"
fi
echo ""
echo ""

# ===========================================
# 3. 注文詳細取得 (GET /api/orders/{id})
# ===========================================
echo "3️⃣  注文詳細取得"
echo "-------------------------------------------"

# 先ほど作成した注文IDまたは既存の注文ID
if [ -f /tmp/last_order_id.txt ]; then
    TEST_ORDER_ID=$(cat /tmp/last_order_id.txt)
else
    # 履歴から最初の注文IDを取得
    TEST_ORDER_ID=$(echo "$RESPONSE_BODY" | grep -oP '"tranId":\K[0-9]+' | head -1)
fi

if [ -z "$TEST_ORDER_ID" ]; then
    echo "⚠️  テスト用の注文IDが見つかりません。注文ID=1でテストします。"
    TEST_ORDER_ID=1
fi

echo "注文ID: $TEST_ORDER_ID"
echo ""

DETAIL_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  "$API_BASE/api/orders/$TEST_ORDER_ID" \
  -b $COOKIES_FILE)

HTTP_STATUS=$(echo "$DETAIL_RESPONSE" | grep -oP 'HTTP_STATUS:\K\d+')
RESPONSE_BODY=$(echo "$DETAIL_RESPONSE" | sed 's/HTTP_STATUS:[0-9]*//')

if [ "$HTTP_STATUS" == "200" ]; then
    echo "✅ 注文詳細取得成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス:"
    echo "$RESPONSE_BODY"
else
    echo "❌ 注文詳細取得失敗 (HTTP $HTTP_STATUS)"
    echo "$RESPONSE_BODY"
fi
echo ""
echo ""

# ===========================================
# 4. 存在しない注文ID（エラーテスト）
# ===========================================
echo "4️⃣  存在しない注文IDでのアクセステスト"
echo "-------------------------------------------"
echo "   注文ID: 99999（404エラーが正常）"
echo ""

NOT_FOUND=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  "$API_BASE/api/orders/99999" \
  -b $COOKIES_FILE)

HTTP_STATUS=$(echo "$NOT_FOUND" | grep -oP 'HTTP_STATUS:\K\d+')
RESPONSE_BODY=$(echo "$NOT_FOUND" | sed 's/HTTP_STATUS:[0-9]*//')

if [ "$HTTP_STATUS" == "404" ]; then
    echo "✅ 正常：404エラーが発生しました"
    echo "レスポンス:"
    echo "$RESPONSE_BODY"
else
    echo "⚠️  予期しないステータス: HTTP $HTTP_STATUS"
    echo "$RESPONSE_BODY"
fi
echo ""
echo ""

# ===========================================
# 5. 在庫不足エラーのテスト
# ===========================================
echo "5️⃣  在庫不足エラーのテスト"
echo "-------------------------------------------"
echo "   大量の数量を注文して在庫不足エラーを発生させる"
echo ""

OVERSTOCK_DATA='{
  "cartItems": [
    {
      "bookId": 1,
      "quantity": 999
    }
  ]
}'

OVERSTOCK_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  -X POST "$API_BASE/api/orders" \
  -H "Content-Type: application/json" \
  -d "$OVERSTOCK_DATA" \
  -b $COOKIES_FILE)

HTTP_STATUS=$(echo "$OVERSTOCK_RESPONSE" | grep -oP 'HTTP_STATUS:\K\d+')
RESPONSE_BODY=$(echo "$OVERSTOCK_RESPONSE" | sed 's/HTTP_STATUS:[0-9]*//')

if [ "$HTTP_STATUS" == "400" ] || [ "$HTTP_STATUS" == "409" ]; then
    echo "✅ 正常：在庫不足エラーが発生しました (HTTP $HTTP_STATUS)"
    echo "レスポンス:"
    echo "$RESPONSE_BODY"
else
    echo "⚠️  予期しないステータス: HTTP $HTTP_STATUS"
    echo "$RESPONSE_BODY"
fi
echo ""

# クリーンアップ
rm -f $COOKIES_FILE
rm -f /tmp/last_order_id.txt

echo "========================================="
echo "✨ 注文APIテスト完了"
echo "========================================="

