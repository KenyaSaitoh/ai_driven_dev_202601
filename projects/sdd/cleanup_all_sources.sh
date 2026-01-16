#!/bin/bash

# sddプロジェクト ソースフォルダクリーンアップスクリプト
# フォルダ構造は維持し、ソースファイルのみを削除します

set -e  # エラーが発生したら停止

# スクリプトのディレクトリを取得
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "======================================"
echo "sddプロジェクト ソースクリーンアップ"
echo "======================================"
echo ""
echo "警告: このスクリプトは以下を削除します："
echo "  - src配下の全ソースファイル（フォルダは残す）"
echo "  - bin, build, logs フォルダ"
echo ""
read -p "続行しますか？ (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "キャンセルされました。"
    exit 0
fi

echo ""

# クリーンアップ関数
cleanup_project() {
    local project_path="$1"
    local project_name="$2"
    
    echo "----------------------------------------"
    echo "クリーンアップ中: $project_name"
    echo "----------------------------------------"
    
    cd "$project_path"
    
    # srcフォルダ配下のファイルを削除（フォルダは残す）
    echo "  - src/main/java のファイルを削除中..."
    if [ -d "src/main/java" ]; then
        find src/main/java -type f -delete 2>/dev/null || true
        # 空のディレクトリも削除（javaフォルダ自体は残す）
        find src/main/java -mindepth 1 -type d -empty -delete 2>/dev/null || true
    fi
    
    echo "  - src/main/resources のファイルを削除中..."
    if [ -d "src/main/resources" ]; then
        find src/main/resources -type f -delete 2>/dev/null || true
        find src/main/resources -mindepth 1 -type d -empty -delete 2>/dev/null || true
    fi
    
    echo "  - src/main/webapp のファイルを削除中..."
    if [ -d "src/main/webapp" ]; then
        find src/main/webapp -type f -delete 2>/dev/null || true
        find src/main/webapp -mindepth 1 -type d -empty -delete 2>/dev/null || true
    fi
    
    echo "  - src/test/java のファイルを削除中..."
    if [ -d "src/test/java" ]; then
        find src/test/java -type f -delete 2>/dev/null || true
        find src/test/java -mindepth 1 -type d -empty -delete 2>/dev/null || true
    fi
    
    echo "  - src/test/resources のファイルを削除中..."
    if [ -d "src/test/resources" ]; then
        find src/test/resources -type f -delete 2>/dev/null || true
        find src/test/resources -mindepth 1 -type d -empty -delete 2>/dev/null || true
    fi
    
    # ビルド生成物を削除
    echo "  - bin フォルダを削除中..."
    rm -rf bin
    
    echo "  - build フォルダを削除中..."
    rm -rf build
    
    echo "  - logs フォルダを削除中..."
    rm -rf logs
    
    echo "  ✓ $project_name のクリーンアップ完了"
    echo ""
    
    cd "$SCRIPT_DIR"
}

# 各プロジェクトをクリーンアップ
cleanup_project "$SCRIPT_DIR/bookstore/back-office-api-sdd" "back-office-api-sdd"
cleanup_project "$SCRIPT_DIR/bookstore/berry-books-api-sdd" "berry-books-api-sdd"
cleanup_project "$SCRIPT_DIR/person/jsf-person-sdd" "jsf-person-sdd"

echo "======================================"
echo "クリーンアップ完了"
echo "======================================"
echo ""
echo "確認方法:"
echo "  cd bookstore/back-office-api-sdd"
echo "  ls -la src/main/"
echo "  find src/main/java -type f"
echo ""
echo "フォルダ構造は残り、ソースファイルのみが削除されました。"
