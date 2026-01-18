# [TARGET_NAME] - 振る舞い仕様書（単体テスト用）

テンプレートパス: templates/detailed_design/behaviors.md  
コピー先: {spec_directory}/detailed_design/[TARGET]/behaviors.md  
ターゲット: [TARGET]  
バージョン: 1.0.0  
最終更新日: [DATE]

---

## 1. 概要

本文書は、[TARGET_NAME]の単体テスト用の振る舞い、テストシナリオ、受入基準を記述する。

ターゲットの種類:
* API（JAX-RSリソースクラス）
* サービス（ビジネスロジック、CDI Bean）
* DAO（データアクセスオブジェクト）
* ユーティリティ
* 画面（JSF Managed Bean、バッキングビーン）

単体テストの範囲:
* タスク粒度内の機能をテスト
* 外部依存（他のサービス、外部API、DBなど）はモック化
* E2Eテストシナリオは basic_design/behaviors.md を参照すること

* 関連ドキュメント:
  * [detailed_design.md](detailed_design.md) - 詳細設計書
  * [../../basic_design/functional_design.md](../../basic_design/functional_design.md) - 機能設計書
  * [../../basic_design/behaviors.md](../../basic_design/behaviors.md) - システム振る舞い仕様書（E2Eテスト用）

---

## 2. テストシナリオ

注意: このテンプレートは、API、サービス、DAO、ユーティリティ、画面など、あらゆるタスクの単体テストに使用できます。

### 2.1 [テストケース名1]

#### シナリオ: [シナリオ名]

* Given（前提条件）:
  * [前提条件1]
  * [前提条件2]
  * モック設定: [外部依存のモック設定]

* When（操作）:
  * [テスト対象の操作を記述]
  * 例（API）: `POST /api/books`、リクエストボディ: `{...}`
  * 例（サービス）: `BookService.createBook(bookDto)`
  * 例（DAO）: `BookDao.findById(bookId)`

* Then（期待結果）:
  * [期待される結果を記述]
  * 例（API）: HTTPステータス 201、レスポンス: `{...}`
  * 例（サービス）: 戻り値: `Book`オブジェクト、例外なし
  * 例（DAO）: Optional<Book>に値が存在、属性が一致
  * データベース状態: [期待される状態]

---

### 2.2 [テストケース名2]

[必要に応じて追加のシナリオを記述]

---

## 3. エラーケース

### 3.1 バリデーションエラー

| シナリオID | 説明 | Given | When | Then |
|-----------|------|-------|------|------|
| ERR-VAL-001 | [シナリオ] | [前提条件] | [操作] | 例（API）: 400 Bad Request / 例（サービス）: ValidationException |

### 3.2 ビジネスエラー

| シナリオID | 説明 | Given | When | Then |
|-----------|------|-------|------|------|
| ERR-BIZ-001 | [シナリオ] | [前提条件] | [操作] | 例（API）: 409 Conflict / 例（サービス）: BusinessException |

### 3.3 システムエラー

| シナリオID | 説明 | Given | When | Then |
|-----------|------|-------|------|------|
| ERR-SYS-001 | [シナリオ] | [前提条件] | [操作] | 例（API）: 500 Internal Server Error / 例（サービス）: RuntimeException |

---

## 4. 受入基準

### 4.1 機能受入基準

| 基準ID | 説明 | 受入基準 |
|--------|------|---------|
| AC-[XXX]-001 | [基準名] | [受入基準の詳細] |
| AC-[XXX]-002 | [基準名] | [受入基準の詳細] |

### 4.2 品質受入基準

| 基準ID | 説明 | 受入基準 |
|--------|------|---------|
| QUALITY-[XXX]-001 | [品質要件] | [受入基準] |

---

## 5. テストデータ

### 5.1 正常系テストデータ

| データID | 説明 | データ |
|---------|------|--------|
| TD-[XXX]-001 | [データ説明] | [データ内容] |

### 5.2 異常系テストデータ

| データID | 説明 | データ |
|---------|------|--------|
| TD-ERR-001 | [データ説明] | [データ内容] |

---

## 6. 参考資料

* [detailed_design.md](detailed_design.md) - 詳細設計書
* [../../basic_design/functional_design.md](../../basic_design/functional_design.md) - 機能設計書
* [../../basic_design/behaviors.md](../../basic_design/behaviors.md) - システム振る舞い仕様書（E2Eテスト用）
