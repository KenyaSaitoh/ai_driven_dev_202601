# API_003_categories - カテゴリAPI受入基準

* API ID: API_003_categories  
* API名: カテゴリAPI  
* ベースパス: `/api/categories`  
* バージョン: 2.0.0  
* 最終更新日: 2025-01-02

---

## 1. 概要

本文書は、カテゴリAPIの受入基準を記述する。各エンドポイントについて、正常系・異常系の振る舞いを定義し、テストシナリオの基礎とする。

---

## 2. カテゴリ一覧取得 (`GET /api/categories`)

### 2.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| CAT-LIST-001 | すべてのカテゴリを取得できる | カテゴリが登録されている | /api/categoriesにリクエスト | 200 OK<br/>カテゴリ一覧が配列形式で返される |
| CAT-LIST-002 | カテゴリがcategoryId昇順でソートされる | 複数のカテゴリが登録されている | /api/categoriesにリクエスト | 200 OK<br/>categoryId昇順で返される |
| CAT-LIST-003 | レスポンス形式が配列 | カテゴリが登録されている | /api/categoriesにリクエスト | 200 OK<br/>[{categoryId: 1, categoryName: "文学"}, ...] |
| CAT-LIST-004 | カテゴリが0件の場合、空配列を返す | カテゴリが登録されていない | /api/categoriesにリクエスト | 200 OK<br/>空配列 [] が返される |

---

## 3. データ整合性受入基準

### 3.1 データ内容

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| DATA-001 | categoryIdとcategoryNameが正しく返される | カテゴリ"文学"(ID=1)が存在 | /api/categoriesにリクエスト | 200 OK<br/>{categoryId: 1, categoryName: "文学"}が含まれる |
| DATA-002 | すべてのカテゴリが含まれる | 4件のカテゴリが存在 | /api/categoriesにリクエスト | 200 OK<br/>4件すべてが返される |

---

## 4. API比較受入基準

### 4.1 `/api/categories` vs `/api/books/categories`

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| COMP-001 | `/api/categories`は配列形式 | - | /api/categoriesにリクエスト | 200 OK<br/>[{categoryId, categoryName}] |
| COMP-002 | `/api/books/categories`はMap形式 | - | /api/books/categoriesにリクエスト | 200 OK<br/>{categoryName: categoryId} |
| COMP-003 | 同じデータを異なる形式で返す | - | 両APIにリクエスト | 同じカテゴリ情報が異なる形式で返される |

---

## 5. パフォーマンス受入基準

### 5.1 レスポンスタイム

| シナリオID | API | 受入基準 |
|-----------|-----|---------|
| PERF-001 | GET /api/categories | 50ms以内（95パーセンタイル） |

### 5.2 キャッシング効果（将来実装時）

| シナリオID | 説明 | 受入基準 |
|-----------|------|---------|
| CACHE-001 | 初回リクエストはDB問い合わせ | 初回レスポンスタイム: 50ms |
| CACHE-002 | 2回目以降はキャッシュから返却 | 2回目以降レスポンスタイム: 5ms以内 |
| CACHE-003 | キャッシュTTLは適切に設定 | キャッシュTTL: 1時間（マスタデータのため） |

---

## 6. ログ出力受入基準

### 6.1 ログレベル

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| LOG-001 | カテゴリ一覧取得時はINFOレベルでログ出力 | - | /api/categories | INFO: [CategoryResource#getAllCategories]<br/>INFO: [CategoryService#getCategoriesAll] |
| LOG-002 | 取得成功時は件数を出力 | 4件のカテゴリが存在 | /api/categories | INFO: [CategoryResource#getAllCategories] Success: 4 categories returned |
| LOG-003 | DB接続エラー時はERRORレベル | DB接続不可 | /api/categories | ERROR: [CategoryDao#findAll] Database error |

---

## 7. 並行処理受入基準

### 7.1 同時リクエスト

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| CONC-001 | 複数ユーザーが同時にカテゴリ一覧を取得できる | - | 3ユーザーが同時に/api/categories | 全リクエストが成功<br/>独立したトランザクション |
| CONC-002 | 読み取り専用のため競合なし | - | 同時に/api/categories | データ競合なし<br/>ロック不要 |
| CONC-003 | 大量リクエスト時も安定 | - | 1000件の同時リクエスト | 全リクエストが3秒以内に完了<br/>平均レスポンスタイム: 150ms |

---

## 8. 将来の拡張受入基準

### 8.1 CRUD操作（実装予定）

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| CRUD-001 | カテゴリ詳細取得 | categoryId=1が存在 | GET /api/categories/1 | 200 OK<br/>{categoryId: 1, categoryName: "文学"} |
| CRUD-002 | カテゴリ作成 | - | POST /api/categories<br/>{categoryName: "新カテゴリ"} | 201 Created<br/>Location: /api/categories/5 |
| CRUD-003 | カテゴリ更新 | categoryId=1が存在 | PUT /api/categories/1<br/>{categoryName: "更新"} | 200 OK |
| CRUD-004 | カテゴリ削除 | categoryId=1が存在 | DELETE /api/categories/1 | 200 OK |

### 8.2 統計情報（実装予定）

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| STATS-001 | カテゴリ別書籍数を取得 | - | GET /api/categories?includeStats=true | 200 OK<br/>[{categoryId: 1, categoryName: "文学", bookCount: 150}] |

---

## 9. マスタデータ管理受入基準

### 9.1 データ安定性

| シナリオID | 説明 | 受入基準 |
|-----------|------|---------|
| MASTER-001 | カテゴリマスタは変更頻度が低い | 月1回未満の更新 |
| MASTER-002 | カテゴリ削除は論理削除 | 物理削除せず、フラグで管理 |
| MASTER-003 | キャッシング推奨 | マスタデータのためキャッシュ適用を推奨 |

---

## 10. 関連ドキュメント

* [functional_design.md](functional_design.md) - カテゴリAPI機能設計書
* [../../system/behaviors.md](../../system/behaviors.md) - 全体受入基準
* [../../system/architecture_design.md](../../system/architecture_design.md) - アーキテクチャ設計書
