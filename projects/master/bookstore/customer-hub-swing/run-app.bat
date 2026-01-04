@echo off
REM Customer Hub Swing Application 実行スクリプト（Windows用）

echo =========================================
echo Customer Hub Swing Application
echo =========================================
echo.

REM カレントディレクトリを保存
set SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"

REM プロジェクトルートへ移動してビルド
echo 1. アプリケーションをビルド中...
cd ..\..\..
call gradlew.bat :customer-hub-swing:clean :customer-hub-swing:buildApp

if errorlevel 1 (
    echo エラー: ビルドに失敗しました
    pause
    exit /b 1
)

REM customer-hub-swingディレクトリに戻る
cd projects\master\bookstore\customer-hub-swing

echo.
echo =========================================
echo 2. アプリケーションを起動します...
echo =========================================
echo.

REM Javaがインストールされているか確認
where java >nul 2>nul
if errorlevel 1 (
    echo エラー: javaコマンドが見つかりません
    echo Java 8以降がインストールされているか確認してください
    pause
    exit /b 1
)

REM Javaバージョンを表示
java -version

echo.

REM アプリケーションを実行
java -jar build\libs\customer-hub-swing-1.0.0.jar

echo.
echo =========================================
echo アプリケーションを終了しました
echo =========================================
pause


