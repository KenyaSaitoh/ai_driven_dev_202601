package pro.kensait.berrybooks.common.exception;

/**
 * 在庫不足例外
 */
public class OutOfStockException extends RuntimeException {
    
    public OutOfStockException(String message) {
        super(message);
    }
    
    public OutOfStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
