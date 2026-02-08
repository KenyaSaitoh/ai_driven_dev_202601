# 単体テスト実行評価インストラクション（アジャイル）

## パラメータ設定

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
target: "common"  # または "usecases/<usecase_folder>" 例: usecases/order-creation
build_script_path: null   # オプション（通常は不要）。マルチプロジェクト構成の場合のみ指定
                          # build.gradleファイルのパス（未指定時は project_root の build.gradle を使用）
                          # 例: "build.gradle" (リポジトリルート) または "d:/GitHubRepos/.../build.gradle" (絶対パス)
```

* 例: common の単体テストを実行
```yaml
project_root: "projects/sdd-agile/bookstore/berry-books-api"
target: "common"
```

* 例: ユースケース order-creation の単体テストを実行
```yaml
project_root: "projects/sdd-agile/bookstore/berry-books-api"
target: "usecases/order-creation"
```

注意: パス区切りはOS環境に応じて調整する。以降、`{project_root}`, `{target}` はパラメータで設定した値に置き換える。

---

## 概要

このインストラクションは、common または 指定ユースケース に紐づく単体テストを実行し、結果とカバレッジを分析してフィードバックレポートを生成するためのものである。

重要な方針
* target が "common" の場合: common タスクに紐づくテスト（共通実装のテスト）を実行する
* target が "usecases/{名}" の場合: そのユースケースに紐づくテストを実行する
* テスト実行: gradle test jacocoTestReport（プロジェクトのビルド設定に従う）
* 問題を発見してもユーザー確認なしに修正しない。推奨アクションを提示する

---

## 1. 参照SPEC（アジャイル）

* @agent_skills/jakarta-ee-api-agile/principles/ を確認する
* カバレッジ目標・テスト方針: {project_root}/specs/baseline/common/architecture_design.md を参照する
* target が common の場合: detailed_design/common/behaviors.md（存在する場合）を参照
* target が usecases/{名} の場合: detailed_design/usecases/{名}/behaviors.md, usecases/{名}/userstory.md を参照

---

## 2. 実行手順

1. テスト実行
   * 実行ディレクトリ: build_script_path が指定されていればその build.gradle ファイルのディレクトリ部分を使用、未指定なら project_root
   * コマンド例: `cd {build_script_path のディレクトリ部分 または project_root}` → `./gradlew test jacocoTestReport --stacktrace`
   * プロジェクトのビルド設定に従う
2. テスト結果とカバレッジの取得・分析
3. 問題の分類: テスト失敗、不足している振る舞い、デッドコード、設計の誤り
4. フィードバックレポートの生成とユーザーへの推奨アクション提示

target に応じて、実行するテストスコープを common 用パッケージ/クラスまたはユースケース用パッケージ/クラスに限定してよい（プロジェクトのテスト構成に依存）。

**マルチプロジェクト構成の考慮:**
* 通常は build_script_path の指定は不要です（project_root の build.gradle を自動使用）
* マルチプロジェクト構成の場合のみ、build_script_path パラメータでルートの build.gradle のパスを指定します（例: "build.gradle"）
* 指定されたパスからディレクトリ部分を抽出してそのディレクトリで Gradle コマンドを実行します

---

## 3. 注意事項

* 問題を発見してもユーザー確認なしに修正しない
* 必要に応じてコード生成（または common/usecases SPEC の見直し）に戻ってループする
* その他の方針は、ウォーターフォール版の unit_test_execution.md に準じる

---

## 参考

* [code_generation.md](code_generation.md) - コード生成
* ウォーターフォール版: @agent_skills/jakarta-ee-api-base/instructions/unit_test_execution.md
