#!/bin/bash
# ===========================================
# 書籍API テストスクリプト
# ===========================================

API_BASE="http://localhost:8080/berry-books-api"
COOKIES_FILE="cookies_books.txt"

echo "========================================="
echo "  書籍API テスト"
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
# 1. 書籍一覧取得 (GET /api/books)
# ===========================================
echo "1️⃣  書籍一覧取得（全件）"
echo "-------------------------------------------"

BOOKS_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  "$API_BASE/api/books" \
  -b $COOKIES_FILE)

HTTP_STATUS=$(echo "$BOOKS_RESPONSE" | grep -oP 'HTTP_STATUS:\K\d+')
RESPONSE_BODY=$(echo "$BOOKS_RESPONSE" | sed 's/HTTP_STATUS:[0-9]*//')

if [ "$HTTP_STATUS" == "200" ]; then
    echo "✅ 書籍一覧取得成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス（最初の3冊）:"
    # 最初の1000文字を表示
    echo "$RESPONSE_BODY" | head -c 1000
    echo ""
    echo "..."
    echo ""
    # 書籍数をカウント
    BOOK_COUNT=$(echo "$RESPONSE_BODY" | grep -o '"bookId"' | wc -l)
    echo "📚 取得した書籍数: $BOOK_COUNT 冊"
else
    echo "❌ 書籍一覧取得失敗 (HTTP $HTTP_STATUS)"
    echo "$RESPONSE_BODY"
fi
echo ""
echo ""

# ===========================================
# 2. 書籍一覧取得（カテゴリフィルタ付き）
# ===========================================
echo "2️⃣  書籍一覧取得（カテゴリID=1でフィルタ）"
echo "-------------------------------------------"

FILTERED_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  "$API_BASE/api/books?categoryId=1" \
  -b $COOKIES_FILE)

HTTP_STATUS=$(echo "$FILTERED_RESPONSE" | grep -oP 'HTTP_STATUS:\K\d+')
RESPONSE_BODY=$(echo "$FILTERED_RESPONSE" | sed 's/HTTP_STATUS:[0-9]*//')

if [ "$HTTP_STATUS" == "200" ]; then
    echo "✅ フィルタ付き書籍一覧取得成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス（最初の800文字）:"
    echo "$RESPONSE_BODY" | head -c 800
    echo ""
    echo "..."
    echo ""
    BOOK_COUNT=$(echo "$RESPONSE_BODY" | grep -o '"bookId"' | wc -l)
    echo "📚 カテゴリID=1 の書籍数: $BOOK_COUNT 冊"
else
    echo "❌ フィルタ付き書籍一覧取得失敗 (HTTP $HTTP_STATUS)"
    echo "$RESPONSE_BODY"
fi
echo ""
echo ""

# ===========================================
# 3. 書籍詳細取得 (GET /api/books/{id})
# ===========================================
echo "3️⃣  書籍詳細取得（Book ID: 1）"
echo "-------------------------------------------"

BOOK_DETAIL=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  "$API_BASE/api/books/1" \
  -b $COOKIES_FILE)

HTTP_STATUS=$(echo "$BOOK_DETAIL" | grep -oP 'HTTP_STATUS:\K\d+')
RESPONSE_BODY=$(echo "$BOOK_DETAIL" | sed 's/HTTP_STATUS:[0-9]*//')

if [ "$HTTP_STATUS" == "200" ]; then
    echo "✅ 書籍詳細取得成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス:"
    echo "$RESPONSE_BODY"
else
    echo "❌ 書籍詳細取得失敗 (HTTP $HTTP_STATUS)"
    echo "$RESPONSE_BODY"
fi
echo ""
echo ""

# ===========================================
# 4. 存在しない書籍ID（エラーテスト）
# ===========================================
echo "4️⃣  存在しない書籍IDでのアクセステスト"
echo "-------------------------------------------"
echo "   Book ID: 99999（404エラーが正常）"
echo ""

NOT_FOUND=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  "$API_BASE/api/books/99999" \
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
# 5. 複数の書籍詳細を取得
# ===========================================
echo "5️⃣  複数の書籍詳細を連続取得"
echo "-------------------------------------------"

for BOOK_ID in 1 5 10 15 20; do
    DETAIL=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
      "$API_BASE/api/books/$BOOK_ID" \
      -b $COOKIES_FILE)
    
    HTTP_STATUS=$(echo "$DETAIL" | grep -oP 'HTTP_STATUS:\K\d+')
    RESPONSE_BODY=$(echo "$DETAIL" | sed 's/HTTP_STATUS:[0-9]*//')
    
    if [ "$HTTP_STATUS" == "200" ]; then
        BOOK_NAME=$(echo "$RESPONSE_BODY" | grep -oP '"bookName":"\K[^"]+' | head -1)
        PRICE=$(echo "$RESPONSE_BODY" | grep -oP '"price":\K[0-9]+' | head -1)
        echo "  📖 Book ID $BOOK_ID: $BOOK_NAME (¥$PRICE)"
    else
        echo "  ⚠️  Book ID $BOOK_ID: HTTP $HTTP_STATUS"
    fi
done
echo ""

# クリーンアップ
rm -f $COOKIES_FILE

echo "========================================="
echo "✨ 書籍APIテスト完了"
echo "========================================="

