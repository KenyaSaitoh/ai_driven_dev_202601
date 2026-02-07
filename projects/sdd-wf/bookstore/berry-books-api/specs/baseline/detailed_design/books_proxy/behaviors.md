# books_proxy - 振る舞い仕様書（単体テスト用）

ドメイン名: books_proxy  
バージョン: 1.0.0  
最終更新日: 2026-02-07

記法: シナリオは Gherkin 記法で記述する（Feature, Scenario, Given, When, Then, And, But）。principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照すること。

---

## 1. 概要

本文書は、books_proxyドメインの単体テスト用の振る舞い、テストシナリオ、受入基準を記述する。

テスト対象:
* BookResource（JAX-RS）

単体テストの範囲:
* books_proxyドメイン内の機能をテスト
* 外部API連携（BackOfficeRestClient）はモック化
* 結合テストシナリオは ../../basic_design/books_proxy/behaviors.md を参照すること
* E2Eテストシナリオは ../../requirements/behaviors.md を参照すること

関連ドキュメント:
* [detailed_design.md](detailed_design.md) - 詳細設計書
* [../../basic_design/books_proxy/functional_design.md](../../basic_design/books_proxy/functional_design.md) - 書籍プロキシ機能設計書
* [../../basic_design/books_proxy/behaviors.md](../../basic_design/books_proxy/behaviors.md) - 書籍プロキシ振る舞い仕様書（結合テスト用）

---

## 2. テストシナリオ

### 2.1 BookResource - 書籍一覧取得（正常系）

#### Feature: 書籍一覧取得

#### Scenario: 全書籍を取得する

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: `getAllBooks()` が BookTO のリストを返す

* When（操作）:
  * BookResource.getAllBooks() を呼び出す

* Then（期待結果）:
  * HTTPステータス 200 OK が返される
  * レスポンスボディに BookTO のリスト（JSON）が含まれる
  * BackOfficeRestClient.getAllBooks() が1回呼ばれる

#### テストデータ

* モックが返すデータ:
  ```json
  [
    {
      "bookId": 1,
      "bookName": "Java完全理解",
      "author": "著者A",
      "categoryId": 1,
      "categoryName": "技術",
      "publisherId": 1,
      "publisherName": "出版社A",
      "price": 3000,
      "quantity": 10,
      "version": 1
    },
    {
      "bookId": 2,
      "bookName": "Spring Boot入門",
      "author": "著者B",
      "categoryId": 1,
      "categoryName": "技術",
      "publisherId": 2,
      "publisherName": "出版社B",
      "price": 2500,
      "quantity": 5,
      "version": 1
    }
  ]
  ```

---

### 2.2 BookResource - 書籍詳細取得（正常系）

#### Feature: 書籍詳細取得

#### Scenario: 指定された書籍IDの詳細を取得する

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: `getBookById(1)` が BookTO を返す

* When（操作）:
  * BookResource.getBookById(1) を呼び出す

* Then（期待結果）:
  * HTTPステータス 200 OK が返される
  * レスポンスボディに BookTO（JSON）が含まれる
  * BackOfficeRestClient.getBookById(1) が1回呼ばれる

#### テストデータ

* 入力: `bookId = 1`
* モックが返すデータ:
  ```json
  {
    "bookId": 1,
    "bookName": "Java完全理解",
    "author": "著者A",
    "categoryId": 1,
    "categoryName": "技術",
    "publisherId": 1,
    "publisherName": "出版社A",
    "price": 3000,
    "quantity": 10,
    "version": 1
  }
  ```

---

### 2.3 BookResource - 書籍詳細取得（異常系: 書籍が見つからない）

#### Feature: 書籍詳細取得

#### Scenario: 存在しない書籍IDを指定した場合

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: `getBookById(999)` が WebApplicationException（404 Not Found）をスローする

* When（操作）:
  * BookResource.getBookById(999) を呼び出す

* Then（期待結果）:
  * WebApplicationException（404 Not Found）がスローされる
  * レスポンスボディにエラーメッセージが含まれる

#### テストデータ

* 入力: `bookId = 999`（存在しない書籍ID）

---

### 2.4 BookResource - 書籍検索（JPQL、正常系）

#### Feature: 書籍検索（JPQL）

#### Scenario: カテゴリIDで書籍を検索する

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: `searchBooksJpql(1, null)` が BookTO のリストを返す

* When（操作）:
  * BookResource.searchBooksJpql(1, null) を呼び出す

* Then（期待結果）:
  * HTTPステータス 200 OK が返される
  * レスポンスボディに BookTO のリスト（JSON）が含まれる
  * BackOfficeRestClient.searchBooksJpql(1, null) が1回呼ばれる

#### テストデータ

* 入力: `categoryId = 1`, `keyword = null`
* モックが返すデータ:
  ```json
  [
    {
      "bookId": 1,
      "bookName": "Java完全理解",
      "categoryId": 1,
      "categoryName": "技術"
    }
  ]
  ```

---

### 2.5 BookResource - 書籍検索（JPQL、キーワード検索）

#### Feature: 書籍検索（JPQL）

#### Scenario: キーワードで書籍を検索する

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: `searchBooksJpql(null, "Java")` が BookTO のリストを返す

* When（操作）:
  * BookResource.searchBooksJpql(null, "Java") を呼び出す

* Then（期待結果）:
  * HTTPステータス 200 OK が返される
  * レスポンスボディに BookTO のリスト（JSON）が含まれる
  * BackOfficeRestClient.searchBooksJpql(null, "Java") が1回呼ばれる

#### テストデータ

* 入力: `categoryId = null`, `keyword = "Java"`
* モックが返すデータ:
  ```json
  [
    {
      "bookId": 1,
      "bookName": "Java完全理解"
    },
    {
      "bookId": 2,
      "bookName": "Java入門"
    }
  ]
  ```

---

### 2.6 BookResource - 書籍検索（Criteria API、正常系）

#### Feature: 書籍検索（Criteria API）

#### Scenario: カテゴリIDとキーワードで書籍を検索する

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: `searchBooksCriteria(1, "Java")` が BookTO のリストを返す

* When（操作）:
  * BookResource.searchBooksCriteria(1, "Java") を呼び出す

* Then（期待結果）:
  * HTTPステータス 200 OK が返される
  * レスポンスボディに BookTO のリスト（JSON）が含まれる
  * BackOfficeRestClient.searchBooksCriteria(1, "Java") が1回呼ばれる

#### テストデータ

* 入力: `categoryId = 1`, `keyword = "Java"`
* モックが返すデータ:
  ```json
  [
    {
      "bookId": 1,
      "bookName": "Java完全理解",
      "categoryId": 1,
      "categoryName": "技術"
    }
  ]
  ```

---

### 2.7 BookResource - カテゴリ一覧取得（正常系）

#### Feature: カテゴリ一覧取得

#### Scenario: カテゴリ一覧をマップ形式で取得する

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: `getAllCategories()` がカテゴリマップを返す

* When（操作）:
  * BookResource.getAllCategories() を呼び出す

* Then（期待結果）:
  * HTTPステータス 200 OK が返される
  * レスポンスボディにカテゴリマップ（JSON）が含まれる
  * BackOfficeRestClient.getAllCategories() が1回呼ばれる

#### テストデータ

* モックが返すデータ:
  ```json
  {
    "文学": 1,
    "ビジネス": 2,
    "技術": 3
  }
  ```

---

### 2.8 BookResource - 外部APIエラー（異常系）

#### Feature: 外部APIエラーハンドリング

#### Scenario: 外部API呼び出しでネットワークエラーが発生した場合

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: `getAllBooks()` が ProcessingException をスローする

* When（操作）:
  * BookResource.getAllBooks() を呼び出す

* Then（期待結果）:
  * ProcessingException がスローされる
  * 例外マッパーにより 503 Service Unavailable が返される

---

### 2.9 BookResource - 外部APIタイムアウト（異常系）

#### Feature: 外部APIタイムアウトハンドリング

#### Scenario: 外部API呼び出しでタイムアウトが発生した場合

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: `getBookById(1)` が ProcessingException（タイムアウト）をスローする

* When（操作）:
  * BookResource.getBookById(1) を呼び出す

* Then（期待結果）:
  * ProcessingException がスローされる
  * 例外マッパーにより 503 Service Unavailable が返される

---

### 2.10 BookResource - パラメータ検証（境界値）

#### Feature: パラメータ検証

#### Scenario: 書籍ID = 0（最小値）で詳細を取得する

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: `getBookById(0)` が BookTO を返す

* When（操作）:
  * BookResource.getBookById(0) を呼び出す

* Then（期待結果）:
  * HTTPステータス 200 OK が返される
  * BackOfficeRestClient.getBookById(0) が1回呼ばれる

#### テストデータ（境界値）

* 最小値: `bookId = 0`
* 通常値: `bookId = 1`
* null: JAX-RSパスパラメータのため、nullは不可（404 Not Found）

---

### 2.11 BookResource - 空の結果（正常系）

#### Feature: 空の結果

#### Scenario: 検索結果が0件の場合

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * モック設定: `searchBooksJpql(999, "存在しないキーワード")` が空のリストを返す

* When（操作）:
  * BookResource.searchBooksJpql(999, "存在しないキーワード") を呼び出す

* Then（期待結果）:
  * HTTPステータス 200 OK が返される
  * レスポンスボディに空の配列（JSON）が含まれる

#### テストデータ

* 入力: `categoryId = 999`, `keyword = "存在しないキーワード"`
* 期待される出力:
  ```json
  []
  ```

---

## 3. モック化の方針

### 3.1 ドメイン内の依存関係

* BookResource → モック不要（テスト対象）

### 3.2 ドメイン外の依存関係

* BackOfficeRestClient（commonドメイン） → モック化
* 外部API（back-office-api） → モック化（WireMock、結合テストで使用）

---

## 4. カバレッジ目標

* ステートメントカバレッジ: 80%以上
* ブランチカバレッジ: 70%以上

---

## 5. 受入基準

### 5.1 機能要件

* [ ] すべての正常系テストが成功する
* [ ] すべての異常系テストが成功する
* [ ] すべての境界値テストが成功する
* [ ] 外部APIエラー時のハンドリングが適切に動作する

### 5.2 品質要件

* [ ] カバレッジ目標を達成する
* [ ] テストコードにコメントが適切に記載されている
* [ ] テストケースが独立している（テスト間の依存関係がない）
* [ ] モック化が適切に行われている

---

## 6. 参考資料

* [detailed_design.md](detailed_design.md) - 詳細設計書
* [../../basic_design/books_proxy/functional_design.md](../../basic_design/books_proxy/functional_design.md) - 書籍プロキシ機能設計書
* [../../basic_design/books_proxy/behaviors.md](../../basic_design/books_proxy/behaviors.md) - 書籍プロキシ振る舞い仕様書（結合テスト用）
* [../../requirements/behaviors.md](../../requirements/behaviors.md) - システム振る舞い仕様書（E2Eテスト用）
* [../common/detailed_design.md](../common/detailed_design.md) - 共通ドメイン詳細設計書
