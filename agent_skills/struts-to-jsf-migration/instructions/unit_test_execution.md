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

# カバレッジ除外設定（デフォルト: DTO、Record、Managed Bean、自動生成コード）
coverage_exclusions:
  - "**/dto/**"
  - "**/*Dto.java"
  - "**/*Record.java"
  - "**/*Bean.java"         # Managed Bean（UI層）
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
project_root: "projects/sdd/person/jsf-person-sdd"
target_type: "FUNC_002_PersonList"
```

例2: カバレッジ目標カスタマイズ
```yaml
project_root: "projects/sdd/person/jsf-person-sdd"
target_type: "FUNC_002_PersonList"
coverage_targets:
  line: 75
  branch: 65
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

## 実行フロー

Jakarta EE API版の [unit_test_execution.md](../../jakarta-ee-api-base/instructions/unit_test_execution.md) と基本的に同じフローに従う。

### JSF特有の注意事項

#### テスト対象の違い

Jakarta EE API版:
* Resource（REST API）
* Service（ビジネスロジック）
* Dao（データアクセス）

JSF版:
* Managed Bean（バッキングビーン）- カバレッジ除外推奨
* Service（ビジネスロジック）- テスト対象
* Dao（データアクセス）- テスト対象

#### カバレッジ除外の考え方

JSFの Managed Bean は以下の理由でカバレッジ除外を推奨:
* UI層の薄いラッパー
* ビジネスロジックは Service に委譲
* 単体テストよりもE2Eテスト（Playwright）で検証すべき

テスト対象とすべきもの:
* Service層のビジネスロジック
* Dao層のデータアクセス
* バリデーション、変換ロジック

---

## テスト実行

### Gradle タスクの実行

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

---

## 結果分析と問題の分類

Jakarta EE API版と同じ分類:
1. 必要な振る舞い（テストが不足）
2. デッドコード（到達不可能・冗長）
3. 設計の誤り

---

## フィードバックレポート生成

### JSF特有の問題例

#### 問題例1: Managed Bean のビジネスロジック

```
⚠️ 問題: Managed Bean にビジネスロジックが含まれている

未カバーコード:
- PersonListBean.java L45-L60: 検索条件の複雑なバリデーション

コード:
```java
public void search() {
    if (keyword != null && keyword.length() > 0) {
        // 複雑なバリデーションロジック
        if (!keyword.matches("[a-zA-Z0-9]+")) {
            addErrorMessage("不正な文字が含まれています");
            return;
        }
    }
    persons = personService.search(keyword);
}
```

【分析】
Managed Bean にビジネスロジック（バリデーション）が含まれています。
これは以下の問題があります:
1. 単体テストが困難
2. 再利用性が低い
3. Service層との責務が不明確

【推奨アクション】
以下のリファクタリングを提案：

1. バリデーションロジックを Service または Validator に移動
2. Managed Bean は薄いラッパーに徹する
3. 移動後、Service のテストを追加

リファクタリング例:
```java
// PersonListBean.java（Managed Bean）
public void search() {
    persons = personService.search(keyword);  // シンプルに
}

// PersonService.java（Service）
public List<Person> search(String keyword) {
    // バリデーション
    if (keyword != null && !keyword.matches("[a-zA-Z0-9]+")) {
        throw new ValidationException("不正な文字が含まれています");
    }
    return personDao.search(keyword);
}
```

このリファクタリングを実行しますか？
A. 実行する
B. このまま（Managed Bean のテストを追加）
C. 詳細を確認
```

---

## レポートの保存

Jakarta EE API版と同じ形式でレポートを保存:
* JSON形式: `build/reports/test-analysis/test_analysis_report.json`
* Markdown形式: `build/reports/test-analysis/test_analysis_report.md`
* HTML: `build/reports/tests/test/index.html`
* JaCoCo: `build/reports/jacoco/test/html/index.html`

---

## 注意事項

### JSF特有の考慮事項

* Managed Bean は基本的にカバレッジ除外
* ビジネスロジックは Service 層に集約
* E2Eテスト（Playwright）との役割分担を意識
* バリデーションロジックは Bean Validation または Service で実装

### カバレッジ除外の確認

デフォルトで除外されるクラス:
* Managed Bean（`**/*Bean.java`）
* DTOクラス（`*Dto.java`、`**/dto/**`）
* Recordクラス（`*Record.java`）
* 自動生成コード（`**/generated/**`）

除外設定は `build.gradle` で確認できる:
```gradle
jacocoTestReport {
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                '**/*Bean.class',
                '**/dto/**',
                '**/*Dto.class',
                '**/*Record.class',
                '**/generated/**'
            ])
        }))
    }
}
```

---

## 参考資料

* [code_generation.md](code_generation.md) - コード生成インストラクション
* [detailed_design.md](detailed_design.md) - 詳細設計インストラクション
* [e2e_test_generation.md](e2e_test_generation.md) - E2Eテスト生成インストラクション
* [Jakarta EE API版 unit_test_execution.md](../../jakarta-ee-api-base/instructions/unit_test_execution.md) - 詳細な実行フロー
