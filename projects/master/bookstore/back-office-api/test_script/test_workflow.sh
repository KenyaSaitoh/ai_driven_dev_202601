#!/bin/bash
# ===========================================
# ワークフローAPI テストスクリプト
# ===========================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/_common.sh"

API_BASE="http://localhost:8080/back-office-api"
COOKIES="cookies_workflow.txt"

echo "==========================================="
echo "  ワークフローAPI テスト"
echo "==========================================="
echo ""

# クリーンアップ
rm -f $COOKIES

# ===========================================
# 1. ワークフロー作成（CREATE: 書籍追加）
# ===========================================
echo "1️⃣  ワークフロー作成（書籍追加）"
echo "-------------------------------------------"

WORKFLOW_CREATE_DATA='{
  "workflowType": "CREATE",
  "bookName": "新Javaプログラミングガイド",
  "author": "山田太郎",
  "categoryId": 1,
  "publisherId": 1,
  "price": 3500,
  "imageUrl": "https://example.com/images/new-java-guide.jpg",
  "applyReason": "新刊書籍の追加",
  "createdBy": 6
}'

RESPONSE=$(api_post "$API_BASE/api/workflows" "$WORKFLOW_CREATE_DATA")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "201" ]; then
    print_success "ワークフロー作成成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス:"
    echo "$BODY" | head -c 500
    echo ""
    
    # ワークフローIDを抽出（簡易版）
    WORKFLOW_ID=$(echo "$BODY" | grep -o '"workflowId":[0-9]*' | head -1 | cut -d':' -f2)
    echo ""
    print_info "作成されたワークフローID: $WORKFLOW_ID"
else
    print_error "ワークフロー作成失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

# ===========================================
# 2. ワークフロー作成（PRICE_TEMP_ADJUSTMENT: 価格調整）
# ===========================================
echo "2️⃣  ワークフロー作成（価格一時調整）"
echo "-------------------------------------------"

PRICE_ADJUST_DATA='{
  "workflowType": "PRICE_TEMP_ADJUSTMENT",
  "bookId": 1,
  "price": 2400,
  "applyReason": "春のキャンペーン割引",
  "startDate": "2025-02-01",
  "endDate": "2025-02-28",
  "createdBy": 9
}'

RESPONSE=$(api_post "$API_BASE/api/workflows" "$PRICE_ADJUST_DATA")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "201" ]; then
    print_success "価格調整ワークフロー作成成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス:"
    echo "$BODY" | head -c 500
    echo ""
    
    WORKFLOW_ID_2=$(echo "$BODY" | grep -o '"workflowId":[0-9]*' | head -1 | cut -d':' -f2)
    print_info "作成されたワークフローID: $WORKFLOW_ID_2"
else
    print_error "価格調整ワークフロー作成失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

# ===========================================
# 3. ワークフロー作成（DELETE: 書籍削除）
# ===========================================
echo "3️⃣  ワークフロー作成（書籍削除）"
echo "-------------------------------------------"

DELETE_WORKFLOW_DATA='{
  "workflowType": "DELETE",
  "bookId": 50,
  "applyReason": "絶版につき販売終了",
  "createdBy": 2
}'

RESPONSE=$(api_post "$API_BASE/api/workflows" "$DELETE_WORKFLOW_DATA")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "201" ]; then
    print_success "削除ワークフロー作成成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス:"
    echo "$BODY" | head -c 500
    echo ""
    
    WORKFLOW_ID_3=$(echo "$BODY" | grep -o '"workflowId":[0-9]*' | head -1 | cut -d':' -f2)
    print_info "作成されたワークフローID: $WORKFLOW_ID_3"
else
    print_error "削除ワークフロー作成失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

# ===========================================
# 4. ワークフロー一覧取得（NEW状態）
# ===========================================
echo "4️⃣  ワークフロー一覧取得（状態: NEW）"
echo "-------------------------------------------"

RESPONSE=$(api_get "$API_BASE/api/workflows?state=NEW")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "ワークフロー一覧取得成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス（最初の800文字）:"
    echo "$BODY" | head -c 800
    echo ""
    echo "..."
else
    print_error "ワークフロー一覧取得失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

# ===========================================
# 5. ワークフロー申請（APPLY）
# ===========================================
if [ -n "$WORKFLOW_ID" ]; then
    echo "5️⃣  ワークフロー申請（Workflow ID: $WORKFLOW_ID）"
    echo "-------------------------------------------"
    
    APPLY_DATA='{
      "operatedBy": 6
    }'
    
    RESPONSE=$(api_post "$API_BASE/api/workflows/$WORKFLOW_ID/apply" "$APPLY_DATA")
    HTTP_STATUS=$(extract_http_status "$RESPONSE")
    BODY=$(extract_response_body "$RESPONSE")
    
    if [ "$HTTP_STATUS" == "200" ]; then
        print_success "ワークフロー申請成功 (HTTP $HTTP_STATUS)"
        echo ""
        echo "レスポンス:"
        echo "$BODY" | head -c 500
        echo ""
    else
        print_error "ワークフロー申請失敗 (HTTP $HTTP_STATUS)"
        echo "$BODY"
    fi
    
    echo ""
    echo ""
fi

# ===========================================
# 6. ワークフロー履歴取得
# ===========================================
if [ -n "$WORKFLOW_ID" ]; then
    echo "6️⃣  ワークフロー履歴取得（Workflow ID: $WORKFLOW_ID）"
    echo "-------------------------------------------"
    
    RESPONSE=$(api_get "$API_BASE/api/workflows/$WORKFLOW_ID")
    HTTP_STATUS=$(extract_http_status "$RESPONSE")
    BODY=$(extract_response_body "$RESPONSE")
    
    if [ "$HTTP_STATUS" == "200" ]; then
        print_success "ワークフロー履歴取得成功 (HTTP $HTTP_STATUS)"
        echo ""
        echo "レスポンス:"
        echo "$BODY" | head -c 800
        echo ""
        echo "..."
    else
        print_error "ワークフロー履歴取得失敗 (HTTP $HTTP_STATUS)"
        echo "$BODY"
    fi
    
    echo ""
    echo ""
fi

# ===========================================
# 7. ワークフロー承認（APPROVE）
# ===========================================
if [ -n "$WORKFLOW_ID" ]; then
    echo "7️⃣  ワークフロー承認（Workflow ID: $WORKFLOW_ID）"
    echo "-------------------------------------------"
    
    APPROVE_DATA='{
      "operatedBy": 1
    }'
    
    RESPONSE=$(api_post "$API_BASE/api/workflows/$WORKFLOW_ID/approve" "$APPROVE_DATA")
    HTTP_STATUS=$(extract_http_status "$RESPONSE")
    BODY=$(extract_response_body "$RESPONSE")
    
    if [ "$HTTP_STATUS" == "200" ]; then
        print_success "ワークフロー承認成功 (HTTP $HTTP_STATUS)"
        echo ""
        echo "レスポンス:"
        echo "$BODY" | head -c 500
        echo ""
    else
        print_error "ワークフロー承認失敗 (HTTP $HTTP_STATUS)"
        echo "$BODY"
    fi
    
    echo ""
    echo ""
fi

# ===========================================
# 8. ワークフロー却下（REJECT）
# ===========================================
if [ -n "$WORKFLOW_ID_3" ]; then
    echo "8️⃣  ワークフロー却下（Workflow ID: $WORKFLOW_ID_3）"
    echo "-------------------------------------------"
    
    # まず申請する
    APPLY_DATA='{
      "operatedBy": 2
    }'
    
    api_post "$API_BASE/api/workflows/$WORKFLOW_ID_3/apply" "$APPLY_DATA" > /dev/null
    
    sleep 1
    
    # 却下する
    REJECT_DATA='{
      "operatedBy": 11,
      "operationReason": "在庫が残っているため削除不可"
    }'
    
    RESPONSE=$(api_post "$API_BASE/api/workflows/$WORKFLOW_ID_3/reject" "$REJECT_DATA")
    HTTP_STATUS=$(extract_http_status "$RESPONSE")
    BODY=$(extract_response_body "$RESPONSE")
    
    if [ "$HTTP_STATUS" == "200" ]; then
        print_success "ワークフロー却下成功 (HTTP $HTTP_STATUS)"
        echo ""
        echo "レスポンス:"
        echo "$BODY" | head -c 500
        echo ""
    else
        print_error "ワークフロー却下失敗 (HTTP $HTTP_STATUS)"
        echo "$BODY"
    fi
    
    echo ""
    echo ""
fi

# ===========================================
# 9. ワークフロー一覧取得（承認済み）
# ===========================================
echo "9️⃣  ワークフロー一覧取得（状態: APPROVED）"
echo "-------------------------------------------"

RESPONSE=$(api_get "$API_BASE/api/workflows?state=APPROVED")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "承認済みワークフロー一覧取得成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス（最初の800文字）:"
    echo "$BODY" | head -c 800
    echo ""
    echo "..."
else
    print_error "承認済みワークフロー一覧取得失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

# ===========================================
# 10. ワークフロー全件取得
# ===========================================
echo "🔟  ワークフロー全件取得"
echo "-------------------------------------------"

RESPONSE=$(api_get "$API_BASE/api/workflows")
HTTP_STATUS=$(extract_http_status "$RESPONSE")
BODY=$(extract_response_body "$RESPONSE")

if [ "$HTTP_STATUS" == "200" ]; then
    print_success "ワークフロー全件取得成功 (HTTP $HTTP_STATUS)"
    echo ""
    echo "レスポンス（最初の1000文字）:"
    echo "$BODY" | head -c 1000
    echo ""
    echo "..."
else
    print_error "ワークフロー全件取得失敗 (HTTP $HTTP_STATUS)"
    echo "$BODY"
fi

echo ""
echo ""

# クリーンアップ
rm -f $COOKIES

echo "==========================================="
echo "✨ ワークフローAPIテスト完了"
echo "==========================================="
echo ""
print_info "テストされたエンドポイント:"
echo "   ✓ POST   /api/workflows (CREATE, PRICE_TEMP_ADJUSTMENT, DELETE)"
echo "   ✓ GET    /api/workflows?state=NEW"
echo "   ✓ GET    /api/workflows?state=APPROVED"
echo "   ✓ GET    /api/workflows (全件)"
echo "   ✓ GET    /api/workflows/{workflowId}"
echo "   ✓ POST   /api/workflows/{workflowId}/apply"
echo "   ✓ POST   /api/workflows/{workflowId}/approve"
echo "   ✓ POST   /api/workflows/{workflowId}/reject"
echo ""


