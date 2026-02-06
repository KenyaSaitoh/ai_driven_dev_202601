package pro.kensait.berrybooks.api.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.berrybooks.common.ErrorResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenericExceptionMapper単体テスト")
class GenericExceptionMapperTest {
    
    @Mock
    private UriInfo uriInfo;
    
    @InjectMocks
    private GenericExceptionMapper mapper;
    
    @BeforeEach
    void setUp() {
        // UriInfoのモック設定（リフレクションで注入）
        try {
            java.lang.reflect.Field field = GenericExceptionMapper.class.getDeclaredField("uriInfo");
            field.setAccessible(true);
            field.set(mapper, uriInfo);
        } catch (Exception e) {
            fail("Failed to inject UriInfo: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("予期しない例外を500 Internal Server Errorにマッピング")
    void testToResponse_GenericException() {
        // Given: 予期しない例外
        Exception exception = new RuntimeException("Unexpected error occurred");
        when(uriInfo.getPath()).thenReturn("/api/orders");
        
        // When: Exception Mapperを呼び出し
        Response response = mapper.toResponse(exception);
        
        // Then: 検証
        assertNotNull(response);
        assertEquals(500, response.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertEquals(500, errorResponse.status());
        assertEquals("Internal Server Error", errorResponse.error());
        assertEquals("システムエラーが発生しました。管理者に連絡してください。", errorResponse.message());
        assertEquals("/api/orders", errorResponse.path());
    }
    
    @Test
    @DisplayName("NullPointerExceptionを500 Internal Server Errorにマッピング")
    void testToResponse_NullPointerException() {
        // Given: NullPointerException
        Exception exception = new NullPointerException("Null value encountered");
        when(uriInfo.getPath()).thenReturn("/api/books");
        
        // When: Exception Mapperを呼び出し
        Response response = mapper.toResponse(exception);
        
        // Then: 検証
        assertNotNull(response);
        assertEquals(500, response.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertEquals(500, errorResponse.status());
        assertEquals("Internal Server Error", errorResponse.error());
        assertEquals("システムエラーが発生しました。管理者に連絡してください。", errorResponse.message());
        assertEquals("/api/books", errorResponse.path());
    }
}
