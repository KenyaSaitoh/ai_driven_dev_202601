@echo off
REM sddプロジェクト ソースフォルダクリーンアップスクリプト (Windows版)
REM フォルダ構造は維持し、ソースファイルのみを削除します

setlocal enabledelayedexpansion

echo ======================================
echo sddプロジェクト ソースクリーンアップ
echo ======================================
echo.
echo 警告: このスクリプトは以下を削除します：
echo   - src配下の全ソースファイル（フォルダは残す）
echo   - bin, build, logs フォルダ
echo.
set /p confirm="続行しますか？ (yes/no): "

if not "%confirm%"=="yes" (
    echo キャンセルされました。
    exit /b 0
)

echo.

REM スクリプトのディレクトリを取得
set SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"

call :cleanup_project "bookstore\back-office-api-sdd" "back-office-api-sdd"
call :cleanup_project "bookstore\berry-books-api-sdd" "berry-books-api-sdd"
call :cleanup_project "person\jsf-person-sdd" "jsf-person-sdd"

echo ======================================
echo クリーンアップ完了
echo ======================================
echo.
echo 確認方法:
echo   cd bookstore\back-office-api-sdd
echo   dir src\main\
echo   dir src\main\java /s /b
echo.
echo フォルダ構造は残り、ソースファイルのみが削除されました。

exit /b 0

:cleanup_project
set project_path=%~1
set project_name=%~2

echo ----------------------------------------
echo クリーンアップ中: %project_name%
echo ----------------------------------------

cd /d "%SCRIPT_DIR%\%project_path%"

REM srcフォルダ配下のファイルを削除（フォルダは残す）
echo   - src\main\java のファイルを削除中...
if exist "src\main\java" (
    del /s /q "src\main\java\*.*" >nul 2>&1
    for /d /r "src\main\java" %%d in (*) do @rd "%%d" 2>nul
)

echo   - src\main\resources のファイルを削除中...
if exist "src\main\resources" (
    del /s /q "src\main\resources\*.*" >nul 2>&1
    for /d /r "src\main\resources" %%d in (*) do @rd "%%d" 2>nul
)

echo   - src\main\webapp のファイルを削除中...
if exist "src\main\webapp" (
    del /s /q "src\main\webapp\*.*" >nul 2>&1
    for /d /r "src\main\webapp" %%d in (*) do @rd "%%d" 2>nul
)

echo   - src\test\java のファイルを削除中...
if exist "src\test\java" (
    del /s /q "src\test\java\*.*" >nul 2>&1
    for /d /r "src\test\java" %%d in (*) do @rd "%%d" 2>nul
)

echo   - src\test\resources のファイルを削除中...
if exist "src\test\resources" (
    del /s /q "src\test\resources\*.*" >nul 2>&1
    for /d /r "src\test\resources" %%d in (*) do @rd "%%d" 2>nul
)

REM ビルド生成物を削除
echo   - bin フォルダを削除中...
if exist "bin" rd /s /q "bin" >nul 2>&1

echo   - build フォルダを削除中...
if exist "build" rd /s /q "build" >nul 2>&1

echo   - logs フォルダを削除中...
if exist "logs" rd /s /q "logs" >nul 2>&1

echo   ✓ %project_name% のクリーンアップ完了
echo.

cd /d "%SCRIPT_DIR%"
exit /b 0
