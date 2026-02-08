package pro.kensait.berrybooks.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PasswordUtilの単体テスト
 */
class PasswordUtilTest {
    
    private PasswordUtil passwordUtil;
    
    @BeforeEach
    void setUp() {
        passwordUtil = new PasswordUtil();
    }
    
    @Test
    void testHashPassword_Success() {
        // Given: 平文パスワード
        String plainPassword = "password123";
        
        // When: hashPassword()を呼び出す
        String hashedPassword = passwordUtil.hashPassword(plainPassword);
        
        // Then: BCryptハッシュ文字列が返される
        assertNotNull(hashedPassword);
        assertEquals(60, hashedPassword.length()); // BCryptハッシュは60文字
        assertTrue(hashedPassword.startsWith("$2a$")); // BCryptの標準的な開始文字列
    }
    
    @Test
    void testHashPassword_DifferentHashesForSamePassword() {
        // Given: 同じ平文パスワード
        String plainPassword = "password123";
        
        // When: 2回ハッシュ化する
        String hash1 = passwordUtil.hashPassword(plainPassword);
        String hash2 = passwordUtil.hashPassword(plainPassword);
        
        // Then: 異なるハッシュが生成される（ソルト付きハッシュの特性）
        assertNotEquals(hash1, hash2);
    }
    
    @Test
    void testVerifyPassword_Success() {
        // Given: 平文パスワードとそのハッシュ
        String plainPassword = "password123";
        String hashedPassword = passwordUtil.hashPassword(plainPassword);
        
        // When: verifyPassword()を呼び出す
        boolean result = passwordUtil.verifyPassword(plainPassword, hashedPassword);
        
        // Then: trueが返される
        assertTrue(result);
    }
    
    @Test
    void testVerifyPassword_WrongPassword() {
        // Given: 平文パスワードとそのハッシュ
        String plainPassword = "password123";
        String hashedPassword = passwordUtil.hashPassword(plainPassword);
        String wrongPassword = "wrongpassword";
        
        // When: 誤ったパスワードでverifyPassword()を呼び出す
        boolean result = passwordUtil.verifyPassword(wrongPassword, hashedPassword);
        
        // Then: falseが返される
        assertFalse(result);
    }
    
    @Test
    void testVerifyPassword_EmptyPassword() {
        // Given: 空のパスワード
        String plainPassword = "";
        String hashedPassword = passwordUtil.hashPassword(plainPassword);
        
        // When: verifyPassword()を呼び出す
        boolean result = passwordUtil.verifyPassword(plainPassword, hashedPassword);
        
        // Then: trueが返される（空パスワードも正しくハッシュ化・検証される）
        assertTrue(result);
    }
    
    @Test
    void testHashPassword_Consistency() {
        // Given: 平文パスワード
        String plainPassword = "password123";
        
        // When: ハッシュ化して検証する
        String hashedPassword = passwordUtil.hashPassword(plainPassword);
        boolean isValid = passwordUtil.verifyPassword(plainPassword, hashedPassword);
        
        // Then: 一貫性が保たれる
        assertTrue(isValid);
    }
}