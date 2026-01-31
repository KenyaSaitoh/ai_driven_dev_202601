#!/bin/bash
# ===========================================
# Back Office API 簡易テストスクリプト
# Windows Git Bash対応版
# ===========================================

API_BASE="http://localhost:8080/back-office-api-sdd"

echo "========================================="
echo "  Back Office API 簡易テスト"
echo "========================================="
echo ""

# ===========================================
# 1. 書籍一覧取得
# ===========================================
echo "1. 書籍一覧取得（最初の500文字）"
echo "-------------------------------------------"

curl -s "$API_BASE/api/books" | head -c 500
echo ""
echo "..."
echo ""
echo ""

# ===========================================
# 2. 書籍詳細取得
# ===========================================
echo "2. 書籍詳細取得（Book ID: 1）"
echo "-------------------------------------------"

curl -s "$API_BASE/api/books/1"
echo ""
echo ""

# ===========================================
# 3. カテゴリ一覧取得
# ===========================================
echo "3. カテゴリ一覧取得"
echo "-------------------------------------------"

curl -s "$API_BASE/api/categories"
echo ""
echo ""

# ===========================================
# 4. 出版社一覧取得
# ===========================================
echo "4. 出版社一覧取得"
echo "-------------------------------------------"

curl -s "$API_BASE/api/publishers"
echo ""
echo ""

# ===========================================
# 5. 在庫一覧取得
# ===========================================
echo "5. 在庫一覧取得（最初の600文字）"
echo "-------------------------------------------"

curl -s "$API_BASE/api/stocks" | head -c 600
echo ""
echo "..."
echo ""
echo ""

# ===========================================
# 6. 在庫詳細取得
# ===========================================
echo "6. 在庫詳細取得（Book ID: 1）"
echo "-------------------------------------------"

curl -s "$API_BASE/api/stocks/1"
echo ""
echo ""

# ===========================================
# 7. ワークフロー作成（書籍追加）
# ===========================================
echo "7. ワークフロー作成（書籍追加）"
echo "-------------------------------------------"

curl -s -X POST "$API_BASE/api/workflows" \
  -H "Content-Type: application/json" \
  -d '{
    "workflowType": "ADD_NEW_BOOK",
    "bookName": "テスト書籍",
    "author": "テスト著者",
    "categoryId": 1,
    "publisherId": 1,
    "price": 3000,
    "applyReason": "テスト用の書籍追加",
    "createdBy": 6
  }' \
  -w "\nHTTP Status: %{http_code}\n"

echo ""
echo ""

# ===========================================
# 8. ワークフロー一覧取得（NEW状態）
# ===========================================
echo "8. ワークフロー一覧取得（状態: NEW）"
echo "-------------------------------------------"

curl -s "$API_BASE/api/workflows?state=NEW" | head -c 800
echo ""
echo "..."
echo ""
echo ""

# ===========================================
# 9. ワークフロー全件取得
# ===========================================
echo "9. ワークフロー全件取得（最初の1000文字）"
echo "-------------------------------------------"

curl -s "$API_BASE/api/workflows" | head -c 1000
echo ""
echo "..."
echo ""
echo ""

# ===========================================
# 10. 各種HTTPステータスの確認
# ===========================================
echo "10. 各種HTTPステータスの確認"
echo "-------------------------------------------"

echo "📖 書籍一覧:"
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE/api/books")
if [ "$HTTP_STATUS" == "200" ]; then
    echo "  ✅ 正常 (HTTP $HTTP_STATUS)"
else
    echo "  ❌ エラー (HTTP $HTTP_STATUS)"
fi

echo "📂 カテゴリ一覧:"
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE/api/categories")
if [ "$HTTP_STATUS" == "200" ]; then
    echo "  ✅ 正常 (HTTP $HTTP_STATUS)"
else
    echo "  ❌ エラー (HTTP $HTTP_STATUS)"
fi

echo "📚 出版社一覧:"
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE/api/publishers")
if [ "$HTTP_STATUS" == "200" ]; then
    echo "  ✅ 正常 (HTTP $HTTP_STATUS)"
else
    echo "  ❌ エラー (HTTP $HTTP_STATUS)"
fi

echo "📦 在庫一覧:"
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE/api/stocks")
if [ "$HTTP_STATUS" == "200" ]; then
    echo "  ✅ 正常 (HTTP $HTTP_STATUS)"
else
    echo "  ❌ エラー (HTTP $HTTP_STATUS)"
fi

echo "🔄 ワークフロー一覧:"
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE/api/workflows")
if [ "$HTTP_STATUS" == "200" ]; then
    echo "  ✅ 正常 (HTTP $HTTP_STATUS)"
else
    echo "  ❌ エラー (HTTP $HTTP_STATUS)"
fi

echo ""

echo "========================================="
echo "✨ テスト完了"
echo "========================================="

