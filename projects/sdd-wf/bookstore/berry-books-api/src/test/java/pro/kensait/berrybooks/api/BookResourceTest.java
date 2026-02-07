package pro.kensait.berrybooks.api;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.berrybooks.external.BackOfficeRestClient;
import pro.kensait.berrybooks.external.dto.BookTO;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * BookResourceの単体テスト
 * 
 * books_proxyドメインの振る舞いを検証する。
 * 外部API（BackOfficeRestClient）はモック化する。
 * 
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class BookResourceTest {
    
    @InjectMocks
    private BookResource bookResource;
    
    @Mock
    private BackOfficeRestClient backOfficeRestClient;
    
    private List<BookTO> testBooks;
    private BookTO testBook;
    private Map<String, Integer> testCategories;
    
    @BeforeEach
    void setUp() {
        // テストデータの準備
        testBook = new BookTO(
            1,
            "Java完全理解",
            "著者A",
            1,
            "技術",
            1,
            "出版社A",
            3000,
            10,
            1L
        );
        
        BookTO testBook2 = new BookTO(
            2,
            "Spring Boot入門",
            "著者B",
            1,
            "技術",
            2,
            "出版社B",
            2500,
            5,
            1L
        );
        
        testBooks = List.of(testBook, testBook2);
        
        testCategories = new HashMap<>();
        testCategories.put("文学", 1);
        testCategories.put("ビジネス", 2);
        testCategories.put("技術", 3);
    }
    
    /**
     * Scenario: 全書籍を取得する
     * Given: BackOfficeRestClientがモック化されている
     * And: モック設定: getAllBooks() が BookTO のリストを返す
     * When: BookResource.getAllBooks() を呼び出す
     * Then: HTTPステータス 200 OK が返される
     * And: レスポンスボディに BookTO のリスト（JSON）が含まれる
     * And: BackOfficeRestClient.getAllBooks() が1回呼ばれる
     */
    @Test
    void testGetAllBooks_Success() {
        // Given
        when(backOfficeRestClient.getAllBooks()).thenReturn(testBooks);
        
        // When
        Response response = bookResource.getAllBooks();
        
        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<BookTO> books = (List<BookTO>) response.getEntity();
        assertNotNull(books);
        assertEquals(2, books.size());
        verify(backOfficeRestClient, times(1)).getAllBooks();
    }
    
    /**
     * Scenario: 指定された書籍IDの詳細を取得する
     * Given: BackOfficeRestClientがモック化されている
     * And: モック設定: getBookById(1) が BookTO を返す
     * When: BookResource.getBookById(1) を呼び出す
     * Then: HTTPステータス 200 OK が返される
     * And: レスポンスボディに BookTO（JSON）が含まれる
     * And: BackOfficeRestClient.getBookById(1) が1回呼ばれる
     */
    @Test
    void testGetBookById_Success() {
        // Given
        when(backOfficeRestClient.getBookById(1)).thenReturn(testBook);
        
        // When
        Response response = bookResource.getBookById(1);
        
        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        BookTO book = (BookTO) response.getEntity();
        assertNotNull(book);
        assertEquals("Java完全理解", book.bookName());
        verify(backOfficeRestClient, times(1)).getBookById(1);
    }
    
    /**
     * Scenario: 存在しない書籍IDを指定した場合
     * Given: BackOfficeRestClientがモック化されている
     * And: モック設定: getBookById(999) が null を返す
     * When: BookResource.getBookById(999) を呼び出す
     * Then: HTTPステータス 404 Not Found が返される
     */
    @Test
    void testGetBookById_NotFound() {
        // Given
        when(backOfficeRestClient.getBookById(999)).thenReturn(null);
        
        // When
        Response response = bookResource.getBookById(999);
        
        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(backOfficeRestClient, times(1)).getBookById(999);
    }
    
    /**
     * Scenario: カテゴリIDで書籍を検索する
     * Given: BackOfficeRestClientがモック化されている
     * And: モック設定: searchBooksJpql(1, null) が BookTO のリストを返す
     * When: BookResource.searchBooksJpql(1, null) を呼び出す
     * Then: HTTPステータス 200 OK が返される
     * And: レスポンスボディに BookTO のリスト（JSON）が含まれる
     * And: BackOfficeRestClient.searchBooksJpql(1, null) が1回呼ばれる
     */
    @Test
    void testSearchBooksJpql_ByCategoryId() {
        // Given
        when(backOfficeRestClient.searchBooksJpql(1, null)).thenReturn(List.of(testBook));
        
        // When
        Response response = bookResource.searchBooksJpql(1, null);
        
        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<BookTO> books = (List<BookTO>) response.getEntity();
        assertNotNull(books);
        assertEquals(1, books.size());
        assertEquals("Java完全理解", books.get(0).bookName());
        verify(backOfficeRestClient, times(1)).searchBooksJpql(1, null);
    }
    
    /**
     * Scenario: キーワードで書籍を検索する
     * Given: BackOfficeRestClientがモック化されている
     * And: モック設定: searchBooksJpql(null, "Java") が BookTO のリストを返す
     * When: BookResource.searchBooksJpql(null, "Java") を呼び出す
     * Then: HTTPステータス 200 OK が返される
     * And: レスポンスボディに BookTO のリスト（JSON）が含まれる
     * And: BackOfficeRestClient.searchBooksJpql(null, "Java") が1回呼ばれる
     */
    @Test
    void testSearchBooksJpql_ByKeyword() {
        // Given
        when(backOfficeRestClient.searchBooksJpql(null, "Java")).thenReturn(testBooks);
        
        // When
        Response response = bookResource.searchBooksJpql(null, "Java");
        
        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<BookTO> books = (List<BookTO>) response.getEntity();
        assertNotNull(books);
        assertEquals(2, books.size());
        verify(backOfficeRestClient, times(1)).searchBooksJpql(null, "Java");
    }
    
    /**
     * Scenario: カテゴリIDとキーワードで書籍を検索する（Criteria API）
     * Given: BackOfficeRestClientがモック化されている
     * And: モック設定: searchBooksCriteria(1, "Java") が BookTO のリストを返す
     * When: BookResource.searchBooksCriteria(1, "Java") を呼び出す
     * Then: HTTPステータス 200 OK が返される
     * And: レスポンスボディに BookTO のリスト（JSON）が含まれる
     * And: BackOfficeRestClient.searchBooksCriteria(1, "Java") が1回呼ばれる
     */
    @Test
    void testSearchBooksCriteria_Success() {
        // Given
        when(backOfficeRestClient.searchBooksCriteria(1, "Java")).thenReturn(List.of(testBook));
        
        // When
        Response response = bookResource.searchBooksCriteria(1, "Java");
        
        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<BookTO> books = (List<BookTO>) response.getEntity();
        assertNotNull(books);
        assertEquals(1, books.size());
        assertEquals("Java完全理解", books.get(0).bookName());
        verify(backOfficeRestClient, times(1)).searchBooksCriteria(1, "Java");
    }
    
    /**
     * Scenario: カテゴリ一覧をマップ形式で取得する
     * Given: BackOfficeRestClientがモック化されている
     * And: モック設定: getAllCategories() がカテゴリマップを返す
     * When: BookResource.getAllCategories() を呼び出す
     * Then: HTTPステータス 200 OK が返される
     * And: レスポンスボディにカテゴリマップ（JSON）が含まれる
     * And: BackOfficeRestClient.getAllCategories() が1回呼ばれる
     */
    @Test
    void testGetAllCategories_Success() {
        // Given
        when(backOfficeRestClient.getAllCategories()).thenReturn(testCategories);
        
        // When
        Response response = bookResource.getAllCategories();
        
        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Integer> categories = (Map<String, Integer>) response.getEntity();
        assertNotNull(categories);
        assertEquals(3, categories.size());
        assertTrue(categories.containsKey("技術"));
        verify(backOfficeRestClient, times(1)).getAllCategories();
    }
    
    /**
     * Scenario: 外部API呼び出しでネットワークエラーが発生した場合
     * Given: BackOfficeRestClientがモック化されている
     * And: モック設定: getAllBooks() が ProcessingException をスローする
     * When: BookResource.getAllBooks() を呼び出す
     * Then: HTTPステータス 500 Internal Server Error が返される
     */
    @Test
    void testGetAllBooks_NetworkError() {
        // Given
        when(backOfficeRestClient.getAllBooks()).thenThrow(new ProcessingException("Network error"));
        
        // When
        Response response = bookResource.getAllBooks();
        
        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        verify(backOfficeRestClient, times(1)).getAllBooks();
    }
    
    /**
     * Scenario: 外部API呼び出しでタイムアウトが発生した場合
     * Given: BackOfficeRestClientがモック化されている
     * And: モック設定: getBookById(1) が ProcessingException（タイムアウト）をスローする
     * When: BookResource.getBookById(1) を呼び出す
     * Then: HTTPステータス 500 Internal Server Error が返される
     */
    @Test
    void testGetBookById_Timeout() {
        // Given
        when(backOfficeRestClient.getBookById(1)).thenThrow(new ProcessingException("Timeout"));
        
        // When
        Response response = bookResource.getBookById(1);
        
        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        verify(backOfficeRestClient, times(1)).getBookById(1);
    }
    
    /**
     * Scenario: 書籍ID = 0（最小値）で詳細を取得する
     * Given: BackOfficeRestClientがモック化されている
     * And: モック設定: getBookById(0) が BookTO を返す
     * When: BookResource.getBookById(0) を呼び出す
     * Then: HTTPステータス 200 OK が返される
     * And: BackOfficeRestClient.getBookById(0) が1回呼ばれる
     */
    @Test
    void testGetBookById_BoundaryValue_Zero() {
        // Given
        BookTO boundaryBook = new BookTO(0, "Boundary Book", "Author", 1, "Category", 1, "Publisher", 1000, 1, 1L);
        when(backOfficeRestClient.getBookById(0)).thenReturn(boundaryBook);
        
        // When
        Response response = bookResource.getBookById(0);
        
        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(backOfficeRestClient, times(1)).getBookById(0);
    }
    
    /**
     * Scenario: 検索結果が0件の場合
     * Given: BackOfficeRestClientがモック化されている
     * And: モック設定: searchBooksJpql(999, "存在しないキーワード") が空のリストを返す
     * When: BookResource.searchBooksJpql(999, "存在しないキーワード") を呼び出す
     * Then: HTTPステータス 200 OK が返される
     * And: レスポンスボディに空の配列（JSON）が含まれる
     */
    @Test
    void testSearchBooksJpql_EmptyResult() {
        // Given
        when(backOfficeRestClient.searchBooksJpql(999, "存在しないキーワード")).thenReturn(Collections.emptyList());
        
        // When
        Response response = bookResource.searchBooksJpql(999, "存在しないキーワード");
        
        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<BookTO> books = (List<BookTO>) response.getEntity();
        assertNotNull(books);
        assertTrue(books.isEmpty());
        verify(backOfficeRestClient, times(1)).searchBooksJpql(999, "存在しないキーワード");
    }
}
