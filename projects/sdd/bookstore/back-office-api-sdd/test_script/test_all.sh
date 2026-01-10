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
echo "🌐 API Base URL: http://localhost:8080/back-office-api-sdd"
echo ""
echo "⚠️  事前確認:"
echo "   1. HSQLDBサーバーが起動していますか？"
echo "      ./gradlew startHsqldb"
echo ""
echo "   2. Payara Serverが起動していますか？"
echo "      ./gradlew startPayara"
echo ""
echo "   3. Back Office APIがデプロイされていますか？"
echo "      ./gradlew :back-office-api-sdd:setupHsqldb"
echo "      ./gradlew :back-office-api-sdd:deploy"
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
# 3. カテゴリAPI テスト
# ===========================================
echo ""
echo "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓"
echo "┃  3. カテゴリAPI テスト                  ┃"
echo "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛"
echo ""

# カテゴリAPIは書籍APIのテストに含まれているため、スキップメッセージを表示
print_info "カテゴリAPIは書籍APIテストに含まれています"
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
# 4. 出版社API テスト
# ===========================================
echo ""
echo "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓"
echo "┃  4. 出版社API テスト                    ┃"
echo "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛"
echo ""

if [ -f "$SCRIPT_DIR/test_publishers.sh" ]; then
    bash "$SCRIPT_DIR/test_publishers.sh"
else
    echo "❌ test_publishers.sh が見つかりません"
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
# 5. 在庫API テスト
# ===========================================
echo ""
echo "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓"
echo "┃  5. 在庫API テスト                      ┃"
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
# 6. ワークフローAPI テスト
# ===========================================
echo ""
echo "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓"
echo "┃  6. ワークフローAPI テスト              ┃"
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
echo "   1. 認証API (ログイン, ログアウト, 現在のユーザー情報)"
echo "   2. 書籍API (一覧, 詳細, 検索, カテゴリ)"
echo "   3. カテゴリAPI (一覧)"
echo "   4. 出版社API (一覧)"
echo "   5. 在庫API (一覧, 詳細, 更新, 楽観ロック)"
echo "   6. ワークフローAPI (作成, 更新, 申請, 承認, 却下, 履歴)"
echo ""
echo "💡 個別にテストを実行する場合:"
echo "   bash test_authen.sh"
echo "   bash test_books.sh"
echo "   bash test_publishers.sh"
echo "   bash test_stocks.sh"
echo "   bash test_workflow.sh"
echo ""
echo "💡 簡易テスト（Windows Git Bash対応）:"
echo "   bash simple_test.sh"
echo ""
echo "==========================================="

