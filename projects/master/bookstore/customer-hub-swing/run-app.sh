#!/bin/bash
# Customer Hub Swing Application 実行スクリプト
# 対応環境: Linux / Mac / Windows Git Bash

echo "========================================="
echo "Customer Hub Swing Application"
echo "========================================="
echo ""

# カレントディレクトリを取得
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# プロジェクトルートへ移動してビルド
echo "1. アプリケーションをビルド中..."
cd ../../..
./gradlew :customer-hub-swing:clean :customer-hub-swing:buildApp

if [ $? -ne 0 ]; then
    echo "エラー: ビルドに失敗しました"
    exit 1
fi

# customer-hub-swingディレクトリに戻る
cd projects/master/bookstore/customer-hub-swing

echo ""
echo "========================================="
echo "2. アプリケーションを起動します..."
echo "========================================="
echo ""

# Javaがインストールされているか確認
if ! command -v java &> /dev/null; then
    echo "エラー: javaコマンドが見つかりません"
    echo "Java 8以降がインストールされているか確認してください"
    exit 1
fi

# Javaバージョンを表示
java -version

echo ""

# アプリケーションを実行
java -jar build/libs/customer-hub-swing-1.0.0.jar

echo ""
echo "========================================="
echo "アプリケーションを終了しました"
echo "========================================="


