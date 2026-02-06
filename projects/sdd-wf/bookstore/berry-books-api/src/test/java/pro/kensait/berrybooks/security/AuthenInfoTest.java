package pro.kensait.berrybooks.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthenInfo の単体テスト
 * 
 * @since 1.0.0
 */
class AuthenInfoTest {
    
    /**
     * Scenario: 認証情報が設定されている場合
     * 
     * Given: AuthenInfoが初期化されている
     *        customerId=1, email="test@example.com"が設定されている
     * When: isAuthenticated()を呼び出す
     * Then: trueが返される
     */
    @Test
    void testIsAuthenticated_WhenBothSet_ReturnsTrue() {
        // Given
        AuthenInfo authenInfo = new AuthenInfo();
        authenInfo.setCustomerId(1);
        authenInfo.setEmail("test@example.com");
        
        // When
        boolean result = authenInfo.isAuthenticated();
        
        // Then
        assertTrue(result, "Should be authenticated when both fields are set");
    }
    
    /**
     * Scenario: 認証情報が設定されていない場合
     * 
     * Given: AuthenInfoが初期化されている
     *        認証情報が設定されていない
     * When: isAuthenticated()を呼び出す
     * Then: falseが返される
     */
    @Test
    void testIsAuthenticated_WhenEmpty_ReturnsFalse() {
        // Given
        AuthenInfo authenInfo = new AuthenInfo();
        
        // When
        boolean result = authenInfo.isAuthenticated();
        
        // Then
        assertFalse(result, "Should not be authenticated when fields are not set");
    }
    
    /**
     * Scenario: 顧客IDのみ設定されている場合
     * 
     * Given: AuthenInfoが初期化されている
     *        customerId=1のみ設定されている
     * When: isAuthenticated()を呼び出す
     * Then: falseが返される
     */
    @Test
    void testIsAuthenticated_WhenOnlyCustomerIdSet_ReturnsFalse() {
        // Given
        AuthenInfo authenInfo = new AuthenInfo();
        authenInfo.setCustomerId(1);
        
        // When
        boolean result = authenInfo.isAuthenticated();
        
        // Then
        assertFalse(result, "Should not be authenticated when only customerId is set");
    }
    
    /**
     * Scenario: メールアドレスのみ設定されている場合
     * 
     * Given: AuthenInfoが初期化されている
     *        email="test@example.com"のみ設定されている
     * When: isAuthenticated()を呼び出す
     * Then: falseが返される
     */
    @Test
    void testIsAuthenticated_WhenOnlyEmailSet_ReturnsFalse() {
        // Given
        AuthenInfo authenInfo = new AuthenInfo();
        authenInfo.setEmail("test@example.com");
        
        // When
        boolean result = authenInfo.isAuthenticated();
        
        // Then
        assertFalse(result, "Should not be authenticated when only email is set");
    }
    
    /**
     * Scenario: toStringメソッドのテスト
     * 
     * Given: AuthenInfoが初期化されている
     *        customerId=1, email="test@example.com"が設定されている
     * When: toString()を呼び出す
     * Then: 文字列表現が返される
     */
    @Test
    void testToString_ReturnsStringRepresentation() {
        // Given
        AuthenInfo authenInfo = new AuthenInfo();
        authenInfo.setCustomerId(1);
        authenInfo.setEmail("test@example.com");
        
        // When
        String result = authenInfo.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("1"), "String should contain customerId");
        assertTrue(result.contains("test@example.com"), "String should contain email");
    }
}
