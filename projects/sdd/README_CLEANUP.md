# sddプロジェクト クリーンアップガイド

## 概要

sddプロジェクト（bookstore、person）のソースコードをクリーンアップするためのGradleタスクとシェルスクリプトを提供します。

## Gradleタスク

各プロジェクトの `build.gradle` に以下のカスタムタスクが定義されています：

### cleanupSources タスク

- **目的**: srcフォルダ配下のソースファイルを削除（フォルダ構造は維持）
- **グループ**: cleanup
- **削除対象**:
  - `src/main/java/**/*` (javaフォルダ自体は残る)
  - `src/main/resources/**/*` (resourcesフォルダ自体は残る)
  - `src/main/webapp/**/*` (webappフォルダ自体は残る)
  - `src/test/java/**/*` (javaフォルダ自体は残る)
  - `src/test/resources/**/*` (resourcesフォルダ自体は残る)
  - `bin/` (完全削除)
  - `logs/` (完全削除)

### cleanAll タスク

- **目的**: ビルド成果物とソースファイルを全て削除
- **グループ**: cleanup
- **実行内容**: `clean` タスクと `cleanupSources` タスクの両方を実行

## 使用方法

### 方法1: Gradleコマンドで個別実行

各プロジェクトに移動して、Gradleタスクを実行します。

```bash
# back-office-api-sdd のクリーンアップ
cd projects/sdd/bookstore/back-office-api-sdd
gradle cleanupSources

# または完全クリーンアップ（build フォルダも削除）
gradle cleanAll
```

```bash
# berry-books-api-sdd のクリーンアップ
cd projects/sdd/bookstore/berry-books-api-sdd
gradle cleanupSources
```

```bash
# jsf-person-sdd のクリーンアップ
cd projects/sdd/person/jsf-person-sdd
gradle cleanupSources
```

### 方法2: シェルスクリプトで一括実行

全プロジェクトを一度にクリーンアップする場合：

```bash
# Linux/Mac/Git Bash
cd projects/sdd
bash cleanup_all_sources.sh
```

```cmd
# Windows コマンドプロンプト
cd projects\sdd
cleanup_all_sources.bat
```

### 方法3: Gradle Wrapperを使う（推奨）

Gradle Wrapperを使用すると、Gradleがインストールされていない環境でも実行できます：

```bash
# Linux/Mac/Git Bash
cd projects/sdd/bookstore/back-office-api-sdd
./gradlew cleanupSources
```

```cmd
# Windows
cd projects\sdd\bookstore\back-office-api-sdd
gradlew.bat cleanupSources
```

## 確認方法

クリーンアップ後、フォルダ構造が維持されていることを確認：

```bash
# フォルダが残っていることを確認
ls -la src/main/
# → java, resources, webapp フォルダが存在するはず

# フォルダが空であることを確認
find src/main/java -type f
# → 何も表示されない（空）
```

## タスク一覧の確認

各プロジェクトで利用可能なタスクを確認：

```bash
cd projects/sdd/bookstore/back-office-api-sdd
gradle tasks --group cleanup
```

出力例：
```
Cleanup tasks
-------------
cleanAll - ビルド成果物とソースファイルを全て削除
cleanupSources - srcフォルダ配下のソースファイルを削除（フォルダ構造は維持）
```

## 注意事項

⚠️ **警告**: クリーンアップタスクを実行すると、ソースファイルが削除されます。

- **実行前に必ずバックアップを取るか、Gitでコミットしてください**
- 削除されたファイルは復元できません
- specs、sql、tasks、test_script などの設計ドキュメントは削除されません

## 復元方法

クリーンアップ後、ソースを再生成する場合：

1. **仕様書を確認**: 各プロジェクトの `specs/baseline/` フォルダ内の仕様書を参照
2. **タスクファイルを確認**: 各プロジェクトの `tasks/` フォルダ内のタスクファイルを参照
3. **AIツールで生成**: Cursor/Claude に仕様書とタスクファイルを渡して実装を自動生成

例：
```bash
# タスクファイルの場所
bookstore/back-office-api-sdd/tasks/tasks.md
bookstore/berry-books-api-sdd/tasks/tasks.md
person/jsf-person-sdd/tasks/tasks.md
```

## トラブルシューティング

### Gradleがインストールされていない

Gradle Wrapperを使用してください：
```bash
./gradlew cleanupSources  # Linux/Mac
gradlew.bat cleanupSources  # Windows
```

### 権限エラーが発生する

Linux/Mac の場合、実行権限を付与：
```bash
chmod +x gradlew
chmod +x cleanup_all_sources.sh
```

### Windows でスクリプトが実行できない

Git Bash を使用するか、バッチファイル（.bat）を使用してください：
```cmd
cleanup_all_sources.bat
```

## プロジェクト構造

```
projects/sdd/
├── bookstore/
│   ├── back-office-api-sdd/
│   │   ├── build.gradle          ← cleanupSources タスク定義
│   │   ├── src/
│   │   ├── specs/
│   │   └── tasks/
│   └── berry-books-api-sdd/
│       ├── build.gradle          ← cleanupSources タスク定義
│       ├── src/
│       ├── specs/
│       └── tasks/
├── person/
│   └── jsf-person-sdd/
│       ├── build.gradle          ← cleanupSources タスク定義
│       ├── src/
│       ├── specs/
│       └── tasks/
├── cleanup_all_sources.sh        ← 一括クリーンアップスクリプト
├── cleanup_all_sources.bat       ← Windows用
├── cleanup_sources.md            ← 詳細ドキュメント
└── README_CLEANUP.md             ← このファイル
```
