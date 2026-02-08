package pro.kensait.berrybooks.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * AuthenticatedUser 単体テスト
 */
@DisplayName("AuthenticatedUser 単体テスト")
class AuthenticatedUserTest {

    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        authenticatedUser = new AuthenticatedUser();
    }

    @Test
    @DisplayName("認証情報の設定と取得")
    void testSetAndGetAuthenticationInfo() {
        // When: 認証情報を設定
        authenticatedUser.setCustomerId(1);
        authenticatedUser.setCustomerName("山田太郎");

        // Then: 認証情報が正しく取得できる
        assertEquals(1, authenticatedUser.getCustomerId());
        assertEquals("山田太郎", authenticatedUser.getCustomerName());
    }

    @Test
    @DisplayName("isAuthenticated: 認証済みの場合")
    void testIsAuthenticated_Authenticated() {
        // Given: 認証情報を設定
        authenticatedUser.setCustomerId(1);
        authenticatedUser.setCustomerName("山田太郎");

        // When & Then: 認証済みと判定される
        assertTrue(authenticatedUser.isAuthenticated());
    }

    @Test
    @DisplayName("isAuthenticated: 未認証の場合（customerIdがnull）")
    void testIsAuthenticated_NotAuthenticated_NullCustomerId() {
        // Given: customerNameのみ設定
        authenticatedUser.setCustomerName("山田太郎");

        // When & Then: 未認証と判定される
        assertFalse(authenticatedUser.isAuthenticated());
    }

    @Test
    @DisplayName("isAuthenticated: 未認証の場合（customerNameがnull）")
    void testIsAuthenticated_NotAuthenticated_NullCustomerName() {
        // Given: customerIdのみ設定
        authenticatedUser.setCustomerId(1);

        // When & Then: 未認証と判定される
        assertFalse(authenticatedUser.isAuthenticated());
    }

    @Test
    @DisplayName("isAuthenticated: 未認証の場合（両方null）")
    void testIsAuthenticated_NotAuthenticated_BothNull() {
        // Given: 何も設定しない（初期状態）

        // When & Then: 未認証と判定される
        assertFalse(authenticatedUser.isAuthenticated());
    }

    @Test
    @DisplayName("認証情報のリセット")
    void testResetAuthenticationInfo() {
        // Given: 認証情報を設定
        authenticatedUser.setCustomerId(1);
        authenticatedUser.setCustomerName("山田太郎");
        assertTrue(authenticatedUser.isAuthenticated());

        // When: 認証情報をリセット
        authenticatedUser.setCustomerId(null);
        authenticatedUser.setCustomerName(null);

        // Then: 未認証と判定される
        assertFalse(authenticatedUser.isAuthenticated());
        assertNull(authenticatedUser.getCustomerId());
        assertNull(authenticatedUser.getCustomerName());
    }

    @Test
    @DisplayName("SerializableインターフェースをImplementsしている")
    void testSerializable() {
        // Then: Serializableインターフェースを実装している
        assertTrue(authenticatedUser instanceof java.io.Serializable);
    }
}
