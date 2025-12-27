#!/bin/bash
# ===========================================
# Berry Books API 簡易テストスクリプト
# Windows Git Bash対応版
# ===========================================

API_BASE="http://localhost:8080/berry-books-api"
COOKIES="cookies.txt"

echo "========================================="
echo "  Berry Books API 簡易テスト"
echo "========================================="
echo ""

# クリーンアップ
rm -f $COOKIES

# ===========================================
# 1. ログイン
# ===========================================
echo "1. ログイン"
echo "-------------------------------------------"

curl -s -X POST "$API_BASE/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@gmail.com","password":"password"}' \
  -c $COOKIES \
  -w "\nHTTP Status: %{http_code}\n"

echo ""
echo ""

# ===========================================
# 2. 書籍一覧取得
# ===========================================
echo "2. 書籍一覧取得（最初の500文字）"
echo "-------------------------------------------"

curl -s "$API_BASE/api/books" -b $COOKIES | head -c 500
echo ""
echo "..."
echo ""
echo ""

# ===========================================
# 3. 書籍詳細取得
# ===========================================
echo "3. 書籍詳細取得（Book ID: 1）"
echo "-------------------------------------------"

curl -s "$API_BASE/api/books/1" -b $COOKIES
echo ""
echo ""

# ===========================================
# 4. カテゴリフィルタ
# ===========================================
echo "4. カテゴリフィルタ（Category ID: 1）"
echo "-------------------------------------------"

curl -s "$API_BASE/api/books?categoryId=1" -b $COOKIES | head -c 400
echo ""
echo "..."
echo ""
echo ""

# ===========================================
# 5. 現在のユーザー情報
# ===========================================
echo "5. 現在のユーザー情報"
echo "-------------------------------------------"

curl -s "$API_BASE/api/auth/me" -b $COOKIES
echo ""
echo ""

# ===========================================
# 6. 注文作成
# ===========================================
echo "6. 注文作成"
echo "-------------------------------------------"

curl -s -X POST "$API_BASE/api/orders" \
  -H "Content-Type: application/json" \
  -d '{"cartItems":[{"bookId":1,"quantity":2},{"bookId":5,"quantity":1}]}' \
  -b $COOKIES \
  -w "\nHTTP Status: %{http_code}\n"

echo ""
echo ""

# ===========================================
# 7. 注文履歴取得
# ===========================================
echo "7. 注文履歴取得（最初の600文字）"
echo "-------------------------------------------"

curl -s "$API_BASE/api/orders/history" -b $COOKIES | head -c 600
echo ""
echo "..."
echo ""
echo ""

# ===========================================
# 8. 画像API（認証不要）
# ===========================================
echo "8. 書籍表紙画像の確認"
echo "-------------------------------------------"

for BOOK_ID in 1 2 3 5 10; do
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE/api/images/covers/$BOOK_ID")
    if [ "$HTTP_STATUS" == "200" ]; then
        echo "  Book ID $BOOK_ID: ✅ 画像あり"
    else
        echo "  Book ID $BOOK_ID: ❌ 画像なし (HTTP $HTTP_STATUS)"
    fi
done

echo ""
echo ""

# ===========================================
# 9. ログアウト
# ===========================================
echo "9. ログアウト"
echo "-------------------------------------------"

curl -s -X POST "$API_BASE/api/auth/logout" \
  -b $COOKIES \
  -w "HTTP Status: %{http_code}\n"

echo ""

# クリーンアップ
rm -f $COOKIES

echo "========================================="
echo "✨ テスト完了"
echo "========================================="

