# [DOMAIN_NAME] - ドメイン振る舞い仕様書（結合テスト用）

テンプレートパス: templates/basic_design/behaviors.md  
コピー先: {spec_directory}/basic_design/[DOMAIN_NAME]/behaviors.md  
ドメイン名: [DOMAIN_NAME]  
バージョン: 1.0.0  
最終更新日: [DATE]

記法: シナリオは Gherkin 記法で記述する（Feature, Scenario, Given, When, Then, And, But）。principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照すること。

**DBUnit対応:**
* Given句: DB初期状態を明記（テーブル名、件数、データ、対応データセット）
* Then句: DB更新後の期待状態を明記（テーブル名、件数、差分、データ、対応データセット）
* 検証条件: 主キー、外部キー、CASCADE、トランザクションロールバックを明記

---

## 1. 概要

本文書は、[DOMAIN_NAME]ドメインの基本設計を外形的に捉えた振る舞い仕様書である。結合テスト用のシナリオを記述し、Service層以下（Service + DAO + Entity + DB）の受入基準を定義する。

**DBUnitによるテストデータ管理:**
* テストデータは XML/CSV 形式で外部管理（`src/test/resources/datasets/[DOMAIN_NAME]/`配下）
* Given句でDB初期状態を明記、対応するXMLデータセットを参照
* Then句でDB更新後の期待状態を明記、対応するXMLデータセットを参照
* データベースの状態を明示的に検証

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

```gherkin
Scenario: [シナリオタイトル]
  Given DBに以下のデータが存在する:
    テーブル: [TABLE_NAME]
    件数: N件
    データセット: /datasets/[DOMAIN_NAME]/initial-[scenario].xml
    データ:
      | COLUMN1 | COLUMN2 | COLUMN3 |
      | 値1     | 値2     | 値3     |
  
  And [外部APIのスタブ設定を記述]:
    | Method | Path | Response |
    | GET    | /api | {...}    |
  
  When [Service名].[メソッド名]([パラメータ])を呼び出す
  
  Then DBのテーブルは以下になる:
    テーブル: [TABLE_NAME]
    件数: M件（±差分）
    データセット: /datasets/[DOMAIN_NAME]/expected-[scenario].xml
    データ:
      | COLUMN1 | COLUMN2 | COLUMN3 |
      | 値1     | 値2     | 値3     |
    検証:
      - [主キー・外部キー・制約の検証条件]
      - [CASCADE動作の検証]
      - [トランザクション動作の検証]
  
  And [外部APIへの期待される呼び出しを記述]
```

**記述ルール:**

**Given句（DB初期状態）:**
* テーブル名を大文字で明記（例: ORDER_TRAN）
* 件数を明記（例: 0件、1件、2件）
* データセットファイルパスを明記（例: `/datasets/orders/initial-create.xml`）
* データをテーブル形式で記述（NULL値は `[null]`）

**Then句（DB期待状態）:**
* テーブル名を大文字で明記
* 件数と差分を明記（例: 1件（+1件追加）、0件（-2件削除）、1件（変更なし））
* データセットファイルパスを明記（例: `/datasets/orders/expected-created.xml`）
* 更新後の全行をテーブル形式で記述
* 検証条件を明記（主キー、外部キー、CASCADE、トランザクション）

---

### 2.2 [Service名] - [エラーハンドリングシナリオ]

#### Feature: [機能名]

```gherkin
Scenario: [エラーシナリオタイトル]
  Given DBに以下のデータが存在する:
    テーブル: [TABLE_NAME]
    件数: N件
    データセット: /datasets/[DOMAIN_NAME]/initial-error-case.xml
  
  And [エラーを引き起こす条件を記述]
  
  When [Service名].[メソッド名]([パラメータ])を呼び出す
  
  Then [ExceptionName]がスローされる
  
  And DBの状態は変化しない:
    テーブル: [TABLE_NAME]
    件数: N件（変更なし）
    検証:
      - トランザクションがロールバックされる
      - 例外発生前の状態が保持される
```

---

## 3. DAO層のデータアクセスシナリオ

### 3.1 [Dao名] - [Dao説明]

#### Feature: [機能名]

```gherkin
Scenario: [シナリオタイトル]
  Given DBに以下のデータが存在する:
    テーブル: [TABLE_NAME]
    件数: N件
    データセット: /datasets/[DOMAIN_NAME]/initial-dao-test.xml
    データ:
      | COLUMN1 | COLUMN2 | COLUMN3 |
      | 値1     | 値2     | 値3     |
  
  When [Dao名].[メソッド名]([パラメータ])を呼び出す
  
  Then [期待される件数]件のデータが返される
  
  And DBの状態は変化しない:
    テーブル: [TABLE_NAME]
    件数: N件（変更なし）
    検証:
      - READ操作のため、DBは更新されない
```

**CREATE操作の例:**
```gherkin
Scenario: [データ作成シナリオ]
  Given DBのテーブルは空である:
    テーブル: [TABLE_NAME]
    件数: 0件
  
  When [Dao名].create([エンティティ])を呼び出す
  
  Then DBのテーブルは以下になる:
    テーブル: [TABLE_NAME]
    件数: 1件（+1件追加）
    データセット: /datasets/[DOMAIN_NAME]/expected-dao-created.xml
    検証:
      - 主キーが自動採番される
      - NOT NULL制約を満たす
      - 外部キー制約を満たす
```

**UPDATE操作の例:**
```gherkin
Scenario: [データ更新シナリオ]
  Given DBに以下のデータが存在する:
    テーブル: [TABLE_NAME]
    件数: 1件
    データセット: /datasets/[DOMAIN_NAME]/initial-before-update.xml
    データ:
      | ID | STATUS  |
      | 1  | PENDING |
  
  When [Dao名].update(id=1, status="COMPLETED")を呼び出す
  
  Then DBのテーブルは以下になる:
    テーブル: [TABLE_NAME]
    件数: 1件（変更なし）
    データセット: /datasets/[DOMAIN_NAME]/expected-after-update.xml
    データ:
      | ID | STATUS    |
      | 1  | COMPLETED |
    検証:
      - ID=1 の STATUS が更新される
      - その他のカラムは変更されない
```

**DELETE操作の例（CASCADE削除を含む）:**
```gherkin
Scenario: [データ削除シナリオ - CASCADE削除]
  Given DBに親データが存在する:
    テーブル: PARENT_TABLE
    件数: 1件
    データ:
      | PARENT_ID | NAME |
      | 1         | Test |
  
  And DBに子データが存在する:
    テーブル: CHILD_TABLE
    件数: 2件
    データ:
      | CHILD_ID | PARENT_ID | VALUE |
      | 1        | 1         | A     |
      | 2        | 1         | B     |
  
  When [Dao名].delete(parentId=1)を呼び出す
  
  Then DBの親テーブルは以下になる:
    テーブル: PARENT_TABLE
    件数: 0件（-1件削除）
  
  And DBの子テーブルは以下になる:
    テーブル: CHILD_TABLE
    件数: 0件（-2件削除）
    検証:
      - PARENT_ID=1 に紐づく子データがすべて削除される（CASCADE DELETE）
```

---

## 4. トランザクション管理シナリオ

### 4.1 [トランザクションシナリオ名]

#### Feature: [機能名]

```gherkin
Scenario: [トランザクションコミット]
  Given DBに以下のデータが存在する:
    テーブル: [TABLE_NAME]
    件数: N件
    データセット: /datasets/[DOMAIN_NAME]/initial-transaction.xml
  
  When [Service名].[メソッド名]([パラメータ])を呼び出す
  
  Then トランザクションがコミットされる
  
  And DBのテーブルは以下になる:
    テーブル: [TABLE_NAME]
    件数: M件（±差分）
    データセット: /datasets/[DOMAIN_NAME]/expected-committed.xml
    検証:
      - 全ての変更がコミットされる
      - データの整合性が保たれる
```

```gherkin
Scenario: [トランザクションロールバック]
  Given DBに以下のデータが存在する:
    テーブル: [TABLE_NAME]
    件数: N件
    データセット: /datasets/[DOMAIN_NAME]/initial-rollback.xml
  
  When [Service名].[メソッド名]([パラメータ])を呼び出す
  
  Then [ExceptionName]がスローされる
  
  And DBの状態は変化しない:
    テーブル: [TABLE_NAME]
    件数: N件（変更なし）
    検証:
      - トランザクションがロールバックされる
      - 全ての変更が取り消される
      - 例外発生前の状態が保持される
```

---

## 5. 外部API連携シナリオ（該当する場合）

### 5.1 [外部API名] - [外部API説明]

#### Feature: [機能名]

```gherkin
Scenario: [外部API連携シナリオ]
  Given WireMockが外部APIをスタブする:
    | Method | Path           | Response     |
    | GET    | /api/resource  | {data: ...}  |
    | PUT    | /api/resource  | {success: OK}|
  
  And DBに以下のデータが存在する:
    テーブル: [TABLE_NAME]
    件数: N件
    データセット: /datasets/[DOMAIN_NAME]/initial-api-call.xml
  
  When [Service名].[メソッド名]([パラメータ])を呼び出す
  
  Then 外部APIが呼び出される:
    リクエスト: [Method] [Path]
    ボディ: [Request Body]
  
  And DBのテーブルは以下になる:
    テーブル: [TABLE_NAME]
    件数: M件（±差分）
    データセット: /datasets/[DOMAIN_NAME]/expected-after-api-call.xml
    検証:
      - 外部APIのレスポンスに基づいてDBが更新される
      - データの整合性が保たれる
```

```gherkin
Scenario: [外部APIエラー時のロールバック]
  Given WireMockが外部APIをスタブする:
    | Method | Path          | Response                |
    | GET    | /api/resource | 500エラー（Internal Error）|
  
  And DBに以下のデータが存在する:
    テーブル: [TABLE_NAME]
    件数: N件
    データセット: /datasets/[DOMAIN_NAME]/initial-api-error.xml
  
  When [Service名].[メソッド名]([パラメータ])を呼び出す
  
  Then [ExceptionName]がスローされる
  
  And DBの状態は変化しない:
    テーブル: [TABLE_NAME]
    件数: N件（変更なし）
    検証:
      - トランザクションがロールバックされる
      - 外部APIエラー時にDBの整合性が保たれる
```

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

## 7. DBUnitデータセット対応表

| シナリオ | 初期データセット | 期待データセット | 検証テーブル |
|---------|----------------|----------------|------------|
| [シナリオ1] | `/datasets/[DOMAIN_NAME]/initial-[scenario1].xml` | `/datasets/[DOMAIN_NAME]/expected-[scenario1].xml` | TABLE1<br>TABLE2 |
| [シナリオ2] | `/datasets/[DOMAIN_NAME]/initial-[scenario2].xml` | `/datasets/[DOMAIN_NAME]/expected-[scenario2].xml` | TABLE1 |
| [エラーケース] | `/datasets/[DOMAIN_NAME]/initial-error.xml` | （変更なし・ロールバック） | TABLE1<br>TABLE2 |

**データセット配置ルール:**
* ディレクトリ: `src/test/resources/datasets/[DOMAIN_NAME]/`
* 命名規則:
  * 初期データ: `initial-{scenario-name}.xml`
  * 期待データ: `expected-{scenario-name}.xml`
  * 共通マスター: `common-master-data.xml`

**データセット記述例（XML）:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<dataset>
  <TABLE_NAME COLUMN1="値1" COLUMN2="値2" COLUMN3="[null]" />
  <TABLE_NAME COLUMN1="値3" COLUMN2="値4" COLUMN3="値5" />
</dataset>
```

---

## 8. 参考資料

* [functional_design.md](functional_design.md) - ドメイン機能設計書
* [../../detailed_design/[DOMAIN_NAME]/behaviors.md](../../detailed_design/[DOMAIN_NAME]/behaviors.md) - 単体テスト用の振る舞い仕様書
* [../../requirements/behaviors.md](../../requirements/behaviors.md) - E2Eテスト用の振る舞い仕様書
* [../common/architecture_design.md](../common/architecture_design.md) - アーキテクチャ設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書
* DBUnit公式ドキュメント: http://dbunit.sourceforge.net/
* @agent_skills/jakarta-ee-api-base/instructions/it_generation.md - 結合テスト生成インストラクション
