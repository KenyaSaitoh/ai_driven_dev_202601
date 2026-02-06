package pro.kensait.berrybooks.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil の単体テスト
 * 
 * @since 1.0.0
 */
class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // 手動で初期化
        try {
            var secretField = JwtUtil.class.getDeclaredField("secretKey");
            secretField.setAccessible(true);
            secretField.set(jwtUtil, "your-secret-key-at-least-256-bits-long-for-hs256-algorithm");
            jwtUtil.init();
        } catch (Exception e) {
            fail("Setup failed: " + e.getMessage());
        }
    }
    
    /**
     * Scenario: 顧客IDとメールアドレスからJWTを生成
     * 
     * Given: JwtUtilが初期化されている
     *        秘密鍵が設定されている
     *        customerId=1, email="test@example.com"
     * When: generateToken(1, "test@example.com")を呼び出す
     * Then: JWTトークン文字列が返される
     *       トークンが3つのパート（ヘッダー、ペイロード、署名）で構成されている
     */
    @Test
    void testGenerateToken_Success() {
        // Given
        Integer customerId = 1;
        String email = "test@example.com";
        
        // When
        String token = jwtUtil.generateToken(customerId, email);
        
        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts");
    }
    
    /**
     * Scenario: 有効なJWTトークンを検証
     * 
     * Given: JwtUtilが初期化されている
     *        有効なJWTトークンが存在する
     * When: validateToken(validToken)を呼び出す
     * Then: trueが返される
     */
    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Given
        String token = jwtUtil.generateToken(1, "test@example.com");
        
        // When
        boolean result = jwtUtil.validateToken(token);
        
        // Then
        assertTrue(result, "Valid token should return true");
    }
    
    /**
     * Scenario: 不正な署名のJWTトークンを検証
     * 
     * Given: JwtUtilが初期化されている
     *        不正な署名のJWTトークンが存在する
     * When: validateToken(invalidToken)を呼び出す
     * Then: falseが返される
     *       例外はスローされない
     */
    @Test
    void testValidateToken_InvalidSignature_ReturnsFalse() {
        // Given
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.invalid_signature";
        
        // When
        boolean result = jwtUtil.validateToken(invalidToken);
        
        // Then
        assertFalse(result, "Invalid token should return false");
    }
    
    /**
     * Scenario: JWTトークンから顧客IDを抽出
     * 
     * Given: JwtUtilが初期化されている
     *        有効なJWTトークン（customerId=1含む）が存在する
     * When: getCustomerIdFromToken(token)を呼び出す
     * Then: 1が返される
     */
    @Test
    void testGetCustomerIdFromToken_Success() {
        // Given
        Integer expectedCustomerId = 1;
        String token = jwtUtil.generateToken(expectedCustomerId, "test@example.com");
        
        // When
        Integer customerId = jwtUtil.getCustomerIdFromToken(token);
        
        // Then
        assertEquals(expectedCustomerId, customerId, "Customer ID should be extracted correctly");
    }
    
    /**
     * Scenario: JWTトークンからメールアドレスを抽出
     * 
     * Given: JwtUtilが初期化されている
     *        有効なJWTトークン（email="test@example.com"含む）が存在する
     * When: getEmailFromToken(token)を呼び出す
     * Then: "test@example.com"が返される
     */
    @Test
    void testGetEmailFromToken_Success() {
        // Given
        String expectedEmail = "test@example.com";
        String token = jwtUtil.generateToken(1, expectedEmail);
        
        // When
        String email = jwtUtil.getEmailFromToken(token);
        
        // Then
        assertEquals(expectedEmail, email, "Email should be extracted correctly");
    }
    
    /**
     * Scenario: 空文字列のトークンを検証
     * 
     * Given: JwtUtilが初期化されている
     * When: validateToken("")を呼び出す
     * Then: falseが返される
     */
    @Test
    void testValidateToken_EmptyToken_ReturnsFalse() {
        // Given
        String emptyToken = "";
        
        // When
        boolean result = jwtUtil.validateToken(emptyToken);
        
        // Then
        assertFalse(result, "Empty token should return false");
    }
}
