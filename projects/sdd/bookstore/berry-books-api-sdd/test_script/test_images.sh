#!/bin/bash
# ===========================================
# 画像API テストスクリプト
# ===========================================

API_BASE="http://localhost:8080/berry-books-api-sdd"

echo "========================================="
echo "  画像API テスト"
echo "========================================="
echo ""
echo "注意: 画像APIは認証不要です"
echo ""

# ===========================================
# 1. 書籍表紙画像取得 (GET /api/images/covers/{bookId})
# ===========================================
echo "1️⃣  書籍表紙画像取得（Book ID: 1）"
echo "-------------------------------------------"

IMAGE_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  "$API_BASE/api/images/covers/1" \
  -o /tmp/book_cover_1.jpg)

HTTP_STATUS=$(echo "$IMAGE_RESPONSE" | grep "HTTP_STATUS:" | sed 's/.*HTTP_STATUS://')

if [ "$HTTP_STATUS" == "200" ]; then
    echo "✅ 画像取得成功 (HTTP $HTTP_STATUS)"
    
    # ファイルサイズを確認
    if [ -f /tmp/book_cover_1.jpg ]; then
        FILE_SIZE=$(stat -c%s /tmp/book_cover_1.jpg 2>/dev/null || stat -f%z /tmp/book_cover_1.jpg 2>/dev/null)
        echo "📷 ファイルサイズ: $FILE_SIZE bytes"
        echo "💾 保存先: /tmp/book_cover_1.jpg"
        
        # ファイルタイプを確認
        FILE_TYPE=$(file /tmp/book_cover_1.jpg 2>/dev/null | sed 's/^[^:]*: *//')
        if [ ! -z "$FILE_TYPE" ]; then
            echo "📄 ファイルタイプ: $FILE_TYPE"
        fi
        
        # クリーンアップ
        rm -f /tmp/book_cover_1.jpg
    fi
else
    echo "❌ 画像取得失敗 (HTTP $HTTP_STATUS)"
    if [ "$HTTP_STATUS" == "404" ]; then
        echo "   画像ファイルが存在しません"
        echo "   配置先: projects/sdd/bookstore/berry-books-api-sdd/src/main/webapp/resources/images/covers/"
        echo "   ファイル名: 書籍タイトル.jpg (例: Java_SEディープダイブ.jpg)"
    fi
fi
echo ""
echo ""

# ===========================================
# 2. 複数の書籍画像を連続取得
# ===========================================
echo "2️⃣  複数の書籍画像を連続取得"
echo "-------------------------------------------"

SUCCESS_COUNT=0
NOT_FOUND_COUNT=0

for BOOK_ID in 1 2 3 5 10 15 20 25 30; do
    IMAGE_CHECK=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
      "$API_BASE/api/images/covers/$BOOK_ID" \
      -o /dev/null)
    
    HTTP_STATUS=$(echo "$IMAGE_CHECK" | grep "HTTP_STATUS:" | sed 's/.*HTTP_STATUS://')
    
    if [ "$HTTP_STATUS" == "200" ]; then
        echo "  📷 Book ID $BOOK_ID: ✅ 画像あり"
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
    elif [ "$HTTP_STATUS" == "404" ]; then
        echo "  📷 Book ID $BOOK_ID: ❌ 画像なし (404)"
        NOT_FOUND_COUNT=$((NOT_FOUND_COUNT + 1))
    else
        echo "  📷 Book ID $BOOK_ID: ⚠️  HTTP $HTTP_STATUS"
    fi
done

echo ""
echo "📊 結果サマリー:"
echo "   画像あり: $SUCCESS_COUNT 件"
echo "   画像なし: $NOT_FOUND_COUNT 件"
echo ""
echo ""

# ===========================================
# 3. 存在しない書籍IDでのテスト
# ===========================================
echo "3️⃣  存在しない書籍IDでのアクセステスト"
echo "-------------------------------------------"
echo "   Book ID: 99999（404エラーが正常）"
echo ""

NOT_FOUND=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  "$API_BASE/api/images/covers/99999" \
  -o /dev/null)

HTTP_STATUS=$(echo "$NOT_FOUND" | grep "HTTP_STATUS:" | sed 's/.*HTTP_STATUS://')

if [ "$HTTP_STATUS" == "404" ]; then
    echo "✅ 正常：404エラーが発生しました"
else
    echo "⚠️  予期しないステータス: HTTP $HTTP_STATUS"
fi
echo ""
echo ""

# ===========================================
# 4. ダウンロードテスト
# ===========================================
echo "4️⃣  画像ダウンロードテスト"
echo "-------------------------------------------"

DOWNLOAD_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}\nCONTENT_TYPE:%{content_type}" \
  "$API_BASE/api/images/covers/1" \
  -o /tmp/test_book_cover.jpg)

HTTP_STATUS=$(echo "$DOWNLOAD_RESPONSE" | grep "HTTP_STATUS:" | sed 's/.*HTTP_STATUS://')
CONTENT_TYPE=$(echo "$DOWNLOAD_RESPONSE" | grep "CONTENT_TYPE:" | sed 's/.*CONTENT_TYPE://')

if [ "$HTTP_STATUS" == "200" ]; then
    echo "✅ ダウンロード成功 (HTTP $HTTP_STATUS)"
    echo "   Content-Type: $CONTENT_TYPE"
    
    if [ -f /tmp/test_book_cover.jpg ]; then
        FILE_SIZE=$(stat -c%s /tmp/test_book_cover.jpg 2>/dev/null || stat -f%z /tmp/test_book_cover.jpg 2>/dev/null)
        echo "   ファイルサイズ: $FILE_SIZE bytes"
        echo "   保存先: /tmp/test_book_cover.jpg"
        
        # クリーンアップ
        rm -f /tmp/test_book_cover.jpg
    fi
else
    echo "❌ ダウンロード失敗 (HTTP $HTTP_STATUS)"
    echo ""
    echo "💡 画像ファイルの配置方法:"
    echo "   1. 配置先ディレクトリ:"
    echo "      projects/sdd/bookstore/berry-books-api-sdd/src/main/webapp/resources/images/covers/"
    echo ""
    echo "   2. ファイル名の規則:"
    echo "      書籍のタイトルをそのままファイル名にする（スペースはアンダースコアに変換）"
    echo "      例: Java_SEディープダイブ.jpg, Jakarta_EEによるアーキテクチャ設計.jpg"
    echo ""
    echo "   3. 推奨仕様:"
    echo "      - 形式: JPG"
    echo "      - サイズ: 幅400px以上"
    echo "      - アスペクト比: 2:3 または 3:4"
    echo "      - ファイルサイズ: 500KB以下"
fi
echo ""

echo "========================================="
echo "✨ 画像APIテスト完了"
echo "========================================="

