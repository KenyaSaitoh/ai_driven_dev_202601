package pro.kensait.berrybooks.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil単体テスト")
class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    
    @BeforeEach
    void setUp() throws Exception {
        // Given: JwtUtilの初期化
        jwtUtil = new JwtUtil();
        
        // プライベートフィールドに値を設定（テスト用）
        setPrivateField(jwtUtil, "secretKey", "test-secret-key-for-testing-at-least-256-bits-long-hs256-algorithm");
        setPrivateField(jwtUtil, "expirationMs", 86400000L);
        
        jwtUtil.init();
    }
    
    @Test
    @DisplayName("顧客IDとメールアドレスからJWTを生成（正常系）")
    void testGenerateToken_Success() {
        // Given: 顧客情報
        Integer customerId = 1;
        String email = "test@example.com";
        
        // When: JWT生成
        String token = jwtUtil.generateToken(customerId, email);
        
        // Then: 検証
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // JWTは3つのパート（ヘッダー、ペイロード、署名）で構成
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }
    
    @Test
    @DisplayName("有効なJWTトークンを検証（正常系）")
    void testValidateToken_ValidToken() {
        // Given: 有効なJWTトークン
        String token = jwtUtil.generateToken(1, "test@example.com");
        
        // When: JWT検証
        boolean isValid = jwtUtil.validateToken(token);
        
        // Then: 検証
        assertTrue(isValid);
    }
    
    @Test
    @DisplayName("不正な署名のJWTトークンを検証（異常系）")
    void testValidateToken_InvalidSignature() {
        // Given: 不正なJWTトークン（署名部分を改変）
        String token = jwtUtil.generateToken(1, "test@example.com");
        String invalidToken = token.substring(0, token.lastIndexOf('.')) + ".invalidsignature";
        
        // When: JWT検証
        boolean isValid = jwtUtil.validateToken(invalidToken);
        
        // Then: 検証
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("空文字列のトークンを検証（異常系）")
    void testValidateToken_EmptyToken() {
        // Given: 空文字列
        String emptyToken = "";
        
        // When: JWT検証
        boolean isValid = jwtUtil.validateToken(emptyToken);
        
        // Then: 検証
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("JWTトークンから顧客IDを抽出（正常系）")
    void testGetCustomerIdFromToken_Success() {
        // Given: 有効なJWTトークン
        Integer expectedCustomerId = 1;
        String token = jwtUtil.generateToken(expectedCustomerId, "test@example.com");
        
        // When: 顧客ID抽出
        Integer customerId = jwtUtil.getCustomerIdFromToken(token);
        
        // Then: 検証
        assertNotNull(customerId);
        assertEquals(expectedCustomerId, customerId);
    }
    
    @Test
    @DisplayName("JWTトークンからメールアドレスを抽出（正常系）")
    void testGetEmailFromToken_Success() {
        // Given: 有効なJWTトークン
        String expectedEmail = "test@example.com";
        String token = jwtUtil.generateToken(1, expectedEmail);
        
        // When: メールアドレス抽出
        String email = jwtUtil.getEmailFromToken(token);
        
        // Then: 検証
        assertNotNull(email);
        assertEquals(expectedEmail, email);
    }
    
    @Test
    @DisplayName("異なる顧客情報でJWTを生成（境界値テスト）")
    void testGenerateToken_DifferentCustomers() {
        // Given: 異なる顧客情報
        String token1 = jwtUtil.generateToken(1, "user1@example.com");
        String token2 = jwtUtil.generateToken(2, "user2@example.com");
        
        // When: トークンから情報を抽出
        Integer customerId1 = jwtUtil.getCustomerIdFromToken(token1);
        String email1 = jwtUtil.getEmailFromToken(token1);
        Integer customerId2 = jwtUtil.getCustomerIdFromToken(token2);
        String email2 = jwtUtil.getEmailFromToken(token2);
        
        // Then: 検証
        assertEquals(1, customerId1);
        assertEquals("user1@example.com", email1);
        assertEquals(2, customerId2);
        assertEquals("user2@example.com", email2);
        assertNotEquals(token1, token2);
    }
    
    // ユーティリティメソッド: プライベートフィールドに値を設定
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
