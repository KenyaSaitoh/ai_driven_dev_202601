# テスト評価インストラクション

## パラメータ設定

実行前に以下のパラメータを設定する

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
jacoco_reports_dir: "ここにJacocoレポートディレクトリのパスを入力"
test_type: "unit"  # unit, integration, e2e のいずれか
spec_directory: "ここにSPECディレクトリのパスを入力（オプション）"
```

### オプショナルパラメータ

```yaml
# カバレッジ目標（未指定時は architecture_design.md → デフォルト値）
coverage_targets:
  line: null      # 行カバレッジ目標 (0-100)、デフォルト: 80
  branch: null    # 分岐カバレッジ目標 (0-100)、デフォルト: 70
  method: null    # メソッドカバレッジ目標 (0-100)、デフォルト: 85

# 品質ゲート設定
failure_handling: "report"  # report: レポート生成して継続 / stop: 即座に停止
dead_code_policy: "warn"    # warn: 警告のみ / error: エラー扱い / ignore: 無視
min_test_count: null        # 最小テスト数（未指定ならチェックなし）

# カバレッジ除外設定（デフォルト: DTO、Record、自動生成コード）
coverage_exclusions:
  - "/dto/"
  - "/*Dto.java"
  - "/*Record.java"
  - "/generated/"

# レポート出力設定
report_formats:
  html: true                # HTML形式（人間向け）、デフォルト: true
  json: true                # JSON形式（AI向け）、デフォルト: true
  xml: false                # XML形式、デフォルト: false

# 出力先
output_report_path: null    # レポート出力先（未指定時は {project_root}/test-reports/{test_type}-evaluation-report.md）
```

### パラメータ例

例1: 単体テスト評価（最小限）
```yaml
project_root: "projects/sdd-wf/bookstore/back-office-api"
jacoco_reports_dir: "projects/sdd-wf/bookstore/back-office-api/build/reports/jacoco/test"
test_type: "unit"
spec_directory: "projects/sdd-wf/bookstore/back-office-api/specs/baseline"
```

例2: 結合テスト評価
```yaml
project_root: "projects/sdd-wf/bookstore/back-office-api"
jacoco_reports_dir: "projects/sdd-wf/bookstore/back-office-api/build/reports/jacoco/integrationTest"
test_type: "integration"
spec_directory: "projects/sdd-wf/bookstore/back-office-api/specs/baseline"
```

例3: E2Eテスト評価
```yaml
project_root: "projects/sdd-wf/bookstore/back-office-api"
jacoco_reports_dir: "projects/sdd-wf/bookstore/back-office-api/build/reports/jacoco/e2eTest"
test_type: "e2e"
spec_directory: "projects/sdd-wf/bookstore/back-office-api/specs/baseline"
```

例4: カバレッジ目標カスタマイズ
```yaml
project_root: "projects/sdd-wf/bookstore/back-office-api"
jacoco_reports_dir: "projects/sdd-wf/bookstore/back-office-api/build/reports/jacoco/test"
test_type: "unit"
coverage_targets:
  line: 85
  branch: 75
  method: 90
```

注意
* パス区切りはOS環境に応じて調整する（Windows: `\`, Unix/Linux/Mac: `/`）
* 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換える
* 以降、`{jacoco_reports_dir}` と表記されている箇所は、上記で設定した値に置き換える

---

## 概要

このインストラクションは、テスト実行結果（Jacocoレポート）を評価し、カバレッジやテスト品質を分析するためのものである。

重要な原則:
* テスト実行は不要: このタスクではテストを実行しない。既に実行済みのJacocoレポートを評価する
* 汎用性: 単体テスト、結合テスト、E2Eテストのいずれにも対応
* 読み取り専用: コードやSPECを修正しない。評価結果とフィードバックのみを提供
* ユーザー確認必須: 問題を発見した場合は、必ずユーザーに提案を提示し、ユーザーの選択を待つ

---

## 1. カバレッジ目標の取得

カバレッジ目標は以下の優先順位で決定する:

1. パラメータで明示的に指定された値（coverage_targets）
2. architecture_design.md の「テスト戦略」セクション（spec_directory が指定されている場合）
3. デフォルト値:
   * 行カバレッジ: 80%
   * 分岐カバレッジ: 70%
   * メソッドカバレッジ: 85%

### architecture_design.md からの取得

spec_directory が指定されている場合、以下のファイルからカバレッジ目標を取得する:

* `{spec_directory}/basic_design/common/architecture_design.md` の「テスト戦略」セクション

例:
```markdown
## テスト戦略

### カバレッジ目標
* 行カバレッジ: 85%以上
* 分岐カバレッジ: 75%以上
* メソッドカバレッジ: 90%以上
```

---

## 2. Jacocoレポートの読み込み

### 2.1 レポートファイルの確認

`{jacoco_reports_dir}` ディレクトリから以下のファイルを確認する:

* 必須: `jacocoTestReport.xml` または `jacoco.xml` - XMLレポート（カバレッジデータ）
* オプション: `html/index.html` - HTMLレポート（人間向け）
* オプション: `jacocoTestReport.csv` または `jacoco.csv` - CSVレポート

### 2.2 XMLレポートの解析

XMLレポートから以下の情報を抽出する:

* 全体カバレッジ:
  * 行カバレッジ (LINE)
  * 分岐カバレッジ (BRANCH)
  * メソッドカバレッジ (METHOD)
  * クラスカバレッジ (CLASS)

* パッケージ別カバレッジ:
  * 各パッケージの行/分岐/メソッドカバレッジ
  * カバレッジが低いパッケージの特定

* クラス別カバレッジ:
  * 各クラスの行/分岐/メソッドカバレッジ
  * カバレッジが0%のクラス（デッドコード候補）
  * カバレッジが目標未達のクラス

* メソッド別カバレッジ:
  * カバレッジが0%のメソッド（未テストメソッド）
  * カバレッジが低いメソッド

### 2.3 XMLレポートの例

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<report name="JaCoCo Coverage Report">
  <sessioninfo id="..." start="..." dump="..."/>
  <package name="com/example/service">
    <class name="com/example/service/OrderService">
      <method name="createOrder" desc="...">
        <counter type="INSTRUCTION" missed="0" covered="45"/>
        <counter type="BRANCH" missed="2" covered="8"/>
        <counter type="LINE" missed="0" covered="12"/>
        <counter type="METHOD" missed="0" covered="1"/>
      </method>
    </class>
    <counter type="INSTRUCTION" missed="10" covered="200"/>
    <counter type="BRANCH" missed="5" covered="40"/>
    <counter type="LINE" missed="2" covered="50"/>
    <counter type="METHOD" missed="1" covered="10"/>
    <counter type="CLASS" missed="0" covered="5"/>
  </package>
  <counter type="INSTRUCTION" missed="50" covered="1000"/>
  <counter type="BRANCH" missed="20" covered="180"/>
  <counter type="LINE" missed="10" covered="250"/>
  <counter type="METHOD" missed="5" covered="60"/>
  <counter type="CLASS" missed="2" covered="25"/>
</report>
```

---

## 3. テスト結果の読み込み

### 3.1 テスト結果ファイルの確認

`{project_root}/build/test-results/{test_type}` ディレクトリから以下のファイルを確認する:

* **TEST-*.xml** - JUnitテスト結果（XML形式）

### 3.2 テスト結果の解析

テスト結果ファイルから以下の情報を抽出する:

* テスト実行サマリー:
  * 総テスト数
  * 成功数
  * 失敗数
  * スキップ数
  * 実行時間

* 失敗したテスト:
  * テストクラス名
  * テストメソッド名
  * 失敗理由（エラーメッセージ、スタックトレース）

---

## 4. カバレッジ評価

### 4.1 全体カバレッジの評価

カバレッジ目標と実測値を比較し、達成状況を評価する:

| 指標 | 目標 | 実測値 | 達成状況 |
|------|------|--------|----------|
| 行カバレッジ | 80% | 85% | ✅ 達成 |
| 分岐カバレッジ | 70% | 65% | ❌ 未達成 (5%不足) |
| メソッドカバレッジ | 85% | 90% | ✅ 達成 |

### 4.2 パッケージ別カバレッジの評価

カバレッジが低いパッケージを特定し、優先度を付ける:

* 高優先度 (カバレッジ < 60%):
  * パッケージ名、現在のカバレッジ、未カバー行数
  * 推奨アクション: 追加テストケースの作成

* 中優先度 (カバレッジ 60-80%):
  * パッケージ名、現在のカバレッジ
  * 推奨アクション: 境界値テスト、エッジケーステストの追加

### 4.3 クラス別カバレッジの評価

カバレッジが0%のクラス（デッドコード候補）を特定:

* デッドコード候補:
  * クラス名、カバレッジ0%
  * 推奨アクション: 使用されているか確認、不要なら削除

* カバレッジ除外対象の確認:
  * coverage_exclusions で指定されたパターンに一致するクラス
  * DTO、Record、自動生成コードは評価対象外

### 4.4 メソッド別カバレッジの評価

未テストメソッドを特定:

* 未テストメソッド (カバレッジ0%):
  * クラス名、メソッド名、行数
  * 推奨アクション: テストケースの追加

* 部分的にテストされているメソッド (カバレッジ < 50%):
  * クラス名、メソッド名、現在のカバレッジ
  * 推奨アクション: 分岐カバレッジの向上、エッジケーステストの追加

---

## 5. テスト品質の評価

### 5.1 テスト数の評価

* 最小テスト数チェック (min_test_count が指定されている場合):
  * 実測テスト数と最小テスト数を比較
  * 不足している場合は警告

* テスト密度 (テスト数 / メソッド数):
  * 一般的な目標: 1.5以上（1メソッドあたり1.5個以上のテストケース）
  * 低い場合は、境界値テストやエッジケーステストが不足している可能性

### 5.2 失敗したテストの分析

失敗したテストがある場合、以下を分析する:

* 失敗の原因:
  * アサーションエラー
  * 例外
  * タイムアウト

* 失敗パターン:
  * 特定のクラス/パッケージに集中しているか
  * 特定のテストタイプ（正常系、異常系、境界値等）に偏っているか

* 推奨アクション:
  * 実装コードの修正
  * テストコードの修正
  * テストデータの見直し

---

## 6. デッドコードの検出

### 6.1 デッドコードの定義

以下の条件を満たすコードをデッドコード候補とする:

* カバレッジ0%のクラス
* カバレッジ0%のメソッド
* カバレッジ除外対象外

### 6.2 デッドコードの分類

* 真のデッドコード:
  * 使用されていないコード
  * 削除推奨

* 未テストコード:
  * 実際には使用されているが、テストが不足しているコード
  * テスト追加推奨

* 除外対象:
  * DTO、Record、自動生成コード
  * カバレッジ評価対象外

### 6.3 デッドコードポリシーの適用

dead_code_policy パラメータに応じて処理を変更:

* warn: 警告メッセージを出力し、処理を継続
* error: エラーとして扱い、failure_handling に従う
* ignore: 無視して処理を継続

---

## 7. 評価レポートの生成

### 7.1 レポート構造

評価レポートは以下の構造で生成する:

```markdown
# {test_type}テスト評価レポート

プロジェクト: {project_root}
テストタイプ: {test_type}
評価日時: {timestamp}

---

## 1. エグゼクティブサマリー

* 全体評価: ✅ 合格 / ⚠️ 警告あり / ❌ 不合格
* カバレッジ達成状況: 3/3項目達成
* テスト実行結果: 50件成功、0件失敗、0件スキップ
* 主な問題点: デッドコード2件検出

---

## 2. カバレッジ評価

### 2.1 全体カバレッジ

| 指標 | 目標 | 実測値 | 達成状況 |
|------|------|--------|----------|
| 行カバレッジ | 80% | 85% | ✅ 達成 |
| 分岐カバレッジ | 70% | 65% | ❌ 未達成 (5%不足) |
| メソッドカバレッジ | 85% | 90% | ✅ 達成 |

### 2.2 パッケージ別カバレッジ（目標未達成のみ）

| パッケージ | 行カバレッジ | 分岐カバレッジ | 推奨アクション |
|-----------|------------|--------------|--------------|
| com.example.service | 55% | 45% | 境界値テスト追加 |
| com.example.dao | 70% | 60% | エッジケーステスト追加 |

---

## 3. 未テストコード

### 3.1 カバレッジ0%のクラス（デッドコード候補）

| クラス名 | 行数 | 推奨アクション |
|---------|------|--------------|
| com.example.util.OldHelper | 50 | 使用されているか確認、不要なら削除 |

### 3.2 カバレッジ0%のメソッド

| クラス名 | メソッド名 | 行数 | 推奨アクション |
|---------|-----------|------|--------------|
| OrderService | cancelOrder | 20 | テストケース追加 |
| OrderService | refundOrder | 15 | テストケース追加 |

---

## 4. テスト品質評価

### 4.1 テスト実行サマリー

* 総テスト数: 50件
* 成功: 50件
* 失敗: 0件
* スキップ: 0件
* 実行時間: 5.2秒

### 4.2 テスト密度

* メソッド数: 30
* テスト数: 50
* テスト密度: 1.67 (✅ 目標1.5以上を達成)

---

## 5. 推奨アクション

### 5.1 高優先度（必須）

1. 分岐カバレッジの向上
   * 対象: com.example.service パッケージ
   * 現在: 45% → 目標: 70%
   * 不足: 25%
   * アクション: 境界値テスト、エラーパステストの追加

2. デッドコードの確認・削除
   * 対象: OldHelper クラス
   * アクション: 使用されているか確認、不要なら削除

### 5.2 中優先度（推奨）

1. 未テストメソッドのテスト追加
   * cancelOrder, refundOrder メソッド
   * アクション: 正常系、異常系、境界値テストの追加

---

## 6. 次のステップ

1. テストケースの追加: 未テストメソッドのテストを追加
2. コードレビュー: デッドコード候補の確認
3. 再評価: テスト追加後、再度評価を実行
```

### 7.2 レポート出力

レポートは以下の場所に出力する:

* デフォルト: `{project_root}/test-reports/{test_type}-evaluation-report.md`
* カスタム: output_report_path で指定されたパス

### 7.3 レポート形式

* Markdown形式 (デフォルト): 人間が読みやすい形式
* JSON形式 (オプション): AI/ツールが処理しやすい形式
* HTML形式 (オプション): ブラウザで表示可能な形式

---

## 8. フィードバックと提案

### 8.1 フィードバックの原則

* 具体的: 何が問題か、どこが問題かを明確に示す
* アクション可能: ユーザーが何をすべきかを具体的に提示する
* 優先度付き: 高優先度、中優先度、低優先度に分類する

### 8.2 提案の形式

問題を発見した場合、以下の形式でユーザーに提案する:

```markdown
## 発見された問題

### 問題1: 分岐カバレッジ未達成

* 現状: 65% (目標: 70%)
* 影響: 5%の分岐がテストされていない
* 対象: com.example.service パッケージ

### 推奨アクション

以下のいずれかを選択してください:

A. テストケースを追加して分岐カバレッジを向上させる
B. カバレッジ目標を見直す（architecture_design.md を更新）
C. 一旦スキップして、後で対応する

どちらを選択しますか？
```

### 8.3 ユーザー確認必須

以下の場合は、必ずユーザーに確認する:

* カバレッジ目標未達成: ユーザーに対応方法を選択してもらう
* デッドコード検出: 削除するか、テストを追加するかを選択してもらう
* テスト失敗: 実装コードを修正するか、テストコードを修正するかを選択してもらう

---

## 9. テストタイプ別の評価ポイント

### 9.1 単体テスト (unit)

* 焦点: コードカバレッジ（行、分岐、メソッド）
* 重要指標: 分岐カバレッジ、未テストメソッド
* 推奨アクション: 境界値テスト、エッジケーステストの追加

### 9.2 結合テスト (integration)

* 焦点: コンポーネント間連携、ビジネスロジック
* 重要指標: Service層のカバレッジ、Dao層のカバレッジ
* 推奨アクション: 複数コンポーネント連携のテスト追加

### 9.3 E2Eテスト (e2e)

* 焦点: エンドツーエンドのフロー、API連携
* 重要指標: Resource層のカバレッジ、API呼び出しパス
* 推奨アクション: エラーパステスト、認証/認可テストの追加

---

## 10. 制約と注意事項

### 10.1 読み取り専用

* コードの修正禁止: このタスクではコードやSPECを修正しない
* 評価のみ: 評価結果とフィードバックのみを提供
* ユーザー判断: 修正はユーザーが判断して実施する

### 10.2 テスト実行不要

* 前提: テストは既に実行済み
* 入力: Jacocoレポート、テスト結果ファイル
* 出力: 評価レポート、フィードバック

### 10.3 汎用性

* 単体テスト: unit
* 結合テスト: integration
* E2Eテスト: e2e
* いずれのテストタイプにも対応

---

## 11. 完了検証

* Jacocoレポートが正常に読み込めたことを確認
* カバレッジ評価が完了したことを確認
* デッドコード検出が完了したことを確認
* 評価レポートが生成されたことを確認
* ユーザーへのフィードバックが完了したことを確認

---

## 12. 次のステップ

評価完了後、ユーザーは以下のアクションを選択できる:

1. テストケースの追加: unit_test_generation.md, it_generation.md, e2e_test_generation.md を使用
2. コードの修正: code_generation.md を使用
3. SPECの更新: basic_design_change.md, detailed_design.md を使用
4. 再テスト実行: テスト追加・修正後、テストを再実行
5. 再評価: テスト再実行後、本インストラクションを再度実行

---

## 参考資料

* [単体テスト生成インストラクション](unit_test_generation.md) - 単体テストコード生成
* [結合テスト生成インストラクション](it_generation.md) - 結合テストコード生成
* [E2Eテスト生成インストラクション](e2e_test_generation.md) - E2Eテストコード生成
* [Jakarta EE開発原則](../principles/) - アーキテクチャ標準、品質基準
