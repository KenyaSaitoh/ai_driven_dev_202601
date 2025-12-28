#!/bin/bash
# ===========================================
# 全API統合テストスクリプト
# ===========================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "==========================================="
echo "  Back Office API - 統合テスト"
echo "==========================================="
echo ""
echo "📍 テストスクリプトディレクトリ: $SCRIPT_DIR"
echo "🌐 API Base URL: http://localhost:8080/back-office-api"
echo ""
echo "⚠️  事前確認:"
echo "   1. HSQLDBサーバーが起動していますか？"
echo "      ./gradlew startHsqldb"
echo ""
echo "   2. Payara Serverが起動していますか？"
echo "      ./gradlew startPayara"
echo ""
echo "   3. Back Office APIがデプロイされていますか？"
echo "      ./gradlew :back-office-api:setupHsqldb"
echo "      ./gradlew :back-office-api:deploy"
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
# 1. 書籍API テスト
# ===========================================
echo ""
echo "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓"
echo "┃  1. 書籍API テスト                      ┃"
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
# 2. 在庫API テスト
# ===========================================
echo ""
echo "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓"
echo "┃  2. 在庫API テスト                      ┃"
echo "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛"
echo ""

if [ -f "$SCRIPT_DIR/test_stocks.sh" ]; then
    bash "$SCRIPT_DIR/test_stocks.sh"
else
    echo "❌ test_stocks.sh が見つかりません"
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
# 3. ワークフローAPI テスト
# ===========================================
echo ""
echo "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓"
echo "┃  3. ワークフローAPI テスト              ┃"
echo "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛"
echo ""

if [ -f "$SCRIPT_DIR/test_workflow.sh" ]; then
    bash "$SCRIPT_DIR/test_workflow.sh"
else
    echo "❌ test_workflow.sh が見つかりません"
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
echo "   1. 書籍API (一覧, 詳細, カテゴリ)"
echo "   2. 在庫API (一覧, 詳細, 更新, 楽観ロック)"
echo "   3. ワークフローAPI (作成, 申請, 承認, 却下, 履歴)"
echo ""
echo "💡 個別にテストを実行する場合:"
echo "   bash test_books.sh"
echo "   bash test_stocks.sh"
echo "   bash test_workflow.sh"
echo ""
echo "💡 簡易テスト（Windows Git Bash対応）:"
echo "   bash simple_test.sh"
echo ""
echo "==========================================="

