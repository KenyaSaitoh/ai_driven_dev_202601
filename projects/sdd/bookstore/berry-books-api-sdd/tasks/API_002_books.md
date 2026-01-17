# API_002_books - 書籍API実装タスク

担当者: 担当者B（1名）  
推奨スキル: JAX-RS, REST Client, 外部API連携  
想定工数: 3時間  
依存タスク: [common_tasks.md](common_tasks.md)

---

## 概要

書籍APIを実装する。全ての書籍情報はback-office-apiから取得する（外部API呼び出し／パススルー方式）。書籍一覧、書籍詳細、書籍検索、カテゴリ一覧のエンドポイントを提供する。

---

## タスクリスト

### Resource層

#### T_API002_001: BookResource の作成

* 目的: 書籍APIのRESTエンドポイントを実装する（外部API呼び出し）
* 対象: BookResource.java (JAX-RS Resourceクラス)
* 参照SPEC: 
  * [API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3. API仕様」
  * [API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) の「3. 書籍API (`/api/books`)」
* 注意事項: 
  * @Path("/books")
  * @ApplicationScoped
  * BackOfficeRestClientをインジェクション
  * getAllBooks, getBookById, searchBooksJpql, searchBooksCriteriaメソッド
  * 全てback-office-apiに転送（パススルー）
  * 認証不要（公開API）

---

#### T_API002_002: CategoryResource の作成

* 目的: カテゴリAPIのRESTエンドポイントを実装する（外部API呼び出し）
* 対象: CategoryResource.java (JAX-RS Resourceクラス)
* 参照SPEC: 
  * [API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.5 カテゴリ一覧取得」
  * [API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) の「3.4 カテゴリ一覧取得」
* 注意事項: 
  * @Path("/categories")
  * @ApplicationScoped
  * BackOfficeRestClientをインジェクション
  * getAllCategoriesメソッド（Map<String, Integer>を返却）
  * back-office-apiに転送（パススルー）
  * 認証不要（公開API）

---

### ユニットテスト

#### T_API002_003: [P] BookResourceのテスト作成

* 目的: BookResourceのユニットテストを実装する
* 対象: BookResourceTest.java (JUnit 5テストクラス)
* 参照SPEC: [API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) の「3. 書籍API (`/api/books`)」
* 注意事項: 
  * JUnit 5 + Mockitoを使用
  * BackOfficeRestClientをモック化
  * 正常系・異常系テストケース
  * 全書籍取得成功、書籍詳細取得成功、書籍詳細取得失敗（404）
  * 書籍検索成功（JPQL/Criteria）

---

#### T_API002_004: [P] CategoryResourceのテスト作成

* 目的: CategoryResourceのユニットテストを実装する
* 対象: CategoryResourceTest.java (JUnit 5テストクラス)
* 参照SPEC: [API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) の「3.4 カテゴリ一覧取得」
* 注意事項: 
  * JUnit 5 + Mockitoを使用
  * BackOfficeRestClientをモック化
  * カテゴリ一覧取得成功テストケース

---

## 完了基準

* [ ] BookResource（全書籍取得、書籍詳細取得、書籍検索JPQL、書籍検索Criteria）が実装されている
* [ ] CategoryResource（カテゴリ一覧取得）が実装されている
* [ ] BookResourceのユニットテストが実装されている
* [ ] CategoryResourceのユニットテストが実装されている
* [ ] 全書籍取得エンドポイント（GET /api/books）が動作する
* [ ] 書籍詳細取得エンドポイント（GET /api/books/{id}）が動作する
* [ ] 書籍検索エンドポイント（GET /api/books/search/jpql）が動作する
* [ ] 書籍検索エンドポイント（GET /api/books/search/criteria）が動作する
* [ ] カテゴリ一覧取得エンドポイント（GET /api/categories）が動作する

---

## 参考資料

* [API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) - 書籍API機能設計書
* [API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) - 書籍API受入基準
* [architecture_design.md](../specs/baseline/system/architecture_design.md) - アーキテクチャ設計書
* [external_interface.md](../specs/baseline/system/external_interface.md) - 外部インターフェース仕様書
