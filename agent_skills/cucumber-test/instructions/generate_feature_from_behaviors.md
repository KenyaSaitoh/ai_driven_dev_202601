# Cucumber Featureファイル生成指示書（behaviors.mdから）

バージョン: 1.0.0  
最終更新日: 2026-02-11

---

## 目的

プロジェクトの `behaviors.md` ファイルを解析し、Gherkin形式の `.feature` ファイルを自動生成する。

重要: この指示書は `.feature` ファイルのみを生成します。Step Definitions やテストランナーを含む完全なCucumberテストコード生成には `generate_cucumber_tests.md` を使用してください。

---

## パラメータ

| パラメータ名 | 必須 | デフォルト値 | 説明 |
|------------|------|-------------|------|
| `project_path` | ✓ | - | プロジェクトのルートディレクトリパス |
| `behaviors_file` | - | 自動検索 | behaviors.mdファイルのパス（省略時はプロジェクト内を検索） |
| `output_same_location` | - | `true` | behaviors.mdと同じディレクトリに出力するか |
| `feature_output_dir` | - | behaviors.mdと同じ場所 | Featureファイルの出力ディレクトリ（output_same_location=falseの場合のみ有効） |

---

## 実行手順

### ステップ1: behaviors.mdファイルの検索

1. パラメータ確認
   ```
   behaviors_file が指定されている場合、そのパスを使用
   指定されていない場合、プロジェクト内を検索
   ```

2. 検索パターン
   
   以下の場所を優先順に検索:
   
   ```
   1. {project_path}/specs/baseline/requirements/behaviors.md
   2. {project_path}/specs/baseline/basic_design/*/behaviors.md
   3. {project_path}/specs/baseline/detailed_design/*/behaviors.md
   4. {project_path}/specs/baseline/usecases/*/behaviors.md
   5. {project_path}//behaviors.md
   ```

3. 複数ファイルが見つかった場合
   - すべてのbehaviors.mdファイルをリストアップ
   - 各ファイルに対して個別に.featureファイルを生成

### ステップ2: behaviors.mdの解析

1. ファイル構造の確認
   
   behaviors.mdは以下のいずれかの形式で記述されている:
   
   パターンA: Gherkin形式（そのまま使用可能）
   ```markdown
   # 注文管理の振る舞い
   
   ## シナリオ1: 注文作成
   
   ```gherkin
   @integration
   Feature: 注文管理
   
     Scenario: 新規注文を作成する
       Given 顧客ID "CUST001" が存在する
       And 書籍ISBN "978-4-0001" の在庫が10冊ある
       When 顧客が書籍を2冊注文する
       Then 注文が正常に作成される
       And 在庫が8冊に減る
   ```
   ```
   
   パターンB: 自然言語形式（Gherkinに変換が必要）
   ```markdown
   # 注文管理の振る舞い
   
   ## シナリオ1: 注文作成
   
   ### 前提条件
   - 顧客ID "CUST001" が登録されている
   - 書籍ISBN "978-4-0001" の在庫が10冊ある
   
   ### 実行
   - 顧客が書籍を2冊注文する
   
   ### 期待結果
   - 注文が正常に作成される
   - 在庫が8冊に減る
   ```

2. シナリオの抽出
   
   各behaviors.mdから以下を抽出:
   - 機能名（ファイル名またはトップレベルヘッダー）
   - シナリオ一覧（## で始まる各セクション）
   - 各シナリオの内容

### ステップ3: Gherkin形式への変換

#### 3-1. パターンA（Gherkin形式）の場合

```gherkin
コードブロック内のGherkin記述をそのまま抽出
複数のGherkinブロックがある場合は統合
```

#### 3-2. パターンB（自然言語形式）の場合

1. 機能名の決定
   ```
   behaviors.mdのファイル名またはトップレベルヘッダーから抽出
   例: "requirements/behaviors.md" → "要件仕様の振る舞い"
   例: "basic_design/orders/behaviors.md" → "注文管理"
   ```

2. シナリオの変換
   
   各シナリオを以下のパターンでGherkin形式に変換:
   
   ```gherkin
   Scenario: {シナリオ名}
     Given {前提条件}
     [And {追加の前提条件}]
     When {実行アクション}
     [And {追加のアクション}]
     Then {期待結果}
     [And {追加の期待結果}]
   ```

3. 英語キーワードマッピング
   
   | 自然言語 | Gherkinキーワード |
   |---------|------------------|
   | 前提条件、前提、Given | `Given` |
   | 実行、アクション、When | `When` |
   | 期待結果、結果、Then | `Then` |
   | および、かつ、And | `And` |
   | しかし、But | `But` |

### ステップ4: .featureファイルの生成

1. ファイル名の決定
   
   behaviors.mdのパスから.featureファイル名を生成:
   
   ```
   {behavior_file_directory}/{feature_name}.feature
   ```
   
   例:
   - `specs/baseline/requirements/behaviors.md` → `specs/baseline/requirements/requirements.feature`
   - `specs/baseline/basic_design/orders/behaviors.md` → `specs/baseline/basic_design/orders/orders.feature`
   - `specs/baseline/detailed_design/common/behaviors.md` → `specs/baseline/detailed_design/common/common.feature`

2. Featureファイルのヘッダー
   
   ```gherkin
   @integration
   Feature: {機能名}
   
     {機能の説明（オプション）}
   ```

3. 背景の追加（共通前提条件がある場合）
   
   ```gherkin
   Background:
     Given テストデータベースが初期化されている
     And トランザクションが開始されている
   ```

4. シナリオの記述
   
   抽出したすべてのシナリオを記述:
   
   ```gherkin
   Scenario: {シナリオ名1}
     Given {前提条件}
     When {アクション}
     Then {期待結果}
   
   Scenario: {シナリオ名2}
     Given {前提条件}
     When {アクション}
     Then {期待結果}
   ```

5. タグの付与
   
   - `@integration` - 結合テストとして識別
   - `@{domain}` - ドメイン名（例: @orders, @books, @customers）
   - `@positive` / `@negative` - 正常系/異常系の分類

### ステップ5: ファイルの保存

1. 出力先の決定
   
   `output_same_location` パラメータに基づいて出力先を決定:
   
   output_same_location = true（デフォルト）:
   ```
   behaviors.mdと同じディレクトリに保存
   例: specs/baseline/requirements/behaviors.md
    → specs/baseline/requirements/requirements.feature
   ```
   
   output_same_location = false:
   ```
   feature_output_dir で指定されたディレクトリに保存
   例: src/test/resources/features/{domain_name}.feature
   ```

2. ディレクトリの作成
   ```
   出力先ディレクトリが存在しない場合は作成
   ```

3. ファイルの書き込み
   ```
   UTF-8エンコーディングで.featureファイルを保存
   ```

### ステップ6: 生成結果の報告

1. 成功メッセージ
   
   ```
   ✅ Featureファイルを生成しました:
   
   📁 {output_path}
   
   📊 生成統計:
   - 機能数: {feature_count}
   - シナリオ数: {scenario_count}
   - ステップ数: {step_count}
   ```

2. 次のステップの案内**
   
   ```
   💡 次のステップ:
   
   1. 生成された.featureファイルを確認してください
   2. 必要に応じて手動でシナリオを調整してください
   3. Step Definitionsを生成する場合は以下を実行してください:
   
   @agent_skills/cucumber-test/instructions/generate_cucumber_tests.md
   
   パラメータ:
   * project_path: {project_path}
   * package_root: {package_root}
   * feature_file: {generated_feature_file}
   ```

---

## 生成ルール

### 1. Gherkin構文の遵守

- 英語キーワードを使用: `Feature`, `Background`, `Scenario`, `Given`, `When`, `Then`, `And`, `But`
- 説明文は日本語で記述
- インデント: 2スペース
- エンコーディング: UTF-8

### 2. シナリオの構造化

```gherkin
Feature: {機能名}
  
  Background:
    Given {共通前提条件}
  
  Scenario: {正常系シナリオ}
    Given {前提条件}
    When {アクション}
    Then {期待結果}
  
  Scenario: {異常系シナリオ}
    Given {前提条件}
    When {エラーを引き起こすアクション}
    Then {エラー処理の確認}
```

### 3. タグの付与規則

- `@integration` - すべてのシナリオに付与
- `@{domain}` - ドメイン名（orders, books, customers等）
- `@positive` - 正常系シナリオ
- `@negative` - 異常系シナリオ
- `@wip` - 作業中（Work In Progress）
- `@smoke` - スモークテスト対象

### 4. データテーブルの使用

繰り返しデータがある場合はデータテーブルを使用:

```gherkin
Scenario: 複数の書籍を検索する
  Given 以下の書籍が登録されている
    | ISBN          | タイトル           | 在庫 |
    | 978-4-0001    | Java入門           | 10   |
    | 978-4-0002    | Spring実践         | 5    |
  When 書籍を検索する
  Then 2冊の書籍が見つかる
```

### 5. シナリオアウトラインの使用

パラメータ化されたテストはシナリオアウトラインを使用:

```gherkin
Scenario Outline: 在庫確認
  Given 書籍ISBN "<ISBN>" の在庫が<初期在庫>冊ある
  When <注文数>冊注文する
  Then 在庫が<残り在庫>冊になる

  Examples:
    | ISBN       | 初期在庫 | 注文数 | 残り在庫 |
    | 978-4-0001 | 10       | 2      | 8        |
    | 978-4-0002 | 5        | 3      | 2        |
```

---

## ベストプラクティス

### 1. 明確なシナリオ名

❌ 悪い例:
```gherkin
Scenario: テスト1
```

✅ 良い例:
```gherkin
Scenario: 在庫がある場合に注文を作成できる
```

### 2. ビジネス言語の使用

❌ 悪い例:
```gherkin
Given データベースのorder_tranテーブルにレコードが存在する
```

✅ 良い例:
```gherkin
Given 顧客"田中太郎"の注文が1件存在する
```

### 3. 実装の詳細を避ける

❌ 悪い例:
```gherkin
When OrderService.createOrder()メソッドを呼び出す
```

✅ 良い例:
```gherkin
When 顧客が書籍を注文する
```

### 4. 1シナリオ1検証

各シナリオは1つの振る舞いのみを検証:

❌ 悪い例:
```gherkin
Scenario: 注文と在庫と配送
  Given 顧客が存在する
  When 注文を作成する
  Then 注文が保存される
  And 在庫が減る
  And 配送予定日が設定される
  And メールが送信される
```

✅ 良い例:
```gherkin
Scenario: 注文作成時に在庫が減る
  Given 書籍の在庫が10冊ある
  When 2冊注文する
  Then 在庫が8冊になる
```

---

## トラブルシューティング

### 問題1: behaviors.mdが見つからない

原因: 指定されたプロジェクトにbehaviors.mdが存在しない

解決策:
1. プロジェクト構造を確認
2. behaviors_fileパラメータで明示的にパスを指定
3. behaviors.mdを先に作成してから実行

### 問題2: Gherkin変換が正しくない

原因: behaviors.mdの構造が想定と異なる

解決策:
1. behaviors.mdの形式を確認（パターンAまたはB）
2. 必要に応じてbehaviors.mdの構造を調整
3. 手動でGherkin形式で記述する

### 問題3: 文字化けが発生する

原因: エンコーディングの不一致

解決策:
1. behaviors.mdがUTF-8であることを確認
2. .featureファイルをUTF-8で保存
3. エディタのエンコーディング設定を確認

---

## 完成チェックリスト

生成完了後、以下を確認してください:

- [ ] .featureファイルが正しいディレクトリに生成されている
- [ ] `@integration` タグが付与されている
- [ ] すべてのシナリオが変換されている
- [ ] Gherkin構文が正しい（Given-When-Then構造）
- [ ] 英語キーワード（Feature, Scenario, Given, When, Then, And）が使用されている
- [ ] 説明文が日本語で記述されている
- [ ] シナリオ名が明確でビジネス言語になっている
- [ ] 実装の詳細が含まれていない
- [ ] UTF-8エンコーディングで保存されている

---

## 参考資料

* [Cucumber Gherkin Reference](https://cucumber.io/docs/gherkin/reference/)
* [Cucumberベストプラクティス](../principles/cucumber_best_practices.md)
* [Featureファイルテンプレート](../templates/cucumber_feature_template.md)
* [完全なCucumberテスト生成](generate_cucumber_tests.md)

---

## バージョン履歴

| バージョン | 日付 | 変更内容 |
|----------|------|---------|
| 1.1.0 | 2026-02-11 | Gherkinキーワードを英語に変更（Feature, Given, When, Then, And） |
| 1.0.0 | 2026-02-11 | 初版作成 |
