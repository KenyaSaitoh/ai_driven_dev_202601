package pro.kensait.berrybooks.api.exception;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.kensait.berrybooks.common.ErrorResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GenericExceptionMapper の単体テスト
 * 
 * @since 1.0.0
 */
class GenericExceptionMapperTest {
    
    private GenericExceptionMapper mapper;
    
    @BeforeEach
    void setUp() {
        mapper = new GenericExceptionMapper();
    }
    
    /**
     * Scenario: 予期しない例外を500 Internal Server Errorにマッピング
     * 
     * Given: Exceptionがスローされる
     * When: toResponse(exception)を呼び出す
     * Then: HTTPステータス500（Internal Server Error）が返される
     *       ErrorResponseボディが含まれる
     *       message: "システムエラーが発生しました。"
     */
    @Test
    void testToResponse_GenericException_Returns500() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");
        
        // When
        Response response = mapper.toResponse(exception);
        
        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertEquals(500, errorResponse.status());
        assertEquals("Internal Server Error", errorResponse.error());
        assertEquals("システムエラーが発生しました。", errorResponse.message());
    }
    
    /**
     * Scenario: NullPointerExceptionのマッピング
     * 
     * Given: NullPointerExceptionがスローされる
     * When: toResponse(exception)を呼び出す
     * Then: HTTPステータス500が返される
     */
    @Test
    void testToResponse_NullPointerException_Returns500() {
        // Given
        Exception exception = new NullPointerException("Null value encountered");
        
        // When
        Response response = mapper.toResponse(exception);
        
        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertEquals(500, errorResponse.status());
        assertEquals("システムエラーが発生しました。", errorResponse.message());
    }
}
