package pro.kensait.berrybooks.common.exception;

/**
 * 認証失敗例外
 * 
 * ログイン時に認証情報が不正な場合にスローされる。
 * 
 * @since 1.0.0
 */
public class AuthenticationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * コンストラクタ
     * 
     * @param message エラーメッセージ
     */
    public AuthenticationException(String message) {
        super(message);
    }
    
    /**
     * コンストラクタ
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
