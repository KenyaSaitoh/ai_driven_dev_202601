package pro.kensait.berrybooks.api.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.kensait.berrybooks.common.ErrorResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OptimisticLockExceptionMapper の単体テスト
 * 
 * @since 1.0.0
 */
class OptimisticLockExceptionMapperTest {
    
    private OptimisticLockExceptionMapper mapper;
    
    @BeforeEach
    void setUp() {
        mapper = new OptimisticLockExceptionMapper();
    }
    
    /**
     * Scenario: OptimisticLockExceptionを409 Conflictにマッピング
     * 
     * Given: OptimisticLockExceptionがスローされる
     * When: toResponse(exception)を呼び出す
     * Then: HTTPステータス409（Conflict）が返される
     *       ErrorResponseボディが含まれる
     *       message: "データが他のユーザーによって更新されました。再度お試しください。"
     */
    @Test
    void testToResponse_OptimisticLockException_Returns409() {
        // Given
        OptimisticLockException exception = new OptimisticLockException("Optimistic lock failure");
        
        // When
        Response response = mapper.toResponse(exception);
        
        // Then
        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertEquals(409, errorResponse.status());
        assertEquals("Conflict", errorResponse.error());
        assertEquals("データが他のユーザーによって更新されました。再度お試しください。", errorResponse.message());
    }
}
