# API_005_stocks - 在庫API受入基準

**API ID:** API_005_stocks  
**API名:** 在庫API  
**ベースパス:** `/api/stocks`  
**バージョン:** 2.0.0  
**最終更新日:** 2025-01-02

---

## 1. 概要

本文書は、在庫APIの受入基準を記述する。各エンドポイントについて、正常系・異常系の振る舞いを定義し、特に楽観的ロック制御に関するテストシナリオを詳細に記載する。

---

## 2. 在庫一覧取得 (`GET /api/stocks`)

### 2.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| STOCK-LIST-001 | すべての在庫情報を取得できる | 在庫情報が登録されている | /api/stocksにリクエスト | 200 OK<br/>在庫一覧が返される |
| STOCK-LIST-002 | 在庫情報にbookId, quantity, versionが含まれる | - | /api/stocksにリクエスト | 200 OK<br/>各在庫に{bookId, quantity, version}が含まれる |
| STOCK-LIST-003 | 在庫が0件の場合、空配列を返す | 在庫情報が登録されていない | /api/stocksにリクエスト | 200 OK<br/>空配列 [] が返される |

---

## 3. 在庫情報取得 (`GET /api/stocks/{bookId}`)

### 3.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| STOCK-GET-001 | 指定bookIdの在庫情報を取得できる | bookId=1の在庫が存在 | GET /api/stocks/1 | 200 OK<br/>{bookId: 1, quantity: 10, version: 1} |
| STOCK-GET-002 | バージョン番号が含まれる | bookId=1, version=5 | GET /api/stocks/1 | 200 OK<br/>version: 5が返される |

### 3.2 異常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| STOCK-GET-E001 | 存在しない書籍IDの場合、エラー | bookId=999が存在しない | GET /api/stocks/999 | 404 Not Found<br/>"在庫情報が見つかりません" |

---

## 4. 在庫更新 (`PUT /api/stocks/{bookId}`)

### 4.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| STOCK-UPDATE-001 | 正しいバージョンで在庫更新できる | bookId=1, quantity=10, version=1 | PUT /api/stocks/1<br/>{version: 1, quantity: 15} | 200 OK<br/>{bookId: 1, quantity: 15, version: 2}<br/>versionが自動インクリメント |
| STOCK-UPDATE-002 | 在庫数を増やせる | quantity=10 | PUT /api/stocks/1<br/>{version: 1, quantity: 20} | 200 OK<br/>quantity: 20に更新 |
| STOCK-UPDATE-003 | 在庫数を減らせる | quantity=10 | PUT /api/stocks/1<br/>{version: 1, quantity: 5} | 200 OK<br/>quantity: 5に更新 |
| STOCK-UPDATE-004 | 在庫数を0にできる | quantity=10 | PUT /api/stocks/1<br/>{version: 1, quantity: 0} | 200 OK<br/>quantity: 0に更新 |

### 4.2 異常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| STOCK-UPDATE-E001 | 存在しない書籍IDの場合、エラー | bookId=999が存在しない | PUT /api/stocks/999<br/>{version: 1, quantity: 10} | 404 Not Found<br/>"在庫情報が見つかりません" |
| STOCK-UPDATE-E002 | バージョンが一致しない場合、エラー | bookId=1, version=2 | PUT /api/stocks/1<br/>{version: 1, quantity: 15} | 409 Conflict<br/>"在庫が他のユーザーによって更新されました。再度お試しください。" |
| STOCK-UPDATE-E003 | バージョンが指定されていない場合、エラー | - | PUT /api/stocks/1<br/>{quantity: 15} | 400 Bad Request<br/>Bean Validationエラー |
| STOCK-UPDATE-E004 | 数量が指定されていない場合、エラー | - | PUT /api/stocks/1<br/>{version: 1} | 400 Bad Request<br/>Bean Validationエラー |
| STOCK-UPDATE-E005 | 負の数量は許可しない | - | PUT /api/stocks/1<br/>{version: 1, quantity: -5} | 400 Bad Request<br/>Bean Validationエラー |

---

## 5. 楽観的ロック受入基準

### 5.1 楽観的ロック制御

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| LOCK-001 | 楽観的ロックが正常に動作する | bookId=1, version=1 | PUT /api/stocks/1<br/>{version: 1, quantity: 15} | 200 OK<br/>version: 2に自動インクリメント |
| LOCK-002 | JPAの@Versionアノテーションが機能する | - | 在庫更新時 | UPDATE文にWHERE VERSION = ?が含まれる |
| LOCK-003 | バージョン不一致でOptimisticLockExceptionが発生 | version=2の時にversion=1で更新 | PUT /api/stocks/1<br/>{version: 1, quantity: 15} | 409 Conflict |

### 5.2 並行更新シナリオ

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| CONC-001 | 2ユーザーが同時に在庫取得 | bookId=1, quantity=10, version=1 | ユーザーAとBが同時に取得 | 両方とも{quantity: 10, version: 1}を取得 |
| CONC-002 | 先に更新したユーザーが成功 | 両ユーザーがversion=1を保持 | ユーザーAが先に更新 | ユーザーA: 200 OK, version=2 |
| CONC-003 | 後から更新したユーザーは失敗 | ユーザーAがversion=2に更新済み | ユーザーBがversion=1で更新 | ユーザーB: 409 Conflict |
| CONC-004 | 失敗後、再取得して再試行できる | ユーザーBが409受信 | 再取得(version=2)して再更新 | ユーザーB: 200 OK, version=3 |

---

## 6. トランザクション受入基準

### 6.1 トランザクション整合性

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| TRAN-001 | 更新はアトミック | - | PUT /api/stocks/1 | quantity更新とversion更新が同時にコミット |
| TRAN-002 | ロールバック時は変更されない | 更新中にエラー発生 | トランザクションロールバック | quantityもversionも変更されない |

---

## 7. パフォーマンス受入基準

### 7.1 レスポンスタイム

| シナリオID | API | 受入基準 |
|-----------|-----|---------|
| PERF-001 | GET /api/stocks | 500ms以内（95パーセンタイル） |
| PERF-002 | GET /api/stocks/{bookId} | 500ms以内（95パーセンタイル） |
| PERF-003 | PUT /api/stocks/{bookId} | 200ms以内（95パーセンタイル） |

---

## 8. ログ出力受入基準

### 8.1 ログレベル

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| LOG-001 | 在庫更新成功時はINFOレベル | - | PUT /api/stocks/1成功 | INFO: [StockResource#updateStock] Success: bookId=1, quantity=15, version=2 |
| LOG-002 | 楽観的ロック失敗時はWARNレベル | version不一致 | PUT /api/stocks/1失敗 | WARN: [StockResource#updateStock] Optimistic lock failure: bookId=1 |
| LOG-003 | 在庫が見つからない時はWARNレベル | bookId=999が存在しない | GET /api/stocks/999 | WARN: [StockResource#getStock] Stock not found: bookId=999 |

---

## 9. エラーメッセージ受入基準

### 9.1 ユーザーフレンドリーなメッセージ

| シナリオID | 説明 | 受入基準 |
|-----------|------|---------|
| MSG-001 | 楽観的ロック失敗時は再試行を促す | "在庫が他のユーザーによって更新されました。再度お試しください。" |
| MSG-002 | 在庫が見つからない時は明確なメッセージ | "在庫情報が見つかりません" |
| MSG-003 | バリデーションエラー時は具体的なメッセージ | "version は必須です" "quantity は必須です" |

---

## 10. クライアント側実装受入基準

### 10.1 再試行ロジック

| シナリオID | 説明 | 受入基準 |
|-----------|------|---------|
| CLIENT-001 | 409受信時は最新情報を再取得 | GET /api/stocks/{bookId}で最新version取得 |
| CLIENT-002 | 最新versionで再更新 | PUT /api/stocks/{bookId}を新しいversionで実行 |
| CLIENT-003 | 再試行回数に上限を設ける | 最大3回までリトライ |

---

## 11. 将来の拡張受入基準

### 11.1 一括更新（実装予定）

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| BULK-001 | 複数在庫を一括更新できる | - | PUT /api/stocks/bulk<br/>[{bookId: 1, version: 1, quantity: 15}, ...]  | 200 OK<br/>全件更新成功 |
| BULK-002 | 一部失敗時は全てロールバック | 一部でversion不一致 | 一括更新 | 409 Conflict<br/>全件ロールバック |

---

## 12. 関連ドキュメント

* [functional_design.md](functional_design.md) - 在庫API機能設計書
* [../../system/behaviors.md](../../system/behaviors.md) - 全体受入基準
* [../../system/architecture_design.md](../../system/architecture_design.md) - アーキテクチャ設計書

