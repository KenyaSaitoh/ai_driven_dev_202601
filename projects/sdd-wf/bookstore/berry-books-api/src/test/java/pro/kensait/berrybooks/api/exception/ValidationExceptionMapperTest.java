package pro.kensait.berrybooks.api.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidationExceptionMapper単体テスト")
class ValidationExceptionMapperTest {
    
    @Mock
    private UriInfo uriInfo;
    
    @InjectMocks
    private ValidationExceptionMapper mapper;
    
    @BeforeEach
    void setUp() {
        // UriInfoのモック設定（リフレクションで注入）
        try {
            java.lang.reflect.Field field = ValidationExceptionMapper.class.getDeclaredField("uriInfo");
            field.setAccessible(true);
            field.set(mapper, uriInfo);
        } catch (Exception e) {
            fail("Failed to inject UriInfo: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("ConstraintViolationExceptionを400 Bad Requestにマッピング")
    void testToResponse_ConstraintViolationException() {
        // Given: ConstraintViolationException
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        
        // モックのConstraintViolationを作成
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        when(violation1.getMessage()).thenReturn("メールアドレスは必須です");
        violations.add(violation1);
        
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        when(violation2.getMessage()).thenReturn("パスワードは必須です");
        violations.add(violation2);
        
        ConstraintViolationException exception = new ConstraintViolationException(violations);
        when(uriInfo.getPath()).thenReturn("/api/auth/login");
        
        // When: Exception Mapperを呼び出し
        Response response = mapper.toResponse(exception);
        
        // Then: 検証
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.status());
        assertEquals("Bad Request", errorResponse.error());
        assertTrue(errorResponse.message().contains("メールアドレスは必須です") || 
                   errorResponse.message().contains("パスワードは必須です"));
        assertEquals("/api/auth/login", errorResponse.path());
    }
}
