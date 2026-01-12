# API_004_publishers - 出版社API受入基準

* API ID: API_004_publishers  
* API名: 出版社API  
* ベースパス: `/api/publishers`  
* バージョン: 2.0.0  
* 最終更新日: 2025-01-02

---

## 1. 概要

本文書は、出版社APIの受入基準を記述する。各エンドポイントについて、正常系・異常系の振る舞いを定義し、テストシナリオの基礎とする。

---

## 2. 出版社一覧取得 (`GET /api/publishers`)

### 2.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| PUB-LIST-001 | すべての出版社を取得できる | 出版社が登録されている | /api/publishersにリクエスト | 200 OK<br/>出版社一覧が配列形式で返される |
| PUB-LIST-002 | 出版社がpublisherId昇順でソートされる | 複数の出版社が登録されている | /api/publishersにリクエスト | 200 OK<br/>publisherId昇順で返される |
| PUB-LIST-003 | レスポンス形式が配列 | 出版社が登録されている | /api/publishersにリクエスト | 200 OK<br/>[{publisherId: 1, publisherName: "出版社A"}, ...] |
| PUB-LIST-004 | 出版社が0件の場合、空配列を返す | 出版社が登録されていない | /api/publishersにリクエスト | 200 OK<br/>空配列 [] が返される |

---

## 3. データ整合性受入基準

### 3.1 データ内容

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| DATA-001 | publisherIdとpublisherNameが正しく返される | 出版社"出版社A"(ID=1)が存在 | /api/publishersにリクエスト | 200 OK<br/>{publisherId: 1, publisherName: "出版社A"}が含まれる |
| DATA-002 | すべての出版社が含まれる | 10件の出版社が存在 | /api/publishersにリクエスト | 200 OK<br/>10件すべてが返される |

---

## 4. パフォーマンス受入基準

### 4.1 レスポンスタイム

| シナリオID | API | 受入基準 |
|-----------|-----|---------|
| PERF-001 | GET /api/publishers | 50ms以内（95パーセンタイル） |

### 4.2 キャッシング効果（将来実装時）

| シナリオID | 説明 | 受入基準 |
|-----------|------|---------|
| CACHE-001 | 初回リクエストはDB問い合わせ | 初回レスポンスタイム: 50ms |
| CACHE-002 | 2回目以降はキャッシュから返却 | 2回目以降レスポンスタイム: 5ms以内 |
| CACHE-003 | キャッシュTTLは適切に設定 | キャッシュTTL: 1時間（マスタデータのため） |

---

## 5. ログ出力受入基準

### 5.1 ログレベル

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| LOG-001 | 出版社一覧取得時はINFOレベルでログ出力 | - | /api/publishers | INFO: [PublisherResource#getAllPublishers]<br/>INFO: [PublisherService#getPublishersAll] |
| LOG-002 | 取得成功時は件数を出力 | 10件の出版社が存在 | /api/publishers | INFO: [PublisherResource#getAllPublishers] Success: 10 publishers returned |
| LOG-003 | DB接続エラー時はERRORレベル | DB接続不可 | /api/publishers | ERROR: [PublisherDao#findAll] Database error |

---

## 6. 並行処理受入基準

### 6.1 同時リクエスト

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| CONC-001 | 複数ユーザーが同時に出版社一覧を取得できる | - | 3ユーザーが同時に/api/publishers | 全リクエストが成功<br/>独立したトランザクション |
| CONC-002 | 読み取り専用のため競合なし | - | 同時に/api/publishers | データ競合なし<br/>ロック不要 |
| CONC-003 | 大量リクエスト時も安定 | - | 1000件の同時リクエスト | 全リクエストが3秒以内に完了<br/>平均レスポンスタイム: 150ms |

---

## 7. 将来の拡張受入基準

### 7.1 CRUD操作（実装予定）

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| CRUD-001 | 出版社詳細取得 | publisherId=1が存在 | GET /api/publishers/1 | 200 OK<br/>{publisherId: 1, publisherName: "出版社A"} |
| CRUD-002 | 出版社作成 | - | POST /api/publishers<br/>{publisherName: "新出版社"} | 201 Created<br/>Location: /api/publishers/11 |
| CRUD-003 | 出版社更新 | publisherId=1が存在 | PUT /api/publishers/1<br/>{publisherName: "更新"} | 200 OK |
| CRUD-004 | 出版社削除 | publisherId=1が存在 | DELETE /api/publishers/1 | 200 OK |

### 7.2 統計情報（実装予定）

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| STATS-001 | 出版社別書籍数を取得 | - | GET /api/publishers?includeStats=true | 200 OK<br/>[{publisherId: 1, publisherName: "出版社A", bookCount: 150}] |

---

## 8. マスタデータ管理受入基準

### 8.1 データ安定性

| シナリオID | 説明 | 受入基準 |
|-----------|------|---------|
| MASTER-001 | 出版社マスタは変更頻度が低い | 月1回未満の更新 |
| MASTER-002 | 出版社削除は論理削除 | 物理削除せず、フラグで管理 |
| MASTER-003 | キャッシング推奨 | マスタデータのためキャッシュ適用を推奨 |

---

## 9. カテゴリAPIとの類似性

### 9.1 共通パターン

| シナリオID | 説明 | 受入基準 |
|-----------|------|---------|
| PATTERN-001 | カテゴリAPIと同じ処理フロー | Resource -> Service -> Dao -> Database |
| PATTERN-002 | カテゴリAPIと同じレスポンス形式 | 配列形式 [{id, name}] |
| PATTERN-003 | カテゴリAPIと同等のパフォーマンス | 50ms以内（95パーセンタイル） |

---

## 10. 関連ドキュメント

* [functional_design.md](functional_design.md) - 出版社API機能設計書
* [../../system/behaviors.md](../../system/behaviors.md) - 全体受入基準
* [../../system/architecture_design.md](../../system/architecture_design.md) - アーキテクチャ設計書

