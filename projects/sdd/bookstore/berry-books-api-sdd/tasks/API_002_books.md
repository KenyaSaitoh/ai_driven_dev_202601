# API_002_books - 書籍APIタスク（プロキシパターン）

担当者: 担当者B（1名）  
推奨スキル: JAX-RS、REST Client API、プロキシパターン  
想定工数: 3時間  
依存タスク: [common_tasks.md](common_tasks.md)

---

## タスク一覧

### Resource作成（プロキシパターン）

* [ ] T_API002_001: BookResourceの作成
  * 目的: 書籍APIエンドポイント（プロキシ）を実装する
  * 対象: BookResource.java（@Path("/books"), @ApplicationScoped）
  * 参照SPEC: 
    * [API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3. API仕様」
    * [API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) の「3. 書籍API」
  * 注意事項: BackOfficeRestClientをインジェクション、プロキシパターンで実装（独自ロジックなし）

---

### エンドポイント実装（プロキシ）

* [ ] T_API002_002: findAllBooksメソッドの実装
  * 目的: 全書籍取得エンドポイント（プロキシ）を実装する
  * 対象: BookResource.findAllBooks()
  * 参照SPEC: [API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.1 書籍一覧取得」
  * 注意事項: BackOfficeRestClient.findAllBooks()をそのまま返却

---

* [ ] T_API002_003: findBookByIdメソッドの実装
  * 目的: 書籍詳細取得エンドポイント（プロキシ）を実装する
  * 対象: BookResource.findBookById()
  * 参照SPEC: [API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.2 書籍詳細取得」
  * 注意事項: BackOfficeRestClient.findBookById()をそのまま返却、404エラー処理

---

* [ ] T_API002_004: searchBooksJpqlメソッドの実装
  * 目的: 書籍検索エンドポイント（JPQL版・プロキシ）を実装する
  * 対象: BookResource.searchBooksJpql()
  * 参照SPEC: [API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.3 書籍検索（JPQL版）」
  * 注意事項: BackOfficeRestClient.searchBooksJpql()をそのまま返却

---

* [ ] T_API002_005: searchBooksCriteriaメソッドの実装
  * 目的: 書籍検索エンドポイント（Criteria API版・プロキシ）を実装する
  * 対象: BookResource.searchBooksCriteria()
  * 参照SPEC: [API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.4 書籍検索（Criteria API版）」
  * 注意事項: BackOfficeRestClient.searchBooksCriteria()をそのまま返却

---

### CategoryResource作成（プロキシパターン）

* [ ] T_API002_006: CategoryResourceの作成
  * 目的: カテゴリAPIエンドポイント（プロキシ）を実装する
  * 対象: CategoryResource.java（@Path("/categories"), @ApplicationScoped）
  * 参照SPEC: [API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.5 カテゴリ一覧取得」
  * 注意事項: BackOfficeRestClientをインジェクション、プロキシパターンで実装

---

* [ ] T_API002_007: findAllCategoriesメソッドの実装
  * 目的: カテゴリ一覧取得エンドポイント（プロキシ）を実装する
  * 対象: CategoryResource.findAllCategories()
  * 参照SPEC: [API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.5 カテゴリ一覧取得」
  * 注意事項: BackOfficeRestClient.findAllCategories()をそのまま返却、Map<String, Integer>形式

---

### 単体テスト

* [ ] [P] T_API002_008: BookResourceのテスト
  * 目的: BookResourceの単体テストを実装する
  * 対象: BookResourceTest.java（JUnit 5 + Mockito）
  * 参照SPEC: [API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) の「3. 書籍API」
  * 注意事項: BackOfficeRestClientをモック、プロキシ動作の確認

---

* [ ] [P] T_API002_009: CategoryResourceのテスト
  * 目的: CategoryResourceの単体テストを実装する
  * 対象: CategoryResourceTest.java（JUnit 5 + Mockito）
  * 参照SPEC: [API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) の「3.4 カテゴリ一覧取得」
  * 注意事項: BackOfficeRestClientをモック、プロキシ動作の確認

---

### APIテスト（オプション）

* [ ] [P] T_API002_010: 書籍APIのE2Eテスト
  * 目的: 書籍APIのE2Eテストを実装する（オプション）
  * 対象: BookApiE2ETest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) の「3. 書籍API」
  * 注意事項: 全書籍取得、書籍検索（カテゴリ+キーワード）のシナリオテスト、@Tag("e2e")でタグ付け

---

## 実装時の注意事項

### プロキシパターンの実装

重要: このAPIはback-office-apiへの単純なプロキシとして機能します。独自のビジネスロジックは実装しません。

実装方針:
1. BackOfficeRestClientをインジェクション
2. 外部APIのメソッドをそのまま呼び出し
3. レスポンスをそのまま返却
4. エラーハンドリングは外部APIのレスポンスコードをそのまま返す

実装例:

```java
@GET
@Produces(MediaType.APPLICATION_JSON)
public Response findAllBooks() {
    logger.info("[ BookResource#findAllBooks ] Get all books");
    List<BookTO> books = backOfficeRestClient.findAllBooks();
    return Response.ok(books).build();
}
```

### 認証要否

全てのエンドポイントは認証不要です。JwtAuthenFilterの公開エンドポイントリストに含まれます。

---

## 参考資料

* [API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) - 書籍API機能設計書
* [API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) - 書籍API受入基準
* [external_interface.md](../specs/baseline/system/external_interface.md) - 外部API連携仕様（back-office-api）
* [architecture_design.md](../specs/baseline/system/architecture_design.md) - BFFアーキテクチャ
