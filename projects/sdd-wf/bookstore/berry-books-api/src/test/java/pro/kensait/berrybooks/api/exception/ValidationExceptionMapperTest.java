package pro.kensait.berrybooks.api.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.kensait.berrybooks.common.ErrorResponse;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ValidationExceptionMapper の単体テスト
 * 
 * @since 1.0.0
 */
class ValidationExceptionMapperTest {
    
    private ValidationExceptionMapper mapper;
    
    @BeforeEach
    void setUp() {
        mapper = new ValidationExceptionMapper();
    }
    
    /**
     * Scenario: ConstraintViolationExceptionを400 Bad Requestにマッピング
     * 
     * Given: ConstraintViolationExceptionがスローされる
     *        違反: "メールアドレスは必須です"
     * When: toResponse(exception)を呼び出す
     * Then: HTTPステータス400（Bad Request）が返される
     *       ErrorResponseボディが含まれる
     */
    @Test
    void testToResponse_ConstraintViolationException_Returns400() {
        // Given
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("メールアドレスは必須です");
        
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);
        
        ConstraintViolationException exception = new ConstraintViolationException(violations);
        
        // When
        Response response = mapper.toResponse(exception);
        
        // Then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.status());
        assertEquals("Bad Request", errorResponse.error());
        assertTrue(errorResponse.message().contains("メールアドレスは必須です"));
    }
    
    /**
     * Scenario: 複数のバリデーションエラー
     * 
     * Given: ConstraintViolationExceptionがスローされる
     *        違反: "メールアドレスは必須です", "パスワードは必須です"
     * When: toResponse(exception)を呼び出す
     * Then: エラーメッセージが結合される
     */
    @Test
    void testToResponse_MultipleViolations_CombinesMessages() {
        // Given
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation1 = mock(ConstraintViolation.class);
        when(violation1.getMessage()).thenReturn("メールアドレスは必須です");
        
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation2 = mock(ConstraintViolation.class);
        when(violation2.getMessage()).thenReturn("パスワードは必須です");
        
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation1);
        violations.add(violation2);
        
        ConstraintViolationException exception = new ConstraintViolationException(violations);
        
        // When
        Response response = mapper.toResponse(exception);
        
        // Then
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        String message = errorResponse.message();
        assertTrue(message.contains("メールアドレスは必須です") || message.contains("パスワードは必須です"));
    }
}
