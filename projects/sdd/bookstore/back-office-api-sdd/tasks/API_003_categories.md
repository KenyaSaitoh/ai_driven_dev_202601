# API_003_categories - カテゴリAPIタスク

担当者: 1名  
推奨スキル: JAX-RS、JPA  
想定工数: 4時間  
依存タスク: [common_tasks.md](common_tasks.md)

---

## 概要

このタスクリストは、カテゴリAPI（一覧取得）の実装タスクを含みます。シンプルなマスタデータ参照APIです。

---

## タスクリスト

### T_API003_001: CategoryTOの作成

* 目的: カテゴリ情報レスポンス用のDTOを実装する
* 対象: `pro.kensait.backoffice.api.dto.CategoryTO`（Record）
* 参照SPEC: 
  * [functional_design.md](../specs/baseline/api/API_003_categories/functional_design.md) の「2.1 GET /api/categories」
* 注意事項: 
  * Java Record形式で実装
  * フィールド: `categoryId`（Integer）、`categoryName`（String）

---

### T_API003_002: CategoryServiceの作成

* 目的: カテゴリ管理ビジネスロジックを実装する
* 対象: `pro.kensait.backoffice.service.CategoryService`
* 参照SPEC: 
  * [functional_design.md](../specs/baseline/api/API_003_categories/functional_design.md) の「3. ビジネスロジック」
* 注意事項: 
  * `@ApplicationScoped`、`@Transactional`
  * CategoryDaoを`@Inject`
  * メソッド:
    * `getAllCategories()`: 全カテゴリ取得（配列形式）
    * CategoryエンティティをCategoryTOに変換

---

### T_API003_003: CategoryResourceの作成

* 目的: カテゴリAPIのエンドポイントを実装する
* 対象: `pro.kensait.backoffice.api.CategoryResource`
* 参照SPEC: 
  * [functional_design.md](../specs/baseline/api/API_003_categories/functional_design.md) の「2. エンドポイント仕様」
  * [behaviors.md](../specs/baseline/api/API_003_categories/behaviors.md) の「2. カテゴリAPI」
* 注意事項: 
  * `@Path("/categories")`、`@ApplicationScoped`
  * CategoryServiceを`@Inject`
  * エンドポイント:
    * `@GET`: 全カテゴリ取得（配列形式）
    * レスポンス: List<CategoryTO>
    * HTTPステータス: 200 OK
    * ログ出力: INFO（API呼び出し）

---

### T_API003_004: カテゴリAPIの単体テスト

* 目的: CategoryServiceのテストケースを実装する
* 対象: `pro.kensait.backoffice.service.CategoryServiceTest`
* 参照SPEC: 
  * [behaviors.md](../specs/baseline/api/API_003_categories/behaviors.md) の「2. カテゴリAPI」
* 注意事項: 
  * JUnit 5 + Mockito使用
  * CategoryDaoをモック化
  * テストケース:
    * `testGetAllCategories()`: 全カテゴリ取得

---

## 完了確認

### チェックリスト

* [X] CategoryTO
* [X] CategoryService
* [X] CategoryResource
* [X] カテゴリAPIの単体テスト

### 動作確認

以下のcurlコマンドで動作確認:

#### 全カテゴリ取得
```bash
curl -X GET http://localhost:8080/back-office-api-sdd/api/categories
```

期待されるレスポンス例:
```json
[
  {
    "categoryId": 1,
    "categoryName": "文学"
  },
  {
    "categoryId": 2,
    "categoryName": "ビジネス"
  },
  {
    "categoryId": 3,
    "categoryName": "技術"
  }
]
```

---

## 次のステップ

このAPI実装完了後、以下のタスクに並行して進めます:

* [API_004_publishers.md](API_004_publishers.md) - 出版社API
* [API_005_stocks.md](API_005_stocks.md) - 在庫API
* [API_006_workflows.md](API_006_workflows.md) - ワークフローAPI

---

タスクファイル作成日: 2025-01-10  
想定実行順序: 5番目（共通機能実装後）
