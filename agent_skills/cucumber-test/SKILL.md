---
name: cucumber-test
description: Jakarta EEプロジェクトのCucumber BDD結合テストを自動生成。Gherkin形式でビジネス要件を記述し、Step Definitionsを実装。CDI（Weld SE）統合、データベーステスト、トランザクション管理対応。
---

# Cucumber BDD結合テスト生成 Agent Skill

## 使い方

### ステップ1: Featureファイル生成（behaviors.mdから）

```
@agent_skills/cucumber-test/instructions/generate_feature_from_behaviors.md

behaviors.mdからFeatureファイルを生成してください

パラメータ
* project_path: <プロジェクトルートパス>
* behaviors_file: <behaviors.mdファイルのパス（省略可、自動検索）>
* output_same_location: true  # behaviors.mdと同じ場所に出力
```

AIが自動で以下を実行
1. プロジェクト内のbehaviors.mdファイルを検索
2. behaviors.mdを解析（Gherkin形式または自然言語形式）
3. Gherkin形式の.featureファイルを生成
4. behaviors.mdと同じディレクトリに保存

**特徴**: 既存のbehaviors.md（振る舞い仕様書）から、Cucumber用の.featureファイルを自動生成します。

### ステップ2: Cucumberテストコード生成

```
@agent_skills/cucumber-test/instructions/generate_cucumber_tests.md

プロジェクトのCucumber結合テストを生成してください

パラメータ
* project_path: <プロジェクトルートパス>
* package_root: <ベースパッケージ名（例: pro.kensait.berrybooks）>
* feature_file: <Featureファイルのパス（省略可）>
* test_output_dir: <テスト出力ディレクトリ（省略可、デフォルト: {project_path}/src/test/java）>
```

AIが自動で以下を実行
1. Featureファイル（Gherkin）の解析または生成
2. Step Definitionsクラスの生成
3. テストランナークラスの生成
4. テストコンテキスト管理クラスの生成
5. README（テスト実行方法）の生成

**特徴**: Gherkin形式のFeatureファイルから、JUnit 5 + Weld SEを使用した結合テストコードを自動生成します。

---

## 実践例

### 例1: Berry Books APIの結合テスト生成

```
@agent_skills/cucumber-test/instructions/generate_cucumber_tests.md

Berry Books APIの注文処理に関するCucumber結合テストを生成してください

パラメータ
* project_path: projects/master/bookstore/berry-books-api
* package_root: pro.kensait.berrybooks
```

AIが自動で実行:
1. ビジネス要件を分析してFeatureファイルを生成
2. Step Definitionsを生成
3. CDI Beanの起動とトランザクション管理コードを生成
4. テストランナーを生成

生成されるテスト:
- `order_management.feature` - 注文管理のシナリオ（Gherkin）
- `OrderManagementSteps.java` - ステップ定義
- `CucumberIntegrationTestRunner.java` - JUnit 5テストランナー
- `TestContext.java` - テストコンテキスト管理

---

## 対応する主要機能

* Gherkin形式でのビジネスシナリオ記述
  - Given-When-Then構文
  - データテーブル
  - シナリオアウトライン
* JUnit 5統合
  - `@Suite`アノテーション
  - Cucumber JUnit Platform Engine
* CDI統合（Weld SE）
  - CDI Beanの起動とシャットダウン
  - `@Inject`による依存性注入
  - `@ApplicationScoped` Beanのテスト
* データベーステスト
  - トランザクション管理
  - テストデータのセットアップ
  - テスト後のロールバック
* カスタムステップ定義
  - パラメータ化
  - 共通ステップの再利用

## 重要な注意事項

### 結合テストの位置づけ

Cucumberテストは**結合テスト**（Integration Test）として実装します：

```
単体テスト（Unit Test）- Mockitoでモック化
    ↓
結合テスト（Integration Test）- Cucumberで実シナリオ検証
    ↓
E2Eテスト（End-to-End Test）- PlaywrightでUI検証
```

### CDI Beanのライフサイクル

- Weld SEを使用してCDI Beanを起動
- 各テストシナリオごとにトランザクションをロールバック
- テストコンテキストでEntityManagerを管理

### データベーステスト

- 本番と同じJPA設定を使用
- テスト用persistence.xmlで設定
- トランザクションロールバックでデータの独立性を保証

---

## ディレクトリ構造

```
agent_skills/cucumber-test/
├── SKILL.md                          # このファイル
├── README.md                         # クイックスタートガイド
├── principles/                       # 原則（全プロジェクト共通）
│   └── cucumber_best_practices.md   # Cucumberベストプラクティス
├── templates/                        # テンプレート
│   └── cucumber_feature_template.md # Featureファイルテンプレート
└── instructions/
    ├── generate_feature_from_behaviors.md  # Featureファイル生成指示（behaviors.mdから）
    └── generate_cucumber_tests.md          # 完全なテスト生成指示
```

---

## 参考資料

* [Cucumber 公式ドキュメント](https://cucumber.io/docs/cucumber/)
* [Gherkin Reference](https://cucumber.io/docs/gherkin/reference/)
* [Cucumberベストプラクティス](principles/cucumber_best_practices.md)
* [Featureファイルテンプレート](templates/cucumber_feature_template.md)
