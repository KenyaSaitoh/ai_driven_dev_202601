package pro.kensait.berrybooks.api.exception;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.kensait.berrybooks.common.ErrorResponse;
import pro.kensait.berrybooks.common.exception.OutOfStockException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OutOfStockExceptionMapper の単体テスト
 * 
 * @since 1.0.0
 */
class OutOfStockExceptionMapperTest {
    
    private OutOfStockExceptionMapper mapper;
    
    @BeforeEach
    void setUp() {
        mapper = new OutOfStockExceptionMapper();
    }
    
    /**
     * Scenario: OutOfStockExceptionを409 Conflictにマッピング
     * 
     * Given: OutOfStockExceptionがスローされる
     *        message="在庫が不足しています"
     * When: toResponse(exception)を呼び出す
     * Then: HTTPステータス409（Conflict）が返される
     *       ErrorResponseボディが含まれる
     */
    @Test
    void testToResponse_OutOfStockException_Returns409() {
        // Given
        String message = "在庫が不足しています";
        OutOfStockException exception = new OutOfStockException(message);
        
        // When
        Response response = mapper.toResponse(exception);
        
        // Then
        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertEquals(409, errorResponse.status());
        assertEquals("Conflict", errorResponse.error());
        assertEquals(message, errorResponse.message());
    }
    
    /**
     * Scenario: 異なるメッセージでのマッピング
     * 
     * Given: OutOfStockExceptionがスローされる
     *        message="商品ID 123 の在庫がありません"
     * When: toResponse(exception)を呼び出す
     * Then: メッセージが正しくマッピングされる
     */
    @Test
    void testToResponse_CustomMessage_MapsCorrectly() {
        // Given
        String message = "商品ID 123 の在庫がありません";
        OutOfStockException exception = new OutOfStockException(message);
        
        // When
        Response response = mapper.toResponse(exception);
        
        // Then
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertEquals(message, errorResponse.message());
    }
}
