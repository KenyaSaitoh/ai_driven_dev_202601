# berry-books-api-sdd - API_002_books タスク

担当者: 担当者B（1名）  
推奨スキル: JAX-RS、REST Client  
想定工数: 6時間  
依存タスク: [共通機能タスク](common.md)

---

## 概要

本タスクは、書籍APIの実装を行う。書籍一覧取得、書籍詳細取得、書籍検索、カテゴリ一覧取得を実装する。

* 実装パターン: 外部API呼び出し
  * 書籍情報はback-office-apiから取得
  * 本システムでは書籍データを管理しない
  * BackOfficeRestClientを使用して外部APIを呼び出す

---

## タスクリスト

### 1. REST Resource

#### T_API002_001: BookResource の作成

* [ ] T_API002_001: BookResource の作成
  * 目的: 書籍APIエンドポイントを実装する
  * 対象: BookResource.java（JAX-RS Resource）
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3. API一覧」
  * 注意事項:
    * @Path("/books")を付与
    * @ApplicationScopedを付与
    * BackOfficeRestClientをインジェクション
    * 実装メソッド:
      1. getAllBooks(): 全書籍取得（外部API経由）
      2. getBookById(int bookId): 書籍詳細取得（外部API経由）
      3. searchBooksJpql(Integer categoryId, String keyword): 書籍検索（JPQL版）
      4. searchBooksCriteria(Integer categoryId, String keyword): 書籍検索（Criteria API版）
    * エラーハンドリング:
      * 書籍が見つからない: 404 Not Found
    * ログ出力: INFO（メソッド開始）、WARN（書籍が見つからない）、ERROR（例外発生）

---

#### T_API002_002: CategoryResource の作成

* [ ] T_API002_002: CategoryResource の作成
  * 目的: カテゴリAPIエンドポイントを実装する
  * 対象: CategoryResource.java（JAX-RS Resource）
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3. API一覧」
  * 注意事項:
    * @Path("/categories")を付与（注意: 複数形）
    * @ApplicationScopedを付与
    * BackOfficeRestClientをインジェクション
    * 実装メソッド:
      1. getAllCategories(): カテゴリ一覧取得（外部API経由）
    * レスポンス: Map<String, Integer>（カテゴリ名 → カテゴリID）
    * ログ出力: INFO（メソッド開始）

---

## 成果物チェックリスト

* [ ] REST Resource（BookResource、CategoryResource）が実装されている
* [ ] 書籍APIが正常に動作する（全書籍取得、書籍詳細取得、書籍検索）
* [ ] カテゴリAPIが正常に動作する（カテゴリ一覧取得）
* [ ] 外部API（back-office-api）との連携が正常に動作する
* [ ] エラーハンドリングが適切に動作する

---

## 動作確認

### 全書籍取得

```bash
curl -X GET http://localhost:8080/berry-books-api-sdd/api/books
```

### 書籍詳細取得

```bash
curl -X GET http://localhost:8080/berry-books-api-sdd/api/books/1
```

### 書籍検索（JPQL版）

```bash
# カテゴリIDで検索
curl -X GET "http://localhost:8080/berry-books-api-sdd/api/books/search/jpql?categoryId=1"

# キーワードで検索
curl -X GET "http://localhost:8080/berry-books-api-sdd/api/books/search/jpql?keyword=Java"

# カテゴリID + キーワードで検索
curl -X GET "http://localhost:8080/berry-books-api-sdd/api/books/search/jpql?categoryId=1&keyword=Java"
```

### 書籍検索（Criteria API版）

```bash
# カテゴリIDで検索
curl -X GET "http://localhost:8080/berry-books-api-sdd/api/books/search/criteria?categoryId=1"

# キーワードで検索
curl -X GET "http://localhost:8080/berry-books-api-sdd/api/books/search/criteria?keyword=Java"

# カテゴリID + キーワードで検索
curl -X GET "http://localhost:8080/berry-books-api-sdd/api/books/search/criteria?categoryId=1&keyword=Java"
```

### カテゴリ一覧取得

```bash
curl -X GET http://localhost:8080/berry-books-api-sdd/api/categories
```

---

## 参考資料

* [functional_design.md](../specs/baseline/basic_design/functional_design.md) - 機能設計書
* [behaviors.md](../specs/baseline/basic_design/behaviors.md) - 振る舞い仕様書
* [external_interface.md](../specs/baseline/basic_design/external_interface.md) - 外部インターフェース仕様書
