# [DOMAIN_NAME] - 振る舞い仕様書（単体テスト用）

テンプレートパス: templates/detailed_design/behaviors.md  
コピー先: {spec_directory}/detailed_design/[DOMAIN_NAME]/behaviors.md  
ドメイン名: [DOMAIN_NAME]  
バージョン: 1.0.0  
最終更新日: [DATE]

記法: シナリオは Gherkin 記法で記述する（Feature, Scenario, Given, When, Then, And, But）。principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照すること。

---

## 1. 概要

本文書は、[DOMAIN_NAME]ドメインの単体テスト用の振る舞い、テストシナリオ、受入基準を記述する。

テスト対象:
* Resource（JAX-RS）
* Service（ビジネスロジック）
* Dao（データアクセス）
* セキュリティコンポーネント
* ユーティリティ

単体テストの範囲:
* ドメイン粒度内の機能をテスト
* ドメイン内のコンポーネント間は実際の連携をテスト可能
* ドメイン外の依存（他ドメインのService、EntityManager、外部API等）はモック化
* 結合テストシナリオは ../../basic_design/[DOMAIN_NAME]/behaviors.md を参照すること
* E2Eテストシナリオは ../../requirements/behaviors.md を参照すること

関連ドキュメント:
* [detailed_design.md](detailed_design.md) - 詳細設計書
* [../../basic_design/[DOMAIN_NAME]/functional_design.md](../../basic_design/[DOMAIN_NAME]/functional_design.md) - ドメイン機能設計書
* [../../basic_design/[DOMAIN_NAME]/behaviors.md](../../basic_design/[DOMAIN_NAME]/behaviors.md) - ドメイン振る舞い仕様書（結合テスト用）

---

## 2. テストシナリオ

注意: このテンプレートは、Resource、Service、Dao、セキュリティコンポーネント、ユーティリティなど、あらゆるコンポーネントの単体テストに使用できます。

### 2.1 [コンポーネント名] - [テストケース名1]

#### Feature: [機能名]

#### Scenario: [シナリオ名]

* Given（前提条件）:
  * [前提条件1]
  * [前提条件2]
  * モック設定: [ドメイン外の依存のモック設定]

* When（操作）:
  * [テスト対象のメソッド呼び出しを記述]

* Then（期待結果）:
  * [期待される戻り値を記述]
  * [期待される状態変化を記述]

* And（追加の検証）:
  * [追加の検証項目]

#### テストデータ
* 入力:
  ```
  [入力データの例]
  ```
* 期待される出力:
  ```
  [期待される出力データの例]
  ```

---

### 2.2 [コンポーネント名] - [テストケース名2: 異常系]

#### Feature: [機能名]

#### Scenario: [エラーシナリオ名]

* Given（前提条件）:
  * [エラーを引き起こす前提条件]
  * モック設定: [エラーを返すモック設定]

* When（操作）:
  * [テスト対象のメソッド呼び出しを記述]

* Then（期待結果）:
  * [期待される例外の種類]
  * [期待されるエラーメッセージ]

---

### 2.3 [コンポーネント名] - [テストケース名3: 境界値]

#### Feature: [機能名]

#### Scenario: [境界値シナリオ名]

* Given（前提条件）:
  * [境界値の前提条件]

* When（操作）:
  * [境界値を使用したメソッド呼び出し]

* Then（期待結果）:
  * [境界値での期待される動作]

#### テストデータ（境界値）
* 最小値: [最小値]
* 最大値: [最大値]
* null: [nullの扱い]
* 空文字列: [空文字列の扱い]

---

## 3. モック化の方針

### 3.1 ドメイン内の依存関係
* [同じドメイン内のコンポーネント] → モック不要（実際の連携をテスト）

### 3.2 ドメイン外の依存関係
* [他ドメインのService] → モック化
* EntityManager → モック化
* [外部APIクライアント] → モック化

---

## 4. カバレッジ目標

* ステートメントカバレッジ: 80%以上
* ブランチカバレッジ: 70%以上

---

## 5. 受入基準

### 5.1 機能要件
- [ ] すべての正常系テストが成功する
- [ ] すべての異常系テストが成功する
- [ ] すべての境界値テストが成功する

### 5.2 品質要件
- [ ] カバレッジ目標を達成する
- [ ] テストコードにコメントが適切に記載されている
- [ ] テストケースが独立している（テスト間の依存関係がない）

---

## 6. 参考資料

* [detailed_design.md](detailed_design.md) - 詳細設計書
* [../../basic_design/[DOMAIN_NAME]/functional_design.md](../../basic_design/[DOMAIN_NAME]/functional_design.md) - ドメイン機能設計書
* [../../basic_design/[DOMAIN_NAME]/behaviors.md](../../basic_design/[DOMAIN_NAME]/behaviors.md) - ドメイン振る舞い仕様書（結合テスト用）
* [../../requirements/behaviors.md](../../requirements/behaviors.md) - システム振る舞い仕様書（E2Eテスト用）
