# 単体テスト実行評価インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

### 必須パラメータ

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
target_type: "FUNC_XXX_xxx"
```

### オプショナルパラメータ

```yaml
# カバレッジ目標（未指定時は architecture_design.md → デフォルト値）
coverage_targets:
  line: null      # 行カバレッジ目標 (0-100)、デフォルト: 80
  branch: null    # 分岐カバレッジ目標 (0-100)、デフォルト: 70
  method: null    # メソッドカバレッジ目標 (0-100)、デフォルト: 85

# テスト実行設定
test_scope: "target"        # target: 対象機能のみ / all: 全テスト / affected: 影響範囲
test_timeout: 600           # テスト実行タイムアウト（秒）、デフォルト: 600
parallel_execution: true    # 並列実行の有効化、デフォルト: true

# 品質ゲート設定
failure_handling: "report"  # report: レポート生成して継続 / stop: 即座に停止
dead_code_policy: "warn"    # warn: 警告のみ / error: エラー扱い / ignore: 無視
min_test_count: null        # 最小テスト数（未指定ならチェックなし）

# カバレッジ除外設定（デフォルト: DTO、Record、自動生成コード）
coverage_exclusions:
  - "**/dto/**"
  - "**/*Dto.java"
  - "**/*Record.java"
  - "**/generated/**"

# レポート出力設定
report_formats:
  html: true                # HTML形式（人間向け）、デフォルト: true
  json: true                # JSON形式（AI向け）、デフォルト: true
  xml: false                # XML形式、デフォルト: false

# カスタムパス（通常は不要、Gradleの標準パスを使用）
test_results_dir: null
jacoco_xml: null
jacoco_html_dir: null
jacoco_json: null
```

### パラメータ例

例1: 最小限
```yaml
project_root: "projects/sdd/bookstore/back-office-api-sdd"
target_type: "FUNC_002_books"
```

例2: カバレッジ目標カスタマイズ
```yaml
project_root: "projects/sdd/bookstore/back-office-api-sdd"
target_type: "FUNC_002_books"
coverage_targets:
  line: 85
  branch: 75
  method: 90
```

例3: 厳格モード
```yaml
project_root: "projects/sdd/bookstore/back-office-api-sdd"
target_type: "FUNC_002_books"
failure_handling: "stop"
dead_code_policy: "error"
min_test_count: 10
```

注意
* パス区切りはOS環境に応じて調整する（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換える
* 以降、`{target_type}` と表記されている箇所は、上記で設定した値に置き換える

---

## 重要原則: ユーザー確認なしに変更しない

このインストラクションでは、以下の原則を厳守する：

1. テスト実行と結果分析のみを行う
2. 問題を発見した場合は、必ずユーザーに提案を提示する
3. ユーザーの選択なしに、コードや設計書を修正しない
4. フィードバックは具体的かつアクション可能な形で提示する

---

## 1. SPECの読み込みと理解

### 1.1 Agent Skillsルール（最優先で確認）

* @agent_skills/jakarta-ee-api-base/principles/ - Jakarta EE開発の原則、アーキテクチャ標準、品質基準、セキュリティ標準を確認する
  * このフォルダ配下の原則ドキュメントを読み込み、共通ルールを遵守すること
  * 重要: 単体テスト実行・評価においても、ルールドキュメントに記載されたすべてのルールを遵守すること
  * 注意: Agent Skills配下のルールは全プロジェクト共通。プロジェクト固有のルールがある場合は `{project_root}/principles/` も確認すること

---

## 2. 実行フロー

### ステップ1: 設定値の確定

#### 1-1. カバレッジ目標の決定

優先順位に従って値を確定:

1. パラメータで指定されているか確認
2. 指定なし → `{project_root}/specs/baseline/basic_design/architecture_design.md` を読み込む
   ```markdown
   ## テスト戦略
   
   ### カバレッジ目標
   - 行カバレッジ: 80%以上
   - 分岐カバレッジ: 70%以上
   - メソッドカバレッジ: 85%以上
   ```
3. architecture_design.md にも記載なし → デフォルト値を使用
   - 行: 80%
   - 分岐: 70%
   - メソッド: 85%

確定した値をユーザーに表示:
```
📊 テスト実行設定:
- 対象: FUNC_002_books
- カバレッジ目標:
  - 行カバレッジ: 80% (from: architecture_design.md)
  - 分岐カバレッジ: 70% (from: architecture_design.md)
  - メソッドカバレッジ: 85% (from: default)
- テスト範囲: target
- 除外パターン: **/dto/**, **/*Record.java, **/generated/**
```

#### 1-2. レポートパスの確定

デフォルトパス（Gradle標準）:
```
{project_root}/build/
├── test-results/test/          # JUnit XMLレポート
│   └── TEST-*.xml
└── reports/
    ├── tests/test/              # HTMLテストレポート
    │   └── index.html
    └── jacoco/test/             # JaCoCoカバレッジレポート
        ├── html/index.html      # HTML版
        ├── jacocoTestReport.xml # XML版
        └── jacocoTestReport.json # JSON版（AI向け）
```

---

### ステップ2: テスト実行

#### 2-1. Gradle タスクの実行

テスト実行コマンド:
```bash
cd {project_root}
./gradlew test jacocoTestReport --stacktrace
```

Windows の場合:
```bash
cd {project_root}
gradlew.bat test jacocoTestReport --stacktrace
```

オプション:
* `test_scope: "target"` の場合: `--tests "*{target_type}*Test"`
* `parallel_execution: true` の場合: `--parallel`
* `test_timeout` の場合: タイムアウト監視

実行中のログを監視し、以下を確認:
* テスト開始
* 進行状況
* 失敗の発生
* 完了

#### 2-2. 実行結果の確認

実行完了後、以下を確認:
* 終了コード（0: 成功、1以上: 失敗）
* ビルドログの出力
* レポートファイルの生成

---

### ステップ3: レポート読み込みと解析

#### 3-1. テスト結果XMLの読み込み

テスト結果XMLファイルを読み込む:
```
{project_root}/build/test-results/test/TEST-*.xml
```

各XMLファイルから以下を抽出:
```xml
<testsuite name="com.example.service.BookServiceTest" 
           tests="10" 
           failures="2" 
           errors="0" 
           skipped="0" 
           time="1.234">
  <testcase name="testSearchBooks_WithKeyword" 
            classname="com.example.service.BookServiceTest" 
            time="0.123">
    <failure message="expected: 3 but was: 2" type="org.junit.AssertionError">
      org.junit.AssertionError: expected: 3 but was: 2
          at com.example.service.BookServiceTest.testSearchBooks_WithKeyword(BookServiceTest.java:45)
    </failure>
  </testcase>
</testsuite>
```

#### 3-2. JaCoCoカバレッジXMLの読み込み

カバレッジXMLファイルを読み込む:
```
{project_root}/build/reports/jacoco/test/jacocoTestReport.xml
```

カバレッジデータを抽出:
```xml
<report name="JaCoCo Coverage Report">
  <package name="com/example/service">
    <class name="com/example/service/BookService" 
           sourcefilename="BookService.java">
      <method name="searchBooks" desc="(Ljava/lang/String;)Ljava/util/List;">
        <counter type="INSTRUCTION" missed="10" covered="40"/>
        <counter type="LINE" missed="3" covered="12"/>
        <counter type="BRANCH" missed="2" covered="4"/>
      </method>
      <counter type="LINE" missed="15" covered="35"/>
      <counter type="BRANCH" missed="4" covered="8"/>
      <counter type="METHOD" missed="1" covered="5"/>
    </class>
  </package>
  <counter type="LINE" missed="15" covered="35"/>
  <counter type="BRANCH" missed="4" covered="8"/>
</report>
```

カバレッジ計算:
* 行カバレッジ: covered / (covered + missed) * 100
* 分岐カバレッジ: covered / (covered + missed) * 100
* メソッドカバレッジ: covered / (covered + missed) * 100

#### 3-3. JaCoCoカバレッジHTMLの読み込み（未カバー行の特定）

HTMLレポートから未カバー行を特定:
```
{project_root}/build/reports/jacoco/test/html/com.example.service/BookService.html
```

HTMLを解析して以下を抽出:
* `<span class="nc" id="L45">`: 未カバー行（赤）
* `<span class="pc" id="L78">`: 部分的カバー行（黄）

未カバーコードの例:
```
L45-L48: 未カバー
  if (book == null) {
      throw new NotFoundException("Book not found: " + id);
  }

L78-L82: 未カバー
  if (!isValid(keyword)) {
      throw new ValidationException("Invalid keyword");
  }
```

#### 3-4. JSON形式でのデータ統合（AI向け）

すべてのデータを統合してJSON形式で整理:
```json
{
  "test_execution": {
    "timestamp": "2025-01-18T12:34:56Z",
    "project_root": "projects/sdd/bookstore/back-office-api-sdd",
    "target_type": "FUNC_002_books",
    "duration_seconds": 12.5
  },
  "test_results": {
    "total": 18,
    "passed": 15,
    "failed": 3,
    "skipped": 0,
    "failures": [
      {
        "class": "com.example.service.BookServiceTest",
        "method": "testSearchBooks_WithKeyword",
        "line": 45,
        "message": "expected: 3 but was: 2",
        "type": "org.junit.AssertionError",
        "stacktrace": "..."
      }
    ]
  },
  "coverage": {
    "line": {
      "covered": 35,
      "missed": 15,
      "total": 50,
      "percentage": 70.0,
      "target": 80.0,
      "status": "BELOW_TARGET"
    },
    "branch": {
      "covered": 8,
      "missed": 4,
      "total": 12,
      "percentage": 66.7,
      "target": 70.0,
      "status": "BELOW_TARGET"
    },
    "method": {
      "covered": 5,
      "missed": 1,
      "total": 6,
      "percentage": 83.3,
      "target": 85.0,
      "status": "BELOW_TARGET"
    }
  },
  "uncovered_code": [
    {
      "class": "com.example.service.BookService",
      "lines": "45-48",
      "code_snippet": "if (book == null) {\n    throw new NotFoundException(...);\n}",
      "analysis": {
        "type": "exception_handling",
        "category": "necessary_behavior",
        "confidence": "high"
      }
    },
    {
      "class": "com.example.service.BookService",
      "lines": "120-125",
      "code_snippet": "} else {\n    // Redundant null check\n}",
      "analysis": {
        "type": "redundant_condition",
        "category": "dead_code",
        "confidence": "high"
      }
    }
  ]
}
```

---

### ステップ4: 結果分析と問題の分類

#### 4-1. テスト失敗の分析

失敗したテストを分類:

パターンA: アサーション失敗（期待値と実際値の不一致）
```
❌ testSearchBooks_WithKeyword
原因: expected: 3 but was: 2
→ 設計 or 実装 or テストのいずれかに誤りがある
```

パターンB: 例外発生
```
❌ testGetBook_NotFound
原因: NullPointerException
→ 実装の誤り（nullチェック漏れ）
```

パターンC: タイムアウト
```
❌ testSearchBooks_Performance
原因: Test timed out after 5000ms
→ パフォーマンス問題
```

#### 4-2. カバレッジギャップの分析

未カバーコードを3つのカテゴリに分類:

カテゴリ1: 必要な振る舞い（テストが不足）
* 例外ハンドリング
* 境界値条件
* エラーケース
* バリデーション

判定基準:
* ビジネスロジックの一部である
* 実際に実行される可能性がある
* 設計書で言及されている

→ behaviors.md に追加すべき

カテゴリ2: デッドコード（到達不可能・冗長）
* 到達不可能な分岐
* 冗長なnullチェック
* 使用されていない条件

判定基準:
* ロジック的に到達不可能
* 先行する条件で既にチェック済み
* 過剰な防御的コード

→ detailed_design.md に明記 + コード削除を提案

カテゴリ3: 設計の誤り
* 不要な複雑性
* 過剰な条件分岐
* アーキテクチャ違反

判定基準:
* 設計書に記載がない
* ビジネス要件に合致しない
* アーキテクチャ標準に反する

→ detailed_design.md を修正すべき

#### 4-3. behaviors.md との整合性確認

behaviors.md を読み込み、以下を確認:
```
{project_root}/specs/baseline/detailed_design/{target_type}/behaviors.md
```

比較:
* behaviors.md に記載されているが、テストが実装されていない
* behaviors.md に記載がないが、テストが実装されている
* behaviors.md に記載がなく、カバレッジも不足している

---

### ステップ5: フィードバックレポート生成

#### 5-1. サマリーの生成

全体サマリーを生成:

```
┌─────────────────────────────────────────────────────────┐
│ 📊 単体テスト実行結果サマリー                                │
├─────────────────────────────────────────────────────────┤
│ 対象: FUNC_002_books                                     │
│ 実行時刻: 2025-01-18 12:34:56                            │
│ 所要時間: 12.5秒                                         │
├─────────────────────────────────────────────────────────┤
│ テスト結果:                                               │
│   ✅ 成功: 15/18 (83.3%)                                 │
│   ❌ 失敗: 3/18 (16.7%)                                  │
│   ⏭️  スキップ: 0/18                                     │
├─────────────────────────────────────────────────────────┤
│ カバレッジ:                                               │
│   📏 行: 70.0% (目標: 80%) ⚠️ 未達                        │
│   🔀 分岐: 66.7% (目標: 70%) ⚠️ 未達                      │
│   📦 メソッド: 83.3% (目標: 85%) ⚠️ 未達                  │
├─────────────────────────────────────────────────────────┤
│ 品質ゲート: ❌ 不合格                                      │
└─────────────────────────────────────────────────────────┘
```

#### 5-2. 問題の詳細レポート

問題ごとに詳細レポートを生成:

##### 問題1: テスト失敗

```
❌ 問題1: テスト失敗

テストケース: BookServiceTest.testSearchBooks_WithKeyword
失敗理由: org.junit.AssertionError: expected: 3 but was: 2
場所: BookServiceTest.java:45

【原因分析】
期待値: 書籍3件
実際値: 書籍2件

考えられる原因:
1. 検索ロジックの誤り（実装の問題）
2. テストデータの不足（テストの問題）
3. 検索仕様の誤解（設計の問題）

【参照SPEC】
- functional_design.md: 「4.2 書籍検索機能」
- detailed_design.md: 「5.1 BookService.searchBooks」
- behaviors.md: 「正常系: キーワード検索」

【推奨アクション】
ユーザーに以下を確認してください：

A. 実装を確認する
   → BookService.searchBooks の LIKE 検索条件を確認
   → SQL: WHERE (book_name LIKE ? OR author LIKE ?)

B. 設計を確認する
   → detailed_design.md の検索ロジック仕様を確認
   → キーワードは部分一致か完全一致か？

C. テストを確認する
   → テストデータに3件の書籍が登録されているか確認
   → setUp メソッドのデータ準備を確認
```

##### 問題2: カバレッジ不足（必要な振る舞い）

```
⚠️ 問題2: カバレッジ不足（必要な振る舞い）

未カバーコード:
- BookService.java L45-L48: NotFoundException ハンドリング
- BookService.java L78-L82: ValidationException ハンドリング

コード:
```java
// L45-L48
if (book == null) {
    throw new NotFoundException("Book not found: " + id);
}

// L78-L82
if (!isValid(keyword)) {
    throw new ValidationException("Invalid keyword");
}
```

【分析】
これらは必要な振る舞い（例外ハンドリング）です。
テストケースが不足しています。

【behaviors.md との比較】
現在の behaviors.md には以下の記載なし:
- 異常系: 書籍が見つからない場合
- 異常系: キーワードが不正な場合

【推奨アクション】
behaviors.md に以下のテストシナリオを追加してください：

```markdown
## 異常系: 書籍が見つからない場合

Given: 存在しないID "999" で検索
When: getBook("999") を呼び出す
Then: NotFoundException がスローされる
And: エラーメッセージは "Book not found: 999"

## 異常系: キーワードが不正な場合

Given: 空文字列のキーワードで検索
When: searchBooks("") を呼び出す
Then: ValidationException がスローされる
And: エラーメッセージは "Invalid keyword"
```

その後、以下を実行:
1. @agent_skills/jakarta-ee-api-base/instructions/code_generation.md でテストコード生成
2. 再度テスト実行
```

##### 問題3: カバレッジ不足（デッドコード）

```
⚠️ 問題3: デッドコード検出

未カバーコード:
- BookService.java L120-L125: 冗長な else 分岐

コード:
```java
// L115-L125
if (book == null) {
    throw new NotFoundException("Book not found");
}
// ... 処理 ...
if (book != null) {  // L120: 冗長
    return book;
} else {             // L122: デッドコード（到達不可能）
    return null;
}
```

【分析】
L115 で既に book != null を確認済みのため、L120-L125 の条件分岐は不要です。
L122 の else 分岐は到達不可能（デッドコード）です。

【推奨アクション】
detailed_design.md に以下を明記してください：

```markdown
## 6. 削除すべき冗長なコード

### 6.1 BookService.searchBooks の冗長な null チェック

現状:
- L115 で book == null をチェックして例外をスロー
- L120 で再度 book != null をチェック

問題:
- L120 の条件は常に true
- L122 の else 分岐は到達不可能

推奨:
- L120-L125 を削除
- L126 以降を直接記述
```

その後、以下を実行:
1. コードから L120-L125 を削除
2. @agent_skills/jakarta-ee-api-base/instructions/detailed_design.md で設計書更新
3. 再度テスト実行
```

##### 問題4: 設計の誤り

```
❌ 問題4: 設計の誤り（仕様との不一致）

未カバーコード:
- BookService.java L200-L210: プレミアム会員向け割引ロジック

コード:
```java
// L200-L210
if (user.isPremium()) {
    price = price * 0.9;  // 10% 割引
}
```

【分析】
このコードは実装されていますが、以下の問題があります:

1. functional_design.md に割引機能の記載がない
2. requirements.md に要件がない
3. テストケースが存在しない

これは以下のいずれかです:
A. 不要な機能（実装ミス）
B. 設計書の記載漏れ

【推奨アクション】
ユーザーに確認してください：

A. 割引機能は必要か？
   → 必要な場合:
     1. requirements.md に要件を追加
     2. functional_design.md に仕様を追加
     3. detailed_design.md に設計を追加
     4. behaviors.md にテストシナリオを追加
   
   → 不要な場合:
     1. コードから削除
     2. 再度テスト実行
```

#### 5-3. アクションプランの提示

すべての問題をまとめてアクションプランを提示:

```
┌─────────────────────────────────────────────────────────┐
│ 🎯 推奨アクションプラン                                     │
└─────────────────────────────────────────────────────────┘

優先度1（Critical）: テスト失敗の修正
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
□ 問題1: BookServiceTest.testSearchBooks_WithKeyword の失敗
  → 選択肢:
    A. 実装を確認・修正
    B. 設計を確認・修正
    C. テストを確認・修正

優先度2（High）: カバレッジ不足の解消
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
□ 問題2: 例外ハンドリングのテスト不足
  → アクション: behaviors.md に異常系シナリオを追加
  
  以下をコピーして behaviors.md に追加してください:
  
  ```markdown
  ## 異常系: 書籍が見つからない場合
  
  Given: 存在しないID "999" で検索
  When: getBook("999") を呼び出す
  Then: NotFoundException がスローされる
  And: エラーメッセージは "Book not found: 999"
  ```
  
  その後: @agent_skills/jakarta-ee-api-base/instructions/code_generation.md

優先度3（Medium）: デッドコードの削除
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
□ 問題3: BookService L120-L125 の冗長なコード
  → アクション:
    1. detailed_design.md に削除理由を明記
    2. コードから削除
    3. 再度テスト実行

優先度4（Low）: 設計の確認
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
□ 問題4: プレミアム会員向け割引ロジックの要否確認
  → 質問: この機能は必要ですか？
    A. 必要 → 設計書に追加
    B. 不要 → コードから削除

┌─────────────────────────────────────────────────────────┐
│ どのように進めますか？                                      │
│                                                          │
│ A. 優先度1から順番に対応                                   │
│ B. 特定の問題のみ対応（番号を指定）                          │
│ C. すべての推奨アクションを一括で適用（手動確認なし）           │
│ D. 詳細を確認したい（問題番号を指定）                        │
│ E. 一旦保留（レポートのみ保存）                             │
└─────────────────────────────────────────────────────────┘
```

---

### ステップ6: ユーザーの選択に従った対応

#### 6-1. ユーザーの選択を待つ

重要: ここでユーザーの入力を待ち、勝手に進めない

ユーザーが選択するまで待機し、以下のいずれかが指示されるまで何もしない:
* 具体的な問題番号と対応方法
* アクションプランの実行指示
* 追加の詳細確認

#### 6-2. 選択に応じた対応（例）

ユーザーが「問題2の behaviors.md 追加を実行」と指示した場合:

1. `{project_root}/specs/baseline/detailed_design/{target_type}/behaviors.md` を読み込む
2. 提案した内容を追加
3. 更新内容を表示
4. 次のステップ（code_generation.md）を案内

ユーザーが「問題3のデッドコード削除を実行」と指示した場合:

1. `{project_root}/specs/baseline/detailed_design/{target_type}/detailed_design.md` を読み込む
2. 「削除すべき冗長なコード」セクションを追加
3. 対象ファイル（BookService.java）から該当行を削除
4. 更新内容を表示
5. 再テスト実行を案内

ユーザーが「問題1の詳細を確認したい」と指示した場合:

1. 該当するコード（BookService.searchBooks）を表示
2. 該当するSPEC（detailed_design.md、functional_design.md）を表示
3. 該当するテスト（BookServiceTest.testSearchBooks_WithKeyword）を表示
4. 比較と分析結果を提示
5. 再度アクションの選択を求める

---

### ステップ7: レポートの保存

最終的なレポートを保存:

#### 7-1. JSON形式で保存（AI・CI/CD向け）

```
{project_root}/build/reports/test-analysis/test_analysis_report.json
```

内容:
```json
{
  "timestamp": "2025-01-18T12:34:56Z",
  "target_type": "FUNC_002_books",
  "summary": { ... },
  "test_results": { ... },
  "coverage": { ... },
  "issues": [
    {
      "id": "issue-1",
      "type": "test_failure",
      "severity": "critical",
      "description": "...",
      "recommendation": "..."
    }
  ],
  "action_plan": { ... }
}
```

#### 7-2. Markdown形式で保存（人間向け）

```
{project_root}/build/reports/test-analysis/test_analysis_report.md
```

内容: ステップ5で生成したフィードバックレポートをそのまま保存

#### 7-3. ユーザーに完了を通知

```
✅ 単体テスト実行評価が完了しました

レポート保存先:
- JSON: build/reports/test-analysis/test_analysis_report.json
- Markdown: build/reports/test-analysis/test_analysis_report.md
- HTML: build/reports/tests/test/index.html
- JaCoCo: build/reports/jacoco/test/html/index.html

次のステップ:
問題を修正後、再度テストを実行してください:

@agent_skills/jakarta-ee-api-base/instructions/unit_test_execution.md

パラメータ:
* project_root: {project_root}
* target_type: {target_type}
```

---

## 注意事項

### テスト実行の制約

* テスト実行はGradleに依存する
* JUnit 5とJaCoCoがセットアップされている前提
* テストクラスの命名規則: `*Test.java`

### カバレッジ除外の確認

以下のクラスはカバレッジ計算から除外される:
* DTOクラス（`*Dto.java`、`**/dto/**`）
* Recordクラス（`*Record.java`）
* 自動生成コード（`**/generated/**`）
* 設定クラス（オプション: `*Config.java`）

除外設定は `build.gradle` で確認できる:
```gradle
jacocoTestReport {
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                '**/dto/**',
                '**/*Dto.class',
                '**/*Record.class',
                '**/generated/**'
            ])
        }))
    }
}
```

### フィードバックループの実行

品質ゲートを通過するまで、以下のループを繰り返す:

```
詳細設計 → コード生成 → テスト実行 → 評価
    ↑                              ↓
    └──────── フィードバック ←─────┘
```

各イテレーションで:
1. 問題を1つずつ解決
2. 再度テスト実行
3. 改善を確認
4. 品質ゲート通過まで繰り返し

---

## 参考資料

* [code_generation.md](code_generation.md) - コード生成インストラクション
* [detailed_design.md](detailed_design.md) - 詳細設計インストラクション
* [e2e_test_generation.md](e2e_test_generation.md) - E2Eテスト生成インストラクション
* [common_rules.md](../principles/common_rules.md) - 共通ルール
* [architecture.md](../principles/architecture.md) - アーキテクチャ標準
