# [DOMAIN_NAME] - ドメイン振る舞い仕様書（結合テスト用）

テンプレートパス: templates/basic_design/behaviors.md  
コピー先: {spec_directory}/basic_design/[DOMAIN_NAME]/behaviors.md  
ドメイン名: [DOMAIN_NAME]  
バージョン: 1.0.0  
最終更新日: [DATE]

記法: シナリオは Gherkin 記法で記述する（Feature, Scenario, Given, When, Then, And, But）。principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照すること。

---

## 1. 概要

本文書は、[DOMAIN_NAME]ドメインの基本設計を外形的に捉えた振る舞い仕様書である。結合テスト用のシナリオを記述し、Service層以下（Service + DAO + Entity + DB）の受入基準を定義する。

テスト対象:
* ドメイン内のService層のビジネスロジック
* ドメイン内のDAO層のデータアクセス
* Entity（JPA）のマッピング
* 実際のDB操作（メモリDB）
* 外部API呼び出し（WireMockでスタブ化）

テスト対象外:
* API層（Resource、JAX-RS） → requirements/behaviors.mdで記述（E2Eテスト）
* HTTPリクエスト/レスポンス → requirements/behaviors.mdで記述（E2Eテスト）
* 認証・認可（JWT、Cookie） → requirements/behaviors.mdで記述（E2Eテスト）

関連ドキュメント:
* [../../requirements/behaviors.md](../../requirements/behaviors.md) - E2Eテスト用の振る舞い仕様書（API層を含む全体）
* [../../requirements/requirements.md](../../requirements/requirements.md) - 要件定義書
* [functional_design.md](functional_design.md) - ドメイン機能設計書
* [../common/architecture_design.md](../common/architecture_design.md) - アーキテクチャ設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書

---

## 2. ドメイン内のビジネスロジックシナリオ

### 2.1 [Service名] - [サービス説明]

#### Feature: [機能名]

#### Scenario: [シナリオタイトル]

* Given（前提条件）:
  * [DBの初期状態を記述]
  * [外部APIのスタブ設定を記述]
  * [その他の前提条件]

* When（操作）:
  * [Serviceメソッドを呼び出す操作を記述]

* Then（期待結果）:
  * [期待されるビジネスロジックの結果を記述]
  * [DBの期待される状態を記述]
  * [外部APIへの期待される呼び出しを記述]

* And（追加の検証）:
  * [追加の検証項目]

#### テストデータ
* 初期データ:
  ```sql
  [DBの初期データを記述]
  ```
* 期待されるデータ:
  ```sql
  [テスト後のDBの期待状態を記述]
  ```

---

### 2.2 [Service名] - [エラーハンドリングシナリオ]

#### Feature: [機能名]

#### Scenario: [エラーシナリオタイトル]

* Given（前提条件）:
  * [エラーを引き起こす前提条件を記述]

* When（操作）:
  * [Serviceメソッドを呼び出す操作を記述]

* Then（期待結果）:
  * [期待される例外の種類を記述]
  * [期待されるエラーメッセージを記述]
  * [DBの期待される状態（ロールバック等）を記述]

---

## 3. DAO層のデータアクセスシナリオ

### 3.1 [Dao名] - [Dao説明]

#### Feature: [機能名]

#### Scenario: [シナリオタイトル]

* Given（前提条件）:
  * [DBの初期状態を記述]

* When（操作）:
  * [Daoメソッドを呼び出す操作を記述]

* Then（期待結果）:
  * [期待されるデータ取得結果を記述]
  * [期待されるデータ更新結果を記述]

---

## 4. トランザクション管理シナリオ

### 4.1 [トランザクションシナリオ名]

#### Feature: [機能名]

#### Scenario: [トランザクションのコミット/ロールバックシナリオ]

* Given（前提条件）:
  * [トランザクション開始前の状態]

* When（操作）:
  * [トランザクション内の操作を記述]

* Then（期待結果）:
  * [コミット/ロールバックの期待される結果を記述]
  * [DBの期待される状態を記述]

---

## 5. 外部API連携シナリオ（該当する場合）

### 5.1 [外部API名] - [外部API説明]

#### Feature: [機能名]

#### Scenario: [外部API連携シナリオ]

* Given（前提条件）:
  * [WireMockのスタブ設定を記述]

* When（操作）:
  * [外部API呼び出しを含むServiceメソッドを呼び出す]

* Then（期待結果）:
  * [期待される外部APIへのリクエストを記述]
  * [期待される外部APIからのレスポンス処理を記述]

---

## 6. 受入基準

### 6.1 機能要件
- [ ] すべてのビジネスロジックシナリオが成功する
- [ ] すべてのデータアクセスシナリオが成功する
- [ ] すべてのトランザクション管理シナリオが成功する
- [ ] すべての外部API連携シナリオが成功する

### 6.2 品質要件
- [ ] テストカバレッジがService層で80%以上
- [ ] テストカバレッジがDAO層で80%以上
- [ ] すべての異常系が適切にハンドリングされる
- [ ] トランザクション境界が正しく設定されている

---

## 7. 参考資料

* [functional_design.md](functional_design.md) - ドメイン機能設計書
* [../../detailed_design/[DOMAIN_NAME]/behaviors.md](../../detailed_design/[DOMAIN_NAME]/behaviors.md) - 単体テスト用の振る舞い仕様書
* [../../requirements/behaviors.md](../../requirements/behaviors.md) - E2Eテスト用の振る舞い仕様書
* [../common/architecture_design.md](../common/architecture_design.md) - アーキテクチャ設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書
