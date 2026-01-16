# sddプロジェクト クリーンアップガイド

## 概要

sddプロジェクト（bookstore、person）のソースコードをクリーンアップするためのGradleタスクです。
フォルダ構造（java、resources、webappなど）は維持したまま、ソースファイルのみを削除します。

## プロジェクト構成

全てのsddプロジェクトは、**プロジェクトルートの `build.gradle`** で一元管理されています。

```
ai_driven_dev_202601/
├── build.gradle                    ← sddタスクの定義
├── settings.gradle
└── projects/
    └── sdd/
        ├── bookstore/
        │   ├── back-office-api-sdd/  ← 個別build.gradle不要
        │   └── berry-books-api-sdd/  ← 個別build.gradle不要
        └── person/
            └── jsf-person-sdd/       ← 個別build.gradle不要
```

## 使用方法

### 重要: プロジェクトルートから実行してください

クリーンアップタスクは、**プロジェクトルート** (`ai_driven_dev_202601`) から実行する必要があります。

```bash
# プロジェクトルートに移動
cd ~/GitHubRepos/cline_training_2025/ai_driven_dev_202601

# または現在のディレクトリから相対パスで移動
cd ../../
```

### 全プロジェクト一括クリーンアップ

```bash
# プロジェクトルートから実行
./gradlew cleanupAllSddProjects
```

これにより以下のプロジェクトがクリーンアップされます：
- `back-office-api-sdd` (projects/sdd/bookstore/)
- `berry-books-api-sdd` (projects/sdd/bookstore/)
- `jsf-person-sdd` (projects/sdd/person/)

### 個別プロジェクトのクリーンアップ

```bash
# プロジェクトルートから実行

# back-office-api-sdd のみ
./gradlew :back-office-api-sdd:cleanSddArtifacts

# berry-books-api-sdd のみ
./gradlew :berry-books-api-sdd:cleanSddArtifacts

# jsf-person-sdd のみ
./gradlew :jsf-person-sdd:cleanSddArtifacts
```

### Windows の場合

```cmd
REM プロジェクトルートに移動
cd d:\GitHubRepos\cline_training_2025\ai_driven_dev_202601

REM 全プロジェクトをクリーンアップ
gradlew.bat cleanupAllSddProjects

REM 個別プロジェクト
gradlew.bat :back-office-api-sdd:cleanSddArtifacts
gradlew.bat :berry-books-api-sdd:cleanSddArtifacts
gradlew.bat :jsf-person-sdd:cleanSddArtifacts
```

## 削除されるもの

各プロジェクトで以下が削除されます：

### ソースファイルとパッケージ構造
- `src/main/java/` 配下の全パッケージとファイル（javaフォルダ自体は残る）
- `src/main/resources/` 配下の全ファイルとフォルダ（resourcesフォルダ自体は残る）
- `src/main/webapp/` 配下の全ファイルとフォルダ（webappフォルダ自体は残る）
- `src/test/java/` 配下の全パッケージとファイル（javaフォルダ自体は残る）
- `src/test/resources/` 配下の全ファイルとフォルダ（resourcesフォルダ自体は残る）

### タスクファイル
- `tasks/` フォルダ配下の全ファイル（tasksフォルダ自体は残る）

### ビルド成果物（完全削除）
- `bin/`
- `build/`
- `logs/`

## 残るもの

以下は削除されません：
- `src/main/java/`（空フォルダとして残る）
- `src/main/resources/`（空フォルダとして残る）
- `src/main/webapp/`（空フォルダとして残る）
- `src/test/java/`（空フォルダとして残る）
- `src/test/resources/`（空フォルダとして残る）
- `tasks/`（フォルダは残るが、中身のファイルは削除）
- `specs/`（仕様書フォルダと内容すべて）
- `sql/`（SQLスクリプトすべて）
- `test_script/`（テストスクリプト）
- `images/`（画像ファイル）
- `README.md`

## Gradleタスク一覧の確認

利用可能なタスクを確認：

```bash
# プロジェクトルートから実行
gradle tasks --group sdd
```

出力例：
```
SDD tasks
---------
cleanSddArtifacts - Clean SDD artifacts (src/ and sql/hsqldb/) while preserving directory structure
cleanupAllSddProjects - Clean all SDD projects (back-office-api-sdd, berry-books-api-sdd, jsf-person-sdd)
```

## 確認方法

クリーンアップ後、フォルダが空になっていることを確認：

```bash
# プロジェクトディレクトリに移動
cd projects/sdd/bookstore/back-office-api-sdd

# フォルダが残っていることを確認
ls -la src/main/
# → java, resources, webapp フォルダが存在

# フォルダが空であることを確認（.gitkeepのみ）
find src/main/java -type f
# → .gitkeep のみ表示されるか、何も表示されない（空）
```

## ⚠️ 注意事項

**警告**: このタスクはソースファイルとタスクファイルを削除します。

- **実行前に必ずバックアップを取るか、Gitでコミットしてください**
- 削除されたファイルは復元できません
- 仕様書（specs/）とSQLスクリプト（sql/）は残りますが、ソースコードとタスクファイルは完全に削除されます

## 復元方法

クリーンアップ後、ソースを再生成するには：

1. **仕様書を参照**: `specs/baseline/` フォルダ
2. **SQLスクリプトを参照**: `sql/` フォルダ
3. **AIツールで生成**: 仕様書とSQLスクリプトをCursor/Claudeに渡して実装を自動生成

## 対象プロジェクト

- `back-office-api-sdd` (projects/sdd/bookstore/)
- `berry-books-api-sdd` (projects/sdd/bookstore/)
- `jsf-person-sdd` (projects/sdd/person/)

**注意**: これらのプロジェクトは、プロジェクトルートの `build.gradle` で一元管理されています。個別のbuild.gradleファイルは不要です。

## トラブルシューティング

### `gradlew: command not found` エラー

**原因**: `projects/sdd` ディレクトリから実行しようとしています。

**解決方法**: プロジェクトルートに移動してください：

```bash
# 現在のディレクトリを確認
pwd

# projects/sdd にいる場合、プロジェクトルートに移動
cd ../../

# 正しいディレクトリにいることを確認
pwd
# → .../ai_driven_dev_202601 が表示されるはず

# タスクを実行
./gradlew cleanupAllSddProjects
```

### タスクが見つからない

プロジェクトルート（build.gradleがある場所）から実行していることを確認してください：

```bash
# タスク一覧を確認
./gradlew tasks --group sdd
```

出力例：
```
SDD tasks
---------
cleanSddArtifacts - Clean SDD artifacts
cleanupAllSddProjects - Clean all SDD projects
```

### Gradleがインストールされていない

プロジェクトのGradle Wrapperを使用してください（`./gradlew` または `gradlew.bat`）。
システムにGradleをインストールする必要はありません。
