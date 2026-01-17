# berry-books-api - API_002_books 単体テスト仕様書

API ID: API_002_books  
API名: 書籍API  
バージョン: 1.0.0  
最終更新日: 2026-01-18  
ステータス: テスト仕様確定

---

## 1. 概要

本文書は、書籍API（API_002_books）の単体テスト仕様を記述する。

* テスト対象: BookResource、CategoryResource
* テスト方針: 純粋な単体テスト（依存関係はモック化）
* モック対象: BackOfficeRestClient
* テストフレームワーク: JUnit 5、Mockito

重要:
* これは純粋な単体テストの仕様である
* E2Eテスト（システム統合テスト）の仕様は [basic_design/behaviors.md](../../basic_design/behaviors.md) を参照
* 実際のDBアクセス、外部API呼び出しは行わない

---

## 2. テスト戦略

### 2.1 テスト対象範囲

| クラス | テスト対象メソッド | モック対象 |
|-------|----------------|----------|
| BookResource | getAllBooks() | BackOfficeRestClient |
| BookResource | getBookById(int bookId) | BackOfficeRestClient |
| BookResource | searchBooksJpql(...) | BackOfficeRestClient |
| BookResource | searchBooksCriteria(...) | BackOfficeRestClient |
| CategoryResource | getAllCategories() | BackOfficeRestClient |

### 2.2 モック戦略

* `@Mock private BackOfficeRestClient backOfficeClient;` - 外部API連携をモック化
* `@InjectMocks private BookResource bookResource;` - テスト対象にモックを注入
* `@InjectMocks private CategoryResource categoryResource;` - テスト対象にモックを注入

### 2.3 テストに含まれないもの

* 外部API（back-office-api-sdd）の実際の呼び出し
* HTTPリクエスト/レスポンスの実際の送受信
* JAX-RS フレームワークの動作検証
* 複数クラスにまたがる統合シナリオ

---

## 3. BookResource 単体テスト

### 3.1 getAllBooks() - 全書籍取得

#### 3.1.1 正常系

| テストケースID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-------------|------|----------------|------------|---------------|
| BOOK-GET-ALL-001 | 書籍一覧を正常に取得できる | BackOfficeRestClient.getAllBooks()が書籍リスト（3件）を返す | getAllBooks()を呼び出す | Response（200 OK）が返される<br/>ボディに書籍リスト（3件）が含まれる<br/>INFOログが出力される |
| BOOK-GET-ALL-002 | 書籍が0件の場合、空リストを返す | BackOfficeRestClient.getAllBooks()が空リストを返す | getAllBooks()を呼び出す | Response（200 OK）が返される<br/>ボディに空リスト[]が含まれる |

##### BOOK-GET-ALL-001 の詳細

* Given:
  * BookTO 3件を含むリストを作成
  * `when(backOfficeClient.getAllBooks()).thenReturn(bookList);`

* When:
  * `Response response = bookResource.getAllBooks();`

* Then:
  * `assertEquals(200, response.getStatus());`
  * `List<BookTO> result = (List<BookTO>) response.getEntity();`
  * `assertEquals(3, result.size());`
  * Loggerのverify: `verify(logger).info("全書籍取得開始")`（Mockitoでログ検証）

#### 3.1.2 異常系

| テストケースID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-------------|------|----------------|------------|---------------|
| BOOK-GET-ALL-E001 | 外部API接続エラー時、例外がスローされる | BackOfficeRestClient.getAllBooks()がRuntimeExceptionをスロー | getAllBooks()を呼び出す | RuntimeExceptionがスローされる<br/>（GenericExceptionMapperで500エラーに変換される） |

---

### 3.2 getBookById(int bookId) - 書籍詳細取得

#### 3.2.1 正常系

| テストケースID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-------------|------|----------------|------------|---------------|
| BOOK-GET-BY-ID-001 | 指定IDの書籍を正常に取得できる | BackOfficeRestClient.getBookById(1)が書籍情報（BookTO）を返す | getBookById(1)を呼び出す | Response（200 OK）が返される<br/>ボディに書籍情報が含まれる<br/>INFOログが出力される |

##### BOOK-GET-BY-ID-001 の詳細

* Given:
  * BookTOを作成（bookId=1, bookName="Java完全理解", ...）
  * `when(backOfficeClient.getBookById(1)).thenReturn(bookTO);`

* When:
  * `Response response = bookResource.getBookById(1);`

* Then:
  * `assertEquals(200, response.getStatus());`
  * `BookTO result = (BookTO) response.getEntity();`
  * `assertEquals(1, result.bookId());`
  * `assertEquals("Java完全理解", result.bookName());`

#### 3.2.2 異常系

| テストケースID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-------------|------|----------------|------------|---------------|
| BOOK-GET-BY-ID-E001 | 指定IDの書籍が存在しない場合、404エラー | BackOfficeRestClient.getBookById(999)がnullを返す | getBookById(999)を呼び出す | Response（404 Not Found）が返される<br/>ボディにErrorResponseが含まれる<br/>WARNログが出力される |
| BOOK-GET-BY-ID-E002 | 外部API接続エラー時、例外がスローされる | BackOfficeRestClient.getBookById(1)がRuntimeExceptionをスロー | getBookById(1)を呼び出す | RuntimeExceptionがスローされる |

##### BOOK-GET-BY-ID-E001 の詳細

* Given:
  * `when(backOfficeClient.getBookById(999)).thenReturn(null);`

* When:
  * `Response response = bookResource.getBookById(999);`

* Then:
  * `assertEquals(404, response.getStatus());`
  * `ErrorResponse error = (ErrorResponse) response.getEntity();`
  * `assertEquals("書籍が見つかりません", error.message());`
  * Loggerのverify: `verify(logger).warn("書籍が見つかりません: bookId={}", 999)`

---

### 3.3 searchBooksJpql(Integer categoryId, String keyword) - 書籍検索（JPQL版）

#### 3.3.1 正常系

| テストケースID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-------------|------|----------------|------------|---------------|
| BOOK-SEARCH-JPQL-001 | カテゴリIDで検索できる | BackOfficeRestClient.searchBooksJpql(1, null)が書籍リスト（2件）を返す | searchBooksJpql(1, null)を呼び出す | Response（200 OK）が返される<br/>ボディに書籍リスト（2件）が含まれる |
| BOOK-SEARCH-JPQL-002 | キーワードで検索できる | BackOfficeRestClient.searchBooksJpql(null, "Java")が書籍リスト（3件）を返す | searchBooksJpql(null, "Java")を呼び出す | Response（200 OK）が返される<br/>ボディに書籍リスト（3件）が含まれる |
| BOOK-SEARCH-JPQL-003 | カテゴリID+キーワードで検索できる | BackOfficeRestClient.searchBooksJpql(1, "Java")が書籍リスト（1件）を返す | searchBooksJpql(1, "Java")を呼び出す | Response（200 OK）が返される<br/>ボディに書籍リスト（1件）が含まれる |
| BOOK-SEARCH-JPQL-004 | パラメータ未指定で全書籍を返す | BackOfficeRestClient.searchBooksJpql(null, null)が書籍リスト（10件）を返す | searchBooksJpql(null, null)を呼び出す | Response（200 OK）が返される<br/>ボディに書籍リスト（10件）が含まれる |
| BOOK-SEARCH-JPQL-005 | 検索結果が0件の場合、空リストを返す | BackOfficeRestClient.searchBooksJpql(null, "存在しないキーワード")が空リストを返す | searchBooksJpql(null, "存在しないキーワード")を呼び出す | Response（200 OK）が返される<br/>ボディに空リスト[]が含まれる |

##### BOOK-SEARCH-JPQL-001 の詳細

* Given:
  * BookTO 2件を含むリストを作成（カテゴリID=1）
  * `when(backOfficeClient.searchBooksJpql(1, null)).thenReturn(bookList);`

* When:
  * `Response response = bookResource.searchBooksJpql(1, null);`

* Then:
  * `assertEquals(200, response.getStatus());`
  * `List<BookTO> result = (List<BookTO>) response.getEntity();`
  * `assertEquals(2, result.size());`

#### 3.3.2 異常系

| テストケースID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-------------|------|----------------|------------|---------------|
| BOOK-SEARCH-JPQL-E001 | 外部API接続エラー時、例外がスローされる | BackOfficeRestClient.searchBooksJpql(...)がRuntimeExceptionをスロー | searchBooksJpql(...)を呼び出す | RuntimeExceptionがスローされる |

---

### 3.4 searchBooksCriteria(Integer categoryId, String keyword) - 書籍検索（Criteria API版）

#### 3.4.1 正常系

| テストケースID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-------------|------|----------------|------------|---------------|
| BOOK-SEARCH-CRITERIA-001 | カテゴリIDで検索できる | BackOfficeRestClient.searchBooksCriteria(1, null)が書籍リスト（2件）を返す | searchBooksCriteria(1, null)を呼び出す | Response（200 OK）が返される<br/>ボディに書籍リスト（2件）が含まれる |
| BOOK-SEARCH-CRITERIA-002 | キーワードで検索できる | BackOfficeRestClient.searchBooksCriteria(null, "Java")が書籍リスト（3件）を返す | searchBooksCriteria(null, "Java")を呼び出す | Response（200 OK）が返される<br/>ボディに書籍リスト（3件）が含まれる |
| BOOK-SEARCH-CRITERIA-003 | カテゴリID+キーワードで検索できる | BackOfficeRestClient.searchBooksCriteria(1, "Java")が書籍リスト（1件）を返す | searchBooksCriteria(1, "Java")を呼び出す | Response（200 OK）が返される<br/>ボディに書籍リスト（1件）が含まれる |
| BOOK-SEARCH-CRITERIA-004 | パラメータ未指定で全書籍を返す | BackOfficeRestClient.searchBooksCriteria(null, null)が書籍リスト（10件）を返す | searchBooksCriteria(null, null)を呼び出す | Response（200 OK）が返される<br/>ボディに書籍リスト（10件）が含まれる |
| BOOK-SEARCH-CRITERIA-005 | 検索結果が0件の場合、空リストを返す | BackOfficeRestClient.searchBooksCriteria(null, "存在しないキーワード")が空リストを返す | searchBooksCriteria(null, "存在しないキーワード")を呼び出す | Response（200 OK）が返される<br/>ボディに空リスト[]が含まれる |

#### 3.4.2 異常系

| テストケースID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-------------|------|----------------|------------|---------------|
| BOOK-SEARCH-CRITERIA-E001 | 外部API接続エラー時、例外がスローされる | BackOfficeRestClient.searchBooksCriteria(...)がRuntimeExceptionをスロー | searchBooksCriteria(...)を呼び出す | RuntimeExceptionがスローされる |

---

## 4. CategoryResource 単体テスト

### 4.1 getAllCategories() - カテゴリ一覧取得

#### 4.1.1 正常系

| テストケースID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-------------|------|----------------|------------|---------------|
| CATEGORY-GET-ALL-001 | カテゴリ一覧を正常に取得できる | BackOfficeRestClient.getAllCategories()がカテゴリマップ（3件）を返す | getAllCategories()を呼び出す | Response（200 OK）が返される<br/>ボディにカテゴリマップが含まれる<br/>INFOログが出力される |
| CATEGORY-GET-ALL-002 | カテゴリが0件の場合、空マップを返す | BackOfficeRestClient.getAllCategories()が空マップを返す | getAllCategories()を呼び出す | Response（200 OK）が返される<br/>ボディに空マップ{}が含まれる |

##### CATEGORY-GET-ALL-001 の詳細

* Given:
  * カテゴリマップを作成（例: {"Java": 1, "JavaScript": 2, "Python": 3}）
  * `when(backOfficeClient.getAllCategories()).thenReturn(categoryMap);`

* When:
  * `Response response = categoryResource.getAllCategories();`

* Then:
  * `assertEquals(200, response.getStatus());`
  * `Map<String, Integer> result = (Map<String, Integer>) response.getEntity();`
  * `assertEquals(3, result.size());`
  * `assertEquals(1, result.get("Java"));`

#### 4.1.2 異常系

| テストケースID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-------------|------|----------------|------------|---------------|
| CATEGORY-GET-ALL-E001 | 外部API接続エラー時、例外がスローされる | BackOfficeRestClient.getAllCategories()がRuntimeExceptionをスロー | getAllCategories()を呼び出す | RuntimeExceptionがスローされる |

---

## 5. テストデータ

### 5.1 BookTO サンプルデータ

```java
BookTO createSampleBook(int bookId, String bookName) {
    return new BookTO(
        bookId,
        bookName,
        "著者名",
        new CategoryTO(1, "Java"),
        new PublisherTO(1, "技術評論社"),
        3200,
        10,
        1L
    );
}
```

### 5.2 CategoryTO サンプルデータ

```java
CategoryTO createSampleCategory(int categoryId, String categoryName) {
    return new CategoryTO(categoryId, categoryName);
}
```

### 5.3 ErrorResponse サンプルデータ

```java
ErrorResponse createNotFoundError() {
    return new ErrorResponse(
        404,
        "Not Found",
        "書籍が見つかりません",
        "/api/books/999"
    );
}
```

---

## 6. テスト実装例

### 6.1 BookResourceTest のセットアップ

```java
@ExtendWith(MockitoExtension.class)
class BookResourceTest {

    @Mock
    private BackOfficeRestClient backOfficeClient;

    @InjectMocks
    private BookResource bookResource;

    private BookTO sampleBook;
    private List<BookTO> bookList;

    @BeforeEach
    void setUp() {
        sampleBook = new BookTO(
            1,
            "Java完全理解",
            "山田太郎",
            new CategoryTO(1, "Java"),
            new PublisherTO(1, "技術評論社"),
            3200,
            10,
            1L
        );
        
        bookList = Arrays.asList(sampleBook);
    }

    @Test
    @DisplayName("全書籍取得 - 正常系")
    void testGetAllBooks_Success() {
        // Given
        when(backOfficeClient.getAllBooks()).thenReturn(bookList);

        // When
        Response response = bookResource.getAllBooks();

        // Then
        assertEquals(200, response.getStatus());
        List<BookTO> result = (List<BookTO>) response.getEntity();
        assertEquals(1, result.size());
        assertEquals("Java完全理解", result.get(0).bookName());
        
        // Verify
        verify(backOfficeClient).getAllBooks();
    }

    @Test
    @DisplayName("書籍詳細取得 - 404 Not Found")
    void testGetBookById_NotFound() {
        // Given
        when(backOfficeClient.getBookById(999)).thenReturn(null);

        // When
        Response response = bookResource.getBookById(999);

        // Then
        assertEquals(404, response.getStatus());
        ErrorResponse error = (ErrorResponse) response.getEntity();
        assertEquals("書籍が見つかりません", error.message());
        
        // Verify
        verify(backOfficeClient).getBookById(999);
    }
}
```

---

## 7. カバレッジ目標

### 7.1 カバレッジ基準

* 行カバレッジ: 80%以上
* 分岐カバレッジ: 80%以上
* メソッドカバレッジ: 100%

### 7.2 カバレッジ測定

* ツール: JaCoCo
* 実行コマンド: `./gradlew :berry-books-api-sdd:jacocoTestReport`
* レポート出力先: `build/reports/jacoco/test/html/index.html`

---

## 8. 受入基準

### 8.1 単体テスト受入基準

* [ ] 全テストケースがパスする
* [ ] カバレッジ基準を満たす（80%以上）
* [ ] BackOfficeRestClientが正しくモック化されている
* [ ] 正常系・異常系の両方がテストされている
* [ ] ログ出力が適切に検証されている

### 8.2 テスト実行環境

* JUnit 5.9+
* Mockito 4.0+
* JDK 21
* Gradle 8.x

---

## 9. 参考資料

### 9.1 関連ドキュメント

* [detailed_design.md](detailed_design.md) - 詳細設計書
* [basic_design/behaviors.md](../../basic_design/behaviors.md) - E2Eテスト仕様書
* [common/detailed_design.md](../common/detailed_design.md) - 共通機能詳細設計書

### 9.2 外部リソース

* [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
* [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
* [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
