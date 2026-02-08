package pro.kensait.berrybooks.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtilの単体テスト
 */
class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Simulate @PostConstruct initialization
        jwtUtil.init();
    }
    
    @Test
    void testGenerateToken_Success() {
        // Given: 顧客情報
        Integer customerId = 1;
        String customerName = "山田太郎";
        
        // When: generateToken()を呼び出す
        String token = jwtUtil.generateToken(customerId, customerName);
        
        // Then: JWT文字列が返される
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.startsWith("eyJ")); // JWTの標準的な開始文字列
    }
    
    @Test
    void testValidateToken_Success() {
        // Given: 有効なJWTトークンを生成
        Integer customerId = 1;
        String customerName = "山田太郎";
        String token = jwtUtil.generateToken(customerId, customerName);
        
        // When: validateToken()を呼び出す
        Claims claims = jwtUtil.validateToken(token);
        
        // Then: Claimsが返される
        assertNotNull(claims);
        assertEquals(customerId, claims.get("customerId", Integer.class));
        assertEquals(customerName, claims.get("customerName", String.class));
    }
    
    @Test
    void testExtractCustomerId_Success() {
        // Given: 有効なJWTトークンを生成
        Integer customerId = 1;
        String customerName = "山田太郎";
        String token = jwtUtil.generateToken(customerId, customerName);
        
        // When: extractCustomerId()を呼び出す
        Integer extractedCustomerId = jwtUtil.extractCustomerId(token);
        
        // Then: customerIdが抽出される
        assertEquals(customerId, extractedCustomerId);
    }
    
    @Test
    void testExtractCustomerName_Success() {
        // Given: 有効なJWTトークンを生成
        Integer customerId = 1;
        String customerName = "山田太郎";
        String token = jwtUtil.generateToken(customerId, customerName);
        
        // When: extractCustomerName()を呼び出す
        String extractedCustomerName = jwtUtil.extractCustomerName(token);
        
        // Then: customerNameが抽出される
        assertEquals(customerName, extractedCustomerName);
    }
    
    @Test
    void testValidateToken_InvalidSignature() {
        // Given: 不正な署名のJWTトークン
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIn0.invalid";
        
        // When & Then: validateToken()はJwtExceptionをスロー
        assertThrows(JwtException.class, () -> {
            jwtUtil.validateToken(invalidToken);
        });
    }
    
    @Test
    void testValidateToken_MalformedToken() {
        // Given: 不正な形式のトークン
        String malformedToken = "not.a.jwt.token";
        
        // When & Then: validateToken()はJwtExceptionをスロー
        assertThrows(JwtException.class, () -> {
            jwtUtil.validateToken(malformedToken);
        });
    }
}