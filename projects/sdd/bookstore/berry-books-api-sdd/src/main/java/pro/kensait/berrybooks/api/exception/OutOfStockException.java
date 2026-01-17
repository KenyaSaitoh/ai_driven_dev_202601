package pro.kensait.berrybooks.api.exception;

/**
 * OutOfStockException - 在庫不足例外
 * 
 * 責務:
 * - 在庫不足時にスローされる例外
 * - ビジネス例外
 * 
 * 使用場所:
 * - OrderService.orderBooks()
 * 
 * HTTPステータス:
 * - 409 Conflict（OutOfStockExceptionMapperで処理）
 */
public class OutOfStockException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * メッセージ付きコンストラクタ
     * 
     * @param message エラーメッセージ
     */
    public OutOfStockException(String message) {
        super(message);
    }
    
    /**
     * メッセージと原因付きコンストラクタ
     * 
     * @param message エラーメッセージ
     * @param cause 原因
     */
    public OutOfStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
