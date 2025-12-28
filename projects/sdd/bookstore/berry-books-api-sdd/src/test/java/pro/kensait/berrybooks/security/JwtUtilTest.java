package pro.kensait.berrybooks.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtilのユニットテスト
 * 
 * JWT生成・検証ロジックのテスト
 */
@DisplayName("JwtUtil Test")
class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        
        // JwtUtilのフィールドを手動で設定（テスト用）
        try {
            var secretKeyField = JwtUtil.class.getDeclaredField("secretKey");
            secretKeyField.setAccessible(true);
            secretKeyField.set(jwtUtil, "BerryBooksSecretKeyForJWT2024MustBe32CharactersOrMore");
            
            var expirationMsField = JwtUtil.class.getDeclaredField("expirationMs");
            expirationMsField.setAccessible(true);
            expirationMsField.set(jwtUtil, 86400000L); // 24時間
            
            jwtUtil.init();
        } catch (Exception e) {
            fail("Failed to initialize JwtUtil: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("JWTトークンを生成できる")
    void testGenerateToken() {
        // Given
        Integer customerId = 1;
        String email = "alice@gmail.com";
        
        // When
        String token = jwtUtil.generateToken(customerId, email);
        
        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    @DisplayName("有効なJWTトークンを検証できる")
    void testValidateToken_ValidToken() {
        // Given
        Integer customerId = 1;
        String email = "alice@gmail.com";
        String token = jwtUtil.generateToken(customerId, email);
        
        // When
        boolean isValid = jwtUtil.validateToken(token);
        
        // Then
        assertTrue(isValid);
    }
    
    @Test
    @DisplayName("無効なJWTトークンは検証に失敗する")
    void testValidateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";
        
        // When
        boolean isValid = jwtUtil.validateToken(invalidToken);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("JWTトークンから顧客IDを取得できる")
    void testGetCustomerIdFromToken() {
        // Given
        Integer customerId = 123;
        String email = "test@example.com";
        String token = jwtUtil.generateToken(customerId, email);
        
        // When
        Integer extractedCustomerId = jwtUtil.getCustomerIdFromToken(token);
        
        // Then
        assertEquals(customerId, extractedCustomerId);
    }
    
    @Test
    @DisplayName("JWTトークンからメールアドレスを取得できる")
    void testGetEmailFromToken() {
        // Given
        Integer customerId = 123;
        String email = "test@example.com";
        String token = jwtUtil.generateToken(customerId, email);
        
        // When
        String extractedEmail = jwtUtil.getEmailFromToken(token);
        
        // Then
        assertEquals(email, extractedEmail);
    }
}

