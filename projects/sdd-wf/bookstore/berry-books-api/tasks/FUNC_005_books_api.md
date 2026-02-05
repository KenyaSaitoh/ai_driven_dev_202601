# FUNC_005_books_api - 書籍API

## メタデータ

* タスクID: FUNC_005
* 機能タイプ: API（外部API呼び出し）
* 依存タスク: FUNC_001
* 並行実行可能: FUNC_004, FUNC_006, FUNC_007
* 担当者: 担当者D
* 推奨スキル: JAX-RS, 外部API連携, Proxyパターン
* 想定工数: 4時間

## 実装内容

書籍APIエンドポイントを実装する。
このAPIは、back-office-apiの書籍情報を取得する外部API呼び出し型のResourceである。

---

## タスクリスト

### 1. Resourceクラスの作成

* [ ] T_FUNC005_001: BookResourceの作成
  * 目的: 書籍APIエンドポイントを実装する
  * 対象: pro.kensait.berrybooks.api.BookResource
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3.1 API実装方式」
    * [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「2.2.1 外部API呼び出し vs 独自実装」
  * 注意事項:
    * @Path("/books")
    * @ApplicationScoped
    * BackOfficeRestClientを注入
    * 認証不要（認証除外エンドポイント）

* [ ] T_FUNC005_002: GET /books エンドポイントの実装
  * 目的: 全書籍を在庫情報と共に取得する
  * 対象: BookResource.getAllBooks()
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「7.1 書籍一覧取得」
  * 注意事項:
    * @GET, @Produces(APPLICATION_JSON)
    * BackOfficeRestClient.getAllBooks()を呼び出し
    * レスポンスをそのまま返却（Proxyパターン）

* [ ] T_FUNC005_003: GET /books/{bookId} エンドポイントの実装
  * 目的: 指定された書籍IDの詳細情報を取得する
  * 対象: BookResource.getBookById()
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「7.2 書籍詳細取得」
  * 注意事項:
    * @GET, @Path("/{bookId}"), @Produces(APPLICATION_JSON)
    * @PathParam("bookId") int bookId
    * BackOfficeRestClient.getBookById()を呼び出し
    * レスポンスをそのまま返却

* [ ] T_FUNC005_004: GET /books/search/jpql エンドポイントの実装
  * 目的: カテゴリIDまたはキーワードで書籍を検索する（JPQL）
  * 対象: BookResource.searchBooksJpql()
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「7.3 書籍検索（JPQL）」
  * 注意事項:
    * @GET, @Path("/search/jpql"), @Produces(APPLICATION_JSON)
    * @QueryParam("categoryId") Integer categoryId
    * @QueryParam("keyword") String keyword
    * BackOfficeRestClient.searchBooksJpql()を呼び出し
    * レスポンスをそのまま返却

* [ ] T_FUNC005_005: GET /books/search/criteria エンドポイントの実装
  * 目的: カテゴリIDまたはキーワードで書籍を検索する（Criteria API）
  * 対象: BookResource.searchBooksCriteria()
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「7.4 書籍検索（Criteria API）」
  * 注意事項:
    * @GET, @Path("/search/criteria"), @Produces(APPLICATION_JSON)
    * @QueryParam("categoryId") Integer categoryId
    * @QueryParam("keyword") String keyword
    * BackOfficeRestClient.searchBooksCriteria()を呼び出し
    * レスポンスをそのまま返却

### 2. カテゴリAPI

* [ ] T_FUNC005_006: CategoryResourceの作成
  * 目的: カテゴリAPIエンドポイントを実装する
  * 対象: pro.kensait.berrybooks.api.CategoryResource
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3.1 API実装方式」
  * 注意事項:
    * @Path("/categories")
    * @ApplicationScoped
    * BackOfficeRestClientを注入
    * 認証不要（認証除外エンドポイント）

* [ ] T_FUNC005_007: GET /categories エンドポイントの実装
  * 目的: カテゴリ一覧を取得する
  * 対象: CategoryResource.getAllCategories()
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「7.5 カテゴリ一覧取得」
  * 注意事項:
    * @GET, @Produces(APPLICATION_JSON)
    * BackOfficeRestClient.getAllCategories()を呼び出し
    * レスポンスをそのまま返却（Map<String, Integer>）

### 3. エラーハンドリング

* [ ] T_FUNC005_008: 書籍未検出時の処理
  * 目的: 指定された書籍IDが見つからない場合の処理を実装する
  * 対象: BookResource.getBookById()
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8.1 エラーレスポンス仕様」
  * 注意事項:
    * BackOfficeRestClientから404 Not Foundが返された場合
    * ResourceNotFoundExceptionをスロー
    * 404 Not Found + ErrorResponseを返却

* [ ] T_FUNC005_009: 外部API接続エラー時の処理
  * 目的: back-office-apiとの接続エラーを処理する
  * 対象: BookResource全体
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「8. エラーハンドリング」
  * 注意事項:
    * ProcessingException発生時: 503 Service Unavailable
    * WebApplicationException発生時: ステータスコードに応じた処理

---

## 完了条件

* [ ] 書籍一覧取得が正常に動作する
* [ ] 書籍詳細取得が正常に動作する
* [ ] 書籍検索（JPQL）が正常に動作する
* [ ] 書籍検索（Criteria API）が正常に動作する
* [ ] カテゴリ一覧取得が正常に動作する
* [ ] 書籍未検出時に404 Not Foundが返される
* [ ] 外部API接続エラー時に503 Service Unavailableが返される
* [ ] 単体テストが全て成功する

---

## 参考資料

* [../specs/baseline/basic_design/architecture_design.md](../specs/baseline/basic_design/architecture_design.md) - アーキテクチャ設計書
* [../specs/baseline/basic_design/functional_design.md](../specs/baseline/basic_design/functional_design.md) - 機能設計書
* [../specs/baseline/basic_design/external_interface.md](../specs/baseline/basic_design/external_interface.md) - 外部インターフェース仕様書
* [../specs/baseline/basic_design/behaviors.md](../specs/baseline/basic_design/behaviors.md) - 振る舞い仕様書
