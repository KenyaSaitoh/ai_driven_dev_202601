# API_002_books - 書籍API受入基準

* API ID: API_002_books  
* API名: 書籍API  
* ベースパス: `/api/books`  
* バージョン: 2.0.0  
* 最終更新日: 2025-01-02

---

## 1. 概要

本文書は、書籍APIの受入基準を記述する。各エンドポイントについて、正常系・異常系の振る舞いを定義し、テストシナリオの基礎とする。

---

## 2. 書籍一覧取得 (`GET /api/books`)

### 2.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| BOOK-LIST-001 | すべての書籍を取得できる | 書籍が登録されている | /api/booksにリクエスト | 200 OK<br/>書籍一覧が返される<br/>削除済み書籍は含まれない |
| BOOK-LIST-002 | 書籍情報にカテゴリ・出版社・在庫情報が含まれる | 書籍が登録されている | /api/booksにリクエスト | 200 OK<br/>各書籍にcategory, publisher, quantity, versionが含まれる |
| BOOK-LIST-003 | 書籍がbookId昇順でソートされる | 複数の書籍が登録されている | /api/booksにリクエスト | 200 OK<br/>bookId昇順で返される |
| BOOK-LIST-004 | 削除済み書籍は含まれない | deleted=trueの書籍が存在 | /api/booksにリクエスト | 200 OK<br/>deleted=falseの書籍のみ返される |
| BOOK-LIST-005 | 書籍が0件の場合、空配列を返す | 書籍が登録されていない | /api/booksにリクエスト | 200 OK<br/>空配列 [] が返される |

---

## 3. 書籍詳細取得 (`GET /api/books/{id}`)

### 3.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| BOOK-DETAIL-001 | 指定IDの書籍詳細を取得できる | bookId=1が存在する | /api/books/1にリクエスト | 200 OK<br/>書籍詳細が返される |
| BOOK-DETAIL-002 | 削除済み書籍も取得できる | bookId=1, deleted=true | /api/books/1にリクエスト | 200 OK<br/>削除済み書籍も返される |

### 3.2 異常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| BOOK-DETAIL-E001 | 指定IDの書籍が存在しない場合、エラー | bookId=999が存在しない | /api/books/999にリクエスト | 404 Not Found<br/>"書籍が見つかりません" |

---

## 4. 書籍検索（デフォルト） (`GET /api/books/search`)

### 4.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| BOOK-SEARCH-001 | カテゴリIDで検索できる | カテゴリ1に書籍が存在 | /api/books/search?categoryId=1 | 200 OK<br/>カテゴリ1の書籍一覧が返される |
| BOOK-SEARCH-002 | キーワードで検索できる | 書籍名に"Java"を含む書籍が存在 | /api/books/search?keyword=Java | 200 OK<br/>書籍名または著者名に"Java"を含む書籍が返される |
| BOOK-SEARCH-003 | カテゴリとキーワードで絞り込み検索できる | - | /api/books/search?categoryId=1&keyword=Java | 200 OK<br/>カテゴリ1かつキーワード"Java"を含む書籍が返される |
| BOOK-SEARCH-004 | パラメータなしで全件取得できる | - | /api/books/search | 200 OK<br/>全書籍が返される |
| BOOK-SEARCH-005 | キーワードは部分一致検索 | 書籍名="Java入門" | /api/books/search?keyword=入門 | 200 OK<br/>書籍名="Java入門"が含まれる |
| BOOK-SEARCH-006 | 検索結果が0件の場合、空配列を返す | 該当する書籍がない | /api/books/search?keyword=NOTEXIST | 200 OK<br/>空配列 [] が返される |

---

## 5. 書籍検索（JPQL） (`GET /api/books/search/jpql`)

### 5.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| BOOK-JPQL-001 | JPQL方式でカテゴリ検索できる | - | /api/books/search/jpql?categoryId=1 | 200 OK<br/>JPQL動的クエリで検索成功 |
| BOOK-JPQL-002 | JPQL方式でキーワード検索できる | - | /api/books/search/jpql?keyword=Java | 200 OK<br/>JPQL LIKE句で検索成功 |
| BOOK-JPQL-003 | JPQL方式で複合検索できる | - | /api/books/search/jpql?categoryId=1&keyword=Java | 200 OK<br/>JPQL WHERE条件を動的に構築して検索 |

---

## 6. 書籍検索（Criteria API） (`GET /api/books/search/criteria`)

### 6.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| BOOK-CRITERIA-001 | Criteria API方式でカテゴリ検索できる | - | /api/books/search/criteria?categoryId=1 | 200 OK<br/>CriteriaBuilderで検索成功 |
| BOOK-CRITERIA-002 | Criteria API方式でキーワード検索できる | - | /api/books/search/criteria?keyword=Java | 200 OK<br/>Predicate.like()で検索成功 |
| BOOK-CRITERIA-003 | Criteria API方式で複合検索できる | - | /api/books/search/criteria?categoryId=1&keyword=Java | 200 OK<br/>Predicate.and()で条件結合して検索 |
| BOOK-CRITERIA-004 | タイプセーフなクエリ構築 | - | /api/books/search/criteria?categoryId=1 | 200 OK<br/>コンパイル時に型チェック |

---

## 7. カテゴリ一覧取得 (`GET /api/books/categories`)

### 7.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| BOOK-CAT-001 | カテゴリ一覧をMap形式で取得できる | カテゴリが登録されている | /api/books/categories | 200 OK<br/>{"カテゴリ名": ID}のMap形式で返される |
| BOOK-CAT-002 | レスポンス形式が正しい | カテゴリ"文学"(ID=1)が存在 | /api/books/categories | 200 OK<br/>{"文学": 1, ...} |

---

## 8. データ整合性受入基準

### 8.1 データ結合

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| DATA-JOIN-001 | カテゴリ情報が正しく結合される | 書籍にcategoryId=1が設定 | /api/books | 200 OK<br/>category.categoryId=1, categoryName="文学" |
| DATA-JOIN-002 | 出版社情報が正しく結合される | 書籍にpublisherId=1が設定 | /api/books | 200 OK<br/>publisher.publisherId=1, publisherName="出版社A" |
| DATA-JOIN-003 | 在庫情報が正しく結合される | 書籍のSTOCKにquantity=10, version=1 | /api/books | 200 OK<br/>quantity=10, version=1 |

---

## 9. パフォーマンス受入基準

### 9.1 レスポンスタイム

| シナリオID | API | 受入基準 |
|-----------|-----|---------|
| PERF-001 | GET /api/books | 500ms以内（95パーセンタイル） |
| PERF-002 | GET /api/books/{id} | 500ms以内（95パーセンタイル） |
| PERF-003 | GET /api/books/search | 500ms以内（95パーセンタイル） |
| PERF-004 | GET /api/books/search/jpql | 500ms以内（95パーセンタイル） |
| PERF-005 | GET /api/books/search/criteria | 500ms以内（95パーセンタイル） |

### 9.2 N+1問題の防止

| シナリオID | 説明 | 受入基準 |
|-----------|------|---------|
| PERF-N1-001 | LEFT JOIN FETCHでカテゴリを一括取得 | 書籍100件取得時、SQLクエリが1回のみ |
| PERF-N1-002 | LEFT JOIN FETCHで出版社を一括取得 | 書籍100件取得時、SQLクエリが1回のみ |
| PERF-N1-003 | SecondaryTableで在庫を一括取得 | 書籍100件取得時、SQLクエリが1回のみ |

---

## 10. ログ出力受入基準

### 10.1 ログレベル

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| LOG-001 | 書籍一覧取得時はINFOレベルでログ出力 | - | /api/books | INFO: [BookResource#getAllBooks]<br/>INFO: [BookService#getAllBooks] |
| LOG-002 | 書籍詳細取得成功時はINFOレベル | bookId=1が存在 | /api/books/1 | INFO: [BookResource#getBook]<br/>INFO: [BookService#getBook] Success: 1 |
| LOG-003 | 書籍が見つからない時はWARNレベル | bookId=999が存在しない | /api/books/999 | WARN: [BookResource#getBook] Book not found: 999 |
| LOG-004 | 検索実行時はINFOレベル | - | /api/books/search | INFO: [BookResource#searchBooks]<br/>INFO: [BookService#searchBooks] |

---

## 11. 並行処理受入基準

### 11.1 同時リクエスト

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| CONC-001 | 複数ユーザーが同時に書籍一覧を取得できる | - | 3ユーザーが同時に/api/books | 全リクエストが成功<br/>独立したトランザクション |
| CONC-002 | 読み取り専用のため競合なし | - | 同時に/api/books | データ競合なし<br/>ロック不要 |

---

## 12. 将来の拡張受入基準

### 12.1 ページネーション（実装予定）

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| PAGE-001 | ページサイズを指定できる | - | /api/books?page=0&size=20 | 20件ずつ取得 |
| PAGE-002 | 総ページ数が返される | - | /api/books?page=0&size=20 | totalPages, totalElementsが含まれる |

### 12.2 ソート（実装予定）

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| SORT-001 | 価格順でソートできる | - | /api/books?sort=price,asc | 価格昇順で返される |
| SORT-002 | 書籍名順でソートできる | - | /api/books?sort=bookName,desc | 書籍名降順で返される |

---

## 13. 関連ドキュメント

* [functional_design.md](functional_design.md) - 書籍API機能設計書
* [../../system/behaviors.md](../../system/behaviors.md) - 全体受入基準
* [../../system/architecture_design.md](../../system/architecture_design.md) - アーキテクチャ設計書

