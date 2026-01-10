package pro.kensait.backoffice.api;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.backoffice.api.dto.StockTO;
import pro.kensait.backoffice.api.dto.StockUpdateRequest;
import pro.kensait.backoffice.dao.StockDao;
import pro.kensait.backoffice.entity.Book;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * StockResourceの単体テスト
 */
@ExtendWith(MockitoExtension.class)
class StockResourceTest {

    @Mock
    private StockDao stockDao;

    @InjectMocks
    private StockResource stockResource;

    private Book testBook1;
    private Book testBook2;
    private Book testBook3;

    @BeforeEach
    void setUp() {
        // テスト用書籍（在庫情報を含む）
        testBook1 = new Book();
        testBook1.setBookId(1);
        testBook1.setBookName("Javaプログラミング入門");
        testBook1.setAuthor("山田太郎");
        testBook1.setPrice(new BigDecimal("3000"));
        testBook1.setQuantity(10);
        testBook1.setVersion(1L);
        testBook1.setDeleted(false);

        testBook2 = new Book();
        testBook2.setBookId(2);
        testBook2.setBookName("Spring Boot実践入門");
        testBook2.setAuthor("鈴木花子");
        testBook2.setPrice(new BigDecimal("3500"));
        testBook2.setQuantity(5);
        testBook2.setVersion(2L);
        testBook2.setDeleted(false);

        testBook3 = new Book();
        testBook3.setBookId(3);
        testBook3.setBookName("データベース設計");
        testBook3.setAuthor("佐藤次郎");
        testBook3.setPrice(new BigDecimal("4000"));
        testBook3.setQuantity(0);
        testBook3.setVersion(3L);
        testBook3.setDeleted(false);
    }

    /**
     * テスト: 全在庫取得（正常系）
     */
    @Test
    void testGetAllStocks() {
        // Given
        List<Book> mockBooks = Arrays.asList(testBook1, testBook2, testBook3);
        when(stockDao.findAll()).thenReturn(mockBooks);

        // When
        Response response = stockResource.getAllStocks();

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        @SuppressWarnings("unchecked")
        List<StockTO> stocks = (List<StockTO>) response.getEntity();
        assertNotNull(stocks);
        assertEquals(3, stocks.size());
        
        // 1つ目の在庫を確認
        StockTO stock1 = stocks.get(0);
        assertEquals(1, stock1.bookId());
        assertEquals("Javaプログラミング入門", stock1.bookName());
        assertEquals(10, stock1.quantity());
        assertEquals(1L, stock1.version());
        
        // 2つ目の在庫を確認
        StockTO stock2 = stocks.get(1);
        assertEquals(2, stock2.bookId());
        assertEquals(5, stock2.quantity());
        
        // モックの呼び出し確認
        verify(stockDao, times(1)).findAll();
    }

    /**
     * テスト: 全在庫取得（0件）
     */
    @Test
    void testGetAllStocks_Empty() {
        // Given
        when(stockDao.findAll()).thenReturn(Collections.emptyList());

        // When
        Response response = stockResource.getAllStocks();

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        @SuppressWarnings("unchecked")
        List<StockTO> stocks = (List<StockTO>) response.getEntity();
        assertNotNull(stocks);
        assertEquals(0, stocks.size());

        // モックの呼び出し確認
        verify(stockDao, times(1)).findAll();
    }

    /**
     * テスト: 在庫取得（存在する）
     */
    @Test
    void testGetStock_Found() {
        // Given
        when(stockDao.findById(1)).thenReturn(testBook1);

        // When
        Response response = stockResource.getStock(1);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        StockTO stock = (StockTO) response.getEntity();
        assertNotNull(stock);
        assertEquals(1, stock.bookId());
        assertEquals("Javaプログラミング入門", stock.bookName());
        assertEquals(10, stock.quantity());
        assertEquals(1L, stock.version());

        // モックの呼び出し確認
        verify(stockDao, times(1)).findById(1);
    }

    /**
     * テスト: 在庫取得（存在しない）
     */
    @Test
    void testGetStock_NotFound() {
        // Given
        when(stockDao.findById(999)).thenReturn(null);

        // When
        Response response = stockResource.getStock(999);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        // モックの呼び出し確認
        verify(stockDao, times(1)).findById(999);
    }

    /**
     * テスト: 在庫更新（成功）
     */
    @Test
    void testUpdateStock_Success() {
        // Given
        StockUpdateRequest request = new StockUpdateRequest(15, 1L);
        when(stockDao.findById(1)).thenReturn(testBook1);
        
        // 更新後のBookを作成
        Book updatedBook = new Book();
        updatedBook.setBookId(1);
        updatedBook.setBookName("Javaプログラミング入門");
        updatedBook.setQuantity(15);
        updatedBook.setVersion(2L);  // versionがインクリメント
        
        when(stockDao.update(any(Book.class))).thenReturn(updatedBook);

        // When
        Response response = stockResource.updateStock(1, request);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        StockTO stock = (StockTO) response.getEntity();
        assertNotNull(stock);
        assertEquals(1, stock.bookId());
        assertEquals(15, stock.quantity());
        assertEquals(2L, stock.version());  // versionが2にインクリメント

        // モックの呼び出し確認
        verify(stockDao, times(1)).findById(1);
        verify(stockDao, times(1)).update(any(Book.class));
    }

    /**
     * テスト: 在庫更新（楽観的ロック失敗）
     */
    @Test
    void testUpdateStock_OptimisticLockFailure() {
        // Given
        // 古いversionで更新しようとする
        StockUpdateRequest request = new StockUpdateRequest(15, 1L);
        
        // データベースのversionは既に2に更新されている
        Book bookWithNewerVersion = new Book();
        bookWithNewerVersion.setBookId(1);
        bookWithNewerVersion.setBookName("Javaプログラミング入門");
        bookWithNewerVersion.setQuantity(10);
        bookWithNewerVersion.setVersion(2L);  // より新しいversion
        
        when(stockDao.findById(1)).thenReturn(bookWithNewerVersion);

        // When
        Response response = stockResource.updateStock(1, request);

        // Then
        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());

        // モックの呼び出し確認
        verify(stockDao, times(1)).findById(1);
        verify(stockDao, never()).update(any(Book.class));  // updateは呼ばれない
    }

    /**
     * テスト: 在庫更新（在庫が存在しない）
     */
    @Test
    void testUpdateStock_NotFound() {
        // Given
        StockUpdateRequest request = new StockUpdateRequest(15, 1L);
        when(stockDao.findById(999)).thenReturn(null);

        // When
        Response response = stockResource.updateStock(999, request);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        // モックの呼び出し確認
        verify(stockDao, times(1)).findById(999);
        verify(stockDao, never()).update(any(Book.class));
    }

    /**
     * テスト: 在庫数を0に更新
     */
    @Test
    void testUpdateStock_ZeroQuantity() {
        // Given
        StockUpdateRequest request = new StockUpdateRequest(0, 1L);
        when(stockDao.findById(1)).thenReturn(testBook1);
        
        // 更新後のBookを作成
        Book updatedBook = new Book();
        updatedBook.setBookId(1);
        updatedBook.setBookName("Javaプログラミング入門");
        updatedBook.setQuantity(0);
        updatedBook.setVersion(2L);
        
        when(stockDao.update(any(Book.class))).thenReturn(updatedBook);

        // When
        Response response = stockResource.updateStock(1, request);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        StockTO stock = (StockTO) response.getEntity();
        assertNotNull(stock);
        assertEquals(0, stock.quantity());

        // モックの呼び出し確認
        verify(stockDao, times(1)).findById(1);
        verify(stockDao, times(1)).update(any(Book.class));
    }
}
