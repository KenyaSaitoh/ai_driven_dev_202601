package pro.kensait.berrybooks.external;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import pro.kensait.berrybooks.external.dto.BookTO;
import pro.kensait.berrybooks.external.dto.StockTO;

/**
 * BackOfficeRestClient 単体テスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BackOfficeRestClient 単体テスト")
class BackOfficeRestClientTest {

    @Mock
    private BackOfficeApi backOfficeApi;

    @InjectMocks
    private BackOfficeRestClient backOfficeClient;

    @BeforeEach
    void setUp() {
        // モックの初期化は@ExtendWith(MockitoExtension.class)で自動実行
    }

    @Test
    @DisplayName("書籍一覧を取得")
    void testFindAllBooks() {
        // Given: モック設定
        BookTO book1 = new BookTO();
        book1.setBookId(1);
        book1.setBookName("Java入門");
        book1.setPrice(3000);

        BookTO book2 = new BookTO();
        book2.setBookId(2);
        book2.setBookName("Spring Boot実践");
        book2.setPrice(3500);

        List<BookTO> mockBooks = Arrays.asList(book1, book2);
        when(backOfficeApi.findAllBooks()).thenReturn(mockBooks);

        // When
        List<BookTO> result = backOfficeClient.findAllBooks();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Java入門", result.get(0).getBookName());
        assertEquals("Spring Boot実践", result.get(1).getBookName());
        verify(backOfficeApi, times(1)).findAllBooks();
    }

    @Test
    @DisplayName("書籍IDで在庫を取得")
    void testFindStockById() {
        // Given: モック設定
        StockTO stock = new StockTO();
        stock.setBookId(1);
        stock.setQuantity(10);
        stock.setVersion(1L);

        when(backOfficeApi.findStockById(1)).thenReturn(stock);

        // When
        StockTO result = backOfficeClient.findStockById(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getBookId());
        assertEquals(10, result.getQuantity());
        assertEquals(1L, result.getVersion());
        verify(backOfficeApi, times(1)).findStockById(1);
    }

    @Test
    @DisplayName("在庫更新（楽観的ロック成功）")
    void testUpdateStock_Success() {
        // Given: モック設定
        StockTO updatedStock = new StockTO();
        updatedStock.setBookId(1);
        updatedStock.setQuantity(95);
        updatedStock.setVersion(6L); // バージョンがインクリメントされる

        when(backOfficeApi.updateStock(eq(1), eq(5L), eq(95))).thenReturn(updatedStock);

        // When
        StockTO result = backOfficeClient.updateStock(1, 5L, 95);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getBookId());
        assertEquals(95, result.getQuantity());
        assertEquals(6L, result.getVersion()); // バージョンが6にインクリメント
        verify(backOfficeApi, times(1)).updateStock(1, 5L, 95);
    }

    @Test
    @DisplayName("在庫更新（楽観的ロック失敗）")
    void testUpdateStock_OptimisticLockFailure() {
        // Given: 楽観的ロック失敗のモック設定
        Response mockResponse = Response.status(Response.Status.CONFLICT)
            .entity("{\"error\":\"Optimistic lock exception\"}")
            .build();

        when(backOfficeApi.updateStock(eq(1), eq(3L), eq(95)))
            .thenThrow(new WebApplicationException(mockResponse));

        // When & Then: WebApplicationExceptionがスローされる
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            backOfficeClient.updateStock(1, 3L, 95);
        });

        assertEquals(Response.Status.CONFLICT.getStatusCode(), exception.getResponse().getStatus());
        verify(backOfficeApi, times(1)).updateStock(1, 3L, 95);
    }

    @Test
    @DisplayName("書籍IDで書籍を取得")
    void testFindBookById() {
        // Given: モック設定
        BookTO book = new BookTO();
        book.setBookId(1);
        book.setBookName("Java入門");
        book.setPrice(3000);

        when(backOfficeApi.findBookById(1)).thenReturn(book);

        // When
        BookTO result = backOfficeClient.findBookById(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getBookId());
        assertEquals("Java入門", result.getBookName());
        assertEquals(3000, result.getPrice());
        verify(backOfficeApi, times(1)).findBookById(1);
    }

    @Test
    @DisplayName("キーワードで書籍を検索（JPQL）")
    void testSearchBooksJpql() {
        // Given: モック設定
        BookTO book1 = new BookTO();
        book1.setBookId(1);
        book1.setBookName("Java入門");

        BookTO book2 = new BookTO();
        book2.setBookId(2);
        book2.setBookName("Java実践");

        List<BookTO> mockBooks = Arrays.asList(book1, book2);
        when(backOfficeApi.searchBooksJpql(null, "Java")).thenReturn(mockBooks);

        // When
        List<BookTO> result = backOfficeClient.searchBooksJpql(null, "Java");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getBookName().contains("Java"));
        assertTrue(result.get(1).getBookName().contains("Java"));
        verify(backOfficeApi, times(1)).searchBooksJpql(null, "Java");
    }
}
