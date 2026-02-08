package pro.kensait.berrybooks.util;

import jakarta.enterprise.context.ApplicationScoped;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * パスワードハッシュ化・検証ユーティリティ
 */
@ApplicationScoped
public class PasswordUtil {
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);
    private static final int BCRYPT_COST = 10;
    
    /**
     * パスワードをBCryptでハッシュ化する
     * 
     * @param plainPassword 平文パスワード
     * @return ハッシュ化されたパスワード
     */
    public String hashPassword(String plainPassword) {
        logger.debug("[ PasswordUtil#hashPassword ] Hashing password");
        String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_COST));
        logger.debug("[ PasswordUtil#hashPassword ] Password hashed successfully");
        return hashed;
    }
    
    /**
     * パスワードを検証する
     * 
     * @param plainPassword 平文パスワード
     * @param hashedPassword ハッシュ化されたパスワード
     * @return 一致する場合true
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        logger.debug("[ PasswordUtil#verifyPassword ] Verifying password");
        boolean result = BCrypt.checkpw(plainPassword, hashedPassword);
        logger.debug("[ PasswordUtil#verifyPassword ] Verification result: {}", result);
        return result;
    }
}