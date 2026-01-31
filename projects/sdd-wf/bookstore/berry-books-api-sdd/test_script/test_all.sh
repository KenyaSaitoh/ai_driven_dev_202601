#!/bin/bash
# ===========================================
# 全API統合テストスクリプト
# ===========================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "==========================================="
echo "  Berry Books API - 統合テスト"
echo "==========================================="
echo ""
echo "📍 テストスクリプトディレクトリ: $SCRIPT_DIR"
echo "🌐 API Base URL: http://localhost:8080/berry-books-api-sdd"
echo ""
echo "⚠️  事前確認:"
echo "   1. HSQLDBサーバーが起動していますか？"
echo "      ./gradlew startHsqldb"
echo ""
echo "   2. Payara Serverが起動していますか？"
echo "      ./gradlew startPayara"
echo ""
echo "   3. Berry Books APIがデプロイされていますか？"
echo "      ./gradlew :berry-books-api-sdd:deploy"
echo ""
echo "   4. Customer APIがデプロイされていますか？"
echo "      ./gradlew :customer-api:deploy"
echo ""
read -p "全てのサービスが起動していますか？ (y/n): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "テストを中止します。サービスを起動してから再実行してください。"
    exit 1
fi

echo ""
echo "==========================================="
echo ""

# ===========================================
# 1. 認証API テスト
# ===========================================
echo ""
echo "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓"
echo "┃  1. 認証API テスト                      ┃"
echo "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛"
echo ""

if [ -f "$SCRIPT_DIR/test_authen.sh" ]; then
    bash "$SCRIPT_DIR/test_authen.sh"
else
    echo "❌ test_authen.sh が見つかりません"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
read -p "次のテストに進みますか？ (y/n): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "テストを終了します"
    exit 0
fi

# ===========================================
# 2. 書籍API テスト
# ===========================================
echo ""
echo "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓"
echo "┃  2. 書籍API テスト                      ┃"
echo "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛"
echo ""

if [ -f "$SCRIPT_DIR/test_books.sh" ]; then
    bash "$SCRIPT_DIR/test_books.sh"
else
    echo "❌ test_books.sh が見つかりません"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
read -p "次のテストに進みますか？ (y/n): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "テストを終了します"
    exit 0
fi

# ===========================================
# 3. 注文API テスト
# ===========================================
echo ""
echo "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓"
echo "┃  3. 注文API テスト                      ┃"
echo "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛"
echo ""

if [ -f "$SCRIPT_DIR/test_orders.sh" ]; then
    bash "$SCRIPT_DIR/test_orders.sh"
else
    echo "❌ test_orders.sh が見つかりません"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
read -p "次のテストに進みますか？ (y/n): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "テストを終了します"
    exit 0
fi

# ===========================================
# 4. 画像API テスト
# ===========================================
echo ""
echo "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓"
echo "┃  4. 画像API テスト                      ┃"
echo "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛"
echo ""

if [ -f "$SCRIPT_DIR/test_images.sh" ]; then
    bash "$SCRIPT_DIR/test_images.sh"
else
    echo "❌ test_images.sh が見つかりません"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# ===========================================
# テスト完了
# ===========================================
echo ""
echo "==========================================="
echo "  ✨ 全APIテスト完了 ✨"
echo "==========================================="
echo ""
echo "📊 実行したテスト:"
echo "   1. 認証API (login, register, logout, me)"
echo "   2. 書籍API (一覧, 詳細, フィルタ)"
echo "   3. 注文API (作成, 履歴, 詳細)"
echo "   4. 画像API (表紙画像取得)"
echo ""
echo "💡 個別にテストを実行する場合:"
echo "   bash test_authen.sh"
echo "   bash test_books.sh"
echo "   bash test_orders.sh"
echo "   bash test_images.sh"
echo ""
echo "==========================================="

