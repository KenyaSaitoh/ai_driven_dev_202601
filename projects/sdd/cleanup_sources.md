# ソースフォルダクリーンアップタスク

## 概要
sddプロジェクトのソースフォルダを初期状態にクリーンアップします。
フォルダ構造（java、resources、webappなど）は維持し、その配下のソースファイルのみを削除します。

## 対象プロジェクト
- `bookstore/back-office-api-sdd`
- `bookstore/berry-books-api-sdd`
- `person/jsf-person-sdd`

## クリーンアップ対象

### 各プロジェクト共通で削除するもの
1. **srcフォルダ配下のソースファイル**
   - `src/main/java/**/*.*` （javaフォルダは残す）
   - `src/main/resources/**/*.*` （resourcesフォルダは残す）
   - `src/main/webapp/**/*.*` （webappフォルダは残す）
   - `src/test/java/**/*.*` （javaフォルダは残す）
   - `src/test/resources/**/*.*` （resourcesフォルダは残す）

2. **ビルド生成物**
   - `bin/**`（完全削除）
   - `build/**`（完全削除）
   - `logs/**`（ログファイルの削除）

### 残すもの
- `src/main/java/`（空フォルダ）
- `src/main/resources/`（空フォルダ）
- `src/main/webapp/`（空フォルダ）
- `src/test/java/`（空フォルダ）
- `src/test/resources/`（空フォルダ）
- その他のフォルダ（specs、sql、tasks、test_script、imagesなど）

## 実行手順

### Gradleタスクを使う方法（推奨）

各プロジェクトのbuild.gradleに `cleanupSources` タスクと `cleanAll` タスクが定義されています。

#### 個別プロジェクトのクリーンアップ

```bash
# 1. back-office-api-sdd のクリーンアップ
cd projects/sdd/bookstore/back-office-api-sdd
gradle cleanupSources

# または、ビルド成果物も含めて完全クリーンアップ
gradle cleanAll

# 2. berry-books-api-sdd のクリーンアップ
cd ../berry-books-api-sdd
gradle cleanupSources

# 3. jsf-person-sdd のクリーンアップ
cd ../../person/jsf-person-sdd
gradle cleanupSources
```

#### Windows環境の場合

```cmd
cd projects\sdd\bookstore\back-office-api-sdd
gradlew.bat cleanupSources
```

### シェルスクリプトを使う方法

`cleanup_all_sources.sh` または `cleanup_all_sources.bat` スクリプトを使用：

```bash
# Linux/Mac/Git Bash
cd projects/sdd
bash cleanup_all_sources.sh

# Windows コマンドプロンプト
cd projects\sdd
cleanup_all_sources.bat
```

## 確認方法
クリーンアップ後、以下を確認：

```bash
# フォルダが残っていることを確認
ls -la src/main/
# java、resources、webapp フォルダが存在すること

# フォルダが空であることを確認
find src/main/java -type f
# 何も表示されないこと（空）
```

## 注意事項
- **実行前に必ずバックアップを取るか、Gitでコミットしておいてください**
- このタスクは元に戻せません
- ビルド生成物（bin、build）は完全に削除されます
- specs、sql、tasks、test_scriptなどの設計ドキュメントやタスクファイルは残ります

## 復元方法
クリーンアップ後、ソースを再生成する場合：
1. 各プロジェクトの `tasks/` フォルダ内のタスクファイルを参照
2. specs/baseline/ の仕様書を元に実装
3. AIツール（Cursor/Claude）にタスクファイルを渡して自動生成
