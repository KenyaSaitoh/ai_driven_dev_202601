package pro.kensait.berrybooks.common.exception;

/**
 * 在庫不足例外
 * 
 * 注文時に在庫が不足している場合にスローされる。
 * 
 * @since 1.0.0
 */
public class OutOfStockException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * コンストラクタ
     * 
     * @param message エラーメッセージ
     */
    public OutOfStockException(String message) {
        super(message);
    }
    
    /**
     * コンストラクタ
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public OutOfStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
