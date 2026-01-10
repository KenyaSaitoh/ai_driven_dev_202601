# API_004_publishers - 出版社APIタスク

**担当者:** 1名  
**推奨スキル:** JAX-RS、JPA  
**想定工数:** 4時間  
**依存タスク:** [common_tasks.md](common_tasks.md)

---

## 概要

このタスクリストは、出版社API（一覧取得）の実装タスクを含みます。シンプルなマスタデータ参照APIです。

---

## タスクリスト

### T_API004_001: PublisherTOの作成

- **目的**: 出版社情報レスポンス用のDTOを実装する
- **対象**: `pro.kensait.backoffice.api.dto.PublisherTO`（Record）
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_004_publishers/functional_design.md) の「2.1 GET /api/publishers」
- **注意事項**: 
  - Java Record形式で実装
  - フィールド: `publisherId`（Integer）、`publisherName`（String）

---

### T_API004_002: PublisherServiceの作成

- **目的**: 出版社管理ビジネスロジックを実装する
- **対象**: `pro.kensait.backoffice.service.PublisherService`
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_004_publishers/functional_design.md) の「3. ビジネスロジック」
- **注意事項**: 
  - `@ApplicationScoped`、`@Transactional`
  - PublisherDaoを`@Inject`
  - メソッド:
    - `getAllPublishers()`: 全出版社取得（配列形式）
  - PublisherエンティティをPublisherTOに変換

---

### T_API004_003: PublisherResourceの作成

- **目的**: 出版社APIのエンドポイントを実装する
- **対象**: `pro.kensait.backoffice.api.PublisherResource`
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_004_publishers/functional_design.md) の「2. エンドポイント仕様」
  - [behaviors.md](../specs/baseline/api/API_004_publishers/behaviors.md) の「2. 出版社API」
- **注意事項**: 
  - `@Path("/publishers")`、`@ApplicationScoped`
  - PublisherServiceを`@Inject`
  - エンドポイント:
    - `@GET`: 全出版社取得（配列形式）
  - レスポンス: List<PublisherTO>
  - HTTPステータス: 200 OK
  - ログ出力: INFO（API呼び出し）

---

### T_API004_004: 出版社APIの単体テスト

- **目的**: PublisherServiceのテストケースを実装する
- **対象**: `pro.kensait.backoffice.service.PublisherServiceTest`
- **参照SPEC**: 
  - [behaviors.md](../specs/baseline/api/API_004_publishers/behaviors.md) の「2. 出版社API」
- **注意事項**: 
  - JUnit 5 + Mockito使用
  - PublisherDaoをモック化
  - テストケース:
    - `testGetAllPublishers()`: 全出版社取得

---

## 完了確認

### チェックリスト

- [X] PublisherTO
- [X] PublisherService
- [X] PublisherResource
- [X] 出版社APIの単体テスト

### 動作確認

以下のcurlコマンドで動作確認:

#### 全出版社取得
```bash
curl -X GET http://localhost:8080/back-office-api-sdd/api/publishers
```

期待されるレスポンス例:
```json
[
  {
    "publisherId": 1,
    "publisherName": "技術評論社"
  },
  {
    "publisherId": 2,
    "publisherName": "翔泳社"
  },
  {
    "publisherId": 3,
    "publisherName": "オライリー・ジャパン"
  }
]
```

---

## 次のステップ

このAPI実装完了後、以下のタスクに並行して進めます:

- [API_005_stocks.md](API_005_stocks.md) - 在庫API
- [API_006_workflows.md](API_006_workflows.md) - ワークフローAPI

---

**タスクファイル作成日:** 2025-01-10  
**想定実行順序:** 6番目（共通機能実装後）
