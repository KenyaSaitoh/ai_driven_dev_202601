# books_proxy - 振る舞い仕様書（単体テスト用）

ドメイン名: books_proxy  
バージョン: 1.0.0  
最終更新日: 2026-02-07

記法: シナリオは Gherkin 記法で記述する（Feature, Scenario, Given, When, Then, And, But）

---

## 1. 概要

本文書は、books_proxyドメインの単体テスト用の振る舞い、テストシナリオ、受入基準を記述する。

テスト対象:
* BookResource（書籍API）
* CategoryResource（カテゴリAPI）

単体テストの範囲:
* 外部API呼び出しの正常系・異常系をテスト
* BackOfficeRestClientはモック化
* 実際の外部API呼び出しは結合テストで検証

関連ドキュメント:
* [detailed_design.md](detailed_design.md) - 詳細設計書
* [../../basic_design/books_proxy/functional_design.md](../../basic_design/books_proxy/functional_design.md) - 書籍API連携機能設計書
* [../../basic_design/books_proxy/behaviors.md](../../basic_design/books_proxy/behaviors.md) - 結合テスト用振る舞い仕様書

---

## 2. テストシナリオ

### 2.1 BookResource - 書籍一覧取得

#### Feature: 書籍一覧取得API

全書籍の一覧を取得する

#### Scenario: 書籍一覧を正常に取得

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: findAllBooks()が書籍リストを返す

* When（操作）:
  * BookResource.getAllBooks()を呼び出す

* Then（期待結果）:
  * HTTPステータス200 OKが返される
  * レスポンスボディにList<BookTO>が含まれる
  * BackOfficeRestClient.findAllBooks()が1回呼ばれる

#### テストデータ
* モックの戻り値:
  ```json
  [
    {
      "bookId": 1,
      "bookName": "Java完全理解",
      "author": "山田太郎",
      "price": 3000,
      "quantity": 10
    }
  ]
  ```

#### Scenario: 外部API呼び出し失敗時

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: findAllBooks()がRuntimeExceptionをスローする

* When（操作）:
  * BookResource.getAllBooks()を呼び出す

* Then（期待結果）:
  * RuntimeExceptionがスローされる
  * ExceptionMapperで500 Internal Server Errorに変換される

---

### 2.2 BookResource - 書籍詳細取得

#### Feature: 書籍詳細取得API

書籍IDで書籍詳細を取得する

#### Scenario: 書籍詳細を正常に取得

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: findBookById(1)が書籍情報を返す

* When（操作）:
  * BookResource.getBookById(1)を呼び出す

* Then（期待結果）:
  * HTTPステータス200 OKが返される
  * レスポンスボディにBookTOが含まれる
  * bookIdが1である

#### テストデータ
* 入力: bookId = 1
* モックの戻り値:
  ```json
  {
    "bookId": 1,
    "bookName": "Java完全理解",
    "author": "山田太郎",
    "categoryId": 1,
    "publisherId": 1,
    "publisherName": "技術評論社",
    "price": 3000,
    "quantity": 10,
    "version": 1
  }
  ```

#### Scenario: 書籍が存在しない場合

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: findBookById(999)がWebApplicationException（404）をスローする

* When（操作）:
  * BookResource.getBookById(999)を呼び出す

* Then（期待結果）:
  * WebApplicationExceptionがスローされる
  * HTTPステータス404 Not Foundが返される

---

### 2.3 BookResource - 書籍検索（JPQL）

#### Feature: 書籍検索API（JPQL版）

カテゴリIDまたはキーワードで書籍を検索する

#### Scenario: カテゴリIDで書籍を検索

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: searchBooksJpql(1, null)が該当書籍リストを返す

* When（操作）:
  * BookResource.searchBooksJpql(categoryId=1, keyword=null)を呼び出す

* Then（期待結果）:
  * HTTPステータス200 OKが返される
  * レスポンスボディに該当するList<BookTO>が含まれる
  * すべての書籍のcategoryIdが1である

#### Scenario: キーワードで書籍を検索

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: searchBooksJpql(null, "Java")がキーワードに一致する書籍リストを返す

* When（操作）:
  * BookResource.searchBooksJpql(categoryId=null, keyword="Java")を呼び出す

* Then（期待結果）:
  * HTTPステータス200 OKが返される
  * レスポンスボディにList<BookTO>が含まれる
  * 書籍名に"Java"が含まれる

#### Scenario: 該当書籍がない場合

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: searchBooksJpql(null, "存在しないキーワード")が空のリストを返す

* When（操作）:
  * BookResource.searchBooksJpql(categoryId=null, keyword="存在しないキーワード")を呼び出す

* Then（期待結果）:
  * HTTPステータス200 OKが返される
  * レスポンスボディに空のリストが含まれる

---

### 2.4 BookResource - 書籍検索（Criteria API）

#### Feature: 書籍検索API（Criteria API版）

カテゴリIDまたはキーワードで書籍を検索する

#### Scenario: カテゴリIDとキーワードの両方で検索

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: searchBooksCriteria(1, "Java")が両条件に一致する書籍リストを返す

* When（操作）:
  * BookResource.searchBooksCriteria(categoryId=1, keyword="Java")を呼び出す

* Then（期待結果）:
  * HTTPステータス200 OKが返される
  * レスポンスボディにList<BookTO>が含まれる
  * すべての書籍のcategoryIdが1である
  * すべての書籍名に"Java"が含まれる

#### Scenario: パラメータなしで検索

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: searchBooksCriteria(null, null)が全書籍リストを返す

* When（操作）:
  * BookResource.searchBooksCriteria(categoryId=null, keyword=null)を呼び出す

* Then（期待結果）:
  * HTTPステータス200 OKが返される
  * レスポンスボディに全書籍のList<BookTO>が含まれる

---

### 2.5 CategoryResource - カテゴリ一覧取得

#### Feature: カテゴリ一覧取得API

カテゴリ一覧をマップ形式で取得する

#### Scenario: カテゴリ一覧を正常に取得

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: findAllCategories()がカテゴリリストを返す

* When（操作）:
  * CategoryResource.getAllCategories()を呼び出す

* Then（期待結果）:
  * HTTPステータス200 OKが返される
  * レスポンスボディにMap<String, Integer>が含まれる
  * キーがカテゴリ名、値がカテゴリIDである
  * BackOfficeRestClient.findAllCategories()が1回呼ばれる

#### テストデータ
* モックの戻り値:
  ```json
  [
    {"categoryId": 1, "categoryName": "プログラミング"},
    {"categoryId": 2, "categoryName": "データベース"},
    {"categoryId": 3, "categoryName": "ネットワーク"}
  ]
  ```
* 期待される出力:
  ```json
  {
    "プログラミング": 1,
    "データベース": 2,
    "ネットワーク": 3
  }
  ```

#### Scenario: 外部API呼び出し失敗時

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: findAllCategories()がRuntimeExceptionをスローする

* When（操作）:
  * CategoryResource.getAllCategories()を呼び出す

* Then（期待結果）:
  * RuntimeExceptionがスローされる
  * ExceptionMapperで500 Internal Server Errorに変換される

---

## 3. モック化の方針

### 3.1 ドメイン内の依存関係
* BookResource、CategoryResourceはモック不要（テスト対象）

### 3.2 ドメイン外の依存関係
* BackOfficeRestClient → モック化（Mockitoを使用）
* 外部API（back-office-api）→ 実際の呼び出しなし（結合テストで検証）

---

## 4. カバレッジ目標

* ステートメントカバレッジ: 80%以上
* ブランチカバレッジ: 70%以上

---

## 5. 受入基準

### 5.1 機能要件
- [ ] すべての正常系テストが成功する
- [ ] すべての異常系テストが成功する
- [ ] すべての境界値テスト（該当する場合）が成功する

### 5.2 品質要件
- [ ] カバレッジ目標を達成する
- [ ] テストコードにコメントが適切に記載されている
- [ ] テストケースが独立している（テスト間の依存関係がない）
- [ ] モック設定が適切に行われている

---

## 6. 参考資料

* [detailed_design.md](detailed_design.md) - 詳細設計書
* [../../basic_design/books_proxy/functional_design.md](../../basic_design/books_proxy/functional_design.md) - 書籍API連携機能設計書
* [../../basic_design/books_proxy/behaviors.md](../../basic_design/books_proxy/behaviors.md) - 結合テスト用振る舞い仕様書
* [../../requirements/behaviors.md](../../requirements/behaviors.md) - システム振る舞い仕様書（E2Eテスト用）