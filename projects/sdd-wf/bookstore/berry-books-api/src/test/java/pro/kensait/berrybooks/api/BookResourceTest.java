package pro.kensait.berrybooks.api;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.berrybooks.external.BackOfficeRestClient;
import pro.kensait.berrybooks.external.dto.BookTO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * BookResourceの単体テスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookResource 単体テスト")
class BookResourceTest {

    @Mock
    private BackOfficeRestClient backOfficeClient;

    @InjectMocks
    private BookResource bookResource;

    private BookTO sampleBook;
    private List<BookTO> sampleBooks;

    @BeforeEach
    void setUp() {
        // Given: テストデータの準備
        sampleBook = new BookTO();
        sampleBook.setBookId(1);
        sampleBook.setBookName("Java完全理解");
        sampleBook.setAuthor("山田太郎");
        sampleBook.setCategoryId(1);
        sampleBook.setPublisherId(1);
        sampleBook.setPublisherName("技術評論社");
        sampleBook.setPrice(3000);
        sampleBook.setQuantity(10);
        sampleBook.setVersion(1L);

        sampleBooks = new ArrayList<>();
        sampleBooks.add(sampleBook);
    }

    // ============================================================
    // 2.1 BookResource - 書籍一覧取得
    // ============================================================

    @Test
    @DisplayName("書籍一覧を正常に取得")
    void testGetAllBooks_Success() {
        // Given: BackOfficeRestClientがモック化されている
        //        モック設定: findAllBooks()が書籍リストを返す
        when(backOfficeClient.findAllBooks()).thenReturn(sampleBooks);

        // When: BookResource.getAllBooks()を呼び出す
        Response response = bookResource.getAllBooks();

        // Then: HTTPステータス200 OKが返される
        //       レスポンスボディにList<BookTO>が含まれる
        //       BackOfficeRestClient.findAllBooks()が1回呼ばれる
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof List);
        @SuppressWarnings("unchecked")
        List<BookTO> resultBooks = (List<BookTO>) response.getEntity();
        assertEquals(1, resultBooks.size());
        assertEquals("Java完全理解", resultBooks.get(0).getBookName());
        verify(backOfficeClient, times(1)).findAllBooks();
    }

    @Test
    @DisplayName("外部API呼び出し失敗時（書籍一覧取得）")
    void testGetAllBooks_ExternalApiFailure() {
        // Given: BackOfficeRestClientがモック化されている
        //        モック設定: findAllBooks()がRuntimeExceptionをスローする
        when(backOfficeClient.findAllBooks()).thenThrow(new RuntimeException("External API Error"));

        // When & Then: BookResource.getAllBooks()を呼び出すとRuntimeExceptionがスローされる
        assertThrows(RuntimeException.class, () -> bookResource.getAllBooks());
    }

    // ============================================================
    // 2.2 BookResource - 書籍詳細取得
    // ============================================================

    @Test
    @DisplayName("書籍詳細を正常に取得")
    void testGetBookById_Success() {
        // Given: BackOfficeRestClientがモック化されている
        //        モック設定: findBookById(1)が書籍情報を返す
        when(backOfficeClient.findBookById(1)).thenReturn(sampleBook);

        // When: BookResource.getBookById(1)を呼び出す
        Response response = bookResource.getBookById(1);

        // Then: HTTPステータス200 OKが返される
        //       レスポンスボディにBookTOが含まれる
        //       bookIdが1である
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof BookTO);
        BookTO resultBook = (BookTO) response.getEntity();
        assertEquals(1, resultBook.getBookId());
        assertEquals("Java完全理解", resultBook.getBookName());
    }

    @Test
    @DisplayName("書籍が存在しない場合（404）")
    void testGetBookById_NotFound() {
        // Given: BackOfficeRestClientがモック化されている
        //        モック設定: findBookById(999)がWebApplicationException（404）をスローする
        when(backOfficeClient.findBookById(999))
            .thenThrow(new WebApplicationException(Response.Status.NOT_FOUND));

        // When & Then: BookResource.getBookById(999)を呼び出すとWebApplicationExceptionがスローされる
        //              HTTPステータス404 Not Foundが返される
        WebApplicationException exception = assertThrows(
            WebApplicationException.class,
            () -> bookResource.getBookById(999)
        );
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), exception.getResponse().getStatus());
    }

    // ============================================================
    // 2.3 BookResource - 書籍検索（JPQL）
    // ============================================================

    @Test
    @DisplayName("カテゴリIDで書籍を検索（JPQL）")
    void testSearchBooksJpql_ByCategoryId() {
        // Given: BackOfficeRestClientがモック化されている
        //        モック設定: searchBooksJpql(1, null)が該当書籍リストを返す
        when(backOfficeClient.searchBooksJpql(1, null)).thenReturn(sampleBooks);

        // When: BookResource.searchBooksJpql(categoryId=1, keyword=null)を呼び出す
        Response response = bookResource.searchBooksJpql(1, null);

        // Then: HTTPステータス200 OKが返される
        //       レスポンスボディに該当するList<BookTO>が含まれる
        //       すべての書籍のcategoryIdが1である
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<BookTO> resultBooks = (List<BookTO>) response.getEntity();
        assertEquals(1, resultBooks.size());
        assertEquals(1, resultBooks.get(0).getCategoryId());
    }

    @Test
    @DisplayName("キーワードで書籍を検索（JPQL）")
    void testSearchBooksJpql_ByKeyword() {
        // Given: BackOfficeRestClientがモック化されている
        //        モック設定: searchBooksJpql(null, "Java")がキーワードに一致する書籍リストを返す
        when(backOfficeClient.searchBooksJpql(null, "Java")).thenReturn(sampleBooks);

        // When: BookResource.searchBooksJpql(categoryId=null, keyword="Java")を呼び出す
        Response response = bookResource.searchBooksJpql(null, "Java");

        // Then: HTTPステータス200 OKが返される
        //       レスポンスボディにList<BookTO>が含まれる
        //       書籍名に"Java"が含まれる
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<BookTO> resultBooks = (List<BookTO>) response.getEntity();
        assertEquals(1, resultBooks.size());
        assertTrue(resultBooks.get(0).getBookName().contains("Java"));
    }

    @Test
    @DisplayName("該当書籍がない場合（JPQL）")
    void testSearchBooksJpql_NoResults() {
        // Given: BackOfficeRestClientがモック化されている
        //        モック設定: searchBooksJpql(null, "存在しないキーワード")が空のリストを返す
        when(backOfficeClient.searchBooksJpql(null, "存在しないキーワード"))
            .thenReturn(new ArrayList<>());

        // When: BookResource.searchBooksJpql(categoryId=null, keyword="存在しないキーワード")を呼び出す
        Response response = bookResource.searchBooksJpql(null, "存在しないキーワード");

        // Then: HTTPステータス200 OKが返される
        //       レスポンスボディに空のリストが含まれる
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<BookTO> resultBooks = (List<BookTO>) response.getEntity();
        assertTrue(resultBooks.isEmpty());
    }

    // ============================================================
    // 2.4 BookResource - 書籍検索（Criteria API）
    // ============================================================

    @Test
    @DisplayName("カテゴリIDとキーワードの両方で検索（Criteria API）")
    void testSearchBooksCriteria_ByCategoryIdAndKeyword() {
        // Given: BackOfficeRestClientがモック化されている
        //        モック設定: searchBooksCriteria(1, "Java")が両条件に一致する書籍リストを返す
        when(backOfficeClient.searchBooksCriteria(1, "Java")).thenReturn(sampleBooks);

        // When: BookResource.searchBooksCriteria(categoryId=1, keyword="Java")を呼び出す
        Response response = bookResource.searchBooksCriteria(1, "Java");

        // Then: HTTPステータス200 OKが返される
        //       レスポンスボディにList<BookTO>が含まれる
        //       すべての書籍のcategoryIdが1である
        //       すべての書籍名に"Java"が含まれる
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<BookTO> resultBooks = (List<BookTO>) response.getEntity();
        assertEquals(1, resultBooks.size());
        assertEquals(1, resultBooks.get(0).getCategoryId());
        assertTrue(resultBooks.get(0).getBookName().contains("Java"));
    }

    @Test
    @DisplayName("パラメータなしで検索（Criteria API）")
    void testSearchBooksCriteria_NoParameters() {
        // Given: BackOfficeRestClientがモック化されている
        //        モック設定: searchBooksCriteria(null, null)が全書籍リストを返す
        when(backOfficeClient.searchBooksCriteria(null, null)).thenReturn(sampleBooks);

        // When: BookResource.searchBooksCriteria(categoryId=null, keyword=null)を呼び出す
        Response response = bookResource.searchBooksCriteria(null, null);

        // Then: HTTPステータス200 OKが返される
        //       レスポンスボディに全書籍のList<BookTO>が含まれる
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<BookTO> resultBooks = (List<BookTO>) response.getEntity();
        assertEquals(1, resultBooks.size());
    }
}
