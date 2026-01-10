# API_005_stocks - 在庫APIタスク

**担当者:** 1名  
**推奨スキル:** JAX-RS、JPA、楽観的ロック  
**想定工数:** 6時間  
**依存タスク:** [common_tasks.md](common_tasks.md)

---

## 概要

このタスクリストは、在庫API（一覧取得、詳細取得、更新）の実装タスクを含みます。楽観的ロック（`@Version`）による排他制御を実装します。

---

## タスクリスト

### T_API005_001: StockTOの作成

- **目的**: 在庫情報レスポンス用のDTOを実装する
- **対象**: `pro.kensait.backoffice.api.dto.StockTO`（Record）
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_005_stocks/functional_design.md) の「2.1 GET /api/stocks」
- **注意事項**: 
  - Java Record形式で実装
  - フィールド: 
    - `bookId`（Integer）
    - `bookName`（String）
    - `quantity`（Integer）
    - `version`（Long）- 楽観的ロック用バージョン

---

### T_API005_002: StockUpdateRequestの作成

- **目的**: 在庫更新リクエスト用のDTOを実装する
- **対象**: `pro.kensait.backoffice.api.dto.StockUpdateRequest`（Record）
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_005_stocks/functional_design.md) の「2.3 PUT /api/stocks/{bookId}」
- **注意事項**: 
  - Java Record形式で実装
  - フィールド: 
    - `quantity`（Integer）- 更新後の在庫数
    - `version`（Long）- 楽観的ロック用バージョン
  - Bean Validation: `@NotNull`、`@Min(0)`

---

### T_API005_003: StockResourceの作成

- **目的**: 在庫APIのエンドポイントを実装する
- **対象**: `pro.kensait.backoffice.api.StockResource`
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_005_stocks/functional_design.md) の「2. エンドポイント仕様」
  - [behaviors.md](../specs/baseline/api/API_005_stocks/behaviors.md) の「2. 在庫API」
- **注意事項**: 
  - `@Path("/stocks")`、`@ApplicationScoped`
  - StockDaoを`@Inject`（Serviceレイヤーなし、直接DAO呼び出し）
  - エンドポイント:
    - `@GET`: 全在庫取得
    - `@GET @Path("/{bookId}")`: 在庫取得（書籍ID指定）
    - `@PUT @Path("/{bookId}")`: 在庫更新（楽観的ロック対応）
      - リクエストボディ: StockUpdateRequest
      - バージョンチェック: リクエストのversionと現在のversionが一致しない場合は409 Conflict
  - レスポンス: List<StockTO> または StockTO
  - HTTPステータス: 200 OK、404 Not Found（在庫が存在しない）、409 Conflict（楽観的ロック失敗）
  - ログ出力: INFO（API呼び出し）、WARN（楽観的ロック失敗）
  - `@Transactional`を付与（在庫更新メソッド）

---

### T_API005_004: 在庫更新の単体テスト

- **目的**: StockResourceのテストケースを実装する
- **対象**: `pro.kensait.backoffice.api.StockResourceTest`
- **参照SPEC**: 
  - [behaviors.md](../specs/baseline/api/API_005_stocks/behaviors.md) の「2. 在庫API」
- **注意事項**: 
  - JUnit 5 + Mockito使用
  - StockDaoをモック化
  - テストケース:
    - `testGetAllStocks()`: 全在庫取得
    - `testGetStock_Found()`: 在庫取得（存在する）
    - `testGetStock_NotFound()`: 在庫取得（存在しない）
    - `testUpdateStock_Success()`: 在庫更新成功
    - `testUpdateStock_OptimisticLockFailure()`: 楽観的ロック失敗（409 Conflict）
    - `testUpdateStock_NotFound()`: 在庫が存在しない（404 Not Found）

---

## 完了確認

### チェックリスト

- [X] StockTO
- [X] StockUpdateRequest
- [X] StockResource
- [X] 在庫更新の単体テスト

### 動作確認

以下のcurlコマンドで動作確認:

#### 全在庫取得
```bash
curl -X GET http://localhost:8080/back-office-api-sdd/api/stocks
```

#### 在庫取得（書籍ID指定）
```bash
curl -X GET http://localhost:8080/back-office-api-sdd/api/stocks/1
```

#### 在庫更新（成功）
```bash
curl -X PUT http://localhost:8080/back-office-api-sdd/api/stocks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 50,
    "version": 0
  }'
```

期待されるレスポンス:
```json
{
  "bookId": 1,
  "bookName": "Javaプログラミング入門",
  "quantity": 50,
  "version": 1
}
```

#### 在庫更新（楽観的ロック失敗）
```bash
# 同じversionで再度更新を試みる
curl -X PUT http://localhost:8080/back-office-api-sdd/api/stocks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 60,
    "version": 0
  }'
```

期待されるレスポンス（409 Conflict）:
```json
{
  "error": "OptimisticLockException",
  "message": "他のユーザーによって更新されました。再度お試しください。"
}
```

---

## 次のステップ

このAPI実装完了後、以下のタスクに並行して進めます:

- [API_006_workflows.md](API_006_workflows.md) - ワークフローAPI

---

**タスクファイル作成日:** 2025-01-10  
**想定実行順序:** 7番目（共通機能実装後）
