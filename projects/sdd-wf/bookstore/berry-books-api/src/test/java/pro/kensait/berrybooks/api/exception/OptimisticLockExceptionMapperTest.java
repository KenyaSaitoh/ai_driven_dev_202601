package pro.kensait.berrybooks.api.exception;

import jakarta.persistence.OptimisticLockException;
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
@DisplayName("OptimisticLockExceptionMapper単体テスト")
class OptimisticLockExceptionMapperTest {
    
    @Mock
    private UriInfo uriInfo;
    
    @InjectMocks
    private OptimisticLockExceptionMapper mapper;
    
    @BeforeEach
    void setUp() {
        // UriInfoのモック設定（リフレクションで注入）
        try {
            java.lang.reflect.Field field = OptimisticLockExceptionMapper.class.getDeclaredField("uriInfo");
            field.setAccessible(true);
            field.set(mapper, uriInfo);
        } catch (Exception e) {
            fail("Failed to inject UriInfo: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("OptimisticLockExceptionを409 Conflictにマッピング")
    void testToResponse_OptimisticLockException() {
        // Given: OptimisticLockException
        OptimisticLockException exception = new OptimisticLockException("Version conflict");
        when(uriInfo.getPath()).thenReturn("/api/orders");
        
        // When: Exception Mapperを呼び出し
        Response response = mapper.toResponse(exception);
        
        // Then: 検証
        assertNotNull(response);
        assertEquals(409, response.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertEquals(409, errorResponse.status());
        assertEquals("Conflict", errorResponse.error());
        assertEquals("データが他のユーザーによって更新されました。再度お試しください。", errorResponse.message());
        assertEquals("/api/orders", errorResponse.path());
    }
}
