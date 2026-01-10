# API_002_books - 書籍APIタスク

**担当者:** 1名  
**推奨スキル:** JAX-RS、JPA、JPQL、Criteria API  
**想定工数:** 12時間  
**依存タスク:** [common_tasks.md](common_tasks.md)

---

## 概要

このタスクリストは、書籍API（一覧取得、詳細取得、検索）の実装タスクを含みます。JPQL検索とCriteria API検索の両方を実装します。

---

## タスクリスト

### T_API002_001: BookTOの作成

- **目的**: 書籍情報レスポンス用のDTOを実装する
- **対象**: `pro.kensait.backoffice.api.dto.BookTO`（Record）
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「2.1 GET /api/books」
- **注意事項**: 
  - Java Record形式で実装
  - フィールド: 
    - `bookId`（Integer）
    - `bookName`（String）
    - `author`（String）
    - `category`（CategoryInfo）
    - `publisher`（PublisherInfo）
    - `price`（BigDecimal）
    - `imageUrl`（String）
    - `quantity`（Integer）- 在庫数
    - `version`（Long）- 楽観的ロック用バージョン
    - `deleted`（Boolean）- 論理削除フラグ

---

### T_API002_002: BookServiceの作成

- **目的**: 書籍検索ビジネスロジックを実装する
- **対象**: `pro.kensait.backoffice.service.BookService`
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3. ビジネスロジック」
- **注意事項**: 
  - `@ApplicationScoped`、`@Transactional`
  - BookDao、BookDaoCriteriaを`@Inject`
  - メソッド:
    - `getBooksAll()`: 全書籍取得（論理削除を除外）
    - `getBook(Integer bookId)`: 書籍詳細取得
    - `searchBook(Integer categoryId)`: カテゴリ別検索
    - `searchBook(String keyword)`: キーワード検索
    - `searchBook(Integer categoryId, String keyword)`: 複合検索
  - BookエンティティをBookTOに変換
  - 論理削除された書籍は検索結果から除外（`deleted = false`）
  - 書籍詳細取得では論理削除された書籍も取得可能

---

### T_API002_003: BookResourceの作成

- **目的**: 書籍APIのエンドポイントを実装する
- **対象**: `pro.kensait.backoffice.api.BookResource`
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「2. エンドポイント仕様」
  - [behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) の「2. 書籍API」
- **注意事項**: 
  - `@Path("/books")`、`@ApplicationScoped`
  - BookServiceを`@Inject`
  - エンドポイント:
    - `@GET`: 全書籍取得
    - `@GET @Path("/{id}")`: 書籍詳細取得
    - `@GET @Path("/search/jpql")`: JPQL検索
      - クエリパラメータ: `categoryId`（Integer、オプション）、`keyword`（String、オプション）
    - `@GET @Path("/search/criteria")`: Criteria API検索
      - クエリパラメータ: `categoryId`（Integer、オプション）、`keyword`（String、オプション）
  - レスポンス: List<BookTO> または BookTO
  - HTTPステータス: 200 OK、404 Not Found（書籍が存在しない場合）
  - ログ出力: INFO（API呼び出し）

---

### T_API002_004: 書籍検索の単体テスト（JPQL）

- **目的**: BookService（JPQL検索）のテストケースを実装する
- **対象**: `pro.kensait.backoffice.service.BookServiceTest`
- **参照SPEC**: 
  - [behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) の「2. 書籍API」
- **注意事項**: 
  - JUnit 5 + Mockito使用
  - BookDaoをモック化
  - テストケース:
    - `testGetBooksAll()`: 全書籍取得
    - `testGetBook_Found()`: 書籍詳細取得（存在する）
    - `testGetBook_NotFound()`: 書籍詳細取得（存在しない）
    - `testSearchBook_ByCategory()`: カテゴリ別検索
    - `testSearchBook_ByKeyword()`: キーワード検索
    - `testSearchBook_ByCategoryAndKeyword()`: 複合検索

---

### T_API002_005: 書籍検索の単体テスト（Criteria API）

- **目的**: BookDaoCriteria（Criteria API検索）のテストケースを実装する
- **対象**: `pro.kensait.backoffice.dao.BookDaoCriteriaTest`
- **参照SPEC**: 
  - [behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) の「2. 書籍API」
- **注意事項**: 
  - JUnit 5使用（モックなし、実際のEntityManager使用が望ましい）
  - テストケース:
    - `testSearch_ByCategoryOnly()`: カテゴリのみ指定
    - `testSearch_ByKeywordOnly()`: キーワードのみ指定
    - `testSearch_ByCategoryAndKeyword()`: 両方指定
    - `testSearch_NoCondition()`: 条件なし（全件取得）

---

## 完了確認

### チェックリスト

- [X] BookTO
- [X] BookService
- [X] BookResource
- [X] 書籍検索の単体テスト（JPQL）
- [X] 書籍検索の単体テスト（Criteria API）

### 動作確認

以下のcurlコマンドで動作確認:

#### 全書籍取得
```bash
curl -X GET http://localhost:8080/back-office-api-sdd/api/books
```

#### 書籍詳細取得
```bash
curl -X GET http://localhost:8080/back-office-api-sdd/api/books/1
```

#### 書籍検索（JPQL） - カテゴリ指定
```bash
curl -X GET "http://localhost:8080/back-office-api-sdd/api/books/search/jpql?categoryId=1"
```

#### 書籍検索（JPQL） - キーワード指定
```bash
curl -X GET "http://localhost:8080/back-office-api-sdd/api/books/search/jpql?keyword=Java"
```

#### 書籍検索（JPQL） - 複合条件
```bash
curl -X GET "http://localhost:8080/back-office-api-sdd/api/books/search/jpql?categoryId=1&keyword=Java"
```

#### 書籍検索（Criteria API） - 複合条件
```bash
curl -X GET "http://localhost:8080/back-office-api-sdd/api/books/search/criteria?categoryId=1&keyword=Java"
```

---

## 次のステップ

このAPI実装完了後、以下のタスクに並行して進めます:

- [API_003_categories.md](API_003_categories.md) - カテゴリAPI
- [API_004_publishers.md](API_004_publishers.md) - 出版社API
- [API_005_stocks.md](API_005_stocks.md) - 在庫API
- [API_006_workflows.md](API_006_workflows.md) - ワークフローAPI

---

**タスクファイル作成日:** 2025-01-10  
**想定実行順序:** 4番目（共通機能実装後）
