---
name: playwright-e2e-test
description: WebアプリケーションのPlaywright E2Eテストコードをテストシナリオ定義書から自動生成。テストシナリオ定義書（Markdown形式）を読み込み、ページ遷移、要素操作、検証を含む高品質なテストコードを生成。TypeScript対応、Page Object Model推奨、ダイアログ処理・非同期処理・エラーハンドリング完備。
---

# Playwright E2Eテスト生成 Agent Skill

## 使い方

### Playwrightテストコード生成

```
@agent_skills/playwright-e2e-test/instructions/generate_playwright_tests.md

テストシナリオ定義書からPlaywrightテストコードを生成してください

パラメータ
* project_root: <プロジェクトルートパス>
* instructions_file: <テストシナリオ定義書のファイルパス>
* test_output_dir: <テストコード出力ディレクトリ（省略可、デフォルト: {project_root}/tests）>
* page_objects_dir: <Page Objectsディレクトリ（省略可、デフォルト: {project_root}/tests/pages）>
* use_page_objects: true  # Page Object Modelを使用するか（デフォルト: true）
```

AIが自動で以下を実行
1. テストシナリオ定義書（Markdown）を読み込み
2. 各シナリオを解析してテストケースを生成
3. Reactコンポーネントを解析してセレクタを自動推論
4. Page Object Model（POM）クラスを生成（use_page_objects=trueの場合）
5. テストコード（TypeScript）を生成
6. 設定ファイル（playwright.config.ts）を生成（存在しない場合）
7. README（テスト実行方法）を生成

**特徴**: 人が書くシナリオ定義書には日本語で要素名を記述するだけで、AIが適切なセレクタを自動推論します。

---

## 実践例

### 例1: テストシナリオ定義書からテストコード生成

```
@agent_skills/playwright-e2e-test/instructions/generate_playwright_tests.md

テストシナリオ定義書からPlaywrightテストコードを生成してください

パラメータ
* project_root: projects/master/bookstore/berry-books-spa
* instructions_file: projects/master/bookstore/berry-books-spa/playwright_berry-books.md
```

AIが自動で実行:
1. テストシナリオ定義書を読み込む
2. 各シナリオを解析
3. アプリケーションコードを解析してセレクタを自動推論
4. Page Objectsを生成
5. テストファイルを生成
6. 設定ファイルを生成

生成されるファイル:
```
{project_root}/
├── tests/
│   ├── pages/                     # Page Objects
│   │   ├── LoginPage.ts
│   │   ├── BookListPage.ts
│   │   └── ...
│   ├── scenario1.spec.ts          # シナリオ1のテスト
│   ├── scenario2.spec.ts          # シナリオ2のテスト
│   └── ...
├── playwright.config.ts           # Playwright設定
└── README_PLAYWRIGHT.md           # テスト実行方法
```

---

## 対応する主要機能

* テストシナリオ定義書（Markdown形式）の解析
* アプリケーションコード解析による自動セレクタ推論
* Page Object Model（POM）パターンの自動生成
* TypeScriptによるテストコード生成
* ページ遷移・要素操作・検証の実装
* ダイアログ処理（confirm/alert）- シンプルなaccept/dismissのみ推奨
* 非同期処理・待機処理（APIから読み込まれる選択肢への対応含む）
* エラーハンドリング
* スクリーンショット機能
* 並列実行対応

## 重要な注意事項

### セレクトボックスの選択肢が動的に読み込まれる場合

選択肢がAPIから非同期に読み込まれる場合、テストシナリオ定義書に「待機」ステップを含める必要があります：

```markdown
| 12 | 待機 | カテゴリ選択肢 | 読み込み完了を待つ | APIから取得 |
| 13 | 選択 | カテゴリ | 最初の選択肢（すべて以外） |  |
```

### ダイアログ処理はシンプルに

複雑なダイアログフロー（キャンセル→再試行など）は避け、シンプルなaccept/dismissのみを使用してください。

### バックグラウンド実行を避ける

生成される`playwright.config.ts`では`reuseExistingServer: true`を使用し、既存のサーバーを再利用します。

---

## ディレクトリ構造

```
agent_skills/playwright-e2e-test/
├── SKILL.md                          # このファイル
├── README.md                         # クイックスタートガイド
├── principles/                       # 原則（全プロジェクト共通）
│   └── playwright_best_practices.md # Playwrightベストプラクティス
├── templates/                        # テンプレート
│   └── playwright_instructions.md   # テストシナリオ定義書テンプレート
└── instructions/
    └── generate_playwright_tests.md  # テストコード生成指示
```

---

## 参考資料

* [Playwright 公式ドキュメント](https://playwright.dev/)
* [Page Object Model パターン](https://playwright.dev/docs/pom)
* [Playwrightベストプラクティス](principles/playwright_best_practices.md)
* [テストシナリオ定義書テンプレート](templates/playwright_instructions.md)
