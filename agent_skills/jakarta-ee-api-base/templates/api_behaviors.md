# [API_ID] - API振る舞い仕様書

ファイル名: behaviors.md（api/[API_ID]/配下）  
API ID: [API_ID]  
API名: [API_NAME]  
バージョン: 1.0.0  
最終更新日: [DATE]

---

## 1. 概要

本文書は、[API_NAME] API固有の振る舞い、テストシナリオ、受入基準を記述する。

* システム全体の振る舞いや共通処理の振る舞いは、[../../system/behaviors.md](../../system/behaviors.md)を参照すること

* 関連ドキュメント:
  * [functional_design.md](functional_design.md) - API機能設計書
  * [detailed_design.md](detailed_design.md) - API詳細設計書
  * [../../system/functional_design.md](../../system/functional_design.md) - システム機能設計書

---

## 2. API固有のシナリオ

### 2.1 [エンドポイント名1]

#### シナリオ: [シナリオ名]

* Given（前提条件）:
  * [前提条件1]
  * [前提条件2]

* When（操作）:
  * [METHOD] `/api/[path]/[endpoint]`
  * リクエストボディ: `[データ]`

* Then（期待結果）:
  * HTTPステータス: [STATUS_CODE]
  * レスポンスボディ: `[データ]`
  * データベース状態: [期待される状態]

---

### 2.2 [エンドポイント名2]

[必要に応じて追加のシナリオを記述]

---

## 3. API固有のエラーケース

### 3.1 バリデーションエラー

| シナリオID | 説明 | Given | When | Then |
|-----------|------|-------|------|------|
| ERR-VAL-001 | [シナリオ] | [前提条件] | [操作] | 400 Bad Request, [メッセージ] |

### 3.2 ビジネスエラー

| シナリオID | 説明 | Given | When | Then |
|-----------|------|-------|------|------|
| ERR-BIZ-001 | [シナリオ] | [前提条件] | [操作] | 409 Conflict, [メッセージ] |

### 3.3 システムエラー

| シナリオID | 説明 | Given | When | Then |
|-----------|------|-------|------|------|
| ERR-SYS-001 | [シナリオ] | [前提条件] | [操作] | 500 Internal Server Error, [メッセージ] |

---

## 4. API固有の受入基準

### 4.1 機能受入基準

| 基準ID | 説明 | 受入基準 |
|--------|------|---------|
| AC-[XXX]-001 | [基準名] | [受入基準の詳細] |
| AC-[XXX]-002 | [基準名] | [受入基準の詳細] |

### 4.2 パフォーマンス受入基準

| 基準ID | 説明 | 受入基準 |
|--------|------|---------|
| PERF-[XXX]-001 | [API名]のレスポンスタイム | [XXX]ms以内（95パーセンタイル） |

### 4.3 セキュリティ受入基準

| 基準ID | 説明 | 受入基準 |
|--------|------|---------|
| SEC-[XXX]-001 | [セキュリティ要件] | [受入基準] |

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

* [functional_design.md](functional_design.md) - API機能設計書
* [detailed_design.md](detailed_design.md) - API詳細設計書
* [../../system/behaviors.md](../../system/behaviors.md) - システム振る舞い仕様書
* [../../system/functional_design.md](../../system/functional_design.md) - システム機能設計書
