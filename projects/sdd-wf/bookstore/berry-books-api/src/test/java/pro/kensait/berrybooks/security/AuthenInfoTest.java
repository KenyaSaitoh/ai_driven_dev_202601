package pro.kensait.berrybooks.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthenInfo単体テスト")
class AuthenInfoTest {
    
    private AuthenInfo authenInfo;
    
    @BeforeEach
    void setUp() {
        authenInfo = new AuthenInfo();
    }
    
    @Test
    @DisplayName("認証情報の設定と取得（正常系）")
    void testSetAndGet_Success() {
        // Given/When: 認証情報を設定
        authenInfo.setCustomerId(1);
        authenInfo.setEmail("test@example.com");
        
        // Then: 検証
        assertEquals(1, authenInfo.getCustomerId());
        assertEquals("test@example.com", authenInfo.getEmail());
    }
    
    @Test
    @DisplayName("認証済み判定（正常系：認証済み）")
    void testIsAuthenticated_True() {
        // Given: 認証情報を設定
        authenInfo.setCustomerId(1);
        authenInfo.setEmail("test@example.com");
        
        // When/Then: 認証済みであることを検証
        assertTrue(authenInfo.isAuthenticated());
    }
    
    @Test
    @DisplayName("認証済み判定（境界値：顧客IDなし）")
    void testIsAuthenticated_False_NoCustomerId() {
        // Given: メールアドレスのみ設定
        authenInfo.setEmail("test@example.com");
        
        // When/Then: 認証されていないことを検証
        assertFalse(authenInfo.isAuthenticated());
    }
    
    @Test
    @DisplayName("認証済み判定（境界値：メールアドレスなし）")
    void testIsAuthenticated_False_NoEmail() {
        // Given: 顧客IDのみ設定
        authenInfo.setCustomerId(1);
        
        // When/Then: 認証されていないことを検証
        assertFalse(authenInfo.isAuthenticated());
    }
    
    @Test
    @DisplayName("認証済み判定（境界値：両方なし）")
    void testIsAuthenticated_False_NoInfo() {
        // Given: 何も設定しない
        
        // When/Then: 認証されていないことを検証
        assertFalse(authenInfo.isAuthenticated());
    }
}
