package pro.kensait.berrybooks.external;

import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.berrybooks.external.dto.BookTO;
import pro.kensait.berrybooks.external.dto.StockTO;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BackOfficeRestClient単体テスト")
class BackOfficeRestClientTest {
    
    @Mock
    private Client client;
    
    @Mock
    private WebTarget baseTarget;
    
    @Mock
    private WebTarget pathTarget;
    
    @Mock
    private WebTarget queryTarget;
    
    @Mock
    private Invocation.Builder builder;
    
    @Mock
    private Response response;
    
    private BackOfficeRestClient restClient;
    
    @BeforeEach
    void setUp() throws Exception {
        restClient = new BackOfficeRestClient();
        
        // プライベートフィールドに値を設定
        setPrivateField(restClient, "baseUrl", "http://localhost:8080/back-office-api/api");
        setPrivateField(restClient, "client", client);
        setPrivateField(restClient, "baseTarget", baseTarget);
    }
    
    @Test
    @DisplayName("全書籍を取得（正常系）")
    void testGetAllBooks_Success() {
        // Given: モック設定
        List<BookTO> expectedBooks = Arrays.asList(
            new BookTO(1, "Java入門", "山田太郎", 1, "技術", 1, "技術評論社", 3000, 10, 1L),
            new BookTO(2, "Spring入門", "佐藤花子", 1, "技術", 2, "翔泳社", 3500, 5, 1L)
        );
        
        when(baseTarget.path("/books")).thenReturn(pathTarget);
        when(pathTarget.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.get()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(any(GenericType.class))).thenReturn(expectedBooks);
        
        // When: メソッド呼び出し
        List<BookTO> result = restClient.getAllBooks();
        
        // Then: 検証
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Java入門", result.get(0).bookName());
        assertEquals("Spring入門", result.get(1).bookName());
        
        verify(baseTarget).path("/books");
        verify(builder).get();
    }
    
    @Test
    @DisplayName("書籍詳細を取得（正常系）")
    void testGetBookById_Success() {
        // Given: モック設定
        BookTO expectedBook = new BookTO(1, "Java入門", "山田太郎", 1, "技術", 1, "技術評論社", 3000, 10, 1L);
        
        when(baseTarget.path("/books/1")).thenReturn(pathTarget);
        when(pathTarget.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.get()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(BookTO.class)).thenReturn(expectedBook);
        
        // When: メソッド呼び出し
        BookTO result = restClient.getBookById(1);
        
        // Then: 検証
        assertNotNull(result);
        assertEquals(1, result.bookId());
        assertEquals("Java入門", result.bookName());
        
        verify(baseTarget).path("/books/1");
        verify(builder).get();
    }
    
    @Test
    @DisplayName("書籍が存在しない（404）")
    void testGetBookById_NotFound() {
        // Given: モック設定（404）
        when(baseTarget.path("/books/999")).thenReturn(pathTarget);
        when(pathTarget.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.get()).thenReturn(response);
        when(response.getStatus()).thenReturn(404);
        
        // When: メソッド呼び出し
        BookTO result = restClient.getBookById(999);
        
        // Then: 検証
        assertNull(result);
        
        verify(baseTarget).path("/books/999");
        verify(builder).get();
    }
    
    @Test
    @DisplayName("在庫情報を取得（正常系）")
    void testFindStockById_Success() {
        // Given: モック設定
        StockTO expectedStock = new StockTO(1, "Java入門", 10, 1L);
        
        when(baseTarget.path("/stocks/1")).thenReturn(pathTarget);
        when(pathTarget.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.get()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(StockTO.class)).thenReturn(expectedStock);
        
        // When: メソッド呼び出し
        StockTO result = restClient.findStockById(1);
        
        // Then: 検証
        assertNotNull(result);
        assertEquals(1, result.bookId());
        assertEquals(10, result.quantity());
        assertEquals(1L, result.version());
        
        verify(baseTarget).path("/stocks/1");
        verify(builder).get();
    }
    
    @Test
    @DisplayName("在庫を更新（正常系：楽観的ロック成功）")
    void testUpdateStock_Success() {
        // Given: モック設定
        StockTO updatedStock = new StockTO(1, "Java入門", 8, 2L);
        
        when(baseTarget.path("/stocks/1")).thenReturn(pathTarget);
        when(pathTarget.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.put(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(StockTO.class)).thenReturn(updatedStock);
        
        // When: メソッド呼び出し
        StockTO result = restClient.updateStock(1, 8, 1L);
        
        // Then: 検証
        assertNotNull(result);
        assertEquals(1, result.bookId());
        assertEquals(8, result.quantity());
        assertEquals(2L, result.version());
        
        verify(baseTarget).path("/stocks/1");
        verify(builder).put(any());
    }
    
    @Test
    @DisplayName("在庫更新時に楽観的ロック失敗（異常系）")
    void testUpdateStock_OptimisticLockConflict() {
        // Given: モック設定（409 Conflict）
        when(baseTarget.path("/stocks/1")).thenReturn(pathTarget);
        when(pathTarget.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.put(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(409);
        
        // When/Then: 例外がスローされることを検証
        assertThrows(OptimisticLockException.class, () -> {
            restClient.updateStock(1, 8, 1L);
        });
        
        verify(baseTarget).path("/stocks/1");
        verify(builder).put(any());
    }
    
    @Test
    @DisplayName("カテゴリ一覧を取得（正常系）")
    void testGetAllCategories_Success() {
        // Given: モック設定
        Map<String, Integer> expectedCategories = new HashMap<>();
        expectedCategories.put("技術書", 1);
        expectedCategories.put("ビジネス書", 2);
        
        when(baseTarget.path("/categories")).thenReturn(pathTarget);
        when(pathTarget.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.get()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(any(GenericType.class))).thenReturn(expectedCategories);
        
        // When: メソッド呼び出し
        Map<String, Integer> result = restClient.getAllCategories();
        
        // Then: 検証
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get("技術書"));
        assertEquals(2, result.get("ビジネス書"));
        
        verify(baseTarget).path("/categories");
        verify(builder).get();
    }
    
    // ユーティリティメソッド: プライベートフィールドに値を設定
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
